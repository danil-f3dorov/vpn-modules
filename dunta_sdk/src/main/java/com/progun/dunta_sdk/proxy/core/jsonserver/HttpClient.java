package com.progun.dunta_sdk.proxy.core.jsonserver;


import androidx.annotation.NonNull;

import com.progun.dunta_sdk.proxy.core.DeviceInfo;
import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.exception.IllegalStateResponseCodeException;
import com.progun.dunta_sdk.proxy.exception.JsonRequestParametersErrorException;
import com.progun.dunta_sdk.proxy.exception.ParseJSONNodeException;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.utils.LogWrap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Класс описывающий клиент для отправки http запроса на JSON сервер.
 * Определяет заголовки запроса и JSON, отправляемый на сервер
 */
public final class HttpClient {

    public static final String EMPTY_IP_ADDRESS = "0.0.0.0";
    public static final String JSON_IP_NODE = "ip";
    public static final String JSON_PORT_NODE = "port";

    public static final String JSON_RESULT_CODE_NODE = "resultCode";
    private final String TAG = HttpClient.class.getSimpleName();
    private final String serverUrl;

    private final int JSON_SERVER_RESPONSE_SUCCESS = 0;
    private final int JSON_SERVER_RESPONSE_NO_SUCH_IDS = 3;
    private final int JSON_SERVER_RESPONSE_PARAMETER_ERROR = 4;
    private final int JSON_SERVER_RESPONSE_NO_FREE_SERVER = 5;

    /*
    'success'     => 0,
    'empty'       => 3, // requested element not found (partnerId or appId)
    'parameter'   => 4, // some parameter was missing or in wrong type
    'allServers'  => 5, // all servers - full
    */

    private JsonInitListener jsonInitListener;
    private boolean isAlreadyInitOnTheServer;



    public HttpClient(
            String serverUrl,
            JsonInitListener jsonInitListener,
            boolean isAlreadyInitOnTheServer
    ) {
        this.serverUrl = serverUrl;
        this.jsonInitListener = jsonInitListener;
        this.isAlreadyInitOnTheServer = isAlreadyInitOnTheServer;
    }

    private void markJsonServerInitialize(boolean isAlreadyInitOnTheServer) {
        jsonInitListener.initialize();
        this.isAlreadyInitOnTheServer = isAlreadyInitOnTheServer;
    }

    @NonNull
    public JsonResponseResult getJsonServerAddressProd(
            @NonNull DeviceInfo deviceInfo, int partnerId, int appId, int advId
    ) throws JsonRequestParametersErrorException, ParseJSONNodeException {
        try {
            URL url = new URL(serverUrl);
            LogWrap.d(TAG, "Connecting to JSON server, ip: " + url.getHost());

//            if (BuildConfig.BUILD_TYPE.equals("debug")) {
//
//            } else if (BuildConfig.BUILD_TYPE.equals("releaseTest")) {
//
//            } else if (BuildConfig.BUILD_TYPE.equals("release")) {
//
//            }

            //HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");


            // Creates JSON request body
            JSONObject jsonRequest = collectJsonRequest(deviceInfo, partnerId, appId, advId);
            sendRequest(jsonRequest.toString(), httpURLConnection);

            // Retrieves response from JSON server
            return getServerResponse(httpURLConnection, jsonRequest);
        } catch (IOException exc) {
            return new JsonResponseResult.UnexpectedException(exc, "Connection with");
        }
    }

    @NonNull
    private JsonResponseResult getServerResponse(
            HttpURLConnection urlConnection, @NonNull JSONObject request
    ) throws IOException, ParseJSONNodeException, JsonRequestParametersErrorException {
        int responseCode = urlConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String inputLine;
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                while ((inputLine = in.readLine()) != null)
                    responseBody.append(inputLine);
            }

            LogWrap.i(TAG, "<------ Response: " + responseBody);
            JSONObject response;
            int statusResponse;
            try {
                response = new JSONObject(responseBody.toString());
                try {
                    statusResponse = response.getInt(JSON_RESULT_CODE_NODE);
                } catch (JSONException exc) {
                    throw new ParseJSONNodeException("Cant parse" + JSON_RESULT_CODE_NODE + " node. body=" + responseBody, exc);
                }
            } catch (JSONException exc) {
                LogWrap.e(TAG, "Error with parse JSON response by:" + exc + "=" + responseBody);
                return new JsonResponseResult.ParseError(exc, "JSON parse error, code=" + responseCode + " body=" + responseBody);
            }
            return handleResponseStatusCode(statusResponse, responseBody, response, request);
        } else
            return new JsonResponseResult.ResponseFailure(responseCode);
    }

    private JsonResponseResult handleResponseStatusCode(
            int statusResponse,
            StringBuilder responseBody,
            JSONObject response,
            @NonNull JSONObject request
    ) throws ParseJSONNodeException, JsonRequestParametersErrorException {
        String ip;
        String port;
        JsonResponseResult statusCodeResult;
        switch (statusResponse) {
            case JSON_SERVER_RESPONSE_SUCCESS:
                try {
                    ip = response.getString(JSON_IP_NODE);
                    port = response.getString(JSON_PORT_NODE);
                } catch (JSONException jsonExc) {
                    throw new ParseJSONNodeException("Cant parse " + JSON_IP_NODE + " or " + JSON_PORT_NODE + " node from json-response. body=" + responseBody, jsonExc);
                }
                markJsonServerInitialize(true);

                if (ProxyClient.DEBUG)
                    ip = handleInvalidIpAddressResponse(ip);

                if (ip.equals(EMPTY_IP_ADDRESS) || port.equals("0"))
                    return new JsonResponseResult.ResponseNoFreeServers();

                int portNum;
                try {
                    portNum = Integer.parseInt(port);
                } catch (NumberFormatException nfe) {
                    return new JsonResponseResult.UnexpectedException(nfe, "Convert string port value to number format failed");
                }

                return new JsonResponseResult.ResponseSuccess(ip, portNum);
            case JSON_SERVER_RESPONSE_NO_FREE_SERVER:
                markJsonServerInitialize(true);
                return new JsonResponseResult.ResponseNoFreeServers();
            case JSON_SERVER_RESPONSE_NO_SUCH_IDS:
                return new JsonResponseResult.ResponseNoSuchID(request.toString(), response.toString());
            case JSON_SERVER_RESPONSE_PARAMETER_ERROR:
                throw new JsonRequestParametersErrorException("Current request is: " + request + ", response: " + ((response == null) ? "null" : response)  );
            default:
                    throw new IllegalStateResponseCodeException("Unexpected server response code=" + statusResponse);
        }
    }

    private void sendRequest(
            String jsonRequest,
            HttpURLConnection urlConnection
    ) throws IOException {
        byte[] request = jsonRequest.getBytes();
        OutputStream os = urlConnection.getOutputStream();
        os.write(request, 0, request.length);
        os.close();
        LogWrap.i(TAG, "------> Request: " + jsonRequest);
    }

    @NonNull
    private JSONObject collectJsonRequest(
            @NonNull DeviceInfo deviceInfo,
            int partnerId,
            int appId,
            int advId
    ) {
        JSONObject jsonRequest = null;
        try {

            if (isAlreadyInitOnTheServer) {
                jsonRequest = new JSONObject()
                        .put(ProtocolConstants.JsonNames.PARTNER_ID_NODE, partnerId)
                        .put(ProtocolConstants.JsonNames.APP_ID_NODE, appId)
                        .put(ProtocolConstants.JsonNames.JSON_PROTOCOL_VERSION_NODE, ProtocolConstants.JSON_PROTO_VERSION);
            } else {
                jsonRequest = new JSONObject()
                        .put(ProtocolConstants.JsonNames.PARTNER_ID_NODE, partnerId)
                        .put(ProtocolConstants.JsonNames.APP_ID_NODE, appId)
                        .put(ProtocolConstants.JsonNames.JSON_PROTOCOL_VERSION_NODE, ProtocolConstants.JSON_PROTO_VERSION)
                        .put(ProtocolConstants.JsonNames.SYSTEM_INFO_NODE, new JSONObject()
                                .put(ProtocolConstants.JsonNames.SDK_VERSION_NODE, String.valueOf(deviceInfo.getSdkVersion()))
                                .put(ProtocolConstants.JsonNames.CPU_ARCH_NODE, String.valueOf(deviceInfo.getCpuArch()))
                                .put(ProtocolConstants.JsonNames.SERVER_PROTOCOL_VERSION_NODE, ProtocolConstants.SRV_PROTO_VERSION)
                                .put(ProtocolConstants.JsonNames.PHONE_MODEL_NODE, String.valueOf(deviceInfo.getDeviceModel()))
                                .put(ProtocolConstants.JsonNames.TOTAL_RAM_NODE, deviceInfo.getTotalRam())
                                .put(ProtocolConstants.JsonNames.ANDROID_ID_NODE, deviceInfo.getDeviceId())
                                .put(ProtocolConstants.JsonNames.ADVERTISEMENT_ID_NODE, advId)
                        );
            }
        } catch (JSONException e) {
            throw new RuntimeException("Collection JSON: has error", e);
        }
        return jsonRequest;
    }

    @NonNull
    private String handleInvalidIpAddressResponse(String ip) {
        String testOutsideServer = "185.180.221.100";
        String testLocalServer = "192.168.46.220";
        //String testLocalServer = "192.168.46.91";

        if (ip.equals("192.168.46.150"))
            ip = testLocalServer;

        return ip;
    }
}
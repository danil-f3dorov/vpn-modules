package com.progun.dunta_sdk.proxy.core.jsonserver;

import org.json.JSONException;

public interface JsonResponseResult {
    final class ResponseSuccess implements JsonResponseResult {
        final String serverAddress;
        final int serverPort;

        public ResponseSuccess(String serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        public String getServerAddress() {
            return serverAddress;
        }

        public int getServerPort() {
            return serverPort;
        }
    }
    //    final class ResponseNoSuchID implements JsonResponseResult {}
    final class ResponseNoSuchID implements JsonResponseResult{
        final String request;
        final String response;

        public ResponseNoSuchID(String request, String response) {
            this.request = request;
            this.response = response;
        }

        public String getRequest() {
            return request;
        }

        public String getResponse() {
            return response;
        }
    }
    final class ResponseNoFreeServers implements JsonResponseResult {}

    /**
     * Returns when client cant parse JSON response. For example, when response consists of HTML instead of JSON.
     */
    final class ParseError implements JsonResponseResult {
        final JSONException exc;
        final String msg;

        public ParseError(JSONException exc, String msg) {
            this.exc = exc;
            this.msg = msg;
        }

        public JSONException getExc() {
            return exc;
        }

        public String getMsg() {
            return msg;
        }
    }

    /**
     * Returns when HTTP response code is NOT_OK(code does not equals 200)
     */
    final class ResponseFailure implements JsonResponseResult {
        final int responseCode;
        public ResponseFailure(int responseCode) {
            this.responseCode = responseCode;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }

    final class UnexpectedException implements JsonResponseResult {
        final Exception exc;
        final String msg;

        public UnexpectedException(Exception exc, String msg) {
            this.exc = exc;
            this.msg = msg;
        }

        public UnexpectedException(Exception exc) {
            this(exc, "");
        }

        public Exception getExc() {
            return exc;
        }

        public String getMsg() {
            return msg;
        }
    }
}

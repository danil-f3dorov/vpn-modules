package com.progun.dunta_sdk.android.referrer;

import static com.progun.dunta_sdk.utils.LogWrap.d;
import static com.progun.dunta_sdk.utils.LogWrap.v;

import android.content.Context;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.progun.dunta_sdk.proxy.utils.ReferrerConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReferrerMgr {
    private ReferrerResultListener resultListener;
    private final Context context;

    private final String referrerExtractPattern;

    public ReferrerMgr(
            @NonNull ReferrerResultListener listener,
            @NonNull Context context,
            @NonNull String referrerExtractPattern
    ) {
        resultListener = listener;
        this.context = context;
        this.referrerExtractPattern = referrerExtractPattern;
    }

    private static final String TAG = ReferrerMgr.class.getSimpleName();
    private InstallReferrerClient referrerClient = null;
    private int referrerCurrentReconnections = 0;
    private InstallReferrerStateListener installReferrerStateListener = null;


    private boolean startConnection() {
        if (referrerClient == null)
            referrerClient = InstallReferrerClient.newBuilder(context).build();
        if (installReferrerStateListener == null)
            installReferrerStateListener = createRefListener();
        if (referrerClient.isReady())
            referrerClient.startConnection(installReferrerStateListener);
        else return false;
        return true;
    }

    private void endConnection() {
        if (referrerClient != null)
            referrerClient.endConnection();
//        installReferrerStateListener = null;
    }

    private void handleReferrerResponseOK() {
        d(TAG, "referrer code = OK");
        try {
            int referrerId;
            ReferrerDetails details = referrerClient.getInstallReferrer();
            String referrerUrl = details.getInstallReferrer();

            String parsedDataFromUrl;
            parsedDataFromUrl = parseRefUrl(referrerUrl, referrerExtractPattern);

            if (parsedDataFromUrl != null) {
                if (parsedDataFromUrl.equals(ReferrerConsts.DEFAULT_RESPONSES.GOOGLE_PLAY)) {
                    referrerId = ReferrerConsts.MARKS.DEFAULT_GOOGLE_PLAY_RESPONSE;
                    LogWrap.w(TAG, "referrer value=" + parsedDataFromUrl + " is google-play");
                } else {
                    referrerId = Integer.parseInt(parsedDataFromUrl);
                    LogWrap.w(TAG, "starting parse to int referrer value=" + parsedDataFromUrl);
                }
            } else {
                d(TAG, "referrer id = null");
                referrerId = ReferrerConsts.MARKS.RESPONSE_PARSE_ERROR;
            }
            endConnection();
            resultListener.onComplete(referrerId);
        } catch (RemoteException | NumberFormatException e) {
            endConnection();
            resultListener.onComplete(ReferrerConsts.MARKS.RESPONSE_PARSE_ERROR);
        }
    }

    private void handleReferrerResponseUnavailable() {
        d(TAG, "referrer code = SERVICE_UNAVAILABLE");
        endConnection();
        resultListener.onComplete(ReferrerConsts.MARKS.DEFAULT_GOOGLE_PLAY_RESPONSE);
    }

    private void handleReferrerResponseFeatureNotSupported() {
        d(TAG, "referrer code = FEATURE_NOT_SUPPORTED");
        endConnection();
        resultListener.onComplete(ReferrerConsts.MARKS.FEATURE_NOT_SUPPORTED);
    }

    private void handleServiceDisconnected() {
        v(TAG, "onInstallReferrerServiceDisconnected() has called");
        endConnection();
        if (referrerCurrentReconnections < ReferrerConsts.MAX_RECONNECTIONS_COUNT) {
            referrerCurrentReconnections++;
//            resultListener.onDisconnected();
        } else {
            resultListener.onComplete(ReferrerConsts.MARKS.DEFAULT_GOOGLE_PLAY_RESPONSE);
        }
    }

    private InstallReferrerStateListener createRefListener() {
        return new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int response) {
                switch (response) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        handleReferrerResponseOK();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        handleReferrerResponseFeatureNotSupported();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        handleReferrerResponseUnavailable();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED:
                        handleServiceDisconnected();
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                handleServiceDisconnected();
            }
        };
    }

    private String parseRefUrl(String inputString, String patternString) {
        v(TAG, "RefParser.parse()");
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(inputString);

        if (matcher.find())
            return matcher.group(1);
        return null;
    }
}

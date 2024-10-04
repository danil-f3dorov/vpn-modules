package com.progun.dunta_sdk.proxy.core.report;

import android.content.Context;

import com.progun.dunta_sdk.proxy.utils.ProxySettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReportProvider {
    public final String FILE_NAME = "report_file.txt";
    private final Context context;

    public ReportProvider(Context context) {
        this.context = context;
    }

    public boolean isReportsExists() {
        return context.getFileStreamPath(FILE_NAME).exists();
    }

    public boolean deleteReport() {
        return context.getFileStreamPath(FILE_NAME).delete();
    }

    public String readReport() {
        try {
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(context.openFileInput(FILE_NAME)));

            StringBuilder sb = new StringBuilder();
            String line;

            while (((line = bufferedReader.readLine()) != null) && (sb.length() < 4096))
                sb.append(line).append(System.lineSeparator());
            bufferedReader.close();

            if (sb.length() > ProxySettings.PROXY_MAX_DATA_LEN)
                return sb.substring(0, ProxySettings.PROXY_MAX_DATA_LEN);

            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
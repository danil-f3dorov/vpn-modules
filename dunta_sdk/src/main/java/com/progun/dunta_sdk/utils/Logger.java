package com.progun.dunta_sdk.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String LOG_FILE_NAME = "sdk_log.txt";

    private File getLogFile() {
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }
        return new File(downloadDirectory, LOG_FILE_NAME);
    }

    private void writeLogMessage(String message) {
        try (FileOutputStream fos = new FileOutputStream(getLogFile(), true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fos.write((timestamp + ": " + message + "\n").getBytes());
        } catch (IOException ioException) {
            writeLogMessage(ioException.getMessage());
        }
    }

    public void logSdkStart() {
        writeLogMessage("SDK started");
    }

    public void logSdkStop() {
        writeLogMessage("SDK stopped");
    }
}
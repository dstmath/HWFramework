package com.android.server.wifi.grs.utils;

import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Io {
    private static final int BUFF_SIZE = 4096;
    private static final int MAX_BYTE = 1024;
    private static final String TAG = Io.class.getSimpleName();

    private Io() {
    }

    private static void closeQuietly(InputStream input) {
        closeQuietly((Closeable) input);
    }

    private static void closeQuietly(OutputStream output) {
        closeQuietly((Closeable) output);
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.d(TAG, "closeQuietly IOException");
            }
        }
    }

    public static String getConfigContent(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.d(TAG, "getConfigContent fileName is null.");
            return "";
        } else if (ContextUtil.getContext() == null) {
            Log.d(TAG, "getConfigContent, context null");
            return "";
        } else {
            InputStream myInput = null;
            ByteArrayOutputStream myOutput = new ByteArrayOutputStream();
            try {
                myInput = ContextUtil.getContext().getAssets().open(fileName);
                byte[] buffer = new byte[1024];
                while (true) {
                    int n = myInput.read(buffer);
                    if (n != -1) {
                        myOutput.write(buffer, 0, n);
                    } else {
                        myOutput.flush();
                        return new String(myOutput.toByteArray(), "UTF-8");
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "getConfigContent IOException: ");
                return "";
            } finally {
                closeQuietly((OutputStream) myOutput);
                closeQuietly(myInput);
            }
        }
    }
}

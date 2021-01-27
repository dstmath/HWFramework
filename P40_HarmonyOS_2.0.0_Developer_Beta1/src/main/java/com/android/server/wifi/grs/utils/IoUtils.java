package com.android.server.wifi.grs.utils;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {
    private static final int BUFF_SIZE = 4096;
    private static final int MAX_SIZE = 16777216;
    private static final String TAG = "IoUtils";

    private IoUtils() {
    }

    public static void closeSecure(InputStream input) {
        closeSecure((Closeable) input);
    }

    public static void closeSecure(OutputStream output) {
        closeSecure((Closeable) output);
    }

    private static void closeSecure(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.d("IOUtil", "closeSecure IOException");
            }
        }
    }

    private static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0;
        while (true) {
            int n = input.read(buffer);
            if (n == -1) {
                return count;
            }
            if (count <= 16777216) {
                output.write(buffer, 0, n);
                count += (long) n;
            } else {
                throw new IOException("input data too large for byte.");
            }
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }
}

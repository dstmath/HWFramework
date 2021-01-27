package com.huawei.secure.android.common.util;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class IOUtil {
    private static final int BUFF_SIZE = 4096;
    private static final String TAG = "IOUtil";

    public static void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void closeSecure(Reader reader) {
        closeSecure((Closeable) reader);
    }

    public static void closeSecure(Writer writer) {
        closeSecure((Closeable) writer);
    }

    public static void closeSecure(InputStream input) {
        closeSecure((Closeable) input);
    }

    public static void closeSecure(OutputStream output) {
        closeSecure((Closeable) output);
    }

    public static void closeSecure(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, "closeSecure IOException");
            }
        }
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, new byte[4096]);
    }

    public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        while (true) {
            int n = input.read(buffer);
            if (-1 == n) {
                return count;
            }
            output.write(buffer, 0, n);
            count += (long) n;
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static InputStream toInputStream(byte[] input) throws IOException {
        return new ByteArrayInputStream(input);
    }

    public static void deleteSecure(File file) {
        if (file != null && file.exists() && !file.delete()) {
            Log.e(TAG, "deleteSecure exception");
        }
    }

    public static void deleteSecure(String file) {
        if (!TextUtils.isEmpty(file)) {
            deleteSecure(new File(file));
        }
    }
}

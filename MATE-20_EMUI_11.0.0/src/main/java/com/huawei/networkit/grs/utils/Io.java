package com.huawei.networkit.grs.utils;

import android.database.Cursor;
import android.text.TextUtils;
import com.huawei.networkit.grs.common.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class Io {
    private static final int BUFF_SIZE = 4096;
    private static final String TAG = Io.class.getSimpleName();

    public static void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void closeQuietly(Reader reader) {
        closeQuietly((Closeable) reader);
    }

    public static void closeQuietly(Writer writer) {
        closeQuietly((Closeable) writer);
    }

    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable) input);
    }

    public static void closeQuietly(OutputStream output) {
        closeQuietly((Closeable) output);
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Logger.e(TAG, "closeQuietly IOException", e);
            }
        }
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, new byte[4096]);
    }

    public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        while (true) {
            int crumb = input.read(buffer);
            if (crumb == -1) {
                return count;
            }
            output.write(buffer, 0, crumb);
            count += (long) crumb;
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

    public static void deleteQuietly(File file) {
        if (file != null && !file.exists() && !file.delete()) {
            Logger.w(TAG, "deleteQuietly exception");
        }
    }

    public static void deleteQuietly(String file) {
        if (file != null) {
            deleteQuietly(new File(file));
        }
    }

    public static String getConfigContent(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Logger.w(TAG, "getConfigContent fileName is null.");
            return "";
        }
        InputStream myInput = null;
        ByteArrayOutputStream myOutput = new ByteArrayOutputStream();
        try {
            myInput = ContextUtil.getContext().getAssets().open(fileName);
            byte[] buffer = new byte[8192];
            while (true) {
                int crumb = myInput.read(buffer);
                if (crumb != -1) {
                    myOutput.write(buffer, 0, crumb);
                } else {
                    myOutput.flush();
                    return new String(myOutput.toByteArray(), "UTF-8");
                }
            }
        } catch (IOException e) {
            Logger.w(TAG, "local config file is not exist.");
            return "";
        } finally {
            closeQuietly((OutputStream) myOutput);
            closeQuietly(myInput);
        }
    }
}

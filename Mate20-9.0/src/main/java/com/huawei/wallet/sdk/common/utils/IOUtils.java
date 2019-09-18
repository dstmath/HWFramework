package com.huawei.wallet.sdk.common.utils;

import android.database.Cursor;
import com.huawei.wallet.sdk.common.log.LogC;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class IOUtils {
    private static final int BUFF_SIZE = 4096;

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
                LogC.e("closeQuietly IOException", false);
            }
        }
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, new byte[BUFF_SIZE]);
    }

    public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        while (true) {
            int read = input.read(buffer);
            int n = read;
            if (-1 == read) {
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

    public static void deleteQuietly(File file) {
        if (file != null && !file.exists() && !file.delete()) {
            LogC.e("deleteQuietly exception", false);
        }
    }

    public static void deleteQuietly(String file) {
        if (file != null) {
            deleteQuietly(new File(file));
        }
    }

    public static boolean deleteFileTree(File file) {
        boolean bDel = false;
        if (file == null) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }
            for (File deleteFileTree : childFiles) {
                deleteFileTree(deleteFileTree);
            }
            bDel = file.delete();
        }
        return bDel;
    }
}

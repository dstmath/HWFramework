package com.huawei.android.pushagent.utils;

import com.huawei.android.pushagent.utils.d.c;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public abstract class d {
    public static void uw(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                c.sj("PushLog2951", "close IOException");
            }
        }
    }

    public static void ux(HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            try {
                httpURLConnection.disconnect();
            } catch (Throwable th) {
                c.sj("PushLog2951", "close HttpURLConnection Exception");
            }
        }
    }

    public static String uy(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = inputStream.read();
            if (-1 != read) {
                byteArrayOutputStream.write(read);
            } else {
                String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
                uw(inputStream);
                return byteArrayOutputStream2;
            }
        }
    }

    public static String uv(BufferedReader bufferedReader) {
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            int read = bufferedReader.read();
            if (read == -1) {
                break;
            }
            char c = (char) read;
            if (c == 10 || c == 13) {
                break;
            } else if (stringBuffer.length() >= 2097152) {
                c.sf("PushLog2951", "read date exceed the max size!");
                return null;
            } else {
                stringBuffer.append(c);
            }
        }
        return stringBuffer.toString();
    }
}

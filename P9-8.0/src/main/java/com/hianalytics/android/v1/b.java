package com.hianalytics.android.v1;

import com.hianalytics.android.a.a.a;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public final class b {
    private static HttpsURLConnection a = null;

    public static boolean a(String str, byte[] bArr) {
        OutputStream outputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            a.h();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(bArr.length));
            outputStream = httpURLConnection.getOutputStream();
            outputStream.write(bArr);
            outputStream.flush();
            int responseCode = httpURLConnection.getResponseCode();
            "connHttp.getResponseCode() = " + responseCode;
            a.h();
            boolean z = responseCode == SmsCheckResult.ESCT_200;
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                a.h();
            }
            return z;
        } catch (IOException e2) {
            "connHttp error:" + e2.getMessage();
            a.h();
            e2.printStackTrace();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                a.h();
            }
            return false;
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                a.h();
            }
        }
    }
}

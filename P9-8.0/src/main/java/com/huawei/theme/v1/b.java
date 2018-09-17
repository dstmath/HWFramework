package com.huawei.theme.v1;

import com.huawei.theme.a.a.a;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class b {
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0084 A:{SYNTHETIC, Splitter: B:20:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009a A:{SYNTHETIC, Splitter: B:30:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x009a A:{SYNTHETIC, Splitter: B:30:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x009f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(String str, byte[] bArr) {
        HttpURLConnection httpURLConnection;
        IOException e;
        Throwable th;
        OutputStream outputStream = null;
        boolean z = true;
        try {
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) new URL(str).openConnection();
            try {
                a.h();
                httpURLConnection2.setRequestMethod("POST");
                httpURLConnection2.setConnectTimeout(5000);
                httpURLConnection2.setDoOutput(true);
                httpURLConnection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                httpURLConnection2.setRequestProperty("Content-Length", String.valueOf(bArr.length));
                outputStream = httpURLConnection2.getOutputStream();
                outputStream.write(bArr);
                outputStream.flush();
                int responseCode = httpURLConnection2.getResponseCode();
                "ResponseCode:" + responseCode;
                a.h();
                if (responseCode != 200) {
                    z = false;
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (httpURLConnection2 == null) {
                    return z;
                }
                httpURLConnection2.disconnect();
                a.h();
                return z;
            } catch (IOException e3) {
                IOException iOException = e3;
                httpURLConnection = httpURLConnection2;
                e = iOException;
                try {
                    "IOException:" + e.getMessage();
                    a.h();
                    e.printStackTrace();
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                        a.h();
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStream != null) {
                    }
                    if (httpURLConnection != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                Throwable th4 = th3;
                httpURLConnection = httpURLConnection2;
                th = th4;
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
                throw th;
            }
        } catch (IOException e5) {
            e4 = e5;
            httpURLConnection = null;
            "IOException:" + e4.getMessage();
            a.h();
            e4.printStackTrace();
            if (outputStream != null) {
            }
            if (httpURLConnection != null) {
            }
            return false;
        } catch (Throwable th5) {
            th = th5;
            httpURLConnection = null;
            if (outputStream != null) {
            }
            if (httpURLConnection != null) {
            }
            throw th;
        }
    }
}

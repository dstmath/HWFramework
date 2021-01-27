package ohos.miscservices.httpaccess;

import android.content.Context;
import android.content.pm.PackageManager;
import android.security.NetworkSecurityPolicy;
import android.security.net.config.ApplicationConfig;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.httpaccess.data.RequestData;

public class HttpUtils {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "HttpUtils");

    public static void closeQuietly(InputStream inputStream) {
        closeQuietly((Closeable) inputStream);
    }

    public static void closeQuietly(OutputStream outputStream) {
        closeQuietly((Closeable) outputStream);
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                HiLog.error(TAG, "closeQuietly IOException!", new Object[0]);
            }
        }
    }

    public static SSLContext getSSLContextForPackage(Context context, String str) throws GeneralSecurityException {
        try {
            ApplicationConfig applicationConfigForPackage = NetworkSecurityPolicy.getApplicationConfigForPackage(context, str);
            SSLContext instance = SSLContext.getInstance("TLS");
            instance.init(null, new TrustManager[]{applicationConfigForPackage.getTrustManager()}, null);
            return instance;
        } catch (PackageManager.NameNotFoundException unused) {
            HiLog.error(TAG, "caught package NameNotFoundException!", new Object[0]);
            return SSLContext.getDefault();
        }
    }

    public static void addHeaders(URLConnection uRLConnection, Map<String, String> map) {
        if (!(map == null || map.isEmpty())) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!(key == null || key.length() == 0 || value == null || value.length() == 0)) {
                    uRLConnection.setRequestProperty(key, value);
                }
            }
        }
    }

    public static void setConnProperty(HttpURLConnection httpURLConnection, RequestData requestData, Context context) {
        if (httpURLConnection == null || requestData == null || context == null) {
            HiLog.error(TAG, "param of the method can not be null!", new Object[0]);
            return;
        }
        httpURLConnection.setConnectTimeout(HttpConstant.TIME_OUT);
        httpURLConnection.setReadTimeout(HttpConstant.TIME_OUT);
        if (requestData.getMethod().equals(HttpConstant.HTTP_METHOD_POST) || requestData.getMethod().equals(HttpConstant.HTTP_METHOD_PUT)) {
            HiLog.debug(TAG, "set doOutPut property!", new Object[0]);
            httpURLConnection.setDoOutput(true);
        }
        addHeaders(httpURLConnection, requestData.getHeader());
        if (httpURLConnection.getRequestProperty("Connection") == null) {
            httpURLConnection.addRequestProperty("Connection", "Keep-Alive");
        }
        if (httpURLConnection instanceof HttpsURLConnection) {
            try {
                HiLog.debug(TAG, "https connection, set sslContext!", new Object[0]);
                ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(getSSLContextForPackage(context, context.getApplicationInfo().packageName).getSocketFactory());
            } catch (GeneralSecurityException unused) {
                HiLog.error(TAG, "caught GeneralSecurityException!", new Object[0]);
            }
        }
    }

    public static byte[] parseInputStream(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        while (true) {
            try {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            } catch (IOException unused) {
                HiLog.error(TAG, "parseInputStream caught IOException!", new Object[0]);
            } catch (Throwable th) {
                closeQuietly((OutputStream) byteArrayOutputStream);
                throw th;
            }
        }
        closeQuietly((OutputStream) byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Map<String, List<String>> analyzeResponseHeaders(Map<String, List<String>> map) {
        HashMap hashMap = new HashMap();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.contains("Android")) {
                hashMap.put(key, entry.getValue());
            }
        }
        return hashMap;
    }
}

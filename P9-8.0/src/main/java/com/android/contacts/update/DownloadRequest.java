package com.android.contacts.update;

import android.content.Context;
import com.android.contacts.external.separated.HsOpenSSLSocketFactory;
import com.android.contacts.update.utils.HsTrustManager;
import com.android.contacts.update.utils.HttpsWakeLockHelper;
import com.android.contacts.util.HwLog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadRequest {
    public static final int CONNECTED_TIME_OUT = 10000;
    private static final String ENCODE_TYPE = "UTF-8";
    private static final String HTTP_IDENTITY_ENCODE = "identity";
    private static final String HTTP_POST = "POST";
    private static final String KEY_FILEID = "fileId";
    private static final String KEY_VER = "ver";
    public static final int READ_TIME_OUT = 10000;
    private static final String TAG = DownloadRequest.class.getSimpleName();
    private static final String URL_QUERY_CONFIG = "https://configserver.hicloud.com/servicesupport/updateserver/getLatestVersion";
    private String fileId;
    private String ver;

    public DownloadRequest(String fileId, String ver) {
        this.fileId = fileId;
        this.ver = ver;
    }

    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_FILEID, this.fileId);
        json.put(KEY_VER, this.ver);
        return json.toString();
    }

    public InputStream doHttpsPost(Context context) throws JSONException {
        return doPostRequest(URL_QUERY_CONFIG, toJson(), false, context);
    }

    private static InputStream doPostRequest(String strUrl, String params, boolean enableGZIP, Context context) {
        try {
            URL url = new URL(strUrl);
            URL url2;
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new HsTrustManager()}, new SecureRandom());
                SSLSocketFactory socketFactory = sc.getSocketFactory();
                if (socketFactory == null) {
                    HwLog.e(TAG, "socketFactory is null error!");
                    url2 = url;
                    return null;
                }
                HttpsURLConnection.setDefaultSSLSocketFactory(new HsOpenSSLSocketFactory(socketFactory).getOpenSSLSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                try {
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    HwLog.d(TAG, "doPostRequest openConnection already!");
                    try {
                        urlConnection.setRequestMethod(HTTP_POST);
                        byte[] data = null;
                        if (params != null) {
                            try {
                                data = params.getBytes(ENCODE_TYPE);
                            } catch (UnsupportedEncodingException e) {
                                HwLog.e(TAG, e.getMessage() + "UnsupportedEncodingException");
                            }
                        }
                        if (data == null) {
                            HwLog.e(TAG, "Get null post data!");
                            url2 = url;
                            return null;
                        }
                        urlConnection.setUseCaches(false);
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        urlConnection.setConnectTimeout(10000);
                        urlConnection.setReadTimeout(10000);
                        urlConnection.setRequestProperty("Content-Type", "application/json");
                        urlConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
                        urlConnection.setRequestProperty("Connection", "keep-alive");
                        if (!enableGZIP) {
                            urlConnection.setRequestProperty("Accept-Encoding", HTTP_IDENTITY_ENCODE);
                        }
                        HwLog.d(TAG, "setRequestProperty jsonObject already!");
                        HttpsWakeLockHelper wakeLock = new HttpsWakeLockHelper();
                        wakeLock.createWakeLockAndAcquire(context);
                        HwLog.d(TAG, "Get wakeLock now");
                        InputStream response = null;
                        try {
                            response = getHttpsResponsesStream(data, urlConnection);
                        } catch (Exception e2) {
                            HwLog.e(TAG, "getHttpsResponsesStream exception!");
                        } finally {
                            wakeLock.releaseWakeLock();
                            HwLog.d(TAG, "Release wakeLock now");
                        }
                        url2 = url;
                        return response;
                    } catch (ProtocolException e3) {
                        HwLog.e(TAG, e3.getMessage() + "setRequestMethod failed");
                        url2 = url;
                        return null;
                    }
                } catch (IOException e4) {
                    HwLog.e(TAG, e4.getMessage() + "openConnection failed");
                    url2 = url;
                    return null;
                }
            } catch (NoSuchAlgorithmException e1) {
                HwLog.e(TAG, "NoSuchAlgorithmException " + e1.getMessage());
                url2 = url;
                return null;
            } catch (KeyManagementException e22) {
                HwLog.e(TAG, "KeyManagementException " + e22.getMessage());
                url2 = url;
                return null;
            }
        } catch (MalformedURLException e5) {
            HwLog.e(TAG, e5.getMessage());
            return null;
        }
    }

    private static InputStream getHttpsResponsesStream(byte[] resquestData, HttpsURLConnection httpsConnection) {
        OutputStream outputStream = null;
        try {
            httpsConnection.connect();
            outputStream = httpsConnection.getOutputStream();
            outputStream.write(resquestData);
            HwLog.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            return getInputStream(httpsConnection);
        } catch (IOException e) {
            HwLog.e(TAG, e.getMessage());
            HwLog.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            return null;
        } catch (Throwable th) {
            HwLog.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            throw th;
        }
    }

    private static InputStream getInputStream(HttpsURLConnection httpsConnection) {
        InputStream respnseStream = null;
        try {
            return httpsConnection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            HwLog.e(TAG, "UnsupportedEncodingException ", e);
            return respnseStream;
        } catch (IOException e2) {
            HwLog.e(TAG, "IOException ", e2);
            return respnseStream;
        }
    }

    private static void closeOutputStream(OutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                HwLog.e(TAG, e.getMessage());
            }
        }
    }
}

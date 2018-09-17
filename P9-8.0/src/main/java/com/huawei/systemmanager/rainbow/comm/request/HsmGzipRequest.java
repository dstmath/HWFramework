package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.util.Log;
import com.huawei.systemmanager.rainbow.comm.request.util.HttpsWakeLockHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.zip.GZIPOutputStream;

public class HsmGzipRequest implements ICommonRequest {
    private static final int CONNECTED_TIME_OUT = 10000;
    private static final String ENCODE_TYPE = "UTF-8";
    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";
    private static final int READ_TIME_OUT = 10000;
    private static final String TAG = "HsmGzipRequest";
    private int mConnectTimeout = 10000;
    private int mReadTimeout = 10000;

    public void setTimeout(int connectTimeout, int readTimeout) {
        this.mConnectTimeout = connectTimeout;
        this.mReadTimeout = readTimeout;
    }

    public String doGetRequest(String strUrl) {
        HttpURLConnection urlConnection = HsmJoinRequest.getHttpURLConnectionGet(strUrl);
        if (urlConnection == null) {
            Log.e(TAG, "doGetRequest urlConnection is null");
            return "";
        }
        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(this.mConnectTimeout);
        urlConnection.setReadTimeout(this.mReadTimeout);
        try {
            urlConnection.connect();
            return HsmJoinRequest.convertInputStreamToString(urlConnection);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    public String doPostRequest(String strUrl, Object params, boolean enableGZIP, Context context) {
        HttpURLConnection urlConnection = HsmJoinRequest.getHttpURLConnectionPost(strUrl);
        if (urlConnection == null) {
            Log.e(TAG, "doPostRequest urlConnection is null");
            return "";
        }
        byte[] data = null;
        try {
            data = params.toString().getBytes(ENCODE_TYPE);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage() + "UnsupportedEncodingException");
        }
        if (data == null) {
            Log.e(TAG, "Get null post data!");
            return "";
        }
        urlConnection.setUseCaches(false);
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        urlConnection.setRequestProperty("Content-Type", "application/x-gzip");
        urlConnection.setRequestProperty("Connection", "keep-alive");
        Log.d(TAG, "setRequestProperty jsonObject already!");
        HttpsWakeLockHelper wakeLock = new HttpsWakeLockHelper();
        wakeLock.createWakeLockAndAcquire(context);
        Log.d(TAG, "Get wakeLock now");
        String response = "";
        try {
            response = getHttpsResponsesString(data, urlConnection);
        } catch (Exception e2) {
            Log.e(TAG, "getHttpsResponsesString exception!", e2);
        } finally {
            wakeLock.releaseWakeLock();
            Log.d(TAG, "Release wakeLock now");
        }
        return response;
    }

    private String getHttpsResponsesString(byte[] resquestData, HttpURLConnection httpsConnection) {
        IOException e;
        String str;
        Throwable th;
        OutputStream outputStream = null;
        try {
            httpsConnection.connect();
            OutputStream outputStream2 = new GZIPOutputStream(httpsConnection.getOutputStream());
            try {
                outputStream2.write(resquestData);
                outputStream2.flush();
                Log.d(TAG, "doPostRequest finally closeOutputStream!");
                closeOutputStream(outputStream2);
                return HsmJoinRequest.convertInputStreamToString(httpsConnection);
            } catch (IOException e2) {
                e = e2;
                outputStream = outputStream2;
                try {
                    Log.e(TAG, "getHttpsResponsesString IOException", e);
                    str = "";
                    Log.d(TAG, "doPostRequest finally closeOutputStream!");
                    closeOutputStream(outputStream);
                    return str;
                } catch (Throwable th2) {
                    th = th2;
                    Log.d(TAG, "doPostRequest finally closeOutputStream!");
                    closeOutputStream(outputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outputStream = outputStream2;
                Log.d(TAG, "doPostRequest finally closeOutputStream!");
                closeOutputStream(outputStream);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            Log.e(TAG, "getHttpsResponsesString IOException", e);
            str = "";
            Log.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            return str;
        }
    }

    private static void closeOutputStream(OutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}

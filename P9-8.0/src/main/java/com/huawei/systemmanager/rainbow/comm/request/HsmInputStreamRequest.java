package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.util.Log;
import com.huawei.systemmanager.rainbow.comm.request.util.HttpsWakeLockHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import org.json.JSONObject;

public class HsmInputStreamRequest {
    private static final int CONNECTED_TIME_OUT = 10000;
    private static final String ENCODE_TYPE = "UTF-8";
    private static final String HTTP_GET = "GET";
    private static final String HTTP_IDENTITY_ENCODE = "identity";
    private static final String HTTP_POST = "POST";
    private static final int READ_TIME_OUT = 10000;
    private static final String TAG = "HsmInputStreamRequest";

    public InputStream doGetRequest(String strUrl) {
        HttpURLConnection urlConnection = HsmJoinRequest.getHttpURLConnectionGet(strUrl);
        if (urlConnection == null) {
            Log.e(TAG, "doGetRequest urlConnection is null");
            return null;
        }
        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        try {
            urlConnection.connect();
            return getInputStream(urlConnection);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public InputStream doPostRequest(String strUrl, Object params, boolean enableGZIP, Context context) {
        HttpURLConnection urlConnection = HsmJoinRequest.getHttpURLConnectionPost(strUrl);
        if (urlConnection == null) {
            Log.e(TAG, "doPostRequest urlConnection is null");
            return null;
        }
        byte[] data = null;
        try {
            data = ((JSONObject) params).toString().getBytes(ENCODE_TYPE);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage() + "UnsupportedEncodingException");
        }
        if (data == null) {
            Log.e(TAG, "Get null post data!");
            return null;
        }
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
        urlConnection.setRequestProperty("Connection", "keep-alive");
        if (!enableGZIP) {
            urlConnection.setRequestProperty("Accept-Encoding", HTTP_IDENTITY_ENCODE);
        }
        Log.d(TAG, "setRequestProperty jsonObject already!");
        HttpsWakeLockHelper wakeLock = new HttpsWakeLockHelper();
        wakeLock.createWakeLockAndAcquire(context);
        Log.d(TAG, "Get wakeLock now");
        InputStream response = null;
        try {
            response = getResponsesStream(data, urlConnection);
        } catch (Exception e2) {
            Log.e(TAG, "getResponsesStream exception!");
        } finally {
            wakeLock.releaseWakeLock();
            Log.d(TAG, "Release wakeLock now");
        }
        return response;
    }

    private InputStream getResponsesStream(byte[] resquestData, HttpURLConnection httpConnection) {
        OutputStream outputStream = null;
        try {
            httpConnection.connect();
            outputStream = httpConnection.getOutputStream();
            outputStream.write(resquestData);
            Log.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            return getInputStream(httpConnection);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Log.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            return null;
        } catch (Throwable th) {
            Log.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            throw th;
        }
    }

    private static InputStream getInputStream(HttpURLConnection httpConnection) {
        InputStream respnseStream = null;
        try {
            return httpConnection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException", e);
            return respnseStream;
        } catch (IOException e2) {
            Log.e(TAG, "IOException", e2);
            return respnseStream;
        }
    }

    private static void closeOutputStream(OutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}

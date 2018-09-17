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

    private java.io.InputStream getResponsesStream(byte[] r6, java.net.HttpURLConnection r7) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 14, 15} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r5 = this;
        r1 = 0;
        r7.connect();	 Catch:{ IOException -> 0x001c, all -> 0x0035 }
        r1 = r7.getOutputStream();	 Catch:{ IOException -> 0x001c, all -> 0x0035 }
        r1.write(r6);	 Catch:{ IOException -> 0x001c, all -> 0x0035 }
        r2 = "HsmInputStreamRequest";
        r3 = "doPostRequest finally closeOutputStream!";
        android.util.Log.d(r2, r3);
        closeOutputStream(r1);
        r2 = getInputStream(r7);
        return r2;
    L_0x001c:
        r0 = move-exception;
        r2 = "HsmInputStreamRequest";	 Catch:{ IOException -> 0x001c, all -> 0x0035 }
        r3 = r0.getMessage();	 Catch:{ IOException -> 0x001c, all -> 0x0035 }
        android.util.Log.e(r2, r3);	 Catch:{ IOException -> 0x001c, all -> 0x0035 }
        r2 = 0;
        r3 = "HsmInputStreamRequest";
        r4 = "doPostRequest finally closeOutputStream!";
        android.util.Log.d(r3, r4);
        closeOutputStream(r1);
        return r2;
    L_0x0035:
        r2 = move-exception;
        r3 = "HsmInputStreamRequest";
        r4 = "doPostRequest finally closeOutputStream!";
        android.util.Log.d(r3, r4);
        closeOutputStream(r1);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.comm.request.HsmInputStreamRequest.getResponsesStream(byte[], java.net.HttpURLConnection):java.io.InputStream");
    }

    public InputStream doGetRequest(String strUrl) {
        HttpURLConnection urlConnection = HsmJoinRequest.getHttpURLConnectionGet(strUrl);
        if (urlConnection == null) {
            Log.e(TAG, "doGetRequest urlConnection is null");
            return null;
        }
        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(READ_TIME_OUT);
        urlConnection.setReadTimeout(READ_TIME_OUT);
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
        urlConnection.setReadTimeout(READ_TIME_OUT);
        urlConnection.setConnectTimeout(READ_TIME_OUT);
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

    private static InputStream getInputStream(HttpURLConnection httpConnection) {
        InputStream respnseStream = null;
        try {
            respnseStream = httpConnection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException", e);
        } catch (IOException e2) {
            Log.e(TAG, "IOException", e2);
        }
        return respnseStream;
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

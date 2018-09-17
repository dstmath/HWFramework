package com.android.contacts.update;

import android.content.Context;
import com.android.contacts.update.utils.HsOpenSSLSocketFactory;
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
    private static final String TAG = null;
    private static final String URL_QUERY_CONFIG = "https://configserver.hicloud.com/servicesupport/updateserver/getLatestVersion";
    private String fileId;
    private String ver;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.contacts.update.DownloadRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.contacts.update.DownloadRequest.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.update.DownloadRequest.<clinit>():void");
    }

    private static java.io.InputStream getHttpsResponsesStream(byte[] r5, javax.net.ssl.HttpsURLConnection r6) {
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
        r1 = 0;
        r6.connect();	 Catch:{ IOException -> 0x001b, all -> 0x0032 }
        r1 = r6.getOutputStream();	 Catch:{ IOException -> 0x001b, all -> 0x0032 }
        r1.write(r5);	 Catch:{ IOException -> 0x001b, all -> 0x0032 }
        r2 = TAG;
        r3 = "doPostRequest finally closeOutputStream!";
        com.android.contacts.util.HwLog.d(r2, r3);
        closeOutputStream(r1);
        r2 = getInputStream(r6);
        return r2;
    L_0x001b:
        r0 = move-exception;
        r2 = TAG;	 Catch:{ IOException -> 0x001b, all -> 0x0032 }
        r3 = r0.getMessage();	 Catch:{ IOException -> 0x001b, all -> 0x0032 }
        com.android.contacts.util.HwLog.e(r2, r3);	 Catch:{ IOException -> 0x001b, all -> 0x0032 }
        r2 = 0;
        r3 = TAG;
        r4 = "doPostRequest finally closeOutputStream!";
        com.android.contacts.util.HwLog.d(r3, r4);
        closeOutputStream(r1);
        return r2;
    L_0x0032:
        r2 = move-exception;
        r3 = TAG;
        r4 = "doPostRequest finally closeOutputStream!";
        com.android.contacts.util.HwLog.d(r3, r4);
        closeOutputStream(r1);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.update.DownloadRequest.getHttpsResponsesStream(byte[], javax.net.ssl.HttpsURLConnection):java.io.InputStream");
    }

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
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[]{new HsTrustManager()}, new SecureRandom());
                SSLSocketFactory socketFactory = sc.getSocketFactory();
                if (socketFactory == null) {
                    HwLog.e(TAG, "socketFactory is null error!");
                    return null;
                }
                HttpsURLConnection.setDefaultSSLSocketFactory(new HsOpenSSLSocketFactory(socketFactory));
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
                            return null;
                        }
                        urlConnection.setUseCaches(false);
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        urlConnection.setConnectTimeout(READ_TIME_OUT);
                        urlConnection.setReadTimeout(READ_TIME_OUT);
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
                        return response;
                    } catch (ProtocolException e3) {
                        HwLog.e(TAG, e3.getMessage() + "setRequestMethod failed");
                        return null;
                    }
                } catch (IOException e4) {
                    HwLog.e(TAG, e4.getMessage() + "openConnection failed");
                    return null;
                }
            } catch (NoSuchAlgorithmException e1) {
                HwLog.e(TAG, "NoSuchAlgorithmException " + e1.getMessage());
                return null;
            } catch (KeyManagementException e22) {
                HwLog.e(TAG, "KeyManagementException " + e22.getMessage());
                return null;
            }
        } catch (MalformedURLException e5) {
            HwLog.e(TAG, e5.getMessage());
            return null;
        }
    }

    private static InputStream getInputStream(HttpsURLConnection httpsConnection) {
        InputStream respnseStream = null;
        try {
            respnseStream = httpsConnection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            HwLog.e(TAG, "UnsupportedEncodingException ", e);
        } catch (IOException e2) {
            HwLog.e(TAG, "IOException ", e2);
        }
        return respnseStream;
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

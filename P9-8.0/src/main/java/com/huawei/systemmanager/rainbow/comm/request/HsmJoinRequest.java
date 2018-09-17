package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.systemmanager.rainbow.comm.request.util.HsmOpenSSLSocketFactory;
import com.huawei.systemmanager.rainbow.comm.request.util.HsmTrustManager;
import com.huawei.systemmanager.rainbow.comm.request.util.HttpsWakeLockHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONObject;

public class HsmJoinRequest implements ICommonRequest {
    private static final int CONNECTED_TIME_OUT = 10000;
    private static final String ENCODE_TYPE = "UTF-8";
    private static final String HTTP_GET = "GET";
    private static final String HTTP_IDENTITY_ENCODE = "identity";
    private static final String HTTP_POST = "POST";
    private static final int READ_TIME_OUT = 10000;
    private static final String TAG = "HsmJoinRequest";

    public void setTimeout(int connectTimeout, int readTimeout) {
    }

    static HttpURLConnection getHttpURLConnectionGet(String strUrl) {
        if (TextUtils.isEmpty(strUrl)) {
            return null;
        }
        try {
            URL url = new URL(strUrl);
            if (strUrl.startsWith("https://")) {
                HttpsURLConnection.setDefaultHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            }
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    urlConnection.setRequestMethod(HTTP_GET);
                    return urlConnection;
                } catch (ProtocolException e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }
            } catch (IOException e2) {
                Log.e(TAG, e2.getMessage());
                return null;
            }
        } catch (MalformedURLException e3) {
            Log.e(TAG, e3.getMessage());
            return null;
        }
    }

    public String doGetRequest(String strUrl) {
        HttpURLConnection urlConnection = getHttpURLConnectionGet(strUrl);
        if (urlConnection == null) {
            Log.e(TAG, "doGetRequest urlConnection is null");
            return "";
        }
        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        try {
            urlConnection.connect();
            return convertInputStreamToString(urlConnection);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    static HttpURLConnection getHttpURLConnectionPost(String strUrl) {
        if (TextUtils.isEmpty(strUrl)) {
            return null;
        }
        try {
            URL url = new URL(strUrl);
            if (strUrl.startsWith("https://")) {
                Log.d(TAG, "https request.");
                try {
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, new TrustManager[]{new HsmTrustManager()}, new SecureRandom());
                    javax.net.ssl.SSLSocketFactory socketFactory = sc.getSocketFactory();
                    if (socketFactory == null) {
                        Log.e(TAG, "socketFactory is null error!");
                        return null;
                    }
                    HttpsURLConnection.setDefaultSSLSocketFactory(new HsmOpenSSLSocketFactory(socketFactory));
                    HttpsURLConnection.setDefaultHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                } catch (NoSuchAlgorithmException e1) {
                    Log.e(TAG, "NoSuchAlgorithmException " + e1.getMessage());
                    return null;
                } catch (KeyManagementException e2) {
                    Log.e(TAG, "KeyManagementException " + e2.getMessage());
                    return null;
                }
            }
            Log.d(TAG, "http request.");
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "doPostRequest openConnection already!");
                try {
                    urlConnection.setRequestMethod(HTTP_POST);
                    return urlConnection;
                } catch (ProtocolException e) {
                    Log.e(TAG, e.getMessage() + "setRequestMethod failed");
                    return null;
                }
            } catch (IOException e3) {
                Log.e(TAG, e3.getMessage() + "openConnection failed");
                return null;
            }
        } catch (MalformedURLException e4) {
            Log.e(TAG, e4.getMessage());
            return null;
        }
    }

    public String doPostRequest(String strUrl, Object params, boolean enableGZIP, Context context) {
        HttpURLConnection urlConnection = getHttpURLConnectionPost(strUrl);
        if (urlConnection == null) {
            Log.e(TAG, "doPostRequest urlConnection is null");
            return "";
        }
        byte[] data = null;
        try {
            data = ((JSONObject) params).toString().getBytes(ENCODE_TYPE);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage() + "UnsupportedEncodingException");
        }
        if (data == null) {
            Log.e(TAG, "Get null post data!");
            return "";
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
        Log.d(TAG, "setRequestProperty jsonObject already!");
        HttpsWakeLockHelper wakeLock = new HttpsWakeLockHelper();
        wakeLock.createWakeLockAndAcquire(context);
        Log.d(TAG, "Get wakeLock now");
        String response = "";
        try {
            response = getHttpsResponsesString(data, urlConnection);
        } catch (Exception e2) {
            Log.e(TAG, "getHttpsResponsesString exception!");
        } finally {
            wakeLock.releaseWakeLock();
            Log.d(TAG, "Release wakeLock now");
        }
        return response;
    }

    private String getHttpsResponsesString(byte[] resquestData, HttpURLConnection httpsConnection) {
        String str;
        OutputStream outputStream = null;
        try {
            httpsConnection.connect();
            outputStream = httpsConnection.getOutputStream();
            outputStream.write(resquestData);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            str = "";
            return str;
        } finally {
            Log.d(TAG, "doPostRequest finally closeOutputStream!");
            closeOutputStream(outputStream);
            return str;
        }
        return convertInputStreamToString(httpsConnection);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0037 A:{SYNTHETIC, Splitter: B:17:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003c A:{Catch:{ IOException -> 0x0082 }} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x006e A:{SYNTHETIC, Splitter: B:37:0x006e} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0073 A:{Catch:{ IOException -> 0x0077 }} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0093 A:{SYNTHETIC, Splitter: B:48:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0098 A:{Catch:{ IOException -> 0x009c }} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0093 A:{SYNTHETIC, Splitter: B:48:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0098 A:{Catch:{ IOException -> 0x009c }} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0037 A:{SYNTHETIC, Splitter: B:17:0x0037} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003c A:{Catch:{ IOException -> 0x0082 }} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x006e A:{SYNTHETIC, Splitter: B:37:0x006e} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0073 A:{Catch:{ IOException -> 0x0077 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static String convertInputStreamToString(HttpURLConnection httpsConnection) {
        IOException e;
        UnsupportedEncodingException e2;
        Throwable th;
        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        BufferedReader bufReader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            inputStream = httpsConnection.getInputStream();
            InputStreamReader streamReader2 = new InputStreamReader(inputStream, ENCODE_TYPE);
            try {
                BufferedReader bufReader2 = new BufferedReader(streamReader2);
                try {
                    String str = "";
                    while (true) {
                        str = bufReader2.readLine();
                        if (str == null) {
                            break;
                        }
                        buffer.append(str);
                    }
                    closeInputStream(inputStream);
                    if (bufReader2 != null) {
                        try {
                            bufReader2.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "IOException", e3);
                        }
                    }
                    if (streamReader2 != null) {
                        streamReader2.close();
                    }
                } catch (UnsupportedEncodingException e4) {
                    e2 = e4;
                    bufReader = bufReader2;
                    streamReader = streamReader2;
                    try {
                        Log.e(TAG, "UnsupportedEncodingException", e2);
                        closeInputStream(inputStream);
                        if (bufReader != null) {
                        }
                        if (streamReader != null) {
                        }
                        return buffer.toString();
                    } catch (Throwable th2) {
                        th = th2;
                        closeInputStream(inputStream);
                        if (bufReader != null) {
                        }
                        if (streamReader != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e3 = e5;
                    bufReader = bufReader2;
                    streamReader = streamReader2;
                    Log.e(TAG, "IOException", e3);
                    closeInputStream(inputStream);
                    if (bufReader != null) {
                    }
                    if (streamReader != null) {
                    }
                    return buffer.toString();
                } catch (Throwable th3) {
                    th = th3;
                    bufReader = bufReader2;
                    streamReader = streamReader2;
                    closeInputStream(inputStream);
                    if (bufReader != null) {
                    }
                    if (streamReader != null) {
                    }
                    throw th;
                }
            } catch (UnsupportedEncodingException e6) {
                e2 = e6;
                streamReader = streamReader2;
                Log.e(TAG, "UnsupportedEncodingException", e2);
                closeInputStream(inputStream);
                if (bufReader != null) {
                    try {
                        bufReader.close();
                    } catch (IOException e32) {
                        Log.e(TAG, "IOException", e32);
                    }
                }
                if (streamReader != null) {
                    streamReader.close();
                }
                return buffer.toString();
            } catch (IOException e7) {
                e32 = e7;
                streamReader = streamReader2;
                Log.e(TAG, "IOException", e32);
                closeInputStream(inputStream);
                if (bufReader != null) {
                    try {
                        bufReader.close();
                    } catch (IOException e322) {
                        Log.e(TAG, "IOException", e322);
                    }
                }
                if (streamReader != null) {
                    streamReader.close();
                }
                return buffer.toString();
            } catch (Throwable th4) {
                th = th4;
                streamReader = streamReader2;
                closeInputStream(inputStream);
                if (bufReader != null) {
                    try {
                        bufReader.close();
                    } catch (IOException e3222) {
                        Log.e(TAG, "IOException", e3222);
                        throw th;
                    }
                }
                if (streamReader != null) {
                    streamReader.close();
                }
                throw th;
            }
        } catch (UnsupportedEncodingException e8) {
            e2 = e8;
            Log.e(TAG, "UnsupportedEncodingException", e2);
            closeInputStream(inputStream);
            if (bufReader != null) {
            }
            if (streamReader != null) {
            }
            return buffer.toString();
        } catch (IOException e9) {
            e3222 = e9;
            Log.e(TAG, "IOException", e3222);
            closeInputStream(inputStream);
            if (bufReader != null) {
            }
            if (streamReader != null) {
            }
            return buffer.toString();
        }
        return buffer.toString();
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

    private static void closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}

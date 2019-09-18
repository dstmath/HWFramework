package com.huawei.security.hccm.common.connection;

import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.security.hccm.EnrollmentException;
import com.huawei.security.hccm.common.connection.exception.MalFormedPKIMessageException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Iterator;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.json.JSONObject;

public class HttpConnection {
    private static final String TAG = "HttpConnection";
    private HttpURLConnection mHttpConn = null;
    private String methodType;

    public static class HttpHeaders {
        public static final String AUTHENTICATION = "Authorization";
        public static final String CONNECTION = "Connection";
        public static final String CONNECTION_CLOSE = "close";
        public static final String CONNECTION_KEEP_ALIVE = "Keep-Alive";
        public static final String CONNECT_TIMEOUT = "ConnectTimeout";
        public static final String CONTENT_LANGUAGE = "Content-Language";
        public static final String CONTENT_LANGUAGE_DEFAULT = "en-US";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final int DEFAULT_TIMEOUT = 15000;
        public static final String DO_INPUT = "DoInput";
        public static final String DO_OUTPUT = "DoOutput";
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String READ_TIMEOUT = "ReadTimeout";
        public static final String X_REQUEST_ID = "X-Request-ID";
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004e  */
    public HttpConnection(@NonNull String type) {
        char c;
        int hashCode = type.hashCode();
        if (hashCode == 70454) {
            if (type.equals(HttpHeaders.GET)) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        } else if (hashCode == 2461856 && type.equals(HttpHeaders.POST)) {
            c = 0;
            switch (c) {
                case 0:
                    this.methodType = HttpHeaders.POST;
                    return;
                case 1:
                    this.methodType = HttpHeaders.GET;
                    return;
                default:
                    throw new InvalidParameterException("Supported request type are GET and POST. Invalid requested type is [" + type + "].");
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
    }

    public void initialize(@NonNull URL url, JSONObject config) throws IOException {
        this.mHttpConn = (HttpURLConnection) url.openConnection();
        this.mHttpConn.setRequestMethod(this.methodType);
        if (config != null) {
            setGeneralConfig(config);
        }
    }

    private void setGeneralConfig(@NonNull JSONObject config) throws ProtocolException {
        this.mHttpConn.setRequestProperty(HttpHeaders.CONTENT_TYPE, config.optString(HttpHeaders.CONTENT_TYPE));
        this.mHttpConn.setRequestProperty(HttpHeaders.CONTENT_LANGUAGE, config.optString(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_LANGUAGE_DEFAULT));
        this.mHttpConn.setRequestProperty(HttpHeaders.X_REQUEST_ID, randomID(64));
        this.mHttpConn.setRequestProperty(HttpHeaders.CONNECTION, config.optString(HttpHeaders.CONNECTION, HttpHeaders.CONNECTION_CLOSE));
        this.mHttpConn.setDoInput(config.optBoolean(HttpHeaders.DO_INPUT));
        this.mHttpConn.setDoOutput(config.optBoolean(HttpHeaders.DO_OUTPUT));
        this.mHttpConn.setConnectTimeout(config.optInt(HttpHeaders.CONNECT_TIMEOUT, HttpHeaders.DEFAULT_TIMEOUT));
        this.mHttpConn.setReadTimeout(config.optInt(HttpHeaders.READ_TIMEOUT, HttpHeaders.DEFAULT_TIMEOUT));
    }

    public void setUserConfig(@NonNull JSONObject config) {
        if (config != null) {
            Iterator<String> keys = config.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!config.optString(key).isEmpty()) {
                    this.mHttpConn.setRequestProperty(key, config.optString(key));
                }
            }
        }
    }

    public void setHeaderProperty(String key, String value) {
        this.mHttpConn.setRequestProperty(key, value);
    }

    public byte[] send(byte[] request) throws MalFormedPKIMessageException, EnrollmentException {
        InputStream is = null;
        OutputStream os = null;
        try {
            os = this.mHttpConn.getOutputStream();
            os.write(request);
            os.close();
            int status = this.mHttpConn.getResponseCode();
            Log.d(TAG, "connection status is " + status);
            if (status == 403) {
                throw new MalFormedPKIMessageException("the public key of the TBSCertificate is not the same as that of the attestation certificate!", status);
            } else if (status == 401) {
                throw new MalFormedPKIMessageException("Invalid AT in HTTP header!", status);
            } else if (status == 200) {
                InputStream is2 = this.mHttpConn.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[PKIFailureInfo.certRevoked];
                while (true) {
                    int read = is2.read(buffer);
                    int len = read;
                    if (read == -1) {
                        break;
                    }
                    bos.write(buffer, 0, len);
                }
                is2.close();
                byte[] response = bos.toByteArray();
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Fail to close a stream.");
                    }
                }
                if (is2 != null) {
                    is2.close();
                }
                return response;
            } else {
                throw new MalFormedPKIMessageException("Unknown HTTP status: " + status, status);
            }
        } catch (IOException ioe) {
            Log.e(TAG, "connect failed" + ioe.getMessage());
            throw new EnrollmentException("Connect failed", -11);
        } catch (Throwable th) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e2) {
                    Log.w(TAG, "Fail to close a stream.");
                    throw th;
                }
            }
            if (is != null) {
                is.close();
            }
            throw th;
        }
    }

    private String randomID(int length) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789" + ".-_";
        StringBuilder builder = new StringBuilder();
        while (true) {
            int length2 = length - 1;
            if (length == 0) {
                return builder.toString();
            }
            builder.append(ALPHA_NUMERIC_STRING.charAt((int) (Math.random() * ((double) ALPHA_NUMERIC_STRING.length()))));
            length = length2;
        }
    }
}

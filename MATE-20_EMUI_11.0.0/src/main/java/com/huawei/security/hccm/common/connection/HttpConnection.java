package com.huawei.security.hccm.common.connection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.huawei.security.hccm.EnrollmentException;
import com.huawei.security.hccm.common.connection.exception.MalFormedPKIMessageException;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.util.Iterator;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.json.JSONObject;

public class HttpConnection {
    private static final String TAG = "HttpConnection";
    private static final int X_REQUEST_ID_LENGTH = 64;
    private HttpURLConnection mHttpConnection = null;
    private String mMethodType;

    public static class HttpHeaders {
        private static final String AUTHENTICATION = "Authorization";
        private static final String CONNECTION = "Connection";
        private static final String CONNECTION_CLOSE = "close";
        private static final String CONNECTION_KEEP_ALIVE = "Keep-Alive";
        private static final String CONNECT_TIMEOUT = "ConnectTimeout";
        private static final String CONTENT_LANGUAGE = "Content-Language";
        private static final String CONTENT_LANGUAGE_DEFAULT = "en-US";
        private static final String CONTENT_TYPE = "Content-Type";
        public static final int DEFAULT_TIMEOUT = 15000;
        private static final String DO_INPUT = "DoInput";
        private static final String DO_OUTPUT = "DoOutput";
        public static final String GET = "GET";
        public static final String POST = "POST";
        private static final String READ_TIMEOUT = "ReadTimeout";
        private static final String X_REQUEST_ID = "X-Request-ID";
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004e  */
    public HttpConnection(@NonNull String type) {
        char c;
        int hashCode = type.hashCode();
        if (hashCode != 70454) {
            if (hashCode == 2461856 && type.equals(HttpHeaders.POST)) {
                c = 0;
                if (c == 0) {
                    this.mMethodType = HttpHeaders.POST;
                    return;
                } else if (c == 1) {
                    this.mMethodType = HttpHeaders.GET;
                    return;
                } else {
                    throw new InvalidParameterException("Supported request type are GET and POST. Invalid requested type is [" + type + "].");
                }
            }
        } else if (type.equals(HttpHeaders.GET)) {
            c = 1;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.w(TAG, "Fail to close a stream: " + e.getMessage());
            }
        }
    }

    public void initialize(@NonNull URL url, @Nullable JSONObject config) throws IOException {
        this.mHttpConnection = (HttpURLConnection) url.openConnection();
        this.mHttpConnection.setRequestMethod(this.mMethodType);
        if (config != null) {
            setGeneralConfig(config);
        }
    }

    private void setGeneralConfig(@NonNull JSONObject config) throws ProtocolException {
        this.mHttpConnection.setRequestProperty("Content-Type", config.optString("Content-Type"));
        this.mHttpConnection.setRequestProperty("Content-Language", config.optString("Content-Type", "en-US"));
        this.mHttpConnection.setRequestProperty("X-Request-ID", randomID(64));
        this.mHttpConnection.setRequestProperty("Connection", config.optString("Connection", "close"));
        this.mHttpConnection.setDoInput(config.optBoolean("DoInput"));
        this.mHttpConnection.setDoOutput(config.optBoolean("DoOutput"));
        this.mHttpConnection.setConnectTimeout(config.optInt("ConnectTimeout", HttpHeaders.DEFAULT_TIMEOUT));
        this.mHttpConnection.setReadTimeout(config.optInt("ReadTimeout", HttpHeaders.DEFAULT_TIMEOUT));
    }

    public void setUserConfig(@Nullable JSONObject config) {
        if (config != null) {
            Iterator<String> keys = config.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!config.optString(key, "").isEmpty()) {
                    this.mHttpConnection.setRequestProperty(key, config.optString(key));
                }
            }
        }
    }

    public void setHeaderProperty(String key, String value) {
        this.mHttpConnection.setRequestProperty(key, value);
    }

    public byte[] send(byte[] request) throws MalFormedPKIMessageException, EnrollmentException {
        try {
            OutputStream os = this.mHttpConnection.getOutputStream();
            os.write(request);
            int status = this.mHttpConnection.getResponseCode();
            if (status == 403) {
                throw new MalFormedPKIMessageException("the public key of the TBSCertificate is not the same as that of the attestation certificate!", status);
            } else if (status == 401) {
                throw new MalFormedPKIMessageException("Invalid AT in HTTP header!", status);
            } else if (status == 200) {
                InputStream is = this.mHttpConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[PKIFailureInfo.certRevoked];
                while (true) {
                    int len = is.read(buffer);
                    if (len != -1) {
                        baos.write(buffer, 0, len);
                    } else {
                        byte[] byteArray = baos.toByteArray();
                        closeQuietly(os);
                        closeQuietly(is);
                        closeQuietly(baos);
                        return byteArray;
                    }
                }
            } else {
                throw new MalFormedPKIMessageException("Unknown HTTP status: " + status, status);
            }
        } catch (IOException e) {
            Log.e(TAG, "Connect failed" + e.getMessage());
            throw new EnrollmentException("Connect failed: " + e.getMessage(), -11);
        } catch (Throwable th) {
            closeQuietly(null);
            closeQuietly(null);
            closeQuietly(null);
            throw th;
        }
    }

    private String randomID(int character) {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_";
        StringBuilder builder = new StringBuilder(character);
        SecureRandom randomGenerator = new SecureRandom();
        int rngBound = alphaNumericString.length();
        while (true) {
            int length = character - 1;
            if (character == 0) {
                return builder.toString();
            }
            builder.append(alphaNumericString.charAt(randomGenerator.nextInt(rngBound)));
            character = length;
        }
    }
}

package android.net.http;

import android.net.compatibility.WebAddress;
import android.webkit.CookieManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.SM;
import org.apache.http.protocol.HTTP;

public class RequestHandle {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    public static final int MAX_REDIRECT_COUNT = 16;
    private static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
    private int mBodyLength;
    private InputStream mBodyProvider;
    private Connection mConnection;
    private Map<String, String> mHeaders;
    private String mMethod;
    private int mRedirectCount;
    private Request mRequest;
    private RequestQueue mRequestQueue;
    private WebAddress mUri;
    private String mUrl;

    public RequestHandle(RequestQueue requestQueue, String url, WebAddress uri, String method, Map<String, String> headers, InputStream bodyProvider, int bodyLength, Request request) {
        this.mRedirectCount = 0;
        this.mHeaders = headers == null ? new HashMap() : headers;
        this.mBodyProvider = bodyProvider;
        this.mBodyLength = bodyLength;
        this.mMethod = method == null ? HttpGet.METHOD_NAME : method;
        this.mUrl = url;
        this.mUri = uri;
        this.mRequestQueue = requestQueue;
        this.mRequest = request;
    }

    public RequestHandle(RequestQueue requestQueue, String url, WebAddress uri, String method, Map<String, String> headers, InputStream bodyProvider, int bodyLength, Request request, Connection conn) {
        this(requestQueue, url, uri, method, headers, bodyProvider, bodyLength, request);
        this.mConnection = conn;
    }

    public void cancel() {
        Request request = this.mRequest;
        if (request != null) {
            request.cancel();
        }
    }

    public void pauseRequest(boolean pause) {
        Request request = this.mRequest;
        if (request != null) {
            request.setLoadingPaused(pause);
        }
    }

    public void handleSslErrorResponse(boolean proceed) {
        Request request = this.mRequest;
        if (request != null) {
            request.handleSslErrorResponse(proceed);
        }
    }

    public boolean isRedirectMax() {
        return this.mRedirectCount >= 16;
    }

    public int getRedirectCount() {
        return this.mRedirectCount;
    }

    public void setRedirectCount(int count) {
        this.mRedirectCount = count;
    }

    public boolean setupRedirect(String redirectTo, int statusCode, Map<String, String> cacheHeaders) {
        this.mHeaders.remove("Authorization");
        this.mHeaders.remove("Proxy-Authorization");
        int i = this.mRedirectCount + 1;
        this.mRedirectCount = i;
        if (i == 16) {
            this.mRequest.error(-9, "The page contains too many server redirects.");
            return false;
        }
        if (this.mUrl.startsWith("https:") && redirectTo.startsWith("http:")) {
            this.mHeaders.remove("Referer");
        }
        this.mUrl = redirectTo;
        try {
            this.mUri = new WebAddress(this.mUrl);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        this.mHeaders.remove(SM.COOKIE);
        String cookie = null;
        if (this.mUri != null) {
            cookie = CookieManager.getInstance().getCookie(this.mUri.toString());
        }
        if (cookie != null && cookie.length() > 0) {
            this.mHeaders.put(SM.COOKIE, cookie);
        }
        if ((statusCode == 302 || statusCode == 303) && this.mMethod.equals(HttpPost.METHOD_NAME)) {
            this.mMethod = HttpGet.METHOD_NAME;
        }
        if (statusCode == 307) {
            try {
                if (this.mBodyProvider != null) {
                    this.mBodyProvider.reset();
                }
            } catch (IOException e2) {
                return false;
            }
        } else {
            this.mHeaders.remove(HTTP.CONTENT_TYPE);
            this.mBodyProvider = null;
        }
        this.mHeaders.putAll(cacheHeaders);
        createAndQueueNewRequest();
        return true;
    }

    public void setupBasicAuthResponse(boolean isProxy, String username, String password) {
        String response = computeBasicAuthResponse(username, password);
        Map<String, String> map = this.mHeaders;
        String authorizationHeader = authorizationHeader(isProxy);
        map.put(authorizationHeader, "Basic " + response);
        setupAuthResponse();
    }

    public void setupDigestAuthResponse(boolean isProxy, String username, String password, String realm, String nonce, String QOP, String algorithm, String opaque) {
        String response = computeDigestAuthResponse(username, password, realm, nonce, QOP, algorithm, opaque);
        Map<String, String> map = this.mHeaders;
        String authorizationHeader = authorizationHeader(isProxy);
        map.put(authorizationHeader, "Digest " + response);
        setupAuthResponse();
    }

    private void setupAuthResponse() {
        try {
            if (this.mBodyProvider != null) {
                this.mBodyProvider.reset();
            }
        } catch (IOException e) {
        }
        createAndQueueNewRequest();
    }

    public String getMethod() {
        return this.mMethod;
    }

    public static String computeBasicAuthResponse(String username, String password) {
        if (username == null) {
            throw new NullPointerException("username == null");
        } else if (password != null) {
            return new String(Base64.encodeBase64((username + ':' + password).getBytes()));
        } else {
            throw new NullPointerException("password == null");
        }
    }

    public void waitUntilComplete() {
        this.mRequest.waitUntilComplete();
    }

    public void processRequest() {
        Connection connection = this.mConnection;
        if (connection != null) {
            connection.processRequests(this.mRequest);
        }
    }

    private String computeDigestAuthResponse(String username, String password, String realm, String nonce, String QOP, String algorithm, String opaque) {
        if (username == null) {
            throw new NullPointerException("username == null");
        } else if (password == null) {
            throw new NullPointerException("password == null");
        } else if (realm != null) {
            String cnonce = computeCnonce();
            String digest = computeDigest(username + ":" + realm + ":" + password, this.mMethod + ":" + this.mUrl, nonce, QOP, "00000001", cnonce);
            String response = (((("username=" + doubleQuote(username) + ", ") + "realm=" + doubleQuote(realm) + ", ") + "nonce=" + doubleQuote(nonce) + ", ") + "uri=" + doubleQuote(this.mUrl) + ", ") + "response=" + doubleQuote(digest);
            if (opaque != null) {
                response = response + ", opaque=" + doubleQuote(opaque);
            }
            if (algorithm != null) {
                response = response + ", algorithm=" + algorithm;
            }
            if (QOP == null) {
                return response;
            }
            return response + ", qop=" + QOP + ", nc=00000001, cnonce=" + doubleQuote(cnonce);
        } else {
            throw new NullPointerException("realm == null");
        }
    }

    public static String authorizationHeader(boolean isProxy) {
        if (!isProxy) {
            return "Authorization";
        }
        return "Proxy-Authorization";
    }

    private String computeDigest(String A1, String A2, String nonce, String QOP, String nc, String cnonce) {
        if (QOP == null) {
            String H = H(A1);
            return KD(H, nonce + ":" + H(A2));
        } else if (!QOP.equalsIgnoreCase("auth")) {
            return null;
        } else {
            String H2 = H(A1);
            return KD(H2, nonce + ":" + nc + ":" + cnonce + ":" + QOP + ":" + H(A2));
        }
    }

    private String KD(String secret, String data) {
        return H(secret + ":" + data);
    }

    private String H(String param) {
        if (param == null) {
            return null;
        }
        try {
            byte[] d = MessageDigest.getInstance("MD5").digest(param.getBytes());
            if (d != null) {
                return bufferToHex(d);
            }
            return null;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String bufferToHex(byte[] buffer) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        if (buffer == null) {
            return null;
        }
        int length = buffer.length;
        if (length <= 0) {
            return "";
        }
        StringBuilder hex = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            hex.append(hexChars[(byte) ((buffer[i] & 240) >> 4)]);
            hex.append(hexChars[(byte) (buffer[i] & 15)]);
        }
        return hex.toString();
    }

    private String computeCnonce() {
        int nextInt = new Random().nextInt();
        return Integer.toString(nextInt == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(nextInt), 16);
    }

    private String doubleQuote(String param) {
        if (param == null) {
            return null;
        }
        return "\"" + param + "\"";
    }

    private void createAndQueueNewRequest() {
        if (this.mConnection != null) {
            RequestHandle newHandle = this.mRequestQueue.queueSynchronousRequest(this.mUrl, this.mUri, this.mMethod, this.mHeaders, this.mRequest.mEventHandler, this.mBodyProvider, this.mBodyLength);
            this.mRequest = newHandle.mRequest;
            this.mConnection = newHandle.mConnection;
            newHandle.processRequest();
            return;
        }
        this.mRequest = this.mRequestQueue.queueRequest(this.mUrl, this.mUri, this.mMethod, this.mHeaders, this.mRequest.mEventHandler, this.mBodyProvider, this.mBodyLength).mRequest;
    }
}

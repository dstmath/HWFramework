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
        this.mHeaders = headers == null ? new HashMap<>() : headers;
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
        if (this.mRequest != null) {
            this.mRequest.cancel();
        }
    }

    public void pauseRequest(boolean pause) {
        if (this.mRequest != null) {
            this.mRequest.setLoadingPaused(pause);
        }
    }

    public void handleSslErrorResponse(boolean proceed) {
        if (this.mRequest != null) {
            this.mRequest.handleSslErrorResponse(proceed);
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
        if (this.mConnection != null) {
            this.mConnection.processRequests(this.mRequest);
        }
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    private java.lang.String computeDigestAuthResponse(java.lang.String r19, java.lang.String r20, java.lang.String r21, java.lang.String r22, java.lang.String r23, java.lang.String r24, java.lang.String r25) {
        /*
            r18 = this;
            r7 = r18
            r8 = r19
            r9 = r20
            r10 = r21
            r11 = r23
            r12 = r24
            r13 = r25
            if (r8 == 0) goto L_0x0164
            if (r9 == 0) goto L_0x015a
            if (r10 == 0) goto L_0x0150
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r8)
            java.lang.String r1 = ":"
            r0.append(r1)
            r0.append(r10)
            java.lang.String r1 = ":"
            r0.append(r1)
            r0.append(r9)
            java.lang.String r14 = r0.toString()
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = r7.mMethod
            r0.append(r1)
            java.lang.String r1 = ":"
            r0.append(r1)
            java.lang.String r1 = r7.mUrl
            r0.append(r1)
            java.lang.String r15 = r0.toString()
            java.lang.String r6 = "00000001"
            java.lang.String r5 = r18.computeCnonce()
            r0 = r7
            r1 = r14
            r2 = r15
            r3 = r22
            r4 = r11
            r16 = r5
            r5 = r6
            r9 = r6
            r6 = r16
            java.lang.String r0 = r0.computeDigest(r1, r2, r3, r4, r5, r6)
            java.lang.String r1 = ""
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r3 = "username="
            r2.append(r3)
            java.lang.String r3 = r18.doubleQuote(r19)
            r2.append(r3)
            java.lang.String r3 = ", "
            r2.append(r3)
            java.lang.String r1 = r2.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r3 = "realm="
            r2.append(r3)
            java.lang.String r3 = r7.doubleQuote(r10)
            r2.append(r3)
            java.lang.String r3 = ", "
            r2.append(r3)
            java.lang.String r1 = r2.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r3 = "nonce="
            r2.append(r3)
            r3 = r22
            java.lang.String r4 = r7.doubleQuote(r3)
            r2.append(r4)
            java.lang.String r4 = ", "
            r2.append(r4)
            java.lang.String r1 = r2.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r4 = "uri="
            r2.append(r4)
            java.lang.String r4 = r7.mUrl
            java.lang.String r4 = r7.doubleQuote(r4)
            r2.append(r4)
            java.lang.String r4 = ", "
            r2.append(r4)
            java.lang.String r1 = r2.toString()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r4 = "response="
            r2.append(r4)
            java.lang.String r4 = r7.doubleQuote(r0)
            r2.append(r4)
            java.lang.String r1 = r2.toString()
            if (r13 == 0) goto L_0x010a
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r4 = ", opaque="
            r2.append(r4)
            java.lang.String r4 = r7.doubleQuote(r13)
            r2.append(r4)
            java.lang.String r1 = r2.toString()
        L_0x010a:
            if (r12 == 0) goto L_0x0120
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r4 = ", algorithm="
            r2.append(r4)
            r2.append(r12)
            java.lang.String r1 = r2.toString()
        L_0x0120:
            if (r11 == 0) goto L_0x014d
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            java.lang.String r4 = ", qop="
            r2.append(r4)
            r2.append(r11)
            java.lang.String r4 = ", nc="
            r2.append(r4)
            r2.append(r9)
            java.lang.String r4 = ", cnonce="
            r2.append(r4)
            r4 = r16
            java.lang.String r5 = r7.doubleQuote(r4)
            r2.append(r5)
            java.lang.String r1 = r2.toString()
            goto L_0x014f
        L_0x014d:
            r4 = r16
        L_0x014f:
            return r1
        L_0x0150:
            r3 = r22
            java.lang.NullPointerException r0 = new java.lang.NullPointerException
            java.lang.String r1 = "realm == null"
            r0.<init>(r1)
            throw r0
        L_0x015a:
            r3 = r22
            java.lang.NullPointerException r0 = new java.lang.NullPointerException
            java.lang.String r1 = "password == null"
            r0.<init>(r1)
            throw r0
        L_0x0164:
            r3 = r22
            java.lang.NullPointerException r0 = new java.lang.NullPointerException
            java.lang.String r1 = "username == null"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.http.RequestHandle.computeDigestAuthResponse(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):java.lang.String");
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
        if (param != null) {
            try {
                byte[] d = MessageDigest.getInstance("MD5").digest(param.getBytes());
                if (d != null) {
                    return bufferToHex(d);
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
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
        StringBuilder hex = new StringBuilder(2 * length);
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

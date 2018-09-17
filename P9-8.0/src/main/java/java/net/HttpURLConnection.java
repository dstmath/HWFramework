package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.Date;
import sun.security.util.SecurityConstants;

public abstract class HttpURLConnection extends URLConnection {
    private static final int DEFAULT_CHUNK_SIZE = 4096;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_BAD_METHOD = 405;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_GONE = 410;
    public static final int HTTP_INTERNAL_ERROR = 500;
    public static final int HTTP_LENGTH_REQUIRED = 411;
    public static final int HTTP_MOVED_PERM = 301;
    public static final int HTTP_MOVED_TEMP = 302;
    public static final int HTTP_MULT_CHOICE = 300;
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_OK = 200;
    public static final int HTTP_PARTIAL = 206;
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    public static final int HTTP_PRECON_FAILED = 412;
    public static final int HTTP_PROXY_AUTH = 407;
    public static final int HTTP_REQ_TOO_LONG = 414;
    public static final int HTTP_RESET = 205;
    public static final int HTTP_SEE_OTHER = 303;
    @Deprecated
    public static final int HTTP_SERVER_ERROR = 500;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_UNAVAILABLE = 503;
    public static final int HTTP_UNSUPPORTED_TYPE = 415;
    public static final int HTTP_USE_PROXY = 305;
    public static final int HTTP_VERSION = 505;
    private static boolean followRedirects = true;
    private static final String[] methods = new String[]{"GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"};
    protected int chunkLength = -1;
    protected int fixedContentLength = -1;
    protected long fixedContentLengthLong = -1;
    protected boolean instanceFollowRedirects = followRedirects;
    protected String method = "GET";
    protected int responseCode = -1;
    protected String responseMessage = null;

    public abstract void disconnect();

    public abstract boolean usingProxy();

    public String getHeaderFieldKey(int n) {
        return null;
    }

    public void setFixedLengthStreamingMode(int contentLength) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (this.chunkLength != -1) {
            throw new IllegalStateException("Chunked encoding streaming mode set");
        } else if (contentLength < 0) {
            throw new IllegalArgumentException("invalid content length");
        } else {
            this.fixedContentLength = contentLength;
        }
    }

    public void setFixedLengthStreamingMode(long contentLength) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (this.chunkLength != -1) {
            throw new IllegalStateException("Chunked encoding streaming mode set");
        } else if (contentLength < 0) {
            throw new IllegalArgumentException("invalid content length");
        } else {
            this.fixedContentLengthLong = contentLength;
        }
    }

    public void setChunkedStreamingMode(int chunklen) {
        if (this.connected) {
            throw new IllegalStateException("Can't set streaming mode: already connected");
        } else if (this.fixedContentLength == -1 && this.fixedContentLengthLong == -1) {
            if (chunklen <= 0) {
                chunklen = 4096;
            }
            this.chunkLength = chunklen;
        } else {
            throw new IllegalStateException("Fixed length streaming mode set");
        }
    }

    public String getHeaderField(int n) {
        return null;
    }

    protected HttpURLConnection(URL u) {
        super(u);
    }

    public static void setFollowRedirects(boolean set) {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkSetFactory();
        }
        followRedirects = set;
    }

    public static boolean getFollowRedirects() {
        return followRedirects;
    }

    public void setInstanceFollowRedirects(boolean followRedirects) {
        this.instanceFollowRedirects = followRedirects;
    }

    public boolean getInstanceFollowRedirects() {
        return this.instanceFollowRedirects;
    }

    public void setRequestMethod(String method) throws ProtocolException {
        if (this.connected) {
            throw new ProtocolException("Can't reset method: already connected");
        }
        for (String equals : methods) {
            if (equals.equals(method)) {
                if (method.equals("TRACE")) {
                    SecurityManager s = System.getSecurityManager();
                    if (s != null) {
                        s.checkPermission(new NetPermission("allowHttpTrace"));
                    }
                }
                this.method = method;
                return;
            }
        }
        throw new ProtocolException("Invalid HTTP method: " + method);
    }

    public String getRequestMethod() {
        return this.method;
    }

    public int getResponseCode() throws IOException {
        if (this.responseCode != -1) {
            return this.responseCode;
        }
        Exception exc = null;
        try {
            getInputStream();
        } catch (Exception e) {
            exc = e;
        }
        String statusLine = getHeaderField(0);
        if (statusLine != null) {
            if (statusLine.startsWith("HTTP/1.")) {
                int codePos = statusLine.indexOf(32);
                if (codePos > 0) {
                    int phrasePos = statusLine.indexOf(32, codePos + 1);
                    if (phrasePos > 0 && phrasePos < statusLine.length()) {
                        this.responseMessage = statusLine.substring(phrasePos + 1);
                    }
                    if (phrasePos < 0) {
                        phrasePos = statusLine.length();
                    }
                    try {
                        this.responseCode = Integer.parseInt(statusLine.substring(codePos + 1, phrasePos));
                        return this.responseCode;
                    } catch (NumberFormatException e2) {
                    }
                }
            }
            return -1;
        } else if (exc == null) {
            return -1;
        } else {
            if (exc instanceof RuntimeException) {
                throw ((RuntimeException) exc);
            }
            throw ((IOException) exc);
        }
    }

    public String getResponseMessage() throws IOException {
        getResponseCode();
        return this.responseMessage;
    }

    public long getHeaderFieldDate(String name, long Default) {
        String dateString = getHeaderField(name);
        try {
            if (dateString.indexOf("GMT") == -1) {
                dateString = dateString + " GMT";
            }
            return Date.parse(dateString);
        } catch (Exception e) {
            return Default;
        }
    }

    public Permission getPermission() throws IOException {
        int port = this.url.getPort();
        if (port < 0) {
            port = 80;
        }
        return new SocketPermission(this.url.getHost() + ":" + port, SecurityConstants.SOCKET_CONNECT_ACTION);
    }

    public InputStream getErrorStream() {
        return null;
    }
}

package org.apache.http.params;

import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;

@Deprecated
public final class HttpProtocolParams implements CoreProtocolPNames {
    private HttpProtocolParams() {
    }

    public static String getHttpElementCharset(HttpParams params) {
        if (params != null) {
            String charset = (String) params.getParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET);
            if (charset == null) {
                return "US-ASCII";
            }
            return charset;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setHttpElementCharset(HttpParams params, String charset) {
        if (params != null) {
            params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, charset);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static String getContentCharset(HttpParams params) {
        if (params != null) {
            String charset = (String) params.getParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET);
            if (charset == null) {
                return "ISO-8859-1";
            }
            return charset;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setContentCharset(HttpParams params, String charset) {
        if (params != null) {
            params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, charset);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static ProtocolVersion getVersion(HttpParams params) {
        if (params != null) {
            Object param = params.getParameter(CoreProtocolPNames.PROTOCOL_VERSION);
            if (param == null) {
                return HttpVersion.HTTP_1_1;
            }
            return (ProtocolVersion) param;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setVersion(HttpParams params, ProtocolVersion version) {
        if (params != null) {
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, version);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static String getUserAgent(HttpParams params) {
        if (params != null) {
            return (String) params.getParameter(CoreProtocolPNames.USER_AGENT);
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setUserAgent(HttpParams params, String useragent) {
        if (params != null) {
            params.setParameter(CoreProtocolPNames.USER_AGENT, useragent);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static boolean useExpectContinue(HttpParams params) {
        if (params != null) {
            return params.getBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }

    public static void setUseExpectContinue(HttpParams params, boolean b) {
        if (params != null) {
            params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, b);
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }
}

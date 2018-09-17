package sun.net.www;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class URLConnection extends java.net.URLConnection {
    private static HashMap<String, Void> proxiedHosts = new HashMap();
    private int contentLength = -1;
    private String contentType;
    protected MessageHeader properties = new MessageHeader();

    public URLConnection(URL u) {
        super(u);
    }

    public MessageHeader getProperties() {
        return this.properties;
    }

    public void setProperties(MessageHeader properties) {
        this.properties = properties;
    }

    public void setRequestProperty(String key, String value) {
        if (this.connected) {
            throw new IllegalAccessError("Already connected");
        } else if (key == null) {
            throw new NullPointerException("key cannot be null");
        } else {
            this.properties.set(key, value);
        }
    }

    public void addRequestProperty(String key, String value) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (key == null) {
            throw new NullPointerException("key is null");
        }
    }

    public String getRequestProperty(String key) {
        if (!this.connected) {
            return null;
        }
        throw new IllegalStateException("Already connected");
    }

    public Map<String, List<String>> getRequestProperties() {
        if (!this.connected) {
            return Collections.EMPTY_MAP;
        }
        throw new IllegalStateException("Already connected");
    }

    public String getHeaderField(String name) {
        String str = null;
        try {
            getInputStream();
            if (this.properties != null) {
                str = this.properties.findValue(name);
            }
            return str;
        } catch (Exception e) {
            return null;
        }
    }

    public String getHeaderFieldKey(int n) {
        String str = null;
        try {
            getInputStream();
            MessageHeader props = this.properties;
            if (props != null) {
                str = props.getKey(n);
            }
            return str;
        } catch (Exception e) {
            return null;
        }
    }

    public String getHeaderField(int n) {
        String str = null;
        try {
            getInputStream();
            MessageHeader props = this.properties;
            if (props != null) {
                str = props.getValue(n);
            }
            return str;
        } catch (Exception e) {
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:23:0x0061, code:
            if ((r3 ^ 1) != 0) goto L_0x0063;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getContentType() {
        if (this.contentType == null) {
            this.contentType = getHeaderField("content-type");
        }
        if (this.contentType == null) {
            String ct = null;
            try {
                ct = java.net.URLConnection.guessContentTypeFromStream(getInputStream());
            } catch (IOException e) {
            }
            String ce = this.properties.findValue("content-encoding");
            if (ct == null) {
                ct = this.properties.findValue("content-type");
                if (ct == null) {
                    if (this.url.getFile().endsWith("/")) {
                        ct = "text/html";
                    } else {
                        ct = java.net.URLConnection.guessContentTypeFromName(this.url.getFile());
                    }
                }
            }
            if (ct != null) {
                if (ce != null) {
                    int i;
                    if (ce.equalsIgnoreCase("7bit") || ce.equalsIgnoreCase("8bit")) {
                        i = 1;
                    } else {
                        i = ce.equalsIgnoreCase("binary");
                    }
                }
                setContentType(ct);
            }
            ct = "content/unknown";
            setContentType(ct);
        }
        return this.contentType;
    }

    public void setContentType(String type) {
        this.contentType = type;
        this.properties.set("content-type", type);
    }

    public int getContentLength() {
        try {
            getInputStream();
            int l = this.contentLength;
            if (l < 0) {
                try {
                    l = Integer.parseInt(this.properties.findValue("content-length"));
                    setContentLength(l);
                } catch (Exception e) {
                }
            }
            return l;
        } catch (Exception e2) {
            return -1;
        }
    }

    protected void setContentLength(int length) {
        this.contentLength = length;
        this.properties.set("content-length", String.valueOf(length));
    }

    public boolean canCache() {
        return this.url.getFile().indexOf(63) < 0;
    }

    public void close() {
        this.url = null;
    }

    public static synchronized void setProxiedHost(String host) {
        synchronized (URLConnection.class) {
            proxiedHosts.put(host.toLowerCase(), null);
        }
    }

    public static synchronized boolean isProxiedHost(String host) {
        boolean containsKey;
        synchronized (URLConnection.class) {
            containsKey = proxiedHosts.containsKey(host.toLowerCase());
        }
        return containsKey;
    }
}

package sun.net.www;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class URLConnection extends java.net.URLConnection {
    private static HashMap<String, Void> proxiedHosts;
    private int contentLength;
    private String contentType;
    protected MessageHeader properties;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.URLConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.URLConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.URLConnection.<clinit>():void");
    }

    public URLConnection(URL u) {
        super(u);
        this.contentLength = -1;
        this.properties = new MessageHeader();
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
            if (ct == null || !(ce == null || ce.equalsIgnoreCase("7bit") || ce.equalsIgnoreCase("8bit") || ce.equalsIgnoreCase("binary"))) {
                ct = "content/unknown";
            }
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

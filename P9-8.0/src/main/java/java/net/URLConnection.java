package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.Permission;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import sun.net.www.MessageHeader;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;
import sun.security.x509.InvalidityDateExtension;

public abstract class URLConnection {
    private static final String contentClassPrefix = "sun.net.www.content";
    private static final String contentPathProp = "java.content.handler.pkgs";
    private static boolean defaultAllowUserInteraction = false;
    private static boolean defaultUseCaches = true;
    static ContentHandlerFactory factory;
    private static FileNameMap fileNameMap;
    private static Hashtable<String, ContentHandler> handlers = new Hashtable();
    protected boolean allowUserInteraction = defaultAllowUserInteraction;
    private int connectTimeout;
    protected boolean connected = false;
    protected boolean doInput = true;
    protected boolean doOutput = false;
    protected long ifModifiedSince = 0;
    private int readTimeout;
    private MessageHeader requests;
    protected URL url;
    protected boolean useCaches = defaultUseCaches;

    public abstract void connect() throws IOException;

    public static synchronized FileNameMap getFileNameMap() {
        FileNameMap fileNameMap;
        synchronized (URLConnection.class) {
            if (fileNameMap == null) {
                fileNameMap = new DefaultFileNameMap();
            }
            fileNameMap = fileNameMap;
        }
        return fileNameMap;
    }

    public static void setFileNameMap(FileNameMap map) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSetFactory();
        }
        fileNameMap = map;
    }

    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can not be negative");
        }
        this.connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can not be negative");
        }
        this.readTimeout = timeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    protected URLConnection(URL url) {
        this.url = url;
    }

    public URL getURL() {
        return this.url;
    }

    public int getContentLength() {
        long l = getContentLengthLong();
        if (l > 2147483647L) {
            return -1;
        }
        return (int) l;
    }

    public long getContentLengthLong() {
        return getHeaderFieldLong("content-length", -1);
    }

    public String getContentType() {
        return getHeaderField("content-type");
    }

    public String getContentEncoding() {
        return getHeaderField("content-encoding");
    }

    public long getExpiration() {
        return getHeaderFieldDate("expires", 0);
    }

    public long getDate() {
        return getHeaderFieldDate(InvalidityDateExtension.DATE, 0);
    }

    public long getLastModified() {
        return getHeaderFieldDate("last-modified", 0);
    }

    public String getHeaderField(String name) {
        return null;
    }

    public Map<String, List<String>> getHeaderFields() {
        return Collections.emptyMap();
    }

    public int getHeaderFieldInt(String name, int Default) {
        try {
            return Integer.parseInt(getHeaderField(name));
        } catch (Exception e) {
            return Default;
        }
    }

    public long getHeaderFieldLong(String name, long Default) {
        try {
            return Long.parseLong(getHeaderField(name));
        } catch (Exception e) {
            return Default;
        }
    }

    public long getHeaderFieldDate(String name, long Default) {
        try {
            return Date.parse(getHeaderField(name));
        } catch (Exception e) {
            return Default;
        }
    }

    public String getHeaderFieldKey(int n) {
        return null;
    }

    public String getHeaderField(int n) {
        return null;
    }

    public Object getContent() throws IOException {
        getInputStream();
        return getContentHandler().getContent(this);
    }

    public Object getContent(Class[] classes) throws IOException {
        getInputStream();
        return getContentHandler().getContent(this, classes);
    }

    public Permission getPermission() throws IOException {
        return SecurityConstants.ALL_PERMISSION;
    }

    public InputStream getInputStream() throws IOException {
        throw new UnknownServiceException("protocol doesn't support input");
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException("protocol doesn't support output");
    }

    public String toString() {
        return getClass().getName() + ":" + this.url;
    }

    public void setDoInput(boolean doinput) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        this.doInput = doinput;
    }

    public boolean getDoInput() {
        return this.doInput;
    }

    public void setDoOutput(boolean dooutput) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        this.doOutput = dooutput;
    }

    public boolean getDoOutput() {
        return this.doOutput;
    }

    public void setAllowUserInteraction(boolean allowuserinteraction) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        this.allowUserInteraction = allowuserinteraction;
    }

    public boolean getAllowUserInteraction() {
        return this.allowUserInteraction;
    }

    public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
        defaultAllowUserInteraction = defaultallowuserinteraction;
    }

    public static boolean getDefaultAllowUserInteraction() {
        return defaultAllowUserInteraction;
    }

    public void setUseCaches(boolean usecaches) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        this.useCaches = usecaches;
    }

    public boolean getUseCaches() {
        return this.useCaches;
    }

    public void setIfModifiedSince(long ifmodifiedsince) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        }
        this.ifModifiedSince = ifmodifiedsince;
    }

    public long getIfModifiedSince() {
        return this.ifModifiedSince;
    }

    public boolean getDefaultUseCaches() {
        return defaultUseCaches;
    }

    public void setDefaultUseCaches(boolean defaultusecaches) {
        defaultUseCaches = defaultusecaches;
    }

    public void setRequestProperty(String key, String value) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (key == null) {
            throw new NullPointerException("key is null");
        } else {
            if (this.requests == null) {
                this.requests = new MessageHeader();
            }
            this.requests.set(key, value);
        }
    }

    public void addRequestProperty(String key, String value) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (key == null) {
            throw new NullPointerException("key is null");
        } else {
            if (this.requests == null) {
                this.requests = new MessageHeader();
            }
            this.requests.add(key, value);
        }
    }

    public String getRequestProperty(String key) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (this.requests == null) {
            return null;
        } else {
            return this.requests.findValue(key);
        }
    }

    public Map<String, List<String>> getRequestProperties() {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (this.requests == null) {
            return Collections.emptyMap();
        } else {
            return this.requests.getHeaders(null);
        }
    }

    @Deprecated
    public static void setDefaultRequestProperty(String key, String value) {
    }

    @Deprecated
    public static String getDefaultRequestProperty(String key) {
        return null;
    }

    public static synchronized void setContentHandlerFactory(ContentHandlerFactory fac) {
        synchronized (URLConnection.class) {
            if (factory != null) {
                throw new Error("factory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            factory = fac;
        }
    }

    /* JADX WARNING: Missing block: B:27:0x004b, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized ContentHandler getContentHandler() throws IOException {
        String contentType = stripOffParameters(getContentType());
        ContentHandler handler = null;
        if (contentType == null) {
            contentType = guessContentTypeFromName(this.url.getFile());
            if (contentType == null) {
                contentType = guessContentTypeFromStream(getInputStream());
            }
        }
        if (contentType == null) {
            return UnknownContentHandler.INSTANCE;
        }
        try {
            handler = (ContentHandler) handlers.get(contentType);
            if (handler != null) {
                return handler;
            }
        } catch (Exception e) {
        }
        if (factory != null) {
            handler = factory.createContentHandler(contentType);
        }
        if (handler == null) {
            try {
                handler = lookupContentHandlerClassFor(contentType);
            } catch (Exception e2) {
                e2.printStackTrace();
                handler = UnknownContentHandler.INSTANCE;
            }
            handlers.put(contentType, handler);
        }
    }

    private String stripOffParameters(String contentType) {
        if (contentType == null) {
            return null;
        }
        int index = contentType.indexOf(59);
        if (index > 0) {
            return contentType.substring(0, index);
        }
        return contentType;
    }

    private ContentHandler lookupContentHandlerClassFor(String contentType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String contentHandlerClassName = typeToPackageName(contentType);
        StringTokenizer packagePrefixIter = new StringTokenizer(getContentHandlerPkgPrefixes(), "|");
        while (packagePrefixIter.hasMoreTokens()) {
            try {
                String clsName = packagePrefixIter.nextToken().trim() + "." + contentHandlerClassName;
                Class cls = null;
                try {
                    cls = Class.forName(clsName);
                } catch (ClassNotFoundException e) {
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    if (cl != null) {
                        cls = cl.loadClass(clsName);
                    }
                }
                if (cls != null) {
                    return (ContentHandler) cls.newInstance();
                }
                continue;
            } catch (Exception e2) {
            }
        }
        return UnknownContentHandler.INSTANCE;
    }

    private String typeToPackageName(String contentType) {
        contentType = contentType.toLowerCase();
        int len = contentType.length();
        char[] nm = new char[len];
        contentType.getChars(0, len, nm, 0);
        for (int i = 0; i < len; i++) {
            char c = nm[i];
            if (c == '/') {
                nm[i] = '.';
            } else if (('A' > c || c > 'Z') && (('a' > c || c > 'z') && ('0' > c || c > '9'))) {
                nm[i] = '_';
            }
        }
        return new String(nm);
    }

    private String getContentHandlerPkgPrefixes() {
        String packagePrefixList = (String) AccessController.doPrivileged(new GetPropertyAction(contentPathProp, ""));
        if (packagePrefixList != "") {
            packagePrefixList = packagePrefixList + "|";
        }
        return packagePrefixList + contentClassPrefix;
    }

    public static String guessContentTypeFromName(String fname) {
        return getFileNameMap().getContentTypeFor(fname);
    }

    public static String guessContentTypeFromStream(InputStream is) throws IOException {
        if (!is.markSupported()) {
            return null;
        }
        is.mark(16);
        int c1 = is.read();
        int c2 = is.read();
        int c3 = is.read();
        int c4 = is.read();
        int c5 = is.read();
        int c6 = is.read();
        int c7 = is.read();
        int c8 = is.read();
        int c9 = is.read();
        int c10 = is.read();
        int c11 = is.read();
        int c12 = is.read();
        int c13 = is.read();
        int c14 = is.read();
        int c15 = is.read();
        int c16 = is.read();
        is.reset();
        if (c1 == 202 && c2 == 254 && c3 == 186 && c4 == 190) {
            return "application/java-vm";
        }
        if (c1 == 172 && c2 == 237) {
            return "application/x-java-serialized-object";
        }
        if (c1 == 60) {
            if (c2 == 33 || ((c2 == 104 && ((c3 == 116 && c4 == 109 && c5 == 108) || (c3 == 101 && c4 == 97 && c5 == 100))) || ((c2 == 98 && c3 == 111 && c4 == 100 && c5 == 121) || ((c2 == 72 && ((c3 == 84 && c4 == 77 && c5 == 76) || (c3 == 69 && c4 == 65 && c5 == 68))) || (c2 == 66 && c3 == 79 && c4 == 68 && c5 == 89))))) {
                return "text/html";
            }
            if (c2 == 63 && c3 == 120 && c4 == 109 && c5 == 108 && c6 == 32) {
                return "application/xml";
            }
        }
        if (c1 == 239 && c2 == 187 && c3 == 191 && c4 == 60 && c5 == 63 && c6 == 120) {
            return "application/xml";
        }
        if (c1 == 254 && c2 == 255 && c3 == 0 && c4 == 60 && c5 == 0 && c6 == 63 && c7 == 0 && c8 == 120) {
            return "application/xml";
        }
        if (c1 == 255 && c2 == 254 && c3 == 60 && c4 == 0 && c5 == 63 && c6 == 0 && c7 == 120 && c8 == 0) {
            return "application/xml";
        }
        if (c1 == 0 && c2 == 0 && c3 == 254 && c4 == 255 && c5 == 0 && c6 == 0 && c7 == 0 && c8 == 60 && c9 == 0 && c10 == 0 && c11 == 0 && c12 == 63 && c13 == 0 && c14 == 0 && c15 == 0 && c16 == 120) {
            return "application/xml";
        }
        if (c1 == 255 && c2 == 254 && c3 == 0 && c4 == 0 && c5 == 60 && c6 == 0 && c7 == 0 && c8 == 0 && c9 == 63 && c10 == 0 && c11 == 0 && c12 == 0 && c13 == 120 && c14 == 0 && c15 == 0 && c16 == 0) {
            return "application/xml";
        }
        if (c1 == 71 && c2 == 73 && c3 == 70 && c4 == 56) {
            return "image/gif";
        }
        if (c1 == 35 && c2 == 100 && c3 == 101 && c4 == 102) {
            return "image/x-bitmap";
        }
        if (c1 == 33 && c2 == 32 && c3 == 88 && c4 == 80 && c5 == 77 && c6 == 50) {
            return "image/x-pixmap";
        }
        if (c1 == 137 && c2 == 80 && c3 == 78 && c4 == 71 && c5 == 13 && c6 == 10 && c7 == 26 && c8 == 10) {
            return "image/png";
        }
        if (c1 == 255 && c2 == 216 && c3 == 255) {
            if (c4 == 224 || c4 == 238) {
                return "image/jpeg";
            }
            if (c4 == 225 && c7 == 69 && c8 == 120 && c9 == 105 && c10 == 102 && c11 == 0) {
                return "image/jpeg";
            }
        }
        if (c1 == 208 && c2 == 207 && c3 == 17 && c4 == 224 && c5 == 161 && c6 == 177 && c7 == 26 && c8 == 225 && checkfpx(is)) {
            return "image/vnd.fpx";
        }
        if (c1 == 46 && c2 == 115 && c3 == 110 && c4 == 100) {
            return "audio/basic";
        }
        if (c1 == 100 && c2 == 110 && c3 == 115 && c4 == 46) {
            return "audio/basic";
        }
        if (c1 == 82 && c2 == 73 && c3 == 70 && c4 == 70) {
            return "audio/x-wav";
        }
        return null;
    }

    private static boolean checkfpx(InputStream is) throws IOException {
        is.mark(256);
        long posn = skipForward(is, 28);
        if (posn < 28) {
            is.reset();
            return false;
        }
        int[] c = new int[16];
        if (readBytes(c, 2, is) < 0) {
            is.reset();
            return false;
        }
        int byteOrder = c[0];
        posn += 2;
        if (readBytes(c, 2, is) < 0) {
            is.reset();
            return false;
        }
        int uSectorShift;
        if (byteOrder == 254) {
            uSectorShift = c[0] + (c[1] << 8);
        } else {
            uSectorShift = (c[0] << 8) + c[1];
        }
        posn += 2;
        long toSkip = 48 - posn;
        long skipped = skipForward(is, toSkip);
        if (skipped < toSkip) {
            is.reset();
            return false;
        }
        posn += skipped;
        if (readBytes(c, 4, is) < 0) {
            is.reset();
            return false;
        }
        int sectDirStart;
        if (byteOrder == 254) {
            sectDirStart = ((c[0] + (c[1] << 8)) + (c[2] << 16)) + (c[3] << 24);
        } else {
            sectDirStart = (((c[0] << 24) + (c[1] << 16)) + (c[2] << 8)) + c[3];
        }
        posn += 4;
        is.reset();
        toSkip = ((((long) (1 << uSectorShift)) * ((long) sectDirStart)) + 512) + 80;
        if (toSkip < 0) {
            return false;
        }
        is.mark(((int) toSkip) + 48);
        if (skipForward(is, toSkip) < toSkip) {
            is.reset();
            return false;
        } else if (readBytes(c, 16, is) < 0) {
            is.reset();
            return false;
        } else if (byteOrder == 254 && c[0] == 0 && c[2] == 97 && c[3] == 86 && c[4] == 84 && c[5] == 193 && c[6] == HttpURLConnection.HTTP_PARTIAL && c[7] == 17 && c[8] == 133 && c[9] == 83 && c[10] == 0 && c[11] == 170 && c[12] == 0 && c[13] == 161 && c[14] == 249 && c[15] == 91) {
            is.reset();
            return true;
        } else if (c[3] == 0 && c[1] == 97 && c[0] == 86 && c[5] == 84 && c[4] == 193 && c[7] == HttpURLConnection.HTTP_PARTIAL && c[6] == 17 && c[8] == 133 && c[9] == 83 && c[10] == 0 && c[11] == 170 && c[12] == 0 && c[13] == 161 && c[14] == 249 && c[15] == 91) {
            is.reset();
            return true;
        } else {
            is.reset();
            return false;
        }
    }

    private static int readBytes(int[] c, int len, InputStream is) throws IOException {
        byte[] buf = new byte[len];
        if (is.read(buf, 0, len) < len) {
            return -1;
        }
        for (int i = 0; i < len; i++) {
            c[i] = buf[i] & 255;
        }
        return 0;
    }

    private static long skipForward(InputStream is, long toSkip) throws IOException {
        long skipped = 0;
        while (skipped != toSkip) {
            long eachSkip = is.skip(toSkip - skipped);
            if (eachSkip <= 0) {
                if (is.read() == -1) {
                    return skipped;
                }
                skipped++;
            }
            skipped += eachSkip;
        }
        return skipped;
    }
}

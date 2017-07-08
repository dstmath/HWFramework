package android.webkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Deprecated
public final class CacheManager {
    static final /* synthetic */ boolean -assertionsDisabled = false;

    @Deprecated
    public static class CacheResult {
        long contentLength;
        String contentdisposition;
        String crossDomain;
        String encoding;
        String etag;
        long expires;
        String expiresString;
        int httpStatusCode;
        InputStream inStream;
        String lastModified;
        String localPath;
        String location;
        String mimeType;
        File outFile;
        OutputStream outStream;

        public int getHttpStatusCode() {
            return this.httpStatusCode;
        }

        public long getContentLength() {
            return this.contentLength;
        }

        public String getLocalPath() {
            return this.localPath;
        }

        public long getExpires() {
            return this.expires;
        }

        public String getExpiresString() {
            return this.expiresString;
        }

        public String getLastModified() {
            return this.lastModified;
        }

        public String getETag() {
            return this.etag;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public String getLocation() {
            return this.location;
        }

        public String getEncoding() {
            return this.encoding;
        }

        public String getContentDisposition() {
            return this.contentdisposition;
        }

        public InputStream getInputStream() {
            return this.inStream;
        }

        public OutputStream getOutputStream() {
            return this.outStream;
        }

        public void setInputStream(InputStream stream) {
            this.inStream = stream;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.webkit.CacheManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.webkit.CacheManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.CacheManager.<clinit>():void");
    }

    @Deprecated
    public static File getCacheFileBaseDir() {
        return null;
    }

    @Deprecated
    public static boolean cacheDisabled() {
        return false;
    }

    @Deprecated
    public static boolean startCacheTransaction() {
        return false;
    }

    @Deprecated
    public static boolean endCacheTransaction() {
        return false;
    }

    @Deprecated
    public static CacheResult getCacheFile(String url, Map<String, String> map) {
        return null;
    }

    @Deprecated
    public static void saveCacheFile(String url, CacheResult cacheResult) {
        saveCacheFile(url, 0, cacheResult);
    }

    static void saveCacheFile(String url, long postIdentifier, CacheResult cacheRet) {
        try {
            cacheRet.outStream.close();
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        } catch (IOException e) {
        }
    }
}

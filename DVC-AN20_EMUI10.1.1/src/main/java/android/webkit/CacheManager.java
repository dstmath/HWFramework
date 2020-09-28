package android.webkit;

import android.annotation.UnsupportedAppUsage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Deprecated
public final class CacheManager {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    @Deprecated
    public static class CacheResult {
        @UnsupportedAppUsage
        long contentLength;
        @UnsupportedAppUsage
        String contentdisposition;
        @UnsupportedAppUsage
        String crossDomain;
        @UnsupportedAppUsage
        String encoding;
        @UnsupportedAppUsage
        String etag;
        @UnsupportedAppUsage
        long expires;
        @UnsupportedAppUsage
        String expiresString;
        @UnsupportedAppUsage
        int httpStatusCode;
        @UnsupportedAppUsage
        InputStream inStream;
        @UnsupportedAppUsage
        String lastModified;
        @UnsupportedAppUsage
        String localPath;
        @UnsupportedAppUsage
        String location;
        @UnsupportedAppUsage
        String mimeType;
        @UnsupportedAppUsage
        File outFile;
        @UnsupportedAppUsage
        OutputStream outStream;

        @UnsupportedAppUsage
        public int getHttpStatusCode() {
            return this.httpStatusCode;
        }

        @UnsupportedAppUsage
        public long getContentLength() {
            return this.contentLength;
        }

        @UnsupportedAppUsage
        public String getLocalPath() {
            return this.localPath;
        }

        @UnsupportedAppUsage
        public long getExpires() {
            return this.expires;
        }

        @UnsupportedAppUsage
        public String getExpiresString() {
            return this.expiresString;
        }

        @UnsupportedAppUsage
        public String getLastModified() {
            return this.lastModified;
        }

        @UnsupportedAppUsage
        public String getETag() {
            return this.etag;
        }

        @UnsupportedAppUsage
        public String getMimeType() {
            return this.mimeType;
        }

        @UnsupportedAppUsage
        public String getLocation() {
            return this.location;
        }

        @UnsupportedAppUsage
        public String getEncoding() {
            return this.encoding;
        }

        @UnsupportedAppUsage
        public String getContentDisposition() {
            return this.contentdisposition;
        }

        @UnsupportedAppUsage
        public InputStream getInputStream() {
            return this.inStream;
        }

        @UnsupportedAppUsage
        public OutputStream getOutputStream() {
            return this.outStream;
        }

        @UnsupportedAppUsage
        public void setInputStream(InputStream stream) {
            this.inStream = stream;
        }

        @UnsupportedAppUsage
        public void setEncoding(String encoding2) {
            this.encoding = encoding2;
        }

        public void setContentLength(long contentLength2) {
            this.contentLength = contentLength2;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public static File getCacheFileBaseDir() {
        return null;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean cacheDisabled() {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean startCacheTransaction() {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean endCacheTransaction() {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static CacheResult getCacheFile(String url, Map<String, String> map) {
        return null;
    }

    @UnsupportedAppUsage
    @Deprecated
    public static void saveCacheFile(String url, CacheResult cacheResult) {
        saveCacheFile(url, 0, cacheResult);
    }

    @UnsupportedAppUsage
    static void saveCacheFile(String url, long postIdentifier, CacheResult cacheRet) {
        try {
            cacheRet.outStream.close();
        } catch (IOException e) {
        }
    }
}

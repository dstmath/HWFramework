package ohos.data.usage;

import java.io.File;
import java.io.IOException;

public class CachePolicy {
    private static final String CACHE_CLEAN = "user.cache_tombstone";
    private static final String CACHE_DELETE = "user.cache_group";
    private static final String LIB_NAME = "datausage.z";

    private native boolean getXattr(String str, String str2) throws IOException;

    private native void removeXattr(String str, String str2) throws IOException;

    private native void setXattr(String str, String str2) throws IOException;

    static {
        System.loadLibrary(LIB_NAME);
    }

    public void setCacheDeleteXattr(File file) throws IOException {
        if (file == null || !file.isDirectory()) {
            throw new IOException("set cacheDeleteXattr failed, object should be directories");
        }
        setXattr(file.getCanonicalPath(), CACHE_DELETE);
    }

    public void removeCacheDeleteXattr(File file) throws IOException {
        if (file == null || !file.isDirectory()) {
            throw new IOException("remove cacheDeleteXattr failed, object should be directories");
        }
        removeXattr(file.getCanonicalPath(), CACHE_DELETE);
    }

    public boolean isCacheDeleteXattr(File file) throws IOException {
        if (file != null && file.isDirectory()) {
            return getXattr(file.getCanonicalPath(), CACHE_DELETE);
        }
        throw new IOException("get cacheDeleteXattr failed, object should be directories");
    }

    public void setCacheCleanXattr(File file) throws IOException {
        if (file == null || !file.isDirectory()) {
            throw new IOException("set cacheCleanXattr failed, object should be directories");
        }
        setXattr(file.getCanonicalPath(), CACHE_CLEAN);
    }

    public void removeCacheCleanXattr(File file) throws IOException {
        if (file == null || !file.isDirectory()) {
            throw new IOException("remove cacheCleanXattr failed, object should be directories");
        }
        removeXattr(file.getCanonicalPath(), CACHE_CLEAN);
    }

    public boolean isCacheCleanXattr(File file) throws IOException {
        if (file != null && file.isDirectory()) {
            return getXattr(file.getCanonicalPath(), CACHE_CLEAN);
        }
        throw new IOException("get cacheCleanXattr failed, object should be directories");
    }
}

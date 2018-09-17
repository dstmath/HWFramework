package huawei.android.hwgallerycache;

import android.content.Context;
import huawei.android.hwgallerycache.BlobCache.LookupRequest;
import huawei.android.hwgallerycache.HwGalleryCacheManagerImpl.BytesBuffer;
import java.io.IOException;

public class ImageCacheService {
    private static final String IMAGE_CACHE_FILE = "imgcache";
    private static final int IMAGE_CACHE_MAX_BYTES = 209715200;
    private static final int IMAGE_CACHE_MAX_ENTRIES = 5000;
    private static final String TAG = "ImageCacheService";
    private BlobCache mCache;

    public ImageCacheService(Context context, String cacheVersion) {
        this.mCache = CacheManager.getCache(context, IMAGE_CACHE_FILE, IMAGE_CACHE_MAX_ENTRIES, IMAGE_CACHE_MAX_BYTES, cacheVersion);
    }

    public boolean getImageData(String path, long timeModified, int type, BytesBuffer buffer) {
        if (this.mCache == null) {
            return false;
        }
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        try {
            LookupRequest request = new LookupRequest();
            request.key = cacheKey;
            request.buffer = buffer.data;
            synchronized (this.mCache) {
                if (this.mCache.lookup(request)) {
                    if (isSameKey(key, request.buffer)) {
                        buffer.data = request.buffer;
                        buffer.offset = key.length;
                        buffer.length = request.length - buffer.offset;
                        return true;
                    }
                    return false;
                }
                return false;
            }
        } catch (IOException e) {
        }
    }

    public byte[] getBytes(String in) {
        byte[] result = new byte[(in.length() * 2)];
        int output = 0;
        for (char ch : in.toCharArray()) {
            int i = output + 1;
            result[output] = (byte) (ch & PduHeaders.STORE_STATUS_ERROR_END);
            output = i + 1;
            result[i] = (byte) (ch >> 8);
        }
        return result;
    }

    private byte[] makeKey(String path, long timeModified, int type) {
        return getBytes(path + "+" + timeModified + "+" + type);
    }

    private boolean isSameKey(byte[] key, byte[] buffer) {
        int n = key.length;
        if (buffer.length < n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (key[i] != buffer[i]) {
                return false;
            }
        }
        return true;
    }
}

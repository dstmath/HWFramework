package huawei.android.hwgallerycache;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class CacheManager {
    private static final String KEY_CACHE_UP_TO_DATE = "cache-up-to-date";
    private static final String TAG = "CacheManager";
    private static HashMap<String, BlobCache> sCacheMap;
    private static String[] sVolumePaths;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwgallerycache.CacheManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwgallerycache.CacheManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwgallerycache.CacheManager.<clinit>():void");
    }

    public static BlobCache getCache(Context context, String filename, int maxEntries, int maxBytes, String version) {
        BlobCache cache;
        IOException e;
        Exception e2;
        synchronized (sCacheMap) {
            BlobCache cache2 = (BlobCache) sCacheMap.get(filename);
            if (cache2 == null) {
                sVolumePaths = ((StorageManager) context.getSystemService("storage")).getVolumePaths();
                String path = new File(sVolumePaths[0], "/Android/data/com.android.gallery3d/cache").getAbsolutePath() + "/" + filename;
                Log.d(TAG, "Using cache file: " + path);
                try {
                    cache = new BlobCache(path, maxEntries, maxBytes, false, version);
                    try {
                        sCacheMap.put(filename, cache);
                    } catch (IOException e3) {
                        e = e3;
                        Log.e(TAG, "Cannot instantiate cache!", e);
                        return cache;
                    } catch (Exception e4) {
                        e2 = e4;
                        Log.e(TAG, "Cannot instantiate cache!", e2);
                        return cache;
                    }
                } catch (IOException e5) {
                    e = e5;
                    cache = cache2;
                    Log.e(TAG, "Cannot instantiate cache!", e);
                    return cache;
                } catch (Exception e6) {
                    e2 = e6;
                    cache = cache2;
                    Log.e(TAG, "Cannot instantiate cache!", e2);
                    return cache;
                }
            }
            cache = cache2;
        }
        return cache;
    }
}

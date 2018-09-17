package android_maps_conflict_avoidance.com.google.android.gsf;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.BaseColumns;
import java.util.HashMap;

public class GoogleSettingsContract$NameValueTable implements BaseColumns {
    static HashMap<Uri, GoogleSettingsContract$UriCacheValue> sCache = new HashMap();

    private static GoogleSettingsContract$UriCacheValue ensureCacheInitializedLocked(ContentResolver cr, Uri uri) {
        GoogleSettingsContract$UriCacheValue cacheValue = (GoogleSettingsContract$UriCacheValue) sCache.get(uri);
        if (cacheValue == null) {
            cacheValue = new GoogleSettingsContract$UriCacheValue();
            sCache.put(uri, cacheValue);
            GoogleSettingsContract$UriCacheValue finalCacheValue = cacheValue;
            cr.registerContentObserver(uri, true, new ContentObserver(null) {
                public void onChange(boolean selfChange) {
                    cacheValue.invalidateCache.set(true);
                }
            });
            return cacheValue;
        } else if (!cacheValue.invalidateCache.getAndSet(false)) {
            return cacheValue;
        } else {
            synchronized (cacheValue) {
                cacheValue.valueCache.clear();
                cacheValue.versionToken = new Object();
            }
            return cacheValue;
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0022, code:
            r9 = null;
            r6 = null;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r0 = r12;
            r1 = r13;
            r6 = r0.query(r1, new java.lang.String[]{"value"}, "name=?", new java.lang.String[]{r14}, null);
     */
    /* JADX WARNING: Missing block: B:19:0x003d, code:
            if (r6 == null) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:21:0x0045, code:
            if ((r6.moveToFirst() ^ 1) == 0) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:22:0x0047, code:
            putCache(r7, r10, r14, null);
     */
    /* JADX WARNING: Missing block: B:23:0x004b, code:
            if (r6 == null) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:24:0x004d, code:
            r6.close();
     */
    /* JADX WARNING: Missing block: B:25:0x0050, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            r9 = r6.getString(0);
            putCache(r7, r10, r14, r9);
     */
    /* JADX WARNING: Missing block: B:32:0x005c, code:
            if (r6 == null) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:33:0x005e, code:
            r6.close();
     */
    /* JADX WARNING: Missing block: B:34:0x0061, code:
            return r9;
     */
    /* JADX WARNING: Missing block: B:35:0x0062, code:
            r8 = move-exception;
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            android.util.Log.e("GoogleSettings", "Can't get key " + r14 + " from " + r13, r8);
     */
    /* JADX WARNING: Missing block: B:38:0x0088, code:
            if (r6 != null) goto L_0x008a;
     */
    /* JADX WARNING: Missing block: B:39:0x008a, code:
            r6.close();
     */
    /* JADX WARNING: Missing block: B:41:0x008f, code:
            if (r6 != null) goto L_0x0091;
     */
    /* JADX WARNING: Missing block: B:42:0x0091, code:
            r6.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static String getString(ContentResolver resolver, Uri uri, String name) {
        GoogleSettingsContract$UriCacheValue cacheValue;
        synchronized (GoogleSettingsContract$NameValueTable.class) {
            cacheValue = ensureCacheInitializedLocked(resolver, uri);
        }
        synchronized (cacheValue) {
            Object version = cacheValue.versionToken;
            if (cacheValue.valueCache.containsKey(name)) {
                String str = (String) cacheValue.valueCache.get(name);
                return str;
            }
        }
    }

    private static void putCache(GoogleSettingsContract$UriCacheValue cacheValue, Object version, String key, String value) {
        synchronized (cacheValue) {
            if (version == cacheValue.versionToken) {
                cacheValue.valueCache.put(key, value);
            }
        }
    }
}

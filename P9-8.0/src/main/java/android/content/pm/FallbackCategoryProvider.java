package android.content.pm;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FallbackCategoryProvider {
    private static final String TAG = "FallbackCategoryProvider";
    private static final ArrayMap<String, Integer> sFallbacks = new ArrayMap();

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b3 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:37:0x00b2} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0073 A:{SYNTHETIC, Splitter: B:23:0x0073} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00c5 A:{Catch:{ IOException -> 0x0079, IOException -> 0x0079 }} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0078 A:{SYNTHETIC, Splitter: B:26:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0079 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:26:0x0078} */
    /* JADX WARNING: Missing block: B:28:0x0079, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:29:0x007a, code:
            android.util.Log.w(TAG, "Failed to read fallback categories", r1);
     */
    /* JADX WARNING: Missing block: B:39:0x00b3, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:40:0x00b4, code:
            r3 = r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loadFallbacks() {
        Throwable th;
        Throwable th2 = null;
        sFallbacks.clear();
        if (SystemProperties.getBoolean("fw.ignore_fb_categories", false)) {
            Log.d(TAG, "Ignoring fallback categories");
            return;
        }
        AssetManager assets = new AssetManager();
        assets.addAssetPath("/system/framework/framework-res.apk");
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new Resources(assets, null, null).openRawResource(17825796)));
            while (true) {
                try {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    } else if (line.charAt(0) != '#') {
                        String[] split = line.split(",");
                        if (split.length == 2) {
                            sFallbacks.put(split[0], Integer.valueOf(Integer.parseInt(split[1])));
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Throwable th4) {
                            if (th2 == null) {
                                th2 = th4;
                            } else if (th2 != th4) {
                                th2.addSuppressed(th4);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (IOException e) {
                        }
                    } else {
                        throw th;
                    }
                }
            }
            Log.d(TAG, "Found " + sFallbacks.size() + " fallback categories");
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (Throwable th5) {
                    th2 = th5;
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e2) {
                }
            }
        } catch (Throwable th6) {
            th = th6;
            if (reader != null) {
            }
            if (th2 == null) {
            }
        }
    }

    public static int getFallbackCategory(String packageName) {
        return ((Integer) sFallbacks.getOrDefault(packageName, Integer.valueOf(-1))).intValue();
    }
}

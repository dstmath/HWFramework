package android.content.pm;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FallbackCategoryProvider {
    private static final String TAG = "FallbackCategoryProvider";
    private static final ArrayMap<String, Integer> sFallbacks = new ArrayMap<>();

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x008b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0090, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0091, code lost:
        r0.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0094, code lost:
        throw r5;
     */
    public static void loadFallbacks() {
        sFallbacks.clear();
        if (SystemProperties.getBoolean("fw.ignore_fb_categories", false)) {
            Log.d(TAG, "Ignoring fallback categories");
            return;
        }
        AssetManager assets = new AssetManager();
        assets.addAssetPath("/system/framework/framework-res.apk");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new Resources(assets, null, null).openRawResource(R.raw.fallback_categories)));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                Log.d(TAG, "Found " + sFallbacks.size() + " fallback categories");
                try {
                    reader.close();
                    return;
                } catch (IOException | NumberFormatException e) {
                    Log.w(TAG, "Failed to read fallback categories", e);
                    return;
                }
            } else if (line.charAt(0) != '#') {
                String[] split = line.split(SmsManager.REGEX_PREFIX_DELIMITER);
                if (split.length == 2) {
                    sFallbacks.put(split[0], Integer.valueOf(Integer.parseInt(split[1])));
                }
            }
        }
    }

    public static int getFallbackCategory(String packageName) {
        return sFallbacks.getOrDefault(packageName, -1).intValue();
    }
}

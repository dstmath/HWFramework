package android.content.pm;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.R;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FallbackCategoryProvider {
    private static final String TAG = "FallbackCategoryProvider";
    private static final ArrayMap<String, Integer> sFallbacks = new ArrayMap<>();

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0095, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x009e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x009f, code lost:
        android.util.Log.w(TAG, "Failed to read fallback categories", r1);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x009e A[ExcHandler: IOException | NumberFormatException (r1v1 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:5:0x0026] */
    public static void loadFallbacks() {
        BufferedReader reader;
        Throwable th;
        sFallbacks.clear();
        if (SystemProperties.getBoolean("fw.ignore_fb_categories", false)) {
            Log.d(TAG, "Ignoring fallback categories");
            return;
        }
        AssetManager assets = new AssetManager();
        assets.addAssetPath("/system/framework/framework-res.apk");
        try {
            reader = new BufferedReader(new InputStreamReader(new Resources(assets, null, null).openRawResource(R.raw.fallback_categories)));
            while (true) {
                String readLine = reader.readLine();
                String line = readLine;
                if (readLine == null) {
                    break;
                } else if (line.charAt(0) != '#') {
                    String[] split = line.split(",");
                    if (split.length == 2) {
                        sFallbacks.put(split[0], Integer.valueOf(Integer.parseInt(split[1])));
                    }
                }
            }
            Log.d(TAG, "Found " + sFallbacks.size() + " fallback categories");
            reader.close();
        } catch (IOException | NumberFormatException e) {
        } catch (Throwable th2) {
            if (th != null) {
                reader.close();
            } else {
                reader.close();
            }
            throw th2;
        }
    }

    public static int getFallbackCategory(String packageName) {
        return ((Integer) sFallbacks.getOrDefault(packageName, -1)).intValue();
    }
}

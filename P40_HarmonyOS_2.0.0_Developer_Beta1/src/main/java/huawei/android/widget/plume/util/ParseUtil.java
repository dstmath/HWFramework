package huawei.android.widget.plume.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import huawei.android.widget.plume.model.PlumeData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseUtil {
    private static final int DEFAULT_CAPACITY = 1024;
    private static final String FILE_SUFFIX = ".json";
    private static final String PATH_PLUME = "huawei_plume/";
    private static final String TAG = ParseUtil.class.getSimpleName();

    private ParseUtil() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0060, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0064, code lost:
        throw r3;
     */
    public static String readJson(Context context, String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.endsWith(FILE_SUFFIX)) {
            Log.e(TAG, "Plume: File name error!");
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        try {
            AssetManager assets = context.getAssets();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assets.open(PATH_PLUME + fileName)));
            StringBuilder stringBuilder = new StringBuilder(1024);
            while (true) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                } else {
                    String json = stringBuilder.toString();
                    bufferedReader.close();
                    return json;
                }
            }
        } catch (IOException e) {
            String str = TAG;
            Log.e(str, "Plume: Exception when reading json file " + fileName);
            return StorageManagerExt.INVALID_KEY_DESC;
        }
    }

    public static JSONObject createJsonObject(String json) {
        if (json == null || json.isEmpty()) {
            Log.w(TAG, "Plume: Json is empty!");
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "Plume: Exception when creating json object! " + json);
            return null;
        }
    }

    public static void parseAttrs(JSONObject jsonObject, PlumeData plumeData) {
        if (jsonObject != null && plumeData != null) {
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                plumeData.addAttribute(key, jsonObject.optString(key));
            }
        }
    }
}

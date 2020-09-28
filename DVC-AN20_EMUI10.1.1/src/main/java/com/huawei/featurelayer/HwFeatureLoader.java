package com.huawei.featurelayer;

import android.content.Context;
import android.util.Log;
import com.huawei.featurelayer.featureframework.IFeatureFramework;
import dalvik.system.PathClassLoader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwFeatureLoader {
    private static final String BUILDIN_SYSTEM_FEATURE_CONFIG = "/system/etc/featurelist.json";
    private static final String FEATURE_CONFIG_FEATURES = "features";
    private static final String FEATURE_CONFIG_FILE = "file";
    private static final String FEATURE_CONFIG_PACKAGE = "package";
    private static final String FEATURE_CONFIG_VERSION = "version";
    private static final String FEATURE_FRAMEWORK_ENTRY = "FeatureEntry";
    private static final String FEATURE_FRAMEWORK_ENTRY_METHOD = "connect";
    private static final String FEATURE_FRAMEWORK_PKG = "com.huawei.featurelayer.featureframework";
    private static final String INSTALLED_SYSTEM_FEATURE_CONFIG = "/data/systemfeature/systemfeature/systemfeature/featurelist.json";
    private static final String TAG = "FLTAG.HwFeatureLoader";
    private static Method sCreateEntry = null;

    /* access modifiers changed from: package-private */
    public static class FeatureInfoItem {
        String path;
        int versionX = -1;
        int versionY = -1;
        int versionZ = -1;

        FeatureInfoItem(String path2, String version) {
            this.path = path2;
            String[] bits = version.split("\\.");
            if (bits.length == 3) {
                try {
                    this.versionX = Integer.valueOf(bits[0]).intValue();
                    this.versionY = Integer.valueOf(bits[1]).intValue();
                    this.versionZ = Integer.valueOf(bits[2]).intValue();
                } catch (NumberFormatException e) {
                    Log.e(HwFeatureLoader.TAG, "FeatureInfoItem() format error: " + version);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public int compareVersion(FeatureInfoItem item) {
            if (item == null) {
                return 1;
            }
            int i = this.versionX;
            int i2 = item.versionX;
            if (i != i2) {
                return i - i2;
            }
            int i3 = this.versionY;
            int i4 = item.versionY;
            if (i3 != i4) {
                return i3 - i4;
            }
            return this.versionZ - item.versionZ;
        }
    }

    private static FeatureInfoItem getFeatureFrameworkInfo(String json) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(json, new String[0])), "utf-8");
        } catch (IOException e) {
            Log.i(TAG, "getFeatureFrameworkInfo() parse error: " + json);
        }
        if (content.isEmpty()) {
            return null;
        }
        try {
            JSONArray array = new JSONObject(content).getJSONArray("features");
            int length = array.length();
            for (int j = 0; j < length; j++) {
                JSONObject feature = array.getJSONObject(j);
                if ("com.huawei.featurelayer.featureframework".equals(feature.getString("package"))) {
                    return new FeatureInfoItem(feature.getString("file"), feature.getString("version"));
                }
            }
        } catch (JSONException e2) {
            Log.e(TAG, "getFeatureFrameworkInfo() file: " + json + " , error: " + e2);
        }
        return null;
    }

    private static String getNewerAPKPath(FeatureInfoItem buildin, FeatureInfoItem installed) {
        if (buildin != null) {
            return buildin.compareVersion(installed) >= 0 ? buildin.path : installed.path;
        }
        if (installed != null) {
            return installed.path;
        }
        Log.w(TAG, "getNewerAPKPath() NO feature framework buildin or installed!");
        return null;
    }

    private static boolean loadFeatureFramework(String apkPath) {
        try {
            sCreateEntry = new PathClassLoader(apkPath, "", null).loadClass("com.huawei.featurelayer.featureframework.FeatureEntry").getDeclaredMethod(FEATURE_FRAMEWORK_ENTRY_METHOD, Context.class);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "loadFeatureFramework() ", e);
            return false;
        }
    }

    private static void loadFeatureFramework() {
        FeatureInfoItem buildin = getFeatureFrameworkInfo(BUILDIN_SYSTEM_FEATURE_CONFIG);
        FeatureInfoItem installed = getFeatureFrameworkInfo(INSTALLED_SYSTEM_FEATURE_CONFIG);
        String newerPath = getNewerAPKPath(buildin, installed);
        Log.i(TAG, "loadFeatureFramework() " + newerPath);
        if (newerPath != null && !loadFeatureFramework(newerPath) && installed != null && newerPath.equals(installed.path) && buildin != null && buildin.path != null) {
            Log.i(TAG, "loadFeatureFramework() reload: " + buildin.path);
            loadFeatureFramework(buildin.path);
        }
    }

    public static IFeatureFramework loadFeatureFramework(Context context) {
        try {
            if (sCreateEntry == null) {
                loadFeatureFramework();
            }
            if (sCreateEntry != null) {
                return (IFeatureFramework) sCreateEntry.invoke(null, context);
            }
            Log.e(TAG, "loadFeatureFramework() sCreateEntry is null");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "loadFeatureFramework() ", e);
            return null;
        }
    }

    public static class SystemFeature {
        private static IFeatureFramework sFeatureFramework = null;

        public static void loadFeatureFramework(Context context) {
            sFeatureFramework = HwFeatureLoader.loadFeatureFramework(context);
        }

        public static IFeatureFramework getFeatureFramework() {
            return sFeatureFramework;
        }

        public static void preloadClasses() {
        }
    }

    public static class SystemServiceFeature {
        private static IFeatureFramework sFeatureFramework = null;

        public static void loadFeatureFramework(Context context) {
            sFeatureFramework = HwFeatureLoader.loadFeatureFramework(context);
        }

        public static IFeatureFramework getFeatureFramework() {
            return sFeatureFramework;
        }
    }
}

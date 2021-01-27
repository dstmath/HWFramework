package com.huawei.networkit.grs.cache;

import android.content.pm.PackageManager;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.common.PLSharedPreferences;
import com.huawei.networkit.grs.utils.ContextUtil;
import java.util.Map;

public class GrsPreferences {
    private static final String GRS_APP_VERSION_KEY = "version";
    private static final String GRS_CP_KEY = "cp";
    private static final String GRS_SP_NAME = "share_pre_grs_conf";
    private static final String TAG = GrsPreferences.class.getSimpleName();
    private static PLSharedPreferences sp = null;

    public static class LazyHolder {
        static final GrsPreferences INSTANCE = new GrsPreferences();
    }

    private GrsPreferences() {
        sp = new PLSharedPreferences(ContextUtil.getContext(), GRS_SP_NAME);
        initVersionInfo();
    }

    private void initVersionInfo() {
        try {
            String version = Long.toString((long) ContextUtil.getContext().getPackageManager().getPackageInfo(ContextUtil.getContext().getPackageName(), 0).versionCode);
            String oldVersion = getString(GRS_APP_VERSION_KEY, "");
            if (!version.equals(oldVersion)) {
                Logger.v(TAG, "app version changed! old version{%s} and new version{%s}", oldVersion, version);
                removeSpFile();
                putString(GRS_APP_VERSION_KEY, version);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(TAG, "get app version failed and catch NameNotFoundException", e);
        }
    }

    public static GrsPreferences getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getCp() {
        return getString(GRS_CP_KEY, "");
    }

    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        sp.putString(key, value);
    }

    public void removeKeyValue(String key) {
        sp.remove(key);
    }

    public void removeSpFile() {
        sp.clear();
    }

    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }
}

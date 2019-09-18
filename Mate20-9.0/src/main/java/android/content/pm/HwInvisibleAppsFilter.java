package android.content.pm;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class HwInvisibleAppsFilter {
    public static final String HIDE_APP_KEY = "hw_invisible_apps_in_appmanager";
    private String mConfigHideApps;

    public HwInvisibleAppsFilter(Context context) {
        this.mConfigHideApps = Settings.System.getString(context.getContentResolver(), HIDE_APP_KEY);
    }

    public boolean isConfigToHide(String packageName) {
        if (this.mConfigHideApps == null || this.mConfigHideApps.isEmpty() || TextUtils.isEmpty(packageName) || !this.mConfigHideApps.contains(packageName)) {
            return false;
        }
        return true;
    }

    public List<ApplicationInfo> filterHideApp(List<ApplicationInfo> allApps) {
        if (this.mConfigHideApps == null || this.mConfigHideApps.isEmpty() || allApps == null) {
            return allApps;
        }
        List<ApplicationInfo> willRemove = new ArrayList<>();
        for (ApplicationInfo info : allApps) {
            if (this.mConfigHideApps.contains(info.packageName)) {
                willRemove.add(info);
            }
        }
        if (willRemove.size() > 0) {
            allApps.removeAll(willRemove);
        }
        return allApps;
    }
}

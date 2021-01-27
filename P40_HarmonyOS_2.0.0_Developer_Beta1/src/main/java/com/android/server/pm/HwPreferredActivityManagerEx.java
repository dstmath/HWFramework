package com.android.server.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class HwPreferredActivityManagerEx {
    private HwPreferredActivityManager hwPreferredActivityManager;

    public HwPreferredActivityManagerEx(Context context, SettingsEx settings, PackageManagerServiceEx pms) {
        this.hwPreferredActivityManager = HwPreferredActivityManager.getInstance(context, settings.getSettings(), pms.getPackageManagerSerivce());
    }

    public HwPreferredActivityManager getHwPreferredActivityManager() {
        return this.hwPreferredActivityManager;
    }

    public void setHwPreferredActivityManager(HwPreferredActivityManager hwPreferredActivityManager2) {
        this.hwPreferredActivityManager = hwPreferredActivityManager2;
    }

    public boolean resolvePreferredActivity(IntentFilter filter, int match, ComponentName[] sets, ComponentName activity, int userId) {
        return this.hwPreferredActivityManager.resolvePreferredActivity(filter, match, sets, activity, userId);
    }

    public void rebuildPreferredActivity(int userId) {
        this.hwPreferredActivityManager.rebuildPreferredActivity(userId);
    }

    public boolean removeMatchedPreferredActivity(Intent intent, PreferredIntentResolverEx preferredIntentResolver, PreferredActivityEx preferredActivity) {
        return this.hwPreferredActivityManager.removeMatchedPreferredActivity(intent, preferredIntentResolver.getPreferredIntentResolver(), preferredActivity.getPreferredActivity());
    }
}

package com.android.server.pm;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwCompensateDexOptManager {
    private static final boolean IS_DEBUG = "on".equals(SystemProperties.get("ro.dbg.pms_log", "0"));
    private static final String TAG = "HwCompensateDexOptManager";
    private final ArraySet<String> mWaitToDexOptPackages;

    private HwCompensateDexOptManager() {
        this.mWaitToDexOptPackages = new ArraySet<>();
    }

    /* access modifiers changed from: private */
    public static class HwCompensateDexOptManagerHolder {
        static HwCompensateDexOptManager hwCompensateDexOptManager = new HwCompensateDexOptManager();

        private HwCompensateDexOptManagerHolder() {
        }
    }

    public static synchronized HwCompensateDexOptManager getInstance() {
        HwCompensateDexOptManager hwCompensateDexOptManager;
        synchronized (HwCompensateDexOptManager.class) {
            hwCompensateDexOptManager = HwCompensateDexOptManagerHolder.hwCompensateDexOptManager;
        }
        return hwCompensateDexOptManager;
    }

    public void addWaitDexOptPackage(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            Slog.i(TAG, "addWaitDexOptPackage:" + packageName);
            synchronized (this.mWaitToDexOptPackages) {
                if (!this.mWaitToDexOptPackages.contains(packageName)) {
                    this.mWaitToDexOptPackages.add(packageName);
                }
            }
        }
    }

    public List<String> getWaitingDexOptPackages() {
        synchronized (this.mWaitToDexOptPackages) {
            if (this.mWaitToDexOptPackages.isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList(this.mWaitToDexOptPackages);
        }
    }

    public void removePackageFromWaitingList(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            synchronized (this.mWaitToDexOptPackages) {
                this.mWaitToDexOptPackages.remove(packageName);
            }
        }
    }
}

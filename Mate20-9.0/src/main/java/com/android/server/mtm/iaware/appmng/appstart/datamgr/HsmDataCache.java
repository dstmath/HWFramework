package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.iaware.srms.AppStartupFeature;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class HsmDataCache {
    private static final String BOOTCACHE_KEY_BLIND = "blind";
    private static final String BOOTCACHE_KEY_SEPARATOR = ":";
    private static final String BOOTCACHE_KEY_WIDGET = "widget";
    public static final long INVALID_ELAPSETIME = -1;
    private static final String STARTUP_SETTING_SEPARATOR = "_";
    private static final String TAG = "HsmDataCache";
    private Set<String> mBlindPkgs = new ArraySet();
    private CacheFileRW mCachedBootListFile;
    private CacheFileRW mSettingListFile;
    private Map<String, HwAppStartupSetting> mStartupSetting = new ArrayMap();
    private Map<String, WidgetUpdateInfo> mWidgetPkgs = new ArrayMap();

    private static class WidgetUpdateInfo {
        public long mUpdateElapse;

        public WidgetUpdateInfo(long updateElapse) {
            this.mUpdateElapse = updateElapse;
        }
    }

    public HsmDataCache() {
        File file = new File(new File(Environment.getDataDirectory(), "system"), "appstart");
        if (!file.exists() && !file.mkdirs()) {
            AwareLog.w(TAG, "Fail to make dir " + file.getPath());
        }
        this.mCachedBootListFile = new CacheFileRW(new File(file, "appstart_cached_boot.list"));
        File settingListFile = new File(file, "appstart_cached_setting.list");
        this.mSettingListFile = new CacheFileRW(settingListFile);
        if (!settingListFile.exists()) {
            SystemProperties.set("persist.sys.appstart.sync", "true");
            AwareLog.w(TAG, "HSM need sync since the list file is not exist");
        }
    }

    public HwAppStartupSetting getAppStartupSetting(String pkgName) {
        synchronized (this.mStartupSetting) {
            HwAppStartupSetting setting = this.mStartupSetting.get(pkgName);
            if (!checkAppStartupSetting(setting)) {
                return null;
            }
            HwAppStartupSetting hwAppStartupSetting = new HwAppStartupSetting(setting);
            return hwAppStartupSetting;
        }
    }

    public List<HwAppStartupSetting> retrieveStartupSettings(List<String> pkgList, HwAppStartupSettingFilter filter) {
        int[] policy = null;
        int[] show = null;
        int[] modifier = null;
        if (filter != null) {
            policy = filter.getPolicy();
            modifier = filter.getModifier();
            show = filter.getShow();
        }
        List<HwAppStartupSetting> settingList = new ArrayList<>();
        synchronized (this.mStartupSetting) {
            if (pkgList == null) {
                try {
                    for (Map.Entry<String, HwAppStartupSetting> entry : this.mStartupSetting.entrySet()) {
                        HwAppStartupSetting setting = entry.getValue();
                        if (filterAppStartupSetting(setting, policy, modifier, show)) {
                            settingList.add(new HwAppStartupSetting(setting));
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                for (String pkg : pkgList) {
                    HwAppStartupSetting setting2 = this.mStartupSetting.get(pkg);
                    if (filterAppStartupSetting(setting2, policy, modifier, show)) {
                        settingList.add(new HwAppStartupSetting(setting2));
                    }
                }
            }
        }
        return settingList;
    }

    public boolean updateStartupSettings(List<HwAppStartupSetting> settingList, boolean clearFirst) {
        if (settingList == null || settingList.size() == 0) {
            return false;
        }
        boolean success = false;
        if (clearFirst) {
            clearStartupSettingCache();
        }
        for (HwAppStartupSetting item : settingList) {
            if (updateSingleSetting(item)) {
                success = true;
            }
        }
        return success;
    }

    public boolean removeStartupSetting(String pkgName) {
        boolean z;
        synchronized (this.mStartupSetting) {
            z = this.mStartupSetting.remove(pkgName) != null;
        }
        return z;
    }

    public void loadStartupSettingCache() {
        loadDiskStartupSetting();
    }

    public void loadBootCache() {
        List<String> cachedList = new ArrayList<>();
        loadDiskCachedBootLocked(cachedList);
        Map<String, WidgetUpdateInfo> widgetPkgs = new ArrayMap<>();
        Set<String> blindPkgs = new ArraySet<>();
        long curTime = SystemClock.elapsedRealtime();
        for (String bootCached : cachedList) {
            if (bootCached != null) {
                String[] bootPkgList = bootCached.split(BOOTCACHE_KEY_SEPARATOR);
                if (bootPkgList.length > 1) {
                    String keyString = bootPkgList[0];
                    String valueString = bootPkgList[1];
                    if (BOOTCACHE_KEY_WIDGET.equals(keyString)) {
                        widgetPkgs.put(valueString, new WidgetUpdateInfo(curTime));
                    } else if (BOOTCACHE_KEY_BLIND.equals(keyString)) {
                        blindPkgs.add(valueString);
                    }
                }
            }
        }
        this.mWidgetPkgs = widgetPkgs;
        this.mBlindPkgs = blindPkgs;
    }

    public void clearStartupSettingCache() {
        synchronized (this.mStartupSetting) {
            this.mStartupSetting.clear();
        }
    }

    private boolean checkAppStartupSetting(HwAppStartupSetting setting) {
        return setting != null && setting.valid();
    }

    private boolean filterAppStartupSetting(HwAppStartupSetting setting, int[] policy, int[] modifier, int[] show) {
        if (!checkAppStartupSetting(setting)) {
            return false;
        }
        if (policy != null) {
            for (int i = 0; i < policy.length; i++) {
                if (policy[i] != -1 && policy[i] != setting.getPolicy(i)) {
                    return false;
                }
            }
        }
        if (show != null) {
            for (int i2 = 0; i2 < show.length; i2++) {
                if (show[i2] != -1 && show[i2] != setting.getShow(i2)) {
                    return false;
                }
            }
        }
        if (modifier != null) {
            for (int i3 = 0; i3 < modifier.length; i3++) {
                if (modifier[i3] != -1 && modifier[i3] != setting.getModifier(i3)) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        return true;
     */
    private boolean updateSingleSetting(HwAppStartupSetting setting) {
        String pkg = setting != null ? setting.getPackageName() : "";
        synchronized (this.mStartupSetting) {
            HwAppStartupSetting old = this.mStartupSetting.get(pkg);
            if (old != null) {
                this.mStartupSetting.put(pkg, old.copyValidInfo(setting));
            } else if (checkAppStartupSetting(setting)) {
                this.mStartupSetting.put(pkg, setting);
            } else {
                AwareLog.w(TAG, "updateSingleSetting invalid setting: " + setting);
                return false;
            }
        }
    }

    public void updateWidgetPkgList(Set<String> pkgList) {
        Map<String, WidgetUpdateInfo> cachedPkgs = new ArrayMap<>();
        Map<String, WidgetUpdateInfo> cachedPkgsOld = this.mWidgetPkgs;
        for (String pkg : pkgList) {
            WidgetUpdateInfo updateInfo = cachedPkgsOld.get(pkg);
            if (updateInfo == null) {
                updateInfo = new WidgetUpdateInfo(SystemClock.elapsedRealtime());
            }
            cachedPkgs.put(pkg, updateInfo);
        }
        this.mWidgetPkgs = cachedPkgs;
    }

    public void updateWidgetUpdateTime(String pkgName) {
        WidgetUpdateInfo updateInfo = this.mWidgetPkgs.get(pkgName);
        if (updateInfo != null) {
            updateInfo.mUpdateElapse = SystemClock.elapsedRealtime();
        }
    }

    public boolean isWidgetExistPkg(String pkgName) {
        return this.mWidgetPkgs.containsKey(pkgName);
    }

    public int getWidgetExistPkgCnt() {
        return this.mWidgetPkgs.size();
    }

    public long getWidgetExistPkgUpdateTime(String pkgName) {
        WidgetUpdateInfo updateInfo = this.mWidgetPkgs.get(pkgName);
        if (updateInfo == null) {
            return -1;
        }
        return updateInfo.mUpdateElapse;
    }

    public void updateBlindPkg(Set<String> pkgSet) {
        Set<String> cachedPkgs = new ArraySet<>();
        cachedPkgs.addAll(pkgSet);
        this.mBlindPkgs = cachedPkgs;
    }

    public boolean isBlindAssistPkg(String pkgName) {
        return this.mBlindPkgs.contains(pkgName);
    }

    public void flushStartupSettingToDisk() {
        writeDiskStartupSetting();
    }

    public void flushBootCacheDataToDisk() {
        writeDiskCachedBootLocked();
    }

    private void loadDiskStartupSetting() {
        List<String> lines;
        synchronized (this.mSettingListFile) {
            lines = this.mSettingListFile.readFileLines();
        }
        synchronized (this.mStartupSetting) {
            for (String line : lines) {
                fromStringLine(line);
            }
        }
    }

    private void loadDiskCachedBootLocked(List<String> pkgList) {
        List<String> lines;
        synchronized (this.mCachedBootListFile) {
            lines = this.mCachedBootListFile.readFileLines();
        }
        pkgList.addAll(lines);
    }

    private void appendCachedBootToList(String key, List<String> cachedList, Set<String> pkgs) {
        for (String pkg : pkgs) {
            if (pkg != null) {
                cachedList.add(key + BOOTCACHE_KEY_SEPARATOR + pkg);
            }
        }
    }

    private void writeDiskCachedBootLocked() {
        List<String> cachedList = new ArrayList<>();
        Map<String, WidgetUpdateInfo> widgetPkgs = this.mWidgetPkgs;
        Set<String> blindPkgs = this.mBlindPkgs;
        appendCachedBootToList(BOOTCACHE_KEY_WIDGET, cachedList, widgetPkgs.keySet());
        appendCachedBootToList(BOOTCACHE_KEY_BLIND, cachedList, blindPkgs);
        synchronized (this.mCachedBootListFile) {
            this.mCachedBootListFile.writeFileLines(cachedList);
        }
    }

    private void writeDiskStartupSetting() {
        List<String> result = new ArrayList<>();
        synchronized (this.mStartupSetting) {
            for (Map.Entry<String, HwAppStartupSetting> entry : this.mStartupSetting.entrySet()) {
                result.add(toStringLine(entry));
            }
        }
        synchronized (this.mSettingListFile) {
            this.mSettingListFile.writeFileLines(result);
        }
    }

    private void fromStringLine(String line) {
        if (!TextUtils.isEmpty(line)) {
            try {
                int[] config = new int[12];
                String pkgName = line;
                for (int i = 0; i < config.length; i++) {
                    config[i] = -1;
                    int lastIndex = pkgName.lastIndexOf("_");
                    if (lastIndex >= 0 && lastIndex == pkgName.length() - 2) {
                        config[i] = Integer.parseInt(pkgName.substring(lastIndex + 1));
                        pkgName = pkgName.substring(0, lastIndex);
                    }
                }
                HwAppStartupSetting item = new HwAppStartupSetting(pkgName, new int[]{config[11], config[10], config[9], config[8]}, new int[]{config[7], config[6], config[5], config[4]}, new int[]{config[3], config[2], config[1], config[0]});
                if (item.valid()) {
                    this.mStartupSetting.put(pkgName, item);
                } else {
                    AwareLog.e(TAG, "fromStringLine invalid record line: " + line);
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "fromStringLine invalid record line: " + line);
            }
        }
    }

    private String toStringLine(Map.Entry<String, HwAppStartupSetting> entry) {
        HwAppStartupSetting item = entry.getValue();
        StringBuilder sb = new StringBuilder();
        sb.append(item.getPackageName());
        sb.append("_");
        for (int i = 0; i < 4; i++) {
            sb.append(item.getPolicy(i));
            sb.append("_");
        }
        for (int i2 = 0; i2 < 4; i2++) {
            sb.append(item.getModifier(i2));
            sb.append("_");
        }
        for (int i3 = 0; i3 < 4; i3++) {
            sb.append(item.getShow(i3));
            if (i3 < 3) {
                sb.append("_");
            }
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder cacheStr = new StringBuilder();
        if (AppStartupFeature.isAppStartupEnabled()) {
            Map<String, WidgetUpdateInfo> widgetPkgs = this.mWidgetPkgs;
            Set<String> blindPkgs = this.mBlindPkgs;
            cacheStr.append("Widgets(");
            cacheStr.append(widgetPkgs.keySet().size());
            cacheStr.append("):");
            for (Map.Entry<String, WidgetUpdateInfo> m : widgetPkgs.entrySet()) {
                cacheStr.append("\n  ");
                cacheStr.append(m.getKey());
                cacheStr.append("  sec:");
                cacheStr.append((SystemClock.elapsedRealtime() - m.getValue().mUpdateElapse) / 1000);
            }
            cacheStr.append("\nBlind(");
            cacheStr.append(blindPkgs.size());
            cacheStr.append("):");
            for (String blindPkg : blindPkgs) {
                cacheStr.append("\n  ");
                cacheStr.append(blindPkg);
            }
        }
        synchronized (this.mStartupSetting) {
            cacheStr.append("\nStartupSetting(");
            cacheStr.append(this.mStartupSetting.size());
            cacheStr.append("):");
            for (Map.Entry<String, HwAppStartupSetting> entry : this.mStartupSetting.entrySet()) {
                cacheStr.append("\n  ");
                cacheStr.append(entry.getValue());
            }
        }
        return cacheStr.toString();
    }
}

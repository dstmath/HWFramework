package com.android.server.rms.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.internal.util.MemInfoReaderExt;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AwareAppUseDataManager {
    private static final int CANCEL_MEM_CACHE = 0;
    private static final int DEFAULT_BASE_WEIGHT = 0;
    private static final long DEFAULT_TIME = -1;
    private static final int DEFAULT_VALUE = -1;
    private static final int HIGH_MEM = 6144;
    private static final int HIGH_MEM_CACHE = 10;
    private static final String IMPORTANT_SYS_APP_MISC = "important_sys_apps";
    private static final int LOW_MEM = 3072;
    private static final int LOW_MEM_CACHE = 3;
    private static final int MAX_SUN_CONFIG_NUM = 2;
    private static final int MID_MEM = 4096;
    private static final int MID_MEM_CACHE = 5;
    private static final Object SLOCK = new Object();
    private static final String TAG = "AwareAppUseDataManager";
    private static final String USER_HABIT_CONFIG_NAME = "HabitFilterList";
    private static final String USER_HABIT_FEATUER_NAME = "AppManagement";
    private static final String WEIGHT_SET_REASON = "weight is decided by recent use";
    private static AwareAppUseDataManager sAwareAppUseDataMgr;
    private final LinkedHashMap<String, Long> mAppUseData;
    private int mBaseWeight;
    private final Set<String> mFilterPkgs;
    private final ArrayList<String> mImportSysApps;
    private boolean mIsIntelliCleanEnable;
    private boolean mIsReady;
    private int mMaxCacheNum;
    private long mRecentTime;

    public static AwareAppUseDataManager getInstance() {
        AwareAppUseDataManager awareAppUseDataManager;
        synchronized (SLOCK) {
            if (sAwareAppUseDataMgr == null) {
                sAwareAppUseDataMgr = new AwareAppUseDataManager();
            }
            awareAppUseDataManager = sAwareAppUseDataMgr;
        }
        return awareAppUseDataManager;
    }

    private AwareAppUseDataManager() {
        this.mAppUseData = new LinkedHashMap<>();
        this.mFilterPkgs = new ArraySet();
        this.mImportSysApps = new ArrayList<>();
        this.mIsIntelliCleanEnable = SystemPropertiesEx.getBoolean("persist.sys.iaware.intelliclean", true);
        this.mRecentTime = SystemPropertiesEx.getLong("persist.sys.iaware.recenttime", 1800000);
        this.mMaxCacheNum = 0;
        this.mIsReady = false;
        this.mBaseWeight = 0;
        this.mMaxCacheNum = SystemPropertiesEx.getInt("persist.sys.iaware.recentapp", getMaxCacheDefaultValue());
    }

    private int getMaxCacheDefaultValue() {
        MemInfoReaderExt memInfo = new MemInfoReaderExt();
        memInfo.readMemInfo();
        long totalMemMb = memInfo.getTotalSize() / MemoryConstant.MB_SIZE;
        if (totalMemMb <= 3072) {
            return 0;
        }
        if (totalMemMb <= 4096) {
            return 3;
        }
        if (totalMemMb <= 6144) {
            return 5;
        }
        return 10;
    }

    private boolean isEnable() {
        return this.mIsReady && this.mIsIntelliCleanEnable;
    }

    private boolean needFilterPkg(String pkgName) {
        boolean isImportSysApps;
        synchronized (this.mFilterPkgs) {
            isImportSysApps = this.mFilterPkgs.contains(pkgName);
        }
        if (!isImportSysApps) {
            synchronized (this.mImportSysApps) {
                isImportSysApps = this.mImportSysApps.contains(pkgName);
            }
        }
        return isImportSysApps;
    }

    public void updateFgActivityChange(int pid, int uid, boolean foregroundActivities) {
        if (isEnable() && !foregroundActivities) {
            String pkgName = InnerUtils.getAwarePkgName(pid);
            if (!needFilterPkg(pkgName)) {
                synchronized (this.mAppUseData) {
                    if (this.mAppUseData.containsKey(pkgName)) {
                        this.mAppUseData.remove(pkgName);
                    }
                    this.mAppUseData.put(pkgName, Long.valueOf(System.currentTimeMillis()));
                    if (this.mAppUseData.size() > this.mMaxCacheNum) {
                        removeOutTimeData();
                    }
                }
            }
        }
    }

    private void removeOutTimeData() {
        synchronized (this.mAppUseData) {
            int needRemoveLeast = this.mAppUseData.size() - this.mMaxCacheNum;
            Iterator<Map.Entry<String, Long>> it = this.mAppUseData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> item = it.next();
                if (item != null) {
                    if (needRemoveLeast > 0) {
                        it.remove();
                        needRemoveLeast--;
                    } else {
                        Long time = item.getValue();
                        if (time == null) {
                            it.remove();
                        } else if (System.currentTimeMillis() - time.longValue() > this.mRecentTime) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    public ArrayList<String> getRecentApp() {
        if (!isEnable()) {
            return new ArrayList<>();
        }
        removeOutTimeData();
        ArrayList<String> recentApps = new ArrayList<>();
        synchronized (this.mAppUseData) {
            for (Map.Entry<String, Long> entry : this.mAppUseData.entrySet()) {
                if (entry != null) {
                    String pkg = entry.getKey();
                    if (pkg != null) {
                        recentApps.add(pkg);
                    }
                }
            }
        }
        synchronized (this.mImportSysApps) {
            recentApps.addAll(this.mImportSysApps);
        }
        return recentApps;
    }

    public void dumpFilterPkgs(PrintWriter pw) {
        if (pw != null) {
            if (!isEnable()) {
                pw.println("AwareAppUseDataManager disable.");
                return;
            }
            synchronized (this.mFilterPkgs) {
                pw.println(this.mFilterPkgs);
            }
        }
    }

    public void dumpAppUseData(PrintWriter pw) {
        if (pw != null) {
            if (!isEnable()) {
                pw.println("AwareAppUseDataManager disable.");
                return;
            }
            long now = System.currentTimeMillis();
            pw.println("mRecentTime : " + this.mRecentTime);
            pw.println("mMaxCacheNum : " + this.mMaxCacheNum);
            pw.println("mBaseWeight : " + this.mBaseWeight);
            synchronized (this.mAppUseData) {
                pw.println("app use data :");
                for (Map.Entry<String, Long> entry : this.mAppUseData.entrySet()) {
                    pw.println(entry.getKey() + " : " + (now - entry.getValue().longValue()));
                }
            }
        }
    }

    public void initUserSwitch() {
        if (isEnable()) {
            AwareLog.i(TAG, "User switch complete, clear data.");
            synchronized (this.mAppUseData) {
                this.mAppUseData.clear();
            }
        }
    }

    public void reportStart() {
        if (!this.mIsIntelliCleanEnable) {
            AwareLog.i(TAG, "mIsIntelliCleanEnable is false.");
            return;
        }
        AwareLog.i(TAG, " report start.");
        if (loadConfigFromXml()) {
            this.mIsReady = true;
        }
    }

    private boolean loadConfigFromXml() {
        boolean res;
        synchronized (this.mFilterPkgs) {
            res = initFilterPkgLock();
        }
        if (res) {
            loadIntelliCleanConfig();
        }
        return res;
    }

    private AwareConfig getConfig(String featureName, String configName) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
            AwareLog.i(TAG, "can not find service awareService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
            return null;
        }
    }

    private void loadIntelliCleanConfig() {
        AwareConfig.Item curMemItem;
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.INTELLIGENT_CLEAN);
        if (configList != null && (curMemItem = MemoryUtils.getCurrentMemItem(configList, false)) != null) {
            loadDataFromCurMemItem(curMemItem);
        }
    }

    private void loadDataFromCurMemItem(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    char c = 65535;
                    switch (itemName.hashCode()) {
                        case -889473228:
                            if (itemName.equals("switch")) {
                                c = 0;
                                break;
                            }
                            break;
                        case -723701015:
                            if (itemName.equals(MemoryConstant.RECENT_APP_COUNT)) {
                                c = 2;
                                break;
                            }
                            break;
                        case -717351767:
                            if (itemName.equals(MemoryConstant.BASE_WEIGHT)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 808439219:
                            if (itemName.equals(MemoryConstant.RECENT_APP_TIME)) {
                                c = 3;
                                break;
                            }
                            break;
                    }
                    if (c != 0) {
                        if (c == 1) {
                            try {
                                this.mBaseWeight = Integer.parseInt(itemValue.trim());
                            } catch (NumberFormatException e) {
                                AwareLog.i(TAG, "loadDataFromCurMemItem mBaseWeight NumberFormatException");
                            }
                        } else if (c == 2) {
                            int cache = getIntFromString(itemValue);
                            if (cache >= 0) {
                                this.mMaxCacheNum = cache;
                            }
                        } else if (c == 3) {
                            long time = getLongFromString(itemValue);
                            if (time >= 0) {
                                this.mRecentTime = 1000 * time;
                            }
                        }
                    } else if ("0".equals(itemValue.trim())) {
                        this.mIsIntelliCleanEnable = false;
                    } else if ("1".equals(itemValue.trim())) {
                        this.mIsIntelliCleanEnable = true;
                    } else {
                        AwareLog.i(TAG, "loadDataFromCurMemItem switch only accept 0 or 1");
                    }
                }
            }
        }
    }

    private long getLongFromString(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            AwareLog.i(TAG, "getLongFromString NumberFormatException!");
            return -1;
        }
    }

    private int getIntFromString(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            AwareLog.i(TAG, "getIntFromString NumberFormatException!");
            return -1;
        }
    }

    private boolean initFilterPkgLock() {
        AwareConfig configList = getConfig("AppManagement", "HabitFilterList");
        if (configList == null) {
            AwareLog.i(TAG, "configList is null.");
            return true;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.i(TAG, "itemList is null.");
            return true;
        }
        int count = 0;
        for (AwareConfig.Item item : itemList) {
            if (count >= 2) {
                break;
            } else if (!(item == null || item.getSubItemList() == null)) {
                for (AwareConfig.SubItem subitem : item.getSubItemList()) {
                    if (subitem != null) {
                        this.mFilterPkgs.add(subitem.getValue());
                    }
                }
                count++;
            }
        }
        return true;
    }

    public long getLastUseTime(String pkg) {
        if (!isEnable() || pkg == null) {
            return -1;
        }
        synchronized (this.mAppUseData) {
            Long time = this.mAppUseData.get(pkg);
            if (time == null) {
                return -1;
            }
            return System.currentTimeMillis() - time.longValue();
        }
    }

    public void reportCleanConfigReadFinish() {
        if (!this.mIsIntelliCleanEnable) {
            AwareLog.i(TAG, "mIsIntelliCleanEnable is false.");
        } else {
            initImportSysApps();
        }
    }

    private void initImportSysApps() {
        ArrayList<String> importSysApps = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_CLEAN.getDesc(), IMPORTANT_SYS_APP_MISC);
        if (importSysApps != null) {
            synchronized (this.mImportSysApps) {
                this.mImportSysApps.clear();
                this.mImportSysApps.addAll(importSysApps);
            }
        }
    }

    public void dumpImportSysApps(PrintWriter pw) {
        if (pw != null) {
            if (!isEnable()) {
                pw.println("AwareAppUseDataManager disable.");
                return;
            }
            synchronized (this.mImportSysApps) {
                pw.println(this.mImportSysApps);
            }
        }
    }

    public String getWeightSetReason() {
        return WEIGHT_SET_REASON;
    }

    public ArrayMap<String, Integer> getRecentAppsWeight() {
        if (!isEnable() || this.mBaseWeight < 0) {
            return new ArrayMap<>();
        }
        removeOutTimeData();
        ArrayMap<String, Integer> recentAppsWeight = new ArrayMap<>();
        int weight = this.mBaseWeight;
        synchronized (this.mAppUseData) {
            for (Map.Entry<String, Long> entry : this.mAppUseData.entrySet()) {
                if (entry != null) {
                    String pkg = entry.getKey();
                    if (pkg != null) {
                        recentAppsWeight.put(pkg, Integer.valueOf(weight));
                        weight++;
                    }
                }
            }
        }
        synchronized (this.mImportSysApps) {
            Iterator<String> it = this.mImportSysApps.iterator();
            while (it.hasNext()) {
                String app = it.next();
                if (app != null) {
                    recentAppsWeight.put(app, Integer.valueOf(weight));
                }
            }
        }
        return recentAppsWeight;
    }
}

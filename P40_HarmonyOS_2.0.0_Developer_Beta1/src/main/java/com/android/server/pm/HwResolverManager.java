package com.android.server.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HwResolverManager {
    private static final int ADD_PCASS = 1;
    private static final String BROWSER_PACKAGE_NAME = "browser";
    private static final int CREATE_GROUP_FAILED = -35;
    private static final int DEVICE_TYPE_PAD = 2;
    private static final int DEVICE_TYPE_PC = 1;
    private static final String ENABLE_PC_PROGRAM = "enable_pc_program";
    private static final boolean IS_DEBUG = SystemProperties.get("ro.dbg.pms_log", "0").equals("on");
    private static final int MULTISCREEN_COL_ENABLE_VALUE = SystemProperties.getInt("hw_mc.multiscreen.collaboration.value", 0);
    private static final int MULTISCREEN_COL_WIRED_FIRST_MASK = 1;
    private static final int MULTISCREEN_PAD_COL_ENABLE_VALUE = SystemProperties.getInt("hw_mc.multiscreen.padcollaboration.value", 1);
    private static final int OPEN_FILE_SUCCESS = 0;
    private static final int PHONE2PAD_VERSION = 1;
    private static final int PHONE2PC_VERSION = 1;
    private static final String TAG = "HwResolverManager";
    private static volatile HwResolverManager sInstance;
    private Map<Integer, Map<String, PreferredActivity>> mPreferredActivityCacheInPcMode = new ConcurrentHashMap();
    private Map<String, Integer> mResolveTypeMap = new HashMap();
    private Set<String> mTagFirstOpenFileTypeSetEnable = new HashSet();
    private Map<Integer, Map<Integer, Boolean>> mVersionMatchMap = new HashMap();

    private HwResolverManager() {
    }

    public static HwResolverManager getInstance() {
        if (sInstance == null) {
            synchronized (HwResolverManager.class) {
                if (sInstance == null) {
                    sInstance = new HwResolverManager();
                }
            }
        }
        return sInstance;
    }

    public void putPreferredActivityInPcMode(int userId, IntentFilter filter, PreferredActivity preferredActivity) {
        String resolvedType;
        Slog.d(TAG, "putPreferredActivityInPcMode enter.");
        if (filter == null || preferredActivity == null) {
            Slog.d(TAG, "putPreferredActivityInPcMode: Request parameters are null.");
            return;
        }
        if (filter.countDataTypes() == 0 && filter.countDataSchemes() != 0 && (filter.hasDataScheme("http") || filter.hasDataScheme("https"))) {
            resolvedType = BROWSER_PACKAGE_NAME;
        } else if (filter.countDataTypes() != 0) {
            resolvedType = filter.getDataType(0);
        } else {
            resolvedType = null;
        }
        if (resolvedType == null) {
            Slog.d(TAG, "putPreferredActivityInPcMode: resolvedType is null.");
            return;
        }
        if (IS_DEBUG) {
            Slog.d(TAG, "putPreferredActivityInPcMode preferredActivity: " + preferredActivity + ", resolvedType: " + resolvedType);
        }
        if (!this.mPreferredActivityCacheInPcMode.containsKey(Integer.valueOf(userId))) {
            this.mPreferredActivityCacheInPcMode.put(Integer.valueOf(userId), new ConcurrentHashMap<>());
        }
        Map<String, PreferredActivity> resolvedType2PreActivities = this.mPreferredActivityCacheInPcMode.get(Integer.valueOf(userId));
        resolvedType2PreActivities.put(resolvedType, preferredActivity);
        this.mPreferredActivityCacheInPcMode.put(Integer.valueOf(userId), resolvedType2PreActivities);
    }

    public ResolveInfo findPreferredActivityInCache(IHwPackageManagerInner pms, Intent intent, String resolvedType, int flags, List<ResolveInfo> query, int userId) {
        if (pms == null || intent == null || query == null) {
            Slog.d(TAG, "findPreferredActivityInCache: Request parameters are null.");
            return null;
        }
        PreferredActivity pa = getPreferredActivityListInPcMode(userId, getResolvedType(intent, resolvedType));
        if (pa == null) {
            Slog.d(TAG, "findPreferredActivityInCache: Pa is null.");
            return null;
        }
        ActivityInfo ai = pms.getActivityInfo(pa.mPref.mComponent, flags, userId);
        if (ai == null || !pa.mPref.mAlways) {
            Slog.d(TAG, "findPreferredActivityInCache: ai is null.");
            return null;
        }
        for (ResolveInfo resolveInfo : query) {
            if (resolveInfo.activityInfo.applicationInfo.packageName.equals(ai.applicationInfo.packageName) && resolveInfo.activityInfo.name.equals(ai.name)) {
                if (IS_DEBUG) {
                    Slog.d(TAG, "findPreferredActivityInCache: Return packageName: " + resolveInfo.activityInfo.applicationInfo.packageName);
                }
                return resolveInfo;
            }
        }
        return null;
    }

    public boolean isOnlyOncePreferredActivity(Intent intent, String resolvedType, int userId) {
        if (intent == null) {
            Slog.d(TAG, "isOnlyOncePreferredActivity: Request parameters are null.");
            return false;
        }
        PreferredActivity pa = getPreferredActivityListInPcMode(userId, getResolvedType(intent, resolvedType));
        if (pa != null) {
            return !pa.mPref.mAlways;
        }
        Slog.d(TAG, "isOnlyOncePreferredActivity: Pa is null.");
        return false;
    }

    public boolean isEmptyPreferredActivityCache(int userId, String resolvedType) {
        if (resolvedType == null) {
            Slog.d(TAG, "isEmptyPreferredActivityCache: resolvedType is null.");
            return false;
        }
        Map<String, PreferredActivity> resolvedType2PreActivities = this.mPreferredActivityCacheInPcMode.get(Integer.valueOf(userId));
        if (resolvedType2PreActivities == null || resolvedType2PreActivities.isEmpty()) {
            Slog.d(TAG, "isEmptyPreferredActivityCache: resolvedType2PreActivities is empty.");
            return true;
        } else if (resolvedType2PreActivities.get(resolvedType) == null) {
            return true;
        } else {
            return false;
        }
    }

    public void preChooseBestActivity(Intent intent, List<ResolveInfo> querys, String resolvedType, int userId) {
        if (intent == null) {
            Slog.d(TAG, "preChooseBestActivity: Request parameters are null.");
        } else if (skipChoosenBestActivity(intent, querys, resolvedType, userId, this.mTagFirstOpenFileTypeSetEnable) && isEmptyPreferredActivityCache(userId, getResolvedType(intent, resolvedType))) {
            addPreferredActivity(querys, resolvedType, intent, "com.huawei.pcassistant", userId);
        }
    }

    public void clearFirstOpenFileTypeTags() {
        if (!HwPCUtils.isInWindowsCastMode()) {
            this.mTagFirstOpenFileTypeSetEnable.clear();
            this.mVersionMatchMap.clear();
            this.mResolveTypeMap.clear();
            this.mPreferredActivityCacheInPcMode.clear();
        }
    }

    public boolean isMultiScreenCollaborationEnabled(Context context, Intent intent) {
        if (context == null) {
            Slog.d(TAG, "isMultiScreenCollaborationEnabled: Request parameters are null.");
            return false;
        } else if (isPcMultiScreenCollaborationEnabled(context, intent) || isPadMultiScreenCollaborationEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPcMultiScreenCollaborationEnabled(Context context, Intent intent) {
        boolean isMatchSuccess;
        boolean z = true;
        boolean isMultiEnabled = (MULTISCREEN_COL_ENABLE_VALUE & 1) == 1 && HwPCUtils.isInWindowsCastMode();
        if (!isMultiEnabled || !(isMatchSuccess = getVersionMatchFlag(1, 1))) {
            return false;
        }
        if (intent != null) {
            if (IS_DEBUG) {
                Slog.i(TAG, "isPcMultiScreenCollaborationEnabled: default browser.");
            }
            return true;
        }
        boolean isEnablePcProgram = false;
        try {
            if (Settings.System.getInt(context.getContentResolver(), ENABLE_PC_PROGRAM) != 1) {
                z = false;
            }
            isEnablePcProgram = z;
        } catch (Settings.SettingNotFoundException e) {
            Slog.i(TAG, "isPcMultiScreenCollaborationEnabled: Operate settings exception! ");
        }
        if (IS_DEBUG) {
            Slog.d(TAG, "isPcMultiScreenCollaborationEnabled isMultiEnabled: " + isMultiEnabled + ", isMatchSuccess: " + isMatchSuccess + ", isEnablePcProgram: " + isEnablePcProgram);
        }
        return isEnablePcProgram;
    }

    private boolean isPadMultiScreenCollaborationEnabled() {
        boolean isMultiEnabled = MULTISCREEN_PAD_COL_ENABLE_VALUE == 1 && HwPCUtils.isInWindowsCastMode();
        boolean isMatchSuccess = getVersionMatchFlag(2, 1);
        if (IS_DEBUG) {
            Slog.d(TAG, "isPadMultiScreenCollaborationEnabled isMultiEnabled:" + isMultiEnabled + ",isMatchSuccess:" + isMatchSuccess);
        }
        return isMultiEnabled && isMatchSuccess;
    }

    public void filterResolveInfo(Context context, Intent intent, int userId, String resolvedType, List<ResolveInfo> resolveInfoList) {
        if (context == null || intent == null || resolveInfoList == null) {
            Slog.d(TAG, "filterResolveInfo: Request parameters are null.");
            return;
        }
        if (IS_DEBUG) {
            Slog.d(TAG, "filterResolveInfo isAddPCAss: " + intent.getIntExtra(BROWSER_PACKAGE_NAME, 0));
        }
        if (intent.getIntExtra(BROWSER_PACKAGE_NAME, 0) == 1) {
            return;
        }
        if (intent.getData() == null || !"com.huawei.pcassistant".equals(intent.getData().getScheme())) {
            boolean isSystemApp = isSystemApp(context, "com.huawei.pcassistant", userId);
            if (!isMultiScreenCollaborationEnabled(context, null) && isSystemApp) {
                resolveInfoList.removeIf($$Lambda$HwResolverManager$VfL81NLUbvKHztdtKb2m66xlkI.INSTANCE);
            }
            if (resolvedType == null && isSystemApp) {
                resolveInfoList.removeIf($$Lambda$HwResolverManager$0LP_VYpi5QajDx78ImDBrBTqp8.INSTANCE);
                return;
            }
            return;
        }
        Slog.d(TAG, "filterResolveInfo: Pull up the screen window by browser sweep");
    }

    public void setVersionMatchFlag(int deviceType, int version, boolean isMatchSuccess) {
        Slog.d(TAG, "setVersionMatchFlag deviceType: " + deviceType + ", version: " + version + ", isMatchSuccess: " + isMatchSuccess);
        if (!this.mVersionMatchMap.containsKey(Integer.valueOf(deviceType))) {
            this.mVersionMatchMap.put(Integer.valueOf(deviceType), new HashMap<>());
        }
        this.mVersionMatchMap.get(Integer.valueOf(deviceType)).put(Integer.valueOf(version), Boolean.valueOf(isMatchSuccess));
    }

    public boolean getVersionMatchFlag(int deviceType, int version) {
        if (this.mVersionMatchMap.isEmpty()) {
            if (IS_DEBUG) {
                Slog.d(TAG, "getVersionMatchFlag: mVersionMatchMap is empty.");
            }
            return false;
        }
        Map<Integer, Boolean> versionMap = this.mVersionMatchMap.get(Integer.valueOf(deviceType));
        if (versionMap == null) {
            if (IS_DEBUG) {
                Slog.d(TAG, "getVersionMatchFlag: versionMap is null.");
            }
            return false;
        } else if (versionMap.get(Integer.valueOf(version)) == null) {
            return false;
        } else {
            return versionMap.get(Integer.valueOf(version)).booleanValue();
        }
    }

    public void setOpenFileResult(Intent intent, int retCode) {
        if (intent == null && retCode == CREATE_GROUP_FAILED) {
            Slog.d(TAG, "setOpenFileResult: ZDFS exception, don't support file continuation.");
            setVersionMatchFlag(1, 1, false);
        } else if (intent == null) {
            Slog.d(TAG, "setOpenFileResult: Intent is null.");
        } else {
            this.mResolveTypeMap.put(intent.getType(), Integer.valueOf(retCode));
        }
    }

    public int getOpenFileResult(Intent intent) {
        if (intent == null) {
            Slog.d(TAG, "getOpenFileResult: Intent is null.");
            return 0;
        } else if (this.mResolveTypeMap.get(intent.getType()) != null) {
            return this.mResolveTypeMap.get(intent.getType()).intValue();
        } else {
            if (IS_DEBUG) {
                Slog.d(TAG, "getOpenFileResult resolveType: " + intent.getType());
            }
            return 0;
        }
    }

    private boolean skipChoosenBestActivity(Intent intent, List<ResolveInfo> list, String resolvedType, int userId, Set<String> tagFirstOpenFileTypeSet) {
        boolean isSkip = false;
        String tempResolvedType = getResolvedType(intent, resolvedType);
        if (tempResolvedType != null && !tagFirstOpenFileTypeSet.contains(resolvedType)) {
            isSkip = true;
            tagFirstOpenFileTypeSet.add(tempResolvedType);
        }
        if (IS_DEBUG) {
            Log.i(TAG, "skipChoosenBestActivity isSkip: " + isSkip);
        }
        return isSkip;
    }

    private boolean isSystemApp(Context context, String packageName, int userId) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Slog.w(TAG, "isSystemApp: Could not get PackageManager.");
            return false;
        }
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfoAsUser(packageName, 0, userId);
            if (appInfo == null || (appInfo.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "Could not found app: " + packageName);
        }
    }

    private PreferredActivity getPreferredActivityListInPcMode(int userId, String resolvedType) {
        if (resolvedType == null) {
            Slog.d(TAG, "getPreferredActivityListInPcMode: resolvedType is null.");
            return null;
        }
        Map<String, PreferredActivity> resolvedType2PreActivities = this.mPreferredActivityCacheInPcMode.get(Integer.valueOf(userId));
        if (resolvedType2PreActivities != null && !resolvedType2PreActivities.isEmpty()) {
            return resolvedType2PreActivities.get(resolvedType);
        }
        Slog.d(TAG, "getPreferredActivityListInPcMode: resolvedType2PreActivities is null.");
        return null;
    }

    private String getResolvedType(Intent intent, String resolvedType) {
        Uri data = intent.getData();
        if (resolvedType != null || data == null) {
            return resolvedType;
        }
        if ("http".equals(data.getScheme()) || "https".equals(data.getScheme())) {
            return BROWSER_PACKAGE_NAME;
        }
        return resolvedType;
    }

    private void addPreferredActivity(List<ResolveInfo> querys, String resolvedType, Intent intent, String pkgName, int userId) {
        ResolveInfo pkginfo;
        IntentFilter filter = new IntentFilter();
        if (intent.getAction() != null) {
            filter.addAction(intent.getAction());
        }
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            for (String cat : categories) {
                filter.addCategory(cat);
            }
        }
        filter.addCategory("android.intent.category.DEFAULT");
        if (resolvedType != null) {
            try {
                filter.addDataType(resolvedType);
            } catch (IntentFilter.MalformedMimeTypeException e) {
                Log.w(TAG, "addPreferredActivity : IntentFilter.MalformedMimeTypeException");
            }
        }
        addDataScheme(intent, filter);
        int size = querys.size();
        ComponentName[] sets = new ComponentName[size];
        int bestMatch = 0;
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = querys.get(i);
            sets[i] = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            if (resolveInfo.match > bestMatch) {
                bestMatch = resolveInfo.match;
            }
        }
        Iterator<ResolveInfo> it = querys.iterator();
        while (true) {
            if (!it.hasNext()) {
                pkginfo = null;
                break;
            }
            ResolveInfo info = it.next();
            if (info.activityInfo != null) {
                if (pkgName.equals(info.activityInfo.packageName)) {
                    pkginfo = info;
                    break;
                }
            }
        }
        if (pkginfo != null) {
            Intent tempintent = new Intent(intent);
            ActivityInfo ai = pkginfo.activityInfo;
            tempintent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            putPreferredActivityInPcMode(userId, filter, new PreferredActivity(filter, bestMatch, sets, tempintent.getComponent(), true));
        }
    }

    private void addDataScheme(Intent intent, IntentFilter filter) {
        Uri data = intent.getData();
        if (data != null && data.getScheme() != null) {
            if ("http".equals(data.getScheme()) || "https".equals(data.getScheme())) {
                filter.addDataScheme("http");
                filter.addDataScheme("https");
                return;
            }
            filter.addDataScheme(data.getScheme());
        }
    }
}

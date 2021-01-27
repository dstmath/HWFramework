package com.huawei.securitycenter;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import com.huawei.permission.HwSystemManager;
import com.huawei.securitycenter.AppPermissionUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HwPermissionManagerAdapter {
    private static final String CHECK_HW_PERM_INFO = "checkHwPerm";
    private static final long COMPENSATE_CODE = 4294967295L;
    private static final String EXTRA_CODE = "opCode";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_MONITOR = "shouldMonitor";
    private static final String EXTRA_PACKAGE = "packageName";
    private static final String EXTRA_RESULT = "result";
    private static final long LOAD_TIMEOUT = 60000;
    private static final int MODE_UNKNOWN = 3;
    private static final int MONITOR = 1;
    private static final String NAME_KEY = "name_key";
    private static final String OPERATION_KEY = "operation_key";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PERM_TYPE_KEY = "perm_type_key";
    private static final int RESULT_OK = 0;
    private static final String RETURN_RESULT_KEY = "return_result_key";
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String SET_HW_PERM_INFO = "setHwPermission";
    private static final long START_TIME = 0;
    private static final String TAG = "HwPermissionManagerAdapter";

    public static int setMode(int code, String packageName, int mode) throws RemoteException {
        Bundle params = new Bundle();
        params.putInt(EXTRA_CODE, code);
        params.putString("packageName", packageName);
        params.putInt(EXTRA_MODE, mode);
        Bundle result = HwSystemManager.callHsmService("setMode", params);
        if (result != null) {
            return result.getInt(EXTRA_RESULT, 0);
        }
        throw new RemoteException("Exception in service.");
    }

    public static int getMode(int code, String packageName) throws RemoteException {
        int userId = UserHandle.getUserId(Binder.getCallingUid());
        long longCode = ((long) code) & COMPENSATE_CODE;
        Bundle bundle = new Bundle();
        bundle.putString(NAME_KEY, CHECK_HW_PERM_INFO);
        HwPermissionManager hwPermissionManager = HwPermissionManager.getInstance();
        if (hwPermissionManager == null) {
            Log.e(TAG, "getMode hwPermissionManager is null");
            return 3;
        }
        Bundle dbPermissionItem = hwPermissionManager.getHwPermissionInfo(packageName, userId, longCode, bundle);
        if (dbPermissionItem != null) {
            return dbPermissionItem.getInt(RETURN_RESULT_KEY);
        }
        Log.e(TAG, "getMode dbPermissionItem is null");
        return 3;
    }

    public static boolean systemFixed(String pkgName) throws RemoteException {
        Bundle params = new Bundle();
        params.putString("packageName", pkgName);
        Bundle result = HwSystemManager.callHsmService("checkShoudMonitor", params);
        if (result != null) {
            return result.getInt(EXTRA_MONITOR) != 1;
        }
        throw new RemoteException("Exception in service.");
    }

    public static Map<String, Bundle> getPermissionUseHistory(Context context, AppOpsManager aom, ArrayList<String> permNameList, long duration) {
        if (context == null || aom == null || permNameList == null || permNameList.isEmpty()) {
            Log.i(TAG, "getPermissionUseHistory param is invalid.");
            return null;
        }
        ArrayList<String> opsList = getOpsList(permNameList);
        AtomicReference<AppOpsManager.HistoricalOps> historicalOpsRef = new AtomicReference<>();
        long filterBeginTimeMillis = System.currentTimeMillis() - duration;
        if (duration <= 0) {
            filterBeginTimeMillis = 0;
        }
        long filterEndTimeMillis = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(1);
        aom.getHistoricalOps(new AppOpsManager.HistoricalOpsRequest.Builder(filterBeginTimeMillis, filterEndTimeMillis).setOpNames(opsList).setFlags(13).build(), $$Lambda$_14QHG018Z6p13d3hzJuGTWnNeo.INSTANCE, new Consumer(historicalOpsRef, latch) {
            /* class com.huawei.securitycenter.$$Lambda$HwPermissionManagerAdapter$s5VLdD96ROazDIDHgmiS7Qrm7ng */
            private final /* synthetic */ AtomicReference f$0;
            private final /* synthetic */ CountDownLatch f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                HwPermissionManagerAdapter.lambda$getPermissionUseHistory$0(this.f$0, this.f$1, (AppOpsManager.HistoricalOps) obj);
            }
        });
        try {
            latch.await(LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Log.e(TAG, "InterruptedException " + ignored.getMessage());
        }
        return getHistoryBundleMap(context, aom, (AppOpsManager.HistoricalOps) historicalOpsRef.get(), permNameList);
    }

    static /* synthetic */ void lambda$getPermissionUseHistory$0(AtomicReference historicalOpsRef, CountDownLatch latch, AppOpsManager.HistoricalOps ops) {
        historicalOpsRef.set(ops);
        latch.countDown();
    }

    private static HashMap<String, Bundle> getHistoryBundleMap(Context context, AppOpsManager aom, AppOpsManager.HistoricalOps historicalOps, ArrayList<String> permNameList) {
        boolean isPermNameListEmpty = permNameList == null || permNameList.isEmpty();
        if (context == null || aom == null || historicalOps == null || isPermNameListEmpty) {
            Log.i(TAG, "getHistoryBundleMap param is invalid.");
            return null;
        }
        ArrayMap<Pair<Integer, String>, AppPermissionUsage.Builder> builderArrayMap = getPermUsageBuilders(context, permNameList);
        if (builderArrayMap == null || builderArrayMap.isEmpty()) {
            Log.i(TAG, "the builder of app permission usage map is empty.");
            return null;
        }
        ArrayMap<Pair<Integer, String>, AppOpsManager.PackageOps> lastUsageMap = getLastUsagesMap(aom, getOpsList(permNameList), builderArrayMap.size());
        ArrayMap<Pair<Integer, String>, AppOpsManager.HistoricalPackageOps> historicalPackageOpsMap = getHistoricalOpsMap(historicalOps, builderArrayMap.size());
        if (historicalPackageOpsMap == null || historicalPackageOpsMap.isEmpty()) {
            Log.i(TAG, "getHistoryBundleMap returns null due to the historical package ops map is empty.");
            return null;
        }
        Log.d(TAG, "getHistoryBundleMap History pkgOps size = " + historicalPackageOpsMap.size());
        int builderSize = builderArrayMap.size();
        HashMap<String, Bundle> historyMap = new HashMap<>(builderSize);
        for (int i = 0; i < builderSize; i++) {
            Pair<Integer, String> key = builderArrayMap.keyAt(i);
            AppPermissionUsage.Builder usageBuilder = builderArrayMap.valueAt(i);
            AppOpsManager.PackageOps lastUsage = null;
            if (lastUsageMap != null) {
                lastUsage = lastUsageMap.get(key);
            }
            usageBuilder.setLastUsage(lastUsage);
            usageBuilder.setHistoricalUsage(historicalPackageOpsMap.get(key));
            AppPermissionUsage appPermissionUsage = usageBuilder.build();
            if (appPermissionUsage == null) {
                Log.i(TAG, "getHistoryBundleMap AppPermissionUsage is null for " + usageBuilder.getPackageName());
            } else {
                Bundle historyData = appPermissionUsage.getPermissionUsageForApp();
                if (historyData != null) {
                    historyMap.put(appPermissionUsage.getPackageName(), historyData);
                }
            }
        }
        return historyMap;
    }

    private static ArrayMap<Pair<Integer, String>, AppOpsManager.PackageOps> getLastUsagesMap(AppOpsManager aom, ArrayList<String> opsList, int size) {
        if (aom == null || opsList == null || opsList.isEmpty()) {
            Log.e(TAG, "getLastUsagesMap param is invalid!");
            return null;
        }
        ArrayMap<Pair<Integer, String>, AppOpsManager.PackageOps> lastUsages = new ArrayMap<>(size);
        List<AppOpsManager.PackageOps> usageOps = aom.getPackagesForOps(getOpsArray(opsList));
        if (usageOps != null && !usageOps.isEmpty()) {
            int usageOpsCount = usageOps.size();
            for (int i = 0; i < usageOpsCount; i++) {
                AppOpsManager.PackageOps usageOp = usageOps.get(i);
                lastUsages.put(Pair.create(Integer.valueOf(usageOp.getUid()), usageOp.getPackageName()), usageOp);
            }
        }
        return lastUsages;
    }

    private static ArrayList<String> getOpsList(ArrayList<String> permList) {
        int permSize = permList.size();
        ArrayList<String> opsList = new ArrayList<>(permSize);
        for (int i = 0; i < permSize; i++) {
            String op = AppOpsManager.permissionToOp(permList.get(i));
            if (!TextUtils.isEmpty(op)) {
                opsList.add(op);
            }
        }
        return opsList;
    }

    private static String[] getOpsArray(ArrayList<String> opsList) {
        int size = opsList.size();
        String[] opsArray = new String[size];
        for (int i = 0; i < size; i++) {
            opsArray[i] = opsList.get(i);
        }
        return opsArray;
    }

    private static ArrayMap<Pair<Integer, String>, AppOpsManager.HistoricalPackageOps> getHistoricalOpsMap(AppOpsManager.HistoricalOps historyOps, int size) {
        if (historyOps == null || size <= 0) {
            Log.e(TAG, "getHistoricalOpsMap history op is null!");
            return null;
        }
        ArrayMap<Pair<Integer, String>, AppOpsManager.HistoricalPackageOps> packageOpsArrayMap = new ArrayMap<>(size);
        int uidCount = historyOps.getUidCount();
        for (int i = 0; i < uidCount; i++) {
            AppOpsManager.HistoricalUidOps uidOps = historyOps.getUidOpsAt(i);
            int packageCount = uidOps.getPackageCount();
            for (int j = 0; j < packageCount; j++) {
                AppOpsManager.HistoricalPackageOps packageOps = uidOps.getPackageOpsAt(j);
                packageOpsArrayMap.put(Pair.create(Integer.valueOf(uidOps.getUid()), packageOps.getPackageName()), packageOps);
            }
        }
        return packageOpsArrayMap;
    }

    private static ArrayMap<Pair<Integer, String>, AppPermissionUsage.Builder> getPermUsageBuilders(Context context, ArrayList<String> permList) {
        if (context == null || permList == null || permList.isEmpty()) {
            Log.e(TAG, "getPermUsageBuilders param is invalid!");
            return null;
        }
        List<PackageInfo> packageInfoList = getPackageInfoList(context, 790528);
        if (packageInfoList == null || packageInfoList.isEmpty()) {
            Log.e(TAG, "getPermUsageBuilders the package info list is null!");
            return null;
        }
        int apkSize = packageInfoList.size();
        ArrayMap<Pair<Integer, String>, AppPermissionUsage.Builder> usageBuilders = new ArrayMap<>(apkSize);
        for (int i = 0; i < apkSize; i++) {
            PackageInfo packageInfo = packageInfoList.get(i);
            if (packageInfo != null && !TextUtils.isEmpty(packageInfo.packageName)) {
                String[] applyPerms = packageInfo.requestedPermissions;
                if (applyPerms == null || applyPerms.length == 0) {
                    Log.i(TAG, "getPermUsageBuilders pkg " + packageInfo.packageName + " not any perm");
                } else {
                    Pair<Integer, String> usageKey = Pair.create(Integer.valueOf(packageInfo.applicationInfo.uid), packageInfo.packageName);
                    if (usageBuilders.get(usageKey) == null) {
                        AppPermissionUsage.Builder builder = new AppPermissionUsage.Builder(packageInfo.packageName, packageInfo.applicationInfo.uid);
                        ArraySet<String> controlledPerms = new ArraySet<>(applyPerms.length);
                        for (String perm : applyPerms) {
                            if (permList.contains(perm)) {
                                controlledPerms.add(perm);
                            }
                        }
                        if (!controlledPerms.isEmpty()) {
                            builder.addPermissionNames(controlledPerms);
                            usageBuilders.put(usageKey, builder);
                        }
                    }
                }
            }
        }
        return usageBuilders;
    }

    private static List<PackageInfo> getPackageInfoList(Context context, int flag) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "getPackageInfoList pm is null!");
            return null;
        }
        try {
            return pm.getInstalledPackages(flag);
        } catch (Exception e) {
            Log.e(TAG, "getPackageInfoList exception");
            return null;
        }
    }
}

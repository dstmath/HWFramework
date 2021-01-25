package com.huawei.server.pc.whiltestrategy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaDataAppStrategy implements AppStrategy {
    private static final String META_DATA_NAME = "supporthwpc";
    private static final String TAG = "MetaDataAppStrategy";

    @Override // com.huawei.server.pc.whiltestrategy.AppStrategy
    public Map<String, Integer> getAppList(Context context) {
        return getAllAppList(context);
    }

    @Override // com.huawei.server.pc.whiltestrategy.AppStrategy
    public int getAppState(String packageName, Context context) {
        if (context == null || packageName == null) {
            return -1;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 128);
            if (!(info == null || info.packageName == null || info.metaData == null)) {
                int state = info.metaData.getInt(META_DATA_NAME, 0);
                if (!(state == 0 || state == 1)) {
                    if (state != -1) {
                        Log.e(TAG, "meta data value is illegal!");
                    }
                }
                return state;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getAppState NameNotFoundException");
        }
        return 0;
    }

    private List<ResolveInfo> getAllResolveInfos(Context context) {
        Intent appIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        appIntent.addCategory("android.intent.category.LAUNCHER");
        return context.getPackageManager().queryIntentActivities(appIntent, 0);
    }

    private Map<String, Integer> getAllAppList(Context context) {
        List<ResolveInfo> resolveInfos = getAllResolveInfos(context);
        Map<String, Integer> allAppSupportPCState = new HashMap<>();
        int length = resolveInfos.size();
        for (int i = 0; i < length; i++) {
            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(resolveInfos.get(i).activityInfo.packageName, 128);
                if (!(appInfo == null || appInfo.packageName == null)) {
                    if (appInfo.metaData != null) {
                        int state = appInfo.metaData.getInt(META_DATA_NAME, 0);
                        if (!(state == 0 || state == 1)) {
                            if (state != -1) {
                                allAppSupportPCState.put(appInfo.packageName, 0);
                            }
                        }
                        allAppSupportPCState.put(appInfo.packageName, Integer.valueOf(state));
                    } else {
                        allAppSupportPCState.put(appInfo.packageName, 0);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "get application erro!");
            }
        }
        return allAppSupportPCState;
    }
}

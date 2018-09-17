package com.android.server.pc.whiltestrategy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaDataAppStrategy implements AppStrategy {
    private static final String META_DATA_NAME = "supporthwpc";
    private static final String TAG = "MetaDataAppStrategy";

    public Map<String, Integer> getAppList(Context context) {
        return getAllAppList(context);
    }

    public int getAppState(String packageName, Context context) {
        if (context == null || packageName == null) {
            return -1;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 128);
            if (!(info == null || info.packageName == null || info.metaData == null)) {
                int state = info.metaData.getInt(META_DATA_NAME, 0);
                if (state == 0 || state == 1 || state == -1) {
                    return state;
                }
                Log.e(TAG, "meta data value is illegal!");
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getAppState NameNotFoundException", e);
        }
        return 0;
    }

    private List<ResolveInfo> getAllResolveInfos(Context context) {
        Intent AppIntent = new Intent("android.intent.action.MAIN", null);
        AppIntent.addCategory("android.intent.category.LAUNCHER");
        return context.getPackageManager().queryIntentActivities(AppIntent, 0);
    }

    private Map<String, Integer> getAllAppList(Context context) {
        List<ResolveInfo> resolveInfos = getAllResolveInfos(context);
        Map<String, Integer> allAppSupportPCState = new HashMap();
        int length = resolveInfos.size();
        for (int i = 0; i < length; i++) {
            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(((ResolveInfo) resolveInfos.get(i)).activityInfo.packageName, 128);
                if (!(appInfo == null || appInfo.packageName == null)) {
                    if (appInfo.metaData != null) {
                        int state = appInfo.metaData.getInt(META_DATA_NAME, 0);
                        if (state == 0 || state == 1 || state == -1) {
                            allAppSupportPCState.put(appInfo.packageName, Integer.valueOf(state));
                        } else {
                            allAppSupportPCState.put(appInfo.packageName, Integer.valueOf(0));
                        }
                    } else {
                        allAppSupportPCState.put(appInfo.packageName, Integer.valueOf(0));
                    }
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "get application erro!");
            }
        }
        return allAppSupportPCState;
    }
}

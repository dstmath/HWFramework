package com.huawei.hsm;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.hsm.HwSystemManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import com.huawei.hsm.permission.PermissionManager;
import huawei.android.security.HsmDefaultImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HsmInterfaceImpl extends HsmDefaultImpl {
    private static final int RESTRICTED_NUM = 2;
    private static final String TAG = HsmInterfaceImpl.class.getSimpleName();
    private static HwSystemManager.HsmInterface sInstance = new HsmInterfaceImpl();

    private HsmInterfaceImpl() {
    }

    public static HwSystemManager.HsmInterface getDefault() {
        return sInstance;
    }

    public boolean canStartActivity(Context context, Intent intent) {
        return PermissionManager.canStartActivity(context, intent);
    }

    public boolean canSendBroadcast(Context context, Intent intent) {
        return PermissionManager.canSendBroadcast(context, intent);
    }

    public void insertSendBroadcastRecord(String pkgName, String action, int uid) {
        PermissionManager.insertSendBroadcastRecord(pkgName, action, uid);
    }

    public boolean allowOp(Uri uri, int action) {
        return PermissionManager.allowOp(uri, action);
    }

    public boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
        return PermissionManager.allowOp(destAddr, smsBody, sentIntent);
    }

    public boolean allowOp(String destAddr, String smsBody, List<PendingIntent> sentIntents) {
        return PermissionManager.allowOp(destAddr, smsBody, sentIntents);
    }

    public void authenticateSmsSend(HwSystemManager.Notifier callback, int callingUid, int smsId, String smsBody, String smsAddress) {
        PermissionManager.authenticateSmsSend(callback, callingUid, smsId, smsBody, smsAddress);
    }

    public void notifyBackgroundMgr(String pkgName, int pid, int uidOf3RdApk, int permType, int permCfg) {
    }

    public boolean allowOp(int type) {
        return PermissionManager.allowOp(type);
    }

    public boolean allowOp(Context cxt, int type) {
        return PermissionManager.allowOp(cxt, type);
    }

    public boolean allowOp(Context cxt, int type, boolean enable) {
        return PermissionManager.allowOp(cxt, type, enable);
    }

    public Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return PermissionManager.getDummyCursor(resolver, uri, projection, selection, selectionArgs, sortOrder);
    }

    public Location getFakeLocation(String name) {
        return PermissionManager.getFakeLocation(name);
    }

    public void setOutputFile(MediaRecorder recorder) throws IllegalStateException, IOException {
        PermissionManager.setOutputFile(recorder);
    }

    public boolean shouldInterceptAudience(String[] people, String pkgName) {
        return false;
    }

    public List<PackageInfo> getFakePackages(List<PackageInfo> installedList) {
        ArrayList arrayList = new ArrayList();
        Iterator<PackageInfo> iter = installedList.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PackageInfo packageInfo = iter.next();
            if (isSameUid(packageInfo.applicationInfo)) {
                arrayList.add(packageInfo);
                break;
            }
        }
        String str = TAG;
        Log.d(str, "List " + getListStr(arrayList));
        return arrayList;
    }

    public List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> installedList) {
        ArrayList arrayList = new ArrayList();
        Iterator<ApplicationInfo> iter = installedList.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            ApplicationInfo applicationInfo = iter.next();
            if (isSameUid(applicationInfo)) {
                arrayList.add(applicationInfo);
                break;
            }
        }
        String str = TAG;
        Log.d(str, "List " + getListStr(arrayList));
        return arrayList;
    }

    public List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> originalList) {
        if (originalList == null) {
            return originalList;
        }
        ArrayList arrayList = new ArrayList();
        int num = 0;
        for (ResolveInfo resolveInfo : originalList) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                if (isSameUid(activityInfo.applicationInfo)) {
                    arrayList.add(resolveInfo);
                } else if (num < 2) {
                    arrayList.add(resolveInfo);
                    num++;
                }
            }
        }
        String str = TAG;
        Log.d(str, "List " + getListStr(arrayList));
        return arrayList;
    }

    private boolean isSameUid(ApplicationInfo applicationInfo) {
        if (applicationInfo == null || applicationInfo.uid != Binder.getCallingUid()) {
            return false;
        }
        return true;
    }

    private <T> String getListStr(List<T> list) {
        if (list == null) {
            return "NULL";
        }
        return Arrays.toString(list.toArray());
    }
}

package android.hsm;

import android.app.PendingIntent;
import android.common.HwFrameworkSecurityPartsFactory;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwSystemManager {
    public static final int ACTION_PTIVATE_DELETE = 3;
    public static final int ACTION_PTIVATE_NONE = 0;
    public static final int ACTION_PTIVATE_READ = 1;
    public static final int ACTION_PTIVATE_WRITE = 2;
    public static final int AUDIO_SERVICE_NOTIFY = 1;
    public static final int BACKGROUND_POLICY_CLOSE = 0;
    public static final int BACKGROUND_POLICY_OPEN = 1;
    public static final int CAMARA_SERVICE_NOTIFY = 0;
    public static final int CMD_ADD = 0;
    public static final int CMD_CHECK = 2;
    public static final int CMD_REMOVE = 1;
    public static final int LOCATION_SERVICE_NOTIFY = 2;
    public static final int PERMISSION_BLUETOOTH = 8388608;
    public static final int PERMISSION_CAMERA = 1024;
    public static final int PERMISSION_EDIT_SHORTCUT = 16777216;
    public static final int PERMISSION_GET_APP_LIST = 33554432;
    public static final int PERMISSION_LOCATION = 8;
    public static final int PERMISSION_MOBILEDATE = 4194304;
    public static final int PERMISSION_RECORD = 128;
    public static final int PERMISSION_WIFI = 2097152;
    public static final int PRIVATE_FLAG_COMMON_SHOW_DIALOG = 1024;
    public static final int RHD_PERMISSION_CODE = 134217728;
    public static final int RMD_PERMISSION_CODE = 67108864;
    private static final String TAG = HwSystemManager.class.getSimpleName();
    public static final int mPermissionEnabled = SystemProperties.getInt("ro.config.hw_rightsmgr", 1);
    private static HsmInterface sInstance = null;

    public interface HsmInterface {
        boolean allowOp(int i);

        boolean allowOp(Context context, int i);

        boolean allowOp(Context context, int i, boolean z);

        boolean allowOp(Uri uri, int i);

        boolean allowOp(String str, String str2, PendingIntent pendingIntent);

        boolean allowOp(String str, String str2, List<PendingIntent> list);

        void authenticateSmsSend(Notifier notifier, int i, int i2, String str, String str2);

        boolean canSendBroadcast(Context context, Intent intent);

        boolean canStartActivity(Context context, Intent intent);

        Cursor getDummyCursor(ContentResolver contentResolver, Uri uri, String[] strArr, String str, String[] strArr2, String str2);

        List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> list);

        Location getFakeLocation(String str);

        List<PackageInfo> getFakePackages(List<PackageInfo> list);

        List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> list);

        void insertSendBroadcastRecord(String str, String str2, int i);

        void notifyBackgroundMgr(String str, int i, int i2, int i3, int i4);

        void setOutputFile(MediaRecorder mediaRecorder) throws IllegalStateException, IOException;

        boolean shouldInterceptAudience(String[] strArr, String str);
    }

    public interface Notifier {
        int notifyResult(Bundle bundle);
    }

    private HwSystemManager() {
    }

    private static HsmInterface getImplObject() {
        HsmInterface hsmInterface = sInstance;
        if (hsmInterface != null) {
            return hsmInterface;
        }
        sInstance = HwFrameworkSecurityPartsFactory.getInstance().getHwSystemManager();
        return sInstance;
    }

    public static boolean canStartActivity(Context context, Intent intent) {
        return getImplObject().canStartActivity(context, intent);
    }

    public static boolean canSendBroadcast(Context context, Intent intent) {
        return getImplObject().canSendBroadcast(context, intent);
    }

    public static boolean allowOp(Uri uri, int action) {
        return getImplObject().allowOp(uri, action);
    }

    public static boolean allowOp(int type) {
        return getImplObject().allowOp(type);
    }

    public static boolean allowOp(Context cxt, int type) {
        return getImplObject().allowOp(cxt, type);
    }

    public static boolean allowOp(Context cxt, int type, boolean enable) {
        return getImplObject().allowOp(cxt, type);
    }

    public static boolean allowOp(String destAddr, String smsBody, List<PendingIntent> sentIntents) {
        return getImplObject().allowOp(destAddr, smsBody, sentIntents);
    }

    public static boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
        return getImplObject().allowOp(destAddr, smsBody, sentIntent);
    }

    public static boolean allowOp(String destAddr, byte[] data, PendingIntent sentIntent) {
        try {
            return allowOp(destAddr, new String(data, "UTF-8"), sentIntent);
        } catch (UnsupportedEncodingException e) {
            return true;
        }
    }

    public static void authenticateSmsSend(Notifier callback, int callingUid, int smsId, String smsBody, String smsAddress) {
        getImplObject().authenticateSmsSend(callback, callingUid, smsId, smsBody, smsAddress);
    }

    public static void notifyBackgroundMgr(String pkgName, int pid, int uidOf3RdApk, int permType, int permCfg) {
        getImplObject().notifyBackgroundMgr(pkgName, pid, uidOf3RdApk, permType, permCfg);
    }

    public static ArrayList<ContentProviderOperation> getAllowedApplyBatchOp(String authority, ArrayList<ContentProviderOperation> operations) {
        ArrayList<ContentProviderOperation> allowedOperations = new ArrayList<>();
        Map<String, Map<Integer, Boolean>> authmap = new HashMap<>();
        Iterator<ContentProviderOperation> it = operations.iterator();
        while (it.hasNext()) {
            ContentProviderOperation operation = it.next();
            String authStr = operation.getUri().getAuthority();
            int type = operation.getType();
            int action = 0;
            if (type == 1 || type == 2) {
                action = 2;
            } else if (type == 3) {
                action = 3;
            } else if (type == 4) {
                action = 1;
            }
            if (authmap.containsKey(authStr)) {
                Map<Integer, Boolean> actionMap = authmap.get(authStr);
                if (actionMap.containsKey(Integer.valueOf(action))) {
                    if (actionMap.get(Integer.valueOf(action)).booleanValue()) {
                        allowedOperations.add(operation);
                    }
                } else if (allowOp(operation.getUri(), action)) {
                    actionMap.put(Integer.valueOf(action), true);
                    allowedOperations.add(operation);
                } else {
                    actionMap.put(Integer.valueOf(action), false);
                }
            } else {
                Map<Integer, Boolean> actionMap2 = new HashMap<>();
                if (allowOp(operation.getUri(), action)) {
                    actionMap2.put(Integer.valueOf(action), true);
                    allowedOperations.add(operation);
                } else {
                    actionMap2.put(Integer.valueOf(action), false);
                }
                authmap.put(authStr, actionMap2);
            }
        }
        return allowedOperations;
    }

    public static Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getImplObject().getDummyCursor(resolver, uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Location getFakeLocation(String name) {
        return getImplObject().getFakeLocation(name);
    }

    public static boolean checkWindowType(int flag) {
        return (flag & 1024) != 0;
    }

    public static void setOutputFile(MediaRecorder recorder) throws IllegalStateException, IOException {
        getImplObject().setOutputFile(recorder);
    }

    public static boolean shouldInterceptAudience(String[] people) {
        return getImplObject().shouldInterceptAudience(people, "");
    }

    public static boolean shouldInterceptAudience(String[] people, String pkgName) {
        return getImplObject().shouldInterceptAudience(people, pkgName);
    }

    public static List<PackageInfo> getFakePackages(List<PackageInfo> installedList) {
        return getImplObject().getFakePackages(installedList);
    }

    public static List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> installedList) {
        return getImplObject().getFakeApplications(installedList);
    }

    public static List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> originalList) {
        return getImplObject().getFakeResolveInfoList(originalList);
    }

    public static void insertSendBroadcastRecord(String pkgName, String action, int uid) {
        getImplObject().insertSendBroadcastRecord(pkgName, action, uid);
    }
}

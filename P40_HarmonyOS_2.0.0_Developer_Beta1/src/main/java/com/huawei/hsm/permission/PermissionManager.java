package com.huawei.hsm.permission;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hsm.HwSystemManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import java.io.IOException;
import java.util.List;

public class PermissionManager {
    private static final String TAG = PermissionManager.class.getSimpleName();

    public static boolean canStartActivity(Context context, Intent intent) {
        if (intent == null) {
            return true;
        }
        if (!(CallPermission.blockStartActivity(context, intent) || ConnectPermission.blockStartActivity(context, intent))) {
            return true;
        }
        return false;
    }

    public static boolean canSendBroadcast(Context context, Intent intent) {
        if (intent == null) {
            return true;
        }
        return new SendBroadcastPermission(context).allowSendBroadcast(intent);
    }

    public static void insertSendBroadcastRecord(String pkgName, String action, int uid) {
        new SendBroadcastPermission().insertSendBroadcastRecord(pkgName, action, uid);
    }

    public static boolean allowOp(Uri uri, int action) {
        return ContentPermission.allowContentOpInner(uri, action);
    }

    public static boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
        return !SmsPermission.getInstance().isSmsBlocked(destAddr, smsBody, sentIntent);
    }

    public static boolean allowOp(String destAddr, String smsBody, List<PendingIntent> sentIntents) {
        return !SmsPermission.getInstance().isSmsBlocked(destAddr, smsBody, sentIntents);
    }

    public static void authenticateSmsSend(HwSystemManager.Notifier callback, int callingUid, int smsId, String smsBody, String smsAddress) {
        SmsPermission.getInstance().authenticateSmsSend(callback, callingUid, smsId, smsBody, smsAddress);
    }

    public static boolean allowOp(int type) {
        if (16777216 == type) {
            return new PinShortcutPermission().allowOp();
        }
        return true;
    }

    public static boolean allowOp(Context cxt, int type) {
        if (8388608 == type) {
            return ConnectPermission.allowOpenBt(cxt);
        }
        if (4194304 == type) {
            return ConnectPermission.allowOpenMobile(cxt);
        }
        if (2097152 == type) {
            return ConnectPermission.allowOpenWifi(cxt);
        }
        return true;
    }

    public static boolean allowOp(Context cxt, int type, boolean enable) {
        if (!enable) {
            return true;
        }
        return allowOp(cxt, type);
    }

    public static Location getFakeLocation(String name) {
        return LocationPermission.getFakeLocation(name);
    }

    public static Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return ContentPermission.getDummyCursor(resolver, uri, projection, selection, selectionArgs, sortOrder);
    }

    public static void setOutputFile(MediaRecorder recorder) throws IllegalStateException, IOException {
    }
}

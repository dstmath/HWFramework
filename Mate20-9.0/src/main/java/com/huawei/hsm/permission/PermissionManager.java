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
import android.util.Slog;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PermissionManager {
    private static final String TAG = PermissionManager.class.getSimpleName();

    public static boolean canStartActivity(Context context, Intent intent) {
        boolean z = true;
        if (intent == null) {
            return true;
        }
        if (CallPermission.blockStartActivity(context, intent) || ConnectPermission.blockStartActivity(context, intent)) {
            z = false;
        }
        return z;
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

    public static void notifyBackgroundMgr(String pkgName, int pid, int uidOf3RdApk, int permType, int permCfg) {
        BackgroundPermManager.getInstance().notifyBackgroundMgr(pkgName, pid, uidOf3RdApk, permType, permCfg);
    }

    public static boolean allowOp(int type) {
        if (1024 == type) {
            CameraPermission cameraPermission = new CameraPermission();
            cameraPermission.remind();
            String str = TAG;
            Slog.i(str, "camera remind result:" + (!cameraPermission.isCameraBlocked));
            return true ^ cameraPermission.isCameraBlocked;
        } else if (128 == type) {
            return new AudioRecordPermission().remindWithResult();
        } else {
            if (33554432 == type) {
                return new AppListPermission().allowOp();
            }
            if (67108864 == type) {
                return new ReadMotionDataPermission().allowOp();
            }
            if (134217728 == type) {
                return new ReadHealthDataPermission().allowOp();
            }
            if (16777216 == type) {
                return new PinShortcutPermission().allowOp();
            }
            return true;
        }
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
        if (8 == type) {
            return true ^ new LocationPermission(cxt).isLocationBlocked();
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
        String str;
        Slog.d(TAG, "set put File null");
        FileOutputStream fos = new FileOutputStream("dev/null");
        try {
            recorder.setInterOutputFile(fos.getFD());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                str = "close output file fail";
                Slog.e(TAG, str);
            }
        }
    }
}

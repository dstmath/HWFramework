package com.huawei.hsm.permission;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.util.Log;
import java.util.List;

public class SmsPermission {
    private static final String DIVIDER_CHAR = ":";
    private static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    private static final String TAG = "SmsPermission";
    private static boolean isControl = SystemProperties.getBoolean("ro.config.hw_wirenetcontrol", false);
    private Context mContext = null;

    public boolean isMmsBlocked() {
        if (!isControl) {
            return false;
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (StubController.checkPreBlock(uid, 32)) {
            return true;
        }
        if (!StubController.checkPrecondition(uid) || !StubController.isGlobalSwitchOn(this.mContext, 32)) {
            return false;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(32, uid, pid, null);
        if (selectionResult != 0) {
            return 2 == selectionResult;
        } else {
            Log.e(TAG, "Get selection error");
            return false;
        }
    }

    public static boolean isSmsBlocked(String destAddr, String smsBody, PendingIntent sentIntent) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (StubController.checkPreBlock(uid, 32)) {
            return true;
        }
        if (!StubController.checkPrecondition(uid)) {
            return false;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(32, uid, pid, destAddr + DIVIDER_CHAR + smsBody);
        if (selectionResult == 0) {
            Log.e(TAG, "Get selection error");
            return false;
        } else if (2 != selectionResult) {
            return false;
        } else {
            sendFakeIntent(sentIntent);
            return true;
        }
    }

    public static boolean isSmsBlocked(String destAddr, String smsBody, List<PendingIntent> sentIntents) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (StubController.checkPreBlock(uid, 32)) {
            return true;
        }
        if (!StubController.checkPrecondition(uid)) {
            return false;
        }
        int selectionResult = StubController.holdForGetPermissionSelection(32, uid, pid, destAddr + DIVIDER_CHAR + smsBody);
        if (selectionResult == 0) {
            Log.e(TAG, "Get selection error");
            return false;
        } else if (2 != selectionResult) {
            return false;
        } else {
            sendFakeIntents(sentIntents);
            return true;
        }
    }

    private static void sendFakeIntents(List<PendingIntent> sentIntents) {
        if (sentIntents != null && !sentIntents.isEmpty()) {
            for (int i = 0; i < sentIntents.size(); i++) {
                sendFakeIntent((PendingIntent) sentIntents.get(i));
            }
        }
    }

    private static void sendFakeIntent(PendingIntent PI) {
        if (PI != null) {
            try {
                PI.send(1);
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }
    }
}

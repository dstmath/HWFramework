package com.huawei.hsm.permission;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.os.Binder;
import android.util.Log;
import java.util.List;

public class SmsPermission {
    private static final String DIVIDER_CHAR = ":";
    private static final int RESULT_ERROR_GENERIC_FAILURE = 1;
    private static final String TAG = "SmsPermission";
    private static boolean isControl;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.permission.SmsPermission.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.permission.SmsPermission.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.permission.SmsPermission.<clinit>():void");
    }

    public SmsPermission() {
        this.mContext = null;
    }

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
            for (int i = 0; i < sentIntents.size(); i += RESULT_ERROR_GENERIC_FAILURE) {
                sendFakeIntent((PendingIntent) sentIntents.get(i));
            }
        }
    }

    private static void sendFakeIntent(PendingIntent PI) {
        if (PI != null) {
            try {
                PI.send(RESULT_ERROR_GENERIC_FAILURE);
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }
    }
}

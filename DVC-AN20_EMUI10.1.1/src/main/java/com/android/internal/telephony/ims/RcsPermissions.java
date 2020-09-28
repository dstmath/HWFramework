package com.android.internal.telephony.ims;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;

/* access modifiers changed from: package-private */
public class RcsPermissions {
    RcsPermissions() {
    }

    static void checkReadPermissions(Context context, String callingPackage) {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        context.enforcePermission("android.permission.READ_SMS", pid, uid, null);
        checkOp(context, uid, callingPackage, 14);
    }

    static void checkWritePermissions(Context context, String callingPackage) {
        checkOp(context, Binder.getCallingUid(), callingPackage, 15);
    }

    private static void checkOp(Context context, int uid, String callingPackage, int op) {
        if (((AppOpsManager) context.getSystemService("appops")).noteOp(op, uid, callingPackage) != 0) {
            throw new SecurityException(AppOpsManager.opToName(op) + " not allowed for " + callingPackage);
        }
    }
}

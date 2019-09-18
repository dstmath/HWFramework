package com.android.server.timezone;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.UserHandle;
import com.android.internal.util.DumpUtils;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;
import java.util.concurrent.Executor;

final class RulesManagerServiceHelperImpl implements PermissionHelper, Executor, RulesManagerIntentHelper {
    private final Context mContext;

    RulesManagerServiceHelperImpl(Context context) {
        this.mContext = context;
    }

    public void enforceCallerHasPermission(String requiredPermission) {
        this.mContext.enforceCallingPermission(requiredPermission, null);
    }

    public boolean checkDumpPermission(String tag, PrintWriter pw) {
        return DumpUtils.checkDumpPermission(this.mContext, tag, pw);
    }

    public void execute(Runnable runnable) {
        AsyncTask.execute(runnable);
    }

    public void sendTimeZoneOperationStaged() {
        sendOperationIntent(true);
    }

    public void sendTimeZoneOperationUnstaged() {
        sendOperationIntent(false);
    }

    private void sendOperationIntent(boolean staged) {
        Intent intent = new Intent("com.android.intent.action.timezone.RULES_UPDATE_OPERATION");
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        intent.putExtra("staged", staged);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }
}

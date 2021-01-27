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

    @Override // com.android.server.timezone.PermissionHelper
    public void enforceCallerHasPermission(String requiredPermission) {
        this.mContext.enforceCallingPermission(requiredPermission, null);
    }

    @Override // com.android.server.timezone.PermissionHelper
    public boolean checkDumpPermission(String tag, PrintWriter pw) {
        return DumpUtils.checkDumpPermission(this.mContext, tag, pw);
    }

    @Override // java.util.concurrent.Executor
    public void execute(Runnable runnable) {
        AsyncTask.execute(runnable);
    }

    @Override // com.android.server.timezone.RulesManagerIntentHelper
    public void sendTimeZoneOperationStaged() {
        sendOperationIntent(true);
    }

    @Override // com.android.server.timezone.RulesManagerIntentHelper
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

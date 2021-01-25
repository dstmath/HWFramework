package com.huawei.server.security.privacyability;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;

class IDAnonymizationBroadcastReceiver {
    private static final int INVALID_UID = -1;
    private static final String TAG = "IDAnonymizationBroadcastReceiver";
    private static final int USER_NULL = -10000;
    private static final int USER_SYSTEM = 0;
    private final Context mContext;
    private final BroadcastReceiver mPackageBroadcastReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.security.privacyability.IDAnonymizationBroadcastReceiver.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction()) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                Uri uri = intent.getData();
                if (uri == null) {
                    Log.e(IDAnonymizationBroadcastReceiver.TAG, "analyze ACTION_PACKAGE_REMOVED error: uri is null.");
                    return;
                }
                String packageName = uri.getSchemeSpecificPart();
                if (packageName == null) {
                    Log.e(IDAnonymizationBroadcastReceiver.TAG, "analyze ACTION_PACKAGE_REMOVED error: packageName is null.");
                    return;
                }
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid == -1) {
                    Log.e(IDAnonymizationBroadcastReceiver.TAG, "do not find Intent.EXTRA_UID for the package.");
                    return;
                }
                int ret = IDAnonymizationDB.getInstance().removeCUID(UserHandleEx.getUserId(uid), packageName);
                if (ret != 0) {
                    Log.e(IDAnonymizationBroadcastReceiver.TAG, "remove cuid failed,ret = " + ret);
                }
            }
        }
    };
    private final BroadcastReceiver mUserRemoveReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.security.privacyability.IDAnonymizationBroadcastReceiver.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", IDAnonymizationBroadcastReceiver.USER_NULL);
                if (userId == IDAnonymizationBroadcastReceiver.USER_NULL) {
                    Log.e(IDAnonymizationBroadcastReceiver.TAG, "analyze ACTION_PACKAGE_REMOVED error: get userId failed.");
                } else if (userId == 0) {
                    Log.e(IDAnonymizationBroadcastReceiver.TAG, "error: system user is removed, it must be wrong.");
                } else {
                    IDAnonymizationDB.getInstance().removeUserAllData(userId);
                }
            }
        }
    };

    IDAnonymizationBroadcastReceiver(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public synchronized void onStart() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        ContextEx.registerReceiverAsUser(this.mContext, this.mPackageBroadcastReceiver, UserHandleEx.ALL, packageFilter, (String) null, (Handler) null);
        this.mContext.registerReceiver(this.mUserRemoveReceiver, new IntentFilter("android.intent.action.USER_REMOVED"));
        Log.i(TAG, "IDAnonymizationBroadcastReceiver started!");
    }

    /* access modifiers changed from: package-private */
    public synchronized void onStop() {
        this.mContext.unregisterReceiver(this.mPackageBroadcastReceiver);
        this.mContext.unregisterReceiver(this.mUserRemoveReceiver);
        Log.i(TAG, "IDAnonymizationBroadcastReceiver stopped!");
    }
}

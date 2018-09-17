package android.app.admin;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;

public class DeviceAdminReceiver extends BroadcastReceiver {
    public static final String ACTION_BUGREPORT_FAILED = "android.app.action.BUGREPORT_FAILED";
    public static final String ACTION_BUGREPORT_SHARE = "android.app.action.BUGREPORT_SHARE";
    public static final String ACTION_BUGREPORT_SHARING_DECLINED = "android.app.action.BUGREPORT_SHARING_DECLINED";
    public static final String ACTION_CHOOSE_PRIVATE_KEY_ALIAS = "android.app.action.CHOOSE_PRIVATE_KEY_ALIAS";
    public static final String ACTION_DEVICE_ADMIN_DISABLED = "android.app.action.DEVICE_ADMIN_DISABLED";
    public static final String ACTION_DEVICE_ADMIN_DISABLE_REQUESTED = "android.app.action.DEVICE_ADMIN_DISABLE_REQUESTED";
    public static final String ACTION_DEVICE_ADMIN_ENABLED = "android.app.action.DEVICE_ADMIN_ENABLED";
    public static final String ACTION_LOCK_TASK_ENTERING = "android.app.action.LOCK_TASK_ENTERING";
    public static final String ACTION_LOCK_TASK_EXITING = "android.app.action.LOCK_TASK_EXITING";
    public static final String ACTION_NETWORK_LOGS_AVAILABLE = "android.app.action.NETWORK_LOGS_AVAILABLE";
    public static final String ACTION_NOTIFY_PENDING_SYSTEM_UPDATE = "android.app.action.NOTIFY_PENDING_SYSTEM_UPDATE";
    public static final String ACTION_PASSWORD_CHANGED = "android.app.action.ACTION_PASSWORD_CHANGED";
    public static final String ACTION_PASSWORD_EXPIRING = "android.app.action.ACTION_PASSWORD_EXPIRING";
    public static final String ACTION_PASSWORD_FAILED = "android.app.action.ACTION_PASSWORD_FAILED";
    public static final String ACTION_PASSWORD_SUCCEEDED = "android.app.action.ACTION_PASSWORD_SUCCEEDED";
    public static final String ACTION_PROFILE_PROVISIONING_COMPLETE = "android.app.action.PROFILE_PROVISIONING_COMPLETE";
    public static final String ACTION_SECURITY_LOGS_AVAILABLE = "android.app.action.SECURITY_LOGS_AVAILABLE";
    public static final String ACTION_USER_ADDED = "android.app.action.USER_ADDED";
    public static final String ACTION_USER_REMOVED = "android.app.action.USER_REMOVED";
    public static final int BUGREPORT_FAILURE_FAILED_COMPLETING = 0;
    public static final int BUGREPORT_FAILURE_FILE_NO_LONGER_AVAILABLE = 1;
    public static final String DEVICE_ADMIN_META_DATA = "android.app.device_admin";
    public static final String EXTRA_BUGREPORT_FAILURE_REASON = "android.app.extra.BUGREPORT_FAILURE_REASON";
    public static final String EXTRA_BUGREPORT_HASH = "android.app.extra.BUGREPORT_HASH";
    public static final String EXTRA_CHOOSE_PRIVATE_KEY_ALIAS = "android.app.extra.CHOOSE_PRIVATE_KEY_ALIAS";
    public static final String EXTRA_CHOOSE_PRIVATE_KEY_RESPONSE = "android.app.extra.CHOOSE_PRIVATE_KEY_RESPONSE";
    public static final String EXTRA_CHOOSE_PRIVATE_KEY_SENDER_UID = "android.app.extra.CHOOSE_PRIVATE_KEY_SENDER_UID";
    public static final String EXTRA_CHOOSE_PRIVATE_KEY_URI = "android.app.extra.CHOOSE_PRIVATE_KEY_URI";
    public static final String EXTRA_DISABLE_WARNING = "android.app.extra.DISABLE_WARNING";
    public static final String EXTRA_LOCK_TASK_PACKAGE = "android.app.extra.LOCK_TASK_PACKAGE";
    public static final String EXTRA_NETWORK_LOGS_COUNT = "android.app.extra.EXTRA_NETWORK_LOGS_COUNT";
    public static final String EXTRA_NETWORK_LOGS_TOKEN = "android.app.extra.EXTRA_NETWORK_LOGS_TOKEN";
    public static final String EXTRA_SYSTEM_UPDATE_RECEIVED_TIME = "android.app.extra.SYSTEM_UPDATE_RECEIVED_TIME";
    private static String TAG = "DevicePolicy";
    private static boolean localLOGV = false;
    private DevicePolicyManager mManager;
    private ComponentName mWho;

    public DevicePolicyManager getManager(Context context) {
        if (this.mManager != null) {
            return this.mManager;
        }
        this.mManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return this.mManager;
    }

    public ComponentName getWho(Context context) {
        if (this.mWho != null) {
            return this.mWho;
        }
        this.mWho = new ComponentName(context, getClass());
        return this.mWho;
    }

    public void onEnabled(Context context, Intent intent) {
    }

    public CharSequence onDisableRequested(Context context, Intent intent) {
        return null;
    }

    public void onDisabled(Context context, Intent intent) {
    }

    @Deprecated
    public void onPasswordChanged(Context context, Intent intent) {
    }

    public void onPasswordChanged(Context context, Intent intent, UserHandle user) {
        onPasswordChanged(context, intent);
    }

    @Deprecated
    public void onPasswordFailed(Context context, Intent intent) {
    }

    public void onPasswordFailed(Context context, Intent intent, UserHandle user) {
        onPasswordFailed(context, intent);
    }

    @Deprecated
    public void onPasswordSucceeded(Context context, Intent intent) {
    }

    public void onPasswordSucceeded(Context context, Intent intent, UserHandle user) {
        onPasswordSucceeded(context, intent);
    }

    @Deprecated
    public void onPasswordExpiring(Context context, Intent intent) {
    }

    public void onPasswordExpiring(Context context, Intent intent, UserHandle user) {
        onPasswordExpiring(context, intent);
    }

    public void onProfileProvisioningComplete(Context context, Intent intent) {
    }

    @Deprecated
    public void onReadyForUserInitialization(Context context, Intent intent) {
    }

    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
    }

    public void onLockTaskModeExiting(Context context, Intent intent) {
    }

    public String onChoosePrivateKeyAlias(Context context, Intent intent, int uid, Uri uri, String alias) {
        return null;
    }

    public void onSystemUpdatePending(Context context, Intent intent, long receivedTime) {
    }

    public void onBugreportSharingDeclined(Context context, Intent intent) {
    }

    public void onBugreportShared(Context context, Intent intent, String bugreportHash) {
    }

    public void onBugreportFailed(Context context, Intent intent, int failureCode) {
    }

    public void onSecurityLogsAvailable(Context context, Intent intent) {
    }

    public void onNetworkLogsAvailable(Context context, Intent intent, long batchToken, int networkLogsCount) {
    }

    public void onUserAdded(Context context, Intent intent, UserHandle newUser) {
    }

    public void onUserRemoved(Context context, Intent intent, UserHandle removedUser) {
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Context context2;
        Intent intent2;
        if (ACTION_PASSWORD_CHANGED.equals(action)) {
            context2 = context;
            intent2 = intent;
            onPasswordChanged(context2, intent2, (UserHandle) intent.getParcelableExtra(Intent.EXTRA_USER));
        } else if (ACTION_PASSWORD_FAILED.equals(action)) {
            context2 = context;
            intent2 = intent;
            onPasswordFailed(context2, intent2, (UserHandle) intent.getParcelableExtra(Intent.EXTRA_USER));
        } else if (ACTION_PASSWORD_SUCCEEDED.equals(action)) {
            context2 = context;
            intent2 = intent;
            onPasswordSucceeded(context2, intent2, (UserHandle) intent.getParcelableExtra(Intent.EXTRA_USER));
        } else if (ACTION_DEVICE_ADMIN_ENABLED.equals(action)) {
            onEnabled(context, intent);
        } else if (ACTION_DEVICE_ADMIN_DISABLE_REQUESTED.equals(action)) {
            CharSequence res = onDisableRequested(context, intent);
            if (res != null) {
                getResultExtras(true).putCharSequence(EXTRA_DISABLE_WARNING, res);
            }
        } else if (ACTION_DEVICE_ADMIN_DISABLED.equals(action)) {
            onDisabled(context, intent);
        } else if (ACTION_PASSWORD_EXPIRING.equals(action)) {
            context2 = context;
            intent2 = intent;
            onPasswordExpiring(context2, intent2, (UserHandle) intent.getParcelableExtra(Intent.EXTRA_USER));
        } else if (ACTION_PROFILE_PROVISIONING_COMPLETE.equals(action)) {
            onProfileProvisioningComplete(context, intent);
        } else if (ACTION_CHOOSE_PRIVATE_KEY_ALIAS.equals(action)) {
            setResultData(onChoosePrivateKeyAlias(context, intent, intent.getIntExtra(EXTRA_CHOOSE_PRIVATE_KEY_SENDER_UID, -1), (Uri) intent.getParcelableExtra(EXTRA_CHOOSE_PRIVATE_KEY_URI), intent.getStringExtra(EXTRA_CHOOSE_PRIVATE_KEY_ALIAS)));
        } else if (ACTION_LOCK_TASK_ENTERING.equals(action)) {
            onLockTaskModeEntering(context, intent, intent.getStringExtra(EXTRA_LOCK_TASK_PACKAGE));
        } else if (ACTION_LOCK_TASK_EXITING.equals(action)) {
            onLockTaskModeExiting(context, intent);
        } else if (ACTION_NOTIFY_PENDING_SYSTEM_UPDATE.equals(action)) {
            onSystemUpdatePending(context, intent, intent.getLongExtra(EXTRA_SYSTEM_UPDATE_RECEIVED_TIME, -1));
        } else if (ACTION_BUGREPORT_SHARING_DECLINED.equals(action)) {
            onBugreportSharingDeclined(context, intent);
        } else if (ACTION_BUGREPORT_SHARE.equals(action)) {
            onBugreportShared(context, intent, intent.getStringExtra(EXTRA_BUGREPORT_HASH));
        } else if (ACTION_BUGREPORT_FAILED.equals(action)) {
            onBugreportFailed(context, intent, intent.getIntExtra(EXTRA_BUGREPORT_FAILURE_REASON, 0));
        } else if (ACTION_SECURITY_LOGS_AVAILABLE.equals(action)) {
            onSecurityLogsAvailable(context, intent);
        } else if (ACTION_NETWORK_LOGS_AVAILABLE.equals(action)) {
            Context context3 = context;
            Intent intent3 = intent;
            onNetworkLogsAvailable(context3, intent3, intent.getLongExtra(EXTRA_NETWORK_LOGS_TOKEN, -1), intent.getIntExtra(EXTRA_NETWORK_LOGS_COUNT, 0));
        } else if (ACTION_USER_ADDED.equals(action)) {
            context2 = context;
            intent2 = intent;
            onUserAdded(context2, intent2, (UserHandle) intent.getParcelableExtra(Intent.EXTRA_USER));
        } else if (ACTION_USER_REMOVED.equals(action)) {
            context2 = context;
            intent2 = intent;
            onUserRemoved(context2, intent2, (UserHandle) intent.getParcelableExtra(Intent.EXTRA_USER));
        }
    }
}

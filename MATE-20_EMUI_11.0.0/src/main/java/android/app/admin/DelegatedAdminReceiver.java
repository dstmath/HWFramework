package android.app.admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class DelegatedAdminReceiver extends BroadcastReceiver {
    private static final String TAG = "DelegatedAdminReceiver";

    public String onChoosePrivateKeyAlias(Context context, Intent intent, int uid, Uri uri, String alias) {
        throw new UnsupportedOperationException("onChoosePrivateKeyAlias should be implemented");
    }

    public void onNetworkLogsAvailable(Context context, Intent intent, long batchToken, int networkLogsCount) {
        throw new UnsupportedOperationException("onNetworkLogsAvailable should be implemented");
    }

    @Override // android.content.BroadcastReceiver
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DeviceAdminReceiver.ACTION_CHOOSE_PRIVATE_KEY_ALIAS.equals(action)) {
            setResultData(onChoosePrivateKeyAlias(context, intent, intent.getIntExtra(DeviceAdminReceiver.EXTRA_CHOOSE_PRIVATE_KEY_SENDER_UID, -1), (Uri) intent.getParcelableExtra(DeviceAdminReceiver.EXTRA_CHOOSE_PRIVATE_KEY_URI), intent.getStringExtra(DeviceAdminReceiver.EXTRA_CHOOSE_PRIVATE_KEY_ALIAS)));
        } else if (DeviceAdminReceiver.ACTION_NETWORK_LOGS_AVAILABLE.equals(action)) {
            onNetworkLogsAvailable(context, intent, intent.getLongExtra(DeviceAdminReceiver.EXTRA_NETWORK_LOGS_TOKEN, -1), intent.getIntExtra(DeviceAdminReceiver.EXTRA_NETWORK_LOGS_COUNT, 0));
        } else {
            Log.w(TAG, "Unhandled broadcast: " + action);
        }
    }
}

package android.service.restrictions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.provider.DocumentsContract;

public abstract class RestrictionsReceiver extends BroadcastReceiver {
    private static final String TAG = "RestrictionsReceiver";

    public abstract void onRequestPermission(Context context, String str, String str2, String str3, PersistableBundle persistableBundle);

    public void onReceive(Context context, Intent intent) {
        if ("android.content.action.REQUEST_PERMISSION".equals(intent.getAction())) {
            onRequestPermission(context, intent.getStringExtra(DocumentsContract.EXTRA_PACKAGE_NAME), intent.getStringExtra("android.content.extra.REQUEST_TYPE"), intent.getStringExtra("android.content.extra.REQUEST_ID"), (PersistableBundle) intent.getParcelableExtra("android.content.extra.REQUEST_BUNDLE"));
        }
    }
}

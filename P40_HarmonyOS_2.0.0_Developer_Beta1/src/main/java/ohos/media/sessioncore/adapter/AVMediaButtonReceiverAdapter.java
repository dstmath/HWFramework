package ohos.media.sessioncore.adapter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVMediaButtonReceiverAdapter extends BroadcastReceiver {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVMediaButtonReceiverAdapter.class);

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent("android.intent.action.MEDIA_BUTTON");
        intent2.setPackage(context.getPackageName());
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            LOGGER.error("Get package manager failed", new Object[0]);
            return;
        }
        List<ResolveInfo> queryIntentServices = packageManager.queryIntentServices(intent2, 0);
        if (queryIntentServices.size() != 1) {
            LOGGER.error("Expected only one service handles MEDIA_BUTTON, but found %{public}d", Integer.valueOf(queryIntentServices.size()));
            return;
        }
        ResolveInfo resolveInfo = queryIntentServices.get(0);
        intent.setComponent(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name));
        context.startForegroundService(intent);
    }
}

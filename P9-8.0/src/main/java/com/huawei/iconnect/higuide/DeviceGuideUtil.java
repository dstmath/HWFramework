package com.huawei.iconnect.higuide;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import java.util.List;

public class DeviceGuideUtil {
    private static final String DEVICEGUIDE_LAUNCH_ACTION = "com.huawei.iconnect.deviceguide.action.LAUNCH";
    private static final String TAG = "DeviceGuideUtil";

    public static void startDeviceGuide(Context context, String uri) {
        Log.i(TAG, "startDeviceGuide " + uri);
        Intent intent = new Intent();
        intent.setAction(DEVICEGUIDE_LAUNCH_ACTION);
        intent.setData(Uri.parse(uri));
        try {
            Intent launchIntent = createExplicitFromImplicitIntent(context, intent);
            if (launchIntent == null) {
                Log.i(TAG, "launchIntent is null");
            } else {
                context.startService(launchIntent);
            }
        } catch (Exception e) {
            Log.i(TAG, "exception in startDeviceGuide " + e.getMessage());
        }
    }

    public static void startDeviceGuide(Context context, Intent intent) {
        intent.setAction(DEVICEGUIDE_LAUNCH_ACTION);
        try {
            Intent launchIntent = createExplicitFromImplicitIntent(context, intent);
            if (launchIntent == null) {
                Log.i(TAG, "launchIntent is null");
            } else {
                context.startService(launchIntent);
            }
        } catch (Exception e) {
            Log.i(TAG, "exception in startDeviceGuide " + e.getMessage());
        }
    }

    private static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            Log.e(TAG, "resolveInfo not found");
            return null;
        }
        ResolveInfo serviceInfo = (ResolveInfo) resolveInfo.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}

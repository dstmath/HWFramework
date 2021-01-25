package com.android.server.usb;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.UserHandle;
import android.util.Slog;
import java.util.ArrayList;

class UsbHandlerManager {
    private static final String LOG_TAG = UsbHandlerManager.class.getSimpleName();
    private final Context mContext;

    UsbHandlerManager(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void showUsbAccessoryUriActivity(UsbAccessory accessory, UserHandle user) {
        String uri = accessory.getUri();
        if (uri != null && uri.length() > 0) {
            Intent dialogIntent = createDialogIntent();
            dialogIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbAccessoryUriActivity");
            dialogIntent.putExtra("accessory", accessory);
            dialogIntent.putExtra("uri", uri);
            try {
                this.mContext.startActivityAsUser(dialogIntent, user);
            } catch (ActivityNotFoundException e) {
                Slog.e(LOG_TAG, "unable to start UsbAccessoryUriActivity");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void confirmUsbHandler(ResolveInfo rInfo, UsbDevice device, UsbAccessory accessory) {
        Intent resolverIntent = createDialogIntent();
        resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbConfirmActivity");
        resolverIntent.putExtra("rinfo", rInfo);
        UserHandle user = UserHandle.getUserHandleForUid(rInfo.activityInfo.applicationInfo.uid);
        if (device != null) {
            resolverIntent.putExtra("device", device);
        } else {
            resolverIntent.putExtra("accessory", accessory);
        }
        try {
            this.mContext.startActivityAsUser(resolverIntent, user);
        } catch (ActivityNotFoundException e) {
            String str = LOG_TAG;
            Slog.e(str, "unable to start activity " + resolverIntent, e);
        }
    }

    /* access modifiers changed from: package-private */
    public void selectUsbHandler(ArrayList<ResolveInfo> matches, UserHandle user, Intent intent) {
        Intent resolverIntent = createDialogIntent();
        resolverIntent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbResolverActivity");
        resolverIntent.putParcelableArrayListExtra("rlist", matches);
        resolverIntent.putExtra("android.intent.extra.INTENT", intent);
        try {
            this.mContext.startActivityAsUser(resolverIntent, user);
        } catch (ActivityNotFoundException e) {
            String str = LOG_TAG;
            Slog.e(str, "unable to start activity " + resolverIntent, e);
        }
    }

    private Intent createDialogIntent() {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        return intent;
    }
}

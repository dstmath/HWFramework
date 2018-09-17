package com.android.server.storage;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.hwnotification.HwNotificationResource;
import android.os.UserHandle;
import android.util.EventLog;
import android.util.Slog;
import huawei.android.storage.HwCustDeviceStorageMonitorService;
import huawei.cust.HwCustUtils;

public class HwDeviceStorageMonitorService extends DeviceStorageMonitorService {
    private static final String ACTION_INTERNAL_STORAGE_SYSTEMMANAGER = "com.huawei.systemmanager.spacecleanner.LowerMemTipActivity";
    private static final String SYSTEMMANAGER_PACKAGENAME = "com.huawei.systemmanager";
    static final String TAG = "HwDeviceStorageMonitorService";
    Object mCust;

    public HwDeviceStorageMonitorService(Context context) {
        super(context);
        this.mCust = HwCustUtils.createObj(HwCustDeviceStorageMonitorService.class, new Object[]{this, this.mHandler});
    }

    public boolean checkSystemManagerApkExist() {
        try {
            getContext().getPackageManager().getApplicationInfo(SYSTEMMANAGER_PACKAGENAME, 8192);
            return true;
        } catch (NameNotFoundException e) {
            Slog.d(TAG, "Hw System Manager not install ,so use original notification");
            return false;
        }
    }

    public void sendNotificationHwSM(long freeMem, boolean isBootImageOnDisk, int notficationId, Intent storageLowIntent) {
        int i;
        Context context = getContext();
        EventLog.writeEvent(2745, freeMem);
        Slog.d(TAG, "HW Sending low memory notification, freeMem = " + freeMem);
        Intent lowMemIntent = new Intent().setComponent(new ComponentName(SYSTEMMANAGER_PACKAGENAME, ACTION_INTERNAL_STORAGE_SYSTEMMANAGER));
        lowMemIntent.putExtra("memory", freeMem);
        lowMemIntent.addFlags(268435456);
        NotificationManager mNotificationMgr = (NotificationManager) context.getSystemService("notification");
        CharSequence title = context.getText(33685823);
        if (isBootImageOnDisk) {
            i = 33685824;
        } else {
            i = 17040236;
        }
        CharSequence details = context.getText(i);
        Notification notification = new Builder(context).setSmallIcon(33751341).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), 33751341)).setTicker(title).setColor(context.getColor(17170519)).setContentTitle(title).setContentText(details).setContentIntent(PendingIntent.getActivityAsUser(context, 0, lowMemIntent, 0, null, UserHandle.CURRENT)).setStyle(new BigTextStyle().bigText(details)).setVisibility(1).setCategory("sys").setExtras(HwNotificationResource.getNotificationThemeData(33751342, -1, 2, 15)).build();
        notification.flags |= 32;
        mNotificationMgr.notifyAsUser(null, notficationId, notification, UserHandle.ALL);
        context.sendStickyBroadcastAsUser(storageLowIntent, UserHandle.ALL);
    }

    public HwCustDeviceStorageMonitorService getCust() {
        return (HwCustDeviceStorageMonitorService) this.mCust;
    }
}

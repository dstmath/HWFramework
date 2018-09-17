package com.android.server;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.BatteryProperties;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import java.io.File;
import java.io.IOException;

public class HwCustBatteryServiceImpl extends HwCustBatteryService {
    private static final String BAD_BATTERY_VALUE = "1";
    private static final String BAD_BATTERY_VALUE_HISI_PATH = "sys/class/power_supply/Battery/bad_battery_flag";
    private static final int ERROR_BATTERY_LEVEL = 10;
    private static final int ERROR_BATTERY_TEMP = 100;
    private static final String TAG = "HwCustBatteryServiceImpl";
    private static final String TEMP_ERR_CHANNEL_ID = "battery_error_temp_id";
    private boolean mBadBatteryWarning = SystemProperties.getBoolean("ro.config.bad_battery_warning", false);
    private NotificationManager mNotificationManager;

    public boolean mutePowerConnectedTone() {
        return SystemProperties.getBoolean("ro.config.mute_usb_sound", false);
    }

    public boolean isBadBatteryWarning() {
        return this.mBadBatteryWarning;
    }

    public void sendBadBatteryWarningNotification(Context context, BatteryProperties oldBatteryProps, BatteryProperties newBatteryProps) {
        if (oldBatteryProps == null || newBatteryProps == null) {
            Slog.i(TAG, "oldBatteryProps:" + oldBatteryProps + " ,newBatteryProps:" + newBatteryProps);
            return;
        }
        Slog.i(TAG, "newBatteryLevel:" + newBatteryProps.batteryLevel + ",oldBatteryLevel:" + oldBatteryProps.batteryLevel + ",batteryTemp:" + newBatteryProps.batteryTemperature);
        if (oldBatteryProps.batteryLevel - newBatteryProps.batteryLevel >= ERROR_BATTERY_LEVEL && newBatteryProps.batteryTemperature < ERROR_BATTERY_TEMP) {
            Object badBattery = null;
            try {
                badBattery = FileUtils.readTextFile(new File(BAD_BATTERY_VALUE_HISI_PATH), 0, null).trim();
            } catch (IOException e) {
                Slog.e(TAG, "Error get bad battery flag." + e);
            }
            if (BAD_BATTERY_VALUE.equals(badBattery)) {
                CharSequence title = context.getResources().getString(33685530);
                if (this.mNotificationManager == null) {
                    this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
                }
                makeNotificationChannel(title.toString());
                Notification notification = new Builder(context, TEMP_ERR_CHANNEL_ID).setSmallIcon(33751168).setContentTitle(title).setContentText(context.getResources().getString(33685529)).setTicker(title).setVibrate(new long[0]).setPriority(2).setVisibility(1).setAutoCancel(true).build();
                if (this.mNotificationManager != null) {
                    this.mNotificationManager.notifyAsUser(null, 33685530, notification, UserHandle.ALL);
                }
            }
        }
    }

    private void makeNotificationChannel(String name) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(TEMP_ERR_CHANNEL_ID, name, 4));
        }
    }
}

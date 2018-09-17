package com.android.internal.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.provider.Settings.System;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemNotificationChannels {
    public static String ACCOUNT = "ACCOUNT";
    public static String ALERTS = "ALERTS";
    public static String ALERT_WINDOW = "ALERT_WINDOW";
    public static String CAR_MODE = "CAR_MODE";
    public static String DEVELOPER = "DEVELOPER";
    public static String DEVICE_ADMIN = "DEVICE_ADMIN";
    public static String FOREGROUND_SERVICE = "FOREGROUND_SERVICE";
    public static final String HW_PCM = "HW_PCM";
    public static String NETWORK_ALERTS = "NETWORK_ALERTS";
    public static String NETWORK_AVAILABLE = "NETWORK_AVAILABLE";
    public static String NETWORK_STATUS = "NETWORK_STATUS";
    public static String PHYSICAL_KEYBOARD = "PHYSICAL_KEYBOARD";
    public static String RETAIL_MODE = "RETAIL_MODE";
    public static String SECURITY = "SECURITY";
    public static String UPDATES = "UPDATES";
    public static String USB = "USB";
    public static String VIRTUAL_KEYBOARD = "VIRTUAL_KEYBOARD";
    public static String VPN = "VPN";

    public static void createAll(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(NotificationManager.class);
        List<NotificationChannel> channelsList = new ArrayList();
        channelsList.add(new NotificationChannel(VIRTUAL_KEYBOARD, context.getString(R.string.notification_channel_virtual_keyboard), 2));
        NotificationChannel physicalKeyboardChannel = new NotificationChannel(PHYSICAL_KEYBOARD, context.getString(R.string.notification_channel_physical_keyboard), 3);
        physicalKeyboardChannel.setSound(System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        channelsList.add(physicalKeyboardChannel);
        channelsList.add(new NotificationChannel(SECURITY, context.getString(R.string.notification_channel_security), 2));
        channelsList.add(new NotificationChannel(CAR_MODE, context.getString(R.string.notification_channel_car_mode), 2));
        channelsList.add(newAccountChannel(context));
        channelsList.add(new NotificationChannel(DEVELOPER, context.getString(R.string.notification_channel_developer), 2));
        channelsList.add(new NotificationChannel(UPDATES, context.getString(R.string.notification_channel_updates), 2));
        channelsList.add(new NotificationChannel(NETWORK_STATUS, context.getString(R.string.notification_channel_network_status), 2));
        NotificationChannel networkAlertsChannel = new NotificationChannel(NETWORK_ALERTS, context.getString(R.string.notification_channel_network_alerts), 4);
        networkAlertsChannel.setSound(System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        channelsList.add(networkAlertsChannel);
        channelsList.add(new NotificationChannel(NETWORK_AVAILABLE, context.getString(R.string.notification_channel_network_available), 2));
        channelsList.add(new NotificationChannel(VPN, context.getString(R.string.notification_channel_vpn), 2));
        channelsList.add(new NotificationChannel(DEVICE_ADMIN, context.getString(R.string.notification_channel_device_admin), 2));
        NotificationChannel alertsChannel = new NotificationChannel(ALERTS, context.getString(R.string.notification_channel_alerts), 3);
        alertsChannel.setSound(System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        channelsList.add(alertsChannel);
        channelsList.add(new NotificationChannel(RETAIL_MODE, context.getString(R.string.notification_channel_retail_mode), 2));
        channelsList.add(new NotificationChannel(USB, context.getString(R.string.notification_channel_usb), 1));
        NotificationChannel foregroundChannel = new NotificationChannel(FOREGROUND_SERVICE, context.getString(R.string.notification_channel_foreground_service), 2);
        foregroundChannel.setBlockableSystem(true);
        channelsList.add(foregroundChannel);
        channelsList.add(new NotificationChannel(HW_PCM, context.getString(33685941), 3));
        nm.createNotificationChannels(channelsList);
    }

    public static void createAccountChannelForPackage(String pkg, int uid, Context context) {
        try {
            NotificationManager.getService().createNotificationChannelsForPackage(pkg, uid, new ParceledListSlice(Arrays.asList(new NotificationChannel[]{newAccountChannel(context)})));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static NotificationChannel newAccountChannel(Context context) {
        return new NotificationChannel(ACCOUNT, context.getString(R.string.notification_channel_account), 2);
    }

    private SystemNotificationChannels() {
    }
}

package com.android.internal.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.media.AudioAttributes;
import android.os.RemoteException;
import android.provider.Settings;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemNotificationChannels {
    public static String ACCOUNT = "ACCOUNT";
    public static String ALERTS = "ALERTS";
    public static String CAR_MODE = "CAR_MODE";
    public static String DEVELOPER = "DEVELOPER";
    public static String DEVICE_ADMIN = "DEVICE_ADMIN_ALERTS";
    @Deprecated
    public static String DEVICE_ADMIN_DEPRECATED = "DEVICE_ADMIN";
    public static String DO_NOT_DISTURB = "DO_NOT_DISTURB";
    public static String FOREGROUND_SERVICE = "FOREGROUND_SERVICE";
    public static String HEAVY_WEIGHT_APP = "HEAVY_WEIGHT_APP";
    public static final String HW_PCM = "HW_PCM";
    public static final String HW_SMS = "HW_SMS";
    public static String NETWORK_ALERTS = "NETWORK_ALERTS";
    public static String NETWORK_AVAILABLE = "NETWORK_AVAILABLE";
    public static String NETWORK_STATUS = "NETWORK_STATUS";
    public static String PHYSICAL_KEYBOARD = "PHYSICAL_KEYBOARD";
    public static String RETAIL_MODE = "RETAIL_MODE";
    public static String SECURITY = "SECURITY";
    public static String SYSTEM_CHANGES = "SYSTEM_CHANGES";
    public static String UPDATES = "UPDATES";
    public static String USB = "USB";
    public static String VIRTUAL_KEYBOARD = "VIRTUAL_KEYBOARD";
    public static String VPN = "VPN";

    public static void createAll(Context context) {
        List<NotificationChannel> channelsList = new ArrayList<>();
        NotificationChannel keyboard = new NotificationChannel(VIRTUAL_KEYBOARD, context.getString(R.string.notification_channel_virtual_keyboard), 2);
        keyboard.setBlockableSystem(true);
        channelsList.add(keyboard);
        NotificationChannel physicalKeyboardChannel = new NotificationChannel(PHYSICAL_KEYBOARD, context.getString(R.string.notification_channel_physical_keyboard), 3);
        physicalKeyboardChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        physicalKeyboardChannel.setBlockableSystem(true);
        channelsList.add(physicalKeyboardChannel);
        channelsList.add(new NotificationChannel(SECURITY, context.getString(R.string.notification_channel_security), 2));
        NotificationChannel car = new NotificationChannel(CAR_MODE, context.getString(R.string.notification_channel_car_mode), 2);
        car.setBlockableSystem(true);
        channelsList.add(car);
        channelsList.add(newAccountChannel(context));
        NotificationChannel developer = new NotificationChannel(DEVELOPER, context.getString(R.string.notification_channel_developer), 2);
        developer.setBlockableSystem(true);
        channelsList.add(developer);
        channelsList.add(new NotificationChannel(UPDATES, context.getString(R.string.notification_channel_updates), 2));
        channelsList.add(new NotificationChannel(NETWORK_STATUS, context.getString(R.string.notification_channel_network_status), 2));
        NotificationChannel networkAlertsChannel = new NotificationChannel(NETWORK_ALERTS, context.getString(R.string.notification_channel_network_alerts), 4);
        networkAlertsChannel.setBlockableSystem(true);
        channelsList.add(networkAlertsChannel);
        NotificationChannel networkAvailable = new NotificationChannel(NETWORK_AVAILABLE, context.getString(R.string.notification_channel_network_available), 2);
        networkAvailable.setBlockableSystem(true);
        channelsList.add(networkAvailable);
        channelsList.add(new NotificationChannel(VPN, context.getString(R.string.notification_channel_vpn), 2));
        channelsList.add(new NotificationChannel(DEVICE_ADMIN, context.getString(R.string.notification_channel_device_admin), 4));
        channelsList.add(new NotificationChannel(ALERTS, context.getString(R.string.notification_channel_alerts), 3));
        channelsList.add(new NotificationChannel(RETAIL_MODE, context.getString(R.string.notification_channel_retail_mode), 2));
        channelsList.add(new NotificationChannel(USB, context.getString(R.string.notification_channel_usb), 1));
        NotificationChannel foregroundChannel = new NotificationChannel(FOREGROUND_SERVICE, context.getString(R.string.notification_channel_foreground_service), 2);
        foregroundChannel.setBlockableSystem(true);
        channelsList.add(foregroundChannel);
        NotificationChannel heavyWeightChannel = new NotificationChannel(HEAVY_WEIGHT_APP, context.getString(R.string.notification_channel_heavy_weight_app), 3);
        heavyWeightChannel.setShowBadge(false);
        heavyWeightChannel.setSound(null, new AudioAttributes.Builder().setContentType(4).setUsage(10).build());
        channelsList.add(heavyWeightChannel);
        channelsList.add(new NotificationChannel(HW_PCM, context.getString(33685941), 3));
        channelsList.add(new NotificationChannel(SYSTEM_CHANGES, context.getString(R.string.notification_channel_system_changes), 2));
        channelsList.add(new NotificationChannel(DO_NOT_DISTURB, context.getString(R.string.notification_channel_do_not_disturb), 3));
        channelsList.add(new NotificationChannel(HW_SMS, context.getString(R.string.storage_sd_card), 3));
        ((NotificationManager) context.getSystemService(NotificationManager.class)).createNotificationChannels(channelsList);
    }

    public static void removeDeprecated(Context context) {
        ((NotificationManager) context.getSystemService(NotificationManager.class)).deleteNotificationChannel(DEVICE_ADMIN_DEPRECATED);
    }

    public static void createAccountChannelForPackage(String pkg, int uid, Context context) {
        try {
            NotificationManager.getService().createNotificationChannelsForPackage(pkg, uid, new ParceledListSlice(Arrays.asList(newAccountChannel(context))));
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

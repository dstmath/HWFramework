package com.android.internal.notification;

import android.app.INotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.media.AudioAttributes;
import android.os.RemoteException;
import android.provider.Settings;
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
    public static String DO_NOT_DISTURB = "DO_NOT_DISTURB";
    public static String FOREGROUND_SERVICE = "FOREGROUND_SERVICE";
    public static String HEAVY_WEIGHT_APP = "HEAVY_WEIGHT_APP";
    public static final String HW_PCM = "HW_PCM";
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
        Context context2 = context;
        List<NotificationChannel> channelsList = new ArrayList<>();
        NotificationChannel keyboard = new NotificationChannel(VIRTUAL_KEYBOARD, context2.getString(17040595), 2);
        keyboard.setBlockableSystem(true);
        channelsList.add(keyboard);
        NotificationChannel physicalKeyboardChannel = new NotificationChannel(PHYSICAL_KEYBOARD, context2.getString(17040587), 3);
        physicalKeyboardChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        physicalKeyboardChannel.setBlockableSystem(true);
        channelsList.add(physicalKeyboardChannel);
        channelsList.add(new NotificationChannel(SECURITY, context2.getString(17040589), 2));
        NotificationChannel car = new NotificationChannel(CAR_MODE, context2.getString(17040574), 2);
        car.setBlockableSystem(true);
        channelsList.add(car);
        channelsList.add(newAccountChannel(context));
        NotificationChannel developer = new NotificationChannel(DEVELOPER, context2.getString(17040575), 2);
        developer.setBlockableSystem(true);
        channelsList.add(developer);
        channelsList.add(new NotificationChannel(UPDATES, context2.getString(17040593), 2));
        channelsList.add(new NotificationChannel(NETWORK_STATUS, context2.getString(17040586), 2));
        NotificationChannel networkAlertsChannel = new NotificationChannel(NETWORK_ALERTS, context2.getString(17040584), 4);
        networkAlertsChannel.setBlockableSystem(true);
        channelsList.add(networkAlertsChannel);
        NotificationChannel networkAvailable = new NotificationChannel(NETWORK_AVAILABLE, context2.getString(17040585), 2);
        networkAvailable.setBlockableSystem(true);
        channelsList.add(networkAvailable);
        channelsList.add(new NotificationChannel(VPN, context2.getString(17040597), 2));
        NotificationChannel notificationChannel = keyboard;
        NotificationChannel deviceAdmin = new NotificationChannel(DEVICE_ADMIN, context2.getString(17040576), 2);
        channelsList.add(deviceAdmin);
        NotificationChannel notificationChannel2 = deviceAdmin;
        NotificationChannel deviceAdmin2 = new NotificationChannel(ALERTS, context2.getString(17040572), 3);
        channelsList.add(deviceAdmin2);
        NotificationChannel notificationChannel3 = deviceAdmin2;
        NotificationChannel retail = new NotificationChannel(RETAIL_MODE, context2.getString(17040588), 2);
        channelsList.add(retail);
        NotificationChannel notificationChannel4 = retail;
        NotificationChannel retail2 = new NotificationChannel(USB, context2.getString(17040594), 1);
        channelsList.add(retail2);
        NotificationChannel notificationChannel5 = retail2;
        NotificationChannel foregroundChannel = new NotificationChannel(FOREGROUND_SERVICE, context2.getString(17040579), 2);
        foregroundChannel.setBlockableSystem(true);
        channelsList.add(foregroundChannel);
        NotificationChannel notificationChannel6 = foregroundChannel;
        NotificationChannel heavyWeightChannel = new NotificationChannel(HEAVY_WEIGHT_APP, context2.getString(17040580), 3);
        heavyWeightChannel.setShowBadge(false);
        heavyWeightChannel.setSound(null, new AudioAttributes.Builder().setContentType(4).setUsage(10).build());
        channelsList.add(heavyWeightChannel);
        NotificationChannel notificationChannel7 = heavyWeightChannel;
        channelsList.add(new NotificationChannel(HW_PCM, context2.getString(33685941), 3));
        NotificationChannel systemChanges = new NotificationChannel(SYSTEM_CHANGES, context2.getString(17040592), 2);
        channelsList.add(systemChanges);
        NotificationChannel notificationChannel8 = systemChanges;
        channelsList.add(new NotificationChannel(DO_NOT_DISTURB, context2.getString(17040577), 2));
        ((NotificationManager) context2.getSystemService(NotificationManager.class)).createNotificationChannels(channelsList);
    }

    public static void createAccountChannelForPackage(String pkg, int uid, Context context) {
        INotificationManager iNotificationManager = NotificationManager.getService();
        try {
            iNotificationManager.createNotificationChannelsForPackage(pkg, uid, new ParceledListSlice(Arrays.asList(new NotificationChannel[]{newAccountChannel(context)})));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static NotificationChannel newAccountChannel(Context context) {
        return new NotificationChannel(ACCOUNT, context.getString(17040571), 2);
    }

    private SystemNotificationChannels() {
    }
}

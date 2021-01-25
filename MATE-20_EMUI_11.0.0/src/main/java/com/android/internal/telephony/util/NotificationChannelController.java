package com.android.internal.telephony.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import java.util.Arrays;

public class NotificationChannelController {
    public static final String CHANNEL_ID_ALERT = "alert";
    public static final String CHANNEL_ID_CALL_FORWARD = "callForwardNew";
    private static final String CHANNEL_ID_CALL_FORWARD_DEPRECATED = "callForward";
    private static final String CHANNEL_ID_MOBILE_DATA_ALERT_DEPRECATED = "mobileDataAlert";
    public static final String CHANNEL_ID_MOBILE_DATA_STATUS = "mobileDataAlertNew";
    public static final String CHANNEL_ID_SIM = "sim";
    public static final String CHANNEL_ID_SMS = "sms";
    public static final String CHANNEL_ID_VOICE_MAIL = "voiceMail";
    public static final String CHANNEL_ID_WFC = "wfc";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.util.NotificationChannelController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                NotificationChannelController.createAll(context);
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction()) && -1 != SubscriptionManager.getDefaultSubscriptionId()) {
                NotificationChannelController.migrateVoicemailNotificationSettings(context);
            }
        }
    };

    /* access modifiers changed from: private */
    public static void createAll(Context context) {
        NotificationChannel alertChannel = new NotificationChannel(CHANNEL_ID_ALERT, context.getText(17040672), 3);
        alertChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, new AudioAttributes.Builder().setUsage(5).build());
        alertChannel.setBlockableSystem(true);
        NotificationChannel mobileDataStatusChannel = new NotificationChannel(CHANNEL_ID_MOBILE_DATA_STATUS, context.getText(17040671), 2);
        mobileDataStatusChannel.setBlockableSystem(true);
        NotificationChannel simChannel = new NotificationChannel(CHANNEL_ID_SIM, context.getText(17040679), 2);
        simChannel.setSound(null, null);
        NotificationChannel callforwardChannel = new NotificationChannel(CHANNEL_ID_CALL_FORWARD, context.getText(17040663), 3);
        migrateCallFowardNotificationChannel(context, callforwardChannel);
        ((NotificationManager) context.getSystemService(NotificationManager.class)).createNotificationChannels(Arrays.asList(new NotificationChannel(CHANNEL_ID_SMS, context.getText(17040680), 4), new NotificationChannel(CHANNEL_ID_WFC, context.getText(17040687), 3), alertChannel, mobileDataStatusChannel, simChannel, callforwardChannel));
        if (getChannel(CHANNEL_ID_VOICE_MAIL, context) != null) {
            migrateVoicemailNotificationSettings(context);
        }
        if (getChannel(CHANNEL_ID_MOBILE_DATA_ALERT_DEPRECATED, context) != null) {
            ((NotificationManager) context.getSystemService(NotificationManager.class)).deleteNotificationChannel(CHANNEL_ID_MOBILE_DATA_ALERT_DEPRECATED);
        }
        if (getChannel(CHANNEL_ID_CALL_FORWARD_DEPRECATED, context) != null) {
            ((NotificationManager) context.getSystemService(NotificationManager.class)).deleteNotificationChannel(CHANNEL_ID_CALL_FORWARD_DEPRECATED);
        }
    }

    public NotificationChannelController(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
        createAll(context);
    }

    public static NotificationChannel getChannel(String channelId, Context context) {
        return ((NotificationManager) context.getSystemService(NotificationManager.class)).getNotificationChannel(channelId);
    }

    /* access modifiers changed from: private */
    public static void migrateVoicemailNotificationSettings(Context context) {
        NotificationChannel voiceMailChannel = new NotificationChannel(CHANNEL_ID_VOICE_MAIL, context.getText(17040685), 3);
        voiceMailChannel.enableVibration(VoicemailNotificationSettingsUtil.getVibrationPreference(context));
        Uri sound = VoicemailNotificationSettingsUtil.getRingTonePreference(context);
        voiceMailChannel.setSound(sound == null ? Settings.System.DEFAULT_NOTIFICATION_URI : sound, new AudioAttributes.Builder().setUsage(5).build());
        ((NotificationManager) context.getSystemService(NotificationManager.class)).createNotificationChannel(voiceMailChannel);
    }

    private static void migrateCallFowardNotificationChannel(Context context, NotificationChannel callforwardChannel) {
        NotificationChannel deprecatedChannel = getChannel(CHANNEL_ID_CALL_FORWARD_DEPRECATED, context);
        if (deprecatedChannel != null) {
            callforwardChannel.setSound(deprecatedChannel.getSound(), deprecatedChannel.getAudioAttributes());
            callforwardChannel.setVibrationPattern(deprecatedChannel.getVibrationPattern());
            callforwardChannel.enableVibration(deprecatedChannel.shouldVibrate());
        }
    }
}

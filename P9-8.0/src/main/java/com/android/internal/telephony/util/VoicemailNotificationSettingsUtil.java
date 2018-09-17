package com.android.internal.telephony.util;

import android.app.NotificationChannel;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class VoicemailNotificationSettingsUtil {
    private static final String OLD_VOICEMAIL_NOTIFICATION_RINGTONE_SHARED_PREFS_KEY = "button_voicemail_notification_ringtone_key";
    private static final String OLD_VOICEMAIL_NOTIFICATION_VIBRATION_SHARED_PREFS_KEY = "button_voicemail_notification_vibrate_key";
    private static final String OLD_VOICEMAIL_RINGTONE_SHARED_PREFS_KEY = "button_voicemail_notification_ringtone_key";
    private static final String OLD_VOICEMAIL_VIBRATE_WHEN_SHARED_PREFS_KEY = "button_voicemail_notification_vibrate_when_key";
    private static final String OLD_VOICEMAIL_VIBRATION_ALWAYS = "always";
    private static final String OLD_VOICEMAIL_VIBRATION_NEVER = "never";
    private static final String VOICEMAIL_NOTIFICATION_RINGTONE_SHARED_PREFS_KEY_PREFIX = "voicemail_notification_ringtone_";
    private static final String VOICEMAIL_NOTIFICATION_VIBRATION_SHARED_PREFS_KEY_PREFIX = "voicemail_notification_vibrate_";

    public static void setVibrationEnabled(Context context, boolean isEnabled) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(getVoicemailVibrationSharedPrefsKey(), isEnabled);
        editor.commit();
    }

    public static boolean isVibrationEnabled(Context context) {
        NotificationChannel channel = NotificationChannelController.getChannel(NotificationChannelController.CHANNEL_ID_VOICE_MAIL, context);
        return channel != null ? channel.shouldVibrate() : getVibrationPreference(context);
    }

    public static boolean getVibrationPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        migrateVoicemailVibrationSettingsIfNeeded(context, prefs);
        return prefs.getBoolean(getVoicemailVibrationSharedPrefsKey(), false);
    }

    public static void setRingtoneUri(Context context, Uri ringtoneUri) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtoneUriStr = ringtoneUri != null ? ringtoneUri.toString() : "";
        Editor editor = prefs.edit();
        editor.putString(getVoicemailRingtoneSharedPrefsKey(), ringtoneUriStr);
        editor.commit();
    }

    public static Uri getRingtoneUri(Context context) {
        NotificationChannel channel = NotificationChannelController.getChannel(NotificationChannelController.CHANNEL_ID_VOICE_MAIL, context);
        return channel != null ? channel.getSound() : getRingTonePreference(context);
    }

    public static Uri getRingTonePreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        migrateVoicemailRingtoneSettingsIfNeeded(context, prefs);
        String uriString = prefs.getString(getVoicemailRingtoneSharedPrefsKey(), System.DEFAULT_NOTIFICATION_URI.toString());
        return !TextUtils.isEmpty(uriString) ? Uri.parse(uriString) : null;
    }

    private static void migrateVoicemailVibrationSettingsIfNeeded(Context context, SharedPreferences prefs) {
        String key = getVoicemailVibrationSharedPrefsKey();
        TelephonyManager telephonyManager = TelephonyManager.from(context);
        if (!prefs.contains(key) && telephonyManager.getPhoneCount() == 1) {
            if (prefs.contains(OLD_VOICEMAIL_NOTIFICATION_VIBRATION_SHARED_PREFS_KEY)) {
                prefs.edit().putBoolean(key, prefs.getBoolean(OLD_VOICEMAIL_NOTIFICATION_VIBRATION_SHARED_PREFS_KEY, false)).remove(OLD_VOICEMAIL_VIBRATE_WHEN_SHARED_PREFS_KEY).commit();
            }
            if (prefs.contains(OLD_VOICEMAIL_VIBRATE_WHEN_SHARED_PREFS_KEY)) {
                prefs.edit().putBoolean(key, prefs.getString(OLD_VOICEMAIL_VIBRATE_WHEN_SHARED_PREFS_KEY, OLD_VOICEMAIL_VIBRATION_NEVER).equals(OLD_VOICEMAIL_VIBRATION_ALWAYS)).remove(OLD_VOICEMAIL_NOTIFICATION_VIBRATION_SHARED_PREFS_KEY).commit();
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0015, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void migrateVoicemailRingtoneSettingsIfNeeded(Context context, SharedPreferences prefs) {
        String key = getVoicemailRingtoneSharedPrefsKey();
        TelephonyManager telephonyManager = TelephonyManager.from(context);
        if (!prefs.contains(key) && telephonyManager.getPhoneCount() == 1 && prefs.contains("button_voicemail_notification_ringtone_key")) {
            prefs.edit().putString(key, prefs.getString("button_voicemail_notification_ringtone_key", null)).remove("button_voicemail_notification_ringtone_key").commit();
        }
    }

    private static String getVoicemailVibrationSharedPrefsKey() {
        return VOICEMAIL_NOTIFICATION_VIBRATION_SHARED_PREFS_KEY_PREFIX + SubscriptionManager.getDefaultSubscriptionId();
    }

    private static String getVoicemailRingtoneSharedPrefsKey() {
        return VOICEMAIL_NOTIFICATION_RINGTONE_SHARED_PREFS_KEY_PREFIX + SubscriptionManager.getDefaultSubscriptionId();
    }
}

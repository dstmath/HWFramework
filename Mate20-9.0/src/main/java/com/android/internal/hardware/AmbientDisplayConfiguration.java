package com.android.internal.hardware;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

public class AmbientDisplayConfiguration {
    private final Context mContext;

    public AmbientDisplayConfiguration(Context context) {
        this.mContext = context;
    }

    public boolean enabled(int user) {
        return pulseOnNotificationEnabled(user) || pulseOnPickupEnabled(user) || pulseOnDoubleTapEnabled(user) || pulseOnLongPressEnabled(user) || alwaysOnEnabled(user);
    }

    public boolean available() {
        return pulseOnNotificationAvailable() || pulseOnPickupAvailable() || pulseOnDoubleTapAvailable();
    }

    public boolean pulseOnNotificationEnabled(int user) {
        return boolSettingDefaultOn("doze_enabled", user) && pulseOnNotificationAvailable();
    }

    public boolean pulseOnNotificationAvailable() {
        return ambientDisplayAvailable();
    }

    public boolean pulseOnPickupEnabled(int user) {
        return (boolSettingDefaultOn("doze_pulse_on_pick_up", user) || alwaysOnEnabled(user)) && pulseOnPickupAvailable();
    }

    public boolean pulseOnPickupAvailable() {
        return dozePulsePickupSensorAvailable() && ambientDisplayAvailable();
    }

    public boolean dozePulsePickupSensorAvailable() {
        return this.mContext.getResources().getBoolean(17956938);
    }

    public boolean pulseOnPickupCanBeModified(int user) {
        return !alwaysOnEnabled(user);
    }

    public boolean pulseOnDoubleTapEnabled(int user) {
        return boolSettingDefaultOn("doze_pulse_on_double_tap", user) && pulseOnDoubleTapAvailable();
    }

    public boolean pulseOnDoubleTapAvailable() {
        return doubleTapSensorAvailable() && ambientDisplayAvailable();
    }

    public boolean doubleTapSensorAvailable() {
        return !TextUtils.isEmpty(doubleTapSensorType());
    }

    public String doubleTapSensorType() {
        return this.mContext.getResources().getString(17039799);
    }

    public String longPressSensorType() {
        return this.mContext.getResources().getString(17039800);
    }

    public boolean pulseOnLongPressEnabled(int user) {
        return pulseOnLongPressAvailable() && boolSettingDefaultOff("doze_pulse_on_long_press", user);
    }

    private boolean pulseOnLongPressAvailable() {
        return !TextUtils.isEmpty(longPressSensorType());
    }

    public boolean alwaysOnEnabled(int user) {
        return boolSettingDefaultOn("doze_always_on", user) && alwaysOnAvailable() && !accessibilityInversionEnabled(user);
    }

    public boolean alwaysOnAvailable() {
        return (alwaysOnDisplayDebuggingEnabled() || alwaysOnDisplayAvailable()) && ambientDisplayAvailable();
    }

    public boolean alwaysOnAvailableForUser(int user) {
        return alwaysOnAvailable() && !accessibilityInversionEnabled(user);
    }

    public String ambientDisplayComponent() {
        return this.mContext.getResources().getString(17039798);
    }

    public boolean accessibilityInversionEnabled(int user) {
        return boolSettingDefaultOff("accessibility_display_inversion_enabled", user);
    }

    public boolean ambientDisplayAvailable() {
        return !TextUtils.isEmpty(ambientDisplayComponent());
    }

    private boolean alwaysOnDisplayAvailable() {
        return this.mContext.getResources().getBoolean(17956937);
    }

    private boolean alwaysOnDisplayDebuggingEnabled() {
        return SystemProperties.getBoolean("debug.doze.aod", false) && Build.IS_DEBUGGABLE;
    }

    private boolean boolSettingDefaultOn(String name, int user) {
        return boolSetting(name, user, 1);
    }

    private boolean boolSettingDefaultOff(String name, int user) {
        return boolSetting(name, user, 0);
    }

    private boolean boolSetting(String name, int user, int def) {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), name, def, user) != 0;
    }
}

package android.hardware.display;

import android.content.Context;
import android.net.wifi.WifiScanLog;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.internal.R;

public class AmbientDisplayConfiguration {
    private static final boolean IS_SUPPORT_APAOD = WifiScanLog.EVENT_KEY2.equals(SystemProperties.get("ro.config.support_aod", null));
    private final boolean mAlwaysOnByDefault = this.mContext.getResources().getBoolean(R.bool.config_dozeAlwaysOnEnabled);
    private final Context mContext;

    public AmbientDisplayConfiguration(Context context) {
        this.mContext = context;
    }

    public boolean enabled(int user) {
        return IS_SUPPORT_APAOD || pulseOnNotificationEnabled(user) || pulseOnLongPressEnabled(user) || alwaysOnEnabled(user) || wakeScreenGestureEnabled(user) || pickupGestureEnabled(user) || tapGestureEnabled(user) || doubleTapGestureEnabled(user);
    }

    public boolean pulseOnNotificationEnabled(int user) {
        return boolSettingDefaultOn(Settings.Secure.DOZE_ENABLED, user) && pulseOnNotificationAvailable();
    }

    public boolean pulseOnNotificationAvailable() {
        return ambientDisplayAvailable();
    }

    public boolean pickupGestureEnabled(int user) {
        return boolSettingDefaultOn(Settings.Secure.DOZE_PICK_UP_GESTURE, user) && dozePickupSensorAvailable();
    }

    public boolean dozePickupSensorAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_dozePulsePickup);
    }

    public boolean tapGestureEnabled(int user) {
        return boolSettingDefaultOn(Settings.Secure.DOZE_TAP_SCREEN_GESTURE, user) && tapSensorAvailable();
    }

    public boolean tapSensorAvailable() {
        return !TextUtils.isEmpty(tapSensorType());
    }

    public boolean doubleTapGestureEnabled(int user) {
        return boolSettingDefaultOn(Settings.Secure.DOZE_DOUBLE_TAP_GESTURE, user) && doubleTapSensorAvailable();
    }

    public boolean doubleTapSensorAvailable() {
        return !TextUtils.isEmpty(doubleTapSensorType());
    }

    public boolean wakeScreenGestureAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_dozeWakeLockScreenSensorAvailable);
    }

    public boolean wakeScreenGestureEnabled(int user) {
        return boolSettingDefaultOn(Settings.Secure.DOZE_WAKE_SCREEN_GESTURE, user) && wakeScreenGestureAvailable();
    }

    public long getWakeLockScreenDebounce() {
        return (long) this.mContext.getResources().getInteger(R.integer.config_dozeWakeLockScreenDebounce);
    }

    public String doubleTapSensorType() {
        return this.mContext.getResources().getString(R.string.config_dozeDoubleTapSensorType);
    }

    public String tapSensorType() {
        return this.mContext.getResources().getString(R.string.config_dozeTapSensorType);
    }

    public String longPressSensorType() {
        return this.mContext.getResources().getString(R.string.config_dozeLongPressSensorType);
    }

    public boolean pulseOnLongPressEnabled(int user) {
        return pulseOnLongPressAvailable() && boolSettingDefaultOff(Settings.Secure.DOZE_PULSE_ON_LONG_PRESS, user);
    }

    private boolean pulseOnLongPressAvailable() {
        return !TextUtils.isEmpty(longPressSensorType());
    }

    public boolean alwaysOnEnabled(int user) {
        return boolSetting(Settings.Secure.DOZE_ALWAYS_ON, user, this.mAlwaysOnByDefault ? 1 : 0) && alwaysOnAvailable() && !accessibilityInversionEnabled(user);
    }

    public boolean alwaysOnAvailable() {
        return (alwaysOnDisplayDebuggingEnabled() || alwaysOnDisplayAvailable()) && ambientDisplayAvailable();
    }

    public boolean alwaysOnAvailableForUser(int user) {
        return alwaysOnAvailable() && !accessibilityInversionEnabled(user);
    }

    public String ambientDisplayComponent() {
        return this.mContext.getResources().getString(R.string.config_dozeComponent);
    }

    public boolean accessibilityInversionEnabled(int user) {
        return boolSettingDefaultOff(Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, user);
    }

    public boolean ambientDisplayAvailable() {
        return !TextUtils.isEmpty(ambientDisplayComponent());
    }

    private boolean alwaysOnDisplayAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_dozeAlwaysOnDisplayAvailable);
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

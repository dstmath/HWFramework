package com.android.internal.hardware;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.internal.R;

public class AmbientDisplayConfiguration {
    private final Context mContext;

    public AmbientDisplayConfiguration(Context context) {
        this.mContext = context;
    }

    public boolean enabled(int user) {
        if (pulseOnNotificationEnabled(user) || pulseOnPickupEnabled(user) || pulseOnDoubleTapEnabled(user)) {
            return true;
        }
        return alwaysOnEnabled(user);
    }

    public boolean available() {
        if (pulseOnNotificationAvailable() || pulseOnPickupAvailable()) {
            return true;
        }
        return pulseOnDoubleTapAvailable();
    }

    public boolean pulseOnNotificationEnabled(int user) {
        return boolSettingDefaultOn(Secure.DOZE_ENABLED, user) ? pulseOnNotificationAvailable() : false;
    }

    public boolean pulseOnNotificationAvailable() {
        return ambientDisplayAvailable();
    }

    public boolean pulseOnPickupEnabled(int user) {
        if (boolSettingDefaultOn(Secure.DOZE_PULSE_ON_PICK_UP, user)) {
            return pulseOnPickupAvailable();
        }
        return false;
    }

    public boolean pulseOnPickupAvailable() {
        if (this.mContext.getResources().getBoolean(R.bool.config_dozePulsePickup)) {
            return ambientDisplayAvailable();
        }
        return false;
    }

    public boolean pulseOnDoubleTapEnabled(int user) {
        if (boolSettingDefaultOn(Secure.DOZE_PULSE_ON_DOUBLE_TAP, user)) {
            return pulseOnDoubleTapAvailable();
        }
        return false;
    }

    public boolean pulseOnDoubleTapAvailable() {
        return !TextUtils.isEmpty(doubleTapSensorType()) ? ambientDisplayAvailable() : false;
    }

    public String doubleTapSensorType() {
        return this.mContext.getResources().getString(R.string.config_dozeDoubleTapSensorType);
    }

    public boolean alwaysOnEnabled(int user) {
        if (boolSettingDefaultOff(Secure.DOZE_ALWAYS_ON, user)) {
            return alwaysOnAvailable();
        }
        return false;
    }

    public boolean alwaysOnAvailable() {
        return false;
    }

    public String ambientDisplayComponent() {
        return this.mContext.getResources().getString(R.string.config_dozeComponent);
    }

    private boolean ambientDisplayAvailable() {
        return TextUtils.isEmpty(ambientDisplayComponent()) ^ 1;
    }

    private boolean boolSettingDefaultOn(String name, int user) {
        return boolSetting(name, user, 1);
    }

    private boolean boolSettingDefaultOff(String name, int user) {
        return boolSetting(name, user, 0);
    }

    private boolean boolSetting(String name, int user, int def) {
        return Secure.getIntForUser(this.mContext.getContentResolver(), name, def, user) != 0;
    }
}

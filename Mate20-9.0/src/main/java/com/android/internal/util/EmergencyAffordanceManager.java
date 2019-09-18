package com.android.internal.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.UserHandle;
import android.provider.Settings;

public class EmergencyAffordanceManager {
    private static final String EMERGENCY_CALL_NUMBER_SETTING = "emergency_affordance_number";
    public static final boolean ENABLED = true;
    private static final String FORCE_EMERGENCY_AFFORDANCE_SETTING = "force_emergency_affordance";
    private final Context mContext;

    public EmergencyAffordanceManager(Context context) {
        this.mContext = context;
    }

    public final void performEmergencyCall() {
        performEmergencyCall(this.mContext);
    }

    private static Uri getPhoneUri(Context context) {
        String number = context.getResources().getString(17039802);
        if (Build.IS_DEBUGGABLE) {
            String override = Settings.Global.getString(context.getContentResolver(), EMERGENCY_CALL_NUMBER_SETTING);
            if (override != null) {
                number = override;
            }
        }
        return Uri.fromParts("tel", number, null);
    }

    private static void performEmergencyCall(Context context) {
        Intent intent = new Intent("android.intent.action.CALL_EMERGENCY");
        intent.setData(getPhoneUri(context));
        intent.setFlags(268435456);
        context.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    public boolean needsEmergencyAffordance() {
        if (forceShowing()) {
            return true;
        }
        return isEmergencyAffordanceNeeded();
    }

    private boolean isEmergencyAffordanceNeeded() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "emergency_affordance_needed", 0) != 0;
    }

    private boolean forceShowing() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), FORCE_EMERGENCY_AFFORDANCE_SETTING, 0) != 0;
    }
}

package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;

public class DeviceControlPlugin extends DevicePolicyPlugin {
    private static final String DB_EYES_PROTECTION_MODE = "eyes_protection_mode";
    private static final int EYE_PROTECTION_CLOSE = 0;
    private static final int EYE_PROTECTION_ON = 1;
    public static final String POLICY_TURN_ON_EYE_COMFORT = "device_control_turn_on_eye_comfort";
    public static final String TAG = "DeviceControlPlugin";

    public DeviceControlPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(POLICY_TURN_ON_EYE_COMFORT, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        return true;
    }

    /* JADX INFO: finally extract failed */
    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        Log.i(TAG, "onSetPolicy: " + policyName);
        if (!checkCallingPermission(who, policyName) || policyData == null) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            if (policyName.hashCode() == 1576234773) {
                if (policyName.equals(POLICY_TURN_ON_EYE_COMFORT)) {
                    c = 0;
                }
            }
            if (c == 0) {
                onSetEyeComfotPolicy(policyData);
            }
            Binder.restoreCallingIdentity(identityToken);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    private void onSetEyeComfotPolicy(Bundle policyData) {
        Settings.System.putInt(this.mContext.getContentResolver(), "eyes_protection_mode", policyData.getBoolean("value", false));
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        Log.i(TAG, "onGetPolicy: " + policyName);
        boolean isTurnedOn = false;
        if (!checkCallingPermission(who, policyName) || policyData == null) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            if (policyName.hashCode() == 1576234773) {
                if (policyName.equals(POLICY_TURN_ON_EYE_COMFORT)) {
                    c = 0;
                }
            }
            if (c == 0) {
                if (Settings.System.getInt(this.mContext.getContentResolver(), "eyes_protection_mode", 0) != 0) {
                    isTurnedOn = true;
                }
                policyData.putBoolean("value", isTurnedOn);
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onRemovePolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }
}

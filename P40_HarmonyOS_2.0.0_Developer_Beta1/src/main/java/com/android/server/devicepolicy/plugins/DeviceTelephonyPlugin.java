package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Iterator;

public class DeviceTelephonyPlugin extends DevicePolicyPlugin {
    private static final String ACTION_TAG = "action_tag";
    private static final String CHANGE_PIN_CODE = "change_pin_code";
    private static final String DEFAULT_MAIN_SLOT_CARRIER = "default_main_slot_carrier";
    private static final String DISABLE_AIR_PLANE_MODE = "disable_airplane_mode";
    private static final String DISABLE_DATA = "disable-data";
    private static final String DISABLE_PUSH = "disable-push";
    private static final String DISABLE_SUB = "disable-sub";
    private static final int DISABLE_SUB_ID = 1;
    private static final String DISABLE_SYNC = "disable-sync";
    private static final String INCOMING_SMS_EXCEPTION_PATTERN = "incoming_sms_exception_pattern";
    private static final String INCOMING_SMS_RESTRICTION_PATTERN = "incoming_sms_restriction_pattern";
    private static final String IS_OUTGOING = "isOutgoing";
    private static final String NODE_VALUE = "value";
    private static final String OUTGOING_SMS_EXCEPTION_PATTERN = "outgoing_sms_exception_pattern";
    private static final String OUTGOING_SMS_RESTRICTION_PATTERN = "outgoing_sms_restriction_pattern";
    private static final String PHONE_PACKAGE = "com.android.phone";
    private static final String REMOVE_TYPE = "removeType";
    private static final String SET_PIN_LOCK = "set_pin_lock";
    private static final int SUB0 = 0;
    private static final int SUB1 = 1;
    private static final String TAG = DeviceTelephonyPlugin.class.getSimpleName();
    private static final String TAG_ACTION_DEFAULT_MAIN_SLOT_CARRIER = "action_default_main_slot_carrier";
    private static final String TAG_ACTION_DISABLE_AIR_PLANE_MODE = "action_disable_airplane_mode";
    private static final String TAG_ACTION_DISABLE_DATA = "action_disable_data";
    private static final String TAG_ACTION_DISABLE_DATA_4G = "action_disable_data_4G";
    private static final String TAG_ACTION_DISABLE_SUB = "action_disable_sub";
    private static final String TAG_MDM_CARRIER_CMCC = "cmcc";
    private static final String TAG_MDM_CARRIER_CT = "ct";

    public DeviceTelephonyPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(DISABLE_SUB, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_DATA, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_AIR_PLANE_MODE, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_SYNC, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_PUSH, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(SET_PIN_LOCK, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(CHANGE_PIN_CODE, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("outgoing_day_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"day_mode", "limit_number_day", "day_mode_time"});
        struct.addStruct("outgoing_week_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"week_mode", "limit_number_week", "week_mode_time"});
        struct.addStruct("outgoing_month_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"month_mode", "limit_number_month", "month_mode_time"});
        struct.addStruct("incoming_day_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"day_mode", "limit_number_day", "day_mode_time"});
        struct.addStruct("incoming_week_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"week_mode", "limit_number_week", "week_mode_time"});
        struct.addStruct("incoming_month_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"month_mode", "limit_number_month", "month_mode_time"});
        struct.addStruct(INCOMING_SMS_EXCEPTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(INCOMING_SMS_RESTRICTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(OUTGOING_SMS_EXCEPTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(OUTGOING_SMS_RESTRICTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(DEFAULT_MAIN_SLOT_CARRIER, PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_TELEPHONY", "have no MDM_TELEPHONY permission!");
        return true;
    }

    private void disableSub(Bundle policyData) {
        boolean isDisableSub = policyData.getBoolean("value");
        String str = TAG;
        HwLog.i(str, "disablesub: " + isDisableSub);
        Intent intentDisableSub = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intentDisableSub.putExtra(ACTION_TAG, TAG_ACTION_DISABLE_SUB);
        intentDisableSub.putExtra("subId", 1);
        intentDisableSub.putExtra("subState", isDisableSub);
        this.mContext.sendBroadcast(intentDisableSub);
    }

    private void disableData(Bundle policyData) {
        boolean isDisableData = policyData.getBoolean("value");
        Intent intentDisableData4G = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intentDisableData4G.putExtra(ACTION_TAG, TAG_ACTION_DISABLE_DATA_4G);
        intentDisableData4G.putExtra("subId", 0);
        intentDisableData4G.putExtra("dataState", isDisableData);
        this.mContext.sendBroadcast(intentDisableData4G);
    }

    private void disableAirPlaneMode(Bundle policyData) {
        boolean isDisableAirPlane = policyData.getBoolean("value");
        String str = TAG;
        HwLog.i(str, "disableAirPlane: " + isDisableAirPlane);
        boolean isAirplaneModeOn = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        String str2 = TAG;
        HwLog.i(str2, "isAirplaneModeOn: " + isAirplaneModeOn);
        if (isDisableAirPlane && isAirplaneModeOn) {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            long ident = Binder.clearCallingIdentity();
            try {
                connectivityManager.setAirplaneMode(false);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        Intent intentDisableAirPlane = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intentDisableAirPlane.setPackage(DeviceSettingsPlugin.SETTINGS_APK_NAME);
        intentDisableAirPlane.putExtra(ACTION_TAG, TAG_ACTION_DISABLE_AIR_PLANE_MODE);
        intentDisableAirPlane.putExtra("airPlaneState", isDisableAirPlane);
        this.mContext.sendBroadcast(intentDisableAirPlane);
    }

    private boolean setPinLock(Bundle policyData) {
        int subId = policyData.getInt("slotId");
        String password = policyData.getString("password");
        boolean isEnablePinLock = policyData.getBoolean("pinLockState");
        if (HwTelephonyManagerInner.getDefault() == null) {
            return false;
        }
        String str = TAG;
        HwLog.i(str, "setPinLock---- enablePinLock: " + isEnablePinLock + "  subId: " + subId);
        return HwTelephonyManagerInner.getDefault().setPinLockEnabled(isEnablePinLock, password, subId);
    }

    private boolean changePinCode(Bundle policyData) {
        int changPinId = policyData.getInt("slotId");
        String oldPinCode = policyData.getString("oldPinCode");
        String newPinCode = policyData.getString("newPinCode");
        if (HwTelephonyManagerInner.getDefault() == null) {
            return false;
        }
        String str = TAG;
        HwLog.i(str, "changePinLock----changPinId: " + changPinId);
        return HwTelephonyManagerInner.getDefault().changeSimPinCode(oldPinCode, newPinCode, changPinId);
    }

    private boolean setDefaultMainSlotCarrire(Bundle policyData) {
        String carrier = policyData.getString("value");
        String str = TAG;
        HwLog.d(str, "set the default main slot carrier: " + carrier);
        if (!TAG_MDM_CARRIER_CMCC.equals(carrier) && !TAG_MDM_CARRIER_CT.equals(carrier)) {
            return false;
        }
        Intent intent = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intent.putExtra(ACTION_TAG, TAG_ACTION_DEFAULT_MAIN_SLOT_CARRIER);
        intent.putExtra("carrier", carrier);
        this.mContext.sendBroadcast(intent);
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003b, code lost:
        if (r11.equals(com.android.server.devicepolicy.plugins.DeviceTelephonyPlugin.DISABLE_SUB) != false) goto L_0x005d;
     */
    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicy");
        boolean z = false;
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return false;
        }
        switch (policyName.hashCode()) {
            case -1032552593:
                if (policyName.equals(DISABLE_DATA)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -808925998:
                if (policyName.equals(SET_PIN_LOCK)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 650237273:
                if (policyName.equals(DISABLE_AIR_PLANE_MODE)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1352180187:
                break;
            case 1684123142:
                if (policyName.equals(CHANGE_PIN_CODE)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 2125519871:
                if (policyName.equals(DEFAULT_MAIN_SLOT_CARRIER)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            disableSub(policyData);
            return true;
        } else if (z) {
            disableData(policyData);
            return true;
        } else if (z) {
            disableAirPlaneMode(policyData);
            return true;
        } else if (z) {
            return setPinLock(policyData);
        } else {
            if (z) {
                return changePinCode(policyData);
            }
            if (!z) {
                return true;
            }
            return setDefaultMainSlotCarrire(policyData);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        char c;
        HwLog.i(TAG, "onRemovePolicy");
        Intent intentDisableSmsLimit = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
        intentDisableSmsLimit.setPackage(PHONE_PACKAGE);
        intentDisableSmsLimit.putExtra(REMOVE_TYPE, "remove_single_policy");
        switch (policyName.hashCode()) {
            case -1888907677:
                if (policyName.equals("outgoing_week_limit")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1155809047:
                if (policyName.equals("outgoing_month_limit")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 115663941:
                if (policyName.equals("outgoing_day_limit")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1195673727:
                if (policyName.equals("incoming_day_limit")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1526624617:
                if (policyName.equals("incoming_week_limit")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1646476963:
                if (policyName.equals("incoming_month_limit")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
            intentDisableSmsLimit.putExtra("time_mode", "day_mode");
        } else if (c == 1) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
            intentDisableSmsLimit.putExtra("time_mode", "week_mode");
        } else if (c == 2) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
            intentDisableSmsLimit.putExtra("time_mode", "month_mode");
        } else if (c == 3) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
            intentDisableSmsLimit.putExtra("time_mode", "day_mode");
        } else if (c == 4) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
            intentDisableSmsLimit.putExtra("time_mode", "week_mode");
        } else if (c == 5) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
            intentDisableSmsLimit.putExtra("time_mode", "month_mode");
        }
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intentDisableSmsLimit);
        }
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        boolean isSmsLimitPolicy = false;
        if (removedPolicies == null) {
            HwLog.e(TAG, "removed policied list is null");
            return;
        }
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            PolicyStruct.PolicyItem pi = it.next();
            if (pi != null && pi.isGlobalPolicyChanged() && isSmsLimitPolicy(pi.getPolicyName())) {
                isSmsLimitPolicy = true;
            }
        }
        if (isSmsLimitPolicy) {
            Intent intentDisableSmsLimit = new Intent("com.huawei.devicepolicy.action.POLICY_CHANGED");
            intentDisableSmsLimit.setPackage(PHONE_PACKAGE);
            intentDisableSmsLimit.putExtra(REMOVE_TYPE, "remove_all_policy");
            if (this.mContext != null) {
                this.mContext.sendBroadcast(intentDisableSmsLimit);
            }
        }
    }

    private boolean isSmsLimitPolicy(String policyName) {
        for (String policy : new String[]{"outgoing_day_limit", "outgoing_week_limit", "outgoing_month_limit", "incoming_day_limit", "incoming_week_limit", "incoming_month_limit"}) {
            if (policy.equals(policyName)) {
                return true;
            }
        }
        return false;
    }
}

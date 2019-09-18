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

public class DeviceTelephonyPlugin extends DevicePolicyPlugin {
    private static final String CHANGE_PIN_CODE = "change_pin_code";
    private static final String DISABLE_AIR_PLANE_MODE = "disable_airplane_mode";
    private static final String DISABLE_DATA = "disable-data";
    private static final String DISABLE_PUSH = "disable-push";
    private static final String DISABLE_SUB = "disable-sub";
    private static final int DISABLE_SUB_ID = 1;
    private static final String DISABLE_SYNC = "disable-sync";
    private static final String INCOMING_SMS_EXCEPTION_PATTERN = "incoming_sms_exception_pattern";
    private static final String INCOMING_SMS_RESTRICTION_PATTERN = "incoming_sms_restriction_pattern";
    private static final String IS_OUTGOING = "isOutgoing";
    private static final String OUTGOING_SMS_EXCEPTION_PATTERN = "outgoing_sms_exception_pattern";
    private static final String OUTGOING_SMS_RESTRICTION_PATTERN = "outgoing_sms_restriction_pattern";
    private static final String PHONE_PACKAGE = "com.android.phone";
    private static final String REMOVE_TYPE = "removeType";
    private static final String SET_PIN_LOCK = "set_pin_lock";
    private static final int SUB0 = 0;
    private static final int SUB1 = 1;
    private static final String TAG = DeviceTelephonyPlugin.class.getSimpleName();
    private static final String TAG_ACTION_DISABLE_AIR_PLANE_MODE = "action_disable_airplane_mode";
    private static final String TAG_ACTION_DISABLE_DATA = "action_disable_data";
    private static final String TAG_ACTION_DISABLE_DATA_4G = "action_disable_data_4G";
    private static final String TAG_ACTION_DISABLE_SUB = "action_disable_sub";

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
        struct.addStruct("disable-sync", PolicyStruct.PolicyType.STATE, new String[]{"value"});
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
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit");
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_TELEPHONY", "have no MDM_TELEPHONY permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.i(TAG, "onSetPolicy");
        boolean result = false;
        if (!checkCallingPermission(who, policyName)) {
            return false;
        }
        char c = 65535;
        boolean z = true;
        switch (policyName.hashCode()) {
            case -1888907677:
                if (policyName.equals("outgoing_week_limit")) {
                    c = 8;
                    break;
                }
                break;
            case -1155809047:
                if (policyName.equals("outgoing_month_limit")) {
                    c = 9;
                    break;
                }
                break;
            case -1032552593:
                if (policyName.equals(DISABLE_DATA)) {
                    c = 1;
                    break;
                }
                break;
            case -1032175905:
                if (policyName.equals(DISABLE_PUSH)) {
                    c = 4;
                    break;
                }
                break;
            case -1032082848:
                if (policyName.equals("disable-sync")) {
                    c = 3;
                    break;
                }
                break;
            case -822171362:
                if (policyName.equals(INCOMING_SMS_RESTRICTION_PATTERN)) {
                    c = 14;
                    break;
                }
                break;
            case -808925998:
                if (policyName.equals(SET_PIN_LOCK)) {
                    c = 5;
                    break;
                }
                break;
            case 34406113:
                if (policyName.equals(INCOMING_SMS_EXCEPTION_PATTERN)) {
                    c = 13;
                    break;
                }
                break;
            case 115663941:
                if (policyName.equals("outgoing_day_limit")) {
                    c = 7;
                    break;
                }
                break;
            case 650237273:
                if (policyName.equals(DISABLE_AIR_PLANE_MODE)) {
                    c = 2;
                    break;
                }
                break;
            case 890761828:
                if (policyName.equals(OUTGOING_SMS_RESTRICTION_PATTERN)) {
                    c = 16;
                    break;
                }
                break;
            case 1195673727:
                if (policyName.equals("incoming_day_limit")) {
                    c = 10;
                    break;
                }
                break;
            case 1352180187:
                if (policyName.equals(DISABLE_SUB)) {
                    c = 0;
                    break;
                }
                break;
            case 1526624617:
                if (policyName.equals("incoming_week_limit")) {
                    c = 11;
                    break;
                }
                break;
            case 1582555559:
                if (policyName.equals(OUTGOING_SMS_EXCEPTION_PATTERN)) {
                    c = 15;
                    break;
                }
                break;
            case 1646476963:
                if (policyName.equals("incoming_month_limit")) {
                    c = 12;
                    break;
                }
                break;
            case 1684123142:
                if (policyName.equals(CHANGE_PIN_CODE)) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                boolean disablesub = policyData.getBoolean("value");
                HwLog.i(TAG, "disablesub: " + disablesub);
                Intent intentDisableSub = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
                intentDisableSub.putExtra("action_tag", TAG_ACTION_DISABLE_SUB);
                intentDisableSub.putExtra("subId", 1);
                intentDisableSub.putExtra("subState", disablesub);
                this.mContext.sendBroadcast(intentDisableSub);
                result = true;
                break;
            case 1:
                boolean disableData = policyData.getBoolean("value");
                Intent intentDisableData4G = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
                intentDisableData4G.putExtra("action_tag", TAG_ACTION_DISABLE_DATA_4G);
                intentDisableData4G.putExtra("subId", 0);
                intentDisableData4G.putExtra("dataState", disableData);
                this.mContext.sendBroadcast(intentDisableData4G);
                result = true;
                break;
            case 2:
                boolean disableAirPlane = policyData.getBoolean("value");
                HwLog.i(TAG, "disableAirPlane: " + disableAirPlane);
                if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
                    z = false;
                }
                boolean isAirplaneModeOn = z;
                HwLog.i(TAG, "isAirplaneModeOn: " + isAirplaneModeOn);
                if (disableAirPlane && isAirplaneModeOn) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                    long ident = Binder.clearCallingIdentity();
                    try {
                        connectivityManager.setAirplaneMode(false);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                Intent intentDisableAirPlane = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
                intentDisableAirPlane.setPackage("com.android.settings");
                intentDisableAirPlane.putExtra("action_tag", TAG_ACTION_DISABLE_AIR_PLANE_MODE);
                intentDisableAirPlane.putExtra("airPlaneState", disableAirPlane);
                this.mContext.sendBroadcast(intentDisableAirPlane);
                result = true;
                break;
            case 3:
            case 4:
                result = true;
                break;
            case 5:
                int subId = policyData.getInt("slotId");
                String password = policyData.getString("password");
                boolean enablePinLock = policyData.getBoolean("pinLockState");
                if (HwTelephonyManagerInner.getDefault() != null) {
                    HwLog.i(TAG, "setPinLock---- enablePinLock: " + enablePinLock + "  subId: " + subId);
                    result = HwTelephonyManagerInner.getDefault().setPinLockEnabled(enablePinLock, password, subId);
                    break;
                }
                break;
            case 6:
                int changPinId = policyData.getInt("slotId");
                String oldPinCode = policyData.getString("oldPinCode");
                String newPinCode = policyData.getString("newPinCode");
                if (HwTelephonyManagerInner.getDefault() != null) {
                    HwLog.i(TAG, "changePinLock----changPinId: " + changPinId);
                    result = HwTelephonyManagerInner.getDefault().changeSimPinCode(oldPinCode, newPinCode, changPinId);
                    break;
                }
                break;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                result = true;
                break;
            case 13:
            case 14:
            case 15:
            case 16:
                result = true;
                break;
        }
        return result;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        char c;
        HwLog.i(TAG, "onRemovePolicy");
        Intent intentDisableSmsLimit = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
        intentDisableSmsLimit.setPackage(PHONE_PACKAGE);
        intentDisableSmsLimit.putExtra(REMOVE_TYPE, "remove_single_policy");
        switch (policyName.hashCode()) {
            case -1888907677:
                if (policyName.equals("outgoing_week_limit")) {
                    c = 1;
                    break;
                }
            case -1155809047:
                if (policyName.equals("outgoing_month_limit")) {
                    c = 2;
                    break;
                }
            case 115663941:
                if (policyName.equals("outgoing_day_limit")) {
                    c = 0;
                    break;
                }
            case 1195673727:
                if (policyName.equals("incoming_day_limit")) {
                    c = 3;
                    break;
                }
            case 1526624617:
                if (policyName.equals("incoming_week_limit")) {
                    c = 4;
                    break;
                }
            case 1646476963:
                if (policyName.equals("incoming_month_limit")) {
                    c = 5;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
                intentDisableSmsLimit.putExtra("time_mode", "day_mode");
                break;
            case 1:
                intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
                intentDisableSmsLimit.putExtra("time_mode", "week_mode");
                break;
            case 2:
                intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
                intentDisableSmsLimit.putExtra("time_mode", "month_mode");
                break;
            case 3:
                intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
                intentDisableSmsLimit.putExtra("time_mode", "day_mode");
                break;
            case 4:
                intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
                intentDisableSmsLimit.putExtra("time_mode", "week_mode");
                break;
            case 5:
                intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
                intentDisableSmsLimit.putExtra("time_mode", "month_mode");
                break;
        }
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intentDisableSmsLimit);
        }
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        boolean isSMSLimitPolicy = false;
        if (removedPolicies == null) {
            HwLog.e(TAG, "removed policied list is null");
            return;
        }
        int removedPoliciesSize = removedPolicies.size();
        for (int i = 0; i < removedPoliciesSize; i++) {
            PolicyStruct.PolicyItem pi = removedPolicies.get(i);
            if (pi != null) {
                String policyName = pi.getPolicyName();
                if (pi.isGlobalPolicyChanged() && isSMSLimitPolicy(policyName)) {
                    isSMSLimitPolicy = true;
                }
            }
        }
        if (isSMSLimitPolicy) {
            Intent intentDisableSmsLimit = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
            intentDisableSmsLimit.setPackage(PHONE_PACKAGE);
            intentDisableSmsLimit.putExtra(REMOVE_TYPE, "remove_all_policy");
            if (this.mContext != null) {
                this.mContext.sendBroadcast(intentDisableSmsLimit);
            }
        }
    }

    private boolean isSMSLimitPolicy(String policyName) {
        return "outgoing_day_limit".equals(policyName) || "outgoing_week_limit".equals(policyName) || "outgoing_month_limit".equals(policyName) || "incoming_day_limit".equals(policyName) || "incoming_week_limit".equals(policyName) || "incoming_month_limit".equals(policyName);
    }
}

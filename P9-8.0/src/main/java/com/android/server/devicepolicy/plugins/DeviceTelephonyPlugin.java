package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManagerInner;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
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
        struct.addStruct(DISABLE_SUB, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_DATA, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_AIR_PLANE_MODE, PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-sync", PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_PUSH, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(SET_PIN_LOCK, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(CHANGE_PIN_CODE, PolicyType.STATE, new String[]{"value"});
        struct.addStruct("outgoing_day_limit", PolicyType.CONFIGURATION, new String[]{"day_mode", "limit_number_day", "day_mode_time"});
        struct.addStruct("outgoing_week_limit", PolicyType.CONFIGURATION, new String[]{"week_mode", "limit_number_week", "week_mode_time"});
        struct.addStruct("outgoing_month_limit", PolicyType.CONFIGURATION, new String[]{"month_mode", "limit_number_month", "month_mode_time"});
        struct.addStruct("incoming_day_limit", PolicyType.CONFIGURATION, new String[]{"day_mode", "limit_number_day", "day_mode_time"});
        struct.addStruct("incoming_week_limit", PolicyType.CONFIGURATION, new String[]{"week_mode", "limit_number_week", "week_mode_time"});
        struct.addStruct("incoming_month_limit", PolicyType.CONFIGURATION, new String[]{"month_mode", "limit_number_month", "month_mode_time"});
        struct.addStruct(INCOMING_SMS_EXCEPTION_PATTERN, PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(INCOMING_SMS_RESTRICTION_PATTERN, PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(OUTGOING_SMS_EXCEPTION_PATTERN, PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(OUTGOING_SMS_RESTRICTION_PATTERN, PolicyType.CONFIGURATION, new String[]{"value"});
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
        if (policyName.equals(DISABLE_SUB)) {
            boolean disablesub = policyData.getBoolean("value");
            HwLog.i(TAG, "disablesub: " + disablesub);
            Intent intentDisableSub = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
            intentDisableSub.putExtra("action_tag", TAG_ACTION_DISABLE_SUB);
            intentDisableSub.putExtra("subId", 1);
            intentDisableSub.putExtra("subState", disablesub);
            this.mContext.sendBroadcast(intentDisableSub);
            result = true;
        } else if (policyName.equals(DISABLE_DATA)) {
            boolean disableData = policyData.getBoolean("value");
            Intent intentDisableData4G = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
            intentDisableData4G.putExtra("action_tag", TAG_ACTION_DISABLE_DATA_4G);
            intentDisableData4G.putExtra("subId", 0);
            intentDisableData4G.putExtra("dataState", disableData);
            this.mContext.sendBroadcast(intentDisableData4G);
            result = true;
        } else if (policyName.equals(DISABLE_AIR_PLANE_MODE)) {
            boolean disableAirPlane = policyData.getBoolean("value");
            HwLog.i(TAG, "disableAirPlane: " + disableAirPlane);
            boolean isAirplaneModeOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
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
        } else if (policyName.equals("disable-sync") || policyName.equals(DISABLE_PUSH)) {
            result = true;
        } else if (policyName.equals(SET_PIN_LOCK)) {
            int subId = policyData.getInt("slotId");
            String password = policyData.getString("password");
            boolean enablePinLock = policyData.getBoolean("pinLockState");
            if (HwTelephonyManagerInner.getDefault() != null) {
                HwLog.i(TAG, "setPinLock---- enablePinLock: " + enablePinLock + "  subId: " + subId);
                result = HwTelephonyManagerInner.getDefault().setPinLockEnabled(enablePinLock, password, subId);
            }
        } else if (policyName.equals(CHANGE_PIN_CODE)) {
            int changPinId = policyData.getInt("slotId");
            String oldPinCode = policyData.getString("oldPinCode");
            String newPinCode = policyData.getString("newPinCode");
            if (HwTelephonyManagerInner.getDefault() != null) {
                HwLog.i(TAG, "changePinLock----changPinId: " + changPinId);
                result = HwTelephonyManagerInner.getDefault().changeSimPinCode(oldPinCode, newPinCode, changPinId);
            }
        } else if (policyName.equals("outgoing_day_limit") || policyName.equals("outgoing_week_limit") || policyName.equals("outgoing_month_limit") || policyName.equals("incoming_day_limit") || policyName.equals("incoming_week_limit") || policyName.equals("incoming_month_limit")) {
            result = true;
        } else if (policyName.equals(INCOMING_SMS_EXCEPTION_PATTERN) || policyName.equals(INCOMING_SMS_RESTRICTION_PATTERN) || policyName.equals(OUTGOING_SMS_EXCEPTION_PATTERN) || policyName.equals(OUTGOING_SMS_RESTRICTION_PATTERN)) {
            result = true;
        }
        return result;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        HwLog.i(TAG, "onRemovePolicy");
        Intent intentDisableSmsLimit = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
        intentDisableSmsLimit.setPackage(PHONE_PACKAGE);
        intentDisableSmsLimit.putExtra(REMOVE_TYPE, "remove_single_policy");
        if (policyName.equals("outgoing_day_limit")) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
            intentDisableSmsLimit.putExtra("time_mode", "day_mode");
        } else if (policyName.equals("outgoing_week_limit")) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
            intentDisableSmsLimit.putExtra("time_mode", "week_mode");
        } else if (policyName.equals("outgoing_month_limit")) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, true);
            intentDisableSmsLimit.putExtra("time_mode", "month_mode");
        } else if (policyName.equals("incoming_day_limit")) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
            intentDisableSmsLimit.putExtra("time_mode", "day_mode");
        } else if (policyName.equals("incoming_week_limit")) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
            intentDisableSmsLimit.putExtra("time_mode", "week_mode");
        } else if (policyName.equals("incoming_month_limit")) {
            intentDisableSmsLimit.putExtra(IS_OUTGOING, false);
            intentDisableSmsLimit.putExtra("time_mode", "month_mode");
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

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        boolean isSMSLimitPolicy = false;
        if (removedPolicies == null) {
            HwLog.e(TAG, "removed policied list is null");
            return;
        }
        int removedPoliciesSize = removedPolicies.size();
        for (int i = 0; i < removedPoliciesSize; i++) {
            PolicyItem pi = (PolicyItem) removedPolicies.get(i);
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
        return ("outgoing_day_limit".equals(policyName) || "outgoing_week_limit".equals(policyName) || "outgoing_month_limit".equals(policyName) || "incoming_day_limit".equals(policyName) || "incoming_week_limit".equals(policyName)) ? true : "incoming_month_limit".equals(policyName);
    }
}

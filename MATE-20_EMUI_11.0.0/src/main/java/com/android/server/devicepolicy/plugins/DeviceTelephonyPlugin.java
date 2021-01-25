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
import com.android.server.devicepolicy.HwDevicePolicyManagerService;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;

public class DeviceTelephonyPlugin extends DevicePolicyPlugin {
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
        struct.addStruct(DISABLE_SUB, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_DATA, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_AIR_PLANE_MODE, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_SYNC, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_PUSH, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(SET_PIN_LOCK, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(CHANGE_PIN_CODE, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("outgoing_day_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"day_mode", "limit_number_day", "day_mode_time"});
        struct.addStruct("outgoing_week_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"week_mode", "limit_number_week", "week_mode_time"});
        struct.addStruct("outgoing_month_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"month_mode", "limit_number_month", "month_mode_time"});
        struct.addStruct("incoming_day_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"day_mode", "limit_number_day", "day_mode_time"});
        struct.addStruct("incoming_week_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"week_mode", "limit_number_week", "week_mode_time"});
        struct.addStruct("incoming_month_limit", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"month_mode", "limit_number_month", "month_mode_time"});
        struct.addStruct(INCOMING_SMS_EXCEPTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(INCOMING_SMS_RESTRICTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(OUTGOING_SMS_EXCEPTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(OUTGOING_SMS_RESTRICTION_PATTERN, PolicyStruct.PolicyType.CONFIGURATION, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DEFAULT_MAIN_SLOT_CARRIER, PolicyStruct.PolicyType.CONFIGURATION, new String[]{SettingsMDMPlugin.STATE_VALUE});
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

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicy");
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return false;
        } else if (!checkCallingPermission(who, policyName)) {
            return false;
        } else {
            char c = 65535;
            boolean isAirplaneModeOn = true;
            switch (policyName.hashCode()) {
                case -1888907677:
                    if (policyName.equals("outgoing_week_limit")) {
                        c = '\b';
                        break;
                    }
                    break;
                case -1155809047:
                    if (policyName.equals("outgoing_month_limit")) {
                        c = '\t';
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
                    if (policyName.equals(DISABLE_SYNC)) {
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
                        c = '\r';
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
                        c = '\n';
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
                        c = '\f';
                        break;
                    }
                    break;
                case 1684123142:
                    if (policyName.equals(CHANGE_PIN_CODE)) {
                        c = 6;
                        break;
                    }
                    break;
                case 2125519871:
                    if (policyName.equals(DEFAULT_MAIN_SLOT_CARRIER)) {
                        c = 17;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    boolean isDisablesub = policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE);
                    HwLog.i(TAG, "disablesub: " + isDisablesub);
                    Intent intentDisableSub = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
                    intentDisableSub.putExtra("action_tag", TAG_ACTION_DISABLE_SUB);
                    intentDisableSub.putExtra("subId", 1);
                    intentDisableSub.putExtra("subState", isDisablesub);
                    this.mContext.sendBroadcast(intentDisableSub);
                    return true;
                case 1:
                    boolean isDisableData = policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE);
                    Intent intentDisableData4G = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
                    intentDisableData4G.putExtra("action_tag", TAG_ACTION_DISABLE_DATA_4G);
                    intentDisableData4G.putExtra("subId", 0);
                    intentDisableData4G.putExtra("dataState", isDisableData);
                    this.mContext.sendBroadcast(intentDisableData4G);
                    return true;
                case 2:
                    boolean isDisableAirPlane = policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE);
                    HwLog.i(TAG, "disableAirPlane: " + isDisableAirPlane);
                    if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
                        isAirplaneModeOn = false;
                    }
                    HwLog.i(TAG, "isAirplaneModeOn: " + isAirplaneModeOn);
                    if (isDisableAirPlane && isAirplaneModeOn) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
                        long ident = Binder.clearCallingIdentity();
                        try {
                            connectivityManager.setAirplaneMode(false);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                    Intent intentDisableAirPlane = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
                    intentDisableAirPlane.setPackage(SettingsMDMPlugin.SETTINGS_APK_NAME);
                    intentDisableAirPlane.putExtra("action_tag", TAG_ACTION_DISABLE_AIR_PLANE_MODE);
                    intentDisableAirPlane.putExtra("airPlaneState", isDisableAirPlane);
                    this.mContext.sendBroadcast(intentDisableAirPlane);
                    return true;
                case 3:
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_DECRYPTING /* 4 */:
                    return true;
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_MISMATCH /* 5 */:
                    int subId = policyData.getInt("slotId");
                    String password = policyData.getString("password");
                    boolean isEnablePinLock = policyData.getBoolean("pinLockState");
                    if (HwTelephonyManagerInner.getDefault() == null) {
                        return false;
                    }
                    HwLog.i(TAG, "setPinLock---- enablePinLock: " + isEnablePinLock + "  subId: " + subId);
                    return HwTelephonyManagerInner.getDefault().setPinLockEnabled(isEnablePinLock, password, subId);
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                    int changPinId = policyData.getInt("slotId");
                    String oldPinCode = policyData.getString("oldPinCode");
                    String newPinCode = policyData.getString("newPinCode");
                    if (HwTelephonyManagerInner.getDefault() == null) {
                        return false;
                    }
                    HwLog.i(TAG, "changePinLock----changPinId: " + changPinId);
                    return HwTelephonyManagerInner.getDefault().changeSimPinCode(oldPinCode, newPinCode, changPinId);
                case 7:
                case '\b':
                case '\t':
                case '\n':
                case 11:
                case '\f':
                    return true;
                case '\r':
                case 14:
                case 15:
                case 16:
                    return true;
                case 17:
                    String carrier = policyData.getString(SettingsMDMPlugin.STATE_VALUE);
                    HwLog.d(TAG, "set the default main slot carrier: " + carrier);
                    if (!TAG_MDM_CARRIER_CMCC.equals(carrier) && !TAG_MDM_CARRIER_CT.equals(carrier)) {
                        return false;
                    }
                    Intent intent = new Intent(HwEmailMDMPlugin.DEVICE_POLICY_ACTION_POLICY_CHANGED);
                    intent.putExtra("action_tag", TAG_ACTION_DEFAULT_MAIN_SLOT_CARRIER);
                    intent.putExtra("carrier", carrier);
                    this.mContext.sendBroadcast(intent);
                    return true;
                default:
                    return false;
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
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

package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DeviceTelephonyManager {
    private static final String CHANGE_PIN_CODE = "change_pin_code";
    public static final String DAY = "day_mode";
    public static final int DAY_MODE = 1;
    public static final String DAY_MODE_TIME = "day_mode_time";
    private static final String DISABLE_AIR_PLANE_MODE = "disable_airplane_mode";
    private static final String DISABLE_DATA = "disable-data";
    private static final String DISABLE_PUSH = "disable-push";
    private static final String DISABLE_SUB = "disable-sub";
    private static final String DISABLE_SYNC = "disable-sync";
    public static final String INCOMING_DAY_LIMIT = "incoming_day_limit";
    public static final String INCOMING_MONTH_LIMIT = "incoming_month_limit";
    private static final String INCOMING_SMS_EXCEPTION_PATTERN = "incoming_sms_exception_pattern";
    private static final String INCOMING_SMS_RESTRICTION_PATTERN = "incoming_sms_restriction_pattern";
    public static final String INCOMING_WEEK_LIMIT = "incoming_week_limit";
    public static final String LIMIT_OF_DAY = "limit_number_day";
    public static final String LIMIT_OF_MONTH = "limit_number_month";
    public static final String LIMIT_OF_WEEK = "limit_number_week";
    private static final int MAX_PIN_CODE_LEN = 8;
    private static final int MIN_PIN_CODE_LEN = 4;
    public static final String MONTH = "month_mode";
    public static final int MONTH_MODE = 3;
    public static final String MONTH_MODE_TIME = "month_mode_time";
    public static final String OUTGOING_DAY_LIMIT = "outgoing_day_limit";
    public static final String OUTGOING_MONTH_LIMIT = "outgoing_month_limit";
    private static final String OUTGOING_SMS_EXCEPTION_PATTERN = "outgoing_sms_exception_pattern";
    private static final String OUTGOING_SMS_RESTRICTION_PATTERN = "outgoing_sms_restriction_pattern";
    public static final String OUTGOING_WEEK_LIMIT = "outgoing_week_limit";
    public static final String POLICY_KEY = "value";
    private static final String SET_PIN_LOCK = "set_pin_lock";
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final String SUB_STATE = "substate";
    private static final String TAG = "DeviceTelephonyManager";
    public static final String TIME_MODE = "time_mode";
    public static final String WEEK = "week_mode";
    public static final int WEEK_MODE = 2;
    public static final String WEEK_MODE_TIME = "week_mode_time";
    private HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setSlot2Disabled(ComponentName admin, boolean allow) {
        Log.d(TAG, "set dual sim active: " + allow);
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", allow);
        return this.mDpm.setPolicy(admin, DISABLE_SUB, bundle);
    }

    public boolean isSlot2Disabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SUB);
        if (bundle == null) {
            return false;
        }
        boolean allow = bundle.getBoolean("value");
        Log.d(TAG, "get dual sim active: " + allow);
        return allow;
    }

    public boolean setSlot2DataConnectivityDisabled(ComponentName admin, boolean allow) {
        Log.d(TAG, "set sub2 data activ: " + allow);
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", allow);
        return this.mDpm.setPolicy(admin, DISABLE_DATA, bundle);
    }

    public boolean isSlot2DataConnectivityDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_DATA);
        if (bundle == null) {
            return false;
        }
        boolean allow = bundle.getBoolean("value");
        Log.d(TAG, "get sub2 data activ: " + allow);
        return allow;
    }

    public boolean setAirplaneModeDisabled(ComponentName admin, boolean allow) {
        Log.d(TAG, "set airplane mode: " + allow);
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", allow);
        return this.mDpm.setPolicy(admin, DISABLE_AIR_PLANE_MODE, bundle);
    }

    public boolean isAirplaneModeDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_AIR_PLANE_MODE);
        if (bundle == null) {
            return false;
        }
        boolean allow = bundle.getBoolean("value");
        Log.d(TAG, "get airplane mode: " + allow);
        return allow;
    }

    public boolean setRoamingSyncDisabled(ComponentName admin, boolean allow) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", allow);
        Log.d(TAG, "setRoamingSyncDisabled: " + allow);
        return this.mDpm.setPolicy(admin, DISABLE_SYNC, bundle);
    }

    public boolean isRoamingSyncDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SYNC);
        Boolean allow = Boolean.valueOf(false);
        if (bundle != null) {
            allow = Boolean.valueOf(bundle.getBoolean("value"));
            Log.d(TAG, "isRoamingSyncDisabled: " + allow);
            return allow.booleanValue();
        }
        Log.d(TAG, "has not set the allow, return default false");
        return allow.booleanValue();
    }

    public boolean setRoamingPushDisabled(ComponentName admin, boolean allow) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", allow);
        Log.d(TAG, "setRoamingPushDisabled: " + allow);
        return this.mDpm.setPolicy(admin, DISABLE_PUSH, bundle);
    }

    public boolean isRoamingPushDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_PUSH);
        Boolean allow = Boolean.valueOf(false);
        if (bundle != null) {
            allow = Boolean.valueOf(bundle.getBoolean("value"));
            Log.d(TAG, "isRoamingPushDisabled: " + allow);
            return allow.booleanValue();
        }
        Log.d(TAG, "has not set the allow, return default false");
        return allow.booleanValue();
    }

    public boolean setSimPinLock(ComponentName admin, boolean enablePinLock, String pinCode, int slotId) {
        if ((slotId < 0 && slotId >= SIM_NUM) || !isValidPinCode(pinCode)) {
            return false;
        }
        Log.d(TAG, "set Pin lock enable.   slotId" + slotId);
        Bundle bundle = new Bundle();
        bundle.putString("password", pinCode);
        bundle.putInt("slotId", slotId);
        bundle.putBoolean("pinLockState", enablePinLock);
        return this.mDpm.setPolicy(admin, SET_PIN_LOCK, bundle);
    }

    public boolean changeSimPinCode(ComponentName admin, String currentPinCode, String newPinCode, int slotId) {
        if ((slotId < 0 && slotId >= SIM_NUM) || !isValidPinCode(currentPinCode) || (isValidPinCode(newPinCode) ^ 1) != 0) {
            return false;
        }
        Log.d(TAG, "change pin code.   slotId" + slotId);
        Bundle bundle = new Bundle();
        bundle.putString("oldPinCode", currentPinCode);
        bundle.putString("newPinCode", newPinCode);
        bundle.putInt("slotId", slotId);
        return this.mDpm.setPolicy(admin, CHANGE_PIN_CODE, bundle);
    }

    private boolean isValidPinCode(String pinCode) {
        if (!TextUtils.isEmpty(pinCode) && pinCode.length() >= 4 && pinCode.length() <= 8) {
            return true;
        }
        return false;
    }

    public boolean setSMSLimitation(ComponentName admin, boolean isOutgoing, int dateType, int limitNumber) {
        if (limitNumber < 0) {
            return false;
        }
        boolean setResult = false;
        switch (dateType) {
            case 1:
                setResult = saveDayMode(isOutgoing, limitNumber, admin);
                break;
            case 2:
                setResult = saveWeekMode(isOutgoing, limitNumber, admin);
                break;
            case 3:
                setResult = saveMonthMode(isOutgoing, limitNumber, admin);
                break;
        }
        Log.d(TAG, "setLimitNumOfSms: " + setResult);
        return setResult;
    }

    public boolean removeSMSLimitation(ComponentName admin, boolean isOutgoing, int dateType) {
        switch (dateType) {
            case 1:
                return removeDayMode(admin, isOutgoing);
            case 2:
                return removeWeekMode(admin, isOutgoing);
            case 3:
                return removeMonthMode(admin, isOutgoing);
            default:
                return false;
        }
    }

    public boolean isSMSLimitationSet(ComponentName admin, boolean isOutgoing) {
        Bundle bundleMonth = this.mDpm.getPolicy(admin, isOutgoing ? "outgoing_month_limit" : "incoming_month_limit");
        if (bundleMonth != null && bundleMonth.getString(MONTH) != null) {
            return true;
        }
        Bundle bundleWeek = this.mDpm.getPolicy(admin, isOutgoing ? "outgoing_week_limit" : "incoming_week_limit");
        if (bundleWeek != null && bundleWeek.getString(WEEK) != null) {
            return true;
        }
        Bundle bundleDay = this.mDpm.getPolicy(admin, isOutgoing ? "outgoing_day_limit" : "incoming_day_limit");
        if (bundleDay == null || bundleDay.getString(DAY) == null) {
            return false;
        }
        return true;
    }

    private boolean removeMonthMode(ComponentName who, boolean isOutgoing) {
        String policyName;
        boolean setResult = false;
        if (isOutgoing) {
            policyName = "outgoing_month_limit";
        } else {
            policyName = "incoming_month_limit";
        }
        if (!TextUtils.isEmpty(policyName)) {
            setResult = this.mDpm.removePolicy(who, policyName, null);
        }
        Log.d(TAG, "removeMonthMode: " + setResult);
        return setResult;
    }

    private boolean removeWeekMode(ComponentName who, boolean isOutgoing) {
        String policyName;
        boolean setResult = false;
        if (isOutgoing) {
            policyName = "outgoing_week_limit";
        } else {
            policyName = "incoming_week_limit";
        }
        if (!TextUtils.isEmpty(policyName)) {
            setResult = this.mDpm.removePolicy(who, policyName, null);
        }
        Log.d(TAG, "removeWeekMode: " + setResult);
        return setResult;
    }

    private boolean removeDayMode(ComponentName who, boolean isOutgoing) {
        String policyName;
        boolean setResult = false;
        if (isOutgoing) {
            policyName = "outgoing_day_limit";
        } else {
            policyName = "incoming_day_limit";
        }
        if (!TextUtils.isEmpty(policyName)) {
            setResult = this.mDpm.removePolicy(who, policyName, null);
        }
        Log.d(TAG, "removeDayMode: " + setResult);
        return setResult;
    }

    private boolean saveMonthMode(boolean isOutgoing, int limitNum, ComponentName who) {
        String policyName;
        boolean setResult = false;
        if (isOutgoing) {
            policyName = "outgoing_month_limit";
        } else {
            policyName = "incoming_month_limit";
        }
        Bundle bundle = new Bundle();
        bundle.putString(MONTH, "true");
        bundle.putString(LIMIT_OF_MONTH, String.valueOf(limitNum));
        bundle.putString(MONTH_MODE_TIME, String.valueOf(System.currentTimeMillis()));
        if (!TextUtils.isEmpty(policyName)) {
            setResult = this.mDpm.setPolicy(who, policyName, bundle);
        }
        Log.d(TAG, "saveMonthMode: " + setResult);
        return setResult;
    }

    private boolean saveWeekMode(boolean isOutgoing, int limitNum, ComponentName who) {
        String policyName;
        boolean setResult = false;
        if (isOutgoing) {
            policyName = "outgoing_week_limit";
        } else {
            policyName = "incoming_week_limit";
        }
        Bundle bundle = new Bundle();
        bundle.putString(WEEK, "true");
        bundle.putString(LIMIT_OF_WEEK, String.valueOf(limitNum));
        bundle.putString(WEEK_MODE_TIME, String.valueOf(System.currentTimeMillis()));
        if (!TextUtils.isEmpty(policyName)) {
            setResult = this.mDpm.setPolicy(who, policyName, bundle);
        }
        Log.d(TAG, "saveWeekMode: " + setResult);
        return setResult;
    }

    private boolean saveDayMode(boolean isOutgoing, int limitNum, ComponentName who) {
        String policyName;
        boolean setResult = false;
        if (isOutgoing) {
            policyName = "outgoing_day_limit";
        } else {
            policyName = "incoming_day_limit";
        }
        Bundle bundle = new Bundle();
        bundle.putString(DAY, "true");
        bundle.putString(LIMIT_OF_DAY, String.valueOf(limitNum));
        bundle.putString(DAY_MODE_TIME, String.valueOf(System.currentTimeMillis()));
        if (!TextUtils.isEmpty(policyName)) {
            setResult = this.mDpm.setPolicy(who, policyName, bundle);
        }
        Log.d(TAG, "saveDayMode: " + setResult);
        return setResult;
    }

    public boolean setIncomingSmsExceptionPattern(ComponentName admin, String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            Log.d(TAG, "remove polciy: incoming_sms_exception_pattern");
            return removePattern(admin, INCOMING_SMS_EXCEPTION_PATTERN);
        }
        Bundle bundle = new Bundle();
        bundle.putString("value", pattern);
        return this.mDpm.setPolicy(admin, INCOMING_SMS_EXCEPTION_PATTERN, bundle);
    }

    public boolean setIncomingSmsRestriction(ComponentName admin, String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            Log.d(TAG, "remove polciy: incoming_sms_restriction_pattern");
            return removePattern(admin, INCOMING_SMS_RESTRICTION_PATTERN);
        }
        Bundle bundle = new Bundle();
        bundle.putString("value", pattern);
        return this.mDpm.setPolicy(admin, INCOMING_SMS_RESTRICTION_PATTERN, bundle);
    }

    public boolean setOutgoingSmsExceptionPattern(ComponentName admin, String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            Log.d(TAG, "remove polciy: outgoing_sms_exception_pattern");
            return removePattern(admin, OUTGOING_SMS_EXCEPTION_PATTERN);
        }
        Bundle bundle = new Bundle();
        bundle.putString("value", pattern);
        return this.mDpm.setPolicy(admin, OUTGOING_SMS_EXCEPTION_PATTERN, bundle);
    }

    public boolean setOutgoingSmsRestriction(ComponentName admin, String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            Log.d(TAG, "remove polciy: outgoing_sms_restriction_pattern");
            return removePattern(admin, OUTGOING_SMS_RESTRICTION_PATTERN);
        }
        Bundle bundle = new Bundle();
        bundle.putString("value", pattern);
        return this.mDpm.setPolicy(admin, OUTGOING_SMS_RESTRICTION_PATTERN, bundle);
    }

    private boolean removePattern(ComponentName admin, String pattern) {
        return this.mDpm.removePolicy(admin, pattern, null);
    }
}

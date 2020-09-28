package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.HwQualcommCsgSearch;
import com.huawei.internal.telephony.PhoneExt;
import huawei.cust.HwCfgFilePolicy;
import java.util.HashMap;

public class HwCustPhoneServiceImpl extends HwCustPhoneService {
    public static final String ACTION_PERMISSION = "com.huawei.phone.permission.DISABLE_2G_CONFIG";
    private static final int EVENT_SET_2G_SWITCH_DONE = 1;
    private static final String HW_NR_ONOFF_MAPPING = "hw_nr_onoff_mapping";
    private static final int INVALID_NETWORK_MODE = -1;
    private static final String LOG_TAG = "HwCustPhoneServiceImpl";
    private static final int OFF_2G_SERVICE = 0;
    private static final int OFF_LTE_SERVICE = 0;
    private static final int OFF_NR_SERVICE = 1;
    private static final int ON_2G_SERVICE = 1;
    private static final int ON_LTE_SERVICE = 1;
    private static final int ON_NR_SERVICE = 0;
    private static final int VALID_LENGTH = 2;
    private static HashMap<Integer, Integer> mNetworkTypeWhen2gOffMapping = new HashMap<>();
    private Context mContext;
    private CustMainHandler mCustMainHandler;
    private HwPhoneService mHwPhoneService;
    private PhoneExt mPhone;

    static {
        mNetworkTypeWhen2gOffMapping.put(3, 2);
        mNetworkTypeWhen2gOffMapping.put(9, 12);
    }

    private class CustMainHandler extends Handler {
        public CustMainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Rlog.d(HwCustPhoneServiceImpl.LOG_TAG, "[PhoneIntfMgr] 2G-Switch EVENT_SET_2G_SWITCH_DONE");
                HwCustPhoneServiceImpl.this.handleSet2GSwitchDown(msg);
            }
        }
    }

    public int getNetworkTypeBaseOnDisabled2G(int networkType) {
        int mappingNetworkType = getOffValueFromMapping(networkType);
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch=getNetworkTypeBaseOnDisabled2G curPrefMode = " + networkType + " ,mappingNetworkType = " + mappingNetworkType);
        return INVALID_NETWORK_MODE != mappingNetworkType ? mappingNetworkType : networkType;
    }

    public HwCustPhoneServiceImpl(HwPhoneService hwPhoneService, Looper looper) {
        this.mHwPhoneService = hwPhoneService;
        this.mCustMainHandler = new CustMainHandler(looper);
    }

    public void setPhone(PhoneExt phone, Context context) {
        this.mPhone = phone;
        this.mContext = context;
        try {
            Settings.System.getInt(this.mContext.getContentResolver(), "disable_2g_visibility_key");
        } catch (Settings.SettingNotFoundException e) {
            Intent intent = new Intent("com.huawei.phone.ACTION_SET_VISIBILITY");
            intent.putExtra("disable_2g_visibility_key", 1);
            this.mContext.sendBroadcast(intent);
        }
        try {
            Settings.System.getInt(this.mContext.getContentResolver(), "disable_2g_default_key");
        } catch (Settings.SettingNotFoundException e2) {
            Intent intent2 = new Intent("com.huawei.phone.ACTION_SET_DEFAULT");
            intent2.putExtra("disable_2g_default_key", 0);
            this.mContext.sendBroadcast(intent2);
        }
        try {
            Settings.System.getInt(this.mContext.getContentResolver(), "disable_2g_key");
        } catch (Settings.SettingNotFoundException e3) {
            send2GServiceSwitchResult(false);
        }
    }

    public boolean isDisable2GServiceCapabilityEnabled() {
        return SystemProperties.getBoolean("ro.config.hw_disable_2g_cap", false);
    }

    public int getOffValueFromMapping(int curPrefMode) {
        if (mNetworkTypeWhen2gOffMapping.containsKey(Integer.valueOf(curPrefMode))) {
            return mNetworkTypeWhen2gOffMapping.get(Integer.valueOf(curPrefMode)).intValue();
        }
        return INVALID_NETWORK_MODE;
    }

    public int get2GServiceAbility() {
        int ability;
        int curPrefMode = getPreferredNetworkModeFromDb();
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= curPrefMode = " + curPrefMode);
        switch (curPrefMode) {
            case 0:
            case 1:
            case 3:
            case 4:
            case HwQualcommCsgSearch.CSGNetworkList.RADIO_IF_UMTS /*{ENCODED_INT: 5}*/:
            case 7:
            case HwQualcommCsgSearch.CSGNetworkList.RADIO_IF_LTE /*{ENCODED_INT: 8}*/:
            case HwQualcommCsgSearch.CSGNetworkList.RADIO_IF_TDSCDMA /*{ENCODED_INT: 9}*/:
            case 10:
            case 16:
            case 17:
            case 18:
            case 20:
            case 21:
            case 22:
                ability = 1;
                break;
            case 2:
            case 6:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 19:
            default:
                ability = 0;
                break;
        }
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= get2GServiceAbility() ability is " + ability);
        return ability;
    }

    public void set2GServiceAbility(int ability) {
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= set2GServiceAbility: ability=" + ability);
        int settingsNetworkMode = getPreferredNetworkModeFromDb();
        int networkMode = settingsNetworkMode;
        int lteServiceAbility = this.mHwPhoneService.getLteServiceAbility();
        if (ability == 0) {
            if (lteServiceAbility == 1) {
                networkMode = 12;
            } else {
                networkMode = 2;
            }
        } else if (ability == 1) {
            if (lteServiceAbility == 1) {
                networkMode = 9;
            } else {
                networkMode = 3;
            }
        }
        if (settingsNetworkMode != networkMode) {
            this.mPhone.setPreferredNetworkType(networkMode, this.mCustMainHandler.obtainMessage(1, Integer.valueOf(networkMode)));
            Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= setPreferredNetworkType-> " + networkMode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSet2GSwitchDown(Message msg) {
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= in handleSet2GSwitchDown");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            Rlog.e(LOG_TAG, "=2G-Switch= set prefer network mode failed!");
            send2GServiceSwitchResult(false);
            return;
        }
        int setPrefMode = ((Integer) ar.userObj).intValue();
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= set preferred network mode database to " + setPrefMode);
        int curPrefMode = getPreferredNetworkModeFromDb();
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= curPrefMode = " + curPrefMode);
        if (curPrefMode != setPrefMode) {
            savePreferredNetworkModeToDb(setPrefMode);
        }
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= set prefer network mode success!");
        send2GServiceSwitchResult(true);
    }

    private void send2GServiceSwitchResult(boolean result) {
        Rlog.d(LOG_TAG, "[PhoneIntfMgr] =2G-Switch= 2G service Switch result is " + result + ". broadcast PREFERRED_2G_SWITCH_DONE");
        if (this.mContext == null) {
            Rlog.e(LOG_TAG, "=2G-Switch= mContext is null. return!");
            return;
        }
        Intent intent = new Intent("com.huawei.telephony.PREF_2G_SWITCH_DONE");
        intent.putExtra("setting_result", result);
        this.mContext.sendBroadcast(intent);
    }

    private int getPreferredNetworkModeFromDb() {
        return HwTelephonyManagerInner.getDefault().getNetworkModeFromDB(this.mPhone.getPhoneId());
    }

    private void savePreferredNetworkModeToDb(int mode) {
        HwTelephonyManagerInner.getDefault().saveNetworkModeToDB(this.mPhone.getPhoneId(), mode);
    }

    public int getNrOnOffMappingNetworkMode(int slotId, int type, int ability) {
        String[] networkModes;
        ServiceState serviceState;
        String networkModeByAbility = (String) HwCfgFilePolicy.getValue(HW_NR_ONOFF_MAPPING, slotId, String.class);
        if (TextUtils.isEmpty(networkModeByAbility) || (networkModes = networkModeByAbility.split(",")) == null || networkModes.length != 2 || (serviceState = TelephonyManager.getDefault().getServiceStateForSubscriber(getSubIdBySlotId(slotId))) == null || serviceState.getRoaming() || type != 1) {
            return INVALID_NETWORK_MODE;
        }
        try {
            return ability == 1 ? Integer.parseInt(networkModes[0].trim()) : Integer.parseInt(networkModes[1].trim());
        } catch (NumberFormatException e) {
            return INVALID_NETWORK_MODE;
        }
    }

    private static int getSubIdBySlotId(int slotId) {
        int[] subIds = SubscriptionManager.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return INVALID_NETWORK_MODE;
        }
        return subIds[0];
    }
}

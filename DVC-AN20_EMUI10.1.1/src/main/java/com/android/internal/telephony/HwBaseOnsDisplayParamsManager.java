package com.android.internal.telephony;

import android.common.HwCfgKey;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.PhoneExt;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwGetCfgFileConfig;

public class HwBaseOnsDisplayParamsManager {
    private static final String EXTRA_SHOW_WIFI = "showWifi";
    private static final String EXTRA_WIFI = "wifi";
    protected static final boolean IS_MULTI_SIM_ENABLED = TelephonyManagerEx.isMultiSimEnabled();
    private static final String KEY_WFC_FORMAT_WIFI_STRING = "wfc_format_wifi_string";
    private static final String KEY_WFC_HIDE_WIFI_BOOL = "wfc_hide_wifi_bool";
    private static final String KEY_WFC_IS_SHOW_AIRPLANE = "wfc_is_show_air_plane";
    private static final String KEY_WFC_IS_SHOW_EMERGENCY_ONLY = "wfc_is_show_emergency_only";
    private static final String KEY_WFC_IS_SHOW_NO_SERVICE = "wfc_is_show_no_service";
    private static final String KEY_WFC_SPN_STRING = "wfc_spn_string";
    private static final String LOG_TAG = "HwBaseOnsDisplayParamsManager";
    private static final int WIFI_IDX = 1;
    protected Context mContext;
    protected ContentResolver mCr;
    protected HwServiceStateTrackerEx mHwServiceStateTrackerEx;
    protected PhoneExt mPhone;
    protected int mPhoneId = this.mPhone.getPhoneId();
    protected IServiceStateTrackerInner mServiceStateTracker;
    protected String mTag = ("HwBaseOnsDisplayParamsManager[" + this.mPhoneId + "]");

    protected HwBaseOnsDisplayParamsManager(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt, HwServiceStateTrackerEx hwServiceStateTrackerEx) {
        this.mServiceStateTracker = serviceStateTracker;
        this.mPhone = phoneExt;
        this.mHwServiceStateTrackerEx = hwServiceStateTrackerEx;
        this.mContext = this.mPhone.getContext();
        this.mCr = this.mContext.getContentResolver();
    }

    /* access modifiers changed from: protected */
    public OnsDisplayParams getOnsDisplayParamsForVoWifi(OnsDisplayParams ons) {
        String formatWifi;
        String combineWifi;
        String combineWifi2;
        int voiceIdx = 0;
        String spnConfiged = "";
        boolean isHideWifi = false;
        String wifiConfiged = "";
        boolean isShowNoService = false;
        boolean isShowEmergency = false;
        boolean isShowAirplane = false;
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader != null) {
            try {
                PersistableBundle bundle = configLoader.getConfigForSubId(this.mPhone.getSubId());
                if (bundle != null) {
                    voiceIdx = bundle.getInt("wfc_spn_format_idx_int");
                    spnConfiged = bundle.getString(KEY_WFC_SPN_STRING);
                    isHideWifi = bundle.getBoolean(KEY_WFC_HIDE_WIFI_BOOL);
                    wifiConfiged = bundle.getString(KEY_WFC_FORMAT_WIFI_STRING);
                    isShowNoService = bundle.getBoolean(KEY_WFC_IS_SHOW_NO_SERVICE);
                    isShowEmergency = bundle.getBoolean(KEY_WFC_IS_SHOW_EMERGENCY_ONLY);
                    isShowAirplane = bundle.getBoolean(KEY_WFC_IS_SHOW_AIRPLANE);
                }
            } catch (Exception e) {
                loge("getOnsDisplayParamsForVoWifi: carrier config error");
            }
        }
        logd("updateSpnDisplay, voiceIdx = " + voiceIdx + " spnConfiged = " + spnConfiged + " isHideWifi = " + isHideWifi + " wifiConfiged = " + wifiConfiged + " isShowNoService = " + isShowNoService + " isShowEmergency = " + isShowEmergency + " isShowAirplane = " + isShowAirplane);
        if (!isHideWifi) {
            boolean isUsingGoogleWifiFormat = voiceIdx == 1;
            String[] wfcSpnFormats = this.mContext.getResources().getStringArray(17236119);
            if (!TextUtils.isEmpty(wifiConfiged)) {
                formatWifi = wifiConfiged;
            } else if (!isUsingGoogleWifiFormat || wfcSpnFormats == null) {
                formatWifi = this.mContext.getResources().getString(17041355);
            } else {
                formatWifi = wfcSpnFormats[1];
            }
        } else {
            formatWifi = "%s";
        }
        boolean isInService = this.mHwServiceStateTrackerEx.getCombinedRegState(this.mServiceStateTracker.getmSSHw()) == 0;
        boolean isNoService = false;
        boolean isEmergencyOnly = false;
        boolean isAirplaneMode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        int combinedRegState = this.mHwServiceStateTrackerEx.getCombinedRegState(this.mServiceStateTracker.getmSSHw());
        if (combinedRegState == 1 || combinedRegState == 2) {
            if (this.mServiceStateTracker.getmSSHw() == null || !this.mServiceStateTracker.getmSSHw().isEmergencyOnly()) {
                isNoService = true;
            } else {
                isEmergencyOnly = true;
            }
        }
        if (!TextUtils.isEmpty(spnConfiged)) {
            combineWifi = spnConfiged;
        } else if (!TextUtils.isEmpty(ons.mSpn)) {
            combineWifi = ons.mSpn;
        } else if (!isInService || TextUtils.isEmpty(ons.mPlmn)) {
            combineWifi = "";
        } else {
            combineWifi = ons.mPlmn;
        }
        if (!isAirplaneMode || !isShowAirplane) {
            combineWifi2 = combineWifi;
            if (isNoService && isShowNoService) {
                combineWifi2 = Resources.getSystem().getText(17040418).toString();
            } else if (isEmergencyOnly && isShowEmergency) {
                combineWifi2 = Resources.getSystem().getText(17040029).toString();
            }
        } else {
            combineWifi2 = Resources.getSystem().getText(17040218).toString();
        }
        try {
            ons.mWifi = String.format(formatWifi, combineWifi2).trim();
            ons.mShowWifi = true;
        } catch (IllegalArgumentException e2) {
            loge("combine wifi fail");
        }
        return getVowifiOnsNameAndStatus(ons, isInService);
    }

    /* access modifiers changed from: protected */
    public OnsDisplayParams getVowifiOnsNameAndStatus(OnsDisplayParams ons, boolean isInService) {
        IServiceStateTrackerInner iServiceStateTrackerInner;
        if (!isInService) {
            return ons;
        }
        int slotId = this.mPhone.getPhoneId();
        Boolean getValueFromCard = (Boolean) HwCfgFilePolicy.getValue("pre_plmn_reg_vowifi", slotId, Boolean.class);
        if (!(getValueFromCard != null && getValueFromCard.booleanValue())) {
            return ons;
        }
        String nitzOperatorName = null;
        if (!(!hasNitzOperatorName(slotId) || (iServiceStateTrackerInner = this.mServiceStateTracker) == null || iServiceStateTrackerInner.getmSSHw() == null)) {
            nitzOperatorName = this.mServiceStateTracker.getmSSHw().getOperatorAlphaLong();
        }
        if (!TextUtils.isEmpty(nitzOperatorName)) {
            ons.mShowPlmn = true;
            ons.mWifi = nitzOperatorName;
        }
        return ons;
    }

    /* access modifiers changed from: protected */
    public boolean hasNitzOperatorName(int slotId) {
        String cardOne = SystemPropertiesEx.get("persist.radio.nitz_hw_name");
        String cardTwo = SystemPropertiesEx.get("persist.radio.nitz_hw_name1");
        if (!IS_MULTI_SIM_ENABLED) {
            return !TextUtils.isEmpty(cardOne);
        }
        if (slotId == 0) {
            return !TextUtils.isEmpty(cardOne);
        }
        if (slotId == 1) {
            return !TextUtils.isEmpty(cardTwo);
        }
        loge("hasNitzOperatorName invalid sub id" + slotId);
        return false;
    }

    public boolean getCarrierConfigPri(int slotId) {
        String regplmn = null;
        IServiceStateTrackerInner iServiceStateTrackerInner = this.mServiceStateTracker;
        if (!(iServiceStateTrackerInner == null || iServiceStateTrackerInner.getmSSHw() == null)) {
            regplmn = this.mServiceStateTracker.getmSSHw().getOperatorNumeric();
        }
        if (TextUtils.isEmpty(regplmn)) {
            return false;
        }
        try {
            Boolean carrerPriority = (Boolean) HwGetCfgFileConfig.getCfgFileData(new HwCfgKey("net_sim_ue_pri", "network_mccmnc", (String) null, (String) null, "network_highest", regplmn, (String) null, (String) null, slotId), Boolean.class);
            if (carrerPriority != null) {
                return carrerPriority.booleanValue();
            }
            return false;
        } catch (IllegalArgumentException e) {
            loge("Exception: read net_sim_ue_pri error in getCarrierConfigPri");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public int getRegState(ServiceState ss) {
        if (ss == null) {
            return 1;
        }
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if (regState != 1 || dataRegState != 0) {
            return regState;
        }
        logd("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    /* access modifiers changed from: protected */
    public ServiceState getSs() {
        return this.mServiceStateTracker.getmSSHw();
    }

    /* access modifiers changed from: protected */
    public ServiceState getNewSS() {
        return this.mServiceStateTracker.getmNewSSHw();
    }

    /* access modifiers changed from: protected */
    public boolean containsPlmn(String plmn, String[] plmnArray) {
        if (plmn == null || plmnArray == null) {
            return false;
        }
        for (String h : plmnArray) {
            if (plmn.equals(h)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void logd(String str) {
        RlogEx.i(this.mTag, str);
    }

    protected static void slogd(String str) {
        RlogEx.d(LOG_TAG, str);
    }

    /* access modifiers changed from: protected */
    public void logi(String str) {
        RlogEx.i(this.mTag, str);
    }

    /* access modifiers changed from: protected */
    public void loge(String str) {
        RlogEx.e(this.mTag, str);
    }
}

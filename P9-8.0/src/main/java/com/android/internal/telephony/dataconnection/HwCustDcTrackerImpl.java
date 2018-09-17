package com.android.internal.telephony.dataconnection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.ArrayUtils;

public class HwCustDcTrackerImpl extends HwCustDcTracker {
    private static final String CUST_ADD_APEC_APN_BUILD = SystemProperties.get("ro.config.add_spec_apn_build", "");
    private static final boolean CUST_ENABLE_OTA_BIP = SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false);
    private static final boolean DBG = true;
    private static final String DISABLED_MOBILE_DATA = "0";
    private static final String DOCOMO_FOTA_APN = "open-dm2.dcm-dm.ne.jp";
    private static final boolean IS_DOCOMO;
    private static final String KOREA_MCC = "450";
    private static final int OPEN_SERVICE_PDP_CREATE_WAIT_MILLIS = 60000;
    private static final int RADIO_TECH_ALL = 0;
    private static final int RADIO_TECH_GU = 255;
    protected static final String SP_HAS_SET_DATA_FEATURE_KEY = "hasSetCustDataFeature";
    private static final String TAG = "HwCustDcTrackerImpl";
    private String LGU_PLMN = "45006";
    private boolean hadSentBoardCastToUI = false;
    DcTracker mDcTracker;
    protected boolean mHwCustMobileDataFeature = false;
    private String mPLMN = "";

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", DISABLED_MOBILE_DATA).equals("341")) {
            equals = SystemProperties.get("ro.config.hw_optb", DISABLED_MOBILE_DATA).equals("392");
        } else {
            equals = false;
        }
        IS_DOCOMO = equals;
    }

    public HwCustDcTrackerImpl(DcTracker dcTracker) {
        super(dcTracker);
        this.mDcTracker = dcTracker;
        updateCustMobileDataFeature();
    }

    public boolean apnRoamingAdjust(DcTracker dcTracker, ApnSetting apnSetting, Phone phone) {
        boolean currentRoaming = phone.getServiceState().getRoaming();
        int radioTech = phone.getServiceState().getRilDataRadioTechnology();
        String operator = dcTracker.getOperatorNumeric();
        String roamingApnStr = System.getString(phone.getContext().getContentResolver(), "hw_customized_roaming_apn");
        Rlog.d(TAG, "[" + phone.getPhoneId() + "]" + "apnRoamingAdjust get hw_customized_roaming_apn: " + roamingApnStr);
        if (TextUtils.isEmpty(roamingApnStr)) {
            return DBG;
        }
        String[] roamingApnList = roamingApnStr.split(";");
        if (roamingApnList.length > 0) {
            for (int i = 0; i < roamingApnList.length; i++) {
                if (!TextUtils.isEmpty(roamingApnList[i])) {
                    String[] roamingApn = roamingApnList[i].split(":");
                    if (4 == roamingApn.length) {
                        String mccmnc = roamingApn[0];
                        String carrier = roamingApn[1];
                        String apn = roamingApn[2];
                        String roaming = roamingApn[3];
                        if (operator.equals(mccmnc) && apnSetting.carrier.equals(carrier) && apnSetting.apn.equals(apn)) {
                            if (roaming.equals(currentRoaming ? "2" : "1") && (apnSetting.bearer == 0 || apnSetting.bearer == radioTech || (14 != radioTech && RADIO_TECH_GU == apnSetting.bearer))) {
                                return DBG;
                            }
                            return false;
                        }
                    }
                    Rlog.d(TAG, "[" + phone.getPhoneId() + "]" + "apnRoamingAdjust got unsuitable configuration " + roamingApnList[i]);
                }
            }
        }
        return DBG;
    }

    private String getLguPlmn() {
        log("getLguPlmn LGU_PLMN is " + this.LGU_PLMN);
        return this.LGU_PLMN;
    }

    public void checkPLMN(String plmn) {
        if (CUST_ENABLE_OTA_BIP) {
            log("checkPLMN plmn = " + plmn);
            String oldPLMN = this.mPLMN;
            this.mPLMN = plmn;
            if (TextUtils.isEmpty(this.mPLMN) || (this.mPLMN.equals(oldPLMN) && this.hadSentBoardCastToUI)) {
                log("checkPLMN newPLMN:" + this.mPLMN + " oldPLMN:" + oldPLMN + " hadSentBoardCastToUI:" + this.hadSentBoardCastToUI);
                return;
            }
            log("checkPLMN mIsPseudoIMSI = " + getmIsPseudoImsi());
            if (getmIsPseudoImsi()) {
                Intent intent;
                if (this.mPLMN.equals(getLguPlmn())) {
                    intent = new Intent();
                    intent.setAction("com.android.telephony.isopencard");
                    this.mDcTracker.mPhone.getContext().sendBroadcast(intent);
                    log("sendbroadcast OTA_ISOPEN_CARD_ACTION");
                    this.hadSentBoardCastToUI = DBG;
                } else if (!this.mPLMN.startsWith(KOREA_MCC)) {
                    intent = new Intent();
                    intent.setAction("com.android.telephony.roamingpseudo");
                    this.mDcTracker.mPhone.getContext().sendBroadcast(intent);
                    log("sendbroadcast ROAMING_PSEUDO_ACTION");
                    this.hadSentBoardCastToUI = DBG;
                }
            }
        }
    }

    public void onOtaAttachFailed(ApnContext apnContext) {
        if (CUST_ENABLE_OTA_BIP) {
            log("onOtaAttachFailed sendbroadcast OTA_OPEN_SERVICE_ACTION, but Phone.OTARESULT.NETWORKFAIL");
            apnContext.setState(State.FAILED);
            this.mDcTracker.mPhone.notifyDataConnection("apnFailed", apnContext.getApnType());
            apnContext.setDataConnectionAc(null);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.open_service_result");
            intent.putExtra("result_code", 1);
            this.mDcTracker.mPhone.getContext().sendBroadcast(intent);
        }
    }

    public boolean getmIsPseudoImsi() {
        boolean isPseudoIMSI = false;
        if (Integer.parseInt(SystemProperties.get("gsm.sim.card.type", "-1")) == 3) {
            isPseudoIMSI = DBG;
        }
        log("getmIsPSendoIms: mIsPseudoIMSI = " + isPseudoIMSI);
        return isPseudoIMSI;
    }

    public void sendOTAAttachTimeoutMsg(ApnContext apnContext, boolean retValue) {
        if (CUST_ENABLE_OTA_BIP && "bip0".equals(apnContext.getApnType()) && retValue) {
            log("trySetupData: open service and setupData return true");
            this.mDcTracker.sendMessageDelayed(this.mDcTracker.obtainMessage(270383, apnContext), 60000);
        }
    }

    public void openServiceStart(UiccController uiccController) {
        if (CUST_ENABLE_OTA_BIP) {
            if (uiccController != null && getmIsPseudoImsi()) {
                UiccCard uiccCard = uiccController.getUiccCard(0);
                if (uiccCard != null) {
                    CatService catService = uiccCard.getCatService();
                    if (catService != null) {
                        catService.onOtaCommand(0);
                        log("onDataSetupComplete: Open Service!");
                    } else {
                        log("onDataSetupComplete:catService is null when Open Service!");
                    }
                }
            }
            if (this.mDcTracker.hasMessages(270383)) {
                this.mDcTracker.removeMessages(270383);
            }
        }
    }

    public String getPlmn() {
        if (CUST_ENABLE_OTA_BIP) {
            return this.mPLMN;
        }
        return null;
    }

    private void log(String string) {
        Rlog.d(TAG, string);
    }

    public boolean isDocomoApn(ApnSetting preferredApn) {
        if (IS_DOCOMO) {
            return preferredApn != null ? "spmode.ne.jp".equals(preferredApn.apn) ^ 1 : DBG;
        } else {
            return false;
        }
    }

    public ApnSetting getDocomoApn(ApnSetting preferredApn) {
        if (!(preferredApn == null || ArrayUtils.contains(preferredApn.types, "dun"))) {
            int len = preferredApn.types.length;
            String[] types = new String[(len + 1)];
            System.arraycopy(preferredApn.types, 0, types, 0, len);
            types[len] = "dun";
            preferredApn.types = types;
        }
        log("getDocomoApn end   preferredApn:" + preferredApn);
        return preferredApn;
    }

    public boolean isCanHandleType(ApnSetting apnSetting, String requestedApnType) {
        if (!IS_DOCOMO || apnSetting == null || !"fota".equalsIgnoreCase(requestedApnType) || (DOCOMO_FOTA_APN.equals(apnSetting.apn) ^ 1) == 0) {
            return DBG;
        }
        return false;
    }

    public boolean isDocomoTetheringApn(ApnSetting apnSetting, String type) {
        if (IS_DOCOMO && apnSetting != null && "dcmtrg.ne.jp".equals(apnSetting.apn) && ("default".equalsIgnoreCase(type) || "supl".equalsIgnoreCase(type))) {
            return DBG;
        }
        return false;
    }

    public boolean hasSetCustDataFeature() {
        return this.mHwCustMobileDataFeature;
    }

    public void updateCustMobileDataFeature() {
        boolean z = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mDcTracker.mPhone.getContext());
        if (sp != null) {
            z = sp.getBoolean(SP_HAS_SET_DATA_FEATURE_KEY, false);
        }
        this.mHwCustMobileDataFeature = z;
    }

    public void setCustDataEnableByHplmn() {
        if (!this.mHwCustMobileDataFeature) {
            if (this.mDcTracker.mPhone == null || this.mDcTracker.mPhone.mIccRecords == null || this.mDcTracker.mPhone.mIccRecords.get() == null) {
                log("setCustDataEnableByHplmn: mPhone or mIccRecords is null.");
                return;
            }
            String simNumeric = ((IccRecords) this.mDcTracker.mPhone.mIccRecords.get()).getOperatorNumeric();
            if (!TextUtils.isEmpty(simNumeric)) {
                int subId = this.mDcTracker.mPhone.getSubId();
                if (SubscriptionManager.isValidSubscriptionId(subId) && this.mDcTracker.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
                    String plmnFeatureConfigs = System.getString(this.mDcTracker.mPhone.getContext().getContentResolver(), "set_data_enable_by_hplmn");
                    if (!TextUtils.isEmpty(plmnFeatureConfigs)) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mDcTracker.mPhone.getContext());
                        if (sp != null) {
                            String[] plmnFeatures = plmnFeatureConfigs.split(";");
                            int length = plmnFeatures.length;
                            int i = 0;
                            while (i < length) {
                                String[] features = plmnFeatures[i].split(",");
                                if (2 > features.length || !simNumeric.equals(features[0])) {
                                    i++;
                                } else {
                                    boolean enable = DISABLED_MOBILE_DATA.equals(features[1]) ? false : DBG;
                                    log("setCustDataEnableByHplmn: simNumeric = " + simNumeric + ", enable = " + enable);
                                    sp.edit().putBoolean(SP_HAS_SET_DATA_FEATURE_KEY, DBG).commit();
                                    this.mHwCustMobileDataFeature = DBG;
                                    this.mDcTracker.setCustUserDataEnabled(enable);
                                    return;
                                }
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                log("setCustDataEnableByHplmn: subid = " + subId + " is invalid, or not dds.");
            }
        }
    }

    public boolean addSpecifiedApnSwitch() {
        if (TextUtils.isEmpty(CUST_ADD_APEC_APN_BUILD)) {
            return false;
        }
        return DBG;
    }

    public boolean addSpecifiedApnToWaitingApns(DcTracker dcTracker, ApnSetting preferredApn, ApnSetting apn) {
        if (CUST_ADD_APEC_APN_BUILD.contains(dcTracker.getOperatorNumeric()) && preferredApn != null && preferredApn.carrier.equals(apn.carrier)) {
            return DBG;
        }
        return false;
    }
}

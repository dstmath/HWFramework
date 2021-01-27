package com.android.internal.telephony.gsm;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.uicc.IccRecords;
import com.huawei.internal.telephony.PhoneExt;
import java.util.HashSet;

public class HwCustGsmServiceStateManagerImpl extends HwCustGsmServiceStateManager {
    private static final String ACTION_RAC_CHANGED = "com.huawei.android.intent.action.RAC_CHANGED";
    private static final int AMERICAN_MCC_BEGIN = 310;
    private static final int AMERICAN_MCC_END = 316;
    private static final boolean IS_HWDBG = true;
    private static final boolean IS_PS_CLEARCODE = SystemProperties.getBoolean("ro.config.hw_clearcode_pdp", false);
    private static final boolean IS_USE_USA_GSMROAMING_CMP = SystemProperties.get("ro.config.USA_gsmroaming_cmp", "false").equals("true");
    private static final boolean IS_VIDEOTRON = ((!"119".equals(SystemProperties.get("ro.config.hw_opta", "0")) || !"124".equals(SystemProperties.get("ro.config.hw_optb", "0"))) ? false : IS_HWDBG);
    private static final String LOG_TAG = "HwCustGsmServiceStateManagerImpl";
    private static final int MCCMNC_LENGTH = 5;
    private static final String[] NET_WORK_PLMN_LIST_ATT = {"310170", "310410", "310560", "311180", "310380", "310030", "310280", "310950", "310150"};
    private static final String[] NET_WORK_PLMN_LIST_TMO = {"310160", "310200", "310210", "310220", "310230", "310240", "310250", "310260", "310270", "310300", "310310", "310490", "310530", "310580", "310590", "310640", "310660", "310800"};
    private static final int PLMN_LENGTH = 3;
    private static final String[] ROAMING_PLMN_LIST_ATT = {"310110", "310140", "310400", "310470", "311170"};
    private static final String[] ROAMING_PLMN_LIST_TMO = {"310470", "310370", "310032", "310140", "310250", "310400", "311170"};
    private static final String ROGERS_MCCMNC = "302720";
    private static final String VIDEOTRON_MCCMNC = "302500,302510,302520";
    private boolean isUSARoamingRuleAffect = false;
    private boolean mIsActualRoaming = false;
    private int mOldRac = -1;
    private int mRac = -1;
    private HashSet<String> mShowNetnameMcc = new HashSet<>();

    public HwCustGsmServiceStateManagerImpl(IServiceStateTrackerInner sst, PhoneExt phoneExt) {
        super(sst, phoneExt);
    }

    public boolean setRoamingStateForOperatorCustomization(ServiceState currentState, boolean isRoamingState) {
        if (currentState == null || currentState.getState() != 0) {
            return isRoamingState;
        }
        String hplmn = null;
        if (!(this.mGsmPhone == null || this.mGsmPhone.getIccRecords() == null)) {
            hplmn = this.mGsmPhone.getIccRecords().getOperatorNumeric();
        }
        String regplmn = currentState.getOperatorNumeric();
        log("Roaming customaziton for vendor hplmn = " + hplmn + "  regplmn=" + regplmn);
        if (hplmn == null || regplmn == null || 5 > regplmn.length()) {
            return isRoamingState;
        }
        if (("302490".equals(hplmn) || "22288".equals(hplmn) || "22201".equals(hplmn)) && "302".equals(regplmn.substring(0, PLMN_LENGTH))) {
            return false;
        }
        return isRoamingState;
    }

    private void log(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public OnsDisplayParams setOnsDisplayCustomization(OnsDisplayParams odp, ServiceState currentState) {
        if (currentState != null && currentState.getState() == 0) {
            String hplmn = null;
            String regplmn = currentState.getOperatorNumeric();
            String spnRes = odp.mSpn;
            IccRecords iccRecords = this.mGsmPhone.getIccRecords();
            if (iccRecords != null) {
                hplmn = iccRecords.getOperatorNumeric();
                if (!TextUtils.isEmpty(iccRecords.getServiceProviderName())) {
                    spnRes = iccRecords.getServiceProviderName();
                }
            }
            Rlog.d(LOG_TAG, "SetOnsDisplayCustomization for vendor hplmn = " + hplmn + "  regplmn=" + regplmn + "  spn=" + spnRes);
            if ("20404".equals(hplmn) && !TextUtils.isEmpty(spnRes) && "ziggo.dataxs.mob".equalsIgnoreCase(spnRes.trim())) {
                odp.mSpn = "Ziggo";
            }
            if (isShowNetnameByMcc(hplmn, regplmn) && !TextUtils.isEmpty(currentState.getOperatorAlphaLong())) {
                odp.mPlmn = currentState.getOperatorAlphaLong();
                odp.mShowPlmn = IS_HWDBG;
                odp.mShowSpn = false;
            }
            if (!TextUtils.isEmpty(regplmn) && regplmn.equals(hplmn) && isShowNetOnly(hplmn)) {
                odp.mShowPlmn = IS_HWDBG;
                odp.mShowSpn = false;
            }
            if (isEplmnShowSpnPlus(hplmn, regplmn, spnRes)) {
                if (!spnRes.contains("+")) {
                    odp.mSpn = spnRes + "+";
                }
                odp.mShowPlmn = false;
                odp.mShowSpn = IS_HWDBG;
            }
            if (isDualLinePlmn(hplmn, regplmn, spnRes)) {
                if (hplmn.equals(regplmn)) {
                    odp.mSpn = "o2 - de " + spnRes;
                } else {
                    odp.mSpn = "o2 - de+ " + spnRes;
                }
                odp.mShowPlmn = false;
                odp.mShowSpn = IS_HWDBG;
            }
        }
        return odp;
    }

    private boolean isShowNetnameByMcc(String hplmn, String regplmn) {
        if (TextUtils.isEmpty(hplmn) || hplmn.length() < PLMN_LENGTH || TextUtils.isEmpty(regplmn) || regplmn.length() < PLMN_LENGTH) {
            return false;
        }
        String hmcc = hplmn.substring(0, PLMN_LENGTH);
        String rmcc = regplmn.substring(0, PLMN_LENGTH);
        if (!isMccForShowNetname(hmcc) || !hmcc.equals(rmcc)) {
            return false;
        }
        return IS_HWDBG;
    }

    private boolean isMccForShowNetname(String currentMccmnc) {
        String strMcc;
        String[] mcc;
        if (this.mShowNetnameMcc.size() == 0 && (strMcc = SettingsEx.Systemex.getString(this.mContext.getContentResolver(), "hw_mcc_show_netname")) != null) {
            for (String str : strMcc.split(",")) {
                this.mShowNetnameMcc.add(str.trim());
            }
        }
        return this.mShowNetnameMcc.contains(currentMccmnc);
    }

    public boolean notUseVirtualName(String imsi) {
        String[] custPlmns;
        if (TextUtils.isEmpty(imsi)) {
            return false;
        }
        String custPlmnsString = Settings.System.getString(this.mContext.getContentResolver(), "hw_notUseVirtualNetCust");
        if (!TextUtils.isEmpty(custPlmnsString)) {
            for (String str : custPlmnsString.split(";")) {
                if (imsi.startsWith(str)) {
                    Rlog.d(LOG_TAG, "Imsi matched,did not use the virtualnets.xml name");
                    return IS_HWDBG;
                }
            }
        }
        return false;
    }

    private boolean isEplmnShowSpnPlus(String hplmn, String rplmn, String spn) {
        String[] custEplmnArray;
        if (TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(spn) || isDualLinePlmn(hplmn, rplmn, spn)) {
            return false;
        }
        String custPlmnString = Settings.System.getString(this.mContext.getContentResolver(), "hw_eplmn_show_spnplus");
        if (!TextUtils.isEmpty(custPlmnString)) {
            for (String str : custPlmnString.split(";")) {
                String[] custEplmnArrayPlmns = str.split(",");
                if (!hplmn.equals(rplmn) && isContained(hplmn, custEplmnArrayPlmns) && isContained(rplmn, custEplmnArrayPlmns)) {
                    Rlog.d(LOG_TAG, "isEplmnShowSpnPlus hplmn:" + hplmn + "|rplmn:" + rplmn);
                    return IS_HWDBG;
                }
            }
        }
        return false;
    }

    private boolean isDualLinePlmn(String hplmn, String rplmn, String spn) {
        if (!"26207".equals(hplmn)) {
            return false;
        }
        if (!"26207".equals(rplmn) && !"26203".equals(rplmn)) {
            return false;
        }
        if (!"Private".equals(spn) && !"Business".equals(spn)) {
            return false;
        }
        Rlog.d(LOG_TAG, "isDualLinePlmn");
        return IS_HWDBG;
    }

    private boolean isContained(String plmn, String[] plmnArray) {
        if (TextUtils.isEmpty(plmn) || plmnArray == null) {
            return false;
        }
        for (String str : plmnArray) {
            if (plmn.equals(str)) {
                return IS_HWDBG;
            }
        }
        return false;
    }

    private boolean isShowNetOnly(String hplmn) {
        String custPlmnString = Settings.System.getString(this.mContext.getContentResolver(), "hw_show_netname_only");
        if (TextUtils.isEmpty(custPlmnString) || TextUtils.isEmpty(hplmn) || !isContained(hplmn, custPlmnString.split(","))) {
            return false;
        }
        Rlog.d(LOG_TAG, "isShowNetOnly hplmn:" + hplmn);
        return IS_HWDBG;
    }

    public void storeModemRoamingStatus(boolean isRoaming) {
        if (IS_VIDEOTRON) {
            this.mIsActualRoaming = isRoaming;
        }
    }

    public OnsDisplayParams getGsmOnsDisplayParamsForVideotron(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        boolean isSpnShowed = isShowSpn;
        boolean isPlmnShowed = isShowPlmn;
        String newSpn = spn;
        int displayRule = rule;
        if (!IS_VIDEOTRON) {
            return null;
        }
        GsmCdmaPhone gsmCdmaPhone = this.mGsmPhone;
        boolean isSstObjectNotNull = IS_HWDBG;
        boolean isPhoneObjectNotNull = (gsmCdmaPhone == null || this.mGsmPhone.getIccRecords() == null) ? false : true;
        if (this.mGsst == null || this.mGsst.mSS == null) {
            isSstObjectNotNull = false;
        }
        if (!isPhoneObjectNotNull || !isSstObjectNotNull) {
            return null;
        }
        String hplmn = this.mGsmPhone.getIccRecords().getOperatorNumericEx(this.mCr, "hw_ons_hplmn_ex");
        String regplmn = this.mGsst.mSS.getOperatorNumeric();
        Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsForVideotron hplmn:" + hplmn + "|regplmn:" + regplmn);
        Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsForVideotron mIsActualRoaming: " + this.mIsActualRoaming + " Roaming: " + this.mGsst.mSS.getRoaming());
        if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(regplmn) && VIDEOTRON_MCCMNC.indexOf(hplmn) != -1 && ROGERS_MCCMNC.equals(regplmn)) {
            if (this.mIsActualRoaming || this.mGsst.mSS.getRoaming()) {
                isSpnShowed = false;
                isPlmnShowed = IS_HWDBG;
                displayRule = 2;
            } else {
                newSpn = "Videotron";
                isSpnShowed = IS_HWDBG;
                isPlmnShowed = false;
                displayRule = 1;
            }
            Rlog.d(LOG_TAG, "getGsmOnsDisplayParamsForVideotron spn:" + newSpn + " showSpn :" + isSpnShowed + " rule:" + displayRule);
        }
        return new OnsDisplayParams(isSpnShowed, isPlmnShowed, displayRule, plmn, newSpn);
    }

    public boolean checkIsInternationalRoaming(boolean isRoaming, ServiceState currentState) {
        boolean isGsmRoaming = isRoaming;
        String simNumeric = null;
        if (IS_USE_USA_GSMROAMING_CMP) {
            Rlog.d(LOG_TAG, "Before checkIsInternationalRoaming, roaming is " + isRoaming);
            if (currentState == null || currentState.getState() != 0 || !isRoaming) {
                this.isUSARoamingRuleAffect = false;
            } else {
                if (this.mGsmPhone.getIccRecords() != null) {
                    simNumeric = this.mGsmPhone.getIccRecords().getOperatorNumeric();
                }
                if (currentState.getOperatorNumeric() == null || simNumeric == null) {
                    Rlog.d(LOG_TAG, "Invalid operatorNumeric or SimNumeric");
                    return isGsmRoaming;
                }
                if (isPlmnOfOperator(simNumeric, NET_WORK_PLMN_LIST_ATT)) {
                    String actingHplmn = this.mGsmPhone.getIccRecords().getActingHplmn();
                    if (actingHplmn != null && !"".equals(actingHplmn)) {
                        Rlog.d(LOG_TAG, "Invalid operatorNumeric or SimNumeric");
                        simNumeric = actingHplmn;
                    }
                    if (isPlmnOfOperator(simNumeric, NET_WORK_PLMN_LIST_ATT) && isAmericanNetwork(currentState.getOperatorNumeric(), ROAMING_PLMN_LIST_ATT)) {
                        isGsmRoaming = false;
                    }
                } else if (isPlmnOfOperator(simNumeric, NET_WORK_PLMN_LIST_TMO) && isAmericanNetwork(currentState.getOperatorNumeric(), ROAMING_PLMN_LIST_TMO)) {
                    isGsmRoaming = false;
                }
                if (!isGsmRoaming) {
                    this.isUSARoamingRuleAffect = IS_HWDBG;
                }
            }
            Rlog.d(LOG_TAG, "After checkIsInternationalRoaming, roaming is " + isGsmRoaming);
        }
        return isGsmRoaming;
    }

    private boolean isPlmnOfOperator(String plmn, String[] operatorPlmnList) {
        if (plmn == null || operatorPlmnList == null) {
            return false;
        }
        for (String opreatorPlmn : operatorPlmnList) {
            if (plmn.equals(opreatorPlmn)) {
                return IS_HWDBG;
            }
        }
        return false;
    }

    private boolean isAmericanNetwork(String plmn, String[] roamingPlmnList) {
        boolean isAmericanNetwork = false;
        if (plmn == null || roamingPlmnList == null) {
            return false;
        }
        if (plmn.length() >= 5) {
            int americanMCC = Integer.parseInt(plmn.substring(0, PLMN_LENGTH));
            if (americanMCC < AMERICAN_MCC_BEGIN || americanMCC > AMERICAN_MCC_END) {
                isAmericanNetwork = false;
            } else {
                isAmericanNetwork = IS_HWDBG;
            }
        }
        if (!isAmericanNetwork) {
            return isAmericanNetwork;
        }
        for (String roamingPlmn : roamingPlmnList) {
            if (plmn.equals(roamingPlmn)) {
                return false;
            }
        }
        return isAmericanNetwork;
    }

    public boolean iscustRoamingRuleAffect(boolean isRoaming) {
        boolean isMatchRoamingRule = false;
        if (IS_USE_USA_GSMROAMING_CMP && !isRoaming) {
            isMatchRoamingRule = this.isUSARoamingRuleAffect;
        }
        Rlog.d("GsmServiceStateTracker", "isMatchRoamingRule: " + isMatchRoamingRule);
        return isMatchRoamingRule;
    }

    public IntentFilter getCustIntentFilter(IntentFilter filter) {
        if (IS_PS_CLEARCODE) {
            filter.addAction(ACTION_RAC_CHANGED);
        }
        return filter;
    }

    public int handleBroadcastReceived(Context context, Intent intent, int rac) {
        if (!IS_PS_CLEARCODE || intent == null || !ACTION_RAC_CHANGED.equals(intent.getAction())) {
            return rac;
        }
        this.mRac = ((Integer) intent.getExtra("newRat", -1)).intValue();
        int i = this.mRac;
        if (i == -1 || this.mOldRac == i) {
            return rac;
        }
        log("CLEARCODE mRac = " + this.mRac + " , mOldRac = " + this.mOldRac);
        this.mOldRac = this.mRac;
        return this.mRac;
    }

    public boolean skipPlmnUpdateFromCust() {
        return SystemProperties.getBoolean("ro.config.display_pnn_name", false);
    }
}

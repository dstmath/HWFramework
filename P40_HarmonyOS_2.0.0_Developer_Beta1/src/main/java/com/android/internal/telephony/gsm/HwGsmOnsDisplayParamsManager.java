package com.android.internal.telephony.gsm;

import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.CustPlmnMember;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwBaseOnsDisplayParamsManager;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPlmnActConcat;
import com.android.internal.telephony.HwServiceStateTrackerEx;
import com.android.internal.telephony.IServiceStateTrackerInner;
import com.android.internal.telephony.OnsDisplayParams;
import com.android.internal.telephony.PlmnConstants;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnReminderEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class HwGsmOnsDisplayParamsManager extends HwBaseOnsDisplayParamsManager {
    private static final String CHINAMOBILE_MCCMNC = "46000;46002;46007;46008;46004";
    private static final String CHINA_TELECOM_SPN = "%E4%B8%AD%E5%9B%BD%E7%94%B5%E4%BF%A1";
    private static final int CUST_ICCID_INDEX = 0;
    private static final int CUST_SPEC_LENGTH = 2;
    private static final int CUST_SPN_INDEX = 1;
    private static final String DEFAULT_SPN = "####";
    private static final String EMERGENCY_PLMN = Resources.getSystem().getText(HwPartResourceUtils.getResourceId("emergency_calls_only")).toString();
    private static final int HPLMN_MIN_LENGTH = 2;
    private static final int INVALID_NUMBER = -1;
    private static final String LOG_TAG = "HwGsmOnsDisplayParamsManager";
    private static final int MNC_MCC_LENGTH = 3;
    private static final String NO_SERVICE_PLMN = Resources.getSystem().getText(HwPartResourceUtils.getResourceId("lockscreen_carrier_default")).toString();
    private static final String ONE_SPACE_STRING = "";
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final String REGEX = "((\\d{5,14},\\d{5,14},[^:,;]{1,20};)){1,}$";
    private static final int ROAMING_RULE_NOT_ROAM = 2;
    private static final int ROAMING_RULE_ROAM = 1;
    private static final int SPN_RULE_SHOW_BOTH = 3;
    private static final int SPN_RULE_SHOW_NITZNAME_PRIOR = 6;
    private static final int SPN_RULE_SHOW_PLMN_ONLY = 2;
    private static final int SPN_RULE_SHOW_PNN_PRIOR = 4;
    private static final int SPN_RULE_SHOW_SIM_ONLY = 0;
    private static final int SPN_RULE_SHOW_SPN_ONLY = 1;
    private static final int SPN_RULE_SHOW_SPN_PRIOR = 5;
    private HwCustGsmServiceStateManager mHwCustGsmServiceStateManager;

    public HwGsmOnsDisplayParamsManager(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt, HwServiceStateTrackerEx hwServiceStateTrackerEx) {
        super(serviceStateTracker, phoneExt, hwServiceStateTrackerEx);
        this.mTag = "HwGsmOnsDisplayParamsManager[" + this.mPhoneId + "]";
        this.mHwCustGsmServiceStateManager = (HwCustGsmServiceStateManager) HwCustUtils.createObj(HwCustGsmServiceStateManager.class, new Object[]{serviceStateTracker, this.mPhone});
    }

    public OnsDisplayParams getOnsDisplayParamsHw(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        OnsDisplayParams odp = getOnsDisplayParamsBySpnOnly(isShowSpn, isShowPlmn, rule, plmn, spn);
        OnsDisplayParams odpTemp = new OnsDisplayParams(odp.mShowSpn, odp.mShowPlmn, odp.mRule, odp.mPlmn, odp.mSpn);
        if (this.mPhone.getIccRecords() != null) {
            CustPlmnMember cpm = CustPlmnMember.getInstance();
            String hplmn = this.mPhone.getIccRecords().getOperatorNumericEx(this.mCr, "hw_ons_hplmn_ex");
            int slotId = SubscriptionManager.getSlotIndex(this.mPhone.getSubId());
            String regplmn = getSs().getOperatorNumeric();
            String custSpn = Settings.System.getString(this.mCr, "hw_plmn_spn");
            if (custSpn != null) {
                logd("custSpn length =" + custSpn.length());
            }
            getOnsByCpm(cpm, hplmn, custSpn, plmn, regplmn, odpTemp);
            getOnsByCfgCust(cpm, hplmn, plmn, regplmn, slotId, odpTemp);
        }
        OnsDisplayParams odp2 = getOdpByCust(odp, odpTemp);
        setShowWifiByOdp(odp2);
        setSpnAndRuleByOdp(odp2);
        setOperatorNameByPlmnOrSpn(odp2);
        return odp2;
    }

    private OnsDisplayParams getOnsDisplayParamsBySpnOnly(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        String plmnRes = plmn;
        String spnRes = spn;
        int mRule = rule;
        boolean isShowSpnTemp = isShowSpn;
        boolean isShowPlmnTemp = isShowPlmn;
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        if (iccRecords == null) {
            return new OnsDisplayParams(isShowSpnTemp, isShowPlmnTemp, mRule, plmnRes, spnRes);
        }
        String homePlmn = iccRecords.getOperatorNumeric();
        String iccid = iccRecords.getIccId();
        String regPlmn = getSs().getOperatorNumeric();
        String spnSim = iccRecords.getServiceProviderName();
        int ruleSim = this.mServiceStateTracker.getCarrierNameDisplayBitmask(getSs());
        String spnByIccid = getCustSpnByIccid(iccid, spnSim);
        logd("SpnOnly spn:" + spnSim + ",hplmn:" + homePlmn + ",regPlmn:" + regPlmn);
        if (!TextUtils.isEmpty(spnSim) && isMccForSpn(iccRecords.getOperatorNumeric()) && !TextUtils.isEmpty(homePlmn)) {
            if (homePlmn.length() > 3) {
                String currentMcc = homePlmn.substring(0, 3);
                if (!TextUtils.isEmpty(regPlmn) && regPlmn.length() > 3 && currentMcc.equals(regPlmn.substring(0, 3))) {
                    isShowSpnTemp = true;
                    isShowPlmnTemp = false;
                    spnRes = spnSim;
                    mRule = ruleSim;
                    plmnRes = "";
                }
            }
        }
        if (!TextUtils.isEmpty(spnByIccid) && getRegState(getSs()) == 0) {
            isShowSpnTemp = true;
            isShowPlmnTemp = false;
            mRule = 1;
            spnRes = DEFAULT_SPN.equals(spnByIccid) ? spnSim : spnByIccid;
        }
        return new OnsDisplayParams(isShowSpnTemp, isShowPlmnTemp, mRule, plmnRes, spnRes);
    }

    private String getCustSpnByIccid(String iccid, String spn) {
        if (TextUtils.isEmpty(iccid)) {
            return null;
        }
        String specIccidSpnList = Settings.System.getString(this.mContext.getContentResolver(), "specIccidSpnList");
        if (TextUtils.isEmpty(specIccidSpnList)) {
            return null;
        }
        String spnCust = null;
        String[] custArrays = specIccidSpnList.trim().split(";");
        int length = custArrays.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String[] items = custArrays[i].split(":");
            if (items.length == 2 && !TextUtils.isEmpty(items[0]) && iccid.startsWith(items[0])) {
                spnCust = items[1];
                break;
            }
            i++;
        }
        if (TextUtils.isEmpty(spnCust)) {
            return null;
        }
        if (!TextUtils.isEmpty(spn) || !DEFAULT_SPN.equals(spnCust)) {
            return spnCust;
        }
        return null;
    }

    private String getSpnFromTempPlmn(CustPlmnMember cpm, String spn, String tempPlmn, String hplmn, String regplmn) {
        if (cpm.rule != 1 || !TextUtils.isEmpty(spn)) {
            return spn;
        }
        logd(" want to show spnTemp while spnTemp is null,use plmn instead " + tempPlmn);
        if (TextUtils.isEmpty(this.mPhone.getIccRecords().getServiceProviderName())) {
            return getDefaultSpn(tempPlmn, hplmn, regplmn);
        }
        return tempPlmn;
    }

    private void getOnsByCpm(CustPlmnMember cpm, String hplmn, String custSpn, String tempPlmn, String regplmn, OnsDisplayParams odpTemp) {
        if (cpm.acquireFromCust(hplmn, getSs(), custSpn)) {
            odpTemp.mShowSpn = cpm.judgeShowSpn(odpTemp.mShowSpn);
            odpTemp.mShowPlmn = cpm.rule == 0 ? odpTemp.mShowPlmn : cpm.mIsShowPlmn;
            odpTemp.mRule = cpm.rule == 0 ? odpTemp.mRule : cpm.rule;
            odpTemp.mPlmn = cpm.judgePlmn(odpTemp.mPlmn);
            odpTemp.mSpn = cpm.judgeSpn(odpTemp.mSpn);
            odpTemp.mSpn = getSpnFromTempPlmn(cpm, odpTemp.mSpn, tempPlmn, hplmn, regplmn);
            if (cpm.rule == 4) {
                syncRuleByPlmnAndRule(getEonsWithoutCphs(), odpTemp);
                boolean z = true;
                odpTemp.mShowSpn = (odpTemp.mRule & 1) == 1;
                if ((odpTemp.mRule & 2) != 2) {
                    z = false;
                }
                odpTemp.mShowPlmn = z;
            }
            if (cpm.rule == 5) {
                OnsDisplayParams odprule5 = getGsmOnsDisplayParamsSpnPrior(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
                odpTemp.mShowSpn = odprule5.mShowSpn;
                odpTemp.mShowPlmn = odprule5.mShowPlmn;
                odpTemp.mRule = odprule5.mRule;
                odpTemp.mPlmn = odprule5.mPlmn;
                odpTemp.mSpn = odprule5.mSpn;
            }
            logd("showSpn2 =" + odpTemp.mShowSpn + " showPlmn2 =" + odpTemp.mShowPlmn + " spn2 =" + odpTemp.mSpn + " plmn2 =" + odpTemp.mPlmn);
        }
    }

    private void getOnsByCfgCust(CustPlmnMember cpm, String hplmn, String tempPlmn, String regplmn, int slotId, OnsDisplayParams odpTemp) {
        if (cpm.getCfgCustDisplayParams(hplmn, getSs(), "plmn_spn", slotId)) {
            odpTemp.mRule = (cpm.rule == 0 || cpm.rule == 6) ? odpTemp.mRule : cpm.rule;
            odpTemp.mPlmn = DEFAULT_SPN.equals(cpm.plmn) ? odpTemp.mPlmn : cpm.plmn;
            odpTemp.mSpn = DEFAULT_SPN.equals(cpm.spn) ? odpTemp.mSpn : cpm.spn;
            odpTemp.mSpn = getSpnFromTempPlmn(cpm, odpTemp.mSpn, tempPlmn, hplmn, regplmn);
            if (cpm.rule == 4) {
                String temPnn = null;
                IccRecordsEx iccRecords = this.mPhone.getIccRecords();
                if (iccRecords != null && !iccRecords.isEonsDisabled()) {
                    logd("getEons():get plmn from SIM card! ");
                    if (updateEons(iccRecords)) {
                        temPnn = iccRecords.getEons();
                    }
                }
                logd("temPnn = " + temPnn);
                syncRuleByPlmnAndRule(temPnn, odpTemp);
            }
            if (cpm.rule == 6) {
                getGsmOnsDisplayParamsNitzNamePrior(odpTemp);
            }
            boolean z = true;
            odpTemp.mShowSpn = (odpTemp.mRule & 1) == 1;
            if ((odpTemp.mRule & 2) != 2) {
                z = false;
            }
            odpTemp.mShowPlmn = z;
            logd("getCfgCustDisplayParams showSpn=" + odpTemp.mShowSpn + " showPlmn=" + odpTemp.mShowPlmn + " spn=" + odpTemp.mSpn + " plmn=" + odpTemp.mPlmn + " rule=" + odpTemp.mRule);
        }
    }

    private OnsDisplayParams getOdpByCust(OnsDisplayParams onsDisplayParams, OnsDisplayParams odpTemp) {
        OnsDisplayParams onsDisplayParamsTemp;
        OnsDisplayParams odpCust = getGsmOnsDisplayParamsBySpecialCust(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        OnsDisplayParams odpForChinaOperator = getGsmOnsDisplayParamsForChinaOperator(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        OnsDisplayParams odpForGeneralOperator = null;
        OnsDisplayParams odpSpnCust = getGsmOnsDisplayParamsBySpnCust(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        OnsDisplayParams odpCustForVideotron = null;
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager = this.mHwCustGsmServiceStateManager;
        if (hwCustGsmServiceStateManager != null) {
            odpForGeneralOperator = hwCustGsmServiceStateManager.getGsmOnsDisplayParamsForGlobalOperator(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
            odpCustForVideotron = this.mHwCustGsmServiceStateManager.getGsmOnsDisplayParamsForVideotron(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        }
        if (odpCust != null) {
            onsDisplayParamsTemp = odpCust;
        } else if (odpSpnCust != null) {
            onsDisplayParamsTemp = odpSpnCust;
        } else if (odpForChinaOperator != null) {
            onsDisplayParamsTemp = odpForChinaOperator;
        } else if (odpForGeneralOperator != null) {
            onsDisplayParamsTemp = odpForGeneralOperator;
        } else if (odpCustForVideotron != null) {
            onsDisplayParamsTemp = odpCustForVideotron;
        } else {
            onsDisplayParamsTemp = new OnsDisplayParams(odpTemp.mShowSpn, odpTemp.mShowPlmn, odpTemp.mRule, odpTemp.mPlmn, odpTemp.mSpn);
        }
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager2 = this.mHwCustGsmServiceStateManager;
        if (hwCustGsmServiceStateManager2 != null) {
            onsDisplayParamsTemp = hwCustGsmServiceStateManager2.setOnsDisplayCustomization(onsDisplayParamsTemp, getSs());
        }
        if (this.mPhone.isWifiCallingEnabled()) {
            return getOnsDisplayParamsForVoWifi(onsDisplayParamsTemp);
        }
        return onsDisplayParamsTemp;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsBySpnCust(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        String spnRslt;
        int ruleRslt;
        boolean isShowPlmnRslt;
        boolean isShowSpnRslt;
        boolean isMatched;
        String spnRslt2 = spn;
        boolean isShowPlmnRslt2 = isShowPlmn;
        boolean isShowSpnRslt2 = isShowSpn;
        int ruleRslt2 = rule;
        boolean isMatched2 = true;
        String regPlmn = getSs().getOperatorNumeric();
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        if (iccRecords != null) {
            String hplmn = iccRecords.getOperatorNumeric();
            String spnSim = iccRecords.getServiceProviderName();
            if (!"732130".equals(hplmn) || TextUtils.isEmpty(spnSim) || (!"732103".equals(regPlmn) && !"732111".equals(regPlmn) && !"732123".equals(regPlmn) && !"732101".equals(regPlmn))) {
                isMatched2 = false;
            } else {
                isShowSpnRslt2 = true;
                isShowPlmnRslt2 = false;
                ruleRslt2 = 1;
                spnRslt2 = spnSim;
            }
            spnRslt = spnRslt2;
            isShowPlmnRslt = isShowPlmnRslt2;
            isShowSpnRslt = isShowSpnRslt2;
            ruleRslt = ruleRslt2;
            isMatched = isMatched2;
        } else {
            spnRslt = spnRslt2;
            isShowPlmnRslt = isShowPlmnRslt2;
            isShowSpnRslt = isShowSpnRslt2;
            ruleRslt = ruleRslt2;
            isMatched = false;
        }
        if (isMatched) {
            return new OnsDisplayParams(isShowSpnRslt, isShowPlmnRslt, ruleRslt, plmn, spnRslt);
        }
        return null;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsBySpecialCust(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        OnsDisplayParams odpRslt;
        int ruleRslt;
        boolean isShowSpnResult;
        boolean isShowPlmnResult;
        String plmnRslt;
        String spnRslt;
        boolean isShowSpnResult2;
        boolean isShowPlmnResult2;
        boolean isShowPlmnResult3;
        boolean isShowSpnResult3;
        String plmnRslt2;
        String plmnRslt3 = plmn;
        String spnRslt2 = spn;
        int ruleRslt2 = rule;
        boolean isMatched = true;
        String regPlmn = getSs().getOperatorNumeric();
        int netType = ServiceStateEx.getVoiceNetworkType(getSs());
        int netClass = TelephonyManagerEx.getNetworkClass(netType);
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        if (iccRecords != null) {
            String hplmn = iccRecords.getOperatorNumeric();
            String spnSim = iccRecords.getServiceProviderName();
            boolean isShowPlmnResult4 = isShowPlmn;
            int ruleSim = this.mServiceStateTracker.getCarrierNameDisplayBitmask(getSs());
            StringBuilder sb = new StringBuilder();
            boolean isShowSpnResult4 = isShowSpn;
            sb.append("regPlmn = ");
            sb.append(regPlmn);
            sb.append(",hplmn = ");
            sb.append(hplmn);
            sb.append(",spnSim = ");
            sb.append(spnSim);
            sb.append(",ruleSim = ");
            sb.append(ruleSim);
            sb.append(",netType = ");
            sb.append(netType);
            sb.append(",netClass = ");
            sb.append(netClass);
            logd(sb.toString());
            boolean z = false;
            odpRslt = null;
            if ("21405".equals(hplmn) && "21407".equals(regPlmn) && "tuenti".equalsIgnoreCase(spnSim)) {
                String pnnName = getEons(plmn);
                if (!TextUtils.isEmpty(spnSim)) {
                    spnRslt2 = spnSim;
                    isShowSpnResult3 = (ruleSim & 1) == 1;
                } else {
                    isShowSpnResult3 = isShowSpnResult4;
                }
                if (!TextUtils.isEmpty(pnnName)) {
                    plmnRslt2 = pnnName;
                    if ((ruleSim & 2) == 2) {
                        z = true;
                    }
                    isShowPlmnResult4 = z;
                } else {
                    plmnRslt2 = plmnRslt3;
                }
                isShowSpnResult2 = isShowSpnResult3;
                isShowPlmnResult2 = isShowPlmnResult4;
                plmnRslt3 = plmnRslt2;
            } else if ("21407".equals(hplmn) && "21407".equals(regPlmn)) {
                String pnnName2 = getEons(plmn);
                if (!TextUtils.isEmpty(spnSim)) {
                    spnRslt2 = spnSim;
                    isShowSpnResult4 = (ruleRslt2 & 1) == 1;
                }
                if (!TextUtils.isEmpty(pnnName2)) {
                    plmnRslt3 = pnnName2;
                    if ((ruleRslt2 & 2) == 2) {
                        z = true;
                    }
                    isShowPlmnResult4 = z;
                }
                isShowPlmnResult2 = isShowPlmnResult4;
                isShowSpnResult2 = isShowSpnResult4;
            } else if ("23420".equals(hplmn) && this.mHwServiceStateTrackerEx.getCombinedRegState(getSs()) == 0) {
                String pnnName3 = getEons(plmn);
                if (!TextUtils.isEmpty(pnnName3)) {
                    spnRslt2 = spnSim;
                    plmnRslt3 = pnnName3;
                    isShowPlmnResult3 = true;
                    ruleRslt2 = 2;
                    isShowSpnResult4 = false;
                } else {
                    isShowPlmnResult3 = isShowPlmnResult4;
                }
                isShowPlmnResult2 = isShowPlmnResult3;
                isShowSpnResult2 = isShowSpnResult4;
            } else if ((!"74000".equals(hplmn) || !"74000".equals(regPlmn) || !plmn.equals(spn)) && (!"45006".equals(hplmn) || !"45006".equals(regPlmn) || !"LG U+".equals(plmn))) {
                if (!"732187".equals(hplmn) || (!"732103".equals(regPlmn) && !"732111".equals(regPlmn))) {
                    if (!"50218".equals(hplmn) || !"50212".equals(regPlmn)) {
                        if ("334050".equals(hplmn) || "334090".equals(hplmn) || "33405".equals(hplmn)) {
                            if (TextUtils.isEmpty(spnSim) && (("334050".equals(regPlmn) || "334090".equals(regPlmn)) && !TextUtils.isEmpty(plmnRslt3) && (plmnRslt3.startsWith("Iusacell") || plmnRslt3.startsWith("Nextel")))) {
                                logd("AT&T some card has no pnn and spn, then want it to be treated as AT&T");
                                plmnRslt3 = "AT&T";
                            }
                            if (!TextUtils.isEmpty(plmnRslt3) && plmnRslt3.startsWith("AT&T")) {
                                if (netClass == 1) {
                                    plmnRslt3 = "AT&T EDGE";
                                    isShowPlmnResult2 = isShowPlmnResult4;
                                    isShowSpnResult2 = isShowSpnResult4;
                                } else if (netClass == 2) {
                                    plmnRslt3 = "AT&T";
                                    isShowPlmnResult2 = isShowPlmnResult4;
                                    isShowSpnResult2 = isShowSpnResult4;
                                } else if (netClass == 3) {
                                    plmnRslt3 = "AT&T 4G";
                                    isShowPlmnResult2 = isShowPlmnResult4;
                                    isShowSpnResult2 = isShowSpnResult4;
                                }
                            }
                        } else if (isDocomoTablet()) {
                            if (TextUtils.isEmpty(spnRslt2)) {
                                spnRslt2 = spnSim;
                            }
                            if (this.mHwServiceStateTrackerEx.getCombinedRegState(getSs()) == 0 && !TextUtils.isEmpty(spnRslt2) && (rule & 1) == 1) {
                                z = true;
                            }
                            isShowSpnResult2 = z;
                            logd("getGsmOnsDisplayParamsBySpecialCust: spn = " + spnRslt2 + ", isShowSpn = " + isShowSpnResult2);
                            isShowPlmnResult2 = isShowPlmnResult4;
                        } else {
                            isMatched = false;
                            isShowPlmnResult2 = isShowPlmnResult4;
                            isShowSpnResult2 = isShowSpnResult4;
                        }
                    } else if (is2g3gNetwork(netClass)) {
                        isShowSpnResult2 = true;
                        ruleRslt2 = 1;
                        plmnRslt3 = "U Mobile";
                        spnRslt2 = "U Mobile";
                        isShowPlmnResult2 = false;
                    } else if (netClass == 3) {
                        isShowSpnResult2 = true;
                        ruleRslt2 = 1;
                        plmnRslt3 = "MY MAXIS";
                        spnRslt2 = "MY MAXIS";
                        isShowPlmnResult2 = false;
                    }
                } else if (is2g3gNetwork(netClass)) {
                    plmnRslt3 = "ETB";
                    isShowPlmnResult2 = isShowPlmnResult4;
                    isShowSpnResult2 = isShowSpnResult4;
                } else if (netClass == 3) {
                    plmnRslt3 = "ETB 4G";
                    isShowPlmnResult2 = isShowPlmnResult4;
                    isShowSpnResult2 = isShowSpnResult4;
                }
                isShowPlmnResult2 = isShowPlmnResult4;
                isShowSpnResult2 = isShowSpnResult4;
            } else {
                ruleRslt2 = 2;
                isShowPlmnResult2 = true;
                isShowSpnResult2 = false;
            }
            plmnRslt = plmnRslt3;
            isShowPlmnResult = isShowPlmnResult2;
            isShowSpnResult = isShowSpnResult2;
            ruleRslt = ruleRslt2;
            spnRslt = spnRslt2;
        } else {
            odpRslt = null;
            isMatched = false;
            plmnRslt = plmnRslt3;
            ruleRslt = ruleRslt2;
            isShowPlmnResult = isShowPlmn;
            isShowSpnResult = isShowSpn;
            spnRslt = spnRslt2;
        }
        logd("isMatched = " + isMatched + ",isShowPlmnResult = " + isShowPlmnResult + ",isShowSpnResult = " + isShowSpnResult + ",ruleRslt = " + ruleRslt + ",plmnRslt = " + plmnRslt + ",spnRslt = " + spnRslt);
        if (isMatched) {
            return new OnsDisplayParams(isShowSpnResult, isShowPlmnResult, ruleRslt, plmnRslt, spnRslt);
        }
        return odpRslt;
    }

    private boolean is2g3gNetwork(int netClass) {
        return netClass == 1 || netClass == 2;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsForChinaOperator(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        String regplmn;
        String hplmn;
        String spnTemp = spn;
        if (this.mPhone.getIccRecords() != null) {
            hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            regplmn = getSs().getOperatorNumeric();
        } else {
            hplmn = null;
            regplmn = null;
        }
        logd("isShowSpnTemp:" + isShowSpn + ",isShowPlmnTemp:" + isShowPlmn + ",rule:" + rule + ",plmn:" + plmn + ",spnTemp:" + spnTemp + ",hplmn:" + hplmn + ",regplmn:" + regplmn);
        if (TextUtils.isEmpty(regplmn)) {
            return null;
        }
        if (!isChinaMobileMccMnc()) {
            if (HwTelephonyManagerInner.getDefault().isCDMASimCard(this.mPhoneId) || HwTelephonyManagerInner.getDefault().isCTSimCard(this.mPhoneId)) {
                if (!getSs().getRoaming()) {
                    logd("In not roaming condition just show plmn without spnTemp.");
                    return new OnsDisplayParams(false, true, rule, plmn, spnTemp);
                } else if (HwTelephonyManagerInner.getDefault().isCTSimCard(this.mPhoneId)) {
                    if (EMERGENCY_PLMN.equals(plmn) || NO_SERVICE_PLMN.equals(plmn)) {
                        logd("out of service or emergency.");
                        return new OnsDisplayParams(isShowSpn, isShowPlmn, rule, plmn, spnTemp);
                    }
                    if (TextUtils.isEmpty(spnTemp)) {
                        logd("spnTemp is null.");
                        try {
                            spnTemp = URLDecoder.decode(CHINA_TELECOM_SPN, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            loge("UnsupportedEncodingException.");
                        }
                    }
                    return new OnsDisplayParams(true, true, rule, plmn, spnTemp);
                }
            }
            if (!HuaweiTelephonyConfigs.isChinaTelecom() && (!getSs().getRoaming() || TextUtils.isEmpty(hplmn) || !"20404".equals(hplmn) || !"20404".equals(regplmn) || (HwTelephonyManager.getDefault().getSpecCardType(this.mPhone.getPhoneId()) & 15) != 3)) {
                return null;
            }
            logd("In China Telecom, just show plmn without spnTemp.");
            return new OnsDisplayParams(false, true, rule, plmn, spnTemp);
        } else if (spnTemp == null || "".equals(spnTemp) || "CMCC".equals(spnTemp) || "China Mobile".equals(spnTemp)) {
            logd("chinamobile just show plmn without spnTemp.");
            return new OnsDisplayParams(false, true, rule, plmn, spnTemp);
        } else {
            logd("third party provider sim cust just show original rule.");
            return new OnsDisplayParams(isShowSpn, isShowPlmn, rule, plmn, spnTemp);
        }
    }

    private void setShowWifiByOdp(OnsDisplayParams odp) {
        this.mHwServiceStateTrackerEx.setVowifi(odp.mShowWifi, odp.mWifi);
    }

    private void setSpnAndRuleByOdp(OnsDisplayParams odp) {
        if (TextUtils.isEmpty(odp.mSpn) && odp.mRule == 3) {
            logd("Show plmn and spn while spn is null, show plmn only !");
            odp.mShowSpn = false;
            odp.mRule = 2;
        }
    }

    private void setOperatorNameByPlmnOrSpn(OnsDisplayParams odp) {
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager;
        String networkNameShow = null;
        if (odp.mShowPlmn) {
            networkNameShow = odp.mPlmn;
        } else if (odp.mShowSpn) {
            networkNameShow = odp.mSpn;
        }
        if (!TextUtils.isEmpty(networkNameShow)) {
            if ((getSs() != null && (ServiceStateEx.getDataState(getSs()) == 0 || ServiceStateEx.getVoiceRegState(getSs()) == 0)) && (hwCustGsmServiceStateManager = this.mHwCustGsmServiceStateManager) != null && !hwCustGsmServiceStateManager.skipPlmnUpdateFromCust()) {
                logd("before setprop:" + getSs().getOperatorAlphaLong());
                getSs().setOperatorName(networkNameShow, getSs().getOperatorAlphaShort(), getSs().getOperatorNumeric());
                logd("after setprop:" + getSs().getOperatorAlphaLong());
            }
        }
    }

    private boolean isMccForSpn(String currentMccmnc) {
        String[] mccs;
        String strMcc = Settings.System.getString(this.mContext.getContentResolver(), "hw_mcc_showspn_only");
        Set<String> showSpnOnlyMccs = new HashSet<>();
        if (currentMccmnc == null || currentMccmnc.length() < 3) {
            return false;
        }
        String currentMcc = currentMccmnc.substring(0, 3);
        if (strMcc == null || showSpnOnlyMccs.size() != 0) {
            return false;
        }
        for (String str : strMcc.split(",")) {
            showSpnOnlyMccs.add(str.trim());
        }
        return showSpnOnlyMccs.contains(currentMcc);
    }

    private String getDefaultSpn(String spn, String hplmn, String regplmn) {
        if (TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(regplmn)) {
            return spn;
        }
        String defaultSpnString = Settings.System.getString(this.mCr, "hw_spnnull_defaultspn");
        if (TextUtils.isEmpty(defaultSpnString) || !Pattern.matches(REGEX, defaultSpnString)) {
            return spn;
        }
        for (String defaultSpnItem : defaultSpnString.split(";")) {
            String[] defaultSpns = defaultSpnItem.split(",");
            if (hplmn.equals(defaultSpns[0]) && regplmn.equals(defaultSpns[1])) {
                logd("defaultspn is not null,use defaultspn instead " + defaultSpns[2]);
                return defaultSpns[2];
            }
        }
        return spn;
    }

    private String getEonsWithoutCphs() {
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        if (iccRecords == null || iccRecords.isEonsDisabled()) {
            return null;
        }
        logd("getEonsWithoutCphs():get plmn from SIM card! ");
        if (updateEons(iccRecords)) {
            return iccRecords.getEons();
        }
        return null;
    }

    private void syncRuleByPlmnAndRule(String temPnn, OnsDisplayParams odpTemp) {
        logd("temPnn = " + temPnn);
        boolean isPnnEmpty = false;
        if (TextUtils.isEmpty(temPnn)) {
            isPnnEmpty = true;
        }
        if (!isPnnEmpty || TextUtils.isEmpty(odpTemp.mSpn)) {
            odpTemp.mRule = 2;
            if (!isPnnEmpty) {
                odpTemp.mPlmn = temPnn;
                return;
            }
            return;
        }
        logd("want to show PNN while PNN is null, show SPN instead ");
        odpTemp.mRule = 1;
    }

    private OnsDisplayParams getGsmOnsDisplayParamsSpnPrior(boolean isShowSpn, boolean isShowPlmn, int rule, String plmn, String spn) {
        String plmnRes = plmn;
        String spnRes = spn;
        String temPnn = null;
        int newRule = 1;
        String cardspn = this.mPhone.getIccRecords() != null ? this.mPhone.getIccRecords().getServiceProviderName() : null;
        if (!TextUtils.isEmpty(cardspn)) {
            newRule = 1;
            spnRes = cardspn;
        } else {
            temPnn = getEonsWithoutCphs();
            if (!TextUtils.isEmpty(temPnn)) {
                newRule = 2;
                plmnRes = temPnn;
            }
        }
        boolean isShowPlmnRes = false;
        boolean isShowSpnRes = (newRule & 1) == 1;
        if ((newRule & 2) == 2) {
            isShowPlmnRes = true;
        }
        logd("getGsmOnsDisplayParamsSpnPrior: cardspn= " + cardspn + " temPnn= " + temPnn + " newRule= " + newRule);
        return new OnsDisplayParams(isShowSpnRes, isShowPlmnRes, newRule, plmnRes, spnRes);
    }

    private void getGsmOnsDisplayParamsNitzNamePrior(OnsDisplayParams odpOri) {
        int slotId = this.mPhone.getPhoneId();
        logd("getGsmOnsDisplayParamsNitzNamePrior hasNitzOperatorName = " + hasNitzOperatorName(slotId));
        if (hasNitzOperatorName(slotId)) {
            odpOri.mPlmn = getSs().getOperatorAlphaLong();
            if (odpOri.mRule == 1) {
                logd("sim rule is 1 and network has value, show network plmn pri");
                odpOri.mRule = 2;
                odpOri.mShowSpn = false;
                odpOri.mShowPlmn = true;
            }
        }
    }

    private boolean isChinaMobileMccMnc() {
        String hplmn = null;
        String regplmn = null;
        if (this.mPhone.getIccRecords() != null) {
            hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            regplmn = getSs().getOperatorNumeric();
        }
        String[] mccMncLists = CHINAMOBILE_MCCMNC.split(";");
        boolean isRegplmnCmcc = false;
        boolean isHplmnCmcc = false;
        if (TextUtils.isEmpty(regplmn) || TextUtils.isEmpty(hplmn)) {
            return false;
        }
        for (int i = 0; i < mccMncLists.length; i++) {
            if (mccMncLists[i].equals(regplmn)) {
                isRegplmnCmcc = true;
            }
            if (mccMncLists[i].equals(hplmn)) {
                isHplmnCmcc = true;
            }
        }
        if (!isRegplmnCmcc || !isHplmnCmcc) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0148  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x014f  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0170  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01ad  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x01be  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x01df  */
    /* JADX WARNING: Removed duplicated region for block: B:65:? A[RETURN, SYNTHETIC] */
    public String getGsmPlmn() {
        String data;
        String plmnValue;
        String imsi;
        String hplmn;
        boolean isPopupApnEmpty;
        String plmnValue2;
        String str;
        if (this.mHwServiceStateTrackerEx.getCombinedRegState(getSs()) != 0) {
            return null;
        }
        String operatorNumeric = getSs().getOperatorNumeric();
        try {
            data = Settings.System.getString(this.mCr, "plmn");
        } catch (Exception e) {
            loge("Exception when got data value");
            data = null;
        }
        PlmnConstants plmnConstants = new PlmnConstants(data);
        String languageCode = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
        String plmnValue3 = plmnConstants.getPlmnValue(operatorNumeric, languageCode);
        logd("getPlmn languageCode:" + languageCode + "  plmnValue:" + plmnValue3);
        if (plmnValue3 == null) {
            String plmnValue4 = plmnConstants.getPlmnValue(operatorNumeric, "en_us");
            logd("get default en_us plmn name:" + plmnValue4);
            plmnValue = plmnValue4;
        } else {
            plmnValue = plmnValue3;
        }
        int slotId = this.mPhone.getPhoneId();
        logd("slotId = " + slotId);
        if (this.mPhone.getIccRecords() != null) {
            hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            imsi = this.mPhone.getIccRecords().getIMSI();
        } else {
            hplmn = null;
            imsi = null;
        }
        logd("hplmn = " + hplmn);
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager = this.mHwCustGsmServiceStateManager;
        boolean isHasNitzOperatorName = true;
        boolean isUseVirtualName = hwCustGsmServiceStateManager != null && hwCustGsmServiceStateManager.notUseVirtualName(imsi);
        boolean roaming = getSs().getRoaming();
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager2 = this.mHwCustGsmServiceStateManager;
        boolean isMatchRoamingRule = hwCustGsmServiceStateManager2 != null && hwCustGsmServiceStateManager2.iscustRoamingRuleAffect(roaming);
        if (!isUseVirtualName) {
            if (!isMatchRoamingRule) {
                plmnValue = getVirCarrierOperatorName(plmnValue, roaming, hasNitzOperatorName(slotId), slotId, hplmn);
                logd("VirtualNetName = " + plmnValue);
                if (!IS_MULTI_SIM_ENABLED) {
                    isPopupApnEmpty = ApnReminderEx.isPopupApnSettingsEmpty(this.mContext, slotId);
                } else {
                    isPopupApnEmpty = ApnReminderEx.isPopupApnSettingsEmpty(this.mContext);
                }
                if (isPopupApnEmpty || getSs().getRoaming() || hasNitzOperatorName(slotId) || hplmn == null) {
                    isHasNitzOperatorName = false;
                }
                if (isHasNitzOperatorName) {
                    int apnId = getPreferedApnId();
                    if (apnId != -1) {
                        if (IS_MULTI_SIM_ENABLED) {
                            str = ApnReminderEx.getOnsNameByPreferedApn(this.mContext, slotId, apnId, plmnValue);
                        } else {
                            str = ApnReminderEx.getOnsNameByPreferedApn(this.mContext, apnId, plmnValue);
                        }
                        plmnValue = str;
                        logd("apnReminder plmnValue = " + plmnValue);
                    } else {
                        plmnValue = null;
                    }
                }
                plmnValue2 = getGsmOnsDisplayPlmnByAbbrevPriority(getGsmOnsDisplayPlmnByPriority(plmnValue, slotId), slotId);
                if (!TextUtils.isEmpty(plmnValue2)) {
                    plmnValue2 = getVirtualNetPlmnValue(operatorNumeric, hplmn, imsi, getEons(getSs().getOperatorAlphaLong()));
                } else {
                    ServiceStateEx.setOperatorAlphaLong(getSs(), plmnValue2);
                }
                logd("plmnValue = " + plmnValue2);
                if (!HwPlmnActConcat.needPlmnActConcat()) {
                    return HwPlmnActConcat.getPlmnActConcat(plmnValue2, getSs());
                }
                return plmnValue2;
            }
        }
        logd("passed the Virtualnet cust");
        if (!IS_MULTI_SIM_ENABLED) {
        }
        isHasNitzOperatorName = false;
        if (isHasNitzOperatorName) {
        }
        plmnValue2 = getGsmOnsDisplayPlmnByAbbrevPriority(getGsmOnsDisplayPlmnByPriority(plmnValue, slotId), slotId);
        if (!TextUtils.isEmpty(plmnValue2)) {
        }
        logd("plmnValue = " + plmnValue2);
        if (!HwPlmnActConcat.needPlmnActConcat()) {
        }
    }

    private String getVirCarrierOperatorName(String plmnValue, boolean isRoaming, boolean isHasNitzOperatorName, int slotId, String hplmn) {
        if (isRoaming || hplmn == null || isHasNitzOperatorName) {
            return plmnValue;
        }
        String custplmn = (String) HwCfgFilePolicy.getValue("virtualnet_operatorname", slotId, String.class);
        if (TextUtils.isEmpty(custplmn)) {
            return plmnValue;
        }
        logd("getVirCarrierOperatorName: plmnValue = " + plmnValue + " custplmn = " + custplmn);
        return custplmn;
    }

    private int getPreferedApnId() {
        Cursor cursor;
        int apnId = -1;
        if (IS_MULTI_SIM_ENABLED) {
            cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) this.mPhone.getPhoneId()), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        } else {
            cursor = this.mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        }
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            String carrierName = cursor.getString(cursor.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME));
            logd("getPreferedApnId: " + apnId + ", apn: " + apnName + ", name: " + carrierName);
        }
        if (cursor != null) {
            cursor.close();
        }
        return apnId;
    }

    private String getGsmOnsDisplayPlmnByPriority(String custPlmnValue, int slotId) {
        String result;
        boolean isHwNetworkSimUePriority = SystemPropertiesEx.getBoolean("ro.config.net_sim_ue_pri", false);
        boolean configNetworkSimUePriority = getCarrierConfigPri(slotId);
        boolean custHplmnRegplmn = enablePlmnByNetSimUePriority();
        if (!isHwNetworkSimUePriority && !custHplmnRegplmn && !configNetworkSimUePriority) {
            return custPlmnValue;
        }
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        String spnSim = null;
        if (iccRecords != null) {
            spnSim = iccRecords.getServiceProviderName();
        }
        if (hasNitzOperatorName(slotId)) {
            getEons(custPlmnValue);
            result = getSs().getOperatorAlphaLong();
        } else {
            String result2 = getSs().getOperatorAlphaLong();
            if (custPlmnValue != null) {
                result2 = custPlmnValue;
            }
            result = getEons(result2);
            if ((custHplmnRegplmn || configNetworkSimUePriority) && !TextUtils.isEmpty(spnSim)) {
                result = spnSim;
            }
        }
        logd("plmnValue = " + result + " slotId = " + slotId + " custPlmnValue = " + custPlmnValue);
        return result;
    }

    private String getGsmOnsDisplayPlmnByAbbrevPriority(String custPlmnValue, int slotId) {
        String result;
        String plmnAbbrev = null;
        String custPlmn = Settings.System.getString(this.mCr, "hw_plmn_abbrev");
        if (this.mPhone.getIccRecords() != null) {
            CustPlmnMember cpm = CustPlmnMember.getInstance();
            String hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            if (cpm.acquireFromCust(hplmn, getSs(), custPlmn)) {
                plmnAbbrev = cpm.plmn;
                logd(" plmn2 =" + plmnAbbrev);
            }
            if (cpm.getCfgCustDisplayParams(hplmn, getSs(), "plmn_abbrev", slotId)) {
                plmnAbbrev = cpm.plmn;
                logd("HwCfgFile: plmn =" + plmnAbbrev);
            }
        }
        if (TextUtils.isEmpty(plmnAbbrev)) {
            return custPlmnValue;
        }
        if (hasNitzOperatorName(slotId)) {
            result = getEons(getSs().getOperatorAlphaLong());
        } else {
            result = getEons(plmnAbbrev);
        }
        logd("result = " + result + " slotId = " + slotId + " PlmnValue = " + custPlmnValue);
        return result;
    }

    private String getEons(String defaultValue) {
        if (HwModemCapability.isCapabilitySupport(5)) {
            return defaultValue;
        }
        String result = null;
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        if (iccRecords != null && !iccRecords.isEonsDisabled()) {
            logd("getEons():get plmn from SIM card! ");
            if (updateEons(iccRecords)) {
                result = iccRecords.getEons();
            }
        } else if (iccRecords != null && iccRecords.isEonsDisabled()) {
            String hplmn = iccRecords.getOperatorNumeric();
            String regplmn = getSs().getOperatorNumeric();
            if (!(hplmn == null || !hplmn.equals(regplmn) || iccRecords.getEons() == null)) {
                logd("getEons():get plmn from Cphs when register to hplmn ");
                result = iccRecords.getEons();
            }
        }
        logd("result = " + result);
        return TextUtils.isEmpty(result) ? defaultValue : result;
    }

    private boolean updateEons(IccRecordsEx r) {
        int lac = -1;
        if (this.mServiceStateTracker.getCellLocationInfo() instanceof GsmCellLocation) {
            lac = ((GsmCellLocation) this.mServiceStateTracker.getCellLocationInfo()).getLac();
        }
        if (r != null) {
            return r.updateEons(getSs().getOperatorNumeric(), lac);
        }
        return false;
    }

    private String getVirtualNetPlmnValue(String operatorNumeric, String hplmn, String imsi, String plmnValue) {
        if (hasNitzOperatorName(this.mPhone.getPhoneId())) {
            return plmnValue;
        }
        if ("22299".equals(operatorNumeric)) {
            if (hplmn == null) {
                return "";
            }
            if (imsi != null && imsi.startsWith("222998")) {
                return "";
            }
        }
        if ("22201".equals(operatorNumeric)) {
            if (hplmn == null) {
                return "";
            }
            IccRecordsEx iccRecords = this.mPhone.getIccRecords();
            String spnSim = null;
            if (iccRecords != null) {
                spnSim = iccRecords.getServiceProviderName();
            }
            if ("Coop Mobile".equals(spnSim) && imsi != null && imsi.startsWith("22201")) {
                return "";
            }
        }
        return plmnValue;
    }

    private boolean enablePlmnByNetSimUePriority() {
        String hplmn = null;
        String regplmn = null;
        boolean isCustHplmnRegplmn = false;
        if (this.mPhone.getIccRecords() != null) {
            hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            regplmn = getSs().getOperatorNumeric();
        }
        String custNetSimUePriority = Settings.System.getString(this.mCr, "hw_net_sim_ue_pri");
        if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(regplmn) && !TextUtils.isEmpty(custNetSimUePriority)) {
            String[] custmccmncs = custNetSimUePriority.split(";");
            int length = custmccmncs.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String[] mccmncs = custmccmncs[i].split(",");
                if (hplmn.equals(mccmncs[0]) && regplmn.equals(mccmncs[1])) {
                    isCustHplmnRegplmn = true;
                    break;
                }
                i++;
            }
        } else {
            isCustHplmnRegplmn = false;
            logd("enablePlmnByNetSimUePriority() failed, priority or hplmm or regplmn is empty");
        }
        logd(" cust_hplmn_equal_regplmn = " + isCustHplmnRegplmn);
        return isCustHplmnRegplmn;
    }

    public boolean getRoamingStateHw(boolean roaming) {
        boolean roamingTemp = roaming;
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager = this.mHwCustGsmServiceStateManager;
        if (hwCustGsmServiceStateManager != null) {
            hwCustGsmServiceStateManager.storeModemRoamingStatus(roamingTemp);
        }
        boolean isCTCardRegGSM = false;
        if (roamingTemp) {
            String hplmn = null;
            if (this.mPhone.getIccRecords() != null) {
                hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            }
            String regplmn = getNewSS().getOperatorNumeric();
            String regplmnCustomString = null;
            if (getNoRoamingByMcc(getNewSS())) {
                roamingTemp = false;
            }
            try {
                regplmnCustomString = Settings.System.getString(this.mContext.getContentResolver(), "reg_plmn_custom");
                logd("handlePollStateResult plmnCustomString = " + regplmnCustomString);
            } catch (Exception e) {
                loge("Exception when got name value");
            }
            if (regplmnCustomString != null) {
                String[] regplmnCustomArrays = regplmnCustomString.split(";");
                if (!TextUtils.isEmpty(hplmn) && !TextUtils.isEmpty(regplmn)) {
                    int regplmnCustomArrayLen = regplmnCustomArrays.length;
                    int i = 0;
                    while (true) {
                        if (i >= regplmnCustomArrayLen) {
                            break;
                        }
                        String[] regplmnCustomArrEleBufs = regplmnCustomArrays[i].split(",");
                        if (containsPlmn(hplmn, regplmnCustomArrEleBufs) && containsPlmn(regplmn, regplmnCustomArrEleBufs)) {
                            roamingTemp = false;
                            break;
                        }
                        i++;
                    }
                } else {
                    roamingTemp = false;
                }
            }
        }
        logd("roamingTemp = " + roamingTemp);
        boolean isRegGsmOrWcdma = ServiceStateEx.isGsm(ServiceStateEx.getRilVoiceRadioTechnology(getNewSS())) && !ServiceStateEx.isLte(ServiceStateEx.getRilVoiceRadioTechnology(getNewSS())) && ServiceStateEx.getRilVoiceRadioTechnology(getNewSS()) != 20;
        if (HwTelephonyManagerInner.getDefault().isCTSimCard(this.mPhone.getPhoneId()) && getNewSS().getState() == 0 && isRegGsmOrWcdma) {
            isCTCardRegGSM = true;
        }
        if (isCTCardRegGSM) {
            roamingTemp = true;
            logd("When CT card register in GSM/UMTS, it always should be roamingTemptrue");
        }
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager2 = this.mHwCustGsmServiceStateManager;
        if (hwCustGsmServiceStateManager2 != null) {
            roamingTemp = hwCustGsmServiceStateManager2.setRoamingStateForOperatorCustomization(getNewSS(), roamingTemp);
            logd("roamingTemp customization for MCC 302 roamingTemp=" + roamingTemp);
        }
        boolean roamingTemp2 = getGsmRoamingSpecialCustByNetType(getGsmRoamingCustByIMSIStart(roamingTemp));
        HwCustGsmServiceStateManager hwCustGsmServiceStateManager3 = this.mHwCustGsmServiceStateManager;
        if (hwCustGsmServiceStateManager3 != null) {
            return hwCustGsmServiceStateManager3.checkIsInternationalRoaming(roamingTemp2, getNewSS());
        }
        return roamingTemp2;
    }

    private boolean getNoRoamingByMcc(ServiceState mSS) {
        IccRecordsEx iccRecords = this.mPhone.getIccRecords();
        if (!(iccRecords == null || mSS == null)) {
            String hplmn = iccRecords.getOperatorNumeric();
            String regplmn = mSS.getOperatorNumeric();
            if (isMccForNoRoaming(hplmn)) {
                String currentMcc = hplmn.substring(0, 3);
                if (regplmn != null && regplmn.length() > 3 && currentMcc.equals(regplmn.substring(0, 3))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMccForNoRoaming(String currentMccmnc) {
        String[] mccs;
        String strMcc = Settings.System.getString(this.mContext.getContentResolver(), "hw_mcc_show_no_roaming");
        Set<String> showNoRoamingMccs = new HashSet<>();
        if (currentMccmnc == null || currentMccmnc.length() < 3) {
            return false;
        }
        String currentMcc = currentMccmnc.substring(0, 3);
        if (strMcc == null || showNoRoamingMccs.size() != 0) {
            return false;
        }
        for (String str : strMcc.split(",")) {
            showNoRoamingMccs.add(str.trim());
        }
        return showNoRoamingMccs.contains(currentMcc);
    }

    private boolean getGsmRoamingCustByIMSIStart(boolean roaming) {
        int netType;
        String regplmnRoamCustomString;
        int ruleRoam;
        String regplmnRoamCustomString2 = null;
        if (this.mPhone.getIccRecords() != null) {
            String hplmn = this.mPhone.getIccRecords().getOperatorNumericEx(this.mCr, "hw_roam_hplmn_ex");
            String regplmn = getNewSS().getOperatorNumeric();
            int netType2 = ServiceStateEx.getVoiceNetworkType(getNewSS());
            int netClass = TelephonyManagerEx.getNetworkClass(netType2);
            logd("hplmn=" + hplmn + "  regplmn=" + regplmn + "  netType=" + netType2 + "  netClass=" + netClass);
            try {
                regplmnRoamCustomString2 = Settings.System.getString(this.mCr, "reg_plmn_roam_custom");
            } catch (Exception e) {
                loge("Exception when got reg_plmn_roam_custom value");
            }
            if (regplmnRoamCustomString2 == null || hplmn == null || regplmn == null) {
                return roaming;
            }
            String[] rules = regplmnRoamCustomString2.split(";");
            int length = rules.length;
            String hplmnMcc = null;
            int i = 0;
            boolean roaming2 = roaming;
            while (i < length) {
                String[] rulePlmnRoams = rules[i].split(":");
                if (rulePlmnRoams.length == 2) {
                    try {
                        ruleRoam = Integer.parseInt(rulePlmnRoams[0]);
                    } catch (NumberFormatException e2) {
                        loge("Exception when parseInt reg_plmn_roam_custom");
                        ruleRoam = 0;
                    }
                    regplmnRoamCustomString = regplmnRoamCustomString2;
                    String[] plmnRoams = rulePlmnRoams[1].split(",");
                    netType = netType2;
                    if (4 == ruleRoam && 3 == plmnRoams.length && containsPlmn(hplmn, plmnRoams[0].split("\\|"))) {
                        return getGsmRoamingCustBySpecialRule(plmnRoams[0], plmnRoams[1], plmnRoams[2], roaming2);
                    }
                    if (plmnRoams.length == 2) {
                        if (plmnRoams[0].equals(hplmn) && plmnRoams[1].equals(regplmn)) {
                            logd("roaming customization by hplmn and regplmn success!");
                            if (ruleRoam == 1) {
                                return true;
                            }
                            if (ruleRoam == 2) {
                                return false;
                            }
                        }
                        if (3 == ruleRoam && hplmn.length() > 2 && regplmn.length() > 2) {
                            hplmnMcc = hplmn.substring(0, 3);
                            String regplmnMcc = regplmn.substring(0, 3);
                            if (plmnRoams[0].equals(hplmnMcc) && plmnRoams[1].equals(regplmnMcc)) {
                                roaming2 = false;
                            }
                        }
                    } else if (3 == plmnRoams.length) {
                        logd("roaming customization by RAT");
                        if (plmnRoams[0].equals(hplmn) && plmnRoams[1].equals(regplmn) && plmnRoams[2].contains(String.valueOf(netClass + 1))) {
                            logd("roaming customization by RAT success!");
                            if (ruleRoam == 1) {
                                return true;
                            }
                            if (ruleRoam == 2) {
                                return false;
                            }
                        }
                    } else {
                        continue;
                    }
                } else {
                    regplmnRoamCustomString = regplmnRoamCustomString2;
                    netType = netType2;
                }
                i++;
                regplmnRoamCustomString2 = regplmnRoamCustomString;
                netType2 = netType;
            }
            return roaming2;
        }
        loge("mIccRecords null while getGsmRoamingCustByIMSIStart was called.");
        return roaming;
    }

    private boolean getGsmRoamingCustBySpecialRule(String hplmnlist, String regmcclist, String regplmnlist, boolean isRoaming) {
        if (!TextUtils.isEmpty(hplmnlist) && !TextUtils.isEmpty(regmcclist) && !TextUtils.isEmpty(regplmnlist) && this.mPhone.getIccRecords() != null) {
            String hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            String regplmn = getNewSS().getOperatorNumeric();
            if (TextUtils.isEmpty(hplmn) || TextUtils.isEmpty(regplmn) || regplmn.length() < 3) {
                return isRoaming;
            }
            boolean isMatchHplmn = false;
            boolean isMatchRegmcc = false;
            boolean isMatchRegplmn = false;
            String[] hplmnStrings = hplmnlist.split("\\|");
            String[] regmccStrings = regmcclist.split("\\|");
            String[] regplmnStrings = regplmnlist.split("\\|");
            if (containsPlmn(hplmn, hplmnStrings)) {
                isMatchHplmn = true;
            }
            if (containsPlmn(regplmn.substring(0, 3), regmccStrings)) {
                isMatchRegmcc = true;
            }
            if (containsPlmn(regplmn, regplmnStrings)) {
                isMatchRegplmn = true;
            }
            if (isMatchHplmn && isMatchRegmcc && isMatchRegplmn) {
                logd("match regmcc and regplmn, isRoaming");
                return true;
            } else if (isMatchHplmn && isMatchRegmcc) {
                logd("only match regmcc, no isRoaming");
                return false;
            }
        }
        return isRoaming;
    }

    private boolean getGsmRoamingSpecialCustByNetType(boolean isRoaming) {
        boolean isRoamingTemp = isRoaming;
        if (this.mPhone.getIccRecords() != null) {
            String hplmn = this.mPhone.getIccRecords().getOperatorNumeric();
            String regplmn = getNewSS().getOperatorNumeric();
            int netType = ServiceStateEx.getVoiceNetworkType(getNewSS());
            int netClass = TelephonyManagerEx.getNetworkClass(netType);
            logd("getGsmRoamingSpecialCustByNetType: hplmn=" + hplmn + " regplmn=" + regplmn + " netType=" + netType + " netClass=" + netClass);
            if ("50218".equals(hplmn) && "50212".equals(regplmn)) {
                if (is2g3gNetwork(netClass)) {
                    isRoamingTemp = false;
                }
                if (netClass == 3) {
                    isRoamingTemp = true;
                }
            }
        }
        logd("getGsmRoamingSpecialCustByNetType: isRoamingTemp = " + isRoamingTemp);
        return isRoamingTemp;
    }

    private boolean isDocomoTablet() {
        return SystemPropertiesEx.get("ro.product.custom", "NULL").contains("docomo") && "tablet".equals(SystemPropertiesEx.get("ro.build.characteristics", "tablet"));
    }

    public HwCustGsmServiceStateManager getHwCustGsmServiceStateManager() {
        return this.mHwCustGsmServiceStateManager;
    }
}

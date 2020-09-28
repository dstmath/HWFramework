package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.ims.ImsSsInfo;
import android.text.SpannableStringBuilder;
import com.android.ims.ImsException;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwCustMmiCode;
import com.android.internal.telephony.HwGsmAlphabet;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.huawei.android.telephony.RlogEx;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwGsmMmiCode {
    protected static final String CARRIER_MMICODE_UNESCAPEHTML = "carrier_mmicode_unescapehtml_bool";
    private static final int CLIR_DEFAULT = 0;
    private static final int CLIR_INVOCATION = 1;
    private static final int CLIR_NOT_PROVISIONED = 0;
    private static final int CLIR_PRESENTATION_ALLOWED_TEMPORARY = 4;
    private static final int CLIR_PRESENTATION_RESTRICTED_TEMPORARY = 3;
    private static final int CLIR_PROVISIONED_PERMANENT = 1;
    private static final int CLIR_SUPPRESSION = 2;
    static final String CONNECT_MESSAGE_ERRORCODE = "ut409perfix";
    static final int EVENT_HWIMS_BASE = 1000;
    static final int EVENT_HWIMS_GET_CLIR_COMPLETE = 1004;
    static final int EVENT_HWIMS_QUERY_CF_COMPLETE = 1006;
    static final int EVENT_HWIMS_QUERY_COMPLETE = 1002;
    static final int EVENT_HWIMS_SET_CFF_COMPLETE = 1005;
    static final int EVENT_HWIMS_SET_COMPLETE = 1001;
    static final int EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE = 1003;
    static final String HW_OPTA = SystemProperties.get("ro.config.hw_opta", "0");
    static final String HW_OPTB = SystemProperties.get("ro.config.hw_optb", "0");
    private static final int INVALID_SERVICE_CODE = -1;
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    static final String LOG_TAG = "HwGsmMmiCode";
    public static final int MATCH_GROUP_DIALING_NUMBER = 16;
    public static final int MATCH_GROUP_PWD_CONFIRM = 15;
    public static final int MATCH_GROUP_SIA = 6;
    public static final int MATCH_GROUP_SIB = 9;
    public static final int MATCH_GROUP_SIC = 12;
    private static final boolean MMI_CODE_CUSTOM = SystemProperties.getBoolean("ro.config.hw_mmicode_custom", false);
    private static final int NOT_HAS_VALUES = -1;
    private static final int NUM_PRESENTATION_ALLOWED = 0;
    private static final int NUM_PRESENTATION_RESTRICTED = 1;
    static final String SC_CFB = "67";
    static final String SC_CFNR = "62";
    static final String SC_CFNRY = "61";
    static final String SC_CFU = "21";
    private static final String SC_CLIP = "30";
    private static final String SC_CLIR = "31";
    private static final String SC_COLP = "76";
    private static final String SC_COLR = "77";
    static final String SC_PIN = "04";
    static final String SC_PIN2 = "042";
    static final String SC_PUK = "05";
    static final String SC_PUK2 = "052";
    private static final String SC_PWD = "03";
    private static final String SC_WAIT = "43";
    private static final int VALUES_FALSE = 0;
    private static final int VALUES_TRUE = 1;
    static String huaweiMmiIgnoreList = SystemProperties.get("ro.config.hw_mmi_ignore_list", "null");
    static String huaweiMmiMatchList = SystemProperties.get("ro.config.hw_mmi_match_list", "null");
    private static HwCustMmiCode mHwCustMmiCode = ((HwCustMmiCode) HwCustUtils.createObj(HwCustMmiCode.class, new Object[0]));
    static boolean mPromptUserBadPin2 = SystemProperties.getBoolean("ro.config.prompt_bad_pin2", true);
    static Pattern sPatternHuaweiMMICode = Pattern.compile("(([\\*\\#\\d]{1,10},)*([\\*\\#\\d]{1,10})?)");
    public static final Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})((\\*#|\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*))?)?)?)?#)(.*)");

    public static boolean isShortCodeCustomization() {
        if (SystemProperties.getBoolean("gsm.hw.matchnum.vmn_shortcode", false)) {
            return true;
        }
        return false;
    }

    public static String convertUssdMessage(String oriUssdMessage) {
        if (!HW_OPTA.equals("27") || !HW_OPTB.equals("604")) {
            return oriUssdMessage;
        }
        char[] ussdChr = oriUssdMessage.toCharArray();
        for (int i = 0; i < ussdChr.length; i++) {
            ussdChr[i] = HwGsmAlphabet.ussd_7bit_ucs2_to_gsm_char_default(ussdChr[i]);
            ussdChr[i] = HwGsmAlphabet.util_UnicodeToGsm7DefaultExtended(ussdChr[i]);
        }
        return new String(ussdChr);
    }

    public static int siToServiceClass(String si) {
        if (si == null || si.length() == 0) {
            return 0;
        }
        int serviceCode = -1;
        try {
            serviceCode = Integer.parseInt(si, 10);
        } catch (NumberFormatException e) {
            RlogEx.e(LOG_TAG, "siToServiceClass NumberFormatException");
        }
        if (serviceCode == 16) {
            return 8;
        }
        if (serviceCode == 99) {
            return 64;
        }
        switch (serviceCode) {
            case 10:
                return 13;
            case 11:
                return 1;
            case MATCH_GROUP_SIC /*{ENCODED_INT: 12}*/:
                return 12;
            case 13:
                return 4;
            default:
                switch (serviceCode) {
                    case 19:
                        return 5;
                    case 20:
                        return 48;
                    case 21:
                        return 160;
                    case 22:
                        return 80;
                    default:
                        switch (serviceCode) {
                            case 24:
                                return 16;
                            case 25:
                                return 32;
                            case 26:
                                return 17;
                            default:
                                throw new RuntimeException("unsupported MMI service code " + si);
                        }
                }
        }
    }

    public static boolean isStringHuaweiIgnoreCode(String dialString) {
        RlogEx.i(LOG_TAG, "isHuaweiIgnoreCode");
        if (dialString == null) {
            RlogEx.e(LOG_TAG, "isStringHuaweiIgnoreCode null dial string");
            return false;
        } else if (isStringHuaweiCustCode(dialString)) {
            return false;
        } else {
            if (huaweiMmiIgnoreList.equals("null")) {
                RlogEx.e(LOG_TAG, "isStringHuaweiIgnoreCode, null ignore code list");
            } else if (huaweiMmiIgnoreList.equals("ALL")) {
                RlogEx.e(LOG_TAG, "isStringHuaweiIgnoreCode, block all codes");
                return true;
            } else {
                Matcher m = sPatternHuaweiMMICode.matcher(huaweiMmiIgnoreList);
                if (m.matches()) {
                    String[] notHwMmiCodes = m.group(1).split(",");
                    RlogEx.i(LOG_TAG, "isHuaweiIgnoreCode,group(1)=" + m.group(1) + ", size()=" + notHwMmiCodes.length);
                    if (Arrays.asList(notHwMmiCodes).contains(dialString)) {
                        return true;
                    }
                } else {
                    RlogEx.i(LOG_TAG, "isHuaweiIgnoreCode, not a ignore mmi code");
                }
            }
            return false;
        }
    }

    public static boolean isStringHuaweiCustCode(String dialString) {
        RlogEx.i(LOG_TAG, "isStringHuaweiCustCode");
        if (dialString == null) {
            RlogEx.e(LOG_TAG, "isStringHuaweiCustCode null dial string");
            return false;
        } else if (huaweiMmiMatchList.equals("null")) {
            RlogEx.e(LOG_TAG, "isStringHuaweiCustCode, null match code list");
            return false;
        } else {
            Matcher m = sPatternHuaweiMMICode.matcher(huaweiMmiMatchList);
            if (m.matches()) {
                String[] hwMmiCodes = m.group(1).split(",");
                RlogEx.i(LOG_TAG, "isStringHuaweiCustCode,group(1)=" + m.group(1) + ", size()=" + hwMmiCodes.length);
                if (Arrays.asList(hwMmiCodes).contains(dialString)) {
                    return true;
                }
            } else {
                RlogEx.i(LOG_TAG, "isStringHuaweiCustCode, not a customized mmi code");
            }
            return false;
        }
    }

    public static CharSequence processgoodPinString(Context context, String sc) {
        RlogEx.i(LOG_TAG, "processgoodPinString enter");
        context.getText(17039467);
        if (!mPromptUserBadPin2 || (!SC_PIN2.equals(sc) && !SC_PUK2.equals(sc))) {
            return context.getText(17039467);
        }
        return context.getText(33685779);
    }

    public static CharSequence processBadPinString(Context context, String sc) {
        RlogEx.i(LOG_TAG, "processBadPinString enter");
        context.getText(17039702);
        if (!mPromptUserBadPin2 || (!SC_PIN2.equals(sc) && !SC_PUK2.equals(sc))) {
            return context.getText(17039702);
        }
        return context.getText(33685780);
    }

    public static int handlePasswordError(String sc) {
        RlogEx.i(LOG_TAG, "handlePasswordError enter");
        if (!mPromptUserBadPin2) {
            return 17040607;
        }
        if (SC_PIN2.equals(sc) || SC_PUK2.equals(sc)) {
            return 33685782;
        }
        return 17040607;
    }

    public static int showMmiError(int oriSc, int slotId) {
        boolean custMmiError = false;
        boolean hasHwCfgConfig = false;
        try {
            Boolean mmiError = (Boolean) HwCfgFilePolicy.getValue("show_mmiError", slotId, Boolean.class);
            if (mmiError != null) {
                hasHwCfgConfig = true;
                custMmiError = mmiError.booleanValue();
            }
            if (hasHwCfgConfig && !custMmiError) {
                return oriSc;
            }
            RlogEx.i(LOG_TAG, "HwCfgFile:custMmiError =" + custMmiError);
            if (SystemProperties.getBoolean("ro.config.hw_show_mmiError", false) || custMmiError) {
                return 17040617;
            }
            return oriSc;
        } catch (ClassCastException e) {
            RlogEx.e(LOG_TAG, "read show_mmiError is ClassCastException!");
        } catch (Exception e2) {
            RlogEx.e(LOG_TAG, "read show_mmiError is error!");
        }
    }

    private static boolean isProcessGsmPhoneMmiCode(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        if (imsPhone == null || gsmMmiCode == null) {
            RlogEx.i(LOG_TAG, "imsPhone is null, don't process IMSPhone Mmi Code.");
            return true;
        } else if (!IS_DOCOMO) {
            return false;
        } else {
            RlogEx.i(LOG_TAG, "Docomo need to process Mmi Code in GsmMmiCode");
            return true;
        }
    }

    public static boolean processImsPhoneMmiCode(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        if (isProcessGsmPhoneMmiCode(gsmMmiCode, imsPhone)) {
            return false;
        }
        if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_CLIP)) {
            RlogEx.i(LOG_TAG, "is CLIP");
            handleMmiCodeScClip(gsmMmiCode, imsPhone);
            return true;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_CLIR)) {
            RlogEx.i(LOG_TAG, "is CLIR");
            handleMmiCodeScClir(gsmMmiCode, imsPhone);
            return true;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_COLP)) {
            RlogEx.i(LOG_TAG, "is COLP");
            handleMmiCodeScColp(gsmMmiCode, imsPhone);
            return true;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_COLR)) {
            RlogEx.i(LOG_TAG, "is COLR");
            handleMmiCodeScColr(gsmMmiCode, imsPhone);
            return true;
        } else if (GsmMmiCode.isServiceCodeCallForwarding(gsmMmiCode.mSc)) {
            RlogEx.i(LOG_TAG, "is CF");
            handleMmiCodeCallForwarding(gsmMmiCode, imsPhone);
            return true;
        } else if (GsmMmiCode.isServiceCodeCallBarring(gsmMmiCode.mSc)) {
            RlogEx.i(LOG_TAG, "is CB");
            handleMmiCodeCallBarring(gsmMmiCode, imsPhone);
            return true;
        } else if (gsmMmiCode.mSc == null || !gsmMmiCode.mSc.equals(SC_WAIT)) {
            return false;
        } else {
            RlogEx.i(LOG_TAG, "is wait");
            handleMmiCodeScWait(gsmMmiCode, imsPhone);
            return true;
        }
    }

    private static void handleMmiCodeScClip(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        HwTelephonyFactory.getHwPhoneManager().checkMMICode(gsmMmiCode.mSc, imsPhone.getPhoneId());
        if (gsmMmiCode.isInterrogate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().queryCLIP(gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
            } catch (ImsException e) {
                RlogEx.e(LOG_TAG, "Exception in getUtInterface : isInterrogate");
            }
        } else if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().updateCLIP(gsmMmiCode.isActivate(), gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } catch (ImsException e2) {
                RlogEx.i(LOG_TAG, "Could not get UT handle for updateCLIP.");
            }
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScClir(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        if (gsmMmiCode.isActivate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().updateCLIR(1, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } catch (ImsException e) {
                RlogEx.e(LOG_TAG, "Exception in getUtInterface : isActivate");
            }
        } else if (gsmMmiCode.isDeactivate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().updateCLIR(2, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } catch (ImsException e2) {
                RlogEx.e(LOG_TAG, "Exception in getUtInterface : isDeactivate");
            }
        } else if (gsmMmiCode.isInterrogate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().queryCLIR(gsmMmiCode.obtainMessage((int) EVENT_HWIMS_GET_CLIR_COMPLETE, gsmMmiCode));
            } catch (ImsException e3) {
                RlogEx.e(LOG_TAG, "Exception in getUtInterface : isInterrogate");
            }
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScColp(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        HwTelephonyFactory.getHwPhoneManager().checkMMICode(gsmMmiCode.mSc, imsPhone.getPhoneId());
        if (gsmMmiCode.isActivate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().updateCOLP(true, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } catch (ImsException e) {
                RlogEx.i(LOG_TAG, "Could not get UT handle for updateCOLP.");
            }
        } else if (gsmMmiCode.isDeactivate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().updateCOLP(false, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } catch (ImsException e2) {
                RlogEx.i(LOG_TAG, "Could not get UT handle for updateCOLP.");
            }
        } else if (gsmMmiCode.isInterrogate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().queryCOLP(gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
            } catch (ImsException e3) {
                RlogEx.i(LOG_TAG, "Could not get UT handle for queryCOLP.");
            }
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScColr(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        HwTelephonyFactory.getHwPhoneManager().checkMMICode(gsmMmiCode.mSc, imsPhone.getPhoneId());
        if (gsmMmiCode.isActivate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().updateCOLR(0, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } catch (ImsException e) {
                RlogEx.i(LOG_TAG, "Could not get UT handle for updateCOLR.");
            }
        } else if (gsmMmiCode.isDeactivate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().updateCOLR(1, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } catch (ImsException e2) {
                RlogEx.i(LOG_TAG, "Could not get UT handle for updateCOLR.");
            }
        } else if (gsmMmiCode.isInterrogate()) {
            try {
                imsPhone.getCallTracker().getUtInterfaceEx().queryCOLR(gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
            } catch (ImsException e3) {
                RlogEx.i(LOG_TAG, "Could not get UT handle for queryCOLR.");
            }
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeCallForwarding(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        int cfAction;
        String dialingNumber = gsmMmiCode.mSia;
        int serviceClass = siToServiceClass(gsmMmiCode.mSib);
        int reason = GsmMmiCode.scToCallForwardReasonEx(gsmMmiCode.mSc);
        int time = GsmMmiCode.siToTimeEx(gsmMmiCode.mSic);
        if (gsmMmiCode.isInterrogate()) {
            ((ImsPhone) imsPhone).getCallForwardForServiceClass(reason, serviceClass, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_QUERY_CF_COMPLETE, gsmMmiCode));
            return;
        }
        int isEnableDesired = 0;
        if (gsmMmiCode.isActivate()) {
            if (isEmptyOrNull(dialingNumber)) {
                gsmMmiCode.setHwCallFwgReg(false);
                cfAction = 1;
            } else {
                gsmMmiCode.setHwCallFwgReg(true);
                cfAction = 3;
            }
        } else if (gsmMmiCode.isDeactivate()) {
            cfAction = 0;
        } else if (gsmMmiCode.isRegister()) {
            cfAction = 3;
        } else if (gsmMmiCode.isErasure()) {
            cfAction = 4;
        } else {
            throw new RuntimeException("invalid action");
        }
        int isSettingUnconditionalVoice = ((reason == 0 || reason == 4) && ((serviceClass & 1) != 0 || serviceClass == 0)) ? 1 : 0;
        if (cfAction == 1 || cfAction == 3) {
            isEnableDesired = 1;
        }
        RlogEx.i(LOG_TAG, "is CF setCallForward");
        ((ImsPhone) imsPhone).setCallForwardingOption(cfAction, reason, dialingNumber, serviceClass, time, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_CFF_COMPLETE, isSettingUnconditionalVoice, isEnableDesired, gsmMmiCode));
    }

    private static void handleMmiCodeCallBarring(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        String password = gsmMmiCode.mSia;
        int serviceClass = siToServiceClass(gsmMmiCode.mSib);
        String facility = GsmMmiCode.scToBarringFacility(gsmMmiCode.mSc);
        RlogEx.i(LOG_TAG, "is CB setCallBarring, with serviceClass:" + serviceClass);
        if (gsmMmiCode.isInterrogate()) {
            ((ImsPhone) imsPhone).getCallBarring(facility, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode), serviceClass);
        } else if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
            ((ImsPhone) imsPhone).setCallBarring(facility, gsmMmiCode.isActivate(), password, gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode), serviceClass);
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScWait(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
            imsPhone.setCallWaiting(gsmMmiCode.isActivate(), gsmMmiCode.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
        } else if (gsmMmiCode.isInterrogate()) {
            imsPhone.getCallWaiting(gsmMmiCode.obtainMessage((int) EVENT_HWIMS_QUERY_COMPLETE, gsmMmiCode));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    public static void handleMessageGsmMmiCode(GsmMmiCode gsmMmiCode, Message msg) {
        switch (msg.what) {
            case EVENT_HWIMS_SET_COMPLETE /*{ENCODED_INT: 1001}*/:
                onHwImsSetComplete(gsmMmiCode, msg, (AsyncResult) msg.obj);
                return;
            case EVENT_HWIMS_QUERY_COMPLETE /*{ENCODED_INT: 1002}*/:
                onHwImsQueryComplete(gsmMmiCode, (AsyncResult) msg.obj);
                return;
            case EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE /*{ENCODED_INT: 1003}*/:
                onHwImsSuppSvcQueryComplete(gsmMmiCode, (AsyncResult) msg.obj);
                return;
            case EVENT_HWIMS_GET_CLIR_COMPLETE /*{ENCODED_INT: 1004}*/:
                onHwImsQueryClirComplete(gsmMmiCode, (AsyncResult) msg.obj);
                return;
            case EVENT_HWIMS_SET_CFF_COMPLETE /*{ENCODED_INT: 1005}*/:
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null && msg.arg1 == 1) {
                    boolean cffEnabled = msg.arg2 == 1;
                    if (gsmMmiCode.mIccRecords != null) {
                        gsmMmiCode.mIccRecords.setVoiceCallForwardingFlag(1, cffEnabled, gsmMmiCode.mDialingNumber);
                    }
                }
                onHwImsSetComplete(gsmMmiCode, msg, ar);
                return;
            case EVENT_HWIMS_QUERY_CF_COMPLETE /*{ENCODED_INT: 1006}*/:
                onHwImsQueryCfComplete(gsmMmiCode, (AsyncResult) msg.obj);
                return;
            default:
                return;
        }
    }

    private static CharSequence getScString(GsmMmiCode gsmMmiCode) {
        if (gsmMmiCode == null || gsmMmiCode.mSc == null) {
            return "";
        }
        if (GsmMmiCode.isServiceCodeCallBarring(gsmMmiCode.mSc)) {
            return gsmMmiCode.mContext.getText(17039397);
        }
        if (GsmMmiCode.isServiceCodeCallForwarding(gsmMmiCode.mSc)) {
            return getCallForwardingString(gsmMmiCode.mContext, gsmMmiCode.mSc);
        }
        if (gsmMmiCode.mSc.equals(SC_CLIP)) {
            return gsmMmiCode.mContext.getText(17039408);
        }
        if (gsmMmiCode.mSc.equals(SC_CLIR)) {
            return gsmMmiCode.mContext.getText(17039409);
        }
        if (gsmMmiCode.mSc.equals(SC_PWD)) {
            return gsmMmiCode.mContext.getText(17039468);
        }
        if (gsmMmiCode.mSc.equals(SC_WAIT)) {
            return gsmMmiCode.mContext.getText(17039415);
        }
        if (gsmMmiCode.mSc.equals(SC_COLP)) {
            return gsmMmiCode.mContext.getText(17039413);
        }
        if (gsmMmiCode.mSc.equals(SC_COLR)) {
            return gsmMmiCode.mContext.getText(17039414);
        }
        return "";
    }

    private static CharSequence getErrorMessage(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        return gsmMmiCode.mContext.getText(17040617);
    }

    private static CharSequence getImsExceptionMessage(GsmMmiCode gsmMmiCode, ImsException error) {
        if (error.getCode() == 0) {
            return getErrorMessage(gsmMmiCode, null);
        }
        if (error.getMessage() != null) {
            return error.getMessage();
        }
        return getErrorMessage(gsmMmiCode, null);
    }

    private static boolean isEmptyOrNull(CharSequence s) {
        return s == null || s.length() == 0;
    }

    private static boolean isUtNoConnectionException(Exception e) {
        if (e instanceof ImsException) {
            if (((ImsException) e).getCode() == 831) {
                return true;
            }
            return false;
        } else if (!(e instanceof CommandException) || ((CommandException) e).getCommandError() != CommandException.Error.UT_NO_CONNECTION) {
            return false;
        } else {
            return true;
        }
    }

    private static void onHwImsSuppSvcQueryComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(CommandException.Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else {
                sb.append(getErrorMessage(gsmMmiCode, ar));
            }
        } else {
            handleHwImsSuppSvcQueryNormal(sb, gsmMmiCode, ar);
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static void handleHwImsSuppSvcQueryNormal(StringBuilder sb, GsmMmiCode gsmMmiCode, AsyncResult ar) {
        gsmMmiCode.mState = MmiCode.State.FAILED;
        ImsSsInfo ssInfo = null;
        if (ar.result instanceof Bundle) {
            RlogEx.i(LOG_TAG, "onHwImsSuppSvcQueryComplete : Received CLIP/COLP/COLR Response.");
            Bundle ssInfoResp = (Bundle) ar.result;
            if (ssInfoResp != null) {
                ssInfo = ssInfoResp.getParcelable("imsSsInfo");
            }
            if (ssInfo != null) {
                RlogEx.i(LOG_TAG, "onHwImsSuppSvcQueryComplete : ImsSsInfo mStatus = " + ssInfo.mStatus);
                if (ssInfo.mStatus == 0) {
                    sb.append(getCustSsToastString(gsmMmiCode.mContext, false, true));
                    gsmMmiCode.mState = MmiCode.State.COMPLETE;
                } else if (ssInfo.mStatus == 1) {
                    sb.append(getCustSsToastString(gsmMmiCode.mContext, true, true));
                    gsmMmiCode.mState = MmiCode.State.COMPLETE;
                } else {
                    sb.append(gsmMmiCode.mContext.getText(17040617));
                }
            } else {
                sb.append(gsmMmiCode.mContext.getText(17040617));
            }
        } else {
            RlogEx.i(LOG_TAG, "onHwImsSuppSvcQueryComplete : Received Call Barring Response.");
            if (((int[]) ar.result)[0] != 0) {
                sb.append(getCustSsToastString(gsmMmiCode.mContext, true, true));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
                return;
            }
            sb.append(getCustSsToastString(gsmMmiCode.mContext, false, true));
            gsmMmiCode.mState = MmiCode.State.COMPLETE;
        }
    }

    private static void onHwImsSetComplete(GsmMmiCode gsmMmiCode, Message msg, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(CommandException.Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            handleHwImsSetException(sb, gsmMmiCode, ar);
        } else if (gsmMmiCode.isActivate()) {
            gsmMmiCode.mState = MmiCode.State.COMPLETE;
            if (gsmMmiCode.getHwCallFwdReg()) {
                sb.append(gsmMmiCode.mContext.getText(17041203));
            } else {
                sb.append(getCustSsToastString(gsmMmiCode.mContext, true, false));
            }
            if (SC_CLIR.equals(gsmMmiCode.mSc)) {
                gsmMmiCode.mPhone.saveClirSetting(1);
            }
        } else if (gsmMmiCode.isDeactivate()) {
            gsmMmiCode.mState = MmiCode.State.COMPLETE;
            sb.append(getCustSsToastString(gsmMmiCode.mContext, false, false));
            if (SC_CLIR.equals(gsmMmiCode.mSc)) {
                gsmMmiCode.mPhone.saveClirSetting(2);
            }
        } else if (gsmMmiCode.isRegister()) {
            gsmMmiCode.mState = MmiCode.State.COMPLETE;
            sb.append(gsmMmiCode.mContext.getText(17041203));
        } else if (gsmMmiCode.isErasure()) {
            gsmMmiCode.mState = MmiCode.State.COMPLETE;
            sb.append(gsmMmiCode.mContext.getText(17041201));
        } else {
            gsmMmiCode.mState = MmiCode.State.FAILED;
            sb.append(gsmMmiCode.mContext.getText(17040617));
        }
        gsmMmiCode.mMessage = sb;
        if (ar.exception == null) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
        } else if (ar.exception instanceof CommandException) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, ar.exception);
        }
    }

    private static void handleHwImsSetException(StringBuilder sb, GsmMmiCode gsmMmiCode, AsyncResult ar) {
        gsmMmiCode.mState = MmiCode.State.FAILED;
        if (ar.exception instanceof CommandException) {
            CommandException.Error err = ar.exception.getCommandError();
            String message = ar.exception.getMessage();
            if (err == CommandException.Error.PASSWORD_INCORRECT) {
                sb.append(gsmMmiCode.mContext.getText(17040718));
            } else if (message == null || message.isEmpty() || !message.contains(CONNECT_MESSAGE_ERRORCODE)) {
                sb.append(gsmMmiCode.mContext.getText(17040617));
            } else {
                sb.append(message.substring(0, message.indexOf(CONNECT_MESSAGE_ERRORCODE)));
                RlogEx.i(LOG_TAG, "onHwImsSetComplete : errorMessage = " + ((Object) sb));
            }
        } else {
            sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
        }
    }

    private static void onHwImsQueryClirComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(CommandException.Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        gsmMmiCode.mState = MmiCode.State.FAILED;
        if (ar.exception == null) {
            int[] clirInfo = null;
            if (ar.result instanceof Bundle) {
                Bundle ssInfo = (Bundle) ar.result;
                if (ssInfo != null) {
                    clirInfo = ssInfo.getIntArray("queryClir");
                }
            } else {
                clirInfo = (int[]) ar.result;
            }
            if (clirInfo == null || clirInfo.length < 2) {
                RlogEx.i(LOG_TAG, "CLIR param invalid.");
                gsmMmiCode.mMessage = sb;
                gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
                return;
            }
            RlogEx.i(LOG_TAG, "CLIR param n=" + clirInfo[0] + " m=" + clirInfo[1]);
            int i = clirInfo[1];
            if (i == 0) {
                sb.append(gsmMmiCode.mContext.getText(17041202));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            } else if (i == 1) {
                sb.append(gsmMmiCode.mContext.getText(17039402));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            } else if (i == 3) {
                handlePresentationRestrictedTemporary(sb, gsmMmiCode, clirInfo[0]);
            } else if (i != 4) {
                sb.append(gsmMmiCode.mContext.getText(17040617));
                gsmMmiCode.mState = MmiCode.State.FAILED;
            } else {
                handlePresentationAllowedTemporary(sb, gsmMmiCode, clirInfo[0]);
            }
        } else if (ar.exception instanceof ImsException) {
            sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static void handlePresentationRestrictedTemporary(StringBuilder sb, GsmMmiCode gsmMmiCode, int parameterN) {
        if (sb != null && gsmMmiCode != null) {
            if (parameterN == 0) {
                sb.append(gsmMmiCode.mContext.getText(17039401));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            } else if (parameterN == 1) {
                sb.append(gsmMmiCode.mContext.getText(17039401));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            } else if (parameterN != 2) {
                sb.append(gsmMmiCode.mContext.getText(17040617));
                gsmMmiCode.mState = MmiCode.State.FAILED;
            } else {
                sb.append(gsmMmiCode.mContext.getText(17039400));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            }
        }
    }

    private static void handlePresentationAllowedTemporary(StringBuilder sb, GsmMmiCode gsmMmiCode, int parameterN) {
        if (sb != null && gsmMmiCode != null) {
            if (parameterN == 0) {
                sb.append(gsmMmiCode.mContext.getText(17039398));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            } else if (parameterN == 1) {
                sb.append(gsmMmiCode.mContext.getText(17039399));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            } else if (parameterN != 2) {
                sb.append(gsmMmiCode.mContext.getText(17040617));
                gsmMmiCode.mState = MmiCode.State.FAILED;
            } else {
                sb.append(gsmMmiCode.mContext.getText(17039398));
                gsmMmiCode.mState = MmiCode.State.COMPLETE;
            }
        }
    }

    private static void onHwImsQueryCfComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(CommandException.Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else {
                sb.append(getErrorMessage(gsmMmiCode, ar));
            }
        } else {
            CallForwardInfo[] infos = (CallForwardInfo[]) ar.result;
            if (infos.length == 0) {
                sb.append(getCustSsToastString(gsmMmiCode.mContext, false, true));
                if (gsmMmiCode.mIccRecords != null) {
                    gsmMmiCode.mIccRecords.setVoiceCallForwardingFlag(1, false, (String) null);
                }
            } else {
                setImsTbToSb(sb, infos, gsmMmiCode);
            }
            gsmMmiCode.mState = MmiCode.State.COMPLETE;
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static void setImsTbToSb(StringBuilder sb, CallForwardInfo[] infos, GsmMmiCode gsmMmiCode) {
        SpannableStringBuilder tb = new SpannableStringBuilder();
        for (int serviceClassMask = 1; serviceClassMask <= 128; serviceClassMask <<= 1) {
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if ((infos[i].serviceClass & serviceClassMask) != 0) {
                    tb.append(gsmMmiCode.makeCFQueryResultMessageEx(infos[i], serviceClassMask));
                    tb.append("\n");
                }
            }
        }
        sb.append((CharSequence) tb);
    }

    private static void onHwImsQueryComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(CommandException.Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else {
                sb.append(getErrorMessage(gsmMmiCode, ar));
            }
        } else {
            int[] ints = (int[]) ar.result;
            if (ints.length == 0) {
                sb.append(gsmMmiCode.mContext.getText(17040617));
            } else if (ints[0] == 0) {
                sb.append(getCustSsToastString(gsmMmiCode.mContext, false, true));
            } else if (gsmMmiCode.mSc.equals(SC_WAIT)) {
                sb.append(gsmMmiCode.createQueryCallWaitingResultMessageEx(ints[1]));
            } else if (ints[0] == 1) {
                sb.append(getCustSsToastString(gsmMmiCode.mContext, true, true));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17040617));
            }
            gsmMmiCode.mState = MmiCode.State.COMPLETE;
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static boolean getHwMmicodeCustom() {
        boolean valueFromProp = MMI_CODE_CUSTOM;
        boolean result = valueFromProp;
        boolean z = false;
        Boolean valueFromCard1 = (Boolean) HwCfgFilePolicy.getValue("hw_mmicode_custom", 0, Boolean.class);
        if (valueFromCard1 != null) {
            result = result || valueFromCard1.booleanValue();
        }
        Boolean valueFromCard2 = (Boolean) HwCfgFilePolicy.getValue("hw_mmicode_custom", 1, Boolean.class);
        if (valueFromCard2 != null) {
            if (result || valueFromCard2.booleanValue()) {
                z = true;
            }
            result = z;
        }
        RlogEx.i(LOG_TAG, "getHwMmicodeCustom, card1:" + valueFromCard1 + ", card2:" + valueFromCard2 + ", prop:" + valueFromProp);
        return result;
    }

    public static CharSequence getCallForwardingString(Context context, String sc) {
        if (!getHwMmicodeCustom()) {
            return context.getText(17039403);
        }
        if (sc.equals(SC_CFU)) {
            return context.getText(17039407);
        }
        if (sc.equals(SC_CFB)) {
            return context.getText(17039406);
        }
        if (sc.equals(SC_CFNRY)) {
            return context.getText(17039405);
        }
        if (sc.equals(SC_CFNR)) {
            return context.getText(17039404);
        }
        return context.getText(17039403);
    }

    private static CharSequence getCustSsToastString(Context context, boolean serviceEnable, boolean isQuery) {
        HwCustMmiCode hwCustMmiCode = mHwCustMmiCode;
        if (hwCustMmiCode != null && hwCustMmiCode.isSsToastSwitchEnabled()) {
            return mHwCustMmiCode.getCustSsToastString(context, serviceEnable, isQuery);
        }
        if (serviceEnable) {
            return context.getText(17041199);
        }
        return context.getText(17041198);
    }

    public static boolean processSendUssdInImsCall(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        CarrierConfigManager cfgManager = (CarrierConfigManager) gsmMmiCode.mContext.getSystemService("carrier_config");
        if (cfgManager == null) {
            RlogEx.w(LOG_TAG, "Carrier config service is not available.");
            return false;
        }
        int subId = gsmMmiCode.mPhone.getSubId();
        PersistableBundle b = cfgManager.getConfigForSubId(subId);
        if (b == null) {
            RlogEx.w(LOG_TAG, "Can't get the config. subId = " + subId);
            return false;
        } else if (!b.getBoolean("carrier_forbid_ussd_when_ims_calling_bool") || !ImsPhone.class.isInstance(imsPhone) || !((ImsPhone) imsPhone).isInCallHw()) {
            return false;
        } else {
            Message onCompleted = gsmMmiCode.obtainMessage(4, gsmMmiCode);
            AsyncResult.forMessage(onCompleted).exception = new CommandException(CommandException.Error.GENERIC_FAILURE);
            onCompleted.sendToTarget();
            return true;
        }
    }

    public static boolean needUnEscapeHtmlforUssdMsg(Phone phone) {
        if (phone == null || phone.getContext() == null) {
            RlogEx.i(LOG_TAG, "needUnEscapeHtmlforUssdMsg: phone or phone Context is null, return false;");
            return false;
        }
        int subId = phone.getSubId();
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            RlogEx.e(LOG_TAG, "needUnEscapeHtmlforUssdMsg: subId=" + subId + " is Invalid, return false;");
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
        if (configManager == null) {
            RlogEx.e(LOG_TAG, "needUnEscapeHtmlforUssdMsg: configManager is null, return false;");
            return false;
        }
        PersistableBundle bundle = configManager.getConfigForSubId(subId);
        if (bundle == null || bundle.get(CARRIER_MMICODE_UNESCAPEHTML) == null) {
            return false;
        }
        return bundle.getBoolean(CARRIER_MMICODE_UNESCAPEHTML);
    }

    public static boolean isShortCodeHw(String dialString, GsmCdmaPhone phone) {
        String[] hwMmiCodes;
        if (phone == null || dialString == null || dialString.length() > 2) {
            return false;
        }
        String hwMmiCodeStr = SystemProperties.get("ro.config.hw_mmi_code", "-1");
        try {
            String cfgMmiCode = (String) HwCfgFilePolicy.getValue("mmi_code", SubscriptionManager.getSlotIndex(phone.getSubId()), String.class);
            if (!(cfgMmiCode == null || cfgMmiCode.length() == 0)) {
                hwMmiCodeStr = cfgMmiCode;
                RlogEx.i(LOG_TAG, "HwCfgFile: hwMmiCodeStr=" + hwMmiCodeStr);
            }
        } catch (ClassCastException e) {
            RlogEx.e(LOG_TAG, "read mmi_code ClassCastException! ");
        } catch (Exception e2) {
            RlogEx.i(LOG_TAG, "read mmi_code error! ");
        }
        if (hwMmiCodeStr == null || (hwMmiCodes = hwMmiCodeStr.split(",")) == null || !Arrays.asList(hwMmiCodes).contains(dialString)) {
            return false;
        }
        return true;
    }

    public static int removeUssdCust(GsmCdmaPhone phone) {
        if (phone == null) {
            return -1;
        }
        boolean removeUssdState = false;
        boolean hasHwCfgConfig = false;
        try {
            Boolean removeUssd = (Boolean) HwCfgFilePolicy.getValue("remove_mmi", SubscriptionManager.getSlotIndex(phone.getSubId()), Boolean.class);
            if (removeUssd != null) {
                removeUssdState = removeUssd.booleanValue();
                hasHwCfgConfig = true;
            }
            if (hasHwCfgConfig && !removeUssdState) {
                return 0;
            }
            if (removeUssdState) {
                return 1;
            }
            return -1;
        } catch (ClassCastException e) {
            RlogEx.e(LOG_TAG, "read remove_mmi ClassCastException! ");
        } catch (Exception e2) {
            RlogEx.e(LOG_TAG, "read remove_mmi error! ");
        }
    }
}

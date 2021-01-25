package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.text.SpannableStringBuilder;
import com.android.internal.telephony.HwCustMmiCode;
import com.android.internal.telephony.HwGsmAlphabet;
import com.android.internal.telephony.HwPhoneManagerImpl;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.CallForwardInfoExt;
import com.huawei.internal.telephony.CommandExceptionExt;
import com.huawei.internal.telephony.ImsExceptionExt;
import com.huawei.internal.telephony.MmiCodeExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.utils.HwPartResourceUtils;
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
    private static final int DISABLED = 0;
    private static final int ENABLED = 1;
    static final int EVENT_HWIMS_BASE = 1000;
    static final int EVENT_HWIMS_GET_CLIR_COMPLETE = 1004;
    static final int EVENT_HWIMS_QUERY_CF_COMPLETE = 1006;
    static final int EVENT_HWIMS_QUERY_COMPLETE = 1002;
    static final int EVENT_HWIMS_SET_CFF_COMPLETE = 1005;
    static final int EVENT_HWIMS_SET_COMPLETE = 1001;
    static final int EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE = 1003;
    static final String HW_OPTA = SystemPropertiesEx.get("ro.config.hw_opta", "0");
    static final String HW_OPTB = SystemPropertiesEx.get("ro.config.hw_optb", "0");
    private static final int INVALID_SERVICE_CODE = -1;
    private static final boolean IS_DOCOMO = SystemPropertiesEx.get("ro.product.custom", "NULL").contains("docomo");
    static final String LOG_TAG = "HwGsmMmiCode";
    public static final int MATCH_GROUP_DIALING_NUMBER = 16;
    public static final int MATCH_GROUP_PWD_CONFIRM = 15;
    public static final int MATCH_GROUP_SIA = 6;
    public static final int MATCH_GROUP_SIB = 9;
    public static final int MATCH_GROUP_SIC = 12;
    private static final boolean MMI_CODE_CUSTOM = SystemPropertiesEx.getBoolean("ro.config.hw_mmicode_custom", false);
    private static final int NOT_HAS_VALUES = -1;
    private static final int NOT_REGISTERED = -1;
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
    private static final String UT_BUNDLE_CLIR = "queryClir";
    private static final int VALUES_FALSE = 0;
    private static final int VALUES_TRUE = 1;
    static String huaweiMmiIgnoreList = SystemPropertiesEx.get("ro.config.hw_mmi_ignore_list", "null");
    static String huaweiMmiMatchList = SystemPropertiesEx.get("ro.config.hw_mmi_match_list", "null");
    private static HwCustMmiCode mHwCustMmiCode = ((HwCustMmiCode) HwCustUtils.createObj(HwCustMmiCode.class, new Object[0]));
    static boolean promptUserBadPin2 = SystemPropertiesEx.getBoolean("ro.config.prompt_bad_pin2", true);
    static Pattern sPatternHuaweiMMICode = Pattern.compile("(([\\*\\#\\d]{1,10},)*([\\*\\#\\d]{1,10})?)");
    public static final Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})((\\*#|\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*))?)?)?)?#)(.*)");

    public static boolean isShortCodeCustomization() {
        return SystemPropertiesEx.getBoolean("gsm.hw.matchnum.vmn_shortcode", false);
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
            case MATCH_GROUP_SIC /* 12 */:
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
                    return Arrays.asList(notHwMmiCodes).contains(dialString);
                }
                RlogEx.i(LOG_TAG, "isHuaweiIgnoreCode, not a ignore mmi code");
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
                return Arrays.asList(hwMmiCodes).contains(dialString);
            }
            RlogEx.i(LOG_TAG, "isStringHuaweiCustCode, not a customized mmi code");
            return false;
        }
    }

    public static CharSequence processgoodPinString(Context context, String sc) {
        RlogEx.i(LOG_TAG, "processgoodPinString enter");
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        context.getText(HwPartResourceUtils.getResourceId("PinMmi"));
        if (!promptUserBadPin2 || (!SC_PIN2.equals(sc) && !SC_PUK2.equals(sc))) {
            return context.getText(HwPartResourceUtils.getResourceId("PinMmi"));
        }
        return context.getText(33685779);
    }

    public static CharSequence processBadPinString(Context context, String sc) {
        RlogEx.i(LOG_TAG, "processBadPinString enter");
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        context.getText(HwPartResourceUtils.getResourceId("badPin"));
        if (!promptUserBadPin2 || (!SC_PIN2.equals(sc) && !SC_PUK2.equals(sc))) {
            return context.getText(HwPartResourceUtils.getResourceId("badPin"));
        }
        return context.getText(33685780);
    }

    public static int handlePasswordError(String sc) {
        RlogEx.i(LOG_TAG, "handlePasswordError enter");
        int result = HwPartResourceUtils.getResourceId("mismatchPin");
        if (!promptUserBadPin2) {
            return result;
        }
        if (SC_PIN2.equals(sc) || SC_PUK2.equals(sc)) {
            return 33685782;
        }
        return result;
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
            if (SystemPropertiesEx.getBoolean("ro.config.hw_show_mmiError", false) || custMmiError) {
                return HwPartResourceUtils.getResourceId("mmiError");
            }
            return oriSc;
        } catch (ClassCastException e) {
            RlogEx.e(LOG_TAG, "read show_mmiError is ClassCastException!");
        } catch (Exception e2) {
            RlogEx.e(LOG_TAG, "read show_mmiError is error!");
        }
    }

    private static boolean isProcessGsmPhoneMmiCode(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        if (phoneExt == null || mmiCodeExt == null) {
            RlogEx.i(LOG_TAG, "imsPhone is null, don't process IMSPhone Mmi Code.");
            return true;
        } else if (!IS_DOCOMO) {
            return false;
        } else {
            RlogEx.i(LOG_TAG, "Docomo need to process Mmi Code in GsmMmiCode");
            return true;
        }
    }

    public static boolean processImsPhoneMmiCode(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        if (isProcessGsmPhoneMmiCode(mmiCodeExt, phoneExt)) {
            return false;
        }
        String sc = mmiCodeExt.getSc();
        if (sc != null && sc.equals(SC_CLIP)) {
            RlogEx.i(LOG_TAG, "is CLIP");
            handleMmiCodeScClip(mmiCodeExt, phoneExt);
            return true;
        } else if (sc != null && sc.equals(SC_CLIR)) {
            RlogEx.i(LOG_TAG, "is CLIR");
            handleMmiCodeScClir(mmiCodeExt, phoneExt);
            return true;
        } else if (sc != null && sc.equals(SC_COLP)) {
            RlogEx.i(LOG_TAG, "is COLP");
            handleMmiCodeScColp(mmiCodeExt, phoneExt);
            return true;
        } else if (sc != null && sc.equals(SC_COLR)) {
            RlogEx.i(LOG_TAG, "is COLR");
            handleMmiCodeScColr(mmiCodeExt, phoneExt);
            return true;
        } else if (mmiCodeExt.isServiceCodeCallForwarding(sc)) {
            RlogEx.i(LOG_TAG, "is CF");
            handleMmiCodeCallForwarding(mmiCodeExt, phoneExt);
            return true;
        } else if (mmiCodeExt.isServiceCodeCallBarring(sc)) {
            RlogEx.i(LOG_TAG, "is CB");
            handleMmiCodeCallBarring(mmiCodeExt, phoneExt);
            return true;
        } else if (sc == null || !sc.equals(SC_WAIT)) {
            RlogEx.i(LOG_TAG, "string code is " + sc);
            return false;
        } else {
            RlogEx.i(LOG_TAG, "is wait");
            handleMmiCodeScWait(mmiCodeExt, phoneExt);
            return true;
        }
    }

    private static void handleMmiCodeScClip(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        HwPhoneManagerImpl.getDefault().checkMMICode(mmiCodeExt.getSc(), phoneExt.getPhoneId());
        if (mmiCodeExt.isInterrogate()) {
            phoneExt.queryCLIP(mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isActivate() || mmiCodeExt.isDeactivate()) {
            phoneExt.updateCLIP(mmiCodeExt.isActivate(), mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScClir(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        if (mmiCodeExt.isActivate()) {
            phoneExt.updateCLIR(1, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isDeactivate()) {
            phoneExt.updateCLIR(2, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isInterrogate()) {
            phoneExt.queryCLIR(mmiCodeExt.obtainMessage((int) EVENT_HWIMS_GET_CLIR_COMPLETE, mmiCodeExt));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScColp(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        HwPhoneManagerImpl.getDefault().checkMMICode(mmiCodeExt.getSc(), phoneExt.getPhoneId());
        if (mmiCodeExt.isActivate()) {
            phoneExt.updateCOLP(true, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isDeactivate()) {
            phoneExt.updateCOLP(false, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isInterrogate()) {
            phoneExt.queryCOLP(mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, mmiCodeExt));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScColr(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        HwPhoneManagerImpl.getDefault().checkMMICode(mmiCodeExt.getSc(), phoneExt.getPhoneId());
        if (mmiCodeExt.isActivate()) {
            phoneExt.updateCOLR(0, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isDeactivate()) {
            phoneExt.updateCOLR(1, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isInterrogate()) {
            phoneExt.queryCOLR(mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, mmiCodeExt));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeCallForwarding(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        int cfAction;
        String dialingNumber = mmiCodeExt.getSia();
        int serviceClass = siToServiceClass(mmiCodeExt.getSib());
        int reason = mmiCodeExt.scToCallForwardReasonEx(mmiCodeExt.getSc());
        int time = mmiCodeExt.siToTimeEx(mmiCodeExt.getSic());
        if (mmiCodeExt.isInterrogate()) {
            phoneExt.getCallForwardForServiceClass(reason, serviceClass, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_QUERY_CF_COMPLETE, mmiCodeExt));
            return;
        }
        int isEnableDesired = 0;
        if (mmiCodeExt.isActivate()) {
            if (isEmptyOrNull(dialingNumber)) {
                mmiCodeExt.setHwCallFwgReg(false);
                cfAction = 1;
            } else {
                mmiCodeExt.setHwCallFwgReg(true);
                cfAction = 3;
            }
        } else if (mmiCodeExt.isDeactivate()) {
            cfAction = 0;
        } else if (mmiCodeExt.isRegister()) {
            cfAction = 3;
        } else if (mmiCodeExt.isErasure()) {
            cfAction = 4;
        } else {
            throw new RuntimeException("invalid action");
        }
        int isSettingUnconditionalVoice = ((reason == 0 || reason == 4) && ((serviceClass & 1) != 0 || serviceClass == 0)) ? 1 : 0;
        if (cfAction == 1 || cfAction == 3) {
            isEnableDesired = 1;
        }
        RlogEx.i(LOG_TAG, "is CF setCallForward");
        phoneExt.setCallForwardingOption(cfAction, reason, dialingNumber, serviceClass, time, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_CFF_COMPLETE, isSettingUnconditionalVoice, isEnableDesired, mmiCodeExt));
    }

    private static void handleMmiCodeCallBarring(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        String password = mmiCodeExt.getSia();
        int serviceClass = siToServiceClass(mmiCodeExt.getSib());
        String facility = mmiCodeExt.scToBarringFacility(mmiCodeExt.getSc());
        RlogEx.i(LOG_TAG, "is CB setCallBarring, with serviceClass:" + serviceClass);
        if (mmiCodeExt.isInterrogate()) {
            phoneExt.getCallBarring(facility, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, mmiCodeExt), serviceClass);
        } else if (mmiCodeExt.isActivate() || mmiCodeExt.isDeactivate()) {
            phoneExt.setCallBarring(facility, mmiCodeExt.isActivate(), password, mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt), serviceClass);
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private static void handleMmiCodeScWait(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        if (mmiCodeExt.isActivate() || mmiCodeExt.isDeactivate()) {
            phoneExt.setCallWaiting(mmiCodeExt.isActivate(), mmiCodeExt.obtainMessage((int) EVENT_HWIMS_SET_COMPLETE, mmiCodeExt));
        } else if (mmiCodeExt.isInterrogate()) {
            phoneExt.getCallWaiting(mmiCodeExt.obtainMessage((int) EVENT_HWIMS_QUERY_COMPLETE, mmiCodeExt));
        } else {
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    public static void handleMessageGsmMmiCode(MmiCodeExt mmiCodeExt, Message msg) {
        switch (msg.what) {
            case EVENT_HWIMS_SET_COMPLETE /* 1001 */:
                AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                setImsException(ar);
                onHwImsSetComplete(mmiCodeExt, msg, ar);
                return;
            case EVENT_HWIMS_QUERY_COMPLETE /* 1002 */:
                AsyncResultEx ar2 = AsyncResultEx.from(msg.obj);
                setImsException(ar2);
                onHwImsQueryComplete(mmiCodeExt, ar2);
                return;
            case EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE /* 1003 */:
                AsyncResultEx ar3 = AsyncResultEx.from(msg.obj);
                setImsException(ar3);
                onHwImsSuppSvcQueryComplete(mmiCodeExt, ar3);
                return;
            case EVENT_HWIMS_GET_CLIR_COMPLETE /* 1004 */:
                AsyncResultEx ar4 = AsyncResultEx.from(msg.obj);
                setImsException(ar4);
                onHwImsQueryClirComplete(mmiCodeExt, ar4);
                return;
            case EVENT_HWIMS_SET_CFF_COMPLETE /* 1005 */:
                AsyncResultEx ar5 = AsyncResultEx.from(msg.obj);
                setImsException(ar5);
                if (ar5.getException() == null && msg.arg1 == 1 && mmiCodeExt != null) {
                    mmiCodeExt.setVoiceCallForwardingFlag(1, msg.arg2 == 1, mmiCodeExt.getDialingNumber());
                }
                onHwImsSetComplete(mmiCodeExt, msg, ar5);
                return;
            case EVENT_HWIMS_QUERY_CF_COMPLETE /* 1006 */:
                AsyncResultEx ar6 = AsyncResultEx.from(msg.obj);
                setImsException(ar6);
                onHwImsQueryCfComplete(mmiCodeExt, ar6);
                return;
            default:
                return;
        }
    }

    private static void setImsException(AsyncResultEx ar) {
        if (ImsExceptionExt.isImsException(ar.getException())) {
            ar.setException(ImsExceptionExt.from(ar.getException()));
        } else if (CommandExceptionExt.isCommandException(ar.getException())) {
            ar.setException(CommandExceptionExt.getCommandException(ar.getException()));
        } else {
            RlogEx.i(LOG_TAG, "other exception type.");
        }
    }

    private static CharSequence getScString(MmiCodeExt mmiCodeExt) {
        String sc = null;
        Context context = null;
        if (mmiCodeExt != null) {
            sc = mmiCodeExt.getSc();
            context = mmiCodeExt.getContext();
        }
        if (sc == null || context == null) {
            return BuildConfig.FLAVOR;
        }
        if (mmiCodeExt.isServiceCodeCallBarring(sc)) {
            return context.getText(HwPartResourceUtils.getResourceId("BaMmi"));
        }
        if (mmiCodeExt.isServiceCodeCallForwarding(sc)) {
            return getCallForwardingString(context, sc);
        }
        if (sc.equals(SC_CLIP)) {
            return context.getText(HwPartResourceUtils.getResourceId("ClipMmi"));
        }
        if (sc.equals(SC_CLIR)) {
            return context.getText(HwPartResourceUtils.getResourceId("ClirMmi"));
        }
        if (sc.equals(SC_PWD)) {
            return context.getText(HwPartResourceUtils.getResourceId("PwdMmi"));
        }
        if (sc.equals(SC_WAIT)) {
            return context.getText(HwPartResourceUtils.getResourceId("CwMmi"));
        }
        if (sc.equals(SC_COLP)) {
            return context.getText(HwPartResourceUtils.getResourceId("ColpMmi"));
        }
        if (sc.equals(SC_COLR)) {
            return context.getText(HwPartResourceUtils.getResourceId("ColrMmi"));
        }
        RlogEx.i(LOG_TAG, "string code is " + sc);
        return BuildConfig.FLAVOR;
    }

    private static CharSequence getErrorMessage(MmiCodeExt mmiCodeExt, AsyncResultEx ar) {
        Context context = mmiCodeExt.getContext();
        if (context != null) {
            return context.getText(HwPartResourceUtils.getResourceId("mmiError"));
        }
        return null;
    }

    private static CharSequence getImsExceptionMessage(MmiCodeExt mmiCodeExt, ImsExceptionExt error) {
        if (error.getCode() == 0) {
            return getErrorMessage(mmiCodeExt, null);
        }
        if (error.getMessage() != null) {
            return error.getMessage();
        }
        return getErrorMessage(mmiCodeExt, null);
    }

    private static boolean isEmptyOrNull(CharSequence s) {
        return s == null || s.length() == 0;
    }

    private static boolean isUtNoConnectionException(PhoneExt phoneExt, Exception exception) {
        if (exception instanceof ImsExceptionExt) {
            if (((ImsExceptionExt) exception).getCode() == 831) {
                return true;
            }
            return false;
        } else if (phoneExt != null) {
            return phoneExt.isUtNoConnectionException(exception);
        } else {
            return false;
        }
    }

    private static void onHwImsSuppSvcQueryComplete(MmiCodeExt mmiCodeExt, AsyncResultEx ar) {
        PhoneExt phoneExt = mmiCodeExt.getPhone();
        if (!isUtNoConnectionException(phoneExt, (Exception) ar.getException())) {
            StringBuilder sb = new StringBuilder(getScString(mmiCodeExt));
            sb.append("\n");
            if (ar.getException() != null) {
                mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
                if (ar.getException() instanceof ImsExceptionExt) {
                    sb.append(getImsExceptionMessage(mmiCodeExt, ar.getException()));
                } else {
                    sb.append(getErrorMessage(mmiCodeExt, ar));
                }
            } else {
                handleHwImsSuppSvcQueryNormal(sb, mmiCodeExt, ar);
            }
            mmiCodeExt.setMessage(sb);
            if (phoneExt != null) {
                phoneExt.onMMIDone(mmiCodeExt);
            }
        } else if (phoneExt != null) {
            phoneExt.processUtNoConnectionException(mmiCodeExt);
        }
    }

    private static void handleHwImsSuppSvcQueryNormal(StringBuilder sb, MmiCodeExt mmiCodeExt, AsyncResultEx ar) {
        mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
        Context context = mmiCodeExt.getContext();
        if (!isContextNull(mmiCodeExt, mmiCodeExt.getPhone(), context)) {
            if (ar.getResult() instanceof Bundle) {
                RlogEx.i(LOG_TAG, "onHwImsSuppSvcQueryComplete : Received CLIP/COLP/COLR Response.");
                int status = mmiCodeExt.getStatus((Bundle) ar.getResult());
                if (status != -1) {
                    RlogEx.i(LOG_TAG, "onHwImsSuppSvcQueryComplete : ImsSsInfo mStatus = " + status);
                    if (status == 0) {
                        sb.append(getCustSsToastString(context, false, true));
                        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                    } else if (status == 1) {
                        sb.append(getCustSsToastString(context, true, true));
                        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                    } else {
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                    }
                } else {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                }
            } else {
                RlogEx.i(LOG_TAG, "onHwImsSuppSvcQueryComplete : Received Call Barring Response.");
                if (((int[]) ar.getResult())[0] != 0) {
                    sb.append(getCustSsToastString(context, true, true));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                    return;
                }
                sb.append(getCustSsToastString(context, false, true));
                mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
            }
        }
    }

    private static void onHwImsSetComplete(MmiCodeExt mmiCodeExt, Message msg, AsyncResultEx ar) {
        if (mmiCodeExt != null) {
            PhoneExt phoneExt = mmiCodeExt.getPhone();
            if (!isUtNoConnectionException(phoneExt, (Exception) ar.getException())) {
                StringBuilder sb = new StringBuilder(getScString(mmiCodeExt));
                sb.append("\n");
                Context context = mmiCodeExt.getContext();
                if (!isContextNull(mmiCodeExt, phoneExt, context)) {
                    if (ar.getException() != null) {
                        handleHwImsSetException(sb, mmiCodeExt, ar);
                    } else if (mmiCodeExt.isActivate()) {
                        handleMmiCodeActivate(phoneExt, mmiCodeExt, sb, context);
                    } else if (mmiCodeExt.isDeactivate()) {
                        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                        sb.append(getCustSsToastString(context, false, false));
                        if (SC_CLIR.equals(mmiCodeExt.getSc()) && phoneExt != null) {
                            phoneExt.saveClirSetting(2);
                        }
                    } else if (mmiCodeExt.isRegister()) {
                        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("serviceRegistered")));
                    } else if (mmiCodeExt.isErasure()) {
                        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("serviceErased")));
                    } else {
                        mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                    }
                    mmiCodeExt.setMessage(sb);
                    if (ar.getException() == null) {
                        if (phoneExt != null) {
                            phoneExt.onMMIDone(mmiCodeExt);
                        }
                    } else if ((ar.getException() instanceof CommandExceptionExt) && phoneExt != null) {
                        phoneExt.onMMIDone(mmiCodeExt, ar.getException());
                    }
                }
            } else if (phoneExt != null) {
                phoneExt.processUtNoConnectionException(mmiCodeExt);
            }
        }
    }

    private static void handleMmiCodeActivate(PhoneExt phoneExt, MmiCodeExt mmiCodeExt, StringBuilder sb, Context context) {
        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
        if (mmiCodeExt.getHwCallFwdReg()) {
            sb.append(context.getText(HwPartResourceUtils.getResourceId("serviceRegistered")));
        } else {
            sb.append(getCustSsToastString(context, true, false));
        }
        if (SC_CLIR.equals(mmiCodeExt.getSc()) && phoneExt != null) {
            phoneExt.saveClirSetting(1);
        }
    }

    private static void handleHwImsSetException(StringBuilder sb, MmiCodeExt mmiCodeExt, AsyncResultEx ar) {
        mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
        if (ar.getException() instanceof CommandExceptionExt) {
            CommandExceptionExt.Error err = ar.getException().getCommandError();
            String message = ar.getException().getMessage();
            Context context = mmiCodeExt.getContext();
            if (!isContextNull(mmiCodeExt, mmiCodeExt.getPhone(), context)) {
                if (err == CommandExceptionExt.Error.PASSWORD_INCORRECT) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("passwordIncorrect")));
                } else if (message == null || message.isEmpty() || !message.contains(CONNECT_MESSAGE_ERRORCODE)) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                } else {
                    sb.append(message.substring(0, message.indexOf(CONNECT_MESSAGE_ERRORCODE)));
                    RlogEx.i(LOG_TAG, "onHwImsSetComplete : errorMessage = " + ((Object) sb));
                }
            }
        } else {
            sb.append(getImsExceptionMessage(mmiCodeExt, ar.getException()));
        }
    }

    private static void onHwImsQueryClirComplete(MmiCodeExt mmiCodeExt, AsyncResultEx ar) {
        PhoneExt phoneExt = mmiCodeExt.getPhone();
        if (!isUtNoConnectionException(phoneExt, (Exception) ar.getException())) {
            StringBuilder sb = new StringBuilder(getScString(mmiCodeExt));
            sb.append("\n");
            mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
            if (ar.getException() == null) {
                int[] clirInfo = null;
                if (ar.getResult() instanceof Bundle) {
                    Bundle ssInfo = (Bundle) ar.getResult();
                    if (ssInfo != null) {
                        clirInfo = ssInfo.getIntArray(UT_BUNDLE_CLIR);
                    }
                } else {
                    clirInfo = (int[]) ar.getResult();
                }
                if (clirInfo == null || clirInfo.length < 2) {
                    RlogEx.i(LOG_TAG, "CLIR param invalid.");
                    mmiCodeExt.setMessage(sb);
                    if (phoneExt != null) {
                        phoneExt.onMMIDone(mmiCodeExt);
                        return;
                    }
                    return;
                }
                RlogEx.i(LOG_TAG, "CLIR param n=" + clirInfo[0] + " m=" + clirInfo[1]);
                Context context = mmiCodeExt.getContext();
                if (!isContextNull(mmiCodeExt, phoneExt, context)) {
                    int i = clirInfo[1];
                    if (i == 0) {
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("serviceNotProvisioned")));
                        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                    } else if (i == 1) {
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("CLIRPermanent")));
                        mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                    } else if (i == 3) {
                        handlePresentationRestrictedTemporary(sb, mmiCodeExt, clirInfo[0]);
                    } else if (i != 4) {
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                        mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
                    } else {
                        handlePresentationAllowedTemporary(sb, mmiCodeExt, clirInfo[0]);
                    }
                } else {
                    return;
                }
            } else if (ar.getException() instanceof ImsExceptionExt) {
                sb.append(getImsExceptionMessage(mmiCodeExt, ar.getException()));
            }
            mmiCodeExt.setMessage(sb);
            if (phoneExt != null) {
                phoneExt.onMMIDone(mmiCodeExt);
            }
        } else if (phoneExt != null) {
            phoneExt.processUtNoConnectionException(mmiCodeExt);
        }
    }

    private static boolean isContextNull(MmiCodeExt mmiCodeExt, PhoneExt phoneExt, Context context) {
        if (context != null) {
            return false;
        }
        RlogEx.i(LOG_TAG, "context is null, return failure.");
        CommandExceptionExt exc = new CommandExceptionExt(CommandExceptionExt.Error.GENERIC_FAILURE);
        if (phoneExt == null) {
            return true;
        }
        phoneExt.onMMIDone(mmiCodeExt, exc);
        return true;
    }

    private static void handlePresentationRestrictedTemporary(StringBuilder sb, MmiCodeExt mmiCodeExt, int parameterN) {
        if (sb != null && mmiCodeExt != null) {
            PhoneExt phoneExt = mmiCodeExt.getPhone();
            Context context = mmiCodeExt.getContext();
            if (!isContextNull(mmiCodeExt, phoneExt, context)) {
                if (parameterN == 0) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("CLIRDefaultOnNextCallOn")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                } else if (parameterN == 1) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("CLIRDefaultOnNextCallOn")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                } else if (parameterN != 2) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
                } else {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("CLIRDefaultOnNextCallOff")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                }
            }
        }
    }

    private static void handlePresentationAllowedTemporary(StringBuilder sb, MmiCodeExt mmiCodeExt, int parameterN) {
        if (sb != null && mmiCodeExt != null) {
            PhoneExt phoneExt = mmiCodeExt.getPhone();
            Context context = mmiCodeExt.getContext();
            if (!isContextNull(mmiCodeExt, phoneExt, context)) {
                if (parameterN == 0) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("CLIRDefaultOffNextCallOff")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                } else if (parameterN == 1) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("CLIRDefaultOffNextCallOn")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                } else if (parameterN != 2) {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
                } else {
                    sb.append(context.getText(HwPartResourceUtils.getResourceId("CLIRDefaultOffNextCallOff")));
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                }
            }
        }
    }

    private static void onHwImsQueryCfComplete(MmiCodeExt mmiCodeExt, AsyncResultEx ar) {
        PhoneExt phoneExt = mmiCodeExt.getPhone();
        if (!isUtNoConnectionException(phoneExt, (Exception) ar.getException())) {
            StringBuilder sb = new StringBuilder(getScString(mmiCodeExt));
            sb.append("\n");
            if (ar.getException() != null) {
                mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
                if (ar.getException() instanceof ImsExceptionExt) {
                    sb.append(getImsExceptionMessage(mmiCodeExt, ar.getException()));
                } else {
                    sb.append(getErrorMessage(mmiCodeExt, ar));
                }
            } else {
                CallForwardInfoExt[] infos = CallForwardInfoExt.fromArray(ar.getResult());
                if (infos.length == 0) {
                    Context context = mmiCodeExt.getContext();
                    if (context != null) {
                        sb.append(getCustSsToastString(context, false, true));
                    }
                    mmiCodeExt.setVoiceCallForwardingFlag(1, false, (String) null);
                } else {
                    setImsTbToSb(sb, infos, mmiCodeExt);
                }
                mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
            }
            mmiCodeExt.setMessage(sb);
            if (phoneExt != null) {
                phoneExt.onMMIDone(mmiCodeExt);
            }
        } else if (phoneExt != null) {
            phoneExt.processUtNoConnectionException(mmiCodeExt);
        }
    }

    private static void setImsTbToSb(StringBuilder sb, CallForwardInfoExt[] infos, MmiCodeExt mmiCodeExt) {
        SpannableStringBuilder tb = new SpannableStringBuilder();
        for (int serviceClassMask = 1; serviceClassMask <= 128; serviceClassMask <<= 1) {
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if ((infos[i].getServiceClass() & serviceClassMask) != 0) {
                    tb.append(mmiCodeExt.makeCFQueryResultMessageEx(infos[i], serviceClassMask));
                    tb.append((CharSequence) "\n");
                }
            }
        }
        sb.append((CharSequence) tb);
    }

    private static void onHwImsQueryComplete(MmiCodeExt mmiCodeExt, AsyncResultEx ar) {
        PhoneExt phoneExt = mmiCodeExt.getPhone();
        if (!isUtNoConnectionException(phoneExt, (Exception) ar.getException())) {
            StringBuilder sb = new StringBuilder(getScString(mmiCodeExt));
            sb.append("\n");
            if (ar.getException() != null) {
                mmiCodeExt.setState(MmiCodeExt.StateExt.FAILED);
                if (ar.getException() instanceof ImsExceptionExt) {
                    sb.append(getImsExceptionMessage(mmiCodeExt, ar.getException()));
                } else {
                    sb.append(getErrorMessage(mmiCodeExt, ar));
                }
            } else {
                int[] ints = (int[]) ar.getResult();
                Context context = mmiCodeExt.getContext();
                if (!isContextNull(mmiCodeExt, phoneExt, context)) {
                    if (ints.length == 0) {
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                    } else if (ints[0] == 0) {
                        sb.append(getCustSsToastString(context, false, true));
                    } else if (SC_WAIT.equals(mmiCodeExt.getSc())) {
                        sb.append(mmiCodeExt.createQueryCallWaitingResultMessageEx(ints[1]));
                    } else if (ints[0] == 1) {
                        sb.append(getCustSsToastString(context, true, true));
                    } else {
                        sb.append(context.getText(HwPartResourceUtils.getResourceId("mmiError")));
                    }
                    mmiCodeExt.setState(MmiCodeExt.StateExt.COMPLETE);
                } else {
                    return;
                }
            }
            mmiCodeExt.setMessage(sb);
            if (phoneExt != null) {
                phoneExt.onMMIDone(mmiCodeExt);
            }
        } else if (phoneExt != null) {
            phoneExt.processUtNoConnectionException(mmiCodeExt);
        }
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
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        if (!getHwMmicodeCustom()) {
            return context.getText(HwPartResourceUtils.getResourceId("CfMmi"));
        }
        if (SC_CFU.equals(sc)) {
            return context.getText(HwPartResourceUtils.getResourceId("CfuMmi"));
        }
        if (SC_CFB.equals(sc)) {
            return context.getText(HwPartResourceUtils.getResourceId("CfbMmi"));
        }
        if (SC_CFNRY.equals(sc)) {
            return context.getText(HwPartResourceUtils.getResourceId("CfNryMmi"));
        }
        if (SC_CFNR.equals(sc)) {
            return context.getText(HwPartResourceUtils.getResourceId("CfNrcMmi"));
        }
        return context.getText(HwPartResourceUtils.getResourceId("CfMmi"));
    }

    private static CharSequence getCustSsToastString(Context context, boolean serviceEnable, boolean isQuery) {
        HwCustMmiCode hwCustMmiCode = mHwCustMmiCode;
        if (hwCustMmiCode != null && hwCustMmiCode.isSsToastSwitchEnabled()) {
            return mHwCustMmiCode.getCustSsToastString(context, serviceEnable, isQuery);
        }
        if (serviceEnable) {
            return context.getText(HwPartResourceUtils.getResourceId("serviceEnabled"));
        }
        return context.getText(HwPartResourceUtils.getResourceId("serviceDisabled"));
    }

    public static boolean processSendUssdInImsCall(MmiCodeExt mmiCodeExt, PhoneExt phoneExt) {
        Context context = null;
        if (mmiCodeExt != null) {
            context = mmiCodeExt.getContext();
        }
        if (context == null) {
            RlogEx.w(LOG_TAG, "context is null.");
            return false;
        }
        CarrierConfigManager cfgManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (cfgManager == null) {
            RlogEx.w(LOG_TAG, "Carrier config service is not available.");
            return false;
        }
        int subId = -1;
        if (phoneExt != null) {
            subId = phoneExt.getSubId();
        }
        PersistableBundle b = cfgManager.getConfigForSubId(subId);
        if (b == null) {
            RlogEx.w(LOG_TAG, "Can't get the config. subId = " + subId);
            return false;
        } else if (!b.getBoolean("carrier_forbid_ussd_when_ims_calling_bool") || phoneExt == null || !phoneExt.isInCallHw()) {
            return false;
        } else {
            Message onCompleted = mmiCodeExt.obtainMessage(4, mmiCodeExt);
            AsyncResultEx.forMessage(onCompleted).setException(new CommandExceptionExt(CommandExceptionExt.Error.GENERIC_FAILURE));
            onCompleted.sendToTarget();
            return true;
        }
    }

    public static boolean needUnEscapeHtmlforUssdMsg(PhoneExt phone) {
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

    public static boolean isShortCodeHw(String dialString, PhoneExt phone) {
        String[] hwMmiCodes;
        if (!(phone == null || dialString == null || dialString.length() > 2)) {
            String hwMmiCodeStr = SystemPropertiesEx.get("ro.config.hw_mmi_code", "-1");
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
        return false;
    }

    public static int removeUssdCust(PhoneExt phone) {
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

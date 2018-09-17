package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.SpannableStringBuilder;
import com.android.ims.ImsException;
import com.android.ims.ImsSsInfo;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.HwGsmAlphabet;
import com.android.internal.telephony.MmiCode.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.imsphone.HwImsPhoneMmiCode;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwGsmMmiCode {
    private static final int CLIR_DEFAULT = 0;
    private static final int CLIR_INVOCATION = 1;
    private static final int CLIR_NOT_PROVISIONED = 0;
    private static final int CLIR_PRESENTATION_ALLOWED_TEMPORARY = 4;
    private static final int CLIR_PRESENTATION_RESTRICTED_TEMPORARY = 3;
    private static final int CLIR_PROVISIONED_PERMANENT = 1;
    private static final int CLIR_SUPPRESSION = 2;
    static final int EVENT_HWIMS_BASE = 1000;
    static final int EVENT_HWIMS_GET_CLIR_COMPLETE = 1004;
    static final int EVENT_HWIMS_QUERY_CF_COMPLETE = 1006;
    static final int EVENT_HWIMS_QUERY_COMPLETE = 1002;
    static final int EVENT_HWIMS_SET_CFF_COMPLETE = 1005;
    static final int EVENT_HWIMS_SET_COMPLETE = 1001;
    static final int EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE = 1003;
    static final String HW_OPTA = SystemProperties.get("ro.config.hw_opta", "0");
    static final String HW_OPTB = SystemProperties.get("ro.config.hw_optb", "0");
    static final String LOG_TAG = "HwGsmMmiCode";
    public static final int MATCH_GROUP_DIALING_NUMBER = 16;
    public static final int MATCH_GROUP_PWD_CONFIRM = 15;
    public static final int MATCH_GROUP_SIA = 6;
    public static final int MATCH_GROUP_SIB = 9;
    public static final int MATCH_GROUP_SIC = 12;
    private static final int NUM_PRESENTATION_ALLOWED = 0;
    private static final int NUM_PRESENTATION_RESTRICTED = 1;
    static final String SC_CFB = "67";
    static final String SC_CFNR = "62";
    static final String SC_CFNRy = "61";
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
    static String huaweiMmiIgnoreList = SystemProperties.get("ro.config.hw_mmi_ignore_list", "null");
    static String huaweiMmiMatchList = SystemProperties.get("ro.config.hw_mmi_match_list", "null");
    static boolean mPromptUserBadPin2 = SystemProperties.getBoolean("ro.config.prompt_bad_pin2", true);
    private static final boolean mToastSwitch = SystemProperties.getBoolean("ro.config.hw_ss_toast", false);
    static Pattern sPatternHuaweiMMICode = Pattern.compile("(([\\*\\#\\d]{1,10},)*([\\*\\#\\d]{1,10})?)");
    public static final Pattern sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})((\\*#|\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*)((\\*|#)([^*#]*))?)?)?)?#)(.*)");

    public static boolean isShortCodeCustomization() {
        if (SystemProperties.getBoolean("gsm.hw.matchnum.vmn_shortcode", false)) {
            return true;
        }
        return false;
    }

    public static String convertUssdMessage(String ussdMessage) {
        if (!HW_OPTA.equals("27") || !HW_OPTB.equals("604")) {
            return ussdMessage;
        }
        char[] ussdChr = ussdMessage.toCharArray();
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
        switch (Integer.parseInt(si, 10)) {
            case 10:
                return 13;
            case 11:
                return 1;
            case 12:
                return 12;
            case 13:
                return 4;
            case 16:
                return 8;
            case 19:
                return 5;
            case 20:
                return 48;
            case 21:
                return 160;
            case 22:
                return 80;
            case HwVSimConstants.EVENT_NETWORK_SCAN_COMPLETED /*24*/:
                return 16;
            case HwVSimConstants.CMD_GET_DEVSUBMODE /*25*/:
                return 32;
            case HwVSimConstants.EVENT_GET_DEVSUBMODE_DONE /*26*/:
                return 17;
            case 99:
                return 64;
            default:
                throw new RuntimeException("unsupported MMI service code " + si);
        }
    }

    public static boolean isStringHuaweiIgnoreCode(String dialString) {
        Rlog.d(LOG_TAG, "isHuaweiIgnoreCode");
        if (dialString == null) {
            Rlog.e(LOG_TAG, "isStringHuaweiIgnoreCode null dial string");
            return false;
        } else if (isStringHuaweiCustCode(dialString)) {
            return false;
        } else {
            if (huaweiMmiIgnoreList.equals("null")) {
                Rlog.e(LOG_TAG, "isStringHuaweiIgnoreCode, null ignore code list");
            } else if (huaweiMmiIgnoreList.equals("ALL")) {
                Rlog.e(LOG_TAG, "isStringHuaweiIgnoreCode, block all codes");
                return true;
            } else {
                Matcher m = sPatternHuaweiMMICode.matcher(huaweiMmiIgnoreList);
                if (m.matches()) {
                    String[] notHwMmiCodes = m.group(1).split(",");
                    Rlog.d(LOG_TAG, "isHuaweiIgnoreCode,group(1)=" + m.group(1) + ", size()=" + notHwMmiCodes.length);
                    if (Arrays.asList(notHwMmiCodes).contains(dialString)) {
                        return true;
                    }
                }
                Rlog.d(LOG_TAG, "isHuaweiIgnoreCode, not a ignore mmi code");
            }
            return false;
        }
    }

    public static boolean isStringHuaweiCustCode(String dialString) {
        Rlog.d(LOG_TAG, "isStringHuaweiCustCode");
        if (dialString == null) {
            Rlog.e(LOG_TAG, "isStringHuaweiCustCode null dial string");
            return false;
        } else if (huaweiMmiMatchList.equals("null")) {
            Rlog.e(LOG_TAG, "isStringHuaweiCustCode, null match code list");
            return false;
        } else {
            Matcher m = sPatternHuaweiMMICode.matcher(huaweiMmiMatchList);
            if (m.matches()) {
                String[] hwMmiCodes = m.group(1).split(",");
                Rlog.d(LOG_TAG, "isStringHuaweiCustCode,group(1)=" + m.group(1) + ", size()=" + hwMmiCodes.length);
                if (Arrays.asList(hwMmiCodes).contains(dialString)) {
                    return true;
                }
            }
            Rlog.d(LOG_TAG, "isStringHuaweiCustCode, not a customized mmi code");
            return false;
        }
    }

    public static CharSequence processgoodPinString(Context context, String sc) {
        Rlog.d(LOG_TAG, "processgoodPinString enter");
        CharSequence result = context.getText(17039454);
        if (mPromptUserBadPin2 && (SC_PIN2.equals(sc) || SC_PUK2.equals(sc))) {
            return context.getText(33685779);
        }
        return context.getText(17039454);
    }

    public static CharSequence processBadPinString(Context context, String sc) {
        Rlog.d(LOG_TAG, "processBadPinString enter");
        CharSequence result = context.getText(17039678);
        if (mPromptUserBadPin2 && (SC_PIN2.equals(sc) || SC_PUK2.equals(sc))) {
            return context.getText(33685780);
        }
        return context.getText(17039678);
    }

    public static int handlePasswordError(String sc) {
        Rlog.d(LOG_TAG, "handlePasswordError enter");
        if (!mPromptUserBadPin2) {
            return 17040442;
        }
        if (SC_PIN2.equals(sc) || SC_PUK2.equals(sc)) {
            return 33685782;
        }
        return 17040442;
    }

    public static int showMmiError(int sc) {
        if (SystemProperties.getBoolean("ro.config.hw_show_mmiError", false)) {
            return 17040448;
        }
        return sc;
    }

    public static boolean processImsPhoneMmiCode(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        if (imsPhone == null || gsmMmiCode == null) {
            Rlog.d(LOG_TAG, "imsPhone is null, don't process IMSPhone Mmi Code.");
            return false;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_CLIP)) {
            Rlog.d(LOG_TAG, "is CLIP");
            HwImsPhoneMmiCode.isUnSupportMMICode(gsmMmiCode.mSc, imsPhone.getPhoneId());
            if (gsmMmiCode.isInterrogate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().queryCLIP(gsmMmiCode.obtainMessage(EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
                } catch (ImsException ex) {
                    Rlog.e(LOG_TAG, "Exception in getUtInterface : " + ex);
                }
            } else if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCLIP(gsmMmiCode.isActivate(), gsmMmiCode.obtainMessage(1001, gsmMmiCode));
                } catch (ImsException e) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCLIP.");
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
            return true;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_CLIR)) {
            Rlog.d(LOG_TAG, "is CLIR");
            if (gsmMmiCode.isActivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCLIR(1, gsmMmiCode.obtainMessage(1001, gsmMmiCode));
                } catch (ImsException ex2) {
                    Rlog.e(LOG_TAG, "Exception in getUtInterface : " + ex2);
                }
            } else if (gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCLIR(2, gsmMmiCode.obtainMessage(1001, gsmMmiCode));
                } catch (ImsException ex22) {
                    Rlog.e(LOG_TAG, "Exception in getUtInterface : " + ex22);
                }
            } else if (gsmMmiCode.isInterrogate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().queryCLIR(gsmMmiCode.obtainMessage(EVENT_HWIMS_GET_CLIR_COMPLETE, gsmMmiCode));
                } catch (ImsException ex222) {
                    Rlog.e(LOG_TAG, "Exception in getUtInterface : " + ex222);
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
            return true;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_COLP)) {
            Rlog.d(LOG_TAG, "is COLP");
            HwImsPhoneMmiCode.isUnSupportMMICode(gsmMmiCode.mSc, imsPhone.getPhoneId());
            if (gsmMmiCode.isActivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLP(true, gsmMmiCode.obtainMessage(1001, gsmMmiCode));
                } catch (ImsException e2) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLP.");
                }
            } else if (gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLP(false, gsmMmiCode.obtainMessage(1001, gsmMmiCode));
                } catch (ImsException e3) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLP.");
                }
            } else if (gsmMmiCode.isInterrogate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().queryCOLP(gsmMmiCode.obtainMessage(EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
                } catch (ImsException e4) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for queryCOLP.");
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
            return true;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_COLR)) {
            Rlog.d(LOG_TAG, "is COLR");
            HwImsPhoneMmiCode.isUnSupportMMICode(gsmMmiCode.mSc, imsPhone.getPhoneId());
            if (gsmMmiCode.isActivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLR(0, gsmMmiCode.obtainMessage(1001, gsmMmiCode));
                } catch (ImsException e5) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLR.");
                }
            } else if (gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLR(1, gsmMmiCode.obtainMessage(1001, gsmMmiCode));
                } catch (ImsException e6) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLR.");
                }
            } else if (gsmMmiCode.isInterrogate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().queryCOLR(gsmMmiCode.obtainMessage(EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
                } catch (ImsException e7) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for queryCOLR.");
                }
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
            return true;
        } else if (GsmMmiCode.isServiceCodeCallForwarding(gsmMmiCode.mSc)) {
            Rlog.d(LOG_TAG, "is CF");
            String dialingNumber = gsmMmiCode.mSia;
            int serviceClass = siToServiceClass(gsmMmiCode.mSib);
            int reason = GsmMmiCode.scToCallForwardReasonEx(gsmMmiCode.mSc);
            int time = GsmMmiCode.siToTimeEx(gsmMmiCode.mSic);
            if (gsmMmiCode.isInterrogate()) {
                imsPhone.getCallForwardingOption(reason, gsmMmiCode.obtainMessage(EVENT_HWIMS_QUERY_CF_COMPLETE, gsmMmiCode));
            } else {
                int cfAction;
                if (gsmMmiCode.isActivate()) {
                    if (isEmptyOrNull(dialingNumber)) {
                        cfAction = 1;
                        gsmMmiCode.setHwCallFwgReg(false);
                    } else {
                        cfAction = 3;
                        gsmMmiCode.setHwCallFwgReg(true);
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
                int isEnableDesired = (cfAction == 1 || cfAction == 3) ? 1 : 0;
                Rlog.d(LOG_TAG, "is CF setCallForward");
                imsPhone.setCallForwardingOption(cfAction, reason, dialingNumber, serviceClass, time, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_CFF_COMPLETE, isSettingUnconditionalVoice, isEnableDesired, gsmMmiCode));
            }
            return true;
        } else if (GsmMmiCode.isServiceCodeCallBarring(gsmMmiCode.mSc)) {
            String password = gsmMmiCode.mSia;
            String facility = GsmMmiCode.scToBarringFacility(gsmMmiCode.mSc);
            if (gsmMmiCode.isInterrogate()) {
                ((ImsPhone) imsPhone).getCallBarring(facility, gsmMmiCode.obtainMessage(EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
            } else if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
                ((ImsPhone) imsPhone).setCallBarring(facility, gsmMmiCode.isActivate(), password, gsmMmiCode.obtainMessage(1001, gsmMmiCode));
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
            return true;
        } else if (gsmMmiCode.mSc == null || !gsmMmiCode.mSc.equals(SC_WAIT)) {
            return false;
        } else {
            if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
                imsPhone.setCallWaiting(gsmMmiCode.isActivate(), gsmMmiCode.obtainMessage(1001, gsmMmiCode));
            } else if (gsmMmiCode.isInterrogate()) {
                imsPhone.getCallWaiting(gsmMmiCode.obtainMessage(EVENT_HWIMS_QUERY_COMPLETE, gsmMmiCode));
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
            return true;
        }
    }

    public static void handleMessageGsmMmiCode(GsmMmiCode gsmMmiCode, Message msg) {
        switch (msg.what) {
            case 1001:
                onHwImsSetComplete(gsmMmiCode, msg, (AsyncResult) msg.obj);
                return;
            case EVENT_HWIMS_QUERY_COMPLETE /*1002*/:
                onHwImsQueryComplete(gsmMmiCode, (AsyncResult) msg.obj);
                return;
            case EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE /*1003*/:
                onHwImsSuppSvcQueryComplete(gsmMmiCode, msg.obj);
                return;
            case EVENT_HWIMS_GET_CLIR_COMPLETE /*1004*/:
                onHwImsQueryClirComplete(gsmMmiCode, (AsyncResult) msg.obj);
                return;
            case EVENT_HWIMS_SET_CFF_COMPLETE /*1005*/:
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null && msg.arg1 == 1) {
                    boolean cffEnabled = msg.arg2 == 1;
                    if (gsmMmiCode.mIccRecords != null) {
                        gsmMmiCode.mIccRecords.setVoiceCallForwardingFlag(1, cffEnabled, gsmMmiCode.mDialingNumber);
                    }
                }
                onHwImsSetComplete(gsmMmiCode, msg, ar);
                return;
            case EVENT_HWIMS_QUERY_CF_COMPLETE /*1006*/:
                onHwImsQueryCfComplete(gsmMmiCode, (AsyncResult) msg.obj);
                return;
            default:
                return;
        }
    }

    private static CharSequence getScString(GsmMmiCode gsmMmiCode) {
        if (!(gsmMmiCode == null || gsmMmiCode.mSc == null)) {
            if (GsmMmiCode.isServiceCodeCallBarring(gsmMmiCode.mSc)) {
                return gsmMmiCode.mContext.getText(17039386);
            }
            if (GsmMmiCode.isServiceCodeCallForwarding(gsmMmiCode.mSc)) {
                return getCallForwardingString(gsmMmiCode.mContext, gsmMmiCode.mSc);
            }
            if (gsmMmiCode.mSc.equals(SC_CLIP)) {
                return gsmMmiCode.mContext.getText(17039397);
            }
            if (gsmMmiCode.mSc.equals(SC_CLIR)) {
                return gsmMmiCode.mContext.getText(17039398);
            }
            if (gsmMmiCode.mSc.equals(SC_PWD)) {
                return gsmMmiCode.mContext.getText(17039455);
            }
            if (gsmMmiCode.mSc.equals(SC_WAIT)) {
                return gsmMmiCode.mContext.getText(17039404);
            }
            if (gsmMmiCode.mSc.equals(SC_COLP)) {
                return gsmMmiCode.mContext.getText(17039402);
            }
            if (gsmMmiCode.mSc.equals(SC_COLR)) {
                return gsmMmiCode.mContext.getText(17039403);
            }
        }
        return "";
    }

    private static CharSequence getErrorMessage(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        return gsmMmiCode.mContext.getText(17040448);
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
        } else if ((e instanceof CommandException) && ((CommandException) e).getCommandError() == Error.UT_NO_CONNECTION) {
            return true;
        }
        return false;
    }

    private static void onHwImsSuppSvcQueryComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else {
                sb.append(getErrorMessage(gsmMmiCode, ar));
            }
        } else {
            gsmMmiCode.mState = State.FAILED;
            if (ar.result instanceof Bundle) {
                Rlog.d(LOG_TAG, "onHwImsSuppSvcQueryComplete : Received CLIP/COLP/COLR Response.");
                ImsSsInfo ssInfo = (ImsSsInfo) ar.result.getParcelable("imsSsInfo");
                if (ssInfo != null) {
                    Rlog.d(LOG_TAG, "onHwImsSuppSvcQueryComplete : ImsSsInfo mStatus = " + ssInfo.mStatus);
                    if (ssInfo.mStatus == 0) {
                        if (mToastSwitch) {
                            sb.append(gsmMmiCode.mContext.getText(17040848));
                        } else {
                            sb.append(gsmMmiCode.mContext.getText(17040961));
                        }
                        gsmMmiCode.mState = State.COMPLETE;
                    } else if (ssInfo.mStatus == 1) {
                        if (mToastSwitch) {
                            sb.append(gsmMmiCode.mContext.getText(17040849));
                        } else {
                            sb.append(gsmMmiCode.mContext.getText(17040962));
                        }
                        gsmMmiCode.mState = State.COMPLETE;
                    } else {
                        sb.append(gsmMmiCode.mContext.getText(17040448));
                    }
                } else {
                    sb.append(gsmMmiCode.mContext.getText(17040448));
                }
            } else {
                Rlog.d(LOG_TAG, "onHwImsSuppSvcQueryComplete : Received Call Barring Response.");
                if (ar.result[0] != 0) {
                    if (mToastSwitch) {
                        sb.append(gsmMmiCode.mContext.getText(17040849));
                    } else {
                        sb.append(gsmMmiCode.mContext.getText(17040962));
                    }
                    gsmMmiCode.mState = State.COMPLETE;
                } else {
                    if (mToastSwitch) {
                        sb.append(gsmMmiCode.mContext.getText(17040848));
                    } else {
                        sb.append(gsmMmiCode.mContext.getText(17040961));
                    }
                    gsmMmiCode.mState = State.COMPLETE;
                }
            }
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static void onHwImsSetComplete(GsmMmiCode gsmMmiCode, Message msg, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = State.FAILED;
            if (!(ar.exception instanceof CommandException)) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else if (((CommandException) ar.exception).getCommandError() == Error.PASSWORD_INCORRECT) {
                sb.append(gsmMmiCode.mContext.getText(17040537));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17040448));
            }
        } else if (gsmMmiCode.isActivate()) {
            gsmMmiCode.mState = State.COMPLETE;
            if (gsmMmiCode.getHwCallFwdReg()) {
                sb.append(gsmMmiCode.mContext.getText(17040966));
            } else if (mToastSwitch) {
                sb.append(gsmMmiCode.mContext.getText(17041040));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17040962));
            }
        } else if (gsmMmiCode.isDeactivate()) {
            gsmMmiCode.mState = State.COMPLETE;
            if (mToastSwitch) {
                sb.append(gsmMmiCode.mContext.getText(17041039));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17040961));
            }
        } else if (gsmMmiCode.isRegister()) {
            gsmMmiCode.mState = State.COMPLETE;
            sb.append(gsmMmiCode.mContext.getText(17040966));
        } else if (gsmMmiCode.isErasure()) {
            gsmMmiCode.mState = State.COMPLETE;
            sb.append(gsmMmiCode.mContext.getText(17040964));
        } else {
            gsmMmiCode.mState = State.FAILED;
            sb.append(gsmMmiCode.mContext.getText(17040448));
        }
        gsmMmiCode.mMessage = sb;
        if (ar.exception == null) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
        } else if (ar.exception instanceof CommandException) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, (CommandException) ar.exception);
        }
    }

    private static void onHwImsQueryClirComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        gsmMmiCode.mState = State.FAILED;
        if (ar.exception == null) {
            int[] clirInfo;
            if (ar.result instanceof Bundle) {
                clirInfo = ar.result.getIntArray("queryClir");
            } else {
                clirInfo = ar.result;
            }
            Rlog.d(LOG_TAG, "CLIR param n=" + clirInfo[0] + " m=" + clirInfo[1]);
            switch (clirInfo[1]) {
                case 0:
                    sb.append(gsmMmiCode.mContext.getText(17040965));
                    gsmMmiCode.mState = State.COMPLETE;
                    break;
                case 1:
                    sb.append(gsmMmiCode.mContext.getText(17039391));
                    gsmMmiCode.mState = State.COMPLETE;
                    break;
                case 3:
                    switch (clirInfo[0]) {
                        case 0:
                            sb.append(gsmMmiCode.mContext.getText(17039390));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case 1:
                            sb.append(gsmMmiCode.mContext.getText(17039390));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case 2:
                            sb.append(gsmMmiCode.mContext.getText(17039389));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(gsmMmiCode.mContext.getText(17040448));
                            gsmMmiCode.mState = State.FAILED;
                            break;
                    }
                case 4:
                    switch (clirInfo[0]) {
                        case 0:
                            sb.append(gsmMmiCode.mContext.getText(17039387));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case 1:
                            sb.append(gsmMmiCode.mContext.getText(17039388));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case 2:
                            sb.append(gsmMmiCode.mContext.getText(17039387));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(gsmMmiCode.mContext.getText(17040448));
                            gsmMmiCode.mState = State.FAILED;
                            break;
                    }
                default:
                    sb.append(gsmMmiCode.mContext.getText(17040448));
                    gsmMmiCode.mState = State.FAILED;
                    break;
            }
        } else if (ar.exception instanceof ImsException) {
            sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static void onHwImsQueryCfComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else {
                sb.append(getErrorMessage(gsmMmiCode, ar));
            }
        } else {
            CallForwardInfo[] infos = ar.result;
            if (infos.length == 0) {
                if (mToastSwitch) {
                    sb.append(gsmMmiCode.mContext.getText(17040848));
                } else {
                    sb.append(gsmMmiCode.mContext.getText(17040961));
                }
                if (gsmMmiCode.mIccRecords != null) {
                    gsmMmiCode.mIccRecords.setVoiceCallForwardingFlag(1, false, null);
                }
            } else {
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
                sb.append(tb);
            }
            gsmMmiCode.mState = State.COMPLETE;
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static void onHwImsQueryComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        if (isUtNoConnectionException((Exception) ar.exception)) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, new CommandException(Error.UT_NO_CONNECTION));
            return;
        }
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = State.FAILED;
            if (ar.exception instanceof ImsException) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else {
                sb.append(getErrorMessage(gsmMmiCode, ar));
            }
        } else {
            int[] ints = ar.result;
            if (ints.length == 0) {
                sb.append(gsmMmiCode.mContext.getText(17040448));
            } else if (ints[0] == 0) {
                if (mToastSwitch) {
                    sb.append(gsmMmiCode.mContext.getText(17040848));
                } else {
                    sb.append(gsmMmiCode.mContext.getText(17040961));
                }
            } else if (gsmMmiCode.mSc.equals(SC_WAIT)) {
                sb.append(gsmMmiCode.createQueryCallWaitingResultMessageEx(ints[1]));
            } else if (ints[0] != 1) {
                sb.append(gsmMmiCode.mContext.getText(17040448));
            } else if (mToastSwitch) {
                sb.append(gsmMmiCode.mContext.getText(17040849));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17040962));
            }
            gsmMmiCode.mState = State.COMPLETE;
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    public static CharSequence getCallForwardingString(Context context, String sc) {
        if (!SystemProperties.getBoolean("ro.config.hw_mmicode_custom", false)) {
            return context.getText(17039392);
        }
        if (sc.equals(SC_CFU)) {
            return context.getText(17039396);
        }
        if (sc.equals(SC_CFB)) {
            return context.getText(17039395);
        }
        if (sc.equals(SC_CFNRy)) {
            return context.getText(17039394);
        }
        if (sc.equals(SC_CFNR)) {
            return context.getText(17039393);
        }
        return context.getText(17039392);
    }
}

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
import com.android.internal.telephony.intelligentdataswitch.IDSConstants;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
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
    static final String HW_OPTA = null;
    static final String HW_OPTB = null;
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
    static String huaweiMmiIgnoreList;
    static String huaweiMmiMatchList;
    static boolean mPromptUserBadPin2;
    static Pattern sPatternHuaweiMMICode;
    public static final Pattern sPatternSuppService = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.HwGsmMmiCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.HwGsmMmiCode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.HwGsmMmiCode.<clinit>():void");
    }

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
        for (int i = NUM_PRESENTATION_ALLOWED; i < ussdChr.length; i += NUM_PRESENTATION_RESTRICTED) {
            ussdChr[i] = HwGsmAlphabet.ussd_7bit_ucs2_to_gsm_char_default(ussdChr[i]);
            ussdChr[i] = HwGsmAlphabet.util_UnicodeToGsm7DefaultExtended(ussdChr[i]);
        }
        return new String(ussdChr);
    }

    public static int siToServiceClass(String si) {
        if (si == null || si.length() == 0) {
            return NUM_PRESENTATION_ALLOWED;
        }
        switch (Integer.parseInt(si, 10)) {
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_NETWORK_TYPE /*10*/:
                return 13;
            case HwVSimUtilsInner.VSIM /*11*/:
                return NUM_PRESENTATION_RESTRICTED;
            case MATCH_GROUP_SIC /*12*/:
                return MATCH_GROUP_SIC;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_DC /*13*/:
                return CLIR_PRESENTATION_ALLOWED_TEMPORARY;
            case MATCH_GROUP_DIALING_NUMBER /*16*/:
                return 8;
            case HwVSimConstants.EVENT_SET_DSFLOWNVCFG_DONE /*19*/:
                return 5;
            case HwVSimConstants.CMD_SET_APN_READY /*20*/:
                return 48;
            case HwVSimConstants.EVENT_SET_APN_READY_DONE /*21*/:
                return 160;
            case HwVSimConstants.CMD_GET_SIM_STATE_VIA_SYSINFOEX /*22*/:
                return 80;
            case HwVSimConstants.EVENT_NETWORK_SCAN_COMPLETED /*24*/:
                return MATCH_GROUP_DIALING_NUMBER;
            case HwVSimConstants.CMD_GET_DEVSUBMODE /*25*/:
                return 32;
            case HwVSimConstants.EVENT_GET_DEVSUBMODE_DONE /*26*/:
                return 17;
            case IDSConstants.GSM_STRENGTH_UNKOUWN /*99*/:
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
                    String[] notHwMmiCodes = m.group(NUM_PRESENTATION_RESTRICTED).split(",");
                    Rlog.d(LOG_TAG, "isHuaweiIgnoreCode,group(1)=" + m.group(NUM_PRESENTATION_RESTRICTED) + ", size()=" + notHwMmiCodes.length);
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
                String[] hwMmiCodes = m.group(NUM_PRESENTATION_RESTRICTED).split(",");
                Rlog.d(LOG_TAG, "isStringHuaweiCustCode,group(1)=" + m.group(NUM_PRESENTATION_RESTRICTED) + ", size()=" + hwMmiCodes.length);
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
        CharSequence result = context.getText(17039543);
        if (mPromptUserBadPin2 && (SC_PIN2.equals(sc) || SC_PUK2.equals(sc))) {
            return context.getText(33685772);
        }
        return context.getText(17039543);
    }

    public static CharSequence processBadPinString(Context context, String sc) {
        Rlog.d(LOG_TAG, "processBadPinString enter");
        CharSequence result = context.getText(17039525);
        if (mPromptUserBadPin2 && (SC_PIN2.equals(sc) || SC_PUK2.equals(sc))) {
            return context.getText(33685773);
        }
        return context.getText(17039525);
    }

    public static int handlePasswordError(String sc) {
        Rlog.d(LOG_TAG, "handlePasswordError enter");
        if (!mPromptUserBadPin2) {
            return 17039527;
        }
        if (SC_PIN2.equals(sc) || SC_PUK2.equals(sc)) {
            return 33685775;
        }
        return 17039527;
    }

    public static int showMmiError(int sc) {
        if (SystemProperties.getBoolean("ro.config.hw_show_mmiError", false)) {
            return 17039516;
        }
        return sc;
    }

    public static boolean processImsPhoneMmiCode(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        if (imsPhone == null || gsmMmiCode == null) {
            Rlog.d(LOG_TAG, "imsPhone is null, don't process IMSPhone Mmi Code.");
            return false;
        } else if (gsmMmiCode.mSc != null && gsmMmiCode.mSc.equals(SC_CLIP)) {
            Rlog.d(LOG_TAG, "is CLIP");
            HwImsPhoneMmiCode.isUnSupportMMICode(gsmMmiCode.mSc);
            if (gsmMmiCode.isInterrogate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().queryCLIP(gsmMmiCode.obtainMessage(EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
                } catch (ImsException ex) {
                    Rlog.e(LOG_TAG, "Exception in getUtInterface : " + ex);
                }
            } else if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCLIP(gsmMmiCode.isActivate(), gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
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
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCLIR(NUM_PRESENTATION_RESTRICTED, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
                } catch (ImsException ex2) {
                    Rlog.e(LOG_TAG, "Exception in getUtInterface : " + ex2);
                }
            } else if (gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCLIR(CLIR_SUPPRESSION, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
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
            HwImsPhoneMmiCode.isUnSupportMMICode(gsmMmiCode.mSc);
            if (gsmMmiCode.isActivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLP(true, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
                } catch (ImsException e2) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLP.");
                }
            } else if (gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLP(false, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
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
            HwImsPhoneMmiCode.isUnSupportMMICode(gsmMmiCode.mSc);
            if (gsmMmiCode.isActivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLR(NUM_PRESENTATION_ALLOWED, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
                } catch (ImsException e5) {
                    Rlog.d(LOG_TAG, "Could not get UT handle for updateCOLR.");
                }
            } else if (gsmMmiCode.isDeactivate()) {
                try {
                    ((ImsPhoneCallTracker) imsPhone.getCallTracker()).getUtInterfaceEx().updateCOLR(NUM_PRESENTATION_RESTRICTED, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
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
                        cfAction = NUM_PRESENTATION_RESTRICTED;
                        gsmMmiCode.setHwCallFwgReg(false);
                    } else {
                        cfAction = CLIR_PRESENTATION_RESTRICTED_TEMPORARY;
                        gsmMmiCode.setHwCallFwgReg(true);
                    }
                } else if (gsmMmiCode.isDeactivate()) {
                    cfAction = NUM_PRESENTATION_ALLOWED;
                } else if (gsmMmiCode.isRegister()) {
                    cfAction = CLIR_PRESENTATION_RESTRICTED_TEMPORARY;
                } else if (gsmMmiCode.isErasure()) {
                    cfAction = CLIR_PRESENTATION_ALLOWED_TEMPORARY;
                } else {
                    throw new RuntimeException("invalid action");
                }
                int isSettingUnconditionalVoice = ((reason == 0 || reason == CLIR_PRESENTATION_ALLOWED_TEMPORARY) && ((serviceClass & NUM_PRESENTATION_RESTRICTED) != 0 || serviceClass == 0)) ? NUM_PRESENTATION_RESTRICTED : NUM_PRESENTATION_ALLOWED;
                int isEnableDesired = (cfAction == NUM_PRESENTATION_RESTRICTED || cfAction == CLIR_PRESENTATION_RESTRICTED_TEMPORARY) ? NUM_PRESENTATION_RESTRICTED : NUM_PRESENTATION_ALLOWED;
                Rlog.d(LOG_TAG, "is CF setCallForward");
                imsPhone.setCallForwardingOption(cfAction, reason, dialingNumber, time, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_CFF_COMPLETE, isSettingUnconditionalVoice, isEnableDesired, gsmMmiCode));
            }
            return true;
        } else if (GsmMmiCode.isServiceCodeCallBarring(gsmMmiCode.mSc)) {
            String password = gsmMmiCode.mSia;
            String facility = GsmMmiCode.scToBarringFacility(gsmMmiCode.mSc);
            if (gsmMmiCode.isInterrogate()) {
                ((ImsPhone) imsPhone).getCallBarring(facility, gsmMmiCode.obtainMessage(EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE, gsmMmiCode));
            } else if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
                ((ImsPhone) imsPhone).setCallBarring(facility, gsmMmiCode.isActivate(), password, gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
            return true;
        } else if (gsmMmiCode.mSc == null || !gsmMmiCode.mSc.equals(SC_WAIT)) {
            return false;
        } else {
            if (gsmMmiCode.isActivate() || gsmMmiCode.isDeactivate()) {
                imsPhone.setCallWaiting(gsmMmiCode.isActivate(), gsmMmiCode.obtainMessage(EVENT_HWIMS_SET_COMPLETE, gsmMmiCode));
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
            case EVENT_HWIMS_SET_COMPLETE /*1001*/:
                onHwImsSetComplete(gsmMmiCode, msg, (AsyncResult) msg.obj);
            case EVENT_HWIMS_QUERY_COMPLETE /*1002*/:
                onHwImsQueryComplete(gsmMmiCode, (AsyncResult) msg.obj);
            case EVENT_HWIMS_SUPP_SVC_QUERY_COMPLETE /*1003*/:
                onHwImsSuppSvcQueryComplete(gsmMmiCode, msg.obj);
            case EVENT_HWIMS_GET_CLIR_COMPLETE /*1004*/:
                onHwImsQueryClirComplete(gsmMmiCode, (AsyncResult) msg.obj);
            case EVENT_HWIMS_SET_CFF_COMPLETE /*1005*/:
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null && msg.arg1 == NUM_PRESENTATION_RESTRICTED) {
                    boolean cffEnabled = msg.arg2 == NUM_PRESENTATION_RESTRICTED;
                    if (gsmMmiCode.mIccRecords != null) {
                        gsmMmiCode.mIccRecords.setVoiceCallForwardingFlag(NUM_PRESENTATION_RESTRICTED, cffEnabled, gsmMmiCode.mDialingNumber);
                    }
                }
                onHwImsSetComplete(gsmMmiCode, msg, ar);
            case EVENT_HWIMS_QUERY_CF_COMPLETE /*1006*/:
                onHwImsQueryCfComplete(gsmMmiCode, (AsyncResult) msg.obj);
            default:
        }
    }

    private static CharSequence getScString(GsmMmiCode gsmMmiCode) {
        if (!(gsmMmiCode == null || gsmMmiCode.mSc == null)) {
            if (GsmMmiCode.isServiceCodeCallBarring(gsmMmiCode.mSc)) {
                return gsmMmiCode.mContext.getText(17039541);
            }
            if (GsmMmiCode.isServiceCodeCallForwarding(gsmMmiCode.mSc)) {
                return getCallForwardingString(gsmMmiCode.mContext, gsmMmiCode.mSc);
            }
            if (gsmMmiCode.mSc.equals(SC_CLIP)) {
                return gsmMmiCode.mContext.getText(17039535);
            }
            if (gsmMmiCode.mSc.equals(SC_CLIR)) {
                return gsmMmiCode.mContext.getText(17039536);
            }
            if (gsmMmiCode.mSc.equals(SC_PWD)) {
                return gsmMmiCode.mContext.getText(17039542);
            }
            if (gsmMmiCode.mSc.equals(SC_WAIT)) {
                return gsmMmiCode.mContext.getText(17039540);
            }
            if (gsmMmiCode.mSc.equals(SC_COLP)) {
                return gsmMmiCode.mContext.getText(17039537);
            }
            if (gsmMmiCode.mSc.equals(SC_COLR)) {
                return gsmMmiCode.mContext.getText(17039538);
            }
        }
        return "";
    }

    private static CharSequence getErrorMessage(GsmMmiCode gsmMmiCode, AsyncResult ar) {
        return gsmMmiCode.mContext.getText(17039516);
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

    private static void onHwImsSuppSvcQueryComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
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
                        sb.append(gsmMmiCode.mContext.getText(17039520));
                        gsmMmiCode.mState = State.COMPLETE;
                    } else if (ssInfo.mStatus == NUM_PRESENTATION_RESTRICTED) {
                        sb.append(gsmMmiCode.mContext.getText(17039518));
                        gsmMmiCode.mState = State.COMPLETE;
                    } else {
                        sb.append(gsmMmiCode.mContext.getText(17039516));
                    }
                } else {
                    sb.append(gsmMmiCode.mContext.getText(17039516));
                }
            } else {
                Rlog.d(LOG_TAG, "onHwImsSuppSvcQueryComplete : Received Call Barring Response.");
                if (ar.result[NUM_PRESENTATION_ALLOWED] != 0) {
                    sb.append(gsmMmiCode.mContext.getText(17039518));
                    gsmMmiCode.mState = State.COMPLETE;
                } else {
                    sb.append(gsmMmiCode.mContext.getText(17039520));
                    gsmMmiCode.mState = State.COMPLETE;
                }
            }
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    private static void onHwImsSetComplete(GsmMmiCode gsmMmiCode, Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString(gsmMmiCode));
        sb.append("\n");
        if (ar.exception != null) {
            gsmMmiCode.mState = State.FAILED;
            if (!(ar.exception instanceof CommandException)) {
                sb.append(getImsExceptionMessage(gsmMmiCode, ar.exception));
            } else if (((CommandException) ar.exception).getCommandError() == Error.PASSWORD_INCORRECT) {
                sb.append(gsmMmiCode.mContext.getText(17039523));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17039516));
            }
        } else if (gsmMmiCode.isActivate()) {
            gsmMmiCode.mState = State.COMPLETE;
            if (gsmMmiCode.getHwCallFwdReg()) {
                sb.append(gsmMmiCode.mContext.getText(17039521));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17039518));
            }
        } else if (gsmMmiCode.isDeactivate()) {
            gsmMmiCode.mState = State.COMPLETE;
            sb.append(gsmMmiCode.mContext.getText(17039520));
        } else if (gsmMmiCode.isRegister()) {
            gsmMmiCode.mState = State.COMPLETE;
            sb.append(gsmMmiCode.mContext.getText(17039521));
        } else if (gsmMmiCode.isErasure()) {
            gsmMmiCode.mState = State.COMPLETE;
            sb.append(gsmMmiCode.mContext.getText(17039522));
        } else {
            gsmMmiCode.mState = State.FAILED;
            sb.append(gsmMmiCode.mContext.getText(17039516));
        }
        gsmMmiCode.mMessage = sb;
        if (ar.exception == null) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
        } else if (ar.exception instanceof CommandException) {
            gsmMmiCode.mPhone.onMMIDone(gsmMmiCode, (CommandException) ar.exception);
        }
    }

    private static void onHwImsQueryClirComplete(GsmMmiCode gsmMmiCode, AsyncResult ar) {
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
            Rlog.d(LOG_TAG, "CLIR param n=" + clirInfo[NUM_PRESENTATION_ALLOWED] + " m=" + clirInfo[NUM_PRESENTATION_RESTRICTED]);
            switch (clirInfo[NUM_PRESENTATION_RESTRICTED]) {
                case NUM_PRESENTATION_ALLOWED /*0*/:
                    sb.append(gsmMmiCode.mContext.getText(17039554));
                    gsmMmiCode.mState = State.COMPLETE;
                    break;
                case NUM_PRESENTATION_RESTRICTED /*1*/:
                    sb.append(gsmMmiCode.mContext.getText(17039555));
                    gsmMmiCode.mState = State.COMPLETE;
                    break;
                case CLIR_PRESENTATION_RESTRICTED_TEMPORARY /*3*/:
                    switch (clirInfo[NUM_PRESENTATION_ALLOWED]) {
                        case NUM_PRESENTATION_ALLOWED /*0*/:
                            sb.append(gsmMmiCode.mContext.getText(17039550));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case NUM_PRESENTATION_RESTRICTED /*1*/:
                            sb.append(gsmMmiCode.mContext.getText(17039550));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case CLIR_SUPPRESSION /*2*/:
                            sb.append(gsmMmiCode.mContext.getText(17039551));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(gsmMmiCode.mContext.getText(17039516));
                            gsmMmiCode.mState = State.FAILED;
                            break;
                    }
                case CLIR_PRESENTATION_ALLOWED_TEMPORARY /*4*/:
                    switch (clirInfo[NUM_PRESENTATION_ALLOWED]) {
                        case NUM_PRESENTATION_ALLOWED /*0*/:
                            sb.append(gsmMmiCode.mContext.getText(17039553));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case NUM_PRESENTATION_RESTRICTED /*1*/:
                            sb.append(gsmMmiCode.mContext.getText(17039552));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        case CLIR_SUPPRESSION /*2*/:
                            sb.append(gsmMmiCode.mContext.getText(17039553));
                            gsmMmiCode.mState = State.COMPLETE;
                            break;
                        default:
                            sb.append(gsmMmiCode.mContext.getText(17039516));
                            gsmMmiCode.mState = State.FAILED;
                            break;
                    }
                default:
                    sb.append(gsmMmiCode.mContext.getText(17039516));
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
                sb.append(gsmMmiCode.mContext.getText(17039520));
                if (gsmMmiCode.mIccRecords != null) {
                    gsmMmiCode.mIccRecords.setVoiceCallForwardingFlag(NUM_PRESENTATION_RESTRICTED, false, null);
                }
            } else {
                SpannableStringBuilder tb = new SpannableStringBuilder();
                for (int serviceClassMask = NUM_PRESENTATION_RESTRICTED; serviceClassMask <= HwSmsMessage.SMS_TOA_UNKNOWN; serviceClassMask <<= NUM_PRESENTATION_RESTRICTED) {
                    int s = infos.length;
                    for (int i = NUM_PRESENTATION_ALLOWED; i < s; i += NUM_PRESENTATION_RESTRICTED) {
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
                sb.append(gsmMmiCode.mContext.getText(17039516));
            } else if (ints[NUM_PRESENTATION_ALLOWED] == 0) {
                sb.append(gsmMmiCode.mContext.getText(17039520));
            } else if (gsmMmiCode.mSc.equals(SC_WAIT)) {
                sb.append(gsmMmiCode.createQueryCallWaitingResultMessageEx(ints[NUM_PRESENTATION_RESTRICTED]));
            } else if (ints[NUM_PRESENTATION_ALLOWED] == NUM_PRESENTATION_RESTRICTED) {
                sb.append(gsmMmiCode.mContext.getText(17039518));
            } else {
                sb.append(gsmMmiCode.mContext.getText(17039516));
            }
            gsmMmiCode.mState = State.COMPLETE;
        }
        gsmMmiCode.mMessage = sb;
        gsmMmiCode.mPhone.onMMIDone(gsmMmiCode);
    }

    public static CharSequence getCallForwardingString(Context context, String sc) {
        if (!SystemProperties.getBoolean("ro.config.hw_mmicode_custom", false)) {
            return context.getText(17039539);
        }
        if (sc.equals(SC_CFU)) {
            return context.getText(17041118);
        }
        if (sc.equals(SC_CFB)) {
            return context.getText(17041119);
        }
        if (sc.equals(SC_CFNRy)) {
            return context.getText(17041120);
        }
        if (sc.equals(SC_CFNR)) {
            return context.getText(17041121);
        }
        return context.getText(17039539);
    }
}

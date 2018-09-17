package com.android.internal.telephony.gsm;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.BidiFormatter;
import android.text.SpannableStringBuilder;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.MmiCode.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.SsData.RequestType;
import com.android.internal.telephony.gsm.SsData.ServiceType;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GsmMmiCode extends Handler implements MmiCode {
    private static final /* synthetic */ int[] -com-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-gsm-SsData$ServiceTypeSwitchesValues = null;
    static final String ACTION_ACTIVATE = "*";
    static final String ACTION_DEACTIVATE = "#";
    static final String ACTION_ERASURE = "##";
    static final String ACTION_INTERROGATE = "*#";
    static final String ACTION_REGISTER = "**";
    static final char END_OF_USSD_COMMAND = '#';
    static final int EVENT_GET_CLIR_COMPLETE = 2;
    static final int EVENT_QUERY_CF_COMPLETE = 3;
    static final int EVENT_QUERY_COMPLETE = 5;
    static final int EVENT_SET_CFF_COMPLETE = 6;
    static final int EVENT_SET_COMPLETE = 1;
    static final int EVENT_USSD_CANCEL_COMPLETE = 7;
    static final int EVENT_USSD_COMPLETE = 4;
    static final String LOG_TAG_STATIC = "GsmMmiCode";
    static final int MATCH_GROUP_ACTION = 2;
    static final int MATCH_GROUP_DIALING_NUMBER = 16;
    static final int MATCH_GROUP_POUND_STRING = 1;
    static final int MATCH_GROUP_PWD_CONFIRM = 15;
    static final int MATCH_GROUP_SERVICE_CODE = 3;
    static final int MATCH_GROUP_SIA = 6;
    static final int MATCH_GROUP_SIB = 9;
    static final int MATCH_GROUP_SIC = 12;
    static final int MAX_LENGTH_SHORT_CODE = 2;
    static final String SC_BAIC = "35";
    static final String SC_BAICr = "351";
    static final String SC_BAOC = "33";
    static final String SC_BAOIC = "331";
    static final String SC_BAOICxH = "332";
    static final String SC_BA_ALL = "330";
    static final String SC_BA_MO = "333";
    static final String SC_BA_MT = "353";
    static final String SC_CFB = "67";
    static final String SC_CFNR = "62";
    static final String SC_CFNRy = "61";
    static final String SC_CFU = "21";
    static final String SC_CF_All = "002";
    static final String SC_CF_All_Conditional = "004";
    static final String SC_CLIP = "30";
    static final String SC_CLIR = "31";
    static final String SC_PIN = "04";
    static final String SC_PIN2 = "042";
    static final String SC_PUK = "05";
    static final String SC_PUK2 = "052";
    static final String SC_PWD = "03";
    static final String SC_WAIT = "43";
    public static final boolean USSD_REMOVE_ERROR_MSG = false;
    static Pattern sPatternSuppService;
    private static String[] sTwoDigitNumberPattern;
    String LOG_TAG;
    String mAction;
    Context mContext;
    public String mDialingNumber;
    IccRecords mIccRecords;
    Phone mImsPhone;
    private boolean mIncomingUSSD;
    private boolean mIsCallFwdReg;
    private boolean mIsPendingUSSD;
    private boolean mIsSsInfo;
    private boolean mIsUssdRequest;
    CharSequence mMessage;
    GsmCdmaPhone mPhone;
    String mPoundString;
    String mPwd;
    String mSc;
    String mSia;
    String mSib;
    String mSic;
    State mState;
    UiccCardApplication mUiccApplication;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues() {
        if (-com-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues != null) {
            return -com-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues;
        }
        int[] iArr = new int[RequestType.values().length];
        try {
            iArr[RequestType.SS_ACTIVATION.ordinal()] = MATCH_GROUP_POUND_STRING;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RequestType.SS_DEACTIVATION.ordinal()] = MAX_LENGTH_SHORT_CODE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RequestType.SS_ERASURE.ordinal()] = MATCH_GROUP_SERVICE_CODE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[RequestType.SS_INTERROGATION.ordinal()] = EVENT_USSD_COMPLETE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[RequestType.SS_REGISTRATION.ordinal()] = EVENT_QUERY_COMPLETE;
        } catch (NoSuchFieldError e5) {
        }
        -com-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-gsm-SsData$ServiceTypeSwitchesValues() {
        if (-com-android-internal-telephony-gsm-SsData$ServiceTypeSwitchesValues != null) {
            return -com-android-internal-telephony-gsm-SsData$ServiceTypeSwitchesValues;
        }
        int[] iArr = new int[ServiceType.values().length];
        try {
            iArr[ServiceType.SS_ALL_BARRING.ordinal()] = MATCH_GROUP_POUND_STRING;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ServiceType.SS_BAIC.ordinal()] = MAX_LENGTH_SHORT_CODE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ServiceType.SS_BAIC_ROAMING.ordinal()] = MATCH_GROUP_SERVICE_CODE;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ServiceType.SS_BAOC.ordinal()] = EVENT_USSD_COMPLETE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ServiceType.SS_BAOIC.ordinal()] = EVENT_QUERY_COMPLETE;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ServiceType.SS_BAOIC_EXC_HOME.ordinal()] = MATCH_GROUP_SIA;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ServiceType.SS_CFU.ordinal()] = EVENT_USSD_CANCEL_COMPLETE;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ServiceType.SS_CF_ALL.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ServiceType.SS_CF_ALL_CONDITIONAL.ordinal()] = MATCH_GROUP_SIB;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ServiceType.SS_CF_BUSY.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ServiceType.SS_CF_NOT_REACHABLE.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ServiceType.SS_CF_NO_REPLY.ordinal()] = MATCH_GROUP_SIC;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ServiceType.SS_CLIP.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ServiceType.SS_CLIR.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ServiceType.SS_COLP.ordinal()] = 23;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ServiceType.SS_COLR.ordinal()] = 24;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ServiceType.SS_INCOMING_BARRING.ordinal()] = MATCH_GROUP_PWD_CONFIRM;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ServiceType.SS_OUTGOING_BARRING.ordinal()] = MATCH_GROUP_DIALING_NUMBER;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ServiceType.SS_WAIT.ordinal()] = 17;
        } catch (NoSuchFieldError e19) {
        }
        -com-android-internal-telephony-gsm-SsData$ServiceTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.gsm.GsmMmiCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.gsm.GsmMmiCode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.gsm.GsmMmiCode.<clinit>():void");
    }

    public static GsmMmiCode newFromDialString(String dialString, GsmCdmaPhone phone, UiccCardApplication app) {
        GsmMmiCode ret = null;
        if (HwTelephonyFactory.getHwPhoneManager().isStringHuaweiIgnoreCode(phone, dialString)) {
            String tag = LOG_TAG_STATIC;
            if (phone != null) {
                tag = tag + "[SUB" + phone.getPhoneId() + "]";
            }
            Rlog.d(tag, "newFromDialString, a huawei ignore code found, return null.");
            return null;
        }
        Matcher m = sPatternSuppService.matcher(dialString);
        if (m.matches()) {
            ret = new GsmMmiCode(phone, app);
            ret.mPoundString = makeEmptyNull(m.group(MATCH_GROUP_POUND_STRING));
            ret.mAction = makeEmptyNull(m.group(MAX_LENGTH_SHORT_CODE));
            ret.mSc = makeEmptyNull(m.group(MATCH_GROUP_SERVICE_CODE));
            ret.mSia = makeEmptyNull(m.group(MATCH_GROUP_SIA));
            ret.mSib = makeEmptyNull(m.group(MATCH_GROUP_SIB));
            ret.mSic = makeEmptyNull(m.group(MATCH_GROUP_SIC));
            ret.mPwd = makeEmptyNull(m.group(MATCH_GROUP_PWD_CONFIRM));
            ret.mDialingNumber = makeEmptyNull(m.group(MATCH_GROUP_DIALING_NUMBER));
            if (ret.mDialingNumber != null && ret.mDialingNumber.endsWith(ACTION_DEACTIVATE) && dialString.endsWith(ACTION_DEACTIVATE)) {
                ret = new GsmMmiCode(phone, app);
                ret.mPoundString = dialString;
            }
        } else if (dialString.endsWith(ACTION_DEACTIVATE)) {
            ret = new GsmMmiCode(phone, app);
            ret.mPoundString = dialString;
        } else if (isTwoDigitShortCode(phone.getContext(), dialString)) {
            ret = null;
        } else if (isShortCode(dialString, phone)) {
            ret = new GsmMmiCode(phone, app);
            ret.mDialingNumber = dialString;
        }
        return ret;
    }

    public static GsmMmiCode newNetworkInitiatedUssd(String ussdMessage, boolean isUssdRequest, GsmCdmaPhone phone, UiccCardApplication app) {
        GsmMmiCode ret = new GsmMmiCode(phone, app);
        ret.mMessage = ussdMessage;
        ret.mIsUssdRequest = isUssdRequest;
        if (isUssdRequest) {
            ret.mIsPendingUSSD = true;
            ret.mState = State.PENDING;
        } else {
            ret.mState = State.COMPLETE;
        }
        return ret;
    }

    public static GsmMmiCode newFromUssdUserInput(String ussdMessge, GsmCdmaPhone phone, UiccCardApplication app) {
        GsmMmiCode ret = new GsmMmiCode(phone, app);
        ret.mMessage = ussdMessge;
        ret.mState = State.PENDING;
        ret.mIsPendingUSSD = true;
        return ret;
    }

    public void processSsData(AsyncResult data) {
        Rlog.d(this.LOG_TAG, "In processSsData");
        this.mIsSsInfo = true;
        try {
            parseSsData(data.result);
        } catch (ClassCastException ex) {
            Rlog.e(this.LOG_TAG, "Class Cast Exception in parsing SS Data : " + ex);
        } catch (NullPointerException ex2) {
            Rlog.e(this.LOG_TAG, "Null Pointer Exception in parsing SS Data : " + ex2);
        }
    }

    void parseSsData(SsData ssData) {
        CommandException ex = CommandException.fromRilErrno(ssData.result);
        this.mSc = getScStringFromScType(ssData.serviceType);
        this.mAction = getActionStringFromReqType(ssData.requestType);
        Rlog.d(this.LOG_TAG, "parseSsData msc = " + this.mSc + ", action = " + this.mAction + ", ex = " + ex);
        switch (-getcom-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues()[ssData.requestType.ordinal()]) {
            case MATCH_GROUP_POUND_STRING /*1*/:
            case MAX_LENGTH_SHORT_CODE /*2*/:
            case MATCH_GROUP_SERVICE_CODE /*3*/:
            case EVENT_QUERY_COMPLETE /*5*/:
                if (ssData.result == 0 && ssData.serviceType.isTypeUnConditional()) {
                    boolean isServiceClassVoiceorNone;
                    if (ssData.requestType == RequestType.SS_ACTIVATION || ssData.requestType == RequestType.SS_REGISTRATION) {
                        isServiceClassVoiceorNone = isServiceClassVoiceorNone(ssData.serviceClass);
                    } else {
                        isServiceClassVoiceorNone = false;
                    }
                    Rlog.d(this.LOG_TAG, "setVoiceCallForwardingFlag cffEnabled: " + isServiceClassVoiceorNone);
                    if (this.mIccRecords != null) {
                        this.mPhone.setVoiceCallForwardingFlag(MATCH_GROUP_POUND_STRING, isServiceClassVoiceorNone, null);
                        Rlog.d(this.LOG_TAG, "setVoiceCallForwardingFlag done from SS Info.");
                    } else {
                        Rlog.e(this.LOG_TAG, "setVoiceCallForwardingFlag aborted. sim records is null.");
                    }
                }
                onSetComplete(null, new AsyncResult(null, ssData.cfInfo, ex));
            case EVENT_USSD_COMPLETE /*4*/:
                if (ssData.serviceType.isTypeClir()) {
                    Rlog.d(this.LOG_TAG, "CLIR INTERROGATION");
                    onGetClirComplete(new AsyncResult(null, ssData.ssInfo, ex));
                } else if (ssData.serviceType.isTypeCF()) {
                    Rlog.d(this.LOG_TAG, "CALL FORWARD INTERROGATION");
                    onQueryCfComplete(new AsyncResult(null, ssData.cfInfo, ex));
                } else {
                    onQueryComplete(new AsyncResult(null, ssData.ssInfo, ex));
                }
            default:
                Rlog.e(this.LOG_TAG, "Invaid requestType in SSData : " + ssData.requestType);
        }
    }

    private String getScStringFromScType(ServiceType sType) {
        switch (-getcom-android-internal-telephony-gsm-SsData$ServiceTypeSwitchesValues()[sType.ordinal()]) {
            case MATCH_GROUP_POUND_STRING /*1*/:
                return SC_BA_ALL;
            case MAX_LENGTH_SHORT_CODE /*2*/:
                return SC_BAIC;
            case MATCH_GROUP_SERVICE_CODE /*3*/:
                return SC_BAICr;
            case EVENT_USSD_COMPLETE /*4*/:
                return SC_BAOC;
            case EVENT_QUERY_COMPLETE /*5*/:
                return SC_BAOIC;
            case MATCH_GROUP_SIA /*6*/:
                return SC_BAOICxH;
            case EVENT_USSD_CANCEL_COMPLETE /*7*/:
                return SC_CFU;
            case CharacterSets.ISO_8859_5 /*8*/:
                return SC_CF_All;
            case MATCH_GROUP_SIB /*9*/:
                return SC_CF_All_Conditional;
            case CharacterSets.ISO_8859_7 /*10*/:
                return SC_CFB;
            case CharacterSets.ISO_8859_8 /*11*/:
                return SC_CFNR;
            case MATCH_GROUP_SIC /*12*/:
                return SC_CFNRy;
            case UserData.ASCII_CR_INDEX /*13*/:
                return SC_CLIP;
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                return SC_CLIR;
            case MATCH_GROUP_PWD_CONFIRM /*15*/:
                return SC_BA_MT;
            case MATCH_GROUP_DIALING_NUMBER /*16*/:
                return SC_BA_MO;
            case PduHeaders.MMS_VERSION_1_1 /*17*/:
                return SC_WAIT;
            default:
                return "";
        }
    }

    private String getActionStringFromReqType(RequestType rType) {
        switch (-getcom-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues()[rType.ordinal()]) {
            case MATCH_GROUP_POUND_STRING /*1*/:
                return ACTION_ACTIVATE;
            case MAX_LENGTH_SHORT_CODE /*2*/:
                return ACTION_DEACTIVATE;
            case MATCH_GROUP_SERVICE_CODE /*3*/:
                return ACTION_ERASURE;
            case EVENT_USSD_COMPLETE /*4*/:
                return ACTION_INTERROGATE;
            case EVENT_QUERY_COMPLETE /*5*/:
                return ACTION_REGISTER;
            default:
                return "";
        }
    }

    private boolean isServiceClassVoiceorNone(int serviceClass) {
        return (serviceClass & MATCH_GROUP_POUND_STRING) != 0 || serviceClass == 0;
    }

    private static String makeEmptyNull(String s) {
        if (s == null || s.length() != 0) {
            return s;
        }
        return null;
    }

    private static boolean isEmptyOrNull(CharSequence s) {
        return s == null || s.length() == 0;
    }

    private static int scToCallForwardReason(String sc) {
        if (sc == null) {
            throw new RuntimeException("invalid call forward sc");
        } else if (sc.equals(SC_CF_All)) {
            return EVENT_USSD_COMPLETE;
        } else {
            if (sc.equals(SC_CFU)) {
                return 0;
            }
            if (sc.equals(SC_CFB)) {
                return MATCH_GROUP_POUND_STRING;
            }
            if (sc.equals(SC_CFNR)) {
                return MATCH_GROUP_SERVICE_CODE;
            }
            if (sc.equals(SC_CFNRy)) {
                return MAX_LENGTH_SHORT_CODE;
            }
            if (sc.equals(SC_CF_All_Conditional)) {
                return EVENT_QUERY_COMPLETE;
            }
            throw new RuntimeException("invalid call forward sc");
        }
    }

    private static int siToServiceClass(String si) {
        if (si == null || si.length() == 0) {
            return 0;
        }
        switch (Integer.parseInt(si, 10)) {
            case CharacterSets.ISO_8859_7 /*10*/:
                return 13;
            case CharacterSets.ISO_8859_8 /*11*/:
                return MATCH_GROUP_POUND_STRING;
            case MATCH_GROUP_SIC /*12*/:
                return MATCH_GROUP_SIC;
            case UserData.ASCII_CR_INDEX /*13*/:
                return EVENT_USSD_COMPLETE;
            case MATCH_GROUP_DIALING_NUMBER /*16*/:
                return 8;
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                return EVENT_QUERY_COMPLETE;
            case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                return 48;
            case SmsHeader.ELT_ID_REUSED_EXTENDED_OBJECT /*21*/:
                return PduHeaders.PREVIOUSLY_SENT_BY;
            case CallFailCause.NUMBER_CHANGED /*22*/:
                return 80;
            case SmsHeader.ELT_ID_STANDARD_WVG_OBJECT /*24*/:
                return MATCH_GROUP_DIALING_NUMBER;
            case SmsHeader.ELT_ID_CHARACTER_SIZE_WVG_OBJECT /*25*/:
                return 32;
            case SmsHeader.ELT_ID_EXTENDED_OBJECT_DATA_REQUEST_CMD /*26*/:
                return 17;
            case com.android.internal.telephony.CallFailCause.INFORMATION_ELEMENT_NON_EXISTENT /*99*/:
                return 64;
            default:
                throw new RuntimeException("unsupported MMI service code " + si);
        }
    }

    private static int siToTime(String si) {
        if (si == null || si.length() == 0) {
            return 0;
        }
        return Integer.parseInt(si, 10);
    }

    static boolean isServiceCodeCallForwarding(String sc) {
        if (sc == null) {
            return false;
        }
        if (sc.equals(SC_CFU) || sc.equals(SC_CFB) || sc.equals(SC_CFNRy) || sc.equals(SC_CFNR) || sc.equals(SC_CF_All)) {
            return true;
        }
        return sc.equals(SC_CF_All_Conditional);
    }

    static boolean isServiceCodeCallBarring(String sc) {
        Resources resource = Resources.getSystem();
        if (sc != null) {
            String[] barringMMI = resource.getStringArray(17236029);
            if (barringMMI != null) {
                int length = barringMMI.length;
                for (int i = 0; i < length; i += MATCH_GROUP_POUND_STRING) {
                    if (sc.equals(barringMMI[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static String scToBarringFacility(String sc) {
        if (sc == null) {
            throw new RuntimeException("invalid call barring sc");
        } else if (sc.equals(SC_BAOC)) {
            return CommandsInterface.CB_FACILITY_BAOC;
        } else {
            if (sc.equals(SC_BAOIC)) {
                return CommandsInterface.CB_FACILITY_BAOIC;
            }
            if (sc.equals(SC_BAOICxH)) {
                return CommandsInterface.CB_FACILITY_BAOICxH;
            }
            if (sc.equals(SC_BAIC)) {
                return CommandsInterface.CB_FACILITY_BAIC;
            }
            if (sc.equals(SC_BAICr)) {
                return CommandsInterface.CB_FACILITY_BAICr;
            }
            if (sc.equals(SC_BA_ALL)) {
                return CommandsInterface.CB_FACILITY_BA_ALL;
            }
            if (sc.equals(SC_BA_MO)) {
                return CommandsInterface.CB_FACILITY_BA_MO;
            }
            if (sc.equals(SC_BA_MT)) {
                return CommandsInterface.CB_FACILITY_BA_MT;
            }
            throw new RuntimeException("invalid call barring sc");
        }
    }

    public GsmMmiCode(GsmCdmaPhone phone, UiccCardApplication app) {
        super(phone.getHandler().getLooper());
        this.LOG_TAG = LOG_TAG_STATIC;
        this.mState = State.PENDING;
        this.mIsSsInfo = false;
        this.mIncomingUSSD = false;
        this.mImsPhone = null;
        this.mPhone = phone;
        this.mContext = phone.getContext();
        this.mUiccApplication = app;
        if (app != null) {
            this.mIccRecords = app.getIccRecords();
        }
        this.LOG_TAG += "[SUB" + phone.getPhoneId() + "]";
    }

    public State getState() {
        return this.mState;
    }

    public CharSequence getMessage() {
        return this.mMessage;
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public void cancel() {
        if (this.mState != State.COMPLETE && this.mState != State.FAILED) {
            this.mState = State.CANCELLED;
            if (this.mIsPendingUSSD) {
                this.mPhone.mCi.cancelPendingUssd(obtainMessage(EVENT_USSD_CANCEL_COMPLETE, this));
            } else {
                this.mPhone.onMMIDone(this);
            }
        }
    }

    public boolean isCancelable() {
        return this.mIsPendingUSSD;
    }

    boolean isMMI() {
        return this.mPoundString != null;
    }

    boolean isShortCode() {
        if (this.mPoundString != null || this.mDialingNumber == null || this.mDialingNumber.length() > MAX_LENGTH_SHORT_CODE) {
            return false;
        }
        return true;
    }

    private static boolean isTwoDigitShortCode(Context context, String dialString) {
        Rlog.d(LOG_TAG_STATIC, "isTwoDigitShortCode");
        if (dialString == null || dialString.length() > MAX_LENGTH_SHORT_CODE) {
            return false;
        }
        if (sTwoDigitNumberPattern == null) {
            sTwoDigitNumberPattern = context.getResources().getStringArray(17236015);
        }
        String[] strArr = sTwoDigitNumberPattern;
        int length = strArr.length;
        for (int i = 0; i < length; i += MATCH_GROUP_POUND_STRING) {
            String dialnumber = strArr[i];
            Rlog.d(LOG_TAG_STATIC, "Two Digit Number Pattern " + dialnumber);
            if (dialString.equals(dialnumber)) {
                Rlog.d(LOG_TAG_STATIC, "Two Digit Number Pattern -true");
                return true;
            }
        }
        Rlog.d(LOG_TAG_STATIC, "Two Digit Number Pattern -false");
        return false;
    }

    private static boolean isShortCode(String dialString, GsmCdmaPhone phone) {
        if (dialString == null || dialString.length() == 0) {
            return false;
        }
        if (dialString != null && MAX_LENGTH_SHORT_CODE >= dialString.length()) {
            String hwMmiCodeStr = SystemProperties.get("ro.config.hw_mmi_code", "-1");
            if (hwMmiCodeStr != null) {
                String[] hwMmiCodes = hwMmiCodeStr.split(",");
                if (hwMmiCodes != null && Arrays.asList(hwMmiCodes).contains(dialString)) {
                    return false;
                }
            }
        }
        if (PhoneNumberUtils.isLocalEmergencyNumber(phone.getContext(), dialString)) {
            return false;
        }
        return isShortCodeUSSD(dialString, phone);
    }

    private static boolean isShortCodeUSSD(String dialString, GsmCdmaPhone phone) {
        return (HwTelephonyFactory.getHwPhoneManager().isShortCodeCustomization() || dialString == null || dialString.length() > MAX_LENGTH_SHORT_CODE || (!phone.isInCall() && dialString.length() == MAX_LENGTH_SHORT_CODE && dialString.charAt(0) == '1')) ? false : true;
    }

    public boolean isPinPukCommand() {
        if (this.mSc == null) {
            return false;
        }
        if (this.mSc.equals(SC_PIN) || this.mSc.equals(SC_PIN2) || this.mSc.equals(SC_PUK)) {
            return true;
        }
        return this.mSc.equals(SC_PUK2);
    }

    public boolean isTemporaryModeCLIR() {
        if (this.mSc == null || !this.mSc.equals(SC_CLIR) || this.mDialingNumber == null) {
            return false;
        }
        return !isActivate() ? isDeactivate() : true;
    }

    public int getCLIRMode() {
        if (this.mSc != null && this.mSc.equals(SC_CLIR)) {
            if (isActivate()) {
                return MAX_LENGTH_SHORT_CODE;
            }
            if (isDeactivate()) {
                return MATCH_GROUP_POUND_STRING;
            }
        }
        return 0;
    }

    boolean isActivate() {
        return this.mAction != null ? this.mAction.equals(ACTION_ACTIVATE) : false;
    }

    boolean isDeactivate() {
        return this.mAction != null ? this.mAction.equals(ACTION_DEACTIVATE) : false;
    }

    boolean isInterrogate() {
        return this.mAction != null ? this.mAction.equals(ACTION_INTERROGATE) : false;
    }

    boolean isRegister() {
        return this.mAction != null ? this.mAction.equals(ACTION_REGISTER) : false;
    }

    boolean isErasure() {
        return this.mAction != null ? this.mAction.equals(ACTION_ERASURE) : false;
    }

    public boolean isPendingUSSD() {
        return this.mIsPendingUSSD;
    }

    public boolean isUssdRequest() {
        return this.mIsUssdRequest;
    }

    public boolean isSsInfo() {
        return this.mIsSsInfo;
    }

    public void processCode() throws CallStateException {
        try {
            if (HwTelephonyFactory.getHwPhoneManager().isStringHuaweiCustCode(this.mPoundString)) {
                Rlog.d(this.LOG_TAG, "Huawei custimized MMI codes, send out directly. ");
                sendUssd(this.mPoundString);
                return;
            }
            if (HwTelephonyFactory.getHwPhoneManager().processImsPhoneMmiCode(this, this.mImsPhone)) {
                Rlog.d(this.LOG_TAG, "Process IMS Phone MMI codes.");
                return;
            }
            if (isShortCode()) {
                Rlog.d(this.LOG_TAG, "isShortCode");
                sendUssd(this.mDialingNumber);
            } else if (HwTelephonyFactory.getHwPhoneManager().changeMMItoUSSD(this.mPhone, this.mPoundString)) {
                Rlog.d(this.LOG_TAG, "changeMMItoUSSD");
                sendUssd(this.mPoundString);
            } else if (this.mDialingNumber != null) {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            } else if (this.mSc != null && this.mSc.equals(SC_CLIP)) {
                Rlog.d(this.LOG_TAG, "is CLIP");
                if (isInterrogate()) {
                    this.mPhone.mCi.queryCLIP(obtainMessage(EVENT_QUERY_COMPLETE, this));
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (this.mSc != null && this.mSc.equals(SC_CLIR)) {
                Rlog.d(this.LOG_TAG, "is CLIR");
                if (isActivate()) {
                    this.mPhone.mCi.setCLIR(MATCH_GROUP_POUND_STRING, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                } else if (isDeactivate()) {
                    this.mPhone.mCi.setCLIR(MAX_LENGTH_SHORT_CODE, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                } else if (isInterrogate()) {
                    this.mPhone.mCi.getCLIR(obtainMessage(MAX_LENGTH_SHORT_CODE, this));
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (isServiceCodeCallForwarding(this.mSc)) {
                Rlog.d(this.LOG_TAG, "is CF");
                String dialingNumber = this.mSia;
                serviceClass = siToServiceClass(this.mSib);
                int reason = scToCallForwardReason(this.mSc);
                int time = siToTime(this.mSic);
                if (isInterrogate()) {
                    this.mPhone.mCi.queryCallForwardStatus(reason, serviceClass, dialingNumber, obtainMessage(MATCH_GROUP_SERVICE_CODE, this));
                } else {
                    int cfAction;
                    if (isActivate()) {
                        if (isEmptyOrNull(dialingNumber)) {
                            cfAction = MATCH_GROUP_POUND_STRING;
                            this.mIsCallFwdReg = false;
                        } else {
                            cfAction = MATCH_GROUP_SERVICE_CODE;
                            this.mIsCallFwdReg = true;
                        }
                    } else if (isDeactivate()) {
                        cfAction = 0;
                    } else if (isRegister()) {
                        cfAction = MATCH_GROUP_SERVICE_CODE;
                    } else if (isErasure()) {
                        cfAction = EVENT_USSD_COMPLETE;
                    } else {
                        throw new RuntimeException("invalid action");
                    }
                    int isSettingUnconditionalVoice = ((reason == 0 || reason == EVENT_USSD_COMPLETE) && ((serviceClass & MATCH_GROUP_POUND_STRING) != 0 || serviceClass == 0)) ? MATCH_GROUP_POUND_STRING : 0;
                    int isEnableDesired = (cfAction == MATCH_GROUP_POUND_STRING || cfAction == MATCH_GROUP_SERVICE_CODE) ? MATCH_GROUP_POUND_STRING : 0;
                    Rlog.d(this.LOG_TAG, "is CF setCallForward");
                    this.mPhone.mCi.setCallForward(cfAction, reason, serviceClass, dialingNumber, time, obtainMessage(MATCH_GROUP_SIA, isSettingUnconditionalVoice, isEnableDesired, this));
                }
            } else if (isServiceCodeCallBarring(this.mSc)) {
                String password = this.mSia;
                serviceClass = siToServiceClass(this.mSib);
                facility = scToBarringFacility(this.mSc);
                if (isInterrogate()) {
                    this.mPhone.mCi.queryFacilityLock(facility, password, serviceClass, obtainMessage(EVENT_QUERY_COMPLETE, this));
                } else if (isActivate() || isDeactivate()) {
                    this.mPhone.mCi.setFacilityLock(facility, isActivate(), password, serviceClass, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (this.mSc != null && this.mSc.equals(SC_PWD)) {
                String oldPwd = this.mSib;
                String newPwd = this.mSic;
                if (isActivate() || isRegister()) {
                    this.mAction = ACTION_REGISTER;
                    if (this.mSia == null) {
                        facility = CommandsInterface.CB_FACILITY_BA_ALL;
                    } else {
                        facility = scToBarringFacility(this.mSia);
                    }
                    if (newPwd.equals(this.mPwd)) {
                        this.mPhone.mCi.changeBarringPassword(facility, oldPwd, newPwd, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                    } else {
                        handlePasswordError(17039523);
                    }
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (this.mSc != null && this.mSc.equals(SC_WAIT)) {
                serviceClass = siToServiceClass(this.mSia);
                if (isActivate() || isDeactivate()) {
                    this.mPhone.mCi.setCallWaiting(isActivate(), serviceClass, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                } else if (isInterrogate()) {
                    this.mPhone.mCi.queryCallWaiting(serviceClass, obtainMessage(EVENT_QUERY_COMPLETE, this));
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (isPinPukCommand()) {
                String oldPinOrPuk = this.mSia;
                String newPinOrPuk = this.mSib;
                int pinLen = newPinOrPuk.length();
                if (isRegister()) {
                    if (!newPinOrPuk.equals(this.mSic)) {
                        handlePasswordError(HwTelephonyFactory.getHwPhoneManager().handlePasswordError(this.mSc));
                    } else if (pinLen < EVENT_USSD_COMPLETE || pinLen > 8) {
                        handlePasswordError(17039528);
                    } else if (this.mSc.equals(SC_PIN) && this.mUiccApplication != null && this.mUiccApplication.getState() == AppState.APPSTATE_PUK) {
                        handlePasswordError(17039530);
                    } else if (this.mUiccApplication != null) {
                        Rlog.d(this.LOG_TAG, "process mmi service code using UiccApp sc=" + this.mSc);
                        if (this.mSc.equals(SC_PIN)) {
                            this.mUiccApplication.changeIccLockPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                        } else if (this.mSc.equals(SC_PIN2)) {
                            this.mUiccApplication.changeIccFdnPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                        } else if (this.mSc.equals(SC_PUK)) {
                            this.mUiccApplication.supplyPuk(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                        } else if (this.mSc.equals(SC_PUK2)) {
                            this.mUiccApplication.supplyPuk2(oldPinOrPuk, newPinOrPuk, obtainMessage(MATCH_GROUP_POUND_STRING, this));
                        } else {
                            throw new RuntimeException("uicc unsupported service code=" + this.mSc);
                        }
                    } else {
                        throw new RuntimeException("No application mUiccApplicaiton is null");
                    }
                }
                throw new RuntimeException("Ivalid register/action=" + this.mAction);
            } else if (this.mPoundString != null) {
                sendUssd(this.mPoundString);
            } else {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        } catch (RuntimeException e) {
            this.mState = State.FAILED;
            this.mMessage = this.mContext.getText(17039516);
            this.mPhone.onMMIDone(this);
        }
    }

    private void handlePasswordError(int res) {
        this.mState = State.FAILED;
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        sb.append(this.mContext.getText(res));
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    public void onUssdFinished(String ussdMessage, boolean isUssdRequest) {
        if (this.mState == State.PENDING) {
            if (ussdMessage == null) {
                this.mMessage = this.mContext.getText(HwTelephonyFactory.getHwPhoneManager().showMmiError(17039524));
            } else {
                this.mMessage = ussdMessage;
            }
            this.mIsUssdRequest = isUssdRequest;
            if (!isUssdRequest) {
                this.mState = State.COMPLETE;
                if (USSD_REMOVE_ERROR_MSG && this.mIncomingUSSD && ussdMessage == null) {
                    this.mMessage = "";
                }
            }
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdFinishedError() {
        if (this.mState == State.PENDING) {
            this.mState = State.FAILED;
            if (USSD_REMOVE_ERROR_MSG && this.mIncomingUSSD) {
                this.mMessage = "";
            } else {
                this.mMessage = this.mContext.getText(17039516);
            }
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdRelease() {
        if (this.mState == State.PENDING) {
            this.mState = State.COMPLETE;
            this.mMessage = null;
            this.mPhone.onMMIDone(this);
        }
    }

    public void sendUssd(String ussdMessage) {
        this.mIsPendingUSSD = true;
        this.mPhone.mCi.sendUSSD(HwTelephonyFactory.getHwPhoneManager().convertUssdMessage(ussdMessage), obtainMessage(EVENT_USSD_COMPLETE, this));
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case MATCH_GROUP_POUND_STRING /*1*/:
                onSetComplete(msg, msg.obj);
            case MAX_LENGTH_SHORT_CODE /*2*/:
                onGetClirComplete((AsyncResult) msg.obj);
            case MATCH_GROUP_SERVICE_CODE /*3*/:
                onQueryCfComplete((AsyncResult) msg.obj);
            case EVENT_USSD_COMPLETE /*4*/:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    this.mState = State.FAILED;
                    this.mMessage = getErrorMessage(ar);
                    this.mPhone.onMMIDone(this);
                }
            case EVENT_QUERY_COMPLETE /*5*/:
                onQueryComplete((AsyncResult) msg.obj);
            case MATCH_GROUP_SIA /*6*/:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && msg.arg1 == MATCH_GROUP_POUND_STRING) {
                    boolean cffEnabled = msg.arg2 == MATCH_GROUP_POUND_STRING;
                    if (this.mIccRecords != null) {
                        this.mPhone.setVoiceCallForwardingFlag(MATCH_GROUP_POUND_STRING, cffEnabled, this.mDialingNumber);
                    }
                }
                onSetComplete(msg, ar);
            case EVENT_USSD_CANCEL_COMPLETE /*7*/:
                this.mPhone.onMMIDone(this);
            default:
                HwTelephonyFactory.getHwPhoneManager().handleMessageGsmMmiCode(this, msg);
        }
    }

    private CharSequence getErrorMessage(AsyncResult ar) {
        if (ar.exception instanceof CommandException) {
            Error err = ((CommandException) ar.exception).getCommandError();
            if (err == Error.FDN_CHECK_FAILURE) {
                Rlog.i(this.LOG_TAG, "FDN_CHECK_FAILURE");
                return this.mContext.getText(17039517);
            } else if (err == Error.USSD_MODIFIED_TO_DIAL) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_DIAL");
                return this.mContext.getText(17040818);
            } else if (err == Error.USSD_MODIFIED_TO_SS) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_SS");
                return this.mContext.getText(17040819);
            } else if (err == Error.USSD_MODIFIED_TO_USSD) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_USSD");
                return this.mContext.getText(17040820);
            } else if (err == Error.SS_MODIFIED_TO_DIAL) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_DIAL");
                return this.mContext.getText(17040821);
            } else if (err == Error.SS_MODIFIED_TO_USSD) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_USSD");
                return this.mContext.getText(17040822);
            } else if (err == Error.SS_MODIFIED_TO_SS) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_SS");
                return this.mContext.getText(17040823);
            }
        }
        return this.mContext.getText(17039516);
    }

    private CharSequence getScString() {
        if (this.mSc != null) {
            if (isServiceCodeCallBarring(this.mSc)) {
                return this.mContext.getText(17039541);
            }
            if (isServiceCodeCallForwarding(this.mSc)) {
                return HwTelephonyFactory.getHwPhoneManager().getCallForwardingString(this.mContext, this.mSc);
            }
            if (this.mSc.equals(SC_CLIP)) {
                return this.mContext.getText(17039535);
            }
            if (this.mSc.equals(SC_CLIR)) {
                return this.mContext.getText(17039536);
            }
            if (this.mSc.equals(SC_PWD)) {
                return this.mContext.getText(17039542);
            }
            if (this.mSc.equals(SC_WAIT)) {
                return this.mContext.getText(17039540);
            }
            if (isPinPukCommand()) {
                return HwTelephonyFactory.getHwPhoneManager().processgoodPinString(this.mContext, this.mSc);
            }
        }
        return "";
    }

    private void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            if (ar.exception instanceof CommandException) {
                Error err = ((CommandException) ar.exception).getCommandError();
                if (err == Error.PASSWORD_INCORRECT) {
                    if (isPinPukCommand()) {
                        if (!this.mSc.equals(SC_PUK) && !this.mSc.equals(SC_PUK2)) {
                            sb.append(HwTelephonyFactory.getHwPhoneManager().processBadPinString(this.mContext, this.mSc));
                        } else if (this.mSc.equals(SC_PUK)) {
                            sb.append(this.mContext.getText(17039526));
                        } else {
                            sb.append(this.mContext.getText(33685774));
                        }
                        int attemptsRemaining = msg.arg1;
                        if (attemptsRemaining <= 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: PUK locked, cancel as lock screen will handle this");
                            this.mState = State.CANCELLED;
                        } else if (attemptsRemaining > 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: attemptsRemaining=" + attemptsRemaining);
                            Resources resources = this.mContext.getResources();
                            Object[] objArr = new Object[MATCH_GROUP_POUND_STRING];
                            objArr[0] = Integer.valueOf(attemptsRemaining);
                            sb.append(resources.getQuantityString(18087936, attemptsRemaining, objArr));
                        }
                    } else {
                        sb.append(this.mContext.getText(17039523));
                    }
                } else if (err == Error.SIM_PUK2) {
                    sb.append(HwTelephonyFactory.getHwPhoneManager().processBadPinString(this.mContext, this.mSc));
                    sb.append("\n");
                    sb.append(this.mContext.getText(17039531));
                } else if (err == Error.REQUEST_NOT_SUPPORTED) {
                    if (this.mSc.equals(SC_PIN)) {
                        sb.append(this.mContext.getText(17039532));
                    }
                } else if (err == Error.FDN_CHECK_FAILURE) {
                    Rlog.i(this.LOG_TAG, "FDN_CHECK_FAILURE");
                    sb.append(this.mContext.getText(17039517));
                } else {
                    sb.append(getErrorMessage(ar));
                }
            } else {
                sb.append(this.mContext.getText(17039516));
            }
        } else if (isActivate()) {
            this.mState = State.COMPLETE;
            if (this.mIsCallFwdReg) {
                sb.append(this.mContext.getText(17039521));
            } else {
                sb.append(this.mContext.getText(17039518));
            }
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(MATCH_GROUP_POUND_STRING);
            }
        } else if (isDeactivate()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17039520));
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(MAX_LENGTH_SHORT_CODE);
            }
        } else if (isRegister()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17039521));
        } else if (isErasure()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17039522));
        } else {
            this.mState = State.FAILED;
            sb.append(this.mContext.getText(17039516));
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private void onGetClirComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception == null) {
            int[] clirArgs = ar.result;
            switch (clirArgs[MATCH_GROUP_POUND_STRING]) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                    sb.append(this.mContext.getText(17039554));
                    this.mState = State.COMPLETE;
                    break;
                case MATCH_GROUP_POUND_STRING /*1*/:
                    sb.append(this.mContext.getText(17039555));
                    this.mState = State.COMPLETE;
                    break;
                case MAX_LENGTH_SHORT_CODE /*2*/:
                    sb.append(this.mContext.getText(17039516));
                    this.mState = State.FAILED;
                    break;
                case MATCH_GROUP_SERVICE_CODE /*3*/:
                    switch (clirArgs[0]) {
                        case MATCH_GROUP_POUND_STRING /*1*/:
                            sb.append(this.mContext.getText(17039550));
                            break;
                        case MAX_LENGTH_SHORT_CODE /*2*/:
                            sb.append(this.mContext.getText(17039551));
                            break;
                        default:
                            sb.append(this.mContext.getText(17039550));
                            break;
                    }
                    this.mState = State.COMPLETE;
                    break;
                case EVENT_USSD_COMPLETE /*4*/:
                    switch (clirArgs[0]) {
                        case MATCH_GROUP_POUND_STRING /*1*/:
                            sb.append(this.mContext.getText(17039552));
                            break;
                        case MAX_LENGTH_SHORT_CODE /*2*/:
                            sb.append(this.mContext.getText(17039553));
                            break;
                        default:
                            sb.append(this.mContext.getText(17039553));
                            break;
                    }
                    this.mState = State.COMPLETE;
                    break;
                default:
                    break;
            }
        }
        this.mState = State.FAILED;
        sb.append(getErrorMessage(ar));
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private CharSequence serviceClassToCFString(int serviceClass) {
        switch (serviceClass) {
            case MATCH_GROUP_POUND_STRING /*1*/:
                return this.mContext.getText(17039568);
            case MAX_LENGTH_SHORT_CODE /*2*/:
                return this.mContext.getText(17039569);
            case EVENT_USSD_COMPLETE /*4*/:
                return this.mContext.getText(17039570);
            case CharacterSets.ISO_8859_5 /*8*/:
                return this.mContext.getText(17039571);
            case MATCH_GROUP_DIALING_NUMBER /*16*/:
                return this.mContext.getText(17039573);
            case UserData.PRINTABLE_ASCII_MIN_INDEX /*32*/:
                return this.mContext.getText(17039572);
            case CommandsInterface.SERVICE_CLASS_PACKET /*64*/:
                return this.mContext.getText(17039574);
            case PduPart.P_Q /*128*/:
                return this.mContext.getText(17039575);
            default:
                return null;
        }
    }

    private CharSequence makeCFQueryResultMessage(CallForwardInfo info, int serviceClassMask) {
        CharSequence template;
        String[] sources = new String[MATCH_GROUP_SERVICE_CODE];
        sources[0] = "{0}";
        sources[MATCH_GROUP_POUND_STRING] = "{1}";
        sources[MAX_LENGTH_SHORT_CODE] = "{2}";
        CharSequence[] destinations = new CharSequence[MATCH_GROUP_SERVICE_CODE];
        boolean needTimeTemplate = info.reason == MAX_LENGTH_SHORT_CODE;
        if (info.status == MATCH_GROUP_POUND_STRING) {
            if (needTimeTemplate) {
                template = this.mContext.getText(17039599);
            } else {
                template = this.mContext.getText(17039598);
            }
        } else if (info.status == 0 && isEmptyOrNull(info.number)) {
            template = this.mContext.getText(17039597);
        } else if (needTimeTemplate) {
            template = this.mContext.getText(17039601);
        } else {
            template = this.mContext.getText(17039600);
        }
        destinations[0] = serviceClassToCFString(info.serviceClass & serviceClassMask);
        destinations[MATCH_GROUP_POUND_STRING] = formatLtr(PhoneNumberUtils.stringFromStringAndTOA(info.number, info.toa));
        destinations[MAX_LENGTH_SHORT_CODE] = Integer.toString(info.timeSeconds);
        if (info.reason == 0 && (info.serviceClass & serviceClassMask) == MATCH_GROUP_POUND_STRING) {
            boolean cffEnabled = info.status == MATCH_GROUP_POUND_STRING;
            if (this.mIccRecords != null) {
                this.mPhone.setVoiceCallForwardingFlag(MATCH_GROUP_POUND_STRING, cffEnabled, info.number);
            }
        }
        return TextUtils.replace(template, sources, destinations);
    }

    private String formatLtr(String str) {
        return str == null ? str : BidiFormatter.getInstance().unicodeWrap(str, TextDirectionHeuristics.LTR, true);
    }

    private void onQueryCfComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            sb.append(getErrorMessage(ar));
        } else {
            CallForwardInfo[] infos = ar.result;
            if (infos.length == 0) {
                sb.append(this.mContext.getText(17039520));
                if (this.mIccRecords != null) {
                    this.mPhone.setVoiceCallForwardingFlag(MATCH_GROUP_POUND_STRING, false, null);
                }
            } else {
                SpannableStringBuilder tb = new SpannableStringBuilder();
                for (int serviceClassMask = MATCH_GROUP_POUND_STRING; serviceClassMask <= PduPart.P_Q; serviceClassMask <<= MATCH_GROUP_POUND_STRING) {
                    int s = infos.length;
                    for (int i = 0; i < s; i += MATCH_GROUP_POUND_STRING) {
                        if ((infos[i].serviceClass & serviceClassMask) != 0) {
                            tb.append(makeCFQueryResultMessage(infos[i], serviceClassMask));
                            tb.append("\n");
                        }
                    }
                }
                sb.append(tb);
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private void onQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = State.FAILED;
            sb.append(getErrorMessage(ar));
        } else {
            int[] ints = ar.result;
            if (ints.length == 0) {
                sb.append(this.mContext.getText(17039516));
            } else if (ints[0] == 0) {
                sb.append(this.mContext.getText(17039520));
            } else if (this.mSc.equals(SC_WAIT)) {
                sb.append(createQueryCallWaitingResultMessage(ints[MATCH_GROUP_POUND_STRING]));
            } else if (isServiceCodeCallBarring(this.mSc)) {
                sb.append(createQueryCallBarringResultMessage(ints[0]));
            } else if (ints[0] == MATCH_GROUP_POUND_STRING) {
                sb.append(this.mContext.getText(17039518));
            } else {
                sb.append(this.mContext.getText(17039516));
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    private CharSequence createQueryCallWaitingResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17039519));
        for (int classMask = MATCH_GROUP_POUND_STRING; classMask <= PduPart.P_Q; classMask <<= MATCH_GROUP_POUND_STRING) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    private CharSequence createQueryCallBarringResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17039519));
        for (int classMask = MATCH_GROUP_POUND_STRING; classMask <= PduPart.P_Q; classMask <<= MATCH_GROUP_POUND_STRING) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("GsmMmiCode {");
        sb.append("State=").append(getState());
        if (this.mAction != null) {
            sb.append(" action=").append(this.mAction);
        }
        if (this.mSc != null) {
            sb.append(" sc=").append(this.mSc);
        }
        if (this.mSia != null) {
            sb.append(" sia=").append(this.mSia);
        }
        if (this.mSib != null) {
            sb.append(" sib=").append(this.mSib);
        }
        if (this.mSic != null) {
            sb.append(" sic=").append(this.mSic);
        }
        if (this.mPoundString != null) {
            sb.append(" poundString=").append(this.mPoundString);
        }
        if (this.mDialingNumber != null) {
            sb.append(" dialingNumber=").append(this.mDialingNumber);
        }
        if (this.mPwd != null) {
            sb.append(" pwd=").append(this.mPwd);
        }
        sb.append("}");
        return sb.toString();
    }

    public void setIncomingUSSD(boolean incomingUSSD) {
        this.mIncomingUSSD = incomingUSSD;
    }

    public void setImsPhone(Phone imsPhone) {
        this.mImsPhone = imsPhone;
    }

    public void setHwCallFwgReg(boolean isCallFwdReg) {
        this.mIsCallFwdReg = isCallFwdReg;
    }

    public boolean getHwCallFwdReg() {
        return this.mIsCallFwdReg;
    }

    public CharSequence createQueryCallWaitingResultMessageEx(int serviceClass) {
        return createQueryCallWaitingResultMessage(serviceClass);
    }

    public CharSequence makeCFQueryResultMessageEx(CallForwardInfo info, int serviceClassMask) {
        return makeCFQueryResultMessage(info, serviceClassMask);
    }

    public static int scToCallForwardReasonEx(String sc) {
        return scToCallForwardReason(sc);
    }

    public static int siToTimeEx(String si) {
        return siToTime(si);
    }
}

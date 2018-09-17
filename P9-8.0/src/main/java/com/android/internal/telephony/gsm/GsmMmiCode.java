package com.android.internal.telephony.gsm;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
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
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.MmiCode.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gsm.SsData.RequestType;
import com.android.internal.telephony.gsm.SsData.ServiceType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
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
    static final String SC_107 = "107";
    static final String SC_108 = "108";
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
    public static final boolean USSD_REMOVE_ERROR_MSG = SystemProperties.getBoolean("ro.config.hw_remove_mmi", false);
    private static final boolean mToastSwitch = SystemProperties.getBoolean("ro.config.hw_ss_toast", false);
    static Pattern sPatternSuppService;
    private static String[] sTwoDigitNumberPattern;
    String LOG_TAG = LOG_TAG_STATIC;
    String mAction;
    private ResultReceiver mCallbackReceiver;
    Context mContext;
    public String mDialingNumber;
    IccRecords mIccRecords;
    Phone mImsPhone = null;
    private boolean mIncomingUSSD = false;
    private boolean mIsCallFwdReg;
    private boolean mIsPendingUSSD;
    private boolean mIsSsInfo = false;
    private boolean mIsUssdRequest;
    CharSequence mMessage;
    GsmCdmaPhone mPhone;
    String mPoundString;
    String mPwd;
    String mSc;
    String mSia;
    String mSib;
    String mSic;
    State mState = State.PENDING;
    UiccCardApplication mUiccApplication;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues() {
        if (-com-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues != null) {
            return -com-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues;
        }
        int[] iArr = new int[RequestType.values().length];
        try {
            iArr[RequestType.SS_ACTIVATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RequestType.SS_DEACTIVATION.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RequestType.SS_ERASURE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[RequestType.SS_INTERROGATION.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[RequestType.SS_REGISTRATION.ordinal()] = 5;
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
            iArr[ServiceType.SS_ALL_BARRING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ServiceType.SS_BAIC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ServiceType.SS_BAIC_ROAMING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ServiceType.SS_BAOC.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ServiceType.SS_BAOIC.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ServiceType.SS_BAOIC_EXC_HOME.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ServiceType.SS_CFU.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ServiceType.SS_CF_ALL.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ServiceType.SS_CF_ALL_CONDITIONAL.ordinal()] = 9;
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
            iArr[ServiceType.SS_CF_NO_REPLY.ordinal()] = 12;
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
            iArr[ServiceType.SS_INCOMING_BARRING.ordinal()] = 15;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ServiceType.SS_OUTGOING_BARRING.ordinal()] = 16;
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
        sPatternSuppService = Pattern.compile("((\\*|#|\\*#|\\*\\*|##)(\\d{2,3})(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*)(\\*([^*#]*))?)?)?)?#)(.*)");
        sPatternSuppService = HwPhoneManager.sPatternSuppService;
    }

    public static GsmMmiCode newFromDialString(String dialString, GsmCdmaPhone phone, UiccCardApplication app) {
        return newFromDialString(dialString, phone, app, null);
    }

    public static GsmMmiCode newFromDialString(String dialString, GsmCdmaPhone phone, UiccCardApplication app, ResultReceiver wrappedCallback) {
        GsmMmiCode ret = null;
        if (dialString == null) {
            Rlog.d(LOG_TAG_STATIC, "newFromDialString: dialString cannot be null");
            return null;
        } else if (HwTelephonyFactory.getHwPhoneManager().isStringHuaweiIgnoreCode(phone, dialString)) {
            String tag = LOG_TAG_STATIC;
            if (phone != null) {
                tag = tag + "[SUB" + phone.getPhoneId() + "]";
            }
            Rlog.d(tag, "newFromDialString, a huawei ignore code found, return null.");
            return null;
        } else {
            Matcher m = sPatternSuppService.matcher(dialString);
            if (m.matches()) {
                ret = new GsmMmiCode(phone, app);
                ret.mPoundString = makeEmptyNull(m.group(1));
                ret.mAction = makeEmptyNull(m.group(2));
                ret.mSc = makeEmptyNull(m.group(3));
                ret.mSia = makeEmptyNull(m.group(6));
                ret.mSib = makeEmptyNull(m.group(9));
                ret.mSic = makeEmptyNull(m.group(12));
                ret.mPwd = makeEmptyNull(m.group(15));
                ret.mDialingNumber = makeEmptyNull(m.group(16));
                ret.mCallbackReceiver = wrappedCallback;
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
    }

    public String getmSC() {
        return this.mSc;
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
            case 1:
            case 2:
            case 3:
            case 5:
                if (ssData.result == 0 && ssData.serviceType.isTypeUnConditional()) {
                    boolean cffEnabled;
                    if (ssData.requestType == RequestType.SS_ACTIVATION || ssData.requestType == RequestType.SS_REGISTRATION) {
                        cffEnabled = isServiceClassVoiceorNone(ssData.serviceClass);
                    } else {
                        cffEnabled = false;
                    }
                    Rlog.d(this.LOG_TAG, "setVoiceCallForwardingFlag cffEnabled: " + cffEnabled);
                    if (this.mIccRecords != null) {
                        this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, null);
                        Rlog.d(this.LOG_TAG, "setVoiceCallForwardingFlag done from SS Info.");
                    } else {
                        Rlog.e(this.LOG_TAG, "setVoiceCallForwardingFlag aborted. sim records is null.");
                    }
                }
                onSetComplete(null, new AsyncResult(null, ssData.cfInfo, ex));
                return;
            case 4:
                if (ssData.serviceType.isTypeClir()) {
                    Rlog.d(this.LOG_TAG, "CLIR INTERROGATION");
                    onGetClirComplete(new AsyncResult(null, ssData.ssInfo, ex));
                    return;
                } else if (ssData.serviceType.isTypeCF()) {
                    Rlog.d(this.LOG_TAG, "CALL FORWARD INTERROGATION");
                    onQueryCfComplete(new AsyncResult(null, ssData.cfInfo, ex));
                    return;
                } else {
                    onQueryComplete(new AsyncResult(null, ssData.ssInfo, ex));
                    return;
                }
            default:
                Rlog.e(this.LOG_TAG, "Invaid requestType in SSData : " + ssData.requestType);
                return;
        }
    }

    private String getScStringFromScType(ServiceType sType) {
        switch (-getcom-android-internal-telephony-gsm-SsData$ServiceTypeSwitchesValues()[sType.ordinal()]) {
            case 1:
                return SC_BA_ALL;
            case 2:
                return SC_BAIC;
            case 3:
                return SC_BAICr;
            case 4:
                return SC_BAOC;
            case 5:
                return SC_BAOIC;
            case 6:
                return SC_BAOICxH;
            case 7:
                return SC_CFU;
            case 8:
                return SC_CF_All;
            case 9:
                return SC_CF_All_Conditional;
            case 10:
                return SC_CFB;
            case 11:
                return SC_CFNR;
            case 12:
                return SC_CFNRy;
            case 13:
                return SC_CLIP;
            case 14:
                return SC_CLIR;
            case 15:
                return SC_BA_MT;
            case 16:
                return SC_BA_MO;
            case 17:
                return SC_WAIT;
            default:
                return "";
        }
    }

    private String getActionStringFromReqType(RequestType rType) {
        switch (-getcom-android-internal-telephony-gsm-SsData$RequestTypeSwitchesValues()[rType.ordinal()]) {
            case 1:
                return "*";
            case 2:
                return ACTION_DEACTIVATE;
            case 3:
                return ACTION_ERASURE;
            case 4:
                return ACTION_INTERROGATE;
            case 5:
                return ACTION_REGISTER;
            default:
                return "";
        }
    }

    private boolean isServiceClassVoiceorNone(int serviceClass) {
        return (serviceClass & 1) != 0 || serviceClass == 0;
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
            return 4;
        } else {
            if (sc.equals(SC_CFU)) {
                return 0;
            }
            if (sc.equals(SC_CFB)) {
                return 1;
            }
            if (sc.equals(SC_CFNR)) {
                return 3;
            }
            if (sc.equals(SC_CFNRy)) {
                return 2;
            }
            if (sc.equals(SC_CF_All_Conditional)) {
                return 5;
            }
            throw new RuntimeException("invalid call forward sc");
        }
    }

    private static int siToServiceClass(String si) {
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
            case 24:
                return 16;
            case 25:
                return 32;
            case 26:
                return 17;
            case 99:
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

    public boolean isServiceCodeCallForwarding() {
        if (TextUtils.isEmpty(this.mSc)) {
            return false;
        }
        return isServiceCodeCallForwarding(this.mSc);
    }

    public static boolean isServiceCodeCallForwarding(String sc) {
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
            String[] barringMMI = resource.getStringArray(17235991);
            if (barringMMI != null) {
                for (String match : barringMMI) {
                    if (sc.equals(match)) {
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
                this.mPhone.mCi.cancelPendingUssd(obtainMessage(7, this));
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
        if (this.mPoundString != null || this.mDialingNumber == null || this.mDialingNumber.length() > 2) {
            return false;
        }
        return true;
    }

    public String getDialString() {
        return this.mPoundString;
    }

    private static boolean isTwoDigitShortCode(Context context, String dialString) {
        Rlog.d(LOG_TAG_STATIC, "isTwoDigitShortCode");
        if (dialString == null || dialString.length() > 2) {
            return false;
        }
        if (sTwoDigitNumberPattern == null) {
            sTwoDigitNumberPattern = context.getResources().getStringArray(17236044);
        }
        for (String dialnumber : sTwoDigitNumberPattern) {
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
        if (dialString != null && 2 >= dialString.length()) {
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
        return (HwTelephonyFactory.getHwPhoneManager().isShortCodeCustomization() || dialString == null || dialString.length() > 2 || (!phone.isInCall() && dialString.length() == 2 && dialString.charAt(0) == '1')) ? false : true;
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
                return 2;
            }
            if (isDeactivate()) {
                return 1;
            }
        }
        return 0;
    }

    boolean isActivate() {
        return this.mAction != null ? this.mAction.equals("*") : false;
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

    public static boolean isVoiceUnconditionalForwarding(int reason, int serviceClass) {
        boolean z = true;
        if (reason != 0 && reason != 4) {
            return false;
        }
        if ((serviceClass & 1) == 0 && serviceClass != 0) {
            z = false;
        }
        return z;
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
            int serviceClass;
            String facility;
            if (isShortCode()) {
                Rlog.d(this.LOG_TAG, "processCode: isShortCode");
                sendUssd(this.mDialingNumber);
            } else if (HwTelephonyFactory.getHwPhoneManager().changeMMItoUSSD(this.mPhone, this.mPoundString)) {
                Rlog.d(this.LOG_TAG, "changeMMItoUSSD");
                sendUssd(this.mPoundString);
            } else if (this.mDialingNumber != null) {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            } else if (this.mSc != null && this.mSc.equals(SC_CLIP)) {
                Rlog.d(this.LOG_TAG, "processCode: is CLIP");
                if (isInterrogate()) {
                    this.mPhone.mCi.queryCLIP(obtainMessage(5, this));
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (this.mSc != null && this.mSc.equals(SC_CLIR)) {
                Rlog.d(this.LOG_TAG, "processCode: is CLIR");
                if (isActivate()) {
                    this.mPhone.mCi.setCLIR(1, obtainMessage(1, this));
                } else if (isDeactivate()) {
                    this.mPhone.mCi.setCLIR(2, obtainMessage(1, this));
                } else if (isInterrogate()) {
                    this.mPhone.mCi.getCLIR(obtainMessage(2, this));
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (isServiceCodeCallForwarding(this.mSc)) {
                Rlog.d(this.LOG_TAG, "processCode: is CF");
                String dialingNumber = this.mSia;
                serviceClass = siToServiceClass(this.mSib);
                int reason = scToCallForwardReason(this.mSc);
                int time = siToTime(this.mSic);
                if (isInterrogate()) {
                    this.mPhone.mCi.queryCallForwardStatus(reason, serviceClass, dialingNumber, obtainMessage(3, this));
                } else {
                    int cfAction;
                    if (isActivate()) {
                        if (isEmptyOrNull(dialingNumber)) {
                            cfAction = 1;
                            this.mIsCallFwdReg = false;
                        } else {
                            cfAction = 3;
                            this.mIsCallFwdReg = true;
                        }
                    } else if (isDeactivate()) {
                        cfAction = 0;
                    } else if (isRegister()) {
                        cfAction = 3;
                    } else if (isErasure()) {
                        cfAction = 4;
                    } else {
                        throw new RuntimeException("invalid action");
                    }
                    int isEnableDesired = (cfAction == 1 || cfAction == 3) ? 1 : 0;
                    Rlog.d(this.LOG_TAG, "processCode: is CF setCallForward");
                    this.mPhone.mCi.setCallForward(cfAction, reason, serviceClass, dialingNumber, time, obtainMessage(6, isVoiceUnconditionalForwarding(reason, serviceClass) ? 1 : 0, isEnableDesired, this));
                }
            } else if (isServiceCodeCallBarring(this.mSc)) {
                String password = this.mSia;
                serviceClass = siToServiceClass(this.mSib);
                facility = scToBarringFacility(this.mSc);
                if (isInterrogate()) {
                    this.mPhone.mCi.queryFacilityLock(facility, password, serviceClass, obtainMessage(5, this));
                } else if (isActivate() || isDeactivate()) {
                    this.mPhone.mCi.setFacilityLock(facility, isActivate(), password, serviceClass, obtainMessage(1, this));
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
                        this.mPhone.mCi.changeBarringPassword(facility, oldPwd, newPwd, obtainMessage(1, this));
                    } else {
                        handlePasswordError(17040537);
                    }
                } else {
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else if (this.mSc != null && this.mSc.equals(SC_WAIT)) {
                serviceClass = siToServiceClass(this.mSia);
                if (isActivate() || isDeactivate()) {
                    this.mPhone.mCi.setCallWaiting(isActivate(), serviceClass, obtainMessage(1, this));
                } else if (isInterrogate()) {
                    this.mPhone.mCi.queryCallWaiting(serviceClass, obtainMessage(5, this));
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
                    } else if (pinLen < 4 || pinLen > 8) {
                        handlePasswordError(17040157);
                    } else if (this.mSc.equals(SC_PIN) && this.mUiccApplication != null && this.mUiccApplication.getState() == AppState.APPSTATE_PUK) {
                        handlePasswordError(17040460);
                    } else if (this.mUiccApplication != null) {
                        Rlog.d(this.LOG_TAG, "processCode: process mmi service code using UiccApp sc=" + this.mSc);
                        if (this.mSc.equals(SC_PIN)) {
                            this.mUiccApplication.changeIccLockPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                        } else if (this.mSc.equals(SC_PIN2)) {
                            this.mUiccApplication.changeIccFdnPassword(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                        } else if (this.mSc.equals(SC_PUK)) {
                            this.mUiccApplication.supplyPuk(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
                        } else if (this.mSc.equals(SC_PUK2)) {
                            this.mUiccApplication.supplyPuk2(oldPinOrPuk, newPinOrPuk, obtainMessage(1, this));
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
                Rlog.d(this.LOG_TAG, "processCode: Invalid or Unsupported MMI Code");
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        } catch (RuntimeException exc) {
            this.mState = State.FAILED;
            this.mMessage = this.mContext.getText(17040448);
            Rlog.d(this.LOG_TAG, "processCode: RuntimeException=" + exc);
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
            if (TextUtils.isEmpty(ussdMessage)) {
                this.mMessage = this.mContext.getText(HwTelephonyFactory.getHwPhoneManager().showMmiError(17040447));
                Rlog.d(this.LOG_TAG, "onUssdFinished: no network provided message; using default.");
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
            Rlog.d(this.LOG_TAG, "onUssdFinished");
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdFinishedError() {
        if (this.mState == State.PENDING) {
            this.mState = State.FAILED;
            if (USSD_REMOVE_ERROR_MSG && this.mIncomingUSSD) {
                this.mMessage = "";
            } else {
                this.mMessage = this.mContext.getText(17040448);
            }
            Rlog.d(this.LOG_TAG, "onUssdFinishedError");
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdRelease() {
        if (this.mState == State.PENDING) {
            this.mState = State.COMPLETE;
            this.mMessage = null;
            Rlog.d(this.LOG_TAG, "onUssdRelease");
            this.mPhone.onMMIDone(this);
        }
    }

    public void sendUssd(String ussdMessage) {
        this.mIsPendingUSSD = true;
        this.mPhone.mCi.sendUSSD(HwTelephonyFactory.getHwPhoneManager().convertUssdMessage(ussdMessage), obtainMessage(4, this));
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                onSetComplete(msg, msg.obj);
                return;
            case 2:
                onGetClirComplete((AsyncResult) msg.obj);
                return;
            case 3:
                onQueryCfComplete((AsyncResult) msg.obj);
                return;
            case 4:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    this.mState = State.FAILED;
                    this.mMessage = getErrorMessage(ar);
                    if (this.mSc != null && ((this.mSc.equals(SC_107) || this.mSc.equals(SC_108)) && (ar.exception instanceof CommandException) && Error.FDN_CHECK_FAILURE == ((CommandException) ar.exception).getCommandError())) {
                        Rlog.d(this.LOG_TAG, "SC is " + this.mSc + " and exception is FDN_CHECK_FAILURE");
                        this.mMessage = "";
                    }
                    this.mPhone.onMMIDone(this);
                    return;
                }
                return;
            case 5:
                onQueryComplete((AsyncResult) msg.obj);
                return;
            case 6:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && msg.arg1 == 1) {
                    boolean cffEnabled = msg.arg2 == 1;
                    if (this.mIccRecords != null) {
                        this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                    }
                }
                onSetComplete(msg, ar);
                return;
            case 7:
                this.mPhone.onMMIDone(this);
                return;
            default:
                HwTelephonyFactory.getHwPhoneManager().handleMessageGsmMmiCode(this, msg);
                return;
        }
    }

    private CharSequence getErrorMessage(AsyncResult ar) {
        if (ar.exception instanceof CommandException) {
            Error err = ((CommandException) ar.exception).getCommandError();
            if (err == Error.FDN_CHECK_FAILURE) {
                Rlog.i(this.LOG_TAG, "FDN_CHECK_FAILURE");
                return this.mContext.getText(17040450);
            } else if (err == Error.USSD_MODIFIED_TO_DIAL) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_DIAL");
                return this.mContext.getText(17041080);
            } else if (err == Error.USSD_MODIFIED_TO_SS) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_SS");
                return this.mContext.getText(17041081);
            } else if (err == Error.USSD_MODIFIED_TO_USSD) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_USSD");
                return this.mContext.getText(17041082);
            } else if (err == Error.SS_MODIFIED_TO_DIAL) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_DIAL");
                return this.mContext.getText(17041077);
            } else if (err == Error.SS_MODIFIED_TO_USSD) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_USSD");
                return this.mContext.getText(17041079);
            } else if (err == Error.SS_MODIFIED_TO_SS) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_SS");
                return this.mContext.getText(17041078);
            }
        }
        return this.mContext.getText(17040448);
    }

    private CharSequence getScString() {
        if (this.mSc != null) {
            if (isServiceCodeCallBarring(this.mSc)) {
                return this.mContext.getText(17039386);
            }
            if (isServiceCodeCallForwarding(this.mSc)) {
                return HwTelephonyFactory.getHwPhoneManager().getCallForwardingString(this.mContext, this.mSc);
            }
            if (this.mSc.equals(SC_CLIP)) {
                return this.mContext.getText(17039397);
            }
            if (this.mSc.equals(SC_CLIR)) {
                return this.mContext.getText(17039398);
            }
            if (this.mSc.equals(SC_PWD)) {
                return this.mContext.getText(17039455);
            }
            if (this.mSc.equals(SC_WAIT)) {
                return this.mContext.getText(17039404);
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
                            sb.append(this.mContext.getText(17039679));
                        } else {
                            sb.append(this.mContext.getText(33685781));
                        }
                        int attemptsRemaining = msg.arg1;
                        if (attemptsRemaining <= 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: PUK locked, cancel as lock screen will handle this");
                            this.mState = State.CANCELLED;
                        } else if (attemptsRemaining > 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: attemptsRemaining=" + attemptsRemaining);
                            sb.append(this.mContext.getResources().getQuantityString(18153496, attemptsRemaining, new Object[]{Integer.valueOf(attemptsRemaining)}));
                        }
                    } else {
                        sb.append(this.mContext.getText(17040537));
                    }
                } else if (err == Error.SIM_PUK2) {
                    sb.append(HwTelephonyFactory.getHwPhoneManager().processBadPinString(this.mContext, this.mSc));
                    sb.append("\n");
                    sb.append(this.mContext.getText(17040461));
                } else if (err == Error.REQUEST_NOT_SUPPORTED) {
                    if (this.mSc.equals(SC_PIN)) {
                        sb.append(this.mContext.getText(17039940));
                    }
                } else if (err == Error.FDN_CHECK_FAILURE) {
                    Rlog.i(this.LOG_TAG, "FDN_CHECK_FAILURE");
                    sb.append(this.mContext.getText(17040450));
                } else if (err == Error.MODEM_ERR) {
                    if (isServiceCodeCallForwarding(this.mSc) && this.mPhone.getServiceState().getVoiceRoaming() && (this.mPhone.supports3gppCallForwardingWhileRoaming() ^ 1) != 0) {
                        sb.append(this.mContext.getText(17040449));
                    } else {
                        sb.append(getErrorMessage(ar));
                    }
                } else if (SC_PIN.equals(this.mSc)) {
                    sb.append(this.mContext.getText(17040967));
                } else {
                    sb.append(getErrorMessage(ar));
                }
            } else {
                sb.append(this.mContext.getText(17040448));
            }
        } else if (isActivate()) {
            this.mState = State.COMPLETE;
            if (this.mIsCallFwdReg) {
                sb.append(this.mContext.getText(17040966));
            } else if (mToastSwitch) {
                sb.append(this.mContext.getText(17041040));
            } else {
                sb.append(this.mContext.getText(17040962));
            }
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(1);
            }
        } else if (isDeactivate()) {
            this.mState = State.COMPLETE;
            if (mToastSwitch) {
                sb.append(this.mContext.getText(17041039));
            } else {
                sb.append(this.mContext.getText(17040961));
            }
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(2);
            }
        } else if (isRegister()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17040966));
        } else if (isErasure()) {
            this.mState = State.COMPLETE;
            sb.append(this.mContext.getText(17040964));
        } else {
            this.mState = State.FAILED;
            sb.append(this.mContext.getText(17040448));
        }
        this.mMessage = sb;
        Rlog.d(this.LOG_TAG, "onSetComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private void onGetClirComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception == null) {
            int[] clirArgs = ar.result;
            switch (clirArgs[1]) {
                case 0:
                    sb.append(this.mContext.getText(17040965));
                    this.mState = State.COMPLETE;
                    break;
                case 1:
                    sb.append(this.mContext.getText(17039391));
                    this.mState = State.COMPLETE;
                    break;
                case 2:
                    sb.append(this.mContext.getText(17040448));
                    this.mState = State.FAILED;
                    break;
                case 3:
                    switch (clirArgs[0]) {
                        case 1:
                            sb.append(this.mContext.getText(17039390));
                            break;
                        case 2:
                            sb.append(this.mContext.getText(17039389));
                            break;
                        default:
                            sb.append(this.mContext.getText(17039390));
                            break;
                    }
                    this.mState = State.COMPLETE;
                    break;
                case 4:
                    switch (clirArgs[0]) {
                        case 1:
                            sb.append(this.mContext.getText(17039388));
                            break;
                        case 2:
                            sb.append(this.mContext.getText(17039387));
                            break;
                        default:
                            sb.append(this.mContext.getText(17039387));
                            break;
                    }
                    this.mState = State.COMPLETE;
                    break;
            }
        }
        this.mState = State.FAILED;
        sb.append(getErrorMessage(ar));
        this.mMessage = sb;
        Rlog.d(this.LOG_TAG, "onGetClirComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private CharSequence serviceClassToCFString(int serviceClass) {
        switch (serviceClass) {
            case 1:
                return this.mContext.getText(17040960);
            case 2:
                return this.mContext.getText(17040953);
            case 4:
                return this.mContext.getText(17040956);
            case 8:
                return this.mContext.getText(17040959);
            case 16:
                return this.mContext.getText(17040955);
            case 32:
                return this.mContext.getText(17040954);
            case 64:
                return this.mContext.getText(17040958);
            case 128:
                return this.mContext.getText(17040957);
            default:
                return null;
        }
    }

    private CharSequence makeCFQueryResultMessage(CallForwardInfo info, int serviceClassMask) {
        CharSequence template;
        String[] sources = new String[]{"{0}", "{1}", "{2}"};
        CharSequence[] destinations = new CharSequence[3];
        boolean needTimeTemplate = info.reason == 2;
        if (info.status == 1) {
            if (needTimeTemplate) {
                template = this.mContext.getText(17039722);
            } else {
                template = this.mContext.getText(17039721);
            }
        } else if (info.status == 0 && isEmptyOrNull(info.number)) {
            template = this.mContext.getText(17039723);
        } else if (needTimeTemplate) {
            template = this.mContext.getText(17039725);
        } else {
            template = this.mContext.getText(17039724);
        }
        destinations[0] = serviceClassToCFString(info.serviceClass & serviceClassMask);
        destinations[1] = formatLtr(PhoneNumberUtils.stringFromStringAndTOA(info.number, info.toa));
        destinations[2] = Integer.toString(info.timeSeconds);
        if (info.reason == 0 && (info.serviceClass & serviceClassMask) == 1) {
            boolean cffEnabled = info.status == 1;
            if (this.mIccRecords != null) {
                this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, info.number);
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
            if (infos == null || infos.length == 0) {
                if (mToastSwitch) {
                    sb.append(this.mContext.getText(17041039));
                } else {
                    sb.append(this.mContext.getText(17040961));
                }
                if (this.mIccRecords != null) {
                    this.mPhone.setVoiceCallForwardingFlag(1, false, null);
                }
            } else {
                SpannableStringBuilder tb = new SpannableStringBuilder();
                for (int serviceClassMask = 1; serviceClassMask <= 128; serviceClassMask <<= 1) {
                    int s = infos.length;
                    for (int i = 0; i < s; i++) {
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
        Rlog.d(this.LOG_TAG, "onQueryCfComplete: mmi=" + this);
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
                sb.append(this.mContext.getText(17040448));
            } else if (ints[0] == 0) {
                if (mToastSwitch) {
                    sb.append(this.mContext.getText(17040848));
                } else {
                    sb.append(this.mContext.getText(17040961));
                }
            } else if (this.mSc.equals(SC_WAIT)) {
                sb.append(createQueryCallWaitingResultMessage(ints[1]));
            } else if (isServiceCodeCallBarring(this.mSc)) {
                sb.append(createQueryCallBarringResultMessage(ints[0]));
            } else if (ints[0] != 1) {
                sb.append(this.mContext.getText(17040448));
            } else if (mToastSwitch) {
                sb.append(this.mContext.getText(17040849));
            } else {
                sb.append(this.mContext.getText(17040962));
            }
            this.mState = State.COMPLETE;
        }
        this.mMessage = sb;
        Rlog.d(this.LOG_TAG, "onQueryComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private CharSequence createQueryCallWaitingResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17040963));
        for (int classMask = 1; classMask <= 128; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    private CharSequence createQueryCallBarringResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17040963));
        for (int classMask = 1; classMask <= 128; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    public ResultReceiver getUssdCallbackReceiver() {
        return this.mCallbackReceiver;
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
            sb.append(" sia=").append(Rlog.pii(this.LOG_TAG, this.mSia));
        }
        if (this.mSib != null) {
            sb.append(" sib=").append(Rlog.pii(this.LOG_TAG, this.mSib));
        }
        if (this.mSic != null) {
            sb.append(" sic=").append(Rlog.pii(this.LOG_TAG, this.mSic));
        }
        if (this.mPoundString != null) {
            sb.append(" poundString=").append(Rlog.pii(this.LOG_TAG, this.mPoundString));
        }
        if (this.mDialingNumber != null) {
            sb.append(" dialingNumber=").append(Rlog.pii(this.LOG_TAG, this.mDialingNumber));
        }
        if (this.mPwd != null) {
            sb.append(" pwd=").append(Rlog.pii(this.LOG_TAG, this.mPwd));
        }
        if (this.mCallbackReceiver != null) {
            sb.append(" hasReceiver");
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

package com.android.internal.telephony.gsm;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.BidiFormatter;
import android.text.SpannableStringBuilder;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwCustMmiCode;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IHwGsmCdmaPhoneInner;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.gsm.SsData;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.util.ArrayUtils;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import huawei.cust.HwCustUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GsmMmiCode extends Handler implements MmiCode {
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
    static final int NOT_HAS_VALUES = -1;
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
    static final int VALUES_FALSE = 0;
    static final int VALUES_TRUE = 1;
    private static final boolean isDocomo = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    static Pattern sPatternSuppService;
    private static String[] sTwoDigitNumberPattern;
    String LOG_TAG = LOG_TAG_STATIC;
    String mAction;
    private ResultReceiver mCallbackReceiver;
    Context mContext;
    public String mDialingNumber;
    protected HwCustGsmMmiCode mHwCustGsmMmiCode;
    private HwCustMmiCode mHwCustMmiCode;
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
    MmiCode.State mState = MmiCode.State.PENDING;
    UiccCardApplication mUiccApplication;

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
        }
        if (phone.getServiceState().getVoiceRoaming() && phone.supportsConversionOfCdmaCallerIdMmiCodesWhileRoaming()) {
            dialString = convertCdmaMmiCodesTo3gppMmiCodes(dialString);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isStringHuaweiIgnoreCode(phone, dialString)) {
            Rlog.d(LOG_TAG_STATIC + "[SUB" + phone.getPhoneId() + "]", "newFromDialString, a huawei ignore code found, return null.");
            return null;
        }
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
            String str = ret.mDialingNumber;
            if (str != null && str.endsWith(ACTION_DEACTIVATE) && dialString.endsWith(ACTION_DEACTIVATE)) {
                ret = new GsmMmiCode(phone, app);
                ret.mPoundString = dialString;
            } else if (ret.isFacToDial()) {
                ret = null;
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
        if (ret != null) {
            ret.mCallbackReceiver = wrappedCallback;
        }
        return ret;
    }

    private static String convertCdmaMmiCodesTo3gppMmiCodes(String dialString) {
        Matcher m = sPatternCdmaMmiCodeWhileRoaming.matcher(dialString);
        if (!m.matches()) {
            return dialString;
        }
        String serviceCode = makeEmptyNull(m.group(1));
        String prefix = m.group(2);
        String number = makeEmptyNull(m.group(3));
        if (serviceCode.equals(SC_CFB) && number != null) {
            return "#31#" + prefix + number;
        } else if (!serviceCode.equals("82") || number == null) {
            return dialString;
        } else {
            return "*31#" + prefix + number;
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
            ret.mState = MmiCode.State.PENDING;
        } else {
            ret.mState = MmiCode.State.COMPLETE;
        }
        return ret;
    }

    public static GsmMmiCode newFromUssdUserInput(String ussdMessge, GsmCdmaPhone phone, UiccCardApplication app) {
        GsmMmiCode ret = new GsmMmiCode(phone, app);
        ret.mMessage = ussdMessge;
        ret.mState = MmiCode.State.PENDING;
        ret.mIsPendingUSSD = true;
        return ret;
    }

    public void processSsData(AsyncResult data) {
        Rlog.d(this.LOG_TAG, "In processSsData");
        this.mIsSsInfo = true;
        try {
            parseSsData((SsData) data.result);
        } catch (ClassCastException ex) {
            String str = this.LOG_TAG;
            Rlog.e(str, "Class Cast Exception in parsing SS Data : " + ex);
        } catch (NullPointerException ex2) {
            String str2 = this.LOG_TAG;
            Rlog.e(str2, "Null Pointer Exception in parsing SS Data : " + ex2);
        }
    }

    /* access modifiers changed from: package-private */
    public void parseSsData(SsData ssData) {
        CommandException ex = CommandException.fromRilErrno(ssData.result);
        this.mSc = getScStringFromScType(ssData.serviceType);
        this.mAction = getActionStringFromReqType(ssData.requestType);
        String str = this.LOG_TAG;
        Rlog.d(str, "parseSsData msc = " + this.mSc + ", action = " + this.mAction + ", ex = " + ex);
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType[ssData.requestType.ordinal()];
        if (i == 1 || i == 2 || i == 3 || i == 4) {
            if (ssData.result == 0 && ssData.serviceType.isTypeUnConditional()) {
                boolean cffEnabled = (ssData.requestType == SsData.RequestType.SS_ACTIVATION || ssData.requestType == SsData.RequestType.SS_REGISTRATION) && isServiceClassVoiceorNone(ssData.serviceClass);
                String str2 = this.LOG_TAG;
                Rlog.d(str2, "setVoiceCallForwardingFlag cffEnabled: " + cffEnabled);
                if (this.mIccRecords != null) {
                    this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, null);
                    Rlog.d(this.LOG_TAG, "setVoiceCallForwardingFlag done from SS Info.");
                } else {
                    Rlog.e(this.LOG_TAG, "setVoiceCallForwardingFlag aborted. sim records is null.");
                }
            }
            onSetComplete(null, new AsyncResult((Object) null, ssData.cfInfo, ex));
        } else if (i != 5) {
            String str3 = this.LOG_TAG;
            Rlog.e(str3, "Invaid requestType in SSData : " + ssData.requestType);
        } else if (ssData.serviceType.isTypeClir()) {
            Rlog.d(this.LOG_TAG, "CLIR INTERROGATION");
            onGetClirComplete(new AsyncResult((Object) null, ssData.ssInfo, ex));
        } else if (ssData.serviceType.isTypeCF()) {
            Rlog.d(this.LOG_TAG, "CALL FORWARD INTERROGATION");
            onQueryCfComplete(new AsyncResult((Object) null, ssData.cfInfo, ex));
        } else {
            onQueryComplete(new AsyncResult((Object) null, ssData.ssInfo, ex));
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.gsm.GsmMmiCode$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType = new int[SsData.RequestType.values().length];

        static {
            $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType = new int[SsData.ServiceType.values().length];
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CFU.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CF_BUSY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CF_NO_REPLY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CF_NOT_REACHABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CF_ALL.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CF_ALL_CONDITIONAL.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CLIP.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_CLIR.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_WAIT.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_BAOC.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_BAOIC.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_BAOIC_EXC_HOME.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_BAIC.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_BAIC_ROAMING.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_ALL_BARRING.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_OUTGOING_BARRING.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$ServiceType[SsData.ServiceType.SS_INCOMING_BARRING.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType[SsData.RequestType.SS_ACTIVATION.ordinal()] = 1;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType[SsData.RequestType.SS_DEACTIVATION.ordinal()] = 2;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType[SsData.RequestType.SS_REGISTRATION.ordinal()] = 3;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType[SsData.RequestType.SS_ERASURE.ordinal()] = 4;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType[SsData.RequestType.SS_INTERROGATION.ordinal()] = 5;
            } catch (NoSuchFieldError e22) {
            }
        }
    }

    private String getScStringFromScType(SsData.ServiceType sType) {
        switch (sType) {
            case SS_CFU:
                return SC_CFU;
            case SS_CF_BUSY:
                return SC_CFB;
            case SS_CF_NO_REPLY:
                return SC_CFNRy;
            case SS_CF_NOT_REACHABLE:
                return SC_CFNR;
            case SS_CF_ALL:
                return SC_CF_All;
            case SS_CF_ALL_CONDITIONAL:
                return SC_CF_All_Conditional;
            case SS_CLIP:
                return SC_CLIP;
            case SS_CLIR:
                return SC_CLIR;
            case SS_WAIT:
                return SC_WAIT;
            case SS_BAOC:
                return SC_BAOC;
            case SS_BAOIC:
                return SC_BAOIC;
            case SS_BAOIC_EXC_HOME:
                return SC_BAOICxH;
            case SS_BAIC:
                return SC_BAIC;
            case SS_BAIC_ROAMING:
                return SC_BAICr;
            case SS_ALL_BARRING:
                return SC_BA_ALL;
            case SS_OUTGOING_BARRING:
                return SC_BA_MO;
            case SS_INCOMING_BARRING:
                return SC_BA_MT;
            default:
                return PhoneConfigurationManager.SSSS;
        }
    }

    private String getActionStringFromReqType(SsData.RequestType rType) {
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$gsm$SsData$RequestType[rType.ordinal()];
        if (i == 1) {
            return "*";
        }
        if (i == 2) {
            return ACTION_DEACTIVATE;
        }
        if (i == 3) {
            return ACTION_REGISTER;
        }
        if (i == 4) {
            return ACTION_ERASURE;
        }
        if (i != 5) {
            return PhoneConfigurationManager.SSSS;
        }
        return ACTION_INTERROGATE;
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
        int serviceCode = Integer.parseInt(si, 10);
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
            case 12:
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
                            case TelephonyProto.RilErrno.RIL_E_USSD_MODIFIED_TO_USSD /*{ENCODED_INT: 24}*/:
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

    private static int siToTime(String si) {
        if (si == null || si.length() == 0) {
            return 0;
        }
        return Integer.parseInt(si, 10);
    }

    public boolean isServiceCodeCallForwarding() {
        if (!TextUtils.isEmpty(this.mSc)) {
            return isServiceCodeCallForwarding(this.mSc);
        }
        return false;
    }

    public static boolean isServiceCodeCallForwarding(String sc) {
        return sc != null && (sc.equals(SC_CFU) || sc.equals(SC_CFB) || sc.equals(SC_CFNRy) || sc.equals(SC_CFNR) || sc.equals(SC_CF_All) || sc.equals(SC_CF_All_Conditional));
    }

    static boolean isServiceCodeCallBarring(String sc) {
        String[] barringMMI;
        Resources resource = Resources.getSystem();
        if (!(sc == null || (barringMMI = resource.getStringArray(17235995)) == null)) {
            for (String match : barringMMI) {
                if (sc.equals(match)) {
                    return true;
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
        if (this.mHwCustGsmMmiCode == null) {
            this.mHwCustGsmMmiCode = (HwCustGsmMmiCode) HwCustUtils.createObj(HwCustGsmMmiCode.class, new Object[]{phone});
        }
        if (this.mHwCustMmiCode == null) {
            this.mHwCustMmiCode = (HwCustMmiCode) HwCustUtils.createObj(HwCustMmiCode.class, new Object[0]);
        }
        this.LOG_TAG += "[SUB" + phone.getPhoneId() + "]";
    }

    @Override // com.android.internal.telephony.MmiCode
    public MmiCode.State getState() {
        return this.mState;
    }

    @Override // com.android.internal.telephony.MmiCode
    public CharSequence getMessage() {
        return this.mMessage;
    }

    @Override // com.android.internal.telephony.MmiCode
    public Phone getPhone() {
        return this.mPhone;
    }

    @Override // com.android.internal.telephony.MmiCode
    public void cancel() {
        if (this.mState != MmiCode.State.COMPLETE && this.mState != MmiCode.State.FAILED) {
            this.mState = MmiCode.State.CANCELLED;
            if (this.mIsPendingUSSD) {
                this.mPhone.mCi.cancelPendingUssd(obtainMessage(7, this));
            } else {
                this.mPhone.onMMIDone(this);
            }
        }
    }

    @Override // com.android.internal.telephony.MmiCode
    public boolean isCancelable() {
        return this.mIsPendingUSSD;
    }

    /* access modifiers changed from: package-private */
    public boolean isMMI() {
        return this.mPoundString != null;
    }

    /* access modifiers changed from: package-private */
    public boolean isShortCode() {
        String str;
        return this.mPoundString == null && (str = this.mDialingNumber) != null && str.length() <= 2;
    }

    @Override // com.android.internal.telephony.MmiCode
    public String getDialString() {
        return this.mPoundString;
    }

    private static boolean isTwoDigitShortCode(Context context, String dialString) {
        Rlog.d(LOG_TAG_STATIC, "isTwoDigitShortCode");
        if (dialString == null || dialString.length() > 2) {
            return false;
        }
        if (sTwoDigitNumberPattern == null) {
            sTwoDigitNumberPattern = context.getResources().getStringArray(17236074);
        }
        String[] strArr = sTwoDigitNumberPattern;
        for (String dialnumber : strArr) {
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
        HwPhoneManager hwPhoneManager = HwTelephonyFactory.getHwPhoneManager();
        if ((hwPhoneManager == null || !hwPhoneManager.isShortCodeHw(dialString, phone)) && !PhoneNumberUtils.isLocalEmergencyNumber(phone.getContext(), dialString)) {
            return isShortCodeUSSD(dialString, phone);
        }
        return false;
    }

    private static boolean isShortCodeUSSD(String dialString, GsmCdmaPhone phone) {
        if (!HwTelephonyFactory.getHwPhoneManager().isShortCodeCustomization() && dialString != null && dialString.length() <= 2 && (phone.isInCall() || dialString.length() != 2 || dialString.charAt(0) != '1')) {
            return true;
        }
        return false;
    }

    @Override // com.android.internal.telephony.MmiCode
    public boolean isPinPukCommand() {
        String str = this.mSc;
        return str != null && (str.equals(SC_PIN) || this.mSc.equals(SC_PIN2) || this.mSc.equals(SC_PUK) || this.mSc.equals(SC_PUK2));
    }

    public boolean isTemporaryModeCLIR() {
        String str = this.mSc;
        return str != null && str.equals(SC_CLIR) && this.mDialingNumber != null && (isActivate() || isDeactivate());
    }

    public int getCLIRMode() {
        String str = this.mSc;
        if (str == null || !str.equals(SC_CLIR)) {
            return 0;
        }
        if (isActivate()) {
            return 2;
        }
        if (isDeactivate()) {
            return 1;
        }
        return 0;
    }

    private boolean isFacToDial() {
        PersistableBundle b = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        if (b != null) {
            String[] dialFacList = b.getStringArray("feature_access_codes_string_array");
            if (!ArrayUtils.isEmpty(dialFacList)) {
                for (String fac : dialFacList) {
                    if (fac.equals(this.mSc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isActivate() {
        String str = this.mAction;
        return str != null && str.equals("*");
    }

    /* access modifiers changed from: package-private */
    public boolean isDeactivate() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_DEACTIVATE);
    }

    /* access modifiers changed from: package-private */
    public boolean isInterrogate() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_INTERROGATE);
    }

    /* access modifiers changed from: package-private */
    public boolean isRegister() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_REGISTER);
    }

    /* access modifiers changed from: package-private */
    public boolean isErasure() {
        String str = this.mAction;
        return str != null && str.equals(ACTION_ERASURE);
    }

    public boolean isPendingUSSD() {
        return this.mIsPendingUSSD;
    }

    @Override // com.android.internal.telephony.MmiCode
    public boolean isUssdRequest() {
        return this.mIsUssdRequest;
    }

    public boolean isSsInfo() {
        return this.mIsSsInfo;
    }

    public static boolean isVoiceUnconditionalForwarding(int reason, int serviceClass) {
        return (reason == 0 || reason == 4) && ((serviceClass & 1) != 0 || serviceClass == 0);
    }

    @Override // com.android.internal.telephony.MmiCode
    public void processCode() throws CallStateException {
        String facility;
        int cfAction;
        try {
            if (HwTelephonyFactory.getHwPhoneManager().isStringHuaweiCustCode(this.mPoundString)) {
                Rlog.d(this.LOG_TAG, "Huawei custimized MMI codes, send out directly. ");
                sendUssd(this.mPoundString);
            } else if (HwTelephonyFactory.getHwPhoneManager().changeMMItoUSSD(this.mPhone, this.mPoundString)) {
                Rlog.d(this.LOG_TAG, "changeMMItoUSSD");
                sendUssd(this.mPoundString);
            } else if (HwTelephonyFactory.getHwPhoneManager().processImsPhoneMmiCode(this, this.mImsPhone)) {
                Rlog.d(this.LOG_TAG, "Process IMS Phone MMI codes.");
            } else if (isShortCode()) {
                Rlog.d(this.LOG_TAG, "processCode: isShortCode");
                sendUssd(this.mDialingNumber);
            } else if (this.mDialingNumber != null) {
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            } else if (this.mSc == null || !this.mSc.equals(SC_CLIP)) {
                int i = 1;
                if (this.mSc != null && this.mSc.equals(SC_CLIR)) {
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
                    int serviceClass = siToServiceClass(this.mSib);
                    int reason = scToCallForwardReason(this.mSc);
                    int time = siToTime(this.mSic);
                    if (isInterrogate()) {
                        this.mPhone.mCi.queryCallForwardStatus(reason, serviceClass, dialingNumber, obtainMessage(3, this));
                        return;
                    }
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
                    CommandsInterface commandsInterface = this.mPhone.mCi;
                    if (!isVoiceUnconditionalForwarding(reason, serviceClass)) {
                        i = 0;
                    }
                    commandsInterface.setCallForward(cfAction, reason, serviceClass, dialingNumber, time, obtainMessage(6, i, isEnableDesired, this));
                } else if (isServiceCodeCallBarring(this.mSc)) {
                    String password = this.mSia;
                    int serviceClass2 = siToServiceClass(this.mSib);
                    String facility2 = scToBarringFacility(this.mSc);
                    if (isInterrogate()) {
                        this.mPhone.mCi.queryFacilityLock(facility2, password, serviceClass2, obtainMessage(5, this));
                    } else if (isActivate() || isDeactivate()) {
                        this.mPhone.mCi.setFacilityLock(facility2, isActivate(), password, serviceClass2, obtainMessage(1, this));
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
                            handlePasswordError(17040718);
                        }
                    } else {
                        throw new RuntimeException("Invalid or Unsupported MMI Code");
                    }
                } else if (this.mSc != null && this.mSc.equals(SC_WAIT)) {
                    int serviceClass3 = siToServiceClass(this.mSia);
                    if (isActivate() || isDeactivate()) {
                        this.mPhone.mCi.setCallWaiting(isActivate(), serviceClass3, obtainMessage(1, this));
                    } else if (isInterrogate()) {
                        this.mPhone.mCi.queryCallWaiting(serviceClass3, obtainMessage(5, this));
                    } else {
                        throw new RuntimeException("Invalid or Unsupported MMI Code");
                    }
                } else if (isPinPukCommand()) {
                    String oldPinOrPuk = this.mSia;
                    String newPinOrPuk = this.mSib;
                    int pinLen = newPinOrPuk.length();
                    if (!isRegister()) {
                        throw new RuntimeException("Ivalid register/action=" + this.mAction);
                    } else if (!newPinOrPuk.equals(this.mSic)) {
                        handlePasswordError(HwTelephonyFactory.getHwPhoneManager().handlePasswordError(this.mSc));
                    } else if (pinLen < 4 || pinLen > 8) {
                        handlePasswordError(17040301);
                    } else if (this.mSc.equals(SC_PIN) && this.mUiccApplication != null && this.mUiccApplication.getState() == IccCardApplicationStatus.AppState.APPSTATE_PUK) {
                        handlePasswordError(17040629);
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
                } else if (this.mPoundString != null) {
                    sendUssd(this.mPoundString);
                } else {
                    Rlog.d(this.LOG_TAG, "processCode: Invalid or Unsupported MMI Code");
                    throw new RuntimeException("Invalid or Unsupported MMI Code");
                }
            } else {
                Rlog.d(this.LOG_TAG, "processCode: is CLIP");
                if (isInterrogate()) {
                    this.mPhone.mCi.queryCLIP(obtainMessage(5, this));
                    return;
                }
                throw new RuntimeException("Invalid or Unsupported MMI Code");
            }
        } catch (RuntimeException exc) {
            this.mState = MmiCode.State.FAILED;
            this.mMessage = this.mContext.getText(17040617);
            Rlog.d(this.LOG_TAG, "processCode: RuntimeException=" + exc);
            this.mPhone.onMMIDone(this);
        }
    }

    private void handlePasswordError(int res) {
        this.mState = MmiCode.State.FAILED;
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        sb.append(this.mContext.getText(res));
        this.mMessage = sb;
        this.mPhone.onMMIDone(this);
    }

    public void onUssdFinished(String ussdMessage, boolean isUssdRequest) {
        if (this.mState == MmiCode.State.PENDING) {
            if (TextUtils.isEmpty(ussdMessage)) {
                this.mMessage = this.mContext.getText(HwTelephonyFactory.getHwPhoneManager().showMmiError(17040616, SubscriptionManager.getSlotIndex(this.mPhone.getSubId())));
                Rlog.d(this.LOG_TAG, "onUssdFinished: no network provided message; using default.");
            } else {
                this.mMessage = ussdMessage;
            }
            this.mIsUssdRequest = isUssdRequest;
            if (!isUssdRequest) {
                this.mState = MmiCode.State.COMPLETE;
                HwPhoneManager hwPhoneManager = HwTelephonyFactory.getHwPhoneManager();
                int custUssdState = -1;
                if (hwPhoneManager != null) {
                    custUssdState = hwPhoneManager.removeUssdCust(this.mPhone);
                }
                if (custUssdState != 0 && ((USSD_REMOVE_ERROR_MSG || custUssdState == 1) && this.mIncomingUSSD && ussdMessage == null)) {
                    this.mMessage = PhoneConfigurationManager.SSSS;
                }
            }
            Rlog.d(this.LOG_TAG, "onUssdFinished");
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdFinishedError() {
        if (this.mState == MmiCode.State.PENDING) {
            this.mState = MmiCode.State.FAILED;
            HwPhoneManager hwPhoneManager = HwTelephonyFactory.getHwPhoneManager();
            int custremoveUssdState = -1;
            if (hwPhoneManager != null) {
                custremoveUssdState = hwPhoneManager.removeUssdCust(this.mPhone);
            }
            if (custremoveUssdState == 0 || ((!USSD_REMOVE_ERROR_MSG && custremoveUssdState != 1) || !this.mIncomingUSSD)) {
                this.mMessage = this.mContext.getText(17040617);
            } else {
                this.mMessage = PhoneConfigurationManager.SSSS;
            }
            Rlog.d(this.LOG_TAG, "onUssdFinishedError");
            this.mPhone.onMMIDone(this);
        }
    }

    public void onUssdRelease() {
        if (this.mState == MmiCode.State.PENDING) {
            this.mState = MmiCode.State.COMPLETE;
            this.mMessage = null;
            Rlog.d(this.LOG_TAG, "onUssdRelease");
            this.mPhone.onMMIDone(this);
        }
    }

    public void sendUssd(String ussdMessage) {
        if (HwTelephonyFactory.getHwPhoneManager().processSendUssdInImsCall(this, this.mImsPhone)) {
            Rlog.i(this.LOG_TAG, "forbid sending ussd when is in ims call.");
            return;
        }
        Rlog.i(this.LOG_TAG, "executing sendUssd.");
        this.mIsPendingUSSD = true;
        this.mPhone.mCi.sendUSSD(HwTelephonyFactory.getHwPhoneManager().convertUssdMessage(ussdMessage), obtainMessage(4, this));
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                onSetComplete(msg, (AsyncResult) msg.obj);
                return;
            case 2:
                onGetClirComplete((AsyncResult) msg.obj);
                return;
            case 3:
                onQueryCfComplete((AsyncResult) msg.obj);
                return;
            case 4:
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    this.mState = MmiCode.State.FAILED;
                    this.mMessage = getErrorMessage(ar);
                    String str = this.mSc;
                    if (str != null && ((str.equals(SC_107) || this.mSc.equals(SC_108)) && (ar.exception instanceof CommandException) && CommandException.Error.FDN_CHECK_FAILURE == ((CommandException) ar.exception).getCommandError())) {
                        String str2 = this.LOG_TAG;
                        Rlog.d(str2, "SC is " + this.mSc + " and exception is FDN_CHECK_FAILURE");
                        this.mMessage = PhoneConfigurationManager.SSSS;
                    }
                    this.mPhone.onMMIDone(this);
                    return;
                }
                return;
            case 5:
                onQueryComplete((AsyncResult) msg.obj);
                return;
            case 6:
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null && msg.arg1 == 1) {
                    boolean cffEnabled = msg.arg2 == 1;
                    if (this.mIccRecords != null) {
                        this.mPhone.setVoiceCallForwardingFlag(1, cffEnabled, this.mDialingNumber);
                    }
                }
                onSetComplete(msg, ar2);
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
            CommandException.Error err = ((CommandException) ar.exception).getCommandError();
            if (err == CommandException.Error.FDN_CHECK_FAILURE) {
                Rlog.i(this.LOG_TAG, "FDN_CHECK_FAILURE");
                return this.mContext.getText(17040619);
            } else if (err == CommandException.Error.USSD_MODIFIED_TO_DIAL) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_DIAL");
                return this.mContext.getText(17041331);
            } else if (err == CommandException.Error.USSD_MODIFIED_TO_SS) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_SS");
                return this.mContext.getText(17041333);
            } else if (err == CommandException.Error.USSD_MODIFIED_TO_USSD) {
                Rlog.i(this.LOG_TAG, "USSD_MODIFIED_TO_USSD");
                return this.mContext.getText(17041334);
            } else if (err == CommandException.Error.SS_MODIFIED_TO_DIAL) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_DIAL");
                return this.mContext.getText(17041327);
            } else if (err == CommandException.Error.SS_MODIFIED_TO_USSD) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_USSD");
                return this.mContext.getText(17041330);
            } else if (err == CommandException.Error.SS_MODIFIED_TO_SS) {
                Rlog.i(this.LOG_TAG, "SS_MODIFIED_TO_SS");
                return this.mContext.getText(17041329);
            } else if (err == CommandException.Error.OEM_ERROR_1) {
                Rlog.i(this.LOG_TAG, "OEM_ERROR_1 USSD_MODIFIED_TO_DIAL_VIDEO");
                return this.mContext.getText(17041332);
            }
        }
        CharSequence result = this.mContext.getText(17040617);
        HwCustGsmMmiCode hwCustGsmMmiCode = this.mHwCustGsmMmiCode;
        if (hwCustGsmMmiCode != null) {
            return hwCustGsmMmiCode.getErrorMessageEx(ar, result);
        }
        return result;
    }

    private CharSequence getScString() {
        String str = this.mSc;
        if (str == null) {
            return PhoneConfigurationManager.SSSS;
        }
        if (isServiceCodeCallBarring(str)) {
            if (!isDocomo || !this.mSc.equals(SC_BAICr)) {
                return this.mContext.getText(17039397);
            }
            return this.mContext.getText(33685504);
        } else if (isServiceCodeCallForwarding(this.mSc)) {
            return HwTelephonyFactory.getHwPhoneManager().getCallForwardingString(this.mContext, this.mSc);
        } else {
            if (this.mSc.equals(SC_CLIP)) {
                return this.mContext.getText(17039408);
            }
            if (this.mSc.equals(SC_CLIR)) {
                return this.mContext.getText(17039409);
            }
            if (this.mSc.equals(SC_PWD)) {
                return this.mContext.getText(17039468);
            }
            if (this.mSc.equals(SC_WAIT)) {
                return this.mContext.getText(17039415);
            }
            if (isPinPukCommand()) {
                return HwTelephonyFactory.getHwPhoneManager().processgoodPinString(this.mContext, this.mSc);
            }
            return PhoneConfigurationManager.SSSS;
        }
    }

    private void onSetComplete(Message msg, AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            if (ar.exception instanceof CommandException) {
                CommandException.Error err = ((CommandException) ar.exception).getCommandError();
                if (err == CommandException.Error.PASSWORD_INCORRECT) {
                    if (isPinPukCommand()) {
                        if (!this.mSc.equals(SC_PUK) && !this.mSc.equals(SC_PUK2)) {
                            sb.append(HwTelephonyFactory.getHwPhoneManager().processBadPinString(this.mContext, this.mSc));
                        } else if (this.mSc.equals(SC_PUK)) {
                            sb.append(this.mContext.getText(17039703));
                        } else {
                            sb.append(this.mContext.getText(33685781));
                        }
                        int attemptsRemaining = msg.arg1;
                        if (attemptsRemaining <= 0) {
                            Rlog.d(this.LOG_TAG, "onSetComplete: PUK locked, cancel as lock screen will handle this");
                            this.mState = MmiCode.State.CANCELLED;
                        } else if (attemptsRemaining > 0) {
                            String str = this.LOG_TAG;
                            Rlog.d(str, "onSetComplete: attemptsRemaining=" + attemptsRemaining);
                            sb.append(this.mContext.getResources().getQuantityString(18153495, attemptsRemaining, Integer.valueOf(attemptsRemaining)));
                        }
                    } else {
                        sb.append(this.mContext.getText(17040718));
                    }
                } else if (err == CommandException.Error.SIM_PUK2) {
                    sb.append(HwTelephonyFactory.getHwPhoneManager().processBadPinString(this.mContext, this.mSc));
                    sb.append("\n");
                    sb.append(this.mContext.getText(17040630));
                } else if (err == CommandException.Error.REQUEST_NOT_SUPPORTED) {
                    if (this.mSc.equals(SC_PIN)) {
                        sb.append(this.mContext.getText(17040032));
                    }
                } else if (err == CommandException.Error.FDN_CHECK_FAILURE) {
                    Rlog.i(this.LOG_TAG, "FDN_CHECK_FAILURE");
                    sb.append(this.mContext.getText(17040619));
                } else if (err == CommandException.Error.MODEM_ERR) {
                    if (!isServiceCodeCallForwarding(this.mSc) || !this.mPhone.getServiceState().getVoiceRoaming() || this.mPhone.supports3gppCallForwardingWhileRoaming()) {
                        sb.append(getErrorMessage(ar));
                    } else {
                        sb.append(this.mContext.getText(17040618));
                    }
                } else if (SC_PIN.equals(this.mSc)) {
                    sb.append(this.mContext.getText(17041204));
                } else {
                    sb.append(getErrorMessage(ar));
                }
            } else {
                sb.append(this.mContext.getText(17040617));
            }
        } else if (isActivate()) {
            this.mState = MmiCode.State.COMPLETE;
            if (this.mIsCallFwdReg) {
                sb.append(this.mContext.getText(17041203));
            } else {
                HwCustMmiCode hwCustMmiCode = this.mHwCustMmiCode;
                if (hwCustMmiCode == null || !hwCustMmiCode.isSsToastSwitchEnabled()) {
                    sb.append(this.mContext.getText(17041199));
                } else {
                    sb.append(this.mHwCustMmiCode.getCustSsToastString(this.mContext, true, false));
                }
            }
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(1);
            }
        } else if (isDeactivate()) {
            this.mState = MmiCode.State.COMPLETE;
            HwCustMmiCode hwCustMmiCode2 = this.mHwCustMmiCode;
            if (hwCustMmiCode2 == null || !hwCustMmiCode2.isSsToastSwitchEnabled()) {
                sb.append(this.mContext.getText(17041198));
            } else {
                sb.append(this.mHwCustMmiCode.getCustSsToastString(this.mContext, false, false));
            }
            if (this.mSc.equals(SC_CLIR)) {
                this.mPhone.saveClirSetting(2);
            }
        } else if (isRegister()) {
            this.mState = MmiCode.State.COMPLETE;
            sb.append(this.mContext.getText(17041203));
        } else if (isErasure()) {
            this.mState = MmiCode.State.COMPLETE;
            sb.append(this.mContext.getText(17041201));
        } else {
            this.mState = MmiCode.State.FAILED;
            sb.append(this.mContext.getText(17040617));
        }
        this.mMessage = sb;
        String str2 = this.LOG_TAG;
        Rlog.d(str2, "onSetComplete mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private void onGetClirComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            sb.append(getErrorMessage(ar));
        } else {
            int[] clirArgs = (int[]) ar.result;
            int i = clirArgs[1];
            if (i == 0) {
                sb.append(this.mContext.getText(17041202));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 1) {
                sb.append(this.mContext.getText(17039402));
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 2) {
                sb.append(this.mContext.getText(17040617));
                this.mState = MmiCode.State.FAILED;
            } else if (i == 3) {
                int i2 = clirArgs[0];
                if (i2 == 1) {
                    sb.append(this.mContext.getText(17039401));
                } else if (i2 != 2) {
                    sb.append(this.mContext.getText(17039401));
                } else {
                    sb.append(this.mContext.getText(17039400));
                }
                this.mState = MmiCode.State.COMPLETE;
            } else if (i == 4) {
                int i3 = clirArgs[0];
                if (i3 == 1) {
                    sb.append(this.mContext.getText(17039399));
                } else if (i3 != 2) {
                    sb.append(this.mContext.getText(17039398));
                } else {
                    sb.append(this.mContext.getText(17039398));
                }
                this.mState = MmiCode.State.COMPLETE;
            }
        }
        this.mMessage = sb;
        String str = this.LOG_TAG;
        Rlog.d(str, "onGetClirComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private CharSequence serviceClassToCFString(int serviceClass) {
        if (serviceClass == 1) {
            return this.mContext.getText(17041197);
        }
        if (serviceClass == 2) {
            return this.mContext.getText(17041190);
        }
        if (serviceClass == 4) {
            return this.mContext.getText(17041193);
        }
        if (serviceClass == 8) {
            return this.mContext.getText(17041196);
        }
        if (serviceClass == 16) {
            return this.mContext.getText(17041192);
        }
        if (serviceClass == 32) {
            return this.mContext.getText(17041191);
        }
        if (serviceClass == 64) {
            return this.mContext.getText(17041195);
        }
        if (serviceClass != 128) {
            return null;
        }
        return this.mContext.getText(17041194);
    }

    private CharSequence makeCFQueryResultMessage(CallForwardInfo info, int serviceClassMask) {
        CharSequence template;
        String[] sources = {"{0}", "{1}", "{2}"};
        CharSequence[] destinations = new CharSequence[3];
        boolean cffEnabled = false;
        boolean needTimeTemplate = info.reason == 2;
        if (info.status == 1) {
            if (needTimeTemplate) {
                template = this.mContext.getText(17039757);
            } else {
                template = this.mContext.getText(17039756);
            }
        } else if (info.status == 0 && isEmptyOrNull(info.number)) {
            template = this.mContext.getText(17039758);
        } else if (needTimeTemplate) {
            template = this.mContext.getText(17039760);
        } else {
            template = this.mContext.getText(17039759);
        }
        destinations[0] = serviceClassToCFString(info.serviceClass & serviceClassMask);
        destinations[1] = formatLtr(PhoneNumberUtils.stringFromStringAndTOA(info.number, info.toa));
        destinations[2] = Integer.toString(info.timeSeconds);
        if (info.reason == 0 && (info.serviceClass & serviceClassMask) == 1) {
            if (info.status == 1) {
                cffEnabled = true;
            }
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
            this.mState = MmiCode.State.FAILED;
            sb.append(getErrorMessage(ar));
        } else {
            CallForwardInfo[] infos = (CallForwardInfo[]) ar.result;
            if (infos == null || infos.length == 0) {
                HwCustMmiCode hwCustMmiCode = this.mHwCustMmiCode;
                if (hwCustMmiCode == null || !hwCustMmiCode.isSsToastSwitchEnabled()) {
                    sb.append(this.mContext.getText(17041198));
                } else {
                    sb.append(this.mHwCustMmiCode.getCustSsToastString(this.mContext, false, false));
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
                            tb.append((CharSequence) "\n");
                        }
                    }
                }
                sb.append((CharSequence) tb);
            }
            this.mState = MmiCode.State.COMPLETE;
        }
        this.mMessage = sb;
        String str = this.LOG_TAG;
        Rlog.d(str, "onQueryCfComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private void onQueryComplete(AsyncResult ar) {
        StringBuilder sb = new StringBuilder(getScString());
        sb.append("\n");
        if (ar.exception != null) {
            this.mState = MmiCode.State.FAILED;
            sb.append(getErrorMessage(ar));
        } else {
            int[] ints = (int[]) ar.result;
            if (ints.length == 0) {
                sb.append(this.mContext.getText(17040617));
            } else if (ints[0] == 0) {
                HwCustMmiCode hwCustMmiCode = this.mHwCustMmiCode;
                if (hwCustMmiCode == null || !hwCustMmiCode.isSsToastSwitchEnabled()) {
                    sb.append(this.mContext.getText(17041198));
                } else {
                    sb.append(this.mHwCustMmiCode.getCustSsToastString(this.mContext, false, true));
                }
            } else if (this.mSc.equals(SC_WAIT)) {
                sb.append(createQueryCallWaitingResultMessage(ints[1]));
            } else if (isServiceCodeCallBarring(this.mSc)) {
                sb.append(createQueryCallBarringResultMessage(ints[0]));
            } else if (ints[0] == 1) {
                HwCustMmiCode hwCustMmiCode2 = this.mHwCustMmiCode;
                if (hwCustMmiCode2 == null || !hwCustMmiCode2.isSsToastSwitchEnabled()) {
                    sb.append(this.mContext.getText(17041199));
                } else {
                    sb.append(this.mHwCustMmiCode.getCustSsToastString(this.mContext, true, true));
                }
            } else {
                sb.append(this.mContext.getText(17040617));
            }
            this.mState = MmiCode.State.COMPLETE;
        }
        this.mMessage = sb;
        String str = this.LOG_TAG;
        Rlog.d(str, "onQueryComplete: mmi=" + this);
        this.mPhone.onMMIDone(this);
    }

    private CharSequence createQueryCallWaitingResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17041200));
        for (int classMask = 1; classMask <= 128; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    private CharSequence createQueryCallBarringResultMessage(int serviceClass) {
        StringBuilder sb = new StringBuilder(this.mContext.getText(17041200));
        for (int classMask = 1; classMask <= 128; classMask <<= 1) {
            if ((classMask & serviceClass) != 0) {
                sb.append("\n");
                sb.append(serviceClassToCFString(classMask & serviceClass));
            }
        }
        return sb;
    }

    @Override // com.android.internal.telephony.MmiCode
    public ResultReceiver getUssdCallbackReceiver() {
        return this.mCallbackReceiver;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("GsmMmiCode {");
        sb.append("State=" + getState());
        if (this.mAction != null) {
            sb.append(" action=" + this.mAction);
        }
        if (this.mSc != null) {
            sb.append(" sc=" + this.mSc);
        }
        if (this.mSia != null) {
            sb.append(" sia=" + Rlog.pii(this.LOG_TAG, this.mSia));
        }
        if (this.mSib != null) {
            sb.append(" sib=" + Rlog.pii(this.LOG_TAG, this.mSib));
        }
        if (this.mSic != null) {
            sb.append(" sic=" + Rlog.pii(this.LOG_TAG, this.mSic));
        }
        if (this.mPoundString != null) {
            sb.append(" poundString=" + Rlog.pii(this.LOG_TAG, this.mPoundString));
        }
        if (this.mDialingNumber != null) {
            sb.append(" dialingNumber=" + Rlog.pii(this.LOG_TAG, this.mDialingNumber));
        }
        if (this.mPwd != null) {
            sb.append(" pwd=" + Rlog.pii(this.LOG_TAG, this.mPwd));
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

    public static GsmMmiCode newFromDialString(String dialString, IHwGsmCdmaPhoneInner phone, UiccCardApplication app) {
        return newFromDialString(dialString, phone.getGsmCdmaPhone(), app);
    }

    public static GsmMmiCode newFromDialString(String dialString, IHwGsmCdmaPhoneInner phone, UiccCardApplicationEx app) {
        return newFromDialString(dialString, phone.getGsmCdmaPhone(), app.getUiccCardApplication());
    }
}

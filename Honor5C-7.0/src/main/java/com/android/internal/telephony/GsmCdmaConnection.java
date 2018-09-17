package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.Connection.PostDialState;
import com.android.internal.telephony.DriverCall.State;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.BearerData;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.huawei.internal.telephony.HwRadarUtils;

public class GsmCdmaConnection extends AbstractGsmCdmaConnection {
    private static final /* synthetic */ int[] -com-android-internal-telephony-DriverCall$StateSwitchesValues = null;
    private static final boolean DBG = true;
    static final int EVENT_DTMF_DELAY_DONE = 5;
    static final int EVENT_DTMF_DONE = 1;
    static final int EVENT_NEXT_POST_DIAL = 3;
    static final int EVENT_PAUSE_DONE = 2;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 4;
    private static final String LOG_TAG = "GsmCdmaConnection";
    static final int PAUSE_DELAY_MILLIS_CDMA = 2000;
    static final int PAUSE_DELAY_MILLIS_GSM = 3000;
    private static final boolean VDBG = false;
    static final int WAKE_LOCK_TIMEOUT_MILLIS = 60000;
    long mDisconnectTime;
    boolean mDisconnected;
    private int mDtmfToneDelay;
    Handler mHandler;
    int mIndex;
    GsmCdmaCallTracker mOwner;
    GsmCdmaCall mParent;
    private WakeLock mPartialWakeLock;
    int mPreciseCause;
    UUSInfo mUusInfo;
    String mVendorCause;

    class MyHandler extends Handler {
        MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GsmCdmaConnection.EVENT_DTMF_DONE /*1*/:
                    GsmCdmaConnection.this.mHandler.sendMessageDelayed(GsmCdmaConnection.this.mHandler.obtainMessage(GsmCdmaConnection.EVENT_DTMF_DELAY_DONE), (long) GsmCdmaConnection.this.mDtmfToneDelay);
                case GsmCdmaConnection.EVENT_PAUSE_DONE /*2*/:
                case GsmCdmaConnection.EVENT_NEXT_POST_DIAL /*3*/:
                case GsmCdmaConnection.EVENT_DTMF_DELAY_DONE /*5*/:
                    GsmCdmaConnection.this.processNextPostDialChar();
                case GsmCdmaConnection.EVENT_WAKE_LOCK_TIMEOUT /*4*/:
                    GsmCdmaConnection.this.releaseWakeLock();
                default:
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-DriverCall$StateSwitchesValues() {
        if (-com-android-internal-telephony-DriverCall$StateSwitchesValues != null) {
            return -com-android-internal-telephony-DriverCall$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = EVENT_DTMF_DONE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = EVENT_PAUSE_DONE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = EVENT_NEXT_POST_DIAL;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = EVENT_WAKE_LOCK_TIMEOUT;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = EVENT_DTMF_DELAY_DONE;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-DriverCall$StateSwitchesValues = iArr;
        return iArr;
    }

    public GsmCdmaConnection(GsmCdmaPhone phone, DriverCall dc, GsmCdmaCallTracker ct, int index) {
        super(phone.getPhoneType());
        this.mPreciseCause = 0;
        this.mDtmfToneDelay = 0;
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mAddress = dc.number;
        this.mDialAddress = this.mAddress;
        this.mIsIncoming = dc.isMT;
        this.mCreateTime = System.currentTimeMillis();
        this.mCnapName = dc.name;
        this.mCnapNamePresentation = dc.namePresentation;
        this.mNumberPresentation = dc.numberPresentation;
        this.mUusInfo = dc.uusInfo;
        this.mIndex = index;
        this.mParent = parentFromDCState(dc.state);
        this.mParent.attach(this, dc);
        fetchDtmfToneDelay(phone);
    }

    public GsmCdmaConnection(GsmCdmaPhone phone, String dialString, GsmCdmaCallTracker ct, GsmCdmaCall parent) {
        super(phone.getPhoneType());
        this.mPreciseCause = 0;
        this.mDtmfToneDelay = 0;
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        if (isPhoneTypeGsm()) {
            this.mDialString = dialString;
        } else {
            this.mDialString = dialString;
            Rlog.d(LOG_TAG, "[GsmCdmaConn] GsmCdmaConnection: dialString=" + maskDialString(dialString));
            dialString = formatDialString(dialString);
            Rlog.d(LOG_TAG, "[GsmCdmaConn] GsmCdmaConnection:formated dialString=" + maskDialString(dialString));
        }
        this.mAddress = PhoneNumberUtils.extractNetworkPortionAlt(this.mDialString);
        this.mDialAddress = PhoneNumberUtils.extractNetworkPortionAlt(dialString);
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        this.mIndex = -1;
        this.mIsIncoming = VDBG;
        this.mCnapName = null;
        this.mCnapNamePresentation = EVENT_DTMF_DONE;
        this.mNumberPresentation = EVENT_DTMF_DONE;
        this.mCreateTime = System.currentTimeMillis();
        if (parent != null) {
            this.mParent = parent;
            if (isPhoneTypeGsm()) {
                parent.attachFake(this, Call.State.DIALING);
            } else if (parent.mState == Call.State.ACTIVE) {
                parent.attachFake(this, Call.State.ACTIVE);
            } else {
                parent.attachFake(this, Call.State.DIALING);
            }
        }
        fetchDtmfToneDelay(phone);
    }

    public GsmCdmaConnection(Context context, CdmaCallWaitingNotification cw, GsmCdmaCallTracker ct, GsmCdmaCall parent) {
        super(parent.getPhone().getPhoneType());
        this.mPreciseCause = 0;
        this.mDtmfToneDelay = 0;
        createWakeLock(context);
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mAddress = cw.number;
        this.mDialAddress = this.mAddress;
        this.mNumberPresentation = cw.numberPresentation;
        this.mCnapName = cw.name;
        this.mCnapNamePresentation = cw.namePresentation;
        this.mIndex = -1;
        this.mIsIncoming = DBG;
        this.mCreateTime = System.currentTimeMillis();
        this.mConnectTime = 0;
        this.mParent = parent;
        parent.attachFake(this, Call.State.WAITING);
    }

    public boolean isNewConnection(CdmaCallWaitingNotification cw) {
        return (cw == null || this.mAddress == null || this.mAddress.equals(cw.number)) ? VDBG : DBG;
    }

    public void dispose() {
        clearPostDialListeners();
        releaseAllWakeLocks();
    }

    static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null ? DBG : VDBG;
        } else {
            return a.equals(b);
        }
    }

    public static String formatDialString(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int length = phoneNumber.length();
        StringBuilder ret = new StringBuilder();
        int currIndex = 0;
        while (currIndex < length) {
            char c = phoneNumber.charAt(currIndex);
            if (!isPause(c) && !isWait(c)) {
                ret.append(c);
            } else if (currIndex < length - 1) {
                int nextIndex = findNextPCharOrNonPOrNonWCharIndex(phoneNumber, currIndex);
                if (nextIndex < length) {
                    ret.append(findPOrWCharToAppend(phoneNumber, currIndex, nextIndex));
                    if (nextIndex > currIndex + EVENT_DTMF_DONE) {
                        currIndex = nextIndex - 1;
                    }
                } else if (nextIndex == length) {
                    currIndex = length - 1;
                }
            }
            currIndex += EVENT_DTMF_DONE;
        }
        return PhoneNumberUtils.cdmaCheckAndProcessPlusCode(ret.toString());
    }

    boolean compareTo(DriverCall c) {
        if (!(!this.mIsIncoming ? c.isMT : DBG)) {
            return DBG;
        }
        if (isPhoneTypeGsm() && this.mOrigConnection != null) {
            return DBG;
        }
        return this.mIsIncoming == c.isMT ? equalsHandlesNulls(this.mAddress, PhoneNumberUtils.stringFromStringAndTOA(c.number, c.TOA)) : VDBG;
    }

    public String getOrigDialString() {
        return this.mDialString;
    }

    public GsmCdmaCall getCall() {
        return this.mParent;
    }

    public long getDisconnectTime() {
        return this.mDisconnectTime;
    }

    public long getHoldDurationMillis() {
        if (getState() != Call.State.HOLDING) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mHoldingStartTime;
    }

    public Call.State getState() {
        if (this.mDisconnected) {
            return Call.State.DISCONNECTED;
        }
        return super.getState();
    }

    public void hangup() throws CallStateException {
        if (this.mDisconnected) {
            throw new CallStateException("disconnected");
        }
        this.mOwner.hangup(this);
    }

    public void separate() throws CallStateException {
        if (this.mDisconnected) {
            throw new CallStateException("disconnected");
        }
        this.mOwner.separate(this);
    }

    public void proceedAfterWaitChar() {
        if (this.mPostDialState != PostDialState.WAIT) {
            Rlog.w(LOG_TAG, "GsmCdmaConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WAIT but was " + this.mPostDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        processNextPostDialChar();
    }

    public void proceedAfterWildChar(String str) {
        if (this.mPostDialState != PostDialState.WILD) {
            Rlog.w(LOG_TAG, "GsmCdmaConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WILD but was " + this.mPostDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        StringBuilder buf = new StringBuilder(str);
        buf.append(this.mPostDialString.substring(this.mNextPostDialChar));
        this.mPostDialString = buf.toString();
        this.mNextPostDialChar = 0;
        log("proceedAfterWildChar: new postDialString is " + this.mPostDialString);
        processNextPostDialChar();
    }

    public void cancelPostDial() {
        setPostDialState(PostDialState.CANCELLED);
    }

    void onHangupLocal() {
        this.mCause = EVENT_NEXT_POST_DIAL;
        this.mPreciseCause = 0;
        this.mVendorCause = null;
    }

    int disconnectCauseFromCode(int causeCode) {
        SystemProperties.set("persist.radio.disconnectCode", String.valueOf(causeCode));
        switch (causeCode) {
            case EVENT_DTMF_DONE /*1*/:
                return 25;
            case EVENT_NEXT_POST_DIAL /*3*/:
                return 95;
            case CharacterSets.ISO_8859_3 /*6*/:
                return 55;
            case CharacterSets.ISO_8859_5 /*8*/:
                return 96;
            case PduHeaders.MMS_VERSION_1_1 /*17*/:
                return EVENT_WAKE_LOCK_TIMEOUT;
            case PduHeaders.MMS_VERSION_1_2 /*18*/:
                return 97;
            case PduHeaders.MMS_VERSION_1_3 /*19*/:
                return 52;
            case SmsHeader.ELT_ID_REUSED_EXTENDED_OBJECT /*21*/:
                return 56;
            case CallFailCause.NUMBER_CHANGED /*22*/:
                return 57;
            case SmsHeader.ELT_ID_CHARACTER_SIZE_WVG_OBJECT /*25*/:
                return 58;
            case CallFailCause.CALL_FAIL_DESTINATION_OUT_OF_ORDER /*27*/:
                return 53;
            case CallFailCause.INVALID_NUMBER /*28*/:
                return 7;
            case CallFailCause.FACILITY_REJECTED /*29*/:
                return 59;
            case CallFailCause.STATUS_ENQUIRY /*30*/:
                return 60;
            case CallFailCause.NORMAL_UNSPECIFIED /*31*/:
                return 61;
            case CallFailCause.NO_CIRCUIT_AVAIL /*34*/:
                return 94;
            case RadioNVItems.RIL_NV_MIP_PROFILE_HA_SPI /*38*/:
                return 62;
            case CallFailCause.TEMPORARY_FAILURE /*41*/:
                return 63;
            case CallFailCause.SWITCHING_CONGESTION /*42*/:
                return 64;
            case CallFailCause.ACCESS_INFORMATION_DISCARDED /*43*/:
                return 65;
            case CallFailCause.CHANNEL_NOT_AVAIL /*44*/:
                return 66;
            case WspTypeDecoder.PARAMETER_ID_X_WAP_APPLICATION_ID /*47*/:
                return 67;
            case CallFailCause.QOS_NOT_AVAIL /*49*/:
                return 68;
            case SmsCbConstants.MESSAGE_ID_GSMA_ALLOCATED_CHANNEL_50 /*50*/:
                return 69;
            case RadioNVItems.RIL_NV_CDMA_SO73_COP0 /*55*/:
                return 70;
            case RadioNVItems.RIL_NV_CDMA_1X_ADVANCED_ENABLED /*57*/:
                return 54;
            case CallFailCause.BEARER_NOT_AVAIL /*58*/:
                return 71;
            case SignalToneUtil.IS95_CONST_IR_SIG_TONE_NO_TONE /*63*/:
                return 72;
            case HwRadarUtils.RADAR_LEVEL_A /*65*/:
                return 73;
            case HwRadarUtils.RADAR_LEVEL_D /*68*/:
                return 15;
            case CallFailCause.REQUESTED_FACILITY_NOT_IMPLEMENTED /*69*/:
                return 74;
            case CallFailCause.ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE /*70*/:
                return 75;
            case RadioNVItems.RIL_NV_LTE_HIDDEN_BAND_PRIORITY_41 /*79*/:
                return 76;
            case RadioNVItems.RIL_NV_LTE_BSR_TIMER /*81*/:
                return 77;
            case CallFailCause.USER_NOT_MEMBER_OF_CUG /*87*/:
                return 78;
            case CallFailCause.INCOMPATIBLE_DESTINATION /*88*/:
                return 79;
            case CallFailCause.INVALID_TRANSIT_NW_SELECTION /*91*/:
                return 80;
            case CallFailCause.SEMANTICALLY_INCORRECT_MESSAGE /*95*/:
                return 81;
            case CallFailCause.INVALID_MANDATORY_INFORMATION /*96*/:
                return 82;
            case CallFailCause.MESSAGE_TYPE_NON_IMPLEMENTED /*97*/:
                return 83;
            case CallFailCause.MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE /*98*/:
                return 84;
            case CallFailCause.INFORMATION_ELEMENT_NON_EXISTENT /*99*/:
                return 85;
            case IccRecords.EVENT_GET_ICC_RECORD_DONE /*100*/:
                return 86;
            case CallFailCause.MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE /*101*/:
                return 87;
            case CallFailCause.RECOVERY_ON_TIMER_EXPIRED /*102*/:
                return 88;
            case CallFailCause.PROTOCOL_ERROR_UNSPECIFIED /*111*/:
                return 89;
            case CallFailCause.INTERWORKING_UNSPECIFIED /*127*/:
                return 90;
            case CallFailCause.CALL_BARRED /*240*/:
                return 20;
            case CallFailCause.FDN_BLOCKED /*241*/:
                return 21;
            case BearerData.RELATIVE_TIME_WEEKS_LIMIT /*244*/:
                return 46;
            case BearerData.RELATIVE_TIME_INDEFINITE /*245*/:
                return 47;
            case BearerData.RELATIVE_TIME_NOW /*246*/:
                return 48;
            case CallFailCause.EMERGENCY_TEMP_FAILURE /*325*/:
                return 92;
            case CallFailCause.EMERGENCY_PERM_FAILURE /*326*/:
                return 93;
            case CharacterSets.UCS2 /*1000*/:
                return 26;
            case TelephonyEventLog.TAG_RIL_REQUEST /*1001*/:
                return 27;
            case TelephonyEventLog.TAG_RIL_RESPONSE /*1002*/:
                return 28;
            case TelephonyEventLog.TAG_RIL_UNSOL_RESPONSE /*1003*/:
                return 29;
            case TelephonyEventLog.TAG_RIL_TIMEOUT_RESPONSE /*1004*/:
                return 30;
            case ServiceStateTracker.CS_NORMAL_ENABLED /*1005*/:
                return 31;
            case ServiceStateTracker.CS_EMERGENCY_ENABLED /*1006*/:
                return 32;
            case CallFailCause.CDMA_PREEMPTED /*1007*/:
                return 33;
            case CallFailCause.CDMA_NOT_EMERGENCY /*1008*/:
                return 34;
            case CallFailCause.CDMA_ACCESS_BLOCKED /*1009*/:
                return 35;
            default:
                AppState uiccAppState;
                GsmCdmaPhone phone = this.mOwner.getPhone();
                int serviceState = phone.getServiceState().getState();
                UiccCardApplication cardApp = phone.getUiccCardApplication();
                if (cardApp != null) {
                    uiccAppState = cardApp.getState();
                } else {
                    uiccAppState = AppState.APPSTATE_UNKNOWN;
                }
                if (serviceState == EVENT_NEXT_POST_DIAL) {
                    return 17;
                }
                if (serviceState == EVENT_DTMF_DONE || serviceState == EVENT_PAUSE_DONE) {
                    if (PhoneNumberUtils.isEmergencyNumber(this.mAddress)) {
                        return EVENT_PAUSE_DONE;
                    }
                    return 18;
                } else if (isPhoneTypeGsm()) {
                    if (uiccAppState != AppState.APPSTATE_READY) {
                        return 19;
                    }
                    return (causeCode != CallFailCause.ERROR_UNSPECIFIED && causeCode == 16) ? EVENT_PAUSE_DONE : 36;
                } else if (phone.mCdmaSubscriptionSource != 0 || uiccAppState == AppState.APPSTATE_READY) {
                    return causeCode == 16 ? EVENT_PAUSE_DONE : 36;
                } else {
                    return 19;
                }
        }
    }

    void onRemoteDisconnect(int causeCode, String vendorCause) {
        this.mPreciseCause = causeCode;
        this.mVendorCause = vendorCause;
        int cause = disconnectCauseFromCode(causeCode);
        if (this.mOwner.isInCsRedial()) {
            Rlog.d(LOG_TAG, "onRemoteDisconnect, CS redial.");
            cause = 100;
        }
        onDisconnect(cause);
    }

    public boolean onDisconnect(int cause) {
        boolean z = VDBG;
        this.mCause = cause;
        if (!this.mDisconnected) {
            doDisconnect();
            Rlog.d(LOG_TAG, "onDisconnect: cause=" + cause);
            this.mOwner.getPhone().notifyDisconnect(this);
            if (this.mParent != null) {
                z = this.mParent.connectionDisconnected(this);
            }
            this.mOrigConnection = null;
        }
        clearPostDialListeners();
        this.mOwner.cleanRilRecovery();
        releaseWakeLock();
        return z;
    }

    void onLocalDisconnect() {
        if (!this.mDisconnected) {
            doDisconnect();
            if (this.mParent != null) {
                this.mParent.detach(this);
            }
        }
        releaseWakeLock();
    }

    public boolean update(DriverCall dc) {
        boolean z;
        boolean changed = VDBG;
        boolean wasConnectingInOrOut = isConnectingInOrOut();
        boolean wasHolding = getState() == Call.State.HOLDING ? DBG : VDBG;
        boolean showConnectedNum = SystemProperties.getBoolean("ro.config.hw_mo_num_update", VDBG);
        GsmCdmaCall newParent = parentFromDCState(dc.state);
        log("parent= " + this.mParent + ", newParent= " + newParent);
        if (!isPhoneTypeGsm() || this.mOrigConnection == null || (showConnectedNum && !this.mIsIncoming)) {
            log(" mNumberConverted " + this.mNumberConverted);
            if (!equalsHandlesNulls(this.mAddress, dc.number) && (!(this.mNumberConverted && equalsHandlesNulls(this.mConvertedNumber, dc.number)) && (showConnectedNum || this.mIsIncoming))) {
                log("update: phone # changed!");
                this.mAddress = TextUtils.equals(this.mDialAddress, dc.number) ? this.mAddress : dc.number;
                this.mDialAddress = TextUtils.isEmpty(this.mDialAddress) ? dc.number : this.mDialAddress;
                changed = DBG;
            }
        } else {
            log("update: mOrigConnection is not null");
        }
        if (TextUtils.isEmpty(dc.name)) {
            if (!TextUtils.isEmpty(this.mCnapName)) {
                changed = DBG;
                this.mCnapName = "";
            }
        } else if (!dc.name.equals(this.mCnapName)) {
            changed = DBG;
            this.mCnapName = dc.name;
        }
        log("--dssds----" + this.mCnapName);
        this.mCnapNamePresentation = dc.namePresentation;
        this.mNumberPresentation = dc.numberPresentation;
        if (newParent != this.mParent) {
            if (this.mParent != null) {
                this.mParent.detach(this);
            }
            newParent.attach(this, dc);
            this.mParent = newParent;
            changed = DBG;
        } else {
            changed = !changed ? this.mParent.update(this, dc) : DBG;
        }
        StringBuilder append = new StringBuilder().append("update: parent=").append(this.mParent).append(", hasNewParent=");
        if (newParent != this.mParent) {
            z = DBG;
        } else {
            z = VDBG;
        }
        log(append.append(z).append(", wasConnectingInOrOut=").append(wasConnectingInOrOut).append(", wasHolding=").append(wasHolding).append(", isConnectingInOrOut=").append(isConnectingInOrOut()).append(", changed=").append(changed).toString());
        if (wasConnectingInOrOut && !isConnectingInOrOut()) {
            onConnectedInOrOut();
        }
        if (changed && !wasHolding && getState() == Call.State.HOLDING) {
            onStartedHolding();
        }
        return changed;
    }

    void fakeHoldBeforeDial() {
        if (this.mParent != null) {
            this.mParent.detach(this);
        }
        this.mParent = this.mOwner.mBackgroundCall;
        this.mParent.attachFake(this, Call.State.HOLDING);
        onStartedHolding();
    }

    int getGsmCdmaIndex() throws CallStateException {
        if (this.mIndex >= 0) {
            return this.mIndex + EVENT_DTMF_DONE;
        }
        throw new CallStateException("GsmCdma index not yet assigned");
    }

    void onConnectedInOrOut() {
        this.mConnectTime = System.currentTimeMillis();
        this.mConnectTimeReal = SystemClock.elapsedRealtime();
        this.mDuration = 0;
        log("onConnectedInOrOut: connectTime=" + this.mConnectTime);
        if (this.mIsIncoming) {
            releaseWakeLock();
        } else {
            processNextPostDialChar();
        }
    }

    private void doDisconnect() {
        this.mIndex = -1;
        this.mDisconnectTime = System.currentTimeMillis();
        this.mDuration = SystemClock.elapsedRealtime() - this.mConnectTimeReal;
        this.mDisconnected = DBG;
        clearPostDialListeners();
    }

    void onStartedHolding() {
        this.mHoldingStartTime = SystemClock.elapsedRealtime();
    }

    private boolean processPostDialChar(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            if (isPhoneTypeGsm()) {
                this.mOwner.mCi.sendDtmf(c, this.mHandler.obtainMessage(EVENT_DTMF_DONE));
            } else {
                this.mOwner.mCi.sendBurstDtmf(Character.toString(c), 0, 0, this.mHandler.obtainMessage(EVENT_DTMF_DONE));
            }
        } else if (isPause(c)) {
            if (!isPhoneTypeGsm()) {
                setPostDialState(PostDialState.PAUSE);
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_PAUSE_DONE), (long) (isPhoneTypeGsm() ? PAUSE_DELAY_MILLIS_GSM : PAUSE_DELAY_MILLIS_CDMA));
        } else if (isWait(c)) {
            setPostDialState(PostDialState.WAIT);
        } else if (!isWild(c)) {
            return VDBG;
        } else {
            setPostDialState(PostDialState.WILD);
        }
        return DBG;
    }

    public String getRemainingPostDialString() {
        String subStr = super.getRemainingPostDialString();
        if (isPhoneTypeGsm() || TextUtils.isEmpty(subStr)) {
            return subStr;
        }
        int wIndex = subStr.indexOf(59);
        int pIndex = subStr.indexOf(44);
        if (wIndex > 0 && (wIndex < pIndex || pIndex <= 0)) {
            return subStr.substring(0, wIndex);
        }
        if (pIndex > 0) {
            return subStr.substring(0, pIndex);
        }
        return subStr;
    }

    public void updateParent(GsmCdmaCall oldParent, GsmCdmaCall newParent) {
        if (newParent != oldParent) {
            if (oldParent != null) {
                oldParent.detach(this);
            }
            newParent.attachFake(this, Call.State.ACTIVE);
            this.mParent = newParent;
        }
    }

    protected void finalize() {
        if (this.mPartialWakeLock.isHeld()) {
            Rlog.e(LOG_TAG, "[GsmCdmaConn] UNEXPECTED; mPartialWakeLock is held when finalizing.");
        }
        clearPostDialListeners();
        releaseWakeLock();
    }

    private void processNextPostDialChar() {
        if (this.mPostDialState == PostDialState.CANCELLED) {
            releaseWakeLock();
            return;
        }
        char c;
        if (this.mPostDialString == null || this.mPostDialString.length() <= this.mNextPostDialChar) {
            setPostDialState(PostDialState.COMPLETE);
            releaseWakeLock();
            c = '\u0000';
        } else {
            setPostDialState(PostDialState.STARTED);
            String str = this.mPostDialString;
            int i = this.mNextPostDialChar;
            this.mNextPostDialChar = i + EVENT_DTMF_DONE;
            c = str.charAt(i);
            if (!processPostDialChar(c)) {
                this.mHandler.obtainMessage(EVENT_NEXT_POST_DIAL).sendToTarget();
                Rlog.e(LOG_TAG, "processNextPostDialChar: c=" + c + " isn't valid!");
                return;
            }
        }
        notifyPostDialListenersNextChar(c);
        Registrant postDialHandler = this.mOwner.getPhone().getPostDialHandler();
        if (postDialHandler != null) {
            Message notifyMessage = postDialHandler.messageForRegistrant();
            if (notifyMessage != null) {
                PostDialState state = this.mPostDialState;
                AsyncResult ar = AsyncResult.forMessage(notifyMessage);
                ar.result = this;
                ar.userObj = state;
                notifyMessage.arg1 = c;
                notifyMessage.sendToTarget();
            }
        }
    }

    private boolean isConnectingInOrOut() {
        if (this.mParent == null || this.mParent == this.mOwner.mRingingCall || this.mParent.mState == Call.State.DIALING || this.mParent.mState == Call.State.ALERTING) {
            return DBG;
        }
        return VDBG;
    }

    private GsmCdmaCall parentFromDCState(State state) {
        switch (-getcom-android-internal-telephony-DriverCall$StateSwitchesValues()[state.ordinal()]) {
            case EVENT_DTMF_DONE /*1*/:
            case EVENT_PAUSE_DONE /*2*/:
            case EVENT_NEXT_POST_DIAL /*3*/:
                return this.mOwner.mForegroundCall;
            case EVENT_WAKE_LOCK_TIMEOUT /*4*/:
                return this.mOwner.mBackgroundCall;
            case EVENT_DTMF_DELAY_DONE /*5*/:
            case CharacterSets.ISO_8859_3 /*6*/:
                return this.mOwner.mRingingCall;
            default:
                throw new RuntimeException("illegal call state: " + state);
        }
    }

    private void setPostDialState(PostDialState s) {
        if (s == PostDialState.STARTED || s == PostDialState.PAUSE) {
            synchronized (this.mPartialWakeLock) {
                if (this.mPartialWakeLock.isHeld()) {
                    this.mHandler.removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
                } else {
                    acquireWakeLock();
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_WAKE_LOCK_TIMEOUT), 60000);
            }
        } else {
            this.mHandler.removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
            releaseWakeLock();
        }
        this.mPostDialState = s;
        notifyPostDialListeners();
    }

    private void createWakeLock(Context context) {
        this.mPartialWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(EVENT_DTMF_DONE, LOG_TAG);
    }

    private void acquireWakeLock() {
        log("acquireWakeLock");
        this.mPartialWakeLock.acquire();
    }

    private void releaseWakeLock() {
        synchronized (this.mPartialWakeLock) {
            if (this.mPartialWakeLock.isHeld()) {
                log("releaseWakeLock");
                this.mPartialWakeLock.release();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void releaseAllWakeLocks() {
        synchronized (this.mPartialWakeLock) {
            while (true) {
                if (this.mPartialWakeLock.isHeld()) {
                    this.mPartialWakeLock.release();
                }
            }
        }
    }

    private static boolean isPause(char c) {
        return c == ',' ? DBG : VDBG;
    }

    private static boolean isWait(char c) {
        return c == ';' ? DBG : VDBG;
    }

    private static boolean isWild(char c) {
        return c == 'N' ? DBG : VDBG;
    }

    private static int findNextPCharOrNonPOrNonWCharIndex(String phoneNumber, int currIndex) {
        boolean wMatched = isWait(phoneNumber.charAt(currIndex));
        int index = currIndex + EVENT_DTMF_DONE;
        int length = phoneNumber.length();
        while (index < length) {
            char cNext = phoneNumber.charAt(index);
            if (isWait(cNext)) {
                wMatched = DBG;
            }
            if (!isWait(cNext) && !isPause(cNext)) {
                break;
            }
            index += EVENT_DTMF_DONE;
        }
        if (index >= length || index <= currIndex + EVENT_DTMF_DONE || r3 || !isPause(phoneNumber.charAt(currIndex))) {
            return index;
        }
        return currIndex + EVENT_DTMF_DONE;
    }

    private static char findPOrWCharToAppend(String phoneNumber, int currPwIndex, int nextNonPwCharIndex) {
        char ret = isPause(phoneNumber.charAt(currPwIndex)) ? ',' : ';';
        if (nextNonPwCharIndex > currPwIndex + EVENT_DTMF_DONE) {
            return ';';
        }
        return ret;
    }

    private String maskDialString(String dialString) {
        return "<MASKED>";
    }

    private void fetchDtmfToneDelay(GsmCdmaPhone phone) {
        PersistableBundle b = ((CarrierConfigManager) phone.getContext().getSystemService("carrier_config")).getConfigForSubId(phone.getSubId());
        if (b != null) {
            this.mDtmfToneDelay = b.getInt(phone.getDtmfToneDelayKey());
        }
    }

    private boolean isPhoneTypeGsm() {
        return this.mOwner.getPhone().getPhoneType() == EVENT_DTMF_DONE ? DBG : VDBG;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, "[GsmCdmaConn] " + msg);
    }

    public int getNumberPresentation() {
        return this.mNumberPresentation;
    }

    public UUSInfo getUUSInfo() {
        return this.mUusInfo;
    }

    public int getPreciseDisconnectCause() {
        return this.mPreciseCause;
    }

    public String getVendorDisconnectCause() {
        return this.mVendorCause;
    }

    public void migrateFrom(Connection c) {
        if (c != null) {
            super.migrateFrom(c);
            this.mUusInfo = c.getUUSInfo();
            setUserData(c.getUserData());
        }
    }

    public Connection getOrigConnection() {
        return this.mOrigConnection;
    }

    public boolean isMultiparty() {
        if (this.mOrigConnection != null) {
            return this.mOrigConnection.isMultiparty();
        }
        return VDBG;
    }

    public void setPostDialString(String postDialString) {
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(postDialString);
    }
}

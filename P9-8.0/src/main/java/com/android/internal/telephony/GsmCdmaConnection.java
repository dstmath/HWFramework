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
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.UiccCardApplication;

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
    static final String PROP_ENABLE_TEST_CLEARCODE = "persist.enable.test.clearcode";
    static final String PROP_FOR_TEST_CLEARCODE = "persist.radio.test.clearcode";
    private static final boolean VDBG = false;
    static final int WAKE_LOCK_TIMEOUT_MILLIS = 60000;
    long mDisconnectTime;
    boolean mDisconnected;
    private int mDtmfToneDelay = 0;
    Handler mHandler;
    int mIndex;
    private boolean mIsEmergencyCall = false;
    GsmCdmaCallTracker mOwner;
    GsmCdmaCall mParent;
    private WakeLock mPartialWakeLock;
    int mPreciseCause = 0;
    UUSInfo mUusInfo;
    String mVendorCause;

    class MyHandler extends Handler {
        MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    GsmCdmaConnection.this.mHandler.sendMessageDelayed(GsmCdmaConnection.this.mHandler.obtainMessage(5), (long) GsmCdmaConnection.this.mDtmfToneDelay);
                    return;
                case 2:
                case 3:
                case 5:
                    GsmCdmaConnection.this.processNextPostDialChar();
                    return;
                case 4:
                    GsmCdmaConnection.this.releaseWakeLock();
                    return;
                default:
                    return;
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-DriverCall$StateSwitchesValues() {
        if (-com-android-internal-telephony-DriverCall$StateSwitchesValues != null) {
            return -com-android-internal-telephony-DriverCall$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 5;
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
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mAddress = dc.number;
        this.mIsEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(phone.getContext(), this.mAddress);
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

    public GsmCdmaConnection(GsmCdmaPhone phone, String dialString, GsmCdmaCallTracker ct, GsmCdmaCall parent, boolean isEmergencyCall) {
        super(phone.getPhoneType());
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
        this.mIsEmergencyCall = isEmergencyCall;
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        this.mIndex = -1;
        this.mIsIncoming = false;
        this.mCnapName = null;
        this.mCnapNamePresentation = 1;
        this.mNumberPresentation = 1;
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
        this.mIsIncoming = true;
        this.mCreateTime = System.currentTimeMillis();
        this.mConnectTime = 0;
        this.mParent = parent;
        parent.attachFake(this, Call.State.WAITING);
    }

    public boolean isNewConnection(CdmaCallWaitingNotification cw) {
        return (cw == null || this.mAddress == null) ? false : this.mAddress.equals(cw.number) ^ 1;
    }

    public void dispose() {
        clearPostDialListeners();
        if (this.mParent != null) {
            this.mParent.detach(this);
        }
        releaseAllWakeLocks();
    }

    static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
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
                    if (nextIndex > currIndex + 1) {
                        currIndex = nextIndex - 1;
                    }
                } else if (nextIndex == length) {
                    currIndex = length - 1;
                }
            }
            currIndex++;
        }
        return PhoneNumberUtils.cdmaCheckAndProcessPlusCode(ret.toString());
    }

    boolean compareTo(DriverCall c) {
        if (!(!this.mIsIncoming ? c.isMT : true)) {
            return true;
        }
        if (isPhoneTypeGsm() && this.mOrigConnection != null) {
            return true;
        }
        return this.mIsIncoming == c.isMT ? equalsHandlesNulls(this.mAddress, PhoneNumberUtils.stringFromStringAndTOA(c.number, c.TOA)) : false;
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
        this.mCause = 3;
        this.mPreciseCause = 0;
        this.mVendorCause = null;
    }

    int disconnectCauseFromCode(int causeCode) {
        if (SystemProperties.getBoolean(PROP_ENABLE_TEST_CLEARCODE, false)) {
            String testClearCode = SystemProperties.get(PROP_FOR_TEST_CLEARCODE, "-1");
            log("disconnectCauseFromCode: real ClearCode = " + causeCode + ", testClearCode = " + testClearCode);
            try {
                causeCode = Integer.parseInt(testClearCode);
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "disconnectCauseFromCode: NumberFormatException for testClearCode:" + testClearCode + "," + e);
            }
        }
        SystemProperties.set("persist.radio.disconnectCode", String.valueOf(causeCode));
        switch (causeCode) {
            case 1:
                return 25;
            case 3:
                return 103;
            case 6:
                return 64;
            case 8:
                return AbstractPhoneBase.EVENT_ECC_NUM;
            case 17:
                return 4;
            case 18:
                return AbstractPhoneBase.EVENT_GET_IMSI_DONE;
            case 19:
                return 61;
            case 21:
                return 65;
            case 22:
                return 66;
            case 25:
                return 67;
            case 27:
                return 62;
            case 28:
                return 7;
            case 29:
                return 68;
            case 30:
                return 69;
            case 31:
                return 70;
            case 34:
                return 102;
            case 38:
                return 71;
            case 41:
                return 72;
            case 42:
                return 73;
            case 43:
                return 74;
            case 44:
                return 75;
            case 47:
                return 76;
            case 49:
                return 77;
            case 50:
                return 78;
            case 55:
                return 79;
            case 57:
                return 63;
            case 58:
                return 80;
            case 63:
                return 81;
            case 65:
                return 82;
            case 68:
                return 15;
            case 69:
                return 83;
            case 70:
                return 84;
            case 79:
                return 85;
            case 81:
                return 86;
            case 87:
                return 87;
            case 88:
                return 88;
            case 91:
                return 89;
            case 95:
                return 90;
            case 96:
                return 91;
            case 97:
                return 92;
            case 98:
                return 93;
            case 99:
                return 94;
            case 100:
                return 95;
            case 101:
                return 96;
            case 102:
                return 97;
            case 111:
                return 98;
            case 127:
                return 99;
            case 240:
                return 20;
            case 241:
                return 21;
            case 243:
                return 58;
            case 244:
                return 46;
            case 245:
                return 47;
            case 246:
                return 48;
            case 325:
                return 100;
            case 326:
                return 101;
            case 1000:
                return 26;
            case 1001:
                return 27;
            case 1002:
                return 28;
            case 1003:
                return 29;
            case 1004:
                return 30;
            case 1005:
                return 31;
            case 1006:
                return 32;
            case 1007:
                return 33;
            case 1008:
                return 34;
            case 1009:
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
                if (serviceState == 3) {
                    return 17;
                }
                if (!this.mIsEmergencyCall) {
                    if (serviceState == 1 || serviceState == 2) {
                        if (PhoneNumberUtils.isEmergencyNumber(this.mAddress)) {
                            return 2;
                        }
                        return 18;
                    } else if (uiccAppState != AppState.APPSTATE_READY && (isPhoneTypeGsm() || phone.mCdmaSubscriptionSource == 0)) {
                        return 19;
                    }
                }
                if (isPhoneTypeGsm()) {
                }
                if (causeCode == 16) {
                    return 2;
                }
                return 36;
        }
    }

    void onRemoteDisconnect(int causeCode, String vendorCause) {
        this.mPreciseCause = causeCode;
        this.mVendorCause = vendorCause;
        int cause = disconnectCauseFromCode(causeCode);
        if (this.mOwner.isInCsRedial()) {
            Rlog.d(LOG_TAG, "onRemoteDisconnect, CS redial.");
            cause = 500;
        }
        onDisconnect(cause);
    }

    public boolean onDisconnect(int cause) {
        boolean changed = false;
        this.mCause = cause;
        if (!this.mDisconnected) {
            doDisconnect();
            Rlog.d(LOG_TAG, "onDisconnect: cause=" + cause);
            this.mOwner.getPhone().notifyDisconnect(this);
            if (this.mParent != null) {
                changed = this.mParent.connectionDisconnected(this);
            }
            this.mOrigConnection = null;
        }
        clearPostDialListeners();
        this.mOwner.cleanRilRecovery();
        releaseWakeLock();
        return changed;
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
        boolean changed = false;
        boolean wasConnectingInOrOut = isConnectingInOrOut();
        boolean wasHolding = getState() == Call.State.HOLDING;
        boolean showConnectedNum = SystemProperties.getBoolean("ro.config.hw_mo_num_update", false);
        GsmCdmaCall newParent = parentFromDCState(dc.state);
        log("parent= " + this.mParent + ", newParent= " + newParent);
        if (!isPhoneTypeGsm() || this.mOrigConnection == null || (showConnectedNum && !this.mIsIncoming)) {
            log(" mNumberConverted " + this.mNumberConverted);
            if (!equalsHandlesNulls(this.mAddress, dc.number) && (!(this.mNumberConverted && (equalsHandlesNulls(this.mConvertedNumber, dc.number) ^ 1) == 0) && (showConnectedNum || this.mIsIncoming))) {
                log("update: phone # changed!");
                this.mAddress = TextUtils.equals(this.mDialAddress, dc.number) ? this.mAddress : dc.number;
                this.mDialAddress = TextUtils.isEmpty(this.mDialAddress) ? dc.number : this.mDialAddress;
                changed = true;
            }
        } else {
            log("update: mOrigConnection is not null");
        }
        if (TextUtils.isEmpty(dc.name)) {
            if (!TextUtils.isEmpty(this.mCnapName)) {
                changed = true;
                this.mCnapName = "";
            }
        } else if (!dc.name.equals(this.mCnapName)) {
            changed = true;
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
            changed = true;
        } else {
            changed = !changed ? this.mParent.update(this, dc) : true;
        }
        StringBuilder append = new StringBuilder().append("update: parent=").append(this.mParent).append(", hasNewParent=");
        if (newParent != this.mParent) {
            z = true;
        } else {
            z = false;
        }
        log(append.append(z).append(", wasConnectingInOrOut=").append(wasConnectingInOrOut).append(", wasHolding=").append(wasHolding).append(", isConnectingInOrOut=").append(isConnectingInOrOut()).append(", changed=").append(changed).toString());
        if (wasConnectingInOrOut && (isConnectingInOrOut() ^ 1) != 0) {
            onConnectedInOrOut();
        }
        if (changed && (wasHolding ^ 1) != 0 && getState() == Call.State.HOLDING) {
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
            return this.mIndex + 1;
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
        this.mDisconnected = true;
        clearPostDialListeners();
    }

    void onStartedHolding() {
        this.mHoldingStartTime = SystemClock.elapsedRealtime();
    }

    private boolean processPostDialChar(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            if (isPhoneTypeGsm()) {
                this.mOwner.mCi.sendDtmf(c, this.mHandler.obtainMessage(1));
            } else {
                this.mOwner.mCi.sendBurstDtmf(Character.toString(c), 0, 0, this.mHandler.obtainMessage(1));
            }
        } else if (isPause(c)) {
            if (!isPhoneTypeGsm()) {
                setPostDialState(PostDialState.PAUSE);
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), (long) (isPhoneTypeGsm() ? PAUSE_DELAY_MILLIS_GSM : 2000));
        } else if (isWait(c)) {
            setPostDialState(PostDialState.WAIT);
        } else if (!isWild(c)) {
            return false;
        } else {
            setPostDialState(PostDialState.WILD);
        }
        return true;
    }

    public String getRemainingPostDialString() {
        String subStr = super.getRemainingPostDialString();
        if (isPhoneTypeGsm() || (TextUtils.isEmpty(subStr) ^ 1) == 0) {
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
        if (this.mPartialWakeLock != null && this.mPartialWakeLock.isHeld()) {
            Rlog.e(LOG_TAG, "UNEXPECTED; mPartialWakeLock is held when finalizing.");
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
            c = 0;
        } else {
            setPostDialState(PostDialState.STARTED);
            String str = this.mPostDialString;
            int i = this.mNextPostDialChar;
            this.mNextPostDialChar = i + 1;
            c = str.charAt(i);
            if (!processPostDialChar(c)) {
                this.mHandler.obtainMessage(3).sendToTarget();
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
            return true;
        }
        return false;
    }

    private GsmCdmaCall parentFromDCState(State state) {
        switch (-getcom-android-internal-telephony-DriverCall$StateSwitchesValues()[state.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return this.mOwner.mForegroundCall;
            case 4:
                return this.mOwner.mBackgroundCall;
            case 5:
            case 6:
                return this.mOwner.mRingingCall;
            default:
                throw new RuntimeException("illegal call state: " + state);
        }
    }

    private void setPostDialState(PostDialState s) {
        if (s == PostDialState.STARTED || s == PostDialState.PAUSE) {
            synchronized (this.mPartialWakeLock) {
                if (this.mPartialWakeLock.isHeld()) {
                    this.mHandler.removeMessages(4);
                } else {
                    acquireWakeLock();
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 60000);
            }
        } else {
            this.mHandler.removeMessages(4);
            releaseWakeLock();
        }
        this.mPostDialState = s;
        notifyPostDialListeners();
    }

    private void createWakeLock(Context context) {
        this.mPartialWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
    }

    private void acquireWakeLock() {
        if (this.mPartialWakeLock != null) {
            synchronized (this.mPartialWakeLock) {
                log("acquireWakeLock");
                this.mPartialWakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (this.mPartialWakeLock != null) {
            synchronized (this.mPartialWakeLock) {
                if (this.mPartialWakeLock.isHeld()) {
                    log("releaseWakeLock");
                    this.mPartialWakeLock.release();
                }
            }
        }
    }

    private void releaseAllWakeLocks() {
        if (this.mPartialWakeLock != null) {
            synchronized (this.mPartialWakeLock) {
                while (this.mPartialWakeLock.isHeld()) {
                    this.mPartialWakeLock.release();
                }
            }
        }
    }

    private static boolean isPause(char c) {
        return c == ',';
    }

    private static boolean isWait(char c) {
        return c == ';';
    }

    private static boolean isWild(char c) {
        return c == 'N';
    }

    private static int findNextPCharOrNonPOrNonWCharIndex(String phoneNumber, int currIndex) {
        boolean wMatched = isWait(phoneNumber.charAt(currIndex));
        int index = currIndex + 1;
        int length = phoneNumber.length();
        while (index < length) {
            char cNext = phoneNumber.charAt(index);
            if (isWait(cNext)) {
                wMatched = true;
            }
            if (!isWait(cNext) && (isPause(cNext) ^ 1) != 0) {
                break;
            }
            index++;
        }
        if (index >= length || index <= currIndex + 1 || wMatched || !isPause(phoneNumber.charAt(currIndex))) {
            return index;
        }
        return currIndex + 1;
    }

    private static char findPOrWCharToAppend(String phoneNumber, int currPwIndex, int nextNonPwCharIndex) {
        char ret = isPause(phoneNumber.charAt(currPwIndex)) ? ',' : ';';
        if (nextNonPwCharIndex > currPwIndex + 1) {
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
        return this.mOwner.getPhone().getPhoneType() == 1;
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
        return false;
    }

    public void setPostDialString(String postDialString) {
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(postDialString);
    }
}

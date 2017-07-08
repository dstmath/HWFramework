package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hdm.HwDeviceManager;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.SmsCbConstants;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GsmCdmaCallTracker extends AbstractGsmCdmaCallTracker {
    private static final boolean DBG_POLL = false;
    private static final String LOG_TAG = "GsmCdmaCallTracker";
    private static final int MAX_CONNECTIONS_CDMA = 8;
    public static final int MAX_CONNECTIONS_GSM = 19;
    private static final int MAX_CONNECTIONS_PER_CALL_CDMA = 1;
    private static final int MAX_CONNECTIONS_PER_CALL_GSM = 5;
    private static final boolean REPEAT_POLLING = false;
    private static final boolean VDBG = false;
    boolean callSwitchPending;
    RegistrantList cdmaWaitingNumberChangedRegistrants;
    private int m3WayCallFlashDelay;
    public GsmCdmaCall mBackgroundCall;
    private RegistrantList mCallWaitingRegistrants;
    private GsmCdmaConnection[] mConnections;
    private boolean mDesiredMute;
    private ArrayList<GsmCdmaConnection> mDroppedDuringPoll;
    private BroadcastReceiver mEcmExitReceiver;
    private TelephonyEventLog mEventLog;
    public GsmCdmaCall mForegroundCall;
    private boolean mHangupPendingMO;
    private boolean mIsEcmTimerCanceled;
    private boolean mIsInCsRedial;
    private boolean mIsInEmergencyCall;
    private int mPendingCallClirMode;
    private boolean mPendingCallInEcm;
    private GsmCdmaConnection mPendingMO;
    public GsmCdmaPhone mPhone;
    public GsmCdmaCall mRingingCall;
    public State mState;
    public RegistrantList mVoiceCallEndedRegistrants;
    private RegistrantList mVoiceCallStartedRegistrants;

    public GsmCdmaCallTracker(GsmCdmaPhone phone) {
        super(phone);
        this.mVoiceCallEndedRegistrants = new RegistrantList();
        this.mVoiceCallStartedRegistrants = new RegistrantList();
        this.cdmaWaitingNumberChangedRegistrants = new RegistrantList();
        this.mDroppedDuringPoll = new ArrayList(MAX_CONNECTIONS_GSM);
        this.mRingingCall = new GsmCdmaCall(this);
        this.mForegroundCall = new GsmCdmaCall(this);
        this.mBackgroundCall = new GsmCdmaCall(this);
        this.callSwitchPending = DBG_POLL;
        this.mDesiredMute = DBG_POLL;
        this.mState = State.IDLE;
        this.mCallWaitingRegistrants = new RegistrantList();
        this.mIsInCsRedial = DBG_POLL;
        this.mEcmExitReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                    boolean isInEcm = intent.getBooleanExtra("phoneinECMState", GsmCdmaCallTracker.DBG_POLL);
                    GsmCdmaCallTracker.this.log("Received ACTION_EMERGENCY_CALLBACK_MODE_CHANGED isInEcm = " + isInEcm);
                    if (!isInEcm) {
                        List<Connection> toNotify = new ArrayList();
                        toNotify.addAll(GsmCdmaCallTracker.this.mRingingCall.getConnections());
                        toNotify.addAll(GsmCdmaCallTracker.this.mForegroundCall.getConnections());
                        toNotify.addAll(GsmCdmaCallTracker.this.mBackgroundCall.getConnections());
                        if (GsmCdmaCallTracker.this.mPendingMO != null) {
                            toNotify.add(GsmCdmaCallTracker.this.mPendingMO);
                        }
                        for (Connection connection : toNotify) {
                            if (connection != null) {
                                connection.onExitedEcmMode();
                            }
                        }
                    }
                }
            }
        };
        this.mPhone = phone;
        this.mCi = phone.mCi;
        this.mCi.registerForCallStateChanged(this, 2, null);
        this.mCi.registerForOn(this, 9, null);
        this.mCi.registerForNotAvailable(this, 10, null);
        this.mCi.registerForRSrvccStateChanged(this, 50, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mEcmExitReceiver, filter);
        updatePhoneType(true);
        this.mEventLog = new TelephonyEventLog(this.mPhone.getPhoneId());
    }

    public void updatePhoneType() {
        updatePhoneType(DBG_POLL);
    }

    private void updatePhoneType(boolean duringInit) {
        if (!duringInit) {
            reset();
            pollCallsWhenSafe();
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            this.mConnections = new GsmCdmaConnection[MAX_CONNECTIONS_GSM];
            this.mCi.unregisterForCallWaitingInfo(this);
            return;
        }
        this.mConnections = new GsmCdmaConnection[MAX_CONNECTIONS_CDMA];
        this.mPendingCallInEcm = DBG_POLL;
        this.mIsInEmergencyCall = DBG_POLL;
        this.mPendingCallClirMode = 0;
        this.mIsEcmTimerCanceled = DBG_POLL;
        this.m3WayCallFlashDelay = 0;
        this.mCi.registerForCallWaitingInfo(this, 15, null);
    }

    private void reset() {
        Rlog.d(LOG_TAG, "reset");
        clearDisconnected();
        GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
        int length = gsmCdmaConnectionArr.length;
        for (int i = 0; i < length; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            GsmCdmaConnection gsmCdmaConnection = gsmCdmaConnectionArr[i];
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.dispose();
            }
        }
    }

    protected void finalize() {
        Rlog.d(LOG_TAG, "GsmCdmaCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceCallStartedRegistrants.add(r);
        if (this.mState != State.IDLE) {
            r.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCallWaitingRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCallWaitingRegistrants.remove(h);
    }

    public void registerForCdmaWaitingNumberChanged(Handler h, int what, Object obj) {
        Rlog.i(LOG_TAG, "registerForCdmaWaitingNumberChanged!");
        this.cdmaWaitingNumberChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCdmaWaitingNumberChanged(Handler h) {
        Rlog.i(LOG_TAG, "unregisterForCdmaWaitingNumberChanged!");
        this.cdmaWaitingNumberChangedRegistrants.remove(h);
    }

    private void fakeHoldForegroundBeforeDial() {
        List<Connection> connCopy = (List) this.mForegroundCall.mConnections.clone();
        int s = connCopy.size();
        for (int i = 0; i < s; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            ((GsmCdmaConnection) connCopy.get(i)).fakeHoldBeforeDial();
        }
    }

    public synchronized Connection dial(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 0, "AP_FLOW_SUC");
        if (canDial()) {
            String origNumber = dialString;
            dialString = convertNumberIfNecessary(this.mPhone, dialString);
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                switchWaitingOrHoldingAndActive();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                fakeHoldForegroundBeforeDial();
            }
            if (this.mForegroundCall.getState() != Call.State.IDLE) {
                throw new CallStateException("cannot dial in current state");
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall);
            this.mHangupPendingMO = DBG_POLL;
            if (!(this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0)) {
                if (this.mPendingMO.getAddress().indexOf(78) < 0) {
                    setMute(DBG_POLL);
                    this.mCi.dial(this.mPendingMO.getAddress(), clirMode, uusInfo, obtainCompleteMessage());
                    if (this.mNumberConverted) {
                        this.mPendingMO.setConverted(origNumber);
                        this.mNumberConverted = DBG_POLL;
                    }
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                }
            }
            this.mPendingMO.mCause = 7;
            pollCallsWhenSafe();
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(origNumber);
                this.mNumberConverted = DBG_POLL;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                this.mIsEcmTimerCanceled = DBG_POLL;
            case MAX_CONNECTIONS_PER_CALL_CDMA /*1*/:
                this.mIsEcmTimerCanceled = true;
            default:
                Rlog.e(LOG_TAG, "handleEcmTimer, unsupported action " + action);
        }
    }

    private void disableDataCallInEmergencyCall(String dialString) {
        if (PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString)) {
            log("disableDataCallInEmergencyCall");
            setIsInEmergencyCall();
        }
    }

    public void setIsInEmergencyCall() {
        this.mIsInEmergencyCall = true;
        this.mPhone.mDcTracker.setInternalDataEnabled(DBG_POLL);
        this.mPhone.notifyEmergencyCallRegistrants(true);
        this.mPhone.sendEmergencyCallStateChange(true);
    }

    private Connection dial(String dialString, int clirMode) throws CallStateException {
        clearDisconnected();
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 0, "AP_FLOW_SUC");
        if (canDial()) {
            boolean internationalRoaming;
            TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
            String origNumber = dialString;
            String operatorIsoContry = tm.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
            String simIsoContry = tm.getSimCountryIsoForPhone(this.mPhone.getPhoneId());
            if (TextUtils.isEmpty(operatorIsoContry) || TextUtils.isEmpty(simIsoContry)) {
                internationalRoaming = DBG_POLL;
            } else {
                internationalRoaming = simIsoContry.equals(operatorIsoContry) ? DBG_POLL : true;
            }
            if (internationalRoaming) {
                if ("us".equals(simIsoContry)) {
                    internationalRoaming = (!internationalRoaming || "vi".equals(operatorIsoContry)) ? DBG_POLL : true;
                } else if ("vi".equals(simIsoContry)) {
                    internationalRoaming = (!internationalRoaming || "us".equals(operatorIsoContry)) ? DBG_POLL : true;
                }
            }
            if (internationalRoaming) {
                dialString = convertNumberIfNecessary(this.mPhone, dialString);
            }
            boolean isPhoneInEcmMode = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "ril.cdma.inecmmode", "false"));
            boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
            if (isPhoneInEcmMode && isEmergencyCall) {
                handleEcmTimer(MAX_CONNECTIONS_PER_CALL_CDMA);
            }
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                return dialThreeWay(dialString);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall);
            this.mHangupPendingMO = DBG_POLL;
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(DBG_POLL);
                disableDataCallInEmergencyCall(dialString);
                if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyCall)) {
                    this.mCi.dial(this.mPendingMO.getDialAddress(), clirMode, obtainCompleteMessage());
                } else {
                    this.mPhone.exitEmergencyCallbackMode();
                    this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                    this.mPendingCallClirMode = clirMode;
                    this.mPendingCallInEcm = true;
                }
            }
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(origNumber);
                this.mNumberConverted = DBG_POLL;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return this.mPendingMO;
        }
        throw new CallStateException("cannot dial in current state");
    }

    private Connection dialThreeWay(String dialString) {
        if (this.mForegroundCall.isIdle()) {
            return null;
        }
        disableDataCallInEmergencyCall(dialString);
        this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall);
        this.m3WayCallFlashDelay = this.mPhone.getContext().getResources().getInteger(17694871);
        if (this.m3WayCallFlashDelay > 0) {
            this.mCi.sendCDMAFeatureCode("", obtainMessage(20));
        } else {
            this.mCi.sendCDMAFeatureCode(this.mPendingMO.getAddress(), obtainMessage(16));
        }
        return this.mPendingMO;
    }

    public Connection dial(String dialString) throws CallStateException {
        if (isPhoneTypeGsm()) {
            return dial(dialString, 0, null);
        }
        return dial(dialString, 0);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        return dial(dialString, 0, uusInfo, intentExtras);
    }

    private Connection dial(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        return dial(dialString, clirMode, null, intentExtras);
    }

    public void acceptCall() throws CallStateException {
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 2, "AP_FLOW_SUC");
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            Rlog.i("phone", "acceptCall: incoming...");
            setMute(DBG_POLL);
            this.mCi.acceptCall(obtainCompleteMessage());
        } else if (this.mRingingCall.getState() == Call.State.WAITING) {
            if (isPhoneTypeGsm()) {
                setMute(DBG_POLL);
            } else {
                GsmCdmaConnection cwConn = (GsmCdmaConnection) this.mRingingCall.getLatestConnection();
                if (cwConn != null) {
                    this.mRingingCall.setLastRingNumberAndChangeTime(cwConn.getAddress());
                    cwConn.updateParent(this.mRingingCall, this.mForegroundCall);
                    cwConn.onConnectedInOrOut();
                }
                updatePhoneState();
            }
            switchWaitingOrHoldingAndActive();
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    public void rejectCall() throws CallStateException {
        if (this.mRingingCall.getState().isRinging()) {
            this.mCi.rejectCall(obtainCompleteMessage());
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    private void flashAndSetGenericTrue() {
        this.mCi.sendCDMAFeatureCode("", obtainMessage(MAX_CONNECTIONS_CDMA));
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (isPhoneTypeGsm()) {
            if (!this.callSwitchPending) {
                this.mCi.switchWaitingOrHoldingAndActive(obtainCompleteMessage(MAX_CONNECTIONS_CDMA));
                this.callSwitchPending = true;
            }
        } else if (this.mForegroundCall.getConnections().size() > MAX_CONNECTIONS_PER_CALL_CDMA) {
            flashAndSetGenericTrue();
        } else {
            this.mCi.sendCDMAFeatureCode("", obtainMessage(MAX_CONNECTIONS_CDMA));
        }
    }

    public void conference() {
        if (isPhoneTypeGsm()) {
            this.mCi.conference(obtainCompleteMessage(11));
        } else {
            flashAndSetGenericTrue();
        }
    }

    public void explicitCallTransfer() {
        this.mCi.explicitCallTransfer(obtainCompleteMessage(13));
    }

    public void clearDisconnected() {
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        if (this.mForegroundCall.getState() != Call.State.ACTIVE || this.mBackgroundCall.getState() != Call.State.HOLDING || this.mBackgroundCall.isFull() || this.mForegroundCall.isFull()) {
            return DBG_POLL;
        }
        return true;
    }

    private boolean canDial() {
        boolean ret;
        boolean z;
        boolean z2 = DBG_POLL;
        int serviceState = this.mPhone.getServiceState().getState();
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        Call.State fgCallState = this.mForegroundCall.getState();
        Call.State bgCallState = this.mBackgroundCall.getState();
        if (serviceState == 3 || this.mPendingMO != null || this.mRingingCall.isRinging() || disableCall.equals("true")) {
            ret = DBG_POLL;
        } else {
            if (isPhoneTypeGsm() && (fgCallState == Call.State.IDLE || fgCallState == Call.State.DISCONNECTED || fgCallState == Call.State.ACTIVE)) {
                if (!(bgCallState == Call.State.IDLE || bgCallState == Call.State.DISCONNECTED)) {
                    if (bgCallState == Call.State.HOLDING) {
                    }
                }
                z = MAX_CONNECTIONS_PER_CALL_CDMA;
                ret = z;
            }
            z = !isPhoneTypeGsm() ? (this.mForegroundCall.getState() == Call.State.ACTIVE || !this.mForegroundCall.getState().isAlive()) ? MAX_CONNECTIONS_PER_CALL_CDMA : this.mBackgroundCall.getState().isAlive() ? DBG_POLL : MAX_CONNECTIONS_PER_CALL_CDMA : DBG_POLL;
            ret = z;
        }
        if (!ret) {
            String str = "canDial is false\n((serviceState=%d) != ServiceState.STATE_POWER_OFF)::=%s\n&& pendingMO == null::=%s\n&& !ringingCall.isRinging()::=%s\n&& !disableCall.equals(\"true\")::=%s\n&& (!foregroundCall.getState().isAlive()::=%s\n   || foregroundCall.getState() == GsmCdmaCall.State.ACTIVE::=%s\n   ||!backgroundCall.getState().isAlive())::=%s)";
            Object[] objArr = new Object[MAX_CONNECTIONS_CDMA];
            objArr[0] = Integer.valueOf(serviceState);
            if (serviceState != 3) {
                z = true;
            } else {
                z = DBG_POLL;
            }
            objArr[MAX_CONNECTIONS_PER_CALL_CDMA] = Boolean.valueOf(z);
            if (this.mPendingMO == null) {
                z = true;
            } else {
                z = DBG_POLL;
            }
            objArr[2] = Boolean.valueOf(z);
            if (this.mRingingCall.isRinging()) {
                z = DBG_POLL;
            } else {
                z = true;
            }
            objArr[3] = Boolean.valueOf(z);
            if (disableCall.equals("true")) {
                z = DBG_POLL;
            } else {
                z = true;
            }
            objArr[4] = Boolean.valueOf(z);
            if (this.mForegroundCall.getState().isAlive()) {
                z = DBG_POLL;
            } else {
                z = true;
            }
            objArr[MAX_CONNECTIONS_PER_CALL_GSM] = Boolean.valueOf(z);
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                z = true;
            } else {
                z = DBG_POLL;
            }
            objArr[6] = Boolean.valueOf(z);
            if (!this.mBackgroundCall.getState().isAlive()) {
                z2 = true;
            }
            objArr[7] = Boolean.valueOf(z2);
            log(String.format(str, objArr));
        }
        return ret;
    }

    public boolean canTransfer() {
        boolean z = DBG_POLL;
        if (isPhoneTypeGsm()) {
            if (!(this.mForegroundCall.getState() == Call.State.ACTIVE || this.mForegroundCall.getState() == Call.State.ALERTING)) {
                if (this.mForegroundCall.getState() == Call.State.DIALING) {
                }
                return z;
            }
            if (this.mBackgroundCall.getState() == Call.State.HOLDING) {
                z = true;
            }
            return z;
        }
        Rlog.e(LOG_TAG, "canTransfer: not possible in CDMA");
        return DBG_POLL;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
    }

    private Message obtainCompleteMessage() {
        return obtainCompleteMessage(4);
    }

    private Message obtainCompleteMessage(int what) {
        this.mPendingOperations += MAX_CONNECTIONS_PER_CALL_CDMA;
        this.mLastRelevantPoll = null;
        this.mNeedsPoll = true;
        return obtainMessage(what);
    }

    private void operationComplete() {
        this.mPendingOperations--;
        if (this.mPendingOperations == 0 && this.mNeedsPoll) {
            this.mLastRelevantPoll = obtainMessage(MAX_CONNECTIONS_PER_CALL_CDMA);
            this.mCi.getCurrentCalls(this.mLastRelevantPoll);
        } else if (this.mPendingOperations < 0) {
            Rlog.e(LOG_TAG, "GsmCdmaCallTracker.pendingOperations < 0");
            this.mPendingOperations = 0;
        }
    }

    private void updatePhoneState() {
        State oldState = this.mState;
        if (this.mRingingCall.isRinging()) {
            this.mState = State.RINGING;
        } else if (this.mPendingMO == null && this.mForegroundCall.isIdle() && this.mBackgroundCall.isIdle()) {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (imsPhone != null) {
                imsPhone.callEndCleanupHandOverCallIfAny();
            }
            this.mState = State.IDLE;
        } else {
            this.mState = State.OFFHOOK;
        }
        if (this.mState != State.IDLE || oldState == this.mState) {
            if (oldState == State.IDLE && oldState != this.mState) {
                this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
            }
        } else if (notifyRegistrantsDelayed()) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        log("update phone state, old=" + oldState + " new=" + this.mState);
        if (this.mState != oldState) {
            this.mPhone.notifyPhoneStateChanged();
            this.mEventLog.writePhoneState(this.mState);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void handlePollCalls(AsyncResult ar) {
        if (HwDeviceManager.disallowOp(MAX_CONNECTIONS_PER_CALL_CDMA)) {
            Rlog.i(LOG_TAG, "HwDeviceManager disallow open call.");
            return;
        }
        List polledCalls;
        int i;
        if (ar.exception == null) {
            polledCalls = ar.result;
        } else {
            if (VSimUtilsInner.isVSimPhone(this.mPhone)) {
                polledCalls = new ArrayList();
                log("vsim phone handlePollCalls");
            } else {
                if (isCommandExceptionRadioNotAvailable(ar.exception)) {
                    polledCalls = new ArrayList();
                    log("no DriverCall");
                } else {
                    pollCallsAfterDelay();
                    log("pollCallsAfterDelay");
                    return;
                }
            }
        }
        Connection newRinging = null;
        ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList();
        Connection newUnknownConnectionCdma = null;
        boolean hasNonHangupStateChanged = DBG_POLL;
        int hasAnyCallDisconnected = 0;
        boolean unknownConnectionAppeared = DBG_POLL;
        boolean noConnectionExists = true;
        int curDC = 0;
        int dcSize = polledCalls.size();
        for (i = 0; i < this.mConnections.length; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            Connection hoConnection;
            Connection conn = this.mConnections[i];
            DriverCall driverCall = null;
            if (curDC < dcSize) {
                driverCall = (DriverCall) polledCalls.get(curDC);
                int i2 = driverCall.index;
                if (r0 == i + MAX_CONNECTIONS_PER_CALL_CDMA) {
                    curDC += MAX_CONNECTIONS_PER_CALL_CDMA;
                } else {
                    driverCall = null;
                }
            }
            if (!(conn == null && driverCall == null)) {
                noConnectionExists = DBG_POLL;
            }
            if (conn == null && driverCall != null) {
                if (this.mPendingMO != null) {
                    if (this.mPendingMO.compareTo(driverCall)) {
                        this.mConnections[i] = this.mPendingMO;
                        this.mPendingMO.mIndex = i;
                        this.mPendingMO.update(driverCall);
                        this.mPendingMO = null;
                        if (this.mHangupPendingMO) {
                            this.mHangupPendingMO = DBG_POLL;
                            if (!isPhoneTypeGsm() && this.mIsEcmTimerCanceled) {
                                handleEcmTimer(0);
                            }
                            try {
                                log("poll: hangupPendingMO, hangup conn " + i);
                                hangup(this.mConnections[i]);
                            } catch (CallStateException e) {
                                Rlog.e(LOG_TAG, "unexpected error on hangup");
                            }
                            return;
                        }
                        hasNonHangupStateChanged = true;
                    }
                }
                log("pendingMo=" + this.mPendingMO + ", dc=" + driverCall);
                this.mConnections[i] = new GsmCdmaConnection(this.mPhone, driverCall, this, i);
                hoConnection = getHoConnection(driverCall);
                if (hoConnection != null) {
                    Rlog.d(LOG_TAG, "hoConnection is not null");
                    this.mConnections[i].setPostDialString(hoConnection.getOrigDialString());
                    this.mConnections[i].migrateFrom(hoConnection);
                    if (!(hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || driverCall.state != DriverCall.State.ACTIVE)) {
                        Rlog.d(LOG_TAG, "mConnections onConnectedInOrOut");
                        this.mConnections[i].onConnectedInOrOut();
                    }
                    this.mHandoverConnections.remove(hoConnection);
                    Rlog.d(LOG_TAG, "notifyHandoverStateChanged mConnections[i]=" + this.mConnections[i]);
                    this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                } else {
                    newRinging = checkMtFindNewRinging(driverCall, i);
                    if (newRinging == null) {
                        unknownConnectionAppeared = true;
                        if (isPhoneTypeGsm()) {
                            newUnknownConnectionsGsm.add(this.mConnections[i]);
                        } else {
                            newUnknownConnectionCdma = this.mConnections[i];
                        }
                    }
                }
                hasNonHangupStateChanged = true;
            } else if (conn == null || driverCall != null) {
                if (conn != null && driverCall != null && !conn.compareTo(driverCall) && isPhoneTypeGsm()) {
                    this.mDroppedDuringPoll.add(conn);
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, driverCall, this, i);
                    if (this.mConnections[i].getCall() == this.mRingingCall) {
                        newRinging = this.mConnections[i];
                    }
                    hasNonHangupStateChanged = true;
                } else if (!(conn == null || driverCall == null)) {
                    if (isPhoneTypeGsm() || conn.isIncoming() == driverCall.isMT) {
                        hasNonHangupStateChanged = !hasNonHangupStateChanged ? conn.update(driverCall) : true;
                    } else if (driverCall.isMT) {
                        this.mDroppedDuringPoll.add(conn);
                        this.mConnections[i] = new GsmCdmaConnection(this.mPhone, driverCall, this, i);
                        newRinging = checkMtFindNewRinging(driverCall, i);
                        if (newRinging == null) {
                            unknownConnectionAppeared = true;
                            newUnknownConnectionCdma = conn;
                        }
                        checkAndEnableDataCallAfterEmergencyCallDropped();
                    } else {
                        Rlog.e(LOG_TAG, "Error in RIL, Phantom call appeared " + driverCall);
                    }
                }
            } else if (isPhoneTypeGsm()) {
                this.mDroppedDuringPoll.add(conn);
                this.mConnections[i] = null;
            } else {
                if (dcSize != 0) {
                    log("conn != null, dc == null. Still have connections in the call list");
                    this.mDroppedDuringPoll.add(conn);
                } else {
                    int n;
                    int count = this.mForegroundCall.mConnections.size();
                    for (n = 0; n < count; n += MAX_CONNECTIONS_PER_CALL_CDMA) {
                        log("adding fgCall cn " + n + " to droppedDuringPoll");
                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n));
                    }
                    count = this.mRingingCall.mConnections.size();
                    for (n = 0; n < count; n += MAX_CONNECTIONS_PER_CALL_CDMA) {
                        log("adding rgCall cn " + n + " to droppedDuringPoll");
                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n));
                    }
                }
                if (this.mIsEcmTimerCanceled) {
                    handleEcmTimer(0);
                }
                checkAndEnableDataCallAfterEmergencyCallDropped();
                this.mConnections[i] = null;
            }
        }
        if (!isPhoneTypeGsm() && noConnectionExists) {
            checkAndEnableDataCallAfterEmergencyCallDropped();
        }
        if (this.mPendingMO != null) {
            Rlog.d(LOG_TAG, "Pending MO dropped before poll fg state:" + this.mForegroundCall.getState());
            this.mDroppedDuringPoll.add(this.mPendingMO);
            this.mPendingMO = null;
            this.mHangupPendingMO = DBG_POLL;
            if (!isPhoneTypeGsm()) {
                if (this.mPendingCallInEcm) {
                    this.mPendingCallInEcm = DBG_POLL;
                }
                checkAndEnableDataCallAfterEmergencyCallDropped();
            }
        }
        if (newRinging != null) {
            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), MAX_CONNECTIONS_PER_CALL_CDMA, LOG_TAG);
            this.mPhone.notifyNewRingingConnection(newRinging);
        }
        for (i = this.mDroppedDuringPoll.size() - 1; i >= 0; i--) {
            conn = (GsmCdmaConnection) this.mDroppedDuringPoll.get(i);
            boolean wasDisconnected = DBG_POLL;
            if (conn.isIncoming() && conn.getConnectTime() == 0) {
                int cause;
                i2 = conn.mCause;
                if (r0 == 3) {
                    cause = 16;
                } else {
                    cause = MAX_CONNECTIONS_PER_CALL_CDMA;
                }
                log("missed/rejected call, conn.cause=" + conn.mCause);
                log("setting cause to " + cause);
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn.onDisconnect(cause);
                wasDisconnected = true;
            } else {
                i2 = conn.mCause;
                if (r0 != 3) {
                    i2 = conn.mCause;
                }
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn.onDisconnect(conn.mCause);
                wasDisconnected = true;
            }
            if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared && conn == newUnknownConnectionCdma) {
                unknownConnectionAppeared = DBG_POLL;
                newUnknownConnectionCdma = null;
            }
        }
        Iterator<Connection> it = this.mHandoverConnections.iterator();
        while (it.hasNext()) {
            hoConnection = (Connection) it.next();
            log("handlePollCalls - disconnect hoConn= " + hoConnection + " hoConn.State= " + hoConnection.getState());
            if (hoConnection.getState().isRinging()) {
                hasAnyCallDisconnected |= hoConnection.onDisconnect(MAX_CONNECTIONS_PER_CALL_CDMA);
            } else {
                hasAnyCallDisconnected |= hoConnection.onDisconnect(-1);
            }
            it.remove();
        }
        if (this.mDroppedDuringPoll.size() > 0) {
            this.mCi.getLastCallFailCause(obtainNoPollCompleteMessage(MAX_CONNECTIONS_PER_CALL_GSM));
        } else {
            this.mIsInCsRedial = DBG_POLL;
        }
        if (DBG_POLL) {
            pollCallsAfterDelay();
        }
        if (!(newRinging == null && !hasNonHangupStateChanged && hasAnyCallDisconnected == 0)) {
            internalClearDisconnected();
        }
        updatePhoneState();
        if (unknownConnectionAppeared) {
            if (isPhoneTypeGsm()) {
                for (Connection c : newUnknownConnectionsGsm) {
                    log("Notify unknown for " + c);
                    this.mPhone.notifyUnknownConnection(c);
                }
            } else {
                this.mPhone.notifyUnknownConnection(newUnknownConnectionCdma);
            }
        }
        if (!hasNonHangupStateChanged && newRinging == null) {
            if (hasAnyCallDisconnected != 0) {
            }
        }
        this.mPhone.notifyPreciseCallStateChanged();
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        int i;
        Rlog.i(LOG_TAG, "Phone State:" + this.mState);
        Rlog.i(LOG_TAG, "Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (i = 0; i < s; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Foreground call: " + this.mForegroundCall.toString());
        l = this.mForegroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Background call: " + this.mBackgroundCall.toString());
        l = this.mBackgroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
    }

    public void hangup(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner != this) {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
        if (conn == this.mPendingMO) {
            log("hangup: set hangupPendingMO to true");
            this.mHangupPendingMO = true;
        } else if (!isPhoneTypeGsm() && conn.getCall() == this.mRingingCall && this.mRingingCall.getState() == Call.State.WAITING) {
            this.mRingingCall.setLastRingNumberAndChangeTime(conn.getAddress());
            conn.onLocalDisconnect();
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return;
        } else {
            try {
                this.mCi.hangupConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage());
            } catch (CallStateException e) {
                Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: hangup() on absent connection " + conn);
            }
        }
        conn.onHangupLocal();
        Call.State state = conn.getState();
        boolean isMultiparty = (this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) ? this.mForegroundCall.isMultiparty() : DBG_POLL;
        if (!isMultiparty) {
            delaySendRilRecoveryMsg(state);
        }
        state = conn.getState();
        if (state == Call.State.IDLE || state == Call.State.DISCONNECTED) {
            cleanRilRecovery();
        }
    }

    public void separate(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner != this) {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
        try {
            this.mCi.separateConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(12));
        } catch (CallStateException e) {
            Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: separate() on absent connection " + conn);
        }
    }

    public void setMute(boolean mute) {
        this.mDesiredMute = mute;
        this.mCi.setMute(this.mDesiredMute, null);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void hangup(GsmCdmaCall call) throws CallStateException {
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections in call");
        }
        if (call == this.mRingingCall) {
            log("(ringing) hangup waiting or background");
            if (IS_SUPPORT_RIL_RECOVERY) {
                hangupAllConnections(call);
            } else {
                this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
            }
            delaySendRilRecoveryMsg(call.getState());
        } else if (call == this.mForegroundCall) {
            if (call.isDialingOrAlerting()) {
                log("(foregnd) hangup dialing or alerting...");
                hangup((GsmCdmaConnection) call.getConnections().get(0));
            } else if (isPhoneTypeGsm() && this.mRingingCall.isRinging()) {
                log("hangup all conns in active/background call, without affecting ringing call");
                hangupAllConnections(call);
            } else {
                hangupForegroundResumeBackground();
                delaySendRilRecoveryMsg(call.getState());
            }
        } else if (call == this.mBackgroundCall) {
            if (this.mRingingCall.isRinging()) {
                log("hangup all conns in background call");
                hangupAllConnections(call);
            } else {
                hangupAllConnections(call);
            }
            delaySendRilRecoveryMsg(call.getState());
        } else {
            throw new RuntimeException("GsmCdmaCall " + call + "does not belong to GsmCdmaCallTracker " + this);
        }
        Call.State state = call.getState();
        if (state == Call.State.IDLE || state == Call.State.DISCONNECTED) {
            cleanRilRecovery();
        }
        call.onHangupLocal();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public void hangupWaitingOrBackground() {
        log("hangupWaitingOrBackground");
        this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
    }

    public void hangupForegroundResumeBackground() {
        log("hangupForegroundResumeBackground");
        this.mCi.hangupForegroundResumeBackground(obtainCompleteMessage());
    }

    public void hangupConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            if (((GsmCdmaConnection) call.mConnections.get(i)).getGsmCdmaIndex() == index) {
                this.mCi.hangupConnection(index, obtainCompleteMessage());
                return;
            }
        }
        throw new CallStateException("no GsmCdma index found");
    }

    public void hangupAllConnections(GsmCdmaCall call) {
        try {
            int count = call.mConnections.size();
            for (int i = 0; i < count; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
                this.mCi.hangupConnection(((GsmCdmaConnection) call.mConnections.get(i)).getGsmCdmaIndex(), obtainCompleteMessage());
            }
        } catch (CallStateException ex) {
            Rlog.e(LOG_TAG, "hangupConnectionByIndex caught " + ex);
        }
    }

    public GsmCdmaConnection getConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            if (cn.getGsmCdmaIndex() == index) {
                return cn;
            }
        }
        return null;
    }

    private void notifyCallWaitingInfo(CdmaCallWaitingNotification obj) {
        if (this.mCallWaitingRegistrants != null) {
            this.mCallWaitingRegistrants.notifyRegistrants(new AsyncResult(null, obj, null));
        }
    }

    private void notifyWaitingNumberChanged() {
        Rlog.i(LOG_TAG, "notifyWaitingNumberChanged");
        if (this.cdmaWaitingNumberChangedRegistrants != null) {
            this.cdmaWaitingNumberChangedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
    }

    private void handleCallWaitingInfo(CdmaCallWaitingNotification cw) {
        if (!isPhoneTypeGsm() && this.mRingingCall.getState() == Call.State.WAITING) {
            GsmCdmaConnection cwConn = (GsmCdmaConnection) this.mRingingCall.getLatestConnection();
            if (!(cwConn == null || cw.number == null || !cw.number.equals(cwConn.getAddress()))) {
                long passedTime = System.currentTimeMillis() - cwConn.getCreateTime();
                if (passedTime > 0 && passedTime < 30000) {
                    Rlog.d(LOG_TAG, "Ignoring callingwaiting events received for same number within 30s");
                    return;
                }
            }
        }
        Rlog.i(LOG_TAG, "the ringingcall connection size = " + this.mRingingCall.mConnections.size());
        GsmCdmaConnection gsmCdmaConnection;
        if (this.mRingingCall.mConnections.size() > 0) {
            if (((GsmCdmaConnection) this.mRingingCall.getLatestConnection()).isNewConnection(cw)) {
                Rlog.i(LOG_TAG, "Update the ringing connection if the call is from another number.");
                notifyWaitingNumberChanged();
                gsmCdmaConnection = new GsmCdmaConnection(this.mPhone.getContext(), cw, this, this.mRingingCall);
            }
        } else if (this.mRingingCall.isTooFrequency(cw.number)) {
            this.mRingingCall.setLastRingNumberAndChangeTime(cw.number);
            Rlog.i(LOG_TAG, "ringing too frequency, ignore this callwaiting message.");
            return;
        } else {
            Rlog.i(LOG_TAG, "new callwaiting message, create a connection");
            gsmCdmaConnection = new GsmCdmaConnection(this.mPhone.getContext(), cw, this, this.mRingingCall);
        }
        updatePhoneState();
        notifyCallWaitingInfo(cw);
    }

    private SuppService getFailedService(int what) {
        switch (what) {
            case MAX_CONNECTIONS_CDMA /*8*/:
                return SuppService.SWITCH;
            case CharacterSets.ISO_8859_8 /*11*/:
                return SuppService.CONFERENCE;
            case CharacterSets.ISO_8859_9 /*12*/:
                return SuppService.SEPARATE;
            case UserData.ASCII_CR_INDEX /*13*/:
                return SuppService.TRANSFER;
            default:
                return SuppService.UNKNOWN;
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case MAX_CONNECTIONS_PER_CALL_CDMA /*1*/:
                Rlog.d(LOG_TAG, "Event EVENT_POLL_CALLS_RESULT Received");
                if (msg == this.mLastRelevantPoll) {
                    log("handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                    this.mNeedsPoll = DBG_POLL;
                    this.mLastRelevantPoll = null;
                    handlePollCalls((AsyncResult) msg.obj);
                }
            case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
            case PduPersister.PROC_STATUS_COMPLETED /*3*/:
                pollCallsWhenSafe();
            case CharacterSets.ISO_8859_1 /*4*/:
                operationComplete();
            case MAX_CONNECTIONS_PER_CALL_GSM /*5*/:
                int causeCode;
                int s;
                int i;
                String vendorCause = null;
                ar = (AsyncResult) msg.obj;
                operationComplete();
                if (ar.exception != null) {
                    causeCode = 16;
                    Rlog.i(LOG_TAG, "Exception during getLastCallFailCause, assuming normal disconnect");
                } else {
                    LastCallFailCause failCause = ar.result;
                    causeCode = failCause.causeCode;
                    vendorCause = failCause.vendorCause;
                }
                if (!(causeCode == 34 || causeCode == 41 || causeCode == 42 || causeCode == 44 || causeCode == 49 || causeCode == 58)) {
                    if (causeCode == CallFailCause.ERROR_UNSPECIFIED) {
                    }
                    s = this.mDroppedDuringPoll.size();
                    for (i = 0; i < s; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
                        ((GsmCdmaConnection) this.mDroppedDuringPoll.get(i)).onRemoteDisconnect(causeCode, vendorCause);
                    }
                    this.mIsInCsRedial = DBG_POLL;
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                    this.mDroppedDuringPoll.clear();
                }
                CellLocation loc = this.mPhone.getCellLocation();
                int cid = -1;
                if (loc != null) {
                    if (isPhoneTypeGsm()) {
                        cid = ((GsmCellLocation) loc).getCid();
                    } else {
                        cid = ((CdmaCellLocation) loc).getBaseStationId();
                    }
                }
                EventLog.writeEvent(EventLogTags.CALL_DROP, new Object[]{Integer.valueOf(causeCode), Integer.valueOf(cid), Integer.valueOf(TelephonyManager.getDefault().getNetworkType())});
                s = this.mDroppedDuringPoll.size();
                for (i = 0; i < s; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
                    ((GsmCdmaConnection) this.mDroppedDuringPoll.get(i)).onRemoteDisconnect(causeCode, vendorCause);
                }
                this.mIsInCsRedial = DBG_POLL;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                this.mDroppedDuringPoll.clear();
            case MAX_CONNECTIONS_CDMA /*8*/:
                if (isPhoneTypeGsm()) {
                    this.callSwitchPending = DBG_POLL;
                    if (msg.obj.exception != null) {
                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                    }
                    operationComplete();
                }
            case CharacterSets.ISO_8859_6 /*9*/:
                handleRadioAvailable();
            case CharacterSets.ISO_8859_7 /*10*/:
                handleRadioNotAvailable();
            case CharacterSets.ISO_8859_8 /*11*/:
                if (isPhoneTypeGsm()) {
                    if (((AsyncResult) msg.obj).exception != null) {
                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                        List<Connection> conn = this.mForegroundCall.getConnections();
                        if (conn != null) {
                            Rlog.d(LOG_TAG, "Notify merge failure");
                            if (conn.size() != 0) {
                                ((Connection) conn.get(0)).onConferenceMergeFailed();
                            }
                        }
                    }
                    operationComplete();
                }
            case CharacterSets.ISO_8859_9 /*12*/:
            case UserData.ASCII_CR_INDEX /*13*/:
                if (isPhoneTypeGsm()) {
                    if (((AsyncResult) msg.obj).exception != null) {
                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                    }
                    operationComplete();
                } else if (msg.what != MAX_CONNECTIONS_CDMA) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                }
            case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                }
                if (this.mPendingCallInEcm) {
                    this.mCi.dial(this.mPendingMO.getDialAddress(), this.mPendingCallClirMode, obtainCompleteMessage());
                    this.mPendingCallInEcm = DBG_POLL;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
            case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                }
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    handleCallWaitingInfo((CdmaCallWaitingNotification) ar.result);
                    Rlog.d(LOG_TAG, "Event EVENT_CALL_WAITING_INFO_CDMA Received");
                }
            case PduHeaders.MMS_VERSION_1_0 /*16*/:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                } else if (((AsyncResult) msg.obj).exception == null && this.mPendingMO != null) {
                    this.mPendingMO.onConnectedInOrOut();
                    this.mPendingMO = null;
                }
            case SmsHeader.ELT_ID_EXTENDED_OBJECT /*20*/:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                } else if (((AsyncResult) msg.obj).exception == null) {
                    postDelayed(new Runnable() {
                        public void run() {
                            if (GsmCdmaCallTracker.this.mPendingMO != null) {
                                GsmCdmaCallTracker.this.mCi.sendCDMAFeatureCode(GsmCdmaCallTracker.this.mPendingMO.getAddress(), GsmCdmaCallTracker.this.obtainMessage(16));
                            }
                        }
                    }, (long) this.m3WayCallFlashDelay);
                } else {
                    this.mPendingMO = null;
                    Rlog.w(LOG_TAG, "exception happened on Blank Flash for 3-way call");
                }
            case SmsCbConstants.MESSAGE_ID_GSMA_ALLOCATED_CHANNEL_50 /*50*/:
                Rlog.d(LOG_TAG, "Event EVENT_RSRVCC_STATE_CHANGED Received");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mIsInCsRedial = true;
                } else {
                    Rlog.e(LOG_TAG, "RSrvcc exception: " + ar.exception);
                }
            case 201:
                if (CallTracker.IS_SUPPORT_RIL_RECOVERY) {
                    log("restartRild for hungup call.");
                    this.mCi.restartRild(null);
                }
            default:
                throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
        }
    }

    private void checkAndEnableDataCallAfterEmergencyCallDropped() {
        if (this.mIsInEmergencyCall) {
            this.mIsInEmergencyCall = DBG_POLL;
            boolean isInEcm = Boolean.parseBoolean(TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "ril.cdma.inecmmode", "false"));
            log("checkAndEnableDataCallAfterEmergencyCallDropped,isInEcm=" + isInEcm);
            if (!isInEcm) {
                this.mPhone.mDcTracker.setInternalDataEnabled(true);
                this.mPhone.notifyEmergencyCallRegistrants(DBG_POLL);
            }
            this.mPhone.sendEmergencyCallStateChange(DBG_POLL);
        }
    }

    private Connection checkMtFindNewRinging(DriverCall dc, int i) {
        if (this.mConnections[i].getCall() == this.mRingingCall) {
            Connection newRinging = this.mConnections[i];
            log("Notify new ring " + dc);
            return newRinging;
        }
        Rlog.e(LOG_TAG, "Phantom call appeared " + dc);
        if (dc.state == DriverCall.State.ALERTING || dc.state == DriverCall.State.DIALING) {
            return null;
        }
        this.mConnections[i].onConnectedInOrOut();
        if (dc.state != DriverCall.State.HOLDING) {
            return null;
        }
        this.mConnections[i].onStartedHolding();
        return null;
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    private boolean isPhoneTypeGsm() {
        return this.mPhone.getPhoneType() == MAX_CONNECTIONS_PER_CALL_CDMA ? true : DBG_POLL;
    }

    public GsmCdmaPhone getPhone() {
        return this.mPhone;
    }

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[GsmCdmaCallTracker] " + msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("GsmCdmaCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println("mConnections: length=" + this.mConnections.length);
        for (i = 0; i < this.mConnections.length; i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            pw.printf("  mConnections[%d]=%s\n", new Object[]{Integer.valueOf(i), this.mConnections[i]});
        }
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        if (!isPhoneTypeGsm()) {
            pw.println(" mCallWaitingRegistrants=" + this.mCallWaitingRegistrants);
        }
        pw.println(" mDroppedDuringPoll: size=" + this.mDroppedDuringPoll.size());
        for (i = 0; i < this.mDroppedDuringPoll.size(); i += MAX_CONNECTIONS_PER_CALL_CDMA) {
            pw.printf("  mDroppedDuringPoll[%d]=%s\n", new Object[]{Integer.valueOf(i), this.mDroppedDuringPoll.get(i)});
        }
        pw.println(" mRingingCall=" + this.mRingingCall);
        pw.println(" mForegroundCall=" + this.mForegroundCall);
        pw.println(" mBackgroundCall=" + this.mBackgroundCall);
        pw.println(" mPendingMO=" + this.mPendingMO);
        pw.println(" mHangupPendingMO=" + this.mHangupPendingMO);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDesiredMute=" + this.mDesiredMute);
        pw.println(" mState=" + this.mState);
        if (!isPhoneTypeGsm()) {
            pw.println(" mPendingCallInEcm=" + this.mPendingCallInEcm);
            pw.println(" mIsInEmergencyCall=" + this.mIsInEmergencyCall);
            pw.println(" mPendingCallClirMode=" + this.mPendingCallClirMode);
            pw.println(" mIsEcmTimerCanceled=" + this.mIsEcmTimerCanceled);
        }
    }

    public State getState() {
        return this.mState;
    }

    public int getMaxConnectionsPerCall() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return MAX_CONNECTIONS_PER_CALL_GSM;
        }
        return MAX_CONNECTIONS_PER_CALL_CDMA;
    }

    public boolean isInCsRedial() {
        return this.mIsInCsRedial;
    }
}

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
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.os.UserManager;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import huawei.cust.HwCustUtils;
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
    boolean callSwitchPending = false;
    RegistrantList cdmaWaitingNumberChangedRegistrants = new RegistrantList();
    private int m3WayCallFlashDelay;
    public GsmCdmaCall mBackgroundCall = new GsmCdmaCall(this);
    private RegistrantList mCallWaitingRegistrants = new RegistrantList();
    @VisibleForTesting
    public GsmCdmaConnection[] mConnections;
    private boolean mDesiredMute = false;
    private ArrayList<GsmCdmaConnection> mDroppedDuringPoll = new ArrayList<>(19);
    private BroadcastReceiver mEcmExitReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !"android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(intent.getAction()))) {
                boolean isInEcm = intent.getBooleanExtra("phoneinECMState", false);
                GsmCdmaCallTracker gsmCdmaCallTracker = GsmCdmaCallTracker.this;
                gsmCdmaCallTracker.log("Received ACTION_EMERGENCY_CALLBACK_MODE_CHANGED isInEcm = " + isInEcm);
                if (!isInEcm) {
                    List<Connection> toNotify = new ArrayList<>();
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
    public GsmCdmaCall mForegroundCall = new GsmCdmaCall(this);
    private boolean mHangupPendingMO;
    private HwCustGsmCdmaCallTracker mHwGCT;
    private boolean mIsEcmTimerCanceled;
    private boolean mIsInCsRedial = false;
    private boolean mIsInEmergencyCall;
    private TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    private int mPendingCallClirMode;
    private boolean mPendingCallInEcm;
    /* access modifiers changed from: private */
    public GsmCdmaConnection mPendingMO;
    public GsmCdmaPhone mPhone;
    public GsmCdmaCall mRingingCall = new GsmCdmaCall(this);
    public PhoneConstants.State mState = PhoneConstants.State.IDLE;
    public RegistrantList mVoiceCallEndedRegistrants = new RegistrantList();
    private RegistrantList mVoiceCallStartedRegistrants = new RegistrantList();

    public GsmCdmaCallTracker(GsmCdmaPhone phone) {
        super(phone);
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
        this.mHwGCT = (HwCustGsmCdmaCallTracker) HwCustUtils.createObj(HwCustGsmCdmaCallTracker.class, new Object[]{this.mPhone});
    }

    public void updatePhoneType() {
        updatePhoneType(false);
    }

    private void updatePhoneType(boolean duringInit) {
        if (!duringInit) {
            reset();
            pollCallsWhenSafe();
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            this.mConnections = new GsmCdmaConnection[19];
            this.mCi.unregisterForCallWaitingInfo(this);
            if (this.mIsInEmergencyCall) {
                this.mPhone.mDcTracker.setInternalDataEnabled(true);
                return;
            }
            return;
        }
        this.mConnections = new GsmCdmaConnection[8];
        this.mPendingCallInEcm = false;
        this.mIsInEmergencyCall = false;
        this.mPendingCallClirMode = 0;
        this.mIsEcmTimerCanceled = false;
        this.m3WayCallFlashDelay = 0;
        this.mCi.registerForCallWaitingInfo(this, 15, null);
    }

    private void reset() {
        Rlog.d(LOG_TAG, "reset");
        for (GsmCdmaConnection gsmCdmaConnection : this.mConnections) {
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.onDisconnect(36);
                gsmCdmaConnection.dispose();
            }
        }
        if (this.mPendingMO != null) {
            this.mPendingMO.onDisconnect(36);
            this.mPendingMO.dispose();
        }
        this.mConnections = null;
        this.mPendingMO = null;
        clearDisconnected();
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        Rlog.d(LOG_TAG, "GsmCdmaCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceCallStartedRegistrants.add(r);
        if (this.mState != PhoneConstants.State.IDLE) {
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
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) connCopy.get(i)).fakeHoldBeforeDial();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00c2  */
    public synchronized Connection dial(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        if (canDial()) {
            String origNumber = dialString;
            String dialString2 = convertNumberIfNecessary(this.mPhone, dialString);
            if (this.mHwCT != null) {
                if (this.mHwCT.isBlockDialing(dialString2, this.mPhone.getSubId())) {
                    Rlog.d(LOG_TAG, "Blocking non emergency call from non AIS card");
                    throw new CallStateException("Non emergency outgoing call not allowed");
                }
            }
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                switchWaitingOrHoldingAndActive();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                fakeHoldForegroundBeforeDial();
            }
            if (this.mForegroundCall.getState() == Call.State.IDLE) {
                GsmCdmaConnection gsmCdmaConnection = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString2));
                this.mPendingMO = gsmCdmaConnection;
                this.mHangupPendingMO = false;
                this.mMetrics.writeRilDial(this.mPhone.getPhoneId(), this.mPendingMO, clirMode, uusInfo);
                if (!(this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0)) {
                    if (this.mPendingMO.getAddress().indexOf(78) < 0) {
                        setMute(false);
                        this.mCi.dial(this.mPendingMO.getAddress(), clirMode, uusInfo, obtainCompleteMessage());
                        if (this.mNumberConverted) {
                            this.mPendingMO.setConverted(origNumber);
                            this.mNumberConverted = false;
                        }
                        updatePhoneState();
                        this.mPhone.notifyPreciseCallStateChanged();
                    }
                }
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
                if (this.mNumberConverted) {
                }
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
            } else {
                throw new CallStateException("cannot dial in current state");
            }
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case 0:
                this.mIsEcmTimerCanceled = false;
                return;
            case 1:
                this.mIsEcmTimerCanceled = true;
                return;
            default:
                Rlog.e(LOG_TAG, "handleEcmTimer, unsupported action " + action);
                return;
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
        this.mPhone.mDcTracker.setInternalDataEnabled(false);
        this.mPhone.notifyEmergencyCallRegistrants(true);
        this.mPhone.sendEmergencyCallStateChange(true);
    }

    private Connection dial(String dialString, int clirMode) throws CallStateException {
        String dialString2;
        int i = clirMode;
        clearDisconnected();
        if (canDial()) {
            TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
            String origNumber = dialString;
            String operatorIsoContry = tm.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
            String simIsoContry = tm.getSimCountryIsoForPhone(this.mPhone.getPhoneId());
            boolean internationalRoaming = !TextUtils.isEmpty(operatorIsoContry) && !TextUtils.isEmpty(simIsoContry) && !simIsoContry.equals(operatorIsoContry);
            if (internationalRoaming) {
                if ("us".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming && !"vi".equals(operatorIsoContry);
                } else if ("vi".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming && !"us".equals(operatorIsoContry);
                }
            }
            if (internationalRoaming) {
                dialString2 = convertNumberIfNecessary(this.mPhone, dialString);
            } else {
                dialString2 = dialString;
            }
            boolean isPhoneInEcmMode = this.mPhone.isInEcm();
            boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString2);
            if (isPhoneInEcmMode && isEmergencyCall) {
                handleEcmTimer(1);
            }
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                return dialThreeWay(dialString2);
            }
            GsmCdmaConnection gsmCdmaConnection = r0;
            GsmCdmaConnection gsmCdmaConnection2 = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, isEmergencyCall);
            this.mPendingMO = gsmCdmaConnection;
            this.mHangupPendingMO = false;
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(false);
                disableDataCallInEmergencyCall(dialString2);
                if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyCall)) {
                    this.mCi.dial(this.mPendingMO.getDialAddress(), i, obtainCompleteMessage());
                } else {
                    this.mPhone.exitEmergencyCallbackMode();
                    this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                    this.mPendingCallClirMode = i;
                    this.mPendingCallInEcm = true;
                }
            }
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(origNumber);
                this.mNumberConverted = false;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return this.mPendingMO;
        }
        String str = dialString;
        throw new CallStateException("cannot dial in current state");
    }

    private Connection dialThreeWay(String dialString) {
        if (this.mForegroundCall.isIdle()) {
            return null;
        }
        disableDataCallInEmergencyCall(dialString);
        GsmCdmaConnection gsmCdmaConnection = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, this.mIsInEmergencyCall);
        this.mPendingMO = gsmCdmaConnection;
        PersistableBundle bundle = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfig();
        if (bundle != null) {
            this.m3WayCallFlashDelay = bundle.getInt("cdma_3waycall_flash_delay_int");
        } else {
            this.m3WayCallFlashDelay = 0;
        }
        if (this.m3WayCallFlashDelay > 0) {
            this.mCi.sendCDMAFeatureCode("", obtainMessage(20));
        } else {
            this.mCi.sendCDMAFeatureCode(this.mPendingMO.getAddress(), obtainMessage(16));
        }
        return this.mPendingMO;
    }

    public Connection dial(String dialString) throws CallStateException {
        if (isPhoneTypeGsm()) {
            return dial(dialString, 0, (Bundle) null);
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
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 2, LOG_TAG);
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            Rlog.i("phone", "acceptCall: incoming...");
            setMute(false);
            this.mCi.acceptCall(obtainCompleteMessage());
        } else if (this.mRingingCall.getState() == Call.State.WAITING) {
            if (isPhoneTypeGsm()) {
                setMute(false);
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
        this.mCi.sendCDMAFeatureCode("", obtainMessage(8));
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (isPhoneTypeGsm()) {
            if (!this.callSwitchPending) {
                this.mCi.switchWaitingOrHoldingAndActive(obtainCompleteMessage(8));
                this.callSwitchPending = true;
            }
        } else if (this.mForegroundCall.getConnections().size() > 1) {
            flashAndSetGenericTrue();
        } else {
            this.mCi.sendCDMAFeatureCode("", obtainMessage(8));
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
        return this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING && !this.mBackgroundCall.isFull() && !this.mForegroundCall.isFull();
    }

    private boolean canDial() {
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        Call.State fgCallState = this.mForegroundCall.getState();
        Call.State bgCallState = this.mBackgroundCall.getState();
        CommandsInterface.RadioState radioState = this.mCi.getRadioState();
        boolean custCanDial = custCanDial();
        boolean z = false;
        boolean ret = radioState == CommandsInterface.RadioState.RADIO_ON && this.mPendingMO == null && !this.mRingingCall.isRinging() && !disableCall.equals("true") && ((isPhoneTypeGsm() && ((fgCallState == Call.State.IDLE || fgCallState == Call.State.DISCONNECTED || fgCallState == Call.State.ACTIVE) && (bgCallState == Call.State.IDLE || bgCallState == Call.State.DISCONNECTED || bgCallState == Call.State.HOLDING))) || (!isPhoneTypeGsm() && (this.mForegroundCall.getState() == Call.State.ACTIVE || !this.mForegroundCall.getState().isAlive() || !this.mBackgroundCall.getState().isAlive()))) && custCanDial;
        if (!ret) {
            Object[] objArr = new Object[9];
            objArr[0] = radioState;
            objArr[1] = Boolean.valueOf(radioState == CommandsInterface.RadioState.RADIO_ON);
            objArr[2] = Boolean.valueOf(this.mPendingMO == null);
            objArr[3] = Boolean.valueOf(!this.mRingingCall.isRinging());
            objArr[4] = Boolean.valueOf(!disableCall.equals("true"));
            objArr[5] = Boolean.valueOf(!this.mForegroundCall.getState().isAlive());
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                z = true;
            }
            objArr[6] = Boolean.valueOf(z);
            objArr[7] = Boolean.valueOf(true ^ this.mBackgroundCall.getState().isAlive());
            objArr[8] = Boolean.valueOf(custCanDial);
            log(String.format("canDial is false\n((radioState=%s) == CommandsInterface.RadioState.RADIO_ON)::=%s\n&& pendingMO == null::=%s\n&& !ringingCall.isRinging()::=%s\n&& !disableCall.equals(\"true\")::=%s\n&& (!foregroundCall.getState().isAlive()::=%s\n   || foregroundCall.getState() == GsmCdmaCall.State.ACTIVE::=%s\n   ||!backgroundCall.getState().isAlive())::=%s)\n&& custCanDial::=%s\n", objArr));
        }
        return ret;
    }

    public boolean canTransfer() {
        boolean z = false;
        if (isPhoneTypeGsm()) {
            if ((this.mForegroundCall.getState() == Call.State.ACTIVE || this.mForegroundCall.getState() == Call.State.ALERTING || this.mForegroundCall.getState() == Call.State.DIALING) && this.mBackgroundCall.getState() == Call.State.HOLDING) {
                z = true;
            }
            return z;
        }
        Rlog.e(LOG_TAG, "canTransfer: not possible in CDMA");
        return false;
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
        this.mPendingOperations++;
        this.mLastRelevantPoll = null;
        this.mNeedsPoll = true;
        return obtainMessage(what);
    }

    private void operationComplete() {
        this.mPendingOperations--;
        if (this.mPendingOperations == 0 && this.mNeedsPoll) {
            this.mLastRelevantPoll = obtainMessage(1);
            if (mIsShowRedirectNumber) {
                this.mCi.getCurrentCallsEx(this.mLastRelevantPoll);
            } else {
                this.mCi.getCurrentCalls(this.mLastRelevantPoll);
            }
        } else if (this.mPendingOperations < 0) {
            Rlog.e(LOG_TAG, "GsmCdmaCallTracker.pendingOperations < 0");
            this.mPendingOperations = 0;
        }
    }

    private void updatePhoneState() {
        PhoneConstants.State oldState = this.mState;
        if (this.mRingingCall.isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (this.mPendingMO != null || !this.mForegroundCall.isIdle() || !this.mBackgroundCall.isIdle()) {
            this.mState = PhoneConstants.State.OFFHOOK;
        } else {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (imsPhone != null && !this.mIsInCsRedial) {
                log("[SRVCC] updatePhoneState -> clean ims HandoverCall.");
                imsPhone.callEndCleanupHandOverCallIfAny();
            }
            this.mState = PhoneConstants.State.IDLE;
        }
        if (this.mState != PhoneConstants.State.IDLE || oldState == this.mState) {
            if (oldState == PhoneConstants.State.IDLE && oldState != this.mState) {
                this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
            }
        } else if (notifyRegistrantsDelayed()) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        log("update phone state, old=" + oldState + " new=" + this.mState);
        if (this.mState != oldState || (this.mIsSrvccHappened && PhoneConstants.State.IDLE == this.mState)) {
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            if (this.mIsSrvccHappened) {
                this.mIsSrvccHappened = false;
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x0595, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0383 A[Catch:{ CallStateException -> 0x016c }] */
    public synchronized void handlePollCalls(AsyncResult ar) {
        List polledCalls;
        Connection c;
        boolean hasAnyCallDisconnected;
        int cause;
        DriverCall dc;
        GsmCdmaConnection conn;
        boolean noConnectionExists;
        boolean unknownConnectionAppeared;
        boolean hasAnyCallDisconnected2;
        boolean hasNonHangupStateChanged;
        boolean z;
        AsyncResult asyncResult = ar;
        synchronized (this) {
            if (HwDeviceManager.disallowOp(1)) {
                Rlog.i(LOG_TAG, "HwDeviceManager disallow open call.");
                return;
            }
            if (asyncResult.exception == null) {
                polledCalls = (List) asyncResult.result;
            } else if (VSimUtilsInner.isVSimPhone(this.mPhone)) {
                polledCalls = new ArrayList();
                log("vsim phone handlePollCalls");
            } else if (isCommandExceptionRadioNotAvailable(asyncResult.exception)) {
                polledCalls = new ArrayList();
                log("no DriverCall");
            } else {
                pollCallsAfterDelay();
                log("pollCallsAfterDelay");
                return;
            }
            Connection newRinging = null;
            ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList<>();
            Connection newUnknownConnectionCdma = null;
            boolean hasNonHangupStateChanged2 = false;
            boolean hasAnyCallDisconnected3 = false;
            boolean unknownConnectionAppeared2 = false;
            int handoverConnectionsSize = this.mHandoverConnections.size();
            boolean noConnectionExists2 = true;
            syncHoConnection();
            int i = 0;
            int curDC = 0;
            int dcSize = polledCalls.size();
            while (i < this.mConnections.length) {
                GsmCdmaConnection conn2 = this.mConnections[i];
                DriverCall driverCall = null;
                if (curDC < dcSize) {
                    DriverCall dc2 = (DriverCall) polledCalls.get(curDC);
                    DriverCall dc3 = dc2;
                    if (dc2.index == i + 1) {
                        curDC++;
                        dc = dc3;
                        List polledCalls2 = polledCalls;
                        conn = conn2;
                        if (!(conn == null && dc == null)) {
                            noConnectionExists2 = false;
                        }
                        StringBuilder sb = new StringBuilder();
                        Connection newUnknownConnectionCdma2 = newUnknownConnectionCdma;
                        sb.append("poll: conn[i=");
                        sb.append(i);
                        sb.append("]=");
                        sb.append(conn);
                        sb.append(", dc=");
                        sb.append(dc);
                        log(sb.toString());
                        if (dc == null && dc.state == DriverCall.State.ALERTING) {
                            hasAnyCallDisconnected2 = hasAnyCallDisconnected3;
                            unknownConnectionAppeared = unknownConnectionAppeared2;
                            noConnectionExists = noConnectionExists2;
                            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 0, LOG_TAG);
                        } else {
                            hasAnyCallDisconnected2 = hasAnyCallDisconnected3;
                            unknownConnectionAppeared = unknownConnectionAppeared2;
                            noConnectionExists = noConnectionExists2;
                        }
                        if (dc != null && dc.isMT && dc.state == DriverCall.State.ACTIVE && !this.mIsSrvccHappened) {
                            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 2, "RadioResponse");
                        }
                        if (conn != null && dc != null) {
                            if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc) || getHoConnection(dc) != null) {
                                log("pendingMo=" + this.mPendingMO + ", dc=" + dc);
                                this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                                Connection hoConnection = getHoConnection(dc);
                                log("[SRVCC] handlepollcall --> hoConnection:" + hoConnection);
                                if (hoConnection != null) {
                                    this.mConnections[i].setPostDialString(hoConnection.getOrigDialString());
                                    this.mConnections[i].migrateFrom(hoConnection);
                                    if (!(hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || dc.state != DriverCall.State.ACTIVE)) {
                                        Rlog.d(LOG_TAG, "[SRVCC] mConnections onConnectedInOrOut");
                                        this.mConnections[i].onConnectedInOrOut();
                                    }
                                    if (dc.state == DriverCall.State.ACTIVE || dc.state == DriverCall.State.HOLDING) {
                                        this.mConnections[i].releaseWakeLock();
                                    }
                                    this.mHandoverConnections.remove(hoConnection);
                                    this.mRemovedHandoverConnections.add(hoConnection);
                                    Rlog.d(LOG_TAG, "[SRVCC] notifyHandoverStateChanged mConnections[i]=" + this.mConnections[i]);
                                    Phone imsPhone = this.mPhone.getImsPhone();
                                    if (imsPhone != null) {
                                        imsPhone.notifyHandoverStateChanged(this.mConnections[i]);
                                    } else {
                                        this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                                    }
                                } else {
                                    newRinging = checkMtFindNewRinging(dc, i);
                                    if (newRinging == null) {
                                        if (isPhoneTypeGsm()) {
                                            newUnknownConnectionsGsm.add(this.mConnections[i]);
                                        } else {
                                            newUnknownConnectionCdma2 = this.mConnections[i];
                                        }
                                        unknownConnectionAppeared = true;
                                    }
                                }
                            } else {
                                log("poll: pendingMO=" + this.mPendingMO);
                                this.mConnections[i] = this.mPendingMO;
                                this.mPendingMO.mIndex = i;
                                this.mPendingMO.update(dc);
                                this.mPendingMO = null;
                                if (this.mHangupPendingMO) {
                                    this.mHangupPendingMO = false;
                                    if (!isPhoneTypeGsm() && this.mIsEcmTimerCanceled) {
                                        handleEcmTimer(0);
                                    }
                                    try {
                                        log("poll: hangupPendingMO, hangup conn " + i);
                                        hangup(this.mConnections[i]);
                                    } catch (CallStateException e) {
                                        Rlog.e(LOG_TAG, "unexpected error on hangup");
                                    }
                                }
                            }
                            hasNonHangupStateChanged = true;
                        } else if (conn == null && dc == null) {
                            if (isPhoneTypeGsm()) {
                                this.mDroppedDuringPoll.add(conn);
                            } else {
                                if (dcSize != 0) {
                                    log("conn != null, dc == null. Still have connections in the call list");
                                    this.mDroppedDuringPoll.add(conn);
                                } else {
                                    int count = this.mForegroundCall.mConnections.size();
                                    for (int n = 0; n < count; n++) {
                                        log("adding fgCall cn " + n + " to droppedDuringPoll");
                                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n));
                                    }
                                    int count2 = this.mRingingCall.mConnections.size();
                                    for (int n2 = 0; n2 < count2; n2++) {
                                        log("adding rgCall cn " + n2 + " to droppedDuringPoll");
                                        this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n2));
                                    }
                                }
                                if (this.mIsEcmTimerCanceled != 0) {
                                    handleEcmTimer(0);
                                }
                                checkAndEnableDataCallAfterEmergencyCallDropped();
                            }
                            this.mConnections[i] = null;
                            newUnknownConnectionCdma = newUnknownConnectionCdma2;
                            unknownConnectionAppeared2 = unknownConnectionAppeared;
                            i++;
                            polledCalls = polledCalls2;
                            hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
                            noConnectionExists2 = noConnectionExists;
                            AsyncResult asyncResult2 = ar;
                        } else if (conn != null || dc == null || conn.compareTo(dc) || !isPhoneTypeGsm()) {
                            if (!(conn == null || dc == null)) {
                                if (!isPhoneTypeGsm() || conn.isIncoming() == dc.isMT) {
                                    boolean changed = conn.update(dc);
                                    if (!hasNonHangupStateChanged2) {
                                        if (!changed) {
                                            z = false;
                                            hasNonHangupStateChanged = z;
                                            hasNonHangupStateChanged2 = hasNonHangupStateChanged;
                                        }
                                    }
                                    z = true;
                                    hasNonHangupStateChanged = z;
                                    hasNonHangupStateChanged2 = hasNonHangupStateChanged;
                                } else if (dc.isMT) {
                                    this.mDroppedDuringPoll.add(conn);
                                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                                    Connection newRinging2 = checkMtFindNewRinging(dc, i);
                                    if (newRinging2 == null) {
                                        newUnknownConnectionCdma = conn;
                                        unknownConnectionAppeared = true;
                                    } else {
                                        newUnknownConnectionCdma = newUnknownConnectionCdma2;
                                    }
                                    checkAndEnableDataCallAfterEmergencyCallDropped();
                                    newRinging = newRinging2;
                                    unknownConnectionAppeared2 = unknownConnectionAppeared;
                                    i++;
                                    polledCalls = polledCalls2;
                                    hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
                                    noConnectionExists2 = noConnectionExists;
                                    AsyncResult asyncResult22 = ar;
                                } else {
                                    Rlog.e(LOG_TAG, "Error in RIL, Phantom call appeared " + dc);
                                }
                            }
                            newUnknownConnectionCdma = newUnknownConnectionCdma2;
                            unknownConnectionAppeared2 = unknownConnectionAppeared;
                            i++;
                            polledCalls = polledCalls2;
                            hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
                            noConnectionExists2 = noConnectionExists;
                            AsyncResult asyncResult222 = ar;
                        } else {
                            this.mDroppedDuringPoll.add(conn);
                            this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                            if (this.mConnections[i].getCall() == this.mRingingCall) {
                                newRinging = this.mConnections[i];
                            }
                            hasNonHangupStateChanged = true;
                        }
                        hasNonHangupStateChanged2 = hasNonHangupStateChanged;
                        newUnknownConnectionCdma = newUnknownConnectionCdma2;
                        unknownConnectionAppeared2 = unknownConnectionAppeared;
                        i++;
                        polledCalls = polledCalls2;
                        hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
                        noConnectionExists2 = noConnectionExists;
                        AsyncResult asyncResult2222 = ar;
                    } else {
                        driverCall = null;
                    }
                }
                dc = driverCall;
                List polledCalls22 = polledCalls;
                conn = conn2;
                noConnectionExists2 = false;
                StringBuilder sb2 = new StringBuilder();
                Connection newUnknownConnectionCdma22 = newUnknownConnectionCdma;
                sb2.append("poll: conn[i=");
                sb2.append(i);
                sb2.append("]=");
                sb2.append(conn);
                sb2.append(", dc=");
                sb2.append(dc);
                log(sb2.toString());
                if (dc == null) {
                }
                hasAnyCallDisconnected2 = hasAnyCallDisconnected3;
                unknownConnectionAppeared = unknownConnectionAppeared2;
                noConnectionExists = noConnectionExists2;
                HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 2, "RadioResponse");
                if (conn != null) {
                }
                if (conn == null) {
                }
                if (conn != null) {
                }
                if (!isPhoneTypeGsm()) {
                }
                boolean changed2 = conn.update(dc);
                if (!hasNonHangupStateChanged2) {
                }
                z = true;
                hasNonHangupStateChanged = z;
                hasNonHangupStateChanged2 = hasNonHangupStateChanged;
                newUnknownConnectionCdma = newUnknownConnectionCdma22;
                unknownConnectionAppeared2 = unknownConnectionAppeared;
                i++;
                polledCalls = polledCalls22;
                hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
                noConnectionExists2 = noConnectionExists;
                AsyncResult asyncResult22222 = ar;
            }
            Connection newUnknownConnectionCdma3 = newUnknownConnectionCdma;
            boolean hasAnyCallDisconnected4 = hasAnyCallDisconnected3;
            boolean unknownConnectionAppeared3 = unknownConnectionAppeared2;
            if (!isPhoneTypeGsm() && noConnectionExists2) {
                checkAndEnableDataCallAfterEmergencyCallDropped();
            }
            if (this.mPendingMO != null) {
                Rlog.d(LOG_TAG, "Pending MO dropped before poll fg state:" + this.mForegroundCall.getState());
                this.mDroppedDuringPoll.add(this.mPendingMO);
                this.mPendingMO = null;
                this.mHangupPendingMO = false;
                if (!isPhoneTypeGsm()) {
                    if (this.mPendingCallInEcm) {
                        this.mPendingCallInEcm = false;
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                }
            }
            if (newRinging != null) {
                HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 1, LOG_TAG);
                this.mPhone.notifyNewRingingConnection(newRinging);
            }
            ArrayList<GsmCdmaConnection> locallyDisconnectedConnections = new ArrayList<>();
            Connection newUnknownConnectionCdma4 = newUnknownConnectionCdma3;
            for (int i2 = this.mDroppedDuringPoll.size() - 1; i2 >= 0; i2--) {
                GsmCdmaConnection conn3 = this.mDroppedDuringPoll.get(i2);
                boolean wasDisconnected = false;
                if (conn3.isIncoming() && conn3.getConnectTime() == 0) {
                    if (conn3.mCause == 3) {
                        cause = 16;
                    } else {
                        cause = 1;
                    }
                    log("missed/rejected call, conn.cause=" + conn3.mCause);
                    log("setting cause to " + cause);
                    this.mDroppedDuringPoll.remove(i2);
                    wasDisconnected = true;
                    locallyDisconnectedConnections.add(conn3);
                    hasAnyCallDisconnected4 |= conn3.onDisconnect(cause);
                } else if (conn3.mCause == 3 || conn3.mCause == 7) {
                    this.mDroppedDuringPoll.remove(i2);
                    wasDisconnected = true;
                    locallyDisconnectedConnections.add(conn3);
                    hasAnyCallDisconnected4 |= conn3.onDisconnect(conn3.mCause);
                }
                if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared3 && conn3 == newUnknownConnectionCdma4) {
                    newUnknownConnectionCdma4 = null;
                    unknownConnectionAppeared3 = false;
                }
            }
            if (locallyDisconnectedConnections.size() > 0) {
                this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), locallyDisconnectedConnections);
            }
            Iterator<Connection> it = this.mHandoverConnections.iterator();
            while (it.hasNext()) {
                Connection hoConnection2 = it.next();
                log("[SRVCC] handlePollCalls - disconnect hoConn= " + hoConnection2 + " hoConn.State= " + hoConnection2.getState());
                if (hoConnection2.getState().isRinging()) {
                    hasAnyCallDisconnected = hasAnyCallDisconnected4 | hoConnection2.onDisconnect(1);
                } else {
                    hasAnyCallDisconnected = hasAnyCallDisconnected4 | hoConnection2.onDisconnect(-1);
                }
                hasAnyCallDisconnected4 = hasAnyCallDisconnected;
                it.remove();
            }
            if (this.mDroppedDuringPoll.size() > 0) {
                this.mCi.getLastCallFailCause(obtainNoPollCompleteMessage(5));
            } else {
                this.mIsInCsRedial = false;
            }
            if (0 != 0) {
                pollCallsAfterDelay();
            }
            if (newRinging != null || hasNonHangupStateChanged2 || hasAnyCallDisconnected4) {
                internalClearDisconnected();
            }
            updatePhoneState();
            if (unknownConnectionAppeared3) {
                if (isPhoneTypeGsm()) {
                    Iterator<Connection> it2 = newUnknownConnectionsGsm.iterator();
                    while (it2.hasNext()) {
                        log("Notify unknown for " + c);
                        this.mPhone.notifyUnknownConnection(c);
                    }
                } else {
                    this.mPhone.notifyUnknownConnection(newUnknownConnectionCdma4);
                }
            }
            if (hasNonHangupStateChanged2 || newRinging != null || hasAnyCallDisconnected4) {
                this.mPhone.notifyPreciseCallStateChanged();
                updateMetrics(this.mConnections);
            }
            if (handoverConnectionsSize > 0 && this.mHandoverConnections.size() == 0) {
                Phone imsPhone2 = this.mPhone.getImsPhone();
                if (imsPhone2 != null) {
                    log("[SRVCC] handlePollCalls - handover connection mapped, clean HandoverCall.");
                    imsPhone2.callEndCleanupHandOverCallIfAny();
                }
            }
        }
    }

    private void updateMetrics(GsmCdmaConnection[] connections) {
        ArrayList<GsmCdmaConnection> activeConnections = new ArrayList<>();
        for (GsmCdmaConnection conn : connections) {
            if (conn != null) {
                activeConnections.add(conn);
            }
        }
        this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), activeConnections);
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        Rlog.i(LOG_TAG, "Phone State:" + this.mState);
        Rlog.i(LOG_TAG, "Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (int i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Foreground call: " + this.mForegroundCall.toString());
        List l2 = this.mForegroundCall.getConnections();
        int s2 = l2.size();
        for (int i2 = 0; i2 < s2; i2++) {
            Rlog.i(LOG_TAG, l2.get(i2).toString());
        }
        Rlog.i(LOG_TAG, "Background call: " + this.mBackgroundCall.toString());
        List l3 = this.mBackgroundCall.getConnections();
        int s3 = l3.size();
        for (int i3 = 0; i3 < s3; i3++) {
            Rlog.i(LOG_TAG, l3.get(i3).toString());
        }
    }

    public void hangup(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner == this) {
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
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), conn, conn.getGsmCdmaIndex());
                    this.mCi.hangupConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage());
                } catch (CallStateException e) {
                    Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: hangup() on absent connection " + conn);
                }
            }
            conn.onHangupLocal();
            GsmCdmaCall call = conn.getCall();
            if (call != null && call.getConnections().size() == 1) {
                call.setState(Call.State.DISCONNECTING);
            }
            Call.State state = conn.getState();
            if ((!this.mPhone.isPhoneTypeCdma() && !this.mPhone.isPhoneTypeCdmaLte()) || !this.mForegroundCall.isMultiparty()) {
                delaySendRilRecoveryMsg(state);
            }
            Call.State state2 = conn.getState();
            if (state2 == Call.State.IDLE || state2 == Call.State.DISCONNECTED) {
                cleanRilRecovery();
            }
            return;
        }
        throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
    }

    public void separate(GsmCdmaConnection conn) throws CallStateException {
        if (conn.mOwner == this) {
            try {
                this.mCi.separateConnection(conn.getGsmCdmaIndex(), obtainCompleteMessage(12));
            } catch (CallStateException e) {
                Rlog.w(LOG_TAG, "GsmCdmaCallTracker WARN: separate() on absent connection " + conn);
            }
        } else {
            throw new CallStateException("GsmCdmaConnection " + conn + "does not belong to GsmCdmaCallTracker " + this);
        }
    }

    public void setMute(boolean mute) {
        Bundle restrictions = ((UserManager) this.mPhone.getContext().getSystemService("user")).getUserRestrictions();
        if (restrictions == null || !restrictions.getBoolean("no_unmute_microphone", false)) {
            this.mDesiredMute = mute;
        } else {
            this.mDesiredMute = true;
        }
        this.mCi.setMute(this.mDesiredMute, null);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void hangup(GsmCdmaCall call) throws CallStateException {
        if (call.getConnections().size() != 0) {
            if (call == this.mRingingCall) {
                log("(ringing) hangup waiting or background");
                logHangupEvent(call);
                if (!IS_SUPPORT_RIL_RECOVERY) {
                    this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
                } else if (this.mHwGCT == null || this.mHwGCT.getRejectCallCause(call) == -1) {
                    hangupAllConnections(call);
                } else {
                    log("rejectCallForCause !!!");
                    this.mHwGCT.rejectCallForCause(this.mCi, call, obtainCompleteMessage());
                }
                delaySendRilRecoveryMsg(call.getState());
            } else if (call == this.mForegroundCall) {
                if (call.isDialingOrAlerting()) {
                    log("(foregnd) hangup dialing or alerting...");
                    hangup((GsmCdmaConnection) call.getConnections().get(0));
                } else if (!isPhoneTypeGsm() || !this.mRingingCall.isRinging()) {
                    logHangupEvent(call);
                    hangupForegroundResumeBackground();
                    delaySendRilRecoveryMsg(call.getState());
                } else {
                    log("hangup all conns in active/background call, without affecting ringing call");
                    hangupAllConnections(call);
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
            return;
        }
        throw new CallStateException("no connections in call");
    }

    private void logHangupEvent(GsmCdmaCall call) {
        int call_index;
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            try {
                call_index = cn.getGsmCdmaIndex();
            } catch (CallStateException e) {
                call_index = -1;
            }
            this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, call_index);
        }
    }

    public void hangupWaitingOrBackground() {
        log("hangupWaitingOrBackground");
        logHangupEvent(this.mBackgroundCall);
        this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
    }

    public void hangupForegroundResumeBackground() {
        log("hangupForegroundResumeBackground");
        this.mCi.hangupForegroundResumeBackground(obtainCompleteMessage());
    }

    public void hangupConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            try {
                if (!cn.mDisconnected && cn.getGsmCdmaIndex() == index) {
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex());
                    this.mCi.hangupConnection(index, obtainCompleteMessage());
                    return;
                }
            } catch (CallStateException ex) {
                Rlog.e(LOG_TAG, "hangupConnectionByIndex, caught " + ex);
            }
        }
        throw new CallStateException("no GsmCdma index found");
    }

    public void hangupAllConnections(GsmCdmaCall call) {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            try {
                if (!cn.mDisconnected) {
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex());
                    this.mCi.hangupConnection(cn.getGsmCdmaIndex(), obtainCompleteMessage());
                }
            } catch (CallStateException ex) {
                Rlog.e(LOG_TAG, "hangupConnectionByIndex caught " + ex);
            }
        }
    }

    public GsmCdmaConnection getConnectionByIndex(GsmCdmaCall call, int index) throws CallStateException {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            GsmCdmaConnection cn = (GsmCdmaConnection) call.mConnections.get(i);
            if (!cn.mDisconnected && cn.getGsmCdmaIndex() == index) {
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
        if (this.mRingingCall.mConnections.size() > 0) {
            if (((GsmCdmaConnection) this.mRingingCall.getLatestConnection()).isNewConnection(cw)) {
                Rlog.i(LOG_TAG, "Update the ringing connection if the call is from another number.");
                notifyWaitingNumberChanged();
                new GsmCdmaConnection(this.mPhone.getContext(), cw, this, this.mRingingCall);
            }
        } else if (this.mRingingCall.isTooFrequency(cw.number)) {
            this.mRingingCall.setLastRingNumberAndChangeTime(cw.number);
            Rlog.i(LOG_TAG, "ringing too frequency, ignore this callwaiting message.");
            return;
        } else {
            Rlog.i(LOG_TAG, "new callwaiting message, create a connection");
            new GsmCdmaConnection(this.mPhone.getContext(), cw, this, this.mRingingCall);
        }
        updatePhoneState();
        notifyCallWaitingInfo(cw);
    }

    private PhoneInternalInterface.SuppService getFailedService(int what) {
        if (what == 8) {
            return PhoneInternalInterface.SuppService.SWITCH;
        }
        switch (what) {
            case 11:
                return PhoneInternalInterface.SuppService.CONFERENCE;
            case 12:
                return PhoneInternalInterface.SuppService.SEPARATE;
            case 13:
                return PhoneInternalInterface.SuppService.TRANSFER;
            default:
                return PhoneInternalInterface.SuppService.UNKNOWN;
        }
    }

    public void handleMessage(Message msg) {
        int causeCode;
        int i = msg.what;
        if (i != 20) {
            if (i == 50) {
                Rlog.d(LOG_TAG, "Event EVENT_RSRVCC_STATE_CHANGED Received");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mIsInCsRedial = true;
                    return;
                }
                Rlog.e(LOG_TAG, "RSrvcc exception: " + ar.exception);
            } else if (i != 201) {
                switch (i) {
                    case 1:
                        Rlog.d(LOG_TAG, "Event EVENT_POLL_CALLS_RESULT Received");
                        if (msg == this.mLastRelevantPoll) {
                            log("handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                            this.mNeedsPoll = false;
                            this.mLastRelevantPoll = null;
                            handlePollCalls((AsyncResult) msg.obj);
                            return;
                        }
                        return;
                    case 2:
                    case 3:
                        pollCallsWhenSafe();
                        return;
                    case 4:
                        operationComplete();
                        return;
                    case 5:
                        String vendorCause = null;
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        operationComplete();
                        if (ar2.exception != null) {
                            causeCode = 16;
                            Rlog.i(LOG_TAG, "Exception during getLastCallFailCause, assuming normal disconnect");
                        } else {
                            LastCallFailCause failCause = (LastCallFailCause) ar2.result;
                            int causeCode2 = failCause.causeCode;
                            vendorCause = failCause.vendorCause;
                            causeCode = causeCode2;
                        }
                        if (causeCode == 34 || causeCode == 41 || causeCode == 42 || causeCode == 44 || causeCode == 49 || causeCode == 58 || causeCode == 65535) {
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
                        }
                        int s = this.mDroppedDuringPoll.size();
                        for (int i2 = 0; i2 < s; i2++) {
                            this.mDroppedDuringPoll.get(i2).onRemoteDisconnect(causeCode, vendorCause);
                        }
                        updatePhoneState();
                        this.mPhone.notifyPreciseCallStateChanged();
                        this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), this.mDroppedDuringPoll);
                        this.mDroppedDuringPoll.clear();
                        this.mIsInCsRedial = false;
                        return;
                    default:
                        switch (i) {
                            case 8:
                                if (isPhoneTypeGsm()) {
                                    this.callSwitchPending = false;
                                    if (((AsyncResult) msg.obj).exception != null) {
                                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                                    }
                                    operationComplete();
                                    return;
                                }
                                return;
                            case 9:
                                handleRadioAvailable();
                                return;
                            case 10:
                                handleRadioNotAvailable();
                                return;
                            case 11:
                                if (isPhoneTypeGsm()) {
                                    if (!(((AsyncResult) msg.obj).exception == null || ((AsyncResult) msg.obj).exception == null)) {
                                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                                        List<Connection> conn = this.mForegroundCall.getConnections();
                                        if (conn != null) {
                                            Rlog.d(LOG_TAG, "Notify merge failure");
                                            if (conn.size() != 0) {
                                                conn.get(0).onConferenceMergeFailed();
                                            }
                                        }
                                    }
                                    operationComplete();
                                    return;
                                }
                                return;
                            case 12:
                            case 13:
                                if (isPhoneTypeGsm()) {
                                    if (((AsyncResult) msg.obj).exception != null) {
                                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                                    }
                                    operationComplete();
                                    return;
                                } else if (msg.what != 8) {
                                    throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                                } else {
                                    return;
                                }
                            case 14:
                                if (!isPhoneTypeGsm()) {
                                    if (this.mPendingCallInEcm) {
                                        this.mCi.dial(this.mPendingMO.getDialAddress(), this.mPendingCallClirMode, obtainCompleteMessage());
                                        this.mPendingCallInEcm = false;
                                    }
                                    this.mPhone.unsetOnEcbModeExitResponse(this);
                                    return;
                                }
                                throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                            case 15:
                                if (!isPhoneTypeGsm()) {
                                    AsyncResult ar3 = (AsyncResult) msg.obj;
                                    if (ar3.exception == null) {
                                        handleCallWaitingInfo((CdmaCallWaitingNotification) ar3.result);
                                        Rlog.d(LOG_TAG, "Event EVENT_CALL_WAITING_INFO_CDMA Received");
                                        return;
                                    }
                                    return;
                                }
                                throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                            case 16:
                                if (isPhoneTypeGsm()) {
                                    throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                                } else if (((AsyncResult) msg.obj).exception == null && this.mPendingMO != null) {
                                    this.mPendingMO.onConnectedInOrOut();
                                    this.mPendingMO = null;
                                    return;
                                } else {
                                    return;
                                }
                            default:
                                throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                        }
                }
            } else if (CallTracker.IS_SUPPORT_RIL_RECOVERY) {
                SystemProperties.set("ril.reset.write_dump", "true");
                log("restartRild for hungup call,ril.reset.write_dump=" + SystemProperties.get("ril.reset.write_dump"));
                this.mCi.restartRild(null);
            }
        } else if (isPhoneTypeGsm()) {
            throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
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
    }

    private void checkAndEnableDataCallAfterEmergencyCallDropped() {
        if (this.mIsInEmergencyCall) {
            this.mIsInEmergencyCall = false;
            boolean inEcm = this.mPhone.isInEcm();
            log("checkAndEnableDataCallAfterEmergencyCallDropped,isInEcm=" + inEcm);
            if (!inEcm) {
                this.mPhone.mDcTracker.setInternalDataEnabled(true);
                this.mPhone.notifyEmergencyCallRegistrants(false);
            }
            this.mPhone.sendEmergencyCallStateChange(false);
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
        return this.mPhone.getPhoneType() == 1;
    }

    public GsmCdmaPhone getPhone() {
        return this.mPhone;
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("GsmCdmaCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println("mConnections: length=" + this.mConnections.length);
        for (int i = 0; i < this.mConnections.length; i++) {
            pw.printf("  mConnections[%d]=%s\n", new Object[]{Integer.valueOf(i), this.mConnections[i]});
        }
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        if (!isPhoneTypeGsm()) {
            pw.println(" mCallWaitingRegistrants=" + this.mCallWaitingRegistrants);
        }
        pw.println(" mDroppedDuringPoll: size=" + this.mDroppedDuringPoll.size());
        for (int i2 = 0; i2 < this.mDroppedDuringPoll.size(); i2++) {
            pw.printf("  mDroppedDuringPoll[%d]=%s\n", new Object[]{Integer.valueOf(i2), this.mDroppedDuringPoll.get(i2)});
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

    public PhoneConstants.State getState() {
        return this.mState;
    }

    public int getMaxConnectionsPerCall() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return 5;
        }
        return 1;
    }

    public void cleanupCalls() {
        pollCallsWhenSafe();
    }

    public boolean isInCsRedial() {
        return this.mIsInCsRedial;
    }

    private void syncHoConnection() {
        Phone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone != null && this.mSrvccState == Call.SrvccState.STARTED) {
            ArrayList<Connection> hoConnections = imsPhone.getHandoverConnection();
            if (hoConnections != null) {
                int list_size = hoConnections.size();
                for (int i = 0; i < list_size; i++) {
                    Connection conn = hoConnections.get(i);
                    if (!this.mHandoverConnections.contains(conn) && !this.mRemovedHandoverConnections.contains(conn)) {
                        this.mHandoverConnections.add(conn);
                    }
                }
            }
        }
    }

    public void markCallRejectCause(String telecomCallId, int cause) {
        log("markCallRejectByUser, telecomCallId: " + telecomCallId + ", cause:" + cause);
        if (this.mHwGCT == null) {
            log("mHwGCT is null!");
        } else {
            this.mHwGCT.markCallRejectCause(telecomCallId, cause);
        }
    }

    private boolean custCanDial() {
        if (this.mHwGCT != null) {
            return this.mHwGCT.canDial();
        }
        return true;
    }
}

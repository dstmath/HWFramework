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
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
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
    public GsmCdmaConnection[] mConnections;
    private boolean mDesiredMute = false;
    private ArrayList<GsmCdmaConnection> mDroppedDuringPoll = new ArrayList(19);
    private BroadcastReceiver mEcmExitReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                boolean isInEcm = intent.getBooleanExtra("phoneinECMState", false);
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
    public GsmCdmaCall mForegroundCall = new GsmCdmaCall(this);
    private boolean mHangupPendingMO;
    private HwCustGsmCdmaCallTracker mHwGCT;
    private boolean mIsEcmTimerCanceled;
    private boolean mIsInCsRedial = false;
    private boolean mIsInEmergencyCall;
    private TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    private int mPendingCallClirMode;
    private boolean mPendingCallInEcm;
    private GsmCdmaConnection mPendingMO;
    public GsmCdmaPhone mPhone;
    public GsmCdmaCall mRingingCall = new GsmCdmaCall(this);
    public State mState = State.IDLE;
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
        this.mHwGCT = (HwCustGsmCdmaCallTracker) HwCustUtils.createObj(HwCustGsmCdmaCallTracker.class, new Object[0]);
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
            this.mPendingMO.dispose();
        }
        this.mConnections = null;
        this.mPendingMO = null;
        clearDisconnected();
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
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) connCopy.get(i)).fakeHoldBeforeDial();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00a5 A:{Catch:{ InterruptedException -> 0x0059 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString));
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
        clearDisconnected();
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 0, "AP_FLOW_SUC");
        if (canDial()) {
            int internationalRoaming;
            TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
            String origNumber = dialString;
            String operatorIsoContry = tm.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
            String simIsoContry = tm.getSimCountryIsoForPhone(this.mPhone.getPhoneId());
            if (TextUtils.isEmpty(operatorIsoContry) || (TextUtils.isEmpty(simIsoContry) ^ 1) == 0) {
                internationalRoaming = 0;
            } else {
                internationalRoaming = simIsoContry.equals(operatorIsoContry) ^ 1;
            }
            if (internationalRoaming != 0) {
                if ("us".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming != 0 ? "vi".equals(operatorIsoContry) ^ 1 : 0;
                } else if ("vi".equals(simIsoContry)) {
                    internationalRoaming = internationalRoaming != 0 ? "us".equals(operatorIsoContry) ^ 1 : 0;
                }
            }
            if (internationalRoaming != 0) {
                dialString = convertNumberIfNecessary(this.mPhone, dialString);
            }
            boolean isPhoneInEcmMode = this.mPhone.isInEcm();
            boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
            if (isPhoneInEcmMode && isEmergencyCall) {
                handleEcmTimer(1);
            }
            if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                return dialThreeWay(dialString);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyCall);
            this.mHangupPendingMO = false;
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(false);
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
                this.mNumberConverted = false;
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
        this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, this.mIsInEmergencyCall);
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
        if (this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING && (this.mBackgroundCall.isFull() ^ 1) != 0) {
            return this.mForegroundCall.isFull() ^ 1;
        }
        return false;
    }

    private boolean canDial() {
        boolean ret;
        boolean z = true;
        int serviceState = this.mPhone.getServiceState().getState();
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        Call.State fgCallState = this.mForegroundCall.getState();
        Call.State bgCallState = this.mBackgroundCall.getState();
        if (serviceState == 3 || this.mPendingMO != null || (this.mRingingCall.isRinging() ^ 1) == 0 || (disableCall.equals("true") ^ 1) == 0) {
            ret = false;
        } else if (isPhoneTypeGsm() && ((fgCallState == Call.State.IDLE || fgCallState == Call.State.DISCONNECTED || fgCallState == Call.State.ACTIVE) && (bgCallState == Call.State.IDLE || bgCallState == Call.State.DISCONNECTED || bgCallState == Call.State.HOLDING))) {
            ret = true;
        } else if (isPhoneTypeGsm()) {
            ret = false;
        } else if (this.mForegroundCall.getState() == Call.State.ACTIVE || (this.mForegroundCall.getState().isAlive() ^ 1) != 0) {
            ret = true;
        } else {
            ret = this.mBackgroundCall.getState().isAlive() ^ 1;
        }
        if (!ret) {
            boolean z2;
            String str = "canDial is false\n((serviceState=%d) != ServiceState.STATE_POWER_OFF)::=%s\n&& pendingMO == null::=%s\n&& !ringingCall.isRinging()::=%s\n&& !disableCall.equals(\"true\")::=%s\n&& (!foregroundCall.getState().isAlive()::=%s\n   || foregroundCall.getState() == GsmCdmaCall.State.ACTIVE::=%s\n   ||!backgroundCall.getState().isAlive())::=%s)";
            Object[] objArr = new Object[8];
            objArr[0] = Integer.valueOf(serviceState);
            if (serviceState != 3) {
                z2 = true;
            } else {
                z2 = false;
            }
            objArr[1] = Boolean.valueOf(z2);
            if (this.mPendingMO == null) {
                z2 = true;
            } else {
                z2 = false;
            }
            objArr[2] = Boolean.valueOf(z2);
            objArr[3] = Boolean.valueOf(this.mRingingCall.isRinging() ^ 1);
            objArr[4] = Boolean.valueOf(disableCall.equals("true") ^ 1);
            objArr[5] = Boolean.valueOf(this.mForegroundCall.getState().isAlive() ^ 1);
            if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
                z = false;
            }
            objArr[6] = Boolean.valueOf(z);
            objArr[7] = Boolean.valueOf(this.mBackgroundCall.getState().isAlive() ^ 1);
            log(String.format(str, objArr));
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
        } else {
            if (this.mPendingMO == null) {
                int isIdle;
                if (this.mForegroundCall.isIdle()) {
                    isIdle = this.mBackgroundCall.isIdle();
                } else {
                    isIdle = 0;
                }
                if ((isIdle ^ 1) == 0) {
                    Phone imsPhone = this.mPhone.getImsPhone();
                    if (imsPhone != null) {
                        imsPhone.callEndCleanupHandOverCallIfAny();
                    }
                    this.mState = State.IDLE;
                }
            }
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
        if (this.mState != oldState || (this.mIsSrvccHappened && State.IDLE == this.mState)) {
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            if (this.mIsSrvccHappened) {
                this.mIsSrvccHappened = false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:201:0x0770, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void handlePollCalls(AsyncResult ar) {
        if (HwDeviceManager.disallowOp(1)) {
            Rlog.i(LOG_TAG, "HwDeviceManager disallow open call.");
            return;
        }
        List polledCalls;
        int i;
        Connection conn;
        Connection hoConnection;
        if (ar.exception == null) {
            polledCalls = ar.result;
        } else if (VSimUtilsInner.isVSimPhone(this.mPhone)) {
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
        Connection newRinging = null;
        ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList();
        Connection newUnknownConnectionCdma = null;
        boolean hasNonHangupStateChanged = false;
        int hasAnyCallDisconnected = 0;
        boolean unknownConnectionAppeared = false;
        int handoverConnectionsSize = this.mHandoverConnections.size();
        boolean noConnectionExists = true;
        syncHoConnection();
        int curDC = 0;
        int dcSize = polledCalls.size();
        for (i = 0; i < this.mConnections.length; i++) {
            conn = this.mConnections[i];
            DriverCall dc = null;
            if (curDC < dcSize) {
                dc = (DriverCall) polledCalls.get(curDC);
                if (dc.index == i + 1) {
                    curDC++;
                } else {
                    dc = null;
                }
            }
            if (!(conn == null && dc == null)) {
                noConnectionExists = false;
            }
            if (conn == null && dc != null) {
                if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc)) {
                    log("pendingMo=" + this.mPendingMO + ", dc=" + dc);
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    hoConnection = getHoConnection(dc);
                    if (hoConnection != null) {
                        Rlog.d(LOG_TAG, "hoConnection is not null");
                        this.mConnections[i].setPostDialString(hoConnection.getOrigDialString());
                        this.mConnections[i].migrateFrom(hoConnection);
                        if (!(hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || dc.state != DriverCall.State.ACTIVE)) {
                            Rlog.d(LOG_TAG, "mConnections onConnectedInOrOut");
                            this.mConnections[i].onConnectedInOrOut();
                        }
                        this.mHandoverConnections.remove(hoConnection);
                        this.mRemovedHandoverConnections.add(hoConnection);
                        Rlog.d(LOG_TAG, "notifyHandoverStateChanged mConnections[i]=" + this.mConnections[i]);
                        this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                    } else {
                        newRinging = checkMtFindNewRinging(dc, i);
                        if (newRinging == null) {
                            unknownConnectionAppeared = true;
                            if (isPhoneTypeGsm()) {
                                newUnknownConnectionsGsm.add(this.mConnections[i]);
                            } else {
                                newUnknownConnectionCdma = this.mConnections[i];
                            }
                        }
                    }
                } else {
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
            } else if (conn != null && dc == null) {
                if (isPhoneTypeGsm()) {
                    this.mDroppedDuringPoll.add(conn);
                } else {
                    if (dcSize != 0) {
                        log("conn != null, dc == null. Still have connections in the call list");
                        this.mDroppedDuringPoll.add(conn);
                    } else {
                        int n;
                        int count = this.mForegroundCall.mConnections.size();
                        for (n = 0; n < count; n++) {
                            log("adding fgCall cn " + n + " to droppedDuringPoll");
                            this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mForegroundCall.mConnections.get(n));
                        }
                        count = this.mRingingCall.mConnections.size();
                        for (n = 0; n < count; n++) {
                            log("adding rgCall cn " + n + " to droppedDuringPoll");
                            this.mDroppedDuringPoll.add((GsmCdmaConnection) this.mRingingCall.mConnections.get(n));
                        }
                    }
                    if (this.mIsEcmTimerCanceled) {
                        handleEcmTimer(0);
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                }
                this.mConnections[i] = null;
            } else if (conn != null && dc != null && (conn.compareTo(dc) ^ 1) != 0 && isPhoneTypeGsm()) {
                this.mDroppedDuringPoll.add(conn);
                this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                if (this.mConnections[i].getCall() == this.mRingingCall) {
                    newRinging = this.mConnections[i];
                }
                hasNonHangupStateChanged = true;
            } else if (!(conn == null || dc == null)) {
                if (isPhoneTypeGsm() || conn.isIncoming() == dc.isMT) {
                    hasNonHangupStateChanged = !hasNonHangupStateChanged ? conn.update(dc) : true;
                } else if (dc.isMT) {
                    this.mDroppedDuringPoll.add(conn);
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    newRinging = checkMtFindNewRinging(dc, i);
                    if (newRinging == null) {
                        unknownConnectionAppeared = true;
                        newUnknownConnectionCdma = conn;
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                } else {
                    Rlog.e(LOG_TAG, "Error in RIL, Phantom call appeared " + dc);
                }
            }
        }
        if (!isPhoneTypeGsm() && noConnectionExists) {
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
        ArrayList<GsmCdmaConnection> locallyDisconnectedConnections = new ArrayList();
        for (i = this.mDroppedDuringPoll.size() - 1; i >= 0; i--) {
            conn = (GsmCdmaConnection) this.mDroppedDuringPoll.get(i);
            boolean wasDisconnected = false;
            if (conn.isIncoming() && conn.getConnectTime() == 0) {
                int cause;
                if (conn.mCause == 3) {
                    cause = 16;
                } else {
                    cause = 1;
                }
                log("missed/rejected call, conn.cause=" + conn.mCause);
                log("setting cause to " + cause);
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn.onDisconnect(cause);
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn);
            } else if (conn.mCause == 3 || conn.mCause == 7) {
                this.mDroppedDuringPoll.remove(i);
                hasAnyCallDisconnected |= conn.onDisconnect(conn.mCause);
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn);
            }
            if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared && conn == newUnknownConnectionCdma) {
                unknownConnectionAppeared = false;
                newUnknownConnectionCdma = null;
            }
        }
        if (locallyDisconnectedConnections.size() > 0) {
            this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), locallyDisconnectedConnections);
        }
        Iterator<Connection> it = this.mHandoverConnections.iterator();
        while (it.hasNext()) {
            int onDisconnect;
            hoConnection = (Connection) it.next();
            log("handlePollCalls - disconnect hoConn= " + hoConnection + " hoConn.State= " + hoConnection.getState());
            if (hoConnection.getState().isRinging()) {
                onDisconnect = hoConnection.onDisconnect(1);
            } else {
                onDisconnect = hoConnection.onDisconnect(-1);
            }
            hasAnyCallDisconnected |= onDisconnect;
            it.remove();
        }
        if (this.mDroppedDuringPoll.size() > 0) {
            this.mCi.getLastCallFailCause(obtainNoPollCompleteMessage(5));
        } else {
            this.mIsInCsRedial = false;
        }
        if (false) {
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
        if (!(!hasNonHangupStateChanged && newRinging == null && hasAnyCallDisconnected == 0)) {
            this.mPhone.notifyPreciseCallStateChanged();
            updateMetrics(this.mConnections);
        }
        if (handoverConnectionsSize > 0 && this.mHandoverConnections.size() == 0) {
            Phone imsPhone = this.mPhone.getImsPhone();
            if (imsPhone != null) {
                imsPhone.callEndCleanupHandOverCallIfAny();
            }
        }
    }

    private void updateMetrics(GsmCdmaConnection[] connections) {
        ArrayList<GsmCdmaConnection> activeConnections = new ArrayList();
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
        int i;
        Rlog.i(LOG_TAG, "Phone State:" + this.mState);
        Rlog.i(LOG_TAG, "Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Foreground call: " + this.mForegroundCall.toString());
        l = this.mForegroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
            Rlog.i(LOG_TAG, l.get(i).toString());
        }
        Rlog.i(LOG_TAG, "Background call: " + this.mBackgroundCall.toString());
        l = this.mBackgroundCall.getConnections();
        s = l.size();
        for (i = 0; i < s; i++) {
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
        boolean isMultiparty = (this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) ? this.mForegroundCall.isMultiparty() : false;
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
            } else if (isPhoneTypeGsm() && this.mRingingCall.isRinging()) {
                log("hangup all conns in active/background call, without affecting ringing call");
                hangupAllConnections(call);
            } else {
                logHangupEvent(call);
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

    private void logHangupEvent(GsmCdmaCall call) {
        int count = call.mConnections.size();
        for (int i = 0; i < count; i++) {
            int call_index;
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
            case 8:
                return SuppService.SWITCH;
            case 11:
                return SuppService.CONFERENCE;
            case 12:
                return SuppService.SEPARATE;
            case 13:
                return SuppService.TRANSFER;
            default:
                return SuppService.UNKNOWN;
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
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
                int causeCode;
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
                for (int i = 0; i < s; i++) {
                    ((GsmCdmaConnection) this.mDroppedDuringPoll.get(i)).onRemoteDisconnect(causeCode, vendorCause);
                }
                this.mIsInCsRedial = false;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), this.mDroppedDuringPoll);
                this.mDroppedDuringPoll.clear();
                return;
            case 8:
                if (isPhoneTypeGsm()) {
                    this.callSwitchPending = false;
                    if (msg.obj.exception != null) {
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
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                } else {
                    return;
                }
            case 14:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                }
                if (this.mPendingCallInEcm) {
                    this.mCi.dial(this.mPendingMO.getDialAddress(), this.mPendingCallClirMode, obtainCompleteMessage());
                    this.mPendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
                return;
            case 15:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                }
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    handleCallWaitingInfo((CdmaCallWaitingNotification) ar.result);
                    Rlog.d(LOG_TAG, "Event EVENT_CALL_WAITING_INFO_CDMA Received");
                    return;
                }
                return;
            case 16:
                if (isPhoneTypeGsm()) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
                } else if (((AsyncResult) msg.obj).exception == null && this.mPendingMO != null) {
                    this.mPendingMO.onConnectedInOrOut();
                    this.mPendingMO = null;
                    return;
                } else {
                    return;
                }
            case 20:
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
                    return;
                } else {
                    this.mPendingMO = null;
                    Rlog.w(LOG_TAG, "exception happened on Blank Flash for 3-way call");
                    return;
                }
            case 50:
                Rlog.d(LOG_TAG, "Event EVENT_RSRVCC_STATE_CHANGED Received");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mIsInCsRedial = true;
                    return;
                } else {
                    Rlog.e(LOG_TAG, "RSrvcc exception: " + ar.exception);
                    return;
                }
            case 201:
                if (CallTracker.IS_SUPPORT_RIL_RECOVERY) {
                    SystemProperties.set("ril.reset.write_dump", "true");
                    log("restartRild for hungup call,ril.reset.write_dump=" + SystemProperties.get("ril.reset.write_dump"));
                    this.mCi.restartRild(null);
                    return;
                }
                return;
            default:
                throw new RuntimeException("unexpected event " + msg.what + " not handled by " + "phone type " + this.mPhone.getPhoneType());
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

    protected void log(String msg) {
        Rlog.d(LOG_TAG, "[GsmCdmaCallTracker] " + msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("GsmCdmaCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println("mConnections: length=" + this.mConnections.length);
        for (i = 0; i < this.mConnections.length; i++) {
            pw.printf("  mConnections[%d]=%s\n", new Object[]{Integer.valueOf(i), this.mConnections[i]});
        }
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        if (!isPhoneTypeGsm()) {
            pw.println(" mCallWaitingRegistrants=" + this.mCallWaitingRegistrants);
        }
        pw.println(" mDroppedDuringPoll: size=" + this.mDroppedDuringPoll.size());
        for (i = 0; i < this.mDroppedDuringPoll.size(); i++) {
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
            return 5;
        }
        return 1;
    }

    public boolean isInCsRedial() {
        return this.mIsInCsRedial;
    }

    private void syncHoConnection() {
        Phone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone != null && this.mSrvccState == SrvccState.STARTED) {
            ArrayList<Connection> hoConnections = imsPhone.getHandoverConnection();
            if (hoConnections != null) {
                int list_size = hoConnections.size();
                for (int i = 0; i < list_size; i++) {
                    boolean z;
                    Connection conn = (Connection) hoConnections.get(i);
                    if (this.mHandoverConnections.contains(conn)) {
                        z = true;
                    } else {
                        z = this.mRemovedHandoverConnections.contains(conn);
                    }
                    if (!z) {
                        this.mHandoverConnections.add(conn);
                    }
                }
            }
        }
    }
}

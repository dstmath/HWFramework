package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.ArraySet;
import android.util.EventLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.DriverCall;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.internal.telephony.GsmCdmaConnectionEx;
import com.huawei.internal.telephony.PhoneExt;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GsmCdmaCallTracker extends CallTracker implements IGsmCdmaCallTrackerInner {
    private static final boolean DBG_POLL = false;
    private static final String LOG_TAG = "GsmCdmaCallTracker";
    private static final int MAX_CONNECTIONS_CDMA = 8;
    public static final int MAX_CONNECTIONS_GSM = 19;
    private static final int MAX_CONNECTIONS_PER_CALL_CDMA = 1;
    private static final int MAX_CONNECTIONS_PER_CALL_GSM = 5;
    private static final boolean REPEAT_POLLING = false;
    private static final boolean VDBG = false;
    RegistrantList cdmaWaitingNumberChangedRegistrants = new RegistrantList();
    private int m3WayCallFlashDelay;
    @UnsupportedAppUsage
    public GsmCdmaCall mBackgroundCall = new GsmCdmaCall(this);
    private Object mCHRLock = new Object();
    private RegistrantList mCallWaitingRegistrants = new RegistrantList();
    @VisibleForTesting
    public GsmCdmaConnection[] mConnections;
    private boolean mDesiredMute = false;
    private ArrayList<GsmCdmaConnection> mDroppedDuringPoll = new ArrayList<>(19);
    private BroadcastReceiver mEcmExitReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.GsmCdmaCallTracker.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
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
    @UnsupportedAppUsage
    public GsmCdmaCall mForegroundCall = new GsmCdmaCall(this);
    private boolean mHangupPendingMO;
    private HwCustGsmCdmaCallTracker mHwGCT;
    private IHwGsmCdmaCallTrackerEx mHwGsmCdmaCallTrackerEx = null;
    private boolean mIsEcmTimerCanceled;
    private boolean mIsInCsRedial = false;
    private boolean mIsInEmergencyCall;
    private TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    private int mPendingCallClirMode;
    private boolean mPendingCallInEcm;
    @UnsupportedAppUsage
    private GsmCdmaConnection mPendingMO;
    @UnsupportedAppUsage
    private GsmCdmaPhone mPhone;
    private Set<Integer> mPrintedAnswerCHRCallId = new ArraySet();
    @UnsupportedAppUsage
    public GsmCdmaCall mRingingCall = new GsmCdmaCall(this);
    @UnsupportedAppUsage
    public PhoneConstants.State mState = PhoneConstants.State.IDLE;
    private RegistrantList mVoiceCallEndedRegistrants = new RegistrantList();
    private RegistrantList mVoiceCallStartedRegistrants = new RegistrantList();

    public GsmCdmaCallTracker(GsmCdmaPhone phone) {
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
        this.mHwGsmCdmaCallTrackerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwGsmCdmaCallTrackerEx(this);
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
                this.mPhone.getDataEnabledSettings().setInternalDataEnabled(true);
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
        Rlog.i(LOG_TAG, "reset");
        GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
        for (GsmCdmaConnection gsmCdmaConnection : gsmCdmaConnectionArr) {
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.onDisconnect(36);
                gsmCdmaConnection.dispose();
            }
        }
        GsmCdmaConnection gsmCdmaConnection2 = this.mPendingMO;
        if (gsmCdmaConnection2 != null) {
            gsmCdmaConnection2.onDisconnect(36);
            this.mPendingMO.dispose();
        }
        this.mConnections = null;
        this.mPendingMO = null;
        clearDisconnected();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        Rlog.i(LOG_TAG, "GsmCdmaCallTracker finalized");
    }

    @Override // com.android.internal.telephony.CallTracker
    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceCallStartedRegistrants.add(r);
        if (this.mState != PhoneConstants.State.IDLE) {
            r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    @Override // com.android.internal.telephony.CallTracker
    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CallTracker
    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CallTracker
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

    @UnsupportedAppUsage
    private void fakeHoldForegroundBeforeDial() {
        List<Connection> connCopy = (List) this.mForegroundCall.mConnections.clone();
        int s = connCopy.size();
        for (int i = 0; i < s; i++) {
            ((GsmCdmaConnection) connCopy.get(i)).fakeHoldBeforeDial();
        }
    }

    public synchronized Connection dialGsm(String dialString, int clirMode, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        clearDisconnected();
        boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
        checkForDialIssues(isEmergencyCall);
        String dialString2 = convertNumberIfNecessary(this.mPhone, dialString);
        if (this.mHwCT != null) {
            if (this.mHwCT.isBlockDialing(dialString2, this.mPhone.getPhoneId())) {
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
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, isEmergencyCall);
            if (intentExtras != null) {
                Rlog.i(LOG_TAG, "dialGsm - emergency dialer: " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            }
            this.mHangupPendingMO = false;
            this.mMetrics.writeRilDial(this.mPhone.getPhoneId(), this.mPendingMO, clirMode, uusInfo);
            if (this.mPendingMO.getAddress() == null || this.mPendingMO.getAddress().length() == 0 || this.mPendingMO.getAddress().indexOf(78) >= 0) {
                this.mPendingMO.mCause = 7;
                pollCallsWhenSafe();
            } else {
                setMute(false);
                this.mCi.dial(this.mPendingMO.getDialAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), clirMode, uusInfo, obtainCompleteMessage());
            }
            if (this.mNumberConverted) {
                this.mPendingMO.setConverted(dialString);
                this.mNumberConverted = false;
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
        } else {
            throw new CallStateException("cannot dial in current state");
        }
        return this.mPendingMO;
    }

    @UnsupportedAppUsage
    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        if (action == 0) {
            this.mIsEcmTimerCanceled = false;
        } else if (action != 1) {
            Rlog.e(LOG_TAG, "handleEcmTimer, unsupported action " + action);
        } else {
            this.mIsEcmTimerCanceled = true;
        }
    }

    @UnsupportedAppUsage
    private void disableDataCallInEmergencyCall(String dialString) {
        if (PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString)) {
            log("disableDataCallInEmergencyCall");
            setIsInEmergencyCall();
        }
    }

    public void setIsInEmergencyCall() {
        this.mIsInEmergencyCall = true;
        this.mPhone.getDataEnabledSettings().setInternalDataEnabled(false);
        this.mPhone.notifyEmergencyCallRegistrants(true);
        this.mPhone.sendEmergencyCallStateChange(true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0083  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0170  */
    private Connection dialCdma(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        boolean internationalRoaming;
        String dialString2;
        clearDisconnected();
        boolean isEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(this.mPhone.getContext(), dialString);
        checkForDialIssues(isEmergencyCall);
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        String operatorIsoContry = tm.getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
        String simIsoContry = tm.getSimCountryIsoForPhone(this.mPhone.getPhoneId());
        boolean internationalRoaming2 = !TextUtils.isEmpty(operatorIsoContry) && !TextUtils.isEmpty(simIsoContry) && !simIsoContry.equals(operatorIsoContry);
        if (internationalRoaming2) {
            if ("us".equals(simIsoContry)) {
                internationalRoaming = internationalRoaming2 && !"vi".equals(operatorIsoContry);
            } else if ("vi".equals(simIsoContry)) {
                internationalRoaming = internationalRoaming2 && !"us".equals(operatorIsoContry);
            }
            if (!internationalRoaming) {
                dialString2 = convertNumberIfNecessary(this.mPhone, dialString);
            } else {
                dialString2 = dialString;
            }
            boolean isPhoneInEcmMode = this.mPhone.isInEcm();
            if (isPhoneInEcmMode && isEmergencyCall) {
                handleEcmTimer(1);
            }
            if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
                return dialThreeWay(dialString2, intentExtras);
            }
            this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString2), this, this.mForegroundCall, isEmergencyCall);
            if (intentExtras != null) {
                Rlog.i(LOG_TAG, "dialGsm - emergency dialer: " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
                this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            }
            this.mHangupPendingMO = false;
            if (this.mPendingMO.getAddress() != null && this.mPendingMO.getAddress().length() != 0) {
                if (this.mPendingMO.getAddress().indexOf(78) < 0) {
                    setMute(false);
                    disableDataCallInEmergencyCall(dialString2);
                    if (isPhoneInEcmMode) {
                        if (!isPhoneInEcmMode || !isEmergencyCall) {
                            this.mPhone.exitEmergencyCallbackMode();
                            this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                            this.mPendingCallClirMode = clirMode;
                            this.mPendingCallInEcm = true;
                            if (this.mNumberConverted) {
                                this.mPendingMO.setConverted(dialString);
                                this.mNumberConverted = false;
                            }
                            updatePhoneState();
                            this.mPhone.notifyPreciseCallStateChanged();
                            return this.mPendingMO;
                        }
                    }
                    this.mCi.dial(this.mPendingMO.getDialAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), clirMode, obtainCompleteMessage());
                    if (this.mNumberConverted) {
                    }
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                    return this.mPendingMO;
                }
            }
            this.mPendingMO.mCause = 7;
            pollCallsWhenSafe();
            if (this.mNumberConverted) {
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            return this.mPendingMO;
        }
        internationalRoaming = internationalRoaming2;
        if (!internationalRoaming) {
        }
        boolean isPhoneInEcmMode2 = this.mPhone.isInEcm();
        handleEcmTimer(1);
        if (this.mForegroundCall.getState() != Call.State.ACTIVE) {
        }
    }

    private Connection dialThreeWay(String dialString, Bundle intentExtras) {
        if (this.mForegroundCall.isIdle()) {
            return null;
        }
        disableDataCallInEmergencyCall(dialString);
        this.mPendingMO = new GsmCdmaConnection(this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, this.mIsInEmergencyCall);
        if (intentExtras != null) {
            Rlog.i(LOG_TAG, "dialThreeWay - emergency dialer " + intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
            this.mPendingMO.setHasKnownUserIntentEmergency(intentExtras.getBoolean("android.telecom.extra.IS_USER_INTENT_EMERGENCY_CALL"));
        }
        PersistableBundle bundle = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        if (bundle != null) {
            this.m3WayCallFlashDelay = bundle.getInt("cdma_3waycall_flash_delay_int");
        } else {
            this.m3WayCallFlashDelay = 0;
        }
        if (this.m3WayCallFlashDelay > 0) {
            this.mCi.sendCDMAFeatureCode(PhoneConfigurationManager.SSSS, obtainMessage(20));
        } else {
            this.mCi.sendCDMAFeatureCode(this.mPendingMO.getAddress(), obtainMessage(16));
        }
        return this.mPendingMO;
    }

    public Connection dial(String dialString, Bundle intentExtras) throws CallStateException {
        if (isPhoneTypeGsm()) {
            return dialGsm(dialString, 0, intentExtras);
        }
        return dialCdma(dialString, 0, intentExtras);
    }

    public Connection dialGsm(String dialString, UUSInfo uusInfo, Bundle intentExtras) throws CallStateException {
        return dialGsm(dialString, 0, uusInfo, intentExtras);
    }

    private Connection dialGsm(String dialString, int clirMode, Bundle intentExtras) throws CallStateException {
        return dialGsm(dialString, clirMode, null, intentExtras);
    }

    public void acceptCall() throws CallStateException {
        HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getPhoneId(), 2, LOG_TAG);
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
        this.mCi.sendCDMAFeatureCode(PhoneConfigurationManager.SSSS, obtainMessage(8));
        this.mPhone.notifyPreciseCallStateChanged();
    }

    @UnsupportedAppUsage
    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (isPhoneTypeGsm()) {
            this.mCi.switchWaitingOrHoldingAndActive(obtainCompleteMessage(8));
        } else if (this.mForegroundCall.getConnections().size() > 1) {
            flashAndSetGenericTrue();
        } else {
            this.mCi.sendCDMAFeatureCode(PhoneConfigurationManager.SSSS, obtainMessage(8));
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

    @UnsupportedAppUsage
    public void clearDisconnected() {
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        return this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING && !this.mBackgroundCall.isFull() && !this.mForegroundCall.isFull();
    }

    public void checkForDialIssues(boolean isEmergencyCall) throws CallStateException {
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        Call.State fgCallState = this.mForegroundCall.getState();
        Call.State bgCallState = this.mBackgroundCall.getState();
        if (this.mCi.getRadioState() != 1) {
            throw new CallStateException(2, "Modem not powered");
        } else if (disableCall.equals("true")) {
            throw new CallStateException(5, "Calling disabled via ro.telephony.disable-call property");
        } else if (this.mPendingMO != null) {
            throw new CallStateException(3, "A call is already dialing.");
        } else if (this.mRingingCall.isRinging()) {
            throw new CallStateException(4, "Can't call while a call is ringing.");
        } else if (isPhoneTypeGsm() && ((fgCallState != Call.State.IDLE && fgCallState != Call.State.DISCONNECTED && fgCallState != Call.State.ACTIVE) || (bgCallState != Call.State.IDLE && bgCallState != Call.State.DISCONNECTED && bgCallState != Call.State.HOLDING))) {
            throw new CallStateException(6, "There is already a foreground and background call.");
        } else if (!isPhoneTypeGsm() && this.mForegroundCall.getState().isAlive() && this.mForegroundCall.getState() != Call.State.ACTIVE && this.mBackgroundCall.getState().isAlive()) {
            throw new CallStateException(6, "There is already a foreground and background call.");
        } else if (!isEmergencyCall && isInOtaspCall()) {
            throw new CallStateException(7, "OTASP provisioning is in process.");
        } else if (!custCanDial()) {
            throw new CallStateException(-1, "Cannot dial in current state.");
        }
    }

    public boolean canTransfer() {
        if (!isPhoneTypeGsm()) {
            Rlog.e(LOG_TAG, "canTransfer: not possible in CDMA");
            return false;
        } else if ((this.mForegroundCall.getState() == Call.State.ACTIVE || this.mForegroundCall.getState() == Call.State.ALERTING || this.mForegroundCall.getState() == Call.State.DIALING) && this.mBackgroundCall.getState() == Call.State.HOLDING) {
            return true;
        } else {
            return false;
        }
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
    }

    @UnsupportedAppUsage
    private Message obtainCompleteMessage() {
        return obtainCompleteMessage(4);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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
                this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        } else if (this.mHwGsmCdmaCallTrackerEx.notifyRegistrantsDelayed()) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
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
    @Override // com.android.internal.telephony.CallTracker
    public synchronized void handlePollCalls(AsyncResult ar) {
        List polledCalls;
        Phone imsPhone;
        boolean hasAnyCallDisconnected;
        DriverCall dc;
        int curDC;
        boolean noConnectionExists;
        boolean hasAnyCallDisconnected2;
        Connection newUnknownConnectionCdma;
        if (ar.exception == null) {
            polledCalls = (List) ar.result;
        } else if (VSimUtilsInner.isVSimPhone(this.mPhone)) {
            List polledCalls2 = new ArrayList();
            log("vsim phone handlePollCalls");
            polledCalls = polledCalls2;
        } else if (isCommandExceptionRadioNotAvailable(ar.exception)) {
            List polledCalls3 = new ArrayList();
            log("no DriverCall");
            polledCalls = polledCalls3;
        } else {
            pollCallsAfterDelay();
            log("pollCallsAfterDelay");
            return;
        }
        ArrayList<Connection> newUnknownConnectionsGsm = new ArrayList<>();
        boolean hasAnyCallDisconnected3 = false;
        int handoverConnectionsSize = this.mHandoverConnections.size();
        boolean noConnectionExists2 = true;
        syncHoConnection();
        Set<Integer> currentCallIds = new ArraySet<>();
        int i = 0;
        int curDC2 = 0;
        int dcSize = polledCalls.size();
        boolean unknownConnectionAppeared = false;
        boolean hasNonHangupStateChanged = false;
        Connection newUnknownConnectionCdma2 = null;
        Connection newRinging = null;
        while (i < this.mConnections.length) {
            GsmCdmaConnection conn = this.mConnections[i];
            if (curDC2 < dcSize) {
                DriverCall dc2 = (DriverCall) polledCalls.get(curDC2);
                if (dc2.index == i + 1) {
                    curDC2++;
                    dc = dc2;
                } else {
                    dc = null;
                }
            } else {
                dc = null;
            }
            if (dc != null) {
                currentCallIds.add(Integer.valueOf(dc.index));
            }
            if (!(conn == null && dc == null)) {
                noConnectionExists2 = false;
            }
            if (dc != null) {
                newUnknownConnectionCdma = newUnknownConnectionCdma2;
                if (dc.state == DriverCall.State.ALERTING) {
                    hasAnyCallDisconnected2 = hasAnyCallDisconnected3;
                    noConnectionExists = noConnectionExists2;
                    curDC = curDC2;
                    HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getPhoneId(), 0, LOG_TAG);
                } else {
                    hasAnyCallDisconnected2 = hasAnyCallDisconnected3;
                    noConnectionExists = noConnectionExists2;
                    curDC = curDC2;
                }
            } else {
                newUnknownConnectionCdma = newUnknownConnectionCdma2;
                hasAnyCallDisconnected2 = hasAnyCallDisconnected3;
                noConnectionExists = noConnectionExists2;
                curDC = curDC2;
            }
            if (isNeedToPrintAnswerCHR(dc)) {
                HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getPhoneId(), 2, "RadioResponse");
                synchronized (this.mCHRLock) {
                    this.mPrintedAnswerCHRCallId.add(Integer.valueOf(dc.index));
                }
            }
            if (conn == null && dc != null) {
                if (this.mPendingMO == null || !this.mPendingMO.compareTo(dc) || getHoConnection(dc) != null) {
                    log("pendingMo=" + this.mPendingMO + ", dc=" + dc);
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    Connection hoConnection = getHoConnection(dc);
                    log("[SRVCC] handlepollcall --> hoConnection:" + hoConnection);
                    if (hoConnection != null) {
                        this.mConnections[i].setPostDialString(hoConnection.getOrigDialString());
                        this.mConnections[i].migrateFrom(hoConnection);
                        if (hoConnection.mPreHandoverState == Call.State.ACTIVE || hoConnection.mPreHandoverState == Call.State.HOLDING || dc.state != DriverCall.State.ACTIVE) {
                            this.mConnections[i].onConnectedConnectionMigrated();
                        } else {
                            Rlog.i(LOG_TAG, "[SRVCC] mConnections onConnectedInOrOut");
                            this.mConnections[i].onConnectedInOrOut();
                        }
                        if (dc.state == DriverCall.State.ACTIVE || dc.state == DriverCall.State.HOLDING) {
                            this.mConnections[i].releaseWakeLock();
                        }
                        this.mHandoverConnections.remove(hoConnection);
                        this.mRemovedHandoverConnections.add(hoConnection);
                        Rlog.i(LOG_TAG, "[SRVCC] notifyHandoverStateChanged mConnections[i]=" + this.mConnections[i]);
                        Phone imsPhone2 = this.mPhone.getImsPhone();
                        if (imsPhone2 != null) {
                            imsPhone2.notifyHandoverStateChanged(this.mConnections[i]);
                        } else {
                            this.mPhone.notifyHandoverStateChanged(this.mConnections[i]);
                        }
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
                        return;
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
                    if (this.mIsEcmTimerCanceled) {
                        handleEcmTimer(0);
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                }
                this.mConnections[i] = null;
            } else if (conn != null && dc != null && !conn.compareTo(dc) && isPhoneTypeGsm()) {
                this.mDroppedDuringPoll.add(conn);
                this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                if (this.mConnections[i].getCall() == this.mRingingCall) {
                    newRinging = this.mConnections[i];
                }
                hasNonHangupStateChanged = true;
            } else if (!(conn == null || dc == null)) {
                if (isPhoneTypeGsm() || conn.isIncoming() == dc.isMT) {
                    hasNonHangupStateChanged = hasNonHangupStateChanged || conn.update(dc);
                } else if (dc.isMT) {
                    this.mDroppedDuringPoll.add(conn);
                    this.mConnections[i] = new GsmCdmaConnection(this.mPhone, dc, this, i);
                    Connection newRinging2 = checkMtFindNewRinging(dc, i);
                    if (newRinging2 == null) {
                        unknownConnectionAppeared = true;
                        newUnknownConnectionCdma = conn;
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                    newRinging = newRinging2;
                } else {
                    Rlog.e(LOG_TAG, "Error in RIL, Phantom call appeared " + dc);
                }
            }
            newUnknownConnectionCdma2 = newUnknownConnectionCdma;
            i++;
            polledCalls = polledCalls;
            hasAnyCallDisconnected3 = hasAnyCallDisconnected2;
            noConnectionExists2 = noConnectionExists;
            curDC2 = curDC;
        }
        boolean hasAnyCallDisconnected4 = hasAnyCallDisconnected3;
        synchronized (this.mCHRLock) {
            Iterator<Integer> iterator = this.mPrintedAnswerCHRCallId.iterator();
            while (iterator.hasNext()) {
                if (!currentCallIds.contains(Integer.valueOf(iterator.next().intValue()))) {
                    iterator.remove();
                }
            }
        }
        if (!isPhoneTypeGsm() && noConnectionExists2) {
            checkAndEnableDataCallAfterEmergencyCallDropped();
        }
        if (this.mPendingMO != null) {
            Rlog.i(LOG_TAG, "Pending MO dropped before poll fg state:" + this.mForegroundCall.getState());
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
            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getPhoneId(), 1, LOG_TAG);
            this.mPhone.notifyNewRingingConnection(newRinging);
        }
        ArrayList<GsmCdmaConnection> locallyDisconnectedConnections = new ArrayList<>();
        Connection newUnknownConnectionCdma3 = newUnknownConnectionCdma2;
        for (int i2 = this.mDroppedDuringPoll.size() - 1; i2 >= 0; i2--) {
            GsmCdmaConnection conn2 = this.mDroppedDuringPoll.get(i2);
            boolean wasDisconnected = false;
            if (conn2.isIncoming() && conn2.getConnectTime() == 0) {
                int cause = conn2.mCause == 3 ? 16 : 1;
                log("missed/rejected call, conn.cause=" + conn2.mCause);
                log("setting cause to " + cause);
                this.mDroppedDuringPoll.remove(i2);
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn2);
                hasAnyCallDisconnected4 |= conn2.onDisconnect(cause);
            } else if (conn2.mCause == 3 || conn2.mCause == 7) {
                this.mDroppedDuringPoll.remove(i2);
                wasDisconnected = true;
                locallyDisconnectedConnections.add(conn2);
                hasAnyCallDisconnected4 |= conn2.onDisconnect(conn2.mCause);
            }
            if (!isPhoneTypeGsm() && wasDisconnected && unknownConnectionAppeared && conn2 == newUnknownConnectionCdma3) {
                newUnknownConnectionCdma3 = null;
                unknownConnectionAppeared = false;
            }
        }
        if (locallyDisconnectedConnections.size() > 0) {
            this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), locallyDisconnectedConnections, getNetworkCountryIso());
        }
        Iterator<Connection> it = this.mHandoverConnections.iterator();
        while (it.hasNext()) {
            Connection hoConnection2 = it.next();
            log("[SRVCC] handlePollCalls - disconnect hoConn= " + hoConnection2 + " hoConn.State= " + hoConnection2.getState());
            if (hoConnection2.getState().isRinging()) {
                hasAnyCallDisconnected = hoConnection2.onDisconnect(1);
            } else {
                hasAnyCallDisconnected = hoConnection2.onDisconnect(-1);
            }
            hasAnyCallDisconnected4 |= hasAnyCallDisconnected;
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
        if (newRinging != null || hasNonHangupStateChanged || hasAnyCallDisconnected4) {
            internalClearDisconnected();
        }
        updatePhoneState();
        if (unknownConnectionAppeared) {
            if (isPhoneTypeGsm()) {
                Iterator<Connection> it2 = newUnknownConnectionsGsm.iterator();
                while (it2.hasNext()) {
                    Connection c = it2.next();
                    log("Notify unknown for " + c);
                    this.mPhone.notifyUnknownConnection(c);
                }
            } else {
                this.mPhone.notifyUnknownConnection(newUnknownConnectionCdma3);
            }
        }
        if (hasNonHangupStateChanged || newRinging != null || hasAnyCallDisconnected4) {
            this.mPhone.notifyPreciseCallStateChanged();
            updateMetrics(this.mConnections);
        }
        if (handoverConnectionsSize > 0 && this.mHandoverConnections.size() == 0 && (imsPhone = this.mPhone.getImsPhone()) != null) {
            log("[SRVCC] handlePollCalls - handover connection mapped, clean HandoverCall.");
            imsPhone.callEndCleanupHandOverCallIfAny();
        }
    }

    private void updateMetrics(GsmCdmaConnection[] connections) {
        ArrayList<GsmCdmaConnection> activeConnections = new ArrayList<>();
        for (GsmCdmaConnection conn : connections) {
            if (conn != null) {
                activeConnections.add(conn);
            }
        }
        this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), activeConnections, getNetworkCountryIso());
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
        GsmCdmaCall gsmCdmaCall;
        if (conn.mOwner == this) {
            if (conn == this.mPendingMO) {
                log("hangup: set hangupPendingMO to true");
                this.mHangupPendingMO = true;
            } else if (!isPhoneTypeGsm() && conn.getCall() == (gsmCdmaCall = this.mRingingCall) && gsmCdmaCall.getState() == Call.State.WAITING) {
                this.mRingingCall.setLastRingNumberAndChangeTime(conn.getAddress());
                conn.onLocalDisconnect();
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                return;
            } else {
                try {
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), conn, conn.getGsmCdmaIndex(), getNetworkCountryIso());
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
                return;
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

    @UnsupportedAppUsage
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
            GsmCdmaCall gsmCdmaCall = this.mRingingCall;
            if (call == gsmCdmaCall) {
                log("(ringing) hangup waiting or background");
                logHangupEvent(call);
                if (IS_SUPPORT_RIL_RECOVERY) {
                    HwCustGsmCdmaCallTracker hwCustGsmCdmaCallTracker = this.mHwGCT;
                    if (hwCustGsmCdmaCallTracker == null || hwCustGsmCdmaCallTracker.getRejectCallCause(call) == -1) {
                        hangupAllConnections(call);
                    } else {
                        log("rejectCallForCause !!!");
                        this.mHwGCT.rejectCallForCause(this.mCi, call, obtainCompleteMessage());
                    }
                } else {
                    this.mCi.hangupWaitingOrBackground(obtainCompleteMessage());
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
                if (gsmCdmaCall.isRinging()) {
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
            this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, call_index, getNetworkCountryIso());
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
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex(), getNetworkCountryIso());
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
                    this.mMetrics.writeRilHangup(this.mPhone.getPhoneId(), cn, cn.getGsmCdmaIndex(), getNetworkCountryIso());
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
        RegistrantList registrantList = this.mCallWaitingRegistrants;
        if (registrantList != null) {
            registrantList.notifyRegistrants(new AsyncResult((Object) null, obj, (Throwable) null));
        }
    }

    private void notifyWaitingNumberChanged() {
        Rlog.i(LOG_TAG, "notifyWaitingNumberChanged");
        RegistrantList registrantList = this.cdmaWaitingNumberChangedRegistrants;
        if (registrantList != null) {
            registrantList.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    private void handleCallWaitingInfo(CdmaCallWaitingNotification cw) {
        GsmCdmaConnection cwConn;
        if (!isPhoneTypeGsm() && this.mRingingCall.getState() == Call.State.WAITING && (cwConn = (GsmCdmaConnection) this.mRingingCall.getLatestConnection()) != null && cw.number != null && cw.number.equals(cwConn.getAddress())) {
            long passedTime = System.currentTimeMillis() - cwConn.getCreateTime();
            if (passedTime > 0 && passedTime < 30000) {
                Rlog.i(LOG_TAG, "Ignoring callingwaiting events received for same number within 30s");
                return;
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.android.internal.telephony.CallTracker, android.os.Handler
    public void handleMessage(Message msg) {
        int causeCode;
        int causeCode2;
        Connection connection;
        GsmCdmaConnection gsmCdmaConnection;
        int i = msg.what;
        if (i == 1) {
            Rlog.i(LOG_TAG, "Event EVENT_POLL_CALLS_RESULT Received");
            if (msg == this.mLastRelevantPoll) {
                log("handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                this.mNeedsPoll = false;
                this.mLastRelevantPoll = null;
                handlePollCalls((AsyncResult) msg.obj);
            }
        } else if (i == 2 || i == 3) {
            pollCallsWhenSafe();
        } else if (i == 4) {
            operationComplete();
        } else if (i == 5) {
            String vendorCause = null;
            AsyncResult ar = (AsyncResult) msg.obj;
            operationComplete();
            if (ar.exception == null) {
                LastCallFailCause failCause = (LastCallFailCause) ar.result;
                causeCode = failCause.causeCode;
                vendorCause = failCause.vendorCause;
            } else if (ar.exception instanceof CommandException) {
                CommandException commandException = (CommandException) ar.exception;
                int i2 = AnonymousClass3.$SwitchMap$com$android$internal$telephony$CommandException$Error[commandException.getCommandError().ordinal()];
                if (i2 == 1 || i2 == 2 || i2 == 3 || i2 == 4) {
                    vendorCause = commandException.getCommandError().toString();
                    causeCode2 = 65535;
                } else {
                    causeCode2 = 16;
                }
                causeCode = causeCode2;
            } else {
                causeCode = 16;
                Rlog.i(LOG_TAG, "Exception during getLastCallFailCause, assuming normal disconnect");
            }
            if (causeCode == 34 || causeCode == 41 || causeCode == 42 || causeCode == 44 || causeCode == 49 || causeCode == 58 || causeCode == 65535) {
                CellLocation loc = this.mPhone.getCellLocation();
                int cid = -1;
                if (loc != null) {
                    if (loc instanceof GsmCellLocation) {
                        cid = ((GsmCellLocation) loc).getCid();
                    } else if (loc instanceof CdmaCellLocation) {
                        cid = ((CdmaCellLocation) loc).getBaseStationId();
                    }
                }
                EventLog.writeEvent((int) EventLogTags.CALL_DROP, Integer.valueOf(causeCode), Integer.valueOf(cid), Integer.valueOf(TelephonyManager.getDefault().getNetworkType()));
            }
            int s = this.mDroppedDuringPoll.size();
            for (int i3 = 0; i3 < s; i3++) {
                this.mDroppedDuringPoll.get(i3).onRemoteDisconnect(causeCode, vendorCause);
            }
            updatePhoneState();
            this.mPhone.notifyPreciseCallStateChanged();
            this.mMetrics.writeRilCallList(this.mPhone.getPhoneId(), this.mDroppedDuringPoll, getNetworkCountryIso());
            this.mDroppedDuringPoll.clear();
            this.mIsInCsRedial = false;
        } else if (i != 20) {
            if (i == 50) {
                Rlog.i(LOG_TAG, "Event EVENT_RSRVCC_STATE_CHANGED Received");
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null) {
                    this.mIsInCsRedial = true;
                    return;
                }
                Rlog.e(LOG_TAG, "RSrvcc exception: " + ar2.exception);
            } else if (i != 201) {
                switch (i) {
                    case 8:
                        if (isPhoneTypeGsm()) {
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
                        if (!(!isPhoneTypeGsm() || ((AsyncResult) msg.obj).exception == null || (connection = this.mForegroundCall.getLatestConnection()) == null)) {
                            connection.onConferenceMergeFailed();
                            break;
                        }
                    case 12:
                    case 13:
                        break;
                    case 14:
                        if (!isPhoneTypeGsm()) {
                            if (this.mPendingCallInEcm) {
                                this.mCi.dial(this.mPendingMO.getDialAddress(), this.mPendingMO.isEmergencyCall(), this.mPendingMO.getEmergencyNumberInfo(), this.mPendingMO.hasKnownUserIntentEmergency(), this.mPendingCallClirMode, obtainCompleteMessage());
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
                                Rlog.i(LOG_TAG, "Event EVENT_CALL_WAITING_INFO_CDMA Received");
                                return;
                            }
                            return;
                        }
                        throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                    case 16:
                        if (isPhoneTypeGsm()) {
                            throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                        } else if (((AsyncResult) msg.obj).exception == null && (gsmCdmaConnection = this.mPendingMO) != null) {
                            gsmCdmaConnection.onConnectedInOrOut();
                            this.mPendingMO = null;
                            return;
                        } else {
                            return;
                        }
                    default:
                        throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
                }
                if (isPhoneTypeGsm()) {
                    if (((AsyncResult) msg.obj).exception != null) {
                        this.mPhone.notifySuppServiceFailed(getFailedService(msg.what));
                    }
                    operationComplete();
                } else if (msg.what != 8) {
                    throw new RuntimeException("unexpected event " + msg.what + " not handled by phone type " + this.mPhone.getPhoneType());
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
                /* class com.android.internal.telephony.GsmCdmaCallTracker.AnonymousClass2 */

                @Override // java.lang.Runnable
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

    /* renamed from: com.android.internal.telephony.GsmCdmaCallTracker$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$CommandException$Error = new int[CommandException.Error.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.RADIO_NOT_AVAILABLE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.NO_MEMORY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.INTERNAL_ERR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$CommandException$Error[CommandException.Error.NO_RESOURCES.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public void dispatchCsCallRadioTech(int vrat) {
        GsmCdmaConnection[] gsmCdmaConnectionArr = this.mConnections;
        if (gsmCdmaConnectionArr == null) {
            log("dispatchCsCallRadioTech: mConnections is null");
            return;
        }
        for (GsmCdmaConnection gsmCdmaConnection : gsmCdmaConnectionArr) {
            if (gsmCdmaConnection != null) {
                gsmCdmaConnection.setCallRadioTech(vrat);
            }
        }
    }

    private void checkAndEnableDataCallAfterEmergencyCallDropped() {
        if (this.mIsInEmergencyCall) {
            this.mIsInEmergencyCall = false;
            boolean inEcm = this.mPhone.isInEcm();
            log("checkAndEnableDataCallAfterEmergencyCallDropped,isInEcm=" + inEcm);
            if (!inEcm) {
                this.mPhone.getDataEnabledSettings().setInternalDataEnabled(true);
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

    public boolean isInOtaspCall() {
        GsmCdmaConnection gsmCdmaConnection = this.mPendingMO;
        return (gsmCdmaConnection != null && gsmCdmaConnection.isOtaspCall()) || this.mForegroundCall.getConnections().stream().filter($$Lambda$GsmCdmaCallTracker$wkXwCyVPcnlqyXzSJdP2cQlpZxg.INSTANCE).count() > 0;
    }

    static /* synthetic */ boolean lambda$isInOtaspCall$0(Connection connection) {
        return (connection instanceof GsmCdmaConnection) && ((GsmCdmaConnection) connection).isOtaspCall();
    }

    @UnsupportedAppUsage
    private boolean isPhoneTypeGsm() {
        return this.mPhone.getPhoneType() == 1;
    }

    @Override // com.android.internal.telephony.CallTracker
    @UnsupportedAppUsage
    public GsmCdmaPhone getPhone() {
        return this.mPhone;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.CallTracker
    @UnsupportedAppUsage
    public void log(String msg) {
        Rlog.i(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    @Override // com.android.internal.telephony.CallTracker
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("GsmCdmaCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println("mConnections: length=" + this.mConnections.length);
        for (int i = 0; i < this.mConnections.length; i++) {
            pw.printf("  mConnections[%d]=%s\n", Integer.valueOf(i), this.mConnections[i]);
        }
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        if (!isPhoneTypeGsm()) {
            pw.println(" mCallWaitingRegistrants=" + this.mCallWaitingRegistrants);
        }
        pw.println(" mDroppedDuringPoll: size=" + this.mDroppedDuringPoll.size());
        for (int i2 = 0; i2 < this.mDroppedDuringPoll.size(); i2++) {
            pw.printf("  mDroppedDuringPoll[%d]=%s\n", Integer.valueOf(i2), this.mDroppedDuringPoll.get(i2));
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

    @Override // com.android.internal.telephony.CallTracker
    public PhoneConstants.State getState() {
        return this.mState;
    }

    public int getMaxConnectionsPerCall() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return 5;
        }
        return 1;
    }

    private String getNetworkCountryIso() {
        ServiceStateTracker sst;
        LocaleTracker lt;
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        if (gsmCdmaPhone == null || (sst = gsmCdmaPhone.getServiceStateTracker()) == null || (lt = sst.getLocaleTracker()) == null) {
            return PhoneConfigurationManager.SSSS;
        }
        return lt.getCurrentCountry();
    }

    @Override // com.android.internal.telephony.CallTracker
    public void cleanupCalls() {
        pollCallsWhenSafe();
    }

    public boolean isInCsRedial() {
        return this.mIsInCsRedial;
    }

    private void syncHoConnection() {
        ArrayList<Connection> hoConnections;
        Phone imsPhone = this.mPhone.getImsPhone();
        if (!(imsPhone == null || this.mSrvccState != Call.SrvccState.STARTED || (hoConnections = imsPhone.getHandoverConnection()) == null)) {
            int list_size = hoConnections.size();
            for (int i = 0; i < list_size; i++) {
                Connection conn = hoConnections.get(i);
                if (!this.mHandoverConnections.contains(conn) && !this.mRemovedHandoverConnections.contains(conn)) {
                    this.mHandoverConnections.add(conn);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CallTracker
    public void markCallRejectCause(String telecomCallId, int cause) {
        log("markCallRejectByUser, telecomCallId: " + telecomCallId + ", cause:" + cause);
        HwCustGsmCdmaCallTracker hwCustGsmCdmaCallTracker = this.mHwGCT;
        if (hwCustGsmCdmaCallTracker == null) {
            log("mHwGCT is null!");
        } else {
            hwCustGsmCdmaCallTracker.markCallRejectCause(telecomCallId, cause);
        }
    }

    private boolean custCanDial() {
        HwCustGsmCdmaCallTracker hwCustGsmCdmaCallTracker = this.mHwGCT;
        if (hwCustGsmCdmaCallTracker != null) {
            return hwCustGsmCdmaCallTracker.canDial();
        }
        return true;
    }

    private boolean isNeedToPrintAnswerCHR(DriverCall dc) {
        if (dc == null) {
            return false;
        }
        synchronized (this.mCHRLock) {
            if (this.mPrintedAnswerCHRCallId.contains(Integer.valueOf(dc.index))) {
                return false;
            }
        }
        if (dc.state == DriverCall.State.ACTIVE && !this.mIsSrvccHappened && dc.isMT) {
            return true;
        }
        return false;
    }

    public void switchVoiceCallBackgroundState(int state) {
        this.mHwGsmCdmaCallTrackerEx.switchVoiceCallBackgroundState(state);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mHwGsmCdmaCallTrackerEx.registerForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mHwGsmCdmaCallTrackerEx.unregisterForLineControlInfo(h);
    }

    @Override // com.android.internal.telephony.IGsmCdmaCallTrackerInner
    public void voiceCallEndedRegistrantsNotifyHw() {
        this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
    }

    @Override // com.android.internal.telephony.IGsmCdmaCallTrackerInner
    public PhoneExt getPhoneHw() {
        return PhoneExt.getPhoneExt(this.mPhone);
    }

    @Override // com.android.internal.telephony.IGsmCdmaCallTrackerInner
    public IHwGsmCdmaCallTrackerEx getHwGsmCdmaCallTrackerEx() {
        return this.mHwGsmCdmaCallTrackerEx;
    }

    @Override // com.android.internal.telephony.IGsmCdmaCallTrackerInner
    public List<GsmCdmaConnectionEx> getRingingCallConnections() {
        return GsmCdmaConnectionEx.getGsmCdmaConnectionExList(this.mRingingCall.getConnections());
    }

    @Override // com.android.internal.telephony.IGsmCdmaCallTrackerInner
    public List<GsmCdmaConnectionEx> getForegroundCallConnections() {
        return GsmCdmaConnectionEx.getGsmCdmaConnectionExList(this.mForegroundCall.getConnections());
    }

    @Override // com.android.internal.telephony.IGsmCdmaCallTrackerInner
    public List<GsmCdmaConnectionEx> getBackgroundCallConnections() {
        return GsmCdmaConnectionEx.getGsmCdmaConnectionExList(this.mBackgroundCall.getConnections());
    }

    @Override // com.android.internal.telephony.IGsmCdmaCallTrackerInner
    public GsmCdmaConnectionEx getForegroundCallLatestConnection() {
        return GsmCdmaConnectionEx.from(this.mForegroundCall.getLatestConnection());
    }
}

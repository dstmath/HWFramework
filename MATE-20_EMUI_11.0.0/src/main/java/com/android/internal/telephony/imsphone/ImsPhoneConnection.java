package com.android.internal.telephony.imsphone;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telecom.Connection;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsStreamMediaProfile;
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.imsphone.ImsRttTextHandler;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.util.Objects;

public class ImsPhoneConnection extends Connection implements ImsVideoCallProviderWrapper.ImsVideoProviderWrapperCallback {
    private static final boolean DBG = true;
    private static final int EVENT_DTMF_DELAY_DONE = 5;
    private static final int EVENT_DTMF_DONE = 1;
    private static final int EVENT_NEXT_POST_DIAL = 3;
    private static final int EVENT_PAUSE_DONE = 2;
    private static final int EVENT_WAKE_LOCK_TIMEOUT = 4;
    private static final int INVALID_CALL_ID = -1;
    private static final String LOG_TAG = "ImsPhoneConnection";
    private static final int PAUSE_DELAY_MILLIS = 3000;
    private static final int WAKE_LOCK_TIMEOUT_MILLIS = 60000;
    private static final boolean isImsAsNormal = HuaweiTelephonyConfigs.isHisiPlatform();
    private static final boolean mIsDocomo = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private int mAudioCodec = 0;
    private long mConferenceConnectTime = 0;
    private long mDisconnectTime;
    @UnsupportedAppUsage
    private boolean mDisconnected;
    private int mDtmfToneDelay = 0;
    private Bundle mExtras = new Bundle();
    private Handler mHandler;
    private final Messenger mHandlerMessenger;
    private ImsCall mImsCall;
    private ImsVideoCallProviderWrapper mImsVideoCallProviderWrapper;
    private boolean mIsEmergency = false;
    private boolean mIsLocalVideoCapable = true;
    private boolean mIsMergeInProcess = false;
    private boolean mIsRttEnabledForCall = false;
    private TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    @UnsupportedAppUsage
    private ImsPhoneCallTracker mOwner;
    @UnsupportedAppUsage
    private ImsPhoneCall mParent;
    private PowerManager.WakeLock mPartialWakeLock;
    private int mPreciseDisconnectCause = 0;
    private ImsRttTextHandler mRttTextHandler;
    private Connection.RttTextStream mRttTextStream;
    private boolean mShouldIgnoreVideoStateChanges = false;
    private UUSInfo mUusInfo;

    class MyHandler extends Handler {
        MyHandler(Looper l) {
            super(l);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1) {
                if (!(i == 2 || i == 3)) {
                    if (i == 4) {
                        ImsPhoneConnection.this.releaseWakeLock();
                        return;
                    } else if (i != 5) {
                        return;
                    }
                }
                ImsPhoneConnection.this.processNextPostDialChar();
                return;
            }
            ImsPhoneConnection.this.mHandler.sendMessageDelayed(ImsPhoneConnection.this.mHandler.obtainMessage(5), (long) ImsPhoneConnection.this.mDtmfToneDelay);
        }
    }

    public ImsPhoneConnection(Phone phone, ImsCall imsCall, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isUnknown) {
        super(5);
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mHandlerMessenger = new Messenger(this.mHandler);
        this.mImsCall = imsCall;
        if (imsCall == null || imsCall.getCallProfile() == null) {
            this.mNumberPresentation = 3;
            this.mCnapNamePresentation = 3;
            this.mRedirectNumberPresentation = 1;
        } else {
            this.mAddress = imsCall.getCallProfile().getCallExtra("oi");
            this.mCnapName = imsCall.getCallProfile().getCallExtra("cna");
            this.mNumberPresentation = ImsCallProfile.OIRToPresentation(imsCall.getCallProfile().getCallExtraInt("oir"));
            this.mCnapNamePresentation = ImsCallProfile.OIRToPresentation(imsCall.getCallProfile().getCallExtraInt("cnap"));
            updateMediaCapabilities(imsCall);
            if (mIsDocomo) {
                this.mRedirectAddress = imsCall.getCallProfile().getCallExtra("redirect_number");
                this.mRedirectNumberPresentation = ImsCallProfile.OIRToPresentation(imsCall.getCallProfile().getCallExtraInt("redirect_number_presentation"));
                Rlog.i(LOG_TAG, "new ImsPhoneConnection  mRedirectNumberPresentation " + this.mRedirectNumberPresentation);
            }
        }
        this.mIsIncoming = !isUnknown;
        this.mCreateTime = System.currentTimeMillis();
        this.mUusInfo = null;
        updateExtras(imsCall);
        this.mParent = parent;
        this.mParent.attach(this, this.mIsIncoming ? Call.State.INCOMING : Call.State.DIALING);
        fetchDtmfToneDelay(phone);
        if (phone.getContext().getResources().getBoolean(17891571)) {
            setAudioModeIsVoip(true);
        }
    }

    public ImsPhoneConnection(Phone phone, String dialString, ImsPhoneCallTracker ct, ImsPhoneCall parent, boolean isEmergency) {
        super(5);
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this.mOwner.getLooper());
        this.mHandlerMessenger = new Messenger(this.mHandler);
        this.mDialString = dialString;
        this.mAddress = PhoneNumberUtils.extractNetworkPortionAlt(dialString);
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        this.mIsIncoming = false;
        this.mCnapName = null;
        this.mCnapNamePresentation = 1;
        this.mNumberPresentation = 1;
        this.mCreateTime = System.currentTimeMillis();
        this.mParent = parent;
        parent.attachFake(this, Call.State.DIALING);
        this.mIsEmergency = isEmergency;
        if (isEmergency) {
            setEmergencyCallInfo(this.mOwner);
        }
        fetchDtmfToneDelay(phone);
        if (phone.getContext().getResources().getBoolean(17891571)) {
            setAudioModeIsVoip(true);
        }
    }

    public void dispose() {
    }

    static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    static boolean equalsBaseDialString(String a, String b) {
        if (a == null) {
            if (b == null) {
                return true;
            }
        } else if (b != null && a.startsWith(b)) {
            return true;
        }
        return false;
    }

    private int applyLocalCallCapabilities(ImsCallProfile localProfile, int capabilities) {
        Rlog.i(LOG_TAG, "applyLocalCallCapabilities - localProfile = " + localProfile);
        int capabilities2 = removeCapability(capabilities, 4);
        if (!this.mIsLocalVideoCapable) {
            Rlog.i(LOG_TAG, "applyLocalCallCapabilities - disabling video (overidden)");
            return capabilities2;
        }
        int i = localProfile.mCallType;
        if (i == 3 || i == 4) {
            return addCapability(capabilities2, 4);
        }
        return capabilities2;
    }

    private static int applyRemoteCallCapabilities(ImsCallProfile remoteProfile, int capabilities) {
        Rlog.w(LOG_TAG, "applyRemoteCallCapabilities - remoteProfile = " + remoteProfile);
        int capabilities2 = removeCapability(capabilities, 8);
        int i = remoteProfile.mCallType;
        if (i == 3 || i == 4) {
            return addCapability(capabilities2, 8);
        }
        return capabilities2;
    }

    @Override // com.android.internal.telephony.Connection, com.android.internal.telephony.IGsmCdmaConnectionInner
    public String getOrigDialString() {
        return this.mDialString;
    }

    @Override // com.android.internal.telephony.Connection
    @UnsupportedAppUsage
    public ImsPhoneCall getCall() {
        return this.mParent;
    }

    @Override // com.android.internal.telephony.Connection
    public long getDisconnectTime() {
        return this.mDisconnectTime;
    }

    @Override // com.android.internal.telephony.Connection
    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    @Override // com.android.internal.telephony.Connection
    public long getHoldDurationMillis() {
        if (getState() != Call.State.HOLDING) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mHoldingStartTime;
    }

    public void setDisconnectCause(int cause) {
        this.mCause = cause;
    }

    @Override // com.android.internal.telephony.Connection
    public String getVendorDisconnectCause() {
        return null;
    }

    @UnsupportedAppUsage
    public ImsPhoneCallTracker getOwner() {
        return this.mOwner;
    }

    @Override // com.android.internal.telephony.Connection
    public Call.State getState() {
        if (this.mDisconnected) {
            return Call.State.DISCONNECTED;
        }
        return super.getState();
    }

    @Override // com.android.internal.telephony.Connection
    public void deflect(String number) throws CallStateException {
        if (this.mParent.getState().isRinging()) {
            try {
                if (this.mImsCall != null) {
                    this.mImsCall.deflect(number);
                    return;
                }
                throw new CallStateException("no valid ims call to deflect");
            } catch (ImsException e) {
                throw new CallStateException("cannot deflect call");
            }
        } else {
            throw new CallStateException("phone not ringing");
        }
    }

    @Override // com.android.internal.telephony.Connection
    public void hangup() throws CallStateException {
        if (!this.mDisconnected) {
            this.mOwner.hangup(this);
            return;
        }
        throw new CallStateException("disconnected");
    }

    @Override // com.android.internal.telephony.Connection
    public void separate() throws CallStateException {
        throw new CallStateException("not supported");
    }

    @Override // com.android.internal.telephony.Connection
    public void proceedAfterWaitChar() {
        if (this.mPostDialState != Connection.PostDialState.WAIT) {
            Rlog.w(LOG_TAG, "ImsPhoneConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WAIT but was " + this.mPostDialState);
            return;
        }
        setPostDialState(Connection.PostDialState.STARTED);
        processNextPostDialChar();
    }

    @Override // com.android.internal.telephony.Connection
    public void proceedAfterWildChar(String str) {
        if (this.mPostDialState != Connection.PostDialState.WILD) {
            Rlog.w(LOG_TAG, "ImsPhoneConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WILD but was " + this.mPostDialState);
            return;
        }
        setPostDialState(Connection.PostDialState.STARTED);
        this.mPostDialString = str + this.mPostDialString.substring(this.mNextPostDialChar);
        this.mNextPostDialChar = 0;
        Rlog.d(LOG_TAG, "proceedAfterWildChar: new postDialString is " + this.mPostDialString);
        processNextPostDialChar();
    }

    @Override // com.android.internal.telephony.Connection
    public void cancelPostDial() {
        setPostDialState(Connection.PostDialState.CANCELLED);
    }

    /* access modifiers changed from: package-private */
    public void onHangupLocal() {
        this.mCause = 3;
    }

    @Override // com.android.internal.telephony.Connection
    public boolean onDisconnect(int cause) {
        Rlog.d(LOG_TAG, "onDisconnect: cause=" + cause);
        this.mCause = cause;
        return onDisconnect();
    }

    /* access modifiers changed from: package-private */
    public int getImsIndex() throws CallStateException {
        String sIndex = this.mImsCall.getCallSession().getCallId();
        if (sIndex == null || sIndex.equals(PhoneConfigurationManager.SSSS)) {
            throw new CallStateException("ISM index not yet assigned");
        }
        int iIndex = -1;
        try {
            iIndex = Integer.parseInt(sIndex);
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, "getImsIndex: NumberFormatException ex");
        }
        if (iIndex >= 0) {
            return iIndex;
        }
        throw new CallStateException("ISM index not yet assigned");
    }

    @UnsupportedAppUsage
    public boolean onDisconnect() {
        boolean changed;
        boolean changed2 = false;
        if (!this.mDisconnected) {
            this.mDisconnectTime = System.currentTimeMillis();
            this.mDuration = SystemClock.elapsedRealtime() - this.mConnectTimeReal;
            this.mDisconnected = true;
            this.mOwner.mPhone.notifyDisconnect(this);
            notifyDisconnect(this.mCause);
            ImsPhoneCallTracker imsPhoneCallTracker = this.mOwner;
            imsPhoneCallTracker.updateCallLog(this, imsPhoneCallTracker.mPhone);
            ImsPhoneCall imsPhoneCall = this.mParent;
            if (imsPhoneCall != null) {
                changed = imsPhoneCall.connectionDisconnected(this);
            } else {
                Rlog.d(LOG_TAG, "onDisconnect: no parent");
                changed = false;
            }
            synchronized (this) {
                if (this.mRttTextHandler != null) {
                    this.mRttTextHandler.tearDown();
                }
                if (this.mImsCall != null) {
                    this.mImsCall.close();
                }
                this.mImsCall = null;
            }
            changed2 = changed;
        }
        releaseWakeLock();
        return changed2;
    }

    /* access modifiers changed from: package-private */
    public void onConnectedInOrOut() {
        this.mConnectTime = System.currentTimeMillis();
        this.mConnectTimeReal = SystemClock.elapsedRealtime();
        this.mDuration = 0;
        Rlog.d(LOG_TAG, "onConnectedInOrOut: connectTime=" + this.mConnectTime);
        if (!this.mIsIncoming) {
            processNextPostDialChar();
        }
        releaseWakeLock();
    }

    /* access modifiers changed from: package-private */
    public void onStartedHolding() {
        this.mHoldingStartTime = SystemClock.elapsedRealtime();
    }

    private boolean processPostDialChar(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            Message dtmfComplete = this.mHandler.obtainMessage(1);
            dtmfComplete.replyTo = this.mHandlerMessenger;
            this.mOwner.sendDtmf(c, dtmfComplete);
        } else if (c == ',') {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(2), 3000);
        } else if (c == ';') {
            setPostDialState(Connection.PostDialState.WAIT);
        } else if (c != 'N') {
            return false;
        } else {
            setPostDialState(Connection.PostDialState.WILD);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        releaseWakeLock();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processNextPostDialChar() {
        char c;
        Message notifyMessage;
        if (this.mPostDialState != Connection.PostDialState.CANCELLED) {
            if (this.mPostDialString == null || this.mPostDialString.length() <= this.mNextPostDialChar) {
                setPostDialState(Connection.PostDialState.COMPLETE);
                c = 0;
            } else {
                setPostDialState(Connection.PostDialState.STARTED);
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
            Registrant postDialHandler = this.mOwner.mPhone.getPostDialHandler();
            if (postDialHandler != null && (notifyMessage = postDialHandler.messageForRegistrant()) != null) {
                Connection.PostDialState state = this.mPostDialState;
                AsyncResult ar = AsyncResult.forMessage(notifyMessage);
                ar.result = this;
                ar.userObj = state;
                notifyMessage.arg1 = c;
                notifyMessage.sendToTarget();
            }
        }
    }

    private void setPostDialState(Connection.PostDialState s) {
        if (this.mPostDialState != Connection.PostDialState.STARTED && s == Connection.PostDialState.STARTED) {
            acquireWakeLock();
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 60000);
        } else if (this.mPostDialState == Connection.PostDialState.STARTED && s != Connection.PostDialState.STARTED) {
            this.mHandler.removeMessages(4);
            releaseWakeLock();
        }
        this.mPostDialState = s;
        notifyPostDialListeners();
    }

    @UnsupportedAppUsage
    private void createWakeLock(Context context) {
        this.mPartialWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
    }

    @UnsupportedAppUsage
    private void acquireWakeLock() {
        Rlog.d(LOG_TAG, "acquireWakeLock");
        this.mPartialWakeLock.acquire();
    }

    /* access modifiers changed from: package-private */
    public void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.mPartialWakeLock;
        if (wakeLock != null) {
            synchronized (wakeLock) {
                if (this.mPartialWakeLock.isHeld()) {
                    Rlog.d(LOG_TAG, "releaseWakeLock");
                    this.mPartialWakeLock.release();
                }
            }
        }
    }

    private void fetchDtmfToneDelay(Phone phone) {
        PersistableBundle b = ((CarrierConfigManager) phone.getContext().getSystemService("carrier_config")).getConfigForSubId(phone.getSubId());
        if (b != null) {
            this.mDtmfToneDelay = b.getInt("ims_dtmf_tone_delay_int");
        }
    }

    @Override // com.android.internal.telephony.Connection
    public int getNumberPresentation() {
        return this.mNumberPresentation;
    }

    @Override // com.android.internal.telephony.Connection
    public UUSInfo getUUSInfo() {
        return this.mUusInfo;
    }

    @Override // com.android.internal.telephony.Connection
    public com.android.internal.telephony.Connection getOrigConnection() {
        return null;
    }

    @Override // com.android.internal.telephony.Connection
    @UnsupportedAppUsage
    public synchronized boolean isMultiparty() {
        return this.mImsCall != null && this.mImsCall.isMultiparty();
    }

    @Override // com.android.internal.telephony.Connection
    public synchronized boolean isConferenceHost() {
        return this.mImsCall != null && this.mImsCall.isConferenceHost();
    }

    @Override // com.android.internal.telephony.Connection
    public boolean isMemberOfPeerConference() {
        return !isConferenceHost();
    }

    public synchronized ImsCall getImsCall() {
        return this.mImsCall;
    }

    public synchronized void setImsCall(ImsCall imsCall) {
        this.mImsCall = imsCall;
    }

    public void changeParent(ImsPhoneCall parent) {
        this.mParent = parent;
    }

    public boolean update(ImsCall imsCall, Call.State state) {
        if (state == Call.State.ACTIVE) {
            if (imsCall.isPendingHold()) {
                Rlog.w(LOG_TAG, "update : state is ACTIVE, but call is pending hold, skipping");
                return false;
            }
            if (this.mParent.getState().isRinging() || this.mParent.getState().isDialing()) {
                onConnectedInOrOut();
            }
            if (this.mParent.getState().isRinging() || this.mParent == this.mOwner.mBackgroundCall || this.mParent == this.mOwner.mRingingCall) {
                this.mParent.detach(this);
                this.mParent = this.mOwner.mForegroundCall;
                this.mParent.attach(this);
            }
        } else if (state == Call.State.HOLDING) {
            onStartedHolding();
            if (isImsAsNormal && (this.mParent == this.mOwner.mForegroundCall || this.mParent == this.mOwner.mRingingCall)) {
                this.mParent.detach(this);
                this.mParent = this.mOwner.mBackgroundCall;
                this.mParent.attach(this);
            }
        }
        boolean updateParent = this.mParent.update(this, imsCall, state);
        boolean updateAddressDisplay = updateAddressDisplay(imsCall);
        boolean updateMediaCapabilities = updateMediaCapabilities(imsCall);
        boolean updateExtras = updateExtras(imsCall);
        if (updateParent || updateAddressDisplay || updateMediaCapabilities || updateExtras) {
            return true;
        }
        return false;
    }

    @Override // com.android.internal.telephony.Connection
    public int getPreciseDisconnectCause() {
        return this.mPreciseDisconnectCause;
    }

    public void setPreciseDisconnectCause(int cause) {
        this.mPreciseDisconnectCause = cause;
    }

    @Override // com.android.internal.telephony.Connection
    public void onDisconnectConferenceParticipant(Uri endpoint) {
        ImsCall imsCall = getImsCall();
        if (imsCall != null) {
            try {
                imsCall.removeParticipants(new String[]{endpoint.toString()});
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "onDisconnectConferenceParticipant: no session in place. Failed to disconnect endpoint = " + endpoint);
            }
        }
    }

    public void setConferenceConnectTime(long conferenceConnectTime) {
        this.mConferenceConnectTime = conferenceConnectTime;
    }

    public long getConferenceConnectTime() {
        return this.mConferenceConnectTime;
    }

    public boolean updateAddressDisplay(ImsCall imsCall) {
        if (imsCall == null) {
            return false;
        }
        boolean changed = false;
        ImsCallProfile callProfile = imsCall.getCallProfile();
        if (callProfile == null) {
            return false;
        }
        if (!isIncoming() && !HuaweiTelephonyConfigs.isQcomPlatform()) {
            return false;
        }
        String address = callProfile.getCallExtra("oi");
        String name = callProfile.getCallExtra("cna");
        int nump = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("oir"));
        int namep = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("cnap"));
        String redirect_address = imsCall.getCallProfile().getCallExtra("redirect_number");
        int redirect_nump = ImsCallProfile.OIRToPresentation(callProfile.getCallExtraInt("redirect_number_presentation"));
        Rlog.d(LOG_TAG, "updateAddressDisplay: callId = " + getTelecomCallId() + " address = " + Rlog.pii(LOG_TAG, address) + " name = " + Rlog.pii(LOG_TAG, name) + " nump = " + nump + " namep = " + namep);
        if (isIncoming() || !HuaweiTelephonyConfigs.isQcomPlatform() || !TextUtils.isEmpty(address)) {
            if (equalsHandlesNulls(this.mAddress, address)) {
                this.mAddress = address;
                changed = true;
            }
            if (!equalsHandlesNulls(this.mRedirectAddress, redirect_address) && mIsDocomo) {
                this.mRedirectAddress = address;
                changed = true;
            }
            if (this.mIsMergeInProcess) {
                return changed;
            }
            if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                if (!equalsHandlesNulls(this.mAddress, address)) {
                    this.mAddress = address;
                    changed = true;
                }
            } else if (!equalsBaseDialString(this.mAddress, address)) {
                this.mAddress = address;
                changed = true;
            }
            if (TextUtils.isEmpty(name)) {
                if (!TextUtils.isEmpty(this.mCnapName)) {
                    this.mCnapName = PhoneConfigurationManager.SSSS;
                    changed = true;
                }
            } else if (!name.equals(this.mCnapName)) {
                this.mCnapName = name;
                changed = true;
            }
            if (this.mNumberPresentation != nump) {
                this.mNumberPresentation = nump;
                changed = true;
            }
            if (mIsDocomo && this.mRedirectNumberPresentation != redirect_nump) {
                this.mRedirectNumberPresentation = redirect_nump;
                changed = true;
            }
            if (this.mCnapNamePresentation == namep) {
                return changed;
            }
            this.mCnapNamePresentation = namep;
            return true;
        }
        Rlog.d(LOG_TAG, "Qcom address null return");
        return false;
    }

    public boolean updateMediaCapabilities(ImsCall imsCall) {
        int capabilities;
        if (imsCall == null) {
            return false;
        }
        boolean changed = false;
        try {
            ImsCallProfile negotiatedCallProfile = imsCall.getCallProfile();
            if (negotiatedCallProfile != null) {
                int oldVideoState = getVideoState();
                int newVideoState = ImsCallProfile.getVideoStateFromImsCallProfile(negotiatedCallProfile);
                if (oldVideoState != newVideoState) {
                    if (VideoProfile.isPaused(oldVideoState) && !VideoProfile.isPaused(newVideoState)) {
                        this.mShouldIgnoreVideoStateChanges = false;
                    }
                    if (!this.mShouldIgnoreVideoStateChanges) {
                        updateVideoState(newVideoState);
                        changed = true;
                    } else {
                        Rlog.d(LOG_TAG, "updateMediaCapabilities - ignoring video state change due to paused state.");
                    }
                    if (!VideoProfile.isPaused(oldVideoState) && VideoProfile.isPaused(newVideoState)) {
                        this.mShouldIgnoreVideoStateChanges = true;
                    }
                }
                if (negotiatedCallProfile.mMediaProfile != null) {
                    this.mIsRttEnabledForCall = negotiatedCallProfile.mMediaProfile.isRttCall();
                    if (this.mIsRttEnabledForCall && this.mRttTextHandler == null) {
                        Rlog.d(LOG_TAG, "updateMediaCapabilities -- turning RTT on, profile=" + negotiatedCallProfile);
                        startRttTextProcessing();
                        onRttInitiated();
                        changed = true;
                    } else if (!this.mIsRttEnabledForCall && this.mRttTextHandler != null) {
                        Rlog.d(LOG_TAG, "updateMediaCapabilities -- turning RTT off, profile=" + negotiatedCallProfile);
                        this.mRttTextHandler.tearDown();
                        this.mRttTextHandler = null;
                        onRttTerminated();
                        changed = true;
                    }
                }
            }
            int capabilities2 = getConnectionCapabilities();
            if (this.mOwner.isCarrierDowngradeOfVtCallSupported()) {
                capabilities = addCapability(capabilities2, 3);
            } else {
                capabilities = removeCapability(capabilities2, 3);
            }
            ImsCallProfile localCallProfile = imsCall.getLocalCallProfile();
            Rlog.v(LOG_TAG, "update localCallProfile=" + localCallProfile);
            if (localCallProfile != null) {
                capabilities = applyLocalCallCapabilities(localCallProfile, capabilities);
            }
            ImsCallProfile remoteCallProfile = imsCall.getRemoteCallProfile();
            Rlog.v(LOG_TAG, "update remoteCallProfile=" + remoteCallProfile);
            if (remoteCallProfile != null) {
                capabilities = applyRemoteCallCapabilities(remoteCallProfile, capabilities);
            }
            if (getConnectionCapabilities() != capabilities) {
                setConnectionCapabilities(capabilities);
                changed = true;
            }
            if (!this.mOwner.isViLteDataMetered()) {
                Rlog.v(LOG_TAG, "data is not metered");
            } else if (this.mImsVideoCallProviderWrapper != null) {
                this.mImsVideoCallProviderWrapper.setIsVideoEnabled(hasCapabilities(4));
            }
            if (!(localCallProfile == null || localCallProfile.mMediaProfile.mAudioQuality == this.mAudioCodec)) {
                this.mAudioCodec = localCallProfile.mMediaProfile.mAudioQuality;
                this.mMetrics.writeAudioCodecIms(this.mOwner.mPhone.getPhoneId(), imsCall.getCallSession());
            }
            int newAudioQuality = getAudioQualityFromCallProfile(localCallProfile, remoteCallProfile);
            if (getAudioQuality() == newAudioQuality) {
                return changed;
            }
            setAudioQuality(newAudioQuality);
            return true;
        } catch (ImsException e) {
            return false;
        }
    }

    private void updateVideoState(int newVideoState) {
        ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = this.mImsVideoCallProviderWrapper;
        if (imsVideoCallProviderWrapper != null) {
            imsVideoCallProviderWrapper.onVideoStateChanged(newVideoState);
        }
        setVideoState(newVideoState);
    }

    public void startRtt(Connection.RttTextStream textStream) {
        if (getImsCall() != null) {
            getImsCall().sendRttModifyRequest(true);
            setCurrentRttTextStream(textStream);
        }
    }

    public void stopRtt() {
        getImsCall().sendRttModifyRequest(false);
    }

    public void sendRttModifyResponse(Connection.RttTextStream textStream) {
        boolean accept = textStream != null;
        ImsCall imsCall = getImsCall();
        if (imsCall != null) {
            imsCall.sendRttModifyResponse(accept);
            if (accept) {
                setCurrentRttTextStream(textStream);
            } else {
                Rlog.e(LOG_TAG, "sendRttModifyResponse: foreground call has no connections");
            }
        }
    }

    public void onRttMessageReceived(String message) {
        synchronized (this) {
            if (this.mRttTextHandler == null) {
                Rlog.w(LOG_TAG, "onRttMessageReceived: RTT text handler not available. Attempting to create one.");
                if (this.mRttTextStream == null) {
                    Rlog.e(LOG_TAG, "onRttMessageReceived: Unable to process incoming message. No textstream available");
                    return;
                }
                createRttTextHandler();
            }
            this.mRttTextHandler.sendToInCall(message);
        }
    }

    public void onRttAudioIndicatorChanged(ImsStreamMediaProfile profile) {
        Bundle extras = new Bundle();
        extras.putBoolean("android.telecom.extra.IS_RTT_AUDIO_PRESENT", profile.isReceivingRttAudio());
        onConnectionEvent("android.telecom.event.RTT_AUDIO_INDICATION_CHANGED", extras);
    }

    public void setCurrentRttTextStream(Connection.RttTextStream rttTextStream) {
        synchronized (this) {
            this.mRttTextStream = rttTextStream;
            if (this.mRttTextHandler == null && this.mIsRttEnabledForCall) {
                Rlog.i(LOG_TAG, "setCurrentRttTextStream: Creating a text handler");
                createRttTextHandler();
            }
        }
    }

    public boolean hasRttTextStream() {
        return this.mRttTextStream != null;
    }

    public boolean isRttEnabledForCall() {
        return this.mIsRttEnabledForCall;
    }

    public void startRttTextProcessing() {
        synchronized (this) {
            if (this.mRttTextStream == null) {
                Rlog.w(LOG_TAG, "startRttTextProcessing: no RTT text stream. Ignoring.");
            } else if (this.mRttTextHandler != null) {
                Rlog.w(LOG_TAG, "startRttTextProcessing: RTT text handler already exists");
            } else {
                createRttTextHandler();
            }
        }
    }

    private void createRttTextHandler() {
        this.mRttTextHandler = new ImsRttTextHandler(Looper.getMainLooper(), new ImsRttTextHandler.NetworkWriter() {
            /* class com.android.internal.telephony.imsphone.$$Lambda$ImsPhoneConnection$gXYXXIQcibrbO2gQqP7d18avaBI */

            @Override // com.android.internal.telephony.imsphone.ImsRttTextHandler.NetworkWriter
            public final void write(String str) {
                ImsPhoneConnection.this.lambda$createRttTextHandler$0$ImsPhoneConnection(str);
            }
        });
        this.mRttTextHandler.initialize(this.mRttTextStream);
    }

    public /* synthetic */ void lambda$createRttTextHandler$0$ImsPhoneConnection(String message) {
        ImsCall imsCall = getImsCall();
        if (imsCall != null) {
            imsCall.sendRttMessage(message);
        }
    }

    private void updateImsCallRatFromExtras(Bundle extras) {
        if (extras.containsKey("CallRadioTech") || extras.containsKey("callRadioTech")) {
            ImsCall call = getImsCall();
            int callTech = 0;
            if (call != null) {
                callTech = call.getRadioTechnology();
            }
            setCallRadioTech(callTech);
        }
    }

    private void updateEmergencyCallFromExtras(Bundle extras) {
        if (extras != null && extras.getBoolean("e_call")) {
            setIsNetworkIdentifiedEmergencyCall(true);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateExtras(ImsCall imsCall) {
        if (imsCall == null) {
            return false;
        }
        ImsCallProfile callProfile = imsCall.getCallProfile();
        Bundle extras = callProfile != null ? callProfile.mCallExtras : null;
        if (extras == null) {
            Rlog.d(LOG_TAG, "Call profile extras are null.");
        }
        boolean changed = !areBundlesEqual(extras, this.mExtras);
        if (changed) {
            updateImsCallRatFromExtras(extras);
            updateEmergencyCallFromExtras(extras);
            this.mExtras.clear();
            this.mExtras.putAll(extras);
            setConnectionExtras(this.mExtras);
        }
        return changed;
    }

    private static boolean areBundlesEqual(Bundle extras, Bundle newExtras) {
        if (extras == null || newExtras == null) {
            return extras == newExtras;
        }
        if (extras.size() != newExtras.size()) {
            return false;
        }
        for (String key : extras.keySet()) {
            if (!(key == null || Objects.equals(extras.get(key), newExtras.get(key)))) {
                return false;
            }
        }
        return true;
    }

    private int getAudioQualityFromCallProfile(ImsCallProfile localCallProfile, ImsCallProfile remoteCallProfile) {
        if (localCallProfile == null || remoteCallProfile == null || localCallProfile.mMediaProfile == null) {
            return 1;
        }
        boolean isHighDef = false;
        boolean isEvsCodecHighDef = localCallProfile.mMediaProfile.mAudioQuality == 18 || localCallProfile.mMediaProfile.mAudioQuality == 19 || localCallProfile.mMediaProfile.mAudioQuality == 20;
        if ((localCallProfile.mMediaProfile.mAudioQuality == 2 || localCallProfile.mMediaProfile.mAudioQuality == 6 || isEvsCodecHighDef) && remoteCallProfile.getRestrictCause() == 0) {
            isHighDef = true;
        }
        if (isHighDef) {
            return 2;
        }
        return 1;
    }

    @Override // com.android.internal.telephony.Connection
    public String toString() {
        Call.State state = getState();
        StringBuilder sb = new StringBuilder();
        sb.append("[ImsPhoneConnection objId: ");
        sb.append(System.identityHashCode(this));
        sb.append(" state: ");
        if (state != null) {
            sb.append(state.toString());
        } else {
            sb.append("null");
        }
        sb.append(" mParent objId: ");
        ImsPhoneCall imsPhoneCall = this.mParent;
        if (imsPhoneCall == null) {
            sb.append("null");
        } else {
            sb.append(System.identityHashCode(imsPhoneCall));
        }
        sb.append(" telecomCallID: ");
        sb.append(getTelecomCallId());
        sb.append(" address: ");
        sb.append(Rlog.pii(LOG_TAG, "XXX"));
        sb.append(" ImsCall: ");
        ImsCall imsCall = this.mImsCall;
        if (imsCall == null) {
            sb.append("null");
        } else {
            sb.append(imsCall);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override // com.android.internal.telephony.Connection
    public void setVideoProvider(Connection.VideoProvider videoProvider) {
        super.setVideoProvider(videoProvider);
        if (videoProvider instanceof ImsVideoCallProviderWrapper) {
            this.mImsVideoCallProviderWrapper = (ImsVideoCallProviderWrapper) videoProvider;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isEmergency() {
        return this.mIsEmergency;
    }

    public void onReceiveSessionModifyResponse(int status, VideoProfile requestProfile, VideoProfile responseProfile) {
        if (status == 1 && this.mShouldIgnoreVideoStateChanges) {
            int currentVideoState = getVideoState();
            int newVideoState = responseProfile.getVideoState();
            int changedBits = (currentVideoState ^ newVideoState) & 3;
            if (changedBits != 0) {
                int currentVideoState2 = (currentVideoState & (~(changedBits & currentVideoState))) | (changedBits & newVideoState);
                Rlog.d(LOG_TAG, "onReceiveSessionModifyResponse : received " + VideoProfile.videoStateToString(requestProfile.getVideoState()) + " / " + VideoProfile.videoStateToString(responseProfile.getVideoState()) + " while paused ; sending new videoState = " + VideoProfile.videoStateToString(currentVideoState2));
                setVideoState(currentVideoState2);
            }
        }
    }

    public void pauseVideo(int source) {
        ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = this.mImsVideoCallProviderWrapper;
        if (imsVideoCallProviderWrapper != null) {
            imsVideoCallProviderWrapper.pauseVideo(getVideoState(), source);
        }
    }

    public void resumeVideo(int source) {
        ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = this.mImsVideoCallProviderWrapper;
        if (imsVideoCallProviderWrapper != null) {
            imsVideoCallProviderWrapper.resumeVideo(getVideoState(), source);
        }
    }

    public boolean wasVideoPausedFromSource(int source) {
        ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = this.mImsVideoCallProviderWrapper;
        if (imsVideoCallProviderWrapper == null) {
            return false;
        }
        return imsVideoCallProviderWrapper.wasVideoPausedFromSource(source);
    }

    public void handleMergeStart() {
        this.mIsMergeInProcess = true;
        onConnectionEvent("android.telecom.event.MERGE_START", null);
    }

    public void handleMergeComplete() {
        this.mIsMergeInProcess = false;
        onConnectionEvent("android.telecom.event.MERGE_COMPLETE", null);
    }

    public void changeToPausedState() {
        int newVideoState = getVideoState() | 4;
        Rlog.i(LOG_TAG, "ImsPhoneConnection: changeToPausedState - setting paused bit; newVideoState=" + VideoProfile.videoStateToString(newVideoState));
        updateVideoState(newVideoState);
        this.mShouldIgnoreVideoStateChanges = true;
    }

    public void changeToUnPausedState() {
        int newVideoState = getVideoState() & -5;
        Rlog.i(LOG_TAG, "ImsPhoneConnection: changeToUnPausedState - unsetting paused bit; newVideoState=" + VideoProfile.videoStateToString(newVideoState));
        updateVideoState(newVideoState);
        this.mShouldIgnoreVideoStateChanges = false;
    }

    public void setLocalVideoCapable(boolean isVideoEnabled) {
        this.mIsLocalVideoCapable = isVideoEnabled;
        Rlog.i(LOG_TAG, "setLocalVideoCapable: mIsLocalVideoCapable = " + this.mIsLocalVideoCapable + "; updating local video availability.");
        updateMediaCapabilities(getImsCall());
    }
}

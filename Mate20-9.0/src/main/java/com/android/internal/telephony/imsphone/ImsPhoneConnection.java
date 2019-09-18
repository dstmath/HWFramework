package com.android.internal.telephony.imsphone;

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
import android.text.TextUtils;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.imsphone.ImsRttTextHandler;
import java.util.Objects;

public class ImsPhoneConnection extends Connection implements ImsVideoCallProviderWrapper.ImsVideoProviderWrapperCallback {
    private static final boolean DBG = true;
    private static final int EVENT_DTMF_DELAY_DONE = 5;
    private static final int EVENT_DTMF_DONE = 1;
    private static final int EVENT_NEXT_POST_DIAL = 3;
    private static final int EVENT_PAUSE_DONE = 2;
    private static final int EVENT_WAKE_LOCK_TIMEOUT = 4;
    private static final String LOG_TAG = "ImsPhoneConnection";
    private static final int PAUSE_DELAY_MILLIS = 3000;
    private static final int WAKE_LOCK_TIMEOUT_MILLIS = 60000;
    private static final boolean isImsAsNormal = HuaweiTelephonyConfigs.isHisiPlatform();
    private static final boolean mIsDocomo = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private long mConferenceConnectTime = 0;
    private long mDisconnectTime;
    private boolean mDisconnected;
    /* access modifiers changed from: private */
    public int mDtmfToneDelay = 0;
    private Bundle mExtras = new Bundle();
    /* access modifiers changed from: private */
    public Handler mHandler;
    private Messenger mHandlerMessenger;
    private ImsCall mImsCall;
    private ImsVideoCallProviderWrapper mImsVideoCallProviderWrapper;
    private boolean mIsEmergency = false;
    private boolean mIsMergeInProcess = false;
    private boolean mIsRttEnabledForCall = false;
    private boolean mIsVideoEnabled = true;
    private ImsPhoneCallTracker mOwner;
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

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ImsPhoneConnection.this.mHandler.sendMessageDelayed(ImsPhoneConnection.this.mHandler.obtainMessage(5), (long) ImsPhoneConnection.this.mDtmfToneDelay);
                    return;
                case 2:
                case 3:
                case 5:
                    ImsPhoneConnection.this.processNextPostDialChar();
                    return;
                case 4:
                    ImsPhoneConnection.this.releaseWakeLock();
                    return;
                default:
                    return;
            }
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
        if (phone.getContext().getResources().getBoolean(17957066)) {
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
        fetchDtmfToneDelay(phone);
        if (phone.getContext().getResources().getBoolean(17957066)) {
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
            if (b != null) {
                return false;
            }
        } else if (b == null || !a.startsWith(b)) {
            return false;
        }
        return true;
    }

    private int applyLocalCallCapabilities(ImsCallProfile localProfile, int capabilities) {
        Rlog.i(LOG_TAG, "applyLocalCallCapabilities - localProfile = " + localProfile);
        int capabilities2 = removeCapability(capabilities, 4);
        if (!this.mIsVideoEnabled) {
            Rlog.i(LOG_TAG, "applyLocalCallCapabilities - disabling video (overidden)");
            return capabilities2;
        }
        switch (localProfile.mCallType) {
            case 3:
            case 4:
                capabilities2 = addCapability(capabilities2, 4);
                break;
        }
        return capabilities2;
    }

    private static int applyRemoteCallCapabilities(ImsCallProfile remoteProfile, int capabilities) {
        Rlog.w(LOG_TAG, "applyRemoteCallCapabilities - remoteProfile = " + remoteProfile);
        int capabilities2 = removeCapability(capabilities, 8);
        switch (remoteProfile.mCallType) {
            case 3:
            case 4:
                return addCapability(capabilities2, 8);
            default:
                return capabilities2;
        }
    }

    public String getOrigDialString() {
        return this.mDialString;
    }

    public ImsPhoneCall getCall() {
        return this.mParent;
    }

    public long getDisconnectTime() {
        return this.mDisconnectTime;
    }

    public long getHoldingStartTime() {
        return this.mHoldingStartTime;
    }

    public long getHoldDurationMillis() {
        if (getState() != Call.State.HOLDING) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mHoldingStartTime;
    }

    public void setDisconnectCause(int cause) {
        this.mCause = cause;
    }

    public String getVendorDisconnectCause() {
        return null;
    }

    public ImsPhoneCallTracker getOwner() {
        return this.mOwner;
    }

    public Call.State getState() {
        if (this.mDisconnected) {
            return Call.State.DISCONNECTED;
        }
        return super.getState();
    }

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

    public void hangup() throws CallStateException {
        if (!this.mDisconnected) {
            this.mOwner.hangup(this);
            return;
        }
        throw new CallStateException("disconnected");
    }

    public void separate() throws CallStateException {
        throw new CallStateException("not supported");
    }

    public void proceedAfterWaitChar() {
        if (this.mPostDialState != Connection.PostDialState.WAIT) {
            Rlog.w(LOG_TAG, "ImsPhoneConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WAIT but was " + this.mPostDialState);
            return;
        }
        setPostDialState(Connection.PostDialState.STARTED);
        processNextPostDialChar();
    }

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

    public void cancelPostDial() {
        setPostDialState(Connection.PostDialState.CANCELLED);
    }

    /* access modifiers changed from: package-private */
    public void onHangupLocal() {
        this.mCause = 3;
    }

    public boolean onDisconnect(int cause) {
        Rlog.d(LOG_TAG, "onDisconnect: cause=" + cause);
        this.mCause = cause;
        return onDisconnect();
    }

    /* access modifiers changed from: package-private */
    public int getImsIndex() throws CallStateException {
        String sIndex = this.mImsCall.getCallSession().getCallId();
        if (sIndex == null || sIndex.equals("")) {
            throw new CallStateException("ISM index not yet assigned");
        }
        int iIndex = Integer.parseInt(sIndex);
        if (iIndex >= 0) {
            return iIndex;
        }
        throw new CallStateException("ISM index not yet assigned");
    }

    public boolean onDisconnect() {
        boolean changed = false;
        if (!this.mDisconnected) {
            this.mDisconnectTime = System.currentTimeMillis();
            this.mDuration = SystemClock.elapsedRealtime() - this.mConnectTimeReal;
            this.mDisconnected = true;
            this.mOwner.mPhone.notifyDisconnect(this);
            notifyDisconnect(this.mCause);
            this.mOwner.updateCallLog(this, this.mOwner.mPhone);
            if (this.mParent != null) {
                changed = this.mParent.connectionDisconnected(this);
            } else {
                Rlog.d(LOG_TAG, "onDisconnect: no parent");
            }
            boolean changed2 = changed;
            synchronized (this) {
                if (this.mImsCall != null) {
                    this.mImsCall.close();
                }
                this.mImsCall = null;
            }
            changed = changed2;
        }
        releaseWakeLock();
        return changed;
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
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 3000);
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
    public void processNextPostDialChar() {
        char c;
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
            if (postDialHandler != null) {
                Message messageForRegistrant = postDialHandler.messageForRegistrant();
                Message notifyMessage = messageForRegistrant;
                if (messageForRegistrant != null) {
                    Connection.PostDialState state = this.mPostDialState;
                    AsyncResult ar = AsyncResult.forMessage(notifyMessage);
                    ar.result = this;
                    ar.userObj = state;
                    notifyMessage.arg1 = c;
                    notifyMessage.sendToTarget();
                }
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

    private void createWakeLock(Context context) {
        this.mPartialWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
    }

    private void acquireWakeLock() {
        Rlog.d(LOG_TAG, "acquireWakeLock");
        this.mPartialWakeLock.acquire();
    }

    /* access modifiers changed from: package-private */
    public void releaseWakeLock() {
        if (this.mPartialWakeLock != null) {
            synchronized (this.mPartialWakeLock) {
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

    public int getNumberPresentation() {
        return this.mNumberPresentation;
    }

    public UUSInfo getUUSInfo() {
        return this.mUusInfo;
    }

    public com.android.internal.telephony.Connection getOrigConnection() {
        return null;
    }

    public synchronized boolean isMultiparty() {
        return this.mImsCall != null && this.mImsCall.isMultiparty();
    }

    public synchronized boolean isConferenceHost() {
        return this.mImsCall != null && this.mImsCall.isConferenceHost();
    }

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
        boolean z = false;
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
            z = true;
        }
        return z;
    }

    public int getPreciseDisconnectCause() {
        return this.mPreciseDisconnectCause;
    }

    public void setPreciseDisconnectCause(int cause) {
        this.mPreciseDisconnectCause = cause;
    }

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
        if (callProfile != null && (isIncoming() || HuaweiTelephonyConfigs.isQcomPlatform())) {
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
                if (!this.mIsMergeInProcess) {
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
                            this.mCnapName = "";
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
                    if (this.mCnapNamePresentation != namep) {
                        this.mCnapNamePresentation = namep;
                        changed = true;
                    }
                }
            } else {
                Rlog.d(LOG_TAG, "Qcom address null return");
                return false;
            }
        }
        return changed;
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
            int newAudioQuality = getAudioQualityFromCallProfile(localCallProfile, remoteCallProfile);
            if (getAudioQuality() != newAudioQuality) {
                setAudioQuality(newAudioQuality);
                changed = true;
            }
        } catch (ImsException e) {
        }
        return changed;
    }

    private void updateVideoState(int newVideoState) {
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.onVideoStateChanged(newVideoState);
        }
        setVideoState(newVideoState);
    }

    public void sendRttModifyRequest(Connection.RttTextStream textStream) {
        ImsCall imsCall = getImsCall();
        if (imsCall != null) {
            imsCall.sendRttModifyRequest();
            setCurrentRttTextStream(textStream);
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        r2.mRttTextHandler.sendToInCall(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        return;
     */
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
        }
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
            public final void write(String str) {
                ImsPhoneConnection.lambda$createRttTextHandler$0(ImsPhoneConnection.this, str);
            }
        });
        this.mRttTextHandler.initialize(this.mRttTextStream);
    }

    public static /* synthetic */ void lambda$createRttTextHandler$0(ImsPhoneConnection imsPhoneConnection, String message) {
        ImsCall imsCall = imsPhoneConnection.getImsCall();
        if (imsCall != null) {
            imsCall.sendRttMessage(message);
        }
    }

    private void updateWifiStateFromExtras(Bundle extras) {
        if (extras.containsKey("CallRadioTech") || extras.containsKey("callRadioTech")) {
            ImsCall call = getImsCall();
            boolean isWifi = false;
            if (call != null) {
                isWifi = call.isWifiCall();
            }
            if (isWifi() != isWifi) {
                setWifi(isWifi);
            }
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
            updateWifiStateFromExtras(extras);
            this.mExtras.clear();
            this.mExtras.putAll(extras);
            setConnectionExtras(this.mExtras);
        }
        return changed;
    }

    private static boolean areBundlesEqual(Bundle extras, Bundle newExtras) {
        boolean z = true;
        if (extras == null || newExtras == null) {
            if (extras != newExtras) {
                z = false;
            }
            return z;
        } else if (extras.size() != newExtras.size()) {
            return false;
        } else {
            for (String key : extras.keySet()) {
                if (key != null && !Objects.equals(extras.get(key), newExtras.get(key))) {
                    return false;
                }
            }
            return true;
        }
    }

    private int getAudioQualityFromCallProfile(ImsCallProfile localCallProfile, ImsCallProfile remoteCallProfile) {
        int i = 1;
        if (localCallProfile == null || remoteCallProfile == null || localCallProfile.mMediaProfile == null) {
            return 1;
        }
        boolean isHighDef = false;
        boolean isEvsCodecHighDef = localCallProfile.mMediaProfile.mAudioQuality == 18 || localCallProfile.mMediaProfile.mAudioQuality == 19 || localCallProfile.mMediaProfile.mAudioQuality == 20;
        if ((localCallProfile.mMediaProfile.mAudioQuality == 2 || localCallProfile.mMediaProfile.mAudioQuality == 6 || isEvsCodecHighDef) && remoteCallProfile.mRestrictCause == 0) {
            isHighDef = true;
        }
        if (isHighDef) {
            i = 2;
        }
        return i;
    }

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
        if (this.mParent == null) {
            sb.append("null");
        } else {
            sb.append(System.identityHashCode(this.mParent));
        }
        sb.append(" telecomCallID: ");
        sb.append(getTelecomCallId());
        sb.append(" ImsCall: ");
        if (this.mImsCall == null) {
            sb.append("null");
        } else {
            sb.append(this.mImsCall);
        }
        sb.append("]");
        return sb.toString();
    }

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
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.pauseVideo(getVideoState(), source);
        }
    }

    public void resumeVideo(int source) {
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.resumeVideo(getVideoState(), source);
        }
    }

    public boolean wasVideoPausedFromSource(int source) {
        if (this.mImsVideoCallProviderWrapper == null) {
            return false;
        }
        return this.mImsVideoCallProviderWrapper.wasVideoPausedFromSource(source);
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

    public void handleDataEnabledChange(boolean isDataEnabled) {
        this.mIsVideoEnabled = isDataEnabled;
        Rlog.i(LOG_TAG, "handleDataEnabledChange: isDataEnabled=" + isDataEnabled + "; updating local video availability.");
        updateMediaCapabilities(getImsCall());
        if (this.mImsVideoCallProviderWrapper != null) {
            this.mImsVideoCallProviderWrapper.setIsVideoEnabled(hasCapabilities(4));
        }
    }
}

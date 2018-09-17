package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.sip.SipPhone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CallManager extends AbstractCallManager {
    private static final boolean DBG = true;
    private static final int EVENT_CALL_WAITING = 108;
    private static final int EVENT_CDMA_OTA_STATUS_CHANGE = 111;
    private static final int EVENT_CDMA_WAITING_NUMBER_CHANGED = 153;
    private static final int EVENT_DISCONNECT = 100;
    private static final int EVENT_DISPLAY_INFO = 109;
    private static final int EVENT_ECM_TIMER_RESET = 115;
    private static final int EVENT_INCOMING_RING = 104;
    private static final int EVENT_IN_CALL_VOICE_PRIVACY_OFF = 107;
    private static final int EVENT_IN_CALL_VOICE_PRIVACY_ON = 106;
    private static final int EVENT_LINE_CONTROL_INFO = 152;
    private static final int EVENT_MMI_COMPLETE = 114;
    private static final int EVENT_MMI_INITIATE = 113;
    private static final int EVENT_NEW_RINGING_CONNECTION = 102;
    private static final int EVENT_ONHOLD_TONE = 120;
    private static final int EVENT_POST_DIAL_CHARACTER = 119;
    private static final int EVENT_PRECISE_CALL_STATE_CHANGED = 101;
    private static final int EVENT_RESEND_INCALL_MUTE = 112;
    private static final int EVENT_RINGBACK_TONE = 105;
    private static final int EVENT_SERVICE_STATE_CHANGED = 118;
    private static final int EVENT_SIGNAL_INFO = 110;
    private static final int EVENT_SUBSCRIPTION_INFO_READY = 116;
    private static final int EVENT_SUPP_SERVICE_FAILED = 117;
    private static final int EVENT_TTY_MODE_RECEIVED = 122;
    private static final int EVENT_UNKNOWN_CONNECTION = 103;
    private static final CallManager INSTANCE = null;
    private static final String LOG_TAG = "CallManager";
    private static final boolean VDBG = false;
    private final ArrayList<Call> mBackgroundCalls;
    protected final RegistrantList mCallWaitingRegistrants;
    protected final RegistrantList mCdmaOtaStatusChangeRegistrants;
    protected final RegistrantList mCdmaWaitingNumberChangedRegistrants;
    private Phone mDefaultPhone;
    protected final RegistrantList mDisconnectRegistrants;
    protected final RegistrantList mDisplayInfoRegistrants;
    protected final RegistrantList mEcmTimerResetRegistrants;
    private final ArrayList<Connection> mEmptyConnections;
    private final ArrayList<Call> mForegroundCalls;
    private final HashMap<Phone, CallManagerHandler> mHandlerMap;
    protected final RegistrantList mInCallVoicePrivacyOffRegistrants;
    protected final RegistrantList mInCallVoicePrivacyOnRegistrants;
    protected final RegistrantList mIncomingRingRegistrants;
    protected final RegistrantList mLineControlInfoRegistrants;
    protected final RegistrantList mMmiCompleteRegistrants;
    protected final RegistrantList mMmiInitiateRegistrants;
    protected final RegistrantList mMmiRegistrants;
    protected final RegistrantList mNewRingingConnectionRegistrants;
    protected final RegistrantList mOnHoldToneRegistrants;
    private final ArrayList<Phone> mPhones;
    protected final RegistrantList mPostDialCharacterRegistrants;
    protected final RegistrantList mPreciseCallStateRegistrants;
    private Object mRegistrantidentifier;
    protected final RegistrantList mResendIncallMuteRegistrants;
    protected final RegistrantList mRingbackToneRegistrants;
    private final ArrayList<Call> mRingingCalls;
    protected final RegistrantList mServiceStateChangedRegistrants;
    protected final RegistrantList mSignalInfoRegistrants;
    private boolean mSpeedUpAudioForMtCall;
    protected final RegistrantList mSubscriptionInfoReadyRegistrants;
    protected final RegistrantList mSuppServiceFailedRegistrants;
    protected final RegistrantList mTtyModeReceivedRegistrants;
    protected final RegistrantList mUnknownConnectionRegistrants;

    private class CallManagerHandler extends Handler {
        private CallManagerHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CallManager.EVENT_DISCONNECT /*100*/:
                    CallManager.this.disconnectNotify(msg);
                    CallManager.this.mDisconnectRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_PRECISE_CALL_STATE_CHANGED /*101*/:
                    CallManager.this.mPreciseCallStateRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_NEW_RINGING_CONNECTION /*102*/:
                    CallManager.this.mNewRingingConnectionRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_UNKNOWN_CONNECTION /*103*/:
                    CallManager.this.mUnknownConnectionRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_INCOMING_RING /*104*/:
                    if (!CallManager.this.hasActiveFgCall()) {
                        CallManager.this.mIncomingRingRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                    }
                case CallManager.EVENT_RINGBACK_TONE /*105*/:
                    CallManager.this.mRingbackToneRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_IN_CALL_VOICE_PRIVACY_ON /*106*/:
                    CallManager.this.mInCallVoicePrivacyOnRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_IN_CALL_VOICE_PRIVACY_OFF /*107*/:
                    CallManager.this.mInCallVoicePrivacyOffRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_CALL_WAITING /*108*/:
                    CallManager.this.mCallWaitingRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_DISPLAY_INFO /*109*/:
                    CallManager.this.mDisplayInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_SIGNAL_INFO /*110*/:
                    CallManager.this.mSignalInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_CDMA_OTA_STATUS_CHANGE /*111*/:
                    CallManager.this.mCdmaOtaStatusChangeRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_RESEND_INCALL_MUTE /*112*/:
                    CallManager.this.mResendIncallMuteRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_MMI_INITIATE /*113*/:
                    CallManager.this.mMmiInitiateRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_MMI_COMPLETE /*114*/:
                    CallManager.this.mMmiCompleteRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_ECM_TIMER_RESET /*115*/:
                    CallManager.this.mEcmTimerResetRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_SUBSCRIPTION_INFO_READY /*116*/:
                    CallManager.this.mSubscriptionInfoReadyRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_SUPP_SERVICE_FAILED /*117*/:
                    CallManager.this.mSuppServiceFailedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_SERVICE_STATE_CHANGED /*118*/:
                    CallManager.this.mServiceStateChangedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_POST_DIAL_CHARACTER /*119*/:
                    for (int i = 0; i < CallManager.this.mPostDialCharacterRegistrants.size(); i++) {
                        Message notifyMsg = ((Registrant) CallManager.this.mPostDialCharacterRegistrants.get(i)).messageForRegistrant();
                        notifyMsg.obj = msg.obj;
                        notifyMsg.arg1 = msg.arg1;
                        notifyMsg.sendToTarget();
                    }
                case CallManager.EVENT_ONHOLD_TONE /*120*/:
                    CallManager.this.mOnHoldToneRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_TTY_MODE_RECEIVED /*122*/:
                    CallManager.this.mTtyModeReceivedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_LINE_CONTROL_INFO /*152*/:
                    CallManager.this.mLineControlInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                case CallManager.EVENT_CDMA_WAITING_NUMBER_CHANGED /*153*/:
                    CallManager.this.mCdmaWaitingNumberChangedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.CallManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.CallManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CallManager.<clinit>():void");
    }

    private CallManager() {
        this.mEmptyConnections = new ArrayList();
        this.mHandlerMap = new HashMap();
        this.mSpeedUpAudioForMtCall = false;
        this.mRegistrantidentifier = new Object();
        this.mPreciseCallStateRegistrants = new RegistrantList();
        this.mNewRingingConnectionRegistrants = new RegistrantList();
        this.mIncomingRingRegistrants = new RegistrantList();
        this.mDisconnectRegistrants = new RegistrantList();
        this.mMmiRegistrants = new RegistrantList();
        this.mUnknownConnectionRegistrants = new RegistrantList();
        this.mRingbackToneRegistrants = new RegistrantList();
        this.mOnHoldToneRegistrants = new RegistrantList();
        this.mInCallVoicePrivacyOnRegistrants = new RegistrantList();
        this.mInCallVoicePrivacyOffRegistrants = new RegistrantList();
        this.mCallWaitingRegistrants = new RegistrantList();
        this.mDisplayInfoRegistrants = new RegistrantList();
        this.mSignalInfoRegistrants = new RegistrantList();
        this.mCdmaOtaStatusChangeRegistrants = new RegistrantList();
        this.mResendIncallMuteRegistrants = new RegistrantList();
        this.mMmiInitiateRegistrants = new RegistrantList();
        this.mMmiCompleteRegistrants = new RegistrantList();
        this.mEcmTimerResetRegistrants = new RegistrantList();
        this.mSubscriptionInfoReadyRegistrants = new RegistrantList();
        this.mSuppServiceFailedRegistrants = new RegistrantList();
        this.mServiceStateChangedRegistrants = new RegistrantList();
        this.mPostDialCharacterRegistrants = new RegistrantList();
        this.mTtyModeReceivedRegistrants = new RegistrantList();
        this.mLineControlInfoRegistrants = new RegistrantList();
        this.mCdmaWaitingNumberChangedRegistrants = new RegistrantList();
        this.mPhones = new ArrayList();
        this.mRingingCalls = new ArrayList();
        this.mBackgroundCalls = new ArrayList();
        this.mForegroundCalls = new ArrayList();
        this.mDefaultPhone = null;
    }

    public static CallManager getInstance() {
        return INSTANCE;
    }

    public List<Phone> getAllPhones() {
        return Collections.unmodifiableList(this.mPhones);
    }

    private Phone getPhone(int subId) {
        for (Phone phone : this.mPhones) {
            if (phone.getSubId() == subId && phone.getPhoneType() != 5) {
                return phone;
            }
        }
        return null;
    }

    public State getState() {
        State s = State.IDLE;
        for (Phone phone : this.mPhones) {
            if (phone.getState() == State.RINGING) {
                s = State.RINGING;
            } else if (phone.getState() == State.OFFHOOK && s == State.IDLE) {
                s = State.OFFHOOK;
            }
        }
        return s;
    }

    public State getState(int subId) {
        State s = State.IDLE;
        for (Phone phone : this.mPhones) {
            if (phone.getSubId() == subId) {
                if (phone.getState() == State.RINGING) {
                    s = State.RINGING;
                } else if (phone.getState() == State.OFFHOOK && s == State.IDLE) {
                    s = State.OFFHOOK;
                }
            }
        }
        return s;
    }

    public int getServiceState() {
        int resultState = 1;
        for (Phone phone : this.mPhones) {
            int serviceState = phone.getServiceState().getState();
            if (serviceState == 0) {
                return serviceState;
            }
            if (serviceState == 1) {
                if (resultState == 2 || resultState == 3) {
                    resultState = serviceState;
                }
            } else if (serviceState == 2 && resultState == 3) {
                resultState = serviceState;
            }
        }
        return resultState;
    }

    public int getServiceState(int subId) {
        int resultState = 1;
        for (Phone phone : this.mPhones) {
            if (phone.getSubId() == subId) {
                int serviceState = phone.getServiceState().getState();
                if (serviceState == 0) {
                    return serviceState;
                }
                if (serviceState == 1) {
                    if (resultState == 2 || resultState == 3) {
                        resultState = serviceState;
                    }
                } else if (serviceState == 2 && resultState == 3) {
                    resultState = serviceState;
                }
            }
        }
        return resultState;
    }

    public Phone getPhoneInCall() {
        if (!getFirstActiveRingingCall().isIdle()) {
            return getFirstActiveRingingCall().getPhone();
        }
        if (getActiveFgCall().isIdle()) {
            return getFirstActiveBgCall().getPhone();
        }
        return getActiveFgCall().getPhone();
    }

    public Phone getPhoneInCall(int subId) {
        if (!getFirstActiveRingingCall(subId).isIdle()) {
            return getFirstActiveRingingCall(subId).getPhone();
        }
        if (getActiveFgCall(subId).isIdle()) {
            return getFirstActiveBgCall(subId).getPhone();
        }
        return getActiveFgCall(subId).getPhone();
    }

    public boolean registerPhone(Phone phone) {
        if (phone == null || this.mPhones.contains(phone)) {
            return false;
        }
        Rlog.d(LOG_TAG, "registerPhone(" + phone.getPhoneName() + " " + phone + ")");
        if (this.mPhones.isEmpty()) {
            this.mDefaultPhone = phone;
        }
        this.mPhones.add(phone);
        this.mRingingCalls.add(phone.getRingingCall());
        this.mBackgroundCalls.add(phone.getBackgroundCall());
        this.mForegroundCalls.add(phone.getForegroundCall());
        registerForPhoneStates(phone);
        return DBG;
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mLineControlInfoRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mLineControlInfoRegistrants.remove(h);
    }

    public void unregisterPhone(Phone phone) {
        if (phone != null && this.mPhones.contains(phone)) {
            Rlog.d(LOG_TAG, "unregisterPhone(" + phone.getPhoneName() + " " + phone + ")");
            Phone imsPhone = phone.getImsPhone();
            if (imsPhone != null) {
                unregisterPhone(imsPhone);
            }
            this.mPhones.remove(phone);
            this.mRingingCalls.remove(phone.getRingingCall());
            this.mBackgroundCalls.remove(phone.getBackgroundCall());
            this.mForegroundCalls.remove(phone.getForegroundCall());
            unregisterForPhoneStates(phone);
            if (phone != this.mDefaultPhone) {
                return;
            }
            if (this.mPhones.isEmpty()) {
                this.mDefaultPhone = null;
            } else {
                this.mDefaultPhone = (Phone) this.mPhones.get(0);
            }
        }
    }

    public Phone getDefaultPhone() {
        return this.mDefaultPhone;
    }

    public Phone getFgPhone() {
        return getActiveFgCall().getPhone();
    }

    public Phone getFgPhone(int subId) {
        return getActiveFgCall(subId).getPhone();
    }

    public Phone getBgPhone() {
        return getFirstActiveBgCall().getPhone();
    }

    public Phone getBgPhone(int subId) {
        return getFirstActiveBgCall(subId).getPhone();
    }

    public Phone getRingingPhone() {
        return getFirstActiveRingingCall().getPhone();
    }

    public Phone getRingingPhone(int subId) {
        return getFirstActiveRingingCall(subId).getPhone();
    }

    private Context getContext() {
        Phone defaultPhone = getDefaultPhone();
        if (defaultPhone == null) {
            return null;
        }
        return defaultPhone.getContext();
    }

    public Object getRegistrantIdentifier() {
        return this.mRegistrantidentifier;
    }

    protected void registerForPhoneStates(Phone phone) {
        if (((CallManagerHandler) this.mHandlerMap.get(phone)) != null) {
            Rlog.d(LOG_TAG, "This phone has already been registered.");
            return;
        }
        CallManagerHandler handler = new CallManagerHandler();
        this.mHandlerMap.put(phone, handler);
        phone.registerForPreciseCallStateChanged(handler, EVENT_PRECISE_CALL_STATE_CHANGED, this.mRegistrantidentifier);
        phone.registerForDisconnect(handler, EVENT_DISCONNECT, this.mRegistrantidentifier);
        phone.registerForNewRingingConnection(handler, EVENT_NEW_RINGING_CONNECTION, this.mRegistrantidentifier);
        phone.registerForUnknownConnection(handler, EVENT_UNKNOWN_CONNECTION, this.mRegistrantidentifier);
        phone.registerForIncomingRing(handler, EVENT_INCOMING_RING, this.mRegistrantidentifier);
        phone.registerForRingbackTone(handler, EVENT_RINGBACK_TONE, this.mRegistrantidentifier);
        phone.registerForInCallVoicePrivacyOn(handler, EVENT_IN_CALL_VOICE_PRIVACY_ON, this.mRegistrantidentifier);
        phone.registerForInCallVoicePrivacyOff(handler, EVENT_IN_CALL_VOICE_PRIVACY_OFF, this.mRegistrantidentifier);
        phone.registerForDisplayInfo(handler, EVENT_DISPLAY_INFO, this.mRegistrantidentifier);
        phone.registerForSignalInfo(handler, EVENT_SIGNAL_INFO, this.mRegistrantidentifier);
        phone.registerForResendIncallMute(handler, EVENT_RESEND_INCALL_MUTE, this.mRegistrantidentifier);
        phone.registerForMmiInitiate(handler, EVENT_MMI_INITIATE, this.mRegistrantidentifier);
        phone.registerForMmiComplete(handler, EVENT_MMI_COMPLETE, this.mRegistrantidentifier);
        phone.registerForSuppServiceFailed(handler, EVENT_SUPP_SERVICE_FAILED, this.mRegistrantidentifier);
        phone.registerForServiceStateChanged(handler, EVENT_SERVICE_STATE_CHANGED, this.mRegistrantidentifier);
        phone.setOnPostDialCharacter(handler, EVENT_POST_DIAL_CHARACTER, null);
        phone.registerForCdmaOtaStatusChange(handler, EVENT_CDMA_OTA_STATUS_CHANGE, null);
        phone.registerForSubscriptionInfoReady(handler, EVENT_SUBSCRIPTION_INFO_READY, null);
        phone.registerForCallWaiting(handler, EVENT_CALL_WAITING, null);
        phone.registerForEcmTimerReset(handler, EVENT_ECM_TIMER_RESET, null);
        phone.registerForLineControlInfo(handler, EVENT_LINE_CONTROL_INFO, null);
        phone.registerForCdmaWaitingNumberChanged(handler, EVENT_CDMA_WAITING_NUMBER_CHANGED, null);
        phone.registerForOnHoldTone(handler, EVENT_ONHOLD_TONE, null);
        phone.registerForSuppServiceFailed(handler, EVENT_SUPP_SERVICE_FAILED, null);
        phone.registerForTtyModeReceived(handler, EVENT_TTY_MODE_RECEIVED, null);
        super.registerForPhoneStates(phone);
    }

    protected void unregisterForPhoneStates(Phone phone) {
        CallManagerHandler handler = (CallManagerHandler) this.mHandlerMap.get(phone);
        if (handler == null) {
            Rlog.e(LOG_TAG, "Could not find Phone handler for unregistration");
            return;
        }
        this.mHandlerMap.remove(phone);
        phone.unregisterForPreciseCallStateChanged(handler);
        phone.unregisterForDisconnect(handler);
        phone.unregisterForNewRingingConnection(handler);
        phone.unregisterForUnknownConnection(handler);
        phone.unregisterForIncomingRing(handler);
        phone.unregisterForRingbackTone(handler);
        phone.unregisterForInCallVoicePrivacyOn(handler);
        phone.unregisterForInCallVoicePrivacyOff(handler);
        phone.unregisterForDisplayInfo(handler);
        phone.unregisterForSignalInfo(handler);
        phone.unregisterForResendIncallMute(handler);
        phone.unregisterForMmiInitiate(handler);
        phone.unregisterForMmiComplete(handler);
        phone.unregisterForSuppServiceFailed(handler);
        phone.unregisterForServiceStateChanged(handler);
        phone.unregisterForTtyModeReceived(handler);
        phone.setOnPostDialCharacter(null, EVENT_POST_DIAL_CHARACTER, null);
        phone.unregisterForCdmaOtaStatusChange(handler);
        phone.unregisterForSubscriptionInfoReady(handler);
        phone.unregisterForCallWaiting(handler);
        phone.unregisterForEcmTimerReset(handler);
        phone.unregisterForLineControlInfo(handler);
        phone.unregisterForCdmaWaitingNumberChanged(handler);
        phone.unregisterForOnHoldTone(handler);
        phone.unregisterForSuppServiceFailed(handler);
        super.unregisterForPhoneStates(phone);
    }

    public void acceptCall(Call ringingCall) throws CallStateException {
        Phone ringingPhone = ringingCall.getPhone();
        if (hasActiveFgCall()) {
            boolean sameChannel;
            Phone activePhone = getActiveFgCall().getPhone();
            boolean hasBgCall = activePhone.getBackgroundCall().isIdle() ? false : DBG;
            if (activePhone == ringingPhone) {
                sameChannel = DBG;
            } else {
                sameChannel = false;
            }
            if (sameChannel && hasBgCall) {
                getActiveFgCall().hangup();
            } else if (!sameChannel && !hasBgCall) {
                activePhone.switchHoldingAndActive();
            } else if (!sameChannel && hasBgCall) {
                getActiveFgCall().hangup();
            }
        }
        ringingPhone.acceptCall(0);
    }

    public void rejectCall(Call ringingCall) throws CallStateException {
        ringingCall.getPhone().rejectCall();
    }

    public void switchHoldingAndActive(Call heldCall) throws CallStateException {
        Phone activePhone = null;
        Phone heldPhone = null;
        if (hasActiveFgCall()) {
            activePhone = getActiveFgCall().getPhone();
        }
        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }
        if (activePhone != null) {
            activePhone.switchHoldingAndActive();
        }
        if (heldPhone != null && heldPhone != activePhone) {
            heldPhone.switchHoldingAndActive();
        }
    }

    public void hangupForegroundResumeBackground(Call heldCall) throws CallStateException {
        if (hasActiveFgCall()) {
            Phone foregroundPhone = getFgPhone();
            if (heldCall == null) {
                return;
            }
            if (foregroundPhone == heldCall.getPhone()) {
                getActiveFgCall().hangup();
                return;
            }
            getActiveFgCall().hangup();
            switchHoldingAndActive(heldCall);
        }
    }

    public boolean canConference(Call heldCall) {
        Phone activePhone = null;
        Phone heldPhone = null;
        if (hasActiveFgCall()) {
            activePhone = getActiveFgCall().getPhone();
        }
        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }
        if (heldPhone == null || activePhone == null) {
            return false;
        }
        return heldPhone.getClass().equals(activePhone.getClass());
    }

    public boolean canConference(Call heldCall, int subId) {
        Phone activePhone = null;
        Phone heldPhone = null;
        if (hasActiveFgCall(subId)) {
            activePhone = getActiveFgCall(subId).getPhone();
        }
        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }
        if (heldPhone == null || activePhone == null) {
            return false;
        }
        return heldPhone.getClass().equals(activePhone.getClass());
    }

    public void conference(Call heldCall) throws CallStateException {
        Phone fgPhone = getFgPhone(heldCall.getPhone().getSubId());
        if (fgPhone == null) {
            Rlog.d(LOG_TAG, "conference: fgPhone=null");
        } else if (fgPhone instanceof SipPhone) {
            ((SipPhone) fgPhone).conference(heldCall);
        } else if (canConference(heldCall)) {
            fgPhone.conference();
        } else {
            throw new CallStateException("Can't conference foreground and selected background call");
        }
    }

    public Connection dial(Phone phone, String dialString, int videoState) throws CallStateException {
        int subId = phone.getSubId();
        if (canDial(phone)) {
            if (hasActiveFgCall(subId)) {
                Phone activePhone = getActiveFgCall(subId).getPhone();
                boolean hasBgCall = activePhone.getBackgroundCall().isIdle() ? false : DBG;
                Rlog.d(LOG_TAG, "hasBgCall: " + hasBgCall + " sameChannel:" + (activePhone == phone ? DBG : false));
                Phone imsPhone = phone.getImsPhone();
                if (activePhone != phone && (imsPhone == null || imsPhone != activePhone)) {
                    if (hasBgCall) {
                        Rlog.d(LOG_TAG, "Hangup");
                        getActiveFgCall(subId).hangup();
                    } else {
                        Rlog.d(LOG_TAG, "Switch");
                        activePhone.switchHoldingAndActive();
                    }
                }
            }
            return phone.dial(dialString, videoState);
        } else if (phone.handleInCallMmiCommands(PhoneNumberUtils.stripSeparators(dialString))) {
            return null;
        } else {
            throw new CallStateException("cannot dial in current state");
        }
    }

    public Connection dial(Phone phone, String dialString, UUSInfo uusInfo, int videoState) throws CallStateException {
        return phone.dial(dialString, uusInfo, videoState, null);
    }

    public void clearDisconnected() {
        for (Phone phone : this.mPhones) {
            phone.clearDisconnected();
        }
    }

    public void clearDisconnected(int subId) {
        for (Phone phone : this.mPhones) {
            if (phone.getSubId() == subId) {
                phone.clearDisconnected();
            }
        }
    }

    private boolean canDial(Phone phone) {
        boolean result;
        boolean z = DBG;
        int serviceState = phone.getServiceState().getState();
        int subId = phone.getSubId();
        boolean hasRingingCall = hasActiveRingingCall();
        Call.State fgCallState = getActiveFgCallState(subId);
        if (serviceState == 3 || hasRingingCall) {
            result = false;
        } else {
            if (!(fgCallState == Call.State.ACTIVE || fgCallState == Call.State.IDLE || fgCallState == Call.State.DISCONNECTED || fgCallState == Call.State.ALERTING)) {
                z = false;
            }
            result = z;
        }
        if (!result) {
            Rlog.d(LOG_TAG, "canDial serviceState=" + serviceState + " hasRingingCall=" + hasRingingCall + " fgCallState=" + fgCallState);
        }
        return result;
    }

    public boolean canTransfer(Call heldCall) {
        Phone activePhone = null;
        Phone heldPhone = null;
        if (hasActiveFgCall()) {
            activePhone = getActiveFgCall().getPhone();
        }
        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }
        return heldPhone == activePhone ? activePhone.canTransfer() : false;
    }

    public boolean canTransfer(Call heldCall, int subId) {
        Phone activePhone = null;
        Phone heldPhone = null;
        if (hasActiveFgCall(subId)) {
            activePhone = getActiveFgCall(subId).getPhone();
        }
        if (heldCall != null) {
            heldPhone = heldCall.getPhone();
        }
        return heldPhone == activePhone ? activePhone.canTransfer() : false;
    }

    public void explicitCallTransfer(Call heldCall) throws CallStateException {
        if (canTransfer(heldCall)) {
            heldCall.getPhone().explicitCallTransfer();
        }
    }

    public List<? extends MmiCode> getPendingMmiCodes(Phone phone) {
        Rlog.e(LOG_TAG, "getPendingMmiCodes not implemented");
        return null;
    }

    public boolean sendUssdResponse(Phone phone, String ussdMessge) {
        Rlog.e(LOG_TAG, "sendUssdResponse not implemented");
        return false;
    }

    public void setMute(boolean muted) {
        if (hasActiveFgCall()) {
            getActiveFgCall().getPhone().setMute(muted);
        }
    }

    public boolean getMute() {
        if (hasActiveFgCall()) {
            return getActiveFgCall().getPhone().getMute();
        }
        if (hasActiveBgCall()) {
            return getFirstActiveBgCall().getPhone().getMute();
        }
        return false;
    }

    public void setEchoSuppressionEnabled() {
        if (hasActiveFgCall()) {
            getActiveFgCall().getPhone().setEchoSuppressionEnabled();
        }
    }

    public boolean sendDtmf(char c) {
        if (!hasActiveFgCall()) {
            return false;
        }
        getActiveFgCall().getPhone().sendDtmf(c);
        return DBG;
    }

    public boolean startDtmf(char c) {
        if (!hasActiveFgCall()) {
            return false;
        }
        getActiveFgCall().getPhone().startDtmf(c);
        return DBG;
    }

    public void stopDtmf() {
        if (hasActiveFgCall()) {
            getFgPhone().stopDtmf();
        }
    }

    public boolean sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        if (!hasActiveFgCall()) {
            return false;
        }
        getActiveFgCall().getPhone().sendBurstDtmf(dtmfString, on, off, onComplete);
        return DBG;
    }

    public void registerForDisconnect(Handler h, int what, Object obj) {
        this.mDisconnectRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForDisconnect(Handler h) {
        this.mDisconnectRegistrants.remove(h);
    }

    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        this.mPreciseCallStateRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForPreciseCallStateChanged(Handler h) {
        this.mPreciseCallStateRegistrants.remove(h);
    }

    public void registerForUnknownConnection(Handler h, int what, Object obj) {
        this.mUnknownConnectionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForUnknownConnection(Handler h) {
        this.mUnknownConnectionRegistrants.remove(h);
    }

    public void registerForNewRingingConnection(Handler h, int what, Object obj) {
        this.mNewRingingConnectionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForNewRingingConnection(Handler h) {
        this.mNewRingingConnectionRegistrants.remove(h);
    }

    public void registerForIncomingRing(Handler h, int what, Object obj) {
        this.mIncomingRingRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForIncomingRing(Handler h) {
        this.mIncomingRingRegistrants.remove(h);
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mRingbackToneRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForRingbackTone(Handler h) {
        this.mRingbackToneRegistrants.remove(h);
    }

    public void registerForOnHoldTone(Handler h, int what, Object obj) {
        this.mOnHoldToneRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForOnHoldTone(Handler h) {
        this.mOnHoldToneRegistrants.remove(h);
    }

    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        this.mResendIncallMuteRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForResendIncallMute(Handler h) {
        this.mResendIncallMuteRegistrants.remove(h);
    }

    public void registerForMmiInitiate(Handler h, int what, Object obj) {
        this.mMmiInitiateRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForMmiInitiate(Handler h) {
        this.mMmiInitiateRegistrants.remove(h);
    }

    public void registerForMmiComplete(Handler h, int what, Object obj) {
        this.mMmiCompleteRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForMmiComplete(Handler h) {
        this.mMmiCompleteRegistrants.remove(h);
    }

    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        this.mEcmTimerResetRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForEcmTimerReset(Handler h) {
        this.mEcmTimerResetRegistrants.remove(h);
    }

    public void registerForServiceStateChanged(Handler h, int what, Object obj) {
        this.mServiceStateChangedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForServiceStateChanged(Handler h) {
        this.mServiceStateChangedRegistrants.remove(h);
    }

    public void registerForSuppServiceFailed(Handler h, int what, Object obj) {
        this.mSuppServiceFailedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSuppServiceFailed(Handler h) {
        this.mSuppServiceFailedRegistrants.remove(h);
    }

    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
        this.mInCallVoicePrivacyOnRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForInCallVoicePrivacyOn(Handler h) {
        this.mInCallVoicePrivacyOnRegistrants.remove(h);
    }

    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
        this.mInCallVoicePrivacyOffRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForInCallVoicePrivacyOff(Handler h) {
        this.mInCallVoicePrivacyOffRegistrants.remove(h);
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCallWaitingRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCallWaitingRegistrants.remove(h);
    }

    public void registerForSignalInfo(Handler h, int what, Object obj) {
        this.mSignalInfoRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSignalInfo(Handler h) {
        this.mSignalInfoRegistrants.remove(h);
    }

    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        this.mDisplayInfoRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForDisplayInfo(Handler h) {
        this.mDisplayInfoRegistrants.remove(h);
    }

    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        this.mCdmaOtaStatusChangeRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForCdmaOtaStatusChange(Handler h) {
        this.mCdmaOtaStatusChangeRegistrants.remove(h);
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        this.mSubscriptionInfoReadyRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mSubscriptionInfoReadyRegistrants.remove(h);
    }

    public void registerForPostDialCharacter(Handler h, int what, Object obj) {
        this.mPostDialCharacterRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForPostDialCharacter(Handler h) {
        this.mPostDialCharacterRegistrants.remove(h);
    }

    public void registerForCdmaWaitingNumberChanged(Handler h, int what, Object obj) {
        this.mCdmaWaitingNumberChangedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForCdmaWaitingNumberChanged(Handler h) {
        this.mCdmaWaitingNumberChangedRegistrants.remove(h);
    }

    public void registerForTtyModeReceived(Handler h, int what, Object obj) {
        this.mTtyModeReceivedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForTtyModeReceived(Handler h) {
        this.mTtyModeReceivedRegistrants.remove(h);
    }

    public List<Call> getRingingCalls() {
        return Collections.unmodifiableList(this.mRingingCalls);
    }

    public List<Call> getForegroundCalls() {
        return Collections.unmodifiableList(this.mForegroundCalls);
    }

    public List<Call> getBackgroundCalls() {
        return Collections.unmodifiableList(this.mBackgroundCalls);
    }

    public boolean hasActiveFgCall() {
        return getFirstActiveCall(this.mForegroundCalls) != null ? DBG : false;
    }

    public boolean hasActiveFgCall(int subId) {
        return getFirstActiveCall(this.mForegroundCalls, subId) != null ? DBG : false;
    }

    public boolean hasActiveBgCall() {
        return getFirstActiveCall(this.mBackgroundCalls) != null ? DBG : false;
    }

    public boolean hasActiveBgCall(int subId) {
        return getFirstActiveCall(this.mBackgroundCalls, subId) != null ? DBG : false;
    }

    public boolean hasActiveRingingCall() {
        return getFirstActiveCall(this.mRingingCalls) != null ? DBG : false;
    }

    public boolean hasActiveRingingCall(int subId) {
        return getFirstActiveCall(this.mRingingCalls, subId) != null ? DBG : false;
    }

    public Call getActiveFgCall() {
        Call call = getFirstNonIdleCall(this.mForegroundCalls);
        if (call != null) {
            return call;
        }
        if (this.mDefaultPhone == null) {
            return null;
        }
        return this.mDefaultPhone.getForegroundCall();
    }

    public Call getActiveFgCall(int subId) {
        Call call = getFirstNonIdleCall(this.mForegroundCalls, subId);
        if (call != null) {
            return call;
        }
        Phone phone = getPhone(subId);
        if (phone == null) {
            return null;
        }
        return phone.getForegroundCall();
    }

    private Call getFirstNonIdleCall(List<Call> calls) {
        Call result = null;
        for (Call call : calls) {
            if (!call.isIdle()) {
                return call;
            }
            if (call.getState() != Call.State.IDLE && result == null) {
                result = call;
            }
        }
        return result;
    }

    private Call getFirstNonIdleCall(List<Call> calls, int subId) {
        Call result = null;
        for (Call call : calls) {
            if (call.getPhone().getSubId() == subId || (call.getPhone() instanceof SipPhone)) {
                if (!call.isIdle()) {
                    return call;
                }
                if (call.getState() != Call.State.IDLE && result == null) {
                    result = call;
                }
            }
        }
        return result;
    }

    public Call getFirstActiveBgCall() {
        Call call = getFirstNonIdleCall(this.mBackgroundCalls);
        if (call != null) {
            return call;
        }
        if (this.mDefaultPhone == null) {
            return null;
        }
        return this.mDefaultPhone.getBackgroundCall();
    }

    public Call getFirstActiveBgCall(int subId) {
        Phone phone = getPhone(subId);
        if (hasMoreThanOneHoldingCall(subId)) {
            return phone.getBackgroundCall();
        }
        Call call = getFirstNonIdleCall(this.mBackgroundCalls, subId);
        if (call == null) {
            if (phone == null) {
                call = null;
            } else {
                call = phone.getBackgroundCall();
            }
        }
        return call;
    }

    public Call getFirstActiveRingingCall() {
        Call call = getFirstNonIdleCall(this.mRingingCalls);
        if (call != null) {
            return call;
        }
        if (this.mDefaultPhone == null) {
            return null;
        }
        return this.mDefaultPhone.getRingingCall();
    }

    public Call getFirstActiveRingingCall(int subId) {
        Phone phone = getPhone(subId);
        Call call = getFirstNonIdleCall(this.mRingingCalls, subId);
        if (call != null) {
            return call;
        }
        if (phone == null) {
            return null;
        }
        return phone.getRingingCall();
    }

    public Call.State getActiveFgCallState() {
        Call fgCall = getActiveFgCall();
        if (fgCall != null) {
            return fgCall.getState();
        }
        return Call.State.IDLE;
    }

    public Call.State getActiveFgCallState(int subId) {
        Call fgCall = getActiveFgCall(subId);
        if (fgCall != null) {
            return fgCall.getState();
        }
        return Call.State.IDLE;
    }

    public List<Connection> getFgCallConnections() {
        Call fgCall = getActiveFgCall();
        if (fgCall != null) {
            return fgCall.getConnections();
        }
        return this.mEmptyConnections;
    }

    public List<Connection> getFgCallConnections(int subId) {
        Call fgCall = getActiveFgCall(subId);
        if (fgCall != null) {
            return fgCall.getConnections();
        }
        return this.mEmptyConnections;
    }

    public List<Connection> getBgCallConnections() {
        Call bgCall = getFirstActiveBgCall();
        if (bgCall != null) {
            return bgCall.getConnections();
        }
        return this.mEmptyConnections;
    }

    public List<Connection> getBgCallConnections(int subId) {
        Call bgCall = getFirstActiveBgCall(subId);
        if (bgCall != null) {
            return bgCall.getConnections();
        }
        return this.mEmptyConnections;
    }

    public Connection getFgCallLatestConnection() {
        Call fgCall = getActiveFgCall();
        if (fgCall != null) {
            return fgCall.getLatestConnection();
        }
        return null;
    }

    public Connection getFgCallLatestConnection(int subId) {
        Call fgCall = getActiveFgCall(subId);
        if (fgCall != null) {
            return fgCall.getLatestConnection();
        }
        return null;
    }

    public boolean hasDisconnectedFgCall() {
        return getFirstCallOfState(this.mForegroundCalls, Call.State.DISCONNECTED) != null ? DBG : false;
    }

    public boolean hasDisconnectedFgCall(int subId) {
        return getFirstCallOfState(this.mForegroundCalls, Call.State.DISCONNECTED, subId) != null ? DBG : false;
    }

    public boolean hasDisconnectedBgCall() {
        return getFirstCallOfState(this.mBackgroundCalls, Call.State.DISCONNECTED) != null ? DBG : false;
    }

    public boolean hasDisconnectedBgCall(int subId) {
        return getFirstCallOfState(this.mBackgroundCalls, Call.State.DISCONNECTED, subId) != null ? DBG : false;
    }

    private Call getFirstActiveCall(ArrayList<Call> calls) {
        for (Call call : calls) {
            if (!call.isIdle()) {
                return call;
            }
        }
        return null;
    }

    private Call getFirstActiveCall(ArrayList<Call> calls, int subId) {
        for (Call call : calls) {
            if (!call.isIdle() && (call.getPhone().getSubId() == subId || (call.getPhone() instanceof SipPhone))) {
                return call;
            }
        }
        return null;
    }

    private Call getFirstCallOfState(ArrayList<Call> calls, Call.State state) {
        for (Call call : calls) {
            if (call.getState() == state) {
                return call;
            }
        }
        return null;
    }

    private Call getFirstCallOfState(ArrayList<Call> calls, Call.State state, int subId) {
        for (Call call : calls) {
            if (call.getState() == state || call.getPhone().getSubId() == subId) {
                return call;
            }
            if (call.getPhone() instanceof SipPhone) {
                return call;
            }
        }
        return null;
    }

    private boolean hasMoreThanOneRingingCall() {
        int count = 0;
        for (Call call : this.mRingingCalls) {
            if (call.getState().isRinging()) {
                count++;
                if (count > 1) {
                    return DBG;
                }
            }
        }
        return false;
    }

    private boolean hasMoreThanOneHoldingCall(int subId) {
        int count = 0;
        for (Call call : this.mBackgroundCalls) {
            if (call.getState() == Call.State.HOLDING && (call.getPhone().getSubId() == subId || (call.getPhone() instanceof SipPhone))) {
                count++;
                if (count > 1) {
                    return DBG;
                }
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            b.append("CallManager {");
            b.append("\nstate = ").append(getState(i));
            Call call = getActiveFgCall(i);
            if (call != null) {
                b.append("\n- Foreground: ").append(getActiveFgCallState(i));
                b.append(" from ").append(call.getPhone());
                b.append("\n  Conn: ").append(getFgCallConnections(i));
            }
            call = getFirstActiveBgCall(i);
            if (call != null) {
                b.append("\n- Background: ").append(call.getState());
                b.append(" from ").append(call.getPhone());
                b.append("\n  Conn: ").append(getBgCallConnections(i));
            }
            call = getFirstActiveRingingCall(i);
            if (call != null) {
                b.append("\n- Ringing: ").append(call.getState());
                b.append(" from ").append(call.getPhone());
            }
        }
        for (Phone phone : getAllPhones()) {
            if (phone != null) {
                b.append("\nPhone: ").append(phone).append(", name = ").append(phone.getPhoneName()).append(", state = ").append(phone.getState());
                call = phone.getForegroundCall();
                if (call != null) {
                    b.append("\n- Foreground: ").append(call);
                }
                call = phone.getBackgroundCall();
                if (call != null) {
                    b.append(" Background: ").append(call);
                }
                call = phone.getRingingCall();
                if (call != null) {
                    b.append(" Ringing: ").append(call);
                }
            }
        }
        b.append("\n}");
        return b.toString();
    }
}

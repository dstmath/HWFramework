package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.sip.SipPhone;
import com.huawei.internal.telephony.PhoneConstantsExt;
import com.huawei.internal.telephony.PhoneExt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CallManager implements ICallManagerInner {
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
    private static final CallManager INSTANCE = new CallManager();
    private static final String LOG_TAG = "CallManager";
    private static final boolean VDBG = false;
    @UnsupportedAppUsage
    private final ArrayList<Call> mBackgroundCalls = new ArrayList<>();
    protected final RegistrantList mCallWaitingRegistrants = new RegistrantList();
    protected final RegistrantList mCdmaOtaStatusChangeRegistrants = new RegistrantList();
    protected final RegistrantList mCdmaWaitingNumberChangedRegistrants = new RegistrantList();
    private Phone mDefaultPhone = null;
    protected final RegistrantList mDisconnectRegistrants = new RegistrantList();
    protected final RegistrantList mDisplayInfoRegistrants = new RegistrantList();
    protected final RegistrantList mEcmTimerResetRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    private final ArrayList<Connection> mEmptyConnections = new ArrayList<>();
    @UnsupportedAppUsage
    private final ArrayList<Call> mForegroundCalls = new ArrayList<>();
    private final HashMap<Phone, CallManagerHandler> mHandlerMap = new HashMap<>();
    private IHwCallManagerEx mHwCallManagerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwCallManagerEx(this);
    protected final RegistrantList mInCallVoicePrivacyOffRegistrants = new RegistrantList();
    protected final RegistrantList mInCallVoicePrivacyOnRegistrants = new RegistrantList();
    protected final RegistrantList mIncomingRingRegistrants = new RegistrantList();
    protected final RegistrantList mLineControlInfoRegistrants = new RegistrantList();
    protected final RegistrantList mMmiCompleteRegistrants = new RegistrantList();
    protected final RegistrantList mMmiInitiateRegistrants = new RegistrantList();
    protected final RegistrantList mMmiRegistrants = new RegistrantList();
    protected final RegistrantList mNewRingingConnectionRegistrants = new RegistrantList();
    protected final RegistrantList mOnHoldToneRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    private final ArrayList<Phone> mPhones = new ArrayList<>();
    protected final RegistrantList mPostDialCharacterRegistrants = new RegistrantList();
    protected final RegistrantList mPreciseCallStateRegistrants = new RegistrantList();
    private Object mRegistrantidentifier = new Object();
    protected final RegistrantList mResendIncallMuteRegistrants = new RegistrantList();
    protected final RegistrantList mRingbackToneRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    private final ArrayList<Call> mRingingCalls = new ArrayList<>();
    protected final RegistrantList mServiceStateChangedRegistrants = new RegistrantList();
    protected final RegistrantList mSignalInfoRegistrants = new RegistrantList();
    private boolean mSpeedUpAudioForMtCall = false;
    protected final RegistrantList mSubscriptionInfoReadyRegistrants = new RegistrantList();
    protected final RegistrantList mSuppServiceFailedRegistrants = new RegistrantList();
    protected final RegistrantList mTtyModeReceivedRegistrants = new RegistrantList();
    protected final RegistrantList mUnknownConnectionRegistrants = new RegistrantList();

    private CallManager() {
    }

    @UnsupportedAppUsage
    public static CallManager getInstance() {
        return INSTANCE;
    }

    public List<Phone> getAllPhones() {
        return Collections.unmodifiableList(this.mPhones);
    }

    private Phone getPhone(int subId) {
        Iterator<Phone> it = this.mPhones.iterator();
        while (it.hasNext()) {
            Phone phone = it.next();
            if (phone.getSubId() == subId && phone.getPhoneType() != 5) {
                return phone;
            }
        }
        return null;
    }

    @UnsupportedAppUsage
    public PhoneConstants.State getState() {
        PhoneConstants.State s = PhoneConstants.State.IDLE;
        Iterator<Phone> it = this.mPhones.iterator();
        while (it.hasNext()) {
            Phone phone = it.next();
            if (phone.getState() == PhoneConstants.State.RINGING) {
                s = PhoneConstants.State.RINGING;
            } else if (phone.getState() == PhoneConstants.State.OFFHOOK && s == PhoneConstants.State.IDLE) {
                s = PhoneConstants.State.OFFHOOK;
            }
        }
        return s;
    }

    @UnsupportedAppUsage
    public PhoneConstants.State getState(int subId) {
        PhoneConstants.State s = PhoneConstants.State.IDLE;
        Iterator<Phone> it = this.mPhones.iterator();
        while (it.hasNext()) {
            Phone phone = it.next();
            if (phone.getSubId() == subId) {
                if (phone.getState() == PhoneConstants.State.RINGING) {
                    s = PhoneConstants.State.RINGING;
                } else if (phone.getState() == PhoneConstants.State.OFFHOOK && s == PhoneConstants.State.IDLE) {
                    s = PhoneConstants.State.OFFHOOK;
                }
            }
        }
        return s;
    }

    public int getServiceState() {
        int resultState = 1;
        Iterator<Phone> it = this.mPhones.iterator();
        while (it.hasNext()) {
            int serviceState = it.next().getServiceState().getState();
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
        Iterator<Phone> it = this.mPhones.iterator();
        while (it.hasNext()) {
            Phone phone = it.next();
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

    @UnsupportedAppUsage
    public Phone getPhoneInCall() {
        if (getFirstActiveRingingCall() != null && !getFirstActiveRingingCall().isIdle()) {
            return getFirstActiveRingingCall().getPhone();
        }
        if (getActiveFgCall() != null && !getActiveFgCall().isIdle()) {
            return getActiveFgCall().getPhone();
        }
        if (getFirstActiveBgCall() != null) {
            return getFirstActiveBgCall().getPhone();
        }
        return null;
    }

    public Phone getPhoneInCall(int subId) {
        if (getFirstActiveRingingCall(subId) != null && !getFirstActiveRingingCall(subId).isIdle()) {
            return getFirstActiveRingingCall(subId).getPhone();
        }
        if (getActiveFgCall(subId) != null && !getActiveFgCall(subId).isIdle()) {
            return getActiveFgCall(subId).getPhone();
        }
        return getFirstActiveBgCall(subId) != null ? getFirstActiveBgCall(subId).getPhone() : null;
    }

    @UnsupportedAppUsage
    public boolean registerPhone(Phone phone) {
        if (phone == null || this.mPhones.contains(phone)) {
            return false;
        }
        Rlog.i(LOG_TAG, "registerPhone(" + phone.getPhoneName() + " " + phone + ")");
        if (this.mPhones.isEmpty()) {
            this.mDefaultPhone = phone;
        }
        this.mPhones.add(phone);
        this.mRingingCalls.add(phone.getRingingCall());
        this.mBackgroundCalls.add(phone.getBackgroundCall());
        this.mForegroundCalls.add(phone.getForegroundCall());
        registerForPhoneStates(phone);
        return true;
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mLineControlInfoRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mLineControlInfoRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void unregisterPhone(Phone phone) {
        if (phone != null && this.mPhones.contains(phone)) {
            Rlog.i(LOG_TAG, "unregisterPhone(" + phone.getPhoneName() + " " + phone + ")");
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
                this.mDefaultPhone = this.mPhones.get(0);
            }
        }
    }

    @UnsupportedAppUsage
    public Phone getDefaultPhone() {
        return this.mDefaultPhone;
    }

    @UnsupportedAppUsage
    public Phone getFgPhone() {
        return getActiveFgCall().getPhone();
    }

    @UnsupportedAppUsage
    public Phone getFgPhone(int subId) {
        Call call = getActiveFgCall(subId);
        if (call != null) {
            return call.getPhone();
        }
        return null;
    }

    @UnsupportedAppUsage
    public Phone getBgPhone() {
        if (getFirstActiveBgCall() == null) {
            return null;
        }
        return getFirstActiveBgCall().getPhone();
    }

    public Phone getBgPhone(int subId) {
        if (getFirstActiveBgCall(subId) == null) {
            return null;
        }
        return getFirstActiveBgCall(subId).getPhone();
    }

    @UnsupportedAppUsage
    public Phone getRingingPhone() {
        if (getFirstActiveRingingCall() == null) {
            return null;
        }
        return getFirstActiveRingingCall().getPhone();
    }

    public Phone getRingingPhone(int subId) {
        if (getFirstActiveRingingCall(subId) == null) {
            return null;
        }
        return getFirstActiveRingingCall(subId).getPhone();
    }

    @UnsupportedAppUsage
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

    private void registerForPhoneStates(Phone phone) {
        if (this.mHandlerMap.get(phone) != null) {
            Rlog.i(LOG_TAG, "This phone has already been registered.");
            return;
        }
        CallManagerHandler handler = new CallManagerHandler();
        this.mHandlerMap.put(phone, handler);
        phone.registerForPreciseCallStateChanged(handler, 101, this.mRegistrantidentifier);
        phone.registerForDisconnect(handler, 100, this.mRegistrantidentifier);
        phone.registerForNewRingingConnection(handler, 102, this.mRegistrantidentifier);
        phone.registerForUnknownConnection(handler, EVENT_UNKNOWN_CONNECTION, this.mRegistrantidentifier);
        phone.registerForIncomingRing(handler, 104, this.mRegistrantidentifier);
        phone.registerForRingbackTone(handler, 105, this.mRegistrantidentifier);
        phone.registerForInCallVoicePrivacyOn(handler, 106, this.mRegistrantidentifier);
        phone.registerForInCallVoicePrivacyOff(handler, EVENT_IN_CALL_VOICE_PRIVACY_OFF, this.mRegistrantidentifier);
        phone.registerForDisplayInfo(handler, 109, this.mRegistrantidentifier);
        phone.registerForSignalInfo(handler, EVENT_SIGNAL_INFO, this.mRegistrantidentifier);
        phone.registerForResendIncallMute(handler, 112, this.mRegistrantidentifier);
        phone.registerForMmiInitiate(handler, 113, this.mRegistrantidentifier);
        phone.registerForMmiComplete(handler, 114, this.mRegistrantidentifier);
        phone.registerForSuppServiceFailed(handler, 117, this.mRegistrantidentifier);
        phone.registerForServiceStateChanged(handler, 118, this.mRegistrantidentifier);
        phone.setOnPostDialCharacter(handler, 119, null);
        phone.registerForCdmaOtaStatusChange(handler, 111, null);
        phone.registerForSubscriptionInfoReady(handler, 116, null);
        phone.registerForCallWaiting(handler, 108, null);
        phone.registerForEcmTimerReset(handler, 115, null);
        phone.registerForLineControlInfo(handler, 152, null);
        phone.registerForCdmaWaitingNumberChanged(handler, 153, null);
        phone.registerForOnHoldTone(handler, 120, null);
        phone.registerForSuppServiceFailed(handler, 117, null);
        phone.registerForTtyModeReceived(handler, 122, null);
        this.mHwCallManagerEx.registerForPhoneStatesHw(PhoneExt.getPhoneExt(phone));
    }

    private void unregisterForPhoneStates(Phone phone) {
        CallManagerHandler handler = this.mHandlerMap.get(phone);
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
        phone.setOnPostDialCharacter(null, 119, null);
        phone.unregisterForCdmaOtaStatusChange(handler);
        phone.unregisterForSubscriptionInfoReady(handler);
        phone.unregisterForCallWaiting(handler);
        phone.unregisterForEcmTimerReset(handler);
        phone.unregisterForLineControlInfo(handler);
        phone.unregisterForCdmaWaitingNumberChanged(handler);
        phone.unregisterForOnHoldTone(handler);
        phone.unregisterForSuppServiceFailed(handler);
        this.mHwCallManagerEx.unregisterForPhoneStatesHw(PhoneExt.getPhoneExt(phone));
    }

    public void acceptCall(Call ringingCall) throws CallStateException {
        Phone ringingPhone = ringingCall.getPhone();
        if (hasActiveFgCall()) {
            Phone activePhone = getActiveFgCall().getPhone();
            boolean sameChannel = true;
            boolean hasBgCall = !activePhone.getBackgroundCall().isIdle();
            if (activePhone != ringingPhone) {
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void conference(Call heldCall) throws CallStateException {
        Phone fgPhone = getFgPhone(heldCall.getPhone().getSubId());
        if (fgPhone == null) {
            Rlog.i(LOG_TAG, "conference: fgPhone=null");
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
                boolean z = true;
                boolean hasBgCall = !activePhone.getBackgroundCall().isIdle();
                StringBuilder sb = new StringBuilder();
                sb.append("hasBgCall: ");
                sb.append(hasBgCall);
                sb.append(" sameChannel:");
                if (activePhone != phone) {
                    z = false;
                }
                sb.append(z);
                Rlog.i(LOG_TAG, sb.toString());
                Phone imsPhone = phone.getImsPhone();
                if (activePhone != phone && (imsPhone == null || imsPhone != activePhone)) {
                    if (hasBgCall) {
                        Rlog.i(LOG_TAG, "Hangup");
                        getActiveFgCall(subId).hangup();
                    } else {
                        Rlog.i(LOG_TAG, "Switch");
                        activePhone.switchHoldingAndActive();
                    }
                }
            }
            return phone.dial(dialString, new PhoneInternalInterface.DialArgs.Builder().setVideoState(videoState).build());
        } else if (phone.handleInCallMmiCommands(PhoneNumberUtils.stripSeparators(dialString))) {
            return null;
        } else {
            throw new CallStateException("cannot dial in current state");
        }
    }

    public Connection dial(Phone phone, String dialString, UUSInfo uusInfo, int videoState) throws CallStateException {
        return phone.dial(dialString, new PhoneInternalInterface.DialArgs.Builder().setUusInfo(uusInfo).setVideoState(videoState).build());
    }

    public void clearDisconnected() {
        Iterator<Phone> it = this.mPhones.iterator();
        while (it.hasNext()) {
            it.next().clearDisconnected();
        }
    }

    public void clearDisconnected(int subId) {
        Iterator<Phone> it = this.mPhones.iterator();
        while (it.hasNext()) {
            Phone phone = it.next();
            if (phone.getSubId() == subId) {
                phone.clearDisconnected();
            }
        }
    }

    @UnsupportedAppUsage
    private boolean canDial(Phone phone) {
        int serviceState = phone.getServiceState().getState();
        int subId = phone.getSubId();
        boolean hasRingingCall = hasActiveRingingCall();
        Call.State fgCallState = getActiveFgCallState(subId);
        boolean result = serviceState != 3 && !hasRingingCall && (fgCallState == Call.State.ACTIVE || fgCallState == Call.State.IDLE || fgCallState == Call.State.DISCONNECTED || fgCallState == Call.State.ALERTING);
        if (!result) {
            Rlog.i(LOG_TAG, "canDial serviceState=" + serviceState + " hasRingingCall=" + hasRingingCall + " fgCallState=" + fgCallState);
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
        return heldPhone == activePhone && activePhone.canTransfer();
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
        return heldPhone == activePhone && activePhone.canTransfer();
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
        if (getActiveFgCall() == null) {
            return false;
        }
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
        return true;
    }

    public boolean startDtmf(char c) {
        if (!hasActiveFgCall()) {
            return false;
        }
        getActiveFgCall().getPhone().startDtmf(c);
        return true;
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
        return true;
    }

    @UnsupportedAppUsage
    public void registerForDisconnect(Handler h, int what, Object obj) {
        this.mDisconnectRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForDisconnect(Handler h) {
        this.mDisconnectRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        this.mPreciseCallStateRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForPreciseCallStateChanged(Handler h) {
        this.mPreciseCallStateRegistrants.remove(h);
    }

    public void registerForUnknownConnection(Handler h, int what, Object obj) {
        this.mUnknownConnectionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForUnknownConnection(Handler h) {
        this.mUnknownConnectionRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForNewRingingConnection(Handler h, int what, Object obj) {
        this.mNewRingingConnectionRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
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
        Rlog.i(LOG_TAG, "registerForMmiComplete");
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

    @UnsupportedAppUsage
    public List<Call> getRingingCalls() {
        return Collections.unmodifiableList(this.mRingingCalls);
    }

    public List<Call> getForegroundCalls() {
        return Collections.unmodifiableList(this.mForegroundCalls);
    }

    @UnsupportedAppUsage
    public List<Call> getBackgroundCalls() {
        return Collections.unmodifiableList(this.mBackgroundCalls);
    }

    @UnsupportedAppUsage
    public boolean hasActiveFgCall() {
        return getFirstActiveCall(this.mForegroundCalls) != null;
    }

    @UnsupportedAppUsage
    public boolean hasActiveFgCall(int subId) {
        return getFirstActiveCall(this.mForegroundCalls, subId) != null;
    }

    @UnsupportedAppUsage
    public boolean hasActiveBgCall() {
        return getFirstActiveCall(this.mBackgroundCalls) != null;
    }

    @UnsupportedAppUsage
    public boolean hasActiveBgCall(int subId) {
        return getFirstActiveCall(this.mBackgroundCalls, subId) != null;
    }

    public boolean hasActiveRingingCall() {
        return getFirstActiveCall(this.mRingingCalls) != null;
    }

    @UnsupportedAppUsage
    public boolean hasActiveRingingCall(int subId) {
        return getFirstActiveCall(this.mRingingCalls, subId) != null;
    }

    public Call getActiveFgCall() {
        Call call;
        Call call2 = getFirstNonIdleCall(this.mForegroundCalls);
        if (call2 != null) {
            return call2;
        }
        Phone phone = this.mDefaultPhone;
        if (phone == null) {
            call = null;
        } else {
            call = phone.getForegroundCall();
        }
        return call;
    }

    @UnsupportedAppUsage
    public Call getActiveFgCall(int subId) {
        Call call;
        Call call2 = getFirstNonIdleCall(this.mForegroundCalls, subId);
        if (call2 != null) {
            return call2;
        }
        Phone phone = getPhone(subId);
        if (phone == null) {
            call = null;
        } else {
            call = phone.getForegroundCall();
        }
        return call;
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

    @UnsupportedAppUsage
    public Call getFirstActiveBgCall() {
        Call call;
        Call call2 = getFirstNonIdleCall(this.mBackgroundCalls);
        if (call2 != null) {
            return call2;
        }
        Phone phone = this.mDefaultPhone;
        if (phone == null) {
            call = null;
        } else {
            call = phone.getBackgroundCall();
        }
        return call;
    }

    @UnsupportedAppUsage
    public Call getFirstActiveBgCall(int subId) {
        Call call;
        Phone phone = getPhone(subId);
        if (phone != null && hasMoreThanOneHoldingCall(subId)) {
            return phone.getBackgroundCall();
        }
        Call call2 = getFirstNonIdleCall(this.mBackgroundCalls, subId);
        if (call2 != null) {
            return call2;
        }
        if (phone == null) {
            call = null;
        } else {
            call = phone.getBackgroundCall();
        }
        return call;
    }

    @UnsupportedAppUsage
    public Call getFirstActiveRingingCall() {
        Call call;
        Call call2 = getFirstNonIdleCall(this.mRingingCalls);
        if (call2 != null) {
            return call2;
        }
        Phone phone = this.mDefaultPhone;
        if (phone == null) {
            call = null;
        } else {
            call = phone.getRingingCall();
        }
        return call;
    }

    @UnsupportedAppUsage
    public Call getFirstActiveRingingCall(int subId) {
        Call call;
        Phone phone = getPhone(subId);
        Call call2 = getFirstNonIdleCall(this.mRingingCalls, subId);
        if (call2 != null) {
            return call2;
        }
        if (phone == null) {
            call = null;
        } else {
            call = phone.getRingingCall();
        }
        return call;
    }

    public Call.State getActiveFgCallState() {
        Call fgCall = getActiveFgCall();
        if (fgCall != null) {
            return fgCall.getState();
        }
        return Call.State.IDLE;
    }

    @UnsupportedAppUsage
    public Call.State getActiveFgCallState(int subId) {
        Call fgCall = getActiveFgCall(subId);
        if (fgCall != null) {
            return fgCall.getState();
        }
        return Call.State.IDLE;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public List<Connection> getBgCallConnections() {
        Call bgCall = getFirstActiveBgCall();
        if (bgCall != null) {
            return bgCall.getConnections();
        }
        return this.mEmptyConnections;
    }

    public boolean hasDisconnectedFgCall() {
        return getFirstCallOfState(this.mForegroundCalls, Call.State.DISCONNECTED) != null;
    }

    public boolean hasDisconnectedFgCall(int subId) {
        return getFirstCallOfState(this.mForegroundCalls, Call.State.DISCONNECTED, subId) != null;
    }

    public boolean hasDisconnectedBgCall() {
        return getFirstCallOfState(this.mBackgroundCalls, Call.State.DISCONNECTED) != null;
    }

    public boolean hasDisconnectedBgCall(int subId) {
        return getFirstCallOfState(this.mBackgroundCalls, Call.State.DISCONNECTED, subId) != null;
    }

    private Call getFirstActiveCall(ArrayList<Call> calls) {
        Iterator<Call> it = calls.iterator();
        while (it.hasNext()) {
            Call call = it.next();
            if (!call.isIdle()) {
                return call;
            }
        }
        return null;
    }

    private Call getFirstActiveCall(ArrayList<Call> calls, int subId) {
        Iterator<Call> it = calls.iterator();
        while (it.hasNext()) {
            Call call = it.next();
            if (!call.isIdle() && (call.getPhone().getSubId() == subId || (call.getPhone() instanceof SipPhone))) {
                return call;
            }
        }
        return null;
    }

    private Call getFirstCallOfState(ArrayList<Call> calls, Call.State state) {
        Iterator<Call> it = calls.iterator();
        while (it.hasNext()) {
            Call call = it.next();
            if (call.getState() == state) {
                return call;
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000a  */
    private Call getFirstCallOfState(ArrayList<Call> calls, Call.State state, int subId) {
        Iterator<Call> it = calls.iterator();
        while (it.hasNext()) {
            Call call = it.next();
            if (call.getState() == state || call.getPhone().getSubId() == subId || (call.getPhone() instanceof SipPhone)) {
                return call;
            }
            while (it.hasNext()) {
            }
        }
        return null;
    }

    @UnsupportedAppUsage
    private boolean hasMoreThanOneRingingCall() {
        int count = 0;
        Iterator<Call> it = this.mRingingCalls.iterator();
        while (it.hasNext()) {
            if (it.next().getState().isRinging() && (count = count + 1) > 1) {
                return true;
            }
        }
        return false;
    }

    @UnsupportedAppUsage
    private boolean hasMoreThanOneRingingCall(int subId) {
        int count = 0;
        Iterator<Call> it = this.mRingingCalls.iterator();
        while (it.hasNext()) {
            Call call = it.next();
            if (call.getState().isRinging() && ((call.getPhone().getSubId() == subId || (call.getPhone() instanceof SipPhone)) && (count = count + 1) > 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMoreThanOneHoldingCall(int subId) {
        int count = 0;
        Iterator<Call> it = this.mBackgroundCalls.iterator();
        while (it.hasNext()) {
            Call call = it.next();
            if (call.getState() == Call.State.HOLDING && ((call.getPhone().getSubId() == subId || (call.getPhone() instanceof SipPhone)) && (count = count + 1) > 1)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class CallManagerHandler extends Handler {
        private CallManagerHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 122) {
                CallManager.this.mTtyModeReceivedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
            } else if (i == 152) {
                CallManager.this.mLineControlInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
            } else if (i != 153) {
                switch (i) {
                    case 100:
                        CallManager.this.mDisconnectRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 101:
                        CallManager.this.mPreciseCallStateRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 102:
                        CallManager.this.mNewRingingConnectionRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case CallManager.EVENT_UNKNOWN_CONNECTION /* 103 */:
                        CallManager.this.mUnknownConnectionRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 104:
                        if (!CallManager.this.hasActiveFgCall()) {
                            CallManager.this.mIncomingRingRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                            return;
                        }
                        return;
                    case 105:
                        CallManager.this.mRingbackToneRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 106:
                        CallManager.this.mInCallVoicePrivacyOnRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case CallManager.EVENT_IN_CALL_VOICE_PRIVACY_OFF /* 107 */:
                        CallManager.this.mInCallVoicePrivacyOffRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 108:
                        CallManager.this.mCallWaitingRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 109:
                        CallManager.this.mDisplayInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case CallManager.EVENT_SIGNAL_INFO /* 110 */:
                        CallManager.this.mSignalInfoRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 111:
                        CallManager.this.mCdmaOtaStatusChangeRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 112:
                        CallManager.this.mResendIncallMuteRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 113:
                        CallManager.this.mMmiInitiateRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 114:
                        Rlog.i(CallManager.LOG_TAG, "CallManager: handleMessage (EVENT_MMI_COMPLETE)");
                        CallManager.this.mMmiCompleteRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 115:
                        CallManager.this.mEcmTimerResetRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 116:
                        CallManager.this.mSubscriptionInfoReadyRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 117:
                        CallManager.this.mSuppServiceFailedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 118:
                        CallManager.this.mServiceStateChangedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    case 119:
                        for (int i2 = 0; i2 < CallManager.this.mPostDialCharacterRegistrants.size(); i2++) {
                            Message notifyMsg = ((Registrant) CallManager.this.mPostDialCharacterRegistrants.get(i2)).messageForRegistrant();
                            if (notifyMsg != null) {
                                notifyMsg.obj = msg.obj;
                                notifyMsg.arg1 = msg.arg1;
                                notifyMsg.sendToTarget();
                            }
                        }
                        return;
                    case 120:
                        CallManager.this.mOnHoldToneRegistrants.notifyRegistrants((AsyncResult) msg.obj);
                        return;
                    default:
                        return;
                }
            } else {
                CallManager.this.mCdmaWaitingNumberChangedRegistrants.notifyRegistrants((AsyncResult) msg.obj);
            }
        }
    }

    public void registerForPhoneStatesHw(Phone phone) {
        this.mHwCallManagerEx.registerForPhoneStatesHw(PhoneExt.getPhoneExt(phone));
    }

    public void unregisterForPhoneStatesHw(Phone phone) {
        this.mHwCallManagerEx.unregisterForPhoneStatesHw(PhoneExt.getPhoneExt(phone));
    }

    public void onSwitchToOtherActiveSub(Phone phone) {
        this.mHwCallManagerEx.onSwitchToOtherActiveSub(PhoneExt.getPhoneExt(phone));
    }

    public void resultForKMCRemoteCmd(Phone phone, int cmd, int reqData) {
        this.mHwCallManagerEx.resultForKMCRemoteCmd(PhoneExt.getPhoneExt(phone), cmd, reqData);
    }

    public void setConnEncryptCallByNumber(Phone phone, String number, boolean val) {
        this.mHwCallManagerEx.setConnEncryptCallByNumber(PhoneExt.getPhoneExt(phone), number, val);
    }

    public void cmdForEncryptedCall(Phone phone, int cmd, byte[] reqData) {
        this.mHwCallManagerEx.cmdForEncryptedCall(PhoneExt.getPhoneExt(phone), cmd, reqData);
    }

    public void registerForEncryptedCall(Handler h, int what, Object obj) {
        this.mHwCallManagerEx.registerForEncryptedCall(h, what, obj);
    }

    public void unregisterForEncryptedCall(Handler h) {
        this.mHwCallManagerEx.unregisterForEncryptedCall(h);
    }

    @Override // com.android.internal.telephony.ICallManagerInner
    public IHwCallManagerEx getHwCallManagerEx() {
        return this.mHwCallManagerEx;
    }

    @Override // com.android.internal.telephony.ICallManagerInner
    public PhoneExt getPhoneHw(int subId) {
        return PhoneExt.getPhoneExt(getPhone(subId));
    }

    @Override // com.android.internal.telephony.ICallManagerInner
    public PhoneConstantsExt.StateEx getStateEx() {
        return PhoneConstantsExt.StateEx.getStateExByState(getState());
    }

    @Override // com.android.internal.telephony.ICallManagerInner
    public PhoneConstantsExt.StateEx getStateEx(int subId) {
        return PhoneConstantsExt.StateEx.getStateExByState(getState(subId));
    }
}

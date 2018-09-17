package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface.RadioState;

public abstract class BaseCommands extends AbstractBaseCommands implements CommandsInterface {
    protected RegistrantList mAvailRegistrants = new RegistrantList();
    protected RegistrantList mCallStateRegistrants = new RegistrantList();
    protected RegistrantList mCallWaitingInfoRegistrants = new RegistrantList();
    protected Registrant mCatCallSetUpRegistrant;
    protected Registrant mCatCcAlphaRegistrant;
    protected Registrant mCatEventRegistrant;
    protected Registrant mCatProCmdRegistrant;
    protected Registrant mCatSessionEndRegistrant;
    protected RegistrantList mCdmaPrlChangedRegistrants = new RegistrantList();
    protected Registrant mCdmaSmsRegistrant;
    protected int mCdmaSubscription;
    protected RegistrantList mCdmaSubscriptionChangedRegistrants = new RegistrantList();
    protected Context mContext;
    protected RegistrantList mDataCallListChangedRegistrants = new RegistrantList();
    protected RegistrantList mDisplayInfoRegistrants = new RegistrantList();
    protected Registrant mEmergencyCallbackModeRegistrant;
    protected RegistrantList mExitEmergencyCallbackModeRegistrants = new RegistrantList();
    protected Registrant mGsmBroadcastSmsRegistrant;
    protected Registrant mGsmSmsRegistrant;
    protected RegistrantList mHardwareConfigChangeRegistrants = new RegistrantList();
    protected RegistrantList mIccRefreshRegistrants = new RegistrantList();
    protected Registrant mIccSmsFullRegistrant;
    protected RegistrantList mIccStatusChangedRegistrants = new RegistrantList();
    protected RegistrantList mImsNetworkStateChangedRegistrants = new RegistrantList();
    protected Registrant mLceInfoRegistrant;
    protected RegistrantList mLineControlInfoRegistrants = new RegistrantList();
    protected RegistrantList mModemCapRegistrants = new RegistrantList();
    protected Registrant mNITZTimeRegistrant;
    protected RegistrantList mNetworkStateRegistrants = new RegistrantList();
    protected RegistrantList mNotAvailRegistrants = new RegistrantList();
    protected RegistrantList mNumberInfoRegistrants = new RegistrantList();
    protected RegistrantList mOffOrNotAvailRegistrants = new RegistrantList();
    protected RegistrantList mOnRegistrants = new RegistrantList();
    protected RegistrantList mOtaProvisionRegistrants = new RegistrantList();
    protected RegistrantList mPcoDataRegistrants = new RegistrantList();
    protected RegistrantList mPhoneRadioCapabilityChangedRegistrants = new RegistrantList();
    protected int mPhoneType;
    protected int mPreferredNetworkType;
    protected RegistrantList mRSrvccStateRegistrants = new RegistrantList();
    protected RegistrantList mRadioStateChangedRegistrants = new RegistrantList();
    protected RegistrantList mRedirNumInfoRegistrants = new RegistrantList();
    protected RegistrantList mResendIncallMuteRegistrants = new RegistrantList();
    protected Registrant mRestrictedStateRegistrant;
    protected RegistrantList mRilCellInfoListRegistrants = new RegistrantList();
    protected RegistrantList mRilConnectedRegistrants = new RegistrantList();
    protected int mRilVersion = -1;
    protected Registrant mRingRegistrant;
    protected RegistrantList mRingbackToneRegistrants = new RegistrantList();
    protected RegistrantList mSignalInfoRegistrants = new RegistrantList();
    protected Registrant mSignalStrengthRegistrant;
    protected Registrant mSmsOnSimRegistrant;
    protected Registrant mSmsStatusRegistrant;
    protected RegistrantList mSrvccStateRegistrants = new RegistrantList();
    protected Registrant mSsRegistrant;
    protected Registrant mSsnRegistrant;
    protected RadioState mState = RadioState.RADIO_UNAVAILABLE;
    protected Object mStateMonitor = new Object();
    protected RegistrantList mSubscriptionStatusRegistrants = new RegistrantList();
    protected RegistrantList mT53AudCntrlInfoRegistrants = new RegistrantList();
    protected RegistrantList mT53ClirInfoRegistrants = new RegistrantList();
    protected Registrant mUSSDRegistrant;
    protected Registrant mUnsolOemHookRawRegistrant;
    protected RegistrantList mVoicePrivacyOffRegistrants = new RegistrantList();
    protected RegistrantList mVoicePrivacyOnRegistrants = new RegistrantList();
    protected RegistrantList mVoiceRadioTechChangedRegistrants = new RegistrantList();

    public BaseCommands(Context context) {
        this.mContext = context;
    }

    public RadioState getRadioState() {
        return this.mState;
    }

    public void registerForRadioStateChanged(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mRadioStateChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForRadioStateChanged(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mRadioStateChangedRegistrants.remove(h);
        }
    }

    public void registerForImsNetworkStateChanged(Handler h, int what, Object obj) {
        this.mImsNetworkStateChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsNetworkStateChanged(Handler h) {
        this.mImsNetworkStateChangedRegistrants.remove(h);
    }

    public void registerForOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.add(r);
            if (this.mState.isOn()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForOn(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.remove(h);
        }
    }

    public void registerForAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mAvailRegistrants.add(r);
            if (this.mState.isAvailable()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mAvailRegistrants.remove(h);
        }
    }

    public void registerForNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.add(r);
            if (!this.mState.isAvailable()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForNotAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.remove(h);
        }
    }

    public void registerForOffOrNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOffOrNotAvailRegistrants.add(r);
            if (this.mState == RadioState.RADIO_OFF || (this.mState.isAvailable() ^ 1) != 0) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForOffOrNotAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOffOrNotAvailRegistrants.remove(h);
        }
    }

    public void registerForCallStateChanged(Handler h, int what, Object obj) {
        this.mCallStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallStateChanged(Handler h) {
        this.mCallStateRegistrants.remove(h);
    }

    public void registerForNetworkStateChanged(Handler h, int what, Object obj) {
        this.mNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkStateChanged(Handler h) {
        this.mNetworkStateRegistrants.remove(h);
    }

    public void registerForDataCallListChanged(Handler h, int what, Object obj) {
        this.mDataCallListChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDataCallListChanged(Handler h) {
        this.mDataCallListChangedRegistrants.remove(h);
    }

    public void registerForVoiceRadioTechChanged(Handler h, int what, Object obj) {
        this.mVoiceRadioTechChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceRadioTechChanged(Handler h) {
        this.mVoiceRadioTechChangedRegistrants.remove(h);
    }

    public void registerForIccStatusChanged(Handler h, int what, Object obj) {
        this.mIccStatusChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIccStatusChanged(Handler h) {
        this.mIccStatusChangedRegistrants.remove(h);
    }

    public void setOnNewGsmSms(Handler h, int what, Object obj) {
        this.mGsmSmsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNewGsmSms(Handler h) {
        if (this.mGsmSmsRegistrant != null && this.mGsmSmsRegistrant.getHandler() == h) {
            this.mGsmSmsRegistrant.clear();
            this.mGsmSmsRegistrant = null;
        }
    }

    public void setOnNewCdmaSms(Handler h, int what, Object obj) {
        this.mCdmaSmsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNewCdmaSms(Handler h) {
        if (this.mCdmaSmsRegistrant != null && this.mCdmaSmsRegistrant.getHandler() == h) {
            this.mCdmaSmsRegistrant.clear();
            this.mCdmaSmsRegistrant = null;
        }
    }

    public void setOnNewGsmBroadcastSms(Handler h, int what, Object obj) {
        this.mGsmBroadcastSmsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNewGsmBroadcastSms(Handler h) {
        if (this.mGsmBroadcastSmsRegistrant != null && this.mGsmBroadcastSmsRegistrant.getHandler() == h) {
            this.mGsmBroadcastSmsRegistrant.clear();
            this.mGsmBroadcastSmsRegistrant = null;
        }
    }

    public void setOnSmsOnSim(Handler h, int what, Object obj) {
        this.mSmsOnSimRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSmsOnSim(Handler h) {
        if (this.mSmsOnSimRegistrant != null && this.mSmsOnSimRegistrant.getHandler() == h) {
            this.mSmsOnSimRegistrant.clear();
            this.mSmsOnSimRegistrant = null;
        }
    }

    public void setOnSmsStatus(Handler h, int what, Object obj) {
        this.mSmsStatusRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSmsStatus(Handler h) {
        if (this.mSmsStatusRegistrant != null && this.mSmsStatusRegistrant.getHandler() == h) {
            this.mSmsStatusRegistrant.clear();
            this.mSmsStatusRegistrant = null;
        }
    }

    public void setOnSignalStrengthUpdate(Handler h, int what, Object obj) {
        this.mSignalStrengthRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSignalStrengthUpdate(Handler h) {
        if (this.mSignalStrengthRegistrant != null && this.mSignalStrengthRegistrant.getHandler() == h) {
            this.mSignalStrengthRegistrant.clear();
            this.mSignalStrengthRegistrant = null;
        }
    }

    public void setOnNITZTime(Handler h, int what, Object obj) {
        this.mNITZTimeRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNITZTime(Handler h) {
        if (this.mNITZTimeRegistrant != null && this.mNITZTimeRegistrant.getHandler() == h) {
            this.mNITZTimeRegistrant.clear();
            this.mNITZTimeRegistrant = null;
        }
    }

    public void setOnUSSD(Handler h, int what, Object obj) {
        this.mUSSDRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnUSSD(Handler h) {
        if (this.mUSSDRegistrant != null && this.mUSSDRegistrant.getHandler() == h) {
            this.mUSSDRegistrant.clear();
            this.mUSSDRegistrant = null;
        }
    }

    public void setOnSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSuppServiceNotification(Handler h) {
        if (this.mSsnRegistrant != null && this.mSsnRegistrant.getHandler() == h) {
            this.mSsnRegistrant.clear();
            this.mSsnRegistrant = null;
        }
    }

    public void setOnCatSessionEnd(Handler h, int what, Object obj) {
        this.mCatSessionEndRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatSessionEnd(Handler h) {
        if (this.mCatSessionEndRegistrant != null && this.mCatSessionEndRegistrant.getHandler() == h) {
            this.mCatSessionEndRegistrant.clear();
            this.mCatSessionEndRegistrant = null;
        }
    }

    public void setOnCatProactiveCmd(Handler h, int what, Object obj) {
        this.mCatProCmdRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatProactiveCmd(Handler h) {
        if (this.mCatProCmdRegistrant != null && this.mCatProCmdRegistrant.getHandler() == h) {
            this.mCatProCmdRegistrant.clear();
            this.mCatProCmdRegistrant = null;
        }
    }

    public void setOnCatEvent(Handler h, int what, Object obj) {
        this.mCatEventRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatEvent(Handler h) {
        if (this.mCatEventRegistrant != null && this.mCatEventRegistrant.getHandler() == h) {
            this.mCatEventRegistrant.clear();
            this.mCatEventRegistrant = null;
        }
    }

    public void setOnCatCallSetUp(Handler h, int what, Object obj) {
        this.mCatCallSetUpRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatCallSetUp(Handler h) {
        if (this.mCatCallSetUpRegistrant != null && this.mCatCallSetUpRegistrant.getHandler() == h) {
            this.mCatCallSetUpRegistrant.clear();
            this.mCatCallSetUpRegistrant = null;
        }
    }

    public void setOnIccSmsFull(Handler h, int what, Object obj) {
        this.mIccSmsFullRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnIccSmsFull(Handler h) {
        if (this.mIccSmsFullRegistrant != null && this.mIccSmsFullRegistrant.getHandler() == h) {
            this.mIccSmsFullRegistrant.clear();
            this.mIccSmsFullRegistrant = null;
        }
    }

    public void registerForIccRefresh(Handler h, int what, Object obj) {
        this.mIccRefreshRegistrants.add(new Registrant(h, what, obj));
    }

    public void setOnIccRefresh(Handler h, int what, Object obj) {
        registerForIccRefresh(h, what, obj);
    }

    public void setEmergencyCallbackMode(Handler h, int what, Object obj) {
        this.mEmergencyCallbackModeRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForIccRefresh(Handler h) {
        this.mIccRefreshRegistrants.remove(h);
    }

    public void unsetOnIccRefresh(Handler h) {
        unregisterForIccRefresh(h);
    }

    public void setOnCallRing(Handler h, int what, Object obj) {
        this.mRingRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCallRing(Handler h) {
        if (this.mRingRegistrant != null && this.mRingRegistrant.getHandler() == h) {
            this.mRingRegistrant.clear();
            this.mRingRegistrant = null;
        }
    }

    public void setOnSs(Handler h, int what, Object obj) {
        this.mSsRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnSs(Handler h) {
        this.mSsRegistrant.clear();
    }

    public void setOnCatCcAlphaNotify(Handler h, int what, Object obj) {
        this.mCatCcAlphaRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnCatCcAlphaNotify(Handler h) {
        this.mCatCcAlphaRegistrant.clear();
    }

    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
        this.mVoicePrivacyOnRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForInCallVoicePrivacyOn(Handler h) {
        this.mVoicePrivacyOnRegistrants.remove(h);
    }

    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
        this.mVoicePrivacyOffRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForInCallVoicePrivacyOff(Handler h) {
        this.mVoicePrivacyOffRegistrants.remove(h);
    }

    public void setOnRestrictedStateChanged(Handler h, int what, Object obj) {
        this.mRestrictedStateRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnRestrictedStateChanged(Handler h) {
        if (this.mRestrictedStateRegistrant != null && this.mRestrictedStateRegistrant.getHandler() == h) {
            this.mRestrictedStateRegistrant.clear();
            this.mRestrictedStateRegistrant = null;
        }
    }

    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        this.mDisplayInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDisplayInfo(Handler h) {
        this.mDisplayInfoRegistrants.remove(h);
    }

    public void registerForCallWaitingInfo(Handler h, int what, Object obj) {
        this.mCallWaitingInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallWaitingInfo(Handler h) {
        this.mCallWaitingInfoRegistrants.remove(h);
    }

    public void registerForSignalInfo(Handler h, int what, Object obj) {
        this.mSignalInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void setOnUnsolOemHookRaw(Handler h, int what, Object obj) {
        this.mUnsolOemHookRawRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnUnsolOemHookRaw(Handler h) {
        if (this.mUnsolOemHookRawRegistrant != null && this.mUnsolOemHookRawRegistrant.getHandler() == h) {
            this.mUnsolOemHookRawRegistrant.clear();
            this.mUnsolOemHookRawRegistrant = null;
        }
    }

    public void unregisterForSignalInfo(Handler h) {
        this.mSignalInfoRegistrants.remove(h);
    }

    public void registerForCdmaOtaProvision(Handler h, int what, Object obj) {
        this.mOtaProvisionRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCdmaOtaProvision(Handler h) {
        this.mOtaProvisionRegistrants.remove(h);
    }

    public void registerForNumberInfo(Handler h, int what, Object obj) {
        this.mNumberInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNumberInfo(Handler h) {
        this.mNumberInfoRegistrants.remove(h);
    }

    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
        this.mRedirNumInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRedirectedNumberInfo(Handler h) {
        this.mRedirNumInfoRegistrants.remove(h);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mLineControlInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mLineControlInfoRegistrants.remove(h);
    }

    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
        this.mT53ClirInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForT53ClirInfo(Handler h) {
        this.mT53ClirInfoRegistrants.remove(h);
    }

    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
        this.mT53AudCntrlInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForT53AudioControlInfo(Handler h) {
        this.mT53AudCntrlInfoRegistrants.remove(h);
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mRingbackToneRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRingbackTone(Handler h) {
        this.mRingbackToneRegistrants.remove(h);
    }

    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        this.mResendIncallMuteRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForResendIncallMute(Handler h) {
        this.mResendIncallMuteRegistrants.remove(h);
    }

    public void registerForCdmaSubscriptionChanged(Handler h, int what, Object obj) {
        this.mCdmaSubscriptionChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCdmaSubscriptionChanged(Handler h) {
        this.mCdmaSubscriptionChangedRegistrants.remove(h);
    }

    public void registerForCdmaPrlChanged(Handler h, int what, Object obj) {
        this.mCdmaPrlChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCdmaPrlChanged(Handler h) {
        this.mCdmaPrlChangedRegistrants.remove(h);
    }

    public void registerForExitEmergencyCallbackMode(Handler h, int what, Object obj) {
        this.mExitEmergencyCallbackModeRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForExitEmergencyCallbackMode(Handler h) {
        this.mExitEmergencyCallbackModeRegistrants.remove(h);
    }

    public void registerForHardwareConfigChanged(Handler h, int what, Object obj) {
        this.mHardwareConfigChangeRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForHardwareConfigChanged(Handler h) {
        this.mHardwareConfigChangeRegistrants.remove(h);
    }

    public void registerForRilConnected(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mRilConnectedRegistrants.add(r);
        if (this.mRilVersion != -1) {
            r.notifyRegistrant(new AsyncResult(null, new Integer(this.mRilVersion), null));
        }
    }

    public void unregisterForRilConnected(Handler h) {
        this.mRilConnectedRegistrants.remove(h);
    }

    public void registerForSubscriptionStatusChanged(Handler h, int what, Object obj) {
        this.mSubscriptionStatusRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSubscriptionStatusChanged(Handler h) {
        this.mSubscriptionStatusRegistrants.remove(h);
    }

    /* JADX WARNING: Missing block: B:33:0x0073, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void setRadioState(RadioState newState) {
        synchronized (this.mStateMonitor) {
            RadioState oldState = this.mState;
            this.mState = newState;
            if (oldState == this.mState) {
                return;
            }
            this.mRadioStateChangedRegistrants.notifyRegistrants();
            if (this.mState.isAvailable() && (oldState.isAvailable() ^ 1) != 0) {
                this.mAvailRegistrants.notifyRegistrants();
            }
            if (!this.mState.isAvailable() && oldState.isAvailable()) {
                this.mNotAvailRegistrants.notifyRegistrants();
            }
            if (this.mState.isOn() && (oldState.isOn() ^ 1) != 0) {
                this.mOnRegistrants.notifyRegistrants();
            }
            if (!(this.mState.isOn() && (this.mState.isAvailable() ^ 1) == 0) && oldState.isOn() && oldState.isAvailable()) {
                this.mOffOrNotAvailRegistrants.notifyRegistrants();
            }
        }
    }

    public int getLteOnCdmaMode() {
        return TelephonyManager.getLteOnCdmaModeStatic();
    }

    public void registerForCellInfoList(Handler h, int what, Object obj) {
        this.mRilCellInfoListRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCellInfoList(Handler h) {
        this.mRilCellInfoListRegistrants.remove(h);
    }

    public void registerForSrvccStateChanged(Handler h, int what, Object obj) {
        this.mSrvccStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSrvccStateChanged(Handler h) {
        this.mSrvccStateRegistrants.remove(h);
    }

    public void registerForRSrvccStateChanged(Handler h, int what, Object obj) {
        this.mRSrvccStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRSrvccStateChanged(Handler h) {
        this.mRSrvccStateRegistrants.remove(h);
    }

    public void testingEmergencyCall() {
    }

    public int getRilVersion() {
        return this.mRilVersion;
    }

    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message response) {
    }

    public void setDataAllowed(boolean allowed, Message response) {
    }

    public void requestShutdown(Message result) {
    }

    public void getRadioCapability(Message result) {
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
    }

    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
        this.mPhoneRadioCapabilityChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForRadioCapabilityChanged(Handler h) {
        this.mPhoneRadioCapabilityChangedRegistrants.remove(h);
    }

    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
    }

    public void stopLceService(Message result) {
    }

    public void pullLceData(Message result) {
    }

    public void registerForLceInfo(Handler h, int what, Object obj) {
        this.mLceInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForLceInfo(Handler h) {
        if (this.mLceInfoRegistrant != null && this.mLceInfoRegistrant.getHandler() == h) {
            this.mLceInfoRegistrant.clear();
            this.mLceInfoRegistrant = null;
        }
    }

    public void registerForPcoData(Handler h, int what, Object obj) {
        this.mPcoDataRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForPcoData(Handler h) {
        this.mPcoDataRegistrants.remove(h);
    }

    public void getCdmaGsmImsi(Message result) {
    }

    public void testVoiceLoopBack(int mode) {
    }
}

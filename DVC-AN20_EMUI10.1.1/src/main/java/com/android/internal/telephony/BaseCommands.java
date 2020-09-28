package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.TelephonyManager;

public abstract class BaseCommands extends AbstractBaseCommands implements CommandsInterface {
    protected RegistrantList mAvailRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mCallStateRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mCallWaitingInfoRegistrants = new RegistrantList();
    protected RegistrantList mCarrierInfoForImsiEncryptionRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mCatCallSetUpRegistrant;
    @UnsupportedAppUsage
    protected Registrant mCatCcAlphaRegistrant;
    @UnsupportedAppUsage
    protected Registrant mCatEventRegistrant;
    @UnsupportedAppUsage
    protected Registrant mCatProCmdRegistrant;
    @UnsupportedAppUsage
    protected Registrant mCatSessionEndRegistrant;
    @UnsupportedAppUsage
    protected RegistrantList mCdmaPrlChangedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mCdmaSmsRegistrant;
    protected int mCdmaSubscription;
    @UnsupportedAppUsage
    protected RegistrantList mCdmaSubscriptionChangedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Context mContext;
    protected RegistrantList mDataCallListChangedRegistrants = new RegistrantList();
    protected RegistrantList mDisplayInfoRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mEmergencyCallbackModeRegistrant;
    protected RegistrantList mEmergencyNumberListRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mExitEmergencyCallbackModeRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mGsmBroadcastSmsRegistrant;
    @UnsupportedAppUsage
    protected Registrant mGsmSmsRegistrant;
    @UnsupportedAppUsage
    protected RegistrantList mHardwareConfigChangeRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mIccRefreshRegistrants = new RegistrantList();
    protected RegistrantList mIccSlotStatusChangedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mIccSmsFullRegistrant;
    @UnsupportedAppUsage
    protected RegistrantList mIccStatusChangedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mImsNetworkStateChangedRegistrants = new RegistrantList();
    protected RegistrantList mLceInfoRegistrants = new RegistrantList();
    protected RegistrantList mLineControlInfoRegistrants = new RegistrantList();
    protected RegistrantList mModemResetRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mNITZTimeRegistrant;
    protected RegistrantList mNattKeepaliveStatusRegistrants = new RegistrantList();
    protected RegistrantList mNetworkStateRegistrants = new RegistrantList();
    protected RegistrantList mNotAvailRegistrants = new RegistrantList();
    protected RegistrantList mNumberInfoRegistrants = new RegistrantList();
    protected RegistrantList mOffOrNotAvailRegistrants = new RegistrantList();
    protected RegistrantList mOnRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mOtaProvisionRegistrants = new RegistrantList();
    protected RegistrantList mPcoDataRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mPhoneRadioCapabilityChangedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected int mPhoneType;
    protected RegistrantList mPhysicalChannelConfigurationRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected int mPreferredNetworkType;
    protected RegistrantList mRadioStateChangedRegistrants = new RegistrantList();
    protected RegistrantList mRedirNumInfoRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mResendIncallMuteRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mRestrictedStateRegistrant;
    @UnsupportedAppUsage
    protected RegistrantList mRilCellInfoListRegistrants = new RegistrantList();
    protected RegistrantList mRilConnectedRegistrants = new RegistrantList();
    protected RegistrantList mRilNetworkScanResultRegistrants = new RegistrantList();
    protected int mRilVersion = -1;
    @UnsupportedAppUsage
    protected Registrant mRingRegistrant;
    @UnsupportedAppUsage
    protected RegistrantList mRingbackToneRegistrants = new RegistrantList();
    protected RegistrantList mSignalInfoRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mSignalStrengthRegistrant;
    @UnsupportedAppUsage
    protected Registrant mSmsOnSimRegistrant;
    @UnsupportedAppUsage
    protected Registrant mSmsStatusRegistrant;
    @UnsupportedAppUsage
    protected RegistrantList mSrvccStateRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mSsRegistrant;
    @UnsupportedAppUsage
    protected Registrant mSsnRegistrant;
    protected int mState = 2;
    @UnsupportedAppUsage
    protected Object mStateMonitor = new Object();
    @UnsupportedAppUsage
    protected RegistrantList mSubscriptionStatusRegistrants = new RegistrantList();
    protected RegistrantList mT53AudCntrlInfoRegistrants = new RegistrantList();
    protected RegistrantList mT53ClirInfoRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected Registrant mUSSDRegistrant;
    @UnsupportedAppUsage
    protected Registrant mUnsolOemHookRawRegistrant;
    protected RegistrantList mVoicePrivacyOffRegistrants = new RegistrantList();
    protected RegistrantList mVoicePrivacyOnRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mVoiceRadioTechChangedRegistrants = new RegistrantList();

    public BaseCommands(Context context) {
        this.mContext = context;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public int getRadioState() {
        return this.mState;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRadioStateChanged(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mRadioStateChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRadioStateChanged(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mRadioStateChangedRegistrants.remove(h);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForImsNetworkStateChanged(Handler h, int what, Object obj) {
        this.mImsNetworkStateChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForImsNetworkStateChanged(Handler h) {
        this.mImsNetworkStateChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.add(r);
            if (this.mState == 1) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForOn(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOnRegistrants.remove(h);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mAvailRegistrants.add(r);
            if (this.mState != 2) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mAvailRegistrants.remove(h);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.add(r);
            if (this.mState == 2) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNotAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mNotAvailRegistrants.remove(h);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForOffOrNotAvailable(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mOffOrNotAvailRegistrants.add(r);
            if (this.mState == 0 || this.mState == 2) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForOffOrNotAvailable(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mOffOrNotAvailRegistrants.remove(h);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCallStateChanged(Handler h, int what, Object obj) {
        this.mCallStateRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCallStateChanged(Handler h) {
        this.mCallStateRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNetworkStateChanged(Handler h, int what, Object obj) {
        this.mNetworkStateRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNetworkStateChanged(Handler h) {
        this.mNetworkStateRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForDataCallListChanged(Handler h, int what, Object obj) {
        this.mDataCallListChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForDataCallListChanged(Handler h) {
        this.mDataCallListChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForVoiceRadioTechChanged(Handler h, int what, Object obj) {
        this.mVoiceRadioTechChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForVoiceRadioTechChanged(Handler h) {
        this.mVoiceRadioTechChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForIccStatusChanged(Handler h, int what, Object obj) {
        this.mIccStatusChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForIccStatusChanged(Handler h) {
        this.mIccStatusChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForIccSlotStatusChanged(Handler h, int what, Object obj) {
        this.mIccSlotStatusChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForIccSlotStatusChanged(Handler h) {
        this.mIccSlotStatusChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNewGsmSms(Handler h, int what, Object obj) {
        this.mGsmSmsRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNewGsmSms(Handler h) {
        Registrant registrant = this.mGsmSmsRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mGsmSmsRegistrant.clear();
            this.mGsmSmsRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNewCdmaSms(Handler h, int what, Object obj) {
        this.mCdmaSmsRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNewCdmaSms(Handler h) {
        Registrant registrant = this.mCdmaSmsRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCdmaSmsRegistrant.clear();
            this.mCdmaSmsRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNewGsmBroadcastSms(Handler h, int what, Object obj) {
        this.mGsmBroadcastSmsRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNewGsmBroadcastSms(Handler h) {
        Registrant registrant = this.mGsmBroadcastSmsRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mGsmBroadcastSmsRegistrant.clear();
            this.mGsmBroadcastSmsRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSmsOnSim(Handler h, int what, Object obj) {
        this.mSmsOnSimRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSmsOnSim(Handler h) {
        Registrant registrant = this.mSmsOnSimRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mSmsOnSimRegistrant.clear();
            this.mSmsOnSimRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSmsStatus(Handler h, int what, Object obj) {
        this.mSmsStatusRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSmsStatus(Handler h) {
        Registrant registrant = this.mSmsStatusRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mSmsStatusRegistrant.clear();
            this.mSmsStatusRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSignalStrengthUpdate(Handler h, int what, Object obj) {
        this.mSignalStrengthRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSignalStrengthUpdate(Handler h) {
        Registrant registrant = this.mSignalStrengthRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mSignalStrengthRegistrant.clear();
            this.mSignalStrengthRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnNITZTime(Handler h, int what, Object obj) {
        this.mNITZTimeRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnNITZTime(Handler h) {
        Registrant registrant = this.mNITZTimeRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mNITZTimeRegistrant.clear();
            this.mNITZTimeRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnUSSD(Handler h, int what, Object obj) {
        this.mUSSDRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnUSSD(Handler h) {
        Registrant registrant = this.mUSSDRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mUSSDRegistrant.clear();
            this.mUSSDRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSuppServiceNotification(Handler h) {
        Registrant registrant = this.mSsnRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mSsnRegistrant.clear();
            this.mSsnRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatSessionEnd(Handler h, int what, Object obj) {
        this.mCatSessionEndRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatSessionEnd(Handler h) {
        Registrant registrant = this.mCatSessionEndRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCatSessionEndRegistrant.clear();
            this.mCatSessionEndRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatProactiveCmd(Handler h, int what, Object obj) {
        this.mCatProCmdRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatProactiveCmd(Handler h) {
        Registrant registrant = this.mCatProCmdRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCatProCmdRegistrant.clear();
            this.mCatProCmdRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatEvent(Handler h, int what, Object obj) {
        this.mCatEventRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatEvent(Handler h) {
        Registrant registrant = this.mCatEventRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCatEventRegistrant.clear();
            this.mCatEventRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatCallSetUp(Handler h, int what, Object obj) {
        this.mCatCallSetUpRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatCallSetUp(Handler h) {
        Registrant registrant = this.mCatCallSetUpRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCatCallSetUpRegistrant.clear();
            this.mCatCallSetUpRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnIccSmsFull(Handler h, int what, Object obj) {
        this.mIccSmsFullRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnIccSmsFull(Handler h) {
        Registrant registrant = this.mIccSmsFullRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mIccSmsFullRegistrant.clear();
            this.mIccSmsFullRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForIccRefresh(Handler h, int what, Object obj) {
        this.mIccRefreshRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnIccRefresh(Handler h, int what, Object obj) {
        registerForIccRefresh(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setEmergencyCallbackMode(Handler h, int what, Object obj) {
        this.mEmergencyCallbackModeRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForIccRefresh(Handler h) {
        this.mIccRefreshRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unsetOnIccRefresh(Handler h) {
        unregisterForIccRefresh(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCallRing(Handler h, int what, Object obj) {
        this.mRingRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCallRing(Handler h) {
        Registrant registrant = this.mRingRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mRingRegistrant.clear();
            this.mRingRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnSs(Handler h, int what, Object obj) {
        this.mSsRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnSs(Handler h) {
        this.mSsRegistrant.clear();
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnCatCcAlphaNotify(Handler h, int what, Object obj) {
        this.mCatCcAlphaRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnCatCcAlphaNotify(Handler h) {
        this.mCatCcAlphaRegistrant.clear();
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
        this.mVoicePrivacyOnRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForInCallVoicePrivacyOn(Handler h) {
        this.mVoicePrivacyOnRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
        this.mVoicePrivacyOffRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForInCallVoicePrivacyOff(Handler h) {
        this.mVoicePrivacyOffRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnRestrictedStateChanged(Handler h, int what, Object obj) {
        this.mRestrictedStateRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnRestrictedStateChanged(Handler h) {
        Registrant registrant = this.mRestrictedStateRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mRestrictedStateRegistrant.clear();
            this.mRestrictedStateRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        this.mDisplayInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForDisplayInfo(Handler h) {
        this.mDisplayInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCallWaitingInfo(Handler h, int what, Object obj) {
        this.mCallWaitingInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCallWaitingInfo(Handler h) {
        this.mCallWaitingInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForSignalInfo(Handler h, int what, Object obj) {
        this.mSignalInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setOnUnsolOemHookRaw(Handler h, int what, Object obj) {
        this.mUnsolOemHookRawRegistrant = new Registrant(h, what, obj);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unSetOnUnsolOemHookRaw(Handler h) {
        Registrant registrant = this.mUnsolOemHookRawRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mUnsolOemHookRawRegistrant.clear();
            this.mUnsolOemHookRawRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForSignalInfo(Handler h) {
        this.mSignalInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCdmaOtaProvision(Handler h, int what, Object obj) {
        this.mOtaProvisionRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCdmaOtaProvision(Handler h) {
        this.mOtaProvisionRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNumberInfo(Handler h, int what, Object obj) {
        this.mNumberInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNumberInfo(Handler h) {
        this.mNumberInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
        this.mRedirNumInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRedirectedNumberInfo(Handler h) {
        this.mRedirNumInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mLineControlInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForLineControlInfo(Handler h) {
        this.mLineControlInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
        this.mT53ClirInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForT53ClirInfo(Handler h) {
        this.mT53ClirInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
        this.mT53AudCntrlInfoRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForT53AudioControlInfo(Handler h) {
        this.mT53AudCntrlInfoRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mRingbackToneRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRingbackTone(Handler h) {
        this.mRingbackToneRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        this.mResendIncallMuteRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForResendIncallMute(Handler h) {
        this.mResendIncallMuteRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCdmaSubscriptionChanged(Handler h, int what, Object obj) {
        this.mCdmaSubscriptionChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCdmaSubscriptionChanged(Handler h) {
        this.mCdmaSubscriptionChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCdmaPrlChanged(Handler h, int what, Object obj) {
        this.mCdmaPrlChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCdmaPrlChanged(Handler h) {
        this.mCdmaPrlChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForExitEmergencyCallbackMode(Handler h, int what, Object obj) {
        this.mExitEmergencyCallbackModeRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForExitEmergencyCallbackMode(Handler h) {
        this.mExitEmergencyCallbackModeRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForHardwareConfigChanged(Handler h, int what, Object obj) {
        this.mHardwareConfigChangeRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForHardwareConfigChanged(Handler h) {
        this.mHardwareConfigChangeRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNetworkScanResult(Handler h, int what, Object obj) {
        this.mRilNetworkScanResultRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNetworkScanResult(Handler h) {
        this.mRilNetworkScanResultRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRilConnected(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mRilConnectedRegistrants.add(r);
        int i = this.mRilVersion;
        if (i != -1) {
            r.notifyRegistrant(new AsyncResult((Object) null, new Integer(i), (Throwable) null));
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRilConnected(Handler h) {
        this.mRilConnectedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForSubscriptionStatusChanged(Handler h, int what, Object obj) {
        this.mSubscriptionStatusRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForSubscriptionStatusChanged(Handler h) {
        this.mSubscriptionStatusRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForEmergencyNumberList(Handler h, int what, Object obj) {
        this.mEmergencyNumberListRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForEmergencyNumberList(Handler h) {
        this.mEmergencyNumberListRegistrants.remove(h);
    }

    /* access modifiers changed from: protected */
    public void setRadioState(int newState, boolean forceNotifyRegistrants) {
        synchronized (this.mStateMonitor) {
            int oldState = this.mState;
            this.mState = newState;
            if (oldState != this.mState || forceNotifyRegistrants) {
                this.mRadioStateChangedRegistrants.notifyRegistrants();
                if (this.mState != 2 && oldState == 2) {
                    this.mAvailRegistrants.notifyRegistrants();
                }
                if (this.mState == 2 && oldState != 2) {
                    this.mNotAvailRegistrants.notifyRegistrants();
                }
                if (this.mState == 1 && oldState != 1) {
                    this.mOnRegistrants.notifyRegistrants();
                }
                if ((this.mState == 0 || this.mState == 2) && oldState == 1) {
                    this.mOffOrNotAvailRegistrants.notifyRegistrants();
                }
            }
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public int getLteOnCdmaMode() {
        return TelephonyManager.getLteOnCdmaModeStatic();
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCellInfoList(Handler h, int what, Object obj) {
        this.mRilCellInfoListRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCellInfoList(Handler h) {
        this.mRilCellInfoListRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForPhysicalChannelConfiguration(Handler h, int what, Object obj) {
        this.mPhysicalChannelConfigurationRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForPhysicalChannelConfiguration(Handler h) {
        this.mPhysicalChannelConfigurationRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForSrvccStateChanged(Handler h, int what, Object obj) {
        this.mSrvccStateRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForSrvccStateChanged(Handler h) {
        this.mSrvccStateRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void testingEmergencyCall() {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public int getRilVersion() {
        return this.mRilVersion;
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setUiccSubscription(int slotId, int appIndex, int subId, int subStatus, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setDataAllowed(boolean allowed, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void requestShutdown(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void getRadioCapability(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void setRadioCapability(RadioCapability rc, Message response) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
        this.mPhoneRadioCapabilityChangedRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForRadioCapabilityChanged(Handler h) {
        this.mPhoneRadioCapabilityChangedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void startLceService(int reportIntervalMs, boolean pullMode, Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void stopLceService(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void pullLceData(Message result) {
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForLceInfo(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mLceInfoRegistrants.add(r);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForLceInfo(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mLceInfoRegistrants.remove(h);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForModemReset(Handler h, int what, Object obj) {
        this.mModemResetRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForModemReset(Handler h) {
        this.mModemResetRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForPcoData(Handler h, int what, Object obj) {
        this.mPcoDataRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForPcoData(Handler h) {
        this.mPcoDataRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForCarrierInfoForImsiEncryption(Handler h, int what, Object obj) {
        this.mCarrierInfoForImsiEncryptionRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForCarrierInfoForImsiEncryption(Handler h) {
        this.mCarrierInfoForImsiEncryptionRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void registerForNattKeepaliveStatus(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mStateMonitor) {
            this.mNattKeepaliveStatusRegistrants.add(r);
        }
    }

    @Override // com.android.internal.telephony.CommandsInterface
    public void unregisterForNattKeepaliveStatus(Handler h) {
        synchronized (this.mStateMonitor) {
            this.mNattKeepaliveStatusRegistrants.remove(h);
        }
    }
}

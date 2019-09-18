package com.android.internal.telephony.imsphone;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.LinkProperties;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telecom.Connection;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UssdResponse;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsInfo;
import android.text.TextUtils;
import com.android.ims.HwImsUtManager;
import com.android.ims.ImsEcbmStateListener;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsUt;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwChrServiceManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.util.NotificationChannelController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ImsPhone extends ImsPhoneBase {
    private static final boolean CALL_WAITING_CLASS_NONE = SystemProperties.getBoolean("ro.config.cw_no_class", false);
    static final int CANCEL_ECM_TIMER = 1;
    private static final boolean DBG = true;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final int EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED = 53;
    private static final int EVENT_GET_CALL_BARRING_DONE = 48;
    private static final int EVENT_GET_CALL_WAITING_DONE = 50;
    private static final int EVENT_GET_CLIR_DONE = 52;
    private static final int EVENT_SERVICE_STATE_CHANGED = 54;
    private static final int EVENT_SET_CALL_BARRING_DONE = 47;
    private static final int EVENT_SET_CALL_WAITING_DONE = 49;
    private static final int EVENT_SET_CLIR_DONE = 51;
    private static final int EVENT_VOICE_CALL_ENDED = 55;
    private static final String LOG_TAG = "ImsPhone";
    static final int RESTART_ECM_TIMER = 0;
    private static final boolean VDBG = false;
    ImsPhoneCallTracker mCT;
    private Uri[] mCurrentSubscriberUris;
    Phone mDefaultPhone;
    private Registrant mEcmExitRespRegistrant;
    private Runnable mExitEcmRunnable;
    ImsExternalCallTracker mExternalCallTracker;
    public IHwImsPhoneEx mHwImsPhoneEx;
    private ImsEcbmStateListener mImsEcbmStateListener;
    private boolean mImsRegistered;
    private String mLastDialString;
    private ServiceState mOldSS;
    private ArrayList<ImsPhoneMmiCode> mPendingMMIs;
    private BroadcastReceiver mResultReceiver;
    private boolean mRoaming;
    private ServiceState mSS;
    private final RegistrantList mSilentRedialRegistrants;
    private RegistrantList mSsnRegistrants;
    private PowerManager.WakeLock mWakeLock;

    private static class Cf {
        final boolean mIsCfu;
        final Message mOnComplete;
        final String mSetCfNumber;

        Cf(String cfNumber, boolean isCfu, Message onComplete) {
            this.mSetCfNumber = cfNumber;
            this.mIsCfu = isCfu;
            this.mOnComplete = onComplete;
        }
    }

    public static class ImsDialArgs extends PhoneInternalInterface.DialArgs {
        public final int clirMode;
        public final Connection.RttTextStream rttTextStream;

        public static class Builder extends PhoneInternalInterface.DialArgs.Builder<Builder> {
            /* access modifiers changed from: private */
            public int mClirMode = 0;
            /* access modifiers changed from: private */
            public Connection.RttTextStream mRttTextStream;

            public static Builder from(PhoneInternalInterface.DialArgs dialArgs) {
                return (Builder) ((Builder) ((Builder) new Builder().setUusInfo(dialArgs.uusInfo)).setVideoState(dialArgs.videoState)).setIntentExtras(dialArgs.intentExtras);
            }

            public static Builder from(ImsDialArgs dialArgs) {
                return ((Builder) ((Builder) ((Builder) new Builder().setUusInfo(dialArgs.uusInfo)).setVideoState(dialArgs.videoState)).setIntentExtras(dialArgs.intentExtras)).setRttTextStream(dialArgs.rttTextStream).setClirMode(dialArgs.clirMode);
            }

            public Builder setRttTextStream(Connection.RttTextStream s) {
                this.mRttTextStream = s;
                return this;
            }

            public Builder setClirMode(int clirMode) {
                this.mClirMode = clirMode;
                return this;
            }

            public ImsDialArgs build() {
                return new ImsDialArgs(this);
            }
        }

        private ImsDialArgs(Builder b) {
            super(b);
            this.rttTextStream = b.mRttTextStream;
            this.clirMode = b.mClirMode;
        }
    }

    public /* bridge */ /* synthetic */ void activateCellBroadcastSms(int i, Message message) {
        super.activateCellBroadcastSms(i, message);
    }

    public /* bridge */ /* synthetic */ boolean disableDataConnectivity() {
        return super.disableDataConnectivity();
    }

    public /* bridge */ /* synthetic */ void disableLocationUpdates() {
        super.disableLocationUpdates();
    }

    public /* bridge */ /* synthetic */ boolean enableDataConnectivity() {
        return super.enableDataConnectivity();
    }

    public /* bridge */ /* synthetic */ void enableLocationUpdates() {
        super.enableLocationUpdates();
    }

    public /* bridge */ /* synthetic */ List getAllCellInfo(WorkSource workSource) {
        return super.getAllCellInfo(workSource);
    }

    public /* bridge */ /* synthetic */ void getAvailableNetworks(Message message) {
        super.getAvailableNetworks(message);
    }

    public /* bridge */ /* synthetic */ void getCellBroadcastSmsConfig(Message message) {
        super.getCellBroadcastSmsConfig(message);
    }

    public /* bridge */ /* synthetic */ CellLocation getCellLocation(WorkSource workSource) {
        return super.getCellLocation(workSource);
    }

    public /* bridge */ /* synthetic */ List getCurrentDataConnectionList() {
        return super.getCurrentDataConnectionList();
    }

    public /* bridge */ /* synthetic */ PhoneInternalInterface.DataActivityState getDataActivityState() {
        return super.getDataActivityState();
    }

    public /* bridge */ /* synthetic */ PhoneConstants.DataState getDataConnectionState() {
        return super.getDataConnectionState();
    }

    public /* bridge */ /* synthetic */ PhoneConstants.DataState getDataConnectionState(String str) {
        return super.getDataConnectionState(str);
    }

    public /* bridge */ /* synthetic */ boolean getDataRoamingEnabled() {
        return super.getDataRoamingEnabled();
    }

    public /* bridge */ /* synthetic */ String getDeviceId() {
        return super.getDeviceId();
    }

    public /* bridge */ /* synthetic */ String getDeviceSvn() {
        return super.getDeviceSvn();
    }

    public /* bridge */ /* synthetic */ String getEsn() {
        return super.getEsn();
    }

    public /* bridge */ /* synthetic */ String getGroupIdLevel1() {
        return super.getGroupIdLevel1();
    }

    public /* bridge */ /* synthetic */ String getGroupIdLevel2() {
        return super.getGroupIdLevel2();
    }

    public /* bridge */ /* synthetic */ IccCard getIccCard() {
        return super.getIccCard();
    }

    public /* bridge */ /* synthetic */ IccFileHandler getIccFileHandler() {
        return super.getIccFileHandler();
    }

    public /* bridge */ /* synthetic */ IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return super.getIccPhoneBookInterfaceManager();
    }

    public /* bridge */ /* synthetic */ boolean getIccRecordsLoaded() {
        return super.getIccRecordsLoaded();
    }

    public /* bridge */ /* synthetic */ String getIccSerialNumber() {
        return super.getIccSerialNumber();
    }

    public /* bridge */ /* synthetic */ String getImei() {
        return super.getImei();
    }

    public /* bridge */ /* synthetic */ String getLine1AlphaTag() {
        return super.getLine1AlphaTag();
    }

    public /* bridge */ /* synthetic */ String getLine1Number() {
        return super.getLine1Number();
    }

    public /* bridge */ /* synthetic */ LinkProperties getLinkProperties(String str) {
        return super.getLinkProperties(str);
    }

    public /* bridge */ /* synthetic */ String getMeid() {
        return super.getMeid();
    }

    public /* bridge */ /* synthetic */ boolean getMessageWaitingIndicator() {
        return super.getMessageWaitingIndicator();
    }

    public /* bridge */ /* synthetic */ int getPhoneType() {
        return super.getPhoneType();
    }

    public /* bridge */ /* synthetic */ SignalStrength getSignalStrength() {
        return super.getSignalStrength();
    }

    public /* bridge */ /* synthetic */ String getVoiceMailAlphaTag() {
        return super.getVoiceMailAlphaTag();
    }

    public /* bridge */ /* synthetic */ String getVoiceMailNumber() {
        return super.getVoiceMailNumber();
    }

    public /* bridge */ /* synthetic */ boolean handlePinMmi(String str) {
        return super.handlePinMmi(str);
    }

    public /* bridge */ /* synthetic */ boolean isDataAllowed() {
        return super.isDataAllowed();
    }

    public /* bridge */ /* synthetic */ boolean isDataEnabled() {
        return super.isDataEnabled();
    }

    public /* bridge */ /* synthetic */ boolean isUserDataEnabled() {
        return super.isUserDataEnabled();
    }

    public /* bridge */ /* synthetic */ void migrateFrom(Phone phone) {
        super.migrateFrom(phone);
    }

    public /* bridge */ /* synthetic */ boolean needsOtaServiceProvisioning() {
        return super.needsOtaServiceProvisioning();
    }

    public /* bridge */ /* synthetic */ void notifyCallForwardingIndicator() {
        super.notifyCallForwardingIndicator();
    }

    public /* bridge */ /* synthetic */ void notifyDisconnect(com.android.internal.telephony.Connection connection) {
        super.notifyDisconnect(connection);
    }

    public /* bridge */ /* synthetic */ void notifyPhoneStateChanged() {
        super.notifyPhoneStateChanged();
    }

    public /* bridge */ /* synthetic */ void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChanged();
    }

    public /* bridge */ /* synthetic */ void notifySuppServiceFailed(PhoneInternalInterface.SuppService suppService) {
        super.notifySuppServiceFailed(suppService);
    }

    public /* bridge */ /* synthetic */ void onTtyModeReceived(int i) {
        super.onTtyModeReceived(i);
    }

    public /* bridge */ /* synthetic */ void registerForOnHoldTone(Handler handler, int i, Object obj) {
        super.registerForOnHoldTone(handler, i, obj);
    }

    public /* bridge */ /* synthetic */ void registerForRingbackTone(Handler handler, int i, Object obj) {
        super.registerForRingbackTone(handler, i, obj);
    }

    public /* bridge */ /* synthetic */ void registerForTtyModeReceived(Handler handler, int i, Object obj) {
        super.registerForTtyModeReceived(handler, i, obj);
    }

    public /* bridge */ /* synthetic */ void selectNetworkManually(OperatorInfo operatorInfo, boolean z, Message message) {
        super.selectNetworkManually(operatorInfo, z, message);
    }

    public /* bridge */ /* synthetic */ void setCellBroadcastSmsConfig(int[] iArr, Message message) {
        super.setCellBroadcastSmsConfig(iArr, message);
    }

    public /* bridge */ /* synthetic */ void setDataRoamingEnabled(boolean z) {
        super.setDataRoamingEnabled(z);
    }

    public /* bridge */ /* synthetic */ boolean setLine1Number(String str, String str2, Message message) {
        return super.setLine1Number(str, str2, message);
    }

    public /* bridge */ /* synthetic */ void setNetworkSelectionModeAutomatic(Message message) {
        super.setNetworkSelectionModeAutomatic(message);
    }

    public /* bridge */ /* synthetic */ void setRadioPower(boolean z) {
        super.setRadioPower(z);
    }

    public /* bridge */ /* synthetic */ void setUserDataEnabled(boolean z) {
        super.setUserDataEnabled(z);
    }

    public /* bridge */ /* synthetic */ void setVoiceMailNumber(String str, String str2, Message message) {
        super.setVoiceMailNumber(str, str2, message);
    }

    public /* bridge */ /* synthetic */ void startNetworkScan(NetworkScanRequest networkScanRequest, Message message) {
        super.startNetworkScan(networkScanRequest, message);
    }

    @VisibleForTesting
    public /* bridge */ /* synthetic */ void startOnHoldTone(com.android.internal.telephony.Connection connection) {
        super.startOnHoldTone(connection);
    }

    public /* bridge */ /* synthetic */ void startRingbackTone() {
        super.startRingbackTone();
    }

    public /* bridge */ /* synthetic */ void stopNetworkScan(Message message) {
        super.stopNetworkScan(message);
    }

    public /* bridge */ /* synthetic */ void stopRingbackTone() {
        super.stopRingbackTone();
    }

    public /* bridge */ /* synthetic */ void unregisterForOnHoldTone(Handler handler) {
        super.unregisterForOnHoldTone(handler);
    }

    public /* bridge */ /* synthetic */ void unregisterForRingbackTone(Handler handler) {
        super.unregisterForRingbackTone(handler);
    }

    public /* bridge */ /* synthetic */ void unregisterForTtyModeReceived(Handler handler) {
        super.unregisterForTtyModeReceived(handler);
    }

    public /* bridge */ /* synthetic */ void updateServiceLocation() {
        super.updateServiceLocation();
    }

    /* access modifiers changed from: protected */
    public void setCurrentSubscriberUris(Uri[] currentSubscriberUris) {
        this.mCurrentSubscriberUris = currentSubscriberUris;
    }

    public Uri[] getCurrentSubscriberUris() {
        return this.mCurrentSubscriberUris;
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone) {
        this(context, notifier, defaultPhone, false);
    }

    @VisibleForTesting
    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone, boolean unitTestMode) {
        super(LOG_TAG, context, notifier, unitTestMode);
        this.mPendingMMIs = new ArrayList<>();
        this.mSS = new ServiceState();
        this.mOldSS = new ServiceState();
        this.mSilentRedialRegistrants = new RegistrantList();
        this.mImsRegistered = false;
        this.mRoaming = false;
        this.mSsnRegistrants = new RegistrantList();
        this.mExitEcmRunnable = new Runnable() {
            public void run() {
                ImsPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mImsEcbmStateListener = new ImsEcbmStateListener() {
            public void onECBMEntered() {
                ImsPhone.this.logd("onECBMEntered");
                ImsPhone.this.handleEnterEmergencyCallbackMode();
            }

            public void onECBMExited() {
                ImsPhone.this.logd("onECBMExited");
                ImsPhone.this.handleExitEmergencyCallbackMode();
            }
        };
        this.mResultReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == -1) {
                    CharSequence title = intent.getCharSequenceExtra(Phone.EXTRA_KEY_ALERT_TITLE);
                    CharSequence messageAlert = intent.getCharSequenceExtra(Phone.EXTRA_KEY_ALERT_MESSAGE);
                    CharSequence messageNotification = intent.getCharSequenceExtra(Phone.EXTRA_KEY_NOTIFICATION_MESSAGE);
                    Intent resultIntent = new Intent("android.intent.action.MAIN");
                    resultIntent.setClassName("com.android.settings", "com.android.settings.Settings$WifiCallingSettingsActivity");
                    resultIntent.putExtra(Phone.EXTRA_KEY_ALERT_SHOW, true);
                    resultIntent.putExtra(Phone.EXTRA_KEY_ALERT_TITLE, title);
                    resultIntent.putExtra(Phone.EXTRA_KEY_ALERT_MESSAGE, messageAlert);
                    ((NotificationManager) ImsPhone.this.mContext.getSystemService("notification")).notify("wifi_calling", 1, new Notification.Builder(ImsPhone.this.mContext).setSmallIcon(17301642).setContentTitle(title).setContentText(messageNotification).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(ImsPhone.this.mContext, 0, resultIntent, 134217728)).setStyle(new Notification.BigTextStyle().bigText(messageNotification)).setChannelId(NotificationChannelController.CHANNEL_ID_WFC).build());
                }
            }
        };
        this.mDefaultPhone = defaultPhone;
        this.mExternalCallTracker = TelephonyComponentFactory.getInstance().makeImsExternalCallTracker(this);
        this.mCT = TelephonyComponentFactory.getInstance().makeImsPhoneCallTracker(this);
        this.mCT.registerPhoneStateListener(this.mExternalCallTracker);
        this.mExternalCallTracker.setCallPuller(this.mCT);
        this.mSS.setStateOff();
        this.mOldSS.setStateOff();
        this.mPhoneId = this.mDefaultPhone.getPhoneId();
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mWakeLock.setReferenceCounted(false);
        if (this.mDefaultPhone.getServiceStateTracker() != null) {
            this.mDefaultPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 53, null);
        }
        setServiceState(1);
        this.mDefaultPhone.registerForServiceStateChanged(this, 54, null);
        this.mHwImsPhoneEx = HwTelephonyFactory.getHwPhoneManager().createHwImsPhoneEx(this.mCT);
    }

    public void dispose() {
        logd("dispose");
        this.mPendingMMIs.clear();
        this.mExternalCallTracker.tearDown();
        this.mCT.unregisterPhoneStateListener(this.mExternalCallTracker);
        this.mCT.unregisterForVoiceCallEnded(this);
        this.mCT.dispose();
        if (this.mDefaultPhone != null && this.mDefaultPhone.getServiceStateTracker() != null) {
            this.mDefaultPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this);
            this.mDefaultPhone.unregisterForServiceStateChanged(this);
        }
    }

    public ServiceState getServiceState() {
        return this.mSS;
    }

    @VisibleForTesting
    public void setServiceState(int state) {
        boolean hasChange;
        boolean isVoiceRegStateChanged;
        boolean hasChange2;
        synchronized (this) {
            hasChange = false;
            isVoiceRegStateChanged = this.mSS.getVoiceRegState() != state;
            this.mSS.setVoiceRegState(state);
        }
        if (this.mSS.getVoiceRegState() != this.mOldSS.getVoiceRegState()) {
            hasChange = true;
        }
        Rlog.d(LOG_TAG, "hasChange:" + hasChange2 + "  mOldSS=" + this.mOldSS + "  mSS=" + this.mSS);
        if (hasChange2) {
            this.mDefaultPhone.notifyServiceStateChangedP(this.mSS);
        }
        this.mOldSS = new ServiceState(this.mSS);
        updateDataServiceState();
        if (isVoiceRegStateChanged && this.mDefaultPhone.getServiceStateTracker() != null) {
            this.mDefaultPhone.getServiceStateTracker().onImsServiceStateChanged();
        }
    }

    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public boolean getCallForwardingIndicator() {
        boolean cf = false;
        IccRecords r = getIccRecords();
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            r = this.mDefaultPhone.getIccRecords();
        }
        boolean z = false;
        if (r != null) {
            cf = r.getVoiceCallForwardingFlag() == 1;
        }
        if (!cf) {
            if (getCallForwardingPreference() && getSubscriberId() != null && getSubscriberId().equals(getVmSimImsi())) {
                z = true;
            }
            cf = z;
        }
        Rlog.d(LOG_TAG, "getCallForwardingIndicator getPhoneId=" + getPhoneId() + ", cf=" + cf);
        return cf;
    }

    public void updateCallForwardStatus() {
        Rlog.d(LOG_TAG, "updateCallForwardStatus");
        IccRecords r = getIccRecords();
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            r = this.mDefaultPhone.getIccRecords();
        }
        if (r != null) {
            Rlog.d(LOG_TAG, "Callforwarding info is present on sim");
            notifyCallForwardingIndicator();
            return;
        }
        sendMessage(obtainMessage(46));
    }

    public ImsExternalCallTracker getExternalCallTracker() {
        return this.mExternalCallTracker;
    }

    public List<? extends ImsPhoneMmiCode> getPendingMmiCodes() {
        return this.mPendingMMIs;
    }

    public void acceptCall(int videoState) throws CallStateException {
        this.mCT.acceptCall(videoState);
    }

    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    public void switchHoldingAndActive() throws CallStateException {
        this.mCT.switchWaitingOrHoldingAndActive();
    }

    public boolean canConference() {
        return this.mCT.canConference();
    }

    public boolean canDial() {
        return this.mCT.canDial();
    }

    public void conference() {
        this.mCT.conference();
    }

    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    public boolean canTransfer() {
        return this.mCT.canTransfer();
    }

    public void explicitCallTransfer() {
        Rlog.d(LOG_TAG, "explicitCallTransfer by the button");
        processECT();
    }

    public ImsPhoneCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    public ImsPhoneCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    public ImsPhoneCall getRingingCall() {
        return this.mCT.mRingingCall;
    }

    public boolean isImsAvailable() {
        return this.mCT.isImsServiceReady();
    }

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: rejectCall");
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "reject failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: hangupWaitingOrBackground");
            try {
                this.mCT.hangup(getBackgroundCall());
            } catch (CallStateException e2) {
                Rlog.d(LOG_TAG, "hangup failed", e2);
            }
        }
        return true;
    }

    private void sendUssdResponse(String ussdRequest, CharSequence message, int returnCode, ResultReceiver wrappedCallback) {
        UssdResponse response = new UssdResponse(ussdRequest, message);
        Bundle returnData = new Bundle();
        returnData.putParcelable("USSD_RESPONSE", response);
        wrappedCallback.send(returnCode, returnData);
    }

    public boolean handleUssdRequest(String ussdRequest, ResultReceiver wrappedCallback) throws CallStateException {
        if (this.mPendingMMIs.size() > 0) {
            logi("handleUssdRequest: queue full: " + Rlog.pii(LOG_TAG, ussdRequest));
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return true;
        }
        try {
            dialInternal(ussdRequest, new ImsDialArgs.Builder().build(), wrappedCallback);
        } catch (CallStateException cse) {
            if (!Phone.CS_FALLBACK.equals(cse.getMessage())) {
                Rlog.w(LOG_TAG, "Could not execute USSD " + cse);
                sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            } else {
                throw cse;
            }
        } catch (Exception e) {
            Rlog.w(LOG_TAG, "Could not execute USSD " + e);
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return false;
        }
        return true;
    }

    private boolean handleCallWaitingIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        ImsPhoneCall call = getForegroundCall();
        if (len > 1) {
            try {
                int callIndex = dialString.charAt(1) - '0';
                if (callIndex >= 1 && callIndex <= 7) {
                    Rlog.d(LOG_TAG, "MmiCode 1: hangupConnectionByIndex " + callIndex);
                    if (HwTelephonyFactory.getHwImsPhoneCallTrackerMgr() != null) {
                        HwTelephonyFactory.getHwImsPhoneCallTrackerMgr().hangupConnectionByIndex(call, callIndex, this.mCT);
                    }
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "hangup failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.HANGUP);
            }
        } else if (call.getState() != Call.State.IDLE) {
            logd("MmiCode 1: hangup foreground");
            this.mCT.hangup(call);
        } else {
            logd("MmiCode 1: switchWaitingOrHoldingAndActive");
            this.mCT.switchWaitingOrHoldingAndActive();
        }
        return true;
    }

    private boolean handleCallHoldIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        if (len > 1) {
            logd("separate not supported");
            notifySuppServiceFailed(PhoneInternalInterface.SuppService.SEPARATE);
        } else {
            try {
                if (getRingingCall().getState() != Call.State.IDLE) {
                    logd("MmiCode 2: accept ringing call");
                    this.mCT.acceptCall(2);
                } else {
                    logd("MmiCode 2: switchWaitingOrHoldingAndActive");
                    this.mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "switch failed", e);
                notifySuppServiceFailed(PhoneInternalInterface.SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        logd("MmiCode 3: merge calls");
        conference();
        return true;
    }

    private boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        Rlog.d(LOG_TAG, "MmiCode 4: support Ims explicit call transfer");
        processECT();
        return true;
    }

    private void processECT() {
        Rlog.d(LOG_TAG, "processECT");
        if (this.mCT != null) {
            try {
                ImsUt imsUt = this.mCT.getUtInterface();
                if (imsUt instanceof ImsUt) {
                    HwImsUtManager.processECT(this.mPhoneId, imsUt);
                }
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "get UtInterface occures exception");
            }
        }
    }

    private boolean handleCcbsIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        logi("MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(PhoneInternalInterface.SuppService.UNKNOWN);
        return true;
    }

    public void notifySuppSvcNotification(SuppServiceNotification suppSvc) {
        logd("notifySuppSvcNotification: suppSvc = " + suppSvc);
        this.mSsnRegistrants.notifyRegistrants(new AsyncResult(null, suppSvc, null));
    }

    public void notifyECTFailed(PhoneInternalInterface.SuppService code) {
        Rlog.d(LOG_TAG, "notifySuppServiceFailed: code = " + code);
        notifySuppServiceFailed(code);
    }

    public boolean handleInCallMmiCommands(String dialString) {
        if (!isInCall() || TextUtils.isEmpty(dialString)) {
            return false;
        }
        boolean result = false;
        switch (dialString.charAt(0)) {
            case '0':
                result = handleCallDeflectionIncallSupplementaryService(dialString);
                break;
            case '1':
                result = handleCallWaitingIncallSupplementaryService(dialString);
                break;
            case '2':
                result = handleCallHoldIncallSupplementaryService(dialString);
                break;
            case '3':
                result = handleMultipartyIncallSupplementaryService(dialString);
                break;
            case '4':
                result = handleEctIncallSupplementaryService(dialString);
                break;
            case '5':
                result = handleCcbsIncallSupplementaryService(dialString);
                break;
        }
        return result;
    }

    public boolean isInCall() {
        return getForegroundCall().getState().isAlive() || getBackgroundCall().getState().isAlive() || getRingingCall().getState().isAlive();
    }

    public boolean isInEcm() {
        return this.mDefaultPhone.isInEcm();
    }

    public void setIsInEcm(boolean isInEcm) {
        this.mDefaultPhone.setIsInEcm(isInEcm);
    }

    public void notifyNewRingingConnection(com.android.internal.telephony.Connection c) {
        this.mDefaultPhone.notifyNewRingingConnectionP(c);
    }

    /* access modifiers changed from: package-private */
    public void notifyUnknownConnection(com.android.internal.telephony.Connection c) {
        this.mDefaultPhone.notifyUnknownConnectionP(c);
    }

    public void notifyForVideoCapabilityChanged(boolean isVideoCapable) {
        this.mIsVideoCapable = isVideoCapable;
        this.mDefaultPhone.notifyForVideoCapabilityChanged(isVideoCapable);
    }

    public com.android.internal.telephony.Connection dial(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        return dialInternal(dialString, dialArgs, null);
    }

    private com.android.internal.telephony.Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs, ResultReceiver wrappedCallback) throws CallStateException {
        ImsDialArgs.Builder imsDialArgsBuilder;
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        if (!(dialArgs instanceof ImsDialArgs)) {
            imsDialArgsBuilder = ImsDialArgs.Builder.from(dialArgs);
        } else {
            imsDialArgsBuilder = ImsDialArgs.Builder.from((ImsDialArgs) dialArgs);
        }
        imsDialArgsBuilder.setClirMode(this.mCT.getClirMode());
        if (HuaweiTelephonyConfigs.isHisiPlatform() && this.mDefaultPhone.getPhoneType() == 2) {
            return this.mCT.dial(dialString, imsDialArgsBuilder.build());
        }
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, wrappedCallback);
        logd("dialInternal: dialing w/ mmi '" + mmi + "'...");
        if (mmi != null) {
            HwChrServiceManager hwChrServiceManager = HwTelephonyFactory.getHwChrServiceManager();
            if (hwChrServiceManager != null) {
                hwChrServiceManager.reportCallException("Telephony", getSubId(), 0, "AP_FLOW_SUC");
            }
        }
        if (mmi == null) {
            return this.mCT.dial(dialString, imsDialArgsBuilder.build());
        }
        if (mmi.isTemporaryModeCLIR()) {
            imsDialArgsBuilder.setClirMode(mmi.getCLIRMode());
            return this.mCT.dial(mmi.getDialingNumber(), imsDialArgsBuilder.build());
        } else if (mmi.isSupportedOverImsPhone()) {
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            try {
                mmi.processCode();
            } catch (CallStateException cse) {
                if (Phone.CS_FALLBACK.equals(cse.getMessage())) {
                    logi("dialInternal: fallback to GSM required.");
                    this.mPendingMMIs.remove(mmi);
                    throw cse;
                }
            }
            return null;
        } else {
            logi("dialInternal: USSD not supported by IMS; fallback to CS.");
            throw new CallStateException(Phone.CS_FALLBACK);
        }
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.getState() == PhoneConstants.State.OFFHOOK) {
            this.mCT.sendDtmf(c, null);
        }
    }

    public void startDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c) || (c >= 'A' && c <= 'D')) {
            this.mCT.startDtmf(c);
            return;
        }
        loge("startDtmf called with invalid character '" + c + "'");
    }

    public void stopDtmf() {
        this.mCT.stopDtmf();
    }

    public void notifyIncomingRing() {
        logd("notifyIncomingRing");
        sendMessage(obtainMessage(14, new AsyncResult(null, null, null)));
    }

    public void setMute(boolean muted) {
        this.mCT.setMute(muted);
    }

    public void setTTYMode(int ttyMode, Message onComplete) {
        this.mCT.setTtyMode(ttyMode);
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        this.mCT.setUiTTYMode(uiTtyMode, onComplete);
    }

    public boolean getMute() {
        return this.mCT.getMute();
    }

    public PhoneConstants.State getState() {
        return this.mCT.getState();
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case 0:
            case 1:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    private int getConditionFromCFReason(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            default:
                return -1;
        }
    }

    private int getCFReasonFromCondition(int condition) {
        switch (condition) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            default:
                return 3;
        }
    }

    private int getActionFromCFAction(int action) {
        switch (action) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                return -1;
        }
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        logd("getCLIR");
        try {
            this.mCT.getUtInterface().queryCLIR(obtainMessage(52, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setOutgoingCallerIdDisplay(int clirMode, Message onComplete) {
        logd("setCLIR action= " + clirMode);
        try {
            this.mCT.getUtInterface().updateCLIR(clirMode, obtainMessage(51, clirMode, 0, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        getCallForwardForServiceClass(commandInterfaceCFReason, 0, onComplete);
    }

    public void getCallForwardForServiceClass(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        logd("getCallForwardingOption reason=" + commandInterfaceCFReason);
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            logd("requesting call forwarding query.");
            Message resp = obtainMessage(13, onComplete);
            try {
                ImsUt imsUt = this.mCT.getUtInterface();
                if (imsUt instanceof ImsUt) {
                    int i = serviceClass;
                    HwImsUtManager.queryCallForwardForServiceClass(getConditionFromCFReason(commandInterfaceCFReason), null, i, resp, this.mPhoneId, imsUt);
                }
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, 1, timerSeconds, onComplete);
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int serviceClass, int timerSeconds, Message onComplete) {
        int i = commandInterfaceCFReason;
        int i2 = serviceClass;
        Message message = onComplete;
        logd("setCallForwardingOption action=" + commandInterfaceCFAction + ", reason=" + i + " serviceClass=" + i2);
        if (!isValidCommandInterfaceCFAction(commandInterfaceCFAction) || !isValidCommandInterfaceCFReason(i)) {
            String str = dialingNumber;
            if (message != null) {
                sendErrorResponse(message);
                return;
            }
            return;
        }
        String str2 = dialingNumber;
        try {
            this.mCT.getUtInterface().updateCallForward(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(i), str2, i2, timerSeconds, obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new Cf(str2, GsmMmiCode.isVoiceUnconditionalForwarding(i, i2), message)));
        } catch (ImsException e) {
            sendErrorResponse(message, e);
        }
    }

    public void getCallWaiting(Message onComplete) {
        logd("getCallWaiting");
        try {
            this.mCT.getUtInterface().queryCallWaiting(obtainMessage(50, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        int serviceClass = 1;
        if (CALL_WAITING_CLASS_NONE) {
            serviceClass = 0;
        }
        Rlog.d(LOG_TAG, "setCallWaiting enable = " + enable + " serviceClass = " + serviceClass);
        setCallWaiting(enable, serviceClass, onComplete);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message onComplete) {
        logd("setCallWaiting enable=" + enable);
        try {
            this.mCT.getUtInterface().updateCallWaiting(enable, serviceClass, obtainMessage(49, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    private int getCBTypeFromFacility(String facility) {
        if (CommandsInterface.CB_FACILITY_BAOC.equals(facility)) {
            return 2;
        }
        if (CommandsInterface.CB_FACILITY_BAOIC.equals(facility)) {
            return 3;
        }
        if (CommandsInterface.CB_FACILITY_BAOICxH.equals(facility)) {
            return 4;
        }
        if (CommandsInterface.CB_FACILITY_BAIC.equals(facility)) {
            return 1;
        }
        if (CommandsInterface.CB_FACILITY_BAICr.equals(facility)) {
            return 5;
        }
        if (CommandsInterface.CB_FACILITY_BA_ALL.equals(facility)) {
            return 7;
        }
        if (CommandsInterface.CB_FACILITY_BA_MO.equals(facility)) {
            return 8;
        }
        if (CommandsInterface.CB_FACILITY_BA_MT.equals(facility)) {
            return 9;
        }
        return 0;
    }

    public void getCallBarring(String facility, Message onComplete) {
        getCallBarring(facility, onComplete, 0);
    }

    public void getCallBarring(String facility, Message onComplete, int serviceClass) {
        getCallBarring(facility, "", onComplete, serviceClass);
    }

    public void getCallBarring(String facility, String password, Message onComplete, int serviceClass) {
        logd("getCallBarring facility=" + facility + ", serviceClass = " + serviceClass);
        try {
            this.mCT.getUtInterface().queryCallBarring(getCBTypeFromFacility(facility), obtainMessage(48, onComplete), serviceClass);
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete) {
        setCallBarring(facility, lockState, password, onComplete, 0);
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete, int serviceClass) {
        boolean z;
        boolean z2 = lockState;
        Message message = onComplete;
        StringBuilder sb = new StringBuilder();
        sb.append("setCallBarring facility=");
        sb.append(facility);
        sb.append(", lockState=");
        sb.append(z2);
        sb.append(", serviceClass = ");
        int i = serviceClass;
        sb.append(i);
        logd(sb.toString());
        Message resp = obtainMessage(47, message);
        if (z2) {
            z = true;
        } else {
            z = false;
        }
        boolean z3 = z;
        try {
            ImsUt imsUt = this.mCT.getUtInterface();
            if (imsUt instanceof ImsUt) {
                String str = password;
                boolean z4 = z2;
                int i2 = i;
                Message message2 = resp;
                HwImsUtManager.updateCallBarringOption(str, getCBTypeFromFacility(facility), z4, i2, message2, null, this.mPhoneId, imsUt);
            }
        } catch (ImsException e) {
            sendErrorResponse(message, e);
        }
    }

    public void sendUssdResponse(String ussdMessge) {
        logd("sendUssdResponse");
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromUssdUserInput(ussdMessge, this);
        this.mPendingMMIs.add(mmi);
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        mmi.sendUssd(ussdMessge);
    }

    public void sendUSSD(String ussdString, Message response) {
        this.mCT.sendUSSD(ussdString, response);
    }

    public void cancelUSSD() {
        this.mCT.cancelUSSD();
    }

    private void sendErrorResponse(Message onComplete) {
        logd("sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    @VisibleForTesting
    public void sendErrorResponse(Message onComplete, Throwable e) {
        logd("sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    private CommandException getCommandException(int code, String errorString) {
        logd("getCommandException code= " + code + ", errorString= " + errorString);
        CommandException.Error error = CommandException.Error.GENERIC_FAILURE;
        if (code == 241) {
            error = CommandException.Error.FDN_CHECK_FAILURE;
        } else if (code != 831) {
            switch (code) {
                case 801:
                    error = CommandException.Error.REQUEST_NOT_SUPPORTED;
                    break;
                case 802:
                    error = CommandException.Error.RADIO_NOT_AVAILABLE;
                    break;
                default:
                    switch (code) {
                        case 821:
                            error = CommandException.Error.PASSWORD_INCORRECT;
                            break;
                        case 822:
                            error = CommandException.Error.SS_MODIFIED_TO_DIAL;
                            break;
                        case 823:
                            error = CommandException.Error.SS_MODIFIED_TO_USSD;
                            break;
                        case 824:
                            error = CommandException.Error.SS_MODIFIED_TO_SS;
                            break;
                        case 825:
                            error = CommandException.Error.SS_MODIFIED_TO_DIAL_VIDEO;
                            break;
                    }
            }
        } else {
            error = CommandException.Error.UT_NO_CONNECTION;
        }
        return new CommandException(error, errorString);
    }

    private CommandException getCommandException(Throwable e) {
        if (e instanceof ImsException) {
            return getCommandException(((ImsException) e).getCode(), e.getMessage());
        }
        if (e instanceof CommandException) {
            Rlog.d(LOG_TAG, "e instanceof CommandException  : " + e);
            return new CommandException(((CommandException) e).getCommandError());
        }
        logd("getCommandException generic failure");
        return new CommandException(CommandException.Error.GENERIC_FAILURE);
    }

    private void onNetworkInitiatedUssd(ImsPhoneMmiCode mmi) {
        logd("onNetworkInitiatedUssd");
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    /* access modifiers changed from: package-private */
    public void onIncomingUSSD(int ussdMode, String ussdMessage) {
        logd("onIncomingUSSD ussdMode=" + ussdMode);
        boolean isUssdError = false;
        boolean isUssdRequest = ussdMode == 1;
        if (!(ussdMode == 0 || ussdMode == 1)) {
            isUssdError = true;
        }
        ImsPhoneMmiCode found = null;
        int i = 0;
        int s = this.mPendingMMIs.size();
        while (true) {
            if (i >= s) {
                break;
            } else if (this.mPendingMMIs.get(i).isPendingUSSD()) {
                found = this.mPendingMMIs.get(i);
                break;
            } else {
                i++;
            }
        }
        if (found != null) {
            if (isUssdError) {
                found.onUssdFinishedError();
            } else {
                found.onUssdFinished(ussdMessage, isUssdRequest);
            }
        } else if (!isUssdError && ussdMessage != null) {
            onNetworkInitiatedUssd(ImsPhoneMmiCode.newNetworkInitiatedUssd(ussdMessage, isUssdRequest, this));
        }
    }

    public void onMMIDone(ImsPhoneMmiCode mmi) {
        logd("onMMIDone: mmi=" + mmi);
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest() || mmi.isSsInfo()) {
            ResultReceiver receiverCallback = mmi.getUssdCallbackReceiver();
            if (receiverCallback != null) {
                sendUssdResponse(mmi.getDialString(), mmi.getMessage(), mmi.getState() == MmiCode.State.COMPLETE ? 100 : -1, receiverCallback);
                return;
            }
            logv("onMMIDone: notifyRegistrants");
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        }
    }

    /* access modifiers changed from: package-private */
    public void onMMIDone(ImsPhoneMmiCode mmi, Exception e) {
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest()) {
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, e));
        }
    }

    public ArrayList<com.android.internal.telephony.Connection> getHandoverConnection() {
        ArrayList<com.android.internal.telephony.Connection> connList = new ArrayList<>();
        connList.addAll(getForegroundCall().mConnections);
        connList.addAll(getBackgroundCall().mConnections);
        connList.addAll(getRingingCall().mConnections);
        if (connList.size() > 0) {
            return connList;
        }
        return null;
    }

    public void notifySrvccState(Call.SrvccState state) {
        this.mCT.notifySrvccState(state);
    }

    /* access modifiers changed from: package-private */
    public void initiateSilentRedial() {
        this.mSilentRedialRegistrants.notifyRegistrants(new AsyncResult(null, this.mLastDialString, null));
    }

    public void registerForSilentRedial(Handler h, int what, Object obj) {
        this.mSilentRedialRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSilentRedial(Handler h) {
        this.mSilentRedialRegistrants.remove(h);
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
        if (this.mSsnRegistrants.size() == 1) {
            this.mDefaultPhone.mCi.setSuppServiceNotifications(true, null);
        }
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
    }

    public int getSubId() {
        return this.mDefaultPhone.getSubId();
    }

    public String getSubscriberId() {
        IccRecords r = getIccRecords();
        if (r != null) {
            return r.getIMSI();
        }
        return null;
    }

    public int getPhoneId() {
        return this.mDefaultPhone.getPhoneId();
    }

    private CallForwardInfo getCallForwardInfo(ImsCallForwardInfo info) {
        CallForwardInfo cfInfo = new CallForwardInfo();
        cfInfo.status = info.getStatus();
        cfInfo.reason = getCFReasonFromCondition(info.getCondition());
        cfInfo.serviceClass = 1;
        cfInfo.toa = info.getToA();
        cfInfo.number = info.getNumber();
        cfInfo.timeSeconds = info.getTimeSeconds();
        cfInfo.startHour = info.mStartHour;
        cfInfo.startMinute = info.mStartMinute;
        cfInfo.endHour = info.mEndHour;
        cfInfo.endMinute = info.mEndMinute;
        return cfInfo;
    }

    public CallForwardInfo[] handleCfQueryResult(ImsCallForwardInfo[] infos) {
        CallForwardInfo[] cfInfos = null;
        if (!(infos == null || infos.length == 0)) {
            cfInfos = new CallForwardInfo[infos.length];
        }
        IccRecords r = this.mDefaultPhone.getIccRecords();
        if (infos != null && infos.length != 0) {
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if (infos[i].getCondition() == 0 && r != null) {
                    setVoiceCallForwardingFlag(r, 1, infos[i].getStatus() == 1, infos[i].getNumber());
                }
                cfInfos[i] = getCallForwardInfo(infos[i]);
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
        }
        return cfInfos;
    }

    private int[] handleCbQueryResult(ImsSsInfo[] infos) {
        int[] cbInfos = {0};
        if (infos[0].getStatus() == 1) {
            cbInfos[0] = 1;
        }
        return cbInfos;
    }

    private int[] handleCwQueryResult(ImsSsInfo[] infos) {
        int[] cwInfos = new int[2];
        cwInfos[0] = 0;
        if (infos[0].getStatus() == 1) {
            cwInfos[0] = 1;
            cwInfos[1] = 1;
        }
        return cwInfos;
    }

    private void sendResponse(Message onComplete, Object result, Throwable e) {
        if (onComplete != null) {
            if (e != null) {
                AsyncResult.forMessage(onComplete, result, getCommandException(e));
            } else {
                AsyncResult.forMessage(onComplete, result, null);
            }
            onComplete.sendToTarget();
        }
    }

    private void updateDataServiceState() {
        if (this.mSS != null && this.mDefaultPhone.getServiceStateTracker() != null && this.mDefaultPhone.getServiceStateTracker().mSS != null) {
            ServiceState ss = this.mDefaultPhone.getServiceStateTracker().mSS;
            this.mSS.setDataRegState(ss.getDataRegState());
            this.mSS.setRilDataRadioTechnology(ss.getRilDataRadioTechnology());
            logd("updateDataServiceState: defSs = " + ss + " imsSs = " + this.mSS);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v21, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v21, resolved type: java.lang.Throwable} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void handleMessage(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        Rlog.d(LOG_TAG, "handleMessage what=" + msg.what);
        if (!this.mHwImsPhoneEx.beforeHandleMessage(msg)) {
            int i = msg.what;
            switch (i) {
                case 12:
                    IccRecords r = this.mDefaultPhone.getIccRecords();
                    Cf cf = (Cf) ar.userObj;
                    if (cf.mIsCfu && ar.exception == null && r != null) {
                        setVoiceCallForwardingFlag(r, 1, msg.arg1 == 1, cf.mSetCfNumber);
                    }
                    sendResponse(cf.mOnComplete, null, ar.exception);
                    break;
                case 13:
                    if (ar.exception == null && (ar.result instanceof ImsCallForwardInfo[])) {
                        sendResponse((Message) ar.userObj, handleCfQueryResult((ImsCallForwardInfo[]) ar.result), ar.exception);
                        break;
                    } else {
                        sendResponse((Message) ar.userObj, ar.result, ar.exception);
                        break;
                    }
                    break;
                default:
                    switch (i) {
                        case 46:
                            boolean cfEnabled = getCallForwardingPreference();
                            Rlog.d(LOG_TAG, "Callforwarding is " + cfEnabled);
                            notifyCallForwardingIndicator();
                            break;
                        case 47:
                        case 49:
                            break;
                        case 48:
                        case 50:
                            Throwable exception = null;
                            if (ar.userObj != null && (ar.userObj instanceof Message)) {
                                Message tempMsg = (Message) ar.userObj;
                                if (tempMsg.obj != null && (tempMsg.obj instanceof Throwable)) {
                                    exception = tempMsg.obj;
                                }
                            }
                            if (ar.exception != null) {
                                exception = ar.exception;
                            }
                            if (ar.exception == null && (ar.result instanceof ImsSsInfo[])) {
                                int[] ssInfos = null;
                                if (msg.what == 48) {
                                    ssInfos = handleCbQueryResult((ImsSsInfo[]) ar.result);
                                } else if (msg.what == 50) {
                                    ssInfos = handleCwQueryResult((ImsSsInfo[]) ar.result);
                                }
                                sendResponse((Message) ar.userObj, ssInfos, exception);
                                break;
                            } else {
                                sendResponse((Message) ar.userObj, ar.result, exception);
                                break;
                            }
                            break;
                        case 51:
                            if (ar.exception == null) {
                                saveClirSetting(msg.arg1);
                                break;
                            }
                            break;
                        case 52:
                            if (!(ar.result instanceof Bundle)) {
                                sendResponse((Message) ar.userObj, ar.result, ar.exception);
                                break;
                            } else {
                                int[] clirInfo = null;
                                Bundle ssInfo = (Bundle) ar.result;
                                if (ssInfo != null) {
                                    clirInfo = ssInfo.getIntArray(ImsPhoneMmiCode.UT_BUNDLE_KEY_CLIR);
                                }
                                sendResponse((Message) ar.userObj, clirInfo, ar.exception);
                                break;
                            }
                        case 53:
                            logd("EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED");
                            updateDataServiceState();
                            break;
                        case 54:
                            ServiceState newServiceState = (ServiceState) ((AsyncResult) msg.obj).result;
                            if (this.mRoaming != newServiceState.getRoaming()) {
                                logd("Roaming state changed");
                                updateRoamingState(newServiceState.getRoaming());
                                break;
                            }
                            break;
                        case 55:
                            logd("Voice call ended. Handle pending updateRoamingState.");
                            this.mCT.unregisterForVoiceCallEnded(this);
                            boolean newRoaming = getCurrentRoaming();
                            if (this.mRoaming != newRoaming) {
                                updateRoamingState(newRoaming);
                                break;
                            }
                            break;
                        default:
                            super.handleMessage(msg);
                            break;
                    }
                    sendResponse((Message) ar.userObj, null, ar.exception);
                    break;
            }
        }
    }

    @VisibleForTesting
    public ImsEcbmStateListener getImsEcbmStateListener() {
        return this.mImsEcbmStateListener;
    }

    public boolean isInEmergencyCall() {
        return this.mCT.isInEmergencyCall();
    }

    private void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", isInEcm());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManager.broadcastStickyIntent(intent, -1);
        logd("sendEmergencyCallbackModeChange: isInEcm=" + isInEcm());
    }

    public void exitEmergencyCallbackMode() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        logd("exitEmergencyCallbackMode()");
        try {
            this.mCT.getEcbmInterface().exitEmergencyCallbackMode();
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void handleEnterEmergencyCallbackMode() {
        logd("handleEnterEmergencyCallbackMode,mIsPhoneInEcmState= " + isInEcm());
        if (!isInEcm()) {
            setIsInEcm(true);
            sendEmergencyCallbackModeChange();
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            this.mWakeLock.acquire();
        }
    }

    /* access modifiers changed from: protected */
    public void handleExitEmergencyCallbackMode() {
        logd("handleExitEmergencyCallbackMode: mIsPhoneInEcmState = " + isInEcm());
        if (isInEcm()) {
            setIsInEcm(false);
        }
        removeCallbacks(this.mExitEcmRunnable);
        if (this.mEcmExitRespRegistrant != null) {
            this.mEcmExitRespRegistrant.notifyResult(Boolean.TRUE);
        }
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        sendEmergencyCallbackModeChange();
        ((GsmCdmaPhone) this.mDefaultPhone).notifyEmergencyCallRegistrants(false);
    }

    /* access modifiers changed from: package-private */
    public void handleTimerInEmergencyCallbackMode(int action) {
        switch (action) {
            case 0:
                postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
                ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.FALSE);
                return;
            case 1:
                removeCallbacks(this.mExitEcmRunnable);
                ((GsmCdmaPhone) this.mDefaultPhone).notifyEcbmTimerReset(Boolean.TRUE);
                return;
            default:
                loge("handleTimerInEmergencyCallbackMode, unsupported action " + action);
                return;
        }
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    public void onFeatureCapabilityChanged() {
        this.mDefaultPhone.getServiceStateTracker().onImsCapabilityChanged();
    }

    public boolean isVolteEnabled() {
        return this.mCT.isVolteEnabled();
    }

    public boolean isWifiCallingEnabled() {
        return this.mCT.isVowifiEnabled();
    }

    public boolean isVideoEnabled() {
        return this.mCT.isVideoCallEnabled();
    }

    public int getImsRegistrationTech() {
        return this.mCT.getImsRegistrationTech();
    }

    public Phone getDefaultPhone() {
        return this.mDefaultPhone;
    }

    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    public void setImsRegistered(boolean value) {
        this.mImsRegistered = value;
    }

    public void callEndCleanupHandOverCallIfAny() {
        this.mCT.callEndCleanupHandOverCallIfAny();
    }

    public void processDisconnectReason(ImsReasonInfo imsReasonInfo) {
        if (imsReasonInfo.mCode == 1000 && imsReasonInfo.mExtraMessage != null && ImsManager.getInstance(this.mContext, this.mPhoneId).isWfcEnabledByUser()) {
            processWfcDisconnectForNotification(imsReasonInfo);
        }
    }

    private void processWfcDisconnectForNotification(ImsReasonInfo imsReasonInfo) {
        PersistableBundle pb;
        CarrierConfigManager configManager;
        char c;
        ImsReasonInfo imsReasonInfo2 = imsReasonInfo;
        CarrierConfigManager configManager2 = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager2 == null) {
            loge("processDisconnectReason: CarrierConfigManager is not ready");
            return;
        }
        PersistableBundle pb2 = configManager2.getConfigForSubId(getSubId());
        if (pb2 == null) {
            loge("processDisconnectReason: no config for subId " + getSubId());
            return;
        }
        String[] wfcOperatorErrorCodes = pb2.getStringArray("wfc_operator_error_codes_string_array");
        if (wfcOperatorErrorCodes != null) {
            String[] wfcOperatorErrorAlertMessages = this.mContext.getResources().getStringArray(17236090);
            String[] wfcOperatorErrorNotificationMessages = this.mContext.getResources().getStringArray(17236091);
            char c2 = 0;
            int i = 0;
            while (true) {
                if (i >= wfcOperatorErrorCodes.length) {
                    PersistableBundle persistableBundle = pb2;
                    break;
                }
                String[] codes = wfcOperatorErrorCodes[i].split("\\|");
                if (codes.length != 2) {
                    loge("Invalid carrier config: " + wfcOperatorErrorCodes[i]);
                } else if (imsReasonInfo2.mExtraMessage.startsWith(codes[c2])) {
                    int codeStringLength = codes[c2].length();
                    if (!Character.isLetterOrDigit(codes[c2].charAt(codeStringLength - 1)) || imsReasonInfo2.mExtraMessage.length() <= codeStringLength || !Character.isLetterOrDigit(imsReasonInfo2.mExtraMessage.charAt(codeStringLength))) {
                        CharSequence title = this.mContext.getText(17041356);
                        int idx = Integer.parseInt(codes[1]);
                        if (idx < 0 || idx >= wfcOperatorErrorAlertMessages.length) {
                            configManager = configManager2;
                            pb = pb2;
                            c = c2;
                        } else if (idx >= wfcOperatorErrorNotificationMessages.length) {
                            configManager = configManager2;
                            pb = pb2;
                            c = c2;
                        } else {
                            String messageAlert = imsReasonInfo2.mExtraMessage;
                            String messageNotification = imsReasonInfo2.mExtraMessage;
                            if (!wfcOperatorErrorAlertMessages[idx].isEmpty()) {
                                CarrierConfigManager carrierConfigManager = configManager2;
                                PersistableBundle persistableBundle2 = pb2;
                                messageAlert = String.format(wfcOperatorErrorAlertMessages[idx], new Object[]{imsReasonInfo2.mExtraMessage});
                            } else {
                                PersistableBundle persistableBundle3 = pb2;
                            }
                            if (!wfcOperatorErrorNotificationMessages[idx].isEmpty()) {
                                messageNotification = String.format(wfcOperatorErrorNotificationMessages[idx], new Object[]{imsReasonInfo2.mExtraMessage});
                            }
                            Intent intent = new Intent("com.android.ims.REGISTRATION_ERROR");
                            intent.putExtra(Phone.EXTRA_KEY_ALERT_TITLE, title);
                            intent.putExtra(Phone.EXTRA_KEY_ALERT_MESSAGE, messageAlert);
                            intent.putExtra(Phone.EXTRA_KEY_NOTIFICATION_MESSAGE, messageNotification);
                            this.mContext.sendOrderedBroadcast(intent, null, this.mResultReceiver, null, -1, null, null);
                        }
                        loge("Invalid index: " + wfcOperatorErrorCodes[i]);
                        i++;
                        c2 = c;
                        configManager2 = configManager;
                        pb2 = pb;
                    }
                }
                configManager = configManager2;
                pb = pb2;
                c = c2;
                i++;
                c2 = c;
                configManager2 = configManager;
                pb2 = pb;
            }
        }
    }

    public boolean isUtEnabled() {
        return this.mCT.isUtEnabled();
    }

    public void sendEmergencyCallStateChange(boolean callActive) {
        this.mDefaultPhone.sendEmergencyCallStateChange(callActive);
    }

    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mDefaultPhone.setBroadcastEmergencyCallStateChanges(broadcast);
    }

    @VisibleForTesting
    public PowerManager.WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        return this.mCT.getVtDataUsage(perUidStats);
    }

    private void updateRoamingState(boolean newRoaming) {
        if (this.mCT.getState() == PhoneConstants.State.IDLE) {
            logd("updateRoamingState now: " + newRoaming);
            this.mRoaming = newRoaming;
            try {
                ((GsmCdmaPhone) this.mDefaultPhone).updateWfcMode(this.mContext, newRoaming, this.mPhoneId);
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "updateWfcMode occurs Exception");
            }
        } else {
            logd("updateRoamingState postponed: " + newRoaming);
            this.mCT.registerForVoiceCallEnded(this, 55, null);
        }
    }

    private boolean getCurrentRoaming() {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).isNetworkRoaming();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ImsPhone extends:");
        super.dump(fd, pw, args);
        pw.flush();
        pw.println("ImsPhone:");
        pw.println("  mDefaultPhone = " + this.mDefaultPhone);
        pw.println("  mPendingMMIs = " + this.mPendingMMIs);
        pw.println("  mPostDialHandler = " + this.mPostDialHandler);
        pw.println("  mSS = " + this.mSS);
        pw.println("  mWakeLock = " + this.mWakeLock);
        pw.println("  mIsPhoneInEcmState = " + isInEcm());
        pw.println("  mEcmExitRespRegistrant = " + this.mEcmExitRespRegistrant);
        pw.println("  mSilentRedialRegistrants = " + this.mSilentRedialRegistrants);
        pw.println("  mImsRegistered = " + this.mImsRegistered);
        pw.println("  mRoaming = " + this.mRoaming);
        pw.println("  mSsnRegistrants = " + this.mSsnRegistrants);
        pw.flush();
    }

    private void logi(String s) {
        Rlog.i(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }

    private void logv(String s) {
        Rlog.v(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhoneId + "] " + s);
    }

    public boolean isInCallHw() {
        return isInCall();
    }
}

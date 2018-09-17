package com.android.internal.telephony.imsphone;

import android.app.ActivityManager;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.UssdResponse;
import android.text.TextUtils;
import com.android.ims.ImsCallForwardInfo;
import com.android.ims.ImsEcbmStateListener;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsSsInfo;
import com.android.ims.ImsUtInterface;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.util.NotificationChannelController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ImsPhone extends ImsPhoneBase {
    static final int CANCEL_ECM_TIMER = 1;
    private static final boolean DBG = true;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final int EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED = 52;
    private static final int EVENT_GET_CALL_BARRING_DONE = 47;
    private static final int EVENT_GET_CALL_WAITING_DONE = 49;
    private static final int EVENT_GET_CLIR_DONE = 51;
    private static final int EVENT_SERVICE_STATE_CHANGED = 53;
    private static final int EVENT_SET_CALL_BARRING_DONE = 46;
    private static final int EVENT_SET_CALL_WAITING_DONE = 48;
    private static final int EVENT_SET_CLIR_DONE = 50;
    private static final int EVENT_VOICE_CALL_ENDED = 54;
    private static final String LOG_TAG = "ImsPhone";
    static final int RESTART_ECM_TIMER = 0;
    private static final boolean VDBG = false;
    ImsPhoneCallTracker mCT;
    private Uri[] mCurrentSubscriberUris;
    Phone mDefaultPhone;
    private Registrant mEcmExitRespRegistrant;
    private Runnable mExitEcmRunnable;
    ImsExternalCallTracker mExternalCallTracker;
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
    private WakeLock mWakeLock;

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

    protected void setCurrentSubscriberUris(Uri[] currentSubscriberUris) {
        this.mCurrentSubscriberUris = currentSubscriberUris;
    }

    public Uri[] getCurrentSubscriberUris() {
        return this.mCurrentSubscriberUris;
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone) {
        this(context, notifier, defaultPhone, false);
    }

    public ImsPhone(Context context, PhoneNotifier notifier, Phone defaultPhone, boolean unitTestMode) {
        super(LOG_TAG, context, notifier, unitTestMode);
        this.mPendingMMIs = new ArrayList();
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
                Rlog.d(ImsPhone.LOG_TAG, "onECBMEntered");
                ImsPhone.this.handleEnterEmergencyCallbackMode();
            }

            public void onECBMExited() {
                Rlog.d(ImsPhone.LOG_TAG, "onECBMExited");
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
                    String notificationTag = "wifi_calling";
                    ((NotificationManager) ImsPhone.this.mContext.getSystemService("notification")).notify("wifi_calling", 1, new Builder(ImsPhone.this.mContext).setSmallIcon(17301642).setContentTitle(title).setContentText(messageNotification).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(ImsPhone.this.mContext, 0, resultIntent, 134217728)).setStyle(new BigTextStyle().bigText(messageNotification)).setChannelId(NotificationChannelController.CHANNEL_ID_WFC).build());
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
            this.mDefaultPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 52, null);
        }
        updateDataServiceState();
        this.mDefaultPhone.registerForServiceStateChanged(this, 53, null);
        this.mHwImsPhone = new HwImsPhone(this.mCT);
    }

    public void dispose() {
        Rlog.d(LOG_TAG, "dispose");
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

    void setServiceState(int state) {
        this.mSS.setVoiceRegState(state);
        boolean hasChange = this.mSS.getVoiceRegState() != this.mOldSS.getVoiceRegState();
        Rlog.d(LOG_TAG, "hasChange:" + hasChange + "  mOldSS=" + this.mOldSS + "  mSS=" + this.mSS);
        if (hasChange) {
            this.mDefaultPhone.notifyServiceStateChangedP(this.mSS);
        }
        this.mOldSS = new ServiceState(this.mSS);
        updateDataServiceState();
    }

    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public boolean getCallForwardingIndicator() {
        boolean cf = false;
        IccRecords r = getIccRecords();
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            r = this.mDefaultPhone.getIccRecords();
        }
        if (r != null) {
            cf = r.getVoiceCallForwardingFlag() == 1;
        }
        if (!cf) {
            cf = (!getCallForwardingPreference() || getSubscriberId() == null) ? false : getSubscriberId().equals(getVmSimImsi());
        }
        Rlog.d(LOG_TAG, "getCallForwardingIndicator getPhoneId=" + getPhoneId() + ", cf=" + cf);
        return cf;
    }

    public void updateCallForwardStatus() {
        Rlog.d(LOG_TAG, "updateCallForwardStatus");
        IccRecords r = getIccRecords();
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            r = this.mDefaultPhone.getIccRecords();
        }
        if (r != null) {
            Rlog.d(LOG_TAG, "Callforwarding info is present on sim");
            notifyCallForwardingIndicator();
            return;
        }
        sendMessage(obtainMessage(45));
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

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 0: rejectCall");
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "reject failed", e);
                notifySuppServiceFailed(SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 0: hangupWaitingOrBackground");
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
            Rlog.i(LOG_TAG, "handleUssdRequest: queue full: " + Rlog.pii(LOG_TAG, ussdRequest));
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
            return true;
        }
        try {
            dialInternal(ussdRequest, 0, null, wrappedCallback);
        } catch (CallStateException cse) {
            if (Phone.CS_FALLBACK.equals(cse.getMessage())) {
                throw cse;
            }
            Rlog.w(LOG_TAG, "Could not execute USSD " + cse);
            sendUssdResponse(ussdRequest, null, -1, wrappedCallback);
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
                int callIndex = dialString.charAt(1) - 48;
                if (callIndex >= 1 && callIndex <= 7) {
                    Rlog.d(LOG_TAG, "MmiCode 1: hangupConnectionByIndex " + callIndex);
                    this.mCT.hangupConnectionByIndex(call, callIndex);
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "hangup failed", e);
                notifySuppServiceFailed(SuppService.HANGUP);
            }
        } else if (call.getState() != State.IDLE) {
            Rlog.d(LOG_TAG, "MmiCode 1: hangup foreground");
            this.mCT.hangup(call);
        } else {
            Rlog.d(LOG_TAG, "MmiCode 1: switchWaitingOrHoldingAndActive");
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
            Rlog.d(LOG_TAG, "separate not supported");
            notifySuppServiceFailed(SuppService.SEPARATE);
        } else {
            try {
                if (getRingingCall().getState() != State.IDLE) {
                    Rlog.d(LOG_TAG, "MmiCode 2: accept ringing call");
                    this.mCT.acceptCall(2);
                } else {
                    Rlog.d(LOG_TAG, "MmiCode 2: switchWaitingOrHoldingAndActive");
                    this.mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "switch failed", e);
                notifySuppServiceFailed(SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.d(LOG_TAG, "MmiCode 3: merge calls");
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
                ImsUtInterface ut = this.mCT.getUtInterface();
                if (ut != null) {
                    ut.processECT();
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
        Rlog.i(LOG_TAG, "MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(SuppService.UNKNOWN);
        return true;
    }

    public void notifySuppSvcNotification(SuppServiceNotification suppSvc) {
        Rlog.d(LOG_TAG, "notifySuppSvcNotification: suppSvc = " + suppSvc);
        this.mSsnRegistrants.notifyRegistrants(new AsyncResult(null, suppSvc, null));
    }

    public void notifyECTFailed(SuppService code) {
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
        State foregroundCallState = getForegroundCall().getState();
        State backgroundCallState = getBackgroundCall().getState();
        State ringingCallState = getRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
            return true;
        }
        return ringingCallState.isAlive();
    }

    public boolean isInEcm() {
        return this.mDefaultPhone.isInEcm();
    }

    public void setIsInEcm(boolean isInEcm) {
        this.mDefaultPhone.setIsInEcm(isInEcm);
    }

    public void notifyNewRingingConnection(Connection c) {
        this.mDefaultPhone.notifyNewRingingConnectionP(c);
    }

    void notifyUnknownConnection(Connection c) {
        this.mDefaultPhone.notifyUnknownConnectionP(c);
    }

    public void notifyForVideoCapabilityChanged(boolean isVideoCapable) {
        this.mIsVideoCapable = isVideoCapable;
        this.mDefaultPhone.notifyForVideoCapabilityChanged(isVideoCapable);
    }

    public Connection dial(String dialString, int videoState) throws CallStateException {
        return dialInternal(dialString, videoState, null, null);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        return dialInternal(dialString, videoState, intentExtras, null);
    }

    protected Connection dialInternal(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        return dialInternal(dialString, videoState, intentExtras, null);
    }

    private Connection dialInternal(String dialString, int videoState, Bundle intentExtras, ResultReceiver wrappedCallback) throws CallStateException {
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        if (this.mDefaultPhone.getPhoneType() == 2) {
            return this.mCT.dial(dialString, videoState, intentExtras);
        }
        ImsPhoneMmiCode mmi = ImsPhoneMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, wrappedCallback);
        Rlog.d(LOG_TAG, "dialInternal: dialing w/ mmi '" + mmi + "'...");
        if (mmi == null) {
            return this.mCT.dial(dialString, videoState, intentExtras);
        }
        if (mmi.isTemporaryModeCLIR()) {
            return this.mCT.dial(mmi.getDialingNumber(), mmi.getCLIRMode(), videoState, intentExtras);
        }
        if (mmi.isSupportedOverImsPhone()) {
            this.mPendingMMIs.add(mmi);
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            try {
                mmi.processCode();
            } catch (CallStateException cse) {
                if (Phone.CS_FALLBACK.equals(cse.getMessage())) {
                    Rlog.i(LOG_TAG, "dialInternal: fallback to GSM required.");
                    this.mPendingMMIs.remove(mmi);
                    throw cse;
                }
            }
            return null;
        }
        Rlog.i(LOG_TAG, "dialInternal: USSD not supported by IMS; fallback to CS.");
        throw new CallStateException(Phone.CS_FALLBACK);
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            Rlog.e(LOG_TAG, "sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.getState() == PhoneConstants.State.OFFHOOK) {
            this.mCT.sendDtmf(c, null);
        }
    }

    public void startDtmf(char c) {
        Object obj = 1;
        if (!PhoneNumberUtils.is12Key(c) && (c < 'A' || c > 'D')) {
            obj = null;
        }
        if (obj == null) {
            Rlog.e(LOG_TAG, "startDtmf called with invalid character '" + c + "'");
        } else {
            this.mCT.startDtmf(c);
        }
    }

    public void stopDtmf() {
        this.mCT.stopDtmf();
    }

    public void notifyIncomingRing() {
        Rlog.d(LOG_TAG, "notifyIncomingRing");
        sendMessage(obtainMessage(14, new AsyncResult(null, null, null)));
    }

    public void setMute(boolean muted) {
        this.mCT.setMute(muted);
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
        Rlog.d(LOG_TAG, "getCLIR");
        try {
            this.mCT.getUtInterface().queryCLIR(obtainMessage(51, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setOutgoingCallerIdDisplay(int clirMode, Message onComplete) {
        Rlog.d(LOG_TAG, "setCLIR action= " + clirMode);
        try {
            this.mCT.getUtInterface().updateCLIR(clirMode, obtainMessage(50, clirMode, 0, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        Rlog.d(LOG_TAG, "getCallForwardingOption reason=" + commandInterfaceCFReason);
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            Rlog.d(LOG_TAG, "requesting call forwarding query.");
            try {
                this.mCT.getUtInterface().queryCallForward(getConditionFromCFReason(commandInterfaceCFReason), null, obtainMessage(13, onComplete));
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
        Rlog.d(LOG_TAG, "setCallForwardingOption action=" + commandInterfaceCFAction + ", reason=" + commandInterfaceCFReason + " serviceClass=" + serviceClass);
        if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            int i;
            Cf cf = new Cf(dialingNumber, GsmMmiCode.isVoiceUnconditionalForwarding(commandInterfaceCFReason, serviceClass), onComplete);
            if (isCfEnable(commandInterfaceCFAction)) {
                i = 1;
            } else {
                i = 0;
            }
            try {
                this.mCT.getUtInterface().updateCallForward(getActionFromCFAction(commandInterfaceCFAction), getConditionFromCFReason(commandInterfaceCFReason), dialingNumber, serviceClass, timerSeconds, obtainMessage(12, i, 0, cf));
            } catch (ImsException e) {
                sendErrorResponse(onComplete, e);
            }
        } else if (onComplete != null) {
            sendErrorResponse(onComplete);
        }
    }

    public void getCallWaiting(Message onComplete) {
        Rlog.d(LOG_TAG, "getCallWaiting");
        try {
            this.mCT.getUtInterface().queryCallWaiting(obtainMessage(49, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        setCallWaiting(enable, 1, onComplete);
    }

    public void setCallWaiting(boolean enable, int serviceClass, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallWaiting enable=" + enable);
        try {
            this.mCT.getUtInterface().updateCallWaiting(enable, serviceClass, obtainMessage(48, onComplete));
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
        Rlog.d(LOG_TAG, "getCallBarring facility=" + facility);
        try {
            this.mCT.getUtInterface().queryCallBarring(getCBTypeFromFacility(facility), obtainMessage(47, onComplete));
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void setCallBarring(String facility, boolean lockState, String password, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallBarring facility=" + facility + ", lockState=" + lockState);
        Message resp = obtainMessage(46, onComplete);
        if (lockState) {
        }
        try {
            this.mCT.getUtInterface().updateCallBarringOption(password, getCBTypeFromFacility(facility), lockState, resp, null);
        } catch (ImsException e) {
            sendErrorResponse(onComplete, e);
        }
    }

    public void sendUssdResponse(String ussdMessge) {
        Rlog.d(LOG_TAG, "sendUssdResponse");
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
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    public void sendErrorResponse(Message onComplete, Throwable e) {
        Rlog.d(LOG_TAG, "sendErrorResponse");
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, getCommandException(e));
            onComplete.sendToTarget();
        }
    }

    private CommandException getCommandException(int code, String errorString) {
        Rlog.d(LOG_TAG, "getCommandException code= " + code + ", errorString= " + errorString);
        Error error = Error.GENERIC_FAILURE;
        switch (code) {
            case 801:
                error = Error.REQUEST_NOT_SUPPORTED;
                break;
            case 802:
                error = Error.RADIO_NOT_AVAILABLE;
                break;
            case 821:
                error = Error.PASSWORD_INCORRECT;
                break;
            case 831:
                error = Error.UT_NO_CONNECTION;
                break;
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
        Rlog.d(LOG_TAG, "getCommandException generic failure");
        return new CommandException(Error.GENERIC_FAILURE);
    }

    private void onNetworkInitiatedUssd(ImsPhoneMmiCode mmi) {
        Rlog.d(LOG_TAG, "onNetworkInitiatedUssd");
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    void onIncomingUSSD(int ussdMode, String ussdMessage) {
        Rlog.d(LOG_TAG, "onIncomingUSSD ussdMode=" + ussdMode);
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = ussdMode != 0 ? ussdMode != 1 : false;
        ImsPhoneMmiCode found = null;
        int s = this.mPendingMMIs.size();
        for (int i = 0; i < s; i++) {
            if (((ImsPhoneMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                found = (ImsPhoneMmiCode) this.mPendingMMIs.get(i);
                break;
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
        Rlog.d(LOG_TAG, "onMMIDone: mmi=" + mmi);
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest()) {
            ResultReceiver receiverCallback = mmi.getUssdCallbackReceiver();
            if (receiverCallback != null) {
                sendUssdResponse(mmi.getDialString(), mmi.getMessage(), mmi.getState() == MmiCode.State.COMPLETE ? 100 : -1, receiverCallback);
                return;
            }
            Rlog.v(LOG_TAG, "onMMIDone: notifyRegistrants");
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        }
    }

    void onMMIDone(ImsPhoneMmiCode mmi, Exception e) {
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest()) {
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, e));
        }
    }

    public ArrayList<Connection> getHandoverConnection() {
        ArrayList<Connection> connList = new ArrayList();
        connList.addAll(getForegroundCall().mConnections);
        connList.addAll(getBackgroundCall().mConnections);
        connList.addAll(getRingingCall().mConnections);
        if (connList.size() > 0) {
            return connList;
        }
        return null;
    }

    public void notifySrvccState(SrvccState state) {
        this.mCT.notifySrvccState(state);
    }

    void initiateSilentRedial() {
        AsyncResult ar = new AsyncResult(null, this.mLastDialString, null);
        if (ar != null) {
            this.mSilentRedialRegistrants.notifyRegistrants(ar);
        }
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
        if (this.mSsnRegistrants.size() == 0) {
            this.mDefaultPhone.mCi.setSuppServiceNotifications(false, null);
        }
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
        cfInfo.status = info.mStatus;
        cfInfo.reason = getCFReasonFromCondition(info.mCondition);
        cfInfo.serviceClass = 1;
        cfInfo.toa = info.mToA;
        cfInfo.number = info.mNumber;
        cfInfo.timeSeconds = info.mTimeSeconds;
        cfInfo.startHour = info.mStartHour;
        cfInfo.startMinute = info.mStartMinute;
        cfInfo.endHour = info.mEndHour;
        cfInfo.endMinute = info.mEndMinute;
        return cfInfo;
    }

    private CallForwardInfo[] handleCfQueryResult(ImsCallForwardInfo[] infos) {
        CallForwardInfo[] cfInfos = null;
        if (!(infos == null || infos.length == 0)) {
            cfInfos = new CallForwardInfo[infos.length];
        }
        IccRecords r = this.mDefaultPhone.getIccRecords();
        if (infos != null && infos.length != 0) {
            int s = infos.length;
            for (int i = 0; i < s; i++) {
                if (infos[i].mCondition == 0 && r != null) {
                    boolean z;
                    if (infos[i].mStatus == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    setVoiceCallForwardingFlag(r, 1, z, infos[i].mNumber);
                }
                cfInfos[i] = getCallForwardInfo(infos[i]);
            }
        } else if (r != null) {
            setVoiceCallForwardingFlag(r, 1, false, null);
        }
        return cfInfos;
    }

    private int[] handleCbQueryResult(ImsSsInfo[] infos) {
        int[] cbInfos = new int[]{0};
        if (infos[0].mStatus == 1) {
            cbInfos[0] = 1;
        }
        return cbInfos;
    }

    private int[] handleCwQueryResult(ImsSsInfo[] infos) {
        int[] cwInfos = new int[2];
        cwInfos[0] = 0;
        if (infos[0].mStatus == 1) {
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
            Rlog.d(LOG_TAG, "updateDataServiceState: defSs = " + ss + " imsSs = " + this.mSS);
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar = msg.obj;
        Rlog.d(LOG_TAG, "handleMessage what=" + msg.what);
        if (!this.mHwImsPhone.beforeHandleMessage(msg)) {
            switch (msg.what) {
                case 12:
                    IccRecords r = this.mDefaultPhone.getIccRecords();
                    Cf cf = ar.userObj;
                    if (cf.mIsCfu && ar.exception == null && r != null) {
                        setVoiceCallForwardingFlag(r, 1, msg.arg1 == 1, cf.mSetCfNumber);
                    }
                    sendResponse(cf.mOnComplete, null, ar.exception);
                    break;
                case 13:
                    if (ar.exception != null || !(ar.result instanceof ImsCallForwardInfo[])) {
                        sendResponse((Message) ar.userObj, ar.result, ar.exception);
                        break;
                    }
                    sendResponse((Message) ar.userObj, handleCfQueryResult((ImsCallForwardInfo[]) ar.result), ar.exception);
                    break;
                    break;
                case 45:
                    Rlog.d(LOG_TAG, "Callforwarding is " + getCallForwardingPreference());
                    notifyCallForwardingIndicator();
                    break;
                case 46:
                case 48:
                    break;
                case 47:
                case 49:
                    Throwable th = null;
                    if (ar.userObj != null && (ar.userObj instanceof Message)) {
                        Message tempMsg = ar.userObj;
                        if (tempMsg.obj != null && (tempMsg.obj instanceof Throwable)) {
                            th = tempMsg.obj;
                        }
                    }
                    if (ar.exception != null) {
                        th = ar.exception;
                    }
                    if (ar.exception != null || !(ar.result instanceof ImsSsInfo[])) {
                        sendResponse((Message) ar.userObj, ar.result, th);
                        break;
                    }
                    Object ssInfos = null;
                    if (msg.what == 47) {
                        ssInfos = handleCbQueryResult((ImsSsInfo[]) ar.result);
                    } else if (msg.what == 49) {
                        ssInfos = handleCwQueryResult((ImsSsInfo[]) ar.result);
                    }
                    sendResponse((Message) ar.userObj, ssInfos, th);
                    break;
                    break;
                case 50:
                    if (ar.exception == null) {
                        saveClirSetting(msg.arg1);
                        break;
                    }
                    break;
                case 51:
                    if (!(ar.result instanceof Bundle)) {
                        sendResponse((Message) ar.userObj, ar.result, ar.exception);
                        break;
                    }
                    Object clirInfo = null;
                    Bundle ssInfo = ar.result;
                    if (ssInfo != null) {
                        clirInfo = ssInfo.getIntArray(ImsPhoneMmiCode.UT_BUNDLE_KEY_CLIR);
                    }
                    sendResponse((Message) ar.userObj, clirInfo, ar.exception);
                    break;
                case 52:
                    Rlog.d(LOG_TAG, "EVENT_DEFAULT_PHONE_DATA_STATE_CHANGED");
                    updateDataServiceState();
                    break;
                case 53:
                    ServiceState newServiceState = msg.obj.result;
                    if (this.mRoaming != newServiceState.getRoaming()) {
                        Rlog.d(LOG_TAG, "Roaming state changed");
                        updateRoamingState(newServiceState.getRoaming());
                        break;
                    }
                    break;
                case 54:
                    Rlog.d(LOG_TAG, "Voice call ended. Handle pending updateRoamingState.");
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
        }
    }

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
        Rlog.d(LOG_TAG, "sendEmergencyCallbackModeChange");
    }

    public void exitEmergencyCallbackMode() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        Rlog.d(LOG_TAG, "exitEmergencyCallbackMode()");
        try {
            this.mCT.getEcbmInterface().exitEmergencyCallbackMode();
        } catch (ImsException e) {
            e.printStackTrace();
        }
    }

    private void handleEnterEmergencyCallbackMode() {
        Rlog.d(LOG_TAG, "handleEnterEmergencyCallbackMode,mIsPhoneInEcmState= " + isInEcm());
        if (!isInEcm()) {
            setIsInEcm(true);
            sendEmergencyCallbackModeChange();
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            this.mWakeLock.acquire();
        }
    }

    private void handleExitEmergencyCallbackMode() {
        Rlog.d(LOG_TAG, "handleExitEmergencyCallbackMode: mIsPhoneInEcmState = " + isInEcm());
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
    }

    void handleTimerInEmergencyCallbackMode(int action) {
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
                Rlog.e(LOG_TAG, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
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
        if (imsReasonInfo.mCode == 1000 && imsReasonInfo.mExtraMessage != null) {
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            if (configManager == null) {
                Rlog.e(LOG_TAG, "processDisconnectReason: CarrierConfigManager is not ready");
                return;
            }
            PersistableBundle pb = configManager.getConfigForSubId(getSubId());
            if (pb == null) {
                Rlog.e(LOG_TAG, "processDisconnectReason: no config for subId " + getSubId());
                return;
            }
            String[] wfcOperatorErrorCodes = pb.getStringArray("wfc_operator_error_codes_string_array");
            if (wfcOperatorErrorCodes != null) {
                String[] wfcOperatorErrorAlertMessages = this.mContext.getResources().getStringArray(17236076);
                String[] wfcOperatorErrorNotificationMessages = this.mContext.getResources().getStringArray(17236077);
                for (int i = 0; i < wfcOperatorErrorCodes.length; i++) {
                    String[] codes = wfcOperatorErrorCodes[i].split("\\|");
                    if (codes.length != 2) {
                        Rlog.e(LOG_TAG, "Invalid carrier config: " + wfcOperatorErrorCodes[i]);
                    } else if (imsReasonInfo.mExtraMessage.startsWith(codes[0])) {
                        int codeStringLength = codes[0].length();
                        if (!Character.isLetterOrDigit(codes[0].charAt(codeStringLength - 1)) || imsReasonInfo.mExtraMessage.length() <= codeStringLength || !Character.isLetterOrDigit(imsReasonInfo.mExtraMessage.charAt(codeStringLength))) {
                            CharSequence title = this.mContext.getText(17041213);
                            int idx = Integer.parseInt(codes[1]);
                            if (idx < 0 || idx >= wfcOperatorErrorAlertMessages.length || idx >= wfcOperatorErrorNotificationMessages.length) {
                                Rlog.e(LOG_TAG, "Invalid index: " + wfcOperatorErrorCodes[i]);
                            } else {
                                CharSequence messageAlert = imsReasonInfo.mExtraMessage;
                                CharSequence messageNotification = imsReasonInfo.mExtraMessage;
                                if (!wfcOperatorErrorAlertMessages[idx].isEmpty()) {
                                    messageAlert = wfcOperatorErrorAlertMessages[idx];
                                }
                                if (!wfcOperatorErrorNotificationMessages[idx].isEmpty()) {
                                    messageNotification = wfcOperatorErrorNotificationMessages[idx];
                                }
                                ImsManager.setWfcSetting(this.mContext, false);
                                Intent intent = new Intent("com.android.ims.REGISTRATION_ERROR");
                                intent.putExtra(Phone.EXTRA_KEY_ALERT_TITLE, title);
                                intent.putExtra(Phone.EXTRA_KEY_ALERT_MESSAGE, messageAlert);
                                intent.putExtra(Phone.EXTRA_KEY_NOTIFICATION_MESSAGE, messageNotification);
                                this.mContext.sendOrderedBroadcast(intent, null, this.mResultReceiver, null, -1, null, null);
                            }
                        }
                    } else {
                        continue;
                    }
                }
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

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public long getVtDataUsage() {
        return this.mCT.getVtDataUsage();
    }

    private void updateRoamingState(boolean newRoaming) {
        if (this.mCT.getState() == PhoneConstants.State.IDLE) {
            Rlog.d(LOG_TAG, "updateRoamingState now: " + newRoaming);
            this.mRoaming = newRoaming;
            try {
                ((GsmCdmaPhone) this.mDefaultPhone).updateWfcMode(this.mContext, newRoaming, this.mPhoneId);
                return;
            } catch (ImsException e) {
                Rlog.e(LOG_TAG, "updateWfcMode occurs Exception");
                return;
            }
        }
        Rlog.d(LOG_TAG, "updateRoamingState postponed: " + newRoaming);
        this.mCT.registerForVoiceCallEnded(this, 54, null);
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

    public String getImsImpu() {
        Rlog.d(LOG_TAG, "getImsImpu");
        if (this.mCT == null) {
            return null;
        }
        try {
            ImsUtInterface ut = this.mCT.getUtInterface();
            if (ut != null) {
                return ut.getUtIMPUFromNetwork();
            }
            return null;
        } catch (ImsException e) {
            Rlog.e(LOG_TAG, "get UtInterface occures exception");
            return null;
        }
    }
}

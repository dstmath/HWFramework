package com.android.internal.telephony;

import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.Pair;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.ImsSmsDispatcher;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.util.SMSDispatcherUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ImsSmsDispatcher extends SMSDispatcher {
    private static final String TAG = "ImsSmsDispacher";
    private ImsFeature.CapabilityCallback mCapabilityCallback = new ImsFeature.CapabilityCallback() {
        public void onCapabilitiesStatusChanged(ImsFeature.Capabilities config) {
            synchronized (ImsSmsDispatcher.this.mLock) {
                boolean unused = ImsSmsDispatcher.this.mIsSmsCapable = config.isCapable(8);
            }
        }
    };
    private final ImsManager.Connector mImsManagerConnector = new ImsManager.Connector(this.mContext, this.mPhone.getPhoneId(), new ImsManager.Connector.Listener() {
        public void connectionReady(ImsManager manager) throws ImsException {
            Rlog.d(ImsSmsDispatcher.TAG, "ImsManager: connection ready.");
            ImsSmsDispatcher.this.setListeners();
            synchronized (ImsSmsDispatcher.this.mLock) {
                boolean unused = ImsSmsDispatcher.this.mIsImsServiceUp = true;
            }
        }

        public void connectionUnavailable() {
            Rlog.d(ImsSmsDispatcher.TAG, "ImsManager: connection unavailable.");
            synchronized (ImsSmsDispatcher.this.mLock) {
                boolean unused = ImsSmsDispatcher.this.mIsImsServiceUp = false;
            }
        }
    });
    private final IImsSmsListener mImsSmsListener = new IImsSmsListener.Stub() {
        public void onSendSmsResult(int token, int messageRef, int status, int reason) throws RemoteException {
            Rlog.d(ImsSmsDispatcher.TAG, "onSendSmsResult token=" + token + " messageRef=" + messageRef + " status=" + status + " reason=" + reason);
            SMSDispatcher.SmsTracker tracker = ImsSmsDispatcher.this.mTrackers.get(Integer.valueOf(token));
            if (tracker != null) {
                tracker.mMessageRef = messageRef;
                switch (status) {
                    case 1:
                        tracker.onSent(ImsSmsDispatcher.this.mContext);
                        return;
                    case 2:
                        tracker.onFailed(ImsSmsDispatcher.this.mContext, reason, 0);
                        ImsSmsDispatcher.this.mTrackers.remove(Integer.valueOf(token));
                        return;
                    case 3:
                        tracker.mRetryCount++;
                        ImsSmsDispatcher.this.sendSms(tracker);
                        return;
                    case 4:
                        ImsSmsDispatcher.this.fallbackToPstn(token, tracker);
                        return;
                    default:
                        return;
                }
            } else {
                throw new IllegalArgumentException("Invalid token.");
            }
        }

        public void onSmsStatusReportReceived(int token, int messageRef, String format, byte[] pdu) throws RemoteException {
            int i;
            Rlog.d(ImsSmsDispatcher.TAG, "Status report received.");
            SMSDispatcher.SmsTracker tracker = ImsSmsDispatcher.this.mTrackers.get(Integer.valueOf(token));
            if (tracker != null) {
                Pair<Boolean, Boolean> result = ImsSmsDispatcher.this.mSmsDispatchersController.handleSmsStatusReport(tracker, format, pdu);
                Rlog.d(ImsSmsDispatcher.TAG, "Status report handle result, success: " + result.first + "complete: " + result.second);
                try {
                    ImsManager access$300 = ImsSmsDispatcher.this.getImsManager();
                    if (((Boolean) result.first).booleanValue()) {
                        i = 1;
                    } else {
                        i = 2;
                    }
                    access$300.acknowledgeSmsReport(token, messageRef, i);
                } catch (ImsException e) {
                    Rlog.e(ImsSmsDispatcher.TAG, "Failed to acknowledgeSmsReport(). Error: " + e.getMessage());
                }
                if (((Boolean) result.second).booleanValue()) {
                    ImsSmsDispatcher.this.mTrackers.remove(Integer.valueOf(token));
                    return;
                }
                return;
            }
            throw new RemoteException("Invalid token.");
        }

        public void onSmsReceived(int token, String format, byte[] pdu) throws RemoteException {
            Rlog.d(ImsSmsDispatcher.TAG, "SMS received.");
            SmsMessage message = SmsMessage.createFromPdu(pdu, format);
            ImsSmsDispatcher.this.mSmsDispatchersController.injectSmsPdu(message, format, new SmsDispatchersController.SmsInjectionCallback(message, token) {
                private final /* synthetic */ SmsMessage f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void onSmsInjectedResult(int i) {
                    ImsSmsDispatcher.AnonymousClass3.lambda$onSmsReceived$0(ImsSmsDispatcher.AnonymousClass3.this, this.f$1, this.f$2, i);
                }
            }, true);
        }

        public static /* synthetic */ void lambda$onSmsReceived$0(AnonymousClass3 r5, SmsMessage message, int token, int result) {
            int mappedResult;
            Rlog.d(ImsSmsDispatcher.TAG, "SMS handled result: " + result);
            if (result != 1) {
                switch (result) {
                    case 3:
                        mappedResult = 3;
                        break;
                    case 4:
                        mappedResult = 4;
                        break;
                    default:
                        mappedResult = 2;
                        break;
                }
            } else {
                mappedResult = 1;
            }
            if (message != null) {
                try {
                    if (message.mWrappedSmsMessage != null) {
                        ImsSmsDispatcher.this.getImsManager().acknowledgeSms(token, message.mWrappedSmsMessage.mMessageRef, mappedResult);
                        return;
                    }
                } catch (ImsException e) {
                    Rlog.e(ImsSmsDispatcher.TAG, "Failed to acknowledgeSms(). Error: " + e.getMessage());
                    return;
                }
            }
            Rlog.w(ImsSmsDispatcher.TAG, "SMS Received with a PDU that could not be parsed.");
            ImsSmsDispatcher.this.getImsManager().acknowledgeSms(token, 0, mappedResult);
        }
    };
    /* access modifiers changed from: private */
    public volatile boolean mIsImsServiceUp;
    /* access modifiers changed from: private */
    public volatile boolean mIsRegistered;
    /* access modifiers changed from: private */
    public volatile boolean mIsSmsCapable;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    @VisibleForTesting
    public AtomicInteger mNextToken = new AtomicInteger();
    private ImsRegistrationImplBase.Callback mRegistrationCallback = new ImsRegistrationImplBase.Callback() {
        public void onRegistered(int imsRadioTech) {
            Rlog.d(ImsSmsDispatcher.TAG, "onImsConnected imsRadioTech=" + imsRadioTech);
            synchronized (ImsSmsDispatcher.this.mLock) {
                boolean unused = ImsSmsDispatcher.this.mIsRegistered = true;
            }
        }

        public void onRegistering(int imsRadioTech) {
            Rlog.d(ImsSmsDispatcher.TAG, "onImsProgressing imsRadioTech=" + imsRadioTech);
            synchronized (ImsSmsDispatcher.this.mLock) {
                boolean unused = ImsSmsDispatcher.this.mIsRegistered = false;
            }
        }

        public void onDeregistered(ImsReasonInfo info) {
            Rlog.d(ImsSmsDispatcher.TAG, "onImsDisconnected imsReasonInfo=" + info);
            synchronized (ImsSmsDispatcher.this.mLock) {
                boolean unused = ImsSmsDispatcher.this.mIsRegistered = false;
            }
        }
    };
    @VisibleForTesting
    public Map<Integer, SMSDispatcher.SmsTracker> mTrackers = new ConcurrentHashMap();

    public ImsSmsDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        super(phone, smsDispatchersController);
        this.mImsManagerConnector.connect();
    }

    /* access modifiers changed from: private */
    public void setListeners() throws ImsException {
        getImsManager().addRegistrationCallback(this.mRegistrationCallback);
        getImsManager().addCapabilitiesCallback(this.mCapabilityCallback);
        getImsManager().setSmsListener(this.mImsSmsListener);
        getImsManager().onSmsReady();
    }

    public boolean isAvailable() {
        boolean z;
        synchronized (this.mLock) {
            Rlog.d(TAG, "isAvailable: up=" + this.mIsImsServiceUp + ", reg= " + this.mIsRegistered + ", cap= " + this.mIsSmsCapable);
            z = this.mIsImsServiceUp && this.mIsRegistered && this.mIsSmsCapable;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public String getFormat() {
        try {
            return getImsManager().getSmsFormat();
        } catch (ImsException e) {
            Rlog.e(TAG, "Failed to get sms format. Error: " + e.getMessage());
            return "unknown";
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldBlockSmsForEcbm() {
        return false;
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, String message, boolean statusReportRequested, SmsHeader smsHeader, int priority, int validityPeriod) {
        return SMSDispatcherUtil.getSubmitPdu(isCdmaMo(), scAddr, destAddr, message, statusReportRequested, smsHeader, priority, validityPeriod);
    }

    /* access modifiers changed from: protected */
    public SmsMessageBase.SubmitPduBase getSubmitPdu(String scAddr, String destAddr, int destPort, byte[] message, boolean statusReportRequested) {
        return SMSDispatcherUtil.getSubmitPdu(isCdmaMo(), scAddr, destAddr, destPort, message, statusReportRequested);
    }

    /* access modifiers changed from: protected */
    public GsmAlphabet.TextEncodingDetails calculateLength(CharSequence messageBody, boolean use7bitOnly) {
        return SMSDispatcherUtil.calculateLength(isCdmaMo(), messageBody, use7bitOnly);
    }

    public void sendSms(SMSDispatcher.SmsTracker tracker) {
        Rlog.d(TAG, "sendSms:  mRetryCount=" + tracker.mRetryCount + " mMessageRef=" + tracker.mMessageRef + " SS=" + this.mPhone.getServiceState().getState());
        tracker.mUsesImsServiceForIms = true;
        HashMap<String, Object> map = tracker.getData();
        byte[] pdu = (byte[]) map.get("pdu");
        byte[] smsc = (byte[]) map.get("smsc");
        boolean isRetry = tracker.mRetryCount > 0;
        if ("3gpp".equals(getFormat()) && tracker.mRetryCount > 0 && (pdu[0] & 1) == 1) {
            pdu[0] = (byte) (pdu[0] | 4);
            pdu[1] = (byte) tracker.mMessageRef;
        }
        int token = this.mNextToken.incrementAndGet();
        this.mTrackers.put(Integer.valueOf(token), tracker);
        try {
            getImsManager().sendSms(token, tracker.mMessageRef, getFormat(), smsc != null ? new String(smsc) : null, isRetry, pdu);
        } catch (ImsException e) {
            Rlog.e(TAG, "sendSms failed. Falling back to PSTN. Error: " + e.getMessage());
            fallbackToPstn(token, tracker);
        }
    }

    /* access modifiers changed from: private */
    public ImsManager getImsManager() {
        return ImsManager.getInstance(this.mContext, this.mPhone.getPhoneId());
    }

    @VisibleForTesting
    public void fallbackToPstn(int token, SMSDispatcher.SmsTracker tracker) {
        this.mSmsDispatchersController.sendRetrySms(tracker);
        this.mTrackers.remove(Integer.valueOf(token));
    }

    /* access modifiers changed from: protected */
    public boolean isCdmaMo() {
        return this.mSmsDispatchersController.isCdmaFormat(getFormat());
    }
}

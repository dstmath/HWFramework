package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SmsMessage;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.SMSDispatcher;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.CdmaInboundSmsHandler;
import com.android.internal.telephony.cdma.CdmaSMSDispatcher;
import com.android.internal.telephony.gsm.GsmInboundSmsHandler;
import com.android.internal.telephony.gsm.GsmSMSDispatcher;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class SmsDispatchersController extends Handler {
    private static final int EVENT_IMS_STATE_CHANGED = 12;
    private static final int EVENT_IMS_STATE_DONE = 13;
    private static final int EVENT_PARTIAL_SEGMENT_TIMER_EXPIRY = 15;
    private static final int EVENT_RADIO_ON = 11;
    private static final int EVENT_SERVICE_STATE_CHANGED = 14;
    protected static final int EVENT_SMS_HANDLER_EXITING_WAITING_STATE = 17;
    private static final int EVENT_USER_UNLOCKED = 16;
    private static final long INVALID_TIME = -1;
    private static final long PARTIAL_SEGMENT_WAIT_DURATION = 86400000;
    private static final String TAG = "SmsDispatchersController";
    private static final boolean VDBG = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.SmsDispatchersController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Rlog.d(SmsDispatchersController.TAG, "Received broadcast " + intent.getAction());
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                SmsDispatchersController smsDispatchersController = SmsDispatchersController.this;
                smsDispatchersController.sendMessage(smsDispatchersController.obtainMessage(16));
            }
        }
    };
    private SMSDispatcher mCdmaDispatcher;
    private CdmaInboundSmsHandler mCdmaInboundSmsHandler;
    private final CommandsInterface mCi;
    private final Context mContext;
    private long mCurrentWaitElapsedDuration = 0;
    private long mCurrentWaitStartTime = -1;
    private SMSDispatcher mGsmDispatcher;
    private GsmInboundSmsHandler mGsmInboundSmsHandler;
    private boolean mIms = false;
    private ImsSmsDispatcher mImsSmsDispatcher;
    private String mImsSmsFormat = "unknown";
    private long mLastInServiceTime = -1;
    private Phone mPhone;
    private final SmsUsageMonitor mUsageMonitor;

    public interface SmsInjectionCallback {
        void onSmsInjectedResult(int i);
    }

    public SmsDispatchersController(Phone phone, SmsStorageMonitor storageMonitor, SmsUsageMonitor usageMonitor) {
        Rlog.d(TAG, "SmsDispatchersController created");
        this.mContext = phone.getContext();
        this.mUsageMonitor = usageMonitor;
        this.mCi = phone.mCi;
        this.mPhone = phone;
        this.mImsSmsDispatcher = new ImsSmsDispatcher(phone, this);
        this.mCdmaDispatcher = new CdmaSMSDispatcher(phone, this);
        this.mGsmInboundSmsHandler = GsmInboundSmsHandler.makeInboundSmsHandler(phone.getContext(), storageMonitor, phone);
        this.mCdmaInboundSmsHandler = CdmaInboundSmsHandler.makeInboundSmsHandler(phone.getContext(), storageMonitor, phone, (CdmaSMSDispatcher) this.mCdmaDispatcher);
        this.mGsmDispatcher = new GsmSMSDispatcher(phone, this, this.mGsmInboundSmsHandler);
        SmsBroadcastUndelivered.initialize(phone.getContext(), this.mGsmInboundSmsHandler, this.mCdmaInboundSmsHandler);
        InboundSmsHandler.registerNewMessageNotificationActionHandler(phone.getContext());
        this.mCi.registerForOn(this, 11, null);
        this.mCi.registerForImsNetworkStateChanged(this, 12, null);
        if (((UserManager) this.mContext.getSystemService("user")).isUserUnlocked()) {
            this.mPhone.registerForServiceStateChanged(this, 14, null);
            resetPartialSegmentWaitTimer();
            return;
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, userFilter);
    }

    public void dispose() {
        this.mCi.unregisterForOn(this);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mPhone.unregisterForServiceStateChanged(this);
        this.mGsmDispatcher.dispose();
        this.mCdmaDispatcher.dispose();
        this.mGsmInboundSmsHandler.dispose();
        this.mCdmaInboundSmsHandler.dispose();
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 11:
            case 12:
                this.mCi.getImsRegistrationState(obtainMessage(13));
                return;
            case 13:
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    updateImsInfo(ar);
                    return;
                }
                Rlog.e(TAG, "IMS State query failed with exp " + ar.exception);
                return;
            case 14:
            case 17:
                reevaluateTimerStatus();
                return;
            case 15:
                handlePartialSegmentTimerExpiry(((Long) msg.obj).longValue());
                return;
            case 16:
                this.mPhone.registerForServiceStateChanged(this, 14, null);
                resetPartialSegmentWaitTimer();
                return;
            default:
                if (isCdmaMo()) {
                    this.mCdmaDispatcher.handleMessage(msg);
                    return;
                } else {
                    this.mGsmDispatcher.handleMessage(msg);
                    return;
                }
        }
    }

    private void reevaluateTimerStatus() {
        long currentTime = System.currentTimeMillis();
        removeMessages(15);
        long j = this.mLastInServiceTime;
        if (j != -1) {
            this.mCurrentWaitElapsedDuration += currentTime - j;
        }
        if (this.mCurrentWaitElapsedDuration > PARTIAL_SEGMENT_WAIT_DURATION) {
            handlePartialSegmentTimerExpiry(this.mCurrentWaitStartTime);
        } else if (isInService()) {
            handleInService(currentTime);
        } else {
            handleOutOfService(currentTime);
        }
    }

    private void handleInService(long currentTime) {
        if (this.mCurrentWaitStartTime == -1) {
            this.mCurrentWaitStartTime = currentTime;
        }
        sendMessageDelayed(obtainMessage(15, Long.valueOf(this.mCurrentWaitStartTime)), PARTIAL_SEGMENT_WAIT_DURATION - this.mCurrentWaitElapsedDuration);
        this.mLastInServiceTime = currentTime;
    }

    private void handleOutOfService(long currentTime) {
        this.mLastInServiceTime = -1;
    }

    private void handlePartialSegmentTimerExpiry(long waitTimerStart) {
        if (this.mGsmInboundSmsHandler.getCurrentState().getName().equals("WaitingState") || this.mCdmaInboundSmsHandler.getCurrentState().getName().equals("WaitingState")) {
            logd("handlePartialSegmentTimerExpiry: ignoring timer expiry as InboundSmsHandler is in WaitingState");
            return;
        }
        SmsBroadcastUndelivered.scanRawTable(this.mContext, this.mCdmaInboundSmsHandler, this.mGsmInboundSmsHandler, waitTimerStart);
        resetPartialSegmentWaitTimer();
    }

    private void resetPartialSegmentWaitTimer() {
        long currentTime = System.currentTimeMillis();
        removeMessages(15);
        if (isInService()) {
            this.mCurrentWaitStartTime = currentTime;
            this.mLastInServiceTime = currentTime;
            sendMessageDelayed(obtainMessage(15, Long.valueOf(this.mCurrentWaitStartTime)), PARTIAL_SEGMENT_WAIT_DURATION);
        } else {
            this.mCurrentWaitStartTime = -1;
            this.mLastInServiceTime = -1;
        }
        this.mCurrentWaitElapsedDuration = 0;
    }

    private boolean isInService() {
        ServiceState serviceState = this.mPhone.getServiceState();
        return serviceState != null && serviceState.getState() == 0;
    }

    private void setImsSmsFormat(int format) {
        if (format == 1) {
            this.mImsSmsFormat = "3gpp";
        } else if (format != 2) {
            this.mImsSmsFormat = "unknown";
        } else {
            this.mImsSmsFormat = "3gpp2";
        }
    }

    private void updateImsInfo(AsyncResult ar) {
        int[] responseArray = (int[]) ar.result;
        boolean z = true;
        setImsSmsFormat(responseArray[1]);
        if (responseArray[0] != 1 || "unknown".equals(this.mImsSmsFormat)) {
            z = false;
        }
        this.mIms = z;
        Rlog.d(TAG, "IMS registration state: " + this.mIms + " format: " + this.mImsSmsFormat);
    }

    @VisibleForTesting
    public void injectSmsPdu(byte[] pdu, String format, SmsInjectionCallback callback) {
        injectSmsPdu(SmsMessage.createFromPdu(pdu, format), format, callback, false);
    }

    @VisibleForTesting
    public void injectSmsPdu(SmsMessage msg, String format, SmsInjectionCallback callback, boolean ignoreClass) {
        Rlog.d(TAG, "SmsDispatchersController:injectSmsPdu");
        if (msg == null) {
            try {
                Rlog.e(TAG, "injectSmsPdu: createFromPdu returned null");
                callback.onSmsInjectedResult(2);
            } catch (Exception e) {
                Rlog.e(TAG, "injectSmsPdu failed: ", e);
                callback.onSmsInjectedResult(2);
            }
        } else if (ignoreClass || msg.getMessageClass() == SmsMessage.MessageClass.CLASS_1) {
            AsyncResult ar = new AsyncResult(callback, msg, (Throwable) null);
            if (format.equals("3gpp")) {
                Rlog.i(TAG, "SmsDispatchersController:injectSmsText Sending msg=" + msg + ", format=" + format + "to mGsmInboundSmsHandler");
                this.mGsmInboundSmsHandler.sendMessage(7, ar);
            } else if (format.equals("3gpp2")) {
                Rlog.i(TAG, "SmsDispatchersController:injectSmsText Sending msg=" + msg + ", format=" + format + "to mCdmaInboundSmsHandler");
                this.mCdmaInboundSmsHandler.sendMessage(7, ar);
            } else {
                Rlog.e(TAG, "Invalid pdu format: " + format);
                callback.onSmsInjectedResult(2);
            }
        } else {
            Rlog.e(TAG, "injectSmsPdu: not class 1");
            callback.onSmsInjectedResult(2);
        }
    }

    public void sendRetrySms(SMSDispatcher.SmsTracker tracker) {
        String oldFormat = tracker.mFormat;
        String newFormat = (2 == this.mPhone.getPhoneType() ? this.mCdmaDispatcher : this.mGsmDispatcher).getFormat();
        if (!oldFormat.equals(newFormat)) {
            HashMap map = tracker.getData();
            boolean z = true;
            if (!map.containsKey("scAddr") || !map.containsKey("destAddr") || (!map.containsKey("text") && (!map.containsKey("data") || !map.containsKey("destPort")))) {
                Rlog.e(TAG, "sendRetrySms failed to re-encode per missing fields!");
                tracker.onFailed(this.mContext, 1, 0);
                return;
            }
            String scAddr = (String) map.get("scAddr");
            String destAddr = (String) map.get("destAddr");
            SmsMessageBase.SubmitPduBase pdu = null;
            if (map.containsKey("text")) {
                Rlog.d(TAG, "sms failed was text");
                String text = (String) map.get("text");
                if (isCdmaFormat(newFormat)) {
                    Rlog.d(TAG, "old format (gsm) ==> new format (cdma)");
                    if (tracker.mDeliveryIntent == null) {
                        z = false;
                    }
                    pdu = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddr, destAddr, text, z, (SmsHeader) null);
                } else {
                    Rlog.d(TAG, "old format (cdma) ==> new format (gsm)");
                    if (tracker.mDeliveryIntent == null) {
                        z = false;
                    }
                    pdu = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddr, destAddr, text, z, (byte[]) null);
                }
            } else if (map.containsKey("data")) {
                Rlog.d(TAG, "sms failed was data");
                byte[] data = (byte[]) map.get("data");
                Integer destPort = (Integer) map.get("destPort");
                if (isCdmaFormat(newFormat)) {
                    Rlog.d(TAG, "old format (gsm) ==> new format (cdma)");
                    int intValue = destPort.intValue();
                    if (tracker.mDeliveryIntent == null) {
                        z = false;
                    }
                    pdu = com.android.internal.telephony.cdma.SmsMessage.getSubmitPdu(scAddr, destAddr, intValue, data, z);
                } else {
                    Rlog.d(TAG, "old format (cdma) ==> new format (gsm)");
                    int intValue2 = destPort.intValue();
                    if (tracker.mDeliveryIntent == null) {
                        z = false;
                    }
                    pdu = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddr, destAddr, intValue2, data, z);
                }
            }
            map.put("smsc", pdu.encodedScAddress);
            map.put("pdu", pdu.encodedMessage);
            SMSDispatcher dispatcher = isCdmaFormat(newFormat) ? this.mCdmaDispatcher : this.mGsmDispatcher;
            tracker.mFormat = dispatcher.getFormat();
            dispatcher.sendSms(tracker);
        } else if (isCdmaFormat(newFormat)) {
            Rlog.d(TAG, "old format matched new format (cdma)");
            this.mCdmaDispatcher.sendSms(tracker);
        } else {
            Rlog.d(TAG, "old format matched new format (gsm)");
            this.mGsmDispatcher.sendSms(tracker);
        }
    }

    public boolean isIms() {
        return this.mIms;
    }

    public String getImsSmsFormat() {
        return this.mImsSmsFormat;
    }

    /* access modifiers changed from: protected */
    public boolean isCdmaMo() {
        if (!isIms()) {
            return 2 == this.mPhone.getPhoneType();
        }
        return isCdmaFormat(this.mImsSmsFormat);
    }

    public boolean isCdmaFormat(String format) {
        return this.mCdmaDispatcher.getFormat().equals(format);
    }

    /* access modifiers changed from: protected */
    public void sendData(String callingPackage, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean isForVvm) {
        if (this.mImsSmsDispatcher.isAvailable()) {
            this.mImsSmsDispatcher.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, isForVvm);
        } else if (isCdmaMo()) {
            this.mCdmaDispatcher.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, isForVvm);
        } else {
            this.mGsmDispatcher.sendData(callingPackage, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent, isForVvm);
        }
    }

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod, boolean isForVvm) {
        if (!this.mImsSmsDispatcher.isAvailable()) {
            if (!this.mImsSmsDispatcher.isEmergencySmsSupport(destAddr)) {
                if (isCdmaMo()) {
                    this.mCdmaDispatcher.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, isForVvm);
                    return;
                } else {
                    this.mGsmDispatcher.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod, isForVvm);
                    return;
                }
            }
        }
        this.mImsSmsDispatcher.sendText(destAddr, scAddr, text, sentIntent, deliveryIntent, messageUri, callingPkg, persistMessage, -1, false, -1, isForVvm);
    }

    /* access modifiers changed from: protected */
    public void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        if (this.mImsSmsDispatcher.isAvailable()) {
            this.mImsSmsDispatcher.sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, -1, false, -1);
        } else if (isCdmaMo()) {
            this.mCdmaDispatcher.sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        } else {
            this.mGsmDispatcher.sendMultipartText(destAddr, scAddr, parts, sentIntents, deliveryIntents, messageUri, callingPkg, persistMessage, priority, expectMore, validityPeriod);
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        return this.mUsageMonitor.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mUsageMonitor.setPremiumSmsPermission(packageName, permission);
    }

    public SmsUsageMonitor getUsageMonitor() {
        return this.mUsageMonitor;
    }

    public Pair<Boolean, Boolean> handleSmsStatusReport(SMSDispatcher.SmsTracker tracker, String format, byte[] pdu) {
        if (isCdmaFormat(format)) {
            return handleCdmaStatusReport(tracker, format, pdu);
        }
        return handleGsmStatusReport(tracker, format, pdu);
    }

    private Pair<Boolean, Boolean> handleCdmaStatusReport(SMSDispatcher.SmsTracker tracker, String format, byte[] pdu) {
        tracker.updateSentMessageStatus(this.mContext, 0);
        return new Pair<>(Boolean.valueOf(triggerDeliveryIntent(tracker, format, pdu)), true);
    }

    private Pair<Boolean, Boolean> handleGsmStatusReport(SMSDispatcher.SmsTracker tracker, String format, byte[] pdu) {
        com.android.internal.telephony.gsm.SmsMessage sms = com.android.internal.telephony.gsm.SmsMessage.newFromCDS(pdu);
        boolean complete = false;
        boolean success = false;
        if (sms != null) {
            int tpStatus = sms.getStatus();
            if (tpStatus >= 64 || tpStatus < 32) {
                tracker.updateSentMessageStatus(this.mContext, tpStatus);
                complete = true;
            }
            success = triggerDeliveryIntent(tracker, format, pdu);
        }
        return new Pair<>(Boolean.valueOf(success), Boolean.valueOf(complete));
    }

    private boolean triggerDeliveryIntent(SMSDispatcher.SmsTracker tracker, String format, byte[] pdu) {
        PendingIntent intent = tracker.mDeliveryIntent;
        Intent fillIn = new Intent();
        fillIn.putExtra("pdu", pdu);
        fillIn.putExtra("format", format);
        try {
            intent.send(this.mContext, -1, fillIn);
            return true;
        } catch (PendingIntent.CanceledException e) {
            return false;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mGsmInboundSmsHandler.dump(fd, pw, args);
        this.mCdmaInboundSmsHandler.dump(fd, pw, args);
    }

    private void logd(String msg) {
        Rlog.d(TAG, msg);
    }
}

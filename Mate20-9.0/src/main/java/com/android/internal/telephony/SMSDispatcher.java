package com.android.internal.telephony;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.service.carrier.ICarrierMessagingCallback;
import android.service.carrier.ICarrierMessagingService;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.EventLog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.cdma.sms.UserData;
import com.android.internal.telephony.gsm.SmsMessage;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SMSDispatcher extends AbstractSMSDispatcher {
    static final boolean DBG = false;
    private static final int EVENT_CONFIRM_SEND_TO_POSSIBLE_PREMIUM_SHORT_CODE = 8;
    private static final int EVENT_CONFIRM_SEND_TO_PREMIUM_SHORT_CODE = 9;
    protected static final int EVENT_GET_IMS_SERVICE = 16;
    protected static final int EVENT_HANDLE_STATUS_REPORT = 10;
    protected static final int EVENT_ICC_CHANGED = 15;
    protected static final int EVENT_NEW_ICC_SMS = 14;
    static final int EVENT_SEND_CONFIRMED_SMS = 5;
    private static final int EVENT_SEND_LIMIT_REACHED_CONFIRMATION = 4;
    private static final int EVENT_SEND_RETRY = 3;
    protected static final int EVENT_SEND_SMS_COMPLETE = 2;
    static final int EVENT_STOP_SENDING = 7;
    protected static final String MAP_KEY_DATA = "data";
    protected static final String MAP_KEY_DEST_ADDR = "destAddr";
    protected static final String MAP_KEY_DEST_PORT = "destPort";
    protected static final String MAP_KEY_PDU = "pdu";
    protected static final String MAP_KEY_SC_ADDR = "scAddr";
    protected static final String MAP_KEY_SMSC = "smsc";
    protected static final String MAP_KEY_TEXT = "text";
    private static final int MAX_SEND_RETRIES = (SystemProperties.getBoolean("ro.config.close_sms_retry", false) ^ true ? 1 : 0);
    private static final int MO_MSG_QUEUE_LIMIT = 5;
    private static final int PREMIUM_RULE_USE_BOTH = 3;
    private static final int PREMIUM_RULE_USE_NETWORK = 2;
    private static final int PREMIUM_RULE_USE_SIM = 1;
    private static final String SEND_NEXT_MSG_EXTRA = "SendNextMsg";
    private static final int SEND_RETRY_DELAY = 2000;
    private static final int SINGLE_PART_SMS = 1;
    static final String TAG = "SMSDispatcher";
    private static int sConcatenatedRef = new Random().nextInt(256);
    protected final ArrayList<SmsTracker> deliveryPendingList = new ArrayList<>();
    protected final CommandsInterface mCi;
    protected final Context mContext;
    private HwCustSMSDispatcher mHwCust;
    private int mPendingTrackerCount;
    protected Phone mPhone;
    private final AtomicInteger mPremiumSmsRule = new AtomicInteger(1);
    protected final ContentResolver mResolver;
    private final SettingsObserver mSettingsObserver;
    protected boolean mSmsCapable = true;
    protected SmsDispatchersController mSmsDispatchersController;
    protected boolean mSmsSendDisabled;
    protected final TelephonyManager mTelephonyManager;

    protected final class ConfirmDialogListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener, CompoundButton.OnCheckedChangeListener {
        private static final int NEVER_ALLOW = 1;
        private static final int RATE_LIMIT = 1;
        private static final int SHORT_CODE_MSG = 0;
        private int mConfirmationType;
        private Button mNegativeButton;
        private Button mPositiveButton;
        private boolean mRememberChoice;
        private final TextView mRememberUndoInstruction;
        private final SmsTracker mTracker;

        ConfirmDialogListener(SmsTracker tracker, TextView textView, int confirmationType) {
            this.mTracker = tracker;
            this.mRememberUndoInstruction = textView;
            this.mConfirmationType = confirmationType;
        }

        /* access modifiers changed from: package-private */
        public void setPositiveButton(Button button) {
            this.mPositiveButton = button;
        }

        /* access modifiers changed from: package-private */
        public void setNegativeButton(Button button) {
            this.mNegativeButton = button;
        }

        public void onClick(DialogInterface dialog, int which) {
            int newSmsPermission = 1;
            int i = -1;
            if (which == -1) {
                Rlog.d(SMSDispatcher.TAG, "CONFIRM sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent(EventLogTags.EXP_DET_SMS_SENT_BY_USER, i);
                SMSDispatcher.this.sendMessage(SMSDispatcher.this.obtainMessage(5, this.mTracker));
                if (this.mRememberChoice) {
                    newSmsPermission = 3;
                }
            } else if (which == -2) {
                Rlog.d(SMSDispatcher.TAG, "DENY sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent(EventLogTags.EXP_DET_SMS_DENIED_BY_USER, i);
                Message msg = SMSDispatcher.this.obtainMessage(7, this.mTracker);
                msg.arg1 = this.mConfirmationType;
                if (this.mRememberChoice) {
                    newSmsPermission = 2;
                    msg.arg2 = 1;
                }
                SMSDispatcher.this.sendMessage(msg);
            }
            SMSDispatcher.this.mSmsDispatchersController.setPremiumSmsPermission(this.mTracker.getAppPackageName(), newSmsPermission);
        }

        public void onCancel(DialogInterface dialog) {
            Rlog.d(SMSDispatcher.TAG, "dialog dismissed: don't send SMS");
            Message msg = SMSDispatcher.this.obtainMessage(7, this.mTracker);
            msg.arg1 = this.mConfirmationType;
            SMSDispatcher.this.sendMessage(msg);
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Rlog.d(SMSDispatcher.TAG, "remember this choice: " + isChecked);
            this.mRememberChoice = isChecked;
            if (isChecked) {
                this.mPositiveButton.setText(17041134);
                this.mNegativeButton.setText(17041137);
                if (this.mRememberUndoInstruction != null) {
                    this.mRememberUndoInstruction.setText(17041140);
                    this.mRememberUndoInstruction.setPadding(0, 0, 0, 32);
                    return;
                }
                return;
            }
            this.mPositiveButton.setText(17041133);
            this.mNegativeButton.setText(17041135);
            if (this.mRememberUndoInstruction != null) {
                this.mRememberUndoInstruction.setText("");
                this.mRememberUndoInstruction.setPadding(0, 0, 0, 0);
            }
        }
    }

    protected final class DataSmsSender extends SmsSender {
        public DataSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        /* access modifiers changed from: protected */
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            HashMap<String, Object> map = this.mTracker.getData();
            byte[] data = (byte[]) map.get(SMSDispatcher.MAP_KEY_DATA);
            int destPort = ((Integer) map.get(SMSDispatcher.MAP_KEY_DEST_PORT)).intValue();
            if (data != null) {
                try {
                    carrierMessagingService.sendDataSms(data, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, destPort, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                } catch (RemoteException e) {
                    Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(1, 0);
                }
            } else {
                this.mSenderCallback.onSendSmsComplete(1, 0);
            }
        }
    }

    private final class MultipartSmsSender extends CarrierMessagingServiceManager {
        private final List<String> mParts;
        private volatile MultipartSmsSenderCallback mSenderCallback;
        public final SmsTracker[] mTrackers;

        MultipartSmsSender(ArrayList<String> parts, SmsTracker[] trackers) {
            this.mParts = parts;
            this.mTrackers = trackers;
        }

        /* access modifiers changed from: package-private */
        public void sendSmsByCarrierApp(String carrierPackageName, MultipartSmsSenderCallback senderCallback) {
            this.mSenderCallback = senderCallback;
            if (!bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
                this.mSenderCallback.onSendMultipartSmsComplete(1, null);
                return;
            }
            Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
        }

        /* access modifiers changed from: protected */
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                carrierMessagingService.sendMultipartTextSms(this.mParts, SMSDispatcher.this.getSubId(), this.mTrackers[0].mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTrackers[0].mDeliveryIntent), this.mSenderCallback);
            } catch (RemoteException e) {
                Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                this.mSenderCallback.onSendMultipartSmsComplete(1, null);
            }
        }
    }

    private final class MultipartSmsSenderCallback extends ICarrierMessagingCallback.Stub {
        private final MultipartSmsSender mSmsSender;

        MultipartSmsSenderCallback(MultipartSmsSender smsSender) {
            this.mSmsSender = smsSender;
        }

        public void onSendSmsComplete(int result, int messageRef) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendSmsComplete call with result: " + result);
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            this.mSmsSender.disposeConnection(SMSDispatcher.this.mContext);
            if (this.mSmsSender.mTrackers == null) {
                Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMultipartSmsComplete call with null trackers.");
                return;
            }
            SMSDispatcher.this.checkCallerIsPhoneOrCarrierApp();
            long identity = Binder.clearCallingIdentity();
            int i = 0;
            while (i < this.mSmsSender.mTrackers.length) {
                try {
                    int messageRef = 0;
                    if (messageRefs != null && messageRefs.length > i) {
                        messageRef = messageRefs[i];
                    }
                    SMSDispatcher.this.processSendSmsResponse(this.mSmsSender.mTrackers[i], result, messageRef);
                    i++;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onFilterComplete(int result) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onFilterComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final AtomicInteger mPremiumSmsRule;

        SettingsObserver(Handler handler, AtomicInteger premiumSmsRule, Context context) {
            super(handler);
            this.mPremiumSmsRule = premiumSmsRule;
            this.mContext = context;
            onChange(false);
        }

        public void onChange(boolean selfChange) {
            this.mPremiumSmsRule.set(Settings.Global.getInt(this.mContext.getContentResolver(), "sms_short_code_rule", 1));
        }
    }

    protected abstract class SmsSender extends CarrierMessagingServiceManager {
        protected volatile SmsSenderCallback mSenderCallback;
        protected final SmsTracker mTracker;

        protected SmsSender(SmsTracker tracker) {
            this.mTracker = tracker;
        }

        public void sendSmsByCarrierApp(String carrierPackageName, SmsSenderCallback senderCallback) {
            this.mSenderCallback = senderCallback;
            if (!bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
                this.mSenderCallback.onSendSmsComplete(1, 0);
                return;
            }
            Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
        }
    }

    protected final class SmsSenderCallback extends ICarrierMessagingCallback.Stub {
        private final SmsSender mSmsSender;

        public SmsSenderCallback(SmsSender smsSender) {
            this.mSmsSender = smsSender;
        }

        public void onSendSmsComplete(int result, int messageRef) {
            SMSDispatcher.this.checkCallerIsPhoneOrCarrierApp();
            long identity = Binder.clearCallingIdentity();
            try {
                this.mSmsSender.disposeConnection(SMSDispatcher.this.mContext);
                SMSDispatcher.this.processSendSmsResponse(this.mSmsSender.mTracker, result, messageRef);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMultipartSmsComplete call with result: " + result);
        }

        public void onFilterComplete(int result) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onFilterComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            Rlog.e(SMSDispatcher.TAG, "Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    public static class SmsTracker {
        private AtomicBoolean mAnyPartFailed;
        public final PackageInfo mAppInfo;
        private final HashMap<String, Object> mData;
        public final PendingIntent mDeliveryIntent;
        public final String mDestAddress;
        public boolean mExpectMore;
        String mFormat;
        private String mFullMessageText;
        public int mImsRetry;
        private boolean mIsSinglePartOrLastPart;
        private boolean mIsText;
        public int mMessageRef;
        public Uri mMessageUri;
        /* access modifiers changed from: private */
        public boolean mPersistMessage;
        public int mPriority;
        public int mRetryCount;
        public final PendingIntent mSentIntent;
        public final SmsHeader mSmsHeader;
        /* access modifiers changed from: private */
        public int mSubId;
        private AtomicInteger mUnsentPartCount;
        /* access modifiers changed from: private */
        public final int mUserId;
        public boolean mUsesImsServiceForIms;
        public int mValidityPeriod;

        private SmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, PackageInfo appInfo, String destAddr, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean expectMore, String fullMessageText, int subId, boolean isText, boolean persistMessage, int userId, int priority, int validityPeriod) {
            this.mData = data;
            this.mSentIntent = sentIntent;
            this.mDeliveryIntent = deliveryIntent;
            this.mRetryCount = 0;
            this.mAppInfo = appInfo;
            this.mDestAddress = destAddr;
            this.mFormat = format;
            this.mExpectMore = expectMore;
            this.mImsRetry = 0;
            this.mUsesImsServiceForIms = false;
            this.mMessageRef = 0;
            this.mUnsentPartCount = unsentPartCount;
            this.mAnyPartFailed = anyPartFailed;
            this.mMessageUri = messageUri;
            this.mSmsHeader = smsHeader;
            this.mFullMessageText = fullMessageText;
            this.mSubId = subId;
            this.mIsText = isText;
            this.mPersistMessage = persistMessage;
            this.mUserId = userId;
            this.mPriority = priority;
            this.mValidityPeriod = validityPeriod;
        }

        /* access modifiers changed from: package-private */
        public boolean isMultipart() {
            return this.mData.containsKey("parts");
        }

        public HashMap<String, Object> getData() {
            return this.mData;
        }

        public String getAppPackageName() {
            if (this.mAppInfo != null) {
                return this.mAppInfo.packageName;
            }
            return null;
        }

        public void updateSentMessageStatus(Context context, int status) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(1);
                values.put("status", Integer.valueOf(status));
                SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null);
            }
        }

        private void updateMessageState(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(2);
                values.put("type", Integer.valueOf(messageType));
                values.put("error_code", Integer.valueOf(errorCode));
                long identity = Binder.clearCallingIdentity();
                try {
                    if (SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null) != 1) {
                        Rlog.e(SMSDispatcher.TAG, "Failed to move message to " + messageType);
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        private Uri persistSentMessageIfRequired(Context context, int messageType, int errorCode) {
            if (!this.mIsText || !this.mPersistMessage || (!HwTelephonyFactory.getHwInnerSmsManager().isSentSmsFromRejectCall(this.mSentIntent) && this.mAppInfo != null && !HwTelephonyFactory.getHwInnerSmsManager().checkShouldWriteSmsPackage(this.mAppInfo.packageName, context))) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Persist SMS into ");
            sb.append(messageType == 5 ? "FAILED" : "SENT");
            Rlog.d(SMSDispatcher.TAG, sb.toString());
            ContentValues values = new ContentValues();
            values.put("sub_id", Integer.valueOf(this.mSubId));
            values.put("address", this.mDestAddress);
            values.put("body", this.mFullMessageText);
            values.put("date", Long.valueOf(System.currentTimeMillis()));
            values.put("seen", 1);
            values.put("read", 1);
            String creator = this.mAppInfo != null ? this.mAppInfo.packageName : null;
            if (!TextUtils.isEmpty(creator)) {
                values.put("creator", creator);
            }
            if (this.mDeliveryIntent != null) {
                values.put("status", 32);
            }
            if (errorCode != 0) {
                values.put("error_code", Integer.valueOf(errorCode));
            }
            long identity = Binder.clearCallingIdentity();
            ContentResolver resolver = context.getContentResolver();
            try {
                Uri uri = resolver.insert(Telephony.Sms.Sent.CONTENT_URI, values);
                if (uri != null && messageType == 5) {
                    ContentValues updateValues = new ContentValues(1);
                    updateValues.put("type", 5);
                    resolver.update(uri, updateValues, null, null);
                }
                return uri;
            } catch (Exception e) {
                Rlog.e(SMSDispatcher.TAG, "writeOutboxMessage: Failed to persist outbox message", e);
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private void persistOrUpdateMessage(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                updateMessageState(context, messageType, errorCode);
            } else {
                this.mMessageUri = persistSentMessageIfRequired(context, messageType, errorCode);
            }
        }

        public void onFailed(Context context, int error, int errorCode) {
            if (this.mAnyPartFailed != null) {
                this.mAnyPartFailed.set(true);
            }
            boolean isSinglePartOrLastPart = true;
            if (this.mUnsentPartCount != null) {
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0;
            }
            if (isSinglePartOrLastPart) {
                persistOrUpdateMessage(context, 5, errorCode);
            }
            if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (errorCode != 0) {
                        fillIn.putExtra("errorCode", errorCode);
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    this.mSentIntent.send(context, error, fillIn);
                } catch (PendingIntent.CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        public void onSent(Context context) {
            boolean isSinglePartOrLastPart = true;
            if (this.mUnsentPartCount != null) {
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0;
            }
            this.mIsSinglePartOrLastPart = isSinglePartOrLastPart;
            if (isSinglePartOrLastPart) {
                int messageType = 2;
                if (this.mAnyPartFailed != null && this.mAnyPartFailed.get()) {
                    messageType = 5;
                }
                persistOrUpdateMessage(context, messageType, 0);
            }
            if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    this.mSentIntent.send(context, -1, fillIn);
                } catch (PendingIntent.CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        public boolean isSinglePartOrLastPart() {
            Rlog.d(SMSDispatcher.TAG, "mIsSinglePartOrLastPart: " + this.mIsSinglePartOrLastPart);
            return this.mIsSinglePartOrLastPart;
        }
    }

    protected final class TextSmsSender extends SmsSender {
        public TextSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        /* access modifiers changed from: protected */
        public void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            String text = (String) this.mTracker.getData().get(SMSDispatcher.MAP_KEY_TEXT);
            if (text != null) {
                try {
                    carrierMessagingService.sendTextSms(text, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                } catch (RemoteException e) {
                    Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(1, 0);
                }
            } else {
                this.mSenderCallback.onSendSmsComplete(1, 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public abstract GsmAlphabet.TextEncodingDetails calculateLength(CharSequence charSequence, boolean z);

    /* access modifiers changed from: protected */
    public abstract String getFormat();

    /* access modifiers changed from: protected */
    public abstract SmsMessageBase.SubmitPduBase getSubmitPdu(String str, String str2, int i, byte[] bArr, boolean z);

    /* access modifiers changed from: protected */
    public abstract SmsMessageBase.SubmitPduBase getSubmitPdu(String str, String str2, String str3, boolean z, SmsHeader smsHeader, int i, int i2);

    /* access modifiers changed from: protected */
    public abstract void sendSms(SmsTracker smsTracker);

    /* access modifiers changed from: protected */
    public abstract boolean shouldBlockSmsForEcbm();

    protected static int getNextConcatenatedRef() {
        sConcatenatedRef++;
        return sConcatenatedRef;
    }

    protected SMSDispatcher(Phone phone, SmsDispatchersController smsDispatchersController) {
        this.mPhone = phone;
        this.mSmsDispatchersController = smsDispatchersController;
        this.mContext = phone.getContext();
        this.mResolver = this.mContext.getContentResolver();
        this.mCi = phone.mCi;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSettingsObserver = new SettingsObserver(this, this.mPremiumSmsRule, this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("sms_short_code_rule"), false, this.mSettingsObserver);
        this.mSmsCapable = this.mContext.getResources().getBoolean(17957026);
        this.mSmsSendDisabled = !this.mTelephonyManager.getSmsSendCapableForPhone(this.mPhone.getPhoneId(), this.mSmsCapable);
        this.mHwCust = (HwCustSMSDispatcher) HwCustUtils.createObj(HwCustSMSDispatcher.class, new Object[0]);
        Rlog.d(TAG, "SMSDispatcher: ctor mSmsCapable=" + this.mSmsCapable + " format=" + getFormat() + " mSmsSendDisabled=" + this.mSmsSendDisabled);
    }

    /* access modifiers changed from: protected */
    public void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        Rlog.d(TAG, "Active phone changed to " + this.mPhone.getPhoneName());
    }

    public void dispose() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
    }

    /* access modifiers changed from: protected */
    public void handleStatusReport(Object o) {
        Rlog.d(TAG, "handleStatusReport() called with no subclass.");
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != 17) {
            switch (i) {
                case 2:
                    handleSendComplete((AsyncResult) msg.obj);
                    return;
                case 3:
                    Rlog.d(TAG, "SMS retry..");
                    sendSmsImmediately((SmsTracker) msg.obj);
                    return;
                case 4:
                    handleReachSentLimit((SmsTracker) msg.obj);
                    return;
                case 5:
                    SmsTracker tracker = (SmsTracker) msg.obj;
                    if (tracker.isMultipart()) {
                        sendMultipartSms(tracker);
                    } else {
                        if (this.mPendingTrackerCount > 1) {
                            tracker.mExpectMore = true;
                        } else {
                            tracker.mExpectMore = false;
                        }
                        sendSms(tracker);
                    }
                    this.mPendingTrackerCount--;
                    return;
                default:
                    switch (i) {
                        case 7:
                            SmsTracker tracker2 = (SmsTracker) msg.obj;
                            if (msg.arg1 == 0) {
                                if (msg.arg2 == 1) {
                                    tracker2.onFailed(this.mContext, 8, 0);
                                    Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending SHORT_CODE_NEVER_ALLOWED error code.");
                                } else {
                                    tracker2.onFailed(this.mContext, 7, 0);
                                    Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending SHORT_CODE_NOT_ALLOWED error code.");
                                }
                            } else if (msg.arg1 == 1) {
                                tracker2.onFailed(this.mContext, 5, 0);
                                Rlog.d(TAG, "SMSDispatcher: EVENT_STOP_SENDING - sending LIMIT_EXCEEDED error code.");
                            } else {
                                Rlog.e(TAG, "SMSDispatcher: EVENT_STOP_SENDING - unexpected cases.");
                            }
                            this.mPendingTrackerCount--;
                            return;
                        case 8:
                            handleConfirmShortCode(false, (SmsTracker) msg.obj);
                            return;
                        case 9:
                            handleConfirmShortCode(true, (SmsTracker) msg.obj);
                            return;
                        case 10:
                            handleStatusReport(msg.obj);
                            return;
                        default:
                            Rlog.e(TAG, "handleMessage() ignoring message of unexpected type " + msg.what);
                            return;
                    }
            }
        } else {
            if (this.mToast != null) {
                this.mToast.cancel();
            }
            this.mToast = Toast.makeText(this.mContext, 17041262, 1);
            this.mToast.show();
        }
    }

    /* access modifiers changed from: private */
    public static int getSendSmsFlag(PendingIntent deliveryIntent) {
        if (deliveryIntent == null) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    public void processSendSmsResponse(SmsTracker tracker, int result, int messageRef) {
        if (tracker == null) {
            Rlog.e(TAG, "processSendSmsResponse: null tracker");
            return;
        }
        SmsResponse smsResponse = new SmsResponse(messageRef, null, -1);
        switch (result) {
            case 0:
                Rlog.d(TAG, "Sending SMS by IP succeeded.");
                sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, null)));
                break;
            case 1:
                Rlog.d(TAG, "Sending SMS by IP failed. Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
            case 2:
                Rlog.d(TAG, "Sending SMS by IP failed.");
                sendMessage(obtainMessage(2, new AsyncResult(tracker, smsResponse, new CommandException(CommandException.Error.GENERIC_FAILURE))));
                break;
            default:
                Rlog.d(TAG, "Unknown result " + result + " Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
        }
    }

    private void sendSubmitPdu(SmsTracker tracker) {
        if (shouldBlockSmsForEcbm()) {
            Rlog.d(TAG, "Block SMS in Emergency Callback mode");
            tracker.onFailed(this.mContext, 4, 0);
            return;
        }
        sendRawPdu(tracker);
    }

    /* access modifiers changed from: protected */
    public void handleSendComplete(AsyncResult ar) {
        SmsTracker tracker = (SmsTracker) ar.userObj;
        PendingIntent pendingIntent = tracker.mSentIntent;
        if (ar.result != null) {
            tracker.mMessageRef = ((SmsResponse) ar.result).mMessageRef;
        } else {
            Rlog.d(TAG, "SmsResponse was null");
        }
        if (ar.exception == null) {
            if (tracker.mDeliveryIntent != null) {
                this.deliveryPendingList.add(tracker);
                Rlog.d(TAG, "deliveryPendingList add mMessageRef: " + tracker.mMessageRef);
            }
            tracker.onSent(this.mContext);
            if (HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(true) && tracker.isSinglePartOrLastPart() && !"com.android.phone".equals(tracker.mAppInfo.packageName)) {
                HwTelephonyFactory.getHwInnerSmsManager().updateSmsUsedNum(this.mContext, true);
            }
        } else {
            int ss = this.mPhone.getServiceState().getState();
            if (tracker.mImsRetry > 0 && ss != 0) {
                tracker.mRetryCount = isViaAndCdma() ? 3 : MAX_SEND_RETRIES;
                Rlog.d(TAG, "handleSendComplete: Skipping retry:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS= " + this.mPhone.getServiceState().getState());
            }
            if (isIms() || ss == 0) {
                if (((CommandException) ar.exception).getCommandError() == CommandException.Error.SMS_FAIL_RETRY) {
                    if (tracker.mRetryCount < (isViaAndCdma() ? 3 : MAX_SEND_RETRIES)) {
                        tracker.mRetryCount++;
                        sendMessageDelayed(obtainMessage(3, tracker), 2000);
                        Rlog.d(TAG, "handleSendComplete: retry for Message  mRetryCount=" + tracker.mRetryCount + " mMessageRef=" + tracker.mMessageRef);
                        return;
                    }
                }
                int errorCode = 0;
                if (ar.result != null) {
                    errorCode = ((SmsResponse) ar.result).mErrorCode;
                }
                int error = 1;
                if (((CommandException) ar.exception).getCommandError() == CommandException.Error.FDN_CHECK_FAILURE) {
                    error = 6;
                }
                tracker.onFailed(this.mContext, error, errorCode);
                HwTelephonyFactory.getHwInnerSmsManager().report(this.mContext, 1311, "sms send fail:" + errorCode, tracker.mSubId);
            } else {
                tracker.onFailed(this.mContext, getNotInServiceError(ss), 0);
            }
        }
        Rlog.d(TAG, "handleSendComplete send for next message");
        sendSmsSendingTimeOutMessageDelayed(tracker);
    }

    protected static void handleNotInService(int ss, PendingIntent sentIntent) {
        if (sentIntent == null) {
            return;
        }
        if (ss == 3) {
            try {
                sentIntent.send(2);
            } catch (PendingIntent.CanceledException e) {
                Rlog.e(TAG, "Failed to send result");
            }
        } else {
            sentIntent.send(4);
        }
    }

    protected static int getNotInServiceError(int ss) {
        if (ss == 3) {
            return 2;
        }
        return 4;
    }

    /* access modifiers changed from: protected */
    public void sendData(String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        SmsMessageBase.SubmitPduBase pdu = getSubmitPdu(scAddr, destAddr, destPort, data, deliveryIntent != null);
        if (pdu != null) {
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, destPort, data, pdu), sentIntent, deliveryIntent, getFormat(), null, false, null, false, true);
            if (!sendSmsByCarrierApp(true, tracker)) {
                sendSubmitPdu(tracker);
            }
            PendingIntent pendingIntent = sentIntent;
            return;
        }
        Rlog.e(TAG, "SMSDispatcher.sendData(): getSubmitPdu() returned null");
        triggerSentIntentForFailure(sentIntent);
    }

    public void sendText(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        Rlog.d(TAG, "sendText");
        this.mCallingPackage = callingPkg;
        SmsMessageBase.SubmitPduBase pdu = getSubmitPdu(scAddr, destAddr, text, deliveryIntent != null, null, priority, validityPeriod);
        if (pdu != null) {
            String str = text;
            SmsMessageBase.SubmitPduBase submitPduBase = pdu;
            SmsTracker tracker = getSmsTracker(getSmsTrackerMap(destAddr, scAddr, str, pdu), sentIntent, deliveryIntent, getFormat(), messageUri, expectMore, str, true, persistMessage, priority, validityPeriod);
            if (!sendSmsByCarrierApp(false, tracker)) {
                sendSubmitPdu(tracker);
            }
            PendingIntent pendingIntent = sentIntent;
            return;
        }
        Rlog.e(TAG, "SmsDispatcher.sendText(): getSubmitPdu() returned null");
        triggerSentIntentForFailure(sentIntent);
    }

    private void triggerSentIntentForFailure(PendingIntent sentIntent) {
        if (sentIntent != null) {
            try {
                sentIntent.send(1);
            } catch (PendingIntent.CanceledException e) {
                Rlog.e(TAG, "Intent has been canceled!");
            }
        }
    }

    private boolean sendSmsByCarrierApp(boolean isDataSms, SmsTracker tracker) {
        SmsSender smsSender;
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage == null) {
            return false;
        }
        Rlog.d(TAG, "Found carrier package.");
        if (isDataSms) {
            smsSender = new DataSmsSender(tracker);
        } else {
            smsSender = new TextSmsSender(tracker);
        }
        smsSender.sendSmsByCarrierApp(carrierPackage, new SmsSenderCallback(smsSender));
        return true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a8  */
    public void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage, int priority, boolean expectMore, int validityPeriod) {
        boolean z;
        PendingIntent sentIntent;
        SMSDispatcher sMSDispatcher = this;
        ArrayList<String> arrayList = parts;
        ArrayList<PendingIntent> arrayList2 = sentIntents;
        ArrayList<PendingIntent> arrayList3 = deliveryIntents;
        String fullMessageText = sMSDispatcher.getMultipartMessageText(arrayList);
        int refNumber = getNextConcatenatedRef() & 255;
        int msgCount = parts.size();
        sMSDispatcher.mCallingPackage = callingPkg;
        GsmAlphabet.TextEncodingDetails[] encodingForParts = new GsmAlphabet.TextEncodingDetails[msgCount];
        int i = 0;
        int encoding = 0;
        int i2 = 0;
        while (true) {
            z = true;
            if (i2 >= msgCount) {
                break;
            }
            GsmAlphabet.TextEncodingDetails details = sMSDispatcher.calculateLength(arrayList.get(i2), false);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == 1)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i2] = details;
            i2++;
        }
        SmsTracker[] trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(false);
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 >= msgCount) {
                break;
            }
            SmsHeader.ConcatRef concatRef = new SmsHeader.ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i4 + 1;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = z;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == z) {
                smsHeader.languageTable = encodingForParts[i4].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i4].languageShiftTable;
            }
            if (arrayList2 != null) {
                SmsHeader.ConcatRef concatRef2 = concatRef;
                if (sentIntents.size() > i4) {
                    sentIntent = arrayList2.get(i4);
                    PendingIntent deliveryIntent = null;
                    if (arrayList3 != null && deliveryIntents.size() > i4) {
                        deliveryIntent = arrayList3.get(i4);
                    }
                    PendingIntent deliveryIntent2 = deliveryIntent;
                    String str = arrayList.get(i4);
                    int i5 = i4;
                    int encoding2 = encoding;
                    SmsHeader smsHeader2 = smsHeader;
                    int msgCount2 = msgCount;
                    boolean z2 = i4 != msgCount + -1;
                    SmsTracker[] trackers2 = trackers;
                    trackers2[i5] = sMSDispatcher.getNewSubmitPduTracker(destAddr, scAddr, str, smsHeader, encoding2, sentIntent, deliveryIntent2, z2, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, expectMore, validityPeriod);
                    boolean unused = trackers2[i5].mPersistMessage = persistMessage;
                    i3 = i5 + 1;
                    sMSDispatcher = this;
                    arrayList2 = sentIntents;
                    arrayList3 = deliveryIntents;
                    String str2 = callingPkg;
                    trackers = trackers2;
                    z = true;
                    refNumber = refNumber;
                    encoding = encoding2;
                    i = 0;
                    encodingForParts = encodingForParts;
                    msgCount = msgCount2;
                    arrayList = parts;
                }
            }
            sentIntent = null;
            PendingIntent deliveryIntent3 = null;
            deliveryIntent3 = arrayList3.get(i4);
            PendingIntent deliveryIntent22 = deliveryIntent3;
            String str3 = arrayList.get(i4);
            int i52 = i4;
            int encoding22 = encoding;
            SmsHeader smsHeader22 = smsHeader;
            int msgCount22 = msgCount;
            boolean z22 = i4 != msgCount + -1;
            SmsTracker[] trackers22 = trackers;
            trackers22[i52] = sMSDispatcher.getNewSubmitPduTracker(destAddr, scAddr, str3, smsHeader, encoding22, sentIntent, deliveryIntent22, z22, unsentPartCount, anyPartFailed, messageUri, fullMessageText, priority, expectMore, validityPeriod);
            boolean unused2 = trackers22[i52].mPersistMessage = persistMessage;
            i3 = i52 + 1;
            sMSDispatcher = this;
            arrayList2 = sentIntents;
            arrayList3 = deliveryIntents;
            String str22 = callingPkg;
            trackers = trackers22;
            z = true;
            refNumber = refNumber;
            encoding = encoding22;
            i = 0;
            encodingForParts = encodingForParts;
            msgCount = msgCount22;
            arrayList = parts;
        }
        SmsTracker[] trackers3 = trackers;
        int i6 = encoding;
        int i7 = i;
        GsmAlphabet.TextEncodingDetails[] textEncodingDetailsArr = encodingForParts;
        int i8 = msgCount;
        int i9 = refNumber;
        boolean z3 = persistMessage;
        ArrayList<String> arrayList4 = parts;
        if (arrayList4 == null || trackers3.length == 0 || trackers3[i7] == null) {
            Rlog.e(TAG, "Cannot send multipart text. parts=" + arrayList4 + " trackers=" + trackers3);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender smsSender = new MultipartSmsSender(arrayList4, trackers3);
            smsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(smsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            int length = trackers3.length;
            for (int i10 = i7; i10 < length; i10++) {
                SmsTracker tracker = trackers3[i10];
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    private SmsTracker getNewSubmitPduTracker(String destinationAddress, String scAddress, String message, SmsHeader smsHeader, int encoding, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean lastPart, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, String fullMessageText, int priority, boolean expectMore, int validityPeriod) {
        String str = destinationAddress;
        String str2 = scAddress;
        String str3 = message;
        SmsHeader smsHeader2 = smsHeader;
        boolean z = false;
        if (isCdmaMo()) {
            UserData uData = new UserData();
            uData.payloadStr = str3;
            uData.userDataHeader = smsHeader2;
            if (encoding == 1) {
                uData.msgEncoding = 2;
            } else {
                uData.msgEncoding = 4;
            }
            uData.msgEncodingSet = true;
            SmsMessage.SubmitPdu submitPdu = SmsMessage.getSubmitPdu(str, uData, deliveryIntent != null && lastPart, priority);
            HashMap<String, Object> smsTrackerMap = getSmsTrackerMap(str, str2, str3, submitPdu);
            String format = getFormat();
            if (!lastPart || expectMore) {
                z = true;
            }
            SmsMessage.SubmitPdu submitPdu2 = submitPdu;
            UserData userData = uData;
            return getSmsTracker(smsTrackerMap, sentIntent, deliveryIntent, format, unsentPartCount, anyPartFailed, messageUri, smsHeader2, z, fullMessageText, true, true, priority, validityPeriod);
        }
        SmsHeader smsHeader3 = smsHeader;
        SmsMessage.SubmitPdu submitPdu3 = com.android.internal.telephony.gsm.SmsMessage.getSubmitPdu(scAddress, destinationAddress, message, deliveryIntent != null, SmsHeader.toByteArray(smsHeader), encoding, smsHeader3.languageTable, smsHeader3.languageShiftTable, validityPeriod);
        if (submitPdu3 != null) {
            HashMap<String, Object> smsTrackerMap2 = getSmsTrackerMap(destinationAddress, scAddress, message, submitPdu3);
            String format2 = getFormat();
            if (!lastPart || expectMore) {
                z = true;
            }
            SmsMessage.SubmitPdu submitPdu4 = submitPdu3;
            return getSmsTracker(smsTrackerMap2, sentIntent, deliveryIntent, format2, unsentPartCount, anyPartFailed, messageUri, smsHeader3, z, fullMessageText, true, false, priority, validityPeriod);
        }
        SmsMessage.SubmitPdu submitPdu5 = submitPdu3;
        Rlog.e(TAG, "GsmSMSDispatcher.sendNewSubmitPdu(): getSubmitPdu() returned null");
        return null;
    }

    @VisibleForTesting
    public void sendRawPdu(SmsTracker tracker) {
        byte[] pdu = (byte[]) tracker.getData().get(MAP_KEY_PDU);
        if (!HwTelephonyFactory.getHwInnerSmsManager().isMatchSMSPattern(tracker.mDestAddress, "outgoing_sms_exception_pattern", tracker.mSubId) && HwTelephonyFactory.getHwInnerSmsManager().isMatchSMSPattern(tracker.mDestAddress, "outgoing_sms_restriction_pattern", tracker.mSubId)) {
            Rlog.d(TAG, "Addressin black list");
            sendMessage(obtainMessage(17));
            sendMessage(obtainMessage(7, tracker));
        } else if (this.mSmsSendDisabled) {
            Rlog.e(TAG, "Device does not support sending sms.");
            tracker.onFailed(this.mContext, 4, 0);
        } else if (this.mHwCust != null && this.mHwCust.isBlockMsgSending(this.mPhone.getSubId())) {
            Rlog.e(TAG, "Non AIS card does not support sending sms.");
            tracker.onFailed(this.mContext, 4, 0);
            sendMessage(obtainMessage(7, tracker));
        } else if (pdu == null) {
            Rlog.e(TAG, "Empty PDU");
            tracker.onFailed(this.mContext, 3, 0);
        } else {
            PackageManager pm = this.mContext.getPackageManager();
            String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
            if (packageNames == null || packageNames.length == 0) {
                Rlog.e(TAG, "Can't get calling app package name: refusing to send SMS");
                tracker.onFailed(this.mContext, 1, 0);
                return;
            }
            try {
                PackageInfo appInfo = pm.getPackageInfoAsUser(packageNames[0], 64, tracker.mUserId);
                if (checkDestination(tracker)) {
                    if (!this.mSmsDispatchersController.getUsageMonitor().check(appInfo.packageName, 1)) {
                        sendMessage(obtainMessage(4, tracker));
                        return;
                    }
                    sendSms(tracker);
                }
                if (PhoneNumberUtils.isLocalEmergencyNumber(this.mContext, tracker.mDestAddress)) {
                    new AsyncEmergencyContactNotifier(this.mContext).execute(new Void[0]);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Rlog.e(TAG, "Can't get calling app package info: refusing to send SMS");
                tracker.onFailed(this.mContext, 1, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkDestination(SmsTracker tracker) {
        boolean shouldIgnoreNotify;
        int event;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.SEND_SMS_NO_CONFIRMATION") == 0) {
            return true;
        }
        int rule = this.mPremiumSmsRule.get();
        int smsCategory = 0;
        String simMccmnc = this.mTelephonyManager.getSimOperator(getSubId());
        if (rule == 1 || rule == 3) {
            String simCountryIso = this.mTelephonyManager.getSimCountryIso(getSubId());
            if (simCountryIso == null || simCountryIso.length() != 2) {
                Rlog.e(TAG, "Can't get SIM country Iso: trying network country Iso");
                simCountryIso = this.mTelephonyManager.getNetworkCountryIso(getSubId());
            }
            smsCategory = this.mSmsDispatchersController.getUsageMonitor().checkDestinationHw(tracker.mDestAddress, simCountryIso, simMccmnc);
        }
        if (rule == 2 || rule == 3) {
            String networkCountryIso = this.mTelephonyManager.getNetworkCountryIso(getSubId());
            if (networkCountryIso == null || networkCountryIso.length() != 2) {
                Rlog.e(TAG, "Can't get Network country Iso: trying SIM country Iso");
                networkCountryIso = this.mTelephonyManager.getSimCountryIso(getSubId());
            }
            smsCategory = SmsUsageMonitor.mergeShortCodeCategories(smsCategory, this.mSmsDispatchersController.getUsageMonitor().checkDestinationHw(tracker.mDestAddress, networkCountryIso, simMccmnc));
        }
        if (smsCategory == 0 || smsCategory == 1 || smsCategory == 2) {
            return true;
        }
        if (Settings.Global.getInt(this.mResolver, "device_provisioned", 0) == 0) {
            Rlog.e(TAG, "Can't send premium sms during Setup Wizard");
            return false;
        }
        int premiumSmsPermission = this.mSmsDispatchersController.getUsageMonitor().getPremiumSmsPermission(tracker.getAppPackageName());
        if (premiumSmsPermission == 0) {
            premiumSmsPermission = 1;
        }
        switch (premiumSmsPermission) {
            case 2:
                Rlog.w(TAG, "User denied this app from sending to premium SMS");
                Message msg = obtainMessage(7, tracker);
                msg.arg1 = 0;
                msg.arg2 = 1;
                sendMessage(msg);
                return false;
            case 3:
                Rlog.d(TAG, "User approved this app to send to premium SMS");
                return true;
            default:
                if (HwTelephonyFactory.getHwInnerSmsManager().getTipPremiumFromConfig(this.mPhone.getPhoneId()) || !this.mSmsDispatchersController.getUsageMonitor().isCurrentPatternMatcherNull()) {
                    shouldIgnoreNotify = false;
                } else {
                    shouldIgnoreNotify = true;
                }
                boolean shouldIgnoreNotifyByHplmn = checkCustIgnoreShortCodeTips();
                if (shouldIgnoreNotify || shouldIgnoreNotifyByHplmn) {
                    Rlog.w(TAG, "flag.tip_premium is false, not notify premium msg.");
                    return true;
                }
                if (smsCategory == 3) {
                    event = 8;
                } else {
                    event = 9;
                }
                sendMessage(obtainMessage(event, tracker));
                return false;
        }
    }

    private boolean denyIfQueueLimitReached(SmsTracker tracker) {
        if (this.mPendingTrackerCount >= 5) {
            Rlog.e(TAG, "Denied because queue limit reached");
            tracker.onFailed(this.mContext, 5, 0);
            return true;
        }
        this.mPendingTrackerCount++;
        return false;
    }

    private CharSequence getAppLabel(String appPackage, int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return pm.getApplicationInfoAsUser(appPackage, 0, userId).loadSafeLabel(pm);
        } catch (PackageManager.NameNotFoundException e) {
            Rlog.e(TAG, "PackageManager Name Not Found for package " + appPackage);
            return appPackage;
        }
    }

    /* access modifiers changed from: protected */
    public void handleReachSentLimit(SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            CharSequence appLabel = getAppLabel(tracker.getAppPackageName(), tracker.mUserId);
            Resources r = Resources.getSystem();
            Spanned messageText = Html.fromHtml(r.getString(17041127, new Object[]{appLabel}));
            ConfirmDialogListener listener = new ConfirmDialogListener(tracker, null, 1);
            AlertDialog d = new AlertDialog.Builder(this.mContext, 33947691).setTitle(17041129).setIcon(17301642).setMessage(messageText).setPositiveButton(r.getString(17041130), listener).setNegativeButton(r.getString(17041128), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(2003);
            d.show();
        }
    }

    /* access modifiers changed from: protected */
    public void handleConfirmShortCode(boolean isPremium, SmsTracker tracker) {
        int detailsId;
        SmsTracker smsTracker = tracker;
        if (!denyIfQueueLimitReached(smsTracker)) {
            if (isPremium) {
                detailsId = 17041132;
            } else {
                detailsId = 17041138;
            }
            CharSequence appLabel = HwTelephonyFactory.getHwInnerSmsManager().getAppLabel(this.mContext, tracker.getAppPackageName(), tracker.mUserId, this);
            Resources r = Resources.getSystem();
            Spanned messageText = Html.fromHtml(r.getString(17041136, new Object[]{appLabel, smsTracker.mDestAddress}));
            View layout = ((LayoutInflater) new ContextThemeWrapper(this.mContext, r.getIdentifier("androidhwext:style/Theme.Emui", null, null)).getSystemService("layout_inflater")).inflate(17367293, null);
            ConfirmDialogListener listener = new ConfirmDialogListener(smsTracker, (TextView) layout.findViewById(16909352), 0);
            ((TextView) layout.findViewById(16909347)).setText(messageText);
            ((TextView) ((ViewGroup) layout.findViewById(16909348)).findViewById(16909349)).setText(detailsId);
            ((CheckBox) layout.findViewById(16909350)).setOnCheckedChangeListener(listener);
            AlertDialog d = new AlertDialog.Builder(this.mContext, 33947691).setView(layout).setPositiveButton(r.getString(17041133), listener).setNegativeButton(r.getString(17041135), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(2003);
            d.show();
            listener.setPositiveButton(d.getButton(-1));
            listener.setNegativeButton(d.getButton(-2));
        }
    }

    public void sendRetrySms(SmsTracker tracker) {
        if (this.mSmsDispatchersController != null) {
            this.mSmsDispatchersController.sendRetrySms(tracker);
            return;
        }
        Rlog.e(TAG, this.mSmsDispatchersController + " is null. Retry failed");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v3, resolved type: android.app.PendingIntent} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void sendMultipartSms(SmsTracker tracker) {
        SmsTracker smsTracker = tracker;
        HashMap<String, Object> map = tracker.getData();
        String destinationAddress = (String) map.get("destination");
        String scAddress = (String) map.get("scaddress");
        ArrayList<String> parts = (ArrayList) map.get("parts");
        ArrayList<PendingIntent> sentIntents = (ArrayList) map.get("sentIntents");
        ArrayList<PendingIntent> deliveryIntents = (ArrayList) map.get("deliveryIntents");
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            boolean access$400 = tracker.mPersistMessage;
            int i = smsTracker.mPriority;
            boolean z = smsTracker.mExpectMore;
            int i2 = i;
            int i3 = ss;
            ArrayList<PendingIntent> arrayList = sentIntents;
            boolean z2 = z;
            ArrayList<String> arrayList2 = parts;
            sendMultipartText(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, null, null, access$400, i2, z2, smsTracker.mValidityPeriod);
            return;
        }
        int count = parts.size();
        for (int i4 = 0; i4 < count; i4++) {
            PendingIntent sentIntent = null;
            if (sentIntents != null && sentIntents.size() > i4) {
                sentIntent = sentIntents.get(i4);
            }
            handleNotInService(ss, sentIntent);
        }
    }

    /* access modifiers changed from: protected */
    public SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, int priority, int validityPeriod) {
        PackageInfo appInfo;
        PackageManager pm = this.mContext.getPackageManager();
        String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
        String processName = HwTelephonyFactory.getHwInnerSmsManager().getAppNameByPid(Binder.getCallingPid(), this.mContext);
        int userId = UserHandle.getCallingUserId();
        if (packageNames != null && packageNames.length > 0) {
            try {
                appInfo = HwTelephonyFactory.getHwInnerSmsManager().getPackageInfoByPid(pm.getPackageInfoAsUser(packageNames[0], 64, userId), pm, packageNames, this.mContext);
            } catch (PackageManager.NameNotFoundException e) {
            }
            if ("com.huawei.systemmanager:service".equals(processName) && appInfo != null) {
                Rlog.d(TAG, "mCallingPackage " + this.mCallingPackage);
                appInfo.packageName = this.mCallingPackage;
            }
            if (!(appInfo == null || appInfo.packageName == null)) {
                this.mPackageSendSmsCount.add(appInfo.packageName);
            }
            HashMap<String, Object> hashMap = data;
            SmsTracker smsTracker = new SmsTracker(hashMap, sentIntent, deliveryIntent, appInfo, PhoneNumberUtils.extractNetworkPortion((String) hashMap.get(MAP_KEY_DEST_ADDR)), format, unsentPartCount, anyPartFailed, messageUri, smsHeader, expectMore, fullMessageText, getSubId(), isText, persistMessage, userId, priority, validityPeriod);
            return smsTracker;
        }
        appInfo = null;
        Rlog.d(TAG, "mCallingPackage " + this.mCallingPackage);
        appInfo.packageName = this.mCallingPackage;
        this.mPackageSendSmsCount.add(appInfo.packageName);
        HashMap<String, Object> hashMap2 = data;
        SmsTracker smsTracker2 = new SmsTracker(hashMap2, sentIntent, deliveryIntent, appInfo, PhoneNumberUtils.extractNetworkPortion((String) hashMap2.get(MAP_KEY_DEST_ADDR)), format, unsentPartCount, anyPartFailed, messageUri, smsHeader, expectMore, fullMessageText, getSubId(), isText, persistMessage, userId, priority, validityPeriod);
        return smsTracker2;
    }

    /* access modifiers changed from: protected */
    public SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage) {
        return getSmsTracker(data, sentIntent, deliveryIntent, format, null, null, messageUri, null, expectMore, fullMessageText, isText, persistMessage, -1, -1);
    }

    /* access modifiers changed from: protected */
    public SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean expectMore, String fullMessageText, boolean isText, boolean persistMessage, int priority, int validityPeriod) {
        return getSmsTracker(data, sentIntent, deliveryIntent, format, null, null, messageUri, null, expectMore, fullMessageText, isText, persistMessage, priority, validityPeriod);
    }

    /* access modifiers changed from: protected */
    public HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, String text, SmsMessageBase.SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(MAP_KEY_DEST_ADDR, destAddr);
        map.put(MAP_KEY_SC_ADDR, scAddr);
        map.put(MAP_KEY_TEXT, text);
        map.put(MAP_KEY_SMSC, pdu.encodedScAddress);
        map.put(MAP_KEY_PDU, pdu.encodedMessage);
        return map;
    }

    /* access modifiers changed from: protected */
    public HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, int destPort, byte[] data, SmsMessageBase.SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(MAP_KEY_DEST_ADDR, destAddr);
        map.put(MAP_KEY_SC_ADDR, scAddr);
        map.put(MAP_KEY_DEST_PORT, Integer.valueOf(destPort));
        map.put(MAP_KEY_DATA, data);
        map.put(MAP_KEY_SMSC, pdu.encodedScAddress);
        map.put(MAP_KEY_PDU, pdu.encodedMessage);
        return map;
    }

    public boolean isIms() {
        if (this.mSmsDispatchersController != null) {
            return this.mSmsDispatchersController.isIms();
        }
        Rlog.e(TAG, "mSmsDispatchersController  is null");
        return false;
    }

    private String getMultipartMessageText(ArrayList<String> parts) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = parts.iterator();
        while (it.hasNext()) {
            String part = it.next();
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public String getCarrierAppPackageName() {
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card == null) {
            return null;
        }
        List<String> carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierPackages == null || carrierPackages.size() != 1) {
            return CarrierSmsUtils.getCarrierImsPackageForIntent(this.mContext, this.mPhone, new Intent("android.service.carrier.CarrierMessagingService"));
        }
        return carrierPackages.get(0);
    }

    /* access modifiers changed from: protected */
    public int getSubId() {
        return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhone.getPhoneId());
    }

    /* access modifiers changed from: private */
    public void checkCallerIsPhoneOrCarrierApp() {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1001 && uid != 0) {
            try {
                if (!UserHandle.isSameApp(this.mContext.getPackageManager().getApplicationInfo(getCarrierAppPackageName(), 0).uid, Binder.getCallingUid())) {
                    throw new SecurityException("Caller is not phone or carrier app!");
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new SecurityException("Caller is not phone or carrier app!");
            }
        }
    }

    public static int getMaxSendRetriesHw() {
        return MAX_SEND_RETRIES;
    }

    public static int getEventSendRetryHw() {
        return 3;
    }

    /* access modifiers changed from: protected */
    public boolean isCdmaMo() {
        return this.mSmsDispatchersController.isCdmaMo();
    }

    public ArraySet<String> getPackageSendSmsCount() {
        return this.mPackageSendSmsCount;
    }

    public void clearPackageSendSmsCount() {
        this.mPackageSendSmsCount.clear();
    }

    public CharSequence getAppLabelHw(String appPackage, int userId) {
        return getAppLabel(appPackage, userId);
    }
}

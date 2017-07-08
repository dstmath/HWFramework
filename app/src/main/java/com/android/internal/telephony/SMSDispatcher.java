package com.android.internal.telephony;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.Sms.Sent;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.service.carrier.ICarrierMessagingCallback.Stub;
import android.service.carrier.ICarrierMessagingService;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.ArraySet;
import android.util.EventLog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmAlphabet.TextEncodingDetails;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.internal.telephony.HwRadarUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SMSDispatcher extends AbstractSMSDispatcher {
    private static final String CONTACTS_PACKAGE_NAME = "com.android.contacts";
    static final boolean DBG = false;
    private static final String DEFAULT_SMS_APPLICATION = "com.android.mms";
    private static final int EVENT_CONFIRM_SEND_TO_POSSIBLE_PREMIUM_SHORT_CODE = 8;
    private static final int EVENT_CONFIRM_SEND_TO_PREMIUM_SHORT_CODE = 9;
    protected static final int EVENT_HANDLE_STATUS_REPORT = 10;
    protected static final int EVENT_ICC_CHANGED = 15;
    protected static final int EVENT_IMS_STATE_CHANGED = 12;
    protected static final int EVENT_IMS_STATE_DONE = 13;
    protected static final int EVENT_NEW_ICC_SMS = 14;
    protected static final int EVENT_RADIO_ON = 11;
    static final int EVENT_SEND_CONFIRMED_SMS = 5;
    private static final int EVENT_SEND_LIMIT_REACHED_CONFIRMATION = 4;
    private static final int EVENT_SEND_RETRY = 3;
    protected static final int EVENT_SEND_SMS_COMPLETE = 2;
    private static final int EVENT_SEND_SMS_OVERLOAD = 20;
    static final int EVENT_STOP_SENDING = 7;
    private static final String HW_CSP_PACKAGE = "com.android.contacts";
    private static final float MAX_LABEL_SIZE_PX = 500.0f;
    private static final int MAX_SEND_RETRIES = 1;
    private static final int MAX_SEND_RETRIES_FOR_VIA = 3;
    private static final String MESSAGE_STRING_NAME = "app_label";
    private static final int MO_MSG_QUEUE_LIMIT = 5;
    private static final int PREMIUM_RULE_USE_BOTH = 3;
    private static final int PREMIUM_RULE_USE_NETWORK = 2;
    private static final int PREMIUM_RULE_USE_SIM = 1;
    private static final String RESOURCE_TYPE_STRING = "string";
    private static final String SEND_NEXT_MSG_EXTRA = "SendNextMsg";
    private static final int SEND_RETRY_DELAY = 2000;
    private static final int SEND_SMS_OVERlOAD_COUNT = 50;
    private static final int SEND_SMS_OVERlOAD_DURATION_TIMEOUT = 3600000;
    private static final int SINGLE_PART_SMS = 1;
    static final String TAG = "SMSDispatcher";
    private static final boolean TIP_PREMIUM_SHORT_CODE = false;
    private static int sConcatenatedRef;
    protected final ArrayList<SmsTracker> deliveryPendingList;
    protected AtomicInteger mAlreadySentSms;
    protected final CommandsInterface mCi;
    protected final Context mContext;
    private ImsSMSDispatcher mImsSMSDispatcher;
    private final ArraySet<String> mPackageSendSmsCount;
    private int mPendingTrackerCount;
    protected Phone mPhone;
    private final AtomicInteger mPremiumSmsRule;
    protected final ContentResolver mResolver;
    private final SettingsObserver mSettingsObserver;
    protected boolean mSmsCapable;
    protected boolean mSmsSendDisabled;
    protected final TelephonyManager mTelephonyManager;
    private SmsUsageMonitor mUsageMonitor;

    protected final class ConfirmDialogListener implements OnClickListener, OnCancelListener, OnCheckedChangeListener {
        private Button mNegativeButton;
        private Button mPositiveButton;
        private boolean mRememberChoice;
        private final TextView mRememberUndoInstruction;
        private final SmsTracker mTracker;

        ConfirmDialogListener(SmsTracker tracker, TextView textView) {
            this.mTracker = tracker;
            this.mRememberUndoInstruction = textView;
        }

        void setPositiveButton(Button button) {
            this.mPositiveButton = button;
        }

        void setNegativeButton(Button button) {
            this.mNegativeButton = button;
        }

        public void onClick(DialogInterface dialog, int which) {
            int i = -1;
            int newSmsPermission = SMSDispatcher.SINGLE_PART_SMS;
            if (which == -1) {
                Rlog.d(SMSDispatcher.TAG, "CONFIRM sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent(EventLogTags.EXP_DET_SMS_SENT_BY_USER, i);
                SMSDispatcher.this.sendMessage(SMSDispatcher.this.obtainMessage(SMSDispatcher.MO_MSG_QUEUE_LIMIT, this.mTracker));
                if (this.mRememberChoice) {
                    newSmsPermission = SMSDispatcher.PREMIUM_RULE_USE_BOTH;
                }
            } else if (which == -2) {
                Rlog.d(SMSDispatcher.TAG, "DENY sending SMS");
                if (this.mTracker.mAppInfo.applicationInfo != null) {
                    i = this.mTracker.mAppInfo.applicationInfo.uid;
                }
                EventLog.writeEvent(EventLogTags.EXP_DET_SMS_DENIED_BY_USER, i);
                SMSDispatcher.this.sendMessage(SMSDispatcher.this.obtainMessage(SMSDispatcher.EVENT_STOP_SENDING, this.mTracker));
                if (this.mRememberChoice) {
                    newSmsPermission = SMSDispatcher.PREMIUM_RULE_USE_NETWORK;
                }
            }
            SMSDispatcher.this.setPremiumSmsPermission(this.mTracker.mAppInfo.packageName, newSmsPermission);
        }

        public void onCancel(DialogInterface dialog) {
            Rlog.d(SMSDispatcher.TAG, "dialog dismissed: don't send SMS");
            SMSDispatcher.this.sendMessage(SMSDispatcher.this.obtainMessage(SMSDispatcher.EVENT_STOP_SENDING, this.mTracker));
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Rlog.d(SMSDispatcher.TAG, "remember this choice: " + isChecked);
            this.mRememberChoice = isChecked;
            if (isChecked) {
                this.mPositiveButton.setText(17040362);
                this.mNegativeButton.setText(17040363);
                if (this.mRememberUndoInstruction != null) {
                    this.mRememberUndoInstruction.setText(17040361);
                    this.mRememberUndoInstruction.setPadding(0, 0, 0, 32);
                    return;
                }
                return;
            }
            this.mPositiveButton.setText(17040358);
            this.mNegativeButton.setText(17040359);
            if (this.mRememberUndoInstruction != null) {
                this.mRememberUndoInstruction.setText("");
                this.mRememberUndoInstruction.setPadding(0, 0, 0, 0);
            }
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
            if (bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
                return;
            }
            Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
            this.mSenderCallback.onSendSmsComplete(SMSDispatcher.SINGLE_PART_SMS, 0);
        }
    }

    protected final class DataSmsSender extends SmsSender {
        public DataSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            HashMap<String, Object> map = this.mTracker.getData();
            byte[] data = (byte[]) map.get("data");
            int destPort = ((Integer) map.get("destPort")).intValue();
            if (data != null) {
                try {
                    carrierMessagingService.sendDataSms(data, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, destPort, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                    return;
                } catch (RemoteException e) {
                    Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(SMSDispatcher.SINGLE_PART_SMS, 0);
                    return;
                }
            }
            this.mSenderCallback.onSendSmsComplete(SMSDispatcher.SINGLE_PART_SMS, 0);
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

        void sendSmsByCarrierApp(String carrierPackageName, MultipartSmsSenderCallback senderCallback) {
            this.mSenderCallback = senderCallback;
            if (bindToCarrierMessagingService(SMSDispatcher.this.mContext, carrierPackageName)) {
                Rlog.d(SMSDispatcher.TAG, "bindService() for carrier messaging service succeeded");
                return;
            }
            Rlog.e(SMSDispatcher.TAG, "bindService() for carrier messaging service failed");
            this.mSenderCallback.onSendMultipartSmsComplete(SMSDispatcher.SINGLE_PART_SMS, null);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                carrierMessagingService.sendMultipartTextSms(this.mParts, SMSDispatcher.this.getSubId(), this.mTrackers[0].mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTrackers[0].mDeliveryIntent), this.mSenderCallback);
            } catch (RemoteException e) {
                Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                this.mSenderCallback.onSendMultipartSmsComplete(SMSDispatcher.SINGLE_PART_SMS, null);
            }
        }
    }

    private final class MultipartSmsSenderCallback extends Stub {
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
                    i += SMSDispatcher.SINGLE_PART_SMS;
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
            onChange(SMSDispatcher.DBG);
        }

        public void onChange(boolean selfChange) {
            this.mPremiumSmsRule.set(Global.getInt(this.mContext.getContentResolver(), "sms_short_code_rule", SMSDispatcher.SINGLE_PART_SMS));
        }
    }

    protected final class SmsSenderCallback extends Stub {
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
        private boolean mIsText;
        public int mMessageRef;
        public Uri mMessageUri;
        private boolean mPersistMessage;
        public int mRetryCount;
        public final PendingIntent mSentIntent;
        public final SmsHeader mSmsHeader;
        private int mSubId;
        private long mTimestamp;
        private AtomicInteger mUnsentPartCount;

        private SmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, PackageInfo appInfo, String destAddr, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, int subId, boolean isText, boolean persistMessage) {
            this.mTimestamp = System.currentTimeMillis();
            this.mData = data;
            this.mSentIntent = sentIntent;
            this.mDeliveryIntent = deliveryIntent;
            this.mRetryCount = 0;
            this.mAppInfo = appInfo;
            this.mDestAddress = destAddr;
            this.mFormat = format;
            this.mExpectMore = isExpectMore;
            this.mImsRetry = 0;
            this.mMessageRef = 0;
            this.mUnsentPartCount = unsentPartCount;
            this.mAnyPartFailed = anyPartFailed;
            this.mMessageUri = messageUri;
            this.mSmsHeader = smsHeader;
            this.mFullMessageText = fullMessageText;
            this.mSubId = subId;
            this.mIsText = isText;
            this.mPersistMessage = persistMessage;
        }

        boolean isMultipart() {
            return this.mData.containsKey("parts");
        }

        public HashMap<String, Object> getData() {
            return this.mData;
        }

        public void updateSentMessageStatus(Context context, int status) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(SMSDispatcher.SINGLE_PART_SMS);
                values.put(TelephonyEventLog.DATA_KEY_DATA_CALL_STATUS, Integer.valueOf(status));
                SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null);
            }
        }

        private void updateMessageState(Context context, int messageType, int errorCode) {
            if (this.mMessageUri != null) {
                ContentValues values = new ContentValues(SMSDispatcher.PREMIUM_RULE_USE_NETWORK);
                values.put(TelephonyEventLog.DATA_KEY_DATA_CALL_TYPE, Integer.valueOf(messageType));
                values.put(TextBasedSmsColumns.ERROR_CODE, Integer.valueOf(errorCode));
                long identity = Binder.clearCallingIdentity();
                try {
                    if (SqliteWrapper.update(context, context.getContentResolver(), this.mMessageUri, values, null, null) != SMSDispatcher.SINGLE_PART_SMS) {
                        Rlog.e(SMSDispatcher.TAG, "Failed to move message to " + messageType);
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Uri persistSentMessageIfRequired(Context context, int messageType, int errorCode) {
            if (!this.mIsText || !this.mPersistMessage || (this.mAppInfo != null && !HwTelephonyFactory.getHwInnerSmsManager().checkShouldWriteSmsPackage(this.mAppInfo.packageName, context))) {
                return null;
            }
            Rlog.d(SMSDispatcher.TAG, "Persist SMS into " + (messageType == SMSDispatcher.MO_MSG_QUEUE_LIMIT ? "FAILED" : "SENT"));
            ContentValues values = new ContentValues();
            values.put(TextBasedSmsColumns.SUBSCRIPTION_ID, Integer.valueOf(this.mSubId));
            values.put(TextBasedSmsColumns.ADDRESS, this.mDestAddress);
            values.put(TextBasedSmsColumns.BODY, this.mFullMessageText);
            values.put(ThreadsColumns.DATE, Long.valueOf(System.currentTimeMillis()));
            values.put(SmsManager.MESSAGE_STATUS_SEEN, Integer.valueOf(SMSDispatcher.SINGLE_PART_SMS));
            values.put(SmsManager.MESSAGE_STATUS_READ, Integer.valueOf(SMSDispatcher.SINGLE_PART_SMS));
            String creator = this.mAppInfo != null ? this.mAppInfo.packageName : null;
            if (!TextUtils.isEmpty(creator)) {
                values.put(TextBasedSmsColumns.CREATOR, creator);
            }
            if (this.mDeliveryIntent != null) {
                values.put(TelephonyEventLog.DATA_KEY_DATA_CALL_STATUS, Integer.valueOf(32));
            }
            if (errorCode != 0) {
                values.put(TextBasedSmsColumns.ERROR_CODE, Integer.valueOf(errorCode));
            }
            long identity = Binder.clearCallingIdentity();
            ContentResolver resolver = context.getContentResolver();
            try {
                Uri uri = resolver.insert(Sent.CONTENT_URI, values);
                if (uri != null && messageType == SMSDispatcher.MO_MSG_QUEUE_LIMIT) {
                    ContentValues updateValues = new ContentValues(SMSDispatcher.SINGLE_PART_SMS);
                    updateValues.put(TelephonyEventLog.DATA_KEY_DATA_CALL_TYPE, Integer.valueOf(SMSDispatcher.MO_MSG_QUEUE_LIMIT));
                    resolver.update(uri, updateValues, null, null);
                }
                Binder.restoreCallingIdentity(identity);
                return uri;
            } catch (Exception e) {
                Rlog.e(SMSDispatcher.TAG, "writeOutboxMessage: Failed to persist outbox message", e);
                return null;
            } catch (Throwable th) {
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
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0 ? true : SMSDispatcher.DBG;
            }
            if (isSinglePartOrLastPart) {
                persistOrUpdateMessage(context, SMSDispatcher.MO_MSG_QUEUE_LIMIT, errorCode);
            }
            if (this.mSentIntent != null) {
                try {
                    Intent fillIn = new Intent();
                    if (this.mMessageUri != null) {
                        fillIn.putExtra("uri", this.mMessageUri.toString());
                    }
                    if (errorCode != 0) {
                        fillIn.putExtra(TelephonyEventLog.DATA_KEY_SMS_ERROR_CODE, errorCode);
                    }
                    if (this.mUnsentPartCount != null && isSinglePartOrLastPart) {
                        fillIn.putExtra(SMSDispatcher.SEND_NEXT_MSG_EXTRA, true);
                    }
                    this.mSentIntent.send(context, error, fillIn);
                } catch (CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }

        public void onSent(Context context) {
            boolean isSinglePartOrLastPart = true;
            if (this.mUnsentPartCount != null) {
                isSinglePartOrLastPart = this.mUnsentPartCount.decrementAndGet() == 0 ? true : SMSDispatcher.DBG;
            }
            if (isSinglePartOrLastPart) {
                int messageType = SMSDispatcher.PREMIUM_RULE_USE_NETWORK;
                if (this.mAnyPartFailed != null && this.mAnyPartFailed.get()) {
                    messageType = SMSDispatcher.MO_MSG_QUEUE_LIMIT;
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
                } catch (CanceledException e) {
                    Rlog.e(SMSDispatcher.TAG, "Failed to send result");
                }
            }
        }
    }

    protected final class TextSmsSender extends SmsSender {
        public TextSmsSender(SmsTracker tracker) {
            super(tracker);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            String text = (String) this.mTracker.getData().get(Part.TEXT);
            if (text != null) {
                try {
                    carrierMessagingService.sendTextSms(text, SMSDispatcher.this.getSubId(), this.mTracker.mDestAddress, SMSDispatcher.getSendSmsFlag(this.mTracker.mDeliveryIntent), this.mSenderCallback);
                    return;
                } catch (RemoteException e) {
                    Rlog.e(SMSDispatcher.TAG, "Exception sending the SMS: " + e);
                    this.mSenderCallback.onSendSmsComplete(SMSDispatcher.SINGLE_PART_SMS, 0);
                    return;
                }
            }
            this.mSenderCallback.onSendSmsComplete(SMSDispatcher.SINGLE_PART_SMS, 0);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SMSDispatcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SMSDispatcher.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SMSDispatcher.<clinit>():void");
    }

    protected abstract TextEncodingDetails calculateLength(CharSequence charSequence, boolean z);

    protected abstract String getFormat();

    protected abstract SmsTracker getNewSubmitPduTracker(String str, String str2, String str3, SmsHeader smsHeader, int i, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z, AtomicInteger atomicInteger, AtomicBoolean atomicBoolean, Uri uri, String str4);

    protected abstract void injectSmsPdu(byte[] bArr, String str, PendingIntent pendingIntent);

    protected abstract void sendData(String str, String str2, int i, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2);

    protected abstract void sendSms(SmsTracker smsTracker);

    protected abstract void sendSmsByPstn(SmsTracker smsTracker);

    protected abstract void sendSubmitPdu(SmsTracker smsTracker);

    protected abstract void sendText(String str, String str2, String str3, PendingIntent pendingIntent, PendingIntent pendingIntent2, Uri uri, String str4, boolean z);

    protected void triggerSendSmsOverLoadCheck() {
        this.mAlreadySentSms.getAndIncrement();
        if (SINGLE_PART_SMS == this.mAlreadySentSms.get()) {
            removeMessages(EVENT_SEND_SMS_OVERLOAD);
            sendMessageDelayed(obtainMessage(EVENT_SEND_SMS_OVERLOAD), 3600000);
        }
    }

    private void handleSendSmsOverLoad() {
        if (this.mAlreadySentSms.get() > SEND_SMS_OVERlOAD_COUNT) {
            boolean findThirdApp = DBG;
            String defaultSmsApplicationName = "";
            StringBuffer buf = new StringBuffer();
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, DBG);
            if (componentName != null) {
                defaultSmsApplicationName = componentName.getPackageName();
            }
            buf.append(defaultSmsApplicationName);
            buf.append(" SendSmsOverLoad");
            for (int i = 0; i < this.mPackageSendSmsCount.size(); i += SINGLE_PART_SMS) {
                String packageName = (String) this.mPackageSendSmsCount.valueAt(i);
                if (!(defaultSmsApplicationName.equals(packageName) || DEFAULT_SMS_APPLICATION.equals(packageName))) {
                    buf.append(" ");
                    buf.append(packageName);
                    findThirdApp = true;
                }
            }
            if (findThirdApp) {
                HwRadarUtils.report(this.mContext, HwRadarUtils.ERR_SMS_SEND_BACKGROUND, buf.toString(), getSubId());
            }
        }
        this.mAlreadySentSms.set(0);
        this.mPackageSendSmsCount.clear();
    }

    protected static int getNextConcatenatedRef() {
        sConcatenatedRef += SINGLE_PART_SMS;
        return sConcatenatedRef;
    }

    protected SMSDispatcher(Phone phone, SmsUsageMonitor usageMonitor, ImsSMSDispatcher imsSMSDispatcher) {
        this.mPremiumSmsRule = new AtomicInteger(SINGLE_PART_SMS);
        this.mSmsCapable = true;
        this.mAlreadySentSms = new AtomicInteger(0);
        this.mPackageSendSmsCount = new ArraySet();
        this.deliveryPendingList = new ArrayList();
        this.mPhone = phone;
        this.mImsSMSDispatcher = imsSMSDispatcher;
        this.mContext = phone.getContext();
        this.mResolver = this.mContext.getContentResolver();
        this.mCi = phone.mCi;
        this.mUsageMonitor = usageMonitor;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSettingsObserver = new SettingsObserver(this, this.mPremiumSmsRule, this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("sms_short_code_rule"), DBG, this.mSettingsObserver);
        this.mSmsCapable = this.mContext.getResources().getBoolean(17956959);
        this.mSmsSendDisabled = this.mTelephonyManager.getSmsSendCapableForPhone(this.mPhone.getPhoneId(), this.mSmsCapable) ? DBG : true;
        Rlog.d(TAG, "SMSDispatcher: ctor mSmsCapable=" + this.mSmsCapable + " format=" + getFormat() + " mSmsSendDisabled=" + this.mSmsSendDisabled);
    }

    protected void updatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mUsageMonitor = phone.mSmsUsageMonitor;
        Rlog.d(TAG, "Active phone changed to " + this.mPhone.getPhoneName());
    }

    public void dispose() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
    }

    protected void handleStatusReport(Object o) {
        Rlog.d(TAG, "handleStatusReport() called with no subclass.");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PREMIUM_RULE_USE_NETWORK /*2*/:
                handleSendComplete((AsyncResult) msg.obj);
            case PREMIUM_RULE_USE_BOTH /*3*/:
                Rlog.d(TAG, "SMS retry..");
                sendSmsImmediately((SmsTracker) msg.obj);
            case EVENT_SEND_LIMIT_REACHED_CONFIRMATION /*4*/:
                handleReachSentLimit((SmsTracker) msg.obj);
            case MO_MSG_QUEUE_LIMIT /*5*/:
                SmsTracker tracker = msg.obj;
                if (tracker.isMultipart()) {
                    sendMultipartSms(tracker);
                } else {
                    if (this.mPendingTrackerCount > SINGLE_PART_SMS) {
                        tracker.mExpectMore = true;
                    } else {
                        tracker.mExpectMore = DBG;
                    }
                    sendSms(tracker);
                }
                this.mPendingTrackerCount--;
            case EVENT_STOP_SENDING /*7*/:
                ((SmsTracker) msg.obj).onFailed(this.mContext, MO_MSG_QUEUE_LIMIT, 0);
                this.mPendingTrackerCount--;
            case EVENT_CONFIRM_SEND_TO_POSSIBLE_PREMIUM_SHORT_CODE /*8*/:
                handleConfirmShortCode(DBG, (SmsTracker) msg.obj);
            case EVENT_CONFIRM_SEND_TO_PREMIUM_SHORT_CODE /*9*/:
                handleConfirmShortCode(true, (SmsTracker) msg.obj);
            case EVENT_HANDLE_STATUS_REPORT /*10*/:
                handleStatusReport(msg.obj);
            case EVENT_SEND_SMS_OVERLOAD /*20*/:
                handleSendSmsOverLoad();
            default:
                Rlog.e(TAG, "handleMessage() ignoring message of unexpected type " + msg.what);
        }
    }

    private static int getSendSmsFlag(PendingIntent deliveryIntent) {
        if (deliveryIntent == null) {
            return 0;
        }
        return SINGLE_PART_SMS;
    }

    private void processSendSmsResponse(SmsTracker tracker, int result, int messageRef) {
        if (tracker == null) {
            Rlog.e(TAG, "processSendSmsResponse: null tracker");
            return;
        }
        SmsResponse smsResponse = new SmsResponse(messageRef, null, -1);
        switch (result) {
            case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                Rlog.d(TAG, "Sending SMS by IP succeeded.");
                sendMessage(obtainMessage(PREMIUM_RULE_USE_NETWORK, new AsyncResult(tracker, smsResponse, null)));
                break;
            case SINGLE_PART_SMS /*1*/:
                Rlog.d(TAG, "Sending SMS by IP failed. Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
            case PREMIUM_RULE_USE_NETWORK /*2*/:
                Rlog.d(TAG, "Sending SMS by IP failed.");
                sendMessage(obtainMessage(PREMIUM_RULE_USE_NETWORK, new AsyncResult(tracker, smsResponse, new CommandException(Error.GENERIC_FAILURE))));
                break;
            default:
                Rlog.d(TAG, "Unknown result " + result + " Retry on carrier network.");
                sendSubmitPdu(tracker);
                break;
        }
    }

    protected void handleSendComplete(AsyncResult ar) {
        int i = SINGLE_PART_SMS;
        SmsTracker tracker = ar.userObj;
        PendingIntent sentIntent = tracker.mSentIntent;
        if (ar.result != null) {
            tracker.mMessageRef = ((SmsResponse) ar.result).mMessageRef;
        } else {
            Rlog.d(TAG, "SmsResponse was null");
        }
        if (ar.exception == null) {
            if (tracker.mDeliveryIntent != null) {
                this.deliveryPendingList.add(tracker);
            }
            tracker.onSent(this.mContext);
        } else {
            int ss = this.mPhone.getServiceState().getState();
            if (tracker.mImsRetry > 0 && ss != 0) {
                tracker.mRetryCount = isViaAndCdma() ? PREMIUM_RULE_USE_BOTH : SINGLE_PART_SMS;
                Rlog.d(TAG, "handleSendComplete: Skipping retry:  isIms()=" + isIms() + " mRetryCount=" + tracker.mRetryCount + " mImsRetry=" + tracker.mImsRetry + " mMessageRef=" + tracker.mMessageRef + " SS= " + this.mPhone.getServiceState().getState());
            }
            if (isIms() || ss == 0) {
                if (((CommandException) ar.exception).getCommandError() == Error.SMS_FAIL_RETRY) {
                    int i2 = tracker.mRetryCount;
                    if (isViaAndCdma()) {
                        i = PREMIUM_RULE_USE_BOTH;
                    }
                    if (i2 < i) {
                        tracker.mRetryCount += SINGLE_PART_SMS;
                        sendMessageDelayed(obtainMessage(PREMIUM_RULE_USE_BOTH, tracker), 2000);
                        Rlog.d(TAG, "handleSendComplete: retry for Message  mRetryCount=" + tracker.mRetryCount + " mMessageRef=" + tracker.mMessageRef);
                        return;
                    }
                }
                int errorCode = 0;
                if (ar.result != null) {
                    errorCode = ((SmsResponse) ar.result).mErrorCode;
                }
                int error = SINGLE_PART_SMS;
                if (((CommandException) ar.exception).getCommandError() == Error.FDN_CHECK_FAILURE) {
                    error = 6;
                }
                tracker.onFailed(this.mContext, error, errorCode);
                HwRadarUtils.report(this.mContext, HwRadarUtils.ERR_SMS_SEND, "sms send fail:" + errorCode, tracker.mSubId);
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
        if (ss == PREMIUM_RULE_USE_BOTH) {
            try {
                sentIntent.send(PREMIUM_RULE_USE_NETWORK);
                return;
            } catch (CanceledException e) {
                return;
            }
        }
        sentIntent.send(EVENT_SEND_LIMIT_REACHED_CONFIRMATION);
    }

    protected static int getNotInServiceError(int ss) {
        if (ss == PREMIUM_RULE_USE_BOTH) {
            return PREMIUM_RULE_USE_NETWORK;
        }
        return EVENT_SEND_LIMIT_REACHED_CONFIRMATION;
    }

    protected void sendMultipartText(String destAddr, String scAddr, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, Uri messageUri, String callingPkg, boolean persistMessage) {
        int i;
        String fullMessageText = getMultipartMessageText(parts);
        int refNumber = getNextConcatenatedRef() & PduHeaders.STORE_STATUS_ERROR_END;
        int msgCount = parts.size();
        int encoding = 0;
        TextEncodingDetails[] encodingForParts = new TextEncodingDetails[msgCount];
        for (i = 0; i < msgCount; i += SINGLE_PART_SMS) {
            TextEncodingDetails details = calculateLength((CharSequence) parts.get(i), DBG);
            if (encoding != details.codeUnitSize && (encoding == 0 || encoding == SINGLE_PART_SMS)) {
                encoding = details.codeUnitSize;
            }
            encodingForParts[i] = details;
        }
        Object trackers = new SmsTracker[msgCount];
        AtomicInteger unsentPartCount = new AtomicInteger(msgCount);
        AtomicBoolean anyPartFailed = new AtomicBoolean(DBG);
        i = 0;
        while (i < msgCount) {
            ConcatRef concatRef = new ConcatRef();
            concatRef.refNumber = refNumber;
            concatRef.seqNumber = i + SINGLE_PART_SMS;
            concatRef.msgCount = msgCount;
            concatRef.isEightBits = true;
            SmsHeader smsHeader = new SmsHeader();
            smsHeader.concatRef = concatRef;
            if (encoding == SINGLE_PART_SMS) {
                smsHeader.languageTable = encodingForParts[i].languageTable;
                smsHeader.languageShiftTable = encodingForParts[i].languageShiftTable;
            }
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            PendingIntent pendingIntent2 = null;
            if (deliveryIntents != null && deliveryIntents.size() > i) {
                pendingIntent2 = (PendingIntent) deliveryIntents.get(i);
            }
            trackers[i] = getNewSubmitPduTracker(destAddr, scAddr, (String) parts.get(i), smsHeader, encoding, pendingIntent, pendingIntent2, i == msgCount + -1 ? true : DBG, unsentPartCount, anyPartFailed, messageUri, fullMessageText);
            trackers[i].mPersistMessage = persistMessage;
            i += SINGLE_PART_SMS;
        }
        if (parts == null || trackers == null || trackers.length == 0 || trackers[0] == null) {
            Rlog.e(TAG, "Cannot send multipart text. parts=" + parts + " trackers=" + trackers);
            return;
        }
        String carrierPackage = getCarrierAppPackageName();
        if (carrierPackage != null) {
            Rlog.d(TAG, "Found carrier package.");
            MultipartSmsSender multipartSmsSender = new MultipartSmsSender(parts, trackers);
            multipartSmsSender.sendSmsByCarrierApp(carrierPackage, new MultipartSmsSenderCallback(multipartSmsSender));
        } else {
            Rlog.v(TAG, "No carrier package.");
            int length = trackers.length;
            for (int i2 = 0; i2 < length; i2 += SINGLE_PART_SMS) {
                SmsTracker tracker = trackers[i2];
                if (tracker != null) {
                    sendSubmitPdu(tracker);
                } else {
                    Rlog.e(TAG, "Null tracker.");
                }
            }
        }
    }

    protected void sendRawPdu(SmsTracker tracker) {
        byte[] pdu = (byte[]) tracker.getData().get("pdu");
        if (this.mSmsSendDisabled) {
            Rlog.e(TAG, "Device does not support sending sms.");
            tracker.onFailed(this.mContext, EVENT_SEND_LIMIT_REACHED_CONFIRMATION, 0);
        } else if (pdu == null) {
            Rlog.e(TAG, "Empty PDU");
            tracker.onFailed(this.mContext, PREMIUM_RULE_USE_BOTH, 0);
        } else {
            PackageManager pm = this.mContext.getPackageManager();
            String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
            if (packageNames == null || packageNames.length == 0) {
                Rlog.e(TAG, "Can't get calling app package name: refusing to send SMS");
                tracker.onFailed(this.mContext, SINGLE_PART_SMS, 0);
                return;
            }
            try {
                PackageInfo appInfo = pm.getPackageInfo(packageNames[0], 64);
                if (checkDestination(tracker)) {
                    if (this.mUsageMonitor.check(appInfo.packageName, SINGLE_PART_SMS)) {
                        sendSms(tracker);
                    } else {
                        sendMessage(obtainMessage(EVENT_SEND_LIMIT_REACHED_CONFIRMATION, tracker));
                        return;
                    }
                }
                if (PhoneNumberUtils.isLocalEmergencyNumber(this.mContext, tracker.mDestAddress)) {
                    new AsyncEmergencyContactNotifier(this.mContext).execute(new Void[0]);
                }
            } catch (NameNotFoundException e) {
                Rlog.e(TAG, "Can't get calling app package info: refusing to send SMS");
                tracker.onFailed(this.mContext, SINGLE_PART_SMS, 0);
            }
        }
    }

    boolean checkDestination(SmsTracker tracker) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.SEND_SMS_NO_CONFIRMATION") == 0) {
            return true;
        }
        int rule = this.mPremiumSmsRule.get();
        int smsCategory = 0;
        if (rule == SINGLE_PART_SMS || rule == PREMIUM_RULE_USE_BOTH) {
            String simCountryIso = this.mTelephonyManager.getSimCountryIso();
            if (simCountryIso == null || simCountryIso.length() != PREMIUM_RULE_USE_NETWORK) {
                Rlog.e(TAG, "Can't get SIM country Iso: trying network country Iso");
                simCountryIso = this.mTelephonyManager.getNetworkCountryIso();
            }
            smsCategory = this.mUsageMonitor.checkDestinationHW(tracker.mDestAddress, simCountryIso);
        }
        if (rule == PREMIUM_RULE_USE_NETWORK || rule == PREMIUM_RULE_USE_BOTH) {
            String networkCountryIso = this.mTelephonyManager.getNetworkCountryIso();
            if (networkCountryIso == null || networkCountryIso.length() != PREMIUM_RULE_USE_NETWORK) {
                Rlog.e(TAG, "Can't get Network country Iso: trying SIM country Iso");
                networkCountryIso = this.mTelephonyManager.getSimCountryIso();
            }
            smsCategory = SmsUsageMonitor.mergeShortCodeCategories(smsCategory, this.mUsageMonitor.checkDestinationHW(tracker.mDestAddress, networkCountryIso));
        }
        if (smsCategory == 0 || smsCategory == SINGLE_PART_SMS || smsCategory == PREMIUM_RULE_USE_NETWORK) {
            return true;
        }
        if (Global.getInt(this.mResolver, "device_provisioned", 0) == 0) {
            Rlog.e(TAG, "Can't send premium sms during Setup Wizard");
            return DBG;
        }
        int premiumSmsPermission = this.mUsageMonitor.getPremiumSmsPermission(tracker.mAppInfo.packageName);
        if (premiumSmsPermission == 0) {
            premiumSmsPermission = SINGLE_PART_SMS;
        }
        switch (premiumSmsPermission) {
            case PREMIUM_RULE_USE_NETWORK /*2*/:
                Rlog.w(TAG, "User denied this app from sending to premium SMS");
                sendMessage(obtainMessage(EVENT_STOP_SENDING, tracker));
                return DBG;
            case PREMIUM_RULE_USE_BOTH /*3*/:
                Rlog.d(TAG, "User approved this app to send to premium SMS");
                return true;
            default:
                boolean shouldIgnoreNotify;
                if (TIP_PREMIUM_SHORT_CODE) {
                    shouldIgnoreNotify = DBG;
                } else {
                    shouldIgnoreNotify = this.mUsageMonitor.isCurrentPatternMatcherNull();
                }
                if (shouldIgnoreNotify) {
                    Rlog.w(TAG, "flag.tip_premium is false, not notify premium msg.");
                    return true;
                }
                int event;
                if (smsCategory == PREMIUM_RULE_USE_BOTH) {
                    event = EVENT_CONFIRM_SEND_TO_POSSIBLE_PREMIUM_SHORT_CODE;
                } else {
                    event = EVENT_CONFIRM_SEND_TO_PREMIUM_SHORT_CODE;
                }
                sendMessage(obtainMessage(event, tracker));
                return DBG;
        }
    }

    private boolean denyIfQueueLimitReached(SmsTracker tracker) {
        if (this.mPendingTrackerCount >= MO_MSG_QUEUE_LIMIT) {
            Rlog.e(TAG, "Denied because queue limit reached");
            tracker.onFailed(this.mContext, MO_MSG_QUEUE_LIMIT, 0);
            return true;
        }
        this.mPendingTrackerCount += SINGLE_PART_SMS;
        return DBG;
    }

    private CharSequence getAppLabel(String appPackage) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return convertSafeLabel(pm.getApplicationInfo(appPackage, 0).loadLabel(pm).toString(), appPackage);
        } catch (NameNotFoundException e) {
            Rlog.e(TAG, "PackageManager Name Not Found for package " + appPackage);
            return appPackage;
        }
    }

    private CharSequence getMsgAppLabel(String appPackage) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            int resId = pm.getResourcesForApplication(appPackage).getIdentifier(MESSAGE_STRING_NAME, RESOURCE_TYPE_STRING, appPackage);
            CharSequence lmsgApplabel = null;
            if (resId != 0) {
                lmsgApplabel = pm.getText(appPackage, resId, null);
            }
            if (resId == 0 || lmsgApplabel == null) {
                return getAppLabel(appPackage);
            }
            return lmsgApplabel;
        } catch (NameNotFoundException e) {
            Rlog.e(TAG, "PackageManager Name Not Found for package" + appPackage);
            return appPackage;
        }
    }

    private CharSequence convertSafeLabel(String labelStr, String appPackage) {
        int labelLength = labelStr.length();
        int offset = 0;
        while (offset < labelLength) {
            int codePoint = labelStr.codePointAt(offset);
            int type = Character.getType(codePoint);
            if (type == EVENT_IMS_STATE_DONE || type == EVENT_ICC_CHANGED || type == EVENT_NEW_ICC_SMS) {
                labelStr = labelStr.substring(0, offset);
                break;
            }
            if (type == EVENT_IMS_STATE_CHANGED) {
                labelStr = labelStr.substring(0, offset) + " " + labelStr.substring(Character.charCount(codePoint) + offset);
            }
            offset += Character.charCount(codePoint);
        }
        labelStr = labelStr.trim();
        if (labelStr.isEmpty()) {
            return appPackage;
        }
        TextPaint paint = new TextPaint();
        paint.setTextSize(42.0f);
        return TextUtils.ellipsize(labelStr, paint, MAX_LABEL_SIZE_PX, TruncateAt.END);
    }

    protected void handleReachSentLimit(SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            CharSequence appLabel = getAppLabel(tracker.mAppInfo.packageName);
            Resources r = Resources.getSystem();
            Object[] objArr = new Object[SINGLE_PART_SMS];
            objArr[0] = appLabel;
            Spanned messageText = Html.fromHtml(r.getString(17040352, objArr));
            ConfirmDialogListener listener = new ConfirmDialogListener(tracker, null);
            AlertDialog d = new Builder(this.mContext, 33947691).setTitle(17040351).setIcon(17301642).setMessage(messageText).setPositiveButton(r.getString(17040353), listener).setNegativeButton(r.getString(17040354), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(TelephonyEventLog.TAG_IMS_CALL_RECEIVE);
            d.show();
        }
    }

    protected void handleConfirmShortCode(boolean isPremium, SmsTracker tracker) {
        if (!denyIfQueueLimitReached(tracker)) {
            int detailsId;
            CharSequence appLabel;
            if (isPremium) {
                detailsId = 17040357;
            } else {
                detailsId = 17040356;
            }
            if (HW_CSP_PACKAGE.equals(tracker.mAppInfo.packageName)) {
                appLabel = getMsgAppLabel(tracker.mAppInfo.packageName);
            } else {
                appLabel = getAppLabel(tracker.mAppInfo.packageName);
            }
            Resources r = Resources.getSystem();
            CharSequence[] charSequenceArr = new Object[PREMIUM_RULE_USE_NETWORK];
            charSequenceArr[0] = appLabel;
            charSequenceArr[SINGLE_PART_SMS] = tracker.mDestAddress;
            Spanned messageText = Html.fromHtml(r.getString(17040355, charSequenceArr));
            int themeID = r.getIdentifier("androidhwext:style/Theme.Emui", null, null);
            String str = "layout_inflater";
            View layout = ((LayoutInflater) new ContextThemeWrapper(this.mContext, themeID).getSystemService(r17)).inflate(17367271, null);
            ConfirmDialogListener listener = new ConfirmDialogListener(tracker, (TextView) layout.findViewById(16909315));
            ((TextView) layout.findViewById(16909309)).setText(messageText);
            ((TextView) ((ViewGroup) layout.findViewById(16909310)).findViewById(16909312)).setText(detailsId);
            ((CheckBox) layout.findViewById(16909313)).setOnCheckedChangeListener(listener);
            AlertDialog d = new Builder(this.mContext, 33947691).setView(layout).setPositiveButton(r.getString(17040358), listener).setNegativeButton(r.getString(17040359), listener).setOnCancelListener(listener).create();
            d.getWindow().setType(TelephonyEventLog.TAG_IMS_CALL_RECEIVE);
            d.show();
            listener.setPositiveButton(d.getButton(-1));
            listener.setNegativeButton(d.getButton(-2));
        }
    }

    public int getPremiumSmsPermission(String packageName) {
        return this.mUsageMonitor.getPremiumSmsPermission(packageName);
    }

    public void setPremiumSmsPermission(String packageName, int permission) {
        this.mUsageMonitor.setPremiumSmsPermission(packageName, permission);
    }

    public void sendRetrySms(SmsTracker tracker) {
        if (this.mImsSMSDispatcher != null) {
            this.mImsSMSDispatcher.sendRetrySms(tracker);
        } else {
            Rlog.e(TAG, this.mImsSMSDispatcher + " is null. Retry failed");
        }
    }

    private void sendMultipartSms(SmsTracker tracker) {
        HashMap<String, Object> map = tracker.getData();
        String destinationAddress = (String) map.get("destination");
        String scAddress = (String) map.get("scaddress");
        ArrayList<String> parts = (ArrayList) map.get("parts");
        ArrayList<PendingIntent> sentIntents = (ArrayList) map.get("sentIntents");
        ArrayList<PendingIntent> deliveryIntents = (ArrayList) map.get("deliveryIntents");
        int ss = this.mPhone.getServiceState().getState();
        if (isIms() || ss == 0) {
            sendMultipartText(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, null, null, tracker.mPersistMessage);
            return;
        }
        int i = 0;
        int count = parts.size();
        while (i < count) {
            PendingIntent pendingIntent = null;
            if (sentIntents != null && sentIntents.size() > i) {
                pendingIntent = (PendingIntent) sentIntents.get(i);
            }
            handleNotInService(ss, pendingIntent);
            i += SINGLE_PART_SMS;
        }
    }

    private int getDefaultIdxForCsp(String[] packageNames) {
        if (packageNames.length <= SINGLE_PART_SMS) {
            return 0;
        }
        for (int i = 0; i < packageNames.length; i += SINGLE_PART_SMS) {
            if (HW_CSP_PACKAGE.equals(packageNames[i])) {
                return i;
            }
        }
        return 0;
    }

    protected SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, AtomicInteger unsentPartCount, AtomicBoolean anyPartFailed, Uri messageUri, SmsHeader smsHeader, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage) {
        PackageManager pm = this.mContext.getPackageManager();
        String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
        PackageInfo packageInfo = null;
        if (packageNames != null && packageNames.length > 0) {
            try {
                int index = getDefaultIdxForCsp(packageNames);
                Rlog.d(TAG, "index =  " + index);
                packageInfo = pm.getPackageInfo(packageNames[index], 64);
                packageInfo = HwTelephonyFactory.getHwInnerSmsManager().getPackageInfoByPid(packageInfo, pm, packageNames, this.mContext);
            } catch (NameNotFoundException e) {
            }
        }
        if (!(packageInfo == null || packageInfo.packageName == null)) {
            this.mPackageSendSmsCount.add(packageInfo.packageName);
        }
        return new SmsTracker(sentIntent, deliveryIntent, packageInfo, PhoneNumberUtils.extractNetworkPortion((String) data.get("destAddr")), format, unsentPartCount, anyPartFailed, messageUri, smsHeader, isExpectMore, fullMessageText, getSubId(), isText, persistMessage, null);
    }

    protected SmsTracker getSmsTracker(HashMap<String, Object> data, PendingIntent sentIntent, PendingIntent deliveryIntent, String format, Uri messageUri, boolean isExpectMore, String fullMessageText, boolean isText, boolean persistMessage) {
        return getSmsTracker(data, sentIntent, deliveryIntent, format, null, null, messageUri, null, isExpectMore, fullMessageText, isText, persistMessage);
    }

    protected HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, String text, SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap();
        map.put("destAddr", destAddr);
        map.put("scAddr", scAddr);
        map.put(Part.TEXT, text);
        map.put("smsc", pdu.encodedScAddress);
        map.put("pdu", pdu.encodedMessage);
        return map;
    }

    protected HashMap<String, Object> getSmsTrackerMap(String destAddr, String scAddr, int destPort, byte[] data, SubmitPduBase pdu) {
        HashMap<String, Object> map = new HashMap();
        map.put("destAddr", destAddr);
        map.put("scAddr", scAddr);
        map.put("destPort", Integer.valueOf(destPort));
        map.put("data", data);
        map.put("smsc", pdu.encodedScAddress);
        map.put("pdu", pdu.encodedMessage);
        return map;
    }

    public boolean isIms() {
        if (this.mImsSMSDispatcher != null) {
            return this.mImsSMSDispatcher.isIms();
        }
        Rlog.e(TAG, this.mImsSMSDispatcher + " is null");
        return DBG;
    }

    public String getImsSmsFormat() {
        if (this.mImsSMSDispatcher != null) {
            return this.mImsSMSDispatcher.getImsSmsFormat();
        }
        Rlog.e(TAG, this.mImsSMSDispatcher + " is null");
        return null;
    }

    private String getMultipartMessageText(ArrayList<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    protected String getCarrierAppPackageName() {
        String str = null;
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card == null) {
            return null;
        }
        List<String> carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        if (carrierPackages != null && carrierPackages.size() == SINGLE_PART_SMS) {
            str = (String) carrierPackages.get(0);
        }
        return str;
    }

    protected int getSubId() {
        return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhone.getPhoneId());
    }

    private void checkCallerIsPhoneOrCarrierApp() {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != TelephonyEventLog.TAG_RIL_REQUEST && uid != 0) {
            try {
                if (!UserHandle.isSameApp(this.mContext.getPackageManager().getApplicationInfo(getCarrierAppPackageName(), 0).uid, Binder.getCallingUid())) {
                    throw new SecurityException("Caller is not phone or carrier app!");
                }
            } catch (NameNotFoundException e) {
                throw new SecurityException("Caller is not phone or carrier app!");
            }
        }
    }

    public static int getMaxSendRetriesHw() {
        return SINGLE_PART_SMS;
    }

    public static int getEventSendRetryHw() {
        return PREMIUM_RULE_USE_BOTH;
    }
}

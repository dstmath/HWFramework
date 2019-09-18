package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.BroadcastOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IDeviceIdleController;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.SettingsEx;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Jlog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CarrierServicesSmsFilter;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InboundSmsHandler extends StateMachine {
    /* access modifiers changed from: private */
    public static String ACTION_OPEN_SMS_APP = "com.android.internal.telephony.OPEN_DEFAULT_SMS_APP";
    public static final int ADDRESS_COLUMN = 6;
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    private static final int DELETE_PERMANENTLY = 1;
    public static final int DESTINATION_PORT_COLUMN = 2;
    public static final int DISPLAY_ADDRESS_COLUMN = 9;
    private static final int ERROR_BASE_MMS = 1300;
    static final int ERR_SMS_SEND = 1311;
    protected static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 8;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    private static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    private static final int EVENT_UPDATE_PHONE_OBJECT = 7;
    public static final int ID_COLUMN = 7;
    private static final String INCOMING_SMS_EXCEPTION_PATTERN = "incoming_sms_exception_pattern";
    private static final String INCOMING_SMS_RESTRICTION_PATTERN = "incoming_sms_restriction_pattern";
    private static final String INTERCEPTION_SMS_RECEIVED = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
    private static final int MARK_DELETED = 2;
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    private static final int NO_DESTINATION_PORT = -1;
    public static final int PDU_COLUMN = 0;
    private static final String[] PDU_PROJECTION = {"pdu"};
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = {"pdu", "sequence", "destination_port", "display_originating_addr"};
    private static final Map<Integer, Integer> PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING = new HashMap<Integer, Integer>() {
        {
            put(0, 0);
            put(1, 1);
            put(2, 2);
            put(9, 3);
        }
    };
    private static final String RECEIVE_SMS_PERMISSION = "huawei.permission.RECEIVE_SMS_INTERCEPTION";
    public static final int RECEIVE_TIME_COLUMN = 10;
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final int SEQUENCE_COLUMN = 1;
    private static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    protected static final Uri sRawUri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");
    protected static final Uri sRawUriPermanentDelete = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw/permanentDelete");
    private boolean isClass0 = false;
    protected CellBroadcastHandler mCellBroadcastHandler;
    protected final Context mContext;
    private final DefaultState mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public final DeliveringState mDeliveringState = new DeliveringState();
    IDeviceIdleController mDeviceIdleController;
    private HwCustInboundSmsHandler mHwCust;
    /* access modifiers changed from: private */
    public final IdleState mIdleState = new IdleState();
    protected Phone mPhone;
    /* access modifiers changed from: private */
    public final ContentResolver mResolver;
    private final boolean mSmsReceiveDisabled;
    private final StartupState mStartupState = new StartupState();
    protected SmsStorageMonitor mStorageMonitor;
    private UserManager mUserManager;
    /* access modifiers changed from: private */
    public final WaitingState mWaitingState = new WaitingState();
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;
    private int mWakeLockTimeout;
    private final WapPushOverSms mWapPush;

    private final class CarrierServicesSmsFilterCallback implements CarrierServicesSmsFilter.CarrierServicesSmsFilterCallbackInterface {
        private final int mDestPort;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private final String mSmsFormat;
        private final boolean mUserUnlocked;

        CarrierServicesSmsFilterCallback(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver, boolean userUnlocked) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
            this.mUserUnlocked = userUnlocked;
        }

        public void onFilterComplete(int result) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.logv("onFilterComplete: result is " + result);
            if ((result & 1) != 0) {
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (VisualVoicemailSmsFilter.filter(InboundSmsHandler.this.mContext, this.mPdus, this.mSmsFormat, this.mDestPort, InboundSmsHandler.this.mPhone.getSubId())) {
                InboundSmsHandler.this.log("Visual voicemail SMS dropped");
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (this.mUserUnlocked) {
                InboundSmsHandler.this.dispatchSmsDeliveryIntent(this.mPdus, this.mSmsFormat, this.mDestPort, this.mSmsBroadcastReceiver);
            } else {
                if (!InboundSmsHandler.this.isSkipNotifyFlagSet(result)) {
                    InboundSmsHandler.this.showNewMessageNotification();
                }
                InboundSmsHandler.this.sendMessage(3);
            }
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 7) {
                String errorText = "processMessage: unhandled message type " + msg.what + " currState=" + InboundSmsHandler.this.getCurrentState().getName();
                if (Build.IS_DEBUGGABLE) {
                    InboundSmsHandler.this.loge("---- Dumping InboundSmsHandler ----");
                    InboundSmsHandler.this.loge("Total records=" + InboundSmsHandler.this.getLogRecCount());
                    for (int i = Math.max(InboundSmsHandler.this.getLogRecSize() + -20, 0); i < InboundSmsHandler.this.getLogRecSize(); i++) {
                        if (InboundSmsHandler.this.getLogRec(i) != null) {
                            InboundSmsHandler.this.loge("Rec[%d]: %s\n" + i + InboundSmsHandler.this.getLogRec(i).toString());
                        }
                    }
                    InboundSmsHandler.this.loge("---- Dumped InboundSmsHandler ----");
                    throw new RuntimeException(errorText);
                }
                InboundSmsHandler.this.loge(errorText);
            } else {
                InboundSmsHandler.this.onUpdatePhoneObject((Phone) msg.obj);
            }
            return true;
        }
    }

    private class DeliveringState extends State {
        private DeliveringState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Delivering state");
        }

        public void exit() {
            InboundSmsHandler.this.log("leaving Delivering state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.log("DeliveringState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    InboundSmsHandler.this.handleNewSms((AsyncResult) msg.obj);
                    InboundSmsHandler.this.sendMessage(4);
                    return true;
                case 2:
                    if (InboundSmsHandler.this.processMessagePart((InboundSmsTracker) msg.obj)) {
                        InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mWaitingState);
                    } else {
                        InboundSmsHandler.this.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                        InboundSmsHandler.this.sendMessage(4);
                    }
                    return true;
                case 4:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (!InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.loge("mWakeLock released while delivering/broadcasting!");
                    }
                    return true;
                case 8:
                    InboundSmsHandler.this.handleInjectSms((AsyncResult) msg.obj);
                    InboundSmsHandler.this.sendMessage(4);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Idle state");
            InboundSmsHandler.this.sendMessageDelayed(5, (long) InboundSmsHandler.this.getWakeLockTimeout());
        }

        public void exit() {
            InboundSmsHandler.this.mWakeLock.acquire();
            InboundSmsHandler.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.log("IdleState.processMessage:" + msg.what);
            InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
            inboundSmsHandler2.log("Idle state processing message type " + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                    InboundSmsHandler.this.deferMessage(msg);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.log("mWakeLock is still held after release");
                    } else {
                        InboundSmsHandler.this.log("mWakeLock released");
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class NewMessageNotificationActionReceiver extends BroadcastReceiver {
        private NewMessageNotificationActionReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (InboundSmsHandler.ACTION_OPEN_SMS_APP.equals(intent.getAction())) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(Telephony.Sms.getDefaultSmsPackage(context)));
            }
        }
    }

    private final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano = System.nanoTime();
        /* access modifiers changed from: private */
        public final String mDeleteWhere;
        /* access modifiers changed from: private */
        public final String[] mDeleteWhereArgs;

        SmsBroadcastReceiver(InboundSmsTracker tracker) {
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
        }

        public void onReceive(Context context, Intent intent) {
            int rc;
            if (HwTelephonyFactory.getHwInnerSmsManager().shouldSendReceivedActionInPrivacyMode(InboundSmsHandler.this, context, intent, this.mDeleteWhere, this.mDeleteWhereArgs) && intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if ("android.provider.Telephony.SMS_DELIVER".equals(action)) {
                    intent.setAction("android.provider.Telephony.SMS_RECEIVED");
                    intent.addFlags(16777216);
                    intent.setComponent(null);
                    InboundSmsHandler.this.dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, InboundSmsHandler.this.handleSmsWhitelisting(null), this, UserHandle.ALL);
                } else if ("android.provider.Telephony.WAP_PUSH_DELIVER".equals(action)) {
                    intent.setAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                    intent.setComponent(null);
                    intent.addFlags(16777216);
                    Bundle options = null;
                    try {
                        long duration = InboundSmsHandler.this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(InboundSmsHandler.this.mContext.getPackageName(), 0, "mms-broadcast");
                        BroadcastOptions bopts = BroadcastOptions.makeBasic();
                        bopts.setTemporaryAppWhitelistDuration(duration);
                        options = bopts.toBundle();
                    } catch (RemoteException e) {
                    }
                    String mimeType = intent.getType();
                    InboundSmsHandler.this.dispatchIntent(intent, WapPushOverSms.getPermissionForType(mimeType), WapPushOverSms.getAppOpsPermissionForIntent(mimeType), options, this, UserHandle.SYSTEM);
                } else {
                    if (!"android.intent.action.DATA_SMS_RECEIVED".equals(action) && !"android.provider.Telephony.SMS_RECEIVED".equals(action) && !"android.intent.action.DATA_SMS_RECEIVED".equals(action) && !"android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
                        InboundSmsHandler.this.loge("unexpected BroadcastReceiver action: " + action);
                    }
                    if ("true".equals(SettingsEx.Systemex.getString(InboundSmsHandler.this.mResolver, "disableErrWhenReceiveSMS"))) {
                        rc = -1;
                    } else {
                        rc = getResultCode();
                    }
                    if (rc == -1 || rc == 1) {
                        InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                    } else {
                        InboundSmsHandler.this.loge("a broadcast receiver set the result code to " + rc + ", deleting from raw table anyway!");
                    }
                    InboundSmsHandler.this.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                    InboundSmsHandler.this.sendMessage(3);
                    int durationMillis = (int) ((System.nanoTime() - this.mBroadcastTimeNano) / 1000000);
                    if (durationMillis >= 5000) {
                        InboundSmsHandler.this.loge("Slow ordered broadcast completion time: " + durationMillis + " ms");
                    } else {
                        InboundSmsHandler.this.log("ordered broadcast completed in: " + durationMillis + " ms");
                    }
                    HwTelephonyFactory.getHwInnerSmsManager().reportSmsReceiveTimeout(InboundSmsHandler.this.mContext, durationMillis);
                }
            }
        }
    }

    private class StartupState extends State {
        private StartupState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Startup state");
            InboundSmsHandler.this.setWakeLockTimeout(0);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.log("StartupState.processMessage:" + msg.what);
            int i = msg.what;
            if (i != 6) {
                if (i != 8) {
                    switch (i) {
                        case 1:
                        case 2:
                            break;
                        default:
                            return false;
                    }
                }
                InboundSmsHandler.this.deferMessage(msg);
                return true;
            }
            InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
            return true;
        }
    }

    private class WaitingState extends State {
        private WaitingState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Waiting state");
        }

        public void exit() {
            InboundSmsHandler.this.log("exiting Waiting state");
            InboundSmsHandler.this.setWakeLockTimeout(InboundSmsHandler.WAKELOCK_TIMEOUT);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.log("WaitingState.processMessage:" + msg.what);
            switch (msg.what) {
                case 2:
                    InboundSmsHandler.this.deferMessage(msg);
                    return true;
                case 3:
                    InboundSmsHandler.this.sendMessage(4);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                default:
                    return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public abstract void acknowledgeLastIncomingSms(boolean z, int i, Message message);

    /* access modifiers changed from: protected */
    public abstract int dispatchMessageRadioSpecific(SmsMessageBase smsMessageBase);

    /* access modifiers changed from: protected */
    public abstract boolean is3gpp2();

    protected InboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        super(name);
        this.mContext = context;
        this.mStorageMonitor = storageMonitor;
        this.mPhone = phone;
        this.mCellBroadcastHandler = cellBroadcastHandler;
        this.mResolver = context.getContentResolver();
        this.mWapPush = HwTelephonyFactory.getHwInnerSmsManager().createHwWapPushOverSms(context);
        HwTelephonyFactory.getHwInnerSmsManager().createSmsInterceptionService(context);
        this.mSmsReceiveDisabled = !TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17957026));
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        HwTelephonyFactory.getHwInnerSmsManager().addInboxInsertObserver(this.mContext);
        this.mHwCust = (HwCustInboundSmsHandler) HwCustUtils.createObj(HwCustInboundSmsHandler.class, new Object[0]);
        if (this.mHwCust != null && this.mHwCust.isIQIEnable()) {
            this.mHwCust.createIQClient(this.mContext);
        }
        log("created InboundSmsHandler");
    }

    public void dispose() {
        quit();
    }

    public void updatePhoneObject(Phone phone) {
        sendMessage(7, phone);
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    /* access modifiers changed from: private */
    public void handleNewSms(AsyncResult ar) {
        int result;
        int result2;
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return;
        }
        Jlog.d(47, "JLID_RIL_RESPONSE_NEW_SMS");
        try {
            SmsMessage sms = (SmsMessage) ar.result;
            if (this.mHwCust == null || !this.mHwCust.isIQIEnable() || !this.mHwCust.isIQISms(sms)) {
                result2 = dispatchMessage(sms.mWrappedSmsMessage);
            } else {
                log("check SMS is true");
                result2 = 1;
            }
            result = HwTelephonyFactory.getHwInnerSmsManager().dealBlacklistSms(this.mContext, sms, result2);
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (result != -1) {
            boolean handled = true;
            if (result != 1) {
                handled = false;
            }
            if (!handled) {
                Jlog.d(50, "JL_DISPATCH_SMS_FAILED");
            }
            if (this.mHwCust == null || !this.mHwCust.isNotNotifyWappushEnabled(ar)) {
                notifyAndAcknowledgeLastIncomingSms(handled, result, null);
            } else {
                acknowledgeLastIncomingSms(handled, result, null);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleInjectSms(AsyncResult ar) {
        int result;
        int result2;
        SmsDispatchersController.SmsInjectionCallback callback = null;
        try {
            callback = (SmsDispatchersController.SmsInjectionCallback) ar.userObj;
            SmsMessage sms = (SmsMessage) ar.result;
            if (sms == null) {
                result2 = 2;
            } else {
                result2 = dispatchMessage(sms.mWrappedSmsMessage, false);
            }
            result = result2;
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (callback != null) {
            callback.onSmsInjectedResult(result);
        }
    }

    private int dispatchMessage(SmsMessageBase smsb, boolean filterRepeatedMessage) {
        if (smsb == null) {
            loge("dispatchSmsMessage: message is null");
            return 2;
        } else if (this.mSmsReceiveDisabled) {
            log("Received short message on device which doesn't support receiving SMS. Ignored.");
            return 1;
        } else if (this.mHwCust == null || !this.mHwCust.isBlockMsgReceive(this.mPhone.getSubId())) {
            boolean onlyCore = false;
            boolean filterRepeatedMessage2 = filterRepeatedMessage || HwTelephonyFactory.getHwInnerSmsManager().isIOTVersion();
            log("dispatchMessage, filterRepeatedMessage:" + filterRepeatedMessage2);
            if (!filterRepeatedMessage2 || !HwTelephonyFactory.getHwInnerSmsManager().hasSameSmsPdu(smsb.getPdu())) {
                try {
                    onlyCore = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
                } catch (RemoteException e) {
                }
                if (!onlyCore) {
                    return dispatchMessageRadioSpecific(smsb);
                }
                log("Received a short message in encrypted state. Rejecting.");
                return 2;
            }
            log("receive a duplicated SMS and abandon it.");
            return 1;
        } else {
            log("dispatchMessage, block receive sms for non AIS card.");
            return 1;
        }
    }

    /* access modifiers changed from: protected */
    public void onUpdatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mStorageMonitor = this.mPhone.mSmsStorageMonitor;
        log("onUpdatePhoneObject: phone=" + this.mPhone.getClass().getSimpleName());
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent("android.provider.Telephony.SMS_REJECTED");
            intent.putExtra("result", result);
            intent.addFlags(16777216);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    /* access modifiers changed from: protected */
    public int dispatchNormalMessage(SmsMessageBase sms) {
        int destPort;
        InboundSmsTracker tracker;
        SmsHeader smsHeader = sms.getUserDataHeader();
        boolean z = false;
        this.isClass0 = sms.getMessageClass() == SmsConstants.MessageClass.CLASS_0;
        int destPort2 = -1;
        Jlog.d(48, "JL_DISPATCH_NORMAL_SMS");
        if (smsHeader == null || smsHeader.concatRef == null) {
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort2 = smsHeader.portAddrs.destPort;
                log("destination port: " + destPort2);
            }
            destPort = destPort2;
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort2, is3gpp2(), false, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getMessageBody());
        } else {
            SmsHeader.ConcatRef concatRef = smsHeader.concatRef;
            SmsHeader.PortAddrs portAddrs = smsHeader.portAddrs;
            destPort = portAddrs != null ? portAddrs.destPort : -1;
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, false, sms.getMessageBody());
        }
        if (!SystemProperties.getBoolean("ro.config.comm_serv_tid_enable", false) || this.mHwCust == null) {
            SmsMessageBase smsMessageBase = sms;
        } else {
            if (this.mHwCust.dispatchMessageByDestPort(destPort, sms, this.mContext)) {
                return 1;
            }
        }
        log("dispatchNormalMessage and created tracker");
        if (tracker.getDestPort() == -1) {
            z = true;
        }
        return addTrackerToRawTableAndSendMessage(tracker, z);
    }

    /* access modifiers changed from: protected */
    public int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        int addTrackerToRawTable = addTrackerToRawTable(tracker, deDup);
        if (addTrackerToRawTable != 1) {
            return addTrackerToRawTable != 5 ? 2 : 1;
        }
        if (((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
            Jlog.d(50, "JL_DISPATCH_SMS_FAILED");
        } else {
            Jlog.d(49, "JL_SEND_BROADCAST_SMS");
        }
        sendMessage(2, tracker);
        return 1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: byte[]} */
    /* JADX WARNING: type inference failed for: r13v1, types: [byte[][], java.lang.Object[], java.io.Serializable] */
    /* JADX WARNING: type inference failed for: r13v11 */
    /* JADX WARNING: type inference failed for: r13v12 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x03e7  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x03ef  */
    public boolean processMessagePart(InboundSmsTracker tracker) {
        ? r13;
        boolean block;
        int destPort;
        int result;
        byte[] pdu;
        int cursorCount;
        InboundSmsTracker inboundSmsTracker = tracker;
        int i = 0;
        if (tracker.isOldMessageInRawTable()) {
            log("processMessagePart, ignore old message kept in raw table.");
            return false;
        }
        int messageCount = tracker.getMessageCount();
        int destPort2 = tracker.getDestPort();
        if (destPort2 == 2948 || !HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(false) || !HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, false)) {
            boolean block2 = false;
            if (messageCount <= 0) {
                loge("processMessagePart: returning false due to invalid message count " + messageCount);
                return false;
            }
            int i2 = 1;
            if (messageCount == 1) {
                byte[][] pdus = {tracker.getPdu()};
                destPort = destPort2;
                block = BlockChecker.isBlocked(this.mContext, tracker.getDisplayAddress(), null);
                r13 = pdus;
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mResolver.query(sRawUri, PDU_SEQUENCE_PORT_PROJECTION, tracker.getQueryForSegmentsSubId(), new String[]{tracker.getAddress(), Integer.toString(tracker.getReferenceNumber()), Integer.toString(tracker.getMessageCount()), String.valueOf(this.mPhone.getSubId())}, null);
                    log("processMessagePart, cursorCount: " + cursorCount + ", ref|seq/count(" + tracker.getReferenceNumber() + "|" + tracker.getSequenceNumber() + "/" + messageCount + ")");
                    if (cursorCount < messageCount) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    }
                    byte[][] pdus2 = new byte[messageCount][];
                    while (cursor.moveToNext()) {
                        try {
                            int index = cursor.getInt(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(i2)).intValue()) - tracker.getIndexOffset();
                            if (index < pdus2.length) {
                                if (index >= 0) {
                                    pdus2[index] = HexDump.hexStringToByteArray(cursor.getString(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(i)).intValue()));
                                    if (index == 0 && !cursor.isNull(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(2).intValue())) {
                                        int port = InboundSmsTracker.getRealDestPort(cursor.getInt(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(2).intValue()));
                                        if (port != -1) {
                                            destPort2 = port;
                                        }
                                    }
                                    if (!block2) {
                                        block2 = BlockChecker.isBlocked(this.mContext, cursor.getString(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(9).intValue()), null);
                                    }
                                    i = 0;
                                    i2 = 1;
                                }
                            }
                            loge(String.format("processMessagePart: invalid seqNumber = %d, messageCount = %d", new Object[]{Integer.valueOf(tracker.getIndexOffset() + index), Integer.valueOf(messageCount)}));
                            i = 0;
                            i2 = 1;
                        } catch (SQLException e) {
                            e = e;
                            int i3 = messageCount;
                            try {
                                loge("Can't access multipart SMS database", e);
                                if (cursor != null) {
                                }
                                return false;
                            } catch (Throwable th) {
                                e = th;
                            }
                        } catch (Throwable th2) {
                            e = th2;
                            int i4 = messageCount;
                            if (cursor != null) {
                            }
                            throw e;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    destPort = destPort2;
                    block = block2;
                    r13 = pdus2;
                } catch (SQLException e2) {
                    e = e2;
                    int i5 = messageCount;
                    loge("Can't access multipart SMS database", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return false;
                } catch (Throwable th3) {
                    e = th3;
                    int i6 = messageCount;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw e;
                }
            }
            List<byte[]> pduList = Arrays.asList(r13);
            if (pduList.size() == 0) {
            } else if (pduList.contains(null)) {
                int i7 = messageCount;
            } else {
                SmsBroadcastReceiver resultReceiver = new SmsBroadcastReceiver(inboundSmsTracker);
                if (!this.mUserManager.isUserUnlocked()) {
                    return processMessagePartWithUserLocked(inboundSmsTracker, r13, destPort, resultReceiver);
                }
                if (destPort == 2948) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    String oriAddress = null;
                    for (byte[] pdu2 : r13) {
                        if (!tracker.is3gpp2()) {
                            SmsMessage msg = SmsMessage.createFromPdu(pdu2, "3gpp");
                            if (msg != null) {
                                byte[] pdu3 = msg.getUserData();
                                oriAddress = msg.getOriginatingAddress();
                                pdu = pdu3;
                            } else {
                                loge("processMessagePart: SmsMessage.createFromPdu returned null");
                                return false;
                            }
                        } else {
                            pdu = pdu2;
                        }
                        output.write(pdu, 0, pdu.length);
                    }
                    if (this.mWapPush.isWapPushForMms(output.toByteArray(), this) && this.mPhone.mDcTracker.isRoamingPushDisabled() && this.mPhone.getServiceState().getDataRoaming()) {
                        deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                        return false;
                    }
                    this.mWapPush.saveSmsTracker(inboundSmsTracker);
                    if (this.mHwCust == null || !this.mHwCust.isIQIEnable() || !this.mHwCust.isIQIWapPush(output)) {
                        result = this.mWapPush.dispatchWapPdu(output.toByteArray(), oriAddress, resultReceiver, this, tracker.is3gpp2());
                    } else {
                        log("check WapPush is true");
                        result = -1;
                    }
                    log("dispatchWapPdu() returned " + result);
                    if (result == -1) {
                        return true;
                    }
                    deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                    return false;
                }
                if (HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(false) && destPort != 2948) {
                    HwTelephonyFactory.getHwInnerSmsManager().updateSmsUsedNum(this.mContext, false);
                }
                Intent intent = new Intent();
                intent.putExtra("pdus", r13);
                intent.putExtra("format", tracker.getFormat());
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
                log("putPhoneIdAndSubIdExtra" + this.mPhone.getPhoneId());
                if (-1 == destPort && !HwTelephonyFactory.getHwInnerSmsManager().isMatchSMSPattern(tracker.getAddress(), INCOMING_SMS_EXCEPTION_PATTERN, this.mPhone.getPhoneId()) && HwTelephonyFactory.getHwInnerSmsManager().isMatchSMSPattern(tracker.getAddress(), INCOMING_SMS_RESTRICTION_PATTERN, this.mPhone.getPhoneId())) {
                    deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                    log("forbid receive sms by mdm!");
                    return false;
                } else if (-1 != destPort || !block) {
                    if (-1 == destPort) {
                        int i8 = messageCount;
                        Intent intent2 = intent;
                        if (HwTelephonyFactory.getHwInnerSmsManager().newSmsShouldBeIntercepted(this.mContext, intent, this, tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), false)) {
                            Intent secretIntent = new Intent(intent2);
                            secretIntent.setAction(INTERCEPTION_SMS_RECEIVED);
                            dispatchIntent(secretIntent, RECEIVE_SMS_PERMISSION, 16, handleSmsWhitelisting(null), null, UserHandle.ALL);
                            return true;
                        }
                    } else {
                        Intent intent3 = intent;
                    }
                    if (!filterSms(r13, destPort, inboundSmsTracker, resultReceiver, true)) {
                        dispatchSmsDeliveryIntent(r13, tracker.getFormat(), destPort, resultReceiver);
                    }
                    return true;
                } else {
                    HwTelephonyFactory.getHwInnerSmsManager().sendGoogleSmsBlockedRecord(intent);
                    deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                    return false;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("processMessagePart: returning false due to ");
            sb.append(pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)");
            loge(sb.toString());
            return false;
        }
        deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
        return false;
    }

    private boolean processMessagePartWithUserLocked(InboundSmsTracker tracker, byte[][] pdus, int destPort, SmsBroadcastReceiver resultReceiver) {
        log("Credential-encrypted storage not available. Port: " + destPort);
        if (destPort == 2948) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (byte[] pdu : pdus) {
                if (!tracker.is3gpp2()) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdu, "3gpp");
                    if (msg != null) {
                        pdu = msg.getUserData();
                    } else {
                        loge("processMessagePartWithUserLocked: SmsMessage.createFromPdu returned null");
                        return false;
                    }
                }
                output.write(pdu, 0, pdu.length);
            }
            if (this.mWapPush.isWapPushForMms(output.toByteArray(), this)) {
                showNewMessageNotification();
                return false;
            }
        }
        if (destPort != -1) {
            return false;
        }
        if (filterSms(pdus, destPort, tracker, resultReceiver, false)) {
            return true;
        }
        showNewMessageNotification();
        return false;
    }

    /* access modifiers changed from: private */
    public void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, 1, new Notification.Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(true).setVisibility(1).setDefaults(-1).setContentTitle(this.mContext.getString(17040557)).setContentText(this.mContext.getString(17040556)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_OPEN_SMS_APP), 1073741824)).setChannelId(NotificationChannelController.CHANNEL_ID_SMS).build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, 1);
    }

    private boolean filterSms(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        CarrierServicesSmsFilterCallback filterCallback = new CarrierServicesSmsFilterCallback(pdus, destPort, tracker.getFormat(), resultReceiver, userUnlocked);
        byte[][] bArr = pdus;
        int i = destPort;
        CarrierServicesSmsFilter carrierServicesFilter = new CarrierServicesSmsFilter(this.mContext, this.mPhone, bArr, i, tracker.getFormat(), filterCallback, getName());
        if (carrierServicesFilter.filter()) {
            return true;
        }
        if (!VisualVoicemailSmsFilter.filter(this.mContext, pdus, tracker.getFormat(), destPort, this.mPhone.getSubId())) {
            return false;
        }
        log("Visual voicemail SMS dropped");
        dropSms(resultReceiver);
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x008f, code lost:
        if (r2.isManagedProfile() == false) goto L_0x0092;
     */
    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        int[] users;
        Intent intent2 = intent;
        intent2.addFlags(134217728);
        String action = intent.getAction();
        if ("android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.SMS_RECEIVED".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
            intent2.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent2, this.mPhone.getPhoneId());
        UserHandle userHandle = user;
        if (userHandle.equals(UserHandle.ALL)) {
            int[] users2 = null;
            try {
                users2 = ActivityManager.getService().getRunningUserIds();
            } catch (RemoteException e) {
            }
            if (users2 == null) {
                users2 = new int[]{user.getIdentifier()};
            }
            int[] users3 = users2;
            int i = users3.length - 1;
            while (true) {
                int i2 = i;
                if (i2 >= 0) {
                    UserHandle targetUser = new UserHandle(users3[i2]);
                    if (users3[i2] != 0) {
                        if (!this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                            UserInfo info = this.mUserManager.getUserInfo(users3[i2]);
                            if (info != null) {
                            }
                        }
                        users = users3;
                        i = i2 - 1;
                        users3 = users;
                    }
                    UserHandle userHandle2 = targetUser;
                    users = users3;
                    this.mContext.sendOrderedBroadcastAsUser(intent2, targetUser, permission, appOp, opts, users3[i2] == 0 ? resultReceiver : null, getHandler(), -1, null, null);
                    i = i2 - 1;
                    users3 = users;
                } else {
                    return;
                }
            }
        } else {
            this.mContext.sendOrderedBroadcastAsUser(intent2, userHandle, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
            HwTelephonyFactory.getHwInnerSmsManager().triggerInboxInsertDoneDetect(intent2, this.isClass0, getHandler());
        }
    }

    /* access modifiers changed from: private */
    public void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        int rows = this.mResolver.delete(deleteType == 1 ? sRawUriPermanentDelete : sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            loge("No rows were deleted from raw table!");
            return;
        }
        log("Deleted " + rows + " rows from raw table.");
    }

    /* access modifiers changed from: private */
    public Bundle handleSmsWhitelisting(ComponentName target) {
        String reason;
        String pkgName;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, 0, reason);
            BroadcastOptions bopts = BroadcastOptions.makeBasic();
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            return null;
        }
    }

    /* JADX WARNING: type inference failed for: r10v0, types: [byte[][], java.io.Serializable] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void dispatchSmsDeliveryIntent(byte[][] r10, String format, int destPort, SmsBroadcastReceiver resultReceiver) {
        Intent intent = new Intent();
        intent.putExtra("pdus", r10);
        intent.putExtra("format", format);
        if (destPort == -1) {
            intent.setAction("android.provider.Telephony.SMS_DELIVER");
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, true);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                Uri uri = writeInboxMessage(intent);
                if (uri != null) {
                    intent.putExtra("uri", uri.toString());
                }
            }
            if (this.mPhone.getAppSmsManager().handleSmsReceivedIntent(intent)) {
                dropSms(resultReceiver);
                return;
            }
        } else {
            intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
            intent.setData(Uri.parse("sms://localhost:" + destPort));
            intent.setComponent(null);
            intent.addFlags(16777216);
        }
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }

    private boolean duplicateExists(InboundSmsTracker tracker) throws SQLException {
        String where;
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        String seqNumber = Integer.toString(tracker.getSequenceNumber());
        String date = Long.toString(tracker.getTimestamp());
        String messageBody = tracker.getMessageBody();
        if (messageBody == null) {
            log("messageBody is null");
            messageBody = "";
        }
        String messageBody2 = messageBody;
        if (tracker.getMessageCount() == 1) {
            where = "address=? AND reference_number=? AND count=? AND sequence=? AND date=? AND message_body=? AND sub_id=?";
            InboundSmsTracker inboundSmsTracker = tracker;
        } else {
            where = tracker.getQueryForMultiPartDuplicatesSubId();
            HwTelephonyFactory.getHwInnerSmsManager().scanAndDeleteOlderPartialMessages(tracker, this.mResolver);
        }
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(sRawUri, PDU_PROJECTION, where, new String[]{address, refNumber, count, seqNumber, date, messageBody2, String.valueOf(this.mPhone.getSubId())}, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            loge("Discarding duplicate message segment, refNumber=" + refNumber + " seqNumber=" + seqNumber + " count=" + count);
            String oldPduString = cursor.getString(0);
            byte[] pdu = tracker.getPdu();
            if (!Arrays.equals(HexDump.hexStringToByteArray(oldPduString), tracker.getPdu())) {
                loge("Warning: dup message segment PDU of length " + pdu.length + " is different from existing PDU of length " + oldPdu.length);
            }
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int addTrackerToRawTable(InboundSmsTracker tracker, boolean deDup) {
        InboundSmsTracker inboundSmsTracker = tracker;
        if (deDup) {
            try {
                if (duplicateExists(tracker)) {
                    return 5;
                }
            } catch (SQLException e) {
                SQLException sQLException = e;
                loge("Can't access SMS database", e);
                return 2;
            }
        } else {
            logd("Skipped message de-duping logic");
        }
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        ContentValues values = tracker.getContentValues();
        values.put("sub_id", Integer.valueOf(this.mPhone.getSubId()));
        values.put("receive_time", Long.valueOf(System.currentTimeMillis()));
        Uri newUri = this.mResolver.insert(sRawUri, values);
        log("URI of new row -> " + newUri);
        try {
            long rowId = ContentUris.parseId(newUri);
            if (tracker.getMessageCount() == 1) {
                inboundSmsTracker.setDeleteWhere(SELECT_BY_ID, new String[]{Long.toString(rowId)});
            } else {
                inboundSmsTracker.setDeleteWhere(tracker.getQueryForSegmentsSubId(), new String[]{address, refNumber, count, String.valueOf(this.mPhone.getSubId())});
            }
            return 1;
        } catch (Exception e2) {
            loge("error parsing URI for new row: " + newUri, e2);
            return 2;
        }
    }

    static boolean isCurrentFormat3gpp2() {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType();
    }

    /* access modifiers changed from: private */
    public void dropSms(SmsBroadcastReceiver receiver) {
        deleteFromRawTable(receiver.mDeleteWhere, receiver.mDeleteWhereArgs, 2);
        sendMessage(3);
    }

    /* access modifiers changed from: private */
    public boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & 2) > 0;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    private Uri writeInboxMessage(Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length < 1) {
            loge("Failed to parse SMS pdu");
            return null;
        }
        int length = messages.length;
        int i = 0;
        while (i < length) {
            try {
                messages[i].getDisplayMessageBody();
                i++;
            } catch (NullPointerException e) {
                loge("NPE inside SmsMessage");
                return null;
            }
        }
        ContentValues values = parseSmsMessage(messages);
        long identity = Binder.clearCallingIdentity();
        try {
            return this.mContext.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values);
        } catch (Exception e2) {
            loge("Failed to persist inbox message", e2);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static ContentValues parseSmsMessage(SmsMessage[] msgs) {
        SmsMessage sms = msgs[0];
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        values.put("body", buildMessageBodyFromPdus(msgs));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("seen", 0);
        values.put("read", 0);
        String subject = sms.getPseudoSubject();
        if (!TextUtils.isEmpty(subject)) {
            values.put("subject", subject);
        }
        values.put("reply_path_present", Integer.valueOf(sms.isReplyPathPresent() ? 1 : 0));
        values.put("service_center", sms.getServiceCenterAddress());
        return values;
    }

    private static String buildMessageBodyFromPdus(SmsMessage[] msgs) {
        if (msgs.length == 1) {
            return replaceFormFeeds(msgs[0].getDisplayMessageBody());
        }
        StringBuilder body = new StringBuilder();
        for (SmsMessage msg : msgs) {
            body.append(msg.getDisplayMessageBody());
        }
        return replaceFormFeeds(body.toString());
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? "" : s.replace(12, 10);
    }

    @VisibleForTesting
    public PowerManager.WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    @VisibleForTesting
    public int getWakeLockTimeout() {
        return this.mWakeLockTimeout;
    }

    /* access modifiers changed from: private */
    public void setWakeLockTimeout(int timeOut) {
        this.mWakeLockTimeout = timeOut;
    }

    static void registerNewMessageNotificationActionHandler(Context context) {
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ACTION_OPEN_SMS_APP);
        context.registerReceiver(new NewMessageNotificationActionReceiver(), userFilter);
    }

    private int dispatchMessage(SmsMessageBase smsb) {
        return dispatchMessage(smsb, true);
    }
}

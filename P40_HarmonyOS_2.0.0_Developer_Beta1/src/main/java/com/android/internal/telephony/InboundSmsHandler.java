package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
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
import android.util.LocalLog;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CarrierServicesSmsFilter;
import com.android.internal.telephony.SmsConstants;
import com.android.internal.telephony.SmsDispatchersController;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.util.HexDump;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.PduParser;
import com.huawei.internal.telephony.InboundSmsHandlerEx;
import com.huawei.internal.telephony.InboundSmsTrackerEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InboundSmsHandler extends StateMachine {
    private static String ACTION_OPEN_SMS_APP = "com.android.internal.telephony.OPEN_DEFAULT_SMS_APP";
    public static final int ADDRESS_COLUMN = 6;
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    public static final int DELETED_FLAG_COLUMN = 10;
    public static final int DESTINATION_PORT_COLUMN = 2;
    public static final int DISPLAY_ADDRESS_COLUMN = 9;
    private static final int ERROR_BASE_MMS = 1300;
    static final int ERR_SMS_SEND = 1311;
    private static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_COMPLETE_HW = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 7;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    private static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    public static final int EVENT_UPDATE_TRACKER = 8;
    public static final int ID_COLUMN = 7;
    private static final String INCOMING_SMS_EXCEPTION_PATTERN = "incoming_sms_exception_pattern";
    private static final String INCOMING_SMS_RESTRICTION_PATTERN = "incoming_sms_restriction_pattern";
    private static final String INTERCEPTION_SMS_RECEIVED = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
    private static final int LOCAL_LOG_SIZE = 10;
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    private static final int NO_DESTINATION_PORT = -1;
    public static final int PDU_COLUMN = 0;
    private static final String[] PDU_DELETED_FLAG_PROJECTION = {"pdu", "deleted"};
    private static final Map<Integer, Integer> PDU_DELETED_FLAG_PROJECTION_INDEX_MAPPING = new HashMap<Integer, Integer>() {
        /* class com.android.internal.telephony.InboundSmsHandler.AnonymousClass1 */

        {
            put(0, 0);
            put(10, 1);
        }
    };
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = {"pdu", "sequence", "destination_port", "display_originating_addr", "date"};
    private static final Map<Integer, Integer> PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING = new HashMap<Integer, Integer>() {
        /* class com.android.internal.telephony.InboundSmsHandler.AnonymousClass2 */

        {
            put(0, 0);
            put(1, 1);
            put(2, 2);
            put(9, 3);
            put(3, 4);
        }
    };
    private static final String RECEIVE_SMS_PERMISSION = "huawei.permission.RECEIVE_SMS_INTERCEPTION";
    public static final int RECEIVE_TIME_COLUMN = 10;
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final int SEQUENCE_COLUMN = 1;
    protected static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    protected static final Uri sRawUri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw");
    protected static final Uri sRawUriPermanentDelete = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "raw/permanentDelete");
    private final int DELETE_PERMANENTLY = 1;
    private final int MARK_DELETED = 2;
    private boolean isClass0 = false;
    @UnsupportedAppUsage
    protected CellBroadcastHandler mCellBroadcastHandler;
    @UnsupportedAppUsage
    protected final Context mContext;
    private final DefaultState mDefaultState = new DefaultState();
    @UnsupportedAppUsage
    private final DeliveringState mDeliveringState = new DeliveringState();
    @UnsupportedAppUsage
    IDeviceIdleController mDeviceIdleController;
    private HwCustInboundSmsHandler mHwCust;
    @UnsupportedAppUsage
    private final IdleState mIdleState = new IdleState();
    private boolean mLastSmsWasInjected = false;
    private LocalLog mLocalLog = new LocalLog(10);
    protected TelephonyMetrics mMetrics = TelephonyMetrics.getInstance();
    @UnsupportedAppUsage
    protected Phone mPhone;
    @UnsupportedAppUsage
    private final ContentResolver mResolver;
    private final boolean mSmsReceiveDisabled;
    private final StartupState mStartupState = new StartupState();
    protected SmsStorageMonitor mStorageMonitor;
    @UnsupportedAppUsage
    private UserManager mUserManager;
    @UnsupportedAppUsage
    private final WaitingState mWaitingState = new WaitingState();
    @UnsupportedAppUsage
    private final PowerManager.WakeLock mWakeLock;
    private int mWakeLockTimeout;
    @UnsupportedAppUsage
    private final WapPushOverSms mWapPush;

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
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
        this.mWapPush = new WapPushOverSms(context);
        HwTelephonyFactory.getHwInnerSmsManager().createSmsInterceptionService(context);
        this.mSmsReceiveDisabled = !TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17891524));
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().inject(IDeviceIdleController.class.getName()).getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        HwTelephonyFactory.getHwInnerSmsManager().addInboxInsertObserver(this.mContext);
        this.mHwCust = (HwCustInboundSmsHandler) HwCustUtils.createObj(HwCustInboundSmsHandler.class, new Object[0]);
        HwCustInboundSmsHandler hwCustInboundSmsHandler = this.mHwCust;
        if (hwCustInboundSmsHandler != null && hwCustInboundSmsHandler.isIQIEnable()) {
            this.mHwCust.createIQClient(this.mContext);
        }
        log("created InboundSmsHandler");
    }

    public void dispose() {
        quit();
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    @UnsupportedAppUsage
    public Phone getPhone() {
        return this.mPhone;
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            String errorText = "processMessage: unhandled message type " + msg.what + " currState=" + InboundSmsHandler.this.getCurrentState().getName();
            if (Build.IS_DEBUGGABLE) {
                InboundSmsHandler.this.loge("---- Dumping InboundSmsHandler ----");
                InboundSmsHandler.this.loge("Total records=" + InboundSmsHandler.this.getLogRecCount());
                for (int i2 = Math.max(InboundSmsHandler.this.getLogRecSize() + -20, 0); i2 < InboundSmsHandler.this.getLogRecSize(); i2++) {
                    if (InboundSmsHandler.this.getLogRec(i2) != null) {
                        InboundSmsHandler.this.loge("Rec[%d]: %s\n" + i2 + InboundSmsHandler.this.getLogRec(i2).toString());
                    }
                }
                InboundSmsHandler.this.loge("---- Dumped InboundSmsHandler ----");
                throw new RuntimeException(errorText);
            }
            InboundSmsHandler.this.loge(errorText);
            return true;
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
            if (!(i == 1 || i == 2)) {
                if (i == 6) {
                    InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
                    inboundSmsHandler2.transitionTo(inboundSmsHandler2.mIdleState);
                    return true;
                } else if (i != 7) {
                    return false;
                }
            }
            InboundSmsHandler.this.deferMessage(msg);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Idle state");
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.sendMessageDelayed(5, (long) inboundSmsHandler.getWakeLockTimeout());
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
            int i = msg.what;
            if (!(i == 1 || i == 2)) {
                if (i == 4) {
                    return true;
                }
                if (i == 5) {
                    InboundSmsHandler.this.mWakeLock.release();
                    if (InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.log("mWakeLock is still held after release");
                    } else {
                        InboundSmsHandler.this.log("mWakeLock released");
                    }
                    return true;
                } else if (i != 7) {
                    return false;
                }
            }
            InboundSmsHandler.this.deferMessage(msg);
            InboundSmsHandler inboundSmsHandler3 = InboundSmsHandler.this;
            inboundSmsHandler3.transitionTo(inboundSmsHandler3.mDeliveringState);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class DeliveringState extends State {
        private DeliveringState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Delivering state");
        }

        public void exit() {
            InboundSmsHandler.this.log("leaving Delivering state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("DeliveringState.processMessage:" + msg.what);
            int i = msg.what;
            if (i == 1) {
                InboundSmsHandler.this.handleNewSms((AsyncResult) msg.obj);
                InboundSmsHandler.this.sendMessage(4);
                return true;
            } else if (i == 2) {
                if (InboundSmsHandler.this.processMessagePart((InboundSmsTracker) msg.obj)) {
                    InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
                    inboundSmsHandler.sendMessage(inboundSmsHandler.obtainMessage(8, msg.obj));
                    InboundSmsHandler inboundSmsHandler2 = InboundSmsHandler.this;
                    inboundSmsHandler2.transitionTo(inboundSmsHandler2.mWaitingState);
                } else {
                    InboundSmsHandler.this.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                    InboundSmsHandler.this.sendMessage(4);
                }
                return true;
            } else if (i == 4) {
                InboundSmsHandler inboundSmsHandler3 = InboundSmsHandler.this;
                inboundSmsHandler3.transitionTo(inboundSmsHandler3.mIdleState);
                return true;
            } else if (i == 5) {
                InboundSmsHandler.this.mWakeLock.release();
                if (!InboundSmsHandler.this.mWakeLock.isHeld()) {
                    InboundSmsHandler.this.loge("mWakeLock released while delivering/broadcasting!");
                }
                return true;
            } else if (i == 7) {
                InboundSmsHandler.this.handleInjectSms((AsyncResult) msg.obj);
                InboundSmsHandler.this.sendMessage(4);
                return true;
            } else if (i != 8) {
                String errorMsg = "Unhandled msg in delivering state, msg.what = " + msg.what;
                InboundSmsHandler.this.loge(errorMsg);
                InboundSmsHandler.this.mLocalLog.log(errorMsg);
                return false;
            } else {
                InboundSmsHandler.this.logd("process tracker message in DeliveringState " + msg.arg1);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class WaitingState extends State {
        private InboundSmsTracker mLastDeliveredSmsTracker;

        private WaitingState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Waiting state");
        }

        public void exit() {
            InboundSmsHandler.this.log("exiting Waiting state");
            InboundSmsHandler.this.setWakeLockTimeout(InboundSmsHandler.WAKELOCK_TIMEOUT);
            InboundSmsHandler.this.mPhone.getIccSmsInterfaceManager().mDispatchersController.sendEmptyMessage(17);
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("WaitingState.processMessage:" + msg.what);
            int i = msg.what;
            if (i == 2) {
                if (this.mLastDeliveredSmsTracker != null) {
                    String str = "Defer sms broadcast due to undelivered sms,  messageCount = " + this.mLastDeliveredSmsTracker.getMessageCount() + " destPort = " + this.mLastDeliveredSmsTracker.getDestPort() + " timestamp = " + this.mLastDeliveredSmsTracker.getTimestamp() + " currentTimestamp = " + System.currentTimeMillis();
                    InboundSmsHandler.this.logd(str);
                    InboundSmsHandler.this.mLocalLog.log(str);
                }
                InboundSmsHandler.this.deferMessage(msg);
                return true;
            } else if (i == 3) {
                this.mLastDeliveredSmsTracker = null;
                InboundSmsHandler.this.sendMessage(4);
                InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
                inboundSmsHandler.transitionTo(inboundSmsHandler.mDeliveringState);
                return true;
            } else if (i == 4) {
                return true;
            } else {
                if (i != 8) {
                    return false;
                }
                this.mLastDeliveredSmsTracker = (InboundSmsTracker) msg.obj;
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void handleNewSms(AsyncResult ar) {
        int result;
        int result2;
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return;
        }
        Jlog.d(47, "JLID_RIL_RESPONSE_NEW_SMS");
        boolean handled = false;
        try {
            SmsMessage sms = (SmsMessage) ar.result;
            if (this.mHwCust == null || !this.mHwCust.isIQIEnable() || !this.mHwCust.isIQISms(sms)) {
                result2 = dispatchMessage(sms.mWrappedSmsMessage);
            } else {
                log("check SMS is true");
                result2 = 1;
            }
            this.mLastSmsWasInjected = false;
            result = HwTelephonyFactory.getHwInnerSmsManager().dealBlacklistSms(this.mContext, sms, result2);
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (result != -1) {
            if (result == 1) {
                handled = true;
            }
            if (!handled) {
                Jlog.d(50, "JL_DISPATCH_SMS_FAILED");
            }
            HwCustInboundSmsHandler hwCustInboundSmsHandler = this.mHwCust;
            if (hwCustInboundSmsHandler == null || !hwCustInboundSmsHandler.isNotNotifyWappushEnabled(ar)) {
                notifyAndAcknowledgeLastIncomingSms(handled, result, null);
            } else {
                acknowledgeLastIncomingSms(handled, result, null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void handleInjectSms(AsyncResult ar) {
        int result;
        SmsDispatchersController.SmsInjectionCallback callback = null;
        try {
            callback = (SmsDispatchersController.SmsInjectionCallback) ar.userObj;
            SmsMessage sms = (SmsMessage) ar.result;
            if (sms == null) {
                result = 2;
            } else {
                this.mLastSmsWasInjected = true;
                result = dispatchMessage(sms.mWrappedSmsMessage, false);
            }
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
        } else {
            HwCustInboundSmsHandler hwCustInboundSmsHandler = this.mHwCust;
            if (hwCustInboundSmsHandler == null || !hwCustInboundSmsHandler.isBlockMsgReceive(this.mPhone.getPhoneId())) {
                boolean filterRepeatedMessage2 = filterRepeatedMessage || HwTelephonyFactory.getHwInnerSmsManager().isIOTVersion();
                log("dispatchMessage, filterRepeatedMessage:" + filterRepeatedMessage2);
                if (!filterRepeatedMessage2 || !HwTelephonyFactory.getHwInnerSmsManager().hasSameSmsPdu(smsb.getPdu(), this.mPhone.getPhoneId())) {
                    boolean onlyCore = false;
                    try {
                        onlyCore = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
                    } catch (RemoteException e) {
                    }
                    if (onlyCore) {
                        log("Received a short message in encrypted state. Rejecting.");
                        return 2;
                    }
                    int result = dispatchMessageRadioSpecific(smsb);
                    if (result != 1) {
                        this.mMetrics.writeIncomingSmsError(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, result);
                    }
                    return result;
                }
                log("receive a duplicated SMS and abandon it.");
                return 1;
            }
            log("dispatchMessage, block receive sms for non AIS card.");
            return 1;
        }
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent("android.provider.Telephony.SMS_REJECTED");
            intent.putExtra("result", result);
            intent.addFlags(ApnSettingHelper.TYPE_WIFI_MMS);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int dispatchNormalMessage(SmsMessageBase sms) {
        InboundSmsTracker tracker;
        HwCustInboundSmsHandler hwCustInboundSmsHandler;
        SmsHeader smsHeader = sms.getUserDataHeader();
        this.isClass0 = sms.getMessageClass() == SmsConstants.MessageClass.CLASS_0;
        int destPort = -1;
        Jlog.d(48, "JL_DISPATCH_NORMAL_SMS");
        if (smsHeader == null || smsHeader.concatRef == null) {
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
                log("destination port: " + destPort);
            }
            tracker = TelephonyComponentFactory.getInstance().inject(InboundSmsTracker.class.getName()).makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), false, sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), sms.getMessageBody(), sms.getMessageClass() == SmsConstants.MessageClass.CLASS_0);
        } else {
            SmsHeader.ConcatRef concatRef = smsHeader.concatRef;
            SmsHeader.PortAddrs portAddrs = smsHeader.portAddrs;
            int destPort2 = portAddrs != null ? portAddrs.destPort : -1;
            tracker = TelephonyComponentFactory.getInstance().inject(InboundSmsTracker.class.getName()).makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort2, is3gpp2(), sms.getOriginatingAddress(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, false, sms.getMessageBody(), sms.getMessageClass() == SmsConstants.MessageClass.CLASS_0);
            destPort = destPort2;
        }
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.comm_serv_tid_enable", false) && (hwCustInboundSmsHandler = this.mHwCust) != null) {
            if (hwCustInboundSmsHandler.dispatchMessageByDestPort(destPort, sms, this.mContext)) {
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

    /* JADX DEBUG: Multi-variable search result rejected for r33v0, resolved type: com.android.internal.telephony.InboundSmsHandler */
    /* JADX DEBUG: Multi-variable search result rejected for r1v12, resolved type: android.content.Intent */
    /* JADX DEBUG: Multi-variable search result rejected for r4v8, resolved type: byte[] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r13v0, types: [java.lang.Object[], byte[][], java.io.Serializable] */
    /* JADX WARN: Type inference failed for: r13v3 */
    /* JADX WARN: Type inference failed for: r13v6 */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0373 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x0375  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x04e8  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x04f0  */
    /* JADX WARNING: Removed duplicated region for block: B:176:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @UnsupportedAppUsage
    private boolean processMessagePart(InboundSmsTracker tracker) {
        long[] timestamps;
        ?? r13;
        boolean block;
        int destPort;
        SmsBroadcastReceiver resultReceiver;
        int result;
        int result2;
        String address;
        int messageCount;
        ByteArrayOutputStream output;
        String oriAddress;
        byte[] pdu;
        int i;
        SQLException e;
        SQLException e2;
        int port;
        int i2 = 0;
        if (tracker.isOldMessageInRawTable()) {
            log("processMessagePart, ignore old message kept in raw table.");
            return false;
        }
        int messageCount2 = tracker.getMessageCount();
        int destPort2 = tracker.getDestPort();
        if (destPort2 == 2948 || !HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(false) || !HwTelephonyFactory.getHwInnerSmsManager().isExceedSMSLimit(this.mContext, false)) {
            boolean block2 = false;
            String address2 = tracker.getAddress();
            if (messageCount2 <= 0) {
                loge("processMessagePart: returning false due to invalid message count " + messageCount2);
                return false;
            }
            int i3 = 1;
            if (messageCount2 == 1) {
                byte[][] pdus = {tracker.getPdu()};
                long[] timestamps2 = {tracker.getTimestamp()};
                destPort = destPort2;
                block = BlockChecker.isBlocked(this.mContext, tracker.getDisplayAddress(), null);
                timestamps = timestamps2;
                r13 = pdus;
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mResolver.query(sRawUri, PDU_SEQUENCE_PORT_PROJECTION, tracker.getQueryForSegmentsSubId(), new String[]{address2, Integer.toString(tracker.getReferenceNumber()), Integer.toString(tracker.getMessageCount()), String.valueOf(this.mPhone.getPhoneId())}, null);
                    int cursorCount = cursor.getCount();
                    log("processMessagePart, cursorCount: " + cursorCount + ", ref|seq/count(" + tracker.getReferenceNumber() + "|" + tracker.getSequenceNumber() + "/" + messageCount2 + ")");
                    if (cursorCount < messageCount2) {
                        cursor.close();
                        return false;
                    }
                    byte[][] pdus2 = new byte[messageCount2][];
                    timestamps = new long[messageCount2];
                    while (cursor.moveToNext()) {
                        try {
                            int index = cursor.getInt(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(i3)).intValue()) - tracker.getIndexOffset();
                            if (index < pdus2.length) {
                                if (index >= 0) {
                                    pdus2[index] = HexDump.hexStringToByteArray(cursor.getString(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(Integer.valueOf(i2)).intValue()));
                                    if (index == 0 && !cursor.isNull(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(2).intValue()) && (port = InboundSmsTracker.getRealDestPort(cursor.getInt(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(2).intValue()))) != -1) {
                                        destPort2 = port;
                                    }
                                    timestamps[index] = cursor.getLong(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(3).intValue());
                                    if (!block2) {
                                        block2 = BlockChecker.isBlocked(this.mContext, cursor.getString(PDU_SEQUENCE_PORT_PROJECTION_INDEX_MAPPING.get(9).intValue()), null);
                                    }
                                    i2 = 0;
                                    i3 = 1;
                                }
                            }
                            loge(String.format("processMessagePart: invalid seqNumber = %d, messageCount = %d", Integer.valueOf(tracker.getIndexOffset() + index), Integer.valueOf(messageCount2)));
                            i2 = 0;
                            i3 = 1;
                        } catch (SQLException e3) {
                            e2 = e3;
                            try {
                                loge("Can't access multipart SMS database", e2);
                                if (cursor != null) {
                                }
                            } catch (Throwable th) {
                                e = th;
                            }
                        } catch (Throwable th2) {
                            e = th2;
                            if (cursor != null) {
                            }
                            throw e;
                        }
                    }
                    cursor.close();
                    destPort = destPort2;
                    block = block2;
                    r13 = pdus2;
                } catch (SQLException e4) {
                    e2 = e4;
                    loge("Can't access multipart SMS database", e2);
                    if (cursor != null) {
                        return false;
                    }
                    cursor.close();
                    return false;
                } catch (Throwable th3) {
                    e = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw e;
                }
            }
            String format = !tracker.is3gpp2() ? "3gpp" : "3gpp2";
            if (destPort != 2948) {
                this.mMetrics.writeIncomingSmsSession(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, block);
            }
            List<byte[]> pduList = Arrays.asList(r13);
            if (pduList.size() != 0) {
                if (!pduList.contains(null)) {
                    SmsBroadcastReceiver resultReceiver2 = new SmsBroadcastReceiver(tracker);
                    if (!this.mUserManager.isUserUnlocked()) {
                        return processMessagePartWithUserLocked(tracker, r13, destPort, resultReceiver2);
                    }
                    if (destPort == 2948) {
                        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
                        int length = r13.length;
                        String oriAddress2 = null;
                        int i4 = 0;
                        while (i4 < length) {
                            byte[] pdu2 = r13[i4];
                            if (!tracker.is3gpp2()) {
                                SmsMessage msg = SmsMessage.createFromPdu(pdu2, "3gpp");
                                if (msg != null) {
                                    byte[] pdu3 = msg.getUserData();
                                    messageCount = messageCount2;
                                    address = address2;
                                    oriAddress = msg.getOriginatingAddress();
                                    output = output2;
                                    pdu = pdu3;
                                    i = 0;
                                } else {
                                    loge("processMessagePart: SmsMessage.createFromPdu returned null");
                                    this.mMetrics.writeIncomingWapPush(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, false);
                                    return false;
                                }
                            } else {
                                messageCount = messageCount2;
                                address = address2;
                                i = 0;
                                oriAddress = oriAddress2;
                                output = output2;
                                pdu = pdu2;
                            }
                            output.write(pdu, i, pdu.length);
                            i4++;
                            oriAddress2 = oriAddress;
                            output2 = output;
                            messageCount2 = messageCount;
                            address2 = address;
                        }
                        if (this.mWapPush.isWapPushForMms(output2.toByteArray(), this) && this.mPhone.getDcTracker(1).getHwDcTrackerEx().isRoamingPushDisabled() && this.mPhone.getServiceState().getDataRoaming()) {
                            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                            return false;
                        }
                        this.mWapPush.mSmsTracker = tracker;
                        HwCustInboundSmsHandler hwCustInboundSmsHandler = this.mHwCust;
                        if (hwCustInboundSmsHandler == null || !hwCustInboundSmsHandler.isIQIEnable() || !this.mHwCust.isIQIWapPush(output2)) {
                            if (!tracker.is3gpp2()) {
                                this.mWapPush.mOriginalAddr = oriAddress2;
                            }
                            result = this.mWapPush.dispatchWapPdu(output2.toByteArray(), resultReceiver2, this);
                        } else {
                            log("check WapPush is true");
                            result = -1;
                        }
                        log("dispatchWapPdu() returned " + result);
                        if (result == -1) {
                            result2 = result;
                        } else if (result == 1) {
                            result2 = result;
                        } else {
                            result2 = result;
                            this.mMetrics.writeIncomingWapPush(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, false);
                            if (result2 != -1) {
                                return true;
                            }
                            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                            return false;
                        }
                        this.mMetrics.writeIncomingWapPush(this.mPhone.getPhoneId(), this.mLastSmsWasInjected, format, timestamps, true);
                        if (result2 != -1) {
                        }
                    } else {
                        if (HwTelephonyFactory.getHwInnerSmsManager().isLimitNumOfSmsEnabled(false) && destPort != 2948) {
                            HwTelephonyFactory.getHwInnerSmsManager().updateSmsUsedNum(this.mContext, false);
                        }
                        Intent intent = new Intent();
                        intent.putExtra("pdus", (Serializable) r13);
                        intent.putExtra("format", tracker.getFormat());
                        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
                        log("putPhoneIdAndSubIdExtra" + this.mPhone.getPhoneId());
                        if (-1 == destPort && !HwTelephonyFactory.getHwInnerSmsManager().isMatchSMSPattern(tracker.getAddress(), INCOMING_SMS_EXCEPTION_PATTERN, this.mPhone.getPhoneId()) && HwTelephonyFactory.getHwInnerSmsManager().isMatchSMSPattern(tracker.getAddress(), INCOMING_SMS_RESTRICTION_PATTERN, this.mPhone.getPhoneId())) {
                            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                            log("forbid receive sms by mdm!");
                            return false;
                        } else if (-1 != destPort || !block) {
                            InboundSmsHandlerEx inboundSmsHandlerEx = new InboundSmsHandlerEx();
                            inboundSmsHandlerEx.setInboundSmsHandler(this);
                            if (destPort != -1) {
                                resultReceiver = resultReceiver2;
                            } else if (HwTelephonyFactory.getHwInnerSmsManager().newSmsShouldBeIntercepted(this.mContext, intent, inboundSmsHandlerEx, tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), false)) {
                                Intent secretIntent = new Intent(intent);
                                secretIntent.setAction(INTERCEPTION_SMS_RECEIVED);
                                dispatchIntent(secretIntent, RECEIVE_SMS_PERMISSION, 16, handleSmsWhitelisting(null, false), null, UserHandle.ALL);
                                return true;
                            } else {
                                resultReceiver = resultReceiver2;
                            }
                            if (filterSms(r13, destPort, tracker, resultReceiver, true)) {
                                return true;
                            }
                            dispatchSmsDeliveryIntent(r13, tracker.getFormat(), destPort, resultReceiver, tracker.isClass0());
                            return true;
                        } else {
                            HwTelephonyFactory.getHwInnerSmsManager().sendGoogleSmsBlockedRecord(intent);
                            deleteFromRawTable(tracker.getDeleteWhere(), tracker.getDeleteWhereArgs(), 2);
                            return false;
                        }
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("processMessagePart: returning false due to ");
            sb.append(pduList.size() == 0 ? "pduList.size() == 0" : "pduList.contains(null)");
            String errorMsg = sb.toString();
            loge(errorMsg);
            this.mLocalLog.log(errorMsg);
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
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, 1, new Notification.Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(true).setVisibility(1).setDefaults(-1).setContentTitle(this.mContext.getString(17040648)).setContentText(this.mContext.getString(17040647)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_OPEN_SMS_APP), 1140850688)).setChannelId(NotificationChannelController.CHANNEL_ID_SMS).build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, 1);
    }

    private boolean filterSms(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        if (new CarrierServicesSmsFilter(this.mContext, this.mPhone, pdus, destPort, tracker.getFormat(), new CarrierServicesSmsFilterCallback(pdus, destPort, tracker.getFormat(), resultReceiver, userUnlocked, tracker.isClass0()), getName(), this.mLocalLog).filter()) {
            return true;
        }
        if (!VisualVoicemailSmsFilter.filter(this.mContext, pdus, tracker.getFormat(), destPort, this.mPhone.getSubId())) {
            return false;
        }
        log("Visual voicemail SMS dropped");
        dropSms(resultReceiver);
        return true;
    }

    @UnsupportedAppUsage
    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        int[] users;
        intent.addFlags(134217728);
        String action = intent.getAction();
        if ("android.provider.Telephony.SMS_DELIVER".equals(action) || "android.provider.Telephony.SMS_RECEIVED".equals(action) || "android.provider.Telephony.WAP_PUSH_DELIVER".equals(action) || "android.provider.Telephony.WAP_PUSH_RECEIVED".equals(action)) {
            intent.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (user.equals(UserHandle.ALL)) {
            int[] users2 = null;
            try {
                users2 = ActivityManager.getService().getRunningUserIds();
            } catch (RemoteException e) {
            }
            int[] users3 = users2 == null ? new int[]{user.getIdentifier()} : users2;
            int i = users3.length - 1;
            while (i >= 0) {
                UserHandle targetUser = new UserHandle(users3[i]);
                if (users3[i] != 0) {
                    if (this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                        users = users3;
                    } else {
                        UserInfo info = this.mUserManager.getUserInfo(users3[i]);
                        if (info == null) {
                            users = users3;
                        } else if (info.isManagedProfile()) {
                            users = users3;
                        }
                    }
                    i--;
                    users3 = users;
                }
                users = users3;
                this.mContext.sendOrderedBroadcastAsUser(intent, targetUser, permission, appOp, opts, users3[i] == 0 ? resultReceiver : null, getHandler(), -1, null, null);
                i--;
                users3 = users;
            }
            return;
        }
        this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
        HwTelephonyFactory.getHwInnerSmsManager().triggerInboxInsertDoneDetect(intent, this.isClass0, getHandler());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        int rows = this.mResolver.delete(deleteType == 1 ? sRawUriPermanentDelete : sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            loge("No rows were deleted from raw table!");
            return;
        }
        log("Deleted " + rows + " rows from raw table.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private Bundle handleSmsWhitelisting(ComponentName target, boolean bgActivityStartAllowed) {
        String reason;
        String pkgName;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        BroadcastOptions bopts = null;
        Bundle bundle = null;
        if (bgActivityStartAllowed) {
            bopts = BroadcastOptions.makeBasic();
            bopts.setBackgroundActivityStartsAllowed(true);
            bundle = bopts.toBundle();
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, 0, reason);
            if (bopts == null) {
                bopts = BroadcastOptions.makeBasic();
            }
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            return bundle;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r10v0, resolved type: byte[][] */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, SmsBroadcastReceiver resultReceiver, boolean isClass02) {
        Uri uri;
        Intent intent = new Intent();
        intent.putExtra("pdus", (Serializable) pdus);
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
            if (SmsManager.getDefault().getAutoPersisting() && (uri = writeInboxMessage(intent)) != null) {
                intent.putExtra("uri", uri.toString());
            }
            if (this.mPhone.getAppSmsManager().handleSmsReceivedIntent(intent)) {
                dropSms(resultReceiver);
                return;
            }
        } else {
            intent.setAction("android.intent.action.DATA_SMS_RECEIVED");
            intent.setData(Uri.parse("sms://localhost:" + destPort));
            intent.setComponent(null);
            intent.addFlags(ApnSettingHelper.TYPE_WIFI_MMS);
        }
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent(), isClass02), resultReceiver, UserHandle.SYSTEM);
    }

    private boolean checkAndHandleDuplicate(InboundSmsTracker tracker) throws SQLException {
        Pair<String, String[]> exactMatchQuery = tracker.getExactMatchDupDetectQueryForSubId(this.mPhone.getPhoneId());
        InboundSmsTrackerEx trackerEx = new InboundSmsTrackerEx();
        trackerEx.setInboundSmsTracker(tracker);
        HwTelephonyFactory.getHwInnerSmsManager().scanAndDeleteOlderPartialMessages(trackerEx, this.mResolver);
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(sRawUri, PDU_DELETED_FLAG_PROJECTION, (String) exactMatchQuery.first, (String[]) exactMatchQuery.second, null);
            if (cursor != null && cursor.moveToNext()) {
                if (cursor.getCount() != 1) {
                    loge("Exact match query returned " + cursor.getCount() + " rows");
                }
                if (cursor.getInt(PDU_DELETED_FLAG_PROJECTION_INDEX_MAPPING.get(10).intValue()) == 1) {
                    loge("Discarding duplicate message segment: " + tracker);
                    logDupPduMismatch(cursor, tracker);
                    cursor.close();
                    return true;
                } else if (tracker.getMessageCount() == 1) {
                    deleteFromRawTable((String) exactMatchQuery.first, (String[]) exactMatchQuery.second, 1);
                    loge("Replacing duplicate message: " + tracker);
                    logDupPduMismatch(cursor, tracker);
                }
            }
            if (tracker.getMessageCount() <= 1) {
                return false;
            }
            Pair<String, String[]> inexactMatchQuery = tracker.getInexactMatchDupDetectQuery();
            Cursor cursor2 = null;
            try {
                cursor2 = this.mResolver.query(sRawUri, PDU_DELETED_FLAG_PROJECTION, (String) inexactMatchQuery.first, (String[]) inexactMatchQuery.second, null);
                if (cursor2 != null && cursor2.moveToNext()) {
                    if (cursor2.getCount() != 1) {
                        loge("Inexact match query returned " + cursor2.getCount() + " rows");
                    }
                    deleteFromRawTable((String) inexactMatchQuery.first, (String[]) inexactMatchQuery.second, 1);
                    loge("Replacing duplicate message segment: " + tracker);
                    logDupPduMismatch(cursor2, tracker);
                }
            } finally {
                if (cursor2 != null) {
                    cursor2.close();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void logDupPduMismatch(Cursor cursor, InboundSmsTracker tracker) {
        String oldPduString = cursor.getString(PDU_DELETED_FLAG_PROJECTION_INDEX_MAPPING.get(0).intValue());
        byte[] pdu = tracker.getPdu();
        byte[] oldPdu = HexDump.hexStringToByteArray(oldPduString);
        if (!Arrays.equals(oldPdu, tracker.getPdu())) {
            loge("Warning: dup message PDU of length " + pdu.length + " is different from existing PDU of length " + oldPdu.length);
        }
    }

    private int addTrackerToRawTable(InboundSmsTracker tracker, boolean deDup) {
        if (deDup) {
            try {
                if (checkAndHandleDuplicate(tracker)) {
                    return 5;
                }
            } catch (SQLException e) {
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
        values.put("sub_id", Integer.valueOf(this.mPhone.getPhoneId()));
        values.put("receive_time", Long.valueOf(System.currentTimeMillis()));
        Uri newUri = this.mResolver.insert(sRawUri, values);
        log("URI of new row -> " + newUri);
        try {
            long rowId = ContentUris.parseId(newUri);
            if (tracker.getMessageCount() == 1) {
                tracker.setDeleteWhere(SELECT_BY_ID, new String[]{Long.toString(rowId)});
            } else {
                tracker.setDeleteWhere(tracker.getQueryForSegmentsSubId(), new String[]{address, refNumber, count, String.valueOf(this.mPhone.getPhoneId())});
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
    public final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano = System.nanoTime();
        @UnsupportedAppUsage
        private final String mDeleteWhere;
        @UnsupportedAppUsage
        private final String[] mDeleteWhereArgs;

        SmsBroadcastReceiver(InboundSmsTracker tracker) {
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int rc;
            Bundle options;
            InboundSmsHandlerEx inboundSmsHandlerEx = new InboundSmsHandlerEx();
            inboundSmsHandlerEx.setInboundSmsHandler(InboundSmsHandler.this);
            if (HwTelephonyFactory.getHwInnerSmsManager().shouldSendReceivedActionInPrivacyMode(inboundSmsHandlerEx, context, intent, this.mDeleteWhere, this.mDeleteWhereArgs) && intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if ("android.provider.Telephony.SMS_DELIVER".equals(action)) {
                    intent.setAction("android.provider.Telephony.SMS_RECEIVED");
                    intent.addFlags(ApnSettingHelper.TYPE_WIFI_MMS);
                    intent.setComponent(null);
                    InboundSmsHandler.this.dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, InboundSmsHandler.this.handleSmsWhitelisting(null, false), this, UserHandle.ALL);
                } else if ("android.provider.Telephony.WAP_PUSH_DELIVER".equals(action)) {
                    intent.setAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                    intent.setComponent(null);
                    intent.addFlags(ApnSettingHelper.TYPE_WIFI_MMS);
                    try {
                        long duration = InboundSmsHandler.this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(InboundSmsHandler.this.mContext.getPackageName(), 0, "mms-broadcast");
                        BroadcastOptions bopts = BroadcastOptions.makeBasic();
                        bopts.setTemporaryAppWhitelistDuration(duration);
                        options = bopts.toBundle();
                    } catch (RemoteException e) {
                        options = null;
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

    /* access modifiers changed from: private */
    public final class CarrierServicesSmsFilterCallback implements CarrierServicesSmsFilter.CarrierServicesSmsFilterCallbackInterface {
        private final int mDestPort;
        private final boolean mIsClass0;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private final String mSmsFormat;
        private final boolean mUserUnlocked;

        CarrierServicesSmsFilterCallback(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver, boolean userUnlocked, boolean isClass0) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
            this.mUserUnlocked = userUnlocked;
            this.mIsClass0 = isClass0;
        }

        @Override // com.android.internal.telephony.CarrierServicesSmsFilter.CarrierServicesSmsFilterCallbackInterface
        public void onFilterComplete(int result) {
            InboundSmsHandler inboundSmsHandler = InboundSmsHandler.this;
            inboundSmsHandler.logv("onFilterComplete: result is " + result);
            if ((result & 1) != 0) {
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (VisualVoicemailSmsFilter.filter(InboundSmsHandler.this.mContext, this.mPdus, this.mSmsFormat, this.mDestPort, InboundSmsHandler.this.mPhone.getSubId())) {
                InboundSmsHandler.this.log("Visual voicemail SMS dropped");
                InboundSmsHandler.this.dropSms(this.mSmsBroadcastReceiver);
            } else if (this.mUserUnlocked) {
                InboundSmsHandler.this.dispatchSmsDeliveryIntent(this.mPdus, this.mSmsFormat, this.mDestPort, this.mSmsBroadcastReceiver, this.mIsClass0);
            } else {
                if (!InboundSmsHandler.this.isSkipNotifyFlagSet(result)) {
                    InboundSmsHandler.this.showNewMessageNotification();
                }
                InboundSmsHandler.this.sendMessage(3);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dropSms(SmsBroadcastReceiver receiver) {
        deleteFromRawTable(receiver.mDeleteWhere, receiver.mDeleteWhereArgs, 2);
        sendMessage(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & 2) > 0;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void log(String s) {
        Rlog.i(getName(), s);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    @UnsupportedAppUsage
    private Uri writeInboxMessage(Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length < 1) {
            loge("Failed to parse SMS pdu");
            return null;
        }
        for (SmsMessage sms : messages) {
            if (sms == null) {
                loge("Can't write null SmsMessage");
                return null;
            }
        }
        ContentValues values = parseSmsMessage(messages);
        long identity = Binder.clearCallingIdentity();
        try {
            return this.mContext.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values);
        } catch (Exception e) {
            loge("Failed to persist inbox message", e);
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
        values.put("seen", (Integer) 0);
        values.put("read", (Integer) 0);
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

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        InboundSmsHandler.super.dump(fd, pw, args);
        CellBroadcastHandler cellBroadcastHandler = this.mCellBroadcastHandler;
        if (cellBroadcastHandler != null) {
            cellBroadcastHandler.dump(fd, pw, args);
        }
        this.mLocalLog.dump(fd, pw, args);
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? PhoneConfigurationManager.SSSS : s.replace('\f', '\n');
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
    /* access modifiers changed from: public */
    private void setWakeLockTimeout(int timeOut) {
        this.mWakeLockTimeout = timeOut;
    }

    /* access modifiers changed from: private */
    public static class NewMessageNotificationActionReceiver extends BroadcastReceiver {
        private NewMessageNotificationActionReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (InboundSmsHandler.ACTION_OPEN_SMS_APP.equals(intent.getAction()) && ((UserManager) context.getSystemService("user")).isUserUnlocked()) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(Telephony.Sms.getDefaultSmsPackage(context)));
            }
        }
    }

    static void registerNewMessageNotificationActionHandler(Context context) {
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ACTION_OPEN_SMS_APP);
        context.registerReceiver(new NewMessageNotificationActionReceiver(), userFilter);
    }

    public static int getBroadcastCompleteEventHw() {
        return 3;
    }

    private int dispatchMessage(SmsMessageBase smsb) {
        return dispatchMessage(smsb, true);
    }

    public boolean isWapPushDeliverActionAndMmsMessage(Intent intent) {
        if (intent != null && "android.provider.Telephony.WAP_PUSH_DELIVER".equals(intent.getAction()) && "application/vnd.wap.mms-message".equals(intent.getType())) {
            return true;
        }
        return false;
    }

    public String getNumberIfWapPushDeliverActionAndMmsMessage(Intent intent) {
        byte[] pushDatas;
        GenericPdu pdu;
        EncodedStringValue fromValue;
        if (intent == null || (pushDatas = intent.getByteArrayExtra("data")) == null || (pdu = new PduParser(pushDatas, false).parse()) == null || pdu.getMessageType() != 130 || (fromValue = pdu.getFrom()) == null) {
            return null;
        }
        return fromValue.getString();
    }
}

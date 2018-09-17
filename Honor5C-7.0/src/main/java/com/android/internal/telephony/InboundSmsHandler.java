package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.BroadcastOptions;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.SettingsEx.Systemex;
import android.provider.Telephony.Carriers;
import android.provider.Telephony.CellBroadcasts;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.provider.Telephony.TextBasedSmsColumns;
import android.provider.Telephony.ThreadsColumns;
import android.service.carrier.ICarrierMessagingCallback.Stub;
import android.service.carrier.ICarrierMessagingService;
import android.service.carrier.MessagePdu;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Jlog;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.huawei.internal.telephony.HwRadarUtils;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class InboundSmsHandler extends StateMachine {
    public static final int ADDRESS_COLUMN = 6;
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    public static final int DESTINATION_PORT_COLUMN = 2;
    protected static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 8;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    private static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    private static final int EVENT_UPDATE_PHONE_OBJECT = 7;
    public static final int ID_COLUMN = 7;
    private static final String INTERCEPTION_SMS_RECEIVED = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
    private static final int MAX_SMS_LIST_DEFAULT = 30;
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    private static final int NO_DESTINATION_PORT = -1;
    static final long PARTIAL_SEGMENT_EXPIRE_TIME = 259200000;
    public static final int PDU_COLUMN = 0;
    private static final String[] PDU_PROJECTION = null;
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = null;
    private static final String RECEIVE_SMS_PERMISSION = "huawei.permission.RECEIVE_SMS_INTERCEPTION";
    public static final int RECEIVE_TIME_COLUMN = 9;
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final String SELECT_BY_REFERENCE = "address=? AND reference_number=? AND count=? AND deleted=0";
    public static final int SEQUENCE_COLUMN = 1;
    private static final int SMS_BROADCAST_DURATION_TIMEOUT = 180000;
    private static final int SMS_INSERTDB_DURATION_TIMEOUT = 60000;
    private static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    protected static final Uri sRawUri = null;
    protected static final Uri sRawUriPermanentDelete = null;
    private final int DELETE_PERMANENTLY;
    private final int MARK_DELETED;
    private String defaultSmsApplicationName;
    private boolean isAlreadyDurationTimeout;
    private boolean isClass0;
    private AtomicInteger mAlreadyReceivedSms;
    protected CellBroadcastHandler mCellBroadcastHandler;
    protected final Context mContext;
    private final DefaultState mDefaultState;
    private final DeliveringState mDeliveringState;
    IDeviceIdleController mDeviceIdleController;
    private final IdleState mIdleState;
    private ContentObserver mInsertObserver;
    protected Phone mPhone;
    private final ContentResolver mResolver;
    private ArrayList<byte[]> mSmsList;
    private final boolean mSmsReceiveDisabled;
    private final StartupState mStartupState;
    protected SmsStorageMonitor mStorageMonitor;
    private Runnable mUpdateCountRunner;
    private UserManager mUserManager;
    private final WaitingState mWaitingState;
    private final WakeLock mWakeLock;
    private final WapPushOverSms mWapPush;
    private int subIdForReceivedSms;

    /* renamed from: com.android.internal.telephony.InboundSmsHandler.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfUpdate) {
            InboundSmsHandler.this.mAlreadyReceivedSms.getAndDecrement();
            if (InboundSmsHandler.this.mAlreadyReceivedSms.get() < 0) {
                InboundSmsHandler.this.mAlreadyReceivedSms.set(InboundSmsHandler.PDU_COLUMN);
            }
        }
    }

    private final class CarrierSmsFilter extends CarrierMessagingServiceManager {
        private final int mDestPort;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private volatile CarrierSmsFilterCallback mSmsFilterCallback;
        private final String mSmsFormat;

        CarrierSmsFilter(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
        }

        void filterSms(String carrierPackageName, CarrierSmsFilterCallback smsFilterCallback) {
            this.mSmsFilterCallback = smsFilterCallback;
            if (bindToCarrierMessagingService(InboundSmsHandler.this.mContext, carrierPackageName)) {
                InboundSmsHandler.this.logv("bindService() for carrier messaging service succeeded");
                return;
            }
            InboundSmsHandler.this.loge("bindService() for carrier messaging service failed");
            smsFilterCallback.onFilterComplete(InboundSmsHandler.PDU_COLUMN);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                carrierMessagingService.filterSms(new MessagePdu(Arrays.asList(this.mPdus)), this.mSmsFormat, this.mDestPort, InboundSmsHandler.this.mPhone.getSubId(), this.mSmsFilterCallback);
            } catch (RemoteException e) {
                InboundSmsHandler.this.loge("Exception filtering the SMS: " + e);
                this.mSmsFilterCallback.onFilterComplete(InboundSmsHandler.PDU_COLUMN);
            }
        }
    }

    private final class CarrierSmsFilterCallback extends Stub {
        private final CarrierSmsFilter mSmsFilter;
        private final boolean mUserUnlocked;

        CarrierSmsFilterCallback(CarrierSmsFilter smsFilter, boolean userUnlocked) {
            this.mSmsFilter = smsFilter;
            this.mUserUnlocked = userUnlocked;
        }

        public void onFilterComplete(int result) {
            this.mSmsFilter.disposeConnection(InboundSmsHandler.this.mContext);
            InboundSmsHandler.this.logv("onFilterComplete: result is " + result);
            if ((result & InboundSmsHandler.SEQUENCE_COLUMN) != 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    InboundSmsHandler.this.deleteFromRawTable(this.mSmsFilter.mSmsBroadcastReceiver.mDeleteWhere, this.mSmsFilter.mSmsBroadcastReceiver.mDeleteWhereArgs, InboundSmsHandler.EVENT_BROADCAST_SMS);
                    InboundSmsHandler.this.sendMessage(InboundSmsHandler.EVENT_BROADCAST_COMPLETE);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else if (this.mUserUnlocked) {
                InboundSmsHandler.this.dispatchSmsDeliveryIntent(this.mSmsFilter.mPdus, this.mSmsFilter.mSmsFormat, this.mSmsFilter.mDestPort, this.mSmsFilter.mSmsBroadcastReceiver);
            } else {
                if (!InboundSmsHandler.this.isSkipNotifyFlagSet(result)) {
                    InboundSmsHandler.this.showNewMessageNotification();
                }
                InboundSmsHandler.this.sendMessage(InboundSmsHandler.EVENT_BROADCAST_COMPLETE);
            }
        }

        public void onSendSmsComplete(int result, int messageRef) {
            InboundSmsHandler.this.loge("Unexpected onSendSmsComplete call with result: " + result);
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            InboundSmsHandler.this.loge("Unexpected onSendMultipartSmsComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            InboundSmsHandler.this.loge("Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            InboundSmsHandler.this.loge("Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case InboundSmsHandler.ID_COLUMN /*7*/:
                    InboundSmsHandler.this.onUpdatePhoneObject((Phone) msg.obj);
                    break;
                default:
                    String errorText = "processMessage: unhandled message type " + msg.what + " currState=" + InboundSmsHandler.this.getCurrentState().getName();
                    if (!Build.IS_DEBUGGABLE) {
                        InboundSmsHandler.this.loge(errorText);
                        break;
                    }
                    InboundSmsHandler.this.loge("---- Dumping InboundSmsHandler ----");
                    InboundSmsHandler.this.loge("Total records=" + InboundSmsHandler.this.getLogRecCount());
                    for (int i = Math.max(InboundSmsHandler.this.getLogRecSize() - 20, InboundSmsHandler.PDU_COLUMN); i < InboundSmsHandler.this.getLogRecSize(); i += InboundSmsHandler.SEQUENCE_COLUMN) {
                        if (InboundSmsHandler.this.getLogRec(i) != null) {
                            InboundSmsHandler.this.loge("Rec[%d]: %s\n" + i + InboundSmsHandler.this.getLogRec(i).toString());
                        }
                    }
                    InboundSmsHandler.this.loge("---- Dumped InboundSmsHandler ----");
                    throw new RuntimeException(errorText);
            }
            return InboundSmsHandler.DBG;
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
            InboundSmsHandler.this.log("DeliveringState.processMessage:" + msg.what);
            switch (msg.what) {
                case InboundSmsHandler.SEQUENCE_COLUMN /*1*/:
                    InboundSmsHandler.this.handleNewSms((AsyncResult) msg.obj);
                    InboundSmsHandler.this.sendMessage(InboundSmsHandler.REFERENCE_NUMBER_COLUMN);
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.EVENT_BROADCAST_SMS /*2*/:
                    if (InboundSmsHandler.this.processMessagePart(msg.obj)) {
                        InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mWaitingState);
                    } else {
                        InboundSmsHandler.this.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                        InboundSmsHandler.this.sendMessage(InboundSmsHandler.REFERENCE_NUMBER_COLUMN);
                    }
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.REFERENCE_NUMBER_COLUMN /*4*/:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.EVENT_RELEASE_WAKELOCK /*5*/:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (!InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.loge("mWakeLock released while delivering/broadcasting!");
                    }
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.MESSAGE_BODY_COLUMN /*8*/:
                    InboundSmsHandler.this.handleInjectSms((AsyncResult) msg.obj);
                    InboundSmsHandler.this.sendMessage(InboundSmsHandler.REFERENCE_NUMBER_COLUMN);
                    return InboundSmsHandler.DBG;
                default:
                    return InboundSmsHandler.VDBG;
            }
        }
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Idle state");
            InboundSmsHandler.this.sendMessageDelayed(InboundSmsHandler.EVENT_RELEASE_WAKELOCK, 3000);
        }

        public void exit() {
            InboundSmsHandler.this.mWakeLock.acquire();
            InboundSmsHandler.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("IdleState.processMessage:" + msg.what);
            InboundSmsHandler.this.log("Idle state processing message type " + msg.what);
            switch (msg.what) {
                case InboundSmsHandler.SEQUENCE_COLUMN /*1*/:
                case InboundSmsHandler.EVENT_BROADCAST_SMS /*2*/:
                case InboundSmsHandler.MESSAGE_BODY_COLUMN /*8*/:
                    InboundSmsHandler.this.deferMessage(msg);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.REFERENCE_NUMBER_COLUMN /*4*/:
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.EVENT_RELEASE_WAKELOCK /*5*/:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.log("mWakeLock is still held after release");
                    } else {
                        InboundSmsHandler.this.log("mWakeLock released");
                    }
                    return InboundSmsHandler.DBG;
                default:
                    return InboundSmsHandler.VDBG;
            }
        }
    }

    private final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano;
        private final String mDeleteWhere;
        private final String[] mDeleteWhereArgs;

        SmsBroadcastReceiver(InboundSmsTracker tracker) {
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
            this.mBroadcastTimeNano = System.nanoTime();
        }

        public void onReceive(Context context, Intent intent) {
            if (HwTelephonyFactory.getHwInnerSmsManager().shouldSendReceivedActionInPrivacyMode(InboundSmsHandler.this, context, intent, this.mDeleteWhere, this.mDeleteWhereArgs)) {
                String action = intent.getAction();
                if (action.equals(Intents.SMS_DELIVER_ACTION)) {
                    intent.setAction(Intents.SMS_RECEIVED_ACTION);
                    intent.setComponent(null);
                    Intent intent2 = intent;
                    InboundSmsHandler.this.dispatchIntent(intent2, "android.permission.RECEIVE_SMS", 16, InboundSmsHandler.this.handleSmsWhitelisting(null), this, UserHandle.ALL);
                } else if (action.equals(Intents.WAP_PUSH_DELIVER_ACTION)) {
                    intent.setAction(Intents.WAP_PUSH_RECEIVED_ACTION);
                    intent.setComponent(null);
                    Bundle options = null;
                    try {
                        long duration = InboundSmsHandler.this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(InboundSmsHandler.this.mContext.getPackageName(), InboundSmsHandler.PDU_COLUMN, "mms-broadcast");
                        BroadcastOptions bopts = BroadcastOptions.makeBasic();
                        bopts.setTemporaryAppWhitelistDuration(duration);
                        options = bopts.toBundle();
                    } catch (RemoteException e) {
                    }
                    String mimeType = intent.getType();
                    InboundSmsHandler.this.dispatchIntent(intent, WapPushOverSms.getPermissionForType(mimeType), WapPushOverSms.getAppOpsPermissionForIntent(mimeType), options, this, UserHandle.SYSTEM);
                } else {
                    int rc;
                    if (!(Intents.DATA_SMS_RECEIVED_ACTION.equals(action) || Intents.SMS_RECEIVED_ACTION.equals(action) || Intents.DATA_SMS_RECEIVED_ACTION.equals(action) || Intents.WAP_PUSH_RECEIVED_ACTION.equals(action))) {
                        InboundSmsHandler.this.loge("unexpected BroadcastReceiver action: " + action);
                    }
                    if ("true".equals(Systemex.getString(InboundSmsHandler.this.mResolver, "disableErrWhenReceiveSMS"))) {
                        rc = InboundSmsHandler.NO_DESTINATION_PORT;
                    } else {
                        rc = getResultCode();
                    }
                    if (rc == InboundSmsHandler.NO_DESTINATION_PORT || rc == InboundSmsHandler.SEQUENCE_COLUMN) {
                        InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                    } else {
                        InboundSmsHandler.this.loge("a broadcast receiver set the result code to " + rc + ", deleting from raw table anyway!");
                    }
                    InboundSmsHandler.this.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, InboundSmsHandler.EVENT_BROADCAST_SMS);
                    InboundSmsHandler.this.sendMessage(InboundSmsHandler.EVENT_BROADCAST_COMPLETE);
                    int durationMillis = (int) ((System.nanoTime() - this.mBroadcastTimeNano) / 1000000);
                    if (durationMillis >= AbstractPhoneBase.SET_TO_AOTO_TIME) {
                        InboundSmsHandler.this.loge("Slow ordered broadcast completion time: " + durationMillis + " ms");
                    } else {
                        InboundSmsHandler.this.log("ordered broadcast completed in: " + durationMillis + " ms");
                    }
                    InboundSmsHandler.this.reportSmsReceiveTimeout(durationMillis);
                }
            }
        }
    }

    private class StartupState extends State {
        private StartupState() {
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("StartupState.processMessage:" + msg.what);
            switch (msg.what) {
                case InboundSmsHandler.SEQUENCE_COLUMN /*1*/:
                case InboundSmsHandler.EVENT_BROADCAST_SMS /*2*/:
                case InboundSmsHandler.MESSAGE_BODY_COLUMN /*8*/:
                    InboundSmsHandler.this.deferMessage(msg);
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.EVENT_START_ACCEPTING_SMS /*6*/:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return InboundSmsHandler.DBG;
                default:
                    return InboundSmsHandler.VDBG;
            }
        }
    }

    private class WaitingState extends State {
        private WaitingState() {
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("WaitingState.processMessage:" + msg.what);
            switch (msg.what) {
                case InboundSmsHandler.EVENT_BROADCAST_SMS /*2*/:
                    InboundSmsHandler.this.deferMessage(msg);
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.EVENT_BROADCAST_COMPLETE /*3*/:
                    InboundSmsHandler.this.sendMessage(InboundSmsHandler.REFERENCE_NUMBER_COLUMN);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return InboundSmsHandler.DBG;
                case InboundSmsHandler.REFERENCE_NUMBER_COLUMN /*4*/:
                    return InboundSmsHandler.DBG;
                default:
                    return InboundSmsHandler.VDBG;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.InboundSmsHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.InboundSmsHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.<clinit>():void");
    }

    private int addTrackerToRawTable(com.android.internal.telephony.InboundSmsTracker r29, boolean r30) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0155 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r28 = this;
        r8 = r29.getAddress();
        r2 = r29.getReferenceNumber();
        r22 = java.lang.Integer.toString(r2);
        r2 = r29.getMessageCount();
        r9 = java.lang.Integer.toString(r2);
        if (r30 == 0) goto L_0x015d;
    L_0x0016:
        r10 = 0;
        r26 = r29.getSequenceNumber();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r23 = java.lang.Integer.toString(r26);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r29.getTimestamp();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r11 = java.lang.Long.toString(r2);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r15 = r29.getMessageBody();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r28;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r0.mResolver;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = sRawUri;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r4 = PDU_PROJECTION;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r5 = "address=? AND reference_number=? AND count=? AND sequence=? AND date=? AND message_body=?";	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6 = 6;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6 = new java.lang.String[r6];	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r7 = 0;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6[r7] = r8;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r7 = 1;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6[r7] = r22;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r7 = 2;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6[r7] = r9;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r7 = 3;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6[r7] = r23;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r7 = 4;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6[r7] = r11;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r7 = 5;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r6[r7] = r15;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r7 = 0;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r10.moveToNext();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        if (r2 == 0) goto L_0x00d3;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
    L_0x0056:
        r2 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2.<init>();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = "Discarding duplicate message segment, refNumber=";	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r22;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r0);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = " seqNumber=";	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r23;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r0);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = " count=";	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r9);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.toString();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r28;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0.loge(r2);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = 0;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r18 = r10.getString(r2);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r19 = r29.getPdu();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r17 = com.android.internal.util.HexDump.hexStringToByteArray(r18);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r29.getPdu();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r17;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = java.util.Arrays.equals(r0, r2);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        if (r2 != 0) goto L_0x00cc;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
    L_0x00a2:
        r2 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2.<init>();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = "Warning: dup message segment PDU of length ";	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r19;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = r0.length;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = " is different from existing PDU of length ";	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r17;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r3 = r0.length;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = r2.toString();	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r28;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0.loge(r2);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
    L_0x00cc:
        r2 = 5;
        if (r10 == 0) goto L_0x00d2;
    L_0x00cf:
        r10.close();
    L_0x00d2:
        return r2;
    L_0x00d3:
        if (r10 == 0) goto L_0x00d8;
    L_0x00d5:
        r10.close();
    L_0x00d8:
        r27 = r29.getContentValues();
        r2 = "sub_id";
        r0 = r28;
        r3 = r0.mPhone;
        r3 = r3.getSubId();
        r3 = java.lang.Integer.valueOf(r3);
        r0 = r27;
        r0.put(r2, r3);
        r20 = java.lang.System.currentTimeMillis();
        r2 = "receive_time";
        r3 = java.lang.Long.valueOf(r20);
        r0 = r27;
        r0.put(r2, r3);
        r0 = r28;
        r2 = r0.mResolver;
        r3 = sRawUri;
        r0 = r27;
        r16 = r2.insert(r3, r0);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "URI of new row -> ";
        r2 = r2.append(r3);
        r0 = r16;
        r2 = r2.append(r0);
        r2 = r2.toString();
        r0 = r28;
        r0.log(r2);
        r24 = android.content.ContentUris.parseId(r16);	 Catch:{ Exception -> 0x017c }
        r2 = r29.getMessageCount();	 Catch:{ Exception -> 0x017c }
        r3 = 1;	 Catch:{ Exception -> 0x017c }
        if (r2 != r3) goto L_0x0167;	 Catch:{ Exception -> 0x017c }
    L_0x0132:
        r2 = "_id=?";	 Catch:{ Exception -> 0x017c }
        r3 = 1;	 Catch:{ Exception -> 0x017c }
        r3 = new java.lang.String[r3];	 Catch:{ Exception -> 0x017c }
        r4 = java.lang.Long.toString(r24);	 Catch:{ Exception -> 0x017c }
        r5 = 0;	 Catch:{ Exception -> 0x017c }
        r3[r5] = r4;	 Catch:{ Exception -> 0x017c }
        r0 = r29;	 Catch:{ Exception -> 0x017c }
        r0.setDeleteWhere(r2, r3);	 Catch:{ Exception -> 0x017c }
    L_0x0144:
        r2 = 1;
        return r2;
    L_0x0146:
        r13 = move-exception;
        r2 = "Can't access SMS database";	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0 = r28;	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r0.loge(r2, r13);	 Catch:{ SQLException -> 0x0146, all -> 0x0156 }
        r2 = 2;
        if (r10 == 0) goto L_0x0155;
    L_0x0152:
        r10.close();
    L_0x0155:
        return r2;
    L_0x0156:
        r2 = move-exception;
        if (r10 == 0) goto L_0x015c;
    L_0x0159:
        r10.close();
    L_0x015c:
        throw r2;
    L_0x015d:
        r2 = "Skipped message de-duping logic";
        r0 = r28;
        r0.logd(r2);
        goto L_0x00d8;
    L_0x0167:
        r2 = 3;
        r12 = new java.lang.String[r2];	 Catch:{ Exception -> 0x017c }
        r2 = 0;	 Catch:{ Exception -> 0x017c }
        r12[r2] = r8;	 Catch:{ Exception -> 0x017c }
        r2 = 1;	 Catch:{ Exception -> 0x017c }
        r12[r2] = r22;	 Catch:{ Exception -> 0x017c }
        r2 = 2;	 Catch:{ Exception -> 0x017c }
        r12[r2] = r9;	 Catch:{ Exception -> 0x017c }
        r2 = "address=? AND reference_number=? AND count=? AND deleted=0";	 Catch:{ Exception -> 0x017c }
        r0 = r29;	 Catch:{ Exception -> 0x017c }
        r0.setDeleteWhere(r2, r12);	 Catch:{ Exception -> 0x017c }
        goto L_0x0144;
    L_0x017c:
        r14 = move-exception;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "error parsing URI for new row: ";
        r2 = r2.append(r3);
        r0 = r16;
        r2 = r2.append(r0);
        r2 = r2.toString();
        r0 = r28;
        r0.loge(r2, r14);
        r2 = 2;
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.addTrackerToRawTable(com.android.internal.telephony.InboundSmsTracker, boolean):int");
    }

    private boolean processMessagePart(com.android.internal.telephony.InboundSmsTracker r43) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x014f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r42 = this;
        r4 = r43.isOldMessageInRawTable();
        if (r4 == 0) goto L_0x0010;
    L_0x0006:
        r4 = "processMessagePart, ignore old message kept in raw table.";
        r0 = r42;
        r0.log(r4);
        r4 = 0;
        return r4;
    L_0x0010:
        r34 = r43.getMessageCount();
        r23 = r43.getDestPort();
        r4 = 1;
        r0 = r34;
        if (r0 != r4) goto L_0x0060;
    L_0x001d:
        r4 = 1;
        r0 = new byte[r4][];
        r22 = r0;
        r4 = r43.getPdu();
        r5 = 0;
        r22[r5] = r4;
    L_0x0029:
        r38 = java.util.Arrays.asList(r22);
        r4 = r38.size();
        if (r4 == 0) goto L_0x003c;
    L_0x0033:
        r4 = 0;
        r0 = r38;
        r4 = r0.contains(r4);
        if (r4 == 0) goto L_0x015c;
    L_0x003c:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "processMessagePart: returning false due to ";
        r5 = r4.append(r5);
        r4 = r38.size();
        if (r4 != 0) goto L_0x0157;
    L_0x004e:
        r4 = "pduList.size() == 0";
    L_0x0051:
        r4 = r5.append(r4);
        r4 = r4.toString();
        r0 = r42;
        r0.loge(r4);
        r4 = 0;
        return r4;
    L_0x0060:
        r42.scanAndDeleteOlderPartialMessages(r43);
        r30 = 0;
        r27 = r43.getAddress();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r43.getReferenceNumber();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r40 = java.lang.Integer.toString(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r43.getMessageCount();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r29 = java.lang.Integer.toString(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = 3;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r8 = new java.lang.String[r4];	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = 0;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r8[r4] = r27;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = 1;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r8[r4] = r40;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = 2;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r8[r4] = r29;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r42;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r0.mResolver;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = sRawUri;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r6 = PDU_SEQUENCE_PORT_PROJECTION;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r7 = "address=? AND reference_number=? AND count=? AND deleted=0";	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r9 = 0;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r30 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r31 = r30.getCount();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4.<init>();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = "processMessagePart, cursorCount: ";	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r31;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r0);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = ", ref|seq/count(";	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = r43.getReferenceNumber();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = "|";	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = r43.getSequenceNumber();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = "/";	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r34;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r0);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = ")";	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r4.toString();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r42;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0.log(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r31;
        r1 = r34;
        if (r0 >= r1) goto L_0x00f3;
    L_0x00ec:
        r4 = 0;
        if (r30 == 0) goto L_0x00f2;
    L_0x00ef:
        r30.close();
    L_0x00f2:
        return r4;
    L_0x00f3:
        r0 = r34;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = new byte[r0][];	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r22 = r0;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
    L_0x00f9:
        r4 = r30.moveToNext();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        if (r4 == 0) goto L_0x0137;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
    L_0x00ff:
        r4 = 1;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r30;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r0.getInt(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r5 = r43.getIndexOffset();	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r33 = r4 - r5;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = 0;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r30;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r0.getString(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = com.android.internal.util.HexDump.hexStringToByteArray(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r22[r33] = r4;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        if (r33 != 0) goto L_0x00f9;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
    L_0x011b:
        r4 = 2;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r30;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = r0.isNull(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        if (r4 != 0) goto L_0x00f9;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
    L_0x0124:
        r4 = 2;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r30;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r39 = r0.getInt(r4);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r39 = com.android.internal.telephony.InboundSmsTracker.getRealDestPort(r39);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = -1;
        r0 = r39;
        if (r0 == r4) goto L_0x00f9;
    L_0x0134:
        r23 = r39;
        goto L_0x00f9;
    L_0x0137:
        if (r30 == 0) goto L_0x0029;
    L_0x0139:
        r30.close();
        goto L_0x0029;
    L_0x013e:
        r32 = move-exception;
        r4 = "Can't access multipart SMS database";	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0 = r42;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r1 = r32;	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r0.loge(r4, r1);	 Catch:{ SQLException -> 0x013e, all -> 0x0150 }
        r4 = 0;
        if (r30 == 0) goto L_0x014f;
    L_0x014c:
        r30.close();
    L_0x014f:
        return r4;
    L_0x0150:
        r4 = move-exception;
        if (r30 == 0) goto L_0x0156;
    L_0x0153:
        r30.close();
    L_0x0156:
        throw r4;
    L_0x0157:
        r4 = "pduList.contains(null)";
        goto L_0x0051;
    L_0x015c:
        r0 = r42;
        r4 = r0.mUserManager;
        r4 = r4.isUserUnlocked();
        if (r4 != 0) goto L_0x0173;
    L_0x0166:
        r0 = r42;
        r1 = r43;
        r2 = r22;
        r3 = r23;
        r4 = r0.processMessagePartWithUserLocked(r1, r2, r3);
        return r4;
    L_0x0173:
        r12 = new com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver;
        r0 = r42;
        r1 = r43;
        r12.<init>(r1);
        r4 = 2948; // 0xb84 float:4.131E-42 double:1.4565E-320;
        r0 = r23;
        if (r0 != r4) goto L_0x020e;
    L_0x0182:
        r36 = new java.io.ByteArrayOutputStream;
        r36.<init>();
        r11 = 0;
        r4 = 0;
        r0 = r22;
        r5 = r0.length;
    L_0x018c:
        if (r4 >= r5) goto L_0x01c1;
    L_0x018e:
        r37 = r22[r4];
        r6 = r43.is3gpp2();
        if (r6 != 0) goto L_0x01a9;
    L_0x0196:
        r6 = "3gpp";
        r0 = r37;
        r35 = android.telephony.SmsMessage.createFromPdu(r0, r6);
        if (r35 == 0) goto L_0x01b7;
    L_0x01a1:
        r37 = r35.getUserData();
        r11 = r35.getOriginatingAddress();
    L_0x01a9:
        r0 = r37;
        r6 = r0.length;
        r7 = 0;
        r0 = r36;
        r1 = r37;
        r0.write(r1, r7, r6);
        r4 = r4 + 1;
        goto L_0x018c;
    L_0x01b7:
        r4 = "processMessagePart: SmsMessage.createFromPdu returned null";
        r0 = r42;
        r0.loge(r4);
        r4 = 0;
        return r4;
    L_0x01c1:
        r0 = r42;
        r4 = r0.mWapPush;
        r0 = r43;
        r4.saveSmsTracker(r0);
        r0 = r42;
        r9 = r0.mWapPush;
        r10 = r36.toByteArray();
        r14 = r43.is3gpp2();
        r13 = r42;
        r41 = r9.dispatchWapPdu(r10, r11, r12, r13, r14);
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "dispatchWapPdu() returned ";
        r4 = r4.append(r5);
        r0 = r41;
        r4 = r4.append(r0);
        r4 = r4.toString();
        r0 = r42;
        r0.log(r4);
        r4 = -1;
        r0 = r41;
        if (r0 != r4) goto L_0x01fe;
    L_0x01fc:
        r4 = 1;
        return r4;
    L_0x01fe:
        r4 = r43.getDeleteWhere();
        r5 = r43.getDeleteWhereArgs();
        r6 = 2;
        r0 = r42;
        r0.deleteFromRawTable(r4, r5, r6);
        r4 = 0;
        return r4;
    L_0x020e:
        r15 = new android.content.Intent;
        r15.<init>();
        r4 = "pdus";
        r0 = r22;
        r15.putExtra(r4, r0);
        r4 = "format";
        r5 = r43.getFormat();
        r15.putExtra(r4, r5);
        r0 = r42;
        r4 = r0.mPhone;
        r4 = r4.getPhoneId();
        android.telephony.SubscriptionManager.putPhoneIdAndSubIdExtra(r15, r4);
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "putPhoneIdAndSubIdExtra";
        r4 = r4.append(r5);
        r0 = r42;
        r5 = r0.mPhone;
        r5 = r5.getPhoneId();
        r4 = r4.append(r5);
        r4 = r4.toString();
        r0 = r42;
        r0.log(r4);
        r4 = -1;
        r0 = r23;
        if (r4 != r0) goto L_0x027b;
    L_0x0256:
        r0 = r42;
        r4 = r0.mContext;
        r5 = r43.getAddress();
        r4 = com.android.internal.telephony.BlockChecker.isBlocked(r4, r5);
        if (r4 == 0) goto L_0x027b;
    L_0x0264:
        r4 = com.android.internal.telephony.HwTelephonyFactory.getHwInnerSmsManager();
        r4.sendGoogleSmsBlockedRecord(r15);
        r4 = r43.getDeleteWhere();
        r5 = r43.getDeleteWhereArgs();
        r6 = 2;
        r0 = r42;
        r0.deleteFromRawTable(r4, r5, r6);
        r4 = 0;
        return r4;
    L_0x027b:
        r4 = -1;
        r0 = r23;
        if (r4 != r0) goto L_0x02c0;
    L_0x0280:
        r13 = com.android.internal.telephony.HwTelephonyFactory.getHwInnerSmsManager();
        r0 = r42;
        r14 = r0.mContext;
        r17 = r43.getDeleteWhere();
        r18 = r43.getDeleteWhereArgs();
        r19 = 0;
        r16 = r42;
        r4 = r13.newSmsShouldBeIntercepted(r14, r15, r16, r17, r18, r19);
        if (r4 == 0) goto L_0x02c0;
    L_0x029a:
        r17 = new android.content.Intent;
        r0 = r17;
        r0.<init>(r15);
        r4 = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
        r0 = r17;
        r0.setAction(r4);
        r4 = 0;
        r0 = r42;
        r20 = r0.handleSmsWhitelisting(r4);
        r18 = "huawei.permission.RECEIVE_SMS_INTERCEPTION";
        r22 = android.os.UserHandle.ALL;
        r19 = 16;
        r21 = 0;
        r16 = r42;
        r16.dispatchIntent(r17, r18, r19, r20, r21, r22);
        r4 = 1;
        return r4;
    L_0x02c0:
        r26 = 1;
        r21 = r42;
        r24 = r43;
        r25 = r12;
        r28 = r21.filterSmsWithCarrierOrSystemApp(r22, r23, r24, r25, r26);
        if (r28 != 0) goto L_0x02db;
    L_0x02ce:
        r4 = r43.getFormat();
        r0 = r42;
        r1 = r22;
        r2 = r23;
        r0.dispatchSmsDeliveryIntent(r1, r4, r2, r12);
    L_0x02db:
        r4 = 1;
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.processMessagePart(com.android.internal.telephony.InboundSmsTracker):boolean");
    }

    protected abstract void acknowledgeLastIncomingSms(boolean z, int i, Message message);

    protected abstract int dispatchMessageRadioSpecific(SmsMessageBase smsMessageBase);

    protected abstract boolean is3gpp2();

    protected InboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        boolean z = VDBG;
        super(name);
        this.mSmsList = new ArrayList();
        this.mDefaultState = new DefaultState();
        this.mStartupState = new StartupState();
        this.mIdleState = new IdleState();
        this.mDeliveringState = new DeliveringState();
        this.mWaitingState = new WaitingState();
        this.subIdForReceivedSms = NO_DESTINATION_PORT;
        this.defaultSmsApplicationName = "";
        this.isAlreadyDurationTimeout = VDBG;
        this.mAlreadyReceivedSms = new AtomicInteger(PDU_COLUMN);
        this.isClass0 = VDBG;
        this.DELETE_PERMANENTLY = SEQUENCE_COLUMN;
        this.MARK_DELETED = EVENT_BROADCAST_SMS;
        this.mUpdateCountRunner = new Runnable() {
            public void run() {
                if (InboundSmsHandler.this.mAlreadyReceivedSms.get() > 0) {
                    HwRadarUtils.report(InboundSmsHandler.this.mContext, HwRadarUtils.ERR_SMS_RECEIVE, "sms receive fail:" + InboundSmsHandler.this.defaultSmsApplicationName, InboundSmsHandler.this.subIdForReceivedSms);
                }
                InboundSmsHandler.this.mAlreadyReceivedSms.set(InboundSmsHandler.PDU_COLUMN);
            }
        };
        this.mInsertObserver = new AnonymousClass2(new Handler());
        this.mContext = context;
        this.mStorageMonitor = storageMonitor;
        this.mPhone = phone;
        this.mCellBroadcastHandler = cellBroadcastHandler;
        this.mResolver = context.getContentResolver();
        this.mWapPush = HwTelephonyFactory.getHwInnerSmsManager().createHwWapPushOverSms(context);
        HwTelephonyFactory.getHwInnerSmsManager().createSmsInterceptionService(context);
        if (!TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17956959))) {
            z = DBG;
        }
        this.mSmsReceiveDisabled = z;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(SEQUENCE_COLUMN, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService(Carriers.USER);
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        addInboxInsertObserver(this.mContext);
        log("created InboundSmsHandler");
    }

    public void dispose() {
        quit();
    }

    public void updatePhoneObject(Phone phone) {
        sendMessage(ID_COLUMN, phone);
    }

    protected void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    private void handleNewSms(AsyncResult ar) {
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return;
        }
        int result;
        try {
            SmsMessage sms = ar.result;
            result = dispatchMessage(sms.mWrappedSmsMessage);
            if (sms.mWrappedSmsMessage.blacklistFlag) {
                result = NO_DESTINATION_PORT;
                HwRadarUtils.report(this.mContext, HwRadarUtils.ERR_SMS_RECEIVE, "receive a blacklist sms, modem has acked it, fw need't reply" + this.defaultSmsApplicationName, PDU_COLUMN);
                log("receive a blacklist sms, modem has acked it, fw need't reply");
            }
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = EVENT_BROADCAST_SMS;
        }
        if (result != NO_DESTINATION_PORT) {
            boolean handled = result == SEQUENCE_COLUMN ? DBG : VDBG;
            if (!handled) {
                Jlog.d(50, "JL_DISPATCH_SMS_FAILED");
            }
            notifyAndAcknowledgeLastIncomingSms(handled, result, null);
        }
    }

    private void handleInjectSms(AsyncResult ar) {
        int result;
        PendingIntent pendingIntent = null;
        try {
            pendingIntent = (PendingIntent) ar.userObj;
            SmsMessage sms = ar.result;
            if (sms == null) {
                result = EVENT_BROADCAST_SMS;
            } else {
                result = dispatchMessage(sms.mWrappedSmsMessage);
            }
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = EVENT_BROADCAST_SMS;
        }
        if (pendingIntent != null) {
            try {
                pendingIntent.send(result);
            } catch (CanceledException e) {
            }
        }
    }

    private int dispatchMessage(SmsMessageBase smsb) {
        if (smsb == null) {
            loge("dispatchSmsMessage: message is null");
            return EVENT_BROADCAST_SMS;
        } else if (this.mSmsReceiveDisabled) {
            log("Received short message on device which doesn't support receiving SMS. Ignored.");
            return SEQUENCE_COLUMN;
        } else if (hasSameSmsPdu(smsb.getPdu())) {
            log("receive a duplicated SMS and abandon it.");
            return SEQUENCE_COLUMN;
        } else {
            boolean onlyCore = VDBG;
            try {
                onlyCore = IPackageManager.Stub.asInterface(ServiceManager.getService(Intents.EXTRA_PACKAGE_NAME)).isOnlyCoreApps();
            } catch (RemoteException e) {
            }
            if (!onlyCore) {
                return dispatchMessageRadioSpecific(smsb);
            }
            log("Received a short message in encrypted state. Rejecting.");
            return EVENT_BROADCAST_SMS;
        }
    }

    private boolean hasSameSmsPdu(byte[] pdu) {
        log("hasSameSmsPdu: check if there is a same pdu in mSmsList.");
        synchronized (this.mSmsList) {
            for (byte[] oldPdu : this.mSmsList) {
                if (Arrays.equals(pdu, oldPdu)) {
                    return DBG;
                }
            }
            this.mSmsList.add(pdu);
            log("hasSameSmsPdu: mSmsList.size() = " + this.mSmsList.size());
            if (this.mSmsList.size() > MAX_SMS_LIST_DEFAULT) {
                log("hasSameSmsPdu: mSmsList.size() > MAX_SMS_LIST_DEFAULT");
                this.mSmsList.remove(PDU_COLUMN);
            }
            return VDBG;
        }
    }

    protected void onUpdatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mStorageMonitor = this.mPhone.mSmsStorageMonitor;
        log("onUpdatePhoneObject: phone=" + this.mPhone.getClass().getSimpleName());
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent(Intents.SMS_REJECTED_ACTION);
            intent.putExtra("result", result);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    protected int dispatchNormalMessage(SmsMessageBase sms) {
        InboundSmsTracker tracker;
        SmsHeader smsHeader = sms.getUserDataHeader();
        this.isClass0 = sms.getMessageClass() == MessageClass.CLASS_0 ? DBG : VDBG;
        Jlog.d(48, "JL_DISPATCH_NORMAL_SMS");
        if (smsHeader == null || smsHeader.concatRef == null) {
            int destPort = NO_DESTINATION_PORT;
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
                log("destination port: " + destPort);
            }
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), VDBG, sms.getDisplayOriginatingAddress(), sms.getMessageBody());
        } else {
            ConcatRef concatRef = smsHeader.concatRef;
            PortAddrs portAddrs = smsHeader.portAddrs;
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), portAddrs != null ? portAddrs.destPort : NO_DESTINATION_PORT, is3gpp2(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, VDBG, sms.getMessageBody());
        }
        return addTrackerToRawTableAndSendMessage(tracker, tracker.getDestPort() == NO_DESTINATION_PORT ? DBG : VDBG);
    }

    protected int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        switch (addTrackerToRawTable(tracker, deDup)) {
            case SEQUENCE_COLUMN /*1*/:
                if (((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
                    Jlog.d(50, "JL_DISPATCH_SMS_FAILED");
                } else {
                    Jlog.d(49, "JL_SEND_BROADCAST_SMS");
                }
                sendMessage(EVENT_BROADCAST_SMS, tracker);
                return SEQUENCE_COLUMN;
            case EVENT_RELEASE_WAKELOCK /*5*/:
                return SEQUENCE_COLUMN;
            default:
                return EVENT_BROADCAST_SMS;
        }
    }

    private void scanAndDeleteOlderPartialMessages(InboundSmsTracker tracker) {
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        StringBuilder deleteWhere = new StringBuilder("date < " + (tracker.getTimestamp() - PARTIAL_SEGMENT_EXPIRE_TIME) + " AND " + SELECT_BY_REFERENCE);
        String[] deleteWhereArgs = new String[EVENT_BROADCAST_COMPLETE];
        deleteWhereArgs[PDU_COLUMN] = address;
        deleteWhereArgs[SEQUENCE_COLUMN] = refNumber;
        deleteWhereArgs[EVENT_BROADCAST_SMS] = count;
        int delCount = PDU_COLUMN;
        try {
            delCount = this.mResolver.delete(sRawUri, deleteWhere.toString(), deleteWhereArgs);
        } catch (Exception e) {
            loge("scanAndDeleteOlderPartialMessages got exception ", e);
        }
        if (delCount > 0) {
            log("scanAndDeleteOlderPartialMessages: delete " + delCount + " raw sms older than " + PARTIAL_SEGMENT_EXPIRE_TIME + " days for " + tracker.getAddress());
        }
    }

    private boolean processMessagePartWithUserLocked(InboundSmsTracker tracker, byte[][] pdus, int destPort) {
        log("Credential-encrypted storage not available. Port: " + destPort);
        if (destPort == SmsHeader.PORT_WAP_PUSH) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int length = pdus.length;
            for (int i = PDU_COLUMN; i < length; i += SEQUENCE_COLUMN) {
                byte[] pdu = pdus[i];
                if (!tracker.is3gpp2()) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdu, SmsMessage.FORMAT_3GPP);
                    if (msg != null) {
                        pdu = msg.getUserData();
                    } else {
                        loge("processMessagePartWithUserLocked: SmsMessage.createFromPdu returned null");
                        return VDBG;
                    }
                }
                output.write(pdu, PDU_COLUMN, pdu.length);
            }
            if (this.mWapPush.isWapPushForMms(output.toByteArray(), this)) {
                showNewMessageNotification();
                return VDBG;
            }
        }
        if (destPort != NO_DESTINATION_PORT) {
            return VDBG;
        }
        if (filterSmsWithCarrierOrSystemApp(pdus, destPort, tracker, null, VDBG)) {
            return DBG;
        }
        showNewMessageNotification();
        return VDBG;
    }

    private void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, SEQUENCE_COLUMN, new Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(DBG).setVisibility(SEQUENCE_COLUMN).setDefaults(NO_DESTINATION_PORT).setContentTitle(this.mContext.getString(17040846)).setContentText(this.mContext.getString(17040847)).setContentIntent(PendingIntent.getActivity(this.mContext, SEQUENCE_COLUMN, Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_MESSAGING"), PDU_COLUMN)).build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, SEQUENCE_COLUMN);
    }

    private boolean filterSmsWithCarrierOrSystemApp(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        List carrierPackages = null;
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card != null) {
            carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        } else {
            loge("UiccCard not initialized.");
        }
        List<String> systemPackages = getSystemAppForIntent(new Intent("android.service.carrier.CarrierMessagingService"));
        CarrierSmsFilter smsFilter;
        if (carrierPackages != null && carrierPackages.size() == SEQUENCE_COLUMN) {
            log("Found carrier package.");
            smsFilter = new CarrierSmsFilter(pdus, destPort, tracker.getFormat(), resultReceiver);
            smsFilter.filterSms((String) carrierPackages.get(PDU_COLUMN), new CarrierSmsFilterCallback(smsFilter, userUnlocked));
            return DBG;
        } else if (systemPackages == null || systemPackages.size() != SEQUENCE_COLUMN) {
            logv("Unable to find carrier package: " + carrierPackages + ", nor systemPackages: " + systemPackages);
            return VDBG;
        } else {
            log("Found system package.");
            smsFilter = new CarrierSmsFilter(pdus, destPort, tracker.getFormat(), resultReceiver);
            smsFilter.filterSms((String) systemPackages.get(PDU_COLUMN), new CarrierSmsFilterCallback(smsFilter, userUnlocked));
            return DBG;
        }
    }

    private List<String> getSystemAppForIntent(Intent intent) {
        List<String> packages = new ArrayList();
        PackageManager packageManager = this.mContext.getPackageManager();
        String carrierFilterSmsPerm = "android.permission.CARRIER_FILTER_SMS";
        for (ResolveInfo info : packageManager.queryIntentServices(intent, PDU_COLUMN)) {
            if (info.serviceInfo == null) {
                loge("Can't get service information from " + info);
            } else {
                String packageName = info.serviceInfo.packageName;
                if (packageManager.checkPermission(carrierFilterSmsPerm, packageName) == 0) {
                    packages.add(packageName);
                    log("getSystemAppForIntent: added package " + packageName);
                }
            }
        }
        return packages;
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        intent.addFlags(134217728);
        String action = intent.getAction();
        if (Intents.SMS_DELIVER_ACTION.equals(action) || Intents.SMS_RECEIVED_ACTION.equals(action) || Intents.WAP_PUSH_DELIVER_ACTION.equals(action) || Intents.WAP_PUSH_RECEIVED_ACTION.equals(action)) {
            intent.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (user.equals(UserHandle.ALL)) {
            int[] users = null;
            try {
                users = ActivityManagerNative.getDefault().getRunningUserIds();
            } catch (RemoteException e) {
            }
            if (users == null) {
                users = new int[SEQUENCE_COLUMN];
                users[PDU_COLUMN] = user.getIdentifier();
            }
            for (int i = users.length + NO_DESTINATION_PORT; i >= 0; i += NO_DESTINATION_PORT) {
                UserHandle targetUser = new UserHandle(users[i]);
                if (users[i] != 0) {
                    if (!this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                        UserInfo info = this.mUserManager.getUserInfo(users[i]);
                        if (info != null) {
                            if (info.isManagedProfile()) {
                            }
                        }
                    }
                }
                this.mContext.sendOrderedBroadcastAsUser(intent, targetUser, permission, appOp, opts, users[i] == 0 ? resultReceiver : null, getHandler(), NO_DESTINATION_PORT, null, null);
            }
            return;
        }
        this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), NO_DESTINATION_PORT, null, null);
        triggerInboxInsertDoneDetect(intent);
    }

    private void triggerInboxInsertDoneDetect(Intent intent) {
        if (Intents.SMS_DELIVER_ACTION.equals(intent.getAction()) && !this.isClass0) {
            ComponentName componentName = intent.getComponent();
            if (componentName != null) {
                this.defaultSmsApplicationName = componentName.getPackageName();
            }
            this.subIdForReceivedSms = intent.getIntExtra("subscription", SubscriptionManager.getDefaultSmsSubscriptionId());
            this.mAlreadyReceivedSms.getAndIncrement();
            getHandler().removeCallbacks(this.mUpdateCountRunner);
            getHandler().postDelayed(this.mUpdateCountRunner, 60000);
        }
    }

    private void addInboxInsertObserver(Context context) {
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/inbox-insert"), DBG, this.mInsertObserver);
    }

    private void reportSmsReceiveTimeout(int durationMillis) {
        if (!this.isAlreadyDurationTimeout && durationMillis >= SMS_BROADCAST_DURATION_TIMEOUT) {
            this.isAlreadyDurationTimeout = DBG;
            HwRadarUtils.report(this.mContext, HwRadarUtils.ERR_SMS_RECEIVE, "sms receive timeout:" + durationMillis + this.defaultSmsApplicationName, this.subIdForReceivedSms);
        }
    }

    private void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        int rows = this.mResolver.delete(deleteType == SEQUENCE_COLUMN ? sRawUriPermanentDelete : sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            loge("No rows were deleted from raw table!");
        } else {
            log("Deleted " + rows + " rows from raw table.");
        }
    }

    private Bundle handleSmsWhitelisting(ComponentName target) {
        String pkgName;
        String reason;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, PDU_COLUMN, reason);
            BroadcastOptions bopts = BroadcastOptions.makeBasic();
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            return null;
        }
    }

    private void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, BroadcastReceiver resultReceiver) {
        Intent intent = new Intent();
        intent.putExtra("pdus", pdus);
        intent.putExtra(CellBroadcasts.MESSAGE_FORMAT, format);
        if (destPort == NO_DESTINATION_PORT) {
            intent.setAction(Intents.SMS_DELIVER_ACTION);
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, DBG);
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
        } else {
            intent.setAction(Intents.DATA_SMS_RECEIVED_ACTION);
            intent.setData(Uri.parse("sms://localhost:" + destPort));
            intent.setComponent(null);
        }
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }

    static boolean isCurrentFormat3gpp2() {
        return EVENT_BROADCAST_SMS == TelephonyManager.getDefault().getCurrentPhoneType() ? DBG : VDBG;
    }

    private boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & EVENT_BROADCAST_SMS) > 0 ? DBG : VDBG;
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    private Uri writeInboxMessage(Intent intent) {
        SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length < SEQUENCE_COLUMN) {
            loge("Failed to parse SMS pdu");
            return null;
        }
        int i = PDU_COLUMN;
        int length = messages.length;
        while (i < length) {
            try {
                messages[i].getDisplayMessageBody();
                i += SEQUENCE_COLUMN;
            } catch (NullPointerException e) {
                loge("NPE inside SmsMessage");
                return null;
            }
        }
        ContentValues values = parseSmsMessage(messages);
        long identity = Binder.clearCallingIdentity();
        Uri insert;
        try {
            insert = this.mContext.getContentResolver().insert(Inbox.CONTENT_URI, values);
            return insert;
        } catch (Exception e2) {
            insert = "Failed to persist inbox message";
            loge(insert, e2);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static ContentValues parseSmsMessage(SmsMessage[] msgs) {
        int i = PDU_COLUMN;
        SmsMessage sms = msgs[PDU_COLUMN];
        ContentValues values = new ContentValues();
        values.put(TextBasedSmsColumns.ADDRESS, sms.getDisplayOriginatingAddress());
        values.put(TextBasedSmsColumns.BODY, buildMessageBodyFromPdus(msgs));
        values.put(TextBasedSmsColumns.DATE_SENT, Long.valueOf(sms.getTimestampMillis()));
        values.put(ThreadsColumns.DATE, Long.valueOf(System.currentTimeMillis()));
        values.put(TelephonyEventLog.DATA_KEY_PROTOCOL, Integer.valueOf(sms.getProtocolIdentifier()));
        values.put(SmsManager.MESSAGE_STATUS_SEEN, Integer.valueOf(PDU_COLUMN));
        values.put(SmsManager.MESSAGE_STATUS_READ, Integer.valueOf(PDU_COLUMN));
        String subject = sms.getPseudoSubject();
        if (!TextUtils.isEmpty(subject)) {
            values.put(TextBasedSmsColumns.SUBJECT, subject);
        }
        String str = TextBasedSmsColumns.REPLY_PATH_PRESENT;
        if (sms.isReplyPathPresent()) {
            i = SEQUENCE_COLUMN;
        }
        values.put(str, Integer.valueOf(i));
        values.put(TextBasedSmsColumns.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }

    private static String buildMessageBodyFromPdus(SmsMessage[] msgs) {
        int i = PDU_COLUMN;
        if (msgs.length == SEQUENCE_COLUMN) {
            return replaceFormFeeds(msgs[PDU_COLUMN].getDisplayMessageBody());
        }
        StringBuilder body = new StringBuilder();
        int length = msgs.length;
        while (i < length) {
            body.append(msgs[i].getDisplayMessageBody());
            i += SEQUENCE_COLUMN;
        }
        return replaceFormFeeds(body.toString());
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? "" : s.replace('\f', '\n');
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public int getWakeLockTimeout() {
        return WAKELOCK_TIMEOUT;
    }
}

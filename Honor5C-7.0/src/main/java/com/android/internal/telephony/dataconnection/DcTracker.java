package com.android.internal.telephony.dataconnection;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RegistrantList;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.provider.Telephony.GlobalMatchs;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RadioNVItems;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyEventLog;
import com.android.internal.telephony.cdma.SignalToneUtil;
import com.android.internal.telephony.dataconnection.DataConnection.ConnectionParams;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DcTracker extends AbstractDcTrackerBase {
    private static final /* synthetic */ int[] -com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
    protected static final int ACTIVE_PDP_FAIL_TO_RESTART_RILD_COUNT = 3;
    protected static final long ACTIVE_PDP_FAIL_TO_RESTART_RILD_MILLIS = 600000;
    static final String APN_ID = "apn_id";
    private static final String APN_TYPE_KEY = "apnType";
    private static final int CDMA_NOT_ROAMING = 0;
    private static final int CDMA_ROAMING = 1;
    private static final String CT_LTE_APN_PREFIX = null;
    private static final String CT_NOT_ROAMING_APN_PREFIX = null;
    private static final String CT_ROAMING_APN_PREFIX = null;
    public static final boolean CT_SUPL_FEATURE_ENABLE = false;
    public static final boolean CUST_RETRY_CONFIG = false;
    private static final int DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 60000;
    private static int DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 0;
    private static final String DATA_STALL_ALARM_TAG_EXTRA = "data.stall.alram.tag";
    private static final boolean DATA_STALL_NOT_SUSPECTED = false;
    private static final boolean DATA_STALL_SUSPECTED = true;
    private static final boolean DBG = true;
    private static final String DEBUG_PROV_APN_ALARM = "persist.debug.prov_apn_alarm";
    private static final String ERROR_CODE_KEY = "errorCode";
    private static final int GSM_ROAMING_CARD1 = 2;
    private static final int GSM_ROAMING_CARD2 = 3;
    private static final String INTENT_DATA_STALL_ALARM = "com.android.internal.telephony.data-stall";
    protected static final String INTENT_PDP_RESET_ALARM = "com.android.internal.telephony.pdp-reset";
    private static final String INTENT_PROVISIONING_APN_ALARM = "com.android.internal.telephony.provisioning_apn_alarm";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.data-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reconnect_alarm_extra_reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "reconnect_alarm_extra_type";
    private static String LOG_TAG = null;
    private static final int LTE_NOT_ROAMING = 4;
    static final Uri MSIM_TELEPHONY_CARRIERS_URI = null;
    private static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    protected static final int PDP_RESET_ALARM_DELAY_IN_MS = 300000;
    protected static final String PDP_RESET_ALARM_TAG_EXTRA = "pdp.reset.alram.tag";
    private static final int POLL_NETSTAT_MILLIS = 1000;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 600000;
    private static final int POLL_PDP_MILLIS = 5000;
    static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = null;
    private static final String PREFERRED_APN_ID = "preferredApnIdEx";
    private static final int PREF_APN_ID_LEN = 5;
    private static final int PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT = 900000;
    private static final String PROVISIONING_APN_ALARM_TAG_EXTRA = "provisioning.apn.alarm.tag";
    private static final int PROVISIONING_SPINNER_TIMEOUT_MILLIS = 120000;
    private static final String PUPPET_MASTER_RADIO_STRESS_TEST = "gsm.defaultpdpcontext.active";
    private static final boolean RADIO_TESTS = false;
    private static final int RECONNECT_ALARM_DELAY_TIME_FOR_CS_ATTACHED = 5000;
    private static final String REDIRECTION_URL_KEY = "redirectionUrl";
    private static final boolean VDBG = true;
    private static final boolean VDBG_STALL = false;
    public static final boolean VOWIFI_CONFIG = false;
    public static final int VP_END = 0;
    public static final int VP_START = 1;
    protected static final HashMap<String, Integer> mIfacePhoneHashMap = null;
    protected static final boolean mWcdmaVpEnabled = false;
    private static int sEnableFailFastRefCounter;
    private static boolean sPolicyDataEnabled;
    private String RADIO_RESET_PROPERTY;
    private boolean dedupeApn;
    public AtomicBoolean isCleanupRequired;
    protected boolean isMultiSimEnabled;
    private Activity mActivity;
    private final AlarmManager mAlarmManager;
    private ArrayList<ApnSetting> mAllApnSettings;
    private RegistrantList mAllDataDisconnectedRegistrants;
    public final ConcurrentHashMap<String, ApnContext> mApnContexts;
    private final SparseArray<ApnContext> mApnContextsById;
    private ApnChangeObserver mApnObserver;
    private HashMap<String, Integer> mApnToDataConnectionId;
    private AtomicBoolean mAttached;
    private AtomicBoolean mAutoAttachOnCreation;
    private boolean mAutoAttachOnCreationConfig;
    private boolean mCanSetPreferApn;
    private boolean mCdmaPsRecoveryEnabled;
    private final ConnectivityManager mCm;
    private boolean mColdSimDetected;
    private int mCurrentState;
    private HashMap<Integer, DcAsyncChannel> mDataConnectionAcHashMap;
    private final Handler mDataConnectionTracker;
    private HashMap<Integer, DataConnection> mDataConnections;
    private Object mDataEnabledLock;
    private PendingIntent mDataStallAlarmIntent;
    private int mDataStallAlarmTag;
    private volatile boolean mDataStallDetectionEnabled;
    private TxRxSum mDataStallTxRxSum;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcController mDcc;
    private String mDefaultApnId;
    private ArrayList<Message> mDisconnectAllCompleteMsgList;
    private int mDisconnectPendingCount;
    private ApnSetting mEmergencyApn;
    private boolean mEmergencyApnLoaded;
    private volatile boolean mFailFast;
    protected long mFirstPdpActFailTimestamp;
    private final AtomicReference<IccRecords> mIccRecords;
    public boolean mImsRegistrationState;
    private boolean mInVoiceCall;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mInternalDataEnabled;
    protected boolean mIsBtConnected;
    private boolean mIsDisposed;
    private boolean mIsProvisioning;
    private boolean mIsPsRestricted;
    private boolean mIsScreenOn;
    private boolean mIsWifiConnected;
    private boolean mMvnoMatched;
    private boolean mNetStatPollEnabled;
    private int mNetStatPollPeriod;
    private int mNoRecvPollCount;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    protected int mPdpActFailCount;
    protected PendingIntent mPdpResetAlarmIntent;
    protected int mPdpResetAlarmTag;
    protected final Phone mPhone;
    PhoneStateListener mPhoneStateListener;
    private final Runnable mPollNetStat;
    private ApnSetting mPreferredApn;
    protected final ArrayList<ApnContext> mPrioritySortedApnContexts;
    private final String mProvisionActionName;
    private BroadcastReceiver mProvisionBroadcastReceiver;
    private PendingIntent mProvisioningApnAlarmIntent;
    private int mProvisioningApnAlarmTag;
    private ProgressDialog mProvisioningSpinner;
    private String mProvisioningUrl;
    private PendingIntent mReconnectIntent;
    private String mRedirectUrl;
    private AsyncChannel mReplyAc;
    private String mRequestedApnType;
    private boolean mReregisterOnReconnectFailure;
    private ContentResolver mResolver;
    protected boolean mRestartRildEnabled;
    private long mRxPkts;
    private long mSentSinceLastRecv;
    private final SettingsObserver mSettingsObserver;
    private State mState;
    private SubscriptionManager mSubscriptionManager;
    private long mTxPkts;
    protected UiccCardApplication mUiccApplcation;
    private final UiccController mUiccController;
    private AtomicInteger mUniqueIdGenerator;
    protected boolean mUserDataEnabled;
    public int mVpStatus;
    private int oldCallState;
    private int preDataRadioTech;
    private int preSetupBasedRadioTech;
    private HashSet<ApnContext> redirectApnContextSet;

    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(DcTracker.this.mDataConnectionTracker);
        }

        public void onChange(boolean selfChange) {
            DcTracker.this.sendMessage(DcTracker.this.obtainMessage(270355));
        }
    }

    public static class DataAllowFailReason {
        private HashSet<DataAllowFailReasonType> mDataAllowFailReasonSet;

        public DataAllowFailReason() {
            this.mDataAllowFailReasonSet = new HashSet();
        }

        public void addDataAllowFailReason(DataAllowFailReasonType type) {
            this.mDataAllowFailReasonSet.add(type);
        }

        public String getDataAllowFailReason() {
            StringBuilder failureReason = new StringBuilder();
            failureReason.append("isDataAllowed: No");
            for (DataAllowFailReasonType reason : this.mDataAllowFailReasonSet) {
                failureReason.append(reason.mFailReasonStr);
            }
            return failureReason.toString();
        }

        public boolean isFailForSingleReason(DataAllowFailReasonType failReasonType) {
            if (this.mDataAllowFailReasonSet.size() == DcTracker.VP_START) {
                return this.mDataAllowFailReasonSet.contains(failReasonType);
            }
            return DcTracker.VOWIFI_CONFIG;
        }

        public void clearAllReasons() {
            this.mDataAllowFailReasonSet.clear();
        }

        public boolean isFailed() {
            return this.mDataAllowFailReasonSet.size() > 0 ? DcTracker.VDBG : DcTracker.VOWIFI_CONFIG;
        }
    }

    public enum DataAllowFailReasonType {
        ;
        
        public String mFailReasonStr;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReasonType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReasonType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReasonType.<clinit>():void");
        }

        private DataAllowFailReasonType(String reason) {
            this.mFailReasonStr = reason;
        }
    }

    private class ProvisionNotificationBroadcastReceiver extends BroadcastReceiver {
        private final String mNetworkOperator;
        private final String mProvisionUrl;
        final /* synthetic */ DcTracker this$0;

        public ProvisionNotificationBroadcastReceiver(DcTracker this$0, String provisionUrl, String networkOperator) {
            this.this$0 = this$0;
            this.mNetworkOperator = networkOperator;
            this.mProvisionUrl = provisionUrl;
        }

        private void setEnableFailFastMobileData(int enabled) {
            this.this$0.sendMessage(this.this$0.obtainMessage(270372, enabled, DcTracker.VP_END));
        }

        private void enableMobileProvisioning() {
            Message msg = this.this$0.obtainMessage(270373);
            msg.setData(Bundle.forPair("provisioningUrl", this.mProvisionUrl));
            this.this$0.sendMessage(msg);
        }

        public void onReceive(Context context, Intent intent) {
            this.this$0.mProvisioningSpinner = new ProgressDialog(context);
            this.this$0.mProvisioningSpinner.setTitle(this.mNetworkOperator);
            this.this$0.mProvisioningSpinner.setMessage(context.getText(17040618));
            this.this$0.mProvisioningSpinner.setIndeterminate(DcTracker.VDBG);
            this.this$0.mProvisioningSpinner.setCancelable(DcTracker.VDBG);
            this.this$0.mProvisioningSpinner.getWindow().setType(TelephonyEventLog.TAG_IMS_CALL_MERGE);
            this.this$0.mProvisioningSpinner.show();
            this.this$0.sendMessageDelayed(this.this$0.obtainMessage(270378, this.this$0.mProvisioningSpinner), 120000);
            this.this$0.setRadio(DcTracker.VDBG);
            setEnableFailFastMobileData(DcTracker.VP_START);
            enableMobileProvisioning();
        }
    }

    private static class RecoveryAction {
        public static final int CLEANUP = 1;
        public static final int DATA_SWITCH_CONNECTION_STALL = 5;
        public static final int GET_DATA_CALL_LIST = 0;
        public static final int RADIO_RESTART = 3;
        public static final int RADIO_RESTART_WITH_PROP = 4;
        public static final int REREGISTER = 2;

        private RecoveryAction() {
        }

        private static boolean isAggressiveRecovery(int value) {
            if (value == CLEANUP || value == REREGISTER || value == RADIO_RESTART || value == DATA_SWITCH_CONNECTION_STALL || value == RADIO_RESTART_WITH_PROP) {
                return DcTracker.VDBG;
            }
            return DcTracker.VOWIFI_CONFIG;
        }
    }

    private enum RetryFailures {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DcTracker.RetryFailures.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DcTracker.RetryFailures.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.RetryFailures.<clinit>():void");
        }
    }

    private class SettingsObserver extends ContentObserver {
        private static final String TAG = "DcTracker.SettingsObserver";
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap;
        final /* synthetic */ DcTracker this$0;

        SettingsObserver(DcTracker this$0, Context context, Handler handler) {
            this.this$0 = this$0;
            super(null);
            this.mUriEventMap = new HashMap();
            this.mContext = context;
            this.mHandler = handler;
        }

        void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, DcTracker.VOWIFI_CONFIG, this);
        }

        void unobserve() {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            Rlog.e(TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = (Integer) this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
            } else {
                Rlog.e(TAG, "No matching event to send for URI=" + uri);
            }
            this.this$0.sendRoamingDataStatusChangBroadcast();
        }
    }

    public static class TxRxSum {
        public long rxPkts;
        public long txPkts;

        public TxRxSum() {
            reset();
        }

        public TxRxSum(long txPkts, long rxPkts) {
            this.txPkts = txPkts;
            this.rxPkts = rxPkts;
        }

        public TxRxSum(TxRxSum sum) {
            this.txPkts = sum.txPkts;
            this.rxPkts = sum.rxPkts;
        }

        public void reset() {
            this.txPkts = -1;
            this.rxPkts = -1;
        }

        public String toString() {
            return "{txSum=" + this.txPkts + " rxSum=" + this.rxPkts + "}";
        }

        public void updateTxRxSum() {
            this.txPkts = TrafficStats.getMobileTcpTxPackets();
            this.rxPkts = TrafficStats.getMobileTcpRxPackets();
        }

        public void updateThisModemMobileTxRxSum(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
            this.txPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileTxPackets(ifacePhoneHashMap, phoneId);
            this.rxPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileRxPackets(ifacePhoneHashMap, phoneId);
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-DctConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-DctConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-DctConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = VP_START;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = GSM_ROAMING_CARD1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = GSM_ROAMING_CARD2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.FAILED.ordinal()] = LTE_NOT_ROAMING;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.IDLE.ordinal()] = PREF_APN_ID_LEN;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.RETRYING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.SCANNING.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-internal-telephony-DctConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.DcTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.DcTracker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.DcTracker.<clinit>():void");
    }

    private void registerSettingsObserver() {
        Uri contentUri;
        this.mSettingsObserver.unobserve();
        if (TelephonyManager.getDefault().getSimCount() == VP_START) {
            contentUri = Global.getUriFor("data_roaming");
        } else {
            contentUri = Global.getUriFor(getDataRoamingSettingItem("data_roaming"));
        }
        this.mSettingsObserver.observe(contentUri, 270347);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioned"), 270379);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioning_mobile_data"), 270379);
    }

    private void onActionIntentReconnectAlarm(Intent intent) {
        String reason = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON);
        String apnType = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = intent.getIntExtra("subscription", -1);
        log("onActionIntentReconnectAlarm: currSubId = " + currSubId + " phoneSubId=" + phoneSubId);
        if (SubscriptionManager.isValidSubscriptionId(currSubId) && currSubId == phoneSubId) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            log("onActionIntentReconnectAlarm: mState=" + this.mState + " reason=" + reason + " apnType=" + apnType + " apnContext=" + apnContext + " mDataConnectionAsyncChannels=" + this.mDataConnectionAcHashMap);
            if (apnContext != null && apnContext.isEnabled()) {
                apnContext.setReason(reason);
                State apnContextState = apnContext.getState();
                log("onActionIntentReconnectAlarm: apnContext state=" + apnContextState);
                if (apnContextState == State.FAILED || apnContextState == State.IDLE) {
                    log("onActionIntentReconnectAlarm: state is FAILED|IDLE, disassociate");
                    DcAsyncChannel dcac = apnContext.getDcAc();
                    if (dcac != null) {
                        log("onActionIntentReconnectAlarm: tearDown apnContext=" + apnContext);
                        dcac.tearDown(apnContext, "", null);
                    }
                    apnContext.setDataConnectionAc(null);
                    apnContext.setState(State.IDLE);
                } else {
                    log("onActionIntentReconnectAlarm: keep associated");
                }
                sendMessage(obtainMessage(270339, apnContext));
                apnContext.setReconnectIntent(null);
            }
            return;
        }
        log("receive ReconnectAlarm but subId incorrect, ignore");
    }

    private void onActionIntentDataStallAlarm(Intent intent) {
        Message msg = obtainMessage(270353, intent.getAction());
        msg.arg1 = intent.getIntExtra(DATA_STALL_ALARM_TAG_EXTRA, VP_END);
        sendMessage(msg);
    }

    public DcTracker(Phone phone) {
        this.isCleanupRequired = new AtomicBoolean(VOWIFI_CONFIG);
        this.oldCallState = VP_END;
        this.mDataEnabledLock = new Object();
        this.mInternalDataEnabled = VDBG;
        this.mUserDataEnabled = VDBG;
        this.mRequestedApnType = "default";
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mPrioritySortedApnContexts = new ArrayList();
        this.mAllApnSettings = new ArrayList();
        this.mPreferredApn = null;
        this.mIsPsRestricted = VOWIFI_CONFIG;
        this.mEmergencyApn = null;
        this.mIsDisposed = VOWIFI_CONFIG;
        this.mIsProvisioning = VOWIFI_CONFIG;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean enabled = DcTracker.VDBG;
                boolean z = DcTracker.VOWIFI_CONFIG;
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    DcTracker.this.log("screen on");
                    DcTracker.this.mIsScreenOn = DcTracker.VDBG;
                    DcTracker.this.stopNetStatPoll();
                    DcTracker.this.startNetStatPoll();
                    DcTracker.this.restartDataStallAlarm();
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    DcTracker.this.log("screen off");
                    DcTracker.this.mIsScreenOn = DcTracker.VOWIFI_CONFIG;
                    DcTracker.this.stopNetStatPoll();
                    DcTracker.this.startNetStatPoll();
                    DcTracker.this.restartDataStallAlarm();
                } else if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                    DcTracker.this.log("Reconnect alarm. Previous state was " + DcTracker.this.mState);
                    DcTracker.this.onActionIntentReconnectAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_DATA_STALL_ALARM)) {
                    DcTracker.this.log("Data stall alarm");
                    DcTracker.this.onActionIntentDataStallAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_PROVISIONING_APN_ALARM)) {
                    DcTracker.this.log("Provisioning apn alarm");
                    DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    DcTracker dcTracker = DcTracker.this;
                    if (networkInfo != null) {
                        z = networkInfo.isConnected();
                    }
                    dcTracker.mIsWifiConnected = z;
                    DcTracker.this.log("NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + DcTracker.this.mIsWifiConnected);
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    DcTracker.this.log("Wifi state changed");
                    if (intent.getIntExtra("wifi_state", DcTracker.LTE_NOT_ROAMING) != DcTracker.GSM_ROAMING_CARD2) {
                        enabled = DcTracker.VOWIFI_CONFIG;
                    }
                    if (!enabled) {
                        DcTracker.this.mIsWifiConnected = DcTracker.VOWIFI_CONFIG;
                    }
                    DcTracker.this.log("WIFI_STATE_CHANGED_ACTION: enabled=" + enabled + " mIsWifiConnected=" + DcTracker.this.mIsWifiConnected);
                } else if (action.equals(DcTracker.INTENT_PDP_RESET_ALARM)) {
                    DcTracker.this.log("Pdp reset alarm");
                    DcTracker.this.onActionIntentPdpResetAlarm(intent);
                } else if (action.equals("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED")) {
                    DcTracker.this.mIsBtConnected = intent.getBooleanExtra("btt_connect_state", DcTracker.VOWIFI_CONFIG);
                    DcTracker.this.log("Received bt_connect_state = " + DcTracker.this.mIsBtConnected);
                } else {
                    DcTracker.this.log("onReceive: Unknown action=" + action);
                }
                DcTracker.this.disposeAddedIntent(action);
            }
        };
        this.mPollNetStat = new Runnable() {
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_poll_interval_ms", DcTracker.POLL_NETSTAT_MILLIS);
                } else {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_long_poll_interval_ms", DcTracker.POLL_NETSTAT_SCREEN_OFF_MILLIS);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
                if (SubscriptionManager.isValidSubscriptionId(DcTracker.this.mPhone.getSubId())) {
                    DcTracker.this.registerSettingsObserver();
                }
            }
        };
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = VOWIFI_CONFIG;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = VP_END;
        this.mDataStallDetectionEnabled = VDBG;
        this.mFailFast = VOWIFI_CONFIG;
        this.mInVoiceCall = VOWIFI_CONFIG;
        this.mIsWifiConnected = VOWIFI_CONFIG;
        this.mIsBtConnected = VOWIFI_CONFIG;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = VDBG;
        this.mAutoAttachOnCreation = new AtomicBoolean(VOWIFI_CONFIG);
        this.mIsScreenOn = VDBG;
        this.mMvnoMatched = VOWIFI_CONFIG;
        this.mUniqueIdGenerator = new AtomicInteger(VP_END);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = VP_END;
        this.mRedirectUrl = null;
        this.mColdSimDetected = VOWIFI_CONFIG;
        this.redirectApnContextSet = new HashSet();
        this.mReregisterOnReconnectFailure = VOWIFI_CONFIG;
        this.mCanSetPreferApn = VOWIFI_CONFIG;
        this.mAttached = new AtomicBoolean(VOWIFI_CONFIG);
        this.mImsRegistrationState = VOWIFI_CONFIG;
        this.mCdmaPsRecoveryEnabled = VOWIFI_CONFIG;
        this.dedupeApn = VOWIFI_CONFIG;
        this.mDefaultApnId = "0,0,0,0,0";
        this.mCurrentState = -1;
        this.preDataRadioTech = -1;
        this.preSetupBasedRadioTech = -1;
        this.mVpStatus = VP_END;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mPdpActFailCount = VP_END;
        this.mFirstPdpActFailTimestamp = 0;
        this.mRestartRildEnabled = VDBG;
        this.mPdpResetAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mPdpResetAlarmIntent = null;
        this.mUiccApplcation = null;
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (DcTracker.this.oldCallState == DcTracker.GSM_ROAMING_CARD1 && state == 0 && DcTracker.this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
                    DcTracker.this.onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                }
                DcTracker.this.oldCallState = state;
            }
        };
        this.mEmergencyApnLoaded = VOWIFI_CONFIG;
        this.mPhone = phone;
        if (phone.getPhoneType() == VP_START) {
            LOG_TAG = "GsmDCT";
        } else if (phone.getPhoneType() == GSM_ROAMING_CARD1) {
            LOG_TAG = "CdmaDCT";
        } else {
            LOG_TAG = "DCT";
            loge("unexpected phone type [" + phone.getPhoneType() + "]");
        }
        log(LOG_TAG + ".constructor");
        if (phone.getPhoneType() == GSM_ROAMING_CARD1 && SystemProperties.getBoolean("hw.dct.psrecovery", VOWIFI_CONFIG)) {
            this.mCdmaPsRecoveryEnabled = VDBG;
        } else {
            this.mCdmaPsRecoveryEnabled = VOWIFI_CONFIG;
        }
        this.mResolver = this.mPhone.getContext().getContentResolver();
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 270369, null);
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            VSimUtilsInner.registerForIccChanged(this, 270369, null);
        }
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mCm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (this.mCm != null) {
            NetworkInfo mWifiNetworkInfo = this.mCm.getNetworkInfo(VP_START);
            if (mWifiNetworkInfo != null && mWifiNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                this.mIsWifiConnected = VDBG;
            }
        }
        log("In DcTracker constructor mIsWifiConnected is" + this.mIsWifiConnected);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction(INTENT_DATA_STALL_ALARM);
        filter.addAction(INTENT_PROVISIONING_APN_ALARM);
        filter.addAction(INTENT_PDP_RESET_ALARM);
        filter.addAction("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED");
        addIntentFilter(filter);
        this.mUserDataEnabled = getDataEnabled();
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mAutoAttachOnCreation.set(PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getBoolean(Phone.DATA_DISABLED_ON_BOOT_KEY, VOWIFI_CONFIG));
        registerPhoneStateListener(this.mPhone.getContext());
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        HandlerThread dcHandlerThread = new HandlerThread("DcHandlerThread");
        dcHandlerThread.start();
        Handler dcHandler = new Handler(dcHandlerThread.getLooper());
        this.mDcc = DcController.makeDcc(this.mPhone, this, dcHandler);
        this.mDcTesterFailBringUpAll = new DcTesterFailBringUpAll(this.mPhone, dcHandler);
        this.mDataConnectionTracker = this;
        registerForAllEvents();
        update();
        this.mApnObserver = new ApnChangeObserver();
        phone.getContext().getContentResolver().registerContentObserver(Carriers.CONTENT_URI, VDBG, this.mApnObserver);
        initApnContexts();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            filter = new IntentFilter();
            filter.addAction("com.android.internal.telephony.data-reconnect." + apnContext.getApnType());
            this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        }
        this.mProvisionActionName = "com.android.internal.telephony.PROVISION" + phone.getPhoneId();
        this.mSettingsObserver = new SettingsObserver(this, this.mPhone.getContext(), this);
        registerSettingsObserver();
        super.init();
        if (isClearCodeEnabled()) {
            startListenCellLocationChange();
        }
        registerForFdn();
        sendMessage(obtainMessage(271137));
    }

    public DcTracker() {
        this.isCleanupRequired = new AtomicBoolean(VOWIFI_CONFIG);
        this.oldCallState = VP_END;
        this.mDataEnabledLock = new Object();
        this.mInternalDataEnabled = VDBG;
        this.mUserDataEnabled = VDBG;
        this.mRequestedApnType = "default";
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mPrioritySortedApnContexts = new ArrayList();
        this.mAllApnSettings = new ArrayList();
        this.mPreferredApn = null;
        this.mIsPsRestricted = VOWIFI_CONFIG;
        this.mEmergencyApn = null;
        this.mIsDisposed = VOWIFI_CONFIG;
        this.mIsProvisioning = VOWIFI_CONFIG;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean enabled = DcTracker.VDBG;
                boolean z = DcTracker.VOWIFI_CONFIG;
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    DcTracker.this.log("screen on");
                    DcTracker.this.mIsScreenOn = DcTracker.VDBG;
                    DcTracker.this.stopNetStatPoll();
                    DcTracker.this.startNetStatPoll();
                    DcTracker.this.restartDataStallAlarm();
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    DcTracker.this.log("screen off");
                    DcTracker.this.mIsScreenOn = DcTracker.VOWIFI_CONFIG;
                    DcTracker.this.stopNetStatPoll();
                    DcTracker.this.startNetStatPoll();
                    DcTracker.this.restartDataStallAlarm();
                } else if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                    DcTracker.this.log("Reconnect alarm. Previous state was " + DcTracker.this.mState);
                    DcTracker.this.onActionIntentReconnectAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_DATA_STALL_ALARM)) {
                    DcTracker.this.log("Data stall alarm");
                    DcTracker.this.onActionIntentDataStallAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_PROVISIONING_APN_ALARM)) {
                    DcTracker.this.log("Provisioning apn alarm");
                    DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    DcTracker dcTracker = DcTracker.this;
                    if (networkInfo != null) {
                        z = networkInfo.isConnected();
                    }
                    dcTracker.mIsWifiConnected = z;
                    DcTracker.this.log("NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + DcTracker.this.mIsWifiConnected);
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    DcTracker.this.log("Wifi state changed");
                    if (intent.getIntExtra("wifi_state", DcTracker.LTE_NOT_ROAMING) != DcTracker.GSM_ROAMING_CARD2) {
                        enabled = DcTracker.VOWIFI_CONFIG;
                    }
                    if (!enabled) {
                        DcTracker.this.mIsWifiConnected = DcTracker.VOWIFI_CONFIG;
                    }
                    DcTracker.this.log("WIFI_STATE_CHANGED_ACTION: enabled=" + enabled + " mIsWifiConnected=" + DcTracker.this.mIsWifiConnected);
                } else if (action.equals(DcTracker.INTENT_PDP_RESET_ALARM)) {
                    DcTracker.this.log("Pdp reset alarm");
                    DcTracker.this.onActionIntentPdpResetAlarm(intent);
                } else if (action.equals("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED")) {
                    DcTracker.this.mIsBtConnected = intent.getBooleanExtra("btt_connect_state", DcTracker.VOWIFI_CONFIG);
                    DcTracker.this.log("Received bt_connect_state = " + DcTracker.this.mIsBtConnected);
                } else {
                    DcTracker.this.log("onReceive: Unknown action=" + action);
                }
                DcTracker.this.disposeAddedIntent(action);
            }
        };
        this.mPollNetStat = new Runnable() {
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_poll_interval_ms", DcTracker.POLL_NETSTAT_MILLIS);
                } else {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_long_poll_interval_ms", DcTracker.POLL_NETSTAT_SCREEN_OFF_MILLIS);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
                if (SubscriptionManager.isValidSubscriptionId(DcTracker.this.mPhone.getSubId())) {
                    DcTracker.this.registerSettingsObserver();
                }
            }
        };
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = VOWIFI_CONFIG;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = VP_END;
        this.mDataStallDetectionEnabled = VDBG;
        this.mFailFast = VOWIFI_CONFIG;
        this.mInVoiceCall = VOWIFI_CONFIG;
        this.mIsWifiConnected = VOWIFI_CONFIG;
        this.mIsBtConnected = VOWIFI_CONFIG;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = VDBG;
        this.mAutoAttachOnCreation = new AtomicBoolean(VOWIFI_CONFIG);
        this.mIsScreenOn = VDBG;
        this.mMvnoMatched = VOWIFI_CONFIG;
        this.mUniqueIdGenerator = new AtomicInteger(VP_END);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = VP_END;
        this.mRedirectUrl = null;
        this.mColdSimDetected = VOWIFI_CONFIG;
        this.redirectApnContextSet = new HashSet();
        this.mReregisterOnReconnectFailure = VOWIFI_CONFIG;
        this.mCanSetPreferApn = VOWIFI_CONFIG;
        this.mAttached = new AtomicBoolean(VOWIFI_CONFIG);
        this.mImsRegistrationState = VOWIFI_CONFIG;
        this.mCdmaPsRecoveryEnabled = VOWIFI_CONFIG;
        this.dedupeApn = VOWIFI_CONFIG;
        this.mDefaultApnId = "0,0,0,0,0";
        this.mCurrentState = -1;
        this.preDataRadioTech = -1;
        this.preSetupBasedRadioTech = -1;
        this.mVpStatus = VP_END;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mPdpActFailCount = VP_END;
        this.mFirstPdpActFailTimestamp = 0;
        this.mRestartRildEnabled = VDBG;
        this.mPdpResetAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mPdpResetAlarmIntent = null;
        this.mUiccApplcation = null;
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (DcTracker.this.oldCallState == DcTracker.GSM_ROAMING_CARD1 && state == 0 && DcTracker.this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId()) {
                    DcTracker.this.onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                }
                DcTracker.this.oldCallState = state;
            }
        };
        this.mEmergencyApnLoaded = VOWIFI_CONFIG;
        this.mAlarmManager = null;
        this.mCm = null;
        this.mPhone = null;
        this.mUiccController = null;
        this.mDataConnectionTracker = null;
        this.mProvisionActionName = null;
        this.mSettingsObserver = new SettingsObserver(this, null, this);
    }

    public void registerServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this, 270352, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this, 270345, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOn(this, 270347, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 270348, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedEnabled(this, 270358, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedDisabled(this, 270359, null);
        log("registerForDataRegStateOrRatChanged");
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 270377, null);
        if (mWcdmaVpEnabled) {
            this.mPhone.mCi.registerForReportVpStatus(this, 271140, null);
        }
    }

    public void unregisterServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedEnabled(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedDisabled(this);
        log("unregisterForDataRegStateOrRatChanged");
        this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this);
    }

    private void registerForAllEvents() {
        this.mPhone.mCi.registerForAvailable(this, 270337, null);
        this.mPhone.mCi.registerForOffOrNotAvailable(this, 270342, null);
        this.mPhone.mCi.registerForDataNetworkStateChanged(this, 270340, null);
        if (this.mPhone.getCallTracker() != null) {
            this.mPhone.getCallTracker().registerForVoiceCallEnded(this, 270344, null);
            this.mPhone.getCallTracker().registerForVoiceCallStarted(this, 270343, null);
        }
        registerServiceStateTrackerEvents();
    }

    public void dispose() {
        log("DCT.dispose");
        if (this.mProvisionBroadcastReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
            this.mProvisionBroadcastReceiver = null;
        }
        if (this.mProvisioningSpinner != null) {
            this.mProvisioningSpinner.dismiss();
            this.mProvisioningSpinner = null;
        }
        cleanUpAllConnections((boolean) VDBG, null);
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            dcac.disconnect();
        }
        this.mDataConnectionAcHashMap.clear();
        this.mIsDisposed = VDBG;
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            VSimUtilsInner.unregisterForIccChanged(this);
        }
        this.mUiccController.unregisterForIccChanged(this);
        this.mSettingsObserver.unobserve();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mDcc.dispose();
        this.mDcTesterFailBringUpAll.dispose();
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mApnObserver);
        this.mApnContexts.clear();
        this.mApnContextsById.clear();
        this.mPrioritySortedApnContexts.clear();
        unregisterForAllEvents();
        if (isClearCodeEnabled()) {
            stopListenCellLocationChange();
        }
        unregisterForFdn();
        destroyDataConnections();
        super.dispose();
    }

    private void unregisterForAllEvents() {
        this.mPhone.mCi.unregisterForAvailable(this);
        this.mPhone.mCi.unregisterForOffOrNotAvailable(this);
        if (this.mUiccApplcation != null) {
            unregisterForGetAdDone(this.mUiccApplcation);
            this.mUiccApplcation = null;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            unregisterForRecordsLoaded(r);
            unregisterForImsiReady(r);
            unregisterForFdnRecordsLoaded(r);
            this.mIccRecords.set(null);
        }
        this.mPhone.mCi.unregisterForDataNetworkStateChanged(this);
        if (this.mPhone.getCallTracker() != null) {
            this.mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
            this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        }
        unregisterServiceStateTrackerEvents();
        if (mWcdmaVpEnabled) {
            this.mPhone.mCi.unregisterForReportVpStatus(this);
        }
    }

    private void onResetDone(AsyncResult ar) {
        log("EVENT_RESET_DONE");
        String str = null;
        if (ar.userObj instanceof String) {
            str = ar.userObj;
        }
        gotoIdleAndNotifyDataConnection(str);
    }

    protected void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
    }

    private String getAppName(int pid) {
        String processName = "";
        List<RunningAppProcessInfo> l = ((ActivityManager) this.mPhone.getContext().getSystemService("activity")).getRunningAppProcesses();
        if (l == null) {
            return processName;
        }
        for (RunningAppProcessInfo info : l) {
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                }
            } catch (RuntimeException e) {
                log("RuntimeException");
            } catch (Exception e2) {
                log("Get The appName is wrong");
            }
        }
        return processName;
    }

    public void setDataEnabled(boolean enable) {
        log("DcTrackerBase setDataEnabled=" + enable);
        int pid = Binder.getCallingPid();
        log("Get the caller pid and appName. pid is " + pid + ", " + "appName is " + getAppName(pid));
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (int i = VP_END; i < stackArray.length; i += VP_START) {
            sb.append(stackArray[i].toString() + "\n");
        }
        log(sb.toString());
        Message msg = obtainMessage(270366);
        msg.arg1 = enable ? VP_START : VP_END;
        log("setDataEnabled: sendMessage: enable=" + enable);
        sendMessage(msg);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onSetUserDataEnabled(boolean enabled) {
        int i = VP_START;
        synchronized (this.mDataEnabledLock) {
            if (this.mUserDataEnabled != enabled) {
                this.mUserDataEnabled = enabled;
                ContentResolver contentResolver;
                String str;
                if (TelephonyManager.getDefault().getSimCount() == VP_START) {
                    contentResolver = this.mResolver;
                    str = "mobile_data";
                    if (!enabled) {
                        i = VP_END;
                    }
                    Global.putInt(contentResolver, str, i);
                } else if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
                    log("vsim does not save mobile data");
                } else {
                    int i2 = VP_END;
                    while (true) {
                        if (i2 >= TelephonyManager.getDefault().getPhoneCount()) {
                            break;
                        }
                        int i3;
                        ContentResolver contentResolver2 = this.mResolver;
                        String str2 = "mobile_data" + i2;
                        if (enabled) {
                            i3 = VP_START;
                        } else {
                            i3 = VP_END;
                        }
                        Global.putInt(contentResolver2, str2, i3);
                        i2 += VP_START;
                    }
                    contentResolver = this.mResolver;
                    str = "mobile_data";
                    if (!enabled) {
                        i = VP_END;
                    }
                    Global.putInt(contentResolver, str, i);
                }
                if (!getDataOnRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
                    if (enabled) {
                        notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
                    } else {
                        notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_DISABLED);
                    }
                }
                if (enabled) {
                    ApnContext apnContext = (ApnContext) this.mApnContexts.get("default");
                    if (!(apnContext == null || apnContext.isEnabled() || this.mIsWifiConnected || this.mIsBtConnected)) {
                        log("onSetUserDataEnabled default apn is disabled and wifi and bluetooth is disconnect, so we need try to restore apncontext");
                        apnContext.setEnabled(VDBG);
                        apnContext.setDependencyMet(VDBG);
                    }
                }
                if (enabled) {
                    onTrySetupData(AbstractPhoneInternalInterface.REASON_USER_DATA_ENABLED);
                } else {
                    onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                    clearRestartRildParam();
                }
            }
        }
    }

    private void onDeviceProvisionedChange() {
        if (getDataEnabled()) {
            this.mUserDataEnabled = VDBG;
            onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            return;
        }
        this.mUserDataEnabled = VOWIFI_CONFIG;
        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
    }

    public long getSubId() {
        return (long) this.mPhone.getSubId();
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    private void setActivity(Activity activity) {
        log("setActivity = " + activity);
        this.mActivity = activity;
        this.mPhone.notifyDataActivity();
    }

    public boolean isDisconnectedOrConnecting() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() != State.CONNECTED) {
                if (apnContext.getState() == State.DISCONNECTING) {
                }
            }
            return VOWIFI_CONFIG;
        }
        return VDBG;
    }

    public void requestNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.requestNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.incRefCount(log);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.releaseNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.decRefCount(log);
        }
    }

    public boolean isApnSupported(String name) {
        if (name == null) {
            loge("isApnSupported: name=null");
            return VOWIFI_CONFIG;
        } else if (((ApnContext) this.mApnContexts.get(name)) != null) {
            return VDBG;
        } else {
            loge("Request for unsupported mobile name: " + name);
            return VOWIFI_CONFIG;
        }
    }

    private boolean isColdSimDetected() {
        int subId = this.mPhone.getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
            if (subInfo != null && subInfo.getSimProvisioningStatus() == VP_START) {
                log("Cold Sim Detected on SubId: " + subId);
                return VDBG;
            }
        }
        return VOWIFI_CONFIG;
    }

    private boolean isOutOfCreditSimDetected() {
        int subId = this.mPhone.getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SubscriptionInfo subInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(subId);
            if (subInfo != null && subInfo.getSimProvisioningStatus() == GSM_ROAMING_CARD1) {
                log("Out Of Credit Sim Detected on SubId: " + subId);
                return VDBG;
            }
        }
        return VOWIFI_CONFIG;
    }

    public int getApnPriority(String name) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(name);
        if (apnContext == null) {
            loge("Request for unsupported mobile name: " + name);
        }
        return apnContext.priority;
    }

    private void setRadio(boolean on) {
        try {
            Stub.asInterface(ServiceManager.checkService("phone")).setRadio(on);
        } catch (Exception e) {
        }
    }

    public boolean isDataPossible(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext == null) {
            return VOWIFI_CONFIG;
        }
        boolean dataAllowed;
        boolean apnTypePossible = (apnContext.isEnabled() && apnContext.getState() == State.FAILED) ? VOWIFI_CONFIG : VDBG;
        if (apnContext.getApnType().equals("emergency") || isDataAllowedByApnType(null, apnType)) {
            dataAllowed = VDBG;
        } else {
            dataAllowed = isBipApnType(apnType);
        }
        boolean z = dataAllowed ? apnTypePossible : VOWIFI_CONFIG;
        if ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            log("Default data call activation not possible in iwlan.");
            z = VOWIFI_CONFIG;
        }
        log(String.format("isDataPossible(%s): possible=%b isDataAllowed=%b apnTypePossible=%b apnContextisEnabled=%b apnContextState()=%s", new Object[]{apnType, Boolean.valueOf(z), Boolean.valueOf(dataAllowed), Boolean.valueOf(apnTypePossible), Boolean.valueOf(apnContextIsEnabled), apnContextState}));
        return z;
    }

    protected void finalize() {
        log("finalize");
    }

    private ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new ApnContext(this.mPhone, type, LOG_TAG, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        this.mApnContextsById.put(ApnContext.apnIdForApnName(type), apnContext);
        this.mPrioritySortedApnContexts.add(VP_END, apnContext);
        return apnContext;
    }

    private void initApnContexts() {
        log("initApnContexts: E");
        String[] networkConfigStrings = this.mPhone.getContext().getResources().getStringArray(17235983);
        int length = networkConfigStrings.length;
        for (int i = VP_END; i < length; i += VP_START) {
            NetworkConfig networkConfig = new NetworkConfig(networkConfigStrings[i]);
            if (!VSimUtilsInner.isVSimFiltrateApn(this.mPhone.getSubId(), networkConfig.type)) {
                String apnType = networkTypeToApnType(networkConfig.type);
                if (isApnTypeDisabled(apnType)) {
                    log("apn type " + apnType + " disabled!");
                } else {
                    ApnContext apnContext;
                    switch (networkConfig.type) {
                        case VP_END /*0*/:
                            apnContext = addApnContext("default", networkConfig);
                            break;
                        case GSM_ROAMING_CARD1 /*2*/:
                            apnContext = addApnContext("mms", networkConfig);
                            break;
                        case GSM_ROAMING_CARD2 /*3*/:
                            apnContext = addApnContext("supl", networkConfig);
                            break;
                        case LTE_NOT_ROAMING /*4*/:
                            apnContext = addApnContext("dun", networkConfig);
                            break;
                        case PREF_APN_ID_LEN /*5*/:
                            apnContext = addApnContext("hipri", networkConfig);
                            break;
                        case NUMBER_SENT_PACKETS_OF_HANG /*10*/:
                            apnContext = addApnContext("fota", networkConfig);
                            break;
                        case CharacterSets.ISO_8859_8 /*11*/:
                            apnContext = addApnContext("ims", networkConfig);
                            break;
                        case CharacterSets.ISO_8859_9 /*12*/:
                            apnContext = addApnContext("cbs", networkConfig);
                            break;
                        case SmsHeader.ELT_ID_LARGE_ANIMATION /*14*/:
                            apnContext = addApnContext("ia", networkConfig);
                            break;
                        case SignalToneUtil.IS95_CONST_IR_SIG_ISDN_OFF /*15*/:
                            apnContext = addApnContext("emergency", networkConfig);
                            break;
                        case RadioNVItems.RIL_NV_MIP_PROFILE_HA_SPI /*38*/:
                            apnContext = addApnContext("bip0", networkConfig);
                            break;
                        case RadioNVItems.RIL_NV_MIP_PROFILE_AAA_SPI /*39*/:
                            apnContext = addApnContext("bip1", networkConfig);
                            break;
                        case RadioNVItems.RIL_NV_MIP_PROFILE_MN_HA_SS /*40*/:
                            apnContext = addApnContext("bip2", networkConfig);
                            break;
                        case CallFailCause.TEMPORARY_FAILURE /*41*/:
                            apnContext = addApnContext("bip3", networkConfig);
                            break;
                        case CallFailCause.SWITCHING_CONGESTION /*42*/:
                            apnContext = addApnContext("bip4", networkConfig);
                            break;
                        case com.android.internal.telephony.CallFailCause.ACCESS_INFORMATION_DISCARDED /*43*/:
                            apnContext = addApnContext("bip5", networkConfig);
                            break;
                        case CallFailCause.CHANNEL_NOT_AVAIL /*44*/:
                            apnContext = addApnContext("bip6", networkConfig);
                            break;
                        case 45:
                            apnContext = addApnContext("xcap", networkConfig);
                            break;
                        default:
                            log("initApnContexts: skipping unknown type=" + networkConfig.type);
                            continue;
                    }
                    log("initApnContexts: apnContext=" + apnContext);
                }
            }
        }
        log("initApnContexts: X mApnContexts=" + this.mApnContexts);
        Collections.sort(this.mPrioritySortedApnContexts, new Comparator<ApnContext>() {
            public int compare(ApnContext c1, ApnContext c2) {
                return c2.priority - c1.priority;
            }
        });
    }

    public LinkProperties getLinkProperties(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dcac = apnContext.getDcAc();
            if (dcac != null) {
                log("return link properites for " + apnType);
                return dcac.getLinkPropertiesSync();
            }
        }
        log("return new LinkProperties");
        return new LinkProperties();
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dataConnectionAc = apnContext.getDcAc();
            if (dataConnectionAc != null) {
                log("get active pdp is not null, return NetworkCapabilities for " + apnType);
                return dataConnectionAc.getNetworkCapabilitiesSync();
            }
        }
        log("return new NetworkCapabilities");
        return new NetworkCapabilities();
    }

    public String[] getActiveApnTypes() {
        log("get all active apn types");
        ArrayList<String> result = new ArrayList();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }
        return (String[]) result.toArray(new String[VP_END]);
    }

    public String getActiveApnString(String apnType) {
        log("get active apn string for type:" + apnType);
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null) {
                return apnSetting.apn;
            }
        }
        return null;
    }

    public State getState(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getState();
        }
        return State.FAILED;
    }

    private boolean isProvisioningApn(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.isProvisioningApn();
        }
        return VOWIFI_CONFIG;
    }

    public State getOverallState() {
        boolean isConnecting = VOWIFI_CONFIG;
        boolean isFailed = VDBG;
        boolean isAnyEnabled = VOWIFI_CONFIG;
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.isEnabled()) {
                isAnyEnabled = VDBG;
                switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[apnContext.getState().ordinal()]) {
                    case VP_START /*1*/:
                    case GSM_ROAMING_CARD2 /*3*/:
                        log("overall state is CONNECTED");
                        return State.CONNECTED;
                    case GSM_ROAMING_CARD1 /*2*/:
                    case CharacterSets.ISO_8859_3 /*6*/:
                        isConnecting = VDBG;
                        isFailed = VOWIFI_CONFIG;
                        break;
                    case PREF_APN_ID_LEN /*5*/:
                    case CharacterSets.ISO_8859_4 /*7*/:
                        isFailed = VOWIFI_CONFIG;
                        break;
                    default:
                        isAnyEnabled = VDBG;
                        break;
                }
            }
        }
        if (!isAnyEnabled) {
            log("overall state is IDLE");
            return State.IDLE;
        } else if (isConnecting) {
            log("overall state is CONNECTING");
            return State.CONNECTING;
        } else if (isFailed) {
            log("overall state is FAILED");
            return State.FAILED;
        } else {
            log("overall state is IDLE");
            return State.IDLE;
        }
    }

    public boolean getAnyDataEnabled() {
        if (!isDataEnabled(VDBG)) {
            return VOWIFI_CONFIG;
        }
        DataAllowFailReason failureReason = new DataAllowFailReason();
        if (isDataAllowed(failureReason)) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (isDataAllowedForApn(apnContext)) {
                    return VDBG;
                }
            }
            return VOWIFI_CONFIG;
        }
        log(failureReason.getDataAllowFailReason());
        return VOWIFI_CONFIG;
    }

    public boolean getAnyDataEnabled(boolean checkUserDataEnabled) {
        if (!isDataEnabled(checkUserDataEnabled)) {
            return VOWIFI_CONFIG;
        }
        DataAllowFailReason failureReason = new DataAllowFailReason();
        if (isDataAllowed(failureReason)) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (isDataAllowedForApn(apnContext)) {
                    HwTelephonyFactory.getHwDataServiceChrManager().setAnyDataEnabledFalseReasonToNull();
                    return VDBG;
                }
            }
            return VOWIFI_CONFIG;
        }
        log(failureReason.getDataAllowFailReason());
        return VOWIFI_CONFIG;
    }

    private boolean isDataEnabled(boolean checkUserDataEnabled) {
        synchronized (this.mDataEnabledLock) {
            boolean z = (!this.mInternalDataEnabled || (checkUserDataEnabled && !this.mUserDataEnabled)) ? VOWIFI_CONFIG : checkUserDataEnabled ? sPolicyDataEnabled : VDBG;
            if (z) {
                return VDBG;
            }
            log("isDataEnabled: false, mInternalDataEnabled = " + this.mInternalDataEnabled + ", mUserDataEnabled = " + this.mUserDataEnabled + ", sPolicyDataEnabled = " + sPolicyDataEnabled + ", checkUserDataEnabled = " + checkUserDataEnabled);
            HwTelephonyFactory.getHwDataServiceChrManager().setAnyDataEnabledFalseReason(this.mInternalDataEnabled, this.mUserDataEnabled, sPolicyDataEnabled, checkUserDataEnabled);
            return VOWIFI_CONFIG;
        }
    }

    private boolean isDataAllowedForApn(ApnContext apnContext) {
        boolean z = VOWIFI_CONFIG;
        if ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && this.mPhone.getServiceState().getRilDataRadioTechnology() == 18) {
            log("Default data call activation not allowed in iwlan.");
            return VOWIFI_CONFIG;
        }
        if (apnContext.isReady()) {
            z = !isDataAllowedByApnContext(apnContext) ? isBipApnType(apnContext.getApnType()) : VDBG;
        }
        return z;
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();
        notifyDataConnection(PhoneInternalInterface.REASON_DATA_DETACHED);
        this.mAttached.set(VOWIFI_CONFIG);
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(VOWIFI_CONFIG);
        this.mPhone.getServiceStateTracker().setDoRecoveryMarker(VDBG);
        if (this.mCdmaPsRecoveryEnabled && getOverallState() == State.CONNECTED) {
            startPdpResetAlarm(PDP_RESET_ALARM_DELAY_IN_MS);
        }
    }

    private void onDataConnectionAttached() {
        log("onDataConnectionAttached");
        this.mAttached.set(VDBG);
        if (this.mCdmaPsRecoveryEnabled) {
            stopPdpResetAlarm();
        }
        clearRestartRildParam();
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(VOWIFI_CONFIG);
        if (getOverallState() == State.CONNECTED) {
            log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(VOWIFI_CONFIG);
            notifyDataConnection(PhoneInternalInterface.REASON_DATA_ATTACHED);
        } else {
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_ATTACHED);
        }
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachOnCreation.set(VDBG);
        }
        if (!(!isCTSimCard(this.mPhone.getPhoneId()) || this.preSetupBasedRadioTech == 0 || this.preSetupBasedRadioTech == this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            log("onDataConnectionAttached need to clear ApnContext, preSetupBasedRadioTech: " + this.preSetupBasedRadioTech);
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == State.SCANNING) {
                    apnContext.setState(State.IDLE);
                    cancelReconnectAlarm(apnContext);
                }
            }
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ATTACHED);
    }

    public boolean isDataAllowed(DataAllowFailReason failureReason, boolean isMms, boolean isUserEnable) {
        boolean z;
        synchronized (this.mDataEnabledLock) {
            boolean internalDataEnabled = this.mInternalDataEnabled;
        }
        boolean attachedState = this.mAttached.get();
        boolean desiredPowerState = this.mPhone.getServiceStateTracker().getDesiredPowerState();
        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        if (radioTech == 18) {
            desiredPowerState = VDBG;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        boolean z2 = VOWIFI_CONFIG;
        if (r != null) {
            z2 = !r.getImsiReady() ? r.getRecordsLoaded() : VDBG;
            if (!z2) {
                log("isDataAllowed getImsiReady=" + r.getImsiReady());
            }
        }
        boolean isDataAllowedVoWiFi = (HuaweiTelephonyConfigs.isQcomPlatform() && radioTech == 18) ? VDBG : VOWIFI_CONFIG;
        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        boolean defaultDataSelected = SubscriptionManager.isValidSubscriptionId(dataSub);
        if (VSimUtilsInner.isVSimEnabled()) {
            defaultDataSelected = VDBG;
        }
        PhoneConstants.State state = PhoneConstants.State.IDLE;
        if (this.mPhone.getCallTracker() != null) {
            state = this.mPhone.getCallTracker().getState();
        }
        if (failureReason != null) {
            failureReason.clearAllReasons();
        }
        if (attachedState || (this.mAutoAttachOnCreation.get() && this.mPhone.getSubId() == dataSub)) {
            z = VDBG;
        } else {
            z = isUserEnable;
        }
        if (!z) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.NOT_ATTACHED);
        }
        if (!(z2 || isUserEnable || isDataAllowedVoWiFi)) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.RECORD_NOT_LOADED);
        }
        if (!(state == PhoneConstants.State.IDLE || this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INVALID_PHONE_STATE);
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.CONCURRENT_VOICE_DATA_NOT_ALLOWED);
        }
        if (!internalDataEnabled) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.INTERNAL_DATA_DISABLED);
        }
        if (!defaultDataSelected) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.DEFAULT_DATA_UNSELECTED);
        }
        if (!(isDataAllowedForRoaming(isMms) || !this.mPhone.getServiceState().getDataRoaming() || getDataOnRoamingEnabled())) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.ROAMING_DISABLED);
        }
        if (this.mIsPsRestricted) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.PS_RESTRICTED);
        }
        if (!desiredPowerState) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.UNDESIRED_POWER_STATE);
        }
        if (!isPsAllowedByFdn()) {
            if (failureReason == null) {
                return VOWIFI_CONFIG;
            }
            failureReason.addDataAllowFailReason(DataAllowFailReasonType.PS_RESTRICTED_BY_FDN);
        }
        z = (failureReason == null || !failureReason.isFailed()) ? VDBG : VOWIFI_CONFIG;
        return z;
    }

    private void setupDataOnConnectableApns(String reason) {
        setupDataOnConnectableApns(reason, RetryFailures.ALWAYS);
    }

    private void setupDataOnConnectableApns(String reason, RetryFailures retryFailures) {
        this.preSetupBasedRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        log("setupDataOnConnectableApns: current radio technology: " + this.preSetupBasedRadioTech);
        log("setupDataOnConnectableApns: " + reason);
        StringBuilder sb = new StringBuilder(AbstractPhoneBase.BUFFER_SIZE);
        for (ApnContext apnContext : this.mPrioritySortedApnContexts) {
            sb.append(apnContext.getApnType());
            sb.append(":[state=");
            sb.append(apnContext.getState());
            sb.append(",enabled=");
            sb.append(apnContext.isEnabled());
            sb.append("] ");
        }
        log("setupDataOnConnectableApns: " + reason + " " + sb);
        for (ApnContext apnContext2 : this.mPrioritySortedApnContexts) {
            ArrayList waitingApns = null;
            if (apnContext2.getState() == State.FAILED || apnContext2.getState() == State.SCANNING) {
                if (retryFailures == RetryFailures.ALWAYS) {
                    apnContext2.releaseDataConnection(reason);
                } else if (apnContext2.isConcurrentVoiceAndDataAllowed() || !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                    int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                    ArrayList<ApnSetting> originalApns = apnContext2.getWaitingApns();
                    if (!(originalApns == null || originalApns.isEmpty())) {
                        waitingApns = buildWaitingApns(apnContext2.getApnType(), radioTech);
                        if (originalApns.size() != waitingApns.size() || !originalApns.containsAll(waitingApns)) {
                            apnContext2.releaseDataConnection(reason);
                        }
                    }
                } else {
                    apnContext2.releaseDataConnection(reason);
                }
            }
            if (isDefaultDataSubscription() && !apnContext2.isEnabled() && PhoneInternalInterface.REASON_SIM_LOADED.equals(reason) && "default".equals(apnContext2.getApnType()) && !this.mIsWifiConnected && !this.mIsBtConnected) {
                log("setupDataOnConnectableApns: for IMSI done, call setEnabled");
                apnContext2.setEnabled(VDBG);
            }
            if (apnContext2.isConnectable()) {
                log("isConnectable() call trySetupData");
                apnContext2.setReason(reason);
                trySetupData(apnContext2, waitingApns);
                HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(VDBG);
            } else {
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, this.mIsWifiConnected, this.mUserDataEnabled, apnContext2);
            }
        }
    }

    boolean isEmergency() {
        boolean isInEmergencyCall;
        synchronized (this.mDataEnabledLock) {
            isInEmergencyCall = !this.mPhone.isInEcm() ? this.mPhone.isInEmergencyCall() : VDBG;
        }
        log("isEmergency: result=" + isInEmergencyCall);
        return isInEmergencyCall;
    }

    private boolean trySetupData(ApnContext apnContext) {
        return trySetupData(apnContext, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean trySetupData(ApnContext apnContext, ArrayList<ApnSetting> waitingApns) {
        log("trySetupData for type:" + apnContext.getApnType() + " due to " + apnContext.getReason() + ", mIsPsRestricted=" + this.mIsPsRestricted);
        apnContext.requestLog("trySetupData due to " + apnContext.getReason());
        int voiceState = this.mPhone.getServiceState().getVoiceRegState();
        int dataState = this.mPhone.getServiceState().getDataRegState();
        log("dataState = " + dataState + "voiceState = " + voiceState + "OperatorNumeric = " + getOperatorNumeric());
        if ("default".equals(apnContext.getApnType()) && (dataState == 0 || voiceState == 0)) {
            this.mPreferredApn = getApnForCT();
            log("get prefered dp for CT " + this.mPreferredApn);
            if (this.mPreferredApn == null) {
                this.mPreferredApn = getPreferredApn();
            }
            log("get prefered DP " + this.mPreferredApn);
        }
        if (VSimUtilsInner.isVSimEnabled() && !VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) && !"mms".equals(apnContext.getApnType())) {
            log("trySetupData not allowed vsim is on for non vsim Dds except mms is enabled");
            return VOWIFI_CONFIG;
        } else if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) && VSimUtilsInner.isMmsOnM2()) {
            log("trySetupData not allowed for vsim sub while mms is on m2");
            return VOWIFI_CONFIG;
        } else if (VSimUtilsInner.isVSimOn() && VSimUtilsInner.isSubOnM2(this.mPhone.getPhoneId()) && "mms".equals(apnContext.getApnType()) && VSimUtilsInner.isM2CSOnly()) {
            log("trySetupData not allowed for sub on m2 while ps not ready");
            VSimUtilsInner.checkMmsStart(this.mPhone.getPhoneId());
            return VOWIFI_CONFIG;
        } else if (!isDefaultDataSubscription() && !"mms".equals(apnContext.getApnType()) && !NetworkFactory.isDualCellDataEnable()) {
            log("trySetupData not allowed on non defaultDds except mms is enabled");
            return VOWIFI_CONFIG;
        } else if (this.mPhone.getSimulatedRadioControl() != null) {
            apnContext.setState(State.CONNECTED);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            log("trySetupData: X We're on the simulator; assuming connected retValue=true");
            return VDBG;
        } else {
            boolean isDataAllowed;
            boolean isEmergencyApn = apnContext.getApnType().equals("emergency");
            ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
            boolean checkUserDataEnabled = ApnSetting.isMeteredApnType(apnContext.getApnType(), this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming());
            if ((HuaweiTelephonyConfigs.isChinaTelecom() && apnContext.getApnType().equals("supl")) || apnContext.getApnType().equals("xcap") || isBipApnType(apnContext.getApnType())) {
                checkUserDataEnabled = VOWIFI_CONFIG;
            }
            DataAllowFailReason failureReason = new DataAllowFailReason();
            if (isDataAllowed(failureReason)) {
                isDataAllowed = VDBG;
            } else if (failureReason.isFailForSingleReason(DataAllowFailReasonType.ROAMING_DISABLED)) {
                if (ApnSetting.isMeteredApnType(apnContext.getApnType(), this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming())) {
                    isDataAllowed = isDataAllowedForRoaming("mms".equals(apnContext.getApnType()));
                } else {
                    isDataAllowed = VDBG;
                }
            } else {
                isDataAllowed = VOWIFI_CONFIG;
            }
            if (apnContext.isConnectable()) {
                if (!isEmergencyApn) {
                    if (isDataAllowed && isDataAllowedForApn(apnContext)) {
                        if (getAnyDataEnabledByApnContext(apnContext, getAnyDataEnabled(checkUserDataEnabled))) {
                        }
                    }
                }
                if (!this.mColdSimDetected) {
                    String str;
                    if (apnContext.getState() == State.FAILED) {
                        str = "trySetupData: make a FAILED ApnContext IDLE so its reusable";
                        log(str);
                        apnContext.requestLog(str);
                        apnContext.setState(State.IDLE);
                    }
                    int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                    apnContext.setConcurrentVoiceAndDataAllowed(sst.isConcurrentVoiceAndDataAllowed());
                    if (apnContext.getState() == State.IDLE) {
                        if (waitingApns == null) {
                            waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                        }
                        if (waitingApns.isEmpty()) {
                            notifyNoData(DcFailCause.MISSING_UNKNOWN_APN, apnContext);
                            notifyOffApnsOfAvailability(apnContext.getReason());
                            str = "trySetupData: X No APN found retValue=false";
                            log(str);
                            apnContext.requestLog(str);
                            return VOWIFI_CONFIG;
                        }
                        apnContext.setWaitingApns(waitingApns);
                        log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
                    }
                    boolean retValue = setupData(apnContext, radioTech);
                    notifyOffApnsOfAvailability(apnContext.getReason());
                    log("trySetupData: X retValue=" + retValue);
                    return retValue;
                }
            }
            if (!apnContext.getApnType().equals("default") && apnContext.isConnectable()) {
                this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
            }
            notifyOffApnsOfAvailability(apnContext.getReason());
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, this.mIsWifiConnected, this.mUserDataEnabled, apnContext);
            StringBuilder str2 = new StringBuilder();
            str2.append("trySetupData failed. apnContext = [type=").append(apnContext.getApnType()).append(", mState=").append(apnContext.getState()).append(", mDataEnabled=").append(apnContext.isEnabled()).append(", mDependencyMet=").append(apnContext.getDependencyMet()).append("] ");
            if (!apnContext.isConnectable()) {
                str2.append("isConnectable = false. ");
            }
            if (!isDataAllowed) {
                str2.append("data not allowed: ").append(failureReason.getDataAllowFailReason()).append(". ");
            }
            if (!isDataAllowedForApn(apnContext)) {
                str2.append("isDataAllowedForApn = false. RAT = ").append(this.mPhone.getServiceState().getRilDataRadioTechnology());
            }
            if (!isDataEnabled(checkUserDataEnabled)) {
                str2.append("isDataEnabled(").append(checkUserDataEnabled).append(") = false. ").append("mInternalDataEnabled = ").append(this.mInternalDataEnabled).append(" , mUserDataEnabled = ").append(this.mUserDataEnabled).append(", sPolicyDataEnabled = ").append(sPolicyDataEnabled).append(" ");
            }
            if (isEmergency()) {
                str2.append("emergency = true");
            }
            if (this.mColdSimDetected) {
                str2.append("coldSimDetected = true");
            }
            log(str2.toString());
            apnContext.requestLog(str2.toString());
            return VOWIFI_CONFIG;
        }
    }

    private void notifyOffApnsOfAvailability(String reason) {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                log("notifyOffApnsOfAvailability skipped apn due to attached && isReady " + apnContext.toString());
            } else if (apnContext.getApnType() == null || !apnContext.getApnType().startsWith("bip")) {
                String str;
                log("notifyOffApnOfAvailability type:" + apnContext.getApnType());
                Phone phone = this.mPhone;
                if (reason != null) {
                    str = reason;
                } else {
                    str = apnContext.getReason();
                }
                phone.notifyDataConnection(str, apnContext.getApnType(), DataState.DISCONNECTED);
            }
        }
    }

    protected boolean cleanUpAllConnections(boolean tearDown, String reason) {
        log("cleanUpAllConnections: tearDown=" + tearDown + " reason=" + reason);
        boolean didDisconnect = VOWIFI_CONFIG;
        boolean specificDisable = VOWIFI_CONFIG;
        if (!TextUtils.isEmpty(reason)) {
            if (reason.equals(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED)) {
                specificDisable = VDBG;
            } else {
                specificDisable = reason.equals(PhoneInternalInterface.REASON_ROAMING_ON);
            }
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[apnContext.getState().ordinal()]) {
                case LTE_NOT_ROAMING /*4*/:
                case PREF_APN_ID_LEN /*5*/:
                case CharacterSets.ISO_8859_4 /*7*/:
                    break;
                default:
                    didDisconnect = VDBG;
                    if (!specificDisable) {
                        apnContext.setReason(reason);
                        cleanUpConnection(tearDown, apnContext);
                        break;
                    }
                    ApnSetting apnSetting = apnContext.getApnSetting();
                    if (!(apnSetting == null || !apnSetting.isMetered(this.mPhone.getContext(), this.mPhone.getSubId(), this.mPhone.getServiceState().getDataRoaming()) || apnContext.getApnType().equals("xcap"))) {
                        log("clean up metered ApnContext Type: " + apnContext.getApnType());
                        apnContext.setReason(reason);
                        cleanUpConnection(tearDown, apnContext);
                        break;
                    }
            }
        }
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mRequestedApnType = "default";
        log("cleanUpConnection: mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
        return didDisconnect;
    }

    private void onCleanUpAllConnections(String cause) {
        cleanUpAllConnections((boolean) VDBG, cause);
    }

    void sendCleanUpConnection(boolean tearDown, ApnContext apnContext) {
        int i;
        log("sendCleanUpConnection: tearDown=" + tearDown + " apnContext=" + apnContext);
        Message msg = obtainMessage(270360);
        if (tearDown) {
            i = VP_START;
        } else {
            i = VP_END;
        }
        msg.arg1 = i;
        msg.arg2 = VP_END;
        msg.obj = apnContext;
        sendMessage(msg);
    }

    protected void cleanUpConnection(boolean tearDown, ApnContext apnContext) {
        if (apnContext == null) {
            log("cleanUpConnection: apn context is null");
            return;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        String str = "cleanUpConnection: tearDown=" + tearDown + " reason=" + apnContext.getReason();
        log(str + " apnContext=" + apnContext);
        apnContext.requestLog(str);
        if (tearDown) {
            if (apnContext.isDisconnected()) {
                apnContext.setState(State.IDLE);
                if (!apnContext.isReady()) {
                    if (dcac != null) {
                        str = "cleanUpConnection: teardown, disconnected, !ready";
                        log(str + " apnContext=" + apnContext);
                        apnContext.requestLog(str);
                        dcac.tearDown(apnContext, "", null);
                    }
                    apnContext.setDataConnectionAc(null);
                }
            } else if (dcac == null) {
                apnContext.setState(State.IDLE);
                apnContext.requestLog("cleanUpConnection: connected, bug no DCAC");
                this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            } else if (apnContext.getState() != State.DISCONNECTING) {
                boolean disconnectAll = VOWIFI_CONFIG;
                if ("dun".equals(apnContext.getApnType()) && teardownForDun()) {
                    log("cleanUpConnection: disconnectAll DUN connection");
                    disconnectAll = VDBG;
                }
                int generation = apnContext.getConnectionGeneration();
                str = "cleanUpConnection: tearing down" + (disconnectAll ? " all" : "") + " using gen#" + generation;
                log(str + "apnContext=" + apnContext);
                apnContext.requestLog(str);
                Message msg = obtainMessage(270351, new Pair(apnContext, Integer.valueOf(generation)));
                if (disconnectAll) {
                    apnContext.getDcAc().tearDownAll(apnContext.getReason(), msg);
                } else {
                    apnContext.getDcAc().tearDown(apnContext, apnContext.getReason(), msg);
                }
                apnContext.setState(State.DISCONNECTING);
                this.mDisconnectPendingCount += VP_START;
            }
        } else if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(apnContext.getReason()) && apnContext.getState() == State.CONNECTING) {
            log("ignore the set IDLE message, because the current state is connecting!");
        } else {
            if (dcac != null) {
                dcac.reqReset();
            }
            apnContext.setState(State.IDLE);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            apnContext.setDataConnectionAc(null);
        }
        setupDataForSinglePdnArbitration(apnContext.getReason());
        if (dcac != null) {
            cancelReconnectAlarm(apnContext);
        }
        str = "cleanUpConnection: X tearDown=" + tearDown + " reason=" + apnContext.getReason();
        log(str + " apnContext=" + apnContext + " dcac=" + apnContext.getDcAc());
        apnContext.requestLog(str);
    }

    ApnSetting fetchDunApn() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", VOWIFI_CONFIG)) {
            log("fetchDunApn: net.tethering.noprovisioning=true ret: null");
            return null;
        }
        int bearer = this.mPhone.getServiceState().getRilDataRadioTechnology();
        String apnData = Global.getString(this.mResolver, "tether_dun_apn");
        IccRecords r = (IccRecords) this.mIccRecords.get();
        for (ApnSetting dunSetting : ApnSetting.arrayFromString(apnData)) {
            String operator = r != null ? r.getOperatorNumeric() : "";
            if (ServiceState.bitmaskHasTech(dunSetting.bearerBitmask, bearer) && dunSetting.numeric.equals(operator)) {
                if (dunSetting.hasMvnoParams()) {
                    if (r != null && ApnSetting.mvnoMatches(r, dunSetting.mvnoType, dunSetting.mvnoMatchData)) {
                        log("fetchDunApn: global TETHER_DUN_APN dunSetting=" + dunSetting);
                        return dunSetting;
                    }
                } else if (!this.mMvnoMatched) {
                    log("fetchDunApn: global TETHER_DUN_APN dunSetting=" + dunSetting);
                    return dunSetting;
                }
            }
        }
        disableGoogleDunApn(this.mPhone.getContext(), apnData, null);
        log("fetchDunApn: config_tether_apndata dunSetting=" + null);
        return null;
    }

    public boolean hasMatchedTetherApnSetting() {
        ApnSetting matched = fetchDunApn();
        log("hasMatchedTetherApnSetting: APN=" + matched);
        return matched != null ? VDBG : VOWIFI_CONFIG;
    }

    protected void setupDataForSinglePdnArbitration(String reason) {
        log("setupDataForSinglePdn: reason = " + reason + " isDisconnected = " + isDisconnected());
        if (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isDisconnected() && !PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION.equals(reason)) {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
        }
    }

    private boolean teardownForDun() {
        boolean z = VDBG;
        if (ServiceState.isCdma(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            return VDBG;
        }
        if (fetchDunApn() == null) {
            z = VOWIFI_CONFIG;
        }
        return z;
    }

    private void cancelReconnectAlarm(ApnContext apnContext) {
        if (apnContext != null) {
            PendingIntent intent = apnContext.getReconnectIntent();
            if (intent != null) {
                ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(intent);
                apnContext.setReconnectIntent(null);
            }
        }
    }

    private String[] parseTypes(String types) {
        if (types != null && !types.equals("")) {
            return types.split(",");
        }
        String[] result = new String[VP_START];
        result[VP_END] = CharacterSets.MIMENAME_ANY_CHARSET;
        return result;
    }

    boolean isPermanentFail(DcFailCause dcFailCause) {
        if (dcFailCause.isPermanentFail()) {
            return (this.mAttached.get() && dcFailCause == DcFailCause.SIGNAL_LOST) ? VOWIFI_CONFIG : VDBG;
        } else {
            return VOWIFI_CONFIG;
        }
    }

    private ApnSetting makeApnSetting(Cursor cursor) {
        String[] types = parseTypes(cursor.getString(cursor.getColumnIndexOrThrow(TelephonyEventLog.DATA_KEY_DATA_CALL_TYPE)));
        ApnSetting apn = new ApnSetting(cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID)), cursor.getString(cursor.getColumnIndexOrThrow(GlobalMatchs.NUMERIC)), cursor.getString(cursor.getColumnIndexOrThrow(Part.NAME)), cursor.getString(cursor.getColumnIndexOrThrow(TelephonyEventLog.DATA_KEY_APN)), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow(Carriers.PROXY))), cursor.getString(cursor.getColumnIndexOrThrow(Carriers.PORT)), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow(Carriers.MMSC))), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow(Carriers.MMSPROXY))), cursor.getString(cursor.getColumnIndexOrThrow(Carriers.MMSPORT)), cursor.getString(cursor.getColumnIndexOrThrow(Carriers.USER)), cursor.getString(cursor.getColumnIndexOrThrow(Carriers.PASSWORD)), cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.AUTH_TYPE)), types, cursor.getString(cursor.getColumnIndexOrThrow(TelephonyEventLog.DATA_KEY_PROTOCOL)), cursor.getString(cursor.getColumnIndexOrThrow(Carriers.ROAMING_PROTOCOL)), cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.CARRIER_ENABLED)) == VP_START ? VDBG : VOWIFI_CONFIG, cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.BEARER)), cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.BEARER_BITMASK)), cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.PROFILE_ID)), cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.MODEM_COGNITIVE)) == VP_START ? VDBG : VOWIFI_CONFIG, cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.MAX_CONNS)), cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.WAIT_TIME)), cursor.getInt(cursor.getColumnIndexOrThrow(Carriers.MAX_CONNS_TIME)), cursor.getInt(cursor.getColumnIndexOrThrow(TextBasedSmsColumns.MTU)), cursor.getString(cursor.getColumnIndexOrThrow(Carriers.MVNO_TYPE)), cursor.getString(cursor.getColumnIndexOrThrow(Carriers.MVNO_MATCH_DATA)));
        ApnSetting hwApn = makeHwApnSetting(cursor, types);
        if (hwApn != null) {
            return hwApn;
        }
        return apn;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ArrayList<ApnSetting> result;
        ArrayList<ApnSetting> mnoApns = new ArrayList();
        ArrayList<ApnSetting> mvnoApns = new ArrayList();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (cursor.moveToFirst()) {
            do {
                ApnSetting apn = makeApnSetting(cursor);
                if (apn != null) {
                    if (VOWIFI_CONFIG) {
                        boolean isVowifiMmsApn = VOWIFI_CONFIG;
                        for (int i = VP_END; i < apn.types.length; i += VP_START) {
                            if ("vowifi_mms".equals(apn.types[i])) {
                                log("found vowifi_mms apn");
                                isVowifiMmsApn = VDBG;
                                break;
                            }
                        }
                    }
                    if (!apn.hasMvnoParams()) {
                        mnoApns.add(apn);
                    } else if (r != null && ApnSetting.mvnoMatches(r, apn.mvnoType, apn.mvnoMatchData)) {
                        mvnoApns.add(apn);
                    }
                }
            } while (cursor.moveToNext());
        }
        if (mvnoApns.isEmpty()) {
            result = mnoApns;
            this.mMvnoMatched = VOWIFI_CONFIG;
        } else {
            result = mvnoApns;
            this.mMvnoMatched = VDBG;
        }
        log("createApnList: X result=" + result);
        return result;
    }

    private boolean dataConnectionNotInUse(DcAsyncChannel dcac) {
        log("dataConnectionNotInUse: check if dcac is inuse dcac=" + dcac);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getDcAc() == dcac) {
                log("dataConnectionNotInUse: in use by apnContext=" + apnContext);
                return VOWIFI_CONFIG;
            }
        }
        log("dataConnectionNotInUse: tearDownAll");
        dcac.tearDownAll("No connection", null);
        log("dataConnectionNotInUse: not in use return true");
        return VDBG;
    }

    private DcAsyncChannel findFreeDataConnection() {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            if (dcac.isInactiveSync() && dataConnectionNotInUse(dcac)) {
                log("findFreeDataConnection: found free DataConnection= dcac=" + dcac);
                return dcac;
            }
        }
        log("findFreeDataConnection: NO free DataConnection");
        return null;
    }

    protected boolean isLTENetwork() {
        int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        log("dataRadioTech = " + dataRadioTech);
        if (dataRadioTech == 13 || dataRadioTech == 14) {
            return VDBG;
        }
        return VOWIFI_CONFIG;
    }

    private ApnSetting getApnForCT() {
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            log("getApnForCT not isCTSimCard");
            return null;
        } else if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            log("getApnForCT mAllApnSettings == null");
            return null;
        } else if (get2gSlot() == this.mPhone.getSubId() && !isCTDualModeCard(get2gSlot())) {
            log("getApnForCT otherslot == mPhone.getSubId() && !isCTDualModeCard(otherslot)");
            return null;
        } else if (this.mPhone.getServiceState().getOperatorNumeric() == null) {
            log("getApnForCT mPhone.getServiceState().getOperatorNumeric() == null");
            return null;
        } else if (getPreferredApn() != null && !isApnPreset(getPreferredApn())) {
            return null;
        } else {
            ApnSetting apnSetting = null;
            this.mCurrentState = getCurState();
            int matchApnId = matchApnId(this.mCurrentState);
            if (-1 == matchApnId) {
                switch (this.mCurrentState) {
                    case VP_END /*0*/:
                        if (!isCTCardForFullNet()) {
                            apnSetting = setApnForCT(CT_NOT_ROAMING_APN_PREFIX);
                            break;
                        }
                        log("getApnForCT: select ctnet for fullNet product");
                        apnSetting = setApnForCT(CT_ROAMING_APN_PREFIX);
                        break;
                    case VP_START /*1*/:
                    case GSM_ROAMING_CARD1 /*2*/:
                    case GSM_ROAMING_CARD2 /*3*/:
                        apnSetting = setApnForCT(CT_ROAMING_APN_PREFIX);
                        break;
                    case LTE_NOT_ROAMING /*4*/:
                        apnSetting = setApnForCT(CT_LTE_APN_PREFIX);
                        break;
                    default:
                        log("Error in CurrentState" + this.mCurrentState);
                        break;
                }
            }
            setPreferredApn(matchApnId);
            return apnSetting;
        }
    }

    private int matchApnId(int sign) {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        int matchId = -1;
        if (this.isMultiSimEnabled) {
            preferredApnIdSlot = PREFERRED_APN_ID + (get4gSlot() == this.mPhone.getSubId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String LastApnId = System.getString(cr, preferredApnIdSlot);
            log("MatchApnId:LastApnId: " + LastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (LastApnId != null) {
                String[] ApId = LastApnId.split(",");
                if (PREF_APN_ID_LEN != ApId.length || ApId[this.mCurrentState] == null) {
                    System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                    return matchId;
                }
                if (!ProxyController.MODEM_0.equals(ApId[this.mCurrentState])) {
                    matchId = Integer.parseInt(ApId[this.mCurrentState]);
                }
                return matchId;
            }
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
            return matchId;
        } catch (Exception ex) {
            log("MatchApnId got exception =" + ex);
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        }
    }

    private int getCurState() {
        int currentStatus = -1;
        if (isLTENetwork()) {
            currentStatus = LTE_NOT_ROAMING;
        } else if (this.mPhone.getPhoneType() == GSM_ROAMING_CARD1) {
            currentStatus = TelephonyManager.getDefault().isNetworkRoaming(get4gSlot()) ? VP_START : VP_END;
        } else if (this.mPhone.getPhoneType() == VP_START) {
            if (get4gSlot() == this.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(get4gSlot())) {
                currentStatus = GSM_ROAMING_CARD1;
            } else if (get2gSlot() == this.mPhone.getSubId() && TelephonyManager.getDefault().isNetworkRoaming(get2gSlot())) {
                currentStatus = GSM_ROAMING_CARD2;
            }
        }
        log("getCurState:CurrentStatus =" + currentStatus);
        return currentStatus;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ApnSetting setApnForCT(String apn) {
        if (apn == null || "".equals(apn)) {
            return null;
        }
        ContentResolver resolver = this.mPhone.getContext().getContentResolver();
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty() || resolver == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
        for (ApnSetting dp : this.mAllApnSettings) {
            if (apn.equals(dp.apn) && dp.canHandleType(this.mRequestedApnType)) {
                if ((!isLTENetwork() || dp.bearer == 13 || dp.bearer == 14) && (isLTENetwork() || !(dp.bearer == 13 || dp.bearer == 14))) {
                    resolver.delete(uri, null, null);
                    values.put(APN_ID, Integer.valueOf(dp.id));
                    resolver.insert(uri, values);
                    return dp;
                }
            }
        }
        return null;
    }

    private boolean setupData(ApnContext apnContext, int radioTech) {
        log("setupData: apnContext=" + apnContext);
        apnContext.requestLog("setupData");
        DcAsyncChannel dcac = null;
        ApnSetting apnSetting = apnContext.getNextApnSetting();
        if (apnSetting == null) {
            log("setupData: return for no apn found!");
            return VOWIFI_CONFIG;
        }
        int profileId = apnSetting.profileId;
        if (profileId == 0) {
            profileId = getApnProfileID(apnContext.getApnType());
        }
        if (!(apnContext.getApnType() == "dun" && teardownForDun())) {
            dcac = checkForCompatibleConnectedApnContext(apnContext);
        }
        if (dcac == null) {
            if (isOnlySingleDcAllowed(radioTech)) {
                if (isHigherPriorityApnContextActive(apnContext)) {
                    log("setupData: Higher priority ApnContext active.  Ignoring call");
                    return VOWIFI_CONFIG;
                } else if (cleanUpAllConnections((boolean) VDBG, PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION)) {
                    log("setupData: Some calls are disconnecting first.  Wait and retry");
                    return VOWIFI_CONFIG;
                } else {
                    log("setupData: Single pdp. Continue setting up data call.");
                }
            }
            dcac = findFreeDataConnection();
            if (dcac == null) {
                dcac = createDataConnection();
            }
            if (dcac == null) {
                log("setupData: No free DataConnection and couldn't create one, WEIRD");
                return VOWIFI_CONFIG;
            }
        }
        int generation = apnContext.incAndGetConnectionGeneration();
        log("setupData: dcac=" + dcac + " apnSetting=" + apnSetting + " gen#=" + generation);
        apnContext.setDataConnectionAc(dcac);
        apnContext.setApnSetting(apnSetting);
        apnContext.setState(State.CONNECTING);
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        Message msg = obtainMessage();
        msg.what = 270336;
        msg.obj = new Pair(apnContext, Integer.valueOf(generation));
        HwTelephonyFactory.getHwDataServiceChrManager().setBringUp(VDBG);
        dcac.bringUp(apnContext, profileId, radioTech, msg, generation);
        log("setupData: initing!");
        return VDBG;
    }

    private void setInitialAttachApn() {
        ApnSetting iaApnSetting = null;
        ApnSetting defaultApnSetting = null;
        ApnSetting firstApnSetting = null;
        if (get4gSlot() != this.mPhone.getSubId()) {
            log("setInitialAttachApn: not 4g slot , skip");
        } else if (VSimUtilsInner.isVSimOn()) {
            log("setInitialAttachApn: vsim is on, skip");
        } else if (SystemProperties.getBoolean("persist.radio.iot_attach_apn", VOWIFI_CONFIG)) {
            log("setInitialAttachApn: iot attach apn enabled, skip");
        } else {
            int esmFlag = VP_END;
            boolean esmFlagAdaptionEnabled = SystemProperties.getBoolean("ro.config.attach_apn_enabled", VOWIFI_CONFIG);
            if (esmFlagAdaptionEnabled) {
                String plmnsConfig = System.getString(this.mPhone.getContext().getContentResolver(), "plmn_esm_flag");
                log("setInitialAttachApn: plmnsConfig = " + plmnsConfig);
                IccRecords r = (IccRecords) this.mIccRecords.get();
                String operator = r != null ? r.getOperatorNumeric() : "null";
                if (plmnsConfig != null) {
                    String[] plmns = plmnsConfig.split(",");
                    int length = plmns.length;
                    for (int i = VP_END; i < length; i += VP_START) {
                        String plmn = plmns[i];
                        if (plmn != null && plmn.equals(operator)) {
                            log("setInitialAttachApn: send initial attach apn for operator " + operator);
                            esmFlag = VP_START;
                            break;
                        }
                    }
                }
            }
            if (isCTSimCard(this.mPhone.getPhoneId())) {
                log("setInitialAttachApn: send initial attach apn for CT");
                esmFlag = VP_START;
            }
            if (esmFlag != 0) {
                log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
                if (!(this.mAllApnSettings == null || this.mAllApnSettings.isEmpty())) {
                    firstApnSetting = (ApnSetting) this.mAllApnSettings.get(VP_END);
                    log("setInitialApn: firstApnSetting=" + firstApnSetting);
                    for (ApnSetting apn : this.mAllApnSettings) {
                        if (ArrayUtils.contains(apn.types, "ia") && apn.carrierEnabled) {
                            log("setInitialApn: iaApnSetting=" + apn);
                            iaApnSetting = apn;
                            break;
                        } else if (defaultApnSetting == null && apn.canHandleType("default")) {
                            log("setInitialApn: defaultApnSetting=" + apn);
                            if (!isCTSimCard(this.mPhone.getPhoneId())) {
                                defaultApnSetting = apn;
                            } else if (isSupportLTE(apn)) {
                                defaultApnSetting = apn;
                            }
                        }
                    }
                }
                ApnSetting initialAttachApnSetting = null;
                if (this.mPreferredApn != null) {
                    log("setInitialAttachApn: using mPreferredApn");
                    if (isCTSimCard(this.mPhone.getPhoneId())) {
                        initialAttachApnSetting = isSupportLTE(this.mPreferredApn) ? this.mPreferredApn : defaultApnSetting != null ? defaultApnSetting : iaApnSetting;
                    } else {
                        initialAttachApnSetting = this.mPreferredApn;
                    }
                } else if (defaultApnSetting != null) {
                    log("setInitialAttachApn: using defaultApnSetting");
                    initialAttachApnSetting = defaultApnSetting;
                } else if (iaApnSetting != null) {
                    log("setInitialAttachApn: using iaApnSetting");
                    initialAttachApnSetting = iaApnSetting;
                } else if (firstApnSetting != null) {
                    log("setInitialAttachApn: using firstApnSetting");
                    if (!isCTSimCard(this.mPhone.getPhoneId())) {
                        initialAttachApnSetting = firstApnSetting;
                    } else if (isSupportLTE(firstApnSetting)) {
                        initialAttachApnSetting = firstApnSetting;
                    }
                }
                if (initialAttachApnSetting == null) {
                    log("setInitialAttachApn: X There in no available apn");
                } else {
                    log("setInitialAttachApn: X selected Apn=" + initialAttachApnSetting);
                    this.mPhone.mCi.setInitialAttachApn(initialAttachApnSetting.apn, initialAttachApnSetting.protocol, initialAttachApnSetting.authType, initialAttachApnSetting.user, initialAttachApnSetting.password, null);
                }
            } else if (esmFlagAdaptionEnabled) {
                log("setInitialAttachApn: send empty initial attach apn to clear esmflag");
                this.mPhone.mCi.setInitialAttachApn("", SystemProperties.get("ro.config.attach_ip_type", "IP"), VP_END, "", "", null);
            } else {
                log("setInitialAttachApn: no need to send initial attach apn");
            }
        }
    }

    private void onApnChanged() {
        boolean z = VOWIFI_CONFIG;
        State overallState = getOverallState();
        boolean isDisconnected = overallState != State.IDLE ? overallState == State.FAILED ? VDBG : VOWIFI_CONFIG : VDBG;
        if (this.mPhone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        }
        log("onApnChanged: createAllApnList and cleanUpAllConnections");
        createAllApnList();
        setInitialAttachApn();
        ApnSetting mCurPreApn = getPreferredApn();
        if (this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId() && mCurPreApn == null) {
            boolean z2;
            if (isDisconnected) {
                z2 = VOWIFI_CONFIG;
            } else {
                z2 = VDBG;
            }
            cleanUpAllConnections(z2, PhoneInternalInterface.REASON_APN_CHANGED);
        } else {
            if (!isDisconnected) {
                z = VDBG;
            }
            cleanUpConnectionsOnUpdatedApns(z);
        }
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_APN_CHANGED);
        }
    }

    private void updateApnId() {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        if (this.isMultiSimEnabled) {
            preferredApnIdSlot = PREFERRED_APN_ID + (get4gSlot() == this.mPhone.getSubId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String LastApnId = System.getString(cr, preferredApnIdSlot);
            this.mCurrentState = getCurState();
            log("updateApnId:LastApnId: " + LastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (LastApnId != null) {
                String[] ApId = LastApnId.split(",");
                ApnSetting CurPreApn = getPreferredApn();
                StringBuffer temApnId = new StringBuffer();
                if (PREF_APN_ID_LEN != ApId.length || ApId[this.mCurrentState] == null) {
                    System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                    return;
                }
                if (CurPreApn == null) {
                    log("updateApnId:CurPreApn: CurPreApn == null");
                    ApId[this.mCurrentState] = ProxyController.MODEM_0;
                } else {
                    log("updateApnId:CurPreApn: " + CurPreApn + ", CurPreApnId: " + Integer.toString(CurPreApn.id));
                    ApId[this.mCurrentState] = Integer.toString(CurPreApn.id);
                }
                for (int i = VP_END; i < ApId.length; i += VP_START) {
                    temApnId.append(ApId[i]);
                    if (i != ApId.length - 1) {
                        temApnId.append(",");
                    }
                }
                System.putString(cr, preferredApnIdSlot, temApnId.toString());
                return;
            }
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        } catch (Exception ex) {
            log("updateApnId got exception =" + ex);
            System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        }
    }

    private DcAsyncChannel findDataConnectionAcByCid(int cid) {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            if (dcac.getCidSync() == cid) {
                return dcac;
            }
        }
        return null;
    }

    private void gotoIdleAndNotifyDataConnection(String reason) {
        log("gotoIdleAndNotifyDataConnection: reason=" + reason);
        notifyDataConnection(reason);
    }

    private boolean isHigherPriorityApnContextActive(ApnContext apnContext) {
        for (ApnContext otherContext : this.mPrioritySortedApnContexts) {
            if (apnContext.getApnType().equalsIgnoreCase(otherContext.getApnType())) {
                return VOWIFI_CONFIG;
            }
            if (otherContext.isEnabled() && otherContext.getState() != State.FAILED) {
                return VDBG;
            }
        }
        return VOWIFI_CONFIG;
    }

    private boolean isOnlySingleDcAllowed(int rilRadioTech) {
        int[] singleDcRats = this.mPhone.getContext().getResources().getIntArray(17236021);
        boolean onlySingleDcAllowed = VOWIFI_CONFIG;
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("persist.telephony.test.singleDc", VOWIFI_CONFIG)) {
            onlySingleDcAllowed = VDBG;
        }
        if (singleDcRats != null) {
            for (int i = VP_END; i < singleDcRats.length && !onlySingleDcAllowed; i += VP_START) {
                if (rilRadioTech == singleDcRats[i]) {
                    onlySingleDcAllowed = VDBG;
                }
            }
        }
        onlySingleDcAllowed = shouldDisableMultiPdps(onlySingleDcAllowed);
        log("isOnlySingleDcAllowed(" + rilRadioTech + "): " + onlySingleDcAllowed);
        return onlySingleDcAllowed;
    }

    void sendRestartRadio() {
        log("sendRestartRadio:");
        sendMessage(obtainMessage(270362));
    }

    private void restartRadio() {
        log("restartRadio: ************TURN OFF RADIO**************");
        cleanUpAllConnections((boolean) VDBG, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        this.mPhone.getServiceStateTracker().powerOffRadioSafely(this);
        SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", ProxyController.MODEM_0)) + VP_START));
    }

    private boolean retryAfterDisconnected(ApnContext apnContext) {
        boolean retry = VDBG;
        String reason = apnContext.getReason();
        if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(reason) || (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = VOWIFI_CONFIG;
        }
        if (AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(reason)) {
            return VOWIFI_CONFIG;
        }
        return retry;
    }

    private void startAlarmForReconnect(long delay, ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent("com.android.internal.telephony.data-reconnect." + apnType);
        intent.addFlags(268435456);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, apnContext.getReason());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE, apnType);
        intent.addFlags(268435456);
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        intent.putExtra("subscription", this.mPhone.getSubId());
        log("startAlarmForReconnect: delay=" + delay + " action=" + intent.getAction() + " apn=" + apnContext);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), VP_END, intent, 134217728);
        apnContext.setReconnectIntent(alarmIntent);
        this.mAlarmManager.setExact(GSM_ROAMING_CARD1, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    private void notifyNoData(DcFailCause lastFailCauseCode, ApnContext apnContext) {
        log("notifyNoData: type=" + apnContext.getApnType());
        if (isPermanentFail(lastFailCauseCode) && !apnContext.getApnType().equals("default")) {
            SystemProperties.set("ril.ps_ce_reason", String.valueOf(lastFailCauseCode.getErrorCode()));
            this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
    }

    public boolean getAutoAttachOnCreation() {
        return this.mAutoAttachOnCreation.get();
    }

    private void onRecordsLoadedOrSubIdChanged() {
        log("onRecordsLoadedOrSubIdChanged: createAllApnList");
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17957015);
        updateApnContextState();
        createAllApnList();
        if (isBlockSetInitialAttachApn()) {
            log("onRecordsLoadedOrSubIdChanged: block setInitialAttachApn");
        } else {
            setInitialAttachApn();
        }
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoadedOrSubIdChanged: notifying data availability");
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_SIM_LOADED);
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SIM_LOADED);
    }

    private void onSimNotReady() {
        log("onSimNotReady");
        cleanUpAllConnections((boolean) VDBG, PhoneInternalInterface.REASON_SIM_NOT_READY);
        this.mAllApnSettings = null;
        this.mAutoAttachOnCreationConfig = VOWIFI_CONFIG;
    }

    private void onSetDependencyMet(String apnType, boolean met) {
        if (!"hipri".equals(apnType)) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            if (apnContext == null) {
                loge("onSetDependencyMet: ApnContext not found in onSetDependencyMet(" + apnType + ", " + met + ")");
                return;
            }
            applyNewState(apnContext, apnContext.isEnabled(), met);
            if ("default".equals(apnType)) {
                apnContext = (ApnContext) this.mApnContexts.get("hipri");
                if (apnContext != null) {
                    applyNewState(apnContext, apnContext.isEnabled(), met);
                }
            }
        }
    }

    private void onSetPolicyDataEnabled(boolean enabled) {
        synchronized (this.mDataEnabledLock) {
            if (sPolicyDataEnabled != enabled) {
                sPolicyDataEnabled = enabled;
                if (enabled) {
                    onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                } else {
                    onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                }
            }
        }
    }

    private void applyNewState(ApnContext apnContext, boolean enabled, boolean met) {
        boolean cleanup = VOWIFI_CONFIG;
        boolean trySetup = VOWIFI_CONFIG;
        String str = "applyNewState(" + apnContext.getApnType() + ", " + enabled + "(" + apnContext.isEnabled() + "), " + met + "(" + apnContext.getDependencyMet() + "))";
        log(str);
        apnContext.requestLog(str);
        if (apnContext.isReady()) {
            cleanup = VDBG;
            if (enabled && met) {
                State state = apnContext.getState();
                switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[state.ordinal()]) {
                    case VP_START /*1*/:
                    case GSM_ROAMING_CARD1 /*2*/:
                    case GSM_ROAMING_CARD2 /*3*/:
                    case CharacterSets.ISO_8859_4 /*7*/:
                        log("applyNewState: 'ready' so return");
                        apnContext.requestLog("applyNewState state=" + state + ", so return");
                        return;
                    case LTE_NOT_ROAMING /*4*/:
                    case PREF_APN_ID_LEN /*5*/:
                    case CharacterSets.ISO_8859_3 /*6*/:
                        if (!CUST_RETRY_CONFIG || !"mms".equals(apnContext.getApnType())) {
                            trySetup = VDBG;
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            break;
                        }
                        log("applyNewState: the mms is retrying,return.");
                        return;
                        break;
                }
            } else if (met) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DISABLED);
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            }
        } else if (enabled && met) {
            if (apnContext.isEnabled()) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_MET);
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
            }
            if (apnContext.getState() == State.FAILED) {
                apnContext.setState(State.IDLE);
            }
            trySetup = VDBG;
        }
        apnContext.setEnabled(enabled);
        apnContext.setDependencyMet(met);
        if (cleanup) {
            cleanUpConnection(VDBG, apnContext);
            if ("default".equals(apnContext.getApnType())) {
                log("applyNewState disable default apncontext, need to reset all param");
                clearRestartRildParam();
            }
        }
        if (trySetup) {
            apnContext.resetErrorCodeRetries();
            trySetupData(apnContext);
        }
    }

    private DcAsyncChannel checkForCompatibleConnectedApnContext(ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        ApnSetting dunSetting = null;
        ApnSetting bipSetting = null;
        if ("dun".equals(apnType)) {
            dunSetting = fetchDunApn();
        }
        if (isBipApnType(apnType)) {
            bipSetting = fetchBipApn(this.mPreferredApn, this.mAllApnSettings);
        }
        log("checkForCompatibleConnectedApnContext: apnContext=" + apnContext);
        DcAsyncChannel potentialDcac = null;
        Object potentialApnCtx = null;
        for (ApnContext curApnCtx : this.mApnContexts.values()) {
            DcAsyncChannel curDcac = curApnCtx.getDcAc();
            if (curDcac != null) {
                ApnSetting apnSetting = curApnCtx.getApnSetting();
                log("apnSetting: " + apnSetting);
                if (dunSetting == null) {
                    if (bipSetting == null) {
                        if (apnSetting != null && ((apnContext.getWaitingApns() == null && apnSetting.canHandleType(apnType)) || (apnContext.getWaitingApns() != null && apnContext.getWaitingApns().contains(apnSetting)))) {
                            switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[curApnCtx.getState().ordinal()]) {
                                case VP_START /*1*/:
                                    log("checkForCompatibleConnectedApnContext: found canHandle conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                    return curDcac;
                                case GSM_ROAMING_CARD1 /*2*/:
                                case CharacterSets.ISO_8859_3 /*6*/:
                                    potentialDcac = curDcac;
                                    potentialApnCtx = curApnCtx;
                                    break;
                                case GSM_ROAMING_CARD2 /*3*/:
                                    if (potentialDcac != null) {
                                        break;
                                    }
                                    potentialDcac = curDcac;
                                    potentialApnCtx = curApnCtx;
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (bipSetting.equals(apnSetting)) {
                        switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[curApnCtx.getState().ordinal()]) {
                            case VP_START /*1*/:
                                log("checkForCompatibleConnectedApnContext: found bip conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                return curDcac;
                            case GSM_ROAMING_CARD1 /*2*/:
                            case CharacterSets.ISO_8859_3 /*6*/:
                                potentialDcac = curDcac;
                                potentialApnCtx = curApnCtx;
                                break;
                            default:
                                break;
                        }
                    } else {
                        continue;
                    }
                } else if (dunSetting.equals(apnSetting)) {
                    switch (-getcom-android-internal-telephony-DctConstants$StateSwitchesValues()[curApnCtx.getState().ordinal()]) {
                        case VP_START /*1*/:
                            log("checkForCompatibleConnectedApnContext: found dun conn=" + curDcac + " curApnCtx=" + curApnCtx);
                            return curDcac;
                        case GSM_ROAMING_CARD1 /*2*/:
                        case CharacterSets.ISO_8859_3 /*6*/:
                            potentialDcac = curDcac;
                            potentialApnCtx = curApnCtx;
                            break;
                        case GSM_ROAMING_CARD2 /*3*/:
                            if (potentialDcac != null) {
                                break;
                            }
                            potentialDcac = curDcac;
                            potentialApnCtx = curApnCtx;
                            break;
                        default:
                            break;
                    }
                } else {
                    continue;
                }
            } else {
                log("checkForCompatibleConnectedApnContext: not conn curApnCtx=" + curApnCtx);
            }
        }
        if (potentialDcac != null) {
            log("checkForCompatibleConnectedApnContext: found potential conn=" + potentialDcac + " curApnCtx=" + potentialApnCtx);
            return potentialDcac;
        }
        log("checkForCompatibleConnectedApnContext: NO conn apnContext=" + apnContext);
        return null;
    }

    public void setEnabled(int id, boolean enable) {
        Message msg = obtainMessage(270349);
        msg.arg1 = id;
        msg.arg2 = enable ? VP_START : VP_END;
        sendMessage(msg);
    }

    private void onEnableApn(int apnId, int enabled) {
        boolean z = VDBG;
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext == null) {
            loge("onEnableApn(" + apnId + ", " + enabled + "): NO ApnContext");
            return;
        }
        log("onEnableApn: apnContext=" + apnContext + " call applyNewState");
        if (enabled != VP_START) {
            z = VOWIFI_CONFIG;
        }
        applyNewState(apnContext, z, apnContext.getDependencyMet());
    }

    protected boolean onTrySetupData(String reason) {
        log("onTrySetupData: reason=" + reason);
        setupDataOnConnectableApns(reason);
        return VDBG;
    }

    protected boolean onTrySetupData(ApnContext apnContext) {
        log("onTrySetupData: apnContext=" + apnContext);
        return trySetupData(apnContext);
    }

    public boolean getDataEnabled() {
        int i = VP_START;
        int device_provisioned = Global.getInt(this.mResolver, "device_provisioned", VP_END);
        boolean retVal = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true"));
        if (TelephonyManager.getDefault().getSimCount() == VP_START) {
            int i2;
            ContentResolver contentResolver = this.mResolver;
            String str = "mobile_data";
            if (retVal) {
                i2 = VP_START;
            } else {
                i2 = VP_END;
            }
            retVal = Global.getInt(contentResolver, str, i2) != 0 ? VDBG : VOWIFI_CONFIG;
        } else {
            int phoneSubId = this.mPhone.getSubId();
            try {
                retVal = VSimUtilsInner.isVSimSub(phoneSubId) ? this.mUserDataEnabled : TelephonyManager.getIntWithSubId(this.mResolver, "mobile_data", phoneSubId) != 0 ? VDBG : VOWIFI_CONFIG;
            } catch (SettingNotFoundException e) {
            }
        }
        log("getDataEnabled: retVal=" + retVal);
        if (device_provisioned == 0) {
            String prov_property = SystemProperties.get("ro.com.android.prov_mobiledata", retVal ? "true" : "false");
            retVal = "true".equalsIgnoreCase(prov_property);
            ContentResolver contentResolver2 = this.mResolver;
            String str2 = "device_provisioning_mobile_data";
            if (!retVal) {
                i = VP_END;
            }
            int prov_mobile_data = Global.getInt(contentResolver2, str2, i);
            retVal = prov_mobile_data != 0 ? VDBG : VOWIFI_CONFIG;
            log("getDataEnabled during provisioning retVal=" + retVal + " - (" + prov_property + ", " + prov_mobile_data + ")");
        }
        return retVal;
    }

    public void setDataOnRoamingEnabled(boolean enabled) {
        int phoneSubId = this.mPhone.getSubId();
        if (getDataOnRoamingEnabled() != enabled) {
            int roaming = enabled ? VP_START : VP_END;
            if (TelephonyManager.getDefault().getSimCount() == VP_START) {
                Global.putInt(this.mResolver, "data_roaming", roaming);
            } else {
                Global.putInt(this.mResolver, getDataRoamingSettingItem("data_roaming"), roaming);
            }
            this.mSubscriptionManager.setDataRoaming(roaming, phoneSubId);
            log("setDataOnRoamingEnabled: set phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
            return;
        }
        log("setDataOnRoamingEnabled: unchanged phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
    }

    public boolean getDataOnRoamingEnabled() {
        int i = VP_START;
        boolean isDataRoamingEnabled = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false"));
        int phoneSubId = this.mPhone.getSubId();
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId()) || HuaweiTelephonyConfigs.isChinaTelecom()) {
            return VDBG;
        }
        try {
            if (isNeedDataRoamingExpend()) {
                isDataRoamingEnabled = getDataRoamingEnabledWithNational();
            } else if (TelephonyManager.getDefault().getSimCount() == VP_START) {
                ContentResolver contentResolver = this.mResolver;
                String str = "data_roaming";
                if (!isDataRoamingEnabled) {
                    i = VP_END;
                }
                isDataRoamingEnabled = Global.getInt(contentResolver, str, i) != 0 ? VDBG : VOWIFI_CONFIG;
            } else {
                isDataRoamingEnabled = Global.getInt(this.mResolver, getDataRoamingSettingItem("data_roaming")) != 0 ? VDBG : VOWIFI_CONFIG;
            }
        } catch (SettingNotFoundException snfe) {
            log("getDataOnRoamingEnabled: SettingNofFoundException snfe=" + snfe);
        }
        log("getDataOnRoamingEnabled: phoneSubId=" + phoneSubId + " isDataRoamingEnabled=" + isDataRoamingEnabled);
        return isDataRoamingEnabled;
    }

    private void onRoamingOff() {
        log("onRoamingOff");
        if (this.mUserDataEnabled) {
            if (getDataOnRoamingEnabled()) {
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_OFF);
            } else {
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_OFF);
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_OFF);
            }
        }
    }

    private void onRoamingOn() {
        log("onRoamingOn");
        if (!this.mUserDataEnabled) {
            log("data not enabled by user");
        } else if (this.mPhone.getServiceState().getDataRoaming()) {
            if (getDataOnRoamingEnabled()) {
                log("onRoamingOn: setup data on roaming");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_ON);
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_ON);
            } else {
                log("onRoamingOn: Tear down data connection on roaming.");
                cleanUpAllConnections((boolean) VDBG, PhoneInternalInterface.REASON_ROAMING_ON);
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
            }
        } else {
            log("device is not roaming. ignored the request.");
        }
    }

    private void onRadioAvailable() {
        log("onRadioAvailable");
        if (this.mPhone.getSimulatedRadioControl() != null) {
            notifyDataConnection(null);
            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null && (r.getImsiReady() || r.getRecordsLoaded())) {
            notifyOffApnsOfAvailability(null);
        }
        if (getOverallState() != State.IDLE) {
            cleanUpConnection(VDBG, null);
        }
    }

    private void onRadioOffOrNotAvailable() {
        this.mReregisterOnReconnectFailure = VOWIFI_CONFIG;
        this.mAutoAttachOnCreation.set(VOWIFI_CONFIG);
        this.mIsPsRestricted = VOWIFI_CONFIG;
        this.mPhone.getServiceStateTracker().mRestrictedState.setPsRestricted(VOWIFI_CONFIG);
        if (this.mPhone.getSimulatedRadioControl() != null) {
            log("We're on the simulator; assuming radio off is meaningless");
        } else {
            log("onRadioOffOrNotAvailable: is off and clean up all connections");
            cleanUpAllConnections((boolean) VOWIFI_CONFIG, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        }
        notifyOffApnsOfAvailability(null);
    }

    private void completeConnection(ApnContext apnContext) {
        log("completeConnection: successful, notify the world apnContext=" + apnContext);
        if (this.mIsProvisioning && !TextUtils.isEmpty(this.mProvisioningUrl)) {
            log("completeConnection: MOBILE_PROVISIONING_ACTION url=" + this.mProvisioningUrl);
            Intent newIntent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
            newIntent.setData(Uri.parse(this.mProvisioningUrl));
            newIntent.setFlags(272629760);
            try {
                this.mPhone.getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                loge("completeConnection: startActivityAsUser failed" + e);
            }
        }
        this.mIsProvisioning = VOWIFI_CONFIG;
        this.mProvisioningUrl = null;
        if (this.mProvisioningSpinner != null) {
            sendMessage(obtainMessage(270378, this.mProvisioningSpinner));
        }
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        startNetStatPoll();
        startDataStallAlarm(VOWIFI_CONFIG);
    }

    private boolean needSetCTProxy(ApnSetting apn) {
        boolean needSet = VOWIFI_CONFIG;
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            return VOWIFI_CONFIG;
        }
        String networkOperatorNumeric = this.mPhone.getServiceState().getOperatorNumeric();
        if (!(apn == null || apn.apn == null || !apn.apn.contains(CT_NOT_ROAMING_APN_PREFIX) || networkOperatorNumeric == null || !"46012".equals(networkOperatorNumeric))) {
            needSet = VDBG;
        }
        return needSet;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onDataSetupComplete(AsyncResult ar) {
        DcFailCause cause = DcFailCause.UNKNOWN;
        boolean handleError = VOWIFI_CONFIG;
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupComplete");
        if (apnContext != null) {
            boolean isDefault = "default".equals(apnContext.getApnType());
            ApnSetting apn;
            if (ar.exception == null) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac == null) {
                    log("onDataSetupComplete: no connection to DC, handle as error");
                    cause = DcFailCause.CONNECTION_TO_DATACONNECTIONAC_BROKEN;
                    handleError = VDBG;
                } else {
                    addIfacePhoneHashMap(dcac, mIfacePhoneHashMap);
                    if (isClearCodeEnabled() && isDefault) {
                        resetTryTimes();
                    }
                    apn = apnContext.getApnSetting();
                    log("onDataSetupComplete: success apn=" + (apn == null ? "unknown" : apn.apn));
                    if (isDefault) {
                        SystemProperties.set("gsm.default.apn", apn == null ? "" : apn.apn);
                        log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
                    }
                    if (needSetCTProxy(apn)) {
                        try {
                            dcac.setLinkPropertiesHttpProxySync(new ProxyInfo("10.0.0.200", Integer.parseInt("80"), "127.0.0.1"));
                        } catch (NumberFormatException e) {
                            loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.apn + "): " + e);
                        }
                    } else if (!(apn == null || apn.proxy == null)) {
                        if (apn.proxy.length() != 0) {
                            try {
                                String port = apn.port;
                                if (TextUtils.isEmpty(port)) {
                                    port = "8080";
                                }
                                dcac.setLinkPropertiesHttpProxySync(new ProxyInfo(apn.proxy, Integer.parseInt(port), "127.0.0.1"));
                            } catch (NumberFormatException e2) {
                                loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.port + "): " + e2);
                            }
                        }
                    }
                    if (TextUtils.equals(apnContext.getApnType(), "default")) {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "true");
                        } catch (RuntimeException e3) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to true");
                        }
                        if (this.mCanSetPreferApn && this.mPreferredApn == null) {
                            log("onDataSetupComplete: PREFERRED APN is null");
                            this.mPreferredApn = apn;
                            if (this.mPreferredApn != null) {
                                setPreferredApn(this.mPreferredApn.id);
                            }
                        }
                    } else {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                        } catch (RuntimeException e4) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                        }
                    }
                    apnContext.setPdpFailCause(DcFailCause.NONE);
                    apnContext.setState(State.CONNECTED);
                    boolean isProvApn = apnContext.isProvisioningApn();
                    ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                    if (this.mProvisionBroadcastReceiver != null) {
                        this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
                        this.mProvisionBroadcastReceiver = null;
                    }
                    if (!isProvApn || this.mIsProvisioning) {
                        cm.setProvisioningNotificationVisible(VOWIFI_CONFIG, VP_END, this.mProvisionActionName);
                        completeConnection(apnContext);
                    } else {
                        log("onDataSetupComplete: successful, BUT send connected to prov apn as mIsProvisioning:" + this.mIsProvisioning + " == false" + " && (isProvisioningApn:" + isProvApn + " == true");
                        this.mProvisionBroadcastReceiver = new ProvisionNotificationBroadcastReceiver(this, cm.getMobileProvisioningUrl(), TelephonyManager.getDefault().getNetworkOperatorName());
                        this.mPhone.getContext().registerReceiver(this.mProvisionBroadcastReceiver, new IntentFilter(this.mProvisionActionName));
                        cm.setProvisioningNotificationVisible(VDBG, VP_END, this.mProvisionActionName);
                        setRadio(VOWIFI_CONFIG);
                    }
                    log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType() + ", reason:" + apnContext.getReason());
                    clearRestartRildParam();
                    setFirstTimeEnableData();
                    log("CHR inform CHR the APN info when data setup succ");
                    LinkProperties chrLinkProperties = getLinkProperties(apnContext.getApnType());
                    if (chrLinkProperties != null) {
                        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDataConnected(this.mPhone, apn, chrLinkProperties);
                    }
                }
            } else {
                cause = ar.result;
                apn = apnContext.getApnSetting();
                String str = "onDataSetupComplete: error apn=%s cause=%s";
                Object[] objArr = new Object[GSM_ROAMING_CARD1];
                objArr[VP_END] = apn == null ? "unknown" : apn.apn;
                objArr[VP_START] = cause;
                log(String.format(str, objArr));
                sendDSMipErrorBroadcast();
                if (cause.isEventLoggable()) {
                    int cid = getCellLocationId();
                    Integer[] numArr = new Object[GSM_ROAMING_CARD2];
                    numArr[VP_END] = Integer.valueOf(cause.ordinal());
                    numArr[VP_START] = Integer.valueOf(cid);
                    numArr[GSM_ROAMING_CARD1] = Integer.valueOf(TelephonyManager.getDefault().getNetworkType());
                    EventLog.writeEvent(EventLogTags.PDP_SETUP_FAIL, numArr);
                }
                apn = apnContext.getApnSetting();
                this.mPhone.notifyPreciseDataConnectionFailed(apnContext.getReason(), apnContext.getApnType(), apn != null ? apn.apn : "unknown", cause.toString());
                Intent intent = new Intent("android.intent.action.REQUEST_NETWORK_FAILED");
                intent.putExtra(ERROR_CODE_KEY, cause.getErrorCode());
                intent.putExtra(APN_TYPE_KEY, apnContext.getApnType());
                notifyCarrierAppWithIntent(intent);
                if (!cause.isRestartRadioFail()) {
                }
                log("Modem restarted.");
                sendRestartRadio();
                if (isClearCodeEnabled()) {
                    long delay = apnContext.getDelayForNextApn(this.mFailFast);
                    Rlog.d(LOG_TAG, "clearcode onDataSetupComplete delay=" + delay);
                    operateClearCodeProcess(apnContext, cause, (int) delay);
                } else if (isPermanentFail(cause)) {
                    log("cause = " + cause + ", mark apn as permanent failed. apn = " + apn);
                    apnContext.markApnPermanentFailed(apn);
                }
                handleError = VDBG;
            }
            if (handleError) {
                onDataSetupCompleteError(ar);
            }
            if (!this.mInternalDataEnabled) {
                cleanUpAllConnections(null);
            }
        }
    }

    private ApnContext getValidApnContext(AsyncResult ar, String logString) {
        if (ar != null && (ar.userObj instanceof Pair)) {
            Pair<ApnContext, Integer> pair = ar.userObj;
            ApnContext apnContext = pair.first;
            if (apnContext != null) {
                int generation = apnContext.getConnectionGeneration();
                log("getValidApnContext (" + logString + ") on " + apnContext + " got " + generation + " vs " + pair.second);
                if (generation == ((Integer) pair.second).intValue()) {
                    return apnContext;
                }
                log("ignoring obsolete " + logString);
                return null;
            }
        }
        throw new RuntimeException(logString + ": No apnContext");
    }

    private void onDataSetupCompleteError(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupCompleteError");
        if (apnContext != null) {
            if (apnContext.isLastApnSetting()) {
                onAllApnFirstActiveFailed();
            }
            if (HwTelephonyFactory.getHwPhoneManager().isSupportOrangeApn(this.mPhone)) {
                HwTelephonyFactory.getHwPhoneManager().addSpecialAPN(this.mPhone);
                Rlog.d(LOG_TAG, "onDataSetupCompleteError.addSpecialAPN()");
            }
            long delay = apnContext.getDelayForNextApn(this.mFailFast);
            if (delay >= 0) {
                log("onDataSetupCompleteError: Try next APN. delay = " + delay);
                if (isClearCodeEnabled()) {
                    setCurFailCause(ar);
                }
                apnContext.setState(State.SCANNING);
                if (isClearCodeEnabled()) {
                    delay = (long) getDelayTime();
                }
                startAlarmForReconnect(delay, apnContext);
            } else {
                onAllApnPermActiveFailed();
                apnContext.setState(State.FAILED);
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_APN_FAILED, apnContext.getApnType());
                apnContext.setDataConnectionAc(null);
                log("onDataSetupCompleteError: Stop retrying APNs.");
            }
        }
    }

    private String[] getActivationAppName() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfig();
        }
        if (b != null) {
            return b.getStringArray("sim_state_detection_carrier_app_string_array");
        }
        return CarrierConfigManager.getDefaultConfig().getStringArray("sim_state_detection_carrier_app_string_array");
    }

    private void onDataConnectionRedirected(String redirectUrl, HashMap<ApnContext, ConnectionParams> apnContextMap) {
        if (!TextUtils.isEmpty(redirectUrl)) {
            this.mRedirectUrl = redirectUrl;
            Intent intent = new Intent("android.intent.action.REDIRECTION_DETECTED");
            intent.putExtra(REDIRECTION_URL_KEY, redirectUrl);
            if (!isColdSimDetected() && !isOutOfCreditSimDetected() && checkCarrierAppAvailable(intent)) {
                log("Starting Activation Carrier app with redirectUrl : " + redirectUrl);
                for (ApnContext context : apnContextMap.keySet()) {
                    cleanUpConnection(VDBG, context);
                    this.redirectApnContextSet.add(context);
                }
            }
        }
    }

    private void onDisconnectDone(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDone");
        if (apnContext != null) {
            if (apnContext.getState() == State.CONNECTING) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (!(dcac == null || dcac.isInactiveSync() || !dcac.checkApnContextSync(apnContext))) {
                    loge("onDisconnectDone: apnContext is activating, ignore " + apnContext);
                    return;
                }
            }
            log("onDisconnectDone: EVENT_DISCONNECT_DONE apnContext=" + apnContext);
            apnContext.setState(State.IDLE);
            if ("default".equals(apnContext.getApnType())) {
                SystemProperties.set("gsm.default.apn", "");
                log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
            }
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            if (this.mCdmaPsRecoveryEnabled && getOverallState() != State.CONNECTED) {
                stopPdpResetAlarm();
            }
            if (isDisconnected()) {
                if (this.mPhone.getServiceStateTracker().processPendingRadioPowerOffAfterDataOff()) {
                    log("onDisconnectDone: radio will be turned off, no retries");
                    apnContext.setApnSetting(null);
                    apnContext.setDataConnectionAc(null);
                    if (this.mDisconnectPendingCount > 0) {
                        this.mDisconnectPendingCount--;
                    }
                    if (this.mDisconnectPendingCount == 0) {
                        notifyDataDisconnectComplete();
                        notifyAllDataDisconnected();
                        notifyCarrierAppForRedirection();
                    }
                    return;
                }
                log("data is disconnected and check if need to setPreferredNetworkType");
                ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
                if (sst != null) {
                    HwTelephonyFactory.getHwNetworkManager().checkAndSetNetworkType(sst, this.mPhone);
                }
            }
            if (this.mAttached.get() && apnContext.isReady() && retryAfterDisconnected(apnContext)) {
                try {
                    SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                } catch (RuntimeException e) {
                    log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                }
                log("onDisconnectDone: attached, ready and retry after disconnect");
                long delay = apnContext.getInterApnDelay(this.mFailFast);
                if (delay > 0) {
                    startAlarmForReconnect(delay, apnContext);
                }
            } else {
                boolean restartRadioAfterProvisioning = this.mPhone.getContext().getResources().getBoolean(17956992);
                if (apnContext.isProvisioningApn() && restartRadioAfterProvisioning) {
                    log("onDisconnectDone: restartRadio after provisioning");
                    restartRadio();
                }
                apnContext.setApnSetting(null);
                apnContext.setDataConnectionAc(null);
                if (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
                    log("onDisconnectDone: isOnlySigneDcAllowed true so setup single apn");
                    if (AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(apnContext.getReason())) {
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION, apnContext.getApnType());
                    } else {
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
                    }
                } else if (this.mCdmaPsRecoveryEnabled && this.mPhone.getServiceState().getVoiceRegState() == 0 && retryAfterDisconnected(apnContext)) {
                    log("onDisconnectDone: cdma cs attached, retry after disconnect");
                    startAlarmForReconnect(5000, apnContext);
                } else {
                    log("onDisconnectDone: not retrying");
                }
            }
            if (this.mDisconnectPendingCount > 0) {
                this.mDisconnectPendingCount--;
            }
            if (this.mDisconnectPendingCount == 0) {
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                notifyDataDisconnectComplete();
                notifyAllDataDisconnected();
                notifyCarrierAppForRedirection();
            }
        }
    }

    private void onDisconnectDcRetrying(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDcRetrying");
        if (apnContext != null) {
            apnContext.setState(State.RETRYING);
            log("onDisconnectDcRetrying: apnContext=" + apnContext);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        }
    }

    private void onVoiceCallStarted() {
        log("onVoiceCallStarted");
        this.mInVoiceCall = VDBG;
        if (isConnected() && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_STARTED);
        }
    }

    private void onVoiceCallEnded() {
        log("onVoiceCallEnded");
        this.mInVoiceCall = VOWIFI_CONFIG;
        if (isConnected()) {
            if (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                resetPollStats();
            } else {
                startNetStatPoll();
                startDataStallAlarm(VOWIFI_CONFIG);
                notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
            }
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
    }

    protected void onVpStatusChanged(AsyncResult ar) {
        log("onVpStatusChanged");
        if (ar.exception != null) {
            log("Exception occurred, failed to report the rssi and ecio.");
            return;
        }
        byte[] vp_status = ar.result;
        if (vp_status == null || VP_START != vp_status.length) {
            log("Error occurred, ReportVpStatus was incorrect");
        } else {
            this.mVpStatus = vp_status[VP_END];
            log("onVpStatusChanged, mVpStatus:" + this.mVpStatus);
        }
        if (VP_START == this.mVpStatus) {
            onVPStarted();
        } else {
            onVPEnded();
        }
    }

    public void onVPStarted() {
        log("onVPStarted");
        this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(VOWIFI_CONFIG);
        if (isConnected() && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() && this.mInVoiceCall) {
            log("onVPStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(PhoneInternalInterface.REASON_VP_STARTED);
        }
    }

    public void onVPEnded() {
        boolean z = VOWIFI_CONFIG;
        log("onVPEnded");
        if (!this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(VDBG);
            if (isConnected() && this.mInVoiceCall) {
                startNetStatPoll();
                startDataStallAlarm(VOWIFI_CONFIG);
                synchronized (this.mDataEnabledLock) {
                    if (this.mInternalDataEnabled && this.mUserDataEnabled) {
                        z = sPolicyDataEnabled;
                    }
                    if (z) {
                        notifyDataConnection(PhoneInternalInterface.REASON_VP_ENDED);
                    } else {
                        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
                    }
                }
            }
        }
    }

    private void onCleanUpConnection(boolean tearDown, int apnId, String reason) {
        log("onCleanUpConnection");
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext != null) {
            apnContext.setReason(reason);
            cleanUpConnection(tearDown, apnContext);
        }
    }

    private boolean isConnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == State.CONNECTED) {
                return VDBG;
            }
        }
        return VOWIFI_CONFIG;
    }

    public boolean isDisconnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!apnContext.isDisconnected()) {
                return VOWIFI_CONFIG;
            }
        }
        return VDBG;
    }

    private void notifyDataConnection(String reason) {
        log("notifyDataConnection: reason=" + reason);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                log("notifyDataConnection: type:" + apnContext.getApnType());
                this.mPhone.notifyDataConnection(reason != null ? reason : apnContext.getReason(), apnContext.getApnType());
            }
        }
        notifyOffApnsOfAvailability(reason);
    }

    private void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        if (this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
            ArrayList<DataProfile> dps = new ArrayList();
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.modemCognitive) {
                    DataProfile dp = new DataProfile(apn, this.mPhone.getServiceState().getDataRoaming());
                    boolean isDup = VOWIFI_CONFIG;
                    for (DataProfile dpIn : dps) {
                        if (dp.equals(dpIn)) {
                            isDup = VDBG;
                            break;
                        }
                    }
                    if (!isDup) {
                        dps.add(dp);
                    }
                }
            }
            if (dps.size() > 0) {
                this.mPhone.mCi.setDataProfile((DataProfile[]) dps.toArray(new DataProfile[VP_END]), null);
            }
        }
    }

    public String getOperatorNumeric() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (operator == null) {
            operator = "";
        }
        log("getOperatorNumberic - returning from card: " + operator);
        return operator;
    }

    public String getCTOperator(String operator) {
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            return operator;
        }
        operator = SystemProperties.get("gsm.national_roaming.apn", "46003");
        log("Select china telecom hplmn: " + operator);
        return operator;
    }

    private void createAllApnList() {
        this.mMvnoMatched = VOWIFI_CONFIG;
        this.mAllApnSettings = new ArrayList();
        String operator = getCTOperator(getOperatorNumeric());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(this.mPhone.getSubId()))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mPhone.getSubId()));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            Cursor cursor;
            String selection = "numeric = '" + operator + "'";
            String orderBy = HbpcdLookup.ID;
            if (SystemProperties.getBoolean("ro.config.reverse_mms_apn", VOWIFI_CONFIG)) {
                orderBy = "_id DESC";
            }
            log("createAllApnList: selection=" + selection);
            String subId = Long.toString((long) this.mPhone.getSubId());
            if (this.isMultiSimEnabled) {
                cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, subId), null, selection, null, orderBy);
            } else {
                cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, orderBy);
            }
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mAllApnSettings = createApnList(cursor);
                }
                cursor.close();
            }
            if (this.mAllApnSettings.isEmpty() && !VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnListEmpty(this.mPhone.getSubId());
            }
        }
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) && this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: vsim enabled and apn not in database");
            this.mAllApnSettings = VSimUtilsInner.createVSimApnList();
            if (VSimUtilsInner.isVSimOn()) {
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnListEmpty(this.mPhone.getSubId());
            }
        }
        addEmergencyApnSetting();
        if (this.dedupeApn) {
            dedupeApnSettings();
        }
        if (this.mAllApnSettings.isEmpty() || VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            log("createAllApnList: No APN found for carrier: " + operator);
            this.mPreferredApn = null;
        } else {
            this.mPreferredApn = getPreferredApn();
            if (!(this.mPreferredApn == null || this.mPreferredApn.numeric.equals(operator))) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
        setDataProfilesAsNeeded();
    }

    public ArrayList<ApnSetting> getAllApnList() {
        return this.mAllApnSettings;
    }

    private void dedupeApnSettings() {
        ArrayList<ApnSetting> resultApns = new ArrayList();
        for (int i = VP_END; i < this.mAllApnSettings.size() - 1; i += VP_START) {
            ApnSetting first = (ApnSetting) this.mAllApnSettings.get(i);
            int j = i + VP_START;
            while (j < this.mAllApnSettings.size()) {
                ApnSetting second = (ApnSetting) this.mAllApnSettings.get(j);
                if (apnsSimilar(first, second)) {
                    ApnSetting newApn = mergeApns(first, second);
                    this.mAllApnSettings.set(i, newApn);
                    first = newApn;
                    this.mAllApnSettings.remove(j);
                } else {
                    j += VP_START;
                }
            }
        }
    }

    private boolean apnTypeSameAny(ApnSetting first, ApnSetting second) {
        int index1;
        StringBuilder apnType1 = new StringBuilder(first.apn + ": ");
        for (index1 = VP_END; index1 < first.types.length; index1 += VP_START) {
            apnType1.append(first.types[index1]);
            apnType1.append(",");
        }
        StringBuilder apnType2 = new StringBuilder(second.apn + ": ");
        for (index1 = VP_END; index1 < second.types.length; index1 += VP_START) {
            apnType2.append(second.types[index1]);
            apnType2.append(",");
        }
        log("APN1: is " + apnType1);
        log("APN2: is " + apnType2);
        index1 = VP_END;
        while (index1 < first.types.length) {
            int index2 = VP_END;
            while (index2 < second.types.length) {
                if (first.types[index1].equals(CharacterSets.MIMENAME_ANY_CHARSET) || second.types[index2].equals(CharacterSets.MIMENAME_ANY_CHARSET) || first.types[index1].equals(second.types[index2])) {
                    log("apnTypeSameAny: return true");
                    return VDBG;
                }
                index2 += VP_START;
            }
            index1 += VP_START;
        }
        log("apnTypeSameAny: return false");
        return VOWIFI_CONFIG;
    }

    private boolean apnsSimilar(ApnSetting first, ApnSetting second) {
        if (!first.canHandleType("dun") && !second.canHandleType("dun") && Objects.equals(first.apn, second.apn) && !apnTypeSameAny(first, second) && xorEquals(first.proxy, second.proxy) && xorEquals(first.port, second.port) && first.carrierEnabled == second.carrierEnabled && first.bearerBitmask == second.bearerBitmask && first.profileId == second.profileId && Objects.equals(first.mvnoType, second.mvnoType) && Objects.equals(first.mvnoMatchData, second.mvnoMatchData) && xorEquals(first.mmsc, second.mmsc) && xorEquals(first.mmsProxy, second.mmsProxy)) {
            return xorEquals(first.mmsPort, second.mmsPort);
        }
        return VOWIFI_CONFIG;
    }

    private boolean xorEquals(String first, String second) {
        if (Objects.equals(first, second) || TextUtils.isEmpty(first)) {
            return VDBG;
        }
        return TextUtils.isEmpty(second);
    }

    private ApnSetting mergeApns(ApnSetting dest, ApnSetting src) {
        String roamingProtocol;
        int id = dest.id;
        ArrayList<String> resultTypes = new ArrayList();
        resultTypes.addAll(Arrays.asList(dest.types));
        String[] strArr = src.types;
        int length = strArr.length;
        for (int i = VP_END; i < length; i += VP_START) {
            String srcType = strArr[i];
            if (!resultTypes.contains(srcType)) {
                resultTypes.add(srcType);
            }
            if (srcType.equals("default")) {
                id = src.id;
            }
        }
        String mmsc = TextUtils.isEmpty(dest.mmsc) ? src.mmsc : dest.mmsc;
        String mmsProxy = TextUtils.isEmpty(dest.mmsProxy) ? src.mmsProxy : dest.mmsProxy;
        String mmsPort = TextUtils.isEmpty(dest.mmsPort) ? src.mmsPort : dest.mmsPort;
        String proxy = TextUtils.isEmpty(dest.proxy) ? src.proxy : dest.proxy;
        String port = TextUtils.isEmpty(dest.port) ? src.port : dest.port;
        String protocol = src.protocol.equals("IPV4V6") ? src.protocol : dest.protocol;
        if (src.roamingProtocol.equals("IPV4V6")) {
            roamingProtocol = src.roamingProtocol;
        } else {
            roamingProtocol = dest.roamingProtocol;
        }
        int bearerBitmask = (dest.bearerBitmask == 0 || src.bearerBitmask == 0) ? VP_END : dest.bearerBitmask | src.bearerBitmask;
        return new ApnSetting(id, dest.numeric, dest.carrier, dest.apn, proxy, port, mmsc, mmsProxy, mmsPort, dest.user, dest.password, dest.authType, (String[]) resultTypes.toArray(new String[VP_END]), protocol, roamingProtocol, dest.carrierEnabled, VP_END, bearerBitmask, dest.profileId, !dest.modemCognitive ? src.modemCognitive : VDBG, dest.maxConns, dest.waitTime, dest.maxConnsTime, dest.mtu, dest.mvnoType, dest.mvnoMatchData);
    }

    private DcAsyncChannel createDataConnection() {
        log("createDataConnection E");
        int id = this.mUniqueIdGenerator.getAndIncrement();
        DataConnection conn = DataConnection.makeDataConnection(this.mPhone, id, this, this.mDcTesterFailBringUpAll, this.mDcc);
        this.mDataConnections.put(Integer.valueOf(id), conn);
        DcAsyncChannel dcac = new DcAsyncChannel(conn, LOG_TAG);
        int status = dcac.fullyConnectSync(this.mPhone.getContext(), this, conn.getHandler());
        if (status == 0) {
            this.mDataConnectionAcHashMap.put(Integer.valueOf(dcac.getDataConnectionIdSync()), dcac);
        } else {
            loge("createDataConnection: Could not connect to dcac=" + dcac + " status=" + status);
        }
        log("createDataConnection() X id=" + id + " dc=" + conn);
        return dcac;
    }

    private void destroyDataConnections() {
        if (this.mDataConnections != null) {
            log("destroyDataConnections: clear mDataConnectionList");
            this.mDataConnections.clear();
            return;
        }
        log("destroyDataConnections: mDataConnecitonList is empty, ignore");
    }

    private ArrayList<ApnSetting> buildWaitingApns(String requestedApnType, int radioTech) {
        boolean usePreferred;
        log("buildWaitingApns: E requestedApnType=" + requestedApnType);
        ArrayList<ApnSetting> apnList = new ArrayList();
        if (requestedApnType.equals("dun")) {
            ApnSetting dun = fetchDunApn();
            if (dun != null) {
                apnList.add(dun);
                log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList);
                return apnList;
            }
        }
        if (isBipApnType(requestedApnType)) {
            ApnSetting bip = fetchBipApn(this.mPreferredApn, this.mAllApnSettings);
            if (bip != null) {
                apnList.add(bip);
                log("buildWaitingApns: X added APN_TYPE_BIP apnList=" + apnList);
                return apnList;
            }
        }
        if (CT_SUPL_FEATURE_ENABLE && "supl".equals(requestedApnType) && isCTSimCard(this.mPhone.getSubId())) {
            ArrayList<ApnSetting> suplApnList = buildWaitingApnsForCTSupl(requestedApnType, radioTech);
            if (!suplApnList.isEmpty()) {
                return suplApnList;
            }
        }
        String operator = getCTOperator(getOperatorNumeric());
        try {
            usePreferred = this.mPhone.getContext().getResources().getBoolean(17956991) ? VOWIFI_CONFIG : VDBG;
        } catch (NotFoundException e) {
            log("buildWaitingApns: usePreferred NotFoundException set to true");
            usePreferred = VDBG;
        }
        if (usePreferred) {
            this.mPreferredApn = getPreferredApn();
        }
        log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech);
        if (usePreferred && this.mCanSetPreferApn && this.mPreferredApn != null && this.mPreferredApn.canHandleType(requestedApnType)) {
            log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.numeric + ":" + this.mPreferredApn);
            if (this.mPreferredApn.numeric == null || !this.mPreferredApn.numeric.equals(operator)) {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            } else if (isCTSimCard(this.mPhone.getPhoneId()) && isLTENetwork() && isApnPreset(this.mPreferredApn)) {
                if (this.mPreferredApn.bearer == 13 || this.mPreferredApn.bearer == 14) {
                    apnList.add(this.mPreferredApn);
                    return apnList;
                }
            } else if (ServiceState.bitmaskHasTech(this.mPreferredApn.bearerBitmask, radioTech)) {
                apnList.add(this.mPreferredApn);
                log("buildWaitingApns: X added preferred apnList=" + apnList);
                return apnList;
            } else {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            }
        }
        String operatorCT = this.mPhone.getServiceState().getOperatorNumeric();
        if (operatorCT != null && !"".equals(operatorCT)) {
            if (!"46003".equals(operatorCT) && !"46011".equals(operatorCT)) {
                boolean isCTnetwork = "46012".equals(operatorCT);
            }
        }
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            loge("mAllApnSettings is null!");
        } else {
            log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
            for (ApnSetting apn : this.mAllApnSettings) {
                if (!apn.canHandleType(requestedApnType)) {
                    log("buildWaitingApns: couldn't handle requested ApnType=" + requestedApnType);
                } else if (isCTSimCard(this.mPhone.getPhoneId()) && isLTENetwork()) {
                    if (apn.bearer == 13 || apn.bearer == 14) {
                        log("buildWaitingApns: adding apn=" + apn.toString());
                        apnList.add(apn);
                    }
                } else if (ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech)) {
                    log("buildWaitingApns: adding apn=" + apn);
                    apnList.add(apn);
                } else {
                    log("buildWaitingApns: bearerBitmask:" + apn.bearerBitmask + " does " + "not include radioTech:" + radioTech);
                }
            }
        }
        log("buildWaitingApns: " + apnList.size() + " APNs in the list: " + apnList);
        return apnList;
    }

    private String apnListToString(ArrayList<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        int size = apns.size();
        for (int i = VP_END; i < size; i += VP_START) {
            result.append('[').append(((ApnSetting) apns.get(i)).toString()).append(']');
        }
        return result.toString();
    }

    private void setPreferredApn(int pos) {
        if (this.mCanSetPreferApn) {
            Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
            log("setPreferredApn: delete");
            ContentResolver resolver = this.mPhone.getContext().getContentResolver();
            resolver.delete(uri, null, null);
            if (pos >= 0) {
                log("setPreferredApn: insert");
                ContentValues values = new ContentValues();
                values.put(APN_ID, Integer.valueOf(pos));
                resolver.insert(uri, values);
            }
            return;
        }
        log("setPreferredApn: X !canSEtPreferApn");
    }

    private ApnSetting getPreferredApn() {
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            log("getPreferredApn: mAllApnSettings is " + (this.mAllApnSettings == null ? "null" : "empty"));
            return null;
        } else if (needRemovedPreferredApn()) {
            return null;
        } else {
            int count;
            Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
            ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
            String[] strArr = new String[GSM_ROAMING_CARD2];
            strArr[VP_END] = HbpcdLookup.ID;
            strArr[VP_START] = Part.NAME;
            strArr[GSM_ROAMING_CARD1] = TelephonyEventLog.DATA_KEY_APN;
            Cursor cursor = contentResolver.query(uri, strArr, null, null, GlobalMatchs.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                this.mCanSetPreferApn = VDBG;
            } else {
                this.mCanSetPreferApn = VOWIFI_CONFIG;
            }
            StringBuilder append = new StringBuilder().append("getPreferredApn: mRequestedApnType=").append(this.mRequestedApnType).append(" cursor=").append(cursor).append(" cursor.count=");
            if (cursor != null) {
                count = cursor.getCount();
            } else {
                count = VP_END;
            }
            log(append.append(count).toString());
            if (this.mCanSetPreferApn && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int pos = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
                for (ApnSetting p : this.mAllApnSettings) {
                    log("getPreferredApn: apnSetting=" + p);
                    if (p.id == pos && p.canHandleType(this.mRequestedApnType)) {
                        log("getPreferredApn: X found apnSetting" + p);
                        cursor.close();
                        return p;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            log("getPreferredApn: X not found");
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        log("handleMessage msg=" + msg);
        beforeHandleMessage(msg);
        ApnContext apnContext;
        int i;
        boolean enabled;
        Bundle bundle;
        switch (msg.what) {
            case 69636:
                log("DISCONNECTED_CONNECTED: msg=" + msg);
                DcAsyncChannel dcac = msg.obj;
                this.mDataConnectionAcHashMap.remove(Integer.valueOf(dcac.getDataConnectionIdSync()));
                dcac.disconnected();
                break;
            case 270336:
                onDataSetupComplete((AsyncResult) msg.obj);
                break;
            case 270337:
                break;
            case 270338:
                HwTelephonyFactory.getHwDataServiceChrManager().setReceivedSimloadedMsg(this.mPhone, VDBG, this.mApnContexts, this.mUserDataEnabled);
                int subId = this.mPhone.getSubId();
                if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                    log("Ignoring EVENT_RECORDS_LOADED as subId is not valid: " + subId);
                    break;
                } else {
                    onRecordsLoadedOrSubIdChanged();
                    break;
                }
            case 270339:
                if (!(msg.obj instanceof ApnContext)) {
                    if (!(msg.obj instanceof String)) {
                        loge("EVENT_TRY_SETUP request w/o apnContext or String");
                        break;
                    } else {
                        onTrySetupData((String) msg.obj);
                        break;
                    }
                }
                onTrySetupData((ApnContext) msg.obj);
                break;
            case 270340:
                break;
            case 270342:
                onRadioOffOrNotAvailable();
                break;
            case 270343:
                onVoiceCallStarted();
                break;
            case 270344:
                onVoiceCallEnded();
                if (mWcdmaVpEnabled) {
                    this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(VDBG);
                    break;
                }
                break;
            case 270345:
                onDataConnectionDetached();
                break;
            case 270347:
                onRoamingOn();
                break;
            case 270348:
                onRoamingOff();
                break;
            case 270349:
                onEnableApn(msg.arg1, msg.arg2);
                break;
            case 270351:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DONE msg=" + msg);
                onDisconnectDone((AsyncResult) msg.obj);
                break;
            case 270352:
                onDataConnectionAttached();
                break;
            case 270353:
                onDataStallAlarm(msg.arg1);
                break;
            case 270354:
                doRecovery();
                break;
            case 270355:
                if (isCTSimCard(this.mPhone.getPhoneId())) {
                    updateApnId();
                }
                onApnChanged();
                break;
            case 270358:
                log("EVENT_PS_RESTRICT_ENABLED " + this.mIsPsRestricted);
                stopNetStatPoll();
                stopDataStallAlarm();
                this.mIsPsRestricted = VDBG;
                break;
            case 270359:
                log("EVENT_PS_RESTRICT_DISABLED " + this.mIsPsRestricted);
                this.mIsPsRestricted = VOWIFI_CONFIG;
                if (isConnected()) {
                    startNetStatPoll();
                    startDataStallAlarm(VOWIFI_CONFIG);
                    break;
                }
                if (this.mState == State.FAILED) {
                    cleanUpAllConnections(VOWIFI_CONFIG, PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    this.mReregisterOnReconnectFailure = VOWIFI_CONFIG;
                }
                apnContext = (ApnContext) this.mApnContextsById.get(VP_END);
                if (apnContext != null) {
                    apnContext.setReason(PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    trySetupData(apnContext);
                    break;
                }
                loge("**** Default ApnContext not found ****");
                if (Build.IS_DEBUGGABLE) {
                    throw new RuntimeException("Default ApnContext not found");
                }
                break;
            case 270360:
                boolean tearDown = msg.arg1 == 0 ? VOWIFI_CONFIG : VDBG;
                log("EVENT_CLEAN_UP_CONNECTION tearDown=" + tearDown);
                if (!(msg.obj instanceof ApnContext)) {
                    onCleanUpConnection(tearDown, msg.arg2, (String) msg.obj);
                    break;
                } else {
                    cleanUpConnection(tearDown, (ApnContext) msg.obj);
                    break;
                }
            case 270362:
                restartRadio();
                break;
            case 270363:
                i = msg.arg1;
                onSetInternalDataEnabled(r0 == VP_START ? VDBG : VOWIFI_CONFIG, (Message) msg.obj);
                break;
            case 270364:
                log("EVENT_RESET_DONE");
                onResetDone((AsyncResult) msg.obj);
                break;
            case 270365:
                if (msg.obj != null) {
                    if (!(msg.obj instanceof String)) {
                        msg.obj = null;
                    }
                }
                onCleanUpAllConnections((String) msg.obj);
                break;
            case 270366:
                i = msg.arg1;
                enabled = r0 == VP_START ? VDBG : VOWIFI_CONFIG;
                log("CMD_SET_USER_DATA_ENABLE enabled=" + enabled);
                onSetUserDataEnabled(enabled);
                break;
            case 270367:
                i = msg.arg1;
                boolean met = r0 == VP_START ? VDBG : VOWIFI_CONFIG;
                log("CMD_SET_DEPENDENCY_MET met=" + met);
                bundle = msg.getData();
                if (bundle != null) {
                    String apnType = (String) bundle.get(APN_TYPE_KEY);
                    if (apnType != null) {
                        onSetDependencyMet(apnType, met);
                        break;
                    }
                }
                break;
            case 270368:
                i = msg.arg1;
                onSetPolicyDataEnabled(r0 == VP_START ? VDBG : VOWIFI_CONFIG);
                break;
            case 270369:
                onUpdateIcc();
                break;
            case 270370:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DC_RETRYING msg=" + msg);
                onDisconnectDcRetrying((AsyncResult) msg.obj);
                break;
            case 270371:
                onDataSetupCompleteError((AsyncResult) msg.obj);
                break;
            case 270372:
                int i2 = sEnableFailFastRefCounter;
                i = msg.arg1;
                sEnableFailFastRefCounter = (r0 == VP_START ? VP_START : -1) + i2;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA:  sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (sEnableFailFastRefCounter < 0) {
                    loge("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: sEnableFailFastRefCounter:" + sEnableFailFastRefCounter + " < 0");
                    sEnableFailFastRefCounter = VP_END;
                }
                enabled = sEnableFailFastRefCounter > 0 ? VDBG : VOWIFI_CONFIG;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: enabled=" + enabled + " sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                boolean z = this.mFailFast;
                if (r0 != enabled) {
                    this.mFailFast = enabled;
                    this.mDataStallDetectionEnabled = enabled ? VOWIFI_CONFIG : VDBG;
                    if (this.mDataStallDetectionEnabled && getOverallState() == State.CONNECTED) {
                        if (this.mInVoiceCall) {
                            break;
                        }
                        log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: start data stall");
                        stopDataStallAlarm();
                        startDataStallAlarm(VOWIFI_CONFIG);
                        break;
                    }
                    log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: stop data stall");
                    stopDataStallAlarm();
                    break;
                }
                break;
            case 270373:
                bundle = msg.getData();
                if (bundle != null) {
                    try {
                        this.mProvisioningUrl = (String) bundle.get("provisioningUrl");
                    } catch (ClassCastException e) {
                        loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url not a string" + e);
                        this.mProvisioningUrl = null;
                    }
                }
                if (!TextUtils.isEmpty(this.mProvisioningUrl)) {
                    loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioningUrl=" + this.mProvisioningUrl);
                    this.mIsProvisioning = VDBG;
                    startProvisioningApnAlarm();
                    break;
                }
                loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url is empty, ignoring");
                this.mIsProvisioning = VOWIFI_CONFIG;
                this.mProvisioningUrl = null;
                break;
            case 270374:
                boolean z2;
                log("CMD_IS_PROVISIONING_APN");
                Object apnType2 = null;
                try {
                    bundle = msg.getData();
                    if (bundle != null) {
                        apnType2 = (String) bundle.get(APN_TYPE_KEY);
                    }
                    if (TextUtils.isEmpty(apnType2)) {
                        loge("CMD_IS_PROVISIONING_APN: apnType is empty");
                        z2 = VOWIFI_CONFIG;
                    } else {
                        z2 = isProvisioningApn(apnType2);
                    }
                } catch (ClassCastException e2) {
                    loge("CMD_IS_PROVISIONING_APN: NO provisioning url ignoring");
                    z2 = VOWIFI_CONFIG;
                }
                log("CMD_IS_PROVISIONING_APN: ret=" + z2);
                this.mReplyAc.replyToMessage(msg, 270374, z2 ? VP_START : VP_END);
                break;
            case 270375:
                log("EVENT_PROVISIONING_APN_ALARM");
                ApnContext apnCtx = (ApnContext) this.mApnContextsById.get(VP_END);
                if (!apnCtx.isProvisioningApn() || !apnCtx.isConnectedOrConnecting()) {
                    log("EVENT_PROVISIONING_APN_ALARM: Not connected ignore");
                    break;
                }
                if (this.mProvisioningApnAlarmTag != msg.arg1) {
                    log("EVENT_PROVISIONING_APN_ALARM: ignore stale tag, mProvisioningApnAlarmTag:" + this.mProvisioningApnAlarmTag + " != arg1:" + msg.arg1);
                    break;
                }
                log("EVENT_PROVISIONING_APN_ALARM: Disconnecting");
                this.mIsProvisioning = VOWIFI_CONFIG;
                this.mProvisioningUrl = null;
                stopProvisioningApnAlarm();
                sendCleanUpConnection(VDBG, apnCtx);
                break;
                break;
            case 270376:
                i = msg.arg1;
                if (r0 != VP_START) {
                    if (msg.arg1 == 0) {
                        handleStopNetStatPoll((Activity) msg.obj);
                        break;
                    }
                }
                handleStartNetStatPoll((Activity) msg.obj);
                break;
                break;
            case 270377:
                onRatChange();
                if (!onUpdateIcc()) {
                    if (!isCTSimCard(this.mPhone.getPhoneId()) && getAnyDataEnabled()) {
                        for (ApnContext apnContext2 : this.mApnContexts.values()) {
                            if (DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED == apnContext2.getPdpFailCause() && (apnContext2.getState() == State.FAILED || apnContext2.getState() == State.IDLE)) {
                                log("tryRestartDataConnections, which reason is nwTypeChangedAndPDPSetupFailedBy33");
                                apnContext2.setPdpFailCause(DcFailCause.NONE);
                                setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                                break;
                            }
                        }
                        break;
                    }
                }
                log("onUpdateIcc: tryRestartDataConnections nwTypeChanged");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED, RetryFailures.ONLY_ON_CHANGE);
                break;
                break;
            case 270378:
                if (this.mProvisioningSpinner == msg.obj) {
                    this.mProvisioningSpinner.dismiss();
                    this.mProvisioningSpinner = null;
                    break;
                }
                break;
            case 270379:
                onDeviceProvisionedChange();
                break;
            case 270380:
                AsyncResult ar = msg.obj;
                String url = ar.userObj;
                log("dataConnectionTracker.handleMessage: EVENT_REDIRECTION_DETECTED=" + url);
                onDataConnectionRedirected(url, (HashMap) ar.result);
                break;
            case 271137:
                this.mPhone.mCi.resetAllConnections();
                break;
            case 271138:
                onDataSetupCompleteFailed();
                break;
            case 271139:
                onPdpResetAlarm(msg.arg1);
                break;
            case 271140:
                if (mWcdmaVpEnabled) {
                    log("EVENT_VP_STATUS_CHANGED");
                    onVpStatusChanged(msg.obj);
                    break;
                }
                break;
            case 271144:
                break;
            default:
                Rlog.e("DcTracker", "Unhandled event=" + msg);
                break;
        }
        onRadioAvailable();
        handleCustMessage(msg);
    }

    private void onRatChange() {
        if (isCTSimCard(this.mPhone.getPhoneId())) {
            int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
            boolean RatChange = ServiceState.isHrpd1X(dataRadioTech) != ServiceState.isHrpd1X(this.preDataRadioTech) ? VDBG : VOWIFI_CONFIG;
            boolean SetupRatChange = ServiceState.isHrpd1X(dataRadioTech) != ServiceState.isHrpd1X(this.preSetupBasedRadioTech) ? VDBG : VOWIFI_CONFIG;
            State overallState = getOverallState();
            boolean isConnected = overallState != State.CONNECTED ? overallState == State.CONNECTING ? VDBG : VOWIFI_CONFIG : VDBG;
            log("onRatChange: preDataRadioTech is: " + this.preDataRadioTech + "; dataRadioTech is: " + dataRadioTech);
            log("onRatChange: preSetupBasedRadioTech is: " + this.preSetupBasedRadioTech + "; overallState is: " + overallState);
            if (dataRadioTech != 0) {
                if (this.preDataRadioTech != -1 && RatChange) {
                    if (this.preSetupBasedRadioTech == 0 || SetupRatChange) {
                        if (isConnected) {
                            cleanUpAllConnections((boolean) VDBG, PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                        } else {
                            cleanUpAllConnections((boolean) VOWIFI_CONFIG, PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                            updateApnContextState();
                        }
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                    } else {
                        log("setup data call has been trigger by other flow, have no need to execute again.");
                    }
                }
                this.preDataRadioTech = dataRadioTech;
            }
        }
    }

    private void updateApnContextState() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == State.SCANNING) {
                apnContext.setState(State.IDLE);
                apnContext.setDataConnectionAc(null);
                cancelReconnectAlarm(apnContext);
            }
        }
    }

    private int getApnProfileID(String apnType) {
        if (TextUtils.equals(apnType, "ims") || TextUtils.equals(apnType, "xcap")) {
            return GSM_ROAMING_CARD1;
        }
        if (TextUtils.equals(apnType, "fota")) {
            return GSM_ROAMING_CARD2;
        }
        if (TextUtils.equals(apnType, "cbs")) {
            return LTE_NOT_ROAMING;
        }
        if (!TextUtils.equals(apnType, "ia") && TextUtils.equals(apnType, "dun")) {
            return VP_START;
        }
        return VP_END;
    }

    private int getCellLocationId() {
        CellLocation loc = this.mPhone.getCellLocation();
        if (loc == null) {
            return -1;
        }
        if (loc instanceof GsmCellLocation) {
            return ((GsmCellLocation) loc).getCid();
        }
        if (loc instanceof CdmaCellLocation) {
            return ((CdmaCellLocation) loc).getBaseStationId();
        }
        return -1;
    }

    private IccRecords getUiccRecords(int appFamily) {
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.fetchVSimIccRecords(appFamily);
        }
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    private UiccCardApplication getUiccCardApplication(int appFamily) {
        if (this.mPhone == null) {
            return null;
        }
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.getVSimUiccCardApplication(appFamily);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), appFamily);
    }

    private boolean onUpdateIcc() {
        boolean result = VOWIFI_CONFIG;
        if (this.mUiccController == null) {
            loge("onUpdateIcc: mUiccController is null. Error!");
            return VOWIFI_CONFIG;
        }
        String name;
        int appFamily = VP_START;
        if (VSimUtilsInner.isVSimPhone(this.mPhone)) {
            appFamily = VP_START;
        } else if (this.mPhone.getPhoneType() == VP_START) {
            appFamily = VP_START;
        } else if (this.mPhone.getPhoneType() == GSM_ROAMING_CARD1) {
            appFamily = GSM_ROAMING_CARD1;
        } else {
            log("Wrong phone type");
        }
        IccRecords newIccRecords = getUiccRecords(appFamily);
        StringBuilder append = new StringBuilder().append("onUpdateIcc: newIccRecords ");
        if (newIccRecords != null) {
            name = newIccRecords.getClass().getName();
        } else {
            name = null;
        }
        log(append.append(name).toString());
        UiccCardApplication newUiccApplication = getUiccCardApplication(appFamily);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!(this.mUiccApplcation == newUiccApplication && r == newIccRecords)) {
            if (this.mUiccApplcation != null) {
                log("Removing stale icc objects.");
                unregisterForGetAdDone(this.mUiccApplcation);
                if (r != null) {
                    unregisterForImsiReady(r);
                    unregisterForRecordsLoaded(r);
                    this.mIccRecords.set(null);
                }
                this.mUiccApplcation = null;
            }
            if (newUiccApplication == null || newIccRecords == null) {
                onSimNotReady();
            } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                log("New records found");
                this.mUiccApplcation = newUiccApplication;
                this.mIccRecords.set(newIccRecords);
                registerForImsi(newUiccApplication, newIccRecords);
                HwTelephonyFactory.getHwDataServiceChrManager().setRecordsLoadedRegistered(VDBG, this.mPhone.getSubId());
                registerForFdnRecordsLoaded(newIccRecords);
                SubscriptionController.getInstance().setSimProvisioningStatus(VP_END, this.mPhone.getSubId());
            }
            result = VDBG;
        }
        return result;
    }

    public void update() {
        log("update sub = " + this.mPhone.getSubId());
        log("update(): Active DDS, register for all events now!");
        onUpdateIcc();
        this.mUserDataEnabled = getDataEnabled();
        this.mAutoAttachOnCreation.set(VOWIFI_CONFIG);
        ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(VOWIFI_CONFIG);
    }

    public void updateForVSim() {
        log("vsim update sub = " + this.mPhone.getSubId());
        unregisterForAllEvents();
        log("update(): Active DDS, register for all events now!");
        registerForAllEvents();
        onUpdateIcc();
        this.mUserDataEnabled = VDBG;
    }

    public void cleanUpAllConnections(String cause) {
        cleanUpAllConnections(cause, null);
    }

    public void updateRecords() {
        onUpdateIcc();
    }

    public void cleanUpAllConnections(String cause, Message disconnectAllCompleteMsg) {
        log("cleanUpAllConnections");
        if (disconnectAllCompleteMsg != null) {
            this.mDisconnectAllCompleteMsgList.add(disconnectAllCompleteMsg);
        }
        Message msg = obtainMessage(270365);
        msg.obj = cause;
        sendMessage(msg);
    }

    private boolean checkCarrierAppAvailable(Intent intent) {
        String[] activationApp = getActivationAppName();
        if (activationApp == null || activationApp.length != GSM_ROAMING_CARD1) {
            return VOWIFI_CONFIG;
        }
        intent.setClassName(activationApp[VP_END], activationApp[VP_START]);
        if (!this.mPhone.getContext().getPackageManager().queryBroadcastReceivers(intent, 65536).isEmpty()) {
            return VDBG;
        }
        loge("Activation Carrier app is configured, but not available: " + activationApp[VP_END] + "." + activationApp[VP_START]);
        return VOWIFI_CONFIG;
    }

    private boolean notifyCarrierAppWithIntent(Intent intent) {
        if (this.mDisconnectPendingCount != 0) {
            loge("Wait for pending disconnect requests done");
            return VOWIFI_CONFIG;
        } else if (checkCarrierAppAvailable(intent)) {
            intent.putExtra("subscription", this.mPhone.getSubId());
            intent.addFlags(268435456);
            try {
                this.mPhone.getContext().sendBroadcast(intent);
                log("send Intent to Carrier app with action: " + intent.getAction());
                return VDBG;
            } catch (ActivityNotFoundException e) {
                loge("sendBroadcast failed: " + e);
                return VOWIFI_CONFIG;
            }
        } else {
            loge("Carrier app is unavailable");
            return VOWIFI_CONFIG;
        }
    }

    private void notifyCarrierAppForRedirection() {
        if (!isColdSimDetected() && !isOutOfCreditSimDetected() && this.mRedirectUrl != null) {
            Intent intent = new Intent("android.intent.action.REDIRECTION_DETECTED");
            intent.putExtra(REDIRECTION_URL_KEY, this.mRedirectUrl);
            if (notifyCarrierAppWithIntent(intent)) {
                this.mRedirectUrl = null;
            }
        }
    }

    private void notifyDataDisconnectComplete() {
        log("notifyDataDisconnectComplete");
        for (Message m : this.mDisconnectAllCompleteMsgList) {
            m.sendToTarget();
        }
        this.mDisconnectAllCompleteMsgList.clear();
    }

    private void notifyAllDataDisconnected() {
        sEnableFailFastRefCounter = VP_END;
        this.mFailFast = VOWIFI_CONFIG;
        this.mAllDataDisconnectedRegistrants.notifyRegistrants();
    }

    public void registerForAllDataDisconnected(Handler h, int what, Object obj) {
        this.mAllDataDisconnectedRegistrants.addUnique(h, what, obj);
        if (isDisconnected()) {
            log("notify All Data Disconnected");
            notifyAllDataDisconnected();
        }
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mAllDataDisconnectedRegistrants.remove(h);
    }

    private void onSetInternalDataEnabled(boolean enabled, Message onCompleteMsg) {
        log("onSetInternalDataEnabled: enabled=" + enabled);
        boolean sendOnComplete = VDBG;
        synchronized (this.mDataEnabledLock) {
            this.mInternalDataEnabled = enabled;
            if (enabled) {
                log("onSetInternalDataEnabled: changed to enabled, try to setup data call");
                onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            } else {
                sendOnComplete = VOWIFI_CONFIG;
                log("onSetInternalDataEnabled: changed to disabled, cleanUpAllConnections");
                cleanUpAllConnections(null, onCompleteMsg);
            }
        }
        if (sendOnComplete && onCompleteMsg != null) {
            onCompleteMsg.sendToTarget();
        }
    }

    public boolean setInternalDataEnabledFlag(boolean enable) {
        log("setInternalDataEnabledFlag(" + enable + ")");
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (int i = VP_END; i < stackArray.length; i += VP_START) {
            sb.append(stackArray[i].toString() + "\n");
        }
        log(sb.toString());
        if (this.mInternalDataEnabled != enable) {
            this.mInternalDataEnabled = enable;
        }
        return VDBG;
    }

    public boolean setInternalDataEnabled(boolean enable) {
        return setInternalDataEnabled(enable, null);
    }

    public boolean setInternalDataEnabled(boolean enable, Message onCompleteMsg) {
        log("setInternalDataEnabled(" + enable + ")");
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (int i = VP_END; i < stackArray.length; i += VP_START) {
            sb.append(stackArray[i].toString() + "\n");
        }
        log(sb.toString());
        Message msg = obtainMessage(270363, onCompleteMsg);
        msg.arg1 = enable ? VP_START : VP_END;
        sendMessage(msg);
        return VDBG;
    }

    public void setDataAllowed(boolean enable, Message response) {
        log("setDataAllowed: enable=" + enable);
        this.isCleanupRequired.set(enable ? VOWIFI_CONFIG : VDBG);
        this.mPhone.mCi.setDataAllowed(enable, response);
        this.mInternalDataEnabled = enable;
    }

    protected boolean isDefaultDataSubscription() {
        boolean z = VDBG;
        long subId = (long) this.mPhone.getSubId();
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            return VDBG;
        }
        long defaultDds = (long) SubscriptionController.getInstance().getDefaultDataSubId();
        log("isDefaultDataSubscription subId: " + subId + "defaultDds: " + defaultDds);
        if (subId != defaultDds) {
            z = VOWIFI_CONFIG;
        }
        return z;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("DcTracker:");
        pw.println(" RADIO_TESTS=false");
        pw.println(" mInternalDataEnabled=" + this.mInternalDataEnabled);
        pw.println(" mUserDataEnabled=" + this.mUserDataEnabled);
        pw.println(" sPolicyDataEnabed=" + sPolicyDataEnabled);
        pw.flush();
        pw.println(" mRequestedApnType=" + this.mRequestedApnType);
        pw.println(" mPhone=" + this.mPhone.getPhoneName());
        pw.println(" mActivity=" + this.mActivity);
        pw.println(" mState=" + this.mState);
        pw.println(" mTxPkts=" + this.mTxPkts);
        pw.println(" mRxPkts=" + this.mRxPkts);
        pw.println(" mNetStatPollPeriod=" + this.mNetStatPollPeriod);
        pw.println(" mNetStatPollEnabled=" + this.mNetStatPollEnabled);
        pw.println(" mDataStallTxRxSum=" + this.mDataStallTxRxSum);
        pw.println(" mDataStallAlarmTag=" + this.mDataStallAlarmTag);
        pw.println(" mDataStallDetectionEanbled=" + this.mDataStallDetectionEnabled);
        pw.println(" mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        pw.println(" mNoRecvPollCount=" + this.mNoRecvPollCount);
        pw.println(" mResolver=" + this.mResolver);
        pw.println(" mIsWifiConnected=" + this.mIsWifiConnected);
        pw.println(" mReconnectIntent=" + this.mReconnectIntent);
        pw.println(" mAutoAttachOnCreation=" + this.mAutoAttachOnCreation.get());
        pw.println(" mIsScreenOn=" + this.mIsScreenOn);
        pw.println(" mUniqueIdGenerator=" + this.mUniqueIdGenerator);
        pw.flush();
        pw.println(" ***************************************");
        DcController dcc = this.mDcc;
        if (dcc != null) {
            dcc.dump(fd, pw, args);
        } else {
            pw.println(" mDcc=null");
        }
        pw.println(" ***************************************");
        if (this.mDataConnections != null) {
            Set<Entry<Integer, DataConnection>> mDcSet = this.mDataConnections.entrySet();
            pw.println(" mDataConnections: count=" + mDcSet.size());
            for (Entry<Integer, DataConnection> entry : mDcSet) {
                Object[] objArr = new Object[VP_START];
                objArr[VP_END] = entry.getKey();
                pw.printf(" *** mDataConnection[%d] \n", objArr);
                ((DataConnection) entry.getValue()).dump(fd, pw, args);
            }
        } else {
            pw.println("mDataConnections=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        HashMap<String, Integer> apnToDcId = this.mApnToDataConnectionId;
        if (apnToDcId != null) {
            Set<Entry<String, Integer>> apnToDcIdSet = apnToDcId.entrySet();
            pw.println(" mApnToDataConnectonId size=" + apnToDcIdSet.size());
            for (Entry<String, Integer> entry2 : apnToDcIdSet) {
                objArr = new Object[GSM_ROAMING_CARD1];
                objArr[VP_END] = entry2.getKey();
                objArr[VP_START] = entry2.getValue();
                pw.printf(" mApnToDataConnectonId[%s]=%d\n", objArr);
            }
        } else {
            pw.println("mApnToDataConnectionId=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        ConcurrentHashMap<String, ApnContext> apnCtxs = this.mApnContexts;
        if (apnCtxs != null) {
            Set<Entry<String, ApnContext>> apnCtxsSet = apnCtxs.entrySet();
            pw.println(" mApnContexts size=" + apnCtxsSet.size());
            for (Entry<String, ApnContext> entry3 : apnCtxsSet) {
                ((ApnContext) entry3.getValue()).dump(fd, pw, args);
            }
            pw.println(" ***************************************");
        } else {
            pw.println(" mApnContexts=null");
        }
        pw.flush();
        ArrayList<ApnSetting> apnSettings = this.mAllApnSettings;
        if (apnSettings != null) {
            pw.println(" mAllApnSettings size=" + apnSettings.size());
            for (int i = VP_END; i < apnSettings.size(); i += VP_START) {
                Integer[] numArr = new Object[GSM_ROAMING_CARD1];
                numArr[VP_END] = Integer.valueOf(i);
                numArr[VP_START] = apnSettings.get(i);
                pw.printf(" mAllApnSettings[%d]: %s\n", numArr);
            }
            pw.flush();
        } else {
            pw.println(" mAllApnSettings=null");
        }
        pw.println(" mPreferredApn=" + this.mPreferredApn);
        pw.println(" mIsPsRestricted=" + this.mIsPsRestricted);
        pw.println(" mIsDisposed=" + this.mIsDisposed);
        pw.println(" mIntentReceiver=" + this.mIntentReceiver);
        pw.println(" mReregisterOnReconnectFailure=" + this.mReregisterOnReconnectFailure);
        pw.println(" canSetPreferApn=" + this.mCanSetPreferApn);
        pw.println(" mApnObserver=" + this.mApnObserver);
        pw.println(" getOverallState=" + getOverallState());
        pw.println(" mDataConnectionAsyncChannels=%s\n" + this.mDataConnectionAcHashMap);
        pw.println(" mAttached=" + this.mAttached.get());
        pw.flush();
    }

    public String[] getPcscfAddress(String apnType) {
        log("getPcscfAddress()");
        if (apnType == null) {
            log("apnType is null, return null");
            return null;
        }
        ApnContext apnContext;
        if (TextUtils.equals(apnType, "emergency")) {
            apnContext = (ApnContext) this.mApnContextsById.get(9);
        } else if (TextUtils.equals(apnType, "ims")) {
            apnContext = (ApnContext) this.mApnContextsById.get(PREF_APN_ID_LEN);
        } else {
            log("apnType is invalid, return null");
            return null;
        }
        if (apnContext == null) {
            log("apnContext is null, return null");
            return null;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        if (dcac == null) {
            return null;
        }
        String[] result = dcac.getPcscfAddr();
        for (int i = VP_END; i < result.length; i += VP_START) {
            log("Pcscf[" + i + "]: " + result[i]);
        }
        return result;
    }

    private void initEmergencyApnSetting() {
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, "type=\"emergency\"", null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                this.mEmergencyApn = makeApnSetting(cursor);
            }
            cursor.close();
        }
    }

    private void addEmergencyApnSetting() {
        if (!this.mEmergencyApnLoaded) {
            initEmergencyApnSetting();
            this.mEmergencyApnLoaded = VDBG;
        }
        if (this.mEmergencyApn == null) {
            return;
        }
        if (this.mAllApnSettings == null) {
            this.mAllApnSettings = new ArrayList();
            return;
        }
        boolean hasEmergencyApn = VOWIFI_CONFIG;
        for (ApnSetting apn : this.mAllApnSettings) {
            if (ArrayUtils.contains(apn.types, "emergency")) {
                hasEmergencyApn = VDBG;
                break;
            }
        }
        if (hasEmergencyApn) {
            log("addEmergencyApnSetting - E-APN setting is already present");
        } else {
            this.mAllApnSettings.add(this.mEmergencyApn);
        }
    }

    protected void onDataSetupCompleteFailed() {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get("default");
        long currentTimeMillis = System.currentTimeMillis();
        TelephonyManager tm = TelephonyManager.getDefault();
        if (this.mPhone.getServiceState().getVoiceRegState() != 0 || this.mAttached.get()) {
            log("onDataSetupCompleteFailed, cs out of service || ps in service!");
            return;
        }
        log("onDataSetupCompleteFailed, cs in service & ps out of service!");
        if (apnContext.isReady() && this.mInternalDataEnabled && this.mUserDataEnabled) {
            this.mPdpActFailCount += VP_START;
            if (VP_START == this.mPdpActFailCount) {
                this.mFirstPdpActFailTimestamp = currentTimeMillis;
            }
            log("onDataSetupCompleteFailed, mFirstPdpActFailTimestamp " + this.mFirstPdpActFailTimestamp + ", currentTimeMillis " + currentTimeMillis + ", mAttached " + this.mAttached + ", mRestartRildEnabled " + this.mRestartRildEnabled + ", mPdpActFailCount " + this.mPdpActFailCount);
            if (GSM_ROAMING_CARD2 <= this.mPdpActFailCount && currentTimeMillis - this.mFirstPdpActFailTimestamp >= ACTIVE_PDP_FAIL_TO_RESTART_RILD_MILLIS && tm.getCallState(VP_END) == 0 && tm.getCallState(VP_START) == 0 && this.mRestartRildEnabled) {
                this.mPhone.mCi.restartRild(null);
                this.mRestartRildEnabled = VOWIFI_CONFIG;
            }
        }
    }

    protected void clearRestartRildParam() {
        log("clearRestartRildParam");
        this.mFirstPdpActFailTimestamp = 0;
        this.mPdpActFailCount = VP_END;
        this.mRestartRildEnabled = VDBG;
    }

    private void cleanUpConnectionsOnUpdatedApns(boolean tearDown) {
        log("cleanUpConnectionsOnUpdatedApns: tearDown=" + tearDown);
        if (this.mAllApnSettings.isEmpty()) {
            cleanUpAllConnections(tearDown, PhoneInternalInterface.REASON_APN_CHANGED);
        } else {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                log("cleanUpConnectionsOnUpdatedApns for " + apnContext);
                boolean cleanUpApn = VDBG;
                ArrayList<ApnSetting> currentWaitingApns = apnContext.getWaitingApns();
                if (!(currentWaitingApns == null || apnContext.isDisconnected())) {
                    ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), this.mPhone.getServiceState().getRilDataRadioTechnology());
                    log("new waitingApns:" + waitingApns);
                    if (waitingApns.size() == currentWaitingApns.size()) {
                        cleanUpApn = VOWIFI_CONFIG;
                        for (int i = VP_END; i < waitingApns.size(); i += VP_START) {
                            if (!((ApnSetting) currentWaitingApns.get(i)).equals(waitingApns.get(i))) {
                                log("new waiting apn is different at " + i);
                                cleanUpApn = VDBG;
                                apnContext.setWaitingApns(waitingApns);
                                break;
                            }
                        }
                    }
                }
                if (cleanUpApn) {
                    apnContext.setReason(PhoneInternalInterface.REASON_APN_CHANGED);
                    cleanUpConnection(VDBG, apnContext);
                }
            }
        }
        if (!isConnected()) {
            stopNetStatPoll();
            stopDataStallAlarm();
        }
        this.mRequestedApnType = "default";
        log("mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
    }

    private void resetPollStats() {
        this.mTxPkts = -1;
        this.mRxPkts = -1;
        this.mNetStatPollPeriod = POLL_NETSTAT_MILLIS;
    }

    private void startNetStatPoll() {
        if (getOverallState() == State.CONNECTED && !this.mNetStatPollEnabled) {
            log("startNetStatPoll");
            resetPollStats();
            this.mNetStatPollEnabled = VDBG;
            this.mPollNetStat.run();
        }
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    private void stopNetStatPoll() {
        this.mNetStatPollEnabled = VOWIFI_CONFIG;
        removeCallbacks(this.mPollNetStat);
        log("stopNetStatPoll");
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    public void sendStartNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = VP_START;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStartNetStatPoll(Activity activity) {
        startNetStatPoll();
        startDataStallAlarm(VOWIFI_CONFIG);
        setActivity(activity);
    }

    public void sendStopNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = VP_END;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStopNetStatPoll(Activity activity) {
        stopNetStatPoll();
        stopDataStallAlarm();
        setActivity(activity);
    }

    private void updateDataActivity() {
        TxRxSum preTxRxSum = new TxRxSum(this.mTxPkts, this.mRxPkts);
        TxRxSum curTxRxSum = new TxRxSum();
        curTxRxSum.updateThisModemMobileTxRxSum(mIfacePhoneHashMap, this.mPhone.getPhoneId());
        this.mTxPkts = curTxRxSum.txPkts;
        this.mRxPkts = curTxRxSum.rxPkts;
        log("updateDataActivity: curTxRxSum=" + curTxRxSum + " preTxRxSum=" + preTxRxSum);
        if (!this.mNetStatPollEnabled) {
            return;
        }
        if (preTxRxSum.txPkts > 0 || preTxRxSum.rxPkts > 0) {
            long sent = this.mTxPkts - preTxRxSum.txPkts;
            long received = this.mRxPkts - preTxRxSum.rxPkts;
            log("updateDataActivity: sent=" + sent + " received=" + received);
            Activity newActivity = (sent <= 0 || received <= 0) ? (sent <= 0 || received != 0) ? (sent != 0 || received <= 0) ? this.mActivity == Activity.DORMANT ? this.mActivity : Activity.NONE : Activity.DATAIN : Activity.DATAOUT : Activity.DATAINANDOUT;
            if (this.mActivity != newActivity && this.mIsScreenOn) {
                log("updateDataActivity: newActivity=" + newActivity);
                this.mActivity = newActivity;
                this.mPhone.notifyDataActivity();
            }
        }
    }

    private int getRecoveryAction() {
        return System.getInt(this.mResolver, "radio.data.stall.recovery.action", VP_END);
    }

    protected void putRecoveryAction(int action) {
        System.putInt(this.mResolver, "radio.data.stall.recovery.action", action);
    }

    private void doRecovery() {
        if (getOverallState() == State.CONNECTED) {
            int recoveryAction = getRecoveryAction();
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDorecovery(this.mPhone, recoveryAction);
            switch (recoveryAction) {
                case VP_END /*0*/:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, this.mSentSinceLastRecv);
                    log("doRecovery() get data call list");
                    if (this.mDcc != null) {
                        this.mDcc.getDataCallList();
                    }
                    if (!noNeedDoRecovery(this.mApnContexts)) {
                        log("Since this apn is preseted apn, so we need to do recovery.");
                        putRecoveryAction(VP_START);
                        break;
                    }
                    putRecoveryAction(VP_END);
                    log("This apn is not preseted apn or we set nodorecovery to fobid do recovery, so we needn't to do recovery.");
                    break;
                case VP_START /*1*/:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_CLEANUP, this.mSentSinceLastRecv);
                    log("doRecovery() cleanup all connections");
                    cleanUpAllConnections(PhoneInternalInterface.REASON_PDP_RESET);
                    putRecoveryAction(GSM_ROAMING_CARD1);
                    break;
                case GSM_ROAMING_CARD1 /*2*/:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_REREGISTER, this.mSentSinceLastRecv);
                    log("doRecovery() re-register");
                    this.mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    putRecoveryAction(GSM_ROAMING_CARD2);
                    break;
                case GSM_ROAMING_CARD2 /*3*/:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART, this.mSentSinceLastRecv);
                    log("restarting radio");
                    putRecoveryAction(LTE_NOT_ROAMING);
                    this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(VDBG);
                    restartRadio();
                    break;
                case LTE_NOT_ROAMING /*4*/:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART_WITH_PROP, -1);
                    log("restarting radio with gsm.radioreset to true");
                    SystemProperties.set(this.RADIO_RESET_PROPERTY, "true");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    restartRadio();
                    if (!this.INTELLIGENT_DATA_SWITCH_CONFIG || !this.mIntelligentDataSwitchIsOn) {
                        putRecoveryAction(VP_END);
                        break;
                    } else {
                        putRecoveryAction(PREF_APN_ID_LEN);
                        break;
                    }
                case PREF_APN_ID_LEN /*5*/:
                    onActionDataSwitch(recoveryAction, VP_END);
                    break;
                default:
                    throw new RuntimeException("doRecovery: Invalid recoveryAction=" + recoveryAction);
            }
            this.mSentSinceLastRecv = 0;
        }
    }

    private void updateDataStallInfo() {
        TxRxSum preTxRxSum = new TxRxSum(this.mDataStallTxRxSum);
        if (enableTcpUdpSumForDataStall()) {
            this.mDataStallTxRxSum.updateThisModemMobileTxRxSum(mIfacePhoneHashMap, this.mPhone.getPhoneId());
        } else {
            this.mDataStallTxRxSum.updateTxRxSum();
        }
        log("updateDataStallInfo: mDataStallTxRxSum=" + this.mDataStallTxRxSum + " preTxRxSum=" + preTxRxSum);
        long sent = this.mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        long received = this.mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;
        if (sent > 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(VP_END);
        } else if (sent > 0 && received == 0) {
            if (isPhoneStateIdle()) {
                this.mSentSinceLastRecv += sent;
            } else {
                this.mSentSinceLastRecv = 0;
            }
            log("updateDataStallInfo: OUT sent=" + sent + " mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        } else if (sent == 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(VP_END);
        }
    }

    private boolean isPhoneStateIdle() {
        int i = VP_END;
        while (i < TelephonyManager.getDefault().getPhoneCount()) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone == null || phone.getState() == PhoneConstants.State.IDLE) {
                i += VP_START;
            } else {
                log("isPhoneStateIdle: Voice call active on sub: " + i);
                return VOWIFI_CONFIG;
            }
        }
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhone.isBusy()) {
            return VDBG;
        }
        log("isPhoneStateIdle: ImsPhone isBusy true");
        return VOWIFI_CONFIG;
    }

    private void onDataStallAlarm(int tag) {
        if (this.mDataStallAlarmTag != tag) {
            log("onDataStallAlarm: ignore, tag=" + tag + " expecting " + this.mDataStallAlarmTag);
            return;
        }
        updateDataStallInfo();
        int hangWatchdogTrigger = Global.getInt(this.mResolver, "pdp_watchdog_trigger_packet_count", NUMBER_SENT_PACKETS_OF_HANG);
        boolean suspectedStall = VOWIFI_CONFIG;
        if (this.mSentSinceLastRecv >= ((long) hangWatchdogTrigger)) {
            if (isPingOk()) {
                this.mSentSinceLastRecv = 0;
            } else {
                log("onDataStallAlarm: tag=" + tag + " do recovery action=" + getRecoveryAction());
                suspectedStall = VDBG;
                sendMessage(obtainMessage(270354));
            }
        }
        startDataStallAlarm(suspectedStall);
    }

    private void startDataStallAlarm(boolean suspectedStall) {
        int nextAction = getRecoveryAction();
        if (this.mDataStallDetectionEnabled && getOverallState() == State.CONNECTED) {
            int delayInMs;
            if (this.mIsScreenOn || suspectedStall || RecoveryAction.isAggressiveRecovery(nextAction)) {
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_aggressive_delay_in_ms", DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            } else {
                if (SystemProperties.getBoolean("ro.config.power", VOWIFI_CONFIG)) {
                    DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 6000000;
                }
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_non_aggressive_delay_in_ms", DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            }
            this.mDataStallAlarmTag += VP_START;
            Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
            intent.addFlags(268435456);
            intent.putExtra(DATA_STALL_ALARM_TAG_EXTRA, this.mDataStallAlarmTag);
            this.mDataStallAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), VP_END, intent, 134217728);
            this.mAlarmManager.setExact(GSM_ROAMING_CARD2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mDataStallAlarmIntent);
        }
    }

    private void stopDataStallAlarm() {
        this.mDataStallAlarmTag += VP_START;
        if (this.mDataStallAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mDataStallAlarmIntent);
            this.mDataStallAlarmIntent = null;
        }
    }

    private void restartDataStallAlarm() {
        if (!isConnected()) {
            return;
        }
        if (RecoveryAction.isAggressiveRecovery(getRecoveryAction())) {
            log("restartDataStallAlarm: action is pending. not resetting the alarm.");
            return;
        }
        stopDataStallAlarm();
        startDataStallAlarm(VOWIFI_CONFIG);
    }

    boolean isSupportLTE(ApnSetting apnSettings) {
        if (((apnSettings.bearer == 13 || apnSettings.bearer == 14) && isApnPreset(apnSettings)) || !isApnPreset(apnSettings)) {
            return VDBG;
        }
        return VOWIFI_CONFIG;
    }

    protected boolean isCTCardForFullNet() {
        if (isFullNetworkSupported()) {
            return isCTSimCard(this.mPhone.getPhoneId());
        }
        return VOWIFI_CONFIG;
    }

    private void onActionIntentProvisioningApnAlarm(Intent intent) {
        log("onActionIntentProvisioningApnAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(270375, intent.getAction());
        msg.arg1 = intent.getIntExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, VP_END);
        sendMessage(msg);
    }

    private void startProvisioningApnAlarm() {
        int delayInMs = Global.getInt(this.mResolver, "provisioning_apn_alarm_delay_in_ms", PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT);
        if (Build.IS_DEBUGGABLE) {
            try {
                delayInMs = Integer.parseInt(System.getProperty(DEBUG_PROV_APN_ALARM, Integer.toString(delayInMs)));
            } catch (NumberFormatException e) {
                loge("startProvisioningApnAlarm: e=" + e);
            }
        }
        this.mProvisioningApnAlarmTag += VP_START;
        log("startProvisioningApnAlarm: tag=" + this.mProvisioningApnAlarmTag + " delay=" + (delayInMs / POLL_NETSTAT_MILLIS) + "s");
        Intent intent = new Intent(INTENT_PROVISIONING_APN_ALARM);
        intent.addFlags(268435456);
        intent.putExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, this.mProvisioningApnAlarmTag);
        this.mProvisioningApnAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), VP_END, intent, 134217728);
        this.mAlarmManager.setExact(GSM_ROAMING_CARD1, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mProvisioningApnAlarmIntent);
    }

    private void stopProvisioningApnAlarm() {
        log("stopProvisioningApnAlarm: current tag=" + this.mProvisioningApnAlarmTag + " mProvsioningApnAlarmIntent=" + this.mProvisioningApnAlarmIntent);
        this.mProvisioningApnAlarmTag += VP_START;
        if (this.mProvisioningApnAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mProvisioningApnAlarmIntent);
            this.mProvisioningApnAlarmIntent = null;
        }
    }

    private ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String requestedApnType, int radioTech) {
        ArrayList<ApnSetting> apnList = new ArrayList();
        if (!(this.mAllApnSettings == null || this.mAllApnSettings.isEmpty())) {
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.canHandleType(requestedApnType) && ((!isLTENetwork() && ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech)) || (isLTENetwork() && (apn.bearer == 13 || apn.bearer == 14)))) {
                    if (!(TelephonyManager.getDefault().isNetworkRoaming(this.mPhone.getSubId()) && "ctnet".equals(apn.apn)) && (TelephonyManager.getDefault().isNetworkRoaming(this.mPhone.getSubId()) || !"ctwap".equals(apn.apn))) {
                        log("buildWaitingApns: ct supl featrue endabled, APN not match");
                    } else {
                        log("buildWaitingApns: adding apn=" + apn);
                        apnList.add(apn);
                    }
                }
            }
        }
        return apnList;
    }

    protected void onActionIntentPdpResetAlarm(Intent intent) {
        log("onActionIntentPdpResetAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(271139, intent.getAction());
        msg.arg1 = intent.getIntExtra(PDP_RESET_ALARM_TAG_EXTRA, VP_END);
        sendMessage(msg);
    }

    protected void onPdpResetAlarm(int tag) {
        if (this.mPdpResetAlarmTag != tag) {
            log("onPdpRestAlarm: ignore, tag=" + tag + " expecting " + this.mPdpResetAlarmTag);
        } else {
            cleanUpAllConnections(PhoneInternalInterface.REASON_PDP_RESET);
        }
    }

    protected void startPdpResetAlarm(int delay) {
        this.mPdpResetAlarmTag += VP_START;
        log("startPdpResetAlarm: tag=" + this.mPdpResetAlarmTag + " delay=" + (delay / POLL_NETSTAT_MILLIS) + "s");
        Intent intent = new Intent(INTENT_PDP_RESET_ALARM);
        intent.putExtra(PDP_RESET_ALARM_TAG_EXTRA, this.mPdpResetAlarmTag);
        log("startPdpResetAlarm: delay=" + delay + " action=" + intent.getAction());
        this.mPdpResetAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), VP_END, intent, 134217728);
        this.mAlarmManager.setExact(GSM_ROAMING_CARD1, SystemClock.elapsedRealtime() + ((long) delay), this.mPdpResetAlarmIntent);
    }

    protected void stopPdpResetAlarm() {
        log("stopPdpResetAlarm: current tag=" + this.mPdpResetAlarmTag + " mPdpResetAlarmIntent=" + this.mPdpResetAlarmIntent);
        this.mPdpResetAlarmTag += VP_START;
        if (this.mPdpResetAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mPdpResetAlarmIntent);
            this.mPdpResetAlarmIntent = null;
        }
    }

    void sendDataSetupCompleteFailed() {
        log("sendDataSetupCompleteFailed:");
        sendMessage(obtainMessage(271138));
    }

    private boolean isBlockSetInitialAttachApn() {
        String plmnsConfig = System.getString(this.mPhone.getContext().getContentResolver(), "apn_reminder_plmn");
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(operator)) {
            return VOWIFI_CONFIG;
        }
        return plmnsConfig.contains(operator);
    }
}

package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.AsyncResult;
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
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.DataFailCause;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataProfile;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwDataConnectionManager;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwNetworkManager;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.huawei.internal.telephony.DctConstantsExt;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import com.huawei.internal.telephony.dataconnection.DataConnectionEx;
import com.huawei.internal.telephony.dataconnection.DcFailCauseExt;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import huawei.cust.HwCustUtils;
import huawei.net.NetworkRequestExt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DcTracker extends Handler {
    static final String APN_ID = "apn_id";
    private static final int CAUSE_BY_DATA = 0;
    private static final int CAUSE_BY_ROAM = 1;
    public static final boolean CT_SUPL_FEATURE_ENABLE = SystemProperties.getBoolean("ro.hwpp.ct_supl_feature_enable", false);
    static final String DATA_COMPLETE_MSG_EXTRA_NETWORK_REQUEST = "extra_network_request";
    static final String DATA_COMPLETE_MSG_EXTRA_REQUEST_TYPE = "extra_request_type";
    static final String DATA_COMPLETE_MSG_EXTRA_SUCCESS = "extra_success";
    static final String DATA_COMPLETE_MSG_EXTRA_TRANSPORT_TYPE = "extra_transport_type";
    private static final int DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 60000;
    private static int DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 600000;
    private static final boolean DATA_STALL_NOT_SUSPECTED = false;
    private static final boolean DATA_STALL_SUSPECTED = true;
    private static final boolean DBG = true;
    private static final String DEBUG_PROV_APN_ALARM = "persist.debug.prov_apn_alarm";
    private static final boolean HW_DBG = SystemProperties.getBoolean("ro.debuggable", false);
    private static boolean HW_RADIO_DATA_STALL_ENABLE = true;
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final String INTENT_DATA_STALL_ALARM = "com.android.internal.telephony.data-stall";
    private static final String INTENT_DATA_STALL_ALARM_EXTRA_TAG = "data_stall_alarm_extra_tag";
    private static final String INTENT_DATA_STALL_ALARM_EXTRA_TRANSPORT_TYPE = "data_stall_alarm_extra_transport_type";
    private static final String INTENT_PROVISIONING_APN_ALARM = "com.android.internal.telephony.provisioning_apn_alarm";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.data-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reconnect_alarm_extra_reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TRANSPORT_TYPE = "reconnect_alarm_extra_transport_type";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "reconnect_alarm_extra_type";
    private static final int INVALID_STEP = -99;
    public static final boolean IS_DELAY_ATTACH_ENABLED = SystemProperties.getBoolean("ro.config.delay_attach_enabled", false);
    private static boolean IS_ENABLE_ESMFLAG_CURE = false;
    private static final boolean IS_NR_SLICE_SUPPORTED = HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported();
    private static boolean IS_PDN_REJ_CURE_ENABLE = true;
    public static final Uri MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
    private static final int NETWORK_TYPE_BIP0 = 38;
    private static final int NETWORK_TYPE_BIP1 = 39;
    private static final int NETWORK_TYPE_BIP2 = 40;
    private static final int NETWORK_TYPE_BIP3 = 41;
    private static final int NETWORK_TYPE_BIP4 = 42;
    private static final int NETWORK_TYPE_BIP5 = 43;
    private static final int NETWORK_TYPE_BIP6 = 44;
    private static final int NETWORK_TYPE_CBS = 12;
    private static final int NETWORK_TYPE_DEFAULT = 0;
    private static final int NETWORK_TYPE_DUN = 4;
    private static final int NETWORK_TYPE_EMERGENCY = 15;
    private static final int NETWORK_TYPE_FOTA = 10;
    private static final int NETWORK_TYPE_HIPRI = 5;
    private static final int NETWORK_TYPE_IA = 14;
    private static final int NETWORK_TYPE_IMS = 11;
    private static final int NETWORK_TYPE_INTERNAL_DEFAULT = 48;
    private static final int NETWORK_TYPE_MCX = 1001;
    private static final int NETWORK_TYPE_MMS = 2;
    private static final int NETWORK_TYPE_SNSSAI1 = 49;
    private static final int NETWORK_TYPE_SNSSAI2 = 50;
    private static final int NETWORK_TYPE_SNSSAI3 = 51;
    private static final int NETWORK_TYPE_SNSSAI4 = 52;
    private static final int NETWORK_TYPE_SNSSAI5 = 53;
    private static final int NETWORK_TYPE_SNSSAI6 = 54;
    private static final int NETWORK_TYPE_SUPL = 3;
    private static final int NETWORK_TYPE_XCAP = 45;
    private static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    protected static final int NVCFG_RESULT_FINISHED = 1;
    protected static final int PDP_RESET_ALARM_DELAY_IN_MS = 300000;
    private static final boolean PERMANENT_ERROR_HEAL_ENABLED = SystemProperties.getBoolean("ro.config.permanent_error_heal", false);
    private static final int POLL_NETSTAT_MILLIS = 1000;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 600000;
    private static final int POLL_PDP_MILLIS = 5000;
    public static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = Uri.parse("content://telephony/carriers/preferapn_no_update/subId/");
    private static final int PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT = 900000;
    private static final String PROVISIONING_APN_ALARM_TAG_EXTRA = "provisioning.apn.alarm.tag";
    private static final int PROVISIONING_SPINNER_TIMEOUT_MILLIS = 120000;
    private static final String PUPPET_MASTER_RADIO_STRESS_TEST = "gsm.defaultpdpcontext.active";
    private static final boolean RADIO_TESTS = false;
    private static final int RECONNECT_ALARM_DELAY_TIME_FOR_CS_ATTACHED = 5000;
    private static final int RECONNECT_ALARM_DELAY_TIME_MIN = 5000;
    private static final int RECOVERY_ACTION_CLEANUP = 1;
    private static final int RECOVERY_ACTION_GET_DATA_CALL_LIST = 0;
    private static final int RECOVERY_ACTION_OEM_EXT = 4;
    private static final int RECOVERY_ACTION_RADIO_RESTART = 3;
    private static final int RECOVERY_ACTION_REREGISTER = 2;
    public static final int RELEASE_TYPE_DETACH = 2;
    public static final int RELEASE_TYPE_HANDOVER = 3;
    public static final int RELEASE_TYPE_NORMAL = 1;
    public static final int REQUEST_TYPE_HANDOVER = 2;
    public static final int REQUEST_TYPE_NORMAL = 1;
    private static final int SLOT_1 = 1;
    protected static final int SWITCH_DISABLED = 0;
    protected static final int SWITCH_ENABLED = 1;
    protected static final String TELEPHONY_SOFT_SWITCH = "telephony_soft_switch";
    private static final int TYPE_REPORT_NO_RETRY_FOR_PDP_FAIL = 702;
    protected static final boolean USER_FORCE_DATA_SETUP = SystemProperties.getBoolean("ro.hwpp.allow_data_onlycs", false);
    private static final boolean VDBG = true;
    private static final boolean VDBG_STALL = false;
    private static final String WAP_APN = "3gwap";
    protected static final HashMap<String, Integer> mIfacePhoneHashMap = new HashMap<>();
    protected static final boolean mWcdmaVpEnabled = SystemProperties.get("ro.hwpp.wcdma_voice_preference", "false").equals("true");
    private static int sEnableFailFastRefCounter = 0;
    public AtomicBoolean isCleanupRequired;
    protected boolean isMultiSimEnabled;
    private DctConstants.Activity mActivity;
    private final AlarmManager mAlarmManager;
    private List<ApnSetting> mAllApnSettings;
    private RegistrantList mAllDataDisconnectedRegistrants;
    private final ConcurrentHashMap<String, ApnContext> mApnContexts;
    private final SparseArray<ApnContext> mApnContextsByType;
    private ApnChangeObserver mApnObserver;
    private final LocalLog mApnSettingsInitializationLog;
    private HashMap<String, Integer> mApnToDataConnectionId;
    private AtomicBoolean mAttached;
    private AtomicBoolean mAutoAttachEnabled;
    private boolean mAutoAttachOnCreationConfig;
    private boolean mCanSetPreferApn;
    public Map<String, List<ApnSetting>> mCureApnSettings;
    private final Handler mDataConnectionTracker;
    private HashMap<Integer, DataConnection> mDataConnections;
    private final DataEnabledSettings mDataEnabledSettings;
    private final LocalLog mDataRoamingLeakageLog;
    private boolean mDataServiceBound;
    private final DataServiceManager mDataServiceManager;
    private PendingIntent mDataStallAlarmIntent;
    private int mDataStallAlarmTag;
    private TxRxSum mDataStallDnsTxRxSum;
    private volatile boolean mDataStallNoRxEnabled;
    private TxRxSum mDataStallTcpTxRxSum;
    private TxRxSum mDataStallTxRxSum;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcController mDcc;
    private int mDisconnectPendingCount;
    private DataStallRecoveryHandler mDsRecoveryHandler;
    private ApnSetting mEmergencyApn;
    private boolean mEmergencyApnLoaded;
    private volatile boolean mFailFast;
    protected HwCustDcTracker mHwCustDcTracker;
    private HwDataConnectionManager mHwDcManager;
    private IHwDcTrackerEx mHwDcTrackerEx;
    private final AtomicReference<IccRecords> mIccRecords;
    private boolean mInVoiceCall;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mIsDisposed;
    private boolean mIsProvisioning;
    private boolean mIsPsRestricted;
    private boolean mIsPseudoImsi;
    private boolean mIsScreenOn;
    private ArrayList<DataProfile> mLastDataProfileList;
    private final String mLogTag;
    private boolean mNetStatPollEnabled;
    private int mNetStatPollPeriod;
    private int mNoRecvPollCount;
    private int mOldRat;
    private final DctOnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    protected final Phone mPhone;
    private final PhoneExt mPhoneExt;
    private final Runnable mPollNetStat;
    private ApnSetting mPreferredApn;
    private final ArrayList<ApnContext> mPrioritySortedApnContexts;
    private final String mProvisionActionName;
    private BroadcastReceiver mProvisionBroadcastReceiver;
    private PendingIntent mProvisioningApnAlarmIntent;
    private int mProvisioningApnAlarmTag;
    private ProgressDialog mProvisioningSpinner;
    private String mProvisioningUrl;
    private PendingIntent mReconnectIntent;
    private String mRecoveryReason;
    private AsyncChannel mReplyAc;
    private final Map<Integer, List<Message>> mRequestNetworkCompletionMsgs;
    private int mRequestedApnType;
    private boolean mReregisterOnReconnectFailure;
    private ContentResolver mResolver;
    private long mRxPkts;
    private long mSentSinceLastRecv;
    private final SettingsObserver mSettingsObserver;
    private DctConstants.State mState;
    private SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;
    private final int mTransportType;
    private long mTxPkts;
    protected UiccCardApplication mUiccApplcation;
    private final UiccController mUiccController;
    private AtomicInteger mUniqueIdGenerator;
    public int mVpStatus;
    public int preSetupBasedRadioTech;

    @Retention(RetentionPolicy.SOURCE)
    private @interface RecoveryAction {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ReleaseNetworkType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestNetworkType {
    }

    /* access modifiers changed from: private */
    public enum RetryFailures {
        ALWAYS,
        ONLY_ON_CHANGE
    }

    /* access modifiers changed from: private */
    public class DctOnSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        private DctOnSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = DcTracker.this.mPhone.getSubId();
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                DcTracker.this.registerSettingsObserver();
            }
            if (SubscriptionManager.isValidSubscriptionId(subId) && this.mPreviousSubId.getAndSet(subId) != subId) {
                DcTracker.this.onRecordsLoadedOrSubIdChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerSettingsObserver() {
        Uri contentUri;
        this.mSettingsObserver.unobserve();
        if (TelephonyManager.getDefault().getSimCount() == 1) {
            contentUri = Settings.Global.getUriFor("data_roaming");
        } else {
            contentUri = Settings.Global.getUriFor(this.mHwDcTrackerEx.getDataRoamingSettingItem("data_roaming"));
        }
        this.mSettingsObserver.observe(contentUri, 270384);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("device_provisioned"), 270386);
    }

    public static class TxRxSum {
        public long rxPkts;
        public long txPkts;

        public TxRxSum() {
            reset();
        }

        public TxRxSum(long txPkts2, long rxPkts2) {
            this.txPkts = txPkts2;
            this.rxPkts = rxPkts2;
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

        public void updateHwTcpTxRxSum(long currentTxPkts, long currentRxPkts) {
            this.txPkts = currentTxPkts;
            this.rxPkts = currentRxPkts;
        }

        public void updateTcpTxRxSum() {
            this.txPkts = TrafficStats.getMobileTcpTxPackets();
            this.rxPkts = TrafficStats.getMobileTcpRxPackets();
        }

        public void updateTotalTxRxSum() {
            this.txPkts = TrafficStats.getMobileTxPackets();
            this.rxPkts = TrafficStats.getMobileRxPackets();
        }

        public void updateThisModemMobileTxRxSum(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
            this.txPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileTxPackets(ifacePhoneHashMap, phoneId);
            this.rxPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileRxPackets(ifacePhoneHashMap, phoneId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentReconnectAlarm(Intent intent) {
        Message msg = obtainMessage(270383);
        msg.setData(intent.getExtras());
        sendMessage(msg);
    }

    private void onDataReconnect(Bundle bundle) {
        String reason = bundle.getString(INTENT_RECONNECT_ALARM_EXTRA_REASON);
        String apnType = bundle.getString(INTENT_RECONNECT_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = bundle.getInt("subscription", -1);
        if (SubscriptionManager.isValidSubscriptionId(currSubId) && currSubId == phoneSubId && bundle.getInt(INTENT_RECONNECT_ALARM_EXTRA_TRANSPORT_TYPE, 0) == this.mTransportType) {
            ApnContext apnContext = this.mApnContexts.get(apnType);
            log("onDataReconnect: mState=" + this.mState + " reason=" + reason + " apnType=" + apnType + " apnContext=" + apnContext);
            if (apnContext != null && apnContext.isEnabled()) {
                apnContext.setReason(reason);
                DctConstants.State apnContextState = apnContext.getState();
                log("onDataReconnect: apnContext state=" + apnContextState);
                if (apnContextState == DctConstants.State.FAILED || apnContextState == DctConstants.State.IDLE) {
                    log("onDataReconnect: state is FAILED|IDLE, disassociate");
                    apnContext.releaseDataConnection(PhoneConfigurationManager.SSSS);
                } else {
                    log("onDataReconnect: keep associated");
                }
                sendMessage(obtainMessage(270339, apnContext));
                apnContext.setReconnectIntent(null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentDataStallAlarm(Intent intent) {
        int subId = intent.getIntExtra("subscription", -1);
        if (SubscriptionManager.isValidSubscriptionId(subId) && subId == this.mPhone.getSubId() && intent.getIntExtra(INTENT_DATA_STALL_ALARM_EXTRA_TRANSPORT_TYPE, 0) == this.mTransportType) {
            Message msg = obtainMessage(270353, intent.getAction());
            msg.arg1 = intent.getIntExtra(INTENT_DATA_STALL_ALARM_EXTRA_TAG, 0);
            sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(DcTracker.this.mDataConnectionTracker);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessage(dcTracker.obtainMessage(270355));
        }
    }

    public DcTracker(Phone phone, int transportType) {
        this.mCureApnSettings = new ConcurrentHashMap();
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = 17;
        this.mIsPseudoImsi = false;
        this.mHwDcTrackerEx = null;
        this.mPrioritySortedApnContexts = new ArrayList<>();
        this.mVpStatus = 0;
        this.mAllApnSettings = new CopyOnWriteArrayList();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mDataServiceBound = false;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mOldRat = 0;
        this.mDataRoamingLeakageLog = new LocalLog(10);
        this.mApnSettingsInitializationLog = new LocalLog(10);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        DcTracker.this.log("screen on");
                        DcTracker.this.mIsScreenOn = true;
                        DcTracker.this.stopNetStatPoll();
                        DcTracker.this.startNetStatPoll();
                        DcTracker.this.restartDataStallAlarm();
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        DcTracker.this.log("screen off");
                        DcTracker.this.mIsScreenOn = false;
                        DcTracker.this.stopNetStatPoll();
                        DcTracker.this.startNetStatPoll();
                        DcTracker.this.restartDataStallAlarm();
                    } else if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                        DcTracker dcTracker = DcTracker.this;
                        dcTracker.log("Reconnect alarm. Previous state was " + DcTracker.this.mState);
                        DcTracker.this.onActionIntentReconnectAlarm(intent);
                    } else if (DcTracker.INTENT_DATA_STALL_ALARM.equals(action)) {
                        DcTracker.this.log("Data stall alarm");
                        DcTracker.this.onActionIntentDataStallAlarm(intent);
                    } else if (DcTracker.INTENT_PROVISIONING_APN_ALARM.equals(action)) {
                        DcTracker.this.log("Provisioning apn alarm");
                        DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                    } else if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                        DcTracker.this.log("received carrier config change");
                        if (DcTracker.this.mIccRecords.get() != null && ((IccRecords) DcTracker.this.mIccRecords.get()).getRecordsLoaded() && !((IccRecords) DcTracker.this.mIccRecords.get()).isHwCustDataRoamingOpenArea()) {
                            DcTracker.this.setDefaultDataRoamingEnabled();
                        }
                    } else if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(action)) {
                        DcTracker.this.log("Received SWITCH_SLOT_DONE");
                        String operator = DcTracker.this.mIccRecords.get() != null ? ((IccRecords) DcTracker.this.mIccRecords.get()).getOperatorNumeric() : PhoneConfigurationManager.SSSS;
                        int switchSlotStep = intent.getIntExtra(DcTracker.HW_SWITCH_SLOT_STEP, DcTracker.INVALID_STEP);
                        if (!TextUtils.isEmpty(operator) && 1 == switchSlotStep) {
                            DcTracker.this.onRecordsLoadedOrSubIdChanged();
                        }
                    } else if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                        DcTracker.this.log("com.huawei.devicepolicy.action.POLICY_CHANGED");
                        String action_tag = intent.getStringExtra("action_tag");
                        if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_data_4G") && DcTracker.this.mPhone != null && DcTracker.this.mPhone.getPhoneId() == 1) {
                            if (intent.getBooleanExtra("dataState", false)) {
                                DcTracker.this.cleanUpAllConnections("dataDisabledInternal");
                            } else {
                                DcTracker.this.setupDataOnAllConnectableApns("dataEnabled", RetryFailures.ALWAYS);
                            }
                        }
                    } else {
                        DcTracker dcTracker2 = DcTracker.this;
                        dcTracker2.log("onReceive: Unknown action=" + action);
                    }
                }
            }
        };
        this.mPollNetStat = new Runnable() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.mNetStatPollPeriod = Settings.Global.getInt(dcTracker.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    DcTracker dcTracker2 = DcTracker.this;
                    dcTracker2.mNetStatPollPeriod = Settings.Global.getInt(dcTracker2.mResolver, "pdp_watchdog_long_poll_interval_ms", 600000);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new DctOnSubscriptionsChangedListener();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference<>();
        this.mActivity = DctConstants.Activity.NONE;
        this.mState = DctConstants.State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallTcpTxRxSum = new TxRxSum(0, 0);
        this.mDataStallDnsTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mRecoveryReason = "sent no recv";
        this.mNoRecvPollCount = 0;
        this.mDataStallNoRxEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = true;
        this.mAutoAttachEnabled = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap<>();
        this.mApnToDataConnectionId = new HashMap<>();
        this.mApnContexts = new ConcurrentHashMap<>();
        this.mApnContextsByType = new SparseArray<>();
        this.mDisconnectPendingCount = 0;
        this.mLastDataProfileList = new ArrayList<>();
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.preSetupBasedRadioTech = -1;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mUiccApplcation = null;
        this.mRequestNetworkCompletionMsgs = new HashMap();
        this.mEmergencyApnLoaded = false;
        this.mPhone = phone;
        this.mPhoneExt = new PhoneExt();
        this.mPhoneExt.setPhone(this.mPhone);
        this.mTelephonyManager = TelephonyManager.from(phone.getContext()).createForSubscriptionId(phone.getSubId());
        StringBuilder sb = new StringBuilder();
        sb.append("-");
        sb.append(transportType == 1 ? "C" : "I");
        String tagSuffix = sb.toString();
        if (this.mTelephonyManager.getPhoneCount() > 1) {
            tagSuffix = tagSuffix + "-" + this.mPhone.getPhoneId();
        }
        this.mLogTag = "DCT" + tagSuffix;
        log(".constructor");
        this.mTransportType = transportType;
        this.mDataServiceManager = new DataServiceManager(phone, transportType, tagSuffix);
        if (this.mHwDcTrackerEx == null) {
            DcTrackerEx dcTrackerEx = new DcTrackerEx();
            dcTrackerEx.setDcTracker(this);
            this.mHwDcTrackerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwDcTrackerEx(this.mPhoneExt, dcTrackerEx);
        }
        IHwDcTrackerEx iHwDcTrackerEx = this.mHwDcTrackerEx;
        if (iHwDcTrackerEx != null) {
            IS_PDN_REJ_CURE_ENABLE = iHwDcTrackerEx.isPdnRejCureEnable();
            HW_RADIO_DATA_STALL_ENABLE = this.mHwDcTrackerEx.isHwRadioDataStallEnable();
            IS_ENABLE_ESMFLAG_CURE = this.mHwDcTrackerEx.isEnableEsmFlagCure();
        }
        this.mHwDcManager = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwDataConnectionManager();
        if (this.mHwCustDcTracker == null) {
            this.mHwCustDcTracker = (HwCustDcTracker) HwCustUtils.createObj(HwCustDcTracker.class, new Object[]{this});
        }
        this.mResolver = this.mPhone.getContext().getContentResolver();
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 270369, null);
        if (VSimUtilsInner.isHisiVSimSlot(this.mPhone.getPhoneId())) {
            VSimUtilsInner.registerForIccChanged(this, 270369, null);
        }
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mDsRecoveryHandler = new DataStallRecoveryHandler();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(INTENT_DATA_STALL_ALARM);
        filter.addAction(INTENT_PROVISIONING_APN_ALARM);
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        this.mDataEnabledSettings = this.mPhone.getDataEnabledSettings();
        this.mDataEnabledSettings.registerForDataEnabledChanged(this, 270382, null);
        this.mDataEnabledSettings.registerForDataEnabledOverrideChanged(this, 270387);
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mAutoAttachEnabled.set(PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getBoolean(Phone.DATA_DISABLED_ON_BOOT_KEY, false));
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        HandlerThread dcHandlerThread = new HandlerThread("DcHandlerThread");
        dcHandlerThread.start();
        Handler dcHandler = new Handler(dcHandlerThread.getLooper());
        this.mDcc = DcController.makeDcc(this.mPhone, this, this.mDataServiceManager, dcHandler, tagSuffix);
        this.mDcTesterFailBringUpAll = new DcTesterFailBringUpAll(this.mPhone, dcHandler);
        this.mDataConnectionTracker = this;
        registerForAllEvents();
        update();
        this.mApnObserver = new ApnChangeObserver();
        phone.getContext().getContentResolver().registerContentObserver(Telephony.Carriers.CONTENT_URI, true, this.mApnObserver);
        initApnContexts();
        Iterator<ApnContext> it = this.mApnContexts.values().iterator();
        while (it.hasNext()) {
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction("com.android.internal.telephony.data-reconnect." + it.next().getApnType());
            this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter2, null, this.mPhone);
        }
        this.mProvisionActionName = "com.android.internal.telephony.PROVISION" + phone.getPhoneId();
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this);
        registerSettingsObserver();
        this.mHwDcTrackerEx.init();
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null && hwCustDcTracker.isClearCodeEnabled()) {
            this.mHwCustDcTracker.startListenCellLocationChange();
        }
        HwCustDcTracker hwCustDcTracker2 = this.mHwCustDcTracker;
        if (hwCustDcTracker2 != null) {
            hwCustDcTracker2.registerForFdn();
        }
        sendMessage(obtainMessage(271137));
    }

    @VisibleForTesting
    public DcTracker() {
        this.mCureApnSettings = new ConcurrentHashMap();
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = 17;
        this.mIsPseudoImsi = false;
        this.mHwDcTrackerEx = null;
        this.mPrioritySortedApnContexts = new ArrayList<>();
        this.mVpStatus = 0;
        this.mAllApnSettings = new CopyOnWriteArrayList();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mDataServiceBound = false;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mOldRat = 0;
        this.mDataRoamingLeakageLog = new LocalLog(10);
        this.mApnSettingsInitializationLog = new LocalLog(10);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        DcTracker.this.log("screen on");
                        DcTracker.this.mIsScreenOn = true;
                        DcTracker.this.stopNetStatPoll();
                        DcTracker.this.startNetStatPoll();
                        DcTracker.this.restartDataStallAlarm();
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        DcTracker.this.log("screen off");
                        DcTracker.this.mIsScreenOn = false;
                        DcTracker.this.stopNetStatPoll();
                        DcTracker.this.startNetStatPoll();
                        DcTracker.this.restartDataStallAlarm();
                    } else if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                        DcTracker dcTracker = DcTracker.this;
                        dcTracker.log("Reconnect alarm. Previous state was " + DcTracker.this.mState);
                        DcTracker.this.onActionIntentReconnectAlarm(intent);
                    } else if (DcTracker.INTENT_DATA_STALL_ALARM.equals(action)) {
                        DcTracker.this.log("Data stall alarm");
                        DcTracker.this.onActionIntentDataStallAlarm(intent);
                    } else if (DcTracker.INTENT_PROVISIONING_APN_ALARM.equals(action)) {
                        DcTracker.this.log("Provisioning apn alarm");
                        DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                    } else if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                        DcTracker.this.log("received carrier config change");
                        if (DcTracker.this.mIccRecords.get() != null && ((IccRecords) DcTracker.this.mIccRecords.get()).getRecordsLoaded() && !((IccRecords) DcTracker.this.mIccRecords.get()).isHwCustDataRoamingOpenArea()) {
                            DcTracker.this.setDefaultDataRoamingEnabled();
                        }
                    } else if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(action)) {
                        DcTracker.this.log("Received SWITCH_SLOT_DONE");
                        String operator = DcTracker.this.mIccRecords.get() != null ? ((IccRecords) DcTracker.this.mIccRecords.get()).getOperatorNumeric() : PhoneConfigurationManager.SSSS;
                        int switchSlotStep = intent.getIntExtra(DcTracker.HW_SWITCH_SLOT_STEP, DcTracker.INVALID_STEP);
                        if (!TextUtils.isEmpty(operator) && 1 == switchSlotStep) {
                            DcTracker.this.onRecordsLoadedOrSubIdChanged();
                        }
                    } else if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                        DcTracker.this.log("com.huawei.devicepolicy.action.POLICY_CHANGED");
                        String action_tag = intent.getStringExtra("action_tag");
                        if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_data_4G") && DcTracker.this.mPhone != null && DcTracker.this.mPhone.getPhoneId() == 1) {
                            if (intent.getBooleanExtra("dataState", false)) {
                                DcTracker.this.cleanUpAllConnections("dataDisabledInternal");
                            } else {
                                DcTracker.this.setupDataOnAllConnectableApns("dataEnabled", RetryFailures.ALWAYS);
                            }
                        }
                    } else {
                        DcTracker dcTracker2 = DcTracker.this;
                        dcTracker2.log("onReceive: Unknown action=" + action);
                    }
                }
            }
        };
        this.mPollNetStat = new Runnable() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.mNetStatPollPeriod = Settings.Global.getInt(dcTracker.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    DcTracker dcTracker2 = DcTracker.this;
                    dcTracker2.mNetStatPollPeriod = Settings.Global.getInt(dcTracker2.mResolver, "pdp_watchdog_long_poll_interval_ms", 600000);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new DctOnSubscriptionsChangedListener();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference<>();
        this.mActivity = DctConstants.Activity.NONE;
        this.mState = DctConstants.State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallTcpTxRxSum = new TxRxSum(0, 0);
        this.mDataStallDnsTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mRecoveryReason = "sent no recv";
        this.mNoRecvPollCount = 0;
        this.mDataStallNoRxEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = true;
        this.mAutoAttachEnabled = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap<>();
        this.mApnToDataConnectionId = new HashMap<>();
        this.mApnContexts = new ConcurrentHashMap<>();
        this.mApnContextsByType = new SparseArray<>();
        this.mDisconnectPendingCount = 0;
        this.mLastDataProfileList = new ArrayList<>();
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.preSetupBasedRadioTech = -1;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mUiccApplcation = null;
        this.mRequestNetworkCompletionMsgs = new HashMap();
        this.mEmergencyApnLoaded = false;
        this.mLogTag = "DCT";
        this.mTelephonyManager = null;
        this.mAlarmManager = null;
        this.mPhone = null;
        this.mPhoneExt = null;
        this.mUiccController = null;
        this.mDataConnectionTracker = null;
        this.mProvisionActionName = null;
        this.mSettingsObserver = new SettingsObserver(null, this);
        this.mDataEnabledSettings = null;
        this.mTransportType = 0;
        this.mDataServiceManager = null;
    }

    public void registerServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this.mTransportType, this, 270352, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this.mTransportType, this, 270345, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOn(this, 270347, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 270348, null, true);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedEnabled(this, 270358, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedDisabled(this, 270359, null);
        log("registerForDataRegStateOrRatChanged");
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this.mTransportType, this, 270377, null);
        if (mWcdmaVpEnabled) {
            this.mPhone.mCi.registerForReportVpStatus(this, DctConstantsExt.EVENT_VP_STATUS_CHANGED, null);
        }
    }

    public void unregisterServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(this.mTransportType, this);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(this.mTransportType, this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedEnabled(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedDisabled(this);
        log("unregisterForDataRegStateOrRatChanged");
        this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this.mTransportType, this);
    }

    private void registerForAllEvents() {
        this.mPhone.mCi.registerForUnsolNvCfgFinished(this, 271145, null);
        if (this.mTransportType == 1) {
            this.mPhone.mCi.registerForAvailable(this, 270337, null);
            this.mPhone.mCi.registerForOffOrNotAvailable(this, 270342, null);
            this.mPhone.mCi.registerForPcoData(this, 270381, null);
        }
        if (this.mPhone.getCallTracker() != null) {
            this.mPhone.getCallTracker().registerForVoiceCallEnded(this, 270344, null);
            this.mPhone.getCallTracker().registerForVoiceCallStarted(this, 270343, null);
        }
        registerServiceStateTrackerEvents();
        this.mPhone.mCi.registerForPcoData(this, 270381, null);
        this.mDataServiceManager.registerForServiceBindingChanged(this, 270385, null);
    }

    public void dispose() {
        log("DCT.dispose");
        if (this.mProvisionBroadcastReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
            this.mProvisionBroadcastReceiver = null;
        }
        ProgressDialog progressDialog = this.mProvisioningSpinner;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mProvisioningSpinner = null;
        }
        cleanUpAllConnectionsInternal(true, null);
        this.mIsDisposed = true;
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        if (VSimUtilsInner.isHisiVSimSlot(this.mPhone.getPhoneId())) {
            VSimUtilsInner.unregisterForIccChanged(this);
        }
        this.mUiccController.unregisterForIccChanged(this);
        this.mSettingsObserver.unobserve();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mDcc.dispose();
        this.mDcTesterFailBringUpAll.dispose();
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mApnObserver);
        this.mApnContexts.clear();
        this.mApnContextsByType.clear();
        this.mPrioritySortedApnContexts.clear();
        unregisterForAllEvents();
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null && hwCustDcTracker.isClearCodeEnabled()) {
            this.mHwCustDcTracker.stopListenCellLocationChange();
        }
        HwCustDcTracker hwCustDcTracker2 = this.mHwCustDcTracker;
        if (hwCustDcTracker2 != null) {
            hwCustDcTracker2.unregisterForFdn();
        }
        destroyDataConnections();
        HwCustDcTracker hwCustDcTracker3 = this.mHwCustDcTracker;
        if (hwCustDcTracker3 != null) {
            hwCustDcTracker3.dispose();
        }
        this.mHwDcTrackerEx.dispose();
    }

    private void unregisterForAllEvents() {
        if (this.mTransportType == 1) {
            this.mPhone.mCi.unregisterForAvailable(this);
            this.mPhone.mCi.unregisterForOffOrNotAvailable(this);
            this.mPhone.mCi.unregisterForPcoData(this);
        }
        if (this.mUiccApplcation != null) {
            UiccCardApplicationEx appEx = new UiccCardApplicationEx();
            appEx.setUiccCardApplication(this.mUiccApplcation);
            this.mHwDcTrackerEx.unregisterForGetAdDone(appEx);
            this.mUiccApplcation = null;
        }
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            IccRecordsEx iccRecordsEx = new IccRecordsEx();
            iccRecordsEx.setIccRecords(r);
            this.mHwDcTrackerEx.unregisterForRecordsLoaded(iccRecordsEx);
            this.mHwDcTrackerEx.unregisterForImsiReady(iccRecordsEx);
            HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
            if (hwCustDcTracker != null) {
                hwCustDcTracker.unregisterForFdnRecordsLoaded(r);
            }
            this.mIccRecords.set(null);
        }
        if (this.mPhone.getCallTracker() != null) {
            this.mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
            this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        }
        unregisterServiceStateTrackerEvents();
        if (mWcdmaVpEnabled) {
            this.mPhone.mCi.unregisterForReportVpStatus(this);
        }
        this.mDataServiceManager.unregisterForServiceBindingChanged(this);
        this.mDataEnabledSettings.unregisterForDataEnabledChanged(this);
        this.mDataEnabledSettings.unregisterForDataEnabledOverrideChanged(this);
        this.mPhone.mCi.unregisterForUnsolNvCfgFinished(this);
    }

    private void reevaluateDataConnections() {
        for (DataConnection dataConnection : this.mDataConnections.values()) {
            dataConnection.reevaluateRestrictedState();
        }
    }

    public int getTransportType() {
        return this.mTransportType;
    }

    public long getSubId() {
        return (long) this.mPhone.getSubId();
    }

    public DctConstants.Activity getActivity() {
        return this.mActivity;
    }

    private void setActivity(DctConstants.Activity activity) {
        log("setActivity = " + activity);
        this.mActivity = activity;
        this.mPhone.notifyDataActivity();
    }

    public void requestNetwork(NetworkRequest networkRequest, int type, Message onCompleteMsg) {
        ApnContext apnContext;
        int apnType = ApnContext.getApnTypeFromNetworkRequest(networkRequest);
        if (!IS_NR_SLICE_SUPPORTED || apnType != 33554432) {
            apnContext = this.mApnContextsByType.get(apnType);
        } else {
            NetworkRequestExt networkRequestExt = new NetworkRequestExt();
            networkRequestExt.setNetworkRequest(networkRequest);
            ApnContextEx apnContextEx = this.mHwDcTrackerEx.getNrSliceApnContext(networkRequestExt);
            apnContext = apnContextEx != null ? apnContextEx.getApnContext() : null;
        }
        if (apnContext != null) {
            apnContext.requestNetwork(networkRequest, type, onCompleteMsg);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, int type) {
        ApnContext apnContext;
        int apnType = ApnContext.getApnTypeFromNetworkRequest(networkRequest);
        if (!IS_NR_SLICE_SUPPORTED || apnType != 33554432) {
            apnContext = this.mApnContextsByType.get(apnType);
        } else {
            NetworkRequestExt networkRequestExt = new NetworkRequestExt();
            networkRequestExt.setNetworkRequest(networkRequest);
            ApnContextEx apnContextEx = this.mHwDcTrackerEx.getNrSliceApnContext(networkRequestExt);
            apnContext = apnContextEx != null ? apnContextEx.getApnContext() : null;
        }
        if (apnContext != null) {
            apnContext.releaseNetwork(networkRequest, type);
        }
    }

    public boolean isApnSupported(String name) {
        if (name == null) {
            loge("isApnSupported: name=null");
            return false;
        } else if (this.mApnContexts.get(name) != null) {
            return true;
        } else {
            loge("Request for unsupported mobile name: " + name);
            return false;
        }
    }

    public int getApnPriority(String name) {
        ApnContext apnContext = this.mApnContexts.get(name);
        if (apnContext == null) {
            loge("Request for unsupported mobile name: " + name);
        }
        return apnContext.priority;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRadio(boolean on) {
        try {
            ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).setRadio(on);
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: private */
    public class ProvisionNotificationBroadcastReceiver extends BroadcastReceiver {
        private final String mNetworkOperator;
        private final String mProvisionUrl;

        public ProvisionNotificationBroadcastReceiver(String provisionUrl, String networkOperator) {
            this.mNetworkOperator = networkOperator;
            this.mProvisionUrl = provisionUrl;
        }

        private void setEnableFailFastMobileData(int enabled) {
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessage(dcTracker.obtainMessage(270372, enabled, 0));
        }

        private void enableMobileProvisioning() {
            Message msg = DcTracker.this.obtainMessage(270373);
            msg.setData(Bundle.forPair("provisioningUrl", this.mProvisionUrl));
            DcTracker.this.sendMessage(msg);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            DcTracker.this.log("onReceive : ProvisionNotificationBroadcastReceiver");
            DcTracker.this.mProvisioningSpinner = new ProgressDialog(context);
            DcTracker.this.mProvisioningSpinner.setTitle(this.mNetworkOperator);
            DcTracker.this.mProvisioningSpinner.setMessage(context.getText(17040490));
            DcTracker.this.mProvisioningSpinner.setIndeterminate(true);
            DcTracker.this.mProvisioningSpinner.setCancelable(true);
            DcTracker.this.mProvisioningSpinner.getWindow().setType(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE);
            DcTracker.this.mProvisioningSpinner.show();
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessageDelayed(dcTracker.obtainMessage(270378, dcTracker.mProvisioningSpinner), 120000);
            DcTracker.this.setRadio(true);
            setEnableFailFastMobileData(1);
            enableMobileProvisioning();
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        if (this.mPhone != null) {
            log("finalize");
        }
    }

    private ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new ApnContext(this.mPhone, type, this.mLogTag, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        if (!IS_NR_SLICE_SUPPORTED || !type.contains("snssai")) {
            this.mApnContextsByType.put(ApnSetting.getApnTypesBitmaskFromString(type), apnContext);
        } else {
            ApnContextEx apnContextEx = new ApnContextEx();
            apnContextEx.setApnContext(apnContext);
            this.mHwDcTrackerEx.putApnContextFor5GSlice(ApnSetting.getApnTypesBitmaskFromStringFor5GSlice(type), apnContextEx);
        }
        this.mPrioritySortedApnContexts.add(0, apnContext);
        return apnContext;
    }

    private void initApnContexts() {
        ApnContext apnContext;
        log("initApnContexts: E");
        String[] networkConfigStrings = this.mPhone.getContext().getResources().getStringArray(17236091);
        if (IS_NR_SLICE_SUPPORTED) {
            networkConfigStrings = this.mHwDcTrackerEx.addSliceNetworkConfigStrings(networkConfigStrings);
        }
        if (networkConfigStrings != null) {
            for (String networkConfigString : networkConfigStrings) {
                NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
                if ((IS_NR_SLICE_SUPPORTED || networkConfig.type < 49 || networkConfig.type > 54) && !VSimUtilsInner.isVSimFiltrateApn(this.mPhone.getSubId(), networkConfig.type)) {
                    if (this.mHwCustDcTracker != null) {
                        String apnType = this.mHwDcTrackerEx.networkTypeToApnType(networkConfig.type);
                        if (this.mHwCustDcTracker.isApnTypeDisabled(apnType)) {
                            log("apn type " + apnType + " disabled!");
                        }
                    }
                    int i = networkConfig.type;
                    if (i == 0) {
                        apnContext = addApnContext(TransportManager.IWLAN_OPERATION_MODE_DEFAULT, networkConfig);
                    } else if (i == 1001) {
                        apnContext = addApnContext("mcx", networkConfig);
                    } else if (i == 2) {
                        apnContext = addApnContext("mms", networkConfig);
                    } else if (i == 3) {
                        apnContext = addApnContext("supl", networkConfig);
                    } else if (i == 4) {
                        apnContext = addApnContext("dun", networkConfig);
                    } else if (i == 5) {
                        apnContext = addApnContext("hipri", networkConfig);
                    } else if (i == 14) {
                        apnContext = addApnContext("ia", networkConfig);
                    } else if (i != 15) {
                        switch (i) {
                            case 10:
                                apnContext = addApnContext("fota", networkConfig);
                                break;
                            case 11:
                                apnContext = addApnContext("ims", networkConfig);
                                break;
                            case 12:
                                apnContext = addApnContext("cbs", networkConfig);
                                break;
                            default:
                                switch (i) {
                                    case 38:
                                        apnContext = addApnContext("bip0", networkConfig);
                                        break;
                                    case 39:
                                        apnContext = addApnContext("bip1", networkConfig);
                                        break;
                                    case 40:
                                        apnContext = addApnContext("bip2", networkConfig);
                                        break;
                                    case 41:
                                        apnContext = addApnContext("bip3", networkConfig);
                                        break;
                                    case 42:
                                        apnContext = addApnContext("bip4", networkConfig);
                                        break;
                                    case 43:
                                        apnContext = addApnContext("bip5", networkConfig);
                                        break;
                                    case 44:
                                        apnContext = addApnContext("bip6", networkConfig);
                                        break;
                                    case 45:
                                        apnContext = addApnContext("xcap", networkConfig);
                                        break;
                                    default:
                                        switch (i) {
                                            case 48:
                                                apnContext = addApnContext("internaldefault", networkConfig);
                                                break;
                                            case 49:
                                                apnContext = addApnContext("snssai1", networkConfig);
                                                break;
                                            case 50:
                                                apnContext = addApnContext("snssai2", networkConfig);
                                                break;
                                            case 51:
                                                apnContext = addApnContext("snssai3", networkConfig);
                                                break;
                                            case 52:
                                                apnContext = addApnContext("snssai4", networkConfig);
                                                break;
                                            case 53:
                                                apnContext = addApnContext("snssai5", networkConfig);
                                                break;
                                            case 54:
                                                apnContext = addApnContext("snssai6", networkConfig);
                                                break;
                                            default:
                                                log("initApnContexts: skipping unknown type=" + networkConfig.type);
                                                break;
                                        }
                                }
                        }
                    } else {
                        apnContext = addApnContext("emergency", networkConfig);
                    }
                    log("initApnContexts: apnContext=" + apnContext);
                }
            }
            log("initApnContexts: X mApnContexts=" + this.mApnContexts);
            Collections.sort(this.mPrioritySortedApnContexts, new Comparator<ApnContext>() {
                /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass3 */

                public int compare(ApnContext c1, ApnContext c2) {
                    return c2.priority - c1.priority;
                }
            });
        }
    }

    public LinkProperties getLinkProperties(String apnType) {
        DataConnection dataConnection;
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext == null || (dataConnection = apnContext.getDataConnection()) == null) {
            log("return new LinkProperties");
            return new LinkProperties();
        }
        log("return link properties for " + apnType);
        return dataConnection.getLinkProperties();
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        DataConnection dataConnection;
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext == null || (dataConnection = apnContext.getDataConnection()) == null) {
            log("return new NetworkCapabilities");
            return new NetworkCapabilities();
        }
        log("get active pdp is not null, return NetworkCapabilities for " + apnType);
        return dataConnection.getNetworkCapabilities();
    }

    public String[] getActiveApnTypes() {
        log("get all active apn types");
        ArrayList<String> result = new ArrayList<>();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    public String getActiveApnString(String apnType) {
        ApnSetting apnSetting;
        log("get active apn string for type:" + apnType);
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext == null || (apnSetting = apnContext.getApnSetting()) == null) {
            return null;
        }
        return apnSetting.getApnName();
    }

    public DctConstants.State getState(String apnType) {
        int apnTypeBitmask = ApnSetting.getApnTypesBitmaskFromString(apnType);
        for (DataConnection dc : this.mDataConnections.values()) {
            ApnSetting apnSetting = dc.getApnSetting();
            if (apnSetting != null && apnSetting.canHandleType(apnTypeBitmask)) {
                if (dc.isActive()) {
                    return DctConstants.State.CONNECTED;
                }
                if (dc.isActivating()) {
                    return DctConstants.State.CONNECTING;
                }
                if (dc.isInactive()) {
                    return DctConstants.State.IDLE;
                }
                if (dc.isDisconnecting()) {
                    return DctConstants.State.DISCONNECTING;
                }
            }
        }
        return DctConstants.State.IDLE;
    }

    private boolean isProvisioningApn(String apnType) {
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.isProvisioningApn();
        }
        return false;
    }

    public DctConstants.State getOverallState() {
        boolean isConnecting = false;
        boolean isFailed = true;
        boolean isAnyEnabled = false;
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.isEnabled()) {
                isAnyEnabled = true;
                int i = AnonymousClass5.$SwitchMap$com$android$internal$telephony$DctConstants$State[apnContext.getState().ordinal()];
                if (i == 1 || i == 2) {
                    log("overall state is CONNECTED");
                    return DctConstants.State.CONNECTED;
                } else if (i == 3) {
                    isConnecting = true;
                    isFailed = false;
                } else if (i == 4 || i == 5) {
                    isFailed = false;
                } else {
                    isAnyEnabled = true;
                }
            }
        }
        if (!isAnyEnabled) {
            log("overall state is IDLE");
            return DctConstants.State.IDLE;
        } else if (isConnecting) {
            log("overall state is CONNECTING");
            return DctConstants.State.CONNECTING;
        } else if (!isFailed) {
            log("overall state is IDLE");
            return DctConstants.State.IDLE;
        } else {
            log("overall state is FAILED");
            return DctConstants.State.FAILED;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$5  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$State = new int[DctConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.DISCONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.IDLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.RETRYING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.FAILED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mPhone.notifyDataConnection();
        this.mAttached.set(false);
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(false);
        if (getOverallState() == DctConstants.State.CONNECTED) {
            this.mHwDcTrackerEx.startPdpResetAlarm(PDP_RESET_ALARM_DELAY_IN_MS);
        }
    }

    private void onDataConnectionAttached() {
        int i;
        log("onDataConnectionAttached");
        this.mAttached.set(true);
        this.mHwDcTrackerEx.stopPdpResetAlarm();
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(false);
        if (getOverallState() == DctConstants.State.CONNECTED) {
            log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(false);
            this.mPhone.notifyDataConnection();
        }
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachEnabled.set(true);
        }
        if (!(!this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId()) || (i = this.preSetupBasedRadioTech) == 0 || i == this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            log("onDataConnectionAttached need to clear ApnContext, preSetupBasedRadioTech: " + this.preSetupBasedRadioTech);
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == DctConstants.State.RETRYING) {
                    apnContext.setState(DctConstants.State.IDLE);
                    cancelReconnectAlarm(apnContext);
                }
            }
        }
        setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_DATA_ATTACHED, RetryFailures.ALWAYS);
    }

    public boolean isDataAllowed(DataConnectionReasons dataConnectionReasons) {
        return isDataAllowed(null, 1, dataConnectionReasons);
    }

    /* JADX WARNING: Removed duplicated region for block: B:109:0x021d  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0224  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0232  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x0239  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x0246  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0261  */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x027a  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x02ab  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x02d8  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x02df  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00fb  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0106  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x010a  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0129  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x016f  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x017e  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01a9  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x01da  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x01e1  */
    public boolean isDataAllowed(ApnContext apnContext, int requestType, DataConnectionReasons dataConnectionReasons) {
        String str;
        boolean isMeteredApnType;
        PhoneConstants.State phoneState;
        int slotId;
        boolean isMeteredApnType2;
        boolean isDataEnabled;
        boolean z;
        HwCustDcTracker hwCustDcTracker;
        DataConnectionReasons reasons = new DataConnectionReasons();
        boolean internalDataEnabled = this.mDataEnabledSettings.isInternalDataEnabled();
        boolean attachedState = this.mHwDcTrackerEx.getAttachedStatus(this.mAttached.get());
        boolean desiredPowerState = this.mPhone.getServiceStateTracker().getDesiredPowerState();
        boolean radioStateFromCarrier = this.mPhone.getServiceStateTracker().getPowerStateFromCarrier();
        int dataRat = getDataRat();
        boolean isMtkVowifiMms = this.mPhone.getImsRegistrationTech() == 1 && apnContext != null && "mms".equals(apnContext.getApnType()) && HuaweiTelephonyConfigs.isMTKPlatform();
        if (dataRat == 18 || isMtkVowifiMms) {
            desiredPowerState = true;
            radioStateFromCarrier = true;
        }
        boolean recordsLoaded = this.mIccRecords.get() != null && (this.mIccRecords.get().getRecordsLoaded() || this.mIccRecords.get().getImsiReady());
        if (this.mIccRecords.get() != null && !recordsLoaded) {
            log("isDataAllowed getImsiReady=" + this.mIccRecords.get().getImsiReady());
        }
        boolean isDataAllowedVoWiFi = !HuaweiTelephonyConfigs.isHisiPlatform() && dataRat == 18;
        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        int slotId2 = SubscriptionController.getInstance().getSlotIndex(dataSub);
        boolean defaultDataSelected = SubscriptionManager.isValidSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (VSimUtilsInner.isVSimEnabled()) {
            defaultDataSelected = true;
        }
        if (apnContext != null) {
            str = "mms";
            if (!ApnSettingUtils.isMeteredApnType(ApnSetting.getApnTypesBitmaskFromString(apnContext.getApnType()), this.mPhone)) {
                isMeteredApnType = false;
                PhoneConstants.State phoneState2 = PhoneConstants.State.IDLE;
                if (this.mPhone.getCallTracker() == null) {
                    phoneState = this.mPhone.getCallTracker().getState();
                } else {
                    phoneState = phoneState2;
                }
                if (apnContext == null) {
                    isMeteredApnType2 = isMeteredApnType;
                    slotId = slotId2;
                    if (apnContext.getApnType().equals("emergency") && apnContext.isConnectable()) {
                        if (dataConnectionReasons == null) {
                            return true;
                        }
                        dataConnectionReasons.add(DataConnectionReasons.DataAllowedReasonType.EMERGENCY_APN);
                        return true;
                    }
                } else {
                    isMeteredApnType2 = isMeteredApnType;
                    slotId = slotId2;
                }
                if (apnContext != null && !apnContext.isConnectable()) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.APN_NOT_CONNECTABLE);
                }
                if (apnContext != null && ((apnContext.getApnType().equals(TransportManager.IWLAN_OPERATION_MODE_DEFAULT) || apnContext.getApnType().equals("ia")) && this.mPhone.getTransportManager().isInLegacyMode() && dataRat == 18)) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.ON_IWLAN);
                }
                if (isEmergency()) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.IN_ECBM);
                }
                ApnContextEx apnContextEx = new ApnContextEx();
                apnContextEx.setApnContext(apnContext);
                if (attachedState) {
                    if (!shouldAutoAttach() || this.mPhone.getSubId() != dataSub) {
                        if (requestType != 2 && !this.mHwDcTrackerEx.isNeedForceSetup(apnContextEx) && !isMtkVowifiMms) {
                            reasons.add(DataConnectionReasons.DataDisallowedReasonType.NOT_ATTACHED);
                        }
                    }
                }
                if (!recordsLoaded && !this.mHwDcTrackerEx.isNeedForceSetup(apnContextEx) && !isDataAllowedVoWiFi) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.RECORD_NOT_LOADED);
                }
                if (phoneState != PhoneConstants.State.IDLE && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.INVALID_PHONE_STATE);
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.CONCURRENT_VOICE_DATA_NOT_ALLOWED);
                }
                if (!internalDataEnabled) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
                }
                if (!defaultDataSelected) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.DEFAULT_DATA_UNSELECTED);
                }
                if (!this.mHwDcTrackerEx.isDataAllowedForRoaming(isMmsApn(apnContext)) && ((!isXcapApn(apnContext) || !this.mHwDcTrackerEx.getXcapDataRoamingEnable()) && this.mPhone.getServiceState().getDataRoaming() && !getDataRoamingEnabled())) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
                }
                if (this.mIsPsRestricted) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.PS_RESTRICTED);
                }
                if (!desiredPowerState) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.UNDESIRED_POWER_STATE);
                }
                if (!radioStateFromCarrier) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.RADIO_DISABLED_BY_CARRIER);
                }
                if (apnContext != null) {
                    isDataEnabled = this.mDataEnabledSettings.isDataEnabled();
                } else {
                    isDataEnabled = this.mDataEnabledSettings.isDataEnabled(apnContext.getApnTypeBitmask());
                }
                if (!isDataEnabled) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED);
                }
                HwCustDcTracker hwCustDcTracker2 = this.mHwCustDcTracker;
                if (hwCustDcTracker2 != null && !hwCustDcTracker2.isPsAllowedByFdn()) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.PS_RESTRICTED_BY_FDN);
                }
                if (slotId != 1) {
                    if (this.mHwDcTrackerEx.isDataConnectivityDisabled(1, "disable-data")) {
                        reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
                        cleanUpAllConnections("dataDisabledInternal");
                    }
                }
                HwCustDcTracker hwCustDcTracker3 = this.mHwCustDcTracker;
                if (hwCustDcTracker3 != null && hwCustDcTracker3.isDataDisableBySim2() && SubscriptionManager.getSlotIndex(this.mPhone.getSubId()) == 1) {
                    log("isDataAllowed sim2 data disable by cust");
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
                }
                if (isDataDisable(this.mPhone.getPhoneId())) {
                    log("ais custom version but not ais card, disable the data.");
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
                }
                if (HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave() && this.mHwDcTrackerEx.getPrimarySlot() == this.mPhone.getPhoneId()) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
                }
                if (reasons.containsHardDisallowedReasons()) {
                    if (!reasons.allowed()) {
                        if (this.mTransportType == 2 || (this.mPhone.getTransportManager().isInLegacyMode() && dataRat == 18)) {
                            reasons.add(DataConnectionReasons.DataAllowedReasonType.UNMETERED_APN);
                        } else if (this.mTransportType == 1 && !isMeteredApnType2) {
                            reasons.add(DataConnectionReasons.DataAllowedReasonType.UNMETERED_APN);
                        }
                        if (apnContext != null) {
                            if (apnContext.hasRestrictedRequests(true) && reasons.contains(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED)) {
                                reasons.add(DataConnectionReasons.DataAllowedReasonType.RESTRICTED_REQUEST);
                            }
                        }
                        if (apnContext != null && (hwCustDcTracker = this.mHwCustDcTracker) != null && hwCustDcTracker.isDataAllowedForSES(apnContext.getApnType()) && reasons.contains(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED)) {
                            reasons.add(DataConnectionReasons.DataAllowedReasonType.RESTRICTED_REQUEST);
                        }
                        if (apnContext == null || reasons.allowed()) {
                            z = false;
                        } else {
                            z = false;
                            if (this.mHwDcTrackerEx.getAnyDataEnabledByApnContext(apnContextEx, false)) {
                                reasons.add(DataConnectionReasons.DataAllowedReasonType.NORMAL);
                            }
                        }
                        if ((this.mHwCustDcTracker != null && apnContext != null && reasons.allowed() && this.mHwCustDcTracker.isRoamDisallowedByCustomization(apnContext)) || (apnContext != null && str.equals(apnContext.getApnType()) && this.mPhone.getServiceState().getDataRoaming() && this.mHwDcTrackerEx.isRoamingPushDisabled())) {
                            reasons.add(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
                        }
                    } else {
                        z = false;
                        reasons.add(DataConnectionReasons.DataAllowedReasonType.NORMAL);
                    }
                    if (dataConnectionReasons != null) {
                        dataConnectionReasons.copyFrom(reasons);
                    }
                    boolean dataAllowedByApnContext = true;
                    if (apnContext != null) {
                        dataAllowedByApnContext = this.mHwDcTrackerEx.isDataAllowedByApnContext(apnContextEx);
                    }
                    if (!reasons.allowed() || !dataAllowedByApnContext) {
                        return z;
                    }
                    return true;
                } else if (dataConnectionReasons == null) {
                    return false;
                } else {
                    dataConnectionReasons.copyFrom(reasons);
                    return false;
                }
            }
        } else {
            str = "mms";
        }
        isMeteredApnType = true;
        PhoneConstants.State phoneState22 = PhoneConstants.State.IDLE;
        if (this.mPhone.getCallTracker() == null) {
        }
        if (apnContext == null) {
        }
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.APN_NOT_CONNECTABLE);
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.ON_IWLAN);
        if (isEmergency()) {
        }
        ApnContextEx apnContextEx2 = new ApnContextEx();
        apnContextEx2.setApnContext(apnContext);
        if (attachedState) {
        }
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.RECORD_NOT_LOADED);
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.INVALID_PHONE_STATE);
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.CONCURRENT_VOICE_DATA_NOT_ALLOWED);
        if (!internalDataEnabled) {
        }
        if (!defaultDataSelected) {
        }
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
        if (this.mIsPsRestricted) {
        }
        if (!desiredPowerState) {
        }
        if (!radioStateFromCarrier) {
        }
        if (apnContext != null) {
        }
        if (!isDataEnabled) {
        }
        HwCustDcTracker hwCustDcTracker22 = this.mHwCustDcTracker;
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.PS_RESTRICTED_BY_FDN);
        if (slotId != 1) {
        }
        HwCustDcTracker hwCustDcTracker32 = this.mHwCustDcTracker;
        log("isDataAllowed sim2 data disable by cust");
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
        if (isDataDisable(this.mPhone.getPhoneId())) {
        }
        reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
        if (reasons.containsHardDisallowedReasons()) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setupDataOnAllConnectableApns(String reason, RetryFailures retryFailures) {
        log("setupDataOnAllConnectableApns: " + reason);
        StringBuilder sb = new StringBuilder(120);
        Iterator<ApnContext> it = this.mPrioritySortedApnContexts.iterator();
        while (it.hasNext()) {
            ApnContext apnContext = it.next();
            sb.append(apnContext.getApnType());
            sb.append(":[state=");
            sb.append(apnContext.getState());
            sb.append(",enabled=");
            sb.append(apnContext.isEnabled());
            sb.append("] ");
        }
        log("setupDataOnAllConnectableApns: " + reason + " " + ((Object) sb));
        if (!getmIsPseudoImsi() || reason.equals("SetPSOnlyOK")) {
            Iterator<ApnContext> it2 = this.mPrioritySortedApnContexts.iterator();
            while (it2.hasNext()) {
                setupDataOnConnectableApn(it2.next(), reason, retryFailures);
                if (this.mIsPseudoImsi) {
                    this.mIsPseudoImsi = false;
                    return;
                }
            }
            return;
        }
        log("getmIsPseudoImsi(): " + getmIsPseudoImsi() + "  reason: " + reason);
    }

    private void setupDataOnConnectableApn(ApnContext apnContext, String reason, RetryFailures retryFailures) {
        if (HW_DBG) {
            log("setupDataOnAllConnectableApns: apnContext " + apnContext);
        }
        if (apnContext.getState() == DctConstants.State.FAILED || apnContext.getState() == DctConstants.State.RETRYING) {
            if (retryFailures == RetryFailures.ALWAYS) {
                apnContext.releaseDataConnection(reason);
            } else if (!apnContext.isConcurrentVoiceAndDataAllowed() && this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                apnContext.releaseDataConnection(reason);
            }
        }
        if (isDefaultDataSubscription() && !apnContext.isEnabled() && "simLoaded".equals(reason) && TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType()) && this.mHwDcTrackerEx.isDataNeededWithWifiAndBt()) {
            boolean hasMatchAll = false;
            if (IS_NR_SLICE_SUPPORTED && this.mHwDcTrackerEx.hasMatchAllSlice()) {
                hasMatchAll = true;
            }
            if (!hasMatchAll) {
                log("setupDataOnConnectableApns: for IMSI done, call setEnabled");
                apnContext.setEnabled(true);
            }
        }
        if (apnContext.isConnectable()) {
            log("isConnectable() call trySetupData");
            if (!getmIsPseudoImsi() || apnContext.getApnType().equals("bip0")) {
                this.preSetupBasedRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                log("setupDataOnConnectableApns: current radio technology: " + this.preSetupBasedRadioTech);
                apnContext.setReason(reason);
                trySetupData(apnContext, 1);
                if (getmIsPseudoImsi()) {
                    log("setupDataOnConnectableApns: pseudo imsi single connection only");
                    this.mIsPseudoImsi = true;
                    return;
                }
                HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(true);
                return;
            }
            return;
        }
        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, this.mHwDcTrackerEx.isWifiConnected(), this.mDataEnabledSettings.isDataEnabled(), apnContext);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmergency() {
        boolean result = this.mPhone.isInEcm() || this.mPhone.isInEmergencyCall();
        log("isEmergency: result=" + result);
        return result;
    }

    private boolean trySetupData(ApnContext apnContext, int requestType) {
        if (IS_NR_SLICE_SUPPORTED && apnContext.isNrSliceApnContext() && TelephonyManager.getDefault().getNetworkType() != 20) {
            return false;
        }
        HwDataConnectionManager hwDataConnectionManager = this.mHwDcManager;
        if (hwDataConnectionManager != null && hwDataConnectionManager.getNamSwitcherForSoftbank() && this.mHwDcManager.isSoftBankCard(this.mPhoneExt) && !this.mHwDcManager.isValidMsisdn(this.mPhoneExt)) {
            log("trySetupData sbnam not allow activate data if MSISDN of softbank card is empty  !");
            return false;
        } else if (this.mHwDcTrackerEx.isDataConnectivityDisabled(this.mPhone.getPhoneId(), "disable-data")) {
            return false;
        } else {
            int voiceState = this.mPhone.getServiceState().getVoiceRegState();
            int dataState = this.mPhone.getServiceState().getDataRegState();
            log("dataState = " + dataState + "voiceState = " + voiceState);
            if (TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType()) && (dataState == 0 || voiceState == 0)) {
                this.mPreferredApn = this.mHwDcTrackerEx.getApnForCT();
                log("get prefered dp for CT " + this.mPreferredApn);
                if (this.mPreferredApn == null) {
                    this.mPreferredApn = getPreferredApn();
                }
                log("get prefered DP " + this.mPreferredApn);
            }
            if (!VSimUtilsInner.isVSimEnabled() || VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId()) || "mms".equals(apnContext.getApnType())) {
                boolean isQcomDualLteImsApn = PhoneFactory.IS_QCOM_DUAL_LTE_STACK && PhoneFactory.IS_DUAL_VOLTE_SUPPORTED && "ims".equals(apnContext.getApnType());
                int soft_switch = Settings.System.getInt(this.mResolver, TELEPHONY_SOFT_SWITCH, 0);
                if (!isDefaultDataSubscription() && !"mms".equals(apnContext.getApnType()) && !"xcap".equals(apnContext.getApnType()) && !isQcomDualLteImsApn && soft_switch != 1 && !isDualPsAllowedForSmartSwitch()) {
                    log("trySetupData not allowed on non defaultDds except mms or xcap or qcomDualLte ims or soft switch.");
                    return false;
                } else if (this.mPhone.getSimulatedRadioControl() != null) {
                    apnContext.setState(DctConstants.State.CONNECTED);
                    this.mPhone.notifyDataConnection(apnContext.getApnType());
                    log("trySetupData: X We're on the simulator; assuming connected retValue=true");
                    return true;
                } else {
                    DataConnectionReasons dataConnectionReasons = new DataConnectionReasons();
                    boolean isDataAllowed = isDataAllowed(apnContext, requestType, dataConnectionReasons);
                    String logStr = "trySetupData for APN type " + apnContext.getApnType() + ", reason: " + apnContext.getReason() + ", requestType=" + requestTypeToString(requestType) + ". " + dataConnectionReasons.toString();
                    log(logStr);
                    apnContext.requestLog(logStr);
                    if (getmIsPseudoImsi() || isDataAllowed) {
                        if (apnContext.getState() == DctConstants.State.FAILED) {
                            log("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                            apnContext.requestLog("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                            apnContext.setState(DctConstants.State.IDLE);
                        }
                        int radioTech = getDataRat();
                        if (radioTech == 0) {
                            radioTech = getVoiceRat();
                        }
                        log("service state=" + this.mPhone.getServiceState());
                        apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                        if (apnContext.getState() == DctConstants.State.IDLE) {
                            ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                            if (waitingApns.isEmpty()) {
                                notifyNoData(27, apnContext);
                                log("trySetupData: X No APN found retValue=false");
                                apnContext.requestLog("trySetupData: X No APN found retValue=false");
                                return false;
                            }
                            apnContext.setWaitingApns(waitingApns);
                            log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
                        }
                        boolean retValue = setupData(apnContext, radioTech, requestType);
                        log("trySetupData: X retValue=" + retValue);
                        return retValue;
                    }
                    HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, this.mHwDcTrackerEx.isWifiConnected(), this.mDataEnabledSettings.isDataEnabled(), apnContext);
                    StringBuilder str = new StringBuilder();
                    str.append("trySetupData failed. apnContext = [type=" + apnContext.getApnType() + ", mState=" + apnContext.getState() + ", apnEnabled=" + apnContext.isEnabled() + ", mDependencyMet=" + apnContext.isDependencyMet() + "] ");
                    if (!this.mDataEnabledSettings.isDataEnabled()) {
                        str.append("isDataEnabled() = false. " + this.mDataEnabledSettings);
                    }
                    if (apnContext.getState() == DctConstants.State.RETRYING) {
                        apnContext.setState(DctConstants.State.FAILED);
                        str.append(" Stop retrying.");
                    }
                    log(str.toString());
                    apnContext.requestLog(str.toString());
                    return false;
                }
            } else {
                log("trySetupData not allowed vsim is on for non vsim Dds except mms is enabled");
                return false;
            }
        }
    }

    public void cleanUpAllConnections(String reason) {
        log("cleanUpAllConnections");
        Message msg = obtainMessage(270365);
        msg.obj = reason;
        sendMessage(msg);
    }

    private boolean cleanUpAllConnectionsInternal(boolean detach, String reason) {
        log("cleanUpAllConnectionsInternal: detach=" + detach + " reason=" + reason);
        boolean didDisconnect = false;
        boolean disableMeteredOnly = false;
        if (!TextUtils.isEmpty(reason)) {
            disableMeteredOnly = reason.equals(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED) || reason.equals("roamingOn") || reason.equals(PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (reason == null || !reason.equals("SinglePdnArbitration") || !apnContext.getApnType().equals("ims")) {
                if (apnContext.isConnectable() || !shouldCleanUpConnection(apnContext, disableMeteredOnly)) {
                    log("cleanUpAllConnectionsInternal: APN type " + apnContext.getApnType() + " shouldn't be cleaned up.");
                } else {
                    if (!apnContext.isDisconnected()) {
                        didDisconnect = true;
                    }
                    apnContext.setReason(reason);
                    cleanUpConnectionInternal(detach, 2, apnContext);
                }
            }
        }
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mRequestedApnType = 17;
        log("cleanUpAllConnectionsInternal: mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (detach && this.mDisconnectPendingCount == 0) {
            notifyAllDataDisconnected();
        }
        return didDisconnect;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldCleanUpConnection(ApnContext apnContext, boolean disableMeteredOnly) {
        if (apnContext == null) {
            return false;
        }
        if (!disableMeteredOnly) {
            return true;
        }
        ApnSetting apnSetting = apnContext.getApnSetting();
        if (apnSetting == null || !ApnSettingUtils.isMetered(apnSetting, this.mPhone)) {
            return false;
        }
        boolean isRoaming = this.mPhone.getServiceState().getDataRoaming();
        boolean isDataRoamingDisabled = !getDataRoamingEnabled();
        if ((!this.mDataEnabledSettings.isDataEnabled(apnSetting.getApnTypeBitmask())) || (isRoaming && isDataRoamingDisabled)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void cleanUpConnection(ApnContext apnContext) {
        log("cleanUpConnection: apnContext=" + apnContext);
        Message msg = obtainMessage(270360);
        msg.arg2 = 0;
        msg.obj = apnContext;
        sendMessage(msg);
    }

    private void cleanUpConnectionInternal(boolean detach, int releaseType, ApnContext apnContext) {
        if (apnContext == null) {
            log("cleanUpConnectionInternal: apn context is null");
            return;
        }
        DataConnection dataConnection = apnContext.getDataConnection();
        String str = "cleanUpConnectionInternal: detach=" + detach + " reason=" + apnContext.getReason();
        if (HW_DBG) {
            log(str + " apnContext=" + apnContext);
        }
        apnContext.requestLog(str);
        if (detach) {
            boolean isDisconnected = apnContext.isDisconnected();
            String str2 = PhoneConfigurationManager.SSSS;
            if (isDisconnected) {
                apnContext.releaseDataConnection(str2);
            } else if (dataConnection == null) {
                apnContext.setState(DctConstants.State.IDLE);
                apnContext.requestLog("cleanUpConnectionInternal: connected, bug no dc");
                this.mPhone.notifyDataConnection(apnContext.getApnType());
            } else if (apnContext.getState() != DctConstants.State.DISCONNECTING) {
                boolean disconnectAll = false;
                if ("dun".equals(apnContext.getApnType()) && ServiceState.isCdma(getDataRat())) {
                    log("cleanUpConnectionInternal: disconnectAll DUN connection");
                    disconnectAll = true;
                }
                int generation = apnContext.getConnectionGeneration();
                StringBuilder sb = new StringBuilder();
                sb.append("cleanUpConnectionInternal: tearing down");
                if (disconnectAll) {
                    str2 = " all";
                }
                sb.append(str2);
                sb.append(" using gen#");
                sb.append(generation);
                String str3 = sb.toString();
                if (HW_DBG) {
                    log(str3 + "apnContext=" + apnContext);
                }
                apnContext.requestLog(str3);
                Message msg = obtainMessage(270351, new Pair<>(apnContext, Integer.valueOf(generation)));
                if (disconnectAll || releaseType == 3) {
                    dataConnection.tearDownAll(apnContext.getReason(), releaseType, msg);
                } else {
                    dataConnection.tearDown(apnContext, apnContext.getReason(), msg);
                }
                apnContext.setState(DctConstants.State.DISCONNECTING);
                this.mDisconnectPendingCount++;
            }
        } else if (!PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(apnContext.getReason()) || apnContext.getState() != DctConstants.State.CONNECTING) {
            if (dataConnection != null) {
                dataConnection.reset();
            }
            apnContext.setState(DctConstants.State.IDLE);
            this.mPhone.notifyDataConnection(apnContext.getApnType(), PhoneConstants.DataState.DISCONNECTED);
            apnContext.setDataConnection(null);
        } else {
            log("ignore the set IDLE message, because the current state is connecting!");
        }
        this.mHwDcTrackerEx.setupDataForSinglePdnArbitration(apnContext.getReason());
        if (dataConnection != null) {
            cancelReconnectAlarm(apnContext);
        }
        String str4 = "cleanUpConnectionInternal: X detach=" + detach + " reason=" + apnContext.getReason();
        if (HW_DBG) {
            log(str4 + " apnContext=" + apnContext + " dc=" + apnContext.getDataConnection());
        }
    }

    @VisibleForTesting
    public ArrayList<ApnSetting> fetchDunApns() {
        int i = 0;
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            log("fetchDunApns: net.tethering.noprovisioning=true ret: empty list");
            return new ArrayList<>(0);
        } else if (VSimUtilsInner.isVSimEnabled()) {
            log("fetchDunApns: vsim is enable, return empty list!.");
            return new ArrayList<>(0);
        } else {
            int bearer = getDataRat();
            ArrayList<ApnSetting> dunCandidates = new ArrayList<>();
            ArrayList<ApnSetting> retDunSettings = new ArrayList<>();
            ApnSetting preferredApn = getPreferredApn();
            HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
            if (hwCustDcTracker == null || !hwCustDcTracker.isDocomoApn(preferredApn)) {
                String apnData = Settings.Global.getString(this.mResolver, "tether_dun_apn");
                if (!TextUtils.isEmpty(apnData)) {
                    dunCandidates.addAll(ApnSetting.arrayFromString(apnData));
                    log("fetchDunApns: dunCandidates from Setting: " + dunCandidates);
                }
                if (preferredApn != null) {
                    String[] typeList = ApnSetting.getApnTypesStringFromBitmask(preferredApn.getApnTypeBitmask()).split(",");
                    int length = typeList.length;
                    while (true) {
                        if (i >= length) {
                            break;
                        } else if (typeList[i].contains("dun")) {
                            dunCandidates.add(preferredApn);
                            log("fetchDunApn: add preferredApn");
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                if (dunCandidates.isEmpty() && !ArrayUtils.isEmpty(this.mAllApnSettings)) {
                    for (ApnSetting apn : this.mAllApnSettings) {
                        if (apn.canHandleType(8) && apn.getApnTypeBitmask() != 8356095) {
                            dunCandidates.add(apn);
                        }
                    }
                    log("fetchDunApns: dunCandidates from database: " + dunCandidates);
                }
                Iterator<ApnSetting> it = dunCandidates.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ApnSetting dunSetting = it.next();
                    if (dunSetting.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(bearer))) {
                        HwCustDcTracker hwCustDcTracker2 = this.mHwCustDcTracker;
                        if (hwCustDcTracker2 == null || !hwCustDcTracker2.addSpecifiedApnSwitch()) {
                            retDunSettings.add(dunSetting);
                        } else if (this.mHwCustDcTracker.addSpecifiedApnToWaitingApns(this, preferredApn, dunSetting)) {
                            retDunSettings.add(dunSetting);
                            break;
                        }
                    }
                }
                log("fetchDunApns: dunSettings=" + retDunSettings);
                return retDunSettings;
            }
            ApnSetting retDunSetting = this.mHwCustDcTracker.getDocomoApn(preferredApn);
            if (retDunSetting != null) {
                retDunSettings.add(retDunSetting);
            }
            return retDunSettings;
        }
    }

    private int getPreferredApnSetId() {
        int setId;
        ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
        Uri uri = Telephony.Carriers.CONTENT_URI;
        Cursor c = contentResolver.query(Uri.withAppendedPath(uri, "preferapnset/subId/" + this.mPhone.getSubId()), new String[]{"apn_set_id"}, null, null, null);
        if (c == null) {
            loge("getPreferredApnSetId: cursor is null");
            return 0;
        }
        if (c.getCount() < 1) {
            loge("getPreferredApnSetId: no APNs found");
            setId = 0;
        } else {
            c.moveToFirst();
            setId = c.getInt(0);
        }
        if (!c.isClosed()) {
            c.close();
        }
        return setId;
    }

    public boolean hasMatchedTetherApnSetting() {
        ArrayList<ApnSetting> matches = fetchDunApns();
        log("hasMatchedTetherApnSetting: APNs=" + matches);
        return matches.size() > 0;
    }

    public DataConnection getDataConnectionByContextId(int cid) {
        return this.mDcc.getActiveDcByCid(cid);
    }

    public DataConnection getDataConnectionByApnType(String apnType) {
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getDataConnection();
        }
        return null;
    }

    private void cancelReconnectAlarm(ApnContext apnContext) {
        PendingIntent intent;
        if (apnContext != null && (intent = apnContext.getReconnectIntent()) != null) {
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(intent);
            apnContext.setReconnectIntent(null);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPermanentFailure(int dcFailCause) {
        return DataFailCause.isPermanentFailure(this.mPhone.getContext(), dcFailCause, this.mPhone.getSubId()) && (!this.mAttached.get() || dcFailCause != -3);
    }

    private DataConnection findFreeDataConnection() {
        for (DataConnection dataConnection : this.mDataConnections.values()) {
            boolean inUse = false;
            Iterator<ApnContext> it = this.mApnContexts.values().iterator();
            while (true) {
                if (it.hasNext()) {
                    if (it.next().getDataConnection() == dataConnection) {
                        inUse = true;
                        continue;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!inUse) {
                log("findFreeDataConnection: found free DataConnection=" + dataConnection);
                return dataConnection;
            }
        }
        log("findFreeDataConnection: NO free DataConnection");
        return null;
    }

    private boolean setupData(ApnContext apnContext, int radioTech, int requestType) {
        int profileId;
        ApnSetting apnSetting;
        DataConnection dataConnection;
        if (HW_DBG) {
            log("setupData: apnContext=" + apnContext + ", requestType=" + requestTypeToString(requestType));
        }
        apnContext.requestLog("setupData. requestType=" + requestTypeToString(requestType));
        DataConnection dataConnection2 = null;
        ApnSetting apnSetting2 = apnContext.getNextApnSetting();
        if (apnSetting2 == null) {
            log("setupData: return for no apn found!");
            return false;
        }
        if (apnSetting2.isPersistent() || HuaweiTelephonyConfigs.isMTKPlatform()) {
            int profileId2 = apnSetting2.getProfileId();
            if (profileId2 == 0) {
                profileId = getApnProfileID(apnContext.getApnType());
            } else {
                profileId = profileId2;
            }
        } else {
            profileId = -1;
        }
        if (!apnContext.getApnType().equals("dun") || ServiceState.isGsm(getDataRat())) {
            dataConnection2 = checkForCompatibleConnectedApnContext(apnContext);
            if (dataConnection2 != null) {
                ApnSetting dataConnectionApnSetting = dataConnection2.getApnSetting();
                if (dataConnectionApnSetting != null) {
                    apnSetting2 = dataConnectionApnSetting;
                }
                ArrayList<ApnSetting> tmpList = new ArrayList<>();
                tmpList.add(apnSetting2);
                HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
                if (hwCustDcTracker == null || !hwCustDcTracker.hasBetterApnByBearer(dataConnection2.getApnSetting(), tmpList, apnContext.getApnType(), radioTech)) {
                    apnSetting = apnSetting2;
                } else {
                    log("setupData: compatible dcac is not best, no use");
                    dataConnection2 = null;
                    apnSetting = apnSetting2;
                }
            } else {
                apnSetting = apnSetting2;
            }
        } else {
            apnSetting = apnSetting2;
        }
        if (dataConnection2 == null) {
            if (isOnlySingleDcAllowed(radioTech)) {
                if (isHigherPriorityApnContextActive(apnContext)) {
                    log("setupData: Higher priority ApnContext active.  Ignoring call");
                    return false;
                } else if (apnContext.getApnType().equals("ims") || !cleanUpAllConnectionsInternal(true, "SinglePdnArbitration")) {
                    log("setupData: Single pdp. Continue setting up data call.");
                } else {
                    log("setupData: Some calls are disconnecting first. Wait and retry");
                    return false;
                }
            }
            DataConnection dataConnection3 = findFreeDataConnection();
            if (dataConnection3 == null) {
                dataConnection3 = createDataConnection();
            }
            if (dataConnection3 == null) {
                log("setupData: No free DataConnection and couldn't create one, WEIRD");
                return false;
            }
            dataConnection = dataConnection3;
        } else {
            dataConnection = dataConnection2;
        }
        int generation = apnContext.incAndGetConnectionGeneration();
        if (HW_DBG) {
            log("setupData: dc=" + dataConnection + " apnSetting=" + apnSetting + " gen#=" + generation);
        }
        apnContext.setDataConnection(dataConnection);
        apnContext.setApnSetting(apnSetting);
        apnContext.setState(DctConstants.State.CONNECTING);
        ApnContextEx apnContextEx = new ApnContextEx();
        apnContextEx.setApnContext(apnContext);
        this.mHwDcTrackerEx.updateDataCureProtocol(apnContextEx);
        this.mPhone.notifyDataConnection(apnContext.getApnType());
        Message msg = obtainMessage();
        msg.what = 270336;
        msg.obj = new Pair(apnContext, Integer.valueOf(generation));
        HwTelephonyFactory.getHwDataServiceChrManager().setBringUp(true);
        dataConnection.bringUp(apnContext, profileId, radioTech, msg, generation, requestType, this.mPhone.getSubId());
        log("setupData: initing!");
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v3, resolved type: com.android.internal.telephony.dataconnection.IHwDcTrackerEx */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r2v37 java.lang.String: [D('defaultApnSetting' android.telephony.data.ApnSetting), D('plmn' java.lang.String)] */
    /* JADX WARN: Type inference failed for: r5v4, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void setInitialAttachApn() {
        ApnSetting defaultApnSetting;
        ApnSetting iaApnSetting;
        int esmFlag;
        ApnSetting iaApnSetting2;
        boolean z;
        ApnSetting defaultApnSetting2 = null;
        ApnSetting firstNonEmergencyApnSetting = null;
        if (this.mHwDcTrackerEx.getPrimarySlot() != this.mPhone.getPhoneId() && !HwModemCapability.isCapabilitySupport(21)) {
            log("setInitialAttachApn: not 4g slot , skip");
            if (IS_DELAY_ATTACH_ENABLED) {
                log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                this.mPhone.mCi.dataConnectionAttach(1, null);
            }
        } else if (SystemProperties.getBoolean("persist.radio.iot_attach_apn", false)) {
            log("setInitialAttachApn: iot attach apn enabled, skip");
        } else {
            ?? r5 = IS_ENABLE_ESMFLAG_CURE;
            int isEsmFlagCustomized = 0;
            boolean esmFlagAdaptionEnabled = false;
            HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
            if (hwCustDcTracker != null) {
                esmFlagAdaptionEnabled = hwCustDcTracker.getEsmFlagAdaptionEnabled();
            }
            String operatorDataCure = "null";
            if (esmFlagAdaptionEnabled) {
                int esmFlagFromCard = this.mHwCustDcTracker.getEsmFlagFromCard();
                if (esmFlagFromCard != -1) {
                    esmFlag = esmFlagFromCard;
                    isEsmFlagCustomized = 1;
                    iaApnSetting = null;
                    defaultApnSetting = null;
                } else {
                    String plmnsConfig = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "plmn_esm_flag");
                    log("setInitialAttachApn: plmnsConfig = " + plmnsConfig);
                    IccRecords r = this.mIccRecords.get();
                    String operator = r != null ? r.getOperatorNumeric() : operatorDataCure;
                    if (plmnsConfig != null) {
                        String[] plmns = plmnsConfig.split(",");
                        int length = plmns.length;
                        iaApnSetting = null;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                defaultApnSetting = defaultApnSetting2;
                                esmFlag = r5;
                                break;
                            }
                            defaultApnSetting = defaultApnSetting2;
                            String plmn = plmns[i];
                            if (plmn != null && plmn.equals(operator)) {
                                log("setInitialAttachApn: send initial attach apn for operator " + operator);
                                esmFlag = 1;
                                isEsmFlagCustomized = 1;
                                break;
                            }
                            i++;
                            defaultApnSetting2 = defaultApnSetting;
                        }
                    } else {
                        iaApnSetting = null;
                        defaultApnSetting = null;
                        esmFlag = r5;
                    }
                }
            } else {
                iaApnSetting = null;
                defaultApnSetting = null;
                esmFlag = r5;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("defaultEsmFlag:");
            int esmFlag2 = r5 == true ? 1 : 0;
            int esmFlag3 = r5 == true ? 1 : 0;
            int esmFlag4 = r5 == true ? 1 : 0;
            sb.append(esmFlag2);
            sb.append("; isEsmFlagCustomized:");
            sb.append(isEsmFlagCustomized);
            log(sb.toString());
            this.mHwDcTrackerEx.updateCustomizedEsmFlagState(r5, isEsmFlagCustomized);
            int esmFlag5 = esmFlag;
            if (this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId())) {
                log("setInitialAttachApn: send initial attach apn for CT");
                esmFlag5 = 1;
            }
            int esmFlag6 = esmFlag5;
            if (esmFlag5 == 0) {
                ApnSetting apnSetting = this.mPreferredApn;
                esmFlag6 = esmFlag5;
                if (apnSetting != null) {
                    esmFlag6 = esmFlag5;
                    if (!this.mHwDcTrackerEx.isApnPreset(apnSetting)) {
                        esmFlag6 = esmFlag5;
                        if (this.mPreferredApn.canHandleType(ApnSettingHelper.TYPE_IA)) {
                            log("setInitialAttachApn: send initial attach apn for IA");
                            esmFlag6 = 1;
                        }
                    }
                }
            }
            int esmFlag7 = esmFlag6;
            if (IS_PDN_REJ_CURE_ENABLE) {
                IHwDcTrackerEx iHwDcTrackerEx = this.mHwDcTrackerEx;
                int esmFlag8 = esmFlag6 == 1 ? 1 : 0;
                int esmFlag9 = esmFlag6 == 1 ? 1 : 0;
                int esmFlag10 = esmFlag6 == 1 ? 1 : 0;
                int esmFlag11 = esmFlag6 == 1 ? 1 : 0;
                int esmFlag12 = esmFlag6 == 1 ? 1 : 0;
                iHwDcTrackerEx.setEsmFlag(esmFlag8);
                IccRecords rDataCure = this.mIccRecords.get();
                if (rDataCure != null) {
                    operatorDataCure = rDataCure.getOperatorNumeric();
                }
                esmFlag7 = esmFlag6;
                if (esmFlag6 == 0) {
                    esmFlag7 = esmFlag6;
                    if (this.mHwDcTrackerEx.getDataCureEsmFlag(operatorDataCure) == 1) {
                        log("setInitialAttachApn: send initial attach apn for DataCure");
                        esmFlag7 = 1;
                    }
                }
            }
            if (esmFlag7 != 0) {
                boolean z2 = false;
                int profileType = 0;
                log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
                ApnSetting apnSetting2 = this.mPreferredApn;
                if (apnSetting2 != null && apnSetting2.canHandleType(ApnSettingHelper.TYPE_IA)) {
                    iaApnSetting2 = this.mPreferredApn;
                } else if (!this.mAllApnSettings.isEmpty()) {
                    Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            iaApnSetting2 = iaApnSetting;
                            break;
                        }
                        ApnSetting apn = it.next();
                        if (firstNonEmergencyApnSetting == null && !apn.canHandleType(ApnSettingHelper.TYPE_EMERGENCY)) {
                            firstNonEmergencyApnSetting = apn;
                            log("setInitialApn: firstNonEmergencyApnSetting=" + firstNonEmergencyApnSetting);
                        }
                        if (apn.canHandleType(ApnSettingHelper.TYPE_IA)) {
                            log("setInitialApn: iaApnSetting=" + apn);
                            iaApnSetting2 = apn;
                            break;
                        } else if (defaultApnSetting == null && apn.canHandleType(17)) {
                            log("setInitialApn: defaultApnSetting=" + apn);
                            if (!this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId())) {
                                defaultApnSetting = apn;
                            } else if (this.mHwDcTrackerEx.isSupportLTE(apn)) {
                                defaultApnSetting = apn;
                            }
                        }
                    }
                } else {
                    iaApnSetting2 = iaApnSetting;
                }
                ApnSetting initialAttachApnSetting = null;
                if (this.mPreferredApn != null) {
                    log("setInitialAttachApn: using mPreferredApn");
                    initialAttachApnSetting = this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId()) ? this.mHwDcTrackerEx.isSupportLTE(this.mPreferredApn) ? this.mPreferredApn : defaultApnSetting != null ? defaultApnSetting : iaApnSetting2 : this.mPreferredApn;
                } else if (defaultApnSetting != null) {
                    log("setInitialAttachApn: using defaultApnSetting");
                    initialAttachApnSetting = defaultApnSetting;
                } else if (iaApnSetting2 != null) {
                    log("setInitialAttachApn: using iaApnSetting");
                    initialAttachApnSetting = iaApnSetting2;
                } else if (firstNonEmergencyApnSetting != null) {
                    log("setInitialAttachApn: using firstNonEmergencyApnSetting");
                    if (!this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId())) {
                        initialAttachApnSetting = firstNonEmergencyApnSetting;
                    } else if (this.mHwDcTrackerEx.isSupportLTE(firstNonEmergencyApnSetting)) {
                        initialAttachApnSetting = firstNonEmergencyApnSetting;
                    }
                }
                if (initialAttachApnSetting == null) {
                    log("setInitialAttachApn: X There in no available apn");
                    if (IS_DELAY_ATTACH_ENABLED) {
                        log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                        this.mPhone.mCi.dataConnectionAttach(1, null);
                        return;
                    }
                    return;
                }
                log("setInitialAttachApn: X selected Apn=" + initialAttachApnSetting);
                HwDataConnectionManager hwDataConnectionManager = this.mHwDcManager;
                if (hwDataConnectionManager == null || !hwDataConnectionManager.getNamSwitcherForSoftbank()) {
                    z = true;
                } else {
                    HashMap<String, String> userInfo = this.mHwDcManager.encryptApnInfoForSoftBank(this.mPhoneExt, initialAttachApnSetting);
                    if (userInfo != null) {
                        userInfo.get("username");
                        userInfo.get("password");
                        ApnSetting apn2 = ApnSetting.makeApnSetting(initialAttachApnSetting);
                        if (apn2.getNetworkTypeBitmask() != 0) {
                            profileType = ServiceState.bearerBitmapHasCdma(ServiceState.convertNetworkTypeBitmaskToBearerBitmask(initialAttachApnSetting.getNetworkTypeBitmask())) ? 2 : 1;
                        }
                        this.mDataServiceManager.setInitialAttachApn(new DataProfile.Builder().setProfileId(apn2.getProfileId()).setApn(apn2.getApnName()).setProtocolType(apn2.getProtocol()).setAuthType(apn2.getAuthType()).setUserName(apn2.getUser()).setPassword(apn2.getPassword()).setType(profileType).setMaxConnectionsTime(apn2.getMaxConnsTime()).setMaxConnections(apn2.getMaxConns()).setWaitTime(apn2.getWaitTime()).enable(apn2.isEnabled()).setSupportedApnTypesBitmask(apn2.getApnTypeBitmask()).setRoamingProtocolType(apn2.getRoamingProtocol()).setBearerBitmask(ServiceState.convertBearerBitmaskToNetworkTypeBitmask(initialAttachApnSetting.getNetworkTypeBitmask())).setMtu(apn2.getMtu()).setPersistent(apn2.isPersistent()).setPreferred(true).build(), this.mPhone.getServiceState().getDataRoaming(), null);
                        log("onConnect: mApnSetting.user-mApnSetting.password handle finish");
                        return;
                    }
                    z = true;
                }
                DataServiceManager dataServiceManager = this.mDataServiceManager;
                if (getPreferredApn() == initialAttachApnSetting) {
                    z2 = z;
                }
                dataServiceManager.setInitialAttachApn(createDataProfile(initialAttachApnSetting, z2), this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
            } else if (esmFlagAdaptionEnabled) {
                log("setInitialAttachApn: send empty initial attach apn to clear esmflag");
                this.mDataServiceManager.setInitialAttachApn(new DataProfile.Builder().setProfileId(0).setApn(PhoneConfigurationManager.SSSS).setProtocolType(ApnSetting.getProtocolIntFromString(SystemProperties.get("ro.config.attach_ip_type", "IP"))).setAuthType(0).setUserName(PhoneConfigurationManager.SSSS).setPassword(PhoneConfigurationManager.SSSS).setType(0).setMaxConnectionsTime(0).setMaxConnections(0).setWaitTime(0).enable(false).setSupportedApnTypesBitmask(0).setRoamingProtocolType(2).setBearerBitmask(0).setMtu(0).setPersistent(false).setPreferred(false).build(), this.mPhone.getServiceState().getDataRoaming(), null);
            } else {
                log("setInitialAttachApn: no need to send initial attach apn");
                if (IS_DELAY_ATTACH_ENABLED) {
                    log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                    this.mPhone.mCi.dataConnectionAttach(1, null);
                }
            }
        }
    }

    private void onApnChanged() {
        DctConstants.State overallState = getOverallState();
        boolean z = false;
        boolean isDisconnected = overallState == DctConstants.State.IDLE || overallState == DctConstants.State.FAILED;
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) phone).updateCurrentCarrierInProvider();
        }
        ApnSetting oldPreferredApn = this.mPreferredApn;
        log("onApnChanged: createAllApnList and cleanUpAllConnections");
        this.mHwDcTrackerEx.resetDataCureInfo("apnChanged");
        createAllApnList();
        setDataProfilesAsNeeded();
        ApnSetting mCurPreApn = getPreferredApn();
        if (!this.mHwDcTrackerEx.isBlockSetInitialAttachApn() || mCurPreApn != null) {
            setInitialAttachApn();
        } else {
            log("onApnChanged: block setInitialAttachApn");
        }
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            hwCustDcTracker.tryRestartRadioWhenPrefApnChange(mCurPreApn, oldPreferredApn);
        }
        if (this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId() && mCurPreApn == null) {
            if (!isDisconnected) {
                z = true;
            }
            cleanUpAllConnectionsInternal(z, "apnChanged");
        } else {
            if (!isDisconnected) {
                z = true;
            }
            cleanUpConnectionsOnUpdatedApns(z, "apnChanged");
        }
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnAllConnectableApns("apnChanged", RetryFailures.ALWAYS);
        }
    }

    private boolean isHigherPriorityApnContextActive(ApnContext apnContext) {
        if (apnContext.getApnType().equals("ims")) {
            return false;
        }
        Iterator<ApnContext> it = this.mPrioritySortedApnContexts.iterator();
        while (it.hasNext()) {
            ApnContext otherContext = it.next();
            if (!otherContext.getApnType().equals("ims")) {
                if (apnContext.getApnType().equalsIgnoreCase(otherContext.getApnType())) {
                    return false;
                }
                if (otherContext.isEnabled() && otherContext.getState() != DctConstants.State.FAILED) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnlySingleDcAllowed(int rilRadioTech) {
        PersistableBundle bundle;
        int[] singleDcRats = null;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (!(configManager == null || (bundle = configManager.getConfigForSubId(this.mPhone.getSubId())) == null)) {
            singleDcRats = bundle.getIntArray("only_single_dc_allowed_int_array");
        }
        boolean onlySingleDcAllowed = false;
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("persist.telephony.test.singleDc", false)) {
            onlySingleDcAllowed = true;
        }
        if (singleDcRats != null) {
            for (int i = 0; i < singleDcRats.length && !onlySingleDcAllowed; i++) {
                if (rilRadioTech == singleDcRats[i]) {
                    onlySingleDcAllowed = true;
                }
            }
        }
        boolean onlySingleDcAllowed2 = this.mHwCustDcTracker.shouldDisableMultiPdps(onlySingleDcAllowed);
        log("isOnlySingleDcAllowed(" + rilRadioTech + "): " + onlySingleDcAllowed2);
        return onlySingleDcAllowed2;
    }

    /* access modifiers changed from: package-private */
    public void sendRestartRadio() {
        log("sendRestartRadio:");
        sendMessage(obtainMessage(270362));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restartRadio() {
        log("restartRadio: ************TURN OFF RADIO**************");
        cleanUpAllConnectionsInternal(true, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        this.mPhone.getServiceStateTracker().powerOffRadioSafely();
        try {
            SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", ProxyController.MODEM_0)) + 1));
        } catch (RuntimeException e) {
            log("Failed to set net.ppp.reset-by-timeout");
        }
    }

    private boolean retryAfterDisconnected(ApnContext apnContext) {
        if (this.mHwDcTrackerEx.isDataConnectivityDisabled(this.mPhone.getPhoneId(), "disable-data")) {
            return false;
        }
        boolean retry = true;
        String reason = apnContext.getReason();
        if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(reason) || (isOnlySingleDcAllowed(getDataRat()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = false;
        }
        if (AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(reason)) {
            return false;
        }
        return retry;
    }

    private void startAlarmForReconnect(long delay, ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent("com.android.internal.telephony.data-reconnect." + apnType);
        int phoneId = this.mPhone.getPhoneId();
        intent.addFlags(268435456);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, apnContext.getReason());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE, apnType);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TRANSPORT_TYPE, this.mTransportType);
        if (VSimUtilsInner.isVSimSlot(phoneId)) {
            log("startAlarmForReconnect: for vsim.");
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId, this.mPhone.getSubId());
        } else {
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
        }
        intent.addFlags(268435456);
        log("startAlarmForReconnect: delay=" + delay + " action=" + intent.getAction());
        if (HW_DBG) {
            log("apn = " + apnContext);
        }
        int requestCode = getRequestCode(phoneId);
        log("startAlarmForReconnect requestCode is: " + requestCode);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), requestCode, intent, 201326592);
        apnContext.setReconnectIntent(alarmIntent);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    private void notifyNoData(int lastFailCauseCode, ApnContext apnContext) {
        log("notifyNoData: type=" + apnContext.getApnType());
        if (isPermanentFailure(lastFailCauseCode) && !apnContext.getApnType().equals(TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
            SystemProperties.set("ril.ps_ce_reason", String.valueOf(lastFailCauseCode));
            this.mPhone.notifyDataConnectionFailed(apnContext.getApnType());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRecordsLoadedOrSubIdChanged() {
        log("onRecordsLoadedOrSubIdChanged: createAllApnList");
        HwNetworkManager hwNetworkManager = HwTelephonyFactory.getHwNetworkManager();
        PhoneExt phoneExt = PhoneExt.getPhoneExt(this.mPhone);
        if (hwNetworkManager != null && hwNetworkManager.isNetworkModeAsynchronized(phoneExt)) {
            hwNetworkManager.factoryResetNetworkTypeForNoMdn(phoneExt);
        }
        if (this.mTransportType == 1) {
            this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17891367);
        }
        this.mHwDcTrackerEx.resetDataCureInfo("simLoaded");
        this.mHwDcTrackerEx.updateApnContextState();
        createAllApnList();
        setDataProfilesAsNeeded();
        String operator = this.mIccRecords.get() != null ? this.mIccRecords.get().getOperatorNumeric() : PhoneConfigurationManager.SSSS;
        if (this.mHwDcTrackerEx.isBlockSetInitialAttachApn()) {
            log("onRecordsLoadedOrSubIdChanged: block setInitialAttachApn");
        } else if (!getmIsPseudoImsi() && !TextUtils.isEmpty(operator)) {
            setInitialAttachApn();
        }
        if (getmIsPseudoImsi()) {
            checkPLMN(this.mHwCustDcTracker.getPlmn());
            log("onRecordsLoaded: createAllApnList --return due to IsPseudoImsi");
        } else {
            this.mPhone.notifyDataConnection();
        }
        setupDataOnAllConnectableApns("simLoaded", RetryFailures.ALWAYS);
    }

    private void onSimNotReady() {
        log("onSimNotReady");
        cleanUpAllConnectionsInternal(true, "simNotReady");
        this.mAllApnSettings.clear();
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachEnabled.set(false);
        this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
        this.mHwDcTrackerEx.resetDataCureInfo("simNotReady");
        createAllApnList();
        setDataProfilesAsNeeded();
    }

    private DataConnection checkForCompatibleConnectedApnContext(ApnContext apnContext) {
        int apnType = apnContext.getApnTypeBitmask();
        ArrayList<ApnSetting> dunSettings = null;
        ApnSetting bipSetting = null;
        if (8 == apnType) {
            dunSettings = sortApnListByPreferred(fetchDunApns());
        }
        if (this.mHwDcTrackerEx.isBipApnType(apnContext.getApnType())) {
            bipSetting = this.mHwDcTrackerEx.fetchBipApn(this.mPreferredApn, this.mAllApnSettings);
        }
        if (HW_DBG) {
            log("checkForCompatibleConnectedApnContext: apnContext=" + apnContext);
        }
        DataConnection potentialDc = null;
        ApnContext potentialApnCtx = null;
        for (ApnContext curApnCtx : this.mApnContexts.values()) {
            DataConnection curDc = curApnCtx.getDataConnection();
            if (curDc != null) {
                ApnSetting apnSetting = curApnCtx.getApnSetting();
                log("apnSetting: " + apnSetting);
                int i = 1;
                if (dunSettings != null && dunSettings.size() > 0) {
                    Iterator<ApnSetting> it = dunSettings.iterator();
                    while (it.hasNext()) {
                        if (it.next().equals(apnSetting)) {
                            int i2 = AnonymousClass5.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()];
                            if (i2 == i) {
                                log("checkForCompatibleConnectedApnContext: found dun conn=" + curDc + " curApnCtx=" + curApnCtx);
                                return curDc;
                            } else if (i2 != 2) {
                                if (i2 == 3) {
                                    potentialDc = curDc;
                                    potentialApnCtx = curApnCtx;
                                }
                            } else if (potentialDc == null) {
                                potentialDc = curDc;
                                potentialApnCtx = curApnCtx;
                            }
                        }
                        i = 1;
                    }
                    continue;
                } else if (bipSetting != null) {
                    if (!bipSetting.equals(apnSetting)) {
                        continue;
                    } else {
                        int i3 = AnonymousClass5.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()];
                        if (i3 == 1) {
                            log("checkForCompatibleConnectedApnContext: found bip conn=" + curDc + " curApnCtx=" + curApnCtx);
                            return curDc;
                        } else if (i3 == 3 || i3 == 5) {
                            potentialDc = curDc;
                            potentialApnCtx = curApnCtx;
                        }
                    }
                } else if (apnSetting != null && ((apnContext.getWaitingApns() == null && apnSetting.canHandleType(apnType)) || (apnContext.getWaitingApns() != null && apnContext.getWaitingApns().contains(apnSetting)))) {
                    if (!IS_NR_SLICE_SUPPORTED || !apnContext.isNrSliceApnContext() || apnContext.getApnType().equals(curApnCtx.getApnType())) {
                        int i4 = AnonymousClass5.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()];
                        if (i4 == 1) {
                            log("checkForCompatibleConnectedApnContext: found canHandle conn=" + curDc + " curApnCtx=" + curApnCtx);
                            return curDc;
                        } else if (i4 != 2) {
                            if (i4 == 3) {
                                potentialDc = curDc;
                                potentialApnCtx = curApnCtx;
                            }
                        } else if (potentialDc == null) {
                            potentialDc = curDc;
                            potentialApnCtx = curApnCtx;
                        }
                    }
                }
            } else {
                log("checkForCompatibleConnectedApnContext: not conn curApnCtx=" + curApnCtx);
            }
        }
        if (potentialDc != null) {
            log("checkForCompatibleConnectedApnContext: found potential conn=" + potentialDc + " curApnCtx=" + potentialApnCtx);
            return potentialDc;
        } else if (!HW_DBG) {
            return null;
        } else {
            log("checkForCompatibleConnectedApnContext: NO conn apnContext=" + apnContext);
            return null;
        }
    }

    private void addRequestNetworkCompleteMsg(Message onCompleteMsg, int apnType) {
        if (onCompleteMsg != null) {
            List<Message> messageList = this.mRequestNetworkCompletionMsgs.get(Integer.valueOf(apnType));
            if (messageList == null) {
                messageList = new ArrayList();
            }
            messageList.add(onCompleteMsg);
            this.mRequestNetworkCompletionMsgs.put(Integer.valueOf(apnType), messageList);
        }
    }

    private void sendRequestNetworkCompleteMsg(Message message, boolean success, int transport, int requestType) {
        if (message != null) {
            Bundle b = message.getData();
            b.putBoolean(DATA_COMPLETE_MSG_EXTRA_SUCCESS, success);
            b.putInt(DATA_COMPLETE_MSG_EXTRA_REQUEST_TYPE, requestType);
            b.putInt(DATA_COMPLETE_MSG_EXTRA_TRANSPORT_TYPE, transport);
            message.sendToTarget();
        }
    }

    public void enableApn(int apnType, int requestType, Message onCompleteMsg) {
        sendMessage(obtainMessage(270349, apnType, requestType, onCompleteMsg));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void onEnableApn(int apnType, int requestType, ApnContext apnContext, Message onCompleteMsg) {
        if (apnContext == null) {
            loge("onEnableApn(" + apnType + "): NO ApnContext");
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType);
            return;
        }
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            hwCustDcTracker.clearAndResumeNetInfoForWifiLteCoexist(apnType, 1, apnContext);
        }
        String str = "onEnableApn: apnType=" + ApnSetting.getApnTypeString(apnType) + ", request type=" + requestTypeToString(requestType);
        log(str);
        apnContext.requestLog(str);
        if (!apnContext.isDependencyMet()) {
            apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            apnContext.setEnabled(true);
            log("onEnableApn: dependency is not met.");
            apnContext.requestLog("onEnableApn: dependency is not met.");
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType);
            return;
        }
        if (apnContext.isReady()) {
            DctConstants.State state = apnContext.getState();
            switch (AnonymousClass5.$SwitchMap$com$android$internal$telephony$DctConstants$State[state.ordinal()]) {
                case 1:
                    log("onEnableApn: 'CONNECTED' so return");
                    apnContext.requestLog("onEnableApn state=CONNECTED, so return");
                    sendRequestNetworkCompleteMsg(onCompleteMsg, true, this.mTransportType, requestType);
                    return;
                case 2:
                    log("onEnableApn: 'DISCONNECTING' so return");
                    apnContext.requestLog("onEnableApn state=DISCONNECTING, so return");
                    sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType);
                    return;
                case 3:
                    log("onEnableApn: 'CONNECTING' so return");
                    apnContext.requestLog("onEnableApn state=CONNECTING, so return");
                    addRequestNetworkCompleteMsg(onCompleteMsg, apnType);
                    return;
                case 4:
                case 5:
                case 6:
                    if (this.mHwCustDcTracker == null || !"mms".equals(apnContext.getApnType()) || state != DctConstants.State.RETRYING || !this.mHwCustDcTracker.getCustRetryConfig()) {
                        apnContext.setReason("dataEnabled");
                        break;
                    } else {
                        log("onEnableApn: the mms is retrying,return.");
                        return;
                    }
                    break;
            }
        } else {
            if (apnContext.isEnabled()) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_MET);
            } else {
                apnContext.setReason("dataEnabled");
            }
            if (apnContext.getState() == DctConstants.State.FAILED) {
                apnContext.setState(DctConstants.State.IDLE);
            }
        }
        apnContext.setEnabled(true);
        apnContext.resetErrorCodeRetries();
        if (trySetupData(apnContext, requestType)) {
            addRequestNetworkCompleteMsg(onCompleteMsg, apnType);
        } else {
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType);
        }
    }

    public void disableApn(int apnType, int releaseType) {
        sendMessage(obtainMessage(270350, apnType, releaseType));
    }

    private void onDisableApn(int apnType, int releaseType, ApnContext apnContext) {
        if (apnContext == null) {
            loge("disableApn(" + apnType + "): NO ApnContext");
            return;
        }
        boolean cleanup = true;
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            cleanup = hwCustDcTracker.clearAndResumeNetInfoForWifiLteCoexist(apnType, 0, apnContext);
        }
        String str = "onDisableApn: apnType=" + ApnSetting.getApnTypeString(apnType) + ", release type=" + releaseTypeToString(releaseType);
        log(str);
        apnContext.requestLog(str);
        if (apnContext.isReady()) {
            cleanup = true;
            if (apnContext.isDependencyMet()) {
                apnContext.setReason("dataDisabledInternal");
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            }
        }
        apnContext.setEnabled(false);
        if (cleanup) {
            cleanUpConnectionInternal(true, releaseType, apnContext);
        }
        if (isOnlySingleDcAllowed(getDataRat()) && !isHigherPriorityApnContextActive(apnContext)) {
            log("disableApn:isOnlySingleDcAllowed true & higher priority APN disabled");
            setupDataOnAllConnectableApns("SinglePdnArbitration", RetryFailures.ALWAYS);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public void setDataRoamingEnabledByUser(boolean enabled) {
        int phoneSubId = this.mPhone.getSubId();
        if (getDataRoamingEnabled() != enabled) {
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                Settings.Global.putInt(this.mResolver, "data_roaming", enabled);
            } else {
                Settings.Global.putInt(this.mResolver, this.mHwDcTrackerEx.getDataRoamingSettingItem("data_roaming"), enabled ? 1 : 0);
            }
            this.mDataEnabledSettings.setDataRoamingEnabled(enabled);
            log("setDataRoamingEnabledByUser: set phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
        } else {
            log("setDataRoamingEnabledByUser: unchanged phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
        }
        setDataRoamingFromUserAction(true);
    }

    public boolean getDataRoamingEnabled() {
        boolean isDataRoamingEnabled;
        boolean isDataRoamingEnabled2 = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false"));
        int phoneSubId = this.mPhone.getSubId();
        boolean z = true;
        if (VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId())) {
            return true;
        }
        try {
            if (this.mHwCustDcTracker == null || !this.mHwCustDcTracker.isNeedDataRoamingExpend()) {
                if (TelephonyManager.getDefault().getSimCount() == 1) {
                    if (Settings.Global.getInt(this.mResolver, "data_roaming", isDataRoamingEnabled2 ? 1 : 0) == 0) {
                        z = false;
                    }
                    isDataRoamingEnabled = z;
                } else {
                    if (Settings.Global.getInt(this.mResolver, this.mHwDcTrackerEx.getDataRoamingSettingItem("data_roaming")) == 0) {
                        z = false;
                    }
                    isDataRoamingEnabled = z;
                }
                log("getDataRoamingEnabled: phoneSubId=" + phoneSubId + " isDataRoamingEnabled=" + isDataRoamingEnabled);
                return isDataRoamingEnabled;
            }
            isDataRoamingEnabled = this.mHwCustDcTracker.getDataRoamingEnabledWithNational();
            log("getDataRoamingEnabled: phoneSubId=" + phoneSubId + " isDataRoamingEnabled=" + isDataRoamingEnabled);
            return isDataRoamingEnabled;
        } catch (Settings.SettingNotFoundException snfe) {
            log("getDataRoamingEnabled: SettingNofFoundException snfe=" + snfe);
            isDataRoamingEnabled = getDefaultDataRoamingEnabled();
        }
    }

    private boolean getDefaultDataRoamingEnabled() {
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        boolean isDataRoamingEnabled = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false"));
        if (configMgr.getConfigForSubId(this.mPhone.getSubId()) != null) {
            return isDataRoamingEnabled | configMgr.getConfigForSubId(this.mPhone.getSubId()).getBoolean("carrier_default_data_roaming_enabled_bool");
        }
        return isDataRoamingEnabled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDefaultDataRoamingEnabled() {
        boolean useCarrierSpecificDefault = false;
        if (this.mTelephonyManager.getSimCount() != 1) {
            try {
                Settings.Global.getInt(this.mResolver, "data_roaming" + this.mPhone.getSubId());
            } catch (Settings.SettingNotFoundException e) {
                useCarrierSpecificDefault = true;
            }
        } else if (!isDataRoamingFromUserAction()) {
            useCarrierSpecificDefault = true;
        }
        log("setDefaultDataRoamingEnabled: useCarrierSpecificDefault " + useCarrierSpecificDefault);
        if (useCarrierSpecificDefault) {
            this.mDataEnabledSettings.setDataRoamingEnabled(this.mDataEnabledSettings.getDefaultDataRoamingEnabled());
        }
    }

    private boolean isDataRoamingFromUserAction() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
        if (!sp.contains(Phone.DATA_ROAMING_IS_USER_SETTING_KEY) && Settings.Global.getInt(this.mResolver, "device_provisioned", 0) == 0) {
            sp.edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, false).commit();
        }
        return sp.getBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, true);
    }

    private void setDataRoamingFromUserAction(boolean isUserAction) {
        PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, isUserAction).commit();
    }

    private void onDataRoamingOff() {
        log("onDataRoamingOff");
        reevaluateDataConnections();
        if (!getDataRoamingEnabled()) {
            setDataProfilesAsNeeded();
            if (!TextUtils.isEmpty(this.mIccRecords.get() != null ? this.mIccRecords.get().getOperatorNumeric() : PhoneConfigurationManager.SSSS)) {
                setInitialAttachApn();
            }
            setupDataOnAllConnectableApns("roamingOff", RetryFailures.ALWAYS);
            return;
        }
        this.mPhone.notifyDataConnection();
    }

    private void onDataRoamingOnOrSettingsChanged(int messageType) {
        log("onDataRoamingOnOrSettingsChanged");
        boolean settingChanged = messageType == 270384;
        if (settingChanged && getDataRoamingEnabled()) {
            this.mHwCustDcTracker.setDataOrRoamOn(1);
        }
        if (this.mHwCustDcTracker.processAttDataRoamingOn()) {
            log("process ATT DataRoaming off");
        } else if (!this.mPhone.getServiceState().getDataRoaming()) {
            log("device is not roaming. ignored the request.");
        } else {
            checkDataRoamingStatus(settingChanged);
            if (getDataRoamingEnabled()) {
                if (settingChanged) {
                    reevaluateDataConnections();
                }
                log("onDataRoamingOnOrSettingsChanged: setup data on roaming");
                setupDataOnAllConnectableApns("roamingOn", RetryFailures.ALWAYS);
                this.mPhone.notifyDataConnection();
                return;
            }
            log("onDataRoamingOnOrSettingsChanged: Tear down data connection on roaming.");
            cleanUpAllConnectionsInternal(true, "roamingOn");
        }
    }

    private void checkDataRoamingStatus(boolean settingChanged) {
        if (!(settingChanged || getDataRoamingEnabled() || !this.mPhone.getServiceState().getDataRoaming())) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == DctConstants.State.CONNECTED) {
                    LocalLog localLog = this.mDataRoamingLeakageLog;
                    StringBuilder sb = new StringBuilder();
                    sb.append("PossibleRoamingLeakage  connection params: ");
                    sb.append(apnContext.getDataConnection() != null ? apnContext.getDataConnection().getConnectionParams() : PhoneConfigurationManager.SSSS);
                    localLog.log(sb.toString());
                }
            }
        }
    }

    private void onRadioAvailable() {
        log("onRadioAvailable");
        if (this.mPhone.getSimulatedRadioControl() != null) {
            this.mPhone.notifyDataConnection();
            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }
        if (getOverallState() != DctConstants.State.IDLE) {
            cleanUpConnectionInternal(true, 2, null);
        }
    }

    private void onRadioOffOrNotAvailable() {
        this.mReregisterOnReconnectFailure = false;
        this.mAutoAttachEnabled.set(false);
        this.mIsPsRestricted = false;
        this.mPhone.getServiceStateTracker().mRestrictedState.setPsRestricted(false);
        if (this.mPhone.getSimulatedRadioControl() != null) {
            log("We're on the simulator; assuming radio off is meaningless");
            return;
        }
        log("onRadioOffOrNotAvailable: is off and clean up all connections");
        cleanUpAllConnectionsInternal(false, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
    }

    private void completeConnection(ApnContext apnContext, int type) {
        if (HW_DBG) {
            log("completeConnection: successful, notify the world apnContext=" + apnContext);
        }
        if (this.mIsProvisioning && !TextUtils.isEmpty(this.mProvisioningUrl)) {
            if (HW_DBG) {
                log("completeConnection: MOBILE_PROVISIONING_ACTION url=" + this.mProvisioningUrl);
            }
            Intent newIntent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
            newIntent.setData(Uri.parse(this.mProvisioningUrl));
            newIntent.setFlags(272629760);
            try {
                this.mPhone.getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                loge("completeConnection: startActivityAsUser failed" + e);
            }
        }
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        ProgressDialog progressDialog = this.mProvisioningSpinner;
        if (progressDialog != null) {
            sendMessage(obtainMessage(270378, progressDialog));
        }
        if (type != 2) {
            this.mPhone.notifyDataConnection(apnContext.getApnType());
        }
        startNetStatPoll();
        startDataStallAlarm(false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:0x02d1  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x02fb  */
    /* JADX WARNING: Removed duplicated region for block: B:140:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0152  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01b6  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01e3  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01f3  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x027b  */
    private void onDataSetupComplete(ApnContext apnContext, boolean success, int cause, int requestType) {
        String str;
        boolean isProvApn;
        HwCustDcTracker hwCustDcTracker;
        LinkProperties chrLinkProperties;
        boolean z;
        HwCustDcTracker hwCustDcTracker2;
        NumberFormatException e;
        String str2;
        if (apnContext != null) {
            boolean isDefault = TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType());
            this.preSetupBasedRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
            List<Message> messageList = this.mRequestNetworkCompletionMsgs.get(Integer.valueOf(ApnSetting.getApnTypesBitmaskFromString(apnContext.getApnType())));
            if (messageList != null) {
                for (Message msg : messageList) {
                    sendRequestNetworkCompleteMsg(msg, success, this.mTransportType, requestType);
                }
                messageList.clear();
            }
            if (success) {
                DataConnection dataConnection = apnContext.getDataConnection();
                if (dataConnection == null) {
                    HwCustDcTracker hwCustDcTracker3 = this.mHwCustDcTracker;
                    if (hwCustDcTracker3 != null) {
                        hwCustDcTracker3.setCurFailCause(cause);
                    }
                    log("onDataSetupComplete: no connection to DC, handle as error");
                    onDataSetupCompleteError(apnContext, requestType);
                    return;
                }
                DataConnectionEx dcEx = new DataConnectionEx();
                dcEx.setDataConnection(dataConnection);
                this.mHwDcTrackerEx.addIfacePhoneHashMap(dcEx, mIfacePhoneHashMap);
                HwCustDcTracker hwCustDcTracker4 = this.mHwCustDcTracker;
                if (hwCustDcTracker4 != null && hwCustDcTracker4.isClearCodeEnabled() && isDefault) {
                    this.mHwCustDcTracker.resetTryTimes();
                }
                ApnSetting apn = apnContext.getApnSetting();
                StringBuilder sb = new StringBuilder();
                sb.append("onDataSetupComplete: success apn=");
                if (apn == null) {
                    str = "unknown";
                } else {
                    str = apn.getApnName();
                }
                sb.append(str);
                log(sb.toString());
                if (isDefault) {
                    if (apn == null) {
                        str2 = PhoneConfigurationManager.SSSS;
                    } else {
                        try {
                            str2 = apn.getApnName();
                        } catch (RuntimeException e2) {
                            loge("Failed to set apn");
                        }
                    }
                    SystemProperties.set("gsm.default.apn", str2);
                    log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
                }
                if (this.mHwDcTrackerEx.needSetCTProxy(apn)) {
                    this.mHwDcTrackerEx.setCtProxy(dcEx);
                } else if (apn != null && !TextUtils.isEmpty(apn.getProxyAddressAsString())) {
                    try {
                        int port = apn.getProxyPort();
                        if (port == -1) {
                            port = 8080;
                        }
                        try {
                            dataConnection.setLinkPropertiesHttpProxy(new ProxyInfo(apn.getProxyAddressAsString(), port, "127.0.0.1"));
                        } catch (NumberFormatException e3) {
                            e = e3;
                        }
                    } catch (NumberFormatException e4) {
                        e = e4;
                        loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.getProxyPort() + "): " + e);
                        if (!TextUtils.equals(apnContext.getApnType(), TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
                        }
                        apnContext.setState(DctConstants.State.CONNECTED);
                        apnContext.resetErrorCodeRetries();
                        cancelReconnectAlarm(apnContext);
                        checkDataRoamingStatus(false);
                        isProvApn = apnContext.isProvisioningApn();
                        ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                        if (this.mProvisionBroadcastReceiver != null) {
                        }
                        if (isProvApn) {
                        }
                        String str3 = this.mProvisionActionName;
                        int i = z ? 1 : 0;
                        int i2 = z ? 1 : 0;
                        int i3 = z ? 1 : 0;
                        cm.setProvisioningNotificationVisible(z, i, str3);
                        completeConnection(apnContext, requestType);
                        log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType());
                        if (Build.IS_DEBUGGABLE) {
                        }
                        hwCustDcTracker = this.mHwCustDcTracker;
                        if (hwCustDcTracker != null) {
                        }
                        log("CHR inform CHR the APN info when data setup succ");
                        ApnContextEx apnContextEx = new ApnContextEx();
                        apnContextEx.setApnContext(apnContext);
                        this.mHwDcTrackerEx.checkOnlyIpv6Cure(apnContextEx);
                        this.mHwDcTrackerEx.isNeedDataCure(DcFailCause.NONE.getErrorCode(), apnContextEx);
                        chrLinkProperties = getLinkProperties(apnContext.getApnType());
                        if (chrLinkProperties != null) {
                        }
                    }
                }
                if (!TextUtils.equals(apnContext.getApnType(), TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
                    try {
                        SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "true");
                    } catch (RuntimeException e5) {
                        log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to true");
                    }
                    if (!(apn == null || (hwCustDcTracker2 = this.mHwCustDcTracker) == null)) {
                        String activedApnOpkey = hwCustDcTracker2.getOpKeyByActivedApn(apn.getOperatorNumeric(), apn.getApnName(), apn.getUser());
                        log("onDataSetupComplete: activedApnVnkey = " + activedApnOpkey);
                        this.mHwCustDcTracker.setApnOpkeyToSettingsDB(activedApnOpkey);
                    }
                    if (this.mCanSetPreferApn && this.mPreferredApn == null) {
                        log("onDataSetupComplete: PREFERRED APN is null");
                        if (apn != null && !apn.equals(this.mHwDcTrackerEx.getAttachedApnSetting())) {
                            this.mPreferredApn = apn;
                        }
                        ApnSetting apnSetting = this.mPreferredApn;
                        if (apnSetting != null) {
                            setPreferredApn(apnSetting.getId());
                        }
                    }
                } else {
                    try {
                        SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                    } catch (RuntimeException e6) {
                        log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                    }
                }
                apnContext.setState(DctConstants.State.CONNECTED);
                apnContext.resetErrorCodeRetries();
                cancelReconnectAlarm(apnContext);
                checkDataRoamingStatus(false);
                isProvApn = apnContext.isProvisioningApn();
                ConnectivityManager cm2 = ConnectivityManager.from(this.mPhone.getContext());
                if (this.mProvisionBroadcastReceiver != null) {
                    this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
                    this.mProvisionBroadcastReceiver = null;
                }
                if (isProvApn) {
                    z = false;
                } else if (this.mIsProvisioning) {
                    z = false;
                } else {
                    log("onDataSetupComplete: successful, BUT send connected to prov apn as mIsProvisioning:" + this.mIsProvisioning + " == false && (isProvisioningApn:" + isProvApn + " == true");
                    this.mProvisionBroadcastReceiver = new ProvisionNotificationBroadcastReceiver(cm2.getMobileProvisioningUrl(), this.mTelephonyManager.getNetworkOperatorName());
                    this.mPhone.getContext().registerReceiver(this.mProvisionBroadcastReceiver, new IntentFilter(this.mProvisionActionName));
                    cm2.setProvisioningNotificationVisible(true, 0, this.mProvisionActionName);
                    setRadio(false);
                    log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType());
                    if (Build.IS_DEBUGGABLE) {
                        int pcoVal = SystemProperties.getInt("persist.radio.test.pco", -1);
                        if (pcoVal != -1) {
                            log("PCO testing: read pco value from persist.radio.test.pco " + pcoVal);
                            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                            intent.putExtra("apnType", TransportManager.IWLAN_OPERATION_MODE_DEFAULT);
                            intent.putExtra("apnProto", "IPV4V6");
                            intent.putExtra("pcoId", 65280);
                            intent.putExtra("pcoValue", new byte[]{(byte) pcoVal});
                            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                        }
                    }
                    hwCustDcTracker = this.mHwCustDcTracker;
                    if (hwCustDcTracker != null) {
                        hwCustDcTracker.setFirstTimeEnableData();
                    }
                    log("CHR inform CHR the APN info when data setup succ");
                    ApnContextEx apnContextEx2 = new ApnContextEx();
                    apnContextEx2.setApnContext(apnContext);
                    this.mHwDcTrackerEx.checkOnlyIpv6Cure(apnContextEx2);
                    this.mHwDcTrackerEx.isNeedDataCure(DcFailCause.NONE.getErrorCode(), apnContextEx2);
                    chrLinkProperties = getLinkProperties(apnContext.getApnType());
                    if (chrLinkProperties != null) {
                        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDataConnected(this.mPhone, apn, chrLinkProperties);
                        return;
                    }
                    return;
                }
                String str32 = this.mProvisionActionName;
                int i4 = z ? 1 : 0;
                int i22 = z ? 1 : 0;
                int i32 = z ? 1 : 0;
                cm2.setProvisioningNotificationVisible(z, i4, str32);
                completeConnection(apnContext, requestType);
                log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType());
                if (Build.IS_DEBUGGABLE) {
                }
                hwCustDcTracker = this.mHwCustDcTracker;
                if (hwCustDcTracker != null) {
                }
                log("CHR inform CHR the APN info when data setup succ");
                ApnContextEx apnContextEx22 = new ApnContextEx();
                apnContextEx22.setApnContext(apnContext);
                this.mHwDcTrackerEx.checkOnlyIpv6Cure(apnContextEx22);
                this.mHwDcTrackerEx.isNeedDataCure(DcFailCause.NONE.getErrorCode(), apnContextEx22);
                chrLinkProperties = getLinkProperties(apnContext.getApnType());
                if (chrLinkProperties != null) {
                }
            } else {
                ApnSetting apn2 = apnContext.getApnSetting();
                StringBuilder sb2 = new StringBuilder();
                sb2.append("onDataSetupComplete: error apn=");
                sb2.append(apn2 != null ? apn2.getApnName() : null);
                sb2.append(", cause=");
                sb2.append(cause);
                sb2.append(", requestType=");
                sb2.append(requestTypeToString(requestType));
                log(sb2.toString());
                this.mHwDcTrackerEx.sendDSMipErrorBroadcast();
                if (DataFailCause.isEventLoggable(cause)) {
                    EventLog.writeEvent((int) EventLogTags.PDP_SETUP_FAIL, Integer.valueOf(cause), Integer.valueOf(getCellLocationId()), Integer.valueOf(this.mTelephonyManager.getNetworkType()));
                }
                ApnSetting apn3 = apnContext.getApnSetting();
                this.mPhone.notifyPreciseDataConnectionFailed(apnContext.getApnType(), apn3 != null ? apn3.getApnName() : null, cause);
                Intent intent2 = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED");
                intent2.putExtra("errorCode", cause);
                intent2.putExtra("apnType", apnContext.getApnType());
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent2);
                SystemClock.elapsedRealtime();
                ApnContextEx apnContextEx3 = new ApnContextEx();
                apnContextEx3.setApnContext(apnContext);
                DcFailCauseExt failCauseExt = new DcFailCauseExt();
                failCauseExt.setDcFailCause(DcFailCause.fromInt(cause));
                if (DataFailCause.isRadioRestartFailure(this.mPhone.getContext(), cause, this.mPhone.getSubId()) || this.mHwDcTrackerEx.needRestartRadioOnError(apnContextEx3, failCauseExt)) {
                    log("Modem restarted.");
                    if (TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType())) {
                        reportToBoosterForPdpNoRetry();
                    }
                    this.mHwDcTrackerEx.sendRestartRadioChr(this.mPhone.getSubId(), cause);
                    sendRestartRadio();
                }
                boolean isDataCure = this.mHwDcTrackerEx.isNeedDataCure(cause, apnContextEx3);
                HwCustDcTracker hwCustDcTracker5 = this.mHwCustDcTracker;
                if (hwCustDcTracker5 != null && hwCustDcTracker5.isClearCodeEnabled()) {
                    long delay = apnContext.getDelayForNextApn(this.mFailFast);
                    log("clearcode onDataSetupComplete delay=" + delay);
                    this.mHwCustDcTracker.operateClearCodeProcess(apnContext, cause, (int) delay);
                } else if (isPermanentFailure(cause) && !isDataCure) {
                    log("cause = " + cause + ", mark apn as permanent failed. apn = " + apn3);
                    apnContext.markApnPermanentFailed(apn3);
                }
                this.mHwCustDcTracker.setCurFailCause(cause);
                onDataSetupCompleteError(apnContext, requestType);
            }
        }
    }

    private void onDataSetupCompleteError(ApnContext apnContext, int requestType) {
        long delay;
        ApnContextEx apnContextEx = new ApnContextEx();
        apnContextEx.setApnContext(apnContext);
        this.mHwDcTrackerEx.updateDataRetryStategy(apnContextEx);
        if (apnContext.isLastApnSetting()) {
            this.mHwCustDcTracker.onAllApnFirstActiveFailed();
            if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_cust", false) && "bip0".equals(apnContext.getApnType())) {
                Intent intent = new Intent();
                intent.setAction(AbstractPhoneInternalInterface.OTA_OPEN_SERVICE_ACTION);
                intent.putExtra(AbstractPhoneInternalInterface.OTA_TAG, 1);
                this.mPhone.getContext().sendBroadcast(intent);
                if (hasMessages(271146)) {
                    removeMessages(271146);
                }
                log("sendbroadcast OTA_OPEN_SERVICE_ACTION");
                return;
            }
        }
        PhoneExt phoneExt = new PhoneExt();
        phoneExt.setPhone(this.mPhone);
        if (HwTelephonyFactory.getHwPhoneManager().isSupportOrangeApn(phoneExt)) {
            HwTelephonyFactory.getHwPhoneManager().addSpecialAPN(phoneExt);
            log("onDataSetupCompleteError.addSpecialAPN()");
        }
        long delay2 = apnContext.getDelayForNextApn(this.mFailFast);
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null && hwCustDcTracker.isPSClearCodeRplmnMatched()) {
            log("PSCLEARCODE retry APN. old delay = " + delay2);
            delay2 = this.mHwCustDcTracker.updatePSClearCodeApnContext(apnContext, delay2);
        }
        if (delay2 >= 0) {
            log("onDataSetupCompleteError: Try next APN. delay = " + delay2);
            apnContext.setState(DctConstants.State.RETRYING);
            HwCustDcTracker hwCustDcTracker2 = this.mHwCustDcTracker;
            if (hwCustDcTracker2 != null && hwCustDcTracker2.isClearCodeEnabled()) {
                delay2 = (long) this.mHwCustDcTracker.getDelayTime();
            }
            if (!onDataRetryDisableNr(delay2, apnContext)) {
                if (this.mHwDcTrackerEx.getDataRetryAction(apnContextEx) == 0) {
                    delay = delay2;
                } else {
                    delay = this.mHwDcTrackerEx.getDataRetryDelay(delay2, apnContextEx);
                }
                startAlarmForReconnect(delay, apnContext);
                return;
            }
            return;
        }
        this.mHwCustDcTracker.onAllApnPermActiveFailed();
        if (TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType())) {
            reportToBoosterForPdpNoRetry();
        }
        apnContext.setState(DctConstants.State.FAILED);
        this.mPhone.notifyDataConnection(apnContext.getApnType());
        apnContext.setDataConnection(null);
        log("onDataSetupCompleteError: Stop retrying APNs. delay=" + delay2 + ", requestType=" + requestTypeToString(requestType));
    }

    private void onNetworkStatusChanged(int status, String redirectUrl) {
        if (!TextUtils.isEmpty(redirectUrl)) {
            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED");
            intent.putExtra("redirectionUrl", redirectUrl);
            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            log("Notify carrier signal receivers with redirectUrl.");
            return;
        }
        boolean isValid = status == 1;
        if (!this.mDsRecoveryHandler.isRecoveryOnBadNetworkEnabled()) {
            log("Skip data stall recovery on network status change with in threshold");
        } else if (this.mTransportType != 1) {
            log("Skip data stall recovery on non WWAN");
        } else {
            this.mDsRecoveryHandler.processNetworkStatusChanged(isValid);
        }
    }

    private void onDisconnectDone(ApnContext apnContext) {
        DataConnection dc = apnContext.getDataConnection();
        if (apnContext.getState() != DctConstants.State.CONNECTING || dc == null || dc.isInactive() || !dc.checkApnContext(apnContext)) {
            if (HW_DBG) {
                log("onDisconnectDone: EVENT_DISCONNECT_DONE apnContext=" + apnContext);
            }
            apnContext.setState(DctConstants.State.IDLE);
            if (TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType())) {
                SystemProperties.set("gsm.default.apn", PhoneConfigurationManager.SSSS);
                log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
            }
            if (dc != null && dc.isInactive() && !dc.hasBeenTransferred()) {
                for (String type : ApnSetting.getApnTypesStringFromBitmask(apnContext.getApnSetting().getApnTypeBitmask()).split(",")) {
                    this.mPhone.notifyDataConnection(type);
                }
            }
            if (getOverallState() != DctConstants.State.CONNECTED) {
                this.mHwDcTrackerEx.stopPdpResetAlarm();
            }
            if (isDisconnected()) {
                if (this.mPhone.getServiceStateTracker().processPendingRadioPowerOffAfterDataOff()) {
                    log("onDisconnectDone: radio will be turned off, no retries");
                    apnContext.setApnSetting(null);
                    apnContext.setDataConnection(null);
                    int i = this.mDisconnectPendingCount;
                    if (i > 0) {
                        this.mDisconnectPendingCount = i - 1;
                    }
                    if (this.mDisconnectPendingCount == 0) {
                        notifyAllDataDisconnected();
                        return;
                    }
                    return;
                }
                log("data is disconnected and check if need to setPreferredNetworkType");
                ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
                if (sst != null) {
                    sst.checkAndSetNetworkType();
                }
                if (!(sst == null || sst.returnObject() == null || !(sst.returnObject().isDataOffForbidLTE() || sst.returnObject().isDataOffbyRoamAndData()))) {
                    log("DCT onDisconnectDone");
                    sst.returnObject().processEnforceLTENetworkTypePending();
                }
            }
            if (!this.mAttached.get() || !apnContext.isReady() || !retryAfterDisconnected(apnContext)) {
                boolean restartRadioAfterProvisioning = this.mPhone.getContext().getResources().getBoolean(17891502);
                if (apnContext.isProvisioningApn() && restartRadioAfterProvisioning) {
                    log("onDisconnectDone: restartRadio after provisioning");
                    restartRadio();
                }
                apnContext.setApnSetting(null);
                apnContext.setDataConnection(null);
                if (isOnlySingleDcAllowed(getDataRat())) {
                    log("onDisconnectDone: isOnlySigneDcAllowed true so setup single apn");
                    if (AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(apnContext.getReason())) {
                        this.mHwDcTrackerEx.setupDataOnConnectableApns("SinglePdnArbitration", apnContext.getApnType());
                    } else if (isDisconnected()) {
                        setupDataOnAllConnectableApns("SinglePdnArbitration", RetryFailures.ALWAYS);
                    } else {
                        log("onDisconnectDone: not all context is disconnected");
                    }
                } else if (this.mPhone.getServiceState().getVoiceRegState() != 0 || !retryAfterDisconnected(apnContext)) {
                    log("onDisconnectDone: not retrying");
                } else {
                    log("onDisconnectDone: cdma cs attached, retry after disconnect");
                    startAlarmForReconnect(5000, apnContext);
                }
            } else {
                try {
                    SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                } catch (RuntimeException e) {
                    log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                }
                log("onDisconnectDone: attached, ready and retry after disconnect");
                ApnContextEx apnContextEx = new ApnContextEx();
                apnContextEx.setApnContext(apnContext);
                this.mHwDcTrackerEx.updateDataRetryStategy(apnContextEx);
                long delay = this.mHwDcTrackerEx.getDataRetryDelay(apnContext.getRetryAfterDisconnectDelay(), apnContextEx);
                if (delay >= 0 && delay < 5000) {
                    sendMessageDelayed(obtainMessage(270339, apnContext), delay);
                    return;
                } else if (delay >= 5000) {
                    if (!onDataRetryDisableNr(delay, apnContext)) {
                        startAlarmForReconnect(delay, apnContext);
                    } else {
                        return;
                    }
                }
            }
            int i2 = this.mDisconnectPendingCount;
            if (i2 > 0) {
                this.mDisconnectPendingCount = i2 - 1;
            }
            if (this.mDisconnectPendingCount == 0) {
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                notifyAllDataDisconnected();
                return;
            }
            return;
        }
        loge("onDisconnectDone: apnContext is activating, ignore " + apnContext);
    }

    private void onVoiceCallStarted() {
        log("onVoiceCallStarted");
        this.mInVoiceCall = true;
        if (isConnected() && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            this.mPhone.notifyDataConnection();
        }
    }

    private void onVoiceCallEnded() {
        log("onVoiceCallEnded");
        this.mInVoiceCall = false;
        if (isConnected()) {
            if (!this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                startNetStatPoll();
                startDataStallAlarm(false);
                this.mPhone.notifyDataConnection();
            } else {
                resetPollStats();
            }
        }
        setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED, RetryFailures.ALWAYS);
    }

    private boolean isConnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == DctConstants.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public boolean isDisconnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!apnContext.isDisconnected()) {
                return false;
            }
        }
        return true;
    }

    private void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        ArrayList<DataProfile> dataProfileList = new ArrayList<>();
        ApnSetting preferredApn = getPreferredApn();
        for (ApnSetting apn : this.mAllApnSettings) {
            DataProfile dp = createDataProfile(apn, apn.equals(preferredApn));
            if (!dataProfileList.contains(dp)) {
                dataProfileList.add(dp);
            }
        }
        if (dataProfileList.isEmpty()) {
            return;
        }
        if (dataProfileList.size() != this.mLastDataProfileList.size() || !this.mLastDataProfileList.containsAll(dataProfileList)) {
            this.mDataServiceManager.setDataProfile(dataProfileList, this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
        }
    }

    private synchronized void createAllApnList() {
        ApnSetting apn;
        Cursor cursor;
        this.mAllApnSettings.clear();
        IccRecords r = this.mIccRecords.get();
        String operator = this.mHwDcTrackerEx.getCTOperator(r != null ? r.getOperatorNumeric() : PhoneConfigurationManager.SSSS);
        SubscriptionManager subscriptionManager = this.mSubscriptionManager;
        int slotId = SubscriptionManager.getSlotIndex(this.mPhone.getSubId());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(slotId));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (!TextUtils.isEmpty(operator)) {
            String selection = "numeric = '" + operator + "'";
            log("createAllApnList: selection=" + selection);
            String subId = Long.toString((long) this.mPhone.getSubId());
            if (this.isMultiSimEnabled) {
                cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, subId), null, selection, null, HbpcdLookup.ID);
            } else {
                cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(Telephony.Carriers.SIM_APN_URI, "filtered/subId/" + this.mPhone.getSubId()), null, null, null, HbpcdLookup.ID);
            }
            boolean hasDefaultApn = false;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ApnSetting apn2 = ApnSetting.makeApnSetting(cursor);
                    boolean z = true;
                    if (cursor.getInt(cursor.getColumnIndexOrThrow(IHwDcTrackerEx.DB_PRESET)) != 1) {
                        z = false;
                    }
                    apn2.setIsPreset(z);
                    this.mAllApnSettings.add(apn2);
                    if (!hasDefaultApn && apn2.canHandleType(17)) {
                        hasDefaultApn = true;
                    }
                    makeAndAddSnssaiApnSetting(apn2);
                }
                cursor.close();
            } else {
                log("createAllApnList: cursor is null");
                this.mApnSettingsInitializationLog.log("cursor is null for carrier, operator: " + operator);
            }
            sendErrorEvent(r, operator, hasDefaultApn);
        }
        if (VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId()) && this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: vsim enabled and apn not in database");
            this.mAllApnSettings = VSimUtilsInner.createVSimApnList();
        }
        addEmergencyApnSetting();
        dedupeApnSettings();
        this.mHwDcTrackerEx.correctApnAuthType(this.mAllApnSettings);
        if (!this.mAllApnSettings.isEmpty()) {
            if (!VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId())) {
                this.mPreferredApn = getPreferredApn();
                if (this.mPreferredApn == null && (apn = this.mHwCustDcTracker.getCustPreferredApn(this.mAllApnSettings)) != null) {
                    setPreferredApn(apn.getId());
                    this.mPreferredApn = getPreferredApn();
                }
                if (this.mPreferredApn != null && !this.mPreferredApn.getOperatorNumeric().equals(operator)) {
                    this.mPreferredApn = null;
                    setPreferredApn(-1);
                }
                log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
                log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
            }
        }
        log("createAllApnList: No APN found for carrier, operator: " + operator);
        this.mApnSettingsInitializationLog.log("no APN found for carrier, operator: " + operator);
        this.mPreferredApn = null;
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
    }

    private void dedupeApnSettings() {
        new ArrayList();
        for (int i = 0; i < this.mAllApnSettings.size() - 1; i++) {
            ApnSetting first = this.mAllApnSettings.get(i);
            int j = i + 1;
            while (j < this.mAllApnSettings.size()) {
                ApnSetting second = this.mAllApnSettings.get(j);
                if (this.mHwDcTrackerEx.isApnSettingsSimilar(first, second)) {
                    ApnSetting newApn = mergeApns(first, second);
                    this.mAllApnSettings.set(i, newApn);
                    first = newApn;
                    this.mAllApnSettings.remove(j);
                } else {
                    j++;
                }
            }
        }
    }

    private ApnSetting mergeApns(ApnSetting dest, ApnSetting src) {
        int protocol;
        int id = dest.getId();
        if ((src.getApnTypeBitmask() & 17) == 17) {
            id = src.getId();
        }
        int resultApnType = src.getApnTypeBitmask() | dest.getApnTypeBitmask();
        Uri mmsc = dest.getMmsc() == null ? src.getMmsc() : dest.getMmsc();
        String mmsProxy = TextUtils.isEmpty(dest.getMmsProxyAddressAsString()) ? src.getMmsProxyAddressAsString() : dest.getMmsProxyAddressAsString();
        int mmsPort = dest.getMmsProxyPort() == -1 ? src.getMmsProxyPort() : dest.getMmsProxyPort();
        String proxy = TextUtils.isEmpty(dest.getProxyAddressAsString()) ? src.getProxyAddressAsString() : dest.getProxyAddressAsString();
        int port = dest.getProxyPort() == -1 ? src.getProxyPort() : dest.getProxyPort();
        if (src.getProtocol() == 2) {
            protocol = src.getProtocol();
        } else {
            protocol = dest.getProtocol();
        }
        return ApnSetting.makeApnSetting(id, dest.getOperatorNumeric(), dest.getApnName(), dest.getApnName(), proxy, port, mmsc, mmsProxy, mmsPort, dest.getUser(), dest.getPassword(), dest.getAuthType(), resultApnType, protocol, src.getRoamingProtocol() == 2 ? src.getRoamingProtocol() : dest.getRoamingProtocol(), dest.isEnabled(), (dest.getNetworkTypeBitmask() == 0 || src.getNetworkTypeBitmask() == 0) ? 0 : dest.getNetworkTypeBitmask() | src.getNetworkTypeBitmask(), dest.getProfileId(), dest.isPersistent() || src.isPersistent(), dest.getMaxConns(), dest.getWaitTime(), dest.getMaxConnsTime(), dest.getMtu(), dest.getMvnoType(), dest.getMvnoMatchData(), dest.getApnSetId(), dest.getCarrierId(), dest.getSkip464Xlat());
    }

    private DataConnection createDataConnection() {
        log("createDataConnection E");
        int id = this.mUniqueIdGenerator.getAndIncrement();
        DataConnection dataConnection = DataConnection.makeDataConnection(this.mPhone, id, this, this.mDataServiceManager, this.mDcTesterFailBringUpAll, this.mDcc);
        this.mDataConnections.put(Integer.valueOf(id), dataConnection);
        log("createDataConnection() X id=" + id + " dc=" + dataConnection);
        return dataConnection;
    }

    private void destroyDataConnections() {
        if (this.mDataConnections != null) {
            log("destroyDataConnections: clear mDataConnectionList");
            this.mDataConnections.clear();
            return;
        }
        log("destroyDataConnections: mDataConnecitonList is empty, ignore");
    }

    /* JADX WARNING: Removed duplicated region for block: B:152:0x045c  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x0461  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0184  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0290  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x02a0  */
    private ArrayList<ApnSetting> buildWaitingApns(String requestedApnType, int radioTech) {
        String requestedApnType2;
        String operator;
        boolean usePreferred;
        HwCustDcTracker hwCustDcTracker;
        List<ApnSetting> list;
        int requestedApnTypeBitmask;
        boolean usePreferred2;
        char c;
        char c2;
        ApnSetting apnSetting;
        ApnSetting bip;
        if (IS_NR_SLICE_SUPPORTED) {
            requestedApnType2 = requestedApnType;
            if (requestedApnType2.contains("snssai")) {
                requestedApnType2 = "snssai";
            }
        } else {
            requestedApnType2 = requestedApnType;
        }
        log("buildWaitingApns: E requestedApnType=" + requestedApnType2);
        ArrayList<ApnSetting> apnList = new ArrayList<>();
        int requestedApnTypeBitmask2 = ApnSetting.getApnTypesBitmaskFromString(requestedApnType2);
        if (requestedApnTypeBitmask2 == 8) {
            ArrayList<ApnSetting> dunApns = fetchDunApns();
            if (dunApns.size() > 0) {
                Iterator<ApnSetting> it = dunApns.iterator();
                while (it.hasNext()) {
                    apnList.add(it.next());
                    log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList);
                }
                return sortApnListByPreferred(apnList);
            }
        }
        if (!this.mHwDcTrackerEx.isBipApnType(requestedApnType2) || (bip = this.mHwDcTrackerEx.fetchBipApn(this.mPreferredApn, this.mAllApnSettings)) == null) {
            if (CT_SUPL_FEATURE_ENABLE && "supl".equals(requestedApnType2) && this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getSubId())) {
                ArrayList<ApnSetting> suplApnList = this.mHwDcTrackerEx.buildWaitingApnsForCTSupl(requestedApnType2, radioTech);
                if (!suplApnList.isEmpty()) {
                    return suplApnList;
                }
            }
            String operator2 = this.mHwDcTrackerEx.getCTOperator(this.mIccRecords.get() != null ? this.mIccRecords.get().getOperatorNumeric() : PhoneConfigurationManager.SSSS);
            SubscriptionManager subscriptionManager = this.mSubscriptionManager;
            int slotId = SubscriptionManager.getSlotIndex(this.mPhone.getSubId());
            if (this.isMultiSimEnabled) {
                if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
                    operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(slotId));
                    usePreferred = !this.mPhone.getContext().getResources().getBoolean(17891416);
                    if (usePreferred) {
                        this.mPreferredApn = getPreferredApn();
                    }
                    log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech);
                    boolean isApnCanHandleType = true;
                    hwCustDcTracker = this.mHwCustDcTracker;
                    if (hwCustDcTracker != null) {
                        isApnCanHandleType = hwCustDcTracker.isCanHandleType(this.mPreferredApn, requestedApnType2);
                    }
                    boolean isNeedFilterVowifiMmsForPrefApn = this.mHwDcTrackerEx.isNeedFilterVowifiMms(this.mPreferredApn, requestedApnType2);
                    log("buildWaitingApns: isNeedFilterVowifiMmsForPrefApn = " + isNeedFilterVowifiMmsForPrefApn);
                    if (usePreferred && this.mCanSetPreferApn && (apnSetting = this.mPreferredApn) != null && apnSetting.canHandleType(requestedApnTypeBitmask2) && isApnCanHandleType && !isNeedFilterVowifiMmsForPrefApn) {
                        log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.getOperatorNumeric() + ":" + this.mPreferredApn);
                        if (!this.mPreferredApn.getOperatorNumeric().equals(operator)) {
                            log("buildWaitingApns: no preferred APN");
                            setPreferredApn(-1);
                            this.mPreferredApn = null;
                        } else if (!this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId()) || ((!this.mHwDcTrackerEx.isCTLteNetwork() && !this.mHwDcTrackerEx.isNRNetwork()) || !this.mHwDcTrackerEx.isApnPreset(this.mPreferredApn))) {
                            if (this.mPreferredApn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(radioTech))) {
                                HwCustDcTracker hwCustDcTracker2 = this.mHwCustDcTracker;
                                if (hwCustDcTracker2 == null || hwCustDcTracker2.canKeepApn(requestedApnType2, this.mPreferredApn, true)) {
                                    apnList.add(this.mPreferredApn);
                                    ArrayList<ApnSetting> apnList2 = sortApnListByPreferred(apnList);
                                    log("buildWaitingApns: X added preferred apnList=" + apnList2);
                                    return apnList2;
                                }
                                log("not add preferred apn to WaitingApns, not applicable");
                            } else {
                                log("buildWaitingApns: no preferred APN");
                                setPreferredApn(-1);
                                this.mPreferredApn = null;
                            }
                        } else if (ServiceState.convertNetworkTypeBitmaskToBearerBitmask(this.mPreferredApn.getNetworkTypeBitmask()) != 0 && (this.mPreferredApn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(13)) || this.mPreferredApn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(14)))) {
                            apnList.add(this.mPreferredApn);
                            return apnList;
                        }
                    }
                    list = this.mAllApnSettings;
                    if (list == null && !list.isEmpty()) {
                        log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
                        for (ApnSetting apn : this.mAllApnSettings) {
                            boolean isApnCanHandleType2 = true;
                            HwCustDcTracker hwCustDcTracker3 = this.mHwCustDcTracker;
                            if (hwCustDcTracker3 != null) {
                                isApnCanHandleType2 = hwCustDcTracker3.isCanHandleType(apn, requestedApnType2);
                            }
                            boolean isNeedFilterVowifiMms = this.mHwDcTrackerEx.isNeedFilterVowifiMms(apn, requestedApnType2);
                            log("buildWaitingApns: isNeedFilterVowifiMms = " + isNeedFilterVowifiMms);
                            if (!apn.canHandleType(ApnSetting.getApnTypesBitmaskFromString(requestedApnType2)) || !isApnCanHandleType2 || isNeedFilterVowifiMms) {
                                usePreferred2 = usePreferred;
                                requestedApnTypeBitmask = requestedApnTypeBitmask2;
                                c2 = 14;
                                c = '\r';
                                log("buildWaitingApns: couldn't handle requested ApnType=" + requestedApnType2);
                            } else if (!WAP_APN.equals(apn.getApnName()) || !this.mHwDcTrackerEx.isApnPreset(apn) || !TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(requestedApnType2)) {
                                if (!this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId())) {
                                    usePreferred2 = usePreferred;
                                    requestedApnTypeBitmask = requestedApnTypeBitmask2;
                                    c2 = 14;
                                    c = '\r';
                                } else if (!this.mHwDcTrackerEx.isCTLteNetwork() && !this.mHwDcTrackerEx.isNRNetwork()) {
                                    usePreferred2 = usePreferred;
                                    requestedApnTypeBitmask = requestedApnTypeBitmask2;
                                    c2 = 14;
                                    c = '\r';
                                } else if (ServiceState.convertNetworkTypeBitmaskToBearerBitmask(apn.getNetworkTypeBitmask()) != 0) {
                                    usePreferred2 = usePreferred;
                                    c = '\r';
                                    if (!apn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(13))) {
                                        requestedApnTypeBitmask = requestedApnTypeBitmask2;
                                        c2 = 14;
                                        if (!apn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(14))) {
                                        }
                                    } else {
                                        requestedApnTypeBitmask = requestedApnTypeBitmask2;
                                        c2 = 14;
                                    }
                                    log("buildWaitingApns: adding apn=" + apn.toString());
                                    apnList.add(apn);
                                } else {
                                    usePreferred2 = usePreferred;
                                    requestedApnTypeBitmask = requestedApnTypeBitmask2;
                                    c2 = 14;
                                    c = '\r';
                                }
                                if (apn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(radioTech))) {
                                    HwCustDcTracker hwCustDcTracker4 = this.mHwCustDcTracker;
                                    if (hwCustDcTracker4 == null || hwCustDcTracker4.canKeepApn(requestedApnType2, apn, false)) {
                                        log("buildWaitingApns: adding apn=" + apn);
                                        apnList.add(apn);
                                    } else {
                                        log("not add apn to WaitingApns, not applicable");
                                    }
                                } else {
                                    log("buildWaitingApns: networkTypeBitmask:" + apn.getNetworkTypeBitmask() + "does not include radioTech:" + ServiceState.rilRadioTechnologyToString(radioTech));
                                }
                            } else {
                                log("buildWaitingApns: unicom skip add 3gwap for default, " + apn.toString());
                                usePreferred2 = usePreferred;
                                requestedApnTypeBitmask = requestedApnTypeBitmask2;
                                c2 = 14;
                                c = '\r';
                            }
                            requestedApnTypeBitmask2 = requestedApnTypeBitmask;
                            usePreferred = usePreferred2;
                        }
                    }
                    if (apnList.isEmpty() && TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(requestedApnType2)) {
                        this.mHwDcTrackerEx.updateApnLists(requestedApnType2, radioTech, apnList, operator);
                        loge("apnList is null!");
                    }
                    HwCustDcTracker hwCustDcTracker5 = this.mHwCustDcTracker;
                    ArrayList<ApnSetting> apnList3 = sortApnListByPreferred(hwCustDcTracker5 != null ? hwCustDcTracker5.sortApnListByBearer(apnList, requestedApnType2, radioTech) : apnList);
                    log("buildWaitingApns: " + apnList3.size() + " APNs in the list: " + apnList3);
                    return apnList3;
                }
            } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
                usePreferred = !this.mPhone.getContext().getResources().getBoolean(17891416);
                if (usePreferred) {
                }
                log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech);
                boolean isApnCanHandleType3 = true;
                hwCustDcTracker = this.mHwCustDcTracker;
                if (hwCustDcTracker != null) {
                }
                boolean isNeedFilterVowifiMmsForPrefApn2 = this.mHwDcTrackerEx.isNeedFilterVowifiMms(this.mPreferredApn, requestedApnType2);
                log("buildWaitingApns: isNeedFilterVowifiMmsForPrefApn = " + isNeedFilterVowifiMmsForPrefApn2);
                log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.getOperatorNumeric() + ":" + this.mPreferredApn);
                if (!this.mPreferredApn.getOperatorNumeric().equals(operator)) {
                }
                list = this.mAllApnSettings;
                if (list == null) {
                }
                this.mHwDcTrackerEx.updateApnLists(requestedApnType2, radioTech, apnList, operator);
                loge("apnList is null!");
                HwCustDcTracker hwCustDcTracker52 = this.mHwCustDcTracker;
                ArrayList<ApnSetting> apnList32 = sortApnListByPreferred(hwCustDcTracker52 != null ? hwCustDcTracker52.sortApnListByBearer(apnList, requestedApnType2, radioTech) : apnList);
                log("buildWaitingApns: " + apnList32.size() + " APNs in the list: " + apnList32);
                return apnList32;
            }
            operator = operator2;
            try {
                usePreferred = !this.mPhone.getContext().getResources().getBoolean(17891416);
            } catch (Resources.NotFoundException e) {
                log("buildWaitingApns: usePreferred NotFoundException set to true");
                usePreferred = true;
            }
            if (usePreferred) {
            }
            log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech);
            boolean isApnCanHandleType32 = true;
            hwCustDcTracker = this.mHwCustDcTracker;
            if (hwCustDcTracker != null) {
            }
            boolean isNeedFilterVowifiMmsForPrefApn22 = this.mHwDcTrackerEx.isNeedFilterVowifiMms(this.mPreferredApn, requestedApnType2);
            log("buildWaitingApns: isNeedFilterVowifiMmsForPrefApn = " + isNeedFilterVowifiMmsForPrefApn22);
            log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.getOperatorNumeric() + ":" + this.mPreferredApn);
            if (!this.mPreferredApn.getOperatorNumeric().equals(operator)) {
            }
            list = this.mAllApnSettings;
            if (list == null) {
            }
            this.mHwDcTrackerEx.updateApnLists(requestedApnType2, radioTech, apnList, operator);
            loge("apnList is null!");
            HwCustDcTracker hwCustDcTracker522 = this.mHwCustDcTracker;
            ArrayList<ApnSetting> apnList322 = sortApnListByPreferred(hwCustDcTracker522 != null ? hwCustDcTracker522.sortApnListByBearer(apnList, requestedApnType2, radioTech) : apnList);
            log("buildWaitingApns: " + apnList322.size() + " APNs in the list: " + apnList322);
            return apnList322;
        }
        apnList.add(bip);
        log("buildWaitingApns: X added APN_TYPE_BIP apnList=" + apnList);
        return apnList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000a, code lost:
        r0 = getPreferredApnSetId();
     */
    @VisibleForTesting
    public ArrayList<ApnSetting> sortApnListByPreferred(ArrayList<ApnSetting> list) {
        final int preferredApnSetId;
        if (!(list == null || list.size() <= 1 || preferredApnSetId == 0)) {
            list.sort(new Comparator<ApnSetting>() {
                /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass4 */

                public int compare(ApnSetting apn1, ApnSetting apn2) {
                    if (apn1.getApnSetId() == preferredApnSetId) {
                        return -1;
                    }
                    if (apn2.getApnSetId() == preferredApnSetId) {
                        return 1;
                    }
                    return 0;
                }
            });
        }
        return list;
    }

    private String apnListToString(List<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        if (apns == null) {
            return null;
        }
        int size = apns.size();
        for (int i = 0; i < size; i++) {
            result.append('[');
            result.append(apns.get(i).toString());
            result.append(']');
        }
        return result.toString();
    }

    private void setPreferredApn(int pos) {
        if (!this.mCanSetPreferApn) {
            log("setPreferredApn: X !canSEtPreferApn");
            return;
        }
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
    }

    /* access modifiers changed from: package-private */
    public ApnSetting getPreferredApn() {
        List<ApnSetting> list = this.mAllApnSettings;
        if (list == null || list.isEmpty()) {
            log("getPreferredApn: mAllApnSettings is empty");
            return null;
        }
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null && hwCustDcTracker.needRemovedPreferredApn()) {
            return null;
        }
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId())), new String[]{HbpcdLookup.ID, "name", "apn"}, null, null, "name ASC");
        int i = 0;
        if (cursor != null) {
            this.mCanSetPreferApn = true;
        } else {
            this.mCanSetPreferApn = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getPreferredApn: mRequestedApnType=");
        sb.append(this.mRequestedApnType);
        sb.append(" cursor=");
        sb.append(cursor);
        sb.append(" cursor.count=");
        if (cursor != null) {
            i = cursor.getCount();
        }
        sb.append(i);
        log(sb.toString());
        if (this.mCanSetPreferApn && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int pos = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
            for (ApnSetting p : this.mAllApnSettings) {
                if (p != null && p.getId() == pos && p.canHandleType(this.mRequestedApnType)) {
                    log("getPreferredApn: For APN type " + ApnSetting.getApnTypeString(this.mRequestedApnType) + " found apnSetting " + p);
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x04d5  */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x051b  */
    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        HwDataConnectionManager hwDataConnectionManager;
        boolean isProvApn;
        log("handleMessage msg=" + msg);
        this.mHwDcTrackerEx.beforeHandleMessage(msg);
        int i = msg.what;
        boolean z = true;
        boolean isOperatorNormalAndNvcfgFinished = true;
        int i2 = 1;
        switch (i) {
            case 270336:
                AsyncResult ar = (AsyncResult) msg.obj;
                Pair<ApnContext, Integer> pair = (Pair) ar.userObj;
                ApnContext apnContext = (ApnContext) pair.first;
                int generation = ((Integer) pair.second).intValue();
                int requestType = msg.arg2;
                if (apnContext.getConnectionGeneration() != generation) {
                    loge("EVENT_DATA_SETUP_COMPLETE: Dropped the event because generation did not match.");
                    break;
                } else {
                    boolean success = true;
                    int cause = 65536;
                    if (ar.exception != null) {
                        success = false;
                        cause = ((Integer) ar.result).intValue();
                    }
                    onDataSetupComplete(apnContext, success, cause, requestType);
                    break;
                }
            case 270337:
                onRadioAvailable();
                break;
            case DctConstantsExt.EVENT_RECORDS_LOADED /* 270338 */:
                if ((msg.what != 271144 || !this.mHwDcTrackerEx.checkMvnoParams()) && (msg.what != 271144 || (hwDataConnectionManager = this.mHwDcManager) == null || !hwDataConnectionManager.getNamSwitcherForSoftbank() || !this.mHwDcManager.isSoftBankCard(this.mPhoneExt))) {
                    HwTelephonyFactory.getHwDataServiceChrManager().setReceivedSimloadedMsg(this.mPhone, true, this.mApnContexts, this.mDataEnabledSettings.isUserDataEnabled());
                    int subId = this.mPhone.getSubId();
                    if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                        log("Ignoring EVENT_RECORDS_LOADED as subId is not valid: " + subId);
                        break;
                    } else {
                        onRecordsLoadedOrSubIdChanged();
                        break;
                    }
                }
            case 270339:
                trySetupData((ApnContext) msg.obj, 1);
                break;
            default:
                switch (i) {
                    case 270342:
                        onRadioOffOrNotAvailable();
                        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
                        if (hwCustDcTracker != null) {
                            hwCustDcTracker.tryClearRejCause();
                            break;
                        }
                        break;
                    case 270343:
                        onVoiceCallStarted();
                        break;
                    case 270344:
                        onVoiceCallEnded();
                        if (mWcdmaVpEnabled) {
                            this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(true);
                            break;
                        }
                        break;
                    case 270345:
                        onDataConnectionDetached();
                        break;
                    default:
                        int i3 = -1;
                        switch (i) {
                            case 270347:
                                if (msg.what == 270347) {
                                    this.mHwDcTrackerEx.resetDataCureInfo("roamingOn");
                                }
                                onDataRoamingOnOrSettingsChanged(msg.what);
                                break;
                            case 270348:
                                this.mHwDcTrackerEx.resetDataCureInfo("roamingOff");
                                onDataRoamingOff();
                                break;
                            case 270349:
                                int apnType5GSlice = msg.getData().getInt("snssai", -1);
                                log("EVENT_ENABLE_APN apnType5GSlice = " + apnType5GSlice);
                                if (apnType5GSlice == -1) {
                                    onEnableApn(msg.arg1, msg.arg2, (Message) msg.obj);
                                    break;
                                } else {
                                    onEnableApn5GSlice(msg.arg1, msg.arg2, apnType5GSlice, (Message) msg.obj);
                                    break;
                                }
                            case 270350:
                                int apnType5GSlice2 = msg.getData().getInt("snssai", -1);
                                log("EVENT_DISABLE_APN apnType5GSlice = " + apnType5GSlice2);
                                if (apnType5GSlice2 == -1) {
                                    onDisableApn(msg.arg1, msg.arg2);
                                    break;
                                } else {
                                    onDisableApn5GSlice(msg.arg1, msg.arg2, apnType5GSlice2);
                                    break;
                                }
                            case 270351:
                                log("EVENT_DISCONNECT_DONE msg");
                                Pair<ApnContext, Integer> pair2 = (Pair) ((AsyncResult) msg.obj).userObj;
                                ApnContext apnContext2 = (ApnContext) pair2.first;
                                if (apnContext2.getConnectionGeneration() != ((Integer) pair2.second).intValue()) {
                                    loge("EVENT_DISCONNECT_DONE: Dropped the event because generation did not match.");
                                    break;
                                } else {
                                    onDisconnectDone(apnContext2);
                                    break;
                                }
                            case 270352:
                                onDataConnectionAttached();
                                break;
                            case 270353:
                                this.mHwDcTrackerEx.notifyGetTcpSumMsgReportToBooster(msg.arg1);
                                break;
                            case DctConstantsExt.EVENT_DO_RECOVERY /* 270354 */:
                                this.mDsRecoveryHandler.doRecovery();
                                break;
                            case 270355:
                                if (this.mHwDcTrackerEx.isCTSimCard(this.mPhone.getPhoneId())) {
                                    this.mHwDcTrackerEx.updateApnId();
                                }
                                onApnChanged();
                                break;
                            default:
                                switch (i) {
                                    case 270358:
                                        log("EVENT_PS_RESTRICT_ENABLED " + this.mIsPsRestricted);
                                        stopNetStatPoll();
                                        stopDataStallAlarm();
                                        this.mIsPsRestricted = true;
                                        break;
                                    case 270359:
                                        log("EVENT_PS_RESTRICT_DISABLED " + this.mIsPsRestricted);
                                        this.mIsPsRestricted = false;
                                        if (isConnected()) {
                                            startNetStatPoll();
                                            startDataStallAlarm(false);
                                            break;
                                        } else {
                                            if (this.mState == DctConstants.State.FAILED) {
                                                cleanUpAllConnectionsInternal(false, PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                                                this.mReregisterOnReconnectFailure = false;
                                            }
                                            ApnContext apnContext3 = this.mApnContextsByType.get(17);
                                            if (apnContext3 != null) {
                                                apnContext3.setReason(PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                                                trySetupData(apnContext3, 1);
                                                break;
                                            } else {
                                                loge("**** Default ApnContext not found ****");
                                                if (Build.IS_DEBUGGABLE) {
                                                    throw new RuntimeException("Default ApnContext not found");
                                                }
                                            }
                                        }
                                        break;
                                    case 270360:
                                        log("EVENT_CLEAN_UP_CONNECTION");
                                        cleanUpConnectionInternal(true, 2, (ApnContext) msg.obj);
                                        break;
                                    default:
                                        switch (i) {
                                            case 270362:
                                                restartRadio();
                                                break;
                                            case 270365:
                                                if (msg.obj != null && !(msg.obj instanceof String)) {
                                                    msg.obj = null;
                                                }
                                                cleanUpAllConnectionsInternal(true, (String) msg.obj);
                                                break;
                                            case 270369:
                                                onUpdateIcc();
                                                break;
                                            case 271137:
                                                this.mPhone.mCi.resetAllConnections();
                                                break;
                                            case DctConstantsExt.EVENT_DATA_STALL_ALARM_UPDATE_PKT_SUM /* 271148 */:
                                                onDataStallAlarm(msg.arg1);
                                                break;
                                            default:
                                                switch (i) {
                                                    case 270371:
                                                        Pair<ApnContext, Integer> pair3 = (Pair) ((AsyncResult) msg.obj).userObj;
                                                        ApnContext apnContext4 = (ApnContext) pair3.first;
                                                        int generation2 = ((Integer) pair3.second).intValue();
                                                        int requestType2 = msg.arg2;
                                                        if (apnContext4.getConnectionGeneration() != generation2) {
                                                            loge("EVENT_DATA_SETUP_COMPLETE_ERROR: Dropped the event because generation did not match.");
                                                            break;
                                                        } else {
                                                            onDataSetupCompleteError(apnContext4, requestType2);
                                                            break;
                                                        }
                                                    case 270372:
                                                        int i4 = sEnableFailFastRefCounter;
                                                        if (msg.arg1 == 1) {
                                                            i3 = 1;
                                                        }
                                                        sEnableFailFastRefCounter = i4 + i3;
                                                        log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA:  sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                                                        if (sEnableFailFastRefCounter < 0) {
                                                            loge("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: sEnableFailFastRefCounter:" + sEnableFailFastRefCounter + " < 0");
                                                            sEnableFailFastRefCounter = 0;
                                                        }
                                                        boolean enabled = sEnableFailFastRefCounter > 0;
                                                        log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: enabled=" + enabled + " sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                                                        if (this.mFailFast != enabled) {
                                                            this.mFailFast = enabled;
                                                            if (enabled) {
                                                                z = false;
                                                            }
                                                            this.mDataStallNoRxEnabled = z;
                                                            if (this.mDsRecoveryHandler.isNoRxDataStallDetectionEnabled() && getOverallState() == DctConstants.State.CONNECTED && (!this.mInVoiceCall || this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
                                                                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: start data stall");
                                                                stopDataStallAlarm();
                                                                startDataStallAlarm(false);
                                                                break;
                                                            } else {
                                                                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: stop data stall");
                                                                stopDataStallAlarm();
                                                                break;
                                                            }
                                                        }
                                                        break;
                                                    case 270373:
                                                        Bundle bundle = msg.getData();
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
                                                            this.mIsProvisioning = true;
                                                            startProvisioningApnAlarm();
                                                            break;
                                                        } else {
                                                            loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url is empty, ignoring");
                                                            this.mIsProvisioning = false;
                                                            this.mProvisioningUrl = null;
                                                            break;
                                                        }
                                                    case 270374:
                                                        log("CMD_IS_PROVISIONING_APN");
                                                        String apnType = null;
                                                        try {
                                                            Bundle bundle2 = msg.getData();
                                                            if (bundle2 != null) {
                                                                apnType = (String) bundle2.get("apnType");
                                                            }
                                                            if (TextUtils.isEmpty(apnType)) {
                                                                loge("CMD_IS_PROVISIONING_APN: apnType is empty");
                                                                isProvApn = false;
                                                            } else {
                                                                isProvApn = isProvisioningApn(apnType);
                                                            }
                                                        } catch (ClassCastException e2) {
                                                            loge("CMD_IS_PROVISIONING_APN: NO provisioning url ignoring");
                                                            isProvApn = false;
                                                        }
                                                        log("CMD_IS_PROVISIONING_APN: ret=" + isProvApn);
                                                        AsyncChannel asyncChannel = this.mReplyAc;
                                                        if (!isProvApn) {
                                                            i2 = 0;
                                                        }
                                                        asyncChannel.replyToMessage(msg, 270374, i2);
                                                        break;
                                                    case 270375:
                                                        log("EVENT_PROVISIONING_APN_ALARM");
                                                        ApnContext apnCtx = this.mApnContextsByType.get(17);
                                                        if (apnCtx.isProvisioningApn() && apnCtx.isConnectedOrConnecting()) {
                                                            if (this.mProvisioningApnAlarmTag != msg.arg1) {
                                                                log("EVENT_PROVISIONING_APN_ALARM: ignore stale tag, mProvisioningApnAlarmTag:" + this.mProvisioningApnAlarmTag + " != arg1:" + msg.arg1);
                                                                break;
                                                            } else {
                                                                log("EVENT_PROVISIONING_APN_ALARM: Disconnecting");
                                                                this.mIsProvisioning = false;
                                                                this.mProvisioningUrl = null;
                                                                stopProvisioningApnAlarm();
                                                                cleanUpConnectionInternal(true, 2, apnCtx);
                                                                break;
                                                            }
                                                        } else {
                                                            log("EVENT_PROVISIONING_APN_ALARM: Not connected ignore");
                                                            break;
                                                        }
                                                        break;
                                                    case 270376:
                                                        if (msg.arg1 != 1) {
                                                            if (msg.arg1 == 0) {
                                                                handleStopNetStatPoll((DctConstants.Activity) msg.obj);
                                                                break;
                                                            }
                                                        } else {
                                                            handleStartNetStatPoll((DctConstants.Activity) msg.obj);
                                                            break;
                                                        }
                                                        break;
                                                    case 270377:
                                                        int dataRat = getDataRat();
                                                        if (dataRat != 0 && dataRat != this.mOldRat) {
                                                            this.mOldRat = dataRat;
                                                            onUpdateIcc();
                                                            cleanUpConnectionsOnUpdatedApns(false, "nwTypeChanged");
                                                            setupDataOnAllConnectableApns("nwTypeChanged", RetryFailures.ONLY_ON_CHANGE);
                                                            break;
                                                        } else {
                                                            this.mOldRat = dataRat;
                                                            break;
                                                        }
                                                    case 270378:
                                                        if (this.mProvisioningSpinner == msg.obj) {
                                                            this.mProvisioningSpinner.dismiss();
                                                            this.mProvisioningSpinner = null;
                                                            break;
                                                        }
                                                        break;
                                                    default:
                                                        switch (i) {
                                                            case 270380:
                                                                onNetworkStatusChanged(msg.arg1, (String) msg.obj);
                                                                break;
                                                            case 270381:
                                                                handlePcoData((AsyncResult) msg.obj);
                                                                break;
                                                            case 270382:
                                                                AsyncResult ar2 = (AsyncResult) msg.obj;
                                                                if (ar2.result instanceof Pair) {
                                                                    Pair<Boolean, Integer> p = (Pair) ar2.result;
                                                                    onDataEnabledChanged(((Boolean) p.first).booleanValue(), ((Integer) p.second).intValue());
                                                                    break;
                                                                }
                                                                break;
                                                            case 270383:
                                                                onDataReconnect(msg.getData());
                                                                break;
                                                            case 270384:
                                                                break;
                                                            case 270385:
                                                                onDataServiceBindingChanged(((Boolean) ((AsyncResult) msg.obj).result).booleanValue());
                                                                break;
                                                            case 270386:
                                                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
                                                                if (!sp.contains(Phone.DATA_ROAMING_IS_USER_SETTING_KEY)) {
                                                                    sp.edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, false).commit();
                                                                    break;
                                                                }
                                                                break;
                                                            case 270387:
                                                                onDataEnabledOverrideRulesChanged();
                                                                break;
                                                            default:
                                                                switch (i) {
                                                                    case DctConstantsExt.EVENT_IMSI_READY /* 271144 */:
                                                                        break;
                                                                    case 271145:
                                                                        AsyncResult arNvcfg = (AsyncResult) msg.obj;
                                                                        if (arNvcfg != null && arNvcfg.exception == null) {
                                                                            int nvcfgResult = ((Integer) arNvcfg.result).intValue();
                                                                            String operator = this.mIccRecords.get() != null ? this.mIccRecords.get().getOperatorNumeric() : PhoneConfigurationManager.SSSS;
                                                                            log("EVENT_UNSOL_SIM_NVCFG_FINISHED: operator=" + operator + ", nvcfgResult=" + nvcfgResult);
                                                                            if (TextUtils.isEmpty(operator) || nvcfgResult != 1) {
                                                                                isOperatorNormalAndNvcfgFinished = false;
                                                                            }
                                                                            if (!this.mHwDcTrackerEx.isBlockSetInitialAttachApn() && isOperatorNormalAndNvcfgFinished) {
                                                                                setInitialAttachApn();
                                                                                break;
                                                                            }
                                                                        } else {
                                                                            loge("EVENT_UNSOL_SIM_NVCFG_FINISHED: ar exception.");
                                                                            break;
                                                                        }
                                                                        break;
                                                                    case 271146:
                                                                        onOtaAttachFailed((ApnContext) msg.obj);
                                                                        break;
                                                                    default:
                                                                        Rlog.e("DcTracker", "Unhandled event=" + msg);
                                                                        break;
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
        AsyncResult ar3 = this.mHwCustDcTracker;
        if (ar3 != null) {
            ar3.handleCustMessage(msg);
        }
        this.mHwDcTrackerEx.handleCustMessage(msg);
    }

    private int getApnProfileID(String apnType) {
        if (TextUtils.equals(apnType, "ims") || TextUtils.equals(apnType, "xcap")) {
            return 2;
        }
        if (TextUtils.equals(apnType, "fota")) {
            return 3;
        }
        if (TextUtils.equals(apnType, "cbs")) {
            return 4;
        }
        if (!TextUtils.equals(apnType, "ia") && TextUtils.equals(apnType, "dun")) {
            return 1;
        }
        return 0;
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
        if (VSimUtilsInner.isHisiVSimSlot(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.fetchVSimIccRecords(appFamily);
        }
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    private boolean onUpdateIcc() {
        if (this.mUiccController == null) {
            loge("onUpdateIcc: mUiccController is null. Error!");
            return false;
        }
        int appFamily = 1;
        if (VSimUtilsInner.isVSimPhone(this.mPhone)) {
            appFamily = 1;
        } else if (this.mPhone.getPhoneType() == 1) {
            appFamily = 1;
        } else if (this.mPhone.getPhoneType() == 2) {
            appFamily = 2;
        } else {
            log("Wrong phone type");
        }
        IccRecords newIccRecords = getUiccRecords(appFamily);
        StringBuilder sb = new StringBuilder();
        sb.append("onUpdateIcc: newIccRecords ");
        sb.append(newIccRecords != null ? newIccRecords.getClass().getName() : null);
        log(sb.toString());
        UiccCardApplication newUiccApplication = this.mHwDcTrackerEx.getUiccCardApplication(this.mPhone.getPhoneId(), appFamily).getUiccCardApplication();
        IccRecords r = this.mIccRecords.get();
        if (this.mUiccApplcation == newUiccApplication && r == newIccRecords) {
            return false;
        }
        if (this.mUiccApplcation != null) {
            log("Removing stale icc objects.");
            UiccCardApplicationEx applicationEx = new UiccCardApplicationEx();
            applicationEx.setUiccCardApplication(this.mUiccApplcation);
            this.mHwDcTrackerEx.unregisterForGetAdDone(applicationEx);
            if (r != null) {
                IccRecordsEx iccRecordsEx = new IccRecordsEx();
                iccRecordsEx.setIccRecords(r);
                this.mHwDcTrackerEx.unregisterForImsiReady(iccRecordsEx);
                this.mHwDcTrackerEx.unregisterForRecordsLoaded(iccRecordsEx);
                this.mIccRecords.set(null);
            }
            this.mUiccApplcation = null;
            if (this.mHwDcTrackerEx.getAttachedApnSetting() != null) {
                log("clean the mAttachedApnSettings, set with null.");
                this.mHwDcTrackerEx.setAttachedApnSetting(null);
            }
        }
        if (newUiccApplication == null || newIccRecords == null) {
            onSimNotReady();
        } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
            log("New records found");
            this.mUiccApplcation = newUiccApplication;
            this.mIccRecords.set(newIccRecords);
            UiccCardApplicationEx newAppEx = new UiccCardApplicationEx();
            newAppEx.setUiccCardApplication(newUiccApplication);
            IccRecordsEx iccEx = new IccRecordsEx();
            iccEx.setIccRecords(newIccRecords);
            this.mHwDcTrackerEx.registerForImsi(newAppEx, iccEx);
            HwTelephonyFactory.getHwDataServiceChrManager().setRecordsLoadedRegistered(true, this.mPhone.getSubId());
            HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
            if (hwCustDcTracker != null) {
                hwCustDcTracker.registerForFdnRecordsLoaded(newIccRecords);
            }
            newIccRecords.registerForRecordsLoaded(this, DctConstantsExt.EVENT_RECORDS_LOADED, null);
        }
        return true;
    }

    public void update() {
        log("update sub = " + this.mPhone.getSubId());
        log("update(): Active DDS, register for all events now!");
        onUpdateIcc();
        this.mAutoAttachEnabled.set(false);
        this.mPhone.updateCurrentCarrierInProvider();
        HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(false);
    }

    @VisibleForTesting
    public boolean shouldAutoAttach() {
        if (this.mAutoAttachEnabled.get()) {
            return true;
        }
        PhoneSwitcher phoneSwitcher = PhoneSwitcher.getInstance();
        ServiceState serviceState = this.mPhone.getServiceState();
        if (phoneSwitcher == null || serviceState == null || this.mPhone.getPhoneId() == phoneSwitcher.getPreferredDataPhoneId() || serviceState.getVoiceRegState() != 0 || serviceState.getVoiceNetworkType() == 13 || serviceState.getVoiceNetworkType() == 20) {
            return false;
        }
        return true;
    }

    private void notifyAllDataDisconnected() {
        sEnableFailFastRefCounter = 0;
        this.mFailFast = false;
        this.mAllDataDisconnectedRegistrants.notifyRegistrants();
    }

    public void registerForAllDataDisconnected(Handler h, int what) {
        this.mAllDataDisconnectedRegistrants.addUnique(h, what, (Object) null);
        if (isDisconnected()) {
            log("notify All Data Disconnected");
            notifyAllDataDisconnected();
        }
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mAllDataDisconnectedRegistrants.remove(h);
    }

    private void onDataEnabledChanged(boolean enable, int enabledChangedReason) {
        String cleanupReason;
        log("onDataEnabledChanged: enable=" + enable + ", enabledChangedReason=" + enabledChangedReason);
        if (enable) {
            HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
            if (hwCustDcTracker != null) {
                hwCustDcTracker.setDataOrRoamOn(0);
            }
            reevaluateDataConnections();
            setupDataOnAllConnectableApns("dataEnabled", RetryFailures.ALWAYS);
            return;
        }
        if (enabledChangedReason == 1) {
            cleanupReason = "dataDisabledInternal";
        } else if (enabledChangedReason != 4) {
            cleanupReason = PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED;
        } else {
            cleanupReason = PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN;
        }
        cleanUpAllConnectionsInternal(true, cleanupReason);
    }

    public void setDataAllowed(boolean enable, Message response) {
        log("setDataAllowed: enable=" + enable);
        this.isCleanupRequired.set(enable ^ true);
        this.mPhone.mCi.setDataAllowed(enable, response);
        this.mDataEnabledSettings.setInternalDataEnabled(enable);
    }

    /* access modifiers changed from: protected */
    public boolean isDefaultDataSubscription() {
        long subId = (long) this.mPhone.getSubId();
        if (VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId())) {
            return true;
        }
        long defaultDds = (long) SubscriptionController.getInstance().getDefaultDataSubId();
        log("isDefaultDataSubscription subId: " + subId + "defaultDds: " + defaultDds);
        if (subId == defaultDds) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.i(this.mLogTag, s);
    }

    private void loge(String s) {
        Rlog.e(this.mLogTag, s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("DcTracker:");
        pw.println(" RADIO_TESTS=false");
        pw.println(" mDataEnabledSettings=" + this.mDataEnabledSettings);
        pw.println(" isDataAllowed=" + isDataAllowed(null));
        pw.flush();
        pw.println(" mRequestedApnType=" + this.mRequestedApnType);
        pw.println(" mPhone=" + this.mPhone.getPhoneName());
        pw.println(" mActivity=" + this.mActivity);
        pw.println(" mState=" + this.mState);
        pw.println(" mTxPkts=" + this.mTxPkts);
        pw.println(" mRxPkts=" + this.mRxPkts);
        pw.println(" mNetStatPollPeriod=" + this.mNetStatPollPeriod);
        pw.println(" mNetStatPollEnabled=" + this.mNetStatPollEnabled);
        pw.println(" mDataStallTcpTxRxSum=" + this.mDataStallTcpTxRxSum);
        pw.println(" mDataStallDnsTxRxSum=" + this.mDataStallDnsTxRxSum);
        pw.println(" mDataStallAlarmTag=" + this.mDataStallAlarmTag);
        pw.println(" mDataStallNoRxEnabled=" + this.mDataStallNoRxEnabled);
        pw.println(" mEmergencyApn=" + this.mEmergencyApn);
        pw.println(" mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        pw.println(" mNoRecvPollCount=" + this.mNoRecvPollCount);
        pw.println(" mResolver=" + this.mResolver);
        pw.println(" mReconnectIntent=" + this.mReconnectIntent);
        pw.println(" mAutoAttachEnabled=" + this.mAutoAttachEnabled.get());
        pw.println(" mIsScreenOn=" + this.mIsScreenOn);
        pw.println(" mUniqueIdGenerator=" + this.mUniqueIdGenerator);
        pw.println(" mDataServiceBound=" + this.mDataServiceBound);
        pw.println(" mDataRoamingLeakageLog= ");
        this.mDataRoamingLeakageLog.dump(fd, pw, args);
        pw.println(" mApnSettingsInitializationLog= ");
        this.mApnSettingsInitializationLog.dump(fd, pw, args);
        pw.flush();
        pw.println(" ***************************************");
        DcController dcc = this.mDcc;
        if (dcc == null) {
            pw.println(" mDcc=null");
        } else if (this.mDataServiceBound) {
            dcc.dump(fd, pw, args);
        } else {
            pw.println(" Can't dump mDcc because data service is not bound.");
        }
        pw.println(" ***************************************");
        if (this.mDataConnections != null) {
            Set<Map.Entry<Integer, DataConnection>> mDcSet = this.mDataConnections.entrySet();
            pw.println(" mDataConnections: count=" + mDcSet.size());
            for (Map.Entry<Integer, DataConnection> entry : mDcSet) {
                pw.printf(" *** mDataConnection[%d] \n", entry.getKey());
                entry.getValue().dump(fd, pw, args);
            }
        } else {
            pw.println("mDataConnections=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        HashMap<String, Integer> apnToDcId = this.mApnToDataConnectionId;
        if (apnToDcId != null) {
            Set<Map.Entry<String, Integer>> apnToDcIdSet = apnToDcId.entrySet();
            pw.println(" mApnToDataConnectonId size=" + apnToDcIdSet.size());
            for (Map.Entry<String, Integer> entry2 : apnToDcIdSet) {
                pw.printf(" mApnToDataConnectonId[%s]=%d\n", entry2.getKey(), entry2.getValue());
            }
        } else {
            pw.println("mApnToDataConnectionId=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        ConcurrentHashMap<String, ApnContext> apnCtxs = this.mApnContexts;
        if (apnCtxs != null) {
            Set<Map.Entry<String, ApnContext>> apnCtxsSet = apnCtxs.entrySet();
            pw.println(" mApnContexts size=" + apnCtxsSet.size());
            for (Map.Entry<String, ApnContext> entry3 : apnCtxsSet) {
                entry3.getValue().dump(fd, pw, args);
            }
            pw.println(" ***************************************");
        } else {
            pw.println(" mApnContexts=null");
        }
        pw.flush();
        pw.println(" mAllApnSettings size=" + this.mAllApnSettings.size());
        for (int i = 0; i < this.mAllApnSettings.size(); i++) {
            pw.printf(" mAllApnSettings[%d]: %s\n", Integer.valueOf(i), this.mAllApnSettings.get(i));
        }
        pw.flush();
        pw.println(" mPreferredApn=" + this.mPreferredApn);
        pw.println(" mIsPsRestricted=" + this.mIsPsRestricted);
        pw.println(" mIsDisposed=" + this.mIsDisposed);
        pw.println(" mIntentReceiver=" + this.mIntentReceiver);
        pw.println(" mReregisterOnReconnectFailure=" + this.mReregisterOnReconnectFailure);
        pw.println(" canSetPreferApn=" + this.mCanSetPreferApn);
        pw.println(" mApnObserver=" + this.mApnObserver);
        pw.println(" getOverallState=" + getOverallState());
        pw.println(" mAttached=" + this.mAttached.get());
        this.mDataEnabledSettings.dump(fd, pw, args);
        pw.flush();
    }

    public String[] getPcscfAddress(String apnType) {
        ApnContext apnContext;
        log("getPcscfAddress()");
        if (apnType == null) {
            log("apnType is null, return null");
            return null;
        }
        if (TextUtils.equals(apnType, "emergency")) {
            apnContext = this.mApnContextsByType.get(ApnSettingHelper.TYPE_EMERGENCY);
        } else if (TextUtils.equals(apnType, "ims")) {
            apnContext = this.mApnContextsByType.get(64);
        } else {
            log("apnType is invalid, return null");
            return null;
        }
        if (apnContext == null) {
            log("apnContext is null, return null");
            return null;
        }
        DataConnection dataConnection = apnContext.getDataConnection();
        if (dataConnection == null) {
            return null;
        }
        String[] result = dataConnection.getPcscfAddresses();
        if (result != null) {
            for (int i = 0; i < result.length; i++) {
                log("Pcscf[" + i + "]: " + result[i]);
            }
        }
        return result;
    }

    private void initEmergencyApnSetting() {
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "filtered"), null, "type=\"emergency\"", null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                this.mEmergencyApn = ApnSetting.makeApnSetting(cursor);
            }
            cursor.close();
        }
        if (this.mEmergencyApn == null) {
            this.mEmergencyApn = new ApnSetting.Builder().setEntryName("Emergency").setProtocol(2).setApnName("sos").setApnTypeBitmask(ApnSettingHelper.TYPE_EMERGENCY).build();
        }
    }

    private void addEmergencyApnSetting() {
        if (!this.mEmergencyApnLoaded) {
            initEmergencyApnSetting();
            this.mEmergencyApnLoaded = true;
        }
        if (this.mEmergencyApn != null) {
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.canHandleType(ApnSettingHelper.TYPE_EMERGENCY)) {
                    log("addEmergencyApnSetting - E-APN setting is already present");
                    return;
                }
            }
            if (!this.mAllApnSettings.contains(this.mEmergencyApn)) {
                this.mAllApnSettings.add(this.mEmergencyApn);
                log("Adding emergency APN : " + this.mEmergencyApn);
            }
        }
    }

    private boolean containsAllApns(ArrayList<ApnSetting> oldApnList, ArrayList<ApnSetting> newApnList) {
        Iterator<ApnSetting> it = newApnList.iterator();
        while (it.hasNext()) {
            ApnSetting newApnSetting = it.next();
            boolean canHandle = false;
            Iterator<ApnSetting> it2 = oldApnList.iterator();
            while (true) {
                if (it2.hasNext()) {
                    if (it2.next().equals(newApnSetting, this.mPhone.getServiceState().getDataRoamingFromRegistration())) {
                        canHandle = true;
                        continue;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!canHandle) {
                return false;
            }
        }
        return true;
    }

    private void cleanUpConnectionsOnUpdatedApns(boolean detach, String reason) {
        log("cleanUpConnectionsOnUpdatedApns: detach=" + detach);
        if (this.mAllApnSettings.isEmpty() && this.mCureApnSettings.isEmpty()) {
            cleanUpAllConnectionsInternal(detach, "apnChanged");
        } else if (getDataRat() != 0) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                ArrayList<ApnSetting> currentWaitingApns = apnContext.getWaitingApns();
                ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), getDataRat());
                log("new waitingApns:" + waitingApns);
                if (currentWaitingApns != null && (waitingApns.size() != currentWaitingApns.size() || !containsAllApns(currentWaitingApns, waitingApns))) {
                    log("new waiting apn is different for " + apnContext);
                    apnContext.setWaitingApns(waitingApns);
                    if (!apnContext.isDisconnected()) {
                        if (HW_DBG) {
                            log("cleanUpConnectionsOnUpdatedApns for " + apnContext);
                        }
                        apnContext.setReason(reason);
                        cleanUpConnectionInternal(true, 2, apnContext);
                    }
                }
            }
        } else {
            return;
        }
        if (!isConnected()) {
            stopNetStatPoll();
            stopDataStallAlarm();
        }
        this.mRequestedApnType = 17;
        log("mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (detach && this.mDisconnectPendingCount == 0) {
            notifyAllDataDisconnected();
        }
    }

    private void resetPollStats() {
        this.mTxPkts = -1;
        this.mRxPkts = -1;
        this.mNetStatPollPeriod = 1000;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startNetStatPoll() {
        if (getOverallState() == DctConstants.State.CONNECTED && !this.mNetStatPollEnabled) {
            log("startNetStatPoll");
            resetPollStats();
            this.mNetStatPollEnabled = true;
            this.mPollNetStat.run();
        }
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyDataActivity();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopNetStatPoll() {
        this.mNetStatPollEnabled = false;
        removeCallbacks(this.mPollNetStat);
        log("stopNetStatPoll");
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyDataActivity();
        }
    }

    public void sendStartNetStatPoll(DctConstants.Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 1;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStartNetStatPoll(DctConstants.Activity activity) {
        startNetStatPoll();
        startDataStallAlarm(false);
        setActivity(activity);
    }

    public void sendStopNetStatPoll(DctConstants.Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 0;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStopNetStatPoll(DctConstants.Activity activity) {
        stopNetStatPoll();
        stopDataStallAlarm();
        setActivity(activity);
    }

    private void onDataEnabledOverrideRulesChanged() {
        log("onDataEnabledOverrideRulesChanged");
        Iterator<ApnContext> it = this.mPrioritySortedApnContexts.iterator();
        while (it.hasNext()) {
            ApnContext apnContext = it.next();
            if (isDataAllowed(apnContext, 1, null)) {
                if (apnContext.getDataConnection() != null) {
                    apnContext.getDataConnection().reevaluateRestrictedState();
                }
                setupDataOnConnectableApn(apnContext, PhoneInternalInterface.REASON_DATA_ENABLED_OVERRIDE, RetryFailures.ALWAYS);
            } else if (shouldCleanUpConnection(apnContext, true)) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED_OVERRIDE);
                cleanUpConnectionInternal(true, 2, apnContext);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDataActivity() {
        DctConstants.Activity newActivity;
        TxRxSum preTxRxSum = new TxRxSum(this.mTxPkts, this.mRxPkts);
        TxRxSum curTxRxSum = new TxRxSum();
        curTxRxSum.updateTotalTxRxSum();
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
            if (sent > 0 && received > 0) {
                newActivity = DctConstants.Activity.DATAINANDOUT;
                this.mHwDcTrackerEx.updateDSUseDuration();
            } else if (sent > 0 && received == 0) {
                newActivity = DctConstants.Activity.DATAOUT;
                this.mHwDcTrackerEx.updateDSUseDuration();
            } else if (sent != 0 || received <= 0) {
                newActivity = this.mActivity == DctConstants.Activity.DORMANT ? this.mActivity : DctConstants.Activity.NONE;
            } else {
                newActivity = DctConstants.Activity.DATAIN;
                this.mHwDcTrackerEx.updateDSUseDuration();
            }
            if (this.mActivity != newActivity && this.mIsScreenOn) {
                log("updateDataActivity: newActivity=" + newActivity);
                this.mActivity = newActivity;
                this.mPhone.notifyDataActivity();
            }
        }
    }

    private void handlePcoData(AsyncResult ar) {
        if (ar.exception != null) {
            loge("PCO_DATA exception: " + ar.exception);
            return;
        }
        PcoData pcoData = (PcoData) ar.result;
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            hwCustDcTracker.savePcoData(pcoData);
        }
        ArrayList<DataConnection> dcList = new ArrayList<>();
        DataConnection temp = this.mDcc.getActiveDcByCid(pcoData.cid);
        if (temp != null) {
            dcList.add(temp);
        }
        if (dcList.size() == 0) {
            loge("PCO_DATA for unknown cid: " + pcoData.cid + ", inferring");
            Iterator<DataConnection> it = this.mDataConnections.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                DataConnection dc = it.next();
                int cid = dc.getCid();
                if (cid == pcoData.cid) {
                    log("  found " + dc);
                    dcList.clear();
                    dcList.add(dc);
                    break;
                } else if (cid == -1) {
                    Iterator<ApnContext> it2 = dc.getApnContexts().iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            if (it2.next().getState() == DctConstants.State.CONNECTING) {
                                log("  found potential " + dc);
                                dcList.add(dc);
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        if (dcList.size() == 0) {
            loge("PCO_DATA - couldn't infer cid");
            return;
        }
        Iterator<DataConnection> it3 = dcList.iterator();
        while (it3.hasNext()) {
            List<ApnContext> apnContextList = it3.next().getApnContexts();
            if (apnContextList.size() != 0) {
                for (ApnContext apnContext : apnContextList) {
                    String apnType = apnContext.getApnType();
                    Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                    intent.putExtra("apnType", apnType);
                    intent.putExtra("apnProto", pcoData.bearerProto);
                    intent.putExtra("pcoId", pcoData.pcoId);
                    intent.putExtra("pcoValue", pcoData.contents);
                    this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public class DataStallRecoveryHandler {
        private static final int DEFAULT_MIN_DURATION_BETWEEN_RECOVERY_STEPS_IN_MS = 180000;
        private boolean mIsValidNetwork;
        private long mTimeLastRecoveryStartMs;

        public DataStallRecoveryHandler() {
            reset();
        }

        public void reset() {
            this.mTimeLastRecoveryStartMs = 0;
            putRecoveryAction(0);
            DcTracker.this.mHwDcTrackerEx.notifyBoosterDoRecovery(0);
        }

        public boolean isAggressiveRecovery() {
            int action = getRecoveryAction();
            return action == 1 || action == 2 || action == 3 || action == 4;
        }

        private long getMinDurationBetweenRecovery() {
            return Settings.Global.getLong(DcTracker.this.mResolver, "min_duration_between_recovery_steps", 180000);
        }

        private long getElapsedTimeSinceRecoveryMs() {
            return SystemClock.elapsedRealtime() - this.mTimeLastRecoveryStartMs;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getRecoveryAction() {
            return Settings.System.getInt(DcTracker.this.mResolver, "radio.data.stall.recovery.action", 0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void putRecoveryAction(int action) {
            Settings.System.putInt(DcTracker.this.mResolver, "radio.data.stall.recovery.action", action);
        }

        private void broadcastDataStallDetected(int recoveryAction) {
            Intent intent = new Intent("android.intent.action.DATA_STALL_DETECTED");
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, DcTracker.this.mPhone.getPhoneId());
            intent.putExtra("recoveryAction", recoveryAction);
            DcTracker.this.mPhone.getContext().sendBroadcast(intent, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        }

        private boolean isRecoveryAlreadyStarted() {
            return getRecoveryAction() != 0;
        }

        private boolean checkRecovery() {
            if (DcTracker.this.mHwDcTrackerEx.isPingOk()) {
                DcTracker.this.log("Wait for ping thread result, no need doRecovery.");
                return false;
            } else if (getElapsedTimeSinceRecoveryMs() >= getMinDurationBetweenRecovery() && DcTracker.this.mAttached.get() && DcTracker.this.isDataAllowed(null)) {
                return true;
            } else {
                return false;
            }
        }

        private void triggerRecovery() {
            DcTracker.this.mHwDcTrackerEx.setIsDorecoveryTrigger(true);
            DcTracker.this.mHwDcTrackerEx.actionProcess(getRecoveryAction());
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessage(dcTracker.obtainMessage(DctConstantsExt.EVENT_DO_RECOVERY));
        }

        public void doRecovery() {
            if (DcTracker.this.getOverallState() == DctConstants.State.CONNECTED) {
                int recoveryAction = getRecoveryAction();
                TelephonyMetrics.getInstance().writeDataStallEvent(DcTracker.this.mPhone.getPhoneId(), recoveryAction);
                broadcastDataStallDetected(recoveryAction);
                if (DcTracker.this.mHwDcTrackerEx.noNeedDoRecovery()) {
                    DcTracker.this.log("noNeedDoRecovery return true, so we needn't to do recovery.");
                    return;
                }
                if (DcTracker.this.mHwDcTrackerEx.isDorecoveryTrigger()) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDorecovery(DcTracker.this.mPhone, recoveryAction, DcTracker.this.mRecoveryReason);
                    DcTracker.this.mHwDcTrackerEx.setIsDorecoveryTrigger(false);
                }
                if (recoveryAction == 0) {
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("doRecovery() get data call list");
                    DcTracker.this.mDataServiceManager.requestDataCallList(DcTracker.this.obtainMessage());
                    if (!DcTracker.this.mIsScreenOn) {
                        DcTracker.this.log("DoRecovery first level screen off restart timer.");
                        DcTracker.this.stopDataStallAlarm();
                        DcTracker.this.startDataStallAlarm(true);
                    }
                    putRecoveryAction(1);
                } else if (recoveryAction == 1) {
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_CLEANUP, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("doRecovery() cleanup all connections");
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.cleanUpConnection((ApnContext) dcTracker.mApnContexts.get(ApnSetting.getApnTypeString(17)));
                    putRecoveryAction(2);
                } else if (recoveryAction == 2) {
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_REREGISTER, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("doRecovery() re-register");
                    DcTracker.this.mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    putRecoveryAction(3);
                } else if (recoveryAction == 3) {
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("doRecovery() restarting radio");
                    DcTracker.this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(true);
                    DcTracker.this.restartRadio();
                    putRecoveryAction(4);
                } else if (recoveryAction == 4) {
                    int ret = DcTracker.this.mHwDcTrackerEx.notifyBoosterDoRecovery(1);
                    DcTracker dcTracker2 = DcTracker.this;
                    dcTracker2.log("doRecovery() action_oem_ext " + ret);
                    if (ret != 0) {
                        reset();
                    }
                } else {
                    throw new RuntimeException("doRecovery: Invalid recoveryAction=" + recoveryAction);
                }
                DcTracker.this.mHwDcTrackerEx.updateLastDoRecoveryTimestamp(recoveryAction);
                DcTracker.this.mSentSinceLastRecv = 0;
                this.mTimeLastRecoveryStartMs = SystemClock.elapsedRealtime();
            }
        }

        public void processNetworkStatusChanged(boolean isValid) {
            if (isValid) {
                this.mIsValidNetwork = true;
                reset();
            } else if (this.mIsValidNetwork || isRecoveryAlreadyStarted()) {
                this.mIsValidNetwork = false;
                if (checkRecovery()) {
                    DcTracker.this.log("trigger data stall recovery");
                    triggerRecovery();
                }
            }
        }

        public boolean isRecoveryOnBadNetworkEnabled() {
            return Settings.Global.getInt(DcTracker.this.mResolver, "data_stall_recovery_on_bad_network", 0) == 1;
        }

        public boolean isNoRxDataStallDetectionEnabled() {
            return DcTracker.this.mDataStallNoRxEnabled && !isRecoveryOnBadNetworkEnabled();
        }
    }

    private void updateDataStallInfo() {
        if (HW_RADIO_DATA_STALL_ENABLE) {
            this.mHwDcTrackerEx.updateRecoveryPktStat();
            return;
        }
        TxRxSum preTxRxSum = new TxRxSum(this.mDataStallTxRxSum);
        this.mDataStallTxRxSum.updateTcpTxRxSum();
        long sent = this.mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        long received = this.mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;
        if (sent > 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            this.mDsRecoveryHandler.reset();
        } else if (sent > 0 && received == 0) {
            if (isPhoneStateIdle()) {
                this.mSentSinceLastRecv += sent;
            } else {
                this.mSentSinceLastRecv = 0;
            }
            log("updateDataStallInfo: OUT sent=" + sent + " mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        } else if (sent == 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            this.mDsRecoveryHandler.reset();
        }
    }

    private boolean isPhoneStateIdle() {
        for (int i = 0; i < this.mTelephonyManager.getPhoneCount(); i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null && phone.getState() != PhoneConstants.State.IDLE) {
                log("isPhoneStateIdle: Voice call active on sub: " + i);
                return false;
            }
        }
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone == null || !imsPhone.mHwImsPhoneEx.isBusy()) {
            return true;
        }
        log("isPhoneStateIdle: ImsPhone isBusy true");
        return false;
    }

    private void onDataStallAlarm(int tag) {
        if (this.mDataStallAlarmTag != tag) {
            log("onDataStallAlarm: ignore, tag=" + tag + " expecting " + this.mDataStallAlarmTag);
            return;
        }
        log("Data stall alarm");
        updateDataStallInfo();
        boolean suspectedStall = false;
        if (this.mSentSinceLastRecv >= ((long) Settings.Global.getInt(this.mResolver, "pdp_watchdog_trigger_packet_count", 10))) {
            if (!this.mHwDcTrackerEx.isPingOk()) {
                log("onDataStallAlarm: tag=" + tag + " do recovery action=" + this.mDsRecoveryHandler.getRecoveryAction());
                suspectedStall = true;
            } else {
                this.mSentSinceLastRecv = 0;
            }
        }
        startDataStallAlarm(suspectedStall);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDataStallAlarm(boolean suspectedStall) {
        int delayInMs;
        if (HuaweiTelephonyConfigs.isMTKPlatform() && !isDefaultDataSubscription()) {
            log("startDataStallAlarm: not start on non defaultDds");
        } else if (this.mDsRecoveryHandler.isNoRxDataStallDetectionEnabled() && getOverallState() == DctConstants.State.CONNECTED) {
            if (this.mIsScreenOn || suspectedStall || this.mDsRecoveryHandler.isAggressiveRecovery()) {
                delayInMs = Settings.Global.getInt(this.mResolver, "data_stall_alarm_aggressive_delay_in_ms", 60000);
            } else {
                if (SystemProperties.getBoolean("ro.config.power", false)) {
                    DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 6000000;
                }
                delayInMs = Settings.Global.getInt(this.mResolver, "data_stall_alarm_non_aggressive_delay_in_ms", DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            }
            this.mDataStallAlarmTag++;
            Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
            intent.putExtra(INTENT_DATA_STALL_ALARM_EXTRA_TAG, this.mDataStallAlarmTag);
            intent.putExtra(INTENT_DATA_STALL_ALARM_EXTRA_TRANSPORT_TYPE, this.mTransportType);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            int requestCode = getRequestCode(this.mPhone.getPhoneId());
            this.mDataStallAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), requestCode, intent, 201326592);
            log("startDataStallAlarm requestCode is: " + requestCode);
            this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mDataStallAlarmIntent);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDataStallAlarm() {
        this.mDataStallAlarmTag++;
        PendingIntent pendingIntent = this.mDataStallAlarmIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mDataStallAlarmIntent = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restartDataStallAlarm() {
        if (isConnected()) {
            if (this.mDsRecoveryHandler.isAggressiveRecovery()) {
                log("restartDataStallAlarm: action is pending. not resetting the alarm.");
                return;
            }
            stopDataStallAlarm();
            startDataStallAlarm(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentProvisioningApnAlarm(Intent intent) {
        log("onActionIntentProvisioningApnAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(270375, intent.getAction());
        msg.arg1 = intent.getIntExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    private void startProvisioningApnAlarm() {
        int delayInMs = Settings.Global.getInt(this.mResolver, "provisioning_apn_alarm_delay_in_ms", PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT);
        if (Build.IS_DEBUGGABLE) {
            try {
                delayInMs = Integer.parseInt(System.getProperty(DEBUG_PROV_APN_ALARM, Integer.toString(delayInMs)));
            } catch (NumberFormatException e) {
                loge("startProvisioningApnAlarm: e=" + e);
            }
        }
        this.mProvisioningApnAlarmTag++;
        log("startProvisioningApnAlarm: tag=" + this.mProvisioningApnAlarmTag + " delay=" + (delayInMs / 1000) + "s");
        Intent intent = new Intent(INTENT_PROVISIONING_APN_ALARM);
        intent.addFlags(268435456);
        intent.putExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, this.mProvisioningApnAlarmTag);
        this.mProvisioningApnAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 201326592);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mProvisioningApnAlarmIntent);
    }

    private void stopProvisioningApnAlarm() {
        log("stopProvisioningApnAlarm: current tag=" + this.mProvisioningApnAlarmTag + " mProvsioningApnAlarmIntent=" + this.mProvisioningApnAlarmIntent);
        this.mProvisioningApnAlarmTag = this.mProvisioningApnAlarmTag + 1;
        PendingIntent pendingIntent = this.mProvisioningApnAlarmIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mProvisioningApnAlarmIntent = null;
        }
    }

    private static DataProfile createDataProfile(ApnSetting apn, boolean isPreferred) {
        return createDataProfile(apn, apn.getProfileId(), isPreferred);
    }

    @VisibleForTesting
    public static DataProfile createDataProfile(ApnSetting apn, int profileId, boolean isPreferred) {
        int profileType;
        int networkTypeBitmask = apn.getNetworkTypeBitmask();
        if (networkTypeBitmask == 0) {
            profileType = 0;
        } else if (ServiceState.bearerBitmapHasCdma(networkTypeBitmask)) {
            profileType = 2;
        } else {
            profileType = 1;
        }
        return new DataProfile.Builder().setProfileId(profileId).setApn(apn.getApnName()).setProtocolType(apn.getProtocol()).setAuthType(apn.getAuthType()).setUserName(apn.getUser()).setPassword(apn.getPassword()).setType(profileType).setMaxConnectionsTime(apn.getMaxConnsTime()).setMaxConnections(apn.getMaxConns()).setWaitTime(apn.getWaitTime()).enable(apn.isEnabled()).setSupportedApnTypesBitmask(apn.getApnTypeBitmask()).setRoamingProtocolType(apn.getRoamingProtocol()).setBearerBitmask(networkTypeBitmask).setMtu(apn.getMtu()).setPersistent(apn.isPersistent()).setPreferred(isPreferred).build();
    }

    private void onDataServiceBindingChanged(boolean bound) {
        if (bound) {
            this.mDcc.start();
        } else {
            this.mDcc.dispose();
        }
        this.mDataServiceBound = bound;
    }

    public static String requestTypeToString(int type) {
        if (type == 1) {
            return "NORMAL";
        }
        if (type != 2) {
            return IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN;
        }
        return "HANDOVER";
    }

    public static String releaseTypeToString(int type) {
        if (type == 1) {
            return "NORMAL";
        }
        if (type == 2) {
            return "DETACH";
        }
        if (type != 3) {
            return IccCardConstantsEx.INTENT_VALUE_ICC_UNKNOWN;
        }
        return "HANDOVER";
    }

    private int getDataRat() {
        NetworkRegistrationInfo nrs = this.mPhone.getServiceState().getNetworkRegistrationInfoHw(2, this.mTransportType);
        if (nrs != null) {
            return ServiceState.networkTypeToRilRadioTechnology(nrs.getAccessNetworkTechnology());
        }
        return 0;
    }

    private int getVoiceRat() {
        NetworkRegistrationInfo nrs = this.mPhone.getServiceState().getNetworkRegistrationInfoHw(1, this.mTransportType);
        if (nrs != null) {
            return ServiceState.networkTypeToRilRadioTechnology(nrs.getAccessNetworkTechnology());
        }
        return 0;
    }

    private boolean isDualPsAllowedForSmartSwitch() {
        PhoneSwitcher phoneSwitcher = PhoneSwitcher.getInstance();
        return phoneSwitcher != null && phoneSwitcher.isDualPsAllowedForSmartSwitch();
    }

    private boolean isMmsApn(ApnContext apnContext) {
        return apnContext != null && "mms".equals(apnContext.getApnType());
    }

    private boolean isXcapApn(ApnContext apnContext) {
        return apnContext != null && "xcap".equals(apnContext.getApnType());
    }

    public List<ApnSetting> getAllApnList() {
        return this.mAllApnSettings;
    }

    public ApnSetting getPreferredApnHw() {
        return getPreferredApn();
    }

    public void setPreferredApnHw(int pos) {
        setPreferredApn(pos);
    }

    public void startNetStatPollHw() {
        startNetStatPoll();
    }

    public void stopNetStatPollHw() {
        stopNetStatPoll();
    }

    public void startDataStallAlarmHw(boolean suspectedStall) {
        startDataStallAlarm(suspectedStall);
    }

    public void stopDataStallAlarmHw() {
        stopDataStallAlarm();
    }

    public void restartDataStallAlarmHw() {
        restartDataStallAlarm();
    }

    public void cancelReconnectAlarmHw(ApnContext apnContext) {
        cancelReconnectAlarm(apnContext);
    }

    public void unregisterForAllEventsHw() {
        unregisterForAllEvents();
    }

    public void registerForAllEventsHw() {
        registerForAllEvents();
        DataEnabledSettings dataEnabledSettings = this.mDataEnabledSettings;
        if (dataEnabledSettings != null) {
            dataEnabledSettings.registerForDataEnabledChanged(this, 270382, null);
            this.mDataEnabledSettings.registerForDataEnabledOverrideChanged(this, 270387);
        }
    }

    public boolean isOnlySingleDcAllowedHw(int rilRadioTech) {
        return isOnlySingleDcAllowed(rilRadioTech);
    }

    public void setupDataOnConnectableApnsHw(String reason) {
        setupDataOnAllConnectableApns(reason, RetryFailures.ALWAYS);
    }

    /* access modifiers changed from: protected */
    public IccRecords getIccRecordsHw() {
        return this.mIccRecords.get();
    }

    /* access modifiers changed from: protected */
    public SparseArray<ApnContext> getApnContextsHw() {
        return this.mApnContextsByType;
    }

    public void createAllApnListHw() {
        createAllApnList();
    }

    public void setInitialAttachApnHw() {
        setInitialAttachApn();
    }

    public ArrayList<ApnContext> getMPrioritySortedApnContexts() {
        return this.mPrioritySortedApnContexts;
    }

    public void cleanUpAllConnectionsHw(String reason) {
        cleanUpAllConnections(reason);
    }

    public void setupDataOnAllConnectableApnsHw(String reason) {
        setupDataOnAllConnectableApns(reason, RetryFailures.ALWAYS);
    }

    public void onTrySetupDataHw(ApnContext apnContext) {
        trySetupData(apnContext, 1);
    }

    public void cleanUpConnectionHw(boolean tearDown, ApnContext apnContext) {
        cleanUpConnectionInternal(tearDown, 1, apnContext);
    }

    public final ConcurrentHashMap<String, ApnContext> getMApnContextsHw() {
        return this.mApnContexts;
    }

    public void notifyOffApnsOfAvailabilityHw() {
        this.mPhone.notifyDataConnection();
    }

    public void notifyDataConnectionHw() {
        this.mPhone.notifyDataConnection();
    }

    public void checkPLMN(String plmn) {
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            hwCustDcTracker.checkPLMN(plmn);
        }
    }

    private void onOtaAttachFailed(ApnContext apnContext) {
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            hwCustDcTracker.onOtaAttachFailed(apnContext);
        }
    }

    private boolean getmIsPseudoImsi() {
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            return hwCustDcTracker.getmIsPseudoImsi();
        }
        return false;
    }

    private void sendOTAAttachTimeoutMsg(ApnContext apnContext, boolean retValue) {
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            hwCustDcTracker.sendOTAAttachTimeoutMsg(apnContext, retValue);
        }
    }

    private void openServiceStart(UiccController uiccController) {
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            hwCustDcTracker.openServiceStart(uiccController);
        }
    }

    private boolean isDataDisable(int slotId) {
        HwCustDcTracker hwCustDcTracker = this.mHwCustDcTracker;
        if (hwCustDcTracker != null) {
            return hwCustDcTracker.isDataDisable(slotId);
        }
        return false;
    }

    public DataEnabledSettings getDataEnabledSettingsHw() {
        return this.mDataEnabledSettings;
    }

    public boolean isPhoneStateIdleHw() {
        return isPhoneStateIdle();
    }

    public void startAlarmForReconnectHw(long delay, ApnContext apnContext) {
        startAlarmForReconnect(delay, apnContext);
    }

    public int getDataRatHw() {
        return getDataRat();
    }

    /* access modifiers changed from: protected */
    public boolean onDataRetryDisableNr(long originDelay, ApnContext apnContext) {
        ApnContextEx apnContextEx = new ApnContextEx();
        apnContextEx.setApnContext(apnContext);
        int action = this.mHwDcTrackerEx.getDataRetryAction(apnContextEx);
        long delay = this.mHwDcTrackerEx.getDataRetryDelay(originDelay, apnContextEx);
        if (action != 3) {
            return false;
        }
        log("onDataRetryDisableNr: Disable NR. delay = " + delay);
        apnContext.setState(DctConstants.State.FAILED);
        this.mHwDcTrackerEx.startAlarmForReenableNr(apnContextEx, delay);
        this.mHwDcTrackerEx.sendDisableNr(apnContextEx, delay);
        return true;
    }

    private void reportToBoosterForPdpNoRetry() {
        IHwCommBoosterServiceManager bm = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (bm != null && this.mTransportType == 1) {
            Bundle data = new Bundle();
            data.putInt("sub", this.mPhone.getSubId());
            int ret = bm.reportBoosterPara("com.android.internal.telephony", (int) TYPE_REPORT_NO_RETRY_FOR_PDP_FAIL, data);
            log("reportBoosterPara ret=" + ret + " subId" + this.mPhone.getSubId());
        }
    }

    public int getApnStatedCount(DctConstants.State state) {
        int result = 0;
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == state) {
                result++;
            }
        }
        return result;
    }

    public void resetRecoveryInfoHw() {
        this.mDsRecoveryHandler.reset();
    }

    public void setRecoveryReasonHw(String reason, boolean isAdd) {
        if (isAdd) {
            this.mRecoveryReason += reason;
            return;
        }
        this.mRecoveryReason = reason;
    }

    public int getRecoveryActionHw() {
        return this.mDsRecoveryHandler.getRecoveryAction();
    }

    public void putRecoveryActionHw(int action) {
        this.mDsRecoveryHandler.putRecoveryAction(action);
    }

    public void setSentSinceLastRecvHw(long sentSinceLastRecv, boolean isAdd) {
        if (isAdd) {
            this.mSentSinceLastRecv += sentSinceLastRecv;
        } else {
            this.mSentSinceLastRecv = sentSinceLastRecv;
        }
    }

    public long getSentSinceLastRecvHw() {
        return this.mSentSinceLastRecv;
    }

    public DcTrackerEx.TxRxSumEx getDataStallTcpTxRxSumHw() {
        DcTrackerEx.TxRxSumEx txRxSumEx = new DcTrackerEx.TxRxSumEx();
        txRxSumEx.setTxRxSum(this.mDataStallTcpTxRxSum);
        return txRxSumEx;
    }

    public DcTrackerEx.TxRxSumEx getDataStallDnsTxRxSumHw() {
        DcTrackerEx.TxRxSumEx txRxSumEx = new DcTrackerEx.TxRxSumEx();
        txRxSumEx.setTxRxSum(this.mDataStallDnsTxRxSum);
        return txRxSumEx;
    }

    public DcTrackerEx.TxRxSumEx getPreDataStallTcpTxRxSumHw() {
        TxRxSum preTcpTxRxSum = new TxRxSum(this.mDataStallTcpTxRxSum);
        DcTrackerEx.TxRxSumEx txRxSumEx = new DcTrackerEx.TxRxSumEx();
        txRxSumEx.setTxRxSum(preTcpTxRxSum);
        return txRxSumEx;
    }

    public DcTrackerEx.TxRxSumEx getPreDataStallDnsTxRxSumHw() {
        TxRxSum preDnsTxRxSum = new TxRxSum(this.mDataStallDnsTxRxSum);
        DcTrackerEx.TxRxSumEx txRxSumEx = new DcTrackerEx.TxRxSumEx();
        txRxSumEx.setTxRxSum(preDnsTxRxSum);
        return txRxSumEx;
    }

    public void clearDefaultLink() {
        this.mHwDcTrackerEx.clearDefaultLink();
    }

    public void resumeDefaultLink() {
        this.mHwDcTrackerEx.resumeDefaultLink();
    }

    public IHwDcTrackerEx getHwDcTrackerEx() {
        return this.mHwDcTrackerEx;
    }

    public HwCustDcTracker getHwCustDcTracker() {
        return this.mHwCustDcTracker;
    }

    public boolean isDisconnectedOrConnecting() {
        return this.mHwDcTrackerEx.isDisconnectedOrConnecting();
    }

    public static DataProfile createDataProfile(ApnSetting apn, int profileId, boolean isPreferred, Bundle extraData) {
        return IHwDcTrackerEx.createDataProfile(apn, profileId, isPreferred, extraData).getDataProfile();
    }

    private void makeAndAddSnssaiApnSetting(ApnSetting apn) {
        ApnSetting sliceApn;
        if (IS_NR_SLICE_SUPPORTED && apn.canHandleType(17) && (sliceApn = this.mHwDcTrackerEx.createSliceApnSetting(apn)) != null) {
            this.mAllApnSettings.add(sliceApn);
        }
    }

    public void enableApn5GSlice(int apnType, int requestType, int apnType5GSlice, Message onCompleteMsg) {
        if (!IS_NR_SLICE_SUPPORTED) {
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType);
            return;
        }
        log("enableApn5GSlice, apnType5GSlice = " + apnType5GSlice);
        Message msg = obtainMessage(270349, apnType, requestType, onCompleteMsg);
        msg.getData().putInt("snssai", apnType5GSlice);
        sendMessage(msg);
    }

    private void onEnableApn5GSlice(int apnType, int requestType, int apnType5GSlice, Message onCompleteMsg) {
        ApnContextEx apnContextEx = this.mHwDcTrackerEx.getApnContextFor5GSlice(apnType5GSlice);
        ApnContext apnContext = apnContextEx == null ? null : apnContextEx.getApnContext();
        log("onEnableApn5GSlice apnType5GSlice" + apnType5GSlice + " apnContext=" + apnContext + " apnType5GSlice=" + apnType5GSlice);
        onEnableApn(apnType, requestType, apnContext, onCompleteMsg);
    }

    private void onEnableApn(int apnType, int requestType, Message onCompleteMsg) {
        onEnableApn(apnType, requestType, this.mApnContextsByType.get(apnType), onCompleteMsg);
    }

    private void onDisableApn5GSlice(int apnType, int releaseType, int apnType5GSlice) {
        ApnContextEx apnContextEx = this.mHwDcTrackerEx.getApnContextFor5GSlice(apnType5GSlice);
        ApnContext apnContext = apnContextEx == null ? null : apnContextEx.getApnContext();
        log("onDisableApn5GSlice apnType5GSlice" + apnType5GSlice + " apnContext=" + apnContext + " apnType5GSlice=" + apnType5GSlice);
        onDisableApn(apnType, releaseType, apnContext);
    }

    private void sendErrorEvent(IccRecords r, String operator, boolean hasDefaultApn) {
        if (this.mAllApnSettings.isEmpty()) {
            if (!VSimUtilsInner.isVSimSlot(this.mPhone.getPhoneId()) && this.mHwDcTrackerEx.getPrimarySlot() == this.mPhone.getPhoneId() && r != null && r.getRecordsLoaded() && operator.length() != 0) {
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnListEmpty(this.mPhone.getSubId());
            }
        } else if (!hasDefaultApn) {
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentHasNoDefaultApn(this.mPhone.getSubId());
        }
    }

    public void disableApn5GSlice(int apnType, int releaseType, int apnType5GSlice) {
        if (IS_NR_SLICE_SUPPORTED) {
            log("disableApn5GSlice, apnType5GSlice = " + apnType5GSlice);
            Message msg = obtainMessage(270350, apnType, releaseType);
            msg.getData().putInt("snssai", apnType5GSlice);
            sendMessage(msg);
        }
    }

    private void onDisableApn(int apnType, int releaseType) {
        onDisableApn(apnType, releaseType, this.mApnContextsByType.get(apnType));
    }

    public void updateForVSim() {
        this.mHwDcTrackerEx.updateForVSim();
    }

    public DataServiceManager getDataServiceManager() {
        return this.mDataServiceManager;
    }

    private int getRequestCode(int phoneId) {
        return this.mTransportType == 1 ? phoneId * 2 : (phoneId * 2) + 1;
    }
}

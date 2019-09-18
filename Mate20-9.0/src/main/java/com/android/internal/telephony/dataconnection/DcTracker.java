package com.android.internal.telephony.dataconnection;

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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkFactory;
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
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
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
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.google.android.mms.pdu.CharacterSets;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DcTracker extends AbstractDcTrackerBase {
    static final String APN_ID = "apn_id";
    private static final int CAUSE_BY_DATA = 0;
    private static final int CAUSE_BY_ROAM = 1;
    public static final boolean CT_SUPL_FEATURE_ENABLE = SystemProperties.getBoolean("ro.hwpp.ct_supl_feature_enable", false);
    private static final int DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 60000;
    private static int DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = POLL_NETSTAT_SCREEN_OFF_MILLIS;
    private static final String DATA_STALL_ALARM_TAG_EXTRA = "data.stall.alram.tag";
    private static final boolean DATA_STALL_NOT_SUSPECTED = false;
    private static final boolean DATA_STALL_SUSPECTED = true;
    private static final boolean DBG = true;
    private static final String DEBUG_PROV_APN_ALARM = "persist.debug.prov_apn_alarm";
    private static final int HW_SWITCH_SLOT_DONE = 1;
    private static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final String INTENT_DATA_STALL_ALARM = "com.android.internal.telephony.data-stall";
    private static final String INTENT_PROVISIONING_APN_ALARM = "com.android.internal.telephony.provisioning_apn_alarm";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.data-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reconnect_alarm_extra_reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "reconnect_alarm_extra_type";
    private static final int INVALID_STEP = -99;
    public static final boolean IS_DELAY_ATTACH_ENABLED = SystemProperties.getBoolean("ro.config.delay_attach_enabled", false);
    private static String LOG_TAG = "DCT";
    static final Uri MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
    private static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    protected static final int NVCFG_RESULT_FINISHED = 1;
    protected static final int PDP_RESET_ALARM_DELAY_IN_MS = 300000;
    private static final int POLL_NETSTAT_MILLIS = 1000;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 600000;
    private static final int POLL_PDP_MILLIS = 5000;
    static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = Uri.parse("content://telephony/carriers/preferapn_no_update/subId/");
    private static final int PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT = 900000;
    private static final String PROVISIONING_APN_ALARM_TAG_EXTRA = "provisioning.apn.alarm.tag";
    private static final int PROVISIONING_SPINNER_TIMEOUT_MILLIS = 120000;
    private static final String PUPPET_MASTER_RADIO_STRESS_TEST = "gsm.defaultpdpcontext.active";
    private static final boolean RADIO_TESTS = false;
    private static final int RECONNECT_ALARM_DELAY_TIME_FOR_CS_ATTACHED = 5000;
    private static final int RECONNECT_ALARM_DELAY_TIME_SHORT = 50;
    private static final int SUB_1 = 1;
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
    private final ConnectivityManager mCm;
    private HashMap<Integer, DcAsyncChannel> mDataConnectionAcHashMap;
    /* access modifiers changed from: private */
    public final Handler mDataConnectionTracker;
    private HashMap<Integer, DataConnection> mDataConnections;
    private final DataEnabledSettings mDataEnabledSettings;
    private final LocalLog mDataRoamingLeakageLog;
    private final DataServiceManager mDataServiceManager;
    private PendingIntent mDataStallAlarmIntent;
    private int mDataStallAlarmTag;
    private volatile boolean mDataStallDetectionEnabled;
    private TxRxSum mDataStallTxRxSum;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcController mDcc;
    private ArrayList<Message> mDisconnectAllCompleteMsgList;
    private int mDisconnectPendingCount;
    private ApnSetting mEmergencyApn;
    private boolean mEmergencyApnLoaded;
    private volatile boolean mFailFast;
    protected HwCustDcTracker mHwCustDcTracker;
    /* access modifiers changed from: private */
    public final AtomicReference<IccRecords> mIccRecords;
    private boolean mInVoiceCall;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mIsDisposed;
    private boolean mIsProvisioning;
    private boolean mIsPsRestricted;
    /* access modifiers changed from: private */
    public boolean mIsScreenOn;
    private boolean mMvnoMatched;
    /* access modifiers changed from: private */
    public boolean mNetStatPollEnabled;
    /* access modifiers changed from: private */
    public int mNetStatPollPeriod;
    private int mNoRecvPollCount;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    protected final Phone mPhone;
    private final Runnable mPollNetStat;
    private ApnSetting mPreferredApn;
    protected final ArrayList<ApnContext> mPrioritySortedApnContexts;
    private final String mProvisionActionName;
    private BroadcastReceiver mProvisionBroadcastReceiver;
    private PendingIntent mProvisioningApnAlarmIntent;
    private int mProvisioningApnAlarmTag;
    /* access modifiers changed from: private */
    public ProgressDialog mProvisioningSpinner;
    private String mProvisioningUrl;
    private PendingIntent mReconnectIntent;
    private AsyncChannel mReplyAc;
    private String mRequestedApnType;
    private boolean mReregisterOnReconnectFailure;
    /* access modifiers changed from: private */
    public ContentResolver mResolver;
    private long mRxPkts;
    private long mSentSinceLastRecv;
    private final SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public DctConstants.State mState;
    private SubscriptionManager mSubscriptionManager;
    private final int mTransportType;
    private long mTxPkts;
    protected UiccCardApplication mUiccApplcation;
    private final UiccController mUiccController;
    private AtomicInteger mUniqueIdGenerator;
    public int preSetupBasedRadioTech;

    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$6  reason: invalid class name */
    static /* synthetic */ class AnonymousClass6 {
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
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.RETRYING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.IDLE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.SCANNING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.FAILED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(DcTracker.this.mDataConnectionTracker);
        }

        public void onChange(boolean selfChange) {
            DcTracker.this.sendMessage(DcTracker.this.obtainMessage(270355));
        }
    }

    private class ProvisionNotificationBroadcastReceiver extends BroadcastReceiver {
        private final String mNetworkOperator;
        private final String mProvisionUrl;

        public ProvisionNotificationBroadcastReceiver(String provisionUrl, String networkOperator) {
            this.mNetworkOperator = networkOperator;
            this.mProvisionUrl = provisionUrl;
        }

        private void setEnableFailFastMobileData(int enabled) {
            DcTracker.this.sendMessage(DcTracker.this.obtainMessage(270372, enabled, 0));
        }

        private void enableMobileProvisioning() {
            Message msg = DcTracker.this.obtainMessage(270373);
            msg.setData(Bundle.forPair("provisioningUrl", this.mProvisionUrl));
            DcTracker.this.sendMessage(msg);
        }

        public void onReceive(Context context, Intent intent) {
            DcTracker.this.log("onReceive : ProvisionNotificationBroadcastReceiver");
            ProgressDialog unused = DcTracker.this.mProvisioningSpinner = new ProgressDialog(context);
            DcTracker.this.mProvisioningSpinner.setTitle(this.mNetworkOperator);
            DcTracker.this.mProvisioningSpinner.setMessage(context.getText(17040418));
            DcTracker.this.mProvisioningSpinner.setIndeterminate(true);
            DcTracker.this.mProvisioningSpinner.setCancelable(true);
            DcTracker.this.mProvisioningSpinner.getWindow().setType(2009);
            DcTracker.this.mProvisioningSpinner.show();
            DcTracker.this.sendMessageDelayed(DcTracker.this.obtainMessage(270378, DcTracker.this.mProvisioningSpinner), 120000);
            DcTracker.this.setRadio(true);
            setEnableFailFastMobileData(1);
            enableMobileProvisioning();
        }
    }

    private static class RecoveryAction {
        public static final int CLEANUP = 1;
        public static final int GET_DATA_CALL_LIST = 0;
        public static final int RADIO_RESTART = 3;
        public static final int REREGISTER = 2;

        private RecoveryAction() {
        }

        /* access modifiers changed from: private */
        public static boolean isAggressiveRecovery(int value) {
            return value == 1 || value == 2 || value == 3;
        }
    }

    private enum RetryFailures {
        ALWAYS,
        ONLY_ON_CHANGE
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

        public void updateTxRxSum() {
            this.txPkts = TrafficStats.getMobileTcpTxPackets();
            this.rxPkts = TrafficStats.getMobileTcpRxPackets();
        }

        public void updateThisModemMobileTxRxSum(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
            this.txPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileTxPackets(ifacePhoneHashMap, phoneId);
            this.rxPkts = HwTelephonyFactory.getHwDataConnectionManager().getThisModemMobileRxPackets(ifacePhoneHashMap, phoneId);
        }
    }

    /* access modifiers changed from: private */
    public void registerSettingsObserver() {
        Uri contentUri;
        this.mSettingsObserver.unobserve();
        if (TelephonyManager.getDefault().getSimCount() == 1) {
            contentUri = Settings.Global.getUriFor("data_roaming");
        } else {
            contentUri = Settings.Global.getUriFor(getDataRoamingSettingItem("data_roaming"));
        }
        this.mSettingsObserver.observe(contentUri, 270384);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("device_provisioned"), 270379);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("device_provisioning_mobile_data"), 270379);
    }

    /* access modifiers changed from: private */
    public void onActionIntentReconnectAlarm(Intent intent) {
        Message msg = obtainMessage(270383);
        msg.setData(intent.getExtras());
        sendMessage(msg);
    }

    private void onDataReconnect(Bundle bundle) {
        String reason = bundle.getString(INTENT_RECONNECT_ALARM_EXTRA_REASON);
        String apnType = bundle.getString(INTENT_RECONNECT_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = bundle.getInt("subscription", -1);
        log("onDataReconnect: currSubId = " + currSubId + " phoneSubId=" + phoneSubId);
        if (!SubscriptionManager.isValidSubscriptionId(currSubId) || currSubId != phoneSubId) {
            log("receive ReconnectAlarm but subId incorrect, ignore");
            return;
        }
        ApnContext apnContext = this.mApnContexts.get(apnType);
        log("onDataReconnect: mState=" + this.mState + " reason=" + reason + " apnType=" + apnType + " apnContext=" + apnContext + " mDataConnectionAsyncChannels=" + this.mDataConnectionAcHashMap);
        if (apnContext != null && apnContext.isEnabled()) {
            apnContext.setReason(reason);
            DctConstants.State apnContextState = apnContext.getState();
            log("onDataReconnect: apnContext state=" + apnContextState);
            if (apnContextState == DctConstants.State.FAILED || apnContextState == DctConstants.State.IDLE) {
                log("onDataReconnect: state is FAILED|IDLE, disassociate");
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac != null) {
                    log("onDataReconnect: tearDown apnContext=" + apnContext);
                    dcac.tearDown(apnContext, "", null);
                }
                apnContext.setDataConnectionAc(null);
                apnContext.setState(DctConstants.State.IDLE);
            } else {
                log("onDataReconnect: keep associated");
            }
            sendMessage(obtainMessage(270339, apnContext));
            apnContext.setReconnectIntent(null);
        }
    }

    /* access modifiers changed from: private */
    public void onActionIntentDataStallAlarm(Intent intent) {
        Message msg = obtainMessage(270353, intent.getAction());
        msg.arg1 = intent.getIntExtra(DATA_STALL_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    public DcTracker(Phone phone, int transportType) {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = "default";
        this.mPrioritySortedApnContexts = new ArrayList<>();
        this.mAllApnSettings = new ArrayList<>();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mDataRoamingLeakageLog = new LocalLog(50);
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        DcTracker.this.log("screen on");
                        boolean unused = DcTracker.this.mIsScreenOn = true;
                        DcTracker.this.stopNetStatPoll();
                        DcTracker.this.startNetStatPoll();
                        DcTracker.this.restartDataStallAlarm();
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        DcTracker.this.log("screen off");
                        boolean unused2 = DcTracker.this.mIsScreenOn = false;
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
                        if (DcTracker.this.mIccRecords.get() != null && ((IccRecords) DcTracker.this.mIccRecords.get()).getRecordsLoaded() && !((IccRecords) DcTracker.this.mIccRecords.get()).isHwCustDataRoamingOpenArea()) {
                            DcTracker.this.setDefaultDataRoamingEnabled();
                        }
                    } else if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(action)) {
                        DcTracker.this.log("Received SWITCH_SLOT_DONE");
                        String operator = DcTracker.this.getOperatorNumeric();
                        int switchSlotStep = intent.getIntExtra(DcTracker.HW_SWITCH_SLOT_STEP, -99);
                        if (!TextUtils.isEmpty(operator) && 1 == switchSlotStep) {
                            DcTracker.this.onRecordsLoadedOrSubIdChanged();
                        }
                    } else if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                        DcTracker.this.log("com.huawei.devicepolicy.action.POLICY_CHANGED");
                        String action_tag = intent.getStringExtra("action_tag");
                        if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_data_4G") && DcTracker.this.mPhone != null && DcTracker.this.mPhone.getSubId() == 1) {
                            if (intent.getBooleanExtra("dataState", false)) {
                                DcTracker.this.cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
                            } else {
                                DcTracker.this.onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
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
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    int unused = DcTracker.this.mNetStatPollPeriod = Settings.Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    int unused2 = DcTracker.this.mNetStatPollPeriod = Settings.Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_long_poll_interval_ms", DcTracker.POLL_NETSTAT_SCREEN_OFF_MILLIS);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
                if (SubscriptionManager.isValidSubscriptionId(DcTracker.this.mPhone.getSubId())) {
                    DcTracker.this.registerSettingsObserver();
                }
            }
        };
        this.mDisconnectAllCompleteMsgList = new ArrayList<>();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference<>();
        this.mActivity = DctConstants.Activity.NONE;
        this.mState = DctConstants.State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = true;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap<>();
        this.mDataConnectionAcHashMap = new HashMap<>();
        this.mApnToDataConnectionId = new HashMap<>();
        this.mApnContexts = new ConcurrentHashMap<>();
        this.mApnContextsById = new SparseArray<>();
        this.mDisconnectPendingCount = 0;
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.preSetupBasedRadioTech = -1;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mUiccApplcation = null;
        this.mEmergencyApnLoaded = false;
        this.mPhone = phone;
        if (this.mHwCustDcTracker == null) {
            this.mHwCustDcTracker = (HwCustDcTracker) HwCustUtils.createObj(HwCustDcTracker.class, new Object[]{this});
        }
        if (phone.getPhoneType() == 1) {
            LOG_TAG = "GsmDCT";
        } else if (phone.getPhoneType() == 2) {
            LOG_TAG = "CdmaDCT";
        } else {
            LOG_TAG = "DCT";
            loge("unexpected phone type [" + phone.getPhoneType() + "]");
        }
        log(LOG_TAG + ".constructor");
        this.mTransportType = transportType;
        this.mDataServiceManager = new DataServiceManager(phone, transportType);
        this.mResolver = this.mPhone.getContext().getContentResolver();
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 270369, null);
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            VSimUtilsInner.registerForIccChanged(this, 270369, null);
        }
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mCm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(INTENT_DATA_STALL_ALARM);
        filter.addAction(INTENT_PROVISIONING_APN_ALARM);
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        this.mDataEnabledSettings = new DataEnabledSettings(phone);
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mAutoAttachOnCreation.set(PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getBoolean(Phone.DATA_DISABLED_ON_BOOT_KEY, false));
        registerPhoneStateListener(this.mPhone.getContext());
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        HandlerThread dcHandlerThread = new HandlerThread("DcHandlerThread");
        dcHandlerThread.start();
        Handler dcHandler = new Handler(dcHandlerThread.getLooper());
        this.mDcc = DcController.makeDcc(this.mPhone, this, this.mDataServiceManager, dcHandler);
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
        super.init();
        if (isClearCodeEnabled()) {
            startListenCellLocationChange();
        }
        registerForFdn();
        sendMessage(obtainMessage(271137));
    }

    @VisibleForTesting
    public DcTracker() {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = "default";
        this.mPrioritySortedApnContexts = new ArrayList<>();
        this.mAllApnSettings = new ArrayList<>();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mDataRoamingLeakageLog = new LocalLog(50);
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        DcTracker.this.log("screen on");
                        boolean unused = DcTracker.this.mIsScreenOn = true;
                        DcTracker.this.stopNetStatPoll();
                        DcTracker.this.startNetStatPoll();
                        DcTracker.this.restartDataStallAlarm();
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        DcTracker.this.log("screen off");
                        boolean unused2 = DcTracker.this.mIsScreenOn = false;
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
                        if (DcTracker.this.mIccRecords.get() != null && ((IccRecords) DcTracker.this.mIccRecords.get()).getRecordsLoaded() && !((IccRecords) DcTracker.this.mIccRecords.get()).isHwCustDataRoamingOpenArea()) {
                            DcTracker.this.setDefaultDataRoamingEnabled();
                        }
                    } else if ("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE".equals(action)) {
                        DcTracker.this.log("Received SWITCH_SLOT_DONE");
                        String operator = DcTracker.this.getOperatorNumeric();
                        int switchSlotStep = intent.getIntExtra(DcTracker.HW_SWITCH_SLOT_STEP, -99);
                        if (!TextUtils.isEmpty(operator) && 1 == switchSlotStep) {
                            DcTracker.this.onRecordsLoadedOrSubIdChanged();
                        }
                    } else if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                        DcTracker.this.log("com.huawei.devicepolicy.action.POLICY_CHANGED");
                        String action_tag = intent.getStringExtra("action_tag");
                        if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_data_4G") && DcTracker.this.mPhone != null && DcTracker.this.mPhone.getSubId() == 1) {
                            if (intent.getBooleanExtra("dataState", false)) {
                                DcTracker.this.cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
                            } else {
                                DcTracker.this.onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
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
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    int unused = DcTracker.this.mNetStatPollPeriod = Settings.Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    int unused2 = DcTracker.this.mNetStatPollPeriod = Settings.Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_long_poll_interval_ms", DcTracker.POLL_NETSTAT_SCREEN_OFF_MILLIS);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
                if (SubscriptionManager.isValidSubscriptionId(DcTracker.this.mPhone.getSubId())) {
                    DcTracker.this.registerSettingsObserver();
                }
            }
        };
        this.mDisconnectAllCompleteMsgList = new ArrayList<>();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference<>();
        this.mActivity = DctConstants.Activity.NONE;
        this.mState = DctConstants.State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = true;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap<>();
        this.mDataConnectionAcHashMap = new HashMap<>();
        this.mApnToDataConnectionId = new HashMap<>();
        this.mApnContexts = new ConcurrentHashMap<>();
        this.mApnContextsById = new SparseArray<>();
        this.mDisconnectPendingCount = 0;
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.preSetupBasedRadioTech = -1;
        this.isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
        this.mUiccApplcation = null;
        this.mEmergencyApnLoaded = false;
        this.mAlarmManager = null;
        this.mCm = null;
        this.mPhone = null;
        this.mUiccController = null;
        this.mDataConnectionTracker = null;
        this.mProvisionActionName = null;
        this.mSettingsObserver = new SettingsObserver(null, this);
        this.mDataEnabledSettings = null;
        this.mTransportType = 0;
        this.mDataServiceManager = null;
    }

    public void registerServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this, 270352, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this, 270345, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOn(this, 270347, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 270348, null, true);
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
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(0, this, 270382, null, false);
        this.mDataServiceManager.registerForServiceBindingChanged(this, 270385, null);
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
        cleanUpAllConnections(true, (String) null);
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            dcac.disconnect();
        }
        this.mDataConnectionAcHashMap.clear();
        this.mIsDisposed = true;
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
        disposeCustDct();
        super.dispose();
    }

    private void unregisterForAllEvents() {
        if (this.mTransportType == 1) {
            this.mPhone.mCi.unregisterForAvailable(this);
            this.mPhone.mCi.unregisterForOffOrNotAvailable(this);
            this.mPhone.mCi.unregisterForPcoData(this);
        }
        if (this.mUiccApplcation != null) {
            unregisterForGetAdDone(this.mUiccApplcation);
            this.mUiccApplcation = null;
        }
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            unregisterForRecordsLoaded(r);
            unregisterForImsiReady(r);
            unregisterForFdnRecordsLoaded(r);
            this.mIccRecords.set(null);
        }
        if (this.mPhone.getCallTracker() != null) {
            this.mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
            this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        }
        unregisterServiceStateTrackerEvents();
        this.mPhone.mCi.unregisterForPcoData(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 0);
        if (mWcdmaVpEnabled) {
            this.mPhone.mCi.unregisterForReportVpStatus(this);
        }
        this.mDataServiceManager.unregisterForServiceBindingChanged(this);
        this.mPhone.mCi.unregisterForUnsolNvCfgFinished(this);
    }

    public void setUserDataEnabled(boolean enable) {
        log("DcTrackerBase setDataEnabled=" + enable);
        String appName = getAppName(Binder.getCallingPid());
        log("Get the caller pid and appName. pid is " + pid + ", appName is " + appName);
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString() + "\n");
        }
        log(sb.toString());
        Message msg = obtainMessage(270366);
        msg.arg1 = enable;
        log("setDataEnabled: sendMessage: enable=" + enable);
        sendMessage(msg);
    }

    private void onSetUserDataEnabled(boolean enabled) {
        if (this.mDataEnabledSettings.isUserDataEnabled() != enabled) {
            this.mDataEnabledSettings.setUserDataEnabled(enabled);
            if (enabled) {
                this.mHwCustDcTracker.setDataOrRoamOn(0);
            }
            if (!getDataRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
                if (enabled) {
                    notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
                } else {
                    notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_DISABLED);
                }
            }
            if (enabled) {
                ApnContext apnContext = this.mApnContexts.get("default");
                if (apnContext != null && !apnContext.isEnabled() && isDataNeededWithWifiAndBt()) {
                    log("onSetUserDataEnabled default apn is disabled and isDataNeededWithWifiAndBt is true, so we need try to restore apncontext");
                    apnContext.setEnabled(true);
                    apnContext.setDependencyMet(true);
                }
            }
            this.mPhone.notifyUserMobileDataStateChanged(enabled);
            if (enabled) {
                reevaluateDataConnections();
                onTrySetupData(AbstractPhoneInternalInterface.REASON_USER_DATA_ENABLED);
                return;
            }
            onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
        }
    }

    private void reevaluateDataConnections() {
        if (this.mDataEnabledSettings.isDataEnabled()) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.isConnectedOrConnecting()) {
                    DcAsyncChannel dcac = apnContext.getDcAc();
                    if (dcac != null) {
                        NetworkCapabilities netCaps = dcac.getNetworkCapabilitiesSync();
                        if (netCaps != null && !netCaps.hasCapability(13) && !netCaps.hasCapability(11)) {
                            log("Tearing down restricted metered net:" + apnContext);
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            cleanUpConnection(true, apnContext);
                        } else if (apnContext.getApnSetting().isMetered(this.mPhone) && netCaps != null && netCaps.hasCapability(11)) {
                            log("Tearing down unmetered net:" + apnContext);
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            cleanUpConnection(true, apnContext);
                        }
                    }
                }
            }
        }
    }

    private void onDeviceProvisionedChange() {
        if (isDataEnabled()) {
            reevaluateDataConnections();
            onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            return;
        }
        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
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

    public void requestNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.requestNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.requestNetwork(networkRequest, log);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.releaseNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.releaseNetwork(networkRequest, log);
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
    public void setRadio(boolean on) {
        try {
            ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).setRadio(on);
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        if (this.mPhone != null) {
            log("finalize");
        }
    }

    private ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new ApnContext(this.mPhone, type, LOG_TAG, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        this.mApnContextsById.put(ApnContext.apnIdForApnName(type), apnContext);
        this.mPrioritySortedApnContexts.add(0, apnContext);
        return apnContext;
    }

    private void initApnContexts() {
        ApnContext apnContext;
        log("initApnContexts: E");
        for (String networkConfigString : this.mPhone.getContext().getResources().getStringArray(17236063)) {
            NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
            if (!VSimUtilsInner.isVSimFiltrateApn(this.mPhone.getSubId(), networkConfig.type)) {
                if (isApnTypeDisabled(networkTypeToApnType(networkConfig.type))) {
                    log("apn type " + apnType + " disabled!");
                } else {
                    int i = networkConfig.type;
                    if (i == 0) {
                        apnContext = addApnContext("default", networkConfig);
                    } else if (i != 48) {
                        switch (i) {
                            case 2:
                                apnContext = addApnContext("mms", networkConfig);
                                break;
                            case 3:
                                apnContext = addApnContext("supl", networkConfig);
                                break;
                            case 4:
                                apnContext = addApnContext("dun", networkConfig);
                                break;
                            case 5:
                                apnContext = addApnContext("hipri", networkConfig);
                                break;
                            default:
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
                                            case 14:
                                                apnContext = addApnContext("ia", networkConfig);
                                                break;
                                            case 15:
                                                apnContext = addApnContext("emergency", networkConfig);
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
                                                        log("initApnContexts: skipping unknown type=" + networkConfig.type);
                                                        continue;
                                                        continue;
                                                        continue;
                                                        continue;
                                                }
                                        }
                                }
                        }
                    } else {
                        apnContext = addApnContext("internaldefault", networkConfig);
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
        ApnContext apnContext = this.mApnContexts.get(apnType);
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
        ApnContext apnContext = this.mApnContexts.get(apnType);
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
        ArrayList<String> result = new ArrayList<>();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    public String getActiveApnString(String apnType) {
        log("get active apn string for type:" + apnType);
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext != null) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null) {
                return apnSetting.apn;
            }
        }
        return null;
    }

    public DctConstants.State getState(String apnType) {
        for (DataConnection dc : this.mDataConnections.values()) {
            ApnSetting apnSetting = dc.getApnSetting();
            if (apnSetting != null && apnSetting.canHandleType(apnType)) {
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
                switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[apnContext.getState().ordinal()]) {
                    case 1:
                    case 2:
                        log("overall state is CONNECTED");
                        return DctConstants.State.CONNECTED;
                    case 3:
                    case 4:
                        isConnecting = true;
                        isFailed = false;
                        break;
                    case 5:
                    case 6:
                        isFailed = false;
                        break;
                    default:
                        isAnyEnabled = true;
                        break;
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

    @VisibleForTesting
    public boolean isDataEnabled() {
        return this.mDataEnabledSettings.isDataEnabled();
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();
        notifyDataConnection(PhoneInternalInterface.REASON_DATA_DETACHED);
        this.mAttached.set(false);
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(false);
        if (getOverallState() == DctConstants.State.CONNECTED) {
            startPdpResetAlarm(PDP_RESET_ALARM_DELAY_IN_MS);
        }
    }

    private void onDataConnectionAttached() {
        log("onDataConnectionAttached");
        this.mAttached.set(true);
        stopPdpResetAlarm();
        this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(false);
        if (getOverallState() == DctConstants.State.CONNECTED) {
            log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(false);
            notifyDataConnection(PhoneInternalInterface.REASON_DATA_ATTACHED);
        } else {
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_ATTACHED);
        }
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachOnCreation.set(true);
        }
        if (!(!isCTSimCard(this.mPhone.getPhoneId()) || this.preSetupBasedRadioTech == 0 || this.preSetupBasedRadioTech == this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            log("onDataConnectionAttached need to clear ApnContext, preSetupBasedRadioTech: " + this.preSetupBasedRadioTech);
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == DctConstants.State.SCANNING) {
                    apnContext.setState(DctConstants.State.IDLE);
                    cancelReconnectAlarm(apnContext);
                }
            }
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ATTACHED);
    }

    public boolean isDataAllowed(DataConnectionReasons dataConnectionReasons) {
        return isDataAllowed(null, dataConnectionReasons);
    }

    /* access modifiers changed from: package-private */
    public boolean isDataAllowed(ApnContext apnContext, DataConnectionReasons dataConnectionReasons) {
        boolean isMeteredApnType;
        ApnContext apnContext2 = apnContext;
        DataConnectionReasons dataConnectionReasons2 = dataConnectionReasons;
        DataConnectionReasons reasons = new DataConnectionReasons();
        boolean internalDataEnabled = this.mDataEnabledSettings.isInternalDataEnabled();
        boolean attachedState = getAttachedStatus(this.mAttached.get());
        boolean desiredPowerState = this.mPhone.getServiceStateTracker().getDesiredPowerState();
        boolean radioStateFromCarrier = this.mPhone.getServiceStateTracker().getPowerStateFromCarrier();
        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        if (radioTech == 18) {
            desiredPowerState = true;
            radioStateFromCarrier = true;
        }
        boolean recordsLoaded = this.mIccRecords.get() != null && (this.mIccRecords.get().getRecordsLoaded() || this.mIccRecords.get().getImsiReady());
        if (this.mIccRecords.get() != null && !recordsLoaded) {
            log("isDataAllowed getImsiReady=" + this.mIccRecords.get().getImsiReady());
        }
        boolean isDataAllowedVoWiFi = !HuaweiTelephonyConfigs.isHisiPlatform() && radioTech == 18;
        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        boolean defaultDataSelected = SubscriptionManager.isValidSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (VSimUtilsInner.isVSimEnabled()) {
            defaultDataSelected = true;
        }
        boolean isMeteredApnType2 = apnContext2 == null || ApnSetting.isMeteredApnType(apnContext.getApnType(), this.mPhone);
        PhoneConstants.State phoneState = PhoneConstants.State.IDLE;
        if (this.mPhone.getCallTracker() != null) {
            phoneState = this.mPhone.getCallTracker().getState();
        }
        if (apnContext2 != null) {
            isMeteredApnType = isMeteredApnType2;
            if (apnContext.getApnType().equals("emergency") && apnContext.isConnectable()) {
                if (dataConnectionReasons2 != null) {
                    dataConnectionReasons2.add(DataConnectionReasons.DataAllowedReasonType.EMERGENCY_APN);
                }
                return true;
            }
        } else {
            isMeteredApnType = isMeteredApnType2;
        }
        if (apnContext2 != null && !apnContext.isConnectable()) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.APN_NOT_CONNECTABLE);
        }
        if (apnContext2 != null && ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && radioTech == 18)) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.ON_IWLAN);
            apnContext2.setPdpFailCause(DcFailCause.NOT_ALLOWED_RADIO_TECHNOLOGY_IWLAN);
        }
        if (isEmergency()) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.IN_ECBM);
        }
        if (!attachedState && ((!this.mAutoAttachOnCreation.get() || this.mPhone.getSubId() != dataSub) && !isNeedForceSetup(apnContext))) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.NOT_ATTACHED);
        }
        if (!recordsLoaded && !isNeedForceSetup(apnContext) && !isDataAllowedVoWiFi) {
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
        if (!isDataAllowedForRoaming(isMmsApn(apnContext)) && ((!isXcapApn(apnContext) || !getXcapDataRoamingEnable()) && this.mPhone.getServiceState().getDataRoaming() && !getDataRoamingEnabled())) {
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
        if (!this.mDataEnabledSettings.isDataEnabled()) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED);
        }
        if (!isPsAllowedByFdn()) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.PS_RESTRICTED_BY_FDN);
        }
        if (dataSub == 1 && isDataConnectivityDisabled(1, "disable-data")) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
            cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
        }
        if (this.mPhone.getSubId() == 1 && isDataDisableBySim2()) {
            log("isDataAllowed sim2 data disable by cust");
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
        }
        if (isDataDisable(this.mPhone.getSubId())) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
        }
        if (HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave() && get4gSlot() == this.mPhone.getSubId()) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
        }
        if (reasons.containsHardDisallowedReasons()) {
            if (dataConnectionReasons2 != null) {
                dataConnectionReasons2.copyFrom(reasons);
            }
            return false;
        }
        if (!isMeteredApnType && !reasons.allowed()) {
            reasons.add(DataConnectionReasons.DataAllowedReasonType.UNMETERED_APN);
        }
        if (apnContext2 != null) {
            if (!apnContext2.hasNoRestrictedRequests(true) && reasons.contains(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED)) {
                reasons.add(DataConnectionReasons.DataAllowedReasonType.RESTRICTED_REQUEST);
            }
        }
        if (apnContext2 != null && this.mHwCustDcTracker != null && this.mHwCustDcTracker.isDataAllowedForSES(apnContext.getApnType()) && reasons.contains(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED)) {
            reasons.add(DataConnectionReasons.DataAllowedReasonType.RESTRICTED_REQUEST);
        }
        if (apnContext2 != null && !reasons.allowed()) {
            if (getAnyDataEnabledByApnContext(apnContext2, false)) {
                reasons.add(DataConnectionReasons.DataAllowedReasonType.NORMAL);
            }
        }
        if ((this.mHwCustDcTracker != null && apnContext2 != null && reasons.allowed() && this.mHwCustDcTracker.isRoamDisallowedByCustomization(apnContext2)) || (apnContext2 != null && "mms".equals(apnContext.getApnType()) && this.mPhone.getServiceState().getDataRoaming() && isRoamingPushDisabled())) {
            reasons.add(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
        }
        if (reasons.allowed()) {
            reasons.add(DataConnectionReasons.DataAllowedReasonType.NORMAL);
        }
        if (dataConnectionReasons2 != null) {
            dataConnectionReasons2.copyFrom(reasons);
        }
        boolean dataAllowedByApnContext = true;
        if (apnContext2 != null) {
            dataAllowedByApnContext = isDataAllowedByApnContext(apnContext);
        }
        return reasons.allowed() && dataAllowedByApnContext;
    }

    private void setupDataOnConnectableApns(String reason) {
        setupDataOnConnectableApns(reason, RetryFailures.ALWAYS);
    }

    private void setupDataOnConnectableApns(String reason, RetryFailures retryFailures) {
        log("setupDataOnConnectableApns: " + reason);
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
        log("setupDataOnConnectableApns: " + reason + " " + sb);
        if (!getmIsPseudoImsi() || reason.equals(AbstractPhoneInternalInterface.REASON_SET_PS_ONLY_OK)) {
            Iterator<ApnContext> it2 = this.mPrioritySortedApnContexts.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                ApnContext apnContext2 = it2.next();
                if (apnContext2.getState() == DctConstants.State.FAILED || apnContext2.getState() == DctConstants.State.SCANNING) {
                    if (retryFailures == RetryFailures.ALWAYS) {
                        apnContext2.releaseDataConnection(reason);
                    } else if (!apnContext2.isConcurrentVoiceAndDataAllowed() && this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                        apnContext2.releaseDataConnection(reason);
                    }
                }
                if (isDefaultDataSubscription() && !apnContext2.isEnabled() && PhoneInternalInterface.REASON_SIM_LOADED.equals(reason) && "default".equals(apnContext2.getApnType()) && isDataNeededWithWifiAndBt()) {
                    log("setupDataOnConnectableApns: for IMSI done, call setEnabled");
                    apnContext2.setEnabled(true);
                }
                if (apnContext2.isConnectable()) {
                    log("isConnectable() call trySetupData");
                    if (!getmIsPseudoImsi() || apnContext2.getApnType().equals("bip0")) {
                        this.preSetupBasedRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                        log("setupDataOnConnectableApns: current radio technology: " + this.preSetupBasedRadioTech);
                        apnContext2.setReason(reason);
                        trySetupData(apnContext2);
                        if (getmIsPseudoImsi()) {
                            log("setupDataOnConnectableApns: pseudo imsi single connection only");
                            break;
                        }
                        HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(true);
                    }
                } else {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, isWifiConnected(), this.mDataEnabledSettings.isDataEnabled(), apnContext2);
                }
            }
            return;
        }
        log("getmIsPseudoImsi(): " + getmIsPseudoImsi() + "  reason: " + reason);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmergency() {
        boolean result = this.mPhone.isInEcm() || this.mPhone.isInEmergencyCall();
        log("isEmergency: result=" + result);
        return result;
    }

    private boolean trySetupData(ApnContext apnContext) {
        int dataState;
        HwDataConnectionManager sHwDataConnectionManager = HwTelephonyFactory.getHwDataConnectionManager();
        if (sHwDataConnectionManager != null && sHwDataConnectionManager.getNamSwitcherForSoftbank() && sHwDataConnectionManager.isSoftBankCard(this.mPhone) && !sHwDataConnectionManager.isValidMsisdn(this.mPhone)) {
            log("trySetupData sbnam not allow activate data if MSISDN of softbank card is empty  !");
            return false;
        } else if (isDataConnectivityDisabled(this.mPhone.getSubId(), "disable-data")) {
            return false;
        } else {
            int voiceState = this.mPhone.getServiceState().getVoiceRegState();
            log("dataState = " + dataState + "voiceState = " + voiceState + "OperatorNumeric = " + getOperatorNumeric());
            if ("default".equals(apnContext.getApnType()) && (dataState == 0 || voiceState == 0)) {
                this.mPreferredApn = getApnForCT();
                log("get prefered dp for CT " + this.mPreferredApn);
                if (this.mPreferredApn == null) {
                    this.mPreferredApn = getPreferredApn();
                }
                log("get prefered DP " + this.mPreferredApn);
            }
            if (!VSimUtilsInner.isVSimEnabled() || VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) || "mms".equals(apnContext.getApnType())) {
                boolean isQcomDualLteImsApn = PhoneFactory.IS_QCOM_DUAL_LTE_STACK && PhoneFactory.IS_DUAL_VOLTE_SUPPORTED && "ims".equals(apnContext.getApnType());
                if (!isDefaultDataSubscription() && !"mms".equals(apnContext.getApnType()) && !"xcap".equals(apnContext.getApnType()) && !isQcomDualLteImsApn && !NetworkFactory.isDualCellDataEnable()) {
                    log("trySetupData not allowed on non defaultDds except mms or xcap or qcomDualLte ims is enabled");
                    return false;
                } else if (this.mPhone.getSimulatedRadioControl() != null) {
                    apnContext.setState(DctConstants.State.CONNECTED);
                    this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
                    log("trySetupData: X We're on the simulator; assuming connected retValue=true");
                    return true;
                } else {
                    DataConnectionReasons dataConnectionReasons = new DataConnectionReasons();
                    boolean isDataAllowed = isDataAllowed(apnContext, dataConnectionReasons);
                    String logStr = "trySetupData for APN type " + apnContext.getApnType() + ", reason: " + apnContext.getReason() + ". " + dataConnectionReasons.toString();
                    log(logStr);
                    apnContext.requestLog(logStr);
                    if (getmIsPseudoImsi() || isDataAllowed) {
                        if (apnContext.getState() == DctConstants.State.FAILED) {
                            log("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                            apnContext.requestLog("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                            apnContext.setState(DctConstants.State.IDLE);
                        }
                        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                        apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                        if (apnContext.getState() == DctConstants.State.IDLE) {
                            ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                            if (waitingApns.isEmpty()) {
                                notifyNoData(DcFailCause.MISSING_UNKNOWN_APN, apnContext);
                                notifyOffApnsOfAvailability(apnContext.getReason());
                                log("trySetupData: X No APN found retValue=false");
                                apnContext.requestLog("trySetupData: X No APN found retValue=false");
                                return false;
                            }
                            apnContext.setWaitingApns(waitingApns);
                            log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
                        }
                        boolean unmeteredUseOnly = dataConnectionReasons.contains(DataConnectionReasons.DataAllowedReasonType.UNMETERED_APN);
                        if ("default".equals(SystemProperties.get("gsm.bip.apn")) && isBipApnType(apnContext.getApnType())) {
                            unmeteredUseOnly = false;
                        }
                        boolean retValue = setupData(apnContext, radioTech, unmeteredUseOnly);
                        notifyOffApnsOfAvailability(apnContext.getReason());
                        sendOTAAttachTimeoutMsg(apnContext, retValue);
                        log("trySetupData: X retValue=" + retValue);
                        return retValue;
                    }
                    if (!apnContext.getApnType().equals("default") && apnContext.isConnectable()) {
                        this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
                    }
                    notifyOffApnsOfAvailability(apnContext.getReason());
                    HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnContextDisabledWhenWifiDisconnected(this.mPhone, isWifiConnected(), this.mDataEnabledSettings.isDataEnabled(), apnContext);
                    StringBuilder str = new StringBuilder();
                    str.append("trySetupData failed. apnContext = [type=" + apnContext.getApnType() + ", mState=" + apnContext.getState() + ", apnEnabled=" + apnContext.isEnabled() + ", mDependencyMet=" + apnContext.getDependencyMet() + "] ");
                    if (!this.mDataEnabledSettings.isDataEnabled()) {
                        str.append("isDataEnabled() = false. " + this.mDataEnabledSettings);
                    }
                    if (apnContext.getState() == DctConstants.State.SCANNING) {
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

    /* access modifiers changed from: protected */
    public void notifyOffApnsOfAvailability(String reason) {
        String str;
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                log("notifyOffApnsOfAvailability skipped apn due to attached && isReady " + apnContext.toString());
            } else if (apnContext.getApnType() == null || !apnContext.getApnType().startsWith("bip")) {
                log("notifyOffApnOfAvailability type:" + apnContext.getApnType());
                Phone phone = this.mPhone;
                if (reason != null) {
                    str = reason;
                } else {
                    str = apnContext.getReason();
                }
                phone.notifyDataConnection(str, apnContext.getApnType(), PhoneConstants.DataState.DISCONNECTED);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean cleanUpAllConnections(boolean tearDown, String reason) {
        log("cleanUpAllConnections: tearDown=" + tearDown + " reason=" + reason);
        boolean didDisconnect = false;
        boolean disableMeteredOnly = false;
        if (!TextUtils.isEmpty(reason)) {
            disableMeteredOnly = reason.equals(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED) || reason.equals(PhoneInternalInterface.REASON_ROAMING_ON) || reason.equals(PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN) || reason.equals(PhoneInternalInterface.REASON_PDP_RESET);
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[apnContext.getState().ordinal()]) {
                case 5:
                case 6:
                case 7:
                    break;
                default:
                    didDisconnect = true;
                    if (!disableMeteredOnly) {
                        apnContext.setReason(reason);
                        cleanUpConnection(tearDown, apnContext);
                        break;
                    } else {
                        ApnSetting apnSetting = apnContext.getApnSetting();
                        if (apnSetting != null && apnSetting.isMetered(this.mPhone) && !apnContext.getApnType().equals("xcap")) {
                            log("clean up metered ApnContext Type: " + apnContext.getApnType());
                            apnContext.setReason(reason);
                            cleanUpConnection(tearDown, apnContext);
                            break;
                        }
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
        cleanUpAllConnections(true, cause);
    }

    /* access modifiers changed from: package-private */
    public void sendCleanUpConnection(boolean tearDown, ApnContext apnContext) {
        log("sendCleanUpConnection: tearDown=" + tearDown + " apnContext=" + apnContext);
        Message msg = obtainMessage(270360);
        msg.arg1 = tearDown;
        msg.arg2 = 0;
        msg.obj = apnContext;
        sendMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void cleanUpConnection(boolean tearDown, ApnContext apnContext) {
        String str;
        if (apnContext == null) {
            log("cleanUpConnection: apn context is null");
            return;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        log(str + " apnContext=" + apnContext);
        apnContext.requestLog("cleanUpConnection: tearDown=" + tearDown + " reason=" + apnContext.getReason());
        if (tearDown) {
            if (apnContext.isDisconnected()) {
                apnContext.setState(DctConstants.State.IDLE);
                if (!apnContext.isReady()) {
                    if (dcac != null) {
                        log("cleanUpConnection: teardown, disconnected, !ready" + " apnContext=" + apnContext);
                        apnContext.requestLog("cleanUpConnection: teardown, disconnected, !ready");
                        dcac.tearDown(apnContext, "", null);
                    }
                    apnContext.setDataConnectionAc(null);
                }
            } else if (dcac == null) {
                apnContext.setState(DctConstants.State.IDLE);
                apnContext.requestLog("cleanUpConnection: connected, bug no DCAC");
                this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            } else if (apnContext.getState() != DctConstants.State.DISCONNECTING) {
                boolean disconnectAll = false;
                if ("dun".equals(apnContext.getApnType()) && teardownForDun()) {
                    log("cleanUpConnection: disconnectAll DUN connection");
                    disconnectAll = true;
                }
                int generation = apnContext.getConnectionGeneration();
                StringBuilder sb = new StringBuilder();
                sb.append("cleanUpConnection: tearing down");
                sb.append(disconnectAll ? " all" : "");
                sb.append(" using gen#");
                sb.append(generation);
                log(str + "apnContext=" + apnContext);
                apnContext.requestLog(str);
                Message msg = obtainMessage(270351, new Pair<>(apnContext, Integer.valueOf(generation)));
                if (disconnectAll) {
                    apnContext.getDcAc().tearDownAll(apnContext.getReason(), msg);
                } else {
                    apnContext.getDcAc().tearDown(apnContext, apnContext.getReason(), msg);
                }
                apnContext.setState(DctConstants.State.DISCONNECTING);
                this.mDisconnectPendingCount++;
            }
        } else if (!PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(apnContext.getReason()) || apnContext.getState() != DctConstants.State.CONNECTING) {
            if (dcac != null) {
                dcac.reqReset();
            }
            apnContext.setState(DctConstants.State.IDLE);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            apnContext.setDataConnectionAc(null);
        } else {
            log("ignore the set IDLE message, because the current state is connecting!");
        }
        setupDataForSinglePdnArbitration(apnContext.getReason());
        if (dcac != null) {
            cancelReconnectAlarm(apnContext);
        }
        log(str + " apnContext=" + apnContext + " dcac=" + apnContext.getDcAc());
        apnContext.requestLog("cleanUpConnection: X tearDown=" + tearDown + " reason=" + apnContext.getReason());
    }

    @VisibleForTesting
    public ArrayList<ApnSetting> fetchDunApns() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            log("fetchDunApns: net.tethering.noprovisioning=true ret: empty list");
            return new ArrayList<>(0);
        }
        int bearer = this.mPhone.getServiceState().getRilDataRadioTechnology();
        IccRecords r = this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : "";
        ArrayList<ApnSetting> dunCandidates = new ArrayList<>();
        ArrayList<ApnSetting> retDunSettings = new ArrayList<>();
        ApnSetting preferredApn = getPreferredApn();
        if (this.mHwCustDcTracker == null || !this.mHwCustDcTracker.isDocomoApn(preferredApn)) {
            String apnData = Settings.Global.getString(this.mResolver, "tether_dun_apn");
            if (!TextUtils.isEmpty(apnData)) {
                dunCandidates.addAll(ApnSetting.arrayFromString(apnData));
                log("fetchDunApns: dunCandidates from Setting: " + dunCandidates);
            }
            if (preferredApn != null) {
                String[] strArr = preferredApn.types;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (strArr[i].contains("dun")) {
                        dunCandidates.add(preferredApn);
                        log("fetchDunApn: add preferredApn");
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (dunCandidates.isEmpty() && !ArrayUtils.isEmpty(this.mAllApnSettings)) {
                Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
                while (it.hasNext()) {
                    ApnSetting apn = it.next();
                    for (String contains : apn.types) {
                        if (contains.contains("dun")) {
                            dunCandidates.add(apn);
                        }
                    }
                }
                log("fetchDunApns: dunCandidates from database: " + dunCandidates);
            }
            Iterator<ApnSetting> it2 = dunCandidates.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                ApnSetting dunSetting = it2.next();
                if (ServiceState.bitmaskHasTech(dunSetting.networkTypeBitmask, ServiceState.rilRadioTechnologyToNetworkType(bearer)) && dunSetting.numeric.equals(operator)) {
                    if (this.mHwCustDcTracker == null || !this.mHwCustDcTracker.addSpecifiedApnSwitch()) {
                        if (dunSetting.hasMvnoParams()) {
                            if (r != null && ApnSetting.mvnoMatches(r, dunSetting.mvnoType, dunSetting.mvnoMatchData)) {
                                retDunSettings.add(dunSetting);
                                break;
                            }
                        } else if (!this.mMvnoMatched) {
                            retDunSettings.add(dunSetting);
                            break;
                        }
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

    private boolean teardownForDun() {
        boolean z = true;
        if (ServiceState.isCdma(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            return true;
        }
        if (fetchDunApns().size() <= 0) {
            z = false;
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
        return new String[]{CharacterSets.MIMENAME_ANY_CHARSET};
    }

    /* access modifiers changed from: package-private */
    public boolean isPermanentFailure(DcFailCause dcFailCause) {
        return dcFailCause.isPermanentFailure(this.mPhone.getContext(), this.mPhone.getSubId()) && (!this.mAttached.get() || dcFailCause != DcFailCause.SIGNAL_LOST);
    }

    private ApnSetting makeApnSetting(Cursor cursor) {
        Cursor cursor2 = cursor;
        String[] types = parseTypes(cursor2.getString(cursor2.getColumnIndexOrThrow("type")));
        ApnSetting apnSetting = new ApnSetting(cursor2.getInt(cursor2.getColumnIndexOrThrow(HbpcdLookup.ID)), cursor2.getString(cursor2.getColumnIndexOrThrow("numeric")), cursor2.getString(cursor2.getColumnIndexOrThrow("name")), cursor2.getString(cursor2.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(cursor2.getString(cursor2.getColumnIndexOrThrow("proxy"))), cursor2.getString(cursor2.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(cursor2.getString(cursor2.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(cursor2.getString(cursor2.getColumnIndexOrThrow("mmsproxy"))), cursor2.getString(cursor2.getColumnIndexOrThrow("mmsport")), cursor2.getString(cursor2.getColumnIndexOrThrow("user")), cursor2.getString(cursor2.getColumnIndexOrThrow("password")), cursor2.getInt(cursor2.getColumnIndexOrThrow("authtype")), types, cursor2.getString(cursor2.getColumnIndexOrThrow("protocol")), cursor2.getString(cursor2.getColumnIndexOrThrow("roaming_protocol")), cursor2.getInt(cursor2.getColumnIndexOrThrow("carrier_enabled")) == 1, cursor2.getInt(cursor2.getColumnIndexOrThrow("network_type_bitmask")), cursor2.getInt(cursor2.getColumnIndexOrThrow("profile_id")), cursor2.getInt(cursor2.getColumnIndexOrThrow("modem_cognitive")) == 1, cursor2.getInt(cursor2.getColumnIndexOrThrow("max_conns")), cursor2.getInt(cursor2.getColumnIndexOrThrow("wait_time")), cursor2.getInt(cursor2.getColumnIndexOrThrow("max_conns_time")), cursor2.getInt(cursor2.getColumnIndexOrThrow("mtu")), cursor2.getString(cursor2.getColumnIndexOrThrow("mvno_type")), cursor2.getString(cursor2.getColumnIndexOrThrow("mvno_match_data")), cursor2.getInt(cursor2.getColumnIndexOrThrow("apn_set_id")));
        ApnSetting apn = apnSetting;
        ApnSetting hwApn = makeHwApnSetting(cursor2, types);
        if (hwApn != null) {
            return hwApn;
        }
        return apn;
    }

    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ArrayList<ApnSetting> result;
        ArrayList<ApnSetting> mnoApns = new ArrayList<>();
        ArrayList<ApnSetting> mvnoApns = new ArrayList<>();
        IccRecords r = this.mIccRecords.get();
        if (cursor.moveToFirst()) {
            do {
                ApnSetting apn = makeApnSetting(cursor);
                if (apn != null) {
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
            this.mMvnoMatched = false;
        } else {
            result = mvnoApns;
            this.mMvnoMatched = true;
        }
        log("createApnList: X result=" + result);
        return result;
    }

    private boolean dataConnectionNotInUse(DcAsyncChannel dcac) {
        log("dataConnectionNotInUse: check if dcac is inuse dcac=" + dcac);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getDcAc() == dcac) {
                log("dataConnectionNotInUse: in use by apnContext=" + apnContext);
                return false;
            }
        }
        log("dataConnectionNotInUse: tearDownAll");
        dcac.tearDownAll("No connection", null);
        log("dataConnectionNotInUse: not in use return true");
        return true;
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

    private boolean setupData(ApnContext apnContext, int radioTech, boolean unmeteredUseOnly) {
        DcAsyncChannel dcac;
        ApnContext apnContext2 = apnContext;
        int i = radioTech;
        log("setupData: apnContext=" + apnContext2);
        apnContext2.requestLog("setupData");
        DcAsyncChannel dcac2 = null;
        ApnSetting apnSetting = apnContext.getNextApnSetting();
        if (apnSetting == null) {
            log("setupData: return for no apn found!");
            return false;
        }
        int profileId = apnSetting.profileId;
        if (profileId == 0) {
            profileId = getApnProfileID(apnContext.getApnType());
        }
        int profileId2 = profileId;
        if (!apnContext.getApnType().equals("dun") || !teardownForDun()) {
            dcac2 = checkForCompatibleConnectedApnContext(apnContext);
            if (dcac2 != null) {
                ArrayList<ApnSetting> tmpList = new ArrayList<>();
                tmpList.add(apnSetting);
                if (this.mHwCustDcTracker != null && this.mHwCustDcTracker.hasBetterApnByBearer(dcac2.getApnSettingSync(), tmpList, apnContext.getApnType(), i)) {
                    log("setupData: compatible dcac is not best, no use");
                    dcac2 = null;
                }
            }
        }
        if (dcac == null) {
            if (isOnlySingleDcAllowed(i)) {
                if (isHigherPriorityApnContextActive(apnContext)) {
                    log("setupData: Higher priority ApnContext active.  Ignoring call");
                    return false;
                } else if (apnContext.getApnType().equals("ims") || !cleanUpAllConnections(true, PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION)) {
                    log("setupData: Single pdp. Continue setting up data call.");
                } else {
                    log("setupData: Some calls are disconnecting first. Wait and retry");
                    return false;
                }
            }
            dcac = findFreeDataConnection();
            if (dcac == null) {
                dcac = createDataConnection();
            }
            if (dcac == null) {
                log("setupData: No free DataConnection and couldn't create one, WEIRD");
                return false;
            }
        }
        DcAsyncChannel dcac3 = dcac;
        int generation = apnContext.incAndGetConnectionGeneration();
        log("setupData: dcac=" + dcac3 + " apnSetting=" + apnSetting + " gen#=" + generation);
        apnContext2.setDataConnectionAc(dcac3);
        apnContext2.setApnSetting(apnSetting);
        apnContext2.setState(DctConstants.State.CONNECTING);
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        Message msg = obtainMessage();
        msg.what = 270336;
        msg.obj = new Pair(apnContext2, Integer.valueOf(generation));
        HwTelephonyFactory.getHwDataServiceChrManager().setBringUp(true);
        dcac3.bringUp(apnContext2, profileId2, i, unmeteredUseOnly, msg, generation);
        log("setupData: initing!");
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x0274  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01fe  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0223  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x0253  */
    private void setInitialAttachApn() {
        ApnSetting initialAttachApnSetting;
        ApnSetting iaApnSetting = null;
        ApnSetting defaultApnSetting = null;
        ApnSetting firstApnSetting = null;
        if (get4gSlot() != this.mPhone.getSubId() && !HwModemCapability.isCapabilitySupport(21)) {
            log("setInitialAttachApn: not 4g slot , skip");
            if (IS_DELAY_ATTACH_ENABLED) {
                log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                this.mPhone.mCi.dataConnectionAttach(1, null);
            }
        } else if (SystemProperties.getBoolean("persist.radio.iot_attach_apn", false)) {
            log("setInitialAttachApn: iot attach apn enabled, skip");
        } else {
            int esmFlag = 0;
            boolean esmFlagAdaptionEnabled = getEsmFlagAdaptionEnabled();
            if (esmFlagAdaptionEnabled) {
                int esmFlagFromCard = getEsmFlagFromCard();
                if (esmFlagFromCard != -1) {
                    esmFlag = esmFlagFromCard;
                } else {
                    String plmnsConfig = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "plmn_esm_flag");
                    log("setInitialAttachApn: plmnsConfig = " + plmnsConfig);
                    IccRecords r = this.mIccRecords.get();
                    String operator = r != null ? r.getOperatorNumeric() : "null";
                    if (plmnsConfig != null) {
                        String[] plmns = plmnsConfig.split(",");
                        int length = plmns.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            }
                            String plmn = plmns[i];
                            if (plmn != null && plmn.equals(operator)) {
                                log("setInitialAttachApn: send initial attach apn for operator " + operator);
                                esmFlag = 1;
                                break;
                            }
                            i++;
                        }
                    }
                }
            }
            if (isCTSimCard(this.mPhone.getPhoneId())) {
                log("setInitialAttachApn: send initial attach apn for CT");
                esmFlag = 1;
            }
            if (esmFlag == 0 && this.mPreferredApn != null && !isApnPreset(this.mPreferredApn) && this.mPreferredApn.canHandleType("ia")) {
                log("setInitialAttachApn: send initial attach apn for IA");
                esmFlag = 1;
            }
            if (esmFlag != 0) {
                log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
                if (this.mPreferredApn != null && this.mPreferredApn.canHandleType("ia")) {
                    iaApnSetting = this.mPreferredApn;
                } else if (this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
                    firstApnSetting = this.mAllApnSettings.get(0);
                    log("setInitialApn: firstApnSetting=" + firstApnSetting);
                    Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ApnSetting apn = it.next();
                        if (apn.canHandleType("ia")) {
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
                    initialAttachApnSetting = null;
                    if (this.mPreferredApn == null) {
                        log("setInitialAttachApn: using mPreferredApn");
                        initialAttachApnSetting = isCTSimCard(this.mPhone.getPhoneId()) ? isSupportLTE(this.mPreferredApn) ? this.mPreferredApn : defaultApnSetting != null ? defaultApnSetting : iaApnSetting : this.mPreferredApn;
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
                    if (initialAttachApnSetting != null) {
                        log("setInitialAttachApn: X There in no available apn");
                        if (IS_DELAY_ATTACH_ENABLED) {
                            log("setInitialAttachApn: sbnam APN handling done, activate cs&ps");
                            this.mPhone.mCi.dataConnectionAttach(1, null);
                        }
                    } else {
                        log("setInitialAttachApn: X selected Apn=" + initialAttachApnSetting);
                        HwDataConnectionManager sHwDataConnectionManager = HwTelephonyFactory.getHwDataConnectionManager();
                        if (sHwDataConnectionManager != null && sHwDataConnectionManager.getNamSwitcherForSoftbank()) {
                            HashMap<String, String> userInfo = sHwDataConnectionManager.encryptApnInfoForSoftBank(this.mPhone, initialAttachApnSetting);
                            if (userInfo != null) {
                                String username = userInfo.get("username");
                                String password = userInfo.get("password");
                                DataServiceManager dataServiceManager = this.mDataServiceManager;
                                int i2 = initialAttachApnSetting.profileId;
                                String str = initialAttachApnSetting.apn;
                                ApnSetting apnSetting = iaApnSetting;
                                String str2 = initialAttachApnSetting.protocol;
                                ApnSetting apnSetting2 = defaultApnSetting;
                                int i3 = initialAttachApnSetting.authType;
                                ApnSetting apnSetting3 = firstApnSetting;
                                int i4 = initialAttachApnSetting.bearerBitmask == 0 ? 0 : ServiceState.bearerBitmapHasCdma(initialAttachApnSetting.bearerBitmask) ? 2 : 1;
                                int i5 = initialAttachApnSetting.maxConnsTime;
                                int i6 = esmFlag;
                                int esmFlag2 = initialAttachApnSetting.maxConns;
                                boolean z = esmFlagAdaptionEnabled;
                                int i7 = initialAttachApnSetting.waitTime;
                                HwDataConnectionManager hwDataConnectionManager = sHwDataConnectionManager;
                                boolean z2 = initialAttachApnSetting.carrierEnabled;
                                HashMap<String, String> hashMap = userInfo;
                                int i8 = initialAttachApnSetting.typesBitmap;
                                Object obj = "username";
                                String userKey = initialAttachApnSetting.roamingProtocol;
                                Object obj2 = "password";
                                int i9 = initialAttachApnSetting.bearerBitmask;
                                int i10 = initialAttachApnSetting.mtu;
                                String str3 = initialAttachApnSetting.mvnoType;
                                String str4 = initialAttachApnSetting.mvnoMatchData;
                                boolean z3 = initialAttachApnSetting.modemCognitive;
                                ApnSetting apnSetting4 = initialAttachApnSetting;
                                DataServiceManager dataServiceManager2 = dataServiceManager;
                                DataProfile dataProfile = new DataProfile(i2, str, str2, i3, username, password, i4, i5, esmFlag2, i7, z2, i8, userKey, i9, i10, str3, str4, z3);
                                dataServiceManager2.setInitialAttachApn(dataProfile, this.mPhone.getServiceState().getDataRoaming(), null);
                                log("onConnect: mApnSetting.user-mApnSetting.password handle finish");
                                return;
                            }
                        }
                        ApnSetting apnSetting5 = defaultApnSetting;
                        ApnSetting apnSetting6 = firstApnSetting;
                        int i11 = esmFlag;
                        boolean z4 = esmFlagAdaptionEnabled;
                        HwDataConnectionManager hwDataConnectionManager2 = sHwDataConnectionManager;
                        this.mDataServiceManager.setInitialAttachApn(createDataProfile(initialAttachApnSetting), this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
                    }
                }
                initialAttachApnSetting = null;
                if (this.mPreferredApn == null) {
                }
                if (initialAttachApnSetting != null) {
                }
            } else if (esmFlagAdaptionEnabled) {
                log("setInitialAttachApn: send empty initial attach apn to clear esmflag");
                DataProfile dataProfile2 = new DataProfile(0, "", SystemProperties.get("ro.config.attach_ip_type", "IP"), 0, "", "", 0, 0, 0, 0, false, 0, "", 0, 0, "", "", false);
                this.mDataServiceManager.setInitialAttachApn(dataProfile2, this.mPhone.getServiceState().getDataRoaming(), null);
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
        boolean z = true;
        boolean isDisconnected = overallState == DctConstants.State.IDLE || overallState == DctConstants.State.FAILED;
        if (this.mPhone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        }
        ApnSetting oldPreferredApn = this.mPreferredApn;
        log("onApnChanged: createAllApnList and cleanUpAllConnections");
        createAllApnList();
        ApnSetting mCurPreApn = getPreferredApn();
        if (!isBlockSetInitialAttachApn() || mCurPreApn != null) {
            setInitialAttachApn();
        } else {
            log("onApnChanged: block setInitialAttachApn");
        }
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.tryRestartRadioWhenPrefApnChange(mCurPreApn, oldPreferredApn);
        }
        if (this.mPhone.getSubId() == SubscriptionController.getInstance().getDefaultDataSubId() && mCurPreApn == null) {
            if (isDisconnected) {
                z = false;
            }
            cleanUpAllConnections(z, PhoneInternalInterface.REASON_APN_CHANGED);
        } else {
            if (isDisconnected) {
                z = false;
            }
            cleanUpConnectionsOnUpdatedApns(z, PhoneInternalInterface.REASON_APN_CHANGED);
        }
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_APN_CHANGED);
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
        int[] singleDcRats = null;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle bundle = configManager.getConfig();
            if (bundle != null) {
                singleDcRats = bundle.getIntArray("only_single_dc_allowed_int_array");
            }
        }
        boolean onlySingleDcAllowed = false;
        int i = 0;
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("persist.telephony.test.singleDc", false)) {
            onlySingleDcAllowed = true;
        }
        if (singleDcRats != null) {
            while (true) {
                int i2 = i;
                if (i2 >= singleDcRats.length || onlySingleDcAllowed) {
                    break;
                }
                if (rilRadioTech == singleDcRats[i2]) {
                    onlySingleDcAllowed = true;
                }
                i = i2 + 1;
            }
        }
        boolean onlySingleDcAllowed2 = shouldDisableMultiPdps(onlySingleDcAllowed);
        log("isOnlySingleDcAllowed(" + rilRadioTech + "): " + onlySingleDcAllowed2);
        return onlySingleDcAllowed2;
    }

    /* access modifiers changed from: package-private */
    public void sendRestartRadio() {
        log("sendRestartRadio:");
        sendMessage(obtainMessage(270362));
    }

    private void restartRadio() {
        log("restartRadio: ************TURN OFF RADIO**************");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        this.mPhone.getServiceStateTracker().powerOffRadioSafely(this);
        SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", ProxyController.MODEM_0)) + 1));
    }

    private boolean retryAfterDisconnected(ApnContext apnContext) {
        if (isDataConnectivityDisabled(this.mPhone.getSubId(), "disable-data")) {
            return false;
        }
        boolean retry = true;
        String reason = apnContext.getReason();
        if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(reason) || (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = false;
        }
        if (AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(reason)) {
            retry = false;
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
        intent.addFlags(268435456);
        intent.putExtra("subscription", this.mPhone.getSubId());
        log("startAlarmForReconnect: delay=" + delay + " action=" + intent.getAction() + " apn=" + apnContext);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), phoneId, intent, 134217728);
        apnContext.setReconnectIntent(alarmIntent);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    private void notifyNoData(DcFailCause lastFailCauseCode, ApnContext apnContext) {
        log("notifyNoData: type=" + apnContext.getApnType());
        if (isPermanentFailure(lastFailCauseCode) && !apnContext.getApnType().equals("default")) {
            SystemProperties.set("ril.ps_ce_reason", String.valueOf(lastFailCauseCode.getErrorCode()));
            this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
    }

    public boolean getAutoAttachOnCreation() {
        return this.mAutoAttachOnCreation.get();
    }

    /* access modifiers changed from: private */
    public void onRecordsLoadedOrSubIdChanged() {
        log("onRecordsLoadedOrSubIdChanged: createAllApnList");
        HwNetworkManager hwNetworkManager = HwTelephonyFactory.getHwNetworkManager();
        if (hwNetworkManager != null && hwNetworkManager.isNetworkModeAsynchronized(this.mPhone)) {
            hwNetworkManager.factoryResetNetworkTypeForNoMdn(this.mPhone);
        }
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17956894);
        updateApnContextState();
        createAllApnList();
        if (isBlockSetInitialAttachApn()) {
            log("onRecordsLoadedOrSubIdChanged: block setInitialAttachApn");
        } else if (!getmIsPseudoImsi()) {
            setInitialAttachApn();
        }
        if (getmIsPseudoImsi()) {
            checkPLMN(this.mHwCustDcTracker.getPlmn());
            log("onRecordsLoaded: createAllApnList --return due to IsPseudoImsi");
            return;
        }
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoadedOrSubIdChanged: notifying data availability");
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_SIM_LOADED);
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SIM_LOADED);
    }

    private void onSetCarrierDataEnabled(AsyncResult ar) {
        if (ar.exception != null) {
            String str = LOG_TAG;
            Rlog.e(str, "CarrierDataEnable exception: " + ar.exception);
            return;
        }
        boolean enabled = ((Boolean) ar.result).booleanValue();
        if (enabled != this.mDataEnabledSettings.isCarrierDataEnabled()) {
            log("carrier Action: set metered apns enabled: " + enabled);
            this.mDataEnabledSettings.setCarrierDataEnabled(enabled);
            if (!enabled) {
                this.mPhone.notifyOtaspChanged(5);
                cleanUpAllConnections(true, PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
            } else {
                this.mPhone.notifyOtaspChanged(this.mPhone.getServiceStateTracker().getOtasp());
                reevaluateDataConnections();
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ENABLED);
            }
        }
    }

    private void onSimNotReady() {
        log("onSimNotReady");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_SIM_NOT_READY);
        this.mAllApnSettings = new ArrayList<>();
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachOnCreation.set(false);
    }

    private void onSetDependencyMet(String apnType, boolean met) {
        if (!"hipri".equals(apnType)) {
            ApnContext apnContext = this.mApnContexts.get(apnType);
            if (apnContext == null) {
                loge("onSetDependencyMet: ApnContext not found in onSetDependencyMet(" + apnType + ", " + met + ")");
                return;
            }
            applyNewState(apnContext, apnContext.isEnabled(), met);
            if ("default".equals(apnType)) {
                ApnContext apnContext2 = this.mApnContexts.get("hipri");
                if (apnContext2 != null) {
                    applyNewState(apnContext2, apnContext2.isEnabled(), met);
                }
            }
        }
    }

    public void setPolicyDataEnabled(boolean enabled) {
        log("setPolicyDataEnabled: " + enabled);
        Message msg = obtainMessage(270368);
        msg.arg1 = enabled;
        sendMessage(msg);
    }

    private void onSetPolicyDataEnabled(boolean enabled) {
        boolean prevEnabled = isDataEnabled();
        if (this.mDataEnabledSettings.isPolicyDataEnabled() != enabled) {
            this.mDataEnabledSettings.setPolicyDataEnabled(enabled);
            if (prevEnabled == isDataEnabled()) {
                return;
            }
            if (!prevEnabled) {
                reevaluateDataConnections();
                onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                return;
            }
            onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
        }
    }

    private void applyNewState(ApnContext apnContext, boolean enabled, boolean met) {
        boolean cleanup = false;
        boolean trySetup = false;
        String str = "applyNewState(" + apnContext.getApnType() + ", " + enabled + "(" + apnContext.isEnabled() + "), " + met + "(" + apnContext.getDependencyMet() + "))";
        log(str);
        apnContext.requestLog(str);
        if (apnContext.isReady()) {
            cleanup = true;
            if (enabled && met) {
                switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[apnContext.getState().ordinal()]) {
                    case 1:
                    case 2:
                    case 4:
                        log("applyNewState: 'ready' so return");
                        apnContext.requestLog("applyNewState state=" + state + ", so return");
                        return;
                    case 3:
                    case 5:
                    case 6:
                    case 7:
                        if (!getCustRetryConfig() || !"mms".equals(apnContext.getApnType())) {
                            trySetup = true;
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            break;
                        } else {
                            log("applyNewState: the mms is retrying,return.");
                            return;
                        }
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
            if (apnContext.getState() == DctConstants.State.FAILED) {
                apnContext.setState(DctConstants.State.IDLE);
            }
            trySetup = true;
        }
        apnContext.setEnabled(enabled);
        apnContext.setDependencyMet(met);
        if (cleanup) {
            cleanUpConnection(true, apnContext);
        }
        if (trySetup) {
            apnContext.resetErrorCodeRetries();
            trySetupData(apnContext);
        }
    }

    private DcAsyncChannel checkForCompatibleConnectedApnContext(ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        ArrayList<ApnSetting> dunSettings = null;
        ApnSetting bipSetting = null;
        if ("dun".equals(apnType)) {
            dunSettings = sortApnListByPreferred(fetchDunApns());
        }
        if (isBipApnType(apnType)) {
            bipSetting = fetchBipApn(this.mPreferredApn, this.mAllApnSettings);
        }
        log("checkForCompatibleConnectedApnContext: apnContext=" + apnContext);
        DcAsyncChannel potentialDcac = null;
        ApnContext potentialApnCtx = null;
        for (ApnContext curApnCtx : this.mApnContexts.values()) {
            DcAsyncChannel curDcac = curApnCtx.getDcAc();
            if (curDcac != null) {
                ApnSetting apnSetting = curApnCtx.getApnSetting();
                log("apnSetting: " + apnSetting);
                if (dunSettings == null || dunSettings.size() <= 0) {
                    if (bipSetting == null) {
                        if (apnSetting != null && ((apnContext.getWaitingApns() == null && apnSetting.canHandleType(apnType)) || ((apnContext.getWaitingApns() != null && apnContext.getWaitingApns().contains(apnSetting)) || (this.mHwCustDcTracker != null && this.mHwCustDcTracker.isDocomoTetheringApn(apnSetting, apnType))))) {
                            switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()]) {
                                case 1:
                                    log("checkForCompatibleConnectedApnContext: found canHandle conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                    return curDcac;
                                case 2:
                                    if (potentialDcac != null) {
                                        break;
                                    } else {
                                        potentialDcac = curDcac;
                                        potentialApnCtx = curApnCtx;
                                        break;
                                    }
                                case 3:
                                case 4:
                                    potentialDcac = curDcac;
                                    potentialApnCtx = curApnCtx;
                                    break;
                            }
                        }
                    } else if (bipSetting.equals(apnSetting)) {
                        int i = AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()];
                        if (i != 1) {
                            switch (i) {
                                case 3:
                                case 4:
                                    potentialDcac = curDcac;
                                    potentialApnCtx = curApnCtx;
                                    break;
                            }
                        } else {
                            log("checkForCompatibleConnectedApnContext: found bip conn=" + curDcac + " curApnCtx=" + curApnCtx);
                            return curDcac;
                        }
                    } else {
                        continue;
                    }
                } else {
                    Iterator<ApnSetting> it = dunSettings.iterator();
                    while (it.hasNext()) {
                        if (it.next().equals(apnSetting)) {
                            switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()]) {
                                case 1:
                                    log("checkForCompatibleConnectedApnContext: found dun conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                    return curDcac;
                                case 2:
                                    if (potentialDcac != null) {
                                        break;
                                    } else {
                                        potentialDcac = curDcac;
                                        potentialApnCtx = curApnCtx;
                                        break;
                                    }
                                case 3:
                                case 4:
                                    potentialDcac = curDcac;
                                    potentialApnCtx = curApnCtx;
                                    break;
                            }
                        }
                    }
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
        msg.arg2 = enable;
        sendMessage(msg);
    }

    private void onEnableApn(int apnId, int enabled) {
        ApnContext apnContext = this.mApnContextsById.get(apnId);
        if (apnContext == null) {
            loge("onEnableApn(" + apnId + ", " + enabled + "): NO ApnContext");
            return;
        }
        clearAndResumeNetInfoForWifiLteCoexist(apnId, enabled, apnContext);
        log("onEnableApn: apnContext=" + apnContext + " call applyNewState");
        boolean z = true;
        if (enabled != 1) {
            z = false;
        }
        applyNewState(apnContext, z, apnContext.getDependencyMet());
        if (enabled == 0 && isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && !isHigherPriorityApnContextActive(apnContext)) {
            log("onEnableApn: isOnlySingleDcAllowed true & higher priority APN disabled");
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onTrySetupData(String reason) {
        log("onTrySetupData: reason=" + reason);
        setupDataOnConnectableApns(reason);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean onTrySetupData(ApnContext apnContext) {
        log("onTrySetupData: apnContext=" + apnContext);
        return trySetupData(apnContext);
    }

    public boolean isUserDataEnabled() {
        if (this.mDataEnabledSettings.isAnySimDetected() || !this.mDataEnabledSettings.isProvisioning()) {
            return this.mDataEnabledSettings.isUserDataEnabled();
        }
        return this.mDataEnabledSettings.isProvisioningDataEnabled();
    }

    public void setDataRoamingEnabledByUser(boolean enabled) {
        int phoneSubId = this.mPhone.getSubId();
        if (getDataRoamingEnabled() != enabled) {
            int roaming = enabled;
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                Settings.Global.putInt(this.mResolver, "data_roaming", roaming);
                setDataRoamingFromUserAction(true);
            } else {
                Settings.Global.putInt(this.mResolver, getDataRoamingSettingItem("data_roaming"), roaming);
            }
            this.mSubscriptionManager.setDataRoaming((int) roaming, phoneSubId);
            log("setDataRoamingEnabledByUser: set phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
            return;
        }
        log("setDataRoamingEnabledByUser: unchanged phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
    }

    public boolean getDataRoamingEnabled() {
        boolean isDataRoamingEnabled;
        boolean isDataRoamingEnabled2 = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false"));
        int phoneSubId = this.mPhone.getSubId();
        boolean z = true;
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            return true;
        }
        try {
            if (isNeedDataRoamingExpend()) {
                isDataRoamingEnabled = getDataRoamingEnabledWithNational();
            } else if (TelephonyManager.getDefault().getSimCount() == 1) {
                if (Settings.Global.getInt(this.mResolver, "data_roaming", isDataRoamingEnabled2) == 0) {
                    z = false;
                }
                isDataRoamingEnabled = z;
            } else {
                if (Settings.Global.getInt(this.mResolver, getDataRoamingSettingItem("data_roaming")) == 0) {
                    z = false;
                }
                isDataRoamingEnabled = z;
            }
        } catch (Settings.SettingNotFoundException snfe) {
            log("getDataRoamingEnabled: SettingNofFoundException snfe=" + snfe);
            isDataRoamingEnabled = getDefaultDataRoamingEnabled();
        }
        log("getDataRoamingEnabled: phoneSubId=" + phoneSubId + " isDataRoamingEnabled=" + isDataRoamingEnabled);
        return isDataRoamingEnabled;
    }

    private boolean getDefaultDataRoamingEnabled() {
        return "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false")) | ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId()).getBoolean("carrier_default_data_roaming_enabled_bool");
    }

    /* access modifiers changed from: private */
    public void setDefaultDataRoamingEnabled() {
        String setting = "data_roaming";
        boolean useCarrierSpecificDefault = false;
        if (TelephonyManager.getDefault().getSimCount() != 1) {
            setting = setting + this.mPhone.getSubId();
            try {
                Settings.Global.getInt(this.mResolver, setting);
            } catch (Settings.SettingNotFoundException e) {
                useCarrierSpecificDefault = true;
            }
        } else if (!isDataRoamingFromUserAction()) {
            useCarrierSpecificDefault = true;
        }
        if (useCarrierSpecificDefault) {
            boolean defaultVal = getDefaultDataRoamingEnabled();
            log("setDefaultDataRoamingEnabled: " + setting + "default value: " + defaultVal);
            Settings.Global.putInt(this.mResolver, setting, defaultVal);
            this.mSubscriptionManager.setDataRoaming(defaultVal, this.mPhone.getSubId());
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
        if (processAttDataRoamingOff()) {
            log("process ATT DataRoaming off");
            return;
        }
        if (!getDataRoamingEnabled()) {
            if (!TextUtils.isEmpty(getOperatorNumeric())) {
                setInitialAttachApn();
            }
            setDataProfilesAsNeeded();
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_OFF);
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_OFF);
        } else {
            notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_OFF);
        }
    }

    private void onDataRoamingOnOrSettingsChanged(int messageType) {
        log("onDataRoamingOnOrSettingsChanged");
        boolean settingChanged = messageType == 270384;
        if (settingChanged && getDataRoamingEnabled()) {
            this.mHwCustDcTracker.setDataOrRoamOn(1);
        }
        if (processAttDataRoamingOn()) {
            log("process ATT DataRoaming off");
        } else if (!this.mPhone.getServiceState().getDataRoaming()) {
            log("device is not roaming. ignored the request.");
        } else {
            checkDataRoamingStatus(settingChanged);
            if (getDataRoamingEnabled()) {
                log("onDataRoamingOnOrSettingsChanged: setup data on roaming");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_ON);
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_ON);
            } else {
                log("onDataRoamingOnOrSettingsChanged: Tear down data connection on roaming.");
                cleanUpAllConnections(true, PhoneInternalInterface.REASON_ROAMING_ON);
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
            }
        }
    }

    private void checkDataRoamingStatus(boolean settingChanged) {
        if (!settingChanged && !getDataRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == DctConstants.State.CONNECTED) {
                    LocalLog localLog = this.mDataRoamingLeakageLog;
                    StringBuilder sb = new StringBuilder();
                    sb.append("PossibleRoamingLeakage  connection params: ");
                    sb.append(apnContext.getDcAc() != null ? apnContext.getDcAc().mLastConnectionParams : "");
                    localLog.log(sb.toString());
                }
            }
        }
    }

    private void onRadioAvailable() {
        log("onRadioAvailable");
        if (this.mPhone.getSimulatedRadioControl() != null) {
            notifyDataConnection(null);
            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }
        IccRecords r = this.mIccRecords.get();
        if (r != null && (r.getImsiReady() || r.getRecordsLoaded())) {
            notifyOffApnsOfAvailability(null);
        }
        if (getOverallState() != DctConstants.State.IDLE) {
            cleanUpConnection(true, null);
        }
    }

    private void onRadioOffOrNotAvailable() {
        this.mReregisterOnReconnectFailure = false;
        this.mAutoAttachOnCreation.set(false);
        this.mIsPsRestricted = false;
        this.mPhone.getServiceStateTracker().mRestrictedState.setPsRestricted(false);
        if (this.mPhone.getSimulatedRadioControl() != null) {
            log("We're on the simulator; assuming radio off is meaningless");
        } else {
            log("onRadioOffOrNotAvailable: is off and clean up all connections");
            cleanUpAllConnections(false, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
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
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        if (this.mProvisioningSpinner != null) {
            sendMessage(obtainMessage(270378, this.mProvisioningSpinner));
        }
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        startNetStatPoll();
        startDataStallAlarm(false);
    }

    private void onDataSetupComplete(AsyncResult ar) {
        DcFailCause dcFailCause = DcFailCause.UNKNOWN;
        boolean handleError = false;
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupComplete");
        if (apnContext != null) {
            boolean isDefault = "default".equals(apnContext.getApnType());
            this.preSetupBasedRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
            if (ar.exception == null) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac == null) {
                    log("onDataSetupComplete: no connection to DC, handle as error");
                    DcFailCause cause = DcFailCause.CONNECTION_TO_DATACONNECTIONAC_BROKEN;
                    handleError = true;
                } else {
                    addIfacePhoneHashMap(dcac, mIfacePhoneHashMap);
                    if (isClearCodeEnabled() && isDefault) {
                        resetTryTimes();
                    }
                    ApnSetting apn = apnContext.getApnSetting();
                    StringBuilder sb = new StringBuilder();
                    sb.append("onDataSetupComplete: success apn=");
                    sb.append(apn == null ? "unknown" : apn.apn);
                    log(sb.toString());
                    if (isDefault) {
                        SystemProperties.set("gsm.default.apn", apn == null ? "" : apn.apn);
                        log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
                    }
                    if (needSetCTProxy(apn)) {
                        setCtProxy(dcac);
                    } else if (!(apn == null || apn.proxy == null || apn.proxy.length() == 0)) {
                        try {
                            String port = apn.port;
                            if (TextUtils.isEmpty(port)) {
                                port = "8080";
                            }
                            dcac.setLinkPropertiesHttpProxySync(new ProxyInfo(apn.proxy, Integer.parseInt(port), "127.0.0.1"));
                        } catch (NumberFormatException e) {
                            loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.port + "): " + e);
                        }
                    }
                    if (TextUtils.equals(apnContext.getApnType(), "default")) {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "true");
                        } catch (RuntimeException e2) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to true");
                        }
                        if (apn != null) {
                            String activedApnOpkey = getOpKeyByActivedApn(apn.numeric, apn.apn, apn.user);
                            log("onDataSetupComplete: activedApnVnkey = " + activedApnOpkey);
                            setApnOpkeyToSettingsDB(activedApnOpkey);
                        }
                        if (this.mCanSetPreferApn && this.mPreferredApn == null) {
                            log("onDataSetupComplete: PREFERRED APN is null");
                            if (getAttachedApnSetting() == null) {
                                this.mPreferredApn = apn;
                            }
                            if (this.mPreferredApn != null) {
                                setPreferredApn(this.mPreferredApn.id);
                            }
                        }
                    } else {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                        } catch (RuntimeException e3) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                        }
                    }
                    apnContext.setPdpFailCause(DcFailCause.NONE);
                    apnContext.setState(DctConstants.State.CONNECTED);
                    checkDataRoamingStatus(false);
                    boolean isProvApn = apnContext.isProvisioningApn();
                    ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                    if (this.mProvisionBroadcastReceiver != null) {
                        this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
                        this.mProvisionBroadcastReceiver = null;
                    }
                    if (!isProvApn || this.mIsProvisioning) {
                        cm.setProvisioningNotificationVisible(false, 0, this.mProvisionActionName);
                        completeConnection(apnContext);
                    } else {
                        log("onDataSetupComplete: successful, BUT send connected to prov apn as mIsProvisioning:" + this.mIsProvisioning + " == false && (isProvisioningApn:" + isProvApn + " == true");
                        this.mProvisionBroadcastReceiver = new ProvisionNotificationBroadcastReceiver(cm.getMobileProvisioningUrl(), TelephonyManager.getDefault().getNetworkOperatorName());
                        this.mPhone.getContext().registerReceiver(this.mProvisionBroadcastReceiver, new IntentFilter(this.mProvisionActionName));
                        cm.setProvisioningNotificationVisible(true, 0, this.mProvisionActionName);
                        setRadio(false);
                    }
                    log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType() + ", reason:" + apnContext.getReason());
                    if (Build.IS_DEBUGGABLE) {
                        int pcoVal = SystemProperties.getInt("persist.radio.test.pco", -1);
                        if (pcoVal != -1) {
                            log("PCO testing: read pco value from persist.radio.test.pco " + pcoVal);
                            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                            intent.putExtra("apnType", "default");
                            intent.putExtra("apnProto", "IPV4V6");
                            intent.putExtra("pcoId", 65280);
                            intent.putExtra("pcoValue", new byte[]{(byte) pcoVal});
                            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                        }
                    }
                    setFirstTimeEnableData();
                    log("CHR inform CHR the APN info when data setup succ");
                    LinkProperties chrLinkProperties = getLinkProperties(apnContext.getApnType());
                    if (chrLinkProperties != null) {
                        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDataConnected(this.mPhone, apn, chrLinkProperties);
                    }
                    getNetdPid();
                }
            } else {
                DcFailCause cause2 = (DcFailCause) ar.result;
                ApnSetting apn2 = apnContext.getApnSetting();
                Object[] objArr = new Object[2];
                objArr[0] = apn2 == null ? "unknown" : apn2.apn;
                objArr[1] = cause2;
                log(String.format("onDataSetupComplete: error apn=%s cause=%s", objArr));
                sendDSMipErrorBroadcast();
                if (cause2.isEventLoggable()) {
                    EventLog.writeEvent(EventLogTags.PDP_SETUP_FAIL, new Object[]{Integer.valueOf(cause2.ordinal()), Integer.valueOf(getCellLocationId()), Integer.valueOf(TelephonyManager.getDefault().getNetworkType())});
                }
                ApnSetting apn3 = apnContext.getApnSetting();
                this.mPhone.notifyPreciseDataConnectionFailed(apnContext.getReason(), apnContext.getApnType(), apn3 != null ? apn3.apn : "unknown", cause2.toString());
                Intent intent2 = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED");
                intent2.putExtra("errorCode", cause2.getErrorCode());
                intent2.putExtra("apnType", apnContext.getApnType());
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent2);
                long elapsedRealtime = SystemClock.elapsedRealtime();
                if (cause2.isRestartRadioFail(this.mPhone.getContext(), this.mPhone.getSubId()) || needRestartRadioOnError(apnContext, cause2)) {
                    log("Modem restarted.");
                    sendRestartRadio();
                }
                if (isClearCodeEnabled()) {
                    long delay = apnContext.getDelayForNextApn(this.mFailFast);
                    String str = LOG_TAG;
                    Rlog.d(str, "clearcode onDataSetupComplete delay=" + delay);
                    operateClearCodeProcess(apnContext, cause2, (int) delay);
                } else if (isPermanentFailure(cause2)) {
                    log("cause = " + cause2 + ", mark apn as permanent failed. apn = " + apn3);
                    apnContext.markApnPermanentFailed(apn3);
                }
                handleError = true;
            }
            if (handleError) {
                onDataSetupCompleteError(ar);
            }
            if (!this.mDataEnabledSettings.isInternalDataEnabled()) {
                cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
            }
        }
    }

    private ApnContext getValidApnContext(AsyncResult ar, String logString) {
        if (ar != null && (ar.userObj instanceof Pair)) {
            Pair<ApnContext, Integer> pair = (Pair) ar.userObj;
            ApnContext apnContext = (ApnContext) pair.first;
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
                if (SystemProperties.getBoolean("ro.config.hw_enable_ota_bip_lgu", false) && "bip0".equals(apnContext.getApnType())) {
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
            if (HwTelephonyFactory.getHwPhoneManager().isSupportOrangeApn(this.mPhone)) {
                HwTelephonyFactory.getHwPhoneManager().addSpecialAPN(this.mPhone);
                Rlog.d(LOG_TAG, "onDataSetupCompleteError.addSpecialAPN()");
            }
            long delay = apnContext.getDelayForNextApn(this.mFailFast);
            if (isPSClearCodeRplmnMatched()) {
                log("PSCLEARCODE retry APN. old delay = " + delay);
                delay = updatePSClearCodeApnContext(ar, apnContext, delay);
            }
            if (delay >= 0) {
                log("onDataSetupCompleteError: Try next APN. delay = " + delay);
                if (isClearCodeEnabled()) {
                    setCurFailCause(ar);
                }
                apnContext.setState(DctConstants.State.SCANNING);
                if (isClearCodeEnabled()) {
                    delay = (long) getDelayTime();
                }
                startAlarmForReconnect(delay, apnContext);
                if (!(apnContext.getReason() == null || apnContext.getApnSetting() == null || apnContext.getApnType() == null)) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendIntentDataConnectionSetupResult(this.mPhone.getSubId(), PhoneConstants.DataState.DISCONNECTED.toString(), apnContext.getReason(), apnContext.getApnSetting().apn, apnContext.getApnType(), getLinkProperties(apnContext.getApnType()));
                }
            } else {
                onAllApnPermActiveFailed();
                apnContext.setState(DctConstants.State.FAILED);
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_APN_FAILED, apnContext.getApnType());
                apnContext.setDataConnectionAc(null);
                log("onDataSetupCompleteError: Stop retrying APNs.");
            }
        }
    }

    private void onDataConnectionRedirected(String redirectUrl) {
        if (!TextUtils.isEmpty(redirectUrl)) {
            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED");
            intent.putExtra("redirectionUrl", redirectUrl);
            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            log("Notify carrier signal receivers with redirectUrl: " + redirectUrl);
        }
    }

    private void onDisconnectDone(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDone");
        if (apnContext != null) {
            if (apnContext.getState() == DctConstants.State.CONNECTING) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac != null && !dcac.isInactiveSync() && dcac.checkApnContextSync(apnContext)) {
                    loge("onDisconnectDone: apnContext is activating, ignore " + apnContext);
                    return;
                }
            }
            log("onDisconnectDone: EVENT_DISCONNECT_DONE apnContext=" + apnContext);
            apnContext.setState(DctConstants.State.IDLE);
            if ("default".equals(apnContext.getApnType())) {
                SystemProperties.set("gsm.default.apn", "");
                log("gsm.default.apn: " + SystemProperties.get("gsm.default.apn"));
            }
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            if (getOverallState() != DctConstants.State.CONNECTED) {
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
                    }
                    return;
                }
                log("data is disconnected and check if need to setPreferredNetworkType");
                ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
                if (sst != null) {
                    HwTelephonyFactory.getHwNetworkManager().checkAndSetNetworkType(sst, this.mPhone);
                }
                if (!(sst == null || sst.returnObject() == null || (!sst.returnObject().isDataOffForbidLTE() && !sst.returnObject().isDataOffbyRoamAndData()))) {
                    log("DCT onDisconnectDone");
                    sst.returnObject().processEnforceLTENetworkTypePending();
                }
            }
            if (!this.mAttached.get() || !apnContext.isReady() || !retryAfterDisconnected(apnContext)) {
                boolean restartRadioAfterProvisioning = this.mPhone.getContext().getResources().getBoolean(17957008);
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
                } else if (this.mPhone.getServiceState().getVoiceRegState() != 0 || !retryAfterDisconnected(apnContext)) {
                    log("onDisconnectDone: not retrying");
                } else {
                    startAlarmForReconnect(5000, apnContext);
                }
            } else {
                try {
                    SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                } catch (RuntimeException e) {
                    log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                }
                log("onDisconnectDone: attached, ready and retry after disconnect");
                long delay = apnContext.getRetryAfterDisconnectDelay();
                if (SystemProperties.getLong("persist.radio.telecom_apn_delay", 0) <= 0) {
                    if (this.mIsScreenOn && (DcFailCause.LOST_CONNECTION.toString().equals(apnContext.getReason()) || PhoneInternalInterface.REASON_PDP_RESET.equals(apnContext.getReason()))) {
                        delay = 50;
                        log(apnContext.getReason() + " reduce the delay time to " + 50);
                    }
                    if (isCTSimCard(this.mPhone.getPhoneId()) && PhoneInternalInterface.REASON_NW_TYPE_CHANGED.equals(apnContext.getReason())) {
                        delay = 50;
                        log("NW_TYPE_CHANGED & CTSIM reduce delay to " + 50);
                    }
                }
                if (delay > 0) {
                    startAlarmForReconnect(delay, apnContext);
                }
            }
            if (this.mDisconnectPendingCount > 0) {
                this.mDisconnectPendingCount--;
            }
            if (this.mDisconnectPendingCount == 0) {
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                notifyDataDisconnectComplete();
                notifyAllDataDisconnected();
            }
        }
    }

    private void onDisconnectDcRetrying(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDcRetrying");
        if (apnContext != null) {
            apnContext.setState(DctConstants.State.RETRYING);
            log("onDisconnectDcRetrying: apnContext=" + apnContext);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        }
    }

    private void onVoiceCallStarted() {
        log("onVoiceCallStarted");
        this.mInVoiceCall = true;
        if (isConnected() && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_STARTED);
        }
    }

    private void onVoiceCallEnded() {
        log("onVoiceCallEnded");
        this.mInVoiceCall = false;
        if (isConnected()) {
            if (!this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                startNetStatPoll();
                startDataStallAlarm(false);
                notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
            } else {
                resetPollStats();
            }
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
    }

    private void onCleanUpConnection(boolean tearDown, int apnId, String reason) {
        log("onCleanUpConnection");
        ApnContext apnContext = this.mApnContextsById.get(apnId);
        if (apnContext != null) {
            apnContext.setReason(reason);
            cleanUpConnection(tearDown, apnContext);
        }
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

    /* access modifiers changed from: protected */
    public void notifyDataConnection(String reason) {
        String str;
        log("notifyDataConnection: reason=" + reason);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                log("notifyDataConnection: type:" + apnContext.getApnType());
                Phone phone = this.mPhone;
                if (reason != null) {
                    str = reason;
                } else {
                    str = apnContext.getReason();
                }
                phone.notifyDataConnection(str, apnContext.getApnType());
            }
        }
        notifyOffApnsOfAvailability(reason);
    }

    private void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        if (this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
            ArrayList<DataProfile> dps = new ArrayList<>();
            Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
            while (it.hasNext()) {
                ApnSetting apn = it.next();
                if (apn.modemCognitive || HuaweiTelephonyConfigs.isMTKPlatform()) {
                    DataProfile dp = createDataProfile(apn);
                    if (!dps.contains(dp)) {
                        dps.add(dp);
                    }
                }
            }
            if (dps.size() > 0) {
                this.mDataServiceManager.setDataProfile(dps, this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
            }
        }
    }

    private void createAllApnList() {
        Cursor cursor;
        this.mMvnoMatched = false;
        this.mAllApnSettings = new ArrayList<>();
        String operator = getCTOperator(getOperatorNumeric());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(this.mPhone.getSubId()))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mPhone.getSubId()));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            log("createAllApnList: selection=" + selection);
            String subId = Long.toString((long) this.mPhone.getSubId());
            if (this.isMultiSimEnabled) {
                cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(MSIM_TELEPHONY_CARRIERS_URI, subId), null, selection, null, HbpcdLookup.ID);
            } else {
                cursor = this.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, selection, null, HbpcdLookup.ID);
            }
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mAllApnSettings = createApnList(cursor);
                }
                cursor.close();
            }
            IccRecords r = this.mIccRecords.get();
            if (this.mAllApnSettings.isEmpty() && !VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) && get4gSlot() == this.mPhone.getSubId() && r != null && true == r.getRecordsLoaded() && operator.length() != 0) {
                HwTelephonyFactory.getHwDataServiceChrManager().sendIntentApnListEmpty(this.mPhone.getSubId());
            }
        }
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId()) && this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: vsim enabled and apn not in database");
            this.mAllApnSettings = VSimUtilsInner.createVSimApnList();
        }
        addEmergencyApnSetting();
        if (this.mAllApnSettings.isEmpty() || VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            log("createAllApnList: No APN found for carrier: " + operator);
            this.mPreferredApn = null;
        } else {
            this.mPreferredApn = getPreferredApn();
            if (this.mPreferredApn == null) {
                ApnSetting apn = getCustPreferredApn(this.mAllApnSettings);
                if (apn != null) {
                    setPreferredApn(apn.id);
                    this.mPreferredApn = getPreferredApn();
                }
            }
            if (this.mPreferredApn != null && !this.mPreferredApn.numeric.equals(operator)) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
        setDataProfilesAsNeeded();
    }

    private void dedupeApnSettings() {
        new ArrayList();
        for (int i = 0; i < this.mAllApnSettings.size() - 1; i++) {
            ApnSetting first = this.mAllApnSettings.get(i);
            int j = i + 1;
            while (j < this.mAllApnSettings.size()) {
                ApnSetting second = this.mAllApnSettings.get(j);
                if (first.similar(second)) {
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
        String str;
        ApnSetting apnSetting = dest;
        ApnSetting apnSetting2 = src;
        int id = apnSetting.id;
        ArrayList<String> resultTypes = new ArrayList<>();
        resultTypes.addAll(Arrays.asList(apnSetting.types));
        int id2 = id;
        for (String srcType : apnSetting2.types) {
            if (!resultTypes.contains(srcType)) {
                resultTypes.add(srcType);
            }
            if (srcType.equals("default")) {
                id2 = apnSetting2.id;
            }
        }
        String mmsc = TextUtils.isEmpty(apnSetting.mmsc) ? apnSetting2.mmsc : apnSetting.mmsc;
        String mmsProxy = TextUtils.isEmpty(apnSetting.mmsProxy) ? apnSetting2.mmsProxy : apnSetting.mmsProxy;
        String mmsPort = TextUtils.isEmpty(apnSetting.mmsPort) ? apnSetting2.mmsPort : apnSetting.mmsPort;
        String proxy = TextUtils.isEmpty(apnSetting.proxy) ? apnSetting2.proxy : apnSetting.proxy;
        String port = TextUtils.isEmpty(apnSetting.port) ? apnSetting2.port : apnSetting.port;
        String protocol = apnSetting2.protocol.equals("IPV4V6") ? apnSetting2.protocol : apnSetting.protocol;
        if (apnSetting2.roamingProtocol.equals("IPV4V6")) {
            str = apnSetting2.roamingProtocol;
        } else {
            str = apnSetting.roamingProtocol;
        }
        String roamingProtocol = str;
        int networkTypeBitmask = (apnSetting.networkTypeBitmask == 0 || apnSetting2.networkTypeBitmask == 0) ? 0 : apnSetting.networkTypeBitmask | apnSetting2.networkTypeBitmask;
        if (networkTypeBitmask == 0) {
            networkTypeBitmask = ServiceState.convertBearerBitmaskToNetworkTypeBitmask((apnSetting.bearerBitmask == 0 || apnSetting2.bearerBitmask == 0) ? 0 : apnSetting.bearerBitmask | apnSetting2.bearerBitmask);
        }
        ArrayList<String> arrayList = resultTypes;
        ApnSetting apnSetting3 = new ApnSetting(id2, apnSetting.numeric, apnSetting.carrier, apnSetting.apn, proxy, port, mmsc, mmsProxy, mmsPort, apnSetting.user, apnSetting.password, apnSetting.authType, (String[]) resultTypes.toArray(new String[0]), protocol, roamingProtocol, apnSetting.carrierEnabled, networkTypeBitmask, apnSetting.profileId, apnSetting.modemCognitive || apnSetting2.modemCognitive, apnSetting.maxConns, apnSetting.waitTime, apnSetting.maxConnsTime, apnSetting.mtu, apnSetting.mvnoType, apnSetting.mvnoMatchData, apnSetting.apnSetId);
        return apnSetting3;
    }

    private DcAsyncChannel createDataConnection() {
        log("createDataConnection E");
        int id = this.mUniqueIdGenerator.getAndIncrement();
        DataConnection conn = DataConnection.makeDataConnection(this.mPhone, id, this, this.mDataServiceManager, this.mDcTesterFailBringUpAll, this.mDcc);
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
        boolean isNeedFilterVowifiMmsForPrefApn;
        boolean isNeedFilterVowifiMms;
        char c;
        ArrayList<ApnSetting> apnList;
        String str = requestedApnType;
        int i = radioTech;
        log("buildWaitingApns: E requestedApnType=" + str);
        ArrayList<ApnSetting> apnList2 = new ArrayList<>();
        if (str.equals("dun")) {
            ArrayList<ApnSetting> dunApns = fetchDunApns();
            if (dunApns.size() > 0) {
                Iterator<ApnSetting> it = dunApns.iterator();
                while (it.hasNext()) {
                    apnList2.add(it.next());
                    log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList2);
                }
                return sortApnListByPreferred(apnList2);
            }
        }
        if (isBipApnType(requestedApnType)) {
            ApnSetting bip = fetchBipApn(this.mPreferredApn, this.mAllApnSettings);
            if (bip != null) {
                apnList2.add(bip);
                log("buildWaitingApns: X added APN_TYPE_BIP apnList=" + apnList2);
                return apnList2;
            }
        }
        if (CT_SUPL_FEATURE_ENABLE && "supl".equals(str) && isCTSimCard(this.mPhone.getSubId())) {
            ArrayList<ApnSetting> suplApnList = buildWaitingApnsForCTSupl(requestedApnType, radioTech);
            if (!suplApnList.isEmpty()) {
                return suplApnList;
            }
        }
        String operator = getCTOperator(getOperatorNumeric());
        try {
            usePreferred = !this.mPhone.getContext().getResources().getBoolean(17956935);
        } catch (Resources.NotFoundException e) {
            log("buildWaitingApns: usePreferred NotFoundException set to true");
            usePreferred = true;
        }
        if (usePreferred) {
            this.mPreferredApn = getPreferredApn();
        }
        log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + i);
        boolean isApnCanHandleType = true;
        if (this.mHwCustDcTracker != null) {
            isApnCanHandleType = this.mHwCustDcTracker.isCanHandleType(this.mPreferredApn, str);
        }
        log("buildWaitingApns: isNeedFilterVowifiMmsForPrefApn = " + isNeedFilterVowifiMmsForPrefApn);
        int i2 = 13;
        ApnSetting apnSetting = null;
        if (usePreferred && this.mCanSetPreferApn && this.mPreferredApn != null && this.mPreferredApn.canHandleType(str) && isApnCanHandleType && !isNeedFilterVowifiMmsForPrefApn) {
            log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.numeric + ":" + this.mPreferredApn);
            if (this.mPreferredApn.numeric == null || !this.mPreferredApn.numeric.equals(operator)) {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            } else if (!isCTSimCard(this.mPhone.getPhoneId()) || !isLTENetwork() || !isApnPreset(this.mPreferredApn)) {
                if (!ServiceState.bitmaskHasTech(this.mPreferredApn.bearerBitmask, i)) {
                    log("buildWaitingApns: no preferred APN");
                    setPreferredApn(-1);
                    this.mPreferredApn = null;
                } else if (this.mHwCustDcTracker == null || this.mHwCustDcTracker.canKeepApn(str, this.mPreferredApn, true)) {
                    apnList2.add(this.mPreferredApn);
                    log("buildWaitingApns: X added preferred apnList=" + apnList);
                    return apnList;
                } else {
                    log("not add preferred apn to WaitingApns, not applicable");
                }
            } else if (this.mPreferredApn.bearer == 13 || this.mPreferredApn.bearer == 14) {
                apnList2.add(this.mPreferredApn);
                return apnList2;
            }
        }
        String operatorCT = this.mPhone.getServiceState().getOperatorNumeric();
        if (operatorCT == null || "".equals(operatorCT) || (!"46003".equals(operatorCT) && !"46011".equals(operatorCT) && !"46012".equals(operatorCT))) {
        }
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            loge("mAllApnSettings is null!");
        } else {
            log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
            Iterator<ApnSetting> it2 = this.mAllApnSettings.iterator();
            while (it2.hasNext()) {
                ApnSetting apn = it2.next();
                boolean isApnCanHandleType2 = true;
                if (this.mHwCustDcTracker != null) {
                    isApnCanHandleType2 = this.mHwCustDcTracker.isCanHandleType(apn, str);
                }
                if (apn.canHandleType("default")) {
                    setAttachedApnSetting(apnSetting);
                }
                log("buildWaitingApns: isNeedFilterVowifiMms = " + isNeedFilterVowifiMms);
                if (!apn.canHandleType(str) || !isApnCanHandleType2 || isNeedFilterVowifiMms) {
                    c = 14;
                    log("buildWaitingApns: couldn't handle requested ApnType=" + str);
                } else if (WAP_APN.equals(apn.apn) && isApnPreset(apn) && "default".equals(str)) {
                    log("buildWaitingApns: unicom skip add 3gwap for default, " + apn.toString());
                    c = 14;
                } else if (!isCTSimCard(this.mPhone.getPhoneId()) || !isLTENetwork()) {
                    c = 14;
                    if (ServiceState.bitmaskHasTech(apn.bearerBitmask, i)) {
                        if (this.mHwCustDcTracker != null) {
                            if (!this.mHwCustDcTracker.canKeepApn(str, apn, false)) {
                                log("not add apn to WaitingApns, not applicable");
                            }
                        }
                        log("buildWaitingApns: adding apn=" + apn);
                        apnList2.add(apn);
                    } else {
                        log("buildWaitingApns: bearerBitmask:" + apn.bearerBitmask + " or networkTypeBitmask:" + apn.networkTypeBitmask + "do not include radioTech:" + i);
                    }
                } else {
                    if (apn.bearer != i2) {
                        c = 14;
                        if (apn.bearer != 14) {
                        }
                    } else {
                        c = 14;
                    }
                    log("buildWaitingApns: adding apn=" + apn.toString());
                    apnList2.add(apn);
                }
                char c2 = c;
                i2 = 13;
                apnSetting = null;
            }
        }
        log("buildWaitingApns: " + sortApnListByPreferred(this.mHwCustDcTracker != null ? this.mHwCustDcTracker.sortApnListByBearer(apnList2, str, i) : apnList2).size() + " APNs in the list: " + apnList);
        return sortApnListByPreferred(this.mHwCustDcTracker != null ? this.mHwCustDcTracker.sortApnListByBearer(apnList2, str, i) : apnList2);
    }

    @VisibleForTesting
    public ArrayList<ApnSetting> sortApnListByPreferred(ArrayList<ApnSetting> list) {
        if (list == null || list.size() <= 1) {
            return list;
        }
        final int preferredApnSetId = getPreferredApnSetId();
        if (preferredApnSetId != 0) {
            list.sort(new Comparator<ApnSetting>() {
                public int compare(ApnSetting apn1, ApnSetting apn2) {
                    if (apn1.apnSetId == preferredApnSetId) {
                        return -1;
                    }
                    if (apn2.apnSetId == preferredApnSetId) {
                        return 1;
                    }
                    return 0;
                }
            });
        }
        return list;
    }

    private String apnListToString(ArrayList<ApnSetting> apns) {
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

    private ApnSetting getPreferredApn() {
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("getPreferredApn: mAllApnSettings is ");
            sb.append(this.mAllApnSettings == null ? "null" : "empty");
            log(sb.toString());
            return null;
        } else if (needRemovedPreferredApn()) {
            return null;
        } else {
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId())), new String[]{HbpcdLookup.ID, "name", "apn"}, null, null, "name ASC");
            int i = 0;
            if (cursor != null) {
                this.mCanSetPreferApn = true;
            } else {
                this.mCanSetPreferApn = false;
            }
            StringBuilder sb2 = new StringBuilder();
            sb2.append("getPreferredApn: mRequestedApnType=");
            sb2.append(this.mRequestedApnType);
            sb2.append(" cursor=");
            sb2.append(cursor);
            sb2.append(" cursor.count=");
            if (cursor != null) {
                i = cursor.getCount();
            }
            sb2.append(i);
            log(sb2.toString());
            if (this.mCanSetPreferApn && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int pos = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
                Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
                while (it.hasNext()) {
                    ApnSetting p = it.next();
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x04d6, code lost:
        onDataRoamingOnOrSettingsChanged(r8.what);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x0528, code lost:
        com.android.internal.telephony.HwTelephonyFactory.getHwDataServiceChrManager().setReceivedSimloadedMsg(r7.mPhone, true, r7.mApnContexts, r7.mDataEnabledSettings.isUserDataEnabled());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x0543, code lost:
        if (android.telephony.SubscriptionManager.isValidSubscriptionId(r7.mPhone.getSubId()) == false) goto L_0x0549;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x0545, code lost:
        onRecordsLoadedOrSubIdChanged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x0549, code lost:
        log("Ignoring EVENT_RECORDS_LOADED as subId is not valid: " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x055e, code lost:
        onRadioAvailable();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x056a, code lost:
        handleCustMessage(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x056d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00f4, code lost:
        if (r0.isSoftBankCard(r7.mPhone) != false) goto L_0x056a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0107, code lost:
        r7.mPhone.mCi.resetAllConnections();
     */
    public void handleMessage(Message msg) {
        boolean met;
        boolean isProvApn;
        boolean isProvApn2;
        String url;
        String operator;
        log("handleMessage msg=" + msg);
        beforeHandleMessage(msg);
        int i = msg.what;
        boolean enabled = true;
        switch (i) {
            case 270336:
                onDataSetupComplete((AsyncResult) msg.obj);
                break;
            case 270337:
                break;
            case 270338:
                break;
            case 270339:
                if (!(msg.obj instanceof ApnContext)) {
                    if (!(msg.obj instanceof String)) {
                        loge("EVENT_TRY_SETUP request w/o apnContext or String");
                        break;
                    } else {
                        onTrySetupData((String) msg.obj);
                        break;
                    }
                } else {
                    onTrySetupData((ApnContext) msg.obj);
                    break;
                }
            default:
                switch (i) {
                    case 270342:
                        onRadioOffOrNotAvailable();
                        if (this.mHwCustDcTracker != null) {
                            this.mHwCustDcTracker.tryClearRejFlag();
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
                        switch (i) {
                            case 270347:
                                break;
                            case 270348:
                                onDataRoamingOff();
                                break;
                            case 270349:
                                onEnableApn(msg.arg1, msg.arg2);
                                break;
                            default:
                                switch (i) {
                                    case 270351:
                                        log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DONE ");
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
                                                        cleanUpAllConnections(false, PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                                                        this.mReregisterOnReconnectFailure = false;
                                                    }
                                                    ApnContext apnContext = this.mApnContextsById.get(0);
                                                    if (apnContext != null) {
                                                        apnContext.setReason(PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                                                        trySetupData(apnContext);
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
                                                if (msg.arg1 == 0) {
                                                    enabled = false;
                                                }
                                                boolean tearDown = enabled;
                                                log("EVENT_CLEAN_UP_CONNECTION tearDown=" + tearDown);
                                                if (!(msg.obj instanceof ApnContext)) {
                                                    onCleanUpConnection(tearDown, msg.arg2, (String) msg.obj);
                                                    break;
                                                } else {
                                                    cleanUpConnection(tearDown, (ApnContext) msg.obj);
                                                    break;
                                                }
                                            default:
                                                switch (i) {
                                                    case 270362:
                                                        restartRadio();
                                                        break;
                                                    case 270363:
                                                        if (msg.arg1 != 1) {
                                                            enabled = false;
                                                        }
                                                        onSetInternalDataEnabled(enabled, (Message) msg.obj);
                                                        break;
                                                    default:
                                                        switch (i) {
                                                            case 270365:
                                                                if (msg.obj != null && !(msg.obj instanceof String)) {
                                                                    msg.obj = null;
                                                                }
                                                                onCleanUpAllConnections((String) msg.obj);
                                                                break;
                                                            case 270366:
                                                                if (msg.arg1 != 1) {
                                                                    enabled = false;
                                                                }
                                                                log("CMD_SET_USER_DATA_ENABLE enabled=" + enabled);
                                                                onSetUserDataEnabled(enabled);
                                                                break;
                                                            case 270367:
                                                                if (msg.arg1 != 1) {
                                                                    enabled = false;
                                                                }
                                                                log("CMD_SET_DEPENDENCY_MET met=" + met);
                                                                Bundle bundle = msg.getData();
                                                                if (bundle != null) {
                                                                    String apnType = (String) bundle.get("apnType");
                                                                    if (apnType != null) {
                                                                        onSetDependencyMet(apnType, met);
                                                                        break;
                                                                    }
                                                                }
                                                                break;
                                                            case 270368:
                                                                if (msg.arg1 != 1) {
                                                                    enabled = false;
                                                                }
                                                                onSetPolicyDataEnabled(enabled);
                                                                break;
                                                            case 270369:
                                                                onUpdateIcc();
                                                                break;
                                                            case 270370:
                                                                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DC_RETRYING");
                                                                onDisconnectDcRetrying((AsyncResult) msg.obj);
                                                                break;
                                                            case 270371:
                                                                onDataSetupCompleteError((AsyncResult) msg.obj);
                                                                break;
                                                            case 270372:
                                                                sEnableFailFastRefCounter += msg.arg1 == 1 ? 1 : -1;
                                                                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA:  sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                                                                if (sEnableFailFastRefCounter < 0) {
                                                                    loge("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: sEnableFailFastRefCounter:" + sEnableFailFastRefCounter + " < 0");
                                                                    sEnableFailFastRefCounter = 0;
                                                                }
                                                                boolean enabled2 = sEnableFailFastRefCounter > 0;
                                                                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: enabled=" + enabled2 + " sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                                                                if (this.mFailFast != enabled2) {
                                                                    this.mFailFast = enabled2;
                                                                    if (enabled2) {
                                                                        enabled = false;
                                                                    }
                                                                    this.mDataStallDetectionEnabled = enabled;
                                                                    if (this.mDataStallDetectionEnabled && getOverallState() == DctConstants.State.CONNECTED && (!this.mInVoiceCall || this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
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
                                                                Bundle bundle2 = msg.getData();
                                                                if (bundle2 != null) {
                                                                    try {
                                                                        this.mProvisioningUrl = (String) bundle2.get("provisioningUrl");
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
                                                                String apnType2 = null;
                                                                try {
                                                                    Bundle bundle3 = msg.getData();
                                                                    if (bundle3 != null) {
                                                                        apnType2 = (String) bundle3.get("apnType");
                                                                    }
                                                                    if (TextUtils.isEmpty(apnType2)) {
                                                                        loge("CMD_IS_PROVISIONING_APN: apnType is empty");
                                                                        isProvApn2 = false;
                                                                    } else {
                                                                        isProvApn2 = isProvisioningApn(apnType2);
                                                                    }
                                                                    isProvApn = isProvApn2;
                                                                } catch (ClassCastException e2) {
                                                                    loge("CMD_IS_PROVISIONING_APN: NO provisioning url ignoring");
                                                                    isProvApn = false;
                                                                }
                                                                log("CMD_IS_PROVISIONING_APN: ret=" + isProvApn);
                                                                AsyncChannel asyncChannel = this.mReplyAc;
                                                                if (!isProvApn) {
                                                                    enabled = false;
                                                                }
                                                                asyncChannel.replyToMessage(msg, 270374, enabled ? 1 : 0);
                                                                break;
                                                            case 270375:
                                                                log("EVENT_PROVISIONING_APN_ALARM");
                                                                ApnContext apnCtx = this.mApnContextsById.get(0);
                                                                if (apnCtx.isProvisioningApn() && apnCtx.isConnectedOrConnecting()) {
                                                                    if (this.mProvisioningApnAlarmTag != msg.arg1) {
                                                                        log("EVENT_PROVISIONING_APN_ALARM: ignore stale tag, mProvisioningApnAlarmTag:" + this.mProvisioningApnAlarmTag + " != arg1:" + msg.arg1);
                                                                        break;
                                                                    } else {
                                                                        log("EVENT_PROVISIONING_APN_ALARM: Disconnecting");
                                                                        this.mIsProvisioning = false;
                                                                        this.mProvisioningUrl = null;
                                                                        stopProvisioningApnAlarm();
                                                                        sendCleanUpConnection(true, apnCtx);
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
                                                                if (this.mPhone.getServiceState().getRilDataRadioTechnology() != 0) {
                                                                    onRatChange();
                                                                    if (onUpdateIcc()) {
                                                                        log("onUpdateIcc: tryRestartDataConnections nwTypeChanged");
                                                                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED, RetryFailures.ONLY_ON_CHANGE);
                                                                        break;
                                                                    }
                                                                }
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
                                                                log("dataConnectionTracker.handleMessage: EVENT_REDIRECTION_DETECTED=" + url);
                                                                onDataConnectionRedirected(url);
                                                                break;
                                                            case 270381:
                                                                handlePcoData((AsyncResult) msg.obj);
                                                                break;
                                                            case 270382:
                                                                onSetCarrierDataEnabled((AsyncResult) msg.obj);
                                                                break;
                                                            case 270383:
                                                                onDataReconnect(msg.getData());
                                                                break;
                                                            case 270384:
                                                                break;
                                                            case 270385:
                                                                onDataServiceBindingChanged(((Boolean) ((AsyncResult) msg.obj).result).booleanValue());
                                                                break;
                                                            default:
                                                                switch (i) {
                                                                    case 271144:
                                                                        if (!checkMvnoParams()) {
                                                                            HwDataConnectionManager sHwDataConnectionManager = HwTelephonyFactory.getHwDataConnectionManager();
                                                                            if (sHwDataConnectionManager != null) {
                                                                                if (sHwDataConnectionManager.getNamSwitcherForSoftbank()) {
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                        break;
                                                                    case 271145:
                                                                        AsyncResult arNvcfg = (AsyncResult) msg.obj;
                                                                        if (arNvcfg != null && arNvcfg.exception == null) {
                                                                            int nvcfgResult = ((Integer) arNvcfg.result).intValue();
                                                                            log("EVENT_UNSOL_SIM_NVCFG_FINISHED: operator=" + operator + ", nvcfgResult=" + nvcfgResult);
                                                                            if (!TextUtils.isEmpty(operator) && 1 == nvcfgResult) {
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
                                                                        switch (i) {
                                                                            case 69636:
                                                                                log("DISCONNECTED_CONNECTED: msg=" + msg);
                                                                                DcAsyncChannel dcac = (DcAsyncChannel) msg.obj;
                                                                                this.mDataConnectionAcHashMap.remove(Integer.valueOf(dcac.getDataConnectionIdSync()));
                                                                                dcac.disconnected();
                                                                                break;
                                                                            case 271137:
                                                                                break;
                                                                            case 271140:
                                                                                if (mWcdmaVpEnabled) {
                                                                                    log("EVENT_VP_STATUS_CHANGED");
                                                                                    onVpStatusChanged((AsyncResult) msg.obj);
                                                                                    break;
                                                                                }
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
        }
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
        if (VSimUtilsInner.isVSimSub(this.mPhone.getPhoneId())) {
            return VSimUtilsInner.fetchVSimIccRecords(appFamily);
        }
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    private boolean onUpdateIcc() {
        boolean result = false;
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
        UiccCardApplication newUiccApplication = getUiccCardApplication(appFamily);
        IccRecords r = this.mIccRecords.get();
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
                if (getAttachedApnSetting() != null) {
                    log("clean the mAttachedApnSettings, set with null.");
                    setAttachedApnSetting(null);
                }
            }
            if (newUiccApplication == null || newIccRecords == null) {
                onSimNotReady();
            } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                log("New records found");
                this.mUiccApplcation = newUiccApplication;
                this.mIccRecords.set(newIccRecords);
                registerForImsi(newUiccApplication, newIccRecords);
                HwTelephonyFactory.getHwDataServiceChrManager().setRecordsLoadedRegistered(true, this.mPhone.getSubId());
                registerForFdnRecordsLoaded(newIccRecords);
                newIccRecords.registerForRecordsLoaded(this, 270338, null);
            }
            result = true;
        }
        return result;
    }

    public void update() {
        log("update sub = " + this.mPhone.getSubId());
        log("update(): Active DDS, register for all events now!");
        onUpdateIcc();
        this.mAutoAttachOnCreation.set(false);
        ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        HwTelephonyFactory.getHwDataServiceChrManager().setCheckApnContextState(false);
    }

    public void cleanUpAllConnections(String cause) {
        cleanUpAllConnections(cause, (Message) null);
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

    private void notifyDataDisconnectComplete() {
        log("notifyDataDisconnectComplete");
        Iterator<Message> it = this.mDisconnectAllCompleteMsgList.iterator();
        while (it.hasNext()) {
            it.next().sendToTarget();
        }
        this.mDisconnectAllCompleteMsgList.clear();
    }

    private void notifyAllDataDisconnected() {
        sEnableFailFastRefCounter = 0;
        this.mFailFast = false;
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

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDataEnabledSettings.registerForDataEnabledChanged(h, what, obj);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDataEnabledSettings.unregisterForDataEnabledChanged(h);
    }

    private void onSetInternalDataEnabled(boolean enabled, Message onCompleteMsg) {
        log("onSetInternalDataEnabled: enabled=" + enabled);
        boolean sendOnComplete = true;
        this.mDataEnabledSettings.setInternalDataEnabled(enabled);
        if (enabled) {
            log("onSetInternalDataEnabled: changed to enabled, try to setup data call");
            onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
        } else {
            sendOnComplete = false;
            log("onSetInternalDataEnabled: changed to disabled, cleanUpAllConnections");
            cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED, onCompleteMsg);
        }
        if (sendOnComplete && onCompleteMsg != null) {
            onCompleteMsg.sendToTarget();
        }
    }

    public boolean setInternalDataEnabledFlag(boolean enable) {
        log("setInternalDataEnabledFlag(" + enable + ")");
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString() + "\n");
        }
        log(sb.toString());
        this.mDataEnabledSettings.setInternalDataEnabled(enable);
        return true;
    }

    public boolean setInternalDataEnabled(boolean enable) {
        return setInternalDataEnabled(enable, null);
    }

    public boolean setInternalDataEnabled(boolean enable, Message onCompleteMsg) {
        log("setInternalDataEnabled(" + enable + ")");
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = new Exception().getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString() + "\n");
        }
        log(sb.toString());
        Message msg = obtainMessage(270363, onCompleteMsg);
        msg.arg1 = enable;
        sendMessage(msg);
        return true;
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
        boolean z = true;
        if (VSimUtilsInner.isVSimSub(this.mPhone.getSubId())) {
            return true;
        }
        long defaultDds = (long) SubscriptionController.getInstance().getDefaultDataSubId();
        log("isDefaultDataSubscription subId: " + subId + "defaultDds: " + defaultDds);
        if (subId != defaultDds) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void log(String s) {
        String str = LOG_TAG;
        Rlog.d(str, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void loge(String s) {
        String str = LOG_TAG;
        Rlog.e(str, "[" + this.mPhone.getPhoneId() + "]" + s);
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
        pw.println(" mDataStallTxRxSum=" + this.mDataStallTxRxSum);
        pw.println(" mDataStallAlarmTag=" + this.mDataStallAlarmTag);
        pw.println(" mDataStallDetectionEnabled=" + this.mDataStallDetectionEnabled);
        pw.println(" mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        pw.println(" mNoRecvPollCount=" + this.mNoRecvPollCount);
        pw.println(" mResolver=" + this.mResolver);
        pw.println(" mReconnectIntent=" + this.mReconnectIntent);
        pw.println(" mAutoAttachOnCreation=" + this.mAutoAttachOnCreation.get());
        pw.println(" mIsScreenOn=" + this.mIsScreenOn);
        pw.println(" mUniqueIdGenerator=" + this.mUniqueIdGenerator);
        pw.println(" mDataRoamingLeakageLog= ");
        this.mDataRoamingLeakageLog.dump(fd, pw, args);
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
            Set<Map.Entry<Integer, DataConnection>> mDcSet = this.mDataConnections.entrySet();
            pw.println(" mDataConnections: count=" + mDcSet.size());
            for (Map.Entry<Integer, DataConnection> entry : mDcSet) {
                pw.printf(" *** mDataConnection[%d] \n", new Object[]{entry.getKey()});
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
                pw.printf(" mApnToDataConnectonId[%s]=%d\n", new Object[]{entry2.getKey(), entry2.getValue()});
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
        ArrayList<ApnSetting> apnSettings = this.mAllApnSettings;
        if (apnSettings != null) {
            pw.println(" mAllApnSettings size=" + apnSettings.size());
            for (int i = 0; i < apnSettings.size(); i++) {
                pw.printf(" mAllApnSettings[%d]: %s\n", new Object[]{Integer.valueOf(i), apnSettings.get(i)});
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
            apnContext = this.mApnContextsById.get(9);
        } else if (TextUtils.equals(apnType, "ims")) {
            apnContext = this.mApnContextsById.get(5);
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
                this.mEmergencyApn = makeApnSetting(cursor);
            }
            cursor.close();
        }
    }

    private void addEmergencyApnSetting() {
        if (!this.mEmergencyApnLoaded) {
            initEmergencyApnSetting();
            this.mEmergencyApnLoaded = true;
        }
        if (this.mEmergencyApn == null) {
            return;
        }
        if (this.mAllApnSettings == null) {
            this.mAllApnSettings = new ArrayList<>();
            return;
        }
        boolean hasEmergencyApn = false;
        Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
        while (true) {
            if (it.hasNext()) {
                if (ArrayUtils.contains(it.next().types, "emergency")) {
                    hasEmergencyApn = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (!hasEmergencyApn) {
            this.mAllApnSettings.add(this.mEmergencyApn);
        } else {
            log("addEmergencyApnSetting - E-APN setting is already present");
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

    private void cleanUpConnectionsOnUpdatedApns(boolean tearDown, String reason) {
        log("cleanUpConnectionsOnUpdatedApns: tearDown=" + tearDown);
        if (this.mAllApnSettings != null && this.mAllApnSettings.isEmpty()) {
            cleanUpAllConnections(tearDown, PhoneInternalInterface.REASON_APN_CHANGED);
        } else if (this.mPhone.getServiceState().getRilDataRadioTechnology() != 0) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                ArrayList<ApnSetting> currentWaitingApns = apnContext.getWaitingApns();
                ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), this.mPhone.getServiceState().getRilDataRadioTechnology());
                log("new waitingApns:" + waitingApns);
                if (currentWaitingApns != null && (waitingApns.size() != currentWaitingApns.size() || !containsAllApns(currentWaitingApns, waitingApns))) {
                    log("new waiting apn is different for " + apnContext);
                    apnContext.setWaitingApns(waitingApns);
                    if (!apnContext.isDisconnected()) {
                        log("cleanUpConnectionsOnUpdatedApns for " + apnContext);
                        apnContext.setReason(reason);
                        cleanUpConnection(true, apnContext);
                    }
                }
            }
        } else {
            return;
        }
        if (isConnected() == 0) {
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
        this.mNetStatPollPeriod = 1000;
    }

    /* access modifiers changed from: private */
    public void startNetStatPoll() {
        if (getOverallState() == DctConstants.State.CONNECTED && !this.mNetStatPollEnabled) {
            log("startNetStatPoll");
            resetPollStats();
            this.mNetStatPollEnabled = true;
            this.mPollNetStat.run();
        }
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    /* access modifiers changed from: private */
    public void stopNetStatPoll() {
        this.mNetStatPollEnabled = false;
        removeCallbacks(this.mPollNetStat);
        log("stopNetStatPoll");
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
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

    /* access modifiers changed from: private */
    public void updateDataActivity() {
        DctConstants.Activity newActivity;
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
            if (sent > 0 && received > 0) {
                newActivity = DctConstants.Activity.DATAINANDOUT;
                updateDSUseDuration();
            } else if (sent > 0 && received == 0) {
                newActivity = DctConstants.Activity.DATAOUT;
                updateDSUseDuration();
            } else if (sent != 0 || received <= 0) {
                newActivity = this.mActivity == DctConstants.Activity.DORMANT ? this.mActivity : DctConstants.Activity.NONE;
            } else {
                newActivity = DctConstants.Activity.DATAIN;
                updateDSUseDuration();
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
            String str = LOG_TAG;
            Rlog.e(str, "PCO_DATA exception: " + ar.exception);
            return;
        }
        PcoData pcoData = (PcoData) ar.result;
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.savePcoData(pcoData);
        }
        ArrayList<DataConnection> dcList = new ArrayList<>();
        DataConnection temp = this.mDcc.getActiveDcByCid(pcoData.cid);
        if (temp != null) {
            dcList.add(temp);
        }
        if (dcList.size() == 0) {
            String str2 = LOG_TAG;
            Rlog.e(str2, "PCO_DATA for unknown cid: " + pcoData.cid + ", inferring");
            Iterator<DataConnection> it = this.mDataConnections.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                DataConnection dc = it.next();
                int cid = dc.getCid();
                if (cid == pcoData.cid) {
                    String str3 = LOG_TAG;
                    Rlog.d(str3, "  found " + dc);
                    dcList.clear();
                    dcList.add(dc);
                    break;
                } else if (cid == -1) {
                    Iterator<ApnContext> it2 = dc.mApnContexts.keySet().iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            if (it2.next().getState() == DctConstants.State.CONNECTING) {
                                String str4 = LOG_TAG;
                                Rlog.d(str4, "  found potential " + dc);
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
            Rlog.e(LOG_TAG, "PCO_DATA - couldn't infer cid");
            return;
        }
        Iterator<DataConnection> it3 = dcList.iterator();
        while (it3.hasNext()) {
            DataConnection dc2 = it3.next();
            if (dc2.mApnContexts.size() == 0) {
                break;
            }
            for (ApnContext apnContext : dc2.mApnContexts.keySet()) {
                String apnType = apnContext.getApnType();
                Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                intent.putExtra("apnType", apnType);
                intent.putExtra("apnProto", pcoData.bearerProto);
                intent.putExtra("pcoId", pcoData.pcoId);
                intent.putExtra("pcoValue", pcoData.contents);
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            }
        }
    }

    private int getRecoveryAction() {
        return Settings.System.getInt(this.mResolver, "radio.data.stall.recovery.action", 0);
    }

    /* access modifiers changed from: protected */
    public void putRecoveryAction(int action) {
        Settings.System.putInt(this.mResolver, "radio.data.stall.recovery.action", action);
    }

    private void broadcastDataStallDetected(int recoveryAction) {
        Intent intent = new Intent("android.intent.action.DATA_STALL_DETECTED");
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        intent.putExtra("recoveryAction", recoveryAction);
        this.mPhone.getContext().sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    private void doRecovery() {
        if (getOverallState() == DctConstants.State.CONNECTED) {
            int recoveryAction = getRecoveryAction();
            TelephonyMetrics.getInstance().writeDataStallEvent(this.mPhone.getPhoneId(), recoveryAction);
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenDorecovery(this.mPhone, recoveryAction);
            broadcastDataStallDetected(recoveryAction);
            switch (recoveryAction) {
                case 0:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, this.mSentSinceLastRecv);
                    log("doRecovery() get data call list");
                    if (this.mDcc != null) {
                        this.mDcc.getDataCallList();
                    }
                    if (!noNeedDoRecovery(this.mApnContexts)) {
                        log("Since this apn is preseted apn, so we need to do recovery.");
                        putRecoveryAction(1);
                        break;
                    } else {
                        putRecoveryAction(0);
                        log("This apn is not preseted apn or we set nodorecovery to fobid do recovery, so we needn't to do recovery.");
                        break;
                    }
                case 1:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_CLEANUP, this.mSentSinceLastRecv);
                    log("doRecovery() cleanup all connections");
                    cleanUpAllConnections(PhoneInternalInterface.REASON_PDP_RESET);
                    putRecoveryAction(2);
                    break;
                case 2:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_REREGISTER, this.mSentSinceLastRecv);
                    log("doRecovery() re-register");
                    this.mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    putRecoveryAction(3);
                    break;
                case 3:
                    updateLastRadioResetTimestamp();
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART, this.mSentSinceLastRecv);
                    log("restarting radio");
                    this.mPhone.getServiceStateTracker().setDoRecoveryTriggerState(true);
                    restartRadio();
                    putRecoveryAction(0);
                    break;
                default:
                    String str = LOG_TAG;
                    Rlog.e(str, "doRecovery: Invalid recoveryAction = " + recoveryAction);
                    putRecoveryAction(0);
                    break;
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
            long[] dnsTxRx = getDnsPacketTxRxSum();
            this.mDataStallTxRxSum.txPkts += dnsTxRx[0];
            this.mDataStallTxRxSum.rxPkts += dnsTxRx[1];
            log("updateDataStallInfo: getDnsPacketTxRxSum dnsTx=" + dnsTxRx[0] + " dnsRx=" + dnsTxRx[1]);
        }
        log("updateDataStallInfo: mDataStallTxRxSum=" + this.mDataStallTxRxSum + " preTxRxSum=" + preTxRxSum);
        long sent = this.mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        long received = this.mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;
        if (sent > 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(0);
        } else if (sent > 0 && received == 0) {
            if (isPhoneStateIdle()) {
                this.mSentSinceLastRecv += sent;
            } else {
                this.mSentSinceLastRecv = 0;
            }
            log("updateDataStallInfo: OUT sent=" + sent + " mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        } else if (sent == 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(0);
        }
    }

    private boolean isPhoneStateIdle() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        int i = 0;
        while (i < phoneCount) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone == null || phone.getState() == PhoneConstants.State.IDLE) {
                i++;
            } else {
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
        updateDataStallInfo();
        boolean suspectedStall = false;
        if (this.mSentSinceLastRecv >= ((long) Settings.Global.getInt(this.mResolver, "pdp_watchdog_trigger_packet_count", 10))) {
            if (!isPingOk()) {
                log("onDataStallAlarm: tag=" + tag + " do recovery action=" + getRecoveryAction());
                suspectedStall = true;
            } else {
                this.mSentSinceLastRecv = 0;
            }
        }
        startDataStallAlarm(suspectedStall);
    }

    private void startDataStallAlarm(boolean suspectedStall) {
        int delayInMs;
        int nextAction = getRecoveryAction();
        if (this.mDataStallDetectionEnabled && getOverallState() == DctConstants.State.CONNECTED) {
            if (this.mIsScreenOn || suspectedStall || RecoveryAction.isAggressiveRecovery(nextAction)) {
                delayInMs = Settings.Global.getInt(this.mResolver, "data_stall_alarm_aggressive_delay_in_ms", 60000);
            } else {
                if (SystemProperties.getBoolean("ro.config.power", false)) {
                    DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 6000000;
                }
                delayInMs = Settings.Global.getInt(this.mResolver, "data_stall_alarm_non_aggressive_delay_in_ms", DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            }
            this.mDataStallAlarmTag++;
            Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
            intent.addFlags(268435456);
            intent.putExtra(DATA_STALL_ALARM_TAG_EXTRA, this.mDataStallAlarmTag);
            this.mDataStallAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
            this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mDataStallAlarmIntent);
        }
    }

    private void stopDataStallAlarm() {
        this.mDataStallAlarmTag++;
        if (this.mDataStallAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mDataStallAlarmIntent);
            this.mDataStallAlarmIntent = null;
        }
    }

    /* access modifiers changed from: private */
    public void restartDataStallAlarm() {
        if (isConnected()) {
            if (RecoveryAction.isAggressiveRecovery(getRecoveryAction())) {
                log("restartDataStallAlarm: action is pending. not resetting the alarm.");
                return;
            }
            stopDataStallAlarm();
            startDataStallAlarm(false);
        }
    }

    /* access modifiers changed from: private */
    public void onActionIntentProvisioningApnAlarm(Intent intent) {
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
        this.mProvisioningApnAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mProvisioningApnAlarmIntent);
    }

    private void stopProvisioningApnAlarm() {
        log("stopProvisioningApnAlarm: current tag=" + this.mProvisioningApnAlarmTag + " mProvsioningApnAlarmIntent=" + this.mProvisioningApnAlarmIntent);
        this.mProvisioningApnAlarmTag = this.mProvisioningApnAlarmTag + 1;
        if (this.mProvisioningApnAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mProvisioningApnAlarmIntent);
            this.mProvisioningApnAlarmIntent = null;
        }
    }

    private static DataProfile createDataProfile(ApnSetting apn) {
        return createDataProfile(apn, apn.profileId);
    }

    @VisibleForTesting
    public static DataProfile createDataProfile(ApnSetting apn, int profileId) {
        int i;
        ApnSetting apnSetting = apn;
        int bearerBitmap = ServiceState.convertNetworkTypeBitmaskToBearerBitmask(apnSetting.networkTypeBitmask);
        if (bearerBitmap == 0) {
            i = 0;
        } else if (ServiceState.bearerBitmapHasCdma(bearerBitmap)) {
            i = 2;
        } else {
            i = 1;
        }
        int profileType = i;
        String str = apnSetting.apn;
        String str2 = apnSetting.protocol;
        int i2 = apnSetting.authType;
        String str3 = apnSetting.user;
        String str4 = apnSetting.password;
        int i3 = apnSetting.maxConnsTime;
        int i4 = apnSetting.maxConns;
        int i5 = apnSetting.waitTime;
        boolean z = apnSetting.carrierEnabled;
        int i6 = apnSetting.typesBitmap;
        String str5 = apnSetting.roamingProtocol;
        String str6 = str5;
        int i7 = profileId;
        int i8 = bearerBitmap;
        int i9 = profileType;
        DataProfile dataProfile = new DataProfile(i7, str, str2, i2, str3, str4, i9, i3, i4, i5, z, i6, str6, i8, apnSetting.mtu, apnSetting.mvnoType, apnSetting.mvnoMatchData, apnSetting.modemCognitive);
        return dataProfile;
    }

    private void onDataServiceBindingChanged(boolean bound) {
        if (bound) {
            this.mDcc.start();
        } else {
            this.mDcc.dispose();
        }
    }

    private boolean isMmsApn(ApnContext apnContext) {
        return apnContext != null && "mms".equals(apnContext.getApnType());
    }

    private boolean isXcapApn(ApnContext apnContext) {
        return apnContext != null && "xcap".equals(apnContext.getApnType());
    }

    /* access modifiers changed from: protected */
    public ArrayList<ApnSetting> getAllApnList() {
        return this.mAllApnSettings;
    }

    /* access modifiers changed from: protected */
    public ApnSetting getPreferredApnHw() {
        return getPreferredApn();
    }

    /* access modifiers changed from: protected */
    public void setPreferredApnHw(int pos) {
        setPreferredApn(pos);
    }

    /* access modifiers changed from: protected */
    public void startNetStatPollHw() {
        startNetStatPoll();
    }

    /* access modifiers changed from: protected */
    public void stopNetStatPollHw() {
        stopNetStatPoll();
    }

    /* access modifiers changed from: protected */
    public void startDataStallAlarmHw(boolean suspectedStall) {
        startDataStallAlarm(suspectedStall);
    }

    /* access modifiers changed from: protected */
    public void stopDataStallAlarmHw() {
        stopDataStallAlarm();
    }

    /* access modifiers changed from: protected */
    public void restartDataStallAlarmHw() {
        restartDataStallAlarm();
    }

    /* access modifiers changed from: protected */
    public void cancelReconnectAlarmHw(ApnContext apnContext) {
        cancelReconnectAlarm(apnContext);
    }

    /* access modifiers changed from: protected */
    public void unregisterForAllEventsHw() {
        unregisterForAllEvents();
    }

    /* access modifiers changed from: protected */
    public void registerForAllEventsHw() {
        registerForAllEvents();
    }

    /* access modifiers changed from: protected */
    public boolean isOnlySingleDcAllowedHw(int rilRadioTech) {
        return isOnlySingleDcAllowed(rilRadioTech);
    }

    /* access modifiers changed from: protected */
    public void setupDataOnConnectableApnsHw(String reason) {
        setupDataOnConnectableApns(reason);
    }

    /* access modifiers changed from: protected */
    public IccRecords getIccRecordsHw() {
        return this.mIccRecords.get();
    }

    /* access modifiers changed from: protected */
    public SparseArray<ApnContext> getApnContextsHw() {
        return this.mApnContextsById;
    }

    /* access modifiers changed from: protected */
    public void createAllApnListHw() {
        createAllApnList();
    }

    /* access modifiers changed from: protected */
    public ArrayList<ApnContext> getMPrioritySortedApnContexts() {
        return this.mPrioritySortedApnContexts;
    }

    /* access modifiers changed from: protected */
    public boolean cleanUpAllConnectionsHw(boolean tearDown, String reason) {
        return cleanUpAllConnections(tearDown, reason);
    }

    /* access modifiers changed from: protected */
    public boolean onTrySetupDataHw(String reason) {
        return onTrySetupData(reason);
    }

    /* access modifiers changed from: protected */
    public boolean onTrySetupDataHw(ApnContext apnContext) {
        return onTrySetupData(apnContext);
    }

    /* access modifiers changed from: protected */
    public void cleanUpConnectionHw(boolean tearDown, ApnContext apnContext) {
        cleanUpConnection(tearDown, apnContext);
    }

    public final ConcurrentHashMap<String, ApnContext> getMApnContextsHw() {
        return this.mApnContexts;
    }

    /* access modifiers changed from: protected */
    public void notifyOffApnsOfAvailabilityHw(String reason) {
        notifyOffApnsOfAvailability(reason);
    }

    /* access modifiers changed from: protected */
    public void notifyDataConnectionHw(String reason) {
        notifyDataConnection(reason);
    }

    public void checkPLMN(String plmn) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.checkPLMN(plmn);
        }
    }

    private void onOtaAttachFailed(ApnContext apnContext) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.onOtaAttachFailed(apnContext);
        }
    }

    private boolean getmIsPseudoImsi() {
        if (this.mHwCustDcTracker != null) {
            return this.mHwCustDcTracker.getmIsPseudoImsi();
        }
        return false;
    }

    private void sendOTAAttachTimeoutMsg(ApnContext apnContext, boolean retValue) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.sendOTAAttachTimeoutMsg(apnContext, retValue);
        }
    }

    private void openServiceStart(UiccController uiccController) {
        if (this.mHwCustDcTracker != null) {
            this.mHwCustDcTracker.openServiceStart(uiccController);
        }
    }

    private boolean isDataDisable(int subId) {
        if (this.mHwCustDcTracker != null) {
            return this.mHwCustDcTracker.isDataDisable(subId);
        }
        return false;
    }
}

package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.LinkProperties;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.GlobalParamsAdaptor;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwServiceStateManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase.DcTrackerBaseReference;
import com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReason;
import com.android.internal.telephony.dataconnection.DcTracker.DataAllowFailReasonType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import huawei.cust.HwCustUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HwDcTrackerBaseReference implements DcTrackerBaseReference {
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = null;
    protected static final String CAUSE_NO_RETRY_AFTER_DISCONNECT = SystemProperties.get("ro.hwpp.disc_noretry_cause", "");
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final String CLEARCODE_2HOUR_DELAY_OVER = "clearcode2HourDelayOver";
    private static final String CT_CDMA_OPERATOR = "46003";
    private static final String CUST_PREFERRED_APN = SystemProperties.get("ro.hwpp.preferred_apn", "").trim();
    public static final int DATA_ROAMING_EXCEPTION = -1;
    public static final int DATA_ROAMING_INTERNATIONAL = 2;
    public static final int DATA_ROAMING_NATIONAL = 1;
    public static final int DATA_ROAMING_OFF = 0;
    public static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static final boolean DBG = true;
    private static final int DELAY_2_HOUR = 7200000;
    protected static final String ENABLE_ALLOW_MMS = "enable_always_allow_mms";
    private static final int EVENT_FDN_RECORDS_LOADED = 2;
    private static final int EVENT_FDN_SWITCH_CHANGED = 1;
    private static final int EVENT_LIMIT_PDP_ACT_IND = 4;
    private static final int EVENT_VOICE_CALL_ENDED = 3;
    protected static final Uri FDN_URL = Uri.parse("content://icc/fdn/subId/");
    private static final String INTENT_LIMIT_PDP_ACT_IND = "com.android.internal.telephony.limitpdpactind";
    private static final String INTENT_SET_PREF_NETWORK_TYPE = "com.android.internal.telephony.set-pref-networktype";
    private static final String INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE = "network_type";
    private static final boolean IS_ATT;
    private static final boolean IS_DUAL_4G_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    private static final String IS_LIMIT_PDP_ACT = "islimitpdpact";
    private static final int MCC_LENGTH = 3;
    protected static final boolean MMSIgnoreDSSwitchNotRoaming;
    protected static final boolean MMSIgnoreDSSwitchOnRoaming;
    protected static final boolean MMS_ON_ROAMING = ((MMS_PROP & 1) == 1);
    protected static final int MMS_PROP = SystemProperties.getInt("ro.config.hw_always_allow_mms", 4);
    private static final String NETD_PROCESS_UID = "0";
    private static final int NETWORK_MODE_GSM_UMTS = 3;
    private static final int NETWORK_MODE_LTE_GSM_WCDMA = 9;
    private static final int NETWORK_MODE_UMTS_ONLY = 2;
    private static final int PID_STATS_FILE_IFACE_INDEX = 1;
    private static final int PID_STATS_FILE_PROCESS_NAME_INDEX = 2;
    private static final int PID_STATS_FILE_UDP_RX_INDEX = 14;
    private static final int PID_STATS_FILE_UDP_TX_INDEX = 20;
    private static final int PID_STATS_FILE_UID_INDEX = 3;
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G = 10000;
    private static final int PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G = 45000;
    private static final long PS_CLEARCODE_APN_DELAY_MILLIS_2G_3G = (SystemProperties.getLong("ro.config.clearcode_2g3g_timer", 45) * 1000);
    private static final long PS_CLEARCODE_APN_DELAY_MILLIS_4G = (SystemProperties.getLong("ro.config.clearcode_4g_timer", 10) * 1000);
    private static final long PS_CLEARCODE_LIMIT_PDP_ACT_DELAY = (SystemProperties.getLong("ro.config.clearcode_limit_timer", 1) * 1000);
    private static final String PS_CLEARCODE_PLMN = SystemProperties.get("ro.config.clearcode_plmn", "");
    private static final boolean RESET_PROFILE = SystemProperties.getBoolean("ro.hwpp_reset_profile", false);
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    public static final int SUB2 = 1;
    private static final String TAG = "HwDcTrackerBaseReference";
    protected static final boolean USER_FORCE_DATA_SETUP = SystemProperties.getBoolean("ro.hwpp.allow_data_onlycs", false);
    private static final String XCAP_DATA_ROAMING_ENABLE = "carrier_xcap_data_roaming_switch";
    protected static final boolean isMultiSimEnabled = HwFrameworkFactory.getHwInnerTelephonyManager().isMultiSimEnabled();
    private static int newRac = -1;
    private static int oldRac = -1;
    private static final String pidStatsPath = "/proc/net/xt_qtaguid/stats_pid";
    protected boolean ALLOW_MMS = false;
    private boolean SETAPN_UNTIL_CARDLOADED = SystemProperties.getBoolean("ro.config.delay_setapn", false);
    private boolean SUPPORT_MPDN = SystemProperties.getBoolean("persist.telephony.mpdn", true);
    private ContentObserver allowMmsObserver = null;
    private boolean broadcastPrePostPay = true;
    GsmCellLocation cellLoc = new GsmCellLocation();
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            HwDcTrackerBaseReference.this.log("handleMessage msg=" + msg.what);
            switch (msg.what) {
                case 3:
                    HwDcTrackerBaseReference.this.onVoiceCallEndedHw();
                    return;
                case 4:
                    AsyncResult ar = msg.obj;
                    if (ar.exception != null) {
                        HwDcTrackerBaseReference.this.log("PSCLEARCODE EVENT_LIMIT_PDP_ACT_IND exception " + ar.exception);
                        return;
                    } else {
                        HwDcTrackerBaseReference.this.onLimitPDPActInd(ar);
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private boolean isRecievedPingReply = false;
    private boolean isSupportPidStats = false;
    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmManager;
    private PendingIntent mClearCodeLimitAlarmIntent = null;
    public DcFailCause mCurFailCause;
    private DcTracker mDcTrackerBase;
    private int mDelayTime = 3000;
    private boolean mDoRecoveryAddDnsProp = SystemProperties.getBoolean("ro.config.dorecovery_add_dns", false);
    private FdnAsyncQueryHandler mFdnAsyncQuery;
    private FdnChangeObserver mFdnChangeObserver;
    ServiceStateTracker mGsmServiceStateTracker = null;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                int subid = 0;
                if (intent.getExtra("subscription") != null) {
                    subid = intent.getIntExtra("subscription", -1);
                }
                if (subid != HwDcTrackerBaseReference.this.mSubscription.intValue()) {
                    Rlog.d(HwDcTrackerBaseReference.TAG, "receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , but the subid is different from mSubscription");
                    return;
                }
                String curSimState = intent.getStringExtra("ss");
                if (TextUtils.equals(curSimState, HwDcTrackerBaseReference.this.mSimState)) {
                    Rlog.d(HwDcTrackerBaseReference.TAG, "the curSimState is same as mSimState, so return");
                    return;
                }
                if (("ABSENT".equals(curSimState) || "CARD_IO_ERROR".equals(curSimState)) && ("ABSENT".equals(HwDcTrackerBaseReference.this.mSimState) ^ 1) != 0 && ("CARD_IO_ERROR".equals(HwDcTrackerBaseReference.this.mSimState) ^ 1) != 0 && HwDcTrackerBaseReference.RESET_PROFILE) {
                    Rlog.d(HwDcTrackerBaseReference.TAG, "receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , resetprofile");
                    HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.mCi.resetProfile(null);
                }
                HwDcTrackerBaseReference.this.mSimState = curSimState;
            } else if (intent.getAction() != null && intent.getAction().equals(HwDcTrackerBaseReference.INTENT_SET_PREF_NETWORK_TYPE)) {
                HwDcTrackerBaseReference.this.onActionIntentSetNetworkType(intent);
            } else if (intent.getAction() != null && HwDcTrackerBaseReference.INTENT_LIMIT_PDP_ACT_IND.equals(intent.getAction())) {
                HwDcTrackerBaseReference.this.onActionIntentLimitPDPActInd(intent);
            }
        }
    };
    private boolean mIsClearCodeEnabled = SystemProperties.getBoolean("ro.config.hw_clearcode_pdp", false);
    private boolean mIsLimitPDPAct = false;
    private INetworkManagementService mNetworkManager = null;
    private int mNwOldMode = Phone.PREFERRED_NT_MODE;
    private AlertDialog mPSClearCodeDialog = null;
    private String mSimState = null;
    private Integer mSubscription;
    private int mTryIndex = 0;
    protected UiccController mUiccController = UiccController.getInstance();
    private int netdPid = -1;
    private int nwMode = Phone.PREFERRED_NT_MODE;
    private ContentObserver nwModeChangeObserver = null;
    private int oldRadioTech = 0;
    private Condition pingCondition = this.pingThreadlLock.newCondition();
    private ReentrantLock pingThreadlLock = new ReentrantLock();
    PhoneStateListener pslForCellLocation = new PhoneStateListener() {
        public void onCellLocationChanged(CellLocation location) {
            if (HwDcTrackerBaseReference.this.mDcTrackerBase.mApnContexts != null) {
                try {
                    HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged");
                    if (location instanceof GsmCellLocation) {
                        GsmCellLocation newCellLoc = (GsmCellLocation) location;
                        HwDcTrackerBaseReference.this.mGsmServiceStateTracker = HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getServiceStateTracker();
                        HwDcTrackerBaseReference.newRac = HwServiceStateManager.getHwGsmServiceStateManager(HwDcTrackerBaseReference.this.mGsmServiceStateTracker, (GsmCdmaPhone) HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone).getRac();
                        int radioTech = HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
                        HwDcTrackerBaseReference.this.log("CLEARCODE newCellLoc = " + newCellLoc + ", oldCellLoc = " + HwDcTrackerBaseReference.this.cellLoc + " oldRac = " + HwDcTrackerBaseReference.oldRac + " newRac = " + HwDcTrackerBaseReference.newRac + " radioTech = " + radioTech + " oldRadioTech = " + HwDcTrackerBaseReference.this.oldRadioTech);
                        boolean isClearRetryAlarm = (HuaweiTelephonyConfigs.isQcomPlatform() || HwDcTrackerBaseReference.this.oldRadioTech == radioTech) ? false : true;
                        if (isClearRetryAlarm) {
                            HwDcTrackerBaseReference.this.oldRadioTech = radioTech;
                            HwDcTrackerBaseReference.this.log("clearcode oldRadioTech = " + HwDcTrackerBaseReference.this.oldRadioTech);
                            HwDcTrackerBaseReference.oldRac = -1;
                            HwDcTrackerBaseReference.this.resetTryTimes();
                        }
                        if (-1 == HwDcTrackerBaseReference.newRac) {
                            HwDcTrackerBaseReference.this.log("CLEARCODE not really changed");
                            return;
                        } else if (HwDcTrackerBaseReference.oldRac == HwDcTrackerBaseReference.newRac || radioTech != 3) {
                            HwDcTrackerBaseReference.this.log("CLEARCODE RAC not really changed");
                            return;
                        } else if (-1 == HwDcTrackerBaseReference.oldRac) {
                            HwDcTrackerBaseReference.oldRac = HwDcTrackerBaseReference.newRac;
                            HwDcTrackerBaseReference.this.log("CLEARCODE oldRac = -1 return");
                            return;
                        } else {
                            HwDcTrackerBaseReference.oldRac = HwDcTrackerBaseReference.newRac;
                            HwDcTrackerBaseReference.this.cellLoc = newCellLoc;
                            DcTracker dcTracker = HwDcTrackerBaseReference.this.mDcTrackerBase;
                            ApnContext defaultApn = (ApnContext) HwDcTrackerBaseReference.this.mDcTrackerBase.mApnContexts.get("default");
                            if (!(!HwDcTrackerBaseReference.this.mDcTrackerBase.isUserDataEnabled() || defaultApn == null || defaultApn.getState() == State.CONNECTED)) {
                                int curPrefMode = Global.getInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", 0);
                                HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged radioTech = " + radioTech + " curPrefMode" + curPrefMode);
                                if (!(curPrefMode == 9 || curPrefMode == 2)) {
                                    HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.setPreferredNetworkType(9, null);
                                    Global.putInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", 9);
                                    HwServiceStateManager.getHwGsmServiceStateManager(HwDcTrackerBaseReference.this.mGsmServiceStateTracker, (GsmCdmaPhone) HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone).setRac(-1);
                                    HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged try switch 3G to 4G and set newrac to -1");
                                }
                                boolean isDisconnected = defaultApn.getState() == State.IDLE || defaultApn.getState() == State.FAILED;
                                HwDcTrackerBaseReference.this.log("CLEARCODE onCellLocationChanged try setup data again");
                                DcTrackerUtils.cleanUpConnection(dcTracker, isDisconnected ^ 1, defaultApn);
                                HwDcTrackerBaseReference.this.setupDataOnConnectableApns("cellLocationChanged", null);
                                HwDcTrackerBaseReference.this.resetTryTimes();
                            }
                            return;
                        }
                    }
                    HwDcTrackerBaseReference.this.log("CLEARCODE location not instanceof GsmCellLocation");
                } catch (Exception e) {
                    Rlog.e(HwDcTrackerBaseReference.TAG, "Exception in CellStateHandler.handleMessage:", e);
                }
            }
        }
    };
    private boolean removePreferredApn = true;

    class AllowMmmsContentObserver extends ContentObserver {
        public AllowMmmsContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            int allowMms = System.getInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), HwDcTrackerBaseReference.ENABLE_ALLOW_MMS, 0);
            HwDcTrackerBaseReference hwDcTrackerBaseReference = HwDcTrackerBaseReference.this;
            if (allowMms != 1) {
                z = false;
            }
            hwDcTrackerBaseReference.ALLOW_MMS = z;
        }
    }

    private class FdnAsyncQueryHandler extends AsyncQueryHandler {
        public FdnAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            long subId = (long) HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getSubId();
            boolean isFdnActivated1 = SystemProperties.getBoolean("gsm.hw.fdn.activated1", false);
            boolean isFdnActivated2 = SystemProperties.getBoolean("gsm.hw.fdn.activated2", false);
            HwDcTrackerBaseReference.this.log("fddn onQueryComplete subId:" + subId + " ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2);
            if ((subId == 0 && isFdnActivated1) || (subId == 1 && isFdnActivated2)) {
                HwDcTrackerBaseReference.this.retryDataConnectionByFdn();
            }
        }
    }

    private class FdnChangeObserver extends ContentObserver {
        public FdnChangeObserver() {
            super(HwDcTrackerBaseReference.this.mDcTrackerBase);
        }

        public void onChange(boolean selfChange) {
            HwDcTrackerBaseReference.this.log("fddn FdnChangeObserver onChange, selfChange:" + selfChange);
            HwDcTrackerBaseReference.this.asyncQueryContact();
        }
    }

    class NwModeContentObserver extends ContentObserver {
        public NwModeContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean Change) {
            if (HwDcTrackerBaseReference.isMultiSimEnabled) {
                if (TelephonyManager.getTelephonyProperty(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getPhoneId(), "gsm.data.gsm_only_not_allow_ps", "false").equals("false")) {
                    return;
                }
            } else if (!SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", false)) {
                return;
            }
            HwDcTrackerBaseReference.this.nwMode = Global.getInt(HwDcTrackerBaseReference.this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", Phone.PREFERRED_NT_MODE);
            HwDcTrackerBaseReference.this.log("NwModeChangeObserver onChange nwMode = " + HwDcTrackerBaseReference.this.nwMode);
            if (HwDcTrackerBaseReference.this.mDcTrackerBase instanceof DcTracker) {
                DcTracker dcTracker = HwDcTrackerBaseReference.this.mDcTrackerBase;
                if (1 == HwDcTrackerBaseReference.this.nwMode) {
                    DcTrackerUtils.cleanUpAllConnections(dcTracker, true, "nwTypeChanged");
                } else if (1 == HwDcTrackerBaseReference.this.mNwOldMode) {
                    DcTrackerUtils.onTrySetupData(dcTracker, "nwTypeChanged");
                }
            }
            HwDcTrackerBaseReference.this.mNwOldMode = HwDcTrackerBaseReference.this.nwMode;
        }
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues;
        }
        int[] iArr = new int[AppType.values().length];
        try {
            iArr[AppType.APPTYPE_CSIM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppType.APPTYPE_ISIM.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppType.APPTYPE_RUIM.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppType.APPTYPE_SIM.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean equals;
        boolean z = true;
        if ("07".equals(SystemProperties.get("ro.config.hw_opta"))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb"));
        } else {
            equals = false;
        }
        IS_ATT = equals;
        if (((MMS_PROP >> 1) & 1) == 1) {
            equals = true;
        } else {
            equals = false;
        }
        MMSIgnoreDSSwitchOnRoaming = equals;
        if (((MMS_PROP >> 2) & 1) != 1) {
            z = false;
        }
        MMSIgnoreDSSwitchNotRoaming = z;
    }

    public HwDcTrackerBaseReference(DcTracker dcTrackerBase) {
        this.mDcTrackerBase = dcTrackerBase;
    }

    public void init() {
        boolean z = false;
        if (System.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), ENABLE_ALLOW_MMS, 0) == 1) {
            z = true;
        }
        this.ALLOW_MMS = z;
        Uri allowMmsUri = System.CONTENT_URI;
        this.allowMmsObserver = new AllowMmmsContentObserver(this.mDcTrackerBase);
        this.mDcTrackerBase.mPhone.getContext().getContentResolver().registerContentObserver(allowMmsUri, true, this.allowMmsObserver);
        this.nwModeChangeObserver = new NwModeContentObserver(this.mDcTrackerBase);
        this.mDcTrackerBase.mPhone.getContext().getContentResolver().registerContentObserver(Global.getUriFor("preferred_network_mode"), true, this.nwModeChangeObserver);
        Phone phone = this.mDcTrackerBase.mPhone;
        this.nwMode = Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", Phone.PREFERRED_NT_MODE);
        this.mNwOldMode = this.nwMode;
        if (this.mDcTrackerBase.mPhone.getCallTracker() != null) {
            this.mDcTrackerBase.mPhone.getCallTracker().registerForVoiceCallEnded(this.handler, 3, null);
        }
        this.mSubscription = Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mAlarmManager = (AlarmManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("alarm");
        filter.addAction(INTENT_SET_PREF_NETWORK_TYPE);
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN)) {
            this.mDcTrackerBase.mPhone.mCi.registerForLimitPDPAct(this.handler, 4, null);
            filter.addAction(INTENT_LIMIT_PDP_ACT_IND);
        }
        this.mDcTrackerBase.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mDcTrackerBase.mPhone);
        isSupportPidStatistics();
    }

    public void dispose() {
        if (this.allowMmsObserver != null) {
            this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.allowMmsObserver);
        }
        if (this.nwModeChangeObserver != null) {
            this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.nwModeChangeObserver);
        }
        if (this.mDcTrackerBase.mPhone.getCallTracker() != null) {
            this.mDcTrackerBase.mPhone.getCallTracker().unregisterForVoiceCallEnded(this.handler);
        }
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN)) {
            this.mDcTrackerBase.mPhone.mCi.unregisterForLimitPDPAct(this.handler);
        }
        if (this.mIntentReceiver != null) {
            this.mDcTrackerBase.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    private void onResetApn() {
        ApnContext apnContext = (ApnContext) this.mDcTrackerBase.mApnContexts.get("default");
        if (apnContext != null) {
            apnContext.setEnabled(true);
            apnContext.setDependencyMet(true);
        }
    }

    public void beforeHandleMessage(Message msg) {
        if (200 == msg.what) {
            onResetApn();
        }
    }

    public boolean isDataAllowedByApnContext(ApnContext apnContext) {
        if (isGsmOnlyPsNotAllowed()) {
            log("in GsmMode not allowed PS!");
            return false;
        } else if (isLimitPDPAct()) {
            log("PSCLEARCODE Limit PDP Act apnContext: " + apnContext);
            return false;
        } else {
            boolean isMMS = "mms".equals(apnContext.getApnType());
            boolean isXcap = "xcap".equals(apnContext.getApnType());
            boolean isUserEnable = isNeedForceSetup(apnContext);
            log("isDataAllowedByApnType: isMms = " + isMMS + " isXcap = " + isXcap + " isUserEnable = " + isUserEnable);
            DataAllowFailReason failureReason = new DataAllowFailReason();
            boolean dataAllowed = this.mDcTrackerBase.isDataAllowed(failureReason, isMMS, isUserEnable);
            if (isXcap && getXcapDataRoamingEnable() && failureReason.isFailForSingleReason(DataAllowFailReasonType.ROAMING_DISABLED)) {
                return true;
            }
            return dataAllowed;
        }
    }

    public boolean isLimitPDPAct() {
        return this.mIsLimitPDPAct ? isPSClearCodeRplmnMatched() : false;
    }

    public boolean isPSClearCodeRplmnMatched() {
        if (!HuaweiTelephonyConfigs.isHisiPlatform() || this.mDcTrackerBase == null || this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.getServiceState() == null) {
            return false;
        }
        String operator = this.mDcTrackerBase.mPhone.getServiceState().getOperatorNumeric();
        if (TextUtils.isEmpty(PS_CLEARCODE_PLMN) || (TextUtils.isEmpty(operator) ^ 1) == 0) {
            return false;
        }
        return PS_CLEARCODE_PLMN.contains(operator);
    }

    private boolean isNeedForceSetup(ApnContext apnContext) {
        boolean isUserEnable = "dataEnabled".equals(apnContext.getReason());
        boolean isCSInService = this.mDcTrackerBase.mPhone.getServiceState().getVoiceRegState() == 0;
        if (isUserEnable && isCSInService) {
            return USER_FORCE_DATA_SETUP;
        }
        return false;
    }

    public boolean isDataAllowedByApnType(DataAllowFailReason failureReason, String apnType) {
        if (isGsmOnlyPsNotAllowed()) {
            log("in GsmMode not allowed PS!");
            return false;
        }
        boolean isMms = "mms".equals(apnType);
        boolean isXcap = "xcap".equals(apnType);
        DataAllowFailReason failReason = new DataAllowFailReason();
        boolean dataAllowed = this.mDcTrackerBase.isDataAllowed(failReason, isMms);
        if (isXcap && getXcapDataRoamingEnable() && failReason.isFailForSingleReason(DataAllowFailReasonType.ROAMING_DISABLED)) {
            return true;
        }
        return dataAllowed;
    }

    private boolean isGsmOnlyPsNotAllowed() {
        boolean z = true;
        boolean z2 = false;
        if (isMultiSimEnabled) {
            int subId = this.mDcTrackerBase.mPhone.getPhoneId();
            int networkMode = this.nwMode;
            if (IS_DUAL_4G_SUPPORTED && SIM_NUM > 1) {
                ContentResolver contentResolver = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
                String str = "preferred_network_mode" + subId;
                Phone phone = this.mDcTrackerBase.mPhone;
                networkMode = Global.getInt(contentResolver, str, Phone.PREFERRED_NT_MODE);
            }
            if (!(TelephonyManager.getTelephonyProperty(subId, "gsm.data.gsm_only_not_allow_ps", "false").equals("true") && 1 == networkMode)) {
                z = false;
            }
            return z;
        }
        if (SystemProperties.getBoolean("gsm.data.gsm_only_not_allow_ps", false) && 1 == this.nwMode) {
            z2 = true;
        }
        return z2;
    }

    public boolean isDataAllowedForRoaming(boolean isMms) {
        if (!this.mDcTrackerBase.mPhone.getServiceState().getRoaming() || this.mDcTrackerBase.getDataRoamingEnabled()) {
            return true;
        }
        if (this.ALLOW_MMS || MMS_ON_ROAMING) {
            return isMms;
        }
        return false;
    }

    public void onAllApnFirstActiveFailed() {
        if (isMultiSimEnabled) {
            ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getPhoneId()).allApnActiveFailed();
            return;
        }
        ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext()).allApnActiveFailed();
    }

    public void onAllApnPermActiveFailed() {
        if (this.broadcastPrePostPay && GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            log("tryToActionPrePostPay.");
            GlobalParamsAdaptor.tryToActionPrePostPay();
            this.broadcastPrePostPay = false;
        }
        ApnReminder.getInstance(this.mDcTrackerBase.mPhone.getContext()).getCust().handleAllApnPermActiveFailed(this.mDcTrackerBase.mPhone.getContext());
    }

    public boolean isBipApnType(String type) {
        if (HuaweiTelephonyConfigs.isModemBipEnable() || (!type.equals("bip0") && !type.equals("bip1") && !type.equals("bip2") && !type.equals("bip3") && !type.equals("bip4") && !type.equals("bip5") && !type.equals("bip6"))) {
            return false;
        }
        return true;
    }

    public ApnSetting fetchBipApn(ApnSetting preferredApn, ArrayList<ApnSetting> allApnSettings) {
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            ApnSetting mDataProfile = ApnSetting.fromString(SystemProperties.get("gsm.bip.apn"));
            if ("default".equals(SystemProperties.get("gsm.bip.apn"))) {
                if (preferredApn != null) {
                    log("find prefer apn, use this");
                    return preferredApn;
                }
                if (allApnSettings != null) {
                    int list_size = allApnSettings.size();
                    for (int i = 0; i < list_size; i++) {
                        ApnSetting apn = (ApnSetting) allApnSettings.get(i);
                        if (apn.canHandleType("default")) {
                            log("find the first default apn");
                            return apn;
                        }
                    }
                }
                log("find non apn for default bip");
                return null;
            } else if (mDataProfile != null) {
                log("fetchBipApn: global BIP mDataProfile=" + mDataProfile);
                return mDataProfile;
            }
        }
        return null;
    }

    private void log(String string) {
        Rlog.d(TAG, string);
    }

    public void setFirstTimeEnableData() {
        log("=PREPOSTPAY=, Data Setup Successful.");
        if (this.broadcastPrePostPay) {
            this.broadcastPrePostPay = false;
        }
    }

    public boolean needRemovedPreferredApn() {
        if (!this.removePreferredApn || !GlobalParamsAdaptor.getPrePostPayPreCondition()) {
            return false;
        }
        log("Remove preferred apn.");
        this.removePreferredApn = false;
        return true;
    }

    public String getDataRoamingSettingItem(String originItem) {
        if (isMultiSimEnabled && this.mDcTrackerBase.mPhone.getPhoneId() == 1) {
            return DATA_ROAMING_SIM2;
        }
        return originItem;
    }

    public void disableGoogleDunApn(Context c, String apnData, ApnSetting dunSetting) {
        if (SystemProperties.getBoolean("ro.config.enable.gdun", false)) {
            dunSetting = ApnSetting.fromString("this is false");
        }
    }

    public boolean getAnyDataEnabledByApnContext(ApnContext apnContext, boolean enable) {
        boolean z = true;
        if (!this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
            if (!((this.ALLOW_MMS || MMSIgnoreDSSwitchNotRoaming) && "mms".equals(apnContext.getApnType()))) {
                z = enable;
            }
            return z;
        } else if (getXcapDataRoamingEnable() && "xcap".equals(apnContext.getApnType())) {
            return true;
        } else {
            if ((this.ALLOW_MMS || MMSIgnoreDSSwitchOnRoaming) && "mms".equals(apnContext.getApnType())) {
                enable = true;
            }
            return enable;
        }
    }

    public boolean shouldDisableMultiPdps(boolean onlySingleDcAllowed) {
        if (!(this.SUPPORT_MPDN || (SystemProperties.getBoolean("gsm.multipdp.plmn.matched", false) ^ 1) == 0)) {
            onlySingleDcAllowed = true;
            log("SUPPORT_MPDN: " + this.SUPPORT_MPDN);
        }
        if (isMultiSimEnabled) {
            int subId = this.mDcTrackerBase.mPhone.getPhoneId();
            if (subId == 0) {
                SystemProperties.set("gsm.check_is_single_pdp_sub1", Boolean.toString(onlySingleDcAllowed));
            } else if (subId == 1) {
                SystemProperties.set("gsm.check_is_single_pdp_sub2", Boolean.toString(onlySingleDcAllowed));
            }
        } else {
            SystemProperties.set("gsm.check_is_single_pdp", Boolean.toString(onlySingleDcAllowed));
        }
        return onlySingleDcAllowed;
    }

    public void setMPDN(boolean bMPDN) {
        if (bMPDN == this.SUPPORT_MPDN) {
            log("MPDN is same,Don't need change");
            return;
        }
        if (bMPDN) {
            int radioTech = this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology();
            if (ServiceState.isCdma(radioTech) && radioTech != 13) {
                log("technology is not EHRPD and ServiceState is CDMA,Can't set MPDN");
                return;
            }
        }
        this.SUPPORT_MPDN = bMPDN;
        log("SUPPORT_MPDN change to " + bMPDN);
    }

    public void setMPDNByNetWork(String plmnNetWork) {
        if (this.mDcTrackerBase.mPhone == null) {
            log("mPhone is null");
            return;
        }
        String plmnsConfig = System.getString(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "mpdn_plmn_matched_by_network");
        if (TextUtils.isEmpty(plmnsConfig)) {
            log("plmnConfig is Empty");
            return;
        }
        boolean bMPDN = false;
        for (String plmn : plmnsConfig.split(",")) {
            if (!TextUtils.isEmpty(plmn) && plmn.equals(plmnNetWork)) {
                bMPDN = true;
                break;
            }
        }
        setMPDN(bMPDN);
        log("setMpdnByNewNetwork done, bMPDN is " + bMPDN);
    }

    public String getCTOperatorNumeric(String operator) {
        String result = operator;
        if (!HuaweiTelephonyConfigs.isChinaTelecom() || this.mDcTrackerBase.mPhone.getPhoneId() != 0) {
            return result;
        }
        result = CT_CDMA_OPERATOR;
        log("getCTOperatorNumeric: use china telecom operator=" + result);
        return result;
    }

    public ApnSetting makeHwApnSetting(Cursor cursor, String[] types) {
        return new HwApnSetting(cursor, types);
    }

    public boolean noNeedDoRecovery(ConcurrentHashMap mApnContexts) {
        if (SystemProperties.getBoolean("persist.radio.hw.nodorecovery", false)) {
            return true;
        }
        if (SystemProperties.getBoolean("hw.ds.np.nopollstat", true)) {
            return isActiveDefaultApnPreset(mApnContexts) ^ 1;
        }
        return false;
    }

    public boolean isActiveDefaultApnPreset(ConcurrentHashMap<String, ApnContext> mApnContexts) {
        ApnContext apnContext = (ApnContext) mApnContexts.get("default");
        if (apnContext != null && State.CONNECTED == apnContext.getState()) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null && (apnSetting instanceof HwApnSetting)) {
                HwApnSetting hwapnSetting = (HwApnSetting) apnSetting;
                log("current default apn is " + (hwapnSetting.isPreset() ? "preset" : "non-preset"));
                return hwapnSetting.isPreset();
            }
        }
        return true;
    }

    public boolean isApnPreset(ApnSetting apnSetting) {
        if (apnSetting == null || !(apnSetting instanceof HwApnSetting)) {
            return true;
        }
        return ((HwApnSetting) apnSetting).isPreset();
    }

    public void getNetdPid() {
        int ret = -1;
        if (this.mDoRecoveryAddDnsProp) {
            try {
                this.mNetworkManager = Stub.asInterface(ServiceManager.getService("network_management"));
                if (this.mNetworkManager != null) {
                    ret = this.mNetworkManager.getNetdPid();
                } else {
                    log("getNetdPid mNetdService is null");
                }
            } catch (RemoteException e) {
                log("getNetdPid mNetdService RemoteException");
            }
            this.netdPid = ret;
        }
        log("getNetdPid:" + ret + ",prop:" + this.mDoRecoveryAddDnsProp);
    }

    public void isSupportPidStatistics() {
        if (!this.mDoRecoveryAddDnsProp) {
            return;
        }
        if (new File(pidStatsPath).exists()) {
            this.isSupportPidStats = true;
        } else {
            this.isSupportPidStats = false;
        }
    }

    private long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x00f6 A:{SYNTHETIC, Splitter: B:40:0x00f6} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00fb A:{Catch:{ IOException -> 0x00ff }} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0104 A:{SYNTHETIC, Splitter: B:47:0x0104} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0109 A:{Catch:{ IOException -> 0x0146 }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00f6 A:{SYNTHETIC, Splitter: B:40:0x00f6} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00fb A:{Catch:{ IOException -> 0x00ff }} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0104 A:{SYNTHETIC, Splitter: B:47:0x0104} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0109 A:{Catch:{ IOException -> 0x0146 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long[] getDnsPacketTxRxSum() {
        Throwable th;
        long[] ret = new long[]{0, 0};
        BufferedReader bReader = null;
        FileInputStream fis = null;
        if (!this.isSupportPidStats || this.netdPid == -1) {
            log("isSupportPidStats=" + this.isSupportPidStats + ",netdPid=" + this.netdPid);
        } else {
            try {
                FileInputStream fis2 = new FileInputStream(pidStatsPath);
                try {
                    BufferedReader bReader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
                    long udpTx = 0;
                    long udpRx = 0;
                    try {
                        String netdPidKey = ":" + String.valueOf(this.netdPid) + "_";
                        String[] allMobiles = TrafficStats.getMobileIfaces();
                        while (true) {
                            String line = bReader2.readLine();
                            if (line == null) {
                                break;
                            }
                            String[] tokens = line.split(" ");
                            if (tokens.length > 20 && tokens[3].equals(NETD_PROCESS_UID)) {
                                if (tokens[2].equals("netd") || tokens[2].contains(netdPidKey)) {
                                    for (String iface : allMobiles) {
                                        if (tokens[1].equals(iface)) {
                                            udpTx += parseLong(tokens[20]);
                                            udpRx += parseLong(tokens[14]);
                                        }
                                    }
                                }
                            }
                        }
                        ret[0] = ret[0] + udpTx;
                        ret[1] = ret[1] + udpRx;
                        if (bReader2 != null) {
                            try {
                                bReader2.close();
                            } catch (IOException e) {
                            }
                        }
                        if (fis2 != null) {
                            fis2.close();
                        }
                        bReader = bReader2;
                    } catch (IOException e2) {
                        fis = fis2;
                        bReader = bReader2;
                        try {
                            log("pidStatsPath not found");
                            if (bReader != null) {
                            }
                            if (fis != null) {
                            }
                            return ret;
                        } catch (Throwable th2) {
                            th = th2;
                            if (bReader != null) {
                                try {
                                    bReader.close();
                                } catch (IOException e3) {
                                    throw th;
                                }
                            }
                            if (fis != null) {
                                fis.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fis = fis2;
                        bReader = bReader2;
                        if (bReader != null) {
                        }
                        if (fis != null) {
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    fis = fis2;
                    log("pidStatsPath not found");
                    if (bReader != null) {
                    }
                    if (fis != null) {
                    }
                    return ret;
                } catch (Throwable th4) {
                    th = th4;
                    fis = fis2;
                    if (bReader != null) {
                    }
                    if (fis != null) {
                    }
                    throw th;
                }
            } catch (IOException e5) {
                log("pidStatsPath not found");
                if (bReader != null) {
                    try {
                        bReader.close();
                    } catch (IOException e6) {
                    }
                }
                if (fis != null) {
                    fis.close();
                }
                return ret;
            }
        }
        return ret;
    }

    public HwCustDcTracker getCust(DcTracker dcTracker) {
        return (HwCustDcTracker) HwCustUtils.createObj(HwCustDcTracker.class, new Object[]{dcTracker});
    }

    public void setupDataOnConnectableApns(String reason, String excludedApnType) {
        log("setupDataOnConnectableApns: " + reason + ", excludedApnType = " + excludedApnType);
        for (ApnContext apnContext : this.mDcTrackerBase.mPrioritySortedApnContexts) {
            if (TextUtils.isEmpty(excludedApnType) || !excludedApnType.equals(apnContext.getApnType())) {
                log("setupDataOnConnectableApns: apnContext " + apnContext);
                if (apnContext.getState() == State.FAILED) {
                    apnContext.setState(State.IDLE);
                }
                if (apnContext.isConnectable()) {
                    log("setupDataOnConnectableApns: isConnectable() call trySetupData");
                    apnContext.setReason(reason);
                    this.mDcTrackerBase.onTrySetupData(apnContext);
                }
            }
        }
    }

    public boolean needRetryAfterDisconnected(DcFailCause cause) {
        String failCauseStr = "";
        if (DcFailCause.ERROR_UNSPECIFIED != cause) {
            return true;
        }
        failCauseStr = SystemProperties.get("ril.ps_ce_reason", "");
        if (TextUtils.isEmpty(failCauseStr)) {
            return true;
        }
        for (String noRetryCause : CAUSE_NO_RETRY_AFTER_DISCONNECT.split(",")) {
            if (failCauseStr.equals(noRetryCause)) {
                return false;
            }
        }
        return true;
    }

    public void setRetryAfterDisconnectedReason(DataConnection dc, ArrayList<ApnContext> apnsToCleanup) {
        for (ApnContext apnContext : dc.mApnContexts.keySet()) {
            apnContext.setReason("noRetryAfterDisconnect");
        }
        apnsToCleanup.addAll(dc.mApnContexts.keySet());
    }

    public boolean isChinaTelecom(int slotId) {
        return HwTelephonyManagerInner.getDefault().isChinaTelecom(slotId);
    }

    public boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public boolean isCTDualModeCard(int sub) {
        int SubType = HwTelephonyManagerInner.getDefault().getCardType(sub);
        if (41 != SubType && 43 != SubType) {
            return false;
        }
        log("sub = " + sub + ", SubType = " + SubType + " is CT dual modem card");
        return true;
    }

    public boolean isPingOk() {
        boolean ret = false;
        if (HwVSimUtils.isVSimOn()) {
            debugLog("isPineOk always ok for vsim on");
            return true;
        }
        try {
            String pingBeforeDorecovery = SystemProperties.get("ro.sys.ping_bf_dorecovery", "false");
            debugLog("isPingOk pingBeforeDorecovery = " + pingBeforeDorecovery);
            String operatorNumeric = ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).getNetworkOperatorForPhone(this.mDcTrackerBase.mPhone.getSubId());
            Object mcc = null;
            if (operatorNumeric != null && operatorNumeric.length() > 3) {
                mcc = operatorNumeric.substring(0, 3);
                debugLog("isPingOk mcc = " + mcc);
            }
            if (pingBeforeDorecovery.equals("true") || CHINA_OPERATOR_MCC.equals(mcc)) {
                Thread pingThread = new Thread(new Runnable() {
                    public void run() {
                        String result = "";
                        String serverName = "www.baidu.com";
                        HwDcTrackerBaseReference.this.debugLog("ping thread enter, server name = " + serverName);
                        try {
                            HwDcTrackerBaseReference.this.pingThreadlLock.lock();
                            HwDcTrackerBaseReference.this.isRecievedPingReply = false;
                            HwDcTrackerBaseReference.this.pingThreadlLock.unlock();
                            HwDcTrackerBaseReference.this.debugLog("pingThread begin to ping");
                            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -W 1 " + serverName);
                            int status = process.waitFor();
                            HwDcTrackerBaseReference.this.debugLog("pingThread, process.waitFor, status = " + status);
                            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            StringBuffer stringBuffer = new StringBuffer();
                            String str = "";
                            while (true) {
                                str = buf.readLine();
                                if (str == null) {
                                    break;
                                }
                                stringBuffer.append(str);
                                stringBuffer.append("\r\n");
                            }
                            String str2 = stringBuffer.toString();
                            buf.close();
                            HwDcTrackerBaseReference.this.debugLog("ping result:" + str2);
                            HwDcTrackerBaseReference.this.debugLog("pingThread pingThreadlLock.lock");
                            HwDcTrackerBaseReference.this.pingThreadlLock.lock();
                            if (status != 0 || str2.indexOf("1 packets transmitted, 1 received") < 0) {
                                HwDcTrackerBaseReference.this.isRecievedPingReply = false;
                            } else {
                                HwDcTrackerBaseReference.this.isRecievedPingReply = true;
                            }
                            HwDcTrackerBaseReference.this.pingCondition.signal();
                            HwDcTrackerBaseReference.this.debugLog("pingThread pingThreadlLock.unlock, ping thread return " + HwDcTrackerBaseReference.this.isRecievedPingReply);
                            HwDcTrackerBaseReference.this.pingThreadlLock.unlock();
                        } catch (Exception e) {
                            Rlog.e(HwDcTrackerBaseReference.TAG, "ping thread Exception: ", e);
                        }
                    }
                }, "ping thread");
                debugLog("isPingOk pingThreadlLock.lock");
                this.pingThreadlLock.lock();
                pingThread.start();
                this.pingCondition.await(1000, TimeUnit.MILLISECONDS);
                ret = this.isRecievedPingReply;
                this.pingThreadlLock.unlock();
                debugLog("isPingOk pingThreadlLock.unlock");
            }
        } catch (Exception e) {
            Rlog.e(TAG, "isPingOk Exception: ", e);
        }
        return ret;
    }

    public boolean isClearCodeEnabled() {
        return this.mIsClearCodeEnabled;
    }

    public void startListenCellLocationChange() {
        ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).listen(this.pslForCellLocation, 16);
    }

    public void stopListenCellLocationChange() {
        ((TelephonyManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("phone")).listen(this.pslForCellLocation, 0);
    }

    public void operateClearCodeProcess(ApnContext apnContext, DcFailCause cause, int delay) {
        this.mDelayTime = delay;
        if (cause.isPermanentFailure(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getSubId())) {
            log("CLEARCODE isPermanentFailure,perhaps APN is wrong");
            boolean isClearcodeDcFailCause = cause == DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED || cause == DcFailCause.USER_AUTHENTICATION;
            if ("default".equals(apnContext.getApnType()) && isClearcodeDcFailCause) {
                this.mTryIndex++;
                log("CLEARCODE mTryIndex increase,current mTryIndex = " + this.mTryIndex);
                if (this.mTryIndex >= 3) {
                    if (isLteRadioTech()) {
                        this.mDcTrackerBase.mPhone.setPreferredNetworkType(3, null);
                        Global.putInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", 3);
                        this.mGsmServiceStateTracker = this.mDcTrackerBase.mPhone.getServiceStateTracker();
                        HwServiceStateManager.getHwGsmServiceStateManager(this.mGsmServiceStateTracker, (GsmCdmaPhone) this.mDcTrackerBase.mPhone).setRac(-1);
                        log("CLEARCODE mTryIndex >= 3 and is LTE,switch 4G to 3G and set newrac to -1");
                    } else {
                        log("CLEARCODE mTryIndex >= 3 and is 3G,show clearcode dialog");
                        if (this.mPSClearCodeDialog == null) {
                            this.mPSClearCodeDialog = createPSClearCodeDiag(cause);
                            if (this.mPSClearCodeDialog != null) {
                                this.mPSClearCodeDialog.show();
                            }
                        }
                        set2HourDelay();
                    }
                    this.mTryIndex = 0;
                    apnContext.markApnPermanentFailed(apnContext.getApnSetting());
                }
            } else {
                this.mTryIndex = 0;
                apnContext.markApnPermanentFailed(apnContext.getApnSetting());
            }
            return;
        }
        this.mTryIndex = 0;
        log("CLEARCODE not isPermanentFailure ");
    }

    public void resetTryTimes() {
        if (isClearCodeEnabled()) {
            this.mTryIndex = 0;
            if (this.mAlarmManager != null && this.mAlarmIntent != null) {
                this.mAlarmManager.cancel(this.mAlarmIntent);
                log("CLEARCODE cancel Alarm resetTryTimes");
            }
        }
    }

    private boolean isLteRadioTech() {
        if (this.mDcTrackerBase.mPhone.getServiceState().getRilDataRadioTechnology() == 14) {
            return true;
        }
        return false;
    }

    public void setCurFailCause(AsyncResult ar) {
        if (!isClearCodeEnabled()) {
            return;
        }
        if (ar.result instanceof DcFailCause) {
            this.mCurFailCause = (DcFailCause) ar.result;
        } else {
            this.mCurFailCause = null;
        }
    }

    private AlertDialog createPSClearCodeDiag(DcFailCause cause) {
        Builder buider = new Builder(this.mDcTrackerBase.mPhone.getContext(), this.mDcTrackerBase.mPhone.getContext().getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        if (cause == DcFailCause.USER_AUTHENTICATION) {
            buider.setMessage(33685827);
            log("CLEARCODE clear_code_29");
        } else if (cause != DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED) {
            return null;
        } else {
            buider.setMessage(33685828);
            log("CLEARCODE clear_code_33");
        }
        buider.setIcon(17301543);
        buider.setCancelable(false);
        buider.setPositiveButton("Aceptar", new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwDcTrackerBaseReference.this.mPSClearCodeDialog = null;
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    private void set2HourDelay() {
        int delayTime = SystemProperties.getInt("gsm.radio.debug.cause_delay", DELAY_2_HOUR);
        log("CLEARCODE dataRadioTech is 3G and mTryIndex >= 3,so set2HourDelay delayTime =" + delayTime);
        Intent intent = new Intent(INTENT_SET_PREF_NETWORK_TYPE);
        intent.putExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, 9);
        this.mAlarmIntent = PendingIntent.getBroadcast(this.mDcTrackerBase.mPhone.getContext(), 0, intent, 134217728);
        if (this.mAlarmManager != null) {
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delayTime), this.mAlarmIntent);
        }
    }

    public int getDelayTime() {
        if (this.mCurFailCause == DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED || this.mCurFailCause == DcFailCause.USER_AUTHENTICATION) {
            if (isLteRadioTech()) {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_4G;
            } else {
                this.mDelayTime = PS_CLEARCODE_APN_DELAY_DEFAULT_MILLIS_NOT_4G;
            }
        }
        return this.mDelayTime;
    }

    protected void onActionIntentSetNetworkType(Intent intent) {
        int networkType = intent.getIntExtra(INTENT_SET_PREF_NETWORK_TYPE_EXTRA_TYPE, 9);
        int curPrefMode = Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", networkType);
        log("CLEARCODE switch network type : " + networkType + " curPrefMode = " + curPrefMode);
        if (!(networkType == curPrefMode || curPrefMode == 2)) {
            this.mDcTrackerBase.mPhone.setPreferredNetworkType(networkType, null);
            log("CLEARCODE switch network type to 4G and set newRac to -1");
            Global.putInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "preferred_network_mode", networkType);
            this.mGsmServiceStateTracker = this.mDcTrackerBase.mPhone.getServiceStateTracker();
            HwServiceStateManager.getHwGsmServiceStateManager(this.mGsmServiceStateTracker, (GsmCdmaPhone) this.mDcTrackerBase.mPhone).setRac(-1);
        }
        ApnContext defaultApn = (ApnContext) this.mDcTrackerBase.mApnContexts.get("default");
        boolean isDisconnected = defaultApn.getState() == State.IDLE || defaultApn.getState() == State.FAILED;
        log("CLEARCODE 2 hours of delay is over,try setup data");
        DcTrackerUtils.cleanUpConnection(this.mDcTrackerBase, isDisconnected ^ 1, defaultApn);
        setupDataOnConnectableApns(CLEARCODE_2HOUR_DELAY_OVER, null);
    }

    public void unregisterForImsiReady(IccRecords r) {
        r.unregisterForImsiReady(this.mDcTrackerBase);
    }

    public void registerForImsiReady(IccRecords r) {
        r.registerForImsiReady(this.mDcTrackerBase, 270338, null);
    }

    public void unregisterForRecordsLoaded(IccRecords r) {
        r.unregisterForRecordsLoaded(this.mDcTrackerBase);
    }

    public void registerForRecordsLoaded(IccRecords r) {
        r.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
    }

    public void registerForGetAdDone(UiccCardApplication newUiccApplication) {
        newUiccApplication.registerForGetAdDone(this.mDcTrackerBase, 270338, null);
    }

    public void unregisterForGetAdDone(UiccCardApplication newUiccApplication) {
        newUiccApplication.unregisterForGetAdDone(this.mDcTrackerBase);
    }

    public void registerForImsi(UiccCardApplication newUiccApplication, IccRecords newIccRecords) {
        if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN) || this.SETAPN_UNTIL_CARDLOADED) {
            newIccRecords.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
            return;
        }
        switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues()[newUiccApplication.getType().ordinal()]) {
            case 1:
            case 2:
                log("New CSIM records found");
                newIccRecords.registerForImsiReady(this.mDcTrackerBase, 271144, null);
                break;
            case 3:
            case 4:
                log("New USIM records found");
                newUiccApplication.registerForGetAdDone(this.mDcTrackerBase, 271144, null);
                break;
            default:
                log("New other records found");
                break;
        }
        newIccRecords.registerForRecordsLoaded(this.mDcTrackerBase, 270338, null);
    }

    public boolean checkMvnoParams() {
        boolean result = false;
        String operator = this.mDcTrackerBase.getCTOperator(this.mDcTrackerBase.getOperatorNumeric());
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId()))) {
                operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mDcTrackerBase.mPhone.getSubId()));
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            operator = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            log("checkMvnoParams: selection=" + selection);
            Cursor cursor = this.mDcTrackerBase.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    result = checkMvno(cursor);
                }
                cursor.close();
            }
        }
        log("checkMvnoParams: X result = " + result);
        return result;
    }

    private boolean checkMvno(Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                String mvnoType = cursor.getString(cursor.getColumnIndexOrThrow("mvno_type"));
                String mvnoMatchData = cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data"));
                if (!TextUtils.isEmpty(mvnoType) && (TextUtils.isEmpty(mvnoMatchData) ^ 1) != 0) {
                    log("checkMvno: X has mvno paras");
                    return true;
                }
            } while (cursor.moveToNext());
        }
        return false;
    }

    public void registerForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.registerForFdnRecordsLoaded(this.mDcTrackerBase, 2, null);
        }
    }

    public void unregisterForFdnRecordsLoaded(IccRecords r) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            r.unregisterForFdnRecordsLoaded(this.mDcTrackerBase);
        }
    }

    public void registerForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("registerForFdn");
            this.mUiccController.registerForFdnStatusChange(this.mDcTrackerBase, 1, null);
            this.mFdnChangeObserver = new FdnChangeObserver();
            ContentResolver cr = this.mDcTrackerBase.mPhone.getContext().getContentResolver();
            cr.registerContentObserver(FDN_URL, true, this.mFdnChangeObserver);
            this.mFdnAsyncQuery = new FdnAsyncQueryHandler(cr);
        }
    }

    public void unregisterForFdn() {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            log("unregisterForFdn");
            this.mUiccController.unregisterForFdnStatusChange(this.mDcTrackerBase);
            if (this.mFdnChangeObserver != null) {
                this.mDcTrackerBase.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mFdnChangeObserver);
            }
        }
    }

    public boolean isPsAllowedByFdn() {
        long curSubId = (long) this.mDcTrackerBase.mPhone.getSubId();
        String isFdnActivated1 = SystemProperties.get("gsm.hw.fdn.activated1", "false");
        String isFdnActivated2 = SystemProperties.get("gsm.hw.fdn.activated2", "false");
        String isPSAllowedByFdn1 = SystemProperties.get("gsm.hw.fdn.ps.flag.exists1", "false");
        String isPSAllowedByFdn2 = SystemProperties.get("gsm.hw.fdn.ps.flag.exists2", "false");
        log("fddn isPSAllowedByFdn ,isFdnActivated1:" + isFdnActivated1 + " ,isFdnActivated2:" + isFdnActivated2 + " ,isPSAllowedByFdn1:" + isPSAllowedByFdn1 + " ,isPSAllowedByFdn2:" + isPSAllowedByFdn2);
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            if (curSubId == 0 && "true".equals(isFdnActivated1) && "false".equals(isPSAllowedByFdn1)) {
                return false;
            }
            if (curSubId == 1 && "true".equals(isFdnActivated2) && "false".equals(isPSAllowedByFdn2)) {
                return false;
            }
        }
        return true;
    }

    public void handleCustMessage(Message msg) {
        switch (msg.what) {
            case 1:
            case 2:
                log("fddn msg.what = " + msg.what);
                retryDataConnectionByFdn();
                return;
            default:
                return;
        }
    }

    public void retryDataConnectionByFdn() {
        if (this.mDcTrackerBase.mPhone.getSubId() != SubscriptionController.getInstance().getCurrentDds()) {
            log("fddn retryDataConnectionByFdn, not dds sub, do nothing.");
            return;
        }
        if (isPsAllowedByFdn()) {
            log("fddn retryDataConnectionByFdn, FDN status change and PS is enable, try setup data.");
            setupDataOnConnectableApns("psRestrictDisabled", null);
        } else {
            log("fddn retryDataConnectionByFdn, PS restricted by FDN, cleaup all connections.");
            this.mDcTrackerBase.cleanUpAllConnections(true, "psRestrictEnabled");
        }
    }

    private void asyncQueryContact() {
        long subId = (long) this.mDcTrackerBase.mPhone.getSubId();
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mFdnAsyncQuery.startQuery(0, null, ContentUris.withAppendedId(FDN_URL, subId), new String[]{"number"}, null, null, null);
        }
    }

    public boolean isActiveDataSubscription() {
        log("isActiveDataSubscription getSubId= " + this.mDcTrackerBase.mPhone.getSubId() + "mCurrentDds" + SubscriptionController.getInstance().getCurrentDds());
        return this.mDcTrackerBase.mPhone.getSubId() == SubscriptionController.getInstance().getCurrentDds();
    }

    public int get4gSlot() {
        int slot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (slot == 0) {
        }
        return slot;
    }

    public int get2gSlot() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0 ? 1 : 0;
    }

    public void addIfacePhoneHashMap(DcAsyncChannel dcac, HashMap<String, Integer> mIfacePhoneHashMap) {
        LinkProperties tempLinkProperties = dcac.getLinkPropertiesSync();
        if (tempLinkProperties != null) {
            String iface = tempLinkProperties.getInterfaceName();
            if (iface != null) {
                mIfacePhoneHashMap.put(iface, Integer.valueOf(this.mDcTrackerBase.mPhone.getPhoneId()));
            }
        }
    }

    public int getVSimSubId() {
        return HwVSimManager.getDefault().getVSimSubId();
    }

    public void sendRoamingDataStatusChangBroadcast() {
        this.mDcTrackerBase.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.INTERNATIONAL_ROAMING_DATA_STATUS_CHANGED"));
    }

    public void sendDSMipErrorBroadcast() {
        if (SystemProperties.getBoolean("ro.config.hw_mip_error_dialog", false)) {
            this.mDcTrackerBase.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.DATA_CONNECTION_MOBILE_IP_ERROR"));
        }
    }

    public boolean enableTcpUdpSumForDataStall() {
        return SystemProperties.getBoolean("ro.hwpp_enable_tcp_udp_sum", false);
    }

    public String networkTypeToApnType(int networkType) {
        switch (networkType) {
            case 0:
                return "default";
            case 2:
                return "mms";
            case 3:
                return "supl";
            case 4:
                return "dun";
            case 5:
                return "hipri";
            case 10:
                return "fota";
            case 11:
                return "ims";
            case 12:
                return "cbs";
            case 14:
                return "ia";
            case 15:
                return "emergency";
            case 38:
                return "bip0";
            case 39:
                return "bip1";
            case 40:
                return "bip2";
            case 41:
                return "bip3";
            case 42:
                return "bip4";
            case 43:
                return "bip5";
            case HwVSimConstants.EVENT_SET_TEE_DATA_READY_DONE /*44*/:
                return "bip6";
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE /*45*/:
                return "xcap";
            case HwVSimConstants.EVENT_GET_PREFERRED_NETWORK_TYPE_DONE /*48*/:
                return "internaldefault";
            default:
                log("Error mapping networkType " + networkType + " to apnType");
                return "";
        }
    }

    public boolean isApnTypeDisabled(String apnType) {
        if (TextUtils.isEmpty(apnType)) {
            return false;
        }
        for (String type : "ro.hwpp.disabled_apn_type".split(",")) {
            if (apnType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNeedDataRoamingExpend() {
        if (this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.mIccRecords == null || this.mDcTrackerBase.mPhone.mIccRecords.get() == null) {
            log("mPhone or mIccRecords is null");
            return false;
        }
        String plmnsConfig = System.getString(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "hw_data_roam_option");
        if (TextUtils.isEmpty(plmnsConfig)) {
            log("plmnConfig is Empty");
            return false;
        } else if ("ALL".equals(plmnsConfig)) {
            return true;
        } else {
            String mccmnc = ((IccRecords) this.mDcTrackerBase.mPhone.mIccRecords.get()).getOperatorNumeric();
            for (String plmn : plmnsConfig.split(",")) {
                if (!TextUtils.isEmpty(plmn) && plmn.equals(mccmnc)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean setDataRoamingScope(int scope) {
        log("dram setDataRoamingScope scope " + scope);
        if (scope < 0 || scope > 2) {
            return false;
        }
        if (getDataRoamingScope() != scope) {
            Global.putInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), getDataRoamingSettingItem("data_roaming"), scope);
            if (this.mDcTrackerBase.mPhone.getServiceState() != null && this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
                log("dram setDataRoamingScope send EVENT_ROAMING_ON");
                this.mDcTrackerBase.sendMessage(this.mDcTrackerBase.obtainMessage(270347));
            }
        }
        return true;
    }

    public int getDataRoamingScope() {
        try {
            return Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), getDataRoamingSettingItem("data_roaming"));
        } catch (SettingNotFoundException e) {
            return -1;
        }
    }

    public boolean getDataRoamingEnabledWithNational() {
        boolean result = true;
        int dataRoamingScope = getDataRoamingScope();
        if (dataRoamingScope == 0 || (1 == dataRoamingScope && isInternationalRoaming())) {
            result = false;
        }
        log("dram getDataRoamingEnabledWithNational result " + result + " dataRoamingScope " + dataRoamingScope);
        return result;
    }

    public boolean isInternationalRoaming() {
        if (this.mDcTrackerBase.mPhone == null || this.mDcTrackerBase.mPhone.mIccRecords == null || this.mDcTrackerBase.mPhone.mIccRecords.get() == null) {
            log("mPhone or mIccRecords is null");
            return false;
        } else if (this.mDcTrackerBase.mPhone.getServiceState() == null) {
            log("dram isInternationalRoaming ServiceState is not start up");
            return false;
        } else if (this.mDcTrackerBase.mPhone.getServiceState().getRoaming()) {
            String simNumeric = ((IccRecords) this.mDcTrackerBase.mPhone.mIccRecords.get()).getOperatorNumeric();
            String operatorNumeric = this.mDcTrackerBase.mPhone.getServiceState().getOperatorNumeric();
            if (TextUtils.isEmpty(simNumeric) || TextUtils.isEmpty(operatorNumeric)) {
                log("dram isInternationalRoaming SIMNumeric or OperatorNumeric is not got!");
                return false;
            }
            log("dram isInternationalRoaming simNumeric " + simNumeric + " operatorNumeric " + operatorNumeric);
            if (simNumeric.length() <= 3 || operatorNumeric.length() <= 3 || simNumeric.substring(0, 3).equals(operatorNumeric.substring(0, 3))) {
                return false;
            }
            return true;
        } else {
            log("dram isInternationalRoaming Current service state is not roaming, bail ");
            return false;
        }
    }

    private void onLimitPDPActInd(AsyncResult ar) {
        if (ar != null && ar.exception == null && ar.result != null) {
            int[] responseArray = ar.result;
            if (responseArray != null && responseArray.length >= 2) {
                log("PSCLEARCODE onLimitPDPActInd result flag: " + responseArray[0] + " , cause: " + responseArray[1]);
                this.mIsLimitPDPAct = responseArray[0] == 1;
                DcFailCause cause = DcFailCause.fromInt(responseArray[1]);
                if (this.mIsLimitPDPAct && (isLteRadioTech() ^ 1) != 0) {
                    showPSClearCodeDialog(cause);
                }
                if (!(this.mAlarmManager == null || this.mClearCodeLimitAlarmIntent == null)) {
                    this.mAlarmManager.cancel(this.mClearCodeLimitAlarmIntent);
                    this.mClearCodeLimitAlarmIntent = null;
                }
                Intent intent = new Intent(INTENT_LIMIT_PDP_ACT_IND);
                intent.putExtra(IS_LIMIT_PDP_ACT, this.mIsLimitPDPAct);
                intent.addFlags(268435456);
                this.mClearCodeLimitAlarmIntent = PendingIntent.getBroadcast(this.mDcTrackerBase.mPhone.getContext(), 0, intent, 134217728);
                if (this.mAlarmManager != null) {
                    this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + PS_CLEARCODE_LIMIT_PDP_ACT_DELAY, this.mClearCodeLimitAlarmIntent);
                }
                log("PSCLEARCODE startAlarmForLimitPDPActInd: delay=" + PS_CLEARCODE_LIMIT_PDP_ACT_DELAY + " flag=" + this.mIsLimitPDPAct);
            }
        }
    }

    private void showPSClearCodeDialog(DcFailCause cause) {
        if (this.mPSClearCodeDialog == null) {
            this.mPSClearCodeDialog = createPSClearCodeDiag(cause);
            if (this.mPSClearCodeDialog != null) {
                this.mPSClearCodeDialog.show();
            }
        }
    }

    private void onActionIntentLimitPDPActInd(Intent intent) {
        if (intent != null) {
            boolean isLimitPDPAct = intent.getBooleanExtra(IS_LIMIT_PDP_ACT, false);
            log("PSCLEARCODE onActionIntentLimitPDPActInd: flag = " + isLimitPDPAct);
            if (!isLimitPDPAct) {
                this.mDcTrackerBase.updateApnContextState();
                setupDataOnConnectableApns("limitPDPActDisabled", null);
            }
        }
    }

    public long updatePSClearCodeApnContext(AsyncResult ar, ApnContext apnContext, long delay) {
        long delayTime = delay;
        if (ar == null || apnContext == null || apnContext.getApnSetting() == null) {
            return delay;
        }
        DcFailCause dcFailCause = ar.result;
        if (DcFailCause.PDP_ACTIVE_LIMIT == dcFailCause) {
            log("PSCLEARCODE retry APN. new delay = -1");
            return -1;
        }
        if (DcFailCause.SERVICE_OPTION_NOT_SUBSCRIBED == dcFailCause || DcFailCause.USER_AUTHENTICATION == dcFailCause) {
            if (isLteRadioTech()) {
                delayTime = PS_CLEARCODE_APN_DELAY_MILLIS_4G;
            } else {
                delayTime = PS_CLEARCODE_APN_DELAY_MILLIS_2G_3G;
            }
            apnContext.getApnSetting().permanentFailed = false;
        }
        log("PSCLEARCODE retry APN. new delay = " + delayTime);
        return delayTime;
    }

    private void onVoiceCallEndedHw() {
        log("onVoiceCallEndedHw");
        if (!HwModemCapability.isCapabilitySupport(0)) {
            int currentSub = this.mDcTrackerBase.mPhone.getPhoneId();
            SubscriptionController subscriptionController = SubscriptionController.getInstance();
            int defaultDataSubId = subscriptionController.getDefaultDataSubId();
            if (subscriptionController.getSubState(defaultDataSubId) == 0 && currentSub != defaultDataSubId) {
                log("defaultDataSub " + defaultDataSubId + " is inactive, set dataSubId to " + currentSub);
                subscriptionController.setDefaultDataSubId(currentSub);
            }
            if (this.mDcTrackerBase.mPhone.getServiceStateTracker() != null) {
                this.mDcTrackerBase.mPhone.notifyServiceStateChangedP(this.mDcTrackerBase.mPhone.getServiceStateTracker().mSS);
            }
        }
    }

    public boolean isDataConnectivityDisabled(int slotId, String tag) {
        return HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(slotId, tag);
    }

    private void debugLog(String logStr) {
        log(logStr);
    }

    public ApnSetting getCustPreferredApn(ArrayList<ApnSetting> apnSettings) {
        if (CUST_PREFERRED_APN == null || "".equals(CUST_PREFERRED_APN)) {
            return null;
        }
        if (apnSettings == null || apnSettings.isEmpty()) {
            log("getCustPreferredApn mAllApnSettings == null");
            return null;
        }
        int list_size = apnSettings.size();
        for (int i = 0; i < list_size; i++) {
            ApnSetting p = (ApnSetting) apnSettings.get(i);
            if (CUST_PREFERRED_APN.equals(p.apn)) {
                log("getCustPreferredApn: X found apnSetting" + p);
                return p;
            }
        }
        log("getCustPreferredApn: not found apn: " + CUST_PREFERRED_APN);
        return null;
    }

    public boolean isRoamingPushDisabled() {
        return HwTelephonyManagerInner.getDefault().isRoamingPushDisabled();
    }

    public boolean processAttDataRoamingOff() {
        if (!IS_ATT) {
            return false;
        }
        boolean domesticDataEnabled = Global.getInt(this.mDcTrackerBase.mPhone.getContext().getContentResolver(), "ATT_DOMESTIC_DATA", 0) != 0;
        log("processAttRoamingOff domesticDataEnabled = " + domesticDataEnabled);
        this.mDcTrackerBase.setDataEnabled(domesticDataEnabled);
        if (domesticDataEnabled) {
            this.mDcTrackerBase.notifyOffApnsOfAvailability("roamingOff");
            setupDataOnConnectableApns("roamingOff", null);
        } else {
            this.mDcTrackerBase.cleanUpAllConnections(true, "roamingOff");
            this.mDcTrackerBase.notifyOffApnsOfAvailability("roamingOff");
        }
        return true;
    }

    public boolean processAttDataRoamingOn() {
        if (!IS_ATT) {
            return false;
        }
        if (!this.mDcTrackerBase.mPhone.getServiceState().getDataRoaming()) {
            return true;
        }
        boolean dataRoamingEnabled = this.mDcTrackerBase.getDataRoamingEnabled();
        this.mDcTrackerBase.setDataEnabled(dataRoamingEnabled);
        if (dataRoamingEnabled) {
            log("onRoamingOn: setup data on in internal roaming");
            setupDataOnConnectableApns("roamingOn", null);
            this.mDcTrackerBase.notifyDataConnection("roamingOn");
        } else {
            log("onRoamingOn: Tear down data connection on internal roaming.");
            this.mDcTrackerBase.cleanUpAllConnections(true, "roamingOn");
            this.mDcTrackerBase.notifyOffApnsOfAvailability("roamingOn");
        }
        return true;
    }

    public boolean getXcapDataRoamingEnable() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mDcTrackerBase.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configLoader != null) {
            b = configLoader.getConfigForSubId(this.mDcTrackerBase.mPhone.getSubId());
        }
        boolean xcapDataRoamingEnable = false;
        if (b != null) {
            xcapDataRoamingEnable = b.getBoolean(XCAP_DATA_ROAMING_ENABLE);
        }
        log("getXcapDataRoamingEnable:xcapDataRoamingEnable " + xcapDataRoamingEnable);
        return xcapDataRoamingEnable;
    }
}

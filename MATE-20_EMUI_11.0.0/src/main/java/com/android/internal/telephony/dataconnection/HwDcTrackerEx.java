package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwCarrierConfigCardManager;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.HwPhoneManagerImpl;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.cust.HwCarrierConfigXmlParseEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import com.huawei.internal.telephony.dataconnection.DataConnectionEx;
import com.huawei.internal.telephony.dataconnection.DcFailCauseEx;
import com.huawei.internal.telephony.dataconnection.DcFailCauseExt;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import huawei.com.android.internal.telephony.RoamingBroker;
import huawei.cust.HwCfgFilePolicy;
import huawei.net.NetworkRequestExt;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class HwDcTrackerEx extends DefaultHwDcTrackerEx {
    private static final String ACTION_BT_CONNECTION_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ALLOW_MMS_PROPERTY_INT = "allow_mms_property_int";
    private static final int APNCURE_BLACKLIST = 3;
    private static final String APNCURE_BLACK_MCC_LIST = SystemPropertiesEx.get("ro.config.hw_hicure.apn_black_mcc", "334,001");
    private static final String APNCURE_BLACK_PLMN_LIST = SystemPropertiesEx.get("ro.config.hw_hicure.apn_black_plmn", BuildConfig.FLAVOR);
    private static final int APNCURE_EHPLMN_EMPTY = 2;
    private static final int APNCURE_FAIL_REASON_BASE = 0;
    private static final int APNCURE_HKGC = 7;
    private static final int APNCURE_ROAMINGBROKER = 5;
    private static final int APNCURE_VALIDE = 1;
    private static final int APNCURE_VIRTUALNET = 4;
    private static final int APNCURE_VSIM = 6;
    private static final int APNFINOFROMSP_APN_INDEX = 1;
    private static final int APNFINOFROMSP_PROTOCOL_INDEX = 2;
    private static final int APNFINOFROMSP_RPLMN_INDEX = 0;
    private static final String APN_ID = "apn_id";
    private static final int APN_PDP_TYPE_IPV4 = 1;
    private static final int APN_PDP_TYPE_IPV4V6 = 3;
    private static final int APN_PDP_TYPE_IPV6 = 2;
    private static final String CAUSE_NO_RETRY_AFTER_DISCONNECT = SystemPropertiesEx.get("ro.hwpp.disc_noretry_cause", BuildConfig.FLAVOR);
    private static final int CDMA_NOT_ROAMING = 0;
    private static final int CDMA_ROAMING = 1;
    private static final String[] CHINAUNICOM_PLMNS = {MO_UNICOM_MCCMNC, "46006", "46009"};
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String CHR_DATA = "chr_data";
    private static final String CHR_PACKAGE_NAME = "com.huawei.android.chr";
    private static final int CODE_GET_NETD_PID_CMD = 1112;
    private static final String CONFIG_XML_KEYNAME = "mcc_mnc";
    private static final String CONFIG_XML_OPKEYNAME = "operator_key";
    private static final String CONFIG_XML_TYPENAME = "carrier";
    private static final String CTNET = "ctnet";
    private static final String CT_CDMA_OPERATOR = "46003";
    private static final String CT_LTE_APN_PREFIX = SystemPropertiesEx.get("ro.config.ct_lte_apn", CTNET);
    private static final String CT_NOT_ROAMING_APN_PREFIX = SystemPropertiesEx.get("ro.config.ct_not_roaming_apn", CTNET);
    private static final String CT_ROAMING_APN_PREFIX = SystemPropertiesEx.get("ro.config.ct_roaming_apn", CTNET);
    private static final String CUST_FILE_PATH = "carrier/rule/xml/simInfo_mapping.xml";
    private static final String DATA_ROAMING_SIM2 = "data_roaming_sim2";
    private static final int DATA_STALL_ALARM_PUNISH_DELAY_IN_MS_DEFAULT = 1800000;
    private static final int DATA_STALL_ALARM_PUNISH_REREGISTER_DELAY_IN_MS_DEFAULT = 20000;
    private static final String DEAULT_ESMCURE_PROP_VALUE = "2";
    private static final boolean DISABLE_GW_PS_ATTACH = SystemPropertiesEx.getBoolean("ro.odm.disable_m1_gw_ps_attach", false);
    private static final String DS_USE_DURATION = "DSUseDuration";
    private static final int DS_USE_STATISTICS_REPORT_INTERVAL = 3600000;
    private static final String ENABLE_ALLOW_MMS = "enable_always_allow_mms";
    private static final String ESMFLAG_CURE_STATE = "persist.radio.hw_hicure.esmflag_state";
    private static final int EVENT_DATA_CONNECTED = 13;
    private static final int EVENT_DATA_CONNECTION_ATTACHED = 18;
    private static final int EVENT_DATA_CONNECTION_DETACHED = 12;
    private static final int EVENT_DATA_ENABLED_CHANGED = 15;
    private static final int EVENT_DATA_RAT_CHANGED = 16;
    private static final int EVENT_DELAY_RETRY_CONNECT_DATA = 19;
    private static final int EVENT_DISABLE_NR = 8;
    private static final int EVENT_GET_NR_SA_STATE_DONE = 11;
    private static final int EVENT_NETWORK_REJINFO_DONE = 6;
    private static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 14;
    private static final int EVENT_REENABLE_NR_ALARM = 7;
    private static final int EVENT_RIL_CONNECTED = 9;
    private static final int EVENT_SET_NR_SA_STATE_DONE = 10;
    private static final int EVENT_SIM_HOTPLUG = 17;
    private static final int EVENT_VOICE_CALL_ENDED = 3;
    private static final int EVENT_VOICE_CALL_STARTED = 5;
    private static final String FALSE = "false";
    private static final String FAULT_ID = "fault_id";
    private static final int FAULT_ID_SMART_CURE_APN_CURE = 30001;
    private static final int FLAG_NORMAL_RPC = 0;
    private static final String GC_ICCID = "8985231";
    private static final String GC_MCCMNC = "45431";
    private static final String GC_SPN = "CTExcel";
    private static final int GSM_ROAMING_CARD1 = 2;
    private static final int GSM_ROAMING_CARD2 = 3;
    private static final long HICURE_INFORM_BLOCKTIME = 86400000;
    private static final String HK_GC_CARD_OPERATOR = "46003";
    private static final String HW_APN_CURE_NOTITY = "com.huawei.action.APN_CURE_NOTIFY";
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final Set<Integer> IMMEDIATELY_RETRY_FAILCAUSES = new HashSet(0);
    private static final Set<String> IMMEDIATELY_RETRY_REASONS = new HashSet(0);
    private static final String INTENT_APN_CURE_STATISTICS = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    private static final String INTENT_DISABLE_NR_ALARM = "com.android.internal.telephony.data_disable_nr";
    private static final String INTENT_DISABLE_NR_ALARM_EXTRA_TIME = "disable_nr_alarm_extra_time";
    private static final String INTENT_DISABLE_NR_ALARM_EXTRA_TYPE = "disable_nr_alarm_extra_type";
    private static final String INTENT_DS_RESTART_RADIO = "com.huawei.intent.action.restart_radio";
    private static final String INTENT_PDP_RESET_ALARM = "com.android.internal.telephony.pdp-reset";
    private static final String INTENT_REENABLE_NR_ALARM = "com.android.internal.telephony.data_reenable_nr";
    private static final String INTENT_REENABLE_NR_ALARM_EXTRA_TYPE = "reenable_nr_alarm_extra_type";
    private static final String INTENT_SET_PREF_NETWORK_TYPE = "com.android.internal.telephony.set-pref-networktype";
    private static final int INVALID_PID = -1;
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_DO_RECOVERY_CHECK_DNS_STATS = SystemPropertiesEx.getBoolean("ro.config.dorecovery_add_dns", true);
    private static final boolean IS_DUAL_4G_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    private static boolean IS_ENABLE_APNCURE = false;
    private static boolean IS_ENABLE_ESMFLAG_CURE = false;
    private static final boolean IS_HW_DATA_RETRY_MANAGER_ENABLED = SystemPropertiesEx.getBoolean("ro.config.hw_data_retry_enabled", false);
    private static final boolean IS_MULTI_SIM_ENABLED = TelephonyManagerEx.isMultiSimEnabled();
    private static boolean IS_PDN_REJ_CURE_ENABLE = true;
    private static final int LTE_NOT_ROAMING = 4;
    private static final int LTE_WEAK_RSRP_THRESHOLD = -118;
    private static final byte MATCH_ALL = 1;
    private static final int MAX_RETRY_TIMES = 3;
    private static final int MAX_RSRP_VALUE = -44;
    private static final int MCCMNC_MIN_LENGTH = 5;
    private static final int MCC_LENGTH = 3;
    private static final int MIN_RSRP_VALUE = -140;
    private static final boolean MMS_IGNORE_DS_SWITCH_NOT_ROAMING = (((MMS_PROP >> 2) & 1) == 1);
    private static final boolean MMS_IGNORE_DS_SWITCH_ON_ROAMING = (((MMS_PROP >> 1) & 1) == 1);
    private static final boolean MMS_ON_ROAMING = ((MMS_PROP & 1) == 1);
    private static final int MMS_PROP = SystemPropertiesEx.getInt("ro.config.hw_always_allow_mms", 4);
    private static final int MNC_LENGTH = 3;
    private static final String MOBILE_DATA_ALWAYS_ON = "mobile_data_always_on";
    private static final String MODULE_ID = "module_id";
    private static final int MODULE_ID_SMART_CURE = 30000;
    private static final String MO_ICCID_1 = "8985302";
    private static final String MO_ICCID_2 = "8985307";
    private static final String MO_UNICOM_MCCMNC = "46001";
    private static final long NO_SUGGESTED_RETRY_DELAY = -2;
    private static final int NR_NOT_ROAMING = 5;
    private static final int NR_WEAK_RSRP_THRESHOLD = -115;
    private static final int OEM_DO_RECOVERY_EXCUTE = 2;
    private static final int OEM_DO_RECOVERY_NULL = 0;
    private static final int OEM_DO_RECOVERY_RESTART = 1;
    private static final String PDP_RESET_ALARM_TAG_EXTRA = "pdp.reset.alram.tag";
    private static final int PERIOD_TRY_DISABLE_NR_IN_MS = 2000;
    private static final int PERIOD_TRY_REENABLE_NR_IN_MS = 2000;
    private static boolean PERMANENT_ERROR_HEAL_PROP = false;
    private static final String PID_STATS_PATH = "/proc/net/xt_qtaguid/stats_pid";
    private static final String PKGNAME_TELEPHONY_DATACONNECTION_PHONE0 = "com.android.internal.telephony.dataconnection.phone0";
    private static final String PKGNAME_TELEPHONY_DATACONNECTION_PHONE1 = "com.android.internal.telephony.dataconnection.phone1";
    private static final String PKGNAME_TELEPHONY_DATACONNECTION_PHONE2 = "com.android.internal.telephony.dataconnection.phone2";
    private static final String PREFERRED_APN_ID = "preferredApnIdEx";
    private static final String PREFERRED_NETWORK_MODE = "preferred_network_mode";
    private static final String PREFIX_SLICE_CONFIG = "mobile_snssai";
    private static final int PREF_APN_ID_LEN = 5;
    private static final String PS_CLEARCODE_PLMN = SystemPropertiesEx.get("ro.config.clearcode_plmn", BuildConfig.FLAVOR);
    private static final int RB_SEQUENCE_LENGTH = 3;
    private static final int RECONNECT_ALARM_DELAY_TIME_SHORT = 300;
    private static final int REJINFO_LEN = 6;
    private static final int REJINFO_RAT_INDEX = 3;
    private static final int REJINFO_TYPE_INDEX = 5;
    private static final boolean RESET_PROFILE = SystemPropertiesEx.getBoolean("ro.hwpp_reset_profile", false);
    private static final int RESTART_RADIO_PUNISH_TIME_IN_MS = 14400000;
    private static final int RETRY_ALARM_DELAY_TIME_LONG = 5000;
    private static final int RETRY_ALARM_DELAY_TIME_SHORT = 50;
    private static final int RETRY_DATA_CONNECT_TIME = 3000;
    private static final String ROAMING_BROKER_SEQ_LIST = "8985231";
    private static final int ROAMING_BROKE_AFTERPLMN_INDEX = 2;
    private static final int ROAMING_BROKE_BEFOREPLMN_INDEX = 1;
    private static final String SEPARATOR_KEY = "=-=";
    private static final boolean SETAPN_UNTIL_CARDLOADED = SystemPropertiesEx.getBoolean("ro.config.delay_setapn", false);
    private static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final String[] SLICE_NETWORK_CONFIG_STRINGS = {"mobile_snssai1,49,0,0,-1,true", "mobile_snssai2,50,0,0,-1,true", "mobile_snssai3,51,0,0,-1,true", "mobile_snssai4,52,0,0,-1,true", "mobile_snssai5,53,0,0,-1,true", "mobile_snssai6,54,0,0,-1,true"};
    private static final int SUB2 = 1;
    private static final String SUB_ID = "sub_id";
    private static final int SWITCH_OFF = 0;
    private static final String TELECOM_IOT_APN_DELAY = "persist.radio.telecom_apn_delay";
    private static final String TRUE = "true";
    private static final int TYPE_CALLBACK_DO_RECOVERY_ACTION = 7;
    private static final int TYPE_CALLBACK_TCP_TX_AND_RX_SUM = 6;
    private static final int TYPE_DO_RECOVERY_OEM = 704;
    private static final int TYPE_GET_TCP_TX_AND_RX_SUM = 703;
    private static final int TYPE_RX_PACKETS = 1;
    private static final int TYPE_TX_PACKETS = 3;
    private static final int UMTS_WEAK_RSRP_THRESHOLD = -110;
    private static final int UNSPECIFIED_INT = -1;
    private static final boolean USER_FORCE_DATA_SETUP = SystemPropertiesEx.getBoolean("ro.hwpp.allow_data_onlycs", false);
    private static final int WAIT_BOOSTER_TCP_STAISTICAL_RESULT_TIMER_MS = 1000;
    private static final boolean WCDMA_VP_ENABLED = SystemPropertiesEx.get("ro.hwpp.wcdma_voice_preference", FALSE).equals(TRUE);
    private static final String XCAP_DATA_ROAMING_ENABLE = "carrier_xcap_data_roaming_switch";
    private static boolean sIsScreenOn = false;
    private ContentObserver allowMmsObserver = null;
    private boolean isAttachedApnRequested = false;
    private boolean isSupportPidStats = false;
    private AlarmManager mAlarmManager;
    private final SparseArray<ApnContextEx> mApnContextsByTypeFor5GSlice = new SparseArray<>();
    private ApnCureStat mApnCureStat = new ApnCureStat();
    private ApnSetting mAttachedApnSettings = null;
    private DcTrackerEx.BoosterCallback mBoosterCallback = new DcTrackerEx.BoosterCallback() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass3 */

        public void callback(int type, Bundle bundle) {
            if (bundle == null) {
                HwDcTrackerEx.this.log("data is null");
            } else if (type == 6) {
                HwDcTrackerEx.this.mTcpRxPktSum = bundle.getLong("tcpRxDataPktSum");
                HwDcTrackerEx.this.mTcpTxPktSum = bundle.getLong("tcpTxPktSum");
                HwDcTrackerEx.this.mCurrentLteRsrp = bundle.getInt("currentLteRsrp");
                HwDcTrackerEx.this.mCurrentNrRsrp = bundle.getInt("currentNrRsrp");
                HwDcTrackerEx.this.mCurrentUmtsRsrp = bundle.getInt("currentUmtsRsrp");
            } else if (type == 7) {
                if (bundle.getInt("DoRecoveryAct") == 0) {
                    HwDcTrackerEx.this.mDoRecoveryAct = 0;
                    HwDcTrackerEx.this.mIsDorecoveryTrigger = true;
                    HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
                    hwDcTrackerEx.actionProcess(hwDcTrackerEx.mDcTracker.getRecoveryAction());
                    HwDcTrackerEx.this.mDcTracker.sendMessage(HwDcTrackerEx.this.mDcTracker.obtainMessage(270354, (Object) null));
                    return;
                }
                HwDcTrackerEx.this.mDoRecoveryAct = 2;
            }
        }
    };
    private int mCurrentLteRsrp = 0;
    private int mCurrentNrRsrp = 0;
    private int mCurrentState = -1;
    private int mCurrentUmtsRsrp = 0;
    private DcTrackerEx mDcTracker;
    private String mDefaultApnId = "0,0,0,0,0";
    private int mDoRecoveryAct = 1;
    private int mDsUseDuration = 0;
    private ArrayList<ApnSetting> mEhplmnApnSettings = new ArrayList<>(0);
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("handleMessage msg=" + msg.what);
            int i = msg.what;
            if (i == 3) {
                HwDcTrackerEx.this.mIsInVoiceCall = false;
                HwDcTrackerEx.this.onVoiceCallEndedHw();
            } else if (i == 5) {
                HwDcTrackerEx.this.mIsInVoiceCall = true;
            } else if (i != 6) {
                HwDcTrackerEx.this.onDataRetryEventProc(msg);
            } else {
                HwDcTrackerEx.this.onNetworkRejInfoDone(msg);
            }
        }
    };
    private HwDataRetryManager mHwDataRetryManager = null;
    private HwDataSelfCure mHwDataSelfCure = null;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwDcTrackerEx.this.loge("intent or intent.getAction() is null.");
                return;
            }
            String action = intent.getAction();
            char c = 65535;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 4;
                        break;
                    }
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 3;
                        break;
                    }
                    break;
                case -1076576821:
                    if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                        c = '\f';
                        break;
                    }
                    break;
                case -910144067:
                    if (action.equals("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE")) {
                        c = '\t';
                        break;
                    }
                    break;
                case -843338808:
                    if (action.equals(HwDcTrackerEx.ACTION_BT_CONNECTION_CHANGED)) {
                        c = 5;
                        break;
                    }
                    break;
                case -374985024:
                    if (action.equals(HwDcTrackerEx.HW_SYSTEM_SERVER_START)) {
                        c = 11;
                        break;
                    }
                    break;
                case -238804267:
                    if (action.equals(HwDcTrackerEx.INTENT_DISABLE_NR_ALARM)) {
                        c = '\b';
                        break;
                    }
                    break;
                case -229777127:
                    if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 82734660:
                    if (action.equals(HwDcTrackerEx.INTENT_SET_PREF_NETWORK_TYPE)) {
                        c = 14;
                        break;
                    }
                    break;
                case 798292259:
                    if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1075003621:
                    if (action.equals(HwDcTrackerEx.HW_APN_CURE_NOTITY)) {
                        c = '\r';
                        break;
                    }
                    break;
                case 1843173795:
                    if (action.equals(HwDcTrackerEx.INTENT_PDP_RESET_ALARM)) {
                        c = 6;
                        break;
                    }
                    break;
                case 1896999900:
                    if (action.equals("com.android.telephony.opencard")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 1947666138:
                    if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                        c = 1;
                        break;
                    }
                    break;
                case 2134631443:
                    if (action.equals(HwDcTrackerEx.INTENT_REENABLE_NR_ALARM)) {
                        c = 7;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    onSimStateChange(intent);
                    return;
                case 1:
                    onShutDown();
                    return;
                case 2:
                    onBootCompleted();
                    return;
                case 3:
                    boolean unused = HwDcTrackerEx.sIsScreenOn = true;
                    return;
                case 4:
                    boolean unused2 = HwDcTrackerEx.sIsScreenOn = false;
                    return;
                case 5:
                    onBtConnectChanged(intent);
                    return;
                case 6:
                    HwDcTrackerEx.this.onActionIntentPdpResetAlarm(intent);
                    return;
                case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                    HwDcTrackerEx.this.onActionIntentReenableNrAlarm(intent);
                    return;
                case '\b':
                    HwDcTrackerEx.this.onActionIntentDisableNrAlarm(intent);
                    return;
                case '\t':
                    HwDcTrackerEx.this.onDualCardSwitchDone();
                    return;
                case HwDcTrackerEx.EVENT_SET_NR_SA_STATE_DONE /* 10 */:
                    HwDcTrackerEx.this.onUserSelectOpenService();
                    return;
                case HwDcTrackerEx.EVENT_GET_NR_SA_STATE_DONE /* 11 */:
                    HwDcTrackerEx.this.log("HW_SYSTEM_SERVER_START restart");
                    HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
                    hwDcTrackerEx.registerBoosterCallback(hwDcTrackerEx.mDcTracker.getTransportType(), HwDcTrackerEx.this.mPhone.getPhoneId());
                    return;
                case '\f':
                    onAirModeOn(intent);
                    return;
                case HwDcTrackerEx.EVENT_DATA_CONNECTED /* 13 */:
                    HwDcTrackerEx.this.mIsApnCureEnabled = intent.getBooleanExtra("APN_CURE_ENABLED", false);
                    HwDcTrackerEx.this.log("apn cure notify, retry enable data: " + HwDcTrackerEx.this.mIsApnCureEnabled);
                    if (HwDcTrackerEx.this.mIsApnCureEnabled) {
                        HwDcTrackerEx.this.mHandler.sendMessageDelayed(HwDcTrackerEx.this.mHandler.obtainMessage(HwDcTrackerEx.EVENT_DELAY_RETRY_CONNECT_DATA), 3000);
                        return;
                    }
                    return;
                case HwDcTrackerEx.EVENT_RADIO_OFF_OR_NOT_AVAILABLE /* 14 */:
                    HwDcTrackerEx.this.onActionIntentSetNetworkType(intent);
                    return;
                default:
                    HwDcTrackerEx.this.loge("Not handle action " + intent.getAction());
                    return;
            }
        }

        private void onSimStateChange(Intent intent) {
            int slotId = intent.getIntExtra("phone", -1);
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("slotId = " + slotId + ", mSubscription = " + HwDcTrackerEx.this.mSubscription);
            if (!SubscriptionManagerEx.isValidSlotIndex(slotId) || slotId != HwDcTrackerEx.this.mSubscription) {
                HwDcTrackerEx.this.log("receive onSimStateChange, but the subid is different from mSubscription");
                return;
            }
            String curSimState = intent.getStringExtra("ss");
            if ("NOT_READY".equals(curSimState) || "ABSENT".equals(curSimState)) {
                HwDcTrackerEx.this.setAttachedApnSetting(null);
                HwDcTrackerEx.this.isAttachedApnRequested = false;
                HwDcTrackerEx.this.mIsApnCureEnabled = false;
                HwDcTrackerEx.this.mDcTracker.clearCureApnSettings();
                HwDcTrackerEx.this.mHwDataSelfCure.apnCureFlagReset();
            }
            if (TextUtils.equals(curSimState, HwDcTrackerEx.this.mSimState)) {
                HwDcTrackerEx.this.logd("the curSimState is same as mSimState, so return");
                return;
            }
            if ("ABSENT".equals(curSimState)) {
                HwDcTrackerEx.this.sendApnCureResult();
            }
            if (("ABSENT".equals(curSimState) || "CARD_IO_ERROR".equals(curSimState)) && !"ABSENT".equals(HwDcTrackerEx.this.mSimState) && !"CARD_IO_ERROR".equals(HwDcTrackerEx.this.mSimState) && HwDcTrackerEx.RESET_PROFILE) {
                HwDcTrackerEx.this.logd("receive INTENT_VALUE_ICC_ABSENT or INTENT_VALUE_ICC_CARD_IO_ERROR , resetprofile");
                CommandsInterfaceEx ci = HwDcTrackerEx.this.mPhone.getCi();
                if (ci != null) {
                    ci.resetProfile((Message) null);
                }
            }
            ((HwDcTrackerEx) HwDcTrackerEx.this).mSimState = curSimState;
        }

        private void onShutDown() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HwDcTrackerEx.this.mPhone.getContext());
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(HwDcTrackerEx.DS_USE_DURATION, HwDcTrackerEx.this.mDsUseDuration);
            editor.commit();
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("Put mDSUseDuration into SharedPreferences, put: " + HwDcTrackerEx.this.mDsUseDuration + ", get: " + sp.getInt(HwDcTrackerEx.DS_USE_DURATION, 0));
        }

        private void onBootCompleted() {
            int lastDsUseDuration = PreferenceManager.getDefaultSharedPreferences(HwDcTrackerEx.this.mPhone.getContext()).getInt(HwDcTrackerEx.DS_USE_DURATION, 0);
            HwDcTrackerEx.this.mDsUseDuration += lastDsUseDuration;
            HwDcTrackerEx.this.log("Read last mDSUseDuration back from SharedPreferences, lastDSUseDuration: " + lastDsUseDuration + ", mDSUseDuration: " + HwDcTrackerEx.this.mDsUseDuration);
        }

        private void onBtConnectChanged(Intent intent) {
            int blueToothState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1);
            if (blueToothState == 0) {
                HwDcTrackerEx.this.mIsBtConnected = false;
            } else if (blueToothState == 2) {
                HwDcTrackerEx.this.mIsBtConnected = true;
            }
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("Received bt_connect_state = " + HwDcTrackerEx.this.mIsBtConnected);
        }

        private void onAirModeOn(Intent intent) {
            if (intent.getBooleanExtra("state", false)) {
                HwDcTrackerEx.this.resetDataCureInfo("airplaneModeOn");
            }
        }
    };
    private boolean mIsApnCureEnabled = false;
    private boolean mIsBtConnected = false;
    private boolean mIsDorecoveryTrigger = true;
    private boolean mIsInVoiceCall = false;
    private boolean mIsMmsAllowed = false;
    private long mLastRadioResetTimestamp = 0;
    private long mLastReRegisterTimestamp = 0;
    private long mMobileDnsRxPktSum = 0;
    private long mMobileDnsTxPktSum = 0;
    private long mNextReportDsUseDurationStamp = (SystemClock.elapsedRealtime() + 3600000);
    private ContentObserver mNwChangeObserver = null;
    private int mNwOldMode = PhoneExt.getPreferredNetworkMode();
    private int mOldState = 0;
    private PendingIntent mPdpResetAlarmIntent = null;
    private int mPdpResetAlarmTag = ((int) SystemClock.elapsedRealtime());
    private PhoneExt mPhone;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.internal.telephony.dataconnection.HwDcTrackerEx.AnonymousClass2 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            if (HwDcTrackerEx.this.mOldState == 2 && state == 0 && HwDcTrackerEx.this.mPhone.getSubId() == SubscriptionControllerEx.getInstance().getDefaultDataSubId()) {
                HwDcTrackerEx.this.mDcTracker.setupDataOnAllConnectableApns("dataEnabled");
                HwDcTrackerEx.this.log("resetRetryCount");
                HwDcTrackerEx.this.mDcTracker.resetDefaultApnRetryCount();
            }
            HwDcTrackerEx.this.mOldState = state;
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataConnectionStateChanged(int state) {
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("onDataConnectionStateChanged mSubId state = " + state);
            if (state == 2) {
                HwDcTrackerEx.this.mHandler.sendMessage(HwDcTrackerEx.this.mHandler.obtainMessage(HwDcTrackerEx.EVENT_DATA_CONNECTED));
            }
        }
    };
    private int mRetryTimes = 0;
    private HashSet<String> mRoamingbrokePlmnList = new HashSet<>(0);
    private String mSimState = null;
    private long mStartCureTime = 0;
    private int mSubscription = -1;
    private String mTag;
    private long mTcpRxPktSum = 0;
    private long mTcpTxPktSum = 0;
    private HashSet<String> mVirtualNetPlmnList = new HashSet<>(0);
    private long mWifiDnsRxPktSum = 0;
    private long mWifiDnsTxPktSum = 0;
    private int msgReceiveCounter = 0;
    private int msgTransCounter = 0;
    private int nwMode = PhoneExt.getPreferredNetworkMode();

    /* access modifiers changed from: private */
    public enum HotplugState {
        PLUG_OUT,
        PLUG_IN
    }

    static {
        IMMEDIATELY_RETRY_FAILCAUSES.add(Integer.valueOf(DcFailCauseEx.LOST_CONNECTION.getErrorCode()));
        IMMEDIATELY_RETRY_FAILCAUSES.add(Integer.valueOf(DcFailCauseEx.NETWORK_RECONFIGURE.getErrorCode()));
        IMMEDIATELY_RETRY_REASONS.add("pdpReset");
        IMMEDIATELY_RETRY_REASONS.add("nwTypeChanged");
        IMMEDIATELY_RETRY_REASONS.add(DcFailCauseEx.LOST_CONNECTION.toString());
    }

    public HwDcTrackerEx(PhoneExt phoneExt, DcTrackerEx dcTrackerEx) {
        if (phoneExt == null || dcTrackerEx == null) {
            loge("error in create HwDcTrackerEx for params is null.");
            return;
        }
        this.mPhone = phoneExt;
        this.mDcTracker = dcTrackerEx;
        this.mTag = HwDcTrackerEx.class.getSimpleName() + " - " + this.mPhone.getPhoneId();
        getPropValues();
        if (IS_PDN_REJ_CURE_ENABLE) {
            this.mHwDataSelfCure = new HwDataSelfCure(this.mDcTracker, this, this.mPhone);
        }
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED) {
            this.mHwDataRetryManager = new HwDataRetryManager(this.mDcTracker, this.mPhone);
        }
    }

    private static boolean isValidPlmn(String plmn) {
        if (plmn == null || plmn.length() < 5) {
            return false;
        }
        return true;
    }

    public void init() {
        boolean z = false;
        if (Settings.System.getInt(this.mPhone.getContext().getContentResolver(), ENABLE_ALLOW_MMS, 0) == 1) {
            z = true;
        }
        this.mIsMmsAllowed = z;
        Uri allowMmsUri = Settings.System.CONTENT_URI;
        this.allowMmsObserver = new AllowMmsContentObserver(this.mHandler);
        this.mPhone.getContext().getContentResolver().registerContentObserver(allowMmsUri, true, this.allowMmsObserver);
        this.mNwChangeObserver = new NwModeContentObserver(this.mHandler);
        this.mPhone.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor(PREFERRED_NETWORK_MODE + this.mPhone.getPhoneId()), true, this.mNwChangeObserver);
        this.nwMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mPhone.getContext(), this.mPhone.getPhoneId());
        this.mNwOldMode = this.nwMode;
        this.mPhone.registerForVoiceCallEnded(this.mHandler, 3, (Object) null);
        this.mPhone.registerForVoiceCallStarted(this.mHandler, 5, (Object) null);
        this.mSubscription = this.mPhone.getPhoneId();
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        registerBroadcastReceiver(this.mHandler);
        registerForDataRetryEvents(this.mDcTracker.getTransportType(), this.mHandler);
        HwDataRetryManager hwDataRetryManager = this.mHwDataRetryManager;
        if (hwDataRetryManager != null) {
            hwDataRetryManager.setLogTagSuffix(this.mTag);
            this.mHwDataRetryManager.configDataRetryStrategy();
        }
        initSupportPidStatistics();
        registerBoosterCallback(this.mDcTracker.getTransportType(), this.mPhone.getPhoneId());
        registerPhoneStateListener(this.mPhone.getContext());
    }

    private void registerBroadcastReceiver(Handler handler) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION_BT_CONNECTION_CHANGED);
        filter.addAction(INTENT_PDP_RESET_ALARM);
        if (SystemPropertiesEx.getBoolean("ro.config.hw_enable_ota_bip_cust", false)) {
            filter.addAction("com.android.telephony.opencard");
        }
        filter.addAction(HW_SYSTEM_SERVER_START);
        filter.addAction(HW_APN_CURE_NOTITY);
        if (IS_PDN_REJ_CURE_ENABLE) {
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.setOnNetReject(handler, 6, Integer.valueOf(this.mPhone.getPhoneId()));
            }
        }
        filter.addAction(INTENT_REENABLE_NR_ALARM);
        filter.addAction(INTENT_DISABLE_NR_ALARM);
        filter.addAction("com.huawei.action.ACTION_HW_SWITCH_SLOT_DONE");
        filter.addAction(INTENT_SET_PREF_NETWORK_TYPE);
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mDcTracker.getHandler());
    }

    private void registerForDataRetryEvents(int transportType, Handler handler) {
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED) {
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.registerForRilConnected(handler, 9, (Object) null);
                ci.registerForOffOrNotAvailable(handler, (int) EVENT_RADIO_OFF_OR_NOT_AVAILABLE, (Object) null);
                ci.registerForSimHotPlug(handler, (int) EVENT_SIM_HOTPLUG, Integer.valueOf(this.mPhone.getPhoneId()));
            }
            this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(transportType, handler, 12, (Object) null);
            this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(transportType, handler, 16, (Object) null);
            this.mDcTracker.registerForDataEnabledChanged(handler, 15, (Object) null);
        }
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(transportType, handler, (int) EVENT_DATA_CONNECTION_ATTACHED, (Object) null);
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(transportType, handler, 16, (Object) null);
    }

    public void dispose() {
        if (this.allowMmsObserver != null) {
            this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.allowMmsObserver);
        }
        if (this.mNwChangeObserver != null) {
            this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mNwChangeObserver);
        }
        this.mPhone.unregisterForVoiceCallStarted(this.mHandler);
        this.mPhone.unregisterForVoiceCallEnded(this.mHandler);
        if (this.mIntentReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
        CommandsInterfaceEx ci = this.mPhone.getCi();
        if (IS_PDN_REJ_CURE_ENABLE && ci != null) {
            ci.unSetOnNetReject(this.mHandler);
        }
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED && ci != null) {
            ci.unregisterForRilConnected(this.mHandler);
        }
    }

    public void beforeHandleMessage(Message msg) {
        if (msg.what == 271140 && WCDMA_VP_ENABLED) {
            log("EVENT_VP_STATUS_CHANGED");
            onVpStatusChanged(AsyncResultEx.from(msg.obj));
        }
    }

    public long getTcpRxPktSum() {
        return this.mTcpRxPktSum;
    }

    public long getTcpTxPktSum() {
        return this.mTcpTxPktSum;
    }

    private boolean isValidRsrpValue(int rsrp) {
        return rsrp <= MAX_RSRP_VALUE && rsrp >= MIN_RSRP_VALUE;
    }

    private boolean isWeakSignalStrength() {
        int networkType = TelephonyManagerEx.getNetworkType(this.mPhone.getSubId());
        boolean isWeak = false;
        boolean z = true;
        if (networkType != 3) {
            if (networkType != EVENT_DATA_CONNECTED) {
                if (networkType == 20 && isValidRsrpValue(this.mCurrentNrRsrp)) {
                    if (this.mCurrentNrRsrp > NR_WEAK_RSRP_THRESHOLD) {
                        z = false;
                    }
                    isWeak = z;
                }
            } else if (isValidRsrpValue(this.mCurrentLteRsrp)) {
                if (this.mCurrentLteRsrp > LTE_WEAK_RSRP_THRESHOLD) {
                    z = false;
                }
                isWeak = z;
            }
        } else if (isValidRsrpValue(this.mCurrentUmtsRsrp)) {
            if (this.mCurrentUmtsRsrp > UMTS_WEAK_RSRP_THRESHOLD) {
                z = false;
            }
            isWeak = z;
        }
        log("updateDataStallInfo: networkType = " + networkType + " LTE RSRP = " + this.mCurrentLteRsrp + " NR RSRP = " + this.mCurrentNrRsrp + " UMTS RSRP = " + this.mCurrentUmtsRsrp);
        return isWeak;
    }

    private void updateRecoveryInfo(long tcpSent, long tcpReceived, long dnsSent, long dnsReceived) {
        if (tcpReceived > 0) {
            log("updateDataStallInfo: TCP IN");
            this.mDcTracker.setSentSinceLastRecv(0, false);
            this.mDcTracker.resetRecoveryInfo();
        } else if (tcpSent > 0) {
            if (this.mDcTracker.isPhoneStateIdle()) {
                this.mDcTracker.setSentSinceLastRecv(tcpSent, true);
                this.mDcTracker.setRecoveryReason("tcp sent no recv", false);
                if (dnsSent > 0 && dnsReceived == 0) {
                    this.mDcTracker.setSentSinceLastRecv(dnsSent, true);
                    this.mDcTracker.setRecoveryReason(", dns sent no recv.", true);
                }
            } else {
                this.mDcTracker.setSentSinceLastRecv(0, false);
            }
            log("updateDataStallInfo: DNS+TCP OUT sent=" + tcpSent + dnsSent + " mSentSinceLastRecv=" + this.mDcTracker.getSentSinceLastRecv());
        } else if (dnsSent <= 0 || dnsReceived != 0) {
            log("updateDataStallInfo: TCP NONE");
        } else {
            if (this.mDcTracker.isPhoneStateIdle()) {
                this.mDcTracker.setRecoveryReason("dns sent no recv", false);
                this.mDcTracker.setSentSinceLastRecv(dnsSent, true);
            } else {
                this.mDcTracker.setSentSinceLastRecv(0, false);
            }
            log("updateDataStallInfo: DNS OUT sent=" + dnsSent + " mSentSinceLastRecv=" + this.mDcTracker.getSentSinceLastRecv());
        }
    }

    public void updateRecoveryPktStat() {
        long dnsReceived = 0;
        if (isWeakSignalStrength()) {
            this.mDcTracker.setSentSinceLastRecv(0, false);
            this.mDcTracker.resetRecoveryInfo();
            log("updateDataStallInfo: weak signal do not dorecover");
            return;
        }
        DcTrackerEx.TxRxSumEx preTcpTxRxSum = this.mDcTracker.getPreDataStallTcpTxRxSum();
        DcTrackerEx.TxRxSumEx preDnsTxRxSum = this.mDcTracker.getPreDataStallDnsTxRxSum();
        this.mDcTracker.updateHwTcpTxRxSum(getTcpTxPktSum(), getTcpRxPktSum());
        this.mDcTracker.setDnsTxPktsSum(getDnsUidPackets(1051, 3));
        this.mDcTracker.setDnsRxPktsSum(getDnsUidPackets(1051, 1));
        log("updateDataStallInfo: mDataStallDnsTxRxSum=" + this.mDcTracker.getDataStallDnsTxRxSum() + " preDnsTxRxSum=" + preDnsTxRxSum + " mDataStallTcpTxRxSum=" + this.mDcTracker.getDataStallTcpTxRxSum() + " preTcpTxRxSum=" + preTcpTxRxSum);
        long tcpSent = this.mDcTracker.getDataStallTcpTxRxSum() != null ? this.mDcTracker.getDataStallTcpTxRxSum().txPkts - preTcpTxRxSum.txPkts : 0;
        long tcpReceived = this.mDcTracker.getDataStallTcpTxRxSum() != null ? this.mDcTracker.getDataStallTcpTxRxSum().rxPkts - preTcpTxRxSum.rxPkts : 0;
        long dnsSent = this.mDcTracker.getDataStallDnsTxRxSum() != null ? this.mDcTracker.getDataStallDnsTxRxSum().txPkts - preDnsTxRxSum.txPkts : 0;
        if (this.mDcTracker.getDataStallDnsTxRxSum() != null) {
            dnsReceived = this.mDcTracker.getDataStallDnsTxRxSum().rxPkts - preDnsTxRxSum.rxPkts;
        }
        updateRecoveryInfo(tcpSent, tcpReceived, dnsSent, dnsReceived);
    }

    private boolean isWifiHasConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (cm == null) {
            log("isWifiHasConnected cm is null");
            return false;
        }
        Network[] allNetworks = cm.getAllNetworks();
        if (allNetworks == null) {
            log("isWifiHasConnected allNetworkInfo is null");
            return false;
        }
        for (Network network : allNetworks) {
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            if (nc != null && nc.hasTransport(1)) {
                return true;
            }
        }
        return false;
    }

    public long getDnsUidPackets(int uid, int type) {
        if (!IS_DO_RECOVERY_CHECK_DNS_STATS) {
            log("mDoRecoveryAddDnsProp is false");
            return 0;
        } else if (type == 3) {
            long increaseNum = (this.mDcTracker.getUidStats(uid, type) - this.mWifiDnsTxPktSum) - this.mMobileDnsTxPktSum;
            if (!isWifiHasConnected()) {
                this.mMobileDnsTxPktSum += increaseNum;
            } else {
                this.mWifiDnsTxPktSum += increaseNum;
            }
            return this.mMobileDnsTxPktSum;
        } else if (type == 1) {
            long increaseNum2 = (this.mDcTracker.getUidStats(uid, type) - this.mWifiDnsRxPktSum) - this.mMobileDnsRxPktSum;
            if (!isWifiHasConnected()) {
                this.mMobileDnsRxPktSum += increaseNum2;
            } else {
                this.mWifiDnsRxPktSum += increaseNum2;
            }
            return this.mMobileDnsRxPktSum;
        } else {
            log("getDnsUidPackets type error");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerBoosterCallback(int transportType, int phoneId) {
        int ret;
        if (transportType != 1) {
            log("mTransportType is not TRANSPORT_TYPE_WWAN, need not registerBoosterCallback");
            return;
        }
        log("registerBoosterCallback enter");
        if (phoneId == 0) {
            ret = DcTrackerEx.registerBoosterCallBack(PKGNAME_TELEPHONY_DATACONNECTION_PHONE0, this.mBoosterCallback);
        } else if (phoneId == 1) {
            ret = DcTrackerEx.registerBoosterCallBack(PKGNAME_TELEPHONY_DATACONNECTION_PHONE1, this.mBoosterCallback);
        } else {
            ret = DcTrackerEx.registerBoosterCallBack(PKGNAME_TELEPHONY_DATACONNECTION_PHONE2, this.mBoosterCallback);
        }
        if (ret != 0) {
            log("registerBoosterCallback:registerCallBack failed, ret=" + ret);
        }
    }

    public void notifyGetTcpSumMsgReportToBooster(int tag) {
        int ret;
        Bundle data = new Bundle();
        data.putInt("currentPhoneId", this.mPhone.getPhoneId());
        if (this.mPhone.getPhoneId() == 0) {
            ret = DcTrackerEx.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE0, (int) TYPE_GET_TCP_TX_AND_RX_SUM, data);
        } else if (this.mPhone.getPhoneId() == 1) {
            ret = DcTrackerEx.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE1, (int) TYPE_GET_TCP_TX_AND_RX_SUM, data);
        } else {
            ret = DcTrackerEx.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE2, (int) TYPE_GET_TCP_TX_AND_RX_SUM, data);
        }
        if (ret != 0) {
            log("reportBoosterPara failed, ret=" + ret);
        }
        Message msg = this.mDcTracker.obtainMessage(271148, (Object) null);
        msg.arg1 = tag;
        this.mDcTracker.sendMessageDelayed(msg, 1000);
    }

    public boolean isDataAllowedByApnContext(ApnContextEx apnContext) {
        if (apnContext == null) {
            loge("isDataAllowedByApnContext return false as default for invalid input.");
            return false;
        } else if (isBipApnType(apnContext.getApnType())) {
            return true;
        } else {
            if (isGsmOnlyPsNotAllowed()) {
                log("in GsmMode not allowed PS!");
                return false;
            } else if (!this.mDcTracker.isLimitPDPAct()) {
                return true;
            } else {
                log("PSCLEARCODE Limit PDP Act apnContext: " + apnContext);
                return false;
            }
        }
    }

    private boolean isGsmOnlyPsNotAllowed() {
        if (!IS_MULTI_SIM_ENABLED) {
            return SystemPropertiesEx.getBoolean("gsm.data.gsm_only_not_allow_ps", false) && this.nwMode == 1;
        }
        int slotId = this.mPhone.getPhoneId();
        int networkMode = this.nwMode;
        if (IS_DUAL_4G_SUPPORTED && SIM_NUM > 1) {
            networkMode = HwNetworkTypeUtils.getNetworkModeFromDB(this.mPhone.getContext(), slotId);
        }
        return TelephonyManagerEx.getTelephonyProperty(slotId, "gsm.data.gsm_only_not_allow_ps", FALSE).equals(TRUE) && networkMode == 1;
    }

    public boolean isDataAllowedForRoaming(boolean isMms) {
        if (!this.mPhone.getServiceState().getRoaming() || this.mDcTracker.getDataRoamingEnabled()) {
            return true;
        }
        int allowMmsPropertyByPlmn = getallowMmsPropertyByPlmn();
        if (allowMmsPropertyByPlmn != -1) {
            boolean mmsOnRoaming = (allowMmsPropertyByPlmn & 1) == 1;
            if ((this.mIsMmsAllowed || mmsOnRoaming) && isMms) {
                return true;
            }
            return false;
        } else if ((this.mIsMmsAllowed || MMS_ON_ROAMING) && isMms) {
            return true;
        } else {
            return false;
        }
    }

    public ApnSetting fetchBipApn(ApnSetting preferredApn, List<ApnSetting> allApnSettings) {
        if (!HuaweiTelephonyConfigs.isModemBipEnable()) {
            ApnSetting apnSetting = ApnSettingHelper.fromString(SystemPropertiesEx.get("gsm.bip.apn"));
            if ("default".equals(SystemPropertiesEx.get("gsm.bip.apn"))) {
                ApnSetting apn = fetchDefaultApnForBip(preferredApn, allApnSettings);
                if (apn != null) {
                    return apn;
                }
                log("find non apn for default bip");
                return null;
            } else if (apnSetting != null) {
                log("fetchBipApn: global BIP mDataProfile=" + apnSetting);
                return apnSetting;
            }
        }
        return null;
    }

    private ApnSetting fetchDefaultApnForBip(ApnSetting preferredApn, List<ApnSetting> allApnSettings) {
        if (preferredApn != null) {
            log("find prefer apn, use this");
            return preferredApn;
        } else if (allApnSettings == null) {
            return null;
        } else {
            for (ApnSetting apn : allApnSettings) {
                if (ApnSettingHelper.canHandleType(apn, (int) EVENT_SIM_HOTPLUG)) {
                    log("find the first default apn");
                    return apn;
                }
            }
            return null;
        }
    }

    public String getDataRoamingSettingItem(String originItem) {
        if (!IS_MULTI_SIM_ENABLED || this.mPhone.getPhoneId() != 1) {
            return originItem;
        }
        return DATA_ROAMING_SIM2;
    }

    public boolean getAnyDataEnabledByApnContext(ApnContextEx apnContext, boolean isEnabled) {
        if (apnContext == null) {
            loge("getAnyDataEnabledByApnContext invalid input, return false for default.");
            return false;
        }
        int allowMmsPropertyByPlmn = getallowMmsPropertyByPlmn();
        if (this.mPhone.getServiceState().getRoaming()) {
            if (getXcapDataRoamingEnable() && "xcap".equals(apnContext.getApnType())) {
                return true;
            }
            if (allowMmsPropertyByPlmn != -1) {
                boolean ignoreDsSwitchOnRoaming = ((allowMmsPropertyByPlmn >> 1) & 1) == 1;
                if (((this.mIsMmsAllowed || ignoreDsSwitchOnRoaming) && "mms".equals(apnContext.getApnType())) || isEnabled) {
                    return true;
                }
                return false;
            } else if (((this.mIsMmsAllowed || MMS_IGNORE_DS_SWITCH_ON_ROAMING) && "mms".equals(apnContext.getApnType())) || isEnabled) {
                return true;
            } else {
                return false;
            }
        } else if (allowMmsPropertyByPlmn != -1) {
            boolean ignoreDsSwitchNotRoaming = ((allowMmsPropertyByPlmn >> 2) & 1) == 1;
            if (((this.mIsMmsAllowed || ignoreDsSwitchNotRoaming) && "mms".equals(apnContext.getApnType())) || isEnabled) {
                return true;
            }
            return false;
        } else if (((this.mIsMmsAllowed || MMS_IGNORE_DS_SWITCH_NOT_ROAMING) && "mms".equals(apnContext.getApnType())) || isEnabled) {
            return true;
        } else {
            return false;
        }
    }

    public boolean noNeedDoRecovery() {
        long now = SystemClock.elapsedRealtime();
        if (!SystemPropertiesEx.getBoolean("persist.radio.hw.nodorecovery", false) && ((!SystemPropertiesEx.getBoolean("hw.ds.np.nopollstat", true) || isActiveDefaultApnPreset()) && ServiceStateEx.getDataState(this.mPhone.getServiceState()) == 0)) {
            long j = this.mLastRadioResetTimestamp;
            if (j == 0 || now - j >= 1800000) {
                return false;
            }
        }
        return true;
    }

    private boolean isReRegisterProtectTimeExpired() {
        long now = SystemClock.elapsedRealtime();
        long j = this.mLastReRegisterTimestamp;
        if (j == 0 || now - j >= 20000) {
            long j2 = this.mLastRadioResetTimestamp;
            if (j2 == 0 || now - j2 >= 20000) {
                return true;
            }
            log("isReRegisterProtectTimeExpired RadioReset action protect timeoutreturn false");
            return false;
        }
        log("isReRegisterProtectTimeExpired ReRegister action protect timeout return false");
        return false;
    }

    public void actionProcess(int inputAction) {
        log("actionProcess intput action:" + inputAction);
        for (int i = inputAction; i <= 3; i++) {
            if (i == 2) {
                if (isReRegisterProtectTimeExpired()) {
                    this.mDcTracker.putRecoveryAction(i);
                    return;
                }
            } else if (i >= this.mDcTracker.getRecoveryAction()) {
                this.mDcTracker.putRecoveryAction(i);
                return;
            }
        }
    }

    private boolean isActiveDefaultApnPreset() {
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType("default");
        if (apnContext == null || apnContext.getState() != ApnContextEx.StateEx.CONNECTED) {
            return true;
        }
        ApnSetting apnSetting = apnContext.getApnSetting();
        StringBuilder sb = new StringBuilder();
        sb.append("current default apn is ");
        sb.append(ApnSettingHelper.isPreset(apnSetting) ? "preset" : "non-preset");
        log(sb.toString());
        return ApnSettingHelper.isPreset(apnSetting);
    }

    private void initSupportPidStatistics() {
        if (!IS_DO_RECOVERY_CHECK_DNS_STATS) {
            return;
        }
        if (new File(PID_STATS_PATH).exists()) {
            this.isSupportPidStats = true;
        } else {
            this.isSupportPidStats = false;
        }
    }

    public void setupDataOnConnectableApns(String reason, String excludedApnType) {
        log("setupDataOnConnectableApns: " + reason + ", excludedApnType = " + excludedApnType);
        this.mDcTracker.setupDataOnConnectableApns(reason, excludedApnType);
    }

    public boolean needRetryAfterDisconnected(int cause) {
        if (cause != 65535) {
            return true;
        }
        String failCauseStr = SystemPropertiesEx.get("ril.ps_ce_reason", BuildConfig.FLAVOR);
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

    private boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    private boolean isCtDualModeCard(int sub) {
        int subType = HwTelephonyManagerInner.getDefault().getCardType(sub);
        if (subType != 41 && subType != 43) {
            return false;
        }
        log("sub = " + sub + ", SubType = " + subType + " is CT dual modem card");
        return true;
    }

    public boolean isInChina() {
        String mcc = null;
        String operatorNumeric = ServiceStateEx.getDataOperatorNumeric(this.mPhone.getServiceState());
        if (operatorNumeric != null && operatorNumeric.length() > 3) {
            mcc = operatorNumeric.substring(0, 3);
            log("isInChina current mcc = " + mcc);
        }
        if (CHINA_OPERATOR_MCC.equals(mcc)) {
            return true;
        }
        return false;
    }

    public boolean isPingOk() {
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimOn()) {
            log("isPineOk always ok for vsim on");
            return true;
        } else if (noNeedDoRecovery() || ServiceStateEx.getDataState(this.mPhone.getServiceState()) != 0) {
            log("isPineOk always false if not default apn or dataRegState not in service");
            return false;
        } else {
            startPingThread();
            return true;
        }
    }

    private void startPingThread() {
        log("startPingThread() enter.");
        new PingThread().start();
    }

    public void unregisterForImsiReady(IccRecordsEx r) {
        if (r != null) {
            r.unregisterForImsiReady(this.mDcTracker.getHandler());
        }
    }

    public void registerForImsiReady(IccRecordsEx r) {
        if (r != null) {
            r.registerForImsiReady(this.mDcTracker.getHandler(), 270338, (Object) null);
        }
    }

    public void unregisterForRecordsLoaded(IccRecordsEx r) {
        if (r != null) {
            r.unregisterForRecordsLoaded(this.mDcTracker.getHandler());
        }
    }

    public void registerForRecordsLoaded(IccRecordsEx r) {
        if (r != null) {
            r.registerForRecordsLoaded(this.mDcTracker.getHandler(), 270338, (Object) null);
        }
    }

    public void registerForGetAdDone(UiccCardApplicationEx newUiccApplication) {
        if (newUiccApplication != null) {
            newUiccApplication.registerForGetAdDone(this.mDcTracker.getHandler(), 270338, (Object) null);
        }
    }

    public void unregisterForGetAdDone(UiccCardApplicationEx newUiccApplication) {
        if (newUiccApplication != null) {
            newUiccApplication.unregisterForGetAdDone(this.mDcTracker.getHandler());
        }
    }

    public void registerForImsi(UiccCardApplicationEx newUiccApplication, IccRecordsEx newIccRecords) {
        if (newUiccApplication != null && newIccRecords != null) {
            if (!TextUtils.isEmpty(PS_CLEARCODE_PLMN) || SETAPN_UNTIL_CARDLOADED) {
                newIccRecords.registerForRecordsLoaded(this.mDcTracker.getHandler(), 270338, (Object) null);
                return;
            }
            int appType = newUiccApplication.getType();
            if (appType == 1 || appType == 2) {
                log("New USIM records found");
                newUiccApplication.registerForGetAdDone(this.mDcTracker.getHandler(), 271144, (Object) null);
            } else if (appType == 3 || appType == 4) {
                log("New CSIM records found");
                newIccRecords.registerForImsiReady(this.mDcTracker.getHandler(), 271144, (Object) null);
            } else {
                log("New other records found");
            }
            newIccRecords.registerForRecordsLoaded(this.mDcTracker.getHandler(), 270338, (Object) null);
        }
    }

    public boolean checkMvnoParams() {
        boolean result = false;
        String operator = getCTOperator(getOperatorNumeric());
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            if (HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated(Integer.valueOf(this.mPhone.getPhoneId()))) {
                operator = HwPhoneManagerImpl.getDefault().getRoamingBrokerOperatorNumeric(Integer.valueOf(this.mPhone.getPhoneId()));
            }
        } else if (HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated()) {
            operator = HwPhoneManagerImpl.getDefault().getRoamingBrokerOperatorNumeric();
        }
        if (operator != null) {
            log("checkMvnoParams: selection=numeric = ?");
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, "numeric = ?", new String[]{operator}, "_id");
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
        if (!cursor.moveToFirst()) {
            return false;
        }
        do {
            String mvnoType = cursor.getString(cursor.getColumnIndexOrThrow("mvno_type"));
            String mvnoMatchData = cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data"));
            if (!TextUtils.isEmpty(mvnoType) && !TextUtils.isEmpty(mvnoMatchData)) {
                log("checkMvno: X has mvno paras");
                return true;
            }
        } while (cursor.moveToNext());
        return false;
    }

    public void handleCustMessage(Message msg) {
        HwDataSelfCure hwDataSelfCure;
        int i = msg.what;
        if (i != 271147) {
            if (i == 271149) {
                log("handleMessage-EVENT_DATA_DATA_CURE_FOR_ESM_FLAG_CHANGE!");
                if (IS_PDN_REJ_CURE_ENABLE && (hwDataSelfCure = this.mHwDataSelfCure) != null) {
                    hwDataSelfCure.esmFlagCureTimerTimeOut();
                }
            }
        } else if (this.msgReceiveCounter >= this.msgTransCounter) {
            log("already handleMessage-EVENT_GET_ATTACH_INFO_DONE, do nothing!");
        } else {
            log("handleMessage-EVENT_GET_ATTACH_INFO_DONE!");
            onGetAttachInfoDone(AsyncResultEx.from(msg.obj));
            this.msgReceiveCounter = this.msgTransCounter;
        }
    }

    public int getPrimarySlot() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    private int getSecondarySlot() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == 0 ? 1 : 0;
    }

    private void checkCureResultToChr(DataConnectionEx dc) {
        ApnContextEx apnContext;
        if (dc != null && (apnContext = dc.getApnContextFromCp()) != null) {
            if (this.mDcTracker.isCureApnContainsType(apnContext.getApnType())) {
                ApnCureStat.access$3708(this.mApnCureStat);
            }
        }
    }

    public void addIfacePhoneHashMap(DataConnectionEx dc, HashMap<String, Integer> map) {
        String iface;
        if (dc == null || map == null) {
            loge("addIfacePhoneHashMap any of input is null, return.");
            return;
        }
        LinkProperties tempLinkProperties = dc.getLinkProperties();
        if (!(tempLinkProperties == null || (iface = tempLinkProperties.getInterfaceName()) == null)) {
            map.put(iface, Integer.valueOf(this.mPhone.getPhoneId()));
        }
        checkCureResultToChr(dc);
    }

    public void sendDSMipErrorBroadcast() {
        if (SystemPropertiesEx.getBoolean("ro.config.hw_mip_error_dialog", false)) {
            this.mPhone.getContext().sendBroadcast(new Intent("com.android.huawei.DATA_CONNECTION_MOBILE_IP_ERROR"));
        }
    }

    public boolean enableTcpUdpSumForDataStall() {
        return SystemPropertiesEx.getBoolean("ro.hwpp_enable_tcp_udp_sum", false);
    }

    public String networkTypeToApnType(int networkType) {
        if (networkType == 0) {
            return "default";
        }
        if (networkType == 2) {
            return "mms";
        }
        if (networkType == 3) {
            return "supl";
        }
        if (networkType == 4) {
            return "dun";
        }
        if (networkType == 5) {
            return "hipri";
        }
        if (networkType == EVENT_RADIO_OFF_OR_NOT_AVAILABLE) {
            return "ia";
        }
        if (networkType == 15) {
            return "emergency";
        }
        switch (networkType) {
            case EVENT_SET_NR_SA_STATE_DONE /* 10 */:
                return "fota";
            case EVENT_GET_NR_SA_STATE_DONE /* 11 */:
                return "ims";
            case 12:
                return "cbs";
            default:
                switch (networkType) {
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
                    case 44:
                        return "bip6";
                    case 45:
                        return "xcap";
                    default:
                        switch (networkType) {
                            case 48:
                                return "internaldefault";
                            case 49:
                                return "snssai1";
                            case RETRY_ALARM_DELAY_TIME_SHORT /* 50 */:
                                return "snssai2";
                            case 51:
                                return "snssai3";
                            case 52:
                                return "snssai4";
                            case 53:
                                return "snssai5";
                            case 54:
                                return "snssai6";
                            default:
                                log("Error mapping networkType " + networkType + " to apnType");
                                return BuildConfig.FLAVOR;
                        }
                }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDataRetryEventProc(Message msg) {
        log("onDataRetryEventProc msg=" + msg.what);
        switch (msg.what) {
            case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                onReenableNrAlarm(msg.getData());
                return;
            case 8:
                onDisableNr(msg.getData());
                return;
            case 9:
                onRilConnected(msg);
                return;
            case EVENT_SET_NR_SA_STATE_DONE /* 10 */:
                onSetNrSaStateDone(msg);
                return;
            case EVENT_GET_NR_SA_STATE_DONE /* 11 */:
                onGetNrSaStateDone(msg);
                return;
            case 12:
                onDataConnectionDetached();
                return;
            case EVENT_DATA_CONNECTED /* 13 */:
                onDataConnected();
                return;
            case EVENT_RADIO_OFF_OR_NOT_AVAILABLE /* 14 */:
                onRadioOffOrNotAvailable();
                return;
            case 15:
                onDataEnabledChanged(msg);
                return;
            case 16:
                onDataRatChanged();
                return;
            case EVENT_SIM_HOTPLUG /* 17 */:
                onSimHotPlug(msg);
                return;
            case EVENT_DATA_CONNECTION_ATTACHED /* 18 */:
                onDataAttached();
                return;
            case EVENT_DELAY_RETRY_CONNECT_DATA /* 19 */:
                log("time out, try connect data" + msg.what);
                this.mDcTracker.setupDataOnAllConnectableApns(BuildConfig.FLAVOR);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onVoiceCallEndedHw() {
        log("onVoiceCallEndedHw");
        if (!HwModemCapability.isCapabilitySupport(0)) {
            int currentSub = this.mPhone.getSubId();
            SubscriptionControllerEx subscriptionController = SubscriptionControllerEx.getInstance();
            int defaultDataSubId = subscriptionController.getDefaultDataSubId();
            if (subscriptionController.getSubState(SubscriptionManager.getSlotIndex(defaultDataSubId)) == 0 && currentSub != defaultDataSubId) {
                if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
                    log("defaultDataSub " + defaultDataSubId + " is inactive, set dataSubId to " + currentSub);
                    subscriptionController.setDefaultDataSubId(currentSub);
                } else {
                    log("vsim is in process or cardreload, not set dds to " + currentSub);
                }
            }
            if (this.mPhone.getServiceStateTracker() != null) {
                this.mPhone.notifyServiceStateChanged();
            }
        }
    }

    public boolean isDataConnectivityDisabled(int slotId, String tag) {
        return HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(slotId, tag);
    }

    public boolean isRoamingPushDisabled() {
        return HwTelephonyManagerInner.getDefault().isRoamingPushDisabled();
    }

    public boolean getXcapDataRoamingEnable() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configLoader != null) {
            b = configLoader.getConfigForSubId(this.mPhone.getSubId());
        }
        boolean isXcapDataRoamingEnable = false;
        if (b != null) {
            isXcapDataRoamingEnable = b.getBoolean(XCAP_DATA_ROAMING_ENABLE);
        }
        log("getXcapDataRoamingEnable:xcapDataRoamingEnable " + isXcapDataRoamingEnable);
        try {
            Boolean isXcapEnable = (Boolean) HwCfgFilePolicy.getValue(XCAP_DATA_ROAMING_ENABLE, SubscriptionManager.getSlotIndex(this.mPhone.getSubId()), Boolean.class);
            if (isXcapEnable != null) {
                return isXcapEnable.booleanValue();
            }
            return isXcapDataRoamingEnable;
        } catch (Exception e) {
            log("Exception: read carrier_xcap_data_roaming_switch failed");
            return isXcapDataRoamingEnable;
        }
    }

    public void updateDSUseDuration() {
        if (sIsScreenOn) {
            this.mDsUseDuration++;
            log("updateDSUseDuration: Update mDSUseDuration: " + this.mDsUseDuration);
            long curTime = SystemClock.elapsedRealtime();
            if (curTime > this.mNextReportDsUseDurationStamp) {
                this.mPhone.sendIntentDsUseStatistics(this.mDsUseDuration);
                log("updateDSUseDuration: report mDSUseDuration: " + this.mDsUseDuration);
                this.mDsUseDuration = 0;
                this.mNextReportDsUseDurationStamp = 3600000 + curTime;
            }
        }
    }

    private int getallowMmsPropertyByPlmn() {
        int allowMmsPropertyInt;
        PersistableBundle bundle;
        int allowMmsPropertyInt2 = -1;
        try {
            int subId = this.mPhone.getSubId();
            Integer hwAlwaysAllowMms = (Integer) HwCfgFilePolicy.getValue(ALLOW_MMS_PROPERTY_INT, SubscriptionManager.getSlotIndex(subId), Integer.class);
            if (hwAlwaysAllowMms != null) {
                allowMmsPropertyInt = hwAlwaysAllowMms.intValue();
            } else {
                CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
                if (configLoader == null || (bundle = configLoader.getConfigForSubId(subId)) == null) {
                    return -1;
                }
                allowMmsPropertyInt = bundle.getInt(ALLOW_MMS_PROPERTY_INT, -1);
            }
            if (allowMmsPropertyInt >= 0) {
                if (allowMmsPropertyInt <= 7) {
                    allowMmsPropertyInt2 = allowMmsPropertyInt;
                    log("getallowMmsPropertyByPlmn:allowMmsPropertyInt " + allowMmsPropertyInt2);
                    return allowMmsPropertyInt2;
                }
            }
            allowMmsPropertyInt2 = -1;
            log("getallowMmsPropertyByPlmn:allowMmsPropertyInt " + allowMmsPropertyInt2);
        } catch (Exception e) {
            log("Exception: read allow_mms_property_int failed");
        }
        return allowMmsPropertyInt2;
    }

    public boolean getAttachedStatus(boolean attached) {
        int dataSub;
        if ((HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK || DISABLE_GW_PS_ATTACH) && (dataSub = SubscriptionManager.getDefaultDataSubscriptionId()) != this.mPhone.getSubId() && SubscriptionManager.isUsableSubscriptionId(dataSub)) {
            return true;
        }
        return attached;
    }

    public boolean isBtConnected() {
        return this.mIsBtConnected;
    }

    public boolean isWifiConnected() {
        NetworkInfo activeNetworkInfo;
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        if (cm == null || (activeNetworkInfo = cm.getActiveNetworkInfo()) == null || activeNetworkInfo.getType() != 1) {
            return false;
        }
        log("isWifiConnected return true");
        return true;
    }

    public boolean isDataNeededWithWifiAndBt() {
        boolean isDataAlwaysOn = Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), MOBILE_DATA_ALWAYS_ON, 0) != 0;
        if (isDataAlwaysOn) {
            log("isDataNeededWithWifiAndBt:isDataAlwaysOn = true");
        }
        return isDataAlwaysOn || (!isBtConnected() && !isWifiConnected());
    }

    public void updateLastDoRecoveryTimestamp(int action) {
        log("updateLastDoRecoveryTimestamp action:" + action);
        DcTrackerEx dcTrackerEx = this.mDcTracker;
        if (action == 2) {
            this.mLastReRegisterTimestamp = SystemClock.elapsedRealtime();
        } else if (action == 4) {
            this.mLastRadioResetTimestamp = SystemClock.elapsedRealtime();
        } else {
            log("updateLastDoRecoveryTimestamp action is wrong");
        }
    }

    public boolean isDorecoveryTrigger() {
        return this.mIsDorecoveryTrigger;
    }

    public void setIsDorecoveryTrigger(boolean isDorecoveryTrigger) {
        this.mIsDorecoveryTrigger = isDorecoveryTrigger;
    }

    public boolean needRestartRadioOnError(ApnContextEx apnContext, DcFailCauseExt cause) {
        TelephonyManager tm = TelephonyManagerEx.getDefault();
        if (apnContext == null || cause == null || tm == null) {
            return false;
        }
        long now = SystemClock.elapsedRealtime();
        int current4gSlotId = getPrimarySlot();
        if ("default".equals(apnContext.getApnType()) && tm.getCallState() == 0 && current4gSlotId == this.mPhone.getPhoneId() && PERMANENT_ERROR_HEAL_PROP) {
            long j = this.mLastRadioResetTimestamp;
            if ((now - j > 14400000 || 0 == j) && apnContext.restartOnError(cause.getErrorCode()) && this.mDcTracker.getTransportType() == 1) {
                log("needRestartRadioOnError return true");
                this.mLastRadioResetTimestamp = now;
                return true;
            }
        }
        return false;
    }

    private boolean shouldUseAttachApn(String expectApnType) {
        if (!"default".equals(expectApnType) || this.mAttachedApnSettings == null) {
            return false;
        }
        if (this.mEhplmnApnSettings == null) {
            return true;
        }
        return checkApnName();
    }

    private boolean checkApnName() {
        int size = this.mEhplmnApnSettings.size();
        for (int i = 0; i < size; i++) {
            if (this.mAttachedApnSettings.getApnName().equalsIgnoreCase(this.mEhplmnApnSettings.get(i).getApnName())) {
                return false;
            }
        }
        return true;
    }

    private void addToApnList(ArrayList<ApnSetting> apnList, ApnSetting curApnSetting) {
        if (apnList != null && !apnList.contains(curApnSetting)) {
            apnList.add(curApnSetting);
        }
    }

    /* access modifiers changed from: protected */
    public void apnSelectAndAdd(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        log("apnSelectAndAdd, type:" + requestedApnType + ", tech:" + radioTech + ", operator" + operator);
        if (shouldUseAttachApn(requestedApnType)) {
            checkAttachApnAndAdd(requestedApnType, radioTech, apnList, operator);
        } else {
            checkEhplmnApnAndAdd(requestedApnType, radioTech, apnList);
        }
    }

    private void checkEhplmnApnAndAdd(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList) {
        int size = this.mEhplmnApnSettings.size();
        boolean isEhplmnUsed = false;
        for (int i = 0; i < size; i++) {
            ApnSetting apnSetting = this.mEhplmnApnSettings.get(i);
            if (ApnSettingHelper.canHandleType(apnSetting, ApnSettingHelper.getApnTypesBitmaskFromString(requestedApnType)) && ServiceStateEx.bitmaskHasTech(apnSetting.getNetworkTypeBitmask(), radioTech)) {
                log("use ehplmn apn:" + apnSetting);
                isEhplmnUsed = true;
                addToApnList(apnList, apnSetting);
            }
        }
        if (isEhplmnUsed) {
            ApnCureStat.access$3808(this.mApnCureStat);
        } else if (this.mApnCureStat.mFailReason == 1) {
            ApnCureStat.access$4008(this.mApnCureStat);
        }
    }

    private void checkAttachApnAndAdd(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        if (this.mAttachedApnSettings.getOperatorNumeric() != null && this.mAttachedApnSettings.getOperatorNumeric().equals(operator) && ApnSettingHelper.canHandleType(this.mAttachedApnSettings, ApnSettingHelper.getApnTypesBitmaskFromString(requestedApnType)) && ServiceStateEx.bitmaskHasTech(this.mAttachedApnSettings.getNetworkTypeBitmask(), radioTech)) {
            log("use attach apn:" + this.mAttachedApnSettings);
            addToApnList(apnList, this.mAttachedApnSettings);
            ApnCureStat.access$4108(this.mApnCureStat);
        }
    }

    private void onGetAttachInfoDone(AsyncResultEx ar) {
        log("onGetAttachInfoDoneX");
        DcTrackerEx.LteAttachInfoEx stAttachInfo = null;
        if (ar.getException() != null) {
            log("Exception occurred, failed to report the LTE attach info");
        } else if (!(ar.getResult() instanceof Bundle)) {
            log("onGetAttachInfoDone::(LteAttachInfo)ar.result is invalid.");
            return;
        } else {
            Bundle bundle = (Bundle) ar.getResult();
            if (bundle != null) {
                stAttachInfo = new DcTrackerEx.LteAttachInfoEx();
                stAttachInfo.setApn(bundle.getString("apn", null));
                stAttachInfo.setProtocol(bundle.getInt("protocol", 0));
                boolean isValidApn = true;
                if (!isValidLteApn(stAttachInfo.getApn())) {
                    log("onGetAttachInfoDone::stAttachInfo.apn is inValid Lte Apn.");
                    isValidApn = false;
                }
                if (!isValidProtocol(stAttachInfo.getProtocol())) {
                    log("onGetAttachInfoDone::stAttachInfo.protocol is inValid protocol.");
                    isValidApn = false;
                }
                if (!isValidApn) {
                    stAttachInfo = null;
                }
            } else {
                return;
            }
        }
        ApnSetting attachedApnSettings = null;
        if (stAttachInfo != null) {
            attachedApnSettings = convertHalAttachInfo(stAttachInfo);
        }
        this.isAttachedApnRequested = true;
        if (attachedApnSettings != null) {
            this.mAttachedApnSettings = attachedApnSettings;
            saveLteAttachApnInfoToSp(this.mAttachedApnSettings);
            this.mDcTracker.setupDataOnAllConnectableApns(BuildConfig.FLAVOR);
            log("attachedApnSetting: " + this.mAttachedApnSettings);
            this.mDcTracker.sendIntentWhenApnNeedReport(this.mPhone, attachedApnSettings, 1024, new LinkProperties());
            return;
        }
        this.mDcTracker.setupDataOnAllConnectableApns(BuildConfig.FLAVOR);
        this.mAttachedApnSettings = null;
        log("attachedApnSetting is null!");
    }

    private ApnSetting convertHalAttachInfo(DcTrackerEx.LteAttachInfoEx attachInfo) {
        log("convertHalAttachInfo");
        String apn = attachInfo.getApn();
        String protocol = covertProtocol(attachInfo.getProtocol());
        String operator = getOperatorNumeric();
        log("convertHalAttachInfo, operator is:" + operator);
        ApnSetting.Builder builder = new ApnSetting.Builder();
        builder.setOperatorNumeric(operator).setApnName(apn).setEntryName("default lte apn").setProxyAddress(BuildConfig.FLAVOR).setProxyPort(0).setMmsProxyAddress(BuildConfig.FLAVOR).setMmsProxyPort(0).setUser(BuildConfig.FLAVOR).setPassword(BuildConfig.FLAVOR).setAuthType(-1).setApnTypeBitmask(EVENT_SIM_HOTPLUG).setProtocol(ApnSettingHelper.getProtocolIntFromString(protocol)).setRoamingProtocol(ApnSettingHelper.getProtocolIntFromString(protocol)).setCarrierEnabled(true);
        return builder.build();
    }

    private boolean notEmptyStr(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return true;
    }

    private String dctGetImsi() {
        IccRecordsEx r = this.mPhone.getIccRecords();
        if (r != null) {
            return r.getIMSI();
        }
        return null;
    }

    private String dctGetIccid() {
        IccRecordsEx r = this.mPhone.getIccRecords();
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    private void saveLteAttachApnInfoToSp(ApnSetting apn) {
        if (apn != null) {
            ApnSetting savedLteAttachApn = getLteAttachApnInfoFromSp();
            String imsi = dctGetImsi();
            String rplmn = ServiceStateEx.getDataOperatorNumeric(this.mPhone.getServiceState());
            if (isValidKey(imsi, rplmn)) {
                if (savedLteAttachApn == null || !apn.equals(savedLteAttachApn)) {
                    SharedPreferences.Editor editor = this.mPhone.getContext().getSharedPreferences("lte_attach_apn", 0).edit();
                    editor.putString(imsi, rplmn + SEPARATOR_KEY + apn.getApnName() + SEPARATOR_KEY + apn.getProtocol());
                    editor.commit();
                }
            }
        }
    }

    private boolean isValidKey(String imsi, String rplmn) {
        return notEmptyStr(imsi) && isValidPlmn(rplmn);
    }

    private ApnSetting getLteAttachApnInfoFromSp() {
        return getLteAttachApnInfoFromSp(dctGetImsi());
    }

    private ApnSetting getLteAttachApnInfoFromSp(String imsi) {
        log("getLteAttachApnInfoFromSp");
        if (!notEmptyStr(imsi)) {
            return null;
        }
        String apnInfo = this.mPhone.getContext().getSharedPreferences("lte_attach_apn", 0).getString(imsi, BuildConfig.FLAVOR);
        if (notEmptyStr(apnInfo)) {
            return fillAttachInfo(apnInfo);
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004d, code lost:
        if (r7.equals("IP") != false) goto L_0x0051;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x005e  */
    private ApnSetting fillAttachInfo(String apnInfo) {
        String[] infos = apnInfo.split(SEPARATOR_KEY);
        boolean z = false;
        String apnRplmn = infos[0];
        String apn = infos[1];
        String proto = infos[2];
        if (!isPlmnInEhplmn(ServiceStateEx.getDataOperatorNumeric(this.mPhone.getServiceState())) || !isPlmnInEhplmn(apnRplmn)) {
            return null;
        }
        DcTrackerEx.LteAttachInfoEx attachInfo = new DcTrackerEx.LteAttachInfoEx();
        attachInfo.setApn(apn);
        int hashCode = proto.hashCode();
        if (hashCode != 2343) {
            if (hashCode == 2254343 && proto.equals("IPV6")) {
                z = true;
                if (z) {
                    attachInfo.setProtocol(1);
                } else if (!z) {
                    attachInfo.setProtocol(3);
                } else {
                    attachInfo.setProtocol(2);
                }
                return convertHalAttachInfo(attachInfo);
            }
        }
        z = true;
        if (z) {
        }
        return convertHalAttachInfo(attachInfo);
    }

    private boolean isHkGcCardForApnCure(int phoneId) {
        String operator = getCTOperator(getOperatorNumeric());
        IccRecordsEx record = this.mPhone.getIccRecords();
        String preSpn = record != null ? record.getServiceProviderName() : BuildConfig.FLAVOR;
        String preIccid = SystemPropertiesEx.get("gsm.sim.preiccid_" + phoneId, BuildConfig.FLAVOR);
        if (!"46003".equals(operator) && isValidPlmn(operator)) {
            return false;
        }
        if (!"8985231".equals(preIccid) && !GC_SPN.equals(preSpn)) {
            return false;
        }
        log("is Hongkong GC card");
        this.mApnCureStat.mFailReason = 7;
        return true;
    }

    private void getVirtualNetPlmns() {
        if (this.mVirtualNetPlmnList.size() <= 0) {
            File custFile = HwCfgFilePolicy.getCfgFile(CUST_FILE_PATH, 0);
            if (custFile == null || !custFile.exists()) {
                log("siminfo mapping file not exists");
                return;
            }
            Map map = HwCarrierConfigXmlParseEx.parseFile(custFile);
            if (map == null || map.isEmpty() || !map.containsKey("carrier")) {
                log("siminfo mapping is invalid");
            } else {
                fillVirtualNetPlmnList(map);
            }
        }
    }

    private void fillVirtualNetPlmnList(Map map) {
        Set<String> mixVnet = new HashSet<>(0);
        HashMap<String, Integer> multiOpkeyVnet = new HashMap<>(0);
        for (Map<String, String> rule : (List) map.get("carrier")) {
            String mccmnc = rule.get(CONFIG_XML_KEYNAME);
            String opKey = rule.get(CONFIG_XML_OPKEYNAME);
            if (mccmnc != null) {
                if (mccmnc.equals(opKey)) {
                    mixVnet.add(mccmnc);
                }
                if (multiOpkeyVnet.containsKey(mccmnc)) {
                    multiOpkeyVnet.put(mccmnc, Integer.valueOf(multiOpkeyVnet.get(mccmnc).intValue() + 1));
                } else {
                    multiOpkeyVnet.put(mccmnc, 1);
                }
            }
        }
        for (String key : mixVnet) {
            if (multiOpkeyVnet.containsKey(key) && multiOpkeyVnet.get(key).intValue() > 1) {
                this.mVirtualNetPlmnList.add(key);
            }
        }
    }

    private void getRoamingBrokerPlmns() {
        String roamingBrokeConfig;
        if (this.mRoamingbrokePlmnList.size() <= 0) {
            Optional<String> rbSequenceListConf = RoamingBroker.getDefault(Integer.valueOf(this.mPhone.getPhoneId())).getRBSeqListVal();
            if (rbSequenceListConf.isPresent()) {
                roamingBrokeConfig = rbSequenceListConf.get();
            } else {
                roamingBrokeConfig = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "roamingBrokerSequenceList");
            }
            if (roamingBrokeConfig != null) {
                fillRoamingBrokerPlmnList(roamingBrokeConfig);
            }
        }
    }

    private void fillRoamingBrokerPlmnList(String roamingBrokeConfig) {
        String[] configList = roamingBrokeConfig.split("\\|");
        for (String config : configList) {
            if (config != null) {
                String[] parts = config.split(",");
                if (parts.length >= 3) {
                    String beforeRbPlmn = parts[1];
                    String afterRbPlmn = parts[2];
                    if (!beforeRbPlmn.isEmpty()) {
                        this.mRoamingbrokePlmnList.add(beforeRbPlmn);
                    }
                    if (!afterRbPlmn.isEmpty()) {
                        this.mRoamingbrokePlmnList.add(afterRbPlmn);
                    }
                }
            }
        }
    }

    private boolean checkPlmnList(HashSet<String> plmnList, String[] plmns) {
        if (plmnList.size() == 0) {
            return true;
        }
        for (String plmn : plmns) {
            if (plmnList.contains(plmn)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInVirtualNet(String[] plmns) {
        getVirtualNetPlmns();
        boolean isVnet = checkPlmnList(this.mVirtualNetPlmnList, plmns);
        if (isVnet) {
            this.mApnCureStat.mFailReason = 4;
        }
        return isVnet;
    }

    private boolean isInRoamingBroker(String[] plmns) {
        boolean isRoamBroker;
        String iccid = dctGetIccid();
        if (iccid == null || iccid.startsWith("8985231")) {
            getRoamingBrokerPlmns();
            isRoamBroker = checkPlmnList(this.mRoamingbrokePlmnList, plmns);
        } else {
            isRoamBroker = false;
        }
        if (isRoamBroker) {
            this.mApnCureStat.mFailReason = 5;
        }
        return isRoamBroker;
    }

    private boolean checkConditionForApnCure(int phoneId) {
        if (!IS_ENABLE_APNCURE || isHkGcCardForApnCure(phoneId)) {
            return false;
        }
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            if (!HwVSimUtils.isVSimSub(phoneId) && !HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated(Integer.valueOf(phoneId))) {
                return true;
            }
            this.mApnCureStat.mFailReason = 6;
            return false;
        } else if (!HwVSimUtils.isVSimSub(phoneId) && !HwPhoneManagerImpl.getDefault().isRoamingBrokerActivated()) {
            return true;
        } else {
            this.mApnCureStat.mFailReason = 6;
            return false;
        }
    }

    private String[] getOrderedSimEhplmn() {
        IccRecordsEx record = UiccControllerExt.getInstance().getIccRecords(SubscriptionManager.getSlotIndex(this.mPhone.getSubId()), UiccControllerExt.APP_FAM_3GPP);
        return record != null ? record.getEhplmnOfSim() : new String[0];
    }

    private boolean isPlmnsInBlackList(String[] plmns) {
        List<String> disabledMccList = Arrays.asList(APNCURE_BLACK_MCC_LIST.split(","));
        List<String> disabledPlmnList = Arrays.asList(APNCURE_BLACK_PLMN_LIST.split(","));
        for (String plmn : plmns) {
            if (isValidPlmn(plmn)) {
                boolean isMccDisabled = disabledMccList.contains(plmn.substring(0, 3));
                boolean isPlmnDisabled = disabledPlmnList.contains(plmn);
                if (isMccDisabled || isPlmnDisabled) {
                    this.mApnCureStat.mFailReason = 3;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean plmnsCheck(String[] plmns) {
        if (isPlmnsInBlackList(plmns) || isInVirtualNet(plmns) || isInRoamingBroker(plmns)) {
            return false;
        }
        return true;
    }

    private boolean isPlmnInEhplmn(String plmn) {
        return Arrays.asList(getOrderedSimEhplmn()).contains(plmn);
    }

    private boolean isPlmnInEhplmn(String plmn, String[] ehplmn) {
        return Arrays.asList(ehplmn).contains(plmn);
    }

    private boolean isInChinaArea(String[] ehplmn) {
        String rplmn = ServiceStateEx.getDataOperatorNumeric(this.mPhone.getServiceState());
        log("isInChinaArea, rplmn:" + rplmn);
        if (!isValidPlmn(rplmn) || !rplmn.substring(0, 3).equals(CHINA_OPERATOR_MCC) || !isPlmnInEhplmn(rplmn, ehplmn)) {
            return false;
        }
        return true;
    }

    private boolean canDoApnCure(int phoneId, String[] ehplmns, String hplmn) {
        if (checkConditionForApnCure(phoneId) && plmnsCheck(ehplmns) && !isHplmnNotForApnCure(ehplmns, hplmn)) {
            return true;
        }
        log("condition not suit");
        return false;
    }

    private boolean isHplmnNotForApnCure(String[] ehplmns, String hplmn) {
        String[] plmnArray = {hplmn};
        if (isValidPlmn(hplmn)) {
            if (!plmnsCheck(plmnArray)) {
                log("hplmn valid, but not suit");
                return true;
            }
        } else if (!isInChinaArea(ehplmns)) {
            log("hplmn invalid, but not suit");
            return true;
        }
        return false;
    }

    private void getEhplmnApn(String[] ehplmnList, String hplmn) {
        int subId = this.mPhone.getSubId();
        int phoneId = this.mPhone.getPhoneId();
        if (ehplmnList != null && canDoApnCure(phoneId, ehplmnList, hplmn)) {
            boolean isFoundApn = false;
            for (String operator : ehplmnList) {
                if (!isFoundApn) {
                    String selection = "numeric = '" + operator + "'";
                    log("getEhplmnApn: selection=" + selection);
                    Cursor cursor = getCursor(selection, Long.toString((long) subId));
                    if (cursor != null) {
                        this.mEhplmnApnSettings.clear();
                        while (cursor.moveToNext()) {
                            this.mEhplmnApnSettings.add(ApnSettingHelper.makeApnSetting(cursor));
                            isFoundApn = true;
                        }
                        cursor.close();
                    }
                } else {
                    return;
                }
            }
        }
    }

    private Cursor getCursor(String selection, String strSubId) {
        if (!TelephonyManagerEx.isMultiSimEnabled()) {
            return this.mPhone.getContext().getContentResolver().query(Telephony.Carriers.CONTENT_URI, null, selection, null, "_id");
        }
        DcTrackerEx dcTrackerEx = this.mDcTracker;
        return this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(DcTrackerEx.MSIM_TELEPHONY_CARRIERS_URI, strSubId), null, selection, null, "_id");
    }

    private String covertProtocol(int protocol) {
        log("covertProtocol - protocol is:" + protocol);
        if (protocol == 1) {
            return "IP";
        }
        if (protocol == 2) {
            return "IPV6";
        }
        if (protocol == 3) {
            return "IPV4V6";
        }
        log("unknown protocol = " + protocol);
        return "unknown";
    }

    private boolean isValidLteApn(String apn) {
        if (apn == null) {
            log("apn is null.");
            return false;
        } else if (!apn.toLowerCase(Locale.ENGLISH).contains("ims")) {
            return true;
        } else {
            log("apn contain string ims.");
            return false;
        }
    }

    private boolean isValidProtocol(int protocol) {
        if (1 == protocol || 2 == protocol || 3 == protocol) {
            return true;
        }
        log("Protocol is:" + protocol);
        return false;
    }

    private boolean isChinaUnicomCard() {
        ServiceState serviceState = this.mPhone.getServiceState();
        String rplmn = ServiceStateEx.getDataOperatorNumeric(serviceState);
        if (ServiceStateEx.getDataRoaming(serviceState) || !Arrays.asList(CHINAUNICOM_PLMNS).contains(rplmn)) {
            return false;
        }
        return true;
    }

    private Bundle buildBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("AttachSuccCnt", this.mApnCureStat.mAttachSuccCnt);
        bundle.putInt("EhplmnSuccCnt", this.mApnCureStat.mEhplmnSuccCnt);
        bundle.putInt("EnabledFailCnt", this.mApnCureStat.mEnabledFailCnt);
        bundle.putInt("SetupSuccCnt", this.mApnCureStat.mSetupSuccCnt);
        bundle.putByte("FailReason", (byte) this.mApnCureStat.mFailReason);
        bundle.putString("SimOperator", this.mApnCureStat.simOperator);
        return bundle;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendApnCureResult() {
        Context context = this.mPhone.getContext();
        if (context == null) {
            log("context should not be null");
        } else if (this.mApnCureStat.mAttachSuccCnt + this.mApnCureStat.mEhplmnSuccCnt + this.mApnCureStat.mEnabledFailCnt != 0) {
            Intent intent = new Intent("com.huawei.android.chr.action.ACTION_REPORT_CHR");
            intent.setFlags(67108864);
            intent.setPackage(CHR_PACKAGE_NAME);
            intent.putExtra("module_id", MODULE_ID_SMART_CURE);
            intent.putExtra("fault_id", FAULT_ID_SMART_CURE_APN_CURE);
            intent.putExtra("sub_id", UiccControllerExt.getInstance().getSlotIdFromPhoneId(this.mPhone.getPhoneId()));
            intent.putExtra("chr_data", buildBundle());
            context.sendBroadcastAsUser(intent, UserHandleEx.ALL, "com.huawei.android.permission.GET_CHR_DATA");
            this.mApnCureStat.resetAll();
            this.mStartCureTime = SystemClock.elapsedRealtime();
        }
    }

    private void checkTimeAndSendResult() {
        if (SystemClock.elapsedRealtime() - this.mStartCureTime >= HICURE_INFORM_BLOCKTIME) {
            sendApnCureResult();
        }
    }

    private void getLteAttachedApnSettings(String operator) {
        if (!isPlmnsInBlackList(new String[]{operator})) {
            this.mAttachedApnSettings = getLteAttachApnInfoFromSp();
        } else {
            this.mAttachedApnSettings = null;
        }
    }

    public void updateApnLists(String requestedApnType, int radioTech, ArrayList<ApnSetting> apnList, String operator) {
        if (requestedApnType == null || apnList == null || operator == null) {
            loge("updateApnLists, some of input is null, return.");
            return;
        }
        log("updateApnLists radioTech:" + radioTech + ",operator:" + operator);
        List<ApnSetting> apnSettings = this.mDcTracker.getAllApnList();
        if ((this.mIsApnCureEnabled || !apnSettings.isEmpty()) && IS_ENABLE_APNCURE) {
            String[] ehplmn = getOrderedSimEhplmn();
            if (!isLTENetworks()) {
                log("Currently is not over LTE network");
                getLteAttachedApnSettings(operator);
            } else {
                log("Currently is over LTE network");
                if (this.mAttachedApnSettings == null && !this.isAttachedApnRequested) {
                    fetchAttachedApnSettings();
                    return;
                }
            }
            this.isAttachedApnRequested = false;
            boolean isEhplmnReceived = true;
            if (ehplmn == null || ehplmn.length == 0) {
                isEhplmnReceived = false;
            }
            if (isEhplmnReceived || "LOADED".equals(this.mSimState)) {
                if (!isEhplmnReceived && isChinaUnicomCard()) {
                    ehplmn = CHINAUNICOM_PLMNS;
                    isEhplmnReceived = true;
                }
                getEhplmnApn(ehplmn, operator);
                if (!isEhplmnReceived) {
                    this.mApnCureStat.mFailReason = 2;
                }
                this.mApnCureStat.simOperator = operator;
                apnSelectAndAdd(requestedApnType, radioTech, apnList, operator);
                if (!apnList.isEmpty()) {
                    this.mDcTracker.addCureApnSettings(requestedApnType, apnList);
                }
                checkTimeAndSendResult();
                return;
            }
            log("ehplmn is empty or sim not loaded");
        }
    }

    private void fetchAttachedApnSettings() {
        log("getAttachedApnSettings");
        Message result = this.mDcTracker.obtainMessage(271147, (Object) null);
        this.msgTransCounter++;
        CommandsInterfaceEx ci = this.mPhone.getCi();
        if (ci != null) {
            ci.getAttachedApnSettings(result);
        }
    }

    public ApnSetting getAttachedApnSetting() {
        log("getAttachedApnSetting");
        return this.mAttachedApnSettings;
    }

    public void setAttachedApnSetting(ApnSetting apnSetting) {
        log("setAttachedApnSetting");
        this.mAttachedApnSettings = apnSetting;
    }

    public void startPdpResetAlarm(int delay) {
        if (this.mPhone.getPhoneType() == 2 && SystemPropertiesEx.getBoolean("hw.dct.psrecovery", false)) {
            this.mPdpResetAlarmTag++;
            log("startPdpResetAlarm for CDMA: tag=" + this.mPdpResetAlarmTag + " delay=" + (delay / WAIT_BOOSTER_TCP_STAISTICAL_RESULT_TIMER_MS) + "s");
            Intent intent = new Intent(INTENT_PDP_RESET_ALARM);
            intent.putExtra(PDP_RESET_ALARM_TAG_EXTRA, this.mPdpResetAlarmTag);
            log("startPdpResetAlarm: delay=" + delay + " action=" + intent.getAction());
            this.mPdpResetAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + ((long) delay), this.mPdpResetAlarmIntent);
        }
    }

    public void stopPdpResetAlarm() {
        boolean isCdmaPsRecoveryEnabled = false;
        if (this.mPhone.getPhoneType() == 2 && SystemPropertiesEx.getBoolean("hw.dct.psrecovery", false)) {
            isCdmaPsRecoveryEnabled = true;
        }
        if (isCdmaPsRecoveryEnabled) {
            log("stopPdpResetAlarm: current tag=" + this.mPdpResetAlarmTag + " mPdpResetAlarmIntent=" + this.mPdpResetAlarmIntent);
            this.mPdpResetAlarmTag = this.mPdpResetAlarmTag + 1;
            PendingIntent pendingIntent = this.mPdpResetAlarmIntent;
            if (pendingIntent != null) {
                this.mAlarmManager.cancel(pendingIntent);
                this.mPdpResetAlarmIntent = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentPdpResetAlarm(Intent intent) {
        log("onActionIntentPdpResetAlarm: action=" + intent.getAction());
        int tag = intent.getIntExtra(PDP_RESET_ALARM_TAG_EXTRA, 0);
        if (this.mPdpResetAlarmTag != tag) {
            log("onPdpRestAlarm: ignore, tag=" + tag + " expecting " + this.mPdpResetAlarmTag);
            return;
        }
        this.mDcTracker.cleanUpAllConnections("pdpReset");
    }

    public ApnSetting getApnForCT() {
        if (!isCTSimCard(this.mPhone.getPhoneId()) || this.mDcTracker.getAllApnList() == null || this.mDcTracker.getAllApnList().isEmpty()) {
            return null;
        }
        if (getSecondarySlot() == this.mPhone.getPhoneId() && !isCtDualModeCard(getSecondarySlot())) {
            log("getApnForCT otherslot == mPhone.getPhoneId() && !isCTDualModeCard(otherslot)");
            return null;
        } else if (this.mPhone.getServiceState().getOperatorNumeric() == null) {
            log("getApnForCT mPhone.getServiceState().getOperatorNumeric() == null");
            return null;
        } else {
            ApnSetting preferredApn = this.mDcTracker.getPreferredApn();
            if (preferredApn != null && !isApnPreset(preferredApn)) {
                return null;
            }
            this.mCurrentState = getCurState();
            int matchApnId = matchApnId(this.mCurrentState);
            if (matchApnId != -1) {
                this.mDcTracker.setPreferredApn(matchApnId);
                return null;
            }
            int i = this.mCurrentState;
            if (i != 0) {
                if (i == 1 || i == 2 || i == 3) {
                    return setApnForCt(CT_ROAMING_APN_PREFIX);
                }
                if (i == 4 || i == 5) {
                    return setApnForCt(CT_LTE_APN_PREFIX);
                }
                return null;
            } else if (isCtCardForFullNet()) {
                return setApnForCt(CT_ROAMING_APN_PREFIX);
            } else {
                return setApnForCt(CT_NOT_ROAMING_APN_PREFIX);
            }
        }
    }

    private ApnSetting setApnForCt(String apn) {
        if (apn == null || BuildConfig.FLAVOR.equals(apn)) {
            return null;
        }
        ContentResolver resolver = this.mPhone.getContext().getContentResolver();
        if (this.mDcTracker.getAllApnList() == null || this.mDcTracker.getAllApnList().isEmpty() || resolver == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        Uri uri = Uri.withAppendedPath(DcTrackerEx.PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
        int apnSize = this.mDcTracker.getAllApnList().size();
        for (int i = 0; i < apnSize; i++) {
            ApnSetting dp = (ApnSetting) this.mDcTracker.getAllApnList().get(i);
            int bearer = ServiceStateEx.convertNetworkTypeBitmaskToBearerBitmask(dp.getNetworkTypeBitmask());
            if (apn.equals(dp.getApnName()) && ApnSettingHelper.canHandleType(dp, (int) EVENT_SIM_HOTPLUG) && (((!isCTLteNetwork() && !isNRNetwork()) || (bearer != 0 && (ApnSettingHelper.canSupportNetworkType(dp, ServiceStateEx.rilRadioTechnologyToNetworkType((int) EVENT_DATA_CONNECTED)) || ApnSettingHelper.canSupportNetworkType(dp, ServiceStateEx.rilRadioTechnologyToNetworkType((int) EVENT_RADIO_OFF_OR_NOT_AVAILABLE))))) && (isCTLteNetwork() || isNRNetwork() || bearer == 0 || (!ApnSettingHelper.canSupportNetworkType(dp, ServiceStateEx.rilRadioTechnologyToNetworkType((int) EVENT_DATA_CONNECTED)) && !ApnSettingHelper.canSupportNetworkType(dp, ServiceStateEx.rilRadioTechnologyToNetworkType((int) EVENT_RADIO_OFF_OR_NOT_AVAILABLE)))))) {
                resolver.delete(uri, null, null);
                values.put(APN_ID, Integer.valueOf(dp.getId()));
                resolver.insert(uri, values);
                return dp;
            }
        }
        return null;
    }

    private int getCurState() {
        int currentStatus = -1;
        if (isCTLteNetwork()) {
            currentStatus = 4;
        } else if (isNRNetwork()) {
            currentStatus = 5;
        } else if (this.mPhone.getPhoneType() == 2) {
            currentStatus = TelephonyManagerEx.isNetworkRoaming(getPrimarySlot()) ? 1 : 0;
        } else if (this.mPhone.getPhoneType() == 1) {
            int current4gSlotId = getPrimarySlot();
            int current2gSlotId = getSecondarySlot();
            int current4gSubId = SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(current4gSlotId);
            int current2gSubId = SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(current2gSlotId);
            if (current4gSubId == this.mPhone.getSubId() && TelephonyManagerEx.isNetworkRoaming(current4gSubId)) {
                currentStatus = 2;
            } else if (current2gSubId == this.mPhone.getSubId() && TelephonyManagerEx.isNetworkRoaming(current2gSubId)) {
                currentStatus = 3;
            }
        }
        log("getCurState:CurrentStatus =" + currentStatus);
        return currentStatus;
    }

    private boolean isCtCardForFullNet() {
        if (!isFullNetworkSupported()) {
            return false;
        }
        return isCTSimCard(this.mPhone.getPhoneId());
    }

    private int matchApnId(int sign) {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        int matchId = -1;
        if (IS_MULTI_SIM_ENABLED) {
            preferredApnIdSlot = PREFERRED_APN_ID + (getPrimarySlot() == this.mPhone.getPhoneId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String lastApnId = Settings.System.getString(cr, preferredApnIdSlot);
            log("MatchApnId:LastApnId: " + lastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (lastApnId != null) {
                String[] apnId = lastApnId.split(",");
                if (apnId.length != 5 || apnId[this.mCurrentState] == null) {
                    Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                } else if (!"0".equals(apnId[this.mCurrentState])) {
                    matchId = Integer.parseInt(apnId[this.mCurrentState]);
                }
            } else {
                Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
            }
        } catch (Exception e) {
            log("MatchApnId got exception");
            Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        }
        return matchId;
    }

    public boolean isLTENetworks() {
        int dataRadioTech = ServiceStateEx.getRilDataRadioTechnology(this.mPhone.getServiceState());
        log("dataRadioTech = " + dataRadioTech);
        return ServiceStateEx.isLte(dataRadioTech);
    }

    public boolean isCTLteNetwork() {
        int dataRadioTech = ServiceStateEx.getRilDataRadioTechnology(this.mPhone.getServiceState());
        log("dataRadioTech = " + dataRadioTech);
        if (dataRadioTech == EVENT_DATA_CONNECTED || dataRadioTech == EVENT_RADIO_OFF_OR_NOT_AVAILABLE) {
            return true;
        }
        return false;
    }

    public boolean isNRNetwork() {
        if (this.mPhone.getServiceState() == null) {
            return false;
        }
        int dataRadioTech = ServiceStateEx.getRilDataRadioTechnology(this.mPhone.getServiceState());
        log("isNRNetwork dataRadioTech = " + dataRadioTech);
        if (dataRadioTech == 20) {
            return true;
        }
        return false;
    }

    public void updateApnId() {
        String preferredApnIdSlot;
        ContentResolver cr = this.mPhone.getContext().getContentResolver();
        if (IS_MULTI_SIM_ENABLED) {
            preferredApnIdSlot = PREFERRED_APN_ID + (getPrimarySlot() == this.mPhone.getPhoneId() ? "4gSlot" : "2gSlot");
        } else {
            preferredApnIdSlot = PREFERRED_APN_ID;
        }
        try {
            String lastApnId = Settings.System.getString(cr, preferredApnIdSlot);
            this.mCurrentState = getCurState();
            log("updateApnId:LastApnId: " + lastApnId + ", CurrentState: " + this.mCurrentState + ", preferredApnIdSlot: " + preferredApnIdSlot);
            if (lastApnId != null) {
                String[] apIds = lastApnId.split(",");
                ApnSetting curPreferApn = this.mDcTracker.getPreferredApn();
                StringBuffer tempApnId = new StringBuffer(0);
                if (apIds.length != 5 || apIds[this.mCurrentState] == null) {
                    Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
                } else {
                    if (curPreferApn == null) {
                        log("updateApnId:CurPreApn: CurPreApn == null");
                        apIds[this.mCurrentState] = "0";
                    } else {
                        log("updateApnId:CurPreApn: " + curPreferApn + ", CurPreApnId: " + Integer.toString(curPreferApn.getId()));
                        apIds[this.mCurrentState] = Integer.toString(curPreferApn.getId());
                    }
                    for (int i = 0; i < apIds.length; i++) {
                        tempApnId.append(apIds[i]);
                        if (i != apIds.length - 1) {
                            tempApnId.append(",");
                        }
                    }
                    Settings.System.putString(cr, preferredApnIdSlot, tempApnId.toString());
                }
                return;
            }
            Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        } catch (Exception e) {
            log("updateApnId got exception");
            Settings.System.putString(cr, preferredApnIdSlot, this.mDefaultApnId);
        }
    }

    public boolean needSetCTProxy(ApnSetting apn) {
        if (!isCTSimCard(this.mPhone.getPhoneId())) {
            return false;
        }
        String networkOperatorNumeric = this.mPhone.getServiceState().getOperatorNumeric();
        if (apn == null || apn.getApnName() == null || !apn.getApnName().contains(CT_NOT_ROAMING_APN_PREFIX) || !"46012".equals(networkOperatorNumeric)) {
            return false;
        }
        return true;
    }

    public void setCtProxy(DataConnectionEx dc) {
        if (dc == null) {
            loge("setCtProxy return for invalid input.");
            return;
        }
        try {
            List<String> exclList = new ArrayList<>(0);
            exclList.add("127.0.0.1");
            dc.setLinkPropertiesHttpProxy(ProxyInfo.buildDirectProxy("10.0.0.200", Integer.parseInt("80"), exclList));
        } catch (NumberFormatException e) {
            log("onDataSetupComplete: NumberFormatException making ProxyProperties for CT.");
        }
    }

    private void onVpStatusChanged(AsyncResultEx ar) {
        log("onVpStatusChanged");
        if (ar.getException() != null) {
            log("Exception occurred, failed to report the rssi and ecio.");
            return;
        }
        int vpStatus = ((Integer) ar.getResult()).intValue();
        this.mDcTracker.setVpStatus(vpStatus);
        log("onVpStatusChanged, mVpStatus:" + vpStatus);
        if (vpStatus == 1) {
            onVpStarted();
        } else {
            onVpEnded();
        }
    }

    private void onVpStarted() {
        log("onVpStarted");
        ServiceStateTrackerEx sstEx = this.mPhone.getServiceStateTracker();
        if (sstEx != null) {
            sstEx.setCurrent3GPsCsAllowed(false);
            if (this.mDcTracker.isConnected() && !sstEx.isConcurrentVoiceAndDataAllowed() && this.mIsInVoiceCall) {
                log("onVpStarted stop polling");
                this.mDcTracker.stopNetStatPoll();
                this.mDcTracker.stopDataStallAlarm();
                this.mDcTracker.notifyDataConnection();
            }
        }
    }

    public void onVpEnded() {
        log("onVpEnded");
        if (!this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            this.mPhone.getServiceStateTracker().setCurrent3GPsCsAllowed(true);
            if (this.mDcTracker.isConnected() && this.mIsInVoiceCall) {
                this.mDcTracker.startNetStatPoll();
                this.mDcTracker.startDataStallAlarm(false);
                if (!this.mDcTracker.isDataEnabled()) {
                    this.mDcTracker.cleanUpAllConnections("dataDisabledInternal");
                } else {
                    this.mDcTracker.notifyDataConnection();
                }
            }
        }
    }

    public ArrayList<ApnSetting> buildWaitingApnsForCTSupl(String requestedApnType, int radioTech) {
        ArrayList<ApnSetting> apnList = new ArrayList<>(0);
        List<ApnSetting> apnSettings = this.mDcTracker.getAllApnList();
        if (apnSettings != null && !apnSettings.isEmpty()) {
            int apnSize = this.mDcTracker.getAllApnList().size();
            for (int i = 0; i < apnSize; i++) {
                ApnSetting apn = (ApnSetting) this.mDcTracker.getAllApnList().get(i);
                int bearerBitmask = ServiceStateEx.convertNetworkTypeBitmaskToBearerBitmask(apn.getNetworkTypeBitmask());
                if (ApnSettingHelper.canHandleType(apn, ApnSettingHelper.getApnTypesBitmaskFromString(requestedApnType)) && ((!isCTLteNetwork() && !isNRNetwork() && ServiceStateEx.bitmaskHasTech(bearerBitmask, radioTech)) || ((isCTLteNetwork() || isNRNetwork()) && (ServiceStateEx.bitmaskHasTech(bearerBitmask, (int) EVENT_DATA_CONNECTED) || ServiceStateEx.bitmaskHasTech(bearerBitmask, (int) EVENT_RADIO_OFF_OR_NOT_AVAILABLE))))) {
                    if ((!TelephonyManagerEx.isNetworkRoaming(this.mPhone.getSubId()) || !CTNET.equals(apn.getApnName())) && (TelephonyManagerEx.isNetworkRoaming(this.mPhone.getSubId()) || !"ctwap".equals(apn.getApnName()))) {
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

    public boolean isSupportLTE(ApnSetting apnSettings) {
        if (apnSettings == null) {
            loge("isSupportLTE input null, return false for CT apn not support LTE.");
            return false;
        } else if (!isApnPreset(apnSettings)) {
            return true;
        } else {
            int bearerBitmask = ServiceStateEx.convertBearerBitmaskToNetworkTypeBitmask(apnSettings.getNetworkTypeBitmask());
            if (bearerBitmask == 0) {
                return false;
            }
            if (ServiceStateEx.bitmaskHasTech(bearerBitmask, (int) EVENT_DATA_CONNECTED) || ServiceStateEx.bitmaskHasTech(bearerBitmask, (int) EVENT_RADIO_OFF_OR_NOT_AVAILABLE)) {
                return true;
            }
            return false;
        }
    }

    public void updateApnContextState() {
        this.mDcTracker.updateApnFromRetryToIdle();
    }

    public void updateForVSim() {
        log("vsim update sub = " + this.mPhone.getSubId());
        this.mDcTracker.unRegisterForAllEvents();
        this.mDcTracker.registerForAllEvents();
        this.mDcTracker.setUserDataEnabled(true);
        this.mDcTracker.update();
    }

    private void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 96);
    }

    public boolean isDisconnectedOrConnecting() {
        return this.mDcTracker.isDisconnectedOrConnecting();
    }

    public void setupDataForSinglePdnArbitration(String reason) {
        log("setupDataForSinglePdn: reason = " + reason + " isDisconnected = " + this.mDcTracker.isDisconnected());
        if (this.mDcTracker.isOnlySingleDcAllowed(ServiceStateEx.getRilDataRadioTechnology(this.mPhone.getServiceState())) && this.mDcTracker.isDisconnected() && !"SinglePdnArbitration".equals(reason)) {
            this.mDcTracker.setupDataOnAllConnectableApns("SinglePdnArbitration");
        }
    }

    public boolean isNeedFilterVowifiMms(ApnSetting apn, String requestedApnType) {
        return "mms".equals(requestedApnType) && (apn != null && ApnSettingHelper.canHandleType(apn, 16777216)) && HuaweiTelephonyConfigs.isHisiPlatform();
    }

    public boolean isBlockSetInitialAttachApn() {
        String plmnsConfig = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "apn_reminder_plmn");
        IccRecordsEx r = this.mPhone.getIccRecords();
        String operator = r != null ? r.getOperatorNumeric() : BuildConfig.FLAVOR;
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(operator)) {
            return false;
        }
        return plmnsConfig.contains(operator);
    }

    public boolean isNeedForceSetup(ApnContextEx apnContext) {
        return apnContext != null && "dataEnabled".equals(apnContext.getReason()) && ServiceStateEx.getVoiceRegState(this.mPhone.getServiceState()) == 0 && USER_FORCE_DATA_SETUP;
    }

    public void clearDefaultLink() {
        DataConnectionEx dc;
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType("default");
        if (apnContext != null && (dc = apnContext.getDataConnection()) != null) {
            dc.clearLink();
        }
    }

    public void resumeDefaultLink() {
        DataConnectionEx dc;
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType("default");
        if (apnContext != null && (dc = apnContext.getDataConnection()) != null) {
            dc.resumeLink();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserSelectOpenService() {
        log("onUserSelectOpenService set apn = bip0");
        List<ApnSetting> allApnSettings = this.mDcTracker.getAllApnList();
        if (allApnSettings != null && allApnSettings.isEmpty()) {
            this.mDcTracker.createAllApnList();
        }
        CommandsInterfaceEx ci = this.mPhone.getCi();
        if (ci != null && ci.getRadioState() == 1) {
            log("onRecordsLoaded: notifying data availability");
            this.mDcTracker.notifyDataConnection();
        }
        this.mDcTracker.setupDataOnAllConnectableApns("SetPSOnlyOK");
    }

    public String getOperatorNumeric() {
        IccRecordsEx r = this.mPhone.getIccRecords();
        String operator = r != null ? r.getOperatorNumeric() : BuildConfig.FLAVOR;
        if (operator == null) {
            operator = BuildConfig.FLAVOR;
        }
        log("getOperatorNumberic - returning from card: " + operator);
        return operator;
    }

    public String getCTOperator(String operator) {
        String newOperator = operator;
        if (isCTSimCard(this.mPhone.getPhoneId())) {
            newOperator = SystemPropertiesEx.get("gsm.national_roaming.apn", "46003");
            log("Select china telecom hplmn: " + newOperator);
        }
        IccRecordsEx record = this.mPhone.getIccRecords();
        IccRecordsEx gsmIccRecords = UiccControllerExt.getInstance().getIccRecords(this.mPhone.getPhoneId(), UiccControllerExt.APP_FAM_3GPP);
        String gsmOperator = gsmIccRecords != null ? gsmIccRecords.getOperatorNumeric() : BuildConfig.FLAVOR;
        String preSpn = record != null ? record.getServiceProviderName() : BuildConfig.FLAVOR;
        String preIccid = SystemPropertiesEx.get("gsm.sim.preiccid_" + this.mPhone.getPhoneId(), BuildConfig.FLAVOR);
        if (!"46003".equals(newOperator)) {
            return newOperator;
        }
        if ("8985231".equals(preIccid) || GC_SPN.equals(preSpn)) {
            log("Hongkong GC card and iccid is: " + preIccid + ",spn is: " + preSpn);
            return GC_MCCMNC;
        } else if (!MO_ICCID_1.equals(preIccid) && !MO_ICCID_2.equals(preIccid)) {
            log("CT card is not GC and MO card.");
            return newOperator;
        } else if (TextUtils.isEmpty(gsmOperator)) {
            log("MO card, gsmOperator is null");
            return null;
        } else if (MO_UNICOM_MCCMNC.equals(gsmOperator)) {
            log("MO card, gsmOperator is UNICOM");
            return MO_UNICOM_MCCMNC;
        } else {
            log("MO card, gsmOperator is not UNICOM");
            return newOperator;
        }
    }

    public void correctApnAuthType(List<ApnSetting> allApnSettings) {
        if (allApnSettings == null || allApnSettings.isEmpty()) {
            log("correctApnAuthType: allApnSettings is empty");
        } else if (this.mDcTracker.isCustCorrectApnAuthOn()) {
            this.mDcTracker.custCorrectApnAuth(allApnSettings);
        } else {
            int size = allApnSettings.size();
            for (int i = 0; i < size; i++) {
                ApnSetting apn = allApnSettings.get(i);
                String username = apn.getUser();
                String password = apn.getPassword();
                int authType = apn.getAuthType();
                if (authType == 1) {
                    if (TextUtils.isEmpty(username)) {
                        authType = 0;
                        password = BuildConfig.FLAVOR;
                        log("authType is pap but username is null, clear all");
                    }
                } else if (authType == 2) {
                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                        authType = 0;
                        username = BuildConfig.FLAVOR;
                        password = BuildConfig.FLAVOR;
                        log("authType is chap but username or password is null, clear all");
                    }
                } else if (authType != 3) {
                    log("ignore other authType.");
                } else if (TextUtils.isEmpty(username)) {
                    authType = 0;
                    password = BuildConfig.FLAVOR;
                    log("authType is pap_chap but username is null, clear all");
                } else if (TextUtils.isEmpty(password)) {
                    authType = 1;
                    log("authType is pap_chap but password is null, tune authType to pap");
                } else {
                    log("ignore other authType.");
                }
                ApnSetting correctApn = ApnSettingHelper.makeApnSettingWithAuth(apn, username, password, authType);
                if (correctApn != null) {
                    ApnSettingHelper.setIsPreset(correctApn, ApnSettingHelper.isPreset(apn));
                    allApnSettings.set(i, correctApn);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentReenableNrAlarm(Intent intent) {
        log("onActionIntentReenableNrAlarm: action = " + intent.getAction());
        Message msg = this.mHandler.obtainMessage(7);
        msg.setData(intent.getExtras());
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentDisableNrAlarm(Intent intent) {
        log("onActionIntentDisableNrAlarm: action = " + intent.getAction());
        Message msg = this.mHandler.obtainMessage(8);
        msg.setData(intent.getExtras());
        this.mHandler.sendMessage(msg);
    }

    private void cancelDisableNrAlarm(ApnContextEx apnContext) {
        if (apnContext != null && apnContext.getDisableNrIntent() != null) {
            log("cancelDisableNrAlarm for apn = " + apnContext.getApnType());
            this.mAlarmManager.cancel(apnContext.getDisableNrIntent());
            apnContext.setDisableNrIntent((PendingIntent) null);
        }
    }

    private void cancelReenableNrAlarm(ApnContextEx apnContext) {
        if (apnContext != null && apnContext.getReenableNrIntent() != null) {
            log("cancelReenableNrAlarm for apn = " + apnContext.getApnType());
            this.mAlarmManager.cancel(apnContext.getReenableNrIntent());
            apnContext.setReenableNrIntent((PendingIntent) null);
        }
    }

    public void startAlarmForReenableNr(ApnContextEx apnContext, long delay) {
        if (apnContext != null) {
            String apnType = apnContext.getApnType();
            Intent intent = new Intent(INTENT_REENABLE_NR_ALARM);
            int phoneId = this.mPhone.getPhoneId();
            intent.addFlags(268435456);
            intent.putExtra(INTENT_REENABLE_NR_ALARM_EXTRA_TYPE, apnType);
            if (HwVSimUtils.isVSimSub(phoneId)) {
                SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, phoneId, this.mPhone.getSubId());
            } else {
                SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, phoneId);
            }
            log("startAlarmForReenableNr: apn = " + apnContext.getApnType() + " delay=" + delay);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
            apnContext.setReenableNrIntent(alarmIntent);
            this.mDcTracker.cancelReconnectAlarm(apnContext);
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
        }
    }

    public void startAlarmForDisableNr(ApnContextEx apnContext, long delay) {
        if (apnContext == null) {
            loge("startAlarmForDisableNr invalid input.");
            return;
        }
        log("startAlarmForDisableNr: delay = " + delay + "  APN = " + apnContext.getApnType());
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, apnContext.getDisableNrIntent());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public void setNrSaState(ApnContextEx apnContext, boolean isOn) {
        if (apnContext == null) {
            log("setNrSaState: apnContext is null");
        } else if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            Message response = this.mHandler.obtainMessage(EVENT_SET_NR_SA_STATE_DONE);
            response.obj = new Pair(apnContext, Boolean.valueOf(isOn));
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.setNrSaState(isOn, response);
            }
            log("setNrSaState: apnContext=" + apnContext.getApnType() + "new nr sa state=" + (isOn ? 1 : 0));
        }
    }

    private void getNrSaState() {
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            log("getNrSaState from ril");
            CommandsInterfaceEx ci = this.mPhone.getCi();
            if (ci != null) {
                ci.getNrSaState(this.mHandler.obtainMessage(EVENT_GET_NR_SA_STATE_DONE));
            }
        }
    }

    private void onRilConnected(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            log("onRilConnected: msg not instanceof AsyncResult");
        } else if (ar.getException() == null && IS_HW_DATA_RETRY_MANAGER_ENABLED && this.mHwDataRetryManager != null) {
            log("onRilConnected: sync nr sa state with RIL");
            getNrSaState();
        }
    }

    private void onReenableNrAlarm(Bundle bundle) {
        if (bundle == null) {
            log("onReenableNrAlarm with null bundle");
            return;
        }
        String apnType = bundle.getString(INTENT_REENABLE_NR_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = bundle.getInt("subscription", -1);
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType(apnType);
        if (SubscriptionManager.isValidSubscriptionId(currSubId) && currSubId == phoneSubId && apnContext != null && apnType != null) {
            log("Re-enable nr alarm expired.");
            if (apnContext.getDisableNrIntent() != null) {
                log("Disable nr isnt executed until reenable nr alarm expired, reconnect again.");
                cancelDisableNrAlarm(apnContext);
                apnContext.setReenableNrIntent((PendingIntent) null);
                this.mDcTracker.startAlarmForReconnect(50, apnContext);
            } else if (this.mDcTracker.isPhoneStateIdle()) {
                setNrSaState(apnContext, true);
                resetRetryManager();
            } else {
                log("Re-enable nr alarm expired, phone is not idle, restart alarm.");
                startAlarmForReenableNr(apnContext, 2000);
            }
        }
    }

    public void resetRetryManager() {
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED && this.mHwDataRetryManager != null) {
            log("resetRetryManager.");
            this.mHwDataRetryManager.resetAllRetryStrategy();
        }
    }

    public void restorePreferredRatNr() {
        log("restorePreferredRatNr");
        resetRetryManager();
        for (ApnContextEx apnContext : this.mDcTracker.getApnContextsList()) {
            if (isHwDataRetryManagerApplied(apnContext) && apnContext.getReenableNrIntent() != null) {
                cancelReenableNrAlarm(apnContext);
                if (apnContext.getDisableNrIntent() != null) {
                    log("restorePreferredRatNr: disable nr isnt executed, so just reeconnect");
                    cancelDisableNrAlarm(apnContext);
                    this.mDcTracker.startAlarmForReconnect(50, apnContext);
                    return;
                }
                log("restorePreferredRatNr: reenable nr");
                startAlarmForReenableNr(apnContext, 50);
                return;
            }
        }
    }

    private void onDisableNr(Bundle bundle) {
        log("onDisableNr");
        if (bundle == null) {
            log("onDisableNr with null bundle");
            return;
        }
        String apnType = bundle.getString(INTENT_DISABLE_NR_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = bundle.getInt("subscription", -1);
        if (!SubscriptionManager.isValidSubscriptionId(currSubId) || currSubId != phoneSubId || apnType == null) {
            log("onDisableNr phone id invalid, phoneSubId=" + phoneSubId + " currSubId=" + currSubId + "apnType=" + apnType);
            return;
        }
        ApnContextEx apnContext = this.mDcTracker.getApnContextByType(apnType);
        if (apnContext != null) {
            apnContext.setDisableNrBundle(bundle);
            if (this.mDcTracker.isPhoneStateIdle()) {
                setNrSaState(apnContext, false);
                return;
            }
            log("onDisableNr: phone isnt idle, restart alram for disable nr");
            startAlarmForDisableNr(apnContext, 2000);
        }
    }

    public void sendDisableNr(ApnContextEx apnContext, long delay) {
        if (apnContext == null) {
            loge("sendDisableNr invalid input.");
            return;
        }
        String apnType = apnContext.getApnType();
        Intent intent = new Intent(INTENT_DISABLE_NR_ALARM);
        intent.addFlags(268435456);
        intent.putExtra(INTENT_DISABLE_NR_ALARM_EXTRA_TYPE, apnType);
        intent.putExtra(INTENT_DISABLE_NR_ALARM_EXTRA_TIME, delay);
        int phoneId = this.mPhone.getPhoneId();
        if (HwVSimUtils.isVSimSub(phoneId)) {
            log("sendDisableNr: for vsim.");
            SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, phoneId);
        } else {
            SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, phoneId);
        }
        log("sendDisableNr for apn = " + apnType + " delay = " + delay);
        apnContext.setDisableNrIntent(PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728));
        Message msg = this.mHandler.obtainMessage(8);
        msg.setData(intent.getExtras());
        this.mHandler.sendMessage(msg);
    }

    public void onGetNrSaStateDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(Integer.valueOf(msg.what));
        if (ar == null) {
            log("onGetNrSaStateDone: msg.obj not instanceof AsyncResult");
        } else if (ar.getException() != null) {
            log("onGetNrSaStateDone: ar.exception != null");
        } else {
            ApnContextEx defaultApn = this.mDcTracker.getApnContextByType("default");
            if (defaultApn != null) {
                boolean rilNrSaState = false;
                boolean apNrSaState = defaultApn.getReenableNrIntent() == null;
                int result = 0;
                try {
                    result = Integer.parseInt(ar.getResult().toString());
                } catch (NumberFormatException e) {
                    log("parse result error");
                }
                if (result > 0) {
                    rilNrSaState = true;
                }
                log("onGetNrSaStateDone: apNrSaState=" + apNrSaState + " rilNrSaState=" + rilNrSaState);
                if (apNrSaState && !rilNrSaState) {
                    if (defaultApn.getDisableNrIntent() != null) {
                        this.mAlarmManager.cancel(defaultApn.getDisableNrIntent());
                    }
                    setNrSaState(defaultApn, true);
                }
            }
        }
    }

    private boolean isDataReconnectNeeded(ApnContextEx apnContext) {
        if (apnContext == null || apnContext.getReconnectIntent() == null || this.mDcTracker.getDataRat() == 20 || (apnContext.getState() != ApnContextEx.StateEx.IDLE && apnContext.getState() != ApnContextEx.StateEx.FAILED)) {
            return false;
        }
        return true;
    }

    private void onSetNrSaStateError(ApnContextEx apnContext, boolean nrState, AsyncResultEx ar) {
        if (CommandExceptionEx.isNotProvisionedError(ar.getException())) {
            log("onSetNrSaStateError, operation not supported.");
            return;
        }
        this.mRetryTimes++;
        if (this.mRetryTimes >= 3) {
            this.mRetryTimes = 0;
            log("onSetNrSaStateError: reach max retry times for setNrState = " + nrState);
        } else if (nrState) {
            log("onSetNrSaStateDone, reenable nr failed, retry again 2s later.");
            startAlarmForReenableNr(apnContext, 2000);
        } else {
            log("onSetNrSaStateDone, disable nr failed, retry again 2s later.");
            startAlarmForDisableNr(apnContext, 2000);
        }
    }

    public void onSetNrSaStateDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            log("onSetNrSaStateDone, msg.obj is not instance of AsyncResult.");
            return;
        }
        ApnContextEx apnContext = null;
        boolean nrState = true;
        if (ar.getUserObj() instanceof Pair) {
            Pair pair = (Pair) ar.getUserObj();
            if ((pair.first instanceof ApnContextEx) && (pair.second instanceof Boolean)) {
                apnContext = (ApnContextEx) pair.first;
                nrState = ((Boolean) pair.second).booleanValue();
            }
        }
        if (apnContext == null) {
            log("onSetNrSaStateDone, ar.userObj is not instance of Pair");
        } else if (ar.getException() == null) {
            this.mRetryTimes = 0;
            apnContext.setDisableNrIntent((PendingIntent) null);
            if (nrState) {
                apnContext.setReenableNrIntent((PendingIntent) null);
                if (isDataReconnectNeeded(apnContext)) {
                    this.mDcTracker.startAlarmForReconnect(5000, apnContext);
                }
                this.mPhone.sendIntentWhenReenableNr();
                return;
            }
            Bundle bundle = apnContext.getDisableNrBundle();
            this.mPhone.sendIntentWhenDisableNr(apnContext.getFailCause(), bundle == null ? 0 : bundle.getLong(INTENT_DISABLE_NR_ALARM_EXTRA_TIME));
        } else {
            onSetNrSaStateError(apnContext, nrState, ar);
        }
    }

    private boolean isHwDataRetryManagerApplied(ApnContextEx apnContext) {
        if (apnContext == null || !IS_HW_DATA_RETRY_MANAGER_ENABLED || this.mHwDataRetryManager == null || !"default".equals(apnContext.getApnType())) {
            return false;
        }
        log("HwDataRetryManager is Applied for " + apnContext.getApnType());
        return true;
    }

    public void updateDataRetryStategy(ApnContextEx apnContext) {
        if (apnContext != null) {
            if (isHwDataRetryManagerApplied(apnContext)) {
                log("updateDataRetryStategy: run data retry manager");
                this.mHwDataRetryManager.updateDataRetryStategy(apnContext);
                if (getDataRetryAction(apnContext) != 0) {
                    apnContext.resetApnPermanentFailedFlag();
                    return;
                }
                return;
            }
            log("updateDataRetryStategy: not satisfied");
            apnContext.setModemSuggestedDelay((long) NO_SUGGESTED_RETRY_DELAY);
        }
    }

    public int getDataRetryAction(ApnContextEx apnContext) {
        if (apnContext == null) {
            loge("getDataRetryAction return for invalid input.");
            return 0;
        } else if (!isHwDataRetryManagerApplied(apnContext)) {
            log("getDataRetryAction:apn=" + apnContext.getApnType() + ",data retry not applied, return no action");
            return 0;
        } else {
            int nextAction = this.mHwDataRetryManager.getDataRetryAction(apnContext);
            log("getDataRetryAction:action=" + nextAction);
            return nextAction;
        }
    }

    public long getDataRetryDelay(long originDelay, ApnContextEx apnContext) {
        if (apnContext == null) {
            return originDelay;
        }
        long delay = originDelay;
        long iotDelayPropTimer = SystemPropertiesEx.getLong(TELECOM_IOT_APN_DELAY, 0);
        if (iotDelayPropTimer > 0) {
            return iotDelayPropTimer;
        }
        if (sIsScreenOn && (IMMEDIATELY_RETRY_FAILCAUSES.contains(Integer.valueOf(apnContext.getFailCause())) || IMMEDIATELY_RETRY_REASONS.contains(apnContext.getReason()))) {
            delay = 300;
            log(apnContext.getReason() + "  " + apnContext.getFailCause() + " reduce the delay time to 300");
        }
        if (getDataRetryAction(apnContext) == 0) {
            return delay;
        }
        if (!isHwDataRetryManagerApplied(apnContext)) {
            log("getDataRetryDelay:apn=" + apnContext.getApnType() + ",data retry not applied, return 0");
            return delay;
        }
        long delay2 = this.mHwDataRetryManager.getDataRetryDelay(apnContext);
        log("getDataRetryDelay:apn=" + apnContext.getApnType() + ",retry delay=" + delay2);
        return delay2;
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached");
        resetRetryManager();
        getNrSaState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDualCardSwitchDone() {
        log("onDualCardSwitchDone");
        ApnContextEx defaultApn = this.mDcTracker.getApnContextByType("default");
        if (defaultApn != null) {
            cancelReenableNrAlarm(defaultApn);
            cancelDisableNrAlarm(defaultApn);
            defaultApn.setDisableNrBundle((Bundle) null);
        }
        resetRetryManager();
    }

    private void onDataConnected() {
        ApnContextEx defaultApn = this.mDcTracker.getApnContextByType("default");
        if (defaultApn == null) {
            loge("onDataConnected: default apn is null, return.");
        } else if (defaultApn.getState() == ApnContextEx.StateEx.CONNECTED) {
            log("onDataConnected: reset retry manager");
            defaultApn.setFailCause(0);
            resetRetryManager();
            if (defaultApn.getDisableNrIntent() != null) {
                log("onDataConnected: canel disable nr operation");
                cancelDisableNrAlarm(defaultApn);
                cancelReenableNrAlarm(defaultApn);
            }
        }
    }

    private void onRadioOffOrNotAvailable() {
        log("onRadioOffOrNotAvailable: restore preferred rat.");
        restorePreferredRatNr();
    }

    private void onDataEnabledChanged(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            loge("onDataEnabledChanged, MSG OBJ is not instance of AsyncResult, return.");
        } else if (ar.getResult() instanceof Pair) {
            Pair p = (Pair) ar.getResult();
            Boolean dataEnabled = true;
            Integer dataEnabledReason = 1;
            if ((p.first instanceof Boolean) && (p.second instanceof Integer)) {
                dataEnabled = (Boolean) p.first;
                dataEnabledReason = (Integer) p.second;
            }
            if (!dataEnabled.booleanValue() && dataEnabledReason.intValue() == 2) {
                log("onDataEnabledChanged: data disabled so restore preferred rat.");
                restorePreferredRatNr();
            }
        }
    }

    private void onDataAttached() {
        if (this.mAttachedApnSettings == null && !IS_ENABLE_APNCURE && isLTENetworks()) {
            fetchAttachedApnSettings();
        }
    }

    private void onDataRatChanged() {
        if (IS_HW_DATA_RETRY_MANAGER_ENABLED && this.mDcTracker.getDataRat() != 0) {
            log("onDataRatChanged: reset retry manager");
            resetRetryManager();
        }
        if (this.mAttachedApnSettings == null && !IS_ENABLE_APNCURE && isLTENetworks()) {
            fetchAttachedApnSettings();
        }
    }

    private void onSimHotPlug(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && ar.getResult() != null && (ar.getResult() instanceof int[]) && ((int[]) ar.getResult()).length > 0 && ((int[]) ar.getResult())[0] == HotplugState.PLUG_OUT.ordinal()) {
            log("onSimHotPlug: sim removed");
            restorePreferredRatNr();
        }
    }

    public void checkOnlyIpv6Cure(ApnContextEx apn) {
        HwDataSelfCure hwDataSelfCure = this.mHwDataSelfCure;
        if (hwDataSelfCure != null) {
            hwDataSelfCure.checkOnlyIpv6Cure(apn);
        }
    }

    public void checkDataSelfCureAfterDisconnect(int failReason) {
        HwDataSelfCure hwDataSelfCure = this.mHwDataSelfCure;
        if (hwDataSelfCure != null) {
            hwDataSelfCure.checkDataSelfCureAfterDisconnect(failReason);
        }
    }

    public boolean isNeedDataCure(int cause, ApnContextEx apn) {
        if (!IS_PDN_REJ_CURE_ENABLE || this.mHwDataSelfCure == null) {
            return false;
        }
        log("datacure subid: " + this.mPhone.getPhoneId());
        return this.mHwDataSelfCure.isNeedDataCure(cause, apn);
    }

    public void setEsmFlag(int esmInfo) {
        HwDataSelfCure hwDataSelfCure;
        if (IS_PDN_REJ_CURE_ENABLE && (hwDataSelfCure = this.mHwDataSelfCure) != null) {
            hwDataSelfCure.setEsmFlag(esmInfo);
        }
    }

    public void updateDataCureProtocol(ApnContextEx apn) {
        if (IS_PDN_REJ_CURE_ENABLE && this.mHwDataSelfCure != null && apn != null) {
            if (!"default".equals(apn.getApnType())) {
                log("checkUpdateApnInfo request type is:" + apn.getApnType());
                return;
            }
            this.mHwDataSelfCure.updateDataCureProtocol(apn);
        }
    }

    public int getDataCureEsmFlag(String operator) {
        HwDataSelfCure hwDataSelfCure;
        if (!IS_PDN_REJ_CURE_ENABLE || (hwDataSelfCure = this.mHwDataSelfCure) == null) {
            return 0;
        }
        return hwDataSelfCure.getDataCureEsmFlag(operator);
    }

    public void resetDataCureInfo(String reason) {
        HwDataSelfCure hwDataSelfCure;
        if (IS_PDN_REJ_CURE_ENABLE && (hwDataSelfCure = this.mHwDataSelfCure) != null) {
            hwDataSelfCure.resetDataCureInfo(reason);
        }
    }

    public ApnSetting getRegApnForCure(ApnSetting apnSetting) {
        return this.mHwDataSelfCure.getRegApnForCure(apnSetting);
    }

    public void sendRestartRadioChr(int subId, int cause) {
        Context context = this.mPhone.getContext();
        if (context != null) {
            logd("sendRestartRadioChr.");
            Intent intent = new Intent(INTENT_DS_RESTART_RADIO);
            intent.putExtra("subscription", subId);
            intent.putExtra("restart_reason", cause);
            context.sendBroadcastAsUser(intent, UserHandleEx.ALL, "com.huawei.android.permission.GET_CHR_DATA");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNetworkRejInfoDone(Message msg) {
        if (IS_PDN_REJ_CURE_ENABLE && this.mHwDataSelfCure != null) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar == null) {
                loge("onNetworkRejInfoDone, MSG OBJ is not instance of AsyncResult, return.");
            } else if (ar.getException() == null) {
                String[] datas = (String[]) ar.getResult();
                int rejectRat = -1;
                int rejectType = -1;
                if (datas.length >= 6 && datas[3] != null && datas[3].length() > 0 && datas[5] != null && datas[5].length() > 0) {
                    try {
                        rejectRat = Integer.parseInt(datas[3]);
                        rejectType = Integer.parseInt(datas[5]);
                    } catch (NumberFormatException e) {
                        log("error parsing NetworkReject fail!");
                    }
                    this.mHwDataSelfCure.handleNetworkRejectInfo(rejectRat, rejectType);
                }
            }
        }
    }

    public void updateCustomizedEsmFlagState(int defaultEsmFlag, int isEsmFlagCustomized) {
        int phoneId = this.mPhone.getPhoneId();
        String propKey = ESMFLAG_CURE_STATE + String.valueOf(phoneId);
        int customizedState = 1;
        if (IS_ENABLE_ESMFLAG_CURE) {
            if (defaultEsmFlag == 1 && isEsmFlagCustomized == 0) {
                if (!this.mDcTracker.isOnlySingleDcAllowed(ServiceStateEx.getRilDataRadioTechnology(this.mPhone.getServiceState()))) {
                    log("mpdn yes");
                    customizedState = 0;
                } else {
                    log("mpdn no");
                }
            }
        } else if (SystemPropertiesEx.get(propKey, DEAULT_ESMCURE_PROP_VALUE).equals(DEAULT_ESMCURE_PROP_VALUE)) {
            return;
        }
        SystemPropertiesEx.set(propKey, String.valueOf(customizedState));
        log("esmflag customized value=" + customizedState);
    }

    public long getLastRadioResetTimestamp() {
        return this.mLastRadioResetTimestamp;
    }

    public String getSimState() {
        return this.mSimState;
    }

    public int notifyBoosterDoRecovery(int event) {
        int ret;
        if (event == 0) {
            this.mDoRecoveryAct = 1;
            return -1;
        } else if (this.mDoRecoveryAct == 0) {
            this.mDoRecoveryAct = 1;
            return -1;
        } else {
            Bundle data = new Bundle();
            int phoneId = this.mPhone.getPhoneId();
            data.putInt("currentPhoneId", phoneId);
            data.putInt("recoveryAct", this.mDoRecoveryAct);
            if (phoneId == 0) {
                ret = DcTrackerEx.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE0, (int) TYPE_DO_RECOVERY_OEM, data);
            } else if (phoneId == 1) {
                ret = DcTrackerEx.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE1, (int) TYPE_DO_RECOVERY_OEM, data);
            } else {
                ret = DcTrackerEx.reportBoosterPara(PKGNAME_TELEPHONY_DATACONNECTION_PHONE2, (int) TYPE_DO_RECOVERY_OEM, data);
            }
            if (ret != 0) {
                log("reportBoosterPara failed, ret=" + ret);
            }
            return ret;
        }
    }

    public ApnContextEx getNrSliceApnContext(NetworkRequestExt networkRequest) {
        if (networkRequest == null) {
            return null;
        }
        ApnContextEx apnContextForNrSlice = this.mApnContextsByTypeFor5GSlice.get(networkRequest.getNetCapability5GSliceType());
        if (apnContextForNrSlice == null) {
            log("Can not get 5G Slice ApnContext");
            return null;
        }
        apnContextForNrSlice.setDnn(networkRequest.getDnn());
        apnContextForNrSlice.setSnssai(networkRequest.getSnssai());
        apnContextForNrSlice.setSscMode(networkRequest.getSscMode());
        apnContextForNrSlice.setPduSessionType(networkRequest.getPduSessionType());
        apnContextForNrSlice.setRouteBitmap(networkRequest.getRouteBitmap());
        return apnContextForNrSlice;
    }

    public void putApnContextFor5GSlice(int sliceIndex, ApnContextEx apnContext) {
        this.mApnContextsByTypeFor5GSlice.put(sliceIndex, apnContext);
    }

    public ApnContextEx getApnContextFor5GSlice(int sliceIndex) {
        return this.mApnContextsByTypeFor5GSlice.get(sliceIndex);
    }

    public boolean hasMatchAllSlice() {
        int size = this.mApnContextsByTypeFor5GSlice.size();
        for (int i = 0; i < size; i++) {
            SparseArray<ApnContextEx> sparseArray = this.mApnContextsByTypeFor5GSlice;
            ApnContextEx apnContextEx = sparseArray.get(sparseArray.keyAt(i));
            if (!(apnContextEx == null || (apnContextEx.getRouteBitmap() & MATCH_ALL) == 0)) {
                return true;
            }
        }
        return false;
    }

    public ApnSetting createSliceApnSetting(ApnSetting apn) {
        if (apn == null) {
            return null;
        }
        return ApnSettingHelper.makeApnSettingForSlice(apn);
    }

    public boolean isApnSettingsSimilar(ApnSetting first, ApnSetting second) {
        if (first == null || second == null) {
            return false;
        }
        boolean similarApnState = false;
        Boolean similarApnSign = (Boolean) HwCfgFilePolicy.getValue("compatible_apn_switch", this.mPhone.getPhoneId(), Boolean.class);
        if (similarApnSign != null) {
            similarApnState = similarApnSign.booleanValue();
        }
        if (!similarApnState) {
            return ApnSettingHelper.isSimilar(first, second);
        }
        if (HuaweiTelephonyConfigs.isMTKPlatform()) {
            return getApnSimilarMtk(first, second);
        }
        return getApnSimilarHisi(first, second);
    }

    private boolean getApnSimilarMtk(ApnSetting first, ApnSetting second) {
        String secondMmsc = null;
        String firstMmsc = first.getMmsc() == null ? null : first.getMmsc().toString();
        if (second.getMmsc() != null) {
            secondMmsc = second.getMmsc().toString();
        }
        if (Objects.equals(first.getApnName(), second.getApnName()) && xorEqualsProtocol(ApnSettingHelper.getProtocolStringFromInt(first.getProtocol()), ApnSettingHelper.getProtocolStringFromInt(second.getProtocol())) && xorEqualsProtocol(ApnSettingHelper.getProtocolStringFromInt(first.getRoamingProtocol()), ApnSettingHelper.getProtocolStringFromInt(second.getRoamingProtocol())) && first.isEnabled() == second.isEnabled() && xorEquals(firstMmsc, secondMmsc) && xorEquals(first.getMmsProxyAddressAsString(), second.getMmsProxyAddressAsString()) && xorEqualsInt(first.getMmsProxyPort(), second.getMmsProxyPort())) {
            return true;
        }
        return false;
    }

    private boolean getApnSimilarHisi(ApnSetting first, ApnSetting second) {
        String secondMmsc = null;
        String firstMmsc = first.getMmsc() == null ? null : first.getMmsc().toString();
        if (second.getMmsc() != null) {
            secondMmsc = second.getMmsc().toString();
        }
        return Objects.equals(first.getApnName(), second.getApnName()) && (first.getAuthType() == second.getAuthType() || -1 == first.getAuthType() || -1 == second.getAuthType()) && Objects.equals(first.getUser(), second.getUser()) && Objects.equals(first.getPassword(), second.getPassword()) && Objects.equals(first.getProxyAddressAsString(), second.getProxyAddressAsString()) && xorEqualsInt(first.getProxyPort(), second.getProxyPort()) && xorEqualsProtocol(ApnSettingHelper.getProtocolStringFromInt(first.getProtocol()), ApnSettingHelper.getProtocolStringFromInt(second.getProtocol())) && xorEqualsProtocol(ApnSettingHelper.getProtocolStringFromInt(first.getRoamingProtocol()), ApnSettingHelper.getProtocolStringFromInt(second.getRoamingProtocol())) && first.isEnabled() == second.isEnabled() && first.getNetworkTypeBitmask() == second.getNetworkTypeBitmask() && ApnSettingHelper.getMtu(first) == ApnSettingHelper.getMtu(second) && xorEquals(firstMmsc, secondMmsc) && xorEquals(first.getMmsProxyAddressAsString(), second.getMmsProxyAddressAsString()) && xorEqualsInt(first.getMmsProxyPort(), second.getMmsProxyPort());
    }

    private boolean xorEqualsInt(int first, int second) {
        return first == -1 || second == -1 || Objects.equals(Integer.valueOf(first), Integer.valueOf(second));
    }

    private boolean xorEquals(String first, String second) {
        return Objects.equals(first, second) || TextUtils.isEmpty(first) || TextUtils.isEmpty(second);
    }

    private boolean xorEqualsProtocol(String first, String second) {
        return Objects.equals(first, second) || ("IPV4V6".equals(first) && ("IP".equals(second) || "IPV6".equals(second))) || (("IP".equals(first) && "IPV4V6".equals(second)) || ("IPV6".equals(first) && "IPV4V6".equals(second)));
    }

    /* JADX INFO: Multiple debug info for r0v6 int: [D('i' int), D('oriLen' int)] */
    public String[] addSliceNetworkConfigStrings(String[] networkConfigStrings) {
        if (networkConfigStrings == null || networkConfigStrings.length == 0) {
            return null;
        }
        for (int i = networkConfigStrings.length - 1; i >= 0; i--) {
            if (networkConfigStrings[i].startsWith(PREFIX_SLICE_CONFIG)) {
                return networkConfigStrings;
            }
        }
        int oriLen = networkConfigStrings.length;
        int sliceLen = SLICE_NETWORK_CONFIG_STRINGS.length;
        String[] ncTemp = new String[(oriLen + sliceLen)];
        System.arraycopy(networkConfigStrings, 0, ncTemp, 0, oriLen);
        System.arraycopy(SLICE_NETWORK_CONFIG_STRINGS, 0, ncTemp, oriLen, sliceLen);
        return ncTemp;
    }

    public int getPropIntParams(String propNames, int defaultValue) {
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            log("getPropIntParams not support HISI!");
            return SystemPropertiesEx.getInt(propNames, defaultValue);
        }
        String strPropValue = Settings.System.getString(this.mPhone.getContext().getContentResolver(), propNames);
        if (strPropValue == null || BuildConfig.FLAVOR.equals(strPropValue)) {
            log("getPropIntParams, Settings.System.getString fail, propNames=" + propNames);
            return SystemPropertiesEx.getInt(propNames, defaultValue);
        }
        try {
            return Integer.valueOf(strPropValue).intValue();
        } catch (NumberFormatException e) {
            log("getPropIntParams, int value Exceptions.");
            return 0;
        }
    }

    public boolean getPropBooleanParams(String propNames, boolean isDefaultValue) {
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            log("getPropBooleanParams not support HISI!");
            return SystemPropertiesEx.getBoolean(propNames, isDefaultValue);
        }
        String propValue = Settings.System.getString(this.mPhone.getContext().getContentResolver(), propNames);
        if (propValue != null && !BuildConfig.FLAVOR.equals(propValue)) {
            return TRUE.equals(propValue);
        }
        log("getPropBooleanParams, Settings.System.getString fail, propNames=" + propNames);
        return SystemPropertiesEx.getBoolean(propNames, isDefaultValue);
    }

    public boolean isHwRadioDataStallEnable() {
        return getPropBooleanParams("ro.config.hw.data.stall.enabled", true);
    }

    public boolean isPdnRejCureEnable() {
        return getPropBooleanParams("ro.config.hw_pdn_rej_data_cure", true);
    }

    public boolean isEnableEsmFlagCure() {
        return getPropBooleanParams("ro.config.hw_hicure.esmflag", false);
    }

    private void getPropValues() {
        IS_PDN_REJ_CURE_ENABLE = isPdnRejCureEnable();
        IS_ENABLE_APNCURE = getPropBooleanParams("ro.config.hw_hicure.apn", false);
        IS_ENABLE_ESMFLAG_CURE = isEnableEsmFlagCure();
        PERMANENT_ERROR_HEAL_PROP = getPropBooleanParams("ro.config.permanent_error_heal", false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentSetNetworkType(Intent intent) {
        restorePreferredRatNr();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String message) {
        RlogEx.i(this.mTag, message);
    }

    private void logv(String message) {
        RlogEx.v(this.mTag, message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String message) {
        RlogEx.i(this.mTag, message);
    }

    private void logi(String message) {
        RlogEx.i(this.mTag, message);
    }

    private void logw(String message) {
        RlogEx.w(this.mTag, message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String message) {
        RlogEx.e(this.mTag, message);
    }

    /* access modifiers changed from: private */
    public static class ApnCureStat {
        private int mAttachSuccCnt;
        private int mEhplmnSuccCnt;
        private int mEnabledFailCnt;
        private int mFailReason;
        private int mSetupSuccCnt;
        private String simOperator;

        static /* synthetic */ int access$3708(ApnCureStat x0) {
            int i = x0.mSetupSuccCnt;
            x0.mSetupSuccCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$3808(ApnCureStat x0) {
            int i = x0.mEhplmnSuccCnt;
            x0.mEhplmnSuccCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$4008(ApnCureStat x0) {
            int i = x0.mEnabledFailCnt;
            x0.mEnabledFailCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$4108(ApnCureStat x0) {
            int i = x0.mAttachSuccCnt;
            x0.mAttachSuccCnt = i + 1;
            return i;
        }

        private ApnCureStat() {
            this.mAttachSuccCnt = 0;
            this.mEhplmnSuccCnt = 0;
            this.mEnabledFailCnt = 0;
            this.mSetupSuccCnt = 0;
            this.mFailReason = 1;
            this.simOperator = BuildConfig.FLAVOR;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetAll() {
            this.mAttachSuccCnt = 0;
            this.mEhplmnSuccCnt = 0;
            this.mEnabledFailCnt = 0;
            this.mSetupSuccCnt = 0;
            this.mFailReason = 1;
            this.simOperator = BuildConfig.FLAVOR;
        }
    }

    class NwModeContentObserver extends ContentObserver {
        NwModeContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            if (HwDcTrackerEx.IS_MULTI_SIM_ENABLED) {
                if (TelephonyManagerEx.getTelephonyProperty(HwDcTrackerEx.this.mPhone.getPhoneId(), "gsm.data.gsm_only_not_allow_ps", HwDcTrackerEx.FALSE).equals(HwDcTrackerEx.FALSE)) {
                    return;
                }
            } else if (!SystemPropertiesEx.getBoolean("gsm.data.gsm_only_not_allow_ps", false)) {
                return;
            }
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.nwMode = HwNetworkTypeUtils.getNetworkModeFromDB(hwDcTrackerEx.mPhone.getContext(), HwDcTrackerEx.this.mPhone.getPhoneId());
            HwDcTrackerEx hwDcTrackerEx2 = HwDcTrackerEx.this;
            hwDcTrackerEx2.log("NwModeChangeObserver onChange nwMode = " + HwDcTrackerEx.this.nwMode);
            if (HwDcTrackerEx.this.nwMode == 1) {
                HwDcTrackerEx.this.mDcTracker.cleanUpAllConnections("nwTypeChanged");
            }
            if (HwDcTrackerEx.this.mNwOldMode == 1 && HwDcTrackerEx.this.mNwOldMode != HwDcTrackerEx.this.nwMode) {
                HwDcTrackerEx.this.mDcTracker.setupDataOnAllConnectableApns("nwTypeChanged");
            }
            HwDcTrackerEx hwDcTrackerEx3 = HwDcTrackerEx.this;
            hwDcTrackerEx3.mNwOldMode = hwDcTrackerEx3.nwMode;
        }
    }

    private class AllowMmsContentObserver extends ContentObserver {
        AllowMmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            boolean z = false;
            int allowMms = Settings.System.getInt(HwDcTrackerEx.this.mPhone.getContext().getContentResolver(), HwDcTrackerEx.ENABLE_ALLOW_MMS, 0);
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            if (allowMms == 1) {
                z = true;
            }
            hwDcTrackerEx.mIsMmsAllowed = z;
        }
    }

    /* access modifiers changed from: private */
    public class PingThread extends Thread {
        private static final int PROCESS_STATUS_FAIL = -1;
        private static final int PROCESS_STATUS_OK = 0;
        private boolean isRecievedPingReply;
        private String pingResultStr;
        private Process process;
        private String serverName;

        private PingThread() {
            this.isRecievedPingReply = false;
            this.process = null;
            this.pingResultStr = BuildConfig.FLAVOR;
            this.serverName = HwDcTrackerEx.this.isInChina() ? "grs.dbankcloud.com" : "www.google.com";
        }

        /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
            return;
         */
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            BufferedReader buf = null;
            HwDcTrackerEx hwDcTrackerEx = HwDcTrackerEx.this;
            hwDcTrackerEx.log("ping thread enter, server name local = " + HwDcTrackerEx.this.isInChina());
            try {
                HwDcTrackerEx.this.log("pingThread begin to ping");
                Runtime runtime = Runtime.getRuntime();
                this.process = runtime.exec("/system/bin/ping -c 1 -W 1 " + this.serverName);
                PingProcessRunner runner = new PingProcessRunner(this.process);
                runner.start();
                runner.join(3000);
                int status = runner.status;
                HwDcTrackerEx hwDcTrackerEx2 = HwDcTrackerEx.this;
                hwDcTrackerEx2.log("pingThread, process.waitFor, status = " + status);
                if (status == 0) {
                    buf = new BufferedReader(new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8));
                    StringBuffer stringBuffer = new StringBuffer(0);
                    while (true) {
                        String line = buf.readLine();
                        if (line == null) {
                            break;
                        }
                        stringBuffer.append(line);
                    }
                    this.pingResultStr = stringBuffer.toString();
                }
                HwDcTrackerEx hwDcTrackerEx3 = HwDcTrackerEx.this;
                hwDcTrackerEx3.log("ping result:" + this.pingResultStr);
                if ((status != 0 || !this.pingResultStr.contains("1 packets transmitted, 1 received")) && HwDcTrackerEx.this.mDcTracker.isPhoneStateIdle()) {
                    this.isRecievedPingReply = false;
                    HwDcTrackerEx.this.mIsDorecoveryTrigger = true;
                    HwDcTrackerEx.this.actionProcess(HwDcTrackerEx.this.mDcTracker.getRecoveryAction());
                    HwDcTrackerEx.this.mDcTracker.sendMessage(HwDcTrackerEx.this.mDcTracker.obtainMessage(270354, (Object) null));
                } else {
                    this.isRecievedPingReply = true;
                }
                HwDcTrackerEx hwDcTrackerEx4 = HwDcTrackerEx.this;
                hwDcTrackerEx4.log("pingThread return is " + this.isRecievedPingReply);
                if (buf != null) {
                    try {
                        buf.close();
                    } catch (IOException e) {
                        HwDcTrackerEx.this.loge("close buffer got IO exception.");
                    }
                }
            } catch (IOException e2) {
                HwDcTrackerEx.this.loge("ping thread IOException.");
                if (0 != 0) {
                    buf.close();
                }
            } catch (InterruptedException e3) {
                HwDcTrackerEx.this.loge("ping thread InterruptedException.");
                if (0 != 0) {
                    buf.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        buf.close();
                    } catch (IOException e4) {
                        HwDcTrackerEx.this.loge("close buffer got IO exception.");
                    }
                }
                throw th;
            }
        }

        private class PingProcessRunner extends Thread {
            private final Process process;
            private int status = -1;

            PingProcessRunner(Process process2) {
                this.process = process2;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                try {
                    this.status = this.process.waitFor();
                } catch (InterruptedException e) {
                    this.status = -1;
                }
            }
        }
    }
}

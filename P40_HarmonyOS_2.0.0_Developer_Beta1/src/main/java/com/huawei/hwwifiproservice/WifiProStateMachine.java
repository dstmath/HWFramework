package com.huawei.hwwifiproservice;

import android.app.ActivityManager;
import android.app.Notification;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.emcom.EmcomManager;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.net.ConnectivityManager;
import android.net.HwNetworkPolicyManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.TrafficStats;
import android.net.booster.HwDataServiceQoeEx;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.wifi.HwHiLog;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwServiceFactory;
import com.android.server.hidata.appqoe.HwAppQoeApkConfig;
import com.android.server.hidata.appqoe.HwAppQoeResourceManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachineUtils;
import com.android.server.wifi.grs.GrsApiManager;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hwwifiproservice.wifipro.WifiProChr;
import com.huawei.hwwifiproservice.wifipro.networkrecommend.NetworkRecommendManager;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class WifiProStateMachine extends StateMachine implements INetworkQosCallBack, INetworksHandoverCallBack, IWifiProUICallBack, IDualBandManagerCallback {
    private static final int ACCESS_SERVER_LETENCY_ONE_SECOND = 1000;
    private static final int ACCESS_SERVER_LETENCY_THIRTY_SECOND = 30000;
    private static final int ACCESS_TYPE = 1;
    private static final String ACCESS_WEB_RECORD_PORTAL = "isPortalAP";
    private static final String ACCESS_WEB_RECORD_REASON = "reason";
    private static final int ACCESS_WEB_RECORD_REASON_INTERNET = 0;
    private static final String ACCESS_WEB_RECORD_SUCC = "succ";
    private static final int AMBR_DL_THRESHOLD = 1024;
    private static final String APK_INFO = "APKInfo";
    private static final String APK_INFO_EXTERN = "APKInfoExtern";
    private static final int APP_NETWORK_QOE_INDEX = 1;
    private static final int APP_NETWORK_QOE_SCORE_INDEX = 3;
    private static final int APP_NETWORK_QOE_SLOW_REASON_INDEX = 2;
    private static final int APP_QOE_BAD = 5;
    private static final int APP_QOE_CONTINOUS_BAD_THREE_TIME = 3;
    private static final int APP_QOE_GOOD = 4;
    private static final int APP_SERVICE_QOE_NOTIFY = 12001;
    private static final boolean AUTO_EVALUATE_SWITCH = false;
    private static final int AVOID_SWITCH_TIMEOUT = 120000;
    private static final int BAD_RSSI_LEVEL = 2;
    private static final int BAD_TIMES_THRESHOLD = 6;
    private static final int BASE = 136168;
    private static final boolean BQE_TEST = false;
    private static final int CELL_2RECOVERY_WIFI = 6;
    private static final int CELL_2STRONG_WIFI = 7;
    private static final int CELL_CHANNEL_QOE_BAD = 5;
    private static final int CELL_CHANNEL_QOE_BAD_CNT = 3;
    private static final int CELL_CHANNEL_QOE_GOOD = 4;
    private static final int CELL_CHANNEL_RTT_THRESHOLD = 150;
    private static final int CELL_SIGNAL_TWO_LEVEL = 2;
    private static final int CELL_TO_WIFI_CNT = 1;
    private static final int CHANNEL_QOE_CONTINOUS_BAD_NINE_TIME = 9;
    private static final int CHANNEL_QOE_CONTINOUS_BAD_THREE_TIME = 3;
    private static final int CHANNEL_QOE_DOWN_LINK_RATE_INDEX = 15;
    private static final int CHANNEL_QOE_HTTP_LATENCY_INDEX = 16;
    private static final int CHANNEL_QOE_INDEX = 5;
    private static final int CHANNEL_QOE_LEVEL_INDEX = 6;
    private static final int CHANNEL_QOE_NUM_INDEX = 4;
    private static final int CHANNEL_QOE_SCORE_INDEX = 8;
    private static final int CHANNEL_QOE_SLOW_REASON_INDEX = 7;
    private static final int CHANNEL_QOE_STRING_LENGTH = 12;
    private static final int CHANNEL_QOE_UP_LINK_RATE_INDEX = 14;
    private static final int CHECK_SMART_CARD_STATE_INTERVEL = 10000;
    private static final int CHECK_WIFI_QOE_TIMEOUT = 120000;
    private static final int CHR_AVAILIABLE_AP_COUNTER = 2;
    private static final int CHR_CHECK_WIFI_HANDOVER_TIMEOUT = 20000;
    private static final int CHR_DUALBAND_LEVEL1 = 0;
    private static final int CHR_DUALBAND_LEVEL2 = 1;
    private static final int CHR_DUALBAND_LEVEL3 = 2;
    private static final int CHR_DUALBAND_LEVEL4 = 3;
    private static final int CHR_DUALBAND_ONLINE_THRESHOLD_TIME = 300000;
    private static final int CHR_DURATION_LEVEL1 = 0;
    private static final int CHR_DURATION_LEVEL2 = 1;
    private static final int CHR_DURATION_LEVEL3 = 2;
    private static final int CHR_DURATION_LEVEL4 = 3;
    private static final int CHR_DURATION_LEVEL5 = 4;
    private static final int CHR_DURATION_LEVEL6 = 5;
    private static final int CHR_ID_DETECT_ALGORITHM_DURATION = 909009142;
    private static final int CHR_ID_DUAL_BAND_EXCEPTION = 909002085;
    private static final int CHR_ID_HANDOVER_BACK_DURATION = 909009141;
    private static final int CHR_ID_HANDOVER_EXCEPTION_NETWORK_QUALITY = 909002083;
    private static final int CHR_ID_WIFI_HANDOVER_TYPE = 909009129;
    private static final int CHR_ID_WIFI_HANDOVER_UNEXPECTED_TYPE = 909009131;
    private static final int CHR_USER_CLOSE_WIFI = 0;
    private static final int CHR_USER_CONNECT_BACK = 2;
    private static final int CHR_USER_FORGET_NETWORK = 3;
    private static final int CHR_USER_SWITCH_BACK = 1;
    private static final long CHR_WIFI_2_CELL_DELAY_TIME_INTERVAL = 20000;
    private static final int CHR_WIFI_HANDOVER_COUNT = 1;
    private static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final String COUNTRY_CODE_CN = "460";
    private static final int CSP_INVISIBILITY = 0;
    private static final int CSP_VISIBILITY = 1;
    private static final boolean DBG = true;
    private static final boolean DDBG = false;
    private static final boolean DEFAULT_WIFI_PRO_ENABLED = false;
    private static final int DELAYED_TIME_LAUNCH_BROWSER = 500;
    private static final int DELAY_EVALUTE_NEXT_AP_TIME = 2000;
    private static final int DELAY_PERIODIC_PORTAL_CHECK_TIME_FAST = 2000;
    private static final int DELAY_PERIODIC_PORTAL_CHECK_TIME_SLOW = 600000;
    private static final int DELAY_SHOW_NONET = 1;
    private static final int DELAY_SHOW_NONET_TIME = 120000;
    private static final int DELAY_START_WIFI_EVALUTE_TIME = 6000;
    private static final int DELAY_TIME_BASE = 2;
    private static final long DELAY_UPLOAD_MS = 120000;
    private static final int DELTA_RSSI = 10;
    private static final int DETECT_SERVER_CNT = 3000;
    private static final int DETECT_SERVER_FAILURE_THREE_TIMES = 3;
    private static final int DETECT_SERVER_FAILURE_TWO_TIMES = 2;
    private static final int DETECT_SERVER_NUM = 1;
    private static final int DETECT_SERVER_PING_PANG_TIME = 60000;
    private static final int DETECT_SERVER_TIMEOUT = 1000;
    private static final int DETECT_SERVER_TIME_INTERVAL_MAX = 60000;
    private static final int DETECT_SERVER_TIME_INTERVAL_MID = 30000;
    private static final int DETECT_SERVER_TIME_INTERVAL_MIN = 15000;
    private static final int DOWNLINK_LIMIT = SystemProperties.getInt("ro.config.network_limit_speed", (int) LIMIT_DEFAULT_VALUE);
    private static final String DSDS_KEY = "dsdsmode";
    private static final int DSDS_V2 = 0;
    private static final int DSDS_V3 = 1;
    private static final int[] DUALBAND_DURATION_INTERVAL = {WifiScanGenieDataBaseImpl.SCAN_GENIE_MAX_RECORD, 5000, 10000};
    private static final int[] DUALBAND_SCAN_COUNT_INTERVAL = {1, 3, 10};
    private static final int[] DUALBAND_TARGET_AP_COUNT_INTERVAL = {1, 5, 10};
    private static final int DURATION_FROM_10_TO_15S = 2;
    private static final int DURATION_FROM_7_TO_10S = 1;
    private static final int DURATION_IN_7S = 0;
    private static final int DURATION_OUT_15S = 3;
    private static final int EVALUATE_ALL_TIMEOUT = 75000;
    private static final int EVALUATE_VALIDITY_TIMEOUT = 120000;
    private static final int EVALUATE_WIFI_CONNECTED_TIMEOUT = 35000;
    private static final int EVALUATE_WIFI_RTT_BQE_INTERVAL = 3000;
    private static final int EVENT_APP_CHANGED = 502;
    private static final int EVENT_BQE_ANALYZE_NETWORK_QUALITY = 136317;
    private static final int EVENT_CALL_STATE_CHANGED = 136201;
    private static final int EVENT_CHECK_AVAILABLE_AP_RESULT = 136176;
    private static final int EVENT_CHECK_MOBILE_QOS_RESULT = 136180;
    private static final int EVENT_CHECK_PORTAL_AUTH_CHECK_RESULT = 136208;
    private static final int EVENT_CHECK_WIFI_INTERNET = 136192;
    private static final int EVENT_CHECK_WIFI_INTERNET_RESULT = 136181;
    private static final int EVENT_CHECK_WIFI_NETWORK = 136219;
    private static final int EVENT_CHECK_WIFI_NETWORK_BACKGROUND = 136211;
    private static final int EVENT_CHECK_WIFI_NETWORK_LATENCY = 136212;
    private static final int EVENT_CHECK_WIFI_NETWORK_RESULT = 136213;
    private static final int EVENT_CHECK_WIFI_NETWORK_STATUS = 136210;
    private static final int EVENT_CHECK_WIFI_NETWORK_STATUS_TIMEOUT = 5000;
    private static final int EVENT_CHR_ALARM_EXPIRED = 136321;
    private static final int EVENT_CHR_CHECK_WIFI_HANDOVER = 136214;
    private static final int EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG = 136375;
    private static final int EVENT_CONFIGURATION_CHANGED = 136197;
    private static final int EVENT_CONFIGURED_NETWORKS_CHANGED = 136308;
    private static final String EVENT_DATA = "eventData";
    private static final int EVENT_DELAY_EVALUTE_NEXT_AP = 136314;
    private static final int EVENT_DELAY_REINITIALIZE_WIFI_MONITOR = 136184;
    private static final int EVENT_DEVICE_SCREEN_OFF = 136206;
    private static final int EVENT_DEVICE_SCREEN_ON = 136170;
    private static final int EVENT_DEVICE_USER_PRESENT = 136207;
    private static final int EVENT_DIALOG_CANCEL = 136183;
    private static final int EVENT_DIALOG_OK = 136182;
    private static final int EVENT_DISPATCH_INTERNET_RESULT = 136224;
    private static final int EVENT_DUALBAND_5GAP_AVAILABLE = 136370;
    private static final int EVENT_DUALBAND_DELAY_RETRY = 136372;
    private static final int EVENT_DUALBAND_NETWROK_TYPE = 136316;
    private static final int EVENT_DUALBAND_RSSITH_RESULT = 136368;
    private static final int EVENT_DUALBAND_SCORE_RESULT = 136369;
    private static final String EVENT_DUALBAND_SWITCH_FINISHED = "DualBandSwitchFinished";
    private static final int EVENT_DUALBAND_WIFI_HANDOVER_RESULT = 136371;
    private static final int EVENT_EMUI_CSP_SETTINGS_CHANGE = 136190;
    private static final int EVENT_EVALUATE_COMPLETE = 136295;
    private static final int EVENT_EVALUATE_INITIATE = 136294;
    private static final int EVENT_EVALUATE_START_CHECK_INTERNET = 136307;
    private static final int EVENT_EVALUATE_VALIDITY_TIMEOUT = 136296;
    private static final int EVENT_EVALUTE_ABANDON = 136305;
    private static final int EVENT_EVALUTE_STOP = 136303;
    private static final int EVENT_EVALUTE_TIMEOUT = 136304;
    private static final int EVENT_GET_WIFI_TCPRX = 136311;
    private static final int EVENT_HTTP_REACHABLE_RESULT = 136195;
    private static final String EVENT_ID = "eventId";
    private static final int EVENT_LAA_STATUS_CHANGED = 136200;
    private static final int EVENT_LAST_EVALUTE_VALID = 136302;
    private static final int EVENT_LAUNCH_BROWSER = 136320;
    private static final int EVENT_LOAD_CONFIG_INTERNET_INFO = 136315;
    private static final int EVENT_MOBILE_CONNECTIVITY = 136175;
    private static final int EVENT_MOBILE_DATA_STATE_CHANGED_ACTION = 136186;
    private static final int EVENT_MOBILE_QOS_CHANGE = 136173;
    private static final int EVENT_MOBILE_RECOVERY_TO_WIFI = 136189;
    private static final int EVENT_MOBILE_SWITCH_DELAY = 136194;
    private static final int EVENT_NETWORK_CONNECTIVITY_CHANGE = 136177;
    private static final int EVENT_NETWORK_USER_CONNECT = 136202;
    private static final int EVENT_NOTIFY_WIFI_LINK_POOR = 136198;
    private static final int EVENT_PERIODIC_PORTAL_CHECK_FAST = 136205;
    private static final int EVENT_PERIODIC_PORTAL_CHECK_SLOW = 136204;
    private static final int EVENT_PORTAL_SELECTED = 136319;
    private static final int EVENT_PROCESS_GRS = 136374;
    private static final int EVENT_RECHECK_SMART_CARD_STATE = 136209;
    private static final int EVENT_RECOVERY_WIFI_AGENT_SCORE = 136223;
    private static final int EVENT_RECOVERY_WLANPRO_SWITCH = 136225;
    private static final int EVENT_REGISTER_APP_QOE = 136220;
    private static final int EVENT_REQUEST_SCAN_DELAY = 136196;
    private static final int EVENT_RETRY_WIFI_TO_WIFI = 136191;
    private static final int EVENT_SCAN_RESULTS_AVAILABLE = 136293;
    private static final String EVENT_SSID_SWITCH_FINISHED = "SsidSwitchFinished";
    private static final int EVENT_START_BQE = 136306;
    private static final int EVENT_SUPPLICANT_STATE_CHANGE = 136297;
    private static final int EVENT_TRY_WIFI_ROVE_OUT = 136199;
    private static final int EVENT_UNREGISTER_CELL_CARD = 136222;
    private static final int EVENT_USER_ROVE_IN = 136193;
    private static final int EVENT_WAIT_CELL_QOE_BETTER = 136221;
    private static final int EVENT_WAIT_CELL_QOE_BETTER_TIMEOUT = 120000;
    private static final int EVENT_WIFIPRO_EVALUTE_STATE_CHANGE = 136298;
    private static final int EVENT_WIFIPRO_WORKING_STATE_CHANGE = 136171;
    private static final int EVENT_WIFI_CHECK_UNKOWN = 136309;
    private static final int EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED = 136203;
    private static final int EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT = 136301;
    private static final int EVENT_WIFI_EVALUTE_TCPRTT_RESULT = 136299;
    private static final int EVENT_WIFI_FIRST_CONNECTED = 136373;
    private static final int EVENT_WIFI_GOOD_INTERVAL_TIMEOUT = 136187;
    private static final int EVENT_WIFI_HANDOVER_WIFI_RESULT = 136178;
    private static final int EVENT_WIFI_NETWORK_QOS_DETECT = 501;
    private static final int EVENT_WIFI_NETWORK_STATE_CHANGE = 136169;
    private static final int EVENT_WIFI_NO_INTERNET_NOTIFICATION = 136318;
    private static final int EVENT_WIFI_P2P_CONNECTION_CHANGED = 136310;
    private static final int EVENT_WIFI_QOS_CHANGE = 136172;
    private static final int EVENT_WIFI_RECOVERY_TIMEOUT = 136188;
    private static final int EVENT_WIFI_RSSI_CHANGE = 136179;
    private static final int EVENT_WIFI_SECURITY_QUERY_TIMEOUT = 136313;
    private static final int EVENT_WIFI_SECURITY_RESPONSE = 136312;
    private static final int EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE = 136300;
    private static final int EVENT_WIFI_STATE_CHANGED_ACTION = 136185;
    private static final int FULLSCREEN = 5;
    private static final int GOOD_LINK_DETECTED = 131874;
    private static final int GRS_DELAY_TIME = 500;
    public static final int HANDOVER_5G_DIFFERENCE_SCORE = 5;
    private static final int HANDOVER_5G_DIRECTLY_RSSI = -70;
    public static final int HANDOVER_5G_DIRECTLY_SCORE = 40;
    public static final int HANDOVER_5G_MAX_RSSI = -45;
    public static final int HANDOVER_5G_SINGLE_RSSI = -65;
    private static final String HANDOVER_BETTER_CNT = "betterCnt";
    private static final String HANDOVER_CNT = "count";
    private static final int HANDOVER_MIN_LEVEL_INTERVAL = 2;
    private static final int HANDOVER_NO_REASON = -1;
    private static final String HANDOVER_OK_CNT = "okCnt";
    private static final String HANDOVER_SUCC_CNT = "succCnt";
    private static final String HANDOVER_TYPE = "type";
    private static final int HAND_OVER_PINGPONG = 2;
    private static final int HARD_SWITCH_TYPE = 1;
    private static final int HILINK_NO_CONFIG = 2;
    private static final int HWEMCOM_REQ_INFO = 605;
    private static final String HWSYNC_DEVICE_CONNECTED_KEY = "huaweishare_device_connected";
    private static final String HW_SYSTEM_SERVER_START = "com.huawei.systemserver.START";
    private static final int ID_WIFI_DUALBAND_DURATION_INFO = 909009137;
    private static final int ID_WIFI_DUALBAND_FAIL_REASON_INFO = 909009135;
    private static final int ID_WIFI_DUALBAND_SCAN_INFO = 909009136;
    private static final int ID_WIFI_DUALBAND_TARGET_AP_INFO = 909009138;
    private static final int ID_WIFI_DUALBAND_TRIGGER_INFO = 909009134;
    private static final int ID_WIFI_HANDOVER_FAIL_INFO = 909009130;
    private static final int ID_WIFI_HANDOVER_REASON_INFO = 909009132;
    private static final String ILLEGAL_BSSID_01 = "any";
    private static final String ILLEGAL_BSSID_02 = "00:00:00:00:00:00";
    private static final String IMS_SERVICE_STATE_BROADCAST_PERMISSION = "com.huawei.ims.permission.GET_IMS_SERVICE_STATE";
    private static final String IMS_SERVICE_STATE_CHANGED = "huawei.intent.action.IMS_SERVICE_STATE_CHANGED";
    private static final String IMS_STATE_REGISTERED = "REGISTERED";
    private static final String IMS_STATE_UNREGISTERED = "UNREGISTERED";
    private static final int INVALID = -1;
    private static final int INVALID_CHR_RCD_TIME = 0;
    private static final int INVALID_LINK_DETECTED = 131875;
    private static final int INVALID_PID = -1;
    private static final int ISCALLING = 6;
    private static final int ISLANDSCAPEMODE = 11;
    private static final int JUDGE_WIFI_FAST_REOPEN_TIME = 30000;
    private static final int KB_IN_BITS = 1000;
    private static final String KEY_EMUI_WIFI_TO_PDP = "wifi_to_pdp";
    private static final int KEY_MOBILE_HANDOVER_WIFI = 2;
    private static final String KEY_SMART_DUAL_CARD_STATE = "persist.sys.smartDualCardState";
    public static final String KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY = "wifipro_manual_connect_ap_configkey";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK = "wifipro_auto_recommend";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK_SAVED_STATE = "wifipro_auto_recommend_saved_state";
    private static final int KEY_WIFI_HANDOVER_MOBILE = 1;
    private static final int LAST_CONNECTED_NETWORK_EXPIRATION_AGE_MILLIS = 10000;
    private static final int LAST_WIFIPRO_DISABLE_EXPIRATION_AGE_MILLIS = 7200000;
    private static final int LIMIT_DEFAULT_VALUE = 3174;
    private static final int LINKSPEED_RX_TH_2G = 27;
    private static final int LINKSPEED_RX_TH_5G = 40;
    private static final int LINKSPEED_TX_TH_2G = 13;
    private static final int LINKSPEED_TX_TH_5G = 27;
    private static final int MAIN_MODEM_ID = 0;
    private static final int MANUAL_WLANPRO_CLOSE = 10;
    private static final String MAPS_LOCATION_FLAG = "hw_higeo_maps_location";
    private static final int MASTER_CELL_CHANNEL_INDEX = 0;
    private static final int MASTER_WIFI_CHANNEL_INDEX = 2;
    private static final int MILLISECONDS_OF_ONE_SECOND = 1000;
    private static final int MOBILE = 0;
    private static final int MOBILE_DATA_INACTIVE = 12;
    private static final int MOBILE_DATA_OFF_SWITCH_DELAY_MS = 3000;
    private static float MOBILE_TRAFFIC_USED_RATE_THRESHOLD = 0.8f;
    private static final String MPLINK_ENABLE_STATE = "isWifiProFromBrainFlag";
    private static final int NEED_RESET_SWITCH_CONTROL = 1;
    private static final int NETWORK_CONNECTIVITY_CHANGE = 1;
    private static final int NETWORK_POOR_2CELL = 1;
    private static final int NETWORK_POOR_2WIFI = 3;
    private static final int NETWORK_POOR_LEVEL_THRESHOLD = 2;
    private static final int[] NORMAL_DURATION_INTERVAL = {SWITCH_DURATION_LEVEL_0, 10000, 15000};
    private static final int[] NORMAL_SCAN_INTERVAL = {15000, 15000, 30000};
    private static final int[] NORMAL_SCAN_MAX_COUNTER = {4, 4, 2};
    private static final int NOT_CONNECT_TO_NETWORK = -1;
    private static final int NO_CANDIDATE_AP = 7;
    private static final int NO_INTERNET_2CELL = 0;
    private static final int NO_INTERNET_2WIFI = 2;
    private static final String NO_INTERNET_TO_CELL_EVENT = "noInterToCellEvent";
    private static final int OOBE_COMPLETE = 1;
    private static final int OTA_CHANNEL_LOAD_INVALID = -1;
    private static final int OTA_CHANNEL_LOAD_MAYBE_BAD_2G = 500;
    private static final int OTA_CHANNEL_LOAD_MAYBE_BAD_5G = 500;
    private static final String PACKAGE_NAME_HOME = "com.huawei.android.launcher";
    private static final String PACKAGE_NAME_SETTINGS = "com.android.settings";
    private static final int PING_PONG_TIME_INTERVAL_MAX = 300000;
    private static final int PING_PONG_TIME_INTERVAL_MIN = 60000;
    private static final int POOR_LINK_DETECTED = 131873;
    private static final int POOR_LINK_RSSI_THRESHOLD = -75;
    private static final int PORTAL_HANDOVER_DELAY_TIME = 15000;
    private static final String PORTAL_STATUS_BAR_TAG = "wifipro_portal_expired_status_bar";
    private static final int QOE_BAD_REASON_OTA_DOWNLINK = 2;
    private static final int QOE_BAD_REASON_OTA_UPLINK = 1;
    private static final int QOE_CONTINOUS_BAD_ONE_TIME = 1;
    private static final int QOE_INFO_UID_INDEX = 0;
    private static final int QOS_LEVEL = 2;
    private static final int QOS_SCORE = 3;
    private static final int[] QUICK_SCAN_INTERVAL = {10000, 10000, 15000};
    private static final int[] QUICK_SCAN_MAX_COUNTER = {20, 20, 10};
    private static final int REASON_APP_QOE_BAD = 11;
    private static final int REASON_CHANNEL_QOE_BAD = 10;
    private static final int REASON_CHANNEL_QUALITY_BAD = 12;
    private static final int REASON_INVALID = -1;
    private static final int REASON_SIGNAL_LEVEL_3_TOP_UID_BAD = 207;
    private static final int REASON_SIGNAL_LEVEL_4_TOP_UID_BAD = 208;
    private static final int RECORDING_SOURCE_VOIP = 7;
    private static final int REGISTER_APP_QOE_MAX_TIME = 6;
    private static final int REGISTER_APP_QOE_TIMEOUT = 10000;
    private static final long REPORT_WIFIPRO_CHR_INTERVAL = 43200000;
    private static final String RESP_CODE_INTERNET_AVAILABLE = "204";
    private static final String RESP_CODE_INTERNET_UNREACHABLE = "599";
    private static final String RESP_CODE_PORTAL = "302";
    private static final int ROAM_SCENE = 1;
    private static final int ROAM_SWITCH_TYPE = 2;
    private static final int SCORE_DIFFERENCE_EXCEEDS_FIVE = 2;
    private static final int SCORE_OVER_FORTY_REASON = 1;
    private static final int SCREEN_OFF = 9;
    private static final int SELF_CURE_RSSI_THRESHOLD = -70;
    private static final int SEND_MESSAGE_DELAY_TIME = 30000;
    private static final String SETTING_SECURE_CONN_WIFI_PID = "wifipro_connect_wifi_app_pid";
    private static final String SETTING_SECURE_VPN_WORK_VALUE = "wifipro_network_vpn_state";
    private static final String SETTING_SECURE_WIFI_NO_INT = "wifi_no_internet_access";
    private static final int SHORT_DISCONNECT_CHECK_TIME = 30000;
    private static final int SIGNAL0_TOP_UID_BAD = 6;
    private static final int SIGNAL0_WEAK_DISCONNECT = 4;
    private static final int SIGNAL1_TOP_UID_BAD = 0;
    private static final int SIGNAL1_WEAK_TO_WIFI = 5;
    private static final int SIGNAL2_TOP_UID_BAD = 1;
    private static final int SIGNAL3_TOP_UID_BAD = 2;
    private static final int SIGNAL4_TOP_UID_BAD = 3;
    private static final int SIGNAL_LEVEL_2 = 2;
    private static final int SIGNAL_LEVEL_3 = 3;
    private static final int SIGNAL_LEVEL_4 = 4;
    private static final int SIM_SLOT_FIRST = 0;
    private static final int SIM_SLOT_INVALID = -1;
    private static final int SIM_SLOT_SECOND = 1;
    private static final int SK_UDP_TX_ERROR = 2;
    private static final int SLAVE_CELL_CHANNEL_INDEX = 1;
    private static final int SLAVE_WIFI_CHANNEL_INDEX = 3;
    private static final String SLOW_INTERNET_TO_CELL_EVENT = "slowInterToCellEvent";
    private static final int STOP_BY_HIDATA = 1;
    private static final int STRATEGY_NO_SWITCH = 0;
    private static final int STRING_BUFFER_LENGTH = 1024;
    private static final int[] STRONG_SIGNAL_DURATION_SECTION = {20, 35, 65, 125, 305};
    private static final int SWITCH_DURATION_LEVEL_0 = 7000;
    private static final int SWITCH_DURATION_LEVEL_1 = 10000;
    private static final int SWITCH_DURATION_LEVEL_2 = 15000;
    private static final String SYSTEM_NAME_HW_DSDS_MODE_STATE = "com.huawei.action.ACTION_HW_DSDS_MODE_STATE";
    private static final int SYSTEM_UID = 1000;
    private static final String SYS_OPER_CMCC = "ro.config.operators";
    private static final String SYS_PROPERT_PDP = "hw_RemindWifiToPdp";
    private static final String TAG = "WiFi_PRO_WifiProStateMachine";
    private static final int TCP_IP = 101;
    private static final int TCP_RTT_MAYBE_BAD = 700;
    private static final int TCP_RX_THRESHOLD_SCREEN_OFF = 5;
    private static final int TCP_RX_THRESHOLD_SCREEN_ON = 3;
    private static final int THE_SAME_AP_REASON = 3;
    private static final int THRESHOD_RSSI = -82;
    private static final int THRESHOD_RSSI_HIGH = -76;
    private static final int THRESHOD_RSSI_LOW = -88;
    private static final int THRESHOLD_GOOD_CELL_QOE = 100;
    private static final int THRESHOLD_GOOD_RSRP = -115;
    private static final String TRIGGER_REASON_APP_QOE_BAD_3 = "APP_QOE_BAD_3";
    private static final String TRIGGER_REASON_CHANNEL_QOE_BAD_3 = "CHANNEL_QOE_BAD_3";
    private static final String TRIGGER_REASON_WIFI_SIGNAL_BAD_4 = "WIFI_SIGNAL_BAD_4";
    private static final int TURN_OFF_MOBILE = 0;
    private static final int TURN_OFF_WIFI = 1;
    private static final int TURN_OFF_WIFI_PRO = 2;
    private static final int TURN_ON_WIFI_PRO = 3;
    private static final String TYPE_APP_QOE_BAD = "APP_QOE_BAD";
    private static final String TYPE_CHANNEL_QOE_BAD = "CHANNEL_QOE_BAD";
    private static final int TYPE_CONNECTED_AND_SCREENON_DURATION = 0;
    private static final int TYPE_NETWORK_SPEED_LIMIT = 3;
    private static final int TYPE_STRONG_SIGNAL_ALGORITHM_BACK = 1;
    private static final int TYPE_STRONG_SIGNAL_USER_BACK = 0;
    private static final int TYPE_USER_PREFERENCE = 1;
    private static final int TYPE_WEAK_SIGNAL_ALGORITHM_BACK = 3;
    private static final int TYPE_WEAK_SIGNAL_USER_BACK = 2;
    private static final int TYPE_WIFI_NOINTERNET_DURATION = 2;
    private static final String TYPE_WIFI_SIGNAL_BAD = "WIFI_SIGNAL_BAD";
    private static final int TYPE_WIFI_SLOW_DURATION = 1;
    private static final int UPGRADE_RSSI_THRESH = 10;
    private static final int USER_SWITCH = 3;
    private static final int USER_UNEXPECTED_WIFI_HANDOVER_TIME = 30000;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    private static final String VEHICLE_STATE_FLAG = "hw_higeo_vehicle_state";
    private static final int VPN_IS_USING = 0;
    private static final int W2WFAIL_2WIFI = 8;
    private static final int W2W_FAILED_TO_WIFI = 8;
    private static final int WAIT_CELL_ACTIVATED_TIMEOUT = 60000;
    private static final int WAIT_CELL_CHANNEL_QOE_READY_COUNT = 3;
    private static final int WAIT_CELL_CHANNEL_READY_TIMEOUT = 2000;
    private static final int WAIT_CELL_UNREGISTER_FIVE_SECOND_TIMEOUT = 5000;
    private static final int WAIT_CELL_UNREGISTER_TWO_SECOND_TIMEOUT = 2000;
    private static final int WAIT_MASTER_CARD_QOE = 136216;
    private static final int WAIT_SLAVE_CARD_QOE = 136217;
    private static final int WAIT_WIFI_CHANNEL_QOE_READY = 4000;
    private static final int WEAK_SIGNAL_2WIFI = 4;
    private static final int[] WEAK_SIGNAL_DURATION_SECTION = {15, 30, WIFI_SCORE_LINK_GOOD, 90, 120};
    private static final int WIFI = 1;
    private static final int WIFI_CHANNEL_QOE_BAD = 5;
    private static final int WIFI_CHANNEL_QOE_GOOD = 4;
    private static final int WIFI_CHANNEL_QOE_GOOD_CNT = 3;
    private static final int WIFI_CHECK_DELAY_TIME = 30000;
    private static final int WIFI_CHECK_UNKNOW_TIMER = 1;
    private static final String WIFI_CSP_DISPALY_STATE = "wifi_csp_dispaly_state";
    private static final int WIFI_DUALBAND_RECORD_MAX_TIME = 180000;
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final int WIFI_GOOD_LINK_MAX_TIME_LIMIT = 1800000;
    private static final String[] WIFI_HANDOVER_5G_AP_NUM_LEVEL_TYPES = {"0-1", "1-5", "5-10", "10"};
    private static final String[] WIFI_HANDOVER_5G_DURA_LEVEL_TYPES = {"0-2s", "2-5s", "5-10s", "10s"};
    private static final String[] WIFI_HANDOVER_5G_SCAN_LEVEL_TYPES = {"0-1s", "1-3s", "3-10s", "10s"};
    private static final String[] WIFI_HANDOVER_CAUSE_TYPES = {"SIGNAL1_TOP_UID_BAD", "SIGNAL2_TOP_UID_BAD", "SIGNAL3_TOP_UID_BAD", "SIGNAL4_TOP_UID_BAD", "SIGNAL0_DISCONNECT", "SIGNAL1_WEAK_TO_WIFI", "SIGNAL0_TOP_UID_BAD"};
    private static final String[] WIFI_HANDOVER_DURAS = {"0-7s", "7-10s", "10-15s", "15s"};
    private static final String[] WIFI_HANDOVER_FAIL_TYPES = {"VPN_IS_USING", "STOP_BY_HIDATA", "HILINK_NO_CONFIG", "USER_SWITCH", "WIFI_REPEATER_MODE", "FULLSCREEN", "ISCALLING", "NO_CANDIDATE_AP", "W2W_FAILED_TO_WIFI", "SCREEN_OFF", "MANUAL_WLANPRO_CLOSE", "ISLANDSCAPEMODE", "MOBILE_DATA_INACTIVE"};
    private static final int WIFI_HANDOVER_MOBILE_TIMER_LIMIT = 4;
    private static final int WIFI_HANDOVER_TIMERS = 2;
    private static final String[] WIFI_HANDOVER_TYPES = {"NOINTERNET2CELL", "NETWORKPOOR2CELL", "NOINTERNET2WIFI", "NETWORKPOOR2WIFI", "WEAKSIGNAL2WIFI", "WIFI_ROAM", "CELL2RECOVERYWIFI", "CELL2STRONGWIFI", "W2WIFIFAIL2WIFI"};
    private static final String[] WIFI_HANDOVER_UNEXPECTED_TYPES = {"USER_CLOSE_WIFI", "USER_SWITCH_BACK", "USER_CONNECT_BACK", "USER_FORGET_NETWORK", "WIFI_PINGPONG_SWITCH", "CHOOSE_NO_SWITCH"};
    private static final int WIFI_NO_INTERNET_DIVISOR = 4;
    private static final int WIFI_NO_INTERNET_MAX = 12;
    private static final int WIFI_REPEATER_MODE = 4;
    private static final int WIFI_REPEATER_OPEN = 1;
    private static final int WIFI_REPEATER_OPEN_GO_WITHOUT_THTHER = 6;
    private static final int WIFI_ROAM = 5;
    private static final int WIFI_SCAN_COUNT = 4;
    private static final int WIFI_SCAN_INTERVAL_MAX = 12;
    private static final int WIFI_SCORE_LINK_BAD = 40;
    private static final int WIFI_SCORE_LINK_GOOD = 60;
    private static final int WIFI_SCORE_NOINTERNET = 20;
    private static final int WIFI_SIGNAL_TWO_LEVEL = 2;
    private static final int WIFI_SWITCH_NEW_ALGORITHM_TIMER_LIMIT = 2;
    private static final String WIFI_SWITCH_REASON = "switchReason";
    private static final long WIFI_SWITCH_RECORD_MAX_TIME = 1209600000;
    private static final String WIFI_SWITCH_TIME_LEVEL = "level";
    private static final int WIFI_TCPRX_STATISTICS_INTERVAL = 5000;
    private static final String WIFI_TO_CELL_CNT = "wifiToCellCnt";
    private static final String WIFI_TO_CELL_SUCC_CNT = "wifiToCellSuccCnt";
    private static final int WIFI_TO_WIFI_THRESHOLD = 3;
    private static final int WIFI_VERYY_INTERVAL_TIME = 30000;
    private static final int WLAN_PLUS_WHITELIST = 1;
    private static boolean isWifiManualEvaluating = false;
    private static boolean isWifiSemiAutoEvaluating = false;
    private static WifiProStateMachine sWifiProStateMachine;
    private long connectStartTime = 0;
    private int detectionNumSlow = 0;
    private boolean hasHandledNoInternetResult = false;
    private boolean[] imsRegisteredState = {false, false};
    private boolean isDialogUpWhenConnected;
    private boolean isMapNavigating;
    private boolean isPeriodicDet = false;
    private boolean isPortalExpired = false;
    private volatile boolean isRecording = false;
    private boolean isVariableInited;
    private boolean isVehicleState;
    private boolean isVerifyWifiNoInternetTimeOut = false;
    private AppQoeInfo mAppQoeInfo;
    private List<String> mAppWhitelists;
    private AudioManager mAudioManager;
    private AudioManager.AudioRecordingCallback mAudioRecordingCallback = new AudioManager.AudioRecordingCallback() {
        /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass1 */

        @Override // android.media.AudioManager.AudioRecordingCallback
        public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.isRecording = wifiProStateMachine.isRecordingInVoip(configs);
        }
    };
    private int mAvailable5GAPAuthType = 0;
    private String mAvailable5GAPBssid = null;
    private String mAvailable5GAPSsid = null;
    private String mBadBssid;
    private String mBadSsid;
    private BroadcastReceiver mBroadcastReceiver;
    private int mCellToWiFiCnt = 0;
    private long mChrDualbandConnectedStartTime = 0;
    private int mChrQosLevelBeforeHandover;
    private long mChrRoveOutStartTime = 0;
    private volatile boolean mChrSwitchToCellSuccFlagInLinkMonitor = false;
    private long mChrWifiDidableStartTime = 0;
    private long mChrWifiDisconnectStartTime = 0;
    private String mChrWifiHandoverType = "";
    private boolean mCloseBySystemui = false;
    private int mConnectWiFiAppPid;
    private ConnectivityManager mConnectivityManager;
    private ContentResolver mContentResolver;
    private Context mContext;
    private WifiInfo mCurrWifiInfo;
    private String mCurrentBssid;
    private int mCurrentRssi;
    private String mCurrentSsid;
    private long mCurrentTime;
    private int mCurrentVerfyCounter;
    private WifiConfiguration mCurrentWifiConfig;
    private int mCurrentWifiLevel;
    private DefaultState mDefaultState = new DefaultState();
    private boolean mDelayedRssiChangedByCalling = false;
    private boolean mDelayedRssiChangedByFullScreen = false;
    private boolean mDisconnectToConnectedState = false;
    private int mDsdsState = 0;
    private String mDualBandConnectApBssid = null;
    private long mDualBandConnectTime;
    private ArrayList<WifiProEstimateApInfo> mDualBandEstimateApList = new ArrayList<>();
    private int mDualBandEstimateInfoSize = 0;
    private HwDualBandManager mDualBandManager;
    private ArrayList<HwDualBandMonitorInfo> mDualBandMonitorApList = new ArrayList<>();
    private int mDualBandMonitorInfoSize = 0;
    private boolean mDualBandMonitorStart = false;
    private int mDuanBandHandoverType = 0;
    private volatile int mEmuiPdpSwichValue;
    private BroadcastReceiver mHMDBroadcastReceiver;
    private IntentFilter mHMDIntentFilter;
    private long mHandoverCellStartTime = 0;
    private long mHandoverCellSuccessTime = 0;
    private int mHandoverFailReason = -1;
    private int mHandoverReason = -1;
    private boolean mHiLinkUnconfig = false;
    private IHwCommBoosterCallback mHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass13 */

        public void callBack(int type, Bundle bundle) {
            HwHiLog.e(WifiProStateMachine.TAG, false, "receive booster callback type " + type, new Object[0]);
            if (bundle == null) {
                HwHiLog.e(WifiProStateMachine.TAG, false, "data is null", new Object[0]);
            } else if (type != 3) {
                HwHiLog.e(WifiProStateMachine.TAG, false, "unexpected event type = " + type, new Object[0]);
            } else {
                WifiProStateMachine.this.handleNetworkSpeedLimit(bundle);
            }
        }
    };
    private HwDualBandBlackListManager mHwDualBandBlackListMgr;
    private HwIntelligenceWiFiManager mHwIntelligenceWiFiManager;
    private BroadcastReceiver mImsStateChangedReceiver = new BroadcastReceiver() {
        /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(WifiProStateMachine.TAG, "intent is null!");
            } else if (!WifiProStateMachine.IMS_SERVICE_STATE_CHANGED.equals(intent.getAction())) {
                Log.e(WifiProStateMachine.TAG, "no need to process IMS_SERVICE_STATE_CHANGED message");
            } else {
                int slotId = intent.getIntExtra("slot", -1);
                boolean isVoWifi = intent.getBooleanExtra("vowifi_state", WifiProStateMachine.DBG);
                String regState = intent.getStringExtra("state");
                if ((slotId == 0 || slotId == 1) && !isVoWifi && (WifiProStateMachine.IMS_STATE_REGISTERED.equals(regState) || WifiProStateMachine.IMS_STATE_UNREGISTERED.equals(regState))) {
                    Log.i(WifiProStateMachine.TAG, "slotId = " + slotId + ", isVoWifi = " + isVoWifi + ", regState = " + regState);
                    WifiProStateMachine.this.imsRegisteredState[slotId] = WifiProStateMachine.IMS_STATE_REGISTERED.equals(regState);
                    return;
                }
                Log.e(WifiProStateMachine.TAG, "invalid slotId or isVoWifi or regState, slotId = " + slotId + ", isVoWifi = " + isVoWifi + ", regState = " + regState);
            }
        }
    };
    private IntentFilter mIntentFilter;
    private boolean mIsAllowEvaluate;
    private boolean mIsBootCompleted = false;
    private boolean mIsChrQosBetterAfterDualbandHandover = false;
    private boolean mIsLimitedSpeed = false;
    private boolean mIsMobileDataEnabled;
    private boolean mIsMplinkStarted = false;
    private boolean mIsNetworkAuthen;
    private boolean mIsP2PConnectedOrConnecting;
    private boolean mIsPortalAp;
    private boolean mIsPrimaryUser;
    private volatile boolean mIsQoeFirstGood = false;
    private boolean mIsRoveOutToDisconn = false;
    private boolean mIsScanedRssiLow;
    private boolean mIsScanedRssiMiddle;
    private boolean mIsSwitchForbiddenFromApp = false;
    private boolean mIsUserHandoverWiFi;
    private boolean mIsUserManualConnectSuccess = false;
    private volatile boolean mIsVpnWorking;
    private boolean mIsWiFiInternetCHRFlag;
    private boolean mIsWiFiNoInternet;
    private boolean mIsWiFiProAutoEvaluateAP;
    private boolean mIsWiFiProEnabled;
    private boolean mIsWifi2CellInStrongSignalEnabled = false;
    private boolean mIsWifiAdvancedChipUser = false;
    private boolean mIsWifiSemiAutoEvaluateComplete;
    private boolean mIsWifiSwitchRobotAlgorithmEnabled = false;
    private boolean mIsWifiproDisableOnReboot = DBG;
    private boolean mIsWifiproInLinkMonitorLast = false;
    private int mLastCSPState = -1;
    private String mLastConnectedSsid = "";
    private int mLastDisconnectedRssi;
    private long mLastDisconnectedTime;
    private long mLastDisconnectedTimeStamp = -1;
    private int mLastHandoverFailReason = -1;
    private long mLastTime;
    private int mLastWifiLevel;
    private long mLastWifiproDisableTime = 0;
    private boolean mLoseInetRoveOut = false;
    private String mManualConnectAp = "";
    private ContentObserver mMapNavigatingStateChangeObserver = new ContentObserver(null) {
        /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass12 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            boolean z = false;
            if (Settings.Secure.getInt(wifiProStateMachine.mContentResolver, WifiProStateMachine.MAPS_LOCATION_FLAG, 0) == 1) {
                z = true;
            }
            wifiProStateMachine.isMapNavigating = z;
            WifiProStateMachine.this.logI("MapNavigating state change, MapNavigating: " + WifiProStateMachine.this.isMapNavigating);
        }
    };
    private ConnectivityManager.NetworkCallback mMasterCardNetworkCallback;
    private int mMasterRsrp;
    private int mMasterSignalLevel;
    private boolean mNeedRetryMonitor;
    private NetworkBlackListManager mNetworkBlackListManager;
    private final Object mNetworkCheckLock = new Object();
    private HwNetworkPropertyChecker mNetworkPropertyChecker = null;
    private NetworkQosMonitor mNetworkQosMonitor = null;
    private String mNewSelect_bssid;
    private int mNotifyWifiLinkPoorReason = -1;
    private int mOpenAvailableAPCounter;
    private boolean mPhoneStateListenerRegisted = false;
    private int mPortalNotificationId = -1;
    private String mPortalUsedUrl = null;
    private PowerManager mPowerManager;
    private volatile int mQoeBadCount = 0;
    private long mQoeFirstGoodTime = 0;
    private volatile int mRegisterAppQoeTime = 0;
    private String mRoSsid = null;
    private boolean mRoveOutStarted = false;
    private List<ScanResult> mScanResultList;
    private ConnectivityManager.NetworkCallback mSlaveCardNetworkCallback;
    private int mSlaveRsrp;
    private int mSlaveSignalLevel;
    private long mStartReportChrTime = 0;
    private StringBuffer mStringBuffer = new StringBuffer((int) MobileQosDetector.DATA_UNIT_1K_BYTE);
    private TelephonyManager mTelephonyManager;
    private String mUserManualConnecConfigKey = "";
    private ContentObserver mVehicleStateChangeObserver = new ContentObserver(null) {
        /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass11 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            boolean z = false;
            if (Settings.Secure.getInt(wifiProStateMachine.mContentResolver, WifiProStateMachine.VEHICLE_STATE_FLAG, 0) == 1) {
                z = true;
            }
            wifiProStateMachine.isVehicleState = z;
            WifiProStateMachine.this.logI("VehicleState state change, VehicleState: " + WifiProStateMachine.this.isVehicleState);
        }
    };
    private boolean mVerfyingToConnectedState = false;
    private WiFiLinkMonitorState mWiFiLinkMonitorState = new WiFiLinkMonitorState();
    private int mWiFiNoInternetReason;
    private WiFiProDisabledState mWiFiProDisabledState = new WiFiProDisabledState();
    private WiFiProEnableState mWiFiProEnableState = new WiFiProEnableState();
    private WiFiProEvaluateController mWiFiProEvaluateController;
    private volatile int mWiFiProPdpSwichValue;
    private WiFiProVerfyingLinkState mWiFiProVerfyingLinkState = new WiFiProVerfyingLinkState();
    private WifiConnectedState mWifiConnectedState = new WifiConnectedState();
    private WifiDisConnectedState mWifiDisConnectedState = new WifiDisConnectedState();
    private long mWifiDualBandStartTime = 0;
    private long mWifiEnableTime;
    private WifiHandover mWifiHandover;
    private long mWifiHandoverStartTime = 0;
    private long mWifiHandoverSucceedTimestamp = 0;
    private WifiManager mWifiManager;
    private ArrayList<String> mWifiProBlacklist = new ArrayList<>();
    private ArrayList<String> mWifiProBlacklistFullScreen = new ArrayList<>();
    private WifiProConfigStore mWifiProConfigStore;
    private WifiProConfigurationManager mWifiProConfigurationManager;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private boolean mWifiProSwitchState = DBG;
    private WifiProUIDisplayManager mWifiProUIDisplayManager;
    private WifiSemiAutoEvaluateState mWifiSemiAutoEvaluateState = new WifiSemiAutoEvaluateState();
    private WifiSemiAutoScoreState mWifiSemiAutoScoreState = new WifiSemiAutoScoreState();
    private WifiStateMachineUtils mWifiStateMachineUtils;
    private int mWifiSwitchReason = 0;
    private int mWifiTcpRxCount;
    private int mWifiToWifiType = 0;
    private AsyncChannel mWsmChannel;
    private WifiProPhoneStateListener phoneStateListener = null;
    private String respCodeChrInfo = "";
    private WifiProChrUploadManager uploadManager;

    static /* synthetic */ String access$12784(WifiProStateMachine x0, Object x1) {
        String str = x0.respCodeChrInfo + x1;
        x0.respCodeChrInfo = str;
        return str;
    }

    static /* synthetic */ int access$17408(WifiProStateMachine x0) {
        int i = x0.mCellToWiFiCnt;
        x0.mCellToWiFiCnt = i + 1;
        return i;
    }

    static /* synthetic */ int access$19408(WifiProStateMachine x0) {
        int i = x0.mCurrentVerfyCounter;
        x0.mCurrentVerfyCounter = i + 1;
        return i;
    }

    static /* synthetic */ int access$35508(WifiProStateMachine x0) {
        int i = x0.mOpenAvailableAPCounter;
        x0.mOpenAvailableAPCounter = i + 1;
        return i;
    }

    /* access modifiers changed from: private */
    public class AppQoeInfo {
        private int mAppQoeBadCnt;
        private int mChannelIndex;
        private int mChannelNum;
        private int mChannelQoeBadCnt;
        private int mMasterCardAmbr;
        private int mMasterCardHttpProbeLatency;
        private int mMasterCellChannelQoeLevel;
        private int mMasterCellChannelQoeScore;
        private int mMasterCellUplinkRate;
        private int mMasterWifiChannelQoeLevel;
        private int mMasterWifiChannelQoeScore;
        private int mMasterWifiChannelQoeSlowReason;
        private int mMasterWifiDownlinkRate;
        private int mMasterWifiRssi;
        private int mMasterWifiUplinkRate;
        private int mMaxPktRatioChannelId;
        private int mNetworkQoeLevel;
        private int mNetworkSlowChannelBadCnt;
        private int mNetworkSlowReason;
        private int mScore;
        private int mSlaveCardAmbr;
        private int mSlaveCardHttpProbeLatency;
        private int mSlaveCellChannelQoeLevel;
        private int mSlaveCellChannelQoeScore;
        private int mSlaveCellUplinkRate;
        private int mSlaveWifiChannelQoeScore;
        private int mUid;
        private int mWifiHttpProbeLatency;

        static /* synthetic */ int access$22208(AppQoeInfo x0) {
            int i = x0.mAppQoeBadCnt;
            x0.mAppQoeBadCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$22408(AppQoeInfo x0) {
            int i = x0.mNetworkSlowChannelBadCnt;
            x0.mNetworkSlowChannelBadCnt = i + 1;
            return i;
        }

        static /* synthetic */ int access$22708(AppQoeInfo x0) {
            int i = x0.mChannelQoeBadCnt;
            x0.mChannelQoeBadCnt = i + 1;
            return i;
        }

        private AppQoeInfo() {
            resetQoeInfo();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void parseQoeInfo(Bundle data) {
            resetQoePara();
            String info = data.getString("networkQoe");
            this.mMaxPktRatioChannelId = data.getInt("maxPktRatioChannelId");
            this.mMasterWifiRssi = data.getInt("masterWifiRssi", 0);
            this.mMasterCardAmbr = data.getInt("masterCardDlAmbr", 0);
            this.mSlaveCardAmbr = data.getInt("slaveCardDlAmbr", 0);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("APP_SERVICE_QOE_NOTIFY Qoe info " + info + " CID " + String.valueOf(this.mMaxPktRatioChannelId) + " mMasterWifiRssi " + String.valueOf(this.mMasterWifiRssi) + " mMasterCardAmbr " + String.valueOf(this.mMasterCardAmbr) + " mSlaveCardAmbr " + String.valueOf(this.mSlaveCardAmbr));
            if (TextUtils.isEmpty(info)) {
                WifiProStateMachine.this.logE("APP_SERVICE_QOE_NOTIFY Qoe info is illegal");
                return;
            }
            String[] result = info.split(",");
            try {
                this.mUid = Integer.valueOf(result[0]).intValue();
                this.mNetworkQoeLevel = Integer.valueOf(result[1]).intValue();
                this.mNetworkSlowReason = Integer.valueOf(result[2]).intValue();
                this.mScore = Integer.valueOf(result[3]).intValue();
                this.mChannelNum = Integer.valueOf(result[4]).intValue();
                getChannelInfo(result);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logE("parseQoeInfo parseInt err" + info);
            }
        }

        private void getChannelInfo(String[] result) {
            for (int i = 0; i < this.mChannelNum; i++) {
                this.mChannelIndex = Integer.valueOf(result[(i * 12) + 5]).intValue();
                int i2 = this.mChannelIndex;
                if (i2 == 0) {
                    this.mMasterCellChannelQoeLevel = Integer.valueOf(result[(i * 12) + 6]).intValue();
                    this.mMasterCellChannelQoeScore = Integer.valueOf(result[(i * 12) + 8]).intValue();
                    this.mMasterCellUplinkRate = Integer.valueOf(result[(i * 12) + 14]).intValue();
                    this.mMasterCardHttpProbeLatency = Integer.valueOf(result[(i * 12) + 16]).intValue();
                } else if (i2 == 1) {
                    this.mSlaveCellChannelQoeLevel = Integer.valueOf(result[(i * 12) + 6]).intValue();
                    this.mSlaveCellChannelQoeScore = Integer.valueOf(result[(i * 12) + 8]).intValue();
                    this.mSlaveCellUplinkRate = Integer.valueOf(result[(i * 12) + 14]).intValue();
                    this.mSlaveCardHttpProbeLatency = Integer.valueOf(result[(i * 12) + 16]).intValue();
                } else if (i2 == 2) {
                    this.mMasterWifiChannelQoeLevel = Integer.valueOf(result[(i * 12) + 6]).intValue();
                    this.mMasterWifiChannelQoeSlowReason = Integer.valueOf(result[(i * 12) + 7]).intValue();
                    this.mMasterWifiChannelQoeScore = Integer.valueOf(result[(i * 12) + 8]).intValue();
                    this.mMasterWifiUplinkRate = Integer.valueOf(result[(i * 12) + 14]).intValue();
                    this.mMasterWifiDownlinkRate = Integer.valueOf(result[(i * 12) + 15]).intValue();
                    this.mWifiHttpProbeLatency = Integer.valueOf(result[(i * 12) + 16]).intValue();
                } else if (i2 == 3) {
                    this.mSlaveWifiChannelQoeScore = Integer.valueOf(result[(i * 12) + 8]).intValue();
                } else {
                    WifiProStateMachine.this.logE("invalid channel index");
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void resetQoeInfo() {
            resetQoePara();
            this.mAppQoeBadCnt = 0;
            this.mChannelQoeBadCnt = 0;
            this.mNetworkSlowChannelBadCnt = 0;
        }

        private void resetQoePara() {
            this.mUid = 0;
            this.mNetworkQoeLevel = 0;
            this.mNetworkSlowReason = 0;
            this.mScore = 0;
            this.mChannelNum = 0;
            this.mChannelIndex = 0;
            this.mMasterWifiChannelQoeSlowReason = 0;
            this.mWifiHttpProbeLatency = 0;
            this.mMasterWifiChannelQoeLevel = 0;
            this.mMasterWifiChannelQoeScore = 0;
            this.mMasterCardHttpProbeLatency = 0;
            this.mSlaveWifiChannelQoeScore = 0;
            this.mSlaveCardHttpProbeLatency = 0;
            this.mMasterCellChannelQoeScore = 0;
            this.mSlaveCellChannelQoeScore = 0;
            this.mMasterCellUplinkRate = 0;
            this.mSlaveCellUplinkRate = 0;
            this.mMasterCellChannelQoeLevel = 0;
            this.mSlaveCellChannelQoeLevel = 0;
            this.mMasterWifiUplinkRate = 0;
            this.mMasterWifiDownlinkRate = 0;
            this.mMaxPktRatioChannelId = 0;
            this.mMasterWifiRssi = 0;
            this.mMasterCardAmbr = 0;
            this.mSlaveCardAmbr = 0;
        }
    }

    private WifiProStateMachine(Context context, Messenger dstMessenger, Looper looper) {
        super("WifiProStateMachine", looper);
        boolean z = false;
        this.mContext = context;
        this.mIsWifiSwitchRobotAlgorithmEnabled = WifiProCommonUtils.isWifiSwitchRobotAlgorithmEnabled();
        this.mIsWifi2CellInStrongSignalEnabled = WifiProCommonUtils.isWifi2CellInStrongSiganalEnabled();
        this.mIsWifiAdvancedChipUser = WifiProCommonUtils.isAdvancedChipUser();
        this.mWsmChannel = new AsyncChannel();
        this.mWsmChannel.connectSync(this.mContext, getHandler(), dstMessenger);
        this.mContentResolver = context.getContentResolver();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        WifiProStatisticsManager.initStatisticsManager(this.mContext, getHandler() != null ? getHandler().getLooper() : null);
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        this.mNetworkBlackListManager = NetworkBlackListManager.getNetworkBlackListManagerInstance(this.mContext);
        this.mWifiProUIDisplayManager = WifiProUIDisplayManager.createInstance(context, this);
        this.mWifiProConfigurationManager = WifiProConfigurationManager.createWifiProConfigurationManager(this.mContext);
        this.mWifiProConfigStore = new WifiProConfigStore(this.mContext, this.mWsmChannel);
        this.mAppWhitelists = this.mWifiProConfigurationManager.getAppWhitelists();
        this.mNetworkQosMonitor = new NetworkQosMonitor(this.mContext, this, dstMessenger, this.mWifiProUIDisplayManager);
        this.mWifiHandover = new WifiHandover(this.mContext, this);
        this.mIsWiFiProEnabled = WifiProCommonUtils.isWifiProSwitchOn(context);
        this.mIsPrimaryUser = ActivityManager.getCurrentUser() == 0 ? true : z;
        logI("UserID =  " + ActivityManager.getCurrentUser() + ", mIsPrimaryUser = " + this.mIsPrimaryUser);
        if (HwWifiProFeatureControl.sWifiProDualBandCtrl) {
            this.mDualBandManager = HwDualBandManager.createInstance(context, this);
        }
        this.mHwDualBandBlackListMgr = HwDualBandBlackListManager.getHwDualBandBlackListMgrInstance();
        this.phoneStateListener = new WifiProPhoneStateListener();
        this.mLastTime = 0;
        this.mCurrentTime = 0;
        this.mAppQoeInfo = new AppQoeInfo();
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        NetworkRecommendManager.getInstance().init(getHandler(), context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IMS_SERVICE_STATE_CHANGED);
        this.mContext.registerReceiver(this.mImsStateChangedReceiver, intentFilter, IMS_SERVICE_STATE_BROADCAST_PERMISSION, null);
        this.uploadManager = WifiProChrUploadManager.getInstance(this.mContext);
        init(context);
        this.mWifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
        this.mStartReportChrTime = SystemClock.elapsedRealtime();
    }

    public static WifiProStateMachine createWifiProStateMachine(Context context, Messenger dstMessenger, Looper looper) {
        if (sWifiProStateMachine == null) {
            sWifiProStateMachine = new WifiProStateMachine(context, dstMessenger, looper);
        }
        sWifiProStateMachine.start();
        return sWifiProStateMachine;
    }

    public static WifiProStateMachine getWifiProStateMachineImpl() {
        return sWifiProStateMachine;
    }

    public NetworkQosMonitor getNetworkQosMonitor() {
        return this.mNetworkQosMonitor;
    }

    /* access modifiers changed from: private */
    public static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        if (Settings.System.getInt(cr, name, def ? 1 : 0) == 1) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        if (Settings.Global.getInt(cr, name, def ? 1 : 0) == 1) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean getSettingsSecureBoolean(ContentResolver cr, String name, boolean def) {
        if (Settings.Secure.getInt(cr, name, def ? 1 : 0) == 1) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static int getSettingsSystemInt(ContentResolver cr, String name, int def) {
        return Settings.System.getInt(cr, name, def);
    }

    private void init(Context context) {
        NetworkQosMonitor networkQosMonitor = this.mNetworkQosMonitor;
        if (networkQosMonitor != null) {
            this.mNetworkPropertyChecker = networkQosMonitor.getNetworkPropertyChecker();
        }
        addState(this.mDefaultState);
        addState(this.mWiFiProDisabledState, this.mDefaultState);
        addState(this.mWiFiProEnableState, this.mDefaultState);
        addState(this.mWifiConnectedState, this.mWiFiProEnableState);
        addState(this.mWiFiProVerfyingLinkState, this.mWiFiProEnableState);
        addState(this.mWifiDisConnectedState, this.mWiFiProEnableState);
        addState(this.mWiFiLinkMonitorState, this.mWiFiProEnableState);
        addState(this.mWifiSemiAutoEvaluateState, this.mWiFiProEnableState);
        addState(this.mWifiSemiAutoScoreState, this.mWifiSemiAutoEvaluateState);
        this.mWiFiProEvaluateController = WiFiProEvaluateController.getInstance(context);
        this.mIsPortalAp = false;
        this.mIsNetworkAuthen = false;
        registerMapNavigatingStateChanges();
        registerVehicleStateChanges();
        registerForSettingsChanges();
        registerForMobileDataChanges();
        registerForMobilePDPSwitchChanges();
        registerNetworkReceiver();
        registerBoosterService();
        registerOOBECompleted();
        registerForVpnSettingsChanges();
        registerForAppPidChanges();
        registerForAPEvaluateChanges();
        registerForManualConnectChanges();
        AudioManager audioManager = this.mAudioManager;
        if (audioManager != null) {
            audioManager.registerAudioRecordingCallback(this.mAudioRecordingCallback, getHandler());
        }
        setInitialState(this.mWiFiProEnableState);
        HwHiLog.d(TAG, false, "System Create WifiProStateMachine Complete!", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRecordingInVoip(List<AudioRecordingConfiguration> configs) {
        if (configs != null && configs.stream().filter($$Lambda$WifiProStateMachine$QCtgg2S_3shjhz4GFkNWGidkG8.INSTANCE).count() > 0) {
            return DBG;
        }
        return false;
    }

    static /* synthetic */ boolean lambda$isRecordingInVoip$0(AudioRecordingConfiguration config) {
        if (config == null || config.getClientAudioSource() != 7) {
            return false;
        }
        return DBG;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void defaulVariableInit() {
        if (!this.isVariableInited) {
            this.mIsMobileDataEnabled = getSettingsGlobalBoolean(this.mContentResolver, "mobile_data", false);
            this.mEmuiPdpSwichValue = getSettingsSystemInt(this.mContentResolver, KEY_EMUI_WIFI_TO_PDP, 1);
            this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, false);
            this.mIsVpnWorking = getSettingsSystemBoolean(this.mContentResolver, SETTING_SECURE_VPN_WORK_VALUE, false);
            if (this.mIsVpnWorking) {
                Settings.System.putInt(this.mContext.getContentResolver(), SETTING_SECURE_VPN_WORK_VALUE, 0);
                this.mIsVpnWorking = false;
            }
            Settings.System.putString(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY, "");
            setWifiEvaluateTag(false);
            this.isVariableInited = DBG;
            logI("Variable Init Complete!");
        }
    }

    private void registerNetworkReceiver() {
        this.mBroadcastReceiver = new NetworkBroadcastReceiver();
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mIntentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mIntentFilter.addAction("com.huawei.hwwifiproservice.hmd.rove.in");
        this.mIntentFilter.addAction("com.huawei.hwwifiproservice.hmd.delete");
        this.mIntentFilter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        this.mIntentFilter.addAction(HW_SYSTEM_SERVER_START);
        this.mIntentFilter.addAction(SYSTEM_NAME_HW_DSDS_MODE_STATE);
        this.mIntentFilter.addAction("com.huawei.wifi.action.FIRST_CHECK_NO_INTERNET_NOTIFICATION");
        this.mIntentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mIntentFilter.addAction("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER");
        this.mIntentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mIntentFilter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
        this.mHMDBroadcastReceiver = new HMDBroadcastReceiver();
        this.mHMDIntentFilter = new IntentFilter();
        this.mHMDIntentFilter.addAction("com.huawei.hwwifiproservice.hmd.rove.in");
        this.mHMDIntentFilter.addAction("com.huawei.hwwifiproservice.hmd.delete");
        this.mHMDIntentFilter.addAction("com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY");
        this.mContext.registerReceiverAsUser(this.mHMDBroadcastReceiver, UserHandle.ALL, this.mHMDIntentFilter, null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateChrToCell(boolean hasInternet) {
        if (this.uploadManager != null) {
            Bundle wifiToCellData = new Bundle();
            if (!hasInternet) {
                this.uploadManager.addChrSsidBundleStat(NO_INTERNET_TO_CELL_EVENT, WIFI_TO_CELL_CNT, wifiToCellData);
            } else {
                this.uploadManager.addChrSsidBundleStat(SLOW_INTERNET_TO_CELL_EVENT, WIFI_TO_CELL_CNT, wifiToCellData);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateChrToCellSucc(boolean hasInternet) {
        if (this.uploadManager != null) {
            Bundle wifiToCellSuccData = new Bundle();
            if (!hasInternet) {
                this.uploadManager.addChrSsidBundleStat(NO_INTERNET_TO_CELL_EVENT, WIFI_TO_CELL_SUCC_CNT, wifiToCellSuccData);
            } else {
                this.uploadManager.addChrSsidBundleStat(SLOW_INTERNET_TO_CELL_EVENT, WIFI_TO_CELL_SUCC_CNT, wifiToCellSuccData);
            }
        }
    }

    /* access modifiers changed from: private */
    public class NetworkBroadcastReceiver extends BroadcastReceiver {
        private NetworkBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                handleNetworkStateChange(intent);
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE, intent);
            } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                if (WifiProStateMachine.this.mWifiManager.getWifiState() == 1) {
                    WifiProStateMachine.this.mUserManualConnecConfigKey = "";
                }
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_SCREEN_ON);
                if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.sendScreenOnEvent();
                }
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_USER_PRESENT);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_SCREEN_OFF);
            } else if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CONFIGURATION_CHANGED, intent);
            } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                WifiProStateMachine.this.handleScanResult();
            } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_SUPPLICANT_STATE_CHANGE, intent);
            } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)) {
                handleNetworkConfigChange(intent);
            } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE, intent);
            } else if (WifiProStateMachine.HW_SYSTEM_SERVER_START.equals(action)) {
                WifiProStateMachine.this.logI("recieve HW_SYSTEM_SERVER_START!!");
                WifiProStateMachine.this.registerBoosterService();
            } else if (WifiProStateMachine.SYSTEM_NAME_HW_DSDS_MODE_STATE.equals(action)) {
                WifiProStateMachine.this.logI("recieve SYSTEM_NAME_HW_DSDS_MODE_STATE!!");
                WifiProStateMachine.this.handleDsdsStateChanged(intent);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                WifiProStateMachine.this.logI("recieve ACTION_BOOT_COMPLETED!!");
                WifiProStateMachine.this.mIsBootCompleted = WifiProStateMachine.DBG;
            } else {
                handleBroadCast(intent);
            }
        }

        private void handleBroadCast(Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                boolean z = false;
                int userID = intent.getIntExtra("android.intent.extra.user_handle", 0);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                if (userID == 0) {
                    z = WifiProStateMachine.DBG;
                }
                wifiProStateMachine.mIsPrimaryUser = z;
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("user has switched,new userID = " + userID);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                WifiProStateMachine.this.handleP2pConnectionChange(intent);
            } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                WifiProStateMachine.this.handleP2pConnectStateChange(intent);
            } else if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_LOAD_CONFIG_INTERNET_INFO, 5000);
            } else if ("com.huawei.wifi.action.FIRST_CHECK_NO_INTERNET_NOTIFICATION".equals(action)) {
                WifiProStateMachine.this.logI("broadcast WifiProCommonDefs.ACTION_FIRST_CHECK_NO_INTERNET_NOTIFICATION received");
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_NO_INTERNET_NOTIFICATION);
            } else if ("com.huawei.wifipro.action.ACTION_PORTAL_USED_BY_USER".equals(intent.getAction())) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_PORTAL_SELECTED);
            }
        }

        private void handleNetworkConfigChange(Intent intent) {
            WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(intent);
            if (intent.getIntExtra("changeReason", -1) == 1) {
                WifiProStateMachine.this.logI("UNEXPECT_SWITCH_EVENT: forgetAp: enter:");
                WifiProStateMachine.this.uploadManager.addChrSsidCntStat("unExpectSwitchEvent", "forgetAp");
                if (SystemClock.elapsedRealtime() - WifiProStateMachine.this.mWifiHandoverSucceedTimestamp < 30000) {
                    WifiProStateMachine.this.uploadChrHandoverUnexpectedTypes(WifiProStateMachine.WIFI_HANDOVER_UNEXPECTED_TYPES[3]);
                }
            }
            if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiSemiAutoScoreState) {
                WifiConfiguration connCfg = null;
                Object objConfig = intent.getParcelableExtra("wifiConfiguration");
                if (objConfig instanceof WifiConfiguration) {
                    connCfg = (WifiConfiguration) objConfig;
                } else {
                    WifiProStateMachine.this.logE("handleNetworkConfigChange:class is not match");
                }
                if (connCfg != null) {
                    int changeReason = intent.getIntExtra("changeReason", -1);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("ssid = " + StringUtilEx.safeDisplaySsid(connCfg.SSID) + ", change reson " + changeReason + ", isTempCreated = " + connCfg.isTempCreated);
                    if (connCfg.isTempCreated && changeReason != 1) {
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.logI("WiFiProDisabledState, forget " + StringUtilEx.safeDisplaySsid(connCfg.SSID));
                        WifiProStateMachine.this.mWifiManager.forget(connCfg.networkId, null);
                    }
                }
            }
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED, intent);
        }

        private void handleNetworkStateChange(Intent intent) {
            NetworkInfo info = null;
            Object objNetworkInfo = intent.getParcelableExtra("networkInfo");
            if (objNetworkInfo instanceof NetworkInfo) {
                info = (NetworkInfo) objNetworkInfo;
            } else {
                WifiProStateMachine.this.logE("handleNetworkStateChange:Class is not match");
            }
            if (info != null) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("handleNetworkStateChange currentState = " + info.getState());
            }
            if (WifiProStateMachine.isWifiEvaluating() && WifiProStateMachine.this.mIsWiFiProEnabled) {
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.mManualConnectAp = Settings.System.getString(wifiProStateMachine2.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY);
                if (!TextUtils.isEmpty(WifiProStateMachine.this.mManualConnectAp)) {
                    WifiProStateMachine.this.logI("ManualConnectedWiFi  AP, ,isWifiEvaluating ");
                    WifiProStateMachine.this.setWifiEvaluateTag(false);
                    WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWiFiProEnableState);
                }
            }
            if (info != null && NetworkInfo.DetailedState.OBTAINING_IPADDR == info.getDetailedState()) {
                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                wifiProStateMachine4.logI("wifi is conencted, WiFiProEnabled = " + WifiProStateMachine.this.mIsWiFiProEnabled + ", VpnWorking " + WifiProStateMachine.this.mIsVpnWorking);
            }
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE, intent);
        }
    }

    private void resetWifiEvaluteInternetType() {
        String str;
        WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
        if (wifiConfiguration != null && (str = this.mCurrentSsid) != null) {
            this.mWifiProConfigStore.updateWifiEvaluateConfig(wifiConfiguration, 1, 0, str);
            this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 0);
            logI("After reset wifi network type, mCurrentSsid = " + StringUtilEx.safeDisplaySsid(this.mCurrentSsid) + ", accessType = " + this.mCurrentWifiConfig.internetAccessType + ", qosLevel = " + this.mCurrentWifiConfig.networkQosLevel);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 82, (Bundle) null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetWifiEvaluteQosLevel() {
        String str;
        WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
        if (wifiConfiguration != null && (str = this.mCurrentSsid) != null) {
            this.mWifiProConfigStore.updateWifiEvaluateConfig(wifiConfiguration, 2, 0, str);
            this.mWiFiProEvaluateController.updateScoreInfoLevel(this.mCurrentSsid, 0);
            logI("After reset wifiqoslevel, mCurrentSsid = " + StringUtilEx.safeDisplaySsid(this.mCurrentSsid) + ", accessType = " + this.mCurrentWifiConfig.internetAccessType + ", qosLevel = " + this.mCurrentWifiConfig.networkQosLevel);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 82, (Bundle) null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResult() {
        if (this.mWifiProUIDisplayManager.mIsNotificationShown && this.mWiFiProEvaluateController.isAccessAPOutOfRange(this.mWifiManager.getScanResults())) {
            this.mWifiProUIDisplayManager.shownAccessNotification(false);
        }
        sendMessage(EVENT_SCAN_RESULTS_AVAILABLE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectStateChange(Intent intent) {
        int p2pState = intent.getIntExtra("extraState", -1);
        if (p2pState == 1 || p2pState == 2) {
            this.mIsP2PConnectedOrConnecting = DBG;
        } else {
            this.mIsP2PConnectedOrConnecting = false;
        }
        sendMessage(EVENT_WIFI_P2P_CONNECTION_CHANGED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectionChange(Intent intent) {
        NetworkInfo p2pNetworkInfo = null;
        Object objNetworkInfo = intent.getParcelableExtra("networkInfo");
        if (objNetworkInfo instanceof NetworkInfo) {
            p2pNetworkInfo = (NetworkInfo) objNetworkInfo;
        } else {
            logE("handleP2pConnectionChange:Class is not match");
        }
        if (p2pNetworkInfo != null) {
            this.mIsP2PConnectedOrConnecting = p2pNetworkInfo.isConnectedOrConnecting();
        }
        if (!this.mIsP2PConnectedOrConnecting) {
            sendMessage(EVENT_WIFI_P2P_CONNECTION_CHANGED);
        }
    }

    /* access modifiers changed from: private */
    public class HMDBroadcastReceiver extends BroadcastReceiver {
        private HMDBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.huawei.hwwifiproservice.hmd.rove.in".equals(action)) {
                WifiProStateMachine.this.logI("ACTION_HIGH_MOBILE_DATA  rove in event received.");
                WifiProStateMachine.this.userHandoverWifi();
                if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseHighMobileDataBtnRiCount();
                }
            } else if ("com.huawei.hwwifiproservice.hmd.delete".equals(action)) {
                WifiProStateMachine.this.logI("ACTION_HIGH_MOBILE_DATA  delete event received, stop notify.");
                if (WifiProStateMachine.this.mNetworkQosMonitor != null) {
                    WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(0);
                }
                if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseUserDelNotifyCount();
                }
            } else if ("com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY".equals(action)) {
                WifiProStateMachine.this.logI("**receive wifi connected concurrently**");
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
            }
        }
    }

    private void unregisterReceiver() {
        BroadcastReceiver broadcastReceiver = this.mBroadcastReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mBroadcastReceiver = null;
        }
        BroadcastReceiver broadcastReceiver2 = this.mHMDBroadcastReceiver;
        if (broadcastReceiver2 != null) {
            this.mContext.unregisterReceiver(broadcastReceiver2);
            this.mHMDBroadcastReceiver = null;
        }
    }

    private void registerOOBECompleted() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                if (WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, "device_provisioned", 0) == 1) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateInitialWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
                }
            }
        });
    }

    private void registerForSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass4 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mIsWiFiProEnabled = WifiProStateMachine.getSettingsSystemBoolean(wifiProStateMachine.mContentResolver, "smart_network_switching", false);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("Wifi pro setting has changed,WiFiProEnabled == " + WifiProStateMachine.this.mIsWiFiProEnabled);
                if (WifiProStateMachine.isWifiEvaluating() && !WifiProStateMachine.this.mIsWiFiProEnabled) {
                    WifiProStateMachine.this.restoreWiFiConfig();
                    WifiProStateMachine.this.setWifiEvaluateTag(false);
                }
                if (!WifiProStateMachine.this.mIsWiFiProEnabled) {
                    WifiProStateMachine.this.uploadManager.addChrSsidCntStat("unExpectSwitchEvent", "closeWifiPro");
                }
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
                WifiProStateMachine.this.logI("OPEN_CLOSE_EVENT ready");
                WifiProStateMachine.this.uploadManager.addChrCntStat("openCloseEvent", "");
            }
        });
    }

    private void registerForMobileDataChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mIsMobileDataEnabled = WifiProStateMachine.getSettingsGlobalBoolean(wifiProStateMachine.mContentResolver, "mobile_data", false);
                if (WifiProStateMachine.this.mIsMobileDataEnabled) {
                    HwWifiConnectivityMonitor.getInstance().notifyHandoverConditionsChangeToEnabled();
                }
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("MobileData has changed,isMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
            }
        });
    }

    private void registerForMobilePDPSwitchChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_EMUI_WIFI_TO_PDP), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass6 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mEmuiPdpSwichValue = WifiProStateMachine.getSettingsSystemInt(wifiProStateMachine.mContentResolver, WifiProStateMachine.KEY_EMUI_WIFI_TO_PDP, 1);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.mWiFiProPdpSwichValue = wifiProStateMachine2.mEmuiPdpSwichValue;
                if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseSelCspSettingChgCount(WifiProStateMachine.this.mWiFiProPdpSwichValue);
                }
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.logI("Mobile PDP setting changed, mWiFiProPdpSwichValue = mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue);
            }
        });
    }

    private void registerForVpnSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SETTING_SECURE_VPN_WORK_VALUE), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass7 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mIsVpnWorking = WifiProStateMachine.getSettingsSystemBoolean(wifiProStateMachine.mContentResolver, WifiProStateMachine.SETTING_SECURE_VPN_WORK_VALUE, false);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("vpn state has changed,mIsVpnWorking == " + WifiProStateMachine.this.mIsVpnWorking);
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.notifyVPNStateChanged(wifiProStateMachine3.mIsVpnWorking);
                if (WifiProStateMachine.this.mIsVpnWorking && WifiProStateMachine.this.getCurrentState() == WifiProStateMachine.this.mWiFiLinkMonitorState && WifiProStateMachine.this.mIsUserManualConnectSuccess) {
                    WifiProStateMachine.this.mIsWifiproInLinkMonitorLast = WifiProStateMachine.DBG;
                }
                if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiDisConnectedState) {
                    if (WifiProStateMachine.this.mIsVpnWorking || WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiConnectedState || !WifiProStateMachine.this.mIsUserManualConnectSuccess || !WifiProStateMachine.this.mIsWifiproInLinkMonitorLast) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                    } else {
                        WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                        wifiProStateMachine4.transitionTo(wifiProStateMachine4.mWiFiLinkMonitorState);
                    }
                }
                if (!WifiProStateMachine.this.mIsVpnWorking) {
                    WifiProStateMachine.this.mIsWifiproInLinkMonitorLast = false;
                }
            }
        });
    }

    private void registerForAppPidChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SETTING_SECURE_CONN_WIFI_PID), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass8 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mConnectWiFiAppPid = WifiProStateMachine.getSettingsSystemInt(wifiProStateMachine.mContentResolver, WifiProStateMachine.SETTING_SECURE_CONN_WIFI_PID, -1);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                StringBuilder sb = new StringBuilder();
                sb.append("current APP name == ");
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                sb.append(wifiProStateMachine3.getAppName(wifiProStateMachine3.mConnectWiFiAppPid));
                wifiProStateMachine2.logI(sb.toString());
            }
        });
    }

    private void registerForAPEvaluateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(KEY_WIFIPRO_RECOMMEND_NETWORK), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass9 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mIsWiFiProAutoEvaluateAP = WifiProStateMachine.getSettingsSecureBoolean(wifiProStateMachine.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_RECOMMEND_NETWORK, false);
            }
        });
    }

    private void registerForManualConnectChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY), false, new ContentObserver(getHandler()) {
            /* class com.huawei.hwwifiproservice.WifiProStateMachine.AnonymousClass10 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mManualConnectAp = Settings.System.getString(wifiProStateMachine.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("mManualConnectAp has change:  " + StringUtilEx.safeDisplaySsid(WifiProStateMachine.this.mManualConnectAp) + ", wifipro state = " + WifiProStateMachine.this.getCurrentState().getName());
                if (!TextUtils.isEmpty(WifiProStateMachine.this.mManualConnectAp)) {
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.mUserManualConnecConfigKey = wifiProStateMachine3.mManualConnectAp;
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetVariables() {
        this.mNetworkQosMonitor.stopBqeService();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mIsWiFiInternetCHRFlag = false;
        this.mWiFiProPdpSwichValue = 0;
        this.mNetworkQosMonitor.stopALLMonitor();
        this.mNetworkQosMonitor.resetMonitorStatus();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = 0;
        this.mDuanBandHandoverType = 0;
        this.mWifiHandover.clearWiFiSameSsidSwitchFlag();
        this.mIsUserHandoverWiFi = false;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = false;
        this.mIsUserManualConnectSuccess = false;
        resetWifiProManualConnect();
        stopDualBandMonitor();
        this.mWifiSwitchReason = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetScanedRssiVariable() {
        this.mIsScanedRssiLow = false;
        this.mIsScanedRssiMiddle = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWifiInternetStateChange(int lenvel) {
        updateWifiInternetStateChange(lenvel, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWifiInternetStateChange(int lenvel, boolean isShowImmediatly) {
        if (!WifiProCommonUtils.isWifiConnectedOrConnecting(this.mWifiManager)) {
            return;
        }
        if (this.mLastWifiLevel != lenvel || isShowImmediatly) {
            this.mLastWifiLevel = lenvel;
            if (-1 == lenvel) {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                Settings.Secure.putString(contentResolver, SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                logI("notificateNetAccessChange, isShowImmediately = " + isShowImmediatly);
                this.mWifiProUIDisplayManager.notificateNetAccessChange(false, isShowImmediatly);
                logI("mIsPortalAp = " + this.mIsPortalAp + ", mIsNetworkAuthen = " + this.mIsNetworkAuthen);
                if (!this.mIsPortalAp || this.mIsNetworkAuthen) {
                    this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, (boolean) DBG, 0, false);
                    this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 2, this.mCurrentSsid);
                    this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 2);
                    return;
                }
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, (boolean) DBG, 1, false);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 3, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 3);
            } else if (6 == lenvel) {
                ContentResolver contentResolver2 = this.mContext.getContentResolver();
                Settings.Secure.putString(contentResolver2, SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                this.mWifiProUIDisplayManager.notificateNetAccessChange((boolean) DBG);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, (boolean) DBG, 1, false);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 3, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 3);
            } else {
                Settings.Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
                this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, false, 0, false);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 4, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 4);
            }
        } else {
            logI("wifi lenvel is not change, don't report, lenvel = " + lenvel);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reSetWifiInternetState() {
        logI("reSetWifiInternetState");
        Settings.Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiCSPState(int state) {
        if (this.mLastCSPState == state) {
            logI("setWifiCSPState state is not change,ignor! mLastCSPState:" + this.mLastCSPState);
            return;
        }
        logI("setWifiCSPState new state = " + state);
        this.mLastCSPState = state;
        Settings.System.putInt(this.mContext.getContentResolver(), WIFI_CSP_DISPALY_STATE, state);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerCallBack() {
        this.mNetworkQosMonitor.registerCallBack(this);
        this.mWifiHandover.registerCallBack(this, this.mNetworkQosMonitor);
        this.mWifiProUIDisplayManager.registerCallBack(this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unRegisterCallBack() {
        this.mNetworkQosMonitor.unRegisterCallBack();
        this.mWifiHandover.unRegisterCallBack();
        this.mWifiProUIDisplayManager.unRegisterCallBack();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWiFiPoorer() {
        StringBuffer stringBuffer = this.mStringBuffer;
        stringBuffer.delete(0, stringBuffer.length());
        StringBuffer stringBuffer2 = this.mStringBuffer;
        stringBuffer2.append("mMasterCellChannelQoeScore = ");
        stringBuffer2.append(this.mAppQoeInfo.mMasterCellChannelQoeScore);
        stringBuffer2.append(", mMasterCardHttpProbeLatency = ");
        stringBuffer2.append(this.mAppQoeInfo.mMasterCardHttpProbeLatency);
        stringBuffer2.append(", mSlaveCellChannelQoeScore = ");
        stringBuffer2.append(this.mAppQoeInfo.mSlaveCellChannelQoeScore);
        stringBuffer2.append(", mSlaveCardHttpProbeLatency = ");
        stringBuffer2.append(this.mAppQoeInfo.mSlaveCardHttpProbeLatency);
        logI(this.mStringBuffer.toString());
        if ((this.mAppQoeInfo.mMasterCardHttpProbeLatency > 0 && this.mAppQoeInfo.mMasterCardHttpProbeLatency <= CELL_CHANNEL_RTT_THRESHOLD) || ((this.mAppQoeInfo.mMasterCardHttpProbeLatency > CELL_CHANNEL_RTT_THRESHOLD && this.mAppQoeInfo.mMasterCardHttpProbeLatency < 1000 && this.mAppQoeInfo.mMasterCellChannelQoeScore > 0 && this.mAppQoeInfo.mMasterCellChannelQoeScore < this.mAppQoeInfo.mMasterWifiChannelQoeScore) || (this.mAppQoeInfo.mMasterCellChannelQoeLevel == 4 && this.mAppQoeInfo.mMasterWifiChannelQoeScore == 0))) {
            logI("setDefaultDataSub Master Card");
            return DBG;
        } else if ((this.mAppQoeInfo.mSlaveCardHttpProbeLatency <= 0 || this.mAppQoeInfo.mSlaveCardHttpProbeLatency > CELL_CHANNEL_RTT_THRESHOLD) && ((this.mAppQoeInfo.mSlaveCellChannelQoeScore <= 0 || this.mAppQoeInfo.mSlaveCellChannelQoeScore >= this.mAppQoeInfo.mMasterWifiChannelQoeScore) && !(this.mAppQoeInfo.mSlaveCellChannelQoeLevel == 4 && this.mAppQoeInfo.mMasterWifiChannelQoeScore == 0))) {
            logI("Keep Master Wifi connection");
            return false;
        } else {
            WifiProCommonUtils.setDefaultDataSub(WifiProCommonUtils.getSlaveCardSubId());
            logI("setDefaultDataSub Slave Card");
            return DBG;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMobileDataConnected() {
        if (5 != this.mTelephonyManager.getSimState() || !this.mIsMobileDataEnabled || isAirModeOn()) {
            return false;
        }
        return DBG;
    }

    private boolean isAirModeOn() {
        Context context = this.mContext;
        if (context != null && Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized boolean isWifiConnected() {
        WifiInfo conInfo;
        if (!this.mWifiManager.isWifiEnabled() || (conInfo = this.mWifiManager.getConnectionInfo()) == null || conInfo.getNetworkId() == -1 || conInfo.getBSSID() == null || "00:00:00:00:00:00".equals(conInfo.getBSSID()) || conInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            return false;
        }
        return DBG;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWifiSignalBad(int rssi) {
        if (rssi == 0) {
            return false;
        }
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            loge("mWifiManager is null");
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            loge("wifiInfo is null");
            return false;
        } else if (WifiProCommonUtils.getSignalLevel(wifiInfo.getFrequency(), rssi) <= 2) {
            return DBG;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyManualConnectAP(boolean isUserManualConnect, boolean isUserHandoverWiFi) {
        Bundle data = new Bundle();
        data.putBoolean("isUserManualConnect", isUserManualConnect);
        data.putBoolean("isUserHandoverWiFi", isUserHandoverWiFi);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 35, data);
        if (isUserManualConnect && this.mWifiHandoverSucceedTimestamp != 0 && SystemClock.elapsedRealtime() - this.mWifiHandoverSucceedTimestamp < 30000) {
            WifiProChrUploadManager wifiProChrUploadManager = this.uploadManager;
            if (wifiProChrUploadManager != null) {
                wifiProChrUploadManager.addChrSsidCntStat("unExpectSwitchEvent", "userRejectSwitch");
            }
            uploadChrHandoverUnexpectedTypes(WIFI_HANDOVER_UNEXPECTED_TYPES[2]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyVPNStateChanged(boolean isVpnConnected) {
        Bundle data = new Bundle();
        data.putBoolean("isVpnConnected", isVpnConnected);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 36, data);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isKeepCurrWiFiConnected() {
        this.mLastHandoverFailReason = this.mHandoverFailReason;
        if (this.mIsVpnWorking) {
            this.mHandoverFailReason = 0;
            logW("vpn is working,should keep current connect");
        }
        if (this.mIsUserManualConnectSuccess && !this.mIsWiFiProEnabled) {
            logW("user manual connect and wifi+ disabled, keep connect and no dialog.");
        }
        if (this.mIsUserHandoverWiFi && !this.mIsWiFiNoInternet) {
            this.mHandoverFailReason = 3;
        }
        if (this.mHiLinkUnconfig) {
            this.mHandoverFailReason = 2;
        }
        if (isWifiRepeaterOn()) {
            this.mHandoverFailReason = 4;
        }
        int i = this.mHandoverFailReason;
        if (i != this.mLastHandoverFailReason) {
            uploadWifiSwitchFailTypeStatistics(i);
            this.mLastHandoverFailReason = this.mHandoverFailReason;
        }
        logW("mIsVpnWorking = " + this.mIsVpnWorking + ", mIsUserHandoverWiFi = " + this.mIsUserHandoverWiFi + ", mIsWiFiNoInternet = " + this.mIsWiFiNoInternet + ", mHiLinkUnconfig = " + this.mHiLinkUnconfig + ", isAppinWhitelists = " + isAppinWhitelists() + ", isWifiRepeaterOn = " + isWifiRepeaterOn() + ", mIsSwitchForbiddenFromApp = " + this.mIsSwitchForbiddenFromApp);
        if (this.mIsVpnWorking || ((this.mIsUserHandoverWiFi && !this.mIsWiFiNoInternet) || this.mHiLinkUnconfig || isAppinWhitelists() || isWifiRepeaterOn() || this.mIsSwitchForbiddenFromApp)) {
            return DBG;
        }
        return false;
    }

    private boolean isWifiRepeaterOn() {
        int state = Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", 0);
        if (1 == state || 6 == state) {
            return DBG;
        }
        return false;
    }

    private boolean isHwSyncClinetConnected() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), HWSYNC_DEVICE_CONNECTED_KEY, 0) != 0) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAllowWiFiAutoEvaluate() {
        if (!this.mIsBootCompleted) {
            return false;
        }
        boolean z = this.mIsWiFiProAutoEvaluateAP;
        if (!this.mIsWiFiProEnabled || this.mIsVpnWorking) {
            return false;
        }
        return DBG;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshConnectedNetWork() {
        if (WifiProCommonUtils.isWifiConnectedOrConnecting(this.mWifiManager)) {
            WifiInfo conInfo = this.mWifiManager.getConnectionInfo();
            this.mCurrWifiInfo = conInfo;
            if (conInfo != null) {
                this.mCurrentBssid = conInfo.getBSSID();
                this.mCurrentSsid = conInfo.getSSID();
                this.mCurrentRssi = conInfo.getRssi();
                List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
                if (configNetworks != null) {
                    for (WifiConfiguration config : configNetworks) {
                        if (config.networkId == conInfo.getNetworkId()) {
                            this.mCurrentWifiConfig = config;
                        }
                    }
                    return;
                }
                return;
            }
        }
        this.mCurrentBssid = null;
        this.mCurrentSsid = null;
        this.mCurrentRssi = WifiHandover.INVALID_RSSI;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAllowWifi2Mobile() {
        int topUid = HwAutoConnectManager.getInstance().getCurrentTopUid();
        if (!this.mIsWiFiProEnabled || !this.mIsPrimaryUser || !isMobileDataConnected() || !this.mPowerManager.isScreenOn() || !HwWifiProFeatureControl.sWifiProToCellularCtrl || this.mEmuiPdpSwichValue == 2 || !isMobileAccessAllowed(this.mContext, topUid)) {
            return false;
        }
        return DBG;
    }

    private boolean isMobileAccessAllowed(Context context, int uid) {
        HwNetworkPolicyManager manager = HwNetworkPolicyManager.from(context);
        if (manager == null) {
            logE("context is null during isMobileAccessAllowed");
            return false;
        }
        int policy = manager.getHwUidPolicy(uid);
        logI("isMobileAccessAllowed uid= " + uid + " policy= " + policy);
        if ((policy & 1) == 0) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPdpAvailable() {
        if ("true".equals(Settings.Global.getString(this.mContext.getContentResolver(), SYS_PROPERT_PDP))) {
            logI("SYS_PROPERT_PDP hw_RemindWifiToPdp is true");
            return DBG;
        }
        logI("SYS_PROPERT_PDP hw_RemindWifiToPdp is false");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getAppName(int pid) {
        Object objManager = this.mContext.getSystemService("activity");
        ActivityManager am = null;
        if (objManager instanceof ActivityManager) {
            am = (ActivityManager) objManager;
        }
        if (am == null) {
            logE("getAppName:class is not match");
            return "";
        }
        List<ActivityManager.RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
        if (appProcessList == null) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    private boolean isAppinWhitelists() {
        List<String> list;
        WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
        if (wifiConfiguration == null) {
            return false;
        }
        String currentAppName = wifiConfiguration.creatorName;
        logI("isAppinWhitelists, currentAppName =  " + currentAppName);
        if (!TextUtils.isEmpty(currentAppName) && (list = this.mAppWhitelists) != null) {
            for (String appName : list) {
                if (currentAppName.equals(appName)) {
                    logI("curr name in the  Whitelists ");
                    return DBG;
                }
            }
        }
        return false;
    }

    private void resetWifiProManualConnect() {
        if (!TextUtils.isEmpty(this.mManualConnectAp)) {
            Settings.System.putString(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT_CONFIGKEY, "");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPortalStatusChanged(boolean popUp, String configKey, boolean hasInternetAccess) {
        Bundle data = new Bundle();
        data.putBoolean("popUp", popUp);
        data.putString("configKey", configKey);
        data.putBoolean("hasInternetAccess", hasInternetAccess);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 33, data);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateWifiConfig(WifiConfiguration config) {
        if (config != null) {
            Bundle data = new Bundle();
            data.putInt("messageWhat", 131672);
            data.putParcelable("messageObj", config);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 28, data);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getReasonType(int reason) {
        if (reason == 10) {
            return 2;
        }
        if (reason == 11) {
            return 1;
        }
        if (reason != 107) {
            return 0;
        }
        return 3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateChrQoeGainStatistics() {
        if (this.mAppQoeInfo == null) {
            Log.e(TAG, "updateChrQoeGainStatistics: mAppQoeInfo is null");
        } else if (this.mChrSwitchToCellSuccFlagInLinkMonitor) {
            WifiProChr.getInstance().setChrSwitchSuccTrafficSum((int) ((TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()) - WifiProChr.getInstance().getChrSwitchSuccStartTraffic()));
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - this.mHandoverCellSuccessTime >= 30000) {
                Log.d(TAG, "updateChrQoeGainStatistics: count qoe only 30s");
                if (!this.mIsQoeFirstGood) {
                    this.mQoeFirstGoodTime = 30000;
                    return;
                }
                return;
            }
            if (!this.mIsQoeFirstGood && (this.mAppQoeInfo.mNetworkQoeLevel == 4 || this.mAppQoeInfo.mNetworkQoeLevel == 0)) {
                this.mQoeFirstGoodTime = currentTime - this.mHandoverCellStartTime;
                this.mIsQoeFirstGood = DBG;
                Log.d(TAG, "updateChrQoeGainStatistics: mQoeFirstGoodTime:" + this.mQoeFirstGoodTime);
            }
            if (this.mAppQoeInfo.mNetworkQoeLevel == 5) {
                this.mQoeBadCount++;
            } else {
                this.mQoeBadCount = 0;
            }
            if (WifiProChr.getInstance().getChrSwitchCellGainState() != 0 && this.mQoeBadCount > 6) {
                Log.d(TAG, "updateChrQoeGainStatistics: gain state bad, mQoeBadCount:" + this.mQoeBadCount);
                WifiProChr.getInstance().setChrSwitchCellGainState(0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSwitchTimeStatistics(int handoverReason) {
        long duration = SystemClock.elapsedRealtime() - this.mHandoverCellStartTime;
        if (duration > 0 && duration <= 7000) {
            WifiProChr.getInstance().updateWifi2CellDuration(getReasonType(handoverReason), 0, 1);
        } else if (duration > 7000 && duration <= 10000) {
            WifiProChr.getInstance().updateWifi2CellDuration(getReasonType(handoverReason), 1, 1);
        } else if (duration <= 10000 || duration > 15000) {
            WifiProChr.getInstance().updateWifi2CellDuration(getReasonType(handoverReason), 3, 1);
        } else {
            WifiProChr.getInstance().updateWifi2CellDuration(getReasonType(handoverReason), 2, 1);
        }
    }

    private void handleWifiProStaticsChrReport() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - this.mStartReportChrTime >= REPORT_WIFIPRO_CHR_INTERVAL) {
            WifiProChr.getInstance().reportWifi2CellQoe();
            WifiProChr.getInstance().reportWifi2CellDuration();
            this.mStartReportChrTime = currentTime;
        }
    }

    /* access modifiers changed from: package-private */
    public class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            WifiProStateMachine.this.logI("DefaultState is Enter");
            WifiProStateMachine.this.defaulVariableInit();
        }

        public void exit() {
            WifiProStateMachine.this.logI("DefaultState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /* 136171 */:
                    if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        return WifiProStateMachine.DBG;
                    }
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.transitionTo(wifiProStateMachine.mWiFiProEnableState);
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_REGISTER_APP_QOE /* 136220 */:
                    WifiProStateMachine.this.registerBoosterNetworkQoe();
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD /* 136222 */:
                    int subId = msg.arg1;
                    if (!WifiProCommonUtils.isValidSubId(subId)) {
                        return WifiProStateMachine.DBG;
                    }
                    if (subId == WifiProCommonUtils.getMasterCardSubId() && WifiProStateMachine.this.mMasterCardNetworkCallback != null && WifiProStateMachine.this.mConnectivityManager != null) {
                        WifiProStateMachine.this.logI("EVENT_UNREGISTER_CELL_CARD MasterCardNetwork, unregisterNetworkCallback");
                        WifiProStateMachine.this.mConnectivityManager.unregisterNetworkCallback(WifiProStateMachine.this.mMasterCardNetworkCallback);
                        return WifiProStateMachine.DBG;
                    } else if (subId != WifiProCommonUtils.getSlaveCardSubId() || WifiProStateMachine.this.mSlaveCardNetworkCallback == null || WifiProStateMachine.this.mConnectivityManager == null) {
                        WifiProStateMachine.this.logI("illegal subId");
                        return WifiProStateMachine.DBG;
                    } else {
                        WifiProStateMachine.this.logI("EVENT_UNREGISTER_CELL_CARD SlaveCarddNetwork, unregisterNetworkCallback");
                        WifiProStateMachine.this.mConnectivityManager.unregisterNetworkCallback(WifiProStateMachine.this.mSlaveCardNetworkCallback);
                        return WifiProStateMachine.DBG;
                    }
                case WifiProStateMachine.EVENT_RECOVERY_WLANPRO_SWITCH /* 136225 */:
                    WifiProStateMachine.this.logI("switch forbidden timeout, recovery wlan+ switch");
                    WifiProStateMachine.this.mIsSwitchForbiddenFromApp = false;
                    return WifiProStateMachine.DBG;
                default:
                    return false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class WiFiProEnableState extends State {
        WiFiProEnableState() {
        }

        public void enter() {
            WifiProStateMachine.this.logI("WiFiProEnableState is Enter");
            WifiProStateMachine.this.mIsWiFiNoInternet = false;
            WifiProStateMachine.this.mWiFiProPdpSwichValue = 0;
            WifiProStateMachine.this.registerCallBack();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(WifiProStateMachine.DBG);
            boolean unused = WifiProStateMachine.isWifiManualEvaluating = false;
            boolean unused2 = WifiProStateMachine.isWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = false;
            if (WifiProStateMachine.this.mIsWiFiProEnabled) {
                WifiProStateMachine.this.mIsWifiproDisableOnReboot = false;
                WifiProStateMachine.this.startDualBandManager();
                if (WifiProStateMachine.this.mHwIntelligenceWiFiManager != null) {
                    WifiProStateMachine.this.mHwIntelligenceWiFiManager.start();
                }
            }
            transitionNetState();
        }

        public void exit() {
            WifiProStateMachine.this.logI("WiFiProEnableState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /* 136171 */:
                    if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        return WifiProStateMachine.DBG;
                    }
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.transitionTo(wifiProStateMachine.mWiFiProEnableState);
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /* 136185 */:
                    handleWifiStateChanged();
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_CHR_CHECK_WIFI_HANDOVER /* 136214 */:
                    WifiProStateMachine.this.handleChrWifiHandoverCheck();
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /* 136293 */:
                    handleScanResult();
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_WIFIPRO_EVALUTE_STATE_CHANGE /* 136298 */:
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWiFiProEnableState);
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_LOAD_CONFIG_INTERNET_INFO /* 136315 */:
                    WifiProStateMachine.this.logI("WiFiProEnableState EVENT_LOAD_CONFIG_INTERNET_INFO");
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.mWiFiProEvaluateController.initWifiProEvaluateRecords();
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_DUALBAND_NETWROK_TYPE /* 136316 */:
                    handleDualBandNetworkType(msg);
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_PROCESS_GRS /* 136374 */:
                    WifiProStateMachine.this.logI("get probe urls by GRS");
                    new GrsApiManager(WifiProStateMachine.this.mContext).ayncGetGrsUrls();
                    return WifiProStateMachine.DBG;
                default:
                    return false;
            }
        }

        private void handleDualBandNetworkType(Message msg) {
            List<HwDualBandMonitorInfo> apList = null;
            if (msg.obj != null) {
                apList = (List) msg.obj;
            }
            if (apList == null || apList.size() == 0) {
                WifiProStateMachine.this.logE("handleDualBandNetworkType apList null error");
            } else if (WifiProStateMachine.this.mDualBandManager == null) {
                WifiProStateMachine.this.logE("handleDualBandNetworkType mDualBandManager is null");
            } else {
                int type = msg.arg1;
                if (!WifiProStateMachine.this.mIsUserManualConnectSuccess || WifiProStateMachine.this.mDualBandManager.isDualBandSignalApSameSsid(type, apList)) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("handleDualBandNetworkType type = " + type + " apList.size() = " + apList.size());
                    WifiProStateMachine.this.mDualBandMonitorApList.clear();
                    WifiProStateMachine.this.mDualBandMonitorInfoSize = apList.size();
                    for (HwDualBandMonitorInfo monitorInfo : apList) {
                        WifiProStateMachine.this.mDualBandMonitorApList.add(monitorInfo);
                        WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
                        apInfo.setApBssid(monitorInfo.mBssid);
                        apInfo.setApRssi(monitorInfo.mCurrentRssi);
                        apInfo.setApAuthType(monitorInfo.mAuthType);
                        WifiProStateMachine.this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
                    }
                    return;
                }
                WifiProStateMachine.this.logI("manual connection and non-dualband signal AP, keep current connection, ignore handover!");
            }
        }

        private void transitionNetState() {
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logI("WiFiProEnableState,go to WifiConnectedState");
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.transitionTo(wifiProStateMachine.mWifiConnectedState);
                return;
            }
            WifiProStateMachine.this.logI("WiFiProEnableState, go to mWifiDisConnectedState");
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiDisConnectedState);
        }

        private void handleScanResult() {
            if (!TextUtils.isEmpty(WifiProStateMachine.this.mUserManualConnecConfigKey)) {
                WifiProStateMachine.this.logI("User manual connecting ap, ignor this evaluate scan result");
            } else if (WifiproUtils.isCastOptWorking()) {
                WifiProStateMachine.this.logI("cast optimization is working, ignore this evaluate scan result");
            } else if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate() && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mWifiManager.isWifiEnabled()) {
                if (System.currentTimeMillis() - WifiProStateMachine.this.mLastDisconnectedTime < 6000) {
                    WifiProStateMachine.this.logI("Disconnected time less than 6s, ignor this scan result");
                    return;
                }
                NetworkInfo wifiInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
                if (wifiInfo != null) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.mScanResultList = wifiProStateMachine.mWifiManager.getScanResults();
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.mScanResultList = wifiProStateMachine2.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mScanResultList);
                    if (WifiProStateMachine.this.mScanResultList != null && WifiProStateMachine.this.mScanResultList.size() != 0) {
                        boolean issetting = WifiProStateMachine.this.isSettingsActivity();
                        int evaluateType = 0;
                        if (issetting) {
                            evaluateType = 1;
                        }
                        WifiProStateMachine.this.handleEvaluteScanResult(wifiInfo, issetting, evaluateType);
                    }
                }
            }
        }

        private void handleWifiStateChanged() {
            if (WifiProStateMachine.this.mWifiManager.getWifiState() == 1) {
                WifiProStateMachine.this.logI("wifi state is DISABLED, go to wifi disconnected");
                if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                    WifiProStateMachine.this.logI("BQE bad rove out, wifi disable time recorded.");
                    WifiProStateMachine.this.mChrWifiDidableStartTime = System.currentTimeMillis();
                }
                WifiProStateMachine.this.logI("UNEXPECT_SWITCH_EVENT: closeWifi: enter:");
                WifiProStateMachine.this.uploadManager.addChrSsidCntStat("unExpectSwitchEvent", "closeWifi");
                if (SystemClock.elapsedRealtime() - WifiProStateMachine.this.mWifiHandoverSucceedTimestamp < 30000) {
                    WifiProStateMachine.this.uploadChrHandoverUnexpectedTypes(WifiProStateMachine.WIFI_HANDOVER_UNEXPECTED_TYPES[0]);
                }
                if (WifiProStateMachine.this.shouldUploadCloseWifiEvent()) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.uploadChrNetQualityInfo(909002058);
                }
                WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
                if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiDisConnectedState) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.transitionTo(wifiProStateMachine.mWifiDisConnectedState);
                }
            } else if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                WifiProStateMachine.this.mWifiEnableTime = System.currentTimeMillis();
                WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                WifiProStateMachine.this.mWiFiProEvaluateController.initWifiProEvaluateRecords();
            }
        }
    }

    private boolean handleEvaluteScanResultCheckPara(int evaluateType) {
        if (this.isMapNavigating || this.isVehicleState) {
            logI("MapNavigatingOrVehicleState, ignor this scan result");
            return false;
        } else if (this.mIsP2PConnectedOrConnecting) {
            logI("P2PConnectedOrConnecting, ignor this scan result");
            this.mWiFiProEvaluateController.updateEvaluateRecords(this.mScanResultList, evaluateType, this.mCurrentSsid);
            return false;
        } else if (this.mWiFiProEvaluateController.isAllowAutoEvaluate(this.mScanResultList)) {
            return DBG;
        } else {
            this.mWiFiProEvaluateController.updateEvaluateRecords(this.mScanResultList, evaluateType, this.mCurrentSsid);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEvaluteScanResult(NetworkInfo wifiInfo, boolean issetting, int evaluateType) {
        if (WifiProCommonUtils.isWifiConnectedOrConnecting(this.mWifiManager) || wifiInfo.getDetailedState() != NetworkInfo.DetailedState.DISCONNECTED) {
            this.mWiFiProEvaluateController.updateEvaluateRecords(this.mScanResultList, evaluateType, this.mCurrentSsid);
        } else if (handleEvaluteScanResultCheckPara(evaluateType)) {
            for (ScanResult scanResult : this.mScanResultList) {
                if (this.mWiFiProEvaluateController.isAllowEvaluate(scanResult, evaluateType) && !this.mWiFiProEvaluateController.isLastEvaluateValid(scanResult, evaluateType)) {
                    this.mWiFiProEvaluateController.addEvaluateRecords(scanResult, evaluateType);
                }
            }
            this.mWiFiProEvaluateController.orderByRssi();
            boolean isfactorymode = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
            if (this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty() || isfactorymode) {
                logE("UnEvaluateAPRecords is Empty");
                return;
            }
            this.mWiFiProEvaluateController.unEvaluateAPQueueDump();
            logI("transition to mwifiSemiAutoEvaluateState, to evaluate ap");
            if (issetting) {
                this.mWifiProStatisticsManager.updateBGChrStatistic(2);
            } else {
                this.mWifiProStatisticsManager.updateBGChrStatistic(1);
            }
            transitionTo(this.mWifiSemiAutoEvaluateState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportNetworkConnectivity(boolean hasConnectivity) {
        Network[] networks = this.mConnectivityManager.getAllNetworks();
        for (Network nw : networks) {
            NetworkCapabilities nc = this.mConnectivityManager.getNetworkCapabilities(nw);
            if (nc != null && nc.hasTransport(1) && nc.hasCapability(12)) {
                this.mConnectivityManager.reportNetworkConnectivity(nw, hasConnectivity);
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWifiNetworkCapabilityValidated() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (!(connectivityManager == null || connectivityManager.getAllNetworks() == null)) {
            for (Network network : this.mConnectivityManager.getAllNetworks()) {
                NetworkCapabilities nc = this.mConnectivityManager.getNetworkCapabilities(network);
                if (nc != null && nc.hasTransport(1)) {
                    return nc.hasCapability(16);
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public class WiFiProDisabledState extends State {
        WiFiProDisabledState() {
        }

        public void enter() {
            WifiProStateMachine.this.logI("WiFiProDisabledState is Enter");
            boolean unused = WifiProStateMachine.isWifiManualEvaluating = false;
            boolean unused2 = WifiProStateMachine.isWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.unRegisterCallBack();
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(false);
            WifiProStateMachine.this.stopDualBandManager();
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logI("WiFiProDisabledState , wifi is connect ");
                WifiInfo cInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && NetworkInfo.DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                    WifiProStateMachine.this.logI("wifi State == VERIFYING_POOR_LINK");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(131874);
                }
                WifiProStateMachine.this.setWifiCSPState(1);
            }
            WifiProStateMachine.this.resetVariables();
        }

        public void exit() {
            WifiProStateMachine.this.logI("WiFiProDisabledState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 131873:
                    WifiProStateMachine.this.logI("receive POOR_LINK_DETECTED sendMessageDelayed");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(131874);
                    return WifiProStateMachine.DBG;
                case 131874:
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /* 136177 */:
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /* 136186 */:
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /* 136169 */:
                    WifiProStateMachine.this.handleWifiNetWorkChange(msg);
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /* 136171 */:
                    if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        return WifiProStateMachine.DBG;
                    }
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.transitionTo(wifiProStateMachine.mWiFiProEnableState);
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /* 136185 */:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() != 3) {
                        return WifiProStateMachine.DBG;
                    }
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /* 136308 */:
                    WifiProStateMachine.this.handleConfigNetworkChange(msg);
                    return WifiProStateMachine.DBG;
                default:
                    return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfigNetworkChange(Message msg) {
        if (msg.obj instanceof Intent) {
            Intent configIntent = (Intent) msg.obj;
            WifiConfiguration connCfg = null;
            if (configIntent.getParcelableExtra("wifiConfiguration") instanceof WifiConfiguration) {
                connCfg = (WifiConfiguration) configIntent.getParcelableExtra("wifiConfiguration");
            } else {
                logE("handleConfigNetworkChange:WifiConfiguration is not match the class");
            }
            if (connCfg != null) {
                int changeReason = configIntent.getIntExtra("changeReason", -1);
                if (connCfg.isTempCreated && changeReason != 1) {
                    logI("WiFiProDisabledState, forget " + StringUtilEx.safeDisplaySsid(connCfg.SSID));
                    this.mWifiManager.forget(connCfg.networkId, null);
                    return;
                }
                return;
            }
            return;
        }
        logE("handleConfigNetworkChange:configIntent is not match the class");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiNetWorkChange(Message msg) {
        if (msg.obj instanceof Intent) {
            Object objNetworkInfo = ((Intent) msg.obj).getParcelableExtra("networkInfo");
            NetworkInfo networkInfo = null;
            if (objNetworkInfo instanceof NetworkInfo) {
                networkInfo = (NetworkInfo) objNetworkInfo;
            }
            if (networkInfo != null && NetworkInfo.DetailedState.VERIFYING_POOR_LINK == networkInfo.getDetailedState()) {
                this.mWsmChannel.sendMessage(131874);
            } else if (networkInfo != null && NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            } else if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                setWifiCSPState(1);
            }
        } else {
            logE("handleWifiNetWorkChange:Class is not match");
        }
    }

    /* access modifiers changed from: package-private */
    public class WifiConnectedState extends State {
        private int internetFailureDetectedCount;
        private boolean isChrShouldReport;
        private boolean isFirstTimeInformNetResult;
        private boolean isIgnorAvailableWifiCheck;
        private boolean isKeepConnected;
        private boolean isPortalAP;
        private boolean isPortalChrEverUploaded;
        private boolean isToastDisplayed;
        private int oldType;
        private int portalCheckCounter;

        WifiConnectedState() {
        }

        private void initConnectedState() {
            this.isFirstTimeInformNetResult = WifiProStateMachine.DBG;
            WifiProStateMachine.this.setWifiEvaluateTag(false);
            WifiProStateMachine.this.mWifiHandover.clearWiFiSameSsidSwitchFlag();
            if (WifiProStateMachine.this.mDuanBandHandoverType == 1) {
                WifiProStateMachine.this.mDuanBandHandoverType = 0;
            } else {
                WifiProStateMachine.this.mIsWiFiNoInternet = false;
            }
            WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
            this.isKeepConnected = false;
            this.isPortalAP = false;
            this.portalCheckCounter = 0;
            WifiProStateMachine.this.mIsScanedRssiLow = false;
            WifiProStateMachine.this.mIsScanedRssiMiddle = false;
            this.isIgnorAvailableWifiCheck = WifiProStateMachine.DBG;
            WifiProStateMachine.this.isDialogUpWhenConnected = false;
            WifiProStateMachine.this.mIsPortalAp = false;
            WifiProStateMachine.this.mIsNetworkAuthen = false;
            this.isPortalChrEverUploaded = false;
            WifiProStateMachine.this.refreshConnectedNetWork();
            WifiProStateMachine.this.mLastWifiLevel = 0;
            this.internetFailureDetectedCount = 0;
            WifiProStateMachine.this.setWifiCSPState(1);
            WifiProStateMachine.this.mHiLinkUnconfig = isHiLinkUnconfigRouter();
            if (!TextUtils.isEmpty(WifiProStateMachine.this.mUserManualConnecConfigKey) && WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mUserManualConnecConfigKey.equals(WifiProStateMachine.this.mCurrentWifiConfig.configKey())) {
                WifiProStateMachine.this.mIsUserManualConnectSuccess = WifiProStateMachine.DBG;
                long deltaTime = System.currentTimeMillis() - WifiProStateMachine.this.mLastDisconnectedTimeStamp;
                if (WifiProStateMachine.this.mCurrentSsid != null && !WifiProStateMachine.this.mCurrentSsid.equals(WifiProStateMachine.this.mLastConnectedSsid) && deltaTime < 10000 && WifiProStateMachine.this.mLastDisconnectedRssi < -75) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.uploadChrNetQualityInfo(909002059);
                }
                WifiProStateMachine.this.logI("User manual connect ap success!");
            }
            WifiProStateMachine.this.mUserManualConnecConfigKey = "";
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.notifyManualConnectAP(wifiProStateMachine.mIsUserManualConnectSuccess, WifiProStateMachine.this.mIsUserHandoverWiFi);
            if (WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                WifiProStateMachine.this.refreshConnectedNetWork();
                WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
            }
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logI("isAllowWiFiAutoEvaluate == " + WifiProStateMachine.this.isAllowWiFiAutoEvaluate());
            WifiProStateMachine.this.initWifiConfig();
            getWifiProBlacklist();
        }

        private void getWifiProBlacklist() {
            WifiProStateMachine.this.mWifiProSwitchState = WifiProCommonUtils.getWifiProSwitchInfo();
            WifiProStateMachine.this.mWifiProBlacklist = WifiProCommonUtils.getWifiProBlacklist(WifiProStateMachine.APK_INFO);
            WifiProStateMachine.this.mWifiProBlacklistFullScreen = WifiProCommonUtils.getWifiProBlacklist(WifiProStateMachine.APK_INFO_EXTERN);
        }

        private void reportDiffTypeCHR(int newType) {
            if (!this.isChrShouldReport) {
                this.isChrShouldReport = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(WifiProStateMachine.this.mCurrentSsid, 0);
                int diffType = WifiProStateMachine.this.mWiFiProEvaluateController.getChrDiffType(this.oldType, newType);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("reportDiffTypeCHR is Enter, diffType  == " + diffType);
                if (diffType != 0) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseBgAcDiffType(diffType);
                }
                if (this.oldType == newType) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseActiveCheckRsSame();
                }
            }
        }

        public void enter() {
            WifiProStateMachine.this.logI("WifiConnectedState is Enter");
            if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT);
            }
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
            WifiProStateMachine.this.refreshConnectedNetWork();
            setWifiAgentScore();
            this.oldType = WifiProStateMachine.this.mWiFiProEvaluateController.getOldNetworkType(WifiProStateMachine.this.mCurrentSsid);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("WiFiProConnected oldType = " + this.oldType);
            NetworkInfo wifiInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
            if (wifiInfo != null && wifiInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logI(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(131874);
            }
            if (WifiProStateMachine.this.mNetworkBlackListManager.isInTempWifiBlackList(WifiProStateMachine.this.mWifiManager.getConnectionInfo().getBSSID())) {
                WifiProStateMachine.this.logI("cleanTempBlackList for this bssid.");
                WifiProStateMachine.this.mNetworkBlackListManager.cleanTempWifiBlackList();
            }
            if (!WifiProStateMachine.this.mPhoneStateListenerRegisted) {
                WifiProStateMachine.this.logI("start PhoneStateListener");
                WifiProStateMachine.this.mTelephonyManager.listen(WifiProStateMachine.this.phoneStateListener, 32);
                WifiProStateMachine.this.mPhoneStateListenerRegisted = WifiProStateMachine.DBG;
            }
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PROCESS_GRS, 500);
            initConnectedState();
        }

        public void exit() {
            WifiProStateMachine.this.logI("WifiConnectedState is Exit");
            this.isFirstTimeInformNetResult = false;
            this.isToastDisplayed = false;
            this.isChrShouldReport = false;
            this.oldType = 0;
            WifiProStateMachine.this.mVerfyingToConnectedState = false;
            WifiProStateMachine.this.mDisconnectToConnectedState = false;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            cancelPortalExpiredNotifyStatusBar();
            WifiProStateMachine.this.respCodeChrInfo = "";
            WifiProStateMachine.this.detectionNumSlow = 0;
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RECOVERY_WIFI_AGENT_SCORE);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /* 136169 */:
                    WifiProStateMachine.this.handleWifiNetworkStateChange(msg);
                    break;
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /* 136170 */:
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /* 136186 */:
                case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /* 136190 */:
                    if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoHandoverNetwork && WifiProStateMachine.this.mIsWiFiNoInternet && WifiProStateMachine.this.isAllowWifi2Mobile()) {
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        break;
                    }
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /* 136176 */:
                    handleCheckAvailableApResult(msg);
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /* 136177 */:
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /* 136181 */:
                    handleCheckWifiInternetResultWithConnected(msg);
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /* 136182 */:
                    handleUserSelectDialogOk();
                    break;
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /* 136183 */:
                    handleDialogCancel();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /* 136192 */:
                    WifiProStateMachine.this.handleCheckWifiInternet();
                    break;
                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /* 136195 */:
                    handleHttpResult(msg);
                    break;
                case WifiProStateMachine.EVENT_NETWORK_USER_CONNECT /* 136202 */:
                    if (msg.obj != null && ((Boolean) msg.obj).booleanValue()) {
                        WifiProStateMachine.this.mIsUserManualConnectSuccess = WifiProStateMachine.DBG;
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.logI("receive EVENT_NETWORK_USER_CONNECT, set mIsUserManualConnectSuccess = " + WifiProStateMachine.this.mIsUserManualConnectSuccess);
                        break;
                    }
                case WifiProStateMachine.EVENT_RECOVERY_WIFI_AGENT_SCORE /* 136223 */:
                    recoveryWifiAgentScore();
                    break;
                case WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT /* 136224 */:
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logI("EVENT_DISPATCH_INTERNET_RESULT in ConnectedState, level = " + msg.arg1 + ", isFirstTimeInformNetResult = " + this.isFirstTimeInformNetResult);
                    if (!this.isFirstTimeInformNetResult) {
                        WifiProStateMachine.this.handleInternetResultForDisplay(msg.arg1, msg.arg2);
                        break;
                    } else {
                        this.isFirstTimeInformNetResult = false;
                        WifiProStateMachine.this.updateWifiInternetStateChange(msg.arg1, WifiProStateMachine.DBG);
                        break;
                    }
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /* 136300 */:
                    if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_CHECK_UNKOWN /* 136309 */:
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /* 136311 */:
                    WifiProStateMachine.this.handleGetWifiTcpRx();
                    break;
                case WifiProStateMachine.EVENT_WIFI_NO_INTERNET_NOTIFICATION /* 136318 */:
                    WifiProStateMachine.this.handleNotidication();
                    break;
                case WifiProStateMachine.EVENT_PORTAL_SELECTED /* 136319 */:
                    handlePortalSelected();
                    break;
                case WifiProStateMachine.EVENT_LAUNCH_BROWSER /* 136320 */:
                    HwAutoConnectManager hwAutoConnectManager = HwAutoConnectManager.getInstance();
                    if (!(hwAutoConnectManager == null || WifiProStateMachine.this.mCurrentWifiConfig == null)) {
                        hwAutoConnectManager.launchBrowserForPortalLogin(WifiProStateMachine.this.mCurrentWifiConfig.configKey());
                        break;
                    }
                case WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED /* 136321 */:
                    WifiProStateMachine.this.logI("alarm expired, upload CHR");
                    if (!this.isPortalChrEverUploaded) {
                        WifiProStateMachine.this.uploadPortalAuthExpirationStatistics(false);
                        this.isPortalChrEverUploaded = WifiProStateMachine.DBG;
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return WifiProStateMachine.DBG;
        }

        private void handleHttpResult(Message msg) {
            if (msg.obj != null && (msg.obj instanceof Boolean)) {
                if (((Boolean) msg.obj).booleanValue()) {
                    this.internetFailureDetectedCount = 0;
                    WifiProStateMachine.this.logI("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ to check wifi immediately.");
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                    return;
                }
                WifiProStateMachine.this.logI("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ the http unreachable.");
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, -1);
            }
        }

        private void handlePortalSelected() {
            WifiProStateMachine.this.logI("###MSG_PORTAL_SELECTED");
            if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                if (!this.isPortalChrEverUploaded) {
                    WifiProStateMachine.this.logI("user clicks the notification, upload CHR");
                    WifiProStateMachine.this.uploadPortalAuthExpirationStatistics(WifiProStateMachine.DBG);
                    this.isPortalChrEverUploaded = WifiProStateMachine.DBG;
                }
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_LAUNCH_BROWSER, 500);
            }
        }

        private void cancelPortalExpiredNotifyStatusBar() {
            if (WifiProStateMachine.this.mPortalNotificationId != -1 && WifiProStateMachine.this.mCurrentWifiConfig != null) {
                Settings.Global.putInt(WifiProStateMachine.this.mContext.getContentResolver(), "captive_portal_notification_shown", 0);
                WifiProStateMachine.this.logI("portal notification is dismissed, change CAPTIVE_PORTAL_NOTIFICATION_SHOWN to 0");
                WifiProStateMachine.this.mWifiProUIDisplayManager.cancelPortalNotificationStatusBar(WifiProStateMachine.PORTAL_STATUS_BAR_TAG, WifiProStateMachine.this.mPortalNotificationId);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.notifyPortalStatusChanged(false, wifiProStateMachine.mCurrentWifiConfig.configKey(), WifiProStateMachine.this.mCurrentWifiConfig.lastHasInternetTimestamp > 0 ? WifiProStateMachine.DBG : false);
                WifiProStateMachine.this.mPortalNotificationId = -1;
            }
        }

        private boolean isContinueToKeepConnected(int internetQos) {
            if (!this.isKeepConnected || WifiProStateMachine.this.mIsUserHandoverWiFi) {
                return false;
            }
            if (internetQos != -1 && internetQos != 6) {
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, false, 0, false);
                return WifiProStateMachine.DBG;
            } else if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mWifiTcpRxCount = wifiProStateMachine.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                return WifiProStateMachine.DBG;
            } else {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                return WifiProStateMachine.DBG;
            }
        }

        private void handleCheckWifiInternetResultWithConnected(Message msg) {
            WifiProStateMachine.this.logI("WiFi internet check level = " + msg.arg1 + ", isKeepConnected = " + this.isKeepConnected + ", mIsUserHandoverWiFi = " + WifiProStateMachine.this.mIsUserHandoverWiFi);
            WifiProStateMachine.this.notifyNetworkCheckResult(msg.arg1);
            reportDiffTypeCHR(WifiProStateMachine.this.mWiFiProEvaluateController.getNewNetworkType(msg.arg1));
            if (isContinueToKeepConnected(msg.arg1)) {
                recoveryWifiAgentScore();
                WifiProStateMachine.this.logI("continue to keep connected");
                return;
            }
            this.isKeepConnected = false;
            int internetLevel = msg.arg1;
            if (!WifiProStateMachine.this.isDialogUpWhenConnected || !(internetLevel == -1 || internetLevel == 6)) {
                if (this.isPortalAP) {
                    if (WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                        this.portalCheckCounter++;
                    }
                    WifiProStateMachine.this.logI("portalCheckCounter = " + this.portalCheckCounter);
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                    if (internetLevel == 6 || internetLevel == -1) {
                        if (internetLevel == -1) {
                            internetFailureSelfcure();
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                        return;
                    }
                }
                if (internetLevel == -1) {
                    WifiProStateMachine.this.logI("WiFi NO internet,isPortalAP = " + this.isPortalAP);
                    if (!WifiProStateMachine.this.mIsWiFiInternetCHRFlag && !this.isPortalAP) {
                        WifiProStateMachine.this.logI("upload WIFI_ACCESS_INTERNET_FAILED event for FIRST_CONNECT_NO_INTERNET,ssid:" + StringUtilEx.safeDisplaySsid(WifiProStateMachine.this.mCurrentSsid));
                        WifiProStateMachine.this.mWifiProStatisticsManager.uploadChrNetQualityInfo(909009015);
                        Bundle data = new Bundle();
                        data.putInt(WifiProStateMachine.EVENT_ID, 909002024);
                        data.putString(WifiProStateMachine.EVENT_DATA, "FIRST_CONNECT_NO_INTERNET");
                        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 4, data);
                    }
                    WifiProStateMachine.this.mIsWiFiInternetCHRFlag = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                    if (this.isPortalAP) {
                        WifiProStateMachine.this.updateWifiInternetStateChange(-1);
                    } else {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT, -1);
                    }
                    if (WifiProStateMachine.this.mIsUserManualConnectSuccess) {
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.mWifiTcpRxCount = wifiProStateMachine.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                        return;
                    }
                    if (this.isPortalAP) {
                        this.isPortalAP = false;
                        WifiProStateMachine.this.mWiFiNoInternetReason = 1;
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 3);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 3, WifiProStateMachine.this.mCurrentSsid);
                    } else {
                        WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange((boolean) WifiProStateMachine.DBG);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 2);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 2, WifiProStateMachine.this.mCurrentSsid);
                        WifiProStateMachine.this.mWiFiNoInternetReason = 0;
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(WifiProStateMachine.DBG);
                    }
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                    if (this.isIgnorAvailableWifiCheck) {
                        HwWifiProFeatureControl.getInstance();
                        if (!HwWifiProFeatureControl.isSelfCureOngoing() && !WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                            WifiProStateMachine.this.logI("inquire the surrounding AP for wifiHandover");
                            this.isIgnorAvailableWifiCheck = false;
                            WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                            updateWifiAgentScore(internetLevel);
                        }
                    }
                    if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.mWifiTcpRxCount = wifiProStateMachine2.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                    } else {
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    }
                    updateWifiAgentScore(internetLevel);
                } else if (internetLevel == 6) {
                    WifiProStateMachine.this.logI("WifiConnectedState: WiFi is protal");
                    this.isPortalAP = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                    WifiProStateMachine.this.mIsPortalAp = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mIsNetworkAuthen = false;
                    WifiProStateMachine.this.mWiFiNoInternetReason = 1;
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 3);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 3, WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, (boolean) WifiProStateMachine.DBG, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                    if (!(WifiProStateMachine.this.mCurrentWifiConfig == null || WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp == 0)) {
                        WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp = 0;
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.updateWifiConfig(wifiProStateMachine3.mCurrentWifiConfig);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                    updateWifiAgentScore(internetLevel);
                } else {
                    Bundle recordData = new Bundle();
                    recordData.putInt("reason", 0);
                    recordData.putBoolean(WifiProStateMachine.ACCESS_WEB_RECORD_SUCC, WifiProStateMachine.DBG);
                    recordData.putBoolean(WifiProStateMachine.ACCESS_WEB_RECORD_PORTAL, this.isPortalAP);
                    WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 32, recordData);
                    this.isKeepConnected = false;
                    WifiProStateMachine.this.mIsWiFiNoInternet = false;
                    WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                    WifiProStateMachine.this.mIsNetworkAuthen = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 4, WifiProStateMachine.this.mCurrentSsid);
                    Bundle data2 = new Bundle();
                    data2.putBoolean("success", WifiProStateMachine.DBG);
                    WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 41, data2);
                    if (this.isPortalAP) {
                        notifyPortalHasInternetAccess();
                    }
                    updateWifiAgentScore(internetLevel);
                }
            } else {
                WifiProStateMachine.this.logI("AP is noInternet or Protal AP , Continue DisplayDialog");
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
            }
        }

        private void setWifiAgentScore() {
            if (!WifiProStateMachine.this.mDisconnectToConnectedState || !WifiProStateMachine.this.mIsWifiAdvancedChipUser || !WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.isAllowWifi2Mobile() || WifiProStateMachine.this.mIsVpnWorking || WifiProStateMachine.this.mIsUserManualConnectSuccess || WifiProStateMachine.this.mCurrentWifiConfig == null || WifiProStateMachine.this.mCurrentWifiConfig.portalNetwork) {
                WifiProStateMachine.this.logI("use the default Wifi score");
                return;
            }
            int score = HwServiceFactory.getHwConnectivityManager().getNetworkAgentInfoScore();
            int rssiLevel = WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mCurrWifiInfo);
            boolean isCell2gOr3g = WifiProCommonUtils.isCellNetworkClass2gOr3g(WifiProStateMachine.this.mTelephonyManager);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("score = " + score + ", rssiLevel = " + rssiLevel + ", isCell2gOr3g = " + isCell2gOr3g);
            if (rssiLevel > 3) {
                if (rssiLevel == 4 && !WifiProStateMachine.this.isShortTimeWifiEnabled()) {
                    if ((!WifiproUtils.isHwEnterprise(WifiProStateMachine.this.mCurrWifiInfo) && !WifiproUtils.isInstallHuaweiCustomizedApp(WifiProStateMachine.this.mContext)) || WifiProCommonUtils.isQueryActivityMatched(WifiProStateMachine.this.mContext, WifiProStateMachine.PACKAGE_NAME_SETTINGS)) {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (score == 20 && !isCell2gOr3g) {
                HwNetworkAgent networkAgent = WifiProStateMachine.this.mWifiStateMachineUtils.getNetworkAgent(WifiInjector.getInstance().getClientModeImpl());
                if (networkAgent != null) {
                    networkAgent.sendNetworkScore(40);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RECOVERY_WIFI_AGENT_SCORE, 15000);
            }
        }

        private void updateWifiAgentScore(int internetLevel) {
            HwNetworkAgent networkAgent;
            HwNetworkAgent networkAgent2;
            if (WifiProStateMachine.this.mDisconnectToConnectedState) {
                WifiProStateMachine.this.mDisconnectToConnectedState = false;
                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_RECOVERY_WIFI_AGENT_SCORE)) {
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RECOVERY_WIFI_AGENT_SCORE);
                }
                int score = HwServiceFactory.getHwConnectivityManager().getNetworkAgentInfoScore();
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("score = " + score);
                if (internetLevel == -1) {
                    WifiProStateMachine.this.logI("wifi network has no internet");
                    if (score == 0 && (networkAgent2 = WifiProStateMachine.this.mWifiStateMachineUtils.getNetworkAgent(WifiInjector.getInstance().getClientModeImpl())) != null) {
                        networkAgent2.sendNetworkScore((int) WifiProStateMachine.WIFI_SCORE_LINK_GOOD);
                    }
                } else if (internetLevel == 6) {
                    if (score == 40 && (networkAgent = WifiProStateMachine.this.mWifiStateMachineUtils.getNetworkAgent(WifiInjector.getInstance().getClientModeImpl())) != null) {
                        networkAgent.sendNetworkScore((int) WifiProStateMachine.WIFI_SCORE_LINK_GOOD);
                    }
                } else if (score == 40) {
                    WifiProStateMachine.this.mNotifyWifiLinkPoorReason = 12;
                    WifiProStateMachine.this.logI("send maybe poor link msg");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(131876);
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWiFiProVerfyingLinkState);
                } else {
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWiFiLinkMonitorState);
                }
            } else if (internetLevel == -1) {
                WifiProStateMachine.this.logI("wifi network has no internet");
            } else if (internetLevel == 6) {
                WifiProStateMachine.this.logI("wifi network is portal");
            } else {
                WifiProStateMachine.this.logI("wifi network has internet");
                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                wifiProStateMachine4.transitionTo(wifiProStateMachine4.mWiFiLinkMonitorState);
            }
        }

        private void recoveryWifiAgentScore() {
            HwNetworkAgent networkAgent;
            int score = HwServiceFactory.getHwConnectivityManager().getNetworkAgentInfoScore();
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("score = " + score + ", mDisconnectToConnectedState = " + WifiProStateMachine.this.mDisconnectToConnectedState);
            if (!WifiProStateMachine.this.mDisconnectToConnectedState) {
                return;
            }
            if ((score == 40 || score == 0) && (networkAgent = WifiProStateMachine.this.mWifiStateMachineUtils.getNetworkAgent(WifiInjector.getInstance().getClientModeImpl())) != null) {
                networkAgent.sendNetworkScore((int) WifiProStateMachine.WIFI_SCORE_LINK_GOOD);
            }
        }

        private void notifyPortalHasInternetAccess() {
            WifiProStateMachine.this.logI("portal has internet access, force network re-evaluation");
            ConnectivityManager connMgr = ConnectivityManager.from(WifiProStateMachine.this.mContext);
            if (connMgr == null) {
                WifiProStateMachine.this.logE("notifyPortalHasInternetAccess connMgr is null");
                return;
            }
            Network[] info = connMgr.getAllNetworks();
            for (Network nw : info) {
                NetworkCapabilities nc = connMgr.getNetworkCapabilities(nw);
                if (nc.hasTransport(1) && nc.hasCapability(12)) {
                    WifiProStateMachine.this.logI("Network has capability");
                    connMgr.reportNetworkConnectivity(nw, false);
                    return;
                }
            }
        }

        private boolean isProvisioned(Context context) {
            if (Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1) {
                return WifiProStateMachine.DBG;
            }
            return false;
        }

        private void handleCheckAvailableApResult(Message msg) {
            if (!this.isIgnorAvailableWifiCheck) {
                if (WifiProStateMachine.this.mIsWiFiNoInternet && msg.obj != null && (msg.obj instanceof Boolean) && ((Boolean) msg.obj).booleanValue()) {
                    HwWifiProFeatureControl.getInstance();
                    if (!HwWifiProFeatureControl.isSelfCureOngoing()) {
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        if (1 != WifiProStateMachine.this.mWiFiNoInternetReason) {
                            WifiProStateMachine.this.logI("AllowWifi2Wifi, transitionTo mWiFiLinkMonitorState");
                            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                            wifiProStateMachine.transitionTo(wifiProStateMachine.mWiFiLinkMonitorState);
                            return;
                        }
                        return;
                    }
                }
                if (!this.isToastDisplayed) {
                    WifiProStateMachine.this.logW("There is no network can switch");
                    if (!WifiProStateMachine.this.mIsUserManualConnectSuccess && WifiProStateMachine.this.mIsWiFiNoInternet && !this.isPortalAP && !WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                        WifiProStateMachine.this.logI("try to switch cell directly");
                        WifiProStateMachine.this.sendInvalidLinkDetected();
                    }
                    this.isToastDisplayed = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                    if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.mWifiTcpRxCount = wifiProStateMachine2.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                        return;
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 60000);
                }
            }
        }

        private void handleUserSelectDialogOk() {
            WifiProStateMachine.this.logI("Intelligent choice other network,go to mWiFiLinkMonitorState");
            WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mIsWiFiInternetCHRFlag = WifiProStateMachine.DBG;
            this.isKeepConnected = false;
            WifiProStateMachine.this.mIsUserManualConnectSuccess = false;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.transitionTo(wifiProStateMachine.mWiFiLinkMonitorState);
        }

        private boolean isHiLinkUnconfigRouter() {
            Bundle data = new Bundle();
            data.putString("CurrentSsid", WifiProStateMachine.this.mCurrentSsid);
            data.putString("CurrentBssid", WifiProStateMachine.this.mCurrentBssid);
            Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 34, data);
            if (result != null) {
                return result.getBoolean("isHiLinkUnconfigRouter");
            }
            return false;
        }

        private void handleDialogCancel() {
            WifiProStateMachine.this.logI("Keep this network,do nothing!!!");
            this.isIgnorAvailableWifiCheck = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            this.isKeepConnected = WifiProStateMachine.DBG;
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.mWifiTcpRxCount = wifiProStateMachine.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                }
            }
            if (WifiProStateMachine.this.isDialogUpWhenConnected && WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
            }
        }

        private void internetFailureSelfcure() {
            HwWifiProFeatureControl.getInstance();
            if (!HwWifiProFeatureControl.isSelfCureOngoing() && this.internetFailureDetectedCount == 0 && WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mCurrentRssi = WifiProCommonUtils.getCurrentRssi(wifiProStateMachine.mWifiManager);
                WifiProStateMachine.this.logI("internetFailureSelfcure mCurrentRssi = " + WifiProStateMachine.this.mCurrentRssi);
                if (WifiProStateMachine.this.mCurrentRssi >= -70) {
                    HwWifiProFeatureControl.getInstance();
                    HwWifiProFeatureControl.notifyInternetFailureDetected(WifiProStateMachine.DBG);
                    this.internetFailureDetectedCount++;
                    WifiProStateMachine.this.mVerfyingToConnectedState = false;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCheckWifiInternet() {
        int result;
        if (this.mPowerManager.isScreenOn()) {
            this.mNetworkQosMonitor.queryNetworkQos(1, this.mIsPortalAp, this.mIsNetworkAuthen, false);
            return;
        }
        logI("Screen off, cancel network check! mIsPortalAp " + this.mIsPortalAp);
        if (this.mIsPortalAp) {
            result = 6;
        } else {
            result = -1;
        }
        sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, result);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotidication() {
        this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, (boolean) DBG, 0, false);
        this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 2, this.mCurrentSsid);
        this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 2);
        this.mWifiProUIDisplayManager.notificateNetAccessChange((boolean) DBG);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiNetworkStateChange(Message msg) {
        if (msg.obj instanceof Intent) {
            Object objNetworkInfo = ((Intent) msg.obj).getParcelableExtra("networkInfo");
            NetworkInfo networkInfo = null;
            if (objNetworkInfo instanceof NetworkInfo) {
                networkInfo = (NetworkInfo) objNetworkInfo;
            }
            if (networkInfo != null && NetworkInfo.State.DISCONNECTED == networkInfo.getState()) {
                transitionTo(this.mWifiDisConnectedState);
                return;
            }
            return;
        }
        logE("handleWifiNetworkStateChange: msg.obj is null or not intent");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWifiConfig() {
        WifiConfiguration cfg = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        if (cfg != null) {
            int accessType = cfg.internetAccessType;
            int qosLevel = cfg.networkQosLevel;
            logI("accessType = : " + accessType + ",qosLevel = " + qosLevel + ",wifiProNoInternetAccess = " + cfg.wifiProNoInternetAccess);
            if (cfg.isTempCreated) {
                this.mWifiProStatisticsManager.updateBGChrStatistic(19);
            }
            if (!this.isPortalExpired) {
                resetWifiEvaluteInternetType();
                this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
            }
            this.isPortalExpired = false;
        } else {
            logE("cfg= null ");
        }
        if (isAllowWiFiAutoEvaluate()) {
            this.mWiFiProEvaluateController.addEvaluateRecords(this.mCurrWifiInfo, 1);
        }
    }

    /* access modifiers changed from: package-private */
    public class WiFiLinkMonitorState extends State {
        private int currWifiPoorlevel;
        private int detectCounter = 0;
        private int internetFailureDetectedCnt;
        private boolean isAllowWiFiHandoverMobile;
        private boolean isBQERequestCheckWiFi;
        private boolean isCancelCHRTypeReport;
        private boolean isCheckWiFiForUpdateSetting;
        private boolean isDialogDisplayed;
        private boolean isDisableWifiAutoSwitch = false;
        private boolean isNoInternetDialogShowing;
        private boolean isNotifyInvalidLinkDetection = false;
        private boolean isRequestWifInetCheck = false;
        private boolean isRssiLowOrMiddleWifi2Wifi = false;
        private boolean isScreenOffMonitor;
        private boolean isSwitching;
        private boolean isToastDisplayed;
        private boolean isWiFiHandoverPriority;
        private boolean isWifi2MobileUIShowing;
        private boolean isWifi2WifiProcess;
        private boolean isWifiHandoverMobileToastShowed = false;
        private HwAppQoeApkConfig mApkConfig;
        private List<HwAppQoeApkConfig> mApkConfigList;
        private HwAppQoeResourceManager mHwAppQoeResourceManager;
        private boolean mIsAppInBlacklist = false;
        private boolean mIsAppInBlacklistFullScreen = false;
        private boolean mIsMasterCardReady = false;
        private boolean mIsNeedIgnoreQoeUpdate = false;
        private boolean mIsSlaveCardReady = false;
        private int mLastUpdatedQosLevel = 0;
        private int mMasterCardWaitQoeCnt = 0;
        private int mSlaveCardWaitQoeCnt = 0;
        private int mWifi2WifiThreshod = WifiHandover.INVALID_RSSI;
        private boolean portalCheck = false;
        private int rssiLevel0Or1ScanedCounter = 0;
        private int rssiLevel2ScanedCounter = 0;
        private long wifiLinkHoldTime;
        private int wifiMonitorCounter;

        WiFiLinkMonitorState() {
        }

        private void wiFiLinkMonitorStateInit(boolean internetRecheck) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("wiFiLinkMonitorStateInit is Start, internetRecheck = " + internetRecheck);
            WifiProStateMachine.this.mBadBssid = null;
            WifiProStateMachine.this.mNotifyWifiLinkPoorReason = -1;
            WifiProStateMachine.this.mHandoverFailReason = -1;
            WifiProStateMachine.this.mLastHandoverFailReason = -1;
            this.isSwitching = false;
            this.isWifi2WifiProcess = false;
            this.isRssiLowOrMiddleWifi2Wifi = false;
            this.isWifi2MobileUIShowing = false;
            this.isCheckWiFiForUpdateSetting = false;
            this.isDialogDisplayed = false;
            this.isNoInternetDialogShowing = false;
            this.detectCounter = 0;
            WifiProStateMachine.this.setWifiCSPState(1);
            this.mLastUpdatedQosLevel = 0;
            if (!internetRecheck) {
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.logI("mIsWiFiNoInternet is true,sendMessage wifi Qos is -1");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, -1, 0, false);
                } else {
                    HwWifiProFeatureControl.getInstance();
                    HwWifiProFeatureControl.notifyInternetAccessRecovery();
                    WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                }
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.mNeedRetryMonitor = false;
        }

        public void enter() {
            WifiProStateMachine.this.logI("WiFiLinkMonitorState is Enter");
            initializeQoe();
            if (WifiProStateMachine.this.mCellToWiFiCnt >= 1) {
                WifiProStateMachine.this.logI("cell2wifi for one time or above");
                WifiProStateMachine.this.mCellToWiFiCnt = 0;
                this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
            }
            NetworkInfo wifiInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
            if (wifiInfo != null && wifiInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logI(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(131874);
            }
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
            }
            WifiProStateMachine.this.mWifiSwitchReason = 0;
            this.wifiMonitorCounter = 0;
            this.internetFailureDetectedCnt = 0;
            this.rssiLevel2ScanedCounter = 0;
            this.rssiLevel0Or1ScanedCounter = 0;
            this.isScreenOffMonitor = false;
            this.isAllowWiFiHandoverMobile = WifiProStateMachine.DBG;
            this.isCancelCHRTypeReport = false;
            this.isDisableWifiAutoSwitch = false;
            this.isRequestWifInetCheck = false;
            this.isNotifyInvalidLinkDetection = false;
            WifiProStateMachine.this.isVerifyWifiNoInternetTimeOut = false;
            WifiProStateMachine.this.hasHandledNoInternetResult = false;
            wiFiLinkMonitorStateInit(false);
            this.currWifiPoorlevel = 3;
            this.wifiLinkHoldTime = System.currentTimeMillis();
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime && (WifiProStateMachine.this.mChrWifiDisconnectStartTime > WifiProStateMachine.this.mChrRoveOutStartTime || WifiProStateMachine.this.mChrWifiDidableStartTime > WifiProStateMachine.this.mChrRoveOutStartTime)) {
                long disableRestoreTime = System.currentTimeMillis() - WifiProStateMachine.this.mChrWifiDidableStartTime;
                boolean ssidIsSame = false;
                if (!(WifiProStateMachine.this.mRoSsid == null || WifiProStateMachine.this.mCurrentSsid == null)) {
                    ssidIsSame = WifiProStateMachine.this.mRoSsid.equals(WifiProStateMachine.this.mCurrentSsid);
                }
                if (ssidIsSame && disableRestoreTime <= 30000 && WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseUserReopenWifiRiCount();
                }
            }
            WifiProStateMachine.this.mChrRoveOutStartTime = 0;
            WifiProStateMachine.this.mChrWifiDisconnectStartTime = 0;
            WifiProStateMachine.this.mChrWifiDidableStartTime = 0;
            if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.portalNetwork) {
                this.portalCheck = WifiProStateMachine.DBG;
                WifiProStateMachine.this.respCodeChrInfo = "";
                WifiProStateMachine.this.detectionNumSlow = 0;
                WifiProStateMachine.this.connectStartTime = System.currentTimeMillis();
                if (WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp == 0) {
                    WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp = System.currentTimeMillis();
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.updateWifiConfig(wifiProStateMachine.mCurrentWifiConfig);
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logI("periodic portal check: update portalAuthTimestamp =" + WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, 600000);
            }
            WifiProStateMachine.this.mChrSwitchToCellSuccFlagInLinkMonitor = false;
        }

        public void exit() {
            WifiProStateMachine.this.logI("WiFiLinkMonitorState is Exit");
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.setWifiMonitorEnabled(false);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY);
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
            }
            WifiProStateMachine.this.stopDualBandMonitor();
            this.detectCounter = 0;
            this.portalCheck = false;
            this.isToastDisplayed = false;
            this.isDialogDisplayed = false;
            WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = false;
            WifiProStateMachine.this.mDelayedRssiChangedByCalling = false;
            this.isWiFiHandoverPriority = false;
            this.isWifiHandoverMobileToastShowed = false;
            if (System.currentTimeMillis() - this.wifiLinkHoldTime > 1800000) {
                WifiProStateMachine.this.mCurrentVerfyCounter = 0;
                WifiProStateMachine.this.mCellToWiFiCnt = 0;
            }
            if (System.currentTimeMillis() - this.wifiLinkHoldTime >= 60000) {
                WifiProStateMachine.this.mLastTime = 0;
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG);
            if (!WifiProStateMachine.this.mChrSwitchToCellSuccFlagInLinkMonitor) {
                WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
            }
        }

        private void unRegisterCellCardImmediately() {
            if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("unRegisterCellCardImmediately, mIsMasterCardReady = " + this.mIsMasterCardReady + ", mIsSlaveCardReady = " + this.mIsSlaveCardReady);
                if (this.mIsMasterCardReady) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getMasterCardSubId());
                    this.mIsMasterCardReady = false;
                    WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
                }
                if (this.mIsSlaveCardReady) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getSlaveCardSubId());
                    this.mIsSlaveCardReady = false;
                    WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
                }
            }
        }

        private void unRegisterCellCardLatency() {
            if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD);
                WifiProStateMachine.this.logI("unRegisterCellCardLatency for two seconds");
                if (this.mIsMasterCardReady) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getMasterCardSubId(), 2000);
                    this.mIsMasterCardReady = false;
                    WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
                }
                if (this.mIsSlaveCardReady) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getSlaveCardSubId(), 2000);
                    this.mIsSlaveCardReady = false;
                    WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void unRegisterMasterCardImmediately() {
            WifiProStateMachine.this.logI("unRegister master card immediately due to activate timeout for 2s");
            WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
            if (WifiProStateMachine.this.mMasterCardNetworkCallback != null && WifiProStateMachine.this.mConnectivityManager != null) {
                WifiProStateMachine.this.logI("unregisterNetworkCallback for master card");
                WifiProStateMachine.this.mConnectivityManager.unregisterNetworkCallback(WifiProStateMachine.this.mMasterCardNetworkCallback);
                this.mMasterCardWaitQoeCnt = 0;
            }
        }

        private void initializeQoe() {
            WifiProStateMachine.this.logI("initializeQoe...");
            WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
            this.mIsNeedIgnoreQoeUpdate = false;
            this.mIsMasterCardReady = false;
            this.mIsSlaveCardReady = false;
            this.mMasterCardWaitQoeCnt = 0;
            this.mSlaveCardWaitQoeCnt = 0;
            if (WifiProStateMachine.this.mMasterCardNetworkCallback == null) {
                WifiProStateMachine.this.mMasterCardNetworkCallback = new ConnectivityManager.NetworkCallback() {
                    /* class com.huawei.hwwifiproservice.WifiProStateMachine.WiFiLinkMonitorState.AnonymousClass1 */

                    @Override // android.net.ConnectivityManager.NetworkCallback
                    public void onAvailable(Network network) {
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.WAIT_MASTER_CARD_QOE)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.WAIT_MASTER_CARD_QOE);
                            WifiProStateMachine.this.logI("master card is available");
                            WiFiLinkMonitorState.this.mIsMasterCardReady = WifiProStateMachine.DBG;
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getMasterCardSubId(), 1), 5000);
                            if (WiFiLinkMonitorState.this.detectCellQoe(network)) {
                                WiFiLinkMonitorState.this.mIsNeedIgnoreQoeUpdate = false;
                                return;
                            }
                            return;
                        }
                        WiFiLinkMonitorState.this.unRegisterMasterCardImmediately();
                    }
                };
            }
            if (WifiProStateMachine.this.mSlaveCardNetworkCallback == null) {
                WifiProStateMachine.this.mSlaveCardNetworkCallback = new ConnectivityManager.NetworkCallback() {
                    /* class com.huawei.hwwifiproservice.WifiProStateMachine.WiFiLinkMonitorState.AnonymousClass2 */

                    @Override // android.net.ConnectivityManager.NetworkCallback
                    public void onAvailable(Network network) {
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.WAIT_SLAVE_CARD_QOE)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.WAIT_SLAVE_CARD_QOE);
                        }
                        WifiProStateMachine.this.logI("slave card is available");
                        WiFiLinkMonitorState.this.mIsSlaveCardReady = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getSlaveCardSubId(), 1), 5000);
                        if (WiFiLinkMonitorState.this.detectCellQoe(network)) {
                            WiFiLinkMonitorState.this.mIsNeedIgnoreQoeUpdate = false;
                        }
                    }
                };
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean detectCellQoe(Network network) {
            if (network == null) {
                return false;
            }
            int netId = network.netId;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("onAvailable, detect the cell card, netId = " + netId);
            Bundle data = new Bundle();
            data.putInt("netId", netId);
            data.putInt("serverNum", 1);
            data.putInt("timeOut", 1000);
            data.putInt("detectTime", 3000);
            IHwCommBoosterServiceManager boosterManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
            if (boosterManager == null) {
                return false;
            }
            boosterManager.reportBoosterPara("com.huawei.hwwifiproservice", (int) WifiProStateMachine.HWEMCOM_REQ_INFO, data);
            return WifiProStateMachine.DBG;
        }

        private boolean tryToActivateMasterCard() {
            this.mIsMasterCardReady = false;
            int subId = WifiProCommonUtils.getMasterCardSubId();
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("try to activate master card, subId = " + subId);
            if (!WifiProCommonUtils.isValidSubId(subId)) {
                WifiProStateMachine.this.logE("subId is illegal");
                return false;
            } else if (WifiProCommonUtils.isNetworkType3G(WifiProStateMachine.this.mTelephonyManager, subId) || WifiProCommonUtils.isNetworkType2gOrUnknow(WifiProStateMachine.this.mTelephonyManager, subId)) {
                WifiProStateMachine.this.logE("can not activate master card, try to activate slave card");
                return false;
            } else if (WifiProStateMachine.this.mMasterSignalLevel < 2) {
                WifiProStateMachine.this.logE("master cell signal level is weak");
                return false;
            } else if (WifiProStateMachine.this.mAppQoeInfo.mMasterCardAmbr <= 0 || WifiProStateMachine.this.mAppQoeInfo.mMasterCardAmbr > 1024) {
                int defaultDataSubId = WifiProCommonUtils.getDefaultDataSubId();
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("defaultDataSubId = " + defaultDataSubId + ", mDsdsState = " + WifiProStateMachine.this.mDsdsState + ", mMasterRsrp = " + WifiProStateMachine.this.mMasterRsrp);
                if (WifiProStateMachine.this.mDsdsState != 0 || defaultDataSubId == subId) {
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    builder.addCapability(12);
                    builder.addTransportType(0);
                    builder.removeCapability(13);
                    builder.setNetworkSpecifier(String.valueOf(subId));
                    NetworkRequest mNetworkRequest = builder.build();
                    if (!(WifiProStateMachine.this.mMasterCardNetworkCallback == null || WifiProStateMachine.this.mConnectivityManager == null)) {
                        WifiProStateMachine.this.logI("MasterCardNetwork, requestNetwork");
                        try {
                            WifiProStateMachine.this.mConnectivityManager.requestNetwork(mNetworkRequest, WifiProStateMachine.this.mMasterCardNetworkCallback, 60000);
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.WAIT_MASTER_CARD_QOE, 2000);
                            return WifiProStateMachine.DBG;
                        } catch (ConnectivityManager.TooManyRequestsException e) {
                            WifiProStateMachine.this.logE("too many requests exception.");
                        }
                    }
                    return false;
                } else if (WifiProStateMachine.this.mMasterRsrp < WifiProStateMachine.THRESHOLD_GOOD_RSRP) {
                    return false;
                } else {
                    this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mAppQoeInfo.mMasterCellChannelQoeScore = 100;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT);
                    return WifiProStateMachine.DBG;
                }
            } else {
                WifiProStateMachine.this.logE("master card is speed limited, and can not be activated");
                return false;
            }
        }

        private boolean tryToActivateSlaveCard() {
            this.mIsSlaveCardReady = false;
            int subId = WifiProCommonUtils.getSlaveCardSubId();
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("try to activate slave card, subId = " + subId);
            if (!WifiProCommonUtils.isValidSubId(subId)) {
                WifiProStateMachine.this.logE("subId is illegal");
                return false;
            } else if (WifiProCommonUtils.isNetworkType3G(WifiProStateMachine.this.mTelephonyManager, subId) || WifiProCommonUtils.isNetworkType2gOrUnknow(WifiProStateMachine.this.mTelephonyManager, subId) || !WifiProCommonUtils.isDualCardStateOn(WifiProStateMachine.this.mContext)) {
                WifiProStateMachine.this.logE("can not activate slave card");
                return false;
            } else if (WifiProStateMachine.this.mSlaveSignalLevel < 2) {
                WifiProStateMachine.this.logE("slave cell signal level is weak");
                return false;
            } else if (WifiProStateMachine.this.mAppQoeInfo.mSlaveCardAmbr <= 0 || WifiProStateMachine.this.mAppQoeInfo.mSlaveCardAmbr > 1024) {
                int defaultDataSubId = WifiProCommonUtils.getDefaultDataSubId();
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("defaultDataSubId = " + defaultDataSubId + ", mDsdsState = " + WifiProStateMachine.this.mDsdsState + ", mSlaveRsrp = " + WifiProStateMachine.this.mSlaveRsrp);
                if (WifiProStateMachine.this.mDsdsState != 0 || defaultDataSubId == subId) {
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    builder.addCapability(12);
                    builder.addTransportType(0);
                    builder.removeCapability(13);
                    builder.setNetworkSpecifier(String.valueOf(subId));
                    NetworkRequest mNetworkRequest = builder.build();
                    if (!(WifiProStateMachine.this.mSlaveCardNetworkCallback == null || WifiProStateMachine.this.mConnectivityManager == null)) {
                        WifiProStateMachine.this.logI("SlaveCarddNetwork, requestNetwork");
                        try {
                            WifiProStateMachine.this.mConnectivityManager.requestNetwork(mNetworkRequest, WifiProStateMachine.this.mSlaveCardNetworkCallback, 60000);
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.WAIT_SLAVE_CARD_QOE, 2000);
                            return WifiProStateMachine.DBG;
                        } catch (ConnectivityManager.TooManyRequestsException e) {
                            WifiProStateMachine.this.logE("too many requests exception.");
                        }
                    }
                    return false;
                } else if (WifiProStateMachine.this.mSlaveRsrp < WifiProStateMachine.THRESHOLD_GOOD_RSRP) {
                    return false;
                } else {
                    this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mAppQoeInfo.mSlaveCellChannelQoeScore = 100;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT);
                    return WifiProStateMachine.DBG;
                }
            } else {
                WifiProStateMachine.this.logE("slave card is speed limited, and can not be activated");
                return false;
            }
        }

        private void handleMasterCardQoeReady() {
            this.mMasterCardWaitQoeCnt++;
            WifiProStateMachine.this.mStringBuffer.delete(0, WifiProStateMachine.this.mStringBuffer.length());
            StringBuffer stringBuffer = WifiProStateMachine.this.mStringBuffer;
            stringBuffer.append("mMasterCellChannelQoeScore = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mMasterCellChannelQoeScore);
            stringBuffer.append(", mMasterWifiChannelQoeScore = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeScore);
            stringBuffer.append(", mMasterCardWaitQoeCnt = ");
            stringBuffer.append(this.mMasterCardWaitQoeCnt);
            stringBuffer.append(", mMasterCardHttpProbeLatency = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mMasterCardHttpProbeLatency);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(wifiProStateMachine.mStringBuffer.toString());
            if (this.mMasterCardWaitQoeCnt > 3) {
                this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                this.mMasterCardWaitQoeCnt = 0;
                unRegisterCellCardImmediately();
                if (!tryToActivateSlaveCard()) {
                    if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
                        WifiProStateMachine.this.logI("can not handover to mobile ,delay 2 minutes go to Monitor");
                    }
                    WifiProStateMachine.this.handleWifi2CellFailChr(30);
                    this.isSwitching = false;
                }
            } else if (WifiProStateMachine.this.mAppQoeInfo.mMasterCellChannelQoeScore == 0 || WifiProStateMachine.this.mAppQoeInfo.mMasterCardHttpProbeLatency == 0) {
                WifiProStateMachine.this.logI("MasterCellChannelQoe and HttpProbe have not come back, please wait..");
            } else if (WifiProStateMachine.this.mAppQoeInfo.mMasterCellChannelQoeScore < WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeScore || ((WifiProStateMachine.this.mAppQoeInfo.mMasterCellChannelQoeLevel == 4 && WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeScore == 0) || (WifiProStateMachine.this.mAppQoeInfo.mMasterCardHttpProbeLatency > 0 && WifiProStateMachine.this.mAppQoeInfo.mMasterCardHttpProbeLatency <= WifiProStateMachine.CELL_CHANNEL_RTT_THRESHOLD))) {
                this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                this.mMasterCardWaitQoeCnt = 0;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT);
            }
        }

        private void handleSlaveCardQoeReady() {
            this.mSlaveCardWaitQoeCnt++;
            WifiProStateMachine.this.mStringBuffer.delete(0, WifiProStateMachine.this.mStringBuffer.length());
            StringBuffer stringBuffer = WifiProStateMachine.this.mStringBuffer;
            stringBuffer.append("mSlaveCellChannelQoeScore = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mSlaveCellChannelQoeScore);
            stringBuffer.append(", mMasterWifiChannelQoeScore = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeScore);
            stringBuffer.append(", mSlaveCardWaitQoeCnt = ");
            stringBuffer.append(this.mSlaveCardWaitQoeCnt);
            stringBuffer.append(", mSlaveCardHttpProbeLatency = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mSlaveCardHttpProbeLatency);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(wifiProStateMachine.mStringBuffer.toString());
            if (this.mSlaveCardWaitQoeCnt > 3) {
                this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                this.mSlaveCardWaitQoeCnt = 0;
                this.isSwitching = false;
                unRegisterCellCardImmediately();
                if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
                    WifiProStateMachine.this.logI("can not handover to mobile ,delay 2 minutes go to Monitor");
                }
                WifiProStateMachine.this.handleWifi2CellFailChr(30);
            } else if (WifiProStateMachine.this.mAppQoeInfo.mSlaveCellChannelQoeScore == 0 || WifiProStateMachine.this.mAppQoeInfo.mSlaveCardHttpProbeLatency == 0) {
                WifiProStateMachine.this.logI("SlaveCellChannelQoe and HttpProbe have not come back, please wait..");
            } else if (WifiProStateMachine.this.mAppQoeInfo.mSlaveCellChannelQoeScore < WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeScore || ((WifiProStateMachine.this.mAppQoeInfo.mSlaveCellChannelQoeLevel == 4 && WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeScore == 0) || (WifiProStateMachine.this.mAppQoeInfo.mSlaveCardHttpProbeLatency > 0 && WifiProStateMachine.this.mAppQoeInfo.mSlaveCardHttpProbeLatency <= WifiProStateMachine.CELL_CHANNEL_RTT_THRESHOLD))) {
                this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                this.mSlaveCardWaitQoeCnt = 0;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0129, code lost:
            if (r0.isWifiSignalBad(r0.mAppQoeInfo.mMasterWifiRssi) == false) goto L_0x012b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x016d, code lost:
            if (r0.isWifiSignalBad(r0.mAppQoeInfo.mMasterWifiRssi) == false) goto L_0x016f;
         */
        private void handleAppQoeInfo() {
            if (WifiProStateMachine.this.mAppQoeInfo.mNetworkQoeLevel == 5 && WifiProStateMachine.this.mAppQoeInfo.mMaxPktRatioChannelId == 2) {
                AppQoeInfo.access$22208(WifiProStateMachine.this.mAppQoeInfo);
                if ((WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowReason & 1) == 1 || (WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowReason & 2) == 2) {
                    AppQoeInfo.access$22408(WifiProStateMachine.this.mAppQoeInfo);
                } else {
                    WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowChannelBadCnt = 0;
                }
            } else {
                WifiProStateMachine.this.mAppQoeInfo.mAppQoeBadCnt = 0;
                WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowChannelBadCnt = 0;
            }
            if (WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeLevel == 5 && ((WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeSlowReason & 1) == 1 || (WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeSlowReason & 2) == 2)) {
                AppQoeInfo.access$22708(WifiProStateMachine.this.mAppQoeInfo);
            } else {
                WifiProStateMachine.this.mAppQoeInfo.mChannelQoeBadCnt = 0;
            }
            WifiProStateMachine.this.mStringBuffer.delete(0, WifiProStateMachine.this.mStringBuffer.length());
            StringBuffer stringBuffer = WifiProStateMachine.this.mStringBuffer;
            stringBuffer.append("mChannelQoeBadCnt = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mChannelQoeBadCnt);
            stringBuffer.append(", mAppQoeBadCnt = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mAppQoeBadCnt);
            stringBuffer.append(", mNetworkSlowReason = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowReason);
            stringBuffer.append(", mNetworkSlowChannelBadCnt = ");
            stringBuffer.append(WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowChannelBadCnt);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(wifiProStateMachine.mStringBuffer.toString());
            if (WifiProStateMachine.this.mAppQoeInfo.mChannelQoeBadCnt >= 1) {
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            }
            if (WifiProStateMachine.this.mAppQoeInfo.mChannelQoeBadCnt < 3) {
                if (WifiProStateMachine.this.mAppQoeInfo.mAppQoeBadCnt >= 1 && WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowChannelBadCnt >= 1) {
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                }
                if (WifiProStateMachine.this.mAppQoeInfo.mAppQoeBadCnt < 3 || WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowChannelBadCnt < 3) {
                    if (WifiProStateMachine.this.mAppQoeInfo.mAppQoeBadCnt >= 9 && WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.isEnterprise()) {
                        WifiProStateMachine.this.logI("enterprise ap, app qoe is bad, channel qoe is good, notify link poor");
                        WifiProStateMachine.this.notifyWifiLinkPoor(WifiProStateMachine.DBG, 11);
                        this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                        return;
                    }
                    return;
                }
                WifiProStateMachine.this.logI("app and channel qoe all bad, notify link poor");
                WifiProStateMachine.this.notifyWifiLinkPoor(WifiProStateMachine.DBG, 10);
                this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
                return;
            }
            WifiProStateMachine.this.logI("channel qoe is bad, notify link poor");
            WifiProStateMachine.this.notifyWifiLinkPoor(WifiProStateMachine.DBG, 10);
            this.mIsNeedIgnoreQoeUpdate = WifiProStateMachine.DBG;
        }

        private boolean isTxRxRateQualityGood(int uplinkRate, int downlinkRate) {
            if (uplinkRate + downlinkRate < 5000 || WifiProStateMachine.this.mCurrentWifiConfig == null || !WifiProStateMachine.this.mCurrentWifiConfig.isEnterprise()) {
                return false;
            }
            return WifiProStateMachine.DBG;
        }

        private void updateAppQoeInfo(Message msg) {
            WifiProStateMachine.this.mStringBuffer.delete(0, WifiProStateMachine.this.mStringBuffer.length());
            StringBuffer stringBuffer = WifiProStateMachine.this.mStringBuffer;
            stringBuffer.append("isWifi2WifiProcess = ");
            stringBuffer.append(this.isWifi2WifiProcess);
            stringBuffer.append(", mIsNeedIgnoreQoeUpdate = ");
            stringBuffer.append(this.mIsNeedIgnoreQoeUpdate);
            stringBuffer.append(", mIsAppInBlacklist = ");
            stringBuffer.append(this.mIsAppInBlacklist);
            stringBuffer.append(", mIsAppInBlacklistFullScreen = ");
            stringBuffer.append(this.mIsAppInBlacklistFullScreen);
            stringBuffer.append(", mIsWiFiNoInternet ");
            stringBuffer.append(WifiProStateMachine.this.mIsWiFiNoInternet);
            stringBuffer.append(", screen on = ");
            stringBuffer.append(WifiProStateMachine.this.mPowerManager.isScreenOn());
            stringBuffer.append(", full screen = ");
            stringBuffer.append(WifiProStateMachine.this.isFullscreen());
            stringBuffer.append(", isLandscapeMode = ");
            stringBuffer.append(WifiProCommonUtils.isLandscapeMode(WifiProStateMachine.this.mContext));
            stringBuffer.append(", mNotifyWifiLinkPoorReason = ");
            stringBuffer.append(WifiProStateMachine.this.mNotifyWifiLinkPoorReason);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(wifiProStateMachine.mStringBuffer.toString());
            if (this.isWifi2WifiProcess || this.mIsNeedIgnoreQoeUpdate || WifiProStateMachine.this.mIsWiFiNoInternet || !WifiProStateMachine.this.mPowerManager.isScreenOn() || this.mIsAppInBlacklist || (this.mIsAppInBlacklistFullScreen && ((WifiProStateMachine.this.isFullscreen() || WifiProCommonUtils.isLandscapeMode(WifiProStateMachine.this.mContext)) && WifiProStateMachine.this.mNotifyWifiLinkPoorReason != 107))) {
                WifiProStateMachine.this.logI("ignore app qoe info update");
                return;
            }
            Bundle data = null;
            if (msg.obj instanceof Bundle) {
                data = (Bundle) msg.obj;
            }
            if (data != null) {
                WifiProStateMachine.this.mAppQoeInfo.parseQoeInfo(data);
                if (isTxRxRateQualityGood(WifiProStateMachine.this.mAppQoeInfo.mMasterWifiUplinkRate, WifiProStateMachine.this.mAppQoeInfo.mMasterWifiDownlinkRate)) {
                    WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
                    WifiProStateMachine.this.logI("ignore app qoe info update, because current wifi link rate is good");
                } else if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.WAIT_MASTER_CARD_QOE) || WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.WAIT_SLAVE_CARD_QOE)) {
                    WifiProStateMachine.this.logI("take no action during activate the cell..");
                } else if (this.mIsMasterCardReady) {
                    WifiProStateMachine.this.logI("Master card is ready, check..");
                    handleMasterCardQoeReady();
                } else if (this.mIsSlaveCardReady) {
                    WifiProStateMachine.this.logI("Slave card is ready, check..");
                    handleSlaveCardQoeReady();
                } else {
                    handleAppQoeInfo();
                }
            }
        }

        private boolean isAppInBlacklist(String packageName) {
            if (packageName == null) {
                return false;
            }
            if (this.mApkConfigList == null) {
                this.mHwAppQoeResourceManager = HwAppQoeResourceManager.getInstance();
                HwAppQoeResourceManager hwAppQoeResourceManager = this.mHwAppQoeResourceManager;
                if (hwAppQoeResourceManager == null) {
                    return false;
                }
                this.mApkConfigList = hwAppQoeResourceManager.getAPKConfigList();
                if (this.mApkConfigList == null) {
                    return false;
                }
            }
            for (HwAppQoeApkConfig apkConfig : this.mApkConfigList) {
                if (apkConfig != null && packageName.equals(apkConfig.packageName) && apkConfig.getWlanPlus() == 1 && apkConfig.getSwitchType() == 0) {
                    Log.i(WifiProStateMachine.TAG, "app: " + apkConfig.packageName + " in blacklist, do not monitor.");
                    return WifiProStateMachine.DBG;
                }
            }
            return false;
        }

        private boolean isAppInBlacklistJson(String packageName) {
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (WifiProStateMachine.this.mWifiProBlacklist == null || WifiProStateMachine.this.mWifiProBlacklist.isEmpty()) {
                WifiProStateMachine.this.mWifiProBlacklist = WifiProCommonUtils.getWifiProBlacklist(WifiProStateMachine.APK_INFO);
                if (WifiProStateMachine.this.mWifiProBlacklist == null || WifiProStateMachine.this.mWifiProBlacklist.isEmpty()) {
                    return false;
                }
            }
            if (!WifiProStateMachine.this.mWifiProBlacklist.contains(packageName)) {
                return false;
            }
            Log.i(WifiProStateMachine.TAG, "app: " + packageName + " in blacklist, do not monitor.");
            return WifiProStateMachine.DBG;
        }

        private boolean isAppInBlacklistFullScreenJson(String packageName) {
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (WifiProStateMachine.this.mWifiProBlacklistFullScreen == null || WifiProStateMachine.this.mWifiProBlacklistFullScreen.isEmpty()) {
                WifiProStateMachine.this.mWifiProBlacklistFullScreen = WifiProCommonUtils.getWifiProBlacklist(WifiProStateMachine.APK_INFO_EXTERN);
                if (WifiProStateMachine.this.mWifiProBlacklistFullScreen == null || WifiProStateMachine.this.mWifiProBlacklistFullScreen.isEmpty()) {
                    return false;
                }
            }
            if (!WifiProStateMachine.this.mWifiProBlacklistFullScreen.contains(packageName)) {
                return false;
            }
            Log.i(WifiProStateMachine.TAG, "app: " + packageName + " in blacklist, full screen no switch.");
            return WifiProStateMachine.DBG;
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            switch (i) {
                case WifiProStateMachine.EVENT_APP_CHANGED /* 502 */:
                    if (HwAutoConnectManager.getInstance() != null) {
                        String pkgName = HwAutoConnectManager.getInstance().getCurrentPackageName();
                        WifiProChr.getInstance().setChrForePkgName(pkgName);
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.logI("current app " + pkgName + ", has changed");
                        this.mIsAppInBlacklist = isAppInBlacklistJson(pkgName);
                        this.mIsAppInBlacklistFullScreen = isAppInBlacklistFullScreenJson(pkgName);
                        WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
                        break;
                    }
                    break;
                case WifiProStateMachine.APP_SERVICE_QOE_NOTIFY /* 12001 */:
                    updateAppQoeInfo(msg);
                    break;
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /* 136172 */:
                    handleWifiQosChangedInLinkMonitorState(msg);
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /* 136186 */:
                    handleMobileDataStateChange();
                    break;
                case WifiProStateMachine.EVENT_CALL_STATE_CHANGED /* 136201 */:
                    handleCallStateChanged(msg);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_STATUS /* 136210 */:
                    checkWifiNetworkStatus();
                    break;
                case WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT /* 136224 */:
                    int level = msg.arg1;
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logI("EVENT_DISPATCH_INTERNET_RESULT in LinkMonitor, level = " + level);
                    WifiProStateMachine.this.handleInternetResultForDisplay(level, msg.arg2);
                    break;
                case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /* 136311 */:
                    WifiProStateMachine.this.handleGetWifiTcpRx();
                    break;
                case WifiProStateMachine.EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG /* 136375 */:
                    WifiProStateMachine.this.handleWifi2CellFailChr(WifiProChr.WIFI_2_CELL_FAIL_REASON_MAX_OTHERS);
                    break;
                default:
                    switch (i) {
                        case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /* 136169 */:
                            handleWifiNetworkStateChange(msg);
                            break;
                        case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /* 136170 */:
                            if (!this.isScreenOffMonitor) {
                                WifiProStateMachine.this.logI("device screen on,but isScreenOffMonitor is false");
                                break;
                            } else {
                                WifiProStateMachine.this.logI("device screen on,reinitialize wifi monitor");
                                this.isScreenOffMonitor = false;
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
                                break;
                            }
                        default:
                            switch (i) {
                                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /* 136176 */:
                                    handleCheckResultInLinkMonitorState(msg);
                                    break;
                                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /* 136177 */:
                                    handleNetworkConnectivityChange(msg);
                                    break;
                                case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /* 136178 */:
                                    handleWifiHandoverResult(msg);
                                    break;
                                case WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE /* 136179 */:
                                    handleRssiChangedInLinkMonitorState(msg);
                                    break;
                                case WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT /* 136180 */:
                                    tryWifi2Mobile(msg.arg1);
                                    break;
                                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /* 136181 */:
                                    handleWifiInternetResultInLinkMonitorState(msg);
                                    break;
                                case WifiProStateMachine.EVENT_DIALOG_OK /* 136182 */:
                                    handleEventDialog(msg);
                                    break;
                                case WifiProStateMachine.EVENT_DIALOG_CANCEL /* 136183 */:
                                    handleDilaogCancel(msg);
                                    break;
                                case WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR /* 136184 */:
                                    handleDelayResetWifi();
                                    break;
                                default:
                                    switch (i) {
                                        case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /* 136190 */:
                                            handleEmuiCspSettingChange();
                                            break;
                                        case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /* 136191 */:
                                            retryWifi2Wifi();
                                            break;
                                        case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /* 136192 */:
                                            handleCheckWifiInternet();
                                            break;
                                        default:
                                            switch (i) {
                                                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /* 136195 */:
                                                    handleHttpReachableResult(msg);
                                                    break;
                                                case WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY /* 136196 */:
                                                    handleReuqestScanInLinkMonitorState(msg);
                                                    break;
                                                case WifiProStateMachine.EVENT_CONFIGURATION_CHANGED /* 136197 */:
                                                    handleOrientationChanged(msg);
                                                    break;
                                                case WifiProStateMachine.EVENT_NOTIFY_WIFI_LINK_POOR /* 136198 */:
                                                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                                                    wifiProStateMachine3.logI("EVENT_NOTIFY_WIFI_LINK_POOR isDisableWifiAutoSwitch = " + this.isDisableWifiAutoSwitch);
                                                    if (this.isDisableWifiAutoSwitch) {
                                                        WifiProStateMachine.this.handleWifi2CellFailChr(21);
                                                        break;
                                                    } else {
                                                        WifiProStateMachine.this.mCurrentWifiLevel = 0;
                                                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, 0, 0, false);
                                                        break;
                                                    }
                                                case WifiProStateMachine.EVENT_TRY_WIFI_ROVE_OUT /* 136199 */:
                                                    handleWiFiRoveOut();
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW /* 136204 */:
                                                            if (WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                                                                WifiProStateMachine.this.handlePeriodPortalCheck();
                                                                break;
                                                            }
                                                            break;
                                                        case WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST /* 136205 */:
                                                            if (HwAutoConnectManager.getInstance() != null) {
                                                                HwAutoConnectManager.getInstance().checkPortalAuthExpiration();
                                                                break;
                                                            }
                                                            break;
                                                        case WifiProStateMachine.EVENT_DEVICE_SCREEN_OFF /* 136206 */:
                                                            if (this.portalCheck) {
                                                                WifiProStateMachine.this.logI("periodic portal check: screen off, remove msg");
                                                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
                                                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
                                                                break;
                                                            }
                                                            break;
                                                        case WifiProStateMachine.EVENT_DEVICE_USER_PRESENT /* 136207 */:
                                                            handleDeviceUserPresent();
                                                            break;
                                                        case WifiProStateMachine.EVENT_CHECK_PORTAL_AUTH_CHECK_RESULT /* 136208 */:
                                                            handlePortalAuthCheckResultInLinkMonitorState(msg);
                                                            break;
                                                        default:
                                                            switch (i) {
                                                                case WifiProStateMachine.WAIT_MASTER_CARD_QOE /* 136216 */:
                                                                    unRegisterCellCardImmediately();
                                                                    if (!tryToActivateSlaveCard()) {
                                                                        this.isSwitching = false;
                                                                        if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                                                                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
                                                                            WifiProStateMachine.this.logI("can not handover to mobile ,delay 2 minutes go to Monitor");
                                                                        }
                                                                        WifiProStateMachine.this.handleWifi2CellFailChr(15);
                                                                        break;
                                                                    }
                                                                    break;
                                                                case WifiProStateMachine.WAIT_SLAVE_CARD_QOE /* 136217 */:
                                                                    unRegisterCellCardImmediately();
                                                                    this.isSwitching = false;
                                                                    if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                                                                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
                                                                        WifiProStateMachine.this.logI("can not handover to mobile ,delay 2 minutes go to Monitor");
                                                                    }
                                                                    WifiProStateMachine.this.handleWifi2CellFailChr(15);
                                                                    break;
                                                                default:
                                                                    switch (i) {
                                                                        case WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER /* 136221 */:
                                                                            initializeQoe();
                                                                            WifiProStateMachine.this.logI("two minutes have expired, please check qoe again");
                                                                            break;
                                                                        case WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD /* 136222 */:
                                                                            int subId = msg.arg1;
                                                                            if (msg.arg2 == 1) {
                                                                                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                                                                                wifiProStateMachine4.logI("subId = " + subId + ",5s timeout, reset switch control");
                                                                                this.mIsNeedIgnoreQoeUpdate = false;
                                                                                this.isSwitching = false;
                                                                                WifiProStateMachine.this.mAppQoeInfo.mChannelQoeBadCnt = 0;
                                                                                WifiProStateMachine.this.mAppQoeInfo.mAppQoeBadCnt = 0;
                                                                                WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowChannelBadCnt = 0;
                                                                            }
                                                                            if (WifiProCommonUtils.isValidSubId(subId)) {
                                                                                if (subId != WifiProCommonUtils.getMasterCardSubId() || WifiProStateMachine.this.mMasterCardNetworkCallback == null || WifiProStateMachine.this.mConnectivityManager == null) {
                                                                                    if (subId == WifiProCommonUtils.getSlaveCardSubId() && WifiProStateMachine.this.mSlaveCardNetworkCallback != null && WifiProStateMachine.this.mConnectivityManager != null) {
                                                                                        WifiProStateMachine.this.logI("EVENT_UNREGISTER_CELL_CARD SlaveCarddNetwork, unregisterNetworkCallback");
                                                                                        WifiProStateMachine.this.mConnectivityManager.unregisterNetworkCallback(WifiProStateMachine.this.mSlaveCardNetworkCallback);
                                                                                        this.mIsSlaveCardReady = false;
                                                                                        this.mSlaveCardWaitQoeCnt = 0;
                                                                                        break;
                                                                                    } else {
                                                                                        WifiProStateMachine.this.logI("illegal subId");
                                                                                        break;
                                                                                    }
                                                                                } else {
                                                                                    WifiProStateMachine.this.logI("EVENT_UNREGISTER_CELL_CARD MasterCardNetwork, unregisterNetworkCallback");
                                                                                    WifiProStateMachine.this.mConnectivityManager.unregisterNetworkCallback(WifiProStateMachine.this.mMasterCardNetworkCallback);
                                                                                    this.mIsMasterCardReady = false;
                                                                                    this.mMasterCardWaitQoeCnt = 0;
                                                                                    break;
                                                                                }
                                                                            }
                                                                            break;
                                                                        default:
                                                                            switch (i) {
                                                                                case WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT /* 136299 */:
                                                                                    handleEvaluteResult(msg);
                                                                                    break;
                                                                                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /* 136300 */:
                                                                                    WifiProStateMachine.this.handleWifiEvaluteChange();
                                                                                    break;
                                                                                default:
                                                                                    switch (i) {
                                                                                        case WifiProStateMachine.EVENT_DUALBAND_RSSITH_RESULT /* 136368 */:
                                                                                            handleDualbandRssithResult(msg);
                                                                                            break;
                                                                                        case WifiProStateMachine.EVENT_DUALBAND_SCORE_RESULT /* 136369 */:
                                                                                            handleDualbandScoreResult(msg);
                                                                                            break;
                                                                                        case WifiProStateMachine.EVENT_DUALBAND_5GAP_AVAILABLE /* 136370 */:
                                                                                            handleDualbandApAvailable();
                                                                                            break;
                                                                                        case WifiProStateMachine.EVENT_DUALBAND_WIFI_HANDOVER_RESULT /* 136371 */:
                                                                                            handleDualbandHandoverResult(msg);
                                                                                            break;
                                                                                        case WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY /* 136372 */:
                                                                                            WifiProStateMachine.this.logI("receive dual band wifi handover delay retry");
                                                                                            WifiProStateMachine.this.retryDualBandAPMonitor();
                                                                                            break;
                                                                                        default:
                                                                                            return false;
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
            return WifiProStateMachine.DBG;
        }

        private void handleHttpReachableResult(Message msg) {
            if (msg.obj != null && (msg.obj instanceof Boolean) && ((Boolean) msg.obj).booleanValue()) {
                this.internetFailureDetectedCnt = 0;
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.reportNetworkConnectivity(WifiProStateMachine.DBG);
                }
                WifiProStateMachine.this.mIsWiFiNoInternet = false;
                WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                WifiProStateMachine.this.onNetworkDetectionResult(1, 5);
            } else if (msg.obj != null && (msg.obj instanceof Boolean) && !((Boolean) msg.obj).booleanValue()) {
                WifiProStateMachine.this.logI("EVENT_HTTP_REACHABLE_RESULT = false, SCE notify WLAN+.");
                this.isNotifyInvalidLinkDetection = WifiProStateMachine.DBG;
                if (WifiProStateMachine.this.mIsUserManualConnectSuccess && !WifiProStateMachine.this.mIsWiFiProEnabled) {
                    WifiProStateMachine.this.updateChrToCell(false);
                    WifiProStateMachine.this.sendInvalidLinkDetected();
                }
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT, -1);
                WifiProStateMachine.this.mIsWiFiInternetCHRFlag = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, 0, 0, false);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mWifiTcpRxCount = wifiProStateMachine.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
            }
        }

        private void handleEvaluteResult(Message msg) {
            if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                int rttlevel = msg.arg1;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI(StringUtilEx.safeDisplaySsid(WifiProStateMachine.this.mCurrentSsid) + "  TCPRTT  level = " + rttlevel);
                if (rttlevel <= 0 || rttlevel > 3) {
                    rttlevel = 0;
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(WifiProStateMachine.this.mCurrentSsid);
                }
                updateWifiQosLevel(false, rttlevel);
            }
        }

        private void handleDilaogCancel(Message msg) {
            if (msg.arg1 == 101) {
                WifiProStateMachine.this.logI("WiFiLinkMonitorState::Click CANCEL ,User don't want wifi switch.");
                this.isDisableWifiAutoSwitch = WifiProStateMachine.DBG;
                this.isNoInternetDialogShowing = false;
            } else if (this.isWifi2MobileUIShowing) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("isDialogDisplayed : " + this.isDialogDisplayed + ", mIsWiFiNoInternet " + WifiProStateMachine.this.mIsWiFiNoInternet);
                if (this.isDialogDisplayed) {
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
                    } else {
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(2);
                    }
                } else if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                } else {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseBqeBadSettingCancelCount();
                }
                this.isDialogDisplayed = false;
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                this.isWifi2MobileUIShowing = false;
                this.isAllowWiFiHandoverMobile = false;
                WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                this.isSwitching = false;
                WifiProStateMachine.this.mWiFiProPdpSwichValue = 2;
                WifiProStateMachine.this.logI("Click Cancel ,is not allow wifi handover mobile, WiFiProPdp is CANNOT");
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DBG;
                    if (WifiProStateMachine.this.mCurrentWifiConfig == null || !WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess || WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason != 0) {
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                        return;
                    }
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.mWifiTcpRxCount = wifiProStateMachine2.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                    return;
                }
                WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
            }
        }

        private void handleEventDialog(Message msg) {
            int roReason;
            if (msg.arg1 == 101) {
                WifiProStateMachine.this.logI("WiFiLinkMonitorState::Click OK ,User start wifi switch.");
                this.isDisableWifiAutoSwitch = false;
                this.isNoInternetDialogShowing = false;
                this.isSwitching = WifiProStateMachine.DBG;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_TRY_WIFI_ROVE_OUT);
            } else if (this.isWifi2MobileUIShowing) {
                this.isDialogDisplayed = false;
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                this.isWifi2MobileUIShowing = false;
                WifiProStateMachine.this.mWiFiProPdpSwichValue = 1;
                WifiProStateMachine.this.setWifiCSPState(0);
                WifiProStateMachine.this.logI("Click OK ,is send message to wifi handover mobile ,WiFiProPdp is AUTO");
                if (WifiProStateMachine.this.mIsMobileDataEnabled && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mEmuiPdpSwichValue != 2) {
                    this.isAllowWiFiHandoverMobile = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.logI("mWsmChannel send Poor Link Detected");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(131873);
                    if (this.currWifiPoorlevel == -1) {
                        roReason = 2;
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetHandoverCount();
                    } else {
                        roReason = 1;
                    }
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("roReason = " + roReason);
                    WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveOutEvent(roReason);
                }
                WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            }
        }

        private void handleDualbandScoreResult(Message msg) {
            if (this.isWifi2WifiProcess) {
                WifiProStateMachine.this.logI("isWifi2WifiProcess is true, ignore this message");
                WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DBG;
                return;
            }
            WifiProEstimateApInfo estimateApInfo = null;
            if (msg.obj instanceof WifiProEstimateApInfo) {
                estimateApInfo = (WifiProEstimateApInfo) msg.obj;
                WifiProStateMachine.this.logI("EVENT_DUALBAND_SCORE_RESULT estimateApInfo: " + estimateApInfo.toString());
            } else {
                WifiProStateMachine.this.logE("handleDualbandScoreResult:Class is not match");
            }
            if (WifiProStateMachine.this.mDualBandEstimateInfoSize > 0) {
                WifiProStateMachine.this.mDualBandEstimateInfoSize--;
                WifiProStateMachine.this.updateDualBandEstimateInfo(estimateApInfo);
            }
            WifiProStateMachine.this.logI("mDualBandEstimateInfoSize = " + WifiProStateMachine.this.mDualBandEstimateInfoSize);
            if (WifiProStateMachine.this.mDualBandEstimateInfoSize == 0) {
                WifiProStateMachine.this.chooseAvalibleDualBandAp();
            }
        }

        private void handleDualbandRssithResult(Message msg) {
            if (this.isWifi2WifiProcess) {
                WifiProStateMachine.this.logI("isWifi2WifiProcess is true, ignore this message");
                WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DBG;
                return;
            }
            WifiProEstimateApInfo apInfo = null;
            if (msg.obj instanceof WifiProEstimateApInfo) {
                apInfo = (WifiProEstimateApInfo) msg.obj;
            } else {
                WifiProStateMachine.this.logE("handleDualbandRssithResult:Class is not match");
            }
            if (WifiProStateMachine.this.mDualBandMonitorInfoSize > 0) {
                WifiProStateMachine.this.mDualBandMonitorInfoSize--;
                WifiProStateMachine.this.updateDualBandMonitorInfo(apInfo);
            }
            if (WifiProStateMachine.this.mDualBandMonitorInfoSize == 0 && WifiProStateMachine.this.mDualBandManager != null) {
                WifiProStateMachine.this.mDualBandMonitorStart = WifiProStateMachine.DBG;
                WifiProStateMachine.this.logI("Start dual band Manager monitor");
                WifiProStateMachine.this.mDualBandManager.startMonitor(WifiProStateMachine.this.mDualBandMonitorApList);
            }
        }

        private void handleWifiHandoverResult(Message msg) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("receive wifi handover wifi Result,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
            if (!this.isWifi2WifiProcess) {
                WifiProStateMachine.this.logE("isWifi2WifiProcess false, return");
            } else if (msg.obj == null || !(msg.obj instanceof Boolean) || !((Boolean) msg.obj).booleanValue()) {
                if (msg.arg1 == 22) {
                    this.isSwitching = false;
                    if (WifiProStateMachine.this.mHandoverFailReason != 7) {
                        WifiProStateMachine.this.mHandoverFailReason = 7;
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.uploadWifiSwitchFailTypeStatistics(wifiProStateMachine2.mHandoverFailReason);
                    }
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.logI("WiFi2WiFi failed because no candidate, try to trigger selfcure");
                        pendingMsgBySelfCureEngine(-103);
                    }
                }
                wifi2WifiFailed();
            } else {
                WifiProStateMachine.this.logI(" wifi --> wifi is  succeed");
                WifiProChrUploadManager.uploadDisconnectedEvent(WifiProStateMachine.EVENT_SSID_SWITCH_FINISHED);
                if (WifiProStateMachine.this.uploadManager != null) {
                    Bundle ssidSwitchSucc = new Bundle();
                    ssidSwitchSucc.putInt("index", 0);
                    WifiProStateMachine.this.uploadManager.addChrBundleStat("wifiSwitchCntEvent", "wifiSwitchSuccCnt", ssidSwitchSucc);
                }
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.uploadChrWifiHandoverTypeStatistics(wifiProStateMachine3.mChrWifiHandoverType, WifiProStateMachine.HANDOVER_SUCC_CNT);
                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                wifiProStateMachine4.uploadWifiSwitchStatistics(wifiProStateMachine4.mIsWiFiNoInternet ^ WifiProStateMachine.DBG);
                WifiProStateMachine.this.mWifiHandoverSucceedTimestamp = SystemClock.elapsedRealtime();
                this.isSwitching = false;
                WifiProStateMachine.this.mIsUserManualConnectSuccess = false;
                WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mBadBssid);
                WifiProStateMachine wifiProStateMachine5 = WifiProStateMachine.this;
                wifiProStateMachine5.addDualBandBlackList(wifiProStateMachine5.mBadBssid);
                WifiProStateMachine.this.resetWifiEvaluteQosLevel();
                WifiProStateMachine.this.refreshConnectedNetWork();
                WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                this.isWifi2WifiProcess = false;
                if (WifiProStateMachine.this.mWifiHandover.isWiFiSameSsidSwitching()) {
                    WifiProStateMachine.this.logI("after same ssid wifi2wifi success and request network check");
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                }
                WifiProStateMachine.this.handleWifi2CellFailChr(12);
                WifiProStateMachine wifiProStateMachine6 = WifiProStateMachine.this;
                wifiProStateMachine6.transitionTo(wifiProStateMachine6.mWifiConnectedState);
            }
        }

        private void handleMobileDataStateChange() {
            WifiProStateMachine.this.logI("WiFiLinkMonitorState : Receive Mobile changed");
            if (WifiProStateMachine.this.isMobileDataConnected() && this.isAllowWiFiHandoverMobile) {
                this.isCheckWiFiForUpdateSetting = false;
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                } else {
                    WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                }
            }
        }

        private void handleDelayResetWifi() {
            WifiProStateMachine.this.logI("ReIniitalize,ScreenOn == " + WifiProStateMachine.this.mPowerManager.isScreenOn());
            if (WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                this.wifiMonitorCounter++;
                int i = this.wifiMonitorCounter;
                if (i >= 4) {
                    if (i > 12) {
                        i = 12;
                    }
                    this.wifiMonitorCounter = i;
                    long delayTime = ((long) Math.pow(2.0d, (double) (this.wifiMonitorCounter / 4))) * 60 * 1000;
                    WifiProStateMachine.this.logI("delayTime = " + delayTime);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delayTime);
                    if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isCheckWiFiForUpdateSetting) {
                        this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                    }
                    if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.mWifiTcpRxCount = wifiProStateMachine.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                    }
                } else {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                }
                WifiProStateMachine.this.logI("wifiMonitorCounter = " + this.wifiMonitorCounter);
                initializeQoe();
                return;
            }
            this.isScreenOffMonitor = WifiProStateMachine.DBG;
        }

        private void handleDeviceUserPresent() {
            if (this.portalCheck) {
                WifiProStateMachine.this.logI("periodic portal check: screen unlocked, perform portal check right now");
                WifiProStateMachine.this.isPeriodicDet = false;
                if (HwAutoConnectManager.getInstance() != null) {
                    HwAutoConnectManager.getInstance().checkPortalAuthExpiration();
                }
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, 600000);
            }
        }

        private void handleCheckWifiInternet() {
            if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                return;
            }
            if (this.isCheckWiFiForUpdateSetting || this.isDialogDisplayed) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("queryNetworkQos for wifi , isCheckWiFiForUpdateSetting =" + this.isCheckWiFiForUpdateSetting + ", isDialogDisplayed =" + this.isDialogDisplayed);
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.mWifiTcpRxCount = wifiProStateMachine2.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
            }
        }

        private void retryWifi2Wifi() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("receive : EVENT_RETRY_WIFI_TO_WIFI, no internet = " + WifiProStateMachine.this.mIsWiFiNoInternet);
            boolean internetRecheck = false;
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                internetRecheck = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
            }
            this.isCheckWiFiForUpdateSetting = false;
            wiFiLinkMonitorStateInit(internetRecheck);
        }

        private void handleWifiNetworkStateChange(Message msg) {
            if (msg.obj instanceof Intent) {
                Object objNetworkInfo = ((Intent) msg.obj).getParcelableExtra("networkInfo");
                NetworkInfo networkInfo = null;
                if (objNetworkInfo instanceof NetworkInfo) {
                    networkInfo = (NetworkInfo) objNetworkInfo;
                } else {
                    WifiProStateMachine.this.logE("handleWifiNetworkStateChange:networkInfo is not match the class");
                }
                if (networkInfo != null && NetworkInfo.DetailedState.VERIFYING_POOR_LINK == networkInfo.getDetailedState()) {
                    WifiProStateMachine.this.logI("wifi handover mobile is Complete!");
                    this.isSwitching = false;
                    WifiProStateMachine.this.mHandoverCellSuccessTime = SystemClock.elapsedRealtime();
                    if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG)) {
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.updateSwitchTimeStatistics(wifiProStateMachine.mNotifyWifiLinkPoorReason);
                        WifiProChr instance = WifiProChr.getInstance();
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        instance.setChrSwitchCellSuccTriggerType(wifiProStateMachine2.getReasonType(wifiProStateMachine2.mNotifyWifiLinkPoorReason));
                        WifiProStateMachine.this.mChrSwitchToCellSuccFlagInLinkMonitor = WifiProStateMachine.DBG;
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.handleWifi2CellSuccChr(wifiProStateMachine3.mNotifyWifiLinkPoorReason);
                    }
                    WifiProStateMachine.this.uploadWifiSwitchStatistics(WifiProStateMachine.DBG);
                    WifiProStateMachine.this.uploadChrWifiHandoverTypeStatistics(WifiProStateMachine.WIFI_HANDOVER_TYPES[1], WifiProStateMachine.HANDOVER_SUCC_CNT);
                    WifiProStateMachine.this.showWifiToCellToast();
                    WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                    wifiProStateMachine4.transitionTo(wifiProStateMachine4.mWiFiProVerfyingLinkState);
                } else if (networkInfo != null && NetworkInfo.State.DISCONNECTED == networkInfo.getState()) {
                    WifiProStateMachine wifiProStateMachine5 = WifiProStateMachine.this;
                    wifiProStateMachine5.logI("wifi has disconnected,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                    if (!this.isWifi2WifiProcess && !WifiProStateMachine.this.mIsWiFiNoInternet && WifiProStateMachine.this.mCurrentRssi < -75) {
                        WifiProStateMachine.this.setLastDisconnectNetwork();
                    }
                    if (!this.isWifi2WifiProcess || !WifiProStateMachine.this.mWifiManager.isWifiEnabled()) {
                        WifiProStateMachine.this.handleWifi2CellFailChr(1);
                        WifiProStateMachine wifiProStateMachine6 = WifiProStateMachine.this;
                        wifiProStateMachine6.transitionTo(wifiProStateMachine6.mWifiDisConnectedState);
                    }
                }
            } else {
                WifiProStateMachine.this.logE("handleWifiNetworkStateChange:intent is not match the class");
            }
        }

        private boolean isStrongRssi() {
            if (WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo()) >= 3) {
                return WifiProStateMachine.DBG;
            }
            return false;
        }

        private void handleNetworkConnectivityChange(Message msg) {
            if (msg.obj instanceof Intent) {
                int networkType = ((Intent) msg.obj).getIntExtra("networkType", 1);
                NetworkInfo mobileInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(0);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("network change, isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                if (networkType == 0 && mobileInfo != null && mobileInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED && !WifiProStateMachine.this.isWifiNetworkCapabilityValidated() && !this.isWifi2WifiProcess) {
                    if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isWifiHandoverMobileToastShowed) {
                        WifiProStateMachine.this.showWifiToCellToast();
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.updateChrToCellSucc(wifiProStateMachine2.mIsWiFiNoInternet ^ WifiProStateMachine.DBG);
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.uploadWifiSwitchStatistics(wifiProStateMachine3.mIsWiFiNoInternet ^ WifiProStateMachine.DBG);
                        WifiProStateMachine.this.uploadChrWifiHandoverTypeStatistics(WifiProStateMachine.WIFI_HANDOVER_TYPES[0], WifiProStateMachine.HANDOVER_SUCC_CNT);
                    }
                    if (!WifiProStateMachine.this.mIsWiFiNoInternet && WifiProStateMachine.this.mLastWifiLevel != -1) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT, -1);
                        this.isCheckWiFiForUpdateSetting = WifiProStateMachine.DBG;
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, -1, 1);
                    }
                    this.isWifiHandoverMobileToastShowed = WifiProStateMachine.DBG;
                    if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT)) {
                        WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true ^ WifiProStateMachine.this.mIsWiFiNoInternet, false);
                        return;
                    }
                    return;
                }
                return;
            }
            WifiProStateMachine.this.logE("handleNetworkConnectivityChange;Class is not match");
        }

        private void showPortalStatusBar() {
            boolean z = false;
            if (Settings.Global.getInt(WifiProStateMachine.this.mContext.getContentResolver(), "captive_portal_notification_shown", 0) == 1) {
                WifiProStateMachine.this.logE("portal notification has been shown already, not show again.");
            } else if (WifiProStateMachine.this.mCurrentWifiConfig != null && !TextUtils.isEmpty(WifiProStateMachine.this.mCurrentWifiConfig.SSID) && !WifiProStateMachine.this.mCurrentWifiConfig.SSID.equals("<unknown ssid>")) {
                Settings.Global.putInt(WifiProStateMachine.this.mContext.getContentResolver(), "captive_portal_notification_shown", 1);
                WifiProStateMachine.this.logI("periodic portal check: showPortalStatusBar, portal network = " + StringUtilEx.safeDisplaySsid(WifiProStateMachine.this.mCurrentWifiConfig.getPrintableSsid()));
                if (WifiProStateMachine.this.mPortalNotificationId == -1) {
                    WifiProStateMachine.this.mPortalNotificationId = new SecureRandom().nextInt(100000);
                }
                WifiProStateMachine.this.mWifiProUIDisplayManager.showPortalNotificationStatusBar(WifiProStateMachine.this.mCurrentWifiConfig.SSID, WifiProStateMachine.PORTAL_STATUS_BAR_TAG, WifiProStateMachine.this.mPortalNotificationId, (Notification.Builder) null);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                String configKey = wifiProStateMachine.mCurrentWifiConfig.configKey();
                if (WifiProStateMachine.this.mCurrentWifiConfig.lastHasInternetTimestamp > 0) {
                    z = true;
                }
                wifiProStateMachine.notifyPortalStatusChanged(WifiProStateMachine.DBG, configKey, z);
            }
        }

        private void updateWifiConfigForPortalExpired() {
            if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                long mPortalValidityDuration = System.currentTimeMillis() - WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp;
                if (((WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration != 0 && WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration > mPortalValidityDuration) || WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration == 0) && mPortalValidityDuration > 0) {
                    WifiProStateMachine.this.mCurrentWifiConfig.portalValidityDuration = mPortalValidityDuration;
                }
                WifiProStateMachine.this.mCurrentWifiConfig.portalAuthTimestamp = 0;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.updateWifiConfig(wifiProStateMachine.mCurrentWifiConfig);
            }
        }

        private void handlePortalAuthCheckResultInLinkMonitorState(Message msg) {
            int wifiInternetLevel = msg.arg1;
            WifiProStateMachine.this.logI("periodic portal check, handlePortalAuthCheckResultInLinkMonitorState : wifiInternetLevel = " + wifiInternetLevel);
            if (6 == msg.arg1) {
                WifiProStateMachine.this.logI("periodic portal check: detectCounter = " + this.detectCounter);
                if (WifiProStateMachine.this.respCodeChrInfo.length() != 0) {
                    WifiProStateMachine.access$12784(WifiProStateMachine.this, "/");
                }
                WifiProStateMachine.access$12784(WifiProStateMachine.this, WifiProStateMachine.RESP_CODE_PORTAL);
                if (this.detectCounter >= 2) {
                    updateWifiConfigForPortalExpired();
                    if (WifiProStateMachine.this.mNetworkPropertyChecker != null) {
                        Settings.Global.putString(WifiProStateMachine.this.mContext.getContentResolver(), "captive_portal_server", WifiProStateMachine.this.mNetworkPropertyChecker.getCaptiveUsedServer());
                    }
                    showPortalStatusBar();
                    WifiProStateMachine.this.isPortalExpired = WifiProStateMachine.DBG;
                    this.detectCounter = 0;
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHR_ALARM_EXPIRED, WifiProStateMachine.DELAY_UPLOAD_MS);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.deferMessage(wifiProStateMachine.obtainMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, 6));
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiConnectedState);
                    return;
                }
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST, 2000);
                this.detectCounter++;
            } else if (this.detectCounter > 0) {
                if (wifiInternetLevel < 1 || wifiInternetLevel > 5) {
                    WifiProStateMachine.access$12784(WifiProStateMachine.this, "/");
                    WifiProStateMachine.access$12784(WifiProStateMachine.this, WifiProStateMachine.RESP_CODE_INTERNET_UNREACHABLE);
                } else {
                    WifiProStateMachine.access$12784(WifiProStateMachine.this, "/");
                    WifiProStateMachine.access$12784(WifiProStateMachine.this, WifiProStateMachine.RESP_CODE_INTERNET_AVAILABLE);
                }
                WifiProStateMachine.this.logI("respCode changes in consecutive checks, upload CHR");
                WifiProStateMachine.this.uploadPortalAuthExpirationStatistics(false);
                this.detectCounter = 0;
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, 600000);
            }
        }

        private void handleWifiInternetResultInLinkMonitorState(Message msg) {
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_STATUS)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_STATUS);
            }
            int wifiInternetLevel = msg.arg1;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("WiFiLinkMonitorState : wifiInternetLevel = " + wifiInternetLevel);
            if (!isNeedToIgnoreNoInternetResult(wifiInternetLevel) || !WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                if (-1 == msg.arg1 || (6 == msg.arg1 && !this.portalCheck)) {
                    if (!WifiProStateMachine.this.mIsWiFiInternetCHRFlag && msg.arg2 != 1 && !isSatisfySelfCureConditions()) {
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.logI("upload WIFI_ACCESS_INTERNET_FAILED event for TRANS_TO_NO_INTERNET,ssid:" + StringUtilEx.safeDisplaySsid(WifiProStateMachine.this.mCurrentSsid));
                        WifiProStateMachine.this.mWifiProStatisticsManager.uploadChrNetQualityInfo(909009015);
                        Bundle data = new Bundle();
                        data.putInt(WifiProStateMachine.EVENT_ID, 909002024);
                        data.putString(WifiProStateMachine.EVENT_DATA, "TRANS_TO_NO_INTERNET");
                        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 4, data);
                        WifiProStateMachine.this.mIsWiFiInternetCHRFlag = WifiProStateMachine.DBG;
                    }
                    WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                    this.currWifiPoorlevel = -1;
                    wifiInternetLevel = this.currWifiPoorlevel;
                    if (this.isBQERequestCheckWiFi) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(false);
                    }
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.mWifiTcpRxCount = wifiProStateMachine3.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                } else if (msg.arg1 != 6 || !this.portalCheck) {
                    WifiProStateMachine.this.sendNetworkCheckingStatus("huawei.conn.NETWORK_CONDITIONS_MEASURED", "extra_is_internet_ready", wifiInternetLevel);
                    this.wifiMonitorCounter = 0;
                    this.isCheckWiFiForUpdateSetting = false;
                    this.isWifiHandoverMobileToastShowed = false;
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
                    }
                    WifiProStateMachine.this.mIsWiFiNoInternet = false;
                    WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                    updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                    WifiProStateMachine.this.reSetWifiInternetState();
                    WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                } else {
                    WifiProStateMachine.this.logD("periodic portal check: portal is detected triggered by watchdog, perform portal check right now");
                    if (HwAutoConnectManager.getInstance() != null) {
                        HwAutoConnectManager.getInstance().checkPortalAuthExpiration();
                    }
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_FAST);
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_PERIODIC_PORTAL_CHECK_SLOW, 600000);
                    return;
                }
                this.isBQERequestCheckWiFi = false;
                if (msg.arg2 != 1) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, wifiInternetLevel, 0, false);
                } else {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, wifiInternetLevel, 1, false);
                }
            } else {
                WifiProStateMachine.this.logI("WLAN+ has handled NoInternet Message, don't need to handle repeatedly.");
            }
        }

        private void checkWifiNetworkStatus() {
            WifiProStateMachine.this.logI("Five seconds is up and There is detection results back");
            WifiProStateMachine.this.isVerifyWifiNoInternetTimeOut = WifiProStateMachine.DBG;
            WifiProStateMachine.this.onNetworkDetectionResult(1, -1);
        }

        private boolean isNeedToIgnoreNoInternetResult(int checkResult) {
            if (WifiProStateMachine.this.hasHandledNoInternetResult) {
                WifiProStateMachine.this.hasHandledNoInternetResult = false;
                if (checkResult == -1) {
                    return WifiProStateMachine.DBG;
                }
            }
            if (WifiProStateMachine.this.isVerifyWifiNoInternetTimeOut && checkResult == -1) {
                WifiProStateMachine.this.isVerifyWifiNoInternetTimeOut = false;
                WifiProStateMachine.this.hasHandledNoInternetResult = WifiProStateMachine.DBG;
            }
            return false;
        }

        private void handleWifiQosChangedInLinkMonitorState(Message msg) {
            if (handleMsgBySwitchOrDialogStatus(msg.arg1)) {
                WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
                return;
            }
            boolean updateUiOnly = false;
            if (msg.obj instanceof Boolean) {
                updateUiOnly = ((Boolean) msg.obj).booleanValue();
            } else {
                WifiProStateMachine.this.logE("handleWifiQosChangedInLinkMonitorState:Class is not match");
            }
            boolean z = WifiProStateMachine.DBG;
            if (!updateUiOnly || -103 == msg.arg1 || -104 == msg.arg1) {
                WifiProStateMachine.this.logI("WiFiLinkMonitorState receive wifi Qos currWifiPoorlevel = " + msg.arg1 + ", dialog = " + this.isNoInternetDialogShowing + ", updateSettings = " + this.isCheckWiFiForUpdateSetting);
                if (-103 == msg.arg1) {
                    if (!WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_STATUS)) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_STATUS, 5000);
                        this.isBQERequestCheckWiFi = WifiProStateMachine.DBG;
                        this.isRequestWifInetCheck = WifiProStateMachine.DBG;
                    }
                } else if (-104 == msg.arg1) {
                    WifiProStateMachine.this.logI("REQUEST_POOR_RSSI_INET_CHECK, no HTTP GET, wait APPs to report poor link.");
                } else {
                    if (msg.arg1 != -1) {
                        z = false;
                    }
                    this.isNotifyInvalidLinkDetection = z;
                    updateQosLevel(msg);
                }
            } else {
                this.currWifiPoorlevel = msg.arg1;
                if (msg.arg1 == 0 || msg.arg1 == 1) {
                    updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, 1);
                }
            }
        }

        private void updateQosLevel(Message msg) {
            if (!this.isCheckWiFiForUpdateSetting || WifiProStateMachine.this.mIsWiFiNoInternet) {
                this.currWifiPoorlevel = msg.arg1;
                if (msg.arg1 <= 2 && !this.isNoInternetDialogShowing) {
                    if (this.currWifiPoorlevel == -1) {
                        WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                        if (msg.arg2 != 1) {
                            WifiProStateMachine.this.mIsWiFiInternetCHRFlag = WifiProStateMachine.DBG;
                        }
                    }
                    WifiProStateMachine.this.mWifiToWifiType = 0;
                    if (this.currWifiPoorlevel == -2) {
                        WifiProStateMachine.this.refreshConnectedNetWork();
                        tryWifiHandoverPreferentially(WifiProStateMachine.this.mCurrentRssi);
                        return;
                    }
                    if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason == -1) {
                        updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, 1);
                    }
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.mWifiToWifiType = 1;
                    }
                    WifiProStateMachine.this.logW("WiFiLinkMonitorState : try wifi --> wifi --> mobile data");
                    this.isWiFiHandoverPriority = false;
                    tryWifi2Wifi();
                }
                if (this.isRequestWifInetCheck && this.currWifiPoorlevel == -1) {
                    this.isRequestWifInetCheck = false;
                    this.isNotifyInvalidLinkDetection = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.logI("Monitoring to the broken network, Maybe needs to be informed the message to networkmonitor");
                    return;
                }
                return;
            }
            WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.sendMessage(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT, wifiProStateMachine.mNetworkQosMonitor.getCurrentWiFiLevel());
        }

        private void handleOrientationChanged(Message msg) {
            if (WifiProStateMachine.this.mDelayedRssiChangedByFullScreen && !WifiProStateMachine.this.isFullscreen()) {
                WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = false;
                if (WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo()) < 3) {
                    WifiProStateMachine.this.logI("handleOrientationChanged, continue full screen skiped scan.");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord()));
                }
            }
        }

        private void handleCallStateChanged(Message msg) {
            if (WifiProStateMachine.this.mDelayedRssiChangedByCalling && msg.arg1 == 0) {
                WifiProStateMachine.this.mDelayedRssiChangedByCalling = false;
                if (WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo()) < 3) {
                    WifiProStateMachine.this.logI("handleCallStateChanged, continue scan.");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord()));
                }
            }
        }

        private void handleEmuiCspSettingChange() {
            if (WifiProStateMachine.this.mEmuiPdpSwichValue != 2) {
                this.isAllowWiFiHandoverMobile = WifiProStateMachine.DBG;
                this.isCheckWiFiForUpdateSetting = false;
                this.isSwitching = false;
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                } else {
                    WifiProStateMachine.this.setWifiMonitorEnabled(WifiProStateMachine.DBG);
                }
            }
        }

        private void handleRssiChangedInLinkMonitorState(Message msg) {
            if (msg.obj instanceof Intent) {
                Intent rssiIntent = (Intent) msg.obj;
                WifiProStateMachine.this.mCurrentRssi = rssiIntent.getIntExtra("newRssi", WifiHandover.INVALID_RSSI);
                if (WifiProStateMachine.this.isFullscreen()) {
                    WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = WifiProStateMachine.DBG;
                } else if (WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext)) {
                    WifiProStateMachine.this.mDelayedRssiChangedByCalling = WifiProStateMachine.DBG;
                } else {
                    WifiProStateMachine.this.mDelayedRssiChangedByCalling = false;
                    WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = false;
                    if (!this.isWiFiHandoverPriority && !this.isWifi2WifiProcess && !WifiProStateMachine.this.mIsWiFiNoInternet && !WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY)) {
                        int rssilevel = WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo());
                        boolean hasSwitchRecord = hasWifiSwitchRecord();
                        if (rssilevel >= 3) {
                            if (rssilevel == 4 && hasSwitchRecord) {
                                this.rssiLevel2ScanedCounter = 0;
                                this.rssiLevel0Or1ScanedCounter = 0;
                            }
                            if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY);
                                return;
                            }
                            return;
                        }
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasSwitchRecord));
                    }
                }
            } else {
                WifiProStateMachine.this.logE("handleRssiChangedInLinkMonitorState:Class is not match");
            }
        }

        private boolean hasWifiSwitchRecord() {
            WifiProStateMachine.this.refreshConnectedNetWork();
            if (WifiProStateMachine.this.mCurrentWifiConfig == null || WifiProStateMachine.this.mCurrentWifiConfig.lastTrySwitchWifiTimestamp <= 0) {
                return false;
            }
            return System.currentTimeMillis() - WifiProStateMachine.this.mCurrentWifiConfig.lastTrySwitchWifiTimestamp < WifiProStateMachine.WIFI_SWITCH_RECORD_MAX_TIME ? WifiProStateMachine.DBG : false;
        }

        private void handleReuqestScanInLinkMonitorState(Message msg) {
            int scanMaxCounter;
            int scanInterval;
            int i;
            int i2;
            if (WifiProStateMachine.this.isFullscreen()) {
                WifiProStateMachine.this.mDelayedRssiChangedByFullScreen = WifiProStateMachine.DBG;
                WifiProStateMachine.this.logE("handleReuqestScanInLinkMonitorState, don't try to swithch wifi when full screen.");
            } else if (WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext)) {
                WifiProStateMachine.this.logE("handleReuqestScanInLinkMonitorState, don't try to swithch wifi when calling.");
                WifiProStateMachine.this.mDelayedRssiChangedByCalling = WifiProStateMachine.DBG;
            } else {
                boolean hasWifiSwitchRecord = false;
                if (msg.obj instanceof Boolean) {
                    hasWifiSwitchRecord = ((Boolean) msg.obj).booleanValue();
                } else {
                    WifiProStateMachine.this.logE("handleReuqestScanInLinkMonitorState:Class is not match");
                }
                HwWifiProFeatureControl.getInstance();
                if (HwWifiProFeatureControl.isSelfCureOngoing()) {
                    if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY);
                    }
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.sendMessageDelayed(wifiProStateMachine.obtainMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord)), 10000);
                    return;
                }
                int rssilevel = WifiProCommonUtils.getCurrenSignalLevel(WifiProStateMachine.this.mWifiManager.getConnectionInfo());
                if (rssilevel < 3) {
                    if (!WifiProStateMachine.this.mIsUserManualConnectSuccess || rssilevel != 2 || this.currWifiPoorlevel <= 2) {
                        if (hasWifiSwitchRecord) {
                            scanInterval = WifiProStateMachine.QUICK_SCAN_INTERVAL[rssilevel];
                            scanMaxCounter = WifiProStateMachine.QUICK_SCAN_MAX_COUNTER[rssilevel];
                        } else {
                            scanInterval = WifiProStateMachine.NORMAL_SCAN_INTERVAL[rssilevel];
                            scanMaxCounter = WifiProStateMachine.NORMAL_SCAN_MAX_COUNTER[rssilevel];
                        }
                        if (rssilevel == 2 && (i2 = this.rssiLevel2ScanedCounter) < scanMaxCounter) {
                            this.rssiLevel2ScanedCounter = i2 + 1;
                            WifiProStateMachine.this.mWifiManager.startScan();
                            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                            wifiProStateMachine2.sendMessageDelayed(wifiProStateMachine2.obtainMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord)), (long) scanInterval);
                        } else if (rssilevel < 2 && (i = this.rssiLevel0Or1ScanedCounter) < scanMaxCounter) {
                            this.rssiLevel0Or1ScanedCounter = i + 1;
                            WifiProStateMachine.this.mWifiManager.startScan();
                            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                            wifiProStateMachine3.sendMessageDelayed(wifiProStateMachine3.obtainMessage(WifiProStateMachine.EVENT_REQUEST_SCAN_DELAY, Boolean.valueOf(hasWifiSwitchRecord)), (long) scanInterval);
                        }
                    } else {
                        WifiProStateMachine.this.logI("handleReuqestScanInLinkMonitorState, user click and signal = 2, but wifi link is good, don't trigger scan.");
                    }
                }
            }
        }

        private boolean isSatisfiedWifiHandoverCondition(Message msg) {
            if (msg == null) {
                return false;
            }
            if (!(msg.obj instanceof Boolean) || !((Boolean) msg.obj).booleanValue()) {
                Log.e(WifiProStateMachine.TAG, "isSatisfiedWifiHandoverCondition target ap is not exist");
                return false;
            } else if (WifiProStateMachine.this.isFullscreen() || WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext) || WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logW("mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet + ", isFullscreen = " + WifiProStateMachine.this.isFullscreen());
                return false;
            } else {
                if (!this.isWiFiHandoverPriority && !this.isWifi2WifiProcess) {
                    HwWifiProFeatureControl.getInstance();
                    if (!HwWifiProFeatureControl.isSelfCureOngoing()) {
                        if (!WifiProStateMachine.this.mNetworkQosMonitor.isHighDataFlowModel()) {
                            return WifiProStateMachine.DBG;
                        }
                        WifiProStateMachine.this.logw("has good rssi network, but user is in high data mode, don't handle wifi switch.");
                        return false;
                    }
                }
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logW("isWiFiHandoverPriority" + this.isWiFiHandoverPriority + "isWifi2WifiProcess" + this.isWifi2WifiProcess);
                return false;
            }
        }

        private void handleCheckResultInLinkMonitorState(Message msg) {
            if (msg != null && isSatisfiedWifiHandoverCondition(msg)) {
                WifiInfo wifiInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                if (wifiInfo == null || wifiInfo.getRssi() == -127) {
                    WifiProStateMachine.this.logW("wifiInfo RSSI is invalid");
                    return;
                }
                int curRssiLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo);
                if (curRssiLevel >= 3) {
                    WifiProStateMachine.this.logI("current ap is good, don't handle wifi switch.");
                    return;
                }
                int targetRssiLevel = msg.arg1;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("curRssiLevel = " + curRssiLevel + ", targetRssiLevel " + targetRssiLevel);
                if (targetRssiLevel - curRssiLevel >= 2) {
                    if (1 == msg.arg2) {
                        WifiProStateMachine.this.logI("handleCheckResultInLinkMonitorState, try preferred wifi handover");
                        tryWifiHandoverWithoutRssiCheck();
                        return;
                    }
                    WifiProStateMachine.this.logI("handleCheckResultInLinkMonitorState, try wifi handover");
                    this.isRssiLowOrMiddleWifi2Wifi = WifiProStateMachine.DBG;
                    tryWifiHandoverPreferentially(curRssiLevel);
                }
            }
        }

        private void handleDualbandApAvailable() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("receive EVENT_DUALBAND_5GAP_AVAILABLE isSwitching = " + this.isSwitching);
            int switchType = 1;
            if (this.isSwitching) {
                WifiProStateMachine.this.mNeedRetryMonitor = WifiProStateMachine.DBG;
                return;
            }
            if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.SSID != null && WifiProStateMachine.this.mAvailable5GAPSsid != null && WifiProStateMachine.this.mCurrentWifiConfig.SSID.equals(WifiProStateMachine.this.mAvailable5GAPSsid) && WifiProStateMachine.this.mCurrentWifiConfig.allowedKeyManagement.cardinality() <= 1 && WifiProStateMachine.this.mCurrentWifiConfig.getAuthType() == WifiProStateMachine.this.mAvailable5GAPAuthType) {
                WifiProStateMachine.this.mDuanBandHandoverType = 1;
                WifiProStateMachine.this.logI("handleDualbandApAvailable 5G and 2.4G AP have the same ssid and auth type");
            }
            WifiProStateMachine.this.logI("do dual band wifi handover");
            if (!WifiProStateMachine.this.isFullscreen() && !WifiProCommonUtils.isCalling(WifiProStateMachine.this.mContext)) {
                HwWifiProFeatureControl.getInstance();
                if (!HwWifiProFeatureControl.isSelfCureOngoing()) {
                    if (WifiProStateMachine.this.mIsSwitchForbiddenFromApp) {
                        WifiProStateMachine.this.logI("App need to keep in current AP");
                        return;
                    } else if (WifiproUtils.isCastOptWorking()) {
                        WifiProStateMachine.this.logI("keep in current AP, cast optimization is working");
                        return;
                    } else {
                        this.isSwitching = WifiProStateMachine.DBG;
                        this.isWifi2WifiProcess = WifiProStateMachine.DBG;
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.mBadBssid = wifiProStateMachine2.mCurrentBssid;
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.mBadSsid = wifiProStateMachine3.mCurrentSsid;
                        WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                        wifiProStateMachine4.logI("do dual band wifi handover, mCurrentBssid = " + StringUtilEx.safeDisplayBssid(WifiProStateMachine.this.mCurrentBssid) + ", mAvailable5GAPBssid = " + StringUtilEx.safeDisplayBssid(WifiProStateMachine.this.mAvailable5GAPBssid) + ", mDuanBandHandoverType = " + WifiProStateMachine.this.mDuanBandHandoverType);
                        WifiProStateMachine wifiProStateMachine5 = WifiProStateMachine.this;
                        wifiProStateMachine5.mNewSelect_bssid = wifiProStateMachine5.mAvailable5GAPBssid;
                        if (WifiProStateMachine.this.mDuanBandHandoverType == 1) {
                            switchType = 2;
                        }
                        WifiProStateMachine wifiProStateMachine6 = WifiProStateMachine.this;
                        wifiProStateMachine6.logI("do dual band wifi handover, switchType = " + switchType);
                        int dualbandReason = WifiProStateMachine.this.mWifiHandover.handleDualBandWifiConnect(WifiProStateMachine.this.mAvailable5GAPBssid, WifiProStateMachine.this.mAvailable5GAPSsid, WifiProStateMachine.this.mAvailable5GAPAuthType, switchType);
                        if (dualbandReason != 0) {
                            dualBandhandoverFailed(dualbandReason);
                            return;
                        }
                        if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                            WifiProStateMachine wifiProStateMachine7 = WifiProStateMachine.this;
                            wifiProStateMachine7.mChrQosLevelBeforeHandover = wifiProStateMachine7.mCurrentWifiConfig.networkQosLevel;
                        }
                        WifiProStateMachine.this.mWifiDualBandStartTime = SystemClock.elapsedRealtime();
                        return;
                    }
                }
            }
            WifiProStateMachine.this.logI("keep in current AP,now is in calling/full screen/selfcure and switch by hardhandover");
        }

        private void tryWifiHandoverPreferentially(int curRssiLevel) {
            if ((!WifiProStateMachine.this.mIsUserManualConnectSuccess || WifiProStateMachine.this.mIsWiFiProEnabled) && curRssiLevel <= 2) {
                if (curRssiLevel < 1 && !WifiProStateMachine.this.mIsScanedRssiLow) {
                    WifiProStateMachine.this.mIsScanedRssiLow = WifiProStateMachine.DBG;
                } else if (curRssiLevel >= 1 && !WifiProStateMachine.this.mIsScanedRssiMiddle) {
                    WifiProStateMachine.this.mIsScanedRssiMiddle = WifiProStateMachine.DBG;
                } else {
                    return;
                }
                this.isWiFiHandoverPriority = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mWifiHandoverStartTime = SystemClock.elapsedRealtime();
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logW("try wifi --> wifi only, current rssi = " + WifiProStateMachine.this.mCurrentRssi);
                this.mWifi2WifiThreshod = WifiProStateMachine.this.mCurrentRssi;
                tryWifi2Wifi();
            }
        }

        private void tryWifiHandoverWithoutRssiCheck() {
            if (!WifiProStateMachine.this.mIsWiFiProEnabled) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logE("tryWifiHandoverWithoutRssiCheck: mIsWiFiProEnabled = " + WifiProStateMachine.this.mIsWiFiProEnabled);
                return;
            }
            this.isWiFiHandoverPriority = WifiProStateMachine.DBG;
            WifiProStateMachine.this.logW("try wifi --> wifi for preferred network");
            this.mWifi2WifiThreshod = WifiProStateMachine.this.mCurrentRssi;
            tryWifi2Wifi();
        }

        private void tryWifi2Wifi() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.uploadChrWifiHandoverWifi(wifiProStateMachine.mIsWiFiNoInternet ^ WifiProStateMachine.DBG, this.isWiFiHandoverPriority);
            if ((WifiProStateMachine.this.mIsUserManualConnectSuccess && !WifiProStateMachine.this.mIsWiFiProEnabled) || WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                WifiProStateMachine.this.logE("User manual connect wifi, and wifi+ disabled. don't try wifi switch!");
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.logE("WiFi2WiFi failed because cannot switch, try to trigger selfcure");
                    pendingMsgBySelfCureEngine(-103);
                }
                if (WifiProStateMachine.this.mHandoverFailReason != 10) {
                    WifiProStateMachine.this.mHandoverFailReason = 10;
                    WifiProStateMachine.this.uploadWifiSwitchFailTypeStatistics(10);
                }
                WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
            } else if (HwWifiProFeatureControl.sWifiProToWifiCtrl) {
                this.isSwitching = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mWifiHandover.updateWiFiInternetAccess(WifiProStateMachine.this.mIsWiFiNoInternet ^ WifiProStateMachine.DBG);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_TRY_WIFI_ROVE_OUT);
            }
        }

        private boolean isSatisfiedWifiToCellCondition() {
            boolean isMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(WifiProStateMachine.this.mContext);
            WifiProStateMachine.this.mStringBuffer.delete(0, WifiProStateMachine.this.mStringBuffer.length());
            StringBuffer stringBuffer = WifiProStateMachine.this.mStringBuffer;
            stringBuffer.append("isWifi2WifiProcess = ");
            stringBuffer.append(this.isWifi2WifiProcess);
            stringBuffer.append(", isAllowWifi2Mobile = ");
            stringBuffer.append(WifiProStateMachine.this.isAllowWifi2Mobile());
            stringBuffer.append(", isAllowWiFiHandoverMobile = ");
            stringBuffer.append(this.isAllowWiFiHandoverMobile);
            stringBuffer.append(", mIsWiFiNoInternet = ");
            stringBuffer.append(WifiProStateMachine.this.mIsWiFiNoInternet);
            stringBuffer.append(", isStrongRssi = ");
            stringBuffer.append(isStrongRssi());
            stringBuffer.append(", isOpenAndPortal = ");
            stringBuffer.append(WifiProCommonUtils.isOpenAndPortal(WifiProStateMachine.this.mCurrentWifiConfig));
            stringBuffer.append(", isDomesticBetaUser = ");
            stringBuffer.append(WifiProCommonUtils.isDomesticBetaUser());
            stringBuffer.append(", isMobileHotspot = ");
            stringBuffer.append(isMobileHotspot);
            stringBuffer.append(", welinkInstalled = ");
            stringBuffer.append(WifiproUtils.isInstallHuaweiCustomizedApp(WifiProStateMachine.this.mContext));
            stringBuffer.append(", isSmartDataSavingSwitchOff = ");
            stringBuffer.append(WifiProStateMachine.this.isSmartDataSavingSwitchOff());
            stringBuffer.append(", mWifiProSwitchState = ");
            stringBuffer.append(WifiProStateMachine.this.mWifiProSwitchState);
            stringBuffer.append(", isOversea = ");
            stringBuffer.append(WifiProCommonUtils.isOversea());
            stringBuffer.append(", mIsWifiAdvancedChipUser = ");
            stringBuffer.append(WifiProStateMachine.this.mIsWifiAdvancedChipUser);
            stringBuffer.append(", mEmuiPdpSwichValue = ");
            stringBuffer.append(WifiProStateMachine.this.mEmuiPdpSwichValue);
            stringBuffer.append(", mIsWifi2CellInStrongSignalEnabled = ");
            stringBuffer.append(WifiProStateMachine.this.mIsWifi2CellInStrongSignalEnabled);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(wifiProStateMachine.mStringBuffer.toString());
            if (this.isWifi2WifiProcess || !WifiProStateMachine.this.isAllowWifi2Mobile() || !this.isAllowWiFiHandoverMobile || !WifiProStateMachine.this.mPowerManager.isScreenOn() || isCallingInCs(WifiProStateMachine.this.mContext) || WifiProStateMachine.this.mIsWiFiNoInternet || WifiProCommonUtils.isOversea() || WifiProStateMachine.this.mEmuiPdpSwichValue != 1) {
                WifiProStateMachine.this.logI("can not hand over to mobile, keep monitor qos");
                return false;
            } else if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                WifiProStateMachine.this.logI("can not hand over to mobile, wait cell qoe better");
                return false;
            } else if (!isStrongRssi()) {
                return WifiProStateMachine.DBG;
            } else {
                boolean isHandoverInStrongRssi = WifiProStateMachine.this.mIsWifi2CellInStrongSignalEnabled && (WifiProStateMachine.this.mWifiProSwitchState || WifiProCommonUtils.isDomesticBetaUser());
                if ((WifiProStateMachine.this.mIsWifiSwitchRobotAlgorithmEnabled || !WifiProCommonUtils.isOpenAndPortal(WifiProStateMachine.this.mCurrentWifiConfig)) && (!WifiProStateMachine.this.mIsWifiSwitchRobotAlgorithmEnabled || isMobileHotspot || ((!isHandoverInStrongRssi && !WifiproUtils.isInstallHuaweiCustomizedApp(WifiProStateMachine.this.mContext)) || !WifiProStateMachine.this.isSmartDataSavingSwitchOff() || !WifiProStateMachine.this.mIsWifiAdvancedChipUser))) {
                    WifiProStateMachine.this.logI("can not hand over to mobile in strong rssi");
                    return false;
                }
                WifiProStateMachine.this.logI("can hand over to mobile in strong rssi");
                return WifiProStateMachine.DBG;
            }
        }

        private void handleWifi2Cell() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.updateChrToCellSucc(wifiProStateMachine.mIsWiFiNoInternet ^ WifiProStateMachine.DBG);
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logI("mobile is better than wifi,and ScreenOn, try wifi --> mobile,show Dialog mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + ", mIsWiFiNoInternet =" + WifiProStateMachine.this.mIsWiFiNoInternet);
            if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
            }
            if (this.isWifi2MobileUIShowing) {
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.logE("isWifi2MobileUIShowing = true, not dispaly " + this.isWifi2MobileUIShowing);
                unRegisterCellCardImmediately();
                WifiProStateMachine.this.handleWifi2CellFailChr(29);
                return;
            }
            this.isWifi2MobileUIShowing = WifiProStateMachine.DBG;
            unRegisterCellCardLatency();
            if (WifiProStateMachine.this.isPdpAvailable()) {
                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                wifiProStateMachine4.logI("mobile is cmcc and wifi pdp, mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + " ,mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue + " last rssi signal=" + WifiProStateMachine.this.mCurrentRssi);
                int emuiPdpSwichType = WifiProStateMachine.this.mEmuiPdpSwichValue;
                if (WifiProStateMachine.this.isDialogUpWhenConnected) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                    return;
                }
                if (emuiPdpSwichType == 0) {
                    emuiPdpSwichType = 1;
                }
                if (emuiPdpSwichType == 1) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                } else if (emuiPdpSwichType == 2) {
                    WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
                }
            } else {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
            }
        }

        private void tryWifi2Mobile(int mobile_level) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("Receive mobile QOS  mobile_level = " + mobile_level + ", isSwitching =" + this.isSwitching);
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.updateChrToCell(wifiProStateMachine2.mIsWiFiNoInternet ^ WifiProStateMachine.DBG);
            if (!isSatisfiedWifiToCellCondition()) {
                this.isSwitching = false;
                this.mIsNeedIgnoreQoeUpdate = false;
                if (!WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mHandoverFailReason != 9) {
                    WifiProStateMachine.this.mHandoverFailReason = 9;
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.uploadWifiSwitchFailTypeStatistics(wifiProStateMachine3.mHandoverFailReason);
                }
                if (isCallingInCs(WifiProStateMachine.this.mContext) && WifiProStateMachine.this.mHandoverFailReason != 6) {
                    WifiProStateMachine.this.mHandoverFailReason = 6;
                    WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                    wifiProStateMachine4.uploadWifiSwitchFailTypeStatistics(wifiProStateMachine4.mHandoverFailReason);
                }
                if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR)) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                }
                unRegisterCellCardImmediately();
                WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
            } else if (!WifiProStateMachine.this.isWiFiPoorer()) {
                WifiProStateMachine.this.logI("mobile is poorer,continue monitor");
                this.isSwitching = false;
                this.mIsNeedIgnoreQoeUpdate = false;
                if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                    this.isToastDisplayed = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                }
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                unRegisterCellCardImmediately();
                WifiProStateMachine.this.handleWifi2CellFailChr(2);
            } else if (!this.isSwitching || !WifiProStateMachine.this.isAllowWifi2Mobile()) {
                WifiProStateMachine.this.logW("no handover,DELAY Transit to Monitor");
                this.isSwitching = false;
                this.mIsNeedIgnoreQoeUpdate = false;
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                unRegisterCellCardImmediately();
                WifiProStateMachine.this.handleWifi2CellFailChr(2);
            } else {
                handleWifi2Cell();
            }
        }

        private void wifi2WifiFailed() {
            if (!(WifiProStateMachine.this.mHandoverFailReason == 8 || WifiProStateMachine.this.mHandoverFailReason == 7)) {
                WifiProStateMachine.this.mHandoverFailReason = 8;
                WifiProStateMachine.this.uploadWifiSwitchFailTypeStatistics(8);
            }
            if (WifiProStateMachine.this.mNewSelect_bssid != null && !WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadBssid)) {
                WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
            }
            WifiProStateMachine.this.logI("wifi to Wifi Failed Finally!");
            if (this.isNotifyInvalidLinkDetection && WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.updateChrToCell(false);
                this.isNotifyInvalidLinkDetection = false;
                WifiProStateMachine.this.logI("We detection no internet, And wifi2WifiFailed, So we need notify msg to networkmonitor");
                WifiProStateMachine.this.sendInvalidLinkDetected();
            }
            this.isWifi2WifiProcess = false;
            this.isSwitching = false;
            if (this.isRssiLowOrMiddleWifi2Wifi) {
                this.isRssiLowOrMiddleWifi2Wifi = false;
                WifiProStateMachine.this.resetScanedRssiVariable();
            }
            if (WifiProCommonUtils.isWifiConnectedOrConnecting(WifiProStateMachine.this.mWifiManager)) {
                handlewifi2WifiFailedInConnectedOrConnecting();
                return;
            }
            WifiProStateMachine.this.logI("wifi handover over Failed and system auto conning ap");
            if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("try to connect : " + StringUtilEx.safeDisplaySsid(WifiProStateMachine.this.mBadSsid));
                WifiProStateMachine.this.uploadChrWifiHandoverTypeStatistics(WifiProStateMachine.WIFI_HANDOVER_TYPES[8], WifiProStateMachine.HANDOVER_CNT);
                WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
            }
            WifiProStateMachine.this.handleWifi2CellFailChr(1);
        }

        private void handlewifi2WifiFailedInConnectedOrConnecting() {
            if (WifiProStateMachine.this.mNeedRetryMonitor) {
                WifiProStateMachine.this.logI("need retry dualband handover monitor");
                WifiProStateMachine.this.retryDualBandAPMonitor();
                WifiProStateMachine.this.mNeedRetryMonitor = false;
            }
            if (this.isWiFiHandoverPriority) {
                WifiProStateMachine.this.logI("wifi handover wifi failed,continue monitor wifi Qos");
                WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
                if (!WifiProStateMachine.this.mIsUserManualConnectSuccess || this.currWifiPoorlevel != -2) {
                    this.isWiFiHandoverPriority = false;
                    if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
                        WifiProStateMachine.this.logI("can not handover to mobile ,delay 2 minutes go to Monitor");
                        return;
                    }
                    return;
                }
                return;
            }
            WifiProStateMachine.this.mStringBuffer.delete(0, WifiProStateMachine.this.mStringBuffer.length());
            StringBuffer stringBuffer = WifiProStateMachine.this.mStringBuffer;
            stringBuffer.append("wifi --> wifi is Failure, but wifi is connected, isMobileDataConnected() = ");
            stringBuffer.append(WifiProStateMachine.this.isMobileDataConnected());
            stringBuffer.append(", isAllowWiFiHandoverMobile =  ");
            stringBuffer.append(this.isAllowWiFiHandoverMobile);
            stringBuffer.append(" , mEmuiPdpSwichValue = ");
            stringBuffer.append(WifiProStateMachine.this.mEmuiPdpSwichValue);
            stringBuffer.append(", mPowerManager.isScreenOn =");
            stringBuffer.append(WifiProStateMachine.this.mPowerManager.isScreenOn());
            stringBuffer.append(", currWifiPoorlevel = ");
            stringBuffer.append(this.currWifiPoorlevel);
            stringBuffer.append(", mIsWiFiNoInternet = ");
            stringBuffer.append(WifiProStateMachine.this.mIsWiFiNoInternet);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(wifiProStateMachine.mStringBuffer.toString());
            handleWifi2WifiFalied();
        }

        private void notifyChrPreventWifiSwitchMobile() {
            if (WifiProStateMachine.this.isMobileDataConnected() && WifiProStateMachine.this.mPowerManager != null && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mEmuiPdpSwichValue == 2 && !this.isCancelCHRTypeReport) {
                this.isCancelCHRTypeReport = WifiProStateMachine.DBG;
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.logI("call increaseNotInetSettingCancelCount.");
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                    return;
                }
                WifiProStateMachine.this.logI("call increaseBQE_BadSettingCancelCount.");
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseBqeBadSettingCancelCount();
            }
        }

        private void tryToGetCellChannelQoe() {
            if (!tryToActivateMasterCard() && !tryToActivateSlaveCard()) {
                WifiProStateMachine.this.logI("activate master card and salve card both fail");
                this.isSwitching = false;
                if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER, WifiProStateMachine.DELAY_UPLOAD_MS);
                    WifiProStateMachine.this.logI("can not handover to mobile ,delay 2 minutes go to Monitor");
                }
                WifiProStateMachine.this.handleWifi2CellFailChr(16);
            }
        }

        private boolean isInBlacklistAndPoorSignal() {
            if (!this.mIsAppInBlacklist || WifiProStateMachine.this.mNotifyWifiLinkPoorReason != 107) {
                WifiProStateMachine.this.logI("app is not in blacklist or poor signal");
                return false;
            }
            WifiProStateMachine.this.logI("app is in blacklist and poor signal.");
            return WifiProStateMachine.DBG;
        }

        private void handleWifi2WifiFalied() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.uploadChrWifiHandoverCell(wifiProStateMachine.mIsWiFiNoInternet ^ WifiProStateMachine.DBG);
            if (WifiProStateMachine.this.mIsWiFiNoInternet || !WifiProStateMachine.this.isAllowWifi2Mobile() || WifiProStateMachine.this.mNotifyWifiLinkPoorReason == -1 || !isSatisfiedWifiToCellCondition() || isInBlacklistAndPoorSignal()) {
                WifiProStateMachine.this.logI("wifi --> wifi is Failure,and can not handover to mobile ,delay 30s go to Monitor");
                WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
                WifiProStateMachine.this.mNotifyWifiLinkPoorReason = -1;
                notifyChrPreventWifiSwitchMobile();
                if (!WifiProStateMachine.this.isMobileDataConnected() && WifiProStateMachine.this.mHandoverFailReason != 12) {
                    WifiProStateMachine.this.mHandoverFailReason = 12;
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.uploadWifiSwitchFailTypeStatistics(wifiProStateMachine2.mHandoverFailReason);
                }
                if (!WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mHandoverFailReason != 9) {
                    WifiProStateMachine.this.mHandoverFailReason = 9;
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.uploadWifiSwitchFailTypeStatistics(wifiProStateMachine3.mHandoverFailReason);
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                    this.isToastDisplayed = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                }
                WifiProStateMachine.this.setWifiMonitorEnabled(false);
                if (!WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR)) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                }
                if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                    WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                    wifiProStateMachine4.mWifiTcpRxCount = wifiProStateMachine4.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, 5000);
                    return;
                }
                return;
            }
            WifiProStateMachine.this.logI("try to wifi --> mobile,Query mobile Qos");
            this.isSwitching = WifiProStateMachine.DBG;
            tryToGetCellChannelQoe();
        }

        private void dualBandhandoverFailed(int reason) {
            if (!(10 == reason || 11 == reason)) {
                if (WifiProStateMachine.this.mNewSelect_bssid != null && !WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadBssid)) {
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPBssid);
                    WifiProStateMachine.this.mHwDualBandBlackListMgr.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPBssid, false);
                }
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("dualBandhandoverFailed  mAvailable5GAPBssid = " + StringUtilEx.safeDisplayBssid(WifiProStateMachine.this.mAvailable5GAPBssid));
                if (!(WifiProStateMachine.this.mAvailable5GAPBssid == null || WifiProStateMachine.this.mAvailable5GAPSsid == null)) {
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPBssid);
                    WifiProStateMachine.this.mHwDualBandBlackListMgr.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPBssid, false);
                }
            }
            this.isWifi2WifiProcess = false;
            WifiProStateMachine.this.uploadWifiDualBandFailReason(reason);
            this.isSwitching = false;
            if (!WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logI("wifi dual band handover over Failed and system auto connecting ap");
                WifiProStateMachine.this.uploadChrWifiHandoverTypeStatistics(WifiProStateMachine.WIFI_HANDOVER_TYPES[8], WifiProStateMachine.HANDOVER_CNT);
                WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
            }
        }

        private void updateWifiQosLevel(boolean isWiFiNoInternet, int qosLevel) {
            WifiProStateMachine.this.refreshConnectedNetWork();
            WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(WifiProStateMachine.this.mCurrWifiInfo, 1);
            if (!WifiProStateMachine.this.mPowerManager.isScreenOn() && isWiFiNoInternet && this.mLastUpdatedQosLevel == 2) {
                return;
            }
            if (WifiProStateMachine.this.mPowerManager.isScreenOn() || this.mLastUpdatedQosLevel == 0 || isWiFiNoInternet || WifiProStateMachine.this.mCurrentWifiConfig == null || WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("updateWifiQosLevel, mCurrentSsid: " + StringUtilEx.safeDisplaySsid(WifiProStateMachine.this.mCurrentSsid) + " ,isWiFiNoInternet: " + isWiFiNoInternet + ", qosLevel: " + qosLevel);
                if (isWiFiNoInternet) {
                    this.mLastUpdatedQosLevel = 2;
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 2);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 2, WifiProStateMachine.this.mCurrentSsid);
                    return;
                }
                this.mLastUpdatedQosLevel = qosLevel;
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, qosLevel);
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, qosLevel, 0);
                return;
            }
            this.mLastUpdatedQosLevel = qosLevel;
        }

        private boolean handleMsgBySwitchOrDialogStatus(int level) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("isSwitching = " + this.isSwitching + ", isDialogDisplayed = " + this.isDialogDisplayed + ", isAllowWiFiHandoverMobile = " + this.isAllowWiFiHandoverMobile + ", level = " + level);
            if (this.isSwitching && this.isDialogDisplayed) {
                if (level > 2 && level != 6) {
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logI("Dialog is  Displayed, Qos is" + level + ", Cancel dialog.");
                    WifiProStateMachine.this.updateWifiInternetStateChange(level);
                    WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                    WifiProStateMachine.this.mIsWiFiNoInternet = false;
                    WifiProStateMachine.this.mIsWiFiInternetCHRFlag = false;
                    wiFiLinkMonitorStateInit(false);
                }
                return WifiProStateMachine.DBG;
            } else if (this.isSwitching || !this.isAllowWiFiHandoverMobile) {
                return WifiProStateMachine.DBG;
            } else {
                return false;
            }
        }

        private void handleDualbandHandoverResult(Message msg) {
            WifiProStateMachine.this.logI("receive dual band wifi handover resust");
            if (this.isWifi2WifiProcess && msg.obj != null && (msg.obj instanceof Boolean)) {
                if (((Boolean) msg.obj).booleanValue()) {
                    if (WifiProStateMachine.this.uploadManager != null) {
                        Bundle dualbandEvent = new Bundle();
                        dualbandEvent.putInt("index", 2);
                        WifiProStateMachine.this.uploadManager.addChrBundleStat("wifiSwitchCntEvent", "wifiSwitchSuccCnt", dualbandEvent);
                    }
                    WifiProChrUploadManager.uploadDisconnectedEvent(WifiProStateMachine.EVENT_DUALBAND_SWITCH_FINISHED);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("dual band wifi handover is succeed, bssid = " + StringUtilEx.safeDisplayBssid(WifiProStateMachine.this.mNewSelect_bssid) + ", mBadBssid = " + StringUtilEx.safeDisplayBssid(WifiProStateMachine.this.mBadBssid));
                    this.isSwitching = false;
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mBadBssid);
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.mDualBandConnectApBssid = wifiProStateMachine2.mNewSelect_bssid;
                    WifiProStateMachine.this.mDualBandConnectTime = System.currentTimeMillis();
                    WifiProStateMachine.this.mWifiHandoverSucceedTimestamp = SystemClock.elapsedRealtime();
                    WifiProStateMachine.this.mChrDualbandConnectedStartTime = SystemClock.elapsedRealtime();
                    WifiProStateMachine.this.uploadWifiDualBandHandoverDura();
                    if (HwDualBandRelationManager.isDualBandAP(WifiProStateMachine.this.mBadBssid, WifiProStateMachine.this.mAvailable5GAPBssid)) {
                        WifiProStateMachine.this.uploadChrDualBandSameApCount();
                    }
                    if (WifiProStateMachine.this.mDuanBandHandoverType == 1) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, false);
                    } else {
                        WifiProStateMachine.this.resetWifiEvaluteQosLevel();
                    }
                    this.isWifi2WifiProcess = false;
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiConnectedState);
                } else {
                    WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                    wifiProStateMachine4.logI("dual band wifi handover is  failure, error reason = " + msg.arg1);
                    dualBandhandoverFailed(msg.arg1);
                }
                uploadDualbandSwitchInfo(((Boolean) msg.obj).booleanValue());
            }
        }

        private void uploadDualbandSwitchInfo(boolean success) {
            Bundle data = new Bundle();
            int i = 0;
            data.putInt("Roam5GSuccCnt", (WifiProStateMachine.this.mDuanBandHandoverType != 1 || !success) ? 0 : 1);
            data.putInt("Roam5GFailedCnt", (WifiProStateMachine.this.mDuanBandHandoverType != 1 || success) ? 0 : 1);
            data.putInt("NoRoam5GSuccCnt", (WifiProStateMachine.this.mDuanBandHandoverType != 0 || !success) ? 0 : 1);
            if (WifiProStateMachine.this.mDuanBandHandoverType == 0 && !success) {
                i = 1;
            }
            data.putInt("NoRoam5GFailedCnt", i);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt(WifiProStateMachine.EVENT_ID, 909009065);
            dftEventData.putBundle(WifiProStateMachine.EVENT_DATA, data);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }

        private boolean pendingMsgBySelfCureEngine(int level) {
            WifiProStateMachine.this.logI("pendingMsgBySelfCureEngine, level = " + level + " isSwitching" + this.isSwitching + " internetFailureDetectedCnt " + this.internetFailureDetectedCnt);
            if (level == -103 && !this.isSwitching) {
                HwWifiProFeatureControl.getInstance();
                if (HwWifiProFeatureControl.isSelfCureOngoing() || !WifiProStateMachine.this.isWifiConnected()) {
                    WifiProStateMachine.this.logI("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", but ignored because of self curing or supplicant not completed.");
                    return WifiProStateMachine.DBG;
                } else if (isSatisfySelfCureConditions()) {
                    this.internetFailureDetectedCnt++;
                    HwWifiProFeatureControl.getInstance();
                    HwWifiProFeatureControl.notifyInternetFailureDetected(false);
                    WifiProStateMachine.this.logI("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", request to do selfcure.");
                    return WifiProStateMachine.DBG;
                }
            }
            if (level == 0 && !this.isSwitching) {
                HwWifiProFeatureControl.getInstance();
                if (HwWifiProFeatureControl.isSelfCureOngoing()) {
                    return WifiProStateMachine.DBG;
                }
            }
            return false;
        }

        private boolean isSatisfySelfCureConditions() {
            if (this.internetFailureDetectedCnt != 0 || HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(WifiProStateMachine.this.mContext) || WifiProStateMachine.this.mCurrentRssi < -70) {
                return false;
            }
            return WifiProStateMachine.DBG;
        }

        private int getCallingSlot(Context context) {
            if (context == null) {
                Log.e(WifiProStateMachine.TAG, "getCallingSlot : context is null");
                return -1;
            }
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            int phoneCount = telephonyManager.getPhoneCount();
            for (int slotId = 0; slotId < phoneCount; slotId++) {
                int simState = telephonyManager.getSimState(slotId);
                int callState = telephonyManager.getCallStateForSlot(slotId);
                if (simState != 1 && (callState == 2 || callState == 1)) {
                    return slotId;
                }
            }
            return -1;
        }

        private boolean isCallingInCs(Context context) {
            if (!WifiProCommonUtils.isCalling(context)) {
                return false;
            }
            int slotId = getCallingSlot(context);
            int validationLength = WifiProStateMachine.this.imsRegisteredState.length;
            if (slotId <= -1 || slotId >= validationLength || WifiProStateMachine.this.imsRegisteredState[slotId]) {
                return false;
            }
            return WifiProStateMachine.DBG;
        }

        private void handleWiFiRoveOut() {
            if (this.isDisableWifiAutoSwitch || isCallingInCs(WifiProStateMachine.this.mContext)) {
                Log.w(WifiProStateMachine.TAG, "isDisableWifiAutoSwitch = " + this.isDisableWifiAutoSwitch + "isCallingInCs = " + isCallingInCs(WifiProStateMachine.this.mContext));
                this.isSwitching = false;
                if (WifiProStateMachine.this.mHandoverFailReason != 6) {
                    WifiProStateMachine.this.mHandoverFailReason = 6;
                    WifiProStateMachine.this.uploadWifiSwitchFailTypeStatistics(6);
                }
                if (this.isRssiLowOrMiddleWifi2Wifi) {
                    this.isRssiLowOrMiddleWifi2Wifi = false;
                    WifiProStateMachine.this.resetScanedRssiVariable();
                }
                WifiProStateMachine.this.handleWifi2CellFailChr(getWifi2CellFailReasonLinkMonitorState());
                return;
            }
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("EVENT_TRY_WIFI_ROVE_OUT, allow wifi to mobile " + (this.isWiFiHandoverPriority ^ WifiProStateMachine.DBG));
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.mBadBssid = wifiProStateMachine2.mCurrentBssid;
            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
            wifiProStateMachine3.mBadSsid = wifiProStateMachine3.mCurrentSsid;
            this.isWifi2WifiProcess = WifiProStateMachine.DBG;
            int threshodRssi = WifiProStateMachine.THRESHOD_RSSI;
            if (this.isWiFiHandoverPriority) {
                threshodRssi = this.mWifi2WifiThreshod + 10;
            }
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mWifiSwitchReason = 1;
            } else if (this.isWiFiHandoverPriority) {
                WifiProStateMachine.this.mWifiSwitchReason = 2;
            } else {
                WifiProStateMachine.this.logI("handleWiFiRoveOut do nothing");
            }
            if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), threshodRssi, 0, WifiProStateMachine.this.mWifiSwitchReason)) {
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.logI("WiFi2WiFi failed before Scan, try to trigger selfcure");
                    pendingMsgBySelfCureEngine(-103);
                }
                wifi2WifiFailed();
            }
        }

        private int getWifi2CellFailReasonLinkMonitorState() {
            int failReason = WifiProStateMachine.this.getWifi2CellFailReason();
            if (failReason != 199) {
                return failReason;
            }
            if (!WifiProStateMachine.this.mIsWiFiProEnabled) {
                return 10;
            }
            if (this.isSwitching || this.isWifi2WifiProcess) {
                return 23;
            }
            if (WifiProStateMachine.this.mIsUserManualConnectSuccess && !WifiProStateMachine.this.mIsWiFiProEnabled) {
                return 10;
            }
            if (this.isDisableWifiAutoSwitch || !this.isAllowWiFiHandoverMobile) {
                return 9;
            }
            if (isCallingInCs(WifiProStateMachine.this.mContext)) {
                return 11;
            }
            if (this.isWiFiHandoverPriority) {
                return 24;
            }
            if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_WAIT_CELL_QOE_BETTER)) {
                return 27;
            }
            if (WifiProStateMachine.this.mHandoverFailReason == 5) {
                return 20;
            }
            if (WifiProStateMachine.this.mHandoverFailReason == 11) {
                return 19;
            }
            if (WifiProStateMachine.this.mEmuiPdpSwichValue == 2) {
                return 31;
            }
            return WifiProChr.WIFI_2_CELL_FAIL_REASON_MAX_OTHERS;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiEvaluteChange() {
        int accessType;
        if (isAllowWiFiAutoEvaluate()) {
            removeMessages(EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
            if (this.mIsWiFiNoInternet) {
                accessType = 4;
            } else {
                accessType = 2;
            }
            if (this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, accessType)) {
                logI("mCurrentSsid   = " + StringUtilEx.safeDisplaySsid(this.mCurrentSsid) + ", updateScoreInfoType  " + accessType);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, accessType, this.mCurrentSsid);
            }
            if (accessType == 4) {
                sendMessage(EVENT_WIFI_EVALUTE_TCPRTT_RESULT, this.mNetworkQosMonitor.getCurrentWiFiLevel());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePeriodPortalCheck() {
        logI("receive : EVENT_PERIODIC_PORTAL_CHECK_SLOW");
        this.isPeriodicDet = DBG;
        if (HwAutoConnectManager.getInstance() != null) {
            HwAutoConnectManager.getInstance().checkPortalAuthExpiration();
        }
        this.detectionNumSlow++;
        sendMessageDelayed(EVENT_PERIODIC_PORTAL_CHECK_SLOW, 600000);
    }

    /* access modifiers changed from: package-private */
    public class WiFiProVerfyingLinkState extends State {
        private int internetFailureDetectedCount;
        private volatile boolean isRecoveryWifi;
        private boolean isWifiGoodIntervalTimerOut;
        private volatile boolean isWifiHandoverWifi;
        private boolean isWifiRecoveryTimerOut;
        private boolean isWifiScanScreenOff;
        private int mCellQoeBadCnt = 0;
        private long mChrHandoverBackStartTime = 0;
        private int mChrHandoverToMobileReason = 0;
        private int mFailedDetectedCount;
        private boolean mIsChrRestoreWifi;
        private boolean mIsChrUserHandoverBack;
        private boolean mIsNeedCheckWifiQoe = false;
        private int mLastLinkPoorRssi;
        private HashMap<Integer, String> mTopAppWhiteList;
        private int mWaitWifiQoeGoodCnt = 0;
        private int wifiInternetLevel;
        private int wifiNoInternetCounter;
        private int wifiQosLevel;
        private int wifiScanCounter;

        WiFiProVerfyingLinkState() {
        }

        private void startScanAndMonitor(long time) {
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, WifiProStateMachine.DELAY_UPLOAD_MS);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(WifiProStateMachine.DBG);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(WifiProStateMachine.DBG);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mCurrentWifiLevel = -1;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("WiFiProVerfyingLinkState, wifi is No Internet,delay check time = " + time);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, time);
                return;
            }
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(2, WifiProStateMachine.DBG);
        }

        private void cancelScanAndMonitor() {
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(false);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(false);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(2, false);
        }

        private void restoreWifiConnect() {
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_RECHECK_SMART_CARD_STATE)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RECHECK_SMART_CARD_STATE);
            }
            cancelScanAndMonitor();
            WifiProStateMachine.this.logI("restoreWifiConnect, mWsmChannel send GOOD Link Detected");
            WifiProStateMachine.this.mWsmChannel.sendMessage(131874);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.notifyManualConnectAP(wifiProStateMachine.mIsUserManualConnectSuccess, WifiProStateMachine.this.mIsUserHandoverWiFi);
            this.mIsChrRestoreWifi = WifiProStateMachine.DBG;
            WifiProStateMachine.this.uploadChrWifiHandoverTypeStatistics(WifiProStateMachine.WIFI_HANDOVER_TYPES[7], WifiProStateMachine.HANDOVER_CNT);
        }

        public void enter() {
            WifiProStateMachine.this.logI("WiFiProVerfyingLinkState is Enter");
            WifiProStateMachine.this.mWifiSwitchReason = 0;
            WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
            this.mWaitWifiQoeGoodCnt = 0;
            this.mIsNeedCheckWifiQoe = false;
            this.isRecoveryWifi = false;
            this.mIsChrRestoreWifi = false;
            this.isWifiHandoverWifi = false;
            this.isWifiRecoveryTimerOut = false;
            this.isWifiGoodIntervalTimerOut = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mIsUserManualConnectSuccess = false;
            this.wifiNoInternetCounter = 0;
            this.internetFailureDetectedCount = 0;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            this.wifiScanCounter = 0;
            this.isWifiScanScreenOff = false;
            this.mLastLinkPoorRssi = -127;
            this.mIsChrUserHandoverBack = false;
            this.mChrHandoverToMobileReason = WifiProStateMachine.this.mNotifyWifiLinkPoorReason;
            this.mChrHandoverBackStartTime = SystemClock.elapsedRealtime();
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.mChrQosLevelBeforeHandover = wifiProStateMachine.mCurrentWifiConfig.networkQosLevel;
            if (WifiProStateMachine.this.mCurrentVerfyCounter > 2) {
                WifiProStateMachine.this.mCurrentVerfyCounter = 2;
            }
            long delayTime = ((long) Math.pow(2.0d, (double) WifiProStateMachine.this.mCurrentVerfyCounter)) * 60 * 1000;
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logI("WiFiProVerfyingLinkState : CurrentWifiLevel = " + WifiProStateMachine.this.mCurrentWifiLevel + ", CurrentVerfyCounter = " + WifiProStateMachine.this.mCurrentVerfyCounter + ", delayTime = " + delayTime);
            WifiProStateMachine.access$19408(WifiProStateMachine.this);
            startScanAndMonitor(delayTime);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT, delayTime);
            if (WifiProStateMachine.this.mCurrentVerfyCounter == 2) {
                WifiProStateMachine.this.logW("network has handover 3 times,maybe ping-pong");
                WifiProStateMachine.this.logI("UNEXPECT_SWITCH_EVENT: pingPong: enter:");
                WifiProStateMachine.this.uploadManager.addChrSsidCntStat("unExpectSwitchEvent", "pingPong");
                WifiProStateMachine.this.mWifiProStatisticsManager.increasePingPongCount();
            }
            HwWifiConnectivityMonitor.getInstance().notifyVerifyingLinkState(WifiProStateMachine.DBG);
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(1);
            if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.logI("BQE bad rove out started.");
                WifiProStateMachine.this.mChrRoveOutStartTime = System.currentTimeMillis();
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.mRoSsid = wifiProStateMachine3.mCurrentSsid;
                WifiProStateMachine.this.mLoseInetRoveOut = false;
            } else {
                WifiProStateMachine.this.mLoseInetRoveOut = WifiProStateMachine.DBG;
            }
            WifiProStateMachine.this.mRoveOutStarted = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mIsRoveOutToDisconn = false;
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_LAA_STATUS_CHANGED, 3000);
            this.mFailedDetectedCount = 0;
            HashMap<Integer, String> hashMap = this.mTopAppWhiteList;
            if (hashMap == null || hashMap.isEmpty()) {
                this.mTopAppWhiteList = WifiProCommonUtils.getAppInWhitelist();
            }
            this.mLastLinkPoorRssi = WifiProCommonUtils.getCurrentRssi(WifiProStateMachine.this.mWifiManager);
            WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
            wifiProStateMachine4.logI("WiFiProVerfyingLinkState mNotifyWifiLinkPoorReason = " + WifiProStateMachine.this.mNotifyWifiLinkPoorReason + ", mLastLinkPoorRssi = " + this.mLastLinkPoorRssi);
            if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason == 10) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 15000);
            } else if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason == 11) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, WifiProStateMachine.DELAY_UPLOAD_MS);
            } else if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason == 12) {
                if (isElevatorScene()) {
                    WifiProStateMachine.this.logI("sendmassage wifi check 30s delay");
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 30000);
                } else {
                    WifiProStateMachine.this.logI("sendmassage wifi check 1s delay");
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 1000);
                }
            } else if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason != 107) {
                WifiProStateMachine.this.logI("invalid reason code");
            } else if (WifiProCommonUtils.isHiSiAdvancedChipUser()) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 15000);
            }
            WifiProStateMachine.this.mIsQoeFirstGood = false;
            WifiProStateMachine.this.mQoeBadCount = 0;
            WifiProStateMachine.this.mQoeFirstGoodTime = 0;
            WifiProChr.getInstance().setChrSwitchSuccStartTraffic(TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes());
        }

        private boolean isElevatorScene() {
            if (WifiProStateMachine.this.mWifiManager == null) {
                WifiProStateMachine.this.logI("mWifiManager is null");
                return false;
            }
            WifiInfo info = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
            if (info == null) {
                WifiProStateMachine.this.logI("info is null");
                return false;
            } else if (!WifiproUtils.isHwEnterprise(info)) {
                WifiProStateMachine.this.logI("Elevator scene: isHwWlan = false");
                return false;
            } else if (WifiProStateMachine.this.isRecording || !WifiProCommonUtils.isQueryActivityMatched(WifiProStateMachine.this.mContext, WifiProStateMachine.PACKAGE_NAME_HOME)) {
                long diffTimeFromDisconnect = System.currentTimeMillis() - WifiProStateMachine.this.mLastDisconnectedTime;
                if (diffTimeFromDisconnect <= 0 || diffTimeFromDisconnect > 30000) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("Elevator scene: disconnectEvent = false, isRecording = " + WifiProStateMachine.this.isRecording);
                    return false;
                } else if (WifiProStateMachine.this.isShortTimeWifiEnabled()) {
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logI("Elevator scene: isShortTimeWifiEnabled = true, isRecording = " + WifiProStateMachine.this.isRecording);
                    return false;
                } else {
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.logI("Elevator scene: satisfy all conditions, isRecording = " + WifiProStateMachine.this.isRecording);
                    return WifiProStateMachine.DBG;
                }
            } else {
                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                wifiProStateMachine4.logI("Elevator scene: hasForegroundApp = false, isRecording = " + WifiProStateMachine.this.isRecording);
                return false;
            }
        }

        public void exit() {
            WifiProStateMachine.this.logI("WiFiProVerfyingLinkState is Exit");
            WifiProStateMachine.this.handleWifi2CellSuccExitChr();
            WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
            cancelScanAndMonitor();
            WifiProStateMachine.this.mIsWiFiNoInternet = false;
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_MOBILE_SWITCH_DELAY);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_LAA_STATUS_CHANGED);
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(0);
            HwWifiConnectivityMonitor.getInstance().notifyVerifyingLinkState(false);
            this.mFailedDetectedCount = 0;
            WifiProStateMachine.this.mNotifyWifiLinkPoorReason = -1;
            this.mLastLinkPoorRssi = -127;
            this.mChrHandoverToMobileReason = 0;
            this.mChrHandoverBackStartTime = 0;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.APP_SERVICE_QOE_NOTIFY /* 12001 */:
                    handleUpdateWifiQoeMsg(msg);
                    break;
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /* 136169 */:
                    handleWifiNetworkStateChange(msg);
                    break;
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /* 136170 */:
                    handleDeviceScreenOn();
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /* 136171 */:
                    handleWifiProStateChange();
                    break;
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /* 136172 */:
                    handleWifiQosChangedInVerifyLinkState(msg);
                    break;
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /* 136176 */:
                    handleCheckAvalableApResult(msg);
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /* 136177 */:
                    handleNetworkConnectivityChange(WifiProStateMachine.DBG);
                    break;
                case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /* 136178 */:
                    handleWifiHandoverWifiResult(msg);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /* 136181 */:
                    handleCheckInternetResultInVerifyLinkState(msg);
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /* 136182 */:
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /* 136183 */:
                case WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_LATENCY /* 136212 */:
                case WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_RESULT /* 136213 */:
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /* 136293 */:
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /* 136185 */:
                    handleWifiStateChange();
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /* 136186 */:
                    handleMobileDataStateChange();
                    break;
                case WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT /* 136187 */:
                    this.isWifiGoodIntervalTimerOut = WifiProStateMachine.DBG;
                    break;
                case WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT /* 136188 */:
                    this.isWifiRecoveryTimerOut = WifiProStateMachine.DBG;
                    WifiProStateMachine.this.logI("isWifiRecoveryTimerOut is true,mobile can handover to wifi");
                    break;
                case WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI /* 136189 */:
                    WifiProStateMachine.this.logW("WiFiProVerfyingLinkState::EVENT_MOBILE_RECOVERY_TO_WIFI, handle it.");
                    this.isWifiHandoverWifi = false;
                    this.mIsChrUserHandoverBack = WifiProStateMachine.DBG;
                    restoreWifiConnect();
                    WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(3);
                    break;
                case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /* 136191 */:
                    handleRetryWifiToWifi();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /* 136192 */:
                    handleCheckInternetInVerifyLinkState(msg);
                    break;
                case WifiProStateMachine.EVENT_USER_ROVE_IN /* 136193 */:
                    handleUserRoveIn();
                    break;
                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /* 136195 */:
                    WifiProStateMachine.this.logW("WiFiProVerfyingLinkState::EVENT_HTTP_REACHABLE_RESULT, handle it.");
                    this.isRecoveryWifi = false;
                    if (!(msg.obj instanceof Boolean)) {
                        WifiProStateMachine.this.logE("WiFiProVerfyingLinkState::EVENT_HTTP_REACHABLE_RESULT, Class is not match");
                        break;
                    } else {
                        WifiProStateMachine.this.mIsWiFiNoInternet = ((Boolean) msg.obj).booleanValue();
                        break;
                    }
                case WifiProStateMachine.EVENT_LAA_STATUS_CHANGED /* 136200 */:
                    boolean is24gConnected = WifiProCommonUtils.isWifi5gConnected(WifiProStateMachine.this.mWifiManager) ^ WifiProStateMachine.DBG;
                    Bundle data = new Bundle();
                    data.putBoolean("is24gConnected", is24gConnected);
                    WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 31, data);
                    break;
                case WifiProStateMachine.EVENT_RECHECK_SMART_CARD_STATE /* 136209 */:
                    handleNetworkConnectivityChange(false);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND /* 136211 */:
                    handleDetectWifiBackground();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK /* 136219 */:
                    this.mIsNeedCheckWifiQoe = false;
                    handleWifiQoeBad();
                    break;
                case WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT /* 136224 */:
                    int level = msg.arg1;
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("EVENT_DISPATCH_INTERNET_RESULT in VerifyingState, level = " + msg.arg1);
                    WifiProStateMachine.this.handleInternetResultForDisplay(level, msg.arg2);
                    break;
                default:
                    return false;
            }
            return WifiProStateMachine.DBG;
        }

        private int getNetworkId() {
            Network network;
            Bundle bundle = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 14, (Bundle) null);
            if (bundle == null || (network = (Network) bundle.getParcelable("Network")) == null) {
                return -1;
            }
            return network.netId;
        }

        private void handleDetectWifiBackground() {
            WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
            this.mWaitWifiQoeGoodCnt = 0;
            Bundle data = new Bundle();
            int netId = getNetworkId();
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("handleDetectWifiBackground, netId = " + netId);
            if (netId != -1) {
                data.putInt("netId", netId);
                data.putInt("serverNum", 1);
                data.putInt("timeOut", 1000);
                data.putInt("detectTime", 3000);
                IHwCommBoosterServiceManager boosterManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
                if (boosterManager != null) {
                    boosterManager.reportBoosterPara("com.huawei.hwwifiproservice", (int) WifiProStateMachine.HWEMCOM_REQ_INFO, data);
                    this.mIsNeedCheckWifiQoe = WifiProStateMachine.DBG;
                }
            }
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
        }

        private void handleWifiQoeBad() {
            WifiProStateMachine.this.logI("WiFiProVerfyingLinkState handleWifiQoeBad, mFailedDetectedCount = " + this.mFailedDetectedCount + ", mNotifyWifiLinkPoorReason = " + WifiProStateMachine.this.mNotifyWifiLinkPoorReason);
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND);
            }
            if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason == 10 || WifiProStateMachine.this.mNotifyWifiLinkPoorReason == 107) {
                this.mFailedDetectedCount++;
                if (this.mFailedDetectedCount >= 3) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 60000);
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 30000);
                }
            } else if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason == 11) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, WifiProStateMachine.DELAY_UPLOAD_MS);
            } else if (WifiProStateMachine.this.mNotifyWifiLinkPoorReason == 12) {
                int deltaRssi = -127;
                if (!(WifiProCommonUtils.getCurrentRssi(WifiProStateMachine.this.mWifiManager) == -127 || this.mLastLinkPoorRssi == -127)) {
                    deltaRssi = WifiProCommonUtils.getCurrentRssi(WifiProStateMachine.this.mWifiManager) - this.mLastLinkPoorRssi;
                }
                WifiProStateMachine.this.logI("deltaRssi = " + deltaRssi);
                if (deltaRssi > 10) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 15000);
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 60000);
                }
            } else {
                WifiProStateMachine.this.logI("WiFiProVerfyingLinkState invalid reason");
            }
        }

        private void handleUpdateWifiQoeMsg(Message msg) {
            WifiProStateMachine.this.mAppQoeInfo.parseQoeInfo((Bundle) msg.obj);
            WifiProStateMachine.this.updateChrQoeGainStatistics();
            if (WifiProStateMachine.this.mAppQoeInfo.mNetworkQoeLevel == 5 && ((WifiProStateMachine.this.mAppQoeInfo.mMaxPktRatioChannelId == 0 || WifiProStateMachine.this.mAppQoeInfo.mMaxPktRatioChannelId == 1) && ((WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowReason & 1) == 1 || (WifiProStateMachine.this.mAppQoeInfo.mNetworkSlowReason & 2) == 2))) {
                this.mCellQoeBadCnt++;
                if (this.mCellQoeBadCnt >= 3) {
                    WifiProStateMachine.this.logI("cell qoe is bad for three times or above, reback to wifi");
                    handleMobileQosChange(2);
                    this.mCellQoeBadCnt = 0;
                    return;
                }
            } else {
                this.mCellQoeBadCnt = 0;
            }
            if (!this.mIsNeedCheckWifiQoe || WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeLevel == 0 || WifiProStateMachine.this.mAppQoeInfo.mWifiHttpProbeLatency == 0) {
                WifiProStateMachine.this.logI("Wifi qoe has no update, please wait..");
                return;
            }
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK);
            }
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND);
            }
            if (WifiProStateMachine.this.mAppQoeInfo.mMasterWifiChannelQoeLevel != 4 || WifiProStateMachine.this.mAppQoeInfo.mWifiHttpProbeLatency > WifiProStateMachine.TCP_RTT_MAYBE_BAD || !isWifiOtaQualityGood()) {
                this.mWaitWifiQoeGoodCnt = 0;
                handleWifiQoeBad();
                this.mIsNeedCheckWifiQoe = false;
            } else {
                this.mWaitWifiQoeGoodCnt++;
                if (this.mWaitWifiQoeGoodCnt >= 3) {
                    this.mIsNeedCheckWifiQoe = false;
                    WifiProChr.getInstance().setChrSwitchSuccedExitReason(201);
                    rebackToWifi();
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 30000);
                }
            }
            WifiProStateMachine.this.mAppQoeInfo.resetQoeInfo();
        }

        private boolean isWifiOtaQualityGood() {
            if (WifiProStateMachine.this.mWifiManager == null) {
                return false;
            }
            WifiInfo info = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
            if (info == null) {
                WifiProStateMachine.this.logE("WifiInfo is null");
                return false;
            }
            int txRate = info.getTxLinkSpeedMbps();
            int rxRate = info.getRxLinkSpeedMbps();
            int chLoad = info.getChload();
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("txRate = " + txRate + ", rxRate = " + rxRate + ", chLoad = " + chLoad);
            if ((info.is24GHz() && (txRate <= 13 || (rxRate > 0 && rxRate <= 27))) || ((info.is5GHz() && (txRate <= 27 || (rxRate > 0 && rxRate <= 40))) || WifiProCommonUtils.getCurrenSignalLevel(info) < 2)) {
                return false;
            }
            if (!WifiproUtils.isHwEnterprise(info)) {
                return WifiProStateMachine.DBG;
            }
            if ((!info.is24GHz() || chLoad < 500) && (!info.is5GHz() || chLoad < 500)) {
                return WifiProStateMachine.DBG;
            }
            return false;
        }

        private void handleStrongRssiRebackToWiFi(int responseCode, long detectTime) {
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND);
            }
            WifiInfo info = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
            if (info == null) {
                WifiProStateMachine.this.logI("WifiInfo is null");
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 60000);
                return;
            }
            int txRate = info.getTxLinkSpeedMbps();
            int rxRate = info.getRxLinkSpeedMbps();
            WifiProStateMachine.this.logI("txRate = " + txRate + ", rxRate = " + rxRate + ", mFailedDetectedCount" + this.mFailedDetectedCount + ", responseCode = " + responseCode + ", detectTime = " + detectTime);
            if (responseCode != 204 || detectTime > 700 || ((info.is24GHz() && (txRate <= 13 || rxRate <= 27)) || (info.is5GHz() && (txRate <= 27 || rxRate <= 40)))) {
                WifiProStateMachine.this.logI("Para does not meet the requirements");
                this.mFailedDetectedCount++;
                int i = this.mFailedDetectedCount;
                if (i >= 3) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 60000);
                } else if (i >= 2) {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 30000);
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 15000);
                }
            } else {
                this.mFailedDetectedCount = 0;
                rebackToWifi();
            }
        }

        private void rebackToWifi() {
            WifiProStateMachine.this.mCurrentTime = SystemClock.elapsedRealtime();
            long deltaTime = WifiProStateMachine.this.mCurrentTime - WifiProStateMachine.this.mLastTime;
            int deltaRssi = -127;
            if (!(WifiProCommonUtils.getCurrentRssi(WifiProStateMachine.this.mWifiManager) == -127 || this.mLastLinkPoorRssi == -127)) {
                deltaRssi = WifiProCommonUtils.getCurrentRssi(WifiProStateMachine.this.mWifiManager) - this.mLastLinkPoorRssi;
            }
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("mLastTime = " + WifiProStateMachine.this.mLastTime + ", mCurrentTime = " + WifiProStateMachine.this.mCurrentTime + ", deltaTime = " + deltaTime + ", deltaRssi = " + deltaRssi);
            if (WifiProStateMachine.this.mLastTime == 0 || deltaTime >= 300000 || (deltaTime >= 60000 && deltaRssi >= 10)) {
                WifiProStateMachine.this.notifyWifiLinkPoor(false, 0);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.mLastTime = wifiProStateMachine2.mCurrentTime;
                WifiProStateMachine.this.logI("notify link good");
            } else {
                WifiProStateMachine.this.logI("continue to monitor");
            }
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_BACKGROUND, 30000);
        }

        private void handleDeviceScreenOn() {
            if (this.isWifiScanScreenOff) {
                WifiProStateMachine.this.logI("isWifiScanScreenOff = true, retry scan wifi");
                this.isWifiScanScreenOff = false;
                this.wifiScanCounter = 0;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                return;
            }
            WifiProStateMachine.this.logI("isWifiScanScreenOff = false, wait a moment, retry scan wifi later");
        }

        private void handleUserRoveIn() {
            WifiProStateMachine.this.mIsUserHandoverWiFi = WifiProStateMachine.DBG;
            this.isWifiHandoverWifi = false;
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserManualRICount();
            } else {
                Log.i(WifiProStateMachine.TAG, "UNEXPECT_SWITCH_EVENT: USER_SELECT_OLD: enter:");
                WifiProStateMachine.this.uploadManager.addChrSsidCntStat("unExpectSwitchEvent", "userSelectOld");
                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(4);
            }
            WifiProStateMachine.this.uploadChrHandoverUnexpectedTypes(WifiProStateMachine.WIFI_HANDOVER_UNEXPECTED_TYPES[1]);
            this.mIsChrUserHandoverBack = WifiProStateMachine.DBG;
            restoreWifiConnect();
        }

        private void handleWifiHandoverWifiResult(Message msg) {
            if (msg.obj != null && (msg.obj instanceof Boolean)) {
                this.isWifiHandoverWifi = false;
                if (((Boolean) msg.obj).booleanValue()) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("connect other AP wifi : succeed ,go to WifiConnectedState, add WifiBlacklist: " + StringUtilEx.safeDisplayBssid(WifiProStateMachine.this.mCurrentBssid));
                    WifiProStateMachine.this.resetWifiEvaluteQosLevel();
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mCurrentBssid);
                    cancelScanAndMonitor();
                    WifiProStateMachine.this.refreshConnectedNetWork();
                    WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                    this.mIsChrUserHandoverBack = false;
                    restoreWifiConnect();
                } else if (WifiProStateMachine.this.mNewSelect_bssid != null && !WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mCurrentBssid)) {
                    WifiProStateMachine.this.logW("connect other AP wifi : Fallure");
                    WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
                    WifiProStateMachine.this.uploadChrWifiHandoverTypeStatistics(WifiProStateMachine.WIFI_HANDOVER_TYPES[8], WifiProStateMachine.HANDOVER_CNT);
                    WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mCurrentBssid);
                } else if (WifiProStateMachine.this.isWifiConnected()) {
                    WifiProStateMachine.this.logI("wifi handover wifi fail, continue monitor");
                }
            }
        }

        private void handleCheckAvalableApResult(Message msg) {
            if (this.isRecoveryWifi || this.isWifiHandoverWifi) {
                WifiProStateMachine.this.logI("receive check available ap result,but is isRecoveryWifi");
            } else if (msg.obj != null && (msg.obj instanceof Boolean)) {
                if (((Boolean) msg.obj).booleanValue()) {
                    WifiProStateMachine.this.logI("Exist a vailable AP,connect this AP and cancel Sacn Timer");
                    this.isWifiHandoverWifi = WifiProStateMachine.DBG;
                    if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, 0, 5)) {
                        this.isWifiHandoverWifi = false;
                        return;
                    }
                    return;
                }
                WifiProStateMachine.this.logE("There is no vailble ap, continue verfyinglink");
            }
        }

        private void handleNetworkConnectivityChange(boolean isNeedCheckDualCard) {
            NetworkInfo mobileInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(0);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("networkConnetc change :mobileInfo : " + mobileInfo + ", mIsMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
            if (WifiProStateMachine.this.mIsMobileDataEnabled && mobileInfo != null && NetworkInfo.State.DISCONNECTED == mobileInfo.getState()) {
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("mobile network service is disconnected, mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet);
                NetworkInfo wInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
                if (!WifiProStateMachine.this.mIsWiFiNoInternet && wInfo != null && NetworkInfo.DetailedState.VERIFYING_POOR_LINK == wInfo.getDetailedState()) {
                    int isSmartDualCardState = 0;
                    if (isNeedCheckDualCard) {
                        isSmartDualCardState = SystemProperties.getInt(WifiProStateMachine.KEY_SMART_DUAL_CARD_STATE, 0);
                    }
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.logI("isSmartDualCardState = " + isSmartDualCardState);
                    WifiProChr.getInstance().setChrSwitchSuccedExitReason(203);
                    if (isSmartDualCardState == 1) {
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_RECHECK_SMART_CARD_STATE)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RECHECK_SMART_CARD_STATE);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RECHECK_SMART_CARD_STATE, 10000);
                        return;
                    }
                    this.isWifiHandoverWifi = false;
                    this.mIsChrUserHandoverBack = WifiProStateMachine.DBG;
                    restoreWifiConnect();
                }
            } else if (!WifiProStateMachine.this.mIsMobileDataEnabled || mobileInfo == null || mobileInfo.getState() != NetworkInfo.State.CONNECTED) {
                WifiProStateMachine.this.logI("Skip this ConnectivityChange at this time");
            } else {
                handleSocketStrategy();
            }
        }

        private void handleSocketStrategy() {
            HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
            if (autoConnectManager == null) {
                WifiProStateMachine.this.logE("HwAutoConnectManager is null, return!!");
                return;
            }
            int topUid = autoConnectManager.getCurrentTopUid();
            String pktName = autoConnectManager.getCurrentPackageName();
            HashMap<Integer, String> hashMap = this.mTopAppWhiteList;
            if (hashMap == null || hashMap.isEmpty() || TextUtils.isEmpty(pktName) || topUid <= 0) {
                WifiProStateMachine.this.logE("params do not meet the requirement, return!!");
                return;
            }
            Object obj = HwFrameworkFactory.getHwInnerNetworkManager();
            if (!(obj instanceof HwInnerNetworkManagerImpl)) {
                WifiProStateMachine.this.logE("obj is not instanceof HwInnerNetworkManagerImpl, return!!");
                return;
            }
            HwInnerNetworkManagerImpl hwInnerNetworkManagerImpl = (HwInnerNetworkManagerImpl) obj;
            HashMap<Integer, String> hashMap2 = this.mTopAppWhiteList;
            if (hashMap2 != null && hashMap2.containsValue(pktName)) {
                hwInnerNetworkManagerImpl.closeSocketsForUid(topUid);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("TCP closeSocketsForUid = " + topUid + " pktName=" + pktName);
            }
        }

        private void handleWifiNetworkStateChange(Message msg) {
            Intent intent = null;
            if (msg.obj instanceof Intent) {
                intent = (Intent) msg.obj;
            }
            if (intent != null && !this.isWifiHandoverWifi) {
                Object objNetworkInfo = intent.getParcelableExtra("networkInfo");
                NetworkInfo networkInfo = null;
                if (objNetworkInfo instanceof NetworkInfo) {
                    networkInfo = (NetworkInfo) objNetworkInfo;
                } else {
                    WifiProStateMachine.this.logE("handleWifiNetworkStateChange:Class is not match");
                }
                if (networkInfo != null) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("WiFiProVerfyingLinkState :Network state change " + networkInfo.getDetailedState());
                }
                if (networkInfo != null && NetworkInfo.State.DISCONNECTED == networkInfo.getState()) {
                    WifiProStateMachine.this.logI("WiFiProVerfyingLinkState : wifi has disconnected");
                    WifiProStateMachine.this.mIsRoveOutToDisconn = WifiProStateMachine.DBG;
                    WifiProChr.getInstance().setChrSwitchSuccedExitReason(204);
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiDisConnectedState);
                } else if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                    this.isRecoveryWifi = false;
                    WifiProStateMachine.this.mChrWifiHandoverType = WifiProStateMachine.WIFI_HANDOVER_TYPES[7];
                    if (this.mIsChrRestoreWifi) {
                        WifiProStateMachine.this.uploadChrWifiHandoverTypeStatistics(WifiProStateMachine.WIFI_HANDOVER_TYPES[7], WifiProStateMachine.HANDOVER_SUCC_CNT);
                        WifiProStateMachine.this.uploadChrHandoverBackDuration(this.mChrHandoverToMobileReason, this.mIsChrUserHandoverBack, this.mChrHandoverBackStartTime);
                        this.mIsChrRestoreWifi = false;
                    }
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(4);
                    WifiProStateMachine.this.logI("WiFiProVerfyingLinkState: Restore the wifi successful,go to mWiFiLinkMonitorState");
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, WifiProStateMachine.DBG);
                    WifiProStateMachine.this.mVerfyingToConnectedState = WifiProStateMachine.DBG;
                    WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                    wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiConnectedState);
                }
            }
        }

        private void handleMobileQosChange(int level) {
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.logI("both wifi and mobile is unusable,can not restore wifi ");
            } else if (!this.isRecoveryWifi && !this.isWifiHandoverWifi) {
                WifiProStateMachine.this.logI("Mobile Qos is poor,try restore wifi,mobile handover wifi");
                WifiProChr.getInstance().setChrSwitchSuccedExitReason(201);
                this.isRecoveryWifi = WifiProStateMachine.DBG;
                this.mIsChrUserHandoverBack = false;
                restoreWifiConnect();
                WifiProStateMachine.access$17408(WifiProStateMachine.this);
                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(7);
            }
        }

        private void handleRetryWifiToWifi() {
            if (WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                WifiProStateMachine.this.logI("inquire the surrounding AP for wifiHandover");
                if (!this.isWifiHandoverWifi) {
                    WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                    this.wifiScanCounter++;
                    int i = this.wifiScanCounter;
                    if (i > 12) {
                        i = 12;
                    }
                    this.wifiScanCounter = i;
                }
                long delayScanTime = ((long) Math.pow(2.0d, (double) (this.wifiScanCounter / 4))) * 60 * 1000;
                WifiProStateMachine.this.logI("delayScanTime = " + delayScanTime);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delayScanTime);
                return;
            }
            this.isWifiScanScreenOff = WifiProStateMachine.DBG;
        }

        private void handleNoInternetInVerfifyLinkState() {
            WifiProStateMachine.this.logI("WiFiProVerfyingLinkState wifi no internet detected time = " + this.wifiNoInternetCounter);
            this.wifiQosLevel = 0;
            this.wifiNoInternetCounter = this.wifiNoInternetCounter + 1;
            if (this.wifiInternetLevel == -1 && this.internetFailureDetectedCount == 0) {
                HwWifiProFeatureControl.getInstance();
                if (!HwWifiProFeatureControl.isSelfCureOngoing() && WifiProStateMachine.this.isWifiConnected()) {
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.mCurrentRssi = WifiProCommonUtils.getCurrentRssi(wifiProStateMachine.mWifiManager);
                    WifiProStateMachine.this.logI("WiFiProVerfyingLinkState mCurrentRssi = " + WifiProStateMachine.this.mCurrentRssi);
                    if (WifiProStateMachine.this.mCurrentRssi >= -70) {
                        HwWifiProFeatureControl.getInstance();
                        HwWifiProFeatureControl.notifyInternetFailureDetected(WifiProStateMachine.DBG);
                        this.internetFailureDetectedCount++;
                    }
                }
            }
        }

        private void handleRecoverWifiInVerfifyLinkState() {
            WifiProStateMachine.this.mIsWiFiNoInternet = false;
            if (!this.isRecoveryWifi) {
                this.isRecoveryWifi = WifiProStateMachine.DBG;
                WifiProStateMachine.this.logI("wifi Internet is better ,try restore wifi 2,mobile handover wifi");
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetRestoreRICount();
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 4, WifiProStateMachine.this.mCurrentSsid);
                this.mIsChrUserHandoverBack = false;
                restoreWifiConnect();
            }
        }

        private void handleCheckInternetResultInVerifyLinkState(Message msg) {
            int i;
            this.wifiInternetLevel = msg.arg1;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("WiFi internet level = " + this.wifiInternetLevel + ", wifiQosLevel = " + this.wifiQosLevel + ", isRecoveryWifi = " + this.isRecoveryWifi);
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logI("mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet + ", isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut);
            if (this.isWifiRecoveryTimerOut) {
                int i2 = this.wifiInternetLevel;
                if (i2 == -1 || i2 == 6) {
                    handleNoInternetInVerfifyLinkState();
                }
                if (!this.isWifiHandoverWifi && this.isRecoveryWifi) {
                    int i3 = this.wifiInternetLevel;
                    if (i3 == -1 || i3 == 6 || this.wifiQosLevel <= 2) {
                        this.isRecoveryWifi = false;
                    } else {
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.logI("wifi Qos is [" + this.wifiQosLevel + " ]Ok, wifiInternetLevel is [" + this.wifiInternetLevel + "] Restore the wifi connection");
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(1);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, 3);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, 3, 0);
                        this.mIsChrUserHandoverBack = false;
                        restoreWifiConnect();
                    }
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet && (i = this.wifiInternetLevel) != -1 && i != 6 && !this.isWifiHandoverWifi) {
                    handleRecoverWifiInVerfifyLinkState();
                }
                updateCurrentWifiInternetStatus();
            }
        }

        private void updateCurrentWifiInternetStatus() {
            if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                int i = this.wifiInternetLevel;
                if (i == -1 || i == 6) {
                    WifiProStateMachine.this.mIsWiFiNoInternet = WifiProStateMachine.DBG;
                    if (this.isRecoveryWifi && !this.isWifiHandoverWifi) {
                        this.isRecoveryWifi = false;
                    }
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("WiFiProVerfyingLinkState updateCurrentWifiInternetStatus mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet + ", isRecoveryWifi = " + this.isRecoveryWifi);
                }
            }
        }

        private void handleCheckInternetInVerifyLinkState(Message msg) {
            WifiProStateMachine.this.logW("start check wifi internet, wifiNoInternetCounter = " + this.wifiNoInternetCounter);
            int i = this.wifiNoInternetCounter;
            if (i > 12) {
                i = 12;
            }
            this.wifiNoInternetCounter = i;
            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, WifiProStateMachine.DBG);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, ((long) Math.pow(2.0d, Math.floor(((double) this.wifiNoInternetCounter) / 4.0d))) * 30000);
        }

        private void handleWifiProStateChange() {
            if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                WifiProStateMachine.this.logI("wifiprochr user close wifipro");
                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(5);
                WifiProStateMachine.this.mRoveOutStarted = false;
                WifiProStateMachine.this.onDisableWiFiPro();
            }
        }

        private void handleMobileDataStateChange() {
            if (!WifiProStateMachine.this.mIsMobileDataEnabled) {
                int delayMs = WifiProStateMachine.this.mIsWiFiNoInternet ? 3000 : 0;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("In verifying link state, MOBILE DATA is OFF, try to delay " + delayMs + " ms to switch back to wifi.");
                WifiProChr.getInstance().setChrSwitchSuccedExitReason(202);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.sendMessageDelayed(wifiProStateMachine2.obtainMessage(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI), (long) delayMs);
            } else if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI)) {
                WifiProStateMachine.this.logI("In verifying link state, MOBILE DATA is ON within delay time, cancel switching back to wifi.");
                WifiProStateMachine.this.getHandler().removeMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI);
            }
        }

        private void handleWifiStateChange() {
            if (WifiProStateMachine.this.mWifiManager.getWifiState() == 1) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("wifi state is : " + WifiProStateMachine.this.mWifiManager.getWifiState() + " ,go to wifi disconnected");
                WifiProStateMachine.this.mIsRoveOutToDisconn = WifiProStateMachine.DBG;
                WifiProChr.getInstance().setChrSwitchSuccedExitReason(204);
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiDisConnectedState);
            } else if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                WifiProStateMachine.this.logI("wifi state is : enabled, forgetUntrustedOpenAp");
                WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            }
        }

        private void handleWifiQosChangedInVerifyLinkState(Message msg) {
            boolean updateUiOnly = false;
            if (msg.obj instanceof Boolean) {
                updateUiOnly = ((Boolean) msg.obj).booleanValue();
            } else {
                WifiProStateMachine.this.logE("handleWifiQosChangedInVerifyLinkState:Class is not match");
            }
            if (updateUiOnly) {
                WifiProStateMachine.this.logI("wifi is connected background, no UI to update");
                return;
            }
            if (msg.arg1 == 3) {
                this.isWifiRecoveryTimerOut = WifiProStateMachine.DBG;
                WifiProStateMachine.this.logI("force to switch to wifi if good enough.");
            }
            HwWifiProFeatureControl.getInstance();
            boolean isSelfCureOngoing = HwWifiProFeatureControl.isSelfCureOngoing();
            if (this.isRecoveryWifi || this.isWifiHandoverWifi || !this.isWifiRecoveryTimerOut || !WifiProStateMachine.this.isWifiConnected() || isSelfCureOngoing) {
                WifiProStateMachine.this.logI("isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut + ", isRecoveryWifi = " + this.isRecoveryWifi + ", isSelfCureOngoing = " + isSelfCureOngoing);
                return;
            }
            this.wifiQosLevel = msg.arg1;
            if (this.wifiQosLevel > 2 && this.isWifiGoodIntervalTimerOut) {
                WifiProStateMachine.this.logI("wifi Qos is [" + this.wifiQosLevel + " ]Ok, start check wifi internet, wifiNoInternetCounter = " + this.wifiNoInternetCounter);
                this.isRecoveryWifi = WifiProStateMachine.DBG;
                this.isWifiGoodIntervalTimerOut = false;
                int i = this.wifiNoInternetCounter;
                if (i > 12) {
                    i = 12;
                }
                this.wifiNoInternetCounter = i;
                long wifiCheckDelayTime = ((long) Math.pow(2.0d, Math.floor(((double) this.wifiNoInternetCounter) / 4.0d))) * 30000;
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT, wifiCheckDelayTime);
                WifiProStateMachine.this.logI("WifiCheckDelayTime=" + wifiCheckDelayTime);
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, WifiProStateMachine.DBG);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInternetResultForDisplay(int level, int delayShow) {
        boolean z = DBG;
        if (level == -1 && delayShow != 1 && !hasMessages(EVENT_DISPATCH_INTERNET_RESULT)) {
            logI("handleInternetResultForDisplay, delay show no net");
            sendMessageDelayed(EVENT_DISPATCH_INTERNET_RESULT, level, 1, DELAY_UPLOAD_MS);
        } else if (level == -1 || !hasMessages(EVENT_DISPATCH_INTERNET_RESULT)) {
            logI("handleQosLevel, do nothing");
        } else {
            logI("handleInternetResultForDisplay, remove EVENT_DISPATCH_INTERNET_RESULT");
            removeMessages(EVENT_DISPATCH_INTERNET_RESULT);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("handleInternetResultForDisplay, show Immediately:");
        sb.append(level != -1 || delayShow == 1);
        logI(sb.toString());
        if (delayShow != 1) {
            z = false;
        }
        updateWifiInternetStateChange(level, z);
    }

    /* access modifiers changed from: package-private */
    public class WifiDisConnectedState extends State {
        WifiDisConnectedState() {
        }

        public void enter() {
            WifiProStateMachine.this.mChrWifiHandoverType = "";
            WifiProStateMachine.this.mChrQosLevelBeforeHandover = 0;
            WifiProStateMachine.this.mWifiHandoverStartTime = 0;
            WifiProStateMachine.this.mIsChrQosBetterAfterDualbandHandover = false;
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getMasterCardSubId());
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_UNREGISTER_CELL_CARD, WifiProCommonUtils.getSlaveCardSubId());
            if (WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DISPATCH_INTERNET_RESULT);
            }
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHR_CHECK_WIFI_HANDOVER)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHR_CHECK_WIFI_HANDOVER);
            }
            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_STATUS)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_STATUS);
            }
            if (WifiProStateMachine.this.mChrDualbandConnectedStartTime != 0) {
                WifiProStateMachine.this.uploadChrDualBandOnLineTime();
                WifiProStateMachine.this.mChrDualbandConnectedStartTime = 0;
            }
            boolean unused = WifiProStateMachine.isWifiManualEvaluating = false;
            boolean unused2 = WifiProStateMachine.isWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.uploadChrDetectAlgorithmDuration();
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.setWifiEvaluateTag(false);
            if (WifiProStateMachine.this.mOpenAvailableAPCounter >= 2) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
            }
            WifiProStateMachine.this.logI("WifiDisConnectedState is Enter");
            WifiProStateMachine.this.mLastDisconnectedTime = System.currentTimeMillis();
            WifiProStateMachine.this.mIsPortalAp = false;
            WifiProStateMachine.this.mIsNetworkAuthen = false;
            WifiProStateMachine.this.resetWifiEvaluteQosLevel();
            WifiProStateMachine.this.resetVariables();
            WifiProStateMachine.this.mVerfyingToConnectedState = false;
            WifiProStateMachine.this.mDisconnectToConnectedState = false;
            WifiProStateMachine.this.mLastTime = 0;
            WifiProStateMachine.this.mCellToWiFiCnt = 0;
            WifiProStateMachine.this.mNotifyWifiLinkPoorReason = -1;
            WifiProStateMachine.this.isPortalExpired = false;
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                WifiProStateMachine.this.logI("BQE bad rove out, disconnect time recorded.");
                WifiProStateMachine.this.mChrWifiDisconnectStartTime = System.currentTimeMillis();
            }
            if (WifiProStateMachine.this.mRoveOutStarted && WifiProStateMachine.this.mIsRoveOutToDisconn) {
                if (WifiProStateMachine.this.mLoseInetRoveOut) {
                    WifiProStateMachine.this.logI("Not Inet rove out and WIFI disconnect.");
                    WifiProStateMachine.this.mWifiProStatisticsManager.accuNotInetRoDisconnectData();
                } else {
                    WifiProStateMachine.this.logI("Qoe bad rove out and WIFI disconnect.");
                    WifiProStateMachine.this.mWifiProStatisticsManager.accuQOEBadRoDisconnectData();
                }
            }
            if (WifiProStateMachine.this.mPhoneStateListenerRegisted) {
                WifiProStateMachine.this.logI("stop PhoneStateListener");
                WifiProStateMachine.this.mTelephonyManager.listen(WifiProStateMachine.this.phoneStateListener, 0);
                WifiProStateMachine.this.mPhoneStateListenerRegisted = false;
            }
            WifiProStateMachine.this.recoveryWlanSwitch();
            WifiProStateMachine.this.mRoveOutStarted = false;
            WifiProStateMachine.this.mIsRoveOutToDisconn = false;
        }

        public void exit() {
            WifiProStateMachine.this.logI("WifiDisConnectedState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /* 136169 */:
                    if (!(msg.obj instanceof Intent)) {
                        WifiProStateMachine.this.logE("processMessage:Intent is not match the Class");
                        break;
                    } else {
                        Object objNetworkInfo = ((Intent) msg.obj).getParcelableExtra("networkInfo");
                        NetworkInfo networkInfo = null;
                        if (objNetworkInfo instanceof NetworkInfo) {
                            networkInfo = (NetworkInfo) objNetworkInfo;
                        } else {
                            WifiProStateMachine.this.logE("processMessage:networkInfo is not match the Class");
                        }
                        if (networkInfo == null || NetworkInfo.State.CONNECTED != networkInfo.getState() || !WifiProStateMachine.this.isWifiConnected()) {
                            if (networkInfo != null && NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                                WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                                break;
                            }
                        } else {
                            WifiProStateMachine.this.logI("WifiDisConnectedState: wifi connect,to go connected");
                            WifiProStateMachine.this.mDisconnectToConnectedState = WifiProStateMachine.DBG;
                            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                            wifiProStateMachine.transitionTo(wifiProStateMachine.mWifiConnectedState);
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /* 136186 */:
                    if (!WifiProStateMachine.this.isMobileDataConnected()) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_NETWORK_USER_CONNECT /* 136202 */:
                    if (msg.obj != null && (msg.obj instanceof Boolean) && ((Boolean) msg.obj).booleanValue()) {
                        WifiProStateMachine.this.mIsUserManualConnectSuccess = WifiProStateMachine.DBG;
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.logI("receive EVENT_NETWORK_USER_CONNECT, set mIsUserManualConnectSuccess = " + WifiProStateMachine.this.mIsUserManualConnectSuccess);
                        break;
                    }
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /* 136308 */:
                    if (!(msg.obj instanceof Intent)) {
                        WifiProStateMachine.this.logE("EVENT_CONFIGURED_NETWORKS_CHANGED:configIntent is not match");
                        break;
                    } else {
                        Intent configIntent = (Intent) msg.obj;
                        Object objConfiguration = configIntent.getParcelableExtra("wifiConfiguration");
                        WifiConfiguration connCfg = null;
                        if (objConfiguration instanceof WifiConfiguration) {
                            connCfg = (WifiConfiguration) objConfiguration;
                        } else {
                            WifiProStateMachine.this.logE("EVENT_CONFIGURED_NETWORKS_CHANGED:WifiConfiguration is not match");
                        }
                        if (connCfg != null) {
                            int changeReason = configIntent.getIntExtra("changeReason", -1);
                            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                            wifiProStateMachine3.logI("WifiDisConnectedState, change reson " + changeReason + ", isTempCreated = " + connCfg.isTempCreated);
                            if (connCfg.isTempCreated && changeReason != 1) {
                                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                                wifiProStateMachine4.logI("WifiDisConnectedState, forget " + connCfg.SSID);
                                WifiProStateMachine.this.mWifiManager.forget(connCfg.networkId, null);
                                break;
                            }
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_FIRST_CONNECTED /* 136373 */:
                    WifiProStateMachine.this.logI("EVENT_WIFI_FIRST_CONNECTED: go to connected");
                    WifiProStateMachine wifiProStateMachine5 = WifiProStateMachine.this;
                    wifiProStateMachine5.transitionTo(wifiProStateMachine5.mWifiConnectedState);
                    break;
                default:
                    return false;
            }
            return WifiProStateMachine.DBG;
        }
    }

    /* access modifiers changed from: package-private */
    public class WifiSemiAutoEvaluateState extends State {
        WifiSemiAutoEvaluateState() {
        }

        public void enter() {
            WifiProStateMachine.this.logI("WifiSemiAutoEvaluateState enter");
            WifiProStateMachine.this.setWifiCSPState(0);
            if (!WifiProStateMachine.isWifiSemiAutoEvaluating) {
                WifiProStateMachine.this.setWifiEvaluateTag(WifiProStateMachine.DBG);
                boolean unused = WifiProStateMachine.isWifiSemiAutoEvaluating = WifiProStateMachine.DBG;
                WifiProStateMachine.this.mIsAllowEvaluate = WifiProStateMachine.DBG;
                if (WifiProStateMachine.this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty() || !HwWifiProFeatureControl.sWifiProOpenApEvaluateCtrl) {
                    WifiProStateMachine.this.logW("UnEvaluate AP records is empty !");
                } else {
                    WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.transitionTo(wifiProStateMachine.mWifiSemiAutoScoreState);
                    return;
                }
            }
            boolean unused2 = WifiProStateMachine.isWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
        }

        public void exit() {
            WifiProStateMachine.this.logI("WifiSemiAutoEvaluateState exit");
            if (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                WifiProStateMachine.this.setWifiEvaluateTag(false);
                WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            }
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateCacheRecords();
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /* 136169 */:
                case WifiProStateMachine.EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED /* 136203 */:
                    handleWifiNetworkStateChange(msg);
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /* 136293 */:
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_EVALUATE_COMPLETE /* 136295 */:
                    handleEvaluateComplete();
                    return WifiProStateMachine.DBG;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /* 136300 */:
                    if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        return WifiProStateMachine.DBG;
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
                    return WifiProStateMachine.DBG;
                default:
                    return false;
            }
        }

        private void handleEvaluateComplete() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("Evaluate has complete, restore wifi Config, mOpenAvailableAPCounter = " + WifiProStateMachine.this.mOpenAvailableAPCounter);
            if (WifiProStateMachine.this.mOpenAvailableAPCounter >= 2) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
            }
            WiFiProEvaluateController unused = WifiProStateMachine.this.mWiFiProEvaluateController;
            WiFiProEvaluateController.evaluateAPHashMapDump();
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
            boolean unused2 = WifiProStateMachine.isWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = WifiProStateMachine.DBG;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            NetworkInfo wifiInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
            if (wifiInfo == null) {
                WifiProStateMachine.this.logI("wifiInfo is null, go to mWiFiProEnableState");
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiDisConnectedState);
            } else if (wifiInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                WifiProStateMachine.this.logI("wifi has disconnected, go to mWifiDisConnectedState");
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiDisConnectedState);
            }
        }

        private void handleWifiNetworkStateChange(Message msg) {
            if (msg.obj instanceof Intent) {
                Object objNetworkInfo = ((Intent) msg.obj).getParcelableExtra("networkInfo");
                NetworkInfo networkInfo = null;
                if (objNetworkInfo instanceof NetworkInfo) {
                    networkInfo = (NetworkInfo) objNetworkInfo;
                }
                if (networkInfo != null && NetworkInfo.DetailedState.CONNECTED == networkInfo.getDetailedState() && WifiProStateMachine.this.isWifiConnected()) {
                    WifiProStateMachine.this.setWifiEvaluateTag(false);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI("mIsWifiSemiAutoEvaluateComplete == " + WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete);
                    WifiProStateMachine.this.logD("******WifiSemiAutoEvaluateState go to mWifiConnectedState *****");
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiConnectedState);
                } else if (networkInfo != null && NetworkInfo.DetailedState.DISCONNECTED == networkInfo.getDetailedState()) {
                    if (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.logW("Evaluate has complete, go to mWifiDisConnectedState");
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiDisConnectedState);
                        WifiProStateMachine.this.setWifiEvaluateTag(false);
                    }
                }
            } else {
                WifiProStateMachine.this.logE("handleWifiNetworkStateChange msg.obj is null or not intent");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class WifiSemiAutoScoreState extends State {
        private int checkCounter;
        private long checkTime;
        private long connectTime;
        private boolean isCheckRuning;
        private String nextBSSID = null;
        private String nextSSID = null;

        WifiSemiAutoScoreState() {
        }

        public void enter() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI("WifiSemiAutoScoreState enter,  mIsAllowEvaluate = " + WifiProStateMachine.this.mIsAllowEvaluate);
            if (isStopEvaluteNextAP()) {
                WifiProStateMachine.this.logI("WiFiPro auto Evaluate has  closed");
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiSemiAutoEvaluateState);
                return;
            }
            this.connectTime = 0;
            this.checkTime = 0;
            this.checkCounter = 0;
            this.isCheckRuning = false;
            this.nextSSID = WifiProStateMachine.this.mWiFiProEvaluateController.getNextEvaluateWiFiSSID();
            if (TextUtils.isEmpty(this.nextSSID)) {
                WifiProStateMachine.this.logI("ALL SemiAutoScore has Evaluate complete!");
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiSemiAutoEvaluateState);
                return;
            }
            WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
            wifiProStateMachine4.logI("***********start SemiAuto Evaluate nextSSID :" + StringUtilEx.safeDisplaySsid(this.nextSSID));
            if (WifiProStateMachine.this.mWiFiProEvaluateController.isAbandonEvaluate(this.nextSSID)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUTE_ABANDON);
                return;
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT, 75000);
            WifiProUIDisplayManager wifiProUIDisplayManager = WifiProStateMachine.this.mWifiProUIDisplayManager;
            wifiProUIDisplayManager.showToastL("start  evaluate :" + StringUtilEx.safeDisplaySsid(this.nextSSID));
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(this.nextSSID, 0);
            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 0, this.nextSSID);
            WifiProStateMachine.this.refreshConnectedNetWork();
            if (!WifiProStateMachine.this.isWifiConnected() || !this.nextSSID.equals(WifiProStateMachine.this.mCurrentSsid)) {
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
                return;
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, WifiProStateMachine.DBG);
            this.isCheckRuning = WifiProStateMachine.DBG;
        }

        public void exit() {
            WifiProStateMachine.this.logI("WifiSemiAutoScoreState exit");
            WifiProStateMachine.this.mNetworkQosMonitor.resetMonitorStatus();
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP);
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            this.nextBSSID = null;
            if (!TextUtils.isEmpty(this.nextSSID)) {
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(this.nextSSID, 1);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /* 136169 */:
                case WifiProStateMachine.EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED /* 136203 */:
                    handleWifiStateChange(msg);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /* 136181 */:
                    handleInternetCheckReusltInAutoScoreState(msg);
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /* 136293 */:
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.mScanResultList = wifiProStateMachine.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mWifiManager.getScanResults());
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.mIsAllowEvaluate = wifiProStateMachine2.mWiFiProEvaluateController.isAllowAutoEvaluate(WifiProStateMachine.this.mScanResultList);
                    if (!WifiProStateMachine.this.mIsAllowEvaluate) {
                        WifiProStateMachine.this.logI("discover save ap, stop allow evaluate");
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiSemiAutoEvaluateState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /* 136297 */:
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /* 136300 */:
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT /* 136299 */:
                    handleWifiEvaluteTcpResult(msg);
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT /* 136301 */:
                    handleWifiEvaluteConnectTimeout();
                    break;
                case WifiProStateMachine.EVENT_LAST_EVALUTE_VALID /* 136302 */:
                    handleEventLastEvaluteValid();
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_TIMEOUT /* 136304 */:
                    handleEventEvaluteTimeout();
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_ABANDON /* 136305 */:
                    WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                    wifiProStateMachine4.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + "abandon evalute ");
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
                    WifiProStateMachine wifiProStateMachine5 = WifiProStateMachine.this;
                    wifiProStateMachine5.transitionTo(wifiProStateMachine5.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET /* 136307 */:
                    handleEvaluteCheck();
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /* 136308 */:
                    handleNetworkConfigChange(msg);
                    break;
                case WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED /* 136310 */:
                    if (WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                        WifiProStateMachine.this.logI("P2PConnectedOrConnecting  , stop allow evaluate");
                        WifiProStateMachine wifiProStateMachine6 = WifiProStateMachine.this;
                        wifiProStateMachine6.transitionTo(wifiProStateMachine6.mWifiSemiAutoEvaluateState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_SECURITY_RESPONSE /* 136312 */:
                case WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT /* 136313 */:
                    handleWifiSecurityTimeout(msg);
                    break;
                case WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP /* 136314 */:
                    if (!WifiProStateMachine.this.isWifiConnected()) {
                        evaluteNextAP();
                        break;
                    } else {
                        WifiProStateMachine.this.logI("wifi still connectd, delay 2s to evalute next ap");
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
                        break;
                    }
                case WifiProStateMachine.EVENT_BQE_ANALYZE_NETWORK_QUALITY /* 136317 */:
                    if (!WifiProStateMachine.this.mNetworkQosMonitor.isBqeServicesStarted()) {
                        WifiProStateMachine.this.logI("EVENT_BQE_ANALYZE_NETWORK_QUALITY, isBqeServicesStarted = false.");
                        WifiProStateMachine wifiProStateMachine7 = WifiProStateMachine.this;
                        wifiProStateMachine7.transitionTo(wifiProStateMachine7.mWifiSemiAutoScoreState);
                        break;
                    } else {
                        WifiProStateMachine.this.mNetworkQosMonitor.startWiFiBqeDetect(3000);
                        break;
                    }
                default:
                    return false;
            }
            return WifiProStateMachine.DBG;
        }

        private void handleEventLastEvaluteValid() {
            WiFiProScoreInfo wiFiProScoreInfo = WifiProStateMachine.this.mWiFiProEvaluateController.getCurrentWiFiProScoreInfo(this.nextSSID);
            if (wiFiProScoreInfo != null) {
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), wiFiProScoreInfo.internetAccessType, wiFiProScoreInfo.networkQosLevel, wiFiProScoreInfo.networkQosScore);
            }
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.transitionTo(wifiProStateMachine.mWifiSemiAutoScoreState);
        }

        private void handleWifiEvaluteConnectTimeout() {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + " Conenct Time Out,connect fail! conenct Time = 35s");
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(15);
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(this.nextSSID);
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(20);
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
            WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiSemiAutoScoreState);
        }

        private void handleEventEvaluteTimeout() {
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(this.nextSSID);
            WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + " evaluate Time = 70s");
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiSemiAutoScoreState);
        }

        private void handleWifiSecurityTimeout(Message msg) {
            if (msg.obj != null) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT);
                if (msg.obj instanceof Bundle) {
                    Bundle bundle = (Bundle) msg.obj;
                    String ssid = bundle.getString("com.huawei.wifipro.FLAG_SSID");
                    if (ssid == null || !ssid.equals(this.nextSSID)) {
                        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                        wifiProStateMachine.logI("handle EVENT_WIFI_SECURITY_RESPONSE, it's invalid ssid = " + StringUtilEx.safeDisplaySsid(ssid) + ", ignore the result.");
                        return;
                    }
                    int status = bundle.getInt("com.huawei.wifipro.FLAG_SECURITY_STATUS");
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logI("handle EVENT_WIFI_SECURITY_RESPONSE, ssid = " + StringUtilEx.safeDisplaySsid(ssid) + ", status = " + status);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, status);
                    if (status >= 2) {
                        WifiProStateMachine.this.logI("handle EVENT_WIFI_SECURITY_RESPONSE, unsecurity, upload CHR statistic.");
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(4);
                    }
                } else {
                    WifiProStateMachine.this.logE("handleWifiSecurityTimeout bundle is null");
                    return;
                }
            } else {
                WifiProStateMachine.this.logW("EVENT_WIFI_SECURITY_RESPONSE, timeout happend.");
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, -1);
            }
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, (boolean) WifiProStateMachine.DBG);
            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
            wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiSemiAutoScoreState);
        }

        private void handleWifiEvaluteTcpResult(Message msg) {
            int level = msg.arg1;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + "  TCPRTT  level = " + level);
            if (WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(this.nextSSID, level)) {
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 2, level, this.nextSSID);
            }
            if (level == 0) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(this.nextSSID);
            }
            boolean enabled = WifiProCommonUtils.isWifiSecDetectOn(WifiProStateMachine.this.mContext);
            int security = WifiProStateMachine.this.mWiFiProEvaluateController.getWifiSecurityInfo(this.nextSSID);
            WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
            wifiProStateMachine2.logI("security switch enabled = " + enabled + ", current security value = " + security);
            if (!enabled || !WifiProCommonUtils.isWifiConnected(WifiProStateMachine.this.mWifiManager) || !(security == -1 || security == 1)) {
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, (boolean) WifiProStateMachine.DBG);
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiSemiAutoScoreState);
                return;
            }
            this.nextBSSID = WifiProCommonUtils.getCurrentBssid(WifiProStateMachine.this.mWifiManager);
            WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
            wifiProStateMachine4.logI("recv BQE level = " + level + ", start to query wifi security, ssid = " + StringUtilEx.safeDisplaySsid(this.nextSSID));
            WifiProStateMachine.this.mNetworkQosMonitor.queryWifiSecurity(this.nextSSID, this.nextBSSID);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT, 30000);
        }

        private void handleEvaluteCheck() {
            WifiProStateMachine.this.logW("wifi conenct, start check internet,  checkCounter =   " + this.checkCounter);
            updateApEvaluateEvent("apEvaluateTrigCnt");
            if (this.checkCounter == 0) {
                this.connectTime = (System.currentTimeMillis() - this.connectTime) / 1000;
                WifiProStateMachine.this.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + " background conenct Time =" + this.connectTime + " s");
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
            }
            this.checkTime = System.currentTimeMillis();
            this.checkCounter++;
            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen, WifiProStateMachine.DBG);
            this.isCheckRuning = WifiProStateMachine.DBG;
        }

        private void handleWifiDisconnectedInAutoScoreState(WifiInfo cInfo) {
            if (cInfo == null) {
                WifiProStateMachine.this.logI("EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED:cInfo is null.");
            } else if ((!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(cInfo.getSSID())) || "<unknown ssid>".equals(cInfo.getSSID())) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(22);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(this.nextSSID);
                WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.transitionTo(wifiProStateMachine.mWifiSemiAutoScoreState);
            }
        }

        private void handleWifiConnectedInAutoScoreState(WifiInfo cInfo) {
            if (cInfo == null) {
                WifiProStateMachine.this.logI("EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED:cInfo is null.");
                return;
            }
            String extssid = cInfo.getSSID();
            if (TextUtils.isEmpty(this.nextSSID) || TextUtils.isEmpty(extssid) || this.nextSSID.equals(extssid)) {
                WifiConfiguration wifiConfig = WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID);
                if (wifiConfig != null) {
                    int tag = Settings.Secure.getInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.WIFI_EVALUATE_TAG, -1);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    wifiProStateMachine.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + "is Connected, wifiConfig isTempCreated = " + wifiConfig.isTempCreated + ", Tag = " + tag);
                }
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI("receive connect msg ,ssid : " + StringUtilEx.safeDisplaySsid(extssid));
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(16);
                return;
            }
            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
            wifiProStateMachine3.logI("Connected other ap ,ssid : " + StringUtilEx.safeDisplaySsid(extssid));
            WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
            wifiProStateMachine4.transitionTo(wifiProStateMachine4.mWifiSemiAutoEvaluateState);
        }

        private void handleWifiStateChange(Message msg) {
            if (msg.obj instanceof Intent) {
                NetworkInfo networkInfo = null;
                Object objNetworkInfo = ((Intent) msg.obj).getParcelableExtra("networkInfo");
                if (objNetworkInfo instanceof NetworkInfo) {
                    networkInfo = (NetworkInfo) objNetworkInfo;
                }
                WifiInfo cInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                if (cInfo == null || networkInfo == null) {
                    WifiProStateMachine.this.logI("EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED:cInfo or networkInfo is null.");
                    return;
                }
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI(", nextSSID SSID = " + StringUtilEx.safeDisplaySsid(this.nextSSID) + ", networkInfo = " + networkInfo);
                if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                    handleWifiDisconnectedInAutoScoreState(cInfo);
                } else if (NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                    handleWifiConnectedInAutoScoreState(cInfo);
                } else if (NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                    String currssid = cInfo.getSSID();
                    if (!TextUtils.isEmpty(this.nextSSID) && !TextUtils.isEmpty(currssid) && !this.nextSSID.equals(currssid)) {
                        WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                        wifiProStateMachine2.logI("Connect other ap ,ssid : " + StringUtilEx.safeDisplaySsid(currssid));
                        WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                        wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiSemiAutoEvaluateState);
                    }
                }
            } else {
                WifiProStateMachine.this.logE("handleWifiStateChange: msg.obj is null or not intent");
            }
        }

        private void handleNetworkConfigChange(Message msg) {
            if (msg.obj instanceof Intent) {
                Intent confgIntent = (Intent) msg.obj;
                int changeReason = confgIntent.getIntExtra("changeReason", -1);
                WifiConfiguration connCfg = null;
                Object objConfig = confgIntent.getParcelableExtra("wifiConfiguration");
                if (objConfig instanceof WifiConfiguration) {
                    connCfg = (WifiConfiguration) objConfig;
                }
                if (connCfg == null) {
                    WifiProStateMachine.this.logE("handleNetworkConfigChange: connCfg is null");
                    return;
                }
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI(", nextSSID SSID = " + StringUtilEx.safeDisplaySsid(this.nextSSID) + ", conf " + StringUtilEx.safeDisplaySsid(connCfg.SSID));
                if (changeReason == 0) {
                    handleWifiConfgChange(changeReason, connCfg);
                } else if (changeReason == 2) {
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.logI("--- changeReason =change, change a ssid = " + StringUtilEx.safeDisplaySsid(connCfg.SSID) + ", status = " + connCfg.status + " isTempCreated " + connCfg.isTempCreated);
                    if (!connCfg.isTempCreated) {
                        if (!WifiProStateMachine.this.isWifiConnected()) {
                            WifiProStateMachine.this.logI("--- wifi has disconnect ----");
                            WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                            wifiProStateMachine3.transitionTo(wifiProStateMachine3.mWifiSemiAutoEvaluateState);
                            return;
                        }
                        if (!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(connCfg.SSID)) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.clearUntrustedOpenApList();
                            WifiProStateMachine.this.mWifiProConfigStore.resetTempCreatedConfig(connCfg);
                            if (connCfg.status == 1) {
                                WifiProStateMachine.this.mWifiManager.connect(connCfg, null);
                                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(18);
                            }
                        }
                        WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                        wifiProStateMachine4.transitionTo(wifiProStateMachine4.mWifiSemiAutoEvaluateState);
                    }
                }
            } else {
                WifiProStateMachine.this.logE("handleNetworkConfigChange: msg.obj is null or not intent");
            }
        }

        private void handleInternetCheckReusltInAutoScoreState(Message msg) {
            if (!TextUtils.isEmpty(this.nextSSID) && this.isCheckRuning) {
                this.checkTime = (System.currentTimeMillis() - this.checkTime) / 1000;
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + " checkTime = " + this.checkTime + " s");
                int result = msg.arg1;
                int type = handleWifiCheckResult(result);
                if (7 == result) {
                    if (this.checkCounter == 1) {
                        WifiProStateMachine.this.logI("internet check timeout ,check again");
                        this.checkTime = System.currentTimeMillis();
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
                        return;
                    }
                    type = 1;
                    WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(21);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(this.nextSSID);
                }
                WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                wifiProStateMachine2.logI(StringUtilEx.safeDisplaySsid(this.nextSSID) + " type = " + type);
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, type);
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, type, this.nextSSID);
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, (boolean) WifiProStateMachine.DBG);
                WifiProStateMachine wifiProStateMachine3 = WifiProStateMachine.this;
                wifiProStateMachine3.logI("clean evaluate ap :" + StringUtilEx.safeDisplaySsid(this.nextSSID));
                WifiProStateMachine wifiProStateMachine4 = WifiProStateMachine.this;
                wifiProStateMachine4.transitionTo(wifiProStateMachine4.mWifiSemiAutoScoreState);
            }
        }

        private void evaluteNextAP() {
            WifiProStateMachine.this.logI("start evalute next ap");
            if (WifiProStateMachine.this.mWiFiProEvaluateController.connectWifi(this.nextSSID)) {
                this.connectTime = System.currentTimeMillis();
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT, 35000);
                return;
            }
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBgApSsid(this.nextSSID);
            WifiProStateMachine.this.logI("background connect fail!");
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.transitionTo(wifiProStateMachine.mWifiSemiAutoScoreState);
        }

        private int handleWifiCheckResult(int result) {
            if (5 == result) {
                updateApEvaluateEvent("internetAPCnt");
                WifiProStateMachine.access$35508(WifiProStateMachine.this);
                WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification((boolean) WifiProStateMachine.DBG);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(3);
                return 4;
            } else if (6 == result) {
                updateApEvaluateEvent("portalAPCnt");
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(6);
                return 3;
            } else if (-1 != result) {
                return 0;
            } else {
                updateApEvaluateEvent("noInternetAPCnt");
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(5);
                return 2;
            }
        }

        private void updateApEvaluateEvent(String type) {
            if (WifiProStateMachine.this.uploadManager != null) {
                char c = 65535;
                switch (type.hashCode()) {
                    case -274122290:
                        if (type.equals("portalAPCnt")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 125451224:
                        if (type.equals("noInternetAPCnt")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 509736101:
                        if (type.equals("apEvaluateTrigCnt")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1281947673:
                        if (type.equals("internetAPCnt")) {
                            c = 0;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    WifiProStateMachine.this.uploadManager.addChrCntStat("apEvaluateEvent", "internetAPCnt");
                } else if (c == 1) {
                    WifiProStateMachine.this.uploadManager.addChrCntStat("apEvaluateEvent", "noInternetAPCnt");
                } else if (c == 2) {
                    WifiProStateMachine.this.uploadManager.addChrCntStat("apEvaluateEvent", "portalAPCnt");
                } else if (c != 3) {
                    WifiProStateMachine.this.logI("Unknown AP Evaluate Event type = " + type);
                } else {
                    WifiProStateMachine.this.uploadManager.addChrCntStat("apEvaluateEvent", "apEvaluateTrigCnt");
                }
            }
        }

        private void handleWifiConfgChange(int reason, WifiConfiguration connCfg) {
            if (connCfg != null && reason == 0) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.logI("add a new connCfg,isTempCreated : " + connCfg.isTempCreated);
                if (!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(connCfg.SSID) && connCfg.isTempCreated) {
                    WifiProStateMachine.this.mWiFiProEvaluateController.addUntrustedOpenApList(connCfg.SSID);
                } else if (!TextUtils.isEmpty(connCfg.SSID)) {
                    WifiProStateMachine.this.logI("system connecting ap,stop background evaluate");
                    WifiProStateMachine wifiProStateMachine2 = WifiProStateMachine.this;
                    wifiProStateMachine2.transitionTo(wifiProStateMachine2.mWifiSemiAutoEvaluateState);
                }
            }
        }

        private boolean isStopEvaluteNextAP() {
            if (!WifiProStateMachine.this.isAllowWiFiAutoEvaluate() || !TextUtils.isEmpty(WifiProStateMachine.this.mUserManualConnecConfigKey) || !WifiProStateMachine.this.mIsAllowEvaluate || WifiProCommonUtils.isWifiConnectedOrConnecting(WifiProStateMachine.this.mWifiManager)) {
                return WifiProStateMachine.DBG;
            }
            return false;
        }
    }

    public void notifyAppChanged() {
        sendMessage(EVENT_APP_CHANGED);
    }

    public static boolean isWifiEvaluating() {
        if (isWifiManualEvaluating || isWifiSemiAutoEvaluating) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSettingsActivity() {
        return WifiProCommonUtils.isQueryActivityMatched(this.mContext, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN);
    }

    public void setWifiApEvaluateEnabled(boolean enable) {
        logI("setWifiApEvaluateEnabled enabled " + enable);
        logI("system can not eavluate ap, ignor setting cmd");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiEvaluateTag(boolean evaluate) {
        logI("setWifiEvaluateTag Tag :" + evaluate);
        Settings.Secure.putInt(this.mContentResolver, WIFI_EVALUATE_TAG, evaluate ? 1 : 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean restoreWiFiConfig() {
        this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, false);
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        NetworkInfo wifiInfo = this.mConnectivityManager.getNetworkInfo(1);
        if (wifiInfo == null || wifiInfo.getDetailedState() != NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            return false;
        }
        this.mWifiManager.disconnect();
        return DBG;
    }

    @Override // com.huawei.hwwifiproservice.INetworkQosCallBack
    public synchronized void onNetworkQosChange(int type, int level, boolean updateUiOnly) {
        if (1 == type) {
            try {
                this.mCurrentWifiLevel = level;
                logI("onNetworkQosChange, currentWifiLevel == " + level + ", wifiNoInternet = " + this.mIsWiFiNoInternet + ", updateUiOnly = " + updateUiOnly);
                if (level == -103) {
                    this.mWifiHandoverStartTime = SystemClock.elapsedRealtime();
                }
                sendMessage(EVENT_WIFI_QOS_CHANGE, level, 0, Boolean.valueOf(updateUiOnly));
            } catch (Throwable th) {
                throw th;
            }
        } else if (type == 0) {
            sendMessage(EVENT_MOBILE_QOS_CHANGE, level);
        }
    }

    @Override // com.huawei.hwwifiproservice.INetworkQosCallBack
    public synchronized void onNetworkDetectionResult(int type, int level) {
        int levelVal = level;
        if (1 == type) {
            logI("wifi Detection level == " + levelVal);
            HwWifiProFeatureControl.getInstance();
            if (!HwWifiProFeatureControl.isSelfCureOngoing() || 5 == levelVal) {
                if ((5 == levelVal && this.mIsWiFiNoInternet) || this.mIsUserHandoverWiFi) {
                    logI("wifi no internet and recovered, notify SCE");
                    HwWifiProFeatureControl.getInstance();
                    HwWifiProFeatureControl.notifyInternetAccessRecovery();
                    reportNetworkConnectivity(DBG);
                    if (WIFI_HANDOVER_TYPES[6].equals(this.mChrWifiHandoverType)) {
                        uploadChrWifiHandoverTypeStatistics(WIFI_HANDOVER_TYPES[6], HANDOVER_SUCC_CNT);
                    }
                }
                if (-101 == levelVal) {
                    sendMessage(EVENT_WIFI_CHECK_UNKOWN, levelVal);
                } else if (!isWifiEvaluating()) {
                    if (7 == levelVal) {
                        levelVal = -1;
                    }
                    if (levelVal == -1 && levelVal != this.mLastWifiLevel && this.mIsUserManualConnectSuccess && !this.mIsWiFiProEnabled) {
                        updateChrToCell(false);
                        sendInvalidLinkDetected();
                    }
                    sendMessage(EVENT_DISPATCH_INTERNET_RESULT, levelVal);
                    sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, levelVal);
                } else {
                    sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, levelVal);
                }
            } else {
                logI("SelfCureOngoing, ignore wifi check result");
                if (this.isVerifyWifiNoInternetTimeOut || this.hasHandledNoInternetResult) {
                    this.isVerifyWifiNoInternetTimeOut = false;
                    this.hasHandledNoInternetResult = false;
                }
            }
        } else if (type == 0) {
            sendMessage(EVENT_CHECK_MOBILE_QOS_RESULT, levelVal);
        }
    }

    public void onPortalAuthCheckResult(int respCode) {
        int level = -1;
        if (respCode == 204) {
            level = 5;
        } else if (WifiProCommonUtils.isRedirectedRespCodeByGoogle(respCode)) {
            level = 6;
        } else if (respCode == 600) {
            level = 7;
        }
        sendMessage(EVENT_CHECK_PORTAL_AUTH_CHECK_RESULT, level);
    }

    @Override // com.huawei.hwwifiproservice.INetworksHandoverCallBack
    public synchronized void onWifiHandoverChange(int type, boolean result, String bssid, int errorReason) {
        if (1 == type) {
            if (result) {
                try {
                    this.mWifiProStatisticsManager.increaseWiFiHandoverWiFiCount(this.mWifiToWifiType);
                } catch (Throwable th) {
                    throw th;
                }
            }
            this.mNewSelect_bssid = bssid;
            sendMessage(EVENT_WIFI_HANDOVER_WIFI_RESULT, errorReason, -1, Boolean.valueOf(result));
        } else {
            if (4 == type) {
                this.mNewSelect_bssid = bssid;
                sendMessage(EVENT_DUALBAND_WIFI_HANDOVER_RESULT, errorReason, -1, Boolean.valueOf(result));
            }
            if (type == 2 && result) {
                this.mChrWifiHandoverType = WIFI_HANDOVER_TYPES[8];
                uploadChrWifiHandoverTypeStatistics(WIFI_HANDOVER_TYPES[8], HANDOVER_SUCC_CNT);
            }
        }
    }

    @Override // com.huawei.hwwifiproservice.IDualBandManagerCallback
    public void onDualBandNetWorkType(int type, List<HwDualBandMonitorInfo> apList, int count) {
        sendMessage(EVENT_DUALBAND_NETWROK_TYPE, type, -1, apList);
        if (type == 1 || type == 2) {
            uploadWifiDualBandTarget5gAp(count);
        }
    }

    @Override // com.huawei.hwwifiproservice.IDualBandManagerCallback
    public synchronized void onDualBandNetWorkFind(List<HwDualBandMonitorInfo> apList, int scanNum) {
        if (apList != null) {
            if (apList.size() != 0) {
                if (this.mDualBandMonitorStart) {
                    logI("onDualBandNetWorkFind  apList.size() = " + apList.size());
                    this.mDualBandMonitorStart = false;
                    this.mDualBandEstimateApList.clear();
                    this.mAvailable5GAPBssid = null;
                    this.mDualBandEstimateInfoSize = apList.size();
                    uploadWifiDualBandScanNum(scanNum);
                    for (HwDualBandMonitorInfo monitorInfo : apList) {
                        WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
                        apInfo.setApBssid(monitorInfo.mBssid);
                        apInfo.setEstimateApSsid(monitorInfo.mSsid);
                        apInfo.setApAuthType(monitorInfo.mAuthType);
                        apInfo.setApRssi(monitorInfo.mCurrentRssi);
                        apInfo.setDualbandAPType(monitorInfo.mIsDualbandAp);
                        this.mDualBandEstimateApList.add(apInfo);
                        this.mNetworkQosMonitor.getApHistoryQualityScore(apInfo);
                        uploadDualbandAlgorithmicInfo(apInfo, scanNum);
                    }
                    refreshConnectedNetWork();
                    this.mDualBandEstimateInfoSize++;
                    WifiProEstimateApInfo currentApInfo = new WifiProEstimateApInfo();
                    currentApInfo.setApBssid(this.mCurrentBssid);
                    currentApInfo.setEstimateApSsid(this.mCurrentSsid);
                    currentApInfo.setApRssi(this.mCurrentRssi);
                    currentApInfo.set5GAP(false);
                    this.mDualBandEstimateApList.add(currentApInfo);
                    this.mNetworkQosMonitor.getApHistoryQualityScore(currentApInfo);
                    return;
                }
            }
        }
        loge("onDualBandNetWorkFind apList null error or mDualBandMonitorStart = " + this.mDualBandMonitorStart);
    }

    @Override // com.huawei.hwwifiproservice.INetworkQosCallBack
    public synchronized void onWifiBqeReturnRssiTH(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            loge("onWifiBqeReturnRssiTH apInfo null error");
        } else {
            sendMessage(EVENT_DUALBAND_RSSITH_RESULT, apInfo);
        }
    }

    @Override // com.huawei.hwwifiproservice.INetworkQosCallBack
    public synchronized void onWifiBqeReturnHistoryScore(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            loge("onWifiBqeReturnHistoryScore apInfo null error");
        } else {
            sendMessage(EVENT_DUALBAND_SCORE_RESULT, apInfo);
        }
    }

    @Override // com.huawei.hwwifiproservice.INetworkQosCallBack
    public synchronized void onWifiBqeReturnCurrentRssi(int rssi) {
        if (this.mDualBandManager != null) {
            this.mDualBandManager.updateCurrentRssi(rssi);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void retryDualBandAPMonitor() {
        this.mDualBandMonitorInfoSize = this.mDualBandMonitorApList.size();
        if (this.mDualBandMonitorInfoSize == 0) {
            loge("retry dual band monitor error, monitorinfo size is zero");
            return;
        }
        Iterator<HwDualBandMonitorInfo> it = this.mDualBandMonitorApList.iterator();
        while (it.hasNext()) {
            HwDualBandMonitorInfo monitorInfo = it.next();
            WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
            apInfo.setApBssid(monitorInfo.mBssid);
            apInfo.setApRssi(monitorInfo.mCurrentRssi);
            this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetWifiTcpRx() {
        int currentWifiTcpRxCount = this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
        int tcpRxThreshold = 3;
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null && !powerManager.isScreenOn()) {
            tcpRxThreshold = 5;
        }
        int increasedRxCount = currentWifiTcpRxCount - this.mWifiTcpRxCount;
        if (increasedRxCount > tcpRxThreshold) {
            logI("to query network Qos, tcpRxThreshold is " + tcpRxThreshold + ", increasedRxCount is " + increasedRxCount);
            String[] strArr = WIFI_HANDOVER_TYPES;
            this.mChrWifiHandoverType = strArr[6];
            uploadChrWifiHandoverTypeStatistics(strArr[6], HANDOVER_CNT);
            this.mNetworkQosMonitor.queryNetworkQos(1, this.mIsPortalAp, this.mIsNetworkAuthen, false);
            return;
        }
        if (getHandler().hasMessages(EVENT_GET_WIFI_TCPRX)) {
            removeMessages(EVENT_GET_WIFI_TCPRX);
        }
        sendMessageDelayed(EVENT_GET_WIFI_TCPRX, 5000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDualBandMonitorInfo(WifiProEstimateApInfo apInfo) {
        Iterator<HwDualBandMonitorInfo> it = this.mDualBandMonitorApList.iterator();
        while (it.hasNext()) {
            HwDualBandMonitorInfo monitorInfo = it.next();
            String bssid = monitorInfo.mBssid;
            if (bssid != null && apInfo != null && bssid.equals(apInfo.getApBssid())) {
                monitorInfo.mTargetRssi = apInfo.getRetRssiTH();
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDualBandEstimateInfo(WifiProEstimateApInfo apInfo) {
        Iterator<WifiProEstimateApInfo> it = this.mDualBandEstimateApList.iterator();
        while (it.hasNext()) {
            WifiProEstimateApInfo estimateApInfo = it.next();
            String bssid = estimateApInfo.getApBssid();
            if (bssid != null && apInfo != null && bssid.equals(apInfo.getApBssid())) {
                estimateApInfo.setRetHistoryScore(apInfo.getRetHistoryScore());
                return;
            }
        }
    }

    private void showNoInternetDialog(int reason) {
        if (1 == reason) {
            this.mWifiProUIDisplayManager.showWifiProDialog(5);
        } else if (reason == 0) {
            this.mWifiProUIDisplayManager.showWifiProDialog(4);
        } else if (3 == reason) {
            this.mWifiProUIDisplayManager.showWifiProDialog(6);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void chooseAvalibleDualBandAp() {
        logI("chooseAvalibleDualBandAp DualBandEstimateApList =" + this.mDualBandEstimateApList.toString());
        if (this.mDualBandEstimateApList.size() == 0 || this.mCurrentBssid == null) {
            Log.e(TAG, "chooseAvalibleDualBandAp ap size error");
            return;
        }
        this.mAvailable5GAPBssid = null;
        this.mAvailable5GAPSsid = null;
        this.mAvailable5GAPAuthType = 0;
        this.mDuanBandHandoverType = 0;
        WifiProEstimateApInfo bestAp = new WifiProEstimateApInfo();
        int currentApScore = 0;
        Iterator<WifiProEstimateApInfo> it = this.mDualBandEstimateApList.iterator();
        while (it.hasNext()) {
            WifiProEstimateApInfo apInfo = it.next();
            if (this.mCurrentBssid.equals(apInfo.getApBssid())) {
                currentApScore = apInfo.getRetHistoryScore();
            } else if (apInfo.getRetHistoryScore() > bestAp.getRetHistoryScore()) {
                bestAp = apInfo;
            }
        }
        logI("chooseAvalibleDualBandAp bestAp =" + bestAp.toString() + ", currentApScore =" + currentApScore);
        WifiInfo wifiInfo = this.mCurrWifiInfo;
        if (wifiInfo == null) {
            logI("chooseAvalibleDualBandAp mCurrWifiInfo is null");
            return;
        }
        if (this.mWiFiProEvaluateController.calculateSignalLevelHW(bestAp.is5GAP(), bestAp.getApRssi()) < WifiProCommonUtils.getCurrenSignalLevel(wifiInfo)) {
            logI("chooseAvalibleDualBandAp bestAp signalLevel is lower than current");
            uploadWifiDualBandFailReason(3);
            return;
        }
        handleScoreInfo(bestAp, currentApScore);
        chooseAvalible5GBand(bestAp);
    }

    private void handleScoreInfo(WifiProEstimateApInfo bestAp, int currentApScore) {
        if (bestAp != null) {
            int dualBandReason = 0;
            int score = bestAp.getRetHistoryScore();
            boolean isSameApReason = DBG;
            this.mIsChrQosBetterAfterDualbandHandover = score > currentApScore;
            if (score < 40 || bestAp.getApRssi() < -70) {
                boolean isSameAp = HwDualBandRelationManager.isDualBandAP(this.mCurrentBssid, bestAp.getApBssid());
                logI("isSameApHandOver = " + isSameAp);
                boolean isDiffReason = score >= currentApScore + 5 && bestAp.getApRssi() >= -70;
                if (!isSameAp || bestAp.getApRssi() < -65) {
                    isSameApReason = false;
                }
                if (isDiffReason || isSameApReason) {
                    setTarget5gApInfo(bestAp);
                    dualBandReason = isDiffReason ? 2 : 3;
                }
            } else {
                setTarget5gApInfo(bestAp);
                dualBandReason = 1;
            }
            uploadWifiDualBandInfo(dualBandReason);
        }
    }

    private void setTarget5gApInfo(WifiProEstimateApInfo targetAp) {
        if (targetAp != null) {
            this.mAvailable5GAPBssid = targetAp.getApBssid();
            this.mAvailable5GAPSsid = targetAp.getApSsid();
            this.mAvailable5GAPAuthType = targetAp.getApAuthType();
        }
    }

    private void chooseAvalible5GBand(WifiProEstimateApInfo bestAp) {
        HwDualBandManager hwDualBandManager;
        String str = this.mAvailable5GAPBssid;
        if (str == null) {
            List<HwDualBandMonitorInfo> mDualBandDeleteList = new ArrayList<>();
            Iterator<HwDualBandMonitorInfo> it = this.mDualBandMonitorApList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HwDualBandMonitorInfo monitorInfo = it.next();
                String bssid = monitorInfo.mBssid;
                if (bssid != null && bssid.equals(bestAp.getApBssid()) && monitorInfo.mTargetRssi < -45) {
                    monitorInfo.mTargetRssi += 10;
                    break;
                } else if (monitorInfo.mCurrentRssi >= -45) {
                    mDualBandDeleteList.add(monitorInfo);
                }
            }
            if (mDualBandDeleteList.size() > 0) {
                int dualBandDeleteListSize = mDualBandDeleteList.size();
                for (int i = 0; i < dualBandDeleteListSize; i++) {
                    logI("remove mix AP for RSSI > -45 DB RSSi = " + mDualBandDeleteList.get(i).mSsid);
                    this.mDualBandMonitorApList.remove(mDualBandDeleteList.get(i));
                }
            }
            if (!(this.mDualBandMonitorApList.size() == 0 || (hwDualBandManager = this.mDualBandManager) == null)) {
                this.mDualBandMonitorStart = DBG;
                hwDualBandManager.startMonitor(this.mDualBandMonitorApList);
            }
        } else if (this.mHwDualBandBlackListMgr.isInWifiBlacklist(str) || this.mNetworkBlackListManager.isInWifiBlacklist(this.mAvailable5GAPBssid)) {
            long expiretime = this.mHwDualBandBlackListMgr.getExpireTimeForRetry(this.mAvailable5GAPBssid);
            logI("getExpireTimeForRetry for bssid " + StringUtilEx.safeDisplayBssid(this.mAvailable5GAPBssid) + ", time =" + expiretime);
            uploadWifiDualBandFailReason(2);
            sendMessageDelayed(EVENT_DUALBAND_DELAY_RETRY, expiretime);
        } else {
            logI("do dualband handover : " + bestAp.toString());
            sendMessage(EVENT_DUALBAND_5GAP_AVAILABLE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addDualBandBlackList(String bssid) {
        String str;
        logI("addDualBandBlackList bssid = " + StringUtilEx.safeDisplayBssid(bssid) + ", mDualBandConnectApBssid = " + StringUtilEx.safeDisplayBssid(this.mDualBandConnectApBssid));
        if (bssid == null || (str = this.mDualBandConnectApBssid) == null || !str.equals(bssid)) {
            logI("addDualBandBlackList do nothing");
            return;
        }
        this.mDualBandConnectApBssid = null;
        if (System.currentTimeMillis() - this.mDualBandConnectTime > 1800000) {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(bssid, DBG);
        } else {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(bssid, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDualBandManager() {
        HwDualBandManager hwDualBandManager = this.mDualBandManager;
        if (hwDualBandManager != null) {
            hwDualBandManager.startDualBandManger();
        } else {
            logE("ro.config.hw_wifipro_dualband is false, do nothing");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDualBandManager() {
        if (this.mDualBandManager != null) {
            stopDualBandMonitor();
            this.mDualBandManager.stopDualBandManger();
            return;
        }
        logE("ro.config.hw_wifipro_dualband is false, do nothing");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopDualBandMonitor() {
        HwDualBandManager hwDualBandManager;
        if (this.mDualBandMonitorStart && (hwDualBandManager = this.mDualBandManager) != null) {
            this.mDualBandMonitorStart = false;
            hwDualBandManager.stopMonitor();
        }
    }

    public int getNetwoksHandoverType() {
        return this.mWifiHandover.getNetwoksHandoverType();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkCheckingStatus(String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyNetworkCheckResult(int result) {
        WifiConfiguration wifiConfiguration;
        int internetLevel = result;
        if (internetLevel == 5 && (wifiConfiguration = this.mCurrentWifiConfig) != null && WifiProCommonUtils.matchedRequestByHistory(wifiConfiguration.internetHistory, 102)) {
            internetLevel = 6;
        }
        sendNetworkCheckingStatus("huawei.conn.NETWORK_CONDITIONS_MEASURED", "extra_is_internet_ready", internetLevel);
    }

    @Override // com.huawei.hwwifiproservice.INetworksHandoverCallBack
    public void onWifiConnected(boolean result, int reason) {
    }

    private boolean isSatisfiedCurrentApNoInternetAndExistStrongAp(int targetRssiLevel) {
        if (!this.mIsWiFiNoInternet || targetRssiLevel < 3 || getCurrentState() != this.mWifiConnectedState) {
            return false;
        }
        return DBG;
    }

    @Override // com.huawei.hwwifiproservice.INetworksHandoverCallBack
    public void onCheckAvailableWifi(boolean exist, int bestRssi, String targetBssid, int preferType, int freq) {
        boolean flag = exist;
        if (!isKeepCurrWiFiConnected()) {
            int rssilevel = WifiProCommonUtils.getCurrenSignalLevel(this.mWifiManager.getConnectionInfo());
            int targetRssiLevel = WifiProCommonUtils.getSignalLevel(freq, bestRssi);
            if (flag && this.mNetworkBlackListManager.isInWifiBlacklist(targetBssid) && (targetRssiLevel <= 3 || targetRssiLevel - rssilevel < 2)) {
                logI("onCheckAvailableWifi, current ap is in WifiBlacklist and mIsWiFiNoInternet = " + this.mIsWiFiNoInternet + ", targetRssiLevel = " + targetRssiLevel + ", currentState = " + getCurrentState().getName());
                if (!isSatisfiedCurrentApNoInternetAndExistStrongAp(targetRssiLevel)) {
                    logW("onCheckAvailableWifi, but wifi blacklists contain it, ignore the result.");
                    flag = false;
                }
            }
            logI("EVENT_CHECK_AVAILABLE_AP_RESULT: targetBssid = " + StringUtilEx.safeDisplayBssid(targetBssid) + ", exist = " + flag + ", prefer = " + preferType + ", freq = " + freq);
            sendMessage(EVENT_CHECK_AVAILABLE_AP_RESULT, targetRssiLevel, preferType, Boolean.valueOf(flag));
        }
    }

    @Override // com.huawei.hwwifiproservice.INetworkQosCallBack
    public void onWifiBqeDetectionResult(int result) {
        logI("onWifiBqeDetectionResult =  " + result);
        sendMessage(EVENT_WIFI_EVALUTE_TCPRTT_RESULT, result);
    }

    @Override // com.huawei.hwwifiproservice.INetworkQosCallBack
    public void onNotifyWifiSecurityStatus(Bundle bundle) {
        logI("onNotifyWifiSecurityStatus, bundle =  " + bundle);
        sendMessage(EVENT_WIFI_SECURITY_RESPONSE, bundle);
    }

    public synchronized void onUserConfirm(int type, int status) {
        if (2 == status) {
            try {
                logI("UserConfirm  is OK ");
                sendMessage(EVENT_DIALOG_OK, type, -1);
            } catch (Throwable th) {
                throw th;
            }
        } else if (1 == status) {
            logI("UserConfirm  is CANCEL");
            sendMessage(EVENT_DIALOG_CANCEL, type, -1);
        }
    }

    public synchronized void userHandoverWifi() {
        logI("User Chose Rove In WiFi");
        sendMessage(EVENT_USER_ROVE_IN);
    }

    public void notifyHttpReachable(boolean isReachable) {
        if (isReachable || this.mPowerManager.isScreenOn()) {
            logI("SEC notifyHttpReachable " + isReachable);
            this.mNetworkQosMonitor.syncNotifyPowerSaveGenie(isReachable, 100, false);
        } else {
            logI("do not notify the PowerSaveGenie when the internet is unreachable becasue the screen is off ");
        }
        sendMessage(EVENT_HTTP_REACHABLE_RESULT, Boolean.valueOf(isReachable));
    }

    public void notifyHttpRedirectedForWifiPro() {
        logI("notifyHttpRedirectedForWifiPro");
        onNetworkDetectionResult(1, 6);
    }

    public void notifyRenewDhcpTimeoutForWifiPro() {
        if (getCurrentState() == this.mWiFiProVerfyingLinkState) {
            logI("current state is verfying link state, not need notify cs wifi validation failed");
            return;
        }
        logI("notifyRenewDhcpTimeoutForWifiPro");
        this.mIsWiFiNoInternet = DBG;
        sendInvalidLinkDetected();
        sendMessage(EVENT_DISPATCH_INTERNET_RESULT, -1);
        if (getHandler().hasMessages(EVENT_GET_WIFI_TCPRX)) {
            removeMessages(EVENT_GET_WIFI_TCPRX);
        }
        sendMessageDelayed(EVENT_GET_WIFI_TCPRX, 5000);
    }

    public void notifyWifiLinkPoor(boolean poorLink, int reason) {
        WifiInfo wifiInfo;
        logI("notifyWifiLinkPoor notifyWifiLinkPoor = " + poorLink + " reason=" + reason);
        handleWifi2CellStartChr(poorLink, reason);
        if (!isKeepCurrWiFiConnected()) {
            if (poorLink) {
                if (reason != 107 || (!this.mIsWiFiNoInternet && getCurrentState() != this.mWiFiProVerfyingLinkState)) {
                    this.mHandoverReason = reason;
                    this.mNotifyWifiLinkPoorReason = reason;
                    this.mWifiHandoverStartTime = SystemClock.elapsedRealtime();
                    this.mHandoverCellStartTime = this.mWifiHandoverStartTime;
                    WifiManager wifiManager = this.mWifiManager;
                    if (wifiManager != null && (wifiInfo = wifiManager.getConnectionInfo()) != null) {
                        if (WifiProCommonUtils.getCurrenSignalLevel(wifiInfo) >= 3) {
                            this.mWifiSwitchReason = 3;
                        } else {
                            this.mWifiSwitchReason = 4;
                        }
                        sendMessage(EVENT_NOTIFY_WIFI_LINK_POOR, false);
                        return;
                    }
                    return;
                }
                logI("Wifi is nointernet or enter VerfyingLinkState already, let NOINTERNET2WIFI to handle.");
            } else if (getCurrentState() == this.mWiFiProVerfyingLinkState) {
                onNetworkQosChange(1, 3, false);
            }
        } else if (hasMessages(EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG)) {
            handleWifi2CellFailChr(getWifi2CellFailReason());
        }
    }

    public void notifyTelephonySignalStrength(int slotId, int rsrp, int signalLevel) {
        if (slotId == WifiProCommonUtils.getMasterCardSlotId()) {
            this.mMasterRsrp = rsrp;
            this.mMasterSignalLevel = signalLevel;
        }
        if (slotId == WifiProCommonUtils.getSlaveCardSlotId()) {
            this.mSlaveRsrp = rsrp;
            this.mSlaveSignalLevel = signalLevel;
        }
    }

    public void notifyRoamingCompleted(String newBssid) {
        if (newBssid != null && getCurrentState() == this.mWiFiProVerfyingLinkState) {
            sendMessageDelayed(EVENT_LAA_STATUS_CHANGED, 3000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logI(String info) {
        Log.i(TAG, info);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logD(String info) {
        Log.d(TAG, info);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logW(String info) {
        Log.w(TAG, info);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logE(String info) {
        Log.e(TAG, info);
    }

    public static void resetParameter() {
        isWifiManualEvaluating = false;
        isWifiSemiAutoEvaluating = false;
    }

    public void onDisableWiFiPro() {
        logI("WiFiProDisabledState is Enter");
        resetParameter();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mWifiProUIDisplayManager.shownAccessNotification(false);
        this.mWiFiProEvaluateController.cleanEvaluateRecords();
        HwIntelligenceWiFiManager hwIntelligenceWiFiManager = this.mHwIntelligenceWiFiManager;
        if (hwIntelligenceWiFiManager != null) {
            hwIntelligenceWiFiManager.stop();
        }
        stopDualBandManager();
        if (isWifiConnected()) {
            logI("WiFiProDisabledState , wifi is connect ");
            WifiInfo cInfo = this.mWifiManager.getConnectionInfo();
            if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && NetworkInfo.DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                logI("wifi State == VERIFYING_POOR_LINK");
                this.mWsmChannel.sendMessage(131874);
            }
            setWifiCSPState(1);
        }
        diableResetVariables();
        disableTransitionNetState();
        if (this.mIsPrimaryUser) {
            uploadWifiproDisabledStatistics();
        }
    }

    private void diableResetVariables() {
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWiFiProPdpSwichValue = 0;
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = 0;
        this.mIsUserHandoverWiFi = false;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = false;
        resetWifiProManualConnect();
        stopDualBandMonitor();
    }

    private void disableTransitionNetState() {
        if (isWifiConnected()) {
            logI("onDisableWiFiPro,go to WifiConnectedState");
            this.mNetworkQosMonitor.queryNetworkQos(1, this.mIsPortalAp, this.mIsNetworkAuthen, false);
            transitionTo(this.mWifiConnectedState);
            return;
        }
        logI("onDisableWiFiPro, go to mWifiDisConnectedState");
        transitionTo(this.mWifiDisConnectedState);
    }

    private void registerMapNavigatingStateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(MAPS_LOCATION_FLAG), false, this.mMapNavigatingStateChangeObserver);
    }

    private void registerVehicleStateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(VEHICLE_STATE_FLAG), false, this.mVehicleStateChangeObserver);
    }

    private boolean isMpLinkStarted() {
        EmcomManager emcomManager = EmcomManager.getInstance();
        if (emcomManager == null) {
            return false;
        }
        return emcomManager.hasAppInMultiPath();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiMonitorEnabled(boolean enabled) {
        logI("setWifiLinkDataMonitorEnabled  is " + enabled);
        this.mNetworkQosMonitor.setMonitorWifiQos(1, enabled);
        this.mNetworkQosMonitor.setIpQosEnabled(enabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFullscreen() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 9, new Bundle());
        if (result != null) {
            return result.getBoolean("isFullscreen");
        }
        return false;
    }

    public Bundle getWifiDisplayInfo(NetworkInfo networkInfo) {
        Bundle result = new Bundle();
        boolean isLinkMonitorState = false;
        result.putBoolean("result", false);
        if (networkInfo == null || this.mCurrentSsid == null) {
            return result;
        }
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            result.putBoolean("result", DBG);
        }
        boolean isDelayState = false;
        if (getCurrentState() == this.mWiFiLinkMonitorState) {
            isLinkMonitorState = true;
        }
        boolean hasDelayEvent = hasMessages(EVENT_DISPATCH_INTERNET_RESULT);
        if (isLinkMonitorState && this.mWiFiLinkMonitorState.isWifiHandoverMobileToastShowed && hasDelayEvent) {
            isDelayState = DBG;
            logI("inform SystemUI wifi arrow set to gray during the delay period");
        }
        result.putBoolean("delayState", isDelayState);
        result.putString("ssid", this.mCurrentSsid);
        return result;
    }

    public void sendInternetCheckRequest() {
        logI("sendInternetCheckRequest");
        sendMessage(EVENT_WIFI_QOS_CHANGE, -1, 0, false);
    }

    public void notifyNetworkUserConnect(boolean isUserConnect) {
        logI("notifyNetworkUserConnect: isUserConnect = " + isUserConnect);
        sendMessage(EVENT_NETWORK_USER_CONNECT, Boolean.valueOf(isUserConnect));
    }

    public void notifyApkChangeWifiStatus(boolean enable, String packageName) {
        if (enable) {
            this.mCloseBySystemui = false;
        } else if (packageName.equals("com.android.systemui")) {
            this.mCloseBySystemui = DBG;
        }
    }

    public void notifyWifiDisconnected(Intent intent) {
        logI("notifyWifiDisconnected:EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED");
        sendMessage(EVENT_WIFI_DISCONNECTED_TO_DISCONNECTED, intent);
    }

    public void uploadWifiproDisabledStatistics() {
        long currentTimeMillis = SystemClock.elapsedRealtime();
        int topUid = -1;
        String pktName = "";
        HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
        if (autoConnectManager != null) {
            topUid = autoConnectManager.getCurrentTopUid();
            pktName = autoConnectManager.getCurrentPackageName();
            if (pktName != null && pktName.equals("com.huawei.hwstartupguide")) {
                this.mIsWifiproDisableOnReboot = false;
            }
        }
        if (topUid != -1 && pktName != null && !this.mIsWifiproDisableOnReboot) {
            long j = this.mLastWifiproDisableTime;
            if (j == 0 || currentTimeMillis - j > 7200000) {
                this.mLastWifiproDisableTime = currentTimeMillis;
                Bundle data = new Bundle();
                if (pktName.equals(PACKAGE_NAME_SETTINGS)) {
                    data.putInt("appType", 101);
                    logI("appType == com.android.settings");
                } else if (pktName.equals("com.huawei.hwstartupguide")) {
                    data.putInt("appType", 102);
                    logI("appType == com.huawei.hwstartupguide");
                } else {
                    data.putInt("appType", 103);
                    logI("appType == 103");
                }
                Bundle dftEventData = new Bundle();
                dftEventData.putInt(EVENT_ID, 909002066);
                dftEventData.putBundle(EVENT_DATA, data);
                WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
            }
        }
    }

    public void uploadPortalAuthExpirationStatistics(boolean isNotificationClicked) {
        int validityDura = 0;
        int connDura = 0;
        WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
        if (wifiConfiguration != null && wifiConfiguration.portalValidityDuration < 86400000) {
            validityDura = (int) this.mCurrentWifiConfig.portalValidityDuration;
        }
        if (System.currentTimeMillis() - this.connectStartTime < 86400000) {
            connDura = (int) (System.currentTimeMillis() - this.connectStartTime);
        }
        logI("upload portal chr");
        Bundle data = new Bundle();
        data.putInt("dura", validityDura);
        data.putInt("isPeriodicDet", this.isPeriodicDet ? 1 : 0);
        data.putString("respCode", this.respCodeChrInfo);
        data.putInt("detNum", this.detectionNumSlow);
        data.putInt("connDura", connDura);
        data.putInt("isNotificationClicked", isNotificationClicked ? 1 : 0);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, 909009072);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        this.respCodeChrInfo = "";
        this.detectionNumSlow = 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldUploadCloseWifiEvent() {
        if (this.mIsWiFiNoInternet || WifiProCommonUtils.getAirplaneModeOn(this.mContext) || this.mWifiManager.isWifiApEnabled()) {
            return false;
        }
        long deltaTime = System.currentTimeMillis() - this.mLastDisconnectedTimeStamp;
        if ((getCurrentState() != this.mWifiDisConnectedState && this.mCurrentRssi >= -75) || ((getCurrentState() == this.mWifiDisConnectedState && deltaTime > 10000) || this.mLastDisconnectedRssi >= -75)) {
            return false;
        }
        String pktName = "";
        HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
        if (autoConnectManager != null) {
            pktName = autoConnectManager.getCurrentPackageName();
        }
        if (PACKAGE_NAME_SETTINGS.equals(pktName) || this.mCloseBySystemui) {
            return DBG;
        }
        return false;
    }

    public void setLastDisconnectNetwork() {
        this.mLastConnectedSsid = this.mCurrentSsid;
        this.mLastDisconnectedTimeStamp = System.currentTimeMillis();
        this.mLastDisconnectedRssi = this.mCurrentRssi;
    }

    /* access modifiers changed from: private */
    public class WifiProPhoneStateListener extends PhoneStateListener {
        private WifiProPhoneStateListener() {
        }

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CALL_STATE_CHANGED, state, -1);
        }
    }

    public void notifyWifiProConnect() {
        logI("notifyWifiProConnect");
        this.mNetworkQosMonitor.resetMonitorStatus();
        sendMessage(EVENT_WIFI_FIRST_CONNECTED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkSpeedLimit(Bundle bundle) {
        if (bundle == null) {
            HwHiLog.e(TAG, false, "input bundle null.", new Object[0]);
        } else if (bundle.getInt("modemId", -1) != 0) {
            HwHiLog.d(TAG, false, "At present, the main card is only concerned.", new Object[0]);
        } else {
            int downLink = bundle.getInt("downLink");
            HwHiLog.d(TAG, false, "downLink = %{public}d, DOWNLINK_LIMIT = %{public}d", new Object[]{Integer.valueOf(downLink), Integer.valueOf(DOWNLINK_LIMIT)});
            if (downLink <= DOWNLINK_LIMIT) {
                HwHiLog.d(TAG, false, "show limit notify", new Object[0]);
                this.mIsLimitedSpeed = DBG;
                return;
            }
            HwHiLog.d(TAG, false, "show no limit notify", new Object[0]);
            this.mIsLimitedSpeed = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSmartDataSavingSwitchOff() {
        if (this.mConnectivityManager.getRestrictBackgroundStatus() == 1) {
            return DBG;
        }
        return false;
    }

    private boolean isNeedToSwitch2Cell() {
        if (this.mIsLimitedSpeed) {
            Log.i(TAG, "Cell Network speed is limited.");
            return false;
        } else if (isSmartDataSavingSwitchOff()) {
            return DBG;
        } else {
            Log.i(TAG, "SmartDataSavingSwitch is on");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerBoosterService() {
        logI("registerBoosterService enter");
        IHwCommBoosterServiceManager hwCommBoosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (hwCommBoosterServiceManager != null) {
            int ret = hwCommBoosterServiceManager.registerCallBack("com.huawei.hwwifiproservice", this.mHwCommBoosterCallback);
            if (ret != 0) {
                logE("registerCallBack failed, ret=" + ret);
            }
            logI("registerBoosterService register callback success");
        } else {
            logE("HwCommBoosterServiceManager is null");
        }
        if (hasMessages(EVENT_REGISTER_APP_QOE)) {
            logI("WiFi construct and booster start broadcast conflict, ignore");
        } else {
            this.mRegisterAppQoeTime = 0;
            registerBoosterNetworkQoe();
        }
        NetworkRecommendManager.getInstance().registerBoosterService();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDsdsStateChanged(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            logE("handleDsdsStateChanged. intent is null");
            return;
        }
        this.mDsdsState = intent.getIntExtra(DSDS_KEY, 0);
        logI("handleDsdsStateChanged, mDsdsState = " + this.mDsdsState);
    }

    private class NetworkCheckThread extends Thread {
        private boolean mIsPortalNetwork = false;
        private boolean mIsWifiBackground = false;

        public NetworkCheckThread(boolean isPortal, boolean isWifiBackground) {
            super.setName("NetworkCheckThread");
            this.mIsPortalNetwork = isPortal;
            this.mIsWifiBackground = isWifiBackground;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            synchronized (WifiProStateMachine.this.mNetworkCheckLock) {
                String startBssid = WifiProCommonUtils.getCurrentBssid(WifiProStateMachine.this.mWifiManager);
                int respCode = WifiProStateMachine.this.mNetworkPropertyChecker.isCaptivePortal((boolean) WifiProStateMachine.DBG, this.mIsPortalNetwork, this.mIsWifiBackground);
                String endBssid = WifiProCommonUtils.getCurrentBssid(WifiProStateMachine.this.mWifiManager);
                if (startBssid != null && startBssid.equals(endBssid)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_NETWORK_RESULT, respCode, (int) WifiProStateMachine.this.mNetworkPropertyChecker.getDetectTime());
                }
            }
        }
    }

    public void uploadWifiSwitchFailTypeStatistics(int reason) {
        if (reason == 201) {
            this.mHandoverFailReason = 12;
        } else if (reason == 202) {
            this.mHandoverFailReason = 9;
        } else if (reason == 203) {
            this.mHandoverFailReason = 5;
        } else if (reason == 204) {
            this.mHandoverFailReason = 11;
        } else {
            this.mHandoverFailReason = reason;
        }
        logI("mHandoverFailReason = " + this.mHandoverFailReason);
        int i = this.mHandoverFailReason;
        if (i >= 0 && i < WIFI_HANDOVER_FAIL_TYPES.length) {
            Bundle data = new Bundle();
            data.putString(HANDOVER_TYPE, WIFI_HANDOVER_FAIL_TYPES[this.mHandoverFailReason]);
            data.putInt(HANDOVER_CNT, 1);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt(EVENT_ID, ID_WIFI_HANDOVER_FAIL_INFO);
            dftEventData.putBundle(EVENT_DATA, data);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }
    }

    public void setChrWifiDisconnectedReason() {
        this.mHandoverReason = 4;
    }

    public void uploadWifiSwitchStatistics(boolean isWifiNetworkPoor) {
        String connTimeDes;
        Bundle data = new Bundle();
        data.putInt(HANDOVER_CNT, 1);
        int i = this.mHandoverReason;
        if (i >= 205) {
            this.mHandoverReason = i - HwWifiConnectivityMonitor.REASON_SIGNAL_LSSS_LEVEL_1_TOP_UID_BAD;
        }
        if (this.mHandoverReason == 107) {
            this.mHandoverReason = 5;
        }
        int i2 = this.mHandoverReason;
        if (i2 >= 0) {
            String[] strArr = WIFI_HANDOVER_CAUSE_TYPES;
            if (i2 < strArr.length && isWifiNetworkPoor) {
                data.putString(WIFI_SWITCH_REASON, strArr[i2]);
                if (this.mHandoverReason != 4 || this.mWifiHandoverStartTime == 0) {
                    data.putString(WIFI_SWITCH_TIME_LEVEL, "");
                } else {
                    int connDura = (int) (SystemClock.elapsedRealtime() - this.mWifiHandoverStartTime);
                    int[] iArr = NORMAL_DURATION_INTERVAL;
                    if (connDura < iArr[0]) {
                        connTimeDes = WIFI_HANDOVER_DURAS[0];
                    } else if (connDura < iArr[1]) {
                        connTimeDes = WIFI_HANDOVER_DURAS[1];
                    } else if (connDura < iArr[2]) {
                        connTimeDes = WIFI_HANDOVER_DURAS[2];
                    } else {
                        connTimeDes = WIFI_HANDOVER_DURAS[3];
                    }
                    data.putString(WIFI_SWITCH_TIME_LEVEL, connTimeDes);
                }
                Bundle dftEventData = new Bundle();
                dftEventData.putInt(EVENT_ID, ID_WIFI_HANDOVER_REASON_INFO);
                dftEventData.putBundle(EVENT_DATA, data);
                WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
                this.mWifiHandoverStartTime = 0;
                this.mHandoverReason = -1;
            }
        }
        data.putString(WIFI_SWITCH_REASON, "");
        if (this.mHandoverReason != 4) {
        }
        data.putString(WIFI_SWITCH_TIME_LEVEL, "");
        Bundle dftEventData2 = new Bundle();
        dftEventData2.putInt(EVENT_ID, ID_WIFI_HANDOVER_REASON_INFO);
        dftEventData2.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData2);
        this.mWifiHandoverStartTime = 0;
        this.mHandoverReason = -1;
    }

    private void uploadDualbandAlgorithmicInfo(WifiProEstimateApInfo apInfo, int scanNum) {
        Bundle apQualityInfo = this.mNetworkQosMonitor.getApHistoryQuality(apInfo);
        if (apQualityInfo != null) {
            apQualityInfo.putInt("ISSAMEAP", HwDualBandRelationManager.isDualBandAP(this.mCurrentBssid, apInfo.getApBssid()) ? 1 : 0);
            apQualityInfo.putInt("NUMSCAN", scanNum);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt(EVENT_ID, CHR_ID_DUAL_BAND_EXCEPTION);
            dftEventData.putBundle(EVENT_DATA, apQualityInfo);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadChrWifiHandoverCell(boolean isValidated) {
        if (!isValidated) {
            uploadChrWifiHandoverTypeStatistics(WIFI_HANDOVER_TYPES[0], HANDOVER_CNT);
        } else {
            uploadChrWifiHandoverTypeStatistics(WIFI_HANDOVER_TYPES[1], HANDOVER_CNT);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadChrWifiHandoverWifi(boolean isValidated, boolean isWifiOnly) {
        if (!isValidated) {
            this.mChrWifiHandoverType = WIFI_HANDOVER_TYPES[2];
        } else if (isWifiOnly) {
            this.mChrWifiHandoverType = WIFI_HANDOVER_TYPES[4];
        } else {
            this.mChrWifiHandoverType = WIFI_HANDOVER_TYPES[3];
        }
        uploadChrWifiHandoverTypeStatistics(this.mChrWifiHandoverType, HANDOVER_CNT);
        uploadChrExceptionHandoverNetworkQuality(this.mChrWifiHandoverType);
    }

    public void uploadChrWifiHandoverTypeStatistics(String handoverType, String eventType) {
        if (TextUtils.isEmpty(handoverType) || TextUtils.isEmpty(eventType)) {
            logE("uploadChrWifiHandoverTypeStatistics error.");
            return;
        }
        if (HANDOVER_CNT.equals(eventType) && this.mCurrentWifiConfig != null && !WIFI_HANDOVER_TYPES[7].equals(handoverType)) {
            this.mChrQosLevelBeforeHandover = this.mCurrentWifiConfig.networkQosLevel;
        }
        if (HANDOVER_SUCC_CNT.equals(eventType) && getChrWifiHandoverTypeIndex(handoverType) >= 2) {
            if (getHandler().hasMessages(EVENT_CHR_CHECK_WIFI_HANDOVER)) {
                removeMessages(EVENT_CHR_CHECK_WIFI_HANDOVER);
            }
            sendMessageDelayed(EVENT_CHR_CHECK_WIFI_HANDOVER, CHR_WIFI_2_CELL_DELAY_TIME_INTERVAL);
        }
        Bundle data = new Bundle();
        data.putString(HANDOVER_TYPE, handoverType);
        data.putInt(eventType, 1);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, CHR_ID_WIFI_HANDOVER_TYPE);
        dftEventData.putBundle(EVENT_DATA, data);
        logI("uploadChrWifiHandoverTypeStatistics dftEventData = " + dftEventData);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    private int getChrWifiHandoverTypeIndex(String type) {
        if (TextUtils.isEmpty(type)) {
            return -1;
        }
        int index = 0;
        while (true) {
            String[] strArr = WIFI_HANDOVER_TYPES;
            if (index >= strArr.length) {
                return -1;
            }
            if (type.equals(strArr[index])) {
                return index;
            }
            index++;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleChrWifiHandoverCheck() {
        if (TextUtils.isEmpty(this.mChrWifiHandoverType) || this.mCurrentWifiConfig == null) {
            logE("wifi is disconnected or not happen wifi handover.");
            return;
        }
        if (!this.mIsWiFiNoInternet) {
            logI("after wifi handover, wifi has internet.");
            uploadChrWifiHandoverTypeStatistics(this.mChrWifiHandoverType, HANDOVER_OK_CNT);
        }
        if (this.mCurrentWifiConfig.networkQosLevel > this.mChrQosLevelBeforeHandover) {
            logI("after wifi handover, wifi qos level is better.");
            uploadChrWifiHandoverTypeStatistics(this.mChrWifiHandoverType, HANDOVER_BETTER_CNT);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadChrHandoverUnexpectedTypes(String unexpectedType) {
        if (TextUtils.isEmpty(unexpectedType)) {
            logE("uploadChrHandoverUnexpectedTypes error.");
            return;
        }
        logI("uploadChrHandoverUnexpectedTypes unexpectedType = " + unexpectedType);
        Bundle data = new Bundle();
        data.putString(HANDOVER_TYPE, unexpectedType);
        data.putInt(HANDOVER_CNT, 1);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, CHR_ID_WIFI_HANDOVER_UNEXPECTED_TYPE);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    private void uploadChrExceptionHandoverNetworkQuality(String handoverType) {
        if (TextUtils.isEmpty(handoverType)) {
            logE("uploadChrExceptionHandoverNetworkQuality error.");
            return;
        }
        Bundle chrNetworkQuality = this.mNetworkQosMonitor.getChrHandoverNetworkQuality();
        if (chrNetworkQuality != null) {
            chrNetworkQuality.putString("TYPE", handoverType);
            chrNetworkQuality.putInt("RSSI", this.mCurrentRssi);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt(EVENT_ID, CHR_ID_HANDOVER_EXCEPTION_NETWORK_QUALITY);
            dftEventData.putBundle(EVENT_DATA, chrNetworkQuality);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }
    }

    private void uploadWifiDualBandInfo(int dualBandReason) {
        if (dualBandReason != 0) {
            uploadWifiDualBandTriggerReason(dualBandReason);
        } else {
            uploadWifiDualBandFailReason(3);
        }
    }

    public void uploadWifiDualBandTriggerReason(int reason) {
        Bundle data = new Bundle();
        data.putString(HANDOVER_TYPE, String.format(Locale.ROOT, "reason%d", Integer.valueOf(reason)));
        data.putInt(HANDOVER_CNT, 1);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, ID_WIFI_DUALBAND_TRIGGER_INFO);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    public void uploadWifiDualBandFailReason(int reason) {
        int dualbandFailReason;
        if (reason == 1 || reason == 10 || reason == 11) {
            dualbandFailReason = 1;
        } else if (reason == 2 || reason == 3) {
            dualbandFailReason = reason;
        } else if (reason == -7 || reason == -6) {
            dualbandFailReason = 4;
        } else {
            logE("reason = " + reason);
            return;
        }
        Bundle data = new Bundle();
        data.putString(HANDOVER_TYPE, String.format(Locale.ROOT, "reason%d", Integer.valueOf(dualbandFailReason)));
        data.putInt(HANDOVER_CNT, 1);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, ID_WIFI_DUALBAND_FAIL_REASON_INFO);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    public void uploadWifiDualBandScanNum(int count) {
        String countLevel;
        if (count > DUALBAND_SCAN_COUNT_INTERVAL[0] || count < 0) {
            int[] iArr = DUALBAND_SCAN_COUNT_INTERVAL;
            if (count > iArr[1] || count <= iArr[0]) {
                int[] iArr2 = DUALBAND_SCAN_COUNT_INTERVAL;
                if (count > iArr2[2] || count <= iArr2[1]) {
                    countLevel = WIFI_HANDOVER_5G_SCAN_LEVEL_TYPES[3];
                } else {
                    countLevel = WIFI_HANDOVER_5G_SCAN_LEVEL_TYPES[2];
                }
            } else {
                countLevel = WIFI_HANDOVER_5G_SCAN_LEVEL_TYPES[1];
            }
        } else {
            countLevel = WIFI_HANDOVER_5G_SCAN_LEVEL_TYPES[0];
        }
        Bundle data = new Bundle();
        data.putString(WIFI_SWITCH_TIME_LEVEL, countLevel);
        data.putInt(HANDOVER_CNT, 1);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, ID_WIFI_DUALBAND_SCAN_INFO);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    public void uploadWifiDualBandHandoverDura() {
        String duraLevel;
        int dualBandDura = (int) (SystemClock.elapsedRealtime() - this.mWifiDualBandStartTime);
        if (dualBandDura > 0 && dualBandDura <= 180000) {
            int[] iArr = DUALBAND_DURATION_INTERVAL;
            if (dualBandDura <= iArr[0]) {
                duraLevel = WIFI_HANDOVER_5G_DURA_LEVEL_TYPES[0];
            } else if (dualBandDura > iArr[1] || dualBandDura <= iArr[0]) {
                int[] iArr2 = DUALBAND_DURATION_INTERVAL;
                if (dualBandDura > iArr2[2] || dualBandDura <= iArr2[1]) {
                    duraLevel = WIFI_HANDOVER_5G_DURA_LEVEL_TYPES[3];
                } else {
                    duraLevel = WIFI_HANDOVER_5G_DURA_LEVEL_TYPES[2];
                }
            } else {
                duraLevel = WIFI_HANDOVER_5G_DURA_LEVEL_TYPES[1];
            }
            Bundle data = new Bundle();
            if (this.mIsChrQosBetterAfterDualbandHandover) {
                data.putInt("BETTERNETCNT", 1);
            }
            data.putString(WIFI_SWITCH_TIME_LEVEL, duraLevel);
            data.putInt(HANDOVER_CNT, 1);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt(EVENT_ID, ID_WIFI_DUALBAND_DURATION_INFO);
            dftEventData.putBundle(EVENT_DATA, data);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }
    }

    public void uploadWifiDualBandTarget5gAp(int count) {
        String countLevel;
        if (count > DUALBAND_TARGET_AP_COUNT_INTERVAL[0] || count < 0) {
            int[] iArr = DUALBAND_TARGET_AP_COUNT_INTERVAL;
            if (count > iArr[1] || count <= iArr[0]) {
                int[] iArr2 = DUALBAND_TARGET_AP_COUNT_INTERVAL;
                if (count > iArr2[2] || count <= iArr2[1]) {
                    countLevel = WIFI_HANDOVER_5G_AP_NUM_LEVEL_TYPES[3];
                } else {
                    countLevel = WIFI_HANDOVER_5G_AP_NUM_LEVEL_TYPES[2];
                }
            } else {
                countLevel = WIFI_HANDOVER_5G_AP_NUM_LEVEL_TYPES[1];
            }
        } else {
            countLevel = WIFI_HANDOVER_5G_AP_NUM_LEVEL_TYPES[0];
        }
        Bundle data = new Bundle();
        data.putString(WIFI_SWITCH_TIME_LEVEL, countLevel);
        data.putInt(HANDOVER_CNT, 1);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, ID_WIFI_DUALBAND_TARGET_AP_INFO);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadChrDualBandOnLineTime() {
        if (SystemClock.elapsedRealtime() - this.mChrDualbandConnectedStartTime > 300000) {
            logI("chr dualband handover online time.");
            Bundle data = new Bundle();
            data.putInt("ONLINETIME", 1);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt(EVENT_ID, ID_WIFI_DUALBAND_DURATION_INFO);
            dftEventData.putBundle(EVENT_DATA, data);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadChrDualBandSameApCount() {
        Bundle data = new Bundle();
        data.putInt("SAMEAPCNT", 1);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, ID_WIFI_DUALBAND_DURATION_INFO);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadChrHandoverBackDuration(int reason, boolean isUserHandoverBack, long handoverBackStartTime) {
        int durationLevel;
        int handoverBackType;
        long handoverBackDuration = (SystemClock.elapsedRealtime() - handoverBackStartTime) / 1000;
        if (handoverBackStartTime != 0 && handoverBackDuration > 0) {
            if (reason == 205 || reason == 206 || reason == 107) {
                if (isUserHandoverBack) {
                    handoverBackType = 2;
                } else {
                    handoverBackType = 3;
                }
                durationLevel = getChrHandoverBackDuratonSection(false, handoverBackDuration);
            } else if (reason == 207 || reason == 208) {
                if (isUserHandoverBack) {
                    handoverBackType = 0;
                } else {
                    handoverBackType = 1;
                }
                durationLevel = getChrHandoverBackDuratonSection(DBG, handoverBackDuration);
            } else {
                logI("unexpect reason happen in uploadChrHandoverBackDuration.");
                return;
            }
            Bundle data = new Bundle();
            data.putInt(HANDOVER_TYPE, handoverBackType);
            data.putInt(WIFI_SWITCH_TIME_LEVEL, durationLevel);
            logI("uploadChrHandoverBackDuration dataInfo = " + data);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt(EVENT_ID, CHR_ID_HANDOVER_BACK_DURATION);
            dftEventData.putBundle(EVENT_DATA, data);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }
    }

    private int getChrHandoverBackDuratonSection(boolean isStrongSignal, long handoverBackDuration) {
        int[] handoverBackDuratonSection;
        if (isStrongSignal) {
            handoverBackDuratonSection = STRONG_SIGNAL_DURATION_SECTION;
        } else {
            handoverBackDuratonSection = WEAK_SIGNAL_DURATION_SECTION;
        }
        if (handoverBackDuration <= ((long) handoverBackDuratonSection[0])) {
            return 0;
        }
        if (handoverBackDuration <= ((long) handoverBackDuratonSection[1])) {
            return 1;
        }
        if (handoverBackDuration <= ((long) handoverBackDuratonSection[2])) {
            return 2;
        }
        if (handoverBackDuration <= ((long) handoverBackDuratonSection[3])) {
            return 3;
        }
        if (handoverBackDuration <= ((long) handoverBackDuratonSection[4])) {
            return 4;
        }
        return 5;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadChrDetectAlgorithmDuration() {
        long chrConnectedDuration = 0;
        if (HwWifiConnectivityMonitor.getInstance() != null) {
            chrConnectedDuration = HwWifiConnectivityMonitor.getInstance().getChrWifiConnectedAndScreenOnDuration();
        }
        Bundle detectResult = this.mNetworkQosMonitor.getChrInterentDetectAlgorithmDuration();
        detectResult.putInt("ConnectedDutation", (int) (chrConnectedDuration / 1000));
        logI("uploadChrDetectAlgorithmDuration detectResultInfo = " + detectResult);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, CHR_ID_DETECT_ALGORITHM_DURATION);
        dftEventData.putBundle(EVENT_DATA, detectResult);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isShortTimeWifiEnabled() {
        long diffTime = System.currentTimeMillis() - this.mWifiEnableTime;
        logI("judge wheather wifi is enabled in short time");
        if (diffTime <= 0 || diffTime >= 30000) {
            return false;
        }
        logI("wifi wifi is enabled in short time");
        return DBG;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendInvalidLinkDetected() {
        if (this.mWsmChannel != null) {
            this.mIsMplinkStarted = isMpLinkStarted();
            logI("sendInvalidLinkDetected and Mplink is Started = " + this.mIsMplinkStarted);
            this.mWsmChannel.sendMessage((int) INVALID_LINK_DETECTED);
        }
    }

    public void setWifiSwitchForbiddenFromApp(boolean enable) {
        this.mIsSwitchForbiddenFromApp = enable;
        if (hasMessages(EVENT_RECOVERY_WLANPRO_SWITCH)) {
            removeMessages(EVENT_RECOVERY_WLANPRO_SWITCH);
        }
        logI("SwitchForbiddenFromApp = " + this.mIsSwitchForbiddenFromApp);
        if (this.mIsSwitchForbiddenFromApp) {
            sendMessageDelayed(EVENT_RECOVERY_WLANPRO_SWITCH, DELAY_UPLOAD_MS);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recoveryWlanSwitch() {
        if (this.mIsSwitchForbiddenFromApp) {
            if (hasMessages(EVENT_RECOVERY_WLANPRO_SWITCH)) {
                removeMessages(EVENT_RECOVERY_WLANPRO_SWITCH);
            }
            this.mIsSwitchForbiddenFromApp = false;
        }
    }

    private boolean isReportWifi2CellChrType(int type) {
        if (type == 10 || type == 11 || type == 107) {
            return DBG;
        }
        return false;
    }

    private int getWifiRssi() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            Log.e(TAG, "handleWifi2CellStartChr: mWifiManager is null");
            return 0;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            return wifiInfo.getRssi();
        }
        Log.e(TAG, "handleWifi2CellStartChr: wifiInfo is null");
        return 0;
    }

    private void reportWifi2CellStartChr(int type, int reason) {
        if (hasMessages(EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG)) {
            handleWifi2CellFailChr(getWifi2CellFailReason());
        }
        if (this.mAppQoeInfo == null) {
            Log.e(TAG, "handleWifi2CellStartChr: appQoeInfo is null");
            return;
        }
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            Log.e(TAG, "handleWifi2CellStartChr: mWifiManager is null");
            return;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.e(TAG, "handleWifi2CellStartChr: wifiInfo is null");
            return;
        }
        WifiProChr.Wifi2CellParam wifi2CellParam = new WifiProChr.Wifi2CellParam();
        wifi2CellParam.setType(type);
        wifi2CellParam.setRssi(wifiInfo.getRssi());
        wifi2CellParam.setTxRate(wifiInfo.getTxLinkSpeedMbps());
        wifi2CellParam.setRxRate(wifiInfo.getRxLinkSpeedMbps());
        wifi2CellParam.setUlDelay(wifiInfo.getUlDelay());
        wifi2CellParam.setChload(wifiInfo.getChload());
        wifi2CellParam.setMasterWifiQoe(this.mAppQoeInfo.mMasterWifiChannelQoeLevel);
        wifi2CellParam.setSlaveWifiQoe(this.mAppQoeInfo.mSlaveWifiChannelQoeScore);
        wifi2CellParam.setMasterCellQoe(this.mAppQoeInfo.mMasterCellChannelQoeLevel);
        wifi2CellParam.setSlaveCellQoe(this.mAppQoeInfo.mSlaveCellChannelQoeLevel);
        wifi2CellParam.setMasterCellUpRate(this.mAppQoeInfo.mMasterCellUplinkRate);
        wifi2CellParam.setSlaveCellUpRate(this.mAppQoeInfo.mSlaveCellUplinkRate);
        wifi2CellParam.setTriggerReason(reason);
        wifi2CellParam.setMpState(isMpLinkStarted() ? 1 : 0);
        wifi2CellParam.setStatusCode("START");
        wifi2CellParam.setPkgName(WifiProChr.getInstance().getChrForePkgName());
        WifiConfiguration wifiConfiguration = this.mCurrentWifiConfig;
        wifi2CellParam.setEnterpriseAp((wifiConfiguration == null || !wifiConfiguration.isEnterprise()) ? 0 : 1);
        WifiProChr.getInstance().updateWifi2CellParamHandoverStart(wifi2CellParam);
        sendMessageDelayed(EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG, CHR_WIFI_2_CELL_DELAY_TIME_INTERVAL);
    }

    private void handleWifi2CellStartChr(boolean poorLink, int type) {
        WifiConfiguration wifiConfiguration;
        if (!poorLink || getCurrentState() != this.mWiFiLinkMonitorState || !isReportWifi2CellChrType(type) || this.mChrSwitchToCellSuccFlagInLinkMonitor) {
            logI("handleWifi2CellStartChr not report chr poorLink:" + poorLink + ", type:" + type + ", mChrSwitchToCellSuccFlagInLinkMonitor:" + this.mChrSwitchToCellSuccFlagInLinkMonitor);
        } else if (type != 10) {
            if (type != 11) {
                if (type == 107) {
                    reportWifi2CellStartChr(3, 6);
                }
            } else if (this.mAppQoeInfo.mAppQoeBadCnt >= 9 && (wifiConfiguration = this.mCurrentWifiConfig) != null && wifiConfiguration.isEnterprise()) {
                reportWifi2CellStartChr(1, 5);
            }
        } else if (this.mAppQoeInfo.mChannelQoeBadCnt >= 1 && isWifiSignalBad(this.mAppQoeInfo.mMasterWifiRssi)) {
            reportWifi2CellStartChr(2, 1);
        } else if (this.mAppQoeInfo.mChannelQoeBadCnt >= 3) {
            reportWifi2CellStartChr(2, 2);
        } else if (this.mAppQoeInfo.mAppQoeBadCnt >= 1 && this.mAppQoeInfo.mNetworkSlowChannelBadCnt >= 1 && isWifiSignalBad(this.mAppQoeInfo.mMasterWifiRssi)) {
            reportWifi2CellStartChr(2, 3);
        } else if (this.mAppQoeInfo.mAppQoeBadCnt >= 3 && this.mAppQoeInfo.mNetworkSlowChannelBadCnt >= 3) {
            reportWifi2CellStartChr(2, 2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifi2CellSuccChr(int reason) {
        if (this.mAppQoeInfo == null) {
            Log.e(TAG, "handleWifi2CellStartChr: appQoeInfo is null");
            return;
        }
        removeMessages(EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG);
        WifiProChr.getInstance().updateWifi2CellParamHandoverSucc(this.mAppQoeInfo.mMasterCellChannelQoeLevel, this.mAppQoeInfo.mSlaveCellChannelQoeLevel, this.mAppQoeInfo.mMasterCellUplinkRate, this.mAppQoeInfo.mSlaveCellUplinkRate, (int) (this.mHandoverCellSuccessTime - this.mHandoverCellStartTime));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifi2CellSuccExitChr() {
        long handoverCellSuccessExitTime = SystemClock.elapsedRealtime();
        int exitReason = WifiProChr.getInstance().getChrSwitchSuccedExitReason();
        int efftiveTime = (int) (handoverCellSuccessExitTime - this.mHandoverCellSuccessTime);
        WifiProChr.getInstance().setChrSwitchSuccTrafficSum((int) ((TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()) - WifiProChr.getInstance().getChrSwitchSuccStartTraffic()));
        WifiProChr.getInstance().updateWifi2CellParamSuccExit(exitReason, getWifiRssi(), efftiveTime, WifiProChr.getInstance().getChrSwitchCellGainState(), WifiProChr.getInstance().getChrSwitchSuccTrafficSum());
        int switchTime = (int) (this.mHandoverCellSuccessTime - this.mHandoverCellStartTime);
        long j = this.mQoeFirstGoodTime;
        int firstGoodTime = ((int) j) > switchTime ? (int) j : switchTime;
        WifiProChr.Wifi2CellQoe wifi2CellQoe = new WifiProChr.Wifi2CellQoe();
        wifi2CellQoe.setType(WifiProChr.getInstance().getChrSwitchCellSuccTriggerType());
        wifi2CellQoe.setQoeGoodCount(WifiProChr.getInstance().getChrSwitchCellGainState());
        wifi2CellQoe.setQoeFirstGoodTime(firstGoodTime);
        wifi2CellQoe.setCount(1);
        wifi2CellQoe.setSwitchTimeSum(switchTime);
        wifi2CellQoe.setEfftiveTimeSum(efftiveTime);
        wifi2CellQoe.setTrafficSum(WifiProChr.getInstance().getChrSwitchSuccTrafficSum());
        WifiProChr.getInstance().updateWifi2CellQoe(wifi2CellQoe);
        WifiProChr.getInstance().reportWifi2CellParam();
        handleWifiProStaticsChrReport();
        resetWifiProChrParam();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifi2CellFailChr(int failReason) {
        if ((failReason == 199 || failReason == 23) && hasMessages(EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG)) {
            Log.d(TAG, "handleWifi2CellFailChr: in 20s, not report failReason:" + failReason);
            return;
        }
        Log.d(TAG, "handleWifi2CellFailChr: failReason:" + failReason);
        WifiProChr.getInstance().updateWifi2CellParamHandoverFail(failReason, getWifiRssi());
        WifiProChr.getInstance().reportWifi2CellParam();
        resetWifiProChrParam();
    }

    private void resetWifiProChrParam() {
        this.mIsQoeFirstGood = false;
        this.mQoeBadCount = 0;
        this.mQoeFirstGoodTime = 0;
        this.mChrSwitchToCellSuccFlagInLinkMonitor = false;
        WifiProChr.getInstance().setChrSwitchCellGainState(1);
        WifiProChr.getInstance().setChrSwitchCellSuccTriggerType(0);
        WifiProChr.getInstance().setChrSwitchSuccedExitReason(WifiProChr.WIFI_2_CELL_SUCC_EXIT_REASION_OTHERS);
        WifiProChr.getInstance().setChrSwitchSuccTrafficSum(0);
        removeMessages(EVENT_CHR_WIFI_2_CELL_DELAY_TIME_OUT_MSG);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getWifi2CellFailReason() {
        if (this.mIsVpnWorking) {
            return 3;
        }
        if (this.mIsUserHandoverWiFi && !this.mIsWiFiNoInternet) {
            return 4;
        }
        if (this.mHiLinkUnconfig) {
            return 5;
        }
        if (isAppinWhitelists()) {
            return 6;
        }
        if (isWifiRepeaterOn()) {
            return 7;
        }
        if (getCurrentState() == this.mWiFiProVerfyingLinkState) {
            return 8;
        }
        if (this.mTelephonyManager.getSimState() != 5) {
            return 25;
        }
        if (!this.mIsMobileDataEnabled) {
            return 26;
        }
        if (isAirModeOn()) {
            return 22;
        }
        if (!this.mPowerManager.isScreenOn()) {
            return 14;
        }
        if (WifiProCommonUtils.isOversea()) {
            return 13;
        }
        return WifiProChr.WIFI_2_CELL_FAIL_REASON_MAX_OTHERS;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerBoosterNetworkQoe() {
        if (this.mRegisterAppQoeTime == 0) {
            logI("first registerBoosterNetworkQoe delay 10 second");
            this.mRegisterAppQoeTime++;
            sendMessageDelayed(EVENT_REGISTER_APP_QOE, 10000);
            return;
        }
        Handler handler = getHandler();
        if (handler == null) {
            logE("registerBoosterNetworkQoe, handler is null");
            return;
        }
        int result = HwDataServiceQoeEx.registerNetworkQoe("com.huawei.hwwifiproservice", "all", handler);
        if (result == 0) {
            logI("registerBoosterNetworkQoe success, mRegisterAppQoeTime=" + this.mRegisterAppQoeTime);
            return;
        }
        logI("registerBoosterNetworkQoe result=" + result + "mRegisterAppQoeTime=" + this.mRegisterAppQoeTime);
        if (this.mRegisterAppQoeTime >= 6) {
            logE("registerBoosterNetworkQoe fail");
            return;
        }
        this.mRegisterAppQoeTime++;
        sendMessageDelayed(EVENT_REGISTER_APP_QOE, 10000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showWifiToCellToast() {
        if (this.mWifiProUIDisplayManager == null) {
            logE("showWifiToCellToast, mWifiProUIDisplayManager is null");
        } else if (NetworkRecommendManager.getInstance().isRecommendShowWifiToCellToast()) {
            this.mWifiProUIDisplayManager.showWifiProToast(1);
        }
    }

    public boolean isWiFiProEnabled() {
        return this.mIsWiFiProEnabled;
    }
}

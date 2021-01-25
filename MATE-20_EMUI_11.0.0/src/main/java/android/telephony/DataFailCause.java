package android.telephony;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.security.keystore.KeyProperties;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.util.ArrayUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntPredicate;

@SystemApi
public final class DataFailCause {
    public static final int ACCESS_ATTEMPT_ALREADY_IN_PROGRESS = 2219;
    public static final int ACCESS_BLOCK = 2087;
    public static final int ACCESS_BLOCK_ALL = 2088;
    public static final int ACCESS_CLASS_DSAC_REJECTION = 2108;
    public static final int ACCESS_CONTROL_LIST_CHECK_FAILURE = 2128;
    public static final int ACTIVATION_REJECTED_BCM_VIOLATION = 48;
    public static final int ACTIVATION_REJECT_GGSN = 30;
    public static final int ACTIVATION_REJECT_UNSPECIFIED = 31;
    public static final int ACTIVE_PDP_CONTEXT_MAX_NUMBER_REACHED = 65;
    public static final int APN_DISABLED = 2045;
    public static final int APN_DISALLOWED_ON_ROAMING = 2059;
    public static final int APN_MISMATCH = 2054;
    public static final int APN_PARAMETERS_CHANGED = 2060;
    public static final int APN_PENDING_HANDOVER = 2041;
    public static final int APN_TYPE_CONFLICT = 112;
    public static final int AUTH_FAILURE_ON_EMERGENCY_CALL = 122;
    public static final int BEARER_HANDLING_NOT_SUPPORTED = 60;
    public static final int CALL_DISALLOWED_IN_ROAMING = 2068;
    public static final int CALL_PREEMPT_BY_EMERGENCY_APN = 127;
    public static final int CANNOT_ENCODE_OTA_MESSAGE = 2159;
    public static final int CDMA_ALERT_STOP = 2077;
    public static final int CDMA_INCOMING_CALL = 2076;
    public static final int CDMA_INTERCEPT = 2073;
    public static final int CDMA_LOCK = 2072;
    public static final int CDMA_RELEASE_DUE_TO_SO_REJECTION = 2075;
    public static final int CDMA_REORDER = 2074;
    public static final int CDMA_RETRY_ORDER = 2086;
    public static final int CHANNEL_ACQUISITION_FAILURE = 2078;
    public static final int CLOSE_IN_PROGRESS = 2030;
    public static final int COLLISION_WITH_NETWORK_INITIATED_REQUEST = 56;
    public static final int COMPANION_IFACE_IN_USE = 118;
    public static final int CONCURRENT_SERVICES_INCOMPATIBLE = 2083;
    public static final int CONCURRENT_SERVICES_NOT_ALLOWED = 2091;
    public static final int CONCURRENT_SERVICE_NOT_SUPPORTED_BY_BASE_STATION = 2080;
    public static final int CONDITIONAL_IE_ERROR = 100;
    public static final int CONGESTION = 2106;
    public static final int CONNECTION_RELEASED = 2113;
    public static final int CONNECTION_TO_DATACONNECTIONAC_BROKEN = 65539;
    public static final int CS_DOMAIN_NOT_AVAILABLE = 2181;
    public static final int CS_FALLBACK_CALL_ESTABLISHMENT_NOT_ALLOWED = 2188;
    private static final String CUST_PSPERMANENT_FAILURE = SystemProperties.get("ro.hwpp_ds_permanent_fail", "");
    public static final int DATA_PLAN_EXPIRED = 2198;
    public static final int DATA_ROAMING_SETTINGS_DISABLED = 2064;
    public static final int DATA_SETTINGS_DISABLED = 2063;
    public static final int DBM_OR_SMS_IN_PROGRESS = 2211;
    public static final int DDS_SWITCHED = 2065;
    public static final int DDS_SWITCH_IN_PROGRESS = 2067;
    public static final int DNN_NOT_SUPPORTED_OR_SUBSCRIBED_IN_SLICE = 91;
    public static final int DRB_RELEASED_BY_RRC = 2112;
    public static final int DSM_UNKNOWN = 3199;
    public static final int DS_EXPLICIT_DEACTIVATION = 2125;
    public static final int DUAL_SWITCH = 2227;
    public static final int DUN_CALL_DISALLOWED = 2056;
    public static final int DUPLICATE_BEARER_ID = 2118;
    public static final int EHRPD_TO_HRPD_FALLBACK = 2049;
    public static final int EHSM_PPP_ERROR_CONN_EXISTS_FOR_THIS_APN = 1821;
    public static final int EMBMS_NOT_ENABLED = 2193;
    public static final int EMBMS_REGULAR_DEACTIVATION = 2195;
    public static final int EMERGENCY_IFACE_ONLY = 116;
    public static final int EMERGENCY_MODE = 2221;
    public static final int EMM_ACCESS_BARRED = 115;
    public static final int EMM_ACCESS_BARRED_INFINITE_RETRY = 121;
    public static final int EMM_ATTACH_FAILED = 2115;
    public static final int EMM_ATTACH_STARTED = 2116;
    public static final int EMM_DETACHED = 2114;
    public static final int EMM_T3417_EXPIRED = 2130;
    public static final int EMM_T3417_EXT_EXPIRED = 2131;
    public static final int EPS_SERVICES_AND_NON_EPS_SERVICES_NOT_ALLOWED = 2178;
    public static final int EPS_SERVICES_NOT_ALLOWED_IN_PLMN = 2179;
    public static final int ERROR_UNSPECIFIED = 65535;
    public static final int ESM_BAD_OTA_MESSAGE = 2122;
    public static final int ESM_BEARER_DEACTIVATED_TO_SYNC_WITH_NETWORK = 2120;
    public static final int ESM_COLLISION_SCENARIOS = 2119;
    public static final int ESM_CONTEXT_TRANSFERRED_DUE_TO_IRAT = 2124;
    public static final int ESM_DOWNLOAD_SERVER_REJECTED_THE_CALL = 2123;
    public static final int ESM_FAILURE = 2182;
    public static final int ESM_INFO_NOT_RECEIVED = 53;
    public static final int ESM_LOCAL_CAUSE_NONE = 2126;
    public static final int ESM_NW_ACTIVATED_DED_BEARER_WITH_ID_OF_DEF_BEARER = 2121;
    public static final int ESM_PROCEDURE_TIME_OUT = 2155;
    public static final int ESM_UNKNOWN_EPS_BEARER_CONTEXT = 2111;
    public static final int EVDO_CONNECTION_DENY_BY_BILLING_OR_AUTHENTICATION_FAILURE = 2201;
    public static final int EVDO_CONNECTION_DENY_BY_GENERAL_OR_NETWORK_BUSY = 2200;
    public static final int EVDO_HDR_CHANGED = 2202;
    public static final int EVDO_HDR_CONNECTION_SETUP_TIMEOUT = 2206;
    public static final int EVDO_HDR_EXITED = 2203;
    public static final int EVDO_HDR_NO_SESSION = 2204;
    public static final int EVDO_USING_GPS_FIX_INSTEAD_OF_HDR_CALL = 2205;
    public static final int FADE = 2217;
    public static final int FAILED_TO_ACQUIRE_COLOCATED_HDR = 2207;
    public static final int FEATURE_NOT_SUPP = 40;
    public static final int FILTER_SEMANTIC_ERROR = 44;
    public static final int FILTER_SYTAX_ERROR = 45;
    public static final int FORBIDDEN_APN_NAME = 2066;
    public static final int GPRS_REGISTRATION_FAIL = -2;
    public static final int GPRS_SERVICES_AND_NON_GPRS_SERVICES_NOT_ALLOWED = 2097;
    public static final int GPRS_SERVICES_NOT_ALLOWED = 2098;
    public static final int GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN = 2103;
    public static final int HANDOFF_PREFERENCE_CHANGED = 2251;
    public static final int HANDOVER_FAILED = 65542;
    public static final int HDR_ACCESS_FAILURE = 2213;
    public static final int HDR_FADE = 2212;
    public static final int HDR_NO_LOCK_GRANTED = 2210;
    public static final int IFACE_AND_POL_FAMILY_MISMATCH = 120;
    public static final int IFACE_MISMATCH = 117;
    public static final int ILLEGAL_ME = 2096;
    public static final int ILLEGAL_MS = 2095;
    public static final int IMEI_NOT_ACCEPTED = 2177;
    public static final int IMPLICITLY_DETACHED = 2100;
    public static final int IMSI_UNKNOWN_IN_HOME_SUBSCRIBER_SERVER = 2176;
    public static final int INCOMING_CALL_REJECTED = 2092;
    public static final int INSUFFICIENT_RESOURCES = 26;
    public static final int INSUFFICIENT_RESOURCES_FOR_SPECIFIC_SLICE = 69;
    public static final int INSUFFICIENT_RESOURCES_FOR_SPECIFIC_SLICE_AND_DNN = 67;
    public static final int INTERFACE_IN_USE = 2058;
    public static final int INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN = 114;
    public static final int INTERNAL_EPC_NONEPC_TRANSITION = 2057;
    public static final int INVALID_CONNECTION_ID = 2156;
    public static final int INVALID_DNS_ADDR = 123;
    public static final int INVALID_EMM_STATE = 2190;
    public static final int INVALID_MANDATORY_INFO = 96;
    public static final int INVALID_MODE = 2223;
    public static final int INVALID_PCSCF_ADDR = 113;
    public static final int INVALID_PCSCF_OR_DNS_ADDRESS = 124;
    public static final int INVALID_PRIMARY_NSAPI = 2158;
    public static final int INVALID_SIM_STATE = 2224;
    public static final int INVALID_TRANSACTION_ID = 81;
    public static final int IPV6_ADDRESS_TRANSFER_FAILED = 2047;
    public static final int IPV6_PREFIX_UNAVAILABLE = 2250;
    public static final int IP_ADDRESS_MISMATCH = 119;
    public static final int IP_VERSION_MISMATCH = 2055;
    public static final int IRAT_HANDOVER_FAILED = 2194;
    public static final int IS707B_MAX_ACCESS_PROBES = 2089;
    public static final int LIMITED_TO_IPV4 = 2234;
    public static final int LIMITED_TO_IPV6 = 2235;
    public static final int LLC_SNDCP = 25;
    public static final int LOCAL_END = 2215;
    public static final int LOCATION_AREA_NOT_ALLOWED = 2102;
    public static final int LOST_CONNECTION = 65540;
    public static final int LOWER_LAYER_REGISTRATION_FAILURE = 2197;
    public static final int LOW_POWER_MODE_OR_POWERING_DOWN = 2044;
    public static final int LTE_NAS_SERVICE_REQUEST_FAILED = 2117;
    public static final int LTE_THROTTLING_NOT_REQUIRED = 2127;
    public static final int MAC_FAILURE = 2183;
    public static final int MAXIMIUM_NSAPIS_EXCEEDED = 2157;
    public static final int MAXINUM_SIZE_OF_L2_MESSAGE_EXCEEDED = 2166;
    public static final int MAX_ACCESS_PROBE = 2079;
    public static final int MAX_DATA_RATE_FOR_UP_INT_PROT_IS_TOO_LOW = 82;
    public static final int MAX_IPV4_CONNECTIONS = 2052;
    public static final int MAX_IPV6_CONNECTIONS = 2053;
    public static final int MAX_PPP_INACTIVITY_TIMER_EXPIRED = 2046;
    public static final int MESSAGE_INCORRECT_SEMANTIC = 95;
    public static final int MESSAGE_TYPE_UNSUPPORTED = 97;
    public static final int MIP_CONFIG_FAILURE = 2050;
    public static final int MIP_FA_ADMIN_PROHIBITED = 2001;
    public static final int MIP_FA_DELIVERY_STYLE_NOT_SUPPORTED = 2012;
    public static final int MIP_FA_ENCAPSULATION_UNAVAILABLE = 2008;
    public static final int MIP_FA_HOME_AGENT_AUTHENTICATION_FAILURE = 2004;
    public static final int MIP_FA_INSUFFICIENT_RESOURCES = 2002;
    public static final int MIP_FA_MALFORMED_REPLY = 2007;
    public static final int MIP_FA_MALFORMED_REQUEST = 2006;
    public static final int MIP_FA_MISSING_CHALLENGE = 2017;
    public static final int MIP_FA_MISSING_HOME_ADDRESS = 2015;
    public static final int MIP_FA_MISSING_HOME_AGENT = 2014;
    public static final int MIP_FA_MISSING_NAI = 2013;
    public static final int MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE = 2003;
    public static final int MIP_FA_REASON_UNSPECIFIED = 2000;
    public static final int MIP_FA_REQUESTED_LIFETIME_TOO_LONG = 2005;
    public static final int MIP_FA_REVERSE_TUNNEL_IS_MANDATORY = 2011;
    public static final int MIP_FA_REVERSE_TUNNEL_UNAVAILABLE = 2010;
    public static final int MIP_FA_STALE_CHALLENGE = 2018;
    public static final int MIP_FA_UNKNOWN_CHALLENGE = 2016;
    public static final int MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE = 2009;
    public static final int MIP_HA_ADMIN_PROHIBITED = 2020;
    public static final int MIP_HA_ENCAPSULATION_UNAVAILABLE = 2029;
    public static final int MIP_HA_FOREIGN_AGENT_AUTHENTICATION_FAILURE = 2023;
    public static final int MIP_HA_INSUFFICIENT_RESOURCES = 2021;
    public static final int MIP_HA_MALFORMED_REQUEST = 2025;
    public static final int MIP_HA_MOBILE_NODE_AUTHENTICATION_FAILURE = 2022;
    public static final int MIP_HA_REASON_UNSPECIFIED = 2019;
    public static final int MIP_HA_REGISTRATION_ID_MISMATCH = 2024;
    public static final int MIP_HA_REVERSE_TUNNEL_IS_MANDATORY = 2028;
    public static final int MIP_HA_REVERSE_TUNNEL_UNAVAILABLE = 2027;
    public static final int MIP_HA_UNKNOWN_HOME_AGENT_ADDRESS = 2026;
    public static final int MISSING_OR_UNKNOWN_DNN_IN_A_SLICE = 70;
    public static final int MISSING_UNKNOWN_APN = 27;
    public static final int MODEM_APP_PREEMPTED = 2032;
    public static final int MODEM_RESTART = 2037;
    public static final int MSC_TEMPORARILY_NOT_REACHABLE = 2180;
    public static final int MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE = 101;
    public static final int MSG_TYPE_NONCOMPATIBLE_STATE = 98;
    public static final int MS_IDENTITY_CANNOT_BE_DERIVED_BY_THE_NETWORK = 2099;
    public static final int MULTIPLE_PDP_CALL_NOT_ALLOWED = 2192;
    public static final int MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED = 55;
    public static final int NAS_LAYER_FAILURE = 2191;
    public static final int NAS_REQUEST_REJECTED_BY_NETWORK = 2167;
    public static final int NAS_SIGNALLING = 14;
    public static final int NETWORK_CONGESTION = 22;
    public static final int NETWORK_FAILURE = 38;
    public static final int NETWORK_INITIATED_DETACH_NO_AUTO_REATTACH = 2154;
    public static final int NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH = 2153;
    public static final int NETWORK_INITIATED_TERMINATION = 2031;
    public static final int NONE = 0;
    public static final int NON_IP_NOT_SUPPORTED = 2069;
    public static final int NORMAL_RELEASE = 2218;
    public static final int NOT_SUPPORTED_SSC_MODE = 68;
    public static final int NO_CDMA_SERVICE = 2084;
    public static final int NO_COLLOCATED_HDR = 2225;
    public static final int NO_EPS_BEARER_CONTEXT_ACTIVATED = 2189;
    public static final int NO_GPRS_CONTEXT = 2094;
    public static final int NO_HYBRID_HDR_SERVICE = 2209;
    public static final int NO_PDP_CONTEXT_ACTIVATED = 2107;
    public static final int NO_RESPONSE_FROM_BASE_STATION = 2081;
    public static final int NO_SERVICE = 2216;
    public static final int NO_SERVICE_ON_GATEWAY = 2093;
    public static final int NRSM_T3580_MAX_TIME_OUT = 3585;
    public static final int NSAPI_IN_USE = 35;
    public static final int NULL_APN_DISALLOWED = 2061;
    public static final int OEM_DCFAILCAUSE_1 = 4097;
    public static final int OEM_DCFAILCAUSE_10 = 4106;
    public static final int OEM_DCFAILCAUSE_11 = 4107;
    public static final int OEM_DCFAILCAUSE_12 = 4108;
    public static final int OEM_DCFAILCAUSE_13 = 4109;
    public static final int OEM_DCFAILCAUSE_14 = 4110;
    public static final int OEM_DCFAILCAUSE_15 = 4111;
    public static final int OEM_DCFAILCAUSE_2 = 4098;
    public static final int OEM_DCFAILCAUSE_3 = 4099;
    public static final int OEM_DCFAILCAUSE_4 = 4100;
    public static final int OEM_DCFAILCAUSE_5 = 4101;
    public static final int OEM_DCFAILCAUSE_6 = 4102;
    public static final int OEM_DCFAILCAUSE_7 = 4103;
    public static final int OEM_DCFAILCAUSE_8 = 4104;
    public static final int OEM_DCFAILCAUSE_9 = 4105;
    public static final int ONLY_IPV4V6_ALLOWED = 57;
    public static final int ONLY_IPV4_ALLOWED = 50;
    public static final int ONLY_IPV6_ALLOWED = 51;
    public static final int ONLY_NON_IP_ALLOWED = 58;
    public static final int ONLY_SINGLE_BEARER_ALLOWED = 52;
    public static final int OPERATOR_BARRED = 8;
    public static final int OTASP_COMMIT_IN_PROGRESS = 2208;
    public static final int PAYLOAD_WAS_NOT_FORWARDED = 90;
    public static final int PDN_CONN_DOES_NOT_EXIST = 54;
    public static final int PDN_INACTIVITY_TIMER_EXPIRED = 2051;
    public static final int PDN_IPV4_CALL_DISALLOWED = 2033;
    public static final int PDN_IPV4_CALL_THROTTLED = 2034;
    public static final int PDN_IPV6_CALL_DISALLOWED = 2035;
    public static final int PDN_IPV6_CALL_THROTTLED = 2036;
    public static final int PDN_NON_IP_CALL_DISALLOWED = 2071;
    public static final int PDN_NON_IP_CALL_THROTTLED = 2070;
    public static final int PDP_ACTIVATE_MAX_RETRY_FAILED = 2109;
    public static final int PDP_ACTIVE_LIMIT = 2304;
    public static final int PDP_DUPLICATE = 2104;
    public static final int PDP_ESTABLISH_TIMEOUT_EXPIRED = 2161;
    public static final int PDP_INACTIVE_TIMEOUT_EXPIRED = 2163;
    public static final int PDP_LOWERLAYER_ERROR = 2164;
    public static final int PDP_MODIFY_COLLISION = 2165;
    public static final int PDP_MODIFY_TIMEOUT_EXPIRED = 2162;
    public static final int PDP_PPP_NOT_SUPPORTED = 2038;
    public static final int PDP_WITHOUT_ACTIVE_TFT = 46;
    public static final int PHONE_IN_USE = 2222;
    public static final int PHYSICAL_LINK_CLOSE_IN_PROGRESS = 2040;
    public static final int PLMN_NOT_ALLOWED = 2101;
    public static final int PPP_AUTH_FAILURE = 2229;
    public static final int PPP_CHAP_FAILURE = 2232;
    public static final int PPP_CLOSE_IN_PROGRESS = 2233;
    public static final int PPP_OPTION_MISMATCH = 2230;
    public static final int PPP_PAP_FAILURE = 2231;
    public static final int PPP_TIMEOUT = 2228;
    public static final int PREF_RADIO_TECH_CHANGED = -4;
    public static final int PROFILE_BEARER_INCOMPATIBLE = 2042;
    public static final int PROTOCOL_ERRORS = 111;
    public static final int QOS_NOT_ACCEPTED = 37;
    public static final int QOS_OPERATION_SEMANTIC_ERROR = 83;
    public static final int QOS_OPERATION_SYTAX_ERROR = 84;
    public static final int RADIO_ACCESS_BEARER_FAILURE = 2110;
    public static final int RADIO_ACCESS_BEARER_SETUP_FAILURE = 2160;
    public static final int RADIO_NOT_AVAILABLE = 65537;
    public static final int RADIO_POWER_OFF = -5;
    public static final int REDIRECTION_OR_HANDOFF_IN_PROGRESS = 2220;
    public static final int REGISTRATION_FAIL = -1;
    public static final int REGULAR_DEACTIVATION = 36;
    public static final int REJECTED_BY_BASE_STATION = 2082;
    public static final int RESET_BY_FRAMEWORK = 65541;
    public static final int RRC_CONNECTION_ABORTED_AFTER_HANDOVER = 2173;
    public static final int RRC_CONNECTION_ABORTED_AFTER_IRAT_CELL_CHANGE = 2174;
    public static final int RRC_CONNECTION_ABORTED_DUE_TO_IRAT_CHANGE = 2171;
    public static final int RRC_CONNECTION_ABORTED_DURING_IRAT_CELL_CHANGE = 2175;
    public static final int RRC_CONNECTION_ABORT_REQUEST = 2151;
    public static final int RRC_CONNECTION_ACCESS_BARRED = 2139;
    public static final int RRC_CONNECTION_ACCESS_STRATUM_FAILURE = 2137;
    public static final int RRC_CONNECTION_ANOTHER_PROCEDURE_IN_PROGRESS = 2138;
    public static final int RRC_CONNECTION_CELL_NOT_CAMPED = 2144;
    public static final int RRC_CONNECTION_CELL_RESELECTION = 2140;
    public static final int RRC_CONNECTION_CONFIG_FAILURE = 2141;
    public static final int RRC_CONNECTION_INVALID_REQUEST = 2168;
    public static final int RRC_CONNECTION_LINK_FAILURE = 2143;
    public static final int RRC_CONNECTION_NORMAL_RELEASE = 2147;
    public static final int RRC_CONNECTION_OUT_OF_SERVICE_DURING_CELL_REGISTER = 2150;
    public static final int RRC_CONNECTION_RADIO_LINK_FAILURE = 2148;
    public static final int RRC_CONNECTION_REESTABLISHMENT_FAILURE = 2149;
    public static final int RRC_CONNECTION_REJECT_BY_NETWORK = 2146;
    public static final int RRC_CONNECTION_RELEASED_SECURITY_NOT_ACTIVE = 2172;
    public static final int RRC_CONNECTION_RF_UNAVAILABLE = 2170;
    public static final int RRC_CONNECTION_SYSTEM_INFORMATION_BLOCK_READ_ERROR = 2152;
    public static final int RRC_CONNECTION_SYSTEM_INTERVAL_FAILURE = 2145;
    public static final int RRC_CONNECTION_TIMER_EXPIRED = 2142;
    public static final int RRC_CONNECTION_TRACKING_AREA_ID_CHANGED = 2169;
    public static final int RRC_UPLINK_CONNECTION_RELEASE = 2134;
    public static final int RRC_UPLINK_DATA_TRANSMISSION_FAILURE = 2132;
    public static final int RRC_UPLINK_DELIVERY_FAILED_DUE_TO_HANDOVER = 2133;
    public static final int RRC_UPLINK_ERROR_REQUEST_FROM_NAS = 2136;
    public static final int RRC_UPLINK_RADIO_LINK_FAILURE = 2135;
    public static final int RUIM_NOT_PRESENT = 2085;
    public static final int SECURITY_MODE_REJECTED = 2186;
    public static final int SERVICE_NOT_ALLOWED_ON_PLMN = 2129;
    public static final int SERVICE_OPTION_NOT_SUBSCRIBED = 33;
    public static final int SERVICE_OPTION_NOT_SUPPORTED = 32;
    public static final int SERVICE_OPTION_OUT_OF_ORDER = 34;
    public static final int SIGNAL_LOST = -3;
    public static final int SIM_CARD_CHANGED = 2043;
    public static final int SM_MAX_TIME_OUT = 130;
    public static final int SYNCHRONIZATION_FAILURE = 2184;
    public static final int TEST_LOOPBACK_REGULAR_DEACTIVATION = 2196;
    public static final int TETHERED_CALL_ACTIVE = -6;
    public static final int TFT_SEMANTIC_ERROR = 41;
    public static final int TFT_SYTAX_ERROR = 42;
    public static final int THERMAL_EMERGENCY = 2090;
    public static final int THERMAL_MITIGATION = 2062;
    public static final int TRAT_SWAP_FAILED = 2048;
    public static final int UE_INITIATED_DETACH_OR_DISCONNECT = 128;
    public static final int UE_IS_ENTERING_POWERSAVE_MODE = 2226;
    public static final int UE_RAT_CHANGE = 2105;
    public static final int UE_SECURITY_CAPABILITIES_MISMATCH = 2185;
    public static final int UMTS_HANDOVER_TO_IWLAN = 2199;
    public static final int UMTS_REACTIVATION_REQ = 39;
    public static final int UNACCEPTABLE_NETWORK_PARAMETER = 65538;
    public static final int UNACCEPTABLE_NON_EPS_AUTHENTICATION = 2187;
    public static final int UNKNOWN = 65536;
    public static final int UNKNOWN_INFO_ELEMENT = 99;
    public static final int UNKNOWN_PDP_ADDRESS_TYPE = 28;
    public static final int UNKNOWN_PDP_CONTEXT = 43;
    public static final int UNPREFERRED_RAT = 2039;
    public static final int UNSUPPORTED_1X_PREV = 2214;
    public static final int UNSUPPORTED_APN_IN_CURRENT_PLMN = 66;
    public static final int UNSUPPORTED_QCI_VALUE = 59;
    public static final int USER_AUTHENTICATION = 29;
    public static final int VSNCP_ADMINISTRATIVELY_PROHIBITED = 2245;
    public static final int VSNCP_APN_UNATHORIZED = 2238;
    public static final int VSNCP_GEN_ERROR = 2237;
    public static final int VSNCP_INSUFFICIENT_PARAMETERS = 2243;
    public static final int VSNCP_NO_PDN_GATEWAY_ADDRESS = 2240;
    public static final int VSNCP_PDN_EXISTS_FOR_THIS_APN = 2248;
    public static final int VSNCP_PDN_GATEWAY_REJECT = 2242;
    public static final int VSNCP_PDN_GATEWAY_UNREACHABLE = 2241;
    public static final int VSNCP_PDN_ID_IN_USE = 2246;
    public static final int VSNCP_PDN_LIMIT_EXCEEDED = 2239;
    public static final int VSNCP_RECONNECT_NOT_ALLOWED = 2249;
    public static final int VSNCP_RESOURCE_UNAVAILABLE = 2244;
    public static final int VSNCP_SUBSCRIBER_LIMITATION = 2247;
    public static final int VSNCP_TIMEOUT = 2236;
    private static final Map<Integer, String> sFailCauseMap = new HashMap();
    private static final HashMap<Integer, Set<Integer>> sPermanentFailureCache = new HashMap<>();

    @Retention(RetentionPolicy.SOURCE)
    public @interface FailCause {
    }

    static {
        sFailCauseMap.put(0, KeyProperties.DIGEST_NONE);
        sFailCauseMap.put(8, "OPERATOR_BARRED");
        sFailCauseMap.put(14, "NAS_SIGNALLING");
        sFailCauseMap.put(22, "NETWORK_CONGESTION");
        sFailCauseMap.put(25, "LLC_SNDCP");
        sFailCauseMap.put(26, "INSUFFICIENT_RESOURCES");
        sFailCauseMap.put(27, "MISSING_UNKNOWN_APN");
        sFailCauseMap.put(28, "UNKNOWN_PDP_ADDRESS_TYPE");
        sFailCauseMap.put(29, "USER_AUTHENTICATION");
        sFailCauseMap.put(30, "ACTIVATION_REJECT_GGSN");
        sFailCauseMap.put(31, "ACTIVATION_REJECT_UNSPECIFIED");
        sFailCauseMap.put(32, "SERVICE_OPTION_NOT_SUPPORTED");
        sFailCauseMap.put(33, "SERVICE_OPTION_NOT_SUBSCRIBED");
        sFailCauseMap.put(34, "SERVICE_OPTION_OUT_OF_ORDER");
        sFailCauseMap.put(35, "NSAPI_IN_USE");
        sFailCauseMap.put(36, "REGULAR_DEACTIVATION");
        sFailCauseMap.put(37, "QOS_NOT_ACCEPTED");
        sFailCauseMap.put(38, "NETWORK_FAILURE");
        sFailCauseMap.put(39, "UMTS_REACTIVATION_REQ");
        sFailCauseMap.put(40, "FEATURE_NOT_SUPP");
        sFailCauseMap.put(41, "TFT_SEMANTIC_ERROR");
        sFailCauseMap.put(42, "TFT_SYTAX_ERROR");
        sFailCauseMap.put(43, "UNKNOWN_PDP_CONTEXT");
        sFailCauseMap.put(44, "FILTER_SEMANTIC_ERROR");
        sFailCauseMap.put(45, "FILTER_SYTAX_ERROR");
        sFailCauseMap.put(46, "PDP_WITHOUT_ACTIVE_TFT");
        sFailCauseMap.put(48, "ACTIVATION_REJECTED_BCM_VIOLATION");
        sFailCauseMap.put(50, "ONLY_IPV4_ALLOWED");
        sFailCauseMap.put(51, "ONLY_IPV6_ALLOWED");
        sFailCauseMap.put(52, "ONLY_SINGLE_BEARER_ALLOWED");
        sFailCauseMap.put(53, "ESM_INFO_NOT_RECEIVED");
        sFailCauseMap.put(54, "PDN_CONN_DOES_NOT_EXIST");
        sFailCauseMap.put(55, "MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED");
        sFailCauseMap.put(56, "COLLISION_WITH_NETWORK_INITIATED_REQUEST");
        sFailCauseMap.put(57, "ONLY_IPV4V6_ALLOWED");
        sFailCauseMap.put(58, "ONLY_NON_IP_ALLOWED");
        sFailCauseMap.put(59, "UNSUPPORTED_QCI_VALUE");
        sFailCauseMap.put(60, "BEARER_HANDLING_NOT_SUPPORTED");
        sFailCauseMap.put(65, "ACTIVE_PDP_CONTEXT_MAX_NUMBER_REACHED");
        sFailCauseMap.put(66, "UNSUPPORTED_APN_IN_CURRENT_PLMN");
        sFailCauseMap.put(67, "INSUFFICIENT_RESOURCES_FOR_SPECIFIC_SLICE_AND_DNN");
        sFailCauseMap.put(68, "NOT_SUPPORTED_SSC_MODE");
        sFailCauseMap.put(69, "INSUFFICIENT_RESOURCES_FOR_SPECIFIC_SLICE");
        sFailCauseMap.put(70, "MISSING_OR_UNKNOWN_DNN_IN_A_SLICE");
        sFailCauseMap.put(82, "MAX_DATA_RATE_FOR_UP_INT_PROT_IS_TOO_LOW");
        sFailCauseMap.put(83, "QOS_OPERATION_SEMANTIC_ERROR");
        sFailCauseMap.put(84, "QOS_OPERATION_SYTAX_ERROR");
        sFailCauseMap.put(90, "PAYLOAD_WAS_NOT_FORWARDED");
        sFailCauseMap.put(91, "DNN_NOT_SUPPORTED_OR_SUBSCRIBED_IN_SLICE");
        sFailCauseMap.put(81, "INVALID_TRANSACTION_ID");
        sFailCauseMap.put(95, "MESSAGE_INCORRECT_SEMANTIC");
        sFailCauseMap.put(96, "INVALID_MANDATORY_INFO");
        sFailCauseMap.put(97, "MESSAGE_TYPE_UNSUPPORTED");
        sFailCauseMap.put(98, "MSG_TYPE_NONCOMPATIBLE_STATE");
        sFailCauseMap.put(99, "UNKNOWN_INFO_ELEMENT");
        sFailCauseMap.put(100, "CONDITIONAL_IE_ERROR");
        sFailCauseMap.put(101, "MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE");
        sFailCauseMap.put(111, "PROTOCOL_ERRORS");
        sFailCauseMap.put(112, "APN_TYPE_CONFLICT");
        sFailCauseMap.put(113, "INVALID_PCSCF_ADDR");
        sFailCauseMap.put(114, "INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN");
        sFailCauseMap.put(115, "EMM_ACCESS_BARRED");
        sFailCauseMap.put(116, "EMERGENCY_IFACE_ONLY");
        sFailCauseMap.put(117, "IFACE_MISMATCH");
        sFailCauseMap.put(118, "COMPANION_IFACE_IN_USE");
        sFailCauseMap.put(119, "IP_ADDRESS_MISMATCH");
        sFailCauseMap.put(120, "IFACE_AND_POL_FAMILY_MISMATCH");
        sFailCauseMap.put(121, "EMM_ACCESS_BARRED_INFINITE_RETRY");
        sFailCauseMap.put(122, "AUTH_FAILURE_ON_EMERGENCY_CALL");
        sFailCauseMap.put(123, "INVALID_DNS_ADDR");
        sFailCauseMap.put(124, "INVALID_PCSCF_OR_DNS_ADDRESS");
        sFailCauseMap.put(127, "CALL_PREEMPT_BY_EMERGENCY_APN");
        sFailCauseMap.put(128, "UE_INITIATED_DETACH_OR_DISCONNECT");
        sFailCauseMap.put(2000, "MIP_FA_REASON_UNSPECIFIED");
        sFailCauseMap.put(2001, "MIP_FA_ADMIN_PROHIBITED");
        sFailCauseMap.put(2002, "MIP_FA_INSUFFICIENT_RESOURCES");
        sFailCauseMap.put(2003, "MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE");
        sFailCauseMap.put(2004, "MIP_FA_HOME_AGENT_AUTHENTICATION_FAILURE");
        sFailCauseMap.put(2005, "MIP_FA_REQUESTED_LIFETIME_TOO_LONG");
        sFailCauseMap.put(2006, "MIP_FA_MALFORMED_REQUEST");
        sFailCauseMap.put(2007, "MIP_FA_MALFORMED_REPLY");
        sFailCauseMap.put(2008, "MIP_FA_ENCAPSULATION_UNAVAILABLE");
        sFailCauseMap.put(2009, "MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE");
        sFailCauseMap.put(2010, "MIP_FA_REVERSE_TUNNEL_UNAVAILABLE");
        sFailCauseMap.put(2011, "MIP_FA_REVERSE_TUNNEL_IS_MANDATORY");
        sFailCauseMap.put(2012, "MIP_FA_DELIVERY_STYLE_NOT_SUPPORTED");
        sFailCauseMap.put(2013, "MIP_FA_MISSING_NAI");
        sFailCauseMap.put(2014, "MIP_FA_MISSING_HOME_AGENT");
        sFailCauseMap.put(2015, "MIP_FA_MISSING_HOME_ADDRESS");
        sFailCauseMap.put(2016, "MIP_FA_UNKNOWN_CHALLENGE");
        sFailCauseMap.put(2017, "MIP_FA_MISSING_CHALLENGE");
        sFailCauseMap.put(2018, "MIP_FA_STALE_CHALLENGE");
        sFailCauseMap.put(2019, "MIP_HA_REASON_UNSPECIFIED");
        sFailCauseMap.put(2020, "MIP_HA_ADMIN_PROHIBITED");
        sFailCauseMap.put(2021, "MIP_HA_INSUFFICIENT_RESOURCES");
        sFailCauseMap.put(2022, "MIP_HA_MOBILE_NODE_AUTHENTICATION_FAILURE");
        sFailCauseMap.put(2023, "MIP_HA_FOREIGN_AGENT_AUTHENTICATION_FAILURE");
        sFailCauseMap.put(2024, "MIP_HA_REGISTRATION_ID_MISMATCH");
        sFailCauseMap.put(2025, "MIP_HA_MALFORMED_REQUEST");
        sFailCauseMap.put(2026, "MIP_HA_UNKNOWN_HOME_AGENT_ADDRESS");
        sFailCauseMap.put(2027, "MIP_HA_REVERSE_TUNNEL_UNAVAILABLE");
        sFailCauseMap.put(2028, "MIP_HA_REVERSE_TUNNEL_IS_MANDATORY");
        sFailCauseMap.put(2029, "MIP_HA_ENCAPSULATION_UNAVAILABLE");
        sFailCauseMap.put(2030, "CLOSE_IN_PROGRESS");
        sFailCauseMap.put(2031, "NETWORK_INITIATED_TERMINATION");
        sFailCauseMap.put(2032, "MODEM_APP_PREEMPTED");
        sFailCauseMap.put(2033, "PDN_IPV4_CALL_DISALLOWED");
        sFailCauseMap.put(2034, "PDN_IPV4_CALL_THROTTLED");
        sFailCauseMap.put(2035, "PDN_IPV6_CALL_DISALLOWED");
        sFailCauseMap.put(2036, "PDN_IPV6_CALL_THROTTLED");
        sFailCauseMap.put(2037, "MODEM_RESTART");
        sFailCauseMap.put(2038, "PDP_PPP_NOT_SUPPORTED");
        sFailCauseMap.put(2039, "UNPREFERRED_RAT");
        sFailCauseMap.put(2040, "PHYSICAL_LINK_CLOSE_IN_PROGRESS");
        sFailCauseMap.put(2041, "APN_PENDING_HANDOVER");
        sFailCauseMap.put(2042, "PROFILE_BEARER_INCOMPATIBLE");
        sFailCauseMap.put(2043, "SIM_CARD_CHANGED");
        sFailCauseMap.put(2044, "LOW_POWER_MODE_OR_POWERING_DOWN");
        sFailCauseMap.put(2045, "APN_DISABLED");
        sFailCauseMap.put(2046, "MAX_PPP_INACTIVITY_TIMER_EXPIRED");
        sFailCauseMap.put(2047, "IPV6_ADDRESS_TRANSFER_FAILED");
        sFailCauseMap.put(2048, "TRAT_SWAP_FAILED");
        sFailCauseMap.put(2049, "EHRPD_TO_HRPD_FALLBACK");
        sFailCauseMap.put(2050, "MIP_CONFIG_FAILURE");
        sFailCauseMap.put(2051, "PDN_INACTIVITY_TIMER_EXPIRED");
        sFailCauseMap.put(2052, "MAX_IPV4_CONNECTIONS");
        sFailCauseMap.put(2053, "MAX_IPV6_CONNECTIONS");
        sFailCauseMap.put(2054, "APN_MISMATCH");
        sFailCauseMap.put(2055, "IP_VERSION_MISMATCH");
        sFailCauseMap.put(2056, "DUN_CALL_DISALLOWED");
        sFailCauseMap.put(2057, "INTERNAL_EPC_NONEPC_TRANSITION");
        sFailCauseMap.put(2058, "INTERFACE_IN_USE");
        sFailCauseMap.put(2059, "APN_DISALLOWED_ON_ROAMING");
        sFailCauseMap.put(2060, "APN_PARAMETERS_CHANGED");
        sFailCauseMap.put(2061, "NULL_APN_DISALLOWED");
        sFailCauseMap.put(2062, "THERMAL_MITIGATION");
        sFailCauseMap.put(2063, "DATA_SETTINGS_DISABLED");
        sFailCauseMap.put(2064, "DATA_ROAMING_SETTINGS_DISABLED");
        sFailCauseMap.put(2065, "DDS_SWITCHED");
        sFailCauseMap.put(2066, "FORBIDDEN_APN_NAME");
        sFailCauseMap.put(2067, "DDS_SWITCH_IN_PROGRESS");
        sFailCauseMap.put(2068, "CALL_DISALLOWED_IN_ROAMING");
        sFailCauseMap.put(2069, "NON_IP_NOT_SUPPORTED");
        sFailCauseMap.put(2070, "PDN_NON_IP_CALL_THROTTLED");
        sFailCauseMap.put(2071, "PDN_NON_IP_CALL_DISALLOWED");
        sFailCauseMap.put(2072, "CDMA_LOCK");
        sFailCauseMap.put(2073, "CDMA_INTERCEPT");
        sFailCauseMap.put(2074, "CDMA_REORDER");
        sFailCauseMap.put(2075, "CDMA_RELEASE_DUE_TO_SO_REJECTION");
        sFailCauseMap.put(2076, "CDMA_INCOMING_CALL");
        sFailCauseMap.put(2077, "CDMA_ALERT_STOP");
        sFailCauseMap.put(2078, "CHANNEL_ACQUISITION_FAILURE");
        sFailCauseMap.put(2079, "MAX_ACCESS_PROBE");
        sFailCauseMap.put(2080, "CONCURRENT_SERVICE_NOT_SUPPORTED_BY_BASE_STATION");
        sFailCauseMap.put(2081, "NO_RESPONSE_FROM_BASE_STATION");
        sFailCauseMap.put(2082, "REJECTED_BY_BASE_STATION");
        sFailCauseMap.put(2083, "CONCURRENT_SERVICES_INCOMPATIBLE");
        sFailCauseMap.put(2084, "NO_CDMA_SERVICE");
        sFailCauseMap.put(2085, "RUIM_NOT_PRESENT");
        sFailCauseMap.put(2086, "CDMA_RETRY_ORDER");
        sFailCauseMap.put(2087, "ACCESS_BLOCK");
        sFailCauseMap.put(2088, "ACCESS_BLOCK_ALL");
        sFailCauseMap.put(2089, "IS707B_MAX_ACCESS_PROBES");
        sFailCauseMap.put(2090, "THERMAL_EMERGENCY");
        sFailCauseMap.put(2091, "CONCURRENT_SERVICES_NOT_ALLOWED");
        sFailCauseMap.put(2092, "INCOMING_CALL_REJECTED");
        sFailCauseMap.put(2093, "NO_SERVICE_ON_GATEWAY");
        sFailCauseMap.put(2094, "NO_GPRS_CONTEXT");
        sFailCauseMap.put(2095, "ILLEGAL_MS");
        sFailCauseMap.put(2096, "ILLEGAL_ME");
        sFailCauseMap.put(2097, "GPRS_SERVICES_AND_NON_GPRS_SERVICES_NOT_ALLOWED");
        sFailCauseMap.put(2098, "GPRS_SERVICES_NOT_ALLOWED");
        sFailCauseMap.put(2099, "MS_IDENTITY_CANNOT_BE_DERIVED_BY_THE_NETWORK");
        sFailCauseMap.put(2100, "IMPLICITLY_DETACHED");
        sFailCauseMap.put(2101, "PLMN_NOT_ALLOWED");
        sFailCauseMap.put(2102, "LOCATION_AREA_NOT_ALLOWED");
        sFailCauseMap.put(2103, "GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN");
        sFailCauseMap.put(2104, "PDP_DUPLICATE");
        sFailCauseMap.put(2105, "UE_RAT_CHANGE");
        sFailCauseMap.put(2106, "CONGESTION");
        sFailCauseMap.put(2107, "NO_PDP_CONTEXT_ACTIVATED");
        sFailCauseMap.put(2108, "ACCESS_CLASS_DSAC_REJECTION");
        sFailCauseMap.put(2109, "PDP_ACTIVATE_MAX_RETRY_FAILED");
        sFailCauseMap.put(2110, "RADIO_ACCESS_BEARER_FAILURE");
        sFailCauseMap.put(2111, "ESM_UNKNOWN_EPS_BEARER_CONTEXT");
        sFailCauseMap.put(2112, "DRB_RELEASED_BY_RRC");
        sFailCauseMap.put(2113, "CONNECTION_RELEASED");
        sFailCauseMap.put(2114, "EMM_DETACHED");
        sFailCauseMap.put(2115, "EMM_ATTACH_FAILED");
        sFailCauseMap.put(2116, "EMM_ATTACH_STARTED");
        sFailCauseMap.put(2117, "LTE_NAS_SERVICE_REQUEST_FAILED");
        sFailCauseMap.put(2118, "DUPLICATE_BEARER_ID");
        sFailCauseMap.put(2119, "ESM_COLLISION_SCENARIOS");
        sFailCauseMap.put(2120, "ESM_BEARER_DEACTIVATED_TO_SYNC_WITH_NETWORK");
        sFailCauseMap.put(2121, "ESM_NW_ACTIVATED_DED_BEARER_WITH_ID_OF_DEF_BEARER");
        sFailCauseMap.put(2122, "ESM_BAD_OTA_MESSAGE");
        sFailCauseMap.put(2123, "ESM_DOWNLOAD_SERVER_REJECTED_THE_CALL");
        sFailCauseMap.put(2124, "ESM_CONTEXT_TRANSFERRED_DUE_TO_IRAT");
        sFailCauseMap.put(2125, "DS_EXPLICIT_DEACTIVATION");
        sFailCauseMap.put(2126, "ESM_LOCAL_CAUSE_NONE");
        sFailCauseMap.put(2127, "LTE_THROTTLING_NOT_REQUIRED");
        sFailCauseMap.put(2128, "ACCESS_CONTROL_LIST_CHECK_FAILURE");
        sFailCauseMap.put(2129, "SERVICE_NOT_ALLOWED_ON_PLMN");
        sFailCauseMap.put(2130, "EMM_T3417_EXPIRED");
        sFailCauseMap.put(2131, "EMM_T3417_EXT_EXPIRED");
        sFailCauseMap.put(2132, "RRC_UPLINK_DATA_TRANSMISSION_FAILURE");
        sFailCauseMap.put(2133, "RRC_UPLINK_DELIVERY_FAILED_DUE_TO_HANDOVER");
        sFailCauseMap.put(2134, "RRC_UPLINK_CONNECTION_RELEASE");
        sFailCauseMap.put(2135, "RRC_UPLINK_RADIO_LINK_FAILURE");
        sFailCauseMap.put(2136, "RRC_UPLINK_ERROR_REQUEST_FROM_NAS");
        sFailCauseMap.put(2137, "RRC_CONNECTION_ACCESS_STRATUM_FAILURE");
        sFailCauseMap.put(2138, "RRC_CONNECTION_ANOTHER_PROCEDURE_IN_PROGRESS");
        sFailCauseMap.put(2139, "RRC_CONNECTION_ACCESS_BARRED");
        sFailCauseMap.put(2140, "RRC_CONNECTION_CELL_RESELECTION");
        sFailCauseMap.put(2141, "RRC_CONNECTION_CONFIG_FAILURE");
        sFailCauseMap.put(2142, "RRC_CONNECTION_TIMER_EXPIRED");
        sFailCauseMap.put(2143, "RRC_CONNECTION_LINK_FAILURE");
        sFailCauseMap.put(2144, "RRC_CONNECTION_CELL_NOT_CAMPED");
        sFailCauseMap.put(2145, "RRC_CONNECTION_SYSTEM_INTERVAL_FAILURE");
        sFailCauseMap.put(2146, "RRC_CONNECTION_REJECT_BY_NETWORK");
        sFailCauseMap.put(2147, "RRC_CONNECTION_NORMAL_RELEASE");
        sFailCauseMap.put(2148, "RRC_CONNECTION_RADIO_LINK_FAILURE");
        sFailCauseMap.put(2149, "RRC_CONNECTION_REESTABLISHMENT_FAILURE");
        sFailCauseMap.put(2150, "RRC_CONNECTION_OUT_OF_SERVICE_DURING_CELL_REGISTER");
        sFailCauseMap.put(2151, "RRC_CONNECTION_ABORT_REQUEST");
        sFailCauseMap.put(2152, "RRC_CONNECTION_SYSTEM_INFORMATION_BLOCK_READ_ERROR");
        sFailCauseMap.put(2153, "NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH");
        sFailCauseMap.put(2154, "NETWORK_INITIATED_DETACH_NO_AUTO_REATTACH");
        sFailCauseMap.put(2155, "ESM_PROCEDURE_TIME_OUT");
        sFailCauseMap.put(2156, "INVALID_CONNECTION_ID");
        sFailCauseMap.put(2157, "MAXIMIUM_NSAPIS_EXCEEDED");
        sFailCauseMap.put(2158, "INVALID_PRIMARY_NSAPI");
        sFailCauseMap.put(2159, "CANNOT_ENCODE_OTA_MESSAGE");
        sFailCauseMap.put(2160, "RADIO_ACCESS_BEARER_SETUP_FAILURE");
        sFailCauseMap.put(2161, "PDP_ESTABLISH_TIMEOUT_EXPIRED");
        sFailCauseMap.put(2162, "PDP_MODIFY_TIMEOUT_EXPIRED");
        sFailCauseMap.put(2163, "PDP_INACTIVE_TIMEOUT_EXPIRED");
        sFailCauseMap.put(2164, "PDP_LOWERLAYER_ERROR");
        sFailCauseMap.put(2165, "PDP_MODIFY_COLLISION");
        sFailCauseMap.put(2166, "MAXINUM_SIZE_OF_L2_MESSAGE_EXCEEDED");
        sFailCauseMap.put(2167, "NAS_REQUEST_REJECTED_BY_NETWORK");
        sFailCauseMap.put(2168, "RRC_CONNECTION_INVALID_REQUEST");
        sFailCauseMap.put(2169, "RRC_CONNECTION_TRACKING_AREA_ID_CHANGED");
        sFailCauseMap.put(2170, "RRC_CONNECTION_RF_UNAVAILABLE");
        sFailCauseMap.put(2171, "RRC_CONNECTION_ABORTED_DUE_TO_IRAT_CHANGE");
        sFailCauseMap.put(2172, "RRC_CONNECTION_RELEASED_SECURITY_NOT_ACTIVE");
        sFailCauseMap.put(2173, "RRC_CONNECTION_ABORTED_AFTER_HANDOVER");
        sFailCauseMap.put(2174, "RRC_CONNECTION_ABORTED_AFTER_IRAT_CELL_CHANGE");
        sFailCauseMap.put(2175, "RRC_CONNECTION_ABORTED_DURING_IRAT_CELL_CHANGE");
        sFailCauseMap.put(2176, "IMSI_UNKNOWN_IN_HOME_SUBSCRIBER_SERVER");
        sFailCauseMap.put(2177, "IMEI_NOT_ACCEPTED");
        sFailCauseMap.put(2178, "EPS_SERVICES_AND_NON_EPS_SERVICES_NOT_ALLOWED");
        sFailCauseMap.put(2179, "EPS_SERVICES_NOT_ALLOWED_IN_PLMN");
        sFailCauseMap.put(2180, "MSC_TEMPORARILY_NOT_REACHABLE");
        sFailCauseMap.put(2181, "CS_DOMAIN_NOT_AVAILABLE");
        sFailCauseMap.put(2182, "ESM_FAILURE");
        sFailCauseMap.put(2183, "MAC_FAILURE");
        sFailCauseMap.put(2184, "SYNCHRONIZATION_FAILURE");
        sFailCauseMap.put(2185, "UE_SECURITY_CAPABILITIES_MISMATCH");
        sFailCauseMap.put(2186, "SECURITY_MODE_REJECTED");
        sFailCauseMap.put(2187, "UNACCEPTABLE_NON_EPS_AUTHENTICATION");
        sFailCauseMap.put(2188, "CS_FALLBACK_CALL_ESTABLISHMENT_NOT_ALLOWED");
        sFailCauseMap.put(2189, "NO_EPS_BEARER_CONTEXT_ACTIVATED");
        sFailCauseMap.put(2190, "INVALID_EMM_STATE");
        sFailCauseMap.put(2191, "NAS_LAYER_FAILURE");
        sFailCauseMap.put(2192, "MULTIPLE_PDP_CALL_NOT_ALLOWED");
        sFailCauseMap.put(2193, "EMBMS_NOT_ENABLED");
        sFailCauseMap.put(2194, "IRAT_HANDOVER_FAILED");
        sFailCauseMap.put(2195, "EMBMS_REGULAR_DEACTIVATION");
        sFailCauseMap.put(2196, "TEST_LOOPBACK_REGULAR_DEACTIVATION");
        sFailCauseMap.put(2197, "LOWER_LAYER_REGISTRATION_FAILURE");
        sFailCauseMap.put(2198, "DATA_PLAN_EXPIRED");
        sFailCauseMap.put(2199, "UMTS_HANDOVER_TO_IWLAN");
        sFailCauseMap.put(2200, "EVDO_CONNECTION_DENY_BY_GENERAL_OR_NETWORK_BUSY");
        sFailCauseMap.put(2201, "EVDO_CONNECTION_DENY_BY_BILLING_OR_AUTHENTICATION_FAILURE");
        sFailCauseMap.put(2202, "EVDO_HDR_CHANGED");
        sFailCauseMap.put(2203, "EVDO_HDR_EXITED");
        sFailCauseMap.put(2204, "EVDO_HDR_NO_SESSION");
        sFailCauseMap.put(2205, "EVDO_USING_GPS_FIX_INSTEAD_OF_HDR_CALL");
        sFailCauseMap.put(2206, "EVDO_HDR_CONNECTION_SETUP_TIMEOUT");
        sFailCauseMap.put(2207, "FAILED_TO_ACQUIRE_COLOCATED_HDR");
        sFailCauseMap.put(2208, "OTASP_COMMIT_IN_PROGRESS");
        sFailCauseMap.put(2209, "NO_HYBRID_HDR_SERVICE");
        sFailCauseMap.put(2210, "HDR_NO_LOCK_GRANTED");
        sFailCauseMap.put(2211, "DBM_OR_SMS_IN_PROGRESS");
        sFailCauseMap.put(2212, "HDR_FADE");
        sFailCauseMap.put(2213, "HDR_ACCESS_FAILURE");
        sFailCauseMap.put(2214, "UNSUPPORTED_1X_PREV");
        sFailCauseMap.put(2215, "LOCAL_END");
        sFailCauseMap.put(2216, "NO_SERVICE");
        sFailCauseMap.put(2217, "FADE");
        sFailCauseMap.put(2218, "NORMAL_RELEASE");
        sFailCauseMap.put(2219, "ACCESS_ATTEMPT_ALREADY_IN_PROGRESS");
        sFailCauseMap.put(2220, "REDIRECTION_OR_HANDOFF_IN_PROGRESS");
        sFailCauseMap.put(2221, "EMERGENCY_MODE");
        sFailCauseMap.put(2222, "PHONE_IN_USE");
        sFailCauseMap.put(2223, "INVALID_MODE");
        sFailCauseMap.put(2224, "INVALID_SIM_STATE");
        sFailCauseMap.put(2225, "NO_COLLOCATED_HDR");
        sFailCauseMap.put(2226, "UE_IS_ENTERING_POWERSAVE_MODE");
        sFailCauseMap.put(2227, "DUAL_SWITCH");
        sFailCauseMap.put(2228, "PPP_TIMEOUT");
        sFailCauseMap.put(2229, "PPP_AUTH_FAILURE");
        sFailCauseMap.put(2230, "PPP_OPTION_MISMATCH");
        sFailCauseMap.put(2231, "PPP_PAP_FAILURE");
        sFailCauseMap.put(2232, "PPP_CHAP_FAILURE");
        sFailCauseMap.put(2233, "PPP_CLOSE_IN_PROGRESS");
        sFailCauseMap.put(2234, "LIMITED_TO_IPV4");
        sFailCauseMap.put(2235, "LIMITED_TO_IPV6");
        sFailCauseMap.put(2236, "VSNCP_TIMEOUT");
        sFailCauseMap.put(2237, "VSNCP_GEN_ERROR");
        sFailCauseMap.put(2238, "VSNCP_APN_UNATHORIZED");
        sFailCauseMap.put(2239, "VSNCP_PDN_LIMIT_EXCEEDED");
        sFailCauseMap.put(2240, "VSNCP_NO_PDN_GATEWAY_ADDRESS");
        sFailCauseMap.put(2241, "VSNCP_PDN_GATEWAY_UNREACHABLE");
        sFailCauseMap.put(2242, "VSNCP_PDN_GATEWAY_REJECT");
        sFailCauseMap.put(2243, "VSNCP_INSUFFICIENT_PARAMETERS");
        sFailCauseMap.put(2244, "VSNCP_RESOURCE_UNAVAILABLE");
        sFailCauseMap.put(2245, "VSNCP_ADMINISTRATIVELY_PROHIBITED");
        sFailCauseMap.put(2246, "VSNCP_PDN_ID_IN_USE");
        sFailCauseMap.put(2247, "VSNCP_SUBSCRIBER_LIMITATION");
        sFailCauseMap.put(2248, "VSNCP_PDN_EXISTS_FOR_THIS_APN");
        sFailCauseMap.put(2249, "VSNCP_RECONNECT_NOT_ALLOWED");
        sFailCauseMap.put(2250, "IPV6_PREFIX_UNAVAILABLE");
        sFailCauseMap.put(2251, "HANDOFF_PREFERENCE_CHANGED");
        sFailCauseMap.put(4097, "OEM_DCFAILCAUSE_1");
        sFailCauseMap.put(4098, "OEM_DCFAILCAUSE_2");
        sFailCauseMap.put(4099, "OEM_DCFAILCAUSE_3");
        sFailCauseMap.put(4100, "OEM_DCFAILCAUSE_4");
        sFailCauseMap.put(4101, "OEM_DCFAILCAUSE_5");
        sFailCauseMap.put(4102, "OEM_DCFAILCAUSE_6");
        sFailCauseMap.put(4103, "OEM_DCFAILCAUSE_7");
        sFailCauseMap.put(4104, "OEM_DCFAILCAUSE_8");
        sFailCauseMap.put(4105, "OEM_DCFAILCAUSE_9");
        sFailCauseMap.put(4106, "OEM_DCFAILCAUSE_10");
        sFailCauseMap.put(4107, "OEM_DCFAILCAUSE_11");
        sFailCauseMap.put(4108, "OEM_DCFAILCAUSE_12");
        sFailCauseMap.put(4109, "OEM_DCFAILCAUSE_13");
        sFailCauseMap.put(4110, "OEM_DCFAILCAUSE_14");
        sFailCauseMap.put(4111, "OEM_DCFAILCAUSE_15");
        sFailCauseMap.put(-1, "REGISTRATION_FAIL");
        sFailCauseMap.put(-2, "GPRS_REGISTRATION_FAIL");
        sFailCauseMap.put(-3, "SIGNAL_LOST");
        sFailCauseMap.put(-4, "PREF_RADIO_TECH_CHANGED");
        sFailCauseMap.put(-5, "RADIO_POWER_OFF");
        sFailCauseMap.put(-6, "TETHERED_CALL_ACTIVE");
        sFailCauseMap.put(65535, "ERROR_UNSPECIFIED");
        sFailCauseMap.put(65536, IccCardConstants.INTENT_VALUE_ICC_UNKNOWN);
        sFailCauseMap.put(65537, "RADIO_NOT_AVAILABLE");
        sFailCauseMap.put(65538, "UNACCEPTABLE_NETWORK_PARAMETER");
        sFailCauseMap.put(65539, "CONNECTION_TO_DATACONNECTIONAC_BROKEN");
        sFailCauseMap.put(65540, "LOST_CONNECTION");
        sFailCauseMap.put(65541, "RESET_BY_FRAMEWORK");
        sFailCauseMap.put(2304, "PDP_ACTIVE_LIMIT");
        sFailCauseMap.put(130, "SM_MAX_TIME_OUT");
        sFailCauseMap.put(Integer.valueOf((int) EHSM_PPP_ERROR_CONN_EXISTS_FOR_THIS_APN), "EHSM_ERR_CONN_EXISTS_FOR_THIS_APN");
        sFailCauseMap.put(3199, "DSM_UNKNOWN");
        sFailCauseMap.put(Integer.valueOf((int) NRSM_T3580_MAX_TIME_OUT), "NRSM_T3580_MAX_TIME_OUT");
    }

    private DataFailCause() {
    }

    public static boolean isRadioRestartFailure(Context context, int cause, int subId) {
        PersistableBundle b;
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        if (configManager == null || (b = configManager.getConfigForSubId(subId)) == null) {
            return false;
        }
        if (cause == 36 && b.getBoolean(CarrierConfigManager.KEY_RESTART_RADIO_ON_PDP_FAIL_REGULAR_DEACTIVATION_BOOL)) {
            return true;
        }
        int[] causeCodes = b.getIntArray(CarrierConfigManager.KEY_RADIO_RESTART_FAILURE_CAUSES_INT_ARRAY);
        if (causeCodes != null) {
            return Arrays.stream(causeCodes).anyMatch(new IntPredicate(cause) {
                /* class android.telephony.$$Lambda$DataFailCause$djkZSxdGsw2L5rQKiGu6OudyY */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.IntPredicate
                public final boolean test(int i) {
                    return DataFailCause.lambda$isRadioRestartFailure$0(this.f$0, i);
                }
            });
        }
        return false;
    }

    static /* synthetic */ boolean lambda$isRadioRestartFailure$0(int cause, int i) {
        return i == cause;
    }

    public static boolean isPermanentFailure(Context context, int failCause, int subId) {
        boolean contains;
        PersistableBundle b;
        String[] permanentFailureStrings;
        if (isMatchedDsFail(failCause)) {
            return false;
        }
        if (isMatchedDsPermanentFail(failCause)) {
            return true;
        }
        synchronized (sPermanentFailureCache) {
            Set<Integer> permanentFailureSet = sPermanentFailureCache.get(Integer.valueOf(subId));
            if (permanentFailureSet == null) {
                CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService(Context.CARRIER_CONFIG_SERVICE);
                if (!(configManager == null || (b = configManager.getConfigForSubId(subId)) == null || (permanentFailureStrings = b.getStringArray(CarrierConfigManager.KEY_CARRIER_DATA_CALL_PERMANENT_FAILURE_STRINGS)) == null)) {
                    permanentFailureSet = new HashSet();
                    for (Map.Entry<Integer, String> e : sFailCauseMap.entrySet()) {
                        if (ArrayUtils.contains(permanentFailureStrings, e.getValue())) {
                            permanentFailureSet.add(e.getKey());
                        }
                    }
                }
                if (permanentFailureSet == null) {
                    permanentFailureSet = new HashSet<Integer>() {
                        /* class android.telephony.DataFailCause.AnonymousClass1 */

                        {
                            add(8);
                            add(27);
                            add(28);
                            add(29);
                            add(30);
                            add(32);
                            add(33);
                            add(35);
                            add(50);
                            add(51);
                            add(111);
                            add(-5);
                            add(-6);
                            add(65537);
                            add(65538);
                            add(-3);
                        }
                    };
                }
                sPermanentFailureCache.put(Integer.valueOf(subId), permanentFailureSet);
            }
            contains = permanentFailureSet.contains(Integer.valueOf(failCause));
        }
        return contains;
    }

    public static boolean isFailCauseValid(int failCause) {
        return failCause == getFailCause(failCause);
    }

    public static boolean isEventLoggable(int dataFailCause) {
        return dataFailCause == 8 || dataFailCause == 26 || dataFailCause == 28 || dataFailCause == 29 || dataFailCause == 30 || dataFailCause == 31 || dataFailCause == 33 || dataFailCause == 32 || dataFailCause == 34 || dataFailCause == 35 || dataFailCause == 50 || dataFailCause == 51 || dataFailCause == 111 || dataFailCause == -3 || dataFailCause == -5 || dataFailCause == -6 || dataFailCause == 65538;
    }

    public static String toString(int dataFailCause) {
        int cause = getFailCause(dataFailCause);
        if (cause != 65536) {
            return sFailCauseMap.get(Integer.valueOf(cause));
        }
        return "UNKNOWN(" + dataFailCause + ")";
    }

    public static int getFailCause(int failCause) {
        if (sFailCauseMap.containsKey(Integer.valueOf(failCause))) {
            return failCause;
        }
        return 65536;
    }

    private static boolean isMatchedDsFail(int failCause) {
        boolean isMatched = false;
        try {
            String cntelfailcau = SystemProperties.get("ro.hwpp_ds_fail", "");
            Rlog.d("DcFailCause", "isMatchedDsFail cntelfailcau: " + cntelfailcau);
            for (String fcau : cntelfailcau.split(SmsManager.REGEX_PREFIX_DELIMITER)) {
                if (Integer.toString(failCause).equals(fcau)) {
                    Rlog.d("DcFailCause", "ErrorCode has been matched: " + failCause);
                    isMatched = true;
                }
            }
        } catch (Exception ex) {
            Rlog.e("DcFailCause", "Exception isMatchedDsFail get ds fail cause, ", ex);
        }
        return isMatched;
    }

    private static boolean isMatchedDsPermanentFail(int failCause) {
        boolean isMatched = false;
        try {
            Rlog.d("DcFailCause", "isMatchedDsPermanentFail CUST_PSPERMANENT_FAILURE: " + CUST_PSPERMANENT_FAILURE);
            for (String fcau : CUST_PSPERMANENT_FAILURE.split(SmsManager.REGEX_PREFIX_DELIMITER)) {
                if (Integer.toString(failCause).equals(fcau)) {
                    Rlog.d("DcFailCause", "isMatchedDsPermanentFail, ErrorCode has been matched: " + failCause);
                    isMatched = true;
                }
            }
        } catch (Exception e) {
            Rlog.e("DcFailCause", "Exception get ds Permanent fail cause: ", e);
        }
        return isMatched;
    }
}

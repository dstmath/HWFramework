package android.hardware.radio.V1_4;

import android.security.keystore.KeyProperties;
import java.util.ArrayList;

public final class DataCallFailCause {
    public static final int ACCESS_ATTEMPT_ALREADY_IN_PROGRESS = 2219;
    public static final int ACCESS_BLOCK = 2087;
    public static final int ACCESS_BLOCK_ALL = 2088;
    public static final int ACCESS_CLASS_DSAC_REJECTION = 2108;
    public static final int ACCESS_CONTROL_LIST_CHECK_FAILURE = 2128;
    public static final int ACTIVATION_REJECTED_BCM_VIOLATION = 48;
    public static final int ACTIVATION_REJECT_GGSN = 30;
    public static final int ACTIVATION_REJECT_UNSPECIFIED = 31;
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
    public static final int CS_DOMAIN_NOT_AVAILABLE = 2181;
    public static final int CS_FALLBACK_CALL_ESTABLISHMENT_NOT_ALLOWED = 2188;
    public static final int DATA_PLAN_EXPIRED = 2198;
    public static final int DATA_REGISTRATION_FAIL = -2;
    public static final int DATA_ROAMING_SETTINGS_DISABLED = 2064;
    public static final int DATA_SETTINGS_DISABLED = 2063;
    public static final int DBM_OR_SMS_IN_PROGRESS = 2211;
    public static final int DDS_SWITCHED = 2065;
    public static final int DDS_SWITCH_IN_PROGRESS = 2067;
    public static final int DRB_RELEASED_BY_RRC = 2112;
    public static final int DS_EXPLICIT_DEACTIVATION = 2125;
    public static final int DUAL_SWITCH = 2227;
    public static final int DUN_CALL_DISALLOWED = 2056;
    public static final int DUPLICATE_BEARER_ID = 2118;
    public static final int EHRPD_TO_HRPD_FALLBACK = 2049;
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
    public static final int GPRS_SERVICES_AND_NON_GPRS_SERVICES_NOT_ALLOWED = 2097;
    public static final int GPRS_SERVICES_NOT_ALLOWED = 2098;
    public static final int GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN = 2103;
    public static final int HANDOFF_PREFERENCE_CHANGED = 2251;
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
    public static final int LOWER_LAYER_REGISTRATION_FAILURE = 2197;
    public static final int LOW_POWER_MODE_OR_POWERING_DOWN = 2044;
    public static final int LTE_NAS_SERVICE_REQUEST_FAILED = 2117;
    public static final int LTE_THROTTLING_NOT_REQUIRED = 2127;
    public static final int MAC_FAILURE = 2183;
    public static final int MAXIMIUM_NSAPIS_EXCEEDED = 2157;
    public static final int MAXINUM_SIZE_OF_L2_MESSAGE_EXCEEDED = 2166;
    public static final int MAX_ACCESS_PROBE = 2079;
    public static final int MAX_ACTIVE_PDP_CONTEXT_REACHED = 65;
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
    public static final int MISSING_UKNOWN_APN = 27;
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
    public static final int NETWORK_FAILURE = 38;
    public static final int NETWORK_INITIATED_DETACH_NO_AUTO_REATTACH = 2154;
    public static final int NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH = 2153;
    public static final int NETWORK_INITIATED_TERMINATION = 2031;
    public static final int NONE = 0;
    public static final int NON_IP_NOT_SUPPORTED = 2069;
    public static final int NORMAL_RELEASE = 2218;
    public static final int NO_CDMA_SERVICE = 2084;
    public static final int NO_COLLOCATED_HDR = 2225;
    public static final int NO_EPS_BEARER_CONTEXT_ACTIVATED = 2189;
    public static final int NO_GPRS_CONTEXT = 2094;
    public static final int NO_HYBRID_HDR_SERVICE = 2209;
    public static final int NO_PDP_CONTEXT_ACTIVATED = 2107;
    public static final int NO_RESPONSE_FROM_BASE_STATION = 2081;
    public static final int NO_SERVICE = 2216;
    public static final int NO_SERVICE_ON_GATEWAY = 2093;
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
    public static final int PDN_CONN_DOES_NOT_EXIST = 54;
    public static final int PDN_INACTIVITY_TIMER_EXPIRED = 2051;
    public static final int PDN_IPV4_CALL_DISALLOWED = 2033;
    public static final int PDN_IPV4_CALL_THROTTLED = 2034;
    public static final int PDN_IPV6_CALL_DISALLOWED = 2035;
    public static final int PDN_IPV6_CALL_THROTTLED = 2036;
    public static final int PDN_NON_IP_CALL_DISALLOWED = 2071;
    public static final int PDN_NON_IP_CALL_THROTTLED = 2070;
    public static final int PDP_ACTIVATE_MAX_RETRY_FAILED = 2109;
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
    public static final int RADIO_ACCESS_BEARER_FAILURE = 2110;
    public static final int RADIO_ACCESS_BEARER_SETUP_FAILURE = 2160;
    public static final int RADIO_POWER_OFF = -5;
    public static final int REDIRECTION_OR_HANDOFF_IN_PROGRESS = 2220;
    public static final int REGULAR_DEACTIVATION = 36;
    public static final int REJECTED_BY_BASE_STATION = 2082;
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
    public static final int UNACCEPTABLE_NON_EPS_AUTHENTICATION = 2187;
    public static final int UNKNOWN_INFO_ELEMENT = 99;
    public static final int UNKNOWN_PDP_ADDRESS_TYPE = 28;
    public static final int UNKNOWN_PDP_CONTEXT = 43;
    public static final int UNPREFERRED_RAT = 2039;
    public static final int UNSUPPORTED_1X_PREV = 2214;
    public static final int UNSUPPORTED_APN_IN_CURRENT_PLMN = 66;
    public static final int UNSUPPORTED_QCI_VALUE = 59;
    public static final int USER_AUTHENTICATION = 29;
    public static final int VOICE_REGISTRATION_FAIL = -1;
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

    public static final String toString(int o) {
        if (o == 0) {
            return KeyProperties.DIGEST_NONE;
        }
        if (o == 8) {
            return "OPERATOR_BARRED";
        }
        if (o == 14) {
            return "NAS_SIGNALLING";
        }
        if (o == 26) {
            return "INSUFFICIENT_RESOURCES";
        }
        if (o == 27) {
            return "MISSING_UKNOWN_APN";
        }
        if (o == 28) {
            return "UNKNOWN_PDP_ADDRESS_TYPE";
        }
        if (o == 29) {
            return "USER_AUTHENTICATION";
        }
        if (o == 30) {
            return "ACTIVATION_REJECT_GGSN";
        }
        if (o == 31) {
            return "ACTIVATION_REJECT_UNSPECIFIED";
        }
        if (o == 32) {
            return "SERVICE_OPTION_NOT_SUPPORTED";
        }
        if (o == 33) {
            return "SERVICE_OPTION_NOT_SUBSCRIBED";
        }
        if (o == 34) {
            return "SERVICE_OPTION_OUT_OF_ORDER";
        }
        if (o == 35) {
            return "NSAPI_IN_USE";
        }
        if (o == 36) {
            return "REGULAR_DEACTIVATION";
        }
        if (o == 37) {
            return "QOS_NOT_ACCEPTED";
        }
        if (o == 38) {
            return "NETWORK_FAILURE";
        }
        if (o == 39) {
            return "UMTS_REACTIVATION_REQ";
        }
        if (o == 40) {
            return "FEATURE_NOT_SUPP";
        }
        if (o == 41) {
            return "TFT_SEMANTIC_ERROR";
        }
        if (o == 42) {
            return "TFT_SYTAX_ERROR";
        }
        if (o == 43) {
            return "UNKNOWN_PDP_CONTEXT";
        }
        if (o == 44) {
            return "FILTER_SEMANTIC_ERROR";
        }
        if (o == 45) {
            return "FILTER_SYTAX_ERROR";
        }
        if (o == 46) {
            return "PDP_WITHOUT_ACTIVE_TFT";
        }
        if (o == 50) {
            return "ONLY_IPV4_ALLOWED";
        }
        if (o == 51) {
            return "ONLY_IPV6_ALLOWED";
        }
        if (o == 52) {
            return "ONLY_SINGLE_BEARER_ALLOWED";
        }
        if (o == 53) {
            return "ESM_INFO_NOT_RECEIVED";
        }
        if (o == 54) {
            return "PDN_CONN_DOES_NOT_EXIST";
        }
        if (o == 55) {
            return "MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED";
        }
        if (o == 65) {
            return "MAX_ACTIVE_PDP_CONTEXT_REACHED";
        }
        if (o == 66) {
            return "UNSUPPORTED_APN_IN_CURRENT_PLMN";
        }
        if (o == 81) {
            return "INVALID_TRANSACTION_ID";
        }
        if (o == 95) {
            return "MESSAGE_INCORRECT_SEMANTIC";
        }
        if (o == 96) {
            return "INVALID_MANDATORY_INFO";
        }
        if (o == 97) {
            return "MESSAGE_TYPE_UNSUPPORTED";
        }
        if (o == 98) {
            return "MSG_TYPE_NONCOMPATIBLE_STATE";
        }
        if (o == 99) {
            return "UNKNOWN_INFO_ELEMENT";
        }
        if (o == 100) {
            return "CONDITIONAL_IE_ERROR";
        }
        if (o == 101) {
            return "MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE";
        }
        if (o == 111) {
            return "PROTOCOL_ERRORS";
        }
        if (o == 112) {
            return "APN_TYPE_CONFLICT";
        }
        if (o == 113) {
            return "INVALID_PCSCF_ADDR";
        }
        if (o == 114) {
            return "INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN";
        }
        if (o == 115) {
            return "EMM_ACCESS_BARRED";
        }
        if (o == 116) {
            return "EMERGENCY_IFACE_ONLY";
        }
        if (o == 117) {
            return "IFACE_MISMATCH";
        }
        if (o == 118) {
            return "COMPANION_IFACE_IN_USE";
        }
        if (o == 119) {
            return "IP_ADDRESS_MISMATCH";
        }
        if (o == 120) {
            return "IFACE_AND_POL_FAMILY_MISMATCH";
        }
        if (o == 121) {
            return "EMM_ACCESS_BARRED_INFINITE_RETRY";
        }
        if (o == 122) {
            return "AUTH_FAILURE_ON_EMERGENCY_CALL";
        }
        if (o == 4097) {
            return "OEM_DCFAILCAUSE_1";
        }
        if (o == 4098) {
            return "OEM_DCFAILCAUSE_2";
        }
        if (o == 4099) {
            return "OEM_DCFAILCAUSE_3";
        }
        if (o == 4100) {
            return "OEM_DCFAILCAUSE_4";
        }
        if (o == 4101) {
            return "OEM_DCFAILCAUSE_5";
        }
        if (o == 4102) {
            return "OEM_DCFAILCAUSE_6";
        }
        if (o == 4103) {
            return "OEM_DCFAILCAUSE_7";
        }
        if (o == 4104) {
            return "OEM_DCFAILCAUSE_8";
        }
        if (o == 4105) {
            return "OEM_DCFAILCAUSE_9";
        }
        if (o == 4106) {
            return "OEM_DCFAILCAUSE_10";
        }
        if (o == 4107) {
            return "OEM_DCFAILCAUSE_11";
        }
        if (o == 4108) {
            return "OEM_DCFAILCAUSE_12";
        }
        if (o == 4109) {
            return "OEM_DCFAILCAUSE_13";
        }
        if (o == 4110) {
            return "OEM_DCFAILCAUSE_14";
        }
        if (o == 4111) {
            return "OEM_DCFAILCAUSE_15";
        }
        if (o == -1) {
            return "VOICE_REGISTRATION_FAIL";
        }
        if (o == -2) {
            return "DATA_REGISTRATION_FAIL";
        }
        if (o == -3) {
            return "SIGNAL_LOST";
        }
        if (o == -4) {
            return "PREF_RADIO_TECH_CHANGED";
        }
        if (o == -5) {
            return "RADIO_POWER_OFF";
        }
        if (o == -6) {
            return "TETHERED_CALL_ACTIVE";
        }
        if (o == 65535) {
            return "ERROR_UNSPECIFIED";
        }
        if (o == 25) {
            return "LLC_SNDCP";
        }
        if (o == 48) {
            return "ACTIVATION_REJECTED_BCM_VIOLATION";
        }
        if (o == 56) {
            return "COLLISION_WITH_NETWORK_INITIATED_REQUEST";
        }
        if (o == 57) {
            return "ONLY_IPV4V6_ALLOWED";
        }
        if (o == 58) {
            return "ONLY_NON_IP_ALLOWED";
        }
        if (o == 59) {
            return "UNSUPPORTED_QCI_VALUE";
        }
        if (o == 60) {
            return "BEARER_HANDLING_NOT_SUPPORTED";
        }
        if (o == 123) {
            return "INVALID_DNS_ADDR";
        }
        if (o == 124) {
            return "INVALID_PCSCF_OR_DNS_ADDRESS";
        }
        if (o == 127) {
            return "CALL_PREEMPT_BY_EMERGENCY_APN";
        }
        if (o == 128) {
            return "UE_INITIATED_DETACH_OR_DISCONNECT";
        }
        if (o == 2000) {
            return "MIP_FA_REASON_UNSPECIFIED";
        }
        if (o == 2001) {
            return "MIP_FA_ADMIN_PROHIBITED";
        }
        if (o == 2002) {
            return "MIP_FA_INSUFFICIENT_RESOURCES";
        }
        if (o == 2003) {
            return "MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE";
        }
        if (o == 2004) {
            return "MIP_FA_HOME_AGENT_AUTHENTICATION_FAILURE";
        }
        if (o == 2005) {
            return "MIP_FA_REQUESTED_LIFETIME_TOO_LONG";
        }
        if (o == 2006) {
            return "MIP_FA_MALFORMED_REQUEST";
        }
        if (o == 2007) {
            return "MIP_FA_MALFORMED_REPLY";
        }
        if (o == 2008) {
            return "MIP_FA_ENCAPSULATION_UNAVAILABLE";
        }
        if (o == 2009) {
            return "MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE";
        }
        if (o == 2010) {
            return "MIP_FA_REVERSE_TUNNEL_UNAVAILABLE";
        }
        if (o == 2011) {
            return "MIP_FA_REVERSE_TUNNEL_IS_MANDATORY";
        }
        if (o == 2012) {
            return "MIP_FA_DELIVERY_STYLE_NOT_SUPPORTED";
        }
        if (o == 2013) {
            return "MIP_FA_MISSING_NAI";
        }
        if (o == 2014) {
            return "MIP_FA_MISSING_HOME_AGENT";
        }
        if (o == 2015) {
            return "MIP_FA_MISSING_HOME_ADDRESS";
        }
        if (o == 2016) {
            return "MIP_FA_UNKNOWN_CHALLENGE";
        }
        if (o == 2017) {
            return "MIP_FA_MISSING_CHALLENGE";
        }
        if (o == 2018) {
            return "MIP_FA_STALE_CHALLENGE";
        }
        if (o == 2019) {
            return "MIP_HA_REASON_UNSPECIFIED";
        }
        if (o == 2020) {
            return "MIP_HA_ADMIN_PROHIBITED";
        }
        if (o == 2021) {
            return "MIP_HA_INSUFFICIENT_RESOURCES";
        }
        if (o == 2022) {
            return "MIP_HA_MOBILE_NODE_AUTHENTICATION_FAILURE";
        }
        if (o == 2023) {
            return "MIP_HA_FOREIGN_AGENT_AUTHENTICATION_FAILURE";
        }
        if (o == 2024) {
            return "MIP_HA_REGISTRATION_ID_MISMATCH";
        }
        if (o == 2025) {
            return "MIP_HA_MALFORMED_REQUEST";
        }
        if (o == 2026) {
            return "MIP_HA_UNKNOWN_HOME_AGENT_ADDRESS";
        }
        if (o == 2027) {
            return "MIP_HA_REVERSE_TUNNEL_UNAVAILABLE";
        }
        if (o == 2028) {
            return "MIP_HA_REVERSE_TUNNEL_IS_MANDATORY";
        }
        if (o == 2029) {
            return "MIP_HA_ENCAPSULATION_UNAVAILABLE";
        }
        if (o == 2030) {
            return "CLOSE_IN_PROGRESS";
        }
        if (o == 2031) {
            return "NETWORK_INITIATED_TERMINATION";
        }
        if (o == 2032) {
            return "MODEM_APP_PREEMPTED";
        }
        if (o == 2033) {
            return "PDN_IPV4_CALL_DISALLOWED";
        }
        if (o == 2034) {
            return "PDN_IPV4_CALL_THROTTLED";
        }
        if (o == 2035) {
            return "PDN_IPV6_CALL_DISALLOWED";
        }
        if (o == 2036) {
            return "PDN_IPV6_CALL_THROTTLED";
        }
        if (o == 2037) {
            return "MODEM_RESTART";
        }
        if (o == 2038) {
            return "PDP_PPP_NOT_SUPPORTED";
        }
        if (o == 2039) {
            return "UNPREFERRED_RAT";
        }
        if (o == 2040) {
            return "PHYSICAL_LINK_CLOSE_IN_PROGRESS";
        }
        if (o == 2041) {
            return "APN_PENDING_HANDOVER";
        }
        if (o == 2042) {
            return "PROFILE_BEARER_INCOMPATIBLE";
        }
        if (o == 2043) {
            return "SIM_CARD_CHANGED";
        }
        if (o == 2044) {
            return "LOW_POWER_MODE_OR_POWERING_DOWN";
        }
        if (o == 2045) {
            return "APN_DISABLED";
        }
        if (o == 2046) {
            return "MAX_PPP_INACTIVITY_TIMER_EXPIRED";
        }
        if (o == 2047) {
            return "IPV6_ADDRESS_TRANSFER_FAILED";
        }
        if (o == 2048) {
            return "TRAT_SWAP_FAILED";
        }
        if (o == 2049) {
            return "EHRPD_TO_HRPD_FALLBACK";
        }
        if (o == 2050) {
            return "MIP_CONFIG_FAILURE";
        }
        if (o == 2051) {
            return "PDN_INACTIVITY_TIMER_EXPIRED";
        }
        if (o == 2052) {
            return "MAX_IPV4_CONNECTIONS";
        }
        if (o == 2053) {
            return "MAX_IPV6_CONNECTIONS";
        }
        if (o == 2054) {
            return "APN_MISMATCH";
        }
        if (o == 2055) {
            return "IP_VERSION_MISMATCH";
        }
        if (o == 2056) {
            return "DUN_CALL_DISALLOWED";
        }
        if (o == 2057) {
            return "INTERNAL_EPC_NONEPC_TRANSITION";
        }
        if (o == 2058) {
            return "INTERFACE_IN_USE";
        }
        if (o == 2059) {
            return "APN_DISALLOWED_ON_ROAMING";
        }
        if (o == 2060) {
            return "APN_PARAMETERS_CHANGED";
        }
        if (o == 2061) {
            return "NULL_APN_DISALLOWED";
        }
        if (o == 2062) {
            return "THERMAL_MITIGATION";
        }
        if (o == 2063) {
            return "DATA_SETTINGS_DISABLED";
        }
        if (o == 2064) {
            return "DATA_ROAMING_SETTINGS_DISABLED";
        }
        if (o == 2065) {
            return "DDS_SWITCHED";
        }
        if (o == 2066) {
            return "FORBIDDEN_APN_NAME";
        }
        if (o == 2067) {
            return "DDS_SWITCH_IN_PROGRESS";
        }
        if (o == 2068) {
            return "CALL_DISALLOWED_IN_ROAMING";
        }
        if (o == 2069) {
            return "NON_IP_NOT_SUPPORTED";
        }
        if (o == 2070) {
            return "PDN_NON_IP_CALL_THROTTLED";
        }
        if (o == 2071) {
            return "PDN_NON_IP_CALL_DISALLOWED";
        }
        if (o == 2072) {
            return "CDMA_LOCK";
        }
        if (o == 2073) {
            return "CDMA_INTERCEPT";
        }
        if (o == 2074) {
            return "CDMA_REORDER";
        }
        if (o == 2075) {
            return "CDMA_RELEASE_DUE_TO_SO_REJECTION";
        }
        if (o == 2076) {
            return "CDMA_INCOMING_CALL";
        }
        if (o == 2077) {
            return "CDMA_ALERT_STOP";
        }
        if (o == 2078) {
            return "CHANNEL_ACQUISITION_FAILURE";
        }
        if (o == 2079) {
            return "MAX_ACCESS_PROBE";
        }
        if (o == 2080) {
            return "CONCURRENT_SERVICE_NOT_SUPPORTED_BY_BASE_STATION";
        }
        if (o == 2081) {
            return "NO_RESPONSE_FROM_BASE_STATION";
        }
        if (o == 2082) {
            return "REJECTED_BY_BASE_STATION";
        }
        if (o == 2083) {
            return "CONCURRENT_SERVICES_INCOMPATIBLE";
        }
        if (o == 2084) {
            return "NO_CDMA_SERVICE";
        }
        if (o == 2085) {
            return "RUIM_NOT_PRESENT";
        }
        if (o == 2086) {
            return "CDMA_RETRY_ORDER";
        }
        if (o == 2087) {
            return "ACCESS_BLOCK";
        }
        if (o == 2088) {
            return "ACCESS_BLOCK_ALL";
        }
        if (o == 2089) {
            return "IS707B_MAX_ACCESS_PROBES";
        }
        if (o == 2090) {
            return "THERMAL_EMERGENCY";
        }
        if (o == 2091) {
            return "CONCURRENT_SERVICES_NOT_ALLOWED";
        }
        if (o == 2092) {
            return "INCOMING_CALL_REJECTED";
        }
        if (o == 2093) {
            return "NO_SERVICE_ON_GATEWAY";
        }
        if (o == 2094) {
            return "NO_GPRS_CONTEXT";
        }
        if (o == 2095) {
            return "ILLEGAL_MS";
        }
        if (o == 2096) {
            return "ILLEGAL_ME";
        }
        if (o == 2097) {
            return "GPRS_SERVICES_AND_NON_GPRS_SERVICES_NOT_ALLOWED";
        }
        if (o == 2098) {
            return "GPRS_SERVICES_NOT_ALLOWED";
        }
        if (o == 2099) {
            return "MS_IDENTITY_CANNOT_BE_DERIVED_BY_THE_NETWORK";
        }
        if (o == 2100) {
            return "IMPLICITLY_DETACHED";
        }
        if (o == 2101) {
            return "PLMN_NOT_ALLOWED";
        }
        if (o == 2102) {
            return "LOCATION_AREA_NOT_ALLOWED";
        }
        if (o == 2103) {
            return "GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN";
        }
        if (o == 2104) {
            return "PDP_DUPLICATE";
        }
        if (o == 2105) {
            return "UE_RAT_CHANGE";
        }
        if (o == 2106) {
            return "CONGESTION";
        }
        if (o == 2107) {
            return "NO_PDP_CONTEXT_ACTIVATED";
        }
        if (o == 2108) {
            return "ACCESS_CLASS_DSAC_REJECTION";
        }
        if (o == 2109) {
            return "PDP_ACTIVATE_MAX_RETRY_FAILED";
        }
        if (o == 2110) {
            return "RADIO_ACCESS_BEARER_FAILURE";
        }
        if (o == 2111) {
            return "ESM_UNKNOWN_EPS_BEARER_CONTEXT";
        }
        if (o == 2112) {
            return "DRB_RELEASED_BY_RRC";
        }
        if (o == 2113) {
            return "CONNECTION_RELEASED";
        }
        if (o == 2114) {
            return "EMM_DETACHED";
        }
        if (o == 2115) {
            return "EMM_ATTACH_FAILED";
        }
        if (o == 2116) {
            return "EMM_ATTACH_STARTED";
        }
        if (o == 2117) {
            return "LTE_NAS_SERVICE_REQUEST_FAILED";
        }
        if (o == 2118) {
            return "DUPLICATE_BEARER_ID";
        }
        if (o == 2119) {
            return "ESM_COLLISION_SCENARIOS";
        }
        if (o == 2120) {
            return "ESM_BEARER_DEACTIVATED_TO_SYNC_WITH_NETWORK";
        }
        if (o == 2121) {
            return "ESM_NW_ACTIVATED_DED_BEARER_WITH_ID_OF_DEF_BEARER";
        }
        if (o == 2122) {
            return "ESM_BAD_OTA_MESSAGE";
        }
        if (o == 2123) {
            return "ESM_DOWNLOAD_SERVER_REJECTED_THE_CALL";
        }
        if (o == 2124) {
            return "ESM_CONTEXT_TRANSFERRED_DUE_TO_IRAT";
        }
        if (o == 2125) {
            return "DS_EXPLICIT_DEACTIVATION";
        }
        if (o == 2126) {
            return "ESM_LOCAL_CAUSE_NONE";
        }
        if (o == 2127) {
            return "LTE_THROTTLING_NOT_REQUIRED";
        }
        if (o == 2128) {
            return "ACCESS_CONTROL_LIST_CHECK_FAILURE";
        }
        if (o == 2129) {
            return "SERVICE_NOT_ALLOWED_ON_PLMN";
        }
        if (o == 2130) {
            return "EMM_T3417_EXPIRED";
        }
        if (o == 2131) {
            return "EMM_T3417_EXT_EXPIRED";
        }
        if (o == 2132) {
            return "RRC_UPLINK_DATA_TRANSMISSION_FAILURE";
        }
        if (o == 2133) {
            return "RRC_UPLINK_DELIVERY_FAILED_DUE_TO_HANDOVER";
        }
        if (o == 2134) {
            return "RRC_UPLINK_CONNECTION_RELEASE";
        }
        if (o == 2135) {
            return "RRC_UPLINK_RADIO_LINK_FAILURE";
        }
        if (o == 2136) {
            return "RRC_UPLINK_ERROR_REQUEST_FROM_NAS";
        }
        if (o == 2137) {
            return "RRC_CONNECTION_ACCESS_STRATUM_FAILURE";
        }
        if (o == 2138) {
            return "RRC_CONNECTION_ANOTHER_PROCEDURE_IN_PROGRESS";
        }
        if (o == 2139) {
            return "RRC_CONNECTION_ACCESS_BARRED";
        }
        if (o == 2140) {
            return "RRC_CONNECTION_CELL_RESELECTION";
        }
        if (o == 2141) {
            return "RRC_CONNECTION_CONFIG_FAILURE";
        }
        if (o == 2142) {
            return "RRC_CONNECTION_TIMER_EXPIRED";
        }
        if (o == 2143) {
            return "RRC_CONNECTION_LINK_FAILURE";
        }
        if (o == 2144) {
            return "RRC_CONNECTION_CELL_NOT_CAMPED";
        }
        if (o == 2145) {
            return "RRC_CONNECTION_SYSTEM_INTERVAL_FAILURE";
        }
        if (o == 2146) {
            return "RRC_CONNECTION_REJECT_BY_NETWORK";
        }
        if (o == 2147) {
            return "RRC_CONNECTION_NORMAL_RELEASE";
        }
        if (o == 2148) {
            return "RRC_CONNECTION_RADIO_LINK_FAILURE";
        }
        if (o == 2149) {
            return "RRC_CONNECTION_REESTABLISHMENT_FAILURE";
        }
        if (o == 2150) {
            return "RRC_CONNECTION_OUT_OF_SERVICE_DURING_CELL_REGISTER";
        }
        if (o == 2151) {
            return "RRC_CONNECTION_ABORT_REQUEST";
        }
        if (o == 2152) {
            return "RRC_CONNECTION_SYSTEM_INFORMATION_BLOCK_READ_ERROR";
        }
        if (o == 2153) {
            return "NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH";
        }
        if (o == 2154) {
            return "NETWORK_INITIATED_DETACH_NO_AUTO_REATTACH";
        }
        if (o == 2155) {
            return "ESM_PROCEDURE_TIME_OUT";
        }
        if (o == 2156) {
            return "INVALID_CONNECTION_ID";
        }
        if (o == 2157) {
            return "MAXIMIUM_NSAPIS_EXCEEDED";
        }
        if (o == 2158) {
            return "INVALID_PRIMARY_NSAPI";
        }
        if (o == 2159) {
            return "CANNOT_ENCODE_OTA_MESSAGE";
        }
        if (o == 2160) {
            return "RADIO_ACCESS_BEARER_SETUP_FAILURE";
        }
        if (o == 2161) {
            return "PDP_ESTABLISH_TIMEOUT_EXPIRED";
        }
        if (o == 2162) {
            return "PDP_MODIFY_TIMEOUT_EXPIRED";
        }
        if (o == 2163) {
            return "PDP_INACTIVE_TIMEOUT_EXPIRED";
        }
        if (o == 2164) {
            return "PDP_LOWERLAYER_ERROR";
        }
        if (o == 2165) {
            return "PDP_MODIFY_COLLISION";
        }
        if (o == 2166) {
            return "MAXINUM_SIZE_OF_L2_MESSAGE_EXCEEDED";
        }
        if (o == 2167) {
            return "NAS_REQUEST_REJECTED_BY_NETWORK";
        }
        if (o == 2168) {
            return "RRC_CONNECTION_INVALID_REQUEST";
        }
        if (o == 2169) {
            return "RRC_CONNECTION_TRACKING_AREA_ID_CHANGED";
        }
        if (o == 2170) {
            return "RRC_CONNECTION_RF_UNAVAILABLE";
        }
        if (o == 2171) {
            return "RRC_CONNECTION_ABORTED_DUE_TO_IRAT_CHANGE";
        }
        if (o == 2172) {
            return "RRC_CONNECTION_RELEASED_SECURITY_NOT_ACTIVE";
        }
        if (o == 2173) {
            return "RRC_CONNECTION_ABORTED_AFTER_HANDOVER";
        }
        if (o == 2174) {
            return "RRC_CONNECTION_ABORTED_AFTER_IRAT_CELL_CHANGE";
        }
        if (o == 2175) {
            return "RRC_CONNECTION_ABORTED_DURING_IRAT_CELL_CHANGE";
        }
        if (o == 2176) {
            return "IMSI_UNKNOWN_IN_HOME_SUBSCRIBER_SERVER";
        }
        if (o == 2177) {
            return "IMEI_NOT_ACCEPTED";
        }
        if (o == 2178) {
            return "EPS_SERVICES_AND_NON_EPS_SERVICES_NOT_ALLOWED";
        }
        if (o == 2179) {
            return "EPS_SERVICES_NOT_ALLOWED_IN_PLMN";
        }
        if (o == 2180) {
            return "MSC_TEMPORARILY_NOT_REACHABLE";
        }
        if (o == 2181) {
            return "CS_DOMAIN_NOT_AVAILABLE";
        }
        if (o == 2182) {
            return "ESM_FAILURE";
        }
        if (o == 2183) {
            return "MAC_FAILURE";
        }
        if (o == 2184) {
            return "SYNCHRONIZATION_FAILURE";
        }
        if (o == 2185) {
            return "UE_SECURITY_CAPABILITIES_MISMATCH";
        }
        if (o == 2186) {
            return "SECURITY_MODE_REJECTED";
        }
        if (o == 2187) {
            return "UNACCEPTABLE_NON_EPS_AUTHENTICATION";
        }
        if (o == 2188) {
            return "CS_FALLBACK_CALL_ESTABLISHMENT_NOT_ALLOWED";
        }
        if (o == 2189) {
            return "NO_EPS_BEARER_CONTEXT_ACTIVATED";
        }
        if (o == 2190) {
            return "INVALID_EMM_STATE";
        }
        if (o == 2191) {
            return "NAS_LAYER_FAILURE";
        }
        if (o == 2192) {
            return "MULTIPLE_PDP_CALL_NOT_ALLOWED";
        }
        if (o == 2193) {
            return "EMBMS_NOT_ENABLED";
        }
        if (o == 2194) {
            return "IRAT_HANDOVER_FAILED";
        }
        if (o == 2195) {
            return "EMBMS_REGULAR_DEACTIVATION";
        }
        if (o == 2196) {
            return "TEST_LOOPBACK_REGULAR_DEACTIVATION";
        }
        if (o == 2197) {
            return "LOWER_LAYER_REGISTRATION_FAILURE";
        }
        if (o == 2198) {
            return "DATA_PLAN_EXPIRED";
        }
        if (o == 2199) {
            return "UMTS_HANDOVER_TO_IWLAN";
        }
        if (o == 2200) {
            return "EVDO_CONNECTION_DENY_BY_GENERAL_OR_NETWORK_BUSY";
        }
        if (o == 2201) {
            return "EVDO_CONNECTION_DENY_BY_BILLING_OR_AUTHENTICATION_FAILURE";
        }
        if (o == 2202) {
            return "EVDO_HDR_CHANGED";
        }
        if (o == 2203) {
            return "EVDO_HDR_EXITED";
        }
        if (o == 2204) {
            return "EVDO_HDR_NO_SESSION";
        }
        if (o == 2205) {
            return "EVDO_USING_GPS_FIX_INSTEAD_OF_HDR_CALL";
        }
        if (o == 2206) {
            return "EVDO_HDR_CONNECTION_SETUP_TIMEOUT";
        }
        if (o == 2207) {
            return "FAILED_TO_ACQUIRE_COLOCATED_HDR";
        }
        if (o == 2208) {
            return "OTASP_COMMIT_IN_PROGRESS";
        }
        if (o == 2209) {
            return "NO_HYBRID_HDR_SERVICE";
        }
        if (o == 2210) {
            return "HDR_NO_LOCK_GRANTED";
        }
        if (o == 2211) {
            return "DBM_OR_SMS_IN_PROGRESS";
        }
        if (o == 2212) {
            return "HDR_FADE";
        }
        if (o == 2213) {
            return "HDR_ACCESS_FAILURE";
        }
        if (o == 2214) {
            return "UNSUPPORTED_1X_PREV";
        }
        if (o == 2215) {
            return "LOCAL_END";
        }
        if (o == 2216) {
            return "NO_SERVICE";
        }
        if (o == 2217) {
            return "FADE";
        }
        if (o == 2218) {
            return "NORMAL_RELEASE";
        }
        if (o == 2219) {
            return "ACCESS_ATTEMPT_ALREADY_IN_PROGRESS";
        }
        if (o == 2220) {
            return "REDIRECTION_OR_HANDOFF_IN_PROGRESS";
        }
        if (o == 2221) {
            return "EMERGENCY_MODE";
        }
        if (o == 2222) {
            return "PHONE_IN_USE";
        }
        if (o == 2223) {
            return "INVALID_MODE";
        }
        if (o == 2224) {
            return "INVALID_SIM_STATE";
        }
        if (o == 2225) {
            return "NO_COLLOCATED_HDR";
        }
        if (o == 2226) {
            return "UE_IS_ENTERING_POWERSAVE_MODE";
        }
        if (o == 2227) {
            return "DUAL_SWITCH";
        }
        if (o == 2228) {
            return "PPP_TIMEOUT";
        }
        if (o == 2229) {
            return "PPP_AUTH_FAILURE";
        }
        if (o == 2230) {
            return "PPP_OPTION_MISMATCH";
        }
        if (o == 2231) {
            return "PPP_PAP_FAILURE";
        }
        if (o == 2232) {
            return "PPP_CHAP_FAILURE";
        }
        if (o == 2233) {
            return "PPP_CLOSE_IN_PROGRESS";
        }
        if (o == 2234) {
            return "LIMITED_TO_IPV4";
        }
        if (o == 2235) {
            return "LIMITED_TO_IPV6";
        }
        if (o == 2236) {
            return "VSNCP_TIMEOUT";
        }
        if (o == 2237) {
            return "VSNCP_GEN_ERROR";
        }
        if (o == 2238) {
            return "VSNCP_APN_UNATHORIZED";
        }
        if (o == 2239) {
            return "VSNCP_PDN_LIMIT_EXCEEDED";
        }
        if (o == 2240) {
            return "VSNCP_NO_PDN_GATEWAY_ADDRESS";
        }
        if (o == 2241) {
            return "VSNCP_PDN_GATEWAY_UNREACHABLE";
        }
        if (o == 2242) {
            return "VSNCP_PDN_GATEWAY_REJECT";
        }
        if (o == 2243) {
            return "VSNCP_INSUFFICIENT_PARAMETERS";
        }
        if (o == 2244) {
            return "VSNCP_RESOURCE_UNAVAILABLE";
        }
        if (o == 2245) {
            return "VSNCP_ADMINISTRATIVELY_PROHIBITED";
        }
        if (o == 2246) {
            return "VSNCP_PDN_ID_IN_USE";
        }
        if (o == 2247) {
            return "VSNCP_SUBSCRIBER_LIMITATION";
        }
        if (o == 2248) {
            return "VSNCP_PDN_EXISTS_FOR_THIS_APN";
        }
        if (o == 2249) {
            return "VSNCP_RECONNECT_NOT_ALLOWED";
        }
        if (o == 2250) {
            return "IPV6_PREFIX_UNAVAILABLE";
        }
        if (o == 2251) {
            return "HANDOFF_PREFERENCE_CHANGED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add(KeyProperties.DIGEST_NONE);
        if ((o & 8) == 8) {
            list.add("OPERATOR_BARRED");
            flipped = 0 | 8;
        }
        if ((o & 14) == 14) {
            list.add("NAS_SIGNALLING");
            flipped |= 14;
        }
        if ((o & 26) == 26) {
            list.add("INSUFFICIENT_RESOURCES");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("MISSING_UKNOWN_APN");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("UNKNOWN_PDP_ADDRESS_TYPE");
            flipped |= 28;
        }
        if ((o & 29) == 29) {
            list.add("USER_AUTHENTICATION");
            flipped |= 29;
        }
        if ((o & 30) == 30) {
            list.add("ACTIVATION_REJECT_GGSN");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("ACTIVATION_REJECT_UNSPECIFIED");
            flipped |= 31;
        }
        if ((o & 32) == 32) {
            list.add("SERVICE_OPTION_NOT_SUPPORTED");
            flipped |= 32;
        }
        if ((o & 33) == 33) {
            list.add("SERVICE_OPTION_NOT_SUBSCRIBED");
            flipped |= 33;
        }
        if ((o & 34) == 34) {
            list.add("SERVICE_OPTION_OUT_OF_ORDER");
            flipped |= 34;
        }
        if ((o & 35) == 35) {
            list.add("NSAPI_IN_USE");
            flipped |= 35;
        }
        if ((o & 36) == 36) {
            list.add("REGULAR_DEACTIVATION");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("QOS_NOT_ACCEPTED");
            flipped |= 37;
        }
        if ((o & 38) == 38) {
            list.add("NETWORK_FAILURE");
            flipped |= 38;
        }
        if ((o & 39) == 39) {
            list.add("UMTS_REACTIVATION_REQ");
            flipped |= 39;
        }
        if ((o & 40) == 40) {
            list.add("FEATURE_NOT_SUPP");
            flipped |= 40;
        }
        if ((o & 41) == 41) {
            list.add("TFT_SEMANTIC_ERROR");
            flipped |= 41;
        }
        if ((o & 42) == 42) {
            list.add("TFT_SYTAX_ERROR");
            flipped |= 42;
        }
        if ((o & 43) == 43) {
            list.add("UNKNOWN_PDP_CONTEXT");
            flipped |= 43;
        }
        if ((o & 44) == 44) {
            list.add("FILTER_SEMANTIC_ERROR");
            flipped |= 44;
        }
        if ((o & 45) == 45) {
            list.add("FILTER_SYTAX_ERROR");
            flipped |= 45;
        }
        if ((o & 46) == 46) {
            list.add("PDP_WITHOUT_ACTIVE_TFT");
            flipped |= 46;
        }
        if ((o & 50) == 50) {
            list.add("ONLY_IPV4_ALLOWED");
            flipped |= 50;
        }
        if ((o & 51) == 51) {
            list.add("ONLY_IPV6_ALLOWED");
            flipped |= 51;
        }
        if ((o & 52) == 52) {
            list.add("ONLY_SINGLE_BEARER_ALLOWED");
            flipped |= 52;
        }
        if ((o & 53) == 53) {
            list.add("ESM_INFO_NOT_RECEIVED");
            flipped |= 53;
        }
        if ((o & 54) == 54) {
            list.add("PDN_CONN_DOES_NOT_EXIST");
            flipped |= 54;
        }
        if ((o & 55) == 55) {
            list.add("MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED");
            flipped |= 55;
        }
        if ((o & 65) == 65) {
            list.add("MAX_ACTIVE_PDP_CONTEXT_REACHED");
            flipped |= 65;
        }
        if ((o & 66) == 66) {
            list.add("UNSUPPORTED_APN_IN_CURRENT_PLMN");
            flipped |= 66;
        }
        if ((o & 81) == 81) {
            list.add("INVALID_TRANSACTION_ID");
            flipped |= 81;
        }
        if ((o & 95) == 95) {
            list.add("MESSAGE_INCORRECT_SEMANTIC");
            flipped |= 95;
        }
        if ((o & 96) == 96) {
            list.add("INVALID_MANDATORY_INFO");
            flipped |= 96;
        }
        if ((o & 97) == 97) {
            list.add("MESSAGE_TYPE_UNSUPPORTED");
            flipped |= 97;
        }
        if ((o & 98) == 98) {
            list.add("MSG_TYPE_NONCOMPATIBLE_STATE");
            flipped |= 98;
        }
        if ((o & 99) == 99) {
            list.add("UNKNOWN_INFO_ELEMENT");
            flipped |= 99;
        }
        if ((o & 100) == 100) {
            list.add("CONDITIONAL_IE_ERROR");
            flipped |= 100;
        }
        if ((o & 101) == 101) {
            list.add("MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE");
            flipped |= 101;
        }
        if ((o & 111) == 111) {
            list.add("PROTOCOL_ERRORS");
            flipped |= 111;
        }
        if ((o & 112) == 112) {
            list.add("APN_TYPE_CONFLICT");
            flipped |= 112;
        }
        if ((o & 113) == 113) {
            list.add("INVALID_PCSCF_ADDR");
            flipped |= 113;
        }
        if ((o & 114) == 114) {
            list.add("INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN");
            flipped |= 114;
        }
        if ((o & 115) == 115) {
            list.add("EMM_ACCESS_BARRED");
            flipped |= 115;
        }
        if ((o & 116) == 116) {
            list.add("EMERGENCY_IFACE_ONLY");
            flipped |= 116;
        }
        if ((o & 117) == 117) {
            list.add("IFACE_MISMATCH");
            flipped |= 117;
        }
        if ((o & 118) == 118) {
            list.add("COMPANION_IFACE_IN_USE");
            flipped |= 118;
        }
        if ((o & 119) == 119) {
            list.add("IP_ADDRESS_MISMATCH");
            flipped |= 119;
        }
        if ((o & 120) == 120) {
            list.add("IFACE_AND_POL_FAMILY_MISMATCH");
            flipped |= 120;
        }
        if ((o & 121) == 121) {
            list.add("EMM_ACCESS_BARRED_INFINITE_RETRY");
            flipped |= 121;
        }
        if ((o & 122) == 122) {
            list.add("AUTH_FAILURE_ON_EMERGENCY_CALL");
            flipped |= 122;
        }
        if ((o & 4097) == 4097) {
            list.add("OEM_DCFAILCAUSE_1");
            flipped |= 4097;
        }
        if ((o & 4098) == 4098) {
            list.add("OEM_DCFAILCAUSE_2");
            flipped |= 4098;
        }
        if ((o & 4099) == 4099) {
            list.add("OEM_DCFAILCAUSE_3");
            flipped |= 4099;
        }
        if ((o & 4100) == 4100) {
            list.add("OEM_DCFAILCAUSE_4");
            flipped |= 4100;
        }
        if ((o & 4101) == 4101) {
            list.add("OEM_DCFAILCAUSE_5");
            flipped |= 4101;
        }
        if ((o & 4102) == 4102) {
            list.add("OEM_DCFAILCAUSE_6");
            flipped |= 4102;
        }
        if ((o & 4103) == 4103) {
            list.add("OEM_DCFAILCAUSE_7");
            flipped |= 4103;
        }
        if ((o & 4104) == 4104) {
            list.add("OEM_DCFAILCAUSE_8");
            flipped |= 4104;
        }
        if ((o & 4105) == 4105) {
            list.add("OEM_DCFAILCAUSE_9");
            flipped |= 4105;
        }
        if ((o & 4106) == 4106) {
            list.add("OEM_DCFAILCAUSE_10");
            flipped |= 4106;
        }
        if ((o & 4107) == 4107) {
            list.add("OEM_DCFAILCAUSE_11");
            flipped |= 4107;
        }
        if ((o & 4108) == 4108) {
            list.add("OEM_DCFAILCAUSE_12");
            flipped |= 4108;
        }
        if ((o & 4109) == 4109) {
            list.add("OEM_DCFAILCAUSE_13");
            flipped |= 4109;
        }
        if ((o & 4110) == 4110) {
            list.add("OEM_DCFAILCAUSE_14");
            flipped |= 4110;
        }
        if ((o & 4111) == 4111) {
            list.add("OEM_DCFAILCAUSE_15");
            flipped |= 4111;
        }
        if ((o & -1) == -1) {
            list.add("VOICE_REGISTRATION_FAIL");
            flipped |= -1;
        }
        if ((o & -2) == -2) {
            list.add("DATA_REGISTRATION_FAIL");
            flipped |= -2;
        }
        if ((o & -3) == -3) {
            list.add("SIGNAL_LOST");
            flipped |= -3;
        }
        if ((o & -4) == -4) {
            list.add("PREF_RADIO_TECH_CHANGED");
            flipped |= -4;
        }
        if ((o & -5) == -5) {
            list.add("RADIO_POWER_OFF");
            flipped |= -5;
        }
        if ((o & -6) == -6) {
            list.add("TETHERED_CALL_ACTIVE");
            flipped |= -6;
        }
        if ((65535 & o) == 65535) {
            list.add("ERROR_UNSPECIFIED");
            flipped |= 65535;
        }
        if ((o & 25) == 25) {
            list.add("LLC_SNDCP");
            flipped |= 25;
        }
        if ((o & 48) == 48) {
            list.add("ACTIVATION_REJECTED_BCM_VIOLATION");
            flipped |= 48;
        }
        if ((o & 56) == 56) {
            list.add("COLLISION_WITH_NETWORK_INITIATED_REQUEST");
            flipped |= 56;
        }
        if ((o & 57) == 57) {
            list.add("ONLY_IPV4V6_ALLOWED");
            flipped |= 57;
        }
        if ((o & 58) == 58) {
            list.add("ONLY_NON_IP_ALLOWED");
            flipped |= 58;
        }
        if ((o & 59) == 59) {
            list.add("UNSUPPORTED_QCI_VALUE");
            flipped |= 59;
        }
        if ((o & 60) == 60) {
            list.add("BEARER_HANDLING_NOT_SUPPORTED");
            flipped |= 60;
        }
        if ((o & 123) == 123) {
            list.add("INVALID_DNS_ADDR");
            flipped |= 123;
        }
        if ((o & 124) == 124) {
            list.add("INVALID_PCSCF_OR_DNS_ADDRESS");
            flipped |= 124;
        }
        if ((o & 127) == 127) {
            list.add("CALL_PREEMPT_BY_EMERGENCY_APN");
            flipped |= 127;
        }
        if ((o & 128) == 128) {
            list.add("UE_INITIATED_DETACH_OR_DISCONNECT");
            flipped |= 128;
        }
        if ((o & 2000) == 2000) {
            list.add("MIP_FA_REASON_UNSPECIFIED");
            flipped |= 2000;
        }
        if ((o & 2001) == 2001) {
            list.add("MIP_FA_ADMIN_PROHIBITED");
            flipped |= 2001;
        }
        if ((o & 2002) == 2002) {
            list.add("MIP_FA_INSUFFICIENT_RESOURCES");
            flipped |= 2002;
        }
        if ((o & 2003) == 2003) {
            list.add("MIP_FA_MOBILE_NODE_AUTHENTICATION_FAILURE");
            flipped |= 2003;
        }
        if ((o & 2004) == 2004) {
            list.add("MIP_FA_HOME_AGENT_AUTHENTICATION_FAILURE");
            flipped |= 2004;
        }
        if ((o & 2005) == 2005) {
            list.add("MIP_FA_REQUESTED_LIFETIME_TOO_LONG");
            flipped |= 2005;
        }
        if ((o & 2006) == 2006) {
            list.add("MIP_FA_MALFORMED_REQUEST");
            flipped |= 2006;
        }
        if ((o & 2007) == 2007) {
            list.add("MIP_FA_MALFORMED_REPLY");
            flipped |= 2007;
        }
        if ((o & 2008) == 2008) {
            list.add("MIP_FA_ENCAPSULATION_UNAVAILABLE");
            flipped |= 2008;
        }
        if ((o & 2009) == 2009) {
            list.add("MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE");
            flipped |= 2009;
        }
        if ((o & 2010) == 2010) {
            list.add("MIP_FA_REVERSE_TUNNEL_UNAVAILABLE");
            flipped |= 2010;
        }
        if ((o & 2011) == 2011) {
            list.add("MIP_FA_REVERSE_TUNNEL_IS_MANDATORY");
            flipped |= 2011;
        }
        if ((o & 2012) == 2012) {
            list.add("MIP_FA_DELIVERY_STYLE_NOT_SUPPORTED");
            flipped |= 2012;
        }
        if ((o & 2013) == 2013) {
            list.add("MIP_FA_MISSING_NAI");
            flipped |= 2013;
        }
        if ((o & 2014) == 2014) {
            list.add("MIP_FA_MISSING_HOME_AGENT");
            flipped |= 2014;
        }
        if ((o & 2015) == 2015) {
            list.add("MIP_FA_MISSING_HOME_ADDRESS");
            flipped |= 2015;
        }
        if ((o & 2016) == 2016) {
            list.add("MIP_FA_UNKNOWN_CHALLENGE");
            flipped |= 2016;
        }
        if ((o & 2017) == 2017) {
            list.add("MIP_FA_MISSING_CHALLENGE");
            flipped |= 2017;
        }
        if ((o & 2018) == 2018) {
            list.add("MIP_FA_STALE_CHALLENGE");
            flipped |= 2018;
        }
        if ((o & 2019) == 2019) {
            list.add("MIP_HA_REASON_UNSPECIFIED");
            flipped |= 2019;
        }
        if ((o & 2020) == 2020) {
            list.add("MIP_HA_ADMIN_PROHIBITED");
            flipped |= 2020;
        }
        if ((o & 2021) == 2021) {
            list.add("MIP_HA_INSUFFICIENT_RESOURCES");
            flipped |= 2021;
        }
        if ((o & 2022) == 2022) {
            list.add("MIP_HA_MOBILE_NODE_AUTHENTICATION_FAILURE");
            flipped |= 2022;
        }
        if ((o & 2023) == 2023) {
            list.add("MIP_HA_FOREIGN_AGENT_AUTHENTICATION_FAILURE");
            flipped |= 2023;
        }
        if ((o & 2024) == 2024) {
            list.add("MIP_HA_REGISTRATION_ID_MISMATCH");
            flipped |= 2024;
        }
        if ((o & 2025) == 2025) {
            list.add("MIP_HA_MALFORMED_REQUEST");
            flipped |= 2025;
        }
        if ((o & 2026) == 2026) {
            list.add("MIP_HA_UNKNOWN_HOME_AGENT_ADDRESS");
            flipped |= 2026;
        }
        if ((o & 2027) == 2027) {
            list.add("MIP_HA_REVERSE_TUNNEL_UNAVAILABLE");
            flipped |= 2027;
        }
        if ((o & 2028) == 2028) {
            list.add("MIP_HA_REVERSE_TUNNEL_IS_MANDATORY");
            flipped |= 2028;
        }
        if ((o & 2029) == 2029) {
            list.add("MIP_HA_ENCAPSULATION_UNAVAILABLE");
            flipped |= 2029;
        }
        if ((o & 2030) == 2030) {
            list.add("CLOSE_IN_PROGRESS");
            flipped |= 2030;
        }
        if ((o & 2031) == 2031) {
            list.add("NETWORK_INITIATED_TERMINATION");
            flipped |= 2031;
        }
        if ((o & 2032) == 2032) {
            list.add("MODEM_APP_PREEMPTED");
            flipped |= 2032;
        }
        if ((o & 2033) == 2033) {
            list.add("PDN_IPV4_CALL_DISALLOWED");
            flipped |= 2033;
        }
        if ((o & 2034) == 2034) {
            list.add("PDN_IPV4_CALL_THROTTLED");
            flipped |= 2034;
        }
        if ((o & 2035) == 2035) {
            list.add("PDN_IPV6_CALL_DISALLOWED");
            flipped |= 2035;
        }
        if ((o & 2036) == 2036) {
            list.add("PDN_IPV6_CALL_THROTTLED");
            flipped |= 2036;
        }
        if ((o & 2037) == 2037) {
            list.add("MODEM_RESTART");
            flipped |= 2037;
        }
        if ((o & 2038) == 2038) {
            list.add("PDP_PPP_NOT_SUPPORTED");
            flipped |= 2038;
        }
        if ((o & 2039) == 2039) {
            list.add("UNPREFERRED_RAT");
            flipped |= 2039;
        }
        if ((o & 2040) == 2040) {
            list.add("PHYSICAL_LINK_CLOSE_IN_PROGRESS");
            flipped |= 2040;
        }
        if ((o & 2041) == 2041) {
            list.add("APN_PENDING_HANDOVER");
            flipped |= 2041;
        }
        if ((o & 2042) == 2042) {
            list.add("PROFILE_BEARER_INCOMPATIBLE");
            flipped |= 2042;
        }
        if ((o & 2043) == 2043) {
            list.add("SIM_CARD_CHANGED");
            flipped |= 2043;
        }
        if ((o & 2044) == 2044) {
            list.add("LOW_POWER_MODE_OR_POWERING_DOWN");
            flipped |= 2044;
        }
        if ((o & 2045) == 2045) {
            list.add("APN_DISABLED");
            flipped |= 2045;
        }
        if ((o & 2046) == 2046) {
            list.add("MAX_PPP_INACTIVITY_TIMER_EXPIRED");
            flipped |= 2046;
        }
        if ((o & 2047) == 2047) {
            list.add("IPV6_ADDRESS_TRANSFER_FAILED");
            flipped |= 2047;
        }
        if ((o & 2048) == 2048) {
            list.add("TRAT_SWAP_FAILED");
            flipped |= 2048;
        }
        if ((o & 2049) == 2049) {
            list.add("EHRPD_TO_HRPD_FALLBACK");
            flipped |= 2049;
        }
        if ((o & 2050) == 2050) {
            list.add("MIP_CONFIG_FAILURE");
            flipped |= 2050;
        }
        if ((o & 2051) == 2051) {
            list.add("PDN_INACTIVITY_TIMER_EXPIRED");
            flipped |= 2051;
        }
        if ((o & 2052) == 2052) {
            list.add("MAX_IPV4_CONNECTIONS");
            flipped |= 2052;
        }
        if ((o & 2053) == 2053) {
            list.add("MAX_IPV6_CONNECTIONS");
            flipped |= 2053;
        }
        if ((o & 2054) == 2054) {
            list.add("APN_MISMATCH");
            flipped |= 2054;
        }
        if ((o & 2055) == 2055) {
            list.add("IP_VERSION_MISMATCH");
            flipped |= 2055;
        }
        if ((o & 2056) == 2056) {
            list.add("DUN_CALL_DISALLOWED");
            flipped |= 2056;
        }
        if ((o & 2057) == 2057) {
            list.add("INTERNAL_EPC_NONEPC_TRANSITION");
            flipped |= 2057;
        }
        if ((o & 2058) == 2058) {
            list.add("INTERFACE_IN_USE");
            flipped |= 2058;
        }
        if ((o & 2059) == 2059) {
            list.add("APN_DISALLOWED_ON_ROAMING");
            flipped |= 2059;
        }
        if ((o & 2060) == 2060) {
            list.add("APN_PARAMETERS_CHANGED");
            flipped |= 2060;
        }
        if ((o & 2061) == 2061) {
            list.add("NULL_APN_DISALLOWED");
            flipped |= 2061;
        }
        if ((o & 2062) == 2062) {
            list.add("THERMAL_MITIGATION");
            flipped |= 2062;
        }
        if ((o & 2063) == 2063) {
            list.add("DATA_SETTINGS_DISABLED");
            flipped |= 2063;
        }
        if ((o & 2064) == 2064) {
            list.add("DATA_ROAMING_SETTINGS_DISABLED");
            flipped |= 2064;
        }
        if ((o & 2065) == 2065) {
            list.add("DDS_SWITCHED");
            flipped |= 2065;
        }
        if ((o & 2066) == 2066) {
            list.add("FORBIDDEN_APN_NAME");
            flipped |= 2066;
        }
        if ((o & 2067) == 2067) {
            list.add("DDS_SWITCH_IN_PROGRESS");
            flipped |= 2067;
        }
        if ((o & 2068) == 2068) {
            list.add("CALL_DISALLOWED_IN_ROAMING");
            flipped |= 2068;
        }
        if ((o & 2069) == 2069) {
            list.add("NON_IP_NOT_SUPPORTED");
            flipped |= 2069;
        }
        if ((o & 2070) == 2070) {
            list.add("PDN_NON_IP_CALL_THROTTLED");
            flipped |= 2070;
        }
        if ((o & 2071) == 2071) {
            list.add("PDN_NON_IP_CALL_DISALLOWED");
            flipped |= 2071;
        }
        if ((o & 2072) == 2072) {
            list.add("CDMA_LOCK");
            flipped |= 2072;
        }
        if ((o & 2073) == 2073) {
            list.add("CDMA_INTERCEPT");
            flipped |= 2073;
        }
        if ((o & 2074) == 2074) {
            list.add("CDMA_REORDER");
            flipped |= 2074;
        }
        if ((o & 2075) == 2075) {
            list.add("CDMA_RELEASE_DUE_TO_SO_REJECTION");
            flipped |= 2075;
        }
        if ((o & 2076) == 2076) {
            list.add("CDMA_INCOMING_CALL");
            flipped |= 2076;
        }
        if ((o & 2077) == 2077) {
            list.add("CDMA_ALERT_STOP");
            flipped |= 2077;
        }
        if ((o & 2078) == 2078) {
            list.add("CHANNEL_ACQUISITION_FAILURE");
            flipped |= 2078;
        }
        if ((o & 2079) == 2079) {
            list.add("MAX_ACCESS_PROBE");
            flipped |= 2079;
        }
        if ((o & 2080) == 2080) {
            list.add("CONCURRENT_SERVICE_NOT_SUPPORTED_BY_BASE_STATION");
            flipped |= 2080;
        }
        if ((o & 2081) == 2081) {
            list.add("NO_RESPONSE_FROM_BASE_STATION");
            flipped |= 2081;
        }
        if ((o & 2082) == 2082) {
            list.add("REJECTED_BY_BASE_STATION");
            flipped |= 2082;
        }
        if ((o & 2083) == 2083) {
            list.add("CONCURRENT_SERVICES_INCOMPATIBLE");
            flipped |= 2083;
        }
        if ((o & 2084) == 2084) {
            list.add("NO_CDMA_SERVICE");
            flipped |= 2084;
        }
        if ((o & 2085) == 2085) {
            list.add("RUIM_NOT_PRESENT");
            flipped |= 2085;
        }
        if ((o & 2086) == 2086) {
            list.add("CDMA_RETRY_ORDER");
            flipped |= 2086;
        }
        if ((o & 2087) == 2087) {
            list.add("ACCESS_BLOCK");
            flipped |= 2087;
        }
        if ((o & 2088) == 2088) {
            list.add("ACCESS_BLOCK_ALL");
            flipped |= 2088;
        }
        if ((o & 2089) == 2089) {
            list.add("IS707B_MAX_ACCESS_PROBES");
            flipped |= 2089;
        }
        if ((o & 2090) == 2090) {
            list.add("THERMAL_EMERGENCY");
            flipped |= 2090;
        }
        if ((o & 2091) == 2091) {
            list.add("CONCURRENT_SERVICES_NOT_ALLOWED");
            flipped |= 2091;
        }
        if ((o & 2092) == 2092) {
            list.add("INCOMING_CALL_REJECTED");
            flipped |= 2092;
        }
        if ((o & 2093) == 2093) {
            list.add("NO_SERVICE_ON_GATEWAY");
            flipped |= 2093;
        }
        if ((o & 2094) == 2094) {
            list.add("NO_GPRS_CONTEXT");
            flipped |= 2094;
        }
        if ((o & 2095) == 2095) {
            list.add("ILLEGAL_MS");
            flipped |= 2095;
        }
        if ((o & 2096) == 2096) {
            list.add("ILLEGAL_ME");
            flipped |= 2096;
        }
        if ((o & 2097) == 2097) {
            list.add("GPRS_SERVICES_AND_NON_GPRS_SERVICES_NOT_ALLOWED");
            flipped |= 2097;
        }
        if ((o & 2098) == 2098) {
            list.add("GPRS_SERVICES_NOT_ALLOWED");
            flipped |= 2098;
        }
        if ((o & 2099) == 2099) {
            list.add("MS_IDENTITY_CANNOT_BE_DERIVED_BY_THE_NETWORK");
            flipped |= 2099;
        }
        if ((o & 2100) == 2100) {
            list.add("IMPLICITLY_DETACHED");
            flipped |= 2100;
        }
        if ((o & 2101) == 2101) {
            list.add("PLMN_NOT_ALLOWED");
            flipped |= 2101;
        }
        if ((o & 2102) == 2102) {
            list.add("LOCATION_AREA_NOT_ALLOWED");
            flipped |= 2102;
        }
        if ((o & 2103) == 2103) {
            list.add("GPRS_SERVICES_NOT_ALLOWED_IN_THIS_PLMN");
            flipped |= 2103;
        }
        if ((o & 2104) == 2104) {
            list.add("PDP_DUPLICATE");
            flipped |= 2104;
        }
        if ((o & 2105) == 2105) {
            list.add("UE_RAT_CHANGE");
            flipped |= 2105;
        }
        if ((o & 2106) == 2106) {
            list.add("CONGESTION");
            flipped |= 2106;
        }
        if ((o & 2107) == 2107) {
            list.add("NO_PDP_CONTEXT_ACTIVATED");
            flipped |= 2107;
        }
        if ((o & 2108) == 2108) {
            list.add("ACCESS_CLASS_DSAC_REJECTION");
            flipped |= 2108;
        }
        if ((o & 2109) == 2109) {
            list.add("PDP_ACTIVATE_MAX_RETRY_FAILED");
            flipped |= 2109;
        }
        if ((o & 2110) == 2110) {
            list.add("RADIO_ACCESS_BEARER_FAILURE");
            flipped |= 2110;
        }
        if ((o & 2111) == 2111) {
            list.add("ESM_UNKNOWN_EPS_BEARER_CONTEXT");
            flipped |= 2111;
        }
        if ((o & 2112) == 2112) {
            list.add("DRB_RELEASED_BY_RRC");
            flipped |= 2112;
        }
        if ((o & 2113) == 2113) {
            list.add("CONNECTION_RELEASED");
            flipped |= 2113;
        }
        if ((o & 2114) == 2114) {
            list.add("EMM_DETACHED");
            flipped |= 2114;
        }
        if ((o & 2115) == 2115) {
            list.add("EMM_ATTACH_FAILED");
            flipped |= 2115;
        }
        if ((o & 2116) == 2116) {
            list.add("EMM_ATTACH_STARTED");
            flipped |= 2116;
        }
        if ((o & 2117) == 2117) {
            list.add("LTE_NAS_SERVICE_REQUEST_FAILED");
            flipped |= 2117;
        }
        if ((o & 2118) == 2118) {
            list.add("DUPLICATE_BEARER_ID");
            flipped |= 2118;
        }
        if ((o & 2119) == 2119) {
            list.add("ESM_COLLISION_SCENARIOS");
            flipped |= 2119;
        }
        if ((o & 2120) == 2120) {
            list.add("ESM_BEARER_DEACTIVATED_TO_SYNC_WITH_NETWORK");
            flipped |= 2120;
        }
        if ((o & 2121) == 2121) {
            list.add("ESM_NW_ACTIVATED_DED_BEARER_WITH_ID_OF_DEF_BEARER");
            flipped |= 2121;
        }
        if ((o & 2122) == 2122) {
            list.add("ESM_BAD_OTA_MESSAGE");
            flipped |= 2122;
        }
        if ((o & 2123) == 2123) {
            list.add("ESM_DOWNLOAD_SERVER_REJECTED_THE_CALL");
            flipped |= 2123;
        }
        if ((o & 2124) == 2124) {
            list.add("ESM_CONTEXT_TRANSFERRED_DUE_TO_IRAT");
            flipped |= 2124;
        }
        if ((o & 2125) == 2125) {
            list.add("DS_EXPLICIT_DEACTIVATION");
            flipped |= 2125;
        }
        if ((o & 2126) == 2126) {
            list.add("ESM_LOCAL_CAUSE_NONE");
            flipped |= 2126;
        }
        if ((o & 2127) == 2127) {
            list.add("LTE_THROTTLING_NOT_REQUIRED");
            flipped |= 2127;
        }
        if ((o & 2128) == 2128) {
            list.add("ACCESS_CONTROL_LIST_CHECK_FAILURE");
            flipped |= 2128;
        }
        if ((o & 2129) == 2129) {
            list.add("SERVICE_NOT_ALLOWED_ON_PLMN");
            flipped |= 2129;
        }
        if ((o & 2130) == 2130) {
            list.add("EMM_T3417_EXPIRED");
            flipped |= 2130;
        }
        if ((o & 2131) == 2131) {
            list.add("EMM_T3417_EXT_EXPIRED");
            flipped |= 2131;
        }
        if ((o & 2132) == 2132) {
            list.add("RRC_UPLINK_DATA_TRANSMISSION_FAILURE");
            flipped |= 2132;
        }
        if ((o & 2133) == 2133) {
            list.add("RRC_UPLINK_DELIVERY_FAILED_DUE_TO_HANDOVER");
            flipped |= 2133;
        }
        if ((o & 2134) == 2134) {
            list.add("RRC_UPLINK_CONNECTION_RELEASE");
            flipped |= 2134;
        }
        if ((o & 2135) == 2135) {
            list.add("RRC_UPLINK_RADIO_LINK_FAILURE");
            flipped |= 2135;
        }
        if ((o & 2136) == 2136) {
            list.add("RRC_UPLINK_ERROR_REQUEST_FROM_NAS");
            flipped |= 2136;
        }
        if ((o & 2137) == 2137) {
            list.add("RRC_CONNECTION_ACCESS_STRATUM_FAILURE");
            flipped |= 2137;
        }
        if ((o & 2138) == 2138) {
            list.add("RRC_CONNECTION_ANOTHER_PROCEDURE_IN_PROGRESS");
            flipped |= 2138;
        }
        if ((o & 2139) == 2139) {
            list.add("RRC_CONNECTION_ACCESS_BARRED");
            flipped |= 2139;
        }
        if ((o & 2140) == 2140) {
            list.add("RRC_CONNECTION_CELL_RESELECTION");
            flipped |= 2140;
        }
        if ((o & 2141) == 2141) {
            list.add("RRC_CONNECTION_CONFIG_FAILURE");
            flipped |= 2141;
        }
        if ((o & 2142) == 2142) {
            list.add("RRC_CONNECTION_TIMER_EXPIRED");
            flipped |= 2142;
        }
        if ((o & 2143) == 2143) {
            list.add("RRC_CONNECTION_LINK_FAILURE");
            flipped |= 2143;
        }
        if ((o & 2144) == 2144) {
            list.add("RRC_CONNECTION_CELL_NOT_CAMPED");
            flipped |= 2144;
        }
        if ((o & 2145) == 2145) {
            list.add("RRC_CONNECTION_SYSTEM_INTERVAL_FAILURE");
            flipped |= 2145;
        }
        if ((o & 2146) == 2146) {
            list.add("RRC_CONNECTION_REJECT_BY_NETWORK");
            flipped |= 2146;
        }
        if ((o & 2147) == 2147) {
            list.add("RRC_CONNECTION_NORMAL_RELEASE");
            flipped |= 2147;
        }
        if ((o & 2148) == 2148) {
            list.add("RRC_CONNECTION_RADIO_LINK_FAILURE");
            flipped |= 2148;
        }
        if ((o & 2149) == 2149) {
            list.add("RRC_CONNECTION_REESTABLISHMENT_FAILURE");
            flipped |= 2149;
        }
        if ((o & 2150) == 2150) {
            list.add("RRC_CONNECTION_OUT_OF_SERVICE_DURING_CELL_REGISTER");
            flipped |= 2150;
        }
        if ((o & 2151) == 2151) {
            list.add("RRC_CONNECTION_ABORT_REQUEST");
            flipped |= 2151;
        }
        if ((o & 2152) == 2152) {
            list.add("RRC_CONNECTION_SYSTEM_INFORMATION_BLOCK_READ_ERROR");
            flipped |= 2152;
        }
        if ((o & 2153) == 2153) {
            list.add("NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH");
            flipped |= 2153;
        }
        if ((o & 2154) == 2154) {
            list.add("NETWORK_INITIATED_DETACH_NO_AUTO_REATTACH");
            flipped |= 2154;
        }
        if ((o & 2155) == 2155) {
            list.add("ESM_PROCEDURE_TIME_OUT");
            flipped |= 2155;
        }
        if ((o & 2156) == 2156) {
            list.add("INVALID_CONNECTION_ID");
            flipped |= 2156;
        }
        if ((o & 2157) == 2157) {
            list.add("MAXIMIUM_NSAPIS_EXCEEDED");
            flipped |= 2157;
        }
        if ((o & 2158) == 2158) {
            list.add("INVALID_PRIMARY_NSAPI");
            flipped |= 2158;
        }
        if ((o & 2159) == 2159) {
            list.add("CANNOT_ENCODE_OTA_MESSAGE");
            flipped |= 2159;
        }
        if ((o & 2160) == 2160) {
            list.add("RADIO_ACCESS_BEARER_SETUP_FAILURE");
            flipped |= 2160;
        }
        if ((o & 2161) == 2161) {
            list.add("PDP_ESTABLISH_TIMEOUT_EXPIRED");
            flipped |= 2161;
        }
        if ((o & 2162) == 2162) {
            list.add("PDP_MODIFY_TIMEOUT_EXPIRED");
            flipped |= 2162;
        }
        if ((o & 2163) == 2163) {
            list.add("PDP_INACTIVE_TIMEOUT_EXPIRED");
            flipped |= 2163;
        }
        if ((o & 2164) == 2164) {
            list.add("PDP_LOWERLAYER_ERROR");
            flipped |= 2164;
        }
        if ((o & 2165) == 2165) {
            list.add("PDP_MODIFY_COLLISION");
            flipped |= 2165;
        }
        if ((o & 2166) == 2166) {
            list.add("MAXINUM_SIZE_OF_L2_MESSAGE_EXCEEDED");
            flipped |= 2166;
        }
        if ((o & 2167) == 2167) {
            list.add("NAS_REQUEST_REJECTED_BY_NETWORK");
            flipped |= 2167;
        }
        if ((o & 2168) == 2168) {
            list.add("RRC_CONNECTION_INVALID_REQUEST");
            flipped |= 2168;
        }
        if ((o & 2169) == 2169) {
            list.add("RRC_CONNECTION_TRACKING_AREA_ID_CHANGED");
            flipped |= 2169;
        }
        if ((o & 2170) == 2170) {
            list.add("RRC_CONNECTION_RF_UNAVAILABLE");
            flipped |= 2170;
        }
        if ((o & 2171) == 2171) {
            list.add("RRC_CONNECTION_ABORTED_DUE_TO_IRAT_CHANGE");
            flipped |= 2171;
        }
        if ((o & 2172) == 2172) {
            list.add("RRC_CONNECTION_RELEASED_SECURITY_NOT_ACTIVE");
            flipped |= 2172;
        }
        if ((o & 2173) == 2173) {
            list.add("RRC_CONNECTION_ABORTED_AFTER_HANDOVER");
            flipped |= 2173;
        }
        if ((o & 2174) == 2174) {
            list.add("RRC_CONNECTION_ABORTED_AFTER_IRAT_CELL_CHANGE");
            flipped |= 2174;
        }
        if ((o & 2175) == 2175) {
            list.add("RRC_CONNECTION_ABORTED_DURING_IRAT_CELL_CHANGE");
            flipped |= 2175;
        }
        if ((o & 2176) == 2176) {
            list.add("IMSI_UNKNOWN_IN_HOME_SUBSCRIBER_SERVER");
            flipped |= 2176;
        }
        if ((o & 2177) == 2177) {
            list.add("IMEI_NOT_ACCEPTED");
            flipped |= 2177;
        }
        if ((o & 2178) == 2178) {
            list.add("EPS_SERVICES_AND_NON_EPS_SERVICES_NOT_ALLOWED");
            flipped |= 2178;
        }
        if ((o & 2179) == 2179) {
            list.add("EPS_SERVICES_NOT_ALLOWED_IN_PLMN");
            flipped |= 2179;
        }
        if ((o & 2180) == 2180) {
            list.add("MSC_TEMPORARILY_NOT_REACHABLE");
            flipped |= 2180;
        }
        if ((o & 2181) == 2181) {
            list.add("CS_DOMAIN_NOT_AVAILABLE");
            flipped |= 2181;
        }
        if ((o & 2182) == 2182) {
            list.add("ESM_FAILURE");
            flipped |= 2182;
        }
        if ((o & 2183) == 2183) {
            list.add("MAC_FAILURE");
            flipped |= 2183;
        }
        if ((o & 2184) == 2184) {
            list.add("SYNCHRONIZATION_FAILURE");
            flipped |= 2184;
        }
        if ((o & 2185) == 2185) {
            list.add("UE_SECURITY_CAPABILITIES_MISMATCH");
            flipped |= 2185;
        }
        if ((o & 2186) == 2186) {
            list.add("SECURITY_MODE_REJECTED");
            flipped |= 2186;
        }
        if ((o & 2187) == 2187) {
            list.add("UNACCEPTABLE_NON_EPS_AUTHENTICATION");
            flipped |= 2187;
        }
        if ((o & 2188) == 2188) {
            list.add("CS_FALLBACK_CALL_ESTABLISHMENT_NOT_ALLOWED");
            flipped |= 2188;
        }
        if ((o & 2189) == 2189) {
            list.add("NO_EPS_BEARER_CONTEXT_ACTIVATED");
            flipped |= 2189;
        }
        if ((o & 2190) == 2190) {
            list.add("INVALID_EMM_STATE");
            flipped |= 2190;
        }
        if ((o & 2191) == 2191) {
            list.add("NAS_LAYER_FAILURE");
            flipped |= 2191;
        }
        if ((o & 2192) == 2192) {
            list.add("MULTIPLE_PDP_CALL_NOT_ALLOWED");
            flipped |= 2192;
        }
        if ((o & 2193) == 2193) {
            list.add("EMBMS_NOT_ENABLED");
            flipped |= 2193;
        }
        if ((o & 2194) == 2194) {
            list.add("IRAT_HANDOVER_FAILED");
            flipped |= 2194;
        }
        if ((o & 2195) == 2195) {
            list.add("EMBMS_REGULAR_DEACTIVATION");
            flipped |= 2195;
        }
        if ((o & 2196) == 2196) {
            list.add("TEST_LOOPBACK_REGULAR_DEACTIVATION");
            flipped |= 2196;
        }
        if ((o & 2197) == 2197) {
            list.add("LOWER_LAYER_REGISTRATION_FAILURE");
            flipped |= 2197;
        }
        if ((o & 2198) == 2198) {
            list.add("DATA_PLAN_EXPIRED");
            flipped |= 2198;
        }
        if ((o & 2199) == 2199) {
            list.add("UMTS_HANDOVER_TO_IWLAN");
            flipped |= 2199;
        }
        if ((o & 2200) == 2200) {
            list.add("EVDO_CONNECTION_DENY_BY_GENERAL_OR_NETWORK_BUSY");
            flipped |= 2200;
        }
        if ((o & 2201) == 2201) {
            list.add("EVDO_CONNECTION_DENY_BY_BILLING_OR_AUTHENTICATION_FAILURE");
            flipped |= 2201;
        }
        if ((o & 2202) == 2202) {
            list.add("EVDO_HDR_CHANGED");
            flipped |= 2202;
        }
        if ((o & 2203) == 2203) {
            list.add("EVDO_HDR_EXITED");
            flipped |= 2203;
        }
        if ((o & 2204) == 2204) {
            list.add("EVDO_HDR_NO_SESSION");
            flipped |= 2204;
        }
        if ((o & 2205) == 2205) {
            list.add("EVDO_USING_GPS_FIX_INSTEAD_OF_HDR_CALL");
            flipped |= 2205;
        }
        if ((o & 2206) == 2206) {
            list.add("EVDO_HDR_CONNECTION_SETUP_TIMEOUT");
            flipped |= 2206;
        }
        if ((o & 2207) == 2207) {
            list.add("FAILED_TO_ACQUIRE_COLOCATED_HDR");
            flipped |= 2207;
        }
        if ((o & 2208) == 2208) {
            list.add("OTASP_COMMIT_IN_PROGRESS");
            flipped |= 2208;
        }
        if ((o & 2209) == 2209) {
            list.add("NO_HYBRID_HDR_SERVICE");
            flipped |= 2209;
        }
        if ((o & 2210) == 2210) {
            list.add("HDR_NO_LOCK_GRANTED");
            flipped |= 2210;
        }
        if ((o & 2211) == 2211) {
            list.add("DBM_OR_SMS_IN_PROGRESS");
            flipped |= 2211;
        }
        if ((o & 2212) == 2212) {
            list.add("HDR_FADE");
            flipped |= 2212;
        }
        if ((o & 2213) == 2213) {
            list.add("HDR_ACCESS_FAILURE");
            flipped |= 2213;
        }
        if ((o & 2214) == 2214) {
            list.add("UNSUPPORTED_1X_PREV");
            flipped |= 2214;
        }
        if ((o & 2215) == 2215) {
            list.add("LOCAL_END");
            flipped |= 2215;
        }
        if ((o & 2216) == 2216) {
            list.add("NO_SERVICE");
            flipped |= 2216;
        }
        if ((o & 2217) == 2217) {
            list.add("FADE");
            flipped |= 2217;
        }
        if ((o & 2218) == 2218) {
            list.add("NORMAL_RELEASE");
            flipped |= 2218;
        }
        if ((o & 2219) == 2219) {
            list.add("ACCESS_ATTEMPT_ALREADY_IN_PROGRESS");
            flipped |= 2219;
        }
        if ((o & 2220) == 2220) {
            list.add("REDIRECTION_OR_HANDOFF_IN_PROGRESS");
            flipped |= 2220;
        }
        if ((o & 2221) == 2221) {
            list.add("EMERGENCY_MODE");
            flipped |= 2221;
        }
        if ((o & 2222) == 2222) {
            list.add("PHONE_IN_USE");
            flipped |= 2222;
        }
        if ((o & 2223) == 2223) {
            list.add("INVALID_MODE");
            flipped |= 2223;
        }
        if ((o & 2224) == 2224) {
            list.add("INVALID_SIM_STATE");
            flipped |= 2224;
        }
        if ((o & 2225) == 2225) {
            list.add("NO_COLLOCATED_HDR");
            flipped |= 2225;
        }
        if ((o & 2226) == 2226) {
            list.add("UE_IS_ENTERING_POWERSAVE_MODE");
            flipped |= 2226;
        }
        if ((o & 2227) == 2227) {
            list.add("DUAL_SWITCH");
            flipped |= 2227;
        }
        if ((o & 2228) == 2228) {
            list.add("PPP_TIMEOUT");
            flipped |= 2228;
        }
        if ((o & 2229) == 2229) {
            list.add("PPP_AUTH_FAILURE");
            flipped |= 2229;
        }
        if ((o & 2230) == 2230) {
            list.add("PPP_OPTION_MISMATCH");
            flipped |= 2230;
        }
        if ((o & 2231) == 2231) {
            list.add("PPP_PAP_FAILURE");
            flipped |= 2231;
        }
        if ((o & 2232) == 2232) {
            list.add("PPP_CHAP_FAILURE");
            flipped |= 2232;
        }
        if ((o & 2233) == 2233) {
            list.add("PPP_CLOSE_IN_PROGRESS");
            flipped |= 2233;
        }
        if ((o & 2234) == 2234) {
            list.add("LIMITED_TO_IPV4");
            flipped |= 2234;
        }
        if ((o & 2235) == 2235) {
            list.add("LIMITED_TO_IPV6");
            flipped |= 2235;
        }
        if ((o & 2236) == 2236) {
            list.add("VSNCP_TIMEOUT");
            flipped |= 2236;
        }
        if ((o & 2237) == 2237) {
            list.add("VSNCP_GEN_ERROR");
            flipped |= 2237;
        }
        if ((o & 2238) == 2238) {
            list.add("VSNCP_APN_UNATHORIZED");
            flipped |= 2238;
        }
        if ((o & 2239) == 2239) {
            list.add("VSNCP_PDN_LIMIT_EXCEEDED");
            flipped |= 2239;
        }
        if ((o & 2240) == 2240) {
            list.add("VSNCP_NO_PDN_GATEWAY_ADDRESS");
            flipped |= 2240;
        }
        if ((o & 2241) == 2241) {
            list.add("VSNCP_PDN_GATEWAY_UNREACHABLE");
            flipped |= 2241;
        }
        if ((o & 2242) == 2242) {
            list.add("VSNCP_PDN_GATEWAY_REJECT");
            flipped |= 2242;
        }
        if ((o & 2243) == 2243) {
            list.add("VSNCP_INSUFFICIENT_PARAMETERS");
            flipped |= 2243;
        }
        if ((o & 2244) == 2244) {
            list.add("VSNCP_RESOURCE_UNAVAILABLE");
            flipped |= 2244;
        }
        if ((o & 2245) == 2245) {
            list.add("VSNCP_ADMINISTRATIVELY_PROHIBITED");
            flipped |= 2245;
        }
        if ((o & 2246) == 2246) {
            list.add("VSNCP_PDN_ID_IN_USE");
            flipped |= 2246;
        }
        if ((o & 2247) == 2247) {
            list.add("VSNCP_SUBSCRIBER_LIMITATION");
            flipped |= 2247;
        }
        if ((o & 2248) == 2248) {
            list.add("VSNCP_PDN_EXISTS_FOR_THIS_APN");
            flipped |= 2248;
        }
        if ((o & 2249) == 2249) {
            list.add("VSNCP_RECONNECT_NOT_ALLOWED");
            flipped |= 2249;
        }
        if ((o & 2250) == 2250) {
            list.add("IPV6_PREFIX_UNAVAILABLE");
            flipped |= 2250;
        }
        if ((o & 2251) == 2251) {
            list.add("HANDOFF_PREFERENCE_CHANGED");
            flipped |= 2251;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

package vendor.huawei.hardware.radio.V1_1;

import java.util.ArrayList;

public final class RilConstS32 {
    public static final int MAX_INTS_NUM = 4;
    public static final int MAX_STRINGS_NUM = 4;
    public static final int RIL_REQUEST_CANCEL_IMS_VIDEO_CALL = 351;
    public static final int RIL_REQUEST_GET_DS_FLOW_NV_WRITE_CFG_PARA = 291;
    public static final int RIL_REQUEST_GET_POL_CAPABILITY = 241;
    public static final int RIL_REQUEST_GET_POL_LIST = 242;
    public static final int RIL_REQUEST_HW_ANT_SWITCH = 239;
    public static final int RIL_REQUEST_HW_AP_SET_DS_FLOW_CONFIG = 288;
    public static final int RIL_REQUEST_HW_CGSMS_MESSAGE = 193;
    public static final int RIL_REQUEST_HW_CHANNEL_INFO = 314;
    public static final int RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO = 268;
    public static final int RIL_REQUEST_HW_DATA_CONNECTION_ATTACH = 189;
    public static final int RIL_REQUEST_HW_DATA_CONNECTION_DETACH = 188;
    public static final int RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY = 340;
    public static final int RIL_REQUEST_HW_DEVICE_BASE = 140;
    public static final int RIL_REQUEST_HW_DEVICE_RESERVED_27 = 167;
    public static final int RIL_REQUEST_HW_DSDS_GET_SIM_STATUS = 274;
    public static final int RIL_REQUEST_HW_EXCHANGE_MODEM_INFO = 348;
    public static final int RIL_REQUEST_HW_FILE_WRITE = 212;
    public static final int RIL_REQUEST_HW_GET_ANT_SWITCH = 240;
    public static final int RIL_REQUEST_HW_GET_BAND_CFG = 248;
    public static final int RIL_REQUEST_HW_GET_CDMA_CHR_INFO = 172;
    public static final int RIL_REQUEST_HW_GET_CDMA_GSM_IMSI = 169;
    public static final int RIL_REQUEST_HW_GET_CDMA_MODE_SIDE = 305;
    public static final int RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA = 341;
    public static final int RIL_REQUEST_HW_GET_CIMSI = 295;
    public static final int RIL_REQUEST_HW_GET_CTROAMINFO = 300;
    public static final int RIL_REQUEST_HW_GET_DATA_CALL_PROFILE = 143;
    public static final int RIL_REQUEST_HW_GET_DATA_PROFILE = 190;
    public static final int RIL_REQUEST_HW_GET_DATA_SUBSCRIPTION = 147;
    public static final int RIL_REQUEST_HW_GET_DEVICE_VERSION = 270;
    public static final int RIL_REQUEST_HW_GET_DS_FLOW_INFO = 267;
    public static final int RIL_REQUEST_HW_GET_EOPLMN_LIST = 216;
    public static final int RIL_REQUEST_HW_GET_HANDLE_DETECT = 208;
    public static final int RIL_REQUEST_HW_GET_ICCID = 252;
    public static final int RIL_REQUEST_HW_GET_IMEI_VERIFY_STATUS = 198;
    public static final int RIL_REQUEST_HW_GET_IMS_DOMAIN = 304;
    public static final int RIL_REQUEST_HW_GET_IMS_SWITCH = 293;
    public static final int RIL_REQUEST_HW_GET_ISMCOEX = 244;
    public static final int RIL_REQUEST_HW_GET_LAA_STATE = 344;
    public static final int RIL_REQUEST_HW_GET_LOCATION_INFO = 174;
    public static final int RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX = 265;
    public static final int RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION = 287;
    public static final int RIL_REQUEST_HW_GET_PLMN_INFO = 219;
    public static final int RIL_REQUEST_HW_GET_PSDOMAIN_AUTOATTACH_TYPE = 201;
    public static final int RIL_REQUEST_HW_GET_QOS_STATUS = 151;
    public static final int RIL_REQUEST_HW_GET_RAT_COMBINE_MODE = 315;
    public static final int RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO = 250;
    public static final int RIL_REQUEST_HW_GET_RCS_SWITCH_STATE = 346;
    public static final int RIL_REQUEST_HW_GET_SCI_CHG_CFG = 273;
    public static final int RIL_REQUEST_HW_GET_SIMLOCK_STATUS = 251;
    public static final int RIL_REQUEST_HW_GET_SIM_CAPACITY = 191;
    public static final int RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE = 173;
    public static final int RIL_REQUEST_HW_GET_SIM_SLOT_CFG = 206;
    public static final int RIL_REQUEST_HW_GET_SYSTEM_INFO_EX = 218;
    public static final int RIL_REQUEST_HW_GET_UICC_FILE = 261;
    public static final int RIL_REQUEST_HW_GET_UICC_SUBSCRIPTION = 146;
    public static final int RIL_REQUEST_HW_GET_USER_SERVICE_STATE = 203;
    public static final int RIL_REQUEST_HW_GET_VOICECALL_BACKGROUND_STATE = 197;
    public static final int RIL_REQUEST_HW_GET_VOICEPREFER_STATUS = 278;
    public static final int RIL_REQUEST_HW_HANDLE_DETECT = 207;
    public static final int RIL_REQUEST_HW_ICC_PREF_APP_SWITCH = 294;
    public static final int RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE = 177;
    public static final int RIL_REQUEST_HW_IMS_ADD_CONFERENCE_MEMBER = 253;
    public static final int RIL_REQUEST_HW_IMS_ANSWER = 231;
    public static final int RIL_REQUEST_HW_IMS_BATTERY_STATUS = 327;
    public static final int RIL_REQUEST_HW_IMS_CANCEL_USSD = 230;
    public static final int RIL_REQUEST_HW_IMS_CHANNEL_INFO = 313;
    public static final int RIL_REQUEST_HW_IMS_CONFERENCE = 226;
    public static final int RIL_REQUEST_HW_IMS_DIAL = 220;
    public static final int RIL_REQUEST_HW_IMS_DOMAIN_CONFIG = 302;
    public static final int RIL_REQUEST_HW_IMS_DTMF = 232;
    public static final int RIL_REQUEST_HW_IMS_DTMF_START = 233;
    public static final int RIL_REQUEST_HW_IMS_DTMF_STOP = 234;
    public static final int RIL_REQUEST_HW_IMS_ECONF_ADD_MEMBER = 283;
    public static final int RIL_REQUEST_HW_IMS_ECONF_DIAL = 279;
    public static final int RIL_REQUEST_HW_IMS_ECONF_REMOVE_MEMBER = 281;
    public static final int RIL_REQUEST_HW_IMS_EXPLICIT_CALL_TRANSFER = 235;
    public static final int RIL_REQUEST_HW_IMS_GET_CLIR = 254;
    public static final int RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS = 221;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_DYN = 322;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_PCSCF = 320;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_SMS = 323;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_TIMER = 321;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_USER = 324;
    public static final int RIL_REQUEST_HW_IMS_GET_ECONF_INFO = 280;
    public static final int RIL_REQUEST_HW_IMS_GET_ECONF_LAST_FAIL_CAUSE = 284;
    public static final int RIL_REQUEST_HW_IMS_GET_NICK_NAME = 326;
    public static final int RIL_REQUEST_HW_IMS_HANGUP = 222;
    public static final int RIL_REQUEST_HW_IMS_HANGUP_FOREGROUND_RESUME_BACKGROUND = 224;
    public static final int RIL_REQUEST_HW_IMS_HANGUP_WAITING_OR_BACKGROUND = 223;
    public static final int RIL_REQUEST_HW_IMS_IMPU = 246;
    public static final int RIL_REQUEST_HW_IMS_IMSVOPS_IND = 237;
    public static final int RIL_REQUEST_HW_IMS_LAST_CALL_CAUSE = 335;
    public static final int RIL_REQUEST_HW_IMS_LAST_CALL_FAIL_CAUSE = 228;
    public static final int RIL_REQUEST_HW_IMS_MERGE_ECONF = 282;
    public static final int RIL_REQUEST_HW_IMS_MODIFY_CALL_CONFIRM = 276;
    public static final int RIL_REQUEST_HW_IMS_MODIFY_CALL_INITIATE = 275;
    public static final int RIL_REQUEST_HW_IMS_REGISTER = 328;
    public static final int RIL_REQUEST_HW_IMS_REGISTRATION_STATE = 141;
    public static final int RIL_REQUEST_HW_IMS_REG_STATE_CHANGE = 236;
    public static final int RIL_REQUEST_HW_IMS_REJ_CALL = 311;
    public static final int RIL_REQUEST_HW_IMS_SEND_SMS = 142;
    public static final int RIL_REQUEST_HW_IMS_SEND_USSD = 229;
    public static final int RIL_REQUEST_HW_IMS_SET_CALL_WAITING = 256;
    public static final int RIL_REQUEST_HW_IMS_SET_CLIR = 255;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_DYN = 317;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_PCSCF = 316;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_SMS = 319;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_TIMER = 318;
    public static final int RIL_REQUEST_HW_IMS_SET_MUTE = 238;
    public static final int RIL_REQUEST_HW_IMS_SET_NICK_NAME = 325;
    public static final int RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE = 225;
    public static final int RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE = 342;
    public static final int RIL_REQUEST_HW_IMS_UDUB = 227;
    public static final int RIL_REQUEST_HW_INFORM_MODEM_TETHER_STATUS = 343;
    public static final int RIL_REQUEST_HW_INITIAL_MESSAGE = 264;
    public static final int RIL_REQUEST_HW_MODEMSTATUS_REPORT = 163;
    public static final int RIL_REQUEST_HW_MODEM_POWER = 179;
    public static final int RIL_REQUEST_HW_MODIFY_CALL_CONFIRM = 156;
    public static final int RIL_REQUEST_HW_MODIFY_CALL_INITIATE = 155;
    public static final int RIL_REQUEST_HW_MODIFY_DATA_PROFILE = 187;
    public static final int RIL_REQUEST_HW_MODIFY_QOS = 152;
    public static final int RIL_REQUEST_HW_MONITOR_SIM_IN_SLOT_IND = 204;
    public static final int RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS = 299;
    public static final int RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID = 333;
    public static final int RIL_REQUEST_HW_QUERY_CARDTYPE = 168;
    public static final int RIL_REQUEST_HW_QUERY_EMERGENCY_NUMBERS = 162;
    public static final int RIL_REQUEST_HW_QUERY_GSM_NMR_INFO = 257;
    public static final int RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND = 307;
    public static final int RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH = 285;
    public static final int RIL_REQUEST_HW_REJ_CALL = 312;
    public static final int RIL_REQUEST_HW_RELEASE_QOS = 150;
    public static final int RIL_REQUEST_HW_RESET_ALL_CONNECTIONS = 194;
    public static final int RIL_REQUEST_HW_RESTRAT_RILD = 182;
    public static final int RIL_REQUEST_HW_RESUME_QOS = 154;
    public static final int RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ = 164;
    public static final int RIL_REQUEST_HW_RRC_CONTROL = 195;
    public static final int RIL_REQUEST_HW_SELECT_CSGID = 334;
    public static final int RIL_REQUEST_HW_SENDAPDU = 159;
    public static final int RIL_REQUEST_HW_SEND_LAA_CMD = 338;
    public static final int RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY = 339;
    public static final int RIL_REQUEST_HW_SETUP_QOS = 149;
    public static final int RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE = 266;
    public static final int RIL_REQUEST_HW_SET_AUDIO_CHANNEL = 160;
    public static final int RIL_REQUEST_HW_SET_BAND_CFG = 247;
    public static final int RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY = 329;
    public static final int RIL_REQUEST_HW_SET_CDMA_MODE_SIDE = 296;
    public static final int RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY = 331;
    public static final int RIL_REQUEST_HW_SET_CT_OOS_COUNT = 301;
    public static final int RIL_REQUEST_HW_SET_DATA_SUBSCRIPTION = 145;
    public static final int RIL_REQUEST_HW_SET_DM_RCS_CFG = 347;
    public static final int RIL_REQUEST_HW_SET_DS_FLOW_REPORT_FLAG = 269;
    public static final int RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS = 178;
    public static final int RIL_REQUEST_HW_SET_EOPLMN_LIST = 217;
    public static final int RIL_REQUEST_HW_SET_IMSVT_CAPABILITY = 336;
    public static final int RIL_REQUEST_HW_SET_IMS_SWITCH = 292;
    public static final int RIL_REQUEST_HW_SET_ISMCOEX = 245;
    public static final int RIL_REQUEST_HW_SET_LONG_MESSAGE = 192;
    public static final int RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION = 286;
    public static final int RIL_REQUEST_HW_SET_NCELL_MONITOR_STATE = 202;
    public static final int RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG = 199;
    public static final int RIL_REQUEST_HW_SET_PCM = 161;
    public static final int RIL_REQUEST_HW_SET_POWER_GRADE = 158;
    public static final int RIL_REQUEST_HW_SET_PSDOMAIN_AUTOATTACH_TYPE = 200;
    public static final int RIL_REQUEST_HW_SET_PSEUDO_INFO = 330;
    public static final int RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO = 249;
    public static final int RIL_REQUEST_HW_SET_RCS_SWITCH = 345;
    public static final int RIL_REQUEST_HW_SET_SIMSLOT_TO_VIA = 170;
    public static final int RIL_REQUEST_HW_SET_SIM_LESS = 180;
    public static final int RIL_REQUEST_HW_SET_SIM_SLOT_CFG = 205;
    public static final int RIL_REQUEST_HW_SET_SUBSCRIPTION_MODE = 148;
    public static final int RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG = 271;
    public static final int RIL_REQUEST_HW_SET_TIME = 308;
    public static final int RIL_REQUEST_HW_SET_TRANSMIT_POWER = 181;
    public static final int RIL_REQUEST_HW_SET_UE_OPERATION_MODE = 297;
    public static final int RIL_REQUEST_HW_SET_UICC_SUBSCRIPTION = 144;
    public static final int RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE = 196;
    public static final int RIL_REQUEST_HW_SET_VOICEPREFER_STATUS = 277;
    public static final int RIL_REQUEST_HW_SET_VT_LTE_QUALITY_RPT_CFG = 310;
    public static final int RIL_REQUEST_HW_SET_WIFI_EMERGENCY_AID = 337;
    public static final int RIL_REQUEST_HW_SET_WIFI_POWER_GRADE = 175;
    public static final int RIL_REQUEST_HW_SIGNAL_STRENGTH = 332;
    public static final int RIL_REQUEST_HW_SIMLOCK_NW_DATA_WRITE = 263;
    public static final int RIL_REQUEST_HW_SIM_CLOSE_CHANNEL = 185;
    public static final int RIL_REQUEST_HW_SIM_GET_ATR = 209;
    public static final int RIL_REQUEST_HW_SIM_OPEN_CHANNEL = 184;
    public static final int RIL_REQUEST_HW_SIM_TRANSMIT_BASIC = 183;
    public static final int RIL_REQUEST_HW_SIM_TRANSMIT_CHANNEL = 186;
    public static final int RIL_REQUEST_HW_STK_CONFIRM_REFRESH = 165;
    public static final int RIL_REQUEST_HW_STK_GET_LOCAL_INFORMATION = 166;
    public static final int RIL_REQUEST_HW_SUSPEND_QOS = 153;
    public static final int RIL_REQUEST_HW_SWITCH_MTKSIM = 157;
    public static final int RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD = 272;
    public static final int RIL_REQUEST_HW_UICC_AUTH = 258;
    public static final int RIL_REQUEST_HW_UICC_GBA_BOOTSTRAP = 259;
    public static final int RIL_REQUEST_HW_UICC_KS_NAF_AUTH = 262;
    public static final int RIL_REQUEST_HW_UPDATE_UICC_FILE = 260;
    public static final int RIL_REQUEST_HW_VOICE_LOOPBACK = 171;
    public static final int RIL_REQUEST_HW_VOLTE_DOMAIN = 350;
    public static final int RIL_REQUEST_HW_VOLTE_IMPI = 349;
    public static final int RIL_REQUEST_HW_VOWIFI_IMSA_MSG = 303;
    public static final int RIL_REQUEST_HW_VOWIFI_UICC_AUTH = 306;
    public static final int RIL_REQUEST_HW_VSIM_CHECK_CARD = 289;
    public static final int RIL_REQUEST_HW_VSIM_GET_ALGROITHM_AND_MODEM_PUBKEY = 210;
    public static final int RIL_REQUEST_HW_VSIM_GET_SIM_CONTENT = 213;
    public static final int RIL_REQUEST_HW_VSIM_GET_SIM_STATE = 215;
    public static final int RIL_REQUEST_HW_VSIM_POWER = 298;
    public static final int RIL_REQUEST_HW_VSIM_SET_SERVER_PUBKEY = 211;
    public static final int RIL_REQUEST_HW_VSIM_SET_SIM_STATE = 214;
    public static final int RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA = 290;
    public static final int RIL_REQUEST_SET_POL_ENTRY = 243;
    public static final int RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2 = 176;
    public static final int RIL_REQUEST_VSIM_BASEBAND_VERSION = 309;
    public static final int RIL_UNSOL_HOOK_HW_VP_STATUS = 1100;
    public static final int RIL_UNSOL_HW_APR_SVLTE_IND = 1090;
    public static final int RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT = 1103;
    public static final int RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT = 1091;
    public static final int RIL_UNSOL_HW_CALL_ALT_SRV = 1129;
    public static final int RIL_UNSOL_HW_CA_STATE_CHANGED = 1105;
    public static final int RIL_UNSOL_HW_CDMA_HPLMN_UPDATE = 1108;
    public static final int RIL_UNSOL_HW_CG_SWITCH_RECOVERY = 1061;
    public static final int RIL_UNSOL_HW_CRR_CONN_IND = 1115;
    public static final int RIL_UNSOL_HW_CS_CHANNEL_INFO_IND = 1071;
    public static final int RIL_UNSOL_HW_CTROAMINFO_CHANGED = 1107;
    public static final int RIL_UNSOL_HW_CURR_GSM_STATE = 1059;
    public static final int RIL_UNSOL_HW_CURR_MCC = 1058;
    public static final int RIL_UNSOL_HW_DEVICE_BASE = 1047;
    public static final int RIL_UNSOL_HW_DIALUP_STATE_CHANGED = 1057;
    public static final int RIL_UNSOL_HW_DSDS_SIM_STATUS_CHANGED = 1096;
    public static final int RIL_UNSOL_HW_DS_FLOW_INFO_REPORT = 1094;
    public static final int RIL_UNSOL_HW_ECCNUM = 1073;
    public static final int RIL_UNSOL_HW_EXIST_NETWORK_INFO = 1102;
    public static final int RIL_UNSOL_HW_IMSA_VOWIFI_MSG = 1109;
    public static final int RIL_UNSOL_HW_IMS_CALL_RING = 1080;
    public static final int RIL_UNSOL_HW_IMS_CS_REDIAL_NOTIFY = 1106;
    public static final int RIL_UNSOL_HW_IMS_DATA_CONNECT_IND = 1110;
    public static final int RIL_UNSOL_HW_IMS_DATA_DISCONNECT_IND = 1111;
    public static final int RIL_UNSOL_HW_IMS_DMCN = 1120;
    public static final int RIL_UNSOL_HW_IMS_ECONF_STATE_CHANGED = 1101;
    public static final int RIL_UNSOL_HW_IMS_HOLD_TONE_IND = 1122;
    public static final int RIL_UNSOL_HW_IMS_MT_STATUS_REPORT = 1104;
    public static final int RIL_UNSOL_HW_IMS_ON_SS = 1085;
    public static final int RIL_UNSOL_HW_IMS_ON_USSD = 1084;
    public static final int RIL_UNSOL_HW_IMS_REG_FAILED_INFO = 1121;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_CALL_STATE_CHANGED = 1079;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_HANDOVER = 1082;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_END_CAUSE = 1098;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_IND = 1097;
    public static final int RIL_UNSOL_HW_IMS_RINGBACK_TONE = 1081;
    public static final int RIL_UNSOL_HW_IMS_SRV_STATUS_UPDATE = 1083;
    public static final int RIL_UNSOL_HW_IMS_SUPP_SVC_NOTIFICATION = 1086;
    public static final int RIL_UNSOL_HW_IMS_VOICE_BAND_INFO = 1087;
    public static final int RIL_UNSOL_HW_INIT_LOCINFO = 1118;
    public static final int RIL_UNSOL_HW_LAA_STATE = 1128;
    public static final int RIL_UNSOL_HW_LIMIT_PDP_ACT_IND = 1119;
    public static final int RIL_UNSOL_HW_LTE_FREQ_WITH_WLAN_COEX = 1093;
    public static final int RIL_UNSOL_HW_LTE_PDCP_INFO = 1113;
    public static final int RIL_UNSOL_HW_LTE_RRC_INFO = 1114;
    public static final int RIL_UNSOL_HW_MCC_CHANGE = 1123;
    public static final int RIL_UNSOL_HW_MIPICLK = 1089;
    public static final int RIL_UNSOL_HW_MODIFY_CALL = 1056;
    public static final int RIL_UNSOL_HW_NCELL_MONITOR = 1072;
    public static final int RIL_UNSOL_HW_NETWORK_REJECT_CASE = 1074;
    public static final int RIL_UNSOL_HW_ON_SS = 1052;
    public static final int RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND = 1078;
    public static final int RIL_UNSOL_HW_PROXY_INIT_STATE_CHANGED = 1092;
    public static final int RIL_UNSOL_HW_QOS_STATE_CHANGED_IND = 1055;
    public static final int RIL_UNSOL_HW_RAT_COMBINE_MODE_IND = 1116;
    public static final int RIL_UNSOL_HW_RESET_CHR_IND = 1066;
    public static final int RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED = 1069;
    public static final int RIL_UNSOL_HW_RESPONSE_DATA_NETWORK_STATE_CHANGED = 1051;
    public static final int RIL_UNSOL_HW_RESPONSE_IMS_NETWORK_STATE_CHANGED = 1049;
    public static final int RIL_UNSOL_HW_RESPONSE_SIMLOCK_STATUS_CHANGED = 1060;
    public static final int RIL_UNSOL_HW_RESPONSE_SIM_TYPE = 1048;
    public static final int RIL_UNSOL_HW_RESPONSE_TETHERED_MODE_STATE_CHANGED = 1050;
    public static final int RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH = 1126;
    public static final int RIL_UNSOL_HW_RIL_CHR_IND = 1065;
    public static final int RIL_UNSOL_HW_ROAMING_MODE_SWITCH = 1112;
    public static final int RIL_UNSOL_HW_SIGNAL_STRENGTH = 1125;
    public static final int RIL_UNSOL_HW_SIMSLOT_CFG = 1064;
    public static final int RIL_UNSOL_HW_SIM_HOTPLUG = 1067;
    public static final int RIL_UNSOL_HW_SIM_ICCID_CHANGED = 1068;
    public static final int RIL_UNSOL_HW_SIM_PNP = 1070;
    public static final int RIL_UNSOL_HW_SIM_SWITCH = 1117;
    public static final int RIL_UNSOL_HW_STK_CC_ALPHA_NOTIFY = 1053;
    public static final int RIL_UNSOL_HW_SVLTE_PS_TRANSFER_IND = 1077;
    public static final int RIL_UNSOL_HW_TEE_TASK_TIME_OUT = 1095;
    public static final int RIL_UNSOL_HW_TETHERED_MODE_STATE_CHANGED = 1062;
    public static final int RIL_UNSOL_HW_TRIGGER_SETUP_DATA_CALL = 1063;
    public static final int RIL_UNSOL_HW_UICC_SUBSCRIPTION_STATUS_CHANGED = 1054;
    public static final int RIL_UNSOL_HW_UIM_LOCKCARD = 1088;
    public static final int RIL_UNSOL_HW_VSIM_OTA_SMS_REPORT = 1076;
    public static final int RIL_UNSOL_HW_VSIM_RDH_REQUEST = 1075;
    public static final int RIL_UNSOL_HW_VT_FLOW_INFO_REPORT = 1127;
    public static final int RIL_UNSOL_HW_XPASS_INFO_RPT = 1099;
    public static final int RIL_UNSOL_RSRVCC_STATE_NOTIFY = 1124;

    public static final String toString(int o) {
        if (o == RIL_REQUEST_HW_SET_RCS_SWITCH) {
            return "RIL_REQUEST_HW_SET_RCS_SWITCH";
        }
        if (o == RIL_REQUEST_HW_GET_RCS_SWITCH_STATE) {
            return "RIL_REQUEST_HW_GET_RCS_SWITCH_STATE";
        }
        if (o == RIL_REQUEST_HW_SET_DM_RCS_CFG) {
            return "RIL_REQUEST_HW_SET_DM_RCS_CFG";
        }
        if (o == RIL_REQUEST_HW_EXCHANGE_MODEM_INFO) {
            return "RIL_REQUEST_HW_EXCHANGE_MODEM_INFO";
        }
        if (o == RIL_REQUEST_HW_VOLTE_IMPI) {
            return "RIL_REQUEST_HW_VOLTE_IMPI";
        }
        if (o == RIL_REQUEST_HW_VOLTE_DOMAIN) {
            return "RIL_REQUEST_HW_VOLTE_DOMAIN";
        }
        if (o == RIL_REQUEST_CANCEL_IMS_VIDEO_CALL) {
            return "RIL_REQUEST_CANCEL_IMS_VIDEO_CALL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & RIL_REQUEST_HW_SET_RCS_SWITCH) == RIL_REQUEST_HW_SET_RCS_SWITCH) {
            list.add("RIL_REQUEST_HW_SET_RCS_SWITCH");
            flipped = RIL_REQUEST_HW_SET_RCS_SWITCH;
        }
        if ((o & RIL_REQUEST_HW_GET_RCS_SWITCH_STATE) == RIL_REQUEST_HW_GET_RCS_SWITCH_STATE) {
            list.add("RIL_REQUEST_HW_GET_RCS_SWITCH_STATE");
            flipped |= RIL_REQUEST_HW_GET_RCS_SWITCH_STATE;
        }
        if ((o & RIL_REQUEST_HW_SET_DM_RCS_CFG) == RIL_REQUEST_HW_SET_DM_RCS_CFG) {
            list.add("RIL_REQUEST_HW_SET_DM_RCS_CFG");
            flipped |= RIL_REQUEST_HW_SET_DM_RCS_CFG;
        }
        if ((o & RIL_REQUEST_HW_EXCHANGE_MODEM_INFO) == RIL_REQUEST_HW_EXCHANGE_MODEM_INFO) {
            list.add("RIL_REQUEST_HW_EXCHANGE_MODEM_INFO");
            flipped |= RIL_REQUEST_HW_EXCHANGE_MODEM_INFO;
        }
        if ((o & RIL_REQUEST_HW_VOLTE_IMPI) == RIL_REQUEST_HW_VOLTE_IMPI) {
            list.add("RIL_REQUEST_HW_VOLTE_IMPI");
            flipped |= RIL_REQUEST_HW_VOLTE_IMPI;
        }
        if ((o & RIL_REQUEST_HW_VOLTE_DOMAIN) == RIL_REQUEST_HW_VOLTE_DOMAIN) {
            list.add("RIL_REQUEST_HW_VOLTE_DOMAIN");
            flipped |= RIL_REQUEST_HW_VOLTE_DOMAIN;
        }
        if ((o & RIL_REQUEST_CANCEL_IMS_VIDEO_CALL) == RIL_REQUEST_CANCEL_IMS_VIDEO_CALL) {
            list.add("RIL_REQUEST_CANCEL_IMS_VIDEO_CALL");
            flipped |= RIL_REQUEST_CANCEL_IMS_VIDEO_CALL;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

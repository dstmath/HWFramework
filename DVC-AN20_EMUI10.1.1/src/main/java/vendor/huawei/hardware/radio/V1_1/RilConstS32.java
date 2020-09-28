package vendor.huawei.hardware.radio.V1_1;

import java.util.ArrayList;

public final class RilConstS32 {
    public static final int MAX_INTS_NUM = 4;
    public static final int MAX_STRINGS_NUM = 4;
    public static final int RIL_REQUEST_GET_DS_FLOW_NV_WRITE_CFG_PARA = 290;
    public static final int RIL_REQUEST_GET_POL_CAPABILITY = 241;
    public static final int RIL_REQUEST_GET_POL_LIST = 242;
    public static final int RIL_REQUEST_HW_ANT_SWITCH = 239;
    public static final int RIL_REQUEST_HW_AP_SET_DS_FLOW_CONFIG = 287;
    public static final int RIL_REQUEST_HW_CGSMS_MESSAGE = 193;
    public static final int RIL_REQUEST_HW_CHANNEL_INFO = 313;
    public static final int RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO = 267;
    public static final int RIL_REQUEST_HW_DATA_CONNECTION_ATTACH = 189;
    public static final int RIL_REQUEST_HW_DATA_CONNECTION_DETACH = 188;
    public static final int RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY = 339;
    public static final int RIL_REQUEST_HW_DEVICE_BASE = 140;
    public static final int RIL_REQUEST_HW_DEVICE_RESERVED_27 = 167;
    public static final int RIL_REQUEST_HW_DSDS_GET_SIM_STATUS = 273;
    public static final int RIL_REQUEST_HW_EXCHANGE_MODEM_INFO = 346;
    public static final int RIL_REQUEST_HW_FILE_WRITE = 212;
    public static final int RIL_REQUEST_HW_GET_ANT_SWITCH = 240;
    public static final int RIL_REQUEST_HW_GET_BAND_CFG = 248;
    public static final int RIL_REQUEST_HW_GET_CDMA_CHR_INFO = 172;
    public static final int RIL_REQUEST_HW_GET_CDMA_GSM_IMSI = 169;
    public static final int RIL_REQUEST_HW_GET_CDMA_MODE_SIDE = 304;
    public static final int RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA = 340;
    public static final int RIL_REQUEST_HW_GET_CIMSI = 294;
    public static final int RIL_REQUEST_HW_GET_CTROAMINFO = 299;
    public static final int RIL_REQUEST_HW_GET_DATA_CALL_PROFILE = 143;
    public static final int RIL_REQUEST_HW_GET_DATA_PROFILE = 190;
    public static final int RIL_REQUEST_HW_GET_DATA_SUBSCRIPTION = 147;
    public static final int RIL_REQUEST_HW_GET_DEVICE_VERSION = 269;
    public static final int RIL_REQUEST_HW_GET_DS_FLOW_INFO = 266;
    public static final int RIL_REQUEST_HW_GET_EOPLMN_LIST = 216;
    public static final int RIL_REQUEST_HW_GET_HANDLE_DETECT = 208;
    public static final int RIL_REQUEST_HW_GET_ICCID = 252;
    public static final int RIL_REQUEST_HW_GET_IMEI_VERIFY_STATUS = 198;
    public static final int RIL_REQUEST_HW_GET_IMS_DOMAIN = 303;
    public static final int RIL_REQUEST_HW_GET_IMS_SWITCH = 292;
    public static final int RIL_REQUEST_HW_GET_ISMCOEX = 244;
    public static final int RIL_REQUEST_HW_GET_LAA_STATE = 342;
    public static final int RIL_REQUEST_HW_GET_LOCATION_INFO = 174;
    public static final int RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX = 264;
    public static final int RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION = 286;
    public static final int RIL_REQUEST_HW_GET_PLMN_INFO = 219;
    public static final int RIL_REQUEST_HW_GET_PSDOMAIN_AUTOATTACH_TYPE = 201;
    public static final int RIL_REQUEST_HW_GET_QOS_STATUS = 151;
    public static final int RIL_REQUEST_HW_GET_RAT_COMBINE_MODE = 314;
    public static final int RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO = 250;
    public static final int RIL_REQUEST_HW_GET_RCS_SWITCH_STATE = 344;
    public static final int RIL_REQUEST_HW_GET_SCI_CHG_CFG = 272;
    public static final int RIL_REQUEST_HW_GET_SIMLOCK_STATUS = 251;
    public static final int RIL_REQUEST_HW_GET_SIM_CAPACITY = 191;
    public static final int RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE = 173;
    public static final int RIL_REQUEST_HW_GET_SIM_SLOT_CFG = 206;
    public static final int RIL_REQUEST_HW_GET_SYSTEM_INFO_EX = 218;
    public static final int RIL_REQUEST_HW_GET_UICC_FILE = 261;
    public static final int RIL_REQUEST_HW_GET_UICC_SUBSCRIPTION = 146;
    public static final int RIL_REQUEST_HW_GET_USER_SERVICE_STATE = 203;
    public static final int RIL_REQUEST_HW_GET_VOICECALL_BACKGROUND_STATE = 197;
    public static final int RIL_REQUEST_HW_GET_VOICEPREFER_STATUS = 277;
    public static final int RIL_REQUEST_HW_HANDLE_DETECT = 207;
    public static final int RIL_REQUEST_HW_ICC_PREF_APP_SWITCH = 293;
    public static final int RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE = 177;
    public static final int RIL_REQUEST_HW_IMS_ADD_CONFERENCE_MEMBER = 253;
    public static final int RIL_REQUEST_HW_IMS_ANSWER = 231;
    public static final int RIL_REQUEST_HW_IMS_BATTERY_STATUS = 326;
    public static final int RIL_REQUEST_HW_IMS_CANCEL_USSD = 230;
    public static final int RIL_REQUEST_HW_IMS_CHANNEL_INFO = 312;
    public static final int RIL_REQUEST_HW_IMS_CONFERENCE = 226;
    public static final int RIL_REQUEST_HW_IMS_DIAL = 220;
    public static final int RIL_REQUEST_HW_IMS_DOMAIN_CONFIG = 301;
    public static final int RIL_REQUEST_HW_IMS_DTMF = 232;
    public static final int RIL_REQUEST_HW_IMS_DTMF_START = 233;
    public static final int RIL_REQUEST_HW_IMS_DTMF_STOP = 234;
    public static final int RIL_REQUEST_HW_IMS_ECONF_ADD_MEMBER = 282;
    public static final int RIL_REQUEST_HW_IMS_ECONF_DIAL = 278;
    public static final int RIL_REQUEST_HW_IMS_ECONF_REMOVE_MEMBER = 280;
    public static final int RIL_REQUEST_HW_IMS_EXPLICIT_CALL_TRANSFER = 235;
    public static final int RIL_REQUEST_HW_IMS_GET_CLIR = 254;
    public static final int RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS = 221;
    public static final int RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_WITH_IMS_DOMAIN = 348;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_DYN = 321;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_PCSCF = 319;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_SMS = 322;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_TIMER = 320;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_USER = 323;
    public static final int RIL_REQUEST_HW_IMS_GET_ECONF_INFO = 279;
    public static final int RIL_REQUEST_HW_IMS_GET_ECONF_LAST_FAIL_CAUSE = 283;
    public static final int RIL_REQUEST_HW_IMS_GET_NICK_NAME = 325;
    public static final int RIL_REQUEST_HW_IMS_HANGUP = 222;
    public static final int RIL_REQUEST_HW_IMS_HANGUP_FOREGROUND_RESUME_BACKGROUND = 224;
    public static final int RIL_REQUEST_HW_IMS_HANGUP_WAITING_OR_BACKGROUND = 223;
    public static final int RIL_REQUEST_HW_IMS_IMPU = 246;
    public static final int RIL_REQUEST_HW_IMS_IMSVOPS_IND = 237;
    public static final int RIL_REQUEST_HW_IMS_LAST_CALL_CAUSE = 334;
    public static final int RIL_REQUEST_HW_IMS_LAST_CALL_FAIL_CAUSE = 228;
    public static final int RIL_REQUEST_HW_IMS_MERGE_ECONF = 281;
    public static final int RIL_REQUEST_HW_IMS_MODIFY_CALL_CONFIRM = 275;
    public static final int RIL_REQUEST_HW_IMS_MODIFY_CALL_INITIATE = 274;
    public static final int RIL_REQUEST_HW_IMS_REGISTER = 327;
    public static final int RIL_REQUEST_HW_IMS_REGISTRATION_STATE = 141;
    public static final int RIL_REQUEST_HW_IMS_REG_STATE_CHANGE = 236;
    public static final int RIL_REQUEST_HW_IMS_REJ_CALL = 310;
    public static final int RIL_REQUEST_HW_IMS_SEND_SMS = 142;
    public static final int RIL_REQUEST_HW_IMS_SEND_USSD = 229;
    public static final int RIL_REQUEST_HW_IMS_SET_CALL_WAITING = 256;
    public static final int RIL_REQUEST_HW_IMS_SET_CLIR = 255;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_DYN = 316;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_PCSCF = 315;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_SMS = 318;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_TIMER = 317;
    public static final int RIL_REQUEST_HW_IMS_SET_MUTE = 238;
    public static final int RIL_REQUEST_HW_IMS_SET_NICK_NAME = 324;
    public static final int RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE = 225;
    public static final int RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE = 341;
    public static final int RIL_REQUEST_HW_IMS_UDUB = 227;
    public static final int RIL_REQUEST_HW_INFORM_MODEM_TETHER_STATUS = 347;
    public static final int RIL_REQUEST_HW_INITIAL_MESSAGE = 263;
    public static final int RIL_REQUEST_HW_MODEMSTATUS_REPORT = 163;
    public static final int RIL_REQUEST_HW_MODEM_POWER = 179;
    public static final int RIL_REQUEST_HW_MODIFY_CALL_CONFIRM = 156;
    public static final int RIL_REQUEST_HW_MODIFY_CALL_INITIATE = 155;
    public static final int RIL_REQUEST_HW_MODIFY_DATA_PROFILE = 187;
    public static final int RIL_REQUEST_HW_MODIFY_QOS = 152;
    public static final int RIL_REQUEST_HW_MONITOR_SIM_IN_SLOT_IND = 204;
    public static final int RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS = 298;
    public static final int RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID = 332;
    public static final int RIL_REQUEST_HW_QUERY_CARDTYPE = 168;
    public static final int RIL_REQUEST_HW_QUERY_EMERGENCY_NUMBERS = 162;
    public static final int RIL_REQUEST_HW_QUERY_GSM_NMR_INFO = 257;
    public static final int RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND = 306;
    public static final int RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH = 284;
    public static final int RIL_REQUEST_HW_REJ_CALL = 311;
    public static final int RIL_REQUEST_HW_RELEASE_QOS = 150;
    public static final int RIL_REQUEST_HW_RESET_ALL_CONNECTIONS = 194;
    public static final int RIL_REQUEST_HW_RESTRAT_RILD = 182;
    public static final int RIL_REQUEST_HW_RESUME_QOS = 154;
    public static final int RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ = 164;
    public static final int RIL_REQUEST_HW_RRC_CONTROL = 195;
    public static final int RIL_REQUEST_HW_SELECT_CSGID = 333;
    public static final int RIL_REQUEST_HW_SENDAPDU = 159;
    public static final int RIL_REQUEST_HW_SEND_LAA_CMD = 337;
    public static final int RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY = 338;
    public static final int RIL_REQUEST_HW_SETUP_QOS = 149;
    public static final int RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE = 265;
    public static final int RIL_REQUEST_HW_SET_AUDIO_CHANNEL = 160;
    public static final int RIL_REQUEST_HW_SET_BAND_CFG = 247;
    public static final int RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY = 328;
    public static final int RIL_REQUEST_HW_SET_CDMA_MODE_SIDE = 295;
    public static final int RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY = 330;
    public static final int RIL_REQUEST_HW_SET_CT_OOS_COUNT = 300;
    public static final int RIL_REQUEST_HW_SET_DATA_SUBSCRIPTION = 145;
    public static final int RIL_REQUEST_HW_SET_DM_RCS_CFG = 345;
    public static final int RIL_REQUEST_HW_SET_DS_FLOW_REPORT_FLAG = 268;
    public static final int RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS = 178;
    public static final int RIL_REQUEST_HW_SET_EOPLMN_LIST = 217;
    public static final int RIL_REQUEST_HW_SET_IMSVT_CAPABILITY = 335;
    public static final int RIL_REQUEST_HW_SET_IMS_SWITCH = 291;
    public static final int RIL_REQUEST_HW_SET_ISMCOEX = 245;
    public static final int RIL_REQUEST_HW_SET_LONG_MESSAGE = 192;
    public static final int RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION = 285;
    public static final int RIL_REQUEST_HW_SET_NCELL_MONITOR_STATE = 202;
    public static final int RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG = 199;
    public static final int RIL_REQUEST_HW_SET_PCM = 161;
    public static final int RIL_REQUEST_HW_SET_POWER_GRADE = 158;
    public static final int RIL_REQUEST_HW_SET_PSDOMAIN_AUTOATTACH_TYPE = 200;
    public static final int RIL_REQUEST_HW_SET_PSEUDO_INFO = 329;
    public static final int RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO = 249;
    public static final int RIL_REQUEST_HW_SET_RCS_SWITCH = 343;
    public static final int RIL_REQUEST_HW_SET_SIMSLOT_TO_VIA = 170;
    public static final int RIL_REQUEST_HW_SET_SIM_LESS = 180;
    public static final int RIL_REQUEST_HW_SET_SIM_SLOT_CFG = 205;
    public static final int RIL_REQUEST_HW_SET_SUBSCRIPTION_MODE = 148;
    public static final int RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG = 270;
    public static final int RIL_REQUEST_HW_SET_TIME = 307;
    public static final int RIL_REQUEST_HW_SET_TRANSMIT_POWER = 181;
    public static final int RIL_REQUEST_HW_SET_UE_OPERATION_MODE = 296;
    public static final int RIL_REQUEST_HW_SET_UICC_SUBSCRIPTION = 144;
    public static final int RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE = 196;
    public static final int RIL_REQUEST_HW_SET_VOICEPREFER_STATUS = 276;
    public static final int RIL_REQUEST_HW_SET_VT_LTE_QUALITY_RPT_CFG = 309;
    public static final int RIL_REQUEST_HW_SET_WIFI_EMERGENCY_AID = 336;
    public static final int RIL_REQUEST_HW_SET_WIFI_POWER_GRADE = 175;
    public static final int RIL_REQUEST_HW_SIGNAL_STRENGTH = 331;
    public static final int RIL_REQUEST_HW_SIM_CLOSE_CHANNEL = 185;
    public static final int RIL_REQUEST_HW_SIM_GET_ATR = 209;
    public static final int RIL_REQUEST_HW_SIM_OPEN_CHANNEL = 184;
    public static final int RIL_REQUEST_HW_SIM_TRANSMIT_BASIC = 183;
    public static final int RIL_REQUEST_HW_SIM_TRANSMIT_CHANNEL = 186;
    public static final int RIL_REQUEST_HW_STK_CONFIRM_REFRESH = 165;
    public static final int RIL_REQUEST_HW_STK_GET_LOCAL_INFORMATION = 166;
    public static final int RIL_REQUEST_HW_SUSPEND_QOS = 153;
    public static final int RIL_REQUEST_HW_SWITCH_MTKSIM = 157;
    public static final int RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD = 271;
    public static final int RIL_REQUEST_HW_UICC_AUTH = 258;
    public static final int RIL_REQUEST_HW_UICC_GBA_BOOTSTRAP = 259;
    public static final int RIL_REQUEST_HW_UICC_KS_NAF_AUTH = 262;
    public static final int RIL_REQUEST_HW_UPDATE_UICC_FILE = 260;
    public static final int RIL_REQUEST_HW_VOICE_LOOPBACK = 171;
    public static final int RIL_REQUEST_HW_VOWIFI_IMSA_MSG = 302;
    public static final int RIL_REQUEST_HW_VOWIFI_UICC_AUTH = 305;
    public static final int RIL_REQUEST_HW_VSIM_CHECK_CARD = 288;
    public static final int RIL_REQUEST_HW_VSIM_GET_ALGROITHM_AND_MODEM_PUBKEY = 210;
    public static final int RIL_REQUEST_HW_VSIM_GET_SIM_CONTENT = 213;
    public static final int RIL_REQUEST_HW_VSIM_GET_SIM_STATE = 215;
    public static final int RIL_REQUEST_HW_VSIM_POWER = 297;
    public static final int RIL_REQUEST_HW_VSIM_SET_SERVER_PUBKEY = 211;
    public static final int RIL_REQUEST_HW_VSIM_SET_SIM_STATE = 214;
    public static final int RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA = 289;
    public static final int RIL_REQUEST_SET_POL_ENTRY = 243;
    public static final int RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2 = 176;
    public static final int RIL_REQUEST_VSIM_BASEBAND_VERSION = 308;
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
        if (o == 4) {
            return "MAX_INTS_NUM";
        }
        if (o == 4) {
            return "MAX_STRINGS_NUM";
        }
        if (o == 140) {
            return "RIL_REQUEST_HW_DEVICE_BASE";
        }
        if (o == 141) {
            return "RIL_REQUEST_HW_IMS_REGISTRATION_STATE";
        }
        if (o == 142) {
            return "RIL_REQUEST_HW_IMS_SEND_SMS";
        }
        if (o == 143) {
            return "RIL_REQUEST_HW_GET_DATA_CALL_PROFILE";
        }
        if (o == 144) {
            return "RIL_REQUEST_HW_SET_UICC_SUBSCRIPTION";
        }
        if (o == 145) {
            return "RIL_REQUEST_HW_SET_DATA_SUBSCRIPTION";
        }
        if (o == 146) {
            return "RIL_REQUEST_HW_GET_UICC_SUBSCRIPTION";
        }
        if (o == 147) {
            return "RIL_REQUEST_HW_GET_DATA_SUBSCRIPTION";
        }
        if (o == 148) {
            return "RIL_REQUEST_HW_SET_SUBSCRIPTION_MODE";
        }
        if (o == 149) {
            return "RIL_REQUEST_HW_SETUP_QOS";
        }
        if (o == 150) {
            return "RIL_REQUEST_HW_RELEASE_QOS";
        }
        if (o == 151) {
            return "RIL_REQUEST_HW_GET_QOS_STATUS";
        }
        if (o == 152) {
            return "RIL_REQUEST_HW_MODIFY_QOS";
        }
        if (o == 153) {
            return "RIL_REQUEST_HW_SUSPEND_QOS";
        }
        if (o == 154) {
            return "RIL_REQUEST_HW_RESUME_QOS";
        }
        if (o == 155) {
            return "RIL_REQUEST_HW_MODIFY_CALL_INITIATE";
        }
        if (o == 156) {
            return "RIL_REQUEST_HW_MODIFY_CALL_CONFIRM";
        }
        if (o == 157) {
            return "RIL_REQUEST_HW_SWITCH_MTKSIM";
        }
        if (o == 158) {
            return "RIL_REQUEST_HW_SET_POWER_GRADE";
        }
        if (o == 159) {
            return "RIL_REQUEST_HW_SENDAPDU";
        }
        if (o == 160) {
            return "RIL_REQUEST_HW_SET_AUDIO_CHANNEL";
        }
        if (o == 161) {
            return "RIL_REQUEST_HW_SET_PCM";
        }
        if (o == 162) {
            return "RIL_REQUEST_HW_QUERY_EMERGENCY_NUMBERS";
        }
        if (o == 163) {
            return "RIL_REQUEST_HW_MODEMSTATUS_REPORT";
        }
        if (o == 164) {
            return "RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ";
        }
        if (o == 165) {
            return "RIL_REQUEST_HW_STK_CONFIRM_REFRESH";
        }
        if (o == 166) {
            return "RIL_REQUEST_HW_STK_GET_LOCAL_INFORMATION";
        }
        if (o == 167) {
            return "RIL_REQUEST_HW_DEVICE_RESERVED_27";
        }
        if (o == 168) {
            return "RIL_REQUEST_HW_QUERY_CARDTYPE";
        }
        if (o == 169) {
            return "RIL_REQUEST_HW_GET_CDMA_GSM_IMSI";
        }
        if (o == 170) {
            return "RIL_REQUEST_HW_SET_SIMSLOT_TO_VIA";
        }
        if (o == 171) {
            return "RIL_REQUEST_HW_VOICE_LOOPBACK";
        }
        if (o == 172) {
            return "RIL_REQUEST_HW_GET_CDMA_CHR_INFO";
        }
        if (o == 173) {
            return "RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE";
        }
        if (o == 174) {
            return "RIL_REQUEST_HW_GET_LOCATION_INFO";
        }
        if (o == 175) {
            return "RIL_REQUEST_HW_SET_WIFI_POWER_GRADE";
        }
        if (o == 176) {
            return "RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2";
        }
        if (o == 177) {
            return "RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE";
        }
        if (o == 178) {
            return "RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS";
        }
        if (o == 179) {
            return "RIL_REQUEST_HW_MODEM_POWER";
        }
        if (o == 180) {
            return "RIL_REQUEST_HW_SET_SIM_LESS";
        }
        if (o == 181) {
            return "RIL_REQUEST_HW_SET_TRANSMIT_POWER";
        }
        if (o == 182) {
            return "RIL_REQUEST_HW_RESTRAT_RILD";
        }
        if (o == 183) {
            return "RIL_REQUEST_HW_SIM_TRANSMIT_BASIC";
        }
        if (o == 184) {
            return "RIL_REQUEST_HW_SIM_OPEN_CHANNEL";
        }
        if (o == 185) {
            return "RIL_REQUEST_HW_SIM_CLOSE_CHANNEL";
        }
        if (o == 186) {
            return "RIL_REQUEST_HW_SIM_TRANSMIT_CHANNEL";
        }
        if (o == 187) {
            return "RIL_REQUEST_HW_MODIFY_DATA_PROFILE";
        }
        if (o == 188) {
            return "RIL_REQUEST_HW_DATA_CONNECTION_DETACH";
        }
        if (o == 189) {
            return "RIL_REQUEST_HW_DATA_CONNECTION_ATTACH";
        }
        if (o == 190) {
            return "RIL_REQUEST_HW_GET_DATA_PROFILE";
        }
        if (o == 191) {
            return "RIL_REQUEST_HW_GET_SIM_CAPACITY";
        }
        if (o == 192) {
            return "RIL_REQUEST_HW_SET_LONG_MESSAGE";
        }
        if (o == 193) {
            return "RIL_REQUEST_HW_CGSMS_MESSAGE";
        }
        if (o == 194) {
            return "RIL_REQUEST_HW_RESET_ALL_CONNECTIONS";
        }
        if (o == 195) {
            return "RIL_REQUEST_HW_RRC_CONTROL";
        }
        if (o == 196) {
            return "RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE";
        }
        if (o == 197) {
            return "RIL_REQUEST_HW_GET_VOICECALL_BACKGROUND_STATE";
        }
        if (o == 198) {
            return "RIL_REQUEST_HW_GET_IMEI_VERIFY_STATUS";
        }
        if (o == 199) {
            return "RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG";
        }
        if (o == 200) {
            return "RIL_REQUEST_HW_SET_PSDOMAIN_AUTOATTACH_TYPE";
        }
        if (o == 201) {
            return "RIL_REQUEST_HW_GET_PSDOMAIN_AUTOATTACH_TYPE";
        }
        if (o == 202) {
            return "RIL_REQUEST_HW_SET_NCELL_MONITOR_STATE";
        }
        if (o == 203) {
            return "RIL_REQUEST_HW_GET_USER_SERVICE_STATE";
        }
        if (o == 204) {
            return "RIL_REQUEST_HW_MONITOR_SIM_IN_SLOT_IND";
        }
        if (o == 205) {
            return "RIL_REQUEST_HW_SET_SIM_SLOT_CFG";
        }
        if (o == 206) {
            return "RIL_REQUEST_HW_GET_SIM_SLOT_CFG";
        }
        if (o == 207) {
            return "RIL_REQUEST_HW_HANDLE_DETECT";
        }
        if (o == 208) {
            return "RIL_REQUEST_HW_GET_HANDLE_DETECT";
        }
        if (o == 209) {
            return "RIL_REQUEST_HW_SIM_GET_ATR";
        }
        if (o == 210) {
            return "RIL_REQUEST_HW_VSIM_GET_ALGROITHM_AND_MODEM_PUBKEY";
        }
        if (o == 211) {
            return "RIL_REQUEST_HW_VSIM_SET_SERVER_PUBKEY";
        }
        if (o == 212) {
            return "RIL_REQUEST_HW_FILE_WRITE";
        }
        if (o == 213) {
            return "RIL_REQUEST_HW_VSIM_GET_SIM_CONTENT";
        }
        if (o == 214) {
            return "RIL_REQUEST_HW_VSIM_SET_SIM_STATE";
        }
        if (o == 215) {
            return "RIL_REQUEST_HW_VSIM_GET_SIM_STATE";
        }
        if (o == 216) {
            return "RIL_REQUEST_HW_GET_EOPLMN_LIST";
        }
        if (o == 217) {
            return "RIL_REQUEST_HW_SET_EOPLMN_LIST";
        }
        if (o == 218) {
            return "RIL_REQUEST_HW_GET_SYSTEM_INFO_EX";
        }
        if (o == 219) {
            return "RIL_REQUEST_HW_GET_PLMN_INFO";
        }
        if (o == 220) {
            return "RIL_REQUEST_HW_IMS_DIAL";
        }
        if (o == 221) {
            return "RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS";
        }
        if (o == 222) {
            return "RIL_REQUEST_HW_IMS_HANGUP";
        }
        if (o == 223) {
            return "RIL_REQUEST_HW_IMS_HANGUP_WAITING_OR_BACKGROUND";
        }
        if (o == 224) {
            return "RIL_REQUEST_HW_IMS_HANGUP_FOREGROUND_RESUME_BACKGROUND";
        }
        if (o == 225) {
            return "RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE";
        }
        if (o == 226) {
            return "RIL_REQUEST_HW_IMS_CONFERENCE";
        }
        if (o == 227) {
            return "RIL_REQUEST_HW_IMS_UDUB";
        }
        if (o == 228) {
            return "RIL_REQUEST_HW_IMS_LAST_CALL_FAIL_CAUSE";
        }
        if (o == 229) {
            return "RIL_REQUEST_HW_IMS_SEND_USSD";
        }
        if (o == 230) {
            return "RIL_REQUEST_HW_IMS_CANCEL_USSD";
        }
        if (o == 231) {
            return "RIL_REQUEST_HW_IMS_ANSWER";
        }
        if (o == 232) {
            return "RIL_REQUEST_HW_IMS_DTMF";
        }
        if (o == 233) {
            return "RIL_REQUEST_HW_IMS_DTMF_START";
        }
        if (o == 234) {
            return "RIL_REQUEST_HW_IMS_DTMF_STOP";
        }
        if (o == 235) {
            return "RIL_REQUEST_HW_IMS_EXPLICIT_CALL_TRANSFER";
        }
        if (o == 236) {
            return "RIL_REQUEST_HW_IMS_REG_STATE_CHANGE";
        }
        if (o == 237) {
            return "RIL_REQUEST_HW_IMS_IMSVOPS_IND";
        }
        if (o == 238) {
            return "RIL_REQUEST_HW_IMS_SET_MUTE";
        }
        if (o == 239) {
            return "RIL_REQUEST_HW_ANT_SWITCH";
        }
        if (o == 240) {
            return "RIL_REQUEST_HW_GET_ANT_SWITCH";
        }
        if (o == 241) {
            return "RIL_REQUEST_GET_POL_CAPABILITY";
        }
        if (o == 242) {
            return "RIL_REQUEST_GET_POL_LIST";
        }
        if (o == 243) {
            return "RIL_REQUEST_SET_POL_ENTRY";
        }
        if (o == 244) {
            return "RIL_REQUEST_HW_GET_ISMCOEX";
        }
        if (o == 245) {
            return "RIL_REQUEST_HW_SET_ISMCOEX";
        }
        if (o == 246) {
            return "RIL_REQUEST_HW_IMS_IMPU";
        }
        if (o == 247) {
            return "RIL_REQUEST_HW_SET_BAND_CFG";
        }
        if (o == 248) {
            return "RIL_REQUEST_HW_GET_BAND_CFG";
        }
        if (o == 249) {
            return "RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO";
        }
        if (o == 250) {
            return "RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO";
        }
        if (o == 251) {
            return "RIL_REQUEST_HW_GET_SIMLOCK_STATUS";
        }
        if (o == 252) {
            return "RIL_REQUEST_HW_GET_ICCID";
        }
        if (o == 253) {
            return "RIL_REQUEST_HW_IMS_ADD_CONFERENCE_MEMBER";
        }
        if (o == 254) {
            return "RIL_REQUEST_HW_IMS_GET_CLIR";
        }
        if (o == 255) {
            return "RIL_REQUEST_HW_IMS_SET_CLIR";
        }
        if (o == 256) {
            return "RIL_REQUEST_HW_IMS_SET_CALL_WAITING";
        }
        if (o == 257) {
            return "RIL_REQUEST_HW_QUERY_GSM_NMR_INFO";
        }
        if (o == 258) {
            return "RIL_REQUEST_HW_UICC_AUTH";
        }
        if (o == 259) {
            return "RIL_REQUEST_HW_UICC_GBA_BOOTSTRAP";
        }
        if (o == 260) {
            return "RIL_REQUEST_HW_UPDATE_UICC_FILE";
        }
        if (o == 261) {
            return "RIL_REQUEST_HW_GET_UICC_FILE";
        }
        if (o == 262) {
            return "RIL_REQUEST_HW_UICC_KS_NAF_AUTH";
        }
        if (o == 263) {
            return "RIL_REQUEST_HW_INITIAL_MESSAGE";
        }
        if (o == 264) {
            return "RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX";
        }
        if (o == 265) {
            return "RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE";
        }
        if (o == 266) {
            return "RIL_REQUEST_HW_GET_DS_FLOW_INFO";
        }
        if (o == 267) {
            return "RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO";
        }
        if (o == 268) {
            return "RIL_REQUEST_HW_SET_DS_FLOW_REPORT_FLAG";
        }
        if (o == 269) {
            return "RIL_REQUEST_HW_GET_DEVICE_VERSION";
        }
        if (o == 270) {
            return "RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG";
        }
        if (o == 271) {
            return "RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD";
        }
        if (o == 272) {
            return "RIL_REQUEST_HW_GET_SCI_CHG_CFG";
        }
        if (o == 273) {
            return "RIL_REQUEST_HW_DSDS_GET_SIM_STATUS";
        }
        if (o == 274) {
            return "RIL_REQUEST_HW_IMS_MODIFY_CALL_INITIATE";
        }
        if (o == 275) {
            return "RIL_REQUEST_HW_IMS_MODIFY_CALL_CONFIRM";
        }
        if (o == 276) {
            return "RIL_REQUEST_HW_SET_VOICEPREFER_STATUS";
        }
        if (o == 277) {
            return "RIL_REQUEST_HW_GET_VOICEPREFER_STATUS";
        }
        if (o == 278) {
            return "RIL_REQUEST_HW_IMS_ECONF_DIAL";
        }
        if (o == 279) {
            return "RIL_REQUEST_HW_IMS_GET_ECONF_INFO";
        }
        if (o == 280) {
            return "RIL_REQUEST_HW_IMS_ECONF_REMOVE_MEMBER";
        }
        if (o == 281) {
            return "RIL_REQUEST_HW_IMS_MERGE_ECONF";
        }
        if (o == 282) {
            return "RIL_REQUEST_HW_IMS_ECONF_ADD_MEMBER";
        }
        if (o == 283) {
            return "RIL_REQUEST_HW_IMS_GET_ECONF_LAST_FAIL_CAUSE";
        }
        if (o == 284) {
            return "RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH";
        }
        if (o == 285) {
            return "RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION";
        }
        if (o == 286) {
            return "RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION";
        }
        if (o == 287) {
            return "RIL_REQUEST_HW_AP_SET_DS_FLOW_CONFIG";
        }
        if (o == 288) {
            return "RIL_REQUEST_HW_VSIM_CHECK_CARD";
        }
        if (o == 289) {
            return "RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA";
        }
        if (o == 290) {
            return "RIL_REQUEST_GET_DS_FLOW_NV_WRITE_CFG_PARA";
        }
        if (o == 291) {
            return "RIL_REQUEST_HW_SET_IMS_SWITCH";
        }
        if (o == 292) {
            return "RIL_REQUEST_HW_GET_IMS_SWITCH";
        }
        if (o == 293) {
            return "RIL_REQUEST_HW_ICC_PREF_APP_SWITCH";
        }
        if (o == 294) {
            return "RIL_REQUEST_HW_GET_CIMSI";
        }
        if (o == 295) {
            return "RIL_REQUEST_HW_SET_CDMA_MODE_SIDE";
        }
        if (o == 296) {
            return "RIL_REQUEST_HW_SET_UE_OPERATION_MODE";
        }
        if (o == 297) {
            return "RIL_REQUEST_HW_VSIM_POWER";
        }
        if (o == 298) {
            return "RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS";
        }
        if (o == 299) {
            return "RIL_REQUEST_HW_GET_CTROAMINFO";
        }
        if (o == 300) {
            return "RIL_REQUEST_HW_SET_CT_OOS_COUNT";
        }
        if (o == 301) {
            return "RIL_REQUEST_HW_IMS_DOMAIN_CONFIG";
        }
        if (o == 302) {
            return "RIL_REQUEST_HW_VOWIFI_IMSA_MSG";
        }
        if (o == 303) {
            return "RIL_REQUEST_HW_GET_IMS_DOMAIN";
        }
        if (o == 304) {
            return "RIL_REQUEST_HW_GET_CDMA_MODE_SIDE";
        }
        if (o == 305) {
            return "RIL_REQUEST_HW_VOWIFI_UICC_AUTH";
        }
        if (o == 306) {
            return "RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND";
        }
        if (o == 307) {
            return "RIL_REQUEST_HW_SET_TIME";
        }
        if (o == 308) {
            return "RIL_REQUEST_VSIM_BASEBAND_VERSION";
        }
        if (o == 309) {
            return "RIL_REQUEST_HW_SET_VT_LTE_QUALITY_RPT_CFG";
        }
        if (o == 310) {
            return "RIL_REQUEST_HW_IMS_REJ_CALL";
        }
        if (o == 311) {
            return "RIL_REQUEST_HW_REJ_CALL";
        }
        if (o == 312) {
            return "RIL_REQUEST_HW_IMS_CHANNEL_INFO";
        }
        if (o == 313) {
            return "RIL_REQUEST_HW_CHANNEL_INFO";
        }
        if (o == 314) {
            return "RIL_REQUEST_HW_GET_RAT_COMBINE_MODE";
        }
        if (o == 315) {
            return "RIL_REQUEST_HW_IMS_SET_DM_PCSCF";
        }
        if (o == 316) {
            return "RIL_REQUEST_HW_IMS_SET_DM_DYN";
        }
        if (o == 317) {
            return "RIL_REQUEST_HW_IMS_SET_DM_TIMER";
        }
        if (o == 318) {
            return "RIL_REQUEST_HW_IMS_SET_DM_SMS";
        }
        if (o == 319) {
            return "RIL_REQUEST_HW_IMS_GET_DM_PCSCF";
        }
        if (o == 320) {
            return "RIL_REQUEST_HW_IMS_GET_DM_TIMER";
        }
        if (o == 321) {
            return "RIL_REQUEST_HW_IMS_GET_DM_DYN";
        }
        if (o == 322) {
            return "RIL_REQUEST_HW_IMS_GET_DM_SMS";
        }
        if (o == 323) {
            return "RIL_REQUEST_HW_IMS_GET_DM_USER";
        }
        if (o == 324) {
            return "RIL_REQUEST_HW_IMS_SET_NICK_NAME";
        }
        if (o == 325) {
            return "RIL_REQUEST_HW_IMS_GET_NICK_NAME";
        }
        if (o == 326) {
            return "RIL_REQUEST_HW_IMS_BATTERY_STATUS";
        }
        if (o == 327) {
            return "RIL_REQUEST_HW_IMS_REGISTER";
        }
        if (o == 328) {
            return "RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY";
        }
        if (o == 329) {
            return "RIL_REQUEST_HW_SET_PSEUDO_INFO";
        }
        if (o == 330) {
            return "RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY";
        }
        if (o == 331) {
            return "RIL_REQUEST_HW_SIGNAL_STRENGTH";
        }
        if (o == 332) {
            return "RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID";
        }
        if (o == 333) {
            return "RIL_REQUEST_HW_SELECT_CSGID";
        }
        if (o == 334) {
            return "RIL_REQUEST_HW_IMS_LAST_CALL_CAUSE";
        }
        if (o == 335) {
            return "RIL_REQUEST_HW_SET_IMSVT_CAPABILITY";
        }
        if (o == 336) {
            return "RIL_REQUEST_HW_SET_WIFI_EMERGENCY_AID";
        }
        if (o == 337) {
            return "RIL_REQUEST_HW_SEND_LAA_CMD";
        }
        if (o == 338) {
            return "RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY";
        }
        if (o == 339) {
            return "RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY";
        }
        if (o == 340) {
            return "RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA";
        }
        if (o == 341) {
            return "RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE";
        }
        if (o == 342) {
            return "RIL_REQUEST_HW_GET_LAA_STATE";
        }
        if (o == 1047) {
            return "RIL_UNSOL_HW_DEVICE_BASE";
        }
        if (o == 1048) {
            return "RIL_UNSOL_HW_RESPONSE_SIM_TYPE";
        }
        if (o == 1049) {
            return "RIL_UNSOL_HW_RESPONSE_IMS_NETWORK_STATE_CHANGED";
        }
        if (o == 1050) {
            return "RIL_UNSOL_HW_RESPONSE_TETHERED_MODE_STATE_CHANGED";
        }
        if (o == 1051) {
            return "RIL_UNSOL_HW_RESPONSE_DATA_NETWORK_STATE_CHANGED";
        }
        if (o == 1052) {
            return "RIL_UNSOL_HW_ON_SS";
        }
        if (o == 1053) {
            return "RIL_UNSOL_HW_STK_CC_ALPHA_NOTIFY";
        }
        if (o == 1054) {
            return "RIL_UNSOL_HW_UICC_SUBSCRIPTION_STATUS_CHANGED";
        }
        if (o == 1055) {
            return "RIL_UNSOL_HW_QOS_STATE_CHANGED_IND";
        }
        if (o == 1056) {
            return "RIL_UNSOL_HW_MODIFY_CALL";
        }
        if (o == 1057) {
            return "RIL_UNSOL_HW_DIALUP_STATE_CHANGED";
        }
        if (o == 1058) {
            return "RIL_UNSOL_HW_CURR_MCC";
        }
        if (o == 1059) {
            return "RIL_UNSOL_HW_CURR_GSM_STATE";
        }
        if (o == 1060) {
            return "RIL_UNSOL_HW_RESPONSE_SIMLOCK_STATUS_CHANGED";
        }
        if (o == 1061) {
            return "RIL_UNSOL_HW_CG_SWITCH_RECOVERY";
        }
        if (o == 1062) {
            return "RIL_UNSOL_HW_TETHERED_MODE_STATE_CHANGED";
        }
        if (o == 1063) {
            return "RIL_UNSOL_HW_TRIGGER_SETUP_DATA_CALL";
        }
        if (o == 1064) {
            return "RIL_UNSOL_HW_SIMSLOT_CFG";
        }
        if (o == 1065) {
            return "RIL_UNSOL_HW_RIL_CHR_IND";
        }
        if (o == 1066) {
            return "RIL_UNSOL_HW_RESET_CHR_IND";
        }
        if (o == 1067) {
            return "RIL_UNSOL_HW_SIM_HOTPLUG";
        }
        if (o == 1068) {
            return "RIL_UNSOL_HW_SIM_ICCID_CHANGED";
        }
        if (o == 1069) {
            return "RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED";
        }
        if (o == 1070) {
            return "RIL_UNSOL_HW_SIM_PNP";
        }
        if (o == 1071) {
            return "RIL_UNSOL_HW_CS_CHANNEL_INFO_IND";
        }
        if (o == 1072) {
            return "RIL_UNSOL_HW_NCELL_MONITOR";
        }
        if (o == 1073) {
            return "RIL_UNSOL_HW_ECCNUM";
        }
        if (o == 1074) {
            return "RIL_UNSOL_HW_NETWORK_REJECT_CASE";
        }
        if (o == 1075) {
            return "RIL_UNSOL_HW_VSIM_RDH_REQUEST";
        }
        if (o == 1076) {
            return "RIL_UNSOL_HW_VSIM_OTA_SMS_REPORT";
        }
        if (o == 1077) {
            return "RIL_UNSOL_HW_SVLTE_PS_TRANSFER_IND";
        }
        if (o == 1078) {
            return "RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND";
        }
        if (o == 1079) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_CALL_STATE_CHANGED";
        }
        if (o == 1080) {
            return "RIL_UNSOL_HW_IMS_CALL_RING";
        }
        if (o == 1081) {
            return "RIL_UNSOL_HW_IMS_RINGBACK_TONE";
        }
        if (o == 1082) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_HANDOVER";
        }
        if (o == 1083) {
            return "RIL_UNSOL_HW_IMS_SRV_STATUS_UPDATE";
        }
        if (o == 1084) {
            return "RIL_UNSOL_HW_IMS_ON_USSD";
        }
        if (o == 1085) {
            return "RIL_UNSOL_HW_IMS_ON_SS";
        }
        if (o == 1086) {
            return "RIL_UNSOL_HW_IMS_SUPP_SVC_NOTIFICATION";
        }
        if (o == 1087) {
            return "RIL_UNSOL_HW_IMS_VOICE_BAND_INFO";
        }
        if (o == 1088) {
            return "RIL_UNSOL_HW_UIM_LOCKCARD";
        }
        if (o == 1089) {
            return "RIL_UNSOL_HW_MIPICLK";
        }
        if (o == 1090) {
            return "RIL_UNSOL_HW_APR_SVLTE_IND";
        }
        if (o == 1091) {
            return "RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT";
        }
        if (o == 1092) {
            return "RIL_UNSOL_HW_PROXY_INIT_STATE_CHANGED";
        }
        if (o == 1093) {
            return "RIL_UNSOL_HW_LTE_FREQ_WITH_WLAN_COEX";
        }
        if (o == 1094) {
            return "RIL_UNSOL_HW_DS_FLOW_INFO_REPORT";
        }
        if (o == 1095) {
            return "RIL_UNSOL_HW_TEE_TASK_TIME_OUT";
        }
        if (o == 1096) {
            return "RIL_UNSOL_HW_DSDS_SIM_STATUS_CHANGED";
        }
        if (o == 1097) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_IND";
        }
        if (o == 1098) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_END_CAUSE";
        }
        if (o == 1099) {
            return "RIL_UNSOL_HW_XPASS_INFO_RPT";
        }
        if (o == 1100) {
            return "RIL_UNSOL_HOOK_HW_VP_STATUS";
        }
        if (o == 1101) {
            return "RIL_UNSOL_HW_IMS_ECONF_STATE_CHANGED";
        }
        if (o == 1102) {
            return "RIL_UNSOL_HW_EXIST_NETWORK_INFO";
        }
        if (o == 1103) {
            return "RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT";
        }
        if (o == 1104) {
            return "RIL_UNSOL_HW_IMS_MT_STATUS_REPORT";
        }
        if (o == 1105) {
            return "RIL_UNSOL_HW_CA_STATE_CHANGED";
        }
        if (o == 1106) {
            return "RIL_UNSOL_HW_IMS_CS_REDIAL_NOTIFY";
        }
        if (o == 1107) {
            return "RIL_UNSOL_HW_CTROAMINFO_CHANGED";
        }
        if (o == 1108) {
            return "RIL_UNSOL_HW_CDMA_HPLMN_UPDATE";
        }
        if (o == 1109) {
            return "RIL_UNSOL_HW_IMSA_VOWIFI_MSG";
        }
        if (o == 1110) {
            return "RIL_UNSOL_HW_IMS_DATA_CONNECT_IND";
        }
        if (o == 1111) {
            return "RIL_UNSOL_HW_IMS_DATA_DISCONNECT_IND";
        }
        if (o == 1112) {
            return "RIL_UNSOL_HW_ROAMING_MODE_SWITCH";
        }
        if (o == 1113) {
            return "RIL_UNSOL_HW_LTE_PDCP_INFO";
        }
        if (o == 1114) {
            return "RIL_UNSOL_HW_LTE_RRC_INFO";
        }
        if (o == 1115) {
            return "RIL_UNSOL_HW_CRR_CONN_IND";
        }
        if (o == 1116) {
            return "RIL_UNSOL_HW_RAT_COMBINE_MODE_IND";
        }
        if (o == 1117) {
            return "RIL_UNSOL_HW_SIM_SWITCH";
        }
        if (o == 1118) {
            return "RIL_UNSOL_HW_INIT_LOCINFO";
        }
        if (o == 1119) {
            return "RIL_UNSOL_HW_LIMIT_PDP_ACT_IND";
        }
        if (o == 1120) {
            return "RIL_UNSOL_HW_IMS_DMCN";
        }
        if (o == 1121) {
            return "RIL_UNSOL_HW_IMS_REG_FAILED_INFO";
        }
        if (o == 1122) {
            return "RIL_UNSOL_HW_IMS_HOLD_TONE_IND";
        }
        if (o == 1123) {
            return "RIL_UNSOL_HW_MCC_CHANGE";
        }
        if (o == 1124) {
            return "RIL_UNSOL_RSRVCC_STATE_NOTIFY";
        }
        if (o == 1125) {
            return "RIL_UNSOL_HW_SIGNAL_STRENGTH";
        }
        if (o == 1126) {
            return "RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH";
        }
        if (o == 1127) {
            return "RIL_UNSOL_HW_VT_FLOW_INFO_REPORT";
        }
        if (o == 1128) {
            return "RIL_UNSOL_HW_LAA_STATE";
        }
        if (o == 1129) {
            return "RIL_UNSOL_HW_CALL_ALT_SRV";
        }
        if (o == 343) {
            return "RIL_REQUEST_HW_SET_RCS_SWITCH";
        }
        if (o == 344) {
            return "RIL_REQUEST_HW_GET_RCS_SWITCH_STATE";
        }
        if (o == 345) {
            return "RIL_REQUEST_HW_SET_DM_RCS_CFG";
        }
        if (o == 346) {
            return "RIL_REQUEST_HW_EXCHANGE_MODEM_INFO";
        }
        if (o == 347) {
            return "RIL_REQUEST_HW_INFORM_MODEM_TETHER_STATUS";
        }
        if (o == 348) {
            return "RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_WITH_IMS_DOMAIN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 4) == 4) {
            list.add("MAX_INTS_NUM");
            flipped = 0 | 4;
        }
        if ((o & 4) == 4) {
            list.add("MAX_STRINGS_NUM");
            flipped |= 4;
        }
        if ((o & 140) == 140) {
            list.add("RIL_REQUEST_HW_DEVICE_BASE");
            flipped |= 140;
        }
        if ((o & 141) == 141) {
            list.add("RIL_REQUEST_HW_IMS_REGISTRATION_STATE");
            flipped |= 141;
        }
        if ((o & 142) == 142) {
            list.add("RIL_REQUEST_HW_IMS_SEND_SMS");
            flipped |= 142;
        }
        if ((o & 143) == 143) {
            list.add("RIL_REQUEST_HW_GET_DATA_CALL_PROFILE");
            flipped |= 143;
        }
        if ((o & 144) == 144) {
            list.add("RIL_REQUEST_HW_SET_UICC_SUBSCRIPTION");
            flipped |= 144;
        }
        if ((o & 145) == 145) {
            list.add("RIL_REQUEST_HW_SET_DATA_SUBSCRIPTION");
            flipped |= 145;
        }
        if ((o & 146) == 146) {
            list.add("RIL_REQUEST_HW_GET_UICC_SUBSCRIPTION");
            flipped |= 146;
        }
        if ((o & 147) == 147) {
            list.add("RIL_REQUEST_HW_GET_DATA_SUBSCRIPTION");
            flipped |= 147;
        }
        if ((o & 148) == 148) {
            list.add("RIL_REQUEST_HW_SET_SUBSCRIPTION_MODE");
            flipped |= 148;
        }
        if ((o & 149) == 149) {
            list.add("RIL_REQUEST_HW_SETUP_QOS");
            flipped |= 149;
        }
        if ((o & 150) == 150) {
            list.add("RIL_REQUEST_HW_RELEASE_QOS");
            flipped |= 150;
        }
        if ((o & 151) == 151) {
            list.add("RIL_REQUEST_HW_GET_QOS_STATUS");
            flipped |= 151;
        }
        if ((o & 152) == 152) {
            list.add("RIL_REQUEST_HW_MODIFY_QOS");
            flipped |= 152;
        }
        if ((o & 153) == 153) {
            list.add("RIL_REQUEST_HW_SUSPEND_QOS");
            flipped |= 153;
        }
        if ((o & 154) == 154) {
            list.add("RIL_REQUEST_HW_RESUME_QOS");
            flipped |= 154;
        }
        if ((o & 155) == 155) {
            list.add("RIL_REQUEST_HW_MODIFY_CALL_INITIATE");
            flipped |= 155;
        }
        if ((o & 156) == 156) {
            list.add("RIL_REQUEST_HW_MODIFY_CALL_CONFIRM");
            flipped |= 156;
        }
        if ((o & 157) == 157) {
            list.add("RIL_REQUEST_HW_SWITCH_MTKSIM");
            flipped |= 157;
        }
        if ((o & 158) == 158) {
            list.add("RIL_REQUEST_HW_SET_POWER_GRADE");
            flipped |= 158;
        }
        if ((o & 159) == 159) {
            list.add("RIL_REQUEST_HW_SENDAPDU");
            flipped |= 159;
        }
        if ((o & 160) == 160) {
            list.add("RIL_REQUEST_HW_SET_AUDIO_CHANNEL");
            flipped |= 160;
        }
        if ((o & 161) == 161) {
            list.add("RIL_REQUEST_HW_SET_PCM");
            flipped |= 161;
        }
        if ((o & 162) == 162) {
            list.add("RIL_REQUEST_HW_QUERY_EMERGENCY_NUMBERS");
            flipped |= 162;
        }
        if ((o & 163) == 163) {
            list.add("RIL_REQUEST_HW_MODEMSTATUS_REPORT");
            flipped |= 163;
        }
        if ((o & 164) == 164) {
            list.add("RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ");
            flipped |= 164;
        }
        if ((o & 165) == 165) {
            list.add("RIL_REQUEST_HW_STK_CONFIRM_REFRESH");
            flipped |= 165;
        }
        if ((o & 166) == 166) {
            list.add("RIL_REQUEST_HW_STK_GET_LOCAL_INFORMATION");
            flipped |= 166;
        }
        if ((o & 167) == 167) {
            list.add("RIL_REQUEST_HW_DEVICE_RESERVED_27");
            flipped |= 167;
        }
        if ((o & 168) == 168) {
            list.add("RIL_REQUEST_HW_QUERY_CARDTYPE");
            flipped |= 168;
        }
        if ((o & 169) == 169) {
            list.add("RIL_REQUEST_HW_GET_CDMA_GSM_IMSI");
            flipped |= 169;
        }
        if ((o & 170) == 170) {
            list.add("RIL_REQUEST_HW_SET_SIMSLOT_TO_VIA");
            flipped |= 170;
        }
        if ((o & 171) == 171) {
            list.add("RIL_REQUEST_HW_VOICE_LOOPBACK");
            flipped |= 171;
        }
        if ((o & 172) == 172) {
            list.add("RIL_REQUEST_HW_GET_CDMA_CHR_INFO");
            flipped |= 172;
        }
        if ((o & 173) == 173) {
            list.add("RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE");
            flipped |= 173;
        }
        if ((o & 174) == 174) {
            list.add("RIL_REQUEST_HW_GET_LOCATION_INFO");
            flipped |= 174;
        }
        if ((o & 175) == 175) {
            list.add("RIL_REQUEST_HW_SET_WIFI_POWER_GRADE");
            flipped |= 175;
        }
        if ((o & 176) == 176) {
            list.add("RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2");
            flipped |= 176;
        }
        if ((o & 177) == 177) {
            list.add("RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE");
            flipped |= 177;
        }
        if ((o & 178) == 178) {
            list.add("RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS");
            flipped |= 178;
        }
        if ((o & 179) == 179) {
            list.add("RIL_REQUEST_HW_MODEM_POWER");
            flipped |= 179;
        }
        if ((o & 180) == 180) {
            list.add("RIL_REQUEST_HW_SET_SIM_LESS");
            flipped |= 180;
        }
        if ((o & 181) == 181) {
            list.add("RIL_REQUEST_HW_SET_TRANSMIT_POWER");
            flipped |= 181;
        }
        if ((o & 182) == 182) {
            list.add("RIL_REQUEST_HW_RESTRAT_RILD");
            flipped |= 182;
        }
        if ((o & 183) == 183) {
            list.add("RIL_REQUEST_HW_SIM_TRANSMIT_BASIC");
            flipped |= 183;
        }
        if ((o & 184) == 184) {
            list.add("RIL_REQUEST_HW_SIM_OPEN_CHANNEL");
            flipped |= 184;
        }
        if ((o & 185) == 185) {
            list.add("RIL_REQUEST_HW_SIM_CLOSE_CHANNEL");
            flipped |= 185;
        }
        if ((o & 186) == 186) {
            list.add("RIL_REQUEST_HW_SIM_TRANSMIT_CHANNEL");
            flipped |= 186;
        }
        if ((o & 187) == 187) {
            list.add("RIL_REQUEST_HW_MODIFY_DATA_PROFILE");
            flipped |= 187;
        }
        if ((o & 188) == 188) {
            list.add("RIL_REQUEST_HW_DATA_CONNECTION_DETACH");
            flipped |= 188;
        }
        if ((o & 189) == 189) {
            list.add("RIL_REQUEST_HW_DATA_CONNECTION_ATTACH");
            flipped |= 189;
        }
        if ((o & 190) == 190) {
            list.add("RIL_REQUEST_HW_GET_DATA_PROFILE");
            flipped |= 190;
        }
        if ((o & 191) == 191) {
            list.add("RIL_REQUEST_HW_GET_SIM_CAPACITY");
            flipped |= 191;
        }
        if ((o & 192) == 192) {
            list.add("RIL_REQUEST_HW_SET_LONG_MESSAGE");
            flipped |= 192;
        }
        if ((o & 193) == 193) {
            list.add("RIL_REQUEST_HW_CGSMS_MESSAGE");
            flipped |= 193;
        }
        if ((o & 194) == 194) {
            list.add("RIL_REQUEST_HW_RESET_ALL_CONNECTIONS");
            flipped |= 194;
        }
        if ((o & 195) == 195) {
            list.add("RIL_REQUEST_HW_RRC_CONTROL");
            flipped |= 195;
        }
        if ((o & 196) == 196) {
            list.add("RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE");
            flipped |= 196;
        }
        if ((o & 197) == 197) {
            list.add("RIL_REQUEST_HW_GET_VOICECALL_BACKGROUND_STATE");
            flipped |= 197;
        }
        if ((o & 198) == 198) {
            list.add("RIL_REQUEST_HW_GET_IMEI_VERIFY_STATUS");
            flipped |= 198;
        }
        if ((o & 199) == 199) {
            list.add("RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG");
            flipped |= 199;
        }
        if ((o & 200) == 200) {
            list.add("RIL_REQUEST_HW_SET_PSDOMAIN_AUTOATTACH_TYPE");
            flipped |= 200;
        }
        if ((o & 201) == 201) {
            list.add("RIL_REQUEST_HW_GET_PSDOMAIN_AUTOATTACH_TYPE");
            flipped |= 201;
        }
        if ((o & 202) == 202) {
            list.add("RIL_REQUEST_HW_SET_NCELL_MONITOR_STATE");
            flipped |= 202;
        }
        if ((o & 203) == 203) {
            list.add("RIL_REQUEST_HW_GET_USER_SERVICE_STATE");
            flipped |= 203;
        }
        if ((o & 204) == 204) {
            list.add("RIL_REQUEST_HW_MONITOR_SIM_IN_SLOT_IND");
            flipped |= 204;
        }
        if ((o & 205) == 205) {
            list.add("RIL_REQUEST_HW_SET_SIM_SLOT_CFG");
            flipped |= 205;
        }
        if ((o & 206) == 206) {
            list.add("RIL_REQUEST_HW_GET_SIM_SLOT_CFG");
            flipped |= 206;
        }
        if ((o & 207) == 207) {
            list.add("RIL_REQUEST_HW_HANDLE_DETECT");
            flipped |= 207;
        }
        if ((o & 208) == 208) {
            list.add("RIL_REQUEST_HW_GET_HANDLE_DETECT");
            flipped |= 208;
        }
        if ((o & 209) == 209) {
            list.add("RIL_REQUEST_HW_SIM_GET_ATR");
            flipped |= 209;
        }
        if ((o & 210) == 210) {
            list.add("RIL_REQUEST_HW_VSIM_GET_ALGROITHM_AND_MODEM_PUBKEY");
            flipped |= 210;
        }
        if ((o & 211) == 211) {
            list.add("RIL_REQUEST_HW_VSIM_SET_SERVER_PUBKEY");
            flipped |= 211;
        }
        if ((o & 212) == 212) {
            list.add("RIL_REQUEST_HW_FILE_WRITE");
            flipped |= 212;
        }
        if ((o & 213) == 213) {
            list.add("RIL_REQUEST_HW_VSIM_GET_SIM_CONTENT");
            flipped |= 213;
        }
        if ((o & 214) == 214) {
            list.add("RIL_REQUEST_HW_VSIM_SET_SIM_STATE");
            flipped |= 214;
        }
        if ((o & 215) == 215) {
            list.add("RIL_REQUEST_HW_VSIM_GET_SIM_STATE");
            flipped |= 215;
        }
        if ((o & 216) == 216) {
            list.add("RIL_REQUEST_HW_GET_EOPLMN_LIST");
            flipped |= 216;
        }
        if ((o & 217) == 217) {
            list.add("RIL_REQUEST_HW_SET_EOPLMN_LIST");
            flipped |= 217;
        }
        if ((o & 218) == 218) {
            list.add("RIL_REQUEST_HW_GET_SYSTEM_INFO_EX");
            flipped |= 218;
        }
        if ((o & 219) == 219) {
            list.add("RIL_REQUEST_HW_GET_PLMN_INFO");
            flipped |= 219;
        }
        if ((o & 220) == 220) {
            list.add("RIL_REQUEST_HW_IMS_DIAL");
            flipped |= 220;
        }
        if ((o & 221) == 221) {
            list.add("RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS");
            flipped |= 221;
        }
        if ((o & 222) == 222) {
            list.add("RIL_REQUEST_HW_IMS_HANGUP");
            flipped |= 222;
        }
        if ((o & 223) == 223) {
            list.add("RIL_REQUEST_HW_IMS_HANGUP_WAITING_OR_BACKGROUND");
            flipped |= 223;
        }
        if ((o & 224) == 224) {
            list.add("RIL_REQUEST_HW_IMS_HANGUP_FOREGROUND_RESUME_BACKGROUND");
            flipped |= 224;
        }
        if ((o & 225) == 225) {
            list.add("RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE");
            flipped |= 225;
        }
        if ((o & 226) == 226) {
            list.add("RIL_REQUEST_HW_IMS_CONFERENCE");
            flipped |= 226;
        }
        if ((o & 227) == 227) {
            list.add("RIL_REQUEST_HW_IMS_UDUB");
            flipped |= 227;
        }
        if ((o & 228) == 228) {
            list.add("RIL_REQUEST_HW_IMS_LAST_CALL_FAIL_CAUSE");
            flipped |= 228;
        }
        if ((o & 229) == 229) {
            list.add("RIL_REQUEST_HW_IMS_SEND_USSD");
            flipped |= 229;
        }
        if ((o & 230) == 230) {
            list.add("RIL_REQUEST_HW_IMS_CANCEL_USSD");
            flipped |= 230;
        }
        if ((o & 231) == 231) {
            list.add("RIL_REQUEST_HW_IMS_ANSWER");
            flipped |= 231;
        }
        if ((o & 232) == 232) {
            list.add("RIL_REQUEST_HW_IMS_DTMF");
            flipped |= 232;
        }
        if ((o & 233) == 233) {
            list.add("RIL_REQUEST_HW_IMS_DTMF_START");
            flipped |= 233;
        }
        if ((o & 234) == 234) {
            list.add("RIL_REQUEST_HW_IMS_DTMF_STOP");
            flipped |= 234;
        }
        if ((o & 235) == 235) {
            list.add("RIL_REQUEST_HW_IMS_EXPLICIT_CALL_TRANSFER");
            flipped |= 235;
        }
        if ((o & 236) == 236) {
            list.add("RIL_REQUEST_HW_IMS_REG_STATE_CHANGE");
            flipped |= 236;
        }
        if ((o & 237) == 237) {
            list.add("RIL_REQUEST_HW_IMS_IMSVOPS_IND");
            flipped |= 237;
        }
        if ((o & 238) == 238) {
            list.add("RIL_REQUEST_HW_IMS_SET_MUTE");
            flipped |= 238;
        }
        if ((o & 239) == 239) {
            list.add("RIL_REQUEST_HW_ANT_SWITCH");
            flipped |= 239;
        }
        if ((o & 240) == 240) {
            list.add("RIL_REQUEST_HW_GET_ANT_SWITCH");
            flipped |= 240;
        }
        if ((o & 241) == 241) {
            list.add("RIL_REQUEST_GET_POL_CAPABILITY");
            flipped |= 241;
        }
        if ((o & 242) == 242) {
            list.add("RIL_REQUEST_GET_POL_LIST");
            flipped |= 242;
        }
        if ((o & 243) == 243) {
            list.add("RIL_REQUEST_SET_POL_ENTRY");
            flipped |= 243;
        }
        if ((o & 244) == 244) {
            list.add("RIL_REQUEST_HW_GET_ISMCOEX");
            flipped |= 244;
        }
        if ((o & 245) == 245) {
            list.add("RIL_REQUEST_HW_SET_ISMCOEX");
            flipped |= 245;
        }
        if ((o & 246) == 246) {
            list.add("RIL_REQUEST_HW_IMS_IMPU");
            flipped |= 246;
        }
        if ((o & 247) == 247) {
            list.add("RIL_REQUEST_HW_SET_BAND_CFG");
            flipped |= 247;
        }
        if ((o & 248) == 248) {
            list.add("RIL_REQUEST_HW_GET_BAND_CFG");
            flipped |= 248;
        }
        if ((o & 249) == 249) {
            list.add("RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO");
            flipped |= 249;
        }
        if ((o & 250) == 250) {
            list.add("RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO");
            flipped |= 250;
        }
        if ((o & 251) == 251) {
            list.add("RIL_REQUEST_HW_GET_SIMLOCK_STATUS");
            flipped |= 251;
        }
        if ((o & 252) == 252) {
            list.add("RIL_REQUEST_HW_GET_ICCID");
            flipped |= 252;
        }
        if ((o & 253) == 253) {
            list.add("RIL_REQUEST_HW_IMS_ADD_CONFERENCE_MEMBER");
            flipped |= 253;
        }
        if ((o & 254) == 254) {
            list.add("RIL_REQUEST_HW_IMS_GET_CLIR");
            flipped |= 254;
        }
        if ((o & 255) == 255) {
            list.add("RIL_REQUEST_HW_IMS_SET_CLIR");
            flipped |= 255;
        }
        if ((o & 256) == 256) {
            list.add("RIL_REQUEST_HW_IMS_SET_CALL_WAITING");
            flipped |= 256;
        }
        if ((o & 257) == 257) {
            list.add("RIL_REQUEST_HW_QUERY_GSM_NMR_INFO");
            flipped |= 257;
        }
        if ((o & 258) == 258) {
            list.add("RIL_REQUEST_HW_UICC_AUTH");
            flipped |= 258;
        }
        if ((o & 259) == 259) {
            list.add("RIL_REQUEST_HW_UICC_GBA_BOOTSTRAP");
            flipped |= 259;
        }
        if ((o & 260) == 260) {
            list.add("RIL_REQUEST_HW_UPDATE_UICC_FILE");
            flipped |= 260;
        }
        if ((o & 261) == 261) {
            list.add("RIL_REQUEST_HW_GET_UICC_FILE");
            flipped |= 261;
        }
        if ((o & 262) == 262) {
            list.add("RIL_REQUEST_HW_UICC_KS_NAF_AUTH");
            flipped |= 262;
        }
        if ((o & 263) == 263) {
            list.add("RIL_REQUEST_HW_INITIAL_MESSAGE");
            flipped |= 263;
        }
        if ((o & 264) == 264) {
            list.add("RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX");
            flipped |= 264;
        }
        if ((o & 265) == 265) {
            list.add("RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE");
            flipped |= 265;
        }
        if ((o & 266) == 266) {
            list.add("RIL_REQUEST_HW_GET_DS_FLOW_INFO");
            flipped |= 266;
        }
        if ((o & 267) == 267) {
            list.add("RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO");
            flipped |= 267;
        }
        if ((o & 268) == 268) {
            list.add("RIL_REQUEST_HW_SET_DS_FLOW_REPORT_FLAG");
            flipped |= 268;
        }
        if ((o & 269) == 269) {
            list.add("RIL_REQUEST_HW_GET_DEVICE_VERSION");
            flipped |= 269;
        }
        if ((o & 270) == 270) {
            list.add("RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG");
            flipped |= 270;
        }
        if ((o & 271) == 271) {
            list.add("RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD");
            flipped |= 271;
        }
        if ((o & 272) == 272) {
            list.add("RIL_REQUEST_HW_GET_SCI_CHG_CFG");
            flipped |= 272;
        }
        if ((o & 273) == 273) {
            list.add("RIL_REQUEST_HW_DSDS_GET_SIM_STATUS");
            flipped |= 273;
        }
        if ((o & 274) == 274) {
            list.add("RIL_REQUEST_HW_IMS_MODIFY_CALL_INITIATE");
            flipped |= 274;
        }
        if ((o & 275) == 275) {
            list.add("RIL_REQUEST_HW_IMS_MODIFY_CALL_CONFIRM");
            flipped |= 275;
        }
        if ((o & 276) == 276) {
            list.add("RIL_REQUEST_HW_SET_VOICEPREFER_STATUS");
            flipped |= 276;
        }
        if ((o & 277) == 277) {
            list.add("RIL_REQUEST_HW_GET_VOICEPREFER_STATUS");
            flipped |= 277;
        }
        if ((o & 278) == 278) {
            list.add("RIL_REQUEST_HW_IMS_ECONF_DIAL");
            flipped |= 278;
        }
        if ((o & 279) == 279) {
            list.add("RIL_REQUEST_HW_IMS_GET_ECONF_INFO");
            flipped |= 279;
        }
        if ((o & 280) == 280) {
            list.add("RIL_REQUEST_HW_IMS_ECONF_REMOVE_MEMBER");
            flipped |= 280;
        }
        if ((o & 281) == 281) {
            list.add("RIL_REQUEST_HW_IMS_MERGE_ECONF");
            flipped |= 281;
        }
        if ((o & 282) == 282) {
            list.add("RIL_REQUEST_HW_IMS_ECONF_ADD_MEMBER");
            flipped |= 282;
        }
        if ((o & 283) == 283) {
            list.add("RIL_REQUEST_HW_IMS_GET_ECONF_LAST_FAIL_CAUSE");
            flipped |= 283;
        }
        if ((o & 284) == 284) {
            list.add("RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH");
            flipped |= 284;
        }
        if ((o & 285) == 285) {
            list.add("RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION");
            flipped |= 285;
        }
        if ((o & 286) == 286) {
            list.add("RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION");
            flipped |= 286;
        }
        if ((o & 287) == 287) {
            list.add("RIL_REQUEST_HW_AP_SET_DS_FLOW_CONFIG");
            flipped |= 287;
        }
        if ((o & 288) == 288) {
            list.add("RIL_REQUEST_HW_VSIM_CHECK_CARD");
            flipped |= 288;
        }
        if ((o & 289) == 289) {
            list.add("RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA");
            flipped |= 289;
        }
        if ((o & 290) == 290) {
            list.add("RIL_REQUEST_GET_DS_FLOW_NV_WRITE_CFG_PARA");
            flipped |= 290;
        }
        if ((o & 291) == 291) {
            list.add("RIL_REQUEST_HW_SET_IMS_SWITCH");
            flipped |= 291;
        }
        if ((o & 292) == 292) {
            list.add("RIL_REQUEST_HW_GET_IMS_SWITCH");
            flipped |= 292;
        }
        if ((o & 293) == 293) {
            list.add("RIL_REQUEST_HW_ICC_PREF_APP_SWITCH");
            flipped |= 293;
        }
        if ((o & 294) == 294) {
            list.add("RIL_REQUEST_HW_GET_CIMSI");
            flipped |= 294;
        }
        if ((o & 295) == 295) {
            list.add("RIL_REQUEST_HW_SET_CDMA_MODE_SIDE");
            flipped |= 295;
        }
        if ((o & 296) == 296) {
            list.add("RIL_REQUEST_HW_SET_UE_OPERATION_MODE");
            flipped |= 296;
        }
        if ((o & 297) == 297) {
            list.add("RIL_REQUEST_HW_VSIM_POWER");
            flipped |= 297;
        }
        if ((o & 298) == 298) {
            list.add("RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS");
            flipped |= 298;
        }
        if ((o & 299) == 299) {
            list.add("RIL_REQUEST_HW_GET_CTROAMINFO");
            flipped |= 299;
        }
        if ((o & 300) == 300) {
            list.add("RIL_REQUEST_HW_SET_CT_OOS_COUNT");
            flipped |= 300;
        }
        if ((o & 301) == 301) {
            list.add("RIL_REQUEST_HW_IMS_DOMAIN_CONFIG");
            flipped |= 301;
        }
        if ((o & 302) == 302) {
            list.add("RIL_REQUEST_HW_VOWIFI_IMSA_MSG");
            flipped |= 302;
        }
        if ((o & 303) == 303) {
            list.add("RIL_REQUEST_HW_GET_IMS_DOMAIN");
            flipped |= 303;
        }
        if ((o & 304) == 304) {
            list.add("RIL_REQUEST_HW_GET_CDMA_MODE_SIDE");
            flipped |= 304;
        }
        if ((o & 305) == 305) {
            list.add("RIL_REQUEST_HW_VOWIFI_UICC_AUTH");
            flipped |= 305;
        }
        if ((o & 306) == 306) {
            list.add("RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND");
            flipped |= 306;
        }
        if ((o & 307) == 307) {
            list.add("RIL_REQUEST_HW_SET_TIME");
            flipped |= 307;
        }
        if ((o & 308) == 308) {
            list.add("RIL_REQUEST_VSIM_BASEBAND_VERSION");
            flipped |= 308;
        }
        if ((o & 309) == 309) {
            list.add("RIL_REQUEST_HW_SET_VT_LTE_QUALITY_RPT_CFG");
            flipped |= 309;
        }
        if ((o & 310) == 310) {
            list.add("RIL_REQUEST_HW_IMS_REJ_CALL");
            flipped |= 310;
        }
        if ((o & 311) == 311) {
            list.add("RIL_REQUEST_HW_REJ_CALL");
            flipped |= 311;
        }
        if ((o & 312) == 312) {
            list.add("RIL_REQUEST_HW_IMS_CHANNEL_INFO");
            flipped |= 312;
        }
        if ((o & 313) == 313) {
            list.add("RIL_REQUEST_HW_CHANNEL_INFO");
            flipped |= 313;
        }
        if ((o & 314) == 314) {
            list.add("RIL_REQUEST_HW_GET_RAT_COMBINE_MODE");
            flipped |= 314;
        }
        if ((o & 315) == 315) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_PCSCF");
            flipped |= 315;
        }
        if ((o & 316) == 316) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_DYN");
            flipped |= 316;
        }
        if ((o & 317) == 317) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_TIMER");
            flipped |= 317;
        }
        if ((o & 318) == 318) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_SMS");
            flipped |= 318;
        }
        if ((o & 319) == 319) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_PCSCF");
            flipped |= 319;
        }
        if ((o & 320) == 320) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_TIMER");
            flipped |= 320;
        }
        if ((o & 321) == 321) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_DYN");
            flipped |= 321;
        }
        if ((o & 322) == 322) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_SMS");
            flipped |= 322;
        }
        if ((o & 323) == 323) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_USER");
            flipped |= 323;
        }
        if ((o & 324) == 324) {
            list.add("RIL_REQUEST_HW_IMS_SET_NICK_NAME");
            flipped |= 324;
        }
        if ((o & 325) == 325) {
            list.add("RIL_REQUEST_HW_IMS_GET_NICK_NAME");
            flipped |= 325;
        }
        if ((o & 326) == 326) {
            list.add("RIL_REQUEST_HW_IMS_BATTERY_STATUS");
            flipped |= 326;
        }
        if ((o & 327) == 327) {
            list.add("RIL_REQUEST_HW_IMS_REGISTER");
            flipped |= 327;
        }
        if ((o & 328) == 328) {
            list.add("RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY");
            flipped |= 328;
        }
        if ((o & 329) == 329) {
            list.add("RIL_REQUEST_HW_SET_PSEUDO_INFO");
            flipped |= 329;
        }
        if ((o & 330) == 330) {
            list.add("RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY");
            flipped |= 330;
        }
        if ((o & 331) == 331) {
            list.add("RIL_REQUEST_HW_SIGNAL_STRENGTH");
            flipped |= 331;
        }
        if ((o & 332) == 332) {
            list.add("RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID");
            flipped |= 332;
        }
        if ((o & 333) == 333) {
            list.add("RIL_REQUEST_HW_SELECT_CSGID");
            flipped |= 333;
        }
        if ((o & 334) == 334) {
            list.add("RIL_REQUEST_HW_IMS_LAST_CALL_CAUSE");
            flipped |= 334;
        }
        if ((o & 335) == 335) {
            list.add("RIL_REQUEST_HW_SET_IMSVT_CAPABILITY");
            flipped |= 335;
        }
        if ((o & 336) == 336) {
            list.add("RIL_REQUEST_HW_SET_WIFI_EMERGENCY_AID");
            flipped |= 336;
        }
        if ((o & 337) == 337) {
            list.add("RIL_REQUEST_HW_SEND_LAA_CMD");
            flipped |= 337;
        }
        if ((o & 338) == 338) {
            list.add("RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY");
            flipped |= 338;
        }
        if ((o & 339) == 339) {
            list.add("RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY");
            flipped |= 339;
        }
        if ((o & 340) == 340) {
            list.add("RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA");
            flipped |= 340;
        }
        if ((o & 341) == 341) {
            list.add("RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE");
            flipped |= 341;
        }
        if ((o & 342) == 342) {
            list.add("RIL_REQUEST_HW_GET_LAA_STATE");
            flipped |= 342;
        }
        if ((o & 1047) == 1047) {
            list.add("RIL_UNSOL_HW_DEVICE_BASE");
            flipped |= 1047;
        }
        if ((o & 1048) == 1048) {
            list.add("RIL_UNSOL_HW_RESPONSE_SIM_TYPE");
            flipped |= 1048;
        }
        if ((o & 1049) == 1049) {
            list.add("RIL_UNSOL_HW_RESPONSE_IMS_NETWORK_STATE_CHANGED");
            flipped |= 1049;
        }
        if ((o & 1050) == 1050) {
            list.add("RIL_UNSOL_HW_RESPONSE_TETHERED_MODE_STATE_CHANGED");
            flipped |= 1050;
        }
        if ((o & 1051) == 1051) {
            list.add("RIL_UNSOL_HW_RESPONSE_DATA_NETWORK_STATE_CHANGED");
            flipped |= 1051;
        }
        if ((o & 1052) == 1052) {
            list.add("RIL_UNSOL_HW_ON_SS");
            flipped |= 1052;
        }
        if ((o & 1053) == 1053) {
            list.add("RIL_UNSOL_HW_STK_CC_ALPHA_NOTIFY");
            flipped |= 1053;
        }
        if ((o & 1054) == 1054) {
            list.add("RIL_UNSOL_HW_UICC_SUBSCRIPTION_STATUS_CHANGED");
            flipped |= 1054;
        }
        if ((o & 1055) == 1055) {
            list.add("RIL_UNSOL_HW_QOS_STATE_CHANGED_IND");
            flipped |= 1055;
        }
        if ((o & 1056) == 1056) {
            list.add("RIL_UNSOL_HW_MODIFY_CALL");
            flipped |= 1056;
        }
        if ((o & 1057) == 1057) {
            list.add("RIL_UNSOL_HW_DIALUP_STATE_CHANGED");
            flipped |= 1057;
        }
        if ((o & 1058) == 1058) {
            list.add("RIL_UNSOL_HW_CURR_MCC");
            flipped |= 1058;
        }
        if ((o & 1059) == 1059) {
            list.add("RIL_UNSOL_HW_CURR_GSM_STATE");
            flipped |= 1059;
        }
        if ((o & 1060) == 1060) {
            list.add("RIL_UNSOL_HW_RESPONSE_SIMLOCK_STATUS_CHANGED");
            flipped |= 1060;
        }
        if ((o & 1061) == 1061) {
            list.add("RIL_UNSOL_HW_CG_SWITCH_RECOVERY");
            flipped |= 1061;
        }
        if ((o & 1062) == 1062) {
            list.add("RIL_UNSOL_HW_TETHERED_MODE_STATE_CHANGED");
            flipped |= 1062;
        }
        if ((o & 1063) == 1063) {
            list.add("RIL_UNSOL_HW_TRIGGER_SETUP_DATA_CALL");
            flipped |= 1063;
        }
        if ((o & 1064) == 1064) {
            list.add("RIL_UNSOL_HW_SIMSLOT_CFG");
            flipped |= 1064;
        }
        if ((o & 1065) == 1065) {
            list.add("RIL_UNSOL_HW_RIL_CHR_IND");
            flipped |= 1065;
        }
        if ((o & 1066) == 1066) {
            list.add("RIL_UNSOL_HW_RESET_CHR_IND");
            flipped |= 1066;
        }
        if ((o & 1067) == 1067) {
            list.add("RIL_UNSOL_HW_SIM_HOTPLUG");
            flipped |= 1067;
        }
        if ((o & 1068) == 1068) {
            list.add("RIL_UNSOL_HW_SIM_ICCID_CHANGED");
            flipped |= 1068;
        }
        if ((o & 1069) == 1069) {
            list.add("RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED");
            flipped |= 1069;
        }
        if ((o & 1070) == 1070) {
            list.add("RIL_UNSOL_HW_SIM_PNP");
            flipped |= 1070;
        }
        if ((o & 1071) == 1071) {
            list.add("RIL_UNSOL_HW_CS_CHANNEL_INFO_IND");
            flipped |= 1071;
        }
        if ((o & 1072) == 1072) {
            list.add("RIL_UNSOL_HW_NCELL_MONITOR");
            flipped |= 1072;
        }
        if ((o & 1073) == 1073) {
            list.add("RIL_UNSOL_HW_ECCNUM");
            flipped |= 1073;
        }
        if ((o & 1074) == 1074) {
            list.add("RIL_UNSOL_HW_NETWORK_REJECT_CASE");
            flipped |= 1074;
        }
        if ((o & 1075) == 1075) {
            list.add("RIL_UNSOL_HW_VSIM_RDH_REQUEST");
            flipped |= 1075;
        }
        if ((o & 1076) == 1076) {
            list.add("RIL_UNSOL_HW_VSIM_OTA_SMS_REPORT");
            flipped |= 1076;
        }
        if ((o & 1077) == 1077) {
            list.add("RIL_UNSOL_HW_SVLTE_PS_TRANSFER_IND");
            flipped |= 1077;
        }
        if ((o & 1078) == 1078) {
            list.add("RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND");
            flipped |= 1078;
        }
        if ((o & 1079) == 1079) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_CALL_STATE_CHANGED");
            flipped |= 1079;
        }
        if ((o & 1080) == 1080) {
            list.add("RIL_UNSOL_HW_IMS_CALL_RING");
            flipped |= 1080;
        }
        if ((o & 1081) == 1081) {
            list.add("RIL_UNSOL_HW_IMS_RINGBACK_TONE");
            flipped |= 1081;
        }
        if ((o & 1082) == 1082) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_HANDOVER");
            flipped |= 1082;
        }
        if ((o & 1083) == 1083) {
            list.add("RIL_UNSOL_HW_IMS_SRV_STATUS_UPDATE");
            flipped |= 1083;
        }
        if ((o & 1084) == 1084) {
            list.add("RIL_UNSOL_HW_IMS_ON_USSD");
            flipped |= 1084;
        }
        if ((o & 1085) == 1085) {
            list.add("RIL_UNSOL_HW_IMS_ON_SS");
            flipped |= 1085;
        }
        if ((o & 1086) == 1086) {
            list.add("RIL_UNSOL_HW_IMS_SUPP_SVC_NOTIFICATION");
            flipped |= 1086;
        }
        if ((o & 1087) == 1087) {
            list.add("RIL_UNSOL_HW_IMS_VOICE_BAND_INFO");
            flipped |= 1087;
        }
        if ((o & 1088) == 1088) {
            list.add("RIL_UNSOL_HW_UIM_LOCKCARD");
            flipped |= 1088;
        }
        if ((o & 1089) == 1089) {
            list.add("RIL_UNSOL_HW_MIPICLK");
            flipped |= 1089;
        }
        if ((o & 1090) == 1090) {
            list.add("RIL_UNSOL_HW_APR_SVLTE_IND");
            flipped |= 1090;
        }
        if ((o & 1091) == 1091) {
            list.add("RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT");
            flipped |= 1091;
        }
        if ((o & 1092) == 1092) {
            list.add("RIL_UNSOL_HW_PROXY_INIT_STATE_CHANGED");
            flipped |= 1092;
        }
        if ((o & 1093) == 1093) {
            list.add("RIL_UNSOL_HW_LTE_FREQ_WITH_WLAN_COEX");
            flipped |= 1093;
        }
        if ((o & 1094) == 1094) {
            list.add("RIL_UNSOL_HW_DS_FLOW_INFO_REPORT");
            flipped |= 1094;
        }
        if ((o & 1095) == 1095) {
            list.add("RIL_UNSOL_HW_TEE_TASK_TIME_OUT");
            flipped |= 1095;
        }
        if ((o & 1096) == 1096) {
            list.add("RIL_UNSOL_HW_DSDS_SIM_STATUS_CHANGED");
            flipped |= 1096;
        }
        if ((o & 1097) == 1097) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_IND");
            flipped |= 1097;
        }
        if ((o & 1098) == 1098) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_END_CAUSE");
            flipped |= 1098;
        }
        if ((o & 1099) == 1099) {
            list.add("RIL_UNSOL_HW_XPASS_INFO_RPT");
            flipped |= 1099;
        }
        if ((o & 1100) == 1100) {
            list.add("RIL_UNSOL_HOOK_HW_VP_STATUS");
            flipped |= 1100;
        }
        if ((o & 1101) == 1101) {
            list.add("RIL_UNSOL_HW_IMS_ECONF_STATE_CHANGED");
            flipped |= 1101;
        }
        if ((o & 1102) == 1102) {
            list.add("RIL_UNSOL_HW_EXIST_NETWORK_INFO");
            flipped |= 1102;
        }
        if ((o & 1103) == 1103) {
            list.add("RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT");
            flipped |= 1103;
        }
        if ((o & 1104) == 1104) {
            list.add("RIL_UNSOL_HW_IMS_MT_STATUS_REPORT");
            flipped |= 1104;
        }
        if ((o & 1105) == 1105) {
            list.add("RIL_UNSOL_HW_CA_STATE_CHANGED");
            flipped |= 1105;
        }
        if ((o & 1106) == 1106) {
            list.add("RIL_UNSOL_HW_IMS_CS_REDIAL_NOTIFY");
            flipped |= 1106;
        }
        if ((o & 1107) == 1107) {
            list.add("RIL_UNSOL_HW_CTROAMINFO_CHANGED");
            flipped |= 1107;
        }
        if ((o & 1108) == 1108) {
            list.add("RIL_UNSOL_HW_CDMA_HPLMN_UPDATE");
            flipped |= 1108;
        }
        if ((o & 1109) == 1109) {
            list.add("RIL_UNSOL_HW_IMSA_VOWIFI_MSG");
            flipped |= 1109;
        }
        if ((o & 1110) == 1110) {
            list.add("RIL_UNSOL_HW_IMS_DATA_CONNECT_IND");
            flipped |= 1110;
        }
        if ((o & 1111) == 1111) {
            list.add("RIL_UNSOL_HW_IMS_DATA_DISCONNECT_IND");
            flipped |= 1111;
        }
        if ((o & 1112) == 1112) {
            list.add("RIL_UNSOL_HW_ROAMING_MODE_SWITCH");
            flipped |= 1112;
        }
        if ((o & 1113) == 1113) {
            list.add("RIL_UNSOL_HW_LTE_PDCP_INFO");
            flipped |= 1113;
        }
        if ((o & 1114) == 1114) {
            list.add("RIL_UNSOL_HW_LTE_RRC_INFO");
            flipped |= 1114;
        }
        if ((o & 1115) == 1115) {
            list.add("RIL_UNSOL_HW_CRR_CONN_IND");
            flipped |= 1115;
        }
        if ((o & 1116) == 1116) {
            list.add("RIL_UNSOL_HW_RAT_COMBINE_MODE_IND");
            flipped |= 1116;
        }
        if ((o & 1117) == 1117) {
            list.add("RIL_UNSOL_HW_SIM_SWITCH");
            flipped |= 1117;
        }
        if ((o & 1118) == 1118) {
            list.add("RIL_UNSOL_HW_INIT_LOCINFO");
            flipped |= 1118;
        }
        if ((o & 1119) == 1119) {
            list.add("RIL_UNSOL_HW_LIMIT_PDP_ACT_IND");
            flipped |= 1119;
        }
        if ((o & 1120) == 1120) {
            list.add("RIL_UNSOL_HW_IMS_DMCN");
            flipped |= 1120;
        }
        if ((o & 1121) == 1121) {
            list.add("RIL_UNSOL_HW_IMS_REG_FAILED_INFO");
            flipped |= 1121;
        }
        if ((o & 1122) == 1122) {
            list.add("RIL_UNSOL_HW_IMS_HOLD_TONE_IND");
            flipped |= 1122;
        }
        if ((o & 1123) == 1123) {
            list.add("RIL_UNSOL_HW_MCC_CHANGE");
            flipped |= 1123;
        }
        if ((o & 1124) == 1124) {
            list.add("RIL_UNSOL_RSRVCC_STATE_NOTIFY");
            flipped |= 1124;
        }
        if ((o & 1125) == 1125) {
            list.add("RIL_UNSOL_HW_SIGNAL_STRENGTH");
            flipped |= 1125;
        }
        if ((o & 1126) == 1126) {
            list.add("RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH");
            flipped |= 1126;
        }
        if ((o & 1127) == 1127) {
            list.add("RIL_UNSOL_HW_VT_FLOW_INFO_REPORT");
            flipped |= 1127;
        }
        if ((o & 1128) == 1128) {
            list.add("RIL_UNSOL_HW_LAA_STATE");
            flipped |= 1128;
        }
        if ((o & 1129) == 1129) {
            list.add("RIL_UNSOL_HW_CALL_ALT_SRV");
            flipped |= 1129;
        }
        if ((o & 343) == 343) {
            list.add("RIL_REQUEST_HW_SET_RCS_SWITCH");
            flipped |= 343;
        }
        if ((o & 344) == 344) {
            list.add("RIL_REQUEST_HW_GET_RCS_SWITCH_STATE");
            flipped |= 344;
        }
        if ((o & 345) == 345) {
            list.add("RIL_REQUEST_HW_SET_DM_RCS_CFG");
            flipped |= 345;
        }
        if ((o & 346) == 346) {
            list.add("RIL_REQUEST_HW_EXCHANGE_MODEM_INFO");
            flipped |= 346;
        }
        if ((o & 347) == 347) {
            list.add("RIL_REQUEST_HW_INFORM_MODEM_TETHER_STATUS");
            flipped |= 347;
        }
        if ((o & 348) == 348) {
            list.add("RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_WITH_IMS_DOMAIN");
            flipped |= 348;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

package vendor.huawei.hardware.hisiradio.V1_2;

import java.util.ArrayList;

public final class RilConstS32 {
    public static final int MAX_INTS_NUM = 4;
    public static final int MAX_STRINGS_NUM = 4;
    public static final int RIL_REQUEST_CANCEL_IMS_VIDEO_CALL = 710;
    public static final int RIL_REQUEST_GET_DS_FLOW_NV_WRITE_CFG_PARA = 649;
    public static final int RIL_REQUEST_GET_POL_CAPABILITY = 600;
    public static final int RIL_REQUEST_GET_POL_LIST = 601;
    public static final int RIL_REQUEST_HW_ACTIVE_NCFG_BIN = 718;
    public static final int RIL_REQUEST_HW_ANT_SWITCH = 598;
    public static final int RIL_REQUEST_HW_AP_SET_DS_FLOW_CONFIG = 646;
    public static final int RIL_REQUEST_HW_CGSMS_MESSAGE = 552;
    public static final int RIL_REQUEST_HW_CHANNEL_INFO = 672;
    public static final int RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO = 626;
    public static final int RIL_REQUEST_HW_DATA_CONNECTION_ATTACH = 548;
    public static final int RIL_REQUEST_HW_DATA_CONNECTION_DETACH = 547;
    public static final int RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY = 698;
    public static final int RIL_REQUEST_HW_DEVICE_BASE = 500;
    public static final int RIL_REQUEST_HW_DEVICE_RESERVED_27 = 526;
    public static final int RIL_REQUEST_HW_DSDS_GET_SIM_STATUS = 632;
    public static final int RIL_REQUEST_HW_EXCHANGE_MODEM_INFO = 705;
    public static final int RIL_REQUEST_HW_FILE_WRITE = 571;
    public static final int RIL_REQUEST_HW_GET_ANT_SWITCH = 599;
    public static final int RIL_REQUEST_HW_GET_BAND_CFG = 607;
    public static final int RIL_REQUEST_HW_GET_CAP_OF_REC_PSE_BASESTATION = 731;
    public static final int RIL_REQUEST_HW_GET_CDMA_CHR_INFO = 531;
    public static final int RIL_REQUEST_HW_GET_CDMA_GSM_IMSI = 528;
    public static final int RIL_REQUEST_HW_GET_CDMA_MODE_SIDE = 663;
    public static final int RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA = 699;
    public static final int RIL_REQUEST_HW_GET_CIMSI = 653;
    public static final int RIL_REQUEST_HW_GET_CSGIDSRCH_INFO = 715;
    public static final int RIL_REQUEST_HW_GET_CTROAMINFO = 658;
    public static final int RIL_REQUEST_HW_GET_CURRENT_CALLS_V1_2 = 720;
    public static final int RIL_REQUEST_HW_GET_DATA_CALL_PROFILE = 502;
    public static final int RIL_REQUEST_HW_GET_DATA_PROFILE = 549;
    public static final int RIL_REQUEST_HW_GET_DATA_SUBSCRIPTION = 506;
    public static final int RIL_REQUEST_HW_GET_DEVICE_VERSION = 628;
    public static final int RIL_REQUEST_HW_GET_DS_FLOW_INFO = 625;
    public static final int RIL_REQUEST_HW_GET_EOPLMN_LIST = 575;
    public static final int RIL_REQUEST_HW_GET_HANDLE_DETECT = 567;
    public static final int RIL_REQUEST_HW_GET_ICCID = 611;
    public static final int RIL_REQUEST_HW_GET_IMEI_VERIFY_STATUS = 557;
    public static final int RIL_REQUEST_HW_GET_IMS_DOMAIN = 662;
    public static final int RIL_REQUEST_HW_GET_IMS_SMS_CONFIG = 714;
    public static final int RIL_REQUEST_HW_GET_IMS_SWITCH = 651;
    public static final int RIL_REQUEST_HW_GET_ISMCOEX = 603;
    public static final int RIL_REQUEST_HW_GET_LAA_STATE = 701;
    public static final int RIL_REQUEST_HW_GET_LOCATION_INFO = 533;
    public static final int RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX = 623;
    public static final int RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION = 645;
    public static final int RIL_REQUEST_HW_GET_NUM_OF_REC_PSE_BASE_STATION = 721;
    public static final int RIL_REQUEST_HW_GET_PLMN_INFO = 578;
    public static final int RIL_REQUEST_HW_GET_PSDOMAIN_AUTOATTACH_TYPE = 560;
    public static final int RIL_REQUEST_HW_GET_QOS_STATUS = 510;
    public static final int RIL_REQUEST_HW_GET_RAT_COMBINE_MODE = 673;
    public static final int RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO = 609;
    public static final int RIL_REQUEST_HW_GET_RCS_SWITCH_STATE = 703;
    public static final int RIL_REQUEST_HW_GET_SCI_CHG_CFG = 631;
    public static final int RIL_REQUEST_HW_GET_SIMLOCK_STATUS = 610;
    public static final int RIL_REQUEST_HW_GET_SIM_CAPACITY = 550;
    public static final int RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE = 532;
    public static final int RIL_REQUEST_HW_GET_SIM_SLOT_CFG = 565;
    public static final int RIL_REQUEST_HW_GET_SYSTEM_INFO_EX = 577;
    public static final int RIL_REQUEST_HW_GET_UICC_FILE = 620;
    public static final int RIL_REQUEST_HW_GET_UICC_SUBSCRIPTION = 505;
    public static final int RIL_REQUEST_HW_GET_USER_SERVICE_STATE = 562;
    public static final int RIL_REQUEST_HW_GET_VOICECALL_BACKGROUND_STATE = 556;
    public static final int RIL_REQUEST_HW_GET_VOICEPREFER_STATUS = 636;
    public static final int RIL_REQUEST_HW_HANDLE_DETECT = 566;
    public static final int RIL_REQUEST_HW_ICC_PREF_APP_SWITCH = 652;
    public static final int RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE = 536;
    public static final int RIL_REQUEST_HW_IMS_ADD_CONFERENCE_MEMBER = 612;
    public static final int RIL_REQUEST_HW_IMS_ANSWER = 590;
    public static final int RIL_REQUEST_HW_IMS_ANSWER_V1_3 = 723;
    public static final int RIL_REQUEST_HW_IMS_BATTERY_STATUS = 685;
    public static final int RIL_REQUEST_HW_IMS_CANCEL_USSD = 589;
    public static final int RIL_REQUEST_HW_IMS_CHANNEL_INFO = 671;
    public static final int RIL_REQUEST_HW_IMS_CONFERENCE = 585;
    public static final int RIL_REQUEST_HW_IMS_DIAL = 579;
    public static final int RIL_REQUEST_HW_IMS_DIAL_V1_3 = 724;
    public static final int RIL_REQUEST_HW_IMS_DOMAIN_CONFIG = 660;
    public static final int RIL_REQUEST_HW_IMS_DTMF = 591;
    public static final int RIL_REQUEST_HW_IMS_DTMF_START = 592;
    public static final int RIL_REQUEST_HW_IMS_DTMF_STOP = 593;
    public static final int RIL_REQUEST_HW_IMS_ECONF_ADD_MEMBER = 641;
    public static final int RIL_REQUEST_HW_IMS_ECONF_DIAL = 637;
    public static final int RIL_REQUEST_HW_IMS_ECONF_REMOVE_MEMBER = 639;
    public static final int RIL_REQUEST_HW_IMS_EXPLICIT_CALL_TRANSFER = 594;
    public static final int RIL_REQUEST_HW_IMS_GET_CLIR = 613;
    public static final int RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS = 580;
    public static final int RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_V1_2 = 719;
    public static final int RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_V1_3 = 727;
    public static final int RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_WITH_IMS_DOMAIN = 707;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_DYN = 680;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_PCSCF = 678;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_SMS = 681;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_TIMER = 679;
    public static final int RIL_REQUEST_HW_IMS_GET_DM_USER = 682;
    public static final int RIL_REQUEST_HW_IMS_GET_ECONF_INFO = 638;
    public static final int RIL_REQUEST_HW_IMS_GET_ECONF_LAST_FAIL_CAUSE = 642;
    public static final int RIL_REQUEST_HW_IMS_GET_NICK_NAME = 684;
    public static final int RIL_REQUEST_HW_IMS_HANGUP = 581;
    public static final int RIL_REQUEST_HW_IMS_HANGUP_FOREGROUND_RESUME_BACKGROUND = 583;
    public static final int RIL_REQUEST_HW_IMS_HANGUP_WAITING_OR_BACKGROUND = 582;
    public static final int RIL_REQUEST_HW_IMS_IMPU = 605;
    public static final int RIL_REQUEST_HW_IMS_IMSVOPS_IND = 596;
    public static final int RIL_REQUEST_HW_IMS_LAST_CALL_CAUSE = 693;
    public static final int RIL_REQUEST_HW_IMS_LAST_CALL_FAIL_CAUSE = 587;
    public static final int RIL_REQUEST_HW_IMS_MERGE_ECONF = 640;
    public static final int RIL_REQUEST_HW_IMS_MODIFY_CALL_CONFIRM = 634;
    public static final int RIL_REQUEST_HW_IMS_MODIFY_CALL_INITIATE = 633;
    public static final int RIL_REQUEST_HW_IMS_REGISTER = 686;
    public static final int RIL_REQUEST_HW_IMS_REGISTRATION_STATE = 500;
    public static final int RIL_REQUEST_HW_IMS_REG_STATE_CHANGE = 595;
    public static final int RIL_REQUEST_HW_IMS_REJ_CALL = 669;
    public static final int RIL_REQUEST_HW_IMS_RESTRAT_RILD = 729;
    public static final int RIL_REQUEST_HW_IMS_RTT_MODIFY = 726;
    public static final int RIL_REQUEST_HW_IMS_SEND_SMS = 501;
    public static final int RIL_REQUEST_HW_IMS_SEND_USSD = 588;
    public static final int RIL_REQUEST_HW_IMS_SET_CALL_WAITING = 615;
    public static final int RIL_REQUEST_HW_IMS_SET_CLIR = 614;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_DYN = 675;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_PCSCF = 674;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_SMS = 677;
    public static final int RIL_REQUEST_HW_IMS_SET_DM_TIMER = 676;
    public static final int RIL_REQUEST_HW_IMS_SET_MUTE = 597;
    public static final int RIL_REQUEST_HW_IMS_SET_NICK_NAME = 683;
    public static final int RIL_REQUEST_HW_IMS_SET_RTT_STATUS = 722;
    public static final int RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE = 584;
    public static final int RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE = 700;
    public static final int RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE_V1_3 = 725;
    public static final int RIL_REQUEST_HW_IMS_UDUB = 586;
    public static final int RIL_REQUEST_HW_IMS_UICC_AUTH = 730;
    public static final int RIL_REQUEST_HW_INFORM_MODEM_TETHER_STATUS = 706;
    public static final int RIL_REQUEST_HW_INITIAL_MESSAGE = 622;
    public static final int RIL_REQUEST_HW_MODEMSTATUS_REPORT = 522;
    public static final int RIL_REQUEST_HW_MODEM_POWER = 538;
    public static final int RIL_REQUEST_HW_MODIFY_CALL_CONFIRM = 515;
    public static final int RIL_REQUEST_HW_MODIFY_CALL_INITIATE = 514;
    public static final int RIL_REQUEST_HW_MODIFY_DATA_PROFILE = 546;
    public static final int RIL_REQUEST_HW_MODIFY_QOS = 511;
    public static final int RIL_REQUEST_HW_MONITOR_SIM_IN_SLOT_IND = 563;
    public static final int RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS = 657;
    public static final int RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID = 691;
    public static final int RIL_REQUEST_HW_QUERY_CARDTYPE = 527;
    public static final int RIL_REQUEST_HW_QUERY_EMERGENCY_NUMBERS = 521;
    public static final int RIL_REQUEST_HW_QUERY_GSM_NMR_INFO = 616;
    public static final int RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND = 665;
    public static final int RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH = 643;
    public static final int RIL_REQUEST_HW_REJ_CALL = 670;
    public static final int RIL_REQUEST_HW_RELEASE_QOS = 509;
    public static final int RIL_REQUEST_HW_RESET_ALL_CONNECTIONS = 553;
    public static final int RIL_REQUEST_HW_RESTRAT_RILD = 541;
    public static final int RIL_REQUEST_HW_RESUME_QOS = 513;
    public static final int RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ = 523;
    public static final int RIL_REQUEST_HW_RRC_CONTROL = 554;
    public static final int RIL_REQUEST_HW_SELECT_CSGID = 692;
    public static final int RIL_REQUEST_HW_SENDAPDU = 518;
    public static final int RIL_REQUEST_HW_SEND_LAA_CMD = 696;
    public static final int RIL_REQUEST_HW_SEND_MUTI_CHIP_SESSION_CONFIG = 735;
    public static final int RIL_REQUEST_HW_SEND_NCFG_OPER_INFO = 728;
    public static final int RIL_REQUEST_HW_SEND_VSIM_DATA_TO_MODEM = 736;
    public static final int RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY = 697;
    public static final int RIL_REQUEST_HW_SETUP_QOS = 508;
    public static final int RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE = 624;
    public static final int RIL_REQUEST_HW_SET_AUDIO_CHANNEL = 519;
    public static final int RIL_REQUEST_HW_SET_BAND_CFG = 606;
    public static final int RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY = 687;
    public static final int RIL_REQUEST_HW_SET_CDMA_MODE_SIDE = 654;
    public static final int RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY = 689;
    public static final int RIL_REQUEST_HW_SET_CT_OOS_COUNT = 659;
    public static final int RIL_REQUEST_HW_SET_DATA_ROAM_SWITCH = 712;
    public static final int RIL_REQUEST_HW_SET_DATA_SUBSCRIPTION = 504;
    public static final int RIL_REQUEST_HW_SET_DATA_SWITCH = 711;
    public static final int RIL_REQUEST_HW_SET_DEEP_NO_DISTURB_SWITCH = 717;
    public static final int RIL_REQUEST_HW_SET_DM_RCS_CFG = 704;
    public static final int RIL_REQUEST_HW_SET_DS_FLOW_REPORT_FLAG = 627;
    public static final int RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS = 537;
    public static final int RIL_REQUEST_HW_SET_EOPLMN_LIST = 576;
    public static final int RIL_REQUEST_HW_SET_IMSVT_CAPABILITY = 694;
    public static final int RIL_REQUEST_HW_SET_IMS_SMS_CONFIG = 713;
    public static final int RIL_REQUEST_HW_SET_IMS_SWITCH = 650;
    public static final int RIL_REQUEST_HW_SET_ISMCOEX = 604;
    public static final int RIL_REQUEST_HW_SET_LONG_MESSAGE = 551;
    public static final int RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION = 644;
    public static final int RIL_REQUEST_HW_SET_NCELL_MONITOR_STATE = 561;
    public static final int RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG = 558;
    public static final int RIL_REQUEST_HW_SET_PCM = 520;
    public static final int RIL_REQUEST_HW_SET_POWER_GRADE = 517;
    public static final int RIL_REQUEST_HW_SET_PSDOMAIN_AUTOATTACH_TYPE = 559;
    public static final int RIL_REQUEST_HW_SET_PSEUDO_INFO = 688;
    public static final int RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO = 608;
    public static final int RIL_REQUEST_HW_SET_RCS_SWITCH = 702;
    public static final int RIL_REQUEST_HW_SET_REATTACH_REQUIRED_UNSOL = 716;
    public static final int RIL_REQUEST_HW_SET_SIMSLOT_TO_VIA = 529;
    public static final int RIL_REQUEST_HW_SET_SIM_LESS = 539;
    public static final int RIL_REQUEST_HW_SET_SIM_SLOT_CFG = 564;
    public static final int RIL_REQUEST_HW_SET_SUBSCRIPTION_MODE = 507;
    public static final int RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG = 629;
    public static final int RIL_REQUEST_HW_SET_TIME = 666;
    public static final int RIL_REQUEST_HW_SET_TRANSMIT_POWER = 540;
    public static final int RIL_REQUEST_HW_SET_UE_OPERATION_MODE = 655;
    public static final int RIL_REQUEST_HW_SET_UICC_SUBSCRIPTION = 503;
    public static final int RIL_REQUEST_HW_SET_ULFREQ_ENABLE = 732;
    public static final int RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE = 555;
    public static final int RIL_REQUEST_HW_SET_VOICEPREFER_STATUS = 635;
    public static final int RIL_REQUEST_HW_SET_VT_LTE_QUALITY_RPT_CFG = 668;
    public static final int RIL_REQUEST_HW_SET_WIFI_EMERGENCY_AID = 695;
    public static final int RIL_REQUEST_HW_SET_WIFI_POWER_GRADE = 534;
    public static final int RIL_REQUEST_HW_SIGNAL_STRENGTH = 690;
    public static final int RIL_REQUEST_HW_SIM_CLOSE_CHANNEL = 544;
    public static final int RIL_REQUEST_HW_SIM_GET_ATR = 568;
    public static final int RIL_REQUEST_HW_SIM_OPEN_CHANNEL = 543;
    public static final int RIL_REQUEST_HW_SIM_TRANSMIT_BASIC = 542;
    public static final int RIL_REQUEST_HW_SIM_TRANSMIT_CHANNEL = 545;
    public static final int RIL_REQUEST_HW_STK_CONFIRM_REFRESH = 524;
    public static final int RIL_REQUEST_HW_STK_GET_LOCAL_INFORMATION = 525;
    public static final int RIL_REQUEST_HW_SUSPEND_QOS = 512;
    public static final int RIL_REQUEST_HW_SWITCH_MTKSIM = 516;
    public static final int RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD = 630;
    public static final int RIL_REQUEST_HW_UICC_AUTH = 617;
    public static final int RIL_REQUEST_HW_UICC_GBA_BOOTSTRAP = 618;
    public static final int RIL_REQUEST_HW_UICC_KS_NAF_AUTH = 621;
    public static final int RIL_REQUEST_HW_UPDATE_UICC_FILE = 619;
    public static final int RIL_REQUEST_HW_VOICE_LOOPBACK = 530;
    public static final int RIL_REQUEST_HW_VOLTE_DOMAIN = 709;
    public static final int RIL_REQUEST_HW_VOLTE_IMPI = 708;
    public static final int RIL_REQUEST_HW_VOWIFI_IMSA_MSG = 661;
    public static final int RIL_REQUEST_HW_VOWIFI_UICC_AUTH = 664;
    public static final int RIL_REQUEST_HW_VSIM_CHECK_CARD = 647;
    public static final int RIL_REQUEST_HW_VSIM_GET_ALGROITHM_AND_MODEM_PUBKEY = 569;
    public static final int RIL_REQUEST_HW_VSIM_GET_SIM_CONTENT = 572;
    public static final int RIL_REQUEST_HW_VSIM_GET_SIM_STATE = 574;
    public static final int RIL_REQUEST_HW_VSIM_POWER = 656;
    public static final int RIL_REQUEST_HW_VSIM_SET_SERVER_PUBKEY = 570;
    public static final int RIL_REQUEST_HW_VSIM_SET_SIM_STATE = 573;
    public static final int RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA = 648;
    public static final int RIL_REQUEST_SET_POL_ENTRY = 602;
    public static final int RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2 = 535;
    public static final int RIL_REQUEST_VSIM_BASEBAND_VERSION = 667;
    public static final int RIL_UNSOL_HOOK_HW_VP_STATUS = 2052;
    public static final int RIL_UNSOL_HW_APR_SVLTE_IND = 2042;
    public static final int RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT = 2055;
    public static final int RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT = 2043;
    public static final int RIL_UNSOL_HW_CALL_ALT_SRV = 2081;
    public static final int RIL_UNSOL_HW_CA_STATE_CHANGED = 2057;
    public static final int RIL_UNSOL_HW_CDMA_HPLMN_UPDATE = 2060;
    public static final int RIL_UNSOL_HW_CG_SWITCH_RECOVERY = 2013;
    public static final int RIL_UNSOL_HW_CRR_CONN_IND = 2067;
    public static final int RIL_UNSOL_HW_CS_CHANNEL_INFO_IND = 2023;
    public static final int RIL_UNSOL_HW_CTROAMINFO_CHANGED = 2059;
    public static final int RIL_UNSOL_HW_CURR_GSM_STATE = 2011;
    public static final int RIL_UNSOL_HW_CURR_MCC = 2010;
    public static final int RIL_UNSOL_HW_DEVICE_BASE = 2000;
    public static final int RIL_UNSOL_HW_DIALUP_STATE_CHANGED = 2009;
    public static final int RIL_UNSOL_HW_DL_256QAM_STATE_IND = 2096;
    public static final int RIL_UNSOL_HW_DSDS_MODE_STATE_IND = 2086;
    public static final int RIL_UNSOL_HW_DSDS_SIM_STATUS_CHANGED = 2048;
    public static final int RIL_UNSOL_HW_DS_FLOW_INFO_REPORT = 2046;
    public static final int RIL_UNSOL_HW_ECCNUM = 2025;
    public static final int RIL_UNSOL_HW_EXIST_NETWORK_INFO = 2054;
    public static final int RIL_UNSOL_HW_HPLMN_IND = 2085;
    public static final int RIL_UNSOL_HW_ICCID_CHANGED_IND = 2082;
    public static final int RIL_UNSOL_HW_IMSA_VOWIFI_MSG = 2061;
    public static final int RIL_UNSOL_HW_IMS_CALL_RING = 2032;
    public static final int RIL_UNSOL_HW_IMS_CS_REDIAL_NOTIFY = 2058;
    public static final int RIL_UNSOL_HW_IMS_DATA_CONNECT_IND = 2062;
    public static final int RIL_UNSOL_HW_IMS_DATA_DISCONNECT_IND = 2063;
    public static final int RIL_UNSOL_HW_IMS_DMCN = 2072;
    public static final int RIL_UNSOL_HW_IMS_ECONF_STATE_CHANGED = 2053;
    public static final int RIL_UNSOL_HW_IMS_HOLD_TONE_IND = 2074;
    public static final int RIL_UNSOL_HW_IMS_MT_STATUS_REPORT = 2056;
    public static final int RIL_UNSOL_HW_IMS_ON_SS = 2037;
    public static final int RIL_UNSOL_HW_IMS_ON_USSD = 2036;
    public static final int RIL_UNSOL_HW_IMS_REG_FAILED_INFO = 2073;
    public static final int RIL_UNSOL_HW_IMS_REG_TYPE_IND = 2083;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_CALL_STATE_CHANGED = 2031;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_HANDOVER = 2034;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_END_CAUSE = 2050;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_IND = 2049;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_RTT_ERR = 2089;
    public static final int RIL_UNSOL_HW_IMS_RESPONSE_RTT_EVENT = 2090;
    public static final int RIL_UNSOL_HW_IMS_RINGBACK_TONE = 2033;
    public static final int RIL_UNSOL_HW_IMS_SRV_STATUS_UPDATE = 2035;
    public static final int RIL_UNSOL_HW_IMS_SUPP_SVC_NOTIFICATION = 2038;
    public static final int RIL_UNSOL_HW_IMS_VOICE_BAND_INFO = 2039;
    public static final int RIL_UNSOL_HW_INIT_LOCINFO = 2070;
    public static final int RIL_UNSOL_HW_LAA_STATE = 2080;
    public static final int RIL_UNSOL_HW_LIMIT_PDP_ACT_IND = 2071;
    public static final int RIL_UNSOL_HW_LOW_TEMPRATURE = 2087;
    public static final int RIL_UNSOL_HW_LTE_FREQ_WITH_WLAN_COEX = 2045;
    public static final int RIL_UNSOL_HW_LTE_PDCP_INFO = 2065;
    public static final int RIL_UNSOL_HW_LTE_RRC_INFO = 2066;
    public static final int RIL_UNSOL_HW_MCC_CHANGE = 2075;
    public static final int RIL_UNSOL_HW_MIMO_STATE_IND = 2088;
    public static final int RIL_UNSOL_HW_MIPICLK = 2041;
    public static final int RIL_UNSOL_HW_MODIFY_CALL = 2008;
    public static final int RIL_UNSOL_HW_NCELL_MONITOR = 2024;
    public static final int RIL_UNSOL_HW_NCFG_FINISHED_IND = 2091;
    public static final int RIL_UNSOL_HW_NETWORK_REJECT_CASE = 2026;
    public static final int RIL_UNSOL_HW_ON_SS = 2004;
    public static final int RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND = 2030;
    public static final int RIL_UNSOL_HW_PROXY_INIT_STATE_CHANGED = 2044;
    public static final int RIL_UNSOL_HW_QOS_STATE_CHANGED_IND = 2007;
    public static final int RIL_UNSOL_HW_RAT_COMBINE_MODE_IND = 2068;
    public static final int RIL_UNSOL_HW_REC_PSE_BASESTATION_REPORT = 2092;
    public static final int RIL_UNSOL_HW_RESET_CHR_IND = 2018;
    public static final int RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED = 2021;
    public static final int RIL_UNSOL_HW_RESPONSE_DATA_NETWORK_STATE_CHANGED = 2003;
    public static final int RIL_UNSOL_HW_RESPONSE_IMS_NETWORK_STATE_CHANGED = 2001;
    public static final int RIL_UNSOL_HW_RESPONSE_SIMLOCK_STATUS_CHANGED = 2012;
    public static final int RIL_UNSOL_HW_RESPONSE_SIM_TYPE = 2000;
    public static final int RIL_UNSOL_HW_RESPONSE_TETHERED_MODE_STATE_CHANGED = 2002;
    public static final int RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH = 2078;
    public static final int RIL_UNSOL_HW_RIL_CHR_IND = 2017;
    public static final int RIL_UNSOL_HW_ROAMING_MODE_SWITCH = 2064;
    public static final int RIL_UNSOL_HW_SIGNAL_STRENGTH = 2077;
    public static final int RIL_UNSOL_HW_SIMSLOT_CFG = 2016;
    public static final int RIL_UNSOL_HW_SIM_HOTPLUG = 2019;
    public static final int RIL_UNSOL_HW_SIM_ICCID_CHANGED = 2020;
    public static final int RIL_UNSOL_HW_SIM_PNP = 2022;
    public static final int RIL_UNSOL_HW_SIM_SWITCH = 2069;
    public static final int RIL_UNSOL_HW_STK_CC_ALPHA_NOTIFY = 2005;
    public static final int RIL_UNSOL_HW_SVLTE_PS_TRANSFER_IND = 2029;
    public static final int RIL_UNSOL_HW_SYS_INFO_IND = 2084;
    public static final int RIL_UNSOL_HW_TEE_TASK_TIME_OUT = 2047;
    public static final int RIL_UNSOL_HW_TETHERED_MODE_STATE_CHANGED = 2014;
    public static final int RIL_UNSOL_HW_TRIGGER_SETUP_DATA_CALL = 2015;
    public static final int RIL_UNSOL_HW_UICC_SUBSCRIPTION_STATUS_CHANGED = 2006;
    public static final int RIL_UNSOL_HW_UIM_LOCKCARD = 2040;
    public static final int RIL_UNSOL_HW_ULFREQ = 2093;
    public static final int RIL_UNSOL_HW_VSIM_OTA_SMS_REPORT = 2028;
    public static final int RIL_UNSOL_HW_VSIM_RDH_REQUEST = 2027;
    public static final int RIL_UNSOL_HW_VT_FLOW_INFO_REPORT = 2079;
    public static final int RIL_UNSOL_HW_XPASS_INFO_RPT = 2051;
    public static final int RIL_UNSOL_RSRVCC_STATE_NOTIFY = 2076;

    public static final String toString(int o) {
        if (o == 4) {
            return "MAX_INTS_NUM";
        }
        if (o == 4) {
            return "MAX_STRINGS_NUM";
        }
        if (o == 500) {
            return "RIL_REQUEST_HW_DEVICE_BASE";
        }
        if (o == 500) {
            return "RIL_REQUEST_HW_IMS_REGISTRATION_STATE";
        }
        if (o == 501) {
            return "RIL_REQUEST_HW_IMS_SEND_SMS";
        }
        if (o == 502) {
            return "RIL_REQUEST_HW_GET_DATA_CALL_PROFILE";
        }
        if (o == 503) {
            return "RIL_REQUEST_HW_SET_UICC_SUBSCRIPTION";
        }
        if (o == 504) {
            return "RIL_REQUEST_HW_SET_DATA_SUBSCRIPTION";
        }
        if (o == 505) {
            return "RIL_REQUEST_HW_GET_UICC_SUBSCRIPTION";
        }
        if (o == 506) {
            return "RIL_REQUEST_HW_GET_DATA_SUBSCRIPTION";
        }
        if (o == 507) {
            return "RIL_REQUEST_HW_SET_SUBSCRIPTION_MODE";
        }
        if (o == 508) {
            return "RIL_REQUEST_HW_SETUP_QOS";
        }
        if (o == 509) {
            return "RIL_REQUEST_HW_RELEASE_QOS";
        }
        if (o == 510) {
            return "RIL_REQUEST_HW_GET_QOS_STATUS";
        }
        if (o == 511) {
            return "RIL_REQUEST_HW_MODIFY_QOS";
        }
        if (o == 512) {
            return "RIL_REQUEST_HW_SUSPEND_QOS";
        }
        if (o == 513) {
            return "RIL_REQUEST_HW_RESUME_QOS";
        }
        if (o == 514) {
            return "RIL_REQUEST_HW_MODIFY_CALL_INITIATE";
        }
        if (o == 515) {
            return "RIL_REQUEST_HW_MODIFY_CALL_CONFIRM";
        }
        if (o == 516) {
            return "RIL_REQUEST_HW_SWITCH_MTKSIM";
        }
        if (o == 517) {
            return "RIL_REQUEST_HW_SET_POWER_GRADE";
        }
        if (o == 518) {
            return "RIL_REQUEST_HW_SENDAPDU";
        }
        if (o == 519) {
            return "RIL_REQUEST_HW_SET_AUDIO_CHANNEL";
        }
        if (o == 520) {
            return "RIL_REQUEST_HW_SET_PCM";
        }
        if (o == 521) {
            return "RIL_REQUEST_HW_QUERY_EMERGENCY_NUMBERS";
        }
        if (o == 522) {
            return "RIL_REQUEST_HW_MODEMSTATUS_REPORT";
        }
        if (o == 523) {
            return "RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ";
        }
        if (o == 524) {
            return "RIL_REQUEST_HW_STK_CONFIRM_REFRESH";
        }
        if (o == 525) {
            return "RIL_REQUEST_HW_STK_GET_LOCAL_INFORMATION";
        }
        if (o == 526) {
            return "RIL_REQUEST_HW_DEVICE_RESERVED_27";
        }
        if (o == 527) {
            return "RIL_REQUEST_HW_QUERY_CARDTYPE";
        }
        if (o == 528) {
            return "RIL_REQUEST_HW_GET_CDMA_GSM_IMSI";
        }
        if (o == 529) {
            return "RIL_REQUEST_HW_SET_SIMSLOT_TO_VIA";
        }
        if (o == 530) {
            return "RIL_REQUEST_HW_VOICE_LOOPBACK";
        }
        if (o == 531) {
            return "RIL_REQUEST_HW_GET_CDMA_CHR_INFO";
        }
        if (o == 532) {
            return "RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE";
        }
        if (o == 533) {
            return "RIL_REQUEST_HW_GET_LOCATION_INFO";
        }
        if (o == 534) {
            return "RIL_REQUEST_HW_SET_WIFI_POWER_GRADE";
        }
        if (o == 535) {
            return "RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2";
        }
        if (o == 536) {
            return "RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE";
        }
        if (o == 537) {
            return "RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS";
        }
        if (o == 538) {
            return "RIL_REQUEST_HW_MODEM_POWER";
        }
        if (o == 539) {
            return "RIL_REQUEST_HW_SET_SIM_LESS";
        }
        if (o == 540) {
            return "RIL_REQUEST_HW_SET_TRANSMIT_POWER";
        }
        if (o == 541) {
            return "RIL_REQUEST_HW_RESTRAT_RILD";
        }
        if (o == 542) {
            return "RIL_REQUEST_HW_SIM_TRANSMIT_BASIC";
        }
        if (o == 543) {
            return "RIL_REQUEST_HW_SIM_OPEN_CHANNEL";
        }
        if (o == 544) {
            return "RIL_REQUEST_HW_SIM_CLOSE_CHANNEL";
        }
        if (o == 545) {
            return "RIL_REQUEST_HW_SIM_TRANSMIT_CHANNEL";
        }
        if (o == 546) {
            return "RIL_REQUEST_HW_MODIFY_DATA_PROFILE";
        }
        if (o == 547) {
            return "RIL_REQUEST_HW_DATA_CONNECTION_DETACH";
        }
        if (o == 548) {
            return "RIL_REQUEST_HW_DATA_CONNECTION_ATTACH";
        }
        if (o == 549) {
            return "RIL_REQUEST_HW_GET_DATA_PROFILE";
        }
        if (o == 550) {
            return "RIL_REQUEST_HW_GET_SIM_CAPACITY";
        }
        if (o == 551) {
            return "RIL_REQUEST_HW_SET_LONG_MESSAGE";
        }
        if (o == 552) {
            return "RIL_REQUEST_HW_CGSMS_MESSAGE";
        }
        if (o == 553) {
            return "RIL_REQUEST_HW_RESET_ALL_CONNECTIONS";
        }
        if (o == 554) {
            return "RIL_REQUEST_HW_RRC_CONTROL";
        }
        if (o == 555) {
            return "RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE";
        }
        if (o == 556) {
            return "RIL_REQUEST_HW_GET_VOICECALL_BACKGROUND_STATE";
        }
        if (o == 557) {
            return "RIL_REQUEST_HW_GET_IMEI_VERIFY_STATUS";
        }
        if (o == 558) {
            return "RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG";
        }
        if (o == 559) {
            return "RIL_REQUEST_HW_SET_PSDOMAIN_AUTOATTACH_TYPE";
        }
        if (o == 560) {
            return "RIL_REQUEST_HW_GET_PSDOMAIN_AUTOATTACH_TYPE";
        }
        if (o == 561) {
            return "RIL_REQUEST_HW_SET_NCELL_MONITOR_STATE";
        }
        if (o == 562) {
            return "RIL_REQUEST_HW_GET_USER_SERVICE_STATE";
        }
        if (o == 563) {
            return "RIL_REQUEST_HW_MONITOR_SIM_IN_SLOT_IND";
        }
        if (o == 564) {
            return "RIL_REQUEST_HW_SET_SIM_SLOT_CFG";
        }
        if (o == 565) {
            return "RIL_REQUEST_HW_GET_SIM_SLOT_CFG";
        }
        if (o == 566) {
            return "RIL_REQUEST_HW_HANDLE_DETECT";
        }
        if (o == 567) {
            return "RIL_REQUEST_HW_GET_HANDLE_DETECT";
        }
        if (o == 568) {
            return "RIL_REQUEST_HW_SIM_GET_ATR";
        }
        if (o == 569) {
            return "RIL_REQUEST_HW_VSIM_GET_ALGROITHM_AND_MODEM_PUBKEY";
        }
        if (o == 570) {
            return "RIL_REQUEST_HW_VSIM_SET_SERVER_PUBKEY";
        }
        if (o == 571) {
            return "RIL_REQUEST_HW_FILE_WRITE";
        }
        if (o == 572) {
            return "RIL_REQUEST_HW_VSIM_GET_SIM_CONTENT";
        }
        if (o == 573) {
            return "RIL_REQUEST_HW_VSIM_SET_SIM_STATE";
        }
        if (o == 574) {
            return "RIL_REQUEST_HW_VSIM_GET_SIM_STATE";
        }
        if (o == 575) {
            return "RIL_REQUEST_HW_GET_EOPLMN_LIST";
        }
        if (o == 576) {
            return "RIL_REQUEST_HW_SET_EOPLMN_LIST";
        }
        if (o == 577) {
            return "RIL_REQUEST_HW_GET_SYSTEM_INFO_EX";
        }
        if (o == 578) {
            return "RIL_REQUEST_HW_GET_PLMN_INFO";
        }
        if (o == 579) {
            return "RIL_REQUEST_HW_IMS_DIAL";
        }
        if (o == 580) {
            return "RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS";
        }
        if (o == 581) {
            return "RIL_REQUEST_HW_IMS_HANGUP";
        }
        if (o == 582) {
            return "RIL_REQUEST_HW_IMS_HANGUP_WAITING_OR_BACKGROUND";
        }
        if (o == 583) {
            return "RIL_REQUEST_HW_IMS_HANGUP_FOREGROUND_RESUME_BACKGROUND";
        }
        if (o == 584) {
            return "RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE";
        }
        if (o == 585) {
            return "RIL_REQUEST_HW_IMS_CONFERENCE";
        }
        if (o == 586) {
            return "RIL_REQUEST_HW_IMS_UDUB";
        }
        if (o == 587) {
            return "RIL_REQUEST_HW_IMS_LAST_CALL_FAIL_CAUSE";
        }
        if (o == 588) {
            return "RIL_REQUEST_HW_IMS_SEND_USSD";
        }
        if (o == 589) {
            return "RIL_REQUEST_HW_IMS_CANCEL_USSD";
        }
        if (o == 590) {
            return "RIL_REQUEST_HW_IMS_ANSWER";
        }
        if (o == 591) {
            return "RIL_REQUEST_HW_IMS_DTMF";
        }
        if (o == 592) {
            return "RIL_REQUEST_HW_IMS_DTMF_START";
        }
        if (o == 593) {
            return "RIL_REQUEST_HW_IMS_DTMF_STOP";
        }
        if (o == 594) {
            return "RIL_REQUEST_HW_IMS_EXPLICIT_CALL_TRANSFER";
        }
        if (o == 595) {
            return "RIL_REQUEST_HW_IMS_REG_STATE_CHANGE";
        }
        if (o == 596) {
            return "RIL_REQUEST_HW_IMS_IMSVOPS_IND";
        }
        if (o == 597) {
            return "RIL_REQUEST_HW_IMS_SET_MUTE";
        }
        if (o == 598) {
            return "RIL_REQUEST_HW_ANT_SWITCH";
        }
        if (o == 599) {
            return "RIL_REQUEST_HW_GET_ANT_SWITCH";
        }
        if (o == 600) {
            return "RIL_REQUEST_GET_POL_CAPABILITY";
        }
        if (o == 601) {
            return "RIL_REQUEST_GET_POL_LIST";
        }
        if (o == 602) {
            return "RIL_REQUEST_SET_POL_ENTRY";
        }
        if (o == 603) {
            return "RIL_REQUEST_HW_GET_ISMCOEX";
        }
        if (o == 604) {
            return "RIL_REQUEST_HW_SET_ISMCOEX";
        }
        if (o == 605) {
            return "RIL_REQUEST_HW_IMS_IMPU";
        }
        if (o == 606) {
            return "RIL_REQUEST_HW_SET_BAND_CFG";
        }
        if (o == 607) {
            return "RIL_REQUEST_HW_GET_BAND_CFG";
        }
        if (o == 608) {
            return "RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO";
        }
        if (o == 609) {
            return "RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO";
        }
        if (o == 610) {
            return "RIL_REQUEST_HW_GET_SIMLOCK_STATUS";
        }
        if (o == 611) {
            return "RIL_REQUEST_HW_GET_ICCID";
        }
        if (o == 612) {
            return "RIL_REQUEST_HW_IMS_ADD_CONFERENCE_MEMBER";
        }
        if (o == 613) {
            return "RIL_REQUEST_HW_IMS_GET_CLIR";
        }
        if (o == 614) {
            return "RIL_REQUEST_HW_IMS_SET_CLIR";
        }
        if (o == 615) {
            return "RIL_REQUEST_HW_IMS_SET_CALL_WAITING";
        }
        if (o == 616) {
            return "RIL_REQUEST_HW_QUERY_GSM_NMR_INFO";
        }
        if (o == 617) {
            return "RIL_REQUEST_HW_UICC_AUTH";
        }
        if (o == 618) {
            return "RIL_REQUEST_HW_UICC_GBA_BOOTSTRAP";
        }
        if (o == 619) {
            return "RIL_REQUEST_HW_UPDATE_UICC_FILE";
        }
        if (o == 620) {
            return "RIL_REQUEST_HW_GET_UICC_FILE";
        }
        if (o == 621) {
            return "RIL_REQUEST_HW_UICC_KS_NAF_AUTH";
        }
        if (o == 622) {
            return "RIL_REQUEST_HW_INITIAL_MESSAGE";
        }
        if (o == 623) {
            return "RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX";
        }
        if (o == 624) {
            return "RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE";
        }
        if (o == 625) {
            return "RIL_REQUEST_HW_GET_DS_FLOW_INFO";
        }
        if (o == 626) {
            return "RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO";
        }
        if (o == 627) {
            return "RIL_REQUEST_HW_SET_DS_FLOW_REPORT_FLAG";
        }
        if (o == 628) {
            return "RIL_REQUEST_HW_GET_DEVICE_VERSION";
        }
        if (o == 629) {
            return "RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG";
        }
        if (o == 630) {
            return "RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD";
        }
        if (o == 631) {
            return "RIL_REQUEST_HW_GET_SCI_CHG_CFG";
        }
        if (o == 632) {
            return "RIL_REQUEST_HW_DSDS_GET_SIM_STATUS";
        }
        if (o == 633) {
            return "RIL_REQUEST_HW_IMS_MODIFY_CALL_INITIATE";
        }
        if (o == 634) {
            return "RIL_REQUEST_HW_IMS_MODIFY_CALL_CONFIRM";
        }
        if (o == 635) {
            return "RIL_REQUEST_HW_SET_VOICEPREFER_STATUS";
        }
        if (o == 636) {
            return "RIL_REQUEST_HW_GET_VOICEPREFER_STATUS";
        }
        if (o == 637) {
            return "RIL_REQUEST_HW_IMS_ECONF_DIAL";
        }
        if (o == 638) {
            return "RIL_REQUEST_HW_IMS_GET_ECONF_INFO";
        }
        if (o == 639) {
            return "RIL_REQUEST_HW_IMS_ECONF_REMOVE_MEMBER";
        }
        if (o == 640) {
            return "RIL_REQUEST_HW_IMS_MERGE_ECONF";
        }
        if (o == 641) {
            return "RIL_REQUEST_HW_IMS_ECONF_ADD_MEMBER";
        }
        if (o == 642) {
            return "RIL_REQUEST_HW_IMS_GET_ECONF_LAST_FAIL_CAUSE";
        }
        if (o == 643) {
            return "RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH";
        }
        if (o == 644) {
            return "RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION";
        }
        if (o == 645) {
            return "RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION";
        }
        if (o == 646) {
            return "RIL_REQUEST_HW_AP_SET_DS_FLOW_CONFIG";
        }
        if (o == 647) {
            return "RIL_REQUEST_HW_VSIM_CHECK_CARD";
        }
        if (o == 648) {
            return "RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA";
        }
        if (o == 649) {
            return "RIL_REQUEST_GET_DS_FLOW_NV_WRITE_CFG_PARA";
        }
        if (o == 650) {
            return "RIL_REQUEST_HW_SET_IMS_SWITCH";
        }
        if (o == 651) {
            return "RIL_REQUEST_HW_GET_IMS_SWITCH";
        }
        if (o == 652) {
            return "RIL_REQUEST_HW_ICC_PREF_APP_SWITCH";
        }
        if (o == 653) {
            return "RIL_REQUEST_HW_GET_CIMSI";
        }
        if (o == 654) {
            return "RIL_REQUEST_HW_SET_CDMA_MODE_SIDE";
        }
        if (o == 655) {
            return "RIL_REQUEST_HW_SET_UE_OPERATION_MODE";
        }
        if (o == 656) {
            return "RIL_REQUEST_HW_VSIM_POWER";
        }
        if (o == 657) {
            return "RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS";
        }
        if (o == 658) {
            return "RIL_REQUEST_HW_GET_CTROAMINFO";
        }
        if (o == 659) {
            return "RIL_REQUEST_HW_SET_CT_OOS_COUNT";
        }
        if (o == 660) {
            return "RIL_REQUEST_HW_IMS_DOMAIN_CONFIG";
        }
        if (o == 661) {
            return "RIL_REQUEST_HW_VOWIFI_IMSA_MSG";
        }
        if (o == 662) {
            return "RIL_REQUEST_HW_GET_IMS_DOMAIN";
        }
        if (o == 663) {
            return "RIL_REQUEST_HW_GET_CDMA_MODE_SIDE";
        }
        if (o == 664) {
            return "RIL_REQUEST_HW_VOWIFI_UICC_AUTH";
        }
        if (o == 665) {
            return "RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND";
        }
        if (o == 666) {
            return "RIL_REQUEST_HW_SET_TIME";
        }
        if (o == 667) {
            return "RIL_REQUEST_VSIM_BASEBAND_VERSION";
        }
        if (o == 668) {
            return "RIL_REQUEST_HW_SET_VT_LTE_QUALITY_RPT_CFG";
        }
        if (o == 669) {
            return "RIL_REQUEST_HW_IMS_REJ_CALL";
        }
        if (o == 670) {
            return "RIL_REQUEST_HW_REJ_CALL";
        }
        if (o == 671) {
            return "RIL_REQUEST_HW_IMS_CHANNEL_INFO";
        }
        if (o == 672) {
            return "RIL_REQUEST_HW_CHANNEL_INFO";
        }
        if (o == 673) {
            return "RIL_REQUEST_HW_GET_RAT_COMBINE_MODE";
        }
        if (o == 674) {
            return "RIL_REQUEST_HW_IMS_SET_DM_PCSCF";
        }
        if (o == 675) {
            return "RIL_REQUEST_HW_IMS_SET_DM_DYN";
        }
        if (o == 676) {
            return "RIL_REQUEST_HW_IMS_SET_DM_TIMER";
        }
        if (o == 677) {
            return "RIL_REQUEST_HW_IMS_SET_DM_SMS";
        }
        if (o == 678) {
            return "RIL_REQUEST_HW_IMS_GET_DM_PCSCF";
        }
        if (o == 679) {
            return "RIL_REQUEST_HW_IMS_GET_DM_TIMER";
        }
        if (o == 680) {
            return "RIL_REQUEST_HW_IMS_GET_DM_DYN";
        }
        if (o == 681) {
            return "RIL_REQUEST_HW_IMS_GET_DM_SMS";
        }
        if (o == 682) {
            return "RIL_REQUEST_HW_IMS_GET_DM_USER";
        }
        if (o == 683) {
            return "RIL_REQUEST_HW_IMS_SET_NICK_NAME";
        }
        if (o == 684) {
            return "RIL_REQUEST_HW_IMS_GET_NICK_NAME";
        }
        if (o == 685) {
            return "RIL_REQUEST_HW_IMS_BATTERY_STATUS";
        }
        if (o == 686) {
            return "RIL_REQUEST_HW_IMS_REGISTER";
        }
        if (o == 687) {
            return "RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY";
        }
        if (o == 688) {
            return "RIL_REQUEST_HW_SET_PSEUDO_INFO";
        }
        if (o == 689) {
            return "RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY";
        }
        if (o == 690) {
            return "RIL_REQUEST_HW_SIGNAL_STRENGTH";
        }
        if (o == 691) {
            return "RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID";
        }
        if (o == 692) {
            return "RIL_REQUEST_HW_SELECT_CSGID";
        }
        if (o == 693) {
            return "RIL_REQUEST_HW_IMS_LAST_CALL_CAUSE";
        }
        if (o == 694) {
            return "RIL_REQUEST_HW_SET_IMSVT_CAPABILITY";
        }
        if (o == 695) {
            return "RIL_REQUEST_HW_SET_WIFI_EMERGENCY_AID";
        }
        if (o == 696) {
            return "RIL_REQUEST_HW_SEND_LAA_CMD";
        }
        if (o == 697) {
            return "RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY";
        }
        if (o == 698) {
            return "RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY";
        }
        if (o == 699) {
            return "RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA";
        }
        if (o == 700) {
            return "RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE";
        }
        if (o == 701) {
            return "RIL_REQUEST_HW_GET_LAA_STATE";
        }
        if (o == 702) {
            return "RIL_REQUEST_HW_SET_RCS_SWITCH";
        }
        if (o == 703) {
            return "RIL_REQUEST_HW_GET_RCS_SWITCH_STATE";
        }
        if (o == 704) {
            return "RIL_REQUEST_HW_SET_DM_RCS_CFG";
        }
        if (o == 705) {
            return "RIL_REQUEST_HW_EXCHANGE_MODEM_INFO";
        }
        if (o == 706) {
            return "RIL_REQUEST_HW_INFORM_MODEM_TETHER_STATUS";
        }
        if (o == 707) {
            return "RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_WITH_IMS_DOMAIN";
        }
        if (o == 708) {
            return "RIL_REQUEST_HW_VOLTE_IMPI";
        }
        if (o == 709) {
            return "RIL_REQUEST_HW_VOLTE_DOMAIN";
        }
        if (o == 710) {
            return "RIL_REQUEST_CANCEL_IMS_VIDEO_CALL";
        }
        if (o == 711) {
            return "RIL_REQUEST_HW_SET_DATA_SWITCH";
        }
        if (o == 712) {
            return "RIL_REQUEST_HW_SET_DATA_ROAM_SWITCH";
        }
        if (o == 713) {
            return "RIL_REQUEST_HW_SET_IMS_SMS_CONFIG";
        }
        if (o == 714) {
            return "RIL_REQUEST_HW_GET_IMS_SMS_CONFIG";
        }
        if (o == 715) {
            return "RIL_REQUEST_HW_GET_CSGIDSRCH_INFO";
        }
        if (o == 716) {
            return "RIL_REQUEST_HW_SET_REATTACH_REQUIRED_UNSOL";
        }
        if (o == 717) {
            return "RIL_REQUEST_HW_SET_DEEP_NO_DISTURB_SWITCH";
        }
        if (o == 718) {
            return "RIL_REQUEST_HW_ACTIVE_NCFG_BIN";
        }
        if (o == 719) {
            return "RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_V1_2";
        }
        if (o == 720) {
            return "RIL_REQUEST_HW_GET_CURRENT_CALLS_V1_2";
        }
        if (o == 721) {
            return "RIL_REQUEST_HW_GET_NUM_OF_REC_PSE_BASE_STATION";
        }
        if (o == 722) {
            return "RIL_REQUEST_HW_IMS_SET_RTT_STATUS";
        }
        if (o == 723) {
            return "RIL_REQUEST_HW_IMS_ANSWER_V1_3";
        }
        if (o == 724) {
            return "RIL_REQUEST_HW_IMS_DIAL_V1_3";
        }
        if (o == 725) {
            return "RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE_V1_3";
        }
        if (o == 726) {
            return "RIL_REQUEST_HW_IMS_RTT_MODIFY";
        }
        if (o == 727) {
            return "RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_V1_3";
        }
        if (o == 728) {
            return "RIL_REQUEST_HW_SEND_NCFG_OPER_INFO";
        }
        if (o == 729) {
            return "RIL_REQUEST_HW_IMS_RESTRAT_RILD";
        }
        if (o == 730) {
            return "RIL_REQUEST_HW_IMS_UICC_AUTH";
        }
        if (o == 731) {
            return "RIL_REQUEST_HW_GET_CAP_OF_REC_PSE_BASESTATION";
        }
        if (o == 2000) {
            return "RIL_UNSOL_HW_DEVICE_BASE";
        }
        if (o == 2000) {
            return "RIL_UNSOL_HW_RESPONSE_SIM_TYPE";
        }
        if (o == 2001) {
            return "RIL_UNSOL_HW_RESPONSE_IMS_NETWORK_STATE_CHANGED";
        }
        if (o == 2002) {
            return "RIL_UNSOL_HW_RESPONSE_TETHERED_MODE_STATE_CHANGED";
        }
        if (o == 2003) {
            return "RIL_UNSOL_HW_RESPONSE_DATA_NETWORK_STATE_CHANGED";
        }
        if (o == 2004) {
            return "RIL_UNSOL_HW_ON_SS";
        }
        if (o == 2005) {
            return "RIL_UNSOL_HW_STK_CC_ALPHA_NOTIFY";
        }
        if (o == 2006) {
            return "RIL_UNSOL_HW_UICC_SUBSCRIPTION_STATUS_CHANGED";
        }
        if (o == 2007) {
            return "RIL_UNSOL_HW_QOS_STATE_CHANGED_IND";
        }
        if (o == 2008) {
            return "RIL_UNSOL_HW_MODIFY_CALL";
        }
        if (o == 2009) {
            return "RIL_UNSOL_HW_DIALUP_STATE_CHANGED";
        }
        if (o == 2010) {
            return "RIL_UNSOL_HW_CURR_MCC";
        }
        if (o == 2011) {
            return "RIL_UNSOL_HW_CURR_GSM_STATE";
        }
        if (o == 2012) {
            return "RIL_UNSOL_HW_RESPONSE_SIMLOCK_STATUS_CHANGED";
        }
        if (o == 2013) {
            return "RIL_UNSOL_HW_CG_SWITCH_RECOVERY";
        }
        if (o == 2014) {
            return "RIL_UNSOL_HW_TETHERED_MODE_STATE_CHANGED";
        }
        if (o == 2015) {
            return "RIL_UNSOL_HW_TRIGGER_SETUP_DATA_CALL";
        }
        if (o == 2016) {
            return "RIL_UNSOL_HW_SIMSLOT_CFG";
        }
        if (o == 2017) {
            return "RIL_UNSOL_HW_RIL_CHR_IND";
        }
        if (o == 2018) {
            return "RIL_UNSOL_HW_RESET_CHR_IND";
        }
        if (o == 2019) {
            return "RIL_UNSOL_HW_SIM_HOTPLUG";
        }
        if (o == 2020) {
            return "RIL_UNSOL_HW_SIM_ICCID_CHANGED";
        }
        if (o == 2021) {
            return "RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED";
        }
        if (o == 2022) {
            return "RIL_UNSOL_HW_SIM_PNP";
        }
        if (o == 2023) {
            return "RIL_UNSOL_HW_CS_CHANNEL_INFO_IND";
        }
        if (o == 2024) {
            return "RIL_UNSOL_HW_NCELL_MONITOR";
        }
        if (o == 2025) {
            return "RIL_UNSOL_HW_ECCNUM";
        }
        if (o == 2026) {
            return "RIL_UNSOL_HW_NETWORK_REJECT_CASE";
        }
        if (o == 2027) {
            return "RIL_UNSOL_HW_VSIM_RDH_REQUEST";
        }
        if (o == 2028) {
            return "RIL_UNSOL_HW_VSIM_OTA_SMS_REPORT";
        }
        if (o == 2029) {
            return "RIL_UNSOL_HW_SVLTE_PS_TRANSFER_IND";
        }
        if (o == 2030) {
            return "RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND";
        }
        if (o == 2031) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_CALL_STATE_CHANGED";
        }
        if (o == 2032) {
            return "RIL_UNSOL_HW_IMS_CALL_RING";
        }
        if (o == 2033) {
            return "RIL_UNSOL_HW_IMS_RINGBACK_TONE";
        }
        if (o == 2034) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_HANDOVER";
        }
        if (o == 2035) {
            return "RIL_UNSOL_HW_IMS_SRV_STATUS_UPDATE";
        }
        if (o == 2036) {
            return "RIL_UNSOL_HW_IMS_ON_USSD";
        }
        if (o == 2037) {
            return "RIL_UNSOL_HW_IMS_ON_SS";
        }
        if (o == 2038) {
            return "RIL_UNSOL_HW_IMS_SUPP_SVC_NOTIFICATION";
        }
        if (o == 2039) {
            return "RIL_UNSOL_HW_IMS_VOICE_BAND_INFO";
        }
        if (o == 2040) {
            return "RIL_UNSOL_HW_UIM_LOCKCARD";
        }
        if (o == 2041) {
            return "RIL_UNSOL_HW_MIPICLK";
        }
        if (o == 2042) {
            return "RIL_UNSOL_HW_APR_SVLTE_IND";
        }
        if (o == 2043) {
            return "RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT";
        }
        if (o == 2044) {
            return "RIL_UNSOL_HW_PROXY_INIT_STATE_CHANGED";
        }
        if (o == 2045) {
            return "RIL_UNSOL_HW_LTE_FREQ_WITH_WLAN_COEX";
        }
        if (o == 2046) {
            return "RIL_UNSOL_HW_DS_FLOW_INFO_REPORT";
        }
        if (o == 2047) {
            return "RIL_UNSOL_HW_TEE_TASK_TIME_OUT";
        }
        if (o == 2048) {
            return "RIL_UNSOL_HW_DSDS_SIM_STATUS_CHANGED";
        }
        if (o == 2049) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_IND";
        }
        if (o == 2050) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_END_CAUSE";
        }
        if (o == 2051) {
            return "RIL_UNSOL_HW_XPASS_INFO_RPT";
        }
        if (o == 2052) {
            return "RIL_UNSOL_HOOK_HW_VP_STATUS";
        }
        if (o == 2053) {
            return "RIL_UNSOL_HW_IMS_ECONF_STATE_CHANGED";
        }
        if (o == 2054) {
            return "RIL_UNSOL_HW_EXIST_NETWORK_INFO";
        }
        if (o == 2055) {
            return "RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT";
        }
        if (o == 2056) {
            return "RIL_UNSOL_HW_IMS_MT_STATUS_REPORT";
        }
        if (o == 2057) {
            return "RIL_UNSOL_HW_CA_STATE_CHANGED";
        }
        if (o == 2058) {
            return "RIL_UNSOL_HW_IMS_CS_REDIAL_NOTIFY";
        }
        if (o == 2059) {
            return "RIL_UNSOL_HW_CTROAMINFO_CHANGED";
        }
        if (o == 2060) {
            return "RIL_UNSOL_HW_CDMA_HPLMN_UPDATE";
        }
        if (o == 2061) {
            return "RIL_UNSOL_HW_IMSA_VOWIFI_MSG";
        }
        if (o == 2062) {
            return "RIL_UNSOL_HW_IMS_DATA_CONNECT_IND";
        }
        if (o == 2063) {
            return "RIL_UNSOL_HW_IMS_DATA_DISCONNECT_IND";
        }
        if (o == 2064) {
            return "RIL_UNSOL_HW_ROAMING_MODE_SWITCH";
        }
        if (o == 2065) {
            return "RIL_UNSOL_HW_LTE_PDCP_INFO";
        }
        if (o == 2066) {
            return "RIL_UNSOL_HW_LTE_RRC_INFO";
        }
        if (o == 2067) {
            return "RIL_UNSOL_HW_CRR_CONN_IND";
        }
        if (o == 2068) {
            return "RIL_UNSOL_HW_RAT_COMBINE_MODE_IND";
        }
        if (o == 2069) {
            return "RIL_UNSOL_HW_SIM_SWITCH";
        }
        if (o == 2070) {
            return "RIL_UNSOL_HW_INIT_LOCINFO";
        }
        if (o == 2071) {
            return "RIL_UNSOL_HW_LIMIT_PDP_ACT_IND";
        }
        if (o == 2072) {
            return "RIL_UNSOL_HW_IMS_DMCN";
        }
        if (o == 2073) {
            return "RIL_UNSOL_HW_IMS_REG_FAILED_INFO";
        }
        if (o == 2074) {
            return "RIL_UNSOL_HW_IMS_HOLD_TONE_IND";
        }
        if (o == 2075) {
            return "RIL_UNSOL_HW_MCC_CHANGE";
        }
        if (o == 2076) {
            return "RIL_UNSOL_RSRVCC_STATE_NOTIFY";
        }
        if (o == 2077) {
            return "RIL_UNSOL_HW_SIGNAL_STRENGTH";
        }
        if (o == 2078) {
            return "RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH";
        }
        if (o == 2079) {
            return "RIL_UNSOL_HW_VT_FLOW_INFO_REPORT";
        }
        if (o == 2080) {
            return "RIL_UNSOL_HW_LAA_STATE";
        }
        if (o == 2081) {
            return "RIL_UNSOL_HW_CALL_ALT_SRV";
        }
        if (o == 2082) {
            return "RIL_UNSOL_HW_ICCID_CHANGED_IND";
        }
        if (o == 2083) {
            return "RIL_UNSOL_HW_IMS_REG_TYPE_IND";
        }
        if (o == 2084) {
            return "RIL_UNSOL_HW_SYS_INFO_IND";
        }
        if (o == 2085) {
            return "RIL_UNSOL_HW_HPLMN_IND";
        }
        if (o == 2086) {
            return "RIL_UNSOL_HW_DSDS_MODE_STATE_IND";
        }
        if (o == 2087) {
            return "RIL_UNSOL_HW_LOW_TEMPRATURE";
        }
        if (o == 2088) {
            return "RIL_UNSOL_HW_MIMO_STATE_IND";
        }
        if (o == 2089) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_RTT_ERR";
        }
        if (o == 2090) {
            return "RIL_UNSOL_HW_IMS_RESPONSE_RTT_EVENT";
        }
        if (o == 2091) {
            return "RIL_UNSOL_HW_NCFG_FINISHED_IND";
        }
        if (o == 2092) {
            return "RIL_UNSOL_HW_REC_PSE_BASESTATION_REPORT";
        }
        if (o == 732) {
            return "RIL_REQUEST_HW_SET_ULFREQ_ENABLE";
        }
        if (o == 2093) {
            return "RIL_UNSOL_HW_ULFREQ";
        }
        if (o == 735) {
            return "RIL_REQUEST_HW_SEND_MUTI_CHIP_SESSION_CONFIG";
        }
        if (o == 736) {
            return "RIL_REQUEST_HW_SEND_VSIM_DATA_TO_MODEM";
        }
        if (o == 2096) {
            return "RIL_UNSOL_HW_DL_256QAM_STATE_IND";
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
        if ((o & 500) == 500) {
            list.add("RIL_REQUEST_HW_DEVICE_BASE");
            flipped |= 500;
        }
        if ((o & 500) == 500) {
            list.add("RIL_REQUEST_HW_IMS_REGISTRATION_STATE");
            flipped |= 500;
        }
        if ((o & 501) == 501) {
            list.add("RIL_REQUEST_HW_IMS_SEND_SMS");
            flipped |= 501;
        }
        if ((o & 502) == 502) {
            list.add("RIL_REQUEST_HW_GET_DATA_CALL_PROFILE");
            flipped |= 502;
        }
        if ((o & 503) == 503) {
            list.add("RIL_REQUEST_HW_SET_UICC_SUBSCRIPTION");
            flipped |= 503;
        }
        if ((o & 504) == 504) {
            list.add("RIL_REQUEST_HW_SET_DATA_SUBSCRIPTION");
            flipped |= 504;
        }
        if ((o & 505) == 505) {
            list.add("RIL_REQUEST_HW_GET_UICC_SUBSCRIPTION");
            flipped |= 505;
        }
        if ((o & 506) == 506) {
            list.add("RIL_REQUEST_HW_GET_DATA_SUBSCRIPTION");
            flipped |= 506;
        }
        if ((o & 507) == 507) {
            list.add("RIL_REQUEST_HW_SET_SUBSCRIPTION_MODE");
            flipped |= 507;
        }
        if ((o & 508) == 508) {
            list.add("RIL_REQUEST_HW_SETUP_QOS");
            flipped |= 508;
        }
        if ((o & 509) == 509) {
            list.add("RIL_REQUEST_HW_RELEASE_QOS");
            flipped |= 509;
        }
        if ((o & 510) == 510) {
            list.add("RIL_REQUEST_HW_GET_QOS_STATUS");
            flipped |= 510;
        }
        if ((o & 511) == 511) {
            list.add("RIL_REQUEST_HW_MODIFY_QOS");
            flipped |= 511;
        }
        if ((o & 512) == 512) {
            list.add("RIL_REQUEST_HW_SUSPEND_QOS");
            flipped |= 512;
        }
        if ((o & 513) == 513) {
            list.add("RIL_REQUEST_HW_RESUME_QOS");
            flipped |= 513;
        }
        if ((o & 514) == 514) {
            list.add("RIL_REQUEST_HW_MODIFY_CALL_INITIATE");
            flipped |= 514;
        }
        if ((o & 515) == 515) {
            list.add("RIL_REQUEST_HW_MODIFY_CALL_CONFIRM");
            flipped |= 515;
        }
        if ((o & 516) == 516) {
            list.add("RIL_REQUEST_HW_SWITCH_MTKSIM");
            flipped |= 516;
        }
        if ((o & 517) == 517) {
            list.add("RIL_REQUEST_HW_SET_POWER_GRADE");
            flipped |= 517;
        }
        if ((o & 518) == 518) {
            list.add("RIL_REQUEST_HW_SENDAPDU");
            flipped |= 518;
        }
        if ((o & 519) == 519) {
            list.add("RIL_REQUEST_HW_SET_AUDIO_CHANNEL");
            flipped |= 519;
        }
        if ((o & 520) == 520) {
            list.add("RIL_REQUEST_HW_SET_PCM");
            flipped |= 520;
        }
        if ((o & 521) == 521) {
            list.add("RIL_REQUEST_HW_QUERY_EMERGENCY_NUMBERS");
            flipped |= 521;
        }
        if ((o & 522) == 522) {
            list.add("RIL_REQUEST_HW_MODEMSTATUS_REPORT");
            flipped |= 522;
        }
        if ((o & 523) == 523) {
            list.add("RIL_REQUEST_HW_RISE_CDMA_CUTOFF_FREQ");
            flipped |= 523;
        }
        if ((o & 524) == 524) {
            list.add("RIL_REQUEST_HW_STK_CONFIRM_REFRESH");
            flipped |= 524;
        }
        if ((o & 525) == 525) {
            list.add("RIL_REQUEST_HW_STK_GET_LOCAL_INFORMATION");
            flipped |= 525;
        }
        if ((o & 526) == 526) {
            list.add("RIL_REQUEST_HW_DEVICE_RESERVED_27");
            flipped |= 526;
        }
        if ((o & 527) == 527) {
            list.add("RIL_REQUEST_HW_QUERY_CARDTYPE");
            flipped |= 527;
        }
        if ((o & 528) == 528) {
            list.add("RIL_REQUEST_HW_GET_CDMA_GSM_IMSI");
            flipped |= 528;
        }
        if ((o & 529) == 529) {
            list.add("RIL_REQUEST_HW_SET_SIMSLOT_TO_VIA");
            flipped |= 529;
        }
        if ((o & 530) == 530) {
            list.add("RIL_REQUEST_HW_VOICE_LOOPBACK");
            flipped |= 530;
        }
        if ((o & 531) == 531) {
            list.add("RIL_REQUEST_HW_GET_CDMA_CHR_INFO");
            flipped |= 531;
        }
        if ((o & 532) == 532) {
            list.add("RIL_REQUEST_HW_GET_SIM_HOTPLUG_STATE");
            flipped |= 532;
        }
        if ((o & 533) == 533) {
            list.add("RIL_REQUEST_HW_GET_LOCATION_INFO");
            flipped |= 533;
        }
        if ((o & 534) == 534) {
            list.add("RIL_REQUEST_HW_SET_WIFI_POWER_GRADE");
            flipped |= 534;
        }
        if ((o & 535) == 535) {
            list.add("RIL_REQUEST_SIM_OPEN_CHANNEL_WITH_P2");
            flipped |= 535;
        }
        if ((o & 536) == 536) {
            list.add("RIL_REQUEST_HW_IMPACT_ANT_DEVSTATE");
            flipped |= 536;
        }
        if ((o & 537) == 537) {
            list.add("RIL_REQUEST_HW_SET_EMERGENCY_NUMBERS");
            flipped |= 537;
        }
        if ((o & 538) == 538) {
            list.add("RIL_REQUEST_HW_MODEM_POWER");
            flipped |= 538;
        }
        if ((o & 539) == 539) {
            list.add("RIL_REQUEST_HW_SET_SIM_LESS");
            flipped |= 539;
        }
        if ((o & 540) == 540) {
            list.add("RIL_REQUEST_HW_SET_TRANSMIT_POWER");
            flipped |= 540;
        }
        if ((o & 541) == 541) {
            list.add("RIL_REQUEST_HW_RESTRAT_RILD");
            flipped |= 541;
        }
        if ((o & 542) == 542) {
            list.add("RIL_REQUEST_HW_SIM_TRANSMIT_BASIC");
            flipped |= 542;
        }
        if ((o & 543) == 543) {
            list.add("RIL_REQUEST_HW_SIM_OPEN_CHANNEL");
            flipped |= 543;
        }
        if ((o & 544) == 544) {
            list.add("RIL_REQUEST_HW_SIM_CLOSE_CHANNEL");
            flipped |= 544;
        }
        if ((o & 545) == 545) {
            list.add("RIL_REQUEST_HW_SIM_TRANSMIT_CHANNEL");
            flipped |= 545;
        }
        if ((o & 546) == 546) {
            list.add("RIL_REQUEST_HW_MODIFY_DATA_PROFILE");
            flipped |= 546;
        }
        if ((o & 547) == 547) {
            list.add("RIL_REQUEST_HW_DATA_CONNECTION_DETACH");
            flipped |= 547;
        }
        if ((o & 548) == 548) {
            list.add("RIL_REQUEST_HW_DATA_CONNECTION_ATTACH");
            flipped |= 548;
        }
        if ((o & 549) == 549) {
            list.add("RIL_REQUEST_HW_GET_DATA_PROFILE");
            flipped |= 549;
        }
        if ((o & 550) == 550) {
            list.add("RIL_REQUEST_HW_GET_SIM_CAPACITY");
            flipped |= 550;
        }
        if ((o & 551) == 551) {
            list.add("RIL_REQUEST_HW_SET_LONG_MESSAGE");
            flipped |= 551;
        }
        if ((o & 552) == 552) {
            list.add("RIL_REQUEST_HW_CGSMS_MESSAGE");
            flipped |= 552;
        }
        if ((o & 553) == 553) {
            list.add("RIL_REQUEST_HW_RESET_ALL_CONNECTIONS");
            flipped |= 553;
        }
        if ((o & 554) == 554) {
            list.add("RIL_REQUEST_HW_RRC_CONTROL");
            flipped |= 554;
        }
        if ((o & 555) == 555) {
            list.add("RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE");
            flipped |= 555;
        }
        if ((o & 556) == 556) {
            list.add("RIL_REQUEST_HW_GET_VOICECALL_BACKGROUND_STATE");
            flipped |= 556;
        }
        if ((o & 557) == 557) {
            list.add("RIL_REQUEST_HW_GET_IMEI_VERIFY_STATUS");
            flipped |= 557;
        }
        if ((o & 558) == 558) {
            list.add("RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG");
            flipped |= 558;
        }
        if ((o & 559) == 559) {
            list.add("RIL_REQUEST_HW_SET_PSDOMAIN_AUTOATTACH_TYPE");
            flipped |= 559;
        }
        if ((o & 560) == 560) {
            list.add("RIL_REQUEST_HW_GET_PSDOMAIN_AUTOATTACH_TYPE");
            flipped |= 560;
        }
        if ((o & 561) == 561) {
            list.add("RIL_REQUEST_HW_SET_NCELL_MONITOR_STATE");
            flipped |= 561;
        }
        if ((o & 562) == 562) {
            list.add("RIL_REQUEST_HW_GET_USER_SERVICE_STATE");
            flipped |= 562;
        }
        if ((o & 563) == 563) {
            list.add("RIL_REQUEST_HW_MONITOR_SIM_IN_SLOT_IND");
            flipped |= 563;
        }
        if ((o & 564) == 564) {
            list.add("RIL_REQUEST_HW_SET_SIM_SLOT_CFG");
            flipped |= 564;
        }
        if ((o & 565) == 565) {
            list.add("RIL_REQUEST_HW_GET_SIM_SLOT_CFG");
            flipped |= 565;
        }
        if ((o & 566) == 566) {
            list.add("RIL_REQUEST_HW_HANDLE_DETECT");
            flipped |= 566;
        }
        if ((o & 567) == 567) {
            list.add("RIL_REQUEST_HW_GET_HANDLE_DETECT");
            flipped |= 567;
        }
        if ((o & 568) == 568) {
            list.add("RIL_REQUEST_HW_SIM_GET_ATR");
            flipped |= 568;
        }
        if ((o & 569) == 569) {
            list.add("RIL_REQUEST_HW_VSIM_GET_ALGROITHM_AND_MODEM_PUBKEY");
            flipped |= 569;
        }
        if ((o & 570) == 570) {
            list.add("RIL_REQUEST_HW_VSIM_SET_SERVER_PUBKEY");
            flipped |= 570;
        }
        if ((o & 571) == 571) {
            list.add("RIL_REQUEST_HW_FILE_WRITE");
            flipped |= 571;
        }
        if ((o & 572) == 572) {
            list.add("RIL_REQUEST_HW_VSIM_GET_SIM_CONTENT");
            flipped |= 572;
        }
        if ((o & 573) == 573) {
            list.add("RIL_REQUEST_HW_VSIM_SET_SIM_STATE");
            flipped |= 573;
        }
        if ((o & 574) == 574) {
            list.add("RIL_REQUEST_HW_VSIM_GET_SIM_STATE");
            flipped |= 574;
        }
        if ((o & 575) == 575) {
            list.add("RIL_REQUEST_HW_GET_EOPLMN_LIST");
            flipped |= 575;
        }
        if ((o & 576) == 576) {
            list.add("RIL_REQUEST_HW_SET_EOPLMN_LIST");
            flipped |= 576;
        }
        if ((o & 577) == 577) {
            list.add("RIL_REQUEST_HW_GET_SYSTEM_INFO_EX");
            flipped |= 577;
        }
        if ((o & 578) == 578) {
            list.add("RIL_REQUEST_HW_GET_PLMN_INFO");
            flipped |= 578;
        }
        if ((o & 579) == 579) {
            list.add("RIL_REQUEST_HW_IMS_DIAL");
            flipped |= 579;
        }
        if ((o & 580) == 580) {
            list.add("RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS");
            flipped |= 580;
        }
        if ((o & 581) == 581) {
            list.add("RIL_REQUEST_HW_IMS_HANGUP");
            flipped |= 581;
        }
        if ((o & 582) == 582) {
            list.add("RIL_REQUEST_HW_IMS_HANGUP_WAITING_OR_BACKGROUND");
            flipped |= 582;
        }
        if ((o & 583) == 583) {
            list.add("RIL_REQUEST_HW_IMS_HANGUP_FOREGROUND_RESUME_BACKGROUND");
            flipped |= 583;
        }
        if ((o & 584) == 584) {
            list.add("RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE");
            flipped |= 584;
        }
        if ((o & 585) == 585) {
            list.add("RIL_REQUEST_HW_IMS_CONFERENCE");
            flipped |= 585;
        }
        if ((o & 586) == 586) {
            list.add("RIL_REQUEST_HW_IMS_UDUB");
            flipped |= 586;
        }
        if ((o & 587) == 587) {
            list.add("RIL_REQUEST_HW_IMS_LAST_CALL_FAIL_CAUSE");
            flipped |= 587;
        }
        if ((o & 588) == 588) {
            list.add("RIL_REQUEST_HW_IMS_SEND_USSD");
            flipped |= 588;
        }
        if ((o & 589) == 589) {
            list.add("RIL_REQUEST_HW_IMS_CANCEL_USSD");
            flipped |= 589;
        }
        if ((o & 590) == 590) {
            list.add("RIL_REQUEST_HW_IMS_ANSWER");
            flipped |= 590;
        }
        if ((o & 591) == 591) {
            list.add("RIL_REQUEST_HW_IMS_DTMF");
            flipped |= 591;
        }
        if ((o & 592) == 592) {
            list.add("RIL_REQUEST_HW_IMS_DTMF_START");
            flipped |= 592;
        }
        if ((o & 593) == 593) {
            list.add("RIL_REQUEST_HW_IMS_DTMF_STOP");
            flipped |= 593;
        }
        if ((o & 594) == 594) {
            list.add("RIL_REQUEST_HW_IMS_EXPLICIT_CALL_TRANSFER");
            flipped |= 594;
        }
        if ((o & 595) == 595) {
            list.add("RIL_REQUEST_HW_IMS_REG_STATE_CHANGE");
            flipped |= 595;
        }
        if ((o & 596) == 596) {
            list.add("RIL_REQUEST_HW_IMS_IMSVOPS_IND");
            flipped |= 596;
        }
        if ((o & 597) == 597) {
            list.add("RIL_REQUEST_HW_IMS_SET_MUTE");
            flipped |= 597;
        }
        if ((o & 598) == 598) {
            list.add("RIL_REQUEST_HW_ANT_SWITCH");
            flipped |= 598;
        }
        if ((o & 599) == 599) {
            list.add("RIL_REQUEST_HW_GET_ANT_SWITCH");
            flipped |= 599;
        }
        if ((o & 600) == 600) {
            list.add("RIL_REQUEST_GET_POL_CAPABILITY");
            flipped |= 600;
        }
        if ((o & 601) == 601) {
            list.add("RIL_REQUEST_GET_POL_LIST");
            flipped |= 601;
        }
        if ((o & 602) == 602) {
            list.add("RIL_REQUEST_SET_POL_ENTRY");
            flipped |= 602;
        }
        if ((o & 603) == 603) {
            list.add("RIL_REQUEST_HW_GET_ISMCOEX");
            flipped |= 603;
        }
        if ((o & 604) == 604) {
            list.add("RIL_REQUEST_HW_SET_ISMCOEX");
            flipped |= 604;
        }
        if ((o & 605) == 605) {
            list.add("RIL_REQUEST_HW_IMS_IMPU");
            flipped |= 605;
        }
        if ((o & 606) == 606) {
            list.add("RIL_REQUEST_HW_SET_BAND_CFG");
            flipped |= 606;
        }
        if ((o & 607) == 607) {
            list.add("RIL_REQUEST_HW_GET_BAND_CFG");
            flipped |= 607;
        }
        if ((o & 608) == 608) {
            list.add("RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO");
            flipped |= 608;
        }
        if ((o & 609) == 609) {
            list.add("RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO");
            flipped |= 609;
        }
        if ((o & 610) == 610) {
            list.add("RIL_REQUEST_HW_GET_SIMLOCK_STATUS");
            flipped |= 610;
        }
        if ((o & 611) == 611) {
            list.add("RIL_REQUEST_HW_GET_ICCID");
            flipped |= 611;
        }
        if ((o & 612) == 612) {
            list.add("RIL_REQUEST_HW_IMS_ADD_CONFERENCE_MEMBER");
            flipped |= 612;
        }
        if ((o & 613) == 613) {
            list.add("RIL_REQUEST_HW_IMS_GET_CLIR");
            flipped |= 613;
        }
        if ((o & 614) == 614) {
            list.add("RIL_REQUEST_HW_IMS_SET_CLIR");
            flipped |= 614;
        }
        if ((o & 615) == 615) {
            list.add("RIL_REQUEST_HW_IMS_SET_CALL_WAITING");
            flipped |= 615;
        }
        if ((o & 616) == 616) {
            list.add("RIL_REQUEST_HW_QUERY_GSM_NMR_INFO");
            flipped |= 616;
        }
        if ((o & 617) == 617) {
            list.add("RIL_REQUEST_HW_UICC_AUTH");
            flipped |= 617;
        }
        if ((o & 618) == 618) {
            list.add("RIL_REQUEST_HW_UICC_GBA_BOOTSTRAP");
            flipped |= 618;
        }
        if ((o & 619) == 619) {
            list.add("RIL_REQUEST_HW_UPDATE_UICC_FILE");
            flipped |= 619;
        }
        if ((o & 620) == 620) {
            list.add("RIL_REQUEST_HW_GET_UICC_FILE");
            flipped |= 620;
        }
        if ((o & 621) == 621) {
            list.add("RIL_REQUEST_HW_UICC_KS_NAF_AUTH");
            flipped |= 621;
        }
        if ((o & 622) == 622) {
            list.add("RIL_REQUEST_HW_INITIAL_MESSAGE");
            flipped |= 622;
        }
        if ((o & 623) == 623) {
            list.add("RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX");
            flipped |= 623;
        }
        if ((o & 624) == 624) {
            list.add("RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE");
            flipped |= 624;
        }
        if ((o & 625) == 625) {
            list.add("RIL_REQUEST_HW_GET_DS_FLOW_INFO");
            flipped |= 625;
        }
        if ((o & 626) == 626) {
            list.add("RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO");
            flipped |= 626;
        }
        if ((o & 627) == 627) {
            list.add("RIL_REQUEST_HW_SET_DS_FLOW_REPORT_FLAG");
            flipped |= 627;
        }
        if ((o & 628) == 628) {
            list.add("RIL_REQUEST_HW_GET_DEVICE_VERSION");
            flipped |= 628;
        }
        if ((o & 629) == 629) {
            list.add("RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG");
            flipped |= 629;
        }
        if ((o & 630) == 630) {
            list.add("RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD");
            flipped |= 630;
        }
        if ((o & 631) == 631) {
            list.add("RIL_REQUEST_HW_GET_SCI_CHG_CFG");
            flipped |= 631;
        }
        if ((o & 632) == 632) {
            list.add("RIL_REQUEST_HW_DSDS_GET_SIM_STATUS");
            flipped |= 632;
        }
        if ((o & 633) == 633) {
            list.add("RIL_REQUEST_HW_IMS_MODIFY_CALL_INITIATE");
            flipped |= 633;
        }
        if ((o & 634) == 634) {
            list.add("RIL_REQUEST_HW_IMS_MODIFY_CALL_CONFIRM");
            flipped |= 634;
        }
        if ((o & 635) == 635) {
            list.add("RIL_REQUEST_HW_SET_VOICEPREFER_STATUS");
            flipped |= 635;
        }
        if ((o & 636) == 636) {
            list.add("RIL_REQUEST_HW_GET_VOICEPREFER_STATUS");
            flipped |= 636;
        }
        if ((o & 637) == 637) {
            list.add("RIL_REQUEST_HW_IMS_ECONF_DIAL");
            flipped |= 637;
        }
        if ((o & 638) == 638) {
            list.add("RIL_REQUEST_HW_IMS_GET_ECONF_INFO");
            flipped |= 638;
        }
        if ((o & 639) == 639) {
            list.add("RIL_REQUEST_HW_IMS_ECONF_REMOVE_MEMBER");
            flipped |= 639;
        }
        if ((o & 640) == 640) {
            list.add("RIL_REQUEST_HW_IMS_MERGE_ECONF");
            flipped |= 640;
        }
        if ((o & 641) == 641) {
            list.add("RIL_REQUEST_HW_IMS_ECONF_ADD_MEMBER");
            flipped |= 641;
        }
        if ((o & 642) == 642) {
            list.add("RIL_REQUEST_HW_IMS_GET_ECONF_LAST_FAIL_CAUSE");
            flipped |= 642;
        }
        if ((o & 643) == 643) {
            list.add("RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH");
            flipped |= 643;
        }
        if ((o & 644) == 644) {
            list.add("RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION");
            flipped |= 644;
        }
        if ((o & 645) == 645) {
            list.add("RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION");
            flipped |= 645;
        }
        if ((o & 646) == 646) {
            list.add("RIL_REQUEST_HW_AP_SET_DS_FLOW_CONFIG");
            flipped |= 646;
        }
        if ((o & 647) == 647) {
            list.add("RIL_REQUEST_HW_VSIM_CHECK_CARD");
            flipped |= 647;
        }
        if ((o & 648) == 648) {
            list.add("RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA");
            flipped |= 648;
        }
        if ((o & 649) == 649) {
            list.add("RIL_REQUEST_GET_DS_FLOW_NV_WRITE_CFG_PARA");
            flipped |= 649;
        }
        if ((o & 650) == 650) {
            list.add("RIL_REQUEST_HW_SET_IMS_SWITCH");
            flipped |= 650;
        }
        if ((o & 651) == 651) {
            list.add("RIL_REQUEST_HW_GET_IMS_SWITCH");
            flipped |= 651;
        }
        if ((o & 652) == 652) {
            list.add("RIL_REQUEST_HW_ICC_PREF_APP_SWITCH");
            flipped |= 652;
        }
        if ((o & 653) == 653) {
            list.add("RIL_REQUEST_HW_GET_CIMSI");
            flipped |= 653;
        }
        if ((o & 654) == 654) {
            list.add("RIL_REQUEST_HW_SET_CDMA_MODE_SIDE");
            flipped |= 654;
        }
        if ((o & 655) == 655) {
            list.add("RIL_REQUEST_HW_SET_UE_OPERATION_MODE");
            flipped |= 655;
        }
        if ((o & 656) == 656) {
            list.add("RIL_REQUEST_HW_VSIM_POWER");
            flipped |= 656;
        }
        if ((o & 657) == 657) {
            list.add("RIL_REQUEST_HW_NOTIFY_CMODEM_STATUS");
            flipped |= 657;
        }
        if ((o & 658) == 658) {
            list.add("RIL_REQUEST_HW_GET_CTROAMINFO");
            flipped |= 658;
        }
        if ((o & 659) == 659) {
            list.add("RIL_REQUEST_HW_SET_CT_OOS_COUNT");
            flipped |= 659;
        }
        if ((o & 660) == 660) {
            list.add("RIL_REQUEST_HW_IMS_DOMAIN_CONFIG");
            flipped |= 660;
        }
        if ((o & 661) == 661) {
            list.add("RIL_REQUEST_HW_VOWIFI_IMSA_MSG");
            flipped |= 661;
        }
        if ((o & 662) == 662) {
            list.add("RIL_REQUEST_HW_GET_IMS_DOMAIN");
            flipped |= 662;
        }
        if ((o & 663) == 663) {
            list.add("RIL_REQUEST_HW_GET_CDMA_MODE_SIDE");
            flipped |= 663;
        }
        if ((o & 664) == 664) {
            list.add("RIL_REQUEST_HW_VOWIFI_UICC_AUTH");
            flipped |= 664;
        }
        if ((o & 665) == 665) {
            list.add("RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND");
            flipped |= 665;
        }
        if ((o & 666) == 666) {
            list.add("RIL_REQUEST_HW_SET_TIME");
            flipped |= 666;
        }
        if ((o & 667) == 667) {
            list.add("RIL_REQUEST_VSIM_BASEBAND_VERSION");
            flipped |= 667;
        }
        if ((o & 668) == 668) {
            list.add("RIL_REQUEST_HW_SET_VT_LTE_QUALITY_RPT_CFG");
            flipped |= 668;
        }
        if ((o & 669) == 669) {
            list.add("RIL_REQUEST_HW_IMS_REJ_CALL");
            flipped |= 669;
        }
        if ((o & 670) == 670) {
            list.add("RIL_REQUEST_HW_REJ_CALL");
            flipped |= 670;
        }
        if ((o & 671) == 671) {
            list.add("RIL_REQUEST_HW_IMS_CHANNEL_INFO");
            flipped |= 671;
        }
        if ((o & 672) == 672) {
            list.add("RIL_REQUEST_HW_CHANNEL_INFO");
            flipped |= 672;
        }
        if ((o & 673) == 673) {
            list.add("RIL_REQUEST_HW_GET_RAT_COMBINE_MODE");
            flipped |= 673;
        }
        if ((o & 674) == 674) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_PCSCF");
            flipped |= 674;
        }
        if ((o & 675) == 675) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_DYN");
            flipped |= 675;
        }
        if ((o & 676) == 676) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_TIMER");
            flipped |= 676;
        }
        if ((o & 677) == 677) {
            list.add("RIL_REQUEST_HW_IMS_SET_DM_SMS");
            flipped |= 677;
        }
        if ((o & 678) == 678) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_PCSCF");
            flipped |= 678;
        }
        if ((o & 679) == 679) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_TIMER");
            flipped |= 679;
        }
        if ((o & 680) == 680) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_DYN");
            flipped |= 680;
        }
        if ((o & 681) == 681) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_SMS");
            flipped |= 681;
        }
        if ((o & 682) == 682) {
            list.add("RIL_REQUEST_HW_IMS_GET_DM_USER");
            flipped |= 682;
        }
        if ((o & 683) == 683) {
            list.add("RIL_REQUEST_HW_IMS_SET_NICK_NAME");
            flipped |= 683;
        }
        if ((o & 684) == 684) {
            list.add("RIL_REQUEST_HW_IMS_GET_NICK_NAME");
            flipped |= 684;
        }
        if ((o & 685) == 685) {
            list.add("RIL_REQUEST_HW_IMS_BATTERY_STATUS");
            flipped |= 685;
        }
        if ((o & 686) == 686) {
            list.add("RIL_REQUEST_HW_IMS_REGISTER");
            flipped |= 686;
        }
        if ((o & 687) == 687) {
            list.add("RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY");
            flipped |= 687;
        }
        if ((o & 688) == 688) {
            list.add("RIL_REQUEST_HW_SET_PSEUDO_INFO");
            flipped |= 688;
        }
        if ((o & 689) == 689) {
            list.add("RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY");
            flipped |= 689;
        }
        if ((o & 690) == 690) {
            list.add("RIL_REQUEST_HW_SIGNAL_STRENGTH");
            flipped |= 690;
        }
        if ((o & 691) == 691) {
            list.add("RIL_REQUEST_HW_QUERY_AVAILABLE_CSGID");
            flipped |= 691;
        }
        if ((o & 692) == 692) {
            list.add("RIL_REQUEST_HW_SELECT_CSGID");
            flipped |= 692;
        }
        if ((o & 693) == 693) {
            list.add("RIL_REQUEST_HW_IMS_LAST_CALL_CAUSE");
            flipped |= 693;
        }
        if ((o & 694) == 694) {
            list.add("RIL_REQUEST_HW_SET_IMSVT_CAPABILITY");
            flipped |= 694;
        }
        if ((o & 695) == 695) {
            list.add("RIL_REQUEST_HW_SET_WIFI_EMERGENCY_AID");
            flipped |= 695;
        }
        if ((o & 696) == 696) {
            list.add("RIL_REQUEST_HW_SEND_LAA_CMD");
            flipped |= 696;
        }
        if ((o & 697) == 697) {
            list.add("RIL_REQUEST_HW_SETUP_DATA_CALL_EMERGENCY");
            flipped |= 697;
        }
        if ((o & 698) == 698) {
            list.add("RIL_REQUEST_HW_DEACTIVATE_DATA_CALL_EMERGENCY");
            flipped |= 698;
        }
        if ((o & 699) == 699) {
            list.add("RIL_REQUEST_HW_GET_CELL_INFO_LIST_OTDOA");
            flipped |= 699;
        }
        if ((o & 700) == 700) {
            list.add("RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE");
            flipped |= 700;
        }
        if ((o & 701) == 701) {
            list.add("RIL_REQUEST_HW_GET_LAA_STATE");
            flipped |= 701;
        }
        if ((o & 702) == 702) {
            list.add("RIL_REQUEST_HW_SET_RCS_SWITCH");
            flipped |= 702;
        }
        if ((o & 703) == 703) {
            list.add("RIL_REQUEST_HW_GET_RCS_SWITCH_STATE");
            flipped |= 703;
        }
        if ((o & 704) == 704) {
            list.add("RIL_REQUEST_HW_SET_DM_RCS_CFG");
            flipped |= 704;
        }
        if ((o & 705) == 705) {
            list.add("RIL_REQUEST_HW_EXCHANGE_MODEM_INFO");
            flipped |= 705;
        }
        if ((o & 706) == 706) {
            list.add("RIL_REQUEST_HW_INFORM_MODEM_TETHER_STATUS");
            flipped |= 706;
        }
        if ((o & 707) == 707) {
            list.add("RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_WITH_IMS_DOMAIN");
            flipped |= 707;
        }
        if ((o & 708) == 708) {
            list.add("RIL_REQUEST_HW_VOLTE_IMPI");
            flipped |= 708;
        }
        if ((o & 709) == 709) {
            list.add("RIL_REQUEST_HW_VOLTE_DOMAIN");
            flipped |= 709;
        }
        if ((o & 710) == 710) {
            list.add("RIL_REQUEST_CANCEL_IMS_VIDEO_CALL");
            flipped |= 710;
        }
        if ((o & 711) == 711) {
            list.add("RIL_REQUEST_HW_SET_DATA_SWITCH");
            flipped |= 711;
        }
        if ((o & 712) == 712) {
            list.add("RIL_REQUEST_HW_SET_DATA_ROAM_SWITCH");
            flipped |= 712;
        }
        if ((o & 713) == 713) {
            list.add("RIL_REQUEST_HW_SET_IMS_SMS_CONFIG");
            flipped |= 713;
        }
        if ((o & 714) == 714) {
            list.add("RIL_REQUEST_HW_GET_IMS_SMS_CONFIG");
            flipped |= 714;
        }
        if ((o & 715) == 715) {
            list.add("RIL_REQUEST_HW_GET_CSGIDSRCH_INFO");
            flipped |= 715;
        }
        if ((o & 716) == 716) {
            list.add("RIL_REQUEST_HW_SET_REATTACH_REQUIRED_UNSOL");
            flipped |= 716;
        }
        if ((o & 717) == 717) {
            list.add("RIL_REQUEST_HW_SET_DEEP_NO_DISTURB_SWITCH");
            flipped |= 717;
        }
        if ((o & 718) == 718) {
            list.add("RIL_REQUEST_HW_ACTIVE_NCFG_BIN");
            flipped |= 718;
        }
        if ((o & 719) == 719) {
            list.add("RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_V1_2");
            flipped |= 719;
        }
        if ((o & 720) == 720) {
            list.add("RIL_REQUEST_HW_GET_CURRENT_CALLS_V1_2");
            flipped |= 720;
        }
        if ((o & 721) == 721) {
            list.add("RIL_REQUEST_HW_GET_NUM_OF_REC_PSE_BASE_STATION");
            flipped |= 721;
        }
        if ((o & 722) == 722) {
            list.add("RIL_REQUEST_HW_IMS_SET_RTT_STATUS");
            flipped |= 722;
        }
        if ((o & 723) == 723) {
            list.add("RIL_REQUEST_HW_IMS_ANSWER_V1_3");
            flipped |= 723;
        }
        if ((o & 724) == 724) {
            list.add("RIL_REQUEST_HW_IMS_DIAL_V1_3");
            flipped |= 724;
        }
        if ((o & 725) == 725) {
            list.add("RIL_REQUEST_HW_IMS_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE_WITH_TYPE_V1_3");
            flipped |= 725;
        }
        if ((o & 726) == 726) {
            list.add("RIL_REQUEST_HW_IMS_RTT_MODIFY");
            flipped |= 726;
        }
        if ((o & 727) == 727) {
            list.add("RIL_REQUEST_HW_IMS_GET_CURRENT_CALLS_V1_3");
            flipped |= 727;
        }
        if ((o & 728) == 728) {
            list.add("RIL_REQUEST_HW_SEND_NCFG_OPER_INFO");
            flipped |= 728;
        }
        if ((o & 729) == 729) {
            list.add("RIL_REQUEST_HW_IMS_RESTRAT_RILD");
            flipped |= 729;
        }
        if ((o & 730) == 730) {
            list.add("RIL_REQUEST_HW_IMS_UICC_AUTH");
            flipped |= 730;
        }
        if ((o & 731) == 731) {
            list.add("RIL_REQUEST_HW_GET_CAP_OF_REC_PSE_BASESTATION");
            flipped |= 731;
        }
        if ((o & 2000) == 2000) {
            list.add("RIL_UNSOL_HW_DEVICE_BASE");
            flipped |= 2000;
        }
        if ((o & 2000) == 2000) {
            list.add("RIL_UNSOL_HW_RESPONSE_SIM_TYPE");
            flipped |= 2000;
        }
        if ((o & 2001) == 2001) {
            list.add("RIL_UNSOL_HW_RESPONSE_IMS_NETWORK_STATE_CHANGED");
            flipped |= 2001;
        }
        if ((o & 2002) == 2002) {
            list.add("RIL_UNSOL_HW_RESPONSE_TETHERED_MODE_STATE_CHANGED");
            flipped |= 2002;
        }
        if ((o & 2003) == 2003) {
            list.add("RIL_UNSOL_HW_RESPONSE_DATA_NETWORK_STATE_CHANGED");
            flipped |= 2003;
        }
        if ((o & 2004) == 2004) {
            list.add("RIL_UNSOL_HW_ON_SS");
            flipped |= 2004;
        }
        if ((o & 2005) == 2005) {
            list.add("RIL_UNSOL_HW_STK_CC_ALPHA_NOTIFY");
            flipped |= 2005;
        }
        if ((o & 2006) == 2006) {
            list.add("RIL_UNSOL_HW_UICC_SUBSCRIPTION_STATUS_CHANGED");
            flipped |= 2006;
        }
        if ((o & 2007) == 2007) {
            list.add("RIL_UNSOL_HW_QOS_STATE_CHANGED_IND");
            flipped |= 2007;
        }
        if ((o & 2008) == 2008) {
            list.add("RIL_UNSOL_HW_MODIFY_CALL");
            flipped |= 2008;
        }
        if ((o & 2009) == 2009) {
            list.add("RIL_UNSOL_HW_DIALUP_STATE_CHANGED");
            flipped |= 2009;
        }
        if ((o & 2010) == 2010) {
            list.add("RIL_UNSOL_HW_CURR_MCC");
            flipped |= 2010;
        }
        if ((o & 2011) == 2011) {
            list.add("RIL_UNSOL_HW_CURR_GSM_STATE");
            flipped |= 2011;
        }
        if ((o & 2012) == 2012) {
            list.add("RIL_UNSOL_HW_RESPONSE_SIMLOCK_STATUS_CHANGED");
            flipped |= 2012;
        }
        if ((o & 2013) == 2013) {
            list.add("RIL_UNSOL_HW_CG_SWITCH_RECOVERY");
            flipped |= 2013;
        }
        if ((o & 2014) == 2014) {
            list.add("RIL_UNSOL_HW_TETHERED_MODE_STATE_CHANGED");
            flipped |= 2014;
        }
        if ((o & 2015) == 2015) {
            list.add("RIL_UNSOL_HW_TRIGGER_SETUP_DATA_CALL");
            flipped |= 2015;
        }
        if ((o & 2016) == 2016) {
            list.add("RIL_UNSOL_HW_SIMSLOT_CFG");
            flipped |= 2016;
        }
        if ((o & 2017) == 2017) {
            list.add("RIL_UNSOL_HW_RIL_CHR_IND");
            flipped |= 2017;
        }
        if ((o & 2018) == 2018) {
            list.add("RIL_UNSOL_HW_RESET_CHR_IND");
            flipped |= 2018;
        }
        if ((o & 2019) == 2019) {
            list.add("RIL_UNSOL_HW_SIM_HOTPLUG");
            flipped |= 2019;
        }
        if ((o & 2020) == 2020) {
            list.add("RIL_UNSOL_HW_SIM_ICCID_CHANGED");
            flipped |= 2020;
        }
        if ((o & 2021) == 2021) {
            list.add("RIL_UNSOL_HW_RESIDENT_NETWORK_CHANGED");
            flipped |= 2021;
        }
        if ((o & 2022) == 2022) {
            list.add("RIL_UNSOL_HW_SIM_PNP");
            flipped |= 2022;
        }
        if ((o & 2023) == 2023) {
            list.add("RIL_UNSOL_HW_CS_CHANNEL_INFO_IND");
            flipped |= 2023;
        }
        if ((o & 2024) == 2024) {
            list.add("RIL_UNSOL_HW_NCELL_MONITOR");
            flipped |= 2024;
        }
        if ((o & 2025) == 2025) {
            list.add("RIL_UNSOL_HW_ECCNUM");
            flipped |= 2025;
        }
        if ((o & 2026) == 2026) {
            list.add("RIL_UNSOL_HW_NETWORK_REJECT_CASE");
            flipped |= 2026;
        }
        if ((o & 2027) == 2027) {
            list.add("RIL_UNSOL_HW_VSIM_RDH_REQUEST");
            flipped |= 2027;
        }
        if ((o & 2028) == 2028) {
            list.add("RIL_UNSOL_HW_VSIM_OTA_SMS_REPORT");
            flipped |= 2028;
        }
        if ((o & 2029) == 2029) {
            list.add("RIL_UNSOL_HW_SVLTE_PS_TRANSFER_IND");
            flipped |= 2029;
        }
        if ((o & 2030) == 2030) {
            list.add("RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND");
            flipped |= 2030;
        }
        if ((o & 2031) == 2031) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_CALL_STATE_CHANGED");
            flipped |= 2031;
        }
        if ((o & 2032) == 2032) {
            list.add("RIL_UNSOL_HW_IMS_CALL_RING");
            flipped |= 2032;
        }
        if ((o & 2033) == 2033) {
            list.add("RIL_UNSOL_HW_IMS_RINGBACK_TONE");
            flipped |= 2033;
        }
        if ((o & 2034) == 2034) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_HANDOVER");
            flipped |= 2034;
        }
        if ((o & 2035) == 2035) {
            list.add("RIL_UNSOL_HW_IMS_SRV_STATUS_UPDATE");
            flipped |= 2035;
        }
        if ((o & 2036) == 2036) {
            list.add("RIL_UNSOL_HW_IMS_ON_USSD");
            flipped |= 2036;
        }
        if ((o & 2037) == 2037) {
            list.add("RIL_UNSOL_HW_IMS_ON_SS");
            flipped |= 2037;
        }
        if ((o & 2038) == 2038) {
            list.add("RIL_UNSOL_HW_IMS_SUPP_SVC_NOTIFICATION");
            flipped |= 2038;
        }
        if ((o & 2039) == 2039) {
            list.add("RIL_UNSOL_HW_IMS_VOICE_BAND_INFO");
            flipped |= 2039;
        }
        if ((o & 2040) == 2040) {
            list.add("RIL_UNSOL_HW_UIM_LOCKCARD");
            flipped |= 2040;
        }
        if ((o & 2041) == 2041) {
            list.add("RIL_UNSOL_HW_MIPICLK");
            flipped |= 2041;
        }
        if ((o & 2042) == 2042) {
            list.add("RIL_UNSOL_HW_APR_SVLTE_IND");
            flipped |= 2042;
        }
        if ((o & 2043) == 2043) {
            list.add("RIL_UNSOL_HW_BALONG_MODEM_RESET_EVENT");
            flipped |= 2043;
        }
        if ((o & 2044) == 2044) {
            list.add("RIL_UNSOL_HW_PROXY_INIT_STATE_CHANGED");
            flipped |= 2044;
        }
        if ((o & 2045) == 2045) {
            list.add("RIL_UNSOL_HW_LTE_FREQ_WITH_WLAN_COEX");
            flipped |= 2045;
        }
        if ((o & 2046) == 2046) {
            list.add("RIL_UNSOL_HW_DS_FLOW_INFO_REPORT");
            flipped |= 2046;
        }
        if ((o & 2047) == 2047) {
            list.add("RIL_UNSOL_HW_TEE_TASK_TIME_OUT");
            flipped |= 2047;
        }
        if ((o & 2048) == 2048) {
            list.add("RIL_UNSOL_HW_DSDS_SIM_STATUS_CHANGED");
            flipped |= 2048;
        }
        if ((o & 2049) == 2049) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_IND");
            flipped |= 2049;
        }
        if ((o & 2050) == 2050) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_MODIFY_END_CAUSE");
            flipped |= 2050;
        }
        if ((o & 2051) == 2051) {
            list.add("RIL_UNSOL_HW_XPASS_INFO_RPT");
            flipped |= 2051;
        }
        if ((o & 2052) == 2052) {
            list.add("RIL_UNSOL_HOOK_HW_VP_STATUS");
            flipped |= 2052;
        }
        if ((o & 2053) == 2053) {
            list.add("RIL_UNSOL_HW_IMS_ECONF_STATE_CHANGED");
            flipped |= 2053;
        }
        if ((o & 2054) == 2054) {
            list.add("RIL_UNSOL_HW_EXIST_NETWORK_INFO");
            flipped |= 2054;
        }
        if ((o & 2055) == 2055) {
            list.add("RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT");
            flipped |= 2055;
        }
        if ((o & 2056) == 2056) {
            list.add("RIL_UNSOL_HW_IMS_MT_STATUS_REPORT");
            flipped |= 2056;
        }
        if ((o & 2057) == 2057) {
            list.add("RIL_UNSOL_HW_CA_STATE_CHANGED");
            flipped |= 2057;
        }
        if ((o & 2058) == 2058) {
            list.add("RIL_UNSOL_HW_IMS_CS_REDIAL_NOTIFY");
            flipped |= 2058;
        }
        if ((o & 2059) == 2059) {
            list.add("RIL_UNSOL_HW_CTROAMINFO_CHANGED");
            flipped |= 2059;
        }
        if ((o & 2060) == 2060) {
            list.add("RIL_UNSOL_HW_CDMA_HPLMN_UPDATE");
            flipped |= 2060;
        }
        if ((o & 2061) == 2061) {
            list.add("RIL_UNSOL_HW_IMSA_VOWIFI_MSG");
            flipped |= 2061;
        }
        if ((o & 2062) == 2062) {
            list.add("RIL_UNSOL_HW_IMS_DATA_CONNECT_IND");
            flipped |= 2062;
        }
        if ((o & 2063) == 2063) {
            list.add("RIL_UNSOL_HW_IMS_DATA_DISCONNECT_IND");
            flipped |= 2063;
        }
        if ((o & 2064) == 2064) {
            list.add("RIL_UNSOL_HW_ROAMING_MODE_SWITCH");
            flipped |= 2064;
        }
        if ((o & 2065) == 2065) {
            list.add("RIL_UNSOL_HW_LTE_PDCP_INFO");
            flipped |= 2065;
        }
        if ((o & 2066) == 2066) {
            list.add("RIL_UNSOL_HW_LTE_RRC_INFO");
            flipped |= 2066;
        }
        if ((o & 2067) == 2067) {
            list.add("RIL_UNSOL_HW_CRR_CONN_IND");
            flipped |= 2067;
        }
        if ((o & 2068) == 2068) {
            list.add("RIL_UNSOL_HW_RAT_COMBINE_MODE_IND");
            flipped |= 2068;
        }
        if ((o & 2069) == 2069) {
            list.add("RIL_UNSOL_HW_SIM_SWITCH");
            flipped |= 2069;
        }
        if ((o & 2070) == 2070) {
            list.add("RIL_UNSOL_HW_INIT_LOCINFO");
            flipped |= 2070;
        }
        if ((o & 2071) == 2071) {
            list.add("RIL_UNSOL_HW_LIMIT_PDP_ACT_IND");
            flipped |= 2071;
        }
        if ((o & 2072) == 2072) {
            list.add("RIL_UNSOL_HW_IMS_DMCN");
            flipped |= 2072;
        }
        if ((o & 2073) == 2073) {
            list.add("RIL_UNSOL_HW_IMS_REG_FAILED_INFO");
            flipped |= 2073;
        }
        if ((o & 2074) == 2074) {
            list.add("RIL_UNSOL_HW_IMS_HOLD_TONE_IND");
            flipped |= 2074;
        }
        if ((o & 2075) == 2075) {
            list.add("RIL_UNSOL_HW_MCC_CHANGE");
            flipped |= 2075;
        }
        if ((o & 2076) == 2076) {
            list.add("RIL_UNSOL_RSRVCC_STATE_NOTIFY");
            flipped |= 2076;
        }
        if ((o & 2077) == 2077) {
            list.add("RIL_UNSOL_HW_SIGNAL_STRENGTH");
            flipped |= 2077;
        }
        if ((o & 2078) == 2078) {
            list.add("RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH");
            flipped |= 2078;
        }
        if ((o & 2079) == 2079) {
            list.add("RIL_UNSOL_HW_VT_FLOW_INFO_REPORT");
            flipped |= 2079;
        }
        if ((o & 2080) == 2080) {
            list.add("RIL_UNSOL_HW_LAA_STATE");
            flipped |= 2080;
        }
        if ((o & 2081) == 2081) {
            list.add("RIL_UNSOL_HW_CALL_ALT_SRV");
            flipped |= 2081;
        }
        if ((o & 2082) == 2082) {
            list.add("RIL_UNSOL_HW_ICCID_CHANGED_IND");
            flipped |= 2082;
        }
        if ((o & 2083) == 2083) {
            list.add("RIL_UNSOL_HW_IMS_REG_TYPE_IND");
            flipped |= 2083;
        }
        if ((o & 2084) == 2084) {
            list.add("RIL_UNSOL_HW_SYS_INFO_IND");
            flipped |= 2084;
        }
        if ((o & 2085) == 2085) {
            list.add("RIL_UNSOL_HW_HPLMN_IND");
            flipped |= 2085;
        }
        if ((o & 2086) == 2086) {
            list.add("RIL_UNSOL_HW_DSDS_MODE_STATE_IND");
            flipped |= 2086;
        }
        if ((o & 2087) == 2087) {
            list.add("RIL_UNSOL_HW_LOW_TEMPRATURE");
            flipped |= 2087;
        }
        if ((o & 2088) == 2088) {
            list.add("RIL_UNSOL_HW_MIMO_STATE_IND");
            flipped |= 2088;
        }
        if ((o & 2089) == 2089) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_RTT_ERR");
            flipped |= 2089;
        }
        if ((o & 2090) == 2090) {
            list.add("RIL_UNSOL_HW_IMS_RESPONSE_RTT_EVENT");
            flipped |= 2090;
        }
        if ((o & 2091) == 2091) {
            list.add("RIL_UNSOL_HW_NCFG_FINISHED_IND");
            flipped |= 2091;
        }
        if ((o & 2092) == 2092) {
            list.add("RIL_UNSOL_HW_REC_PSE_BASESTATION_REPORT");
            flipped |= 2092;
        }
        if ((o & 732) == 732) {
            list.add("RIL_REQUEST_HW_SET_ULFREQ_ENABLE");
            flipped |= 732;
        }
        if ((o & 2093) == 2093) {
            list.add("RIL_UNSOL_HW_ULFREQ");
            flipped |= 2093;
        }
        if ((o & 735) == 735) {
            list.add("RIL_REQUEST_HW_SEND_MUTI_CHIP_SESSION_CONFIG");
            flipped |= 735;
        }
        if ((o & 736) == 736) {
            list.add("RIL_REQUEST_HW_SEND_VSIM_DATA_TO_MODEM");
            flipped |= 736;
        }
        if ((o & 2096) == 2096) {
            list.add("RIL_UNSOL_HW_DL_256QAM_STATE_IND");
            flipped |= 2096;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

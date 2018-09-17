package com.android.internal.telephony;

public class DctConstants {
    public static final int APN_BIP0_ID = 12;
    public static final int APN_BIP1_ID = 13;
    public static final int APN_BIP2_ID = 14;
    public static final int APN_BIP3_ID = 15;
    public static final int APN_BIP4_ID = 16;
    public static final int APN_BIP5_ID = 17;
    public static final int APN_BIP6_ID = 18;
    public static final int APN_CBS_ID = 7;
    public static final int APN_DEFAULT_ID = 0;
    public static final int APN_DUN_ID = 3;
    public static final int APN_EMERGENCY_ID = 9;
    public static final int APN_FOTA_ID = 6;
    public static final int APN_HIPRI_ID = 4;
    public static final int APN_IA_ID = 8;
    public static final int APN_IMS_ID = 5;
    public static final int APN_INTERNALDEFAULT_ID = 20;
    public static final int APN_INVALID_ID = -1;
    public static final int APN_MAX_ID = 20;
    public static final int APN_MMS_ID = 1;
    public static final int APN_NUM_TYPES = 21;
    public static final int APN_SUPL_ID = 2;
    public static final String APN_TYPE_KEY = "apnType";
    public static final int APN_XCAP_ID = 19;
    public static final int BASE = 270336;
    protected static final int BASE_HW = 271136;
    public static final int CMD_CLEAR_PROVISIONING_SPINNER = 270378;
    public static final int CMD_ENABLE_MOBILE_PROVISIONING = 270373;
    public static final int CMD_IS_PROVISIONING_APN = 270374;
    public static final int CMD_NET_STAT_POLL = 270376;
    public static final int CMD_SET_DEPENDENCY_MET = 270367;
    public static final int CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA = 270372;
    public static final int CMD_SET_POLICY_DATA_ENABLE = 270368;
    public static final int CMD_SET_USER_DATA_ENABLE = 270366;
    public static final int DISABLED = 0;
    public static final int ENABLED = 1;
    public static final int EVENT_APN_CHANGED = 270355;
    public static final int EVENT_CDMA_DATA_DETACHED = 270356;
    public static final int EVENT_CDMA_OTA_PROVISION = 270361;
    public static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 270357;
    public static final int EVENT_CLEAN_UP_ALL_CONNECTIONS = 270365;
    public static final int EVENT_CLEAN_UP_CONNECTION = 270360;
    public static final int EVENT_DATA_CONNECTION_ATTACHED = 270352;
    public static final int EVENT_DATA_CONNECTION_DETACHED = 270345;
    public static final int EVENT_DATA_RAT_CHANGED = 270377;
    public static final int EVENT_DATA_SETUP_COMPLETE = 270336;
    public static final int EVENT_DATA_SETUP_COMPLETE_ERROR = 270371;
    public static final int EVENT_DATA_SETUP_COMPLETE_FAILED = 271138;
    public static final int EVENT_DATA_STALL_ALARM = 270353;
    public static final int EVENT_DATA_STATE_CHANGED = 270340;
    public static final int EVENT_DEVICE_PROVISIONED_CHANGE = 270379;
    public static final int EVENT_DISCONNECT_DC_CONNECTING = 271141;
    public static final int EVENT_DISCONNECT_DC_RETRYING = 270370;
    public static final int EVENT_DISCONNECT_DONE = 270351;
    public static final int EVENT_DO_RECOVERY = 270354;
    public static final int EVENT_ENABLE_NEW_APN = 270349;
    public static final int EVENT_ICC_CHANGED = 270369;
    public static final int EVENT_IMSI_READY = 271144;
    public static final int EVENT_LINK_STATE_CHANGED = 270346;
    public static final int EVENT_OTA_APN_ATTACHED_FAILED = 270383;
    public static final int EVENT_PCO_DATA_RECEIVED = 270381;
    public static final int EVENT_PDP_RESET_ALARM = 271139;
    public static final int EVENT_PHONE_RADIO_ON = 271142;
    public static final int EVENT_POLL_PDP = 270341;
    public static final int EVENT_PROVISIONING_APN_ALARM = 270375;
    public static final int EVENT_PS_RESTRICT_DISABLED = 270359;
    public static final int EVENT_PS_RESTRICT_ENABLED = 270358;
    public static final int EVENT_RADIO_AVAILABLE = 270337;
    public static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 270342;
    public static final int EVENT_RAT_CHANGED = 271143;
    public static final int EVENT_RECORDS_LOADED = 270338;
    public static final int EVENT_REDIRECTION_DETECTED = 270380;
    public static final int EVENT_RESET_ALL_CONNECTIONS = 271137;
    public static final int EVENT_RESET_DONE = 270364;
    public static final int EVENT_RESTART_RADIO = 270362;
    public static final int EVENT_RESTORE_DEFAULT_APN = 270350;
    public static final int EVENT_ROAMING_OFF = 270348;
    public static final int EVENT_ROAMING_ON = 270347;
    public static final int EVENT_SET_CARRIER_DATA_ENABLED = 270382;
    public static final int EVENT_SET_INTERNAL_DATA_ENABLE = 270363;
    public static final int EVENT_TRY_SETUP_DATA = 270339;
    public static final int EVENT_VOICE_CALL_ENDED = 270344;
    public static final int EVENT_VOICE_CALL_STARTED = 270343;
    public static final int EVENT_VP_STATUS_CHANGED = 271140;
    public static final int INVALID = -1;
    public static final String PROVISIONING_URL_KEY = "provisioningUrl";

    public enum Activity {
        NONE,
        DATAIN,
        DATAOUT,
        DATAINANDOUT,
        DORMANT
    }

    public enum State {
        IDLE,
        CONNECTING,
        SCANNING,
        CONNECTED,
        DISCONNECTING,
        FAILED,
        RETRYING
    }
}

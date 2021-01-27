package ohos.dmsdp.sdk;

public class DMSDPConfig {
    public static final int ALGORITHM_AES128_CTR = 2;
    public static final int ALGORITHM_AES128_GCM = 1;
    public static final int ALGORITHM_NO = 0;
    public static final int BASE = 1;
    public static final int BUSINESSID_HICAR = 1;
    public static final int BUSINESSID_LOCAL_VIRTUAL_DEVICE = 5;
    public static final int BUSINESSID_PAD = 3;
    public static final int BUSINESSID_PC = 2;
    public static final int BUSINESSID_RESERVED = 255;
    public static final int BUSINESSID_TV_HW = 7;
    public static final int BUSINESSID_VIDEOCALL = 4;
    public static final int BUSINESSID_VIDEOCALL_HW = 6;
    public static final int CAMERA_ENABLE_TYPE_SWITCH = 1;
    public static final int CAMERA_ENABLE_TYPE_VIRTUAL = 2;
    public static final int CHANNEL_TYPE_HILINK = 9;
    public static final int CHANNEL_TYPE_HIWEAR = 11;
    public static final int CHANNEL_TYPE_LOCALAPP = 8;
    public static final int CHANNEL_TYPE_MIRACAST_P2P = 12;
    public static final int CHANNEL_TYPE_MSDP_BR = 6;
    public static final int CHANNEL_TYPE_MSDP_P2P = 7;
    public static final int CHANNEL_TYPE_NEARBY_BR = 3;
    public static final int CHANNEL_TYPE_NEARBY_P2P = 4;
    public static final int CHANNEL_TYPE_NEARBY_USB = 5;
    public static final int CHANNEL_TYPE_NEARFIELD = 10;
    public static final int CHANNEL_TYPE_NO_SOCKET_IP = 2;
    public static final int CHANNEL_TYPE_SOCKET_IP = 1;
    public static final int CHANNEL_TYPE_SOFT_BUS = 14;
    public static final int CONNECT_DEVICE_FAILED = 7004;
    public static final int CONNECT_DEVICE_TIMEOUT = 7003;
    public static final int DATA_TYPE_AIRSHARING_DISPLAY_CHANGE = 21;
    public static final int DATA_TYPE_AIRSHARING_HISIGHT_EXT_WINI_NFO = 9;
    public static final int DATA_TYPE_AIRSHARING_INPUT = 8;
    public static final int DATA_TYPE_AIRSHARING_INPUT_FOUCS = 14;
    public static final int DATA_TYPE_AIRSHARING_PORT = 7;
    public static final int DATA_TYPE_AUDIO_NET_UNSTABLE = 12;
    public static final int DATA_TYPE_CAR_CTRL_CMD = 1;
    public static final int DATA_TYPE_CAR_HARDWARE_MANAGER = 17;
    public static final int DATA_TYPE_DEVICE_POLICY_EVENT = 16;
    public static final int DATA_TYPE_DISPLAY_NET_UNSTABLE = 13;
    public static final int DATA_TYPE_HISIGHT_KEY_EVENT = 5;
    public static final int DATA_TYPE_HISIGHT_MOTION_EVENT = 4;
    public static final int DATA_TYPE_HOTWORD = 2;
    public static final int DATA_TYPE_INPUT_DEVICE_EVNET = 11;
    public static final int DATA_TYPE_KEY_EVENT = 3;
    public static final int DATA_TYPE_MSDP_PORT = 6;
    public static final int DATA_TYPE_REMOTE_DEVICE = 26;
    public static final int DATA_TYPE_SCREEN_EVENT = 15;
    public static final int DATA_TYPE_TV_NETWORK_WEAK = 20;
    public static final int DATA_TYPE_UPGRADE_DATA = 18;
    public static final int DATA_TYPE_VIRTUAL_KEY_EVENT = 10;
    public static final int DEVICE_BUSY = 7002;
    public static final int DEVICE_CONNECTION_PERMISSION_INEXISTENCE = -1;
    public static final int DEVICE_CONNECTION_PERMISSION_PERMANENT = 1;
    public static final int DEVICE_CONNECTION_PERMISSION_TEMPORARY = 0;
    public static final int DEVICE_EVENT_AUDIO_LATENCY = 3;
    public static final int DEVICE_EVENT_AUDIO_MUTE = 2;
    public static final int DEVICE_EVENT_DISCONNECT = 1;
    public static final int DEVICE_EVENT_SCREEN_OFF = 5;
    public static final int DEVICE_EVENT_SCREEN_ON = 4;
    public static final int DEVICE_SERVICE_STATUS_BUSY = 2;
    public static final int DEVICE_SERVICE_STATUS_EXCEPTION = 3;
    public static final int DEVICE_SERVICE_STATUS_IDLE = 1;
    public static final int DEVICE_SERVICE_TYPE_A2DP = 128;
    public static final int DEVICE_SERVICE_TYPE_ALL = 409599;
    public static final int DEVICE_SERVICE_TYPE_BUTTON = 32;
    public static final int DEVICE_SERVICE_TYPE_CAMERA = 1;
    public static final int DEVICE_SERVICE_TYPE_DISPLAY = 8;
    public static final int DEVICE_SERVICE_TYPE_GPS = 16;
    public static final int DEVICE_SERVICE_TYPE_HFP = 64;
    public static final int DEVICE_SERVICE_TYPE_MIC = 2;
    public static final int DEVICE_SERVICE_TYPE_NOTIFICATION = 8192;
    public static final int DEVICE_SERVICE_TYPE_PURE_AUDIO = 262144;
    public static final int DEVICE_SERVICE_TYPE_PURE_VIDEO = 131072;
    public static final int DEVICE_SERVICE_TYPE_SENSOR = 2048;
    public static final int DEVICE_SERVICE_TYPE_SPEAKER = 4;
    public static final int DEVICE_SERVICE_TYPE_UNKNOWN = 0;
    public static final int DEVICE_SERVICE_TYPE_VIBRATE = 4096;
    public static final int DEVICE_SERVICE_TYPE_VIRMODEM = 1024;
    public static final int DEVICE_SERVICE_TYPE_VIRMODEM_MIC = 256;
    public static final int DEVICE_SERVICE_TYPE_VIRMODEM_SPEAKER = 512;
    public static final int DEVICE_TYPE_CAMERA = 5;
    public static final int DEVICE_TYPE_CAR = 4;
    public static final int DEVICE_TYPE_HIWEAR = 9;
    public static final int DEVICE_TYPE_PAD = 2;
    public static final int DEVICE_TYPE_PC = 7;
    public static final int DEVICE_TYPE_PC_MULTI_WIN = 8;
    public static final int DEVICE_TYPE_PHONE = 1;
    public static final int DEVICE_TYPE_TV = 3;
    public static final int DEVICE_TYPE_UNKNOWN = 0;
    public static final int DEVICE_TYPE_VOICEBOX = 6;
    public static final int DISADBLE_FAILED = 7101;
    public static final String DISCONNECT_TYPE = "DISCONNECT_TYPE";
    public static final int DISCONNECT_TYPE_ABNORMAL = 1;
    public static final int DISCONNECT_TYPE_EXCEPTION = 3;
    public static final int DISCONNECT_TYPE_MANUAL = 2;
    public static final int DISCOVER_DEVICE_FILTER_ALL = 255;
    public static final int DISCOVER_DEVICE_FILTER_CAMERA = 16;
    public static final int DISCOVER_DEVICE_FILTER_CAR = 8;
    public static final int DISCOVER_DEVICE_FILTER_NONE = 0;
    public static final int DISCOVER_DEVICE_FILTER_PAD = 2;
    public static final int DISCOVER_DEVICE_FILTER_PC = 64;
    public static final int DISCOVER_DEVICE_FILTER_PHONE = 1;
    public static final int DISCOVER_DEVICE_FILTER_TV = 4;
    public static final int DISCOVER_DEVICE_FILTER_VOICEBOX = 32;
    public static final int DISCOVER_DEVICE_FILTER_WEAR = 128;
    public static final int DISCOVER_PROTOCOL_ALL = 255;
    public static final int DISCOVER_PROTOCOL_BLE = 1;
    public static final int DISCOVER_PROTOCOL_BR = 2;
    public static final int DISCOVER_PROTOCOL_COAP = 64;
    public static final int DISCOVER_PROTOCOL_HILINK = 32;
    public static final int DISCOVER_PROTOCOL_HIWEAR = 128;
    public static final int DISCOVER_PROTOCOL_IPC_ONVIF = 8;
    public static final int DISCOVER_PROTOCOL_LOCALAPP = 16;
    public static final int DISCOVER_PROTOCOL_P2P = 4;
    public static final int DISCOVER_SERVICE_FILTER_ALL = 511;
    public static final int DISCOVER_SERVICE_FILTER_BUTTON = 32;
    public static final int DISCOVER_SERVICE_FILTER_CAMERA = 1;
    public static final int DISCOVER_SERVICE_FILTER_DISPLAY = 8;
    public static final int DISCOVER_SERVICE_FILTER_GPS = 16;
    public static final int DISCOVER_SERVICE_FILTER_MIC = 2;
    public static final int DISCOVER_SERVICE_FILTER_NONE = 0;
    public static final int DISCOVER_SERVICE_FILTER_NOTIFICATION = 256;
    public static final int DISCOVER_SERVICE_FILTER_SENSOR = 64;
    public static final int DISCOVER_SERVICE_FILTER_SPEAKER = 4;
    public static final int DISCOVER_SERVICE_FILTER_VIBRATE = 128;
    public static final int ERROR_CODE_ALREADY_CONNECTING = -13;
    public static final int ERROR_CODE_ALREADY_DISCONNECTING = -14;
    public static final int ERROR_CODE_ALREADY_REGISTER = -6;
    public static final int ERROR_CODE_BUSY = -5;
    public static final int ERROR_CODE_CONNECTED = -8;
    public static final int ERROR_CODE_DEVICE_NOT_SUPPORT = -17;
    public static final int ERROR_CODE_DISCONNECTED = -9;
    public static final int ERROR_CODE_FAILED = -1;
    public static final int ERROR_CODE_INVALID_ARGUMENT = -2;
    public static final int ERROR_CODE_NOT_CREATED = -15;
    public static final int ERROR_CODE_NOT_IMPLEMENT = -7;
    public static final int ERROR_CODE_NOT_REGISTER = -4;
    public static final int ERROR_CODE_NOT_SUPPORT_IN_SUPERCLASS = -16;
    public static final int ERROR_CODE_NO_PERMISSION = -11;
    public static final int ERROR_CODE_NO_SERVICE = -10;
    public static final int ERROR_CODE_REMOTE_EXCEPTION = -3;
    public static final int ERROR_CODE_SCREEN_OFF = -12;
    public static final int EVENT_DEVICE_ADV_START = 111;
    public static final int EVENT_DEVICE_ADV_STOP = 112;
    public static final int EVENT_DEVICE_CAMERA_SERVICE_CONFIG = 207;
    public static final int EVENT_DEVICE_CONNECT = 101;
    public static final int EVENT_DEVICE_CONNECT_FAILED = 103;
    public static final int EVENT_DEVICE_DISCONNECT = 102;
    public static final int EVENT_DEVICE_DISPLAY_SERVICE_PLAYING = 209;
    public static final int EVENT_DEVICE_DISPLAY_SERVICE_PLAY_FAILED = 210;
    public static final int EVENT_DEVICE_ERROR_PIN_INPUT = 108;
    public static final int EVENT_DEVICE_HICHAIN_AUTH_FAIL = 301;
    public static final int EVENT_DEVICE_HICHAIN_AUTH_SUCCESS = 302;
    public static final int EVENT_DEVICE_MIC_RELEASE = 115;
    public static final int EVENT_DEVICE_MIC_REQUEST = 114;
    public static final int EVENT_DEVICE_NETWORK_WEAK = 303;
    public static final int EVENT_DEVICE_PIN_AUTH_FAILED = 116;
    public static final int EVENT_DEVICE_PIN_SUCCESS = 109;
    public static final int EVENT_DEVICE_PORT_UPDATE = 106;
    public static final int EVENT_DEVICE_PROJECT_CONNECT = 104;
    public static final int EVENT_DEVICE_PROJECT_DISCONNECT = 105;
    public static final int EVENT_DEVICE_REQUEST_CONNECTION = 117;
    public static final int EVENT_DEVICE_SERVICE_ABNORMAL = 206;
    public static final int EVENT_DEVICE_SERVICE_BUSY = 223;
    public static final int EVENT_DEVICE_SERVICE_INVALID = 220;
    public static final int EVENT_DEVICE_SERVICE_LOST = 208;
    public static final int EVENT_DEVICE_SERVICE_PAUSE = 202;
    public static final int EVENT_DEVICE_SERVICE_READY = 221;
    public static final int EVENT_DEVICE_SERVICE_RESUME = 203;
    public static final int EVENT_DEVICE_SERVICE_RUNNING = 222;
    public static final int EVENT_DEVICE_SERVICE_START = 204;
    public static final int EVENT_DEVICE_SERVICE_STOP = 205;
    public static final int EVENT_DEVICE_SERVICE_UNKNOW = 200;
    public static final int EVENT_DEVICE_SERVICE_UPDATE = 201;
    public static final int EVENT_DEVICE_SERVICE_VIRMODEM_CALLING = 211;
    public static final int EVENT_DEVICE_SERVICE_VIRMODEM_HANG_UP = 212;
    public static final int EVENT_DEVICE_SHOW_PIN_CODE = 113;
    public static final int EVENT_DEVICE_SHOW_PIN_INPUT = 107;
    public static final int EVENT_REMOTE_ACTIVE_DISCONNECT = 110;
    public static final int HILINK_KEY_NEG_ERROR = 1000;
    public static final int HILINK_KEY_NEG_RST_FORMAT_ERROR = 1001;
    public static final int HILINK_PORT_NEG_ERROR = 1002;
    public static final int HILINK_PORT_NEG_RST_FORMAT_ERROR = 1003;
    public static final int INPUT_DEVICE_ADD = 1;
    public static final int INPUT_DEVICE_REMOVE = 0;
    public static final String LIST_TO_STRING_SPLIT = ";";
    public static final int PROPERTY_NONE = 0;
    public static final int PROPERTY_TYPE_BOOLEAN = 5;
    public static final int PROPERTY_TYPE_DOUBLE = 4;
    public static final int PROPERTY_TYPE_INT = 1;
    public static final int PROPERTY_TYPE_LONG = 3;
    public static final int PROPERTY_TYPE_STRING = 2;
    public static final String SPLIT = "#";
    public static final int SUCCESS = 0;
    public static final int VIRTUAL_KEY_ACTION_BACK = 0;
    public static final int VIRTUAL_KEY_ACTION_HOME = 1;
    public static final int VIRTUAL_KEY_ACTION_QUICK_SETTING = 3;
    public static final int VIRTUAL_KEY_ACTION_RECENT_APP = 2;

    public enum DeviceOri {
        LANDSCAPE,
        PORTRAIT,
        AUTOROTATION
    }

    public static class UserConfig {
        public static final String USER_CFG_DEVICE_SCREEN_ORI = "VIRCAM_DEVICE_SCREEN_ORI";
        public static final String USER_CFG_HDMI_SOURCE_INDEX = "HDMI_SOURCE_INDEX";
        public static final String USER_CFG_PHONE_SCREEN_ORI = "VIRCAM_PHONE_SCREEN_ORI";
        public static final String USER_CFG_VIRCAM_AUTO_ORIENTATION = "VIRCAM_AUTO_ORIENTATION";
        public static final String USER_CFG_VIRCAM_DO_MIRROR = "VIRCAM_DO_MIRROR";
        public static final String USER_CFG_VIRCAM_PROCESS_STRATGY = "VIRCAM_PROCESS_STRATGY";
        public static final String USER_CFG_VIRCAM_TYPE = "VIRCAM_TYPE";
    }

    public enum FrameProcessStratgy {
        FULL_CONTENT(0),
        MAX_SCREEN_FILL(1);
        
        private int key;

        private FrameProcessStratgy(int i) {
            this.key = i;
        }

        public int getKey() {
            return this.key;
        }
    }

    public enum VirCameraType {
        SWITCH_CAMERA(0, "SWITCH"),
        REGISTER_CAMERA(1, "VIRTUAL");
        
        private String enableType;
        private int preEnableType;

        private VirCameraType(int i, String str) {
            this.preEnableType = i;
            this.enableType = str;
        }

        public int getPreEnableType() {
            return this.preEnableType;
        }

        public String getEnableType() {
            return this.enableType;
        }
    }
}

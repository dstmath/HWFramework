package com.android.server.hdmi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final class Constants {
    static final int ABORT_CANNOT_PROVIDE_SOURCE = 2;
    static final int ABORT_INVALID_OPERAND = 3;
    static final int ABORT_NOT_IN_CORRECT_MODE = 1;
    static final int ABORT_NO_ERROR = -1;
    static final int ABORT_REFUSED = 4;
    static final int ABORT_UNABLE_TO_DETERMINE = 5;
    static final int ABORT_UNRECOGNIZED_OPCODE = 0;
    public static final int ADDR_AUDIO_SYSTEM = 5;
    public static final int ADDR_BROADCAST = 15;
    public static final int ADDR_INTERNAL = 0;
    public static final int ADDR_INVALID = -1;
    public static final int ADDR_PLAYBACK_1 = 4;
    public static final int ADDR_PLAYBACK_2 = 8;
    public static final int ADDR_PLAYBACK_3 = 11;
    public static final int ADDR_RECORDER_1 = 1;
    public static final int ADDR_RECORDER_2 = 2;
    public static final int ADDR_RECORDER_3 = 9;
    public static final int ADDR_RESERVED_1 = 12;
    public static final int ADDR_RESERVED_2 = 13;
    public static final int ADDR_SPECIFIC_USE = 14;
    public static final int ADDR_TUNER_1 = 3;
    public static final int ADDR_TUNER_2 = 6;
    public static final int ADDR_TUNER_3 = 7;
    public static final int ADDR_TUNER_4 = 10;
    public static final int ADDR_TV = 0;
    public static final int ADDR_UNREGISTERED = 15;
    static final int ALWAYS_SYSTEM_AUDIO_CONTROL_ON_POWER_ON = 0;
    static final int AUDIO_CODEC_AAC = 6;
    static final int AUDIO_CODEC_ATRAC = 8;
    static final int AUDIO_CODEC_DD = 2;
    static final int AUDIO_CODEC_DDP = 10;
    static final int AUDIO_CODEC_DST = 13;
    static final int AUDIO_CODEC_DTS = 7;
    static final int AUDIO_CODEC_DTSHD = 11;
    static final int AUDIO_CODEC_LPCM = 1;
    static final int AUDIO_CODEC_MAX = 15;
    static final int AUDIO_CODEC_MP3 = 4;
    static final int AUDIO_CODEC_MPEG1 = 3;
    static final int AUDIO_CODEC_MPEG2 = 5;
    static final int AUDIO_CODEC_NONE = 0;
    static final int AUDIO_CODEC_ONEBITAUDIO = 9;
    static final int AUDIO_CODEC_TRUEHD = 12;
    static final int AUDIO_CODEC_WMAPRO = 14;
    static final String AUDIO_DEVICE_ARC_IN = "ARC_IN";
    static final String AUDIO_DEVICE_SPDIF = "SPDIF";
    static final int CEC_SWITCH_ARC = 17;
    static final int CEC_SWITCH_AUX = 20;
    static final int CEC_SWITCH_BLUETOOTH = 18;
    static final int CEC_SWITCH_HDMI1 = 1;
    static final int CEC_SWITCH_HDMI2 = 2;
    static final int CEC_SWITCH_HDMI3 = 3;
    static final int CEC_SWITCH_HDMI4 = 4;
    static final int CEC_SWITCH_HDMI5 = 5;
    static final int CEC_SWITCH_HDMI6 = 6;
    static final int CEC_SWITCH_HDMI7 = 7;
    static final int CEC_SWITCH_HDMI8 = 8;
    static final int CEC_SWITCH_HOME = 0;
    static final int CEC_SWITCH_OPTICAL = 19;
    static final int CEC_SWITCH_PORT_MAX = 21;
    static final int DISABLED = 0;
    static final int ENABLED = 1;
    static final int FALSE = 0;
    static final int INVALID_PHYSICAL_ADDRESS = 65535;
    static final int INVALID_PORT_ID = -1;
    static final int MENU_STATE_ACTIVATED = 0;
    static final int MENU_STATE_DEACTIVATED = 1;
    static final int MESSAGE_ABORT = 255;
    static final int MESSAGE_ACTIVE_SOURCE = 130;
    static final int MESSAGE_CDC_MESSAGE = 248;
    static final int MESSAGE_CEC_VERSION = 158;
    static final int MESSAGE_CLEAR_ANALOG_TIMER = 51;
    static final int MESSAGE_CLEAR_DIGITAL_TIMER = 153;
    static final int MESSAGE_CLEAR_EXTERNAL_TIMER = 161;
    static final int MESSAGE_DECK_CONTROL = 66;
    static final int MESSAGE_DECK_STATUS = 27;
    static final int MESSAGE_DEVICE_VENDOR_ID = 135;
    static final int MESSAGE_FEATURE_ABORT = 0;
    static final int MESSAGE_GET_CEC_VERSION = 159;
    static final int MESSAGE_GET_MENU_LANGUAGE = 145;
    static final int MESSAGE_GIVE_AUDIO_STATUS = 113;
    static final int MESSAGE_GIVE_DECK_STATUS = 26;
    static final int MESSAGE_GIVE_DEVICE_POWER_STATUS = 143;
    static final int MESSAGE_GIVE_DEVICE_VENDOR_ID = 140;
    static final int MESSAGE_GIVE_OSD_NAME = 70;
    static final int MESSAGE_GIVE_PHYSICAL_ADDRESS = 131;
    static final int MESSAGE_GIVE_SYSTEM_AUDIO_MODE_STATUS = 125;
    static final int MESSAGE_GIVE_TUNER_DEVICE_STATUS = 8;
    static final int MESSAGE_IMAGE_VIEW_ON = 4;
    static final int MESSAGE_INACTIVE_SOURCE = 157;
    static final int MESSAGE_INITIATE_ARC = 192;
    static final int MESSAGE_MENU_REQUEST = 141;
    static final int MESSAGE_MENU_STATUS = 142;
    static final int MESSAGE_PLAY = 65;
    static final int MESSAGE_RECORD_OFF = 11;
    static final int MESSAGE_RECORD_ON = 9;
    static final int MESSAGE_RECORD_STATUS = 10;
    static final int MESSAGE_RECORD_TV_SCREEN = 15;
    static final int MESSAGE_REPORT_ARC_INITIATED = 193;
    static final int MESSAGE_REPORT_ARC_TERMINATED = 194;
    static final int MESSAGE_REPORT_AUDIO_STATUS = 122;
    static final int MESSAGE_REPORT_PHYSICAL_ADDRESS = 132;
    static final int MESSAGE_REPORT_POWER_STATUS = 144;
    static final int MESSAGE_REPORT_SHORT_AUDIO_DESCRIPTOR = 163;
    static final int MESSAGE_REQUEST_ACTIVE_SOURCE = 133;
    static final int MESSAGE_REQUEST_ARC_INITIATION = 195;
    static final int MESSAGE_REQUEST_ARC_TERMINATION = 196;
    static final int MESSAGE_REQUEST_SHORT_AUDIO_DESCRIPTOR = 164;
    static final int MESSAGE_ROUTING_CHANGE = 128;
    static final int MESSAGE_ROUTING_INFORMATION = 129;
    static final int MESSAGE_SELECT_ANALOG_SERVICE = 146;
    static final int MESSAGE_SELECT_DIGITAL_SERVICE = 147;
    static final int MESSAGE_SET_ANALOG_TIMER = 52;
    static final int MESSAGE_SET_AUDIO_RATE = 154;
    static final int MESSAGE_SET_DIGITAL_TIMER = 151;
    static final int MESSAGE_SET_EXTERNAL_TIMER = 162;
    static final int MESSAGE_SET_MENU_LANGUAGE = 50;
    static final int MESSAGE_SET_OSD_NAME = 71;
    static final int MESSAGE_SET_OSD_STRING = 100;
    static final int MESSAGE_SET_STREAM_PATH = 134;
    static final int MESSAGE_SET_SYSTEM_AUDIO_MODE = 114;
    static final int MESSAGE_SET_TIMER_PROGRAM_TITLE = 103;
    static final int MESSAGE_STANDBY = 54;
    static final int MESSAGE_SYSTEM_AUDIO_MODE_REQUEST = 112;
    static final int MESSAGE_SYSTEM_AUDIO_MODE_STATUS = 126;
    static final int MESSAGE_TERMINATE_ARC = 197;
    static final int MESSAGE_TEXT_VIEW_ON = 13;
    static final int MESSAGE_TIMER_CLEARED_STATUS = 67;
    static final int MESSAGE_TIMER_STATUS = 53;
    static final int MESSAGE_TUNER_DEVICE_STATUS = 7;
    static final int MESSAGE_TUNER_STEP_DECREMENT = 6;
    static final int MESSAGE_TUNER_STEP_INCREMENT = 5;
    static final int MESSAGE_USER_CONTROL_PRESSED = 68;
    static final int MESSAGE_USER_CONTROL_RELEASED = 69;
    static final int MESSAGE_VENDOR_COMMAND = 137;
    static final int MESSAGE_VENDOR_COMMAND_WITH_ID = 160;
    static final int MESSAGE_VENDOR_REMOTE_BUTTON_DOWN = 138;
    static final int MESSAGE_VENDOR_REMOTE_BUTTON_UP = 139;
    static final int NEVER_SYSTEM_AUDIO_CONTROL_ON_POWER_ON = 2;
    static final int OPTION_MHL_ENABLE = 103;
    static final int OPTION_MHL_INPUT_SWITCHING = 101;
    static final int OPTION_MHL_POWER_CHARGE = 102;
    static final int OPTION_MHL_SERVICE_CONTROL = 104;
    static final int PATH_INTERNAL = 0;
    static final int POLL_ITERATION_IN_ORDER = 65536;
    static final int POLL_ITERATION_REVERSE_ORDER = 131072;
    static final int POLL_ITERATION_STRATEGY_MASK = 196608;
    static final int POLL_STRATEGY_MASK = 3;
    static final int POLL_STRATEGY_REMOTES_DEVICES = 1;
    static final int POLL_STRATEGY_SYSTEM_AUDIO = 2;
    static final String PROPERTY_ARC_SUPPORT = "persist.sys.hdmi.property_arc_support";
    static final String PROPERTY_DEVICE_TYPE = "ro.hdmi.device_type";
    static final String PROPERTY_HDMI_CEC_NEVER_ASSIGN_LOGICAL_ADDRESSES = "ro.hdmi.property_hdmi_cec_never_assign_logical_addresses";
    static final String PROPERTY_HDMI_CEC_NEVER_CLAIM_PLAYBACK_LOGICAL_ADDRESS = "ro.hdmi.property_hdmi_cec_never_claim_playback_logical_address";
    static final String PROPERTY_KEEP_AWAKE = "persist.sys.hdmi.keep_awake";
    static final String PROPERTY_LAST_SYSTEM_AUDIO_CONTROL = "persist.sys.hdmi.last_system_audio_control";
    static final String PROPERTY_PREFERRED_ADDRESS_AUDIO_SYSTEM = "persist.sys.hdmi.addr.audiosystem";
    static final String PROPERTY_PREFERRED_ADDRESS_PLAYBACK = "persist.sys.hdmi.addr.playback";
    static final String PROPERTY_PREFERRED_ADDRESS_TV = "persist.sys.hdmi.addr.tv";
    static final String PROPERTY_SET_MENU_LANGUAGE = "ro.hdmi.set_menu_language";
    static final String PROPERTY_STRIP_AUDIO_TV_NO_SYSTEM_AUDIO = "persist.sys.hdmi.property_strip_audio_tv_no_system_audio";
    static final String PROPERTY_SYSTEM_AUDIO_CONTROL_ON_POWER_ON = "persist.sys.hdmi.system_audio_control_on_power_on";
    static final String PROPERTY_SYSTEM_AUDIO_DEVICE_ARC_PORT = "ro.hdmi.property_sytem_audio_device_arc_port";
    static final String PROPERTY_SYSTEM_AUDIO_MODE_AUDIO_PORT = "persist.sys.hdmi.property_sytem_audio_mode_audio_port";
    static final String PROPERTY_SYSTEM_AUDIO_MODE_MUTING_ENABLE = "ro.hdmi.property_system_audio_mode_muting_enable";
    static final String PROPERTY_WAKE_ON_HOTPLUG = "ro.hdmi.wake_on_hotplug";
    static final int RECORDING_TYPE_ANALOGUE_RF = 2;
    static final int RECORDING_TYPE_DIGITAL_RF = 1;
    static final int RECORDING_TYPE_EXTERNAL_PHYSICAL_ADDRESS = 3;
    static final int RECORDING_TYPE_OWN_SOURCE = 4;
    static final int ROUTING_PATH_TOP_MASK = 61440;
    static final int ROUTING_PATH_TOP_SHIFT = 12;
    static final int SYSTEM_AUDIO_STATUS_OFF = 0;
    static final int SYSTEM_AUDIO_STATUS_ON = 1;
    static final int TRUE = 1;
    static final int UNKNOWN_VENDOR_ID = 16777215;
    static final int UNKNOWN_VOLUME = -1;
    static final int USE_LAST_STATE_SYSTEM_AUDIO_CONTROL_ON_POWER_ON = 1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AudioCodec {
    }

    public @interface AudioDevice {
    }

    @interface LocalActivePort {
    }

    @interface SystemAudioControlOnPowerOn {
    }

    private Constants() {
    }
}

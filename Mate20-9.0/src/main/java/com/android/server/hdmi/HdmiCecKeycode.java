package com.android.server.hdmi;

import android.net.dhcp.DhcpPacket;
import android.net.util.NetworkConstants;
import com.android.server.lights.LightsManager;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.util.Arrays;
import libcore.util.EmptyArray;

final class HdmiCecKeycode {
    public static final int CEC_KEYCODE_ANGLE = 80;
    public static final int CEC_KEYCODE_BACKWARD = 76;
    public static final int CEC_KEYCODE_CHANNEL_DOWN = 49;
    public static final int CEC_KEYCODE_CHANNEL_UP = 48;
    public static final int CEC_KEYCODE_CLEAR = 44;
    public static final int CEC_KEYCODE_CONTENTS_MENU = 11;
    public static final int CEC_KEYCODE_DATA = 118;
    public static final int CEC_KEYCODE_DISPLAY_INFORMATION = 53;
    public static final int CEC_KEYCODE_DOT = 42;
    public static final int CEC_KEYCODE_DOWN = 2;
    public static final int CEC_KEYCODE_EJECT = 74;
    public static final int CEC_KEYCODE_ELECTRONIC_PROGRAM_GUIDE = 83;
    public static final int CEC_KEYCODE_ENTER = 43;
    public static final int CEC_KEYCODE_EXIT = 13;
    public static final int CEC_KEYCODE_F1_BLUE = 113;
    public static final int CEC_KEYCODE_F2_RED = 114;
    public static final int CEC_KEYCODE_F3_GREEN = 115;
    public static final int CEC_KEYCODE_F4_YELLOW = 116;
    public static final int CEC_KEYCODE_F5 = 117;
    public static final int CEC_KEYCODE_FAST_FORWARD = 73;
    public static final int CEC_KEYCODE_FAVORITE_MENU = 12;
    public static final int CEC_KEYCODE_FORWARD = 75;
    public static final int CEC_KEYCODE_HELP = 54;
    public static final int CEC_KEYCODE_INITIAL_CONFIGURATION = 85;
    public static final int CEC_KEYCODE_INPUT_SELECT = 52;
    public static final int CEC_KEYCODE_LEFT = 3;
    public static final int CEC_KEYCODE_LEFT_DOWN = 8;
    public static final int CEC_KEYCODE_LEFT_UP = 7;
    public static final int CEC_KEYCODE_MEDIA_CONTEXT_SENSITIVE_MENU = 17;
    public static final int CEC_KEYCODE_MEDIA_TOP_MENU = 16;
    public static final int CEC_KEYCODE_MUTE = 67;
    public static final int CEC_KEYCODE_MUTE_FUNCTION = 101;
    public static final int CEC_KEYCODE_NEXT_FAVORITE = 47;
    public static final int CEC_KEYCODE_NUMBERS_1 = 33;
    public static final int CEC_KEYCODE_NUMBERS_2 = 34;
    public static final int CEC_KEYCODE_NUMBERS_3 = 35;
    public static final int CEC_KEYCODE_NUMBERS_4 = 36;
    public static final int CEC_KEYCODE_NUMBERS_5 = 37;
    public static final int CEC_KEYCODE_NUMBERS_6 = 38;
    public static final int CEC_KEYCODE_NUMBERS_7 = 39;
    public static final int CEC_KEYCODE_NUMBERS_8 = 40;
    public static final int CEC_KEYCODE_NUMBERS_9 = 41;
    public static final int CEC_KEYCODE_NUMBER_0_OR_NUMBER_10 = 32;
    public static final int CEC_KEYCODE_NUMBER_11 = 30;
    public static final int CEC_KEYCODE_NUMBER_12 = 31;
    public static final int CEC_KEYCODE_NUMBER_ENTRY_MODE = 29;
    public static final int CEC_KEYCODE_PAGE_DOWN = 56;
    public static final int CEC_KEYCODE_PAGE_UP = 55;
    public static final int CEC_KEYCODE_PAUSE = 70;
    public static final int CEC_KEYCODE_PAUSE_PLAY_FUNCTION = 97;
    public static final int CEC_KEYCODE_PAUSE_RECORD = 78;
    public static final int CEC_KEYCODE_PAUSE_RECORD_FUNCTION = 99;
    public static final int CEC_KEYCODE_PLAY = 68;
    public static final int CEC_KEYCODE_PLAY_FUNCTION = 96;
    public static final int CEC_KEYCODE_POWER = 64;
    public static final int CEC_KEYCODE_POWER_OFF_FUNCTION = 108;
    public static final int CEC_KEYCODE_POWER_ON_FUNCTION = 109;
    public static final int CEC_KEYCODE_POWER_TOGGLE_FUNCTION = 107;
    public static final int CEC_KEYCODE_PREVIOUS_CHANNEL = 50;
    public static final int CEC_KEYCODE_RECORD = 71;
    public static final int CEC_KEYCODE_RECORD_FUNCTION = 98;
    public static final int CEC_KEYCODE_RESERVED = 79;
    public static final int CEC_KEYCODE_RESTORE_VOLUME_FUNCTION = 102;
    public static final int CEC_KEYCODE_REWIND = 72;
    public static final int CEC_KEYCODE_RIGHT = 4;
    public static final int CEC_KEYCODE_RIGHT_DOWN = 6;
    public static final int CEC_KEYCODE_RIGHT_UP = 5;
    public static final int CEC_KEYCODE_ROOT_MENU = 9;
    public static final int CEC_KEYCODE_SELECT = 0;
    public static final int CEC_KEYCODE_SELECT_AUDIO_INPUT_FUNCTION = 106;
    public static final int CEC_KEYCODE_SELECT_AV_INPUT_FUNCTION = 105;
    public static final int CEC_KEYCODE_SELECT_BROADCAST_TYPE = 86;
    public static final int CEC_KEYCODE_SELECT_MEDIA_FUNCTION = 104;
    public static final int CEC_KEYCODE_SELECT_SOUND_PRESENTATION = 87;
    public static final int CEC_KEYCODE_SETUP_MENU = 10;
    public static final int CEC_KEYCODE_SOUND_SELECT = 51;
    public static final int CEC_KEYCODE_STOP = 69;
    public static final int CEC_KEYCODE_STOP_FUNCTION = 100;
    public static final int CEC_KEYCODE_STOP_RECORD = 77;
    public static final int CEC_KEYCODE_SUB_PICTURE = 81;
    public static final int CEC_KEYCODE_TIMER_PROGRAMMING = 84;
    public static final int CEC_KEYCODE_TUNE_FUNCTION = 103;
    public static final int CEC_KEYCODE_UP = 1;
    public static final int CEC_KEYCODE_VIDEO_ON_DEMAND = 82;
    public static final int CEC_KEYCODE_VOLUME_DOWN = 66;
    public static final int CEC_KEYCODE_VOLUME_UP = 65;
    private static final KeycodeEntry[] KEYCODE_ENTRIES;
    public static final int NO_PARAM = -1;
    public static final int UI_BROADCAST_ANALOGUE = 16;
    public static final int UI_BROADCAST_ANALOGUE_CABLE = 48;
    public static final int UI_BROADCAST_ANALOGUE_SATELLITE = 64;
    public static final int UI_BROADCAST_ANALOGUE_TERRESTRIAL = 32;
    public static final int UI_BROADCAST_DIGITAL = 80;
    public static final int UI_BROADCAST_DIGITAL_CABLE = 112;
    public static final int UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE = 144;
    public static final int UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2 = 145;
    public static final int UI_BROADCAST_DIGITAL_SATELLITE = 128;
    public static final int UI_BROADCAST_DIGITAL_TERRESTRIAL = 96;
    public static final int UI_BROADCAST_IP = 160;
    public static final int UI_BROADCAST_TOGGLE_ALL = 0;
    public static final int UI_BROADCAST_TOGGLE_ANALOGUE_DIGITAL = 1;
    public static final int UI_SOUND_PRESENTATION_BASS_NEUTRAL = 178;
    public static final int UI_SOUND_PRESENTATION_BASS_STEP_MINUS = 179;
    public static final int UI_SOUND_PRESENTATION_BASS_STEP_PLUS = 177;
    public static final int UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER = 160;
    public static final int UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_REVERBERATION = 144;
    public static final int UI_SOUND_PRESENTATION_SELECT_AUDIO_DOWN_MIX = 128;
    public static final int UI_SOUND_PRESENTATION_SOUND_MIX_DUAL_MONO = 32;
    public static final int UI_SOUND_PRESENTATION_SOUND_MIX_KARAOKE = 48;
    public static final int UI_SOUND_PRESENTATION_TREBLE_NEUTRAL = 194;
    public static final int UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS = 195;
    public static final int UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS = 193;
    public static final int UNSUPPORTED_KEYCODE = -1;

    private static class KeycodeEntry {
        private final int mAndroidKeycode;
        private final byte[] mCecKeycodeAndParams;
        private final boolean mIsRepeatable;

        private KeycodeEntry(int androidKeycode, int cecKeycode, boolean isRepeatable, byte[] cecParams) {
            this.mAndroidKeycode = androidKeycode;
            this.mIsRepeatable = isRepeatable;
            this.mCecKeycodeAndParams = new byte[(cecParams.length + 1)];
            System.arraycopy(cecParams, 0, this.mCecKeycodeAndParams, 1, cecParams.length);
            this.mCecKeycodeAndParams[0] = (byte) (cecKeycode & 255);
        }

        private KeycodeEntry(int androidKeycode, int cecKeycode, boolean isRepeatable) {
            this(androidKeycode, cecKeycode, isRepeatable, EmptyArray.BYTE);
        }

        private KeycodeEntry(int androidKeycode, int cecKeycode, byte[] cecParams) {
            this(androidKeycode, cecKeycode, true, cecParams);
        }

        private KeycodeEntry(int androidKeycode, int cecKeycode) {
            this(androidKeycode, cecKeycode, true, EmptyArray.BYTE);
        }

        /* access modifiers changed from: private */
        public byte[] toCecKeycodeAndParamIfMatched(int androidKeycode) {
            if (this.mAndroidKeycode == androidKeycode) {
                return this.mCecKeycodeAndParams;
            }
            return null;
        }

        /* access modifiers changed from: private */
        public int toAndroidKeycodeIfMatched(byte[] cecKeycodeAndParams) {
            if (Arrays.equals(this.mCecKeycodeAndParams, cecKeycodeAndParams)) {
                return this.mAndroidKeycode;
            }
            return -1;
        }

        /* access modifiers changed from: private */
        public Boolean isRepeatableIfMatched(int androidKeycode) {
            if (this.mAndroidKeycode == androidKeycode) {
                return Boolean.valueOf(this.mIsRepeatable);
            }
            return null;
        }
    }

    private HdmiCecKeycode() {
    }

    private static byte[] intToSingleByteArray(int value) {
        return new byte[]{(byte) (value & 255)};
    }

    static {
        KeycodeEntry keycodeEntry = new KeycodeEntry(235, 86, true, intToSingleByteArray(16));
        KeycodeEntry keycodeEntry2 = new KeycodeEntry(DhcpPacket.MIN_PACKET_LENGTH_BOOTP, 86, true, intToSingleByteArray(96));
        KeycodeEntry keycodeEntry3 = new KeycodeEntry(238, 86, true, intToSingleByteArray(128));
        KeycodeEntry keycodeEntry4 = new KeycodeEntry(UsbDescriptor.CLASSID_MISC, 86, true, intToSingleByteArray(144));
        KeycodeEntry keycodeEntry5 = new KeycodeEntry(241, 86, true, intToSingleByteArray(1));
        KEYCODE_ENTRIES = new KeycodeEntry[]{new KeycodeEntry(23, 0), new KeycodeEntry(19, 1), new KeycodeEntry(20, 2), new KeycodeEntry(21, 3), new KeycodeEntry(22, 4), new KeycodeEntry(-1, 5), new KeycodeEntry(-1, 6), new KeycodeEntry(-1, 7), new KeycodeEntry(-1, 8), new KeycodeEntry(3, 9), new KeycodeEntry(176, 10), new KeycodeEntry(256, 11, false), new KeycodeEntry(-1, 12), new KeycodeEntry(4, 13), new KeycodeEntry(111, 13), new KeycodeEntry(226, 16), new KeycodeEntry(257, 17), new KeycodeEntry(234, 29), new KeycodeEntry(227, 30), new KeycodeEntry(228, 31), new KeycodeEntry(7, 32), new KeycodeEntry(8, 33), new KeycodeEntry(9, 34), new KeycodeEntry(10, 35), new KeycodeEntry(11, 36), new KeycodeEntry(12, 37), new KeycodeEntry(13, 38), new KeycodeEntry(14, 39), new KeycodeEntry(15, 40), new KeycodeEntry(16, 41), new KeycodeEntry(56, 42), new KeycodeEntry(160, 43), new KeycodeEntry(28, 44), new KeycodeEntry(-1, 47), new KeycodeEntry(166, 48), new KeycodeEntry(167, 49), new KeycodeEntry(229, 50), new KeycodeEntry(-1, 51), new KeycodeEntry((int) UI_SOUND_PRESENTATION_BASS_NEUTRAL, 52), new KeycodeEntry(165, 53), new KeycodeEntry(-1, 54), new KeycodeEntry(92, 55), new KeycodeEntry(93, 56), new KeycodeEntry(26, 64, false), new KeycodeEntry(24, 65), new KeycodeEntry(25, 66), new KeycodeEntry(164, 67, false), new KeycodeEntry(126, 68), new KeycodeEntry(86, 69), new KeycodeEntry(127, 70), new KeycodeEntry(85, 70), new KeycodeEntry(130, 71), new KeycodeEntry(89, 72), new KeycodeEntry(90, 73), new KeycodeEntry((int) NetworkConstants.ICMPV6_ECHO_REPLY_TYPE, 74), new KeycodeEntry(87, 75), new KeycodeEntry(88, 76), new KeycodeEntry(-1, 77), new KeycodeEntry(-1, 78), new KeycodeEntry(-1, 79), new KeycodeEntry(-1, 80), new KeycodeEntry(175, 81), new KeycodeEntry(-1, 82), new KeycodeEntry(172, 83), new KeycodeEntry((int) LightsManager.LIGHT_ID_AUTOCUSTOMBACKLIGHT, 84), new KeycodeEntry(-1, 85), new KeycodeEntry(-1, 86), keycodeEntry, keycodeEntry2, keycodeEntry3, keycodeEntry4, keycodeEntry5, new KeycodeEntry(-1, 87), new KeycodeEntry(-1, 96, false), new KeycodeEntry(-1, 97, false), new KeycodeEntry(-1, 98, false), new KeycodeEntry(-1, 99, false), new KeycodeEntry(-1, 100, false), new KeycodeEntry(-1, 101, false), new KeycodeEntry(-1, 102, false), new KeycodeEntry(-1, 103, false), new KeycodeEntry(-1, 104, false), new KeycodeEntry(-1, 105, false), new KeycodeEntry(-1, 106, false), new KeycodeEntry(-1, 107, false), new KeycodeEntry(-1, 108, false), new KeycodeEntry(-1, 109, false), new KeycodeEntry(186, 113), new KeycodeEntry(183, 114), new KeycodeEntry(184, (int) CEC_KEYCODE_F3_GREEN), new KeycodeEntry(185, (int) CEC_KEYCODE_F4_YELLOW), new KeycodeEntry((int) NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION, (int) CEC_KEYCODE_F5), new KeycodeEntry(230, (int) CEC_KEYCODE_DATA)};
    }

    static byte[] androidKeyToCecKey(int keycode) {
        for (KeycodeEntry access$300 : KEYCODE_ENTRIES) {
            byte[] cecKeycodeAndParams = access$300.toCecKeycodeAndParamIfMatched(keycode);
            if (cecKeycodeAndParams != null) {
                return cecKeycodeAndParams;
            }
        }
        return null;
    }

    static int cecKeycodeAndParamsToAndroidKey(byte[] cecKeycodeAndParams) {
        for (KeycodeEntry access$400 : KEYCODE_ENTRIES) {
            int androidKey = access$400.toAndroidKeycodeIfMatched(cecKeycodeAndParams);
            if (androidKey != -1) {
                return androidKey;
            }
        }
        return -1;
    }

    static boolean isRepeatableKey(int androidKeycode) {
        for (KeycodeEntry access$500 : KEYCODE_ENTRIES) {
            Boolean isRepeatable = access$500.isRepeatableIfMatched(androidKeycode);
            if (isRepeatable != null) {
                return isRepeatable.booleanValue();
            }
        }
        return false;
    }

    static boolean isSupportedKeycode(int androidKeycode) {
        return androidKeyToCecKey(androidKeycode) != null;
    }

    public static int getMuteKey(boolean muting) {
        return 67;
    }
}

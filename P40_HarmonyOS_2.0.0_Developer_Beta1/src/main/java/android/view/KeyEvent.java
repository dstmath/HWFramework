package android.view;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.view.KeyCharacterMap;

public class KeyEvent extends InputEvent implements Parcelable {
    public static final int ACTION_DOWN = 0;
    @Deprecated
    public static final int ACTION_MULTIPLE = 2;
    public static final int ACTION_UP = 1;
    public static final Parcelable.Creator<KeyEvent> CREATOR = new Parcelable.Creator<KeyEvent>() {
        /* class android.view.KeyEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KeyEvent createFromParcel(Parcel in) {
            in.readInt();
            return KeyEvent.createFromParcelBody(in);
        }

        @Override // android.os.Parcelable.Creator
        public KeyEvent[] newArray(int size) {
            return new KeyEvent[size];
        }
    };
    static final boolean DEBUG = false;
    public static final int FINGERPRINT_APP_MAX_KEYCODE = 600;
    public static final int FINGERPRINT_DOUBLE_TAP = 501;
    public static final int FINGERPRINT_SINGLE_TAP = 601;
    public static final int FLAG_CANCELED = 32;
    public static final int FLAG_CANCELED_LONG_PRESS = 256;
    public static final int FLAG_EDITOR_ACTION = 16;
    public static final int FLAG_FALLBACK = 1024;
    public static final int FLAG_FROM_FINGERPRINT = 2048;
    public static final int FLAG_FROM_HW_VASSIST_EXTERNAL = 4096;
    public static final int FLAG_FROM_SYSTEM = 8;
    public static final int FLAG_KEEP_TOUCH_MODE = 4;
    public static final int FLAG_LONG_PRESS = 128;
    public static final int FLAG_PREDISPATCH = 536870912;
    public static final int FLAG_SOFT_KEYBOARD = 2;
    public static final int FLAG_START_TRACKING = 1073741824;
    public static final int FLAG_TAINTED = Integer.MIN_VALUE;
    public static final int FLAG_TRACKING = 512;
    public static final int FLAG_VIRTUAL_HARD_KEY = 64;
    @Deprecated
    public static final int FLAG_WOKE_HERE = 1;
    public static final int HW_LAST_KEYCODE = 746;
    public static final int KEYCODE_0 = 7;
    public static final int KEYCODE_1 = 8;
    public static final int KEYCODE_11 = 227;
    public static final int KEYCODE_12 = 228;
    public static final int KEYCODE_2 = 9;
    public static final int KEYCODE_3 = 10;
    public static final int KEYCODE_3D_MODE = 206;
    public static final int KEYCODE_4 = 11;
    public static final int KEYCODE_5 = 12;
    public static final int KEYCODE_6 = 13;
    public static final int KEYCODE_7 = 14;
    public static final int KEYCODE_8 = 15;
    public static final int KEYCODE_9 = 16;
    public static final int KEYCODE_A = 29;
    public static final int KEYCODE_ALL_APPS = 284;
    public static final int KEYCODE_ALT_LEFT = 57;
    public static final int KEYCODE_ALT_RIGHT = 58;
    public static final int KEYCODE_AOD_FINGER_DOWN = 735;
    public static final int KEYCODE_AOD_FINGER_UP = 736;
    public static final int KEYCODE_APOSTROPHE = 75;
    public static final int KEYCODE_APP_SWITCH = 187;
    public static final int KEYCODE_ASSIST = 219;
    public static final int KEYCODE_AT = 77;
    public static final int KEYCODE_AVR_INPUT = 182;
    public static final int KEYCODE_AVR_POWER = 181;
    public static final int KEYCODE_B = 30;
    public static final int KEYCODE_BACK = 4;
    public static final int KEYCODE_BACKSLASH = 73;
    public static final int KEYCODE_BLE_DOUBLE_CLICK = 724;
    public static final int KEYCODE_BLE_POWER = 723;
    public static final int KEYCODE_BLE_TRIPLE_CLICK = 725;
    public static final int KEYCODE_BOOKMARK = 174;
    public static final int KEYCODE_BREAK = 121;
    public static final int KEYCODE_BRIGHTNESS_DOWN = 220;
    public static final int KEYCODE_BRIGHTNESS_UP = 221;
    public static final int KEYCODE_BUTTON_1 = 188;
    public static final int KEYCODE_BUTTON_10 = 197;
    public static final int KEYCODE_BUTTON_11 = 198;
    public static final int KEYCODE_BUTTON_12 = 199;
    public static final int KEYCODE_BUTTON_13 = 200;
    public static final int KEYCODE_BUTTON_14 = 201;
    public static final int KEYCODE_BUTTON_15 = 202;
    public static final int KEYCODE_BUTTON_16 = 203;
    public static final int KEYCODE_BUTTON_2 = 189;
    public static final int KEYCODE_BUTTON_3 = 190;
    public static final int KEYCODE_BUTTON_4 = 191;
    public static final int KEYCODE_BUTTON_5 = 192;
    public static final int KEYCODE_BUTTON_6 = 193;
    public static final int KEYCODE_BUTTON_7 = 194;
    public static final int KEYCODE_BUTTON_8 = 195;
    public static final int KEYCODE_BUTTON_9 = 196;
    public static final int KEYCODE_BUTTON_A = 96;
    public static final int KEYCODE_BUTTON_B = 97;
    public static final int KEYCODE_BUTTON_C = 98;
    public static final int KEYCODE_BUTTON_L1 = 102;
    public static final int KEYCODE_BUTTON_L2 = 104;
    public static final int KEYCODE_BUTTON_MODE = 110;
    public static final int KEYCODE_BUTTON_R1 = 103;
    public static final int KEYCODE_BUTTON_R2 = 105;
    public static final int KEYCODE_BUTTON_SELECT = 109;
    public static final int KEYCODE_BUTTON_START = 108;
    public static final int KEYCODE_BUTTON_THUMBL = 106;
    public static final int KEYCODE_BUTTON_THUMBR = 107;
    public static final int KEYCODE_BUTTON_X = 99;
    public static final int KEYCODE_BUTTON_Y = 100;
    public static final int KEYCODE_BUTTON_Z = 101;
    public static final int KEYCODE_C = 31;
    public static final int KEYCODE_CALCULATOR = 210;
    public static final int KEYCODE_CALENDAR = 208;
    public static final int KEYCODE_CALL = 5;
    public static final int KEYCODE_CAMERA = 27;
    public static final int KEYCODE_CAMERA_AWAKE = 717;
    public static final int KEYCODE_CAPS_LOCK = 115;
    public static final int KEYCODE_CAPTIONS = 175;
    public static final int KEYCODE_CARROCKER_DOWN = 742;
    public static final int KEYCODE_CARROCKER_LEFT = 743;
    public static final int KEYCODE_CARROCKER_RIGHT = 744;
    public static final int KEYCODE_CARROCKER_UP = 741;
    public static final int KEYCODE_CHANNEL_DOWN = 167;
    public static final int KEYCODE_CHANNEL_UP = 166;
    public static final int KEYCODE_CLEAR = 28;
    public static final int KEYCODE_COMMA = 55;
    public static final int KEYCODE_CONTACTS = 207;
    public static final int KEYCODE_COPY = 278;
    public static final int KEYCODE_CTRL_LEFT = 113;
    public static final int KEYCODE_CTRL_RIGHT = 114;
    public static final int KEYCODE_CUT = 277;
    public static final int KEYCODE_D = 32;
    public static final int KEYCODE_DEL = 67;
    public static final int KEYCODE_DPAD_CENTER = 23;
    public static final int KEYCODE_DPAD_DOWN = 20;
    public static final int KEYCODE_DPAD_DOWN_LEFT = 269;
    public static final int KEYCODE_DPAD_DOWN_RIGHT = 271;
    public static final int KEYCODE_DPAD_LEFT = 21;
    public static final int KEYCODE_DPAD_RIGHT = 22;
    public static final int KEYCODE_DPAD_UP = 19;
    public static final int KEYCODE_DPAD_UP_LEFT = 268;
    public static final int KEYCODE_DPAD_UP_RIGHT = 270;
    public static final int KEYCODE_DVR = 173;
    public static final int KEYCODE_E = 33;
    public static final int KEYCODE_EISU = 212;
    public static final int KEYCODE_ENDCALL = 6;
    public static final int KEYCODE_ENTER = 66;
    public static final int KEYCODE_ENVELOPE = 65;
    public static final int KEYCODE_EQUALS = 70;
    public static final int KEYCODE_ESCAPE = 111;
    public static final int KEYCODE_EXPLORER = 64;
    public static final int KEYCODE_F = 34;
    public static final int KEYCODE_F1 = 131;
    public static final int KEYCODE_F10 = 140;
    public static final int KEYCODE_F11 = 141;
    public static final int KEYCODE_F12 = 142;
    public static final int KEYCODE_F2 = 132;
    public static final int KEYCODE_F20 = 718;
    public static final int KEYCODE_F26 = 703;
    public static final int KEYCODE_F3 = 133;
    public static final int KEYCODE_F4 = 134;
    public static final int KEYCODE_F5 = 135;
    public static final int KEYCODE_F6 = 136;
    public static final int KEYCODE_F7 = 137;
    public static final int KEYCODE_F8 = 138;
    public static final int KEYCODE_F9 = 139;
    public static final int KEYCODE_FINGERPRINT_DOWN = 512;
    public static final int KEYCODE_FINGERPRINT_LEFT = 513;
    public static final int KEYCODE_FINGERPRINT_LONGPRESS = 502;
    public static final int KEYCODE_FINGERPRINT_RIGHT = 514;
    public static final int KEYCODE_FINGERPRINT_UP = 511;
    public static final int KEYCODE_FOCUS = 80;
    public static final int KEYCODE_FORWARD = 125;
    public static final int KEYCODE_FORWARD_ACTION = 728;
    public static final int KEYCODE_FORWARD_DEL = 112;
    public static final int KEYCODE_FUNCTION = 119;
    public static final int KEYCODE_G = 35;
    public static final int KEYCODE_GAMEKEY = 702;
    public static final int KEYCODE_GAMESPACE = 715;
    public static final int KEYCODE_GESTURE_PAUSE = 731;
    public static final int KEYCODE_GESTURE_STATUS = 734;
    public static final int KEYCODE_GESTURE_STOP = 729;
    public static final int KEYCODE_GRAVE = 68;
    public static final int KEYCODE_GUIDE = 172;
    public static final int KEYCODE_H = 36;
    public static final int KEYCODE_HEADSETHOOK = 79;
    public static final int KEYCODE_HELP = 259;
    public static final int KEYCODE_HENKAN = 214;
    public static final int KEYCODE_HOME = 3;
    public static final int KEYCODE_I = 37;
    public static final int KEYCODE_INFO = 165;
    public static final int KEYCODE_INPUTMETHOD_ACTION = 746;
    public static final int KEYCODE_INSERT = 124;
    public static final int KEYCODE_J = 38;
    public static final int KEYCODE_K = 39;
    public static final int KEYCODE_KANA = 218;
    public static final int KEYCODE_KATAKANA_HIRAGANA = 215;
    public static final int KEYCODE_L = 40;
    public static final int KEYCODE_LANGUAGE_SWITCH = 204;
    public static final int KEYCODE_LAST_CHANNEL = 229;
    public static final int KEYCODE_LEFT_BRACKET = 71;
    public static final int KEYCODE_M = 41;
    public static final int KEYCODE_MANNER_MODE = 205;
    public static final int KEYCODE_MEDIA_AUDIO_TRACK = 222;
    public static final int KEYCODE_MEDIA_CLOSE = 128;
    public static final int KEYCODE_MEDIA_EJECT = 129;
    public static final int KEYCODE_MEDIA_FAST_FORWARD = 90;
    public static final int KEYCODE_MEDIA_NEXT = 87;
    public static final int KEYCODE_MEDIA_PAUSE = 127;
    public static final int KEYCODE_MEDIA_PLAY = 126;
    public static final int KEYCODE_MEDIA_PLAY_PAUSE = 85;
    public static final int KEYCODE_MEDIA_PREVIOUS = 88;
    public static final int KEYCODE_MEDIA_RECORD = 130;
    public static final int KEYCODE_MEDIA_REWIND = 89;
    public static final int KEYCODE_MEDIA_SKIP_BACKWARD = 273;
    public static final int KEYCODE_MEDIA_SKIP_FORWARD = 272;
    public static final int KEYCODE_MEDIA_STEP_BACKWARD = 275;
    public static final int KEYCODE_MEDIA_STEP_FORWARD = 274;
    public static final int KEYCODE_MEDIA_STOP = 86;
    public static final int KEYCODE_MEDIA_TOP_MENU = 226;
    public static final int KEYCODE_MENU = 82;
    public static final int KEYCODE_META_LEFT = 117;
    public static final int KEYCODE_META_RIGHT = 118;
    public static final int KEYCODE_MINUS = 69;
    public static final int KEYCODE_MOVE_END = 123;
    public static final int KEYCODE_MOVE_HOME = 122;
    public static final int KEYCODE_MUHENKAN = 213;
    public static final int KEYCODE_MUSIC = 209;
    public static final int KEYCODE_MUTE = 91;
    public static final int KEYCODE_N = 42;
    public static final int KEYCODE_NAVIGATE_IN = 262;
    public static final int KEYCODE_NAVIGATE_NEXT = 261;
    public static final int KEYCODE_NAVIGATE_OUT = 263;
    public static final int KEYCODE_NAVIGATE_PREVIOUS = 260;
    public static final int KEYCODE_NOTIFICATION = 83;
    public static final int KEYCODE_NUM = 78;
    public static final int KEYCODE_NUMPAD_0 = 144;
    public static final int KEYCODE_NUMPAD_1 = 145;
    public static final int KEYCODE_NUMPAD_2 = 146;
    public static final int KEYCODE_NUMPAD_3 = 147;
    public static final int KEYCODE_NUMPAD_4 = 148;
    public static final int KEYCODE_NUMPAD_5 = 149;
    public static final int KEYCODE_NUMPAD_6 = 150;
    public static final int KEYCODE_NUMPAD_7 = 151;
    public static final int KEYCODE_NUMPAD_8 = 152;
    public static final int KEYCODE_NUMPAD_9 = 153;
    public static final int KEYCODE_NUMPAD_ADD = 157;
    public static final int KEYCODE_NUMPAD_COMMA = 159;
    public static final int KEYCODE_NUMPAD_DIVIDE = 154;
    public static final int KEYCODE_NUMPAD_DOT = 158;
    public static final int KEYCODE_NUMPAD_ENTER = 160;
    public static final int KEYCODE_NUMPAD_EQUALS = 161;
    public static final int KEYCODE_NUMPAD_LEFT_PAREN = 162;
    public static final int KEYCODE_NUMPAD_MULTIPLY = 155;
    public static final int KEYCODE_NUMPAD_RIGHT_PAREN = 163;
    public static final int KEYCODE_NUMPAD_SUBTRACT = 156;
    public static final int KEYCODE_NUM_LOCK = 143;
    public static final int KEYCODE_O = 43;
    public static final int KEYCODE_P = 44;
    public static final int KEYCODE_PAGE_DOWN = 93;
    public static final int KEYCODE_PAGE_UP = 92;
    public static final int KEYCODE_PAIRING = 225;
    public static final int KEYCODE_PASTE = 279;
    public static final int KEYCODE_PEOPLE_COUNT = 732;
    public static final int KEYCODE_PERIOD = 56;
    public static final int KEYCODE_PICTSYMBOLS = 94;
    public static final int KEYCODE_PLUS = 81;
    public static final int KEYCODE_POUND = 18;
    public static final int KEYCODE_POWER = 26;
    public static final int KEYCODE_PROFILE_SWITCH = 288;
    public static final int KEYCODE_PROG_BLUE = 186;
    public static final int KEYCODE_PROG_GREEN = 184;
    public static final int KEYCODE_PROG_RED = 183;
    public static final int KEYCODE_PROG_YELLOW = 185;
    public static final int KEYCODE_Q = 45;
    public static final int KEYCODE_R = 46;
    public static final int KEYCODE_REFRESH = 285;
    public static final int KEYCODE_RIGHT_BRACKET = 72;
    public static final int KEYCODE_RO = 217;
    public static final int KEYCODE_ROTATE_CAMERA = 701;
    public static final int KEYCODE_S = 47;
    public static final int KEYCODE_SCREEN_FLIP = 707;
    public static final int KEYCODE_SCROLL_LOCK = 116;
    public static final int KEYCODE_SEARCH = 84;
    public static final int KEYCODE_SEMICOLON = 74;
    public static final int KEYCODE_SETTINGS = 176;
    public static final int KEYCODE_SHIFT_LEFT = 59;
    public static final int KEYCODE_SHIFT_RIGHT = 60;
    public static final int KEYCODE_SINGLE_TOUCH = 716;
    public static final int KEYCODE_SLASH = 76;
    public static final int KEYCODE_SLEEP = 223;
    public static final int KEYCODE_SMARTKEY = 308;
    public static final int KEYCODE_SOFT_LEFT = 1;
    public static final int KEYCODE_SOFT_RIGHT = 2;
    public static final int KEYCODE_SOFT_SLEEP = 276;
    public static final int KEYCODE_SOUNDTRIGGER_1 = 401;
    public static final int KEYCODE_SOUNDTRIGGER_2 = 402;
    public static final int KEYCODE_SOUNDTRIGGER_3 = 403;
    public static final int KEYCODE_SOUNDTRIGGER_4 = 404;
    public static final int KEYCODE_SOUNDTRIGGER_5 = 405;
    public static final int KEYCODE_SPACE = 62;
    public static final int KEYCODE_STAR = 17;
    public static final int KEYCODE_STB_INPUT = 180;
    public static final int KEYCODE_STB_POWER = 179;
    public static final int KEYCODE_STEM_1 = 265;
    public static final int KEYCODE_STEM_2 = 266;
    public static final int KEYCODE_STEM_3 = 267;
    public static final int KEYCODE_STEM_PRIMARY = 264;
    public static final int KEYCODE_STYLUS_POWERON = 705;
    public static final int KEYCODE_STYLUS_WAKEUP = 704;
    public static final int KEYCODE_SWING_MOTION_START_DOWN = 722;
    public static final int KEYCODE_SWING_MOTION_START_UP = 721;
    public static final int KEYCODE_SWING_SWIPE_DOWN = 713;
    public static final int KEYCODE_SWING_SWIPE_LEFT = 710;
    public static final int KEYCODE_SWING_SWIPE_PUSH = 714;
    public static final int KEYCODE_SWING_SWIPE_RIGHT = 711;
    public static final int KEYCODE_SWING_SWIPE_UP = 712;
    public static final int KEYCODE_SWITCH_CHARSET = 95;
    public static final int KEYCODE_SYM = 63;
    public static final int KEYCODE_SYSRQ = 120;
    public static final int KEYCODE_SYSTEM_NAVIGATION_DOWN = 281;
    public static final int KEYCODE_SYSTEM_NAVIGATION_LEFT = 282;
    public static final int KEYCODE_SYSTEM_NAVIGATION_RIGHT = 283;
    public static final int KEYCODE_SYSTEM_NAVIGATION_UP = 280;
    public static final int KEYCODE_T = 48;
    public static final int KEYCODE_TAB = 61;
    public static final int KEYCODE_THUMBS_DOWN = 287;
    public static final int KEYCODE_THUMBS_UP = 286;
    public static final int KEYCODE_TP_KEEP = 708;
    public static final int KEYCODE_TP_MOVED = 709;
    public static final int KEYCODE_TV = 170;
    public static final int KEYCODE_TV_ANTENNA_CABLE = 242;
    public static final int KEYCODE_TV_AUDIO_DESCRIPTION = 252;
    public static final int KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN = 254;
    public static final int KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP = 253;
    public static final int KEYCODE_TV_CONTENTS_MENU = 256;
    public static final int KEYCODE_TV_CUSTOM_1 = 737;
    public static final int KEYCODE_TV_CUSTOM_2 = 738;
    public static final int KEYCODE_TV_CUSTOM_3 = 739;
    public static final int KEYCODE_TV_CUSTOM_4 = 740;
    public static final int KEYCODE_TV_DATA_SERVICE = 230;
    public static final int KEYCODE_TV_INPUT = 178;
    public static final int KEYCODE_TV_INPUT_COMPONENT_1 = 249;
    public static final int KEYCODE_TV_INPUT_COMPONENT_2 = 250;
    public static final int KEYCODE_TV_INPUT_COMPOSITE_1 = 247;
    public static final int KEYCODE_TV_INPUT_COMPOSITE_2 = 248;
    public static final int KEYCODE_TV_INPUT_HDMI_1 = 243;
    public static final int KEYCODE_TV_INPUT_HDMI_2 = 244;
    public static final int KEYCODE_TV_INPUT_HDMI_3 = 245;
    public static final int KEYCODE_TV_INPUT_HDMI_4 = 246;
    public static final int KEYCODE_TV_INPUT_VGA_1 = 251;
    public static final int KEYCODE_TV_MEDIA_CONTEXT_MENU = 257;
    public static final int KEYCODE_TV_NETWORK = 241;
    public static final int KEYCODE_TV_NUMBER_ENTRY = 234;
    public static final int KEYCODE_TV_POWER = 177;
    public static final int KEYCODE_TV_RADIO_SERVICE = 232;
    public static final int KEYCODE_TV_SATELLITE = 237;
    public static final int KEYCODE_TV_SATELLITE_BS = 238;
    public static final int KEYCODE_TV_SATELLITE_CS = 239;
    public static final int KEYCODE_TV_SATELLITE_SERVICE = 240;
    public static final int KEYCODE_TV_TELETEXT = 233;
    public static final int KEYCODE_TV_TERRESTRIAL_ANALOG = 235;
    public static final int KEYCODE_TV_TERRESTRIAL_DIGITAL = 236;
    public static final int KEYCODE_TV_TIMER_PROGRAMMING = 258;
    public static final int KEYCODE_TV_ZOOM_MODE = 255;
    public static final int KEYCODE_U = 49;
    public static final int KEYCODE_UNKNOWN = 0;
    public static final int KEYCODE_V = 50;
    public static final int KEYCODE_VIDEO_NEXT = 706;
    public static final int KEYCODE_VOICE_ASSIST = 231;
    public static final int KEYCODE_VOICE_INPUT = 745;
    public static final int KEYCODE_VOLUME_CHANGE = 730;
    public static final int KEYCODE_VOLUME_DOWN = 25;
    public static final int KEYCODE_VOLUME_MUTE = 164;
    public static final int KEYCODE_VOLUME_TRIGGER = 720;
    public static final int KEYCODE_VOLUME_UP = 24;
    public static final int KEYCODE_W = 51;
    public static final int KEYCODE_WAKEUP = 224;
    public static final int KEYCODE_WINDOW = 171;
    public static final int KEYCODE_X = 52;
    public static final int KEYCODE_Y = 53;
    public static final int KEYCODE_YEN = 216;
    public static final int KEYCODE_Z = 54;
    public static final int KEYCODE_ZENKAKU_HANKAKU = 211;
    public static final int KEYCODE_ZOOM_IN = 168;
    public static final int KEYCODE_ZOOM_OUT = 169;
    private static final String LABEL_PREFIX = "KEYCODE_";
    public static final int LAST_KEYCODE = 288;
    @Deprecated
    public static final int MAX_KEYCODE = 84;
    private static final int MAX_RECYCLED = 10;
    @UnsupportedAppUsage
    private static final int META_ALL_MASK = 7827711;
    public static final int META_ALT_LEFT_ON = 16;
    @UnsupportedAppUsage
    public static final int META_ALT_LOCKED = 512;
    public static final int META_ALT_MASK = 50;
    public static final int META_ALT_ON = 2;
    public static final int META_ALT_RIGHT_ON = 32;
    public static final int META_CAPS_LOCK_ON = 1048576;
    @UnsupportedAppUsage
    public static final int META_CAP_LOCKED = 256;
    public static final int META_CTRL_LEFT_ON = 8192;
    public static final int META_CTRL_MASK = 28672;
    public static final int META_CTRL_ON = 4096;
    public static final int META_CTRL_RIGHT_ON = 16384;
    public static final int META_FUNCTION_ON = 8;
    @UnsupportedAppUsage
    private static final int META_INVALID_MODIFIER_MASK = 7343872;
    @UnsupportedAppUsage
    private static final int META_LOCK_MASK = 7340032;
    public static final int META_META_LEFT_ON = 131072;
    public static final int META_META_MASK = 458752;
    public static final int META_META_ON = 65536;
    public static final int META_META_RIGHT_ON = 262144;
    @UnsupportedAppUsage
    private static final int META_MODIFIER_MASK = 487679;
    public static final int META_NUM_LOCK_ON = 2097152;
    public static final int META_SCROLL_LOCK_ON = 4194304;
    @UnsupportedAppUsage
    public static final int META_SELECTING = 2048;
    public static final int META_SHIFT_LEFT_ON = 64;
    public static final int META_SHIFT_MASK = 193;
    public static final int META_SHIFT_ON = 1;
    public static final int META_SHIFT_RIGHT_ON = 128;
    @UnsupportedAppUsage
    private static final String[] META_SYMBOLIC_NAMES = {"META_SHIFT_ON", "META_ALT_ON", "META_SYM_ON", "META_FUNCTION_ON", "META_ALT_LEFT_ON", "META_ALT_RIGHT_ON", "META_SHIFT_LEFT_ON", "META_SHIFT_RIGHT_ON", "META_CAP_LOCKED", "META_ALT_LOCKED", "META_SYM_LOCKED", "0x00000800", "META_CTRL_ON", "META_CTRL_LEFT_ON", "META_CTRL_RIGHT_ON", "0x00008000", "META_META_ON", "META_META_LEFT_ON", "META_META_RIGHT_ON", "0x00080000", "META_CAPS_LOCK_ON", "META_NUM_LOCK_ON", "META_SCROLL_LOCK_ON", "0x00800000", "0x01000000", "0x02000000", "0x04000000", "0x08000000", "0x10000000", "0x20000000", "0x40000000", "0x80000000"};
    @UnsupportedAppUsage
    public static final int META_SYM_LOCKED = 1024;
    public static final int META_SYM_ON = 4;
    @UnsupportedAppUsage
    private static final int META_SYNTHETIC_MASK = 3840;
    static final String TAG = "KeyEvent";
    private static final Object gRecyclerLock = new Object();
    private static KeyEvent gRecyclerTop;
    private static int gRecyclerUsed;
    @UnsupportedAppUsage
    private int mAction;
    @UnsupportedAppUsage
    private String mCharacters;
    @UnsupportedAppUsage
    private int mDeviceId;
    private int mDisplayId;
    @UnsupportedAppUsage
    private long mDownTime;
    @UnsupportedAppUsage
    private long mEventTime;
    @UnsupportedAppUsage
    private int mFlags;
    @UnsupportedAppUsage
    private int mKeyCode;
    @UnsupportedAppUsage
    private int mMetaState;
    private KeyEvent mNext;
    @UnsupportedAppUsage
    private int mRepeatCount;
    @UnsupportedAppUsage
    private int mScanCode;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mSource;

    public interface Callback {
        boolean onKeyDown(int i, KeyEvent keyEvent);

        boolean onKeyLongPress(int i, KeyEvent keyEvent);

        boolean onKeyMultiple(int i, int i2, KeyEvent keyEvent);

        boolean onKeyUp(int i, KeyEvent keyEvent);
    }

    private static native int nativeKeyCodeFromString(String str);

    private static native String nativeKeyCodeToString(int i);

    static /* synthetic */ int access$076(KeyEvent x0, int x1) {
        int i = x0.mFlags | x1;
        x0.mFlags = i;
        return i;
    }

    public static int getMaxKeyCode() {
        return 288;
    }

    public static int getHwMaxKeyCode() {
        return 746;
    }

    public static int getDeadChar(int accent, int c) {
        return KeyCharacterMap.getDeadChar(accent, c);
    }

    private KeyEvent() {
    }

    public KeyEvent(int action, int code) {
        this.mAction = action;
        this.mKeyCode = code;
        this.mRepeatCount = 0;
        this.mDeviceId = -1;
    }

    public KeyEvent(long downTime, long eventTime, int action, int code, int repeat) {
        this.mDownTime = downTime;
        this.mEventTime = eventTime;
        this.mAction = action;
        this.mKeyCode = code;
        this.mRepeatCount = repeat;
        this.mDeviceId = -1;
    }

    public KeyEvent(long downTime, long eventTime, int action, int code, int repeat, int metaState) {
        this.mDownTime = downTime;
        this.mEventTime = eventTime;
        this.mAction = action;
        this.mKeyCode = code;
        this.mRepeatCount = repeat;
        this.mMetaState = metaState;
        this.mDeviceId = -1;
    }

    public KeyEvent(long downTime, long eventTime, int action, int code, int repeat, int metaState, int deviceId, int scancode) {
        this.mDownTime = downTime;
        this.mEventTime = eventTime;
        this.mAction = action;
        this.mKeyCode = code;
        this.mRepeatCount = repeat;
        this.mMetaState = metaState;
        this.mDeviceId = deviceId;
        this.mScanCode = scancode;
    }

    public KeyEvent(long downTime, long eventTime, int action, int code, int repeat, int metaState, int deviceId, int scancode, int flags) {
        this.mDownTime = downTime;
        this.mEventTime = eventTime;
        this.mAction = action;
        this.mKeyCode = code;
        this.mRepeatCount = repeat;
        this.mMetaState = metaState;
        this.mDeviceId = deviceId;
        this.mScanCode = scancode;
        this.mFlags = flags;
    }

    public KeyEvent(long downTime, long eventTime, int action, int code, int repeat, int metaState, int deviceId, int scancode, int flags, int source) {
        this.mDownTime = downTime;
        this.mEventTime = eventTime;
        this.mAction = action;
        this.mKeyCode = code;
        this.mRepeatCount = repeat;
        this.mMetaState = metaState;
        this.mDeviceId = deviceId;
        this.mScanCode = scancode;
        this.mFlags = flags;
        this.mSource = source;
        this.mDisplayId = -1;
    }

    public KeyEvent(long time, String characters, int deviceId, int flags) {
        this.mDownTime = time;
        this.mEventTime = time;
        this.mCharacters = characters;
        this.mAction = 2;
        this.mKeyCode = 0;
        this.mRepeatCount = 0;
        this.mDeviceId = deviceId;
        this.mFlags = flags;
        this.mSource = 257;
        this.mDisplayId = -1;
    }

    public KeyEvent(KeyEvent origEvent) {
        this.mDownTime = origEvent.mDownTime;
        this.mEventTime = origEvent.mEventTime;
        this.mAction = origEvent.mAction;
        this.mKeyCode = origEvent.mKeyCode;
        this.mRepeatCount = origEvent.mRepeatCount;
        this.mMetaState = origEvent.mMetaState;
        this.mDeviceId = origEvent.mDeviceId;
        this.mSource = origEvent.mSource;
        this.mDisplayId = origEvent.mDisplayId;
        this.mScanCode = origEvent.mScanCode;
        this.mFlags = origEvent.mFlags;
        this.mCharacters = origEvent.mCharacters;
    }

    @Deprecated
    public KeyEvent(KeyEvent origEvent, long eventTime, int newRepeat) {
        this.mDownTime = origEvent.mDownTime;
        this.mEventTime = eventTime;
        this.mAction = origEvent.mAction;
        this.mKeyCode = origEvent.mKeyCode;
        this.mRepeatCount = newRepeat;
        this.mMetaState = origEvent.mMetaState;
        this.mDeviceId = origEvent.mDeviceId;
        this.mSource = origEvent.mSource;
        this.mDisplayId = origEvent.mDisplayId;
        this.mScanCode = origEvent.mScanCode;
        this.mFlags = origEvent.mFlags;
        this.mCharacters = origEvent.mCharacters;
    }

    private static KeyEvent obtain() {
        synchronized (gRecyclerLock) {
            KeyEvent ev = gRecyclerTop;
            if (ev == null) {
                return new KeyEvent();
            }
            gRecyclerTop = ev.mNext;
            gRecyclerUsed--;
            ev.mNext = null;
            ev.prepareForReuse();
            return ev;
        }
    }

    public static KeyEvent obtain(long downTime, long eventTime, int action, int code, int repeat, int metaState, int deviceId, int scancode, int flags, int source, int displayId, String characters) {
        KeyEvent ev = obtain();
        ev.mDownTime = downTime;
        ev.mEventTime = eventTime;
        ev.mAction = action;
        ev.mKeyCode = code;
        ev.mRepeatCount = repeat;
        ev.mMetaState = metaState;
        ev.mDeviceId = deviceId;
        ev.mScanCode = scancode;
        ev.mFlags = flags;
        ev.mSource = source;
        ev.mDisplayId = displayId;
        ev.mCharacters = characters;
        return ev;
    }

    @UnsupportedAppUsage
    public static KeyEvent obtain(long downTime, long eventTime, int action, int code, int repeat, int metaState, int deviceId, int scancode, int flags, int source, String characters) {
        return obtain(downTime, eventTime, action, code, repeat, metaState, deviceId, scancode, flags, source, -1, characters);
    }

    public static KeyEvent obtain(KeyEvent other) {
        KeyEvent ev = obtain();
        ev.mDownTime = other.mDownTime;
        ev.mEventTime = other.mEventTime;
        ev.mAction = other.mAction;
        ev.mKeyCode = other.mKeyCode;
        ev.mRepeatCount = other.mRepeatCount;
        ev.mMetaState = other.mMetaState;
        ev.mDeviceId = other.mDeviceId;
        ev.mScanCode = other.mScanCode;
        ev.mFlags = other.mFlags;
        ev.mSource = other.mSource;
        ev.mDisplayId = other.mDisplayId;
        ev.mCharacters = other.mCharacters;
        return ev;
    }

    @Override // android.view.InputEvent
    public KeyEvent copy() {
        return obtain(this);
    }

    @Override // android.view.InputEvent
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final void recycle() {
        super.recycle();
        this.mCharacters = null;
        synchronized (gRecyclerLock) {
            if (gRecyclerUsed < 10) {
                gRecyclerUsed++;
                this.mNext = gRecyclerTop;
                gRecyclerTop = this;
            }
        }
    }

    @Override // android.view.InputEvent
    public final void recycleIfNeededAfterDispatch() {
    }

    public static KeyEvent changeTimeRepeat(KeyEvent event, long eventTime, int newRepeat) {
        return new KeyEvent(event, eventTime, newRepeat);
    }

    public static KeyEvent changeTimeRepeat(KeyEvent event, long eventTime, int newRepeat, int newFlags) {
        KeyEvent ret = new KeyEvent(event);
        ret.mEventTime = eventTime;
        ret.mRepeatCount = newRepeat;
        ret.mFlags = newFlags;
        return ret;
    }

    private KeyEvent(KeyEvent origEvent, int action) {
        this.mDownTime = origEvent.mDownTime;
        this.mEventTime = origEvent.mEventTime;
        this.mAction = action;
        this.mKeyCode = origEvent.mKeyCode;
        this.mRepeatCount = origEvent.mRepeatCount;
        this.mMetaState = origEvent.mMetaState;
        this.mDeviceId = origEvent.mDeviceId;
        this.mSource = origEvent.mSource;
        this.mDisplayId = origEvent.mDisplayId;
        this.mScanCode = origEvent.mScanCode;
        this.mFlags = origEvent.mFlags;
    }

    public static KeyEvent changeAction(KeyEvent event, int action) {
        return new KeyEvent(event, action);
    }

    public static KeyEvent changeFlags(KeyEvent event, int flags) {
        KeyEvent event2 = new KeyEvent(event);
        event2.mFlags = flags;
        return event2;
    }

    @Override // android.view.InputEvent
    public final boolean isTainted() {
        return (this.mFlags & Integer.MIN_VALUE) != 0;
    }

    @Override // android.view.InputEvent
    public final void setTainted(boolean tainted) {
        int i = this.mFlags;
        this.mFlags = tainted ? i | Integer.MIN_VALUE : i & Integer.MAX_VALUE;
    }

    @UnsupportedAppUsage
    @Deprecated
    public final boolean isDown() {
        return this.mAction == 0;
    }

    public final boolean isSystem() {
        return isSystemKey(this.mKeyCode);
    }

    public final boolean isWakeKey() {
        return isWakeKey(this.mKeyCode);
    }

    public static final boolean isGamepadButton(int keyCode) {
        switch (keyCode) {
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
                return true;
            default:
                switch (keyCode) {
                    case 188:
                    case 189:
                    case 190:
                    case 191:
                    case 192:
                    case 193:
                    case 194:
                    case 195:
                    case 196:
                    case 197:
                    case 198:
                    case 199:
                    case 200:
                    case 201:
                    case 202:
                    case 203:
                        return true;
                    default:
                        return false;
                }
        }
    }

    @UnsupportedAppUsage
    public static final boolean isConfirmKey(int keyCode) {
        if (keyCode == 23 || keyCode == 62 || keyCode == 66 || keyCode == 160) {
            return true;
        }
        return false;
    }

    public static final boolean isMediaSessionKey(int keyCode) {
        if (keyCode == 79 || keyCode == 130 || keyCode == 126 || keyCode == 127) {
            return true;
        }
        switch (keyCode) {
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
                return true;
            default:
                return false;
        }
    }

    public static final boolean isSystemKey(int keyCode) {
        if (keyCode == 2 || keyCode == 3 || keyCode == 4 || keyCode == 5 || keyCode == 6 || keyCode == 79 || keyCode == 80 || keyCode == 82 || keyCode == 130 || keyCode == 164 || keyCode == 126 || keyCode == 127) {
            return true;
        }
        switch (keyCode) {
            case 24:
            case 25:
            case 26:
            case 27:
                return true;
            default:
                switch (keyCode) {
                    case 84:
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                    case 89:
                    case 90:
                    case 91:
                        return true;
                    default:
                        switch (keyCode) {
                            case 220:
                            case 221:
                            case 222:
                                return true;
                            default:
                                switch (keyCode) {
                                    case 280:
                                    case 281:
                                    case 282:
                                    case 283:
                                        return true;
                                    default:
                                        return false;
                                }
                        }
                }
        }
    }

    public static final boolean isWakeKey(int keyCode) {
        if (keyCode == 82 || keyCode == 224 || keyCode == 225) {
            return true;
        }
        switch (keyCode) {
            case 265:
            case 266:
            case 267:
                return true;
            default:
                return false;
        }
    }

    public static final boolean isMetaKey(int keyCode) {
        return keyCode == 117 || keyCode == 118;
    }

    public static final boolean isAltKey(int keyCode) {
        return keyCode == 57 || keyCode == 58;
    }

    @Override // android.view.InputEvent
    public final int getDeviceId() {
        return this.mDeviceId;
    }

    @Override // android.view.InputEvent
    public final int getSource() {
        return this.mSource;
    }

    @Override // android.view.InputEvent
    public final void setSource(int source) {
        this.mSource = source;
    }

    @Override // android.view.InputEvent
    public final int getDisplayId() {
        return this.mDisplayId;
    }

    @Override // android.view.InputEvent
    public final void setDisplayId(int displayId) {
        this.mDisplayId = displayId;
    }

    public final int getMetaState() {
        return this.mMetaState;
    }

    public final int getModifiers() {
        return normalizeMetaState(this.mMetaState) & META_MODIFIER_MASK;
    }

    public final void setFlags(int newFlags) {
        this.mFlags = newFlags;
    }

    public final int getFlags() {
        return this.mFlags;
    }

    public static int getModifierMetaStateMask() {
        return META_MODIFIER_MASK;
    }

    public static boolean isModifierKey(int keyCode) {
        if (keyCode == 63 || keyCode == 78 || keyCode == 113 || keyCode == 114) {
            return true;
        }
        switch (keyCode) {
            case 57:
            case 58:
            case 59:
            case 60:
                return true;
            default:
                switch (keyCode) {
                    case 117:
                    case 118:
                    case 119:
                        return true;
                    default:
                        return false;
                }
        }
    }

    public static int normalizeMetaState(int metaState) {
        if ((metaState & 192) != 0) {
            metaState |= 1;
        }
        if ((metaState & 48) != 0) {
            metaState |= 2;
        }
        if ((metaState & 24576) != 0) {
            metaState |= 4096;
        }
        if ((393216 & metaState) != 0) {
            metaState |= 65536;
        }
        if ((metaState & 256) != 0) {
            metaState |= 1048576;
        }
        if ((metaState & 512) != 0) {
            metaState |= 2;
        }
        if ((metaState & 1024) != 0) {
            metaState |= 4;
        }
        return META_ALL_MASK & metaState;
    }

    public static boolean metaStateHasNoModifiers(int metaState) {
        return (normalizeMetaState(metaState) & META_MODIFIER_MASK) == 0;
    }

    public static boolean metaStateHasModifiers(int metaState, int modifiers) {
        if ((META_INVALID_MODIFIER_MASK & modifiers) != 0) {
            throw new IllegalArgumentException("modifiers must not contain META_CAPS_LOCK_ON, META_NUM_LOCK_ON, META_SCROLL_LOCK_ON, META_CAP_LOCKED, META_ALT_LOCKED, META_SYM_LOCKED, or META_SELECTING");
        } else if (metaStateFilterDirectionalModifiers(metaStateFilterDirectionalModifiers(metaStateFilterDirectionalModifiers(metaStateFilterDirectionalModifiers(normalizeMetaState(metaState) & META_MODIFIER_MASK, modifiers, 1, 64, 128), modifiers, 2, 16, 32), modifiers, 4096, 8192, 16384), modifiers, 65536, 131072, 262144) == modifiers) {
            return true;
        } else {
            return false;
        }
    }

    private static int metaStateFilterDirectionalModifiers(int metaState, int modifiers, int basic, int left, int right) {
        boolean wantLeftOrRight = true;
        boolean wantBasic = (modifiers & basic) != 0;
        int directional = left | right;
        if ((modifiers & directional) == 0) {
            wantLeftOrRight = false;
        }
        if (wantBasic) {
            if (!wantLeftOrRight) {
                return (~directional) & metaState;
            }
            throw new IllegalArgumentException("modifiers must not contain " + metaStateToString(basic) + " combined with " + metaStateToString(left) + " or " + metaStateToString(right));
        } else if (wantLeftOrRight) {
            return (~basic) & metaState;
        } else {
            return metaState;
        }
    }

    public final boolean hasNoModifiers() {
        return metaStateHasNoModifiers(this.mMetaState);
    }

    public final boolean hasModifiers(int modifiers) {
        return metaStateHasModifiers(this.mMetaState, modifiers);
    }

    public final boolean isAltPressed() {
        return (this.mMetaState & 2) != 0;
    }

    public final boolean isShiftPressed() {
        return (this.mMetaState & 1) != 0;
    }

    public final boolean isSymPressed() {
        return (this.mMetaState & 4) != 0;
    }

    public final boolean isCtrlPressed() {
        return (this.mMetaState & 4096) != 0;
    }

    public final boolean isMetaPressed() {
        return (this.mMetaState & 65536) != 0;
    }

    public final boolean isFunctionPressed() {
        return (this.mMetaState & 8) != 0;
    }

    public final boolean isCapsLockOn() {
        return (this.mMetaState & 1048576) != 0;
    }

    public final boolean isNumLockOn() {
        return (this.mMetaState & 2097152) != 0;
    }

    public final boolean isScrollLockOn() {
        return (this.mMetaState & 4194304) != 0;
    }

    public final int getAction() {
        return this.mAction;
    }

    public final boolean isCanceled() {
        return (this.mFlags & 32) != 0;
    }

    @Override // android.view.InputEvent
    public final void cancel() {
        this.mFlags |= 32;
    }

    public final void startTracking() {
        this.mFlags |= 1073741824;
    }

    public final boolean isTracking() {
        return (this.mFlags & 512) != 0;
    }

    public final boolean isLongPress() {
        return (this.mFlags & 128) != 0;
    }

    public final int getKeyCode() {
        return this.mKeyCode;
    }

    @Deprecated
    public final String getCharacters() {
        return this.mCharacters;
    }

    public final int getScanCode() {
        return this.mScanCode;
    }

    public final int getRepeatCount() {
        return this.mRepeatCount;
    }

    public final void setTime(long downTime, long eventTime) {
        this.mDownTime = downTime;
        this.mEventTime = eventTime;
    }

    public final long getDownTime() {
        return this.mDownTime;
    }

    @Override // android.view.InputEvent
    public final long getEventTime() {
        return this.mEventTime;
    }

    @Override // android.view.InputEvent
    public final long getEventTimeNano() {
        return this.mEventTime * TimeUtils.NANOS_PER_MS;
    }

    @Deprecated
    public final int getKeyboardDevice() {
        return this.mDeviceId;
    }

    public final KeyCharacterMap getKeyCharacterMap() {
        return KeyCharacterMap.load(this.mDeviceId);
    }

    public char getDisplayLabel() {
        return getKeyCharacterMap().getDisplayLabel(this.mKeyCode);
    }

    public int getUnicodeChar() {
        return getUnicodeChar(this.mMetaState);
    }

    public int getUnicodeChar(int metaState) {
        return getKeyCharacterMap().get(this.mKeyCode, metaState);
    }

    @Deprecated
    public boolean getKeyData(KeyCharacterMap.KeyData results) {
        return getKeyCharacterMap().getKeyData(this.mKeyCode, results);
    }

    public char getMatch(char[] chars) {
        return getMatch(chars, 0);
    }

    public char getMatch(char[] chars, int metaState) {
        return getKeyCharacterMap().getMatch(this.mKeyCode, chars, metaState);
    }

    public char getNumber() {
        return getKeyCharacterMap().getNumber(this.mKeyCode);
    }

    public boolean isPrintingKey() {
        return getKeyCharacterMap().isPrintingKey(this.mKeyCode);
    }

    @Deprecated
    public final boolean dispatch(Callback receiver) {
        return dispatch(receiver, null, null);
    }

    public final boolean dispatch(Callback receiver, DispatcherState state, Object target) {
        int i = this.mAction;
        if (i == 0) {
            this.mFlags &= -1073741825;
            boolean res = receiver.onKeyDown(this.mKeyCode, this);
            if (state == null) {
                return res;
            }
            if (res && this.mRepeatCount == 0 && (this.mFlags & 1073741824) != 0) {
                state.startTracking(this, target);
                return res;
            } else if (!isLongPress() || !state.isTracking(this)) {
                return res;
            } else {
                try {
                    if (!receiver.onKeyLongPress(this.mKeyCode, this)) {
                        return res;
                    }
                    state.performedLongPress(this);
                    return true;
                } catch (AbstractMethodError e) {
                    return res;
                }
            }
        } else if (i == 1) {
            if (state != null) {
                state.handleUpEvent(this);
            }
            return receiver.onKeyUp(this.mKeyCode, this);
        } else if (i != 2) {
            return false;
        } else {
            int count = this.mRepeatCount;
            int code = this.mKeyCode;
            if (receiver.onKeyMultiple(code, count, this)) {
                return true;
            }
            if (code == 0) {
                return false;
            }
            this.mAction = 0;
            this.mRepeatCount = 0;
            boolean handled = receiver.onKeyDown(code, this);
            if (handled) {
                this.mAction = 1;
                receiver.onKeyUp(code, this);
            }
            this.mAction = 2;
            this.mRepeatCount = count;
            return handled;
        }
    }

    public static class DispatcherState {
        SparseIntArray mActiveLongPresses = new SparseIntArray();
        int mDownKeyCode;
        Object mDownTarget;

        public void reset() {
            this.mDownKeyCode = 0;
            this.mDownTarget = null;
            this.mActiveLongPresses.clear();
        }

        public void reset(Object target) {
            if (this.mDownTarget == target) {
                this.mDownKeyCode = 0;
                this.mDownTarget = null;
            }
        }

        public void startTracking(KeyEvent event, Object target) {
            if (event.getAction() == 0) {
                this.mDownKeyCode = event.getKeyCode();
                this.mDownTarget = target;
                return;
            }
            throw new IllegalArgumentException("Can only start tracking on a down event");
        }

        public boolean isTracking(KeyEvent event) {
            return this.mDownKeyCode == event.getKeyCode();
        }

        public void performedLongPress(KeyEvent event) {
            this.mActiveLongPresses.put(event.getKeyCode(), 1);
        }

        public void handleUpEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            int index = this.mActiveLongPresses.indexOfKey(keyCode);
            if (index >= 0) {
                KeyEvent.access$076(event, 288);
                this.mActiveLongPresses.removeAt(index);
            }
            if (this.mDownKeyCode == keyCode) {
                KeyEvent.access$076(event, 512);
                this.mDownKeyCode = 0;
                this.mDownTarget = null;
            }
        }
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("KeyEvent { action=");
        msg.append(actionToString(this.mAction));
        msg.append(", keyCode=");
        msg.append(keyCodeToString(this.mKeyCode));
        msg.append(", scanCode=");
        msg.append(this.mScanCode);
        if (this.mCharacters != null) {
            msg.append(", characters=\"");
            msg.append(this.mCharacters);
            msg.append("\"");
        }
        msg.append(", metaState=");
        msg.append(metaStateToString(this.mMetaState));
        msg.append(", flags=0x");
        msg.append(Integer.toHexString(this.mFlags));
        msg.append(", repeatCount=");
        msg.append(this.mRepeatCount);
        msg.append(", eventTime=");
        msg.append(this.mEventTime);
        msg.append(", downTime=");
        msg.append(this.mDownTime);
        msg.append(", deviceId=");
        msg.append(this.mDeviceId);
        msg.append(", source=0x");
        msg.append(Integer.toHexString(this.mSource));
        msg.append(", displayId=");
        msg.append(this.mDisplayId);
        msg.append(" }");
        return msg.toString();
    }

    public static String actionToString(int action) {
        if (action == 0) {
            return "ACTION_DOWN";
        }
        if (action == 1) {
            return "ACTION_UP";
        }
        if (action != 2) {
            return Integer.toString(action);
        }
        return "ACTION_MULTIPLE";
    }

    public static String keyCodeToString(int keyCode) {
        String symbolicName = nativeKeyCodeToString(keyCode);
        if (symbolicName == null) {
            return Integer.toString(keyCode);
        }
        return LABEL_PREFIX + symbolicName;
    }

    public static int keyCodeFromString(String symbolicName) {
        try {
            int keyCode = Integer.parseInt(symbolicName);
            if (keyCodeIsValid(keyCode)) {
                return keyCode;
            }
        } catch (NumberFormatException e) {
        }
        if (symbolicName.startsWith(LABEL_PREFIX)) {
            symbolicName = symbolicName.substring(LABEL_PREFIX.length());
        }
        int keyCode2 = nativeKeyCodeFromString(symbolicName);
        if (keyCodeIsValid(keyCode2)) {
            return keyCode2;
        }
        return 0;
    }

    private static boolean keyCodeIsValid(int keyCode) {
        return (keyCode >= 0 && keyCode <= 288) || (keyCode >= 308 && keyCode <= 746);
    }

    public static String metaStateToString(int metaState) {
        if (metaState == 0) {
            return WifiEnterpriseConfig.ENGINE_DISABLE;
        }
        StringBuilder result = null;
        int i = 0;
        while (metaState != 0) {
            boolean isSet = (metaState & 1) != 0;
            metaState >>>= 1;
            if (isSet) {
                String name = META_SYMBOLIC_NAMES[i];
                if (result != null) {
                    result.append('|');
                    result.append(name);
                } else if (metaState == 0) {
                    return name;
                } else {
                    result = new StringBuilder(name);
                }
            }
            i++;
        }
        return result.toString();
    }

    public static KeyEvent createFromParcelBody(Parcel in) {
        return new KeyEvent(in);
    }

    private KeyEvent(Parcel in) {
        this.mDeviceId = in.readInt();
        this.mSource = in.readInt();
        this.mDisplayId = in.readInt();
        this.mAction = in.readInt();
        this.mKeyCode = in.readInt();
        this.mRepeatCount = in.readInt();
        this.mMetaState = in.readInt();
        this.mScanCode = in.readInt();
        this.mFlags = in.readInt();
        this.mDownTime = in.readLong();
        this.mEventTime = in.readLong();
        this.mCharacters = in.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(2);
        out.writeInt(this.mDeviceId);
        out.writeInt(this.mSource);
        out.writeInt(this.mDisplayId);
        out.writeInt(this.mAction);
        out.writeInt(this.mKeyCode);
        out.writeInt(this.mRepeatCount);
        out.writeInt(this.mMetaState);
        out.writeInt(this.mScanCode);
        out.writeInt(this.mFlags);
        out.writeLong(this.mDownTime);
        out.writeLong(this.mEventTime);
        out.writeString(this.mCharacters);
    }
}

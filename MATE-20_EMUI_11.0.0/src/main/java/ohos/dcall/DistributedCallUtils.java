package ohos.dcall;

import ohos.hiviewdfx.HiLogLabel;

public class DistributedCallUtils {
    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_OFFHOOK = 2;
    public static final int CALL_STATE_RINGING = 1;
    public static final int CALL_STATE_UNKNOWN = -1;
    public static final int LOG_ID_DCALL = 218111744;
    public static final int MSG_ADD_CALL_OBSERVER = 2006;
    public static final int MSG_ANSWER_CALL = 1;
    public static final int MSG_CALL_BASE = 2000;
    public static final int MSG_CHECK_VIDEO_CALLING_ENABLED = 2009;
    public static final int MSG_DCALL_ABILITY_BASE = 0;
    public static final int MSG_DCALL_BASE = 0;
    public static final int MSG_DIAL = 2002;
    public static final int MSG_DISCONNECT_CALL = 2;
    public static final int MSG_DISPLAY_CALL_SCREEN = 2003;
    public static final int MSG_GET_CALL_STATE = 2005;
    public static final int MSG_HAS_CALL = 2001;
    public static final int MSG_INPUT_DIALER_SPECIAL_CODE = 2010;
    public static final int MSG_IS_NEW_CALL_ALLOWED = 7;
    public static final int MSG_MUTE_RINGER = 2004;
    public static final int MSG_ON_CALL_AUDIO_STATE_CHANGED = 1;
    public static final int MSG_ON_CALL_COMPLETED = 9;
    public static final int MSG_ON_CALL_CREATED = 2;
    public static final int MSG_ON_CALL_DELETED = 3;
    public static final int MSG_ON_CALL_EVENT_CHANGED = 10;
    public static final int MSG_ON_INFO_CHANGED = 7;
    public static final int MSG_ON_IS_NEW_CALL_ALLOWED_CHANGED = 4;
    public static final int MSG_ON_POST_DIAL_WAIT = 8;
    public static final int MSG_ON_RINGTONE_MUTED = 5;
    public static final int MSG_ON_STATE_CHANGED = 6;
    public static final int MSG_POST_DIAL_DTMF_CONTINUE = 5;
    public static final int MSG_REJECT_CALL = 6;
    public static final int MSG_REMOVE_CALL_OBSERVER = 2007;
    public static final int MSG_SET_AUDIO_DEVICE = 9;
    public static final int MSG_SET_MUTED = 8;
    public static final int MSG_START_DTMF_TONE = 3;
    public static final int MSG_STOP_DTMF_TONE = 4;
    public static final int RESULT_ERROR = -1;
    public static final int RESULT_PERMISSION_DENY = -2;
    public static final int RESULT_SUCCESS = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LOG_ID_DCALL, "DistributedCallUtils");

    public static final String msgCodeToString(int i) {
        switch (i) {
            case 1:
                return "MSG_ON_CALL_AUDIO_STATE_CHANGED";
            case 2:
                return "MSG_ON_CALL_CREATED";
            case 3:
                return "MSG_ON_CALL_DELETED";
            case 4:
                return "MSG_ON_IS_NEW_CALL_ALLOWED_CHANGED";
            case 5:
                return "MSG_ON_RINGTONE_MUTED";
            case 6:
                return "MSG_ON_STATE_CHANGED";
            case 7:
                return "MSG_ON_INFO_CHANGED";
            case 8:
                return "MSG_ON_POST_DIAL_WAIT";
            case 9:
                return "MSG_ON_CALL_COMPLETED";
            case 10:
                return "MSG_ON_CALL_EVENT_CHANGED";
            default:
                return "Unsupported Msg";
        }
    }
}

package com.android.internal.view;

public final class InputMethodClient {
    public static final int START_INPUT_REASON_ACTIVATED_BY_IMMS = 7;
    public static final int START_INPUT_REASON_APP_CALLED_RESTART_INPUT_API = 3;
    public static final int START_INPUT_REASON_BOUND_TO_IMMS = 5;
    public static final int START_INPUT_REASON_CHECK_FOCUS = 4;
    public static final int START_INPUT_REASON_DEACTIVATED_BY_IMMS = 8;
    public static final int START_INPUT_REASON_SESSION_CREATED_BY_IME = 9;
    public static final int START_INPUT_REASON_UNBOUND_FROM_IMMS = 6;
    public static final int START_INPUT_REASON_UNSPECIFIED = 0;
    public static final int START_INPUT_REASON_WINDOW_FOCUS_GAIN = 1;
    public static final int START_INPUT_REASON_WINDOW_FOCUS_GAIN_REPORT_ONLY = 2;
    public static final int UNBIND_REASON_DISCONNECT_IME = 3;
    public static final int UNBIND_REASON_NO_IME = 4;
    public static final int UNBIND_REASON_SWITCH_CLIENT = 1;
    public static final int UNBIND_REASON_SWITCH_IME = 2;
    public static final int UNBIND_REASON_SWITCH_IME_FAILED = 5;
    public static final int UNBIND_REASON_SWITCH_USER = 6;
    public static final int UNBIND_REASON_UNSPECIFIED = 0;

    public static String getStartInputReason(int reason) {
        switch (reason) {
            case 0:
                return "UNSPECIFIED";
            case 1:
                return "WINDOW_FOCUS_GAIN";
            case 2:
                return "WINDOW_FOCUS_GAIN_REPORT_ONLY";
            case 3:
                return "APP_CALLED_RESTART_INPUT_API";
            case 4:
                return "CHECK_FOCUS";
            case 5:
                return "BOUND_TO_IMMS";
            case 6:
                return "UNBOUND_FROM_IMMS";
            case 7:
                return "ACTIVATED_BY_IMMS";
            case 8:
                return "DEACTIVATED_BY_IMMS";
            case 9:
                return "SESSION_CREATED_BY_IME";
            default:
                return "Unknown=" + reason;
        }
    }

    public static String getUnbindReason(int reason) {
        switch (reason) {
            case 0:
                return "UNSPECIFIED";
            case 1:
                return "SWITCH_CLIENT";
            case 2:
                return "SWITCH_IME";
            case 3:
                return "DISCONNECT_IME";
            case 4:
                return "NO_IME";
            case 5:
                return "SWITCH_IME_FAILED";
            case 6:
                return "SWITCH_USER";
            default:
                return "Unknown=" + reason;
        }
    }

    public static String softInputModeToString(int softInputMode) {
        StringBuilder sb = new StringBuilder();
        int state = softInputMode & 15;
        int adjust = softInputMode & 240;
        boolean isForwardNav = (softInputMode & 256) != 0;
        switch (state) {
            case 0:
                sb.append("STATE_UNSPECIFIED");
                break;
            case 1:
                sb.append("STATE_UNCHANGED");
                break;
            case 2:
                sb.append("STATE_HIDDEN");
                break;
            case 3:
                sb.append("STATE_ALWAYS_HIDDEN");
                break;
            case 4:
                sb.append("STATE_VISIBLE");
                break;
            case 5:
                sb.append("STATE_ALWAYS_VISIBLE");
                break;
            default:
                sb.append("STATE_UNKNOWN(");
                sb.append(state);
                sb.append(")");
                break;
        }
        switch (adjust) {
            case 0:
                sb.append("|ADJUST_UNSPECIFIED");
                break;
            case 16:
                sb.append("|ADJUST_RESIZE");
                break;
            case 32:
                sb.append("|ADJUST_PAN");
                break;
            case 48:
                sb.append("|ADJUST_NOTHING");
                break;
            default:
                sb.append("|ADJUST_UNKNOWN(");
                sb.append(adjust);
                sb.append(")");
                break;
        }
        if (isForwardNav) {
            sb.append("|IS_FORWARD_NAVIGATION");
        }
        return sb.toString();
    }
}

package com.android.internal.view;

public final class InputMethodClient {
    public static final int START_INPUT_REASON_ACTIVATED_BY_IMMS = 7;
    public static final int START_INPUT_REASON_APP_CALLED_RESTART_INPUT_API = 3;
    public static final int START_INPUT_REASON_BOUND_TO_IMMS = 5;
    public static final int START_INPUT_REASON_CHECK_FOCUS = 4;
    public static final int START_INPUT_REASON_DEACTIVATED_BY_IMMS = 8;
    public static final int START_INPUT_REASON_UNBOUND_FROM_IMMS = 6;
    public static final int START_INPUT_REASON_UNSPECIFIED = 0;
    public static final int START_INPUT_REASON_WINDOW_FOCUS_GAIN = 1;
    public static final int START_INPUT_REASON_WINDOW_FOCUS_GAIN_REPORT_ONLY = 2;
    public static final int UNBIND_REASON_DISCONNECT_IME = 3;
    public static final int UNBIND_REASON_NO_IME = 4;
    public static final int UNBIND_REASON_RESET_IME = 6;
    public static final int UNBIND_REASON_SWITCH_CLIENT = 1;
    public static final int UNBIND_REASON_SWITCH_IME = 2;
    public static final int UNBIND_REASON_SWITCH_IME_FAILED = 5;
    public static final int UNBIND_REASON_UNSPECIFIED = 0;

    public static String getStartInputReason(int reason) {
        switch (reason) {
            case START_INPUT_REASON_UNSPECIFIED /*0*/:
                return "UNSPECIFIED";
            case UNBIND_REASON_SWITCH_CLIENT /*1*/:
                return "WINDOW_FOCUS_GAIN";
            case UNBIND_REASON_SWITCH_IME /*2*/:
                return "WINDOW_FOCUS_GAIN_REPORT_ONLY";
            case UNBIND_REASON_DISCONNECT_IME /*3*/:
                return "APP_CALLED_RESTART_INPUT_API";
            case UNBIND_REASON_NO_IME /*4*/:
                return "CHECK_FOCUS";
            case UNBIND_REASON_SWITCH_IME_FAILED /*5*/:
                return "BOUND_TO_IMMS";
            case UNBIND_REASON_RESET_IME /*6*/:
                return "UNBOUND_FROM_IMMS";
            case START_INPUT_REASON_ACTIVATED_BY_IMMS /*7*/:
                return "ACTIVATED_BY_IMMS";
            case START_INPUT_REASON_DEACTIVATED_BY_IMMS /*8*/:
                return "DEACTIVATED_BY_IMMS";
            default:
                return "Unknown=" + reason;
        }
    }

    public static String getUnbindReason(int reason) {
        switch (reason) {
            case START_INPUT_REASON_UNSPECIFIED /*0*/:
                return "UNSPECIFIED";
            case UNBIND_REASON_SWITCH_CLIENT /*1*/:
                return "SWITCH_CLIENT";
            case UNBIND_REASON_SWITCH_IME /*2*/:
                return "SWITCH_IME";
            case UNBIND_REASON_DISCONNECT_IME /*3*/:
                return "DISCONNECT_IME";
            case UNBIND_REASON_NO_IME /*4*/:
                return "NO_IME";
            case UNBIND_REASON_SWITCH_IME_FAILED /*5*/:
                return "SWITCH_IME_FAILED";
            case UNBIND_REASON_RESET_IME /*6*/:
                return "RESET_IME";
            default:
                return "Unknown=" + reason;
        }
    }
}

package com.android.internal.inputmethod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface StartInputReason {
    public static final int ACTIVATED_BY_IMMS = 7;
    public static final int APP_CALLED_RESTART_INPUT_API = 3;
    public static final int BOUND_TO_IMMS = 5;
    public static final int CHECK_FOCUS = 4;
    public static final int DEACTIVATED_BY_IMMS = 8;
    public static final int SESSION_CREATED_BY_IME = 9;
    public static final int UNBOUND_FROM_IMMS = 6;
    public static final int UNSPECIFIED = 0;
    public static final int WINDOW_FOCUS_GAIN = 1;
    public static final int WINDOW_FOCUS_GAIN_REPORT_ONLY = 2;
}

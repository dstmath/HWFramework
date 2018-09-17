package com.huawei.android.view.inputmethod;

import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputMethodManagerEx {
    public static void toggleSoftInput(InputMethodManager imm, int showFlags, int hideFlags) {
        imm.toggleSoftInput(showFlags, hideFlags);
    }

    public static void windowDismissed(InputMethodManager imm, IBinder appWindowToken) {
        imm.windowDismissed(appWindowToken);
    }

    public static void onPreWindowFocus(InputMethodManager imm, View rootView, boolean hasWindowFocus) {
        imm.onPreWindowFocus(rootView, hasWindowFocus);
    }

    public static InputMethodManager getInstance() {
        return InputMethodManager.getInstance();
    }

    public static int getInputMethodWindowVisibleHeight(InputMethodManager imm) {
        return imm.getInputMethodWindowVisibleHeight();
    }
}

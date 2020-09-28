package com.huawei.android.view.inputmethod;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import com.huawei.android.inputmethod.HwInputMethodManager;

public class InputMethodManagerEx {
    private static final String TAG = InputMethodManagerEx.class.getSimpleName();

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

    public static void registerInputMethodListener(HwInputMethodListenerEx listener) {
        if (listener == null) {
            Log.e(TAG, "registerInputMethodListener listener is null.");
        } else {
            HwInputMethodManager.registerInputMethodListener(listener.getInnerListener());
        }
    }

    public static void unregisterInputMethodListener() {
        HwInputMethodManager.unregisterInputMethodListener();
    }

    public static void onReceivedInputContent(String content) {
        HwInputMethodManager.onReceivedInputContent(content);
    }

    public static void onReceivedComposingText(String content) {
        HwInputMethodManager.onReceivedComposingText(content);
    }

    public static InputConnection getCurInputConnection() {
        return HwInputMethodManager.getCurInputConnection();
    }

    public static EditorInfo getCurrentInputStyle() {
        return HwInputMethodManager.getCurrentInputStyle();
    }
}

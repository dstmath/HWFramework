package com.huawei.server.inputmethod;

import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.inputmethod.InputMethodManagerInternal;

public class InputMethodManagerInternalEx {
    private static final String TAG = "InputMethodManagerInternalEx";
    private static InputMethodManagerInternal sInputMethodService = ((InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class));

    public static void hideCurrentInputMethod() {
        InputMethodManagerInternal inputMethodManagerInternal = sInputMethodService;
        if (inputMethodManagerInternal != null) {
            inputMethodManagerInternal.hideCurrentInputMethod();
        } else {
            Log.e(TAG, "Failed to execute hideCurrentInputMethod");
        }
    }
}

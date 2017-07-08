package com.android.internal.widget;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class PasswordEntryKeyboardView extends KeyboardView {
    static final int KEYCODE_F1 = -103;
    static final int KEYCODE_NEXT_LANGUAGE = -104;
    static final int KEYCODE_OPTIONS = -100;
    static final int KEYCODE_SHIFT_LONGPRESS = -101;
    static final int KEYCODE_VOICE = -102;

    public PasswordEntryKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordEntryKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PasswordEntryKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean setShifted(boolean shifted) {
        boolean result = super.setShifted(shifted);
        for (int index : getKeyboard().getShiftKeyIndices()) {
            invalidateKey(index);
        }
        return result;
    }
}

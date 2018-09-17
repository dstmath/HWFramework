package com.android.internal.widget;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewRootImpl;
import com.android.internal.R;

public class PasswordEntryKeyboardHelper implements OnKeyboardActionListener {
    public static final int KEYBOARD_MODE_ALPHA = 0;
    public static final int KEYBOARD_MODE_NUMERIC = 1;
    private static final int KEYBOARD_STATE_CAPSLOCK = 2;
    private static final int KEYBOARD_STATE_NORMAL = 0;
    private static final int KEYBOARD_STATE_SHIFTED = 1;
    private static final int NUMERIC = 0;
    private static final int QWERTY = 1;
    private static final int QWERTY_SHIFTED = 2;
    private static final int SYMBOLS = 3;
    private static final int SYMBOLS_SHIFTED = 4;
    private static final String TAG = "PasswordEntryKeyboardHelper";
    private final Context mContext;
    private boolean mEnableHaptics;
    private int mKeyboardMode;
    private int mKeyboardState;
    private final KeyboardView mKeyboardView;
    int[] mLayouts;
    private PasswordEntryKeyboard mNumericKeyboard;
    private PasswordEntryKeyboard mQwertyKeyboard;
    private PasswordEntryKeyboard mQwertyKeyboardShifted;
    private PasswordEntryKeyboard mSymbolsKeyboard;
    private PasswordEntryKeyboard mSymbolsKeyboardShifted;
    private final View mTargetView;
    private boolean mUsingScreenWidth;
    private long[] mVibratePattern;

    public PasswordEntryKeyboardHelper(Context context, KeyboardView keyboardView, View targetView) {
        this(context, keyboardView, targetView, true, null);
    }

    public PasswordEntryKeyboardHelper(Context context, KeyboardView keyboardView, View targetView, boolean useFullScreenWidth) {
        this(context, keyboardView, targetView, useFullScreenWidth, null);
    }

    public PasswordEntryKeyboardHelper(Context context, KeyboardView keyboardView, View targetView, boolean useFullScreenWidth, int[] layouts) {
        this.mKeyboardMode = NUMERIC;
        this.mKeyboardState = NUMERIC;
        this.mEnableHaptics = false;
        this.mLayouts = new int[]{R.xml.password_kbd_numeric, R.xml.password_kbd_qwerty, R.xml.password_kbd_qwerty_shifted, R.xml.password_kbd_symbols, R.xml.password_kbd_symbols_shift};
        this.mContext = context;
        this.mTargetView = targetView;
        this.mKeyboardView = keyboardView;
        this.mKeyboardView.setOnKeyboardActionListener(this);
        this.mUsingScreenWidth = useFullScreenWidth;
        if (layouts != null) {
            if (layouts.length != this.mLayouts.length) {
                throw new RuntimeException("Wrong number of layouts");
            }
            for (int i = NUMERIC; i < this.mLayouts.length; i += QWERTY) {
                this.mLayouts[i] = layouts[i];
            }
        }
        createKeyboards();
    }

    public void createKeyboards() {
        LayoutParams lp = this.mKeyboardView.getLayoutParams();
        if (this.mUsingScreenWidth || lp.width == -1) {
            createKeyboardsWithDefaultWidth();
        } else {
            createKeyboardsWithSpecificSize(lp.width, lp.height);
        }
    }

    public void setEnableHaptics(boolean enabled) {
        this.mEnableHaptics = enabled;
    }

    public boolean isAlpha() {
        return this.mKeyboardMode == 0;
    }

    private void createKeyboardsWithSpecificSize(int width, int height) {
        this.mNumericKeyboard = new PasswordEntryKeyboard(this.mContext, this.mLayouts[NUMERIC], width, height);
        this.mQwertyKeyboard = new PasswordEntryKeyboard(this.mContext, this.mLayouts[QWERTY], (int) R.id.mode_normal, width, height);
        this.mQwertyKeyboard.enableShiftLock();
        this.mQwertyKeyboardShifted = new PasswordEntryKeyboard(this.mContext, this.mLayouts[QWERTY_SHIFTED], (int) R.id.mode_normal, width, height);
        this.mQwertyKeyboardShifted.enableShiftLock();
        this.mQwertyKeyboardShifted.setShifted(true);
        this.mSymbolsKeyboard = new PasswordEntryKeyboard(this.mContext, this.mLayouts[SYMBOLS], width, height);
        this.mSymbolsKeyboard.enableShiftLock();
        this.mSymbolsKeyboardShifted = new PasswordEntryKeyboard(this.mContext, this.mLayouts[SYMBOLS_SHIFTED], width, height);
        this.mSymbolsKeyboardShifted.enableShiftLock();
        this.mSymbolsKeyboardShifted.setShifted(true);
    }

    private void createKeyboardsWithDefaultWidth() {
        this.mNumericKeyboard = new PasswordEntryKeyboard(this.mContext, this.mLayouts[NUMERIC]);
        this.mQwertyKeyboard = new PasswordEntryKeyboard(this.mContext, this.mLayouts[QWERTY], R.id.mode_normal);
        this.mQwertyKeyboard.enableShiftLock();
        this.mQwertyKeyboardShifted = new PasswordEntryKeyboard(this.mContext, this.mLayouts[QWERTY_SHIFTED], R.id.mode_normal);
        this.mQwertyKeyboardShifted.enableShiftLock();
        this.mQwertyKeyboardShifted.setShifted(true);
        this.mSymbolsKeyboard = new PasswordEntryKeyboard(this.mContext, this.mLayouts[SYMBOLS]);
        this.mSymbolsKeyboard.enableShiftLock();
        this.mSymbolsKeyboardShifted = new PasswordEntryKeyboard(this.mContext, this.mLayouts[SYMBOLS_SHIFTED]);
        this.mSymbolsKeyboardShifted.enableShiftLock();
        this.mSymbolsKeyboardShifted.setShifted(true);
    }

    public void setKeyboardMode(int mode) {
        switch (mode) {
            case NUMERIC /*0*/:
                this.mKeyboardView.setKeyboard(this.mQwertyKeyboard);
                this.mKeyboardState = NUMERIC;
                boolean visiblePassword = System.getInt(this.mContext.getContentResolver(), "show_password", QWERTY) != 0;
                KeyboardView keyboardView = this.mKeyboardView;
                if (visiblePassword) {
                    keyboardView.setPreviewEnabled(false);
                    break;
                }
                keyboardView.setPreviewEnabled(false);
            case QWERTY /*1*/:
                this.mKeyboardView.setKeyboard(this.mNumericKeyboard);
                this.mKeyboardState = NUMERIC;
                this.mKeyboardView.setPreviewEnabled(false);
                break;
        }
        this.mKeyboardMode = mode;
    }

    private void sendKeyEventsToTarget(int character) {
        ViewRootImpl viewRootImpl = this.mTargetView.getViewRootImpl();
        KeyCharacterMap load = KeyCharacterMap.load(-1);
        char[] cArr = new char[QWERTY];
        cArr[NUMERIC] = (char) character;
        KeyEvent[] events = load.getEvents(cArr);
        if (events != null) {
            int N = events.length;
            for (int i = NUMERIC; i < N; i += QWERTY) {
                KeyEvent event = events[i];
                viewRootImpl.dispatchInputEvent(KeyEvent.changeFlags(event, (event.getFlags() | QWERTY_SHIFTED) | SYMBOLS_SHIFTED));
            }
        }
    }

    public void sendDownUpKeyEvents(int keyEventCode) {
        long eventTime = SystemClock.uptimeMillis();
        ViewRootImpl viewRootImpl = this.mTargetView.getViewRootImpl();
        viewRootImpl.dispatchKeyFromIme(new KeyEvent(eventTime, eventTime, NUMERIC, keyEventCode, NUMERIC, NUMERIC, -1, NUMERIC, 6));
        viewRootImpl.dispatchKeyFromIme(new KeyEvent(eventTime, eventTime, QWERTY, keyEventCode, NUMERIC, NUMERIC, -1, NUMERIC, 6));
    }

    public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode == -5) {
            handleBackspace();
        } else if (primaryCode == -1) {
            handleShift();
        } else if (primaryCode == -3) {
            handleClose();
        } else if (primaryCode != -2 || this.mKeyboardView == null) {
            handleCharacter(primaryCode, keyCodes);
            if (this.mKeyboardState == QWERTY) {
                this.mKeyboardState = QWERTY_SHIFTED;
                handleShift();
            }
        } else {
            handleModeChange();
        }
    }

    public void setVibratePattern(int id) {
        int[] tmpArray = null;
        try {
            tmpArray = this.mContext.getResources().getIntArray(id);
        } catch (NotFoundException e) {
            if (id != 0) {
                Log.e(TAG, "Vibrate pattern missing", e);
            }
        }
        if (tmpArray == null) {
            this.mVibratePattern = null;
            return;
        }
        this.mVibratePattern = new long[tmpArray.length];
        for (int i = NUMERIC; i < tmpArray.length; i += QWERTY) {
            this.mVibratePattern[i] = (long) tmpArray[i];
        }
    }

    private void handleModeChange() {
        Keyboard current = this.mKeyboardView.getKeyboard();
        Keyboard next = null;
        if (current == this.mQwertyKeyboard || current == this.mQwertyKeyboardShifted) {
            next = this.mSymbolsKeyboard;
        } else if (current == this.mSymbolsKeyboard || current == this.mSymbolsKeyboardShifted) {
            next = this.mQwertyKeyboard;
        }
        if (next != null) {
            this.mKeyboardView.setKeyboard(next);
            this.mKeyboardState = NUMERIC;
        }
    }

    public void handleBackspace() {
        sendDownUpKeyEvents(67);
        performHapticFeedback();
    }

    private void handleShift() {
        boolean z = true;
        if (this.mKeyboardView != null) {
            Keyboard current = this.mKeyboardView.getKeyboard();
            Keyboard next = null;
            boolean isAlphaMode = current != this.mQwertyKeyboard ? current == this.mQwertyKeyboardShifted : true;
            if (this.mKeyboardState == 0) {
                int i;
                if (isAlphaMode) {
                    i = QWERTY;
                } else {
                    i = QWERTY_SHIFTED;
                }
                this.mKeyboardState = i;
                next = isAlphaMode ? this.mQwertyKeyboardShifted : this.mSymbolsKeyboardShifted;
            } else if (this.mKeyboardState == QWERTY) {
                this.mKeyboardState = QWERTY_SHIFTED;
                next = isAlphaMode ? this.mQwertyKeyboardShifted : this.mSymbolsKeyboardShifted;
            } else if (this.mKeyboardState == QWERTY_SHIFTED) {
                this.mKeyboardState = NUMERIC;
                next = isAlphaMode ? this.mQwertyKeyboard : this.mSymbolsKeyboard;
            }
            if (next != null) {
                boolean z2;
                if (next != current) {
                    this.mKeyboardView.setKeyboard(next);
                }
                if (this.mKeyboardState == QWERTY_SHIFTED) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                next.setShiftLocked(z2);
                KeyboardView keyboardView = this.mKeyboardView;
                if (this.mKeyboardState == 0) {
                    z = false;
                }
                keyboardView.setShifted(z);
            }
        }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (!(!this.mKeyboardView.isShifted() || primaryCode == 32 || primaryCode == 10)) {
            primaryCode = Character.toUpperCase(primaryCode);
        }
        sendKeyEventsToTarget(primaryCode);
    }

    private void handleClose() {
    }

    public void onPress(int primaryCode) {
        performHapticFeedback();
    }

    private void performHapticFeedback() {
        if (this.mEnableHaptics) {
            this.mKeyboardView.performHapticFeedback(QWERTY, SYMBOLS);
        }
    }

    public void onRelease(int primaryCode) {
    }

    public void onText(CharSequence text) {
    }

    public void swipeDown() {
    }

    public void swipeLeft() {
    }

    public void swipeRight() {
    }

    public void swipeUp() {
    }
}

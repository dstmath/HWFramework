package com.android.internal.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.Keyboard.Row;
import com.android.internal.R;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;

public class PasswordEntryKeyboard extends Keyboard {
    public static final int KEYCODE_SPACE = 32;
    private static final int SHIFT_LOCKED = 2;
    private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    static int sSpacebarVerticalCorrection;
    private Key mEnterKey;
    private Key mF1Key;
    private Drawable[] mOldShiftIcons;
    private Drawable mShiftIcon;
    private Key[] mShiftKeys;
    private Drawable mShiftLockIcon;
    private int mShiftState;
    private Key mSpaceKey;

    static class LatinKey extends Key {
        private boolean mEnabled;
        private boolean mShiftLockEnabled;

        public LatinKey(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            this.mEnabled = true;
            if (this.popupCharacters != null && this.popupCharacters.length() == 0) {
                this.popupResId = PasswordEntryKeyboard.SHIFT_OFF;
            }
        }

        void setEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }

        void enableShiftLock() {
            this.mShiftLockEnabled = true;
        }

        public void onReleased(boolean inside) {
            if (this.mShiftLockEnabled) {
                this.pressed = !this.pressed;
            } else {
                super.onReleased(inside);
            }
        }

        public boolean isInside(int x, int y) {
            if (!this.mEnabled) {
                return false;
            }
            int code = this.codes[PasswordEntryKeyboard.SHIFT_OFF];
            if (code == -1 || code == -5) {
                y -= this.height / 10;
                if (code == -1) {
                    x += this.width / 6;
                }
                if (code == -5) {
                    x -= this.width / 6;
                }
            } else if (code == PasswordEntryKeyboard.KEYCODE_SPACE) {
                y += PasswordEntryKeyboard.sSpacebarVerticalCorrection;
            }
            return super.isInside(x, y);
        }
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId) {
        this(context, xmlLayoutResId, SHIFT_OFF);
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId, int width, int height) {
        this(context, xmlLayoutResId, (int) SHIFT_OFF, width, height);
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId, int mode) {
        super(context, xmlLayoutResId, mode);
        Drawable[] drawableArr = new Drawable[SHIFT_LOCKED];
        drawableArr[SHIFT_OFF] = null;
        drawableArr[SHIFT_ON] = null;
        this.mOldShiftIcons = drawableArr;
        Key[] keyArr = new Key[SHIFT_LOCKED];
        keyArr[SHIFT_OFF] = null;
        keyArr[SHIFT_ON] = null;
        this.mShiftKeys = keyArr;
        this.mShiftState = SHIFT_OFF;
        init(context);
    }

    public PasswordEntryKeyboard(Context context, int xmlLayoutResId, int mode, int width, int height) {
        super(context, xmlLayoutResId, mode, width, height);
        Drawable[] drawableArr = new Drawable[SHIFT_LOCKED];
        drawableArr[SHIFT_OFF] = null;
        drawableArr[SHIFT_ON] = null;
        this.mOldShiftIcons = drawableArr;
        Key[] keyArr = new Key[SHIFT_LOCKED];
        keyArr[SHIFT_OFF] = null;
        keyArr[SHIFT_ON] = null;
        this.mShiftKeys = keyArr;
        this.mShiftState = SHIFT_OFF;
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();
        this.mShiftIcon = context.getDrawable(R.drawable.sym_keyboard_shift);
        this.mShiftLockIcon = context.getDrawable(R.drawable.sym_keyboard_shift_locked);
        sSpacebarVerticalCorrection = res.getDimensionPixelOffset(R.dimen.password_keyboard_spacebar_vertical_correction);
    }

    public PasswordEntryKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
        Drawable[] drawableArr = new Drawable[SHIFT_LOCKED];
        drawableArr[SHIFT_OFF] = null;
        drawableArr[SHIFT_ON] = null;
        this.mOldShiftIcons = drawableArr;
        Key[] keyArr = new Key[SHIFT_LOCKED];
        keyArr[SHIFT_OFF] = null;
        keyArr[SHIFT_ON] = null;
        this.mShiftKeys = keyArr;
        this.mShiftState = SHIFT_OFF;
    }

    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        LatinKey key = new LatinKey(res, parent, x, y, parser);
        int code = key.codes[SHIFT_OFF];
        if (code >= 0 && code != 10 && (code < KEYCODE_SPACE || code > LogPower.MIME_TYPE)) {
            key.label = " ";
            key.setEnabled(false);
        }
        switch (key.codes[SHIFT_OFF]) {
            case -103:
                this.mF1Key = key;
                break;
            case PGSdk.TYPE_CLOCK /*10*/:
                this.mEnterKey = key;
                break;
            case KEYCODE_SPACE /*32*/:
                this.mSpaceKey = key;
                break;
        }
        return key;
    }

    void setEnterKeyResources(Resources res, int previewId, int iconId, int labelId) {
        if (this.mEnterKey != null) {
            this.mEnterKey.popupCharacters = null;
            this.mEnterKey.popupResId = SHIFT_OFF;
            this.mEnterKey.text = null;
            this.mEnterKey.iconPreview = res.getDrawable(previewId);
            this.mEnterKey.icon = res.getDrawable(iconId);
            this.mEnterKey.label = res.getText(labelId);
            if (this.mEnterKey.iconPreview != null) {
                this.mEnterKey.iconPreview.setBounds(SHIFT_OFF, SHIFT_OFF, this.mEnterKey.iconPreview.getIntrinsicWidth(), this.mEnterKey.iconPreview.getIntrinsicHeight());
            }
        }
    }

    void enableShiftLock() {
        int i = SHIFT_OFF;
        int[] shiftKeyIndices = getShiftKeyIndices();
        int length = shiftKeyIndices.length;
        for (int i2 = SHIFT_OFF; i2 < length; i2 += SHIFT_ON) {
            int index = shiftKeyIndices[i2];
            if (index >= 0 && i < this.mShiftKeys.length) {
                this.mShiftKeys[i] = (Key) getKeys().get(index);
                if (this.mShiftKeys[i] instanceof LatinKey) {
                    ((LatinKey) this.mShiftKeys[i]).enableShiftLock();
                }
                this.mOldShiftIcons[i] = this.mShiftKeys[i].icon;
                i += SHIFT_ON;
            }
        }
    }

    void setShiftLocked(boolean shiftLocked) {
        int i;
        Key[] keyArr = this.mShiftKeys;
        int length = keyArr.length;
        for (i = SHIFT_OFF; i < length; i += SHIFT_ON) {
            Key shiftKey = keyArr[i];
            if (shiftKey != null) {
                shiftKey.on = shiftLocked;
                shiftKey.icon = this.mShiftLockIcon;
            }
        }
        if (shiftLocked) {
            i = SHIFT_LOCKED;
        } else {
            i = SHIFT_ON;
        }
        this.mShiftState = i;
    }

    public boolean setShifted(boolean shiftState) {
        boolean shiftChanged = false;
        if (!shiftState) {
            shiftChanged = this.mShiftState != 0;
            this.mShiftState = SHIFT_OFF;
        } else if (this.mShiftState == 0) {
            shiftChanged = this.mShiftState == 0;
            this.mShiftState = SHIFT_ON;
        }
        for (int i = SHIFT_OFF; i < this.mShiftKeys.length; i += SHIFT_ON) {
            if (this.mShiftKeys[i] != null) {
                if (!shiftState) {
                    this.mShiftKeys[i].on = false;
                    this.mShiftKeys[i].icon = this.mOldShiftIcons[i];
                } else if (this.mShiftState == 0) {
                    this.mShiftKeys[i].on = false;
                    this.mShiftKeys[i].icon = this.mShiftIcon;
                }
            }
        }
        return shiftChanged;
    }

    public boolean isShifted() {
        boolean z = false;
        if (this.mShiftKeys[SHIFT_OFF] == null) {
            return super.isShifted();
        }
        if (this.mShiftState != 0) {
            z = true;
        }
        return z;
    }
}

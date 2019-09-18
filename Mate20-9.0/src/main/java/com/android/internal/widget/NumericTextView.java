package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.KeyEvent;
import android.widget.TextView;

public class NumericTextView extends TextView {
    private static final double LOG_RADIX = Math.log(10.0d);
    private static final int RADIX = 10;
    private int mCount;
    private OnValueChangedListener mListener;
    private int mMaxCount = 2;
    private int mMaxValue = 99;
    private int mMinValue = 0;
    private int mPreviousValue;
    private boolean mShowLeadingZeroes = true;
    private int mValue;

    public interface OnValueChangedListener {
        void onValueChanged(NumericTextView numericTextView, int i, boolean z, boolean z2);
    }

    public NumericTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHintTextColor(getTextColors().getColorForState(StateSet.get(0), 0));
        setFocusable(true);
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            this.mPreviousValue = this.mValue;
            this.mValue = 0;
            this.mCount = 0;
            setHint(getText());
            setText("");
            return;
        }
        if (this.mCount == 0) {
            this.mValue = this.mPreviousValue;
            setText(getHint());
            setHint("");
        }
        if (this.mValue < this.mMinValue) {
            this.mValue = this.mMinValue;
        }
        setValue(this.mValue);
        if (this.mListener != null) {
            this.mListener.onValueChanged(this, this.mValue, true, true);
        }
    }

    public final void setValue(int value) {
        if (this.mValue != value) {
            this.mValue = value;
            updateDisplayedValue();
        }
    }

    public final int getValue() {
        return this.mValue;
    }

    public final void setRange(int minValue, int maxValue) {
        if (this.mMinValue != minValue) {
            this.mMinValue = minValue;
        }
        if (this.mMaxValue != maxValue) {
            this.mMaxValue = maxValue;
            this.mMaxCount = 1 + ((int) (Math.log((double) maxValue) / LOG_RADIX));
            updateMinimumWidth();
            updateDisplayedValue();
        }
    }

    public final int getRangeMinimum() {
        return this.mMinValue;
    }

    public final int getRangeMaximum() {
        return this.mMaxValue;
    }

    public final void setShowLeadingZeroes(boolean showLeadingZeroes) {
        if (this.mShowLeadingZeroes != showLeadingZeroes) {
            this.mShowLeadingZeroes = showLeadingZeroes;
            updateDisplayedValue();
        }
    }

    public final boolean getShowLeadingZeroes() {
        return this.mShowLeadingZeroes;
    }

    private void updateDisplayedValue() {
        String format;
        if (this.mShowLeadingZeroes) {
            format = "%0" + this.mMaxCount + "d";
        } else {
            format = "%d";
        }
        setText(String.format(format, new Object[]{Integer.valueOf(this.mValue)}));
    }

    private void updateMinimumWidth() {
        CharSequence previousText = getText();
        int maxWidth = 0;
        for (int i = 0; i < this.mMaxValue; i++) {
            setText(String.format("%0" + this.mMaxCount + "d", new Object[]{Integer.valueOf(i)}));
            measure(0, 0);
            int width = getMeasuredWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        setText(previousText);
        setMinWidth(maxWidth);
        setMinimumWidth(maxWidth);
    }

    public final void setOnDigitEnteredListener(OnValueChangedListener listener) {
        this.mListener = listener;
    }

    public final OnValueChangedListener getOnDigitEnteredListener() {
        return this.mListener;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return isKeyCodeNumeric(keyCode) || keyCode == 67 || super.onKeyDown(keyCode, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return isKeyCodeNumeric(keyCode) || keyCode == 67 || super.onKeyMultiple(keyCode, repeatCount, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return handleKeyUp(keyCode) || super.onKeyUp(keyCode, event);
    }

    private boolean handleKeyUp(int keyCode) {
        String formattedValue;
        boolean isFinished = false;
        if (keyCode == 67) {
            if (this.mCount > 0) {
                this.mValue /= 10;
                this.mCount--;
            }
        } else if (!isKeyCodeNumeric(keyCode)) {
            return false;
        } else {
            if (this.mCount < this.mMaxCount) {
                int newValue = (this.mValue * 10) + numericKeyCodeToInt(keyCode);
                if (newValue <= this.mMaxValue) {
                    this.mValue = newValue;
                    this.mCount++;
                }
            }
        }
        if (this.mCount > 0) {
            formattedValue = String.format("%0" + this.mCount + "d", new Object[]{Integer.valueOf(this.mValue)});
        } else {
            formattedValue = "";
        }
        setText(formattedValue);
        if (this.mListener != null) {
            boolean isValid = this.mValue >= this.mMinValue;
            if (this.mCount >= this.mMaxCount || this.mValue * 10 > this.mMaxValue) {
                isFinished = true;
            }
            this.mListener.onValueChanged(this, this.mValue, isValid, isFinished);
        }
        return true;
    }

    private static boolean isKeyCodeNumeric(int keyCode) {
        return keyCode == 7 || keyCode == 8 || keyCode == 9 || keyCode == 10 || keyCode == 11 || keyCode == 12 || keyCode == 13 || keyCode == 14 || keyCode == 15 || keyCode == 16;
    }

    private static int numericKeyCodeToInt(int keyCode) {
        return keyCode - 7;
    }
}

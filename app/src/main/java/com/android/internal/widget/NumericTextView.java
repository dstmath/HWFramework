package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.KeyEvent;
import android.widget.TextView;

public class NumericTextView extends TextView {
    private static final double LOG_RADIX = 0.0d;
    private static final int RADIX = 10;
    private int mCount;
    private OnValueChangedListener mListener;
    private int mMaxCount;
    private int mMaxValue;
    private int mMinValue;
    private int mPreviousValue;
    private boolean mShowLeadingZeroes;
    private int mValue;

    public interface OnValueChangedListener {
        void onValueChanged(NumericTextView numericTextView, int i, boolean z, boolean z2);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.NumericTextView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.NumericTextView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.NumericTextView.<clinit>():void");
    }

    public NumericTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMinValue = 0;
        this.mMaxValue = 99;
        this.mMaxCount = 2;
        this.mShowLeadingZeroes = true;
        setHintTextColor(getTextColors().getColorForState(StateSet.get(0), 0));
        setFocusable(true);
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            this.mPreviousValue = this.mValue;
            this.mValue = 0;
            this.mCount = 0;
            setHint(getText());
            setText((CharSequence) "");
            return;
        }
        if (this.mCount == 0) {
            this.mValue = this.mPreviousValue;
            setText(getHint());
            setHint((CharSequence) "");
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
            this.mMaxCount = ((int) (Math.log((double) maxValue) / LOG_RADIX)) + 1;
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
        if (isKeyCodeNumeric(keyCode) || keyCode == 67) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        if (isKeyCodeNumeric(keyCode) || keyCode == 67) {
            return true;
        }
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (handleKeyUp(keyCode)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean handleKeyUp(int keyCode) {
        String formattedValue;
        if (keyCode == 67) {
            if (this.mCount > 0) {
                this.mValue /= RADIX;
                this.mCount--;
            }
        } else if (!isKeyCodeNumeric(keyCode)) {
            return false;
        } else {
            if (this.mCount < this.mMaxCount) {
                int newValue = (this.mValue * RADIX) + numericKeyCodeToInt(keyCode);
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
        setText((CharSequence) formattedValue);
        if (this.mListener != null) {
            boolean isValid = this.mValue >= this.mMinValue;
            boolean isFinished = this.mCount >= this.mMaxCount || this.mValue * RADIX > this.mMaxValue;
            this.mListener.onValueChanged(this, this.mValue, isValid, isFinished);
        }
        return true;
    }

    private static boolean isKeyCodeNumeric(int keyCode) {
        if (keyCode == 7 || keyCode == 8 || keyCode == 9 || keyCode == RADIX || keyCode == 11 || keyCode == 12 || keyCode == 13 || keyCode == 14 || keyCode == 15 || keyCode == 16) {
            return true;
        }
        return false;
    }

    private static int numericKeyCodeToInt(int keyCode) {
        return keyCode - 7;
    }
}

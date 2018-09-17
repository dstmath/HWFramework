package com.android.internal.widget;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.LogException;
import android.widget.TextView;

public class TextViewInputDisabler {
    private InputFilter[] mDefaultFilters;
    private InputFilter[] mNoInputFilters = new InputFilter[]{new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            return LogException.NO_VALUE;
        }
    }};
    private TextView mTextView;

    public TextViewInputDisabler(TextView textView) {
        this.mTextView = textView;
        this.mDefaultFilters = this.mTextView.getFilters();
    }

    public void setInputEnabled(boolean enabled) {
        this.mTextView.setFilters(enabled ? this.mDefaultFilters : this.mNoInputFilters);
    }
}

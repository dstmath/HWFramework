package com.android.internal.widget;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.TextView;

public class TextViewInputDisabler {
    private InputFilter[] mDefaultFilters;
    private InputFilter[] mNoInputFilters;
    private TextView mTextView;

    public TextViewInputDisabler(TextView textView) {
        this.mNoInputFilters = new InputFilter[]{new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return "";
            }
        }};
        this.mTextView = textView;
        this.mDefaultFilters = this.mTextView.getFilters();
    }

    public void setInputEnabled(boolean enabled) {
        this.mTextView.setFilters(enabled ? this.mDefaultFilters : this.mNoInputFilters);
    }
}

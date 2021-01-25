package com.huawei.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.AsYouTypeFormatter;

public class AsYouTypeFormatterEx {
    private AsYouTypeFormatter aytFormatter;

    protected AsYouTypeFormatterEx(AsYouTypeFormatter formatter) {
        this.aytFormatter = formatter;
    }

    public String inputDigit(char nextChar) {
        return this.aytFormatter.inputDigit(nextChar);
    }

    public int getRememberedPosition() {
        return this.aytFormatter.getRememberedPosition();
    }

    public void clear() {
        this.aytFormatter.clear();
    }

    public String inputDigitAndRememberPosition(char nextChar) {
        return this.aytFormatter.inputDigitAndRememberPosition(nextChar);
    }
}

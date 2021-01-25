package ohos.global.i18n.phonenumbers;

import com.huawei.android.i18n.phonenumbers.AsYouTypeFormatterEx;

public class AsYouTypeFormatterImpl extends AsYouTypeFormatter {
    private AsYouTypeFormatterEx formatter;

    public AsYouTypeFormatterImpl(AsYouTypeFormatterEx asYouTypeFormatterEx) {
        this.formatter = asYouTypeFormatterEx;
    }

    @Override // ohos.global.i18n.phonenumbers.AsYouTypeFormatter
    public String inputDigit(char c) {
        return this.formatter.inputDigit(c);
    }

    @Override // ohos.global.i18n.phonenumbers.AsYouTypeFormatter
    public int getRememberedPosition() {
        return this.formatter.getRememberedPosition();
    }

    @Override // ohos.global.i18n.phonenumbers.AsYouTypeFormatter
    public void clear() {
        this.formatter.clear();
    }

    @Override // ohos.global.i18n.phonenumbers.AsYouTypeFormatter
    public String inputDigitAndRememberPosition(char c) {
        return this.formatter.inputDigitAndRememberPosition(c);
    }
}

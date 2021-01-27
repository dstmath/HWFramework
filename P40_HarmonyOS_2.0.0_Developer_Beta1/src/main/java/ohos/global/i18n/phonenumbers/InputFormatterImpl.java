package ohos.global.i18n.phonenumbers;

import com.huawei.android.i18n.phonenumbers.AsYouTypeFormatterEx;

public class InputFormatterImpl extends InputFormatter {
    private AsYouTypeFormatterEx formatter;

    public InputFormatterImpl(AsYouTypeFormatterEx asYouTypeFormatterEx) {
        this.formatter = asYouTypeFormatterEx;
    }

    @Override // ohos.global.i18n.phonenumbers.InputFormatter
    public String inputNumber(char c) {
        return this.formatter.inputDigit(c);
    }

    @Override // ohos.global.i18n.phonenumbers.InputFormatter
    public int getPosition() {
        return this.formatter.getRememberedPosition();
    }

    @Override // ohos.global.i18n.phonenumbers.InputFormatter
    public void clean() {
        this.formatter.clear();
    }

    @Override // ohos.global.i18n.phonenumbers.InputFormatter
    public String inputNumberAndRememberPosition(char c) {
        return this.formatter.inputDigitAndRememberPosition(c);
    }
}

package ohos.global.i18n.phonenumbers;

public abstract class AsYouTypeFormatter {
    public abstract void clear();

    public abstract int getRememberedPosition();

    public abstract String inputDigit(char c);

    public abstract String inputDigitAndRememberPosition(char c);
}

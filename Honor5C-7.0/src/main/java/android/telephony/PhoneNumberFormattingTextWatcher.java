package android.telephony;

import android.common.HwFrameworkFactory;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import com.android.i18n.phonenumbers.AsYouTypeFormatter;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import java.util.Locale;

public class PhoneNumberFormattingTextWatcher implements TextWatcher {
    private AsYouTypeFormatter mFormatter;
    private boolean mSelfChange;
    private boolean mStopFormatting;

    public PhoneNumberFormattingTextWatcher() {
        this(Locale.getDefault().getCountry());
    }

    public PhoneNumberFormattingTextWatcher(String countryCode) {
        this.mSelfChange = false;
        if (countryCode == null) {
            throw new IllegalArgumentException();
        }
        this.mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!this.mSelfChange && !this.mStopFormatting && count > 0 && hasSeparator(s, start, count)) {
            stopFormatting();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!this.mSelfChange && !this.mStopFormatting && count > 0 && hasSeparator(s, start, count)) {
            stopFormatting();
        }
    }

    public synchronized void afterTextChanged(Editable s) {
        boolean z = true;
        synchronized (this) {
            if (this.mStopFormatting) {
                if (s.length() == 0) {
                    z = false;
                }
                this.mStopFormatting = z;
            } else if (this.mSelfChange) {
            } else {
                String formatted = reformat(s, Selection.getSelectionEnd(s));
                if (formatted != null) {
                    int rememberedPos = this.mFormatter.getRememberedPosition();
                    if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustomProcess()) {
                        int numBrackts = 0;
                        int i = 0;
                        while (i < rememberedPos && i < formatted.length()) {
                            if ('(' == formatted.charAt(i) || ')' == formatted.charAt(i)) {
                                numBrackts++;
                            }
                            i++;
                        }
                        formatted = HwFrameworkFactory.getHwInnerTelephonyManager().stripBrackets(formatted);
                        rememberedPos -= numBrackts;
                    }
                    if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustRemoveSep() || HwFrameworkFactory.getHwInnerTelephonyManager().isRemoveSeparateOnSK()) {
                        rememberedPos = HwFrameworkFactory.getHwInnerTelephonyManager().getNewRememberedPos(rememberedPos, formatted);
                        formatted = HwFrameworkFactory.getHwInnerTelephonyManager().removeAllSeparate(formatted);
                    }
                    this.mSelfChange = true;
                    s.replace(0, s.length(), formatted, 0, formatted.length());
                    if (formatted.equals(s.toString())) {
                        Selection.setSelection(s, rememberedPos);
                    }
                    this.mSelfChange = false;
                }
                PhoneNumberUtils.ttsSpanAsPhoneNumber(s, 0, s.length());
            }
        }
    }

    private String reformat(CharSequence s, int cursor) {
        int curIndex = cursor - 1;
        String formatted = null;
        this.mFormatter.clear();
        char lastNonSeparator = '\u0000';
        boolean hasCursor = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != '\u0000') {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor);
                    hasCursor = false;
                }
                lastNonSeparator = c;
            }
            if (i == curIndex) {
                hasCursor = true;
            }
        }
        if (lastNonSeparator != '\u0000') {
            return getFormattedNumber(lastNonSeparator, hasCursor);
        }
        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        if (hasCursor) {
            return this.mFormatter.inputDigitAndRememberPosition(lastNonSeparator);
        }
        return this.mFormatter.inputDigit(lastNonSeparator);
    }

    private void stopFormatting() {
        this.mStopFormatting = true;
        this.mFormatter.clear();
    }

    private boolean hasSeparator(CharSequence s, int start, int count) {
        int i = start;
        while (i < start + count) {
            try {
                if (!PhoneNumberUtils.isNonSeparator(s.charAt(i))) {
                    return true;
                }
                i++;
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
        }
        return false;
    }
}

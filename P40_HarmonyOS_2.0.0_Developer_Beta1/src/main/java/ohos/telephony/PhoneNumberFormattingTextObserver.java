package ohos.telephony;

import java.util.Locale;
import ohos.agp.components.Text;
import ohos.hiviewdfx.HiLogLabel;

public class PhoneNumberFormattingTextObserver implements Text.TextObserver {
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "PhoneNumberFormattingTextObserver");
    private String mCountryCode;
    private String mPhoneNumberFormat;

    public PhoneNumberFormattingTextObserver(String str) {
        this.mCountryCode = str == null ? Locale.getDefault().getCountry() : str;
    }

    private boolean hasSeparator(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!TelephoneNumberUtils.isNotSeparator(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void onTextUpdated(String str, int i, int i2, int i3) {
        if (i3 <= 0 || !hasSeparator(str)) {
            this.mPhoneNumberFormat = TelephonyAdapt.formartPhoneNumber(str, this.mCountryCode);
        } else {
            this.mPhoneNumberFormat = str;
        }
    }

    public String getPhoneNumberAfterFormat() {
        return this.mPhoneNumberFormat;
    }
}

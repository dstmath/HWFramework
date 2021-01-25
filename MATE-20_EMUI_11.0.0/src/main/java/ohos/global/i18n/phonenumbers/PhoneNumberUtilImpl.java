package ohos.global.i18n.phonenumbers;

import com.huawei.android.i18n.phonenumbers.NumberParseExceptionEx;
import com.huawei.android.i18n.phonenumbers.PhoneNumberUtilEx;
import ohos.global.i18n.phonenumbers.PhoneNumberParseException;

public class PhoneNumberUtilImpl extends PhoneNumberUtil {
    private PhoneNumberUtilEx mUtil = PhoneNumberUtilEx.getInstance();

    @Override // ohos.global.i18n.phonenumbers.PhoneNumberUtil
    public AsYouTypeFormatter getAsYouTypeFormatter(String str) {
        return new AsYouTypeFormatterImpl(this.mUtil.getAsYouTypeFormatter(str));
    }

    @Override // ohos.global.i18n.phonenumbers.PhoneNumberUtil
    public PhoneNumber parse(String str, String str2) throws PhoneNumberParseException {
        try {
            return new PhoneNumberImpl(this.mUtil.parse(str, str2));
        } catch (NumberParseExceptionEx unused) {
            throw new PhoneNumberParseException(PhoneNumberParseException.Type.WRONG_COUNTRY_CODE, "Country calling code supplied was not recognised.");
        }
    }
}

package ohos.global.i18n;

import java.util.Locale;
import ohos.global.icu.util.Calendar;

public class PersianCalendar {
    public static Calendar getPersianCalendar(Locale locale) {
        return new ohos.global.icu.util.PersianCalendar(locale);
    }
}

package ohos.global.i18n.utils;

import java.util.MissingResourceException;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.UResourceBundleIterator;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class CalendarUtilsImpl extends CalendarUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "CalendarUtilsImpl");
    private ICUResourceBundle fBundle = null;

    @Override // ohos.global.i18n.utils.CalendarUtils
    public String getCalendarName(Calendar calendar, ULocale uLocale) {
        if (uLocale == null) {
            return getCalendarName(calendar, ULocale.US);
        }
        ICUResourceBundle iCUResourceBundle = this.fBundle;
        if ((iCUResourceBundle == null || iCUResourceBundle.getULocale() == null || !this.fBundle.getULocale().equals(uLocale)) && (UResourceBundle.getBundleInstance(ICUData.ICU_LANG_BASE_NAME, uLocale) instanceof ICUResourceBundle)) {
            this.fBundle = UResourceBundle.getBundleInstance(ICUData.ICU_LANG_BASE_NAME, uLocale);
        }
        try {
            UResourceBundleIterator iterator = this.fBundle.getWithFallback("Types/calendar").getIterator();
            while (iterator.hasNext()) {
                UResourceBundle next = iterator.next();
                if (next.getType() == 0 && next.getKey().equals(calendar.getType())) {
                    return next.getString();
                }
            }
        } catch (MissingResourceException unused) {
            HiLog.debug(LABEL, "get data of ICUResource fail", new Object[0]);
        }
        return getCalendarName(calendar, uLocale.getFallback());
    }
}

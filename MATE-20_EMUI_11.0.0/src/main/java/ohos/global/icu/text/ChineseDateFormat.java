package ohos.global.icu.text;

import java.io.InvalidObjectException;
import java.text.FieldPosition;
import java.util.Locale;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.ChineseCalendar;
import ohos.global.icu.util.TimeZone;
import ohos.global.icu.util.ULocale;

@Deprecated
public class ChineseDateFormat extends SimpleDateFormat {
    static final long serialVersionUID = -4610300753104099899L;

    @Deprecated
    public ChineseDateFormat(String str, Locale locale) {
        this(str, ULocale.forLocale(locale));
    }

    @Deprecated
    public ChineseDateFormat(String str, ULocale uLocale) {
        this(str, null, uLocale);
    }

    @Deprecated
    public ChineseDateFormat(String str, String str2, ULocale uLocale) {
        super(str, new ChineseDateFormatSymbols(uLocale), new ChineseCalendar(TimeZone.getDefault(), uLocale), uLocale, true, str2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.SimpleDateFormat
    @Deprecated
    public void subFormat(StringBuffer stringBuffer, char c, int i, int i2, int i3, DisplayContext displayContext, FieldPosition fieldPosition, Calendar calendar) {
        super.subFormat(stringBuffer, c, i, i2, i3, displayContext, fieldPosition, calendar);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.SimpleDateFormat
    @Deprecated
    public int subParse(String str, int i, char c, int i2, boolean z, boolean z2, boolean[] zArr, Calendar calendar) {
        return super.subParse(str, i, c, i2, z, z2, zArr, calendar);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.SimpleDateFormat
    @Deprecated
    public DateFormat.Field patternCharToDateFormatField(char c) {
        return super.patternCharToDateFormatField(c);
    }

    @Deprecated
    public static class Field extends DateFormat.Field {
        @Deprecated
        public static final Field IS_LEAP_MONTH = new Field("is leap month", 22);
        private static final long serialVersionUID = -5102130532751400330L;

        @Deprecated
        protected Field(String str, int i) {
            super(str, i);
        }

        @Deprecated
        public static DateFormat.Field ofCalendarField(int i) {
            if (i == 22) {
                return IS_LEAP_MONTH;
            }
            return DateFormat.Field.ofCalendarField(i);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.DateFormat.Field, java.text.AttributedCharacterIterator.Attribute
        @Deprecated
        public Object readResolve() throws InvalidObjectException {
            if (getClass() != Field.class) {
                throw new InvalidObjectException("A subclass of ChineseDateFormat.Field must implement readResolve.");
            } else if (getName().equals(IS_LEAP_MONTH.getName())) {
                return IS_LEAP_MONTH;
            } else {
                throw new InvalidObjectException("Unknown attribute name.");
            }
        }
    }
}

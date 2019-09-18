package android.icu.text;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.icu.util.ChineseCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import java.io.InvalidObjectException;
import java.text.FieldPosition;
import java.util.Locale;

@Deprecated
public class ChineseDateFormat extends SimpleDateFormat {
    static final long serialVersionUID = -4610300753104099899L;

    @Deprecated
    public static class Field extends DateFormat.Field {
        @Deprecated
        public static final Field IS_LEAP_MONTH = new Field("is leap month", 22);
        private static final long serialVersionUID = -5102130532751400330L;

        @Deprecated
        protected Field(String name, int calendarField) {
            super(name, calendarField);
        }

        @Deprecated
        public static DateFormat.Field ofCalendarField(int calendarField) {
            if (calendarField == 22) {
                return IS_LEAP_MONTH;
            }
            return DateFormat.Field.ofCalendarField(calendarField);
        }

        /* access modifiers changed from: protected */
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

    @Deprecated
    public ChineseDateFormat(String pattern, Locale locale) {
        this(pattern, ULocale.forLocale(locale));
    }

    @Deprecated
    public ChineseDateFormat(String pattern, ULocale locale) {
        this(pattern, null, locale);
    }

    @Deprecated
    public ChineseDateFormat(String pattern, String override, ULocale locale) {
        super(pattern, new ChineseDateFormatSymbols(locale), new ChineseCalendar(TimeZone.getDefault(), locale), locale, true, override);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void subFormat(StringBuffer buf, char ch, int count, int beginOffset, int fieldNum, DisplayContext capitalizationContext, FieldPosition pos, Calendar cal) {
        super.subFormat(buf, ch, count, beginOffset, fieldNum, capitalizationContext, pos, cal);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int subParse(String text, int start, char ch, int count, boolean obeyCount, boolean allowNegative, boolean[] ambiguousYear, Calendar cal) {
        return super.subParse(text, start, ch, count, obeyCount, allowNegative, ambiguousYear, cal);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public DateFormat.Field patternCharToDateFormatField(char ch) {
        return super.patternCharToDateFormatField(ch);
    }
}

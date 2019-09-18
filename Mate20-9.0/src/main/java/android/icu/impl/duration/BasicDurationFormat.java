package android.icu.impl.duration;

import android.icu.text.DurationFormat;
import android.icu.util.ULocale;
import java.text.FieldPosition;
import java.util.Date;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

public class BasicDurationFormat extends DurationFormat {
    private static final long serialVersionUID = -3146984141909457700L;
    transient DurationFormatter formatter;
    transient PeriodFormatter pformatter;
    transient PeriodFormatterService pfs;

    public static BasicDurationFormat getInstance(ULocale locale) {
        return new BasicDurationFormat(locale);
    }

    public StringBuffer format(Object object, StringBuffer toAppend, FieldPosition pos) {
        if (object instanceof Long) {
            toAppend.append(formatDurationFromNow(((Long) object).longValue()));
            return toAppend;
        } else if (object instanceof Date) {
            toAppend.append(formatDurationFromNowTo((Date) object));
            return toAppend;
        } else if (object instanceof Duration) {
            toAppend.append(formatDuration(object));
            return toAppend;
        } else {
            throw new IllegalArgumentException("Cannot format given Object as a Duration");
        }
    }

    public BasicDurationFormat() {
        this.pfs = null;
        this.pfs = BasicPeriodFormatterService.getInstance();
        this.formatter = this.pfs.newDurationFormatterFactory().getFormatter();
        this.pformatter = this.pfs.newPeriodFormatterFactory().setDisplayPastFuture(false).getFormatter();
    }

    public BasicDurationFormat(ULocale locale) {
        super(locale);
        this.pfs = null;
        this.pfs = BasicPeriodFormatterService.getInstance();
        this.formatter = this.pfs.newDurationFormatterFactory().setLocale(locale.getName()).getFormatter();
        this.pformatter = this.pfs.newPeriodFormatterFactory().setDisplayPastFuture(false).setLocale(locale.getName()).getFormatter();
    }

    public String formatDurationFrom(long duration, long referenceDate) {
        return this.formatter.formatDurationFrom(duration, referenceDate);
    }

    public String formatDurationFromNow(long duration) {
        return this.formatter.formatDurationFromNow(duration);
    }

    public String formatDurationFromNowTo(Date targetDate) {
        return this.formatter.formatDurationFromNowTo(targetDate);
    }

    public String formatDuration(Object obj) {
        Period p;
        DatatypeConstants.Field[] inFields;
        boolean sawNonZero;
        Period p2;
        int i = 0;
        DatatypeConstants.Field[] inFields2 = {DatatypeConstants.YEARS, DatatypeConstants.MONTHS, DatatypeConstants.DAYS, DatatypeConstants.HOURS, DatatypeConstants.MINUTES, DatatypeConstants.SECONDS};
        TimeUnit[] outFields = {TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY, TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND};
        Duration inDuration = (Duration) obj;
        Period p3 = null;
        Duration duration = inDuration;
        boolean inPast = false;
        if (inDuration.getSign() < 0) {
            duration = inDuration.negate();
            inPast = true;
        }
        boolean sawNonZero2 = false;
        while (i < inFields2.length) {
            if (duration.isSet(inFields2[i])) {
                Number n = duration.getField(inFields2[i]);
                if (n.intValue() != 0 || sawNonZero2) {
                    float floatVal = n.floatValue();
                    TimeUnit alternateUnit = null;
                    float alternateVal = 0.0f;
                    if (outFields[i] == TimeUnit.SECOND) {
                        inFields = inFields2;
                        double intSeconds = Math.floor((double) floatVal);
                        sawNonZero = true;
                        Number number = n;
                        double millis = (((double) floatVal) - intSeconds) * 1000.0d;
                        if (millis > 0.0d) {
                            alternateUnit = TimeUnit.MILLISECOND;
                            alternateVal = (float) millis;
                            floatVal = (float) intSeconds;
                        }
                    } else {
                        inFields = inFields2;
                        sawNonZero = true;
                        Number number2 = n;
                    }
                    if (p3 == null) {
                        p2 = Period.at(floatVal, outFields[i]);
                    } else {
                        p2 = p3.and(floatVal, outFields[i]);
                    }
                    if (alternateUnit != null) {
                        p2 = p2.and(alternateVal, alternateUnit);
                    }
                    p3 = p2;
                    sawNonZero2 = sawNonZero;
                } else {
                    inFields = inFields2;
                }
            } else {
                inFields = inFields2;
            }
            i++;
            inFields2 = inFields;
        }
        if (p3 == null) {
            return formatDurationFromNow(0);
        }
        if (inPast) {
            p = p3.inPast();
        } else {
            p = p3.inFuture();
        }
        return this.pformatter.format(p);
    }
}

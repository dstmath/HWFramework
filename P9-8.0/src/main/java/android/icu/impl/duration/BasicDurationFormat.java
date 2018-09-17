package android.icu.impl.duration;

import android.icu.text.DurationFormat;
import android.icu.util.ULocale;
import java.text.FieldPosition;
import java.util.Date;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeConstants.Field;
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
            return toAppend.append(formatDurationFromNow(((Long) object).longValue()));
        }
        if (object instanceof Date) {
            return toAppend.append(formatDurationFromNowTo((Date) object));
        }
        if (object instanceof Duration) {
            return toAppend.append(formatDuration(object));
        }
        throw new IllegalArgumentException("Cannot format given Object as a Duration");
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
        Field[] inFields = new Field[]{DatatypeConstants.YEARS, DatatypeConstants.MONTHS, DatatypeConstants.DAYS, DatatypeConstants.HOURS, DatatypeConstants.MINUTES, DatatypeConstants.SECONDS};
        TimeUnit[] outFields = new TimeUnit[]{TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY, TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND};
        Duration inDuration = (Duration) obj;
        Period p = null;
        Duration duration = inDuration;
        boolean inPast = false;
        if (inDuration.getSign() < 0) {
            duration = inDuration.negate();
            inPast = true;
        }
        boolean sawNonZero = false;
        for (int i = 0; i < inFields.length; i++) {
            if (duration.isSet(inFields[i])) {
                Number n = duration.getField(inFields[i]);
                if (n.intValue() != 0 || (sawNonZero ^ 1) == 0) {
                    sawNonZero = true;
                    float floatVal = n.floatValue();
                    TimeUnit alternateUnit = null;
                    float alternateVal = 0.0f;
                    if (outFields[i] == TimeUnit.SECOND) {
                        double fullSeconds = (double) floatVal;
                        double intSeconds = Math.floor((double) floatVal);
                        double millis = (fullSeconds - intSeconds) * 1000.0d;
                        if (millis > 0.0d) {
                            alternateUnit = TimeUnit.MILLISECOND;
                            alternateVal = (float) millis;
                            floatVal = (float) intSeconds;
                        }
                    }
                    if (p == null) {
                        p = Period.at(floatVal, outFields[i]);
                    } else {
                        p = p.and(floatVal, outFields[i]);
                    }
                    if (alternateUnit != null) {
                        p = p.and(alternateVal, alternateUnit);
                    }
                }
            }
        }
        if (p == null) {
            return formatDurationFromNow(0);
        }
        if (inPast) {
            p = p.inPast();
        } else {
            p = p.inFuture();
        }
        return this.pformatter.format(p);
    }
}

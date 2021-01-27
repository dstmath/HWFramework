package ohos.global.icu.impl.duration;

import java.text.FieldPosition;
import java.util.Date;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.global.icu.text.DurationFormat;
import ohos.global.icu.util.ULocale;

public class BasicDurationFormat extends DurationFormat {
    private static final long serialVersionUID = -3146984141909457700L;
    transient DurationFormatter formatter;
    transient PeriodFormatter pformatter;
    transient PeriodFormatterService pfs;

    public static BasicDurationFormat getInstance(ULocale uLocale) {
        return new BasicDurationFormat(uLocale);
    }

    public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (obj instanceof Long) {
            stringBuffer.append(formatDurationFromNow(((Long) obj).longValue()));
            return stringBuffer;
        } else if (obj instanceof Date) {
            stringBuffer.append(formatDurationFromNowTo((Date) obj));
            return stringBuffer;
        } else if (obj instanceof Duration) {
            stringBuffer.append(formatDuration(obj));
            return stringBuffer;
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

    public BasicDurationFormat(ULocale uLocale) {
        super(uLocale);
        this.pfs = null;
        this.pfs = BasicPeriodFormatterService.getInstance();
        this.formatter = this.pfs.newDurationFormatterFactory().setLocale(uLocale.getName()).getFormatter();
        this.pformatter = this.pfs.newPeriodFormatterFactory().setDisplayPastFuture(false).setLocale(uLocale.getName()).getFormatter();
    }

    public String formatDurationFrom(long j, long j2) {
        return this.formatter.formatDurationFrom(j, j2);
    }

    public String formatDurationFromNow(long j) {
        return this.formatter.formatDurationFromNow(j);
    }

    public String formatDurationFromNowTo(Date date) {
        return this.formatter.formatDurationFromNowTo(date);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0097  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00a6  */
    public String formatDuration(Object obj) {
        boolean z;
        Period period;
        float f;
        TimeUnit timeUnit;
        DatatypeConstants.Field[] fieldArr = {DatatypeConstants.YEARS, DatatypeConstants.MONTHS, DatatypeConstants.DAYS, DatatypeConstants.HOURS, DatatypeConstants.MINUTES, DatatypeConstants.SECONDS};
        TimeUnit[] timeUnitArr = {TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY, TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND};
        Duration duration = (Duration) obj;
        if (duration.getSign() < 0) {
            duration = duration.negate();
            z = true;
        } else {
            z = false;
        }
        boolean z2 = false;
        Period period2 = null;
        for (int i = 0; i < fieldArr.length; i++) {
            if (duration.isSet(fieldArr[i])) {
                Number field = duration.getField(fieldArr[i]);
                if (field.intValue() != 0 || z2) {
                    float floatValue = field.floatValue();
                    if (timeUnitArr[i] == TimeUnit.SECOND) {
                        double d = (double) floatValue;
                        double floor = Math.floor(d);
                        double d2 = (d - floor) * 1000.0d;
                        if (d2 > XPath.MATCH_SCORE_QNAME) {
                            timeUnit = TimeUnit.MILLISECOND;
                            floatValue = (float) floor;
                            f = (float) d2;
                            if (period2 != null) {
                                period2 = Period.at(floatValue, timeUnitArr[i]);
                            } else {
                                period2 = period2.and(floatValue, timeUnitArr[i]);
                            }
                            if (timeUnit != null) {
                                period2 = period2.and(f, timeUnit);
                            }
                            z2 = true;
                        }
                    }
                    f = 0.0f;
                    timeUnit = null;
                    if (period2 != null) {
                    }
                    if (timeUnit != null) {
                    }
                    z2 = true;
                }
            }
        }
        if (period2 == null) {
            return formatDurationFromNow(0);
        }
        if (z) {
            period = period2.inPast();
        } else {
            period = period2.inFuture();
        }
        return this.pformatter.format(period);
    }
}

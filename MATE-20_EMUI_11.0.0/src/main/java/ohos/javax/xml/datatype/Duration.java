package ohos.javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import ohos.javax.xml.datatype.DatatypeConstants;
import ohos.javax.xml.namespace.QName;

public abstract class Duration {
    private static final boolean DEBUG = true;

    public abstract Duration add(Duration duration);

    public abstract void addTo(Calendar calendar);

    public abstract int compare(Duration duration);

    public abstract Number getField(DatatypeConstants.Field field);

    public abstract int getSign();

    public abstract int hashCode();

    public abstract boolean isSet(DatatypeConstants.Field field);

    public abstract Duration multiply(BigDecimal bigDecimal);

    public abstract Duration negate();

    public abstract Duration normalizeWith(Calendar calendar);

    public QName getXMLSchemaType() {
        boolean isSet = isSet(DatatypeConstants.YEARS);
        boolean isSet2 = isSet(DatatypeConstants.MONTHS);
        boolean isSet3 = isSet(DatatypeConstants.DAYS);
        boolean isSet4 = isSet(DatatypeConstants.HOURS);
        boolean isSet5 = isSet(DatatypeConstants.MINUTES);
        boolean isSet6 = isSet(DatatypeConstants.SECONDS);
        if (isSet && isSet2 && isSet3 && isSet4 && isSet5 && isSet6) {
            return DatatypeConstants.DURATION;
        }
        if (!isSet && !isSet2 && isSet3 && isSet4 && isSet5 && isSet6) {
            return DatatypeConstants.DURATION_DAYTIME;
        }
        if (isSet && isSet2 && !isSet3 && !isSet4 && !isSet5 && !isSet6) {
            return DatatypeConstants.DURATION_YEARMONTH;
        }
        throw new IllegalStateException("ohos.javax.xml.datatype.Duration#getXMLSchemaType(): this Duration does not match one of the XML Schema date/time datatypes: year set = " + isSet + " month set = " + isSet2 + " day set = " + isSet3 + " hour set = " + isSet4 + " minute set = " + isSet5 + " second set = " + isSet6);
    }

    public int getYears() {
        return getField(DatatypeConstants.YEARS).intValue();
    }

    public int getMonths() {
        return getField(DatatypeConstants.MONTHS).intValue();
    }

    public int getDays() {
        return getField(DatatypeConstants.DAYS).intValue();
    }

    public int getHours() {
        return getField(DatatypeConstants.HOURS).intValue();
    }

    public int getMinutes() {
        return getField(DatatypeConstants.MINUTES).intValue();
    }

    public int getSeconds() {
        return getField(DatatypeConstants.SECONDS).intValue();
    }

    public long getTimeInMillis(Calendar calendar) {
        Calendar calendar2 = (Calendar) calendar.clone();
        addTo(calendar2);
        return getCalendarTimeInMillis(calendar2) - getCalendarTimeInMillis(calendar);
    }

    public long getTimeInMillis(Date date) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        addTo(gregorianCalendar);
        return getCalendarTimeInMillis(gregorianCalendar) - date.getTime();
    }

    public void addTo(Date date) {
        if (date != null) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(date);
            addTo(gregorianCalendar);
            date.setTime(getCalendarTimeInMillis(gregorianCalendar));
            return;
        }
        throw new NullPointerException("Cannot call " + getClass().getName() + "#addTo(Date date) with date == null.");
    }

    public Duration subtract(Duration duration) {
        return add(duration.negate());
    }

    public Duration multiply(int i) {
        return multiply(new BigDecimal(String.valueOf(i)));
    }

    public boolean isLongerThan(Duration duration) {
        if (compare(duration) == 1) {
            return DEBUG;
        }
        return false;
    }

    public boolean isShorterThan(Duration duration) {
        if (compare(duration) == -1) {
            return DEBUG;
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Duration) || compare((Duration) obj) != 0) {
            return false;
        }
        return DEBUG;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        if (getSign() < 0) {
            stringBuffer.append('-');
        }
        stringBuffer.append('P');
        BigInteger bigInteger = (BigInteger) getField(DatatypeConstants.YEARS);
        if (bigInteger != null) {
            stringBuffer.append(bigInteger + "Y");
        }
        BigInteger bigInteger2 = (BigInteger) getField(DatatypeConstants.MONTHS);
        if (bigInteger2 != null) {
            stringBuffer.append(bigInteger2 + "M");
        }
        BigInteger bigInteger3 = (BigInteger) getField(DatatypeConstants.DAYS);
        if (bigInteger3 != null) {
            stringBuffer.append(bigInteger3 + "D");
        }
        BigInteger bigInteger4 = (BigInteger) getField(DatatypeConstants.HOURS);
        BigInteger bigInteger5 = (BigInteger) getField(DatatypeConstants.MINUTES);
        BigDecimal bigDecimal = (BigDecimal) getField(DatatypeConstants.SECONDS);
        if (!(bigInteger4 == null && bigInteger5 == null && bigDecimal == null)) {
            stringBuffer.append('T');
            if (bigInteger4 != null) {
                stringBuffer.append(bigInteger4 + "H");
            }
            if (bigInteger5 != null) {
                stringBuffer.append(bigInteger5 + "M");
            }
            if (bigDecimal != null) {
                stringBuffer.append(toString(bigDecimal) + "S");
            }
        }
        return stringBuffer.toString();
    }

    private String toString(BigDecimal bigDecimal) {
        StringBuffer stringBuffer;
        String bigInteger = bigDecimal.unscaledValue().toString();
        int scale = bigDecimal.scale();
        if (scale == 0) {
            return bigInteger;
        }
        int length = bigInteger.length() - scale;
        if (length == 0) {
            return "0." + bigInteger;
        }
        if (length > 0) {
            stringBuffer = new StringBuffer(bigInteger);
            stringBuffer.insert(length, '.');
        } else {
            StringBuffer stringBuffer2 = new StringBuffer((3 - length) + bigInteger.length());
            stringBuffer2.append("0.");
            for (int i = 0; i < (-length); i++) {
                stringBuffer2.append('0');
            }
            stringBuffer2.append(bigInteger);
            stringBuffer = stringBuffer2;
        }
        return stringBuffer.toString();
    }

    private static long getCalendarTimeInMillis(Calendar calendar) {
        return calendar.getTime().getTime();
    }
}

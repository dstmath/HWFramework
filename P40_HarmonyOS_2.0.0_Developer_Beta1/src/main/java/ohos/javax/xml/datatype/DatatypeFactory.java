package ohos.javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public abstract class DatatypeFactory {
    public static final String DATATYPEFACTORY_IMPLEMENTATION_CLASS = new String("ohos.com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl");
    public static final String DATATYPEFACTORY_PROPERTY = "ohos.javax.xml.datatype.DatatypeFactory";
    private static final Pattern XDTSCHEMA_DTD = Pattern.compile("[^YM]*[DT].*");
    private static final Pattern XDTSCHEMA_YMD = Pattern.compile("[^DT]*");

    public abstract Duration newDuration(long j);

    public abstract Duration newDuration(String str);

    public abstract Duration newDuration(boolean z, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, BigInteger bigInteger5, BigDecimal bigDecimal);

    public abstract XMLGregorianCalendar newXMLGregorianCalendar();

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(String str);

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(BigInteger bigInteger, int i, int i2, int i3, int i4, int i5, BigDecimal bigDecimal, int i6);

    public abstract XMLGregorianCalendar newXMLGregorianCalendar(GregorianCalendar gregorianCalendar);

    protected DatatypeFactory() {
    }

    public static DatatypeFactory newInstance() throws DatatypeConfigurationException {
        return (DatatypeFactory) FactoryFinder.find(DatatypeFactory.class, DATATYPEFACTORY_IMPLEMENTATION_CLASS);
    }

    public static DatatypeFactory newInstance(String str, ClassLoader classLoader) throws DatatypeConfigurationException {
        return (DatatypeFactory) FactoryFinder.newInstance(DatatypeFactory.class, str, classLoader, false);
    }

    public Duration newDuration(boolean z, int i, int i2, int i3, int i4, int i5, int i6) {
        BigDecimal bigDecimal = null;
        BigInteger valueOf = i != Integer.MIN_VALUE ? BigInteger.valueOf((long) i) : null;
        BigInteger valueOf2 = i2 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i2) : null;
        BigInteger valueOf3 = i3 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i3) : null;
        BigInteger valueOf4 = i4 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i4) : null;
        BigInteger valueOf5 = i5 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i5) : null;
        if (i6 != Integer.MIN_VALUE) {
            bigDecimal = BigDecimal.valueOf((long) i6);
        }
        return newDuration(z, valueOf, valueOf2, valueOf3, valueOf4, valueOf5, bigDecimal);
    }

    public Duration newDurationDayTime(String str) {
        if (str == null) {
            throw new NullPointerException("Trying to create an xdt:dayTimeDuration with an invalid lexical representation of \"null\"");
        } else if (XDTSCHEMA_DTD.matcher(str).matches()) {
            return newDuration(str);
        } else {
            throw new IllegalArgumentException("Trying to create an xdt:dayTimeDuration with an invalid lexical representation of \"" + str + "\", data model requires years and months only.");
        }
    }

    public Duration newDurationDayTime(long j) {
        return newDuration(j);
    }

    public Duration newDurationDayTime(boolean z, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        return newDuration(z, (BigInteger) null, (BigInteger) null, bigInteger, bigInteger2, bigInteger3, bigInteger4 != null ? new BigDecimal(bigInteger4) : null);
    }

    public Duration newDurationDayTime(boolean z, int i, int i2, int i3, int i4) {
        return newDurationDayTime(z, BigInteger.valueOf((long) i), BigInteger.valueOf((long) i2), BigInteger.valueOf((long) i3), BigInteger.valueOf((long) i4));
    }

    public Duration newDurationYearMonth(String str) {
        if (str == null) {
            throw new NullPointerException("Trying to create an xdt:yearMonthDuration with an invalid lexical representation of \"null\"");
        } else if (XDTSCHEMA_YMD.matcher(str).matches()) {
            return newDuration(str);
        } else {
            throw new IllegalArgumentException("Trying to create an xdt:yearMonthDuration with an invalid lexical representation of \"" + str + "\", data model requires days and times only.");
        }
    }

    public Duration newDurationYearMonth(long j) {
        Duration newDuration = newDuration(j);
        boolean z = newDuration.getSign() != -1;
        BigInteger bigInteger = (BigInteger) newDuration.getField(DatatypeConstants.YEARS);
        if (bigInteger == null) {
            bigInteger = BigInteger.ZERO;
        }
        BigInteger bigInteger2 = (BigInteger) newDuration.getField(DatatypeConstants.MONTHS);
        if (bigInteger2 == null) {
            bigInteger2 = BigInteger.ZERO;
        }
        return newDurationYearMonth(z, bigInteger, bigInteger2);
    }

    public Duration newDurationYearMonth(boolean z, BigInteger bigInteger, BigInteger bigInteger2) {
        return newDuration(z, bigInteger, bigInteger2, (BigInteger) null, (BigInteger) null, (BigInteger) null, (BigDecimal) null);
    }

    public Duration newDurationYearMonth(boolean z, int i, int i2) {
        return newDurationYearMonth(z, BigInteger.valueOf((long) i), BigInteger.valueOf((long) i2));
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        BigInteger bigInteger;
        BigDecimal bigDecimal = null;
        if (i != Integer.MIN_VALUE) {
            bigInteger = BigInteger.valueOf((long) i);
        } else {
            bigInteger = null;
        }
        if (i7 != Integer.MIN_VALUE) {
            if (i7 < 0 || i7 > 1000) {
                throw new IllegalArgumentException("ohos.javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendar(int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone)with invalid millisecond: " + i7);
            }
            bigDecimal = BigDecimal.valueOf((long) i7).movePointLeft(3);
        }
        return newXMLGregorianCalendar(bigInteger, i2, i3, i4, i5, i6, bigDecimal, i8);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarDate(int i, int i2, int i3, int i4) {
        return newXMLGregorianCalendar(i, i2, i3, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, i4);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int i, int i2, int i3, int i4) {
        return newXMLGregorianCalendar(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, i, i2, i3, Integer.MIN_VALUE, i4);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int i, int i2, int i3, BigDecimal bigDecimal, int i4) {
        return newXMLGregorianCalendar((BigInteger) null, Integer.MIN_VALUE, Integer.MIN_VALUE, i, i2, i3, bigDecimal, i4);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int i, int i2, int i3, int i4, int i5) {
        BigDecimal bigDecimal;
        if (i4 == Integer.MIN_VALUE) {
            bigDecimal = null;
        } else if (i4 < 0 || i4 > 1000) {
            throw new IllegalArgumentException("ohos.javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int milliseconds, int timezone)with invalid milliseconds: " + i4);
        } else {
            bigDecimal = BigDecimal.valueOf((long) i4).movePointLeft(3);
        }
        return newXMLGregorianCalendarTime(i, i2, i3, bigDecimal, i5);
    }
}

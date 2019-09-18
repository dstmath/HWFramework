package javax.xml.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import javax.xml.datatype.FactoryFinder;
import libcore.icu.RelativeDateTimeFormatter;

public abstract class DatatypeFactory {
    public static final String DATATYPEFACTORY_IMPLEMENTATION_CLASS = new String("org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl");
    public static final String DATATYPEFACTORY_PROPERTY = "javax.xml.datatype.DatatypeFactory";

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
        try {
            return (DatatypeFactory) FactoryFinder.find(DATATYPEFACTORY_PROPERTY, DATATYPEFACTORY_IMPLEMENTATION_CLASS);
        } catch (FactoryFinder.ConfigurationError e) {
            throw new DatatypeConfigurationException(e.getMessage(), e.getException());
        }
    }

    public static DatatypeFactory newInstance(String factoryClassName, ClassLoader classLoader) throws DatatypeConfigurationException {
        Class<?> type;
        if (factoryClassName != null) {
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            if (classLoader != null) {
                try {
                    type = classLoader.loadClass(factoryClassName);
                } catch (ClassNotFoundException e) {
                    throw new DatatypeConfigurationException((Throwable) e);
                } catch (InstantiationException e2) {
                    throw new DatatypeConfigurationException((Throwable) e2);
                } catch (IllegalAccessException e3) {
                    throw new DatatypeConfigurationException((Throwable) e3);
                }
            } else {
                type = Class.forName(factoryClassName);
            }
            return (DatatypeFactory) type.newInstance();
        }
        throw new DatatypeConfigurationException("factoryClassName == null");
    }

    public Duration newDuration(boolean isPositive, int years, int months, int days, int hours, int minutes, int seconds) {
        int i = years;
        int i2 = months;
        int i3 = days;
        int i4 = hours;
        int i5 = minutes;
        int i6 = seconds;
        BigDecimal realSeconds = null;
        BigInteger realYears = i != Integer.MIN_VALUE ? BigInteger.valueOf((long) i) : null;
        BigInteger realMonths = i2 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i2) : null;
        BigInteger realDays = i3 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i3) : null;
        BigInteger realHours = i4 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i4) : null;
        BigInteger realMinutes = i5 != Integer.MIN_VALUE ? BigInteger.valueOf((long) i5) : null;
        if (i6 != Integer.MIN_VALUE) {
            realSeconds = BigDecimal.valueOf((long) i6);
        }
        return newDuration(isPositive, realYears, realMonths, realDays, realHours, realMinutes, realSeconds);
    }

    public Duration newDurationDayTime(String lexicalRepresentation) {
        if (lexicalRepresentation != null) {
            int pos = lexicalRepresentation.indexOf(84);
            int length = pos >= 0 ? pos : lexicalRepresentation.length();
            for (int i = 0; i < length; i++) {
                char c = lexicalRepresentation.charAt(i);
                if (c == 'Y' || c == 'M') {
                    throw new IllegalArgumentException("Invalid dayTimeDuration value: " + lexicalRepresentation);
                }
            }
            return newDuration(lexicalRepresentation);
        }
        throw new NullPointerException("lexicalRepresentation == null");
    }

    public Duration newDurationDayTime(long durationInMilliseconds) {
        boolean isPositive;
        long _durationInMilliseconds = durationInMilliseconds;
        if (_durationInMilliseconds == 0) {
            return newDuration(true, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, 0, 0);
        }
        boolean tooLong = false;
        if (_durationInMilliseconds < 0) {
            isPositive = false;
            if (_durationInMilliseconds == Long.MIN_VALUE) {
                _durationInMilliseconds++;
                tooLong = true;
            }
            _durationInMilliseconds *= -1;
        } else {
            isPositive = true;
        }
        boolean isPositive2 = isPositive;
        long val = _durationInMilliseconds;
        int milliseconds = (int) (val % RelativeDateTimeFormatter.MINUTE_IN_MILLIS);
        if (tooLong) {
            milliseconds++;
        }
        int milliseconds2 = milliseconds;
        if (milliseconds2 % 1000 == 0) {
            int seconds = milliseconds2 / 1000;
            long val2 = val / RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
            int minutes = (int) (val2 % 60);
            long val3 = val2 / 60;
            int hours = (int) (val3 % 24);
            long days = val3 / 24;
            if (days <= 2147483647L) {
                long j = days;
                int i = seconds;
                return newDuration(isPositive2, Integer.MIN_VALUE, Integer.MIN_VALUE, (int) days, hours, minutes, seconds);
            }
            int i2 = seconds;
            return newDuration(isPositive2, (BigInteger) null, (BigInteger) null, BigInteger.valueOf(days), BigInteger.valueOf((long) hours), BigInteger.valueOf((long) minutes), BigDecimal.valueOf((long) milliseconds2, 3));
        }
        BigDecimal seconds2 = BigDecimal.valueOf((long) milliseconds2, 3);
        long val4 = val / RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
        BigInteger minutes2 = BigInteger.valueOf(val4 % 60);
        long val5 = val4 / 60;
        return newDuration(isPositive2, (BigInteger) null, (BigInteger) null, BigInteger.valueOf(val5 / 24), BigInteger.valueOf(val5 % 24), minutes2, seconds2);
    }

    public Duration newDurationDayTime(boolean isPositive, BigInteger day, BigInteger hour, BigInteger minute, BigInteger second) {
        BigDecimal bigDecimal;
        if (second != null) {
            bigDecimal = new BigDecimal(second);
        } else {
            bigDecimal = null;
        }
        return newDuration(isPositive, (BigInteger) null, (BigInteger) null, day, hour, minute, bigDecimal);
    }

    public Duration newDurationDayTime(boolean isPositive, int day, int hour, int minute, int second) {
        return newDuration(isPositive, Integer.MIN_VALUE, Integer.MIN_VALUE, day, hour, minute, second);
    }

    public Duration newDurationYearMonth(String lexicalRepresentation) {
        if (lexicalRepresentation != null) {
            int length = lexicalRepresentation.length();
            for (int i = 0; i < length; i++) {
                char c = lexicalRepresentation.charAt(i);
                if (c == 'D' || c == 'T') {
                    throw new IllegalArgumentException("Invalid yearMonthDuration value: " + lexicalRepresentation);
                }
            }
            return newDuration(lexicalRepresentation);
        }
        throw new NullPointerException("lexicalRepresentation == null");
    }

    public Duration newDurationYearMonth(long durationInMilliseconds) {
        return newDuration(durationInMilliseconds);
    }

    public Duration newDurationYearMonth(boolean isPositive, BigInteger year, BigInteger month) {
        return newDuration(isPositive, year, month, (BigInteger) null, (BigInteger) null, (BigInteger) null, (BigDecimal) null);
    }

    public Duration newDurationYearMonth(boolean isPositive, int year, int month) {
        return newDuration(isPositive, year, month, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone) {
        BigDecimal realMillisecond;
        int i = year;
        int i2 = millisecond;
        BigInteger realYear = i != Integer.MIN_VALUE ? BigInteger.valueOf((long) i) : null;
        if (i2 == Integer.MIN_VALUE) {
            realMillisecond = null;
        } else if (i2 < 0 || i2 > 1000) {
            throw new IllegalArgumentException("javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendar(int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone)with invalid millisecond: " + i2);
        } else {
            realMillisecond = BigDecimal.valueOf((long) i2, 3);
        }
        return newXMLGregorianCalendar(realYear, month, day, hour, minute, second, realMillisecond, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarDate(int year, int month, int day, int timezone) {
        return newXMLGregorianCalendar(year, month, day, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int timezone) {
        return newXMLGregorianCalendar(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, hours, minutes, seconds, Integer.MIN_VALUE, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, BigDecimal fractionalSecond, int timezone) {
        return newXMLGregorianCalendar((BigInteger) null, Integer.MIN_VALUE, Integer.MIN_VALUE, hours, minutes, seconds, fractionalSecond, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int milliseconds, int timezone) {
        BigDecimal realMilliseconds = null;
        if (milliseconds != Integer.MIN_VALUE) {
            if (milliseconds < 0 || milliseconds > 1000) {
                throw new IllegalArgumentException("javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int milliseconds, int timezone)with invalid milliseconds: " + milliseconds);
            }
            realMilliseconds = BigDecimal.valueOf((long) milliseconds, 3);
        }
        return newXMLGregorianCalendarTime(hours, minutes, seconds, realMilliseconds, timezone);
    }
}

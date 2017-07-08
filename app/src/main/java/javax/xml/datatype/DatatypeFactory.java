package javax.xml.datatype;

import android.icu.impl.Grego;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import libcore.icu.RelativeDateTimeFormatter;

public abstract class DatatypeFactory {
    public static final String DATATYPEFACTORY_IMPLEMENTATION_CLASS = null;
    public static final String DATATYPEFACTORY_PROPERTY = "javax.xml.datatype.DatatypeFactory";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.xml.datatype.DatatypeFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.xml.datatype.DatatypeFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.xml.datatype.DatatypeFactory.<clinit>():void");
    }

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
        } catch (ConfigurationError e) {
            throw new DatatypeConfigurationException(e.getMessage(), e.getException());
        }
    }

    public static DatatypeFactory newInstance(String factoryClassName, ClassLoader classLoader) throws DatatypeConfigurationException {
        if (factoryClassName == null) {
            throw new DatatypeConfigurationException("factoryClassName == null");
        }
        Class<?> type;
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader != null) {
            try {
                type = classLoader.loadClass(factoryClassName);
            } catch (Throwable e) {
                throw new DatatypeConfigurationException(e);
            } catch (Throwable e2) {
                throw new DatatypeConfigurationException(e2);
            } catch (Throwable e3) {
                throw new DatatypeConfigurationException(e3);
            }
        }
        type = Class.forName(factoryClassName);
        return (DatatypeFactory) type.newInstance();
    }

    public Duration newDuration(boolean isPositive, int years, int months, int days, int hours, int minutes, int seconds) {
        return newDuration(isPositive, years != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) years) : null, months != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) months) : null, days != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) days) : null, hours != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) hours) : null, minutes != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) minutes) : null, seconds != DatatypeConstants.FIELD_UNDEFINED ? BigDecimal.valueOf((long) seconds) : null);
    }

    public Duration newDurationDayTime(String lexicalRepresentation) {
        if (lexicalRepresentation == null) {
            throw new NullPointerException("lexicalRepresentation == null");
        }
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

    public Duration newDurationDayTime(long durationInMilliseconds) {
        long _durationInMilliseconds = durationInMilliseconds;
        if (durationInMilliseconds == 0) {
            return newDuration(true, (int) DatatypeConstants.FIELD_UNDEFINED, (int) DatatypeConstants.FIELD_UNDEFINED, 0, 0, 0, 0);
        }
        boolean isPositive;
        boolean tooLong = false;
        if (durationInMilliseconds < 0) {
            isPositive = false;
            if (durationInMilliseconds == Long.MIN_VALUE) {
                _durationInMilliseconds = durationInMilliseconds + 1;
                tooLong = true;
            }
            _durationInMilliseconds *= -1;
        } else {
            isPositive = true;
        }
        long val = _durationInMilliseconds;
        int milliseconds = (int) (_durationInMilliseconds % RelativeDateTimeFormatter.MINUTE_IN_MILLIS);
        if (tooLong) {
            milliseconds++;
        }
        if (milliseconds % Grego.MILLIS_PER_SECOND == 0) {
            int seconds = milliseconds / Grego.MILLIS_PER_SECOND;
            val /= RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
            int minutes = (int) (val % 60);
            val /= 60;
            int hours = (int) (val % 24);
            long days = val / 24;
            if (days <= 2147483647L) {
                return newDuration(isPositive, (int) DatatypeConstants.FIELD_UNDEFINED, (int) DatatypeConstants.FIELD_UNDEFINED, (int) days, hours, minutes, seconds);
            }
            return newDuration(isPositive, null, null, BigInteger.valueOf(days), BigInteger.valueOf((long) hours), BigInteger.valueOf((long) minutes), BigDecimal.valueOf((long) milliseconds, 3));
        }
        BigDecimal seconds2 = BigDecimal.valueOf((long) milliseconds, 3);
        val /= RelativeDateTimeFormatter.MINUTE_IN_MILLIS;
        BigInteger minutes2 = BigInteger.valueOf(val % 60);
        val /= 60;
        return newDuration(isPositive, null, null, BigInteger.valueOf(val / 24), BigInteger.valueOf(val % 24), minutes2, seconds2);
    }

    public Duration newDurationDayTime(boolean isPositive, BigInteger day, BigInteger hour, BigInteger minute, BigInteger second) {
        return newDuration(isPositive, null, null, day, hour, minute, second != null ? new BigDecimal(second) : null);
    }

    public Duration newDurationDayTime(boolean isPositive, int day, int hour, int minute, int second) {
        return newDuration(isPositive, (int) DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, day, hour, minute, second);
    }

    public Duration newDurationYearMonth(String lexicalRepresentation) {
        if (lexicalRepresentation == null) {
            throw new NullPointerException("lexicalRepresentation == null");
        }
        int length = lexicalRepresentation.length();
        for (int i = 0; i < length; i++) {
            char c = lexicalRepresentation.charAt(i);
            if (c == 'D' || c == 'T') {
                throw new IllegalArgumentException("Invalid yearMonthDuration value: " + lexicalRepresentation);
            }
        }
        return newDuration(lexicalRepresentation);
    }

    public Duration newDurationYearMonth(long durationInMilliseconds) {
        return newDuration(durationInMilliseconds);
    }

    public Duration newDurationYearMonth(boolean isPositive, BigInteger year, BigInteger month) {
        return newDuration(isPositive, year, month, null, null, null, null);
    }

    public Duration newDurationYearMonth(boolean isPositive, int year, int month) {
        return newDuration(isPositive, year, month, (int) DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
    }

    public XMLGregorianCalendar newXMLGregorianCalendar(int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone) {
        BigInteger valueOf = year != DatatypeConstants.FIELD_UNDEFINED ? BigInteger.valueOf((long) year) : null;
        BigDecimal bigDecimal = null;
        if (millisecond != Integer.MIN_VALUE) {
            if (millisecond < 0 || millisecond > 1000) {
                throw new IllegalArgumentException("javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendar(int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone)with invalid millisecond: " + millisecond);
            }
            bigDecimal = BigDecimal.valueOf((long) millisecond, 3);
        }
        return newXMLGregorianCalendar(valueOf, month, day, hour, minute, second, bigDecimal, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarDate(int year, int month, int day, int timezone) {
        return newXMLGregorianCalendar(year, month, day, (int) DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int timezone) {
        return newXMLGregorianCalendar((int) DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, hours, minutes, seconds, DatatypeConstants.FIELD_UNDEFINED, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, BigDecimal fractionalSecond, int timezone) {
        return newXMLGregorianCalendar(null, (int) DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, hours, minutes, seconds, fractionalSecond, timezone);
    }

    public XMLGregorianCalendar newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int milliseconds, int timezone) {
        BigDecimal realMilliseconds = null;
        if (milliseconds != DatatypeConstants.FIELD_UNDEFINED) {
            if (milliseconds < 0 || milliseconds > Grego.MILLIS_PER_SECOND) {
                throw new IllegalArgumentException("javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendarTime(int hours, int minutes, int seconds, int milliseconds, int timezone)with invalid milliseconds: " + milliseconds);
            }
            realMilliseconds = BigDecimal.valueOf((long) milliseconds, 3);
        }
        return newXMLGregorianCalendarTime(hours, minutes, seconds, realMilliseconds, timezone);
    }
}

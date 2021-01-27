package ohos.com.sun.org.apache.xerces.internal.jaxp.datatype;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import ohos.com.sun.org.apache.xerces.internal.util.DatatypeMessageFormatter;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.CalendarAstronomer;
import ohos.javax.xml.datatype.DatatypeConstants;
import ohos.javax.xml.datatype.Duration;
import ohos.javax.xml.datatype.XMLGregorianCalendar;

/* access modifiers changed from: package-private */
public class DurationImpl extends Duration implements Serializable {
    private static final BigDecimal[] FACTORS = {BigDecimal.valueOf(12L), null, BigDecimal.valueOf(24L), BigDecimal.valueOf(60L), BigDecimal.valueOf(60L)};
    private static final DatatypeConstants.Field[] FIELDS = {DatatypeConstants.YEARS, DatatypeConstants.MONTHS, DatatypeConstants.DAYS, DatatypeConstants.HOURS, DatatypeConstants.MINUTES, DatatypeConstants.SECONDS};
    private static final int[] FIELD_IDS = {DatatypeConstants.YEARS.getId(), DatatypeConstants.MONTHS.getId(), DatatypeConstants.DAYS.getId(), DatatypeConstants.HOURS.getId(), DatatypeConstants.MINUTES.getId(), DatatypeConstants.SECONDS.getId()};
    private static final int FIELD_NUM = 6;
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final XMLGregorianCalendar[] TEST_POINTS = {XMLGregorianCalendarImpl.parse("1696-09-01T00:00:00Z"), XMLGregorianCalendarImpl.parse("1697-02-01T00:00:00Z"), XMLGregorianCalendarImpl.parse("1903-03-01T00:00:00Z"), XMLGregorianCalendarImpl.parse("1903-07-01T00:00:00Z")};
    private static final BigDecimal ZERO = BigDecimal.valueOf(0L);
    private static final long serialVersionUID = 1;
    protected BigInteger days;
    protected BigInteger hours;
    protected BigInteger minutes;
    protected BigInteger months;
    protected BigDecimal seconds;
    protected int signum;
    protected BigInteger years;

    private int compareResults(int i, int i2) {
        if (i2 != 2 && i == i2) {
            return i;
        }
        return 2;
    }

    private static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    public int getSign() {
        return this.signum;
    }

    /* access modifiers changed from: protected */
    public int calcSignum(boolean z) {
        BigInteger bigInteger;
        BigInteger bigInteger2;
        BigInteger bigInteger3;
        BigInteger bigInteger4;
        BigDecimal bigDecimal;
        BigInteger bigInteger5 = this.years;
        if ((bigInteger5 == null || bigInteger5.signum() == 0) && (((bigInteger = this.months) == null || bigInteger.signum() == 0) && (((bigInteger2 = this.days) == null || bigInteger2.signum() == 0) && (((bigInteger3 = this.hours) == null || bigInteger3.signum() == 0) && (((bigInteger4 = this.minutes) == null || bigInteger4.signum() == 0) && ((bigDecimal = this.seconds) == null || bigDecimal.signum() == 0)))))) {
            return 0;
        }
        return z ? 1 : -1;
    }

    protected DurationImpl(boolean z, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, BigInteger bigInteger5, BigDecimal bigDecimal) {
        this.years = bigInteger;
        this.months = bigInteger2;
        this.days = bigInteger3;
        this.hours = bigInteger4;
        this.minutes = bigInteger5;
        this.seconds = bigDecimal;
        this.signum = calcSignum(z);
        if (bigInteger == null && bigInteger2 == null && bigInteger3 == null && bigInteger4 == null && bigInteger5 == null && bigDecimal == null) {
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "AllFieldsNull", null));
        }
        testNonNegative(bigInteger, DatatypeConstants.YEARS);
        testNonNegative(bigInteger2, DatatypeConstants.MONTHS);
        testNonNegative(bigInteger3, DatatypeConstants.DAYS);
        testNonNegative(bigInteger4, DatatypeConstants.HOURS);
        testNonNegative(bigInteger5, DatatypeConstants.MINUTES);
        testNonNegative(bigDecimal, DatatypeConstants.SECONDS);
    }

    protected static void testNonNegative(BigInteger bigInteger, DatatypeConstants.Field field) {
        if (bigInteger != null && bigInteger.signum() < 0) {
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "NegativeField", new Object[]{field.toString()}));
        }
    }

    protected static void testNonNegative(BigDecimal bigDecimal, DatatypeConstants.Field field) {
        if (bigDecimal != null && bigDecimal.signum() < 0) {
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "NegativeField", new Object[]{field.toString()}));
        }
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    protected DurationImpl(boolean z, int i, int i2, int i3, int i4, int i5, int i6) {
        this(z, wrap(i), wrap(i2), wrap(i3), wrap(i4), wrap(i5), i6 != Integer.MIN_VALUE ? new BigDecimal(String.valueOf(i6)) : null);
    }

    protected static BigInteger wrap(int i) {
        if (i == Integer.MIN_VALUE) {
            return null;
        }
        return new BigInteger(String.valueOf(i));
    }

    protected DurationImpl(long j) {
        int i = (j > 0 ? 1 : (j == 0 ? 0 : -1));
        if (i > 0) {
            this.signum = 1;
        } else if (i < 0) {
            this.signum = -1;
            j = (j == Long.MIN_VALUE ? j + serialVersionUID : j) * -1;
        } else {
            this.signum = 0;
        }
        GregorianCalendar gregorianCalendar = new GregorianCalendar(GMT);
        gregorianCalendar.setTimeInMillis(j);
        this.years = BigInteger.valueOf((long) (gregorianCalendar.get(1) - 1970));
        this.months = BigInteger.valueOf((long) gregorianCalendar.get(2));
        this.days = BigInteger.valueOf((long) (gregorianCalendar.get(5) - 1));
        this.hours = BigInteger.valueOf((long) gregorianCalendar.get(11));
        this.minutes = BigInteger.valueOf((long) gregorianCalendar.get(12));
        this.seconds = BigDecimal.valueOf((long) ((gregorianCalendar.get(13) * 1000) + gregorianCalendar.get(14)), 3);
    }

    protected DurationImpl(String str) throws IllegalArgumentException {
        boolean z;
        boolean z2;
        int length = str.length();
        int[] iArr = {0};
        if (length == iArr[0] || str.charAt(iArr[0]) != '-') {
            z = true;
        } else {
            iArr[0] = iArr[0] + 1;
            z = false;
        }
        if (length != iArr[0]) {
            int i = iArr[0];
            iArr[0] = i + 1;
            if (str.charAt(i) != 'P') {
                throw new IllegalArgumentException(str);
            }
        }
        String[] strArr = new String[3];
        int[] iArr2 = new int[3];
        int i2 = 0;
        while (length != iArr[0] && isDigit(str.charAt(iArr[0])) && i2 < 3) {
            iArr2[i2] = iArr[0];
            strArr[i2] = parsePiece(str, iArr);
            i2++;
        }
        if (length != iArr[0]) {
            int i3 = iArr[0];
            iArr[0] = i3 + 1;
            if (str.charAt(i3) == 'T') {
                z2 = true;
            } else {
                throw new IllegalArgumentException(str);
            }
        } else {
            z2 = false;
        }
        String[] strArr2 = new String[3];
        int[] iArr3 = new int[3];
        int i4 = 0;
        while (length != iArr[0] && isDigitOrPeriod(str.charAt(iArr[0])) && i4 < 3) {
            iArr3[i4] = iArr[0];
            strArr2[i4] = parsePiece(str, iArr);
            i4++;
        }
        if (z2 && i4 == 0) {
            throw new IllegalArgumentException(str);
        } else if (length != iArr[0]) {
            throw new IllegalArgumentException(str);
        } else if (i2 == 0 && i4 == 0) {
            throw new IllegalArgumentException(str);
        } else {
            organizeParts(str, strArr, iArr2, i2, "YMD");
            organizeParts(str, strArr2, iArr3, i4, "HMS");
            this.years = parseBigInteger(str, strArr[0], iArr2[0]);
            this.months = parseBigInteger(str, strArr[1], iArr2[1]);
            this.days = parseBigInteger(str, strArr[2], iArr2[2]);
            this.hours = parseBigInteger(str, strArr2[0], iArr3[0]);
            this.minutes = parseBigInteger(str, strArr2[1], iArr3[1]);
            this.seconds = parseBigDecimal(str, strArr2[2], iArr3[2]);
            this.signum = calcSignum(z);
        }
    }

    private static boolean isDigitOrPeriod(char c) {
        return isDigit(c) || c == '.';
    }

    private static String parsePiece(String str, int[] iArr) throws IllegalArgumentException {
        int i = iArr[0];
        while (iArr[0] < str.length() && isDigitOrPeriod(str.charAt(iArr[0]))) {
            iArr[0] = iArr[0] + 1;
        }
        if (iArr[0] != str.length()) {
            iArr[0] = iArr[0] + 1;
            return str.substring(i, iArr[0]);
        }
        throw new IllegalArgumentException(str);
    }

    private static void organizeParts(String str, String[] strArr, int[] iArr, int i, String str2) throws IllegalArgumentException {
        int length = str2.length();
        int i2 = i - 1;
        while (i2 >= 0) {
            int lastIndexOf = str2.lastIndexOf(strArr[i2].charAt(strArr[i2].length() - 1), length - 1);
            if (lastIndexOf != -1) {
                for (int i3 = lastIndexOf + 1; i3 < length; i3++) {
                    strArr[i3] = null;
                }
                strArr[lastIndexOf] = strArr[i2];
                iArr[lastIndexOf] = iArr[i2];
                i2--;
                length = lastIndexOf;
            } else {
                throw new IllegalArgumentException(str);
            }
        }
        for (int i4 = length - 1; i4 >= 0; i4--) {
            strArr[i4] = null;
        }
    }

    private static BigInteger parseBigInteger(String str, String str2, int i) throws IllegalArgumentException {
        if (str2 == null) {
            return null;
        }
        return new BigInteger(str2.substring(0, str2.length() - 1));
    }

    private static BigDecimal parseBigDecimal(String str, String str2, int i) throws IllegalArgumentException {
        if (str2 == null) {
            return null;
        }
        return new BigDecimal(str2.substring(0, str2.length() - 1));
    }

    public int compare(Duration duration) {
        BigInteger valueOf = BigInteger.valueOf(2147483647L);
        BigInteger.valueOf(-2147483648L);
        BigInteger bigInteger = this.years;
        if (bigInteger == null || bigInteger.compareTo(valueOf) != 1) {
            BigInteger bigInteger2 = this.months;
            if (bigInteger2 == null || bigInteger2.compareTo(valueOf) != 1) {
                BigInteger bigInteger3 = this.days;
                if (bigInteger3 == null || bigInteger3.compareTo(valueOf) != 1) {
                    BigInteger bigInteger4 = this.hours;
                    if (bigInteger4 == null || bigInteger4.compareTo(valueOf) != 1) {
                        BigInteger bigInteger5 = this.minutes;
                        if (bigInteger5 == null || bigInteger5.compareTo(valueOf) != 1) {
                            BigDecimal bigDecimal = this.seconds;
                            if (bigDecimal == null || bigDecimal.toBigInteger().compareTo(valueOf) != 1) {
                                BigInteger bigInteger6 = (BigInteger) duration.getField(DatatypeConstants.YEARS);
                                if (bigInteger6 == null || bigInteger6.compareTo(valueOf) != 1) {
                                    BigInteger bigInteger7 = (BigInteger) duration.getField(DatatypeConstants.MONTHS);
                                    if (bigInteger7 == null || bigInteger7.compareTo(valueOf) != 1) {
                                        BigInteger bigInteger8 = (BigInteger) duration.getField(DatatypeConstants.DAYS);
                                        if (bigInteger8 == null || bigInteger8.compareTo(valueOf) != 1) {
                                            BigInteger bigInteger9 = (BigInteger) duration.getField(DatatypeConstants.HOURS);
                                            if (bigInteger9 == null || bigInteger9.compareTo(valueOf) != 1) {
                                                BigInteger bigInteger10 = (BigInteger) duration.getField(DatatypeConstants.MINUTES);
                                                if (bigInteger10 == null || bigInteger10.compareTo(valueOf) != 1) {
                                                    BigDecimal bigDecimal2 = (BigDecimal) duration.getField(DatatypeConstants.SECONDS);
                                                    BigInteger bigInteger11 = bigDecimal2 != null ? bigDecimal2.toBigInteger() : null;
                                                    if (bigInteger11 == null || bigInteger11.compareTo(valueOf) != 1) {
                                                        GregorianCalendar gregorianCalendar = new GregorianCalendar(1970, 1, 1, 0, 0, 0);
                                                        gregorianCalendar.add(1, getYears() * getSign());
                                                        gregorianCalendar.add(2, getMonths() * getSign());
                                                        gregorianCalendar.add(6, getDays() * getSign());
                                                        gregorianCalendar.add(11, getHours() * getSign());
                                                        gregorianCalendar.add(12, getMinutes() * getSign());
                                                        gregorianCalendar.add(13, getSeconds() * getSign());
                                                        GregorianCalendar gregorianCalendar2 = new GregorianCalendar(1970, 1, 1, 0, 0, 0);
                                                        gregorianCalendar2.add(1, duration.getYears() * duration.getSign());
                                                        gregorianCalendar2.add(2, duration.getMonths() * duration.getSign());
                                                        gregorianCalendar2.add(6, duration.getDays() * duration.getSign());
                                                        gregorianCalendar2.add(11, duration.getHours() * duration.getSign());
                                                        gregorianCalendar2.add(12, duration.getMinutes() * duration.getSign());
                                                        gregorianCalendar2.add(13, duration.getSeconds() * duration.getSign());
                                                        if (gregorianCalendar.equals(gregorianCalendar2)) {
                                                            return 0;
                                                        }
                                                        return compareDates(this, duration);
                                                    }
                                                    throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.SECONDS.toString(), bigInteger11.toString()}));
                                                }
                                                throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MINUTES.toString(), bigInteger10.toString()}));
                                            }
                                            throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.HOURS.toString(), bigInteger9.toString()}));
                                        }
                                        throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.DAYS.toString(), bigInteger8.toString()}));
                                    }
                                    throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MONTHS.toString(), bigInteger7.toString()}));
                                }
                                throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.YEARS.toString(), bigInteger6.toString()}));
                            }
                            throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.SECONDS.toString(), this.seconds.toString()}));
                        }
                        throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MINUTES.toString(), this.minutes.toString()}));
                    }
                    throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.HOURS.toString(), this.hours.toString()}));
                }
                throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.DAYS.toString(), this.days.toString()}));
            }
            throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.MONTHS.toString(), this.months.toString()}));
        }
        throw new UnsupportedOperationException(DatatypeMessageFormatter.formatMessage(null, "TooLarge", new Object[]{getClass().getName() + "#compare(Duration duration)" + DatatypeConstants.YEARS.toString(), this.years.toString()}));
    }

    private int compareDates(Duration duration, Duration duration2) {
        XMLGregorianCalendar xMLGregorianCalendar = (XMLGregorianCalendar) TEST_POINTS[0].clone();
        XMLGregorianCalendar xMLGregorianCalendar2 = (XMLGregorianCalendar) TEST_POINTS[0].clone();
        xMLGregorianCalendar.add(duration);
        xMLGregorianCalendar2.add(duration2);
        int compare = xMLGregorianCalendar.compare(xMLGregorianCalendar2);
        if (compare == 2) {
            return 2;
        }
        XMLGregorianCalendar xMLGregorianCalendar3 = (XMLGregorianCalendar) TEST_POINTS[1].clone();
        XMLGregorianCalendar xMLGregorianCalendar4 = (XMLGregorianCalendar) TEST_POINTS[1].clone();
        xMLGregorianCalendar3.add(duration);
        xMLGregorianCalendar4.add(duration2);
        int compareResults = compareResults(compare, xMLGregorianCalendar3.compare(xMLGregorianCalendar4));
        if (compareResults == 2) {
            return 2;
        }
        XMLGregorianCalendar xMLGregorianCalendar5 = (XMLGregorianCalendar) TEST_POINTS[2].clone();
        XMLGregorianCalendar xMLGregorianCalendar6 = (XMLGregorianCalendar) TEST_POINTS[2].clone();
        xMLGregorianCalendar5.add(duration);
        xMLGregorianCalendar6.add(duration2);
        int compareResults2 = compareResults(compareResults, xMLGregorianCalendar5.compare(xMLGregorianCalendar6));
        if (compareResults2 == 2) {
            return 2;
        }
        XMLGregorianCalendar xMLGregorianCalendar7 = (XMLGregorianCalendar) TEST_POINTS[3].clone();
        XMLGregorianCalendar xMLGregorianCalendar8 = (XMLGregorianCalendar) TEST_POINTS[3].clone();
        xMLGregorianCalendar7.add(duration);
        xMLGregorianCalendar8.add(duration2);
        return compareResults(compareResults2, xMLGregorianCalendar7.compare(xMLGregorianCalendar8));
    }

    @Override // java.lang.Object
    public int hashCode() {
        GregorianCalendar gregorianCalendar = TEST_POINTS[0].toGregorianCalendar();
        addTo(gregorianCalendar);
        return (int) getCalendarTimeInMillis(gregorianCalendar);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        if (this.signum < 0) {
            stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
        }
        stringBuffer.append('P');
        if (this.years != null) {
            stringBuffer.append(this.years + "Y");
        }
        if (this.months != null) {
            stringBuffer.append(this.months + "M");
        }
        if (this.days != null) {
            stringBuffer.append(this.days + "D");
        }
        if (!(this.hours == null && this.minutes == null && this.seconds == null)) {
            stringBuffer.append('T');
            if (this.hours != null) {
                stringBuffer.append(this.hours + "H");
            }
            if (this.minutes != null) {
                stringBuffer.append(this.minutes + "M");
            }
            if (this.seconds != null) {
                stringBuffer.append(toString(this.seconds) + "S");
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

    public boolean isSet(DatatypeConstants.Field field) {
        if (field == null) {
            throw new NullPointerException(DatatypeMessageFormatter.formatMessage(null, "FieldCannotBeNull", new Object[]{"ohos.javax.xml.datatype.Duration#isSet(DatatypeConstants.Field field)"}));
        } else if (field == DatatypeConstants.YEARS) {
            return this.years != null;
        } else {
            if (field == DatatypeConstants.MONTHS) {
                return this.months != null;
            }
            if (field == DatatypeConstants.DAYS) {
                return this.days != null;
            }
            if (field == DatatypeConstants.HOURS) {
                return this.hours != null;
            }
            if (field == DatatypeConstants.MINUTES) {
                return this.minutes != null;
            }
            if (field == DatatypeConstants.SECONDS) {
                return this.seconds != null;
            }
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "UnknownField", new Object[]{"ohos.javax.xml.datatype.Duration#isSet(DatatypeConstants.Field field)", field.toString()}));
        }
    }

    public Number getField(DatatypeConstants.Field field) {
        if (field == null) {
            throw new NullPointerException(DatatypeMessageFormatter.formatMessage(null, "FieldCannotBeNull", new Object[]{"ohos.javax.xml.datatype.Duration#isSet(DatatypeConstants.Field field) "}));
        } else if (field == DatatypeConstants.YEARS) {
            return this.years;
        } else {
            if (field == DatatypeConstants.MONTHS) {
                return this.months;
            }
            if (field == DatatypeConstants.DAYS) {
                return this.days;
            }
            if (field == DatatypeConstants.HOURS) {
                return this.hours;
            }
            if (field == DatatypeConstants.MINUTES) {
                return this.minutes;
            }
            if (field == DatatypeConstants.SECONDS) {
                return this.seconds;
            }
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "UnknownField", new Object[]{"ohos.javax.xml.datatype.Duration#(getSet(DatatypeConstants.Field field)", field.toString()}));
        }
    }

    public int getYears() {
        return getInt(DatatypeConstants.YEARS);
    }

    public int getMonths() {
        return getInt(DatatypeConstants.MONTHS);
    }

    public int getDays() {
        return getInt(DatatypeConstants.DAYS);
    }

    public int getHours() {
        return getInt(DatatypeConstants.HOURS);
    }

    public int getMinutes() {
        return getInt(DatatypeConstants.MINUTES);
    }

    public int getSeconds() {
        return getInt(DatatypeConstants.SECONDS);
    }

    private int getInt(DatatypeConstants.Field field) {
        Number field2 = getField(field);
        if (field2 == null) {
            return 0;
        }
        return field2.intValue();
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

    public Duration normalizeWith(Calendar calendar) {
        Calendar calendar2 = (Calendar) calendar.clone();
        boolean z = true;
        calendar2.add(1, getYears() * this.signum);
        calendar2.add(2, getMonths() * this.signum);
        calendar2.add(5, getDays() * this.signum);
        int calendarTimeInMillis = (int) ((getCalendarTimeInMillis(calendar2) - getCalendarTimeInMillis(calendar)) / CalendarAstronomer.DAY_MS);
        if (calendarTimeInMillis < 0) {
            z = false;
        }
        return new DurationImpl(z, (BigInteger) null, (BigInteger) null, wrap(Math.abs(calendarTimeInMillis)), (BigInteger) getField(DatatypeConstants.HOURS), (BigInteger) getField(DatatypeConstants.MINUTES), (BigDecimal) getField(DatatypeConstants.SECONDS));
    }

    public Duration multiply(int i) {
        return multiply(BigDecimal.valueOf((long) i));
    }

    public Duration multiply(BigDecimal bigDecimal) {
        BigDecimal bigDecimal2 = ZERO;
        int signum2 = bigDecimal.signum();
        BigDecimal abs = bigDecimal.abs();
        BigDecimal[] bigDecimalArr = new BigDecimal[6];
        boolean z = false;
        BigDecimal bigDecimal3 = bigDecimal2;
        for (int i = 0; i < 5; i++) {
            BigDecimal add = getFieldAsBigDecimal(FIELDS[i]).multiply(abs).add(bigDecimal3);
            bigDecimalArr[i] = add.setScale(0, 1);
            BigDecimal subtract = add.subtract(bigDecimalArr[i]);
            if (i != 1) {
                bigDecimal3 = subtract.multiply(FACTORS[i]);
            } else if (subtract.signum() == 0) {
                bigDecimal3 = ZERO;
            } else {
                throw new IllegalStateException();
            }
        }
        BigDecimal bigDecimal4 = this.seconds;
        if (bigDecimal4 != null) {
            bigDecimalArr[5] = bigDecimal4.multiply(abs).add(bigDecimal3);
        } else {
            bigDecimalArr[5] = bigDecimal3;
        }
        boolean z2 = this.signum * signum2 >= 0;
        BigInteger bigInteger = toBigInteger(bigDecimalArr[0], this.years == null);
        BigInteger bigInteger2 = toBigInteger(bigDecimalArr[1], this.months == null);
        BigInteger bigInteger3 = toBigInteger(bigDecimalArr[2], this.days == null);
        BigInteger bigInteger4 = toBigInteger(bigDecimalArr[3], this.hours == null);
        BigDecimal bigDecimal5 = bigDecimalArr[4];
        if (this.minutes == null) {
            z = true;
        }
        return new DurationImpl(z2, bigInteger, bigInteger2, bigInteger3, bigInteger4, toBigInteger(bigDecimal5, z), (bigDecimalArr[5].signum() == 0 && this.seconds == null) ? null : bigDecimalArr[5]);
    }

    private BigDecimal getFieldAsBigDecimal(DatatypeConstants.Field field) {
        if (field == DatatypeConstants.SECONDS) {
            BigDecimal bigDecimal = this.seconds;
            if (bigDecimal != null) {
                return bigDecimal;
            }
            return ZERO;
        }
        BigInteger bigInteger = (BigInteger) getField(field);
        if (bigInteger == null) {
            return ZERO;
        }
        return new BigDecimal(bigInteger);
    }

    private static BigInteger toBigInteger(BigDecimal bigDecimal, boolean z) {
        if (!z || bigDecimal.signum() != 0) {
            return bigDecimal.unscaledValue();
        }
        return null;
    }

    public Duration add(Duration duration) {
        boolean z = false;
        BigDecimal[] bigDecimalArr = {sanitize((BigInteger) getField(DatatypeConstants.YEARS), getSign()).add(sanitize((BigInteger) duration.getField(DatatypeConstants.YEARS), duration.getSign())), sanitize((BigInteger) getField(DatatypeConstants.MONTHS), getSign()).add(sanitize((BigInteger) duration.getField(DatatypeConstants.MONTHS), duration.getSign())), sanitize((BigInteger) getField(DatatypeConstants.DAYS), getSign()).add(sanitize((BigInteger) duration.getField(DatatypeConstants.DAYS), duration.getSign())), sanitize((BigInteger) getField(DatatypeConstants.HOURS), getSign()).add(sanitize((BigInteger) duration.getField(DatatypeConstants.HOURS), duration.getSign())), sanitize((BigInteger) getField(DatatypeConstants.MINUTES), getSign()).add(sanitize((BigInteger) duration.getField(DatatypeConstants.MINUTES), duration.getSign())), sanitize((BigDecimal) getField(DatatypeConstants.SECONDS), getSign()).add(sanitize((BigDecimal) duration.getField(DatatypeConstants.SECONDS), duration.getSign()))};
        alignSigns(bigDecimalArr, 0, 2);
        alignSigns(bigDecimalArr, 2, 6);
        int i = 0;
        for (int i2 = 0; i2 < 6; i2++) {
            if (bigDecimalArr[i2].signum() * i >= 0) {
                if (i == 0) {
                    i = bigDecimalArr[i2].signum();
                }
            } else {
                throw new IllegalStateException();
            }
        }
        boolean z2 = i >= 0;
        BigInteger bigInteger = toBigInteger(sanitize(bigDecimalArr[0], i), getField(DatatypeConstants.YEARS) == null && duration.getField(DatatypeConstants.YEARS) == null);
        BigInteger bigInteger2 = toBigInteger(sanitize(bigDecimalArr[1], i), getField(DatatypeConstants.MONTHS) == null && duration.getField(DatatypeConstants.MONTHS) == null);
        BigInteger bigInteger3 = toBigInteger(sanitize(bigDecimalArr[2], i), getField(DatatypeConstants.DAYS) == null && duration.getField(DatatypeConstants.DAYS) == null);
        BigInteger bigInteger4 = toBigInteger(sanitize(bigDecimalArr[3], i), getField(DatatypeConstants.HOURS) == null && duration.getField(DatatypeConstants.HOURS) == null);
        BigDecimal sanitize = sanitize(bigDecimalArr[4], i);
        if (getField(DatatypeConstants.MINUTES) == null && duration.getField(DatatypeConstants.MINUTES) == null) {
            z = true;
        }
        return new DurationImpl(z2, bigInteger, bigInteger2, bigInteger3, bigInteger4, toBigInteger(sanitize, z), (bigDecimalArr[5].signum() == 0 && getField(DatatypeConstants.SECONDS) == null && duration.getField(DatatypeConstants.SECONDS) == null) ? null : sanitize(bigDecimalArr[5], i));
    }

    private static void alignSigns(BigDecimal[] bigDecimalArr, int i, int i2) {
        boolean z;
        do {
            z = false;
            int i3 = 0;
            for (int i4 = i; i4 < i2; i4++) {
                if (bigDecimalArr[i4].signum() * i3 < 0) {
                    int i5 = i4 - 1;
                    BigDecimal divide = bigDecimalArr[i4].abs().divide(FACTORS[i5], 0);
                    if (bigDecimalArr[i4].signum() > 0) {
                        divide = divide.negate();
                    }
                    bigDecimalArr[i5] = bigDecimalArr[i5].subtract(divide);
                    bigDecimalArr[i4] = bigDecimalArr[i4].add(divide.multiply(FACTORS[i5]));
                    z = true;
                }
                if (bigDecimalArr[i4].signum() != 0) {
                    i3 = bigDecimalArr[i4].signum();
                }
            }
        } while (z);
    }

    private static BigDecimal sanitize(BigInteger bigInteger, int i) {
        if (i == 0 || bigInteger == null) {
            return ZERO;
        }
        if (i > 0) {
            return new BigDecimal(bigInteger);
        }
        return new BigDecimal(bigInteger.negate());
    }

    static BigDecimal sanitize(BigDecimal bigDecimal, int i) {
        if (i == 0 || bigDecimal == null) {
            return ZERO;
        }
        return i > 0 ? bigDecimal : bigDecimal.negate();
    }

    public Duration subtract(Duration duration) {
        return add(duration.negate());
    }

    public Duration negate() {
        return new DurationImpl(this.signum <= 0, this.years, this.months, this.days, this.hours, this.minutes, this.seconds);
    }

    public int signum() {
        return this.signum;
    }

    public void addTo(Calendar calendar) {
        calendar.add(1, getYears() * this.signum);
        calendar.add(2, getMonths() * this.signum);
        calendar.add(5, getDays() * this.signum);
        calendar.add(10, getHours() * this.signum);
        calendar.add(12, getMinutes() * this.signum);
        calendar.add(13, getSeconds() * this.signum);
        BigDecimal bigDecimal = this.seconds;
        if (bigDecimal != null) {
            calendar.add(14, bigDecimal.subtract(bigDecimal.setScale(0, 1)).movePointRight(3).intValue() * this.signum);
        }
    }

    public void addTo(Date date) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        addTo(gregorianCalendar);
        date.setTime(getCalendarTimeInMillis(gregorianCalendar));
    }

    private Object writeReplace() throws IOException {
        return new DurationStream(toString());
    }

    private static class DurationStream implements Serializable {
        private static final long serialVersionUID = 1;
        private final String lexical;

        private DurationStream(String str) {
            this.lexical = str;
        }

        private Object readResolve() throws ObjectStreamException {
            return new DurationImpl(this.lexical);
        }
    }

    private static long getCalendarTimeInMillis(Calendar calendar) {
        return calendar.getTime().getTime();
    }
}

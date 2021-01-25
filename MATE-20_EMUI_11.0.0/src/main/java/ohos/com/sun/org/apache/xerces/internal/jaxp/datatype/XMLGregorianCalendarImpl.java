package ohos.com.sun.org.apache.xerces.internal.jaxp.datatype;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import ohos.com.sun.org.apache.xerces.internal.util.DatatypeMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.javax.xml.datatype.DatatypeConstants;
import ohos.javax.xml.datatype.Duration;
import ohos.javax.xml.datatype.XMLGregorianCalendar;
import ohos.javax.xml.namespace.QName;

public class XMLGregorianCalendarImpl extends XMLGregorianCalendar implements Serializable, Cloneable {
    private static final BigInteger BILLION = new BigInteger("1000000000");
    private static final int DAY = 2;
    private static final BigDecimal DECIMAL_ONE = new BigDecimal("1");
    private static final BigDecimal DECIMAL_SIXTY = new BigDecimal("60");
    private static final BigDecimal DECIMAL_ZERO = new BigDecimal("0");
    private static final String[] FIELD_NAME = {"Year", "Month", "Day", "Hour", "Minute", "Second", "Millisecond", "Timezone"};
    private static final BigInteger FOUR = BigInteger.valueOf(4);
    private static final BigInteger FOUR_HUNDRED = BigInteger.valueOf(400);
    private static final int HOUR = 3;
    private static final BigInteger HUNDRED = BigInteger.valueOf(100);
    public static final XMLGregorianCalendar LEAP_YEAR_DEFAULT = createDateTime(400, 1, 1, 0, 0, 0, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private static final int MILLISECOND = 6;
    private static final int MINUTE = 4;
    private static final int MONTH = 1;
    private static final Date PURE_GREGORIAN_CHANGE = new Date(Long.MIN_VALUE);
    private static final int SECOND = 5;
    private static final BigInteger SIXTY = BigInteger.valueOf(60);
    private static final int TIMEZONE = 7;
    private static final BigInteger TWELVE = BigInteger.valueOf(12);
    private static final BigInteger TWENTY_FOUR = BigInteger.valueOf(24);
    private static final int YEAR = 0;
    private static int[] daysInMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final long serialVersionUID = 1;
    private int day = Integer.MIN_VALUE;
    private BigInteger eon = null;
    private BigDecimal fractionalSecond = null;
    private int hour = Integer.MIN_VALUE;
    private int minute = Integer.MIN_VALUE;
    private int month = Integer.MIN_VALUE;
    private int second = Integer.MIN_VALUE;
    private int timezone = Integer.MIN_VALUE;
    private int year = Integer.MIN_VALUE;

    private static int compareField(int i, int i2) {
        if (i == i2) {
            return 0;
        }
        if (i == Integer.MIN_VALUE || i2 == Integer.MIN_VALUE) {
            return 2;
        }
        return i < i2 ? -1 : 1;
    }

    /* access modifiers changed from: private */
    public static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    public void reset() {
    }

    protected XMLGregorianCalendarImpl(String str) throws IllegalArgumentException {
        String str2;
        int length = str.length();
        if (str.indexOf(84) != -1) {
            str2 = "%Y-%M-%DT%h:%m:%s%z";
        } else if (length >= 3 && str.charAt(2) == ':') {
            str2 = "%h:%m:%s%z";
        } else if (str.startsWith("--")) {
            str2 = (length < 3 || str.charAt(2) != '-') ? (length == 4 || length == 5 || length == 10) ? "--%M%z" : "--%M-%D%z" : "---%D%z";
        } else {
            length = str.indexOf(58) != -1 ? length - 6 : length;
            int i = 0;
            for (int i2 = 1; i2 < length; i2++) {
                if (str.charAt(i2) == '-') {
                    i++;
                }
            }
            str2 = i == 0 ? "%Y%z" : i == 1 ? "%Y-%M%z" : "%Y-%M-%D%z";
        }
        new Parser(str2, str).parse();
        if (!isValid()) {
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "InvalidXGCRepresentation", new Object[]{str}));
        }
    }

    public XMLGregorianCalendarImpl() {
    }

    protected XMLGregorianCalendarImpl(BigInteger bigInteger, int i, int i2, int i3, int i4, int i5, BigDecimal bigDecimal, int i6) {
        setYear(bigInteger);
        setMonth(i);
        setDay(i2);
        setTime(i3, i4, i5, bigDecimal);
        setTimezone(i6);
        if (!isValid()) {
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "InvalidXGCValue-fractional", new Object[]{bigInteger, new Integer(i), new Integer(i2), new Integer(i3), new Integer(i4), new Integer(i5), bigDecimal, new Integer(i6)}));
        }
    }

    private XMLGregorianCalendarImpl(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        setYear(i);
        setMonth(i2);
        setDay(i3);
        setTime(i4, i5, i6);
        setTimezone(i8);
        setMillisecond(i7);
        if (!isValid()) {
            throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "InvalidXGCValue-milli", new Object[]{new Integer(i), new Integer(i2), new Integer(i3), new Integer(i4), new Integer(i5), new Integer(i6), new Integer(i7), new Integer(i8)}));
        }
    }

    public XMLGregorianCalendarImpl(GregorianCalendar gregorianCalendar) {
        int i = gregorianCalendar.get(1);
        setYear(gregorianCalendar.get(0) == 0 ? -i : i);
        setMonth(gregorianCalendar.get(2) + 1);
        setDay(gregorianCalendar.get(5));
        setTime(gregorianCalendar.get(11), gregorianCalendar.get(12), gregorianCalendar.get(13), gregorianCalendar.get(14));
        setTimezone((gregorianCalendar.get(15) + gregorianCalendar.get(16)) / 60000);
    }

    public static XMLGregorianCalendar createDateTime(BigInteger bigInteger, int i, int i2, int i3, int i4, int i5, BigDecimal bigDecimal, int i6) {
        return new XMLGregorianCalendarImpl(bigInteger, i, i2, i3, i4, i5, bigDecimal, i6);
    }

    public static XMLGregorianCalendar createDateTime(int i, int i2, int i3, int i4, int i5, int i6) {
        return new XMLGregorianCalendarImpl(i, i2, i3, i4, i5, i6, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public static XMLGregorianCalendar createDateTime(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        return new XMLGregorianCalendarImpl(i, i2, i3, i4, i5, i6, i7, i8);
    }

    public static XMLGregorianCalendar createDate(int i, int i2, int i3, int i4) {
        return new XMLGregorianCalendarImpl(i, i2, i3, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, i4);
    }

    public static XMLGregorianCalendar createTime(int i, int i2, int i3, int i4) {
        return new XMLGregorianCalendarImpl(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, i, i2, i3, Integer.MIN_VALUE, i4);
    }

    public static XMLGregorianCalendar createTime(int i, int i2, int i3, BigDecimal bigDecimal, int i4) {
        return new XMLGregorianCalendarImpl((BigInteger) null, Integer.MIN_VALUE, Integer.MIN_VALUE, i, i2, i3, bigDecimal, i4);
    }

    public static XMLGregorianCalendar createTime(int i, int i2, int i3, int i4, int i5) {
        return new XMLGregorianCalendarImpl(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, i, i2, i3, i4, i5);
    }

    public BigInteger getEon() {
        return this.eon;
    }

    public int getYear() {
        return this.year;
    }

    public BigInteger getEonAndYear() {
        BigInteger bigInteger;
        int i = this.year;
        if (i != Integer.MIN_VALUE && (bigInteger = this.eon) != null) {
            return bigInteger.add(BigInteger.valueOf((long) i));
        }
        int i2 = this.year;
        if (i2 == Integer.MIN_VALUE || this.eon != null) {
            return null;
        }
        return BigInteger.valueOf((long) i2);
    }

    public int getMonth() {
        return this.month;
    }

    public int getDay() {
        return this.day;
    }

    public int getTimezone() {
        return this.timezone;
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getSecond() {
        return this.second;
    }

    private BigDecimal getSeconds() {
        int i = this.second;
        if (i == Integer.MIN_VALUE) {
            return DECIMAL_ZERO;
        }
        BigDecimal valueOf = BigDecimal.valueOf((long) i);
        BigDecimal bigDecimal = this.fractionalSecond;
        return bigDecimal != null ? valueOf.add(bigDecimal) : valueOf;
    }

    public int getMillisecond() {
        BigDecimal bigDecimal = this.fractionalSecond;
        if (bigDecimal == null) {
            return Integer.MIN_VALUE;
        }
        return bigDecimal.movePointRight(3).intValue();
    }

    public BigDecimal getFractionalSecond() {
        return this.fractionalSecond;
    }

    public void setYear(BigInteger bigInteger) {
        if (bigInteger == null) {
            this.eon = null;
            this.year = Integer.MIN_VALUE;
            return;
        }
        BigInteger remainder = bigInteger.remainder(BILLION);
        this.year = remainder.intValue();
        setEon(bigInteger.subtract(remainder));
    }

    public void setYear(int i) {
        if (i == Integer.MIN_VALUE) {
            this.year = Integer.MIN_VALUE;
            this.eon = null;
        } else if (Math.abs(i) < BILLION.intValue()) {
            this.year = i;
            this.eon = null;
        } else {
            BigInteger valueOf = BigInteger.valueOf((long) i);
            BigInteger remainder = valueOf.remainder(BILLION);
            this.year = remainder.intValue();
            setEon(valueOf.subtract(remainder));
        }
    }

    private void setEon(BigInteger bigInteger) {
        if (bigInteger == null || bigInteger.compareTo(BigInteger.ZERO) != 0) {
            this.eon = bigInteger;
        } else {
            this.eon = null;
        }
    }

    public void setMonth(int i) {
        if ((i < 1 || 12 < i) && i != Integer.MIN_VALUE) {
            invalidFieldValue(1, i);
        }
        this.month = i;
    }

    public void setDay(int i) {
        if ((i < 1 || 31 < i) && i != Integer.MIN_VALUE) {
            invalidFieldValue(2, i);
        }
        this.day = i;
    }

    public void setTimezone(int i) {
        if ((i < -840 || 840 < i) && i != Integer.MIN_VALUE) {
            invalidFieldValue(7, i);
        }
        this.timezone = i;
    }

    public void setTime(int i, int i2, int i3) {
        setTime(i, i2, i3, (BigDecimal) null);
    }

    private void invalidFieldValue(int i, int i2) {
        throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "InvalidFieldValue", new Object[]{new Integer(i2), FIELD_NAME[i]}));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void testHour() {
        if (getHour() == 24) {
            if (!(getMinute() == 0 && getSecond() == 0)) {
                invalidFieldValue(3, getHour());
            }
            setHour(0, false);
            add(new DurationImpl(true, 0, 0, 1, 0, 0, 0));
        }
    }

    public void setHour(int i) {
        setHour(i, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setHour(int i, boolean z) {
        if ((i < 0 || i > 24) && i != Integer.MIN_VALUE) {
            invalidFieldValue(3, i);
        }
        this.hour = i;
        if (z) {
            testHour();
        }
    }

    public void setMinute(int i) {
        if ((i < 0 || 59 < i) && i != Integer.MIN_VALUE) {
            invalidFieldValue(4, i);
        }
        this.minute = i;
    }

    public void setSecond(int i) {
        if ((i < 0 || 60 < i) && i != Integer.MIN_VALUE) {
            invalidFieldValue(5, i);
        }
        this.second = i;
    }

    public void setTime(int i, int i2, int i3, BigDecimal bigDecimal) {
        setHour(i, false);
        setMinute(i2);
        if (i3 != 60) {
            setSecond(i3);
        } else if ((i == 23 && i2 == 59) || (i == 0 && i2 == 0)) {
            setSecond(i3);
        } else {
            invalidFieldValue(5, i3);
        }
        setFractionalSecond(bigDecimal);
        testHour();
    }

    public void setTime(int i, int i2, int i3, int i4) {
        setHour(i, false);
        setMinute(i2);
        if (i3 != 60) {
            setSecond(i3);
        } else if ((i == 23 && i2 == 59) || (i == 0 && i2 == 0)) {
            setSecond(i3);
        } else {
            invalidFieldValue(5, i3);
        }
        setMillisecond(i4);
        testHour();
    }

    public int compare(XMLGregorianCalendar xMLGregorianCalendar) {
        XMLGregorianCalendarImpl xMLGregorianCalendarImpl = this;
        XMLGregorianCalendarImpl xMLGregorianCalendarImpl2 = (XMLGregorianCalendarImpl) xMLGregorianCalendar;
        if (xMLGregorianCalendarImpl.getTimezone() == xMLGregorianCalendarImpl2.getTimezone()) {
            return internalCompare(xMLGregorianCalendarImpl, xMLGregorianCalendarImpl2);
        }
        if (xMLGregorianCalendarImpl.getTimezone() != Integer.MIN_VALUE && xMLGregorianCalendarImpl2.getTimezone() != Integer.MIN_VALUE) {
            return internalCompare(xMLGregorianCalendarImpl.normalize(), xMLGregorianCalendarImpl2.normalize());
        }
        if (xMLGregorianCalendarImpl.getTimezone() != Integer.MIN_VALUE) {
            if (xMLGregorianCalendarImpl.getTimezone() != 0) {
                xMLGregorianCalendarImpl = xMLGregorianCalendarImpl.normalize();
            }
            int internalCompare = internalCompare(xMLGregorianCalendarImpl, xMLGregorianCalendarImpl2.normalizeToTimezone(840));
            if (internalCompare == -1) {
                return internalCompare;
            }
            int internalCompare2 = internalCompare(xMLGregorianCalendarImpl, xMLGregorianCalendarImpl2.normalizeToTimezone(-840));
            if (internalCompare2 == 1) {
                return internalCompare2;
            }
            return 2;
        }
        if (xMLGregorianCalendarImpl2.getTimezone() != 0) {
            xMLGregorianCalendarImpl2 = xMLGregorianCalendarImpl2.normalizeToTimezone(xMLGregorianCalendarImpl2.getTimezone());
        }
        int internalCompare3 = internalCompare(xMLGregorianCalendarImpl.normalizeToTimezone(-840), xMLGregorianCalendarImpl2);
        if (internalCompare3 == -1) {
            return internalCompare3;
        }
        int internalCompare4 = internalCompare(xMLGregorianCalendarImpl.normalizeToTimezone(840), xMLGregorianCalendarImpl2);
        if (internalCompare4 == 1) {
            return internalCompare4;
        }
        return 2;
    }

    public XMLGregorianCalendar normalize() {
        XMLGregorianCalendar normalizeToTimezone = normalizeToTimezone(this.timezone);
        if (getTimezone() == Integer.MIN_VALUE) {
            normalizeToTimezone.setTimezone(Integer.MIN_VALUE);
        }
        if (getMillisecond() == Integer.MIN_VALUE) {
            normalizeToTimezone.setMillisecond(Integer.MIN_VALUE);
        }
        return normalizeToTimezone;
    }

    private XMLGregorianCalendar normalizeToTimezone(int i) {
        XMLGregorianCalendar xMLGregorianCalendar = (XMLGregorianCalendar) clone();
        int i2 = -i;
        boolean z = i2 >= 0;
        if (i2 < 0) {
            i2 = -i2;
        }
        xMLGregorianCalendar.add(new DurationImpl(z, 0, 0, 0, 0, i2, 0));
        xMLGregorianCalendar.setTimezone(0);
        return xMLGregorianCalendar;
    }

    private static int internalCompare(XMLGregorianCalendar xMLGregorianCalendar, XMLGregorianCalendar xMLGregorianCalendar2) {
        if (xMLGregorianCalendar.getEon() == xMLGregorianCalendar2.getEon()) {
            int compareField = compareField(xMLGregorianCalendar.getYear(), xMLGregorianCalendar2.getYear());
            if (compareField != 0) {
                return compareField;
            }
        } else {
            int compareField2 = compareField(xMLGregorianCalendar.getEonAndYear(), xMLGregorianCalendar2.getEonAndYear());
            if (compareField2 != 0) {
                return compareField2;
            }
        }
        int compareField3 = compareField(xMLGregorianCalendar.getMonth(), xMLGregorianCalendar2.getMonth());
        if (compareField3 != 0) {
            return compareField3;
        }
        int compareField4 = compareField(xMLGregorianCalendar.getDay(), xMLGregorianCalendar2.getDay());
        if (compareField4 != 0) {
            return compareField4;
        }
        int compareField5 = compareField(xMLGregorianCalendar.getHour(), xMLGregorianCalendar2.getHour());
        if (compareField5 != 0) {
            return compareField5;
        }
        int compareField6 = compareField(xMLGregorianCalendar.getMinute(), xMLGregorianCalendar2.getMinute());
        if (compareField6 != 0) {
            return compareField6;
        }
        int compareField7 = compareField(xMLGregorianCalendar.getSecond(), xMLGregorianCalendar2.getSecond());
        if (compareField7 != 0) {
            return compareField7;
        }
        return compareField(xMLGregorianCalendar.getFractionalSecond(), xMLGregorianCalendar2.getFractionalSecond());
    }

    private static int compareField(BigInteger bigInteger, BigInteger bigInteger2) {
        if (bigInteger == null) {
            return bigInteger2 == null ? 0 : 2;
        }
        if (bigInteger2 == null) {
            return 2;
        }
        return bigInteger.compareTo(bigInteger2);
    }

    private static int compareField(BigDecimal bigDecimal, BigDecimal bigDecimal2) {
        if (bigDecimal == bigDecimal2) {
            return 0;
        }
        if (bigDecimal == null) {
            bigDecimal = DECIMAL_ZERO;
        }
        if (bigDecimal2 == null) {
            bigDecimal2 = DECIMAL_ZERO;
        }
        return bigDecimal.compareTo(bigDecimal2);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof XMLGregorianCalendar) || compare((XMLGregorianCalendar) obj) != 0) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        int timezone2 = getTimezone();
        if (timezone2 == Integer.MIN_VALUE) {
            timezone2 = 0;
        }
        if (timezone2 != 0) {
            this = normalizeToTimezone(getTimezone());
        }
        return this.getYear() + this.getMonth() + this.getDay() + this.getHour() + this.getMinute() + this.getSecond();
    }

    public static XMLGregorianCalendar parse(String str) {
        return new XMLGregorianCalendarImpl(str);
    }

    public String toXMLFormat() {
        String str;
        QName xMLSchemaType = getXMLSchemaType();
        if (xMLSchemaType == DatatypeConstants.DATETIME) {
            str = "%Y-%M-%DT%h:%m:%s%z";
        } else if (xMLSchemaType == DatatypeConstants.DATE) {
            str = "%Y-%M-%D%z";
        } else if (xMLSchemaType == DatatypeConstants.TIME) {
            str = "%h:%m:%s%z";
        } else if (xMLSchemaType == DatatypeConstants.GMONTH) {
            str = "--%M%z";
        } else if (xMLSchemaType == DatatypeConstants.GDAY) {
            str = "---%D%z";
        } else if (xMLSchemaType == DatatypeConstants.GYEAR) {
            str = "%Y%z";
        } else if (xMLSchemaType == DatatypeConstants.GYEARMONTH) {
            str = "%Y-%M%z";
        } else {
            str = xMLSchemaType == DatatypeConstants.GMONTHDAY ? "--%M-%D%z" : null;
        }
        return format(str);
    }

    public QName getXMLSchemaType() {
        char c = 0;
        int i = (this.year != Integer.MIN_VALUE ? ' ' : 0) | (this.month != Integer.MIN_VALUE ? (char) 16 : 0) | (this.day != Integer.MIN_VALUE ? 8 : 0) | (this.hour != Integer.MIN_VALUE ? 4 : 0) | (this.minute != Integer.MIN_VALUE ? 2 : 0);
        if (this.second != Integer.MIN_VALUE) {
            c = 1;
        }
        int i2 = i | c;
        if (i2 == 7) {
            return DatatypeConstants.TIME;
        }
        if (i2 == 8) {
            return DatatypeConstants.GDAY;
        }
        if (i2 == 16) {
            return DatatypeConstants.GMONTH;
        }
        if (i2 == 24) {
            return DatatypeConstants.GMONTHDAY;
        }
        if (i2 == 32) {
            return DatatypeConstants.GYEAR;
        }
        if (i2 == 48) {
            return DatatypeConstants.GYEARMONTH;
        }
        if (i2 == 56) {
            return DatatypeConstants.DATE;
        }
        if (i2 == 63) {
            return DatatypeConstants.DATETIME;
        }
        throw new IllegalStateException(getClass().getName() + "#getXMLSchemaType() :" + DatatypeMessageFormatter.formatMessage(null, "InvalidXGCFields", null));
    }

    public boolean isValid() {
        if (getMonth() == 2) {
            int i = 29;
            if (this.eon == null) {
                int i2 = this.year;
                if (i2 != Integer.MIN_VALUE) {
                    i = maximumDayInMonthFor(i2, getMonth());
                }
            } else if (getEonAndYear() != null) {
                i = maximumDayInMonthFor(getEonAndYear(), 2);
            }
            if (getDay() > i) {
                return false;
            }
        }
        if (getHour() == 24 && (getMinute() != 0 || getSecond() != 0)) {
            return false;
        }
        if (this.eon != null) {
            BigInteger eonAndYear = getEonAndYear();
            if (eonAndYear == null || compareField(eonAndYear, BigInteger.ZERO) != 0) {
                return true;
            }
            return false;
        } else if (this.year == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void add(Duration duration) {
        BigDecimal bigDecimal;
        BigInteger bigInteger;
        int i;
        int i2;
        int i3;
        BigInteger bigInteger2;
        boolean[] zArr = {false, false, false, false, false, false};
        int sign = duration.getSign();
        int month2 = getMonth();
        if (month2 == Integer.MIN_VALUE) {
            zArr[1] = true;
            month2 = 1;
        }
        BigInteger add = BigInteger.valueOf((long) month2).add(sanitize(duration.getField(DatatypeConstants.MONTHS), sign));
        setMonth(add.subtract(BigInteger.ONE).mod(TWELVE).intValue() + 1);
        BigInteger bigInteger3 = new BigDecimal(add.subtract(BigInteger.ONE)).divide(new BigDecimal(TWELVE), 3).toBigInteger();
        BigInteger eonAndYear = getEonAndYear();
        if (eonAndYear == null) {
            zArr[0] = true;
            eonAndYear = BigInteger.ZERO;
        }
        setYear(eonAndYear.add(sanitize(duration.getField(DatatypeConstants.YEARS), sign)).add(bigInteger3));
        if (getSecond() == Integer.MIN_VALUE) {
            zArr[5] = true;
            bigDecimal = DECIMAL_ZERO;
        } else {
            bigDecimal = getSeconds();
        }
        BigDecimal add2 = bigDecimal.add(DurationImpl.sanitize((BigDecimal) duration.getField(DatatypeConstants.SECONDS), sign));
        BigDecimal bigDecimal2 = new BigDecimal(new BigDecimal(add2.toBigInteger()).divide(DECIMAL_SIXTY, 3).toBigInteger());
        BigDecimal subtract = add2.subtract(bigDecimal2.multiply(DECIMAL_SIXTY));
        BigInteger bigInteger4 = bigDecimal2.toBigInteger();
        setSecond(subtract.intValue());
        BigDecimal subtract2 = subtract.subtract(new BigDecimal(BigInteger.valueOf((long) getSecond())));
        if (subtract2.compareTo(DECIMAL_ZERO) < 0) {
            setFractionalSecond(DECIMAL_ONE.add(subtract2));
            if (getSecond() == 0) {
                setSecond(59);
                bigInteger4 = bigInteger4.subtract(BigInteger.ONE);
            } else {
                setSecond(getSecond() - 1);
            }
        } else {
            setFractionalSecond(subtract2);
        }
        int minute2 = getMinute();
        if (minute2 == Integer.MIN_VALUE) {
            zArr[4] = true;
            minute2 = 0;
        }
        BigInteger add3 = BigInteger.valueOf((long) minute2).add(sanitize(duration.getField(DatatypeConstants.MINUTES), sign)).add(bigInteger4);
        setMinute(add3.mod(SIXTY).intValue());
        BigInteger bigInteger5 = new BigDecimal(add3).divide(DECIMAL_SIXTY, 3).toBigInteger();
        int hour2 = getHour();
        if (hour2 == Integer.MIN_VALUE) {
            zArr[3] = true;
            hour2 = 0;
        }
        BigInteger add4 = BigInteger.valueOf((long) hour2).add(sanitize(duration.getField(DatatypeConstants.HOURS), sign)).add(bigInteger5);
        setHour(add4.mod(TWENTY_FOUR).intValue(), false);
        BigInteger bigInteger6 = new BigDecimal(add4).divide(new BigDecimal(TWENTY_FOUR), 3).toBigInteger();
        int day2 = getDay();
        if (day2 == Integer.MIN_VALUE) {
            zArr[2] = true;
            day2 = 1;
        }
        BigInteger sanitize = sanitize(duration.getField(DatatypeConstants.DAYS), sign);
        int maximumDayInMonthFor = maximumDayInMonthFor(getEonAndYear(), getMonth());
        if (day2 > maximumDayInMonthFor) {
            bigInteger = BigInteger.valueOf((long) maximumDayInMonthFor);
        } else if (day2 < 1) {
            bigInteger = BigInteger.ONE;
        } else {
            bigInteger = BigInteger.valueOf((long) day2);
        }
        BigInteger add5 = bigInteger.add(sanitize).add(bigInteger6);
        while (true) {
            if (add5.compareTo(BigInteger.ONE) >= 0) {
                if (add5.compareTo(BigInteger.valueOf((long) maximumDayInMonthFor(getEonAndYear(), getMonth()))) <= 0) {
                    break;
                }
                add5 = add5.add(BigInteger.valueOf((long) (-maximumDayInMonthFor(getEonAndYear(), getMonth()))));
                i = 1;
            } else {
                if (this.month >= 2) {
                    bigInteger2 = BigInteger.valueOf((long) maximumDayInMonthFor(getEonAndYear(), getMonth() - 1));
                } else {
                    bigInteger2 = BigInteger.valueOf((long) maximumDayInMonthFor(getEonAndYear().subtract(BigInteger.valueOf(serialVersionUID)), 12));
                }
                add5 = add5.add(bigInteger2);
                i = -1;
            }
            int month3 = (getMonth() + i) - 1;
            int i4 = month3 % 12;
            if (i4 < 0) {
                i3 = i4 + 12 + 1;
                i2 = new BigDecimal(month3).divide(new BigDecimal(TWELVE), 0).intValue();
            } else {
                i2 = month3 / 12;
                i3 = i4 + 1;
            }
            setMonth(i3);
            if (i2 != 0) {
                setYear(getEonAndYear().add(BigInteger.valueOf((long) i2)));
            }
        }
        setDay(add5.intValue());
        for (int i5 = 0; i5 <= 5; i5++) {
            if (zArr[i5]) {
                if (i5 == 0) {
                    setYear(Integer.MIN_VALUE);
                } else if (i5 == 1) {
                    setMonth(Integer.MIN_VALUE);
                } else if (i5 == 2) {
                    setDay(Integer.MIN_VALUE);
                } else if (i5 == 3) {
                    setHour(Integer.MIN_VALUE, false);
                } else if (i5 == 4) {
                    setMinute(Integer.MIN_VALUE);
                } else if (i5 == 5) {
                    setSecond(Integer.MIN_VALUE);
                    setFractionalSecond(null);
                }
            }
        }
    }

    private static int maximumDayInMonthFor(BigInteger bigInteger, int i) {
        if (i != 2) {
            return daysInMonth[i];
        }
        if (bigInteger.mod(FOUR_HUNDRED).equals(BigInteger.ZERO)) {
            return 29;
        }
        if (bigInteger.mod(HUNDRED).equals(BigInteger.ZERO) || !bigInteger.mod(FOUR).equals(BigInteger.ZERO)) {
            return daysInMonth[i];
        }
        return 29;
    }

    private static int maximumDayInMonthFor(int i, int i2) {
        if (i2 != 2) {
            return daysInMonth[i2];
        }
        if (i % 400 == 0) {
            return 29;
        }
        if (i % 100 == 0 || i % 4 != 0) {
            return daysInMonth[2];
        }
        return 29;
    }

    public GregorianCalendar toGregorianCalendar() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(getTimeZone(Integer.MIN_VALUE), getDefaultLocale());
        gregorianCalendar.clear();
        gregorianCalendar.setGregorianChange(PURE_GREGORIAN_CHANGE);
        BigInteger eonAndYear = getEonAndYear();
        if (eonAndYear != null) {
            gregorianCalendar.set(0, eonAndYear.signum() == -1 ? 0 : 1);
            gregorianCalendar.set(1, eonAndYear.abs().intValue());
        }
        int i = this.month;
        if (i != Integer.MIN_VALUE) {
            gregorianCalendar.set(2, i - 1);
        }
        int i2 = this.day;
        if (i2 != Integer.MIN_VALUE) {
            gregorianCalendar.set(5, i2);
        }
        int i3 = this.hour;
        if (i3 != Integer.MIN_VALUE) {
            gregorianCalendar.set(11, i3);
        }
        int i4 = this.minute;
        if (i4 != Integer.MIN_VALUE) {
            gregorianCalendar.set(12, i4);
        }
        int i5 = this.second;
        if (i5 != Integer.MIN_VALUE) {
            gregorianCalendar.set(13, i5);
        }
        if (this.fractionalSecond != null) {
            gregorianCalendar.set(14, getMillisecond());
        }
        return gregorianCalendar;
    }

    private Locale getDefaultLocale() {
        Locale locale;
        String systemProperty = SecuritySupport.getSystemProperty("user.language.format");
        String systemProperty2 = SecuritySupport.getSystemProperty("user.country.format");
        String systemProperty3 = SecuritySupport.getSystemProperty("user.variant.format");
        if (systemProperty != null) {
            locale = systemProperty2 != null ? systemProperty3 != null ? new Locale(systemProperty, systemProperty2, systemProperty3) : new Locale(systemProperty, systemProperty2) : new Locale(systemProperty);
        } else {
            locale = null;
        }
        return locale == null ? Locale.getDefault() : locale;
    }

    public GregorianCalendar toGregorianCalendar(TimeZone timeZone, Locale locale, XMLGregorianCalendar xMLGregorianCalendar) {
        if (timeZone == null) {
            timeZone = getTimeZone(xMLGregorianCalendar != null ? xMLGregorianCalendar.getTimezone() : Integer.MIN_VALUE);
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        GregorianCalendar gregorianCalendar = new GregorianCalendar(timeZone, locale);
        gregorianCalendar.clear();
        gregorianCalendar.setGregorianChange(PURE_GREGORIAN_CHANGE);
        BigInteger eonAndYear = getEonAndYear();
        BigDecimal bigDecimal = null;
        if (eonAndYear != null) {
            gregorianCalendar.set(0, eonAndYear.signum() == -1 ? 0 : 1);
            gregorianCalendar.set(1, eonAndYear.abs().intValue());
        } else {
            BigInteger eonAndYear2 = xMLGregorianCalendar != null ? xMLGregorianCalendar.getEonAndYear() : null;
            if (eonAndYear2 != null) {
                gregorianCalendar.set(0, eonAndYear2.signum() == -1 ? 0 : 1);
                gregorianCalendar.set(1, eonAndYear2.abs().intValue());
            }
        }
        int i = this.month;
        if (i != Integer.MIN_VALUE) {
            gregorianCalendar.set(2, i - 1);
        } else {
            int month2 = xMLGregorianCalendar != null ? xMLGregorianCalendar.getMonth() : Integer.MIN_VALUE;
            if (month2 != Integer.MIN_VALUE) {
                gregorianCalendar.set(2, month2 - 1);
            }
        }
        int i2 = this.day;
        if (i2 != Integer.MIN_VALUE) {
            gregorianCalendar.set(5, i2);
        } else {
            int day2 = xMLGregorianCalendar != null ? xMLGregorianCalendar.getDay() : Integer.MIN_VALUE;
            if (day2 != Integer.MIN_VALUE) {
                gregorianCalendar.set(5, day2);
            }
        }
        int i3 = this.hour;
        if (i3 != Integer.MIN_VALUE) {
            gregorianCalendar.set(11, i3);
        } else {
            int hour2 = xMLGregorianCalendar != null ? xMLGregorianCalendar.getHour() : Integer.MIN_VALUE;
            if (hour2 != Integer.MIN_VALUE) {
                gregorianCalendar.set(11, hour2);
            }
        }
        int i4 = this.minute;
        if (i4 != Integer.MIN_VALUE) {
            gregorianCalendar.set(12, i4);
        } else {
            int minute2 = xMLGregorianCalendar != null ? xMLGregorianCalendar.getMinute() : Integer.MIN_VALUE;
            if (minute2 != Integer.MIN_VALUE) {
                gregorianCalendar.set(12, minute2);
            }
        }
        int i5 = this.second;
        if (i5 != Integer.MIN_VALUE) {
            gregorianCalendar.set(13, i5);
        } else {
            int second2 = xMLGregorianCalendar != null ? xMLGregorianCalendar.getSecond() : Integer.MIN_VALUE;
            if (second2 != Integer.MIN_VALUE) {
                gregorianCalendar.set(13, second2);
            }
        }
        if (this.fractionalSecond != null) {
            gregorianCalendar.set(14, getMillisecond());
        } else {
            if (xMLGregorianCalendar != null) {
                bigDecimal = xMLGregorianCalendar.getFractionalSecond();
            }
            if (bigDecimal != null) {
                gregorianCalendar.set(14, xMLGregorianCalendar.getMillisecond());
            }
        }
        return gregorianCalendar;
    }

    public TimeZone getTimeZone(int i) {
        int timezone2 = getTimezone();
        if (timezone2 == Integer.MIN_VALUE) {
            timezone2 = i;
        }
        if (timezone2 == Integer.MIN_VALUE) {
            return TimeZone.getDefault();
        }
        char c = timezone2 < 0 ? '-' : '+';
        if (c == '-') {
            timezone2 = -timezone2;
        }
        int i2 = timezone2 / 60;
        int i3 = timezone2 - (i2 * 60);
        StringBuffer stringBuffer = new StringBuffer(8);
        stringBuffer.append("GMT");
        stringBuffer.append(c);
        stringBuffer.append(i2);
        if (i3 != 0) {
            if (i3 < 10) {
                stringBuffer.append('0');
            }
            stringBuffer.append(i3);
        }
        return TimeZone.getTimeZone(stringBuffer.toString());
    }

    @Override // java.lang.Object
    public Object clone() {
        return new XMLGregorianCalendarImpl(getEonAndYear(), this.month, this.day, this.hour, this.minute, this.second, this.fractionalSecond, this.timezone);
    }

    public void clear() {
        this.eon = null;
        this.year = Integer.MIN_VALUE;
        this.month = Integer.MIN_VALUE;
        this.day = Integer.MIN_VALUE;
        this.timezone = Integer.MIN_VALUE;
        this.hour = Integer.MIN_VALUE;
        this.minute = Integer.MIN_VALUE;
        this.second = Integer.MIN_VALUE;
        this.fractionalSecond = null;
    }

    public void setMillisecond(int i) {
        if (i == Integer.MIN_VALUE) {
            this.fractionalSecond = null;
            return;
        }
        if ((i < 0 || 999 < i) && i != Integer.MIN_VALUE) {
            invalidFieldValue(6, i);
        }
        this.fractionalSecond = new BigDecimal((long) i).movePointLeft(3);
    }

    public void setFractionalSecond(BigDecimal bigDecimal) {
        if (bigDecimal == null || (bigDecimal.compareTo(DECIMAL_ZERO) >= 0 && bigDecimal.compareTo(DECIMAL_ONE) <= 0)) {
            this.fractionalSecond = bigDecimal;
            return;
        }
        throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "InvalidFractional", new Object[]{bigDecimal.toString()}));
    }

    private final class Parser {
        private int fidx;
        private final int flen;
        private final String format;
        private final String value;
        private int vidx;
        private final int vlen;

        private Parser(String str, String str2) {
            this.format = str;
            this.value = str2;
            this.flen = str.length();
            this.vlen = str2.length();
        }

        public void parse() throws IllegalArgumentException {
            while (true) {
                int i = this.fidx;
                if (i < this.flen) {
                    String str = this.format;
                    this.fidx = i + 1;
                    char charAt = str.charAt(i);
                    if (charAt != '%') {
                        skip(charAt);
                    } else {
                        String str2 = this.format;
                        int i2 = this.fidx;
                        this.fidx = i2 + 1;
                        char charAt2 = str2.charAt(i2);
                        if (charAt2 == 'D') {
                            XMLGregorianCalendarImpl.this.setDay(parseInt(2, 2));
                        } else if (charAt2 == 'M') {
                            XMLGregorianCalendarImpl.this.setMonth(parseInt(2, 2));
                        } else if (charAt2 == 'Y') {
                            parseAndSetYear(4);
                        } else if (charAt2 == 'h') {
                            XMLGregorianCalendarImpl.this.setHour(parseInt(2, 2), false);
                        } else if (charAt2 == 'm') {
                            XMLGregorianCalendarImpl.this.setMinute(parseInt(2, 2));
                        } else if (charAt2 == 's') {
                            XMLGregorianCalendarImpl.this.setSecond(parseInt(2, 2));
                            if (peek() == '.') {
                                XMLGregorianCalendarImpl.this.setFractionalSecond(parseBigDecimal());
                            }
                        } else if (charAt2 == 'z') {
                            char peek = peek();
                            int i3 = 1;
                            if (peek == 'Z') {
                                this.vidx++;
                                XMLGregorianCalendarImpl.this.setTimezone(0);
                            } else if (peek == '+' || peek == '-') {
                                this.vidx++;
                                int parseInt = parseInt(2, 2);
                                skip(':');
                                int parseInt2 = parseInt(2, 2);
                                XMLGregorianCalendarImpl xMLGregorianCalendarImpl = XMLGregorianCalendarImpl.this;
                                int i4 = (parseInt * 60) + parseInt2;
                                if (peek != '+') {
                                    i3 = -1;
                                }
                                xMLGregorianCalendarImpl.setTimezone(i4 * i3);
                            }
                        } else {
                            throw new InternalError();
                        }
                    }
                } else if (this.vidx == this.vlen) {
                    XMLGregorianCalendarImpl.this.testHour();
                    return;
                } else {
                    throw new IllegalArgumentException(this.value);
                }
            }
        }

        private char peek() throws IllegalArgumentException {
            int i = this.vidx;
            if (i == this.vlen) {
                return 65535;
            }
            return this.value.charAt(i);
        }

        private char read() throws IllegalArgumentException {
            int i = this.vidx;
            if (i != this.vlen) {
                String str = this.value;
                this.vidx = i + 1;
                return str.charAt(i);
            }
            throw new IllegalArgumentException(this.value);
        }

        private void skip(char c) throws IllegalArgumentException {
            if (read() != c) {
                throw new IllegalArgumentException(this.value);
            }
        }

        private int parseInt(int i, int i2) throws IllegalArgumentException {
            int i3 = this.vidx;
            int i4 = 0;
            while (true) {
                char peek = peek();
                if (!XMLGregorianCalendarImpl.isDigit(peek)) {
                    break;
                }
                int i5 = this.vidx;
                if (i5 - i3 > i2) {
                    break;
                }
                this.vidx = i5 + 1;
                i4 = ((i4 * 10) + peek) - 48;
            }
            if (this.vidx - i3 >= i) {
                return i4;
            }
            throw new IllegalArgumentException(this.value);
        }

        private void parseAndSetYear(int i) throws IllegalArgumentException {
            boolean z;
            int i2 = this.vidx;
            int i3 = 0;
            if (peek() == '-') {
                this.vidx++;
                z = true;
            } else {
                z = false;
            }
            while (true) {
                char peek = peek();
                if (!XMLGregorianCalendarImpl.isDigit(peek)) {
                    break;
                }
                this.vidx++;
                i3 = ((i3 * 10) + peek) - 48;
            }
            int i4 = this.vidx;
            if (i4 - i2 < i) {
                throw new IllegalArgumentException(this.value);
            } else if (i4 - i2 < 7) {
                if (z) {
                    i3 = -i3;
                }
                XMLGregorianCalendarImpl.this.year = i3;
                XMLGregorianCalendarImpl.this.eon = null;
            } else {
                XMLGregorianCalendarImpl.this.setYear(new BigInteger(this.value.substring(i2, i4)));
            }
        }

        private BigDecimal parseBigDecimal() throws IllegalArgumentException {
            int i = this.vidx;
            if (peek() == '.') {
                this.vidx++;
                while (XMLGregorianCalendarImpl.isDigit(peek())) {
                    this.vidx++;
                }
                return new BigDecimal(this.value.substring(i, this.vidx));
            }
            throw new IllegalArgumentException(this.value);
        }
    }

    private String format(String str) {
        char[] cArr;
        int length;
        int i;
        int length2 = str.length();
        char[] cArr2 = new char[32];
        int i2 = 0;
        int i3 = 0;
        while (i2 < length2) {
            int i4 = i2 + 1;
            char charAt = str.charAt(i2);
            if (charAt != '%') {
                cArr2[i3] = charAt;
                i2 = i4;
                i3++;
            } else {
                i2 = i4 + 1;
                char charAt2 = str.charAt(i4);
                if (charAt2 == 'D') {
                    i3 = print2Number(cArr2, i3, getDay());
                } else if (charAt2 != 'M') {
                    if (charAt2 != 'Y') {
                        if (charAt2 == 'h') {
                            i3 = print2Number(cArr2, i3, getHour());
                        } else if (charAt2 == 'm') {
                            i3 = print2Number(cArr2, i3, getMinute());
                        } else if (charAt2 == 's') {
                            i3 = print2Number(cArr2, i3, getSecond());
                            if (getFractionalSecond() != null) {
                                String bigDecimal = getFractionalSecond().toString();
                                int indexOf = bigDecimal.indexOf("E-");
                                if (indexOf >= 0) {
                                    String substring = bigDecimal.substring(indexOf + 2);
                                    String substring2 = bigDecimal.substring(0, indexOf);
                                    int indexOf2 = substring2.indexOf(".");
                                    if (indexOf2 >= 0) {
                                        substring2 = substring2.substring(0, indexOf2) + substring2.substring(indexOf2 + 1);
                                    }
                                    int parseInt = Integer.parseInt(substring);
                                    if (parseInt < 40) {
                                        substring2 = "00000000000000000000000000000000000000000".substring(0, parseInt - 1) + substring2;
                                    } else {
                                        while (parseInt > 1) {
                                            substring2 = "0" + substring2;
                                            parseInt--;
                                        }
                                    }
                                    bigDecimal = "0." + substring2;
                                }
                                cArr = new char[(cArr2.length + bigDecimal.length())];
                                System.arraycopy(cArr2, 0, cArr, 0, i3);
                                bigDecimal.getChars(1, bigDecimal.length(), cArr, i3);
                                length = bigDecimal.length() - 1;
                            }
                        } else if (charAt2 == 'z') {
                            int timezone2 = getTimezone();
                            if (timezone2 == 0) {
                                cArr2[i3] = 'Z';
                                i3++;
                            } else if (timezone2 != Integer.MIN_VALUE) {
                                if (timezone2 < 0) {
                                    i = i3 + 1;
                                    cArr2[i3] = LocaleUtility.IETF_SEPARATOR;
                                    timezone2 *= -1;
                                } else {
                                    i = i3 + 1;
                                    cArr2[i3] = '+';
                                }
                                int print2Number = print2Number(cArr2, i, timezone2 / 60);
                                cArr2[print2Number] = ':';
                                i3 = print2Number(cArr2, print2Number + 1, timezone2 % 60);
                            }
                        } else {
                            throw new InternalError();
                        }
                    } else if (this.eon == null) {
                        int year2 = getYear();
                        if (year2 < 0) {
                            cArr2[i3] = LocaleUtility.IETF_SEPARATOR;
                            year2 = -year2;
                            i3++;
                        }
                        i3 = print4Number(cArr2, i3, year2);
                    } else {
                        String bigInteger = getEonAndYear().toString();
                        cArr = new char[(cArr2.length + bigInteger.length())];
                        System.arraycopy(cArr2, 0, cArr, 0, i3);
                        int length3 = bigInteger.length();
                        while (length3 < 4) {
                            cArr[i3] = '0';
                            length3++;
                            i3++;
                        }
                        bigInteger.getChars(0, bigInteger.length(), cArr, i3);
                        length = bigInteger.length();
                    }
                    i3 += length;
                    cArr2 = cArr;
                } else {
                    i3 = print2Number(cArr2, i3, getMonth());
                }
            }
        }
        return new String(cArr2, 0, i3);
    }

    private int print2Number(char[] cArr, int i, int i2) {
        int i3 = i + 1;
        cArr[i] = (char) ((i2 / 10) + 48);
        int i4 = i3 + 1;
        cArr[i3] = (char) ((i2 % 10) + 48);
        return i4;
    }

    private int print4Number(char[] cArr, int i, int i2) {
        cArr[i + 3] = (char) ((i2 % 10) + 48);
        int i3 = i2 / 10;
        cArr[i + 2] = (char) ((i3 % 10) + 48);
        int i4 = i3 / 10;
        cArr[i + 1] = (char) ((i4 % 10) + 48);
        cArr[i] = (char) (((i4 / 10) % 10) + 48);
        return i + 4;
    }

    static BigInteger sanitize(Number number, int i) {
        if (i == 0 || number == null) {
            return BigInteger.ZERO;
        }
        BigInteger bigInteger = (BigInteger) number;
        return i < 0 ? bigInteger.negate() : bigInteger;
    }
}

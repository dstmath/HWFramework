package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.math.BigDecimal;
import ohos.com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl;
import ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.javax.xml.datatype.DatatypeFactory;
import ohos.javax.xml.datatype.Duration;
import ohos.javax.xml.datatype.XMLGregorianCalendar;

public abstract class AbstractDateTimeDV extends TypeValidator {
    protected static final int DAY = 1;
    private static final boolean DEBUG = false;
    protected static final int MONTH = 1;
    protected static final int YEAR = 2000;
    protected static final DatatypeFactory datatypeFactory = new DatatypeFactoryImpl();

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2552;
    }

    /* access modifiers changed from: protected */
    public Duration getDuration(DateTimeData dateTimeData) {
        return null;
    }

    /* access modifiers changed from: protected */
    public XMLGregorianCalendar getXMLGregorianCalendar(DateTimeData dateTimeData) {
        return null;
    }

    /* access modifiers changed from: protected */
    public int mod(int i, int i2, int i3) {
        return i - (i3 * i2);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public boolean isIdentical(Object obj, Object obj2) {
        if ((obj instanceof DateTimeData) && (obj2 instanceof DateTimeData)) {
            DateTimeData dateTimeData = (DateTimeData) obj;
            DateTimeData dateTimeData2 = (DateTimeData) obj2;
            if (dateTimeData.timezoneHr == dateTimeData2.timezoneHr && dateTimeData.timezoneMin == dateTimeData2.timezoneMin) {
                return dateTimeData.equals(dateTimeData2);
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public int compare(Object obj, Object obj2) {
        return compareDates((DateTimeData) obj, (DateTimeData) obj2, true);
    }

    /* access modifiers changed from: protected */
    public short compareDates(DateTimeData dateTimeData, DateTimeData dateTimeData2, boolean z) {
        if (dateTimeData.utc == dateTimeData2.utc) {
            return compareOrder(dateTimeData, dateTimeData2);
        }
        DateTimeData dateTimeData3 = new DateTimeData(null, this);
        if (dateTimeData.utc == 90) {
            cloneDate(dateTimeData2, dateTimeData3);
            dateTimeData3.timezoneHr = 14;
            dateTimeData3.timezoneMin = 0;
            dateTimeData3.utc = 43;
            normalize(dateTimeData3);
            short compareOrder = compareOrder(dateTimeData, dateTimeData3);
            if (compareOrder == -1) {
                return compareOrder;
            }
            cloneDate(dateTimeData2, dateTimeData3);
            dateTimeData3.timezoneHr = -14;
            dateTimeData3.timezoneMin = 0;
            dateTimeData3.utc = 45;
            normalize(dateTimeData3);
            short compareOrder2 = compareOrder(dateTimeData, dateTimeData3);
            if (compareOrder2 == 1) {
                return compareOrder2;
            }
            return 2;
        }
        if (dateTimeData2.utc == 90) {
            cloneDate(dateTimeData, dateTimeData3);
            dateTimeData3.timezoneHr = -14;
            dateTimeData3.timezoneMin = 0;
            dateTimeData3.utc = 45;
            normalize(dateTimeData3);
            short compareOrder3 = compareOrder(dateTimeData3, dateTimeData2);
            if (compareOrder3 == -1) {
                return compareOrder3;
            }
            cloneDate(dateTimeData, dateTimeData3);
            dateTimeData3.timezoneHr = 14;
            dateTimeData3.timezoneMin = 0;
            dateTimeData3.utc = 43;
            normalize(dateTimeData3);
            short compareOrder4 = compareOrder(dateTimeData3, dateTimeData2);
            if (compareOrder4 == 1) {
                return compareOrder4;
            }
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public short compareOrder(DateTimeData dateTimeData, DateTimeData dateTimeData2) {
        if (dateTimeData.position < 1) {
            if (dateTimeData.year < dateTimeData2.year) {
                return -1;
            }
            if (dateTimeData.year > dateTimeData2.year) {
                return 1;
            }
        }
        if (dateTimeData.position < 2) {
            if (dateTimeData.month < dateTimeData2.month) {
                return -1;
            }
            if (dateTimeData.month > dateTimeData2.month) {
                return 1;
            }
        }
        if (dateTimeData.day < dateTimeData2.day) {
            return -1;
        }
        if (dateTimeData.day > dateTimeData2.day) {
            return 1;
        }
        if (dateTimeData.hour < dateTimeData2.hour) {
            return -1;
        }
        if (dateTimeData.hour > dateTimeData2.hour) {
            return 1;
        }
        if (dateTimeData.minute < dateTimeData2.minute) {
            return -1;
        }
        if (dateTimeData.minute > dateTimeData2.minute) {
            return 1;
        }
        if (dateTimeData.second < dateTimeData2.second) {
            return -1;
        }
        if (dateTimeData.second > dateTimeData2.second) {
            return 1;
        }
        if (dateTimeData.utc < dateTimeData2.utc) {
            return -1;
        }
        if (dateTimeData.utc > dateTimeData2.utc) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void getTime(String str, int i, int i2, DateTimeData dateTimeData) throws RuntimeException {
        int i3 = i + 2;
        dateTimeData.hour = parseInt(str, i, i3);
        int i4 = i3 + 1;
        if (str.charAt(i3) == ':') {
            int i5 = i4 + 2;
            dateTimeData.minute = parseInt(str, i4, i5);
            int i6 = i5 + 1;
            if (str.charAt(i5) == ':') {
                int findUTCSign = findUTCSign(str, i4, i2);
                dateTimeData.second = parseSecond(str, i6, findUTCSign < 0 ? i2 : findUTCSign);
                if (findUTCSign > 0) {
                    getTimeZone(str, dateTimeData, findUTCSign, i2);
                    return;
                }
                return;
            }
            throw new RuntimeException("Error in parsing time zone");
        }
        throw new RuntimeException("Error in parsing time zone");
    }

    /* access modifiers changed from: protected */
    public int getDate(String str, int i, int i2, DateTimeData dateTimeData) throws RuntimeException {
        int yearMonth = getYearMonth(str, i, i2, dateTimeData);
        int i3 = yearMonth + 1;
        if (str.charAt(yearMonth) == '-') {
            int i4 = i3 + 2;
            dateTimeData.day = parseInt(str, i3, i4);
            return i4;
        }
        throw new RuntimeException("CCYY-MM must be followed by '-' sign");
    }

    /* access modifiers changed from: protected */
    public int getYearMonth(String str, int i, int i2, DateTimeData dateTimeData) throws RuntimeException {
        if (str.charAt(0) == '-') {
            i++;
        }
        int indexOf = indexOf(str, i, i2, LocaleUtility.IETF_SEPARATOR);
        if (indexOf != -1) {
            int i3 = indexOf - i;
            if (i3 < 4) {
                throw new RuntimeException("Year must have 'CCYY' format");
            } else if (i3 <= 4 || str.charAt(i) != '0') {
                dateTimeData.year = parseIntYear(str, indexOf);
                if (str.charAt(indexOf) == '-') {
                    int i4 = indexOf + 1;
                    int i5 = i4 + 2;
                    dateTimeData.month = parseInt(str, i4, i5);
                    return i5;
                }
                throw new RuntimeException("CCYY must be followed by '-' sign");
            } else {
                throw new RuntimeException("Leading zeros are required if the year value would otherwise have fewer than four digits; otherwise they are forbidden");
            }
        } else {
            throw new RuntimeException("Year separator is missing or misplaced");
        }
    }

    /* access modifiers changed from: protected */
    public void parseTimeZone(String str, int i, int i2, DateTimeData dateTimeData) throws RuntimeException {
        if (i >= i2) {
            return;
        }
        if (isNextCharUTCSign(str, i, i2)) {
            getTimeZone(str, dateTimeData, i, i2);
            return;
        }
        throw new RuntimeException("Error in month parsing");
    }

    /* access modifiers changed from: protected */
    public void getTimeZone(String str, DateTimeData dateTimeData, int i, int i2) throws RuntimeException {
        dateTimeData.utc = str.charAt(i);
        if (str.charAt(i) == 'Z') {
            if (i2 > i + 1) {
                throw new RuntimeException("Error in parsing time zone");
            }
        } else if (i <= i2 - 6) {
            int i3 = str.charAt(i) == '-' ? -1 : 1;
            int i4 = i + 1;
            int i5 = i4 + 2;
            dateTimeData.timezoneHr = parseInt(str, i4, i5) * i3;
            int i6 = i5 + 1;
            if (str.charAt(i5) == ':') {
                int i7 = i6 + 2;
                dateTimeData.timezoneMin = i3 * parseInt(str, i6, i7);
                if (i7 != i2) {
                    throw new RuntimeException("Error in parsing time zone");
                } else if (dateTimeData.timezoneHr != 0 || dateTimeData.timezoneMin != 0) {
                    dateTimeData.normalized = false;
                }
            } else {
                throw new RuntimeException("Error in parsing time zone");
            }
        } else {
            throw new RuntimeException("Error in parsing time zone");
        }
    }

    /* access modifiers changed from: protected */
    public int indexOf(String str, int i, int i2, char c) {
        while (i < i2) {
            if (str.charAt(i) == c) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void validateDateTime(DateTimeData dateTimeData) {
        if (dateTimeData.year == 0) {
            throw new RuntimeException("The year \"0000\" is an illegal year value");
        } else if (dateTimeData.month < 1 || dateTimeData.month > 12) {
            throw new RuntimeException("The month must have values 1 to 12");
        } else if (dateTimeData.day > maxDayInMonthFor(dateTimeData.year, dateTimeData.month) || dateTimeData.day < 1) {
            throw new RuntimeException("The day must have values 1 to 31");
        } else {
            if (dateTimeData.hour > 23 || dateTimeData.hour < 0) {
                if (dateTimeData.hour == 24 && dateTimeData.minute == 0 && dateTimeData.second == XPath.MATCH_SCORE_QNAME) {
                    dateTimeData.hour = 0;
                    int i = dateTimeData.day + 1;
                    dateTimeData.day = i;
                    if (i > maxDayInMonthFor(dateTimeData.year, dateTimeData.month)) {
                        dateTimeData.day = 1;
                        int i2 = dateTimeData.month + 1;
                        dateTimeData.month = i2;
                        if (i2 > 12) {
                            dateTimeData.month = 1;
                            int i3 = dateTimeData.year + 1;
                            dateTimeData.year = i3;
                            if (i3 == 0) {
                                dateTimeData.year = 1;
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("Hour must have values 0-23, unless 24:00:00");
                }
            }
            if (dateTimeData.minute > 59 || dateTimeData.minute < 0) {
                throw new RuntimeException("Minute must have values 0-59");
            } else if (dateTimeData.second >= 60.0d || dateTimeData.second < XPath.MATCH_SCORE_QNAME) {
                throw new RuntimeException("Second must have values 0-59");
            } else if (dateTimeData.timezoneHr > 14 || dateTimeData.timezoneHr < -14) {
                throw new RuntimeException("Time zone should have range -14:00 to +14:00");
            } else if ((dateTimeData.timezoneHr == 14 || dateTimeData.timezoneHr == -14) && dateTimeData.timezoneMin != 0) {
                throw new RuntimeException("Time zone should have range -14:00 to +14:00");
            } else if (dateTimeData.timezoneMin > 59 || dateTimeData.timezoneMin < -59) {
                throw new RuntimeException("Minute must have values 0-59");
            }
        }
    }

    /* access modifiers changed from: protected */
    public int findUTCSign(String str, int i, int i2) {
        while (i < i2) {
            char charAt = str.charAt(i);
            if (charAt == 'Z' || charAt == '+' || charAt == '-') {
                return i;
            }
            i++;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public final boolean isNextCharUTCSign(String str, int i, int i2) {
        if (i >= i2) {
            return false;
        }
        char charAt = str.charAt(i);
        return charAt == 'Z' || charAt == '+' || charAt == '-';
    }

    /* access modifiers changed from: protected */
    public int parseInt(String str, int i, int i2) throws NumberFormatException {
        int i3 = 0;
        do {
            int digit = getDigit(str.charAt(i));
            if (digit < 0) {
                throw new NumberFormatException("'" + str + "' has wrong format");
            } else if (i3 >= -214748364) {
                int i4 = i3 * 10;
                if (i4 >= -2147483647 + digit) {
                    i3 = i4 - digit;
                    i++;
                } else {
                    throw new NumberFormatException("'" + str + "' has wrong format");
                }
            } else {
                throw new NumberFormatException("'" + str + "' has wrong format");
            }
        } while (i < i2);
        return -i3;
    }

    /* access modifiers changed from: protected */
    public int parseIntYear(String str, int i) {
        int i2;
        int i3;
        int i4 = 0;
        if (str.charAt(0) == '-') {
            i3 = Integer.MIN_VALUE;
            i2 = 1;
        } else {
            i3 = -2147483647;
            i2 = 0;
        }
        int i5 = i3 / 10;
        while (i2 < i) {
            int i6 = i2 + 1;
            int digit = getDigit(str.charAt(i2));
            if (digit < 0) {
                throw new NumberFormatException("'" + str + "' has wrong format");
            } else if (i4 >= i5) {
                int i7 = i4 * 10;
                if (i7 >= i3 + digit) {
                    i4 = i7 - digit;
                    i2 = i6;
                } else {
                    throw new NumberFormatException("'" + str + "' has wrong format");
                }
            } else {
                throw new NumberFormatException("'" + str + "' has wrong format");
            }
        }
        if (i2 == 0) {
            return -i4;
        }
        if (i2 > 1) {
            return i4;
        }
        throw new NumberFormatException("'" + str + "' has wrong format");
    }

    /* access modifiers changed from: protected */
    public void normalize(DateTimeData dateTimeData) {
        int i;
        int i2 = dateTimeData.minute + (dateTimeData.timezoneMin * -1);
        int fQuotient = fQuotient(i2, 60);
        dateTimeData.minute = mod(i2, 60, fQuotient);
        int i3 = dateTimeData.hour + (dateTimeData.timezoneHr * -1) + fQuotient;
        int fQuotient2 = fQuotient(i3, 24);
        dateTimeData.hour = mod(i3, 24, fQuotient2);
        dateTimeData.day += fQuotient2;
        while (true) {
            int maxDayInMonthFor = maxDayInMonthFor(dateTimeData.year, dateTimeData.month);
            int i4 = 1;
            if (dateTimeData.day < 1) {
                dateTimeData.day += maxDayInMonthFor(dateTimeData.year, dateTimeData.month - 1);
                i = -1;
            } else if (dateTimeData.day > maxDayInMonthFor) {
                dateTimeData.day -= maxDayInMonthFor;
                i = 1;
            } else {
                dateTimeData.utc = 90;
                return;
            }
            int i5 = dateTimeData.month + i;
            dateTimeData.month = modulo(i5, 1, 13);
            dateTimeData.year += fQuotient(i5, 1, 13);
            if (dateTimeData.year == 0) {
                if (dateTimeData.timezoneHr >= 0 && dateTimeData.timezoneMin >= 0) {
                    i4 = -1;
                }
                dateTimeData.year = i4;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void saveUnnormalized(DateTimeData dateTimeData) {
        dateTimeData.unNormYear = dateTimeData.year;
        dateTimeData.unNormMonth = dateTimeData.month;
        dateTimeData.unNormDay = dateTimeData.day;
        dateTimeData.unNormHour = dateTimeData.hour;
        dateTimeData.unNormMinute = dateTimeData.minute;
        dateTimeData.unNormSecond = dateTimeData.second;
    }

    /* access modifiers changed from: protected */
    public void resetDateObj(DateTimeData dateTimeData) {
        dateTimeData.year = 0;
        dateTimeData.month = 0;
        dateTimeData.day = 0;
        dateTimeData.hour = 0;
        dateTimeData.minute = 0;
        dateTimeData.second = XPath.MATCH_SCORE_QNAME;
        dateTimeData.utc = 0;
        dateTimeData.timezoneHr = 0;
        dateTimeData.timezoneMin = 0;
    }

    /* access modifiers changed from: protected */
    public int maxDayInMonthFor(int i, int i2) {
        if (i2 == 4 || i2 == 6 || i2 == 9 || i2 == 11) {
            return 30;
        }
        if (i2 == 2) {
            return isLeapYear(i) ? 29 : 28;
        }
        return 31;
    }

    private boolean isLeapYear(int i) {
        return i % 4 == 0 && (i % 100 != 0 || i % 400 == 0);
    }

    /* access modifiers changed from: protected */
    public int fQuotient(int i, int i2) {
        return (int) Math.floor((double) (((float) i) / ((float) i2)));
    }

    /* access modifiers changed from: protected */
    public int modulo(int i, int i2, int i3) {
        int i4 = i - i2;
        int i5 = i3 - i2;
        return mod(i4, i5, fQuotient(i4, i5)) + i2;
    }

    /* access modifiers changed from: protected */
    public int fQuotient(int i, int i2, int i3) {
        return fQuotient(i - i2, i3 - i2);
    }

    /* access modifiers changed from: protected */
    public String dateToString(DateTimeData dateTimeData) {
        StringBuffer stringBuffer = new StringBuffer(25);
        append(stringBuffer, dateTimeData.year, 4);
        stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
        append(stringBuffer, dateTimeData.month, 2);
        stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
        append(stringBuffer, dateTimeData.day, 2);
        stringBuffer.append('T');
        append(stringBuffer, dateTimeData.hour, 2);
        stringBuffer.append(':');
        append(stringBuffer, dateTimeData.minute, 2);
        stringBuffer.append(':');
        append(stringBuffer, dateTimeData.second);
        append(stringBuffer, (char) dateTimeData.utc, 0);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    public final void append(StringBuffer stringBuffer, int i, int i2) {
        if (i == Integer.MIN_VALUE) {
            stringBuffer.append(i);
            return;
        }
        if (i < 0) {
            stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
            i = -i;
        }
        if (i2 == 4) {
            if (i < 10) {
                stringBuffer.append("000");
            } else if (i < 100) {
                stringBuffer.append("00");
            } else if (i < 1000) {
                stringBuffer.append('0');
            }
            stringBuffer.append(i);
        } else if (i2 == 2) {
            if (i < 10) {
                stringBuffer.append('0');
            }
            stringBuffer.append(i);
        } else if (i != 0) {
            stringBuffer.append((char) i);
        }
    }

    /* access modifiers changed from: protected */
    public final void append(StringBuffer stringBuffer, double d) {
        if (d < XPath.MATCH_SCORE_QNAME) {
            stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
            d = -d;
        }
        if (d < 10.0d) {
            stringBuffer.append('0');
        }
        append2(stringBuffer, d);
    }

    /* access modifiers changed from: protected */
    public final void append2(StringBuffer stringBuffer, double d) {
        int i = (int) d;
        if (d == ((double) i)) {
            stringBuffer.append(i);
        } else {
            append3(stringBuffer, d);
        }
    }

    private void append3(StringBuffer stringBuffer, double d) {
        String valueOf = String.valueOf(d);
        int indexOf = valueOf.indexOf(69);
        if (indexOf == -1) {
            stringBuffer.append(valueOf);
            return;
        }
        int i = 0;
        if (d < 1.0d) {
            try {
                int parseInt = parseInt(valueOf, indexOf + 2, valueOf.length());
                stringBuffer.append("0.");
                for (int i2 = 1; i2 < parseInt; i2++) {
                    stringBuffer.append('0');
                }
                int i3 = indexOf - 1;
                while (i3 > 0 && valueOf.charAt(i3) == '0') {
                    i3--;
                }
                while (i <= i3) {
                    char charAt = valueOf.charAt(i);
                    if (charAt != '.') {
                        stringBuffer.append(charAt);
                    }
                    i++;
                }
            } catch (Exception unused) {
                stringBuffer.append(valueOf);
            }
        } else {
            try {
                int parseInt2 = parseInt(valueOf, indexOf + 1, valueOf.length()) + 2;
                while (i < indexOf) {
                    char charAt2 = valueOf.charAt(i);
                    if (charAt2 != '.') {
                        if (i == parseInt2) {
                            stringBuffer.append('.');
                        }
                        stringBuffer.append(charAt2);
                    }
                    i++;
                }
                for (int i4 = parseInt2 - indexOf; i4 > 0; i4--) {
                    stringBuffer.append('0');
                }
            } catch (Exception unused2) {
                stringBuffer.append(valueOf);
            }
        }
    }

    /* access modifiers changed from: protected */
    public double parseSecond(String str, int i, int i2) throws NumberFormatException {
        int i3 = -1;
        for (int i4 = i; i4 < i2; i4++) {
            char charAt = str.charAt(i4);
            if (charAt == '.') {
                i3 = i4;
            } else if (charAt > '9' || charAt < '0') {
                throw new NumberFormatException("'" + str + "' has wrong format");
            }
        }
        if (i3 == -1) {
            if (i + 2 != i2) {
                throw new NumberFormatException("'" + str + "' has wrong format");
            }
        } else if (i + 2 != i3 || i3 + 1 == i2) {
            throw new NumberFormatException("'" + str + "' has wrong format");
        }
        return Double.parseDouble(str.substring(i, i2));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cloneDate(DateTimeData dateTimeData, DateTimeData dateTimeData2) {
        dateTimeData2.year = dateTimeData.year;
        dateTimeData2.month = dateTimeData.month;
        dateTimeData2.day = dateTimeData.day;
        dateTimeData2.hour = dateTimeData.hour;
        dateTimeData2.minute = dateTimeData.minute;
        dateTimeData2.second = dateTimeData.second;
        dateTimeData2.utc = dateTimeData.utc;
        dateTimeData2.timezoneHr = dateTimeData.timezoneHr;
        dateTimeData2.timezoneMin = dateTimeData.timezoneMin;
    }

    /* access modifiers changed from: package-private */
    public static final class DateTimeData implements XSDateTime {
        private volatile String canonical;
        int day;
        int hour;
        int minute;
        int month;
        boolean normalized = true;
        private String originalValue;
        int position;
        double second;
        int timezoneHr;
        int timezoneMin;
        final AbstractDateTimeDV type;
        int unNormDay;
        int unNormHour;
        int unNormMinute;
        int unNormMonth;
        double unNormSecond;
        int unNormYear;
        int utc;
        int year;

        public DateTimeData(String str, AbstractDateTimeDV abstractDateTimeDV) {
            this.originalValue = str;
            this.type = abstractDateTimeDV;
        }

        public DateTimeData(int i, int i2, int i3, int i4, int i5, double d, int i6, String str, boolean z, AbstractDateTimeDV abstractDateTimeDV) {
            this.year = i;
            this.month = i2;
            this.day = i3;
            this.hour = i4;
            this.minute = i5;
            this.second = d;
            this.utc = i6;
            this.type = abstractDateTimeDV;
            this.originalValue = str;
        }

        public boolean equals(Object obj) {
            if ((obj instanceof DateTimeData) && this.type.compareDates(this, (DateTimeData) obj, true) == 0) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            DateTimeData dateTimeData = new DateTimeData(null, this.type);
            this.type.cloneDate(this, dateTimeData);
            this.type.normalize(dateTimeData);
            return this.type.dateToString(dateTimeData).hashCode();
        }

        public String toString() {
            if (this.canonical == null) {
                this.canonical = this.type.dateToString(this);
            }
            return this.canonical;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public int getYears() {
            if (this.type instanceof DurationDV) {
                return 0;
            }
            return this.normalized ? this.year : this.unNormYear;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public int getMonths() {
            if (this.type instanceof DurationDV) {
                return (this.year * 12) + this.month;
            }
            return this.normalized ? this.month : this.unNormMonth;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public int getDays() {
            if (this.type instanceof DurationDV) {
                return 0;
            }
            return this.normalized ? this.day : this.unNormDay;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public int getHours() {
            if (this.type instanceof DurationDV) {
                return 0;
            }
            return this.normalized ? this.hour : this.unNormHour;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public int getMinutes() {
            if (this.type instanceof DurationDV) {
                return 0;
            }
            return this.normalized ? this.minute : this.unNormMinute;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public double getSeconds() {
            if (this.type instanceof DurationDV) {
                return ((double) ((this.day * 24 * 60 * 60) + (this.hour * 60 * 60) + (this.minute * 60))) + this.second;
            }
            return this.normalized ? this.second : this.unNormSecond;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public boolean hasTimeZone() {
            return this.utc != 0;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public int getTimeZoneHours() {
            return this.timezoneHr;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public int getTimeZoneMinutes() {
            return this.timezoneMin;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public String getLexicalValue() {
            return this.originalValue;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public XSDateTime normalize() {
            if (this.normalized) {
                return this;
            }
            DateTimeData dateTimeData = (DateTimeData) clone();
            dateTimeData.normalized = true;
            return dateTimeData;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public boolean isNormalized() {
            return this.normalized;
        }

        public Object clone() {
            DateTimeData dateTimeData = new DateTimeData(this.year, this.month, this.day, this.hour, this.minute, this.second, this.utc, this.originalValue, this.normalized, this.type);
            dateTimeData.canonical = this.canonical;
            dateTimeData.position = this.position;
            dateTimeData.timezoneHr = this.timezoneHr;
            dateTimeData.timezoneMin = this.timezoneMin;
            dateTimeData.unNormYear = this.unNormYear;
            dateTimeData.unNormMonth = this.unNormMonth;
            dateTimeData.unNormDay = this.unNormDay;
            dateTimeData.unNormHour = this.unNormHour;
            dateTimeData.unNormMinute = this.unNormMinute;
            dateTimeData.unNormSecond = this.unNormSecond;
            return dateTimeData;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public XMLGregorianCalendar getXMLGregorianCalendar() {
            return this.type.getXMLGregorianCalendar(this);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xs.datatypes.XSDateTime
        public Duration getDuration() {
            return this.type.getDuration(this);
        }
    }

    /* access modifiers changed from: protected */
    public final BigDecimal getFractionalSecondsAsBigDecimal(DateTimeData dateTimeData) {
        StringBuffer stringBuffer = new StringBuffer();
        append3(stringBuffer, dateTimeData.unNormSecond);
        String stringBuffer2 = stringBuffer.toString();
        int indexOf = stringBuffer2.indexOf(46);
        if (indexOf == -1) {
            return null;
        }
        BigDecimal bigDecimal = new BigDecimal(stringBuffer2.substring(indexOf));
        if (bigDecimal.compareTo(BigDecimal.valueOf(0L)) == 0) {
            return null;
        }
        return bigDecimal;
    }
}

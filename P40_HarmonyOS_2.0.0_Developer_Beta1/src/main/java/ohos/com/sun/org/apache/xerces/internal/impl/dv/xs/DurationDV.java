package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.math.BigDecimal;
import java.math.BigInteger;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.javax.xml.datatype.DatatypeFactory;
import ohos.javax.xml.datatype.Duration;

public class DurationDV extends AbstractDateTimeDV {
    private static final AbstractDateTimeDV.DateTimeData[] DATETIMES = {new AbstractDateTimeDV.DateTimeData(1696, 9, 1, 0, 0, XPath.MATCH_SCORE_QNAME, 90, null, true, null), new AbstractDateTimeDV.DateTimeData(1697, 2, 1, 0, 0, XPath.MATCH_SCORE_QNAME, 90, null, true, null), new AbstractDateTimeDV.DateTimeData(1903, 3, 1, 0, 0, XPath.MATCH_SCORE_QNAME, 90, null, true, null), new AbstractDateTimeDV.DateTimeData(1903, 7, 1, 0, 0, XPath.MATCH_SCORE_QNAME, 90, null, true, null)};
    public static final int DAYTIMEDURATION_TYPE = 2;
    public static final int DURATION_TYPE = 0;
    public static final int YEARMONTHDURATION_TYPE = 1;

    private short compareResults(short s, short s2, boolean z) {
        if (s2 == 2) {
            return 2;
        }
        if (s != s2 && z) {
            return 2;
        }
        if (s == s2 || z) {
            return s;
        }
        if (s == 0 || s2 == 0) {
            return s != 0 ? s : s2;
        }
        return 2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return parse(str, 0);
        } catch (Exception unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_DURATION});
        }
    }

    /* access modifiers changed from: protected */
    public AbstractDateTimeDV.DateTimeData parse(String str, int i) throws SchemaDateTimeException {
        int i2;
        int length = str.length();
        AbstractDateTimeDV.DateTimeData dateTimeData = new AbstractDateTimeDV.DateTimeData(str, this);
        boolean z = false;
        char charAt = str.charAt(0);
        if (charAt == 'P' || charAt == '-') {
            dateTimeData.utc = charAt == '-' ? 45 : 0;
            if (charAt != '-') {
                i2 = 1;
            } else if (str.charAt(1) == 'P') {
                i2 = 2;
            } else {
                throw new SchemaDateTimeException();
            }
            int i3 = dateTimeData.utc == 45 ? -1 : 1;
            int indexOf = indexOf(str, i2, length, 'T');
            if (indexOf == -1) {
                indexOf = length;
            } else if (i == 1) {
                throw new SchemaDateTimeException();
            }
            int indexOf2 = indexOf(str, i2, indexOf, 'Y');
            if (indexOf2 != -1) {
                if (i != 2) {
                    dateTimeData.year = parseInt(str, i2, indexOf2) * i3;
                    i2 = indexOf2 + 1;
                    z = true;
                } else {
                    throw new SchemaDateTimeException();
                }
            }
            int indexOf3 = indexOf(str, i2, indexOf, 'M');
            if (indexOf3 != -1) {
                if (i != 2) {
                    dateTimeData.month = parseInt(str, i2, indexOf3) * i3;
                    i2 = indexOf3 + 1;
                    z = true;
                } else {
                    throw new SchemaDateTimeException();
                }
            }
            int indexOf4 = indexOf(str, i2, indexOf, 'D');
            if (indexOf4 != -1) {
                if (i != 1) {
                    dateTimeData.day = parseInt(str, i2, indexOf4) * i3;
                    i2 = indexOf4 + 1;
                    z = true;
                } else {
                    throw new SchemaDateTimeException();
                }
            }
            if (length != indexOf || i2 == length) {
                if (length != indexOf) {
                    int i4 = i2 + 1;
                    int indexOf5 = indexOf(str, i4, length, 'H');
                    if (indexOf5 != -1) {
                        dateTimeData.hour = parseInt(str, i4, indexOf5) * i3;
                        i4 = indexOf5 + 1;
                        z = true;
                    }
                    int indexOf6 = indexOf(str, i4, length, 'M');
                    if (indexOf6 != -1) {
                        dateTimeData.minute = parseInt(str, i4, indexOf6) * i3;
                        i4 = indexOf6 + 1;
                        z = true;
                    }
                    int indexOf7 = indexOf(str, i4, length, 'S');
                    if (indexOf7 != -1) {
                        dateTimeData.second = ((double) i3) * parseSecond(str, i4, indexOf7);
                        i4 = indexOf7 + 1;
                        z = true;
                    }
                    if (i4 != length || str.charAt(i4 - 1) == 'T') {
                        throw new SchemaDateTimeException();
                    }
                }
                if (z) {
                    return dateTimeData;
                }
                throw new SchemaDateTimeException();
            }
            throw new SchemaDateTimeException();
        }
        throw new SchemaDateTimeException();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public short compareDates(AbstractDateTimeDV.DateTimeData dateTimeData, AbstractDateTimeDV.DateTimeData dateTimeData2, boolean z) {
        short compareResults;
        short compareResults2;
        if (compareOrder(dateTimeData, dateTimeData2) == 0) {
            return 0;
        }
        AbstractDateTimeDV.DateTimeData[] dateTimeDataArr = {new AbstractDateTimeDV.DateTimeData(null, this), new AbstractDateTimeDV.DateTimeData(null, this)};
        short compareOrder = compareOrder(addDuration(dateTimeData, DATETIMES[0], dateTimeDataArr[0]), addDuration(dateTimeData2, DATETIMES[0], dateTimeDataArr[1]));
        if (compareOrder == 2 || (compareResults = compareResults(compareOrder, compareOrder(addDuration(dateTimeData, DATETIMES[1], dateTimeDataArr[0]), addDuration(dateTimeData2, DATETIMES[1], dateTimeDataArr[1])), z)) == 2 || (compareResults2 = compareResults(compareResults, compareOrder(addDuration(dateTimeData, DATETIMES[2], dateTimeDataArr[0]), addDuration(dateTimeData2, DATETIMES[2], dateTimeDataArr[1])), z)) == 2) {
            return 2;
        }
        return compareResults(compareResults2, compareOrder(addDuration(dateTimeData, DATETIMES[3], dateTimeDataArr[0]), addDuration(dateTimeData2, DATETIMES[3], dateTimeDataArr[1])), z);
    }

    private AbstractDateTimeDV.DateTimeData addDuration(AbstractDateTimeDV.DateTimeData dateTimeData, AbstractDateTimeDV.DateTimeData dateTimeData2, AbstractDateTimeDV.DateTimeData dateTimeData3) {
        int i;
        resetDateObj(dateTimeData3);
        int i2 = dateTimeData2.month + dateTimeData.month;
        dateTimeData3.month = modulo(i2, 1, 13);
        dateTimeData3.year = dateTimeData2.year + dateTimeData.year + fQuotient(i2, 1, 13);
        double d = dateTimeData2.second + dateTimeData.second;
        int floor = (int) Math.floor(d / 60.0d);
        dateTimeData3.second = d - ((double) (floor * 60));
        int i3 = dateTimeData2.minute + dateTimeData.minute + floor;
        int fQuotient = fQuotient(i3, 60);
        dateTimeData3.minute = mod(i3, 60, fQuotient);
        int i4 = dateTimeData2.hour + dateTimeData.hour + fQuotient;
        int fQuotient2 = fQuotient(i4, 24);
        dateTimeData3.hour = mod(i4, 24, fQuotient2);
        dateTimeData3.day = dateTimeData2.day + dateTimeData.day + fQuotient2;
        while (true) {
            int maxDayInMonthFor = maxDayInMonthFor(dateTimeData3.year, dateTimeData3.month);
            if (dateTimeData3.day < 1) {
                dateTimeData3.day += maxDayInMonthFor(dateTimeData3.year, dateTimeData3.month - 1);
                i = -1;
            } else if (dateTimeData3.day > maxDayInMonthFor) {
                dateTimeData3.day -= maxDayInMonthFor;
                i = 1;
            } else {
                dateTimeData3.utc = 90;
                return dateTimeData3;
            }
            int i5 = dateTimeData3.month + i;
            dateTimeData3.month = modulo(i5, 1, 13);
            dateTimeData3.year += fQuotient(i5, 1, 13);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
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
        if (i3 + 1 != i2) {
            double parseDouble = Double.parseDouble(str.substring(i, i2));
            if (parseDouble != Double.POSITIVE_INFINITY) {
                return parseDouble;
            }
            throw new NumberFormatException("'" + str + "' has wrong format");
        }
        throw new NumberFormatException("'" + str + "' has wrong format");
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public String dateToString(AbstractDateTimeDV.DateTimeData dateTimeData) {
        StringBuffer stringBuffer = new StringBuffer(30);
        if (dateTimeData.year < 0 || dateTimeData.month < 0 || dateTimeData.day < 0 || dateTimeData.hour < 0 || dateTimeData.minute < 0 || dateTimeData.second < XPath.MATCH_SCORE_QNAME) {
            stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
        }
        stringBuffer.append('P');
        int i = -1;
        stringBuffer.append((dateTimeData.year < 0 ? -1 : 1) * dateTimeData.year);
        stringBuffer.append('Y');
        stringBuffer.append((dateTimeData.month < 0 ? -1 : 1) * dateTimeData.month);
        stringBuffer.append('M');
        stringBuffer.append((dateTimeData.day < 0 ? -1 : 1) * dateTimeData.day);
        stringBuffer.append('D');
        stringBuffer.append('T');
        stringBuffer.append((dateTimeData.hour < 0 ? -1 : 1) * dateTimeData.hour);
        stringBuffer.append('H');
        stringBuffer.append((dateTimeData.minute < 0 ? -1 : 1) * dateTimeData.minute);
        stringBuffer.append('M');
        if (dateTimeData.second >= XPath.MATCH_SCORE_QNAME) {
            i = 1;
        }
        append2(stringBuffer, ((double) i) * dateTimeData.second);
        stringBuffer.append('S');
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public Duration getDuration(AbstractDateTimeDV.DateTimeData dateTimeData) {
        boolean z = true;
        int i = (dateTimeData.year < 0 || dateTimeData.month < 0 || dateTimeData.day < 0 || dateTimeData.hour < 0 || dateTimeData.minute < 0 || dateTimeData.second < XPath.MATCH_SCORE_QNAME) ? -1 : 1;
        DatatypeFactory datatypeFactory = datatypeFactory;
        if (i != 1) {
            z = false;
        }
        return datatypeFactory.newDuration(z, dateTimeData.year != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.year * i)) : null, dateTimeData.month != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.month * i)) : null, dateTimeData.day != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.day * i)) : null, dateTimeData.hour != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.hour * i)) : null, dateTimeData.minute != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.minute * i)) : null, dateTimeData.second != -2.147483648E9d ? new BigDecimal(String.valueOf(((double) i) * dateTimeData.second)) : null);
    }
}

package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.javax.xml.datatype.XMLGregorianCalendar;

public class YearDV extends AbstractDateTimeDV {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return parse(str);
        } catch (Exception unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_YEAR});
        }
    }

    /* access modifiers changed from: protected */
    public AbstractDateTimeDV.DateTimeData parse(String str) throws SchemaDateTimeException {
        AbstractDateTimeDV.DateTimeData dateTimeData = new AbstractDateTimeDV.DateTimeData(str, this);
        int length = str.length();
        int i = str.charAt(0) == '-' ? 1 : 0;
        int findUTCSign = findUTCSign(str, i, length);
        int i2 = (findUTCSign == -1 ? length : findUTCSign) - i;
        if (i2 < 4) {
            throw new RuntimeException("Year must have 'CCYY' format");
        } else if (i2 <= 4 || str.charAt(i) != '0') {
            if (findUTCSign == -1) {
                dateTimeData.year = parseIntYear(str, length);
            } else {
                dateTimeData.year = parseIntYear(str, findUTCSign);
                getTimeZone(str, dateTimeData, findUTCSign, length);
            }
            dateTimeData.month = 1;
            dateTimeData.day = 1;
            validateDateTime(dateTimeData);
            saveUnnormalized(dateTimeData);
            if (!(dateTimeData.utc == 0 || dateTimeData.utc == 90)) {
                normalize(dateTimeData);
            }
            dateTimeData.position = 0;
            return dateTimeData;
        } else {
            throw new RuntimeException("Leading zeros are required if the year value would otherwise have fewer than four digits; otherwise they are forbidden");
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public String dateToString(AbstractDateTimeDV.DateTimeData dateTimeData) {
        StringBuffer stringBuffer = new StringBuffer(5);
        append(stringBuffer, dateTimeData.year, 4);
        append(stringBuffer, (char) dateTimeData.utc, 0);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public XMLGregorianCalendar getXMLGregorianCalendar(AbstractDateTimeDV.DateTimeData dateTimeData) {
        return datatypeFactory.newXMLGregorianCalendar(dateTimeData.unNormYear, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, dateTimeData.hasTimeZone() ? (dateTimeData.timezoneHr * 60) + dateTimeData.timezoneMin : Integer.MIN_VALUE);
    }
}

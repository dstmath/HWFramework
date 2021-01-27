package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.math.BigInteger;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.javax.xml.datatype.XMLGregorianCalendar;

public class DateTimeDV extends AbstractDateTimeDV {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return parse(str);
        } catch (Exception unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_DATETIME});
        }
    }

    /* access modifiers changed from: protected */
    public AbstractDateTimeDV.DateTimeData parse(String str) throws SchemaDateTimeException {
        AbstractDateTimeDV.DateTimeData dateTimeData = new AbstractDateTimeDV.DateTimeData(str, this);
        int length = str.length();
        int indexOf = indexOf(str, 0, length, 'T');
        int date = getDate(str, 0, indexOf, dateTimeData);
        getTime(str, indexOf + 1, length, dateTimeData);
        if (date == indexOf) {
            validateDateTime(dateTimeData);
            saveUnnormalized(dateTimeData);
            if (!(dateTimeData.utc == 0 || dateTimeData.utc == 90)) {
                normalize(dateTimeData);
            }
            return dateTimeData;
        }
        throw new RuntimeException(str + " is an invalid dateTime dataype value. Invalid character(s) seprating date and time values.");
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public XMLGregorianCalendar getXMLGregorianCalendar(AbstractDateTimeDV.DateTimeData dateTimeData) {
        return datatypeFactory.newXMLGregorianCalendar(BigInteger.valueOf((long) dateTimeData.unNormYear), dateTimeData.unNormMonth, dateTimeData.unNormDay, dateTimeData.unNormHour, dateTimeData.unNormMinute, (int) dateTimeData.unNormSecond, dateTimeData.unNormSecond != XPath.MATCH_SCORE_QNAME ? getFractionalSecondsAsBigDecimal(dateTimeData) : null, dateTimeData.hasTimeZone() ? (dateTimeData.timezoneHr * 60) + dateTimeData.timezoneMin : Integer.MIN_VALUE);
    }
}

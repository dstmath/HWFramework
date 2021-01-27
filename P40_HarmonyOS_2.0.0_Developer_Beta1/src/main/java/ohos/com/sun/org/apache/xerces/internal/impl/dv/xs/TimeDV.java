package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.math.BigInteger;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.javax.xml.datatype.XMLGregorianCalendar;

public class TimeDV extends AbstractDateTimeDV {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return parse(str);
        } catch (Exception unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, "time"});
        }
    }

    /* access modifiers changed from: protected */
    public AbstractDateTimeDV.DateTimeData parse(String str) throws SchemaDateTimeException {
        AbstractDateTimeDV.DateTimeData dateTimeData = new AbstractDateTimeDV.DateTimeData(str, this);
        int length = str.length();
        dateTimeData.year = 2000;
        dateTimeData.month = 1;
        dateTimeData.day = 15;
        getTime(str, 0, length, dateTimeData);
        validateDateTime(dateTimeData);
        saveUnnormalized(dateTimeData);
        if (!(dateTimeData.utc == 0 || dateTimeData.utc == 90)) {
            normalize(dateTimeData);
        }
        dateTimeData.position = 2;
        return dateTimeData;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public String dateToString(AbstractDateTimeDV.DateTimeData dateTimeData) {
        StringBuffer stringBuffer = new StringBuffer(16);
        append(stringBuffer, dateTimeData.hour, 2);
        stringBuffer.append(':');
        append(stringBuffer, dateTimeData.minute, 2);
        stringBuffer.append(':');
        append(stringBuffer, dateTimeData.second);
        append(stringBuffer, (char) dateTimeData.utc, 0);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public XMLGregorianCalendar getXMLGregorianCalendar(AbstractDateTimeDV.DateTimeData dateTimeData) {
        return datatypeFactory.newXMLGregorianCalendar((BigInteger) null, Integer.MIN_VALUE, Integer.MIN_VALUE, dateTimeData.unNormHour, dateTimeData.unNormMinute, (int) dateTimeData.unNormSecond, dateTimeData.unNormSecond != XPath.MATCH_SCORE_QNAME ? getFractionalSecondsAsBigDecimal(dateTimeData) : null, dateTimeData.hasTimeZone() ? (dateTimeData.timezoneHr * 60) + dateTimeData.timezoneMin : Integer.MIN_VALUE);
    }
}

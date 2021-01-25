package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.javax.xml.datatype.XMLGregorianCalendar;

public class MonthDV extends AbstractDateTimeDV {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return parse(str);
        } catch (Exception unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_MONTH});
        }
    }

    /* access modifiers changed from: protected */
    public AbstractDateTimeDV.DateTimeData parse(String str) throws SchemaDateTimeException {
        AbstractDateTimeDV.DateTimeData dateTimeData = new AbstractDateTimeDV.DateTimeData(str, this);
        int length = str.length();
        dateTimeData.year = 2000;
        dateTimeData.day = 1;
        if (str.charAt(0) == '-' && str.charAt(1) == '-') {
            int i = 4;
            dateTimeData.month = parseInt(str, 2, 4);
            if (str.length() >= 6 && str.charAt(4) == '-' && str.charAt(5) == '-') {
                i = 6;
            }
            if (i < length) {
                if (isNextCharUTCSign(str, i, length)) {
                    getTimeZone(str, dateTimeData, i, length);
                } else {
                    throw new SchemaDateTimeException("Error in month parsing: " + str);
                }
            }
            validateDateTime(dateTimeData);
            saveUnnormalized(dateTimeData);
            if (!(dateTimeData.utc == 0 || dateTimeData.utc == 90)) {
                normalize(dateTimeData);
            }
            dateTimeData.position = 1;
            return dateTimeData;
        }
        throw new SchemaDateTimeException("Invalid format for gMonth: " + str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public String dateToString(AbstractDateTimeDV.DateTimeData dateTimeData) {
        StringBuffer stringBuffer = new StringBuffer(5);
        stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
        stringBuffer.append(LocaleUtility.IETF_SEPARATOR);
        append(stringBuffer, dateTimeData.month, 2);
        append(stringBuffer, (char) dateTimeData.utc, 0);
        return stringBuffer.toString();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public XMLGregorianCalendar getXMLGregorianCalendar(AbstractDateTimeDV.DateTimeData dateTimeData) {
        return datatypeFactory.newXMLGregorianCalendar(Integer.MIN_VALUE, dateTimeData.unNormMonth, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, dateTimeData.hasTimeZone() ? (dateTimeData.timezoneHr * 60) + dateTimeData.timezoneMin : Integer.MIN_VALUE);
    }
}

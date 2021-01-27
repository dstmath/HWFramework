package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.math.BigDecimal;
import java.math.BigInteger;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.javax.xml.datatype.DatatypeFactory;
import ohos.javax.xml.datatype.Duration;

class DayTimeDurationDV extends DurationDV {
    DayTimeDurationDV() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.DurationDV, ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return parse(str, 2);
        } catch (Exception unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, "dayTimeDuration"});
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.DurationDV, ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public Duration getDuration(AbstractDateTimeDV.DateTimeData dateTimeData) {
        boolean z = true;
        int i = (dateTimeData.day < 0 || dateTimeData.hour < 0 || dateTimeData.minute < 0 || dateTimeData.second < XPath.MATCH_SCORE_QNAME) ? -1 : 1;
        DatatypeFactory datatypeFactory = datatypeFactory;
        if (i != 1) {
            z = false;
        }
        return datatypeFactory.newDuration(z, (BigInteger) null, (BigInteger) null, dateTimeData.day != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.day * i)) : null, dateTimeData.hour != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.hour * i)) : null, dateTimeData.minute != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.minute * i)) : null, dateTimeData.second != -2.147483648E9d ? new BigDecimal(String.valueOf(((double) i) * dateTimeData.second)) : null);
    }
}

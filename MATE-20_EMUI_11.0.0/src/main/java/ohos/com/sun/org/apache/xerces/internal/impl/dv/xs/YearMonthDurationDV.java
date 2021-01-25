package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import java.math.BigDecimal;
import java.math.BigInteger;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV;
import ohos.javax.xml.datatype.DatatypeFactory;
import ohos.javax.xml.datatype.Duration;

class YearMonthDurationDV extends DurationDV {
    YearMonthDurationDV() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.DurationDV, ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return parse(str, 1);
        } catch (Exception unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, "yearMonthDuration"});
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.DurationDV, ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV
    public Duration getDuration(AbstractDateTimeDV.DateTimeData dateTimeData) {
        boolean z = true;
        int i = (dateTimeData.year < 0 || dateTimeData.month < 0) ? -1 : 1;
        DatatypeFactory datatypeFactory = datatypeFactory;
        if (i != 1) {
            z = false;
        }
        return datatypeFactory.newDuration(z, dateTimeData.year != Integer.MIN_VALUE ? BigInteger.valueOf((long) (dateTimeData.year * i)) : null, dateTimeData.month != Integer.MIN_VALUE ? BigInteger.valueOf((long) (i * dateTimeData.month)) : null, (BigInteger) null, (BigInteger) null, (BigInteger) null, (BigDecimal) null);
    }
}

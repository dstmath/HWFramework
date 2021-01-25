package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.DecimalDV;

public class IntegerDV extends DecimalDV {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.DecimalDV, ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        try {
            return new DecimalDV.XDecimal(str, true);
        } catch (NumberFormatException unused) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, "integer"});
        }
    }
}

package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

public class BooleanDV extends TypeValidator {
    private static final String[] fValueSpace = {"false", "true", "0", "1"};

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 24;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (str.equals(fValueSpace[0]) || str.equals(fValueSpace[2])) {
            return Boolean.FALSE;
        }
        if (str.equals(fValueSpace[1]) || str.equals(fValueSpace[3])) {
            return Boolean.TRUE;
        }
        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, "boolean"});
    }
}

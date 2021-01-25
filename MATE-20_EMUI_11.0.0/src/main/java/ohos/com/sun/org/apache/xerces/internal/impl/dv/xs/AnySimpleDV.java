package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

public class AnySimpleDV extends TypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        return str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 0;
    }
}

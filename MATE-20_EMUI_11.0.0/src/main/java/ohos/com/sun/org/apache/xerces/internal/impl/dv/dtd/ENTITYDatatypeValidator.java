package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

public class ENTITYDatatypeValidator implements DatatypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (!validationContext.isEntityUnparsed(str)) {
            throw new InvalidDatatypeValueException("ENTITYNotUnparsed", new Object[]{str});
        }
    }
}

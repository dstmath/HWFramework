package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;

public class NMTOKENDatatypeValidator implements DatatypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (!XMLChar.isValidNmtoken(str)) {
            throw new InvalidDatatypeValueException("NMTOKENInvalid", new Object[]{str});
        }
    }
}

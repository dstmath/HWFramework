package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;

public class IDREFDatatypeValidator implements DatatypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (validationContext.useNamespaces()) {
            if (!XMLChar.isValidNCName(str)) {
                throw new InvalidDatatypeValueException("IDREFInvalidWithNamespaces", new Object[]{str});
            }
        } else if (!XMLChar.isValidName(str)) {
            throw new InvalidDatatypeValueException("IDREFInvalid", new Object[]{str});
        }
        validationContext.addIdRef(str);
    }
}

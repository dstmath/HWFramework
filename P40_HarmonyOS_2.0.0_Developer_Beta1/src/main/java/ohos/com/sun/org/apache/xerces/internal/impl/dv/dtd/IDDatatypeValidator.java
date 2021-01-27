package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;

public class IDDatatypeValidator implements DatatypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (validationContext.useNamespaces()) {
            if (!XMLChar.isValidNCName(str)) {
                throw new InvalidDatatypeValueException("IDInvalidWithNamespaces", new Object[]{str});
            }
        } else if (!XMLChar.isValidName(str)) {
            throw new InvalidDatatypeValueException("IDInvalid", new Object[]{str});
        }
        if (!validationContext.isIdDeclared(str)) {
            validationContext.addId(str);
            return;
        }
        throw new InvalidDatatypeValueException("IDNotUnique", new Object[]{str});
    }
}

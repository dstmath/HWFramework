package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;

public class XML11IDDatatypeValidator extends IDDatatypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.IDDatatypeValidator, ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (validationContext.useNamespaces()) {
            if (!XML11Char.isXML11ValidNCName(str)) {
                throw new InvalidDatatypeValueException("IDInvalidWithNamespaces", new Object[]{str});
            }
        } else if (!XML11Char.isXML11ValidName(str)) {
            throw new InvalidDatatypeValueException("IDInvalid", new Object[]{str});
        }
        if (!validationContext.isIdDeclared(str)) {
            validationContext.addId(str);
            return;
        }
        throw new InvalidDatatypeValueException("IDNotUnique", new Object[]{str});
    }
}

package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;

public class XML11IDREFDatatypeValidator extends IDREFDatatypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.IDREFDatatypeValidator, ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (validationContext.useNamespaces()) {
            if (!XML11Char.isXML11ValidNCName(str)) {
                throw new InvalidDatatypeValueException("IDREFInvalidWithNamespaces", new Object[]{str});
            }
        } else if (!XML11Char.isXML11ValidName(str)) {
            throw new InvalidDatatypeValueException("IDREFInvalid", new Object[]{str});
        }
        validationContext.addIdRef(str);
    }
}

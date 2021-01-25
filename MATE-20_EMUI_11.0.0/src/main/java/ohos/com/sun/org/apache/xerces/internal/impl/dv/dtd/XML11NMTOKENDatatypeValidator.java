package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;

public class XML11NMTOKENDatatypeValidator extends NMTOKENDatatypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd.NMTOKENDatatypeValidator, ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (!XML11Char.isXML11ValidNmtoken(str)) {
            throw new InvalidDatatypeValueException("NMTOKENInvalid", new Object[]{str});
        }
    }
}

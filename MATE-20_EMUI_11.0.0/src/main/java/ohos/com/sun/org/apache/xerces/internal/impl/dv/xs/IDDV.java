package ohos.com.sun.org.apache.xerces.internal.impl.dv.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;

public class IDDV extends TypeValidator {
    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public short getAllowedFacets() {
        return 2079;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public Object getActualValue(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        if (XMLChar.isValidNCName(str)) {
            return str;
        }
        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{str, SchemaSymbols.ATTVAL_NCNAME});
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator
    public void checkExtraRules(Object obj, ValidationContext validationContext) throws InvalidDatatypeValueException {
        String str = (String) obj;
        if (!validationContext.isIdDeclared(str)) {
            validationContext.addId(str);
            return;
        }
        throw new InvalidDatatypeValueException("cvc-id.2", new Object[]{str});
    }
}

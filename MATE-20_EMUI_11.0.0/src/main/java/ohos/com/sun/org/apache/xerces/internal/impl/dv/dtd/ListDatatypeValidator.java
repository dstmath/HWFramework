package ohos.com.sun.org.apache.xerces.internal.impl.dv.dtd;

import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

public class ListDatatypeValidator implements DatatypeValidator {
    DatatypeValidator fItemValidator;

    public ListDatatypeValidator(DatatypeValidator datatypeValidator) {
        this.fItemValidator = datatypeValidator;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator
    public void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException {
        StringTokenizer stringTokenizer = new StringTokenizer(str, " ");
        if (stringTokenizer.countTokens() != 0) {
            while (stringTokenizer.hasMoreTokens()) {
                this.fItemValidator.validate(stringTokenizer.nextToken(), validationContext);
            }
            return;
        }
        throw new InvalidDatatypeValueException("EmptyList", null);
    }
}

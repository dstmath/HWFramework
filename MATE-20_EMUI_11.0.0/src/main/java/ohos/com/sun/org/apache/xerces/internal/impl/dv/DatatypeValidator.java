package ohos.com.sun.org.apache.xerces.internal.impl.dv;

public interface DatatypeValidator {
    void validate(String str, ValidationContext validationContext) throws InvalidDatatypeValueException;
}

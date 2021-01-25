package org.bouncycastle.asn1.x509;

public interface NameConstraintValidator {
    void addExcludedSubtree(GeneralSubtree generalSubtree);

    void checkExcluded(GeneralName generalName) throws NameConstraintValidatorException;

    void checkPermitted(GeneralName generalName) throws NameConstraintValidatorException;

    void intersectEmptyPermittedSubtree(int i);

    void intersectPermittedSubtree(GeneralSubtree generalSubtree);

    void intersectPermittedSubtree(GeneralSubtree[] generalSubtreeArr);
}

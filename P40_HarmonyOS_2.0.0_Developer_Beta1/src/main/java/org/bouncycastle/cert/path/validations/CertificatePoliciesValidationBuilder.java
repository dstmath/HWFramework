package org.bouncycastle.cert.path.validations;

import org.bouncycastle.cert.path.CertPath;

public class CertificatePoliciesValidationBuilder {
    private boolean isAnyPolicyInhibited;
    private boolean isExplicitPolicyRequired;
    private boolean isPolicyMappingInhibited;

    public CertificatePoliciesValidation build(int i) {
        return new CertificatePoliciesValidation(i, this.isExplicitPolicyRequired, this.isAnyPolicyInhibited, this.isPolicyMappingInhibited);
    }

    public CertificatePoliciesValidation build(CertPath certPath) {
        return build(certPath.length());
    }

    public void setAnyPolicyInhibited(boolean z) {
        this.isAnyPolicyInhibited = z;
    }

    public void setExplicitPolicyRequired(boolean z) {
        this.isExplicitPolicyRequired = z;
    }

    public void setPolicyMappingInhibited(boolean z) {
        this.isPolicyMappingInhibited = z;
    }
}

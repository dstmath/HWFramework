package org.bouncycastle.cert.path.validations;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.PolicyConstraints;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.path.CertPathValidation;
import org.bouncycastle.cert.path.CertPathValidationContext;
import org.bouncycastle.cert.path.CertPathValidationException;
import org.bouncycastle.util.Memoable;

public class CertificatePoliciesValidation implements CertPathValidation {
    private int explicitPolicy;
    private int inhibitAnyPolicy;
    private int policyMapping;

    CertificatePoliciesValidation(int i) {
        this(i, false, false, false);
    }

    CertificatePoliciesValidation(int i, boolean z, boolean z2, boolean z3) {
        if (z) {
            this.explicitPolicy = 0;
        } else {
            this.explicitPolicy = i + 1;
        }
        if (z2) {
            this.inhibitAnyPolicy = 0;
        } else {
            this.inhibitAnyPolicy = i + 1;
        }
        if (z3) {
            this.policyMapping = 0;
        } else {
            this.policyMapping = i + 1;
        }
    }

    private int countDown(int i) {
        if (i != 0) {
            return i - 1;
        }
        return 0;
    }

    @Override // org.bouncycastle.util.Memoable
    public Memoable copy() {
        return new CertificatePoliciesValidation(0);
    }

    @Override // org.bouncycastle.util.Memoable
    public void reset(Memoable memoable) {
        CertificatePoliciesValidation certificatePoliciesValidation = (CertificatePoliciesValidation) memoable;
    }

    @Override // org.bouncycastle.cert.path.CertPathValidation
    public void validate(CertPathValidationContext certPathValidationContext, X509CertificateHolder x509CertificateHolder) throws CertPathValidationException {
        int intValueExact;
        certPathValidationContext.addHandledExtension(Extension.policyConstraints);
        certPathValidationContext.addHandledExtension(Extension.inhibitAnyPolicy);
        if (!certPathValidationContext.isEndEntity() && !ValidationUtils.isSelfIssued(x509CertificateHolder)) {
            this.explicitPolicy = countDown(this.explicitPolicy);
            this.policyMapping = countDown(this.policyMapping);
            this.inhibitAnyPolicy = countDown(this.inhibitAnyPolicy);
            PolicyConstraints fromExtensions = PolicyConstraints.fromExtensions(x509CertificateHolder.getExtensions());
            if (fromExtensions != null) {
                BigInteger requireExplicitPolicyMapping = fromExtensions.getRequireExplicitPolicyMapping();
                if (requireExplicitPolicyMapping != null && requireExplicitPolicyMapping.intValue() < this.explicitPolicy) {
                    this.explicitPolicy = requireExplicitPolicyMapping.intValue();
                }
                BigInteger inhibitPolicyMapping = fromExtensions.getInhibitPolicyMapping();
                if (inhibitPolicyMapping != null && inhibitPolicyMapping.intValue() < this.policyMapping) {
                    this.policyMapping = inhibitPolicyMapping.intValue();
                }
            }
            Extension extension = x509CertificateHolder.getExtension(Extension.inhibitAnyPolicy);
            if (extension != null && (intValueExact = ASN1Integer.getInstance(extension.getParsedValue()).intValueExact()) < this.inhibitAnyPolicy) {
                this.inhibitAnyPolicy = intValueExact;
            }
        }
    }
}

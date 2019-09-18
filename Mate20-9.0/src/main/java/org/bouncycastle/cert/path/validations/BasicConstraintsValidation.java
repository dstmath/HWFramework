package org.bouncycastle.cert.path.validations;

import java.math.BigInteger;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.path.CertPathValidation;
import org.bouncycastle.cert.path.CertPathValidationContext;
import org.bouncycastle.cert.path.CertPathValidationException;
import org.bouncycastle.util.Memoable;

public class BasicConstraintsValidation implements CertPathValidation {
    private BasicConstraints bc;
    private boolean isMandatory;
    private BigInteger maxPathLength;
    private int pathLengthRemaining;

    public BasicConstraintsValidation() {
        this(true);
    }

    public BasicConstraintsValidation(boolean z) {
        this.isMandatory = z;
    }

    public Memoable copy() {
        BasicConstraintsValidation basicConstraintsValidation = new BasicConstraintsValidation(this.isMandatory);
        basicConstraintsValidation.bc = this.bc;
        basicConstraintsValidation.pathLengthRemaining = this.pathLengthRemaining;
        return basicConstraintsValidation;
    }

    public void reset(Memoable memoable) {
        BasicConstraintsValidation basicConstraintsValidation = (BasicConstraintsValidation) memoable;
        this.isMandatory = basicConstraintsValidation.isMandatory;
        this.bc = basicConstraintsValidation.bc;
        this.pathLengthRemaining = basicConstraintsValidation.pathLengthRemaining;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0071 A[ADDED_TO_REGION, RETURN] */
    public void validate(CertPathValidationContext certPathValidationContext, X509CertificateHolder x509CertificateHolder) throws CertPathValidationException {
        int i;
        if (this.maxPathLength == null || this.pathLengthRemaining >= 0) {
            certPathValidationContext.addHandledExtension(Extension.basicConstraints);
            BasicConstraints fromExtensions = BasicConstraints.fromExtensions(x509CertificateHolder.getExtensions());
            if (fromExtensions != null) {
                if (this.bc == null) {
                    this.bc = fromExtensions;
                    if (fromExtensions.isCA()) {
                        this.maxPathLength = fromExtensions.getPathLenConstraint();
                        if (this.maxPathLength != null) {
                            i = this.maxPathLength.intValue();
                        }
                    }
                } else if (fromExtensions.isCA()) {
                    BigInteger pathLenConstraint = fromExtensions.getPathLenConstraint();
                    if (pathLenConstraint != null) {
                        int intValue = pathLenConstraint.intValue();
                        if (intValue < this.pathLengthRemaining) {
                            this.pathLengthRemaining = intValue;
                            this.bc = fromExtensions;
                        }
                    }
                }
                if (!this.isMandatory && this.bc == null) {
                    throw new CertPathValidationException("BasicConstraints not present in path");
                }
                return;
            }
            if (this.bc != null) {
                i = this.pathLengthRemaining - 1;
            }
            if (!this.isMandatory) {
                return;
            }
            return;
            this.pathLengthRemaining = i;
            if (!this.isMandatory) {
            }
        } else {
            throw new CertPathValidationException("BasicConstraints path length exceeded");
        }
    }
}

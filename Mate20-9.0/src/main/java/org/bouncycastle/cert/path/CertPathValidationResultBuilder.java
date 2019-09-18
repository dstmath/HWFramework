package org.bouncycastle.cert.path;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.util.Integers;

class CertPathValidationResultBuilder {
    private final List<Integer> certIndexes = new ArrayList();
    private final CertPathValidationContext context;
    private final List<CertPathValidationException> exceptions = new ArrayList();
    private final List<Integer> ruleIndexes = new ArrayList();

    CertPathValidationResultBuilder(CertPathValidationContext certPathValidationContext) {
        this.context = certPathValidationContext;
    }

    private int[] toInts(List<Integer> list) {
        int[] iArr = new int[list.size()];
        for (int i = 0; i != iArr.length; i++) {
            iArr[i] = list.get(i).intValue();
        }
        return iArr;
    }

    public void addException(int i, int i2, CertPathValidationException certPathValidationException) {
        this.certIndexes.add(Integers.valueOf(i));
        this.ruleIndexes.add(Integers.valueOf(i2));
        this.exceptions.add(certPathValidationException);
    }

    public CertPathValidationResult build() {
        return this.exceptions.isEmpty() ? new CertPathValidationResult(this.context) : new CertPathValidationResult(this.context, toInts(this.certIndexes), toInts(this.ruleIndexes), (CertPathValidationException[]) this.exceptions.toArray(new CertPathValidationException[this.exceptions.size()]));
    }
}

package org.bouncycastle.asn1.x9;

public abstract class X9ECParametersHolder {
    private X9ECParameters params;

    /* access modifiers changed from: protected */
    public abstract X9ECParameters createParameters();

    public synchronized X9ECParameters getParameters() {
        if (this.params == null) {
            this.params = createParameters();
        }
        return this.params;
    }
}

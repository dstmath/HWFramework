package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class DHGenParameterSpec implements AlgorithmParameterSpec {
    private int exponentSize;
    private int primeSize;

    public DHGenParameterSpec(int primeSize2, int exponentSize2) {
        this.primeSize = primeSize2;
        this.exponentSize = exponentSize2;
    }

    public int getPrimeSize() {
        return this.primeSize;
    }

    public int getExponentSize() {
        return this.exponentSize;
    }
}

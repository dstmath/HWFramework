package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class DHGenParameterSpec implements AlgorithmParameterSpec {
    private int exponentSize;
    private int primeSize;

    public DHGenParameterSpec(int primeSize, int exponentSize) {
        this.primeSize = primeSize;
        this.exponentSize = exponentSize;
    }

    public int getPrimeSize() {
        return this.primeSize;
    }

    public int getExponentSize() {
        return this.exponentSize;
    }
}

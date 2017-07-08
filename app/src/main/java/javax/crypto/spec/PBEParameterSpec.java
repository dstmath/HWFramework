package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class PBEParameterSpec implements AlgorithmParameterSpec {
    private int iterationCount;
    private byte[] salt;

    public PBEParameterSpec(byte[] salt, int iterationCount) {
        this.salt = (byte[]) salt.clone();
        this.iterationCount = iterationCount;
    }

    public byte[] getSalt() {
        return (byte[]) this.salt.clone();
    }

    public int getIterationCount() {
        return this.iterationCount;
    }
}

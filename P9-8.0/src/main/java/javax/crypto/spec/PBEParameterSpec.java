package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class PBEParameterSpec implements AlgorithmParameterSpec {
    private int iterationCount;
    private AlgorithmParameterSpec paramSpec = null;
    private byte[] salt;

    public PBEParameterSpec(byte[] salt, int iterationCount) {
        this.salt = (byte[]) salt.clone();
        this.iterationCount = iterationCount;
    }

    public PBEParameterSpec(byte[] salt, int iterationCount, AlgorithmParameterSpec paramSpec) {
        this.salt = (byte[]) salt.clone();
        this.iterationCount = iterationCount;
        this.paramSpec = paramSpec;
    }

    public byte[] getSalt() {
        return (byte[]) this.salt.clone();
    }

    public int getIterationCount() {
        return this.iterationCount;
    }

    public AlgorithmParameterSpec getParameterSpec() {
        return this.paramSpec;
    }
}

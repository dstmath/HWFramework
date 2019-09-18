package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class PBEParameterSpec implements AlgorithmParameterSpec {
    private int iterationCount;
    private AlgorithmParameterSpec paramSpec = null;
    private byte[] salt;

    public PBEParameterSpec(byte[] salt2, int iterationCount2) {
        this.salt = (byte[]) salt2.clone();
        this.iterationCount = iterationCount2;
    }

    public PBEParameterSpec(byte[] salt2, int iterationCount2, AlgorithmParameterSpec paramSpec2) {
        this.salt = (byte[]) salt2.clone();
        this.iterationCount = iterationCount2;
        this.paramSpec = paramSpec2;
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

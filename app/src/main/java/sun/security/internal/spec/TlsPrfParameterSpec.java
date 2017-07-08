package sun.security.internal.spec;

import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;

@Deprecated
public class TlsPrfParameterSpec implements AlgorithmParameterSpec {
    private final String label;
    private final int outputLength;
    private final int prfBlockSize;
    private final String prfHashAlg;
    private final int prfHashLength;
    private final SecretKey secret;
    private final byte[] seed;

    public TlsPrfParameterSpec(SecretKey secret, String label, byte[] seed, int outputLength, String prfHashAlg, int prfHashLength, int prfBlockSize) {
        if (label == null || seed == null) {
            throw new NullPointerException("label and seed must not be null");
        } else if (outputLength <= 0) {
            throw new IllegalArgumentException("outputLength must be positive");
        } else {
            this.secret = secret;
            this.label = label;
            this.seed = (byte[]) seed.clone();
            this.outputLength = outputLength;
            this.prfHashAlg = prfHashAlg;
            this.prfHashLength = prfHashLength;
            this.prfBlockSize = prfBlockSize;
        }
    }

    public SecretKey getSecret() {
        return this.secret;
    }

    public String getLabel() {
        return this.label;
    }

    public byte[] getSeed() {
        return (byte[]) this.seed.clone();
    }

    public int getOutputLength() {
        return this.outputLength;
    }

    public String getPRFHashAlg() {
        return this.prfHashAlg;
    }

    public int getPRFHashLength() {
        return this.prfHashLength;
    }

    public int getPRFBlockSize() {
        return this.prfBlockSize;
    }
}

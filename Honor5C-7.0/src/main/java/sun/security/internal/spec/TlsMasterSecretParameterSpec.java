package sun.security.internal.spec;

import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;

@Deprecated
public class TlsMasterSecretParameterSpec implements AlgorithmParameterSpec {
    private final byte[] clientRandom;
    private final int majorVersion;
    private final int minorVersion;
    private final SecretKey premasterSecret;
    private final int prfBlockSize;
    private final String prfHashAlg;
    private final int prfHashLength;
    private final byte[] serverRandom;

    public TlsMasterSecretParameterSpec(SecretKey premasterSecret, int majorVersion, int minorVersion, byte[] clientRandom, byte[] serverRandom, String prfHashAlg, int prfHashLength, int prfBlockSize) {
        if (premasterSecret == null) {
            throw new NullPointerException("premasterSecret must not be null");
        }
        this.premasterSecret = premasterSecret;
        this.majorVersion = checkVersion(majorVersion);
        this.minorVersion = checkVersion(minorVersion);
        this.clientRandom = (byte[]) clientRandom.clone();
        this.serverRandom = (byte[]) serverRandom.clone();
        this.prfHashAlg = prfHashAlg;
        this.prfHashLength = prfHashLength;
        this.prfBlockSize = prfBlockSize;
    }

    static int checkVersion(int version) {
        if (version >= 0 && version <= 255) {
            return version;
        }
        throw new IllegalArgumentException("Version must be between 0 and 255");
    }

    public SecretKey getPremasterSecret() {
        return this.premasterSecret;
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    public byte[] getClientRandom() {
        return (byte[]) this.clientRandom.clone();
    }

    public byte[] getServerRandom() {
        return (byte[]) this.serverRandom.clone();
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

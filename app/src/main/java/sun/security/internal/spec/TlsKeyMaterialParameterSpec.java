package sun.security.internal.spec;

import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;

@Deprecated
public class TlsKeyMaterialParameterSpec implements AlgorithmParameterSpec {
    private final String cipherAlgorithm;
    private final int cipherKeyLength;
    private final byte[] clientRandom;
    private final int expandedCipherKeyLength;
    private final int ivLength;
    private final int macKeyLength;
    private final int majorVersion;
    private final SecretKey masterSecret;
    private final int minorVersion;
    private final int prfBlockSize;
    private final String prfHashAlg;
    private final int prfHashLength;
    private final byte[] serverRandom;

    public TlsKeyMaterialParameterSpec(SecretKey masterSecret, int majorVersion, int minorVersion, byte[] clientRandom, byte[] serverRandom, String cipherAlgorithm, int cipherKeyLength, int expandedCipherKeyLength, int ivLength, int macKeyLength, String prfHashAlg, int prfHashLength, int prfBlockSize) {
        if (!masterSecret.getAlgorithm().equals("TlsMasterSecret")) {
            throw new IllegalArgumentException("Not a TLS master secret");
        } else if (cipherAlgorithm == null) {
            throw new NullPointerException();
        } else {
            this.masterSecret = masterSecret;
            this.majorVersion = TlsMasterSecretParameterSpec.checkVersion(majorVersion);
            this.minorVersion = TlsMasterSecretParameterSpec.checkVersion(minorVersion);
            this.clientRandom = (byte[]) clientRandom.clone();
            this.serverRandom = (byte[]) serverRandom.clone();
            this.cipherAlgorithm = cipherAlgorithm;
            this.cipherKeyLength = checkSign(cipherKeyLength);
            this.expandedCipherKeyLength = checkSign(expandedCipherKeyLength);
            this.ivLength = checkSign(ivLength);
            this.macKeyLength = checkSign(macKeyLength);
            this.prfHashAlg = prfHashAlg;
            this.prfHashLength = prfHashLength;
            this.prfBlockSize = prfBlockSize;
        }
    }

    private static int checkSign(int k) {
        if (k >= 0) {
            return k;
        }
        throw new IllegalArgumentException("Value must not be negative");
    }

    public SecretKey getMasterSecret() {
        return this.masterSecret;
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

    public String getCipherAlgorithm() {
        return this.cipherAlgorithm;
    }

    public int getCipherKeyLength() {
        return this.cipherKeyLength;
    }

    public int getExpandedCipherKeyLength() {
        if (this.majorVersion < 3 || this.minorVersion < 2) {
            return this.expandedCipherKeyLength;
        }
        return 0;
    }

    public int getIvLength() {
        if (this.majorVersion < 3 || this.minorVersion < 2) {
            return this.ivLength;
        }
        return 0;
    }

    public int getMacKeyLength() {
        return this.macKeyLength;
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

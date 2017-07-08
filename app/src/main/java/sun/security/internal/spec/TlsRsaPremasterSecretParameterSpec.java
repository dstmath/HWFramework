package sun.security.internal.spec;

import java.security.spec.AlgorithmParameterSpec;

@Deprecated
public class TlsRsaPremasterSecretParameterSpec implements AlgorithmParameterSpec {
    private final int majorVersion;
    private final int minorVersion;

    public TlsRsaPremasterSecretParameterSpec(int majorVersion, int minorVersion) {
        this.majorVersion = TlsMasterSecretParameterSpec.checkVersion(majorVersion);
        this.minorVersion = TlsMasterSecretParameterSpec.checkVersion(minorVersion);
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }
}

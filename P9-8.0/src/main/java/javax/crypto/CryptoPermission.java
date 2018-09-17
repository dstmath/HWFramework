package javax.crypto;

import java.security.Permission;
import java.security.spec.AlgorithmParameterSpec;

class CryptoPermission extends Permission {
    static final String ALG_NAME_WILDCARD = null;

    CryptoPermission(String alg) {
        super("");
    }

    CryptoPermission(String alg, int maxKeySize) {
        super("");
    }

    CryptoPermission(String alg, int maxKeySize, AlgorithmParameterSpec algParamSpec) {
        super("");
    }

    CryptoPermission(String alg, String exemptionMechanism) {
        super("");
    }

    CryptoPermission(String alg, int maxKeySize, String exemptionMechanism) {
        super("");
    }

    CryptoPermission(String alg, int maxKeySize, AlgorithmParameterSpec algParamSpec, String exemptionMechanism) {
        super("");
    }

    public boolean implies(Permission p) {
        return true;
    }

    public String getActions() {
        return null;
    }

    final String getAlgorithm() {
        return null;
    }

    final String getExemptionMechanism() {
        return null;
    }

    final int getMaxKeySize() {
        return Integer.MAX_VALUE;
    }

    final boolean getCheckParam() {
        return false;
    }

    final AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return null;
    }
}

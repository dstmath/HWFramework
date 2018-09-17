package java.security.cert;

public interface CertPathChecker {
    void check(Certificate certificate) throws CertPathValidatorException;

    void init(boolean z) throws CertPathValidatorException;

    boolean isForwardCheckingSupported();
}

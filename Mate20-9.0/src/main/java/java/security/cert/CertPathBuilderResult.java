package java.security.cert;

public interface CertPathBuilderResult extends Cloneable {
    Object clone();

    CertPath getCertPath();
}

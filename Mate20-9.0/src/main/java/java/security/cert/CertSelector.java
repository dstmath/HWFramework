package java.security.cert;

public interface CertSelector extends Cloneable {
    Object clone();

    boolean match(Certificate certificate);
}

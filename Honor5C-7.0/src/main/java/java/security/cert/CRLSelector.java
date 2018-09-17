package java.security.cert;

public interface CRLSelector extends Cloneable {
    Object clone();

    boolean match(CRL crl);
}

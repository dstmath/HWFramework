package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public interface CertAttrSet<T> {
    void delete(String str) throws CertificateException, IOException;

    void encode(OutputStream outputStream) throws CertificateException, IOException;

    Object get(String str) throws CertificateException, IOException;

    Enumeration<T> getElements();

    String getName();

    void set(String str, Object obj) throws CertificateException, IOException;

    String toString();
}

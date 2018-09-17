package java.security;

import java.io.Serializable;
import java.net.URL;
import java.security.cert.Certificate;

public class CodeSource implements Serializable {
    public CodeSource(URL url, Certificate[] certs) {
    }

    public CodeSource(URL url, CodeSigner[] signers) {
    }

    public final URL getLocation() {
        return null;
    }

    public final Certificate[] getCertificates() {
        return null;
    }

    public final CodeSigner[] getCodeSigners() {
        return null;
    }

    public boolean implies(CodeSource codesource) {
        return true;
    }
}

package java.security;

import java.io.Serializable;
import java.net.URL;
import java.security.cert.Certificate;

public class CodeSource implements Serializable {
    private URL location;

    public CodeSource(URL url, Certificate[] certs) {
        this.location = url;
    }

    public CodeSource(URL url, CodeSigner[] signers) {
        this.location = url;
    }

    public final URL getLocation() {
        return this.location;
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

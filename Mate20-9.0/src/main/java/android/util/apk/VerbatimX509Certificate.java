package android.util.apk;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

class VerbatimX509Certificate extends WrappedX509Certificate {
    private final byte[] mEncodedVerbatim;
    private int mHash = -1;

    VerbatimX509Certificate(X509Certificate wrapped, byte[] encodedVerbatim) {
        super(wrapped);
        this.mEncodedVerbatim = encodedVerbatim;
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return this.mEncodedVerbatim;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerbatimX509Certificate)) {
            return false;
        }
        try {
            return Arrays.equals(getEncoded(), ((VerbatimX509Certificate) o).getEncoded());
        } catch (CertificateEncodingException e) {
            return false;
        }
    }

    public int hashCode() {
        if (this.mHash == -1) {
            try {
                this.mHash = Arrays.hashCode(getEncoded());
            } catch (CertificateEncodingException e) {
                this.mHash = 0;
            }
        }
        return this.mHash;
    }
}

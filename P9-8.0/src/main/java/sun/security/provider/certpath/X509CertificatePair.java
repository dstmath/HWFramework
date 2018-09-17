package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.X509Factory;
import sun.security.util.Cache;
import sun.security.util.Cache.EqualByteArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509CertImpl;

public class X509CertificatePair {
    private static final byte TAG_FORWARD = (byte) 0;
    private static final byte TAG_REVERSE = (byte) 1;
    private static final Cache<Object, X509CertificatePair> cache = Cache.newSoftMemoryCache(750);
    private byte[] encoded;
    private X509Certificate forward;
    private X509Certificate reverse;

    public X509CertificatePair(X509Certificate forward, X509Certificate reverse) throws CertificateException {
        if (forward == null && reverse == null) {
            throw new CertificateException("at least one of certificate pair must be non-null");
        }
        this.forward = forward;
        this.reverse = reverse;
        checkPair();
    }

    private X509CertificatePair(byte[] encoded) throws CertificateException {
        try {
            parse(new DerValue(encoded));
            this.encoded = encoded;
            checkPair();
        } catch (IOException ex) {
            throw new CertificateException(ex.toString());
        }
    }

    public static synchronized void clearCache() {
        synchronized (X509CertificatePair.class) {
            cache.clear();
        }
    }

    public static synchronized X509CertificatePair generateCertificatePair(byte[] encoded) throws CertificateException {
        synchronized (X509CertificatePair.class) {
            X509CertificatePair pair = (X509CertificatePair) cache.get(new EqualByteArray(encoded));
            if (pair != null) {
                return pair;
            }
            pair = new X509CertificatePair(encoded);
            cache.put(new EqualByteArray(pair.encoded), pair);
            return pair;
        }
    }

    public void setForward(X509Certificate cert) throws CertificateException {
        checkPair();
        this.forward = cert;
    }

    public void setReverse(X509Certificate cert) throws CertificateException {
        checkPair();
        this.reverse = cert;
    }

    public X509Certificate getForward() {
        return this.forward;
    }

    public X509Certificate getReverse() {
        return this.reverse;
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        try {
            if (this.encoded == null) {
                DerOutputStream tmp = new DerOutputStream();
                emit(tmp);
                this.encoded = tmp.toByteArray();
            }
            return this.encoded;
        } catch (IOException ex) {
            throw new CertificateEncodingException(ex.toString());
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("X.509 Certificate Pair: [\n");
        if (this.forward != null) {
            sb.append("  Forward: ").append(this.forward).append("\n");
        }
        if (this.reverse != null) {
            sb.append("  Reverse: ").append(this.reverse).append("\n");
        }
        sb.append("]");
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    private void parse(DerValue val) throws IOException, CertificateException {
        if (val.tag != (byte) 48) {
            throw new IOException("Sequence tag missing for X509CertificatePair");
        }
        while (val.data != null && val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            switch ((short) ((byte) (opt.tag & 31))) {
                case (short) 0:
                    if (opt.isContextSpecific() && opt.isConstructed()) {
                        if (this.forward == null) {
                            this.forward = X509Factory.intern(new X509CertImpl(opt.data.getDerValue().toByteArray()));
                            break;
                        }
                        throw new IOException("Duplicate forward certificate in X509CertificatePair");
                    }
                case (short) 1:
                    if (opt.isContextSpecific() && opt.isConstructed()) {
                        if (this.reverse == null) {
                            this.reverse = X509Factory.intern(new X509CertImpl(opt.data.getDerValue().toByteArray()));
                            break;
                        }
                        throw new IOException("Duplicate reverse certificate in X509CertificatePair");
                    }
                default:
                    throw new IOException("Invalid encoding of X509CertificatePair");
            }
        }
        if (this.forward == null && this.reverse == null) {
            throw new CertificateException("at least one of certificate pair must be non-null");
        }
    }

    private void emit(DerOutputStream out) throws IOException, CertificateEncodingException {
        DerOutputStream tmp;
        DerOutputStream tagged = new DerOutputStream();
        if (this.forward != null) {
            tmp = new DerOutputStream();
            tmp.putDerValue(new DerValue(this.forward.getEncoded()));
            tagged.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 0), tmp);
        }
        if (this.reverse != null) {
            tmp = new DerOutputStream();
            tmp.putDerValue(new DerValue(this.reverse.getEncoded()));
            tagged.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 1), tmp);
        }
        out.write((byte) 48, tagged);
    }

    private void checkPair() throws CertificateException {
        if (this.forward != null && this.reverse != null) {
            X500Principal fwSubject = this.forward.getSubjectX500Principal();
            X500Principal fwIssuer = this.forward.getIssuerX500Principal();
            X500Principal rvSubject = this.reverse.getSubjectX500Principal();
            X500Principal rvIssuer = this.reverse.getIssuerX500Principal();
            if (fwIssuer.equals(rvSubject) && (rvIssuer.equals(fwSubject) ^ 1) == 0) {
                try {
                    PublicKey pk = this.reverse.getPublicKey();
                    if (!((pk instanceof DSAPublicKey) && ((DSAPublicKey) pk).getParams() == null)) {
                        this.forward.verify(pk);
                    }
                    pk = this.forward.getPublicKey();
                    if (!((pk instanceof DSAPublicKey) && ((DSAPublicKey) pk).getParams() == null)) {
                        this.reverse.verify(pk);
                    }
                    return;
                } catch (GeneralSecurityException e) {
                    throw new CertificateException("invalid signature: " + e.getMessage());
                }
            }
            throw new CertificateException("subject and issuer names in forward and reverse certificates do not match");
        }
    }
}

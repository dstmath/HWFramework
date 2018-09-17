package com.android.org.bouncycastle.jcajce.provider.asymmetric.x509;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERSet;
import com.android.org.bouncycastle.asn1.pkcs.ContentInfo;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.SignedData;
import com.android.org.bouncycastle.jcajce.util.BCJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.security.auth.x500.X500Principal;

public class PKIXCertPath extends CertPath {
    static final List certPathEncodings;
    private List certificates;
    private final JcaJceHelper helper = new BCJcaJceHelper();

    static {
        List encodings = new ArrayList();
        encodings.add("PkiPath");
        encodings.add("PKCS7");
        certPathEncodings = Collections.unmodifiableList(encodings);
    }

    private List sortCerts(List certs) {
        if (certs.size() < 2) {
            return certs;
        }
        int i;
        X500Principal issuer = ((X509Certificate) certs.get(0)).getIssuerX500Principal();
        boolean okay = true;
        for (i = 1; i != certs.size(); i++) {
            if (!issuer.equals(((X509Certificate) certs.get(i)).getSubjectX500Principal())) {
                okay = false;
                break;
            }
            issuer = ((X509Certificate) certs.get(i)).getIssuerX500Principal();
        }
        if (okay) {
            return certs;
        }
        int j;
        List retList = new ArrayList(certs.size());
        List orig = new ArrayList(certs);
        for (i = 0; i < certs.size(); i++) {
            X509Certificate cert = (X509Certificate) certs.get(i);
            boolean found = false;
            X500Principal subject = cert.getSubjectX500Principal();
            for (j = 0; j != certs.size(); j++) {
                if (((X509Certificate) certs.get(j)).getIssuerX500Principal().equals(subject)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                retList.add(cert);
                certs.remove(i);
            }
        }
        if (retList.size() > 1) {
            return orig;
        }
        for (i = 0; i != retList.size(); i++) {
            issuer = ((X509Certificate) retList.get(i)).getIssuerX500Principal();
            for (j = 0; j < certs.size(); j++) {
                X509Certificate c = (X509Certificate) certs.get(j);
                if (issuer.equals(c.getSubjectX500Principal())) {
                    retList.add(c);
                    certs.remove(j);
                    break;
                }
            }
        }
        if (certs.size() > 0) {
            return orig;
        }
        return retList;
    }

    PKIXCertPath(List certificates) {
        super("X.509");
        this.certificates = sortCerts(new ArrayList(certificates));
    }

    PKIXCertPath(InputStream inStream, String encoding) throws CertificateException {
        IOException ex;
        NoSuchProviderException ex2;
        super("X.509");
        try {
            CertificateFactory certFactory;
            if (encoding.equalsIgnoreCase("PkiPath")) {
                ASN1Primitive derObject = new ASN1InputStream(inStream).readObject();
                if (derObject instanceof ASN1Sequence) {
                    Enumeration e = ((ASN1Sequence) derObject).getObjects();
                    this.certificates = new ArrayList();
                    certFactory = this.helper.createCertificateFactory("X.509");
                    while (e.hasMoreElements()) {
                        this.certificates.add(0, certFactory.generateCertificate(new ByteArrayInputStream(((ASN1Encodable) e.nextElement()).toASN1Primitive().getEncoded(ASN1Encoding.DER))));
                    }
                } else {
                    throw new CertificateException("input stream does not contain a ASN1 SEQUENCE while reading PkiPath encoded data to load CertPath");
                }
            } else if (encoding.equalsIgnoreCase("PKCS7") || encoding.equalsIgnoreCase("PEM")) {
                InputStream inStream2 = new BufferedInputStream(inStream);
                try {
                    this.certificates = new ArrayList();
                    certFactory = this.helper.createCertificateFactory("X.509");
                    while (true) {
                        Certificate cert = certFactory.generateCertificate(inStream2);
                        if (cert == null) {
                            break;
                        }
                        this.certificates.add(cert);
                    }
                } catch (IOException e2) {
                    ex = e2;
                    inStream = inStream2;
                    throw new CertificateException("IOException throw while decoding CertPath:\n" + ex.toString());
                } catch (NoSuchProviderException e3) {
                    ex2 = e3;
                    inStream = inStream2;
                    throw new CertificateException("BouncyCastle provider not found while trying to get a CertificateFactory:\n" + ex2.toString());
                }
            } else {
                throw new CertificateException("unsupported encoding: " + encoding);
            }
            this.certificates = sortCerts(this.certificates);
        } catch (IOException e4) {
            ex = e4;
            throw new CertificateException("IOException throw while decoding CertPath:\n" + ex.toString());
        } catch (NoSuchProviderException e5) {
            ex2 = e5;
            throw new CertificateException("BouncyCastle provider not found while trying to get a CertificateFactory:\n" + ex2.toString());
        }
    }

    public Iterator getEncodings() {
        return certPathEncodings.iterator();
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        Iterator iter = getEncodings();
        if (iter.hasNext()) {
            Object enc = iter.next();
            if (enc instanceof String) {
                return getEncoded((String) enc);
            }
        }
        return null;
    }

    public byte[] getEncoded(String encoding) throws CertificateEncodingException {
        ASN1EncodableVector v;
        if (encoding.equalsIgnoreCase("PkiPath")) {
            v = new ASN1EncodableVector();
            ListIterator iter = this.certificates.listIterator(this.certificates.size());
            while (iter.hasPrevious()) {
                v.add(toASN1Object((X509Certificate) iter.previous()));
            }
            return toDEREncoded(new DERSequence(v));
        } else if (encoding.equalsIgnoreCase("PKCS7")) {
            ContentInfo encInfo = new ContentInfo(PKCSObjectIdentifiers.data, null);
            v = new ASN1EncodableVector();
            for (int i = 0; i != this.certificates.size(); i++) {
                v.add(toASN1Object((X509Certificate) this.certificates.get(i)));
            }
            return toDEREncoded(new ContentInfo(PKCSObjectIdentifiers.signedData, new SignedData(new ASN1Integer(1), new DERSet(), encInfo, new DERSet(v), null, new DERSet())));
        } else {
            throw new CertificateEncodingException("unsupported encoding: " + encoding);
        }
    }

    public List getCertificates() {
        return Collections.unmodifiableList(new ArrayList(this.certificates));
    }

    private ASN1Primitive toASN1Object(X509Certificate cert) throws CertificateEncodingException {
        try {
            return new ASN1InputStream(cert.getEncoded()).readObject();
        } catch (Exception e) {
            throw new CertificateEncodingException("Exception while encoding certificate: " + e.toString());
        }
    }

    private byte[] toDEREncoded(ASN1Encodable obj) throws CertificateEncodingException {
        try {
            return obj.toASN1Primitive().getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new CertificateEncodingException("Exception thrown: " + e);
        }
    }
}

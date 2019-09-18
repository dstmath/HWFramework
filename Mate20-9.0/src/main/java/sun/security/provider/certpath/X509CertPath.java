package sun.security.provider.certpath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class X509CertPath extends CertPath {
    private static final String COUNT_ENCODING = "count";
    private static final String PKCS7_ENCODING = "PKCS7";
    private static final String PKIPATH_ENCODING = "PkiPath";
    private static final Collection<String> encodingList;
    private static final long serialVersionUID = 4989800333263052980L;
    private List<X509Certificate> certs;

    static {
        List<String> list = new ArrayList<>(2);
        list.add(PKIPATH_ENCODING);
        list.add(PKCS7_ENCODING);
        encodingList = Collections.unmodifiableCollection(list);
    }

    public X509CertPath(List<? extends Certificate> certs2) throws CertificateException {
        super("X.509");
        for (Object obj : certs2) {
            if (!(obj instanceof X509Certificate)) {
                throw new CertificateException("List is not all X509Certificates: " + obj.getClass().getName());
            }
        }
        this.certs = Collections.unmodifiableList(new ArrayList(certs2));
    }

    public X509CertPath(InputStream is) throws CertificateException {
        this(is, PKIPATH_ENCODING);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
    public X509CertPath(InputStream is, String encoding) throws CertificateException {
        super("X.509");
        char c;
        int hashCode = encoding.hashCode();
        if (hashCode == 76183020) {
            if (encoding.equals(PKCS7_ENCODING)) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        } else if (hashCode == 1148619507 && encoding.equals(PKIPATH_ENCODING)) {
            c = 0;
            switch (c) {
                case 0:
                    this.certs = parsePKIPATH(is);
                    return;
                case 1:
                    this.certs = parsePKCS7(is);
                    return;
                default:
                    throw new CertificateException("unsupported encoding");
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
    }

    private static List<X509Certificate> parsePKIPATH(InputStream is) throws CertificateException {
        if (is != null) {
            try {
                DerValue[] seq = new DerInputStream(readAllBytes(is)).getSequence(3);
                if (seq.length == 0) {
                    return Collections.emptyList();
                }
                CertificateFactory certFac = CertificateFactory.getInstance("X.509");
                List<X509Certificate> certList = new ArrayList<>(seq.length);
                for (int i = seq.length - 1; i >= 0; i--) {
                    certList.add((X509Certificate) certFac.generateCertificate(new ByteArrayInputStream(seq[i].toByteArray())));
                }
                return Collections.unmodifiableList(certList);
            } catch (IOException ioe) {
                throw new CertificateException("IOException parsing PkiPath data: " + ioe, ioe);
            }
        } else {
            throw new CertificateException("input stream is null");
        }
    }

    private static List<X509Certificate> parsePKCS7(InputStream is) throws CertificateException {
        List<X509Certificate> certList;
        if (is != null) {
            try {
                if (!is.markSupported()) {
                    is = new ByteArrayInputStream(readAllBytes(is));
                }
                X509Certificate[] certArray = new PKCS7(is).getCertificates();
                if (certArray != null) {
                    certList = Arrays.asList(certArray);
                } else {
                    certList = new ArrayList<>(0);
                }
                return Collections.unmodifiableList(certList);
            } catch (IOException ioe) {
                throw new CertificateException("IOException parsing PKCS7 data: " + ioe);
            }
        } else {
            throw new CertificateException("input stream is null");
        }
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        while (true) {
            int read = is.read(buffer);
            int n = read;
            if (read == -1) {
                return baos.toByteArray();
            }
            baos.write(buffer, 0, n);
        }
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return encodePKIPATH();
    }

    private byte[] encodePKIPATH() throws CertificateEncodingException {
        ListIterator<X509Certificate> li = this.certs.listIterator(this.certs.size());
        try {
            DerOutputStream bytes = new DerOutputStream();
            while (li.hasPrevious()) {
                X509Certificate cert = li.previous();
                if (this.certs.lastIndexOf(cert) == this.certs.indexOf(cert)) {
                    bytes.write(cert.getEncoded());
                } else {
                    throw new CertificateEncodingException("Duplicate Certificate");
                }
            }
            DerOutputStream derout = new DerOutputStream();
            derout.write((byte) 48, bytes);
            return derout.toByteArray();
        } catch (IOException ioe) {
            throw new CertificateEncodingException("IOException encoding PkiPath data: " + ioe, ioe);
        }
    }

    private byte[] encodePKCS7() throws CertificateEncodingException {
        PKCS7 p7 = new PKCS7(new AlgorithmId[0], new ContentInfo(ContentInfo.DATA_OID, (DerValue) null), (X509Certificate[]) this.certs.toArray(new X509Certificate[this.certs.size()]), new SignerInfo[0]);
        DerOutputStream derout = new DerOutputStream();
        try {
            p7.encodeSignedData(derout);
            return derout.toByteArray();
        } catch (IOException ioe) {
            throw new CertificateEncodingException(ioe.getMessage());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0034  */
    public byte[] getEncoded(String encoding) throws CertificateEncodingException {
        char c;
        int hashCode = encoding.hashCode();
        if (hashCode == 76183020) {
            if (encoding.equals(PKCS7_ENCODING)) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        } else if (hashCode == 1148619507 && encoding.equals(PKIPATH_ENCODING)) {
            c = 0;
            switch (c) {
                case 0:
                    return encodePKIPATH();
                case 1:
                    return encodePKCS7();
                default:
                    throw new CertificateEncodingException("unsupported encoding");
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
        }
    }

    public static Iterator<String> getEncodingsStatic() {
        return encodingList.iterator();
    }

    public Iterator<String> getEncodings() {
        return getEncodingsStatic();
    }

    public List<X509Certificate> getCertificates() {
        return this.certs;
    }
}

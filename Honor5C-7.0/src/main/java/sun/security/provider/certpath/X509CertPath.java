package sun.security.provider.certpath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
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
import java.util.prefs.Preferences;
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
    private static final Collection<String> encodingList = null;
    private static final long serialVersionUID = 4989800333263052980L;
    private List<X509Certificate> certs;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.X509CertPath.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.X509CertPath.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.X509CertPath.<clinit>():void");
    }

    public X509CertPath(List<? extends Certificate> certs) throws CertificateException {
        super("X.509");
        for (Object obj : certs) {
            if (!(obj instanceof X509Certificate)) {
                throw new CertificateException("List is not all X509Certificates: " + obj.getClass().getName());
            }
        }
        this.certs = Collections.unmodifiableList(new ArrayList((Collection) certs));
    }

    public X509CertPath(InputStream is) throws CertificateException {
        this(is, PKIPATH_ENCODING);
    }

    public X509CertPath(InputStream is, String encoding) throws CertificateException {
        super("X.509");
        if (encoding.equals(PKIPATH_ENCODING)) {
            this.certs = parsePKIPATH(is);
        } else if (encoding.equals(PKCS7_ENCODING)) {
            this.certs = parsePKCS7(is);
        } else {
            throw new CertificateException("unsupported encoding");
        }
    }

    private static List<X509Certificate> parsePKIPATH(InputStream is) throws CertificateException {
        Object ioe;
        if (is == null) {
            throw new CertificateException("input stream is null");
        }
        try {
            DerValue[] seq = new DerInputStream(readAllBytes(is)).getSequence(3);
            if (seq.length == 0) {
                return Collections.emptyList();
            }
            CertificateFactory certFac = CertificateFactory.getInstance("X.509");
            List<X509Certificate> certList = new ArrayList(seq.length);
            try {
                for (int i = seq.length - 1; i >= 0; i--) {
                    certList.add((X509Certificate) certFac.generateCertificate(new ByteArrayInputStream(seq[i].toByteArray())));
                }
                return Collections.unmodifiableList(certList);
            } catch (IOException e) {
                ioe = e;
                throw new CertificateException("IOException parsing PkiPath data: " + ioe, ioe);
            }
        } catch (IOException e2) {
            ioe = e2;
            throw new CertificateException("IOException parsing PkiPath data: " + ioe, ioe);
        }
    }

    private static List<X509Certificate> parsePKCS7(InputStream is) throws CertificateException {
        if (is == null) {
            throw new CertificateException("input stream is null");
        }
        try {
            List<X509Certificate> certList;
            if (!is.markSupported()) {
                is = new ByteArrayInputStream(readAllBytes(is));
            }
            X509Certificate[] certArray = new PKCS7(is).getCertificates();
            if (certArray != null) {
                certList = Arrays.asList(certArray);
            } else {
                certList = new ArrayList(0);
            }
            return Collections.unmodifiableList(certList);
        } catch (Object ioe) {
            throw new CertificateException("IOException parsing PKCS7 data: " + ioe);
        }
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        byte[] buffer = new byte[Preferences.MAX_VALUE_LENGTH];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Modifier.STRICT);
        while (true) {
            int n = is.read(buffer);
            if (n == -1) {
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
                X509Certificate cert = (X509Certificate) li.previous();
                if (this.certs.lastIndexOf(cert) != this.certs.indexOf(cert)) {
                    throw new CertificateEncodingException("Duplicate Certificate");
                }
                bytes.write(cert.getEncoded());
            }
            DerOutputStream derout = new DerOutputStream();
            derout.write((byte) DerValue.tag_SequenceOf, bytes);
            return derout.toByteArray();
        } catch (Object ioe) {
            throw new CertificateEncodingException("IOException encoding PkiPath data: " + ioe, ioe);
        }
    }

    private byte[] encodePKCS7() throws CertificateEncodingException {
        PKCS7 p7 = new PKCS7(new AlgorithmId[0], new ContentInfo(ContentInfo.DATA_OID, null), (X509Certificate[]) this.certs.toArray(new X509Certificate[this.certs.size()]), new SignerInfo[0]);
        DerOutputStream derout = new DerOutputStream();
        try {
            p7.encodeSignedData(derout);
            return derout.toByteArray();
        } catch (IOException ioe) {
            throw new CertificateEncodingException(ioe.getMessage());
        }
    }

    public byte[] getEncoded(String encoding) throws CertificateEncodingException {
        if (encoding.equals(PKIPATH_ENCODING)) {
            return encodePKIPATH();
        }
        if (encoding.equals(PKCS7_ENCODING)) {
            return encodePKCS7();
        }
        throw new CertificateEncodingException("unsupported encoding");
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

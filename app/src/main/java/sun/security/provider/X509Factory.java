package sun.security.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import sun.misc.BASE64Decoder;
import sun.misc.FloatConsts;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;
import sun.security.provider.certpath.X509CertPath;
import sun.security.provider.certpath.X509CertificatePair;
import sun.security.util.Cache;
import sun.security.util.Cache.EqualByteArray;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;
import sun.util.locale.LanguageTag;

public class X509Factory extends CertificateFactorySpi {
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final int ENC_MAX_LENGTH = 4194304;
    public static final String END_CERT = "-----END CERTIFICATE-----";
    private static final Cache certCache = null;
    private static final Cache crlCache = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.X509Factory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.X509Factory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.X509Factory.<clinit>():void");
    }

    public Certificate engineGenerateCertificate(InputStream is) throws CertificateException {
        if (is == null) {
            certCache.clear();
            X509CertificatePair.clearCache();
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] encoding = readOneBlock(is);
            if (encoding != null) {
                X509CertImpl cert = (X509CertImpl) getFromCache(certCache, encoding);
                if (cert != null) {
                    return cert;
                }
                cert = new X509CertImpl(encoding);
                addToCache(certCache, cert.getEncodedInternal(), cert);
                return cert;
            }
            throw new IOException("Empty input");
        } catch (IOException ioe) {
            throw ((CertificateException) new CertificateException("Could not parse certificate: " + ioe.toString()).initCause(ioe));
        }
    }

    private static int readFully(InputStream in, ByteArrayOutputStream bout, int length) throws IOException {
        int read = 0;
        byte[] buffer = new byte[Modifier.STRICT];
        while (length > 0) {
            int n = in.read(buffer, 0, length < Modifier.STRICT ? length : Modifier.STRICT);
            if (n <= 0) {
                break;
            }
            bout.write(buffer, 0, n);
            read += n;
            length -= n;
        }
        return read;
    }

    public static synchronized X509CertImpl intern(X509Certificate c) throws CertificateException {
        synchronized (X509Factory.class) {
            if (c == null) {
                return null;
            }
            byte[] encoding;
            boolean isImpl = c instanceof X509CertImpl;
            if (isImpl) {
                encoding = ((X509CertImpl) c).getEncodedInternal();
            } else {
                encoding = c.getEncoded();
            }
            X509CertImpl newC = (X509CertImpl) getFromCache(certCache, encoding);
            if (newC != null) {
                return newC;
            }
            if (isImpl) {
                newC = (X509CertImpl) c;
            } else {
                newC = new X509CertImpl(encoding);
                encoding = newC.getEncodedInternal();
            }
            addToCache(certCache, encoding, newC);
            return newC;
        }
    }

    public static synchronized X509CRLImpl intern(X509CRL c) throws CRLException {
        synchronized (X509Factory.class) {
            if (c == null) {
                return null;
            }
            byte[] encoding;
            boolean isImpl = c instanceof X509CRLImpl;
            if (isImpl) {
                encoding = ((X509CRLImpl) c).getEncodedInternal();
            } else {
                encoding = c.getEncoded();
            }
            X509CRLImpl newC = (X509CRLImpl) getFromCache(crlCache, encoding);
            if (newC != null) {
                return newC;
            }
            if (isImpl) {
                newC = (X509CRLImpl) c;
            } else {
                newC = new X509CRLImpl(encoding);
                encoding = newC.getEncodedInternal();
            }
            addToCache(crlCache, encoding, newC);
            return newC;
        }
    }

    private static synchronized Object getFromCache(Cache cache, byte[] encoding) {
        Object value;
        synchronized (X509Factory.class) {
            value = cache.get(new EqualByteArray(encoding));
        }
        return value;
    }

    private static synchronized void addToCache(Cache cache, byte[] encoding, Object value) {
        synchronized (X509Factory.class) {
            if (encoding.length > ENC_MAX_LENGTH) {
                return;
            }
            cache.put(new EqualByteArray(encoding), value);
        }
    }

    public CertPath engineGenerateCertPath(InputStream inStream) throws CertificateException {
        if (inStream == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] encoding = readOneBlock(inStream);
            if (encoding != null) {
                return new X509CertPath(new ByteArrayInputStream(encoding));
            }
            throw new IOException("Empty input");
        } catch (IOException ioe) {
            throw new CertificateException(ioe.getMessage());
        }
    }

    public CertPath engineGenerateCertPath(InputStream inStream, String encoding) throws CertificateException {
        if (inStream == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] data = readOneBlock(inStream);
            if (data != null) {
                return new X509CertPath(new ByteArrayInputStream(data), encoding);
            }
            throw new IOException("Empty input");
        } catch (IOException ioe) {
            throw new CertificateException(ioe.getMessage());
        }
    }

    public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) throws CertificateException {
        return new X509CertPath((List) certificates);
    }

    public Iterator<String> engineGetCertPathEncodings() {
        return X509CertPath.getEncodingsStatic();
    }

    public Collection<? extends Certificate> engineGenerateCertificates(InputStream is) throws CertificateException {
        if (is == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            return parseX509orPKCS7Cert(is);
        } catch (Throwable ioe) {
            throw new CertificateException(ioe);
        }
    }

    public CRL engineGenerateCRL(InputStream is) throws CRLException {
        if (is == null) {
            crlCache.clear();
            throw new CRLException("Missing input stream");
        }
        try {
            byte[] encoding = readOneBlock(is);
            if (encoding != null) {
                X509CRLImpl crl = (X509CRLImpl) getFromCache(crlCache, encoding);
                if (crl != null) {
                    return crl;
                }
                crl = new X509CRLImpl(encoding);
                addToCache(crlCache, crl.getEncodedInternal(), crl);
                return crl;
            }
            throw new IOException("Empty input");
        } catch (IOException ioe) {
            throw new CRLException(ioe.getMessage());
        }
    }

    public Collection<? extends CRL> engineGenerateCRLs(InputStream is) throws CRLException {
        if (is == null) {
            throw new CRLException("Missing input stream");
        }
        try {
            return parseX509orPKCS7CRL(is);
        } catch (IOException ioe) {
            throw new CRLException(ioe.getMessage());
        }
    }

    private Collection<? extends Certificate> parseX509orPKCS7Cert(InputStream is) throws CertificateException, IOException {
        Collection<X509CertImpl> coll = new ArrayList();
        byte[] data = readOneBlock(is);
        if (data == null) {
            return new ArrayList(0);
        }
        try {
            X509Certificate[] certs = new PKCS7(data).getCertificates();
            if (certs != null) {
                return Arrays.asList(certs);
            }
            return new ArrayList(0);
        } catch (ParsingException e) {
            while (data != null) {
                coll.add(new X509CertImpl(data));
                data = readOneBlock(is);
            }
            return coll;
        }
    }

    private Collection<? extends CRL> parseX509orPKCS7CRL(InputStream is) throws CRLException, IOException {
        Collection<X509CRLImpl> coll = new ArrayList();
        byte[] data = readOneBlock(is);
        if (data == null) {
            return new ArrayList(0);
        }
        try {
            X509CRL[] crls = new PKCS7(data).getCRLs();
            if (crls != null) {
                return Arrays.asList(crls);
            }
            return new ArrayList(0);
        } catch (ParsingException e) {
            while (data != null) {
                coll.add(new X509CRLImpl(data));
                data = readOneBlock(is);
            }
            return coll;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] readOneBlock(InputStream is) throws IOException {
        int c = is.read();
        if (c == -1) {
            return null;
        }
        if (c == 48) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(Modifier.STRICT);
            bout.write(c);
            readBERInternal(is, bout, c);
            return bout.toByteArray();
        }
        int pos;
        char[] data = new char[Modifier.STRICT];
        int pos2 = 0;
        int hyphen = c == 45 ? 1 : 0;
        int last = c == 45 ? -1 : c;
        while (true) {
            int next = is.read();
            if (next == -1) {
                return null;
            }
            if (next == 45) {
                hyphen++;
            } else {
                hyphen = 0;
                last = next;
            }
            if (hyphen == 5) {
                if (!(last == -1 || last == 13)) {
                    if (last == 10) {
                        break;
                    }
                }
            }
        }
        StringBuffer header = new StringBuffer("-----");
        while (true) {
            next = is.read();
            if (next == -1) {
                break;
            }
            if (next == 10) {
                break;
            } else if (next == 13) {
                break;
            } else {
                header.append((char) next);
            }
            while (true) {
                next = is.read();
                if (next != -1) {
                    if (next != 45) {
                        break;
                    }
                    pos = pos2 + 1;
                    data[pos2] = (char) next;
                    if (pos >= data.length) {
                        data = Arrays.copyOf(data, data.length + Record.maxExpansion);
                    }
                    pos2 = pos;
                } else {
                    break;
                }
            }
            StringBuffer footer = new StringBuffer(LanguageTag.SEP);
            while (true) {
                next = is.read();
                if (next == -1 || next == end || next == 10) {
                    checkHeaderFooter(header.toString(), footer.toString());
                } else if (next != 13) {
                    footer.append((char) next);
                }
            }
            checkHeaderFooter(header.toString(), footer.toString());
            return new BASE64Decoder().decodeBuffer(new String(data, 0, pos2));
        }
        int end = 10;
        while (true) {
            next = is.read();
            if (next != -1) {
                break;
                throw new IOException("Incomplete data");
            } else if (next != 45) {
                break;
                StringBuffer footer2 = new StringBuffer(LanguageTag.SEP);
                while (true) {
                    next = is.read();
                    if (next == -1) {
                        break;
                    }
                    break;
                    checkHeaderFooter(header.toString(), footer2.toString());
                    return new BASE64Decoder().decodeBuffer(new String(data, 0, pos2));
                }
            } else {
                pos = pos2 + 1;
                data[pos2] = (char) next;
                if (pos >= data.length) {
                    data = Arrays.copyOf(data, data.length + Record.maxExpansion);
                }
                pos2 = pos;
            }
        }
    }

    private static void checkHeaderFooter(String header, String footer) throws IOException {
        if (header.length() < 16 || !header.startsWith("-----BEGIN ") || !header.endsWith("-----")) {
            throw new IOException("Illegal header: " + header);
        } else if (footer.length() < 14 || !footer.startsWith("-----END ") || !footer.endsWith("-----")) {
            throw new IOException("Illegal footer: " + footer);
        } else if (!header.substring(11, header.length() - 5).equals(footer.substring(9, footer.length() - 5))) {
            throw new IOException("Header and footer do not match: " + header + " " + footer);
        }
    }

    private static int readBERInternal(InputStream is, ByteArrayOutputStream bout, int tag) throws IOException {
        if (tag == -1) {
            tag = is.read();
            if (tag == -1) {
                throw new IOException("BER/DER tag info absent");
            } else if ((tag & 31) == 31) {
                throw new IOException("Multi octets tag not supported");
            } else {
                bout.write(tag);
            }
        }
        int n = is.read();
        if (n == -1) {
            throw new IOException("BER/DER length info ansent");
        }
        bout.write(n);
        if (n != Pattern.CANON_EQ) {
            int length;
            if (n < Pattern.CANON_EQ) {
                length = n;
            } else if (n == 129) {
                length = is.read();
                if (length == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(length);
            } else if (n == 130) {
                highByte = is.read();
                lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(highByte);
                bout.write(lowByte);
                length = (highByte << 8) | lowByte;
            } else if (n == 131) {
                highByte = is.read();
                midByte = is.read();
                lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(highByte);
                bout.write(midByte);
                bout.write(lowByte);
                length = ((highByte << 16) | (midByte << 8)) | lowByte;
            } else if (n == 132) {
                highByte = is.read();
                int nextByte = is.read();
                midByte = is.read();
                lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                } else if (highByte > FloatConsts.MAX_EXPONENT) {
                    throw new IOException("Invalid BER/DER data (a little huge?)");
                } else {
                    bout.write(highByte);
                    bout.write(nextByte);
                    bout.write(midByte);
                    bout.write(lowByte);
                    length = (((highByte << 24) | (nextByte << 16)) | (midByte << 8)) | lowByte;
                }
            } else {
                throw new IOException("Invalid BER/DER data (too huge?)");
            }
            if (readFully(is, bout, length) != length) {
                throw new IOException("Incomplete BER/DER data");
            }
        } else if ((tag & 32) != 32) {
            throw new IOException("Non constructed encoding must have definite length");
        } else {
            do {
            } while (readBERInternal(is, bout, -1) != 0);
        }
        return tag;
    }
}

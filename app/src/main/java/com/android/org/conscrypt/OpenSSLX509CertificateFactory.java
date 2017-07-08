package com.android.org.conscrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OpenSSLX509CertificateFactory extends CertificateFactorySpi {
    private static final byte[] PKCS7_MARKER = null;
    private static final int PUSHBACK_SIZE = 64;
    private Parser<OpenSSLX509Certificate> certificateParser;
    private Parser<OpenSSLX509CRL> crlParser;

    private static abstract class Parser<T> {
        protected abstract List<? extends T> fromPkcs7DerInputStream(InputStream inputStream) throws ParsingException;

        protected abstract List<? extends T> fromPkcs7PemInputStream(InputStream inputStream) throws ParsingException;

        protected abstract T fromX509DerInputStream(InputStream inputStream) throws ParsingException;

        protected abstract T fromX509PemInputStream(InputStream inputStream) throws ParsingException;

        private Parser() {
        }

        public T generateItem(InputStream inStream) throws ParsingException {
            if (inStream == null) {
                throw new ParsingException("inStream == null");
            }
            boolean markable = inStream.markSupported();
            if (markable) {
                inStream.mark(OpenSSLX509CertificateFactory.PKCS7_MARKER.length);
            }
            PushbackInputStream pbis = new PushbackInputStream(inStream, OpenSSLX509CertificateFactory.PUSHBACK_SIZE);
            try {
                byte[] buffer = new byte[OpenSSLX509CertificateFactory.PKCS7_MARKER.length];
                int len = pbis.read(buffer);
                if (len < 0) {
                    throw new ParsingException("inStream is empty");
                }
                pbis.unread(buffer, 0, len);
                if (buffer[0] == 45) {
                    if (len != OpenSSLX509CertificateFactory.PKCS7_MARKER.length || !Arrays.equals(OpenSSLX509CertificateFactory.PKCS7_MARKER, buffer)) {
                        return fromX509PemInputStream(pbis);
                    }
                    List<? extends T> items = fromPkcs7PemInputStream(pbis);
                    if (items.size() == 0) {
                        return null;
                    }
                    items.get(0);
                }
                if (buffer[4] != 6) {
                    return fromX509DerInputStream(pbis);
                }
                List<? extends T> certs = fromPkcs7DerInputStream(pbis);
                if (certs.size() == 0) {
                    return null;
                }
                return certs.get(0);
            } catch (Exception e) {
                if (markable) {
                    try {
                        inStream.reset();
                    } catch (IOException e2) {
                    }
                }
                throw new ParsingException(e);
            }
        }

        public Collection<? extends T> generateItems(InputStream inStream) throws ParsingException {
            if (inStream == null) {
                throw new ParsingException("inStream == null");
            }
            try {
                if (inStream.available() == 0) {
                    return Collections.emptyList();
                }
                boolean markable = inStream.markSupported();
                if (markable) {
                    inStream.mark(OpenSSLX509CertificateFactory.PUSHBACK_SIZE);
                }
                PushbackInputStream pbis = new PushbackInputStream(inStream, OpenSSLX509CertificateFactory.PUSHBACK_SIZE);
                try {
                    byte[] buffer = new byte[OpenSSLX509CertificateFactory.PKCS7_MARKER.length];
                    int len = pbis.read(buffer);
                    if (len < 0) {
                        throw new ParsingException("inStream is empty");
                    }
                    pbis.unread(buffer, 0, len);
                    if (len == OpenSSLX509CertificateFactory.PKCS7_MARKER.length && Arrays.equals(OpenSSLX509CertificateFactory.PKCS7_MARKER, buffer)) {
                        return fromPkcs7PemInputStream(pbis);
                    }
                    if (buffer[4] == 6) {
                        return fromPkcs7DerInputStream(pbis);
                    }
                    List<T> coll = new ArrayList();
                    T c;
                    do {
                        if (markable) {
                            inStream.mark(OpenSSLX509CertificateFactory.PUSHBACK_SIZE);
                        }
                        try {
                            c = generateItem(pbis);
                            coll.add(c);
                            continue;
                        } catch (ParsingException e) {
                            if (markable) {
                                try {
                                    inStream.reset();
                                } catch (IOException e2) {
                                }
                            }
                            c = null;
                            continue;
                        }
                    } while (c != null);
                    return coll;
                } catch (Exception e3) {
                    if (markable) {
                        try {
                            inStream.reset();
                        } catch (IOException e4) {
                        }
                    }
                    throw new ParsingException(e3);
                }
            } catch (IOException e5) {
                throw new ParsingException("Problem reading input stream", e5);
            }
        }
    }

    static class ParsingException extends Exception {
        private static final long serialVersionUID = 8390802697728301325L;

        public ParsingException(String message) {
            super(message);
        }

        public ParsingException(Exception cause) {
            super(cause);
        }

        public ParsingException(String message, Exception cause) {
            super(message, cause);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLX509CertificateFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLX509CertificateFactory.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLX509CertificateFactory.<clinit>():void");
    }

    public OpenSSLX509CertificateFactory() {
        this.certificateParser = new Parser<OpenSSLX509Certificate>() {
            public OpenSSLX509Certificate fromX509PemInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509Certificate.fromX509PemInputStream(is);
            }

            public OpenSSLX509Certificate fromX509DerInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509Certificate.fromX509DerInputStream(is);
            }

            public List<? extends OpenSSLX509Certificate> fromPkcs7PemInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509Certificate.fromPkcs7PemInputStream(is);
            }

            public List<? extends OpenSSLX509Certificate> fromPkcs7DerInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509Certificate.fromPkcs7DerInputStream(is);
            }
        };
        this.crlParser = new Parser<OpenSSLX509CRL>() {
            public OpenSSLX509CRL fromX509PemInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509CRL.fromX509PemInputStream(is);
            }

            public OpenSSLX509CRL fromX509DerInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509CRL.fromX509DerInputStream(is);
            }

            public List<? extends OpenSSLX509CRL> fromPkcs7PemInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509CRL.fromPkcs7PemInputStream(is);
            }

            public List<? extends OpenSSLX509CRL> fromPkcs7DerInputStream(InputStream is) throws ParsingException {
                return OpenSSLX509CRL.fromPkcs7DerInputStream(is);
            }
        };
    }

    public Certificate engineGenerateCertificate(InputStream inStream) throws CertificateException {
        try {
            return (Certificate) this.certificateParser.generateItem(inStream);
        } catch (ParsingException e) {
            throw new CertificateException(e);
        }
    }

    public Collection<? extends Certificate> engineGenerateCertificates(InputStream inStream) throws CertificateException {
        try {
            return this.certificateParser.generateItems(inStream);
        } catch (ParsingException e) {
            throw new CertificateException(e);
        }
    }

    public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
        try {
            return (CRL) this.crlParser.generateItem(inStream);
        } catch (ParsingException e) {
            throw new CRLException(e);
        }
    }

    public Collection<? extends CRL> engineGenerateCRLs(InputStream inStream) throws CRLException {
        if (inStream == null) {
            return Collections.emptyList();
        }
        try {
            return this.crlParser.generateItems(inStream);
        } catch (ParsingException e) {
            throw new CRLException(e);
        }
    }

    public Iterator<String> engineGetCertPathEncodings() {
        return OpenSSLX509CertPath.getEncodingsIterator();
    }

    public CertPath engineGenerateCertPath(InputStream inStream) throws CertificateException {
        return OpenSSLX509CertPath.fromEncoding(inStream);
    }

    public CertPath engineGenerateCertPath(InputStream inStream, String encoding) throws CertificateException {
        return OpenSSLX509CertPath.fromEncoding(inStream, encoding);
    }

    public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) throws CertificateException {
        List<X509Certificate> filtered = new ArrayList(certificates.size());
        int i = 0;
        while (i < certificates.size()) {
            Certificate c = (Certificate) certificates.get(i);
            if (c instanceof X509Certificate) {
                filtered.add((X509Certificate) c);
                i++;
            } else {
                throw new CertificateException("Certificate not X.509 type at index " + i);
            }
        }
        return new OpenSSLX509CertPath(filtered);
    }
}

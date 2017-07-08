package com.android.org.conscrypt;

import com.android.org.conscrypt.ct.CTConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.conscrypt.OpenSSLX509Certificate;

public class OpenSSLX509CertPath extends CertPath {
    private static final /* synthetic */ int[] -com-android-org-conscrypt-OpenSSLX509CertPath$EncodingSwitchesValues = null;
    private static final List<String> ALL_ENCODINGS = null;
    private static final Encoding DEFAULT_ENCODING = null;
    private static final byte[] PKCS7_MARKER = null;
    private static final int PUSHBACK_SIZE = 64;
    private final List<? extends X509Certificate> mCertificates;

    private enum Encoding {
        ;
        
        private final String apiName;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLX509CertPath.Encoding.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLX509CertPath.Encoding.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLX509CertPath.Encoding.<clinit>():void");
        }

        private Encoding(String apiName) {
            this.apiName = apiName;
        }

        static Encoding findByApiName(String apiName) throws CertificateEncodingException {
            for (Encoding element : values()) {
                if (element.apiName.equals(apiName)) {
                    return element;
                }
            }
            return null;
        }
    }

    private static /* synthetic */ int[] -getcom-android-org-conscrypt-OpenSSLX509CertPath$EncodingSwitchesValues() {
        if (-com-android-org-conscrypt-OpenSSLX509CertPath$EncodingSwitchesValues != null) {
            return -com-android-org-conscrypt-OpenSSLX509CertPath$EncodingSwitchesValues;
        }
        int[] iArr = new int[Encoding.values().length];
        try {
            iArr[Encoding.PKCS7.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Encoding.PKI_PATH.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -com-android-org-conscrypt-OpenSSLX509CertPath$EncodingSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.OpenSSLX509CertPath.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.OpenSSLX509CertPath.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.OpenSSLX509CertPath.<clinit>():void");
    }

    static Iterator<String> getEncodingsIterator() {
        return ALL_ENCODINGS.iterator();
    }

    protected OpenSSLX509CertPath(List<? extends X509Certificate> certificates) {
        super("X.509");
        this.mCertificates = certificates;
    }

    public List<? extends Certificate> getCertificates() {
        return Collections.unmodifiableList(this.mCertificates);
    }

    private byte[] getEncoded(Encoding encoding) throws CertificateEncodingException {
        OpenSSLX509Certificate[] certs = new OpenSSLX509Certificate[this.mCertificates.size()];
        long[] certRefs = new long[certs.length];
        int i = 0;
        for (int j = certs.length - 1; j >= 0; j--) {
            X509Certificate cert = (X509Certificate) this.mCertificates.get(i);
            if (cert instanceof OpenSSLX509Certificate) {
                certs[j] = (OpenSSLX509Certificate) cert;
            } else {
                certs[j] = OpenSSLX509Certificate.fromX509Der(cert.getEncoded());
            }
            certRefs[j] = certs[j].getContext();
            i++;
        }
        switch (-getcom-android-org-conscrypt-OpenSSLX509CertPath$EncodingSwitchesValues()[encoding.ordinal()]) {
            case CTConstants.VERSION_LENGTH /*1*/:
                return NativeCrypto.i2d_PKCS7(certRefs);
            case CTConstants.SIGNATURE_LENGTH_BYTES /*2*/:
                return NativeCrypto.ASN1_seq_pack_X509(certRefs);
            default:
                throw new CertificateEncodingException("Unknown encoding");
        }
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return getEncoded(DEFAULT_ENCODING);
    }

    public byte[] getEncoded(String encoding) throws CertificateEncodingException {
        Encoding enc = Encoding.findByApiName(encoding);
        if (enc != null) {
            return getEncoded(enc);
        }
        throw new CertificateEncodingException("Invalid encoding: " + encoding);
    }

    public Iterator<String> getEncodings() {
        return getEncodingsIterator();
    }

    private static CertPath fromPkiPathEncoding(InputStream inStream) throws CertificateException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(inStream, true);
        boolean markable = inStream.markSupported();
        if (markable) {
            inStream.mark(PUSHBACK_SIZE);
        }
        try {
            long[] certRefs = NativeCrypto.ASN1_seq_unpack_X509_bio(bis.getBioContext());
            bis.release();
            if (certRefs == null) {
                return new OpenSSLX509CertPath(Collections.emptyList());
            }
            List<OpenSSLX509Certificate> certs = new ArrayList(certRefs.length);
            for (int i = certRefs.length - 1; i >= 0; i--) {
                if (certRefs[i] != 0) {
                    certs.add(new OpenSSLX509Certificate(certRefs[i]));
                }
            }
            return new OpenSSLX509CertPath(certs);
        } catch (Exception e) {
            if (markable) {
                try {
                    inStream.reset();
                } catch (IOException e2) {
                }
            }
            throw new CertificateException(e);
        } catch (Throwable th) {
            bis.release();
        }
    }

    private static CertPath fromPkcs7Encoding(InputStream inStream) throws CertificateException {
        if (inStream != null) {
            try {
                if (inStream.available() != 0) {
                    boolean markable = inStream.markSupported();
                    if (markable) {
                        inStream.mark(PUSHBACK_SIZE);
                    }
                    PushbackInputStream pbis = new PushbackInputStream(inStream, PUSHBACK_SIZE);
                    try {
                        byte[] buffer = new byte[PKCS7_MARKER.length];
                        int len = pbis.read(buffer);
                        if (len < 0) {
                            throw new ParsingException("inStream is empty");
                        }
                        pbis.unread(buffer, 0, len);
                        if (len == PKCS7_MARKER.length && Arrays.equals(PKCS7_MARKER, buffer)) {
                            return new OpenSSLX509CertPath(OpenSSLX509Certificate.fromPkcs7PemInputStream(pbis));
                        }
                        return new OpenSSLX509CertPath(OpenSSLX509Certificate.fromPkcs7DerInputStream(pbis));
                    } catch (Exception e) {
                        if (markable) {
                            try {
                                inStream.reset();
                            } catch (IOException e2) {
                            }
                        }
                        throw new CertificateException(e);
                    }
                }
            } catch (IOException e3) {
                throw new CertificateException("Problem reading input stream", e3);
            }
        }
        return new OpenSSLX509CertPath(Collections.emptyList());
    }

    private static CertPath fromEncoding(InputStream inStream, Encoding encoding) throws CertificateException {
        switch (-getcom-android-org-conscrypt-OpenSSLX509CertPath$EncodingSwitchesValues()[encoding.ordinal()]) {
            case CTConstants.VERSION_LENGTH /*1*/:
                return fromPkcs7Encoding(inStream);
            case CTConstants.SIGNATURE_LENGTH_BYTES /*2*/:
                return fromPkiPathEncoding(inStream);
            default:
                throw new CertificateEncodingException("Unknown encoding");
        }
    }

    public static CertPath fromEncoding(InputStream inStream, String encoding) throws CertificateException {
        if (inStream == null) {
            throw new CertificateException("inStream == null");
        }
        Encoding enc = Encoding.findByApiName(encoding);
        if (enc != null) {
            return fromEncoding(inStream, enc);
        }
        throw new CertificateException("Invalid encoding: " + encoding);
    }

    public static CertPath fromEncoding(InputStream inStream) throws CertificateException {
        if (inStream != null) {
            return fromEncoding(inStream, DEFAULT_ENCODING);
        }
        throw new CertificateException("inStream == null");
    }
}

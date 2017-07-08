package com.android.org.conscrypt.ct;

import com.android.org.conscrypt.OpenSSLX509Certificate;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CertificateEntry {
    private final byte[] certificate;
    private final LogEntryType entryType;
    private final byte[] issuerKeyHash;

    public enum LogEntryType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.ct.CertificateEntry.LogEntryType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.ct.CertificateEntry.LogEntryType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.ct.CertificateEntry.LogEntryType.<clinit>():void");
        }
    }

    private CertificateEntry(LogEntryType entryType, byte[] certificate, byte[] issuerKeyHash) {
        if (entryType == LogEntryType.PRECERT_ENTRY && issuerKeyHash == null) {
            throw new IllegalArgumentException("issuerKeyHash missing for precert entry.");
        } else if (entryType == LogEntryType.X509_ENTRY && issuerKeyHash != null) {
            throw new IllegalArgumentException("unexpected issuerKeyHash for X509 entry.");
        } else if (issuerKeyHash == null || issuerKeyHash.length == 32) {
            this.entryType = entryType;
            this.issuerKeyHash = issuerKeyHash;
            this.certificate = certificate;
        } else {
            throw new IllegalArgumentException("issuerKeyHash must be 32 bytes long");
        }
    }

    public static CertificateEntry createForPrecertificate(byte[] tbsCertificate, byte[] issuerKeyHash) {
        return new CertificateEntry(LogEntryType.PRECERT_ENTRY, tbsCertificate, issuerKeyHash);
    }

    public static CertificateEntry createForPrecertificate(OpenSSLX509Certificate leaf, OpenSSLX509Certificate issuer) throws CertificateException {
        try {
            if (leaf.getNonCriticalExtensionOIDs().contains(CTConstants.X509_SCT_LIST_OID)) {
                byte[] tbs = leaf.withDeletedExtension(CTConstants.X509_SCT_LIST_OID).getTBSCertificate();
                byte[] issuerKey = issuer.getPublicKey().getEncoded();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(issuerKey);
                return createForPrecertificate(tbs, md.digest());
            }
            throw new CertificateException("Certificate does not contain embedded signed timestamps");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static CertificateEntry createForX509Certificate(byte[] x509Certificate) {
        return new CertificateEntry(LogEntryType.X509_ENTRY, x509Certificate, null);
    }

    public static CertificateEntry createForX509Certificate(X509Certificate cert) throws CertificateEncodingException {
        return createForX509Certificate(cert.getEncoded());
    }

    public LogEntryType getEntryType() {
        return this.entryType;
    }

    public byte[] getCertificate() {
        return this.certificate;
    }

    public byte[] getIssuerKeyHash() {
        return this.issuerKeyHash;
    }

    public void encode(OutputStream output) throws SerializationException {
        Serialization.writeNumber(output, (long) this.entryType.ordinal(), 2);
        if (this.entryType == LogEntryType.PRECERT_ENTRY) {
            Serialization.writeFixedBytes(output, this.issuerKeyHash);
        }
        Serialization.writeVariableBytes(output, this.certificate, 3);
    }
}

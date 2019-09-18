package com.android.org.conscrypt.ct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SignedCertificateTimestamp {
    private final byte[] extensions;
    private final byte[] logId;
    private final Origin origin;
    private final DigitallySigned signature;
    private final long timestamp;
    private final Version version;

    public enum Origin {
        EMBEDDED,
        TLS_EXTENSION,
        OCSP_RESPONSE
    }

    public enum SignatureType {
        CERTIFICATE_TIMESTAMP,
        TREE_HASH
    }

    public enum Version {
        V1
    }

    public SignedCertificateTimestamp(Version version2, byte[] logId2, long timestamp2, byte[] extensions2, DigitallySigned signature2, Origin origin2) {
        this.version = version2;
        this.logId = logId2;
        this.timestamp = timestamp2;
        this.extensions = extensions2;
        this.signature = signature2;
        this.origin = origin2;
    }

    public Version getVersion() {
        return this.version;
    }

    public byte[] getLogID() {
        return this.logId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public byte[] getExtensions() {
        return this.extensions;
    }

    public DigitallySigned getSignature() {
        return this.signature;
    }

    public Origin getOrigin() {
        return this.origin;
    }

    public static SignedCertificateTimestamp decode(InputStream input, Origin origin2) throws SerializationException {
        int version2 = Serialization.readNumber(input, 1);
        if (version2 == Version.V1.ordinal()) {
            SignedCertificateTimestamp signedCertificateTimestamp = new SignedCertificateTimestamp(Version.V1, Serialization.readFixedBytes(input, 32), Serialization.readLong(input, 8), Serialization.readVariableBytes(input, 2), DigitallySigned.decode(input), origin2);
            return signedCertificateTimestamp;
        }
        throw new SerializationException("Unsupported SCT version " + version2);
    }

    public static SignedCertificateTimestamp decode(byte[] input, Origin origin2) throws SerializationException {
        return decode((InputStream) new ByteArrayInputStream(input), origin2);
    }

    public void encodeTBS(OutputStream output, CertificateEntry certEntry) throws SerializationException {
        Serialization.writeNumber(output, (long) this.version.ordinal(), 1);
        Serialization.writeNumber(output, (long) SignatureType.CERTIFICATE_TIMESTAMP.ordinal(), 1);
        Serialization.writeNumber(output, this.timestamp, 8);
        certEntry.encode(output);
        Serialization.writeVariableBytes(output, this.extensions, 2);
    }

    public byte[] encodeTBS(CertificateEntry certEntry) throws SerializationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        encodeTBS(output, certEntry);
        return output.toByteArray();
    }
}

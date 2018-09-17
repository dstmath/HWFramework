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

    public SignedCertificateTimestamp(Version version, byte[] logId, long timestamp, byte[] extensions, DigitallySigned signature, Origin origin) {
        this.version = version;
        this.logId = logId;
        this.timestamp = timestamp;
        this.extensions = extensions;
        this.signature = signature;
        this.origin = origin;
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

    public static SignedCertificateTimestamp decode(InputStream input, Origin origin) throws SerializationException {
        int version = Serialization.readNumber(input, 1);
        if (version == Version.V1.ordinal()) {
            return new SignedCertificateTimestamp(Version.V1, Serialization.readFixedBytes(input, 32), Serialization.readLong(input, 8), Serialization.readVariableBytes(input, 2), DigitallySigned.decode(input), origin);
        }
        throw new SerializationException("Unsupported SCT version " + version);
    }

    public static SignedCertificateTimestamp decode(byte[] input, Origin origin) throws SerializationException {
        return decode(new ByteArrayInputStream(input), origin);
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

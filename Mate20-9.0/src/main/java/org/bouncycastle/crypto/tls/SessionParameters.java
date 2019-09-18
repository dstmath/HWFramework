package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import org.bouncycastle.util.Arrays;

public final class SessionParameters {
    private int cipherSuite;
    private short compressionAlgorithm;
    private byte[] encodedServerExtensions;
    private byte[] masterSecret;
    private Certificate peerCertificate;
    private byte[] pskIdentity;
    private byte[] srpIdentity;

    public static final class Builder {
        private int cipherSuite = -1;
        private short compressionAlgorithm = -1;
        private byte[] encodedServerExtensions = null;
        private byte[] masterSecret = null;
        private Certificate peerCertificate = null;
        private byte[] pskIdentity = null;
        private byte[] srpIdentity = null;

        private void validate(boolean z, String str) {
            if (!z) {
                throw new IllegalStateException("Required session parameter '" + str + "' not configured");
            }
        }

        public SessionParameters build() {
            boolean z = false;
            validate(this.cipherSuite >= 0, "cipherSuite");
            validate(this.compressionAlgorithm >= 0, "compressionAlgorithm");
            if (this.masterSecret != null) {
                z = true;
            }
            validate(z, "masterSecret");
            SessionParameters sessionParameters = new SessionParameters(this.cipherSuite, this.compressionAlgorithm, this.masterSecret, this.peerCertificate, this.pskIdentity, this.srpIdentity, this.encodedServerExtensions);
            return sessionParameters;
        }

        public Builder setCipherSuite(int i) {
            this.cipherSuite = i;
            return this;
        }

        public Builder setCompressionAlgorithm(short s) {
            this.compressionAlgorithm = s;
            return this;
        }

        public Builder setMasterSecret(byte[] bArr) {
            this.masterSecret = bArr;
            return this;
        }

        public Builder setPSKIdentity(byte[] bArr) {
            this.pskIdentity = bArr;
            return this;
        }

        public Builder setPeerCertificate(Certificate certificate) {
            this.peerCertificate = certificate;
            return this;
        }

        public Builder setPskIdentity(byte[] bArr) {
            this.pskIdentity = bArr;
            return this;
        }

        public Builder setSRPIdentity(byte[] bArr) {
            this.srpIdentity = bArr;
            return this;
        }

        public Builder setServerExtensions(Hashtable hashtable) throws IOException {
            byte[] byteArray;
            if (hashtable == null) {
                byteArray = null;
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                TlsProtocol.writeExtensions(byteArrayOutputStream, hashtable);
                byteArray = byteArrayOutputStream.toByteArray();
            }
            this.encodedServerExtensions = byteArray;
            return this;
        }
    }

    private SessionParameters(int i, short s, byte[] bArr, Certificate certificate, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        this.pskIdentity = null;
        this.srpIdentity = null;
        this.cipherSuite = i;
        this.compressionAlgorithm = s;
        this.masterSecret = Arrays.clone(bArr);
        this.peerCertificate = certificate;
        this.pskIdentity = Arrays.clone(bArr2);
        this.srpIdentity = Arrays.clone(bArr3);
        this.encodedServerExtensions = bArr4;
    }

    public void clear() {
        if (this.masterSecret != null) {
            Arrays.fill(this.masterSecret, (byte) 0);
        }
    }

    public SessionParameters copy() {
        SessionParameters sessionParameters = new SessionParameters(this.cipherSuite, this.compressionAlgorithm, this.masterSecret, this.peerCertificate, this.pskIdentity, this.srpIdentity, this.encodedServerExtensions);
        return sessionParameters;
    }

    public int getCipherSuite() {
        return this.cipherSuite;
    }

    public short getCompressionAlgorithm() {
        return this.compressionAlgorithm;
    }

    public byte[] getMasterSecret() {
        return this.masterSecret;
    }

    public byte[] getPSKIdentity() {
        return this.pskIdentity;
    }

    public Certificate getPeerCertificate() {
        return this.peerCertificate;
    }

    public byte[] getPskIdentity() {
        return this.pskIdentity;
    }

    public byte[] getSRPIdentity() {
        return this.srpIdentity;
    }

    public Hashtable readServerExtensions() throws IOException {
        if (this.encodedServerExtensions == null) {
            return null;
        }
        return TlsProtocol.readExtensions(new ByteArrayInputStream(this.encodedServerExtensions));
    }
}

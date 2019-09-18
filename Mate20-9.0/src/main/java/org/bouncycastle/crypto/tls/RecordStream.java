package org.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.util.io.SimpleOutputStream;

class RecordStream {
    private static int DEFAULT_PLAINTEXT_LIMIT = 16384;
    static final int TLS_HEADER_LENGTH_OFFSET = 3;
    static final int TLS_HEADER_SIZE = 5;
    static final int TLS_HEADER_TYPE_OFFSET = 0;
    static final int TLS_HEADER_VERSION_OFFSET = 1;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private int ciphertextLimit;
    private int compressedLimit;
    private TlsProtocol handler;
    /* access modifiers changed from: private */
    public TlsHandshakeHash handshakeHash = null;
    private SimpleOutputStream handshakeHashUpdater = new SimpleOutputStream() {
        public void write(byte[] bArr, int i, int i2) throws IOException {
            RecordStream.this.handshakeHash.update(bArr, i, i2);
        }
    };
    private InputStream input;
    private OutputStream output;
    private TlsCipher pendingCipher = null;
    private TlsCompression pendingCompression = null;
    private int plaintextLimit;
    private TlsCipher readCipher = null;
    private TlsCompression readCompression = null;
    private SequenceNumber readSeqNo = new SequenceNumber();
    private ProtocolVersion readVersion = null;
    private boolean restrictReadVersion = true;
    private TlsCipher writeCipher = null;
    private TlsCompression writeCompression = null;
    private SequenceNumber writeSeqNo = new SequenceNumber();
    private ProtocolVersion writeVersion = null;

    private static class SequenceNumber {
        private boolean exhausted;
        private long value;

        private SequenceNumber() {
            this.value = 0;
            this.exhausted = false;
        }

        /* access modifiers changed from: package-private */
        public synchronized long nextValue(short s) throws TlsFatalAlert {
            long j;
            if (!this.exhausted) {
                j = this.value;
                long j2 = this.value + 1;
                this.value = j2;
                if (j2 == 0) {
                    this.exhausted = true;
                }
            } else {
                throw new TlsFatalAlert(s);
            }
            return j;
        }
    }

    RecordStream(TlsProtocol tlsProtocol, InputStream inputStream, OutputStream outputStream) {
        this.handler = tlsProtocol;
        this.input = inputStream;
        this.output = outputStream;
        this.readCompression = new TlsNullCompression();
        this.writeCompression = this.readCompression;
    }

    private static void checkLength(int i, int i2, short s) throws IOException {
        if (i > i2) {
            throw new TlsFatalAlert(s);
        }
    }

    private static void checkType(short s, short s2) throws IOException {
        switch (s) {
            case 20:
            case 21:
            case 22:
            case 23:
                return;
            default:
                throw new TlsFatalAlert(s2);
        }
    }

    private byte[] getBufferContents() {
        byte[] byteArray = this.buffer.toByteArray();
        this.buffer.reset();
        return byteArray;
    }

    /* access modifiers changed from: package-private */
    public void checkRecordHeader(byte[] bArr) throws IOException {
        checkType(TlsUtils.readUint8(bArr, 0), 10);
        if (this.restrictReadVersion) {
            ProtocolVersion readVersion2 = TlsUtils.readVersion(bArr, 1);
            if (this.readVersion != null && !readVersion2.equals(this.readVersion)) {
                throw new TlsFatalAlert(47);
            }
        } else if ((TlsUtils.readVersionRaw(bArr, 1) & -256) != 768) {
            throw new TlsFatalAlert(47);
        }
        checkLength(TlsUtils.readUint16(bArr, 3), this.ciphertextLimit, 22);
    }

    /* access modifiers changed from: package-private */
    public byte[] decodeAndVerify(short s, InputStream inputStream, int i) throws IOException {
        byte[] readFully = TlsUtils.readFully(i, inputStream);
        byte[] decodeCiphertext = this.readCipher.decodeCiphertext(this.readSeqNo.nextValue(10), s, readFully, 0, readFully.length);
        checkLength(decodeCiphertext.length, this.compressedLimit, 22);
        OutputStream decompress = this.readCompression.decompress(this.buffer);
        if (decompress != this.buffer) {
            decompress.write(decodeCiphertext, 0, decodeCiphertext.length);
            decompress.flush();
            decodeCiphertext = getBufferContents();
        }
        checkLength(decodeCiphertext.length, this.plaintextLimit, 30);
        if (decodeCiphertext.length >= 1 || s == 23) {
            return decodeCiphertext;
        }
        throw new TlsFatalAlert(47);
    }

    /* access modifiers changed from: package-private */
    public void finaliseHandshake() throws IOException {
        if (this.readCompression == this.pendingCompression && this.writeCompression == this.pendingCompression && this.readCipher == this.pendingCipher && this.writeCipher == this.pendingCipher) {
            this.pendingCompression = null;
            this.pendingCipher = null;
            return;
        }
        throw new TlsFatalAlert(40);
    }

    /* access modifiers changed from: package-private */
    public void flush() throws IOException {
        this.output.flush();
    }

    /* access modifiers changed from: package-private */
    public TlsHandshakeHash getHandshakeHash() {
        return this.handshakeHash;
    }

    /* access modifiers changed from: package-private */
    public OutputStream getHandshakeHashUpdater() {
        return this.handshakeHashUpdater;
    }

    /* access modifiers changed from: package-private */
    public int getPlaintextLimit() {
        return this.plaintextLimit;
    }

    /* access modifiers changed from: package-private */
    public ProtocolVersion getReadVersion() {
        return this.readVersion;
    }

    /* access modifiers changed from: package-private */
    public void init(TlsContext tlsContext) {
        this.readCipher = new TlsNullCipher(tlsContext);
        this.writeCipher = this.readCipher;
        this.handshakeHash = new DeferredHash();
        this.handshakeHash.init(tlsContext);
        setPlaintextLimit(DEFAULT_PLAINTEXT_LIMIT);
    }

    /* access modifiers changed from: package-private */
    public void notifyHelloComplete() {
        this.handshakeHash = this.handshakeHash.notifyPRFDetermined();
    }

    /* access modifiers changed from: package-private */
    public TlsHandshakeHash prepareToFinish() {
        TlsHandshakeHash tlsHandshakeHash = this.handshakeHash;
        this.handshakeHash = this.handshakeHash.stopTracking();
        return tlsHandshakeHash;
    }

    /* access modifiers changed from: package-private */
    public boolean readRecord() throws IOException {
        byte[] readAllOrNothing = TlsUtils.readAllOrNothing(5, this.input);
        if (readAllOrNothing == null) {
            return false;
        }
        short readUint8 = TlsUtils.readUint8(readAllOrNothing, 0);
        checkType(readUint8, 10);
        if (this.restrictReadVersion) {
            ProtocolVersion readVersion2 = TlsUtils.readVersion(readAllOrNothing, 1);
            if (this.readVersion == null) {
                this.readVersion = readVersion2;
            } else if (!readVersion2.equals(this.readVersion)) {
                throw new TlsFatalAlert(47);
            }
        } else if ((TlsUtils.readVersionRaw(readAllOrNothing, 1) & -256) != 768) {
            throw new TlsFatalAlert(47);
        }
        int readUint16 = TlsUtils.readUint16(readAllOrNothing, 3);
        checkLength(readUint16, this.ciphertextLimit, 22);
        byte[] decodeAndVerify = decodeAndVerify(readUint8, this.input, readUint16);
        this.handler.processRecord(readUint8, decodeAndVerify, 0, decodeAndVerify.length);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void receivedReadCipherSpec() throws IOException {
        if (this.pendingCompression == null || this.pendingCipher == null) {
            throw new TlsFatalAlert(40);
        }
        this.readCompression = this.pendingCompression;
        this.readCipher = this.pendingCipher;
        this.readSeqNo = new SequenceNumber();
    }

    /* access modifiers changed from: package-private */
    public void safeClose() {
        try {
            this.input.close();
        } catch (IOException e) {
        }
        try {
            this.output.close();
        } catch (IOException e2) {
        }
    }

    /* access modifiers changed from: package-private */
    public void sentWriteCipherSpec() throws IOException {
        if (this.pendingCompression == null || this.pendingCipher == null) {
            throw new TlsFatalAlert(40);
        }
        this.writeCompression = this.pendingCompression;
        this.writeCipher = this.pendingCipher;
        this.writeSeqNo = new SequenceNumber();
    }

    /* access modifiers changed from: package-private */
    public void setPendingConnectionState(TlsCompression tlsCompression, TlsCipher tlsCipher) {
        this.pendingCompression = tlsCompression;
        this.pendingCipher = tlsCipher;
    }

    /* access modifiers changed from: package-private */
    public void setPlaintextLimit(int i) {
        this.plaintextLimit = i;
        this.compressedLimit = this.plaintextLimit + 1024;
        this.ciphertextLimit = this.compressedLimit + 1024;
    }

    /* access modifiers changed from: package-private */
    public void setReadVersion(ProtocolVersion protocolVersion) {
        this.readVersion = protocolVersion;
    }

    /* access modifiers changed from: package-private */
    public void setRestrictReadVersion(boolean z) {
        this.restrictReadVersion = z;
    }

    /* access modifiers changed from: package-private */
    public void setWriteVersion(ProtocolVersion protocolVersion) {
        this.writeVersion = protocolVersion;
    }

    /* access modifiers changed from: package-private */
    public void writeRecord(short s, byte[] bArr, int i, int i2) throws IOException {
        byte[] bufferContents;
        TlsCipher tlsCipher;
        int i3;
        int length;
        short s2;
        if (this.writeVersion != null) {
            checkType(s, 80);
            checkLength(i2, this.plaintextLimit, 80);
            if (i2 >= 1 || s == 23) {
                OutputStream compress = this.writeCompression.compress(this.buffer);
                long nextValue = this.writeSeqNo.nextValue(80);
                if (compress == this.buffer) {
                    tlsCipher = this.writeCipher;
                    s2 = s;
                    bufferContents = bArr;
                    i3 = i;
                    length = i2;
                } else {
                    compress.write(bArr, i, i2);
                    compress.flush();
                    bufferContents = getBufferContents();
                    checkLength(bufferContents.length, i2 + 1024, 80);
                    tlsCipher = this.writeCipher;
                    i3 = 0;
                    length = bufferContents.length;
                    s2 = s;
                }
                byte[] encodePlaintext = tlsCipher.encodePlaintext(nextValue, s2, bufferContents, i3, length);
                checkLength(encodePlaintext.length, this.ciphertextLimit, 80);
                byte[] bArr2 = new byte[(encodePlaintext.length + 5)];
                TlsUtils.writeUint8(s, bArr2, 0);
                TlsUtils.writeVersion(this.writeVersion, bArr2, 1);
                TlsUtils.writeUint16(encodePlaintext.length, bArr2, 3);
                System.arraycopy(encodePlaintext, 0, bArr2, 5, encodePlaintext.length);
                this.output.write(bArr2);
                this.output.flush();
                return;
            }
            throw new TlsFatalAlert(80);
        }
    }
}

package org.bouncycastle.crypto.tls;

import java.io.IOException;
import org.bouncycastle.asn1.cmc.BodyPartID;

class DTLSRecordLayer implements DatagramTransport {
    private static final int MAX_FRAGMENT_LENGTH = 16384;
    private static final int RECORD_HEADER_LENGTH = 13;
    private static final long RETRANSMIT_TIMEOUT = 240000;
    private static final long TCP_MSL = 120000;
    private volatile boolean closed = false;
    private final TlsContext context;
    private DTLSEpoch currentEpoch;
    private volatile boolean failed = false;
    private volatile boolean inHandshake;
    private final TlsPeer peer;
    private DTLSEpoch pendingEpoch;
    private volatile int plaintextLimit;
    private DTLSEpoch readEpoch;
    private volatile ProtocolVersion readVersion = null;
    private final ByteQueue recordQueue = new ByteQueue();
    private DTLSHandshakeRetransmit retransmit = null;
    private DTLSEpoch retransmitEpoch = null;
    private long retransmitExpiry = 0;
    private final DatagramTransport transport;
    private DTLSEpoch writeEpoch;
    private volatile ProtocolVersion writeVersion = null;

    DTLSRecordLayer(DatagramTransport datagramTransport, TlsContext tlsContext, TlsPeer tlsPeer, short s) {
        this.transport = datagramTransport;
        this.context = tlsContext;
        this.peer = tlsPeer;
        this.inHandshake = true;
        this.currentEpoch = new DTLSEpoch(0, new TlsNullCipher(tlsContext));
        this.pendingEpoch = null;
        this.readEpoch = this.currentEpoch;
        this.writeEpoch = this.currentEpoch;
        setPlaintextLimit(16384);
    }

    private void closeTransport() {
        if (!this.closed) {
            try {
                if (!this.failed) {
                    warn(0, null);
                }
                this.transport.close();
            } catch (Exception e) {
            }
            this.closed = true;
        }
    }

    private static long getMacSequenceNumber(int i, long j) {
        return ((((long) i) & BodyPartID.bodyIdMax) << 48) | j;
    }

    private void raiseAlert(short s, short s2, String str, Throwable th) throws IOException {
        this.peer.notifyAlertRaised(s, s2, str, th);
        sendRecord(21, new byte[]{(byte) s, (byte) s2}, 0, 2);
    }

    private int receiveRecord(byte[] bArr, int i, int i2, int i3) throws IOException {
        int i4;
        if (this.recordQueue.available() > 0) {
            if (this.recordQueue.available() >= 13) {
                byte[] bArr2 = new byte[2];
                this.recordQueue.read(bArr2, 0, 2, 11);
                i4 = TlsUtils.readUint16(bArr2, 0);
            } else {
                i4 = 0;
            }
            int min = Math.min(this.recordQueue.available(), 13 + i4);
            this.recordQueue.removeData(bArr, i, min, 0);
            return min;
        }
        int receive = this.transport.receive(bArr, i, i2, i3);
        if (receive >= 13) {
            int readUint16 = TlsUtils.readUint16(bArr, i + 11) + 13;
            if (receive > readUint16) {
                this.recordQueue.addData(bArr, i + readUint16, receive - readUint16);
                receive = readUint16;
            }
        }
        return receive;
    }

    private void sendRecord(short s, byte[] bArr, int i, int i2) throws IOException {
        short s2 = s;
        int i3 = i2;
        if (this.writeVersion != null) {
            if (i3 > this.plaintextLimit) {
                throw new TlsFatalAlert(80);
            } else if (i3 >= 1 || s2 == 23) {
                int epoch = this.writeEpoch.getEpoch();
                long allocateSequenceNumber = this.writeEpoch.allocateSequenceNumber();
                byte[] encodePlaintext = this.writeEpoch.getCipher().encodePlaintext(getMacSequenceNumber(epoch, allocateSequenceNumber), s2, bArr, i, i3);
                byte[] bArr2 = new byte[(encodePlaintext.length + 13)];
                TlsUtils.writeUint8(s2, bArr2, 0);
                TlsUtils.writeVersion(this.writeVersion, bArr2, 1);
                TlsUtils.writeUint16(epoch, bArr2, 3);
                TlsUtils.writeUint48(allocateSequenceNumber, bArr2, 5);
                TlsUtils.writeUint16(encodePlaintext.length, bArr2, 11);
                System.arraycopy(encodePlaintext, 0, bArr2, 13, encodePlaintext.length);
                this.transport.send(bArr2, 0, bArr2.length);
            } else {
                throw new TlsFatalAlert(80);
            }
        }
    }

    public void close() throws IOException {
        if (!this.closed) {
            if (this.inHandshake) {
                warn(90, "User canceled handshake");
            }
            closeTransport();
        }
    }

    /* access modifiers changed from: package-private */
    public void fail(short s) {
        if (!this.closed) {
            try {
                raiseAlert(2, s, null, null);
            } catch (Exception e) {
            }
            this.failed = true;
            closeTransport();
        }
    }

    /* access modifiers changed from: package-private */
    public void failed() {
        if (!this.closed) {
            this.failed = true;
            closeTransport();
        }
    }

    /* access modifiers changed from: package-private */
    public int getReadEpoch() {
        return this.readEpoch.getEpoch();
    }

    /* access modifiers changed from: package-private */
    public ProtocolVersion getReadVersion() {
        return this.readVersion;
    }

    public int getReceiveLimit() throws IOException {
        return Math.min(this.plaintextLimit, this.readEpoch.getCipher().getPlaintextLimit(this.transport.getReceiveLimit() - 13));
    }

    public int getSendLimit() throws IOException {
        return Math.min(this.plaintextLimit, this.writeEpoch.getCipher().getPlaintextLimit(this.transport.getSendLimit() - 13));
    }

    /* access modifiers changed from: package-private */
    public void handshakeSuccessful(DTLSHandshakeRetransmit dTLSHandshakeRetransmit) {
        if (this.readEpoch == this.currentEpoch || this.writeEpoch == this.currentEpoch) {
            throw new IllegalStateException();
        }
        if (dTLSHandshakeRetransmit != null) {
            this.retransmit = dTLSHandshakeRetransmit;
            this.retransmitEpoch = this.currentEpoch;
            this.retransmitExpiry = System.currentTimeMillis() + RETRANSMIT_TIMEOUT;
        }
        this.inHandshake = false;
        this.currentEpoch = this.pendingEpoch;
        this.pendingEpoch = null;
    }

    /* access modifiers changed from: package-private */
    public void initPendingEpoch(TlsCipher tlsCipher) {
        if (this.pendingEpoch == null) {
            this.pendingEpoch = new DTLSEpoch(this.writeEpoch.getEpoch() + 1, tlsCipher);
            return;
        }
        throw new IllegalStateException();
    }

    /* JADX WARNING: type inference failed for: r1v0 */
    /* JADX WARNING: type inference failed for: r1v1, types: [org.bouncycastle.crypto.tls.DTLSEpoch, org.bouncycastle.crypto.tls.DTLSHandshakeRetransmit] */
    /* JADX WARNING: type inference failed for: r1v21 */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0077 A[Catch:{ IOException -> 0x0148 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0078 A[Catch:{ IOException -> 0x0148 }] */
    public int receive(byte[] bArr, int i, int i2, int i3) throws IOException {
        byte[] bArr2;
        boolean z;
        DTLSEpoch dTLSEpoch;
        byte[] decodeCiphertext;
        DTLSEpoch dTLSEpoch2;
        ? r1 = 0;
        byte[] bArr3 = null;
        while (true) {
            int min = Math.min(i2, getReceiveLimit()) + 13;
            if (bArr3 == null || bArr3.length < min) {
                bArr3 = new byte[min];
            }
            try {
                if (this.retransmit != null && System.currentTimeMillis() > this.retransmitExpiry) {
                    this.retransmit = r1;
                    this.retransmitEpoch = r1;
                }
                int receiveRecord = receiveRecord(bArr3, 0, min, i3);
                if (receiveRecord < 0) {
                    return receiveRecord;
                }
                if (receiveRecord >= 13) {
                    if (receiveRecord == TlsUtils.readUint16(bArr3, 11) + 13) {
                        short readUint8 = TlsUtils.readUint8(bArr3, 0);
                        switch (readUint8) {
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                                int readUint16 = TlsUtils.readUint16(bArr3, 3);
                                if (readUint16 == this.readEpoch.getEpoch()) {
                                    dTLSEpoch2 = this.readEpoch;
                                } else if (readUint8 == 22 && this.retransmitEpoch != null && readUint16 == this.retransmitEpoch.getEpoch()) {
                                    dTLSEpoch2 = this.retransmitEpoch;
                                } else {
                                    dTLSEpoch = r1;
                                    if (dTLSEpoch == null) {
                                        long readUint48 = TlsUtils.readUint48(bArr3, 5);
                                        if (!dTLSEpoch.getReplayWindow().shouldDiscard(readUint48)) {
                                            ProtocolVersion readVersion2 = TlsUtils.readVersion(bArr3, 1);
                                            if (readVersion2.isDTLS()) {
                                                if (this.readVersion == null || this.readVersion.equals(readVersion2)) {
                                                    ProtocolVersion protocolVersion = readVersion2;
                                                    long j = readUint48;
                                                    bArr2 = bArr3;
                                                    decodeCiphertext = dTLSEpoch.getCipher().decodeCiphertext(getMacSequenceNumber(dTLSEpoch.getEpoch(), readUint48), readUint8, bArr3, 13, receiveRecord - 13);
                                                    dTLSEpoch.getReplayWindow().reportAuthenticated(j);
                                                    if (decodeCiphertext.length <= this.plaintextLimit) {
                                                        if (this.readVersion == null) {
                                                            this.readVersion = protocolVersion;
                                                        }
                                                        switch (readUint8) {
                                                            case 20:
                                                                for (int i4 = 0; i4 < decodeCiphertext.length; i4++) {
                                                                    if (TlsUtils.readUint8(decodeCiphertext, i4) == 1) {
                                                                        if (this.pendingEpoch != null) {
                                                                            this.readEpoch = this.pendingEpoch;
                                                                        }
                                                                    }
                                                                }
                                                                break;
                                                            case 21:
                                                                if (decodeCiphertext.length == 2) {
                                                                    short s = (short) decodeCiphertext[0];
                                                                    short s2 = (short) decodeCiphertext[1];
                                                                    this.peer.notifyAlertReceived(s, s2);
                                                                    if (s != 2) {
                                                                        if (s2 == 0) {
                                                                            closeTransport();
                                                                            break;
                                                                        }
                                                                    } else {
                                                                        failed();
                                                                        throw new TlsFatalAlert(s2);
                                                                    }
                                                                }
                                                                break;
                                                            case 22:
                                                                if (!this.inHandshake) {
                                                                    if (this.retransmit != null) {
                                                                        this.retransmit.receivedHandshakeRecord(readUint16, decodeCiphertext, 0, decodeCiphertext.length);
                                                                        break;
                                                                    }
                                                                } else {
                                                                    break;
                                                                }
                                                                break;
                                                            case 23:
                                                                if (!this.inHandshake) {
                                                                    break;
                                                                } else {
                                                                    break;
                                                                }
                                                            case 24:
                                                                break;
                                                        }
                                                    }
                                                    byte[] bArr4 = bArr;
                                                    int i5 = i;
                                                    z = false;
                                                    continue;
                                                }
                                            }
                                        }
                                    }
                                }
                                dTLSEpoch = dTLSEpoch2;
                                if (dTLSEpoch == null) {
                                }
                                break;
                        }
                    }
                }
                int i6 = i;
                bArr2 = bArr3;
                byte[] bArr5 = bArr;
                z = r1;
                bArr3 = bArr2;
                r1 = z;
            } catch (IOException e) {
                throw e;
            }
        }
        if (!this.inHandshake && this.retransmit != null) {
            this.retransmit = null;
            this.retransmitEpoch = null;
        }
        System.arraycopy(decodeCiphertext, 0, bArr, i, decodeCiphertext.length);
        return decodeCiphertext.length;
    }

    /* access modifiers changed from: package-private */
    public void resetWriteEpoch() {
        this.writeEpoch = this.retransmitEpoch != null ? this.retransmitEpoch : this.currentEpoch;
    }

    public void send(byte[] bArr, int i, int i2) throws IOException {
        short s;
        if (this.inHandshake || this.writeEpoch == this.retransmitEpoch) {
            s = 22;
            if (TlsUtils.readUint8(bArr, i) == 20) {
                DTLSEpoch dTLSEpoch = null;
                if (this.inHandshake) {
                    dTLSEpoch = this.pendingEpoch;
                } else if (this.writeEpoch == this.retransmitEpoch) {
                    dTLSEpoch = this.currentEpoch;
                }
                if (dTLSEpoch != null) {
                    byte[] bArr2 = {1};
                    sendRecord(20, bArr2, 0, bArr2.length);
                    this.writeEpoch = dTLSEpoch;
                } else {
                    throw new IllegalStateException();
                }
            }
        } else {
            s = 23;
        }
        sendRecord(s, bArr, i, i2);
    }

    /* access modifiers changed from: package-private */
    public void setPlaintextLimit(int i) {
        this.plaintextLimit = i;
    }

    /* access modifiers changed from: package-private */
    public void setReadVersion(ProtocolVersion protocolVersion) {
        this.readVersion = protocolVersion;
    }

    /* access modifiers changed from: package-private */
    public void setWriteVersion(ProtocolVersion protocolVersion) {
        this.writeVersion = protocolVersion;
    }

    /* access modifiers changed from: package-private */
    public void warn(short s, String str) throws IOException {
        raiseAlert(1, s, str, null);
    }
}

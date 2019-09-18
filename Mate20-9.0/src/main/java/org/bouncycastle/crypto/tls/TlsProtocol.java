package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.crypto.tls.SessionParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;

public abstract class TlsProtocol {
    protected static final short ADS_MODE_0_N = 1;
    protected static final short ADS_MODE_0_N_FIRSTONLY = 2;
    protected static final short ADS_MODE_1_Nsub1 = 0;
    protected static final short CS_CERTIFICATE_REQUEST = 7;
    protected static final short CS_CERTIFICATE_STATUS = 5;
    protected static final short CS_CERTIFICATE_VERIFY = 12;
    protected static final short CS_CLIENT_CERTIFICATE = 10;
    protected static final short CS_CLIENT_FINISHED = 13;
    protected static final short CS_CLIENT_HELLO = 1;
    protected static final short CS_CLIENT_KEY_EXCHANGE = 11;
    protected static final short CS_CLIENT_SUPPLEMENTAL_DATA = 9;
    protected static final short CS_END = 16;
    protected static final short CS_SERVER_CERTIFICATE = 4;
    protected static final short CS_SERVER_FINISHED = 15;
    protected static final short CS_SERVER_HELLO = 2;
    protected static final short CS_SERVER_HELLO_DONE = 8;
    protected static final short CS_SERVER_KEY_EXCHANGE = 6;
    protected static final short CS_SERVER_SESSION_TICKET = 14;
    protected static final short CS_SERVER_SUPPLEMENTAL_DATA = 3;
    protected static final short CS_START = 0;
    protected static final Integer EXT_RenegotiationInfo = Integers.valueOf(65281);
    protected static final Integer EXT_SessionTicket = Integers.valueOf(35);
    private ByteQueue alertQueue;
    protected boolean allowCertificateStatus;
    private volatile boolean appDataReady;
    private volatile boolean appDataSplitEnabled;
    private volatile int appDataSplitMode;
    private ByteQueue applicationDataQueue;
    protected boolean blocking;
    protected Hashtable clientExtensions;
    private volatile boolean closed;
    protected short connection_state;
    protected boolean expectSessionTicket;
    private byte[] expected_verify_data;
    private volatile boolean failedWithError;
    private ByteQueue handshakeQueue;
    protected ByteQueueInputStream inputBuffers;
    protected int[] offeredCipherSuites;
    protected short[] offeredCompressionMethods;
    protected ByteQueueOutputStream outputBuffer;
    protected Certificate peerCertificate;
    protected boolean receivedChangeCipherSpec;
    RecordStream recordStream;
    protected boolean resumedSession;
    protected SecureRandom secureRandom;
    protected boolean secure_renegotiation;
    protected SecurityParameters securityParameters;
    protected Hashtable serverExtensions;
    protected SessionParameters sessionParameters;
    private TlsInputStream tlsInputStream;
    private TlsOutputStream tlsOutputStream;
    protected TlsSession tlsSession;

    class HandshakeMessage extends ByteArrayOutputStream {
        HandshakeMessage(TlsProtocol tlsProtocol, short s) throws IOException {
            this(s, 60);
        }

        HandshakeMessage(short s, int i) throws IOException {
            super(i + 4);
            TlsUtils.writeUint8(s, (OutputStream) this);
            this.count += 3;
        }

        /* access modifiers changed from: package-private */
        public void writeToRecordStream() throws IOException {
            int i = this.count - 4;
            TlsUtils.checkUint24(i);
            TlsUtils.writeUint24(i, this.buf, 1);
            TlsProtocol.this.writeHandshakeMessage(this.buf, 0, this.count);
            this.buf = null;
        }
    }

    public TlsProtocol(InputStream inputStream, OutputStream outputStream, SecureRandom secureRandom2) {
        this.applicationDataQueue = new ByteQueue(0);
        this.alertQueue = new ByteQueue(2);
        this.handshakeQueue = new ByteQueue(0);
        this.tlsInputStream = null;
        this.tlsOutputStream = null;
        this.closed = false;
        this.failedWithError = false;
        this.appDataReady = false;
        this.appDataSplitEnabled = true;
        this.appDataSplitMode = 0;
        this.expected_verify_data = null;
        this.tlsSession = null;
        this.sessionParameters = null;
        this.securityParameters = null;
        this.peerCertificate = null;
        this.offeredCipherSuites = null;
        this.offeredCompressionMethods = null;
        this.clientExtensions = null;
        this.serverExtensions = null;
        this.connection_state = 0;
        this.resumedSession = false;
        this.receivedChangeCipherSpec = false;
        this.secure_renegotiation = false;
        this.allowCertificateStatus = false;
        this.expectSessionTicket = false;
        this.blocking = true;
        this.recordStream = new RecordStream(this, inputStream, outputStream);
        this.secureRandom = secureRandom2;
    }

    public TlsProtocol(SecureRandom secureRandom2) {
        this.applicationDataQueue = new ByteQueue(0);
        this.alertQueue = new ByteQueue(2);
        this.handshakeQueue = new ByteQueue(0);
        this.tlsInputStream = null;
        this.tlsOutputStream = null;
        this.closed = false;
        this.failedWithError = false;
        this.appDataReady = false;
        this.appDataSplitEnabled = true;
        this.appDataSplitMode = 0;
        this.expected_verify_data = null;
        this.tlsSession = null;
        this.sessionParameters = null;
        this.securityParameters = null;
        this.peerCertificate = null;
        this.offeredCipherSuites = null;
        this.offeredCompressionMethods = null;
        this.clientExtensions = null;
        this.serverExtensions = null;
        this.connection_state = 0;
        this.resumedSession = false;
        this.receivedChangeCipherSpec = false;
        this.secure_renegotiation = false;
        this.allowCertificateStatus = false;
        this.expectSessionTicket = false;
        this.blocking = false;
        this.inputBuffers = new ByteQueueInputStream();
        this.outputBuffer = new ByteQueueOutputStream();
        this.recordStream = new RecordStream(this, this.inputBuffers, this.outputBuffer);
        this.secureRandom = secureRandom2;
    }

    protected static void assertEmpty(ByteArrayInputStream byteArrayInputStream) throws IOException {
        if (byteArrayInputStream.available() > 0) {
            throw new TlsFatalAlert(50);
        }
    }

    protected static byte[] createRandomBlock(boolean z, RandomGenerator randomGenerator) {
        byte[] bArr = new byte[32];
        randomGenerator.nextBytes(bArr);
        if (z) {
            TlsUtils.writeGMTUnixTime(bArr, 0);
        }
        return bArr;
    }

    protected static byte[] createRenegotiationInfo(byte[] bArr) throws IOException {
        return TlsUtils.encodeOpaque8(bArr);
    }

    protected static void establishMasterSecret(TlsContext tlsContext, TlsKeyExchange tlsKeyExchange) throws IOException {
        byte[] generatePremasterSecret = tlsKeyExchange.generatePremasterSecret();
        try {
            tlsContext.getSecurityParameters().masterSecret = TlsUtils.calculateMasterSecret(tlsContext, generatePremasterSecret);
        } finally {
            if (generatePremasterSecret != null) {
                Arrays.fill(generatePremasterSecret, (byte) 0);
            }
        }
    }

    protected static byte[] getCurrentPRFHash(TlsContext tlsContext, TlsHandshakeHash tlsHandshakeHash, byte[] bArr) {
        Digest forkPRFHash = tlsHandshakeHash.forkPRFHash();
        if (bArr != null && TlsUtils.isSSL(tlsContext)) {
            forkPRFHash.update(bArr, 0, bArr.length);
        }
        byte[] bArr2 = new byte[forkPRFHash.getDigestSize()];
        forkPRFHash.doFinal(bArr2, 0);
        return bArr2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002e, code lost:
        if (r4 == false) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0031, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0032, code lost:
        if (r4 == false) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0034, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003a, code lost:
        throw new org.bouncycastle.crypto.tls.TlsFatalAlert(47);
     */
    protected static int getPRFAlgorithm(TlsContext tlsContext, int i) throws IOException {
        boolean isTLSv12 = TlsUtils.isTLSv12(tlsContext);
        switch (i) {
            case 59:
            case 60:
            case 61:
            case CipherSuite.TLS_DH_DSS_WITH_AES_128_CBC_SHA256:
            case 63:
            case 64:
                break;
            default:
                switch (i) {
                    case 103:
                    case 104:
                    case CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA256:
                    case CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256:
                    case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256:
                    case CipherSuite.TLS_DH_anon_WITH_AES_128_CBC_SHA256:
                    case CipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA256:
                        break;
                    default:
                        switch (i) {
                            case CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_DH_RSA_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_DHE_DSS_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_DH_DSS_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_DH_anon_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_PSK_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_DHE_PSK_WITH_AES_128_GCM_SHA256:
                            case CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256:
                                break;
                            case CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_DH_RSA_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_DHE_DSS_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_DH_DSS_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_DH_anon_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_PSK_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_DHE_PSK_WITH_AES_256_GCM_SHA384:
                            case CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384:
                                break;
                            default:
                                switch (i) {
                                    case CipherSuite.TLS_RSA_PSK_WITH_NULL_SHA384:
                                        break;
                                    case CipherSuite.TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256:
                                    case CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256:
                                    case 188:
                                    case CipherSuite.TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256:
                                    case CipherSuite.TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256:
                                    case CipherSuite.TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256:
                                    case 192:
                                    case CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256:
                                    case CipherSuite.TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256:
                                    case CipherSuite.TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256:
                                    case CipherSuite.TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256:
                                    case CipherSuite.TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256:
                                        break;
                                    default:
                                        switch (i) {
                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256:
                                            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256:
                                            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256:
                                            case CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256:
                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256:
                                            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256:
                                            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256:
                                            case CipherSuite.TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256:
                                                break;
                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384:
                                            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384:
                                            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384:
                                            case CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384:
                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384:
                                            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384:
                                            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384:
                                            case CipherSuite.TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384:
                                                break;
                                            default:
                                                switch (i) {
                                                    case CipherSuite.TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_CBC_SHA256:
                                                    case CipherSuite.TLS_ECDH_ECDSA_WITH_CAMELLIA_128_CBC_SHA256:
                                                    case CipherSuite.TLS_ECDHE_RSA_WITH_CAMELLIA_128_CBC_SHA256:
                                                    case CipherSuite.TLS_ECDH_RSA_WITH_CAMELLIA_128_CBC_SHA256:
                                                    case CipherSuite.TLS_RSA_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_DHE_RSA_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_DH_RSA_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_DHE_DSS_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_DH_anon_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_ECDH_ECDSA_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_ECDHE_RSA_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_ECDH_RSA_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_PSK_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_DHE_PSK_WITH_CAMELLIA_128_GCM_SHA256:
                                                    case CipherSuite.TLS_RSA_PSK_WITH_CAMELLIA_128_GCM_SHA256:
                                                        break;
                                                    case CipherSuite.TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_CBC_SHA384:
                                                    case CipherSuite.TLS_ECDH_ECDSA_WITH_CAMELLIA_256_CBC_SHA384:
                                                    case CipherSuite.TLS_ECDHE_RSA_WITH_CAMELLIA_256_CBC_SHA384:
                                                    case CipherSuite.TLS_ECDH_RSA_WITH_CAMELLIA_256_CBC_SHA384:
                                                    case CipherSuite.TLS_RSA_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_DHE_RSA_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_DH_RSA_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_DHE_DSS_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_DH_anon_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_ECDH_ECDSA_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_ECDHE_RSA_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_ECDH_RSA_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_PSK_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_DHE_PSK_WITH_CAMELLIA_256_GCM_SHA384:
                                                    case CipherSuite.TLS_RSA_PSK_WITH_CAMELLIA_256_GCM_SHA384:
                                                        break;
                                                    default:
                                                        switch (i) {
                                                            case CipherSuite.TLS_ECDHE_PSK_WITH_CAMELLIA_256_CBC_SHA384:
                                                                break;
                                                            case CipherSuite.TLS_RSA_WITH_AES_128_CCM:
                                                            case CipherSuite.TLS_RSA_WITH_AES_256_CCM:
                                                            case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CCM:
                                                            case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CCM:
                                                            case CipherSuite.TLS_RSA_WITH_AES_128_CCM_8:
                                                            case CipherSuite.TLS_RSA_WITH_AES_256_CCM_8:
                                                            case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CCM_8:
                                                            case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CCM_8:
                                                            case CipherSuite.TLS_PSK_WITH_AES_128_CCM:
                                                            case CipherSuite.TLS_PSK_WITH_AES_256_CCM:
                                                            case CipherSuite.TLS_DHE_PSK_WITH_AES_128_CCM:
                                                            case CipherSuite.TLS_DHE_PSK_WITH_AES_256_CCM:
                                                            case CipherSuite.TLS_PSK_WITH_AES_128_CCM_8:
                                                            case CipherSuite.TLS_PSK_WITH_AES_256_CCM_8:
                                                            case CipherSuite.TLS_PSK_DHE_WITH_AES_128_CCM_8:
                                                            case CipherSuite.TLS_PSK_DHE_WITH_AES_256_CCM_8:
                                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM:
                                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CCM:
                                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8:
                                                            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CCM_8:
                                                                break;
                                                            default:
                                                                switch (i) {
                                                                    case CipherSuite.DRAFT_TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256:
                                                                    case CipherSuite.DRAFT_TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256:
                                                                    case CipherSuite.DRAFT_TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256:
                                                                    case CipherSuite.DRAFT_TLS_PSK_WITH_CHACHA20_POLY1305_SHA256:
                                                                    case CipherSuite.DRAFT_TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256:
                                                                    case CipherSuite.DRAFT_TLS_DHE_PSK_WITH_CHACHA20_POLY1305_SHA256:
                                                                    case CipherSuite.DRAFT_TLS_RSA_PSK_WITH_CHACHA20_POLY1305_SHA256:
                                                                        break;
                                                                    default:
                                                                        switch (i) {
                                                                            case CipherSuite.DRAFT_TLS_DHE_RSA_WITH_AES_128_OCB:
                                                                            case 65281:
                                                                            case 65282:
                                                                            case CipherSuite.DRAFT_TLS_ECDHE_RSA_WITH_AES_256_OCB:
                                                                            case CipherSuite.DRAFT_TLS_ECDHE_ECDSA_WITH_AES_128_OCB:
                                                                            case CipherSuite.DRAFT_TLS_ECDHE_ECDSA_WITH_AES_256_OCB:
                                                                                break;
                                                                            default:
                                                                                switch (i) {
                                                                                    case CipherSuite.DRAFT_TLS_PSK_WITH_AES_128_OCB:
                                                                                    case CipherSuite.DRAFT_TLS_PSK_WITH_AES_256_OCB:
                                                                                    case CipherSuite.DRAFT_TLS_DHE_PSK_WITH_AES_128_OCB:
                                                                                    case CipherSuite.DRAFT_TLS_DHE_PSK_WITH_AES_256_OCB:
                                                                                    case CipherSuite.DRAFT_TLS_ECDHE_PSK_WITH_AES_128_OCB:
                                                                                    case CipherSuite.DRAFT_TLS_ECDHE_PSK_WITH_AES_256_OCB:
                                                                                        break;
                                                                                    default:
                                                                                        switch (i) {
                                                                                            case CipherSuite.TLS_PSK_WITH_AES_256_CBC_SHA384:
                                                                                            case CipherSuite.TLS_PSK_WITH_NULL_SHA384:
                                                                                            case CipherSuite.TLS_DHE_PSK_WITH_AES_256_CBC_SHA384:
                                                                                            case CipherSuite.TLS_DHE_PSK_WITH_NULL_SHA384:
                                                                                            case CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384:
                                                                                            case CipherSuite.TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384:
                                                                                            case CipherSuite.TLS_ECDHE_PSK_WITH_NULL_SHA384:
                                                                                            case CipherSuite.TLS_PSK_WITH_CAMELLIA_256_CBC_SHA384:
                                                                                            case CipherSuite.TLS_DHE_PSK_WITH_CAMELLIA_256_CBC_SHA384:
                                                                                            case CipherSuite.TLS_RSA_PSK_WITH_CAMELLIA_256_CBC_SHA384:
                                                                                                break;
                                                                                            default:
                                                                                                return isTLSv12 ? 1 : 0;
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
        if (isTLSv12) {
            return 1;
        }
        throw new TlsFatalAlert(47);
    }

    private void processAlertQueue() throws IOException {
        while (this.alertQueue.available() >= 2) {
            byte[] removeData = this.alertQueue.removeData(2, 0);
            handleAlertMessage((short) removeData[0], (short) removeData[1]);
        }
    }

    private void processApplicationDataQueue() {
    }

    private void processChangeCipherSpec(byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        while (i3 < i2) {
            if (TlsUtils.readUint8(bArr, i + i3) != 1) {
                throw new TlsFatalAlert(50);
            } else if (this.receivedChangeCipherSpec || this.alertQueue.available() > 0 || this.handshakeQueue.available() > 0) {
                throw new TlsFatalAlert(10);
            } else {
                this.recordStream.receivedReadCipherSpec();
                this.receivedChangeCipherSpec = true;
                handleChangeCipherSpecMessage();
                i3++;
            }
        }
    }

    private void processHandshakeQueue(ByteQueue byteQueue) throws IOException {
        while (byteQueue.available() >= 4) {
            byte[] bArr = new byte[4];
            boolean z = false;
            byteQueue.read(bArr, 0, 4, 0);
            short readUint8 = TlsUtils.readUint8(bArr, 0);
            int readUint24 = TlsUtils.readUint24(bArr, 1);
            int i = 4 + readUint24;
            if (byteQueue.available() >= i) {
                if (this.connection_state == 16 || readUint8 == 20) {
                    z = true;
                }
                checkReceivedChangeCipherSpec(z);
                if (readUint8 != 0) {
                    if (readUint8 == 20) {
                        TlsContext context = getContext();
                        if (this.expected_verify_data == null && context.getSecurityParameters().getMasterSecret() != null) {
                            this.expected_verify_data = createVerifyData(!context.isServer());
                        }
                    }
                    byteQueue.copyTo(this.recordStream.getHandshakeHashUpdater(), i);
                }
                byteQueue.removeData(4);
                handleHandshakeMessage(readUint8, byteQueue.readFrom(readUint24));
            } else {
                return;
            }
        }
    }

    protected static Hashtable readExtensions(ByteArrayInputStream byteArrayInputStream) throws IOException {
        if (byteArrayInputStream.available() < 1) {
            return null;
        }
        byte[] readOpaque16 = TlsUtils.readOpaque16(byteArrayInputStream);
        assertEmpty(byteArrayInputStream);
        ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(readOpaque16);
        Hashtable hashtable = new Hashtable();
        while (byteArrayInputStream2.available() > 0) {
            if (hashtable.put(Integers.valueOf(TlsUtils.readUint16(byteArrayInputStream2)), TlsUtils.readOpaque16(byteArrayInputStream2)) != null) {
                throw new TlsFatalAlert(47);
            }
        }
        return hashtable;
    }

    protected static Vector readSupplementalDataMessage(ByteArrayInputStream byteArrayInputStream) throws IOException {
        byte[] readOpaque24 = TlsUtils.readOpaque24(byteArrayInputStream);
        assertEmpty(byteArrayInputStream);
        ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(readOpaque24);
        Vector vector = new Vector();
        while (byteArrayInputStream2.available() > 0) {
            vector.addElement(new SupplementalDataEntry(TlsUtils.readUint16(byteArrayInputStream2), TlsUtils.readOpaque16(byteArrayInputStream2)));
        }
        return vector;
    }

    protected static void writeExtensions(OutputStream outputStream, Hashtable hashtable) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        writeSelectedExtensions(byteArrayOutputStream, hashtable, true);
        writeSelectedExtensions(byteArrayOutputStream, hashtable, false);
        TlsUtils.writeOpaque16(byteArrayOutputStream.toByteArray(), outputStream);
    }

    protected static void writeSelectedExtensions(OutputStream outputStream, Hashtable hashtable, boolean z) throws IOException {
        Enumeration keys = hashtable.keys();
        while (keys.hasMoreElements()) {
            Integer num = (Integer) keys.nextElement();
            int intValue = num.intValue();
            byte[] bArr = (byte[]) hashtable.get(num);
            if (z == (bArr.length == 0)) {
                TlsUtils.checkUint16(intValue);
                TlsUtils.writeUint16(intValue, outputStream);
                TlsUtils.writeOpaque16(bArr, outputStream);
            }
        }
    }

    protected static void writeSupplementalData(OutputStream outputStream, Vector vector) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < vector.size(); i++) {
            SupplementalDataEntry supplementalDataEntry = (SupplementalDataEntry) vector.elementAt(i);
            int dataType = supplementalDataEntry.getDataType();
            TlsUtils.checkUint16(dataType);
            TlsUtils.writeUint16(dataType, byteArrayOutputStream);
            TlsUtils.writeOpaque16(supplementalDataEntry.getData(), byteArrayOutputStream);
        }
        TlsUtils.writeOpaque24(byteArrayOutputStream.toByteArray(), outputStream);
    }

    /* access modifiers changed from: protected */
    public int applicationDataAvailable() {
        return this.applicationDataQueue.available();
    }

    /* access modifiers changed from: protected */
    public void applyMaxFragmentLengthExtension() throws IOException {
        if (this.securityParameters.maxFragmentLength < 0) {
            return;
        }
        if (MaxFragmentLength.isValid(this.securityParameters.maxFragmentLength)) {
            this.recordStream.setPlaintextLimit(1 << (8 + this.securityParameters.maxFragmentLength));
            return;
        }
        throw new TlsFatalAlert(80);
    }

    /* access modifiers changed from: protected */
    public void blockForHandshake() throws IOException {
        if (this.blocking) {
            while (this.connection_state != 16) {
                if (!this.closed) {
                    safeReadRecord();
                } else {
                    throw new TlsFatalAlert(80);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkReceivedChangeCipherSpec(boolean z) throws IOException {
        if (z != this.receivedChangeCipherSpec) {
            throw new TlsFatalAlert(10);
        }
    }

    /* access modifiers changed from: protected */
    public void cleanupHandshake() {
        if (this.expected_verify_data != null) {
            Arrays.fill(this.expected_verify_data, (byte) 0);
            this.expected_verify_data = null;
        }
        this.securityParameters.clear();
        this.peerCertificate = null;
        this.offeredCipherSuites = null;
        this.offeredCompressionMethods = null;
        this.clientExtensions = null;
        this.serverExtensions = null;
        this.resumedSession = false;
        this.receivedChangeCipherSpec = false;
        this.secure_renegotiation = false;
        this.allowCertificateStatus = false;
        this.expectSessionTicket = false;
    }

    public void close() throws IOException {
        handleClose(true);
    }

    public void closeInput() throws IOException {
        if (this.blocking) {
            throw new IllegalStateException("Cannot use closeInput() in blocking mode!");
        } else if (!this.closed) {
            if (this.inputBuffers.available() > 0) {
                throw new EOFException();
            } else if (!this.appDataReady) {
                throw new TlsFatalAlert(40);
            } else {
                throw new TlsNoCloseNotifyException();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void completeHandshake() throws IOException {
        try {
            this.connection_state = 16;
            this.alertQueue.shrink();
            this.handshakeQueue.shrink();
            this.recordStream.finaliseHandshake();
            this.appDataSplitEnabled = !TlsUtils.isTLSv11(getContext());
            if (!this.appDataReady) {
                this.appDataReady = true;
                if (this.blocking) {
                    this.tlsInputStream = new TlsInputStream(this);
                    this.tlsOutputStream = new TlsOutputStream(this);
                }
            }
            if (this.tlsSession != null) {
                if (this.sessionParameters == null) {
                    this.sessionParameters = new SessionParameters.Builder().setCipherSuite(this.securityParameters.getCipherSuite()).setCompressionAlgorithm(this.securityParameters.getCompressionAlgorithm()).setMasterSecret(this.securityParameters.getMasterSecret()).setPeerCertificate(this.peerCertificate).setPSKIdentity(this.securityParameters.getPSKIdentity()).setSRPIdentity(this.securityParameters.getSRPIdentity()).setServerExtensions(this.serverExtensions).build();
                    this.tlsSession = new TlsSessionImpl(this.tlsSession.getSessionID(), this.sessionParameters);
                }
                getContextAdmin().setResumableSession(this.tlsSession);
            }
            getPeer().notifyHandshakeComplete();
        } finally {
            cleanupHandshake();
        }
    }

    /* access modifiers changed from: protected */
    public byte[] createVerifyData(boolean z) {
        TlsContext context = getContext();
        return TlsUtils.calculateVerifyData(context, z ? ExporterLabel.server_finished : ExporterLabel.client_finished, getCurrentPRFHash(context, this.recordStream.getHandshakeHash(), z ? TlsUtils.SSL_SERVER : TlsUtils.SSL_CLIENT));
    }

    /* access modifiers changed from: protected */
    public void flush() throws IOException {
        this.recordStream.flush();
    }

    public int getAvailableInputBytes() {
        if (!this.blocking) {
            return applicationDataAvailable();
        }
        throw new IllegalStateException("Cannot use getAvailableInputBytes() in blocking mode! Use getInputStream().available() instead.");
    }

    public int getAvailableOutputBytes() {
        if (!this.blocking) {
            return this.outputBuffer.getBuffer().available();
        }
        throw new IllegalStateException("Cannot use getAvailableOutputBytes() in blocking mode! Use getOutputStream() instead.");
    }

    /* access modifiers changed from: protected */
    public abstract TlsContext getContext();

    /* access modifiers changed from: package-private */
    public abstract AbstractTlsContext getContextAdmin();

    public InputStream getInputStream() {
        if (this.blocking) {
            return this.tlsInputStream;
        }
        throw new IllegalStateException("Cannot use InputStream in non-blocking mode! Use offerInput() instead.");
    }

    public OutputStream getOutputStream() {
        if (this.blocking) {
            return this.tlsOutputStream;
        }
        throw new IllegalStateException("Cannot use OutputStream in non-blocking mode! Use offerOutput() instead.");
    }

    /* access modifiers changed from: protected */
    public abstract TlsPeer getPeer();

    /* access modifiers changed from: protected */
    public void handleAlertMessage(short s, short s2) throws IOException {
        getPeer().notifyAlertReceived(s, s2);
        if (s == 1) {
            handleAlertWarningMessage(s2);
        } else {
            handleFailure();
            throw new TlsFatalAlertReceived(s2);
        }
    }

    /* access modifiers changed from: protected */
    public void handleAlertWarningMessage(short s) throws IOException {
        if (s != 0) {
            return;
        }
        if (this.appDataReady) {
            handleClose(false);
            return;
        }
        throw new TlsFatalAlert(40);
    }

    /* access modifiers changed from: protected */
    public void handleChangeCipherSpecMessage() throws IOException {
    }

    /* access modifiers changed from: protected */
    public void handleClose(boolean z) throws IOException {
        if (!this.closed) {
            this.closed = true;
            if (z && !this.appDataReady) {
                raiseAlertWarning(90, "User canceled handshake");
            }
            raiseAlertWarning(0, "Connection closed");
            this.recordStream.safeClose();
            if (!this.appDataReady) {
                cleanupHandshake();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleException(short s, String str, Throwable th) throws IOException {
        if (!this.closed) {
            raiseAlertFatal(s, str, th);
            handleFailure();
        }
    }

    /* access modifiers changed from: protected */
    public void handleFailure() {
        this.closed = true;
        this.failedWithError = true;
        invalidateSession();
        this.recordStream.safeClose();
        if (!this.appDataReady) {
            cleanupHandshake();
        }
    }

    /* access modifiers changed from: protected */
    public abstract void handleHandshakeMessage(short s, ByteArrayInputStream byteArrayInputStream) throws IOException;

    /* access modifiers changed from: protected */
    public void invalidateSession() {
        if (this.sessionParameters != null) {
            this.sessionParameters.clear();
            this.sessionParameters = null;
        }
        if (this.tlsSession != null) {
            this.tlsSession.invalidate();
            this.tlsSession = null;
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void offerInput(byte[] bArr) throws IOException {
        if (this.blocking) {
            throw new IllegalStateException("Cannot use offerInput() in blocking mode! Use getInputStream() instead.");
        } else if (!this.closed) {
            this.inputBuffers.addBytes(bArr);
            while (this.inputBuffers.available() >= 5) {
                byte[] bArr2 = new byte[5];
                this.inputBuffers.peek(bArr2);
                if (this.inputBuffers.available() < TlsUtils.readUint16(bArr2, 3) + 5) {
                    safeCheckRecordHeader(bArr2);
                    return;
                }
                safeReadRecord();
                if (this.closed) {
                    if (this.connection_state != 16) {
                        throw new TlsFatalAlert(80);
                    }
                    return;
                }
            }
        } else {
            throw new IOException("Connection is closed, cannot accept any more input");
        }
    }

    public void offerOutput(byte[] bArr, int i, int i2) throws IOException {
        if (this.blocking) {
            throw new IllegalStateException("Cannot use offerOutput() in blocking mode! Use getOutputStream() instead.");
        } else if (this.appDataReady) {
            writeData(bArr, i, i2);
        } else {
            throw new IOException("Application data cannot be sent until the handshake is complete!");
        }
    }

    /* access modifiers changed from: protected */
    public void processFinishedMessage(ByteArrayInputStream byteArrayInputStream) throws IOException {
        if (this.expected_verify_data != null) {
            byte[] readFully = TlsUtils.readFully(this.expected_verify_data.length, (InputStream) byteArrayInputStream);
            assertEmpty(byteArrayInputStream);
            if (!Arrays.constantTimeAreEqual(this.expected_verify_data, readFully)) {
                throw new TlsFatalAlert(51);
            }
            return;
        }
        throw new TlsFatalAlert(80);
    }

    /* access modifiers changed from: protected */
    public short processMaxFragmentLengthExtension(Hashtable hashtable, Hashtable hashtable2, short s) throws IOException {
        short maxFragmentLengthExtension = TlsExtensionsUtils.getMaxFragmentLengthExtension(hashtable2);
        if (maxFragmentLengthExtension < 0 || (MaxFragmentLength.isValid(maxFragmentLengthExtension) && (this.resumedSession || maxFragmentLengthExtension == TlsExtensionsUtils.getMaxFragmentLengthExtension(hashtable)))) {
            return maxFragmentLengthExtension;
        }
        throw new TlsFatalAlert(s);
    }

    /* access modifiers changed from: protected */
    public void processRecord(short s, byte[] bArr, int i, int i2) throws IOException {
        switch (s) {
            case 20:
                processChangeCipherSpec(bArr, i, i2);
                break;
            case 21:
                this.alertQueue.addData(bArr, i, i2);
                processAlertQueue();
                return;
            case 22:
                if (this.handshakeQueue.available() > 0) {
                    this.handshakeQueue.addData(bArr, i, i2);
                    processHandshakeQueue(this.handshakeQueue);
                    return;
                }
                ByteQueue byteQueue = new ByteQueue(bArr, i, i2);
                processHandshakeQueue(byteQueue);
                int available = byteQueue.available();
                if (available > 0) {
                    this.handshakeQueue.addData(bArr, (i + i2) - available, available);
                    return;
                }
                break;
            case 23:
                if (this.appDataReady) {
                    this.applicationDataQueue.addData(bArr, i, i2);
                    processApplicationDataQueue();
                    return;
                }
                throw new TlsFatalAlert(10);
            default:
                throw new TlsFatalAlert(80);
        }
    }

    /* access modifiers changed from: protected */
    public void raiseAlertFatal(short s, String str, Throwable th) throws IOException {
        getPeer().notifyAlertRaised(2, s, str, th);
        try {
            this.recordStream.writeRecord(21, new byte[]{2, (byte) s}, 0, 2);
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: protected */
    public void raiseAlertWarning(short s, String str) throws IOException {
        getPeer().notifyAlertRaised(1, s, str, null);
        safeWriteRecord(21, new byte[]{1, (byte) s}, 0, 2);
    }

    /* access modifiers changed from: protected */
    public int readApplicationData(byte[] bArr, int i, int i2) throws IOException {
        if (i2 < 1) {
            return 0;
        }
        while (this.applicationDataQueue.available() == 0) {
            if (!this.closed) {
                safeReadRecord();
            } else if (this.failedWithError) {
                throw new IOException("Cannot read application data on failed TLS connection");
            } else if (this.appDataReady) {
                return -1;
            } else {
                throw new IllegalStateException("Cannot read application data until initial handshake completed.");
            }
        }
        int min = Math.min(i2, this.applicationDataQueue.available());
        this.applicationDataQueue.removeData(bArr, i, min, 0);
        return min;
    }

    public int readInput(byte[] bArr, int i, int i2) {
        if (!this.blocking) {
            try {
                return readApplicationData(bArr, i, Math.min(i2, applicationDataAvailable()));
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        } else {
            throw new IllegalStateException("Cannot use readInput() in blocking mode! Use getInputStream() instead.");
        }
    }

    public int readOutput(byte[] bArr, int i, int i2) {
        if (!this.blocking) {
            int min = Math.min(getAvailableOutputBytes(), i2);
            this.outputBuffer.getBuffer().removeData(bArr, i, min, 0);
            return min;
        }
        throw new IllegalStateException("Cannot use readOutput() in blocking mode! Use getOutputStream() instead.");
    }

    /* access modifiers changed from: protected */
    public void refuseRenegotiation() throws IOException {
        if (!TlsUtils.isSSL(getContext())) {
            raiseAlertWarning(100, "Renegotiation not supported");
            return;
        }
        throw new TlsFatalAlert(40);
    }

    /* access modifiers changed from: protected */
    public void safeCheckRecordHeader(byte[] bArr) throws IOException {
        try {
            this.recordStream.checkRecordHeader(bArr);
        } catch (TlsFatalAlert e) {
            handleException(e.getAlertDescription(), "Failed to read record", e);
            throw e;
        } catch (IOException e2) {
            handleException(80, "Failed to read record", e2);
            throw e2;
        } catch (RuntimeException e3) {
            handleException(80, "Failed to read record", e3);
            throw new TlsFatalAlert(80, e3);
        }
    }

    /* access modifiers changed from: protected */
    public void safeReadRecord() throws IOException {
        try {
            if (!this.recordStream.readRecord()) {
                if (!this.appDataReady) {
                    throw new TlsFatalAlert(40);
                }
                handleFailure();
                throw new TlsNoCloseNotifyException();
            }
        } catch (TlsFatalAlertReceived e) {
            throw e;
        } catch (TlsFatalAlert e2) {
            handleException(e2.getAlertDescription(), "Failed to read record", e2);
            throw e2;
        } catch (IOException e3) {
            handleException(80, "Failed to read record", e3);
            throw e3;
        } catch (RuntimeException e4) {
            handleException(80, "Failed to read record", e4);
            throw new TlsFatalAlert(80, e4);
        }
    }

    /* access modifiers changed from: protected */
    public void safeWriteRecord(short s, byte[] bArr, int i, int i2) throws IOException {
        try {
            this.recordStream.writeRecord(s, bArr, i, i2);
        } catch (TlsFatalAlert e) {
            handleException(e.getAlertDescription(), "Failed to write record", e);
            throw e;
        } catch (IOException e2) {
            handleException(80, "Failed to write record", e2);
            throw e2;
        } catch (RuntimeException e3) {
            handleException(80, "Failed to write record", e3);
            throw new TlsFatalAlert(80, e3);
        }
    }

    /* access modifiers changed from: protected */
    public void sendCertificateMessage(Certificate certificate) throws IOException {
        if (certificate == null) {
            certificate = Certificate.EMPTY_CHAIN;
        }
        if (certificate.isEmpty() && !getContext().isServer()) {
            ProtocolVersion serverVersion = getContext().getServerVersion();
            if (serverVersion.isSSL()) {
                raiseAlertWarning(41, serverVersion.toString() + " client didn't provide credentials");
                return;
            }
        }
        HandshakeMessage handshakeMessage = new HandshakeMessage(this, 11);
        certificate.encode(handshakeMessage);
        handshakeMessage.writeToRecordStream();
    }

    /* access modifiers changed from: protected */
    public void sendChangeCipherSpecMessage() throws IOException {
        byte[] bArr = {1};
        safeWriteRecord(20, bArr, 0, bArr.length);
        this.recordStream.sentWriteCipherSpec();
    }

    /* access modifiers changed from: protected */
    public void sendFinishedMessage() throws IOException {
        byte[] createVerifyData = createVerifyData(getContext().isServer());
        HandshakeMessage handshakeMessage = new HandshakeMessage(20, createVerifyData.length);
        handshakeMessage.write(createVerifyData);
        handshakeMessage.writeToRecordStream();
    }

    /* access modifiers changed from: protected */
    public void sendSupplementalDataMessage(Vector vector) throws IOException {
        HandshakeMessage handshakeMessage = new HandshakeMessage(this, 23);
        writeSupplementalData(handshakeMessage, vector);
        handshakeMessage.writeToRecordStream();
    }

    /* access modifiers changed from: protected */
    public void setAppDataSplitMode(int i) {
        if (i < 0 || i > 2) {
            throw new IllegalArgumentException("Illegal appDataSplitMode mode: " + i);
        }
        this.appDataSplitMode = i;
    }

    /* access modifiers changed from: protected */
    public void writeData(byte[] bArr, int i, int i2) throws IOException {
        if (!this.closed) {
            while (i2 > 0) {
                if (this.appDataSplitEnabled) {
                    switch (this.appDataSplitMode) {
                        case 1:
                            break;
                        case 2:
                            this.appDataSplitEnabled = false;
                            break;
                        default:
                            safeWriteRecord(23, bArr, i, 1);
                            i++;
                            i2--;
                            break;
                    }
                    safeWriteRecord(23, TlsUtils.EMPTY_BYTES, 0, 0);
                }
                if (i2 > 0) {
                    int min = Math.min(i2, this.recordStream.getPlaintextLimit());
                    safeWriteRecord(23, bArr, i, min);
                    i += min;
                    i2 -= min;
                }
            }
            return;
        }
        throw new IOException("Cannot write application data on closed/failed TLS connection");
    }

    /* access modifiers changed from: protected */
    public void writeHandshakeMessage(byte[] bArr, int i, int i2) throws IOException {
        if (i2 >= 4) {
            if (TlsUtils.readUint8(bArr, i) != 0) {
                this.recordStream.getHandshakeHashUpdater().write(bArr, i, i2);
            }
            int i3 = 0;
            do {
                int min = Math.min(i2 - i3, this.recordStream.getPlaintextLimit());
                safeWriteRecord(22, bArr, i + i3, min);
                i3 += min;
            } while (i3 < i2);
            return;
        }
        throw new TlsFatalAlert(80);
    }
}

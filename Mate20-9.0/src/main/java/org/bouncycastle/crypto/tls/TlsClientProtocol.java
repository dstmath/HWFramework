package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.bouncycastle.crypto.tls.TlsProtocol;
import org.bouncycastle.util.Arrays;

public class TlsClientProtocol extends TlsProtocol {
    protected TlsAuthentication authentication = null;
    protected CertificateRequest certificateRequest = null;
    protected CertificateStatus certificateStatus = null;
    protected TlsKeyExchange keyExchange = null;
    protected byte[] selectedSessionID = null;
    protected TlsClient tlsClient = null;
    TlsClientContextImpl tlsClientContext = null;

    public TlsClientProtocol(InputStream inputStream, OutputStream outputStream, SecureRandom secureRandom) {
        super(inputStream, outputStream, secureRandom);
    }

    public TlsClientProtocol(SecureRandom secureRandom) {
        super(secureRandom);
    }

    /* access modifiers changed from: protected */
    public void cleanupHandshake() {
        super.cleanupHandshake();
        this.selectedSessionID = null;
        this.keyExchange = null;
        this.authentication = null;
        this.certificateStatus = null;
        this.certificateRequest = null;
    }

    public void connect(TlsClient tlsClient2) throws IOException {
        if (tlsClient2 == null) {
            throw new IllegalArgumentException("'tlsClient' cannot be null");
        } else if (this.tlsClient == null) {
            this.tlsClient = tlsClient2;
            this.securityParameters = new SecurityParameters();
            this.securityParameters.entity = 1;
            this.tlsClientContext = new TlsClientContextImpl(this.secureRandom, this.securityParameters);
            this.securityParameters.clientRandom = createRandomBlock(tlsClient2.shouldUseGMTUnixTime(), this.tlsClientContext.getNonceRandomGenerator());
            this.tlsClient.init(this.tlsClientContext);
            this.recordStream.init(this.tlsClientContext);
            TlsSession sessionToResume = tlsClient2.getSessionToResume();
            if (sessionToResume != null && sessionToResume.isResumable()) {
                SessionParameters exportSessionParameters = sessionToResume.exportSessionParameters();
                if (exportSessionParameters != null) {
                    this.tlsSession = sessionToResume;
                    this.sessionParameters = exportSessionParameters;
                }
            }
            sendClientHelloMessage();
            this.connection_state = 1;
            blockForHandshake();
        } else {
            throw new IllegalStateException("'connect' can only be called once");
        }
    }

    /* access modifiers changed from: protected */
    public TlsContext getContext() {
        return this.tlsClientContext;
    }

    /* access modifiers changed from: package-private */
    public AbstractTlsContext getContextAdmin() {
        return this.tlsClientContext;
    }

    /* access modifiers changed from: protected */
    public TlsPeer getPeer() {
        return this.tlsClient;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0080, code lost:
        r7.keyExchange.skipServerCredentials();
        r7.authentication = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0087, code lost:
        r7.keyExchange.skipServerKeyExchange();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008c, code lost:
        assertEmpty(r9);
        r7.connection_state = 8;
        r7.recordStream.getHandshakeHash().sealHashAlgorithms();
        r8 = r7.tlsClient.getClientSupplementalData();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a2, code lost:
        if (r8 == null) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00a4, code lost:
        sendSupplementalDataMessage(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00a7, code lost:
        r7.connection_state = 9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ad, code lost:
        if (r7.certificateRequest != null) goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00af, code lost:
        r7.keyExchange.skipClientCredentials();
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00b6, code lost:
        r8 = r7.authentication.getClientCredentials(r7.certificateRequest);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00be, code lost:
        if (r8 != null) goto L_0x00cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c0, code lost:
        r7.keyExchange.skipClientCredentials();
        r9 = org.bouncycastle.crypto.tls.Certificate.EMPTY_CHAIN;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00c7, code lost:
        sendCertificateMessage(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00cb, code lost:
        r7.keyExchange.processClientCredentials(r8);
        r9 = r8.getCertificate();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d5, code lost:
        r7.connection_state = 10;
        sendClientKeyExchangeMessage();
        r7.connection_state = 11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e6, code lost:
        if (org.bouncycastle.crypto.tls.TlsUtils.isSSL(getContext()) == false) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e8, code lost:
        establishMasterSecret(getContext(), r7.keyExchange);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f1, code lost:
        r9 = r7.recordStream.prepareToFinish();
        r7.securityParameters.sessionHash = getCurrentPRFHash(getContext(), r9, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x010b, code lost:
        if (org.bouncycastle.crypto.tls.TlsUtils.isSSL(getContext()) != false) goto L_0x0116;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x010d, code lost:
        establishMasterSecret(getContext(), r7.keyExchange);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0116, code lost:
        r7.recordStream.setPendingConnectionState(getPeer().getCompression(), getPeer().getCipher());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x012b, code lost:
        if (r8 == null) goto L_0x015c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x012f, code lost:
        if ((r8 instanceof org.bouncycastle.crypto.tls.TlsSignerCredentials) == false) goto L_0x015c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0131, code lost:
        r8 = (org.bouncycastle.crypto.tls.TlsSignerCredentials) r8;
        r0 = org.bouncycastle.crypto.tls.TlsUtils.getSignatureAndHashAlgorithm(getContext(), r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x013b, code lost:
        if (r0 != null) goto L_0x0144;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x013d, code lost:
        r9 = r7.securityParameters.getSessionHash();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0144, code lost:
        r9 = r9.getFinalHash(r0.getHash());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x014c, code lost:
        sendCertificateVerifyMessage(new org.bouncycastle.crypto.tls.DigitallySigned(r0, r8.generateCertificateSignature(r9)));
        r7.connection_state = 12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x015c, code lost:
        sendChangeCipherSpecMessage();
        sendFinishedMessage();
        r7.connection_state = 13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0164, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01b4, code lost:
        r7.keyExchange.skipServerCredentials();
        r7.authentication = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01bb, code lost:
        r7.keyExchange.processServerKeyExchange(r9);
        assertEmpty(r9);
        r8 = 6;
     */
    public void handleHandshakeMessage(short s, ByteArrayInputStream byteArrayInputStream) throws IOException {
        short s2;
        if (!this.resumedSession) {
            if (s == 0) {
                assertEmpty(byteArrayInputStream);
                if (this.connection_state == 16) {
                    refuseRenegotiation();
                }
            } else if (s != 2) {
                if (s != 4) {
                    if (s != 20) {
                        switch (s) {
                            case 11:
                                switch (this.connection_state) {
                                    case 2:
                                        handleSupplementalData(null);
                                        break;
                                    case 3:
                                        break;
                                    default:
                                        throw new TlsFatalAlert(10);
                                }
                                this.peerCertificate = Certificate.parse(byteArrayInputStream);
                                assertEmpty(byteArrayInputStream);
                                if (this.peerCertificate == null || this.peerCertificate.isEmpty()) {
                                    this.allowCertificateStatus = false;
                                }
                                this.keyExchange.processServerCertificate(this.peerCertificate);
                                this.authentication = this.tlsClient.getAuthentication();
                                this.authentication.notifyServerCertificate(this.peerCertificate);
                                this.connection_state = 4;
                                return;
                            case 12:
                                switch (this.connection_state) {
                                    case 2:
                                        handleSupplementalData(null);
                                        break;
                                    case 3:
                                        break;
                                    case 4:
                                    case 5:
                                        break;
                                    default:
                                        throw new TlsFatalAlert(10);
                                }
                            case 13:
                                switch (this.connection_state) {
                                    case 4:
                                    case 5:
                                        this.keyExchange.skipServerKeyExchange();
                                        break;
                                    case 6:
                                        break;
                                    default:
                                        throw new TlsFatalAlert(10);
                                }
                                if (this.authentication != null) {
                                    this.certificateRequest = CertificateRequest.parse(getContext(), byteArrayInputStream);
                                    assertEmpty(byteArrayInputStream);
                                    this.keyExchange.validateCertificateRequest(this.certificateRequest);
                                    TlsUtils.trackHashAlgorithms(this.recordStream.getHandshakeHash(), this.certificateRequest.getSupportedSignatureAlgorithms());
                                    s2 = 7;
                                    break;
                                } else {
                                    throw new TlsFatalAlert(40);
                                }
                            case 14:
                                switch (this.connection_state) {
                                    case 2:
                                        handleSupplementalData(null);
                                        break;
                                    case 3:
                                        break;
                                    case 4:
                                    case 5:
                                        break;
                                    case 6:
                                    case 7:
                                        break;
                                    default:
                                        throw new TlsFatalAlert(10);
                                }
                            default:
                                switch (s) {
                                    case 22:
                                        if (this.connection_state != 4) {
                                            throw new TlsFatalAlert(10);
                                        } else if (this.allowCertificateStatus) {
                                            this.certificateStatus = CertificateStatus.parse(byteArrayInputStream);
                                            assertEmpty(byteArrayInputStream);
                                            s2 = 5;
                                            break;
                                        } else {
                                            throw new TlsFatalAlert(10);
                                        }
                                    case 23:
                                        if (this.connection_state == 2) {
                                            handleSupplementalData(readSupplementalDataMessage(byteArrayInputStream));
                                            return;
                                        }
                                        throw new TlsFatalAlert(10);
                                    default:
                                        throw new TlsFatalAlert(10);
                                }
                        }
                    } else {
                        switch (this.connection_state) {
                            case 13:
                                if (this.expectSessionTicket) {
                                    throw new TlsFatalAlert(10);
                                }
                                break;
                            case 14:
                                break;
                            default:
                                throw new TlsFatalAlert(10);
                        }
                        processFinishedMessage(byteArrayInputStream);
                        this.connection_state = 15;
                        completeHandshake();
                        return;
                    }
                } else if (this.connection_state != 13) {
                    throw new TlsFatalAlert(10);
                } else if (this.expectSessionTicket) {
                    invalidateSession();
                    receiveNewSessionTicketMessage(byteArrayInputStream);
                    s2 = 14;
                } else {
                    throw new TlsFatalAlert(10);
                }
                this.connection_state = s2;
            } else if (this.connection_state == 1) {
                receiveServerHelloMessage(byteArrayInputStream);
                this.connection_state = 2;
                this.recordStream.notifyHelloComplete();
                applyMaxFragmentLengthExtension();
                if (this.resumedSession) {
                    this.securityParameters.masterSecret = Arrays.clone(this.sessionParameters.getMasterSecret());
                    this.recordStream.setPendingConnectionState(getPeer().getCompression(), getPeer().getCipher());
                    return;
                }
                invalidateSession();
                if (this.selectedSessionID.length > 0) {
                    this.tlsSession = new TlsSessionImpl(this.selectedSessionID, null);
                }
            } else {
                throw new TlsFatalAlert(10);
            }
        } else if (s == 20 && this.connection_state == 2) {
            processFinishedMessage(byteArrayInputStream);
            this.connection_state = 15;
            sendChangeCipherSpecMessage();
            sendFinishedMessage();
            this.connection_state = 13;
            completeHandshake();
        } else {
            throw new TlsFatalAlert(10);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSupplementalData(Vector vector) throws IOException {
        this.tlsClient.processServerSupplementalData(vector);
        this.connection_state = 3;
        this.keyExchange = this.tlsClient.getKeyExchange();
        this.keyExchange.init(getContext());
    }

    /* access modifiers changed from: protected */
    public void receiveNewSessionTicketMessage(ByteArrayInputStream byteArrayInputStream) throws IOException {
        NewSessionTicket parse = NewSessionTicket.parse(byteArrayInputStream);
        assertEmpty(byteArrayInputStream);
        this.tlsClient.notifyNewSessionTicket(parse);
    }

    /* access modifiers changed from: protected */
    public void receiveServerHelloMessage(ByteArrayInputStream byteArrayInputStream) throws IOException {
        ProtocolVersion readVersion = TlsUtils.readVersion(byteArrayInputStream);
        if (readVersion.isDTLS()) {
            throw new TlsFatalAlert(47);
        } else if (!readVersion.equals(this.recordStream.getReadVersion())) {
            throw new TlsFatalAlert(47);
        } else if (readVersion.isEqualOrEarlierVersionOf(getContext().getClientVersion())) {
            this.recordStream.setWriteVersion(readVersion);
            getContextAdmin().setServerVersion(readVersion);
            this.tlsClient.notifyServerVersion(readVersion);
            this.securityParameters.serverRandom = TlsUtils.readFully(32, (InputStream) byteArrayInputStream);
            this.selectedSessionID = TlsUtils.readOpaque8(byteArrayInputStream);
            if (this.selectedSessionID.length <= 32) {
                this.tlsClient.notifySessionID(this.selectedSessionID);
                boolean z = false;
                this.resumedSession = this.selectedSessionID.length > 0 && this.tlsSession != null && Arrays.areEqual(this.selectedSessionID, this.tlsSession.getSessionID());
                int readUint16 = TlsUtils.readUint16(byteArrayInputStream);
                if (!Arrays.contains(this.offeredCipherSuites, readUint16) || readUint16 == 0 || CipherSuite.isSCSV(readUint16) || !TlsUtils.isValidCipherSuiteForVersion(readUint16, getContext().getServerVersion())) {
                    throw new TlsFatalAlert(47);
                }
                this.tlsClient.notifySelectedCipherSuite(readUint16);
                short readUint8 = TlsUtils.readUint8(byteArrayInputStream);
                if (Arrays.contains(this.offeredCompressionMethods, readUint8)) {
                    this.tlsClient.notifySelectedCompressionMethod(readUint8);
                    this.serverExtensions = readExtensions(byteArrayInputStream);
                    if (this.serverExtensions != null) {
                        Enumeration keys = this.serverExtensions.keys();
                        while (keys.hasMoreElements()) {
                            Integer num = (Integer) keys.nextElement();
                            if (!num.equals(EXT_RenegotiationInfo)) {
                                if (TlsUtils.getExtensionData(this.clientExtensions, num) != null) {
                                    boolean z2 = this.resumedSession;
                                } else {
                                    throw new TlsFatalAlert(AlertDescription.unsupported_extension);
                                }
                            }
                        }
                    }
                    byte[] extensionData = TlsUtils.getExtensionData(this.serverExtensions, EXT_RenegotiationInfo);
                    if (extensionData != null) {
                        this.secure_renegotiation = true;
                        if (!Arrays.constantTimeAreEqual(extensionData, createRenegotiationInfo(TlsUtils.EMPTY_BYTES))) {
                            throw new TlsFatalAlert(40);
                        }
                    }
                    this.tlsClient.notifySecureRenegotiation(this.secure_renegotiation);
                    Hashtable hashtable = this.clientExtensions;
                    Hashtable hashtable2 = this.serverExtensions;
                    if (this.resumedSession) {
                        if (readUint16 == this.sessionParameters.getCipherSuite() && readUint8 == this.sessionParameters.getCompressionAlgorithm()) {
                            hashtable = null;
                            hashtable2 = this.sessionParameters.readServerExtensions();
                        } else {
                            throw new TlsFatalAlert(47);
                        }
                    }
                    this.securityParameters.cipherSuite = readUint16;
                    this.securityParameters.compressionAlgorithm = readUint8;
                    if (hashtable2 != null) {
                        boolean hasEncryptThenMACExtension = TlsExtensionsUtils.hasEncryptThenMACExtension(hashtable2);
                        if (!hasEncryptThenMACExtension || TlsUtils.isBlockCipherSuite(readUint16)) {
                            this.securityParameters.encryptThenMAC = hasEncryptThenMACExtension;
                            this.securityParameters.extendedMasterSecret = TlsExtensionsUtils.hasExtendedMasterSecretExtension(hashtable2);
                            this.securityParameters.maxFragmentLength = processMaxFragmentLengthExtension(hashtable, hashtable2, 47);
                            this.securityParameters.truncatedHMac = TlsExtensionsUtils.hasTruncatedHMacExtension(hashtable2);
                            this.allowCertificateStatus = !this.resumedSession && TlsUtils.hasExpectedEmptyExtensionData(hashtable2, TlsExtensionsUtils.EXT_status_request, 47);
                            if (!this.resumedSession && TlsUtils.hasExpectedEmptyExtensionData(hashtable2, TlsProtocol.EXT_SessionTicket, 47)) {
                                z = true;
                            }
                            this.expectSessionTicket = z;
                        } else {
                            throw new TlsFatalAlert(47);
                        }
                    }
                    if (hashtable != null) {
                        this.tlsClient.processServerExtensions(hashtable2);
                    }
                    this.securityParameters.prfAlgorithm = getPRFAlgorithm(getContext(), this.securityParameters.getCipherSuite());
                    this.securityParameters.verifyDataLength = 12;
                    return;
                }
                throw new TlsFatalAlert(47);
            }
            throw new TlsFatalAlert(47);
        } else {
            throw new TlsFatalAlert(47);
        }
    }

    /* access modifiers changed from: protected */
    public void sendCertificateVerifyMessage(DigitallySigned digitallySigned) throws IOException {
        TlsProtocol.HandshakeMessage handshakeMessage = new TlsProtocol.HandshakeMessage(this, 15);
        digitallySigned.encode(handshakeMessage);
        handshakeMessage.writeToRecordStream();
    }

    /* access modifiers changed from: protected */
    public void sendClientHelloMessage() throws IOException {
        this.recordStream.setWriteVersion(this.tlsClient.getClientHelloRecordLayerVersion());
        ProtocolVersion clientVersion = this.tlsClient.getClientVersion();
        if (!clientVersion.isDTLS()) {
            getContextAdmin().setClientVersion(clientVersion);
            byte[] bArr = TlsUtils.EMPTY_BYTES;
            if (this.tlsSession != null) {
                bArr = this.tlsSession.getSessionID();
                if (bArr == null || bArr.length > 32) {
                    bArr = TlsUtils.EMPTY_BYTES;
                }
            }
            boolean isFallback = this.tlsClient.isFallback();
            this.offeredCipherSuites = this.tlsClient.getCipherSuites();
            this.offeredCompressionMethods = this.tlsClient.getCompressionMethods();
            if (bArr.length > 0 && this.sessionParameters != null && (!Arrays.contains(this.offeredCipherSuites, this.sessionParameters.getCipherSuite()) || !Arrays.contains(this.offeredCompressionMethods, this.sessionParameters.getCompressionAlgorithm()))) {
                bArr = TlsUtils.EMPTY_BYTES;
            }
            this.clientExtensions = this.tlsClient.getClientExtensions();
            TlsProtocol.HandshakeMessage handshakeMessage = new TlsProtocol.HandshakeMessage(this, 1);
            TlsUtils.writeVersion(clientVersion, handshakeMessage);
            handshakeMessage.write(this.securityParameters.getClientRandom());
            TlsUtils.writeOpaque8(bArr, handshakeMessage);
            boolean z = TlsUtils.getExtensionData(this.clientExtensions, EXT_RenegotiationInfo) == null;
            boolean z2 = !Arrays.contains(this.offeredCipherSuites, 255);
            if (z && z2) {
                this.offeredCipherSuites = Arrays.append(this.offeredCipherSuites, 255);
            }
            if (isFallback && !Arrays.contains(this.offeredCipherSuites, (int) CipherSuite.TLS_FALLBACK_SCSV)) {
                this.offeredCipherSuites = Arrays.append(this.offeredCipherSuites, (int) CipherSuite.TLS_FALLBACK_SCSV);
            }
            TlsUtils.writeUint16ArrayWithUint16Length(this.offeredCipherSuites, handshakeMessage);
            TlsUtils.writeUint8ArrayWithUint8Length(this.offeredCompressionMethods, handshakeMessage);
            if (this.clientExtensions != null) {
                writeExtensions(handshakeMessage, this.clientExtensions);
            }
            handshakeMessage.writeToRecordStream();
            return;
        }
        throw new TlsFatalAlert(80);
    }

    /* access modifiers changed from: protected */
    public void sendClientKeyExchangeMessage() throws IOException {
        TlsProtocol.HandshakeMessage handshakeMessage = new TlsProtocol.HandshakeMessage(this, 16);
        this.keyExchange.generateClientKeyExchange(handshakeMessage);
        handshakeMessage.writeToRecordStream();
    }
}

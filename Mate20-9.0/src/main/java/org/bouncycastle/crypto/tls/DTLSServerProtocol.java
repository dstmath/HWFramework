package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Vector;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.tls.DTLSReliableHandshake;
import org.bouncycastle.crypto.tls.SessionParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.util.Arrays;

public class DTLSServerProtocol extends DTLSProtocol {
    protected boolean verifyRequests = true;

    protected static class ServerHandshakeState {
        boolean allowCertificateStatus = false;
        CertificateRequest certificateRequest = null;
        Certificate clientCertificate = null;
        short clientCertificateType = -1;
        Hashtable clientExtensions = null;
        boolean expectSessionTicket = false;
        TlsKeyExchange keyExchange = null;
        int[] offeredCipherSuites = null;
        short[] offeredCompressionMethods = null;
        boolean resumedSession = false;
        boolean secure_renegotiation = false;
        TlsServer server = null;
        TlsServerContextImpl serverContext = null;
        TlsCredentials serverCredentials = null;
        Hashtable serverExtensions = null;
        SessionParameters sessionParameters = null;
        SessionParameters.Builder sessionParametersBuilder = null;
        TlsSession tlsSession = null;

        protected ServerHandshakeState() {
        }
    }

    public DTLSServerProtocol(SecureRandom secureRandom) {
        super(secureRandom);
    }

    /* access modifiers changed from: protected */
    public void abortServerHandshake(ServerHandshakeState serverHandshakeState, DTLSRecordLayer dTLSRecordLayer, short s) {
        dTLSRecordLayer.fail(s);
        invalidateSession(serverHandshakeState);
    }

    public DTLSTransport accept(TlsServer tlsServer, DatagramTransport datagramTransport) throws IOException {
        if (tlsServer == null) {
            throw new IllegalArgumentException("'server' cannot be null");
        } else if (datagramTransport != null) {
            SecurityParameters securityParameters = new SecurityParameters();
            securityParameters.entity = 0;
            ServerHandshakeState serverHandshakeState = new ServerHandshakeState();
            serverHandshakeState.server = tlsServer;
            serverHandshakeState.serverContext = new TlsServerContextImpl(this.secureRandom, securityParameters);
            securityParameters.serverRandom = TlsProtocol.createRandomBlock(tlsServer.shouldUseGMTUnixTime(), serverHandshakeState.serverContext.getNonceRandomGenerator());
            tlsServer.init(serverHandshakeState.serverContext);
            DTLSRecordLayer dTLSRecordLayer = new DTLSRecordLayer(datagramTransport, serverHandshakeState.serverContext, tlsServer, 22);
            try {
                DTLSTransport serverHandshake = serverHandshake(serverHandshakeState, dTLSRecordLayer);
                securityParameters.clear();
                return serverHandshake;
            } catch (TlsFatalAlert e) {
                abortServerHandshake(serverHandshakeState, dTLSRecordLayer, e.getAlertDescription());
                throw e;
            } catch (IOException e2) {
                abortServerHandshake(serverHandshakeState, dTLSRecordLayer, 80);
                throw e2;
            } catch (RuntimeException e3) {
                abortServerHandshake(serverHandshakeState, dTLSRecordLayer, 80);
                throw new TlsFatalAlert(80, e3);
            } catch (Throwable th) {
                securityParameters.clear();
                throw th;
            }
        } else {
            throw new IllegalArgumentException("'transport' cannot be null");
        }
    }

    /* access modifiers changed from: protected */
    public boolean expectCertificateVerifyMessage(ServerHandshakeState serverHandshakeState) {
        return serverHandshakeState.clientCertificateType >= 0 && TlsUtils.hasSigningCapability(serverHandshakeState.clientCertificateType);
    }

    /* access modifiers changed from: protected */
    public byte[] generateCertificateRequest(ServerHandshakeState serverHandshakeState, CertificateRequest certificateRequest) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        certificateRequest.encode(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /* access modifiers changed from: protected */
    public byte[] generateCertificateStatus(ServerHandshakeState serverHandshakeState, CertificateStatus certificateStatus) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        certificateStatus.encode(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /* access modifiers changed from: protected */
    public byte[] generateNewSessionTicket(ServerHandshakeState serverHandshakeState, NewSessionTicket newSessionTicket) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        newSessionTicket.encode(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /* access modifiers changed from: protected */
    public byte[] generateServerHello(ServerHandshakeState serverHandshakeState) throws IOException {
        SecurityParameters securityParameters = serverHandshakeState.serverContext.getSecurityParameters();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ProtocolVersion serverVersion = serverHandshakeState.server.getServerVersion();
        if (serverVersion.isEqualOrEarlierVersionOf(serverHandshakeState.serverContext.getClientVersion())) {
            serverHandshakeState.serverContext.setServerVersion(serverVersion);
            TlsUtils.writeVersion(serverHandshakeState.serverContext.getServerVersion(), byteArrayOutputStream);
            byteArrayOutputStream.write(securityParameters.getServerRandom());
            TlsUtils.writeOpaque8(TlsUtils.EMPTY_BYTES, byteArrayOutputStream);
            int selectedCipherSuite = serverHandshakeState.server.getSelectedCipherSuite();
            if (!Arrays.contains(serverHandshakeState.offeredCipherSuites, selectedCipherSuite) || selectedCipherSuite == 0 || CipherSuite.isSCSV(selectedCipherSuite) || !TlsUtils.isValidCipherSuiteForVersion(selectedCipherSuite, serverHandshakeState.serverContext.getServerVersion())) {
                throw new TlsFatalAlert(80);
            }
            validateSelectedCipherSuite(selectedCipherSuite, 80);
            securityParameters.cipherSuite = selectedCipherSuite;
            short selectedCompressionMethod = serverHandshakeState.server.getSelectedCompressionMethod();
            if (Arrays.contains(serverHandshakeState.offeredCompressionMethods, selectedCompressionMethod)) {
                securityParameters.compressionAlgorithm = selectedCompressionMethod;
                TlsUtils.writeUint16(selectedCipherSuite, byteArrayOutputStream);
                TlsUtils.writeUint8(selectedCompressionMethod, (OutputStream) byteArrayOutputStream);
                serverHandshakeState.serverExtensions = serverHandshakeState.server.getServerExtensions();
                boolean z = false;
                if (serverHandshakeState.secure_renegotiation) {
                    if (TlsUtils.getExtensionData(serverHandshakeState.serverExtensions, TlsProtocol.EXT_RenegotiationInfo) == null) {
                        serverHandshakeState.serverExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(serverHandshakeState.serverExtensions);
                        serverHandshakeState.serverExtensions.put(TlsProtocol.EXT_RenegotiationInfo, TlsProtocol.createRenegotiationInfo(TlsUtils.EMPTY_BYTES));
                    }
                }
                if (securityParameters.extendedMasterSecret) {
                    serverHandshakeState.serverExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(serverHandshakeState.serverExtensions);
                    TlsExtensionsUtils.addExtendedMasterSecretExtension(serverHandshakeState.serverExtensions);
                }
                if (serverHandshakeState.serverExtensions != null) {
                    securityParameters.encryptThenMAC = TlsExtensionsUtils.hasEncryptThenMACExtension(serverHandshakeState.serverExtensions);
                    securityParameters.maxFragmentLength = evaluateMaxFragmentLengthExtension(serverHandshakeState.resumedSession, serverHandshakeState.clientExtensions, serverHandshakeState.serverExtensions, 80);
                    securityParameters.truncatedHMac = TlsExtensionsUtils.hasTruncatedHMacExtension(serverHandshakeState.serverExtensions);
                    serverHandshakeState.allowCertificateStatus = !serverHandshakeState.resumedSession && TlsUtils.hasExpectedEmptyExtensionData(serverHandshakeState.serverExtensions, TlsExtensionsUtils.EXT_status_request, 80);
                    if (!serverHandshakeState.resumedSession && TlsUtils.hasExpectedEmptyExtensionData(serverHandshakeState.serverExtensions, TlsProtocol.EXT_SessionTicket, 80)) {
                        z = true;
                    }
                    serverHandshakeState.expectSessionTicket = z;
                    TlsProtocol.writeExtensions(byteArrayOutputStream, serverHandshakeState.serverExtensions);
                }
                securityParameters.prfAlgorithm = TlsProtocol.getPRFAlgorithm(serverHandshakeState.serverContext, securityParameters.getCipherSuite());
                securityParameters.verifyDataLength = 12;
                return byteArrayOutputStream.toByteArray();
            }
            throw new TlsFatalAlert(80);
        }
        throw new TlsFatalAlert(80);
    }

    public boolean getVerifyRequests() {
        return this.verifyRequests;
    }

    /* access modifiers changed from: protected */
    public void invalidateSession(ServerHandshakeState serverHandshakeState) {
        if (serverHandshakeState.sessionParameters != null) {
            serverHandshakeState.sessionParameters.clear();
            serverHandshakeState.sessionParameters = null;
        }
        if (serverHandshakeState.tlsSession != null) {
            serverHandshakeState.tlsSession.invalidate();
            serverHandshakeState.tlsSession = null;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyClientCertificate(ServerHandshakeState serverHandshakeState, Certificate certificate) throws IOException {
        if (serverHandshakeState.certificateRequest == null) {
            throw new IllegalStateException();
        } else if (serverHandshakeState.clientCertificate == null) {
            serverHandshakeState.clientCertificate = certificate;
            if (certificate.isEmpty()) {
                serverHandshakeState.keyExchange.skipClientCredentials();
            } else {
                serverHandshakeState.clientCertificateType = TlsUtils.getClientCertificateType(certificate, serverHandshakeState.serverCredentials.getCertificate());
                serverHandshakeState.keyExchange.processClientCertificate(certificate);
            }
            serverHandshakeState.server.notifyClientCertificate(certificate);
        } else {
            throw new TlsFatalAlert(10);
        }
    }

    /* access modifiers changed from: protected */
    public void processCertificateVerify(ServerHandshakeState serverHandshakeState, byte[] bArr, TlsHandshakeHash tlsHandshakeHash) throws IOException {
        byte[] bArr2;
        if (serverHandshakeState.certificateRequest != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
            TlsServerContextImpl tlsServerContextImpl = serverHandshakeState.serverContext;
            DigitallySigned parse = DigitallySigned.parse(tlsServerContextImpl, byteArrayInputStream);
            TlsProtocol.assertEmpty(byteArrayInputStream);
            try {
                SignatureAndHashAlgorithm algorithm = parse.getAlgorithm();
                if (TlsUtils.isTLSv12((TlsContext) tlsServerContextImpl)) {
                    TlsUtils.verifySupportedSignatureAlgorithm(serverHandshakeState.certificateRequest.getSupportedSignatureAlgorithms(), algorithm);
                    bArr2 = tlsHandshakeHash.getFinalHash(algorithm.getHash());
                } else {
                    bArr2 = tlsServerContextImpl.getSecurityParameters().getSessionHash();
                }
                AsymmetricKeyParameter createKey = PublicKeyFactory.createKey(serverHandshakeState.clientCertificate.getCertificateAt(0).getSubjectPublicKeyInfo());
                TlsSigner createTlsSigner = TlsUtils.createTlsSigner(serverHandshakeState.clientCertificateType);
                createTlsSigner.init(tlsServerContextImpl);
                if (!createTlsSigner.verifyRawSignature(algorithm, parse.getSignature(), createKey, bArr2)) {
                    throw new TlsFatalAlert(51);
                }
            } catch (TlsFatalAlert e) {
                throw e;
            } catch (Exception e2) {
                throw new TlsFatalAlert(51, e2);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /* access modifiers changed from: protected */
    public void processClientCertificate(ServerHandshakeState serverHandshakeState, byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        Certificate parse = Certificate.parse(byteArrayInputStream);
        TlsProtocol.assertEmpty(byteArrayInputStream);
        notifyClientCertificate(serverHandshakeState, parse);
    }

    /* access modifiers changed from: protected */
    public void processClientHello(ServerHandshakeState serverHandshakeState, byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        ProtocolVersion readVersion = TlsUtils.readVersion(byteArrayInputStream);
        if (readVersion.isDTLS()) {
            byte[] readFully = TlsUtils.readFully(32, (InputStream) byteArrayInputStream);
            if (TlsUtils.readOpaque8(byteArrayInputStream).length <= 32) {
                TlsUtils.readOpaque8(byteArrayInputStream);
                int readUint16 = TlsUtils.readUint16(byteArrayInputStream);
                if (readUint16 < 2 || (readUint16 & 1) != 0) {
                    throw new TlsFatalAlert(50);
                }
                serverHandshakeState.offeredCipherSuites = TlsUtils.readUint16Array(readUint16 / 2, byteArrayInputStream);
                short readUint8 = TlsUtils.readUint8(byteArrayInputStream);
                if (readUint8 >= 1) {
                    serverHandshakeState.offeredCompressionMethods = TlsUtils.readUint8Array(readUint8, byteArrayInputStream);
                    serverHandshakeState.clientExtensions = TlsProtocol.readExtensions(byteArrayInputStream);
                    TlsServerContextImpl tlsServerContextImpl = serverHandshakeState.serverContext;
                    SecurityParameters securityParameters = tlsServerContextImpl.getSecurityParameters();
                    securityParameters.extendedMasterSecret = TlsExtensionsUtils.hasExtendedMasterSecretExtension(serverHandshakeState.clientExtensions);
                    tlsServerContextImpl.setClientVersion(readVersion);
                    serverHandshakeState.server.notifyClientVersion(readVersion);
                    serverHandshakeState.server.notifyFallback(Arrays.contains(serverHandshakeState.offeredCipherSuites, (int) CipherSuite.TLS_FALLBACK_SCSV));
                    securityParameters.clientRandom = readFully;
                    serverHandshakeState.server.notifyOfferedCipherSuites(serverHandshakeState.offeredCipherSuites);
                    serverHandshakeState.server.notifyOfferedCompressionMethods(serverHandshakeState.offeredCompressionMethods);
                    if (Arrays.contains(serverHandshakeState.offeredCipherSuites, 255)) {
                        serverHandshakeState.secure_renegotiation = true;
                    }
                    byte[] extensionData = TlsUtils.getExtensionData(serverHandshakeState.clientExtensions, TlsProtocol.EXT_RenegotiationInfo);
                    if (extensionData != null) {
                        serverHandshakeState.secure_renegotiation = true;
                        if (!Arrays.constantTimeAreEqual(extensionData, TlsProtocol.createRenegotiationInfo(TlsUtils.EMPTY_BYTES))) {
                            throw new TlsFatalAlert(40);
                        }
                    }
                    serverHandshakeState.server.notifySecureRenegotiation(serverHandshakeState.secure_renegotiation);
                    if (serverHandshakeState.clientExtensions != null) {
                        TlsExtensionsUtils.getPaddingExtension(serverHandshakeState.clientExtensions);
                        serverHandshakeState.server.processClientExtensions(serverHandshakeState.clientExtensions);
                        return;
                    }
                    return;
                }
                throw new TlsFatalAlert(47);
            }
            throw new TlsFatalAlert(47);
        }
        throw new TlsFatalAlert(47);
    }

    /* access modifiers changed from: protected */
    public void processClientKeyExchange(ServerHandshakeState serverHandshakeState, byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        serverHandshakeState.keyExchange.processClientKeyExchange(byteArrayInputStream);
        TlsProtocol.assertEmpty(byteArrayInputStream);
    }

    /* access modifiers changed from: protected */
    public void processClientSupplementalData(ServerHandshakeState serverHandshakeState, byte[] bArr) throws IOException {
        serverHandshakeState.server.processClientSupplementalData(TlsProtocol.readSupplementalDataMessage(new ByteArrayInputStream(bArr)));
    }

    /* access modifiers changed from: protected */
    public DTLSTransport serverHandshake(ServerHandshakeState serverHandshakeState, DTLSRecordLayer dTLSRecordLayer) throws IOException {
        Certificate certificate;
        SecurityParameters securityParameters = serverHandshakeState.serverContext.getSecurityParameters();
        DTLSReliableHandshake dTLSReliableHandshake = new DTLSReliableHandshake(serverHandshakeState.serverContext, dTLSRecordLayer);
        DTLSReliableHandshake.Message receiveMessage = dTLSReliableHandshake.receiveMessage();
        boolean z = true;
        if (receiveMessage.getType() == 1) {
            processClientHello(serverHandshakeState, receiveMessage.getBody());
            byte[] generateServerHello = generateServerHello(serverHandshakeState);
            applyMaxFragmentLengthExtension(dTLSRecordLayer, securityParameters.maxFragmentLength);
            ProtocolVersion serverVersion = serverHandshakeState.serverContext.getServerVersion();
            dTLSRecordLayer.setReadVersion(serverVersion);
            dTLSRecordLayer.setWriteVersion(serverVersion);
            dTLSReliableHandshake.sendMessage(2, generateServerHello);
            dTLSReliableHandshake.notifyHelloComplete();
            Vector serverSupplementalData = serverHandshakeState.server.getServerSupplementalData();
            if (serverSupplementalData != null) {
                dTLSReliableHandshake.sendMessage(23, generateSupplementalData(serverSupplementalData));
            }
            serverHandshakeState.keyExchange = serverHandshakeState.server.getKeyExchange();
            serverHandshakeState.keyExchange.init(serverHandshakeState.serverContext);
            serverHandshakeState.serverCredentials = serverHandshakeState.server.getCredentials();
            if (serverHandshakeState.serverCredentials == null) {
                serverHandshakeState.keyExchange.skipServerCredentials();
                certificate = null;
            } else {
                serverHandshakeState.keyExchange.processServerCredentials(serverHandshakeState.serverCredentials);
                certificate = serverHandshakeState.serverCredentials.getCertificate();
                dTLSReliableHandshake.sendMessage(11, generateCertificate(certificate));
            }
            if (certificate == null || certificate.isEmpty()) {
                serverHandshakeState.allowCertificateStatus = false;
            }
            if (serverHandshakeState.allowCertificateStatus) {
                CertificateStatus certificateStatus = serverHandshakeState.server.getCertificateStatus();
                if (certificateStatus != null) {
                    dTLSReliableHandshake.sendMessage(22, generateCertificateStatus(serverHandshakeState, certificateStatus));
                }
            }
            byte[] generateServerKeyExchange = serverHandshakeState.keyExchange.generateServerKeyExchange();
            if (generateServerKeyExchange != null) {
                dTLSReliableHandshake.sendMessage(12, generateServerKeyExchange);
            }
            if (serverHandshakeState.serverCredentials != null) {
                serverHandshakeState.certificateRequest = serverHandshakeState.server.getCertificateRequest();
                if (serverHandshakeState.certificateRequest != null) {
                    boolean isTLSv12 = TlsUtils.isTLSv12((TlsContext) serverHandshakeState.serverContext);
                    if (serverHandshakeState.certificateRequest.getSupportedSignatureAlgorithms() == null) {
                        z = false;
                    }
                    if (isTLSv12 == z) {
                        serverHandshakeState.keyExchange.validateCertificateRequest(serverHandshakeState.certificateRequest);
                        dTLSReliableHandshake.sendMessage(13, generateCertificateRequest(serverHandshakeState, serverHandshakeState.certificateRequest));
                        TlsUtils.trackHashAlgorithms(dTLSReliableHandshake.getHandshakeHash(), serverHandshakeState.certificateRequest.getSupportedSignatureAlgorithms());
                    } else {
                        throw new TlsFatalAlert(80);
                    }
                }
            }
            dTLSReliableHandshake.sendMessage(14, TlsUtils.EMPTY_BYTES);
            dTLSReliableHandshake.getHandshakeHash().sealHashAlgorithms();
            DTLSReliableHandshake.Message receiveMessage2 = dTLSReliableHandshake.receiveMessage();
            if (receiveMessage2.getType() == 23) {
                processClientSupplementalData(serverHandshakeState, receiveMessage2.getBody());
                receiveMessage2 = dTLSReliableHandshake.receiveMessage();
            } else {
                serverHandshakeState.server.processClientSupplementalData(null);
            }
            if (serverHandshakeState.certificateRequest == null) {
                serverHandshakeState.keyExchange.skipClientCredentials();
            } else if (receiveMessage2.getType() == 11) {
                processClientCertificate(serverHandshakeState, receiveMessage2.getBody());
                receiveMessage2 = dTLSReliableHandshake.receiveMessage();
            } else if (!TlsUtils.isTLSv12((TlsContext) serverHandshakeState.serverContext)) {
                notifyClientCertificate(serverHandshakeState, Certificate.EMPTY_CHAIN);
            } else {
                throw new TlsFatalAlert(10);
            }
            if (receiveMessage2.getType() == 16) {
                processClientKeyExchange(serverHandshakeState, receiveMessage2.getBody());
                TlsHandshakeHash prepareToFinish = dTLSReliableHandshake.prepareToFinish();
                securityParameters.sessionHash = TlsProtocol.getCurrentPRFHash(serverHandshakeState.serverContext, prepareToFinish, null);
                TlsProtocol.establishMasterSecret(serverHandshakeState.serverContext, serverHandshakeState.keyExchange);
                dTLSRecordLayer.initPendingEpoch(serverHandshakeState.server.getCipher());
                if (expectCertificateVerifyMessage(serverHandshakeState)) {
                    processCertificateVerify(serverHandshakeState, dTLSReliableHandshake.receiveMessageBody(15), prepareToFinish);
                }
                processFinished(dTLSReliableHandshake.receiveMessageBody(20), TlsUtils.calculateVerifyData(serverHandshakeState.serverContext, ExporterLabel.client_finished, TlsProtocol.getCurrentPRFHash(serverHandshakeState.serverContext, dTLSReliableHandshake.getHandshakeHash(), null)));
                if (serverHandshakeState.expectSessionTicket) {
                    dTLSReliableHandshake.sendMessage(4, generateNewSessionTicket(serverHandshakeState, serverHandshakeState.server.getNewSessionTicket()));
                }
                dTLSReliableHandshake.sendMessage(20, TlsUtils.calculateVerifyData(serverHandshakeState.serverContext, ExporterLabel.server_finished, TlsProtocol.getCurrentPRFHash(serverHandshakeState.serverContext, dTLSReliableHandshake.getHandshakeHash(), null)));
                dTLSReliableHandshake.finish();
                serverHandshakeState.server.notifyHandshakeComplete();
                return new DTLSTransport(dTLSRecordLayer);
            }
            throw new TlsFatalAlert(10);
        }
        throw new TlsFatalAlert(10);
    }

    public void setVerifyRequests(boolean z) {
        this.verifyRequests = z;
    }
}

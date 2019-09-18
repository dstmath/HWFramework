package org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.bouncycastle.crypto.tls.DTLSReliableHandshake;
import org.bouncycastle.crypto.tls.SessionParameters;
import org.bouncycastle.util.Arrays;

public class DTLSClientProtocol extends DTLSProtocol {

    protected static class ClientHandshakeState {
        boolean allowCertificateStatus = false;
        TlsAuthentication authentication = null;
        CertificateRequest certificateRequest = null;
        CertificateStatus certificateStatus = null;
        TlsClient client = null;
        TlsClientContextImpl clientContext = null;
        TlsCredentials clientCredentials = null;
        Hashtable clientExtensions = null;
        boolean expectSessionTicket = false;
        TlsKeyExchange keyExchange = null;
        int[] offeredCipherSuites = null;
        short[] offeredCompressionMethods = null;
        boolean resumedSession = false;
        boolean secure_renegotiation = false;
        byte[] selectedSessionID = null;
        Hashtable serverExtensions = null;
        SessionParameters sessionParameters = null;
        SessionParameters.Builder sessionParametersBuilder = null;
        TlsSession tlsSession = null;

        protected ClientHandshakeState() {
        }
    }

    public DTLSClientProtocol(SecureRandom secureRandom) {
        super(secureRandom);
    }

    protected static byte[] patchClientHelloWithCookie(byte[] bArr, byte[] bArr2) throws IOException {
        int readUint8 = 35 + TlsUtils.readUint8(bArr, 34);
        int i = readUint8 + 1;
        byte[] bArr3 = new byte[(bArr.length + bArr2.length)];
        System.arraycopy(bArr, 0, bArr3, 0, readUint8);
        TlsUtils.checkUint8(bArr2.length);
        TlsUtils.writeUint8(bArr2.length, bArr3, readUint8);
        System.arraycopy(bArr2, 0, bArr3, i, bArr2.length);
        System.arraycopy(bArr, i, bArr3, bArr2.length + i, bArr.length - i);
        return bArr3;
    }

    /* access modifiers changed from: protected */
    public void abortClientHandshake(ClientHandshakeState clientHandshakeState, DTLSRecordLayer dTLSRecordLayer, short s) {
        dTLSRecordLayer.fail(s);
        invalidateSession(clientHandshakeState);
    }

    /* access modifiers changed from: protected */
    public DTLSTransport clientHandshake(ClientHandshakeState clientHandshakeState, DTLSRecordLayer dTLSRecordLayer) throws IOException {
        DTLSReliableHandshake.Message message;
        Certificate certificate;
        SecurityParameters securityParameters = clientHandshakeState.clientContext.getSecurityParameters();
        DTLSReliableHandshake dTLSReliableHandshake = new DTLSReliableHandshake(clientHandshakeState.clientContext, dTLSRecordLayer);
        byte[] generateClientHello = generateClientHello(clientHandshakeState, clientHandshakeState.client);
        dTLSRecordLayer.setWriteVersion(ProtocolVersion.DTLSv10);
        dTLSReliableHandshake.sendMessage(1, generateClientHello);
        while (true) {
            DTLSReliableHandshake.Message receiveMessage = dTLSReliableHandshake.receiveMessage();
            if (receiveMessage.getType() == 3) {
                if (dTLSRecordLayer.getReadVersion().isEqualOrEarlierVersionOf(clientHandshakeState.clientContext.getClientVersion())) {
                    dTLSRecordLayer.setReadVersion(null);
                    byte[] patchClientHelloWithCookie = patchClientHelloWithCookie(generateClientHello, processHelloVerifyRequest(clientHandshakeState, receiveMessage.getBody()));
                    dTLSReliableHandshake.resetHandshakeMessagesDigest();
                    dTLSReliableHandshake.sendMessage(1, patchClientHelloWithCookie);
                } else {
                    throw new TlsFatalAlert(47);
                }
            } else if (receiveMessage.getType() == 2) {
                ProtocolVersion readVersion = dTLSRecordLayer.getReadVersion();
                reportServerVersion(clientHandshakeState, readVersion);
                dTLSRecordLayer.setWriteVersion(readVersion);
                processServerHello(clientHandshakeState, receiveMessage.getBody());
                dTLSReliableHandshake.notifyHelloComplete();
                applyMaxFragmentLengthExtension(dTLSRecordLayer, securityParameters.maxFragmentLength);
                if (clientHandshakeState.resumedSession) {
                    securityParameters.masterSecret = Arrays.clone(clientHandshakeState.sessionParameters.getMasterSecret());
                    dTLSRecordLayer.initPendingEpoch(clientHandshakeState.client.getCipher());
                    processFinished(dTLSReliableHandshake.receiveMessageBody(20), TlsUtils.calculateVerifyData(clientHandshakeState.clientContext, ExporterLabel.server_finished, TlsProtocol.getCurrentPRFHash(clientHandshakeState.clientContext, dTLSReliableHandshake.getHandshakeHash(), null)));
                    dTLSReliableHandshake.sendMessage(20, TlsUtils.calculateVerifyData(clientHandshakeState.clientContext, ExporterLabel.client_finished, TlsProtocol.getCurrentPRFHash(clientHandshakeState.clientContext, dTLSReliableHandshake.getHandshakeHash(), null)));
                    dTLSReliableHandshake.finish();
                    clientHandshakeState.clientContext.setResumableSession(clientHandshakeState.tlsSession);
                    clientHandshakeState.client.notifyHandshakeComplete();
                    return new DTLSTransport(dTLSRecordLayer);
                }
                invalidateSession(clientHandshakeState);
                if (clientHandshakeState.selectedSessionID.length > 0) {
                    clientHandshakeState.tlsSession = new TlsSessionImpl(clientHandshakeState.selectedSessionID, null);
                }
                DTLSReliableHandshake.Message receiveMessage2 = dTLSReliableHandshake.receiveMessage();
                if (receiveMessage2.getType() == 23) {
                    processServerSupplementalData(clientHandshakeState, receiveMessage2.getBody());
                    receiveMessage2 = dTLSReliableHandshake.receiveMessage();
                } else {
                    clientHandshakeState.client.processServerSupplementalData(null);
                }
                clientHandshakeState.keyExchange = clientHandshakeState.client.getKeyExchange();
                clientHandshakeState.keyExchange.init(clientHandshakeState.clientContext);
                if (receiveMessage2.getType() == 11) {
                    certificate = processServerCertificate(clientHandshakeState, receiveMessage2.getBody());
                    message = dTLSReliableHandshake.receiveMessage();
                } else {
                    clientHandshakeState.keyExchange.skipServerCredentials();
                    message = receiveMessage2;
                    certificate = null;
                }
                if (certificate == null || certificate.isEmpty()) {
                    clientHandshakeState.allowCertificateStatus = false;
                }
                if (message.getType() == 22) {
                    processCertificateStatus(clientHandshakeState, message.getBody());
                    message = dTLSReliableHandshake.receiveMessage();
                }
                if (message.getType() == 12) {
                    processServerKeyExchange(clientHandshakeState, message.getBody());
                    message = dTLSReliableHandshake.receiveMessage();
                } else {
                    clientHandshakeState.keyExchange.skipServerKeyExchange();
                }
                if (message.getType() == 13) {
                    processCertificateRequest(clientHandshakeState, message.getBody());
                    TlsUtils.trackHashAlgorithms(dTLSReliableHandshake.getHandshakeHash(), clientHandshakeState.certificateRequest.getSupportedSignatureAlgorithms());
                    message = dTLSReliableHandshake.receiveMessage();
                }
                if (message.getType() != 14) {
                    throw new TlsFatalAlert(10);
                } else if (message.getBody().length == 0) {
                    dTLSReliableHandshake.getHandshakeHash().sealHashAlgorithms();
                    Vector clientSupplementalData = clientHandshakeState.client.getClientSupplementalData();
                    if (clientSupplementalData != null) {
                        dTLSReliableHandshake.sendMessage(23, generateSupplementalData(clientSupplementalData));
                    }
                    if (clientHandshakeState.certificateRequest != null) {
                        clientHandshakeState.clientCredentials = clientHandshakeState.authentication.getClientCredentials(clientHandshakeState.certificateRequest);
                        Certificate certificate2 = clientHandshakeState.clientCredentials != null ? clientHandshakeState.clientCredentials.getCertificate() : null;
                        if (certificate2 == null) {
                            certificate2 = Certificate.EMPTY_CHAIN;
                        }
                        dTLSReliableHandshake.sendMessage(11, generateCertificate(certificate2));
                    }
                    if (clientHandshakeState.clientCredentials != null) {
                        clientHandshakeState.keyExchange.processClientCredentials(clientHandshakeState.clientCredentials);
                    } else {
                        clientHandshakeState.keyExchange.skipClientCredentials();
                    }
                    dTLSReliableHandshake.sendMessage(16, generateClientKeyExchange(clientHandshakeState));
                    TlsHandshakeHash prepareToFinish = dTLSReliableHandshake.prepareToFinish();
                    securityParameters.sessionHash = TlsProtocol.getCurrentPRFHash(clientHandshakeState.clientContext, prepareToFinish, null);
                    TlsProtocol.establishMasterSecret(clientHandshakeState.clientContext, clientHandshakeState.keyExchange);
                    dTLSRecordLayer.initPendingEpoch(clientHandshakeState.client.getCipher());
                    if (clientHandshakeState.clientCredentials != null && (clientHandshakeState.clientCredentials instanceof TlsSignerCredentials)) {
                        TlsSignerCredentials tlsSignerCredentials = (TlsSignerCredentials) clientHandshakeState.clientCredentials;
                        SignatureAndHashAlgorithm signatureAndHashAlgorithm = TlsUtils.getSignatureAndHashAlgorithm(clientHandshakeState.clientContext, tlsSignerCredentials);
                        dTLSReliableHandshake.sendMessage(15, generateCertificateVerify(clientHandshakeState, new DigitallySigned(signatureAndHashAlgorithm, tlsSignerCredentials.generateCertificateSignature(signatureAndHashAlgorithm == null ? securityParameters.getSessionHash() : prepareToFinish.getFinalHash(signatureAndHashAlgorithm.getHash())))));
                    }
                    dTLSReliableHandshake.sendMessage(20, TlsUtils.calculateVerifyData(clientHandshakeState.clientContext, ExporterLabel.client_finished, TlsProtocol.getCurrentPRFHash(clientHandshakeState.clientContext, dTLSReliableHandshake.getHandshakeHash(), null)));
                    if (clientHandshakeState.expectSessionTicket) {
                        DTLSReliableHandshake.Message receiveMessage3 = dTLSReliableHandshake.receiveMessage();
                        if (receiveMessage3.getType() == 4) {
                            processNewSessionTicket(clientHandshakeState, receiveMessage3.getBody());
                        } else {
                            throw new TlsFatalAlert(10);
                        }
                    }
                    processFinished(dTLSReliableHandshake.receiveMessageBody(20), TlsUtils.calculateVerifyData(clientHandshakeState.clientContext, ExporterLabel.server_finished, TlsProtocol.getCurrentPRFHash(clientHandshakeState.clientContext, dTLSReliableHandshake.getHandshakeHash(), null)));
                    dTLSReliableHandshake.finish();
                    if (clientHandshakeState.tlsSession != null) {
                        clientHandshakeState.sessionParameters = new SessionParameters.Builder().setCipherSuite(securityParameters.getCipherSuite()).setCompressionAlgorithm(securityParameters.getCompressionAlgorithm()).setMasterSecret(securityParameters.getMasterSecret()).setPeerCertificate(certificate).setPSKIdentity(securityParameters.getPSKIdentity()).setSRPIdentity(securityParameters.getSRPIdentity()).setServerExtensions(clientHandshakeState.serverExtensions).build();
                        clientHandshakeState.tlsSession = TlsUtils.importSession(clientHandshakeState.tlsSession.getSessionID(), clientHandshakeState.sessionParameters);
                        clientHandshakeState.clientContext.setResumableSession(clientHandshakeState.tlsSession);
                    }
                    clientHandshakeState.client.notifyHandshakeComplete();
                    return new DTLSTransport(dTLSRecordLayer);
                } else {
                    throw new TlsFatalAlert(50);
                }
            } else {
                throw new TlsFatalAlert(10);
            }
        }
    }

    public DTLSTransport connect(TlsClient tlsClient, DatagramTransport datagramTransport) throws IOException {
        if (tlsClient == null) {
            throw new IllegalArgumentException("'client' cannot be null");
        } else if (datagramTransport != null) {
            SecurityParameters securityParameters = new SecurityParameters();
            securityParameters.entity = 1;
            ClientHandshakeState clientHandshakeState = new ClientHandshakeState();
            clientHandshakeState.client = tlsClient;
            clientHandshakeState.clientContext = new TlsClientContextImpl(this.secureRandom, securityParameters);
            securityParameters.clientRandom = TlsProtocol.createRandomBlock(tlsClient.shouldUseGMTUnixTime(), clientHandshakeState.clientContext.getNonceRandomGenerator());
            tlsClient.init(clientHandshakeState.clientContext);
            DTLSRecordLayer dTLSRecordLayer = new DTLSRecordLayer(datagramTransport, clientHandshakeState.clientContext, tlsClient, 22);
            TlsSession sessionToResume = clientHandshakeState.client.getSessionToResume();
            if (sessionToResume != null && sessionToResume.isResumable()) {
                SessionParameters exportSessionParameters = sessionToResume.exportSessionParameters();
                if (exportSessionParameters != null) {
                    clientHandshakeState.tlsSession = sessionToResume;
                    clientHandshakeState.sessionParameters = exportSessionParameters;
                }
            }
            try {
                DTLSTransport clientHandshake = clientHandshake(clientHandshakeState, dTLSRecordLayer);
                securityParameters.clear();
                return clientHandshake;
            } catch (TlsFatalAlert e) {
                abortClientHandshake(clientHandshakeState, dTLSRecordLayer, e.getAlertDescription());
                throw e;
            } catch (IOException e2) {
                abortClientHandshake(clientHandshakeState, dTLSRecordLayer, 80);
                throw e2;
            } catch (RuntimeException e3) {
                abortClientHandshake(clientHandshakeState, dTLSRecordLayer, 80);
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
    public byte[] generateCertificateVerify(ClientHandshakeState clientHandshakeState, DigitallySigned digitallySigned) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        digitallySigned.encode(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /* access modifiers changed from: protected */
    public byte[] generateClientHello(ClientHandshakeState clientHandshakeState, TlsClient tlsClient) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ProtocolVersion clientVersion = tlsClient.getClientVersion();
        if (clientVersion.isDTLS()) {
            TlsClientContextImpl tlsClientContextImpl = clientHandshakeState.clientContext;
            tlsClientContextImpl.setClientVersion(clientVersion);
            TlsUtils.writeVersion(clientVersion, byteArrayOutputStream);
            byteArrayOutputStream.write(tlsClientContextImpl.getSecurityParameters().getClientRandom());
            byte[] bArr = TlsUtils.EMPTY_BYTES;
            if (clientHandshakeState.tlsSession != null) {
                bArr = clientHandshakeState.tlsSession.getSessionID();
                if (bArr == null || bArr.length > 32) {
                    bArr = TlsUtils.EMPTY_BYTES;
                }
            }
            TlsUtils.writeOpaque8(bArr, byteArrayOutputStream);
            TlsUtils.writeOpaque8(TlsUtils.EMPTY_BYTES, byteArrayOutputStream);
            boolean isFallback = tlsClient.isFallback();
            clientHandshakeState.offeredCipherSuites = tlsClient.getCipherSuites();
            clientHandshakeState.clientExtensions = tlsClient.getClientExtensions();
            boolean z = TlsUtils.getExtensionData(clientHandshakeState.clientExtensions, TlsProtocol.EXT_RenegotiationInfo) == null;
            boolean z2 = !Arrays.contains(clientHandshakeState.offeredCipherSuites, 255);
            if (z && z2) {
                clientHandshakeState.offeredCipherSuites = Arrays.append(clientHandshakeState.offeredCipherSuites, 255);
            }
            if (isFallback && !Arrays.contains(clientHandshakeState.offeredCipherSuites, (int) CipherSuite.TLS_FALLBACK_SCSV)) {
                clientHandshakeState.offeredCipherSuites = Arrays.append(clientHandshakeState.offeredCipherSuites, (int) CipherSuite.TLS_FALLBACK_SCSV);
            }
            TlsUtils.writeUint16ArrayWithUint16Length(clientHandshakeState.offeredCipherSuites, byteArrayOutputStream);
            clientHandshakeState.offeredCompressionMethods = new short[]{0};
            TlsUtils.writeUint8ArrayWithUint8Length(clientHandshakeState.offeredCompressionMethods, byteArrayOutputStream);
            if (clientHandshakeState.clientExtensions != null) {
                TlsProtocol.writeExtensions(byteArrayOutputStream, clientHandshakeState.clientExtensions);
            }
            return byteArrayOutputStream.toByteArray();
        }
        throw new TlsFatalAlert(80);
    }

    /* access modifiers changed from: protected */
    public byte[] generateClientKeyExchange(ClientHandshakeState clientHandshakeState) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        clientHandshakeState.keyExchange.generateClientKeyExchange(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /* access modifiers changed from: protected */
    public void invalidateSession(ClientHandshakeState clientHandshakeState) {
        if (clientHandshakeState.sessionParameters != null) {
            clientHandshakeState.sessionParameters.clear();
            clientHandshakeState.sessionParameters = null;
        }
        if (clientHandshakeState.tlsSession != null) {
            clientHandshakeState.tlsSession.invalidate();
            clientHandshakeState.tlsSession = null;
        }
    }

    /* access modifiers changed from: protected */
    public void processCertificateRequest(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        if (clientHandshakeState.authentication != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
            clientHandshakeState.certificateRequest = CertificateRequest.parse(clientHandshakeState.clientContext, byteArrayInputStream);
            TlsProtocol.assertEmpty(byteArrayInputStream);
            clientHandshakeState.keyExchange.validateCertificateRequest(clientHandshakeState.certificateRequest);
            return;
        }
        throw new TlsFatalAlert(40);
    }

    /* access modifiers changed from: protected */
    public void processCertificateStatus(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        if (clientHandshakeState.allowCertificateStatus) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
            clientHandshakeState.certificateStatus = CertificateStatus.parse(byteArrayInputStream);
            TlsProtocol.assertEmpty(byteArrayInputStream);
            return;
        }
        throw new TlsFatalAlert(10);
    }

    /* access modifiers changed from: protected */
    public byte[] processHelloVerifyRequest(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        ProtocolVersion readVersion = TlsUtils.readVersion(byteArrayInputStream);
        byte[] readOpaque8 = TlsUtils.readOpaque8(byteArrayInputStream);
        TlsProtocol.assertEmpty(byteArrayInputStream);
        if (!readVersion.isEqualOrEarlierVersionOf(clientHandshakeState.clientContext.getClientVersion())) {
            throw new TlsFatalAlert(47);
        } else if (ProtocolVersion.DTLSv12.isEqualOrEarlierVersionOf(readVersion) || readOpaque8.length <= 32) {
            return readOpaque8;
        } else {
            throw new TlsFatalAlert(47);
        }
    }

    /* access modifiers changed from: protected */
    public void processNewSessionTicket(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        NewSessionTicket parse = NewSessionTicket.parse(byteArrayInputStream);
        TlsProtocol.assertEmpty(byteArrayInputStream);
        clientHandshakeState.client.notifyNewSessionTicket(parse);
    }

    /* access modifiers changed from: protected */
    public Certificate processServerCertificate(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        Certificate parse = Certificate.parse(byteArrayInputStream);
        TlsProtocol.assertEmpty(byteArrayInputStream);
        clientHandshakeState.keyExchange.processServerCertificate(parse);
        clientHandshakeState.authentication = clientHandshakeState.client.getAuthentication();
        clientHandshakeState.authentication.notifyServerCertificate(parse);
        return parse;
    }

    /* access modifiers changed from: protected */
    public void processServerHello(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        SecurityParameters securityParameters = clientHandshakeState.clientContext.getSecurityParameters();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        reportServerVersion(clientHandshakeState, TlsUtils.readVersion(byteArrayInputStream));
        securityParameters.serverRandom = TlsUtils.readFully(32, (InputStream) byteArrayInputStream);
        clientHandshakeState.selectedSessionID = TlsUtils.readOpaque8(byteArrayInputStream);
        if (clientHandshakeState.selectedSessionID.length <= 32) {
            clientHandshakeState.client.notifySessionID(clientHandshakeState.selectedSessionID);
            boolean z = false;
            clientHandshakeState.resumedSession = clientHandshakeState.selectedSessionID.length > 0 && clientHandshakeState.tlsSession != null && Arrays.areEqual(clientHandshakeState.selectedSessionID, clientHandshakeState.tlsSession.getSessionID());
            int readUint16 = TlsUtils.readUint16(byteArrayInputStream);
            if (!Arrays.contains(clientHandshakeState.offeredCipherSuites, readUint16) || readUint16 == 0 || CipherSuite.isSCSV(readUint16) || !TlsUtils.isValidCipherSuiteForVersion(readUint16, clientHandshakeState.clientContext.getServerVersion())) {
                throw new TlsFatalAlert(47);
            }
            validateSelectedCipherSuite(readUint16, 47);
            clientHandshakeState.client.notifySelectedCipherSuite(readUint16);
            short readUint8 = TlsUtils.readUint8(byteArrayInputStream);
            if (Arrays.contains(clientHandshakeState.offeredCompressionMethods, readUint8)) {
                clientHandshakeState.client.notifySelectedCompressionMethod(readUint8);
                clientHandshakeState.serverExtensions = TlsProtocol.readExtensions(byteArrayInputStream);
                if (clientHandshakeState.serverExtensions != null) {
                    Enumeration keys = clientHandshakeState.serverExtensions.keys();
                    while (keys.hasMoreElements()) {
                        Integer num = (Integer) keys.nextElement();
                        if (!num.equals(TlsProtocol.EXT_RenegotiationInfo)) {
                            if (TlsUtils.getExtensionData(clientHandshakeState.clientExtensions, num) != null) {
                                boolean z2 = clientHandshakeState.resumedSession;
                            } else {
                                throw new TlsFatalAlert(AlertDescription.unsupported_extension);
                            }
                        }
                    }
                }
                byte[] extensionData = TlsUtils.getExtensionData(clientHandshakeState.serverExtensions, TlsProtocol.EXT_RenegotiationInfo);
                if (extensionData != null) {
                    clientHandshakeState.secure_renegotiation = true;
                    if (!Arrays.constantTimeAreEqual(extensionData, TlsProtocol.createRenegotiationInfo(TlsUtils.EMPTY_BYTES))) {
                        throw new TlsFatalAlert(40);
                    }
                }
                clientHandshakeState.client.notifySecureRenegotiation(clientHandshakeState.secure_renegotiation);
                Hashtable hashtable = clientHandshakeState.clientExtensions;
                Hashtable hashtable2 = clientHandshakeState.serverExtensions;
                if (clientHandshakeState.resumedSession) {
                    if (readUint16 == clientHandshakeState.sessionParameters.getCipherSuite() && readUint8 == clientHandshakeState.sessionParameters.getCompressionAlgorithm()) {
                        hashtable = null;
                        hashtable2 = clientHandshakeState.sessionParameters.readServerExtensions();
                    } else {
                        throw new TlsFatalAlert(47);
                    }
                }
                securityParameters.cipherSuite = readUint16;
                securityParameters.compressionAlgorithm = readUint8;
                if (hashtable2 != null) {
                    boolean hasEncryptThenMACExtension = TlsExtensionsUtils.hasEncryptThenMACExtension(hashtable2);
                    if (!hasEncryptThenMACExtension || TlsUtils.isBlockCipherSuite(securityParameters.getCipherSuite())) {
                        securityParameters.encryptThenMAC = hasEncryptThenMACExtension;
                        securityParameters.extendedMasterSecret = TlsExtensionsUtils.hasExtendedMasterSecretExtension(hashtable2);
                        securityParameters.maxFragmentLength = evaluateMaxFragmentLengthExtension(clientHandshakeState.resumedSession, hashtable, hashtable2, 47);
                        securityParameters.truncatedHMac = TlsExtensionsUtils.hasTruncatedHMacExtension(hashtable2);
                        clientHandshakeState.allowCertificateStatus = !clientHandshakeState.resumedSession && TlsUtils.hasExpectedEmptyExtensionData(hashtable2, TlsExtensionsUtils.EXT_status_request, 47);
                        if (!clientHandshakeState.resumedSession && TlsUtils.hasExpectedEmptyExtensionData(hashtable2, TlsProtocol.EXT_SessionTicket, 47)) {
                            z = true;
                        }
                        clientHandshakeState.expectSessionTicket = z;
                    } else {
                        throw new TlsFatalAlert(47);
                    }
                }
                if (hashtable != null) {
                    clientHandshakeState.client.processServerExtensions(hashtable2);
                }
                securityParameters.prfAlgorithm = TlsProtocol.getPRFAlgorithm(clientHandshakeState.clientContext, securityParameters.getCipherSuite());
                securityParameters.verifyDataLength = 12;
                return;
            }
            throw new TlsFatalAlert(47);
        }
        throw new TlsFatalAlert(47);
    }

    /* access modifiers changed from: protected */
    public void processServerKeyExchange(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        clientHandshakeState.keyExchange.processServerKeyExchange(byteArrayInputStream);
        TlsProtocol.assertEmpty(byteArrayInputStream);
    }

    /* access modifiers changed from: protected */
    public void processServerSupplementalData(ClientHandshakeState clientHandshakeState, byte[] bArr) throws IOException {
        clientHandshakeState.client.processServerSupplementalData(TlsProtocol.readSupplementalDataMessage(new ByteArrayInputStream(bArr)));
    }

    /* access modifiers changed from: protected */
    public void reportServerVersion(ClientHandshakeState clientHandshakeState, ProtocolVersion protocolVersion) throws IOException {
        TlsClientContextImpl tlsClientContextImpl = clientHandshakeState.clientContext;
        ProtocolVersion serverVersion = tlsClientContextImpl.getServerVersion();
        if (serverVersion == null) {
            tlsClientContextImpl.setServerVersion(protocolVersion);
            clientHandshakeState.client.notifyServerVersion(protocolVersion);
        } else if (!serverVersion.equals(protocolVersion)) {
            throw new TlsFatalAlert(47);
        }
    }
}

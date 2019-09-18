package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeCrypto;
import com.android.org.conscrypt.SSLParametersImpl;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;

final class NativeSsl {
    private final SSLParametersImpl.AliasChooser aliasChooser;
    /* access modifiers changed from: private */
    public final NativeCrypto.SSLHandshakeCallbacks handshakeCallbacks;
    private X509Certificate[] localCertificates;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final SSLParametersImpl parameters;
    private final SSLParametersImpl.PSKCallbacks pskCallbacks;
    /* access modifiers changed from: private */
    public volatile long ssl;

    final class BioWrapper {
        private long bio;

        private BioWrapper() throws SSLException {
            this.bio = NativeCrypto.SSL_BIO_new(NativeSsl.this.ssl, NativeSsl.this);
        }

        /* access modifiers changed from: package-private */
        public int getPendingWrittenBytes() {
            return NativeCrypto.SSL_pending_written_bytes_in_BIO(this.bio);
        }

        /* access modifiers changed from: package-private */
        public int writeDirectByteBuffer(long address, int length) throws IOException {
            return NativeCrypto.ENGINE_SSL_write_BIO_direct(NativeSsl.this.ssl, NativeSsl.this, this.bio, address, length, NativeSsl.this.handshakeCallbacks);
        }

        /* access modifiers changed from: package-private */
        public int readDirectByteBuffer(long destAddress, int destLength) throws IOException {
            return NativeCrypto.ENGINE_SSL_read_BIO_direct(NativeSsl.this.ssl, NativeSsl.this, this.bio, destAddress, destLength, NativeSsl.this.handshakeCallbacks);
        }

        /* access modifiers changed from: package-private */
        public void close() {
            NativeCrypto.BIO_free_all(this.bio);
            this.bio = 0;
        }
    }

    private NativeSsl(long ssl2, SSLParametersImpl parameters2, NativeCrypto.SSLHandshakeCallbacks handshakeCallbacks2, SSLParametersImpl.AliasChooser aliasChooser2, SSLParametersImpl.PSKCallbacks pskCallbacks2) {
        this.ssl = ssl2;
        this.parameters = parameters2;
        this.handshakeCallbacks = handshakeCallbacks2;
        this.aliasChooser = aliasChooser2;
        this.pskCallbacks = pskCallbacks2;
    }

    static NativeSsl newInstance(SSLParametersImpl parameters2, NativeCrypto.SSLHandshakeCallbacks handshakeCallbacks2, SSLParametersImpl.AliasChooser chooser, SSLParametersImpl.PSKCallbacks pskCallbacks2) throws SSLException {
        AbstractSessionContext ctx = parameters2.getSessionContext();
        NativeSsl nativeSsl = new NativeSsl(NativeCrypto.SSL_new(ctx.sslCtxNativePointer, ctx), parameters2, handshakeCallbacks2, chooser, pskCallbacks2);
        return nativeSsl;
    }

    /* access modifiers changed from: package-private */
    public BioWrapper newBio() {
        try {
            return new BioWrapper();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public void offerToResumeSession(long sslSessionNativePointer) throws SSLException {
        NativeCrypto.SSL_set_session(this.ssl, this, sslSessionNativePointer);
    }

    /* access modifiers changed from: package-private */
    public byte[] getSessionId() {
        return NativeCrypto.SSL_session_id(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public long getTime() {
        return NativeCrypto.SSL_get_time(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public long getTimeout() {
        return NativeCrypto.SSL_get_timeout(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public void setTimeout(long millis) {
        NativeCrypto.SSL_set_timeout(this.ssl, this, millis);
    }

    /* access modifiers changed from: package-private */
    public String getCipherSuite() {
        return NativeCrypto.cipherSuiteToJava(NativeCrypto.SSL_get_current_cipher(this.ssl, this));
    }

    /* access modifiers changed from: package-private */
    public X509Certificate[] getPeerCertificates() throws CertificateException {
        byte[][] encoded = NativeCrypto.SSL_get0_peer_certificates(this.ssl, this);
        if (encoded == null) {
            return null;
        }
        return SSLUtils.decodeX509CertificateChain(encoded);
    }

    /* access modifiers changed from: package-private */
    public X509Certificate[] getLocalCertificates() {
        return this.localCertificates;
    }

    /* access modifiers changed from: package-private */
    public byte[] getPeerCertificateOcspData() {
        return NativeCrypto.SSL_get_ocsp_response(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public byte[] getTlsUnique() {
        return NativeCrypto.SSL_get_tls_unique(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public byte[] getPeerTlsSctData() {
        return NativeCrypto.SSL_get_signed_cert_timestamp_list(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public int clientPSKKeyRequested(String identityHint, byte[] identityBytesOut, byte[] key) {
        byte[] identityBytes;
        PSKKeyManager pskKeyManager = this.parameters.getPSKKeyManager();
        if (pskKeyManager == null) {
            return 0;
        }
        String identity = this.pskCallbacks.chooseClientPSKIdentity(pskKeyManager, identityHint);
        if (identity == null) {
            identity = "";
            identityBytes = EmptyArray.BYTE;
        } else if (identity.isEmpty()) {
            identityBytes = EmptyArray.BYTE;
        } else {
            try {
                identityBytes = identity.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported", e);
            }
        }
        if (identityBytes.length + 1 > identityBytesOut.length) {
            return 0;
        }
        if (identityBytes.length > 0) {
            System.arraycopy(identityBytes, 0, identityBytesOut, 0, identityBytes.length);
        }
        identityBytesOut[identityBytes.length] = 0;
        byte[] secretKeyBytes = this.pskCallbacks.getPSKKey(pskKeyManager, identityHint, identity).getEncoded();
        if (secretKeyBytes == null || secretKeyBytes.length > key.length) {
            return 0;
        }
        System.arraycopy(secretKeyBytes, 0, key, 0, secretKeyBytes.length);
        return secretKeyBytes.length;
    }

    /* access modifiers changed from: package-private */
    public int serverPSKKeyRequested(String identityHint, String identity, byte[] key) {
        PSKKeyManager pskKeyManager = this.parameters.getPSKKeyManager();
        if (pskKeyManager == null) {
            return 0;
        }
        byte[] secretKeyBytes = this.pskCallbacks.getPSKKey(pskKeyManager, identityHint, identity).getEncoded();
        if (secretKeyBytes == null || secretKeyBytes.length > key.length) {
            return 0;
        }
        System.arraycopy(secretKeyBytes, 0, key, 0, secretKeyBytes.length);
        return secretKeyBytes.length;
    }

    /* access modifiers changed from: package-private */
    public void chooseClientCertificate(byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws SSLException, CertificateEncodingException {
        X500Principal[] issuers;
        String alias;
        Set<String> keyTypesSet = SSLUtils.getSupportedClientKeyTypes(keyTypeBytes);
        String[] keyTypes = (String[]) keyTypesSet.toArray(new String[keyTypesSet.size()]);
        if (asn1DerEncodedPrincipals == null) {
            issuers = null;
        } else {
            issuers = new X500Principal[asn1DerEncodedPrincipals.length];
            for (int i = 0; i < asn1DerEncodedPrincipals.length; i++) {
                issuers[i] = new X500Principal(asn1DerEncodedPrincipals[i]);
            }
        }
        X509KeyManager keyManager = this.parameters.getX509KeyManager();
        if (keyManager != null) {
            alias = this.aliasChooser.chooseClientAlias(keyManager, issuers, keyTypes);
        } else {
            alias = null;
        }
        setCertificate(alias);
    }

    /* access modifiers changed from: package-private */
    public void setCertificate(String alias) throws CertificateEncodingException, SSLException {
        if (alias != null) {
            X509KeyManager keyManager = this.parameters.getX509KeyManager();
            if (keyManager != null) {
                PrivateKey privateKey = keyManager.getPrivateKey(alias);
                if (privateKey != null) {
                    this.localCertificates = keyManager.getCertificateChain(alias);
                    if (this.localCertificates != null) {
                        int numLocalCerts = this.localCertificates.length;
                        PublicKey publicKey = numLocalCerts > 0 ? this.localCertificates[0].getPublicKey() : null;
                        byte[][] encodedLocalCerts = new byte[numLocalCerts][];
                        for (int i = 0; i < numLocalCerts; i++) {
                            encodedLocalCerts[i] = this.localCertificates[i].getEncoded();
                        }
                        try {
                            NativeCrypto.setLocalCertsAndPrivateKey(this.ssl, this, encodedLocalCerts, OpenSSLKey.fromPrivateKeyForTLSStackOnly(privateKey, publicKey).getNativeRef());
                        } catch (InvalidKeyException e) {
                            throw new SSLException(e);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String getVersion() {
        return NativeCrypto.SSL_get_version(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public String getRequestedServerName() {
        return NativeCrypto.SSL_get_servername(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public byte[] getTlsChannelId() throws SSLException {
        return NativeCrypto.SSL_get_tls_channel_id(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public void initialize(String hostname, OpenSSLKey channelIdPrivateKey) throws IOException {
        if (!this.parameters.getEnableSessionCreation()) {
            NativeCrypto.SSL_set_session_creation_enabled(this.ssl, this, false);
        }
        NativeCrypto.SSL_accept_renegotiations(this.ssl, this);
        if (isClient()) {
            NativeCrypto.SSL_set_connect_state(this.ssl, this);
            NativeCrypto.SSL_enable_ocsp_stapling(this.ssl, this);
            if (this.parameters.isCTVerificationEnabled(hostname)) {
                NativeCrypto.SSL_enable_signed_cert_timestamps(this.ssl, this);
            }
        } else {
            NativeCrypto.SSL_set_accept_state(this.ssl, this);
            if (this.parameters.getOCSPResponse() != null) {
                NativeCrypto.SSL_enable_ocsp_stapling(this.ssl, this);
            }
        }
        if (this.parameters.getEnabledProtocols().length != 0 || !this.parameters.isEnabledProtocolsFiltered) {
            NativeCrypto.setEnabledProtocols(this.ssl, this, this.parameters.enabledProtocols);
            NativeCrypto.setEnabledCipherSuites(this.ssl, this, this.parameters.enabledCipherSuites);
            if (this.parameters.applicationProtocols.length > 0) {
                NativeCrypto.setApplicationProtocols(this.ssl, this, isClient(), this.parameters.applicationProtocols);
            }
            if (!isClient() && this.parameters.applicationProtocolSelector != null) {
                NativeCrypto.setApplicationProtocolSelector(this.ssl, this, this.parameters.applicationProtocolSelector);
            }
            if (!isClient()) {
                Set<String> keyTypes = new HashSet<>();
                for (long sslCipherNativePointer : NativeCrypto.SSL_get_ciphers(this.ssl, this)) {
                    String keyType = SSLUtils.getServerX509KeyType(sslCipherNativePointer);
                    if (keyType != null) {
                        keyTypes.add(keyType);
                    }
                }
                X509KeyManager keyManager = this.parameters.getX509KeyManager();
                if (keyManager != null) {
                    for (String keyType2 : keyTypes) {
                        try {
                            setCertificate(this.aliasChooser.chooseServerAlias(keyManager, keyType2));
                        } catch (CertificateEncodingException e) {
                            throw new IOException(e);
                        }
                    }
                }
                NativeCrypto.SSL_set_options(this.ssl, this, 4194304);
                if (this.parameters.sctExtension != null) {
                    NativeCrypto.SSL_set_signed_cert_timestamp_list(this.ssl, this, this.parameters.sctExtension);
                }
                if (this.parameters.ocspResponse != null) {
                    NativeCrypto.SSL_set_ocsp_response(this.ssl, this, this.parameters.ocspResponse);
                }
            }
            enablePSKKeyManagerIfRequested();
            if (this.parameters.useSessionTickets) {
                NativeCrypto.SSL_clear_options(this.ssl, this, 16384);
            } else {
                NativeCrypto.SSL_set_options(this.ssl, this, NativeCrypto.SSL_get_options(this.ssl, this) | 16384);
            }
            if (this.parameters.getUseSni() && AddressUtils.isValidSniHostname(hostname)) {
                NativeCrypto.SSL_set_tlsext_host_name(this.ssl, this, hostname);
            }
            NativeCrypto.SSL_set_mode(this.ssl, this, 256);
            setCertificateValidation();
            setTlsChannelId(channelIdPrivateKey);
            return;
        }
        throw new SSLHandshakeException("No enabled protocols; SSLv3 is no longer supported and was filtered from the list");
    }

    /* access modifiers changed from: package-private */
    public void doHandshake(FileDescriptor fd, int timeoutMillis) throws CertificateException, IOException {
        this.lock.readLock().lock();
        try {
            if (isClosed() || fd == null || !fd.valid()) {
                throw new SocketException("Socket is closed");
            }
            NativeCrypto.SSL_do_handshake(this.ssl, this, fd, this.handshakeCallbacks, timeoutMillis);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public int doHandshake() throws IOException {
        this.lock.readLock().lock();
        try {
            return NativeCrypto.ENGINE_SSL_do_handshake(this.ssl, this, this.handshakeCallbacks);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public int read(FileDescriptor fd, byte[] buf, int offset, int len, int timeoutMillis) throws IOException {
        this.lock.readLock().lock();
        try {
            if (isClosed() || fd == null || !fd.valid()) {
                throw new SocketException("Socket is closed");
            }
            return NativeCrypto.SSL_read(this.ssl, this, fd, this.handshakeCallbacks, buf, offset, len, timeoutMillis);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public void write(FileDescriptor fd, byte[] buf, int offset, int len, int timeoutMillis) throws IOException {
        this.lock.readLock().lock();
        try {
            if (isClosed() || fd == null || !fd.valid()) {
                throw new SocketException("Socket is closed");
            }
            NativeCrypto.SSL_write(this.ssl, this, fd, this.handshakeCallbacks, buf, offset, len, timeoutMillis);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private void enablePSKKeyManagerIfRequested() throws SSLException {
        PSKKeyManager pskKeyManager = this.parameters.getPSKKeyManager();
        if (pskKeyManager != null) {
            boolean pskEnabled = false;
            String[] strArr = this.parameters.enabledCipherSuites;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String enabledCipherSuite = strArr[i];
                if (enabledCipherSuite != null && enabledCipherSuite.contains("PSK")) {
                    pskEnabled = true;
                    break;
                }
                i++;
            }
            if (!pskEnabled) {
                return;
            }
            if (isClient()) {
                NativeCrypto.set_SSL_psk_client_callback_enabled(this.ssl, this, true);
                return;
            }
            NativeCrypto.set_SSL_psk_server_callback_enabled(this.ssl, this, true);
            NativeCrypto.SSL_use_psk_identity_hint(this.ssl, this, this.pskCallbacks.chooseServerPSKIdentityHint(pskKeyManager));
        }
    }

    private void setTlsChannelId(OpenSSLKey channelIdPrivateKey) throws SSLException {
        if (this.parameters.channelIdEnabled) {
            if (!this.parameters.getUseClientMode()) {
                NativeCrypto.SSL_enable_tls_channel_id(this.ssl, this);
            } else if (channelIdPrivateKey != null) {
                NativeCrypto.SSL_set1_tls_channel_id(this.ssl, this, channelIdPrivateKey.getNativeRef());
            } else {
                throw new SSLHandshakeException("Invalid TLS channel ID key specified");
            }
        }
    }

    private void setCertificateValidation() throws SSLException {
        if (!isClient()) {
            boolean certRequested = false;
            if (this.parameters.getNeedClientAuth()) {
                NativeCrypto.SSL_set_verify(this.ssl, this, 3);
                certRequested = true;
            } else if (this.parameters.getWantClientAuth()) {
                NativeCrypto.SSL_set_verify(this.ssl, this, 1);
                certRequested = true;
            } else {
                NativeCrypto.SSL_set_verify(this.ssl, this, 0);
            }
            if (certRequested) {
                X509Certificate[] issuers = this.parameters.getX509TrustManager().getAcceptedIssuers();
                if (issuers != null && issuers.length != 0) {
                    try {
                        NativeCrypto.SSL_set_client_CA_list(this.ssl, this, SSLUtils.encodeIssuerX509Principals(issuers));
                    } catch (CertificateEncodingException e) {
                        throw new SSLException("Problem encoding principals", e);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void interrupt() {
        NativeCrypto.SSL_interrupt(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public void shutdown(FileDescriptor fd) throws IOException {
        NativeCrypto.SSL_shutdown(this.ssl, this, fd, this.handshakeCallbacks);
    }

    /* access modifiers changed from: package-private */
    public void shutdown() throws IOException {
        NativeCrypto.ENGINE_SSL_shutdown(this.ssl, this, this.handshakeCallbacks);
    }

    /* access modifiers changed from: package-private */
    public boolean wasShutdownReceived() {
        return (NativeCrypto.SSL_get_shutdown(this.ssl, this) & 2) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean wasShutdownSent() {
        return (NativeCrypto.SSL_get_shutdown(this.ssl, this) & 1) != 0;
    }

    /* access modifiers changed from: package-private */
    public int readDirectByteBuffer(long destAddress, int destLength) throws IOException, CertificateException {
        this.lock.readLock().lock();
        try {
            return NativeCrypto.ENGINE_SSL_read_direct(this.ssl, this, destAddress, destLength, this.handshakeCallbacks);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public int writeDirectByteBuffer(long sourceAddress, int sourceLength) throws IOException {
        this.lock.readLock().lock();
        try {
            return NativeCrypto.ENGINE_SSL_write_direct(this.ssl, this, sourceAddress, sourceLength, this.handshakeCallbacks);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public int getPendingReadableBytes() {
        return NativeCrypto.SSL_pending_readable_bytes(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public int getMaxSealOverhead() {
        return NativeCrypto.SSL_max_seal_overhead(this.ssl, this);
    }

    /* access modifiers changed from: package-private */
    public void close() {
        this.lock.writeLock().lock();
        try {
            if (!isClosed()) {
                long toFree = this.ssl;
                this.ssl = 0;
                NativeCrypto.SSL_free(toFree, this);
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isClosed() {
        return this.ssl == 0;
    }

    /* access modifiers changed from: package-private */
    public int getError(int result) {
        return NativeCrypto.SSL_get_error(this.ssl, this, result);
    }

    /* access modifiers changed from: package-private */
    public byte[] getApplicationProtocol() {
        return NativeCrypto.getApplicationProtocol(this.ssl, this);
    }

    private boolean isClient() {
        return this.parameters.getUseClientMode();
    }

    /* access modifiers changed from: protected */
    public final void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}

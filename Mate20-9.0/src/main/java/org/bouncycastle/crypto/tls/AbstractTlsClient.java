package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public abstract class AbstractTlsClient extends AbstractTlsPeer implements TlsClient {
    protected TlsCipherFactory cipherFactory;
    protected short[] clientECPointFormats;
    protected TlsClientContext context;
    protected int[] namedCurves;
    protected int selectedCipherSuite;
    protected short selectedCompressionMethod;
    protected short[] serverECPointFormats;
    protected Vector supportedSignatureAlgorithms;

    public AbstractTlsClient() {
        this(new DefaultTlsCipherFactory());
    }

    public AbstractTlsClient(TlsCipherFactory tlsCipherFactory) {
        this.cipherFactory = tlsCipherFactory;
    }

    /* access modifiers changed from: protected */
    public boolean allowUnexpectedServerExtension(Integer num, byte[] bArr) throws IOException {
        switch (num.intValue()) {
            case 10:
                TlsECCUtils.readSupportedEllipticCurvesExtension(bArr);
                return true;
            case 11:
                TlsECCUtils.readSupportedPointFormatsExtension(bArr);
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public void checkForUnexpectedServerExtension(Hashtable hashtable, Integer num) throws IOException {
        byte[] extensionData = TlsUtils.getExtensionData(hashtable, num);
        if (extensionData != null && !allowUnexpectedServerExtension(num, extensionData)) {
            throw new TlsFatalAlert(47);
        }
    }

    public TlsCipher getCipher() throws IOException {
        return this.cipherFactory.createCipher(this.context, TlsUtils.getEncryptionAlgorithm(this.selectedCipherSuite), TlsUtils.getMACAlgorithm(this.selectedCipherSuite));
    }

    public Hashtable getClientExtensions() throws IOException {
        Hashtable hashtable = null;
        if (TlsUtils.isSignatureAlgorithmsExtensionAllowed(this.context.getClientVersion())) {
            this.supportedSignatureAlgorithms = TlsUtils.getDefaultSupportedSignatureAlgorithms();
            hashtable = TlsExtensionsUtils.ensureExtensionsInitialised(null);
            TlsUtils.addSignatureAlgorithmsExtension(hashtable, this.supportedSignatureAlgorithms);
        }
        if (!TlsECCUtils.containsECCCipherSuites(getCipherSuites())) {
            return hashtable;
        }
        this.namedCurves = new int[]{23, 24};
        this.clientECPointFormats = new short[]{0, 1, 2};
        Hashtable ensureExtensionsInitialised = TlsExtensionsUtils.ensureExtensionsInitialised(hashtable);
        TlsECCUtils.addSupportedEllipticCurvesExtension(ensureExtensionsInitialised, this.namedCurves);
        TlsECCUtils.addSupportedPointFormatsExtension(ensureExtensionsInitialised, this.clientECPointFormats);
        return ensureExtensionsInitialised;
    }

    public ProtocolVersion getClientHelloRecordLayerVersion() {
        return getClientVersion();
    }

    public Vector getClientSupplementalData() throws IOException {
        return null;
    }

    public ProtocolVersion getClientVersion() {
        return ProtocolVersion.TLSv12;
    }

    public TlsCompression getCompression() throws IOException {
        if (this.selectedCompressionMethod == 0) {
            return new TlsNullCompression();
        }
        throw new TlsFatalAlert(80);
    }

    public short[] getCompressionMethods() {
        return new short[]{0};
    }

    public ProtocolVersion getMinimumVersion() {
        return ProtocolVersion.TLSv10;
    }

    public TlsSession getSessionToResume() {
        return null;
    }

    public void init(TlsClientContext tlsClientContext) {
        this.context = tlsClientContext;
    }

    public boolean isFallback() {
        return false;
    }

    public void notifyNewSessionTicket(NewSessionTicket newSessionTicket) throws IOException {
    }

    public void notifySelectedCipherSuite(int i) {
        this.selectedCipherSuite = i;
    }

    public void notifySelectedCompressionMethod(short s) {
        this.selectedCompressionMethod = s;
    }

    public void notifyServerVersion(ProtocolVersion protocolVersion) throws IOException {
        if (!getMinimumVersion().isEqualOrEarlierVersionOf(protocolVersion)) {
            throw new TlsFatalAlert(70);
        }
    }

    public void notifySessionID(byte[] bArr) {
    }

    public void processServerExtensions(Hashtable hashtable) throws IOException {
        if (hashtable != null) {
            checkForUnexpectedServerExtension(hashtable, TlsUtils.EXT_signature_algorithms);
            checkForUnexpectedServerExtension(hashtable, TlsECCUtils.EXT_elliptic_curves);
            if (TlsECCUtils.isECCCipherSuite(this.selectedCipherSuite)) {
                this.serverECPointFormats = TlsECCUtils.getSupportedPointFormatsExtension(hashtable);
            } else {
                checkForUnexpectedServerExtension(hashtable, TlsECCUtils.EXT_ec_point_formats);
            }
            checkForUnexpectedServerExtension(hashtable, TlsExtensionsUtils.EXT_padding);
        }
    }

    public void processServerSupplementalData(Vector vector) throws IOException {
        if (vector != null) {
            throw new TlsFatalAlert(10);
        }
    }
}

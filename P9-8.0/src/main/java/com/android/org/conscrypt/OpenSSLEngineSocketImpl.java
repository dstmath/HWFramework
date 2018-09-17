package com.android.org.conscrypt;

import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;

final class OpenSSLEngineSocketImpl extends OpenSSLSocketImplWrapper {
    private static final /* synthetic */ int[] -javax-net-ssl-SSLEngineResult$HandshakeStatusSwitchesValues = null;
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private final OpenSSLEngineImpl engine;
    private boolean handshakeComplete;
    private final InputStreamWrapper inputStreamWrapper = new InputStreamWrapper();
    private final OutputStreamWrapper outputStreamWrapper = new OutputStreamWrapper();
    private final Socket socket;

    private final class InputStreamWrapper extends InputStream {
        private static final /* synthetic */ int[] -javax-net-ssl-SSLEngineResult$StatusSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$javax$net$ssl$SSLEngineResult$Status;
        private final ByteBuffer fromEngine;
        private ByteBuffer fromSocket;
        private final byte[] singleByte = new byte[1];
        private SocketChannel socketChannel;
        private InputStream socketInputStream;
        private final Object stateLock = new Object();

        private static /* synthetic */ int[] -getjavax-net-ssl-SSLEngineResult$StatusSwitchesValues() {
            if (-javax-net-ssl-SSLEngineResult$StatusSwitchesValues != null) {
                return -javax-net-ssl-SSLEngineResult$StatusSwitchesValues;
            }
            int[] iArr = new int[Status.values().length];
            try {
                iArr[Status.BUFFER_OVERFLOW.ordinal()] = 4;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Status.BUFFER_UNDERFLOW.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Status.CLOSED.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Status.OK.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            -javax-net-ssl-SSLEngineResult$StatusSwitchesValues = iArr;
            return iArr;
        }

        InputStreamWrapper() {
            this.fromEngine = ByteBuffer.allocateDirect(OpenSSLEngineSocketImpl.this.engine.getSession().getApplicationBufferSize());
            this.fromEngine.flip();
        }

        public int read() throws IOException {
            synchronized (this.stateLock) {
                int count = read(this.singleByte, 0, 1);
                if (count == -1) {
                    return -1;
                } else if (count != 1) {
                    throw new SSLException("read incorrect number of bytes " + count);
                } else {
                    byte b = this.singleByte[0];
                    return b;
                }
            }
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            synchronized (this.stateLock) {
                try {
                    init();
                    while (this.fromEngine.remaining() <= 0) {
                        boolean needMoreData = true;
                        if (this.fromSocket.position() > 0) {
                            this.fromSocket.flip();
                            this.fromEngine.clear();
                            SSLEngineResult engineResult = OpenSSLEngineSocketImpl.this.engine.unwrap(this.fromSocket, this.fromEngine);
                            this.fromSocket.compact();
                            this.fromEngine.flip();
                            switch (-getjavax-net-ssl-SSLEngineResult$StatusSwitchesValues()[engineResult.getStatus().ordinal()]) {
                                case 1:
                                    if (engineResult.bytesProduced() != 0) {
                                        needMoreData = false;
                                        break;
                                    }
                                    break;
                                case 2:
                                    return -1;
                                case 3:
                                    needMoreData = false;
                                    break;
                                default:
                                    throw new SSLException("Unexpected engine result " + engineResult.getStatus());
                            }
                            if (!needMoreData && engineResult.bytesProduced() == 0) {
                                return 0;
                            }
                        }
                        if (needMoreData) {
                            if (readFromSocket() == -1) {
                                return -1;
                            }
                        }
                    }
                    int readFromEngine = Math.min(this.fromEngine.remaining(), len);
                    this.fromEngine.get(b, off, readFromEngine);
                    return readFromEngine;
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                    throw e2;
                }
            }
        }

        private void init() throws IOException {
            if (this.socketInputStream == null) {
                this.socketInputStream = OpenSSLEngineSocketImpl.this.socket.getInputStream();
                this.socketChannel = OpenSSLEngineSocketImpl.this.socket.getChannel();
                if (this.socketChannel != null) {
                    this.fromSocket = ByteBuffer.allocateDirect(OpenSSLEngineSocketImpl.this.engine.getSession().getPacketBufferSize());
                } else {
                    this.fromSocket = ByteBuffer.allocate(OpenSSLEngineSocketImpl.this.engine.getSession().getPacketBufferSize());
                }
            }
        }

        private int readFromSocket() throws IOException {
            if (this.socketChannel != null) {
                return this.socketChannel.read(this.fromSocket);
            }
            int read = this.socketInputStream.read(this.fromSocket.array(), this.fromSocket.position(), this.fromSocket.remaining());
            if (read > 0) {
                this.fromSocket.position(this.fromSocket.position() + read);
            }
            return read;
        }
    }

    private final class OutputStreamWrapper extends OutputStream {
        private SocketChannel socketChannel;
        private OutputStream socketOutputStream;
        private final Object stateLock = new Object();
        private ByteBuffer target;

        OutputStreamWrapper() {
        }

        public void write(int b) throws IOException {
            write(new byte[]{(byte) b});
        }

        public void write(byte[] b) throws IOException {
            write(ByteBuffer.wrap(b));
        }

        public void write(byte[] b, int off, int len) throws IOException {
            write(ByteBuffer.wrap(b, off, len));
        }

        private void write(ByteBuffer buffer) throws IOException {
            synchronized (this.stateLock) {
                try {
                    init();
                    int len = buffer.remaining();
                    do {
                        this.target.clear();
                        SSLEngineResult engineResult = OpenSSLEngineSocketImpl.this.engine.wrap(buffer, this.target);
                        if (engineResult.getStatus() != Status.OK) {
                            throw new SSLException("Unexpected engine result " + engineResult.getStatus());
                        } else if (this.target.position() != engineResult.bytesProduced()) {
                            throw new SSLException("Engine bytesProduced " + engineResult.bytesProduced() + " does not match bytes written " + this.target.position());
                        } else {
                            len -= engineResult.bytesConsumed();
                            if (len != buffer.remaining()) {
                                throw new SSLException("Engine did not read the correct number of bytes");
                            }
                            this.target.flip();
                            if (this.socketChannel != null) {
                                while (this.target.hasRemaining()) {
                                    this.socketChannel.write(this.target);
                                }
                                continue;
                            } else {
                                this.socketOutputStream.write(this.target.array(), 0, this.target.limit());
                                continue;
                            }
                        }
                    } while (len > 0);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                    throw e2;
                }
            }
        }

        public void flush() throws IOException {
            synchronized (this.stateLock) {
                init();
                this.socketOutputStream.flush();
            }
        }

        public void close() throws IOException {
            OpenSSLEngineSocketImpl.this.socket.close();
        }

        private void init() throws IOException {
            if (this.socketOutputStream == null) {
                this.socketOutputStream = OpenSSLEngineSocketImpl.this.socket.getOutputStream();
                this.socketChannel = OpenSSLEngineSocketImpl.this.socket.getChannel();
                if (this.socketChannel != null) {
                    this.target = ByteBuffer.allocateDirect(OpenSSLEngineSocketImpl.this.engine.getSession().getPacketBufferSize());
                } else {
                    this.target = ByteBuffer.allocate(OpenSSLEngineSocketImpl.this.engine.getSession().getPacketBufferSize());
                }
            }
        }
    }

    private static /* synthetic */ int[] -getjavax-net-ssl-SSLEngineResult$HandshakeStatusSwitchesValues() {
        if (-javax-net-ssl-SSLEngineResult$HandshakeStatusSwitchesValues != null) {
            return -javax-net-ssl-SSLEngineResult$HandshakeStatusSwitchesValues;
        }
        int[] iArr = new int[HandshakeStatus.values().length];
        try {
            iArr[HandshakeStatus.FINISHED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[HandshakeStatus.NEED_TASK.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[HandshakeStatus.NEED_UNWRAP.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[HandshakeStatus.NEED_WRAP.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[HandshakeStatus.NOT_HANDSHAKING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -javax-net-ssl-SSLEngineResult$HandshakeStatusSwitchesValues = iArr;
        return iArr;
    }

    OpenSSLEngineSocketImpl(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        super(socket, hostname, port, autoClose, sslParameters);
        this.socket = socket;
        this.engine = new OpenSSLEngineImpl(hostname, port, sslParameters);
        this.engine.setHandshakeListener(new HandshakeListener() {
            public void onHandshakeFinished() {
                if (!OpenSSLEngineSocketImpl.this.handshakeComplete) {
                    OpenSSLEngineSocketImpl.this.handshakeComplete = true;
                    OpenSSLEngineSocketImpl.this.notifyHandshakeCompletedListeners();
                }
            }
        });
        this.engine.setUseClientMode(sslParameters.getUseClientMode());
    }

    public void startHandshake() throws IOException {
        boolean beginHandshakeCalled = false;
        while (!this.handshakeComplete) {
            switch (-getjavax-net-ssl-SSLEngineResult$HandshakeStatusSwitchesValues()[this.engine.getHandshakeStatus().ordinal()]) {
                case 1:
                    return;
                case 2:
                    throw new IllegalStateException("OpenSSLEngineImpl returned NEED_TASK");
                case 3:
                    if (this.inputStreamWrapper.read(EmptyArray.BYTE) != -1) {
                        break;
                    }
                    throw new EOFException();
                case 4:
                    this.outputStreamWrapper.write(EMPTY_BUFFER);
                    break;
                case NativeConstants.SSL3_RT_HEADER_LENGTH /*5*/:
                    if (!beginHandshakeCalled) {
                        beginHandshakeCalled = true;
                        this.engine.beginHandshake();
                        break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void onSSLStateChange(int type, int val) {
        throw new AssertionError("Should be handled by engine");
    }

    public void verifyCertificateChain(long[] certRefs, String authMethod) throws CertificateException {
        throw new AssertionError("Should be handled by engine");
    }

    public InputStream getInputStream() throws IOException {
        return this.inputStreamWrapper;
    }

    public OutputStream getOutputStream() throws IOException {
        return this.outputStreamWrapper;
    }

    public SSLSession getSession() {
        return this.engine.getSession();
    }

    public boolean getEnableSessionCreation() {
        return super.getEnableSessionCreation();
    }

    public void setEnableSessionCreation(boolean flag) {
        super.setEnableSessionCreation(flag);
    }

    public String[] getSupportedCipherSuites() {
        return super.getSupportedCipherSuites();
    }

    public String[] getEnabledCipherSuites() {
        return super.getEnabledCipherSuites();
    }

    public void setEnabledCipherSuites(String[] suites) {
        super.setEnabledCipherSuites(suites);
    }

    public String[] getSupportedProtocols() {
        return super.getSupportedProtocols();
    }

    public String[] getEnabledProtocols() {
        return super.getEnabledProtocols();
    }

    public void setEnabledProtocols(String[] protocols) {
        super.setEnabledProtocols(protocols);
    }

    public void setUseSessionTickets(boolean useSessionTickets) {
        super.setUseSessionTickets(useSessionTickets);
    }

    public void setHostname(String hostname) {
        super.setHostname(hostname);
    }

    public void setChannelIdEnabled(boolean enabled) {
        super.setChannelIdEnabled(enabled);
    }

    public byte[] getChannelId() throws SSLException {
        return super.getChannelId();
    }

    public void setChannelIdPrivateKey(PrivateKey privateKey) {
        super.setChannelIdPrivateKey(privateKey);
    }

    public boolean getUseClientMode() {
        return super.getUseClientMode();
    }

    public void setUseClientMode(boolean mode) {
        this.engine.setUseClientMode(mode);
    }

    public boolean getWantClientAuth() {
        return super.getWantClientAuth();
    }

    public boolean getNeedClientAuth() {
        return super.getNeedClientAuth();
    }

    public void setNeedClientAuth(boolean need) {
        super.setNeedClientAuth(need);
    }

    public void setWantClientAuth(boolean want) {
        super.setWantClientAuth(want);
    }

    public void sendUrgentData(int data) throws IOException {
        super.sendUrgentData(data);
    }

    public void setOOBInline(boolean on) throws SocketException {
        super.setOOBInline(on);
    }

    public void setSoWriteTimeout(int writeTimeoutMilliseconds) throws SocketException {
        throw new UnsupportedOperationException("Not supported");
    }

    public int getSoWriteTimeout() throws SocketException {
        return 0;
    }

    public void setHandshakeTimeout(int handshakeTimeoutMilliseconds) throws SocketException {
        throw new UnsupportedOperationException("Not supported");
    }

    public synchronized void close() throws IOException {
        this.engine.closeInbound();
        this.engine.closeOutbound();
        this.socket.close();
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public SocketChannel getChannel() {
        return super.getChannel();
    }

    public FileDescriptor getFileDescriptor$() {
        throw new UnsupportedOperationException("Not supported");
    }

    public byte[] getNpnSelectedProtocol() {
        return null;
    }

    public byte[] getAlpnSelectedProtocol() {
        return this.engine.getAlpnSelectedProtocol();
    }

    public void setNpnProtocols(byte[] npnProtocols) {
        super.setNpnProtocols(npnProtocols);
    }

    public void setAlpnProtocols(byte[] alpnProtocols) {
        super.setAlpnProtocols(alpnProtocols);
    }

    public String chooseServerAlias(X509KeyManager keyManager, String keyType) {
        return this.engine.chooseServerAlias(keyManager, keyType);
    }

    public String chooseClientAlias(X509KeyManager keyManager, X500Principal[] issuers, String[] keyTypes) {
        return this.engine.chooseClientAlias(keyManager, issuers, keyTypes);
    }

    public String chooseServerPSKIdentityHint(PSKKeyManager keyManager) {
        return this.engine.chooseServerPSKIdentityHint(keyManager);
    }

    public String chooseClientPSKIdentity(PSKKeyManager keyManager, String identityHint) {
        return this.engine.chooseClientPSKIdentity(keyManager, identityHint);
    }

    public SecretKey getPSKKey(PSKKeyManager keyManager, String identityHint, String identity) {
        return this.engine.getPSKKey(keyManager, identityHint, identity);
    }
}

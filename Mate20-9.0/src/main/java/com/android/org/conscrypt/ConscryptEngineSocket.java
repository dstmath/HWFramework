package com.android.org.conscrypt;

import com.android.org.conscrypt.ct.CTConstants;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

class ConscryptEngineSocket extends OpenSSLSocketImpl {
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    /* access modifiers changed from: private */
    public final ConscryptEngine engine;
    /* access modifiers changed from: private */
    public final Object handshakeLock = new Object();
    private SSLInputStream in;
    private SSLOutputStream out;
    /* access modifiers changed from: private */
    public int state = 0;
    /* access modifiers changed from: private */
    public final Object stateLock = new Object();

    /* renamed from: com.android.org.conscrypt.ConscryptEngineSocket$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus = new int[SSLEngineResult.HandshakeStatus.values().length];
        static final /* synthetic */ int[] $SwitchMap$javax$net$ssl$SSLEngineResult$Status = new int[SSLEngineResult.Status.values().length];

        static {
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.BUFFER_UNDERFLOW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.OK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$Status[SSLEngineResult.Status.CLOSED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_UNWRAP.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_WRAP.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NEED_TASK.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[SSLEngineResult.HandshakeStatus.FINISHED.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    private final class SSLInputStream extends InputStream {
        private final ByteBuffer fromEngine;
        private final ByteBuffer fromSocket;
        private final int fromSocketArrayOffset;
        private final Object readLock = new Object();
        private final byte[] singleByte = new byte[1];
        private InputStream socketInputStream;

        SSLInputStream() {
            this.fromEngine = ByteBuffer.allocateDirect(ConscryptEngineSocket.this.engine.getSession().getApplicationBufferSize());
            this.fromEngine.flip();
            this.fromSocket = ByteBuffer.allocate(ConscryptEngineSocket.this.engine.getSession().getPacketBufferSize());
            this.fromSocketArrayOffset = this.fromSocket.arrayOffset();
        }

        public void close() throws IOException {
            ConscryptEngineSocket.this.close();
        }

        public int read() throws IOException {
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.readLock) {
                int count = read(this.singleByte, 0, 1);
                if (count == -1) {
                    return -1;
                }
                if (count == 1) {
                    byte b = this.singleByte[0];
                    return b;
                }
                throw new SSLException("read incorrect number of bytes " + count);
            }
        }

        public int read(byte[] b) throws IOException {
            int read;
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.readLock) {
                read = read(b, 0, b.length);
            }
            return read;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int readInternal;
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.readLock) {
                readInternal = readInternal(b, off, len);
            }
            return readInternal;
        }

        public int available() throws IOException {
            int i;
            int i2;
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.readLock) {
                init();
                int remaining = this.fromEngine.remaining();
                if (!this.fromSocket.hasRemaining()) {
                    if (this.socketInputStream.available() <= 0) {
                        i = 0;
                        i2 = remaining + i;
                    }
                }
                i = 1;
                i2 = remaining + i;
            }
            return i2;
        }

        private boolean isHandshaking(SSLEngineResult.HandshakeStatus status) {
            switch (AnonymousClass2.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[status.ordinal()]) {
                case 1:
                case 2:
                case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                    return true;
                default:
                    return false;
            }
        }

        /* access modifiers changed from: private */
        public int readInternal(byte[] b, int off, int len) throws IOException {
            Platform.blockGuardOnNetwork();
            ConscryptEngineSocket.this.checkOpen();
            init();
            while (this.fromEngine.remaining() <= 0) {
                boolean needMoreDataFromSocket = true;
                this.fromSocket.flip();
                this.fromEngine.clear();
                boolean engineHandshaking = isHandshaking(ConscryptEngineSocket.this.engine.getHandshakeStatus());
                SSLEngineResult engineResult = ConscryptEngineSocket.this.engine.unwrap(this.fromSocket, this.fromEngine);
                this.fromSocket.compact();
                this.fromEngine.flip();
                switch (AnonymousClass2.$SwitchMap$javax$net$ssl$SSLEngineResult$Status[engineResult.getStatus().ordinal()]) {
                    case 1:
                        if (engineResult.bytesProduced() != 0) {
                            needMoreDataFromSocket = false;
                            break;
                        }
                        break;
                    case 2:
                        if (engineHandshaking || !isHandshaking(engineResult.getHandshakeStatus()) || !isHandshakeFinished()) {
                            needMoreDataFromSocket = false;
                            break;
                        } else {
                            renegotiate();
                            return 0;
                        }
                        break;
                    case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                        return -1;
                    default:
                        throw new SSLException("Unexpected engine result " + engineResult.getStatus());
                }
                if (!needMoreDataFromSocket && engineResult.bytesProduced() == 0) {
                    return 0;
                }
                if (needMoreDataFromSocket && readFromSocket() == -1) {
                    return -1;
                }
            }
            int readFromEngine = Math.min(this.fromEngine.remaining(), len);
            this.fromEngine.get(b, off, readFromEngine);
            return readFromEngine;
        }

        private boolean isHandshakeFinished() {
            boolean z;
            synchronized (ConscryptEngineSocket.this.stateLock) {
                z = ConscryptEngineSocket.this.state >= 4;
            }
            return z;
        }

        private void renegotiate() throws IOException {
            synchronized (ConscryptEngineSocket.this.handshakeLock) {
                ConscryptEngineSocket.this.doHandshake();
            }
        }

        private void init() throws IOException {
            if (this.socketInputStream == null) {
                this.socketInputStream = ConscryptEngineSocket.this.getUnderlyingInputStream();
            }
        }

        private int readFromSocket() throws IOException {
            try {
                int pos = this.fromSocket.position();
                int read = this.socketInputStream.read(this.fromSocket.array(), this.fromSocketArrayOffset + pos, this.fromSocket.limit() - pos);
                if (read > 0) {
                    this.fromSocket.position(pos + read);
                }
                return read;
            } catch (EOFException e) {
                return -1;
            }
        }
    }

    private final class SSLOutputStream extends OutputStream {
        private OutputStream socketOutputStream;
        private final ByteBuffer target;
        private final int targetArrayOffset;
        private final Object writeLock = new Object();

        SSLOutputStream() {
            this.target = ByteBuffer.allocate(ConscryptEngineSocket.this.engine.getSession().getPacketBufferSize());
            this.targetArrayOffset = this.target.arrayOffset();
        }

        public void close() throws IOException {
            ConscryptEngineSocket.this.close();
        }

        public void write(int b) throws IOException {
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.writeLock) {
                write(new byte[]{(byte) b});
            }
        }

        public void write(byte[] b) throws IOException {
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.writeLock) {
                writeInternal(ByteBuffer.wrap(b));
            }
        }

        public void write(byte[] b, int off, int len) throws IOException {
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.writeLock) {
                writeInternal(ByteBuffer.wrap(b, off, len));
            }
        }

        /* access modifiers changed from: private */
        public void writeInternal(ByteBuffer buffer) throws IOException {
            Platform.blockGuardOnNetwork();
            ConscryptEngineSocket.this.checkOpen();
            init();
            int len = buffer.remaining();
            do {
                this.target.clear();
                SSLEngineResult engineResult = ConscryptEngineSocket.this.engine.wrap(buffer, this.target);
                if (engineResult.getStatus() != SSLEngineResult.Status.OK) {
                    throw new SSLException("Unexpected engine result " + engineResult.getStatus());
                } else if (this.target.position() == engineResult.bytesProduced()) {
                    len -= engineResult.bytesConsumed();
                    if (len == buffer.remaining()) {
                        this.target.flip();
                        writeToSocket();
                    } else {
                        throw new SSLException("Engine did not read the correct number of bytes");
                    }
                } else {
                    throw new SSLException("Engine bytesProduced " + engineResult.bytesProduced() + " does not match bytes written " + this.target.position());
                }
            } while (len > 0);
        }

        public void flush() throws IOException {
            ConscryptEngineSocket.this.startHandshake();
            synchronized (this.writeLock) {
                flushInternal();
            }
        }

        /* access modifiers changed from: private */
        public void flushInternal() throws IOException {
            ConscryptEngineSocket.this.checkOpen();
            init();
            this.socketOutputStream.flush();
        }

        private void init() throws IOException {
            if (this.socketOutputStream == null) {
                this.socketOutputStream = ConscryptEngineSocket.this.getUnderlyingOutputStream();
            }
        }

        private void writeToSocket() throws IOException {
            this.socketOutputStream.write(this.target.array(), this.targetArrayOffset, this.target.limit());
        }
    }

    ConscryptEngineSocket(SSLParametersImpl sslParameters) throws IOException {
        this.engine = newEngine(sslParameters, this);
    }

    ConscryptEngineSocket(String hostname, int port, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port);
        this.engine = newEngine(sslParameters, this);
    }

    ConscryptEngineSocket(InetAddress address, int port, SSLParametersImpl sslParameters) throws IOException {
        super(address, port);
        this.engine = newEngine(sslParameters, this);
    }

    ConscryptEngineSocket(String hostname, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(hostname, port, clientAddress, clientPort);
        this.engine = newEngine(sslParameters, this);
    }

    ConscryptEngineSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        super(address, port, clientAddress, clientPort);
        this.engine = newEngine(sslParameters, this);
    }

    ConscryptEngineSocket(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        super(socket, hostname, port, autoClose);
        this.engine = newEngine(sslParameters, this);
    }

    private static ConscryptEngine newEngine(SSLParametersImpl sslParameters, ConscryptEngineSocket socket) {
        ConscryptEngine engine2 = new ConscryptEngine(sslParameters, socket.peerInfoProvider());
        engine2.setHandshakeListener(new HandshakeListener() {
            public void onHandshakeFinished() {
                ConscryptEngineSocket.this.onHandshakeFinished();
            }
        });
        engine2.setUseClientMode(sslParameters.getUseClientMode());
        return engine2;
    }

    public final SSLParameters getSSLParameters() {
        return this.engine.getSSLParameters();
    }

    public final void setSSLParameters(SSLParameters sslParameters) {
        this.engine.setSSLParameters(sslParameters);
    }

    public final void startHandshake() throws IOException {
        checkOpen();
        try {
            synchronized (this.handshakeLock) {
                synchronized (this.stateLock) {
                    if (this.state == 0) {
                        this.state = 2;
                        this.engine.beginHandshake();
                        this.in = new SSLInputStream();
                        this.out = new SSLOutputStream();
                        doHandshake();
                    }
                }
            }
        } catch (SSLException e) {
            close();
            throw e;
        } catch (IOException e2) {
            close();
            throw e2;
        } catch (Exception e3) {
            close();
            throw SSLUtils.toSSLHandshakeException(e3);
        }
    }

    /* access modifiers changed from: private */
    public void doHandshake() throws IOException {
        boolean finished = false;
        while (!finished) {
            try {
                switch (AnonymousClass2.$SwitchMap$javax$net$ssl$SSLEngineResult$HandshakeStatus[this.engine.getHandshakeStatus().ordinal()]) {
                    case 1:
                        if (this.in.readInternal(EmptyArray.BYTE, 0, 0) >= 0) {
                            break;
                        } else {
                            throw SSLUtils.toSSLHandshakeException(new EOFException());
                        }
                    case 2:
                        this.out.writeInternal(EMPTY_BUFFER);
                        this.out.flushInternal();
                        break;
                    case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                        throw new IllegalStateException("Engine tasks are unsupported");
                    case 4:
                    case 5:
                        finished = true;
                        break;
                    default:
                        throw new IllegalStateException("Unknown handshake status: " + this.engine.getHandshakeStatus());
                }
            } catch (SSLException e) {
                close();
                throw e;
            } catch (IOException e2) {
                close();
                throw e2;
            } catch (Exception e3) {
                close();
                throw SSLUtils.toSSLHandshakeException(e3);
            }
        }
    }

    public final InputStream getInputStream() throws IOException {
        checkOpen();
        waitForHandshake();
        return this.in;
    }

    public final OutputStream getOutputStream() throws IOException {
        checkOpen();
        waitForHandshake();
        return this.out;
    }

    public final SSLSession getHandshakeSession() {
        return this.engine.handshakeSession();
    }

    public final SSLSession getSession() {
        SSLSession session = this.engine.getSession();
        if (SSLNullSession.isNullSession(session)) {
            boolean handshakeCompleted = false;
            try {
                if (isConnected()) {
                    waitForHandshake();
                    handshakeCompleted = true;
                }
            } catch (IOException e) {
            }
            if (!handshakeCompleted) {
                return session;
            }
            session = this.engine.getSession();
        }
        return session;
    }

    /* access modifiers changed from: package-private */
    public final SSLSession getActiveSession() {
        return this.engine.getSession();
    }

    public final boolean getEnableSessionCreation() {
        return this.engine.getEnableSessionCreation();
    }

    public final void setEnableSessionCreation(boolean flag) {
        this.engine.setEnableSessionCreation(flag);
    }

    public final String[] getSupportedCipherSuites() {
        return this.engine.getSupportedCipherSuites();
    }

    public final String[] getEnabledCipherSuites() {
        return this.engine.getEnabledCipherSuites();
    }

    public final void setEnabledCipherSuites(String[] suites) {
        this.engine.setEnabledCipherSuites(suites);
    }

    public final String[] getSupportedProtocols() {
        return this.engine.getSupportedProtocols();
    }

    public final String[] getEnabledProtocols() {
        return this.engine.getEnabledProtocols();
    }

    public final void setEnabledProtocols(String[] protocols) {
        this.engine.setEnabledProtocols(protocols);
    }

    public final void setHostname(String hostname) {
        this.engine.setHostname(hostname);
        super.setHostname(hostname);
    }

    public final void setUseSessionTickets(boolean useSessionTickets) {
        this.engine.setUseSessionTickets(useSessionTickets);
    }

    public final void setChannelIdEnabled(boolean enabled) {
        this.engine.setChannelIdEnabled(enabled);
    }

    public final byte[] getChannelId() throws SSLException {
        return this.engine.getChannelId();
    }

    public final void setChannelIdPrivateKey(PrivateKey privateKey) {
        this.engine.setChannelIdPrivateKey(privateKey);
    }

    /* access modifiers changed from: package-private */
    public byte[] getTlsUnique() {
        return this.engine.getTlsUnique();
    }

    public final boolean getUseClientMode() {
        return this.engine.getUseClientMode();
    }

    public final void setUseClientMode(boolean mode) {
        this.engine.setUseClientMode(mode);
    }

    public final boolean getWantClientAuth() {
        return this.engine.getWantClientAuth();
    }

    public final boolean getNeedClientAuth() {
        return this.engine.getNeedClientAuth();
    }

    public final void setNeedClientAuth(boolean need) {
        this.engine.setNeedClientAuth(need);
    }

    public final void setWantClientAuth(boolean want) {
        this.engine.setWantClientAuth(want);
    }

    public final void close() throws IOException {
        if (this.stateLock != null) {
            synchronized (this.stateLock) {
                if (this.state != 8) {
                    this.state = 8;
                    this.stateLock.notifyAll();
                    super.close();
                    this.engine.closeInbound();
                    this.engine.closeOutbound();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void setApplicationProtocols(String[] protocols) {
        this.engine.setApplicationProtocols(protocols);
    }

    /* access modifiers changed from: package-private */
    public final String[] getApplicationProtocols() {
        return this.engine.getApplicationProtocols();
    }

    public final String getApplicationProtocol() {
        return this.engine.getApplicationProtocol();
    }

    public final String getHandshakeApplicationProtocol() {
        return this.engine.getHandshakeApplicationProtocol();
    }

    public final void setApplicationProtocolSelector(ApplicationProtocolSelector selector) {
        setApplicationProtocolSelector(selector == null ? null : new ApplicationProtocolSelectorAdapter((SSLSocket) this, selector));
    }

    /* access modifiers changed from: package-private */
    public final void setApplicationProtocolSelector(ApplicationProtocolSelectorAdapter selector) {
        this.engine.setApplicationProtocolSelector(selector);
    }

    /* access modifiers changed from: private */
    public void onHandshakeFinished() {
        boolean notify = false;
        synchronized (this.stateLock) {
            if (this.state != 8) {
                if (this.state == 2) {
                    this.state = 4;
                } else if (this.state == 3) {
                    this.state = 5;
                }
                this.stateLock.notifyAll();
                notify = true;
            }
        }
        if (notify) {
            notifyHandshakeCompletedListeners();
        }
    }

    private void waitForHandshake() throws IOException {
        startHandshake();
        synchronized (this.stateLock) {
            while (this.state != 5 && this.state != 4 && this.state != 8) {
                try {
                    this.stateLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted waiting for handshake", e);
                }
            }
            if (this.state == 8) {
                throw new SocketException("Socket is closed");
            }
        }
    }

    /* access modifiers changed from: private */
    public OutputStream getUnderlyingOutputStream() throws IOException {
        return super.getOutputStream();
    }

    /* access modifiers changed from: private */
    public InputStream getUnderlyingInputStream() throws IOException {
        return super.getInputStream();
    }
}

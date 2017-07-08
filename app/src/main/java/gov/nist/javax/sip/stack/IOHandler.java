package gov.nist.javax.sip.stack;

import gov.nist.core.Separators;
import gov.nist.javax.sip.SipStackImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import org.ccil.cowan.tagsoup.HTMLModels;

class IOHandler {
    private static String TCP;
    private static String TLS;
    private Semaphore ioSemaphore;
    private SipStackImpl sipStack;
    private ConcurrentHashMap<String, Socket> socketTable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.javax.sip.stack.IOHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.javax.sip.stack.IOHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.stack.IOHandler.<clinit>():void");
    }

    protected static String makeKey(InetAddress addr, int port) {
        return addr.getHostAddress() + Separators.COLON + port;
    }

    protected IOHandler(SIPTransactionStack sipStack) {
        this.ioSemaphore = new Semaphore(1);
        this.sipStack = (SipStackImpl) sipStack;
        this.socketTable = new ConcurrentHashMap();
    }

    protected void putSocket(String key, Socket sock) {
        this.socketTable.put(key, sock);
    }

    protected Socket getSocket(String key) {
        return (Socket) this.socketTable.get(key);
    }

    protected void removeSocket(String key) {
        this.socketTable.remove(key);
    }

    private void writeChunks(OutputStream outputStream, byte[] bytes, int length) throws IOException {
        synchronized (outputStream) {
            for (int p = 0; p < length; p += HTMLModels.M_LEGEND) {
                int chunk;
                if (p + HTMLModels.M_LEGEND < length) {
                    chunk = HTMLModels.M_LEGEND;
                } else {
                    chunk = length - p;
                }
                outputStream.write(bytes, p, chunk);
            }
        }
        outputStream.flush();
    }

    public SocketAddress obtainLocalAddress(InetAddress dst, int dstPort, InetAddress localAddress, int localPort) throws IOException {
        String key = makeKey(dst, dstPort);
        Socket clientSock = getSocket(key);
        if (clientSock == null) {
            clientSock = this.sipStack.getNetworkLayer().createSocket(dst, dstPort, localAddress, localPort);
            putSocket(key, clientSock);
        }
        return clientSock.getLocalSocketAddress();
    }

    public Socket sendBytes(InetAddress senderAddress, InetAddress receiverAddress, int contactPort, String transport, byte[] bytes, boolean retry, MessageChannel messageChannel) throws IOException {
        String key;
        int retry_count = 0;
        int max_retry = retry ? 2 : 1;
        int length = bytes.length;
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("sendBytes " + transport + " inAddr " + receiverAddress.getHostAddress() + " port = " + contactPort + " length = " + length);
        }
        if (this.sipStack.isLoggingEnabled() && this.sipStack.isLogStackTraceOnMessageSend()) {
            this.sipStack.getStackLogger().logStackTrace(16);
        }
        Socket socket;
        if (transport.compareToIgnoreCase(TCP) == 0) {
            key = makeKey(receiverAddress, contactPort);
            try {
                if (this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                    socket = getSocket(key);
                    while (retry_count < max_retry) {
                        if (socket == null) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("inaddr = " + receiverAddress);
                                this.sipStack.getStackLogger().logDebug("port = " + contactPort);
                            }
                            socket = this.sipStack.getNetworkLayer().createSocket(receiverAddress, contactPort, senderAddress);
                            writeChunks(socket.getOutputStream(), bytes, length);
                            putSocket(key, socket);
                        } else {
                            try {
                                writeChunks(socket.getOutputStream(), bytes, length);
                                break;
                            } catch (IOException e) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logDebug("IOException occured retryCount " + retry_count);
                                }
                                removeSocket(key);
                                try {
                                    socket.close();
                                } catch (Exception e2) {
                                }
                                socket = null;
                                retry_count++;
                            } catch (Throwable th) {
                                this.ioSemaphore.release();
                            }
                        }
                    }
                    this.ioSemaphore.release();
                    if (socket != null) {
                        return socket;
                    }
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug(this.socketTable.toString());
                        this.sipStack.getStackLogger().logError("Could not connect to " + receiverAddress + Separators.COLON + contactPort);
                    }
                    throw new IOException("Could not connect to " + receiverAddress + Separators.COLON + contactPort);
                }
                throw new IOException("Could not acquire IO Semaphore after 10 seconds -- giving up ");
            } catch (InterruptedException e3) {
                throw new IOException("exception in acquiring sem");
            }
        }
        if (transport.compareToIgnoreCase(TLS) == 0) {
            key = makeKey(receiverAddress, contactPort);
            try {
                if (this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                    socket = getSocket(key);
                    while (retry_count < max_retry) {
                        if (socket == null) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("inaddr = " + receiverAddress);
                                this.sipStack.getStackLogger().logDebug("port = " + contactPort);
                            }
                            socket = this.sipStack.getNetworkLayer().createSSLSocket(receiverAddress, contactPort, senderAddress);
                            SSLSocket sslsock = (SSLSocket) socket;
                            HandshakeCompletedListener handshakeCompletedListenerImpl = new HandshakeCompletedListenerImpl((TLSMessageChannel) messageChannel);
                            ((TLSMessageChannel) messageChannel).setHandshakeCompletedListener(handshakeCompletedListenerImpl);
                            sslsock.addHandshakeCompletedListener(handshakeCompletedListenerImpl);
                            sslsock.setEnabledProtocols(this.sipStack.getEnabledProtocols());
                            sslsock.startHandshake();
                            writeChunks(socket.getOutputStream(), bytes, length);
                            putSocket(key, socket);
                        } else {
                            try {
                                writeChunks(socket.getOutputStream(), bytes, length);
                                break;
                            } catch (IOException ex) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logException(ex);
                                }
                                removeSocket(key);
                                try {
                                    socket.close();
                                } catch (Exception e4) {
                                }
                                socket = null;
                                retry_count++;
                            } catch (Throwable th2) {
                                this.ioSemaphore.release();
                            }
                        }
                    }
                    this.ioSemaphore.release();
                    if (socket != null) {
                        return socket;
                    }
                    throw new IOException("Could not connect to " + receiverAddress + Separators.COLON + contactPort);
                }
                throw new IOException("Timeout acquiring IO SEM");
            } catch (InterruptedException e5) {
                throw new IOException("exception in acquiring sem");
            }
        }
        DatagramSocket datagramSock = this.sipStack.getNetworkLayer().createDatagramSocket();
        datagramSock.connect(receiverAddress, contactPort);
        datagramSock.send(new DatagramPacket(bytes, 0, length, receiverAddress, contactPort));
        datagramSock.close();
        return null;
    }

    public void closeAll() {
        Enumeration<Socket> values = this.socketTable.elements();
        while (values.hasMoreElements()) {
            try {
                ((Socket) values.nextElement()).close();
            } catch (IOException e) {
            }
        }
    }
}

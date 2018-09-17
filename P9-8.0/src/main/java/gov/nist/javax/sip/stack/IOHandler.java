package gov.nist.javax.sip.stack;

import gov.nist.core.Separators;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.address.ParameterNames;
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
    private static String TCP = ParameterNames.TCP;
    private static String TLS = ParameterNames.TLS;
    private Semaphore ioSemaphore = new Semaphore(1);
    private SipStackImpl sipStack;
    private ConcurrentHashMap<String, Socket> socketTable;

    protected static String makeKey(InetAddress addr, int port) {
        return addr.getHostAddress() + Separators.COLON + port;
    }

    protected IOHandler(SIPTransactionStack sipStack) {
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
        int retry_count = 0;
        int max_retry = retry ? 2 : 1;
        int length = bytes.length;
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("sendBytes " + transport + " inAddr " + receiverAddress.getHostAddress() + " port = " + contactPort + " length = " + length);
        }
        if (this.sipStack.isLoggingEnabled() && this.sipStack.isLogStackTraceOnMessageSend()) {
            this.sipStack.getStackLogger().logStackTrace(16);
        }
        String key;
        Socket clientSock;
        if (transport.compareToIgnoreCase(TCP) == 0) {
            key = makeKey(receiverAddress, contactPort);
            try {
                if (this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                    clientSock = getSocket(key);
                    while (retry_count < max_retry) {
                        if (clientSock == null) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("inaddr = " + receiverAddress);
                                this.sipStack.getStackLogger().logDebug("port = " + contactPort);
                            }
                            clientSock = this.sipStack.getNetworkLayer().createSocket(receiverAddress, contactPort, senderAddress);
                            writeChunks(clientSock.getOutputStream(), bytes, length);
                            putSocket(key, clientSock);
                        } else {
                            try {
                                writeChunks(clientSock.getOutputStream(), bytes, length);
                                break;
                            } catch (IOException e) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logDebug("IOException occured retryCount " + retry_count);
                                }
                                removeSocket(key);
                                try {
                                    clientSock.close();
                                } catch (Exception e2) {
                                }
                                clientSock = null;
                                retry_count++;
                            } catch (Throwable th) {
                                this.ioSemaphore.release();
                            }
                        }
                    }
                    this.ioSemaphore.release();
                    if (clientSock != null) {
                        return clientSock;
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
                    clientSock = getSocket(key);
                    while (retry_count < max_retry) {
                        if (clientSock == null) {
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug("inaddr = " + receiverAddress);
                                this.sipStack.getStackLogger().logDebug("port = " + contactPort);
                            }
                            clientSock = this.sipStack.getNetworkLayer().createSSLSocket(receiverAddress, contactPort, senderAddress);
                            SSLSocket sslsock = (SSLSocket) clientSock;
                            HandshakeCompletedListener handshakeCompletedListenerImpl = new HandshakeCompletedListenerImpl((TLSMessageChannel) messageChannel);
                            ((TLSMessageChannel) messageChannel).setHandshakeCompletedListener(handshakeCompletedListenerImpl);
                            sslsock.addHandshakeCompletedListener(handshakeCompletedListenerImpl);
                            sslsock.setEnabledProtocols(this.sipStack.getEnabledProtocols());
                            sslsock.startHandshake();
                            writeChunks(clientSock.getOutputStream(), bytes, length);
                            putSocket(key, clientSock);
                        } else {
                            try {
                                writeChunks(clientSock.getOutputStream(), bytes, length);
                                break;
                            } catch (IOException ex) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logException(ex);
                                }
                                removeSocket(key);
                                try {
                                    clientSock.close();
                                } catch (Exception e4) {
                                }
                                clientSock = null;
                                retry_count++;
                            } catch (Throwable th2) {
                                this.ioSemaphore.release();
                            }
                        }
                    }
                    this.ioSemaphore.release();
                    if (clientSock != null) {
                        return clientSock;
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

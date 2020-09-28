package gov.nist.javax.sip.stack;

import gov.nist.core.Separators;
import gov.nist.core.StackLogger;
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

/* access modifiers changed from: package-private */
public class IOHandler {
    private static String TCP = ParameterNames.TCP;
    private static String TLS = ParameterNames.TLS;
    private Semaphore ioSemaphore = new Semaphore(1);
    private SipStackImpl sipStack;
    private ConcurrentHashMap<String, Socket> socketTable;

    protected static String makeKey(InetAddress addr, int port) {
        return addr.getHostAddress() + Separators.COLON + port;
    }

    protected IOHandler(SIPTransactionStack sipStack2) {
        this.sipStack = (SipStackImpl) sipStack2;
        this.socketTable = new ConcurrentHashMap<>();
    }

    /* access modifiers changed from: protected */
    public void putSocket(String key, Socket sock) {
        this.socketTable.put(key, sock);
    }

    /* access modifiers changed from: protected */
    public Socket getSocket(String key) {
        return this.socketTable.get(key);
    }

    /* access modifiers changed from: protected */
    public void removeSocket(String key) {
        this.socketTable.remove(key);
    }

    private void writeChunks(OutputStream outputStream, byte[] bytes, int length) throws IOException {
        synchronized (outputStream) {
            for (int p = 0; p < length; p += HTMLModels.M_LEGEND) {
                outputStream.write(bytes, p, p + HTMLModels.M_LEGEND < length ? 8192 : length - p);
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
        String str;
        String str2;
        byte[] bArr = bytes;
        int max_retry = retry ? 2 : 1;
        int length = bArr.length;
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("sendBytes " + transport + " inAddr " + receiverAddress.getHostAddress() + " port = " + contactPort + " length = " + length);
        }
        if (this.sipStack.isLoggingEnabled() && this.sipStack.isLogStackTraceOnMessageSend()) {
            this.sipStack.getStackLogger().logStackTrace(16);
        }
        if (transport.compareToIgnoreCase(TCP) == 0) {
            String key = makeKey(receiverAddress, contactPort);
            try {
                try {
                    str = "exception in acquiring sem";
                    try {
                        if (this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                            Socket clientSock = getSocket(key);
                            int retry_count = 0;
                            while (true) {
                                if (retry_count >= max_retry) {
                                    break;
                                } else if (clientSock == null) {
                                    try {
                                        if (this.sipStack.isLoggingEnabled()) {
                                            this.sipStack.getStackLogger().logDebug("inaddr = " + receiverAddress);
                                            this.sipStack.getStackLogger().logDebug("port = " + contactPort);
                                        }
                                        clientSock = this.sipStack.getNetworkLayer().createSocket(receiverAddress, contactPort, senderAddress);
                                        writeChunks(clientSock.getOutputStream(), bArr, length);
                                        putSocket(key, clientSock);
                                    } catch (Throwable th) {
                                        this.ioSemaphore.release();
                                        throw th;
                                    }
                                } else {
                                    try {
                                        writeChunks(clientSock.getOutputStream(), bArr, length);
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
                                        bArr = bytes;
                                    }
                                }
                            }
                            this.ioSemaphore.release();
                            if (clientSock != null) {
                                return clientSock;
                            }
                            if (this.sipStack.isLoggingEnabled()) {
                                this.sipStack.getStackLogger().logDebug(this.socketTable.toString());
                                StackLogger stackLogger = this.sipStack.getStackLogger();
                                StringBuilder sb = new StringBuilder();
                                str2 = "Could not connect to ";
                                sb.append(str2);
                                sb.append(receiverAddress);
                                sb.append(Separators.COLON);
                                sb.append(contactPort);
                                stackLogger.logError(sb.toString());
                            } else {
                                str2 = "Could not connect to ";
                            }
                            throw new IOException(str2 + receiverAddress + Separators.COLON + contactPort);
                        }
                        throw new IOException("Could not acquire IO Semaphore after 10 seconds -- giving up ");
                    } catch (InterruptedException e3) {
                        throw new IOException(str);
                    }
                } catch (InterruptedException e4) {
                    str = "exception in acquiring sem";
                    throw new IOException(str);
                }
            } catch (InterruptedException e5) {
                str = "exception in acquiring sem";
                throw new IOException(str);
            }
        } else if (transport.compareToIgnoreCase(TLS) == 0) {
            String key2 = makeKey(receiverAddress, contactPort);
            try {
                if (this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                    Socket clientSock2 = getSocket(key2);
                    int retry_count2 = 0;
                    while (true) {
                        if (retry_count2 >= max_retry) {
                            break;
                        } else if (clientSock2 == null) {
                            try {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logDebug("inaddr = " + receiverAddress);
                                    this.sipStack.getStackLogger().logDebug("port = " + contactPort);
                                }
                                clientSock2 = this.sipStack.getNetworkLayer().createSSLSocket(receiverAddress, contactPort, senderAddress);
                                SSLSocket sslsock = (SSLSocket) clientSock2;
                                HandshakeCompletedListener listner = new HandshakeCompletedListenerImpl((TLSMessageChannel) messageChannel);
                                ((TLSMessageChannel) messageChannel).setHandshakeCompletedListener(listner);
                                sslsock.addHandshakeCompletedListener(listner);
                                sslsock.setEnabledProtocols(this.sipStack.getEnabledProtocols());
                                sslsock.startHandshake();
                                try {
                                    writeChunks(clientSock2.getOutputStream(), bytes, length);
                                    putSocket(key2, clientSock2);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                this.ioSemaphore.release();
                                throw th;
                            }
                        } else {
                            try {
                                writeChunks(clientSock2.getOutputStream(), bytes, length);
                                break;
                            } catch (IOException ex) {
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logException(ex);
                                }
                                removeSocket(key2);
                                try {
                                    clientSock2.close();
                                } catch (Exception e6) {
                                }
                                clientSock2 = null;
                                retry_count2++;
                            }
                        }
                    }
                    this.ioSemaphore.release();
                    if (clientSock2 != null) {
                        return clientSock2;
                    }
                    throw new IOException("Could not connect to " + receiverAddress + Separators.COLON + contactPort);
                }
                try {
                    throw new IOException("Timeout acquiring IO SEM");
                } catch (InterruptedException e7) {
                    throw new IOException("exception in acquiring sem");
                }
            } catch (InterruptedException e8) {
                throw new IOException("exception in acquiring sem");
            }
        } else {
            DatagramSocket datagramSock = this.sipStack.getNetworkLayer().createDatagramSocket();
            datagramSock.connect(receiverAddress, contactPort);
            datagramSock.send(new DatagramPacket(bytes, 0, length, receiverAddress, contactPort));
            datagramSock.close();
            return null;
        }
    }

    public void closeAll() {
        Enumeration<Socket> values = this.socketTable.elements();
        while (values.hasMoreElements()) {
            try {
                values.nextElement().close();
            } catch (IOException e) {
            }
        }
    }
}

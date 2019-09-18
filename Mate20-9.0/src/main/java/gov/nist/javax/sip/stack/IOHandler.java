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

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0096, code lost:
        if (r1.sipStack.isLoggingEnabled() == false) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0098, code lost:
        r0 = r1.sipStack.getStackLogger();
        r0.logDebug("inaddr = " + r9);
        r0 = r1.sipStack.getStackLogger();
        r0.logDebug("port = " + r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00cc, code lost:
        r3 = r1.sipStack.getNetworkLayer().createSocket(r9, r10, r2);
        writeChunks(r3.getOutputStream(), r12, r7);
        putSocket(r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x01bf, code lost:
        if (r1.sipStack.isLoggingEnabled() == false) goto L_0x01f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01c1, code lost:
        r0 = r1.sipStack.getStackLogger();
        r0.logDebug("inaddr = " + r9);
        r0 = r1.sipStack.getStackLogger();
        r0.logDebug("port = " + r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01f5, code lost:
        r3 = r1.sipStack.getNetworkLayer().createSSLSocket(r9, r10, r2);
        r0 = (javax.net.ssl.SSLSocket) r3;
        r4 = new gov.nist.javax.sip.stack.HandshakeCompletedListenerImpl((gov.nist.javax.sip.stack.TLSMessageChannel) r24);
        ((gov.nist.javax.sip.stack.TLSMessageChannel) r24).setHandshakeCompletedListener(r4);
        r0.addHandshakeCompletedListener(r4);
        r0.setEnabledProtocols(r1.sipStack.getEnabledProtocols());
        r0.startHandshake();
        writeChunks(r3.getOutputStream(), r12, r7);
        putSocket(r5, r3);
     */
    public Socket sendBytes(InetAddress senderAddress, InetAddress receiverAddress, int contactPort, String transport, byte[] bytes, boolean retry, MessageChannel messageChannel) throws IOException {
        Socket clientSock;
        Socket clientSock2;
        InetAddress inetAddress = senderAddress;
        InetAddress inetAddress2 = receiverAddress;
        int max_retry = contactPort;
        String str = transport;
        byte[] bArr = bytes;
        int retry_count = 0;
        int max_retry2 = retry ? 2 : 1;
        int length = bArr.length;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("sendBytes " + str + " inAddr " + receiverAddress.getHostAddress() + " port = " + max_retry + " length = " + length);
        }
        if (this.sipStack.isLoggingEnabled() && this.sipStack.isLogStackTraceOnMessageSend()) {
            this.sipStack.getStackLogger().logStackTrace(16);
        }
        if (str.compareToIgnoreCase(TCP) == 0) {
            String key = makeKey(receiverAddress, contactPort);
            try {
                if (this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                    Socket clientSock3 = getSocket(key);
                    while (true) {
                        clientSock2 = clientSock3;
                        if (retry_count >= max_retry2) {
                            break;
                        } else if (clientSock2 == null) {
                            break;
                        } else {
                            try {
                                writeChunks(clientSock2.getOutputStream(), bArr, length);
                                break;
                            } catch (IOException e) {
                                IOException ex = e;
                                if (this.sipStack.isLoggingEnabled()) {
                                    StackLogger stackLogger2 = this.sipStack.getStackLogger();
                                    StringBuilder sb = new StringBuilder();
                                    IOException iOException = ex;
                                    sb.append("IOException occured retryCount ");
                                    sb.append(retry_count);
                                    stackLogger2.logDebug(sb.toString());
                                }
                                removeSocket(key);
                                try {
                                    clientSock2.close();
                                } catch (Exception e2) {
                                }
                                clientSock3 = null;
                                retry_count++;
                            } catch (Throwable th) {
                                this.ioSemaphore.release();
                                throw th;
                            }
                        }
                    }
                    this.ioSemaphore.release();
                    if (clientSock2 != null) {
                        return clientSock2;
                    }
                    if (this.sipStack.isLoggingEnabled()) {
                        this.sipStack.getStackLogger().logDebug(this.socketTable.toString());
                        StackLogger stackLogger3 = this.sipStack.getStackLogger();
                        stackLogger3.logError("Could not connect to " + inetAddress2 + Separators.COLON + max_retry);
                    }
                    throw new IOException("Could not connect to " + inetAddress2 + Separators.COLON + max_retry);
                }
                throw new IOException("Could not acquire IO Semaphore after 10 seconds -- giving up ");
            } catch (InterruptedException e3) {
                throw new IOException("exception in acquiring sem");
            }
        } else if (str.compareToIgnoreCase(TLS) == 0) {
            String key2 = makeKey(receiverAddress, contactPort);
            try {
                if (this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS)) {
                    Socket clientSock4 = getSocket(key2);
                    while (true) {
                        clientSock = clientSock4;
                        if (retry_count >= max_retry2) {
                            break;
                        } else if (clientSock == null) {
                            break;
                        } else {
                            try {
                                writeChunks(clientSock.getOutputStream(), bArr, length);
                                break;
                            } catch (IOException e4) {
                                IOException ex2 = e4;
                                if (this.sipStack.isLoggingEnabled()) {
                                    this.sipStack.getStackLogger().logException(ex2);
                                }
                                removeSocket(key2);
                                try {
                                    clientSock.close();
                                } catch (Exception e5) {
                                }
                                clientSock4 = null;
                                retry_count++;
                            } catch (Throwable th2) {
                                this.ioSemaphore.release();
                                throw th2;
                            }
                        }
                    }
                    this.ioSemaphore.release();
                    if (clientSock != null) {
                        return clientSock;
                    }
                    throw new IOException("Could not connect to " + inetAddress2 + Separators.COLON + max_retry);
                }
                throw new IOException("Timeout acquiring IO SEM");
            } catch (InterruptedException e6) {
                throw new IOException("exception in acquiring sem");
            }
        } else {
            DatagramSocket datagramSock = this.sipStack.getNetworkLayer().createDatagramSocket();
            datagramSock.connect(inetAddress2, max_retry);
            int i = length;
            int i2 = max_retry2;
            DatagramPacket dgPacket = new DatagramPacket(bArr, 0, length, inetAddress2, max_retry);
            datagramSock.send(dgPacket);
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

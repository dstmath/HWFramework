package gov.nist.javax.sip.stack;

import gov.nist.core.HostPort;
import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.address.ParameterNames;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import javax.sip.ListeningPoint;

public class TCPMessageProcessor extends MessageProcessor {
    private ArrayList<TCPMessageChannel> incomingTcpMessageChannels = new ArrayList();
    private boolean isRunning;
    protected int nConnections;
    private ServerSocket sock;
    private Hashtable tcpMessageChannels = new Hashtable();
    protected int useCount;

    protected TCPMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) {
        super(ipAddress, port, ParameterNames.TCP, sipStack);
        this.sipStack = sipStack;
    }

    public void start() throws IOException {
        Thread thread = new Thread(this);
        thread.setName("TCPMessageProcessorThread");
        thread.setPriority(10);
        thread.setDaemon(true);
        this.sock = this.sipStack.getNetworkLayer().createServerSocket(getPort(), 0, getIpAddress());
        if (getIpAddress().getHostAddress().equals("0.0.0.0") || getIpAddress().getHostAddress().equals("::0")) {
            super.setIpAddress(this.sock.getInetAddress());
        }
        this.isRunning = true;
        thread.start();
    }

    /* JADX WARNING: Missing block: B:20:0x0025, code:
            r4 = r8.sock.accept();
     */
    /* JADX WARNING: Missing block: B:21:0x0031, code:
            if (r8.sipStack.isLoggingEnabled() == false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:22:0x0033, code:
            getSIPStack().getStackLogger().logDebug("Accepting new connection!");
     */
    /* JADX WARNING: Missing block: B:23:0x0041, code:
            r8.incomingTcpMessageChannels.add(new gov.nist.javax.sip.stack.TCPMessageChannel(r4, r8.sipStack, r8));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        while (this.isRunning) {
            try {
                synchronized (this) {
                    do {
                        if (this.sipStack.maxConnections != -1 && this.nConnections >= this.sipStack.maxConnections) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        this.nConnections++;
                    } while (this.isRunning);
                    return;
                }
            } catch (SocketException e2) {
                this.isRunning = false;
            } catch (IOException ex) {
                if (this.sipStack.isLoggingEnabled()) {
                    getSIPStack().getStackLogger().logException(ex);
                }
            } catch (Exception ex2) {
                InternalErrorHandler.handleException(ex2);
            }
        }
    }

    public String getTransport() {
        return ParameterNames.TCP;
    }

    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    public synchronized void stop() {
        this.isRunning = false;
        try {
            this.sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (TCPMessageChannel next : this.tcpMessageChannels.values()) {
            next.close();
        }
        Iterator incomingMCIterator = this.incomingTcpMessageChannels.iterator();
        while (incomingMCIterator.hasNext()) {
            ((TCPMessageChannel) incomingMCIterator.next()).close();
        }
        notify();
    }

    protected synchronized void remove(TCPMessageChannel tcpMessageChannel) {
        String key = tcpMessageChannel.getKey();
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug(Thread.currentThread() + " removing " + key);
        }
        if (this.tcpMessageChannels.get(key) == tcpMessageChannel) {
            this.tcpMessageChannels.remove(key);
        }
        this.incomingTcpMessageChannels.remove(tcpMessageChannel);
    }

    /* JADX WARNING: Missing block: B:12:0x0074, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
        String key = MessageChannel.getKey(targetHostPort, ListeningPoint.TCP);
        if (this.tcpMessageChannels.get(key) != null) {
            return (TCPMessageChannel) this.tcpMessageChannels.get(key);
        }
        TCPMessageChannel retval = new TCPMessageChannel(targetHostPort.getInetAddress(), targetHostPort.getPort(), this.sipStack, this);
        this.tcpMessageChannels.put(key, retval);
        retval.isCached = true;
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("key " + key);
            this.sipStack.getStackLogger().logDebug("Creating " + retval);
        }
    }

    protected synchronized void cacheMessageChannel(TCPMessageChannel messageChannel) {
        String key = messageChannel.getKey();
        TCPMessageChannel currentChannel = (TCPMessageChannel) this.tcpMessageChannels.get(key);
        if (currentChannel != null) {
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("Closing " + key);
            }
            currentChannel.close();
        }
        if (this.sipStack.isLoggingEnabled()) {
            this.sipStack.getStackLogger().logDebug("Caching " + key);
        }
        this.tcpMessageChannels.put(key, messageChannel);
    }

    /* JADX WARNING: Missing block: B:12:0x006c, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized MessageChannel createMessageChannel(InetAddress host, int port) throws IOException {
        try {
            String key = MessageChannel.getKey(host, port, ListeningPoint.TCP);
            if (this.tcpMessageChannels.get(key) != null) {
                return (TCPMessageChannel) this.tcpMessageChannels.get(key);
            }
            TCPMessageChannel retval = new TCPMessageChannel(host, port, this.sipStack, this);
            this.tcpMessageChannels.put(key, retval);
            retval.isCached = true;
            if (this.sipStack.isLoggingEnabled()) {
                this.sipStack.getStackLogger().logDebug("key " + key);
                this.sipStack.getStackLogger().logDebug("Creating " + retval);
            }
        } catch (UnknownHostException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public int getMaximumMessageSize() {
        return Integer.MAX_VALUE;
    }

    public boolean inUse() {
        return this.useCount != 0;
    }

    public int getDefaultTargetPort() {
        return 5060;
    }

    public boolean isSecure() {
        return false;
    }
}

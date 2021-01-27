package gov.nist.javax.sip.stack;

import gov.nist.core.HostPort;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.address.ParameterNames;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import javax.sip.ListeningPoint;

public class TCPMessageProcessor extends MessageProcessor {
    private ArrayList<TCPMessageChannel> incomingTcpMessageChannels = new ArrayList<>();
    private boolean isRunning;
    protected int nConnections;
    private ServerSocket sock;
    private Hashtable tcpMessageChannels = new Hashtable();
    protected int useCount;

    protected TCPMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) {
        super(ipAddress, port, ParameterNames.TCP, sipStack);
        this.sipStack = sipStack;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
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

    @Override // gov.nist.javax.sip.stack.MessageProcessor, java.lang.Runnable
    public void run() {
        while (this.isRunning) {
            try {
                synchronized (this) {
                    while (this.sipStack.maxConnections != -1 && this.nConnections >= this.sipStack.maxConnections) {
                        try {
                            wait();
                            if (!this.isRunning) {
                                return;
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                    this.nConnections++;
                }
                Socket newsock = this.sock.accept();
                if (this.sipStack.isLoggingEnabled()) {
                    getSIPStack().getStackLogger().logDebug("Accepting new connection!");
                }
                this.incomingTcpMessageChannels.add(new TCPMessageChannel(newsock, this.sipStack, this));
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

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public String getTransport() {
        return ParameterNames.TCP;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
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
            incomingMCIterator.next().close();
        }
        notify();
    }

    /* access modifiers changed from: protected */
    public synchronized void remove(TCPMessageChannel tcpMessageChannel) {
        String key = tcpMessageChannel.getKey();
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug(Thread.currentThread() + " removing " + key);
        }
        if (this.tcpMessageChannels.get(key) == tcpMessageChannel) {
            this.tcpMessageChannels.remove(key);
        }
        this.incomingTcpMessageChannels.remove(tcpMessageChannel);
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public synchronized MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
        String key = MessageChannel.getKey(targetHostPort, ListeningPoint.TCP);
        if (this.tcpMessageChannels.get(key) != null) {
            return (TCPMessageChannel) this.tcpMessageChannels.get(key);
        }
        TCPMessageChannel retval = new TCPMessageChannel(targetHostPort.getInetAddress(), targetHostPort.getPort(), this.sipStack, this);
        this.tcpMessageChannels.put(key, retval);
        retval.isCached = true;
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger = this.sipStack.getStackLogger();
            stackLogger.logDebug("key " + key);
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("Creating " + retval);
        }
        return retval;
    }

    /* access modifiers changed from: protected */
    public synchronized void cacheMessageChannel(TCPMessageChannel messageChannel) {
        String key = messageChannel.getKey();
        TCPMessageChannel currentChannel = (TCPMessageChannel) this.tcpMessageChannels.get(key);
        if (currentChannel != null) {
            if (this.sipStack.isLoggingEnabled()) {
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("Closing " + key);
            }
            currentChannel.close();
        }
        if (this.sipStack.isLoggingEnabled()) {
            StackLogger stackLogger2 = this.sipStack.getStackLogger();
            stackLogger2.logDebug("Caching " + key);
        }
        this.tcpMessageChannels.put(key, messageChannel);
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
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
                StackLogger stackLogger = this.sipStack.getStackLogger();
                stackLogger.logDebug("key " + key);
                StackLogger stackLogger2 = this.sipStack.getStackLogger();
                stackLogger2.logDebug("Creating " + retval);
            }
            return retval;
        } catch (UnknownHostException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public int getMaximumMessageSize() {
        return Integer.MAX_VALUE;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public boolean inUse() {
        return this.useCount != 0;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public int getDefaultTargetPort() {
        return 5060;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public boolean isSecure() {
        return false;
    }
}

package gov.nist.javax.sip.stack;

import gov.nist.core.HostPort;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.StackLogger;
import gov.nist.core.ThreadAuditor;
import gov.nist.javax.sip.address.ParameterNames;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import org.ccil.cowan.tagsoup.HTMLModels;

public class UDPMessageProcessor extends MessageProcessor {
    private static final int HIGHWAT = 5000;
    private static final int LOWAT = 2500;
    protected boolean isRunning;
    protected LinkedList messageChannels;
    protected LinkedList messageQueue = new LinkedList();
    private int port;
    protected DatagramSocket sock;
    protected int threadPoolSize;

    protected UDPMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port2) throws IOException {
        super(ipAddress, port2, ParameterNames.UDP, sipStack);
        this.sipStack = sipStack;
        this.port = port2;
        try {
            this.sock = sipStack.getNetworkLayer().createDatagramSocket(port2, ipAddress);
            this.sock.setReceiveBufferSize(sipStack.getReceiveUdpBufferSize());
            this.sock.setSendBufferSize(sipStack.getSendUdpBufferSize());
            if (sipStack.getThreadAuditor().isEnabled()) {
                this.sock.setSoTimeout((int) sipStack.getThreadAuditor().getPingIntervalInMillisecs());
            }
            if (ipAddress.getHostAddress().equals("0.0.0.0") || ipAddress.getHostAddress().equals("::0")) {
                super.setIpAddress(this.sock.getLocalAddress());
            }
        } catch (SocketException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public int getPort() {
        return this.port;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public void start() throws IOException {
        this.isRunning = true;
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("UDPMessageProcessorThread");
        thread.setPriority(10);
        thread.start();
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public void run() {
        this.messageChannels = new LinkedList();
        if (this.sipStack.threadPoolSize != -1) {
            for (int i = 0; i < this.sipStack.threadPoolSize; i++) {
                this.messageChannels.add(new UDPMessageChannel(this.sipStack, this));
            }
        }
        ThreadAuditor.ThreadHandle threadHandle = this.sipStack.getThreadAuditor().addCurrentThread();
        while (this.isRunning) {
            try {
                threadHandle.ping();
                int bufsize = this.sock.getReceiveBufferSize();
                DatagramPacket packet = new DatagramPacket(new byte[bufsize], bufsize);
                this.sock.receive(packet);
                if (this.sipStack.stackDoesCongestionControl) {
                    if (this.messageQueue.size() >= HIGHWAT) {
                        if (this.sipStack.isLoggingEnabled()) {
                            this.sipStack.getStackLogger().logDebug("Dropping message -- queue length exceeded");
                        }
                    } else if (this.messageQueue.size() > LOWAT && this.messageQueue.size() < HIGHWAT) {
                        float threshold = ((float) (this.messageQueue.size() - LOWAT)) / 2500.0f;
                        if (Math.random() > 1.0d - ((double) threshold)) {
                            if (this.sipStack.isLoggingEnabled()) {
                                StackLogger stackLogger = this.sipStack.getStackLogger();
                                stackLogger.logDebug("Dropping message with probability  " + (1.0d - ((double) threshold)));
                            }
                        }
                    }
                }
                if (this.sipStack.threadPoolSize != -1) {
                    synchronized (this.messageQueue) {
                        this.messageQueue.add(packet);
                        this.messageQueue.notify();
                    }
                } else {
                    new UDPMessageChannel(this.sipStack, this, packet);
                }
            } catch (SocketTimeoutException e) {
            } catch (SocketException e2) {
                if (this.sipStack.isLoggingEnabled()) {
                    getSIPStack().getStackLogger().logDebug("UDPMessageProcessor: Stopping");
                }
                this.isRunning = false;
                synchronized (this.messageQueue) {
                    this.messageQueue.notifyAll();
                }
            } catch (IOException ex) {
                this.isRunning = false;
                ex.printStackTrace();
                if (this.sipStack.isLoggingEnabled()) {
                    getSIPStack().getStackLogger().logDebug("UDPMessageProcessor: Got an IO Exception");
                }
            } catch (Exception ex2) {
                if (this.sipStack.isLoggingEnabled()) {
                    getSIPStack().getStackLogger().logDebug("UDPMessageProcessor: Unexpected Exception - quitting");
                }
                InternalErrorHandler.handleException(ex2);
                return;
            }
        }
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public void stop() {
        synchronized (this.messageQueue) {
            this.isRunning = false;
            this.messageQueue.notifyAll();
            this.sock.close();
        }
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public String getTransport() {
        return ParameterNames.UDP;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public SIPTransactionStack getSIPStack() {
        return this.sipStack;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public MessageChannel createMessageChannel(HostPort targetHostPort) throws UnknownHostException {
        return new UDPMessageChannel(targetHostPort.getInetAddress(), targetHostPort.getPort(), this.sipStack, this);
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public MessageChannel createMessageChannel(InetAddress host, int port2) throws IOException {
        return new UDPMessageChannel(host, port2, this.sipStack, this);
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public int getDefaultTargetPort() {
        return 5060;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public boolean isSecure() {
        return false;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public int getMaximumMessageSize() {
        return HTMLModels.M_LEGEND;
    }

    @Override // gov.nist.javax.sip.stack.MessageProcessor
    public boolean inUse() {
        boolean z;
        synchronized (this.messageQueue) {
            z = this.messageQueue.size() != 0;
        }
        return z;
    }
}

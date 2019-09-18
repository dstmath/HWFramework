package gov.nist.javax.sip.stack;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.header.Via;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;

public abstract class MessageProcessor implements Runnable {
    protected static final String IN6_ADDR_ANY = "::0";
    protected static final String IN_ADDR_ANY = "0.0.0.0";
    private InetAddress ipAddress;
    private ListeningPointImpl listeningPoint;
    private int port;
    private String savedIpAddress;
    private String sentBy;
    private HostPort sentByHostPort;
    private boolean sentBySet;
    protected SIPTransactionStack sipStack;
    protected String transport;

    public abstract MessageChannel createMessageChannel(HostPort hostPort) throws IOException;

    public abstract MessageChannel createMessageChannel(InetAddress inetAddress, int i) throws IOException;

    public abstract int getDefaultTargetPort();

    public abstract int getMaximumMessageSize();

    public abstract SIPTransactionStack getSIPStack();

    public abstract boolean inUse();

    public abstract boolean isSecure();

    public abstract void run();

    public abstract void start() throws IOException;

    public abstract void stop();

    protected MessageProcessor(String transport2) {
        this.transport = transport2;
    }

    protected MessageProcessor(InetAddress ipAddress2, int port2, String transport2, SIPTransactionStack transactionStack) {
        this(transport2);
        initialize(ipAddress2, port2, transactionStack);
    }

    public final void initialize(InetAddress ipAddress2, int port2, SIPTransactionStack transactionStack) {
        this.sipStack = transactionStack;
        this.savedIpAddress = ipAddress2.getHostAddress();
        this.ipAddress = ipAddress2;
        this.port = port2;
        this.sentByHostPort = new HostPort();
        this.sentByHostPort.setHost(new Host(ipAddress2.getHostAddress()));
        this.sentByHostPort.setPort(port2);
    }

    public String getTransport() {
        return this.transport;
    }

    public int getPort() {
        return this.port;
    }

    public Via getViaHeader() {
        try {
            Via via = new Via();
            if (this.sentByHostPort != null) {
                via.setSentBy(this.sentByHostPort);
                via.setTransport(getTransport());
            } else {
                Host host = new Host();
                host.setHostname(getIpAddress().getHostAddress());
                via.setHost(host);
                via.setPort(getPort());
                via.setTransport(getTransport());
            }
            return via;
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        } catch (InvalidArgumentException ex2) {
            ex2.printStackTrace();
            return null;
        }
    }

    public ListeningPointImpl getListeningPoint() {
        if (this.listeningPoint == null && getSIPStack().isLoggingEnabled()) {
            StackLogger stackLogger = getSIPStack().getStackLogger();
            stackLogger.logError("getListeningPoint" + this + " returning null listeningpoint");
        }
        return this.listeningPoint;
    }

    public void setListeningPoint(ListeningPointImpl lp) {
        if (getSIPStack().isLoggingEnabled()) {
            StackLogger stackLogger = getSIPStack().getStackLogger();
            stackLogger.logDebug("setListeningPoint" + this + " listeningPoint = " + lp);
        }
        if (lp.getPort() != getPort()) {
            InternalErrorHandler.handleException("lp mismatch with provider", getSIPStack().getStackLogger());
        }
        this.listeningPoint = lp;
    }

    public String getSavedIpAddress() {
        return this.savedIpAddress;
    }

    public InetAddress getIpAddress() {
        return this.ipAddress;
    }

    /* access modifiers changed from: protected */
    public void setIpAddress(InetAddress ipAddress2) {
        this.sentByHostPort.setHost(new Host(ipAddress2.getHostAddress()));
        this.ipAddress = ipAddress2;
    }

    public void setSentBy(String sentBy2) throws ParseException {
        int ind = sentBy2.indexOf(Separators.COLON);
        if (ind == -1) {
            this.sentByHostPort = new HostPort();
            this.sentByHostPort.setHost(new Host(sentBy2));
        } else {
            this.sentByHostPort = new HostPort();
            this.sentByHostPort.setHost(new Host(sentBy2.substring(0, ind)));
            try {
                this.sentByHostPort.setPort(Integer.parseInt(sentBy2.substring(ind + 1)));
            } catch (NumberFormatException e) {
                throw new ParseException("Bad format encountered at ", ind);
            }
        }
        this.sentBySet = true;
        this.sentBy = sentBy2;
    }

    public String getSentBy() {
        if (this.sentBy == null && this.sentByHostPort != null) {
            this.sentBy = this.sentByHostPort.toString();
        }
        return this.sentBy;
    }

    public boolean isSentBySet() {
        return this.sentBySet;
    }

    public static int getDefaultPort(String transport2) {
        return transport2.equalsIgnoreCase(ListeningPoint.TLS) ? 5061 : 5060;
    }
}

package gov.nist.javax.sip.stack;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
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

    protected MessageProcessor(String transport) {
        this.transport = transport;
    }

    protected MessageProcessor(InetAddress ipAddress, int port, String transport, SIPTransactionStack transactionStack) {
        this(transport);
        initialize(ipAddress, port, transactionStack);
    }

    public final void initialize(InetAddress ipAddress, int port, SIPTransactionStack transactionStack) {
        this.sipStack = transactionStack;
        this.savedIpAddress = ipAddress.getHostAddress();
        this.ipAddress = ipAddress;
        this.port = port;
        this.sentByHostPort = new HostPort();
        this.sentByHostPort.setHost(new Host(ipAddress.getHostAddress()));
        this.sentByHostPort.setPort(port);
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
            getSIPStack().getStackLogger().logError("getListeningPoint" + this + " returning null listeningpoint");
        }
        return this.listeningPoint;
    }

    public void setListeningPoint(ListeningPointImpl lp) {
        if (getSIPStack().isLoggingEnabled()) {
            getSIPStack().getStackLogger().logDebug("setListeningPoint" + this + " listeningPoint = " + lp);
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

    protected void setIpAddress(InetAddress ipAddress) {
        this.sentByHostPort.setHost(new Host(ipAddress.getHostAddress()));
        this.ipAddress = ipAddress;
    }

    public void setSentBy(String sentBy) throws ParseException {
        int ind = sentBy.indexOf(Separators.COLON);
        if (ind == -1) {
            this.sentByHostPort = new HostPort();
            this.sentByHostPort.setHost(new Host(sentBy));
        } else {
            this.sentByHostPort = new HostPort();
            this.sentByHostPort.setHost(new Host(sentBy.substring(0, ind)));
            try {
                this.sentByHostPort.setPort(Integer.parseInt(sentBy.substring(ind + 1)));
            } catch (NumberFormatException e) {
                throw new ParseException("Bad format encountered at ", ind);
            }
        }
        this.sentBySet = true;
        this.sentBy = sentBy;
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

    public static int getDefaultPort(String transport) {
        return transport.equalsIgnoreCase(ListeningPoint.TLS) ? 5061 : 5060;
    }
}

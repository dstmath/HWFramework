package gov.nist.javax.sip.header;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.javax.sip.stack.HopImpl;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.address.Hop;
import javax.sip.header.ViaHeader;

public class Via extends ParametersHeader implements ViaHeader, ViaHeaderExt {
    public static final String BRANCH = "branch";
    public static final String MADDR = "maddr";
    public static final String RECEIVED = "received";
    public static final String RPORT = "rport";
    public static final String TTL = "ttl";
    private static final long serialVersionUID = 5281728373401351378L;
    protected String comment;
    private boolean rPortFlag = false;
    protected HostPort sentBy;
    protected Protocol sentProtocol = new Protocol();

    public Via() {
        super("Via");
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == this) {
            return true;
        }
        if (!(other instanceof ViaHeader)) {
            return false;
        }
        ViaHeader o = (ViaHeader) other;
        if (getProtocol().equalsIgnoreCase(o.getProtocol()) && getTransport().equalsIgnoreCase(o.getTransport()) && getHost().equalsIgnoreCase(o.getHost()) && getPort() == o.getPort()) {
            z = equalParameters(o);
        }
        return z;
    }

    public String getProtocolVersion() {
        if (this.sentProtocol == null) {
            return null;
        }
        return this.sentProtocol.getProtocolVersion();
    }

    public Protocol getSentProtocol() {
        return this.sentProtocol;
    }

    public HostPort getSentBy() {
        return this.sentBy;
    }

    public Hop getHop() {
        return new HopImpl(this.sentBy.getHost().getHostname(), this.sentBy.getPort(), this.sentProtocol.getTransport());
    }

    public NameValueList getViaParms() {
        return this.parameters;
    }

    public String getComment() {
        return this.comment;
    }

    public boolean hasPort() {
        return getSentBy().hasPort();
    }

    public boolean hasComment() {
        return this.comment != null;
    }

    public void removePort() {
        this.sentBy.removePort();
    }

    public void removeComment() {
        this.comment = null;
    }

    public void setProtocolVersion(String protocolVersion) {
        if (this.sentProtocol == null) {
            this.sentProtocol = new Protocol();
        }
        this.sentProtocol.setProtocolVersion(protocolVersion);
    }

    public void setHost(Host host) {
        if (this.sentBy == null) {
            this.sentBy = new HostPort();
        }
        this.sentBy.setHost(host);
    }

    public void setSentProtocol(Protocol s) {
        this.sentProtocol = s;
    }

    public void setSentBy(HostPort s) {
        this.sentBy = s;
    }

    public void setComment(String c) {
        this.comment = c;
    }

    protected String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        this.sentProtocol.encode(buffer);
        buffer.append(Separators.SP);
        this.sentBy.encode(buffer);
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        if (this.comment != null) {
            buffer.append(Separators.SP).append(Separators.LPAREN).append(this.comment).append(Separators.RPAREN);
        }
        if (this.rPortFlag) {
            buffer.append(";rport");
        }
        return buffer;
    }

    public void setHost(String host) throws ParseException {
        if (this.sentBy == null) {
            this.sentBy = new HostPort();
        }
        try {
            this.sentBy.setHost(new Host(host));
        } catch (Exception e) {
            throw new NullPointerException(" host parameter is null");
        }
    }

    public String getHost() {
        if (this.sentBy == null) {
            return null;
        }
        Host host = this.sentBy.getHost();
        if (host == null) {
            return null;
        }
        return host.getHostname();
    }

    public void setPort(int port) throws InvalidArgumentException {
        if (port == -1 || (port >= 1 && port <= 65535)) {
            if (this.sentBy == null) {
                this.sentBy = new HostPort();
            }
            this.sentBy.setPort(port);
            return;
        }
        throw new InvalidArgumentException("Port value out of range -1, [1..65535]");
    }

    public void setRPort() {
        this.rPortFlag = true;
    }

    public int getPort() {
        if (this.sentBy == null) {
            return -1;
        }
        return this.sentBy.getPort();
    }

    public int getRPort() {
        String strRport = getParameter("rport");
        if (strRport == null || (strRport.equals("") ^ 1) == 0) {
            return -1;
        }
        return Integer.valueOf(strRport).intValue();
    }

    public String getTransport() {
        if (this.sentProtocol == null) {
            return null;
        }
        return this.sentProtocol.getTransport();
    }

    public void setTransport(String transport) throws ParseException {
        if (transport == null) {
            throw new NullPointerException("JAIN-SIP Exception, Via, setTransport(), the transport parameter is null.");
        }
        if (this.sentProtocol == null) {
            this.sentProtocol = new Protocol();
        }
        this.sentProtocol.setTransport(transport);
    }

    public String getProtocol() {
        if (this.sentProtocol == null) {
            return null;
        }
        return this.sentProtocol.getProtocol();
    }

    public void setProtocol(String protocol) throws ParseException {
        if (protocol == null) {
            throw new NullPointerException("JAIN-SIP Exception, Via, setProtocol(), the protocol parameter is null.");
        }
        if (this.sentProtocol == null) {
            this.sentProtocol = new Protocol();
        }
        this.sentProtocol.setProtocol(protocol);
    }

    public int getTTL() {
        return getParameterAsInt("ttl");
    }

    public void setTTL(int ttl) throws InvalidArgumentException {
        if (ttl >= 0 || ttl == -1) {
            setParameter(new NameValue("ttl", Integer.valueOf(ttl)));
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, Via, setTTL(), the ttl parameter is < 0");
    }

    public String getMAddr() {
        return getParameter("maddr");
    }

    public void setMAddr(String mAddr) throws ParseException {
        if (mAddr == null) {
            throw new NullPointerException("JAIN-SIP Exception, Via, setMAddr(), the mAddr parameter is null.");
        }
        Host host = new Host();
        host.setAddress(mAddr);
        setParameter(new NameValue("maddr", host));
    }

    public String getReceived() {
        return getParameter("received");
    }

    public void setReceived(String received) throws ParseException {
        if (received == null) {
            throw new NullPointerException("JAIN-SIP Exception, Via, setReceived(), the received parameter is null.");
        }
        setParameter("received", received);
    }

    public String getBranch() {
        return getParameter("branch");
    }

    public void setBranch(String branch) throws ParseException {
        if (branch == null || branch.length() == 0) {
            throw new NullPointerException("JAIN-SIP Exception, Via, setBranch(), the branch parameter is null or length 0.");
        }
        setParameter("branch", branch);
    }

    public Object clone() {
        Via retval = (Via) super.clone();
        if (this.sentProtocol != null) {
            retval.sentProtocol = (Protocol) this.sentProtocol.clone();
        }
        if (this.sentBy != null) {
            retval.sentBy = (HostPort) this.sentBy.clone();
        }
        if (getRPort() != -1) {
            retval.setParameter("rport", getRPort());
        }
        return retval;
    }

    public String getSentByField() {
        if (this.sentBy != null) {
            return this.sentBy.encode();
        }
        return null;
    }

    public String getSentProtocolField() {
        if (this.sentProtocol != null) {
            return this.sentProtocol.encode();
        }
        return null;
    }
}

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

    @Override // gov.nist.core.GenericObject, gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ViaHeader)) {
            return false;
        }
        ViaHeader o = (ViaHeader) other;
        if (!getProtocol().equalsIgnoreCase(o.getProtocol()) || !getTransport().equalsIgnoreCase(o.getTransport()) || !getHost().equalsIgnoreCase(o.getHost()) || getPort() != o.getPort() || !equalParameters(o)) {
            return false;
        }
        return true;
    }

    public String getProtocolVersion() {
        Protocol protocol = this.sentProtocol;
        if (protocol == null) {
            return null;
        }
        return protocol.getProtocolVersion();
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

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        this.sentProtocol.encode(buffer);
        buffer.append(Separators.SP);
        this.sentBy.encode(buffer);
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        if (this.comment != null) {
            buffer.append(Separators.SP);
            buffer.append(Separators.LPAREN);
            buffer.append(this.comment);
            buffer.append(Separators.RPAREN);
        }
        if (this.rPortFlag) {
            buffer.append(";rport");
        }
        return buffer;
    }

    @Override // javax.sip.header.ViaHeader
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

    @Override // javax.sip.header.ViaHeader
    public String getHost() {
        Host host;
        HostPort hostPort = this.sentBy;
        if (hostPort == null || (host = hostPort.getHost()) == null) {
            return null;
        }
        return host.getHostname();
    }

    @Override // javax.sip.header.ViaHeader
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

    @Override // javax.sip.header.ViaHeader
    public void setRPort() {
        this.rPortFlag = true;
    }

    @Override // javax.sip.header.ViaHeader
    public int getPort() {
        HostPort hostPort = this.sentBy;
        if (hostPort == null) {
            return -1;
        }
        return hostPort.getPort();
    }

    @Override // javax.sip.header.ViaHeader
    public int getRPort() {
        String strRport = getParameter("rport");
        if (strRport == null || strRport.equals("")) {
            return -1;
        }
        return Integer.valueOf(strRport).intValue();
    }

    @Override // javax.sip.header.ViaHeader
    public String getTransport() {
        Protocol protocol = this.sentProtocol;
        if (protocol == null) {
            return null;
        }
        return protocol.getTransport();
    }

    @Override // javax.sip.header.ViaHeader
    public void setTransport(String transport) throws ParseException {
        if (transport != null) {
            if (this.sentProtocol == null) {
                this.sentProtocol = new Protocol();
            }
            this.sentProtocol.setTransport(transport);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Via, setTransport(), the transport parameter is null.");
    }

    @Override // javax.sip.header.ViaHeader
    public String getProtocol() {
        Protocol protocol = this.sentProtocol;
        if (protocol == null) {
            return null;
        }
        return protocol.getProtocol();
    }

    @Override // javax.sip.header.ViaHeader
    public void setProtocol(String protocol) throws ParseException {
        if (protocol != null) {
            if (this.sentProtocol == null) {
                this.sentProtocol = new Protocol();
            }
            this.sentProtocol.setProtocol(protocol);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Via, setProtocol(), the protocol parameter is null.");
    }

    @Override // javax.sip.header.ViaHeader
    public int getTTL() {
        return getParameterAsInt("ttl");
    }

    @Override // javax.sip.header.ViaHeader
    public void setTTL(int ttl) throws InvalidArgumentException {
        if (ttl >= 0 || ttl == -1) {
            setParameter(new NameValue("ttl", Integer.valueOf(ttl)));
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, Via, setTTL(), the ttl parameter is < 0");
    }

    @Override // javax.sip.header.ViaHeader
    public String getMAddr() {
        return getParameter("maddr");
    }

    @Override // javax.sip.header.ViaHeader
    public void setMAddr(String mAddr) throws ParseException {
        if (mAddr != null) {
            Host host = new Host();
            host.setAddress(mAddr);
            setParameter(new NameValue("maddr", host));
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Via, setMAddr(), the mAddr parameter is null.");
    }

    @Override // javax.sip.header.ViaHeader
    public String getReceived() {
        return getParameter("received");
    }

    @Override // javax.sip.header.ViaHeader
    public void setReceived(String received) throws ParseException {
        if (received != null) {
            setParameter("received", received);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, Via, setReceived(), the received parameter is null.");
    }

    @Override // javax.sip.header.ViaHeader
    public String getBranch() {
        return getParameter("branch");
    }

    @Override // javax.sip.header.ViaHeader
    public void setBranch(String branch) throws ParseException {
        if (branch == null || branch.length() == 0) {
            throw new NullPointerException("JAIN-SIP Exception, Via, setBranch(), the branch parameter is null or length 0.");
        }
        setParameter("branch", branch);
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.core.GenericObject, java.lang.Object, javax.sip.header.Header
    public Object clone() {
        Via retval = (Via) super.clone();
        Protocol protocol = this.sentProtocol;
        if (protocol != null) {
            retval.sentProtocol = (Protocol) protocol.clone();
        }
        HostPort hostPort = this.sentBy;
        if (hostPort != null) {
            retval.sentBy = (HostPort) hostPort.clone();
        }
        if (getRPort() != -1) {
            retval.setParameter("rport", getRPort());
        }
        return retval;
    }

    @Override // javax.sip.header.ViaHeader, gov.nist.javax.sip.header.ViaHeaderExt
    public String getSentByField() {
        HostPort hostPort = this.sentBy;
        if (hostPort != null) {
            return hostPort.encode();
        }
        return null;
    }

    @Override // javax.sip.header.ViaHeader, gov.nist.javax.sip.header.ViaHeaderExt
    public String getSentProtocolField() {
        Protocol protocol = this.sentProtocol;
        if (protocol != null) {
            return protocol.encode();
        }
        return null;
    }
}

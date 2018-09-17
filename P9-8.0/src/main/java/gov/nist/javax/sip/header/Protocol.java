package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.ListeningPoint;

public class Protocol extends SIPObject {
    private static final long serialVersionUID = 2216758055974073280L;
    protected String protocolName = "SIP";
    protected String protocolVersion = "2.0";
    protected String transport = ListeningPoint.UDP;

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(this.protocolName.toUpperCase()).append(Separators.SLASH).append(this.protocolVersion).append(Separators.SLASH).append(this.transport.toUpperCase());
        return buffer;
    }

    public String getProtocolName() {
        return this.protocolName;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public String getProtocol() {
        return this.protocolName + '/' + this.protocolVersion;
    }

    public void setProtocol(String name_and_version) throws ParseException {
        int slash = name_and_version.indexOf(47);
        if (slash > 0) {
            this.protocolName = name_and_version.substring(0, slash);
            this.protocolVersion = name_and_version.substring(slash + 1);
            return;
        }
        throw new ParseException("Missing '/' in protocol", 0);
    }

    public String getTransport() {
        return this.transport;
    }

    public void setProtocolName(String p) {
        this.protocolName = p;
    }

    public void setProtocolVersion(String p) {
        this.protocolVersion = p;
    }

    public void setTransport(String t) {
        this.transport = t;
    }
}

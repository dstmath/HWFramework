package gov.nist.javax.sip.header;

import gov.nist.core.HostPort;
import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import javax.sip.header.ReplyToHeader;

public final class ReplyTo extends AddressParametersHeader implements ReplyToHeader {
    private static final long serialVersionUID = -9103698729465531373L;

    public ReplyTo() {
        super("Reply-To");
    }

    public ReplyTo(AddressImpl address) {
        super("Reply-To");
        this.address = address;
    }

    @Override // gov.nist.core.GenericObject, gov.nist.javax.sip.header.SIPHeader, gov.nist.javax.sip.header.SIPObject
    public String encode() {
        return this.headerName + Separators.COLON + Separators.SP + encodeBody() + Separators.NEWLINE;
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        String retval = "";
        if (this.address.getAddressType() == 2) {
            retval = retval + Separators.LESS_THAN;
        }
        String retval2 = retval + this.address.encode();
        if (this.address.getAddressType() == 2) {
            retval2 = retval2 + Separators.GREATER_THAN;
        }
        if (this.parameters.isEmpty()) {
            return retval2;
        }
        return retval2 + Separators.SEMICOLON + this.parameters.encode();
    }

    public HostPort getHostPort() {
        return this.address.getHostPort();
    }

    @Override // javax.sip.header.ReplyToHeader
    public String getDisplayName() {
        return this.address.getDisplayName();
    }
}

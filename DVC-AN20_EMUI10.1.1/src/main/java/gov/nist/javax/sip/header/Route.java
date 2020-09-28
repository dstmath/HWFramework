package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import javax.sip.header.RouteHeader;

public class Route extends AddressParametersHeader implements RouteHeader {
    private static final long serialVersionUID = 5683577362998368846L;

    public Route() {
        super("Route");
    }

    public Route(AddressImpl address) {
        super("Route");
        this.address = address;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader, javax.sip.header.Header
    public int hashCode() {
        return this.address.getHostPort().encode().toLowerCase().hashCode();
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        boolean addrFlag = true;
        if (this.address.getAddressType() != 1) {
            addrFlag = false;
        }
        if (!addrFlag) {
            buffer.append('<');
            this.address.encode(buffer);
            buffer.append('>');
        } else {
            this.address.encode(buffer);
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    @Override // gov.nist.javax.sip.header.AddressParametersHeader, gov.nist.core.GenericObject, gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header
    public boolean equals(Object other) {
        return (other instanceof RouteHeader) && super.equals(other);
    }
}

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

    public int hashCode() {
        return this.address.getHostPort().encode().toLowerCase().hashCode();
    }

    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
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

    public boolean equals(Object other) {
        return (other instanceof RouteHeader) && super.equals(other);
    }
}

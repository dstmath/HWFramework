package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.header.ReferToHeader;

public final class ReferTo extends AddressParametersHeader implements ReferToHeader {
    private static final long serialVersionUID = -1666700428440034851L;

    public ReferTo() {
        super(ReferToHeader.NAME);
    }

    protected String encodeBody() {
        if (this.address == null) {
            return null;
        }
        String retval = "";
        if (this.address.getAddressType() == 2) {
            retval = retval + Separators.LESS_THAN;
        }
        retval = retval + this.address.encode();
        if (this.address.getAddressType() == 2) {
            retval = retval + Separators.GREATER_THAN;
        }
        if (!this.parameters.isEmpty()) {
            retval = retval + Separators.SEMICOLON + this.parameters.encode();
        }
        return retval;
    }
}

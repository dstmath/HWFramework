package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import javax.sip.header.ReferToHeader;

public final class ReferTo extends AddressParametersHeader implements ReferToHeader {
    private static final long serialVersionUID = -1666700428440034851L;

    public ReferTo() {
        super(ReferToHeader.NAME);
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        if (this.address == null) {
            return null;
        }
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
}

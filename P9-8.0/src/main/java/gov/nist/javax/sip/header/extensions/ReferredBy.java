package gov.nist.javax.sip.header.extensions;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.AddressParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public final class ReferredBy extends AddressParametersHeader implements ExtensionHeader, ReferredByHeader {
    public static final String NAME = "Referred-By";
    private static final long serialVersionUID = 3134344915465784267L;

    public ReferredBy() {
        super("Referred-By");
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
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

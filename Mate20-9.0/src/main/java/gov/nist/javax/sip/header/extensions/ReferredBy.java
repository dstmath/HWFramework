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

    /* access modifiers changed from: protected */
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
        if (!this.parameters.isEmpty()) {
            retval2 = retval2 + Separators.SEMICOLON + this.parameters.encode();
        }
        return retval2;
    }
}

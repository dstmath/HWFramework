package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.AddressParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class PAssertedIdentity extends AddressParametersHeader implements PAssertedIdentityHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PAssertedIdentity(AddressImpl address) {
        super(SIPHeaderNamesIms.P_ASSERTED_IDENTITY);
        this.address = address;
    }

    public PAssertedIdentity() {
        super(SIPHeaderNamesIms.P_ASSERTED_IDENTITY);
    }

    public String encodeBody() {
        StringBuffer retval = new StringBuffer();
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.LESS_THAN);
        }
        retval.append(this.address.encode());
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.GREATER_THAN);
        }
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.COMMA + this.parameters.encode());
        }
        return retval.toString();
    }

    public Object clone() {
        return (PAssertedIdentity) super.clone();
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}

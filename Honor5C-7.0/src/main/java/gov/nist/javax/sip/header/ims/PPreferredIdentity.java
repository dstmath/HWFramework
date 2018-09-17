package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.AddressParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class PPreferredIdentity extends AddressParametersHeader implements PPreferredIdentityHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PPreferredIdentity(AddressImpl address) {
        super(PPreferredIdentityHeader.NAME);
        this.address = address;
    }

    public PPreferredIdentity() {
        super(PPreferredIdentityHeader.NAME);
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
        return retval.toString();
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}

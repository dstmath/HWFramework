package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.AddressParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class Path extends AddressParametersHeader implements PathHeader, SIPHeaderNamesIms, ExtensionHeader {
    public Path(AddressImpl address) {
        super("Path");
        this.address = address;
    }

    public Path() {
        super("Path");
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
            retval.append(Separators.SEMICOLON + this.parameters.encode());
        }
        return retval.toString();
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}

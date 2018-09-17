package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.AddressParametersHeader;
import java.text.ParseException;
import javax.sip.header.ExtensionHeader;

public class PProfileKey extends AddressParametersHeader implements PProfileKeyHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PProfileKey() {
        super("P-Profile-Key");
    }

    public PProfileKey(AddressImpl address) {
        super("P-Profile-Key");
        this.address = address;
    }

    protected String encodeBody() {
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

    public boolean equals(Object other) {
        return other instanceof PProfileKey ? super.equals(other) : false;
    }

    public Object clone() {
        return (PProfileKey) super.clone();
    }
}

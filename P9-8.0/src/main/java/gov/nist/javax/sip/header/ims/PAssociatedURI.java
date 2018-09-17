package gov.nist.javax.sip.header.ims;

import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.header.AddressParametersHeader;
import java.text.ParseException;
import javax.sip.address.URI;
import javax.sip.header.ExtensionHeader;

public class PAssociatedURI extends AddressParametersHeader implements PAssociatedURIHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PAssociatedURI() {
        super("P-Associated-URI");
    }

    public PAssociatedURI(AddressImpl address) {
        super("P-Associated-URI");
        this.address = address;
    }

    public PAssociatedURI(GenericURI associatedURI) {
        super("P-Associated-URI");
        this.address = new AddressImpl();
        this.address.setURI(associatedURI);
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

    public void setAssociatedURI(URI associatedURI) throws NullPointerException {
        if (associatedURI == null) {
            throw new NullPointerException("null URI");
        }
        this.address.setURI(associatedURI);
    }

    public URI getAssociatedURI() {
        return this.address.getURI();
    }

    public Object clone() {
        PAssociatedURI retval = (PAssociatedURI) super.clone();
        if (this.address != null) {
            retval.address = (AddressImpl) this.address.clone();
        }
        return retval;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}

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

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
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

    @Override // gov.nist.javax.sip.header.ims.PAssociatedURIHeader
    public void setAssociatedURI(URI associatedURI) throws NullPointerException {
        if (associatedURI != null) {
            this.address.setURI(associatedURI);
            return;
        }
        throw new NullPointerException("null URI");
    }

    @Override // gov.nist.javax.sip.header.ims.PAssociatedURIHeader
    public URI getAssociatedURI() {
        return this.address.getURI();
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.AddressParametersHeader, gov.nist.core.GenericObject, java.lang.Object, javax.sip.header.Header
    public Object clone() {
        PAssociatedURI retval = (PAssociatedURI) super.clone();
        if (this.address != null) {
            retval.address = (AddressImpl) this.address.clone();
        }
        return retval;
    }

    @Override // javax.sip.header.ExtensionHeader
    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}

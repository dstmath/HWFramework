package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.SIPHeader;
import javax.sip.address.Address;

public abstract class AddressHeaderIms extends SIPHeader {
    protected AddressImpl address;

    @Override // gov.nist.javax.sip.header.SIPHeader
    public abstract String encodeBody();

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address2) {
        this.address = (AddressImpl) address2;
    }

    public AddressHeaderIms(String name) {
        super(name);
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        AddressHeaderIms retval = (AddressHeaderIms) super.clone();
        AddressImpl addressImpl = this.address;
        if (addressImpl != null) {
            retval.address = (AddressImpl) addressImpl.clone();
        }
        return retval;
    }
}

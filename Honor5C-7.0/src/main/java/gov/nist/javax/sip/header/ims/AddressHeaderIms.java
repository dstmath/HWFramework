package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.header.SIPHeader;
import javax.sip.address.Address;

public abstract class AddressHeaderIms extends SIPHeader {
    protected AddressImpl address;

    public abstract String encodeBody();

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = (AddressImpl) address;
    }

    public AddressHeaderIms(String name) {
        super(name);
    }

    public Object clone() {
        AddressHeaderIms retval = (AddressHeaderIms) super.clone();
        if (this.address != null) {
            retval.address = (AddressImpl) this.address.clone();
        }
        return retval;
    }
}

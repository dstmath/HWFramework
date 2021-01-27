package gov.nist.javax.sip.header;

import gov.nist.javax.sip.address.AddressImpl;
import javax.sip.address.Address;
import javax.sip.header.HeaderAddress;
import javax.sip.header.Parameters;

public abstract class AddressParametersHeader extends ParametersHeader implements Parameters {
    protected AddressImpl address;

    @Override // javax.sip.header.HeaderAddress
    public Address getAddress() {
        return this.address;
    }

    @Override // javax.sip.header.HeaderAddress
    public void setAddress(Address address2) {
        this.address = (AddressImpl) address2;
    }

    protected AddressParametersHeader(String name) {
        super(name);
    }

    protected AddressParametersHeader(String name, boolean sync) {
        super(name, sync);
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        AddressParametersHeader retval = (AddressParametersHeader) super.clone();
        AddressImpl addressImpl = this.address;
        if (addressImpl != null) {
            retval.address = (AddressImpl) addressImpl.clone();
        }
        return retval;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HeaderAddress) || !(other instanceof Parameters)) {
            return false;
        }
        HeaderAddress o = (HeaderAddress) other;
        if (!getAddress().equals(o.getAddress()) || !equalParameters((Parameters) o)) {
            return false;
        }
        return true;
    }
}

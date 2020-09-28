package gov.nist.javax.sip.address;

import gov.nist.core.HostPort;
import gov.nist.core.Separators;
import javax.sip.address.Address;
import javax.sip.address.URI;

public final class AddressImpl extends NetObject implements Address {
    public static final int ADDRESS_SPEC = 2;
    public static final int NAME_ADDR = 1;
    public static final int WILD_CARD = 3;
    private static final long serialVersionUID = 429592779568617259L;
    protected GenericURI address;
    protected int addressType = 1;
    protected String displayName;

    @Override // gov.nist.javax.sip.address.NetObject, gov.nist.core.GenericObject
    public boolean match(Object other) {
        if (other == null) {
            return true;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        AddressImpl that = (AddressImpl) other;
        if (that.getMatcher() != null) {
            return that.getMatcher().match(encode());
        }
        if (that.displayName != null && this.displayName == null) {
            return false;
        }
        String str = that.displayName;
        if (str == null) {
            return this.address.match(that.address);
        }
        if (!this.displayName.equalsIgnoreCase(str) || !this.address.match(that.address)) {
            return false;
        }
        return true;
    }

    public HostPort getHostPort() {
        GenericURI genericURI = this.address;
        if (genericURI instanceof SipUri) {
            return ((SipUri) genericURI).getHostPort();
        }
        throw new RuntimeException("address is not a SipUri");
    }

    @Override // javax.sip.address.Address
    public int getPort() {
        GenericURI genericURI = this.address;
        if (genericURI instanceof SipUri) {
            return ((SipUri) genericURI).getHostPort().getPort();
        }
        throw new RuntimeException("address is not a SipUri");
    }

    @Override // javax.sip.address.Address
    public String getUserAtHostPort() {
        GenericURI genericURI = this.address;
        if (genericURI instanceof SipUri) {
            return ((SipUri) genericURI).getUserAtHostPort();
        }
        return genericURI.toString();
    }

    @Override // javax.sip.address.Address
    public String getHost() {
        GenericURI genericURI = this.address;
        if (genericURI instanceof SipUri) {
            return ((SipUri) genericURI).getHostPort().getHost().getHostname();
        }
        throw new RuntimeException("address is not a SipUri");
    }

    public void removeParameter(String parameterName) {
        GenericURI genericURI = this.address;
        if (genericURI instanceof SipUri) {
            ((SipUri) genericURI).removeParameter(parameterName);
            return;
        }
        throw new RuntimeException("address is not a SipUri");
    }

    @Override // gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        if (this.addressType == 3) {
            buffer.append('*');
        } else {
            if (this.displayName != null) {
                buffer.append(Separators.DOUBLE_QUOTE);
                buffer.append(this.displayName);
                buffer.append(Separators.DOUBLE_QUOTE);
                buffer.append(Separators.SP);
            }
            if (this.address != null) {
                if (this.addressType == 1 || this.displayName != null) {
                    buffer.append(Separators.LESS_THAN);
                }
                this.address.encode(buffer);
                if (this.addressType == 1 || this.displayName != null) {
                    buffer.append(Separators.GREATER_THAN);
                }
            }
        }
        return buffer;
    }

    public int getAddressType() {
        return this.addressType;
    }

    public void setAddressType(int atype) {
        this.addressType = atype;
    }

    @Override // javax.sip.address.Address
    public String getDisplayName() {
        return this.displayName;
    }

    @Override // javax.sip.address.Address
    public void setDisplayName(String displayName2) {
        this.displayName = displayName2;
        this.addressType = 1;
    }

    public void setAddess(URI address2) {
        this.address = (GenericURI) address2;
    }

    @Override // javax.sip.address.Address
    public int hashCode() {
        return this.address.hashCode();
    }

    @Override // javax.sip.address.Address, gov.nist.javax.sip.address.NetObject, gov.nist.core.GenericObject
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Address) {
            return getURI().equals(((Address) other).getURI());
        }
        return false;
    }

    @Override // javax.sip.address.Address
    public boolean hasDisplayName() {
        return this.displayName != null;
    }

    public void removeDisplayName() {
        this.displayName = null;
    }

    @Override // javax.sip.address.Address
    public boolean isSIPAddress() {
        return this.address instanceof SipUri;
    }

    @Override // javax.sip.address.Address
    public URI getURI() {
        return this.address;
    }

    @Override // javax.sip.address.Address
    public boolean isWildcard() {
        return this.addressType == 3;
    }

    @Override // javax.sip.address.Address
    public void setURI(URI address2) {
        this.address = (GenericURI) address2;
    }

    public void setUser(String user) {
        ((SipUri) this.address).setUser(user);
    }

    @Override // javax.sip.address.Address
    public void setWildCardFlag() {
        this.addressType = 3;
        this.address = new SipUri();
        ((SipUri) this.address).setUser(Separators.STAR);
    }

    @Override // javax.sip.address.Address, java.lang.Object, gov.nist.core.GenericObject
    public Object clone() {
        AddressImpl retval = (AddressImpl) super.clone();
        GenericURI genericURI = this.address;
        if (genericURI != null) {
            retval.address = (GenericURI) genericURI.clone();
        }
        return retval;
    }
}

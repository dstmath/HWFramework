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

    public boolean match(Object other) {
        boolean z = false;
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
        if (that.displayName == null) {
            return this.address.match(that.address);
        }
        if (this.displayName.equalsIgnoreCase(that.displayName)) {
            z = this.address.match(that.address);
        }
        return z;
    }

    public HostPort getHostPort() {
        if (this.address instanceof SipUri) {
            return this.address.getHostPort();
        }
        throw new RuntimeException("address is not a SipUri");
    }

    public int getPort() {
        if (this.address instanceof SipUri) {
            return this.address.getHostPort().getPort();
        }
        throw new RuntimeException("address is not a SipUri");
    }

    public String getUserAtHostPort() {
        if (this.address instanceof SipUri) {
            return this.address.getUserAtHostPort();
        }
        return this.address.toString();
    }

    public String getHost() {
        if (this.address instanceof SipUri) {
            return this.address.getHostPort().getHost().getHostname();
        }
        throw new RuntimeException("address is not a SipUri");
    }

    public void removeParameter(String parameterName) {
        if (this.address instanceof SipUri) {
            this.address.removeParameter(parameterName);
            return;
        }
        throw new RuntimeException("address is not a SipUri");
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (this.addressType == 3) {
            buffer.append('*');
        } else {
            if (this.displayName != null) {
                buffer.append(Separators.DOUBLE_QUOTE).append(this.displayName).append(Separators.DOUBLE_QUOTE).append(Separators.SP);
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

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.addressType = 1;
    }

    public void setAddess(URI address) {
        this.address = (GenericURI) address;
    }

    public int hashCode() {
        return this.address.hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        return getURI().equals(((Address) other).getURI());
    }

    public boolean hasDisplayName() {
        return this.displayName != null;
    }

    public void removeDisplayName() {
        this.displayName = null;
    }

    public boolean isSIPAddress() {
        return this.address instanceof SipUri;
    }

    public URI getURI() {
        return this.address;
    }

    public boolean isWildcard() {
        return this.addressType == 3;
    }

    public void setURI(URI address) {
        this.address = (GenericURI) address;
    }

    public void setUser(String user) {
        ((SipUri) this.address).setUser(user);
    }

    public void setWildCardFlag() {
        this.addressType = 3;
        this.address = new SipUri();
        ((SipUri) this.address).setUser(Separators.STAR);
    }

    public Object clone() {
        AddressImpl retval = (AddressImpl) super.clone();
        if (this.address != null) {
            retval.address = (GenericURI) this.address.clone();
        }
        return retval;
    }
}

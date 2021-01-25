package gov.nist.javax.sip.header;

import gov.nist.core.HostPort;
import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.parser.Parser;
import java.text.ParseException;
import javax.sip.address.Address;
import javax.sip.header.FromHeader;

public final class From extends AddressParametersHeader implements FromHeader {
    private static final long serialVersionUID = -6312727234330643892L;

    public From() {
        super("From");
    }

    public From(To to) {
        super("From");
        this.address = to.address;
        this.parameters = to.parameters;
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        if (this.address.getAddressType() == 2) {
            buffer.append(Separators.LESS_THAN);
        }
        this.address.encode(buffer);
        if (this.address.getAddressType() == 2) {
            buffer.append(Separators.GREATER_THAN);
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public HostPort getHostPort() {
        return this.address.getHostPort();
    }

    @Override // javax.sip.header.FromHeader
    public String getDisplayName() {
        return this.address.getDisplayName();
    }

    @Override // javax.sip.header.FromHeader
    public String getTag() {
        if (this.parameters == null) {
            return null;
        }
        return getParameter(ParameterNames.TAG);
    }

    @Override // javax.sip.header.FromHeader
    public boolean hasTag() {
        return hasParameter(ParameterNames.TAG);
    }

    @Override // javax.sip.header.FromHeader
    public void removeTag() {
        this.parameters.delete(ParameterNames.TAG);
    }

    @Override // gov.nist.javax.sip.header.AddressParametersHeader, javax.sip.header.HeaderAddress
    public void setAddress(Address address) {
        this.address = (AddressImpl) address;
    }

    @Override // javax.sip.header.FromHeader
    public void setTag(String t) throws ParseException {
        Parser.checkToken(t);
        setParameter(ParameterNames.TAG, t);
    }

    @Override // javax.sip.header.FromHeader
    public String getUserAtHostPort() {
        return this.address.getUserAtHostPort();
    }

    @Override // gov.nist.javax.sip.header.AddressParametersHeader, gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        return (other instanceof FromHeader) && super.equals(other);
    }
}

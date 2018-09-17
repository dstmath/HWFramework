package gov.nist.javax.sip.header;

import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import gov.nist.javax.sip.address.AddressImpl;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.address.Address;
import javax.sip.header.ContactHeader;

public final class Contact extends AddressParametersHeader implements ContactHeader {
    public static final String ACTION = "action";
    public static final String EXPIRES = "expires";
    public static final String PROXY = "proxy";
    public static final String Q = "q";
    public static final String REDIRECT = "redirect";
    private static final long serialVersionUID = 1677294871695706288L;
    private ContactList contactList;
    protected boolean wildCardFlag;

    public Contact() {
        super("Contact");
    }

    public void setParameter(String name, String value) throws ParseException {
        NameValue nv = this.parameters.getNameValue(name);
        if (nv != null) {
            nv.setValueAsObject(value);
            return;
        }
        nv = new NameValue(name, value);
        if (name.equalsIgnoreCase("methods")) {
            nv.setQuotedValue();
        }
        this.parameters.set(nv);
    }

    protected String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    protected StringBuffer encodeBody(StringBuffer buffer) {
        if (this.wildCardFlag) {
            buffer.append('*');
        } else {
            if (this.address.getAddressType() == 1) {
                this.address.encode(buffer);
            } else {
                buffer.append('<');
                this.address.encode(buffer);
                buffer.append('>');
            }
            if (!this.parameters.isEmpty()) {
                buffer.append(Separators.SEMICOLON);
                this.parameters.encode(buffer);
            }
        }
        return buffer;
    }

    public ContactList getContactList() {
        return this.contactList;
    }

    public boolean getWildCardFlag() {
        return this.wildCardFlag;
    }

    public Address getAddress() {
        return this.address;
    }

    public NameValueList getContactParms() {
        return this.parameters;
    }

    public int getExpires() {
        return getParameterAsInt("expires");
    }

    public void setExpires(int expiryDeltaSeconds) {
        this.parameters.set("expires", Integer.valueOf(expiryDeltaSeconds));
    }

    public float getQValue() {
        return getParameterAsFloat("q");
    }

    public void setContactList(ContactList cl) {
        this.contactList = cl;
    }

    public void setWildCardFlag(boolean w) {
        this.wildCardFlag = true;
        this.address = new AddressImpl();
        this.address.setWildCardFlag();
    }

    public void setAddress(Address address) {
        if (address == null) {
            throw new NullPointerException("null address");
        }
        this.address = (AddressImpl) address;
        this.wildCardFlag = false;
    }

    public void setQValue(float qValue) throws InvalidArgumentException {
        if (qValue == -1.0f || (qValue >= 0.0f && qValue <= 1.0f)) {
            this.parameters.set("q", Float.valueOf(qValue));
            return;
        }
        throw new InvalidArgumentException("JAIN-SIP Exception, Contact, setQValue(), the qValue is not between 0 and 1");
    }

    public Object clone() {
        Contact retval = (Contact) super.clone();
        if (this.contactList != null) {
            retval.contactList = (ContactList) this.contactList.clone();
        }
        return retval;
    }

    public void setWildCard() {
        setWildCardFlag(true);
    }

    public boolean isWildCard() {
        return this.address.isWildcard();
    }

    public boolean equals(Object other) {
        return other instanceof ContactHeader ? super.equals(other) : false;
    }

    public void removeSipInstanceParam() {
        if (this.parameters != null) {
            this.parameters.delete(ParameterNames.SIP_INSTANCE);
        }
    }

    public String getSipInstanceParam() {
        return (String) this.parameters.getValue(ParameterNames.SIP_INSTANCE);
    }

    public void setSipInstanceParam(String value) {
        this.parameters.set(ParameterNames.SIP_INSTANCE, value);
    }

    public void removePubGruuParam() {
        if (this.parameters != null) {
            this.parameters.delete(ParameterNames.PUB_GRUU);
        }
    }

    public String getPubGruuParam() {
        return (String) this.parameters.getValue(ParameterNames.PUB_GRUU);
    }

    public void setPubGruuParam(String value) {
        this.parameters.set(ParameterNames.PUB_GRUU, value);
    }

    public void removeTempGruuParam() {
        if (this.parameters != null) {
            this.parameters.delete(ParameterNames.TEMP_GRUU);
        }
    }

    public String getTempGruuParam() {
        return (String) this.parameters.getValue(ParameterNames.TEMP_GRUU);
    }

    public void setTempGruuParam(String value) {
        this.parameters.set(ParameterNames.TEMP_GRUU, value);
    }
}

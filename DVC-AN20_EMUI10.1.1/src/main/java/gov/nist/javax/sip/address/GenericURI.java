package gov.nist.javax.sip.address;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.address.URI;

public class GenericURI extends NetObject implements URI {
    public static final String ISUB = "isub";
    public static final String PHONE_CONTEXT_TAG = "context-tag";
    public static final String POSTDIAL = "postdial";
    public static final String PROVIDER_TAG = "provider-tag";
    public static final String SIP = "sip";
    public static final String SIPS = "sips";
    public static final String TEL = "tel";
    private static final long serialVersionUID = 3237685256878068790L;
    protected String scheme;
    protected String uriString;

    protected GenericURI() {
    }

    public GenericURI(String uriString2) throws ParseException {
        try {
            this.uriString = uriString2;
            this.scheme = uriString2.substring(0, uriString2.indexOf(Separators.COLON));
        } catch (Exception e) {
            throw new ParseException("GenericURI, Bad URI format", 0);
        }
    }

    @Override // gov.nist.core.GenericObject
    public String encode() {
        return this.uriString;
    }

    @Override // gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(this.uriString);
        return buffer;
    }

    @Override // gov.nist.javax.sip.address.NetObject, javax.sip.address.URI
    public String toString() {
        return encode();
    }

    @Override // javax.sip.address.URI
    public String getScheme() {
        return this.scheme;
    }

    @Override // javax.sip.address.URI
    public boolean isSipURI() {
        return this instanceof SipUri;
    }

    @Override // gov.nist.javax.sip.address.NetObject, gov.nist.core.GenericObject
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof URI) {
            return toString().equalsIgnoreCase(((URI) that).toString());
        }
        return false;
    }

    public int hashCode() {
        return toString().hashCode();
    }
}

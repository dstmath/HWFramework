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

    public GenericURI(String uriString) throws ParseException {
        try {
            this.uriString = uriString;
            this.scheme = uriString.substring(0, uriString.indexOf(Separators.COLON));
        } catch (Exception e) {
            throw new ParseException("GenericURI, Bad URI format", 0);
        }
    }

    public String encode() {
        return this.uriString;
    }

    public StringBuffer encode(StringBuffer buffer) {
        return buffer.append(this.uriString);
    }

    public String toString() {
        return encode();
    }

    public String getScheme() {
        return this.scheme;
    }

    public boolean isSipURI() {
        return this instanceof SipUri;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof URI)) {
            return false;
        }
        return toString().equalsIgnoreCase(((URI) that).toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }
}

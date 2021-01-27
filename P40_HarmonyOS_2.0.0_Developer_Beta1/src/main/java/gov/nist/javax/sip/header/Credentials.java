package gov.nist.javax.sip.header;

import gov.nist.core.NameValueList;
import gov.nist.core.Separators;

public class Credentials extends SIPObject {
    private static String CNONCE = "cnonce";
    private static String DOMAIN = "domain";
    private static String NONCE = "nonce";
    private static String OPAQUE = "opaque";
    private static String REALM = "realm";
    private static String RESPONSE = "response";
    private static String URI = "uri";
    private static String USERNAME = "username";
    private static final long serialVersionUID = -6335592791505451524L;
    protected NameValueList parameters = new NameValueList();
    protected String scheme;

    public Credentials() {
        this.parameters.setSeparator(Separators.COMMA);
    }

    public NameValueList getCredentials() {
        return this.parameters;
    }

    public String getScheme() {
        return this.scheme;
    }

    public void setScheme(String s) {
        this.scheme = s;
    }

    public void setCredentials(NameValueList c) {
        this.parameters = c;
    }

    @Override // gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        String retval = this.scheme;
        if (this.parameters.isEmpty()) {
            return retval;
        }
        return retval + Separators.SP + this.parameters.encode();
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        Credentials retval = (Credentials) super.clone();
        NameValueList nameValueList = this.parameters;
        if (nameValueList != null) {
            retval.parameters = (NameValueList) nameValueList.clone();
        }
        return retval;
    }
}

package gov.nist.javax.sip.header;

import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;

public class Challenge extends SIPObject {
    private static String ALGORITHM = "algorithm";
    private static String DOMAIN = "domain";
    private static String OPAQUE = "opaque";
    private static String QOP = "qop";
    private static String REALM = "realm";
    private static String RESPONSE = "response";
    private static String SIGNATURE = "signature";
    private static String SIGNED_BY = "signed-by";
    private static String STALE = "stale";
    private static String URI = "uri";
    private static final long serialVersionUID = 5944455875924336L;
    protected NameValueList authParams = new NameValueList();
    protected String scheme;

    public Challenge() {
        this.authParams.setSeparator(Separators.COMMA);
    }

    public String encode() {
        return new StringBuffer(this.scheme).append(Separators.SP).append(this.authParams.encode()).toString();
    }

    public String getScheme() {
        return this.scheme;
    }

    public NameValueList getAuthParams() {
        return this.authParams;
    }

    public String getDomain() {
        return (String) this.authParams.getValue(DOMAIN);
    }

    public String getURI() {
        return (String) this.authParams.getValue(URI);
    }

    public String getOpaque() {
        return (String) this.authParams.getValue(OPAQUE);
    }

    public String getQOP() {
        return (String) this.authParams.getValue(QOP);
    }

    public String getAlgorithm() {
        return (String) this.authParams.getValue(ALGORITHM);
    }

    public String getStale() {
        return (String) this.authParams.getValue(STALE);
    }

    public String getSignature() {
        return (String) this.authParams.getValue(SIGNATURE);
    }

    public String getSignedBy() {
        return (String) this.authParams.getValue(SIGNED_BY);
    }

    public String getResponse() {
        return (String) this.authParams.getValue(RESPONSE);
    }

    public String getRealm() {
        return (String) this.authParams.getValue(REALM);
    }

    public String getParameter(String name) {
        return (String) this.authParams.getValue(name);
    }

    public boolean hasParameter(String name) {
        return this.authParams.getNameValue(name) != null;
    }

    public boolean hasParameters() {
        return this.authParams.size() != 0;
    }

    public boolean removeParameter(String name) {
        return this.authParams.delete(name);
    }

    public void removeParameters() {
        this.authParams = new NameValueList();
    }

    public void setParameter(NameValue nv) {
        this.authParams.set(nv);
    }

    public void setScheme(String s) {
        this.scheme = s;
    }

    public void setAuthParams(NameValueList a) {
        this.authParams = a;
    }

    public Object clone() {
        Challenge retval = (Challenge) super.clone();
        if (this.authParams != null) {
            retval.authParams = (NameValueList) this.authParams.clone();
        }
        return retval;
    }
}

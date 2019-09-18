package gov.nist.javax.sip.header;

public class WWWAuthenticateList extends SIPHeaderList<WWWAuthenticate> {
    private static final long serialVersionUID = -6978902284285501346L;

    public Object clone() {
        return new WWWAuthenticateList().clonehlist(this.hlist);
    }

    public WWWAuthenticateList() {
        super(WWWAuthenticate.class, "WWW-Authenticate");
    }
}

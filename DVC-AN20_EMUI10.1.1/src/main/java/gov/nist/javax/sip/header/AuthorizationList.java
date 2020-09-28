package gov.nist.javax.sip.header;

public class AuthorizationList extends SIPHeaderList<Authorization> {
    private static final long serialVersionUID = 1;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        AuthorizationList retval = new AuthorizationList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AuthorizationList() {
        super(Authorization.class, "Authorization");
    }
}

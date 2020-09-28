package gov.nist.javax.sip.header;

public class AuthenticationInfoList extends SIPHeaderList<AuthenticationInfo> {
    private static final long serialVersionUID = 1;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        AuthenticationInfoList retval = new AuthenticationInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AuthenticationInfoList() {
        super(AuthenticationInfo.class, "Authentication-Info");
    }
}

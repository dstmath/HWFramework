package gov.nist.javax.sip.header;

public class AuthenticationInfoList extends SIPHeaderList<AuthenticationInfo> {
    private static final long serialVersionUID = 1;

    public Object clone() {
        AuthenticationInfoList retval = new AuthenticationInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AuthenticationInfoList() {
        super(AuthenticationInfo.class, "Authentication-Info");
    }
}

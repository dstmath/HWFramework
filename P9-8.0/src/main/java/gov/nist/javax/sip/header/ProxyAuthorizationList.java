package gov.nist.javax.sip.header;

public class ProxyAuthorizationList extends SIPHeaderList<ProxyAuthorization> {
    private static final long serialVersionUID = -1;

    public Object clone() {
        ProxyAuthorizationList retval = new ProxyAuthorizationList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ProxyAuthorizationList() {
        super(ProxyAuthorization.class, "Proxy-Authorization");
    }
}

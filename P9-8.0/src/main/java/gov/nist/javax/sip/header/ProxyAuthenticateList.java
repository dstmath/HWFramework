package gov.nist.javax.sip.header;

public class ProxyAuthenticateList extends SIPHeaderList<ProxyAuthenticate> {
    private static final long serialVersionUID = 1;

    public Object clone() {
        ProxyAuthenticateList retval = new ProxyAuthenticateList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ProxyAuthenticateList() {
        super(ProxyAuthenticate.class, "Proxy-Authenticate");
    }
}

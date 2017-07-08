package gov.nist.javax.sip.header;

import javax.sip.header.ProxyRequireHeader;

public class ProxyRequireList extends SIPHeaderList<ProxyRequire> {
    private static final long serialVersionUID = 5648630649476486042L;

    public Object clone() {
        ProxyRequireList retval = new ProxyRequireList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ProxyRequireList() {
        super(ProxyRequire.class, ProxyRequireHeader.NAME);
    }
}

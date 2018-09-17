package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class SecurityServerList extends SIPHeaderList<SecurityServer> {
    private static final long serialVersionUID = -1392066520803180238L;

    public SecurityServerList() {
        super(SecurityServer.class, "Security-Server");
    }

    public Object clone() {
        return new SecurityServerList().clonehlist(this.hlist);
    }
}

package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class SecurityClientList extends SIPHeaderList<SecurityClient> {
    private static final long serialVersionUID = 3094231003329176217L;

    public SecurityClientList() {
        super(SecurityClient.class, SecurityClientHeader.NAME);
    }

    public Object clone() {
        return new SecurityClientList().clonehlist(this.hlist);
    }
}

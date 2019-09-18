package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PMediaAuthorizationList extends SIPHeaderList<PMediaAuthorization> {
    private static final long serialVersionUID = -8226328073989632317L;

    public PMediaAuthorizationList() {
        super(PMediaAuthorization.class, "P-Media-Authorization");
    }

    public Object clone() {
        return new PMediaAuthorizationList().clonehlist(this.hlist);
    }
}

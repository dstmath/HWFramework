package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PVisitedNetworkIDList extends SIPHeaderList<PVisitedNetworkID> {
    private static final long serialVersionUID = -4346667490341752478L;

    public PVisitedNetworkIDList() {
        super(PVisitedNetworkID.class, "P-Visited-Network-ID");
    }

    public Object clone() {
        return new PVisitedNetworkIDList().clonehlist(this.hlist);
    }
}

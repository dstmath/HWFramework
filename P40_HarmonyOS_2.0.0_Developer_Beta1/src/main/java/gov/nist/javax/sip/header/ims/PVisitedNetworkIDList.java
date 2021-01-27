package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PVisitedNetworkIDList extends SIPHeaderList<PVisitedNetworkID> {
    private static final long serialVersionUID = -4346667490341752478L;

    public PVisitedNetworkIDList() {
        super(PVisitedNetworkID.class, "P-Visited-Network-ID");
    }

    @Override // gov.nist.javax.sip.header.SIPHeaderList, gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        return new PVisitedNetworkIDList().clonehlist(this.hlist);
    }
}

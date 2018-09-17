package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PAssociatedURIList extends SIPHeaderList<PAssociatedURI> {
    private static final long serialVersionUID = 4454306052557362851L;

    public PAssociatedURIList() {
        super(PAssociatedURI.class, "P-Associated-URI");
    }

    public Object clone() {
        return new PAssociatedURIList().clonehlist(this.hlist);
    }
}

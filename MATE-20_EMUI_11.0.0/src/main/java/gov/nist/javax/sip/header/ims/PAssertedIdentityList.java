package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PAssertedIdentityList extends SIPHeaderList<PAssertedIdentity> {
    private static final long serialVersionUID = -6465152445570308974L;

    public PAssertedIdentityList() {
        super(PAssertedIdentity.class, "P-Asserted-Identity");
    }

    @Override // gov.nist.javax.sip.header.SIPHeaderList, gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        return new PAssertedIdentityList().clonehlist(this.hlist);
    }
}

package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class SecurityVerifyList extends SIPHeaderList<SecurityVerify> {
    private static final long serialVersionUID = 563201040577795125L;

    public SecurityVerifyList() {
        super(SecurityVerify.class, "Security-Verify");
    }

    public Object clone() {
        return new SecurityVerifyList().clonehlist(this.hlist);
    }
}

package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.SIPHeaderList;

public class PrivacyList extends SIPHeaderList<Privacy> {
    private static final long serialVersionUID = 1798720509806307461L;

    public PrivacyList() {
        super(Privacy.class, "Privacy");
    }

    public Object clone() {
        return new PrivacyList().clonehlist(this.hlist);
    }
}

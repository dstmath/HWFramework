package gov.nist.javax.sip.header;

public final class ReasonList extends SIPHeaderList<Reason> {
    private static final long serialVersionUID = 7459989997463160670L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        ReasonList retval = new ReasonList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ReasonList() {
        super(Reason.class, "Reason");
    }
}

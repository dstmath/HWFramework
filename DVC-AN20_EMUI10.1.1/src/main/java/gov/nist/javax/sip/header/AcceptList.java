package gov.nist.javax.sip.header;

public class AcceptList extends SIPHeaderList<Accept> {
    private static final long serialVersionUID = -1800813338560484831L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        AcceptList retval = new AcceptList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AcceptList() {
        super(Accept.class, "Accept");
    }
}

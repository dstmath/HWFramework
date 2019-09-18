package gov.nist.javax.sip.header;

public final class ViaList extends SIPHeaderList<Via> {
    private static final long serialVersionUID = 3899679374556152313L;

    public Object clone() {
        return new ViaList().clonehlist(this.hlist);
    }

    public ViaList() {
        super(Via.class, "Via");
    }
}

package gov.nist.javax.sip.header;

public class SupportedList extends SIPHeaderList<Supported> {
    private static final long serialVersionUID = -4539299544895602367L;

    public Object clone() {
        SupportedList retval = new SupportedList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public SupportedList() {
        super(Supported.class, "Supported");
    }
}

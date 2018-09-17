package gov.nist.javax.sip.header;

public class WarningList extends SIPHeaderList<Warning> {
    private static final long serialVersionUID = -1423278728898430175L;

    public Object clone() {
        return new WarningList().clonehlist(this.hlist);
    }

    public WarningList() {
        super(Warning.class, "Warning");
    }
}

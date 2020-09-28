package gov.nist.javax.sip.header;

public class WarningList extends SIPHeaderList<Warning> {
    private static final long serialVersionUID = -1423278728898430175L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        return new WarningList().clonehlist(this.hlist);
    }

    public WarningList() {
        super(Warning.class, "Warning");
    }
}

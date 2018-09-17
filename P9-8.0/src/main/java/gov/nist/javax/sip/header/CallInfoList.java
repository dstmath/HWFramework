package gov.nist.javax.sip.header;

public class CallInfoList extends SIPHeaderList<CallInfo> {
    private static final long serialVersionUID = -4949850334388806423L;

    public Object clone() {
        CallInfoList retval = new CallInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public CallInfoList() {
        super(CallInfo.class, "Call-Info");
    }
}

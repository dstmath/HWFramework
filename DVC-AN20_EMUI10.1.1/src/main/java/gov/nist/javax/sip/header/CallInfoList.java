package gov.nist.javax.sip.header;

public class CallInfoList extends SIPHeaderList<CallInfo> {
    private static final long serialVersionUID = -4949850334388806423L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        CallInfoList retval = new CallInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public CallInfoList() {
        super(CallInfo.class, "Call-Info");
    }
}

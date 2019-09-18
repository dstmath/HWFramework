package gov.nist.javax.sip.header;

public class ErrorInfoList extends SIPHeaderList<ErrorInfo> {
    private static final long serialVersionUID = 1;

    public Object clone() {
        ErrorInfoList retval = new ErrorInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ErrorInfoList() {
        super(ErrorInfo.class, "Error-Info");
    }
}

package gov.nist.javax.sip.header;

public class AlertInfoList extends SIPHeaderList<AlertInfo> {
    private static final long serialVersionUID = 1;

    public Object clone() {
        AlertInfoList retval = new AlertInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AlertInfoList() {
        super(AlertInfo.class, "Alert-Info");
    }
}

package gov.nist.javax.sip.header;

import javax.sip.header.AlertInfoHeader;

public class AlertInfoList extends SIPHeaderList<AlertInfo> {
    private static final long serialVersionUID = 1;

    public Object clone() {
        AlertInfoList retval = new AlertInfoList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AlertInfoList() {
        super(AlertInfo.class, AlertInfoHeader.NAME);
    }
}

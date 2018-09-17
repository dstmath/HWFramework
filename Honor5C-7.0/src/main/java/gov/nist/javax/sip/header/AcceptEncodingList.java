package gov.nist.javax.sip.header;

import javax.sip.header.AcceptEncodingHeader;

public class AcceptEncodingList extends SIPHeaderList<AcceptEncoding> {
    public Object clone() {
        AcceptEncodingList retval = new AcceptEncodingList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AcceptEncodingList() {
        super(AcceptEncoding.class, AcceptEncodingHeader.NAME);
    }
}

package gov.nist.javax.sip.header;

public class AcceptEncodingList extends SIPHeaderList<AcceptEncoding> {
    public Object clone() {
        AcceptEncodingList retval = new AcceptEncodingList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AcceptEncodingList() {
        super(AcceptEncoding.class, "Accept-Encoding");
    }
}

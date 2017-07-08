package gov.nist.javax.sip.header;

import javax.sip.header.UnsupportedHeader;

public class UnsupportedList extends SIPHeaderList<Unsupported> {
    private static final long serialVersionUID = -4052610269407058661L;

    public UnsupportedList() {
        super(Unsupported.class, UnsupportedHeader.NAME);
    }

    public Object clone() {
        return new UnsupportedList().clonehlist(this.hlist);
    }
}

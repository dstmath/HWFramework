package gov.nist.javax.sip.header;

public final class ContentEncodingList extends SIPHeaderList<ContentEncoding> {
    private static final long serialVersionUID = 7365216146576273970L;

    public Object clone() {
        ContentEncodingList retval = new ContentEncodingList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ContentEncodingList() {
        super(ContentEncoding.class, "Content-Encoding");
    }
}

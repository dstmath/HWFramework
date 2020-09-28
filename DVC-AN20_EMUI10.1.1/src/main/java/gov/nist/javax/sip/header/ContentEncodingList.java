package gov.nist.javax.sip.header;

public final class ContentEncodingList extends SIPHeaderList<ContentEncoding> {
    private static final long serialVersionUID = 7365216146576273970L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        ContentEncodingList retval = new ContentEncodingList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ContentEncodingList() {
        super(ContentEncoding.class, "Content-Encoding");
    }
}

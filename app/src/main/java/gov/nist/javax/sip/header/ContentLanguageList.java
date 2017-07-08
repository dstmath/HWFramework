package gov.nist.javax.sip.header;

import javax.sip.header.ContentLanguageHeader;

public final class ContentLanguageList extends SIPHeaderList<ContentLanguage> {
    private static final long serialVersionUID = -5302265987802886465L;

    public Object clone() {
        ContentLanguageList retval = new ContentLanguageList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ContentLanguageList() {
        super(ContentLanguage.class, ContentLanguageHeader.NAME);
    }
}

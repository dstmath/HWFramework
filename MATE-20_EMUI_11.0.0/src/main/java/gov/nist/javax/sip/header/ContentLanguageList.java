package gov.nist.javax.sip.header;

public final class ContentLanguageList extends SIPHeaderList<ContentLanguage> {
    private static final long serialVersionUID = -5302265987802886465L;

    @Override // gov.nist.javax.sip.header.SIPHeaderList, gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        ContentLanguageList retval = new ContentLanguageList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ContentLanguageList() {
        super(ContentLanguage.class, "Content-Language");
    }
}

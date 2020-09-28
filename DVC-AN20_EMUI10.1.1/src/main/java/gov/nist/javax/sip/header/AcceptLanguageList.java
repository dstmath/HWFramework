package gov.nist.javax.sip.header;

public class AcceptLanguageList extends SIPHeaderList<AcceptLanguage> {
    private static final long serialVersionUID = -3289606805203488840L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        AcceptLanguageList retval = new AcceptLanguageList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AcceptLanguageList() {
        super(AcceptLanguage.class, "Accept-Language");
    }

    @Override // gov.nist.javax.sip.header.SIPHeaderList
    public AcceptLanguage getFirst() {
        AcceptLanguage retval = (AcceptLanguage) super.getFirst();
        if (retval != null) {
            return retval;
        }
        return new AcceptLanguage();
    }

    @Override // gov.nist.javax.sip.header.SIPHeaderList
    public AcceptLanguage getLast() {
        AcceptLanguage retval = (AcceptLanguage) super.getLast();
        if (retval != null) {
            return retval;
        }
        return new AcceptLanguage();
    }
}

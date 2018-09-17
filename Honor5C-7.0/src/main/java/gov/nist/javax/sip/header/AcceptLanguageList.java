package gov.nist.javax.sip.header;

import javax.sip.header.AcceptLanguageHeader;

public class AcceptLanguageList extends SIPHeaderList<AcceptLanguage> {
    private static final long serialVersionUID = -3289606805203488840L;

    public Object clone() {
        AcceptLanguageList retval = new AcceptLanguageList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AcceptLanguageList() {
        super(AcceptLanguage.class, AcceptLanguageHeader.NAME);
    }

    public AcceptLanguage getFirst() {
        AcceptLanguage retval = (AcceptLanguage) super.getFirst();
        if (retval != null) {
            return retval;
        }
        return new AcceptLanguage();
    }

    public AcceptLanguage getLast() {
        AcceptLanguage retval = (AcceptLanguage) super.getLast();
        if (retval != null) {
            return retval;
        }
        return new AcceptLanguage();
    }
}

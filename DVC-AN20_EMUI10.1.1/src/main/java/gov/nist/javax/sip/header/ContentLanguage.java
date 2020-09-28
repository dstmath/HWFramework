package gov.nist.javax.sip.header;

import java.util.Locale;
import javax.sip.header.ContentLanguageHeader;

public class ContentLanguage extends SIPHeader implements ContentLanguageHeader {
    private static final long serialVersionUID = -5195728427134181070L;
    protected Locale locale;

    public ContentLanguage() {
        super("Content-Language");
    }

    public ContentLanguage(String languageTag) {
        super("Content-Language");
        setLanguageTag(languageTag);
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return getLanguageTag();
    }

    @Override // javax.sip.header.ContentLanguageHeader
    public String getLanguageTag() {
        if ("".equals(this.locale.getCountry())) {
            return this.locale.getLanguage();
        }
        return this.locale.getLanguage() + '-' + this.locale.getCountry();
    }

    @Override // javax.sip.header.ContentLanguageHeader
    public void setLanguageTag(String languageTag) {
        int slash = languageTag.indexOf(45);
        if (slash >= 0) {
            this.locale = new Locale(languageTag.substring(0, slash), languageTag.substring(slash + 1));
        } else {
            this.locale = new Locale(languageTag);
        }
    }

    @Override // javax.sip.header.ContentLanguageHeader
    public Locale getContentLanguage() {
        return this.locale;
    }

    @Override // javax.sip.header.ContentLanguageHeader
    public void setContentLanguage(Locale language) {
        this.locale = language;
    }

    @Override // java.lang.Object, javax.sip.header.Header, gov.nist.core.GenericObject
    public Object clone() {
        ContentLanguage retval = (ContentLanguage) super.clone();
        Locale locale2 = this.locale;
        if (locale2 != null) {
            retval.locale = (Locale) locale2.clone();
        }
        return retval;
    }
}

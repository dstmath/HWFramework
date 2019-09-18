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

    public String encodeBody() {
        return getLanguageTag();
    }

    public String getLanguageTag() {
        if ("".equals(this.locale.getCountry())) {
            return this.locale.getLanguage();
        }
        return this.locale.getLanguage() + '-' + this.locale.getCountry();
    }

    public void setLanguageTag(String languageTag) {
        int slash = languageTag.indexOf(45);
        if (slash >= 0) {
            this.locale = new Locale(languageTag.substring(0, slash), languageTag.substring(slash + 1));
        } else {
            this.locale = new Locale(languageTag);
        }
    }

    public Locale getContentLanguage() {
        return this.locale;
    }

    public void setContentLanguage(Locale language) {
        this.locale = language;
    }

    public Object clone() {
        ContentLanguage retval = (ContentLanguage) super.clone();
        if (this.locale != null) {
            retval.locale = (Locale) this.locale.clone();
        }
        return retval;
    }
}

package javax.sip.header;

import java.util.Locale;

public interface ContentLanguageHeader extends Header {
    public static final String NAME = "Content-Language";

    Locale getContentLanguage();

    String getLanguageTag();

    void setContentLanguage(Locale locale);

    void setLanguageTag(String str);
}

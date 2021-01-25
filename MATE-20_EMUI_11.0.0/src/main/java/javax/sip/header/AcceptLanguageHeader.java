package javax.sip.header;

import java.util.Locale;
import javax.sip.InvalidArgumentException;

public interface AcceptLanguageHeader extends Header, Parameters {
    public static final String NAME = "Accept-Language";

    Locale getAcceptLanguage();

    float getQValue();

    boolean hasQValue();

    void removeQValue();

    void setAcceptLanguage(Locale locale);

    void setLanguageRange(String str);

    void setQValue(float f) throws InvalidArgumentException;
}

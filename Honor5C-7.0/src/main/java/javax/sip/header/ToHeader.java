package javax.sip.header;

import java.text.ParseException;

public interface ToHeader extends HeaderAddress, Header, Parameters {
    public static final String NAME = "To";

    String getDisplayName();

    String getTag();

    String getUserAtHostPort();

    boolean hasTag();

    void removeTag();

    void setTag(String str) throws ParseException;
}

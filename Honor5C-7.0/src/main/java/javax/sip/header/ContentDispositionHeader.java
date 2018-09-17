package javax.sip.header;

import java.text.ParseException;

public interface ContentDispositionHeader extends Header, Parameters {
    public static final String ALERT = "Alert";
    public static final String ICON = "Icon";
    public static final String NAME = "Content-Disposition";
    public static final String RENDER = "Render";
    public static final String SESSION = "Session";

    String getDispositionType();

    String getHandling();

    void setDispositionType(String str) throws ParseException;

    void setHandling(String str) throws ParseException;
}

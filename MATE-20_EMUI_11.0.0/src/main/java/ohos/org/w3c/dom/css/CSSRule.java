package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.DOMException;

public interface CSSRule {
    public static final short CHARSET_RULE = 2;
    public static final short FONT_FACE_RULE = 5;
    public static final short IMPORT_RULE = 3;
    public static final short MEDIA_RULE = 4;
    public static final short PAGE_RULE = 6;
    public static final short STYLE_RULE = 1;
    public static final short UNKNOWN_RULE = 0;

    String getCssText();

    CSSRule getParentRule();

    CSSStyleSheet getParentStyleSheet();

    short getType();

    void setCssText(String str) throws DOMException;
}

package ohos.org.w3c.dom.html;

public interface HTMLAnchorElement extends HTMLElement {
    void blur();

    void focus();

    String getAccessKey();

    String getCharset();

    String getCoords();

    String getHref();

    String getHreflang();

    String getName();

    String getRel();

    String getRev();

    String getShape();

    int getTabIndex();

    String getTarget();

    String getType();

    void setAccessKey(String str);

    void setCharset(String str);

    void setCoords(String str);

    void setHref(String str);

    void setHreflang(String str);

    void setName(String str);

    void setRel(String str);

    void setRev(String str);

    void setShape(String str);

    void setTabIndex(int i);

    void setTarget(String str);

    void setType(String str);
}

package ohos.org.w3c.dom.html;

public interface HTMLAreaElement extends HTMLElement {
    String getAccessKey();

    String getAlt();

    String getCoords();

    String getHref();

    boolean getNoHref();

    String getShape();

    int getTabIndex();

    String getTarget();

    void setAccessKey(String str);

    void setAlt(String str);

    void setCoords(String str);

    void setHref(String str);

    void setNoHref(boolean z);

    void setShape(String str);

    void setTabIndex(int i);

    void setTarget(String str);
}

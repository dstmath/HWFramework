package ohos.org.w3c.dom.html;

public interface HTMLLinkElement extends HTMLElement {
    String getCharset();

    boolean getDisabled();

    String getHref();

    String getHreflang();

    String getMedia();

    String getRel();

    String getRev();

    String getTarget();

    String getType();

    void setCharset(String str);

    void setDisabled(boolean z);

    void setHref(String str);

    void setHreflang(String str);

    void setMedia(String str);

    void setRel(String str);

    void setRev(String str);

    void setTarget(String str);

    void setType(String str);
}

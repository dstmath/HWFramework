package ohos.org.w3c.dom.html;

public interface HTMLScriptElement extends HTMLElement {
    String getCharset();

    boolean getDefer();

    String getEvent();

    String getHtmlFor();

    String getSrc();

    String getText();

    String getType();

    void setCharset(String str);

    void setDefer(boolean z);

    void setEvent(String str);

    void setHtmlFor(String str);

    void setSrc(String str);

    void setText(String str);

    void setType(String str);
}

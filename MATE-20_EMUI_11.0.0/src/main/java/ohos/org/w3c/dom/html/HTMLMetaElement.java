package ohos.org.w3c.dom.html;

public interface HTMLMetaElement extends HTMLElement {
    String getContent();

    String getHttpEquiv();

    String getName();

    String getScheme();

    void setContent(String str);

    void setHttpEquiv(String str);

    void setName(String str);

    void setScheme(String str);
}

package ohos.org.w3c.dom.html;

public interface HTMLLabelElement extends HTMLElement {
    String getAccessKey();

    HTMLFormElement getForm();

    String getHtmlFor();

    void setAccessKey(String str);

    void setHtmlFor(String str);
}

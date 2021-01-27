package ohos.org.w3c.dom.html;

public interface HTMLLIElement extends HTMLElement {
    String getType();

    int getValue();

    void setType(String str);

    void setValue(int i);
}

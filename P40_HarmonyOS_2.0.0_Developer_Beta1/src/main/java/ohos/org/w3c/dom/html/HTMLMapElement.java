package ohos.org.w3c.dom.html;

public interface HTMLMapElement extends HTMLElement {
    HTMLCollection getAreas();

    String getName();

    void setName(String str);
}

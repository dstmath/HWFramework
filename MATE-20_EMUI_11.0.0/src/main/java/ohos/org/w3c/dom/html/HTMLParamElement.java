package ohos.org.w3c.dom.html;

public interface HTMLParamElement extends HTMLElement {
    String getName();

    String getType();

    String getValue();

    String getValueType();

    void setName(String str);

    void setType(String str);

    void setValue(String str);

    void setValueType(String str);
}

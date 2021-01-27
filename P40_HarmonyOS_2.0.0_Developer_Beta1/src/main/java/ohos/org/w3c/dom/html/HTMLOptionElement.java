package ohos.org.w3c.dom.html;

public interface HTMLOptionElement extends HTMLElement {
    boolean getDefaultSelected();

    boolean getDisabled();

    HTMLFormElement getForm();

    int getIndex();

    String getLabel();

    boolean getSelected();

    String getText();

    String getValue();

    void setDefaultSelected(boolean z);

    void setDisabled(boolean z);

    void setLabel(String str);

    void setSelected(boolean z);

    void setValue(String str);
}

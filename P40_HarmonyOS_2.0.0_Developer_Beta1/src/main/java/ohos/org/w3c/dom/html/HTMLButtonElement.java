package ohos.org.w3c.dom.html;

public interface HTMLButtonElement extends HTMLElement {
    String getAccessKey();

    boolean getDisabled();

    HTMLFormElement getForm();

    String getName();

    int getTabIndex();

    String getType();

    String getValue();

    void setAccessKey(String str);

    void setDisabled(boolean z);

    void setName(String str);

    void setTabIndex(int i);

    void setValue(String str);
}

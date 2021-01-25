package ohos.org.w3c.dom.html;

public interface HTMLTextAreaElement extends HTMLElement {
    void blur();

    void focus();

    String getAccessKey();

    int getCols();

    String getDefaultValue();

    boolean getDisabled();

    HTMLFormElement getForm();

    String getName();

    boolean getReadOnly();

    int getRows();

    int getTabIndex();

    String getType();

    String getValue();

    void select();

    void setAccessKey(String str);

    void setCols(int i);

    void setDefaultValue(String str);

    void setDisabled(boolean z);

    void setName(String str);

    void setReadOnly(boolean z);

    void setRows(int i);

    void setTabIndex(int i);

    void setValue(String str);
}

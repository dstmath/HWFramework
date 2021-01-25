package ohos.org.w3c.dom.html;

public interface HTMLInputElement extends HTMLElement {
    void blur();

    void click();

    void focus();

    String getAccept();

    String getAccessKey();

    String getAlign();

    String getAlt();

    boolean getChecked();

    boolean getDefaultChecked();

    String getDefaultValue();

    boolean getDisabled();

    HTMLFormElement getForm();

    int getMaxLength();

    String getName();

    boolean getReadOnly();

    String getSize();

    String getSrc();

    int getTabIndex();

    String getType();

    String getUseMap();

    String getValue();

    void select();

    void setAccept(String str);

    void setAccessKey(String str);

    void setAlign(String str);

    void setAlt(String str);

    void setChecked(boolean z);

    void setDefaultChecked(boolean z);

    void setDefaultValue(String str);

    void setDisabled(boolean z);

    void setMaxLength(int i);

    void setName(String str);

    void setReadOnly(boolean z);

    void setSize(String str);

    void setSrc(String str);

    void setTabIndex(int i);

    void setUseMap(String str);

    void setValue(String str);
}

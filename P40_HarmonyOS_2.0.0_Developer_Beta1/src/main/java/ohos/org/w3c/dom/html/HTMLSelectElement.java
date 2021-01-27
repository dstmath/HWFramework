package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.DOMException;

public interface HTMLSelectElement extends HTMLElement {
    void add(HTMLElement hTMLElement, HTMLElement hTMLElement2) throws DOMException;

    void blur();

    void focus();

    boolean getDisabled();

    HTMLFormElement getForm();

    int getLength();

    boolean getMultiple();

    String getName();

    HTMLCollection getOptions();

    int getSelectedIndex();

    int getSize();

    int getTabIndex();

    String getType();

    String getValue();

    void remove(int i);

    void setDisabled(boolean z);

    void setMultiple(boolean z);

    void setName(String str);

    void setSelectedIndex(int i);

    void setSize(int i);

    void setTabIndex(int i);

    void setValue(String str);
}

package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.DOMException;

public interface HTMLTableSectionElement extends HTMLElement {
    void deleteRow(int i) throws DOMException;

    String getAlign();

    String getCh();

    String getChOff();

    HTMLCollection getRows();

    String getVAlign();

    HTMLElement insertRow(int i) throws DOMException;

    void setAlign(String str);

    void setCh(String str);

    void setChOff(String str);

    void setVAlign(String str);
}

package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.DOMException;

public interface HTMLTableRowElement extends HTMLElement {
    void deleteCell(int i) throws DOMException;

    String getAlign();

    String getBgColor();

    HTMLCollection getCells();

    String getCh();

    String getChOff();

    int getRowIndex();

    int getSectionRowIndex();

    String getVAlign();

    HTMLElement insertCell(int i) throws DOMException;

    void setAlign(String str);

    void setBgColor(String str);

    void setCh(String str);

    void setChOff(String str);

    void setVAlign(String str);
}

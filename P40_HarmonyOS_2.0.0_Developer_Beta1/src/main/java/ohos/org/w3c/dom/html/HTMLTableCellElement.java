package ohos.org.w3c.dom.html;

public interface HTMLTableCellElement extends HTMLElement {
    String getAbbr();

    String getAlign();

    String getAxis();

    String getBgColor();

    int getCellIndex();

    String getCh();

    String getChOff();

    int getColSpan();

    String getHeaders();

    String getHeight();

    boolean getNoWrap();

    int getRowSpan();

    String getScope();

    String getVAlign();

    String getWidth();

    void setAbbr(String str);

    void setAlign(String str);

    void setAxis(String str);

    void setBgColor(String str);

    void setCh(String str);

    void setChOff(String str);

    void setColSpan(int i);

    void setHeaders(String str);

    void setHeight(String str);

    void setNoWrap(boolean z);

    void setRowSpan(int i);

    void setScope(String str);

    void setVAlign(String str);

    void setWidth(String str);
}

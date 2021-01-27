package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.Document;

public interface HTMLIFrameElement extends HTMLElement {
    String getAlign();

    Document getContentDocument();

    String getFrameBorder();

    String getHeight();

    String getLongDesc();

    String getMarginHeight();

    String getMarginWidth();

    String getName();

    String getScrolling();

    String getSrc();

    String getWidth();

    void setAlign(String str);

    void setFrameBorder(String str);

    void setHeight(String str);

    void setLongDesc(String str);

    void setMarginHeight(String str);

    void setMarginWidth(String str);

    void setName(String str);

    void setScrolling(String str);

    void setSrc(String str);

    void setWidth(String str);
}

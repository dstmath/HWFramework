package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.Document;

public interface HTMLFrameElement extends HTMLElement {
    Document getContentDocument();

    String getFrameBorder();

    String getLongDesc();

    String getMarginHeight();

    String getMarginWidth();

    String getName();

    boolean getNoResize();

    String getScrolling();

    String getSrc();

    void setFrameBorder(String str);

    void setLongDesc(String str);

    void setMarginHeight(String str);

    void setMarginWidth(String str);

    void setName(String str);

    void setNoResize(boolean z);

    void setScrolling(String str);

    void setSrc(String str);
}

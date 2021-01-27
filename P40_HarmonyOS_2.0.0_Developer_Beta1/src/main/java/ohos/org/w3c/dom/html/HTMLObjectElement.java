package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.Document;

public interface HTMLObjectElement extends HTMLElement {
    String getAlign();

    String getArchive();

    String getBorder();

    String getCode();

    String getCodeBase();

    String getCodeType();

    Document getContentDocument();

    String getData();

    boolean getDeclare();

    HTMLFormElement getForm();

    String getHeight();

    String getHspace();

    String getName();

    String getStandby();

    int getTabIndex();

    String getType();

    String getUseMap();

    String getVspace();

    String getWidth();

    void setAlign(String str);

    void setArchive(String str);

    void setBorder(String str);

    void setCode(String str);

    void setCodeBase(String str);

    void setCodeType(String str);

    void setData(String str);

    void setDeclare(boolean z);

    void setHeight(String str);

    void setHspace(String str);

    void setName(String str);

    void setStandby(String str);

    void setTabIndex(int i);

    void setType(String str);

    void setUseMap(String str);

    void setVspace(String str);

    void setWidth(String str);
}

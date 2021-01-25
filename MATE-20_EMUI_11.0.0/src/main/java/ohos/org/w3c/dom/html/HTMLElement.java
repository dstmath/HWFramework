package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.Element;

public interface HTMLElement extends Element {
    String getClassName();

    String getDir();

    String getId();

    String getLang();

    String getTitle();

    void setClassName(String str);

    void setDir(String str);

    void setId(String str);

    void setLang(String str);

    void setTitle(String str);
}

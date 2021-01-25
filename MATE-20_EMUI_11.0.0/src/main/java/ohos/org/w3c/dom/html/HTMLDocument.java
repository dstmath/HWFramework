package ohos.org.w3c.dom.html;

import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.NodeList;

public interface HTMLDocument extends Document {
    void close();

    HTMLCollection getAnchors();

    HTMLCollection getApplets();

    HTMLElement getBody();

    String getCookie();

    String getDomain();

    NodeList getElementsByName(String str);

    HTMLCollection getForms();

    HTMLCollection getImages();

    HTMLCollection getLinks();

    String getReferrer();

    String getTitle();

    String getURL();

    void open();

    void setBody(HTMLElement hTMLElement);

    void setCookie(String str);

    void setTitle(String str);

    void write(String str);

    void writeln(String str);
}

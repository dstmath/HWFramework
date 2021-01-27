package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.DOMException;

public interface CSSCharsetRule extends CSSRule {
    String getEncoding();

    void setEncoding(String str) throws DOMException;
}

package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.DOMException;

public interface CSSStyleRule extends CSSRule {
    String getSelectorText();

    CSSStyleDeclaration getStyle();

    void setSelectorText(String str) throws DOMException;
}

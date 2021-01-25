package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;

public interface DOMImplementationCSS extends DOMImplementation {
    CSSStyleSheet createCSSStyleSheet(String str, String str2) throws DOMException;
}

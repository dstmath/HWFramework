package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.views.AbstractView;

public interface ViewCSS extends AbstractView {
    CSSStyleDeclaration getComputedStyle(Element element, String str);
}

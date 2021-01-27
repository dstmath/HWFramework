package ohos.org.w3c.dom.css;

import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.stylesheets.DocumentStyle;

public interface DocumentCSS extends DocumentStyle {
    CSSStyleDeclaration getOverrideStyle(Element element, String str);
}

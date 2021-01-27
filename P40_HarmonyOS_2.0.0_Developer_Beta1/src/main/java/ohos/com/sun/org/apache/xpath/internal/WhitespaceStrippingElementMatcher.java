package ohos.com.sun.org.apache.xpath.internal;

import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.Element;

public interface WhitespaceStrippingElementMatcher {
    boolean canStripWhiteSpace();

    boolean shouldStripWhiteSpace(XPathContext xPathContext, Element element) throws TransformerException;
}

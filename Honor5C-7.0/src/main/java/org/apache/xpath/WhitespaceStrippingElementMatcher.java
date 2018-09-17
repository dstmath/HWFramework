package org.apache.xpath;

import javax.xml.transform.TransformerException;
import org.w3c.dom.Element;

public interface WhitespaceStrippingElementMatcher {
    boolean canStripWhiteSpace();

    boolean shouldStripWhiteSpace(XPathContext xPathContext, Element element) throws TransformerException;
}

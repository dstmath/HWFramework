package javax.xml.xpath;

import javax.xml.namespace.QName;

public interface XPathFunctionResolver {
    XPathFunction resolveFunction(QName qName, int i);
}

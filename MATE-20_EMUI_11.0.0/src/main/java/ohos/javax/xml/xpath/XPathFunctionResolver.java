package ohos.javax.xml.xpath;

import ohos.javax.xml.namespace.QName;

public interface XPathFunctionResolver {
    XPathFunction resolveFunction(QName qName, int i);
}

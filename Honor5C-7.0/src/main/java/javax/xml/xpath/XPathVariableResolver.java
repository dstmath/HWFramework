package javax.xml.xpath;

import javax.xml.namespace.QName;

public interface XPathVariableResolver {
    Object resolveVariable(QName qName);
}

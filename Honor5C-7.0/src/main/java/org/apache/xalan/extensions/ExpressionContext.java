package org.apache.xalan.extensions;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public interface ExpressionContext {
    Node getContextNode();

    NodeIterator getContextNodes();

    ErrorListener getErrorListener();

    XObject getVariableOrParam(QName qName) throws TransformerException;

    XPathContext getXPathContext() throws TransformerException;

    double toNumber(Node node);

    String toString(Node node);
}

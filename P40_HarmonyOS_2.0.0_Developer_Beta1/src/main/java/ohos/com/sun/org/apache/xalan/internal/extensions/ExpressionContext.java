package ohos.com.sun.org.apache.xalan.internal.extensions;

import ohos.com.sun.org.apache.xml.internal.utils.QName;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.traversal.NodeIterator;

public interface ExpressionContext {
    Node getContextNode();

    NodeIterator getContextNodes();

    ErrorListener getErrorListener();

    XObject getVariableOrParam(QName qName) throws TransformerException;

    XPathContext getXPathContext() throws TransformerException;

    double toNumber(Node node);

    String toString(Node node);
}

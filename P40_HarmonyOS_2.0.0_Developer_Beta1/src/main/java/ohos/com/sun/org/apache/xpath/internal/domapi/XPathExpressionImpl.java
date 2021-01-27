package ohos.com.sun.org.apache.xpath.internal.domapi;

import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.res.XPATHMessages;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.xpath.XPathException;
import ohos.org.w3c.dom.xpath.XPathExpression;

class XPathExpressionImpl implements XPathExpression {
    private final Document m_doc;
    private final XPath m_xpath;

    XPathExpressionImpl(XPath xPath, Document document) {
        this.m_xpath = xPath;
        this.m_doc = document;
    }

    public Object evaluate(Node node, short s, Object obj) throws XPathException, DOMException {
        Document document = this.m_doc;
        if (document != null) {
            if (node == document || node.getOwnerDocument().equals(this.m_doc)) {
                short nodeType = node.getNodeType();
                if (!(nodeType == 9 || nodeType == 1 || nodeType == 2 || nodeType == 3 || nodeType == 4 || nodeType == 8 || nodeType == 7 || nodeType == 13)) {
                    throw new DOMException(9, XPATHMessages.createXPATHMessage("ER_WRONG_NODETYPE", null));
                }
            } else {
                throw new DOMException(4, XPATHMessages.createXPATHMessage("ER_WRONG_DOCUMENT", null));
            }
        }
        if (XPathResultImpl.isValidType(s)) {
            XPathContext xPathContext = new XPathContext();
            Document document2 = this.m_doc;
            if (document2 != null) {
                xPathContext.getDTMHandleFromNode(document2);
            }
            try {
                return new XPathResultImpl(s, this.m_xpath.execute(xPathContext, node, (PrefixResolver) null), node, this.m_xpath);
            } catch (TransformerException e) {
                throw new XPathException(1, e.getMessageAndLocation());
            }
        } else {
            throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_INVALID_XPATH_TYPE", new Object[]{new Integer(s)}));
        }
    }
}

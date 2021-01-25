package ohos.com.sun.org.apache.xpath.internal.domapi;

import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.res.XPATHMessages;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.xpath.XPathEvaluator;
import ohos.org.w3c.dom.xpath.XPathException;
import ohos.org.w3c.dom.xpath.XPathExpression;
import ohos.org.w3c.dom.xpath.XPathNSResolver;

public final class XPathEvaluatorImpl implements XPathEvaluator {
    private final Document m_doc;

    /* access modifiers changed from: private */
    public class DummyPrefixResolver implements PrefixResolver {
        @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
        public String getBaseIdentifier() {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
        public boolean handlesNullPrefixes() {
            return false;
        }

        DummyPrefixResolver() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
        public String getNamespaceForPrefix(String str, Node node) {
            throw new DOMException(14, XPATHMessages.createXPATHMessage("ER_NULL_RESOLVER", null));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver
        public String getNamespaceForPrefix(String str) {
            return getNamespaceForPrefix(str, null);
        }
    }

    public XPathEvaluatorImpl(Document document) {
        this.m_doc = document;
    }

    public XPathEvaluatorImpl() {
        this.m_doc = null;
    }

    public XPathExpression createExpression(String str, XPathNSResolver xPathNSResolver) throws XPathException, DOMException {
        try {
            return new XPathExpressionImpl(new XPath(str, null, xPathNSResolver == null ? new DummyPrefixResolver() : (PrefixResolver) xPathNSResolver, 0), this.m_doc);
        } catch (TransformerException e) {
            if (e instanceof XPathStylesheetDOM3Exception) {
                throw new DOMException(14, e.getMessageAndLocation());
            }
            throw new XPathException(1, e.getMessageAndLocation());
        }
    }

    public XPathNSResolver createNSResolver(Node node) {
        if (node.getNodeType() == 9) {
            node = ((Document) node).getDocumentElement();
        }
        return new XPathNSResolverImpl(node);
    }

    public Object evaluate(String str, Node node, XPathNSResolver xPathNSResolver, short s, Object obj) throws XPathException, DOMException {
        return createExpression(str, xPathNSResolver).evaluate(node, s, obj);
    }
}

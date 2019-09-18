package org.apache.xalan.templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.UnImplNode;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.WhitespaceStrippingElementMatcher;
import org.apache.xpath.XPathContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

public class ElemTemplateElement extends UnImplNode implements PrefixResolver, Serializable, ExpressionNode, WhitespaceStrippingElementMatcher, XSLTVisitable {
    static final long serialVersionUID = 4440018597841834447L;
    private transient Node m_DOMBackPointer;
    private int m_columnNumber;
    private List m_declaredPrefixes;
    private boolean m_defaultSpace = true;
    protected int m_docOrderNumber = -1;
    private int m_endColumnNumber;
    private int m_endLineNumber;
    ElemTemplateElement m_firstChild;
    private boolean m_hasTextLitOnly = false;
    protected boolean m_hasVariableDecl = false;
    private int m_lineNumber;
    ElemTemplateElement m_nextSibling;
    protected ElemTemplateElement m_parentNode;
    private List m_prefixTable;

    public boolean isCompiledTemplate() {
        return false;
    }

    public int getXSLToken() {
        return -1;
    }

    public String getNodeName() {
        return "Unknown XSLT Element";
    }

    public String getLocalName() {
        return getNodeName();
    }

    public void runtimeInit(TransformerImpl transformer) throws TransformerException {
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
    }

    public StylesheetComposed getStylesheetComposed() {
        return this.m_parentNode.getStylesheetComposed();
    }

    public Stylesheet getStylesheet() {
        if (this.m_parentNode == null) {
            return null;
        }
        return this.m_parentNode.getStylesheet();
    }

    public StylesheetRoot getStylesheetRoot() {
        return this.m_parentNode.getStylesheetRoot();
    }

    public void recompose(StylesheetRoot root) throws TransformerException {
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        resolvePrefixTables();
        ElemTemplateElement t = getFirstChildElem();
        this.m_hasTextLitOnly = t != null && t.getXSLToken() == 78 && t.getNextSiblingElem() == null;
        sroot.getComposeState().pushStackMark();
    }

    public void endCompose(StylesheetRoot sroot) throws TransformerException {
        sroot.getComposeState().popStackMark();
    }

    public void error(String msg, Object[] args) {
        throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_ELEMTEMPLATEELEM_ERR, new Object[]{XSLMessages.createMessage(msg, args)}));
    }

    public void error(String msg) {
        error(msg, null);
    }

    public Node appendChild(Node newChild) throws DOMException {
        if (newChild == null) {
            error(XSLTErrorResources.ER_NULL_CHILD, null);
        }
        ElemTemplateElement elem = (ElemTemplateElement) newChild;
        if (this.m_firstChild == null) {
            this.m_firstChild = elem;
        } else {
            ((ElemTemplateElement) getLastChild()).m_nextSibling = elem;
        }
        elem.m_parentNode = this;
        return newChild;
    }

    public ElemTemplateElement appendChild(ElemTemplateElement elem) {
        if (elem == null) {
            error(XSLTErrorResources.ER_NULL_CHILD, null);
        }
        if (this.m_firstChild == null) {
            this.m_firstChild = elem;
        } else {
            getLastChildElem().m_nextSibling = elem;
        }
        elem.setParentElem(this);
        return elem;
    }

    public boolean hasChildNodes() {
        return this.m_firstChild != null;
    }

    public short getNodeType() {
        return 1;
    }

    public NodeList getChildNodes() {
        return this;
    }

    public ElemTemplateElement removeChild(ElemTemplateElement childETE) {
        if (childETE == null || childETE.m_parentNode != this) {
            return null;
        }
        if (childETE == this.m_firstChild) {
            this.m_firstChild = childETE.m_nextSibling;
        } else {
            childETE.getPreviousSiblingElem().m_nextSibling = childETE.m_nextSibling;
        }
        childETE.m_parentNode = null;
        childETE.m_nextSibling = null;
        return childETE;
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        if (oldChild == null || oldChild.getParentNode() != this) {
            return null;
        }
        ElemTemplateElement newChildElem = (ElemTemplateElement) newChild;
        ElemTemplateElement oldChildElem = (ElemTemplateElement) oldChild;
        ElemTemplateElement prev = (ElemTemplateElement) oldChildElem.getPreviousSibling();
        if (prev != null) {
            prev.m_nextSibling = newChildElem;
        }
        if (this.m_firstChild == oldChildElem) {
            this.m_firstChild = newChildElem;
        }
        newChildElem.m_parentNode = this;
        oldChildElem.m_parentNode = null;
        newChildElem.m_nextSibling = oldChildElem.m_nextSibling;
        oldChildElem.m_nextSibling = null;
        return newChildElem;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        if (refChild == null) {
            appendChild(newChild);
            return newChild;
        } else if (newChild == refChild) {
            return newChild;
        } else {
            Node node = this.m_firstChild;
            Node prev = null;
            boolean foundit = false;
            while (node != null) {
                if (newChild == node) {
                    if (prev != null) {
                        ((ElemTemplateElement) prev).m_nextSibling = (ElemTemplateElement) node.getNextSibling();
                    } else {
                        this.m_firstChild = (ElemTemplateElement) node.getNextSibling();
                    }
                    node = node.getNextSibling();
                } else if (refChild == node) {
                    if (prev != null) {
                        ((ElemTemplateElement) prev).m_nextSibling = (ElemTemplateElement) newChild;
                    } else {
                        this.m_firstChild = (ElemTemplateElement) newChild;
                    }
                    ((ElemTemplateElement) newChild).m_nextSibling = (ElemTemplateElement) refChild;
                    ((ElemTemplateElement) newChild).setParentElem(this);
                    prev = newChild;
                    node = node.getNextSibling();
                    foundit = true;
                } else {
                    prev = node;
                    node = node.getNextSibling();
                }
            }
            if (foundit) {
                return newChild;
            }
            throw new DOMException(8, "refChild was not found in insertBefore method!");
        }
    }

    public ElemTemplateElement replaceChild(ElemTemplateElement newChildElem, ElemTemplateElement oldChildElem) {
        if (oldChildElem == null || oldChildElem.getParentElem() != this) {
            return null;
        }
        ElemTemplateElement prev = oldChildElem.getPreviousSiblingElem();
        if (prev != null) {
            prev.m_nextSibling = newChildElem;
        }
        if (this.m_firstChild == oldChildElem) {
            this.m_firstChild = newChildElem;
        }
        newChildElem.m_parentNode = this;
        oldChildElem.m_parentNode = null;
        newChildElem.m_nextSibling = oldChildElem.m_nextSibling;
        oldChildElem.m_nextSibling = null;
        return newChildElem;
    }

    public int getLength() {
        int count = 0;
        for (ElemTemplateElement node = this.m_firstChild; node != null; node = node.m_nextSibling) {
            count++;
        }
        return count;
    }

    public Node item(int index) {
        ElemTemplateElement node = this.m_firstChild;
        for (int i = 0; i < index && node != null; i++) {
            node = node.m_nextSibling;
        }
        return node;
    }

    public Document getOwnerDocument() {
        return getStylesheet();
    }

    public ElemTemplate getOwnerXSLTemplate() {
        ElemTemplateElement el = this;
        int type = el.getXSLToken();
        while (el != null && type != 19) {
            el = el.getParentElem();
            if (el != null) {
                type = el.getXSLToken();
            }
        }
        return (ElemTemplate) el;
    }

    public String getTagName() {
        return getNodeName();
    }

    public boolean hasTextLitOnly() {
        return this.m_hasTextLitOnly;
    }

    public String getBaseIdentifier() {
        return getSystemId();
    }

    public int getEndLineNumber() {
        return this.m_endLineNumber;
    }

    public int getLineNumber() {
        return this.m_lineNumber;
    }

    public int getEndColumnNumber() {
        return this.m_endColumnNumber;
    }

    public int getColumnNumber() {
        return this.m_columnNumber;
    }

    public String getPublicId() {
        if (this.m_parentNode != null) {
            return this.m_parentNode.getPublicId();
        }
        return null;
    }

    public String getSystemId() {
        Stylesheet sheet = getStylesheet();
        if (sheet == null) {
            return null;
        }
        return sheet.getHref();
    }

    public void setLocaterInfo(SourceLocator locator) {
        this.m_lineNumber = locator.getLineNumber();
        this.m_columnNumber = locator.getColumnNumber();
    }

    public void setEndLocaterInfo(SourceLocator locator) {
        this.m_endLineNumber = locator.getLineNumber();
        this.m_endColumnNumber = locator.getColumnNumber();
    }

    public boolean hasVariableDecl() {
        return this.m_hasVariableDecl;
    }

    public void setXmlSpace(int v) {
        this.m_defaultSpace = 2 == v;
    }

    public boolean getXmlSpace() {
        return this.m_defaultSpace;
    }

    public List getDeclaredPrefixes() {
        return this.m_declaredPrefixes;
    }

    public void setPrefixes(NamespaceSupport nsSupport) throws TransformerException {
        setPrefixes(nsSupport, false);
    }

    public void setPrefixes(NamespaceSupport nsSupport, boolean excludeXSLDecl) throws TransformerException {
        Enumeration decls = nsSupport.getDeclaredPrefixes();
        while (decls.hasMoreElements()) {
            String prefix = (String) decls.nextElement();
            if (this.m_declaredPrefixes == null) {
                this.m_declaredPrefixes = new ArrayList();
            }
            String uri = nsSupport.getURI(prefix);
            if (!excludeXSLDecl || !uri.equals(Constants.S_XSLNAMESPACEURL)) {
                this.m_declaredPrefixes.add(new XMLNSDecl(prefix, uri, false));
            }
        }
    }

    public String getNamespaceForPrefix(String prefix, Node context) {
        error(XSLTErrorResources.ER_CANT_RESOLVE_NSPREFIX, null);
        return null;
    }

    public String getNamespaceForPrefix(String prefix) {
        List nsDecls = this.m_declaredPrefixes;
        if (nsDecls != null) {
            int n = nsDecls.size();
            if (prefix.equals("#default")) {
                prefix = "";
            }
            for (int i = 0; i < n; i++) {
                XMLNSDecl decl = (XMLNSDecl) nsDecls.get(i);
                if (prefix.equals(decl.getPrefix())) {
                    return decl.getURI();
                }
            }
        }
        if (this.m_parentNode != null) {
            return this.m_parentNode.getNamespaceForPrefix(prefix);
        }
        if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public List getPrefixTable() {
        return this.m_prefixTable;
    }

    /* access modifiers changed from: package-private */
    public void setPrefixTable(List list) {
        this.m_prefixTable = list;
    }

    public boolean containsExcludeResultPrefix(String prefix, String uri) {
        ElemTemplateElement parent = getParentElem();
        if (parent != null) {
            return parent.containsExcludeResultPrefix(prefix, uri);
        }
        return false;
    }

    private boolean excludeResultNSDecl(String prefix, String uri) throws TransformerException {
        if (uri == null || (!uri.equals(Constants.S_XSLNAMESPACEURL) && !getStylesheet().containsExtensionElementURI(uri) && !containsExcludeResultPrefix(prefix, uri))) {
            return false;
        }
        return true;
    }

    public void resolvePrefixTables() throws TransformerException {
        XMLNSDecl decl;
        setPrefixTable(null);
        if (this.m_declaredPrefixes != null) {
            StylesheetRoot stylesheet = getStylesheetRoot();
            int n = this.m_declaredPrefixes.size();
            for (int i = 0; i < n; i++) {
                XMLNSDecl decl2 = (XMLNSDecl) this.m_declaredPrefixes.get(i);
                String prefix = decl2.getPrefix();
                String uri = decl2.getURI();
                if (uri == null) {
                    uri = "";
                }
                boolean shouldExclude = excludeResultNSDecl(prefix, uri);
                if (this.m_prefixTable == null) {
                    setPrefixTable(new ArrayList());
                }
                NamespaceAlias nsAlias = stylesheet.getNamespaceAliasComposed(uri);
                if (nsAlias != null) {
                    decl = new XMLNSDecl(nsAlias.getStylesheetPrefix(), nsAlias.getResultNamespace(), shouldExclude);
                } else {
                    decl = new XMLNSDecl(prefix, uri, shouldExclude);
                }
                this.m_prefixTable.add(decl);
            }
        }
        ElemTemplateElement parent = getParentNodeElem();
        if (parent != null) {
            List prefixes = parent.m_prefixTable;
            if (this.m_prefixTable != null || needToCheckExclude()) {
                int n2 = prefixes.size();
                for (int i2 = 0; i2 < n2; i2++) {
                    XMLNSDecl decl3 = (XMLNSDecl) prefixes.get(i2);
                    boolean shouldExclude2 = excludeResultNSDecl(decl3.getPrefix(), decl3.getURI());
                    if (shouldExclude2 != decl3.getIsExcluded()) {
                        decl3 = new XMLNSDecl(decl3.getPrefix(), decl3.getURI(), shouldExclude2);
                    }
                    addOrReplaceDecls(decl3);
                }
                return;
            }
            setPrefixTable(parent.m_prefixTable);
        } else if (this.m_prefixTable == null) {
            setPrefixTable(new ArrayList());
        }
    }

    /* access modifiers changed from: package-private */
    public void addOrReplaceDecls(XMLNSDecl newDecl) {
        int i = this.m_prefixTable.size() - 1;
        while (i >= 0) {
            if (!((XMLNSDecl) this.m_prefixTable.get(i)).getPrefix().equals(newDecl.getPrefix())) {
                i--;
            } else {
                return;
            }
        }
        this.m_prefixTable.add(newDecl);
    }

    /* access modifiers changed from: package-private */
    public boolean needToCheckExclude() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void executeNSDecls(TransformerImpl transformer) throws TransformerException {
        executeNSDecls(transformer, null);
    }

    /* access modifiers changed from: package-private */
    public void executeNSDecls(TransformerImpl transformer, String ignorePrefix) throws TransformerException {
        try {
            if (this.m_prefixTable != null) {
                SerializationHandler rhandler = transformer.getResultTreeHandler();
                for (int i = this.m_prefixTable.size() - 1; i >= 0; i--) {
                    XMLNSDecl decl = (XMLNSDecl) this.m_prefixTable.get(i);
                    if (!decl.getIsExcluded() && (ignorePrefix == null || !decl.getPrefix().equals(ignorePrefix))) {
                        rhandler.startPrefixMapping(decl.getPrefix(), decl.getURI(), true);
                    }
                }
            }
        } catch (SAXException se) {
            throw new TransformerException(se);
        }
    }

    /* access modifiers changed from: package-private */
    public void unexecuteNSDecls(TransformerImpl transformer) throws TransformerException {
        unexecuteNSDecls(transformer, null);
    }

    /* access modifiers changed from: package-private */
    public void unexecuteNSDecls(TransformerImpl transformer, String ignorePrefix) throws TransformerException {
        try {
            if (this.m_prefixTable != null) {
                SerializationHandler rhandler = transformer.getResultTreeHandler();
                int n = this.m_prefixTable.size();
                for (int i = 0; i < n; i++) {
                    XMLNSDecl decl = (XMLNSDecl) this.m_prefixTable.get(i);
                    if (!decl.getIsExcluded() && (ignorePrefix == null || !decl.getPrefix().equals(ignorePrefix))) {
                        rhandler.endPrefixMapping(decl.getPrefix());
                    }
                }
            }
        } catch (SAXException se) {
            throw new TransformerException(se);
        }
    }

    public void setUid(int i) {
        this.m_docOrderNumber = i;
    }

    public int getUid() {
        return this.m_docOrderNumber;
    }

    public Node getParentNode() {
        return this.m_parentNode;
    }

    public ElemTemplateElement getParentElem() {
        return this.m_parentNode;
    }

    public void setParentElem(ElemTemplateElement p) {
        this.m_parentNode = p;
    }

    public Node getNextSibling() {
        return this.m_nextSibling;
    }

    public Node getPreviousSibling() {
        Node walker = getParentNode();
        Node prev = null;
        if (walker != null) {
            for (Node walker2 = walker.getFirstChild(); walker2 != null; walker2 = walker2.getNextSibling()) {
                if (walker2 == this) {
                    return prev;
                }
                prev = walker2;
            }
        }
        return null;
    }

    public ElemTemplateElement getPreviousSiblingElem() {
        ElemTemplateElement walker = getParentNodeElem();
        ElemTemplateElement prev = null;
        if (walker != null) {
            for (ElemTemplateElement walker2 = walker.getFirstChildElem(); walker2 != null; walker2 = walker2.getNextSiblingElem()) {
                if (walker2 == this) {
                    return prev;
                }
                prev = walker2;
            }
        }
        return null;
    }

    public ElemTemplateElement getNextSiblingElem() {
        return this.m_nextSibling;
    }

    public ElemTemplateElement getParentNodeElem() {
        return this.m_parentNode;
    }

    public Node getFirstChild() {
        return this.m_firstChild;
    }

    public ElemTemplateElement getFirstChildElem() {
        return this.m_firstChild;
    }

    public Node getLastChild() {
        ElemTemplateElement lastChild = null;
        for (ElemTemplateElement node = this.m_firstChild; node != null; node = node.m_nextSibling) {
            lastChild = node;
        }
        return lastChild;
    }

    public ElemTemplateElement getLastChildElem() {
        ElemTemplateElement lastChild = null;
        for (ElemTemplateElement node = this.m_firstChild; node != null; node = node.m_nextSibling) {
            lastChild = node;
        }
        return lastChild;
    }

    public Node getDOMBackPointer() {
        return this.m_DOMBackPointer;
    }

    public void setDOMBackPointer(Node n) {
        this.m_DOMBackPointer = n;
    }

    public int compareTo(Object o) throws ClassCastException {
        ElemTemplateElement ro = (ElemTemplateElement) o;
        int roPrecedence = ro.getStylesheetComposed().getImportCountComposed();
        int myPrecedence = getStylesheetComposed().getImportCountComposed();
        if (myPrecedence < roPrecedence) {
            return -1;
        }
        if (myPrecedence > roPrecedence) {
            return 1;
        }
        return getUid() - ro.getUid();
    }

    public boolean shouldStripWhiteSpace(XPathContext support, Element targetElement) throws TransformerException {
        StylesheetRoot sroot = getStylesheetRoot();
        if (sroot != null) {
            return sroot.shouldStripWhiteSpace(support, targetElement);
        }
        return false;
    }

    public boolean canStripWhiteSpace() {
        StylesheetRoot sroot = getStylesheetRoot();
        if (sroot != null) {
            return sroot.canStripWhiteSpace();
        }
        return false;
    }

    public boolean canAcceptVariables() {
        return true;
    }

    public void exprSetParent(ExpressionNode n) {
        setParentElem((ElemTemplateElement) n);
    }

    public ExpressionNode exprGetParent() {
        return getParentElem();
    }

    public void exprAddChild(ExpressionNode n, int i) {
        appendChild((ElemTemplateElement) n);
    }

    public ExpressionNode exprGetChild(int i) {
        return (ExpressionNode) item(i);
    }

    public int exprGetNumChildren() {
        return getLength();
    }

    /* access modifiers changed from: protected */
    public boolean accept(XSLTVisitor visitor) {
        return visitor.visitInstruction(this);
    }

    public void callVisitors(XSLTVisitor visitor) {
        if (accept(visitor)) {
            callChildVisitors(visitor);
        }
    }

    /* access modifiers changed from: protected */
    public void callChildVisitors(XSLTVisitor visitor, boolean callAttributes) {
        for (ElemTemplateElement node = this.m_firstChild; node != null; node = node.m_nextSibling) {
            node.callVisitors(visitor);
        }
    }

    /* access modifiers changed from: protected */
    public void callChildVisitors(XSLTVisitor visitor) {
        callChildVisitors(visitor, true);
    }

    public boolean handlesNullPrefixes() {
        return false;
    }
}

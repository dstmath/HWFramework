package org.apache.xalan.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.StringVector;
import org.apache.xpath.XPathContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

public class ElemLiteralResult extends ElemUse {
    private static final String EMPTYSTRING = "";
    static final long serialVersionUID = -8703409074421657260L;
    private boolean isLiteralResultAsStylesheet = false;
    private StringVector m_ExtensionElementURIs;
    /* access modifiers changed from: private */
    public List m_avts = null;
    private StringVector m_excludeResultPrefixes;
    private String m_localName;
    private String m_namespace;
    private String m_rawName;
    private String m_version;
    private List m_xslAttr = null;

    public class Attribute implements Attr {
        private AVT m_attribute;
        private Element m_owner = null;

        public Attribute(AVT avt, Element elem) {
            this.m_attribute = avt;
            this.m_owner = elem;
        }

        public Node appendChild(Node newChild) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node cloneNode(boolean deep) {
            return new Attribute(this.m_attribute, this.m_owner);
        }

        public NamedNodeMap getAttributes() {
            return null;
        }

        public NodeList getChildNodes() {
            return new NodeList() {
                public int getLength() {
                    return 0;
                }

                public Node item(int index) {
                    return null;
                }
            };
        }

        public Node getFirstChild() {
            return null;
        }

        public Node getLastChild() {
            return null;
        }

        public String getLocalName() {
            return this.m_attribute.getName();
        }

        public String getNamespaceURI() {
            String uri = this.m_attribute.getURI();
            if (uri.equals("")) {
                return null;
            }
            return uri;
        }

        public Node getNextSibling() {
            return null;
        }

        public String getNodeName() {
            String uri = this.m_attribute.getURI();
            String localName = getLocalName();
            if (uri.equals("")) {
                return localName;
            }
            return uri + ":" + localName;
        }

        public short getNodeType() {
            return 2;
        }

        public String getNodeValue() throws DOMException {
            return this.m_attribute.getSimpleString();
        }

        public Document getOwnerDocument() {
            return this.m_owner.getOwnerDocument();
        }

        public Node getParentNode() {
            return this.m_owner;
        }

        public String getPrefix() {
            String uri = this.m_attribute.getURI();
            String rawName = this.m_attribute.getRawName();
            if (uri.equals("")) {
                return null;
            }
            return rawName.substring(0, rawName.indexOf(":"));
        }

        public Node getPreviousSibling() {
            return null;
        }

        public boolean hasAttributes() {
            return false;
        }

        public boolean hasChildNodes() {
            return false;
        }

        public Node insertBefore(Node newChild, Node refChild) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public boolean isSupported(String feature, String version) {
            return false;
        }

        public void normalize() {
        }

        public Node removeChild(Node oldChild) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public void setNodeValue(String nodeValue) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
        }

        public void setPrefix(String prefix) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
        }

        public String getName() {
            return this.m_attribute.getName();
        }

        public String getValue() {
            return this.m_attribute.getSimpleString();
        }

        public Element getOwnerElement() {
            return this.m_owner;
        }

        public boolean getSpecified() {
            return true;
        }

        public void setValue(String value) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
        }

        public TypeInfo getSchemaTypeInfo() {
            return null;
        }

        public boolean isId() {
            return false;
        }

        public Object setUserData(String key, Object data, UserDataHandler handler) {
            return getOwnerDocument().setUserData(key, data, handler);
        }

        public Object getUserData(String key) {
            return getOwnerDocument().getUserData(key);
        }

        public Object getFeature(String feature, String version) {
            if (isSupported(feature, version)) {
                return this;
            }
            return null;
        }

        public boolean isEqualNode(Node arg) {
            return arg == this;
        }

        public String lookupNamespaceURI(String specifiedPrefix) {
            return null;
        }

        public boolean isDefaultNamespace(String namespaceURI) {
            return false;
        }

        public String lookupPrefix(String namespaceURI) {
            return null;
        }

        public boolean isSameNode(Node other) {
            return this == other;
        }

        public void setTextContent(String textContent) throws DOMException {
            setNodeValue(textContent);
        }

        public String getTextContent() throws DOMException {
            return getNodeValue();
        }

        public short compareDocumentPosition(Node other) throws DOMException {
            return 0;
        }

        public String getBaseURI() {
            return null;
        }
    }

    public class LiteralElementAttributes implements NamedNodeMap {
        private int m_count = -1;

        public LiteralElementAttributes() {
        }

        public int getLength() {
            if (this.m_count == -1) {
                if (ElemLiteralResult.this.m_avts != null) {
                    this.m_count = ElemLiteralResult.this.m_avts.size();
                } else {
                    this.m_count = 0;
                }
            }
            return this.m_count;
        }

        public Node getNamedItem(String name) {
            AVT avt;
            if (getLength() == 0) {
                return null;
            }
            String uri = null;
            String localName = name;
            int index = name.indexOf(":");
            if (-1 != index) {
                uri = name.substring(0, index);
                localName = name.substring(index + 1);
            }
            Node retNode = null;
            Iterator eum = ElemLiteralResult.this.m_avts.iterator();
            while (true) {
                if (!eum.hasNext()) {
                    break;
                }
                avt = (AVT) eum.next();
                if (localName.equals(avt.getName())) {
                    String nsURI = avt.getURI();
                    if ((uri == null && nsURI == null) || (uri != null && uri.equals(nsURI))) {
                        retNode = new Attribute(avt, ElemLiteralResult.this);
                    }
                }
            }
            retNode = new Attribute(avt, ElemLiteralResult.this);
            return retNode;
        }

        public Node getNamedItemNS(String namespaceURI, String localName) {
            AVT avt;
            if (getLength() == 0) {
                return null;
            }
            Node retNode = null;
            Iterator eum = ElemLiteralResult.this.m_avts.iterator();
            while (true) {
                if (!eum.hasNext()) {
                    break;
                }
                avt = (AVT) eum.next();
                if (localName.equals(avt.getName())) {
                    String nsURI = avt.getURI();
                    if ((namespaceURI == null && nsURI == null) || (namespaceURI != null && namespaceURI.equals(nsURI))) {
                        retNode = new Attribute(avt, ElemLiteralResult.this);
                    }
                }
            }
            retNode = new Attribute(avt, ElemLiteralResult.this);
            return retNode;
        }

        public Node item(int i) {
            if (getLength() == 0 || i >= ElemLiteralResult.this.m_avts.size()) {
                return null;
            }
            return new Attribute((AVT) ElemLiteralResult.this.m_avts.get(i), ElemLiteralResult.this);
        }

        public Node removeNamedItem(String name) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node setNamedItem(Node arg) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node setNamedItemNS(Node arg) throws DOMException {
            ElemLiteralResult.this.throwDOMException(7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }
    }

    public void setIsLiteralResultAsStylesheet(boolean b) {
        this.isLiteralResultAsStylesheet = b;
    }

    public boolean getIsLiteralResultAsStylesheet() {
        return this.isLiteralResultAsStylesheet;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        StylesheetRoot.ComposeState cstate = sroot.getComposeState();
        Vector vnames = cstate.getVariableNames();
        if (this.m_avts != null) {
            for (int i = this.m_avts.size() - 1; i >= 0; i--) {
                ((AVT) this.m_avts.get(i)).fixupVariables(vnames, cstate.getGlobalsSize());
            }
        }
    }

    public void addLiteralResultAttribute(AVT avt) {
        if (this.m_avts == null) {
            this.m_avts = new ArrayList();
        }
        this.m_avts.add(avt);
    }

    public void addLiteralResultAttribute(String att) {
        if (this.m_xslAttr == null) {
            this.m_xslAttr = new ArrayList();
        }
        this.m_xslAttr.add(att);
    }

    public void setXmlSpace(AVT avt) {
        addLiteralResultAttribute(avt);
        String val = avt.getSimpleString();
        if (val.equals(Constants.ATTRNAME_DEFAULT)) {
            super.setXmlSpace(2);
        } else if (val.equals("preserve")) {
            super.setXmlSpace(1);
        }
    }

    public AVT getLiteralResultAttributeNS(String namespaceURI, String localName) {
        if (this.m_avts != null) {
            for (int i = this.m_avts.size() - 1; i >= 0; i--) {
                AVT avt = (AVT) this.m_avts.get(i);
                if (avt.getName().equals(localName) && avt.getURI().equals(namespaceURI)) {
                    return avt;
                }
            }
        }
        return null;
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        AVT avt = getLiteralResultAttributeNS(namespaceURI, localName);
        if (avt != null) {
            return avt.getSimpleString();
        }
        return "";
    }

    public AVT getLiteralResultAttribute(String name) {
        if (this.m_avts != null) {
            for (int i = this.m_avts.size() - 1; i >= 0; i--) {
                AVT avt = (AVT) this.m_avts.get(i);
                String namespace = avt.getURI();
                if (namespace != null && !namespace.equals("")) {
                    if ((namespace + ":" + avt.getName()).equals(name)) {
                        return avt;
                    }
                }
                if ((namespace == null || namespace.equals("")) && avt.getRawName().equals(name)) {
                    return avt;
                }
            }
        }
        return null;
    }

    public String getAttribute(String rawName) {
        AVT avt = getLiteralResultAttribute(rawName);
        if (avt != null) {
            return avt.getSimpleString();
        }
        return "";
    }

    public boolean containsExcludeResultPrefix(String prefix, String uri) {
        if (uri == null || (this.m_excludeResultPrefixes == null && this.m_ExtensionElementURIs == null)) {
            return super.containsExcludeResultPrefix(prefix, uri);
        }
        if (prefix.length() == 0) {
            prefix = "#default";
        }
        if (this.m_excludeResultPrefixes != null) {
            for (int i = 0; i < this.m_excludeResultPrefixes.size(); i++) {
                if (uri.equals(getNamespaceForPrefix(this.m_excludeResultPrefixes.elementAt(i)))) {
                    return true;
                }
            }
        }
        if (this.m_ExtensionElementURIs == null || !this.m_ExtensionElementURIs.contains(uri)) {
            return super.containsExcludeResultPrefix(prefix, uri);
        }
        return true;
    }

    public void resolvePrefixTables() throws TransformerException {
        super.resolvePrefixTables();
        StylesheetRoot stylesheet = getStylesheetRoot();
        if (this.m_namespace != null && this.m_namespace.length() > 0) {
            NamespaceAlias nsa = stylesheet.getNamespaceAliasComposed(this.m_namespace);
            if (nsa != null) {
                this.m_namespace = nsa.getResultNamespace();
                String resultPrefix = nsa.getStylesheetPrefix();
                if (resultPrefix == null || resultPrefix.length() <= 0) {
                    this.m_rawName = this.m_localName;
                } else {
                    this.m_rawName = resultPrefix + ":" + this.m_localName;
                }
            }
        }
        if (this.m_avts != null) {
            int n = this.m_avts.size();
            for (int i = 0; i < n; i++) {
                AVT avt = (AVT) this.m_avts.get(i);
                String ns = avt.getURI();
                if (ns != null && ns.length() > 0) {
                    NamespaceAlias nsa2 = stylesheet.getNamespaceAliasComposed(this.m_namespace);
                    if (nsa2 != null) {
                        String namespace = nsa2.getResultNamespace();
                        String resultPrefix2 = nsa2.getStylesheetPrefix();
                        String rawName = avt.getName();
                        if (resultPrefix2 != null && resultPrefix2.length() > 0) {
                            rawName = resultPrefix2 + ":" + rawName;
                        }
                        avt.setURI(namespace);
                        avt.setRawName(rawName);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needToCheckExclude() {
        if (this.m_excludeResultPrefixes == null && getPrefixTable() == null && this.m_ExtensionElementURIs == null) {
            return false;
        }
        if (getPrefixTable() == null) {
            setPrefixTable(new ArrayList());
        }
        return true;
    }

    public void setNamespace(String ns) {
        if (ns == null) {
            ns = "";
        }
        this.m_namespace = ns;
    }

    public String getNamespace() {
        return this.m_namespace;
    }

    public void setLocalName(String localName) {
        this.m_localName = localName;
    }

    public String getLocalName() {
        return this.m_localName;
    }

    public void setRawName(String rawName) {
        this.m_rawName = rawName;
    }

    public String getRawName() {
        return this.m_rawName;
    }

    public String getPrefix() {
        int len = (this.m_rawName.length() - this.m_localName.length()) - 1;
        if (len > 0) {
            return this.m_rawName.substring(0, len);
        }
        return "";
    }

    public void setExtensionElementPrefixes(StringVector v) {
        this.m_ExtensionElementURIs = v;
    }

    public NamedNodeMap getAttributes() {
        return new LiteralElementAttributes();
    }

    public String getExtensionElementPrefix(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_ExtensionElementURIs != null) {
            return this.m_ExtensionElementURIs.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getExtensionElementPrefixCount() {
        if (this.m_ExtensionElementURIs != null) {
            return this.m_ExtensionElementURIs.size();
        }
        return 0;
    }

    public boolean containsExtensionElementURI(String uri) {
        if (this.m_ExtensionElementURIs == null) {
            return false;
        }
        return this.m_ExtensionElementURIs.contains(uri);
    }

    public int getXSLToken() {
        return 77;
    }

    public String getNodeName() {
        return this.m_rawName;
    }

    public void setVersion(String v) {
        this.m_version = v;
    }

    public String getVersion() {
        return this.m_version;
    }

    public void setExcludeResultPrefixes(StringVector v) {
        this.m_excludeResultPrefixes = v;
    }

    private boolean excludeResultNSDecl(String prefix, String uri) throws TransformerException {
        if (this.m_excludeResultPrefixes != null) {
            return containsExcludeResultPrefix(prefix, uri);
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ab  */
    public void execute(TransformerImpl transformer) throws TransformerException {
        SerializationHandler rhandler = transformer.getSerializationHandler();
        try {
            rhandler.startPrefixMapping(getPrefix(), getNamespace());
            executeNSDecls(transformer);
            rhandler.startElement(getNamespace(), getLocalName(), getRawName());
            TransformerException tException = null;
            try {
                super.execute(transformer);
                if (this.m_avts != null) {
                    int i = this.m_avts.size() - 1;
                    while (true) {
                        int i2 = i;
                        if (i2 < 0) {
                            break;
                        }
                        AVT avt = (AVT) this.m_avts.get(i2);
                        XPathContext xctxt = transformer.getXPathContext();
                        String stringedValue = avt.evaluate(xctxt, xctxt.getCurrentNode(), this);
                        if (stringedValue != null) {
                            rhandler.addAttribute(avt.getURI(), avt.getName(), avt.getRawName(), "CDATA", stringedValue, false);
                        }
                        i = i2 - 1;
                    }
                }
                try {
                    transformer.executeChildTemplates((ElemTemplateElement) this, true);
                } catch (TransformerException e) {
                    te = e;
                } catch (SAXException e2) {
                    se = e2;
                    tException = new TransformerException(se);
                    rhandler.endElement(getNamespace(), getLocalName(), getRawName());
                    if (tException != null) {
                    }
                }
            } catch (TransformerException e3) {
                te = e3;
                TransformerImpl transformerImpl = transformer;
                tException = te;
                rhandler.endElement(getNamespace(), getLocalName(), getRawName());
                if (tException != null) {
                }
            } catch (SAXException e4) {
                se = e4;
                TransformerImpl transformerImpl2 = transformer;
                tException = new TransformerException(se);
                rhandler.endElement(getNamespace(), getLocalName(), getRawName());
                if (tException != null) {
                }
            }
            try {
                rhandler.endElement(getNamespace(), getLocalName(), getRawName());
                if (tException != null) {
                    unexecuteNSDecls(transformer);
                    try {
                        rhandler.endPrefixMapping(getPrefix());
                    } catch (SAXException se) {
                        throw new TransformerException(se);
                    }
                } else {
                    throw tException;
                }
            } catch (SAXException se2) {
                if (tException != null) {
                    throw tException;
                }
                throw new TransformerException(se2);
            }
        } catch (SAXException se3) {
            TransformerImpl transformerImpl3 = transformer;
            throw new TransformerException(se3);
        }
    }

    public Iterator enumerateLiteralResultAttributes() {
        if (this.m_avts == null) {
            return null;
        }
        return this.m_avts.iterator();
    }

    /* access modifiers changed from: protected */
    public boolean accept(XSLTVisitor visitor) {
        return visitor.visitLiteralResultElement(this);
    }

    /* access modifiers changed from: protected */
    public void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        if (callAttrs && this.m_avts != null) {
            for (int i = this.m_avts.size() - 1; i >= 0; i--) {
                ((AVT) this.m_avts.get(i)).callVisitors(visitor);
            }
        }
        super.callChildVisitors(visitor, callAttrs);
    }

    public void throwDOMException(short code, String msg) {
        throw new DOMException(code, XSLMessages.createMessage(msg, null));
    }
}

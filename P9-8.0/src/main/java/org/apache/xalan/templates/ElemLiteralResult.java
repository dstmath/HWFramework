package org.apache.xalan.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
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
    private List m_avts = null;
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
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
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
            return uri.equals("") ? null : uri;
        }

        public Node getNextSibling() {
            return null;
        }

        public String getNodeName() {
            String uri = this.m_attribute.getURI();
            String localName = getLocalName();
            return uri.equals("") ? localName : uri + ":" + localName;
        }

        public short getNodeType() {
            return (short) 2;
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
            return uri.equals("") ? null : rawName.substring(0, rawName.indexOf(":"));
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
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public boolean isSupported(String feature, String version) {
            return false;
        }

        public void normalize() {
        }

        public Node removeChild(Node oldChild) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public void setNodeValue(String nodeValue) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
        }

        public void setPrefix(String prefix) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
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
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
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
            return isSupported(feature, version) ? this : null;
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
            return (short) 0;
        }

        public String getBaseURI() {
            return null;
        }
    }

    public class LiteralElementAttributes implements NamedNodeMap {
        private int m_count = -1;

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
            Node node = null;
            for (AVT avt : ElemLiteralResult.this.m_avts) {
                if (localName.equals(avt.getName())) {
                    String nsURI = avt.getURI();
                    if ((uri == null && nsURI == null) || (uri != null && uri.equals(nsURI))) {
                        node = new Attribute(avt, ElemLiteralResult.this);
                        break;
                    }
                }
            }
            return node;
        }

        public Node getNamedItemNS(String namespaceURI, String localName) {
            if (getLength() == 0) {
                return null;
            }
            Node node = null;
            for (AVT avt : ElemLiteralResult.this.m_avts) {
                if (localName.equals(avt.getName())) {
                    String nsURI = avt.getURI();
                    if ((namespaceURI == null && nsURI == null) || (namespaceURI != null && namespaceURI.equals(nsURI))) {
                        node = new Attribute(avt, ElemLiteralResult.this);
                        break;
                    }
                }
            }
            return node;
        }

        public Node item(int i) {
            if (getLength() == 0 || i >= ElemLiteralResult.this.m_avts.size()) {
                return null;
            }
            return new Attribute((AVT) ElemLiteralResult.this.m_avts.get(i), ElemLiteralResult.this);
        }

        public Node removeNamedItem(String name) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node setNamedItem(Node arg) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
            return null;
        }

        public Node setNamedItemNS(Node arg) throws DOMException {
            ElemLiteralResult.this.throwDOMException((short) 7, XSLTErrorResources.NO_MODIFICATION_ALLOWED_ERR);
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
        ComposeState cstate = sroot.getComposeState();
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
                if ((namespace != null && (namespace.equals("") ^ 1) != 0 && (namespace + ":" + avt.getName()).equals(name)) || ((namespace == null || namespace.equals("")) && avt.getRawName().equals(name))) {
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
        NamespaceAlias nsa;
        String resultPrefix;
        super.resolvePrefixTables();
        StylesheetRoot stylesheet = getStylesheetRoot();
        if (this.m_namespace != null && this.m_namespace.length() > 0) {
            nsa = stylesheet.getNamespaceAliasComposed(this.m_namespace);
            if (nsa != null) {
                this.m_namespace = nsa.getResultNamespace();
                resultPrefix = nsa.getStylesheetPrefix();
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
                    nsa = stylesheet.getNamespaceAliasComposed(this.m_namespace);
                    if (nsa != null) {
                        String namespace = nsa.getResultNamespace();
                        resultPrefix = nsa.getStylesheetPrefix();
                        String rawName = avt.getName();
                        if (resultPrefix != null && resultPrefix.length() > 0) {
                            rawName = resultPrefix + ":" + rawName;
                        }
                        avt.setURI(namespace);
                        avt.setRawName(rawName);
                    }
                }
            }
        }
    }

    boolean needToCheckExclude() {
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
        return this.m_ExtensionElementURIs != null ? this.m_ExtensionElementURIs.size() : 0;
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
                    for (int i = this.m_avts.size() - 1; i >= 0; i--) {
                        AVT avt = (AVT) this.m_avts.get(i);
                        XPathContext xctxt = transformer.getXPathContext();
                        String stringedValue = avt.evaluate(xctxt, xctxt.getCurrentNode(), this);
                        if (stringedValue != null) {
                            rhandler.addAttribute(avt.getURI(), avt.getName(), avt.getRawName(), "CDATA", stringedValue, false);
                        }
                    }
                }
                transformer.executeChildTemplates((ElemTemplateElement) this, true);
            } catch (TransformerException te) {
                tException = te;
            } catch (SAXException se) {
                tException = new TransformerException(se);
            }
            try {
                rhandler.endElement(getNamespace(), getLocalName(), getRawName());
                if (tException != null) {
                    throw tException;
                }
                unexecuteNSDecls(transformer);
                try {
                    rhandler.endPrefixMapping(getPrefix());
                } catch (SAXException se2) {
                    throw new TransformerException(se2);
                }
            } catch (SAXException se22) {
                if (tException != null) {
                    throw tException;
                }
                throw new TransformerException(se22);
            }
        } catch (SAXException se222) {
            throw new TransformerException(se222);
        }
    }

    public Iterator enumerateLiteralResultAttributes() {
        return this.m_avts == null ? null : this.m_avts.iterator();
    }

    protected boolean accept(XSLTVisitor visitor) {
        return visitor.visitLiteralResultElement(this);
    }

    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
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

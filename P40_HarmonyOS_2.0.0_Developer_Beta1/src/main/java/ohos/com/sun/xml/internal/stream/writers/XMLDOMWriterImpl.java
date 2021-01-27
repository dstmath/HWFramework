package ohos.com.sun.xml.internal.stream.writers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamWriter;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.helpers.NamespaceSupport;

public class XMLDOMWriterImpl implements XMLStreamWriter {
    private Node currentNode = null;
    private int depth = 0;
    private Method mXmlVersion = null;
    private NamespaceSupport namespaceContext = null;
    private boolean[] needContextPop = null;
    private Node node = null;
    private Document ownerDoc = null;
    private int resizeValue = 20;
    private StringBuffer stringBuffer = null;

    public void close() throws XMLStreamException {
    }

    public void flush() throws XMLStreamException {
    }

    public NamespaceContext getNamespaceContext() {
        return null;
    }

    public XMLDOMWriterImpl(DOMResult dOMResult) {
        this.node = dOMResult.getNode();
        if (this.node.getNodeType() == 9) {
            this.ownerDoc = this.node;
            this.currentNode = this.ownerDoc;
        } else {
            this.ownerDoc = this.node.getOwnerDocument();
            this.currentNode = this.node;
        }
        getDLThreeMethods();
        this.stringBuffer = new StringBuffer();
        this.needContextPop = new boolean[this.resizeValue];
        this.namespaceContext = new NamespaceSupport();
    }

    private void getDLThreeMethods() {
        try {
            this.mXmlVersion = this.ownerDoc.getClass().getMethod("setXmlVersion", String.class);
        } catch (NoSuchMethodException unused) {
            this.mXmlVersion = null;
        } catch (SecurityException unused2) {
            this.mXmlVersion = null;
        }
    }

    public String getPrefix(String str) throws XMLStreamException {
        NamespaceSupport namespaceSupport = this.namespaceContext;
        if (namespaceSupport != null) {
            return namespaceSupport.getPrefix(str);
        }
        return null;
    }

    public Object getProperty(String str) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    public void setDefaultNamespace(String str) throws XMLStreamException {
        this.namespaceContext.declarePrefix("", str);
        boolean[] zArr = this.needContextPop;
        int i = this.depth;
        if (!zArr[i]) {
            zArr[i] = true;
        }
    }

    public void setNamespaceContext(NamespaceContext namespaceContext2) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public void setPrefix(String str, String str2) throws XMLStreamException {
        if (str != null) {
            this.namespaceContext.declarePrefix(str, str2);
            boolean[] zArr = this.needContextPop;
            int i = this.depth;
            if (!zArr[i]) {
                zArr[i] = true;
                return;
            }
            return;
        }
        throw new XMLStreamException("Prefix cannot be null");
    }

    public void writeAttribute(String str, String str2) throws XMLStreamException {
        if (this.currentNode.getNodeType() == 1) {
            Attr createAttribute = this.ownerDoc.createAttribute(str);
            createAttribute.setValue(str2);
            this.currentNode.setAttributeNode(createAttribute);
            return;
        }
        throw new IllegalStateException("Current DOM Node type  is " + ((int) this.currentNode.getNodeType()) + "and does not allow attributes to be set ");
    }

    public void writeAttribute(String str, String str2, String str3) throws XMLStreamException {
        if (this.currentNode.getNodeType() == 1) {
            String str4 = null;
            if (str == null) {
                throw new XMLStreamException("NamespaceURI cannot be null");
            } else if (str2 != null) {
                NamespaceSupport namespaceSupport = this.namespaceContext;
                if (namespaceSupport != null) {
                    str4 = namespaceSupport.getPrefix(str);
                }
                if (str4 != null) {
                    if (!str4.equals("")) {
                        str2 = getQName(str4, str2);
                    }
                    Attr createAttributeNS = this.ownerDoc.createAttributeNS(str, str2);
                    createAttributeNS.setValue(str3);
                    this.currentNode.setAttributeNode(createAttributeNS);
                    return;
                }
                throw new XMLStreamException("Namespace URI " + str + "is not bound to any prefix");
            } else {
                throw new XMLStreamException("Local name cannot be null");
            }
        } else {
            throw new IllegalStateException("Current DOM Node type  is " + ((int) this.currentNode.getNodeType()) + "and does not allow attributes to be set ");
        }
    }

    public void writeAttribute(String str, String str2, String str3, String str4) throws XMLStreamException {
        if (this.currentNode.getNodeType() != 1) {
            throw new IllegalStateException("Current DOM Node type  is " + ((int) this.currentNode.getNodeType()) + "and does not allow attributes to be set ");
        } else if (str2 == null) {
            throw new XMLStreamException("NamespaceURI cannot be null");
        } else if (str3 == null) {
            throw new XMLStreamException("Local name cannot be null");
        } else if (str != null) {
            if (!str.equals("")) {
                str3 = getQName(str, str3);
            }
            Attr createAttributeNS = this.ownerDoc.createAttributeNS(str2, str3);
            createAttributeNS.setValue(str4);
            this.currentNode.setAttributeNodeNS(createAttributeNS);
        } else {
            throw new XMLStreamException("prefix cannot be null");
        }
    }

    public void writeCData(String str) throws XMLStreamException {
        if (str != null) {
            getNode().appendChild(this.ownerDoc.createCDATASection(str));
            return;
        }
        throw new XMLStreamException("CDATA cannot be null");
    }

    public void writeCharacters(String str) throws XMLStreamException {
        this.currentNode.appendChild(this.ownerDoc.createTextNode(str));
    }

    public void writeCharacters(char[] cArr, int i, int i2) throws XMLStreamException {
        this.currentNode.appendChild(this.ownerDoc.createTextNode(new String(cArr, i, i2)));
    }

    public void writeComment(String str) throws XMLStreamException {
        getNode().appendChild(this.ownerDoc.createComment(str));
    }

    public void writeDTD(String str) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public void writeDefaultNamespace(String str) throws XMLStreamException {
        if (this.currentNode.getNodeType() == 1) {
            this.currentNode.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", str);
            return;
        }
        throw new IllegalStateException("Current DOM Node type  is " + ((int) this.currentNode.getNodeType()) + "and does not allow attributes to be set ");
    }

    public void writeEmptyElement(String str) throws XMLStreamException {
        Document document = this.ownerDoc;
        if (document != null) {
            Element createElement = document.createElement(str);
            Node node2 = this.currentNode;
            if (node2 != null) {
                node2.appendChild(createElement);
            } else {
                this.ownerDoc.appendChild(createElement);
            }
        }
    }

    public void writeEmptyElement(String str, String str2) throws XMLStreamException {
        if (this.ownerDoc != null) {
            String str3 = null;
            if (str == null) {
                throw new XMLStreamException("NamespaceURI cannot be null");
            } else if (str2 != null) {
                NamespaceSupport namespaceSupport = this.namespaceContext;
                if (namespaceSupport != null) {
                    str3 = namespaceSupport.getPrefix(str);
                }
                if (str3 != null) {
                    if (!"".equals(str3)) {
                        str2 = getQName(str3, str2);
                    }
                    Element createElementNS = this.ownerDoc.createElementNS(str, str2);
                    Node node2 = this.currentNode;
                    if (node2 != null) {
                        node2.appendChild(createElementNS);
                    } else {
                        this.ownerDoc.appendChild(createElementNS);
                    }
                } else {
                    throw new XMLStreamException("Namespace URI " + str + "is not bound to any prefix");
                }
            } else {
                throw new XMLStreamException("Local name cannot be null");
            }
        }
    }

    public void writeEmptyElement(String str, String str2, String str3) throws XMLStreamException {
        if (this.ownerDoc == null) {
            return;
        }
        if (str3 == null) {
            throw new XMLStreamException("NamespaceURI cannot be null");
        } else if (str2 == null) {
            throw new XMLStreamException("Local name cannot be null");
        } else if (str != null) {
            if (!"".equals(str)) {
                str2 = getQName(str, str2);
            }
            Element createElementNS = this.ownerDoc.createElementNS(str3, str2);
            Node node2 = this.currentNode;
            if (node2 != null) {
                node2.appendChild(createElementNS);
            } else {
                this.ownerDoc.appendChild(createElementNS);
            }
        } else {
            throw new XMLStreamException("Prefix cannot be null");
        }
    }

    public void writeEndDocument() throws XMLStreamException {
        this.currentNode = null;
        int i = 0;
        while (true) {
            int i2 = this.depth;
            if (i < i2) {
                boolean[] zArr = this.needContextPop;
                if (zArr[i2]) {
                    zArr[i2] = false;
                    this.namespaceContext.popContext();
                }
                this.depth--;
                i++;
            } else {
                this.depth = 0;
                return;
            }
        }
    }

    public void writeEndElement() throws XMLStreamException {
        Node parentNode = this.currentNode.getParentNode();
        if (this.currentNode.getNodeType() == 9) {
            this.currentNode = null;
        } else {
            this.currentNode = parentNode;
        }
        boolean[] zArr = this.needContextPop;
        int i = this.depth;
        if (zArr[i]) {
            zArr[i] = false;
            this.namespaceContext.popContext();
        }
        this.depth--;
    }

    public void writeEntityRef(String str) throws XMLStreamException {
        this.currentNode.appendChild(this.ownerDoc.createEntityReference(str));
    }

    public void writeNamespace(String str, String str2) throws XMLStreamException {
        if (str == null) {
            throw new XMLStreamException("prefix cannot be null");
        } else if (str2 != null) {
            String str3 = "xmlns";
            if (!str.equals("")) {
                str3 = getQName(str3, str);
            }
            this.currentNode.setAttributeNS("http://www.w3.org/2000/xmlns/", str3, str2);
        } else {
            throw new XMLStreamException("NamespaceURI cannot be null");
        }
    }

    public void writeProcessingInstruction(String str) throws XMLStreamException {
        if (str != null) {
            this.currentNode.appendChild(this.ownerDoc.createProcessingInstruction(str, ""));
            return;
        }
        throw new XMLStreamException("Target cannot be null");
    }

    public void writeProcessingInstruction(String str, String str2) throws XMLStreamException {
        if (str != null) {
            this.currentNode.appendChild(this.ownerDoc.createProcessingInstruction(str, str2));
            return;
        }
        throw new XMLStreamException("Target cannot be null");
    }

    public void writeStartDocument() throws XMLStreamException {
        try {
            if (this.mXmlVersion != null) {
                this.mXmlVersion.invoke(this.ownerDoc, "1.0");
            }
        } catch (IllegalAccessException e) {
            throw new XMLStreamException(e);
        } catch (InvocationTargetException e2) {
            throw new XMLStreamException(e2);
        }
    }

    public void writeStartDocument(String str) throws XMLStreamException {
        try {
            if (this.mXmlVersion != null) {
                this.mXmlVersion.invoke(this.ownerDoc, str);
            }
        } catch (IllegalAccessException e) {
            throw new XMLStreamException(e);
        } catch (InvocationTargetException e2) {
            throw new XMLStreamException(e2);
        }
    }

    public void writeStartDocument(String str, String str2) throws XMLStreamException {
        try {
            if (this.mXmlVersion != null) {
                this.mXmlVersion.invoke(this.ownerDoc, str2);
            }
        } catch (IllegalAccessException e) {
            throw new XMLStreamException(e);
        } catch (InvocationTargetException e2) {
            throw new XMLStreamException(e2);
        }
    }

    public void writeStartElement(String str) throws XMLStreamException {
        Document document = this.ownerDoc;
        if (document != null) {
            Element createElement = document.createElement(str);
            Node node2 = this.currentNode;
            if (node2 != null) {
                node2.appendChild(createElement);
            } else {
                this.ownerDoc.appendChild(createElement);
            }
            this.currentNode = createElement;
        }
        if (this.needContextPop[this.depth]) {
            this.namespaceContext.pushContext();
        }
        incDepth();
    }

    public void writeStartElement(String str, String str2) throws XMLStreamException {
        if (this.ownerDoc != null) {
            String str3 = null;
            if (str == null) {
                throw new XMLStreamException("NamespaceURI cannot be null");
            } else if (str2 != null) {
                NamespaceSupport namespaceSupport = this.namespaceContext;
                if (namespaceSupport != null) {
                    str3 = namespaceSupport.getPrefix(str);
                }
                if (str3 != null) {
                    if (!"".equals(str3)) {
                        str2 = getQName(str3, str2);
                    }
                    Element createElementNS = this.ownerDoc.createElementNS(str, str2);
                    Node node2 = this.currentNode;
                    if (node2 != null) {
                        node2.appendChild(createElementNS);
                    } else {
                        this.ownerDoc.appendChild(createElementNS);
                    }
                    this.currentNode = createElementNS;
                } else {
                    throw new XMLStreamException("Namespace URI " + str + "is not bound to any prefix");
                }
            } else {
                throw new XMLStreamException("Local name cannot be null");
            }
        }
        if (this.needContextPop[this.depth]) {
            this.namespaceContext.pushContext();
        }
        incDepth();
    }

    public void writeStartElement(String str, String str2, String str3) throws XMLStreamException {
        if (this.ownerDoc == null) {
            return;
        }
        if (str3 == null) {
            throw new XMLStreamException("NamespaceURI cannot be null");
        } else if (str2 == null) {
            throw new XMLStreamException("Local name cannot be null");
        } else if (str != null) {
            if (!str.equals("")) {
                str2 = getQName(str, str2);
            }
            Element createElementNS = this.ownerDoc.createElementNS(str3, str2);
            Node node2 = this.currentNode;
            if (node2 != null) {
                node2.appendChild(createElementNS);
            } else {
                this.ownerDoc.appendChild(createElementNS);
            }
            this.currentNode = createElementNS;
            if (this.needContextPop[this.depth]) {
                this.namespaceContext.pushContext();
            }
            incDepth();
        } else {
            throw new XMLStreamException("Prefix cannot be null");
        }
    }

    private String getQName(String str, String str2) {
        this.stringBuffer.setLength(0);
        this.stringBuffer.append(str);
        this.stringBuffer.append(":");
        this.stringBuffer.append(str2);
        return this.stringBuffer.toString();
    }

    private Node getNode() {
        Node node2 = this.currentNode;
        return node2 == null ? this.ownerDoc : node2;
    }

    private void incDepth() {
        this.depth++;
        int i = this.depth;
        boolean[] zArr = this.needContextPop;
        if (i == zArr.length) {
            boolean[] zArr2 = new boolean[(this.resizeValue + i)];
            System.arraycopy(zArr, 0, zArr2, 0, i);
            this.needContextPop = zArr2;
        }
    }
}

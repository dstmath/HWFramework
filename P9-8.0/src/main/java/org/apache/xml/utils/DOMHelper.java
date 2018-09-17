package org.apache.xml.utils;

import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class DOMHelper {
    protected static final NSInfo m_NSInfoNullNoAncestorXMLNS = new NSInfo(true, false, 2);
    protected static final NSInfo m_NSInfoNullWithXMLNS = new NSInfo(true, true);
    protected static final NSInfo m_NSInfoNullWithoutXMLNS = new NSInfo(true, false);
    protected static final NSInfo m_NSInfoUnProcNoAncestorXMLNS = new NSInfo(false, false, 2);
    protected static final NSInfo m_NSInfoUnProcWithXMLNS = new NSInfo(false, true);
    protected static final NSInfo m_NSInfoUnProcWithoutXMLNS = new NSInfo(false, false);
    protected Document m_DOMFactory = null;
    Hashtable m_NSInfos = new Hashtable();
    protected Vector m_candidateNoAncestorXMLNS = new Vector();

    public static Document createDocument(boolean isSecureProcessing) {
        try {
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            dfactory.setNamespaceAware(true);
            return dfactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CREATEDOCUMENT_NOT_SUPPORTED, null));
        }
    }

    public static Document createDocument() {
        return createDocument(false);
    }

    public boolean shouldStripSourceNode(Node textNode) throws TransformerException {
        return false;
    }

    public String getUniqueID(Node node) {
        return "N" + Integer.toHexString(node.hashCode()).toUpperCase();
    }

    public static boolean isNodeAfter(Node node1, Node node2) {
        if (node1 == node2 || isNodeTheSame(node1, node2)) {
            return true;
        }
        boolean isNodeAfter = true;
        Node parent1 = getParentOfNode(node1);
        Node parent2 = getParentOfNode(node2);
        if (parent1 != parent2 && !isNodeTheSame(parent1, parent2)) {
            int nParents1 = 2;
            int nParents2 = 2;
            while (parent1 != null) {
                nParents1++;
                parent1 = getParentOfNode(parent1);
            }
            while (parent2 != null) {
                nParents2++;
                parent2 = getParentOfNode(parent2);
            }
            Node startNode1 = node1;
            Node startNode2 = node2;
            int adjust;
            int i;
            if (nParents1 < nParents2) {
                adjust = nParents2 - nParents1;
                for (i = 0; i < adjust; i++) {
                    startNode2 = getParentOfNode(startNode2);
                }
            } else if (nParents1 > nParents2) {
                adjust = nParents1 - nParents2;
                for (i = 0; i < adjust; i++) {
                    startNode1 = getParentOfNode(startNode1);
                }
            }
            Node prevChild1 = null;
            Node prevChild2 = null;
            while (startNode1 != null) {
                if (startNode1 == startNode2 || isNodeTheSame(startNode1, startNode2)) {
                    isNodeAfter = prevChild1 == null ? nParents1 < nParents2 : isNodeAfterSibling(startNode1, prevChild1, prevChild2);
                } else {
                    prevChild1 = startNode1;
                    startNode1 = getParentOfNode(startNode1);
                    prevChild2 = startNode2;
                    startNode2 = getParentOfNode(startNode2);
                }
            }
        } else if (parent1 != null) {
            isNodeAfter = isNodeAfterSibling(parent1, node1, node2);
        }
        return isNodeAfter;
    }

    public static boolean isNodeTheSame(Node node1, Node node2) {
        if ((node1 instanceof DTMNodeProxy) && (node2 instanceof DTMNodeProxy)) {
            return ((DTMNodeProxy) node1).equals((DTMNodeProxy) node2);
        }
        return node1 == node2;
    }

    private static boolean isNodeAfterSibling(Node parent, Node child1, Node child2) {
        short child1type = child1.getNodeType();
        short child2type = child2.getNodeType();
        if ((short) 2 != child1type && (short) 2 == child2type) {
            return false;
        }
        if ((short) 2 == child1type && (short) 2 != child2type) {
            return true;
        }
        boolean found1;
        boolean found2;
        Node child;
        if ((short) 2 == child1type) {
            NamedNodeMap children = parent.getAttributes();
            int nNodes = children.getLength();
            found1 = false;
            found2 = false;
            for (int i = 0; i < nNodes; i++) {
                child = children.item(i);
                if (child1 == child || isNodeTheSame(child1, child)) {
                    if (found2) {
                        return false;
                    }
                    found1 = true;
                } else if (child2 == child || isNodeTheSame(child2, child)) {
                    if (found1) {
                        return true;
                    }
                    found2 = true;
                }
            }
            return false;
        }
        child = parent.getFirstChild();
        found1 = false;
        found2 = false;
        while (child != null) {
            if (child1 == child || isNodeTheSame(child1, child)) {
                if (found2) {
                    return false;
                }
                found1 = true;
            } else if (child2 == child || isNodeTheSame(child2, child)) {
                if (found1) {
                    return true;
                }
                found2 = true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    public short getLevel(Node n) {
        short level = (short) 1;
        while (true) {
            n = getParentOfNode(n);
            if (n == null) {
                return level;
            }
            level = (short) (level + 1);
        }
    }

    public String getNamespaceForPrefix(String prefix, Element namespaceContext) {
        Node parent = namespaceContext;
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if (prefix.equals("xmlns")) {
            return SerializerConstants.XMLNS_URI;
        }
        String declname;
        if (prefix == "") {
            declname = "xmlns";
        } else {
            declname = Constants.ATTRNAME_XMLNS + prefix;
        }
        while (parent != null) {
            int type = parent.getNodeType();
            if (type != 1 && type != 5) {
                return null;
            }
            if (type == 1) {
                Attr attr = ((Element) parent).getAttributeNode(declname);
                if (attr != null) {
                    return attr.getNodeValue();
                }
            }
            parent = getParentOfNode(parent);
        }
        return null;
    }

    public String getNamespaceOfNode(Node n) {
        NSInfo nsInfo;
        boolean hasProcessedNS;
        String namespaceOfPrefix;
        short ntype = n.getNodeType();
        if ((short) 2 != ntype) {
            NSInfo nsObj = this.m_NSInfos.get(n);
            nsInfo = nsObj == null ? null : nsObj;
            hasProcessedNS = nsInfo == null ? false : nsInfo.m_hasProcessedNS;
        } else {
            hasProcessedNS = false;
            nsInfo = null;
        }
        if (hasProcessedNS) {
            namespaceOfPrefix = nsInfo.m_namespace;
        } else {
            String prefix;
            namespaceOfPrefix = null;
            String nodeName = n.getNodeName();
            int indexOfNSSep = nodeName.indexOf(58);
            if ((short) 2 != ntype) {
                prefix = indexOfNSSep >= 0 ? nodeName.substring(0, indexOfNSSep) : "";
            } else if (indexOfNSSep <= 0) {
                return null;
            } else {
                prefix = nodeName.substring(0, indexOfNSSep);
            }
            boolean ancestorsHaveXMLNS = false;
            boolean nHasXMLNS = false;
            if (prefix.equals("xml")) {
                namespaceOfPrefix = "http://www.w3.org/XML/1998/namespace";
            } else {
                int i;
                Node parent = n;
                while (parent != null && namespaceOfPrefix == null && (nsInfo == null || nsInfo.m_ancestorHasXMLNSAttrs != 2)) {
                    int parentType = parent.getNodeType();
                    if (nsInfo == null || nsInfo.m_hasXMLNSAttrs) {
                        boolean elementHasXMLNS = false;
                        if (parentType == 1) {
                            NamedNodeMap nnm = parent.getAttributes();
                            for (i = 0; i < nnm.getLength(); i++) {
                                Node attr = nnm.item(i);
                                String aname = attr.getNodeName();
                                if (aname.charAt(0) == 'x') {
                                    boolean isPrefix = aname.startsWith(Constants.ATTRNAME_XMLNS);
                                    if (aname.equals("xmlns") || isPrefix) {
                                        if (n == parent) {
                                            nHasXMLNS = true;
                                        }
                                        elementHasXMLNS = true;
                                        ancestorsHaveXMLNS = true;
                                        if ((isPrefix ? aname.substring(6) : "").equals(prefix)) {
                                            namespaceOfPrefix = attr.getNodeValue();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (!(2 == parentType || nsInfo != null || n == parent)) {
                            nsInfo = elementHasXMLNS ? m_NSInfoUnProcWithXMLNS : m_NSInfoUnProcWithoutXMLNS;
                            this.m_NSInfos.put(parent, nsInfo);
                        }
                    }
                    if (2 == parentType) {
                        parent = getParentOfNode(parent);
                    } else {
                        this.m_candidateNoAncestorXMLNS.addElement(parent);
                        this.m_candidateNoAncestorXMLNS.addElement(nsInfo);
                        parent = parent.getParentNode();
                    }
                    if (parent != null) {
                        Object nsObj2 = this.m_NSInfos.get(parent);
                        nsInfo = nsObj2 == null ? null : (NSInfo) nsObj2;
                    }
                }
                int nCandidates = this.m_candidateNoAncestorXMLNS.size();
                if (nCandidates > 0) {
                    if (!ancestorsHaveXMLNS && parent == null) {
                        for (i = 0; i < nCandidates; i += 2) {
                            NSInfo candidateInfo = this.m_candidateNoAncestorXMLNS.elementAt(i + 1);
                            if (candidateInfo == m_NSInfoUnProcWithoutXMLNS) {
                                this.m_NSInfos.put(this.m_candidateNoAncestorXMLNS.elementAt(i), m_NSInfoUnProcNoAncestorXMLNS);
                            } else if (candidateInfo == m_NSInfoNullWithoutXMLNS) {
                                this.m_NSInfos.put(this.m_candidateNoAncestorXMLNS.elementAt(i), m_NSInfoNullNoAncestorXMLNS);
                            }
                        }
                    }
                    this.m_candidateNoAncestorXMLNS.removeAllElements();
                }
            }
            if ((short) 2 != ntype) {
                if (namespaceOfPrefix != null) {
                    this.m_NSInfos.put(n, new NSInfo(namespaceOfPrefix, nHasXMLNS));
                } else if (!ancestorsHaveXMLNS) {
                    this.m_NSInfos.put(n, m_NSInfoNullNoAncestorXMLNS);
                } else if (nHasXMLNS) {
                    this.m_NSInfos.put(n, m_NSInfoNullWithXMLNS);
                } else {
                    this.m_NSInfos.put(n, m_NSInfoNullWithoutXMLNS);
                }
            }
        }
        return namespaceOfPrefix;
    }

    public String getLocalNameOfNode(Node n) {
        String qname = n.getNodeName();
        int index = qname.indexOf(58);
        return index < 0 ? qname : qname.substring(index + 1);
    }

    public String getExpandedElementName(Element elem) {
        String namespace = getNamespaceOfNode(elem);
        if (namespace != null) {
            return namespace + ":" + getLocalNameOfNode(elem);
        }
        return getLocalNameOfNode(elem);
    }

    public String getExpandedAttributeName(Attr attr) {
        String namespace = getNamespaceOfNode(attr);
        if (namespace != null) {
            return namespace + ":" + getLocalNameOfNode(attr);
        }
        return getLocalNameOfNode(attr);
    }

    public boolean isIgnorableWhitespace(Text node) {
        return false;
    }

    public Node getRoot(Node node) {
        Node root = null;
        while (node != null) {
            root = node;
            node = getParentOfNode(node);
        }
        return root;
    }

    public Node getRootNode(Node n) {
        int nt = n.getNodeType();
        if (9 == nt || 11 == nt) {
            return n;
        }
        return n.getOwnerDocument();
    }

    public boolean isNamespaceNode(Node n) {
        if ((short) 2 != n.getNodeType()) {
            return false;
        }
        String attrName = n.getNodeName();
        return !attrName.startsWith(Constants.ATTRNAME_XMLNS) ? attrName.equals("xmlns") : true;
    }

    public static Node getParentOfNode(Node node) throws RuntimeException {
        Node parent;
        if ((short) 2 == node.getNodeType()) {
            Document doc = node.getOwnerDocument();
            DOMImplementation impl = doc.getImplementation();
            if (impl != null && impl.hasFeature("Core", "2.0")) {
                return ((Attr) node).getOwnerElement();
            }
            Element rootElem = doc.getDocumentElement();
            if (rootElem == null) {
                throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT, null));
            }
            parent = locateAttrParent(rootElem, node);
        } else {
            parent = node.getParentNode();
        }
        return parent;
    }

    public Element getElementByID(String id, Document doc) {
        return null;
    }

    public String getUnparsedEntityURI(String name, Document doc) {
        String url = "";
        DocumentType doctype = doc.getDoctype();
        if (doctype != null) {
            NamedNodeMap entities = doctype.getEntities();
            if (entities == null) {
                return url;
            }
            Entity entity = (Entity) entities.getNamedItem(name);
            if (entity == null) {
                return url;
            }
            if (entity.getNotationName() != null) {
                url = entity.getSystemId();
                if (url == null) {
                    url = entity.getPublicId();
                }
            }
        }
        return url;
    }

    private static Node locateAttrParent(Element elem, Node attr) {
        Node parent = null;
        if (elem.getAttributeNode(attr.getNodeName()) == attr) {
            parent = elem;
        }
        if (parent == null) {
            for (Node node = elem.getFirstChild(); node != null; node = node.getNextSibling()) {
                if ((short) 1 == node.getNodeType()) {
                    parent = locateAttrParent((Element) node, attr);
                    if (parent != null) {
                        break;
                    }
                }
            }
        }
        return parent;
    }

    public void setDOMFactory(Document domFactory) {
        this.m_DOMFactory = domFactory;
    }

    public Document getDOMFactory() {
        if (this.m_DOMFactory == null) {
            this.m_DOMFactory = createDocument();
        }
        return this.m_DOMFactory;
    }

    public static String getNodeData(Node node) {
        FastStringBuffer buf = StringBufferPool.get();
        try {
            getNodeData(node, buf);
            String s = buf.length() > 0 ? buf.toString() : "";
            StringBufferPool.free(buf);
            return s;
        } catch (Throwable th) {
            StringBufferPool.free(buf);
        }
    }

    public static void getNodeData(Node node, FastStringBuffer buf) {
        switch (node.getNodeType()) {
            case (short) 1:
            case (short) 9:
            case (short) 11:
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    getNodeData(child, buf);
                }
                return;
            case (short) 2:
                buf.append(node.getNodeValue());
                return;
            case (short) 3:
            case (short) 4:
                buf.append(node.getNodeValue());
                return;
            default:
                return;
        }
    }
}

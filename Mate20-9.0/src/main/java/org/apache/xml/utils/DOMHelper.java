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

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0077  */
    public static boolean isNodeAfter(Node node1, Node node2) {
        boolean z = true;
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
            if (nParents1 < nParents2) {
                int adjust = nParents2 - nParents1;
                Node startNode22 = startNode2;
                for (int i = 0; i < adjust; i++) {
                    startNode22 = getParentOfNode(startNode22);
                }
                startNode2 = startNode22;
            } else if (nParents1 > nParents2) {
                int adjust2 = nParents1 - nParents2;
                Node startNode12 = startNode1;
                for (int i2 = 0; i2 < adjust2; i2++) {
                    startNode12 = getParentOfNode(startNode12);
                }
                startNode1 = startNode12;
            }
            Node prevChild1 = null;
            Node prevChild2 = null;
            while (true) {
                if (startNode1 == null) {
                    break;
                } else if (startNode1 != startNode2 && !isNodeTheSame(startNode1, startNode2)) {
                    prevChild1 = startNode1;
                    startNode1 = getParentOfNode(startNode1);
                    prevChild2 = startNode2;
                    startNode2 = getParentOfNode(startNode2);
                } else if (prevChild1 != null) {
                    if (nParents1 >= nParents2) {
                        z = false;
                    }
                    isNodeAfter = z;
                } else {
                    isNodeAfter = isNodeAfterSibling(startNode1, prevChild1, prevChild2);
                }
            }
            if (prevChild1 != null) {
            }
        } else if (parent1 != null) {
            isNodeAfter = isNodeAfterSibling(parent1, node1, node2);
        }
        return isNodeAfter;
    }

    public static boolean isNodeTheSame(Node node1, Node node2) {
        if ((node1 instanceof DTMNodeProxy) && (node2 instanceof DTMNodeProxy)) {
            return ((DTMNodeProxy) node1).equals((Node) (DTMNodeProxy) node2);
        }
        return node1 == node2;
    }

    private static boolean isNodeAfterSibling(Node parent, Node child1, Node child2) {
        short child1type = child1.getNodeType();
        short child2type = child2.getNodeType();
        if (2 != child1type && 2 == child2type) {
            return false;
        }
        if (2 == child1type && 2 != child2type) {
            return true;
        }
        int i = 0;
        if (2 == child1type) {
            NamedNodeMap children = parent.getAttributes();
            int nNodes = children.getLength();
            boolean found1 = false;
            boolean found2 = false;
            while (i < nNodes) {
                Node child = children.item(i);
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
                i++;
            }
            return false;
        }
        boolean found12 = false;
        for (Node child3 = parent.getFirstChild(); child3 != null; child3 = child3.getNextSibling()) {
            if (child1 == child3 || isNodeTheSame(child1, child3)) {
                if (i != 0) {
                    return false;
                }
                found12 = true;
            } else if (child2 == child3 || isNodeTheSame(child2, child3)) {
                if (found12) {
                    return true;
                }
                i = 1;
            }
        }
        return false;
    }

    public short getLevel(Node n) {
        short level = 1;
        while (true) {
            Node parentOfNode = getParentOfNode(n);
            n = parentOfNode;
            if (parentOfNode == null) {
                return level;
            }
            level = (short) (level + 1);
        }
    }

    public String getNamespaceForPrefix(String prefix, Element namespaceContext) {
        String declname;
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if (prefix.equals("xmlns")) {
            return SerializerConstants.XMLNS_URI;
        }
        if (prefix == "") {
            declname = "xmlns";
        } else {
            declname = Constants.ATTRNAME_XMLNS + prefix;
        }
        for (Node parent = namespaceContext; parent != null && 0 == 0; parent = getParentOfNode(parent)) {
            int nodeType = parent.getNodeType();
            int type = nodeType;
            if (nodeType != 1 && type != 5) {
                return null;
            }
            if (type == 1) {
                Attr attr = ((Element) parent).getAttributeNode(declname);
                if (attr != null) {
                    return attr.getNodeValue();
                }
            }
        }
        return null;
    }

    public String getNamespaceOfNode(Node n) {
        NSInfo nsInfo;
        boolean hasProcessedNS;
        String namespaceOfPrefix;
        String prefix;
        String namespaceOfPrefix2;
        boolean hasProcessedNS2;
        Node parent;
        String namespaceOfPrefix3;
        String namespaceOfPrefix4;
        boolean ancestorsHaveXMLNS;
        String p;
        Node node = n;
        short ntype = n.getNodeType();
        int i = 0;
        int i2 = 2;
        if (2 != ntype) {
            Object nsObj = this.m_NSInfos.get(node);
            nsInfo = nsObj == null ? null : (NSInfo) nsObj;
            hasProcessedNS = nsInfo == null ? false : nsInfo.m_hasProcessedNS;
        } else {
            hasProcessedNS = false;
            nsInfo = null;
        }
        if (hasProcessedNS) {
            namespaceOfPrefix = nsInfo.m_namespace;
            boolean z = hasProcessedNS;
        } else {
            String namespaceOfPrefix5 = null;
            String nodeName = n.getNodeName();
            int indexOfNSSep = nodeName.indexOf(58);
            if (2 != ntype) {
                prefix = indexOfNSSep >= 0 ? nodeName.substring(0, indexOfNSSep) : "";
            } else if (indexOfNSSep <= 0) {
                return null;
            } else {
                prefix = nodeName.substring(0, indexOfNSSep);
            }
            String nsInfo2 = null;
            boolean ancestorsHaveXMLNS2 = false;
            if (prefix.equals("xml")) {
                namespaceOfPrefix = "http://www.w3.org/XML/1998/namespace";
                boolean z2 = hasProcessedNS;
            } else {
                boolean nHasXMLNS = false;
                String ancestorsHaveXMLNS3 = null;
                NSInfo nsInfo3 = nsInfo;
                Node parent2 = node;
                while (true) {
                    if (parent2 != null && namespaceOfPrefix5 == null) {
                        if (nsInfo3 != null && nsInfo3.m_ancestorHasXMLNSAttrs == i2) {
                            boolean z3 = hasProcessedNS;
                            namespaceOfPrefix2 = namespaceOfPrefix5;
                            break;
                        }
                        int parentType = parent2.getNodeType();
                        if (nsInfo3 == null || nsInfo3.m_hasXMLNSAttrs) {
                            if (parentType == 1) {
                                NamedNodeMap nnm = parent2.getAttributes();
                                namespaceOfPrefix4 = ancestorsHaveXMLNS3;
                                ancestorsHaveXMLNS = false;
                                int i3 = i;
                                while (true) {
                                    if (i3 >= nnm.getLength()) {
                                        hasProcessedNS2 = hasProcessedNS;
                                        namespaceOfPrefix3 = namespaceOfPrefix5;
                                        break;
                                    }
                                    Node attr = nnm.item(i3);
                                    NamedNodeMap nnm2 = nnm;
                                    String aname = attr.getNodeName();
                                    hasProcessedNS2 = hasProcessedNS;
                                    String namespaceOfPrefix6 = namespaceOfPrefix5;
                                    if (aname.charAt(0) == 'x') {
                                        boolean isPrefix = aname.startsWith(Constants.ATTRNAME_XMLNS);
                                        if (aname.equals("xmlns") || isPrefix) {
                                            if (node == parent2) {
                                                nHasXMLNS = true;
                                            }
                                            if (isPrefix) {
                                                boolean z4 = isPrefix;
                                                p = aname.substring(6);
                                            } else {
                                                p = "";
                                            }
                                            if (p.equals(prefix)) {
                                                namespaceOfPrefix3 = attr.getNodeValue();
                                                namespaceOfPrefix4 = 1;
                                                ancestorsHaveXMLNS = true;
                                                break;
                                            }
                                            namespaceOfPrefix4 = 1;
                                            ancestorsHaveXMLNS = true;
                                        }
                                    }
                                    i3++;
                                    nnm = nnm2;
                                    hasProcessedNS = hasProcessedNS2;
                                    namespaceOfPrefix5 = namespaceOfPrefix6;
                                }
                            } else {
                                hasProcessedNS2 = hasProcessedNS;
                                namespaceOfPrefix3 = namespaceOfPrefix5;
                                namespaceOfPrefix4 = ancestorsHaveXMLNS3;
                                ancestorsHaveXMLNS = false;
                            }
                            if (!(2 == parentType || nsInfo3 != null || node == parent2)) {
                                NSInfo nsInfo4 = ancestorsHaveXMLNS ? m_NSInfoUnProcWithXMLNS : m_NSInfoUnProcWithoutXMLNS;
                                this.m_NSInfos.put(parent2, nsInfo4);
                                nsInfo3 = nsInfo4;
                            }
                            ancestorsHaveXMLNS3 = namespaceOfPrefix4;
                            namespaceOfPrefix5 = namespaceOfPrefix3;
                        } else {
                            hasProcessedNS2 = hasProcessedNS;
                        }
                        if (2 == parentType) {
                            parent = getParentOfNode(parent2);
                        } else {
                            this.m_candidateNoAncestorXMLNS.addElement(parent2);
                            this.m_candidateNoAncestorXMLNS.addElement(nsInfo3);
                            parent = parent2.getParentNode();
                        }
                        parent2 = parent;
                        if (parent2 != null) {
                            Object nsObj2 = this.m_NSInfos.get(parent2);
                            nsInfo3 = nsObj2 == null ? null : (NSInfo) nsObj2;
                        }
                        hasProcessedNS = hasProcessedNS2;
                        i = 0;
                        i2 = 2;
                    } else {
                        boolean z5 = hasProcessedNS;
                        namespaceOfPrefix2 = namespaceOfPrefix5;
                    }
                }
                int nCandidates = this.m_candidateNoAncestorXMLNS.size();
                if (nCandidates > 0) {
                    if (ancestorsHaveXMLNS3 == null && parent2 == null) {
                        int i4 = 0;
                        while (true) {
                            int i5 = i4;
                            if (i5 >= nCandidates) {
                                break;
                            }
                            Object candidateInfo = this.m_candidateNoAncestorXMLNS.elementAt(i5 + 1);
                            if (candidateInfo == m_NSInfoUnProcWithoutXMLNS) {
                                this.m_NSInfos.put(this.m_candidateNoAncestorXMLNS.elementAt(i5), m_NSInfoUnProcNoAncestorXMLNS);
                            } else if (candidateInfo == m_NSInfoNullWithoutXMLNS) {
                                this.m_NSInfos.put(this.m_candidateNoAncestorXMLNS.elementAt(i5), m_NSInfoNullNoAncestorXMLNS);
                            }
                            i4 = i5 + 2;
                        }
                    }
                    this.m_candidateNoAncestorXMLNS.removeAllElements();
                }
                NSInfo nSInfo = nsInfo3;
                nsInfo2 = ancestorsHaveXMLNS3;
                ancestorsHaveXMLNS2 = nHasXMLNS;
                namespaceOfPrefix = namespaceOfPrefix2;
            }
            if (2 != ntype) {
                if (namespaceOfPrefix != null) {
                    this.m_NSInfos.put(node, new NSInfo(namespaceOfPrefix, ancestorsHaveXMLNS2));
                } else if (nsInfo2 == null) {
                    this.m_NSInfos.put(node, m_NSInfoNullNoAncestorXMLNS);
                } else if (ancestorsHaveXMLNS2) {
                    this.m_NSInfos.put(node, m_NSInfoNullWithXMLNS);
                } else {
                    this.m_NSInfos.put(node, m_NSInfoNullWithoutXMLNS);
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
        if (namespace == null) {
            return getLocalNameOfNode(elem);
        }
        return namespace + ":" + getLocalNameOfNode(elem);
    }

    public String getExpandedAttributeName(Attr attr) {
        String namespace = getNamespaceOfNode(attr);
        if (namespace == null) {
            return getLocalNameOfNode(attr);
        }
        return namespace + ":" + getLocalNameOfNode(attr);
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
        boolean z = false;
        if (2 != n.getNodeType()) {
            return false;
        }
        String attrName = n.getNodeName();
        if (attrName.startsWith(Constants.ATTRNAME_XMLNS) || attrName.equals("xmlns")) {
            z = true;
        }
        return z;
    }

    public static Node getParentOfNode(Node node) throws RuntimeException {
        Node parent;
        if (2 == node.getNodeType()) {
            Document doc = node.getOwnerDocument();
            DOMImplementation impl = doc.getImplementation();
            if (impl != null && impl.hasFeature("Core", "2.0")) {
                return ((Attr) node).getOwnerElement();
            }
            Element rootElem = doc.getDocumentElement();
            if (rootElem != null) {
                parent = locateAttrParent(rootElem, node);
            } else {
                throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CHILD_HAS_NO_OWNER_DOCUMENT_ELEMENT, null));
            }
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
                if (1 == node.getNodeType()) {
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
            return buf.length() > 0 ? buf.toString() : "";
        } finally {
            StringBufferPool.free(buf);
        }
    }

    public static void getNodeData(Node node, FastStringBuffer buf) {
        short nodeType = node.getNodeType();
        if (nodeType != 7) {
            if (!(nodeType == 9 || nodeType == 11)) {
                switch (nodeType) {
                    case 1:
                        break;
                    case 2:
                        buf.append(node.getNodeValue());
                        return;
                    case 3:
                    case 4:
                        buf.append(node.getNodeValue());
                        return;
                    default:
                        return;
                }
            }
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                getNodeData(child, buf);
            }
        }
    }
}

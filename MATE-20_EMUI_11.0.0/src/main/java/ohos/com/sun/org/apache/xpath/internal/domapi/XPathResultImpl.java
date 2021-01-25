package ohos.com.sun.org.apache.xpath.internal.domapi;

import ohos.com.sun.org.apache.xerces.internal.dom.events.MutationEventImpl;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.res.XPATHMessages;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.events.Event;
import ohos.org.w3c.dom.events.EventListener;
import ohos.org.w3c.dom.events.EventTarget;
import ohos.org.w3c.dom.traversal.NodeIterator;
import ohos.org.w3c.dom.xpath.XPathException;
import ohos.org.w3c.dom.xpath.XPathResult;

class XPathResultImpl implements XPathResult, EventListener {
    private final Node m_contextNode;
    private boolean m_isInvalidIteratorState = false;
    private NodeIterator m_iterator = null;
    private NodeList m_list = null;
    private final XObject m_resultObj;
    private final short m_resultType;
    private final XPath m_xpath;

    private String getTypeString(int i) {
        switch (i) {
            case 0:
                return "ANY_TYPE";
            case 1:
                return "NUMBER_TYPE";
            case 2:
                return "STRING_TYPE";
            case 3:
                return "BOOLEAN";
            case 4:
                return "UNORDERED_NODE_ITERATOR_TYPE";
            case 5:
                return "ORDERED_NODE_ITERATOR_TYPE";
            case 6:
                return "UNORDERED_NODE_SNAPSHOT_TYPE";
            case 7:
                return "ORDERED_NODE_SNAPSHOT_TYPE";
            case 8:
                return "ANY_UNORDERED_NODE_TYPE";
            case 9:
                return "FIRST_ORDERED_NODE_TYPE";
            default:
                return "#UNKNOWN";
        }
    }

    static boolean isValidType(short s) {
        switch (s) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return true;
            default:
                return false;
        }
    }

    XPathResultImpl(short s, XObject xObject, Node node, XPath xPath) {
        if (!isValidType(s)) {
            throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_INVALID_XPATH_TYPE", new Object[]{new Integer(s)}));
        } else if (xObject != null) {
            this.m_resultObj = xObject;
            this.m_contextNode = node;
            this.m_xpath = xPath;
            if (s == 0) {
                this.m_resultType = getTypeFromXObject(xObject);
            } else {
                this.m_resultType = s;
            }
            short s2 = this.m_resultType;
            if (s2 == 5 || s2 == 4) {
                addEventListener();
            }
            short s3 = this.m_resultType;
            if (s3 == 5 || s3 == 4 || s3 == 8 || s3 == 9) {
                try {
                    this.m_iterator = this.m_resultObj.nodeset();
                } catch (TransformerException unused) {
                    throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_INCOMPATIBLE_TYPES", new Object[]{this.m_xpath.getPatternString(), getTypeString(getTypeFromXObject(this.m_resultObj)), getTypeString(this.m_resultType)}));
                }
            } else if (s3 == 6 || s3 == 7) {
                try {
                    this.m_list = this.m_resultObj.nodelist();
                } catch (TransformerException unused2) {
                    throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_INCOMPATIBLE_TYPES", new Object[]{this.m_xpath.getPatternString(), getTypeString(getTypeFromXObject(this.m_resultObj)), getTypeString(this.m_resultType)}));
                }
            }
        } else {
            throw new XPathException(1, XPATHMessages.createXPATHMessage("ER_EMPTY_XPATH_RESULT", null));
        }
    }

    public short getResultType() {
        return this.m_resultType;
    }

    public double getNumberValue() throws XPathException {
        if (getResultType() == 1) {
            try {
                return this.m_resultObj.num();
            } catch (Exception e) {
                throw new XPathException(2, e.getMessage());
            }
        } else {
            throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_CANT_CONVERT_XPATHRESULTTYPE_TO_NUMBER", new Object[]{this.m_xpath.getPatternString(), getTypeString(this.m_resultType)}));
        }
    }

    public String getStringValue() throws XPathException {
        if (getResultType() == 2) {
            try {
                return this.m_resultObj.str();
            } catch (Exception e) {
                throw new XPathException(2, e.getMessage());
            }
        } else {
            throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_CANT_CONVERT_TO_STRING", new Object[]{this.m_xpath.getPatternString(), this.m_resultObj.getTypeString()}));
        }
    }

    public boolean getBooleanValue() throws XPathException {
        if (getResultType() == 3) {
            try {
                return this.m_resultObj.bool();
            } catch (TransformerException e) {
                throw new XPathException(2, e.getMessage());
            }
        } else {
            throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_CANT_CONVERT_TO_BOOLEAN", new Object[]{this.m_xpath.getPatternString(), getTypeString(this.m_resultType)}));
        }
    }

    public Node getSingleNodeValue() throws XPathException {
        short s = this.m_resultType;
        if (s == 8 || s == 9) {
            try {
                NodeIterator nodeset = this.m_resultObj.nodeset();
                if (nodeset == null) {
                    return null;
                }
                Node nextNode = nodeset.nextNode();
                return isNamespaceNode(nextNode) ? new XPathNamespaceImpl(nextNode) : nextNode;
            } catch (TransformerException e) {
                throw new XPathException(2, e.getMessage());
            }
        } else {
            throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_CANT_CONVERT_TO_SINGLENODE", new Object[]{this.m_xpath.getPatternString(), getTypeString(this.m_resultType)}));
        }
    }

    public boolean getInvalidIteratorState() {
        return this.m_isInvalidIteratorState;
    }

    public int getSnapshotLength() throws XPathException {
        short s = this.m_resultType;
        if (s == 6 || s == 7) {
            return this.m_list.getLength();
        }
        throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_CANT_GET_SNAPSHOT_LENGTH", new Object[]{this.m_xpath.getPatternString(), getTypeString(this.m_resultType)}));
    }

    public Node iterateNext() throws XPathException, DOMException {
        short s = this.m_resultType;
        if (s != 4 && s != 5) {
            throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_NON_ITERATOR_TYPE", new Object[]{this.m_xpath.getPatternString(), getTypeString(this.m_resultType)}));
        } else if (!getInvalidIteratorState()) {
            Node nextNode = this.m_iterator.nextNode();
            if (nextNode == null) {
                removeEventListener();
            }
            return isNamespaceNode(nextNode) ? new XPathNamespaceImpl(nextNode) : nextNode;
        } else {
            throw new DOMException(11, XPATHMessages.createXPATHMessage("ER_DOC_MUTATED", null));
        }
    }

    public Node snapshotItem(int i) throws XPathException {
        short s = this.m_resultType;
        if (s == 6 || s == 7) {
            Node item = this.m_list.item(i);
            return isNamespaceNode(item) ? new XPathNamespaceImpl(item) : item;
        }
        throw new XPathException(2, XPATHMessages.createXPATHMessage("ER_NON_SNAPSHOT_TYPE", new Object[]{this.m_xpath.getPatternString(), getTypeString(this.m_resultType)}));
    }

    public void handleEvent(Event event) {
        if (event.getType().equals(MutationEventImpl.DOM_SUBTREE_MODIFIED)) {
            this.m_isInvalidIteratorState = true;
            removeEventListener();
        }
    }

    private short getTypeFromXObject(XObject xObject) {
        int type = xObject.getType();
        if (type == -1) {
            return 0;
        }
        if (type == 1) {
            return 3;
        }
        if (type == 2) {
            return 1;
        }
        if (type != 3) {
            return (type == 4 || type == 5) ? (short) 4 : 0;
        }
        return 2;
    }

    private boolean isNamespaceNode(Node node) {
        if (node == null || node.getNodeType() != 2) {
            return false;
        }
        return node.getNodeName().startsWith("xmlns:") || node.getNodeName().equals("xmlns");
    }

    private void addEventListener() {
        EventTarget eventTarget = this.m_contextNode;
        if (eventTarget instanceof EventTarget) {
            eventTarget.addEventListener(MutationEventImpl.DOM_SUBTREE_MODIFIED, this, true);
        }
    }

    private void removeEventListener() {
        EventTarget eventTarget = this.m_contextNode;
        if (eventTarget instanceof EventTarget) {
            eventTarget.removeEventListener(MutationEventImpl.DOM_SUBTREE_MODIFIED, this, true);
        }
    }
}

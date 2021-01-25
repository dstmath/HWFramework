package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.Vector;
import ohos.org.w3c.dom.CharacterData;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ranges.Range;
import ohos.org.w3c.dom.ranges.RangeException;

public class RangeImpl implements Range {
    static final int CLONE_CONTENTS = 2;
    static final int DELETE_CONTENTS = 3;
    static final int EXTRACT_CONTENTS = 1;
    Node fDeleteNode = null;
    boolean fDetach = false;
    DocumentImpl fDocument;
    Node fEndContainer;
    int fEndOffset;
    Node fInsertNode = null;
    boolean fInsertedFromRange = false;
    boolean fIsCollapsed;
    Node fRemoveChild = null;
    Node fSplitNode = null;
    Node fStartContainer;
    int fStartOffset;

    public RangeImpl(DocumentImpl documentImpl) {
        this.fDocument = documentImpl;
        this.fStartContainer = documentImpl;
        this.fEndContainer = documentImpl;
        this.fStartOffset = 0;
        this.fEndOffset = 0;
        this.fDetach = false;
    }

    public Node getStartContainer() {
        if (!this.fDetach) {
            return this.fStartContainer;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public int getStartOffset() {
        if (!this.fDetach) {
            return this.fStartOffset;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public Node getEndContainer() {
        if (!this.fDetach) {
            return this.fEndContainer;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public int getEndOffset() {
        if (!this.fDetach) {
            return this.fEndOffset;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public boolean getCollapsed() {
        if (!this.fDetach) {
            return this.fStartContainer == this.fEndContainer && this.fStartOffset == this.fEndOffset;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public Node getCommonAncestorContainer() {
        Object obj = null;
        if (!this.fDetach) {
            Vector vector = new Vector();
            for (Node node = this.fStartContainer; node != null; node = node.getParentNode()) {
                vector.addElement(node);
            }
            Vector vector2 = new Vector();
            for (Node node2 = this.fEndContainer; node2 != null; node2 = node2.getParentNode()) {
                vector2.addElement(node2);
            }
            int size = vector.size() - 1;
            int size2 = vector2.size() - 1;
            while (size >= 0 && size2 >= 0 && vector.elementAt(size) == vector2.elementAt(size2)) {
                obj = vector.elementAt(size);
                size--;
                size2--;
            }
            return (Node) obj;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setStart(Node node, int i) throws RangeException, DOMException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!isLegalContainer(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        checkIndex(node, i);
        this.fStartContainer = node;
        this.fStartOffset = i;
        if (getCommonAncestorContainer() == null || (this.fStartContainer == this.fEndContainer && this.fEndOffset < this.fStartOffset)) {
            collapse(true);
        }
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setEnd(Node node, int i) throws RangeException, DOMException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!isLegalContainer(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        checkIndex(node, i);
        this.fEndContainer = node;
        this.fEndOffset = i;
        if (getCommonAncestorContainer() == null || (this.fStartContainer == this.fEndContainer && this.fEndOffset < this.fStartOffset)) {
            collapse(false);
        }
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setStartBefore(Node node) throws RangeException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!hasLegalRootContainer(node) || !isLegalContainedNode(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        this.fStartContainer = node.getParentNode();
        int i = 0;
        while (node != null) {
            i++;
            node = node.getPreviousSibling();
        }
        this.fStartOffset = i - 1;
        if (getCommonAncestorContainer() == null || (this.fStartContainer == this.fEndContainer && this.fEndOffset < this.fStartOffset)) {
            collapse(true);
        }
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setStartAfter(Node node) throws RangeException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!hasLegalRootContainer(node) || !isLegalContainedNode(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        this.fStartContainer = node.getParentNode();
        int i = 0;
        while (node != null) {
            i++;
            node = node.getPreviousSibling();
        }
        this.fStartOffset = i;
        if (getCommonAncestorContainer() == null || (this.fStartContainer == this.fEndContainer && this.fEndOffset < this.fStartOffset)) {
            collapse(true);
        }
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setEndBefore(Node node) throws RangeException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!hasLegalRootContainer(node) || !isLegalContainedNode(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        this.fEndContainer = node.getParentNode();
        int i = 0;
        while (node != null) {
            i++;
            node = node.getPreviousSibling();
        }
        this.fEndOffset = i - 1;
        if (getCommonAncestorContainer() == null || (this.fStartContainer == this.fEndContainer && this.fEndOffset < this.fStartOffset)) {
            collapse(false);
        }
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setEndAfter(Node node) throws RangeException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!hasLegalRootContainer(node) || !isLegalContainedNode(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        this.fEndContainer = node.getParentNode();
        int i = 0;
        while (node != null) {
            i++;
            node = node.getPreviousSibling();
        }
        this.fEndOffset = i;
        if (getCommonAncestorContainer() == null || (this.fStartContainer == this.fEndContainer && this.fEndOffset < this.fStartOffset)) {
            collapse(false);
        }
    }

    public void collapse(boolean z) {
        if (this.fDetach) {
            throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
        } else if (z) {
            this.fEndContainer = this.fStartContainer;
            this.fEndOffset = this.fStartOffset;
        } else {
            this.fStartContainer = this.fEndContainer;
            this.fStartOffset = this.fEndOffset;
        }
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void selectNode(Node node) throws RangeException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!isLegalContainer(node.getParentNode()) || !isLegalContainedNode(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            this.fStartContainer = parentNode;
            this.fEndContainer = parentNode;
            int i = 0;
            while (node != null) {
                i++;
                node = node.getPreviousSibling();
            }
            this.fStartOffset = i - 1;
            this.fEndOffset = this.fStartOffset + 1;
        }
    }

    /* JADX WARN: Type inference failed for: r4v2, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void selectNodeContents(Node node) throws RangeException {
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!isLegalContainer(node)) {
                throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
            } else if (!(this.fDocument == node.getOwnerDocument() || this.fDocument == node)) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        this.fStartContainer = node;
        this.fEndContainer = node;
        Node firstChild = node.getFirstChild();
        int i = 0;
        this.fStartOffset = 0;
        if (firstChild == null) {
            this.fEndOffset = 0;
            return;
        }
        while (firstChild != null) {
            i++;
            firstChild = firstChild.getNextSibling();
        }
        this.fEndOffset = i;
    }

    public short compareBoundaryPoints(short s, Range range) throws DOMException {
        int i;
        Node node;
        int i2;
        Node node2;
        if (this.fDocument.errorChecking) {
            if (this.fDetach) {
                throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
            } else if (!((this.fDocument == range.getStartContainer().getOwnerDocument() || this.fDocument == range.getStartContainer() || range.getStartContainer() == null) && (this.fDocument == range.getEndContainer().getOwnerDocument() || this.fDocument == range.getEndContainer() || range.getStartContainer() == null))) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        if (s == 0) {
            node = range.getStartContainer();
            node2 = this.fStartContainer;
            i = range.getStartOffset();
            i2 = this.fStartOffset;
        } else if (s == 1) {
            node = range.getStartContainer();
            node2 = this.fEndContainer;
            i = range.getStartOffset();
            i2 = this.fEndOffset;
        } else if (s == 3) {
            node = range.getEndContainer();
            node2 = this.fStartContainer;
            i = range.getEndOffset();
            i2 = this.fStartOffset;
        } else {
            node = range.getEndContainer();
            node2 = this.fEndContainer;
            i = range.getEndOffset();
            i2 = this.fEndOffset;
        }
        int i3 = 0;
        if (node != node2) {
            Node node3 = node2;
            for (Node parentNode = node2.getParentNode(); parentNode != null; parentNode = parentNode.getParentNode()) {
                if (parentNode != node) {
                    node3 = parentNode;
                } else if (i <= indexOf(node3, node)) {
                    return 1;
                } else {
                    return -1;
                }
            }
            Node node4 = node;
            for (Node parentNode2 = node.getParentNode(); parentNode2 != null; parentNode2 = parentNode2.getParentNode()) {
                if (parentNode2 != node2) {
                    node4 = parentNode2;
                } else if (indexOf(node4, node2) < i2) {
                    return 1;
                } else {
                    return -1;
                }
            }
            for (Node node5 = node; node5 != null; node5 = node5.getParentNode()) {
                i3++;
            }
            for (Node node6 = node2; node6 != null; node6 = node6.getParentNode()) {
                i3--;
            }
            while (i3 > 0) {
                node = node.getParentNode();
                i3--;
            }
            while (i3 < 0) {
                node2 = node2.getParentNode();
                i3++;
            }
            Node parentNode3 = node.getParentNode();
            Node parentNode4 = node2.getParentNode();
            while (true) {
                node = parentNode3;
                node2 = parentNode4;
                if (node == node2) {
                    break;
                }
                parentNode3 = node.getParentNode();
                parentNode4 = node2.getParentNode();
            }
            for (Node nextSibling = node.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
                if (nextSibling == node2) {
                    return 1;
                }
            }
            return -1;
        } else if (i < i2) {
            return 1;
        } else {
            return i == i2 ? (short) 0 : -1;
        }
    }

    public void deleteContents() throws DOMException {
        traverseContents(3);
    }

    public DocumentFragment extractContents() throws DOMException {
        return traverseContents(1);
    }

    public DocumentFragment cloneContents() throws DOMException {
        return traverseContents(2);
    }

    /* JADX WARN: Type inference failed for: r7v3, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void insertNode(Node node) throws DOMException, RangeException {
        int i;
        if (node != null) {
            short nodeType = node.getNodeType();
            if (this.fDocument.errorChecking) {
                if (this.fDetach) {
                    throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
                } else if (this.fDocument != node.getOwnerDocument()) {
                    throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
                } else if (nodeType == 2 || nodeType == 6 || nodeType == 12 || nodeType == 9) {
                    throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
                }
            }
            this.fInsertedFromRange = true;
            if (this.fStartContainer.getNodeType() == 3) {
                Node parentNode = this.fStartContainer.getParentNode();
                int length = parentNode.getChildNodes().getLength();
                TextImpl cloneNode = this.fStartContainer.cloneNode(false);
                cloneNode.setNodeValueInternal(cloneNode.getNodeValue().substring(this.fStartOffset));
                TextImpl textImpl = this.fStartContainer;
                textImpl.setNodeValueInternal(textImpl.getNodeValue().substring(0, this.fStartOffset));
                Node nextSibling = this.fStartContainer.getNextSibling();
                if (nextSibling != null) {
                    parentNode.insertBefore(node, nextSibling);
                    parentNode.insertBefore(cloneNode, nextSibling);
                } else {
                    parentNode.appendChild(node);
                    parentNode.appendChild(cloneNode);
                }
                Node node2 = this.fEndContainer;
                if (node2 == this.fStartContainer) {
                    this.fEndContainer = cloneNode;
                    this.fEndOffset -= this.fStartOffset;
                } else if (node2 == parentNode) {
                    this.fEndOffset += parentNode.getChildNodes().getLength() - length;
                }
                signalSplitData(this.fStartContainer, cloneNode, this.fStartOffset);
            } else {
                Node node3 = this.fEndContainer;
                int length2 = node3 == this.fStartContainer ? node3.getChildNodes().getLength() : 0;
                Node firstChild = this.fStartContainer.getFirstChild();
                for (int i2 = 0; i2 < this.fStartOffset && firstChild != null; i2++) {
                    firstChild = firstChild.getNextSibling();
                }
                if (firstChild != null) {
                    this.fStartContainer.insertBefore(node, firstChild);
                } else {
                    this.fStartContainer.appendChild(node);
                }
                Node node4 = this.fEndContainer;
                if (node4 == this.fStartContainer && (i = this.fEndOffset) != 0) {
                    this.fEndOffset = i + (node4.getChildNodes().getLength() - length2);
                }
            }
            this.fInsertedFromRange = false;
        }
    }

    /* JADX WARN: Type inference failed for: r6v1, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARN: Type inference failed for: r6v3, types: [ohos.com.sun.org.apache.xerces.internal.dom.RangeExceptionImpl, java.lang.Throwable] */
    /* JADX WARNING: Unknown variable types count: 2 */
    public void surroundContents(Node node) throws DOMException, RangeException {
        if (node != null) {
            short nodeType = node.getNodeType();
            if (this.fDocument.errorChecking) {
                if (this.fDetach) {
                    throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
                } else if (nodeType == 2 || nodeType == 6 || nodeType == 12 || nodeType == 10 || nodeType == 9 || nodeType == 11) {
                    throw new RangeExceptionImpl(2, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_NODE_TYPE_ERR", null));
                }
            }
            Node node2 = this.fStartContainer;
            Node node3 = this.fEndContainer;
            if (node2.getNodeType() == 3) {
                node2 = this.fStartContainer.getParentNode();
            }
            if (this.fEndContainer.getNodeType() == 3) {
                node3 = this.fEndContainer.getParentNode();
            }
            if (node2 == node3) {
                DocumentFragment extractContents = extractContents();
                insertNode(node);
                node.appendChild(extractContents);
                selectNode(node);
                return;
            }
            throw new RangeExceptionImpl(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "BAD_BOUNDARYPOINTS_ERR", null));
        }
    }

    public Range cloneRange() {
        if (!this.fDetach) {
            Range createRange = this.fDocument.createRange();
            createRange.setStart(this.fStartContainer, this.fStartOffset);
            createRange.setEnd(this.fEndContainer, this.fEndOffset);
            return createRange;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public String toString() {
        Node node;
        if (!this.fDetach) {
            Node node2 = this.fStartContainer;
            Node node3 = this.fEndContainer;
            StringBuffer stringBuffer = new StringBuffer();
            if (this.fStartContainer.getNodeType() == 3 || this.fStartContainer.getNodeType() == 4) {
                Node node4 = this.fStartContainer;
                if (node4 == this.fEndContainer) {
                    stringBuffer.append(node4.getNodeValue().substring(this.fStartOffset, this.fEndOffset));
                    return stringBuffer.toString();
                }
                stringBuffer.append(node4.getNodeValue().substring(this.fStartOffset));
                node = nextNode(node2, true);
            } else {
                node = node2.getFirstChild();
                if (this.fStartOffset > 0) {
                    Node node5 = node;
                    for (int i = 0; i < this.fStartOffset && node5 != null; i++) {
                        node5 = node5.getNextSibling();
                    }
                    node = node5;
                }
                if (node == null) {
                    node = nextNode(this.fStartContainer, false);
                }
            }
            if (!(this.fEndContainer.getNodeType() == 3 || this.fEndContainer.getNodeType() == 4)) {
                int i2 = this.fEndOffset;
                Node firstChild = this.fEndContainer.getFirstChild();
                while (i2 > 0 && firstChild != null) {
                    i2--;
                    firstChild = firstChild.getNextSibling();
                }
                node3 = firstChild == null ? nextNode(this.fEndContainer, false) : firstChild;
            }
            while (node != node3 && node != null) {
                if (node.getNodeType() == 3 || node.getNodeType() == 4) {
                    stringBuffer.append(node.getNodeValue());
                }
                node = nextNode(node, true);
            }
            if (this.fEndContainer.getNodeType() == 3 || this.fEndContainer.getNodeType() == 4) {
                stringBuffer.append(this.fEndContainer.getNodeValue().substring(0, this.fEndOffset));
            }
            return stringBuffer.toString();
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    public void detach() {
        if (!this.fDetach) {
            this.fDetach = true;
            this.fDocument.removeRange(this);
            return;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    /* access modifiers changed from: package-private */
    public void signalSplitData(Node node, Node node2, int i) {
        this.fSplitNode = node;
        this.fDocument.splitData(node, node2, i);
        this.fSplitNode = null;
    }

    /* access modifiers changed from: package-private */
    public void receiveSplitData(Node node, Node node2, int i) {
        int i2;
        int i3;
        if (node != null && node2 != null && this.fSplitNode != node) {
            Node node3 = this.fStartContainer;
            if (node == node3 && node3.getNodeType() == 3 && (i3 = this.fStartOffset) > i) {
                this.fStartOffset = i3 - i;
                this.fStartContainer = node2;
            }
            Node node4 = this.fEndContainer;
            if (node == node4 && node4.getNodeType() == 3 && (i2 = this.fEndOffset) > i) {
                this.fEndOffset = i2 - i;
                this.fEndContainer = node2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteData(CharacterData characterData, int i, int i2) {
        this.fDeleteNode = characterData;
        characterData.deleteData(i, i2);
        this.fDeleteNode = null;
    }

    /* access modifiers changed from: package-private */
    public void receiveDeletedText(Node node, int i, int i2) {
        if (node != null && this.fDeleteNode != node) {
            Node node2 = this.fStartContainer;
            if (node == node2 && node2.getNodeType() == 3) {
                int i3 = this.fStartOffset;
                int i4 = i + i2;
                if (i3 > i4) {
                    this.fStartOffset = (i3 - i4) + i;
                } else if (i3 > i) {
                    this.fStartOffset = i;
                }
            }
            Node node3 = this.fEndContainer;
            if (node == node3 && node3.getNodeType() == 3) {
                int i5 = this.fEndOffset;
                int i6 = i2 + i;
                if (i5 > i6) {
                    this.fEndOffset = i + (i5 - i6);
                } else if (i5 > i) {
                    this.fEndOffset = i;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void insertData(CharacterData characterData, int i, String str) {
        this.fInsertNode = characterData;
        characterData.insertData(i, str);
        this.fInsertNode = null;
    }

    /* access modifiers changed from: package-private */
    public void receiveInsertedText(Node node, int i, int i2) {
        int i3;
        int i4;
        if (node != null && this.fInsertNode != node) {
            Node node2 = this.fStartContainer;
            if (node == node2 && node2.getNodeType() == 3 && i < (i4 = this.fStartOffset)) {
                this.fStartOffset = i4 + i2;
            }
            Node node3 = this.fEndContainer;
            if (node == node3 && node3.getNodeType() == 3 && i < (i3 = this.fEndOffset)) {
                this.fEndOffset = i3 + i2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void receiveReplacedText(Node node) {
        if (node != null) {
            Node node2 = this.fStartContainer;
            if (node == node2 && node2.getNodeType() == 3) {
                this.fStartOffset = 0;
            }
            Node node3 = this.fEndContainer;
            if (node == node3 && node3.getNodeType() == 3) {
                this.fEndOffset = 0;
            }
        }
    }

    public void insertedNodeFromDOM(Node node) {
        int i;
        int i2;
        if (node != null && this.fInsertNode != node && !this.fInsertedFromRange) {
            Node parentNode = node.getParentNode();
            Node node2 = this.fStartContainer;
            if (parentNode == node2 && indexOf(node, node2) < (i2 = this.fStartOffset)) {
                this.fStartOffset = i2 + 1;
            }
            Node node3 = this.fEndContainer;
            if (parentNode == node3 && indexOf(node, node3) < (i = this.fEndOffset)) {
                this.fEndOffset = i + 1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Node removeChild(Node node, Node node2) {
        this.fRemoveChild = node2;
        Node removeChild = node.removeChild(node2);
        this.fRemoveChild = null;
        return removeChild;
    }

    /* access modifiers changed from: package-private */
    public void removeNode(Node node) {
        int i;
        int i2;
        if (node != null && this.fRemoveChild != node) {
            Node parentNode = node.getParentNode();
            Node node2 = this.fStartContainer;
            if (parentNode == node2 && indexOf(node, node2) < (i2 = this.fStartOffset)) {
                this.fStartOffset = i2 - 1;
            }
            Node node3 = this.fEndContainer;
            if (parentNode == node3 && indexOf(node, node3) < (i = this.fEndOffset)) {
                this.fEndOffset = i - 1;
            }
            if (parentNode != this.fStartContainer || parentNode != this.fEndContainer) {
                if (isAncestorOf(node, this.fStartContainer)) {
                    this.fStartContainer = parentNode;
                    this.fStartOffset = indexOf(node, parentNode);
                }
                if (isAncestorOf(node, this.fEndContainer)) {
                    this.fEndContainer = parentNode;
                    this.fEndOffset = indexOf(node, parentNode);
                }
            }
        }
    }

    private DocumentFragment traverseContents(int i) throws DOMException {
        Node node;
        Node node2 = this.fStartContainer;
        if (node2 == null || (node = this.fEndContainer) == null) {
            return null;
        }
        if (this.fDetach) {
            throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
        } else if (node2 == node) {
            return traverseSameContainer(i);
        } else {
            int i2 = 0;
            Node node3 = node;
            int i3 = 0;
            for (Node parentNode = node.getParentNode(); parentNode != null; parentNode = parentNode.getParentNode()) {
                if (parentNode == this.fStartContainer) {
                    return traverseCommonStartContainer(node3, i);
                }
                i3++;
                node3 = parentNode;
            }
            Node node4 = this.fStartContainer;
            Node parentNode2 = node4.getParentNode();
            while (true) {
                node4 = parentNode2;
                if (node4 == null) {
                    int i4 = i2 - i3;
                    Node node5 = this.fStartContainer;
                    while (i4 > 0) {
                        node5 = node5.getParentNode();
                        i4--;
                    }
                    Node node6 = this.fEndContainer;
                    while (i4 < 0) {
                        node6 = node6.getParentNode();
                        i4++;
                    }
                    Node parentNode3 = node5.getParentNode();
                    Node parentNode4 = node6.getParentNode();
                    while (true) {
                        node5 = parentNode3;
                        node6 = parentNode4;
                        if (node5 == node6) {
                            return traverseCommonAncestors(node5, node6, i);
                        }
                        parentNode3 = node5.getParentNode();
                        parentNode4 = node6.getParentNode();
                    }
                } else if (node4 == this.fEndContainer) {
                    return traverseCommonEndContainer(node4, i);
                } else {
                    i2++;
                    parentNode2 = node4.getParentNode();
                }
            }
        }
    }

    private DocumentFragment traverseSameContainer(int i) {
        DocumentFragment createDocumentFragment = i != 3 ? this.fDocument.createDocumentFragment() : null;
        if (this.fStartOffset == this.fEndOffset) {
            return createDocumentFragment;
        }
        if (this.fStartContainer.getNodeType() == 3) {
            String substring = this.fStartContainer.getNodeValue().substring(this.fStartOffset, this.fEndOffset);
            if (i != 2) {
                int i2 = this.fStartOffset;
                this.fStartContainer.deleteData(i2, this.fEndOffset - i2);
                collapse(true);
            }
            if (i == 3) {
                return null;
            }
            createDocumentFragment.appendChild(this.fDocument.createTextNode(substring));
            return createDocumentFragment;
        }
        Node selectedNode = getSelectedNode(this.fStartContainer, this.fStartOffset);
        int i3 = this.fEndOffset - this.fStartOffset;
        while (i3 > 0) {
            Node nextSibling = selectedNode.getNextSibling();
            Node traverseFullySelected = traverseFullySelected(selectedNode, i);
            if (createDocumentFragment != null) {
                createDocumentFragment.appendChild(traverseFullySelected);
            }
            i3--;
            selectedNode = nextSibling;
        }
        if (i != 2) {
            collapse(true);
        }
        return createDocumentFragment;
    }

    private DocumentFragment traverseCommonStartContainer(Node node, int i) {
        DocumentFragment createDocumentFragment = i != 3 ? this.fDocument.createDocumentFragment() : null;
        Node traverseRightBoundary = traverseRightBoundary(node, i);
        if (createDocumentFragment != null) {
            createDocumentFragment.appendChild(traverseRightBoundary);
        }
        int indexOf = indexOf(node, this.fStartContainer) - this.fStartOffset;
        if (indexOf <= 0) {
            if (i != 2) {
                setEndBefore(node);
                collapse(false);
            }
            return createDocumentFragment;
        }
        Node previousSibling = node.getPreviousSibling();
        while (indexOf > 0) {
            Node previousSibling2 = previousSibling.getPreviousSibling();
            Node traverseFullySelected = traverseFullySelected(previousSibling, i);
            if (createDocumentFragment != null) {
                createDocumentFragment.insertBefore(traverseFullySelected, createDocumentFragment.getFirstChild());
            }
            indexOf--;
            previousSibling = previousSibling2;
        }
        if (i != 2) {
            setEndBefore(node);
            collapse(false);
        }
        return createDocumentFragment;
    }

    private DocumentFragment traverseCommonEndContainer(Node node, int i) {
        DocumentFragment createDocumentFragment = i != 3 ? this.fDocument.createDocumentFragment() : null;
        Node traverseLeftBoundary = traverseLeftBoundary(node, i);
        if (createDocumentFragment != null) {
            createDocumentFragment.appendChild(traverseLeftBoundary);
        }
        int indexOf = this.fEndOffset - (indexOf(node, this.fEndContainer) + 1);
        Node nextSibling = node.getNextSibling();
        while (indexOf > 0) {
            Node nextSibling2 = nextSibling.getNextSibling();
            Node traverseFullySelected = traverseFullySelected(nextSibling, i);
            if (createDocumentFragment != null) {
                createDocumentFragment.appendChild(traverseFullySelected);
            }
            indexOf--;
            nextSibling = nextSibling2;
        }
        if (i != 2) {
            setStartAfter(node);
            collapse(true);
        }
        return createDocumentFragment;
    }

    private DocumentFragment traverseCommonAncestors(Node node, Node node2, int i) {
        DocumentFragment createDocumentFragment = i != 3 ? this.fDocument.createDocumentFragment() : null;
        Node traverseLeftBoundary = traverseLeftBoundary(node, i);
        if (createDocumentFragment != null) {
            createDocumentFragment.appendChild(traverseLeftBoundary);
        }
        Node parentNode = node.getParentNode();
        int indexOf = indexOf(node2, parentNode) - (indexOf(node, parentNode) + 1);
        Node nextSibling = node.getNextSibling();
        while (indexOf > 0) {
            Node nextSibling2 = nextSibling.getNextSibling();
            Node traverseFullySelected = traverseFullySelected(nextSibling, i);
            if (createDocumentFragment != null) {
                createDocumentFragment.appendChild(traverseFullySelected);
            }
            indexOf--;
            nextSibling = nextSibling2;
        }
        Node traverseRightBoundary = traverseRightBoundary(node2, i);
        if (createDocumentFragment != null) {
            createDocumentFragment.appendChild(traverseRightBoundary);
        }
        if (i != 2) {
            setStartAfter(node);
            collapse(true);
        }
        return createDocumentFragment;
    }

    private Node traverseRightBoundary(Node node, int i) {
        Node selectedNode = getSelectedNode(this.fEndContainer, this.fEndOffset - 1);
        boolean z = selectedNode != this.fEndContainer;
        if (selectedNode == node) {
            return traverseNode(selectedNode, z, false, i);
        }
        Node parentNode = selectedNode.getParentNode();
        Node traverseNode = traverseNode(parentNode, false, false, i);
        while (parentNode != null) {
            while (selectedNode != null) {
                Node previousSibling = selectedNode.getPreviousSibling();
                Node traverseNode2 = traverseNode(selectedNode, z, false, i);
                if (i != 3) {
                    traverseNode.insertBefore(traverseNode2, traverseNode.getFirstChild());
                }
                z = true;
                selectedNode = previousSibling;
            }
            if (parentNode == node) {
                return traverseNode;
            }
            selectedNode = parentNode.getPreviousSibling();
            parentNode = parentNode.getParentNode();
            Node traverseNode3 = traverseNode(parentNode, false, false, i);
            if (i != 3) {
                traverseNode3.appendChild(traverseNode);
            }
            traverseNode = traverseNode3;
        }
        return null;
    }

    private Node traverseLeftBoundary(Node node, int i) {
        Node selectedNode = getSelectedNode(getStartContainer(), getStartOffset());
        boolean z = selectedNode != getStartContainer();
        if (selectedNode == node) {
            return traverseNode(selectedNode, z, true, i);
        }
        Node parentNode = selectedNode.getParentNode();
        Node traverseNode = traverseNode(parentNode, false, true, i);
        while (parentNode != null) {
            while (selectedNode != null) {
                Node nextSibling = selectedNode.getNextSibling();
                Node traverseNode2 = traverseNode(selectedNode, z, true, i);
                if (i != 3) {
                    traverseNode.appendChild(traverseNode2);
                }
                z = true;
                selectedNode = nextSibling;
            }
            if (parentNode == node) {
                return traverseNode;
            }
            selectedNode = parentNode.getNextSibling();
            parentNode = parentNode.getParentNode();
            Node traverseNode3 = traverseNode(parentNode, false, true, i);
            if (i != 3) {
                traverseNode3.appendChild(traverseNode);
            }
            traverseNode = traverseNode3;
        }
        return null;
    }

    private Node traverseNode(Node node, boolean z, boolean z2, int i) {
        if (z) {
            return traverseFullySelected(node, i);
        }
        if (node.getNodeType() == 3) {
            return traverseTextNode(node, z2, i);
        }
        return traversePartiallySelected(node, i);
    }

    private Node traverseFullySelected(Node node, int i) {
        if (i != 1) {
            if (i == 2) {
                return node.cloneNode(true);
            }
            if (i != 3) {
                return null;
            }
            node.getParentNode().removeChild(node);
            return null;
        } else if (node.getNodeType() != 10) {
            return node;
        } else {
            throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
        }
    }

    private Node traversePartiallySelected(Node node, int i) {
        if (i == 1 || i == 2) {
            return node.cloneNode(false);
        }
        if (i != 3) {
        }
        return null;
    }

    private Node traverseTextNode(Node node, boolean z, int i) {
        String str;
        String str2;
        String nodeValue = node.getNodeValue();
        if (z) {
            int startOffset = getStartOffset();
            str = nodeValue.substring(startOffset);
            str2 = nodeValue.substring(0, startOffset);
        } else {
            int endOffset = getEndOffset();
            str = nodeValue.substring(0, endOffset);
            str2 = nodeValue.substring(endOffset);
        }
        if (i != 2) {
            node.setNodeValue(str2);
        }
        if (i == 3) {
            return null;
        }
        Node cloneNode = node.cloneNode(false);
        cloneNode.setNodeValue(str);
        return cloneNode;
    }

    /* access modifiers changed from: package-private */
    public void checkIndex(Node node, int i) throws DOMException {
        if (i >= 0) {
            short nodeType = node.getNodeType();
            if (nodeType == 3 || nodeType == 4 || nodeType == 8 || nodeType == 7) {
                if (i > node.getNodeValue().length()) {
                    throw new DOMException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
                }
            } else if (i > node.getChildNodes().getLength()) {
                throw new DOMException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
            }
        } else {
            throw new DOMException(1, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
        }
    }

    private Node getRootContainer(Node node) {
        if (node == null) {
            return null;
        }
        while (node.getParentNode() != null) {
            node = node.getParentNode();
        }
        return node;
    }

    private boolean isLegalContainer(Node node) {
        if (node == null) {
            return false;
        }
        while (node != null) {
            short nodeType = node.getNodeType();
            if (nodeType == 6 || nodeType == 10 || nodeType == 12) {
                return false;
            }
            node = node.getParentNode();
        }
        return true;
    }

    private boolean hasLegalRootContainer(Node node) {
        if (node == null) {
            return false;
        }
        short nodeType = getRootContainer(node).getNodeType();
        if (nodeType == 2 || nodeType == 9 || nodeType == 11) {
            return true;
        }
        return false;
    }

    private boolean isLegalContainedNode(Node node) {
        short nodeType;
        return (node == null || (nodeType = node.getNodeType()) == 2 || nodeType == 6 || nodeType == 9 || nodeType == 11 || nodeType == 12) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public Node nextNode(Node node, boolean z) {
        Node firstChild;
        if (node == null) {
            return null;
        }
        if (z && (firstChild = node.getFirstChild()) != null) {
            return firstChild;
        }
        Node nextSibling = node.getNextSibling();
        if (nextSibling != null) {
            return nextSibling;
        }
        Node parentNode = node.getParentNode();
        while (parentNode != null && parentNode != this.fDocument) {
            Node nextSibling2 = parentNode.getNextSibling();
            if (nextSibling2 != null) {
                return nextSibling2;
            }
            parentNode = parentNode.getParentNode();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isAncestorOf(Node node, Node node2) {
        while (node2 != null) {
            if (node2 == node) {
                return true;
            }
            node2 = node2.getParentNode();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int indexOf(Node node, Node node2) {
        if (node.getParentNode() != node2) {
            return -1;
        }
        int i = 0;
        for (Node firstChild = node2.getFirstChild(); firstChild != node; firstChild = firstChild.getNextSibling()) {
            i++;
        }
        return i;
    }

    private Node getSelectedNode(Node node, int i) {
        if (node.getNodeType() == 3 || i < 0) {
            return node;
        }
        Node firstChild = node.getFirstChild();
        while (firstChild != null && i > 0) {
            i--;
            firstChild = firstChild.getNextSibling();
        }
        return firstChild != null ? firstChild : node;
    }
}

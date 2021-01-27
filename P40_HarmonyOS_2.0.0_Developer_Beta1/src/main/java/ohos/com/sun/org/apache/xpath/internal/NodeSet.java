package ohos.com.sun.org.apache.xpath.internal;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.DOM2Helper;
import ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.traversal.NodeFilter;
import ohos.org.w3c.dom.traversal.NodeIterator;

public class NodeSet implements NodeList, NodeIterator, Cloneable, ContextNodeList {
    private int m_blocksize;
    protected transient boolean m_cacheNodes;
    protected int m_firstFree;
    private transient int m_last;
    Node[] m_map;
    private int m_mapSize;
    protected transient boolean m_mutable;
    protected transient int m_next;

    public void detach() {
    }

    public boolean getExpandEntityReferences() {
        return true;
    }

    public NodeFilter getFilter() {
        return null;
    }

    public Node getRoot() {
        return null;
    }

    public int getWhatToShow() {
        return -17;
    }

    public NodeSet() {
        this.m_next = 0;
        this.m_mutable = true;
        this.m_cacheNodes = true;
        this.m_last = 0;
        this.m_firstFree = 0;
        this.m_blocksize = 32;
        this.m_mapSize = 0;
    }

    public NodeSet(int i) {
        this.m_next = 0;
        this.m_mutable = true;
        this.m_cacheNodes = true;
        this.m_last = 0;
        this.m_firstFree = 0;
        this.m_blocksize = i;
        this.m_mapSize = 0;
    }

    public NodeSet(NodeList nodeList) {
        this(32);
        addNodes(nodeList);
    }

    public NodeSet(NodeSet nodeSet) {
        this(32);
        addNodes((NodeIterator) nodeSet);
    }

    public NodeSet(NodeIterator nodeIterator) {
        this(32);
        addNodes(nodeIterator);
    }

    public NodeSet(Node node) {
        this(32);
        addNode(node);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public NodeIterator cloneWithReset() throws CloneNotSupportedException {
        NodeSet nodeSet = (NodeSet) clone();
        nodeSet.reset();
        return nodeSet;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public void reset() {
        this.m_next = 0;
    }

    public Node nextNode() throws DOMException {
        if (this.m_next >= size()) {
            return null;
        }
        Node elementAt = elementAt(this.m_next);
        this.m_next++;
        return elementAt;
    }

    public Node previousNode() throws DOMException {
        if (this.m_cacheNodes) {
            int i = this.m_next;
            if (i - 1 <= 0) {
                return null;
            }
            this.m_next = i - 1;
            return elementAt(this.m_next);
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_CANNOT_ITERATE", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public boolean isFresh() {
        return this.m_next == 0;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public void runTo(int i) {
        if (!this.m_cacheNodes) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_CANNOT_INDEX", null));
        } else if (i < 0 || this.m_next >= this.m_firstFree) {
            this.m_next = this.m_firstFree - 1;
        } else {
            this.m_next = i;
        }
    }

    public Node item(int i) {
        runTo(i);
        return elementAt(i);
    }

    public int getLength() {
        runTo(-1);
        return size();
    }

    public void addNode(Node node) {
        if (this.m_mutable) {
            addElement(node);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public void insertNode(Node node, int i) {
        if (this.m_mutable) {
            insertElementAt(node, i);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public void removeNode(Node node) {
        if (this.m_mutable) {
            removeElement(node);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public void addNodes(NodeList nodeList) {
        if (!this.m_mutable) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
        } else if (nodeList != null) {
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                Node item = nodeList.item(i);
                if (item != null) {
                    addElement(item);
                }
            }
        }
    }

    public void addNodes(NodeSet nodeSet) {
        if (this.m_mutable) {
            addNodes((NodeIterator) nodeSet);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public void addNodes(NodeIterator nodeIterator) {
        if (!this.m_mutable) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
        } else if (nodeIterator != null) {
            while (true) {
                Node nextNode = nodeIterator.nextNode();
                if (nextNode != null) {
                    addElement(nextNode);
                } else {
                    return;
                }
            }
        }
    }

    public void addNodesInDocOrder(NodeList nodeList, XPathContext xPathContext) {
        if (this.m_mutable) {
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                Node item = nodeList.item(i);
                if (item != null) {
                    addNodeInDocOrder(item, xPathContext);
                }
            }
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public void addNodesInDocOrder(NodeIterator nodeIterator, XPathContext xPathContext) {
        if (this.m_mutable) {
            while (true) {
                Node nextNode = nodeIterator.nextNode();
                if (nextNode != null) {
                    addNodeInDocOrder(nextNode, xPathContext);
                } else {
                    return;
                }
            }
        } else {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
        }
    }

    private boolean addNodesInDocOrder(int i, int i2, int i3, NodeList nodeList, XPathContext xPathContext) {
        if (this.m_mutable) {
            Node item = nodeList.item(i3);
            while (true) {
                if (i2 < i) {
                    break;
                }
                Node elementAt = elementAt(i2);
                if (elementAt == item) {
                    i2 = -2;
                    break;
                } else if (!DOM2Helper.isNodeAfter(item, elementAt)) {
                    insertElementAt(item, i2 + 1);
                    int i4 = i3 - 1;
                    if (i4 > 0 && !addNodesInDocOrder(0, i2, i4, nodeList, xPathContext)) {
                        addNodesInDocOrder(i2, size() - 1, i4, nodeList, xPathContext);
                    }
                } else {
                    i2--;
                }
            }
            if (i2 == -1) {
                insertElementAt(item, 0);
            }
            return false;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public int addNodeInDocOrder(Node node, boolean z, XPathContext xPathContext) {
        if (this.m_mutable) {
            boolean z2 = true;
            if (z) {
                int size = size() - 1;
                while (true) {
                    if (size < 0) {
                        break;
                    }
                    Node elementAt = elementAt(size);
                    if (elementAt == node) {
                        size = -2;
                        break;
                    } else if (!DOM2Helper.isNodeAfter(node, elementAt)) {
                        break;
                    } else {
                        size--;
                    }
                }
                if (size == -2) {
                    return -1;
                }
                int i = size + 1;
                insertElementAt(node, i);
                return i;
            }
            int size2 = size();
            int i2 = 0;
            while (true) {
                if (i2 >= size2) {
                    z2 = false;
                    break;
                } else if (item(i2).equals(node)) {
                    break;
                } else {
                    i2++;
                }
            }
            if (z2) {
                return size2;
            }
            addElement(node);
            return size2;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public int addNodeInDocOrder(Node node, XPathContext xPathContext) {
        if (this.m_mutable) {
            return addNodeInDocOrder(node, true, xPathContext);
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public int getCurrentPos() {
        return this.m_next;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public void setCurrentPos(int i) {
        if (this.m_cacheNodes) {
            this.m_next = i;
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_CANNOT_INDEX", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public Node getCurrentNode() {
        Node node = null;
        if (this.m_cacheNodes) {
            int i = this.m_next;
            if (i < this.m_firstFree) {
                node = elementAt(i);
            }
            this.m_next = i;
            return node;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_CANNOT_INDEX", null));
    }

    public boolean getShouldCacheNodes() {
        return this.m_cacheNodes;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public void setShouldCacheNodes(boolean z) {
        if (isFresh()) {
            this.m_cacheNodes = z;
            this.m_mutable = true;
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_CANNOT_CALL_SETSHOULDCACHENODE", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public int getLast() {
        return this.m_last;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public void setLast(int i) {
        this.m_last = i;
    }

    @Override // java.lang.Object, ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public Object clone() throws CloneNotSupportedException {
        NodeSet nodeSet = (NodeSet) super.clone();
        Node[] nodeArr = this.m_map;
        if (nodeArr != null && nodeArr == nodeSet.m_map) {
            nodeSet.m_map = new Node[nodeArr.length];
            Node[] nodeArr2 = this.m_map;
            System.arraycopy(nodeArr2, 0, nodeSet.m_map, 0, nodeArr2.length);
        }
        return nodeSet;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ContextNodeList
    public int size() {
        return this.m_firstFree;
    }

    public void addElement(Node node) {
        if (this.m_mutable) {
            int i = this.m_firstFree;
            int i2 = i + 1;
            int i3 = this.m_mapSize;
            if (i2 >= i3) {
                Node[] nodeArr = this.m_map;
                if (nodeArr == null) {
                    int i4 = this.m_blocksize;
                    this.m_map = new Node[i4];
                    this.m_mapSize = i4;
                } else {
                    this.m_mapSize = i3 + this.m_blocksize;
                    Node[] nodeArr2 = new Node[this.m_mapSize];
                    System.arraycopy(nodeArr, 0, nodeArr2, 0, i + 1);
                    this.m_map = nodeArr2;
                }
            }
            Node[] nodeArr3 = this.m_map;
            int i5 = this.m_firstFree;
            nodeArr3[i5] = node;
            this.m_firstFree = i5 + 1;
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public final void push(Node node) {
        int i = this.m_firstFree;
        int i2 = i + 1;
        int i3 = this.m_mapSize;
        if (i2 >= i3) {
            Node[] nodeArr = this.m_map;
            if (nodeArr == null) {
                int i4 = this.m_blocksize;
                this.m_map = new Node[i4];
                this.m_mapSize = i4;
            } else {
                this.m_mapSize = i3 + this.m_blocksize;
                Node[] nodeArr2 = new Node[this.m_mapSize];
                System.arraycopy(nodeArr, 0, nodeArr2, 0, i2);
                this.m_map = nodeArr2;
            }
        }
        this.m_map[i] = node;
        this.m_firstFree = i2;
    }

    public final Node pop() {
        this.m_firstFree--;
        Node[] nodeArr = this.m_map;
        int i = this.m_firstFree;
        Node node = nodeArr[i];
        nodeArr[i] = null;
        return node;
    }

    public final Node popAndTop() {
        this.m_firstFree--;
        Node[] nodeArr = this.m_map;
        int i = this.m_firstFree;
        nodeArr[i] = null;
        if (i == 0) {
            return null;
        }
        return nodeArr[i - 1];
    }

    public final void popQuick() {
        this.m_firstFree--;
        this.m_map[this.m_firstFree] = null;
    }

    public final Node peepOrNull() {
        int i;
        Node[] nodeArr = this.m_map;
        if (nodeArr == null || (i = this.m_firstFree) <= 0) {
            return null;
        }
        return nodeArr[i - 1];
    }

    public final void pushPair(Node node, Node node2) {
        Node[] nodeArr = this.m_map;
        if (nodeArr == null) {
            int i = this.m_blocksize;
            this.m_map = new Node[i];
            this.m_mapSize = i;
        } else {
            int i2 = this.m_firstFree;
            int i3 = i2 + 2;
            int i4 = this.m_mapSize;
            if (i3 >= i4) {
                this.m_mapSize = i4 + this.m_blocksize;
                Node[] nodeArr2 = new Node[this.m_mapSize];
                System.arraycopy(nodeArr, 0, nodeArr2, 0, i2);
                this.m_map = nodeArr2;
            }
        }
        Node[] nodeArr3 = this.m_map;
        int i5 = this.m_firstFree;
        nodeArr3[i5] = node;
        nodeArr3[i5 + 1] = node2;
        this.m_firstFree = i5 + 2;
    }

    public final void popPair() {
        this.m_firstFree -= 2;
        Node[] nodeArr = this.m_map;
        int i = this.m_firstFree;
        nodeArr[i] = null;
        nodeArr[i + 1] = null;
    }

    public final void setTail(Node node) {
        this.m_map[this.m_firstFree - 1] = node;
    }

    public final void setTailSub1(Node node) {
        this.m_map[this.m_firstFree - 2] = node;
    }

    public final Node peepTail() {
        return this.m_map[this.m_firstFree - 1];
    }

    public final Node peepTailSub1() {
        return this.m_map[this.m_firstFree - 2];
    }

    public void insertElementAt(Node node, int i) {
        if (this.m_mutable) {
            Node[] nodeArr = this.m_map;
            if (nodeArr == null) {
                int i2 = this.m_blocksize;
                this.m_map = new Node[i2];
                this.m_mapSize = i2;
            } else {
                int i3 = this.m_firstFree;
                int i4 = i3 + 1;
                int i5 = this.m_mapSize;
                if (i4 >= i5) {
                    this.m_mapSize = i5 + this.m_blocksize;
                    Node[] nodeArr2 = new Node[this.m_mapSize];
                    System.arraycopy(nodeArr, 0, nodeArr2, 0, i3 + 1);
                    this.m_map = nodeArr2;
                }
            }
            int i6 = this.m_firstFree;
            if (i <= i6 - 1) {
                Node[] nodeArr3 = this.m_map;
                System.arraycopy(nodeArr3, i, nodeArr3, i + 1, i6 - i);
            }
            this.m_map[i] = node;
            this.m_firstFree++;
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public void appendNodes(NodeSet nodeSet) {
        int size = nodeSet.size();
        Node[] nodeArr = this.m_map;
        if (nodeArr == null) {
            this.m_mapSize = this.m_blocksize + size;
            this.m_map = new Node[this.m_mapSize];
        } else {
            int i = this.m_firstFree;
            int i2 = i + size;
            int i3 = this.m_mapSize;
            if (i2 >= i3) {
                this.m_mapSize = i3 + this.m_blocksize + size;
                Node[] nodeArr2 = new Node[this.m_mapSize];
                System.arraycopy(nodeArr, 0, nodeArr2, 0, i + size);
                this.m_map = nodeArr2;
            }
        }
        System.arraycopy(nodeSet.m_map, 0, this.m_map, this.m_firstFree, size);
        this.m_firstFree += size;
    }

    public void removeAllElements() {
        if (this.m_map != null) {
            for (int i = 0; i < this.m_firstFree; i++) {
                this.m_map[i] = null;
            }
            this.m_firstFree = 0;
        }
    }

    public boolean removeElement(Node node) {
        if (!this.m_mutable) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
        } else if (this.m_map == null) {
            return false;
        } else {
            for (int i = 0; i < this.m_firstFree; i++) {
                Node node2 = this.m_map[i];
                if (node2 != null && node2.equals(node)) {
                    int i2 = this.m_firstFree;
                    if (i < i2 - 1) {
                        Node[] nodeArr = this.m_map;
                        System.arraycopy(nodeArr, i + 1, nodeArr, i, (i2 - i) - 1);
                    }
                    this.m_firstFree--;
                    this.m_map[this.m_firstFree] = null;
                    return true;
                }
            }
            return false;
        }
    }

    public void removeElementAt(int i) {
        Node[] nodeArr = this.m_map;
        if (nodeArr != null) {
            int i2 = this.m_firstFree;
            if (i >= i2) {
                throw new ArrayIndexOutOfBoundsException(i + " >= " + this.m_firstFree);
            } else if (i >= 0) {
                if (i < i2 - 1) {
                    System.arraycopy(nodeArr, i + 1, nodeArr, i, (i2 - i) - 1);
                }
                this.m_firstFree--;
                this.m_map[this.m_firstFree] = null;
            } else {
                throw new ArrayIndexOutOfBoundsException(i);
            }
        }
    }

    public void setElementAt(Node node, int i) {
        if (this.m_mutable) {
            if (this.m_map == null) {
                int i2 = this.m_blocksize;
                this.m_map = new Node[i2];
                this.m_mapSize = i2;
            }
            this.m_map[i] = node;
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESET_NOT_MUTABLE", null));
    }

    public Node elementAt(int i) {
        Node[] nodeArr = this.m_map;
        if (nodeArr == null) {
            return null;
        }
        return nodeArr[i];
    }

    public boolean contains(Node node) {
        runTo(-1);
        if (this.m_map == null) {
            return false;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            Node node2 = this.m_map[i];
            if (node2 != null && node2.equals(node)) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(Node node, int i) {
        runTo(-1);
        if (this.m_map == null) {
            return -1;
        }
        while (i < this.m_firstFree) {
            Node node2 = this.m_map[i];
            if (node2 != null && node2.equals(node)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int indexOf(Node node) {
        runTo(-1);
        if (this.m_map == null) {
            return -1;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            Node node2 = this.m_map[i];
            if (node2 != null && node2.equals(node)) {
                return i;
            }
        }
        return -1;
    }
}

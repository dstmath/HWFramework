package org.apache.xpath;

import org.apache.xml.utils.DOM2Helper;
import org.apache.xpath.axes.ContextNodeList;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

public class NodeSet implements NodeList, NodeIterator, Cloneable, ContextNodeList {
    private int m_blocksize;
    protected transient boolean m_cacheNodes;
    protected int m_firstFree;
    private transient int m_last;
    Node[] m_map;
    private int m_mapSize;
    protected transient boolean m_mutable;
    protected transient int m_next;

    public NodeSet() {
        this.m_next = 0;
        this.m_mutable = true;
        this.m_cacheNodes = true;
        this.m_last = 0;
        this.m_firstFree = 0;
        this.m_blocksize = 32;
        this.m_mapSize = 0;
    }

    public NodeSet(int blocksize) {
        this.m_next = 0;
        this.m_mutable = true;
        this.m_cacheNodes = true;
        this.m_last = 0;
        this.m_firstFree = 0;
        this.m_blocksize = blocksize;
        this.m_mapSize = 0;
    }

    public NodeSet(NodeList nodelist) {
        this(32);
        addNodes(nodelist);
    }

    public NodeSet(NodeSet nodelist) {
        this(32);
        addNodes((NodeIterator) nodelist);
    }

    public NodeSet(NodeIterator ni) {
        this(32);
        addNodes(ni);
    }

    public NodeSet(Node node) {
        this(32);
        addNode(node);
    }

    public Node getRoot() {
        return null;
    }

    public NodeIterator cloneWithReset() throws CloneNotSupportedException {
        NodeSet clone = (NodeSet) clone();
        clone.reset();
        return clone;
    }

    public void reset() {
        this.m_next = 0;
    }

    public int getWhatToShow() {
        return -17;
    }

    public NodeFilter getFilter() {
        return null;
    }

    public boolean getExpandEntityReferences() {
        return true;
    }

    public Node nextNode() throws DOMException {
        if (this.m_next >= size()) {
            return null;
        }
        Node next = elementAt(this.m_next);
        this.m_next++;
        return next;
    }

    public Node previousNode() throws DOMException {
        if (!this.m_cacheNodes) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_ITERATE, null));
        } else if (this.m_next - 1 <= 0) {
            return null;
        } else {
            this.m_next--;
            return elementAt(this.m_next);
        }
    }

    public void detach() {
    }

    public boolean isFresh() {
        return this.m_next == 0;
    }

    public void runTo(int index) {
        if (!this.m_cacheNodes) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_INDEX, null));
        } else if (index < 0 || this.m_next >= this.m_firstFree) {
            this.m_next = this.m_firstFree - 1;
        } else {
            this.m_next = index;
        }
    }

    public Node item(int index) {
        runTo(index);
        return elementAt(index);
    }

    public int getLength() {
        runTo(-1);
        return size();
    }

    public void addNode(Node n) {
        if (this.m_mutable) {
            addElement(n);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public void insertNode(Node n, int pos) {
        if (this.m_mutable) {
            insertElementAt(n, pos);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public void removeNode(Node n) {
        if (this.m_mutable) {
            removeElement(n);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public void addNodes(NodeList nodelist) {
        if (!this.m_mutable) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
        } else if (nodelist != null) {
            int nChildren = nodelist.getLength();
            for (int i = 0; i < nChildren; i++) {
                Node obj = nodelist.item(i);
                if (obj != null) {
                    addElement(obj);
                }
            }
        }
    }

    public void addNodes(NodeSet ns) {
        if (this.m_mutable) {
            addNodes((NodeIterator) ns);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public void addNodes(NodeIterator iterator) {
        if (!this.m_mutable) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
        } else if (iterator != null) {
            while (true) {
                Node obj = iterator.nextNode();
                if (obj != null) {
                    addElement(obj);
                } else {
                    return;
                }
            }
        }
    }

    public void addNodesInDocOrder(NodeList nodelist, XPathContext support) {
        if (this.m_mutable) {
            int nChildren = nodelist.getLength();
            for (int i = 0; i < nChildren; i++) {
                Node node = nodelist.item(i);
                if (node != null) {
                    addNodeInDocOrder(node, support);
                }
            }
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public void addNodesInDocOrder(NodeIterator iterator, XPathContext support) {
        if (this.m_mutable) {
            while (true) {
                Node node = iterator.nextNode();
                if (node != null) {
                    addNodeInDocOrder(node, support);
                } else {
                    return;
                }
            }
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    private boolean addNodesInDocOrder(int start, int end, int testIndex, NodeList nodelist, XPathContext support) {
        if (this.m_mutable) {
            Node node = nodelist.item(testIndex);
            int i = end;
            while (i >= start) {
                Node child = elementAt(i);
                if (child == node) {
                    i = -2;
                    break;
                } else if (DOM2Helper.isNodeAfter(node, child)) {
                    i--;
                } else {
                    insertElementAt(node, i + 1);
                    testIndex--;
                    if (testIndex > 0 && !addNodesInDocOrder(0, i, testIndex, nodelist, support)) {
                        addNodesInDocOrder(i, size() - 1, testIndex, nodelist, support);
                    }
                }
            }
            if (i == -1) {
                insertElementAt(node, 0);
            }
            return false;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public int addNodeInDocOrder(Node node, boolean test, XPathContext support) {
        int i;
        int insertIndex;
        if (!this.m_mutable) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
        } else if (test) {
            i = size() - 1;
            while (i >= 0) {
                Node child = elementAt(i);
                if (child != node) {
                    if (!DOM2Helper.isNodeAfter(node, child)) {
                        break;
                    }
                    i--;
                } else {
                    i = -2;
                    break;
                }
            }
            if (i == -2) {
                return -1;
            }
            insertIndex = i + 1;
            insertElementAt(node, insertIndex);
            return insertIndex;
        } else {
            insertIndex = size();
            boolean foundit = false;
            for (i = 0; i < insertIndex; i++) {
                if (item(i).equals(node)) {
                    foundit = true;
                    break;
                }
            }
            if (foundit) {
                return insertIndex;
            }
            addElement(node);
            return insertIndex;
        }
    }

    public int addNodeInDocOrder(Node node, XPathContext support) {
        if (this.m_mutable) {
            return addNodeInDocOrder(node, true, support);
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public int getCurrentPos() {
        return this.m_next;
    }

    public void setCurrentPos(int i) {
        if (this.m_cacheNodes) {
            this.m_next = i;
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_INDEX, null));
    }

    public Node getCurrentNode() {
        if (this.m_cacheNodes) {
            int saved = this.m_next;
            Node n = this.m_next < this.m_firstFree ? elementAt(this.m_next) : null;
            this.m_next = saved;
            return n;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_CANNOT_INDEX, null));
    }

    public boolean getShouldCacheNodes() {
        return this.m_cacheNodes;
    }

    public void setShouldCacheNodes(boolean b) {
        if (isFresh()) {
            this.m_cacheNodes = b;
            this.m_mutable = true;
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_CANNOT_CALL_SETSHOULDCACHENODE, null));
    }

    public int getLast() {
        return this.m_last;
    }

    public void setLast(int last) {
        this.m_last = last;
    }

    public Object clone() throws CloneNotSupportedException {
        NodeSet clone = (NodeSet) super.clone();
        if (this.m_map != null && this.m_map == clone.m_map) {
            clone.m_map = new Node[this.m_map.length];
            System.arraycopy(this.m_map, 0, clone.m_map, 0, this.m_map.length);
        }
        return clone;
    }

    public int size() {
        return this.m_firstFree;
    }

    public void addElement(Node value) {
        if (this.m_mutable) {
            if (this.m_firstFree + 1 >= this.m_mapSize) {
                if (this.m_map == null) {
                    this.m_map = new Node[this.m_blocksize];
                    this.m_mapSize = this.m_blocksize;
                } else {
                    this.m_mapSize += this.m_blocksize;
                    Node[] newMap = new Node[this.m_mapSize];
                    System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
                    this.m_map = newMap;
                }
            }
            this.m_map[this.m_firstFree] = value;
            this.m_firstFree++;
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public final void push(Node value) {
        int ff = this.m_firstFree;
        if (ff + 1 >= this.m_mapSize) {
            if (this.m_map == null) {
                this.m_map = new Node[this.m_blocksize];
                this.m_mapSize = this.m_blocksize;
            } else {
                this.m_mapSize += this.m_blocksize;
                Node[] newMap = new Node[this.m_mapSize];
                System.arraycopy(this.m_map, 0, newMap, 0, ff + 1);
                this.m_map = newMap;
            }
        }
        this.m_map[ff] = value;
        this.m_firstFree = ff + 1;
    }

    public final Node pop() {
        this.m_firstFree--;
        Node n = this.m_map[this.m_firstFree];
        this.m_map[this.m_firstFree] = null;
        return n;
    }

    public final Node popAndTop() {
        this.m_firstFree--;
        this.m_map[this.m_firstFree] = null;
        if (this.m_firstFree == 0) {
            return null;
        }
        return this.m_map[this.m_firstFree - 1];
    }

    public final void popQuick() {
        this.m_firstFree--;
        this.m_map[this.m_firstFree] = null;
    }

    public final Node peepOrNull() {
        if (this.m_map == null || this.m_firstFree <= 0) {
            return null;
        }
        return this.m_map[this.m_firstFree - 1];
    }

    public final void pushPair(Node v1, Node v2) {
        if (this.m_map == null) {
            this.m_map = new Node[this.m_blocksize];
            this.m_mapSize = this.m_blocksize;
        } else if (this.m_firstFree + 2 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            Node[] newMap = new Node[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree);
            this.m_map = newMap;
        }
        this.m_map[this.m_firstFree] = v1;
        this.m_map[this.m_firstFree + 1] = v2;
        this.m_firstFree += 2;
    }

    public final void popPair() {
        this.m_firstFree -= 2;
        this.m_map[this.m_firstFree] = null;
        this.m_map[this.m_firstFree + 1] = null;
    }

    public final void setTail(Node n) {
        this.m_map[this.m_firstFree - 1] = n;
    }

    public final void setTailSub1(Node n) {
        this.m_map[this.m_firstFree - 2] = n;
    }

    public final Node peepTail() {
        return this.m_map[this.m_firstFree - 1];
    }

    public final Node peepTailSub1() {
        return this.m_map[this.m_firstFree - 2];
    }

    public void insertElementAt(Node value, int at) {
        if (this.m_mutable) {
            if (this.m_map == null) {
                this.m_map = new Node[this.m_blocksize];
                this.m_mapSize = this.m_blocksize;
            } else if (this.m_firstFree + 1 >= this.m_mapSize) {
                this.m_mapSize += this.m_blocksize;
                Node[] newMap = new Node[this.m_mapSize];
                System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
                this.m_map = newMap;
            }
            if (at <= this.m_firstFree - 1) {
                System.arraycopy(this.m_map, at, this.m_map, at + 1, this.m_firstFree - at);
            }
            this.m_map[at] = value;
            this.m_firstFree++;
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public void appendNodes(NodeSet nodes) {
        int nNodes = nodes.size();
        if (this.m_map == null) {
            this.m_mapSize = this.m_blocksize + nNodes;
            this.m_map = new Node[this.m_mapSize];
        } else if (this.m_firstFree + nNodes >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize + nNodes;
            Node[] newMap = new Node[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + nNodes);
            this.m_map = newMap;
        }
        System.arraycopy(nodes.m_map, 0, this.m_map, this.m_firstFree, nNodes);
        this.m_firstFree += nNodes;
    }

    public void removeAllElements() {
        if (this.m_map != null) {
            for (int i = 0; i < this.m_firstFree; i++) {
                this.m_map[i] = null;
            }
            this.m_firstFree = 0;
        }
    }

    public boolean removeElement(Node s) {
        if (!this.m_mutable) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
        } else if (this.m_map == null) {
            return false;
        } else {
            int i = 0;
            while (i < this.m_firstFree) {
                Node node = this.m_map[i];
                if (node == null || !node.equals(s)) {
                    i++;
                } else {
                    if (i < this.m_firstFree - 1) {
                        System.arraycopy(this.m_map, i + 1, this.m_map, i, (this.m_firstFree - i) - 1);
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
        if (this.m_map != null) {
            if (i >= this.m_firstFree) {
                throw new ArrayIndexOutOfBoundsException(i + " >= " + this.m_firstFree);
            } else if (i < 0) {
                throw new ArrayIndexOutOfBoundsException(i);
            } else {
                if (i < this.m_firstFree - 1) {
                    System.arraycopy(this.m_map, i + 1, this.m_map, i, (this.m_firstFree - i) - 1);
                }
                this.m_firstFree--;
                this.m_map[this.m_firstFree] = null;
            }
        }
    }

    public void setElementAt(Node node, int index) {
        if (this.m_mutable) {
            if (this.m_map == null) {
                this.m_map = new Node[this.m_blocksize];
                this.m_mapSize = this.m_blocksize;
            }
            this.m_map[index] = node;
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESET_NOT_MUTABLE, null));
    }

    public Node elementAt(int i) {
        if (this.m_map == null) {
            return null;
        }
        return this.m_map[i];
    }

    public boolean contains(Node s) {
        runTo(-1);
        if (this.m_map == null) {
            return false;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            Node node = this.m_map[i];
            if (node != null && node.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(Node elem, int index) {
        runTo(-1);
        if (this.m_map == null) {
            return -1;
        }
        for (int i = index; i < this.m_firstFree; i++) {
            Node node = this.m_map[i];
            if (node != null && node.equals(elem)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(Node elem) {
        runTo(-1);
        if (this.m_map == null) {
            return -1;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            Node node = this.m_map[i];
            if (node != null && node.equals(elem)) {
                return i;
            }
        }
        return -1;
    }
}

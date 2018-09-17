package org.apache.xpath;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.utils.NodeVector;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

public class NodeSetDTM extends NodeVector implements DTMIterator, Cloneable {
    static final long serialVersionUID = 7686480133331317070L;
    protected transient boolean m_cacheNodes = true;
    private transient int m_last = 0;
    DTMManager m_manager;
    protected transient boolean m_mutable = true;
    protected transient int m_next = 0;
    protected int m_root = -1;

    public NodeSetDTM(DTMManager dtmManager) {
        this.m_manager = dtmManager;
    }

    public NodeSetDTM(int blocksize, int dummy, DTMManager dtmManager) {
        super(blocksize);
        this.m_manager = dtmManager;
    }

    public NodeSetDTM(NodeSetDTM nodelist) {
        this.m_manager = nodelist.getDTMManager();
        this.m_root = nodelist.getRoot();
        addNodes(nodelist);
    }

    public NodeSetDTM(DTMIterator ni) {
        this.m_manager = ni.getDTMManager();
        this.m_root = ni.getRoot();
        addNodes(ni);
    }

    public NodeSetDTM(NodeIterator iterator, XPathContext xctxt) {
        this.m_manager = xctxt.getDTMManager();
        while (true) {
            Node node = iterator.nextNode();
            if (node != null) {
                addNodeInDocOrder(xctxt.getDTMHandleFromNode(node), xctxt);
            } else {
                return;
            }
        }
    }

    public NodeSetDTM(NodeList nodeList, XPathContext xctxt) {
        this.m_manager = xctxt.getDTMManager();
        int n = nodeList.getLength();
        for (int i = 0; i < n; i++) {
            addNode(xctxt.getDTMHandleFromNode(nodeList.item(i)));
        }
    }

    public NodeSetDTM(int node, DTMManager dtmManager) {
        this.m_manager = dtmManager;
        addNode(node);
    }

    public void setEnvironment(Object environment) {
    }

    public int getRoot() {
        if (-1 != this.m_root) {
            return this.m_root;
        }
        if (size() > 0) {
            return item(0);
        }
        return -1;
    }

    public void setRoot(int context, Object environment) {
    }

    public Object clone() throws CloneNotSupportedException {
        return (NodeSetDTM) super.clone();
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        NodeSetDTM clone = (NodeSetDTM) clone();
        clone.reset();
        return clone;
    }

    public void reset() {
        this.m_next = 0;
    }

    public int getWhatToShow() {
        return -17;
    }

    public DTMFilter getFilter() {
        return null;
    }

    public boolean getExpandEntityReferences() {
        return true;
    }

    public DTM getDTM(int nodeHandle) {
        return this.m_manager.getDTM(nodeHandle);
    }

    public DTMManager getDTMManager() {
        return this.m_manager;
    }

    public int nextNode() {
        if (this.m_next >= size()) {
            return -1;
        }
        int next = elementAt(this.m_next);
        this.m_next++;
        return next;
    }

    public int previousNode() {
        if (!this.m_cacheNodes) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_ITERATE, null));
        } else if (this.m_next - 1 <= 0) {
            return -1;
        } else {
            this.m_next--;
            return elementAt(this.m_next);
        }
    }

    public void detach() {
    }

    public void allowDetachToRelease(boolean allowRelease) {
    }

    public boolean isFresh() {
        return this.m_next == 0;
    }

    public void runTo(int index) {
        if (!this.m_cacheNodes) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_INDEX, null));
        } else if (index < 0 || this.m_next >= this.m_firstFree) {
            this.m_next = this.m_firstFree - 1;
        } else {
            this.m_next = index;
        }
    }

    public int item(int index) {
        runTo(index);
        return elementAt(index);
    }

    public int getLength() {
        runTo(-1);
        return size();
    }

    public void addNode(int n) {
        if (this.m_mutable) {
            addElement(n);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void insertNode(int n, int pos) {
        if (this.m_mutable) {
            insertElementAt(n, pos);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void removeNode(int n) {
        if (this.m_mutable) {
            removeElement(n);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void addNodes(DTMIterator iterator) {
        if (!this.m_mutable) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
        } else if (iterator != null) {
            while (true) {
                int obj = iterator.nextNode();
                if (-1 != obj) {
                    addElement(obj);
                } else {
                    return;
                }
            }
        }
    }

    public void addNodesInDocOrder(DTMIterator iterator, XPathContext support) {
        if (this.m_mutable) {
            while (true) {
                int node = iterator.nextNode();
                if (-1 != node) {
                    addNodeInDocOrder(node, support);
                } else {
                    return;
                }
            }
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public int addNodeInDocOrder(int node, boolean test, XPathContext support) {
        int i;
        int insertIndex;
        if (!this.m_mutable) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
        } else if (test) {
            i = size() - 1;
            while (i >= 0) {
                int child = elementAt(i);
                if (child != node) {
                    if (!support.getDTM(node).isNodeAfter(node, child)) {
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
                if (i == node) {
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

    public int addNodeInDocOrder(int node, XPathContext support) {
        if (this.m_mutable) {
            return addNodeInDocOrder(node, true, support);
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public int size() {
        return super.size();
    }

    public void addElement(int value) {
        if (this.m_mutable) {
            super.addElement(value);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void insertElementAt(int value, int at) {
        if (this.m_mutable) {
            super.insertElementAt(value, at);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void appendNodes(NodeVector nodes) {
        if (this.m_mutable) {
            super.appendNodes(nodes);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void removeAllElements() {
        if (this.m_mutable) {
            super.removeAllElements();
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public boolean removeElement(int s) {
        if (this.m_mutable) {
            return super.removeElement(s);
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void removeElementAt(int i) {
        if (this.m_mutable) {
            super.removeElementAt(i);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void setElementAt(int node, int index) {
        if (this.m_mutable) {
            super.setElementAt(node, index);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public void setItem(int node, int index) {
        if (this.m_mutable) {
            super.setElementAt(node, index);
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_NOT_MUTABLE, null));
    }

    public int elementAt(int i) {
        runTo(i);
        return super.elementAt(i);
    }

    public boolean contains(int s) {
        runTo(-1);
        return super.contains(s);
    }

    public int indexOf(int elem, int index) {
        runTo(-1);
        return super.indexOf(elem, index);
    }

    public int indexOf(int elem) {
        runTo(-1);
        return super.indexOf(elem);
    }

    public int getCurrentPos() {
        return this.m_next;
    }

    public void setCurrentPos(int i) {
        if (this.m_cacheNodes) {
            this.m_next = i;
            return;
        }
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_INDEX, null));
    }

    public int getCurrentNode() {
        if (this.m_cacheNodes) {
            int saved = this.m_next;
            int current = this.m_next > 0 ? this.m_next - 1 : this.m_next;
            int n = current < this.m_firstFree ? elementAt(current) : -1;
            this.m_next = saved;
            return n;
        }
        throw new RuntimeException("This NodeSetDTM can not do indexing or counting functions!");
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

    public boolean isMutable() {
        return this.m_mutable;
    }

    public int getLast() {
        return this.m_last;
    }

    public void setLast(int last) {
        this.m_last = last;
    }

    public boolean isDocOrdered() {
        return true;
    }

    public int getAxis() {
        return -1;
    }
}

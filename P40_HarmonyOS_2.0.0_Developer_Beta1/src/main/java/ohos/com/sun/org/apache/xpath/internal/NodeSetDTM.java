package ohos.com.sun.org.apache.xpath.internal;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMFilter;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.utils.NodeVector;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.traversal.NodeIterator;

public class NodeSetDTM extends NodeVector implements DTMIterator, Cloneable {
    static final long serialVersionUID = 7686480133331317070L;
    protected transient boolean m_cacheNodes = true;
    private transient int m_last = 0;
    DTMManager m_manager;
    protected transient boolean m_mutable = true;
    protected transient int m_next = 0;
    protected int m_root = -1;

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void allowDetachToRelease(boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean getExpandEntityReferences() {
        return true;
    }

    public DTMFilter getFilter() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getWhatToShow() {
        return -17;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isDocOrdered() {
        return true;
    }

    public void setEnvironment(Object obj) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
    }

    public NodeSetDTM(DTMManager dTMManager) {
        this.m_manager = dTMManager;
    }

    public NodeSetDTM(int i, int i2, DTMManager dTMManager) {
        super(i);
        this.m_manager = dTMManager;
    }

    public NodeSetDTM(NodeSetDTM nodeSetDTM) {
        this.m_manager = nodeSetDTM.getDTMManager();
        this.m_root = nodeSetDTM.getRoot();
        addNodes(nodeSetDTM);
    }

    public NodeSetDTM(DTMIterator dTMIterator) {
        this.m_manager = dTMIterator.getDTMManager();
        this.m_root = dTMIterator.getRoot();
        addNodes(dTMIterator);
    }

    public NodeSetDTM(NodeIterator nodeIterator, XPathContext xPathContext) {
        this.m_manager = xPathContext.getDTMManager();
        while (true) {
            Node nextNode = nodeIterator.nextNode();
            if (nextNode != null) {
                addNodeInDocOrder(xPathContext.getDTMHandleFromNode(nextNode), xPathContext);
            } else {
                return;
            }
        }
    }

    public NodeSetDTM(NodeList nodeList, XPathContext xPathContext) {
        this.m_manager = xPathContext.getDTMManager();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            addNode(xPathContext.getDTMHandleFromNode(nodeList.item(i)));
        }
    }

    public NodeSetDTM(int i, DTMManager dTMManager) {
        this.m_manager = dTMManager;
        addNode(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getRoot() {
        int i = this.m_root;
        if (-1 != i) {
            return i;
        }
        if (size() > 0) {
            return item(0);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector, java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return (NodeSetDTM) super.clone();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        NodeSetDTM nodeSetDTM = (NodeSetDTM) clone();
        nodeSetDTM.reset();
        return nodeSetDTM;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void reset() {
        this.m_next = 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTM getDTM(int i) {
        return this.m_manager.getDTM(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMManager getDTMManager() {
        return this.m_manager;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        if (this.m_next >= size()) {
            return -1;
        }
        int elementAt = elementAt(this.m_next);
        this.m_next++;
        return elementAt;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int previousNode() {
        if (this.m_cacheNodes) {
            int i = this.m_next;
            if (i - 1 <= 0) {
                return -1;
            }
            this.m_next = i - 1;
            return elementAt(this.m_next);
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_CANNOT_ITERATE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isFresh() {
        return this.m_next == 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void runTo(int i) {
        if (!this.m_cacheNodes) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_CANNOT_INDEX", null));
        } else if (i < 0 || this.m_next >= this.m_firstFree) {
            this.m_next = this.m_firstFree - 1;
        } else {
            this.m_next = i;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int item(int i) {
        runTo(i);
        return elementAt(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getLength() {
        runTo(-1);
        return size();
    }

    public void addNode(int i) {
        if (this.m_mutable) {
            addElement(i);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    public void insertNode(int i, int i2) {
        if (this.m_mutable) {
            insertElementAt(i, i2);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    public void removeNode(int i) {
        if (this.m_mutable) {
            removeElement(i);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    public void addNodes(DTMIterator dTMIterator) {
        if (!this.m_mutable) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
        } else if (dTMIterator != null) {
            while (true) {
                int nextNode = dTMIterator.nextNode();
                if (-1 != nextNode) {
                    addElement(nextNode);
                } else {
                    return;
                }
            }
        }
    }

    public void addNodesInDocOrder(DTMIterator dTMIterator, XPathContext xPathContext) {
        if (this.m_mutable) {
            while (true) {
                int nextNode = dTMIterator.nextNode();
                if (-1 != nextNode) {
                    addNodeInDocOrder(nextNode, xPathContext);
                } else {
                    return;
                }
            }
        } else {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
        }
    }

    public int addNodeInDocOrder(int i, boolean z, XPathContext xPathContext) {
        if (!this.m_mutable) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
        } else if (z) {
            int size = size() - 1;
            while (true) {
                if (size < 0) {
                    break;
                }
                int elementAt = elementAt(size);
                if (elementAt == i) {
                    size = -2;
                    break;
                } else if (!xPathContext.getDTM(i).isNodeAfter(i, elementAt)) {
                    break;
                } else {
                    size--;
                }
            }
            if (size == -2) {
                return -1;
            }
            int i2 = size + 1;
            insertElementAt(i, i2);
            return i2;
        } else {
            int size2 = size();
            boolean z2 = false;
            int i3 = 0;
            while (true) {
                if (i3 >= size2) {
                    break;
                } else if (i3 == i) {
                    z2 = true;
                    break;
                } else {
                    i3++;
                }
            }
            if (z2) {
                return size2;
            }
            addElement(i);
            return size2;
        }
    }

    public int addNodeInDocOrder(int i, XPathContext xPathContext) {
        if (this.m_mutable) {
            return addNodeInDocOrder(i, true, xPathContext);
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public int size() {
        return super.size();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public void addElement(int i) {
        if (this.m_mutable) {
            super.addElement(i);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public void insertElementAt(int i, int i2) {
        if (this.m_mutable) {
            super.insertElementAt(i, i2);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public void appendNodes(NodeVector nodeVector) {
        if (this.m_mutable) {
            super.appendNodes(nodeVector);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public void removeAllElements() {
        if (this.m_mutable) {
            super.removeAllElements();
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public boolean removeElement(int i) {
        if (this.m_mutable) {
            return super.removeElement(i);
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public void removeElementAt(int i) {
        if (this.m_mutable) {
            super.removeElementAt(i);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public void setElementAt(int i, int i2) {
        if (this.m_mutable) {
            super.setElementAt(i, i2);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setItem(int i, int i2) {
        if (this.m_mutable) {
            super.setElementAt(i, i2);
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_NOT_MUTABLE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public int elementAt(int i) {
        runTo(i);
        return super.elementAt(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public boolean contains(int i) {
        runTo(-1);
        return super.contains(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public int indexOf(int i, int i2) {
        runTo(-1);
        return super.indexOf(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.utils.NodeVector
    public int indexOf(int i) {
        runTo(-1);
        return super.indexOf(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getCurrentPos() {
        return this.m_next;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setCurrentPos(int i) {
        if (this.m_cacheNodes) {
            this.m_next = i;
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NODESETDTM_CANNOT_INDEX", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getCurrentNode() {
        if (this.m_cacheNodes) {
            int i = this.m_next;
            int i2 = i > 0 ? i - 1 : i;
            int elementAt = i2 < this.m_firstFree ? elementAt(i2) : -1;
            this.m_next = i;
            return elementAt;
        }
        throw new RuntimeException("This NodeSetDTM can not do indexing or counting functions!");
    }

    public boolean getShouldCacheNodes() {
        return this.m_cacheNodes;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setShouldCacheNodes(boolean z) {
        if (isFresh()) {
            this.m_cacheNodes = z;
            this.m_mutable = true;
            return;
        }
        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_CANNOT_CALL_SETSHOULDCACHENODE", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isMutable() {
        return this.m_mutable;
    }

    public int getLast() {
        return this.m_last;
    }

    public void setLast(int i) {
        this.m_last = i;
    }
}

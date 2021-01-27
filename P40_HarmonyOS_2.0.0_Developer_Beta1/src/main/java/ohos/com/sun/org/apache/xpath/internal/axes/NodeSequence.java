package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.utils.NodeVector;
import ohos.com.sun.org.apache.xpath.internal.NodeSetDTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;

public class NodeSequence extends XObject implements DTMIterator, Cloneable, PathComponent {
    static final long serialVersionUID = 3866261934726581044L;
    private IteratorCache m_cache;
    protected DTMManager m_dtmMgr;
    protected DTMIterator m_iter;
    protected int m_last;
    protected int m_next;

    /* access modifiers changed from: protected */
    public NodeVector getVector() {
        IteratorCache iteratorCache = this.m_cache;
        if (iteratorCache != null) {
            return iteratorCache.getVector();
        }
        return null;
    }

    private IteratorCache getCache() {
        return this.m_cache;
    }

    /* access modifiers changed from: protected */
    public void SetVector(NodeVector nodeVector) {
        setObject(nodeVector);
    }

    public boolean hasCache() {
        return getVector() != null;
    }

    private boolean cacheComplete() {
        IteratorCache iteratorCache = this.m_cache;
        if (iteratorCache != null) {
            return iteratorCache.isComplete();
        }
        return false;
    }

    private void markCacheComplete() {
        if (getVector() != null) {
            this.m_cache.setCacheComplete(true);
        }
    }

    public final void setIter(DTMIterator dTMIterator) {
        this.m_iter = dTMIterator;
    }

    public final DTMIterator getContainedIter() {
        return this.m_iter;
    }

    private NodeSequence(DTMIterator dTMIterator, int i, XPathContext xPathContext, boolean z) {
        this.m_last = -1;
        this.m_next = 0;
        setIter(dTMIterator);
        setRoot(i, xPathContext);
        setShouldCacheNodes(z);
    }

    public NodeSequence(Object obj) {
        super(obj);
        this.m_last = -1;
        this.m_next = 0;
        boolean z = obj instanceof NodeVector;
        if (z) {
            SetVector((NodeVector) obj);
        }
        if (obj != null) {
            assertion(z, "Must have a NodeVector as the object for NodeSequence!");
            if (obj instanceof DTMIterator) {
                DTMIterator dTMIterator = (DTMIterator) obj;
                setIter(dTMIterator);
                this.m_last = dTMIterator.getLength();
            }
        }
    }

    private NodeSequence(DTMManager dTMManager) {
        super(new NodeVector());
        this.m_last = -1;
        this.m_next = 0;
        this.m_last = 0;
        this.m_dtmMgr = dTMManager;
    }

    public NodeSequence() {
        this.m_last = -1;
        this.m_next = 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTM getDTM(int i) {
        if (getDTMManager() != null) {
            return getDTMManager().getDTM(i);
        }
        assertion(false, "Can not get a DTM Unless a DTMManager has been set!");
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMManager getDTMManager() {
        return this.m_dtmMgr;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getRoot() {
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            return dTMIterator.getRoot();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        if (i == -1) {
            throw new RuntimeException("Unable to evaluate expression using this context");
        } else if (this.m_iter != null) {
            this.m_dtmMgr = ((XPathContext) obj).getDTMManager();
            this.m_iter.setRoot(i, obj);
            if (!this.m_iter.isDocOrdered()) {
                if (!hasCache()) {
                    setShouldCacheNodes(true);
                }
                runTo(-1);
                this.m_next = 0;
            }
        } else {
            assertion(false, "Can not setRoot on a non-iterated NodeSequence!");
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void reset() {
        this.m_next = 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getWhatToShow() {
        if (hasCache()) {
            return -17;
        }
        return this.m_iter.getWhatToShow();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean getExpandEntityReferences() {
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            return dTMIterator.getExpandEntityReferences();
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        NodeVector vector = getVector();
        if (vector != null) {
            if (this.m_next < vector.size()) {
                int elementAt = vector.elementAt(this.m_next);
                this.m_next++;
                return elementAt;
            } else if (cacheComplete() || -1 != this.m_last || this.m_iter == null) {
                this.m_next++;
                return -1;
            }
        }
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator == null) {
            return -1;
        }
        int nextNode = dTMIterator.nextNode();
        if (-1 == nextNode) {
            markCacheComplete();
            int i = this.m_next;
            this.m_last = i;
            this.m_next = i + 1;
        } else if (!hasCache()) {
            this.m_next++;
        } else if (this.m_iter.isDocOrdered()) {
            getVector().addElement(nextNode);
            this.m_next++;
        } else if (addNodeInDocOrder(nextNode) >= 0) {
            this.m_next++;
        }
        return nextNode;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int previousNode() {
        if (hasCache()) {
            int i = this.m_next;
            if (i <= 0) {
                return -1;
            }
            this.m_next = i - 1;
            return item(this.m_next);
        }
        this.m_iter.previousNode();
        this.m_next = this.m_iter.getCurrentPos();
        return this.m_next;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            dTMIterator.detach();
        }
        super.detach();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void allowDetachToRelease(boolean z) {
        if (!z && !hasCache()) {
            setShouldCacheNodes(true);
        }
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            dTMIterator.allowDetachToRelease(z);
        }
        super.allowDetachToRelease(z);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getCurrentNode() {
        if (hasCache()) {
            int i = this.m_next - 1;
            NodeVector vector = getVector();
            if (i < 0 || i >= vector.size()) {
                return -1;
            }
            return vector.elementAt(i);
        }
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            return dTMIterator.getCurrentNode();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isFresh() {
        return this.m_next == 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setShouldCacheNodes(boolean z) {
        if (!z) {
            SetVector(null);
        } else if (!hasCache()) {
            SetVector(new NodeVector());
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isMutable() {
        return hasCache();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getCurrentPos() {
        return this.m_next;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void runTo(int i) {
        if (-1 == i) {
            int i2 = this.m_next;
            do {
            } while (-1 != nextNode());
            this.m_next = i2;
        } else if (this.m_next != i) {
            if (hasCache() && i < getVector().size()) {
                this.m_next = i;
            } else if (getVector() != null || i >= this.m_next) {
                while (this.m_next < i && -1 != nextNode()) {
                }
            } else {
                while (this.m_next >= i && -1 != previousNode()) {
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setCurrentPos(int i) {
        runTo(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int item(int i) {
        setCurrentPos(i);
        int nextNode = nextNode();
        this.m_next = i;
        return nextNode;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setItem(int i, int i2) {
        NodeVector vector = getVector();
        if (vector != null) {
            if (vector.elementAt(i2) != i && this.m_cache.useCount() > 1) {
                IteratorCache iteratorCache = new IteratorCache();
                try {
                    vector = (NodeVector) vector.clone();
                    iteratorCache.setVector(vector);
                    iteratorCache.setCacheComplete(true);
                    this.m_cache = iteratorCache;
                    super.setObject(vector);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
            vector.setElementAt(i, i2);
            this.m_last = vector.size();
            return;
        }
        this.m_iter.setItem(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getLength() {
        IteratorCache cache = getCache();
        if (cache == null) {
            int i = this.m_last;
            if (-1 != i) {
                return i;
            }
            int length = this.m_iter.getLength();
            this.m_last = length;
            return length;
        } else if (cache.isComplete()) {
            return cache.getVector().size();
        } else {
            DTMIterator dTMIterator = this.m_iter;
            if (dTMIterator instanceof NodeSetDTM) {
                return dTMIterator.getLength();
            }
            if (-1 == this.m_last) {
                int i2 = this.m_next;
                runTo(-1);
                this.m_next = i2;
            }
            return this.m_last;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        NodeSequence nodeSequence = (NodeSequence) super.clone();
        nodeSequence.m_next = 0;
        IteratorCache iteratorCache = this.m_cache;
        if (iteratorCache != null) {
            iteratorCache.increaseUseCount();
        }
        return nodeSequence;
    }

    @Override // java.lang.Object, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public Object clone() throws CloneNotSupportedException {
        NodeSequence nodeSequence = (NodeSequence) super.clone();
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            nodeSequence.m_iter = (DTMIterator) dTMIterator.clone();
        }
        IteratorCache iteratorCache = this.m_cache;
        if (iteratorCache != null) {
            iteratorCache.increaseUseCount();
        }
        return nodeSequence;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isDocOrdered() {
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            return dTMIterator.isDocOrdered();
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator != null) {
            return dTMIterator.getAxis();
        }
        assertion(false, "Can not getAxis from a non-iterated node sequence!");
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PathComponent
    public int getAnalysisBits() {
        DTMIterator dTMIterator = this.m_iter;
        if (dTMIterator == null || !(dTMIterator instanceof PathComponent)) {
            return 0;
        }
        return ((PathComponent) dTMIterator).getAnalysisBits();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
    }

    /* access modifiers changed from: protected */
    public int addNodeInDocOrder(int i) {
        assertion(hasCache(), "addNodeInDocOrder must be done on a mutable sequence!");
        NodeVector vector = getVector();
        int size = vector.size();
        while (true) {
            size--;
            if (size < 0) {
                break;
            }
            int elementAt = vector.elementAt(size);
            if (elementAt == i) {
                size = -2;
                break;
            } else if (!this.m_dtmMgr.getDTM(i).isNodeAfter(i, elementAt)) {
                break;
            }
        }
        if (size == -2) {
            return -1;
        }
        int i2 = size + 1;
        vector.insertElementAt(i, i2);
        return i2;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void setObject(Object obj) {
        if (obj instanceof NodeVector) {
            super.setObject(obj);
            NodeVector nodeVector = (NodeVector) obj;
            IteratorCache iteratorCache = this.m_cache;
            if (iteratorCache != null) {
                iteratorCache.setVector(nodeVector);
            } else if (nodeVector != null) {
                this.m_cache = new IteratorCache();
                this.m_cache.setVector(nodeVector);
            }
        } else if (obj instanceof IteratorCache) {
            IteratorCache iteratorCache2 = (IteratorCache) obj;
            this.m_cache = iteratorCache2;
            this.m_cache.increaseUseCount();
            super.setObject(iteratorCache2.getVector());
        } else {
            super.setObject(obj);
        }
    }

    /* access modifiers changed from: private */
    public static final class IteratorCache {
        private boolean m_isComplete2 = false;
        private int m_useCount2 = 1;
        private NodeVector m_vec2 = null;

        IteratorCache() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int useCount() {
            return this.m_useCount2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void increaseUseCount() {
            if (this.m_vec2 != null) {
                this.m_useCount2++;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setVector(NodeVector nodeVector) {
            this.m_vec2 = nodeVector;
            this.m_useCount2 = 1;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private NodeVector getVector() {
            return this.m_vec2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setCacheComplete(boolean z) {
            this.m_isComplete2 = z;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isComplete() {
            return this.m_isComplete2;
        }
    }

    /* access modifiers changed from: protected */
    public IteratorCache getIteratorCache() {
        return this.m_cache;
    }
}

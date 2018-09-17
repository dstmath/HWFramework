package org.apache.xpath.axes;

import java.util.Vector;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.utils.NodeVector;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

public class NodeSequence extends XObject implements DTMIterator, Cloneable, PathComponent {
    static final long serialVersionUID = 3866261934726581044L;
    private IteratorCache m_cache;
    protected DTMManager m_dtmMgr;
    protected DTMIterator m_iter;
    protected int m_last;
    protected int m_next;

    private static final class IteratorCache {
        private boolean m_isComplete2 = false;
        private int m_useCount2 = 1;
        private NodeVector m_vec2 = null;

        IteratorCache() {
        }

        private int useCount() {
            return this.m_useCount2;
        }

        private void increaseUseCount() {
            if (this.m_vec2 != null) {
                this.m_useCount2++;
            }
        }

        private void setVector(NodeVector nv) {
            this.m_vec2 = nv;
            this.m_useCount2 = 1;
        }

        private NodeVector getVector() {
            return this.m_vec2;
        }

        private void setCacheComplete(boolean b) {
            this.m_isComplete2 = b;
        }

        private boolean isComplete() {
            return this.m_isComplete2;
        }
    }

    protected NodeVector getVector() {
        return this.m_cache != null ? this.m_cache.getVector() : null;
    }

    private IteratorCache getCache() {
        return this.m_cache;
    }

    protected void SetVector(NodeVector v) {
        setObject(v);
    }

    public boolean hasCache() {
        return getVector() != null;
    }

    private boolean cacheComplete() {
        if (this.m_cache != null) {
            return this.m_cache.isComplete();
        }
        return false;
    }

    private void markCacheComplete() {
        if (getVector() != null) {
            this.m_cache.setCacheComplete(true);
        }
    }

    public final void setIter(DTMIterator iter) {
        this.m_iter = iter;
    }

    public final DTMIterator getContainedIter() {
        return this.m_iter;
    }

    private NodeSequence(DTMIterator iter, int context, XPathContext xctxt, boolean shouldCacheNodes) {
        this.m_last = -1;
        this.m_next = 0;
        setIter(iter);
        setRoot(context, xctxt);
        setShouldCacheNodes(shouldCacheNodes);
    }

    public NodeSequence(Object nodeVector) {
        super(nodeVector);
        this.m_last = -1;
        this.m_next = 0;
        if (nodeVector instanceof NodeVector) {
            SetVector((NodeVector) nodeVector);
        }
        if (nodeVector != null) {
            assertion(nodeVector instanceof NodeVector, "Must have a NodeVector as the object for NodeSequence!");
            if (nodeVector instanceof DTMIterator) {
                setIter((DTMIterator) nodeVector);
                this.m_last = ((DTMIterator) nodeVector).getLength();
            }
        }
    }

    private NodeSequence(DTMManager dtmMgr) {
        super(new NodeVector());
        this.m_last = -1;
        this.m_next = 0;
        this.m_last = 0;
        this.m_dtmMgr = dtmMgr;
    }

    public NodeSequence() {
        this.m_last = -1;
        this.m_next = 0;
    }

    public DTM getDTM(int nodeHandle) {
        if (getDTMManager() != null) {
            return getDTMManager().getDTM(nodeHandle);
        }
        assertion(false, "Can not get a DTM Unless a DTMManager has been set!");
        return null;
    }

    public DTMManager getDTMManager() {
        return this.m_dtmMgr;
    }

    public int getRoot() {
        if (this.m_iter != null) {
            return this.m_iter.getRoot();
        }
        return -1;
    }

    public void setRoot(int nodeHandle, Object environment) {
        if (this.m_iter != null) {
            this.m_dtmMgr = ((XPathContext) environment).getDTMManager();
            this.m_iter.setRoot(nodeHandle, environment);
            if (!this.m_iter.isDocOrdered()) {
                if (!hasCache()) {
                    setShouldCacheNodes(true);
                }
                runTo(-1);
                this.m_next = 0;
                return;
            }
            return;
        }
        assertion(false, "Can not setRoot on a non-iterated NodeSequence!");
    }

    public void reset() {
        this.m_next = 0;
    }

    public int getWhatToShow() {
        if (hasCache()) {
            return -17;
        }
        return this.m_iter.getWhatToShow();
    }

    public boolean getExpandEntityReferences() {
        if (this.m_iter != null) {
            return this.m_iter.getExpandEntityReferences();
        }
        return true;
    }

    public int nextNode() {
        int next;
        NodeVector vec = getVector();
        if (vec != null) {
            if (this.m_next < vec.size()) {
                next = vec.elementAt(this.m_next);
                this.m_next++;
                return next;
            } else if (cacheComplete() || -1 != this.m_last || this.m_iter == null) {
                this.m_next++;
                return -1;
            }
        }
        if (this.m_iter == null) {
            return -1;
        }
        next = this.m_iter.nextNode();
        if (-1 == next) {
            markCacheComplete();
            this.m_last = this.m_next;
            this.m_next++;
        } else if (!hasCache()) {
            this.m_next++;
        } else if (this.m_iter.isDocOrdered()) {
            getVector().addElement(next);
            this.m_next++;
        } else if (addNodeInDocOrder(next) >= 0) {
            this.m_next++;
        }
        return next;
    }

    public int previousNode() {
        if (!hasCache()) {
            int n = this.m_iter.previousNode();
            this.m_next = this.m_iter.getCurrentPos();
            return this.m_next;
        } else if (this.m_next <= 0) {
            return -1;
        } else {
            this.m_next--;
            return item(this.m_next);
        }
    }

    public void detach() {
        if (this.m_iter != null) {
            this.m_iter.detach();
        }
        super.detach();
    }

    public void allowDetachToRelease(boolean allowRelease) {
        if (!(allowRelease || (hasCache() ^ 1) == 0)) {
            setShouldCacheNodes(true);
        }
        if (this.m_iter != null) {
            this.m_iter.allowDetachToRelease(allowRelease);
        }
        super.allowDetachToRelease(allowRelease);
    }

    public int getCurrentNode() {
        if (hasCache()) {
            int currentIndex = this.m_next - 1;
            NodeVector vec = getVector();
            if (currentIndex < 0 || currentIndex >= vec.size()) {
                return -1;
            }
            return vec.elementAt(currentIndex);
        } else if (this.m_iter != null) {
            return this.m_iter.getCurrentNode();
        } else {
            return -1;
        }
    }

    public boolean isFresh() {
        return this.m_next == 0;
    }

    public void setShouldCacheNodes(boolean b) {
        if (!b) {
            SetVector(null);
        } else if (!hasCache()) {
            SetVector(new NodeVector());
        }
    }

    public boolean isMutable() {
        return hasCache();
    }

    public int getCurrentPos() {
        return this.m_next;
    }

    public void runTo(int index) {
        if (-1 == index) {
            int pos = this.m_next;
            do {
            } while (-1 != nextNode());
            this.m_next = pos;
        } else if (this.m_next != index) {
            if (hasCache() && this.m_next < getVector().size()) {
                this.m_next = index;
            } else if (getVector() != null || index >= this.m_next) {
                while (this.m_next < index && -1 != nextNode()) {
                }
            } else {
                while (this.m_next >= index && -1 != previousNode()) {
                }
            }
        }
    }

    public void setCurrentPos(int i) {
        runTo(i);
    }

    public int item(int index) {
        setCurrentPos(index);
        int n = nextNode();
        this.m_next = index;
        return n;
    }

    public void setItem(int node, int index) {
        NodeVector vec = getVector();
        if (vec != null) {
            if (vec.elementAt(index) != node && this.m_cache.useCount() > 1) {
                IteratorCache newCache = new IteratorCache();
                try {
                    NodeVector nv = (NodeVector) vec.clone();
                    newCache.setVector(nv);
                    newCache.setCacheComplete(true);
                    this.m_cache = newCache;
                    vec = nv;
                    super.setObject(nv);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
            vec.setElementAt(node, index);
            this.m_last = vec.size();
            return;
        }
        this.m_iter.setItem(node, index);
    }

    public int getLength() {
        IteratorCache cache = getCache();
        if (cache == null) {
            int length;
            if (-1 == this.m_last) {
                length = this.m_iter.getLength();
                this.m_last = length;
            } else {
                length = this.m_last;
            }
            return length;
        } else if (cache.isComplete()) {
            return cache.getVector().size();
        } else {
            if (this.m_iter instanceof NodeSetDTM) {
                return this.m_iter.getLength();
            }
            if (-1 == this.m_last) {
                int pos = this.m_next;
                runTo(-1);
                this.m_next = pos;
            }
            return this.m_last;
        }
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        NodeSequence seq = (NodeSequence) super.clone();
        seq.m_next = 0;
        if (this.m_cache != null) {
            this.m_cache.increaseUseCount();
        }
        return seq;
    }

    public Object clone() throws CloneNotSupportedException {
        NodeSequence clone = (NodeSequence) super.clone();
        if (this.m_iter != null) {
            clone.m_iter = (DTMIterator) this.m_iter.clone();
        }
        if (this.m_cache != null) {
            this.m_cache.increaseUseCount();
        }
        return clone;
    }

    public boolean isDocOrdered() {
        if (this.m_iter != null) {
            return this.m_iter.isDocOrdered();
        }
        return true;
    }

    public int getAxis() {
        if (this.m_iter != null) {
            return this.m_iter.getAxis();
        }
        assertion(false, "Can not getAxis from a non-iterated node sequence!");
        return 0;
    }

    public int getAnalysisBits() {
        if (this.m_iter == null || !(this.m_iter instanceof PathComponent)) {
            return 0;
        }
        return ((PathComponent) this.m_iter).getAnalysisBits();
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
    }

    protected int addNodeInDocOrder(int node) {
        assertion(hasCache(), "addNodeInDocOrder must be done on a mutable sequence!");
        NodeVector vec = getVector();
        int i = vec.size() - 1;
        while (i >= 0) {
            int child = vec.elementAt(i);
            if (child != node) {
                if (!this.m_dtmMgr.getDTM(node).isNodeAfter(node, child)) {
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
        int insertIndex = i + 1;
        vec.insertElementAt(node, insertIndex);
        return insertIndex;
    }

    protected void setObject(Object obj) {
        if (obj instanceof NodeVector) {
            super.setObject(obj);
            NodeVector v = (NodeVector) obj;
            if (this.m_cache != null) {
                this.m_cache.setVector(v);
            } else if (v != null) {
                this.m_cache = new IteratorCache();
                this.m_cache.setVector(v);
            }
        } else if (obj instanceof IteratorCache) {
            IteratorCache cache = (IteratorCache) obj;
            this.m_cache = cache;
            this.m_cache.increaseUseCount();
            super.setObject(cache.getVector());
        } else {
            super.setObject(obj);
        }
    }

    protected IteratorCache getIteratorCache() {
        return this.m_cache;
    }
}

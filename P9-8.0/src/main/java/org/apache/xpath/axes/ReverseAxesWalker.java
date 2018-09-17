package org.apache.xpath.axes;

import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xpath.XPathContext;

public class ReverseAxesWalker extends AxesWalker {
    static final long serialVersionUID = 2847007647832768941L;
    protected DTMAxisIterator m_iterator;

    ReverseAxesWalker(LocPathIterator locPathIterator, int axis) {
        super(locPathIterator, axis);
    }

    public void setRoot(int root) {
        super.setRoot(root);
        this.m_iterator = getDTM(root).getAxisIterator(this.m_axis);
        this.m_iterator.setStartNode(root);
    }

    public void detach() {
        this.m_iterator = null;
        super.detach();
    }

    protected int getNextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        int next = this.m_iterator.next();
        if (this.m_isFresh) {
            this.m_isFresh = false;
        }
        if (-1 == next) {
            this.m_foundLast = true;
        }
        return next;
    }

    public boolean isReverseAxes() {
        return true;
    }

    protected int getProximityPosition(int predicateIndex) {
        if (predicateIndex < 0) {
            return -1;
        }
        int count = this.m_proximityPositions[predicateIndex];
        if (count <= 0) {
            AxesWalker savedWalker = wi().getLastUsedWalker();
            try {
                ReverseAxesWalker clone = (ReverseAxesWalker) clone();
                clone.setRoot(getRoot());
                clone.setPredicateCount(predicateIndex);
                clone.setPrevWalker(null);
                clone.setNextWalker(null);
                wi().setLastUsedWalker(clone);
                while (true) {
                    count++;
                    if (-1 == clone.nextNode()) {
                        break;
                    }
                }
                this.m_proximityPositions[predicateIndex] = count;
            } catch (CloneNotSupportedException e) {
            } finally {
                wi().setLastUsedWalker(savedWalker);
            }
        }
        return count;
    }

    protected void countProximityPosition(int i) {
        if (i < this.m_proximityPositions.length) {
            int[] iArr = this.m_proximityPositions;
            iArr[i] = iArr[i] - 1;
        }
    }

    public int getLastPos(XPathContext xctxt) {
        int count = 0;
        AxesWalker savedWalker = wi().getLastUsedWalker();
        try {
            ReverseAxesWalker clone = (ReverseAxesWalker) clone();
            clone.setRoot(getRoot());
            clone.setPredicateCount(this.m_predicateIndex);
            clone.setPrevWalker(null);
            clone.setNextWalker(null);
            wi().setLastUsedWalker(clone);
            while (-1 != clone.nextNode()) {
                count++;
            }
        } catch (CloneNotSupportedException e) {
        } finally {
            wi().setLastUsedWalker(savedWalker);
        }
        return count;
    }

    public boolean isDocOrdered() {
        return false;
    }
}

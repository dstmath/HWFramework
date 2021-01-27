package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;

public class ReverseAxesWalker extends AxesWalker {
    static final long serialVersionUID = 2847007647832768941L;
    protected DTMAxisIterator m_iterator;

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public boolean isDocOrdered() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public boolean isReverseAxes() {
        return true;
    }

    ReverseAxesWalker(LocPathIterator locPathIterator, int i) {
        super(locPathIterator, i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public void setRoot(int i) {
        super.setRoot(i);
        this.m_iterator = getDTM(i).getAxisIterator(this.m_axis);
        this.m_iterator.setStartNode(i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public void detach() {
        this.m_iterator = null;
        super.detach();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public int getNextNode() {
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

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public int getProximityPosition(int i) {
        if (i < 0) {
            return -1;
        }
        int i2 = this.m_proximityPositions[i];
        if (i2 <= 0) {
            AxesWalker lastUsedWalker = wi().getLastUsedWalker();
            try {
                ReverseAxesWalker reverseAxesWalker = (ReverseAxesWalker) clone();
                reverseAxesWalker.setRoot(getRoot());
                reverseAxesWalker.setPredicateCount(i);
                reverseAxesWalker.setPrevWalker(null);
                reverseAxesWalker.setNextWalker(null);
                wi().setLastUsedWalker(reverseAxesWalker);
                do {
                    i2++;
                } while (-1 != reverseAxesWalker.nextNode());
                this.m_proximityPositions[i] = i2;
            } catch (CloneNotSupportedException unused) {
            } catch (Throwable th) {
                wi().setLastUsedWalker(lastUsedWalker);
                throw th;
            }
            wi().setLastUsedWalker(lastUsedWalker);
        }
        return i2;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public void countProximityPosition(int i) {
        if (i < this.m_proximityPositions.length) {
            int[] iArr = this.m_proximityPositions;
            iArr[i] = iArr[i] - 1;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker, ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public int getLastPos(XPathContext xPathContext) {
        AxesWalker lastUsedWalker = wi().getLastUsedWalker();
        int i = 0;
        try {
            ReverseAxesWalker reverseAxesWalker = (ReverseAxesWalker) clone();
            reverseAxesWalker.setRoot(getRoot());
            reverseAxesWalker.setPredicateCount(getPredicateCount() - 1);
            reverseAxesWalker.setPrevWalker(null);
            reverseAxesWalker.setNextWalker(null);
            wi().setLastUsedWalker(reverseAxesWalker);
            while (-1 != reverseAxesWalker.nextNode()) {
                i++;
            }
        } catch (CloneNotSupportedException unused) {
        } catch (Throwable th) {
            wi().setLastUsedWalker(lastUsedWalker);
            throw th;
        }
        wi().setLastUsedWalker(lastUsedWalker);
        return i;
    }
}

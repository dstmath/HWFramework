package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class CurrentNodeListIterator extends DTMAxisIteratorBase {
    private int _currentIndex;
    private final int _currentNode;
    private boolean _docOrder;
    private final CurrentNodeListFilter _filter;
    private IntegerArray _nodes;
    private DTMAxisIterator _source;
    private AbstractTranslet _translet;

    public CurrentNodeListIterator(DTMAxisIterator dTMAxisIterator, CurrentNodeListFilter currentNodeListFilter, int i, AbstractTranslet abstractTranslet) {
        this(dTMAxisIterator, !dTMAxisIterator.isReverse(), currentNodeListFilter, i, abstractTranslet);
    }

    public CurrentNodeListIterator(DTMAxisIterator dTMAxisIterator, boolean z, CurrentNodeListFilter currentNodeListFilter, int i, AbstractTranslet abstractTranslet) {
        this._nodes = new IntegerArray();
        this._source = dTMAxisIterator;
        this._filter = currentNodeListFilter;
        this._translet = abstractTranslet;
        this._docOrder = z;
        this._currentNode = i;
    }

    public DTMAxisIterator forceNaturalOrder() {
        this._docOrder = true;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setRestartable(boolean z) {
        this._isRestartable = z;
        this._source.setRestartable(z);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public boolean isReverse() {
        return !this._docOrder;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        try {
            CurrentNodeListIterator currentNodeListIterator = (CurrentNodeListIterator) super.clone();
            currentNodeListIterator._nodes = (IntegerArray) this._nodes.clone();
            currentNodeListIterator._source = this._source.cloneIterator();
            currentNodeListIterator._isRestartable = false;
            return currentNodeListIterator.reset();
        } catch (CloneNotSupportedException e) {
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        this._currentIndex = 0;
        return resetPosition();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        int cardinality = this._nodes.cardinality();
        int i = this._currentNode;
        AbstractTranslet abstractTranslet = this._translet;
        int i2 = this._currentIndex;
        while (i2 < cardinality) {
            int i3 = this._docOrder ? i2 + 1 : cardinality - i2;
            int i4 = i2 + 1;
            int at = this._nodes.at(i2);
            if (this._filter.test(at, i3, cardinality, i, abstractTranslet, this)) {
                this._currentIndex = i4;
                return returnNode(at);
            }
            i2 = i4;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        if (this._isRestartable) {
            DTMAxisIterator dTMAxisIterator = this._source;
            this._startNode = i;
            dTMAxisIterator.setStartNode(i);
            this._nodes.clear();
            while (true) {
                int next = this._source.next();
                if (next == -1) {
                    break;
                }
                this._nodes.add(next);
            }
            this._currentIndex = 0;
            resetPosition();
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getLast() {
        if (this._last == -1) {
            this._last = computePositionOfLast();
        }
        return this._last;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setMark() {
        this._markedNode = this._currentIndex;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void gotoMark() {
        this._currentIndex = this._markedNode;
    }

    private int computePositionOfLast() {
        int cardinality = this._nodes.cardinality();
        int i = this._currentNode;
        AbstractTranslet abstractTranslet = this._translet;
        int i2 = this._position;
        int i3 = this._currentIndex;
        int i4 = i2;
        while (i3 < cardinality) {
            int i5 = i3 + 1;
            if (this._filter.test(this._nodes.at(i3), this._docOrder ? i3 + 1 : cardinality - i3, cardinality, i, abstractTranslet, this)) {
                i4++;
            }
            i3 = i5;
        }
        return i4;
    }
}

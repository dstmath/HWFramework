package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class DupFilterIterator extends DTMAxisIteratorBase {
    private int _current = 0;
    private int _lastNext = -1;
    private int _markedLastNext = -1;
    private IntegerArray _nodes = new IntegerArray();
    private int _nodesSize = 0;
    private DTMAxisIterator _source;

    public DupFilterIterator(DTMAxisIterator dTMAxisIterator) {
        this._source = dTMAxisIterator;
        if (dTMAxisIterator instanceof KeyIndex) {
            setStartNode(0);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        boolean z;
        if (this._isRestartable && ((!((z = this._source instanceof KeyIndex)) || this._startNode != 0) && i != this._startNode)) {
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
            if (!z) {
                this._nodes.sort();
            }
            this._nodesSize = this._nodes.cardinality();
            this._current = 0;
            this._lastNext = -1;
            resetPosition();
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        int at;
        do {
            int i = this._current;
            if (i >= this._nodesSize) {
                return -1;
            }
            IntegerArray integerArray = this._nodes;
            this._current = i + 1;
            at = integerArray.at(i);
        } while (at == this._lastNext);
        this._lastNext = at;
        return returnNode(at);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        try {
            DupFilterIterator dupFilterIterator = (DupFilterIterator) super.clone();
            dupFilterIterator._nodes = (IntegerArray) this._nodes.clone();
            dupFilterIterator._source = this._source.cloneIterator();
            dupFilterIterator._isRestartable = false;
            return dupFilterIterator.reset();
        } catch (CloneNotSupportedException e) {
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setRestartable(boolean z) {
        this._isRestartable = z;
        this._source.setRestartable(z);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setMark() {
        this._markedNode = this._current;
        this._markedLastNext = this._lastNext;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void gotoMark() {
        this._current = this._markedNode;
        this._lastNext = this._markedLastNext;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        this._current = 0;
        this._lastNext = -1;
        return resetPosition();
    }
}

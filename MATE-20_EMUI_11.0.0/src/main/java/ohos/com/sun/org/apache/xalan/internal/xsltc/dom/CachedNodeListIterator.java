package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class CachedNodeListIterator extends DTMAxisIteratorBase {
    private int _index = 0;
    private boolean _isEnded = false;
    private IntegerArray _nodes = new IntegerArray();
    private int _numCachedNodes = 0;
    private DTMAxisIterator _source;

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setRestartable(boolean z) {
    }

    public CachedNodeListIterator(DTMAxisIterator dTMAxisIterator) {
        this._source = dTMAxisIterator;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        if (this._isRestartable) {
            this._startNode = i;
            this._source.setStartNode(i);
            resetPosition();
            this._isRestartable = false;
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        int i = this._index;
        this._index = i + 1;
        return getNode(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getPosition() {
        int i = this._index;
        if (i == 0) {
            return 1;
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getNodeByPosition(int i) {
        return getNode(i);
    }

    public int getNode(int i) {
        if (i < this._numCachedNodes) {
            return this._nodes.at(i);
        }
        if (this._isEnded) {
            return -1;
        }
        int next = this._source.next();
        if (next != -1) {
            this._nodes.add(next);
            this._numCachedNodes++;
        } else {
            this._isEnded = true;
        }
        return next;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        return new ClonedNodeListIterator(this);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        this._index = 0;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setMark() {
        this._source.setMark();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void gotoMark() {
        this._source.gotoMark();
    }
}

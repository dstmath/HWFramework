package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;

public class ArrayNodeListIterator implements DTMAxisIterator {
    private static final int[] EMPTY = new int[0];
    private int _mark = 0;
    private int[] _nodes;
    private int _pos = 0;

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getStartNode() {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public boolean isReverse() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setRestartable(boolean z) {
    }

    public ArrayNodeListIterator(int[] iArr) {
        this._nodes = iArr;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        int i = this._pos;
        int[] iArr = this._nodes;
        if (i >= iArr.length) {
            return -1;
        }
        this._pos = i + 1;
        return iArr[i];
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        this._pos = 0;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getLast() {
        return this._nodes.length;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getPosition() {
        return this._pos;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setMark() {
        this._mark = this._pos;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void gotoMark() {
        this._pos = this._mark;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        if (i == -1) {
            this._nodes = EMPTY;
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        return new ArrayNodeListIterator(this._nodes);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getNodeByPosition(int i) {
        return this._nodes[i - 1];
    }
}

package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;

public abstract class NodeIteratorBase implements NodeIterator {
    protected boolean _includeSelf = false;
    protected boolean _isRestartable = true;
    protected int _last = -1;
    protected int _markedNode;
    protected int _position = 0;
    protected int _startNode = -1;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator
    public boolean isReverse() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator
    public abstract NodeIterator setStartNode(int i);

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator
    public void setRestartable(boolean z) {
        this._isRestartable = z;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator
    public NodeIterator reset() {
        boolean z = this._isRestartable;
        this._isRestartable = true;
        setStartNode(this._includeSelf ? this._startNode + 1 : this._startNode);
        this._isRestartable = z;
        return this;
    }

    public NodeIterator includeSelf() {
        this._includeSelf = true;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator
    public int getLast() {
        if (this._last == -1) {
            int i = this._position;
            setMark();
            reset();
            do {
                this._last++;
            } while (next() != -1);
            gotoMark();
            this._position = i;
        }
        return this._last;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator
    public int getPosition() {
        int i = this._position;
        if (i == 0) {
            return 1;
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.NodeIterator
    public NodeIterator cloneIterator() {
        try {
            NodeIteratorBase nodeIteratorBase = (NodeIteratorBase) super.clone();
            nodeIteratorBase._isRestartable = false;
            return nodeIteratorBase.reset();
        } catch (CloneNotSupportedException e) {
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public final int returnNode(int i) {
        this._position++;
        return i;
    }

    /* access modifiers changed from: protected */
    public final NodeIterator resetPosition() {
        this._position = 0;
        return this;
    }
}

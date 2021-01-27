package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;

public abstract class DTMAxisIteratorBase implements DTMAxisIterator {
    protected boolean _includeSelf = false;
    protected boolean _isRestartable = true;
    protected int _last = -1;
    protected int _markedNode;
    protected int _position = 0;
    protected int _startNode = -1;

    public int getAxis() {
        return -1;
    }

    public boolean isDocOrdered() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public boolean isReverse() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getStartNode() {
        return this._startNode;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        boolean z = this._isRestartable;
        this._isRestartable = true;
        setStartNode(this._startNode);
        this._isRestartable = z;
        return this;
    }

    public DTMAxisIterator includeSelf() {
        this._includeSelf = true;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
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

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getPosition() {
        int i = this._position;
        if (i == 0) {
            return 1;
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        try {
            DTMAxisIteratorBase dTMAxisIteratorBase = (DTMAxisIteratorBase) super.clone();
            dTMAxisIteratorBase._isRestartable = false;
            return dTMAxisIteratorBase;
        } catch (CloneNotSupportedException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /* access modifiers changed from: protected */
    public final int returnNode(int i) {
        this._position++;
        return i;
    }

    /* access modifiers changed from: protected */
    public final DTMAxisIterator resetPosition() {
        this._position = 0;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setRestartable(boolean z) {
        this._isRestartable = z;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getNodeByPosition(int i) {
        int next;
        if (i > 0) {
            if (isReverse()) {
                i = (getLast() - i) + 1;
            }
            do {
                next = next();
                if (next != -1) {
                }
            } while (i != getPosition());
            return next;
        }
        return -1;
    }
}

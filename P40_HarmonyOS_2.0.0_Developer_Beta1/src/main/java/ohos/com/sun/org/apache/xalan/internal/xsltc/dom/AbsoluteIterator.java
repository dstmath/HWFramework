package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class AbsoluteIterator extends DTMAxisIteratorBase {
    private DTMAxisIterator _source;

    public AbsoluteIterator(DTMAxisIterator dTMAxisIterator) {
        this._source = dTMAxisIterator;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setRestartable(boolean z) {
        this._isRestartable = z;
        this._source.setRestartable(z);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        this._startNode = 0;
        if (this._isRestartable) {
            this._source.setStartNode(this._startNode);
            resetPosition();
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        return returnNode(this._source.next());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        try {
            AbsoluteIterator absoluteIterator = (AbsoluteIterator) super.clone();
            absoluteIterator._source = this._source.cloneIterator();
            absoluteIterator.resetPosition();
            absoluteIterator._isRestartable = false;
            return absoluteIterator;
        } catch (CloneNotSupportedException e) {
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        this._source.reset();
        return resetPosition();
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

package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class ForwardPositionIterator extends DTMAxisIteratorBase {
    private DTMAxisIterator _source;

    public ForwardPositionIterator(DTMAxisIterator dTMAxisIterator) {
        this._source = dTMAxisIterator;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        try {
            ForwardPositionIterator forwardPositionIterator = (ForwardPositionIterator) super.clone();
            forwardPositionIterator._source = this._source.cloneIterator();
            forwardPositionIterator._isRestartable = false;
            return forwardPositionIterator.reset();
        } catch (CloneNotSupportedException e) {
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        return returnNode(this._source.next());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        this._source.setStartNode(i);
        return this;
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

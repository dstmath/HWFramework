package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;

public final class UnionIterator extends MultiValuedNodeHeapIterator {
    private final DOM _dom;

    private final class LookAheadIterator extends MultiValuedNodeHeapIterator.HeapNode {
        public DTMAxisIterator iterator;

        public LookAheadIterator(DTMAxisIterator dTMAxisIterator) {
            super();
            this.iterator = dTMAxisIterator;
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
        public int step() {
            this._node = this.iterator.next();
            return this._node;
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
        public MultiValuedNodeHeapIterator.HeapNode cloneHeapNode() {
            LookAheadIterator lookAheadIterator = (LookAheadIterator) super.cloneHeapNode();
            lookAheadIterator.iterator = this.iterator.cloneIterator();
            return lookAheadIterator;
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
        public void setMark() {
            super.setMark();
            this.iterator.setMark();
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
        public void gotoMark() {
            super.gotoMark();
            this.iterator.gotoMark();
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
        public boolean isLessThan(MultiValuedNodeHeapIterator.HeapNode heapNode) {
            LookAheadIterator lookAheadIterator = (LookAheadIterator) heapNode;
            return UnionIterator.this._dom.lessThan(this._node, heapNode._node);
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
        public MultiValuedNodeHeapIterator.HeapNode setStartNode(int i) {
            this.iterator.setStartNode(i);
            return this;
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
        public MultiValuedNodeHeapIterator.HeapNode reset() {
            this.iterator.reset();
            return this;
        }
    }

    public UnionIterator(DOM dom) {
        this._dom = dom;
    }

    public UnionIterator addIterator(DTMAxisIterator dTMAxisIterator) {
        addHeapNode(new LookAheadIterator(dTMAxisIterator));
        return this;
    }
}

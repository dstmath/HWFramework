package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public abstract class MultiValuedNodeHeapIterator extends DTMAxisIteratorBase {
    private static final int InitSize = 8;
    private int _cachedHeapSize;
    private int _cachedReturnedLast = -1;
    private int _free = 0;
    private HeapNode[] _heap = new HeapNode[8];
    private int _heapSize = 0;
    private int _returnedLast;
    private int _size = 8;

    public abstract class HeapNode implements Cloneable {
        protected boolean _isStartSet = false;
        protected int _markedNode;
        protected int _node;

        public abstract boolean isLessThan(HeapNode heapNode);

        public abstract HeapNode reset();

        public abstract HeapNode setStartNode(int i);

        public abstract int step();

        public HeapNode() {
        }

        public HeapNode cloneHeapNode() {
            try {
                HeapNode heapNode = (HeapNode) super.clone();
                heapNode._node = this._node;
                heapNode._markedNode = this._node;
                return heapNode;
            } catch (CloneNotSupportedException e) {
                BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
                return null;
            }
        }

        public void setMark() {
            this._markedNode = this._node;
        }

        public void gotoMark() {
            this._node = this._markedNode;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        this._isRestartable = false;
        HeapNode[] heapNodeArr = new HeapNode[this._heap.length];
        try {
            MultiValuedNodeHeapIterator multiValuedNodeHeapIterator = (MultiValuedNodeHeapIterator) super.clone();
            for (int i = 0; i < this._free; i++) {
                heapNodeArr[i] = this._heap[i].cloneHeapNode();
            }
            multiValuedNodeHeapIterator.setRestartable(false);
            multiValuedNodeHeapIterator._heap = heapNodeArr;
            return multiValuedNodeHeapIterator.reset();
        } catch (CloneNotSupportedException e) {
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void addHeapNode(HeapNode heapNode) {
        int i = this._free;
        int i2 = this._size;
        if (i == i2) {
            int i3 = i2 * 2;
            this._size = i3;
            HeapNode[] heapNodeArr = new HeapNode[i3];
            System.arraycopy(this._heap, 0, heapNodeArr, 0, i);
            this._heap = heapNodeArr;
        }
        this._heapSize++;
        HeapNode[] heapNodeArr2 = this._heap;
        int i4 = this._free;
        this._free = i4 + 1;
        heapNodeArr2[i4] = heapNode;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        while (this._heapSize > 0) {
            int i = this._heap[0]._node;
            if (i == -1) {
                int i2 = this._heapSize;
                if (i2 <= 1) {
                    return -1;
                }
                HeapNode[] heapNodeArr = this._heap;
                HeapNode heapNode = heapNodeArr[0];
                int i3 = i2 - 1;
                this._heapSize = i3;
                heapNodeArr[0] = heapNodeArr[i3];
                heapNodeArr[this._heapSize] = heapNode;
            } else if (i == this._returnedLast) {
                this._heap[0].step();
            } else {
                this._heap[0].step();
                heapify(0);
                this._returnedLast = i;
                return returnNode(i);
            }
            heapify(0);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        int i2;
        if (!this._isRestartable) {
            return this;
        }
        this._startNode = i;
        int i3 = 0;
        while (true) {
            i2 = this._free;
            if (i3 >= i2) {
                break;
            }
            if (!this._heap[i3]._isStartSet) {
                this._heap[i3].setStartNode(i);
                this._heap[i3].step();
                this._heap[i3]._isStartSet = true;
            }
            i3++;
        }
        this._heapSize = i2;
        for (int i4 = i2 / 2; i4 >= 0; i4--) {
            heapify(i4);
        }
        this._returnedLast = -1;
        return resetPosition();
    }

    /* access modifiers changed from: protected */
    public void init() {
        for (int i = 0; i < this._free; i++) {
            this._heap[i] = null;
        }
        this._heapSize = 0;
        this._free = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0014, code lost:
        if (r2[r1].isLessThan(r2[r5]) != false) goto L_0x0018;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0026, code lost:
        if (r2[r0].isLessThan(r2[r1]) != false) goto L_0x002a;
     */
    private void heapify(int i) {
        while (true) {
            int i2 = (i + 1) << 1;
            int i3 = i2 - 1;
            if (i3 < this._heapSize) {
                HeapNode[] heapNodeArr = this._heap;
            }
            i3 = i;
            if (i2 < this._heapSize) {
                HeapNode[] heapNodeArr2 = this._heap;
            }
            i2 = i3;
            if (i2 != i) {
                HeapNode[] heapNodeArr3 = this._heap;
                HeapNode heapNode = heapNodeArr3[i2];
                heapNodeArr3[i2] = heapNodeArr3[i];
                heapNodeArr3[i] = heapNode;
                i = i2;
            } else {
                return;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setMark() {
        for (int i = 0; i < this._free; i++) {
            this._heap[i].setMark();
        }
        this._cachedReturnedLast = this._returnedLast;
        this._cachedHeapSize = this._heapSize;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void gotoMark() {
        for (int i = 0; i < this._free; i++) {
            this._heap[i].gotoMark();
        }
        int i2 = this._cachedHeapSize;
        this._heapSize = i2;
        for (int i3 = i2 / 2; i3 >= 0; i3--) {
            heapify(i3);
        }
        this._returnedLast = this._cachedReturnedLast;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        int i;
        int i2 = 0;
        while (true) {
            i = this._free;
            if (i2 >= i) {
                break;
            }
            this._heap[i2].reset();
            this._heap[i2].step();
            i2++;
        }
        this._heapSize = i;
        for (int i3 = i / 2; i3 >= 0; i3--) {
            heapify(i3);
        }
        this._returnedLast = -1;
        return resetPosition();
    }
}

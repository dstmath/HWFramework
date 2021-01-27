package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class SortingIterator extends DTMAxisIteratorBase {
    private static final int INIT_DATA_SIZE = 16;
    private int _current;
    private NodeSortRecord[] _data;
    private NodeSortRecordFactory _factory;
    private int _free = 0;
    private DTMAxisIterator _source;

    public SortingIterator(DTMAxisIterator dTMAxisIterator, NodeSortRecordFactory nodeSortRecordFactory) {
        this._source = dTMAxisIterator;
        this._factory = nodeSortRecordFactory;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        int i = this._current;
        if (i >= this._free) {
            return -1;
        }
        NodeSortRecord[] nodeSortRecordArr = this._data;
        this._current = i + 1;
        return nodeSortRecordArr[i].getNode();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        try {
            DTMAxisIterator dTMAxisIterator = this._source;
            this._startNode = i;
            dTMAxisIterator.setStartNode(i);
            this._data = new NodeSortRecord[16];
            this._free = 0;
            while (true) {
                int next = this._source.next();
                if (next == -1) {
                    break;
                }
                addRecord(this._factory.makeNodeSortRecord(next, this._free));
            }
            quicksort(0, this._free - 1);
            this._current = 0;
        } catch (Exception unused) {
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getPosition() {
        int i = this._current;
        if (i == 0) {
            return 1;
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getLast() {
        return this._free;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setMark() {
        this._source.setMark();
        this._markedNode = this._current;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void gotoMark() {
        this._source.gotoMark();
        this._current = this._markedNode;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        try {
            SortingIterator sortingIterator = (SortingIterator) super.clone();
            sortingIterator._source = this._source.cloneIterator();
            sortingIterator._factory = this._factory;
            sortingIterator._data = this._data;
            sortingIterator._free = this._free;
            sortingIterator._current = this._current;
            sortingIterator.setRestartable(false);
            return sortingIterator.reset();
        } catch (CloneNotSupportedException e) {
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
            return null;
        }
    }

    private void addRecord(NodeSortRecord nodeSortRecord) {
        int i = this._free;
        NodeSortRecord[] nodeSortRecordArr = this._data;
        if (i == nodeSortRecordArr.length) {
            NodeSortRecord[] nodeSortRecordArr2 = new NodeSortRecord[(nodeSortRecordArr.length * 2)];
            System.arraycopy(nodeSortRecordArr, 0, nodeSortRecordArr2, 0, i);
            this._data = nodeSortRecordArr2;
        }
        NodeSortRecord[] nodeSortRecordArr3 = this._data;
        int i2 = this._free;
        this._free = i2 + 1;
        nodeSortRecordArr3[i2] = nodeSortRecord;
    }

    private void quicksort(int i, int i2) {
        while (i < i2) {
            int partition = partition(i, i2);
            quicksort(i, partition);
            i = partition + 1;
        }
    }

    private int partition(int i, int i2) {
        NodeSortRecord nodeSortRecord = this._data[(i + i2) >>> 1];
        int i3 = i - 1;
        int i4 = i2 + 1;
        while (true) {
            i4--;
            if (nodeSortRecord.compareTo(this._data[i4]) >= 0) {
                do {
                    i3++;
                } while (nodeSortRecord.compareTo(this._data[i3]) > 0);
                if (i3 >= i4) {
                    return i4;
                }
                NodeSortRecord[] nodeSortRecordArr = this._data;
                NodeSortRecord nodeSortRecord2 = nodeSortRecordArr[i3];
                nodeSortRecordArr[i3] = nodeSortRecordArr[i4];
                nodeSortRecordArr[i4] = nodeSortRecord2;
            }
        }
    }
}

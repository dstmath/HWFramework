package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xml.internal.dtm.Axis;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIterNodeList;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.com.sun.org.apache.xml.internal.utils.SuballocatedIntVector;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;

public final class MultiDOM implements DOM {
    private static final int INITIAL_SIZE = 4;
    private static final int NO_TYPE = -2;
    private DOM[] _adapters = new DOM[4];
    private Map<String, Integer> _documents = new HashMap();
    private DTMManager _dtmManager;
    private int _free = 1;
    private DOMAdapter _main;
    private int _size = 4;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void setupMapping(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3) {
    }

    private final class AxisIterator extends DTMAxisIteratorBase {
        private final int _axis;
        private int _dtmId = -1;
        private DTMAxisIterator _source;
        private final int _type;

        public AxisIterator(int i, int i2) {
            this._axis = i;
            this._type = i2;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator == null) {
                return -1;
            }
            return dTMAxisIterator.next();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setRestartable(boolean z) {
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator != null) {
                dTMAxisIterator.setRestartable(z);
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == -1) {
                return this;
            }
            int i2 = i >>> 16;
            if (this._source == null || this._dtmId != i2) {
                if (this._type == -2) {
                    this._source = MultiDOM.this._adapters[i2].getAxisIterator(this._axis);
                } else if (this._axis == 3) {
                    this._source = MultiDOM.this._adapters[i2].getTypedChildren(this._type);
                } else {
                    this._source = MultiDOM.this._adapters[i2].getTypedAxisIterator(this._axis, this._type);
                }
            }
            this._dtmId = i2;
            this._source.setStartNode(i);
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator != null) {
                dTMAxisIterator.reset();
            }
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getLast() {
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator != null) {
                return dTMAxisIterator.getLast();
            }
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getPosition() {
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator != null) {
                return dTMAxisIterator.getPosition();
            }
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public boolean isReverse() {
            return Axis.isReverse(this._axis);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setMark() {
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator != null) {
                dTMAxisIterator.setMark();
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void gotoMark() {
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator != null) {
                dTMAxisIterator.gotoMark();
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator cloneIterator() {
            AxisIterator axisIterator = new AxisIterator(this._axis, this._type);
            DTMAxisIterator dTMAxisIterator = this._source;
            if (dTMAxisIterator != null) {
                axisIterator._source = dTMAxisIterator.cloneIterator();
            }
            axisIterator._dtmId = this._dtmId;
            return axisIterator;
        }
    }

    private final class NodeValueIterator extends DTMAxisIteratorBase {
        private final boolean _isReverse;
        private boolean _op;
        private int _returnType = 1;
        private DTMAxisIterator _source;
        private String _value;

        public NodeValueIterator(DTMAxisIterator dTMAxisIterator, int i, String str, boolean z) {
            this._source = dTMAxisIterator;
            this._returnType = i;
            this._value = str;
            this._op = z;
            this._isReverse = dTMAxisIterator.isReverse();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public boolean isReverse() {
            return this._isReverse;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator cloneIterator() {
            try {
                NodeValueIterator nodeValueIterator = (NodeValueIterator) super.clone();
                nodeValueIterator._source = this._source.cloneIterator();
                nodeValueIterator.setRestartable(false);
                return nodeValueIterator.reset();
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

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            this._source.reset();
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int next;
            do {
                next = this._source.next();
                if (next == -1) {
                    return -1;
                }
            } while (this._value.equals(MultiDOM.this.getStringValueX(next)) != this._op);
            if (this._returnType == 0) {
                return returnNode(next);
            }
            return returnNode(MultiDOM.this.getParent(next));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (!this._isRestartable) {
                return this;
            }
            DTMAxisIterator dTMAxisIterator = this._source;
            this._startNode = i;
            dTMAxisIterator.setStartNode(i);
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

    public MultiDOM(DOM dom) {
        DOMAdapter dOMAdapter = (DOMAdapter) dom;
        this._adapters[0] = dOMAdapter;
        this._main = dOMAdapter;
        DOM dOMImpl = dOMAdapter.getDOMImpl();
        if (dOMImpl instanceof DTMDefaultBase) {
            this._dtmManager = ((DTMDefaultBase) dOMImpl).getManager();
        }
        addDOMAdapter(dOMAdapter, false);
    }

    public int nextMask() {
        return this._free;
    }

    public int addDOMAdapter(DOMAdapter dOMAdapter) {
        return addDOMAdapter(dOMAdapter, true);
    }

    private int addDOMAdapter(DOMAdapter dOMAdapter, boolean z) {
        int i;
        int i2;
        DOM nestedDOM;
        int i3;
        DOM dOMImpl = dOMAdapter.getDOMImpl();
        SuballocatedIntVector suballocatedIntVector = null;
        if (dOMImpl instanceof DTMDefaultBase) {
            suballocatedIntVector = ((DTMDefaultBase) dOMImpl).getDTMIDs();
            int size = suballocatedIntVector.size();
            i = size;
            i2 = suballocatedIntVector.elementAt(size - 1) >>> 16;
        } else if (dOMImpl instanceof SimpleResultTreeImpl) {
            i2 = ((SimpleResultTreeImpl) dOMImpl).getDocument() >>> 16;
            i = 1;
        } else {
            i2 = 1;
            i = 1;
        }
        int i4 = this._size;
        if (i2 >= i4) {
            do {
                this._size *= 2;
                i3 = this._size;
            } while (i3 <= i2);
            DOMAdapter[] dOMAdapterArr = new DOMAdapter[i3];
            System.arraycopy(this._adapters, 0, dOMAdapterArr, 0, i4);
            this._adapters = dOMAdapterArr;
        }
        this._free = i2 + 1;
        if (i == 1) {
            this._adapters[i2] = dOMAdapter;
        } else if (suballocatedIntVector != null) {
            i2 = 0;
            for (int i5 = i - 1; i5 >= 0; i5--) {
                i2 = suballocatedIntVector.elementAt(i5) >>> 16;
                this._adapters[i2] = dOMAdapter;
            }
        }
        if (z) {
            this._documents.put(dOMAdapter.getDocumentURI(0), Integer.valueOf(i2));
        }
        if ((dOMImpl instanceof AdaptiveResultTreeImpl) && (nestedDOM = ((AdaptiveResultTreeImpl) dOMImpl).getNestedDOM()) != null) {
            addDOMAdapter(new DOMAdapter(nestedDOM, dOMAdapter.getNamesArray(), dOMAdapter.getUrisArray(), dOMAdapter.getTypesArray(), dOMAdapter.getNamespaceArray()));
        }
        return i2;
    }

    public int getDocumentMask(String str) {
        Integer num = this._documents.get(str);
        if (num == null) {
            return -1;
        }
        return num.intValue();
    }

    public DOM getDOMAdapter(String str) {
        Integer num = this._documents.get(str);
        if (num == null) {
            return null;
        }
        return this._adapters[num.intValue()];
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocument() {
        return this._main.getDocument();
    }

    public DTMManager getDTMManager() {
        return this._dtmManager;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getIterator() {
        return this._main.getIterator();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getStringValue() {
        return this._main.getStringValue();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getChildren(int i) {
        return this._adapters[getDTMId(i)].getChildren(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getTypedChildren(int i) {
        return new AxisIterator(3, i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getAxisIterator(int i) {
        return new AxisIterator(i, -2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getTypedAxisIterator(int i, int i2) {
        return new AxisIterator(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNthDescendant(int i, int i2, boolean z) {
        return this._adapters[getDTMId(i)].getNthDescendant(i, i2, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNodeValueIterator(DTMAxisIterator dTMAxisIterator, int i, String str, boolean z) {
        return new NodeValueIterator(dTMAxisIterator, i, str, z);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator getNamespaceAxisIterator(int i, int i2) {
        return this._main.getNamespaceAxisIterator(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DTMAxisIterator orderNodes(DTMAxisIterator dTMAxisIterator, int i) {
        return this._adapters[getDTMId(i)].orderNodes(dTMAxisIterator, i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(int i) {
        if (i != -1) {
            return this._adapters[i >>> 16].getExpandedTypeID(i);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNamespaceType(int i) {
        return this._adapters[getDTMId(i)].getNamespaceType(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNSType(int i) {
        return this._adapters[getDTMId(i)].getNSType(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getParent(int i) {
        if (i == -1) {
            return -1;
        }
        return this._adapters[i >>> 16].getParent(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getAttributeNode(int i, int i2) {
        if (i2 == -1) {
            return -1;
        }
        return this._adapters[i2 >>> 16].getAttributeNode(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeName(int i) {
        return i == -1 ? "" : this._adapters[i >>> 16].getNodeName(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeNameX(int i) {
        return i == -1 ? "" : this._adapters[i >>> 16].getNodeNameX(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getNamespaceName(int i) {
        return i == -1 ? "" : this._adapters[i >>> 16].getNamespaceName(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getStringValueX(int i) {
        return i == -1 ? "" : this._adapters[i >>> 16].getStringValueX(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void copy(int i, SerializationHandler serializationHandler) throws TransletException {
        if (i != -1) {
            this._adapters[i >>> 16].copy(i, serializationHandler);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void copy(DTMAxisIterator dTMAxisIterator, SerializationHandler serializationHandler) throws TransletException {
        while (true) {
            int next = dTMAxisIterator.next();
            if (next != -1) {
                this._adapters[next >>> 16].copy(next, serializationHandler);
            } else {
                return;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String shallowCopy(int i, SerializationHandler serializationHandler) throws TransletException {
        return i == -1 ? "" : this._adapters[i >>> 16].shallowCopy(i, serializationHandler);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean lessThan(int i, int i2) {
        if (i == -1) {
            return true;
        }
        if (i2 == -1) {
            return false;
        }
        int dTMId = getDTMId(i);
        int dTMId2 = getDTMId(i2);
        if (dTMId == dTMId2) {
            return this._adapters[dTMId].lessThan(i, i2);
        }
        return dTMId < dTMId2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void characters(int i, SerializationHandler serializationHandler) throws TransletException {
        if (i != -1) {
            this._adapters[i >>> 16].characters(i, serializationHandler);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void setFilter(StripFilter stripFilter) {
        for (int i = 0; i < this._free; i++) {
            DOM[] domArr = this._adapters;
            if (domArr[i] != null) {
                domArr[i].setFilter(stripFilter);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Node makeNode(int i) {
        if (i == -1) {
            return null;
        }
        return this._adapters[getDTMId(i)].makeNode(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Node makeNode(DTMAxisIterator dTMAxisIterator) {
        return this._main.makeNode(dTMAxisIterator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public NodeList makeNodeList(int i) {
        if (i == -1) {
            return null;
        }
        return this._adapters[getDTMId(i)].makeNodeList(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public NodeList makeNodeList(DTMAxisIterator dTMAxisIterator) {
        int next = dTMAxisIterator.next();
        if (next == -1) {
            return new DTMAxisIterNodeList(null, null);
        }
        dTMAxisIterator.reset();
        return this._adapters[getDTMId(next)].makeNodeList(dTMAxisIterator);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getLanguage(int i) {
        return this._adapters[getDTMId(i)].getLanguage(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getSize() {
        int i = 0;
        for (int i2 = 0; i2 < this._size; i2++) {
            i += this._adapters[i2].getSize();
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String getDocumentURI(int i) {
        if (i == -1) {
            i = 0;
        }
        return this._adapters[i >>> 16].getDocumentURI(0);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean isElement(int i) {
        if (i == -1) {
            return false;
        }
        return this._adapters[i >>> 16].isElement(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public boolean isAttribute(int i) {
        if (i == -1) {
            return false;
        }
        return this._adapters[i >>> 16].isAttribute(i);
    }

    public int getDTMId(int i) {
        if (i == -1) {
            return 0;
        }
        int i2 = i >>> 16;
        while (i2 >= 2) {
            DOM[] domArr = this._adapters;
            if (domArr[i2] != domArr[i2 - 1]) {
                break;
            }
            i2--;
        }
        return i2;
    }

    public DOM getDTM(int i) {
        return this._adapters[getDTMId(i)];
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNodeIdent(int i) {
        return this._adapters[i >>> 16].getNodeIdent(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNodeHandle(int i) {
        return this._main.getNodeHandle(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DOM getResultTreeFrag(int i, int i2) {
        return this._main.getResultTreeFrag(i, i2);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public DOM getResultTreeFrag(int i, int i2, boolean z) {
        return this._main.getResultTreeFrag(i, i2, z);
    }

    public DOM getMain() {
        return this._main;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public SerializationHandler getOutputDomBuilder() {
        return this._main.getOutputDomBuilder();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public String lookupNamespace(int i, String str) throws TransletException {
        return this._main.lookupNamespace(i, str);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getUnparsedEntityURI(String str) {
        return this._main.getUnparsedEntityURI(str);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public Map<String, Integer> getElementsWithIDs() {
        return this._main.getElementsWithIDs();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public void release() {
        this._main.release();
    }

    private boolean isMatchingAdapterEntry(DOM dom, DOMAdapter dOMAdapter) {
        DOM dOMImpl = dOMAdapter.getDOMImpl();
        return dom == dOMAdapter || ((dOMImpl instanceof AdaptiveResultTreeImpl) && (dom instanceof DOMAdapter) && ((AdaptiveResultTreeImpl) dOMImpl).getNestedDOM() == ((DOMAdapter) dom).getDOMImpl());
    }

    public void removeDOMAdapter(DOMAdapter dOMAdapter) {
        int i = 0;
        this._documents.remove(dOMAdapter.getDocumentURI(0));
        DOM dOMImpl = dOMAdapter.getDOMImpl();
        if (dOMImpl instanceof DTMDefaultBase) {
            SuballocatedIntVector dTMIDs = ((DTMDefaultBase) dOMImpl).getDTMIDs();
            int size = dTMIDs.size();
            while (i < size) {
                this._adapters[dTMIDs.elementAt(i) >>> 16] = null;
                i++;
            }
            return;
        }
        int document = dOMImpl.getDocument() >>> 16;
        if (document > 0) {
            DOM[] domArr = this._adapters;
            if (document < domArr.length && isMatchingAdapterEntry(domArr[document], dOMAdapter)) {
                this._adapters[document] = null;
                return;
            }
        }
        while (true) {
            DOM[] domArr2 = this._adapters;
            if (i >= domArr2.length) {
                return;
            }
            if (isMatchingAdapterEntry(domArr2[document], dOMAdapter)) {
                this._adapters[i] = null;
                return;
            }
            i++;
        }
    }
}

package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public class KeyIndex extends DTMAxisIteratorBase {
    private static final IntegerArray EMPTY_NODES = new IntegerArray(0);
    private int _currentDocumentNode = -1;
    private DOM _dom;
    private DOMEnhancedForDTM _enhancedDOM;
    private Map<String, IntegerArray> _index;
    private int _markedPosition = 0;
    private IntegerArray _nodes = null;
    private Map<Integer, Map> _rootToIndexMap = new HashMap();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getStartNode() {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public boolean isReverse() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setRestartable(boolean z) {
    }

    public KeyIndex(int i) {
    }

    public void add(String str, int i, int i2) {
        if (this._currentDocumentNode != i2) {
            this._currentDocumentNode = i2;
            this._index = new HashMap();
            this._rootToIndexMap.put(Integer.valueOf(i2), this._index);
        }
        IntegerArray integerArray = this._index.get(str);
        if (integerArray == null) {
            IntegerArray integerArray2 = new IntegerArray();
            this._index.put(str, integerArray2);
            integerArray2.add(i);
        } else if (i != integerArray.at(integerArray.cardinality() - 1)) {
            integerArray.add(i);
        }
    }

    public void merge(KeyIndex keyIndex) {
        IntegerArray integerArray;
        if (keyIndex != null && (integerArray = keyIndex._nodes) != null) {
            IntegerArray integerArray2 = this._nodes;
            if (integerArray2 == null) {
                this._nodes = (IntegerArray) integerArray.clone();
            } else {
                integerArray2.merge(integerArray);
            }
        }
    }

    public void lookupId(Object obj) {
        DOMEnhancedForDTM dOMEnhancedForDTM;
        this._nodes = null;
        StringTokenizer stringTokenizer = new StringTokenizer((String) obj, " \n\t");
        while (stringTokenizer.hasMoreElements()) {
            String str = (String) stringTokenizer.nextElement();
            IntegerArray integerArray = this._index.get(str);
            if (integerArray == null && (dOMEnhancedForDTM = this._enhancedDOM) != null && dOMEnhancedForDTM.hasDOMSource()) {
                integerArray = getDOMNodeById(str);
            }
            if (integerArray != null) {
                IntegerArray integerArray2 = this._nodes;
                if (integerArray2 == null) {
                    this._nodes = (IntegerArray) integerArray.clone();
                } else {
                    integerArray2.merge(integerArray);
                }
            }
        }
    }

    public IntegerArray getDOMNodeById(String str) {
        int elementById;
        DOMEnhancedForDTM dOMEnhancedForDTM = this._enhancedDOM;
        IntegerArray integerArray = null;
        if (!(dOMEnhancedForDTM == null || (elementById = dOMEnhancedForDTM.getElementById(str)) == -1)) {
            Integer num = new Integer(this._enhancedDOM.getDocument());
            Map map = this._rootToIndexMap.get(num);
            if (map == null) {
                map = new HashMap();
                this._rootToIndexMap.put(num, map);
            } else {
                integerArray = (IntegerArray) map.get(str);
            }
            if (integerArray == null) {
                integerArray = new IntegerArray();
                map.put(str, integerArray);
            }
            integerArray.add(this._enhancedDOM.getNodeHandle(elementById));
        }
        return integerArray;
    }

    public void lookupKey(Object obj) {
        IntegerArray integerArray = this._index.get(obj);
        this._nodes = integerArray != null ? (IntegerArray) integerArray.clone() : null;
        this._position = 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int next() {
        if (this._nodes == null || this._position >= this._nodes.cardinality()) {
            return -1;
        }
        DOM dom = this._dom;
        IntegerArray integerArray = this._nodes;
        int i = this._position;
        this._position = i + 1;
        return dom.getNodeHandle(integerArray.at(i));
    }

    public int containsID(int i, Object obj) {
        DOMEnhancedForDTM dOMEnhancedForDTM;
        Map map = this._rootToIndexMap.get(Integer.valueOf(this._dom.getAxisIterator(19).setStartNode(i).next()));
        StringTokenizer stringTokenizer = new StringTokenizer((String) obj, " \n\t");
        while (stringTokenizer.hasMoreElements()) {
            String str = (String) stringTokenizer.nextElement();
            IntegerArray integerArray = null;
            if (map != null) {
                integerArray = (IntegerArray) map.get(str);
            }
            if (integerArray == null && (dOMEnhancedForDTM = this._enhancedDOM) != null && dOMEnhancedForDTM.hasDOMSource()) {
                integerArray = getDOMNodeById(str);
            }
            if (integerArray != null && integerArray.indexOf(i) >= 0) {
                return 1;
            }
        }
        return 0;
    }

    public int containsKey(int i, Object obj) {
        IntegerArray integerArray;
        Map map = this._rootToIndexMap.get(new Integer(this._dom.getAxisIterator(19).setStartNode(i).next()));
        if (map == null || (integerArray = (IntegerArray) map.get(obj)) == null || integerArray.indexOf(i) < 0) {
            return 0;
        }
        return 1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator reset() {
        this._position = 0;
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getLast() {
        IntegerArray integerArray = this._nodes;
        if (integerArray == null) {
            return 0;
        }
        return integerArray.cardinality();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public int getPosition() {
        return this._position;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void setMark() {
        this._markedPosition = this._position;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public void gotoMark() {
        this._position = this._markedPosition;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator setStartNode(int i) {
        if (i == -1) {
            this._nodes = null;
        } else if (this._nodes != null) {
            this._position = 0;
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public DTMAxisIterator cloneIterator() {
        KeyIndex keyIndex = new KeyIndex(0);
        keyIndex._index = this._index;
        keyIndex._rootToIndexMap = this._rootToIndexMap;
        keyIndex._nodes = this._nodes;
        keyIndex._position = this._position;
        return keyIndex;
    }

    public void setDom(DOM dom, int i) {
        this._dom = dom;
        if (dom instanceof MultiDOM) {
            dom = ((MultiDOM) dom).getDTM(i);
        }
        if (dom instanceof DOMEnhancedForDTM) {
            this._enhancedDOM = (DOMEnhancedForDTM) dom;
        } else if (dom instanceof DOMAdapter) {
            DOM dOMImpl = ((DOMAdapter) dom).getDOMImpl();
            if (dOMImpl instanceof DOMEnhancedForDTM) {
                this._enhancedDOM = (DOMEnhancedForDTM) dOMImpl;
            }
        }
    }

    public KeyIndexIterator getKeyIndexIterator(Object obj, boolean z) {
        if (obj instanceof DTMAxisIterator) {
            return getKeyIndexIterator((DTMAxisIterator) obj, z);
        }
        return getKeyIndexIterator(BasisLibrary.stringF(obj, this._dom), z);
    }

    public KeyIndexIterator getKeyIndexIterator(String str, boolean z) {
        return new KeyIndexIterator(str, z);
    }

    public KeyIndexIterator getKeyIndexIterator(DTMAxisIterator dTMAxisIterator, boolean z) {
        return new KeyIndexIterator(dTMAxisIterator, z);
    }

    public class KeyIndexIterator extends MultiValuedNodeHeapIterator {
        private boolean _isKeyIterator;
        private String _keyValue;
        private DTMAxisIterator _keyValueIterator;
        private IntegerArray _nodes;

        /* access modifiers changed from: protected */
        public class KeyIndexHeapNode extends MultiValuedNodeHeapIterator.HeapNode {
            private int _markPosition = -1;
            private IntegerArray _nodes;
            private int _position = 0;

            @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
            public MultiValuedNodeHeapIterator.HeapNode setStartNode(int i) {
                return this;
            }

            KeyIndexHeapNode(IntegerArray integerArray) {
                super();
                this._nodes = integerArray;
            }

            @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
            public int step() {
                if (this._position < this._nodes.cardinality()) {
                    this._node = this._nodes.at(this._position);
                    this._position++;
                } else {
                    this._node = -1;
                }
                return this._node;
            }

            @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
            public MultiValuedNodeHeapIterator.HeapNode cloneHeapNode() {
                KeyIndexHeapNode keyIndexHeapNode = (KeyIndexHeapNode) super.cloneHeapNode();
                keyIndexHeapNode._nodes = this._nodes;
                keyIndexHeapNode._position = this._position;
                keyIndexHeapNode._markPosition = this._markPosition;
                return keyIndexHeapNode;
            }

            @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
            public void setMark() {
                this._markPosition = this._position;
            }

            @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
            public void gotoMark() {
                this._position = this._markPosition;
            }

            @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
            public boolean isLessThan(MultiValuedNodeHeapIterator.HeapNode heapNode) {
                return this._node < heapNode._node;
            }

            @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator.HeapNode
            public MultiValuedNodeHeapIterator.HeapNode reset() {
                this._position = 0;
                return this;
            }
        }

        KeyIndexIterator(String str, boolean z) {
            this._isKeyIterator = z;
            this._keyValue = str;
        }

        KeyIndexIterator(DTMAxisIterator dTMAxisIterator, boolean z) {
            this._keyValueIterator = dTMAxisIterator;
            this._isKeyIterator = z;
        }

        /* access modifiers changed from: protected */
        public IntegerArray lookupNodes(int i, String str) {
            Map map = (Map) KeyIndex.this._rootToIndexMap.get(Integer.valueOf(i));
            if (!this._isKeyIterator) {
                StringTokenizer stringTokenizer = new StringTokenizer(str, " \n\t");
                IntegerArray integerArray = null;
                while (stringTokenizer.hasMoreElements()) {
                    String str2 = (String) stringTokenizer.nextElement();
                    IntegerArray integerArray2 = map != null ? (IntegerArray) map.get(str2) : null;
                    if (integerArray2 == null && KeyIndex.this._enhancedDOM != null && KeyIndex.this._enhancedDOM.hasDOMSource()) {
                        integerArray2 = KeyIndex.this.getDOMNodeById(str2);
                    }
                    if (integerArray2 != null) {
                        if (integerArray == null) {
                            integerArray = (IntegerArray) integerArray2.clone();
                        } else {
                            integerArray.merge(integerArray2);
                        }
                    }
                }
                return integerArray;
            } else if (map != null) {
                return (IntegerArray) map.get(str);
            } else {
                return null;
            }
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            this._startNode = i;
            DTMAxisIterator dTMAxisIterator = this._keyValueIterator;
            if (dTMAxisIterator != null) {
                this._keyValueIterator = dTMAxisIterator.setStartNode(i);
            }
            init();
            return super.setStartNode(i);
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._nodes == null) {
                return super.next();
            }
            if (this._position < this._nodes.cardinality()) {
                return returnNode(this._nodes.at(this._position));
            }
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            if (this._nodes == null) {
                init();
            } else {
                super.reset();
            }
            return resetPosition();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiValuedNodeHeapIterator
        public void init() {
            super.init();
            boolean z = false;
            this._position = 0;
            int next = KeyIndex.this._dom.getAxisIterator(19).setStartNode(this._startNode).next();
            DTMAxisIterator dTMAxisIterator = this._keyValueIterator;
            if (dTMAxisIterator == null) {
                this._nodes = lookupNodes(next, this._keyValue);
                if (this._nodes == null) {
                    this._nodes = KeyIndex.EMPTY_NODES;
                    return;
                }
                return;
            }
            DTMAxisIterator reset = dTMAxisIterator.reset();
            this._nodes = null;
            for (int next2 = reset.next(); next2 != -1; next2 = reset.next()) {
                IntegerArray lookupNodes = lookupNodes(next, BasisLibrary.stringF(next2, KeyIndex.this._dom));
                if (lookupNodes != null) {
                    if (!z) {
                        this._nodes = lookupNodes;
                        z = true;
                    } else {
                        IntegerArray integerArray = this._nodes;
                        if (integerArray != null) {
                            addHeapNode(new KeyIndexHeapNode(integerArray));
                            this._nodes = null;
                        }
                        addHeapNode(new KeyIndexHeapNode(lookupNodes));
                    }
                }
            }
            if (!z) {
                this._nodes = KeyIndex.EMPTY_NODES;
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getLast() {
            IntegerArray integerArray = this._nodes;
            return integerArray != null ? integerArray.cardinality() : super.getLast();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getNodeByPosition(int i) {
            IntegerArray integerArray = this._nodes;
            if (integerArray == null) {
                return super.getNodeByPosition(i);
            }
            if (i > 0) {
                if (i <= integerArray.cardinality()) {
                    this._position = i;
                    return this._nodes.at(i - 1);
                }
                this._position = this._nodes.cardinality();
            }
            return -1;
        }
    }
}

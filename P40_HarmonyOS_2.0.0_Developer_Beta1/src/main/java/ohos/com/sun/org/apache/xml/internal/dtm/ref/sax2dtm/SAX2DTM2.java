package ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm;

import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMException;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.ExtendedType;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.SuballocatedIntVector;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringDefault;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.javax.xml.transform.Source;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

public class SAX2DTM2 extends SAX2DTM {
    private static final String EMPTY_STR = "";
    private static final XMLString EMPTY_XML_STR = new XMLStringDefault("");
    protected static final int TEXT_LENGTH_BITS = 10;
    protected static final int TEXT_LENGTH_MAX = 1023;
    protected static final int TEXT_OFFSET_BITS = 21;
    protected static final int TEXT_OFFSET_MAX = 2097151;
    protected int m_MASK;
    protected int m_SHIFT;
    protected int m_blocksize;
    protected boolean m_buildIdIndex;
    private int[][] m_exptype_map;
    private int[] m_exptype_map0;
    protected ExtendedType[] m_extendedTypes;
    private int[][] m_firstch_map;
    private int[] m_firstch_map0;
    private int m_maxNodeIndex;
    private int[][] m_nextsib_map;
    private int[] m_nextsib_map0;
    private int[][] m_parent_map;
    private int[] m_parent_map0;
    private int m_valueIndex;
    protected Vector m_values;

    public final class ChildrenIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        public ChildrenIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int i2 = -1;
            if (i != -1) {
                SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
                i2 = sax2dtm2._firstch2(sax2dtm2.makeNodeIdentity(i));
            }
            this._currentNode = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._currentNode == -1) {
                return -1;
            }
            int i = this._currentNode;
            this._currentNode = SAX2DTM2.this._nextsib2(i);
            return returnNode(SAX2DTM2.this.makeNodeHandle(i));
        }
    }

    public final class ParentIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        private int _nodeType = -1;

        public ParentIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            if (i != -1) {
                SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
                this._currentNode = sax2dtm2._parent2(sax2dtm2.makeNodeIdentity(i));
            } else {
                this._currentNode = -1;
            }
            return resetPosition();
        }

        public DTMAxisIterator setNodeType(int i) {
            this._nodeType = i;
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            if (i == -1) {
                return -1;
            }
            int i2 = this._nodeType;
            if (i2 == -1) {
                this._currentNode = -1;
                return returnNode(SAX2DTM2.this.makeNodeHandle(i));
            }
            if (i2 >= 14) {
                if (i2 == SAX2DTM2.this._exptype2(i)) {
                    this._currentNode = -1;
                    return returnNode(SAX2DTM2.this.makeNodeHandle(i));
                }
            } else if (i2 == SAX2DTM2.this._type2(i)) {
                this._currentNode = -1;
                return returnNode(SAX2DTM2.this.makeNodeHandle(i));
            }
            return -1;
        }
    }

    public final class TypedChildrenIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        private final int _nodeType;

        public TypedChildrenIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int i2 = -1;
            if (i != -1) {
                SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
                i2 = sax2dtm2._firstch2(sax2dtm2.makeNodeIdentity(this._startNode));
            }
            this._currentNode = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            if (i == -1) {
                return -1;
            }
            int i2 = this._nodeType;
            if (i2 != 1) {
                while (i != -1 && SAX2DTM2.this._exptype2(i) != i2) {
                    i = SAX2DTM2.this._nextsib2(i);
                }
            } else {
                while (i != -1 && SAX2DTM2.this._exptype2(i) < 14) {
                    i = SAX2DTM2.this._nextsib2(i);
                }
            }
            if (i == -1) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = SAX2DTM2.this._nextsib2(i);
            return returnNode(SAX2DTM2.this.makeNodeHandle(i));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getNodeByPosition(int i) {
            if (i <= 0) {
                return -1;
            }
            int i2 = this._currentNode;
            int i3 = 0;
            int i4 = this._nodeType;
            if (i4 != 1) {
                while (i2 != -1) {
                    if (SAX2DTM2.this._exptype2(i2) == i4 && (i3 = i3 + 1) == i) {
                        return SAX2DTM2.this.makeNodeHandle(i2);
                    }
                    i2 = SAX2DTM2.this._nextsib2(i2);
                }
                return -1;
            }
            while (i2 != -1) {
                if (SAX2DTM2.this._exptype2(i2) >= 14 && (i3 = i3 + 1) == i) {
                    return SAX2DTM2.this.makeNodeHandle(i2);
                }
                i2 = SAX2DTM2.this._nextsib2(i2);
            }
            return -1;
        }
    }

    public class TypedRootIterator extends DTMDefaultBaseIterators.RootIterator {
        private final int _nodeType;

        public TypedRootIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.RootIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._startNode == this._currentNode) {
                return -1;
            }
            int i = this._startNode;
            SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
            int _exptype2 = sax2dtm2._exptype2(sax2dtm2.makeNodeIdentity(i));
            this._currentNode = i;
            int i2 = this._nodeType;
            if (i2 >= 14) {
                if (i2 == _exptype2) {
                    return returnNode(i);
                }
            } else if (_exptype2 < 14) {
                if (_exptype2 == i2) {
                    return returnNode(i);
                }
            } else if (SAX2DTM2.this.m_extendedTypes[_exptype2].getNodeType() == this._nodeType) {
                return returnNode(i);
            }
            return -1;
        }
    }

    public class FollowingSiblingIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        public FollowingSiblingIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            this._currentNode = SAX2DTM2.this.makeNodeIdentity(i);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = -1;
            if (this._currentNode != -1) {
                i = SAX2DTM2.this._nextsib2(this._currentNode);
            }
            this._currentNode = i;
            return returnNode(SAX2DTM2.this.makeNodeHandle(this._currentNode));
        }
    }

    public final class TypedFollowingSiblingIterator extends FollowingSiblingIterator {
        private final int _nodeType;

        public TypedFollowingSiblingIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2.FollowingSiblingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._currentNode == -1) {
                return -1;
            }
            int i = this._currentNode;
            int i2 = this._nodeType;
            if (i2 == 1) {
                do {
                    i = SAX2DTM2.this._nextsib2(i);
                    if (i == -1) {
                        break;
                    }
                } while (SAX2DTM2.this._exptype2(i) < 14);
            } else {
                do {
                    i = SAX2DTM2.this._nextsib2(i);
                    if (i == -1) {
                        break;
                    }
                } while (SAX2DTM2.this._exptype2(i) != i2);
            }
            this._currentNode = i;
            if (i == -1) {
                return -1;
            }
            return returnNode(SAX2DTM2.this.makeNodeHandle(i));
        }
    }

    public final class AttributeIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        public AttributeIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
            this._currentNode = sax2dtm2.getFirstAttributeIdentity(sax2dtm2.makeNodeIdentity(i));
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            if (i == -1) {
                return -1;
            }
            this._currentNode = SAX2DTM2.this.getNextAttributeIdentity(i);
            return returnNode(SAX2DTM2.this.makeNodeHandle(i));
        }
    }

    public final class TypedAttributeIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        private final int _nodeType;

        public TypedAttributeIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            this._currentNode = SAX2DTM2.this.getTypedAttribute(i, this._nodeType);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            this._currentNode = -1;
            return returnNode(i);
        }
    }

    public class PrecedingSiblingIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        protected int _startNodeID;

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public boolean isReverse() {
            return true;
        }

        public PrecedingSiblingIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(i);
            this._startNodeID = makeNodeIdentity;
            if (makeNodeIdentity == -1) {
                this._currentNode = makeNodeIdentity;
                return resetPosition();
            }
            int _type2 = SAX2DTM2.this._type2(makeNodeIdentity);
            if (2 == _type2 || 13 == _type2) {
                this._currentNode = makeNodeIdentity;
            } else {
                this._currentNode = SAX2DTM2.this._parent2(makeNodeIdentity);
                if (-1 != this._currentNode) {
                    this._currentNode = SAX2DTM2.this._firstch2(this._currentNode);
                } else {
                    this._currentNode = makeNodeIdentity;
                }
            }
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._currentNode == this._startNodeID || this._currentNode == -1) {
                return -1;
            }
            int i = this._currentNode;
            this._currentNode = SAX2DTM2.this._nextsib2(i);
            return returnNode(SAX2DTM2.this.makeNodeHandle(i));
        }
    }

    public final class TypedPrecedingSiblingIterator extends PrecedingSiblingIterator {
        private final int _nodeType;

        public TypedPrecedingSiblingIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2.PrecedingSiblingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this._nodeType;
            int i3 = this._startNodeID;
            if (i2 != 1) {
                while (i != -1 && i != i3 && SAX2DTM2.this._exptype2(i) != i2) {
                    i = SAX2DTM2.this._nextsib2(i);
                }
            } else {
                while (i != -1 && i != i3 && SAX2DTM2.this._exptype2(i) < 14) {
                    i = SAX2DTM2.this._nextsib2(i);
                }
            }
            if (i == -1 || i == i3) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = SAX2DTM2.this._nextsib2(i);
            return returnNode(SAX2DTM2.this.makeNodeHandle(i));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getLast() {
            if (this._last != -1) {
                return this._last;
            }
            setMark();
            int i = this._currentNode;
            int i2 = this._nodeType;
            int i3 = this._startNodeID;
            int i4 = 0;
            if (i2 != 1) {
                while (i != -1 && i != i3) {
                    if (SAX2DTM2.this._exptype2(i) == i2) {
                        i4++;
                    }
                    i = SAX2DTM2.this._nextsib2(i);
                }
            } else {
                while (i != -1 && i != i3) {
                    if (SAX2DTM2.this._exptype2(i) >= 14) {
                        i4++;
                    }
                    i = SAX2DTM2.this._nextsib2(i);
                }
            }
            gotoMark();
            this._last = i4;
            return i4;
        }
    }

    public class PrecedingIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        protected int _markedDescendant;
        protected int _markedNode;
        protected int _markedsp;
        private final int _maxAncestors = 8;
        protected int _oldsp;
        protected int _sp;
        protected int[] _stack = new int[8];

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public boolean isReverse() {
            return true;
        }

        public PrecedingIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator cloneIterator() {
            this._isRestartable = false;
            try {
                PrecedingIterator precedingIterator = (PrecedingIterator) super.clone();
                int[] iArr = new int[this._stack.length];
                System.arraycopy(this._stack, 0, iArr, 0, this._stack.length);
                precedingIterator._stack = iArr;
                return precedingIterator;
            } catch (CloneNotSupportedException unused) {
                throw new DTMException(XMLMessages.createXMLMessage("ER_ITERATOR_CLONE_NOT_SUPPORTED", null));
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(i);
            if (SAX2DTM2.this._type2(makeNodeIdentity) == 2) {
                makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
            }
            this._startNode = makeNodeIdentity;
            this._stack[0] = makeNodeIdentity;
            int i2 = 0;
            while (true) {
                makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
                if (makeNodeIdentity == -1) {
                    break;
                }
                i2++;
                int[] iArr = this._stack;
                if (i2 == iArr.length) {
                    int[] iArr2 = new int[(i2 * 2)];
                    System.arraycopy(iArr, 0, iArr2, 0, i2);
                    this._stack = iArr2;
                }
                this._stack[i2] = makeNodeIdentity;
            }
            if (i2 > 0) {
                i2--;
            }
            this._currentNode = this._stack[i2];
            this._sp = i2;
            this._oldsp = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            while (true) {
                this._currentNode = i + 1;
                if (this._sp < 0) {
                    return -1;
                }
                int i2 = this._currentNode;
                int[] iArr = this._stack;
                int i3 = this._sp;
                if (i2 < iArr[i3]) {
                    int _type2 = SAX2DTM2.this._type2(this._currentNode);
                    if (!(_type2 == 2 || _type2 == 13)) {
                        return returnNode(SAX2DTM2.this.makeNodeHandle(this._currentNode));
                    }
                } else {
                    this._sp = i3 - 1;
                }
                i = this._currentNode;
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            this._sp = this._oldsp;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.InternalAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setMark() {
            this._markedsp = this._sp;
            this._markedNode = this._currentNode;
            this._markedDescendant = this._stack[0];
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.InternalAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void gotoMark() {
            this._sp = this._markedsp;
            this._currentNode = this._markedNode;
        }
    }

    public final class TypedPrecedingIterator extends PrecedingIterator {
        private final int _nodeType;

        public TypedPrecedingIterator(int i) {
            super();
            this._nodeType = i;
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x005f  */
        /* JADX WARNING: Removed duplicated region for block: B:40:? A[RETURN, SYNTHETIC] */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2.PrecedingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this._nodeType;
            if (i2 >= 14) {
                while (true) {
                    i++;
                    if (this._sp < 0) {
                        break;
                    } else if (i >= this._stack[this._sp]) {
                        int i3 = this._sp - 1;
                        this._sp = i3;
                        if (i3 < 0) {
                            break;
                        }
                    } else if (SAX2DTM2.this._exptype2(i) == i2) {
                        break;
                    }
                }
            } else {
                while (true) {
                    i++;
                    if (this._sp < 0) {
                        break;
                    } else if (i >= this._stack[this._sp]) {
                        int i4 = this._sp - 1;
                        this._sp = i4;
                        if (i4 < 0) {
                            break;
                        }
                    } else {
                        int _exptype2 = SAX2DTM2.this._exptype2(i);
                        if (_exptype2 < 14) {
                            if (_exptype2 == i2) {
                                break;
                            }
                        } else if (SAX2DTM2.this.m_extendedTypes[_exptype2].getNodeType() == i2) {
                            break;
                        }
                    }
                }
                this._currentNode = i;
                if (i != -1) {
                    return -1;
                }
                return returnNode(SAX2DTM2.this.makeNodeHandle(i));
            }
            i = -1;
            this._currentNode = i;
            if (i != -1) {
            }
        }
    }

    public class FollowingIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        public FollowingIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            int _nextsib2;
            int _firstch2;
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(i);
            int _type2 = SAX2DTM2.this._type2(makeNodeIdentity);
            if ((2 == _type2 || 13 == _type2) && -1 != (_firstch2 = SAX2DTM2.this._firstch2((makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity))))) {
                this._currentNode = SAX2DTM2.this.makeNodeHandle(_firstch2);
                return resetPosition();
            }
            do {
                _nextsib2 = SAX2DTM2.this._nextsib2(makeNodeIdentity);
                if (-1 == _nextsib2) {
                    makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
                }
                if (-1 != _nextsib2) {
                    break;
                }
            } while (-1 != makeNodeIdentity);
            this._currentNode = SAX2DTM2.this.makeNodeHandle(_nextsib2);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(i);
            while (true) {
                makeNodeIdentity++;
                int _type2 = SAX2DTM2.this._type2(makeNodeIdentity);
                if (-1 == _type2) {
                    this._currentNode = -1;
                    return returnNode(i);
                } else if (2 != _type2 && 13 != _type2) {
                    this._currentNode = SAX2DTM2.this.makeNodeHandle(makeNodeIdentity);
                    return returnNode(i);
                }
            }
        }
    }

    public final class TypedFollowingIterator extends FollowingIterator {
        private final int _nodeType;

        public TypedFollowingIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2.FollowingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i;
            int _type2;
            int _type22;
            int i2 = this._nodeType;
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(this._currentNode);
            if (i2 < 14) {
                while (true) {
                    i = makeNodeIdentity;
                    while (true) {
                        i++;
                        _type2 = SAX2DTM2.this._type2(i);
                        if (_type2 == -1 || !(2 == _type2 || 13 == _type2)) {
                            break;
                        }
                    }
                    if (_type2 == -1) {
                        i = -1;
                    }
                    if (makeNodeIdentity == -1 || SAX2DTM2.this._exptype2(makeNodeIdentity) == i2 || SAX2DTM2.this._type2(makeNodeIdentity) == i2) {
                        break;
                    }
                    makeNodeIdentity = i;
                }
            } else {
                while (true) {
                    i = makeNodeIdentity;
                    while (true) {
                        i++;
                        _type22 = SAX2DTM2.this._type2(i);
                        if (_type22 == -1 || !(2 == _type22 || 13 == _type22)) {
                            break;
                        }
                    }
                    if (_type22 == -1) {
                        i = -1;
                    }
                    if (makeNodeIdentity == -1 || SAX2DTM2.this._exptype2(makeNodeIdentity) == i2) {
                        break;
                    }
                    makeNodeIdentity = i;
                }
            }
            this._currentNode = SAX2DTM2.this.makeNodeHandle(i);
            if (makeNodeIdentity == -1) {
                return -1;
            }
            return returnNode(SAX2DTM2.this.makeNodeHandle(makeNodeIdentity));
        }
    }

    public class AncestorIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        private static final int m_blocksize = 32;
        int[] m_ancestors = new int[32];
        int m_ancestorsPos;
        int m_markedPos;
        int m_realStartNode;
        int m_size = 0;

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public final boolean isReverse() {
            return true;
        }

        public AncestorIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getStartNode() {
            return this.m_realStartNode;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator cloneIterator() {
            this._isRestartable = false;
            try {
                AncestorIterator ancestorIterator = (AncestorIterator) super.clone();
                ancestorIterator._startNode = this._startNode;
                return ancestorIterator;
            } catch (CloneNotSupportedException unused) {
                throw new DTMException(XMLMessages.createXMLMessage("ER_ITERATOR_CLONE_NOT_SUPPORTED", null));
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            this.m_realStartNode = i;
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(i);
            this.m_size = 0;
            int i2 = -1;
            if (makeNodeIdentity == -1) {
                this._currentNode = -1;
                this.m_ancestorsPos = 0;
                return this;
            }
            if (!this._includeSelf) {
                makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
                i = SAX2DTM2.this.makeNodeHandle(makeNodeIdentity);
            }
            this._startNode = i;
            while (makeNodeIdentity != -1) {
                int i3 = this.m_size;
                int[] iArr = this.m_ancestors;
                if (i3 >= iArr.length) {
                    int[] iArr2 = new int[(i3 * 2)];
                    System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                    this.m_ancestors = iArr2;
                }
                int[] iArr3 = this.m_ancestors;
                int i4 = this.m_size;
                this.m_size = i4 + 1;
                iArr3[i4] = i;
                makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
                i = SAX2DTM2.this.makeNodeHandle(makeNodeIdentity);
            }
            this.m_ancestorsPos = this.m_size - 1;
            int i5 = this.m_ancestorsPos;
            if (i5 >= 0) {
                i2 = this.m_ancestors[i5];
            }
            this._currentNode = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            this.m_ancestorsPos = this.m_size - 1;
            int i = this.m_ancestorsPos;
            this._currentNode = i >= 0 ? this.m_ancestors[i] : -1;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this.m_ancestorsPos - 1;
            this.m_ancestorsPos = i2;
            this._currentNode = i2 >= 0 ? this.m_ancestors[this.m_ancestorsPos] : -1;
            return returnNode(i);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.InternalAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setMark() {
            this.m_markedPos = this.m_ancestorsPos;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.InternalAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void gotoMark() {
            this.m_ancestorsPos = this.m_markedPos;
            int i = this.m_ancestorsPos;
            this._currentNode = i >= 0 ? this.m_ancestors[i] : -1;
        }
    }

    public final class TypedAncestorIterator extends AncestorIterator {
        private final int _nodeType;

        public TypedAncestorIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2.AncestorIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            this.m_realStartNode = i;
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(i);
            this.m_size = 0;
            int i2 = -1;
            if (makeNodeIdentity == -1) {
                this._currentNode = -1;
                this.m_ancestorsPos = 0;
                return this;
            }
            int i3 = this._nodeType;
            if (!this._includeSelf) {
                makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
                i = SAX2DTM2.this.makeNodeHandle(makeNodeIdentity);
            }
            this._startNode = i;
            if (i3 >= 14) {
                while (makeNodeIdentity != -1) {
                    if (SAX2DTM2.this._exptype2(makeNodeIdentity) == i3) {
                        if (this.m_size >= this.m_ancestors.length) {
                            int[] iArr = new int[(this.m_size * 2)];
                            System.arraycopy(this.m_ancestors, 0, iArr, 0, this.m_ancestors.length);
                            this.m_ancestors = iArr;
                        }
                        int[] iArr2 = this.m_ancestors;
                        int i4 = this.m_size;
                        this.m_size = i4 + 1;
                        iArr2[i4] = SAX2DTM2.this.makeNodeHandle(makeNodeIdentity);
                    }
                    makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
                }
            } else {
                while (makeNodeIdentity != -1) {
                    int _exptype2 = SAX2DTM2.this._exptype2(makeNodeIdentity);
                    if ((_exptype2 < 14 && _exptype2 == i3) || (_exptype2 >= 14 && SAX2DTM2.this.m_extendedTypes[_exptype2].getNodeType() == i3)) {
                        if (this.m_size >= this.m_ancestors.length) {
                            int[] iArr3 = new int[(this.m_size * 2)];
                            System.arraycopy(this.m_ancestors, 0, iArr3, 0, this.m_ancestors.length);
                            this.m_ancestors = iArr3;
                        }
                        int[] iArr4 = this.m_ancestors;
                        int i5 = this.m_size;
                        this.m_size = i5 + 1;
                        iArr4[i5] = SAX2DTM2.this.makeNodeHandle(makeNodeIdentity);
                    }
                    makeNodeIdentity = SAX2DTM2.this._parent2(makeNodeIdentity);
                }
            }
            this.m_ancestorsPos = this.m_size - 1;
            if (this.m_ancestorsPos >= 0) {
                i2 = this.m_ancestors[this.m_ancestorsPos];
            }
            this._currentNode = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getNodeByPosition(int i) {
            if (i <= 0 || i > this.m_size) {
                return -1;
            }
            return this.m_ancestors[i - 1];
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int getLast() {
            return this.m_size;
        }
    }

    public class DescendantIterator extends DTMDefaultBaseIterators.InternalAxisIteratorBase {
        public DescendantIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = SAX2DTM2.this.makeNodeIdentity(i);
            this._startNode = makeNodeIdentity;
            if (this._includeSelf) {
                makeNodeIdentity--;
            }
            this._currentNode = makeNodeIdentity;
            return resetPosition();
        }

        /* access modifiers changed from: protected */
        public final boolean isDescendant(int i) {
            return SAX2DTM2.this._parent2(i) >= this._startNode || this._startNode == i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int nodeType;
            int i = this._startNode;
            if (i == -1) {
                return -1;
            }
            if (!this._includeSelf || this._currentNode + 1 != i) {
                int i2 = this._currentNode;
                if (i == 0) {
                    while (true) {
                        i2++;
                        int _exptype2 = SAX2DTM2.this._exptype2(i2);
                        if (-1 != _exptype2) {
                            if (_exptype2 != 3 && (nodeType = SAX2DTM2.this.m_extendedTypes[_exptype2].getNodeType()) != 2 && nodeType != 13) {
                                break;
                            }
                        } else {
                            this._currentNode = -1;
                            return -1;
                        }
                    }
                } else {
                    while (true) {
                        i2++;
                        int _type2 = SAX2DTM2.this._type2(i2);
                        if (-1 != _type2 && isDescendant(i2)) {
                            if (2 != _type2 && 3 != _type2 && 13 != _type2) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    this._currentNode = -1;
                    return -1;
                }
                this._currentNode = i2;
                return returnNode(SAX2DTM2.this.makeNodeHandle(i2));
            }
            SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
            int i3 = this._currentNode + 1;
            this._currentNode = i3;
            return returnNode(sax2dtm2.makeNodeHandle(i3));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            boolean z = this._isRestartable;
            this._isRestartable = true;
            setStartNode(SAX2DTM2.this.makeNodeHandle(this._startNode));
            this._isRestartable = z;
            return this;
        }
    }

    public final class TypedDescendantIterator extends DescendantIterator {
        private final int _nodeType;

        public TypedDescendantIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM2.DescendantIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int _exptype2;
            int i = this._startNode;
            if (this._startNode == -1) {
                return -1;
            }
            int i2 = this._currentNode;
            int i3 = this._nodeType;
            if (i3 != 1) {
                do {
                    i2++;
                    _exptype2 = SAX2DTM2.this._exptype2(i2);
                    if (-1 == _exptype2 || (SAX2DTM2.this._parent2(i2) < i && i != i2)) {
                        this._currentNode = -1;
                        return -1;
                    }
                } while (_exptype2 != i3);
            } else if (i == 0) {
                while (true) {
                    i2++;
                    int _exptype22 = SAX2DTM2.this._exptype2(i2);
                    if (-1 != _exptype22) {
                        if (_exptype22 >= 14 && SAX2DTM2.this.m_extendedTypes[_exptype22].getNodeType() == 1) {
                            break;
                        }
                    } else {
                        this._currentNode = -1;
                        return -1;
                    }
                }
            } else {
                while (true) {
                    i2++;
                    int _exptype23 = SAX2DTM2.this._exptype2(i2);
                    if (-1 != _exptype23 && (SAX2DTM2.this._parent2(i2) >= i || i == i2)) {
                        if (_exptype23 >= 14 && SAX2DTM2.this.m_extendedTypes[_exptype23].getNodeType() == 1) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = i2;
            return returnNode(SAX2DTM2.this.makeNodeHandle(i2));
        }
    }

    public final class TypedSingletonIterator extends DTMDefaultBaseIterators.SingletonIterator {
        private final int _nodeType;

        public TypedSingletonIterator(int i) {
            super(SAX2DTM2.this);
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.SingletonIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            if (i == -1) {
                return -1;
            }
            this._currentNode = -1;
            if (this._nodeType >= 14) {
                SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
                if (sax2dtm2._exptype2(sax2dtm2.makeNodeIdentity(i)) == this._nodeType) {
                    return returnNode(i);
                }
            } else {
                SAX2DTM2 sax2dtm22 = SAX2DTM2.this;
                if (sax2dtm22._type2(sax2dtm22.makeNodeIdentity(i)) == this._nodeType) {
                    return returnNode(i);
                }
            }
            return -1;
        }
    }

    public SAX2DTM2(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z) {
        this(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z, 512, true, true, false);
    }

    public SAX2DTM2(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z, int i2, boolean z2, boolean z3, boolean z4) {
        super(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z, i2, z2, z4);
        this.m_valueIndex = 0;
        this.m_buildIdIndex = true;
        int i3 = 0;
        int i4 = i2;
        while (true) {
            i4 >>>= 1;
            if (i4 != 0) {
                i3++;
            } else {
                this.m_blocksize = 1 << i3;
                this.m_SHIFT = i3;
                this.m_MASK = this.m_blocksize - 1;
                this.m_buildIdIndex = z3;
                this.m_values = new Vector(32, 512);
                this.m_maxNodeIndex = 65536;
                this.m_exptype_map0 = this.m_exptype.getMap0();
                this.m_nextsib_map0 = this.m_nextsib.getMap0();
                this.m_firstch_map0 = this.m_firstch.getMap0();
                this.m_parent_map0 = this.m_parent.getMap0();
                return;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public final int _exptype(int i) {
        return this.m_exptype.elementAt(i);
    }

    public final int _exptype2(int i) {
        if (i < this.m_blocksize) {
            return this.m_exptype_map0[i];
        }
        return this.m_exptype_map[i >>> this.m_SHIFT][this.m_MASK & i];
    }

    public final int _nextsib2(int i) {
        if (i < this.m_blocksize) {
            return this.m_nextsib_map0[i];
        }
        return this.m_nextsib_map[i >>> this.m_SHIFT][this.m_MASK & i];
    }

    public final int _firstch2(int i) {
        if (i < this.m_blocksize) {
            return this.m_firstch_map0[i];
        }
        return this.m_firstch_map[i >>> this.m_SHIFT][this.m_MASK & i];
    }

    public final int _parent2(int i) {
        if (i < this.m_blocksize) {
            return this.m_parent_map0[i];
        }
        return this.m_parent_map[i >>> this.m_SHIFT][this.m_MASK & i];
    }

    public final int _type2(int i) {
        int i2;
        if (i < this.m_blocksize) {
            i2 = this.m_exptype_map0[i];
        } else {
            i2 = this.m_exptype_map[i >>> this.m_SHIFT][i & this.m_MASK];
        }
        if (-1 != i2) {
            return this.m_extendedTypes[i2].getNodeType();
        }
        return -1;
    }

    public final int getExpandedTypeID2(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity == -1) {
            return -1;
        }
        if (makeNodeIdentity < this.m_blocksize) {
            return this.m_exptype_map0[makeNodeIdentity];
        }
        return this.m_exptype_map[makeNodeIdentity >>> this.m_SHIFT][this.m_MASK & makeNodeIdentity];
    }

    public final int _exptype2Type(int i) {
        if (-1 != i) {
            return this.m_extendedTypes[i].getNodeType();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public int getIdForNamespace(String str) {
        int indexOf = this.m_values.indexOf(str);
        if (indexOf >= 0) {
            return indexOf;
        }
        this.m_values.addElement(str);
        int i = this.m_valueIndex;
        this.m_valueIndex = i + 1;
        return i;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        int i;
        charactersFlush();
        boolean z = true;
        int expandedTypeID = this.m_expandedNameTable.getExpandedTypeID(str, str2, 1);
        int addNode = addNode(1, expandedTypeID, this.m_parents.peek(), this.m_previous, str3.length() != str2.length() ? this.m_valuesOrPrefixes.stringToIndex(str3) : 0, true);
        if (this.m_indexing) {
            indexNode(expandedTypeID, addNode);
        }
        this.m_parents.push(addNode);
        int size = this.m_prefixMappings.size();
        if (!this.m_pastFirstElement) {
            int expandedTypeID2 = this.m_expandedNameTable.getExpandedTypeID(null, "xml", 13);
            this.m_values.addElement("http://www.w3.org/XML/1998/namespace");
            int i2 = this.m_valueIndex;
            this.m_valueIndex = i2 + 1;
            addNode(13, expandedTypeID2, addNode, -1, i2, false);
            this.m_pastFirstElement = true;
        }
        for (int peek = this.m_contextIndexes.peek(); peek < size; peek += 2) {
            String str4 = (String) this.m_prefixMappings.elementAt(peek);
            if (str4 != null) {
                int expandedTypeID3 = this.m_expandedNameTable.getExpandedTypeID(null, str4, 13);
                this.m_values.addElement((String) this.m_prefixMappings.elementAt(peek + 1));
                int i3 = this.m_valueIndex;
                this.m_valueIndex = i3 + 1;
                addNode(13, expandedTypeID3, addNode, -1, i3, false);
            }
        }
        int length = attributes.getLength();
        for (int i4 = 0; i4 < length; i4++) {
            String uri = attributes.getURI(i4);
            String qName = attributes.getQName(i4);
            String value = attributes.getValue(i4);
            String localName = attributes.getLocalName(i4);
            if (qName == null || (!qName.equals("xmlns") && !qName.startsWith("xmlns:"))) {
                if (this.m_buildIdIndex && attributes.getType(i4).equalsIgnoreCase(SchemaSymbols.ATTVAL_ID)) {
                    setIDAttribute(value, addNode);
                }
                i = 2;
            } else if (!declAlreadyDeclared(getPrefix(qName, uri))) {
                i = 13;
            }
            if (value == null) {
                value = "";
            }
            this.m_values.addElement(value);
            int i5 = this.m_valueIndex;
            this.m_valueIndex = i5 + 1;
            if (localName.length() != qName.length()) {
                int stringToIndex = this.m_valuesOrPrefixes.stringToIndex(qName);
                int size2 = this.m_data.size();
                this.m_data.addElement(stringToIndex);
                this.m_data.addElement(i5);
                i5 = -size2;
            }
            addNode(i, this.m_expandedNameTable.getExpandedTypeID(uri, localName, i), addNode, -1, i5, false);
        }
        if (this.m_wsfilter != null) {
            short shouldStripSpace = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(addNode), this);
            if (3 == shouldStripSpace) {
                z = getShouldStripWhitespace();
            } else if (2 != shouldStripSpace) {
                z = false;
            }
            pushShouldStripWhitespace(z);
        }
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public void endElement(String str, String str2, String str3) throws SAXException {
        charactersFlush();
        this.m_contextIndexes.quickPop(1);
        int peek = this.m_contextIndexes.peek();
        if (peek != this.m_prefixMappings.size()) {
            this.m_prefixMappings.setSize(peek);
        }
        this.m_previous = this.m_parents.pop();
        popShouldStripWhitespace();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public void comment(char[] cArr, int i, int i2) throws SAXException {
        if (!this.m_insideDTD) {
            charactersFlush();
            this.m_values.addElement(new String(cArr, i, i2));
            int i3 = this.m_valueIndex;
            this.m_valueIndex = i3 + 1;
            this.m_previous = addNode(8, 8, this.m_parents.peek(), this.m_previous, i3, false);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public void startDocument() throws SAXException {
        this.m_parents.push(addNode(9, 9, -1, -1, 0, true));
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public void endDocument() throws SAXException {
        super.endDocument();
        this.m_exptype.addElement(-1);
        this.m_parent.addElement(-1);
        this.m_nextsib.addElement(-1);
        this.m_firstch.addElement(-1);
        this.m_extendedTypes = this.m_expandedNameTable.getExtendedTypes();
        this.m_exptype_map = this.m_exptype.getMap();
        this.m_nextsib_map = this.m_nextsib.getMap();
        this.m_firstch_map = this.m_firstch.getMap();
        this.m_parent_map = this.m_parent.getMap();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public final int addNode(int i, int i2, int i3, int i4, int i5, boolean z) {
        int i6 = this.m_size;
        this.m_size = i6 + 1;
        if (i6 == this.m_maxNodeIndex) {
            addNewDTMID(i6);
            this.m_maxNodeIndex += 65536;
        }
        this.m_firstch.addElement(-1);
        this.m_nextsib.addElement(-1);
        this.m_parent.addElement(i3);
        this.m_exptype.addElement(i2);
        this.m_dataOrQName.addElement(i5);
        if (this.m_prevsib != null) {
            this.m_prevsib.addElement(i4);
        }
        if (this.m_locator != null && this.m_useSourceLocationProperty) {
            setSourceLocation();
        }
        if (i != 2) {
            if (i == 13) {
                declareNamespaceInContext(i3, i6);
            } else if (-1 != i4) {
                this.m_nextsib.setElementAt(i6, i4);
            } else if (-1 != i3) {
                this.m_firstch.setElementAt(i6, i3);
            }
        }
        return i6;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public final void charactersFlush() {
        if (this.m_textPendingStart >= 0) {
            int size = this.m_chars.size() - this.m_textPendingStart;
            boolean z = false;
            if (getShouldStripWhitespace()) {
                z = this.m_chars.isWhitespace(this.m_textPendingStart, size);
            }
            if (z) {
                this.m_chars.setLength(this.m_textPendingStart);
            } else if (size > 0) {
                if (size > 1023 || this.m_textPendingStart > TEXT_OFFSET_MAX) {
                    this.m_previous = addNode(this.m_coalescedTextType, 3, this.m_parents.peek(), this.m_previous, -this.m_data.size(), false);
                    this.m_data.addElement(this.m_textPendingStart);
                    this.m_data.addElement(size);
                } else {
                    this.m_previous = addNode(this.m_coalescedTextType, 3, this.m_parents.peek(), this.m_previous, size + (this.m_textPendingStart << 10), false);
                }
            }
            this.m_textPendingStart = -1;
            this.m_coalescedTextType = 3;
            this.m_textType = 3;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM
    public void processingInstruction(String str, String str2) throws SAXException {
        charactersFlush();
        this.m_previous = addNode(7, 7, this.m_parents.peek(), this.m_previous, -this.m_data.size(), false);
        this.m_data.addElement(this.m_valuesOrPrefixes.stringToIndex(str));
        this.m_values.addElement(str2);
        SuballocatedIntVector suballocatedIntVector = this.m_data;
        int i = this.m_valueIndex;
        this.m_valueIndex = i + 1;
        suballocatedIntVector.addElement(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public final int getFirstAttribute(int i) {
        int _type2;
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity == -1 || 1 != _type2(makeNodeIdentity)) {
            return -1;
        }
        do {
            makeNodeIdentity++;
            _type2 = _type2(makeNodeIdentity);
            if (_type2 == 2) {
                return makeNodeHandle(makeNodeIdentity);
            }
        } while (13 == _type2);
        return -1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public int getFirstAttributeIdentity(int i) {
        int _type2;
        if (i == -1 || 1 != _type2(i)) {
            return -1;
        }
        do {
            i++;
            _type2 = _type2(i);
            if (_type2 == 2) {
                return i;
            }
        } while (13 == _type2);
        return -1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public int getNextAttributeIdentity(int i) {
        int _type2;
        do {
            i++;
            _type2 = _type2(i);
            if (_type2 == 2) {
                return i;
            }
        } while (_type2 == 13);
        return -1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public final int getTypedAttribute(int i, int i2) {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity != -1 && 1 == _type2(makeNodeIdentity)) {
            while (true) {
                makeNodeIdentity++;
                int _exptype2 = _exptype2(makeNodeIdentity);
                if (_exptype2 == -1) {
                    break;
                }
                int nodeType = this.m_extendedTypes[_exptype2].getNodeType();
                if (nodeType == 2) {
                    if (_exptype2 == i2) {
                        return makeNodeHandle(makeNodeIdentity);
                    }
                } else if (13 != nodeType) {
                    break;
                }
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalName(int i) {
        int _exptype = _exptype(makeNodeIdentity(i));
        if (_exptype != 7) {
            return this.m_expandedNameTable.getLocalName(_exptype);
        }
        return this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-_dataOrQName(makeNodeIdentity(i))));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public final String getNodeNameX(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        int _exptype2 = _exptype2(makeNodeIdentity);
        if (_exptype2 == 7) {
            return this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-_dataOrQName(makeNodeIdentity)));
        }
        ExtendedType extendedType = this.m_extendedTypes[_exptype2];
        if (extendedType.getNamespace().length() == 0) {
            return extendedType.getLocalName();
        }
        int elementAt = this.m_dataOrQName.elementAt(makeNodeIdentity);
        if (elementAt == 0) {
            return extendedType.getLocalName();
        }
        if (elementAt < 0) {
            elementAt = this.m_data.elementAt(-elementAt);
        }
        return this.m_valuesOrPrefixes.indexToString(elementAt);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeName(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        ExtendedType extendedType = this.m_extendedTypes[_exptype2(makeNodeIdentity)];
        if (extendedType.getNamespace().length() == 0) {
            int nodeType = extendedType.getNodeType();
            String localName = extendedType.getLocalName();
            if (nodeType == 13) {
                if (localName.length() == 0) {
                    return "xmlns";
                }
                return "xmlns:" + localName;
            } else if (nodeType != 7) {
                return localName.length() == 0 ? getFixedNames(nodeType) : localName;
            } else {
                return this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-_dataOrQName(makeNodeIdentity)));
            }
        } else {
            int elementAt = this.m_dataOrQName.elementAt(makeNodeIdentity);
            if (elementAt == 0) {
                return extendedType.getLocalName();
            }
            if (elementAt < 0) {
                elementAt = this.m_data.elementAt(-elementAt);
            }
            return this.m_valuesOrPrefixes.indexToString(elementAt);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public XMLString getStringValue(int i) {
        int i2;
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity == -1) {
            return EMPTY_XML_STR;
        }
        int _type2 = _type2(makeNodeIdentity);
        if (_type2 == 1 || _type2 == 9) {
            int _firstch2 = _firstch2(makeNodeIdentity);
            if (-1 == _firstch2) {
                return EMPTY_XML_STR;
            }
            int i3 = 0;
            int i4 = -1;
            do {
                int _exptype2 = _exptype2(_firstch2);
                if (_exptype2 == 3 || _exptype2 == 4) {
                    int elementAt = this.m_dataOrQName.elementAt(_firstch2);
                    if (elementAt >= 0) {
                        if (-1 == i4) {
                            i4 = elementAt >>> 10;
                        }
                        i2 = elementAt & 1023;
                    } else {
                        if (-1 == i4) {
                            i4 = this.m_data.elementAt(-elementAt);
                        }
                        i2 = this.m_data.elementAt((-elementAt) + 1);
                    }
                    i3 += i2;
                }
                _firstch2++;
            } while (_parent2(_firstch2) >= makeNodeIdentity);
            if (i3 <= 0) {
                return EMPTY_XML_STR;
            }
            if (this.m_xstrf != null) {
                return this.m_xstrf.newstr(this.m_chars, i4, i3);
            }
            return new XMLStringDefault(this.m_chars.getString(i4, i3));
        } else if (3 == _type2 || 4 == _type2) {
            int elementAt2 = this.m_dataOrQName.elementAt(makeNodeIdentity);
            if (elementAt2 >= 0) {
                if (this.m_xstrf != null) {
                    return this.m_xstrf.newstr(this.m_chars, elementAt2 >>> 10, elementAt2 & 1023);
                }
                return new XMLStringDefault(this.m_chars.getString(elementAt2 >>> 10, elementAt2 & 1023));
            } else if (this.m_xstrf != null) {
                int i5 = -elementAt2;
                return this.m_xstrf.newstr(this.m_chars, this.m_data.elementAt(i5), this.m_data.elementAt(i5 + 1));
            } else {
                int i6 = -elementAt2;
                return new XMLStringDefault(this.m_chars.getString(this.m_data.elementAt(i6), this.m_data.elementAt(i6 + 1)));
            }
        } else {
            int elementAt3 = this.m_dataOrQName.elementAt(makeNodeIdentity);
            if (elementAt3 < 0) {
                elementAt3 = this.m_data.elementAt((-elementAt3) + 1);
            }
            if (this.m_xstrf != null) {
                return this.m_xstrf.newstr((String) this.m_values.elementAt(elementAt3));
            }
            return new XMLStringDefault((String) this.m_values.elementAt(elementAt3));
        }
    }

    public final String getStringValueX(int i) {
        int i2;
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity == -1) {
            return "";
        }
        int _type2 = _type2(makeNodeIdentity);
        if (_type2 == 1 || _type2 == 9) {
            int _firstch2 = _firstch2(makeNodeIdentity);
            if (-1 != _firstch2) {
                int i3 = 0;
                int i4 = -1;
                do {
                    int _exptype2 = _exptype2(_firstch2);
                    if (_exptype2 == 3 || _exptype2 == 4) {
                        int elementAt = this.m_dataOrQName.elementAt(_firstch2);
                        if (elementAt >= 0) {
                            if (-1 == i4) {
                                i4 = elementAt >>> 10;
                            }
                            i2 = elementAt & 1023;
                        } else {
                            if (-1 == i4) {
                                i4 = this.m_data.elementAt(-elementAt);
                            }
                            i2 = this.m_data.elementAt((-elementAt) + 1);
                        }
                        i3 += i2;
                    }
                    _firstch2++;
                } while (_parent2(_firstch2) >= makeNodeIdentity);
                if (i3 > 0) {
                    return this.m_chars.getString(i4, i3);
                }
            }
            return "";
        } else if (3 == _type2 || 4 == _type2) {
            int elementAt2 = this.m_dataOrQName.elementAt(makeNodeIdentity);
            if (elementAt2 >= 0) {
                return this.m_chars.getString(elementAt2 >>> 10, elementAt2 & 1023);
            }
            int i5 = -elementAt2;
            return this.m_chars.getString(this.m_data.elementAt(i5), this.m_data.elementAt(i5 + 1));
        } else {
            int elementAt3 = this.m_dataOrQName.elementAt(makeNodeIdentity);
            if (elementAt3 < 0) {
                elementAt3 = this.m_data.elementAt((-elementAt3) + 1);
            }
            return (String) this.m_values.elementAt(elementAt3);
        }
    }

    public String getStringValue() {
        int _firstch2 = _firstch2(0);
        if (_firstch2 == -1) {
            return "";
        }
        if (_exptype2(_firstch2) != 3 || _nextsib2(_firstch2) != -1) {
            return getStringValueX(getDocument());
        }
        int elementAt = this.m_dataOrQName.elementAt(_firstch2);
        if (elementAt >= 0) {
            return this.m_chars.getString(elementAt >>> 10, elementAt & 1023);
        }
        int i = -elementAt;
        return this.m_chars.getString(this.m_data.elementAt(i), this.m_data.elementAt(i + 1));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public final void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException {
        int i2;
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity != -1) {
            int _type2 = _type2(makeNodeIdentity);
            if (_type2 == 1 || _type2 == 9) {
                int _firstch2 = _firstch2(makeNodeIdentity);
                if (-1 != _firstch2) {
                    int i3 = 0;
                    int i4 = -1;
                    do {
                        int _exptype2 = _exptype2(_firstch2);
                        if (_exptype2 == 3 || _exptype2 == 4) {
                            int elementAt = this.m_dataOrQName.elementAt(_firstch2);
                            if (elementAt >= 0) {
                                if (-1 == i4) {
                                    i4 = elementAt >>> 10;
                                }
                                i2 = elementAt & 1023;
                            } else {
                                if (-1 == i4) {
                                    i4 = this.m_data.elementAt(-elementAt);
                                }
                                i2 = this.m_data.elementAt((-elementAt) + 1);
                            }
                            i3 += i2;
                        }
                        _firstch2++;
                    } while (_parent2(_firstch2) >= makeNodeIdentity);
                    if (i3 <= 0) {
                        return;
                    }
                    if (z) {
                        this.m_chars.sendNormalizedSAXcharacters(contentHandler, i4, i3);
                    } else {
                        this.m_chars.sendSAXcharacters(contentHandler, i4, i3);
                    }
                }
            } else if (3 == _type2 || 4 == _type2) {
                int elementAt2 = this.m_dataOrQName.elementAt(makeNodeIdentity);
                if (elementAt2 >= 0) {
                    if (z) {
                        this.m_chars.sendNormalizedSAXcharacters(contentHandler, elementAt2 >>> 10, elementAt2 & 1023);
                    } else {
                        this.m_chars.sendSAXcharacters(contentHandler, elementAt2 >>> 10, elementAt2 & 1023);
                    }
                } else if (z) {
                    int i5 = -elementAt2;
                    this.m_chars.sendNormalizedSAXcharacters(contentHandler, this.m_data.elementAt(i5), this.m_data.elementAt(i5 + 1));
                } else {
                    int i6 = -elementAt2;
                    this.m_chars.sendSAXcharacters(contentHandler, this.m_data.elementAt(i6), this.m_data.elementAt(i6 + 1));
                }
            } else {
                int elementAt3 = this.m_dataOrQName.elementAt(makeNodeIdentity);
                if (elementAt3 < 0) {
                    elementAt3 = this.m_data.elementAt((-elementAt3) + 1);
                }
                String str = (String) this.m_values.elementAt(elementAt3);
                if (z) {
                    FastStringBuffer.sendNormalizedSAXcharacters(str.toCharArray(), 0, str.length(), contentHandler);
                } else {
                    contentHandler.characters(str.toCharArray(), 0, str.length());
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeValue(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        int _type2 = _type2(makeNodeIdentity);
        if (_type2 == 3 || _type2 == 4) {
            int _dataOrQName = _dataOrQName(makeNodeIdentity);
            if (_dataOrQName > 0) {
                return this.m_chars.getString(_dataOrQName >>> 10, _dataOrQName & 1023);
            }
            int i2 = -_dataOrQName;
            return this.m_chars.getString(this.m_data.elementAt(i2), this.m_data.elementAt(i2 + 1));
        } else if (1 == _type2 || 11 == _type2 || 9 == _type2) {
            return null;
        } else {
            int elementAt = this.m_dataOrQName.elementAt(makeNodeIdentity);
            if (elementAt < 0) {
                elementAt = this.m_data.elementAt((-elementAt) + 1);
            }
            return (String) this.m_values.elementAt(elementAt);
        }
    }

    /* access modifiers changed from: protected */
    public final void copyTextNode(int i, SerializationHandler serializationHandler) throws SAXException {
        if (i != -1) {
            int elementAt = this.m_dataOrQName.elementAt(i);
            if (elementAt >= 0) {
                this.m_chars.sendSAXcharacters(serializationHandler, elementAt >>> 10, elementAt & 1023);
                return;
            }
            int i2 = -elementAt;
            this.m_chars.sendSAXcharacters(serializationHandler, this.m_data.elementAt(i2), this.m_data.elementAt(i2 + 1));
        }
    }

    /* access modifiers changed from: protected */
    public final String copyElement(int i, int i2, SerializationHandler serializationHandler) throws SAXException {
        ExtendedType extendedType = this.m_extendedTypes[i2];
        String namespace = extendedType.getNamespace();
        String localName = extendedType.getLocalName();
        if (namespace.length() == 0) {
            serializationHandler.startElement(localName);
            return localName;
        }
        int elementAt = this.m_dataOrQName.elementAt(i);
        if (elementAt == 0) {
            serializationHandler.startElement(localName);
            serializationHandler.namespaceAfterStartElement("", namespace);
            return localName;
        }
        if (elementAt < 0) {
            elementAt = this.m_data.elementAt(-elementAt);
        }
        String indexToString = this.m_valuesOrPrefixes.indexToString(elementAt);
        serializationHandler.startElement(indexToString);
        int indexOf = indexToString.indexOf(58);
        serializationHandler.namespaceAfterStartElement(indexOf > 0 ? indexToString.substring(0, indexOf) : null, namespace);
        return indexToString;
    }

    /* access modifiers changed from: protected */
    public final void copyNS(int i, SerializationHandler serializationHandler, boolean z) throws SAXException {
        int i2;
        if (this.m_namespaceDeclSetElements == null || this.m_namespaceDeclSetElements.size() != 1 || this.m_namespaceDeclSets == null || ((SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(0)).size() != 1) {
            SuballocatedIntVector suballocatedIntVector = null;
            if (z) {
                suballocatedIntVector = findNamespaceContext(i);
                if (suballocatedIntVector != null && suballocatedIntVector.size() >= 1) {
                    i2 = makeNodeIdentity(suballocatedIntVector.elementAt(0));
                } else {
                    return;
                }
            } else {
                i2 = getNextNamespaceNode2(i);
            }
            int i3 = 1;
            while (i2 != -1) {
                String localName = this.m_extendedTypes[_exptype2(i2)].getLocalName();
                int elementAt = this.m_dataOrQName.elementAt(i2);
                if (elementAt < 0) {
                    elementAt = this.m_data.elementAt((-elementAt) + 1);
                }
                serializationHandler.namespaceAfterStartElement(localName, (String) this.m_values.elementAt(elementAt));
                if (!z) {
                    i2 = getNextNamespaceNode2(i2);
                } else if (i3 < suballocatedIntVector.size()) {
                    i2 = makeNodeIdentity(suballocatedIntVector.elementAt(i3));
                    i3++;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public final int getNextNamespaceNode2(int i) {
        int _type2;
        do {
            i++;
            _type2 = _type2(i);
        } while (_type2 == 2);
        if (_type2 == 13) {
            return i;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public final void copyAttributes(int i, SerializationHandler serializationHandler) throws SAXException {
        int firstAttributeIdentity = getFirstAttributeIdentity(i);
        while (firstAttributeIdentity != -1) {
            copyAttribute(firstAttributeIdentity, _exptype2(firstAttributeIdentity), serializationHandler);
            firstAttributeIdentity = getNextAttributeIdentity(firstAttributeIdentity);
        }
    }

    /* access modifiers changed from: protected */
    public final void copyAttribute(int i, int i2, SerializationHandler serializationHandler) throws SAXException {
        String str;
        ExtendedType extendedType = this.m_extendedTypes[i2];
        String namespace = extendedType.getNamespace();
        String localName = extendedType.getLocalName();
        int _dataOrQName = _dataOrQName(i);
        String str2 = null;
        if (_dataOrQName <= 0) {
            int i3 = -_dataOrQName;
            int elementAt = this.m_data.elementAt(i3);
            _dataOrQName = this.m_data.elementAt(i3 + 1);
            str = this.m_valuesOrPrefixes.indexToString(elementAt);
            int indexOf = str.indexOf(58);
            if (indexOf > 0) {
                str2 = str.substring(0, indexOf);
            }
        } else {
            str = null;
        }
        if (namespace.length() != 0) {
            serializationHandler.namespaceAfterStartElement(str2, namespace);
        }
        serializationHandler.addAttribute(namespace, localName, str2 != null ? str : localName, "CDATA", (String) this.m_values.elementAt(_dataOrQName));
    }
}

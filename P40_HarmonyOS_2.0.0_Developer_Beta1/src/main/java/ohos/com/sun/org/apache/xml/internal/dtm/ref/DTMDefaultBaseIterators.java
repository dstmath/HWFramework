package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.Axis;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMException;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.NodeVector;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.javax.xml.transform.Source;

public abstract class DTMDefaultBaseIterators extends DTMDefaultBaseTraversers {
    public DTMDefaultBaseIterators(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z) {
        super(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z);
    }

    public DTMDefaultBaseIterators(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z, int i2, boolean z2, boolean z3) {
        super(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z, i2, z2, z3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getTypedAxisIterator(int i, int i2) {
        if (i == 19) {
            return new TypedRootIterator(i2);
        }
        switch (i) {
            case 0:
                return new TypedAncestorIterator(i2);
            case 1:
                return new TypedAncestorIterator(i2).includeSelf();
            case 2:
                return new TypedAttributeIterator(i2);
            case 3:
                return new TypedChildrenIterator(i2);
            case 4:
                return new TypedDescendantIterator(i2);
            case 5:
                return new TypedDescendantIterator(i2).includeSelf();
            case 6:
                return new TypedFollowingIterator(i2);
            case 7:
                return new TypedFollowingSiblingIterator(i2);
            default:
                switch (i) {
                    case 9:
                        return new TypedNamespaceIterator(i2);
                    case 10:
                        return new ParentIterator().setNodeType(i2);
                    case 11:
                        return new TypedPrecedingIterator(i2);
                    case 12:
                        return new TypedPrecedingSiblingIterator(i2);
                    case 13:
                        return new TypedSingletonIterator(i2);
                    default:
                        throw new DTMException(XMLMessages.createXMLMessage("ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED", new Object[]{Axis.getNames(i)}));
                }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getAxisIterator(int i) {
        if (i == 19) {
            return new RootIterator();
        }
        switch (i) {
            case 0:
                return new AncestorIterator();
            case 1:
                return new AncestorIterator().includeSelf();
            case 2:
                return new AttributeIterator();
            case 3:
                return new ChildrenIterator();
            case 4:
                return new DescendantIterator();
            case 5:
                return new DescendantIterator().includeSelf();
            case 6:
                return new FollowingIterator();
            case 7:
                return new FollowingSiblingIterator();
            default:
                switch (i) {
                    case 9:
                        return new NamespaceIterator();
                    case 10:
                        return new ParentIterator();
                    case 11:
                        return new PrecedingIterator();
                    case 12:
                        return new PrecedingSiblingIterator();
                    case 13:
                        return new SingletonIterator(this);
                    default:
                        throw new DTMException(XMLMessages.createXMLMessage("ER_ITERATOR_AXIS_NOT_IMPLEMENTED", new Object[]{Axis.getNames(i)}));
                }
        }
    }

    public abstract class InternalAxisIteratorBase extends DTMAxisIteratorBase {
        protected int _currentNode;

        public InternalAxisIteratorBase() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void setMark() {
            this._markedNode = this._currentNode;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public void gotoMark() {
            this._currentNode = this._markedNode;
        }
    }

    public final class ChildrenIterator extends InternalAxisIteratorBase {
        public ChildrenIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int i2 = -1;
            if (i != -1) {
                DTMDefaultBaseIterators dTMDefaultBaseIterators = DTMDefaultBaseIterators.this;
                i2 = dTMDefaultBaseIterators._firstch(dTMDefaultBaseIterators.makeNodeIdentity(i));
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
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(i);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
        }
    }

    public final class ParentIterator extends InternalAxisIteratorBase {
        private int _nodeType = -1;

        public ParentIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            this._currentNode = DTMDefaultBaseIterators.this.getParent(i);
            return resetPosition();
        }

        public DTMAxisIterator setNodeType(int i) {
            this._nodeType = i;
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this._nodeType;
            if (i2 < 14 ? !(i2 == -1 || i2 == DTMDefaultBaseIterators.this.getNodeType(this._currentNode)) : i2 != DTMDefaultBaseIterators.this.getExpandedTypeID(this._currentNode)) {
                i = -1;
            }
            this._currentNode = -1;
            return returnNode(i);
        }
    }

    public final class TypedChildrenIterator extends InternalAxisIteratorBase {
        private final int _nodeType;

        public TypedChildrenIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int i2 = -1;
            if (i != -1) {
                DTMDefaultBaseIterators dTMDefaultBaseIterators = DTMDefaultBaseIterators.this;
                i2 = dTMDefaultBaseIterators._firstch(dTMDefaultBaseIterators.makeNodeIdentity(this._startNode));
            }
            this._currentNode = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this._nodeType;
            if (i2 >= 14) {
                while (i != -1 && DTMDefaultBaseIterators.this._exptype(i) != i2) {
                    i = DTMDefaultBaseIterators.this._nextsib(i);
                }
            } else {
                while (i != -1) {
                    int _exptype = DTMDefaultBaseIterators.this._exptype(i);
                    if (_exptype < 14) {
                        if (_exptype == i2) {
                            break;
                        }
                    } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(_exptype) == i2) {
                        break;
                    }
                    i = DTMDefaultBaseIterators.this._nextsib(i);
                }
            }
            if (i == -1) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(i);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
        }
    }

    public final class NamespaceChildrenIterator extends InternalAxisIteratorBase {
        private final int _nsType;

        public NamespaceChildrenIterator(int i) {
            super();
            this._nsType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int i2 = -1;
            if (i != -1) {
                i2 = -2;
            }
            this._currentNode = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i;
            if (this._currentNode != -1) {
                if (-2 == this._currentNode) {
                    DTMDefaultBaseIterators dTMDefaultBaseIterators = DTMDefaultBaseIterators.this;
                    i = dTMDefaultBaseIterators._firstch(dTMDefaultBaseIterators.makeNodeIdentity(this._startNode));
                } else {
                    i = DTMDefaultBaseIterators.this._nextsib(this._currentNode);
                }
                while (i != -1) {
                    if (DTMDefaultBaseIterators.this.m_expandedNameTable.getNamespaceID(DTMDefaultBaseIterators.this._exptype(i)) == this._nsType) {
                        this._currentNode = i;
                        return returnNode(i);
                    }
                    i = DTMDefaultBaseIterators.this._nextsib(i);
                }
            }
            return -1;
        }
    }

    public class NamespaceIterator extends InternalAxisIteratorBase {
        public NamespaceIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            this._currentNode = DTMDefaultBaseIterators.this.getFirstNamespaceNode(i, true);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            if (-1 != i) {
                this._currentNode = DTMDefaultBaseIterators.this.getNextNamespaceNode(this._startNode, i, true);
            }
            return returnNode(i);
        }
    }

    public class TypedNamespaceIterator extends NamespaceIterator {
        private final int _nodeType;

        public TypedNamespaceIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.NamespaceIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            while (i != -1) {
                if (DTMDefaultBaseIterators.this.getExpandedTypeID(i) == this._nodeType || DTMDefaultBaseIterators.this.getNodeType(i) == this._nodeType || DTMDefaultBaseIterators.this.getNamespaceType(i) == this._nodeType) {
                    this._currentNode = i;
                    return returnNode(i);
                }
                i = DTMDefaultBaseIterators.this.getNextNamespaceNode(this._startNode, i, true);
            }
            this._currentNode = -1;
            return -1;
        }
    }

    public class RootIterator extends InternalAxisIteratorBase {
        public RootIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = DTMDefaultBaseIterators.this.getDocumentRoot(i);
            this._currentNode = -1;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._startNode == this._currentNode) {
                return -1;
            }
            this._currentNode = this._startNode;
            return returnNode(this._startNode);
        }
    }

    public class TypedRootIterator extends RootIterator {
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
            int i = this._nodeType;
            int i2 = this._startNode;
            int expandedTypeID = DTMDefaultBaseIterators.this.getExpandedTypeID(i2);
            this._currentNode = i2;
            if (i >= 14) {
                if (i == expandedTypeID) {
                    return returnNode(i2);
                }
            } else if (expandedTypeID < 14) {
                if (expandedTypeID == i) {
                    return returnNode(i2);
                }
            } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(expandedTypeID) == i) {
                return returnNode(i2);
            }
            return -1;
        }
    }

    public final class NamespaceAttributeIterator extends InternalAxisIteratorBase {
        private final int _nsType;

        public NamespaceAttributeIterator(int i) {
            super();
            this._nsType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            this._currentNode = DTMDefaultBaseIterators.this.getFirstNamespaceNode(i, false);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            if (-1 != i) {
                this._currentNode = DTMDefaultBaseIterators.this.getNextNamespaceNode(this._startNode, i, false);
            }
            return returnNode(i);
        }
    }

    public class FollowingSiblingIterator extends InternalAxisIteratorBase {
        public FollowingSiblingIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            this._currentNode = DTMDefaultBaseIterators.this.makeNodeIdentity(i);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = -1;
            if (this._currentNode != -1) {
                i = DTMDefaultBaseIterators.this._nextsib(this._currentNode);
            }
            this._currentNode = i;
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._currentNode));
        }
    }

    public final class TypedFollowingSiblingIterator extends FollowingSiblingIterator {
        private final int _nodeType;

        public TypedFollowingSiblingIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.FollowingSiblingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._currentNode == -1) {
                return -1;
            }
            int i = this._currentNode;
            int i2 = this._nodeType;
            if (i2 >= 14) {
                do {
                    i = DTMDefaultBaseIterators.this._nextsib(i);
                    if (i == -1) {
                        break;
                    }
                } while (DTMDefaultBaseIterators.this._exptype(i) != i2);
            } else {
                while (true) {
                    i = DTMDefaultBaseIterators.this._nextsib(i);
                    if (i == -1) {
                        break;
                    }
                    int _exptype = DTMDefaultBaseIterators.this._exptype(i);
                    if (_exptype < 14) {
                        if (_exptype == i2) {
                            break;
                        }
                    } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(_exptype) == i2) {
                        break;
                    }
                }
            }
            this._currentNode = i;
            if (this._currentNode == -1) {
                return -1;
            }
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._currentNode));
        }
    }

    public final class AttributeIterator extends InternalAxisIteratorBase {
        public AttributeIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            DTMDefaultBaseIterators dTMDefaultBaseIterators = DTMDefaultBaseIterators.this;
            this._currentNode = dTMDefaultBaseIterators.getFirstAttributeIdentity(dTMDefaultBaseIterators.makeNodeIdentity(i));
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            if (i == -1) {
                return -1;
            }
            this._currentNode = DTMDefaultBaseIterators.this.getNextAttributeIdentity(i);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
        }
    }

    public final class TypedAttributeIterator extends InternalAxisIteratorBase {
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
            this._currentNode = DTMDefaultBaseIterators.this.getTypedAttribute(i, this._nodeType);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            this._currentNode = -1;
            return returnNode(i);
        }
    }

    public class PrecedingSiblingIterator extends InternalAxisIteratorBase {
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
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            int makeNodeIdentity = DTMDefaultBaseIterators.this.makeNodeIdentity(i);
            this._startNodeID = makeNodeIdentity;
            if (makeNodeIdentity == -1) {
                this._currentNode = makeNodeIdentity;
                return resetPosition();
            }
            short type = DTMDefaultBaseIterators.this.m_expandedNameTable.getType(DTMDefaultBaseIterators.this._exptype(makeNodeIdentity));
            if (2 == type || 13 == type) {
                this._currentNode = makeNodeIdentity;
            } else {
                this._currentNode = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
                if (-1 != this._currentNode) {
                    this._currentNode = DTMDefaultBaseIterators.this._firstch(this._currentNode);
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
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(i);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
        }
    }

    public final class TypedPrecedingSiblingIterator extends PrecedingSiblingIterator {
        private final int _nodeType;

        public TypedPrecedingSiblingIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.PrecedingSiblingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this._nodeType;
            int i3 = this._startNodeID;
            if (i2 >= 14) {
                while (i != -1 && i != i3 && DTMDefaultBaseIterators.this._exptype(i) != i2) {
                    i = DTMDefaultBaseIterators.this._nextsib(i);
                }
            } else {
                while (i != -1 && i != i3) {
                    int _exptype = DTMDefaultBaseIterators.this._exptype(i);
                    if (_exptype < 14) {
                        if (_exptype == i2) {
                            break;
                        }
                    } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(_exptype) == i2) {
                        break;
                    }
                    i = DTMDefaultBaseIterators.this._nextsib(i);
                }
            }
            if (i == -1 || i == this._startNodeID) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(i);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
        }
    }

    public class PrecedingIterator extends InternalAxisIteratorBase {
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
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = DTMDefaultBaseIterators.this.makeNodeIdentity(i);
            if (DTMDefaultBaseIterators.this._type(makeNodeIdentity) == 2) {
                makeNodeIdentity = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
            }
            this._startNode = makeNodeIdentity;
            this._stack[0] = makeNodeIdentity;
            int i2 = 0;
            while (true) {
                makeNodeIdentity = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
                if (makeNodeIdentity == -1) {
                    break;
                }
                i2++;
                int[] iArr = this._stack;
                if (i2 == iArr.length) {
                    int[] iArr2 = new int[(i2 + 4)];
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
            this._currentNode++;
            while (this._sp >= 0) {
                int i = this._currentNode;
                int[] iArr = this._stack;
                int i2 = this._sp;
                if (i >= iArr[i2]) {
                    this._sp = i2 - 1;
                } else if (!(DTMDefaultBaseIterators.this._type(this._currentNode) == 2 || DTMDefaultBaseIterators.this._type(this._currentNode) == 13)) {
                    return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._currentNode));
                }
                this._currentNode++;
            }
            return -1;
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

        /* JADX WARNING: Removed duplicated region for block: B:24:0x005d  */
        /* JADX WARNING: Removed duplicated region for block: B:40:? A[RETURN, SYNTHETIC] */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.PrecedingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
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
                    } else if (DTMDefaultBaseIterators.this._exptype(i) == i2) {
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
                        int _exptype = DTMDefaultBaseIterators.this._exptype(i);
                        if (_exptype < 14) {
                            if (_exptype == i2) {
                                break;
                            }
                        } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(_exptype) == i2) {
                            break;
                        }
                    }
                }
                this._currentNode = i;
                if (i != -1) {
                    return -1;
                }
                return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
            }
            i = -1;
            this._currentNode = i;
            if (i != -1) {
            }
        }
    }

    public class FollowingIterator extends InternalAxisIteratorBase {
        DTMAxisTraverser m_traverser;

        public FollowingIterator() {
            super();
            this.m_traverser = DTMDefaultBaseIterators.this.getAxisTraverser(6);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = i;
            this._currentNode = this.m_traverser.first(i);
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            this._currentNode = this.m_traverser.next(this._startNode, this._currentNode);
            return returnNode(i);
        }
    }

    public final class TypedFollowingIterator extends FollowingIterator {
        private final int _nodeType;

        public TypedFollowingIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.FollowingIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i;
            do {
                i = this._currentNode;
                this._currentNode = this.m_traverser.next(this._startNode, this._currentNode);
                if (i == -1 || DTMDefaultBaseIterators.this.getExpandedTypeID(i) == this._nodeType) {
                    break;
                }
            } while (DTMDefaultBaseIterators.this.getNodeType(i) != this._nodeType);
            if (i == -1) {
                return -1;
            }
            return returnNode(i);
        }
    }

    public class AncestorIterator extends InternalAxisIteratorBase {
        NodeVector m_ancestors = new NodeVector();
        int m_ancestorsPos;
        int m_markedPos;
        int m_realStartNode;

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
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            this.m_realStartNode = i;
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = DTMDefaultBaseIterators.this.makeNodeIdentity(i);
            int i2 = -1;
            if (!this._includeSelf && i != -1) {
                makeNodeIdentity = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
                i = DTMDefaultBaseIterators.this.makeNodeHandle(makeNodeIdentity);
            }
            this._startNode = i;
            while (makeNodeIdentity != -1) {
                this.m_ancestors.addElement(i);
                makeNodeIdentity = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
                i = DTMDefaultBaseIterators.this.makeNodeHandle(makeNodeIdentity);
            }
            this.m_ancestorsPos = this.m_ancestors.size() - 1;
            int i3 = this.m_ancestorsPos;
            if (i3 >= 0) {
                i2 = this.m_ancestors.elementAt(i3);
            }
            this._currentNode = i2;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            this.m_ancestorsPos = this.m_ancestors.size() - 1;
            int i = this.m_ancestorsPos;
            this._currentNode = i >= 0 ? this.m_ancestors.elementAt(i) : -1;
            return resetPosition();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this.m_ancestorsPos - 1;
            this.m_ancestorsPos = i2;
            this._currentNode = i2 >= 0 ? this.m_ancestors.elementAt(this.m_ancestorsPos) : -1;
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
            this._currentNode = i >= 0 ? this.m_ancestors.elementAt(i) : -1;
        }
    }

    public final class TypedAncestorIterator extends AncestorIterator {
        private final int _nodeType;

        public TypedAncestorIterator(int i) {
            super();
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.AncestorIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            this.m_realStartNode = i;
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = DTMDefaultBaseIterators.this.makeNodeIdentity(i);
            int i2 = this._nodeType;
            int i3 = -1;
            if (!this._includeSelf && i != -1) {
                makeNodeIdentity = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
            }
            this._startNode = i;
            if (i2 >= 14) {
                while (makeNodeIdentity != -1) {
                    if (DTMDefaultBaseIterators.this._exptype(makeNodeIdentity) == i2) {
                        this.m_ancestors.addElement(DTMDefaultBaseIterators.this.makeNodeHandle(makeNodeIdentity));
                    }
                    makeNodeIdentity = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
                }
            } else {
                while (makeNodeIdentity != -1) {
                    int _exptype = DTMDefaultBaseIterators.this._exptype(makeNodeIdentity);
                    if ((_exptype >= 14 && DTMDefaultBaseIterators.this.m_expandedNameTable.getType(_exptype) == i2) || (_exptype < 14 && _exptype == i2)) {
                        this.m_ancestors.addElement(DTMDefaultBaseIterators.this.makeNodeHandle(makeNodeIdentity));
                    }
                    makeNodeIdentity = DTMDefaultBaseIterators.this._parent(makeNodeIdentity);
                }
            }
            this.m_ancestorsPos = this.m_ancestors.size() - 1;
            if (this.m_ancestorsPos >= 0) {
                i3 = this.m_ancestors.elementAt(this.m_ancestorsPos);
            }
            this._currentNode = i3;
            return resetPosition();
        }
    }

    public class DescendantIterator extends InternalAxisIteratorBase {
        public DescendantIterator() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            int makeNodeIdentity = DTMDefaultBaseIterators.this.makeNodeIdentity(i);
            this._startNode = makeNodeIdentity;
            if (this._includeSelf) {
                makeNodeIdentity--;
            }
            this._currentNode = makeNodeIdentity;
            return resetPosition();
        }

        /* access modifiers changed from: protected */
        public boolean isDescendant(int i) {
            return DTMDefaultBaseIterators.this._parent(i) >= this._startNode || this._startNode == i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._startNode == -1) {
                return -1;
            }
            if (!this._includeSelf || this._currentNode + 1 != this._startNode) {
                int i = this._currentNode;
                while (true) {
                    i++;
                    short _type = DTMDefaultBaseIterators.this._type(i);
                    if (-1 == _type || !isDescendant(i)) {
                        break;
                    } else if (2 != _type && 3 != _type && 13 != _type) {
                        this._currentNode = i;
                        return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
                    }
                }
                this._currentNode = -1;
                return -1;
            }
            DTMDefaultBaseIterators dTMDefaultBaseIterators = DTMDefaultBaseIterators.this;
            int i2 = this._currentNode + 1;
            this._currentNode = i2;
            return returnNode(dTMDefaultBaseIterators.makeNodeHandle(i2));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            boolean z = this._isRestartable;
            this._isRestartable = true;
            setStartNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._startNode));
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

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.DescendantIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            if (this._startNode == -1) {
                return -1;
            }
            int i = this._currentNode;
            do {
                i++;
                short _type = DTMDefaultBaseIterators.this._type(i);
                if (-1 != _type && isDescendant(i)) {
                    if (_type == this._nodeType) {
                        break;
                    }
                } else {
                    this._currentNode = -1;
                    return -1;
                }
            } while (DTMDefaultBaseIterators.this._exptype(i) != this._nodeType);
            this._currentNode = i;
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(i));
        }
    }

    public class NthDescendantIterator extends DescendantIterator {
        int _pos;

        public NthDescendantIterator(int i) {
            super();
            this._pos = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.DescendantIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int makeNodeIdentity;
            int _firstch;
            do {
                int next = super.next();
                if (next == -1) {
                    return -1;
                }
                makeNodeIdentity = DTMDefaultBaseIterators.this.makeNodeIdentity(next);
                _firstch = DTMDefaultBaseIterators.this._firstch(DTMDefaultBaseIterators.this._parent(makeNodeIdentity));
                int i = 0;
                do {
                    if (1 == DTMDefaultBaseIterators.this._type(_firstch)) {
                        i++;
                    }
                    if (i >= this._pos) {
                        break;
                    }
                    _firstch = DTMDefaultBaseIterators.this._nextsib(_firstch);
                } while (_firstch != -1);
                continue;
            } while (makeNodeIdentity != _firstch);
            return makeNodeIdentity;
        }
    }

    public class SingletonIterator extends InternalAxisIteratorBase {
        private boolean _isConstant;

        public SingletonIterator(DTMDefaultBaseIterators dTMDefaultBaseIterators) {
            this(Integer.MIN_VALUE, false);
        }

        public SingletonIterator(DTMDefaultBaseIterators dTMDefaultBaseIterators, int i) {
            this(i, false);
        }

        public SingletonIterator(int i, boolean z) {
            super();
            this._startNode = i;
            this._currentNode = i;
            this._isConstant = z;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator setStartNode(int i) {
            if (i == 0) {
                i = DTMDefaultBaseIterators.this.getDocument();
            }
            if (this._isConstant) {
                this._currentNode = this._startNode;
                return resetPosition();
            } else if (!this._isRestartable) {
                return this;
            } else {
                this._startNode = i;
                this._currentNode = i;
                return resetPosition();
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public DTMAxisIterator reset() {
            if (this._isConstant) {
                this._currentNode = this._startNode;
                return resetPosition();
            }
            boolean z = this._isRestartable;
            this._isRestartable = true;
            setStartNode(this._startNode);
            this._isRestartable = z;
            return this;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            this._currentNode = -1;
            return returnNode(i);
        }
    }

    public final class TypedSingletonIterator extends SingletonIterator {
        private final int _nodeType;

        public TypedSingletonIterator(int i) {
            super(DTMDefaultBaseIterators.this);
            this._nodeType = i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators.SingletonIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
        public int next() {
            int i = this._currentNode;
            int i2 = this._nodeType;
            this._currentNode = -1;
            if (i2 >= 14) {
                if (DTMDefaultBaseIterators.this.getExpandedTypeID(i) == i2) {
                    return returnNode(i);
                }
            } else if (DTMDefaultBaseIterators.this.getNodeType(i) == i2) {
                return returnNode(i);
            }
            return -1;
        }
    }
}

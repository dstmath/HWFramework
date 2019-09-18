package org.apache.xml.dtm.ref;

import javax.xml.transform.Source;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMException;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.XMLStringFactory;
import org.apache.xpath.axes.WalkerFactory;

public abstract class DTMDefaultBaseIterators extends DTMDefaultBaseTraversers {

    public class AncestorIterator extends InternalAxisIteratorBase {
        NodeVector m_ancestors = new NodeVector();
        int m_ancestorsPos;
        int m_markedPos;
        int m_realStartNode;

        public AncestorIterator() {
            super();
        }

        public int getStartNode() {
            return this.m_realStartNode;
        }

        public final boolean isReverse() {
            return true;
        }

        public DTMAxisIterator cloneIterator() {
            this._isRestartable = false;
            try {
                AncestorIterator clone = (AncestorIterator) super.clone();
                clone._startNode = this._startNode;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ITERATOR_CLONE_NOT_SUPPORTED, null));
            }
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            this.m_realStartNode = node;
            if (!this._isRestartable) {
                return this;
            }
            int nodeID = DTMDefaultBaseIterators.this.makeNodeIdentity(node);
            int i = -1;
            if (!this._includeSelf && node != -1) {
                nodeID = DTMDefaultBaseIterators.this._parent(nodeID);
                node = DTMDefaultBaseIterators.this.makeNodeHandle(nodeID);
            }
            this._startNode = node;
            while (nodeID != -1) {
                this.m_ancestors.addElement(node);
                nodeID = DTMDefaultBaseIterators.this._parent(nodeID);
                node = DTMDefaultBaseIterators.this.makeNodeHandle(nodeID);
            }
            this.m_ancestorsPos = this.m_ancestors.size() - 1;
            if (this.m_ancestorsPos >= 0) {
                i = this.m_ancestors.elementAt(this.m_ancestorsPos);
            }
            this._currentNode = i;
            return resetPosition();
        }

        public DTMAxisIterator reset() {
            int i;
            this.m_ancestorsPos = this.m_ancestors.size() - 1;
            if (this.m_ancestorsPos >= 0) {
                i = this.m_ancestors.elementAt(this.m_ancestorsPos);
            } else {
                i = -1;
            }
            this._currentNode = i;
            return resetPosition();
        }

        public int next() {
            int i;
            int next = this._currentNode;
            int pos = this.m_ancestorsPos - 1;
            this.m_ancestorsPos = pos;
            if (pos >= 0) {
                i = this.m_ancestors.elementAt(this.m_ancestorsPos);
            } else {
                i = -1;
            }
            this._currentNode = i;
            return returnNode(next);
        }

        public void setMark() {
            this.m_markedPos = this.m_ancestorsPos;
        }

        public void gotoMark() {
            int i;
            this.m_ancestorsPos = this.m_markedPos;
            if (this.m_ancestorsPos >= 0) {
                i = this.m_ancestors.elementAt(this.m_ancestorsPos);
            } else {
                i = -1;
            }
            this._currentNode = i;
        }
    }

    public final class AttributeIterator extends InternalAxisIteratorBase {
        public AttributeIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = DTMDefaultBaseIterators.this.getFirstAttributeIdentity(DTMDefaultBaseIterators.this.makeNodeIdentity(node));
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            if (node == -1) {
                return -1;
            }
            this._currentNode = DTMDefaultBaseIterators.this.getNextAttributeIdentity(node);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
        }
    }

    public final class ChildrenIterator extends InternalAxisIteratorBase {
        public ChildrenIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            int i = -1;
            if (node != -1) {
                i = DTMDefaultBaseIterators.this._firstch(DTMDefaultBaseIterators.this.makeNodeIdentity(node));
            }
            this._currentNode = i;
            return resetPosition();
        }

        public int next() {
            if (this._currentNode == -1) {
                return -1;
            }
            int node = this._currentNode;
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(node);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
        }
    }

    public class DescendantIterator extends InternalAxisIteratorBase {
        public DescendantIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            int node2 = DTMDefaultBaseIterators.this.makeNodeIdentity(node);
            this._startNode = node2;
            if (this._includeSelf) {
                node2--;
            }
            this._currentNode = node2;
            return resetPosition();
        }

        /* access modifiers changed from: protected */
        public boolean isDescendant(int identity) {
            return DTMDefaultBaseIterators.this._parent(identity) >= this._startNode || this._startNode == identity;
        }

        public int next() {
            if (this._startNode == -1) {
                return -1;
            }
            if (!this._includeSelf || this._currentNode + 1 != this._startNode) {
                int node = this._currentNode;
                while (true) {
                    node++;
                    int type = DTMDefaultBaseIterators.this._type(node);
                    if (-1 == type || !isDescendant(node)) {
                        this._currentNode = -1;
                    } else if (2 != type && 3 != type && 13 != type) {
                        this._currentNode = node;
                        return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
                    }
                }
                this._currentNode = -1;
                return -1;
            }
            DTMDefaultBaseIterators dTMDefaultBaseIterators = DTMDefaultBaseIterators.this;
            int i = this._currentNode + 1;
            this._currentNode = i;
            return returnNode(dTMDefaultBaseIterators.makeNodeHandle(i));
        }

        public DTMAxisIterator reset() {
            boolean temp = this._isRestartable;
            this._isRestartable = true;
            setStartNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._startNode));
            this._isRestartable = temp;
            return this;
        }
    }

    public class FollowingIterator extends InternalAxisIteratorBase {
        DTMAxisTraverser m_traverser;

        public FollowingIterator() {
            super();
            this.m_traverser = DTMDefaultBaseIterators.this.getAxisTraverser(6);
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = this.m_traverser.first(node);
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            this._currentNode = this.m_traverser.next(this._startNode, this._currentNode);
            return returnNode(node);
        }
    }

    public class FollowingSiblingIterator extends InternalAxisIteratorBase {
        public FollowingSiblingIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = DTMDefaultBaseIterators.this.makeNodeIdentity(node);
            return resetPosition();
        }

        public int next() {
            int i = -1;
            if (this._currentNode != -1) {
                i = DTMDefaultBaseIterators.this._nextsib(this._currentNode);
            }
            this._currentNode = i;
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._currentNode));
        }
    }

    public abstract class InternalAxisIteratorBase extends DTMAxisIteratorBase {
        protected int _currentNode;

        public InternalAxisIteratorBase() {
        }

        public void setMark() {
            this._markedNode = this._currentNode;
        }

        public void gotoMark() {
            this._currentNode = this._markedNode;
        }
    }

    public final class NamespaceAttributeIterator extends InternalAxisIteratorBase {
        private final int _nsType;

        public NamespaceAttributeIterator(int nsType) {
            super();
            this._nsType = nsType;
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = DTMDefaultBaseIterators.this.getFirstNamespaceNode(node, false);
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            if (-1 != node) {
                this._currentNode = DTMDefaultBaseIterators.this.getNextNamespaceNode(this._startNode, node, false);
            }
            return returnNode(node);
        }
    }

    public final class NamespaceChildrenIterator extends InternalAxisIteratorBase {
        private final int _nsType;

        public NamespaceChildrenIterator(int type) {
            super();
            this._nsType = type;
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            int i = -1;
            if (node != -1) {
                i = -2;
            }
            this._currentNode = i;
            return resetPosition();
        }

        public int next() {
            int node;
            if (this._currentNode != -1) {
                if (-2 == this._currentNode) {
                    node = DTMDefaultBaseIterators.this._firstch(DTMDefaultBaseIterators.this.makeNodeIdentity(this._startNode));
                } else {
                    node = DTMDefaultBaseIterators.this._nextsib(this._currentNode);
                }
                while (node != -1) {
                    if (DTMDefaultBaseIterators.this.m_expandedNameTable.getNamespaceID(DTMDefaultBaseIterators.this._exptype(node)) == this._nsType) {
                        this._currentNode = node;
                        return returnNode(node);
                    }
                    node = DTMDefaultBaseIterators.this._nextsib(node);
                }
            }
            return -1;
        }
    }

    public class NamespaceIterator extends InternalAxisIteratorBase {
        public NamespaceIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = DTMDefaultBaseIterators.this.getFirstNamespaceNode(node, true);
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            if (-1 != node) {
                this._currentNode = DTMDefaultBaseIterators.this.getNextNamespaceNode(this._startNode, node, true);
            }
            return returnNode(node);
        }
    }

    public class NthDescendantIterator extends DescendantIterator {
        int _pos;

        public NthDescendantIterator(int pos) {
            super();
            this._pos = pos;
        }

        public int next() {
            int node;
            int child;
            int _nextsib;
            do {
                int next = super.next();
                int node2 = next;
                if (next == -1) {
                    return -1;
                }
                node = DTMDefaultBaseIterators.this.makeNodeIdentity(node2);
                child = DTMDefaultBaseIterators.this._firstch(DTMDefaultBaseIterators.this._parent(node));
                int pos = 0;
                do {
                    if (1 == DTMDefaultBaseIterators.this._type(child)) {
                        pos++;
                    }
                    if (pos >= this._pos) {
                        break;
                    }
                    _nextsib = DTMDefaultBaseIterators.this._nextsib(child);
                    child = _nextsib;
                } while (_nextsib != -1);
                continue;
            } while (node != child);
            return node;
        }
    }

    public final class ParentIterator extends InternalAxisIteratorBase {
        private int _nodeType = -1;

        public ParentIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = DTMDefaultBaseIterators.this.getParent(node);
            return resetPosition();
        }

        public DTMAxisIterator setNodeType(int type) {
            this._nodeType = type;
            return this;
        }

        public int next() {
            int result = this._currentNode;
            if (this._nodeType >= 14) {
                if (this._nodeType != DTMDefaultBaseIterators.this.getExpandedTypeID(this._currentNode)) {
                    result = -1;
                }
            } else if (!(this._nodeType == -1 || this._nodeType == DTMDefaultBaseIterators.this.getNodeType(this._currentNode))) {
                result = -1;
            }
            this._currentNode = -1;
            return returnNode(result);
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

        public PrecedingIterator() {
            super();
        }

        public boolean isReverse() {
            return true;
        }

        public DTMAxisIterator cloneIterator() {
            this._isRestartable = false;
            try {
                PrecedingIterator clone = (PrecedingIterator) super.clone();
                int[] stackCopy = new int[this._stack.length];
                System.arraycopy(this._stack, 0, stackCopy, 0, this._stack.length);
                clone._stack = stackCopy;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ITERATOR_CLONE_NOT_SUPPORTED, null));
            }
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            int node2 = DTMDefaultBaseIterators.this.makeNodeIdentity(node);
            if (DTMDefaultBaseIterators.this._type(node2) == 2) {
                node2 = DTMDefaultBaseIterators.this._parent(node2);
            }
            this._startNode = node2;
            int index = 0;
            this._stack[0] = node2;
            int parent = node2;
            while (true) {
                int _parent = DTMDefaultBaseIterators.this._parent(parent);
                parent = _parent;
                if (_parent == -1) {
                    break;
                }
                index++;
                if (index == this._stack.length) {
                    int[] stack = new int[(index + 4)];
                    System.arraycopy(this._stack, 0, stack, 0, index);
                    this._stack = stack;
                }
                this._stack[index] = parent;
            }
            if (index > 0) {
                index--;
            }
            this._currentNode = this._stack[index];
            this._sp = index;
            this._oldsp = index;
            return resetPosition();
        }

        public int next() {
            this._currentNode++;
            while (this._sp >= 0) {
                if (this._currentNode >= this._stack[this._sp]) {
                    this._sp--;
                } else if (!(DTMDefaultBaseIterators.this._type(this._currentNode) == 2 || DTMDefaultBaseIterators.this._type(this._currentNode) == 13)) {
                    return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._currentNode));
                }
                this._currentNode++;
            }
            return -1;
        }

        public DTMAxisIterator reset() {
            this._sp = this._oldsp;
            return resetPosition();
        }

        public void setMark() {
            this._markedsp = this._sp;
            this._markedNode = this._currentNode;
            this._markedDescendant = this._stack[0];
        }

        public void gotoMark() {
            this._sp = this._markedsp;
            this._currentNode = this._markedNode;
        }
    }

    public class PrecedingSiblingIterator extends InternalAxisIteratorBase {
        protected int _startNodeID;

        public PrecedingSiblingIterator() {
            super();
        }

        public boolean isReverse() {
            return true;
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            int makeNodeIdentity = DTMDefaultBaseIterators.this.makeNodeIdentity(node);
            this._startNodeID = makeNodeIdentity;
            int node2 = makeNodeIdentity;
            if (node2 == -1) {
                this._currentNode = node2;
                return resetPosition();
            }
            int type = DTMDefaultBaseIterators.this.m_expandedNameTable.getType(DTMDefaultBaseIterators.this._exptype(node2));
            if (2 == type || 13 == type) {
                this._currentNode = node2;
            } else {
                this._currentNode = DTMDefaultBaseIterators.this._parent(node2);
                if (-1 != this._currentNode) {
                    this._currentNode = DTMDefaultBaseIterators.this._firstch(this._currentNode);
                } else {
                    this._currentNode = node2;
                }
            }
            return resetPosition();
        }

        public int next() {
            if (this._currentNode == this._startNodeID || this._currentNode == -1) {
                return -1;
            }
            int node = this._currentNode;
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(node);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
        }
    }

    public class RootIterator extends InternalAxisIteratorBase {
        public RootIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = DTMDefaultBaseIterators.this.getDocumentRoot(node);
            this._currentNode = -1;
            return resetPosition();
        }

        public int next() {
            if (this._startNode == this._currentNode) {
                return -1;
            }
            this._currentNode = this._startNode;
            return returnNode(this._startNode);
        }
    }

    public class SingletonIterator extends InternalAxisIteratorBase {
        private boolean _isConstant;

        public SingletonIterator(DTMDefaultBaseIterators this$02) {
            this(WalkerFactory.BIT_MATCH_PATTERN, false);
        }

        public SingletonIterator(DTMDefaultBaseIterators this$02, int node) {
            this(node, false);
        }

        public SingletonIterator(int node, boolean constant) {
            super();
            this._startNode = node;
            this._currentNode = node;
            this._isConstant = constant;
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (this._isConstant) {
                this._currentNode = this._startNode;
                return resetPosition();
            } else if (!this._isRestartable) {
                return this;
            } else {
                this._startNode = node;
                this._currentNode = node;
                return resetPosition();
            }
        }

        public DTMAxisIterator reset() {
            if (this._isConstant) {
                this._currentNode = this._startNode;
                return resetPosition();
            }
            boolean temp = this._isRestartable;
            this._isRestartable = true;
            setStartNode(this._startNode);
            this._isRestartable = temp;
            return this;
        }

        public int next() {
            int result = this._currentNode;
            this._currentNode = -1;
            return returnNode(result);
        }
    }

    public final class TypedAncestorIterator extends AncestorIterator {
        private final int _nodeType;

        public TypedAncestorIterator(int type) {
            super();
            this._nodeType = type;
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            this.m_realStartNode = node;
            if (!this._isRestartable) {
                return this;
            }
            int nodeID = DTMDefaultBaseIterators.this.makeNodeIdentity(node);
            int nodeType = this._nodeType;
            int i = -1;
            if (!this._includeSelf && node != -1) {
                nodeID = DTMDefaultBaseIterators.this._parent(nodeID);
            }
            this._startNode = node;
            if (nodeType >= 14) {
                while (nodeID != -1) {
                    if (DTMDefaultBaseIterators.this._exptype(nodeID) == nodeType) {
                        this.m_ancestors.addElement(DTMDefaultBaseIterators.this.makeNodeHandle(nodeID));
                    }
                    nodeID = DTMDefaultBaseIterators.this._parent(nodeID);
                }
            } else {
                while (nodeID != -1) {
                    int eType = DTMDefaultBaseIterators.this._exptype(nodeID);
                    if ((eType >= 14 && DTMDefaultBaseIterators.this.m_expandedNameTable.getType(eType) == nodeType) || (eType < 14 && eType == nodeType)) {
                        this.m_ancestors.addElement(DTMDefaultBaseIterators.this.makeNodeHandle(nodeID));
                    }
                    nodeID = DTMDefaultBaseIterators.this._parent(nodeID);
                }
            }
            this.m_ancestorsPos = this.m_ancestors.size() - 1;
            if (this.m_ancestorsPos >= 0) {
                i = this.m_ancestors.elementAt(this.m_ancestorsPos);
            }
            this._currentNode = i;
            return resetPosition();
        }
    }

    public final class TypedAttributeIterator extends InternalAxisIteratorBase {
        private final int _nodeType;

        public TypedAttributeIterator(int nodeType) {
            super();
            this._nodeType = nodeType;
        }

        public DTMAxisIterator setStartNode(int node) {
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = DTMDefaultBaseIterators.this.getTypedAttribute(node, this._nodeType);
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            this._currentNode = -1;
            return returnNode(node);
        }
    }

    public final class TypedChildrenIterator extends InternalAxisIteratorBase {
        private final int _nodeType;

        public TypedChildrenIterator(int nodeType) {
            super();
            this._nodeType = nodeType;
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = DTMDefaultBaseIterators.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            int i = -1;
            if (node != -1) {
                i = DTMDefaultBaseIterators.this._firstch(DTMDefaultBaseIterators.this.makeNodeIdentity(this._startNode));
            }
            this._currentNode = i;
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            int nodeType = this._nodeType;
            if (nodeType >= 14) {
                while (node != -1 && DTMDefaultBaseIterators.this._exptype(node) != nodeType) {
                    node = DTMDefaultBaseIterators.this._nextsib(node);
                }
            } else {
                while (node != -1) {
                    int eType = DTMDefaultBaseIterators.this._exptype(node);
                    if (eType < 14) {
                        if (eType == nodeType) {
                            break;
                        }
                    } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(eType) == nodeType) {
                        break;
                    }
                    node = DTMDefaultBaseIterators.this._nextsib(node);
                }
            }
            if (node == -1) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(node);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
        }
    }

    public final class TypedDescendantIterator extends DescendantIterator {
        private final int _nodeType;

        public TypedDescendantIterator(int nodeType) {
            super();
            this._nodeType = nodeType;
        }

        public int next() {
            if (this._startNode == -1) {
                return -1;
            }
            int node = this._currentNode;
            do {
                node++;
                int type = DTMDefaultBaseIterators.this._type(node);
                if (-1 != type && isDescendant(node)) {
                    if (type == this._nodeType) {
                        break;
                    }
                } else {
                    this._currentNode = -1;
                    return -1;
                }
            } while (DTMDefaultBaseIterators.this._exptype(node) != this._nodeType);
            this._currentNode = node;
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
        }
    }

    public final class TypedFollowingIterator extends FollowingIterator {
        private final int _nodeType;

        public TypedFollowingIterator(int type) {
            super();
            this._nodeType = type;
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:7:0x0028  */
        public int next() {
            int node;
            do {
                node = this._currentNode;
                this._currentNode = this.m_traverser.next(this._startNode, this._currentNode);
                if (node == -1 || DTMDefaultBaseIterators.this.getExpandedTypeID(node) == this._nodeType) {
                    if (node != -1) {
                        return -1;
                    }
                    return returnNode(node);
                }
            } while (DTMDefaultBaseIterators.this.getNodeType(node) != this._nodeType);
            if (node != -1) {
            }
        }
    }

    public final class TypedFollowingSiblingIterator extends FollowingSiblingIterator {
        private final int _nodeType;

        public TypedFollowingSiblingIterator(int type) {
            super();
            this._nodeType = type;
        }

        public int next() {
            int i = -1;
            if (this._currentNode == -1) {
                return -1;
            }
            int node = this._currentNode;
            int nodeType = this._nodeType;
            if (nodeType < 14) {
                while (true) {
                    int _nextsib = DTMDefaultBaseIterators.this._nextsib(node);
                    node = _nextsib;
                    if (_nextsib == -1) {
                        break;
                    }
                    int eType = DTMDefaultBaseIterators.this._exptype(node);
                    if (eType < 14) {
                        if (eType == nodeType) {
                            break;
                        }
                    } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(eType) == nodeType) {
                        break;
                    }
                }
            } else {
                do {
                    node = DTMDefaultBaseIterators.this._nextsib(node);
                    if (node == -1) {
                        break;
                    }
                } while (DTMDefaultBaseIterators.this._exptype(node) != nodeType);
            }
            this._currentNode = node;
            if (this._currentNode != -1) {
                i = returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(this._currentNode));
            }
            return i;
        }
    }

    public class TypedNamespaceIterator extends NamespaceIterator {
        private final int _nodeType;

        public TypedNamespaceIterator(int nodeType) {
            super();
            this._nodeType = nodeType;
        }

        public int next() {
            int node = this._currentNode;
            while (node != -1) {
                if (DTMDefaultBaseIterators.this.getExpandedTypeID(node) == this._nodeType || DTMDefaultBaseIterators.this.getNodeType(node) == this._nodeType || DTMDefaultBaseIterators.this.getNamespaceType(node) == this._nodeType) {
                    this._currentNode = node;
                    return returnNode(node);
                }
                node = DTMDefaultBaseIterators.this.getNextNamespaceNode(this._startNode, node, true);
            }
            this._currentNode = -1;
            return -1;
        }
    }

    public final class TypedPrecedingIterator extends PrecedingIterator {
        private final int _nodeType;

        public TypedPrecedingIterator(int type) {
            super();
            this._nodeType = type;
        }

        public int next() {
            int node = this._currentNode;
            int nodeType = this._nodeType;
            if (nodeType >= 14) {
                while (true) {
                    node++;
                    if (this._sp < 0) {
                        node = -1;
                        break;
                    } else if (node >= this._stack[this._sp]) {
                        int i = this._sp - 1;
                        this._sp = i;
                        if (i < 0) {
                            node = -1;
                            break;
                        }
                    } else if (DTMDefaultBaseIterators.this._exptype(node) == nodeType) {
                        break;
                    }
                }
            } else {
                while (true) {
                    node++;
                    if (this._sp < 0) {
                        node = -1;
                        break;
                    } else if (node >= this._stack[this._sp]) {
                        int i2 = this._sp - 1;
                        this._sp = i2;
                        if (i2 < 0) {
                            node = -1;
                            break;
                        }
                    } else {
                        int expType = DTMDefaultBaseIterators.this._exptype(node);
                        if (expType < 14) {
                            if (expType == nodeType) {
                                break;
                            }
                        } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(expType) == nodeType) {
                            break;
                        }
                    }
                }
            }
            this._currentNode = node;
            if (node == -1) {
                return -1;
            }
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
        }
    }

    public final class TypedPrecedingSiblingIterator extends PrecedingSiblingIterator {
        private final int _nodeType;

        public TypedPrecedingSiblingIterator(int type) {
            super();
            this._nodeType = type;
        }

        public int next() {
            int node = this._currentNode;
            int nodeType = this._nodeType;
            int startID = this._startNodeID;
            if (nodeType >= 14) {
                while (node != -1 && node != startID && DTMDefaultBaseIterators.this._exptype(node) != nodeType) {
                    node = DTMDefaultBaseIterators.this._nextsib(node);
                }
            } else {
                while (node != -1 && node != startID) {
                    int expType = DTMDefaultBaseIterators.this._exptype(node);
                    if (expType < 14) {
                        if (expType == nodeType) {
                            break;
                        }
                    } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(expType) == nodeType) {
                        break;
                    }
                    node = DTMDefaultBaseIterators.this._nextsib(node);
                }
            }
            if (node == -1 || node == this._startNodeID) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = DTMDefaultBaseIterators.this._nextsib(node);
            return returnNode(DTMDefaultBaseIterators.this.makeNodeHandle(node));
        }
    }

    public class TypedRootIterator extends RootIterator {
        private final int _nodeType;

        public TypedRootIterator(int nodeType) {
            super();
            this._nodeType = nodeType;
        }

        public int next() {
            if (this._startNode == this._currentNode) {
                return -1;
            }
            int nodeType = this._nodeType;
            int node = this._startNode;
            int expType = DTMDefaultBaseIterators.this.getExpandedTypeID(node);
            this._currentNode = node;
            if (nodeType >= 14) {
                if (nodeType == expType) {
                    return returnNode(node);
                }
            } else if (expType < 14) {
                if (expType == nodeType) {
                    return returnNode(node);
                }
            } else if (DTMDefaultBaseIterators.this.m_expandedNameTable.getType(expType) == nodeType) {
                return returnNode(node);
            }
            return -1;
        }
    }

    public final class TypedSingletonIterator extends SingletonIterator {
        private final int _nodeType;

        public TypedSingletonIterator(int nodeType) {
            super(DTMDefaultBaseIterators.this);
            this._nodeType = nodeType;
        }

        public int next() {
            int result = this._currentNode;
            int nodeType = this._nodeType;
            this._currentNode = -1;
            if (nodeType >= 14) {
                if (DTMDefaultBaseIterators.this.getExpandedTypeID(result) == nodeType) {
                    return returnNode(result);
                }
            } else if (DTMDefaultBaseIterators.this.getNodeType(result) == nodeType) {
                return returnNode(result);
            }
            return -1;
        }
    }

    public DTMDefaultBaseIterators(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing);
    }

    public DTMDefaultBaseIterators(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing, int blocksize, boolean usePrevsib, boolean newNameTable) {
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, blocksize, usePrevsib, newNameTable);
    }

    public DTMAxisIterator getTypedAxisIterator(int axis, int type) {
        DTMAxisIterator iterator;
        if (axis != 19) {
            switch (axis) {
                case 0:
                    return new TypedAncestorIterator(type);
                case 1:
                    return new TypedAncestorIterator(type).includeSelf();
                case 2:
                    return new TypedAttributeIterator(type);
                case 3:
                    iterator = new TypedChildrenIterator(type);
                    break;
                case 4:
                    iterator = new TypedDescendantIterator(type);
                    break;
                case 5:
                    iterator = new TypedDescendantIterator(type).includeSelf();
                    break;
                case 6:
                    iterator = new TypedFollowingIterator(type);
                    break;
                case 7:
                    iterator = new TypedFollowingSiblingIterator(type);
                    break;
                default:
                    switch (axis) {
                        case 9:
                            iterator = new TypedNamespaceIterator(type);
                            break;
                        case 10:
                            return new ParentIterator().setNodeType(type);
                        case 11:
                            iterator = new TypedPrecedingIterator(type);
                            break;
                        case 12:
                            iterator = new TypedPrecedingSiblingIterator(type);
                            break;
                        case 13:
                            iterator = new TypedSingletonIterator(type);
                            break;
                        default:
                            throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_TYPED_ITERATOR_AXIS_NOT_IMPLEMENTED, new Object[]{Axis.getNames(axis)}));
                    }
            }
        } else {
            iterator = new TypedRootIterator(type);
        }
        return iterator;
    }

    public DTMAxisIterator getAxisIterator(int axis) {
        DTMAxisIterator iterator;
        if (axis != 19) {
            switch (axis) {
                case 0:
                    return new AncestorIterator();
                case 1:
                    return new AncestorIterator().includeSelf();
                case 2:
                    return new AttributeIterator();
                case 3:
                    iterator = new ChildrenIterator();
                    break;
                case 4:
                    iterator = new DescendantIterator();
                    break;
                case 5:
                    iterator = new DescendantIterator().includeSelf();
                    break;
                case 6:
                    iterator = new FollowingIterator();
                    break;
                case 7:
                    iterator = new FollowingSiblingIterator();
                    break;
                default:
                    switch (axis) {
                        case 9:
                            iterator = new NamespaceIterator();
                            break;
                        case 10:
                            return new ParentIterator();
                        case 11:
                            iterator = new PrecedingIterator();
                            break;
                        case 12:
                            iterator = new PrecedingSiblingIterator();
                            break;
                        case 13:
                            iterator = new SingletonIterator(this);
                            break;
                        default:
                            throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ITERATOR_AXIS_NOT_IMPLEMENTED, new Object[]{Axis.getNames(axis)}));
                    }
            }
        } else {
            iterator = new RootIterator();
        }
        return iterator;
    }
}

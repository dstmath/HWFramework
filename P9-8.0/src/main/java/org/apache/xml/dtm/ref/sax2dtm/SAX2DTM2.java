package org.apache.xml.dtm.ref.sax2dtm;

import java.util.Vector;
import javax.xml.transform.Source;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMException;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.ref.DTMDefaultBaseIterators.InternalAxisIteratorBase;
import org.apache.xml.dtm.ref.DTMDefaultBaseIterators.RootIterator;
import org.apache.xml.dtm.ref.DTMDefaultBaseIterators.SingletonIterator;
import org.apache.xml.dtm.ref.ExtendedType;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringDefault;
import org.apache.xml.utils.XMLStringFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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

    public class AncestorIterator extends InternalAxisIteratorBase {
        private static final int m_blocksize = 32;
        int[] m_ancestors = new int[32];
        int m_ancestorsPos;
        int m_markedPos;
        int m_realStartNode;
        int m_size = 0;

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
            int i = -1;
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            this.m_realStartNode = node;
            if (!this._isRestartable) {
                return this;
            }
            int nodeID = SAX2DTM2.this.makeNodeIdentity(node);
            this.m_size = 0;
            if (nodeID == -1) {
                this._currentNode = -1;
                this.m_ancestorsPos = 0;
                return this;
            }
            if (!this._includeSelf) {
                nodeID = SAX2DTM2.this._parent2(nodeID);
                node = SAX2DTM2.this.makeNodeHandle(nodeID);
            }
            this._startNode = node;
            while (nodeID != -1) {
                if (this.m_size >= this.m_ancestors.length) {
                    int[] newAncestors = new int[(this.m_size * 2)];
                    System.arraycopy(this.m_ancestors, 0, newAncestors, 0, this.m_ancestors.length);
                    this.m_ancestors = newAncestors;
                }
                int[] iArr = this.m_ancestors;
                int i2 = this.m_size;
                this.m_size = i2 + 1;
                iArr[i2] = node;
                nodeID = SAX2DTM2.this._parent2(nodeID);
                node = SAX2DTM2.this.makeNodeHandle(nodeID);
            }
            this.m_ancestorsPos = this.m_size - 1;
            if (this.m_ancestorsPos >= 0) {
                i = this.m_ancestors[this.m_ancestorsPos];
            }
            this._currentNode = i;
            return resetPosition();
        }

        public DTMAxisIterator reset() {
            int i;
            this.m_ancestorsPos = this.m_size - 1;
            if (this.m_ancestorsPos >= 0) {
                i = this.m_ancestors[this.m_ancestorsPos];
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
                i = this.m_ancestors[this.m_ancestorsPos];
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
                i = this.m_ancestors[this.m_ancestorsPos];
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
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = SAX2DTM2.this.getFirstAttributeIdentity(SAX2DTM2.this.makeNodeIdentity(node));
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            if (node == -1) {
                return -1;
            }
            this._currentNode = SAX2DTM2.this.getNextAttributeIdentity(node);
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
        }
    }

    public final class ChildrenIterator extends InternalAxisIteratorBase {
        public ChildrenIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            int i = -1;
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            if (node != -1) {
                i = SAX2DTM2.this._firstch2(SAX2DTM2.this.makeNodeIdentity(node));
            }
            this._currentNode = i;
            return resetPosition();
        }

        public int next() {
            if (this._currentNode == -1) {
                return -1;
            }
            int node = this._currentNode;
            this._currentNode = SAX2DTM2.this._nextsib2(node);
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
        }
    }

    public class DescendantIterator extends InternalAxisIteratorBase {
        public DescendantIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            node = SAX2DTM2.this.makeNodeIdentity(node);
            this._startNode = node;
            if (this._includeSelf) {
                node--;
            }
            this._currentNode = node;
            return resetPosition();
        }

        protected final boolean isDescendant(int identity) {
            return SAX2DTM2.this._parent2(identity) >= this._startNode || this._startNode == identity;
        }

        public int next() {
            int startNode = this._startNode;
            if (startNode == -1) {
                return -1;
            }
            if (this._includeSelf && this._currentNode + 1 == startNode) {
                SAX2DTM2 sax2dtm2 = SAX2DTM2.this;
                int i = this._currentNode + 1;
                this._currentNode = i;
                return returnNode(sax2dtm2.makeNodeHandle(i));
            }
            int node = this._currentNode;
            int type;
            if (startNode == 0) {
                while (true) {
                    node++;
                    int eType = SAX2DTM2.this._exptype2(node);
                    if (-1 == eType) {
                        this._currentNode = -1;
                        return -1;
                    } else if (eType != 3) {
                        type = SAX2DTM2.this.m_extendedTypes[eType].getNodeType();
                        if (!(type == 2 || type == 13)) {
                            break;
                        }
                    }
                }
            } else {
                while (true) {
                    node++;
                    type = SAX2DTM2.this._type2(node);
                    if (-1 != type && (isDescendant(node) ^ 1) == 0) {
                        if (!(2 == type || 3 == type)) {
                            if (13 != type) {
                                break;
                            }
                        }
                    } else {
                        this._currentNode = -1;
                    }
                }
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = node;
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
        }

        public DTMAxisIterator reset() {
            boolean temp = this._isRestartable;
            this._isRestartable = true;
            setStartNode(SAX2DTM2.this.makeNodeHandle(this._startNode));
            this._isRestartable = temp;
            return this;
        }
    }

    public class FollowingIterator extends InternalAxisIteratorBase {
        public FollowingIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            int first;
            this._startNode = node;
            node = SAX2DTM2.this.makeNodeIdentity(node);
            int type = SAX2DTM2.this._type2(node);
            if (2 == type || 13 == type) {
                node = SAX2DTM2.this._parent2(node);
                first = SAX2DTM2.this._firstch2(node);
                if (-1 != first) {
                    this._currentNode = SAX2DTM2.this.makeNodeHandle(first);
                    return resetPosition();
                }
            }
            do {
                first = SAX2DTM2.this._nextsib2(node);
                if (-1 == first) {
                    node = SAX2DTM2.this._parent2(node);
                }
                if (-1 != first) {
                    break;
                }
            } while (-1 != node);
            this._currentNode = SAX2DTM2.this.makeNodeHandle(first);
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            int current = SAX2DTM2.this.makeNodeIdentity(node);
            while (true) {
                current++;
                int type = SAX2DTM2.this._type2(current);
                if (-1 == type) {
                    this._currentNode = -1;
                    return returnNode(node);
                } else if (2 != type && 13 != type) {
                    this._currentNode = SAX2DTM2.this.makeNodeHandle(current);
                    return returnNode(node);
                }
            }
        }
    }

    public class FollowingSiblingIterator extends InternalAxisIteratorBase {
        public FollowingSiblingIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            this._currentNode = SAX2DTM2.this.makeNodeIdentity(node);
            return resetPosition();
        }

        public int next() {
            int i = -1;
            if (this._currentNode != -1) {
                i = SAX2DTM2.this._nextsib2(this._currentNode);
            }
            this._currentNode = i;
            return returnNode(SAX2DTM2.this.makeNodeHandle(this._currentNode));
        }
    }

    public final class ParentIterator extends InternalAxisIteratorBase {
        private int _nodeType = -1;

        public ParentIterator() {
            super();
        }

        public DTMAxisIterator setStartNode(int node) {
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            if (node != -1) {
                this._currentNode = SAX2DTM2.this._parent2(SAX2DTM2.this.makeNodeIdentity(node));
            } else {
                this._currentNode = -1;
            }
            return resetPosition();
        }

        public DTMAxisIterator setNodeType(int type) {
            this._nodeType = type;
            return this;
        }

        public int next() {
            int result = this._currentNode;
            if (result == -1) {
                return -1;
            }
            if (this._nodeType == -1) {
                this._currentNode = -1;
                return returnNode(SAX2DTM2.this.makeNodeHandle(result));
            }
            if (this._nodeType >= 14) {
                if (this._nodeType == SAX2DTM2.this._exptype2(result)) {
                    this._currentNode = -1;
                    return returnNode(SAX2DTM2.this.makeNodeHandle(result));
                }
            } else if (this._nodeType == SAX2DTM2.this._type2(result)) {
                this._currentNode = -1;
                return returnNode(SAX2DTM2.this.makeNodeHandle(result));
            }
            return -1;
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
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            node = SAX2DTM2.this.makeNodeIdentity(node);
            if (SAX2DTM2.this._type2(node) == 2) {
                node = SAX2DTM2.this._parent2(node);
            }
            this._startNode = node;
            int index = 0;
            this._stack[0] = node;
            int parent = node;
            while (true) {
                parent = SAX2DTM2.this._parent2(parent);
                if (parent == -1) {
                    break;
                }
                index++;
                if (index == this._stack.length) {
                    int[] stack = new int[(index * 2)];
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
            int i = this._currentNode;
            while (true) {
                this._currentNode = i + 1;
                if (this._sp < 0) {
                    return -1;
                }
                if (this._currentNode < this._stack[this._sp]) {
                    int type = SAX2DTM2.this._type2(this._currentNode);
                    if (!(type == 2 || type == 13)) {
                        return returnNode(SAX2DTM2.this.makeNodeHandle(this._currentNode));
                    }
                }
                this._sp--;
                i = this._currentNode;
            }
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
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            node = SAX2DTM2.this.makeNodeIdentity(node);
            this._startNodeID = node;
            if (node == -1) {
                this._currentNode = node;
                return resetPosition();
            }
            int type = SAX2DTM2.this._type2(node);
            if (2 == type || 13 == type) {
                this._currentNode = node;
            } else {
                this._currentNode = SAX2DTM2.this._parent2(node);
                if (-1 != this._currentNode) {
                    this._currentNode = SAX2DTM2.this._firstch2(this._currentNode);
                } else {
                    this._currentNode = node;
                }
            }
            return resetPosition();
        }

        public int next() {
            if (this._currentNode == this._startNodeID || this._currentNode == -1) {
                return -1;
            }
            int node = this._currentNode;
            this._currentNode = SAX2DTM2.this._nextsib2(node);
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
        }
    }

    public final class TypedAncestorIterator extends AncestorIterator {
        private final int _nodeType;

        public TypedAncestorIterator(int type) {
            super();
            this._nodeType = type;
        }

        public DTMAxisIterator setStartNode(int node) {
            int i = -1;
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            this.m_realStartNode = node;
            if (!this._isRestartable) {
                return this;
            }
            int nodeID = SAX2DTM2.this.makeNodeIdentity(node);
            this.m_size = 0;
            if (nodeID == -1) {
                this._currentNode = -1;
                this.m_ancestorsPos = 0;
                return this;
            }
            int nodeType = this._nodeType;
            if (!this._includeSelf) {
                nodeID = SAX2DTM2.this._parent2(nodeID);
                node = SAX2DTM2.this.makeNodeHandle(nodeID);
            }
            this._startNode = node;
            int[] newAncestors;
            int[] iArr;
            int i2;
            if (nodeType >= 14) {
                while (nodeID != -1) {
                    if (SAX2DTM2.this._exptype2(nodeID) == nodeType) {
                        if (this.m_size >= this.m_ancestors.length) {
                            newAncestors = new int[(this.m_size * 2)];
                            System.arraycopy(this.m_ancestors, 0, newAncestors, 0, this.m_ancestors.length);
                            this.m_ancestors = newAncestors;
                        }
                        iArr = this.m_ancestors;
                        i2 = this.m_size;
                        this.m_size = i2 + 1;
                        iArr[i2] = SAX2DTM2.this.makeNodeHandle(nodeID);
                    }
                    nodeID = SAX2DTM2.this._parent2(nodeID);
                }
            } else {
                while (nodeID != -1) {
                    int eType = SAX2DTM2.this._exptype2(nodeID);
                    if ((eType < 14 && eType == nodeType) || (eType >= 14 && SAX2DTM2.this.m_extendedTypes[eType].getNodeType() == nodeType)) {
                        if (this.m_size >= this.m_ancestors.length) {
                            newAncestors = new int[(this.m_size * 2)];
                            System.arraycopy(this.m_ancestors, 0, newAncestors, 0, this.m_ancestors.length);
                            this.m_ancestors = newAncestors;
                        }
                        iArr = this.m_ancestors;
                        i2 = this.m_size;
                        this.m_size = i2 + 1;
                        iArr[i2] = SAX2DTM2.this.makeNodeHandle(nodeID);
                    }
                    nodeID = SAX2DTM2.this._parent2(nodeID);
                }
            }
            this.m_ancestorsPos = this.m_size - 1;
            if (this.m_ancestorsPos >= 0) {
                i = this.m_ancestors[this.m_ancestorsPos];
            }
            this._currentNode = i;
            return resetPosition();
        }

        public int getNodeByPosition(int position) {
            if (position <= 0 || position > this.m_size) {
                return -1;
            }
            return this.m_ancestors[position - 1];
        }

        public int getLast() {
            return this.m_size;
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
            this._currentNode = SAX2DTM2.this.getTypedAttribute(node, this._nodeType);
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
            int i = -1;
            if (node == 0) {
                node = SAX2DTM2.this.getDocument();
            }
            if (!this._isRestartable) {
                return this;
            }
            this._startNode = node;
            if (node != -1) {
                i = SAX2DTM2.this._firstch2(SAX2DTM2.this.makeNodeIdentity(this._startNode));
            }
            this._currentNode = i;
            return resetPosition();
        }

        public int next() {
            int node = this._currentNode;
            if (node == -1) {
                return -1;
            }
            int nodeType = this._nodeType;
            if (nodeType != 1) {
                while (node != -1 && SAX2DTM2.this._exptype2(node) != nodeType) {
                    node = SAX2DTM2.this._nextsib2(node);
                }
            } else {
                while (node != -1 && SAX2DTM2.this._exptype2(node) < 14) {
                    node = SAX2DTM2.this._nextsib2(node);
                }
            }
            if (node == -1) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = SAX2DTM2.this._nextsib2(node);
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
        }

        public int getNodeByPosition(int position) {
            if (position <= 0) {
                return -1;
            }
            int node = this._currentNode;
            int pos = 0;
            int nodeType = this._nodeType;
            if (nodeType != 1) {
                while (node != -1) {
                    if (SAX2DTM2.this._exptype2(node) == nodeType) {
                        pos++;
                        if (pos == position) {
                            return SAX2DTM2.this.makeNodeHandle(node);
                        }
                    }
                    node = SAX2DTM2.this._nextsib2(node);
                }
                return -1;
            }
            while (node != -1) {
                if (SAX2DTM2.this._exptype2(node) >= 14) {
                    pos++;
                    if (pos == position) {
                        return SAX2DTM2.this.makeNodeHandle(node);
                    }
                }
                node = SAX2DTM2.this._nextsib2(node);
            }
            return -1;
        }
    }

    public final class TypedDescendantIterator extends DescendantIterator {
        private final int _nodeType;

        public TypedDescendantIterator(int nodeType) {
            super();
            this._nodeType = nodeType;
        }

        public int next() {
            int startNode = this._startNode;
            if (this._startNode == -1) {
                return -1;
            }
            int node = this._currentNode;
            int nodeType = this._nodeType;
            int expType;
            if (nodeType != 1) {
                do {
                    node++;
                    expType = SAX2DTM2.this._exptype2(node);
                    if (-1 == expType || (SAX2DTM2.this._parent2(node) < startNode && startNode != node)) {
                        this._currentNode = -1;
                        return -1;
                    }
                } while (expType != nodeType);
            } else if (startNode == 0) {
                while (true) {
                    node++;
                    expType = SAX2DTM2.this._exptype2(node);
                    if (-1 != expType) {
                        if (expType >= 14) {
                            if (SAX2DTM2.this.m_extendedTypes[expType].getNodeType() == 1) {
                                break;
                            }
                        }
                    } else {
                        this._currentNode = -1;
                        return -1;
                    }
                }
            } else {
                while (true) {
                    node++;
                    expType = SAX2DTM2.this._exptype2(node);
                    if (-1 != expType && (SAX2DTM2.this._parent2(node) >= startNode || startNode == node)) {
                        if (expType >= 14 && SAX2DTM2.this.m_extendedTypes[expType].getNodeType() == 1) {
                            break;
                        }
                    } else {
                        this._currentNode = -1;
                    }
                }
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = node;
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
        }
    }

    public final class TypedFollowingIterator extends FollowingIterator {
        private final int _nodeType;

        public TypedFollowingIterator(int type) {
            super();
            this._nodeType = type;
        }

        /* JADX WARNING: Removed duplicated region for block: B:14:0x003a  */
        /* JADX WARNING: Removed duplicated region for block: B:8:0x0024  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int next() {
            int node;
            int nodeType = this._nodeType;
            int currentNodeID = SAX2DTM2.this.makeNodeIdentity(this._currentNode);
            int current;
            int type;
            if (nodeType < 14) {
                do {
                    node = currentNodeID;
                    current = currentNodeID;
                    while (true) {
                        current++;
                        type = SAX2DTM2.this._type2(current);
                        if (type != -1) {
                            if (2 != type) {
                                if (13 != type) {
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    currentNodeID = type != -1 ? current : -1;
                    if (node == -1 || SAX2DTM2.this._exptype2(node) == nodeType) {
                        break;
                    }
                } while (SAX2DTM2.this._type2(node) != nodeType);
            } else {
                do {
                    node = currentNodeID;
                    current = currentNodeID;
                    while (true) {
                        current++;
                        type = SAX2DTM2.this._type2(current);
                        if (type == -1 || !(2 == type || 13 == type)) {
                            currentNodeID = type == -1 ? current : -1;
                        }
                    }
                    if (type == -1) {
                    }
                    if (node == -1) {
                        break;
                    }
                } while (SAX2DTM2.this._exptype2(node) != nodeType);
            }
            this._currentNode = SAX2DTM2.this.makeNodeHandle(currentNodeID);
            if (node == -1) {
                return -1;
            }
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
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
            if (nodeType == 1) {
                while (true) {
                    node = SAX2DTM2.this._nextsib2(node);
                    if (node == -1 || SAX2DTM2.this._exptype2(node) >= 14) {
                        break;
                    }
                }
            } else {
                do {
                    node = SAX2DTM2.this._nextsib2(node);
                    if (node == -1) {
                        break;
                    }
                } while (SAX2DTM2.this._exptype2(node) != nodeType);
            }
            this._currentNode = node;
            if (node != -1) {
                i = returnNode(SAX2DTM2.this.makeNodeHandle(node));
            }
            return i;
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
            int i;
            if (nodeType >= 14) {
                while (true) {
                    node++;
                    if (this._sp < 0) {
                        node = -1;
                        break;
                    } else if (node >= this._stack[this._sp]) {
                        i = this._sp - 1;
                        this._sp = i;
                        if (i < 0) {
                            node = -1;
                            break;
                        }
                    } else if (SAX2DTM2.this._exptype2(node) == nodeType) {
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
                        i = this._sp - 1;
                        this._sp = i;
                        if (i < 0) {
                            node = -1;
                            break;
                        }
                    } else {
                        int expType = SAX2DTM2.this._exptype2(node);
                        if (expType < 14) {
                            if (expType == nodeType) {
                                break;
                            }
                        } else if (SAX2DTM2.this.m_extendedTypes[expType].getNodeType() == nodeType) {
                            break;
                        }
                    }
                }
            }
            this._currentNode = node;
            if (node == -1) {
                return -1;
            }
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
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
            int startNodeID = this._startNodeID;
            if (nodeType != 1) {
                while (node != -1 && node != startNodeID && SAX2DTM2.this._exptype2(node) != nodeType) {
                    node = SAX2DTM2.this._nextsib2(node);
                }
            } else {
                while (node != -1 && node != startNodeID && SAX2DTM2.this._exptype2(node) < 14) {
                    node = SAX2DTM2.this._nextsib2(node);
                }
            }
            if (node == -1 || node == startNodeID) {
                this._currentNode = -1;
                return -1;
            }
            this._currentNode = SAX2DTM2.this._nextsib2(node);
            return returnNode(SAX2DTM2.this.makeNodeHandle(node));
        }

        public int getLast() {
            if (this._last != -1) {
                return this._last;
            }
            setMark();
            int node = this._currentNode;
            int nodeType = this._nodeType;
            int startNodeID = this._startNodeID;
            int last = 0;
            if (nodeType != 1) {
                while (node != -1 && node != startNodeID) {
                    if (SAX2DTM2.this._exptype2(node) == nodeType) {
                        last++;
                    }
                    node = SAX2DTM2.this._nextsib2(node);
                }
            } else {
                while (node != -1 && node != startNodeID) {
                    if (SAX2DTM2.this._exptype2(node) >= 14) {
                        last++;
                    }
                    node = SAX2DTM2.this._nextsib2(node);
                }
            }
            gotoMark();
            this._last = last;
            return last;
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
            int node = this._startNode;
            int expType = SAX2DTM2.this._exptype2(SAX2DTM2.this.makeNodeIdentity(node));
            this._currentNode = node;
            if (this._nodeType >= 14) {
                if (this._nodeType == expType) {
                    return returnNode(node);
                }
            } else if (expType < 14) {
                if (expType == this._nodeType) {
                    return returnNode(node);
                }
            } else if (SAX2DTM2.this.m_extendedTypes[expType].getNodeType() == this._nodeType) {
                return returnNode(node);
            }
            return -1;
        }
    }

    public final class TypedSingletonIterator extends SingletonIterator {
        private final int _nodeType;

        public TypedSingletonIterator(int nodeType) {
            super(SAX2DTM2.this);
            this._nodeType = nodeType;
        }

        public int next() {
            int result = this._currentNode;
            if (result == -1) {
                return -1;
            }
            this._currentNode = -1;
            if (this._nodeType >= 14) {
                if (SAX2DTM2.this._exptype2(SAX2DTM2.this.makeNodeIdentity(result)) == this._nodeType) {
                    return returnNode(result);
                }
            } else if (SAX2DTM2.this._type2(SAX2DTM2.this.makeNodeIdentity(result)) == this._nodeType) {
                return returnNode(result);
            }
            return -1;
        }
    }

    public SAX2DTM2(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        this(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, 512, true, true, false);
    }

    public SAX2DTM2(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing, int blocksize, boolean usePrevsib, boolean buildIdIndex, boolean newNameTable) {
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, blocksize, usePrevsib, newNameTable);
        this.m_valueIndex = 0;
        this.m_buildIdIndex = true;
        int shift = 0;
        while (true) {
            blocksize >>>= 1;
            if (blocksize != 0) {
                shift++;
            } else {
                this.m_blocksize = 1 << shift;
                this.m_SHIFT = shift;
                this.m_MASK = this.m_blocksize - 1;
                this.m_buildIdIndex = buildIdIndex;
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

    public final int _exptype(int identity) {
        return this.m_exptype.elementAt(identity);
    }

    public final int _exptype2(int identity) {
        if (identity < this.m_blocksize) {
            return this.m_exptype_map0[identity];
        }
        return this.m_exptype_map[identity >>> this.m_SHIFT][this.m_MASK & identity];
    }

    public final int _nextsib2(int identity) {
        if (identity < this.m_blocksize) {
            return this.m_nextsib_map0[identity];
        }
        return this.m_nextsib_map[identity >>> this.m_SHIFT][this.m_MASK & identity];
    }

    public final int _firstch2(int identity) {
        if (identity < this.m_blocksize) {
            return this.m_firstch_map0[identity];
        }
        return this.m_firstch_map[identity >>> this.m_SHIFT][this.m_MASK & identity];
    }

    public final int _parent2(int identity) {
        if (identity < this.m_blocksize) {
            return this.m_parent_map0[identity];
        }
        return this.m_parent_map[identity >>> this.m_SHIFT][this.m_MASK & identity];
    }

    public final int _type2(int identity) {
        int eType;
        if (identity < this.m_blocksize) {
            eType = this.m_exptype_map0[identity];
        } else {
            eType = this.m_exptype_map[identity >>> this.m_SHIFT][this.m_MASK & identity];
        }
        if (-1 != eType) {
            return this.m_extendedTypes[eType].getNodeType();
        }
        return -1;
    }

    public final int getExpandedTypeID2(int nodeHandle) {
        int nodeID = makeNodeIdentity(nodeHandle);
        if (nodeID == -1) {
            return -1;
        }
        if (nodeID < this.m_blocksize) {
            return this.m_exptype_map0[nodeID];
        }
        return this.m_exptype_map[nodeID >>> this.m_SHIFT][this.m_MASK & nodeID];
    }

    public final int _exptype2Type(int exptype) {
        if (-1 != exptype) {
            return this.m_extendedTypes[exptype].getNodeType();
        }
        return -1;
    }

    public int getIdForNamespace(String uri) {
        int index = this.m_values.indexOf(uri);
        if (index >= 0) {
            return index;
        }
        this.m_values.addElement(uri);
        int i = this.m_valueIndex;
        this.m_valueIndex = i + 1;
        return i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0147  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0167  */
    /* JADX WARNING: Missing block: B:25:0x012a, code:
            if (r17.startsWith(org.apache.xalan.templates.Constants.ATTRNAME_XMLNS) != false) goto L_0x012c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        int val;
        int i;
        charactersFlush();
        int exName = this.m_expandedNameTable.getExpandedTypeID(uri, localName, 1);
        int elemNode = addNode(1, exName, this.m_parents.peek(), this.m_previous, qName.length() != localName.length() ? this.m_valuesOrPrefixes.stringToIndex(qName) : 0, true);
        if (this.m_indexing) {
            indexNode(exName, elemNode);
        }
        this.m_parents.push(elemNode);
        int startDecls = this.m_contextIndexes.peek();
        int nDecls = this.m_prefixMappings.size();
        if (!this.m_pastFirstElement) {
            exName = this.m_expandedNameTable.getExpandedTypeID(null, "xml", 13);
            this.m_values.addElement("http://www.w3.org/XML/1998/namespace");
            val = this.m_valueIndex;
            this.m_valueIndex = val + 1;
            addNode(13, exName, elemNode, -1, val, false);
            this.m_pastFirstElement = true;
        }
        for (i = startDecls; i < nDecls; i += 2) {
            String prefix = (String) this.m_prefixMappings.elementAt(i);
            if (prefix != null) {
                String declURL = (String) this.m_prefixMappings.elementAt(i + 1);
                exName = this.m_expandedNameTable.getExpandedTypeID(null, prefix, 13);
                this.m_values.addElement(declURL);
                val = this.m_valueIndex;
                this.m_valueIndex = val + 1;
                addNode(13, exName, elemNode, -1, val, false);
            }
        }
        int n = attributes.getLength();
        i = 0;
        while (i < n) {
            int nodeType;
            String attrUri = attributes.getURI(i);
            String attrQName = attributes.getQName(i);
            String valString = attributes.getValue(i);
            String attrLocalName = attributes.getLocalName(i);
            if (attrQName != null) {
                if (!attrQName.equals("xmlns")) {
                }
                if (declAlreadyDeclared(getPrefix(attrQName, attrUri))) {
                    i++;
                } else {
                    nodeType = 13;
                    if (valString == null) {
                        valString = "";
                    }
                    this.m_values.addElement(valString);
                    val = this.m_valueIndex;
                    this.m_valueIndex = val + 1;
                    if (attrLocalName.length() != attrQName.length()) {
                        int prefixIndex = this.m_valuesOrPrefixes.stringToIndex(attrQName);
                        int dataIndex = this.m_data.size();
                        this.m_data.addElement(prefixIndex);
                        this.m_data.addElement(val);
                        val = -dataIndex;
                    }
                    addNode(nodeType, this.m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType), elemNode, -1, val, false);
                    i++;
                }
            }
            nodeType = 2;
            if (this.m_buildIdIndex && attributes.getType(i).equalsIgnoreCase("ID")) {
                setIDAttribute(valString, elemNode);
            }
            if (valString == null) {
            }
            this.m_values.addElement(valString);
            val = this.m_valueIndex;
            this.m_valueIndex = val + 1;
            if (attrLocalName.length() != attrQName.length()) {
            }
            addNode(nodeType, this.m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType), elemNode, -1, val, false);
            i++;
        }
        if (this.m_wsfilter != null) {
            short wsv = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode), this);
            boolean shouldStrip = (short) 3 == wsv ? getShouldStripWhitespace() : (short) 2 == wsv;
            pushShouldStripWhitespace(shouldStrip);
        }
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        charactersFlush();
        this.m_contextIndexes.quickPop(1);
        int topContextIndex = this.m_contextIndexes.peek();
        if (topContextIndex != this.m_prefixMappings.size()) {
            this.m_prefixMappings.setSize(topContextIndex);
        }
        this.m_previous = this.m_parents.pop();
        popShouldStripWhitespace();
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (!this.m_insideDTD) {
            charactersFlush();
            this.m_values.addElement(new String(ch, start, length));
            int dataIndex = this.m_valueIndex;
            this.m_valueIndex = dataIndex + 1;
            this.m_previous = addNode(8, 8, this.m_parents.peek(), this.m_previous, dataIndex, false);
        }
    }

    public void startDocument() throws SAXException {
        this.m_parents.push(addNode(9, 9, -1, -1, 0, true));
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

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

    protected final int addNode(int type, int expandedTypeID, int parentIndex, int previousSibling, int dataOrPrefix, boolean canHaveFirstChild) {
        int nodeIndex = this.m_size;
        this.m_size = nodeIndex + 1;
        if (nodeIndex == this.m_maxNodeIndex) {
            addNewDTMID(nodeIndex);
            this.m_maxNodeIndex += 65536;
        }
        this.m_firstch.addElement(-1);
        this.m_nextsib.addElement(-1);
        this.m_parent.addElement(parentIndex);
        this.m_exptype.addElement(expandedTypeID);
        this.m_dataOrQName.addElement(dataOrPrefix);
        if (this.m_prevsib != null) {
            this.m_prevsib.addElement(previousSibling);
        }
        if (this.m_locator != null && this.m_useSourceLocationProperty) {
            setSourceLocation();
        }
        switch (type) {
            case 2:
                break;
            case 13:
                declareNamespaceInContext(parentIndex, nodeIndex);
                break;
            default:
                if (-1 == previousSibling) {
                    if (-1 != parentIndex) {
                        this.m_firstch.setElementAt(nodeIndex, parentIndex);
                        break;
                    }
                }
                this.m_nextsib.setElementAt(nodeIndex, previousSibling);
                break;
                break;
        }
        return nodeIndex;
    }

    protected final void charactersFlush() {
        if (this.m_textPendingStart >= 0) {
            int length = this.m_chars.size() - this.m_textPendingStart;
            boolean doStrip = false;
            if (getShouldStripWhitespace()) {
                doStrip = this.m_chars.isWhitespace(this.m_textPendingStart, length);
            }
            if (doStrip) {
                this.m_chars.setLength(this.m_textPendingStart);
            } else if (length > 0) {
                if (length > TEXT_LENGTH_MAX || this.m_textPendingStart > TEXT_OFFSET_MAX) {
                    this.m_previous = addNode(this.m_coalescedTextType, 3, this.m_parents.peek(), this.m_previous, -this.m_data.size(), false);
                    this.m_data.addElement(this.m_textPendingStart);
                    this.m_data.addElement(length);
                } else {
                    this.m_previous = addNode(this.m_coalescedTextType, 3, this.m_parents.peek(), this.m_previous, length + (this.m_textPendingStart << 10), false);
                }
            }
            this.m_textPendingStart = -1;
            this.m_coalescedTextType = 3;
            this.m_textType = 3;
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        charactersFlush();
        int i = 7;
        this.m_previous = addNode(7, i, this.m_parents.peek(), this.m_previous, -this.m_data.size(), false);
        this.m_data.addElement(this.m_valuesOrPrefixes.stringToIndex(target));
        this.m_values.addElement(data);
        SuballocatedIntVector suballocatedIntVector = this.m_data;
        int i2 = this.m_valueIndex;
        this.m_valueIndex = i2 + 1;
        suballocatedIntVector.addElement(i2);
    }

    public final int getFirstAttribute(int nodeHandle) {
        int nodeID = makeNodeIdentity(nodeHandle);
        if (nodeID == -1 || 1 != _type2(nodeID)) {
            return -1;
        }
        int type;
        do {
            nodeID++;
            type = _type2(nodeID);
            if (type == 2) {
                return makeNodeHandle(nodeID);
            }
        } while (13 == type);
        return -1;
    }

    protected int getFirstAttributeIdentity(int identity) {
        if (identity == -1 || 1 != _type2(identity)) {
            return -1;
        }
        int type;
        do {
            identity++;
            type = _type2(identity);
            if (type == 2) {
                return identity;
            }
        } while (13 == type);
        return -1;
    }

    protected int getNextAttributeIdentity(int identity) {
        int type;
        do {
            identity++;
            type = _type2(identity);
            if (type == 2) {
                return identity;
            }
        } while (type == 13);
        return -1;
    }

    protected final int getTypedAttribute(int nodeHandle, int attType) {
        int nodeID = makeNodeIdentity(nodeHandle);
        if (nodeID != -1 && 1 == _type2(nodeID)) {
            while (true) {
                nodeID++;
                int expType = _exptype2(nodeID);
                if (expType == -1) {
                    return -1;
                }
                int type = this.m_extendedTypes[expType].getNodeType();
                if (type == 2) {
                    if (expType == attType) {
                        return makeNodeHandle(nodeID);
                    }
                } else if (13 != type) {
                    break;
                }
            }
        }
        return -1;
    }

    public String getLocalName(int nodeHandle) {
        int expType = _exptype(makeNodeIdentity(nodeHandle));
        if (expType != 7) {
            return this.m_expandedNameTable.getLocalName(expType);
        }
        return this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-_dataOrQName(makeNodeIdentity(nodeHandle))));
    }

    public final String getNodeNameX(int nodeHandle) {
        int nodeID = makeNodeIdentity(nodeHandle);
        int eType = _exptype2(nodeID);
        if (eType == 7) {
            return this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-_dataOrQName(nodeID)));
        }
        ExtendedType extType = this.m_extendedTypes[eType];
        if (extType.getNamespace().length() == 0) {
            return extType.getLocalName();
        }
        int qnameIndex = this.m_dataOrQName.elementAt(nodeID);
        if (qnameIndex == 0) {
            return extType.getLocalName();
        }
        if (qnameIndex < 0) {
            qnameIndex = this.m_data.elementAt(-qnameIndex);
        }
        return this.m_valuesOrPrefixes.indexToString(qnameIndex);
    }

    public String getNodeName(int nodeHandle) {
        int nodeID = makeNodeIdentity(nodeHandle);
        ExtendedType extType = this.m_extendedTypes[_exptype2(nodeID)];
        if (extType.getNamespace().length() == 0) {
            int type = extType.getNodeType();
            String localName = extType.getLocalName();
            if (type == 13) {
                if (localName.length() == 0) {
                    return "xmlns";
                }
                return Constants.ATTRNAME_XMLNS + localName;
            } else if (type == 7) {
                return this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-_dataOrQName(nodeID)));
            } else if (localName.length() == 0) {
                return getFixedNames(type);
            } else {
                return localName;
            }
        }
        int qnameIndex = this.m_dataOrQName.elementAt(nodeID);
        if (qnameIndex == 0) {
            return extType.getLocalName();
        }
        if (qnameIndex < 0) {
            qnameIndex = this.m_data.elementAt(-qnameIndex);
        }
        return this.m_valuesOrPrefixes.indexToString(qnameIndex);
    }

    public XMLString getStringValue(int nodeHandle) {
        int identity = makeNodeIdentity(nodeHandle);
        if (identity == -1) {
            return EMPTY_XML_STR;
        }
        int type = _type2(identity);
        int dataIndex;
        if (type == 1 || type == 9) {
            int startNode = identity;
            identity = _firstch2(identity);
            if (-1 == identity) {
                return EMPTY_XML_STR;
            }
            int offset = -1;
            int length = 0;
            do {
                type = _exptype2(identity);
                if (type == 3 || type == 4) {
                    dataIndex = this.m_dataOrQName.elementAt(identity);
                    if (dataIndex >= 0) {
                        if (-1 == offset) {
                            offset = dataIndex >>> 10;
                        }
                        length += dataIndex & TEXT_LENGTH_MAX;
                    } else {
                        if (-1 == offset) {
                            offset = this.m_data.elementAt(-dataIndex);
                        }
                        length += this.m_data.elementAt((-dataIndex) + 1);
                    }
                }
                identity++;
            } while (_parent2(identity) >= startNode);
            if (length <= 0) {
                return EMPTY_XML_STR;
            }
            if (this.m_xstrf != null) {
                return this.m_xstrf.newstr(this.m_chars, offset, length);
            }
            return new XMLStringDefault(this.m_chars.getString(offset, length));
        } else if (3 == type || 4 == type) {
            dataIndex = this.m_dataOrQName.elementAt(identity);
            if (dataIndex >= 0) {
                if (this.m_xstrf != null) {
                    return this.m_xstrf.newstr(this.m_chars, dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX);
                }
                return new XMLStringDefault(this.m_chars.getString(dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX));
            } else if (this.m_xstrf != null) {
                return this.m_xstrf.newstr(this.m_chars, this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1));
            } else {
                return new XMLStringDefault(this.m_chars.getString(this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1)));
            }
        } else {
            dataIndex = this.m_dataOrQName.elementAt(identity);
            if (dataIndex < 0) {
                dataIndex = this.m_data.elementAt((-dataIndex) + 1);
            }
            if (this.m_xstrf != null) {
                return this.m_xstrf.newstr((String) this.m_values.elementAt(dataIndex));
            }
            return new XMLStringDefault((String) this.m_values.elementAt(dataIndex));
        }
    }

    public final String getStringValueX(int nodeHandle) {
        int identity = makeNodeIdentity(nodeHandle);
        if (identity == -1) {
            return "";
        }
        int type = _type2(identity);
        int dataIndex;
        if (type == 1 || type == 9) {
            int startNode = identity;
            identity = _firstch2(identity);
            if (-1 == identity) {
                return "";
            }
            int offset = -1;
            int length = 0;
            do {
                type = _exptype2(identity);
                if (type == 3 || type == 4) {
                    dataIndex = this.m_dataOrQName.elementAt(identity);
                    if (dataIndex >= 0) {
                        if (-1 == offset) {
                            offset = dataIndex >>> 10;
                        }
                        length += dataIndex & TEXT_LENGTH_MAX;
                    } else {
                        if (-1 == offset) {
                            offset = this.m_data.elementAt(-dataIndex);
                        }
                        length += this.m_data.elementAt((-dataIndex) + 1);
                    }
                }
                identity++;
            } while (_parent2(identity) >= startNode);
            if (length > 0) {
                return this.m_chars.getString(offset, length);
            }
            return "";
        } else if (3 == type || 4 == type) {
            dataIndex = this.m_dataOrQName.elementAt(identity);
            if (dataIndex >= 0) {
                return this.m_chars.getString(dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX);
            }
            return this.m_chars.getString(this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1));
        } else {
            dataIndex = this.m_dataOrQName.elementAt(identity);
            if (dataIndex < 0) {
                dataIndex = this.m_data.elementAt((-dataIndex) + 1);
            }
            return (String) this.m_values.elementAt(dataIndex);
        }
    }

    public String getStringValue() {
        int child = _firstch2(0);
        if (child == -1) {
            return "";
        }
        if (_exptype2(child) != 3 || _nextsib2(child) != -1) {
            return getStringValueX(getDocument());
        }
        int dataIndex = this.m_dataOrQName.elementAt(child);
        if (dataIndex >= 0) {
            return this.m_chars.getString(dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX);
        }
        return this.m_chars.getString(this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1));
    }

    public final void dispatchCharactersEvents(int nodeHandle, ContentHandler ch, boolean normalize) throws SAXException {
        int identity = makeNodeIdentity(nodeHandle);
        if (identity != -1) {
            int type = _type2(identity);
            int dataIndex;
            if (type == 1 || type == 9) {
                int startNode = identity;
                identity = _firstch2(identity);
                if (-1 != identity) {
                    int offset = -1;
                    int length = 0;
                    do {
                        type = _exptype2(identity);
                        if (type == 3 || type == 4) {
                            dataIndex = this.m_dataOrQName.elementAt(identity);
                            if (dataIndex >= 0) {
                                if (-1 == offset) {
                                    offset = dataIndex >>> 10;
                                }
                                length += dataIndex & TEXT_LENGTH_MAX;
                            } else {
                                if (-1 == offset) {
                                    offset = this.m_data.elementAt(-dataIndex);
                                }
                                length += this.m_data.elementAt((-dataIndex) + 1);
                            }
                        }
                        identity++;
                    } while (_parent2(identity) >= startNode);
                    if (length > 0) {
                        if (normalize) {
                            this.m_chars.sendNormalizedSAXcharacters(ch, offset, length);
                        } else {
                            this.m_chars.sendSAXcharacters(ch, offset, length);
                        }
                    }
                }
            } else if (3 == type || 4 == type) {
                dataIndex = this.m_dataOrQName.elementAt(identity);
                if (dataIndex >= 0) {
                    if (normalize) {
                        this.m_chars.sendNormalizedSAXcharacters(ch, dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX);
                    } else {
                        this.m_chars.sendSAXcharacters(ch, dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX);
                    }
                } else if (normalize) {
                    this.m_chars.sendNormalizedSAXcharacters(ch, this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1));
                } else {
                    this.m_chars.sendSAXcharacters(ch, this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1));
                }
            } else {
                dataIndex = this.m_dataOrQName.elementAt(identity);
                if (dataIndex < 0) {
                    dataIndex = this.m_data.elementAt((-dataIndex) + 1);
                }
                String str = (String) this.m_values.elementAt(dataIndex);
                if (normalize) {
                    FastStringBuffer.sendNormalizedSAXcharacters(str.toCharArray(), 0, str.length(), ch);
                } else {
                    ch.characters(str.toCharArray(), 0, str.length());
                }
            }
        }
    }

    public String getNodeValue(int nodeHandle) {
        int identity = makeNodeIdentity(nodeHandle);
        int type = _type2(identity);
        int dataIndex;
        if (type == 3 || type == 4) {
            dataIndex = _dataOrQName(identity);
            if (dataIndex > 0) {
                return this.m_chars.getString(dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX);
            }
            return this.m_chars.getString(this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1));
        } else if (1 == type || 11 == type || 9 == type) {
            return null;
        } else {
            dataIndex = this.m_dataOrQName.elementAt(identity);
            if (dataIndex < 0) {
                dataIndex = this.m_data.elementAt((-dataIndex) + 1);
            }
            return (String) this.m_values.elementAt(dataIndex);
        }
    }

    protected final void copyTextNode(int nodeID, SerializationHandler handler) throws SAXException {
        if (nodeID != -1) {
            int dataIndex = this.m_dataOrQName.elementAt(nodeID);
            if (dataIndex >= 0) {
                this.m_chars.sendSAXcharacters(handler, dataIndex >>> 10, dataIndex & TEXT_LENGTH_MAX);
            } else {
                this.m_chars.sendSAXcharacters(handler, this.m_data.elementAt(-dataIndex), this.m_data.elementAt((-dataIndex) + 1));
            }
        }
    }

    protected final String copyElement(int nodeID, int exptype, SerializationHandler handler) throws SAXException {
        ExtendedType extType = this.m_extendedTypes[exptype];
        String uri = extType.getNamespace();
        String name = extType.getLocalName();
        if (uri.length() == 0) {
            handler.startElement(name);
            return name;
        }
        int qnameIndex = this.m_dataOrQName.elementAt(nodeID);
        if (qnameIndex == 0) {
            handler.startElement(name);
            handler.namespaceAfterStartElement("", uri);
            return name;
        }
        String prefix;
        if (qnameIndex < 0) {
            qnameIndex = this.m_data.elementAt(-qnameIndex);
        }
        String qName = this.m_valuesOrPrefixes.indexToString(qnameIndex);
        handler.startElement(qName);
        int prefixIndex = qName.indexOf(58);
        if (prefixIndex > 0) {
            prefix = qName.substring(0, prefixIndex);
        } else {
            prefix = null;
        }
        handler.namespaceAfterStartElement(prefix, uri);
        return qName;
    }

    protected final void copyNS(int nodeID, SerializationHandler handler, boolean inScope) throws SAXException {
        if (this.m_namespaceDeclSetElements == null || this.m_namespaceDeclSetElements.size() != 1 || this.m_namespaceDeclSets == null || ((SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(0)).size() != 1) {
            int nextNSNode;
            SuballocatedIntVector nsContext = null;
            if (inScope) {
                nsContext = findNamespaceContext(nodeID);
                if (nsContext != null && nsContext.size() >= 1) {
                    nextNSNode = makeNodeIdentity(nsContext.elementAt(0));
                } else {
                    return;
                }
            }
            nextNSNode = getNextNamespaceNode2(nodeID);
            int nsIndex = 1;
            while (nextNSNode != -1) {
                String nodeName = this.m_extendedTypes[_exptype2(nextNSNode)].getLocalName();
                int dataIndex = this.m_dataOrQName.elementAt(nextNSNode);
                if (dataIndex < 0) {
                    dataIndex = this.m_data.elementAt((-dataIndex) + 1);
                }
                handler.namespaceAfterStartElement(nodeName, (String) this.m_values.elementAt(dataIndex));
                if (!inScope) {
                    nextNSNode = getNextNamespaceNode2(nextNSNode);
                } else if (nsIndex < nsContext.size()) {
                    nextNSNode = makeNodeIdentity(nsContext.elementAt(nsIndex));
                    nsIndex++;
                } else {
                    return;
                }
            }
        }
    }

    protected final int getNextNamespaceNode2(int baseID) {
        int type;
        do {
            baseID++;
            type = _type2(baseID);
        } while (type == 2);
        if (type == 13) {
            return baseID;
        }
        return -1;
    }

    protected final void copyAttributes(int nodeID, SerializationHandler handler) throws SAXException {
        int current = getFirstAttributeIdentity(nodeID);
        while (current != -1) {
            copyAttribute(current, _exptype2(current), handler);
            current = getNextAttributeIdentity(current);
        }
    }

    protected final void copyAttribute(int nodeID, int exptype, SerializationHandler handler) throws SAXException {
        ExtendedType extType = this.m_extendedTypes[exptype];
        String uri = extType.getNamespace();
        String localName = extType.getLocalName();
        String prefix = null;
        String qname = null;
        int dataIndex = _dataOrQName(nodeID);
        int valueIndex = dataIndex;
        if (dataIndex <= 0) {
            int prefixIndex = this.m_data.elementAt(-dataIndex);
            valueIndex = this.m_data.elementAt((-dataIndex) + 1);
            qname = this.m_valuesOrPrefixes.indexToString(prefixIndex);
            int colonIndex = qname.indexOf(58);
            if (colonIndex > 0) {
                prefix = qname.substring(0, colonIndex);
            }
        }
        if (uri.length() != 0) {
            handler.namespaceAfterStartElement(prefix, uri);
        }
        handler.addAttribute(prefix != null ? qname : localName, (String) this.m_values.elementAt(valueIndex));
    }
}

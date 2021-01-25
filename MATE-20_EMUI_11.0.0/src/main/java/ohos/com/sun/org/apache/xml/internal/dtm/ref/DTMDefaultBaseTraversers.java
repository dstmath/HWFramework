package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.Axis;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMException;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.javax.xml.transform.Source;

public abstract class DTMDefaultBaseTraversers extends DTMDefaultBase {
    public DTMDefaultBaseTraversers(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z) {
        super(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z);
    }

    public DTMDefaultBaseTraversers(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z, int i2, boolean z2, boolean z3) {
        super(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z, i2, z2, z3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisTraverser getAxisTraverser(int i) {
        DTMAxisTraverser dTMAxisTraverser;
        if (this.m_traversers == null) {
            this.m_traversers = new DTMAxisTraverser[Axis.getNamesLength()];
        } else {
            DTMAxisTraverser dTMAxisTraverser2 = this.m_traversers[i];
            if (dTMAxisTraverser2 != null) {
                return dTMAxisTraverser2;
            }
        }
        switch (i) {
            case 0:
                dTMAxisTraverser = new AncestorTraverser();
                break;
            case 1:
                dTMAxisTraverser = new AncestorOrSelfTraverser();
                break;
            case 2:
                dTMAxisTraverser = new AttributeTraverser();
                break;
            case 3:
                dTMAxisTraverser = new ChildTraverser();
                break;
            case 4:
                dTMAxisTraverser = new DescendantTraverser();
                break;
            case 5:
                dTMAxisTraverser = new DescendantOrSelfTraverser();
                break;
            case 6:
                dTMAxisTraverser = new FollowingTraverser();
                break;
            case 7:
                dTMAxisTraverser = new FollowingSiblingTraverser();
                break;
            case 8:
                dTMAxisTraverser = new NamespaceDeclsTraverser();
                break;
            case 9:
                dTMAxisTraverser = new NamespaceTraverser();
                break;
            case 10:
                dTMAxisTraverser = new ParentTraverser();
                break;
            case 11:
                dTMAxisTraverser = new PrecedingTraverser();
                break;
            case 12:
                dTMAxisTraverser = new PrecedingSiblingTraverser();
                break;
            case 13:
                dTMAxisTraverser = new SelfTraverser();
                break;
            case 14:
                dTMAxisTraverser = new AllFromNodeTraverser();
                break;
            case 15:
                dTMAxisTraverser = new PrecedingAndAncestorTraverser();
                break;
            case 16:
                dTMAxisTraverser = new AllFromRootTraverser();
                break;
            case 17:
                dTMAxisTraverser = new DescendantFromRootTraverser();
                break;
            case 18:
                dTMAxisTraverser = new DescendantOrSelfFromRootTraverser();
                break;
            case 19:
                dTMAxisTraverser = new RootTraverser();
                break;
            case 20:
                return null;
            default:
                throw new DTMException(XMLMessages.createXMLMessage("ER_UNKNOWN_AXIS_TYPE", new Object[]{Integer.toString(i)}));
        }
        this.m_traversers[i] = dTMAxisTraverser;
        return dTMAxisTraverser;
    }

    private class AncestorTraverser extends DTMAxisTraverser {
        private AncestorTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return DTMDefaultBaseTraversers.this.getParent(i2);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2);
            do {
                makeNodeIdentity = DTMDefaultBaseTraversers.this.m_parent.elementAt(makeNodeIdentity);
                if (-1 == makeNodeIdentity) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.m_exptype.elementAt(makeNodeIdentity) != i3);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
        }
    }

    private class AncestorOrSelfTraverser extends AncestorTraverser {
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            return i;
        }

        private AncestorOrSelfTraverser() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            return DTMDefaultBaseTraversers.this.getExpandedTypeID(i) == i2 ? i : next(i, i, i2);
        }
    }

    private class AttributeTraverser extends DTMAxisTraverser {
        private AttributeTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return i == i2 ? DTMDefaultBaseTraversers.this.getFirstAttribute(i) : DTMDefaultBaseTraversers.this.getNextAttribute(i2);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int firstAttribute = i == i2 ? DTMDefaultBaseTraversers.this.getFirstAttribute(i) : DTMDefaultBaseTraversers.this.getNextAttribute(i2);
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(firstAttribute) != i3) {
                firstAttribute = DTMDefaultBaseTraversers.this.getNextAttribute(firstAttribute);
                if (-1 == firstAttribute) {
                    return -1;
                }
            }
            return firstAttribute;
        }
    }

    private class ChildTraverser extends DTMAxisTraverser {
        private ChildTraverser() {
        }

        /* access modifiers changed from: protected */
        public int getNextIndexed(int i, int i2, int i3) {
            int namespaceID = DTMDefaultBaseTraversers.this.m_expandedNameTable.getNamespaceID(i3);
            int localNameID = DTMDefaultBaseTraversers.this.m_expandedNameTable.getLocalNameID(i3);
            while (true) {
                int findElementFromIndex = DTMDefaultBaseTraversers.this.findElementFromIndex(namespaceID, localNameID, i2);
                if (-2 != findElementFromIndex) {
                    int elementAt = DTMDefaultBaseTraversers.this.m_parent.elementAt(findElementFromIndex);
                    if (elementAt == i) {
                        return findElementFromIndex;
                    }
                    if (elementAt < i) {
                        return -1;
                    }
                    do {
                        elementAt = DTMDefaultBaseTraversers.this.m_parent.elementAt(elementAt);
                        if (elementAt < i) {
                            return -1;
                        }
                    } while (elementAt > i);
                    i2 = findElementFromIndex + 1;
                } else {
                    DTMDefaultBaseTraversers.this.nextNode();
                    if (DTMDefaultBaseTraversers.this.m_nextsib.elementAt(i) != -2) {
                        return -1;
                    }
                }
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            return DTMDefaultBaseTraversers.this.getFirstChild(i);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(makeNodeIdentity, DTMDefaultBaseTraversers.this._firstch(makeNodeIdentity), i2));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return DTMDefaultBaseTraversers.this.getNextSibling(i2);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            DTMDefaultBaseTraversers dTMDefaultBaseTraversers = DTMDefaultBaseTraversers.this;
            int _nextsib = dTMDefaultBaseTraversers._nextsib(dTMDefaultBaseTraversers.makeNodeIdentity(i2));
            while (-1 != _nextsib) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(_nextsib) == i3) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(_nextsib);
                }
                _nextsib = DTMDefaultBaseTraversers.this._nextsib(_nextsib);
            }
            return -1;
        }
    }

    private abstract class IndexedDTMAxisTraverser extends DTMAxisTraverser {
        /* access modifiers changed from: protected */
        public abstract boolean axisHasBeenProcessed(int i);

        /* access modifiers changed from: protected */
        public abstract boolean isAfterAxis(int i, int i2);

        private IndexedDTMAxisTraverser() {
        }

        /* access modifiers changed from: protected */
        public final boolean isIndexed(int i) {
            if (!DTMDefaultBaseTraversers.this.m_indexing || 1 != DTMDefaultBaseTraversers.this.m_expandedNameTable.getType(i)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public int getNextIndexed(int i, int i2, int i3) {
            int namespaceID = DTMDefaultBaseTraversers.this.m_expandedNameTable.getNamespaceID(i3);
            int localNameID = DTMDefaultBaseTraversers.this.m_expandedNameTable.getLocalNameID(i3);
            while (true) {
                int findElementFromIndex = DTMDefaultBaseTraversers.this.findElementFromIndex(namespaceID, localNameID, i2);
                if (-2 != findElementFromIndex) {
                    if (isAfterAxis(i, findElementFromIndex)) {
                        return -1;
                    }
                    return findElementFromIndex;
                } else if (axisHasBeenProcessed(i)) {
                    return -1;
                } else {
                    DTMDefaultBaseTraversers.this.nextNode();
                }
            }
        }
    }

    private class DescendantTraverser extends IndexedDTMAxisTraverser {
        /* access modifiers changed from: protected */
        public int getFirstPotential(int i) {
            return i + 1;
        }

        private DescendantTraverser() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.IndexedDTMAxisTraverser
        public boolean axisHasBeenProcessed(int i) {
            return DTMDefaultBaseTraversers.this.m_nextsib.elementAt(i) != -2;
        }

        /* access modifiers changed from: protected */
        public int getSubtreeRoot(int i) {
            return DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
        }

        /* access modifiers changed from: protected */
        public boolean isDescendant(int i, int i2) {
            return DTMDefaultBaseTraversers.this._parent(i2) >= i;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.IndexedDTMAxisTraverser
        public boolean isAfterAxis(int i, int i2) {
            while (i2 != i) {
                i2 = DTMDefaultBaseTraversers.this.m_parent.elementAt(i2);
                if (i2 < i) {
                    return true;
                }
            }
            return false;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            if (!isIndexed(i2)) {
                return next(i, i, i2);
            }
            int subtreeRoot = getSubtreeRoot(i);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(subtreeRoot, getFirstPotential(subtreeRoot), i2));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            int subtreeRoot = getSubtreeRoot(i);
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2);
            while (true) {
                makeNodeIdentity++;
                short _type = DTMDefaultBaseTraversers.this._type(makeNodeIdentity);
                if (!isDescendant(subtreeRoot, makeNodeIdentity)) {
                    return -1;
                }
                if (2 != _type && 13 != _type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
                }
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int subtreeRoot = getSubtreeRoot(i);
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2) + 1;
            if (isIndexed(i3)) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(subtreeRoot, makeNodeIdentity, i3));
            }
            while (true) {
                int _exptype = DTMDefaultBaseTraversers.this._exptype(makeNodeIdentity);
                if (!isDescendant(subtreeRoot, makeNodeIdentity)) {
                    return -1;
                }
                if (_exptype == i3) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
                }
                makeNodeIdentity++;
            }
        }
    }

    private class DescendantOrSelfTraverser extends DescendantTraverser {
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            return i;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser
        public int getFirstPotential(int i) {
            return i;
        }

        private DescendantOrSelfTraverser() {
            super();
        }
    }

    private class AllFromNodeTraverser extends DescendantOrSelfTraverser {
        private AllFromNodeTraverser() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            int makeNodeIdentity2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2) + 1;
            DTMDefaultBaseTraversers.this._exptype(makeNodeIdentity2);
            if (!isDescendant(makeNodeIdentity, makeNodeIdentity2)) {
                return -1;
            }
            return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity2);
        }
    }

    private class FollowingTraverser extends DescendantTraverser {
        private FollowingTraverser() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            int _nextsib;
            int _firstch;
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            short _type = DTMDefaultBaseTraversers.this._type(makeNodeIdentity);
            if ((2 == _type || 13 == _type) && -1 != (_firstch = DTMDefaultBaseTraversers.this._firstch((makeNodeIdentity = DTMDefaultBaseTraversers.this._parent(makeNodeIdentity))))) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(_firstch);
            }
            do {
                _nextsib = DTMDefaultBaseTraversers.this._nextsib(makeNodeIdentity);
                if (-1 == _nextsib) {
                    makeNodeIdentity = DTMDefaultBaseTraversers.this._parent(makeNodeIdentity);
                }
                if (-1 != _nextsib) {
                    break;
                }
            } while (-1 != makeNodeIdentity);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(_nextsib);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            int nextSibling;
            int firstChild;
            short nodeType = DTMDefaultBaseTraversers.this.getNodeType(i);
            if ((2 != nodeType && 13 != nodeType) || -1 == (firstChild = DTMDefaultBaseTraversers.this.getFirstChild((i = DTMDefaultBaseTraversers.this.getParent(i))))) {
                do {
                    nextSibling = DTMDefaultBaseTraversers.this.getNextSibling(i);
                    if (-1 == nextSibling) {
                        i = DTMDefaultBaseTraversers.this.getParent(i);
                        if (-1 != nextSibling) {
                            break;
                        }
                    } else if (DTMDefaultBaseTraversers.this.getExpandedTypeID(nextSibling) == i2) {
                        return nextSibling;
                    } else {
                        return next(i, nextSibling, i2);
                    }
                } while (-1 != i);
                return nextSibling;
            } else if (DTMDefaultBaseTraversers.this.getExpandedTypeID(firstChild) == i2) {
                return firstChild;
            } else {
                return next(i, firstChild, i2);
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2);
            while (true) {
                makeNodeIdentity++;
                short _type = DTMDefaultBaseTraversers.this._type(makeNodeIdentity);
                if (-1 == _type) {
                    return -1;
                }
                if (2 != _type && 13 != _type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
                }
            }
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int _exptype;
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2);
            do {
                makeNodeIdentity++;
                _exptype = DTMDefaultBaseTraversers.this._exptype(makeNodeIdentity);
                if (-1 == _exptype) {
                    return -1;
                }
            } while (_exptype != i3);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
        }
    }

    private class FollowingSiblingTraverser extends DTMAxisTraverser {
        private FollowingSiblingTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return DTMDefaultBaseTraversers.this.getNextSibling(i2);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            do {
                i2 = DTMDefaultBaseTraversers.this.getNextSibling(i2);
                if (-1 == i2) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.getExpandedTypeID(i2) != i3);
            return i2;
        }
    }

    private class NamespaceDeclsTraverser extends DTMAxisTraverser {
        private NamespaceDeclsTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            if (i == i2) {
                return DTMDefaultBaseTraversers.this.getFirstNamespaceNode(i, false);
            }
            return DTMDefaultBaseTraversers.this.getNextNamespaceNode(i, i2, false);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int i4;
            if (i == i2) {
                i4 = DTMDefaultBaseTraversers.this.getFirstNamespaceNode(i, false);
            } else {
                i4 = DTMDefaultBaseTraversers.this.getNextNamespaceNode(i, i2, false);
            }
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(i4) != i3) {
                i4 = DTMDefaultBaseTraversers.this.getNextNamespaceNode(i, i4, false);
                if (-1 == i4) {
                    return -1;
                }
            }
            return i4;
        }
    }

    private class NamespaceTraverser extends DTMAxisTraverser {
        private NamespaceTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            if (i == i2) {
                return DTMDefaultBaseTraversers.this.getFirstNamespaceNode(i, true);
            }
            return DTMDefaultBaseTraversers.this.getNextNamespaceNode(i, i2, true);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int i4;
            if (i == i2) {
                i4 = DTMDefaultBaseTraversers.this.getFirstNamespaceNode(i, true);
            } else {
                i4 = DTMDefaultBaseTraversers.this.getNextNamespaceNode(i, i2, true);
            }
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(i4) != i3) {
                i4 = DTMDefaultBaseTraversers.this.getNextNamespaceNode(i, i4, true);
                if (-1 == i4) {
                    return -1;
                }
            }
            return i4;
        }
    }

    private class ParentTraverser extends DTMAxisTraverser {
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            return -1;
        }

        private ParentTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            return DTMDefaultBaseTraversers.this.getParent(i);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            do {
                makeNodeIdentity = DTMDefaultBaseTraversers.this.m_parent.elementAt(makeNodeIdentity);
                if (-1 == makeNodeIdentity) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.m_exptype.elementAt(makeNodeIdentity) != i2);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
        }
    }

    private class PrecedingTraverser extends DTMAxisTraverser {
        private PrecedingTraverser() {
        }

        /* access modifiers changed from: protected */
        public boolean isAncestor(int i, int i2) {
            int elementAt = DTMDefaultBaseTraversers.this.m_parent.elementAt(i);
            while (-1 != elementAt) {
                if (elementAt == i2) {
                    return true;
                }
                elementAt = DTMDefaultBaseTraversers.this.m_parent.elementAt(elementAt);
            }
            return false;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            for (int makeNodeIdentity2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2) - 1; makeNodeIdentity2 >= 0; makeNodeIdentity2--) {
                short _type = DTMDefaultBaseTraversers.this._type(makeNodeIdentity2);
                if (!(2 == _type || 13 == _type || isAncestor(makeNodeIdentity, makeNodeIdentity2))) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity2);
                }
            }
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            for (int makeNodeIdentity2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2) - 1; makeNodeIdentity2 >= 0; makeNodeIdentity2--) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(makeNodeIdentity2) == i3 && !isAncestor(makeNodeIdentity, makeNodeIdentity2)) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity2);
                }
            }
            return -1;
        }
    }

    private class PrecedingAndAncestorTraverser extends DTMAxisTraverser {
        private PrecedingAndAncestorTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            for (int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2) - 1; makeNodeIdentity >= 0; makeNodeIdentity--) {
                short _type = DTMDefaultBaseTraversers.this._type(makeNodeIdentity);
                if (!(2 == _type || 13 == _type)) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
                }
            }
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            for (int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2) - 1; makeNodeIdentity >= 0; makeNodeIdentity--) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(makeNodeIdentity) == i3) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
                }
            }
            return -1;
        }
    }

    private class PrecedingSiblingTraverser extends DTMAxisTraverser {
        private PrecedingSiblingTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return DTMDefaultBaseTraversers.this.getPreviousSibling(i2);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            do {
                i2 = DTMDefaultBaseTraversers.this.getPreviousSibling(i2);
                if (-1 == i2) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.getExpandedTypeID(i2) != i3);
            return i2;
        }
    }

    private class SelfTraverser extends DTMAxisTraverser {
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            return i;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            return -1;
        }

        private SelfTraverser() {
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            if (DTMDefaultBaseTraversers.this.getExpandedTypeID(i) == i2) {
                return i;
            }
            return -1;
        }
    }

    private class AllFromRootTraverser extends AllFromNodeTraverser {
        private AllFromRootTraverser() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantOrSelfTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            return DTMDefaultBaseTraversers.this.getDocumentRoot(i);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            DTMDefaultBaseTraversers dTMDefaultBaseTraversers = DTMDefaultBaseTraversers.this;
            return dTMDefaultBaseTraversers.getExpandedTypeID(dTMDefaultBaseTraversers.getDocumentRoot(i)) == i2 ? i : next(i, i, i2);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.AllFromNodeTraverser, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2) + 1;
            if (DTMDefaultBaseTraversers.this._type(makeNodeIdentity) == -1) {
                return -1;
            }
            return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            int _exptype;
            DTMDefaultBaseTraversers.this.makeNodeIdentity(i);
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(i2);
            do {
                makeNodeIdentity++;
                _exptype = DTMDefaultBaseTraversers.this._exptype(makeNodeIdentity);
                if (_exptype == -1) {
                    return -1;
                }
            } while (_exptype != i3);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(makeNodeIdentity);
        }
    }

    private class RootTraverser extends AllFromRootTraverser {
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.AllFromRootTraverser, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.AllFromNodeTraverser, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2) {
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.AllFromRootTraverser, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int next(int i, int i2, int i3) {
            return -1;
        }

        private RootTraverser() {
            super();
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.AllFromRootTraverser, ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            int documentRoot = DTMDefaultBaseTraversers.this.getDocumentRoot(i);
            if (DTMDefaultBaseTraversers.this.getExpandedTypeID(documentRoot) == i2) {
                return documentRoot;
            }
            return -1;
        }
    }

    private class DescendantOrSelfFromRootTraverser extends DescendantTraverser {
        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser
        public int getFirstPotential(int i) {
            return i;
        }

        private DescendantOrSelfFromRootTraverser() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser
        public int getSubtreeRoot(int i) {
            DTMDefaultBaseTraversers dTMDefaultBaseTraversers = DTMDefaultBaseTraversers.this;
            return dTMDefaultBaseTraversers.makeNodeIdentity(dTMDefaultBaseTraversers.getDocument());
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            return DTMDefaultBaseTraversers.this.getDocumentRoot(i);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            if (isIndexed(i2)) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(0, getFirstPotential(0), i2));
            }
            int first = first(i);
            return next(first, first, i2);
        }
    }

    private class DescendantFromRootTraverser extends DescendantTraverser {
        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser
        public int getSubtreeRoot(int i) {
            return 0;
        }

        private DescendantFromRootTraverser() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser
        public int getFirstPotential(int i) {
            return DTMDefaultBaseTraversers.this._firstch(0);
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i) {
            DTMDefaultBaseTraversers dTMDefaultBaseTraversers = DTMDefaultBaseTraversers.this;
            return dTMDefaultBaseTraversers.makeNodeHandle(dTMDefaultBaseTraversers._firstch(0));
        }

        @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseTraversers.DescendantTraverser, ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser
        public int first(int i, int i2) {
            if (isIndexed(i2)) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(0, getFirstPotential(0), i2));
            }
            int documentRoot = DTMDefaultBaseTraversers.this.getDocumentRoot(i);
            return next(documentRoot, documentRoot, i2);
        }
    }
}

package org.apache.xml.dtm.ref;

import javax.xml.transform.Source;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMException;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.XMLStringFactory;

public abstract class DTMDefaultBaseTraversers extends DTMDefaultBase {

    private class AllFromNodeTraverser extends DescendantOrSelfTraverser {
        private AllFromNodeTraverser() {
            super();
        }

        public int next(int context, int current) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            DTMDefaultBaseTraversers.this._exptype(current2);
            if (!isDescendant(subtreeRootIdent, current2)) {
                return -1;
            }
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
        }
    }

    private class AllFromRootTraverser extends AllFromNodeTraverser {
        private AllFromRootTraverser() {
            super();
        }

        public int first(int context) {
            return DTMDefaultBaseTraversers.this.getDocumentRoot(context);
        }

        public int first(int context, int expandedTypeID) {
            if (DTMDefaultBaseTraversers.this.getExpandedTypeID(DTMDefaultBaseTraversers.this.getDocumentRoot(context)) == expandedTypeID) {
                return context;
            }
            return next(context, context, expandedTypeID);
        }

        public int next(int context, int current) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            if (DTMDefaultBaseTraversers.this._type(current2) == -1) {
                return -1;
            }
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
        }

        public int next(int context, int current, int expandedTypeID) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            while (true) {
                current2++;
                int exptype = DTMDefaultBaseTraversers.this._exptype(current2);
                if (exptype == -1) {
                    return -1;
                }
                if (exptype == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
            }
        }
    }

    private class AncestorOrSelfTraverser extends AncestorTraverser {
        private AncestorOrSelfTraverser() {
            super();
        }

        public int first(int context) {
            return context;
        }

        public int first(int context, int expandedTypeID) {
            if (DTMDefaultBaseTraversers.this.getExpandedTypeID(context) == expandedTypeID) {
                return context;
            }
            return next(context, context, expandedTypeID);
        }
    }

    private class AncestorTraverser extends DTMAxisTraverser {
        private AncestorTraverser() {
        }

        public int next(int context, int current) {
            return DTMDefaultBaseTraversers.this.getParent(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            do {
                int elementAt = DTMDefaultBaseTraversers.this.m_parent.elementAt(current2);
                current2 = elementAt;
                if (-1 == elementAt) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current2) != expandedTypeID);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
        }
    }

    private class AttributeTraverser extends DTMAxisTraverser {
        private AttributeTraverser() {
        }

        public int next(int context, int current) {
            return context == current ? DTMDefaultBaseTraversers.this.getFirstAttribute(context) : DTMDefaultBaseTraversers.this.getNextAttribute(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            int current2 = context == current ? DTMDefaultBaseTraversers.this.getFirstAttribute(context) : DTMDefaultBaseTraversers.this.getNextAttribute(current);
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current2) != expandedTypeID) {
                int nextAttribute = DTMDefaultBaseTraversers.this.getNextAttribute(current2);
                current2 = nextAttribute;
                if (-1 == nextAttribute) {
                    return -1;
                }
            }
            return current2;
        }
    }

    private class ChildTraverser extends DTMAxisTraverser {
        private ChildTraverser() {
        }

        /* access modifiers changed from: protected */
        public int getNextIndexed(int axisRoot, int nextPotential, int expandedTypeID) {
            int nsIndex = DTMDefaultBaseTraversers.this.m_expandedNameTable.getNamespaceID(expandedTypeID);
            int lnIndex = DTMDefaultBaseTraversers.this.m_expandedNameTable.getLocalNameID(expandedTypeID);
            while (true) {
                int nextID = DTMDefaultBaseTraversers.this.findElementFromIndex(nsIndex, lnIndex, nextPotential);
                if (-2 != nextID) {
                    int parentID = DTMDefaultBaseTraversers.this.m_parent.elementAt(nextID);
                    if (parentID == axisRoot) {
                        return nextID;
                    }
                    if (parentID < axisRoot) {
                        return -1;
                    }
                    do {
                        parentID = DTMDefaultBaseTraversers.this.m_parent.elementAt(parentID);
                        if (parentID < axisRoot) {
                            return -1;
                        }
                    } while (parentID > axisRoot);
                    nextPotential = nextID + 1;
                } else {
                    DTMDefaultBaseTraversers.this.nextNode();
                    if (DTMDefaultBaseTraversers.this.m_nextsib.elementAt(axisRoot) != -2) {
                        return -1;
                    }
                }
            }
        }

        public int first(int context) {
            return DTMDefaultBaseTraversers.this.getFirstChild(context);
        }

        public int first(int context, int expandedTypeID) {
            int identity = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(identity, DTMDefaultBaseTraversers.this._firstch(identity), expandedTypeID));
        }

        public int next(int context, int current) {
            return DTMDefaultBaseTraversers.this.getNextSibling(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            int current2 = DTMDefaultBaseTraversers.this._nextsib(DTMDefaultBaseTraversers.this.makeNodeIdentity(current));
            while (-1 != current2) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current2) == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
                current2 = DTMDefaultBaseTraversers.this._nextsib(current2);
            }
            return -1;
        }
    }

    private class DescendantFromRootTraverser extends DescendantTraverser {
        private DescendantFromRootTraverser() {
            super();
        }

        /* access modifiers changed from: protected */
        public int getFirstPotential(int identity) {
            return DTMDefaultBaseTraversers.this._firstch(0);
        }

        /* access modifiers changed from: protected */
        public int getSubtreeRoot(int handle) {
            return 0;
        }

        public int first(int context) {
            return DTMDefaultBaseTraversers.this.makeNodeHandle(DTMDefaultBaseTraversers.this._firstch(0));
        }

        public int first(int context, int expandedTypeID) {
            if (isIndexed(expandedTypeID)) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(0, getFirstPotential(0), expandedTypeID));
            }
            int root = DTMDefaultBaseTraversers.this.getDocumentRoot(context);
            return next(root, root, expandedTypeID);
        }
    }

    private class DescendantOrSelfFromRootTraverser extends DescendantTraverser {
        private DescendantOrSelfFromRootTraverser() {
            super();
        }

        /* access modifiers changed from: protected */
        public int getFirstPotential(int identity) {
            return identity;
        }

        /* access modifiers changed from: protected */
        public int getSubtreeRoot(int handle) {
            return DTMDefaultBaseTraversers.this.makeNodeIdentity(DTMDefaultBaseTraversers.this.getDocument());
        }

        public int first(int context) {
            return DTMDefaultBaseTraversers.this.getDocumentRoot(context);
        }

        public int first(int context, int expandedTypeID) {
            if (isIndexed(expandedTypeID)) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(0, getFirstPotential(0), expandedTypeID));
            }
            int root = first(context);
            return next(root, root, expandedTypeID);
        }
    }

    private class DescendantOrSelfTraverser extends DescendantTraverser {
        private DescendantOrSelfTraverser() {
            super();
        }

        /* access modifiers changed from: protected */
        public int getFirstPotential(int identity) {
            return identity;
        }

        public int first(int context) {
            return context;
        }
    }

    private class DescendantTraverser extends IndexedDTMAxisTraverser {
        private DescendantTraverser() {
            super();
        }

        /* access modifiers changed from: protected */
        public int getFirstPotential(int identity) {
            return identity + 1;
        }

        /* access modifiers changed from: protected */
        public boolean axisHasBeenProcessed(int axisRoot) {
            return DTMDefaultBaseTraversers.this.m_nextsib.elementAt(axisRoot) != -2;
        }

        /* access modifiers changed from: protected */
        public int getSubtreeRoot(int handle) {
            return DTMDefaultBaseTraversers.this.makeNodeIdentity(handle);
        }

        /* access modifiers changed from: protected */
        public boolean isDescendant(int subtreeRootIdentity, int identity) {
            return DTMDefaultBaseTraversers.this._parent(identity) >= subtreeRootIdentity;
        }

        /* access modifiers changed from: protected */
        public boolean isAfterAxis(int axisRoot, int identity) {
            while (identity != axisRoot) {
                identity = DTMDefaultBaseTraversers.this.m_parent.elementAt(identity);
                if (identity < axisRoot) {
                    return true;
                }
            }
            return false;
        }

        public int first(int context, int expandedTypeID) {
            if (!isIndexed(expandedTypeID)) {
                return next(context, context, expandedTypeID);
            }
            int identity = getSubtreeRoot(context);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(identity, getFirstPotential(identity), expandedTypeID));
        }

        public int next(int context, int current) {
            int subtreeRootIdent = getSubtreeRoot(context);
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            while (true) {
                current2++;
                int type = DTMDefaultBaseTraversers.this._type(current2);
                if (!isDescendant(subtreeRootIdent, current2)) {
                    return -1;
                }
                if (2 != type && 13 != type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
            }
        }

        public int next(int context, int current, int expandedTypeID) {
            int subtreeRootIdent = getSubtreeRoot(context);
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            if (isIndexed(expandedTypeID) != 0) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(subtreeRootIdent, current2, expandedTypeID));
            }
            while (true) {
                int exptype = DTMDefaultBaseTraversers.this._exptype(current2);
                if (!isDescendant(subtreeRootIdent, current2)) {
                    return -1;
                }
                if (exptype == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
                current2++;
            }
        }
    }

    private class FollowingSiblingTraverser extends DTMAxisTraverser {
        private FollowingSiblingTraverser() {
        }

        public int next(int context, int current) {
            return DTMDefaultBaseTraversers.this.getNextSibling(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            do {
                int nextSibling = DTMDefaultBaseTraversers.this.getNextSibling(current);
                current = nextSibling;
                if (-1 == nextSibling) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current) != expandedTypeID);
            return current;
        }
    }

    private class FollowingTraverser extends DescendantTraverser {
        private FollowingTraverser() {
            super();
        }

        public int first(int context) {
            int first;
            int context2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            int type = DTMDefaultBaseTraversers.this._type(context2);
            if (2 == type || 13 == type) {
                context2 = DTMDefaultBaseTraversers.this._parent(context2);
                int first2 = DTMDefaultBaseTraversers.this._firstch(context2);
                if (-1 != first2) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(first2);
                }
            }
            do {
                first = DTMDefaultBaseTraversers.this._nextsib(context2);
                if (-1 == first) {
                    context2 = DTMDefaultBaseTraversers.this._parent(context2);
                }
                if (-1 != first) {
                    break;
                }
            } while (-1 != context2);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(first);
        }

        public int first(int context, int expandedTypeID) {
            int first;
            int type = DTMDefaultBaseTraversers.this.getNodeType(context);
            if (2 == type || 13 == type) {
                context = DTMDefaultBaseTraversers.this.getParent(context);
                int first2 = DTMDefaultBaseTraversers.this.getFirstChild(context);
                if (-1 != first2) {
                    if (DTMDefaultBaseTraversers.this.getExpandedTypeID(first2) == expandedTypeID) {
                        return first2;
                    }
                    return next(context, first2, expandedTypeID);
                }
            }
            do {
                first = DTMDefaultBaseTraversers.this.getNextSibling(context);
                if (-1 == first) {
                    context = DTMDefaultBaseTraversers.this.getParent(context);
                    if (-1 != first) {
                        break;
                    }
                } else if (DTMDefaultBaseTraversers.this.getExpandedTypeID(first) == expandedTypeID) {
                    return first;
                } else {
                    return next(context, first, expandedTypeID);
                }
            } while (-1 != context);
            return first;
        }

        public int next(int context, int current) {
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            while (true) {
                current2++;
                int type = DTMDefaultBaseTraversers.this._type(current2);
                if (-1 == type) {
                    return -1;
                }
                if (2 != type && 13 != type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
            }
        }

        public int next(int context, int current, int expandedTypeID) {
            int etype;
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            do {
                current2++;
                etype = DTMDefaultBaseTraversers.this._exptype(current2);
                if (-1 == etype) {
                    return -1;
                }
            } while (etype != expandedTypeID);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
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
        public final boolean isIndexed(int expandedTypeID) {
            if (!DTMDefaultBaseTraversers.this.m_indexing || 1 != DTMDefaultBaseTraversers.this.m_expandedNameTable.getType(expandedTypeID)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public int getNextIndexed(int axisRoot, int nextPotential, int expandedTypeID) {
            int nsIndex = DTMDefaultBaseTraversers.this.m_expandedNameTable.getNamespaceID(expandedTypeID);
            int lnIndex = DTMDefaultBaseTraversers.this.m_expandedNameTable.getLocalNameID(expandedTypeID);
            while (true) {
                int next = DTMDefaultBaseTraversers.this.findElementFromIndex(nsIndex, lnIndex, nextPotential);
                if (-2 != next) {
                    if (isAfterAxis(axisRoot, next)) {
                        return -1;
                    }
                    return next;
                } else if (axisHasBeenProcessed(axisRoot)) {
                    return -1;
                } else {
                    DTMDefaultBaseTraversers.this.nextNode();
                }
            }
        }
    }

    private class NamespaceDeclsTraverser extends DTMAxisTraverser {
        private NamespaceDeclsTraverser() {
        }

        public int next(int context, int current) {
            if (context == current) {
                return DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, false);
            }
            return DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, false);
        }

        public int next(int context, int current, int expandedTypeID) {
            int i;
            if (context == current) {
                i = DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, false);
            } else {
                i = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, false);
            }
            int current2 = i;
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current2) != expandedTypeID) {
                int nextNamespaceNode = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current2, false);
                current2 = nextNamespaceNode;
                if (-1 == nextNamespaceNode) {
                    return -1;
                }
            }
            return current2;
        }
    }

    private class NamespaceTraverser extends DTMAxisTraverser {
        private NamespaceTraverser() {
        }

        public int next(int context, int current) {
            if (context == current) {
                return DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, true);
            }
            return DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, true);
        }

        public int next(int context, int current, int expandedTypeID) {
            int i;
            if (context == current) {
                i = DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, true);
            } else {
                i = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, true);
            }
            int current2 = i;
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current2) != expandedTypeID) {
                int nextNamespaceNode = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current2, true);
                current2 = nextNamespaceNode;
                if (-1 == nextNamespaceNode) {
                    return -1;
                }
            }
            return current2;
        }
    }

    private class ParentTraverser extends DTMAxisTraverser {
        private ParentTraverser() {
        }

        public int first(int context) {
            return DTMDefaultBaseTraversers.this.getParent(context);
        }

        public int first(int current, int expandedTypeID) {
            int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            do {
                int elementAt = DTMDefaultBaseTraversers.this.m_parent.elementAt(current2);
                current2 = elementAt;
                if (-1 == elementAt) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current2) != expandedTypeID);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
        }

        public int next(int context, int current) {
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            return -1;
        }
    }

    private class PrecedingAndAncestorTraverser extends DTMAxisTraverser {
        private PrecedingAndAncestorTraverser() {
        }

        public int next(int context, int current) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            for (int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1; current2 >= 0; current2--) {
                short type = DTMDefaultBaseTraversers.this._type(current2);
                if (2 != type && 13 != type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
            }
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            int makeNodeIdentity = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            for (int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1; current2 >= 0; current2--) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current2) == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
            }
            return -1;
        }
    }

    private class PrecedingSiblingTraverser extends DTMAxisTraverser {
        private PrecedingSiblingTraverser() {
        }

        public int next(int context, int current) {
            return DTMDefaultBaseTraversers.this.getPreviousSibling(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            do {
                int previousSibling = DTMDefaultBaseTraversers.this.getPreviousSibling(current);
                current = previousSibling;
                if (-1 == previousSibling) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current) != expandedTypeID);
            return current;
        }
    }

    private class PrecedingTraverser extends DTMAxisTraverser {
        private PrecedingTraverser() {
        }

        /* access modifiers changed from: protected */
        public boolean isAncestor(int contextIdent, int currentIdent) {
            int contextIdent2 = DTMDefaultBaseTraversers.this.m_parent.elementAt(contextIdent);
            while (-1 != contextIdent2) {
                if (contextIdent2 == currentIdent) {
                    return true;
                }
                contextIdent2 = DTMDefaultBaseTraversers.this.m_parent.elementAt(contextIdent2);
            }
            return false;
        }

        public int next(int context, int current) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            for (int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1; current2 >= 0; current2--) {
                short type = DTMDefaultBaseTraversers.this._type(current2);
                if (2 != type && 13 != type && !isAncestor(subtreeRootIdent, current2)) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
            }
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            for (int current2 = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1; current2 >= 0; current2--) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current2) == expandedTypeID && !isAncestor(subtreeRootIdent, current2)) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current2);
                }
            }
            return -1;
        }
    }

    private class RootTraverser extends AllFromRootTraverser {
        private RootTraverser() {
            super();
        }

        public int first(int context, int expandedTypeID) {
            int root = DTMDefaultBaseTraversers.this.getDocumentRoot(context);
            if (DTMDefaultBaseTraversers.this.getExpandedTypeID(root) == expandedTypeID) {
                return root;
            }
            return -1;
        }

        public int next(int context, int current) {
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            return -1;
        }
    }

    private class SelfTraverser extends DTMAxisTraverser {
        private SelfTraverser() {
        }

        public int first(int context) {
            return context;
        }

        public int first(int context, int expandedTypeID) {
            if (DTMDefaultBaseTraversers.this.getExpandedTypeID(context) == expandedTypeID) {
                return context;
            }
            return -1;
        }

        public int next(int context, int current) {
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            return -1;
        }
    }

    public DTMDefaultBaseTraversers(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing);
    }

    public DTMDefaultBaseTraversers(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing, int blocksize, boolean usePrevsib, boolean newNameTable) {
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, blocksize, usePrevsib, newNameTable);
    }

    public DTMAxisTraverser getAxisTraverser(int axis) {
        DTMAxisTraverser traverser;
        if (this.m_traversers == null) {
            this.m_traversers = new DTMAxisTraverser[Axis.getNamesLength()];
        } else {
            DTMAxisTraverser traverser2 = this.m_traversers[axis];
            if (traverser2 != null) {
                return traverser2;
            }
        }
        switch (axis) {
            case 0:
                traverser = new AncestorTraverser();
                break;
            case 1:
                traverser = new AncestorOrSelfTraverser();
                break;
            case 2:
                traverser = new AttributeTraverser();
                break;
            case 3:
                traverser = new ChildTraverser();
                break;
            case 4:
                traverser = new DescendantTraverser();
                break;
            case 5:
                traverser = new DescendantOrSelfTraverser();
                break;
            case 6:
                traverser = new FollowingTraverser();
                break;
            case 7:
                traverser = new FollowingSiblingTraverser();
                break;
            case 8:
                traverser = new NamespaceDeclsTraverser();
                break;
            case 9:
                traverser = new NamespaceTraverser();
                break;
            case 10:
                traverser = new ParentTraverser();
                break;
            case 11:
                traverser = new PrecedingTraverser();
                break;
            case 12:
                traverser = new PrecedingSiblingTraverser();
                break;
            case 13:
                traverser = new SelfTraverser();
                break;
            case 14:
                traverser = new AllFromNodeTraverser();
                break;
            case 15:
                traverser = new PrecedingAndAncestorTraverser();
                break;
            case 16:
                traverser = new AllFromRootTraverser();
                break;
            case 17:
                traverser = new DescendantFromRootTraverser();
                break;
            case 18:
                traverser = new DescendantOrSelfFromRootTraverser();
                break;
            case 19:
                traverser = new RootTraverser();
                break;
            case 20:
                return null;
            default:
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_UNKNOWN_AXIS_TYPE, new Object[]{Integer.toString(axis)}));
        }
        this.m_traversers[axis] = traverser;
        return traverser;
    }
}

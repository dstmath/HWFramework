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

    private abstract class IndexedDTMAxisTraverser extends DTMAxisTraverser {
        /* synthetic */ IndexedDTMAxisTraverser(DTMDefaultBaseTraversers this$0, IndexedDTMAxisTraverser -this1) {
            this();
        }

        protected abstract boolean axisHasBeenProcessed(int i);

        protected abstract boolean isAfterAxis(int i, int i2);

        private IndexedDTMAxisTraverser() {
        }

        protected final boolean isIndexed(int expandedTypeID) {
            if (DTMDefaultBaseTraversers.this.m_indexing) {
                return (short) 1 == DTMDefaultBaseTraversers.this.m_expandedNameTable.getType(expandedTypeID);
            } else {
                return false;
            }
        }

        protected int getNextIndexed(int axisRoot, int nextPotential, int expandedTypeID) {
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

    private class DescendantTraverser extends IndexedDTMAxisTraverser {
        /* synthetic */ DescendantTraverser(DTMDefaultBaseTraversers this$0, DescendantTraverser -this1) {
            this();
        }

        private DescendantTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
        }

        protected int getFirstPotential(int identity) {
            return identity + 1;
        }

        protected boolean axisHasBeenProcessed(int axisRoot) {
            return DTMDefaultBaseTraversers.this.m_nextsib.elementAt(axisRoot) != -2;
        }

        protected int getSubtreeRoot(int handle) {
            return DTMDefaultBaseTraversers.this.makeNodeIdentity(handle);
        }

        protected boolean isDescendant(int subtreeRootIdentity, int identity) {
            return DTMDefaultBaseTraversers.this._parent(identity) >= subtreeRootIdentity;
        }

        protected boolean isAfterAxis(int axisRoot, int identity) {
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
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            while (true) {
                int type = DTMDefaultBaseTraversers.this._type(current);
                if (!isDescendant(subtreeRootIdent, current)) {
                    return -1;
                }
                if (2 != type && 13 != type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
                current++;
            }
        }

        public int next(int context, int current, int expandedTypeID) {
            int subtreeRootIdent = getSubtreeRoot(context);
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            if (isIndexed(expandedTypeID)) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(getNextIndexed(subtreeRootIdent, current, expandedTypeID));
            }
            while (true) {
                int _exptype = DTMDefaultBaseTraversers.this._exptype(current);
                if (!isDescendant(subtreeRootIdent, current)) {
                    return -1;
                }
                if (_exptype == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
                current++;
            }
        }
    }

    private class DescendantOrSelfTraverser extends DescendantTraverser {
        /* synthetic */ DescendantOrSelfTraverser(DTMDefaultBaseTraversers this$0, DescendantOrSelfTraverser -this1) {
            this();
        }

        private DescendantOrSelfTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
        }

        protected int getFirstPotential(int identity) {
            return identity;
        }

        public int first(int context) {
            return context;
        }
    }

    private class AllFromNodeTraverser extends DescendantOrSelfTraverser {
        /* synthetic */ AllFromNodeTraverser(DTMDefaultBaseTraversers this$0, AllFromNodeTraverser -this1) {
            this();
        }

        private AllFromNodeTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
        }

        public int next(int context, int current) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            DTMDefaultBaseTraversers.this._exptype(current);
            if (isDescendant(subtreeRootIdent, current)) {
                return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
            }
            return -1;
        }
    }

    private class AllFromRootTraverser extends AllFromNodeTraverser {
        /* synthetic */ AllFromRootTraverser(DTMDefaultBaseTraversers this$0, AllFromRootTraverser -this1) {
            this();
        }

        private AllFromRootTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
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
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            if (DTMDefaultBaseTraversers.this._type(current) == -1) {
                return -1;
            }
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) + 1;
            while (true) {
                int exptype = DTMDefaultBaseTraversers.this._exptype(current);
                if (exptype == -1) {
                    return -1;
                }
                if (exptype == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
                current++;
            }
        }
    }

    private class AncestorTraverser extends DTMAxisTraverser {
        /* synthetic */ AncestorTraverser(DTMDefaultBaseTraversers this$0, AncestorTraverser -this1) {
            this();
        }

        private AncestorTraverser() {
        }

        public int next(int context, int current) {
            return DTMDefaultBaseTraversers.this.getParent(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            do {
                current = DTMDefaultBaseTraversers.this.m_parent.elementAt(current);
                if (-1 == current) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current) != expandedTypeID);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
        }
    }

    private class AncestorOrSelfTraverser extends AncestorTraverser {
        /* synthetic */ AncestorOrSelfTraverser(DTMDefaultBaseTraversers this$0, AncestorOrSelfTraverser -this1) {
            this();
        }

        private AncestorOrSelfTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
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

    private class AttributeTraverser extends DTMAxisTraverser {
        /* synthetic */ AttributeTraverser(DTMDefaultBaseTraversers this$0, AttributeTraverser -this1) {
            this();
        }

        private AttributeTraverser() {
        }

        public int next(int context, int current) {
            return context == current ? DTMDefaultBaseTraversers.this.getFirstAttribute(context) : DTMDefaultBaseTraversers.this.getNextAttribute(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            current = context == current ? DTMDefaultBaseTraversers.this.getFirstAttribute(context) : DTMDefaultBaseTraversers.this.getNextAttribute(current);
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current) != expandedTypeID) {
                current = DTMDefaultBaseTraversers.this.getNextAttribute(current);
                if (-1 == current) {
                    return -1;
                }
            }
            return current;
        }
    }

    private class ChildTraverser extends DTMAxisTraverser {
        /* synthetic */ ChildTraverser(DTMDefaultBaseTraversers this$0, ChildTraverser -this1) {
            this();
        }

        private ChildTraverser() {
        }

        protected int getNextIndexed(int axisRoot, int nextPotential, int expandedTypeID) {
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
            current = DTMDefaultBaseTraversers.this._nextsib(DTMDefaultBaseTraversers.this.makeNodeIdentity(current));
            while (-1 != current) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current) == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
                current = DTMDefaultBaseTraversers.this._nextsib(current);
            }
            return -1;
        }
    }

    private class DescendantFromRootTraverser extends DescendantTraverser {
        /* synthetic */ DescendantFromRootTraverser(DTMDefaultBaseTraversers this$0, DescendantFromRootTraverser -this1) {
            this();
        }

        private DescendantFromRootTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
        }

        protected int getFirstPotential(int identity) {
            return DTMDefaultBaseTraversers.this._firstch(0);
        }

        protected int getSubtreeRoot(int handle) {
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
        /* synthetic */ DescendantOrSelfFromRootTraverser(DTMDefaultBaseTraversers this$0, DescendantOrSelfFromRootTraverser -this1) {
            this();
        }

        private DescendantOrSelfFromRootTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
        }

        protected int getFirstPotential(int identity) {
            return identity;
        }

        protected int getSubtreeRoot(int handle) {
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

    private class FollowingSiblingTraverser extends DTMAxisTraverser {
        /* synthetic */ FollowingSiblingTraverser(DTMDefaultBaseTraversers this$0, FollowingSiblingTraverser -this1) {
            this();
        }

        private FollowingSiblingTraverser() {
        }

        public int next(int context, int current) {
            return DTMDefaultBaseTraversers.this.getNextSibling(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            do {
                current = DTMDefaultBaseTraversers.this.getNextSibling(current);
                if (-1 == current) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current) != expandedTypeID);
            return current;
        }
    }

    private class FollowingTraverser extends DescendantTraverser {
        /* synthetic */ FollowingTraverser(DTMDefaultBaseTraversers this$0, FollowingTraverser -this1) {
            this();
        }

        private FollowingTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
        }

        public int first(int context) {
            int first;
            context = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            int type = DTMDefaultBaseTraversers.this._type(context);
            if (2 == type || 13 == type) {
                context = DTMDefaultBaseTraversers.this._parent(context);
                first = DTMDefaultBaseTraversers.this._firstch(context);
                if (-1 != first) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(first);
                }
            }
            do {
                first = DTMDefaultBaseTraversers.this._nextsib(context);
                if (-1 == first) {
                    context = DTMDefaultBaseTraversers.this._parent(context);
                }
                if (-1 != first) {
                    break;
                }
            } while (-1 != context);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(first);
        }

        public int first(int context, int expandedTypeID) {
            int first;
            int type = DTMDefaultBaseTraversers.this.getNodeType(context);
            if (2 == type || 13 == type) {
                context = DTMDefaultBaseTraversers.this.getParent(context);
                first = DTMDefaultBaseTraversers.this.getFirstChild(context);
                if (-1 != first) {
                    if (DTMDefaultBaseTraversers.this.getExpandedTypeID(first) == expandedTypeID) {
                        return first;
                    }
                    return next(context, first, expandedTypeID);
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
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            while (true) {
                current++;
                int type = DTMDefaultBaseTraversers.this._type(current);
                if (-1 == type) {
                    return -1;
                }
                if (2 != type && 13 != type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
            }
        }

        public int next(int context, int current, int expandedTypeID) {
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            int etype;
            do {
                current++;
                etype = DTMDefaultBaseTraversers.this._exptype(current);
                if (-1 == etype) {
                    return -1;
                }
            } while (etype != expandedTypeID);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
        }
    }

    private class NamespaceDeclsTraverser extends DTMAxisTraverser {
        /* synthetic */ NamespaceDeclsTraverser(DTMDefaultBaseTraversers this$0, NamespaceDeclsTraverser -this1) {
            this();
        }

        private NamespaceDeclsTraverser() {
        }

        public int next(int context, int current) {
            if (context == current) {
                return DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, false);
            }
            return DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, false);
        }

        public int next(int context, int current, int expandedTypeID) {
            if (context == current) {
                current = DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, false);
            } else {
                current = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, false);
            }
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current) != expandedTypeID) {
                current = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, false);
                if (-1 == current) {
                    return -1;
                }
            }
            return current;
        }
    }

    private class NamespaceTraverser extends DTMAxisTraverser {
        /* synthetic */ NamespaceTraverser(DTMDefaultBaseTraversers this$0, NamespaceTraverser -this1) {
            this();
        }

        private NamespaceTraverser() {
        }

        public int next(int context, int current) {
            if (context == current) {
                return DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, true);
            }
            return DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, true);
        }

        public int next(int context, int current, int expandedTypeID) {
            if (context == current) {
                current = DTMDefaultBaseTraversers.this.getFirstNamespaceNode(context, true);
            } else {
                current = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, true);
            }
            while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current) != expandedTypeID) {
                current = DTMDefaultBaseTraversers.this.getNextNamespaceNode(context, current, true);
                if (-1 == current) {
                    return -1;
                }
            }
            return current;
        }
    }

    private class ParentTraverser extends DTMAxisTraverser {
        /* synthetic */ ParentTraverser(DTMDefaultBaseTraversers this$0, ParentTraverser -this1) {
            this();
        }

        private ParentTraverser() {
        }

        public int first(int context) {
            return DTMDefaultBaseTraversers.this.getParent(context);
        }

        public int first(int current, int expandedTypeID) {
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current);
            do {
                current = DTMDefaultBaseTraversers.this.m_parent.elementAt(current);
                if (-1 == current) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current) != expandedTypeID);
            return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
        }

        public int next(int context, int current) {
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            return -1;
        }
    }

    private class PrecedingAndAncestorTraverser extends DTMAxisTraverser {
        /* synthetic */ PrecedingAndAncestorTraverser(DTMDefaultBaseTraversers this$0, PrecedingAndAncestorTraverser -this1) {
            this();
        }

        private PrecedingAndAncestorTraverser() {
        }

        public int next(int context, int current) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            for (current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1; current >= 0; current--) {
                short type = DTMDefaultBaseTraversers.this._type(current);
                if ((short) 2 != type && (short) 13 != type) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
            }
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            for (current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1; current >= 0; current--) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current) == expandedTypeID) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
            }
            return -1;
        }
    }

    private class PrecedingSiblingTraverser extends DTMAxisTraverser {
        /* synthetic */ PrecedingSiblingTraverser(DTMDefaultBaseTraversers this$0, PrecedingSiblingTraverser -this1) {
            this();
        }

        private PrecedingSiblingTraverser() {
        }

        public int next(int context, int current) {
            return DTMDefaultBaseTraversers.this.getPreviousSibling(current);
        }

        public int next(int context, int current, int expandedTypeID) {
            do {
                current = DTMDefaultBaseTraversers.this.getPreviousSibling(current);
                if (-1 == current) {
                    return -1;
                }
            } while (DTMDefaultBaseTraversers.this.getExpandedTypeID(current) != expandedTypeID);
            return current;
        }
    }

    private class PrecedingTraverser extends DTMAxisTraverser {
        /* synthetic */ PrecedingTraverser(DTMDefaultBaseTraversers this$0, PrecedingTraverser -this1) {
            this();
        }

        private PrecedingTraverser() {
        }

        protected boolean isAncestor(int contextIdent, int currentIdent) {
            contextIdent = DTMDefaultBaseTraversers.this.m_parent.elementAt(contextIdent);
            while (-1 != contextIdent) {
                if (contextIdent == currentIdent) {
                    return true;
                }
                contextIdent = DTMDefaultBaseTraversers.this.m_parent.elementAt(contextIdent);
            }
            return false;
        }

        public int next(int context, int current) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1;
            while (current >= 0) {
                short type = DTMDefaultBaseTraversers.this._type(current);
                if ((short) 2 != type && (short) 13 != type && !isAncestor(subtreeRootIdent, current)) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
                current--;
            }
            return -1;
        }

        public int next(int context, int current, int expandedTypeID) {
            int subtreeRootIdent = DTMDefaultBaseTraversers.this.makeNodeIdentity(context);
            current = DTMDefaultBaseTraversers.this.makeNodeIdentity(current) - 1;
            while (current >= 0) {
                if (DTMDefaultBaseTraversers.this.m_exptype.elementAt(current) == expandedTypeID && !isAncestor(subtreeRootIdent, current)) {
                    return DTMDefaultBaseTraversers.this.makeNodeHandle(current);
                }
                current--;
            }
            return -1;
        }
    }

    private class RootTraverser extends AllFromRootTraverser {
        /* synthetic */ RootTraverser(DTMDefaultBaseTraversers this$0, RootTraverser -this1) {
            this();
        }

        private RootTraverser() {
            super(DTMDefaultBaseTraversers.this, null);
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
        /* synthetic */ SelfTraverser(DTMDefaultBaseTraversers this$0, SelfTraverser -this1) {
            this();
        }

        private SelfTraverser() {
        }

        public int first(int context) {
            return context;
        }

        public int first(int context, int expandedTypeID) {
            return DTMDefaultBaseTraversers.this.getExpandedTypeID(context) == expandedTypeID ? context : -1;
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
            traverser = this.m_traversers[axis];
            if (traverser != null) {
                return traverser;
            }
        }
        switch (axis) {
            case 0:
                traverser = new AncestorTraverser(this, null);
                break;
            case 1:
                traverser = new AncestorOrSelfTraverser(this, null);
                break;
            case 2:
                traverser = new AttributeTraverser(this, null);
                break;
            case 3:
                traverser = new ChildTraverser(this, null);
                break;
            case 4:
                traverser = new DescendantTraverser(this, null);
                break;
            case 5:
                traverser = new DescendantOrSelfTraverser(this, null);
                break;
            case 6:
                traverser = new FollowingTraverser(this, null);
                break;
            case 7:
                traverser = new FollowingSiblingTraverser(this, null);
                break;
            case 8:
                traverser = new NamespaceDeclsTraverser(this, null);
                break;
            case 9:
                traverser = new NamespaceTraverser(this, null);
                break;
            case 10:
                traverser = new ParentTraverser(this, null);
                break;
            case 11:
                traverser = new PrecedingTraverser(this, null);
                break;
            case 12:
                traverser = new PrecedingSiblingTraverser(this, null);
                break;
            case 13:
                traverser = new SelfTraverser(this, null);
                break;
            case 14:
                traverser = new AllFromNodeTraverser(this, null);
                break;
            case 15:
                traverser = new PrecedingAndAncestorTraverser(this, null);
                break;
            case 16:
                traverser = new AllFromRootTraverser(this, null);
                break;
            case 17:
                traverser = new DescendantFromRootTraverser(this, null);
                break;
            case 18:
                traverser = new DescendantOrSelfFromRootTraverser(this, null);
                break;
            case 19:
                traverser = new RootTraverser(this, null);
                break;
            case 20:
                return null;
            default:
                throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_UNKNOWN_AXIS_TYPE, new Object[]{Integer.toString(axis)}));
        }
        if (traverser == null) {
            throw new DTMException(XMLMessages.createXMLMessage(XMLErrorResources.ER_AXIS_TRAVERSER_NOT_SUPPORTED, new Object[]{Axis.getNames(axis)}));
        }
        this.m_traversers[axis] = traverser;
        return traverser;
    }
}

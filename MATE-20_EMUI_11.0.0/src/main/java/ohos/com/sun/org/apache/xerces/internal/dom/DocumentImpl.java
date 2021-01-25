package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.dom.events.EventImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.events.MutationEventImpl;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.events.DocumentEvent;
import ohos.org.w3c.dom.events.Event;
import ohos.org.w3c.dom.events.EventException;
import ohos.org.w3c.dom.events.EventListener;
import ohos.org.w3c.dom.ranges.DocumentRange;
import ohos.org.w3c.dom.ranges.Range;
import ohos.org.w3c.dom.traversal.DocumentTraversal;
import ohos.org.w3c.dom.traversal.NodeFilter;
import ohos.org.w3c.dom.traversal.NodeIterator;
import ohos.org.w3c.dom.traversal.TreeWalker;

public class DocumentImpl extends CoreDocumentImpl implements DocumentTraversal, DocumentEvent, DocumentRange {
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("iterators", Vector.class), new ObjectStreamField("ranges", Vector.class), new ObjectStreamField("eventListeners", Hashtable.class), new ObjectStreamField("mutationEvents", Boolean.TYPE)};
    static final long serialVersionUID = 515687835542616694L;
    protected Map<NodeImpl, List<LEntry>> eventListeners;
    protected List<NodeIterator> iterators;
    protected boolean mutationEvents = false;
    protected List<Range> ranges;
    EnclosingAttr savedEnclosingAttr;

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void renamedAttrNode(Attr attr, Attr attr2) {
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void renamedElement(Element element, Element element2) {
    }

    public DocumentImpl() {
    }

    public DocumentImpl(boolean z) {
        super(z);
    }

    public DocumentImpl(DocumentType documentType) {
        super(documentType);
    }

    public DocumentImpl(DocumentType documentType, boolean z) {
        super(documentType, z);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl, ohos.com.sun.org.apache.xerces.internal.dom.ParentNode, ohos.com.sun.org.apache.xerces.internal.dom.ChildNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node cloneNode(boolean z) {
        Node documentImpl = new DocumentImpl();
        callUserDataHandlers(this, documentImpl, 1);
        cloneNode(documentImpl, z);
        documentImpl.mutationEvents = this.mutationEvents;
        return documentImpl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public DOMImplementation getImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }

    public NodeIterator createNodeIterator(Node node, short s, NodeFilter nodeFilter) {
        return createNodeIterator(node, s, nodeFilter, true);
    }

    public NodeIterator createNodeIterator(Node node, int i, NodeFilter nodeFilter, boolean z) {
        if (node != null) {
            NodeIterator nodeIteratorImpl = new NodeIteratorImpl(this, node, i, nodeFilter, z);
            if (this.iterators == null) {
                this.iterators = new ArrayList();
            }
            this.iterators.add(nodeIteratorImpl);
            return nodeIteratorImpl;
        }
        throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null));
    }

    public TreeWalker createTreeWalker(Node node, short s, NodeFilter nodeFilter) {
        return createTreeWalker(node, s, nodeFilter, true);
    }

    public TreeWalker createTreeWalker(Node node, int i, NodeFilter nodeFilter, boolean z) {
        if (node != null) {
            return new TreeWalkerImpl(node, i, nodeFilter, z);
        }
        throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null));
    }

    /* access modifiers changed from: package-private */
    public void removeNodeIterator(NodeIterator nodeIterator) {
        List<NodeIterator> list;
        if (nodeIterator != null && (list = this.iterators) != null) {
            list.remove(nodeIterator);
        }
    }

    public Range createRange() {
        if (this.ranges == null) {
            this.ranges = new ArrayList();
        }
        Range rangeImpl = new RangeImpl(this);
        this.ranges.add(rangeImpl);
        return rangeImpl;
    }

    /* access modifiers changed from: package-private */
    public void removeRange(Range range) {
        List<Range> list;
        if (range != null && (list = this.ranges) != null) {
            list.remove(range);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void replacedText(NodeImpl nodeImpl) {
        List<Range> list = this.ranges;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i != size; i++) {
                this.ranges.get(i).receiveReplacedText(nodeImpl);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void deletedText(NodeImpl nodeImpl, int i, int i2) {
        List<Range> list = this.ranges;
        if (list != null) {
            int size = list.size();
            for (int i3 = 0; i3 != size; i3++) {
                this.ranges.get(i3).receiveDeletedText(nodeImpl, i, i2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void insertedText(NodeImpl nodeImpl, int i, int i2) {
        List<Range> list = this.ranges;
        if (list != null) {
            int size = list.size();
            for (int i3 = 0; i3 != size; i3++) {
                this.ranges.get(i3).receiveInsertedText(nodeImpl, i, i2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void splitData(Node node, Node node2, int i) {
        List<Range> list = this.ranges;
        if (list != null) {
            int size = list.size();
            for (int i2 = 0; i2 != size; i2++) {
                this.ranges.get(i2).receiveSplitData(node, node2, i);
            }
        }
    }

    public Event createEvent(String str) throws DOMException {
        if (str.equalsIgnoreCase("Events") || "Event".equals(str)) {
            return new EventImpl();
        }
        if (str.equalsIgnoreCase("MutationEvents") || "MutationEvent".equals(str)) {
            return new MutationEventImpl();
        }
        throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null));
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void setMutationEvents(boolean z) {
        this.mutationEvents = z;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public boolean getMutationEvents() {
        return this.mutationEvents;
    }

    private void setEventListeners(NodeImpl nodeImpl, List<LEntry> list) {
        if (this.eventListeners == null) {
            this.eventListeners = new HashMap();
        }
        if (list == null) {
            this.eventListeners.remove(nodeImpl);
            if (this.eventListeners.isEmpty()) {
                this.mutationEvents = false;
                return;
            }
            return;
        }
        this.eventListeners.put(nodeImpl, list);
        this.mutationEvents = true;
    }

    private List<LEntry> getEventListeners(NodeImpl nodeImpl) {
        Map<NodeImpl, List<LEntry>> map = this.eventListeners;
        if (map == null) {
            return null;
        }
        return map.get(nodeImpl);
    }

    /* access modifiers changed from: package-private */
    public class LEntry implements Serializable {
        private static final long serialVersionUID = -8426757059492421631L;
        EventListener listener;
        String type;
        boolean useCapture;

        LEntry(String str, EventListener eventListener, boolean z) {
            this.type = str;
            this.listener = eventListener;
            this.useCapture = z;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void addEventListener(NodeImpl nodeImpl, String str, EventListener eventListener, boolean z) {
        if (str != null && !str.equals("") && eventListener != null) {
            removeEventListener(nodeImpl, str, eventListener, z);
            List<LEntry> eventListeners2 = getEventListeners(nodeImpl);
            if (eventListeners2 == null) {
                eventListeners2 = new ArrayList<>();
                setEventListeners(nodeImpl, eventListeners2);
            }
            eventListeners2.add(new LEntry(str, eventListener, z));
            LCount lookup = LCount.lookup(str);
            if (z) {
                lookup.captures++;
                lookup.total++;
                return;
            }
            lookup.bubbles++;
            lookup.total++;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void removeEventListener(NodeImpl nodeImpl, String str, EventListener eventListener, boolean z) {
        List<LEntry> eventListeners2;
        if (!(str == null || str.equals("") || eventListener == null || (eventListeners2 = getEventListeners(nodeImpl)) == null)) {
            for (int size = eventListeners2.size() - 1; size >= 0; size--) {
                LEntry lEntry = eventListeners2.get(size);
                if (lEntry.useCapture == z && lEntry.listener == eventListener && lEntry.type.equals(str)) {
                    eventListeners2.remove(size);
                    if (eventListeners2.isEmpty()) {
                        setEventListeners(nodeImpl, null);
                    }
                    LCount lookup = LCount.lookup(str);
                    if (z) {
                        lookup.captures--;
                        lookup.total--;
                        return;
                    }
                    lookup.bubbles--;
                    lookup.total--;
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void copyEventListeners(NodeImpl nodeImpl, NodeImpl nodeImpl2) {
        List<LEntry> eventListeners2 = getEventListeners(nodeImpl);
        if (eventListeners2 != null) {
            setEventListeners(nodeImpl2, new ArrayList(eventListeners2));
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public boolean dispatchEvent(NodeImpl nodeImpl, Event event) {
        if (event == null) {
            return false;
        }
        EventImpl eventImpl = (EventImpl) event;
        if (!eventImpl.initialized || eventImpl.type == null || eventImpl.type.equals("")) {
            throw new EventException(0, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "UNSPECIFIED_EVENT_TYPE_ERR", null));
        }
        LCount lookup = LCount.lookup(eventImpl.getType());
        if (lookup.total == 0) {
            return eventImpl.preventDefault;
        }
        eventImpl.target = nodeImpl;
        eventImpl.stopPropagation = false;
        eventImpl.preventDefault = false;
        ArrayList arrayList = new ArrayList(10);
        for (Node parentNode = nodeImpl.getParentNode(); parentNode != null; parentNode = parentNode.getParentNode()) {
            arrayList.add(parentNode);
        }
        if (lookup.captures > 0) {
            eventImpl.eventPhase = 1;
            for (int size = arrayList.size() - 1; size >= 0 && !eventImpl.stopPropagation; size--) {
                NodeImpl nodeImpl2 = (NodeImpl) arrayList.get(size);
                eventImpl.currentTarget = nodeImpl2;
                List<LEntry> eventListeners2 = getEventListeners(nodeImpl2);
                if (eventListeners2 != null) {
                    List list = (List) ((ArrayList) eventListeners2).clone();
                    int size2 = list.size();
                    for (int i = 0; i < size2; i++) {
                        LEntry lEntry = (LEntry) list.get(i);
                        if (lEntry.useCapture && lEntry.type.equals(eventImpl.type) && eventListeners2.contains(lEntry)) {
                            try {
                                lEntry.listener.handleEvent(eventImpl);
                            } catch (Exception unused) {
                            }
                        }
                    }
                }
            }
        }
        if (lookup.bubbles > 0) {
            eventImpl.eventPhase = 2;
            eventImpl.currentTarget = nodeImpl;
            List<LEntry> eventListeners3 = getEventListeners(nodeImpl);
            if (!eventImpl.stopPropagation && eventListeners3 != null) {
                List list2 = (List) ((ArrayList) eventListeners3).clone();
                int size3 = list2.size();
                for (int i2 = 0; i2 < size3; i2++) {
                    LEntry lEntry2 = (LEntry) list2.get(i2);
                    if (!lEntry2.useCapture && lEntry2.type.equals(eventImpl.type) && eventListeners3.contains(lEntry2)) {
                        try {
                            lEntry2.listener.handleEvent(eventImpl);
                        } catch (Exception unused2) {
                        }
                    }
                }
            }
            if (eventImpl.bubbles) {
                eventImpl.eventPhase = 3;
                int size4 = arrayList.size();
                for (int i3 = 0; i3 < size4 && !eventImpl.stopPropagation; i3++) {
                    NodeImpl nodeImpl3 = (NodeImpl) arrayList.get(i3);
                    eventImpl.currentTarget = nodeImpl3;
                    List<LEntry> eventListeners4 = getEventListeners(nodeImpl3);
                    if (eventListeners4 != null) {
                        List list3 = (List) ((ArrayList) eventListeners4).clone();
                        int size5 = list3.size();
                        for (int i4 = 0; i4 < size5; i4++) {
                            LEntry lEntry3 = (LEntry) list3.get(i4);
                            if (!lEntry3.useCapture && lEntry3.type.equals(eventImpl.type) && eventListeners4.contains(lEntry3)) {
                                try {
                                    lEntry3.listener.handleEvent(eventImpl);
                                } catch (Exception unused3) {
                                }
                            }
                        }
                    }
                }
            }
        }
        if (lookup.defaults > 0 && eventImpl.cancelable) {
            boolean z = eventImpl.preventDefault;
        }
        return eventImpl.preventDefault;
    }

    /* access modifiers changed from: protected */
    public void dispatchEventToSubtree(Node node, Event event) {
        ((NodeImpl) node).dispatchEvent(event);
        if (node.getNodeType() == 1) {
            NamedNodeMap attributes = node.getAttributes();
            for (int length = attributes.getLength() - 1; length >= 0; length--) {
                dispatchingEventToSubtree(attributes.item(length), event);
            }
        }
        dispatchingEventToSubtree(node.getFirstChild(), event);
    }

    /* access modifiers changed from: protected */
    public void dispatchingEventToSubtree(Node node, Event event) {
        if (node != null) {
            ((NodeImpl) node).dispatchEvent(event);
            if (node.getNodeType() == 1) {
                NamedNodeMap attributes = node.getAttributes();
                for (int length = attributes.getLength() - 1; length >= 0; length--) {
                    dispatchingEventToSubtree(attributes.item(length), event);
                }
            }
            dispatchingEventToSubtree(node.getFirstChild(), event);
            dispatchingEventToSubtree(node.getNextSibling(), event);
        }
    }

    /* access modifiers changed from: package-private */
    public class EnclosingAttr implements Serializable {
        private static final long serialVersionUID = 5208387723391647216L;
        AttrImpl node;
        String oldvalue;

        EnclosingAttr() {
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchAggregateEvents(NodeImpl nodeImpl, EnclosingAttr enclosingAttr) {
        if (enclosingAttr != null) {
            dispatchAggregateEvents(nodeImpl, enclosingAttr.node, enclosingAttr.oldvalue, 1);
        } else {
            dispatchAggregateEvents(nodeImpl, null, null, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchAggregateEvents(NodeImpl nodeImpl, AttrImpl attrImpl, String str, short s) {
        NodeImpl nodeImpl2;
        if (attrImpl != null) {
            LCount lookup = LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            nodeImpl2 = (NodeImpl) attrImpl.getOwnerElement();
            if (lookup.total > 0 && nodeImpl2 != null) {
                MutationEventImpl mutationEventImpl = new MutationEventImpl();
                mutationEventImpl.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED, true, false, attrImpl, str, attrImpl.getNodeValue(), attrImpl.getNodeName(), s);
                nodeImpl2.dispatchEvent(mutationEventImpl);
            }
        } else {
            nodeImpl2 = null;
        }
        if (LCount.lookup(MutationEventImpl.DOM_SUBTREE_MODIFIED).total > 0) {
            MutationEventImpl mutationEventImpl2 = new MutationEventImpl();
            mutationEventImpl2.initMutationEvent(MutationEventImpl.DOM_SUBTREE_MODIFIED, true, false, (Node) null, (String) null, (String) null, (String) null, 0);
            if (attrImpl != null) {
                dispatchEvent(attrImpl, mutationEventImpl2);
                if (nodeImpl2 != null) {
                    dispatchEvent(nodeImpl2, mutationEventImpl2);
                    return;
                }
                return;
            }
            dispatchEvent(nodeImpl, mutationEventImpl2);
        }
    }

    /* access modifiers changed from: protected */
    public void saveEnclosingAttr(NodeImpl nodeImpl) {
        this.savedEnclosingAttr = null;
        if (LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED).total > 0) {
            while (nodeImpl != null) {
                short nodeType = nodeImpl.getNodeType();
                if (nodeType == 2) {
                    EnclosingAttr enclosingAttr = new EnclosingAttr();
                    enclosingAttr.node = (AttrImpl) nodeImpl;
                    enclosingAttr.oldvalue = enclosingAttr.node.getNodeValue();
                    this.savedEnclosingAttr = enclosingAttr;
                    return;
                } else if (nodeType == 5) {
                    nodeImpl = nodeImpl.parentNode();
                } else if (nodeType == 3) {
                    nodeImpl = nodeImpl.parentNode();
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void modifyingCharacterData(NodeImpl nodeImpl, boolean z) {
        if (this.mutationEvents && !z) {
            saveEnclosingAttr(nodeImpl);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void modifiedCharacterData(NodeImpl nodeImpl, String str, String str2, boolean z) {
        if (this.mutationEvents && !z) {
            if (LCount.lookup(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED).total > 0) {
                MutationEventImpl mutationEventImpl = new MutationEventImpl();
                mutationEventImpl.initMutationEvent(MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED, true, false, (Node) null, str, str2, (String) null, 0);
                dispatchEvent(nodeImpl, mutationEventImpl);
            }
            dispatchAggregateEvents(nodeImpl, this.savedEnclosingAttr);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void replacedCharacterData(NodeImpl nodeImpl, String str, String str2) {
        modifiedCharacterData(nodeImpl, str, str2, false);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void insertingNode(NodeImpl nodeImpl, boolean z) {
        if (this.mutationEvents && !z) {
            saveEnclosingAttr(nodeImpl);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void insertedNode(NodeImpl nodeImpl, NodeImpl nodeImpl2, boolean z) {
        NodeImpl nodeImpl3;
        if (this.mutationEvents) {
            if (LCount.lookup(MutationEventImpl.DOM_NODE_INSERTED).total > 0) {
                MutationEventImpl mutationEventImpl = new MutationEventImpl();
                mutationEventImpl.initMutationEvent(MutationEventImpl.DOM_NODE_INSERTED, true, false, nodeImpl, null, null, null, 0);
                dispatchEvent(nodeImpl2, mutationEventImpl);
            }
            if (LCount.lookup(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT).total > 0) {
                EnclosingAttr enclosingAttr = this.savedEnclosingAttr;
                NodeImpl nodeImpl4 = enclosingAttr != null ? (NodeImpl) enclosingAttr.node.getOwnerElement() : nodeImpl;
                if (nodeImpl4 != null) {
                    NodeImpl nodeImpl5 = nodeImpl4;
                    while (nodeImpl4 != null) {
                        if (nodeImpl4.getNodeType() == 2) {
                            nodeImpl3 = (NodeImpl) ((AttrImpl) nodeImpl4).getOwnerElement();
                        } else {
                            nodeImpl3 = nodeImpl4.parentNode();
                        }
                        nodeImpl5 = nodeImpl4;
                        nodeImpl4 = nodeImpl3;
                    }
                    if (nodeImpl5.getNodeType() == 9) {
                        MutationEventImpl mutationEventImpl2 = new MutationEventImpl();
                        mutationEventImpl2.initMutationEvent(MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT, false, false, null, null, null, null, 0);
                        dispatchEventToSubtree(nodeImpl2, mutationEventImpl2);
                    }
                }
            }
            if (!z) {
                dispatchAggregateEvents(nodeImpl, this.savedEnclosingAttr);
            }
        }
        List<Range> list = this.ranges;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i != size; i++) {
                this.ranges.get(i).insertedNodeFromDOM(nodeImpl2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void removingNode(NodeImpl nodeImpl, NodeImpl nodeImpl2, boolean z) {
        List<NodeIterator> list = this.iterators;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i != size; i++) {
                this.iterators.get(i).removeNode(nodeImpl2);
            }
        }
        List<Range> list2 = this.ranges;
        if (list2 != null) {
            int size2 = list2.size();
            for (int i2 = 0; i2 != size2; i2++) {
                this.ranges.get(i2).removeNode(nodeImpl2);
            }
        }
        if (this.mutationEvents) {
            if (!z) {
                saveEnclosingAttr(nodeImpl);
            }
            if (LCount.lookup(MutationEventImpl.DOM_NODE_REMOVED).total > 0) {
                MutationEventImpl mutationEventImpl = new MutationEventImpl();
                mutationEventImpl.initMutationEvent(MutationEventImpl.DOM_NODE_REMOVED, true, false, nodeImpl, null, null, null, 0);
                dispatchEvent(nodeImpl2, mutationEventImpl);
            }
            if (LCount.lookup(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT).total > 0) {
                EnclosingAttr enclosingAttr = this.savedEnclosingAttr;
                NodeImpl nodeImpl3 = enclosingAttr != null ? (NodeImpl) enclosingAttr.node.getOwnerElement() : this;
                if (nodeImpl3 != null) {
                    NodeImpl parentNode = nodeImpl3.parentNode();
                    while (true) {
                        nodeImpl3 = parentNode;
                        if (nodeImpl3 == null) {
                            break;
                        }
                        parentNode = nodeImpl3.parentNode();
                    }
                    if (nodeImpl3.getNodeType() == 9) {
                        MutationEventImpl mutationEventImpl2 = new MutationEventImpl();
                        mutationEventImpl2.initMutationEvent(MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT, false, false, null, null, null, null, 0);
                        dispatchEventToSubtree(nodeImpl2, mutationEventImpl2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void removedNode(NodeImpl nodeImpl, boolean z) {
        if (this.mutationEvents && !z) {
            dispatchAggregateEvents(nodeImpl, this.savedEnclosingAttr);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void replacingNode(NodeImpl nodeImpl) {
        if (this.mutationEvents) {
            saveEnclosingAttr(nodeImpl);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void replacingData(NodeImpl nodeImpl) {
        if (this.mutationEvents) {
            saveEnclosingAttr(nodeImpl);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void replacedNode(NodeImpl nodeImpl) {
        if (this.mutationEvents) {
            dispatchAggregateEvents(nodeImpl, this.savedEnclosingAttr);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void modifiedAttrValue(AttrImpl attrImpl, String str) {
        if (this.mutationEvents) {
            dispatchAggregateEvents(attrImpl, attrImpl, str, 1);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void setAttrNode(AttrImpl attrImpl, AttrImpl attrImpl2) {
        if (!this.mutationEvents) {
            return;
        }
        if (attrImpl2 == null) {
            dispatchAggregateEvents(attrImpl.ownerNode, attrImpl, null, 2);
        } else {
            dispatchAggregateEvents(attrImpl.ownerNode, attrImpl, attrImpl2.getNodeValue(), 1);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl
    public void removedAttrNode(AttrImpl attrImpl, NodeImpl nodeImpl, String str) {
        if (this.mutationEvents) {
            if (LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED).total > 0) {
                MutationEventImpl mutationEventImpl = new MutationEventImpl();
                mutationEventImpl.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED, true, false, attrImpl, attrImpl.getNodeValue(), null, str, 3);
                dispatchEvent(nodeImpl, mutationEventImpl);
            }
            dispatchAggregateEvents(nodeImpl, null, null, 0);
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        List<NodeIterator> list = this.iterators;
        Hashtable hashtable = null;
        Vector vector = list == null ? null : new Vector(list);
        List<Range> list2 = this.ranges;
        Vector vector2 = list2 == null ? null : new Vector(list2);
        if (this.eventListeners != null) {
            hashtable = new Hashtable();
            for (Map.Entry<NodeImpl, List<LEntry>> entry : this.eventListeners.entrySet()) {
                hashtable.put(entry.getKey(), new Vector(entry.getValue()));
            }
        }
        ObjectOutputStream.PutField putFields = objectOutputStream.putFields();
        putFields.put("iterators", vector);
        putFields.put("ranges", vector2);
        putFields.put("eventListeners", hashtable);
        putFields.put("mutationEvents", this.mutationEvents);
        objectOutputStream.writeFields();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField readFields = objectInputStream.readFields();
        Vector vector = (Vector) readFields.get("iterators", (Object) null);
        Vector vector2 = (Vector) readFields.get("ranges", (Object) null);
        Hashtable hashtable = (Hashtable) readFields.get("eventListeners", (Object) null);
        this.mutationEvents = readFields.get("mutationEvents", false);
        if (vector != null) {
            this.iterators = new ArrayList(vector);
        }
        if (vector2 != null) {
            this.ranges = new ArrayList(vector2);
        }
        if (hashtable != null) {
            this.eventListeners = new HashMap();
            for (Map.Entry entry : hashtable.entrySet()) {
                this.eventListeners.put((NodeImpl) entry.getKey(), new ArrayList((Collection) entry.getValue()));
            }
        }
    }
}

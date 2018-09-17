package org.apache.xpath.axes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class LocPathIterator extends PredicatedNodeTest implements Cloneable, DTMIterator, Serializable, PathComponent {
    static final long serialVersionUID = -4602476357268405754L;
    protected boolean m_allowDetach;
    protected transient DTM m_cdtm;
    protected transient IteratorPool m_clones;
    protected transient int m_context;
    protected transient int m_currentContextNode;
    protected transient XPathContext m_execContext;
    private boolean m_isTopLevel;
    public transient int m_lastFetched;
    protected transient int m_length;
    protected transient int m_pos;
    private PrefixResolver m_prefixResolver;
    transient int m_stackFrame;

    public abstract int nextNode();

    protected LocPathIterator() {
        this.m_allowDetach = true;
        this.m_clones = new IteratorPool(this);
        this.m_stackFrame = -1;
        this.m_isTopLevel = false;
        this.m_lastFetched = -1;
        this.m_context = -1;
        this.m_currentContextNode = -1;
        this.m_pos = 0;
        this.m_length = -1;
    }

    protected LocPathIterator(PrefixResolver nscontext) {
        this.m_allowDetach = true;
        this.m_clones = new IteratorPool(this);
        this.m_stackFrame = -1;
        this.m_isTopLevel = false;
        this.m_lastFetched = -1;
        this.m_context = -1;
        this.m_currentContextNode = -1;
        this.m_pos = 0;
        this.m_length = -1;
        setLocPathIterator(this);
        this.m_prefixResolver = nscontext;
    }

    protected LocPathIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        this(compiler, opPos, analysis, true);
    }

    protected LocPathIterator(Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers) throws TransformerException {
        this.m_allowDetach = true;
        this.m_clones = new IteratorPool(this);
        this.m_stackFrame = -1;
        this.m_isTopLevel = false;
        this.m_lastFetched = -1;
        this.m_context = -1;
        this.m_currentContextNode = -1;
        this.m_pos = 0;
        this.m_length = -1;
        setLocPathIterator(this);
    }

    public int getAnalysisBits() {
        return WalkerFactory.getAnalysisBitFromAxes(getAxis());
    }

    private void readObject(ObjectInputStream stream) throws IOException, TransformerException {
        try {
            stream.defaultReadObject();
            this.m_clones = new IteratorPool(this);
        } catch (ClassNotFoundException cnfe) {
            throw new TransformerException(cnfe);
        }
    }

    public void setEnvironment(Object environment) {
    }

    public DTM getDTM(int nodeHandle) {
        return this.m_execContext.getDTM(nodeHandle);
    }

    public DTMManager getDTMManager() {
        return this.m_execContext.getDTMManager();
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        XNodeSet iter = new XNodeSet((LocPathIterator) this.m_clones.getInstance());
        iter.setRoot(xctxt.getCurrentNode(), xctxt);
        return iter;
    }

    public void executeCharsToContentHandler(XPathContext xctxt, ContentHandler handler) throws TransformerException, SAXException {
        LocPathIterator clone = (LocPathIterator) this.m_clones.getInstance();
        clone.setRoot(xctxt.getCurrentNode(), xctxt);
        int node = clone.nextNode();
        DTM dtm = clone.getDTM(node);
        clone.detach();
        if (node != -1) {
            dtm.dispatchCharactersEvents(node, handler, false);
        }
    }

    public DTMIterator asIterator(XPathContext xctxt, int contextNode) throws TransformerException {
        XNodeSet iter = new XNodeSet((LocPathIterator) this.m_clones.getInstance());
        iter.setRoot(contextNode, xctxt);
        return iter;
    }

    public boolean isNodesetExpr() {
        return true;
    }

    public int asNode(XPathContext xctxt) throws TransformerException {
        DTMIterator iter = this.m_clones.getInstance();
        iter.setRoot(xctxt.getCurrentNode(), xctxt);
        int next = iter.nextNode();
        iter.detach();
        return next;
    }

    public boolean bool(XPathContext xctxt) throws TransformerException {
        return asNode(xctxt) != -1;
    }

    public void setIsTopLevel(boolean b) {
        this.m_isTopLevel = b;
    }

    public boolean getIsTopLevel() {
        return this.m_isTopLevel;
    }

    public void setRoot(int context, Object environment) {
        this.m_context = context;
        XPathContext xctxt = (XPathContext) environment;
        this.m_execContext = xctxt;
        this.m_cdtm = xctxt.getDTM(context);
        this.m_currentContextNode = context;
        if (this.m_prefixResolver == null) {
            this.m_prefixResolver = xctxt.getNamespaceContext();
        }
        this.m_lastFetched = -1;
        this.m_foundLast = false;
        this.m_pos = 0;
        this.m_length = -1;
        if (this.m_isTopLevel) {
            this.m_stackFrame = xctxt.getVarStack().getStackFrame();
        }
    }

    protected void setNextPosition(int next) {
        assertion(false, "setNextPosition not supported in this iterator!");
    }

    public final int getCurrentPos() {
        return this.m_pos;
    }

    public void setShouldCacheNodes(boolean b) {
        assertion(false, "setShouldCacheNodes not supported by this iterater!");
    }

    public boolean isMutable() {
        return false;
    }

    public void setCurrentPos(int i) {
        assertion(false, "setCurrentPos not supported by this iterator!");
    }

    public void incrementCurrentPos() {
        this.m_pos++;
    }

    public int size() {
        assertion(false, "size() not supported by this iterator!");
        return 0;
    }

    public int item(int index) {
        assertion(false, "item(int index) not supported by this iterator!");
        return 0;
    }

    public void setItem(int node, int index) {
        assertion(false, "setItem not supported by this iterator!");
    }

    public int getLength() {
        boolean isPredicateTest = this == this.m_execContext.getSubContextList();
        int predCount = getPredicateCount();
        if (-1 != this.m_length && isPredicateTest && this.m_predicateIndex < 1) {
            return this.m_length;
        }
        if (this.m_foundLast) {
            return this.m_pos;
        }
        int pos = this.m_predicateIndex >= 0 ? getProximityPosition() : this.m_pos;
        try {
            LocPathIterator clone = (LocPathIterator) clone();
            if (predCount > 0 && isPredicateTest) {
                clone.m_predCount = this.m_predicateIndex;
            }
            while (-1 != clone.nextNode()) {
                pos++;
            }
            if (isPredicateTest && this.m_predicateIndex < 1) {
                this.m_length = pos;
            }
            return pos;
        } catch (CloneNotSupportedException e) {
            return -1;
        }
    }

    public boolean isFresh() {
        return this.m_pos == 0;
    }

    public int previousNode() {
        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NODESETDTM_CANNOT_ITERATE, null));
    }

    public int getWhatToShow() {
        return -17;
    }

    public DTMFilter getFilter() {
        return null;
    }

    public int getRoot() {
        return this.m_context;
    }

    public boolean getExpandEntityReferences() {
        return true;
    }

    public void allowDetachToRelease(boolean allowRelease) {
        this.m_allowDetach = allowRelease;
    }

    public void detach() {
        if (this.m_allowDetach) {
            this.m_execContext = null;
            this.m_cdtm = null;
            this.m_length = -1;
            this.m_pos = 0;
            this.m_lastFetched = -1;
            this.m_context = -1;
            this.m_currentContextNode = -1;
            this.m_clones.freeInstance(this);
        }
    }

    public void reset() {
        assertion(false, "This iterator can not reset!");
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        LocPathIterator clone = (LocPathIterator) this.m_clones.getInstanceOrThrow();
        clone.m_execContext = this.m_execContext;
        clone.m_cdtm = this.m_cdtm;
        clone.m_context = this.m_context;
        clone.m_currentContextNode = this.m_currentContextNode;
        clone.m_stackFrame = this.m_stackFrame;
        return clone;
    }

    protected int returnNextNode(int nextNode) {
        if (-1 != nextNode) {
            this.m_pos++;
        }
        this.m_lastFetched = nextNode;
        if (-1 == nextNode) {
            this.m_foundLast = true;
        }
        return nextNode;
    }

    public int getCurrentNode() {
        return this.m_lastFetched;
    }

    public void runTo(int index) {
        if (!this.m_foundLast && (index < 0 || index > getCurrentPos())) {
            if (-1 != index) {
                while (-1 != nextNode()) {
                    if (getCurrentPos() >= index) {
                        break;
                    }
                }
            }
            do {
            } while (-1 != nextNode());
        }
    }

    public final boolean getFoundLast() {
        return this.m_foundLast;
    }

    public final XPathContext getXPathContext() {
        return this.m_execContext;
    }

    public final int getContext() {
        return this.m_context;
    }

    public final int getCurrentContextNode() {
        return this.m_currentContextNode;
    }

    public final void setCurrentContextNode(int n) {
        this.m_currentContextNode = n;
    }

    public final PrefixResolver getPrefixResolver() {
        if (this.m_prefixResolver == null) {
            this.m_prefixResolver = (PrefixResolver) getExpressionOwner();
        }
        return this.m_prefixResolver;
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitLocationPath(owner, this)) {
            visitor.visitStep(owner, this);
            callPredicateVisitors(visitor);
        }
    }

    public boolean isDocOrdered() {
        return true;
    }

    public int getAxis() {
        return -1;
    }

    public int getLastPos(XPathContext xctxt) {
        return getLength();
    }
}

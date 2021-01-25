package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.VariableStack;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.OpMap;
import ohos.javax.xml.transform.TransformerException;

public class WalkingIterator extends LocPathIterator implements ExpressionOwner {
    static final long serialVersionUID = 9110225941815665906L;
    protected AxesWalker m_firstWalker;
    protected AxesWalker m_lastUsedWalker;

    WalkingIterator(Compiler compiler, int i, int i2, boolean z) throws TransformerException {
        super(compiler, i, i2, z);
        int firstChildPos = OpMap.getFirstChildPos(i);
        if (z) {
            this.m_firstWalker = WalkerFactory.loadWalkers(this, compiler, firstChildPos, 0);
            this.m_lastUsedWalker = this.m_firstWalker;
        }
    }

    public WalkingIterator(PrefixResolver prefixResolver) {
        super(prefixResolver);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xpath.internal.axes.PathComponent
    public int getAnalysisBits() {
        AxesWalker axesWalker = this.m_firstWalker;
        int i = 0;
        if (axesWalker != null) {
            while (axesWalker != null) {
                i |= axesWalker.getAnalysisBits();
                axesWalker = axesWalker.getNextWalker();
            }
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        WalkingIterator walkingIterator = (WalkingIterator) super.clone();
        AxesWalker axesWalker = this.m_firstWalker;
        if (axesWalker != null) {
            walkingIterator.m_firstWalker = axesWalker.cloneDeep(walkingIterator, null);
        }
        return walkingIterator;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void reset() {
        super.reset();
        AxesWalker axesWalker = this.m_firstWalker;
        if (axesWalker != null) {
            this.m_lastUsedWalker = axesWalker;
            axesWalker.setRoot(this.m_context);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        AxesWalker axesWalker = this.m_firstWalker;
        if (axesWalker != null) {
            axesWalker.setRoot(i);
            this.m_lastUsedWalker = this.m_firstWalker;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        if (-1 == this.m_stackFrame) {
            return returnNextNode(this.m_firstWalker.nextNode());
        }
        VariableStack varStack = this.m_execContext.getVarStack();
        int stackFrame = varStack.getStackFrame();
        varStack.setStackFrame(this.m_stackFrame);
        int returnNextNode = returnNextNode(this.m_firstWalker.nextNode());
        varStack.setStackFrame(stackFrame);
        return returnNextNode;
    }

    public final AxesWalker getFirstWalker() {
        return this.m_firstWalker;
    }

    public final void setFirstWalker(AxesWalker axesWalker) {
        this.m_firstWalker = axesWalker;
    }

    public final void setLastUsedWalker(AxesWalker axesWalker) {
        this.m_lastUsedWalker = axesWalker;
    }

    public final AxesWalker getLastUsedWalker() {
        return this.m_lastUsedWalker;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        if (this.m_allowDetach) {
            for (AxesWalker axesWalker = this.m_firstWalker; axesWalker != null; axesWalker = axesWalker.getNextWalker()) {
                axesWalker.detach();
            }
            this.m_lastUsedWalker = null;
            super.detach();
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        this.m_predicateIndex = -1;
        for (AxesWalker axesWalker = this.m_firstWalker; axesWalker != null; axesWalker = axesWalker.getNextWalker()) {
            axesWalker.fixupVariables(vector, i);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        AxesWalker axesWalker;
        if (xPathVisitor.visitLocationPath(expressionOwner, this) && (axesWalker = this.m_firstWalker) != null) {
            axesWalker.callVisitors(this, xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public Expression getExpression() {
        return this.m_firstWalker;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public void setExpression(Expression expression) {
        expression.exprSetParent(this);
        this.m_firstWalker = (AxesWalker) expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!super.deepEquals(expression)) {
            return false;
        }
        AxesWalker axesWalker = this.m_firstWalker;
        AxesWalker axesWalker2 = ((WalkingIterator) expression).m_firstWalker;
        while (axesWalker != null && axesWalker2 != null) {
            if (!axesWalker.deepEquals(axesWalker2)) {
                return false;
            }
            axesWalker = axesWalker.getNextWalker();
            axesWalker2 = axesWalker2.getNextWalker();
        }
        if (axesWalker == null && axesWalker2 == null) {
            return true;
        }
        return false;
    }
}

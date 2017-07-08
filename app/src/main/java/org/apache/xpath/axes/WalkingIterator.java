package org.apache.xpath.axes;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;

public class WalkingIterator extends LocPathIterator implements ExpressionOwner {
    static final long serialVersionUID = 9110225941815665906L;
    protected AxesWalker m_firstWalker;
    protected AxesWalker m_lastUsedWalker;

    WalkingIterator(Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers) throws TransformerException {
        super(compiler, opPos, analysis, shouldLoadWalkers);
        int firstStepPos = OpMap.getFirstChildPos(opPos);
        if (shouldLoadWalkers) {
            this.m_firstWalker = WalkerFactory.loadWalkers(this, compiler, firstStepPos, 0);
            this.m_lastUsedWalker = this.m_firstWalker;
        }
    }

    public WalkingIterator(PrefixResolver nscontext) {
        super(nscontext);
    }

    public int getAnalysisBits() {
        int bits = 0;
        if (this.m_firstWalker != null) {
            for (AxesWalker walker = this.m_firstWalker; walker != null; walker = walker.getNextWalker()) {
                bits |= walker.getAnalysisBits();
            }
        }
        return bits;
    }

    public Object clone() throws CloneNotSupportedException {
        WalkingIterator clone = (WalkingIterator) super.clone();
        if (this.m_firstWalker != null) {
            clone.m_firstWalker = this.m_firstWalker.cloneDeep(clone, null);
        }
        return clone;
    }

    public void reset() {
        super.reset();
        if (this.m_firstWalker != null) {
            this.m_lastUsedWalker = this.m_firstWalker;
            this.m_firstWalker.setRoot(this.m_context);
        }
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        if (this.m_firstWalker != null) {
            this.m_firstWalker.setRoot(context);
            this.m_lastUsedWalker = this.m_firstWalker;
        }
    }

    public int nextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        if (-1 == this.m_stackFrame) {
            return returnNextNode(this.m_firstWalker.nextNode());
        }
        VariableStack vars = this.m_execContext.getVarStack();
        int savedStart = vars.getStackFrame();
        vars.setStackFrame(this.m_stackFrame);
        int n = returnNextNode(this.m_firstWalker.nextNode());
        vars.setStackFrame(savedStart);
        return n;
    }

    public final AxesWalker getFirstWalker() {
        return this.m_firstWalker;
    }

    public final void setFirstWalker(AxesWalker walker) {
        this.m_firstWalker = walker;
    }

    public final void setLastUsedWalker(AxesWalker walker) {
        this.m_lastUsedWalker = walker;
    }

    public final AxesWalker getLastUsedWalker() {
        return this.m_lastUsedWalker;
    }

    public void detach() {
        if (this.m_allowDetach) {
            for (AxesWalker walker = this.m_firstWalker; walker != null; walker = walker.getNextWalker()) {
                walker.detach();
            }
            this.m_lastUsedWalker = null;
            super.detach();
        }
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        this.m_predicateIndex = -1;
        for (AxesWalker walker = this.m_firstWalker; walker != null; walker = walker.getNextWalker()) {
            walker.fixupVariables(vars, globalsSize);
        }
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        if (visitor.visitLocationPath(owner, this) && this.m_firstWalker != null) {
            this.m_firstWalker.callVisitors(this, visitor);
        }
    }

    public Expression getExpression() {
        return this.m_firstWalker;
    }

    public void setExpression(Expression exp) {
        exp.exprSetParent(this);
        this.m_firstWalker = (AxesWalker) exp;
    }

    public boolean deepEquals(Expression expr) {
        if (!super.deepEquals(expr)) {
            return false;
        }
        AxesWalker walker1 = this.m_firstWalker;
        AxesWalker walker2 = ((WalkingIterator) expr).m_firstWalker;
        while (walker1 != null && walker2 != null) {
            if (!walker1.deepEquals(walker2)) {
                return false;
            }
            walker1 = walker1.getNextWalker();
            walker2 = walker2.getNextWalker();
        }
        if (walker1 == null && walker2 == null) {
            return true;
        }
        return false;
    }
}

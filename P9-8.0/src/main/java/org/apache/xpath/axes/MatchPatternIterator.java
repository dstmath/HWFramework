package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;
import org.apache.xpath.patterns.NodeTest;
import org.apache.xpath.patterns.StepPattern;

public class MatchPatternIterator extends LocPathIterator {
    private static final boolean DEBUG = false;
    static final long serialVersionUID = -5201153767396296474L;
    protected StepPattern m_pattern;
    protected int m_superAxis = -1;
    protected DTMAxisTraverser m_traverser;

    MatchPatternIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis, false);
        this.m_pattern = WalkerFactory.loadSteps(this, compiler, OpMap.getFirstChildPos(opPos), 0);
        boolean fromRoot = false;
        boolean walkBack = false;
        boolean walkDescendants = false;
        boolean walkAttributes = false;
        if ((671088640 & analysis) != 0) {
            fromRoot = true;
        }
        if ((98066432 & analysis) != 0) {
            walkBack = true;
        }
        if ((458752 & analysis) != 0) {
            walkDescendants = true;
        }
        if ((2129920 & analysis) != 0) {
            walkAttributes = true;
        }
        if (fromRoot || walkBack) {
            if (walkAttributes) {
                this.m_superAxis = 16;
            } else {
                this.m_superAxis = 17;
            }
        } else if (!walkDescendants) {
            this.m_superAxis = 16;
        } else if (walkAttributes) {
            this.m_superAxis = 14;
        } else {
            this.m_superAxis = 5;
        }
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        this.m_traverser = this.m_cdtm.getAxisTraverser(this.m_superAxis);
    }

    public void detach() {
        if (this.m_allowDetach) {
            this.m_traverser = null;
            super.detach();
        }
    }

    protected int getNextNode() {
        int first;
        if (-1 == this.m_lastFetched) {
            first = this.m_traverser.first(this.m_context);
        } else {
            first = this.m_traverser.next(this.m_context, this.m_lastFetched);
        }
        this.m_lastFetched = first;
        return this.m_lastFetched;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x002a A:{Catch:{ all -> 0x0046 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int nextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        VariableStack vars;
        int savedStart;
        int next;
        if (-1 != this.m_stackFrame) {
            vars = this.m_execContext.getVarStack();
            savedStart = vars.getStackFrame();
            vars.setStackFrame(this.m_stackFrame);
        } else {
            vars = null;
            savedStart = 0;
        }
        while (true) {
            try {
                next = getNextNode();
                if (-1 == next || (short) 1 == acceptNode(next, this.m_execContext) || next == -1) {
                    if (-1 == next) {
                        incrementCurrentPos();
                        return next;
                    }
                    this.m_foundLast = true;
                    if (-1 != this.m_stackFrame) {
                        vars.setStackFrame(savedStart);
                    }
                    return -1;
                }
            } finally {
                if (-1 != this.m_stackFrame) {
                    vars.setStackFrame(savedStart);
                }
            }
        }
        if (-1 == next) {
        }
    }

    public short acceptNode(int n, XPathContext xctxt) {
        try {
            short s;
            xctxt.pushCurrentNode(n);
            xctxt.pushIteratorRoot(this.m_context);
            if (this.m_pattern.execute(xctxt) == NodeTest.SCORE_NONE) {
                s = (short) 3;
            } else {
                s = (short) 1;
            }
            xctxt.popCurrentNode();
            xctxt.popIteratorRoot();
            return s;
        } catch (TransformerException se) {
            throw new RuntimeException(se.getMessage());
        } catch (Throwable th) {
            xctxt.popCurrentNode();
            xctxt.popIteratorRoot();
        }
    }
}

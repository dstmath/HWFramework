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
        boolean walkBack = false;
        boolean walkDescendants = false;
        boolean walkAttributes = false;
        boolean fromRoot = (671088640 & analysis) != 0;
        walkBack = (98066432 & analysis) != 0 ? true : walkBack;
        walkDescendants = (458752 & analysis) != 0 ? true : walkDescendants;
        walkAttributes = (2129920 & analysis) != 0 ? true : walkAttributes;
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

    /* access modifiers changed from: protected */
    public int getNextNode() {
        int i;
        if (-1 == this.m_lastFetched) {
            i = this.m_traverser.first(this.m_context);
        } else {
            i = this.m_traverser.next(this.m_context, this.m_lastFetched);
        }
        this.m_lastFetched = i;
        return this.m_lastFetched;
    }

    public int nextNode() {
        int savedStart;
        VariableStack vars;
        int next;
        if (this.m_foundLast) {
            return -1;
        }
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
                if (-1 != next) {
                    if (1 != acceptNode(next, this.m_execContext)) {
                        if (next == -1) {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } finally {
                if (-1 != this.m_stackFrame) {
                    vars.setStackFrame(savedStart);
                }
            }
        }
        if (-1 != next) {
            incrementCurrentPos();
            return next;
        }
        this.m_foundLast = true;
        if (-1 != this.m_stackFrame) {
            vars.setStackFrame(savedStart);
        }
        return -1;
    }

    public short acceptNode(int n, XPathContext xctxt) {
        short s;
        try {
            xctxt.pushCurrentNode(n);
            xctxt.pushIteratorRoot(this.m_context);
            if (this.m_pattern.execute(xctxt) == NodeTest.SCORE_NONE) {
                s = 3;
            } else {
                s = 1;
            }
            xctxt.popCurrentNode();
            xctxt.popIteratorRoot();
            return s;
        } catch (TransformerException se) {
            throw new RuntimeException(se.getMessage());
        } catch (Throwable th) {
            xctxt.popCurrentNode();
            xctxt.popIteratorRoot();
            throw th;
        }
    }
}

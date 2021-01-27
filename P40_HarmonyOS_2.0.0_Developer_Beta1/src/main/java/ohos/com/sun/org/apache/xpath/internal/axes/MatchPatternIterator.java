package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xpath.internal.VariableStack;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.OpMap;
import ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest;
import ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern;
import ohos.javax.xml.transform.TransformerException;

public class MatchPatternIterator extends LocPathIterator {
    private static final boolean DEBUG = false;
    static final long serialVersionUID = -5201153767396296474L;
    protected StepPattern m_pattern;
    protected int m_superAxis = -1;
    protected DTMAxisTraverser m_traverser;

    MatchPatternIterator(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2, false);
        this.m_pattern = WalkerFactory.loadSteps(this, compiler, OpMap.getFirstChildPos(i), 0);
        boolean z = true;
        boolean z2 = (671088640 & i2) != 0;
        boolean z3 = (98066432 & i2) != 0;
        boolean z4 = (458752 & i2) != 0;
        z = (i2 & 2129920) == 0 ? false : z;
        if (z2 || z3) {
            if (z) {
                this.m_superAxis = 16;
            } else {
                this.m_superAxis = 17;
            }
        } else if (!z4) {
            this.m_superAxis = 16;
        } else if (z) {
            this.m_superAxis = 14;
        } else {
            this.m_superAxis = 5;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        this.m_traverser = this.m_cdtm.getAxisTraverser(this.m_superAxis);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
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

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        int i;
        VariableStack variableStack;
        int nextNode;
        if (this.m_foundLast) {
            return -1;
        }
        if (-1 != this.m_stackFrame) {
            variableStack = this.m_execContext.getVarStack();
            i = variableStack.getStackFrame();
            variableStack.setStackFrame(this.m_stackFrame);
        } else {
            variableStack = null;
            i = 0;
        }
        while (true) {
            try {
                nextNode = getNextNode();
                if (-1 != nextNode) {
                    if (1 != acceptNode(nextNode, this.m_execContext)) {
                        if (nextNode == -1) {
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
                    variableStack.setStackFrame(i);
                }
            }
        }
        if (-1 != nextNode) {
            incrementCurrentPos();
            return nextNode;
        }
        this.m_foundLast = true;
        if (-1 != this.m_stackFrame) {
            variableStack.setStackFrame(i);
        }
        return -1;
    }

    public short acceptNode(int i, XPathContext xPathContext) {
        try {
            xPathContext.pushCurrentNode(i);
            xPathContext.pushIteratorRoot(this.m_context);
            short s = this.m_pattern.execute(xPathContext) == NodeTest.SCORE_NONE ? (short) 3 : 1;
            xPathContext.popCurrentNode();
            xPathContext.popIteratorRoot();
            return s;
        } catch (TransformerException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Throwable th) {
            xPathContext.popCurrentNode();
            xPathContext.popIteratorRoot();
            throw th;
        }
    }
}

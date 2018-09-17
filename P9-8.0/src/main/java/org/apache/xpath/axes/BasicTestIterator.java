package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.VariableStack;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;

public abstract class BasicTestIterator extends LocPathIterator {
    static final long serialVersionUID = 3505378079378096623L;

    protected abstract int getNextNode();

    protected BasicTestIterator() {
    }

    protected BasicTestIterator(PrefixResolver nscontext) {
        super(nscontext);
    }

    protected BasicTestIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis, false);
        int firstStepPos = OpMap.getFirstChildPos(opPos);
        int whatToShow = compiler.getWhatToShow(firstStepPos);
        if ((whatToShow & 4163) == 0 || whatToShow == -1) {
            initNodeTest(whatToShow);
        } else {
            initNodeTest(whatToShow, compiler.getStepNS(firstStepPos), compiler.getStepLocalName(firstStepPos));
        }
        initPredicateInfo(compiler, firstStepPos);
    }

    protected BasicTestIterator(Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers) throws TransformerException {
        super(compiler, opPos, analysis, shouldLoadWalkers);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0031 A:{Catch:{ all -> 0x0050 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int nextNode() {
        if (this.m_foundLast) {
            this.m_lastFetched = -1;
            return -1;
        }
        VariableStack vars;
        int savedStart;
        int next;
        if (-1 == this.m_lastFetched) {
            resetProximityPositions();
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
                if (-1 == next || (short) 1 == acceptNode(next) || next == -1) {
                    if (-1 == next) {
                        this.m_pos++;
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

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        ChildTestIterator clone = (ChildTestIterator) super.cloneWithReset();
        clone.resetProximityPositions();
        return clone;
    }
}

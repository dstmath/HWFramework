package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.VariableStack;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;

public abstract class BasicTestIterator extends LocPathIterator {
    static final long serialVersionUID = 3505378079378096623L;

    /* access modifiers changed from: protected */
    public abstract int getNextNode();

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

    public int nextNode() {
        int savedStart;
        VariableStack vars;
        int next;
        if (this.m_foundLast) {
            this.m_lastFetched = -1;
            return -1;
        }
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
                if (-1 != next) {
                    if (1 != acceptNode(next)) {
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
            this.m_pos++;
            return next;
        }
        this.m_foundLast = true;
        if (-1 != this.m_stackFrame) {
            vars.setStackFrame(savedStart);
        }
        return -1;
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        ChildTestIterator clone = (ChildTestIterator) super.cloneWithReset();
        clone.resetProximityPositions();
        return clone;
    }
}

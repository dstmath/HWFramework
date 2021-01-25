package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xpath.internal.VariableStack;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.OpMap;
import ohos.javax.xml.transform.TransformerException;

public abstract class BasicTestIterator extends LocPathIterator {
    static final long serialVersionUID = 3505378079378096623L;

    /* access modifiers changed from: protected */
    public abstract int getNextNode();

    protected BasicTestIterator() {
    }

    protected BasicTestIterator(PrefixResolver prefixResolver) {
        super(prefixResolver);
    }

    protected BasicTestIterator(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2, false);
        int firstChildPos = OpMap.getFirstChildPos(i);
        int whatToShow = compiler.getWhatToShow(firstChildPos);
        if ((whatToShow & 4163) == 0 || whatToShow == -1) {
            initNodeTest(whatToShow);
        } else {
            initNodeTest(whatToShow, compiler.getStepNS(firstChildPos), compiler.getStepLocalName(firstChildPos));
        }
        initPredicateInfo(compiler, firstChildPos);
    }

    protected BasicTestIterator(Compiler compiler, int i, int i2, boolean z) throws TransformerException {
        super(compiler, i, i2, z);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        int i;
        VariableStack variableStack;
        int nextNode;
        if (this.m_foundLast) {
            this.m_lastFetched = -1;
            return -1;
        }
        if (-1 == this.m_lastFetched) {
            resetProximityPositions();
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
                    if (1 != acceptNode(nextNode)) {
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
            this.m_pos++;
            return nextNode;
        }
        this.m_foundLast = true;
        if (-1 != this.m_stackFrame) {
            variableStack.setStackFrame(i);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        ChildTestIterator childTestIterator = (ChildTestIterator) super.cloneWithReset();
        childTestIterator.resetProximityPositions();
        return childTestIterator;
    }
}

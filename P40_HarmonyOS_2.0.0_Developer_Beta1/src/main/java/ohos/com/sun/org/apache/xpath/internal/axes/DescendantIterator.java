package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.VariableStack;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.OpMap;
import ohos.javax.xml.transform.TransformerException;

public class DescendantIterator extends LocPathIterator {
    static final long serialVersionUID = -1190338607743976938L;
    protected int m_axis;
    protected int m_extendedTypeID;
    protected transient DTMAxisTraverser m_traverser;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004f  */
    DescendantIterator(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2, false);
        int nextStepPos;
        int whatToShow;
        boolean z = false;
        int firstChildPos = OpMap.getFirstChildPos(i);
        int op = compiler.getOp(firstChildPos);
        boolean z2 = true;
        boolean z3 = 42 == op;
        if (48 == op) {
            z3 = true;
        } else if (50 == op) {
            if (compiler.getOp(compiler.getNextStepPos(firstChildPos)) == 42) {
                z3 = true;
            }
            while (true) {
                nextStepPos = compiler.getNextStepPos(firstChildPos);
                if (nextStepPos <= 0 || -1 == compiler.getOp(nextStepPos)) {
                    break;
                }
                firstChildPos = nextStepPos;
            }
            z = (i2 & 65536) == 0 ? z3 : z;
            if (!z2) {
                if (z) {
                    this.m_axis = 18;
                } else {
                    this.m_axis = 17;
                }
            } else if (z) {
                this.m_axis = 5;
            } else {
                this.m_axis = 4;
            }
            whatToShow = compiler.getWhatToShow(firstChildPos);
            if ((whatToShow & 67) != 0 || whatToShow == -1) {
                initNodeTest(whatToShow);
            } else {
                initNodeTest(whatToShow, compiler.getStepNS(firstChildPos), compiler.getStepLocalName(firstChildPos));
            }
            initPredicateInfo(compiler, firstChildPos);
        }
        z2 = false;
        while (true) {
            nextStepPos = compiler.getNextStepPos(firstChildPos);
            if (nextStepPos <= 0) {
                break;
            }
            break;
            firstChildPos = nextStepPos;
        }
        if ((i2 & 65536) == 0) {
        }
        if (!z2) {
        }
        whatToShow = compiler.getWhatToShow(firstChildPos);
        if ((whatToShow & 67) != 0) {
        }
        initNodeTest(whatToShow);
        initPredicateInfo(compiler, firstChildPos);
    }

    public DescendantIterator() {
        super(null);
        this.m_axis = 18;
        initNodeTest(-1);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        DescendantIterator descendantIterator = (DescendantIterator) super.cloneWithReset();
        descendantIterator.m_traverser = this.m_traverser;
        descendantIterator.resetProximityPositions();
        return descendantIterator;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        int i;
        VariableStack variableStack;
        int i2;
        if (this.m_foundLast) {
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
                if (this.m_extendedTypeID == 0) {
                    if (-1 == this.m_lastFetched) {
                        i2 = this.m_traverser.first(this.m_context);
                    } else {
                        i2 = this.m_traverser.next(this.m_context, this.m_lastFetched);
                    }
                    this.m_lastFetched = i2;
                } else {
                    if (-1 == this.m_lastFetched) {
                        i2 = this.m_traverser.first(this.m_context, this.m_extendedTypeID);
                    } else {
                        i2 = this.m_traverser.next(this.m_context, this.m_lastFetched, this.m_extendedTypeID);
                    }
                    this.m_lastFetched = i2;
                }
                if (-1 != i2) {
                    if (1 != acceptNode(i2)) {
                        if (i2 == -1) {
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
        if (-1 != i2) {
            this.m_pos++;
            return i2;
        }
        this.m_foundLast = true;
        if (-1 != this.m_stackFrame) {
            variableStack.setStackFrame(i);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        this.m_traverser = this.m_cdtm.getAxisTraverser(this.m_axis);
        String localName = getLocalName();
        String namespace = getNamespace();
        int i2 = this.m_whatToShow;
        if (-1 == i2 || "*".equals(localName) || "*".equals(namespace)) {
            this.m_extendedTypeID = 0;
            return;
        }
        this.m_extendedTypeID = this.m_cdtm.getExpandedTypeID(namespace, localName, getNodeTypeTest(i2));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xpath.internal.Expression
    public int asNode(XPathContext xPathContext) throws TransformerException {
        if (getPredicateCount() > 0) {
            return super.asNode(xPathContext);
        }
        int currentNode = xPathContext.getCurrentNode();
        DTM dtm = xPathContext.getDTM(currentNode);
        DTMAxisTraverser axisTraverser = dtm.getAxisTraverser(this.m_axis);
        String localName = getLocalName();
        String namespace = getNamespace();
        int i = this.m_whatToShow;
        if (-1 == i || localName == "*" || namespace == "*") {
            return axisTraverser.first(currentNode);
        }
        return axisTraverser.first(currentNode, dtm.getExpandedTypeID(namespace, localName, getNodeTypeTest(i)));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        if (this.m_allowDetach) {
            this.m_traverser = null;
            this.m_extendedTypeID = 0;
            super.detach();
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        return this.m_axis;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (super.deepEquals(expression) && this.m_axis == ((DescendantIterator) expression).m_axis) {
            return true;
        }
        return false;
    }
}

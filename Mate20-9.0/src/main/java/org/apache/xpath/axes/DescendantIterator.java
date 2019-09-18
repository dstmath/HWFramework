package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.Expression;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;

public class DescendantIterator extends LocPathIterator {
    static final long serialVersionUID = -1190338607743976938L;
    protected int m_axis;
    protected int m_extendedTypeID;
    protected transient DTMAxisTraverser m_traverser;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    DescendantIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis, false);
        int firstStepPos;
        boolean orSelf = false;
        int nextStepPos = OpMap.getFirstChildPos(opPos);
        int stepType = compiler.getOp(nextStepPos);
        orSelf = 42 == stepType ? true : orSelf;
        boolean fromRoot = false;
        if (48 == stepType) {
            orSelf = true;
        } else if (50 == stepType) {
            fromRoot = true;
            if (compiler.getOp(compiler.getNextStepPos(nextStepPos)) == 42) {
                orSelf = true;
            }
        }
        while (true) {
            firstStepPos = nextStepPos;
            nextStepPos = compiler.getNextStepPos(nextStepPos);
            if (nextStepPos <= 0 || -1 == compiler.getOp(nextStepPos)) {
            }
        }
        orSelf = (65536 & analysis) != 0 ? false : orSelf;
        if (fromRoot) {
            if (orSelf) {
                this.m_axis = 18;
            } else {
                this.m_axis = 17;
            }
        } else if (orSelf) {
            this.m_axis = 5;
        } else {
            this.m_axis = 4;
        }
        int whatToShow = compiler.getWhatToShow(firstStepPos);
        if ((whatToShow & 67) == 0 || whatToShow == -1) {
            initNodeTest(whatToShow);
        } else {
            initNodeTest(whatToShow, compiler.getStepNS(firstStepPos), compiler.getStepLocalName(firstStepPos));
        }
        initPredicateInfo(compiler, firstStepPos);
    }

    public DescendantIterator() {
        super(null);
        this.m_axis = 18;
        initNodeTest(-1);
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        DescendantIterator clone = (DescendantIterator) super.cloneWithReset();
        clone.m_traverser = this.m_traverser;
        clone.resetProximityPositions();
        return clone;
    }

    public int nextNode() {
        int savedStart;
        VariableStack vars;
        int next;
        int next2;
        if (this.m_foundLast) {
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
                if (this.m_extendedTypeID == 0) {
                    if (-1 == this.m_lastFetched) {
                        next = this.m_traverser.first(this.m_context);
                    } else {
                        next = this.m_traverser.next(this.m_context, this.m_lastFetched);
                    }
                    this.m_lastFetched = next;
                } else {
                    if (-1 == this.m_lastFetched) {
                        next2 = this.m_traverser.first(this.m_context, this.m_extendedTypeID);
                    } else {
                        next2 = this.m_traverser.next(this.m_context, this.m_lastFetched, this.m_extendedTypeID);
                    }
                    this.m_lastFetched = next;
                }
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

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        this.m_traverser = this.m_cdtm.getAxisTraverser(this.m_axis);
        String localName = getLocalName();
        String namespace = getNamespace();
        int what = this.m_whatToShow;
        if (-1 == what || "*".equals(localName) || "*".equals(namespace)) {
            this.m_extendedTypeID = 0;
            return;
        }
        this.m_extendedTypeID = this.m_cdtm.getExpandedTypeID(namespace, localName, getNodeTypeTest(what));
    }

    public int asNode(XPathContext xctxt) throws TransformerException {
        if (getPredicateCount() > 0) {
            return super.asNode(xctxt);
        }
        int current = xctxt.getCurrentNode();
        DTM dtm = xctxt.getDTM(current);
        DTMAxisTraverser traverser = dtm.getAxisTraverser(this.m_axis);
        String localName = getLocalName();
        String namespace = getNamespace();
        int what = this.m_whatToShow;
        if (-1 == what || localName == "*" || namespace == "*") {
            return traverser.first(current);
        }
        return traverser.first(current, dtm.getExpandedTypeID(namespace, localName, getNodeTypeTest(what)));
    }

    public void detach() {
        if (this.m_allowDetach) {
            this.m_traverser = null;
            this.m_extendedTypeID = 0;
            super.detach();
        }
    }

    public int getAxis() {
        return this.m_axis;
    }

    public boolean deepEquals(Expression expr) {
        if (super.deepEquals(expr) && this.m_axis == ((DescendantIterator) expr).m_axis) {
            return true;
        }
        return false;
    }
}

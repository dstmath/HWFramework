package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.Expression;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;

public class OneStepIterator extends ChildTestIterator {
    static final long serialVersionUID = 4623710779664998283L;
    protected int m_axis = -1;
    protected DTMAxisIterator m_iterator;

    OneStepIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis);
        this.m_axis = WalkerFactory.getAxisFromStep(compiler, OpMap.getFirstChildPos(opPos));
    }

    public OneStepIterator(DTMAxisIterator iterator, int axis) throws TransformerException {
        super(null);
        this.m_iterator = iterator;
        this.m_axis = axis;
        initNodeTest(-1);
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        if (this.m_axis > -1) {
            this.m_iterator = this.m_cdtm.getAxisIterator(this.m_axis);
        }
        this.m_iterator.setStartNode(this.m_context);
    }

    public void detach() {
        if (this.m_allowDetach) {
            if (this.m_axis > -1) {
                this.m_iterator = null;
            }
            super.detach();
        }
    }

    protected int getNextNode() {
        int next = this.m_iterator.next();
        this.m_lastFetched = next;
        return next;
    }

    public Object clone() throws CloneNotSupportedException {
        OneStepIterator clone = (OneStepIterator) super.clone();
        if (this.m_iterator != null) {
            clone.m_iterator = this.m_iterator.cloneIterator();
        }
        return clone;
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        OneStepIterator clone = (OneStepIterator) super.cloneWithReset();
        clone.m_iterator = this.m_iterator;
        return clone;
    }

    public boolean isReverseAxes() {
        return this.m_iterator.isReverse();
    }

    protected int getProximityPosition(int predicateIndex) {
        if (!isReverseAxes()) {
            return super.getProximityPosition(predicateIndex);
        }
        if (predicateIndex < 0) {
            return -1;
        }
        if (this.m_proximityPositions[predicateIndex] <= 0) {
            XPathContext xctxt = getXPathContext();
            try {
                OneStepIterator clone = (OneStepIterator) clone();
                int root = getRoot();
                xctxt.pushCurrentNode(root);
                clone.setRoot(root, xctxt);
                clone.m_predCount = predicateIndex;
                int count = 1;
                while (-1 != clone.nextNode()) {
                    count++;
                }
                int[] iArr = this.m_proximityPositions;
                iArr[predicateIndex] = iArr[predicateIndex] + count;
            } catch (CloneNotSupportedException e) {
            } finally {
                xctxt.popCurrentNode();
            }
        }
        return this.m_proximityPositions[predicateIndex];
    }

    public int getLength() {
        if (!isReverseAxes()) {
            return super.getLength();
        }
        boolean isPredicateTest = this == this.m_execContext.getSubContextList();
        int predCount = getPredicateCount();
        if (-1 != this.m_length && isPredicateTest && this.m_predicateIndex < 1) {
            return this.m_length;
        }
        int count = 0;
        XPathContext xctxt = getXPathContext();
        try {
            OneStepIterator clone = (OneStepIterator) cloneWithReset();
            int root = getRoot();
            xctxt.pushCurrentNode(root);
            clone.setRoot(root, xctxt);
            clone.m_predCount = this.m_predicateIndex;
            while (-1 != clone.nextNode()) {
                count++;
            }
        } catch (CloneNotSupportedException e) {
        } finally {
            xctxt.popCurrentNode();
        }
        if (isPredicateTest && this.m_predicateIndex < 1) {
            this.m_length = count;
        }
        return count;
    }

    protected void countProximityPosition(int i) {
        if (!isReverseAxes()) {
            super.countProximityPosition(i);
        } else if (i < this.m_proximityPositions.length) {
            int[] iArr = this.m_proximityPositions;
            iArr[i] = iArr[i] - 1;
        }
    }

    public void reset() {
        super.reset();
        if (this.m_iterator != null) {
            this.m_iterator.reset();
        }
    }

    public int getAxis() {
        return this.m_axis;
    }

    public boolean deepEquals(Expression expr) {
        if (super.deepEquals(expr) && this.m_axis == ((OneStepIterator) expr).m_axis) {
            return true;
        }
        return false;
    }
}

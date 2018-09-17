package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xpath.Expression;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.OpMap;

public class OneStepIteratorForward extends ChildTestIterator {
    static final long serialVersionUID = -1576936606178190566L;
    protected int m_axis = -1;

    OneStepIteratorForward(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis);
        this.m_axis = WalkerFactory.getAxisFromStep(compiler, OpMap.getFirstChildPos(opPos));
    }

    public OneStepIteratorForward(int axis) {
        super(null);
        this.m_axis = axis;
        initNodeTest(-1);
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        this.m_traverser = this.m_cdtm.getAxisTraverser(this.m_axis);
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

    public int getAxis() {
        return this.m_axis;
    }

    public boolean deepEquals(Expression expr) {
        if (super.deepEquals(expr) && this.m_axis == ((OneStepIteratorForward) expr).m_axis) {
            return true;
        }
        return false;
    }
}

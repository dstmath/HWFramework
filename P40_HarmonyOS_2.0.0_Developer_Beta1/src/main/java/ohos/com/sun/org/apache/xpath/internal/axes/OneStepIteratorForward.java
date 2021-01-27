package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.OpMap;
import ohos.javax.xml.transform.TransformerException;

public class OneStepIteratorForward extends ChildTestIterator {
    static final long serialVersionUID = -1576936606178190566L;
    protected int m_axis = -1;

    OneStepIteratorForward(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2);
        this.m_axis = WalkerFactory.getAxisFromStep(compiler, OpMap.getFirstChildPos(i));
    }

    public OneStepIteratorForward(int i) {
        super(null);
        this.m_axis = i;
        initNodeTest(-1);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        this.m_traverser = this.m_cdtm.getAxisTraverser(this.m_axis);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.BasicTestIterator
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

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        return this.m_axis;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (super.deepEquals(expression) && this.m_axis == ((OneStepIteratorForward) expression).m_axis) {
            return true;
        }
        return false;
    }
}

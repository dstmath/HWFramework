package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.operations.Variable;
import ohos.javax.xml.transform.TransformerException;

public class FilterExprWalker extends AxesWalker {
    static final long serialVersionUID = 5457182471424488375L;
    private boolean m_canDetachNodeset = true;
    private Expression m_expr;
    private transient XNodeSet m_exprObj;
    private boolean m_mustHardReset = false;

    public FilterExprWalker(WalkingIterator walkingIterator) {
        super(walkingIterator, 20);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public void init(Compiler compiler, int i, int i2) throws TransformerException {
        super.init(compiler, i, i2);
        switch (i2) {
            case 22:
            case 23:
                break;
            default:
                this.m_expr = compiler.compileExpression(i + 2);
                this.m_expr.exprSetParent(this);
                return;
            case 24:
            case 25:
                this.m_mustHardReset = true;
                break;
        }
        this.m_expr = compiler.compileExpression(i);
        this.m_expr.exprSetParent(this);
        if (this.m_expr instanceof Variable) {
            this.m_canDetachNodeset = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public void detach() {
        super.detach();
        if (this.m_canDetachNodeset) {
            this.m_exprObj.detach();
        }
        this.m_exprObj = null;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public void setRoot(int i) {
        super.setRoot(i);
        this.m_exprObj = FilterExprIteratorSimple.executeFilterExpr(i, this.m_lpi.getXPathContext(), this.m_lpi.getPrefixResolver(), this.m_lpi.getIsTopLevel(), this.m_lpi.m_stackFrame, this.m_expr);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker, ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        FilterExprWalker filterExprWalker = (FilterExprWalker) super.clone();
        XNodeSet xNodeSet = this.m_exprObj;
        if (xNodeSet != null) {
            filterExprWalker.m_exprObj = (XNodeSet) xNodeSet.clone();
        }
        return filterExprWalker;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public short acceptNode(int i) {
        try {
            if (getPredicateCount() <= 0) {
                return 1;
            }
            countProximityPosition(0);
            return !executePredicates(i, this.m_lpi.getXPathContext()) ? (short) 3 : 1;
        } catch (TransformerException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public int getNextNode() {
        XNodeSet xNodeSet = this.m_exprObj;
        if (xNodeSet != null) {
            return xNodeSet.nextNode();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker, ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public int getLastPos(XPathContext xPathContext) {
        return this.m_exprObj.getLength();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        this.m_expr.fixupVariables(vector, i);
    }

    public Expression getInnerExpression() {
        return this.m_expr;
    }

    public void setInnerExpression(Expression expression) {
        expression.exprSetParent(this);
        this.m_expr = expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker, ohos.com.sun.org.apache.xpath.internal.axes.PathComponent
    public int getAnalysisBits() {
        Expression expression = this.m_expr;
        if (expression == null || !(expression instanceof PathComponent)) {
            return 67108864;
        }
        return ((PathComponent) expression).getAnalysisBits();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public boolean isDocOrdered() {
        return this.m_exprObj.isDocOrdered();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker
    public int getAxis() {
        return this.m_exprObj.getAxis();
    }

    class filterExprOwner implements ExpressionOwner {
        filterExprOwner() {
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return FilterExprWalker.this.m_expr;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(FilterExprWalker.this);
            FilterExprWalker.this.m_expr = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public void callPredicateVisitors(XPathVisitor xPathVisitor) {
        this.m_expr.callVisitors(new filterExprOwner(), xPathVisitor);
        super.callPredicateVisitors(xPathVisitor);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.AxesWalker, ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (super.deepEquals(expression) && this.m_expr.deepEquals(((FilterExprWalker) expression).m_expr)) {
            return true;
        }
        return false;
    }
}

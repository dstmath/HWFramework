package ohos.com.sun.org.apache.xpath.internal.patterns;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FunctionPattern extends StepPattern {
    static final long serialVersionUID = -5426793413091209944L;
    Expression m_functionExpr;

    public FunctionPattern(Expression expression, int i, int i2) {
        super(0, null, null, i, i2);
        this.m_functionExpr = expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest
    public final void calcScore() {
        this.m_score = SCORE_OTHER;
        if (this.m_targetString == null) {
            calcTargetString();
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        this.m_functionExpr.fixupVariables(vector, i);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, int i) throws TransformerException {
        DTMIterator asIterator = this.m_functionExpr.asIterator(xPathContext, i);
        XNumber xNumber = SCORE_NONE;
        if (asIterator != null) {
            do {
                int nextNode = asIterator.nextNode();
                if (-1 == nextNode) {
                    break;
                }
                xNumber = nextNode == i ? SCORE_OTHER : SCORE_NONE;
            } while (xNumber != SCORE_OTHER);
        }
        asIterator.detach();
        return xNumber;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, int i, DTM dtm, int i2) throws TransformerException {
        DTMIterator asIterator = this.m_functionExpr.asIterator(xPathContext, i);
        XNumber xNumber = SCORE_NONE;
        if (asIterator != null) {
            do {
                int nextNode = asIterator.nextNode();
                if (-1 == nextNode) {
                    break;
                }
                xNumber = nextNode == i ? SCORE_OTHER : SCORE_NONE;
            } while (xNumber != SCORE_OTHER);
            asIterator.detach();
        }
        return xNumber;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        int currentNode = xPathContext.getCurrentNode();
        DTMIterator asIterator = this.m_functionExpr.asIterator(xPathContext, currentNode);
        XNumber xNumber = SCORE_NONE;
        if (asIterator != null) {
            do {
                int nextNode = asIterator.nextNode();
                if (-1 == nextNode) {
                    break;
                }
                xNumber = nextNode == currentNode ? SCORE_OTHER : SCORE_NONE;
            } while (xNumber != SCORE_OTHER);
            asIterator.detach();
        }
        return xNumber;
    }

    class FunctionOwner implements ExpressionOwner {
        FunctionOwner() {
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return FunctionPattern.this.m_functionExpr;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(FunctionPattern.this);
            FunctionPattern.this.m_functionExpr = expression;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern
    public void callSubtreeVisitors(XPathVisitor xPathVisitor) {
        this.m_functionExpr.callVisitors(new FunctionOwner(), xPathVisitor);
        super.callSubtreeVisitors(xPathVisitor);
    }
}

package org.apache.xpath.patterns;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class FunctionPattern extends StepPattern {
    static final long serialVersionUID = -5426793413091209944L;
    Expression m_functionExpr;

    class FunctionOwner implements ExpressionOwner {
        FunctionOwner() {
        }

        public Expression getExpression() {
            return FunctionPattern.this.m_functionExpr;
        }

        public void setExpression(Expression exp) {
            exp.exprSetParent(FunctionPattern.this);
            FunctionPattern.this.m_functionExpr = exp;
        }
    }

    public FunctionPattern(Expression expr, int axis, int predaxis) {
        super(0, null, null, axis, predaxis);
        this.m_functionExpr = expr;
    }

    public final void calcScore() {
        this.m_score = SCORE_OTHER;
        if (this.m_targetString == null) {
            calcTargetString();
        }
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
        this.m_functionExpr.fixupVariables(vars, globalsSize);
    }

    public XObject execute(XPathContext xctxt, int context) throws TransformerException {
        DTMIterator nl = this.m_functionExpr.asIterator(xctxt, context);
        XNumber score = SCORE_NONE;
        if (nl != null) {
            int n;
            do {
                n = nl.nextNode();
                if (-1 == n) {
                    break;
                }
                score = n == context ? SCORE_OTHER : SCORE_NONE;
            } while (score != SCORE_OTHER);
            context = n;
        }
        nl.detach();
        return score;
    }

    public XObject execute(XPathContext xctxt, int context, DTM dtm, int expType) throws TransformerException {
        DTMIterator nl = this.m_functionExpr.asIterator(xctxt, context);
        XNumber score = SCORE_NONE;
        if (nl != null) {
            int n;
            do {
                n = nl.nextNode();
                if (-1 == n) {
                    break;
                }
                score = n == context ? SCORE_OTHER : SCORE_NONE;
            } while (score != SCORE_OTHER);
            context = n;
            nl.detach();
        }
        return score;
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        int context = xctxt.getCurrentNode();
        DTMIterator nl = this.m_functionExpr.asIterator(xctxt, context);
        XNumber score = SCORE_NONE;
        if (nl != null) {
            int n;
            do {
                n = nl.nextNode();
                if (-1 == n) {
                    break;
                }
                score = n == context ? SCORE_OTHER : SCORE_NONE;
            } while (score != SCORE_OTHER);
            context = n;
            nl.detach();
        }
        return score;
    }

    protected void callSubtreeVisitors(XPathVisitor visitor) {
        this.m_functionExpr.callVisitors(new FunctionOwner(), visitor);
        super.callSubtreeVisitors(visitor);
    }
}

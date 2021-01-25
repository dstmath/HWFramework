package ohos.com.sun.org.apache.xpath.internal.patterns;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class UnionPattern extends Expression {
    static final long serialVersionUID = -6670449967116905820L;
    private StepPattern[] m_patterns;

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        int i2 = 0;
        while (true) {
            StepPattern[] stepPatternArr = this.m_patterns;
            if (i2 < stepPatternArr.length) {
                stepPatternArr[i2].fixupVariables(vector, i);
                i2++;
            } else {
                return;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        StepPattern[] stepPatternArr = this.m_patterns;
        if (stepPatternArr != null) {
            int length = stepPatternArr.length;
            for (int i = 0; i < length; i++) {
                if (this.m_patterns[i].canTraverseOutsideSubtree()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setPatterns(StepPattern[] stepPatternArr) {
        this.m_patterns = stepPatternArr;
        if (stepPatternArr != null) {
            for (StepPattern stepPattern : stepPatternArr) {
                stepPattern.exprSetParent(this);
            }
        }
    }

    public StepPattern[] getPatterns() {
        return this.m_patterns;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        int length = this.m_patterns.length;
        XObject xObject = null;
        for (int i = 0; i < length; i++) {
            XObject execute = this.m_patterns[i].execute(xPathContext);
            if (execute != NodeTest.SCORE_NONE && (xObject == null || execute.num() > xObject.num())) {
                xObject = execute;
            }
        }
        return xObject == null ? NodeTest.SCORE_NONE : xObject;
    }

    class UnionPathPartOwner implements ExpressionOwner {
        int m_index;

        UnionPathPartOwner(int i) {
            this.m_index = i;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return UnionPattern.this.m_patterns[this.m_index];
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(UnionPattern.this);
            UnionPattern.this.m_patterns[this.m_index] = (StepPattern) expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        xPathVisitor.visitUnionPattern(expressionOwner, this);
        StepPattern[] stepPatternArr = this.m_patterns;
        if (stepPatternArr != null) {
            int length = stepPatternArr.length;
            for (int i = 0; i < length; i++) {
                this.m_patterns[i].callVisitors(new UnionPathPartOwner(i), xPathVisitor);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!isSameClass(expression)) {
            return false;
        }
        UnionPattern unionPattern = (UnionPattern) expression;
        StepPattern[] stepPatternArr = this.m_patterns;
        if (stepPatternArr != null) {
            int length = stepPatternArr.length;
            StepPattern[] stepPatternArr2 = unionPattern.m_patterns;
            if (stepPatternArr2 == null || stepPatternArr2.length != length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!this.m_patterns[i].deepEquals(unionPattern.m_patterns[i])) {
                    return false;
                }
            }
            return true;
        } else if (unionPattern.m_patterns != null) {
            return false;
        } else {
            return true;
        }
    }
}

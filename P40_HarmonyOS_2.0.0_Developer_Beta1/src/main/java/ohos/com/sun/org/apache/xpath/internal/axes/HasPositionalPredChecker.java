package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncLast;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncPosition;
import ohos.com.sun.org.apache.xpath.internal.functions.Function;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.operations.Div;
import ohos.com.sun.org.apache.xpath.internal.operations.Minus;
import ohos.com.sun.org.apache.xpath.internal.operations.Mod;
import ohos.com.sun.org.apache.xpath.internal.operations.Mult;
import ohos.com.sun.org.apache.xpath.internal.operations.Number;
import ohos.com.sun.org.apache.xpath.internal.operations.Plus;
import ohos.com.sun.org.apache.xpath.internal.operations.Quo;
import ohos.com.sun.org.apache.xpath.internal.operations.Variable;

public class HasPositionalPredChecker extends XPathVisitor {
    private boolean m_hasPositionalPred = false;
    private int m_predDepth = 0;

    public static boolean check(LocPathIterator locPathIterator) {
        HasPositionalPredChecker hasPositionalPredChecker = new HasPositionalPredChecker();
        locPathIterator.callVisitors(null, hasPositionalPredChecker);
        return hasPositionalPredChecker.m_hasPositionalPred;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitor
    public boolean visitFunction(ExpressionOwner expressionOwner, Function function) {
        if ((function instanceof FuncPosition) || (function instanceof FuncLast)) {
            this.m_hasPositionalPred = true;
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitor
    public boolean visitPredicate(ExpressionOwner expressionOwner, Expression expression) {
        this.m_predDepth++;
        if (this.m_predDepth == 1) {
            if ((expression instanceof Variable) || (expression instanceof XNumber) || (expression instanceof Div) || (expression instanceof Plus) || (expression instanceof Minus) || (expression instanceof Mod) || (expression instanceof Quo) || (expression instanceof Mult) || (expression instanceof Number) || (expression instanceof Function)) {
                this.m_hasPositionalPred = true;
            } else {
                expression.callVisitors(expressionOwner, this);
            }
        }
        this.m_predDepth--;
        return false;
    }
}

package ohos.com.sun.org.apache.xpath.internal;

import ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator;
import ohos.com.sun.org.apache.xpath.internal.axes.UnionPathIterator;
import ohos.com.sun.org.apache.xpath.internal.functions.Function;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.com.sun.org.apache.xpath.internal.operations.Operation;
import ohos.com.sun.org.apache.xpath.internal.operations.UnaryOperation;
import ohos.com.sun.org.apache.xpath.internal.operations.Variable;
import ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest;
import ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern;
import ohos.com.sun.org.apache.xpath.internal.patterns.UnionPattern;

public class XPathVisitor {
    public boolean visitBinaryOperation(ExpressionOwner expressionOwner, Operation operation) {
        return true;
    }

    public boolean visitFunction(ExpressionOwner expressionOwner, Function function) {
        return true;
    }

    public boolean visitLocationPath(ExpressionOwner expressionOwner, LocPathIterator locPathIterator) {
        return true;
    }

    public boolean visitMatchPattern(ExpressionOwner expressionOwner, StepPattern stepPattern) {
        return true;
    }

    public boolean visitNumberLiteral(ExpressionOwner expressionOwner, XNumber xNumber) {
        return true;
    }

    public boolean visitPredicate(ExpressionOwner expressionOwner, Expression expression) {
        return true;
    }

    public boolean visitStep(ExpressionOwner expressionOwner, NodeTest nodeTest) {
        return true;
    }

    public boolean visitStringLiteral(ExpressionOwner expressionOwner, XString xString) {
        return true;
    }

    public boolean visitUnaryOperation(ExpressionOwner expressionOwner, UnaryOperation unaryOperation) {
        return true;
    }

    public boolean visitUnionPath(ExpressionOwner expressionOwner, UnionPathIterator unionPathIterator) {
        return true;
    }

    public boolean visitUnionPattern(ExpressionOwner expressionOwner, UnionPattern unionPattern) {
        return true;
    }

    public boolean visitVariableRef(ExpressionOwner expressionOwner, Variable variable) {
        return true;
    }
}

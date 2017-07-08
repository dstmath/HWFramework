package org.apache.xpath;

import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.axes.UnionPathIterator;
import org.apache.xpath.functions.Function;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XString;
import org.apache.xpath.operations.Operation;
import org.apache.xpath.operations.UnaryOperation;
import org.apache.xpath.operations.Variable;
import org.apache.xpath.patterns.NodeTest;
import org.apache.xpath.patterns.StepPattern;
import org.apache.xpath.patterns.UnionPattern;

public class XPathVisitor {
    public boolean visitLocationPath(ExpressionOwner owner, LocPathIterator path) {
        return true;
    }

    public boolean visitUnionPath(ExpressionOwner owner, UnionPathIterator path) {
        return true;
    }

    public boolean visitStep(ExpressionOwner owner, NodeTest step) {
        return true;
    }

    public boolean visitPredicate(ExpressionOwner owner, Expression pred) {
        return true;
    }

    public boolean visitBinaryOperation(ExpressionOwner owner, Operation op) {
        return true;
    }

    public boolean visitUnaryOperation(ExpressionOwner owner, UnaryOperation op) {
        return true;
    }

    public boolean visitVariableRef(ExpressionOwner owner, Variable var) {
        return true;
    }

    public boolean visitFunction(ExpressionOwner owner, Function func) {
        return true;
    }

    public boolean visitMatchPattern(ExpressionOwner owner, StepPattern pattern) {
        return true;
    }

    public boolean visitUnionPattern(ExpressionOwner owner, UnionPattern pattern) {
        return true;
    }

    public boolean visitStringLiteral(ExpressionOwner owner, XString str) {
        return true;
    }

    public boolean visitNumberLiteral(ExpressionOwner owner, XNumber num) {
        return true;
    }
}

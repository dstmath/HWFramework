package org.apache.xpath;

public interface ExpressionOwner {
    Expression getExpression();

    void setExpression(Expression expression);
}

package org.apache.xpath;

import javax.xml.transform.SourceLocator;

public interface ExpressionNode extends SourceLocator {
    void exprAddChild(ExpressionNode expressionNode, int i);

    ExpressionNode exprGetChild(int i);

    int exprGetNumChildren();

    ExpressionNode exprGetParent();

    void exprSetParent(ExpressionNode expressionNode);
}

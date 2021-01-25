package ohos.com.sun.org.apache.xpath.internal;

import ohos.javax.xml.transform.SourceLocator;

public interface ExpressionNode extends SourceLocator {
    void exprAddChild(ExpressionNode expressionNode, int i);

    ExpressionNode exprGetChild(int i);

    int exprGetNumChildren();

    ExpressionNode exprGetParent();

    void exprSetParent(ExpressionNode expressionNode);
}

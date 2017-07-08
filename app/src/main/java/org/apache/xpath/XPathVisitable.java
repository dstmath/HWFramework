package org.apache.xpath;

public interface XPathVisitable {
    void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor);
}

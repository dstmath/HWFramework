package ohos.com.sun.org.apache.xpath.internal;

public interface XPathVisitable {
    void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor);
}

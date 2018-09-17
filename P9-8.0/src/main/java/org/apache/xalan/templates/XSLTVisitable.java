package org.apache.xalan.templates;

public interface XSLTVisitable {
    void callVisitors(XSLTVisitor xSLTVisitor);
}

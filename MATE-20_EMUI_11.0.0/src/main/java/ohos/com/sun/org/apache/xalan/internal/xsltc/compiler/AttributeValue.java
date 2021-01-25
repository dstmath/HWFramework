package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

/* access modifiers changed from: package-private */
public abstract class AttributeValue extends Expression {
    AttributeValue() {
    }

    public static final AttributeValue create(SyntaxTreeNode syntaxTreeNode, String str, Parser parser) {
        if (str.indexOf(123) != -1) {
            return new AttributeValueTemplate(str, parser, syntaxTreeNode);
        }
        if (str.indexOf(125) != -1) {
            return new AttributeValueTemplate(str, parser, syntaxTreeNode);
        }
        SimpleAttributeValue simpleAttributeValue = new SimpleAttributeValue(str);
        simpleAttributeValue.setParser(parser);
        simpleAttributeValue.setParent(syntaxTreeNode);
        return simpleAttributeValue;
    }
}

package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FunctionDef1Arg extends FunctionOneArg {
    static final long serialVersionUID = 2325189412814149264L;

    /* access modifiers changed from: protected */
    public int getArg0AsNode(XPathContext xPathContext) throws TransformerException {
        return this.m_arg0 == null ? xPathContext.getCurrentNode() : this.m_arg0.asNode(xPathContext);
    }

    public boolean Arg0IsNodesetExpr() {
        if (this.m_arg0 == null) {
            return true;
        }
        return this.m_arg0.isNodesetExpr();
    }

    /* access modifiers changed from: protected */
    public XMLString getArg0AsString(XPathContext xPathContext) throws TransformerException {
        if (this.m_arg0 != null) {
            return this.m_arg0.execute(xPathContext).xstr();
        }
        int currentNode = xPathContext.getCurrentNode();
        if (-1 == currentNode) {
            return XString.EMPTYSTRING;
        }
        return xPathContext.getDTM(currentNode).getStringValue(currentNode);
    }

    /* access modifiers changed from: protected */
    public double getArg0AsNumber(XPathContext xPathContext) throws TransformerException {
        if (this.m_arg0 != null) {
            return this.m_arg0.execute(xPathContext).num();
        }
        int currentNode = xPathContext.getCurrentNode();
        if (-1 == currentNode) {
            return XPath.MATCH_SCORE_QNAME;
        }
        return xPathContext.getDTM(currentNode).getStringValue(currentNode).toDouble();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
        if (i > 1) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("ER_ZERO_OR_ONE", null));
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        if (this.m_arg0 == null) {
            return false;
        }
        return super.canTraverseOutsideSubtree();
    }
}

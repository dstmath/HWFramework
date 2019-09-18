package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHErrorResources;

public class FunctionDef1Arg extends FunctionOneArg {
    static final long serialVersionUID = 2325189412814149264L;

    /* access modifiers changed from: protected */
    public int getArg0AsNode(XPathContext xctxt) throws TransformerException {
        return this.m_arg0 == null ? xctxt.getCurrentNode() : this.m_arg0.asNode(xctxt);
    }

    public boolean Arg0IsNodesetExpr() {
        if (this.m_arg0 == null) {
            return true;
        }
        return this.m_arg0.isNodesetExpr();
    }

    /* access modifiers changed from: protected */
    public XMLString getArg0AsString(XPathContext xctxt) throws TransformerException {
        if (this.m_arg0 != null) {
            return this.m_arg0.execute(xctxt).xstr();
        }
        int currentNode = xctxt.getCurrentNode();
        if (-1 == currentNode) {
            return XString.EMPTYSTRING;
        }
        return xctxt.getDTM(currentNode).getStringValue(currentNode);
    }

    /* access modifiers changed from: protected */
    public double getArg0AsNumber(XPathContext xctxt) throws TransformerException {
        if (this.m_arg0 != null) {
            return this.m_arg0.execute(xctxt).num();
        }
        int currentNode = xctxt.getCurrentNode();
        if (-1 == currentNode) {
            return XPath.MATCH_SCORE_QNAME;
        }
        return xctxt.getDTM(currentNode).getStringValue(currentNode).toDouble();
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
        if (argNum > 1) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ZERO_OR_ONE, null));
    }

    public boolean canTraverseOutsideSubtree() {
        if (this.m_arg0 == null) {
            return false;
        }
        return super.canTraverseOutsideSubtree();
    }
}

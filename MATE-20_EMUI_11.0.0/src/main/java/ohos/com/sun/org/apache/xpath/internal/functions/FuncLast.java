package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.axes.SubContextList;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncLast extends Function {
    static final long serialVersionUID = 9205812403085432943L;
    private boolean m_isTopLevel;

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void postCompileStep(Compiler compiler) {
        this.m_isTopLevel = compiler.getLocationPathDepth() == -1;
    }

    public int getCountOfContextNodeList(XPathContext xPathContext) throws TransformerException {
        SubContextList subContextList = this.m_isTopLevel ? null : xPathContext.getSubContextList();
        if (subContextList != null) {
            return subContextList.getLastPos(xPathContext);
        }
        DTMIterator contextNodeList = xPathContext.getContextNodeList();
        if (contextNodeList != null) {
            return contextNodeList.getLength();
        }
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return new XNumber((double) getCountOfContextNodeList(xPathContext));
    }
}

package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest;
import ohos.com.sun.org.apache.xpath.internal.axes.SubContextList;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern;
import ohos.javax.xml.transform.TransformerException;

public class FuncCurrent extends Function {
    static final long serialVersionUID = 5715316804877715008L;

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        int i;
        SubContextList currentNodeList = xPathContext.getCurrentNodeList();
        if (currentNodeList == null) {
            i = xPathContext.getContextNode();
        } else if (currentNodeList instanceof PredicatedNodeTest) {
            i = ((PredicatedNodeTest) currentNodeList).getLocPathIterator().getCurrentContextNode();
        } else if (!(currentNodeList instanceof StepPattern)) {
            i = -1;
        } else {
            throw new RuntimeException(XSLMessages.createMessage("ER_PROCESSOR_ERROR", null));
        }
        return new XNodeSet(i, xPathContext.getDTMManager());
    }
}

package ohos.com.sun.org.apache.xpath.internal.operations;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class VariableSafeAbsRef extends Variable {
    static final long serialVersionUID = -9174661990819967452L;

    @Override // ohos.com.sun.org.apache.xpath.internal.operations.Variable, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, boolean z) throws TransformerException {
        XNodeSet xNodeSet = (XNodeSet) super.execute(xPathContext, z);
        DTMManager dTMManager = xPathContext.getDTMManager();
        int contextNode = xPathContext.getContextNode();
        return dTMManager.getDTM(xNodeSet.getRoot()).getDocument() != dTMManager.getDTM(contextNode).getDocument() ? (XNodeSet) ((Expression) xNodeSet.getContainedIter()).asIterator(xPathContext, contextNode) : xNodeSet;
    }
}

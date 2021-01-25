package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.axes.SubContextList;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncPosition extends Function {
    static final long serialVersionUID = -9092846348197271582L;
    private boolean m_isTopLevel;

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void postCompileStep(Compiler compiler) {
        this.m_isTopLevel = compiler.getLocationPathDepth() == -1;
    }

    public int getPositionInContextNodeList(XPathContext xPathContext) {
        int nextNode;
        SubContextList subContextList = this.m_isTopLevel ? null : xPathContext.getSubContextList();
        if (subContextList != null) {
            return subContextList.getProximityPosition(xPathContext);
        }
        DTMIterator contextNodeList = xPathContext.getContextNodeList();
        if (contextNodeList == null) {
            return -1;
        }
        if (contextNodeList.getCurrentNode() == -1) {
            if (contextNodeList.getCurrentPos() == 0) {
                return 0;
            }
            try {
                contextNodeList = contextNodeList.cloneWithReset();
                int contextNode = xPathContext.getContextNode();
                do {
                    nextNode = contextNodeList.nextNode();
                    if (-1 == nextNode) {
                        break;
                    }
                } while (nextNode != contextNode);
            } catch (CloneNotSupportedException e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return contextNodeList.getCurrentPos();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return new XNumber((double) getPositionInContextNodeList(xPathContext));
    }
}

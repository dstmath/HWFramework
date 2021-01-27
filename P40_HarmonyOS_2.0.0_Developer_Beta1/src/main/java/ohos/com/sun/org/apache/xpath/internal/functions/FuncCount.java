package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncCount extends FunctionOneArg {
    static final long serialVersionUID = -7116225100474153751L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        DTMIterator asIterator = this.m_arg0.asIterator(xPathContext, xPathContext.getCurrentNode());
        int length = asIterator.getLength();
        asIterator.detach();
        return new XNumber((double) length);
    }
}

package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncBoolean extends FunctionOneArg {
    static final long serialVersionUID = 4328660760070034592L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return this.m_arg0.execute(xPathContext).bool() ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}

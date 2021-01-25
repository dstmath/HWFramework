package ohos.com.sun.org.apache.xpath.internal.functions;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncFalse extends Function {
    static final long serialVersionUID = 6150918062759769887L;

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return XBoolean.S_FALSE;
    }
}

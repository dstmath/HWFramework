package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.javax.xml.transform.TransformerException;

public class FuncConcat extends FunctionMultiArgs {
    static final long serialVersionUID = 1737228885202314413L;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.m_arg0.execute(xPathContext).str());
        stringBuffer.append(this.m_arg1.execute(xPathContext).str());
        if (this.m_arg2 != null) {
            stringBuffer.append(this.m_arg2.execute(xPathContext).str());
        }
        if (this.m_args != null) {
            for (int i = 0; i < this.m_args.length; i++) {
                stringBuffer.append(this.m_args[i].execute(xPathContext).str());
            }
        }
        return new XString(stringBuffer.toString());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionMultiArgs, ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void checkNumberArgs(int i) throws WrongNumberArgsException {
        if (i < 2) {
            reportWrongNumberArgs();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.functions.FunctionMultiArgs, ohos.com.sun.org.apache.xpath.internal.functions.Function3Args, ohos.com.sun.org.apache.xpath.internal.functions.Function2Args, ohos.com.sun.org.apache.xpath.internal.functions.FunctionOneArg, ohos.com.sun.org.apache.xpath.internal.functions.Function
    public void reportWrongNumberArgs() throws WrongNumberArgsException {
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("gtone", null));
    }
}

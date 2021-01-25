package ohos.com.sun.org.apache.xpath.internal.functions;

import ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.compiler.FunctionTable;
import ohos.com.sun.org.apache.xpath.internal.objects.XBoolean;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class FuncExtFunctionAvailable extends FunctionOneArg {
    static final long serialVersionUID = 5118814314918592241L;
    private transient FunctionTable m_functionTable = null;

    @Override // ohos.com.sun.org.apache.xpath.internal.functions.Function, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        String str;
        String str2 = this.m_arg0.execute(xPathContext).str();
        int indexOf = str2.indexOf(58);
        if (indexOf < 0) {
            str = "http://www.w3.org/1999/XSL/Transform";
        } else {
            str = xPathContext.getNamespaceContext().getNamespaceForPrefix(str2.substring(0, indexOf));
            if (str == null) {
                return XBoolean.S_FALSE;
            }
            str2 = str2.substring(indexOf + 1);
        }
        if (!str.equals("http://www.w3.org/1999/XSL/Transform")) {
            return ((ExtensionsProvider) xPathContext.getOwnerObject()).functionAvailable(str, str2) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
        }
        try {
            if (this.m_functionTable == null) {
                this.m_functionTable = new FunctionTable();
            }
            return this.m_functionTable.functionAvailable(str2) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
        } catch (Exception unused) {
            return XBoolean.S_FALSE;
        }
    }

    public void setFunctionTable(FunctionTable functionTable) {
        this.m_functionTable = functionTable;
    }
}

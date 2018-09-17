package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xml.utils.Constants;
import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class FuncExtFunctionAvailable extends FunctionOneArg {
    static final long serialVersionUID = 5118814314918592241L;
    private transient FunctionTable m_functionTable = null;

    public XObject execute(XPathContext xctxt) throws TransformerException {
        String namespace;
        String methName;
        String fullName = this.m_arg0.execute(xctxt).str();
        int indexOfNSSep = fullName.indexOf(58);
        if (indexOfNSSep < 0) {
            String prefix = "";
            namespace = Constants.S_XSLNAMESPACEURL;
            methName = fullName;
        } else {
            namespace = xctxt.getNamespaceContext().getNamespaceForPrefix(fullName.substring(0, indexOfNSSep));
            if (namespace == null) {
                return XBoolean.S_FALSE;
            }
            methName = fullName.substring(indexOfNSSep + 1);
        }
        if (namespace.equals(Constants.S_XSLNAMESPACEURL)) {
            try {
                XObject xObject;
                if (this.m_functionTable == null) {
                    this.m_functionTable = new FunctionTable();
                }
                if (this.m_functionTable.functionAvailable(methName)) {
                    xObject = XBoolean.S_TRUE;
                } else {
                    xObject = XBoolean.S_FALSE;
                }
                return xObject;
            } catch (Exception e) {
                return XBoolean.S_FALSE;
            }
        }
        return ((ExtensionsProvider) xctxt.getOwnerObject()).functionAvailable(namespace, methName) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }

    public void setFunctionTable(FunctionTable aTable) {
        this.m_functionTable = aTable;
    }
}

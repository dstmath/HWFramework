package org.apache.xpath.functions;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.QName;
import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

public class FuncExtElementAvailable extends FunctionOneArg {
    static final long serialVersionUID = -472533699257968546L;

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
        if (namespace.equals(Constants.S_XSLNAMESPACEURL) || namespace.equals("http://xml.apache.org/xalan")) {
            try {
                return ((TransformerImpl) xctxt.getOwnerObject()).getStylesheet().getAvailableElements().containsKey(new QName(namespace, methName)) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
            } catch (Exception e) {
                return XBoolean.S_FALSE;
            }
        }
        return ((ExtensionsProvider) xctxt.getOwnerObject()).elementAvailable(namespace, methName) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
}

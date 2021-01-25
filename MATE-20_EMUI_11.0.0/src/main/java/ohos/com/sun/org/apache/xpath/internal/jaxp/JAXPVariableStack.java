package ohos.com.sun.org.apache.xpath.internal.jaxp;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.QName;
import ohos.com.sun.org.apache.xpath.internal.VariableStack;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.xpath.XPathVariableResolver;

public class JAXPVariableStack extends VariableStack {
    private final XPathVariableResolver resolver;

    public JAXPVariableStack(XPathVariableResolver xPathVariableResolver) {
        this.resolver = xPathVariableResolver;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.VariableStack
    public XObject getVariableOrParam(XPathContext xPathContext, QName qName) throws TransformerException, IllegalArgumentException {
        if (qName != null) {
            ohos.javax.xml.namespace.QName qName2 = new ohos.javax.xml.namespace.QName(qName.getNamespace(), qName.getLocalPart());
            Object resolveVariable = this.resolver.resolveVariable(qName2);
            if (resolveVariable != null) {
                return XObject.create(resolveVariable, xPathContext);
            }
            throw new TransformerException(XSLMessages.createXPATHMessage("ER_RESOLVE_VARIABLE_RETURNS_NULL", new Object[]{qName2.toString()}));
        }
        throw new IllegalArgumentException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"Variable qname"}));
    }
}

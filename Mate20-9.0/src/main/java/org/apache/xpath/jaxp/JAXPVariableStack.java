package org.apache.xpath.jaxp;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.xalan.res.XSLMessages;
import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;

public class JAXPVariableStack extends VariableStack {
    private final XPathVariableResolver resolver;

    public JAXPVariableStack(XPathVariableResolver resolver2) {
        super(2);
        this.resolver = resolver2;
    }

    public XObject getVariableOrParam(XPathContext xctxt, QName qname) throws TransformerException, IllegalArgumentException {
        if (qname != null) {
            javax.xml.namespace.QName name = new javax.xml.namespace.QName(qname.getNamespace(), qname.getLocalPart());
            Object varValue = this.resolver.resolveVariable(name);
            if (varValue != null) {
                return XObject.create(varValue, xctxt);
            }
            throw new TransformerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_RESOLVE_VARIABLE_RETURNS_NULL, new Object[]{name.toString()}));
        }
        throw new IllegalArgumentException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"Variable qname"}));
    }
}

package org.apache.xpath.jaxp;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class JAXPVariableStack extends VariableStack {
    private final XPathVariableResolver resolver;

    public JAXPVariableStack(XPathVariableResolver resolver) {
        super(2);
        this.resolver = resolver;
    }

    public XObject getVariableOrParam(XPathContext xctxt, QName qname) throws TransformerException, IllegalArgumentException {
        if (qname == null) {
            throw new IllegalArgumentException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"Variable qname"}));
        }
        Object varValue = this.resolver.resolveVariable(new javax.xml.namespace.QName(qname.getNamespace(), qname.getLocalPart()));
        if (varValue != null) {
            return XObject.create(varValue, xctxt);
        }
        throw new TransformerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_RESOLVE_VARIABLE_RETURNS_NULL, new Object[]{name.toString()}));
    }
}

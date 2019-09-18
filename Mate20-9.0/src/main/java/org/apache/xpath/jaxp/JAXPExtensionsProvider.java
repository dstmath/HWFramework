package org.apache.xpath.jaxp;

import java.util.ArrayList;
import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import org.apache.xalan.res.XSLMessages;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;

public class JAXPExtensionsProvider implements ExtensionsProvider {
    private boolean extensionInvocationDisabled = false;
    private final XPathFunctionResolver resolver;

    public JAXPExtensionsProvider(XPathFunctionResolver resolver2) {
        this.resolver = resolver2;
        this.extensionInvocationDisabled = false;
    }

    public JAXPExtensionsProvider(XPathFunctionResolver resolver2, boolean featureSecureProcessing) {
        this.resolver = resolver2;
        this.extensionInvocationDisabled = featureSecureProcessing;
    }

    public boolean functionAvailable(String ns, String funcName) throws TransformerException {
        if (funcName != null) {
            try {
                return this.resolver.resolveFunction(new QName(ns, funcName), 0) != null;
            } catch (Exception e) {
                return false;
            }
        } else {
            throw new NullPointerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"Function Name"}));
        }
    }

    public boolean elementAvailable(String ns, String elemName) throws TransformerException {
        return false;
    }

    public Object extFunction(String ns, String funcName, Vector argVec, Object methodKey) throws TransformerException {
        if (funcName != null) {
            try {
                QName myQName = new QName(ns, funcName);
                if (!this.extensionInvocationDisabled) {
                    int arity = argVec.size();
                    XPathFunction xpathFunction = this.resolver.resolveFunction(myQName, arity);
                    ArrayList argList = new ArrayList(arity);
                    for (int i = 0; i < arity; i++) {
                        Object argument = argVec.elementAt(i);
                        if (argument instanceof XNodeSet) {
                            argList.add(i, ((XNodeSet) argument).nodelist());
                        } else if (argument instanceof XObject) {
                            argList.add(i, ((XObject) argument).object());
                        } else {
                            argList.add(i, argument);
                        }
                    }
                    return xpathFunction.evaluate(argList);
                }
                throw new XPathFunctionException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED, new Object[]{myQName.toString()}));
            } catch (XPathFunctionException xfe) {
                throw new WrappedRuntimeException(xfe);
            } catch (Exception e) {
                throw new TransformerException(e);
            }
        } else {
            throw new NullPointerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL, new Object[]{"Function Name"}));
        }
    }

    public Object extFunction(FuncExtFunction extFunction, Vector argVec) throws TransformerException {
        try {
            String namespace = extFunction.getNamespace();
            String functionName = extFunction.getFunctionName();
            int arity = extFunction.getArgCount();
            QName myQName = new QName(namespace, functionName);
            if (!this.extensionInvocationDisabled) {
                XPathFunction xpathFunction = this.resolver.resolveFunction(myQName, arity);
                ArrayList argList = new ArrayList(arity);
                for (int i = 0; i < arity; i++) {
                    Object argument = argVec.elementAt(i);
                    if (argument instanceof XNodeSet) {
                        argList.add(i, ((XNodeSet) argument).nodelist());
                    } else if (argument instanceof XObject) {
                        argList.add(i, ((XObject) argument).object());
                    } else {
                        argList.add(i, argument);
                    }
                }
                return xpathFunction.evaluate(argList);
            }
            throw new XPathFunctionException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED, new Object[]{myQName.toString()}));
        } catch (XPathFunctionException xfe) {
            throw new WrappedRuntimeException(xfe);
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }
}

package ohos.com.sun.org.apache.xpath.internal.jaxp;

import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncExtFunction;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.xpath.XPathFunction;
import ohos.javax.xml.xpath.XPathFunctionException;
import ohos.javax.xml.xpath.XPathFunctionResolver;
import ohos.jdk.xml.internal.JdkXmlFeatures;

public class JAXPExtensionsProvider implements ExtensionsProvider {
    private boolean extensionInvocationDisabled = false;
    private final XPathFunctionResolver resolver;

    @Override // ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider
    public boolean elementAvailable(String str, String str2) throws TransformerException {
        return false;
    }

    public JAXPExtensionsProvider(XPathFunctionResolver xPathFunctionResolver) {
        this.resolver = xPathFunctionResolver;
        this.extensionInvocationDisabled = false;
    }

    public JAXPExtensionsProvider(XPathFunctionResolver xPathFunctionResolver, boolean z, JdkXmlFeatures jdkXmlFeatures) {
        this.resolver = xPathFunctionResolver;
        if (z && !jdkXmlFeatures.getFeature(JdkXmlFeatures.XmlFeature.ENABLE_EXTENSION_FUNCTION)) {
            this.extensionInvocationDisabled = true;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider
    public boolean functionAvailable(String str, String str2) throws TransformerException {
        if (str2 != null) {
            try {
                return this.resolver.resolveFunction(new QName(str, str2), 0) != null;
            } catch (Exception unused) {
                return false;
            }
        } else {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"Function Name"}));
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider
    public Object extFunction(String str, String str2, Vector vector, Object obj) throws TransformerException {
        if (str2 != null) {
            try {
                QName qName = new QName(str, str2);
                if (!this.extensionInvocationDisabled) {
                    int size = vector.size();
                    XPathFunction resolveFunction = this.resolver.resolveFunction(qName, size);
                    ArrayList arrayList = new ArrayList(size);
                    for (int i = 0; i < size; i++) {
                        Object elementAt = vector.elementAt(i);
                        if (elementAt instanceof XNodeSet) {
                            arrayList.add(i, ((XNodeSet) elementAt).nodelist());
                        } else if (elementAt instanceof XObject) {
                            arrayList.add(i, ((XObject) elementAt).object());
                        } else {
                            arrayList.add(i, elementAt);
                        }
                    }
                    return resolveFunction.evaluate(arrayList);
                }
                throw new XPathFunctionException(XSLMessages.createXPATHMessage("ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED", new Object[]{qName.toString()}));
            } catch (XPathFunctionException e) {
                throw new WrappedRuntimeException(e);
            } catch (Exception e2) {
                throw new TransformerException(e2);
            }
        } else {
            throw new NullPointerException(XSLMessages.createXPATHMessage("ER_ARG_CANNOT_BE_NULL", new Object[]{"Function Name"}));
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExtensionsProvider
    public Object extFunction(FuncExtFunction funcExtFunction, Vector vector) throws TransformerException {
        try {
            String namespace = funcExtFunction.getNamespace();
            String functionName = funcExtFunction.getFunctionName();
            int argCount = funcExtFunction.getArgCount();
            QName qName = new QName(namespace, functionName);
            if (!this.extensionInvocationDisabled) {
                XPathFunction resolveFunction = this.resolver.resolveFunction(qName, argCount);
                ArrayList arrayList = new ArrayList(argCount);
                for (int i = 0; i < argCount; i++) {
                    Object elementAt = vector.elementAt(i);
                    if (elementAt instanceof XNodeSet) {
                        arrayList.add(i, ((XNodeSet) elementAt).nodelist());
                    } else if (elementAt instanceof XObject) {
                        arrayList.add(i, ((XObject) elementAt).object());
                    } else {
                        arrayList.add(i, elementAt);
                    }
                }
                return resolveFunction.evaluate(arrayList);
            }
            throw new XPathFunctionException(XSLMessages.createXPATHMessage("ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED", new Object[]{qName.toString()}));
        } catch (XPathFunctionException e) {
            throw new WrappedRuntimeException(e);
        } catch (Exception e2) {
            throw new TransformerException(e2);
        }
    }
}

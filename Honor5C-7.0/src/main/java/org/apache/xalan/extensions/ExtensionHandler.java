package org.apache.xalan.extensions;

import java.io.IOException;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.functions.FuncExtFunction;

public abstract class ExtensionHandler {
    protected String m_namespaceUri;
    protected String m_scriptLang;

    public abstract Object callFunction(String str, Vector vector, Object obj, ExpressionContext expressionContext) throws TransformerException;

    public abstract Object callFunction(FuncExtFunction funcExtFunction, Vector vector, ExpressionContext expressionContext) throws TransformerException;

    public abstract boolean isElementAvailable(String str);

    public abstract boolean isFunctionAvailable(String str);

    public abstract void processElement(String str, ElemTemplateElement elemTemplateElement, TransformerImpl transformerImpl, Stylesheet stylesheet, Object obj) throws TransformerException, IOException;

    static Class getClassForName(String className) throws ClassNotFoundException {
        if (className.equals("org.apache.xalan.xslt.extensions.Redirect")) {
            className = "org.apache.xalan.lib.Redirect";
        }
        return ObjectFactory.findProviderClass(className, ObjectFactory.findClassLoader(), true);
    }

    protected ExtensionHandler(String namespaceUri, String scriptLang) {
        this.m_namespaceUri = namespaceUri;
        this.m_scriptLang = scriptLang;
    }
}

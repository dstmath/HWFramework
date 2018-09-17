package org.apache.xalan.extensions;

import java.lang.reflect.Constructor;
import javax.xml.transform.TransformerException;

public class ExtensionNamespaceSupport {
    Object[] m_args = null;
    String m_handlerClass = null;
    String m_namespace = null;
    Class[] m_sig = null;

    public ExtensionNamespaceSupport(String namespace, String handlerClass, Object[] constructorArgs) {
        this.m_namespace = namespace;
        this.m_handlerClass = handlerClass;
        this.m_args = constructorArgs;
        this.m_sig = new Class[this.m_args.length];
        int i = 0;
        while (i < this.m_args.length) {
            if (this.m_args[i] != null) {
                this.m_sig[i] = this.m_args[i].getClass();
                i++;
            } else {
                this.m_sig = null;
                return;
            }
        }
    }

    public String getNamespace() {
        return this.m_namespace;
    }

    public ExtensionHandler launch() throws TransformerException {
        try {
            Class cl = ExtensionHandler.getClassForName(this.m_handlerClass);
            Constructor con = null;
            if (this.m_sig != null) {
                con = cl.getConstructor(this.m_sig);
            } else {
                Constructor[] cons = cl.getConstructors();
                for (int i = 0; i < cons.length; i++) {
                    if (cons[i].getParameterTypes().length == this.m_args.length) {
                        con = cons[i];
                        break;
                    }
                }
            }
            if (con != null) {
                return (ExtensionHandler) con.newInstance(this.m_args);
            }
            throw new TransformerException("ExtensionHandler constructor not found");
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }
}

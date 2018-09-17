package org.apache.xalan.extensions;

import java.util.Vector;
import org.apache.xalan.templates.Constants;
import org.apache.xpath.compiler.PsuedoNames;

public class ExtensionNamespacesManager {
    private Vector m_extensions = new Vector();
    private Vector m_predefExtensions = new Vector(7);
    private Vector m_unregisteredExtensions = new Vector();

    public ExtensionNamespacesManager() {
        setPredefinedNamespaces();
    }

    public void registerExtension(String namespace) {
        if (namespaceIndex(namespace, this.m_extensions) == -1) {
            int predef = namespaceIndex(namespace, this.m_predefExtensions);
            if (predef != -1) {
                this.m_extensions.add(this.m_predefExtensions.get(predef));
            } else if (!this.m_unregisteredExtensions.contains(namespace)) {
                this.m_unregisteredExtensions.add(namespace);
            }
        }
    }

    public void registerExtension(ExtensionNamespaceSupport extNsSpt) {
        String namespace = extNsSpt.getNamespace();
        if (namespaceIndex(namespace, this.m_extensions) == -1) {
            this.m_extensions.add(extNsSpt);
            if (this.m_unregisteredExtensions.contains(namespace)) {
                this.m_unregisteredExtensions.remove(namespace);
            }
        }
    }

    public int namespaceIndex(String namespace, Vector extensions) {
        for (int i = 0; i < extensions.size(); i++) {
            if (((ExtensionNamespaceSupport) extensions.get(i)).getNamespace().equals(namespace)) {
                return i;
            }
        }
        return -1;
    }

    public Vector getExtensions() {
        return this.m_extensions;
    }

    public void registerUnregisteredNamespaces() {
        for (int i = 0; i < this.m_unregisteredExtensions.size(); i++) {
            ExtensionNamespaceSupport extNsSpt = defineJavaNamespace((String) this.m_unregisteredExtensions.get(i));
            if (extNsSpt != null) {
                this.m_extensions.add(extNsSpt);
            }
        }
    }

    public ExtensionNamespaceSupport defineJavaNamespace(String ns) {
        return defineJavaNamespace(ns, ns);
    }

    public ExtensionNamespaceSupport defineJavaNamespace(String ns, String classOrPackage) {
        if (ns == null || ns.trim().length() == 0) {
            return null;
        }
        String className = classOrPackage;
        if (classOrPackage.startsWith("class:")) {
            className = classOrPackage.substring(6);
        }
        int lastSlash = className.lastIndexOf(PsuedoNames.PSEUDONAME_ROOT);
        if (-1 != lastSlash) {
            className = className.substring(lastSlash + 1);
        }
        if (className == null || className.trim().length() == 0) {
            return null;
        }
        try {
            ExtensionHandler.getClassForName(className);
            return new ExtensionNamespaceSupport(ns, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{ns, "javaclass", className});
        } catch (ClassNotFoundException e) {
            return new ExtensionNamespaceSupport(ns, "org.apache.xalan.extensions.ExtensionHandlerJavaPackage", new Object[]{ns, "javapackage", className + Constants.ATTRVAL_THIS});
        }
    }

    private void setPredefinedNamespaces() {
        String uri = org.apache.xml.utils.Constants.S_EXTENSIONS_JAVA_URL;
        String handlerClassName = "org.apache.xalan.extensions.ExtensionHandlerJavaPackage";
        String lang = "javapackage";
        String lib = "";
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, lib}));
        uri = org.apache.xml.utils.Constants.S_EXTENSIONS_OLD_JAVA_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, lib}));
        uri = org.apache.xml.utils.Constants.S_EXTENSIONS_LOTUSXSL_JAVA_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, lib}));
        handlerClassName = "org.apache.xalan.extensions.ExtensionHandlerJavaClass";
        lang = "javaclass";
        lib = "org.apache.xalan.lib.Extensions";
        Vector vector = this.m_predefExtensions;
        vector.add(new ExtensionNamespaceSupport("http://xml.apache.org/xalan", handlerClassName, new Object[]{"http://xml.apache.org/xalan", lang, lib}));
        uri = org.apache.xml.utils.Constants.S_BUILTIN_OLD_EXTENSIONS_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, lib}));
        uri = org.apache.xml.utils.Constants.S_EXTENSIONS_REDIRECT_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.Redirect"}));
        uri = org.apache.xml.utils.Constants.S_EXTENSIONS_PIPE_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.PipeDocument"}));
        uri = org.apache.xml.utils.Constants.S_EXTENSIONS_SQL_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.sql.XConnection"}));
        uri = org.apache.xml.utils.Constants.S_EXSLT_COMMON_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.ExsltCommon"}));
        uri = org.apache.xml.utils.Constants.S_EXSLT_MATH_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.ExsltMath"}));
        uri = org.apache.xml.utils.Constants.S_EXSLT_SETS_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.ExsltSets"}));
        uri = org.apache.xml.utils.Constants.S_EXSLT_DATETIME_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.ExsltDatetime"}));
        uri = org.apache.xml.utils.Constants.S_EXSLT_DYNAMIC_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.ExsltDynamic"}));
        uri = org.apache.xml.utils.Constants.S_EXSLT_STRINGS_URL;
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(uri, handlerClassName, new Object[]{uri, lang, "org.apache.xalan.lib.ExsltStrings"}));
    }
}

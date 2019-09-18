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
        if (className.startsWith("class:")) {
            className = className.substring(6);
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
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXTENSIONS_JAVA_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaPackage", new Object[]{org.apache.xml.utils.Constants.S_EXTENSIONS_JAVA_URL, "javapackage", ""}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXTENSIONS_OLD_JAVA_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaPackage", new Object[]{org.apache.xml.utils.Constants.S_EXTENSIONS_OLD_JAVA_URL, "javapackage", ""}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXTENSIONS_LOTUSXSL_JAVA_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaPackage", new Object[]{org.apache.xml.utils.Constants.S_EXTENSIONS_LOTUSXSL_JAVA_URL, "javapackage", ""}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport("http://xml.apache.org/xalan", "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{"http://xml.apache.org/xalan", "javaclass", "org.apache.xalan.lib.Extensions"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_BUILTIN_OLD_EXTENSIONS_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_BUILTIN_OLD_EXTENSIONS_URL, "javaclass", "org.apache.xalan.lib.Extensions"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXTENSIONS_REDIRECT_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXTENSIONS_REDIRECT_URL, "javaclass", "org.apache.xalan.lib.Redirect"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXTENSIONS_PIPE_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXTENSIONS_PIPE_URL, "javaclass", "org.apache.xalan.lib.PipeDocument"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXTENSIONS_SQL_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXTENSIONS_SQL_URL, "javaclass", "org.apache.xalan.lib.sql.XConnection"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXSLT_COMMON_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXSLT_COMMON_URL, "javaclass", "org.apache.xalan.lib.ExsltCommon"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXSLT_MATH_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXSLT_MATH_URL, "javaclass", "org.apache.xalan.lib.ExsltMath"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXSLT_SETS_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXSLT_SETS_URL, "javaclass", "org.apache.xalan.lib.ExsltSets"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXSLT_DATETIME_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXSLT_DATETIME_URL, "javaclass", "org.apache.xalan.lib.ExsltDatetime"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXSLT_DYNAMIC_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXSLT_DYNAMIC_URL, "javaclass", "org.apache.xalan.lib.ExsltDynamic"}));
        this.m_predefExtensions.add(new ExtensionNamespaceSupport(org.apache.xml.utils.Constants.S_EXSLT_STRINGS_URL, "org.apache.xalan.extensions.ExtensionHandlerJavaClass", new Object[]{org.apache.xml.utils.Constants.S_EXSLT_STRINGS_URL, "javaclass", "org.apache.xalan.lib.ExsltStrings"}));
    }
}

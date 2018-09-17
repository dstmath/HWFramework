package org.apache.xalan.extensions;

import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.functions.FuncExtFunctionAvailable;
import org.apache.xpath.functions.Function;

public class ExpressionVisitor extends XPathVisitor {
    private StylesheetRoot m_sroot;

    public ExpressionVisitor(StylesheetRoot sroot) {
        this.m_sroot = sroot;
    }

    public boolean visitFunction(ExpressionOwner owner, Function func) {
        if (func instanceof FuncExtFunction) {
            this.m_sroot.getExtensionNamespacesManager().registerExtension(((FuncExtFunction) func).getNamespace());
        } else if (func instanceof FuncExtFunctionAvailable) {
            String arg = ((FuncExtFunctionAvailable) func).getArg0().toString();
            if (arg.indexOf(":") > 0) {
                this.m_sroot.getExtensionNamespacesManager().registerExtension(this.m_sroot.getNamespaceForPrefix(arg.substring(0, arg.indexOf(":"))));
            }
        }
        return true;
    }
}

package org.apache.xalan.templates;

import org.apache.xpath.XPathVisitor;

public class XSLTVisitor extends XPathVisitor {
    public boolean visitInstruction(ElemTemplateElement elem) {
        return true;
    }

    public boolean visitStylesheet(ElemTemplateElement elem) {
        return true;
    }

    public boolean visitTopLevelInstruction(ElemTemplateElement elem) {
        return true;
    }

    public boolean visitTopLevelVariableOrParamDecl(ElemTemplateElement elem) {
        return true;
    }

    public boolean visitVariableOrParamDecl(ElemVariable elem) {
        return true;
    }

    public boolean visitLiteralResultElement(ElemLiteralResult elem) {
        return true;
    }

    public boolean visitAVT(AVT elem) {
        return true;
    }

    public boolean visitExtensionElement(ElemExtensionCall elem) {
        return true;
    }
}

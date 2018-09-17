package org.apache.xalan.templates;

import java.util.Vector;
import org.apache.xml.utils.QName;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.operations.Variable;

public class VarNameCollector extends XPathVisitor {
    Vector m_refs = new Vector();

    public void reset() {
        this.m_refs.removeAllElements();
    }

    public int getVarCount() {
        return this.m_refs.size();
    }

    boolean doesOccur(QName refName) {
        return this.m_refs.contains(refName);
    }

    public boolean visitVariableRef(ExpressionOwner owner, Variable var) {
        this.m_refs.addElement(var.getQName());
        return true;
    }
}

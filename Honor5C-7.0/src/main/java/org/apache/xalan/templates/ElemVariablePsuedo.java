package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPath;

public class ElemVariablePsuedo extends ElemVariable {
    static final long serialVersionUID = 692295692732588486L;
    XUnresolvedVariableSimple m_lazyVar;

    public void setSelect(XPath v) {
        super.setSelect(v);
        this.m_lazyVar = new XUnresolvedVariableSimple(this);
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        transformer.getXPathContext().getVarStack().setLocalVariable(this.m_index, this.m_lazyVar);
    }
}

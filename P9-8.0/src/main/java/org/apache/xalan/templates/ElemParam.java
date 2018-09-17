package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;

public class ElemParam extends ElemVariable {
    static final long serialVersionUID = -1131781475589006431L;
    int m_qnameID;

    public int getXSLToken() {
        return 41;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_PARAMVARIABLE_STRING;
    }

    public ElemParam(ElemParam param) throws TransformerException {
        super(param);
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        this.m_qnameID = sroot.getComposeState().getQNameID(this.m_qname);
        int parentToken = this.m_parentNode.getXSLToken();
        if (parentToken == 19 || parentToken == 88) {
            ElemTemplate elemTemplate = (ElemTemplate) this.m_parentNode;
            elemTemplate.m_inArgsSize++;
        }
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        if (!transformer.getXPathContext().getVarStack().isLocalSet(this.m_index)) {
            transformer.getXPathContext().getVarStack().setLocalVariable(this.m_index, getValue(transformer, transformer.getXPathContext().getCurrentNode()));
        }
    }
}

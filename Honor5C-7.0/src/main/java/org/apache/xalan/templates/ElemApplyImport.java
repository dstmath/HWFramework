package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;

public class ElemApplyImport extends ElemTemplateElement {
    static final long serialVersionUID = 3764728663373024038L;

    public int getXSLToken() {
        return 72;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_APPLY_IMPORTS_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        if (transformer.currentTemplateRuleIsNull()) {
            transformer.getMsgMgr().error(this, XSLTErrorResources.ER_NO_APPLY_IMPORT_IN_FOR_EACH);
        }
        int sourceNode = transformer.getXPathContext().getCurrentNode();
        if (-1 != sourceNode) {
            transformer.applyTemplateToNode(this, transformer.getMatchedTemplate(), sourceNode);
        } else {
            transformer.getMsgMgr().error(this, XSLTErrorResources.ER_NULL_SOURCENODE_APPLYIMPORTS);
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
        return null;
    }
}

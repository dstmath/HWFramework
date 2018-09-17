package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.apache.xpath.compiler.OpCodes;

public class ElemAttributeSet extends ElemUse {
    static final long serialVersionUID = -426740318278164496L;
    public QName m_qname;

    public ElemAttributeSet() {
        this.m_qname = null;
    }

    public void setName(QName name) {
        this.m_qname = name;
    }

    public QName getName() {
        return this.m_qname;
    }

    public int getXSLToken() {
        return 40;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_ATTRIBUTESET_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        if (transformer.isRecursiveAttrSet(this)) {
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_XSLATTRSET_USED_ITSELF, new Object[]{this.m_qname.getLocalPart()}));
        }
        transformer.pushElemAttributeSet(this);
        super.execute(transformer);
        for (ElemAttribute attr = (ElemAttribute) getFirstChildElem(); attr != null; attr = (ElemAttribute) attr.getNextSiblingElem()) {
            attr.execute(transformer);
        }
        transformer.popElemAttributeSet();
    }

    public ElemTemplateElement appendChildElem(ElemTemplateElement newChild) {
        switch (newChild.getXSLToken()) {
            case OpCodes.FROM_SELF /*48*/:
                break;
            default:
                error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
                break;
        }
        return super.appendChild(newChild);
    }

    public void recompose(StylesheetRoot root) {
        root.recomposeAttributeSets(this);
    }
}

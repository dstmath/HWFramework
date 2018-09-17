package org.apache.xalan.templates;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;

public class ElemCallTemplate extends ElemForEach {
    static final long serialVersionUID = 5009634612916030591L;
    protected ElemWithParam[] m_paramElems = null;
    private ElemTemplate m_template = null;
    public QName m_templateName = null;

    public void setName(QName name) {
        this.m_templateName = name;
    }

    public QName getName() {
        return this.m_templateName;
    }

    public int getXSLToken() {
        return 17;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_CALLTEMPLATE_STRING;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        int i;
        super.compose(sroot);
        int length = getParamElemCount();
        for (i = 0; i < length; i++) {
            getParamElem(i).compose(sroot);
        }
        if (this.m_templateName != null && this.m_template == null) {
            this.m_template = getStylesheetRoot().getTemplateComposed(this.m_templateName);
            if (this.m_template == null) {
                throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ELEMTEMPLATEELEM_ERR, new Object[]{this.m_templateName}), this);
            }
            length = getParamElemCount();
            for (i = 0; i < length; i++) {
                ElemWithParam ewp = getParamElem(i);
                ewp.m_index = -1;
                int etePos = 0;
                ElemTemplateElement ete = this.m_template.getFirstChildElem();
                while (ete != null && ete.getXSLToken() == 41) {
                    if (((ElemParam) ete).getName().equals(ewp.getName())) {
                        ewp.m_index = etePos;
                    }
                    etePos++;
                    ete = ete.getNextSiblingElem();
                }
            }
        }
    }

    public void endCompose(StylesheetRoot sroot) throws TransformerException {
        int length = getParamElemCount();
        for (int i = 0; i < length; i++) {
            getParamElem(i).endCompose(sroot);
        }
        super.endCompose(sroot);
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        if (this.m_template != null) {
            XPathContext xctxt = transformer.getXPathContext();
            VariableStack vars = xctxt.getVarStack();
            int thisframe = vars.getStackFrame();
            int nextFrame = vars.link(this.m_template.m_frameSize);
            if (this.m_template.m_inArgsSize > 0) {
                vars.clearLocalSlots(0, this.m_template.m_inArgsSize);
                if (this.m_paramElems != null) {
                    int currentNode = xctxt.getCurrentNode();
                    vars.setStackFrame(thisframe);
                    for (ElemWithParam ewp : this.m_paramElems) {
                        if (ewp.m_index >= 0) {
                            vars.setLocalVariable(ewp.m_index, ewp.getValue(transformer, currentNode), nextFrame);
                        }
                    }
                    vars.setStackFrame(nextFrame);
                }
            }
            SourceLocator savedLocator = xctxt.getSAXLocator();
            try {
                xctxt.setSAXLocator(this.m_template);
                transformer.pushElemTemplateElement(this.m_template);
                this.m_template.execute(transformer);
            } finally {
                transformer.popElemTemplateElement();
                xctxt.setSAXLocator(savedLocator);
                vars.unlink(thisframe);
            }
        } else {
            transformer.getMsgMgr().error((SourceLocator) this, XSLTErrorResources.ER_TEMPLATE_NOT_FOUND, new Object[]{this.m_templateName});
        }
    }

    public int getParamElemCount() {
        return this.m_paramElems == null ? 0 : this.m_paramElems.length;
    }

    public ElemWithParam getParamElem(int i) {
        return this.m_paramElems[i];
    }

    public void setParamElem(ElemWithParam ParamElem) {
        if (this.m_paramElems == null) {
            this.m_paramElems = new ElemWithParam[1];
            this.m_paramElems[0] = ParamElem;
            return;
        }
        int length = this.m_paramElems.length;
        ElemWithParam[] ewp = new ElemWithParam[(length + 1)];
        System.arraycopy(this.m_paramElems, 0, ewp, 0, length);
        this.m_paramElems = ewp;
        ewp[length] = ParamElem;
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        if (2 == newChild.getXSLToken()) {
            setParamElem((ElemWithParam) newChild);
        }
        return super.appendChild(newChild);
    }

    public void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        super.callChildVisitors(visitor, callAttrs);
    }
}

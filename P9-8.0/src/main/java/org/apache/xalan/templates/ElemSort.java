package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xpath.XPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class ElemSort extends ElemTemplateElement {
    static final long serialVersionUID = -4991510257335851938L;
    private AVT m_caseorder_avt = null;
    private AVT m_dataType_avt = null;
    private AVT m_lang_avt = null;
    private AVT m_order_avt = null;
    private XPath m_selectExpression = null;

    public void setSelect(XPath v) {
        if (v.getPatternString().indexOf("{") < 0) {
            this.m_selectExpression = v;
        } else {
            error(XSLTErrorResources.ER_NO_CURLYBRACE, null);
        }
    }

    public XPath getSelect() {
        return this.m_selectExpression;
    }

    public void setLang(AVT v) {
        this.m_lang_avt = v;
    }

    public AVT getLang() {
        return this.m_lang_avt;
    }

    public void setDataType(AVT v) {
        this.m_dataType_avt = v;
    }

    public AVT getDataType() {
        return this.m_dataType_avt;
    }

    public void setOrder(AVT v) {
        this.m_order_avt = v;
    }

    public AVT getOrder() {
        return this.m_order_avt;
    }

    public void setCaseOrder(AVT v) {
        this.m_caseorder_avt = v;
    }

    public AVT getCaseOrder() {
        return this.m_caseorder_avt;
    }

    public int getXSLToken() {
        return 64;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_SORT_STRING;
    }

    public Node appendChild(Node newChild) throws DOMException {
        error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
        return null;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        ComposeState cstate = sroot.getComposeState();
        Vector vnames = cstate.getVariableNames();
        if (this.m_caseorder_avt != null) {
            this.m_caseorder_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_dataType_avt != null) {
            this.m_dataType_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_lang_avt != null) {
            this.m_lang_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_order_avt != null) {
            this.m_order_avt.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (this.m_selectExpression != null) {
            this.m_selectExpression.fixupVariables(vnames, cstate.getGlobalsSize());
        }
    }
}

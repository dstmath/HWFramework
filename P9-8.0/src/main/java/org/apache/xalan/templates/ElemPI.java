package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

public class ElemPI extends ElemTemplateElement {
    static final long serialVersionUID = 5621976448020889825L;
    private AVT m_name_atv = null;

    public void setName(AVT v) {
        this.m_name_atv = v;
    }

    public AVT getName() {
        return this.m_name_atv;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        Vector vnames = sroot.getComposeState().getVariableNames();
        if (this.m_name_atv != null) {
            this.m_name_atv.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
        }
    }

    public int getXSLToken() {
        return 58;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_PI_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        XPathContext xctxt = transformer.getXPathContext();
        String piName = this.m_name_atv == null ? null : this.m_name_atv.evaluate(xctxt, xctxt.getCurrentNode(), this);
        if (piName != null) {
            if (piName.equalsIgnoreCase("xml")) {
                transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML, new Object[]{"name", piName});
            } else if (this.m_name_atv.isSimple() || (XML11Char.isXML11ValidNCName(piName) ^ 1) == 0) {
                try {
                    transformer.getResultTreeHandler().processingInstruction(piName, transformer.transformToString(this));
                } catch (SAXException se) {
                    throw new TransformerException(se);
                }
            } else {
                transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME, new Object[]{"name", piName});
            }
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        switch (newChild.getXSLToken()) {
            case 9:
            case 17:
            case 28:
            case 30:
            case 35:
            case 36:
            case 37:
            case 42:
            case 50:
            case Constants.ELEMNAME_APPLY_IMPORTS /*72*/:
            case Constants.ELEMNAME_VARIABLE /*73*/:
            case Constants.ELEMNAME_COPY_OF /*74*/:
            case Constants.ELEMNAME_MESSAGE /*75*/:
            case Constants.ELEMNAME_TEXTLITERALRESULT /*78*/:
                break;
            default:
                error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
                break;
        }
        return super.appendChild(newChild);
    }
}

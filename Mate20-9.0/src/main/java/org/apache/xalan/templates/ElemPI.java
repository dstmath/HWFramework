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
            } else if (this.m_name_atv.isSimple() || XML11Char.isXML11ValidNCName(piName)) {
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
        int type = newChild.getXSLToken();
        if (!(type == 9 || type == 17 || type == 28 || type == 30 || type == 42 || type == 50 || type == 78)) {
            switch (type) {
                case 35:
                case 36:
                case 37:
                    break;
                default:
                    switch (type) {
                        case Constants.ELEMNAME_APPLY_IMPORTS:
                        case Constants.ELEMNAME_VARIABLE:
                        case Constants.ELEMNAME_COPY_OF:
                        case Constants.ELEMNAME_MESSAGE:
                            break;
                        default:
                            error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
                            break;
                    }
            }
        }
        return super.appendChild(newChild);
    }
}

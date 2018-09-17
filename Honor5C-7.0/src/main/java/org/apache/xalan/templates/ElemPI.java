package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.Keywords;
import org.apache.xpath.compiler.OpCodes;
import org.xml.sax.SAXException;

public class ElemPI extends ElemTemplateElement {
    static final long serialVersionUID = 5621976448020889825L;
    private AVT m_name_atv;

    public ElemPI() {
        this.m_name_atv = null;
    }

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
        String piName = null;
        XPathContext xctxt = transformer.getXPathContext();
        int sourceNode = xctxt.getCurrentNode();
        if (this.m_name_atv != null) {
            piName = this.m_name_atv.evaluate(xctxt, sourceNode, this);
        }
        if (piName != null) {
            if (piName.equalsIgnoreCase(SerializerConstants.XML_PREFIX)) {
                transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML, new Object[]{Keywords.FUNC_NAME_STRING, piName});
            } else if (this.m_name_atv.isSimple() || XML11Char.isXML11ValidNCName(piName)) {
                try {
                    transformer.getResultTreeHandler().processingInstruction(piName, transformer.transformToString(this));
                } catch (SAXException se) {
                    throw new TransformerException(se);
                }
            } else {
                transformer.getMsgMgr().warn(this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME, new Object[]{Keywords.FUNC_NAME_STRING, piName});
            }
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        switch (newChild.getXSLToken()) {
            case OpCodes.OP_GT /*9*/:
            case OpCodes.OP_STRING /*17*/:
            case OpCodes.OP_LOCATIONPATH /*28*/:
            case OpCodes.OP_MATCHPATTERN /*30*/:
            case OpCodes.NODETYPE_ROOT /*35*/:
            case OpCodes.NODETYPE_ANYELEMENT /*36*/:
            case OpCodes.FROM_ANCESTORS /*37*/:
            case OpCodes.FROM_DESCENDANTS_OR_SELF /*42*/:
            case OpCodes.FROM_ROOT /*50*/:
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

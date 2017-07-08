package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.compiler.OpCodes;
import org.xml.sax.SAXException;

public class ElemComment extends ElemTemplateElement {
    static final long serialVersionUID = -8813199122875770142L;

    public int getXSLToken() {
        return 59;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_COMMENT_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        try {
            transformer.getResultTreeHandler().comment(transformer.transformToString(this));
        } catch (SAXException se) {
            throw new TransformerException(se);
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

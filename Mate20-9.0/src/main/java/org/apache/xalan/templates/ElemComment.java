package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
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

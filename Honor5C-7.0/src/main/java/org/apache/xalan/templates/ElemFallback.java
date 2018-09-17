package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;

public class ElemFallback extends ElemTemplateElement {
    static final long serialVersionUID = 1782962139867340703L;

    public int getXSLToken() {
        return 57;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_FALLBACK_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
    }

    public void executeFallback(TransformerImpl transformer) throws TransformerException {
        int parentElemType = this.m_parentNode.getXSLToken();
        if (79 == parentElemType || -1 == parentElemType) {
            transformer.executeChildTemplates((ElemTemplateElement) this, true);
        } else {
            System.out.println("Error!  parent of xsl:fallback must be an extension or unknown element!");
        }
    }
}

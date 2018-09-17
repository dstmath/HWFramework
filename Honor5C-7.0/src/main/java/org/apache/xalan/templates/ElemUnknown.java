package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;

public class ElemUnknown extends ElemLiteralResult {
    static final long serialVersionUID = -4573981712648730168L;

    public int getXSLToken() {
        return -1;
    }

    private void executeFallbacks(TransformerImpl transformer) throws TransformerException {
        for (ElemTemplateElement child = this.m_firstChild; child != null; child = child.m_nextSibling) {
            if (child.getXSLToken() == 57) {
                try {
                    transformer.pushElemTemplateElement(child);
                    ((ElemFallback) child).executeFallback(transformer);
                } finally {
                    transformer.popElemTemplateElement();
                }
            }
        }
    }

    private boolean hasFallbackChildren() {
        for (ElemTemplateElement child = this.m_firstChild; child != null; child = child.m_nextSibling) {
            if (child.getXSLToken() == 57) {
                return true;
            }
        }
        return false;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        try {
            if (hasFallbackChildren()) {
                executeFallbacks(transformer);
            }
        } catch (TransformerException e) {
            transformer.getErrorListener().fatalError(e);
        }
    }
}

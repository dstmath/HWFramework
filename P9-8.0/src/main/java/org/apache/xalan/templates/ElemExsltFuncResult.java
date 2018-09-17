package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

public class ElemExsltFuncResult extends ElemVariable {
    static final long serialVersionUID = -3478311949388304563L;
    private int m_callerFrameSize = 0;
    private boolean m_isResultSet = false;
    private XObject m_result = null;

    public void execute(TransformerImpl transformer) throws TransformerException {
        XPathContext context = transformer.getXPathContext();
        if (transformer.currentFuncResultSeen()) {
            throw new TransformerException("An EXSLT function cannot set more than one result!");
        }
        XObject var = getValue(transformer, context.getCurrentNode());
        transformer.popCurrentFuncResult();
        transformer.pushCurrentFuncResult(var);
    }

    public int getXSLToken() {
        return 89;
    }

    public String getNodeName() {
        return Constants.EXSLT_ELEMNAME_FUNCRESULT_STRING;
    }
}

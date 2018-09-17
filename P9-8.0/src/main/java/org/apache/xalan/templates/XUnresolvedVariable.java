package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

public class XUnresolvedVariable extends XObject {
    static final long serialVersionUID = -256779804767950188L;
    private transient int m_context;
    private transient boolean m_doneEval = true;
    private boolean m_isGlobal;
    private transient TransformerImpl m_transformer;
    private transient int m_varStackContext;
    private transient int m_varStackPos = -1;

    public XUnresolvedVariable(ElemVariable obj, int sourceNode, TransformerImpl transformer, int varStackPos, int varStackContext, boolean isGlobal) {
        super(obj);
        this.m_context = sourceNode;
        this.m_transformer = transformer;
        this.m_varStackPos = varStackPos;
        this.m_varStackContext = varStackContext;
        this.m_isGlobal = isGlobal;
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        if (!this.m_doneEval) {
            this.m_transformer.getMsgMgr().error(xctxt.getSAXLocator(), XSLTErrorResources.ER_REFERENCING_ITSELF, new Object[]{((ElemVariable) object()).getName().getLocalName()});
        }
        VariableStack vars = xctxt.getVarStack();
        int currentFrame = vars.getStackFrame();
        ElemVariable velem = this.m_obj;
        try {
            this.m_doneEval = false;
            if (-1 != velem.m_frameSize) {
                vars.link(velem.m_frameSize);
            }
            XObject var = velem.getValue(this.m_transformer, this.m_context);
            this.m_doneEval = true;
            return var;
        } finally {
            if (-1 != velem.m_frameSize) {
                vars.unlink(currentFrame);
            }
        }
    }

    public void setVarStackPos(int top) {
        this.m_varStackPos = top;
    }

    public void setVarStackContext(int bottom) {
        this.m_varStackContext = bottom;
    }

    public int getType() {
        return XObject.CLASS_UNRESOLVEDVARIABLE;
    }

    public String getTypeString() {
        return "XUnresolvedVariable (" + object().getClass().getName() + ")";
    }
}

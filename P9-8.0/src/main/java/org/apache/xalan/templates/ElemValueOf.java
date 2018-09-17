package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

public class ElemValueOf extends ElemTemplateElement {
    static final long serialVersionUID = 3490728458007586786L;
    private boolean m_disableOutputEscaping = false;
    private boolean m_isDot = false;
    private XPath m_selectExpression = null;

    public void setSelect(XPath v) {
        if (v != null) {
            String s = v.getPatternString();
            this.m_isDot = s != null ? s.equals(Constants.ATTRVAL_THIS) : false;
        }
        this.m_selectExpression = v;
    }

    public XPath getSelect() {
        return this.m_selectExpression;
    }

    public void setDisableOutputEscaping(boolean v) {
        this.m_disableOutputEscaping = v;
    }

    public boolean getDisableOutputEscaping() {
        return this.m_disableOutputEscaping;
    }

    public int getXSLToken() {
        return 30;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
        Vector vnames = sroot.getComposeState().getVariableNames();
        if (this.m_selectExpression != null) {
            this.m_selectExpression.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
        }
    }

    public String getNodeName() {
        return Constants.ELEMNAME_VALUEOF_STRING;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        XPathContext xctxt = transformer.getXPathContext();
        SerializationHandler rth = transformer.getResultTreeHandler();
        try {
            xctxt.pushNamespaceContext(this);
            int current = xctxt.getCurrentNode();
            xctxt.pushCurrentNodeAndExpression(current, current);
            if (this.m_disableOutputEscaping) {
                rth.processingInstruction("javax.xml.transform.disable-output-escaping", "");
            }
            this.m_selectExpression.getExpression().executeCharsToContentHandler(xctxt, rth);
            if (this.m_disableOutputEscaping) {
                rth.processingInstruction("javax.xml.transform.enable-output-escaping", "");
            }
            xctxt.popNamespaceContext();
            xctxt.popCurrentNodeAndExpression();
        } catch (SAXException se) {
            throw new TransformerException(se);
        } catch (RuntimeException re) {
            TransformerException te = new TransformerException(re);
            te.setLocator(this);
            throw te;
        } catch (Throwable th) {
            if (this.m_disableOutputEscaping) {
                rth.processingInstruction("javax.xml.transform.enable-output-escaping", "");
            }
            xctxt.popNamespaceContext();
            xctxt.popCurrentNodeAndExpression();
        }
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
        return null;
    }

    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        if (callAttrs) {
            this.m_selectExpression.getExpression().callVisitors(this.m_selectExpression, visitor);
        }
        super.callChildVisitors(visitor, callAttrs);
    }
}

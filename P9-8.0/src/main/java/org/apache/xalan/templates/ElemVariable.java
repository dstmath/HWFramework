package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XRTreeFrag;
import org.apache.xpath.objects.XRTreeFragSelectWrapper;
import org.apache.xpath.objects.XString;

public class ElemVariable extends ElemTemplateElement {
    static final long serialVersionUID = 9111131075322790061L;
    int m_frameSize = -1;
    protected int m_index;
    private boolean m_isTopLevel = false;
    protected QName m_qname;
    private XPath m_selectPattern;

    public void setIndex(int index) {
        this.m_index = index;
    }

    public int getIndex() {
        return this.m_index;
    }

    public void setSelect(XPath v) {
        this.m_selectPattern = v;
    }

    public XPath getSelect() {
        return this.m_selectPattern;
    }

    public void setName(QName v) {
        this.m_qname = v;
    }

    public QName getName() {
        return this.m_qname;
    }

    public void setIsTopLevel(boolean v) {
        this.m_isTopLevel = v;
    }

    public boolean getIsTopLevel() {
        return this.m_isTopLevel;
    }

    public int getXSLToken() {
        return 73;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_VARIABLE_STRING;
    }

    public ElemVariable(ElemVariable param) throws TransformerException {
        this.m_selectPattern = param.m_selectPattern;
        this.m_qname = param.m_qname;
        this.m_isTopLevel = param.m_isTopLevel;
    }

    public void execute(TransformerImpl transformer) throws TransformerException {
        transformer.getXPathContext().getVarStack().setLocalVariable(this.m_index, getValue(transformer, transformer.getXPathContext().getCurrentNode()));
    }

    public XObject getValue(TransformerImpl transformer, int sourceNode) throws TransformerException {
        XPathContext xctxt = transformer.getXPathContext();
        xctxt.pushCurrentNode(sourceNode);
        try {
            XObject var;
            if (this.m_selectPattern != null) {
                var = this.m_selectPattern.execute(xctxt, sourceNode, (PrefixResolver) this);
                var.allowDetachToRelease(false);
            } else if (getFirstChildElem() == null) {
                var = XString.EMPTYSTRING;
            } else {
                int df;
                if (this.m_parentNode instanceof Stylesheet) {
                    df = transformer.transformToGlobalRTF(this);
                } else {
                    df = transformer.transformToRTF(this);
                }
                var = new XRTreeFrag(df, xctxt, this);
            }
            xctxt.popCurrentNode();
            return var;
        } catch (Throwable th) {
            xctxt.popCurrentNode();
        }
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        if (this.m_selectPattern == null && sroot.getOptimizer()) {
            XPath newSelect = rewriteChildToExpression(this);
            if (newSelect != null) {
                this.m_selectPattern = newSelect;
            }
        }
        ComposeState cstate = sroot.getComposeState();
        Vector vnames = cstate.getVariableNames();
        if (this.m_selectPattern != null) {
            this.m_selectPattern.fixupVariables(vnames, cstate.getGlobalsSize());
        }
        if (!(this.m_parentNode instanceof Stylesheet) && this.m_qname != null) {
            this.m_index = cstate.addVariableName(this.m_qname) - cstate.getGlobalsSize();
        } else if (this.m_parentNode instanceof Stylesheet) {
            cstate.resetStackFrameSize();
        }
        super.compose(sroot);
    }

    public void endCompose(StylesheetRoot sroot) throws TransformerException {
        super.endCompose(sroot);
        if (this.m_parentNode instanceof Stylesheet) {
            ComposeState cstate = sroot.getComposeState();
            this.m_frameSize = cstate.getFrameSize();
            cstate.resetStackFrameSize();
        }
    }

    static XPath rewriteChildToExpression(ElemTemplateElement varElem) throws TransformerException {
        ElemTemplateElement t = varElem.getFirstChildElem();
        if (t != null && t.getNextSiblingElem() == null) {
            int etype = t.getXSLToken();
            if (30 == etype) {
                ElemValueOf valueof = (ElemValueOf) t;
                if (!valueof.getDisableOutputEscaping() && valueof.getDOMBackPointer() == null) {
                    varElem.m_firstChild = null;
                    return new XPath(new XRTreeFragSelectWrapper(valueof.getSelect().getExpression()));
                }
            } else if (78 == etype) {
                ElemTextLiteral lit = (ElemTextLiteral) t;
                if (!lit.getDisableOutputEscaping() && lit.getDOMBackPointer() == null) {
                    XString xstr = new XString(lit.getNodeValue());
                    varElem.m_firstChild = null;
                    return new XPath(new XRTreeFragSelectWrapper(xstr));
                }
            }
        }
        return null;
    }

    public void recompose(StylesheetRoot root) {
        root.recomposeVariables(this);
    }

    public void setParentElem(ElemTemplateElement p) {
        super.setParentElem(p);
        p.m_hasVariableDecl = true;
    }

    protected boolean accept(XSLTVisitor visitor) {
        return visitor.visitVariableOrParamDecl(this);
    }

    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        if (this.m_selectPattern != null) {
            this.m_selectPattern.getExpression().callVisitors(this.m_selectPattern, visitor);
        }
        super.callChildVisitors(visitor, callAttrs);
    }

    public boolean isPsuedoVar() {
        String ns = this.m_qname.getNamespaceURI();
        if (ns != null && ns.equals("http://xml.apache.org/xalan/psuedovar") && this.m_qname.getLocalName().startsWith("#")) {
            return true;
        }
        return false;
    }

    public ElemTemplateElement appendChild(ElemTemplateElement elem) {
        if (this.m_selectPattern == null) {
            return super.appendChild(elem);
        }
        error(XSLTErrorResources.ER_CANT_HAVE_CONTENT_AND_SELECT, new Object[]{"xsl:" + getNodeName()});
        return null;
    }
}

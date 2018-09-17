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
import org.apache.xpath.objects.XString;

public class ElemWithParam extends ElemTemplateElement {
    static final long serialVersionUID = -1070355175864326257L;
    int m_index;
    private QName m_qname = null;
    int m_qnameID;
    private XPath m_selectPattern = null;

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

    public int getXSLToken() {
        return 2;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_WITHPARAM_STRING;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        if (this.m_selectPattern == null && sroot.getOptimizer()) {
            XPath newSelect = ElemVariable.rewriteChildToExpression(this);
            if (newSelect != null) {
                this.m_selectPattern = newSelect;
            }
        }
        this.m_qnameID = sroot.getComposeState().getQNameID(this.m_qname);
        super.compose(sroot);
        Vector vnames = sroot.getComposeState().getVariableNames();
        if (this.m_selectPattern != null) {
            this.m_selectPattern.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
        }
    }

    public void setParentElem(ElemTemplateElement p) {
        super.setParentElem(p);
        p.m_hasVariableDecl = true;
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
                var = new XRTreeFrag(transformer.transformToRTF(this), xctxt, this);
            }
            xctxt.popCurrentNode();
            return var;
        } catch (Throwable th) {
            xctxt.popCurrentNode();
        }
    }

    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        if (callAttrs && this.m_selectPattern != null) {
            this.m_selectPattern.getExpression().callVisitors(this.m_selectPattern, visitor);
        }
        super.callChildVisitors(visitor, callAttrs);
    }

    public ElemTemplateElement appendChild(ElemTemplateElement elem) {
        if (this.m_selectPattern == null) {
            return super.appendChild(elem);
        }
        error(XSLTErrorResources.ER_CANT_HAVE_CONTENT_AND_SELECT, new Object[]{"xsl:" + getNodeName()});
        return null;
    }
}

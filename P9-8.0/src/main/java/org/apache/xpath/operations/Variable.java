package org.apache.xpath.operations;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.axes.PathComponent;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class Variable extends Expression implements PathComponent {
    static final String PSUEDOVARNAMESPACE = "http://xml.apache.org/xalan/psuedovar";
    static final long serialVersionUID = -4334975375609297049L;
    private boolean m_fixUpWasCalled = false;
    protected int m_index;
    protected boolean m_isGlobal = false;
    protected QName m_qname;

    public void setIndex(int index) {
        this.m_index = index;
    }

    public int getIndex() {
        return this.m_index;
    }

    public void setIsGlobal(boolean isGlobal) {
        this.m_isGlobal = isGlobal;
    }

    public boolean getGlobal() {
        return this.m_isGlobal;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        this.m_fixUpWasCalled = true;
        int sz = vars.size();
        int i = vars.size() - 1;
        while (i >= 0) {
            if (!((QName) vars.elementAt(i)).equals(this.m_qname)) {
                i--;
            } else if (i < globalsSize) {
                this.m_isGlobal = true;
                this.m_index = i;
                return;
            } else {
                this.m_index = i - globalsSize;
                return;
            }
        }
        throw new WrappedRuntimeException(new TransformerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_COULD_NOT_FIND_VAR, new Object[]{this.m_qname.toString()}), this));
    }

    public void setQName(QName qname) {
        this.m_qname = qname;
    }

    public QName getQName() {
        return this.m_qname;
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return execute(xctxt, false);
    }

    public XObject execute(XPathContext xctxt, boolean destructiveOK) throws TransformerException {
        XObject result;
        PrefixResolver xprefixResolver = xctxt.getNamespaceContext();
        if (!this.m_fixUpWasCalled) {
            result = xctxt.getVarStack().getVariableOrParam(xctxt, this.m_qname);
        } else if (this.m_isGlobal) {
            result = xctxt.getVarStack().getGlobalVariable(xctxt, this.m_index, destructiveOK);
        } else {
            result = xctxt.getVarStack().getLocalVariable(xctxt, this.m_index, destructiveOK);
        }
        if (result != null) {
            return result;
        }
        warn(xctxt, XPATHErrorResources.WG_ILLEGAL_VARIABLE_REFERENCE, new Object[]{this.m_qname.getLocalPart()});
        return new XNodeSet(xctxt.getDTMManager());
    }

    /* JADX WARNING: Missing block: B:18:0x003b, code:
            r1 = r2.getParentElem();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ElemVariable getElemVariable() {
        ElemVariable vvar = null;
        ExpressionNode owner = getExpressionOwner();
        if (owner != null && (owner instanceof ElemTemplateElement)) {
            ElemTemplateElement prev = (ElemTemplateElement) owner;
            if (!(prev instanceof Stylesheet)) {
                while (prev != null && ((prev.getParentNode() instanceof Stylesheet) ^ 1) != 0) {
                    ElemTemplateElement savedprev = prev;
                    while (true) {
                        prev = prev.getPreviousSiblingElem();
                        if (prev == null) {
                            break;
                        } else if (prev instanceof ElemVariable) {
                            vvar = (ElemVariable) prev;
                            if (vvar.getName().equals(this.m_qname)) {
                                return vvar;
                            }
                            vvar = null;
                        }
                    }
                }
            }
            if (prev != null) {
                vvar = prev.getStylesheetRoot().getVariableOrParamComposed(this.m_qname);
            }
        }
        return vvar;
    }

    public boolean isStableNumber() {
        return true;
    }

    public int getAnalysisBits() {
        ElemVariable vvar = getElemVariable();
        if (vvar != null) {
            XPath xpath = vvar.getSelect();
            if (xpath != null) {
                Expression expr = xpath.getExpression();
                if (expr != null && (expr instanceof PathComponent)) {
                    return ((PathComponent) expr).getAnalysisBits();
                }
            }
        }
        return WalkerFactory.BIT_FILTER;
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        visitor.visitVariableRef(owner, this);
    }

    public boolean deepEquals(Expression expr) {
        if (isSameClass(expr) && this.m_qname.equals(((Variable) expr).m_qname) && getElemVariable() == ((Variable) expr).getElemVariable()) {
            return true;
        }
        return false;
    }

    public boolean isPsuedoVarRef() {
        String ns = this.m_qname.getNamespaceURI();
        if (ns != null && ns.equals(PSUEDOVARNAMESPACE) && this.m_qname.getLocalName().startsWith("#")) {
            return true;
        }
        return false;
    }
}

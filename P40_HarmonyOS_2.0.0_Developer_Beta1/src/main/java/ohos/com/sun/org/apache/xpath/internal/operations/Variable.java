package ohos.com.sun.org.apache.xpath.internal.operations;

import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.QName;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.axes.PathComponent;
import ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.javax.xml.transform.TransformerException;

public class Variable extends Expression implements PathComponent {
    static final String PSUEDOVARNAMESPACE = "http://xml.apache.org/xalan/psuedovar";
    static final long serialVersionUID = -4334975375609297049L;
    private boolean m_fixUpWasCalled = false;
    protected int m_index;
    protected boolean m_isGlobal = false;
    protected QName m_qname;

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PathComponent
    public int getAnalysisBits() {
        return 67108864;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean isStableNumber() {
        return true;
    }

    public void setIndex(int i) {
        this.m_index = i;
    }

    public int getIndex() {
        return this.m_index;
    }

    public void setIsGlobal(boolean z) {
        this.m_isGlobal = z;
    }

    public boolean getGlobal() {
        return this.m_isGlobal;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        this.m_fixUpWasCalled = true;
        vector.size();
        for (int size = vector.size() - 1; size >= 0; size--) {
            if (((QName) vector.elementAt(size)).equals(this.m_qname)) {
                if (size < i) {
                    this.m_isGlobal = true;
                    this.m_index = size;
                    return;
                } else {
                    this.m_index = size - i;
                    return;
                }
            }
        }
        throw new WrappedRuntimeException(new TransformerException(XSLMessages.createXPATHMessage("ER_COULD_NOT_FIND_VAR", new Object[]{this.m_qname.toString()}), this));
    }

    public void setQName(QName qName) {
        this.m_qname = qName;
    }

    public QName getQName() {
        return this.m_qname;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return execute(xPathContext, false);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, boolean z) throws TransformerException {
        XObject xObject;
        xPathContext.getNamespaceContext();
        if (!this.m_fixUpWasCalled) {
            xObject = xPathContext.getVarStack().getVariableOrParam(xPathContext, this.m_qname);
        } else if (this.m_isGlobal) {
            xObject = xPathContext.getVarStack().getGlobalVariable(xPathContext, this.m_index, z);
        } else {
            xObject = xPathContext.getVarStack().getLocalVariable(xPathContext, this.m_index, z);
        }
        if (xObject != null) {
            return xObject;
        }
        warn(xPathContext, "WG_ILLEGAL_VARIABLE_REFERENCE", new Object[]{this.m_qname.getLocalPart()});
        return new XNodeSet(xPathContext.getDTMManager());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        xPathVisitor.visitVariableRef(expressionOwner, this);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (isSameClass(expression) && this.m_qname.equals(((Variable) expression).m_qname)) {
            return true;
        }
        return false;
    }

    public boolean isPsuedoVarRef() {
        String namespaceURI = this.m_qname.getNamespaceURI();
        return namespaceURI != null && namespaceURI.equals(PSUEDOVARNAMESPACE) && this.m_qname.getLocalName().startsWith(DMSDPConfig.SPLIT);
    }
}

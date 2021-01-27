package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionNode;
import ohos.com.sun.org.apache.xpath.internal.NodeSetDTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.axes.RTFIterator;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.NodeList;

public class XRTreeFrag extends XObject implements Cloneable {
    static final long serialVersionUID = -3201553822254911567L;
    private DTMXRTreeFrag m_DTMXRTreeFrag;
    protected boolean m_allowRelease = false;
    private int m_dtmRoot = -1;
    private XMLString m_xmlStr = null;

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean bool() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int getType() {
        return 5;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String getTypeString() {
        return "#RTREEFRAG";
    }

    public XRTreeFrag(int i, XPathContext xPathContext, ExpressionNode expressionNode) {
        super(null);
        exprSetParent(expressionNode);
        initDTM(i, xPathContext);
    }

    public XRTreeFrag(int i, XPathContext xPathContext) {
        super(null);
        initDTM(i, xPathContext);
    }

    private final void initDTM(int i, XPathContext xPathContext) {
        this.m_dtmRoot = i;
        DTM dtm = xPathContext.getDTM(i);
        if (dtm != null) {
            this.m_DTMXRTreeFrag = xPathContext.getDTMXRTreeFrag(xPathContext.getDTMIdentity(dtm));
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public Object object() {
        if (this.m_DTMXRTreeFrag.getXPathContext() != null) {
            return new DTMNodeIterator(new NodeSetDTM(this.m_dtmRoot, this.m_DTMXRTreeFrag.getXPathContext().getDTMManager()));
        }
        return super.object();
    }

    public XRTreeFrag(Expression expression) {
        super(expression);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void allowDetachToRelease(boolean z) {
        this.m_allowRelease = z;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        if (this.m_allowRelease) {
            this.m_DTMXRTreeFrag.destruct();
            setObject(null);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double num() throws TransformerException {
        return xstr().toDouble();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public XMLString xstr() {
        if (this.m_xmlStr == null) {
            this.m_xmlStr = this.m_DTMXRTreeFrag.getDTM().getStringValue(this.m_dtmRoot);
        }
        return this.m_xmlStr;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public void appendToFsb(FastStringBuffer fastStringBuffer) {
        ((XString) xstr()).appendToFsb(fastStringBuffer);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        String xMLString = this.m_DTMXRTreeFrag.getDTM().getStringValue(this.m_dtmRoot).toString();
        return xMLString == null ? "" : xMLString;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int rtf() {
        return this.m_dtmRoot;
    }

    public DTMIterator asNodeIterator() {
        return new RTFIterator(this.m_dtmRoot, this.m_DTMXRTreeFrag.getXPathContext().getDTMManager());
    }

    public NodeList convertToNodeset() {
        if (this.m_obj instanceof NodeList) {
            return (NodeList) this.m_obj;
        }
        return new DTMNodeList(asNodeIterator());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        try {
            if (4 == xObject.getType()) {
                return xObject.equals((XObject) this);
            }
            if (1 == xObject.getType()) {
                return bool() == xObject.bool();
            }
            if (2 == xObject.getType()) {
                return num() == xObject.num();
            }
            if (4 == xObject.getType()) {
                return xstr().equals(xObject.xstr());
            }
            if (3 == xObject.getType()) {
                return xstr().equals(xObject.xstr());
            }
            if (5 == xObject.getType()) {
                return xstr().equals(xObject.xstr());
            }
            return super.equals(xObject);
        } catch (TransformerException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

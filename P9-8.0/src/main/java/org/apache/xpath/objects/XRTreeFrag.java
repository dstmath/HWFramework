package org.apache.xpath.objects;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.ref.DTMNodeIterator;
import org.apache.xml.dtm.ref.DTMNodeList;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.RTFIterator;
import org.w3c.dom.NodeList;

public class XRTreeFrag extends XObject implements Cloneable {
    static final long serialVersionUID = -3201553822254911567L;
    private DTMXRTreeFrag m_DTMXRTreeFrag;
    protected boolean m_allowRelease = false;
    private int m_dtmRoot = -1;
    private XMLString m_xmlStr = null;

    public XRTreeFrag(int root, XPathContext xctxt, ExpressionNode parent) {
        super(null);
        exprSetParent(parent);
        initDTM(root, xctxt);
    }

    public XRTreeFrag(int root, XPathContext xctxt) {
        super(null);
        initDTM(root, xctxt);
    }

    private final void initDTM(int root, XPathContext xctxt) {
        this.m_dtmRoot = root;
        DTM dtm = xctxt.getDTM(root);
        if (dtm != null) {
            this.m_DTMXRTreeFrag = xctxt.getDTMXRTreeFrag(xctxt.getDTMIdentity(dtm));
        }
    }

    public Object object() {
        if (this.m_DTMXRTreeFrag.getXPathContext() != null) {
            return new DTMNodeIterator(new NodeSetDTM(this.m_dtmRoot, this.m_DTMXRTreeFrag.getXPathContext().getDTMManager()));
        }
        return super.object();
    }

    public XRTreeFrag(Expression expr) {
        super(expr);
    }

    public void allowDetachToRelease(boolean allowRelease) {
        this.m_allowRelease = allowRelease;
    }

    public void detach() {
        if (this.m_allowRelease) {
            this.m_DTMXRTreeFrag.destruct();
            setObject(null);
        }
    }

    public int getType() {
        return 5;
    }

    public String getTypeString() {
        return "#RTREEFRAG";
    }

    public double num() throws TransformerException {
        return xstr().toDouble();
    }

    public boolean bool() {
        return true;
    }

    public XMLString xstr() {
        if (this.m_xmlStr == null) {
            this.m_xmlStr = this.m_DTMXRTreeFrag.getDTM().getStringValue(this.m_dtmRoot);
        }
        return this.m_xmlStr;
    }

    public void appendToFsb(FastStringBuffer fsb) {
        ((XString) xstr()).appendToFsb(fsb);
    }

    public String str() {
        String str = this.m_DTMXRTreeFrag.getDTM().getStringValue(this.m_dtmRoot).toString();
        return str == null ? "" : str;
    }

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

    public boolean equals(XObject obj2) {
        boolean z = true;
        try {
            if (4 == obj2.getType()) {
                return obj2.equals(this);
            }
            if (1 == obj2.getType()) {
                if (bool() != obj2.bool()) {
                    z = false;
                }
                return z;
            } else if (2 == obj2.getType()) {
                if (num() != obj2.num()) {
                    z = false;
                }
                return z;
            } else if (4 == obj2.getType()) {
                return xstr().equals(obj2.xstr());
            } else {
                if (3 == obj2.getType()) {
                    return xstr().equals(obj2.xstr());
                }
                if (5 == obj2.getType()) {
                    return xstr().equals(obj2.xstr());
                }
                return super.equals(obj2);
            }
        } catch (TransformerException te) {
            throw new WrappedRuntimeException(te);
        }
    }
}

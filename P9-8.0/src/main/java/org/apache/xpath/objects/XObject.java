package org.apache.xpath.objects;

import java.io.Serializable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathException;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XObject extends Expression implements Serializable, Cloneable {
    public static final int CLASS_BOOLEAN = 1;
    public static final int CLASS_NODESET = 4;
    public static final int CLASS_NULL = -1;
    public static final int CLASS_NUMBER = 2;
    public static final int CLASS_RTREEFRAG = 5;
    public static final int CLASS_STRING = 3;
    public static final int CLASS_UNKNOWN = 0;
    public static final int CLASS_UNRESOLVEDVARIABLE = 600;
    static final long serialVersionUID = -821887098985662951L;
    protected Object m_obj;

    public XObject(Object obj) {
        setObject(obj);
    }

    protected void setObject(Object obj) {
        this.m_obj = obj;
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return this;
    }

    public void allowDetachToRelease(boolean allowRelease) {
    }

    public void detach() {
    }

    public void destruct() {
        if (this.m_obj != null) {
            allowDetachToRelease(true);
            detach();
            setObject(null);
        }
    }

    public void reset() {
    }

    public void dispatchCharactersEvents(ContentHandler ch) throws SAXException {
        xstr().dispatchCharactersEvents(ch);
    }

    public static XObject create(Object val) {
        return XObjectFactory.create(val);
    }

    public static XObject create(Object val, XPathContext xctxt) {
        return XObjectFactory.create(val, xctxt);
    }

    public int getType() {
        return 0;
    }

    public String getTypeString() {
        return "#UNKNOWN (" + object().getClass().getName() + ")";
    }

    public double num() throws TransformerException {
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NUMBER, new Object[]{getTypeString()});
        return XPath.MATCH_SCORE_QNAME;
    }

    public double numWithSideEffects() throws TransformerException {
        return num();
    }

    public boolean bool() throws TransformerException {
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NUMBER, new Object[]{getTypeString()});
        return false;
    }

    public boolean boolWithSideEffects() throws TransformerException {
        return bool();
    }

    public XMLString xstr() {
        return XMLStringFactoryImpl.getFactory().newstr(str());
    }

    public String str() {
        return this.m_obj != null ? this.m_obj.toString() : "";
    }

    public String toString() {
        return str();
    }

    public int rtf(XPathContext support) {
        int result = rtf();
        if (-1 != result) {
            return result;
        }
        DTM frag = support.createDocumentFragment();
        frag.appendTextChild(str());
        return frag.getDocument();
    }

    public DocumentFragment rtree(XPathContext support) {
        int result = rtf();
        DTM frag;
        if (-1 == result) {
            frag = support.createDocumentFragment();
            frag.appendTextChild(str());
            return (DocumentFragment) frag.getNode(frag.getDocument());
        }
        frag = support.getDTM(result);
        return (DocumentFragment) frag.getNode(frag.getDocument());
    }

    public DocumentFragment rtree() {
        return null;
    }

    public int rtf() {
        return -1;
    }

    public Object object() {
        return this.m_obj;
    }

    public DTMIterator iter() throws TransformerException {
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NODELIST, new Object[]{getTypeString()});
        return null;
    }

    public XObject getFresh() {
        return this;
    }

    public NodeIterator nodeset() throws TransformerException {
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NODELIST, new Object[]{getTypeString()});
        return null;
    }

    public NodeList nodelist() throws TransformerException {
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_NODELIST, new Object[]{getTypeString()});
        return null;
    }

    public NodeSetDTM mutableNodeset() throws TransformerException {
        error(XPATHErrorResources.ER_CANT_CONVERT_TO_MUTABLENODELIST, new Object[]{getTypeString()});
        return (NodeSetDTM) this.m_obj;
    }

    public Object castToType(int t, XPathContext support) throws TransformerException {
        switch (t) {
            case 0:
                return this.m_obj;
            case 1:
                return new Boolean(bool());
            case 2:
                return new Double(num());
            case 3:
                return str();
            case 4:
                return iter();
            default:
                error(XPATHErrorResources.ER_CANT_CONVERT_TO_TYPE, new Object[]{getTypeString(), Integer.toString(t)});
                return null;
        }
    }

    public boolean lessThan(XObject obj2) throws TransformerException {
        if (obj2.getType() == 4) {
            return obj2.greaterThan(this);
        }
        return num() < obj2.num();
    }

    public boolean lessThanOrEqual(XObject obj2) throws TransformerException {
        if (obj2.getType() == 4) {
            return obj2.greaterThanOrEqual(this);
        }
        return num() <= obj2.num();
    }

    public boolean greaterThan(XObject obj2) throws TransformerException {
        if (obj2.getType() == 4) {
            return obj2.lessThan(this);
        }
        return num() > obj2.num();
    }

    public boolean greaterThanOrEqual(XObject obj2) throws TransformerException {
        if (obj2.getType() == 4) {
            return obj2.lessThanOrEqual(this);
        }
        return num() >= obj2.num();
    }

    public boolean equals(XObject obj2) {
        if (obj2.getType() == 4) {
            return obj2.equals(this);
        }
        if (this.m_obj != null) {
            return this.m_obj.equals(obj2.m_obj);
        }
        return obj2.m_obj == null;
    }

    public boolean notEquals(XObject obj2) throws TransformerException {
        if (obj2.getType() == 4) {
            return obj2.notEquals(this);
        }
        return equals(obj2) ^ 1;
    }

    protected void error(String msg) throws TransformerException {
        error(msg, null);
    }

    protected void error(String msg, Object[] args) throws TransformerException {
        throw new XPathException(XPATHMessages.createXPATHMessage(msg, args), (ExpressionNode) this);
    }

    public void fixupVariables(Vector vars, int globalsSize) {
    }

    public void appendToFsb(FastStringBuffer fsb) {
        fsb.append(str());
    }

    public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
        assertion(false, "callVisitors should not be called for this object!!!");
    }

    public boolean deepEquals(Expression expr) {
        if (isSameClass(expr) && equals((XObject) expr)) {
            return true;
        }
        return false;
    }
}

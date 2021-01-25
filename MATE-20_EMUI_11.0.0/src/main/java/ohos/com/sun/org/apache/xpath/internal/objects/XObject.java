package ohos.com.sun.org.apache.xpath.internal.objects;

import java.io.Serializable;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionNode;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.NodeSetDTM;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathException;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.traversal.NodeIterator;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

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

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void allowDetachToRelease(boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
    }

    public XObject getFresh() {
        return this;
    }

    public int getType() {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void reset() {
    }

    public int rtf() {
        return -1;
    }

    public DocumentFragment rtree() {
        return null;
    }

    public XObject() {
    }

    public XObject(Object obj) {
        setObject(obj);
    }

    /* access modifiers changed from: protected */
    public void setObject(Object obj) {
        this.m_obj = obj;
    }

    public void destruct() {
        if (this.m_obj != null) {
            allowDetachToRelease(true);
            detach();
            setObject(null);
        }
    }

    public void dispatchCharactersEvents(ContentHandler contentHandler) throws SAXException {
        xstr().dispatchCharactersEvents(contentHandler);
    }

    public static XObject create(Object obj) {
        return XObjectFactory.create(obj);
    }

    public static XObject create(Object obj, XPathContext xPathContext) {
        return XObjectFactory.create(obj, xPathContext);
    }

    public String getTypeString() {
        return "#UNKNOWN (" + object().getClass().getName() + ")";
    }

    public double num() throws TransformerException {
        error("ER_CANT_CONVERT_TO_NUMBER", new Object[]{getTypeString()});
        return XPath.MATCH_SCORE_QNAME;
    }

    public double numWithSideEffects() throws TransformerException {
        return num();
    }

    public boolean bool() throws TransformerException {
        error("ER_CANT_CONVERT_TO_NUMBER", new Object[]{getTypeString()});
        return false;
    }

    public boolean boolWithSideEffects() throws TransformerException {
        return bool();
    }

    public XMLString xstr() {
        return XMLStringFactoryImpl.getFactory().newstr(str());
    }

    public String str() {
        Object obj = this.m_obj;
        return obj != null ? obj.toString() : "";
    }

    @Override // java.lang.Object
    public String toString() {
        return str();
    }

    public int rtf(XPathContext xPathContext) {
        int rtf = rtf();
        if (-1 != rtf) {
            return rtf;
        }
        DTM createDocumentFragment = xPathContext.createDocumentFragment();
        createDocumentFragment.appendTextChild(str());
        return createDocumentFragment.getDocument();
    }

    public DocumentFragment rtree(XPathContext xPathContext) {
        int rtf = rtf();
        if (-1 == rtf) {
            DTM createDocumentFragment = xPathContext.createDocumentFragment();
            createDocumentFragment.appendTextChild(str());
            return createDocumentFragment.getNode(createDocumentFragment.getDocument());
        }
        DTM dtm = xPathContext.getDTM(rtf);
        return dtm.getNode(dtm.getDocument());
    }

    public Object object() {
        return this.m_obj;
    }

    public DTMIterator iter() throws TransformerException {
        error("ER_CANT_CONVERT_TO_NODELIST", new Object[]{getTypeString()});
        return null;
    }

    public NodeIterator nodeset() throws TransformerException {
        error("ER_CANT_CONVERT_TO_NODELIST", new Object[]{getTypeString()});
        return null;
    }

    public NodeList nodelist() throws TransformerException {
        error("ER_CANT_CONVERT_TO_NODELIST", new Object[]{getTypeString()});
        return null;
    }

    public NodeSetDTM mutableNodeset() throws TransformerException {
        error("ER_CANT_CONVERT_TO_MUTABLENODELIST", new Object[]{getTypeString()});
        return (NodeSetDTM) this.m_obj;
    }

    public Object castToType(int i, XPathContext xPathContext) throws TransformerException {
        Object obj;
        if (i == 0) {
            return this.m_obj;
        }
        if (i == 1) {
            obj = new Boolean(bool());
        } else if (i == 2) {
            obj = new Double(num());
        } else if (i == 3) {
            return str();
        } else {
            if (i == 4) {
                return iter();
            }
            error("ER_CANT_CONVERT_TO_TYPE", new Object[]{getTypeString(), Integer.toString(i)});
            return null;
        }
        return obj;
    }

    public boolean lessThan(XObject xObject) throws TransformerException {
        if (xObject.getType() == 4) {
            return xObject.greaterThan(this);
        }
        return num() < xObject.num();
    }

    public boolean lessThanOrEqual(XObject xObject) throws TransformerException {
        if (xObject.getType() == 4) {
            return xObject.greaterThanOrEqual(this);
        }
        return num() <= xObject.num();
    }

    public boolean greaterThan(XObject xObject) throws TransformerException {
        if (xObject.getType() == 4) {
            return xObject.lessThan(this);
        }
        return num() > xObject.num();
    }

    public boolean greaterThanOrEqual(XObject xObject) throws TransformerException {
        if (xObject.getType() == 4) {
            return xObject.lessThanOrEqual(this);
        }
        return num() >= xObject.num();
    }

    public boolean equals(XObject xObject) {
        if (xObject.getType() == 4) {
            return xObject.equals(this);
        }
        Object obj = this.m_obj;
        if (obj != null) {
            return obj.equals(xObject.m_obj);
        }
        return xObject.m_obj == null;
    }

    public boolean notEquals(XObject xObject) throws TransformerException {
        if (xObject.getType() == 4) {
            return xObject.notEquals(this);
        }
        return !equals(xObject);
    }

    /* access modifiers changed from: protected */
    public void error(String str) throws TransformerException {
        error(str, null);
    }

    /* JADX WARN: Type inference failed for: r2v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xpath.internal.XPathException] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void error(String str, Object[] objArr) throws TransformerException {
        throw new XPathException(XSLMessages.createXPATHMessage(str, objArr), (ExpressionNode) this);
    }

    public void appendToFsb(FastStringBuffer fastStringBuffer) {
        fastStringBuffer.append(str());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        assertion(false, "callVisitors should not be called for this object!!!");
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (isSameClass(expression) && equals((XObject) expression)) {
            return true;
        }
        return false;
    }
}

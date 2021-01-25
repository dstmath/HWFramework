package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.javax.xml.transform.TransformerException;

public class XBoolean extends XObject {
    public static final XBoolean S_FALSE = new XBooleanStatic(false);
    public static final XBoolean S_TRUE = new XBooleanStatic(true);
    static final long serialVersionUID = -2964933058866100881L;
    private final boolean m_val;

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int getType() {
        return 1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String getTypeString() {
        return "#BOOLEAN";
    }

    public XBoolean(boolean z) {
        this.m_val = z;
    }

    public XBoolean(Boolean bool) {
        this.m_val = bool.booleanValue();
        setObject(bool);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double num() {
        if (this.m_val) {
            return 1.0d;
        }
        return XPath.MATCH_SCORE_QNAME;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean bool() {
        return this.m_val;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        return this.m_val ? "true" : "false";
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public Object object() {
        if (this.m_obj == null) {
            setObject(new Boolean(this.m_val));
        }
        return this.m_obj;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        if (xObject.getType() == 4) {
            return xObject.equals((XObject) this);
        }
        try {
            return this.m_val == xObject.bool();
        } catch (TransformerException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

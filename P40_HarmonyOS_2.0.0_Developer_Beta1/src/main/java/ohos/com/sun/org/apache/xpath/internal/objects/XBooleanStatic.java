package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.javax.xml.transform.TransformerException;

public class XBooleanStatic extends XBoolean {
    static final long serialVersionUID = -8064147275772687409L;
    private final boolean m_val;

    public XBooleanStatic(boolean z) {
        super(z);
        this.m_val = z;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XBoolean, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        try {
            return this.m_val == xObject.bool();
        } catch (TransformerException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

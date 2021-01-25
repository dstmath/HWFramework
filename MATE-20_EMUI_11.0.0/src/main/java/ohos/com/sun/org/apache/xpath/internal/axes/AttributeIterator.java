package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.javax.xml.transform.TransformerException;

public class AttributeIterator extends ChildTestIterator {
    static final long serialVersionUID = -8417986700712229686L;

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        return 2;
    }

    AttributeIterator(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.BasicTestIterator
    public int getNextNode() {
        int i;
        if (-1 == this.m_lastFetched) {
            i = this.m_cdtm.getFirstAttribute(this.m_context);
        } else {
            i = this.m_cdtm.getNextAttribute(this.m_lastFetched);
        }
        this.m_lastFetched = i;
        return this.m_lastFetched;
    }
}

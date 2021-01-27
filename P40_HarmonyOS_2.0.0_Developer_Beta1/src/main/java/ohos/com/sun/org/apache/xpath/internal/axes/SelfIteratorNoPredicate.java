package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.javax.xml.transform.TransformerException;

public class SelfIteratorNoPredicate extends LocPathIterator {
    static final long serialVersionUID = -4226887905279814201L;

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public int getLastPos(XPathContext xPathContext) {
        return 1;
    }

    SelfIteratorNoPredicate(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2, false);
    }

    public SelfIteratorNoPredicate() throws TransformerException {
        super(null);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        DTM dtm = this.m_cdtm;
        int i = -1 == this.m_lastFetched ? this.m_context : -1;
        this.m_lastFetched = i;
        if (-1 != i) {
            this.m_pos++;
            return i;
        }
        this.m_foundLast = true;
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xpath.internal.Expression
    public int asNode(XPathContext xPathContext) throws TransformerException {
        return xPathContext.getCurrentNode();
    }
}

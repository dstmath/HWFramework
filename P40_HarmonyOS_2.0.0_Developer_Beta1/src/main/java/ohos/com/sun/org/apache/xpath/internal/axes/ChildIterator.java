package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.javax.xml.transform.TransformerException;

public class ChildIterator extends LocPathIterator {
    static final long serialVersionUID = -6935428015142993583L;

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        return 3;
    }

    ChildIterator(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2, false);
        initNodeTest(-1);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xpath.internal.Expression
    public int asNode(XPathContext xPathContext) throws TransformerException {
        int currentNode = xPathContext.getCurrentNode();
        return xPathContext.getDTM(currentNode).getFirstChild(currentNode);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int nextNode() {
        int i;
        if (this.m_foundLast) {
            return -1;
        }
        if (-1 == this.m_lastFetched) {
            i = this.m_cdtm.getFirstChild(this.m_context);
        } else {
            i = this.m_cdtm.getNextSibling(this.m_lastFetched);
        }
        this.m_lastFetched = i;
        if (-1 != i) {
            this.m_pos++;
            return i;
        }
        this.m_foundLast = true;
        return -1;
    }
}

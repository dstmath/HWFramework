package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.javax.xml.transform.TransformerException;

public class ChildTestIterator extends BasicTestIterator {
    static final long serialVersionUID = -7936835957960705722L;
    protected transient DTMAxisTraverser m_traverser;

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        return 3;
    }

    ChildTestIterator(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2);
    }

    public ChildTestIterator(DTMAxisTraverser dTMAxisTraverser) {
        super(null);
        this.m_traverser = dTMAxisTraverser;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.BasicTestIterator
    public int getNextNode() {
        int i;
        if (-1 == this.m_lastFetched) {
            i = this.m_traverser.first(this.m_context);
        } else {
            i = this.m_traverser.next(this.m_context, this.m_lastFetched);
        }
        this.m_lastFetched = i;
        return this.m_lastFetched;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.BasicTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        ChildTestIterator childTestIterator = (ChildTestIterator) super.cloneWithReset();
        childTestIterator.m_traverser = this.m_traverser;
        return childTestIterator;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        this.m_traverser = this.m_cdtm.getAxisTraverser(3);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        if (this.m_allowDetach) {
            this.m_traverser = null;
            super.detach();
        }
    }
}

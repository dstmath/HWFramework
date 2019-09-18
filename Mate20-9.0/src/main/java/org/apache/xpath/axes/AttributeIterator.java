package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xpath.compiler.Compiler;

public class AttributeIterator extends ChildTestIterator {
    static final long serialVersionUID = -8417986700712229686L;

    AttributeIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis);
    }

    /* access modifiers changed from: protected */
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

    public int getAxis() {
        return 2;
    }
}

package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xpath.compiler.Compiler;

public class AttributeIterator extends ChildTestIterator {
    static final long serialVersionUID = -8417986700712229686L;

    AttributeIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis);
    }

    protected int getNextNode() {
        int firstAttribute;
        if (-1 == this.m_lastFetched) {
            firstAttribute = this.m_cdtm.getFirstAttribute(this.m_context);
        } else {
            firstAttribute = this.m_cdtm.getNextAttribute(this.m_lastFetched);
        }
        this.m_lastFetched = firstAttribute;
        return this.m_lastFetched;
    }

    public int getAxis() {
        return 2;
    }
}

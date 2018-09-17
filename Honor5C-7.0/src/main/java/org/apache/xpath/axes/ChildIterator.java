package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.Compiler;

public class ChildIterator extends LocPathIterator {
    static final long serialVersionUID = -6935428015142993583L;

    ChildIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis, false);
        initNodeTest(-1);
    }

    public int asNode(XPathContext xctxt) throws TransformerException {
        int current = xctxt.getCurrentNode();
        return xctxt.getDTM(current).getFirstChild(current);
    }

    public int nextNode() {
        if (this.m_foundLast) {
            return -1;
        }
        int next;
        if (-1 == this.m_lastFetched) {
            next = this.m_cdtm.getFirstChild(this.m_context);
        } else {
            next = this.m_cdtm.getNextSibling(this.m_lastFetched);
        }
        this.m_lastFetched = next;
        if (-1 != next) {
            this.m_pos++;
            return next;
        }
        this.m_foundLast = true;
        return -1;
    }

    public int getAxis() {
        return 3;
    }
}

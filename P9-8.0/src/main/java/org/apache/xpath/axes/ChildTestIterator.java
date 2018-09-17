package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.compiler.Compiler;

public class ChildTestIterator extends BasicTestIterator {
    static final long serialVersionUID = -7936835957960705722L;
    protected transient DTMAxisTraverser m_traverser;

    ChildTestIterator(Compiler compiler, int opPos, int analysis) throws TransformerException {
        super(compiler, opPos, analysis);
    }

    public ChildTestIterator(DTMAxisTraverser traverser) {
        super(null);
        this.m_traverser = traverser;
    }

    protected int getNextNode() {
        int first;
        if (-1 == this.m_lastFetched) {
            first = this.m_traverser.first(this.m_context);
        } else {
            first = this.m_traverser.next(this.m_context, this.m_lastFetched);
        }
        this.m_lastFetched = first;
        return this.m_lastFetched;
    }

    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        ChildTestIterator clone = (ChildTestIterator) super.cloneWithReset();
        clone.m_traverser = this.m_traverser;
        return clone;
    }

    public void setRoot(int context, Object environment) {
        super.setRoot(context, environment);
        this.m_traverser = this.m_cdtm.getAxisTraverser(3);
    }

    public int getAxis() {
        return 3;
    }

    public void detach() {
        if (this.m_allowDetach) {
            this.m_traverser = null;
            super.detach();
        }
    }
}

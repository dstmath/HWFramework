package org.apache.xpath.axes;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.compiler.Compiler;

public class WalkingIteratorSorted extends WalkingIterator {
    static final long serialVersionUID = -4512512007542368213L;
    protected boolean m_inNaturalOrderStatic = false;

    public WalkingIteratorSorted(PrefixResolver nscontext) {
        super(nscontext);
    }

    WalkingIteratorSorted(Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers) throws TransformerException {
        super(compiler, opPos, analysis, shouldLoadWalkers);
    }

    public boolean isDocOrdered() {
        return this.m_inNaturalOrderStatic;
    }

    boolean canBeWalkedInNaturalDocOrderStatic() {
        if (this.m_firstWalker == null) {
            return false;
        }
        AxesWalker walker = this.m_firstWalker;
        int i = 0;
        while (walker != null) {
            int axis = walker.getAxis();
            if (!walker.isDocOrdered()) {
                return false;
            }
            boolean isSimpleDownAxis = (axis == 3 || axis == 13) ? true : axis == 19;
            if (isSimpleDownAxis || axis == -1) {
                walker = walker.getNextWalker();
                i++;
            } else {
                return (walker.getNextWalker() == null) && ((walker.isDocOrdered() && (axis == 4 || axis == 5 || axis == 17 || axis == 18)) || axis == 2);
            }
        }
        return true;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
        if (WalkerFactory.isNaturalDocOrder(getAnalysisBits())) {
            this.m_inNaturalOrderStatic = true;
        } else {
            this.m_inNaturalOrderStatic = false;
        }
    }
}

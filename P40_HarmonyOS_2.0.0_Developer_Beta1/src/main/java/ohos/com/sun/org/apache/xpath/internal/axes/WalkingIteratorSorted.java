package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.javax.xml.transform.TransformerException;

public class WalkingIteratorSorted extends WalkingIterator {
    static final long serialVersionUID = -4512512007542368213L;
    protected boolean m_inNaturalOrderStatic = false;

    public WalkingIteratorSorted(PrefixResolver prefixResolver) {
        super(prefixResolver);
    }

    WalkingIteratorSorted(Compiler compiler, int i, int i2, boolean z) throws TransformerException {
        super(compiler, i, i2, z);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public boolean isDocOrdered() {
        return this.m_inNaturalOrderStatic;
    }

    /* access modifiers changed from: package-private */
    public boolean canBeWalkedInNaturalDocOrderStatic() {
        if (this.m_firstWalker == null) {
            return false;
        }
        for (AxesWalker axesWalker = this.m_firstWalker; axesWalker != null; axesWalker = axesWalker.getNextWalker()) {
            int axis = axesWalker.getAxis();
            if (!axesWalker.isDocOrdered()) {
                return false;
            }
            if (!(axis == 3 || axis == 13 || axis == 19) && axis != -1) {
                if (!(axesWalker.getNextWalker() == null) || ((!axesWalker.isDocOrdered() || !(axis == 4 || axis == 5 || axis == 17 || axis == 18)) && axis != 2)) {
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.WalkingIterator, ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        if (WalkerFactory.isNaturalDocOrder(getAnalysisBits())) {
            this.m_inNaturalOrderStatic = true;
        } else {
            this.m_inNaturalOrderStatic = false;
        }
    }
}

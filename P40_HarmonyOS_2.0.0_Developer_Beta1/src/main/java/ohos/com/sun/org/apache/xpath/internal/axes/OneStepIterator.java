package ohos.com.sun.org.apache.xpath.internal.axes;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.OpMap;
import ohos.javax.xml.transform.TransformerException;

public class OneStepIterator extends ChildTestIterator {
    static final long serialVersionUID = 4623710779664998283L;
    protected int m_axis = -1;
    protected DTMAxisIterator m_iterator;

    OneStepIterator(Compiler compiler, int i, int i2) throws TransformerException {
        super(compiler, i, i2);
        this.m_axis = WalkerFactory.getAxisFromStep(compiler, OpMap.getFirstChildPos(i));
    }

    public OneStepIterator(DTMAxisIterator dTMAxisIterator, int i) throws TransformerException {
        super(null);
        this.m_iterator = dTMAxisIterator;
        this.m_axis = i;
        initNodeTest(-1);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void setRoot(int i, Object obj) {
        super.setRoot(i, obj);
        if (this.m_axis > -1) {
            this.m_iterator = this.m_cdtm.getAxisIterator(this.m_axis);
        }
        this.m_iterator.setStartNode(this.m_context);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void detach() {
        if (this.m_allowDetach) {
            if (this.m_axis > -1) {
                this.m_iterator = null;
            }
            super.detach();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.BasicTestIterator
    public int getNextNode() {
        int next = this.m_iterator.next();
        this.m_lastFetched = next;
        return next;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        OneStepIterator oneStepIterator = (OneStepIterator) super.clone();
        DTMAxisIterator dTMAxisIterator = this.m_iterator;
        if (dTMAxisIterator != null) {
            oneStepIterator.m_iterator = dTMAxisIterator.cloneIterator();
        }
        return oneStepIterator;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.BasicTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public DTMIterator cloneWithReset() throws CloneNotSupportedException {
        OneStepIterator oneStepIterator = (OneStepIterator) super.cloneWithReset();
        oneStepIterator.m_iterator = this.m_iterator;
        return oneStepIterator;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public boolean isReverseAxes() {
        return this.m_iterator.isReverse();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public int getProximityPosition(int i) {
        if (!isReverseAxes()) {
            return super.getProximityPosition(i);
        }
        if (i < 0) {
            return -1;
        }
        if (this.m_proximityPositions[i] <= 0) {
            XPathContext xPathContext = getXPathContext();
            try {
                OneStepIterator oneStepIterator = (OneStepIterator) clone();
                int root = getRoot();
                xPathContext.pushCurrentNode(root);
                oneStepIterator.setRoot(root, xPathContext);
                oneStepIterator.m_predCount = i;
                int i2 = 1;
                while (-1 != oneStepIterator.nextNode()) {
                    i2++;
                }
                int[] iArr = this.m_proximityPositions;
                iArr[i] = iArr[i] + i2;
            } catch (CloneNotSupportedException unused) {
            } catch (Throwable th) {
                xPathContext.popCurrentNode();
                throw th;
            }
            xPathContext.popCurrentNode();
        }
        return this.m_proximityPositions[i];
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getLength() {
        if (!isReverseAxes()) {
            return super.getLength();
        }
        int i = 0;
        boolean z = this == this.m_execContext.getSubContextList();
        getPredicateCount();
        if (-1 != this.m_length && z && this.m_predicateIndex < 1) {
            return this.m_length;
        }
        XPathContext xPathContext = getXPathContext();
        try {
            OneStepIterator oneStepIterator = (OneStepIterator) cloneWithReset();
            int root = getRoot();
            xPathContext.pushCurrentNode(root);
            oneStepIterator.setRoot(root, xPathContext);
            oneStepIterator.m_predCount = this.m_predicateIndex;
            while (-1 != oneStepIterator.nextNode()) {
                i++;
            }
        } catch (CloneNotSupportedException unused) {
        } catch (Throwable th) {
            xPathContext.popCurrentNode();
            throw th;
        }
        xPathContext.popCurrentNode();
        if (z && this.m_predicateIndex < 1) {
            this.m_length = i;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public void countProximityPosition(int i) {
        if (!isReverseAxes()) {
            super.countProximityPosition(i);
        } else if (i < this.m_proximityPositions.length) {
            int[] iArr = this.m_proximityPositions;
            iArr[i] = iArr[i] - 1;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public void reset() {
        super.reset();
        DTMAxisIterator dTMAxisIterator = this.m_iterator;
        if (dTMAxisIterator != null) {
            dTMAxisIterator.reset();
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.ChildTestIterator, ohos.com.sun.org.apache.xpath.internal.axes.LocPathIterator, ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator
    public int getAxis() {
        return this.m_axis;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (super.deepEquals(expression) && this.m_axis == ((OneStepIterator) expression).m_axis) {
            return true;
        }
        return false;
    }
}

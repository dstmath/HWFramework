package ohos.com.sun.org.apache.xpath.internal.axes;

import java.util.Vector;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest;
import ohos.javax.xml.transform.TransformerException;

public class UnionChildIterator extends ChildTestIterator {
    static final long serialVersionUID = 3500298482193003495L;
    private PredicatedNodeTest[] m_nodeTests = null;

    public UnionChildIterator() {
        super(null);
    }

    public void addNodeTest(PredicatedNodeTest predicatedNodeTest) {
        PredicatedNodeTest[] predicatedNodeTestArr = this.m_nodeTests;
        if (predicatedNodeTestArr == null) {
            this.m_nodeTests = new PredicatedNodeTest[1];
            this.m_nodeTests[0] = predicatedNodeTest;
        } else {
            int length = predicatedNodeTestArr.length;
            this.m_nodeTests = new PredicatedNodeTest[(length + 1)];
            System.arraycopy(predicatedNodeTestArr, 0, this.m_nodeTests, 0, length);
            this.m_nodeTests[length] = predicatedNodeTest;
        }
        predicatedNodeTest.exprSetParent(this);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest, ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        if (this.m_nodeTests != null) {
            int i2 = 0;
            while (true) {
                PredicatedNodeTest[] predicatedNodeTestArr = this.m_nodeTests;
                if (i2 < predicatedNodeTestArr.length) {
                    predicatedNodeTestArr[i2].fixupVariables(vector, i);
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.PredicatedNodeTest
    public short acceptNode(int i) {
        XPathContext xPathContext = getXPathContext();
        try {
            xPathContext.pushCurrentNode(i);
            for (int i2 = 0; i2 < this.m_nodeTests.length; i2++) {
                PredicatedNodeTest predicatedNodeTest = this.m_nodeTests[i2];
                if (predicatedNodeTest.execute(xPathContext, i) != NodeTest.SCORE_NONE) {
                    if (predicatedNodeTest.getPredicateCount() <= 0) {
                        xPathContext.popCurrentNode();
                        return 1;
                    } else if (predicatedNodeTest.executePredicates(i, xPathContext)) {
                        xPathContext.popCurrentNode();
                        return 1;
                    }
                }
            }
            xPathContext.popCurrentNode();
            return 3;
        } catch (TransformerException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Throwable th) {
            xPathContext.popCurrentNode();
            throw th;
        }
    }
}

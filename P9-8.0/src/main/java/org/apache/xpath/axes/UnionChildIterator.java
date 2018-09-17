package org.apache.xpath.axes;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathContext;
import org.apache.xpath.patterns.NodeTest;

public class UnionChildIterator extends ChildTestIterator {
    static final long serialVersionUID = 3500298482193003495L;
    private PredicatedNodeTest[] m_nodeTests = null;

    public UnionChildIterator() {
        super(null);
    }

    public void addNodeTest(PredicatedNodeTest test) {
        if (this.m_nodeTests == null) {
            this.m_nodeTests = new PredicatedNodeTest[1];
            this.m_nodeTests[0] = test;
        } else {
            PredicatedNodeTest[] tests = this.m_nodeTests;
            int len = this.m_nodeTests.length;
            this.m_nodeTests = new PredicatedNodeTest[(len + 1)];
            System.arraycopy(tests, 0, this.m_nodeTests, 0, len);
            this.m_nodeTests[len] = test;
        }
        test.exprSetParent(this);
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        super.fixupVariables(vars, globalsSize);
        if (this.m_nodeTests != null) {
            for (PredicatedNodeTest fixupVariables : this.m_nodeTests) {
                fixupVariables.fixupVariables(vars, globalsSize);
            }
        }
    }

    public short acceptNode(int n) {
        XPathContext xctxt = getXPathContext();
        try {
            xctxt.pushCurrentNode(n);
            for (PredicatedNodeTest pnt : this.m_nodeTests) {
                if (pnt.execute(xctxt, n) != NodeTest.SCORE_NONE) {
                    if (pnt.getPredicateCount() <= 0) {
                        xctxt.popCurrentNode();
                        return (short) 1;
                    } else if (pnt.executePredicates(n, xctxt)) {
                        xctxt.popCurrentNode();
                        return (short) 1;
                    }
                }
            }
            xctxt.popCurrentNode();
            return (short) 3;
        } catch (TransformerException se) {
            throw new RuntimeException(se.getMessage());
        } catch (Throwable th) {
            xctxt.popCurrentNode();
        }
    }
}

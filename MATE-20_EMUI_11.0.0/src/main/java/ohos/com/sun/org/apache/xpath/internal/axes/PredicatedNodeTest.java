package ohos.com.sun.org.apache.xpath.internal.axes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest;
import ohos.javax.xml.transform.TransformerException;

public abstract class PredicatedNodeTest extends NodeTest implements SubContextList {
    static final boolean DEBUG_PREDICATECOUNTING = false;
    static final long serialVersionUID = -6193530757296377351L;
    protected transient boolean m_foundLast = false;
    protected LocPathIterator m_lpi;
    protected int m_predCount = -1;
    transient int m_predicateIndex = -1;
    private Expression[] m_predicates;
    protected transient int[] m_proximityPositions;

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public abstract int getLastPos(XPathContext xPathContext);

    public boolean isReverseAxes() {
        return false;
    }

    PredicatedNodeTest(LocPathIterator locPathIterator) {
        this.m_lpi = locPathIterator;
    }

    PredicatedNodeTest() {
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, TransformerException {
        try {
            objectInputStream.defaultReadObject();
            this.m_predicateIndex = -1;
            this.m_predCount = -1;
            resetProximityPositions();
        } catch (ClassNotFoundException e) {
            throw new TransformerException(e);
        }
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        PredicatedNodeTest predicatedNodeTest = (PredicatedNodeTest) super.clone();
        int[] iArr = this.m_proximityPositions;
        if (iArr != null && iArr == predicatedNodeTest.m_proximityPositions) {
            predicatedNodeTest.m_proximityPositions = new int[iArr.length];
            int[] iArr2 = this.m_proximityPositions;
            System.arraycopy(iArr2, 0, predicatedNodeTest.m_proximityPositions, 0, iArr2.length);
        }
        if (predicatedNodeTest.m_lpi == this) {
            predicatedNodeTest.m_lpi = (LocPathIterator) predicatedNodeTest;
        }
        return predicatedNodeTest;
    }

    public int getPredicateCount() {
        int i = this.m_predCount;
        if (-1 != i) {
            return i;
        }
        Expression[] expressionArr = this.m_predicates;
        if (expressionArr == null) {
            return 0;
        }
        return expressionArr.length;
    }

    public void setPredicateCount(int i) {
        if (i > 0) {
            Expression[] expressionArr = new Expression[i];
            for (int i2 = 0; i2 < i; i2++) {
                expressionArr[i2] = this.m_predicates[i2];
            }
            this.m_predicates = expressionArr;
            return;
        }
        this.m_predicates = null;
    }

    /* access modifiers changed from: protected */
    public void initPredicateInfo(Compiler compiler, int i) throws TransformerException {
        int firstPredicateOpPos = compiler.getFirstPredicateOpPos(i);
        if (firstPredicateOpPos > 0) {
            this.m_predicates = compiler.getCompiledPredicates(firstPredicateOpPos);
            if (this.m_predicates != null) {
                int i2 = 0;
                while (true) {
                    Expression[] expressionArr = this.m_predicates;
                    if (i2 < expressionArr.length) {
                        expressionArr[i2].exprSetParent(this);
                        i2++;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public Expression getPredicate(int i) {
        return this.m_predicates[i];
    }

    public int getProximityPosition() {
        return getProximityPosition(this.m_predicateIndex);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public int getProximityPosition(XPathContext xPathContext) {
        return getProximityPosition();
    }

    /* access modifiers changed from: protected */
    public int getProximityPosition(int i) {
        if (i >= 0) {
            return this.m_proximityPositions[i];
        }
        return 0;
    }

    public void resetProximityPositions() {
        int predicateCount = getPredicateCount();
        if (predicateCount > 0) {
            if (this.m_proximityPositions == null) {
                this.m_proximityPositions = new int[predicateCount];
            }
            for (int i = 0; i < predicateCount; i++) {
                try {
                    initProximityPosition(i);
                } catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
    }

    public void initProximityPosition(int i) throws TransformerException {
        this.m_proximityPositions[i] = 0;
    }

    /* access modifiers changed from: protected */
    public void countProximityPosition(int i) {
        int[] iArr = this.m_proximityPositions;
        if (iArr != null && i < iArr.length) {
            iArr[i] = iArr[i] + 1;
        }
    }

    public int getPredicateIndex() {
        return this.m_predicateIndex;
    }

    /* access modifiers changed from: package-private */
    public boolean executePredicates(int i, XPathContext xPathContext) throws TransformerException {
        int predicateCount = getPredicateCount();
        if (predicateCount == 0) {
            return true;
        }
        xPathContext.getNamespaceContext();
        try {
            this.m_predicateIndex = 0;
            xPathContext.pushSubContextList(this);
            xPathContext.pushNamespaceContext(this.m_lpi.getPrefixResolver());
            xPathContext.pushCurrentNode(i);
            for (int i2 = 0; i2 < predicateCount; i2++) {
                XObject execute = this.m_predicates[i2].execute(xPathContext);
                if (2 == execute.getType()) {
                    if (getProximityPosition(this.m_predicateIndex) == ((int) execute.num())) {
                        if (this.m_predicates[i2].isStableNumber() && i2 == predicateCount - 1) {
                            this.m_foundLast = true;
                        }
                        int i3 = this.m_predicateIndex + 1;
                        this.m_predicateIndex = i3;
                        countProximityPosition(i3);
                    }
                } else if (execute.bool()) {
                    int i32 = this.m_predicateIndex + 1;
                    this.m_predicateIndex = i32;
                    countProximityPosition(i32);
                }
                return false;
            }
            xPathContext.popCurrentNode();
            xPathContext.popNamespaceContext();
            xPathContext.popSubContextList();
            this.m_predicateIndex = -1;
            return true;
        } finally {
            xPathContext.popCurrentNode();
            xPathContext.popNamespaceContext();
            xPathContext.popSubContextList();
            this.m_predicateIndex = -1;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        int predicateCount = getPredicateCount();
        for (int i2 = 0; i2 < predicateCount; i2++) {
            this.m_predicates[i2].fixupVariables(vector, i);
        }
    }

    /* access modifiers changed from: protected */
    public String nodeToString(int i) {
        if (-1 == i) {
            return "null";
        }
        DTM dtm = this.m_lpi.getXPathContext().getDTM(i);
        return dtm.getNodeName(i) + "{" + (i + 1) + "}";
    }

    public short acceptNode(int i) {
        XPathContext xPathContext = this.m_lpi.getXPathContext();
        try {
            xPathContext.pushCurrentNode(i);
            if (execute(xPathContext, i) != NodeTest.SCORE_NONE) {
                if (getPredicateCount() > 0) {
                    countProximityPosition(0);
                    if (!executePredicates(i, xPathContext)) {
                        xPathContext.popCurrentNode();
                        return 3;
                    }
                }
                xPathContext.popCurrentNode();
                return 1;
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

    public LocPathIterator getLocPathIterator() {
        return this.m_lpi;
    }

    public void setLocPathIterator(LocPathIterator locPathIterator) {
        this.m_lpi = locPathIterator;
        if (this != locPathIterator) {
            locPathIterator.exprSetParent(this);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean canTraverseOutsideSubtree() {
        int predicateCount = getPredicateCount();
        for (int i = 0; i < predicateCount; i++) {
            if (getPredicate(i).canTraverseOutsideSubtree()) {
                return true;
            }
        }
        return false;
    }

    public void callPredicateVisitors(XPathVisitor xPathVisitor) {
        Expression[] expressionArr = this.m_predicates;
        if (expressionArr != null) {
            int length = expressionArr.length;
            for (int i = 0; i < length; i++) {
                PredOwner predOwner = new PredOwner(i);
                if (xPathVisitor.visitPredicate(predOwner, this.m_predicates[i])) {
                    this.m_predicates[i].callVisitors(predOwner, xPathVisitor);
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!super.deepEquals(expression)) {
            return false;
        }
        PredicatedNodeTest predicatedNodeTest = (PredicatedNodeTest) expression;
        Expression[] expressionArr = this.m_predicates;
        if (expressionArr != null) {
            int length = expressionArr.length;
            Expression[] expressionArr2 = predicatedNodeTest.m_predicates;
            if (expressionArr2 == null || expressionArr2.length != length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!this.m_predicates[i].deepEquals(predicatedNodeTest.m_predicates[i])) {
                    return false;
                }
            }
            return true;
        } else if (predicatedNodeTest.m_predicates != null) {
            return false;
        } else {
            return true;
        }
    }

    class PredOwner implements ExpressionOwner {
        int m_index;

        PredOwner(int i) {
            this.m_index = i;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return PredicatedNodeTest.this.m_predicates[this.m_index];
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(PredicatedNodeTest.this);
            PredicatedNodeTest.this.m_predicates[this.m_index] = expression;
        }
    }
}

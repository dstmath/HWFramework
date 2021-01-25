package ohos.com.sun.org.apache.xpath.internal.patterns;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.Axis;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.ExpressionOwner;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;
import ohos.com.sun.org.apache.xpath.internal.XPathVisitor;
import ohos.com.sun.org.apache.xpath.internal.axes.SubContextList;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.com.sun.org.apache.xpath.internal.objects.XObject;
import ohos.javax.xml.transform.TransformerException;

public class StepPattern extends NodeTest implements SubContextList, ExpressionOwner {
    private static final boolean DEBUG_MATCHES = false;
    static final long serialVersionUID = 9071668960168152644L;
    protected int m_axis;
    Expression[] m_predicates;
    StepPattern m_relativePathPattern;
    String m_targetString;

    public StepPattern(int i, String str, String str2, int i2, int i3) {
        super(i, str, str2);
        this.m_axis = i2;
    }

    public StepPattern(int i, int i2, int i3) {
        super(i);
        this.m_axis = i2;
    }

    public void calcTargetString() {
        int whatToShow = getWhatToShow();
        if (whatToShow == -1) {
            this.m_targetString = "*";
        } else if (whatToShow != 1) {
            if (whatToShow == 4 || whatToShow == 8 || whatToShow == 12) {
                this.m_targetString = PsuedoNames.PSEUDONAME_TEXT;
            } else if (whatToShow == 128) {
                this.m_targetString = PsuedoNames.PSEUDONAME_COMMENT;
            } else if (whatToShow == 256 || whatToShow == 1280) {
                this.m_targetString = PsuedoNames.PSEUDONAME_ROOT;
            } else {
                this.m_targetString = "*";
            }
        } else if ("*" == this.m_name) {
            this.m_targetString = "*";
        } else {
            this.m_targetString = this.m_name;
        }
    }

    public String getTargetString() {
        return this.m_targetString;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public void fixupVariables(Vector vector, int i) {
        super.fixupVariables(vector, i);
        if (this.m_predicates != null) {
            int i2 = 0;
            while (true) {
                Expression[] expressionArr = this.m_predicates;
                if (i2 >= expressionArr.length) {
                    break;
                }
                expressionArr[i2].fixupVariables(vector, i);
                i2++;
            }
        }
        StepPattern stepPattern = this.m_relativePathPattern;
        if (stepPattern != null) {
            stepPattern.fixupVariables(vector, i);
        }
    }

    public void setRelativePathPattern(StepPattern stepPattern) {
        this.m_relativePathPattern = stepPattern;
        stepPattern.exprSetParent(this);
        calcScore();
    }

    public StepPattern getRelativePathPattern() {
        return this.m_relativePathPattern;
    }

    public Expression[] getPredicates() {
        return this.m_predicates;
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

    public Expression getPredicate(int i) {
        return this.m_predicates[i];
    }

    public final int getPredicateCount() {
        Expression[] expressionArr = this.m_predicates;
        if (expressionArr == null) {
            return 0;
        }
        return expressionArr.length;
    }

    public void setPredicates(Expression[] expressionArr) {
        this.m_predicates = expressionArr;
        if (expressionArr != null) {
            for (Expression expression : expressionArr) {
                expression.exprSetParent(this);
            }
        }
        calcScore();
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest
    public void calcScore() {
        if (getPredicateCount() > 0 || this.m_relativePathPattern != null) {
            this.m_score = SCORE_OTHER;
        } else {
            super.calcScore();
        }
        if (this.m_targetString == null) {
            calcTargetString();
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, int i) throws TransformerException {
        DTM dtm = xPathContext.getDTM(i);
        if (dtm != null) {
            return execute(xPathContext, i, dtm, dtm.getExpandedTypeID(i));
        }
        return NodeTest.SCORE_NONE;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext) throws TransformerException {
        return execute(xPathContext, xPathContext.getCurrentNode());
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public XObject execute(XPathContext xPathContext, int i, DTM dtm, int i2) throws TransformerException {
        if (this.m_whatToShow == 65536) {
            StepPattern stepPattern = this.m_relativePathPattern;
            if (stepPattern != null) {
                return stepPattern.execute(xPathContext);
            }
            return NodeTest.SCORE_NONE;
        }
        XObject execute = super.execute(xPathContext, i, dtm, i2);
        if (execute == NodeTest.SCORE_NONE) {
            return NodeTest.SCORE_NONE;
        }
        if (getPredicateCount() != 0 && !executePredicates(xPathContext, dtm, i)) {
            return NodeTest.SCORE_NONE;
        }
        StepPattern stepPattern2 = this.m_relativePathPattern;
        return stepPattern2 != null ? stepPattern2.executeRelativePathPattern(xPathContext, dtm, i) : execute;
    }

    /* JADX INFO: finally extract failed */
    private final boolean checkProximityPosition(XPathContext xPathContext, int i, DTM dtm, int i2, int i3) {
        boolean z;
        try {
            DTMAxisTraverser axisTraverser = dtm.getAxisTraverser(12);
            int first = axisTraverser.first(i2);
            while (-1 != first) {
                try {
                    xPathContext.pushCurrentNode(first);
                    if (NodeTest.SCORE_NONE != super.execute(xPathContext, first)) {
                        try {
                            xPathContext.pushSubContextList(this);
                            int i4 = 0;
                            while (true) {
                                if (i4 >= i) {
                                    z = true;
                                    break;
                                }
                                xPathContext.pushPredicatePos(i4);
                                try {
                                    XObject execute = this.m_predicates[i4].execute(xPathContext);
                                    try {
                                        if (2 == execute.getType()) {
                                            throw new Error("Why: Should never have been called");
                                        } else if (!execute.boolWithSideEffects()) {
                                            execute.detach();
                                            xPathContext.popPredicatePos();
                                            z = false;
                                            break;
                                        } else {
                                            execute.detach();
                                            xPathContext.popPredicatePos();
                                            i4++;
                                        }
                                    } catch (Throwable th) {
                                        execute.detach();
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    xPathContext.popPredicatePos();
                                    throw th2;
                                }
                            }
                            if (z) {
                                i3--;
                            }
                            if (i3 < 1) {
                                return false;
                            }
                        } finally {
                            xPathContext.popSubContextList();
                        }
                    }
                    xPathContext.popCurrentNode();
                    first = axisTraverser.next(i2, first);
                } finally {
                    xPathContext.popCurrentNode();
                }
            }
            return i3 == 1;
        } catch (TransformerException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /* JADX INFO: finally extract failed */
    private final int getProximityPosition(XPathContext xPathContext, int i, boolean z) {
        boolean z2;
        int currentNode = xPathContext.getCurrentNode();
        DTM dtm = xPathContext.getDTM(currentNode);
        int parent = dtm.getParent(currentNode);
        try {
            DTMAxisTraverser axisTraverser = dtm.getAxisTraverser(3);
            int i2 = 0;
            for (int first = axisTraverser.first(parent); -1 != first; first = axisTraverser.next(parent, first)) {
                try {
                    xPathContext.pushCurrentNode(first);
                    if (NodeTest.SCORE_NONE != super.execute(xPathContext, first)) {
                        try {
                            xPathContext.pushSubContextList(this);
                            for (int i3 = 0; i3 < i; i3++) {
                                xPathContext.pushPredicatePos(i3);
                                try {
                                    XObject execute = this.m_predicates[i3].execute(xPathContext);
                                    try {
                                        if (2 == execute.getType()) {
                                            if (i2 + 1 == ((int) execute.numWithSideEffects())) {
                                                execute.detach();
                                                xPathContext.popPredicatePos();
                                            }
                                        } else if (!execute.boolWithSideEffects()) {
                                            execute.detach();
                                        } else {
                                            execute.detach();
                                            xPathContext.popPredicatePos();
                                        }
                                        xPathContext.popPredicatePos();
                                        z2 = false;
                                        break;
                                    } finally {
                                        execute.detach();
                                    }
                                } catch (Throwable th) {
                                    xPathContext.popPredicatePos();
                                    throw th;
                                }
                            }
                            z2 = true;
                            if (z2) {
                                i2++;
                            }
                            if (!z && first == currentNode) {
                                return i2;
                            }
                        } finally {
                            xPathContext.popSubContextList();
                        }
                    }
                    xPathContext.popCurrentNode();
                } finally {
                    xPathContext.popCurrentNode();
                }
            }
            return i2;
        } catch (TransformerException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public int getProximityPosition(XPathContext xPathContext) {
        return getProximityPosition(xPathContext, xPathContext.getPredicatePos(), false);
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.axes.SubContextList
    public int getLastPos(XPathContext xPathContext) {
        return getProximityPosition(xPathContext, xPathContext.getPredicatePos(), true);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public final XObject executeRelativePathPattern(XPathContext xPathContext, DTM dtm, int i) throws TransformerException {
        XObject xObject = NodeTest.SCORE_NONE;
        DTMAxisTraverser axisTraverser = dtm.getAxisTraverser(this.m_axis);
        int first = axisTraverser.first(i);
        while (true) {
            if (-1 == first) {
                break;
            }
            try {
                xPathContext.pushCurrentNode(first);
                xObject = execute(xPathContext);
                if (xObject != NodeTest.SCORE_NONE) {
                    xPathContext.popCurrentNode();
                    break;
                }
                xPathContext.popCurrentNode();
                first = axisTraverser.next(i, first);
            } catch (Throwable th) {
                xPathContext.popCurrentNode();
                throw th;
            }
        }
        return xObject;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public final boolean executePredicates(XPathContext xPathContext, DTM dtm, int i) throws TransformerException {
        int predicateCount = getPredicateCount();
        try {
            xPathContext.pushSubContextList(this);
            boolean z = false;
            boolean z2 = false;
            for (int i2 = 0; i2 < predicateCount; i2++) {
                xPathContext.pushPredicatePos(i2);
                try {
                    XObject execute = this.m_predicates[i2].execute(xPathContext);
                    try {
                        if (2 == execute.getType()) {
                            int num = (int) execute.num();
                            if (z2) {
                                if (num == 1) {
                                    z = true;
                                }
                                execute.detach();
                            } else if (!checkProximityPosition(xPathContext, i2, dtm, i, num)) {
                                execute.detach();
                            } else {
                                z2 = true;
                                execute.detach();
                                xPathContext.popPredicatePos();
                            }
                        } else if (!execute.boolWithSideEffects()) {
                            execute.detach();
                        } else {
                            execute.detach();
                            xPathContext.popPredicatePos();
                        }
                        xPathContext.popPredicatePos();
                        break;
                    } catch (Throwable th) {
                        execute.detach();
                        throw th;
                    }
                } catch (Throwable th2) {
                    xPathContext.popPredicatePos();
                    throw th2;
                }
            }
            z = true;
            return z;
        } finally {
            xPathContext.popSubContextList();
        }
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (StepPattern stepPattern = this; stepPattern != null; stepPattern = stepPattern.m_relativePathPattern) {
            if (stepPattern != this) {
                stringBuffer.append(PsuedoNames.PSEUDONAME_ROOT);
            }
            stringBuffer.append(Axis.getNames(stepPattern.m_axis));
            stringBuffer.append("::");
            if (20480 == stepPattern.m_whatToShow) {
                stringBuffer.append("doc()");
            } else if (65536 == stepPattern.m_whatToShow) {
                stringBuffer.append("function()");
            } else if (-1 == stepPattern.m_whatToShow) {
                stringBuffer.append("node()");
            } else if (4 == stepPattern.m_whatToShow) {
                stringBuffer.append("text()");
            } else if (64 == stepPattern.m_whatToShow) {
                stringBuffer.append("processing-instruction(");
                if (stepPattern.m_name != null) {
                    stringBuffer.append(stepPattern.m_name);
                }
                stringBuffer.append(")");
            } else if (128 == stepPattern.m_whatToShow) {
                stringBuffer.append("comment()");
            } else if (stepPattern.m_name != null) {
                if (2 == stepPattern.m_whatToShow) {
                    stringBuffer.append("@");
                }
                if (stepPattern.m_namespace != null) {
                    stringBuffer.append("{");
                    stringBuffer.append(stepPattern.m_namespace);
                    stringBuffer.append("}");
                }
                stringBuffer.append(stepPattern.m_name);
            } else if (2 == stepPattern.m_whatToShow) {
                stringBuffer.append("@");
            } else if (1280 == stepPattern.m_whatToShow) {
                stringBuffer.append("doc-root()");
            } else {
                stringBuffer.append('?');
                stringBuffer.append(Integer.toHexString(stepPattern.m_whatToShow));
            }
            if (stepPattern.m_predicates != null) {
                for (int i = 0; i < stepPattern.m_predicates.length; i++) {
                    stringBuffer.append("[");
                    stringBuffer.append(stepPattern.m_predicates[i]);
                    stringBuffer.append("]");
                }
            }
        }
        return stringBuffer.toString();
    }

    public double getMatchScore(XPathContext xPathContext, int i) throws TransformerException {
        xPathContext.pushCurrentNode(i);
        xPathContext.pushCurrentExpressionNode(i);
        try {
            return execute(xPathContext).num();
        } finally {
            xPathContext.popCurrentNode();
            xPathContext.popCurrentExpressionNode();
        }
    }

    public void setAxis(int i) {
        this.m_axis = i;
    }

    public int getAxis() {
        return this.m_axis;
    }

    /* access modifiers changed from: package-private */
    public class PredOwner implements ExpressionOwner {
        int m_index;

        PredOwner(int i) {
            this.m_index = i;
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public Expression getExpression() {
            return StepPattern.this.m_predicates[this.m_index];
        }

        @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
        public void setExpression(Expression expression) {
            expression.exprSetParent(StepPattern.this);
            StepPattern.this.m_predicates[this.m_index] = expression;
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.XPathVisitable
    public void callVisitors(ExpressionOwner expressionOwner, XPathVisitor xPathVisitor) {
        if (xPathVisitor.visitMatchPattern(expressionOwner, this)) {
            callSubtreeVisitors(xPathVisitor);
        }
    }

    /* access modifiers changed from: protected */
    public void callSubtreeVisitors(XPathVisitor xPathVisitor) {
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
        StepPattern stepPattern = this.m_relativePathPattern;
        if (stepPattern != null) {
            stepPattern.callVisitors(this, xPathVisitor);
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public Expression getExpression() {
        return this.m_relativePathPattern;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.ExpressionOwner
    public void setExpression(Expression expression) {
        expression.exprSetParent(this);
        this.m_relativePathPattern = (StepPattern) expression;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.patterns.NodeTest, ohos.com.sun.org.apache.xpath.internal.Expression
    public boolean deepEquals(Expression expression) {
        if (!super.deepEquals(expression)) {
            return false;
        }
        StepPattern stepPattern = (StepPattern) expression;
        Expression[] expressionArr = this.m_predicates;
        if (expressionArr != null) {
            int length = expressionArr.length;
            Expression[] expressionArr2 = stepPattern.m_predicates;
            if (expressionArr2 == null || expressionArr2.length != length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!this.m_predicates[i].deepEquals(stepPattern.m_predicates[i])) {
                    return false;
                }
            }
        } else if (stepPattern.m_predicates != null) {
            return false;
        }
        StepPattern stepPattern2 = this.m_relativePathPattern;
        if (stepPattern2 != null) {
            if (!stepPattern2.deepEquals(stepPattern.m_relativePathPattern)) {
                return false;
            }
            return true;
        } else if (stepPattern.m_relativePathPattern != null) {
            return false;
        } else {
            return true;
        }
    }
}

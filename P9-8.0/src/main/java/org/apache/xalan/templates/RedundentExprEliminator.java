package org.apache.xalan.templates;

import java.util.Vector;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPath;
import org.apache.xpath.axes.AxesWalker;
import org.apache.xpath.axes.FilterExprIteratorSimple;
import org.apache.xpath.axes.FilterExprWalker;
import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.axes.SelfIteratorNoPredicate;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.axes.WalkingIterator;
import org.apache.xpath.operations.Variable;
import org.apache.xpath.operations.VariableSafeAbsRef;
import org.w3c.dom.DOMException;

public class RedundentExprEliminator extends XSLTVisitor {
    public static final boolean DEBUG = false;
    public static final boolean DIAGNOSE_MULTISTEPLIST = false;
    public static final boolean DIAGNOSE_NUM_PATHS_REDUCED = false;
    static final String PSUEDOVARNAMESPACE = "http://xml.apache.org/xalan/psuedovar";
    private static int m_uniquePseudoVarID = 1;
    AbsPathChecker m_absPathChecker = new AbsPathChecker();
    Vector m_absPaths = new Vector();
    boolean m_isSameContext = true;
    Vector m_paths = null;
    VarNameCollector m_varNameCollector = new VarNameCollector();

    class MultistepExprHolder implements Cloneable {
        ExpressionOwner m_exprOwner;
        MultistepExprHolder m_next;
        final int m_stepCount;

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        MultistepExprHolder(ExpressionOwner exprOwner, int stepCount, MultistepExprHolder next) {
            this.m_exprOwner = exprOwner;
            RedundentExprEliminator.assertion(this.m_exprOwner != null, "exprOwner can not be null!");
            this.m_stepCount = stepCount;
            this.m_next = next;
        }

        MultistepExprHolder addInSortedOrder(ExpressionOwner exprOwner, int stepCount) {
            MultistepExprHolder first = this;
            MultistepExprHolder prev = null;
            for (MultistepExprHolder next = this; next != null; next = next.m_next) {
                if (stepCount >= next.m_stepCount) {
                    MultistepExprHolder newholder = new MultistepExprHolder(exprOwner, stepCount, next);
                    if (prev == null) {
                        first = newholder;
                    } else {
                        prev.m_next = newholder;
                    }
                    return first;
                }
                prev = next;
            }
            prev.m_next = new MultistepExprHolder(exprOwner, stepCount, null);
            return this;
        }

        MultistepExprHolder unlink(MultistepExprHolder itemToRemove) {
            MultistepExprHolder first = this;
            MultistepExprHolder prev = null;
            for (MultistepExprHolder next = this; next != null; next = next.m_next) {
                if (next == itemToRemove) {
                    if (prev == null) {
                        first = next.m_next;
                    } else {
                        prev.m_next = next.m_next;
                    }
                    next.m_next = null;
                    return first;
                }
                prev = next;
            }
            RedundentExprEliminator.assertion(false, "unlink failed!!!");
            return null;
        }

        int getLength() {
            int count = 0;
            for (MultistepExprHolder next = this; next != null; next = next.m_next) {
                count++;
            }
            return count;
        }

        protected void diagnose() {
            System.err.print("Found multistep iterators: " + getLength() + "  ");
            MultistepExprHolder next = this;
            while (next != null) {
                System.err.print("" + next.m_stepCount);
                next = next.m_next;
                if (next != null) {
                    System.err.print(", ");
                }
            }
            System.err.println();
        }
    }

    public void eleminateRedundentLocals(ElemTemplateElement psuedoVarRecipient) {
        eleminateRedundent(psuedoVarRecipient, this.m_paths);
    }

    public void eleminateRedundentGlobals(StylesheetRoot stylesheet) {
        eleminateRedundent(stylesheet, this.m_absPaths);
    }

    protected void eleminateRedundent(ElemTemplateElement psuedoVarRecipient, Vector paths) {
        int n = paths.size();
        int numPathsEliminated = 0;
        int numUniquePathsEliminated = 0;
        for (int i = 0; i < n; i++) {
            ExpressionOwner owner = (ExpressionOwner) paths.elementAt(i);
            if (owner != null) {
                int found = findAndEliminateRedundant(i + 1, i, owner, psuedoVarRecipient, paths);
                if (found > 0) {
                    numUniquePathsEliminated++;
                }
                numPathsEliminated += found;
            }
        }
        eleminateSharedPartialPaths(psuedoVarRecipient, paths);
    }

    protected void eleminateSharedPartialPaths(ElemTemplateElement psuedoVarRecipient, Vector paths) {
        MultistepExprHolder list = createMultistepExprList(paths);
        if (list != null) {
            boolean isGlobal = paths == this.m_absPaths;
            int i = list.m_stepCount - 1;
            while (i >= 1) {
                MultistepExprHolder next = list;
                while (next != null && next.m_stepCount >= i) {
                    list = matchAndEliminatePartialPaths(next, list, isGlobal, i, psuedoVarRecipient);
                    next = next.m_next;
                }
                i--;
            }
        }
    }

    protected MultistepExprHolder matchAndEliminatePartialPaths(MultistepExprHolder testee, MultistepExprHolder head, boolean isGlobal, int lengthToTest, ElemTemplateElement varScope) {
        if (testee.m_exprOwner == null) {
            return head;
        }
        WalkingIterator iter1 = (WalkingIterator) testee.m_exprOwner.getExpression();
        if (partialIsVariable(testee, lengthToTest)) {
            return head;
        }
        MultistepExprHolder matchedPaths = null;
        MultistepExprHolder matchedPathsTail = null;
        MultistepExprHolder meh = head;
        while (meh != null) {
            if (!(meh == testee || meh.m_exprOwner == null)) {
                if (stepsEqual(iter1, (WalkingIterator) meh.m_exprOwner.getExpression(), lengthToTest)) {
                    if (matchedPaths == null) {
                        try {
                            matchedPaths = (MultistepExprHolder) testee.clone();
                            testee.m_exprOwner = null;
                        } catch (CloneNotSupportedException e) {
                        }
                        matchedPathsTail = matchedPaths;
                        matchedPaths.m_next = null;
                    }
                    try {
                        matchedPathsTail.m_next = (MultistepExprHolder) meh.clone();
                        meh.m_exprOwner = null;
                    } catch (CloneNotSupportedException e2) {
                    }
                    matchedPathsTail = matchedPathsTail.m_next;
                    matchedPathsTail.m_next = null;
                }
            }
            meh = meh.m_next;
        }
        if (matchedPaths != null) {
            ElemVariable var = createPseudoVarDecl(isGlobal ? varScope : findCommonAncestor(matchedPaths), createIteratorFromSteps((WalkingIterator) matchedPaths.m_exprOwner.getExpression(), lengthToTest), isGlobal);
            while (matchedPaths != null) {
                ExpressionOwner owner = matchedPaths.m_exprOwner;
                owner.setExpression(changePartToRef(var.getName(), (WalkingIterator) owner.getExpression(), lengthToTest, isGlobal));
                matchedPaths = matchedPaths.m_next;
            }
        }
        return head;
    }

    boolean partialIsVariable(MultistepExprHolder testee, int lengthToTest) {
        if (1 == lengthToTest && (((WalkingIterator) testee.m_exprOwner.getExpression()).getFirstWalker() instanceof FilterExprWalker)) {
            return true;
        }
        return false;
    }

    protected void diagnoseLineNumber(Expression expr) {
        ElemTemplateElement e = getElemFromExpression(expr);
        System.err.println("   " + e.getSystemId() + " Line " + e.getLineNumber());
    }

    protected ElemTemplateElement findCommonAncestor(MultistepExprHolder head) {
        int i;
        int numExprs = head.getLength();
        ElemTemplateElement[] elems = new ElemTemplateElement[numExprs];
        int[] ancestorCounts = new int[numExprs];
        MultistepExprHolder next = head;
        int shortestAncestorCount = 10000;
        for (i = 0; i < numExprs; i++) {
            ElemTemplateElement elem = getElemFromExpression(next.m_exprOwner.getExpression());
            elems[i] = elem;
            int numAncestors = countAncestors(elem);
            ancestorCounts[i] = numAncestors;
            if (numAncestors < shortestAncestorCount) {
                shortestAncestorCount = numAncestors;
            }
            next = next.m_next;
        }
        for (i = 0; i < numExprs; i++) {
            if (ancestorCounts[i] > shortestAncestorCount) {
                int numStepCorrection = ancestorCounts[i] - shortestAncestorCount;
                for (int j = 0; j < numStepCorrection; j++) {
                    elems[i] = elems[i].getParentElem();
                }
            }
        }
        while (true) {
            int shortestAncestorCount2 = shortestAncestorCount;
            shortestAncestorCount = shortestAncestorCount2 - 1;
            if (shortestAncestorCount2 >= 0) {
                boolean areEqual = true;
                ElemTemplateElement first = elems[0];
                for (i = 1; i < numExprs; i++) {
                    if (first != elems[i]) {
                        areEqual = false;
                        break;
                    }
                }
                if (areEqual && isNotSameAsOwner(head, first) && first.canAcceptVariables()) {
                    return first;
                }
                for (i = 0; i < numExprs; i++) {
                    elems[i] = elems[i].getParentElem();
                }
            } else {
                assertion(false, "Could not find common ancestor!!!");
                return null;
            }
        }
    }

    protected boolean isNotSameAsOwner(MultistepExprHolder head, ElemTemplateElement ete) {
        for (MultistepExprHolder next = head; next != null; next = next.m_next) {
            if (getElemFromExpression(next.m_exprOwner.getExpression()) == ete) {
                return false;
            }
        }
        return true;
    }

    protected int countAncestors(ElemTemplateElement elem) {
        int count = 0;
        while (elem != null) {
            count++;
            elem = elem.getParentElem();
        }
        return count;
    }

    protected void diagnoseMultistepList(int matchCount, int lengthToTest, boolean isGlobal) {
        if (matchCount > 0) {
            System.err.print("Found multistep matches: " + matchCount + ", " + lengthToTest + " length");
            if (isGlobal) {
                System.err.println(" (global)");
            } else {
                System.err.println();
            }
        }
    }

    protected LocPathIterator changePartToRef(QName uniquePseudoVarName, WalkingIterator wi, int numSteps, boolean isGlobal) {
        Variable var = new Variable();
        var.setQName(uniquePseudoVarName);
        var.setIsGlobal(isGlobal);
        if (isGlobal) {
            var.setIndex(getElemFromExpression(wi).getStylesheetRoot().getVariablesAndParamsComposed().size() - 1);
        }
        AxesWalker walker = wi.getFirstWalker();
        for (int i = 0; i < numSteps; i++) {
            assertion(walker != null, "Walker should not be null!");
            walker = walker.getNextWalker();
        }
        if (walker != null) {
            FilterExprWalker few = new FilterExprWalker(wi);
            few.setInnerExpression(var);
            few.exprSetParent(wi);
            few.setNextWalker(walker);
            walker.setPrevWalker(few);
            wi.setFirstWalker(few);
            return wi;
        }
        FilterExprIteratorSimple feis = new FilterExprIteratorSimple(var);
        feis.exprSetParent(wi.exprGetParent());
        return feis;
    }

    protected WalkingIterator createIteratorFromSteps(WalkingIterator wi, int numSteps) {
        WalkingIterator newIter = new WalkingIterator(wi.getPrefixResolver());
        try {
            AxesWalker walker = (AxesWalker) wi.getFirstWalker().clone();
            newIter.setFirstWalker(walker);
            walker.setLocPathIterator(newIter);
            for (int i = 1; i < numSteps; i++) {
                AxesWalker next = (AxesWalker) walker.getNextWalker().clone();
                walker.setNextWalker(next);
                next.setLocPathIterator(newIter);
                walker = next;
            }
            walker.setNextWalker(null);
            return newIter;
        } catch (CloneNotSupportedException cnse) {
            throw new WrappedRuntimeException(cnse);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0011, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean stepsEqual(WalkingIterator iter1, WalkingIterator iter2, int numSteps) {
        boolean z = false;
        AxesWalker aw1 = iter1.getFirstWalker();
        AxesWalker aw2 = iter2.getFirstWalker();
        for (int i = 0; i < numSteps; i++) {
            if (aw1 == null || aw2 == null || !aw1.deepEquals(aw2)) {
                return false;
            }
            aw1 = aw1.getNextWalker();
            aw2 = aw2.getNextWalker();
        }
        if (!(aw1 == null && aw2 == null)) {
            z = true;
        }
        assertion(z, "Total match is incorrect!");
        return true;
    }

    protected MultistepExprHolder createMultistepExprList(Vector paths) {
        MultistepExprHolder first = null;
        int n = paths.size();
        for (int i = 0; i < n; i++) {
            ExpressionOwner eo = (ExpressionOwner) paths.elementAt(i);
            if (eo != null) {
                int numPaths = countSteps((LocPathIterator) eo.getExpression());
                if (numPaths > 1) {
                    if (first == null) {
                        first = new MultistepExprHolder(eo, numPaths, null);
                    } else {
                        first = first.addInSortedOrder(eo, numPaths);
                    }
                }
            }
        }
        if (first == null || first.getLength() <= 1) {
            return null;
        }
        return first;
    }

    protected int findAndEliminateRedundant(int start, int firstOccuranceIndex, ExpressionOwner firstOccuranceOwner, ElemTemplateElement psuedoVarRecipient, Vector paths) throws DOMException {
        MultistepExprHolder head = null;
        MultistepExprHolder tail = null;
        int numPathsFound = 0;
        int n = paths.size();
        Expression expr1 = firstOccuranceOwner.getExpression();
        boolean isGlobal = paths == this.m_absPaths;
        LocPathIterator lpi = (LocPathIterator) expr1;
        int stepCount = countSteps(lpi);
        for (int j = start; j < n; j++) {
            ExpressionOwner owner2 = (ExpressionOwner) paths.elementAt(j);
            if (owner2 != null) {
                Expression expr2 = owner2.getExpression();
                if (expr2.deepEquals(lpi)) {
                    LocPathIterator lpi2 = (LocPathIterator) expr2;
                    if (head == null) {
                        head = new MultistepExprHolder(firstOccuranceOwner, stepCount, null);
                        tail = head;
                        numPathsFound++;
                    }
                    tail.m_next = new MultistepExprHolder(owner2, stepCount, null);
                    tail = tail.m_next;
                    paths.setElementAt(null, j);
                    numPathsFound++;
                }
            }
        }
        if (numPathsFound == 0 && isGlobal) {
            head = new MultistepExprHolder(firstOccuranceOwner, stepCount, null);
            numPathsFound++;
        }
        if (head != null) {
            ElemTemplateElement root = isGlobal ? psuedoVarRecipient : findCommonAncestor(head);
            ElemVariable var = createPseudoVarDecl(root, (LocPathIterator) head.m_exprOwner.getExpression(), isGlobal);
            QName uniquePseudoVarName = var.getName();
            while (head != null) {
                changeToVarRef(uniquePseudoVarName, head.m_exprOwner, paths, root);
                head = head.m_next;
            }
            paths.setElementAt(var.getSelect(), firstOccuranceIndex);
        }
        return numPathsFound;
    }

    protected int oldFindAndEliminateRedundant(int start, int firstOccuranceIndex, ExpressionOwner firstOccuranceOwner, ElemTemplateElement psuedoVarRecipient, Vector paths) throws DOMException {
        ElemVariable var;
        QName uniquePseudoVarName = null;
        boolean foundFirst = false;
        int numPathsFound = 0;
        int n = paths.size();
        Expression expr1 = firstOccuranceOwner.getExpression();
        boolean isGlobal = paths == this.m_absPaths;
        LocPathIterator lpi = (LocPathIterator) expr1;
        for (int j = start; j < n; j++) {
            ExpressionOwner owner2 = (ExpressionOwner) paths.elementAt(j);
            if (owner2 != null) {
                Expression expr2 = owner2.getExpression();
                if (expr2.deepEquals(lpi)) {
                    LocPathIterator lpi2 = (LocPathIterator) expr2;
                    if (!foundFirst) {
                        foundFirst = true;
                        var = createPseudoVarDecl(psuedoVarRecipient, lpi, isGlobal);
                        if (var == null) {
                            return 0;
                        }
                        uniquePseudoVarName = var.getName();
                        changeToVarRef(uniquePseudoVarName, firstOccuranceOwner, paths, psuedoVarRecipient);
                        paths.setElementAt(var.getSelect(), firstOccuranceIndex);
                        numPathsFound++;
                    }
                    changeToVarRef(uniquePseudoVarName, owner2, paths, psuedoVarRecipient);
                    paths.setElementAt(null, j);
                    numPathsFound++;
                } else {
                    continue;
                }
            }
        }
        if (numPathsFound == 0 && paths == this.m_absPaths) {
            var = createPseudoVarDecl(psuedoVarRecipient, lpi, true);
            if (var == null) {
                return 0;
            }
            changeToVarRef(var.getName(), firstOccuranceOwner, paths, psuedoVarRecipient);
            paths.setElementAt(var.getSelect(), firstOccuranceIndex);
            numPathsFound++;
        }
        return numPathsFound;
    }

    protected int countSteps(LocPathIterator lpi) {
        if (!(lpi instanceof WalkingIterator)) {
            return 1;
        }
        int count = 0;
        for (AxesWalker aw = ((WalkingIterator) lpi).getFirstWalker(); aw != null; aw = aw.getNextWalker()) {
            count++;
        }
        return count;
    }

    protected void changeToVarRef(QName varName, ExpressionOwner owner, Vector paths, ElemTemplateElement psuedoVarRecipient) {
        Variable varRef = paths == this.m_absPaths ? new VariableSafeAbsRef() : new Variable();
        varRef.setQName(varName);
        if (paths == this.m_absPaths) {
            varRef.setIndex(((StylesheetRoot) psuedoVarRecipient).getVariablesAndParamsComposed().size() - 1);
            varRef.setIsGlobal(true);
        }
        owner.setExpression(varRef);
    }

    private static synchronized int getPseudoVarID() {
        int i;
        synchronized (RedundentExprEliminator.class) {
            i = m_uniquePseudoVarID;
            m_uniquePseudoVarID = i + 1;
        }
        return i;
    }

    protected ElemVariable createPseudoVarDecl(ElemTemplateElement psuedoVarRecipient, LocPathIterator lpi, boolean isGlobal) throws DOMException {
        QName uniquePseudoVarName = new QName(PSUEDOVARNAMESPACE, "#" + getPseudoVarID());
        if (isGlobal) {
            return createGlobalPseudoVarDecl(uniquePseudoVarName, (StylesheetRoot) psuedoVarRecipient, lpi);
        }
        return createLocalPseudoVarDecl(uniquePseudoVarName, psuedoVarRecipient, lpi);
    }

    protected ElemVariable createGlobalPseudoVarDecl(QName uniquePseudoVarName, StylesheetRoot stylesheetRoot, LocPathIterator lpi) throws DOMException {
        ElemVariable psuedoVar = new ElemVariable();
        psuedoVar.setIsTopLevel(true);
        psuedoVar.setSelect(new XPath(lpi));
        psuedoVar.setName(uniquePseudoVarName);
        Vector globalVars = stylesheetRoot.getVariablesAndParamsComposed();
        psuedoVar.setIndex(globalVars.size());
        globalVars.addElement(psuedoVar);
        return psuedoVar;
    }

    protected ElemVariable createLocalPseudoVarDecl(QName uniquePseudoVarName, ElemTemplateElement psuedoVarRecipient, LocPathIterator lpi) throws DOMException {
        ElemVariable psuedoVar = new ElemVariablePsuedo();
        psuedoVar.setSelect(new XPath(lpi));
        psuedoVar.setName(uniquePseudoVarName);
        ElemVariable var = addVarDeclToElem(psuedoVarRecipient, lpi, psuedoVar);
        lpi.exprSetParent(var);
        return var;
    }

    protected ElemVariable addVarDeclToElem(ElemTemplateElement psuedoVarRecipient, LocPathIterator lpi, ElemVariable psuedoVar) throws DOMException {
        ElemTemplateElement ete = psuedoVarRecipient.getFirstChildElem();
        lpi.callVisitors(null, this.m_varNameCollector);
        if (this.m_varNameCollector.getVarCount() > 0) {
            ElemVariable varElem = getPrevVariableElem(getElemFromExpression(lpi));
            while (varElem != null) {
                if (this.m_varNameCollector.doesOccur(varElem.getName())) {
                    psuedoVarRecipient = varElem.getParentElem();
                    ete = varElem.getNextSiblingElem();
                    break;
                }
                varElem = getPrevVariableElem(varElem);
            }
        }
        if (ete != null && 41 == ete.getXSLToken()) {
            if (!isParam(lpi)) {
                while (ete != null) {
                    ete = ete.getNextSiblingElem();
                    if (ete != null && 41 != ete.getXSLToken()) {
                        break;
                    }
                }
            } else {
                return null;
            }
        }
        psuedoVarRecipient.insertBefore(psuedoVar, ete);
        this.m_varNameCollector.reset();
        return psuedoVar;
    }

    protected boolean isParam(ExpressionNode expr) {
        while (expr != null && !(expr instanceof ElemTemplateElement)) {
            expr = expr.exprGetParent();
        }
        if (expr != null) {
            ElemTemplateElement ete = (ElemTemplateElement) expr;
            while (ete != null) {
                switch (ete.getXSLToken()) {
                    case 19:
                    case 25:
                        return false;
                    case 41:
                        return true;
                    default:
                        ete = ete.getParentElem();
                }
            }
        }
        return false;
    }

    protected ElemVariable getPrevVariableElem(ElemTemplateElement elem) {
        int type;
        do {
            elem = getPrevElementWithinContext(elem);
            if (elem != null) {
                type = elem.getXSLToken();
                if (73 == type) {
                    break;
                }
            } else {
                return null;
            }
        } while (41 != type);
        return (ElemVariable) elem;
    }

    protected ElemTemplateElement getPrevElementWithinContext(ElemTemplateElement elem) {
        ElemTemplateElement prev = elem.getPreviousSiblingElem();
        if (prev == null) {
            prev = elem.getParentElem();
        }
        if (prev == null) {
            return prev;
        }
        int type = prev.getXSLToken();
        if (28 == type || 19 == type || 25 == type) {
            return null;
        }
        return prev;
    }

    protected ElemTemplateElement getElemFromExpression(Expression expr) {
        for (ExpressionNode parent = expr.exprGetParent(); parent != null; parent = parent.exprGetParent()) {
            if (parent instanceof ElemTemplateElement) {
                return (ElemTemplateElement) parent;
            }
        }
        throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_ASSERT_NO_TEMPLATE_PARENT, null));
    }

    public boolean isAbsolute(LocPathIterator path) {
        boolean isAbs;
        int analysis = path.getAnalysisBits();
        if (WalkerFactory.isSet(analysis, WalkerFactory.BIT_ROOT)) {
            isAbs = true;
        } else {
            isAbs = WalkerFactory.isSet(analysis, WalkerFactory.BIT_ANY_DESCENDANT_FROM_ROOT);
        }
        if (isAbs) {
            return this.m_absPathChecker.checkAbsolute(path);
        }
        return isAbs;
    }

    public boolean visitLocationPath(ExpressionOwner owner, LocPathIterator path) {
        if (path instanceof SelfIteratorNoPredicate) {
            return true;
        }
        if (path instanceof WalkingIterator) {
            AxesWalker aw = ((WalkingIterator) path).getFirstWalker();
            if ((aw instanceof FilterExprWalker) && aw.getNextWalker() == null && (((FilterExprWalker) aw).getInnerExpression() instanceof Variable)) {
                return true;
            }
        }
        if (isAbsolute(path) && this.m_absPaths != null) {
            this.m_absPaths.addElement(owner);
        } else if (this.m_isSameContext && this.m_paths != null) {
            this.m_paths.addElement(owner);
        }
        return true;
    }

    public boolean visitPredicate(ExpressionOwner owner, Expression pred) {
        boolean savedIsSame = this.m_isSameContext;
        this.m_isSameContext = false;
        pred.callVisitors(owner, this);
        this.m_isSameContext = savedIsSame;
        return false;
    }

    public boolean visitTopLevelInstruction(ElemTemplateElement elem) {
        switch (elem.getXSLToken()) {
            case 19:
                return visitInstruction(elem);
            default:
                return true;
        }
    }

    public boolean visitInstruction(ElemTemplateElement elem) {
        int type = elem.getXSLToken();
        switch (type) {
            case 17:
            case 19:
            case 28:
                if (type == 28) {
                    ElemForEach efe = (ElemForEach) elem;
                    efe.getSelect().callVisitors(efe, this);
                }
                Vector savedPaths = this.m_paths;
                this.m_paths = new Vector();
                elem.callChildVisitors(this, false);
                eleminateRedundentLocals(elem);
                this.m_paths = savedPaths;
                return false;
            case 35:
            case 64:
                boolean savedIsSame = this.m_isSameContext;
                this.m_isSameContext = false;
                elem.callChildVisitors(this);
                this.m_isSameContext = savedIsSame;
                return false;
            default:
                return true;
        }
    }

    protected void diagnoseNumPaths(Vector paths, int numPathsEliminated, int numUniquePathsEliminated) {
        if (numPathsEliminated <= 0) {
            return;
        }
        if (paths == this.m_paths) {
            System.err.println("Eliminated " + numPathsEliminated + " total paths!");
            System.err.println("Consolodated " + numUniquePathsEliminated + " redundent paths!");
            return;
        }
        System.err.println("Eliminated " + numPathsEliminated + " total global paths!");
        System.err.println("Consolodated " + numUniquePathsEliminated + " redundent global paths!");
    }

    private final void assertIsLocPathIterator(Expression expr1, ExpressionOwner eo) throws RuntimeException {
        if (!(expr1 instanceof LocPathIterator)) {
            String errMsg;
            if (expr1 instanceof Variable) {
                errMsg = "Programmer's assertion: expr1 not an iterator: " + ((Variable) expr1).getQName();
            } else {
                errMsg = "Programmer's assertion: expr1 not an iterator: " + expr1.getClass().getName();
            }
            throw new RuntimeException(errMsg + ", " + eo.getClass().getName() + " " + expr1.exprGetParent());
        }
    }

    private static void validateNewAddition(Vector paths, ExpressionOwner owner, LocPathIterator path) throws RuntimeException {
        boolean z;
        if (owner.getExpression() == path) {
            z = true;
        } else {
            z = false;
        }
        assertion(z, "owner.getExpression() != path!!!");
        int n = paths.size();
        for (int i = 0; i < n; i++) {
            ExpressionOwner ew = (ExpressionOwner) paths.elementAt(i);
            if (ew != owner) {
                z = true;
            } else {
                z = false;
            }
            assertion(z, "duplicate owner on the list!!!");
            if (ew.getExpression() != path) {
                z = true;
            } else {
                z = false;
            }
            assertion(z, "duplicate expression on the list!!!");
        }
    }

    protected static void assertion(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR, new Object[]{msg}));
        }
    }
}

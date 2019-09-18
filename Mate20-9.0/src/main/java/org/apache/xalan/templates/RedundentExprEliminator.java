package org.apache.xalan.templates;

import java.io.PrintStream;
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

        /* access modifiers changed from: package-private */
        public MultistepExprHolder addInSortedOrder(ExpressionOwner exprOwner, int stepCount) {
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
            return first;
        }

        /* access modifiers changed from: package-private */
        public MultistepExprHolder unlink(MultistepExprHolder itemToRemove) {
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

        /* access modifiers changed from: package-private */
        public int getLength() {
            int count = 0;
            for (MultistepExprHolder next = this; next != null; next = next.m_next) {
                count++;
            }
            return count;
        }

        /* access modifiers changed from: protected */
        public void diagnose() {
            PrintStream printStream = System.err;
            printStream.print("Found multistep iterators: " + getLength() + "  ");
            MultistepExprHolder next = this;
            while (next != null) {
                PrintStream printStream2 = System.err;
                printStream2.print("" + next.m_stepCount);
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

    /* access modifiers changed from: protected */
    public void eleminateRedundent(ElemTemplateElement psuedoVarRecipient, Vector paths) {
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

    /* access modifiers changed from: protected */
    public void eleminateSharedPartialPaths(ElemTemplateElement psuedoVarRecipient, Vector paths) {
        MultistepExprHolder next = createMultistepExprList(paths);
        if (next != null) {
            boolean isGlobal = paths == this.m_absPaths;
            int i = next.m_stepCount - 1;
            while (true) {
                int i2 = i;
                if (i2 >= 1) {
                    MultistepExprHolder list = next;
                    while (next != null && next.m_stepCount >= i2) {
                        list = matchAndEliminatePartialPaths(next, list, isGlobal, i2, psuedoVarRecipient);
                        next = next.m_next;
                    }
                    i = i2 - 1;
                    next = list;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public MultistepExprHolder matchAndEliminatePartialPaths(MultistepExprHolder testee, MultistepExprHolder head, boolean isGlobal, int lengthToTest, ElemTemplateElement varScope) {
        MultistepExprHolder multistepExprHolder = testee;
        boolean z = isGlobal;
        int i = lengthToTest;
        if (multistepExprHolder.m_exprOwner == null) {
            return head;
        }
        WalkingIterator iter1 = (WalkingIterator) multistepExprHolder.m_exprOwner.getExpression();
        if (partialIsVariable(multistepExprHolder, i)) {
            return head;
        }
        MultistepExprHolder matchedPathsTail = null;
        MultistepExprHolder matchedPaths = null;
        MultistepExprHolder matchedPathsTail2 = head;
        while (true) {
            MultistepExprHolder meh = matchedPathsTail2;
            if (meh == null) {
                break;
            }
            if (!(meh == multistepExprHolder || meh.m_exprOwner == null || !stepsEqual(iter1, (WalkingIterator) meh.m_exprOwner.getExpression(), i))) {
                if (matchedPaths == null) {
                    try {
                        matchedPaths = (MultistepExprHolder) testee.clone();
                        multistepExprHolder.m_exprOwner = null;
                    } catch (CloneNotSupportedException e) {
                    }
                    matchedPathsTail = matchedPaths;
                    matchedPathsTail.m_next = null;
                }
                try {
                    matchedPathsTail.m_next = (MultistepExprHolder) meh.clone();
                    meh.m_exprOwner = null;
                } catch (CloneNotSupportedException e2) {
                }
                MultistepExprHolder matchedPathsTail3 = matchedPathsTail.m_next;
                matchedPathsTail3.m_next = null;
                matchedPathsTail = matchedPathsTail3;
            }
            matchedPathsTail2 = meh.m_next;
        }
        int matchCount = 0;
        if (matchedPaths != null) {
            ElemVariable var = createPseudoVarDecl(z ? varScope : findCommonAncestor(matchedPaths), createIteratorFromSteps((WalkingIterator) matchedPaths.m_exprOwner.getExpression(), i), z);
            while (matchedPaths != null) {
                ExpressionOwner owner = matchedPaths.m_exprOwner;
                owner.setExpression(changePartToRef(var.getName(), (WalkingIterator) owner.getExpression(), i, z));
                matchedPaths = matchedPaths.m_next;
                matchCount = matchCount;
            }
        }
        return head;
    }

    /* access modifiers changed from: package-private */
    public boolean partialIsVariable(MultistepExprHolder testee, int lengthToTest) {
        if (1 != lengthToTest || !(((WalkingIterator) testee.m_exprOwner.getExpression()).getFirstWalker() instanceof FilterExprWalker)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void diagnoseLineNumber(Expression expr) {
        ElemTemplateElement e = getElemFromExpression(expr);
        PrintStream printStream = System.err;
        printStream.println("   " + e.getSystemId() + " Line " + e.getLineNumber());
    }

    /* access modifiers changed from: protected */
    public ElemTemplateElement findCommonAncestor(MultistepExprHolder head) {
        int numExprs = head.getLength();
        ElemTemplateElement[] elems = new ElemTemplateElement[numExprs];
        int[] ancestorCounts = new int[numExprs];
        int shortestAncestorCount = 10000;
        MultistepExprHolder next = head;
        for (int i = 0; i < numExprs; i++) {
            ElemTemplateElement elem = getElemFromExpression(next.m_exprOwner.getExpression());
            elems[i] = elem;
            int numAncestors = countAncestors(elem);
            ancestorCounts[i] = numAncestors;
            if (numAncestors < shortestAncestorCount) {
                shortestAncestorCount = numAncestors;
            }
            next = next.m_next;
        }
        for (int i2 = 0; i2 < numExprs; i2++) {
            if (ancestorCounts[i2] > shortestAncestorCount) {
                int numStepCorrection = ancestorCounts[i2] - shortestAncestorCount;
                for (int j = 0; j < numStepCorrection; j++) {
                    elems[i2] = elems[i2].getParentElem();
                }
            }
        }
        int shortestAncestorCount2 = shortestAncestorCount;
        while (true) {
            int shortestAncestorCount3 = shortestAncestorCount2 - 1;
            if (shortestAncestorCount2 >= 0) {
                boolean areEqual = true;
                ElemTemplateElement first = elems[0];
                int i3 = 1;
                while (true) {
                    if (i3 >= numExprs) {
                        break;
                    } else if (first != elems[i3]) {
                        areEqual = false;
                        break;
                    } else {
                        i3++;
                    }
                }
                if (areEqual && isNotSameAsOwner(head, first) && first.canAcceptVariables()) {
                    return first;
                }
                for (int i4 = 0; i4 < numExprs; i4++) {
                    elems[i4] = elems[i4].getParentElem();
                }
                shortestAncestorCount2 = shortestAncestorCount3;
            } else {
                assertion(false, "Could not find common ancestor!!!");
                return null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isNotSameAsOwner(MultistepExprHolder head, ElemTemplateElement ete) {
        for (MultistepExprHolder next = head; next != null; next = next.m_next) {
            if (getElemFromExpression(next.m_exprOwner.getExpression()) == ete) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public int countAncestors(ElemTemplateElement elem) {
        int count = 0;
        while (elem != null) {
            count++;
            elem = elem.getParentElem();
        }
        return count;
    }

    /* access modifiers changed from: protected */
    public void diagnoseMultistepList(int matchCount, int lengthToTest, boolean isGlobal) {
        if (matchCount > 0) {
            PrintStream printStream = System.err;
            printStream.print("Found multistep matches: " + matchCount + ", " + lengthToTest + " length");
            if (isGlobal) {
                System.err.println(" (global)");
            } else {
                System.err.println();
            }
        }
    }

    /* access modifiers changed from: protected */
    public LocPathIterator changePartToRef(QName uniquePseudoVarName, WalkingIterator wi, int numSteps, boolean isGlobal) {
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

    /* access modifiers changed from: protected */
    public WalkingIterator createIteratorFromSteps(WalkingIterator wi, int numSteps) {
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

    /* access modifiers changed from: protected */
    public boolean stepsEqual(WalkingIterator iter1, WalkingIterator iter2, int numSteps) {
        AxesWalker aw1 = iter1.getFirstWalker();
        boolean z = false;
        AxesWalker aw2 = iter2.getFirstWalker();
        AxesWalker aw12 = aw1;
        for (int i = 0; i < numSteps; i++) {
            if (aw12 == null || aw2 == null || !aw12.deepEquals(aw2)) {
                return false;
            }
            aw12 = aw12.getNextWalker();
            aw2 = aw2.getNextWalker();
        }
        if (!(aw12 == null && aw2 == null)) {
            z = true;
        }
        assertion(z, "Total match is incorrect!");
        return true;
    }

    /* access modifiers changed from: protected */
    public MultistepExprHolder createMultistepExprList(Vector paths) {
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

    /* access modifiers changed from: protected */
    public int findAndEliminateRedundant(int start, int firstOccuranceIndex, ExpressionOwner firstOccuranceOwner, ElemTemplateElement psuedoVarRecipient, Vector paths) throws DOMException {
        ExpressionOwner expressionOwner = firstOccuranceOwner;
        Vector vector = paths;
        int numPathsFound = 0;
        int n = paths.size();
        Expression expr1 = firstOccuranceOwner.getExpression();
        boolean isGlobal = vector == this.m_absPaths;
        LocPathIterator lpi = (LocPathIterator) expr1;
        int stepCount = countSteps(lpi);
        MultistepExprHolder tail = null;
        MultistepExprHolder head = null;
        for (int j = start; j < n; j++) {
            ExpressionOwner owner2 = (ExpressionOwner) vector.elementAt(j);
            if (owner2 != null && owner2.getExpression().deepEquals(lpi)) {
                if (head == null) {
                    head = new MultistepExprHolder(expressionOwner, stepCount, null);
                    tail = head;
                    numPathsFound++;
                }
                tail.m_next = new MultistepExprHolder(owner2, stepCount, null);
                MultistepExprHolder tail2 = tail.m_next;
                vector.setElementAt(null, j);
                numPathsFound++;
                tail = tail2;
            }
        }
        if (numPathsFound == 0 && isGlobal) {
            head = new MultistepExprHolder(expressionOwner, stepCount, null);
            numPathsFound++;
        }
        if (head != null) {
            ElemTemplateElement root = isGlobal ? psuedoVarRecipient : findCommonAncestor(head);
            ElemVariable var = createPseudoVarDecl(root, (LocPathIterator) head.m_exprOwner.getExpression(), isGlobal);
            QName uniquePseudoVarName = var.getName();
            while (head != null) {
                changeToVarRef(uniquePseudoVarName, head.m_exprOwner, vector, root);
                head = head.m_next;
            }
            vector.setElementAt(var.getSelect(), firstOccuranceIndex);
        } else {
            int i = firstOccuranceIndex;
        }
        return numPathsFound;
    }

    /* access modifiers changed from: protected */
    public int oldFindAndEliminateRedundant(int start, int firstOccuranceIndex, ExpressionOwner firstOccuranceOwner, ElemTemplateElement psuedoVarRecipient, Vector paths) throws DOMException {
        boolean foundFirst;
        int i = firstOccuranceIndex;
        ExpressionOwner expressionOwner = firstOccuranceOwner;
        ElemTemplateElement elemTemplateElement = psuedoVarRecipient;
        Vector vector = paths;
        boolean foundFirst2 = false;
        int numPathsFound = 0;
        int n = paths.size();
        Expression expr1 = firstOccuranceOwner.getExpression();
        boolean isGlobal = vector == this.m_absPaths;
        LocPathIterator lpi = (LocPathIterator) expr1;
        QName uniquePseudoVarName = null;
        for (int j = start; j < n; j++) {
            ExpressionOwner owner2 = (ExpressionOwner) vector.elementAt(j);
            if (owner2 != null && owner2.getExpression().deepEquals(lpi)) {
                if (!foundFirst2) {
                    ElemVariable var = createPseudoVarDecl(elemTemplateElement, lpi, isGlobal);
                    if (var == null) {
                        return 0;
                    }
                    uniquePseudoVarName = var.getName();
                    changeToVarRef(uniquePseudoVarName, expressionOwner, vector, elemTemplateElement);
                    foundFirst = true;
                    vector.setElementAt(var.getSelect(), i);
                    numPathsFound++;
                } else {
                    foundFirst = foundFirst2;
                }
                changeToVarRef(uniquePseudoVarName, owner2, vector, elemTemplateElement);
                vector.setElementAt(null, j);
                numPathsFound++;
                foundFirst2 = foundFirst;
            }
        }
        if (numPathsFound == 0 && vector == this.m_absPaths) {
            ElemVariable var2 = createPseudoVarDecl(elemTemplateElement, lpi, true);
            if (var2 == null) {
                return 0;
            }
            changeToVarRef(var2.getName(), expressionOwner, vector, elemTemplateElement);
            vector.setElementAt(var2.getSelect(), i);
            numPathsFound++;
        }
        return numPathsFound;
    }

    /* access modifiers changed from: protected */
    public int countSteps(LocPathIterator lpi) {
        if (!(lpi instanceof WalkingIterator)) {
            return 1;
        }
        int count = 0;
        for (AxesWalker aw = ((WalkingIterator) lpi).getFirstWalker(); aw != null; aw = aw.getNextWalker()) {
            count++;
        }
        return count;
    }

    /* access modifiers changed from: protected */
    public void changeToVarRef(QName varName, ExpressionOwner owner, Vector paths, ElemTemplateElement psuedoVarRecipient) {
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

    /* access modifiers changed from: protected */
    public ElemVariable createPseudoVarDecl(ElemTemplateElement psuedoVarRecipient, LocPathIterator lpi, boolean isGlobal) throws DOMException {
        QName uniquePseudoVarName = new QName(PSUEDOVARNAMESPACE, "#" + getPseudoVarID());
        if (isGlobal) {
            return createGlobalPseudoVarDecl(uniquePseudoVarName, (StylesheetRoot) psuedoVarRecipient, lpi);
        }
        return createLocalPseudoVarDecl(uniquePseudoVarName, psuedoVarRecipient, lpi);
    }

    /* access modifiers changed from: protected */
    public ElemVariable createGlobalPseudoVarDecl(QName uniquePseudoVarName, StylesheetRoot stylesheetRoot, LocPathIterator lpi) throws DOMException {
        ElemVariable psuedoVar = new ElemVariable();
        psuedoVar.setIsTopLevel(true);
        psuedoVar.setSelect(new XPath(lpi));
        psuedoVar.setName(uniquePseudoVarName);
        Vector globalVars = stylesheetRoot.getVariablesAndParamsComposed();
        psuedoVar.setIndex(globalVars.size());
        globalVars.addElement(psuedoVar);
        return psuedoVar;
    }

    /* access modifiers changed from: protected */
    public ElemVariable createLocalPseudoVarDecl(QName uniquePseudoVarName, ElemTemplateElement psuedoVarRecipient, LocPathIterator lpi) throws DOMException {
        ElemVariable psuedoVar = new ElemVariablePsuedo();
        psuedoVar.setSelect(new XPath(lpi));
        psuedoVar.setName(uniquePseudoVarName);
        ElemVariable var = addVarDeclToElem(psuedoVarRecipient, lpi, psuedoVar);
        lpi.exprSetParent(var);
        return var;
    }

    /* access modifiers changed from: protected */
    public ElemVariable addVarDeclToElem(ElemTemplateElement psuedoVarRecipient, LocPathIterator lpi, ElemVariable psuedoVar) throws DOMException {
        ElemTemplateElement ete = psuedoVarRecipient.getFirstChildElem();
        lpi.callVisitors(null, this.m_varNameCollector);
        if (this.m_varNameCollector.getVarCount() > 0) {
            ElemVariable varElem = getPrevVariableElem(getElemFromExpression(lpi));
            while (true) {
                if (varElem == null) {
                    break;
                } else if (this.m_varNameCollector.doesOccur(varElem.getName())) {
                    psuedoVarRecipient = varElem.getParentElem();
                    ete = varElem.getNextSiblingElem();
                    break;
                } else {
                    varElem = getPrevVariableElem(varElem);
                }
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

    /* access modifiers changed from: protected */
    public boolean isParam(ExpressionNode expr) {
        while (expr != null && !(expr instanceof ElemTemplateElement)) {
            expr = expr.exprGetParent();
        }
        if (expr != null) {
            for (ElemTemplateElement ete = (ElemTemplateElement) expr; ete != null; ete = ete.getParentElem()) {
                int type = ete.getXSLToken();
                if (type == 19 || type == 25) {
                    return false;
                }
                if (type == 41) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public ElemVariable getPrevVariableElem(ElemTemplateElement elem) {
        int type;
        do {
            ElemTemplateElement prevElementWithinContext = getPrevElementWithinContext(elem);
            elem = prevElementWithinContext;
            if (prevElementWithinContext != null) {
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

    /* access modifiers changed from: protected */
    public ElemTemplateElement getPrevElementWithinContext(ElemTemplateElement elem) {
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

    /* access modifiers changed from: protected */
    public ElemTemplateElement getElemFromExpression(Expression expr) {
        for (ExpressionNode parent = expr.exprGetParent(); parent != null; parent = parent.exprGetParent()) {
            if (parent instanceof ElemTemplateElement) {
                return (ElemTemplateElement) parent;
            }
        }
        throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_ASSERT_NO_TEMPLATE_PARENT, null));
    }

    public boolean isAbsolute(LocPathIterator path) {
        int analysis = path.getAnalysisBits();
        boolean isAbs = WalkerFactory.isSet(analysis, WalkerFactory.BIT_ROOT) || WalkerFactory.isSet(analysis, WalkerFactory.BIT_ANY_DESCENDANT_FROM_ROOT);
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
        if (elem.getXSLToken() != 19) {
            return true;
        }
        return visitInstruction(elem);
    }

    public boolean visitInstruction(ElemTemplateElement elem) {
        int type = elem.getXSLToken();
        if (type == 17 || type == 19 || type == 28) {
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
        } else if (type != 35 && type != 64) {
            return true;
        } else {
            boolean savedIsSame = this.m_isSameContext;
            this.m_isSameContext = false;
            elem.callChildVisitors(this);
            this.m_isSameContext = savedIsSame;
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void diagnoseNumPaths(Vector paths, int numPathsEliminated, int numUniquePathsEliminated) {
        if (numPathsEliminated <= 0) {
            return;
        }
        if (paths == this.m_paths) {
            PrintStream printStream = System.err;
            printStream.println("Eliminated " + numPathsEliminated + " total paths!");
            PrintStream printStream2 = System.err;
            printStream2.println("Consolodated " + numUniquePathsEliminated + " redundent paths!");
            return;
        }
        PrintStream printStream3 = System.err;
        printStream3.println("Eliminated " + numPathsEliminated + " total global paths!");
        PrintStream printStream4 = System.err;
        printStream4.println("Consolodated " + numUniquePathsEliminated + " redundent global paths!");
    }

    private final void assertIsLocPathIterator(Expression expr1, ExpressionOwner eo) throws RuntimeException {
        String errMsg;
        if (!(expr1 instanceof LocPathIterator)) {
            if (expr1 instanceof Variable) {
                errMsg = "Programmer's assertion: expr1 not an iterator: " + ((Variable) expr1).getQName();
            } else {
                errMsg = "Programmer's assertion: expr1 not an iterator: " + expr1.getClass().getName();
            }
            throw new RuntimeException(errMsg + ", " + eo.getClass().getName() + " " + expr1.exprGetParent());
        }
    }

    private static void validateNewAddition(Vector paths, ExpressionOwner owner, LocPathIterator path) throws RuntimeException {
        assertion(owner.getExpression() == path, "owner.getExpression() != path!!!");
        int n = paths.size();
        for (int i = 0; i < n; i++) {
            ExpressionOwner ew = (ExpressionOwner) paths.elementAt(i);
            assertion(ew != owner, "duplicate owner on the list!!!");
            assertion(ew.getExpression() != path, "duplicate expression on the list!!!");
        }
    }

    protected static void assertion(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR, new Object[]{msg}));
        }
    }
}

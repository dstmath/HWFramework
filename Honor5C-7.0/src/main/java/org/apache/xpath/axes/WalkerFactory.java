package org.apache.xpath.axes;

import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.Expression;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.compiler.OpCodes;
import org.apache.xpath.compiler.OpMap;
import org.apache.xpath.compiler.PsuedoNames;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.patterns.ContextMatchStepPattern;
import org.apache.xpath.patterns.FunctionPattern;
import org.apache.xpath.patterns.StepPattern;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class WalkerFactory {
    public static final int BITMASK_TRAVERSES_OUTSIDE_SUBTREE = 234381312;
    public static final int BITS_COUNT = 255;
    public static final int BITS_RESERVED = 3840;
    public static final int BIT_ANCESTOR = 8192;
    public static final int BIT_ANCESTOR_OR_SELF = 16384;
    public static final int BIT_ANY_DESCENDANT_FROM_ROOT = 536870912;
    public static final int BIT_ATTRIBUTE = 32768;
    public static final int BIT_BACKWARDS_SELF = 268435456;
    public static final int BIT_CHILD = 65536;
    public static final int BIT_DESCENDANT = 131072;
    public static final int BIT_DESCENDANT_OR_SELF = 262144;
    public static final int BIT_FILTER = 67108864;
    public static final int BIT_FOLLOWING = 524288;
    public static final int BIT_FOLLOWING_SIBLING = 1048576;
    public static final int BIT_MATCH_PATTERN = Integer.MIN_VALUE;
    public static final int BIT_NAMESPACE = 2097152;
    public static final int BIT_NODETEST_ANY = 1073741824;
    public static final int BIT_PARENT = 4194304;
    public static final int BIT_PRECEDING = 8388608;
    public static final int BIT_PRECEDING_SIBLING = 16777216;
    public static final int BIT_PREDICATE = 4096;
    public static final int BIT_ROOT = 134217728;
    public static final int BIT_SELF = 33554432;
    static final boolean DEBUG_ITERATOR_CREATION = false;
    static final boolean DEBUG_PATTERN_CREATION = false;
    static final boolean DEBUG_WALKER_CREATION = false;

    static AxesWalker loadOneWalker(WalkingIterator lpi, Compiler compiler, int stepOpCodePos) throws TransformerException {
        int stepType = compiler.getOp(stepOpCodePos);
        if (stepType == -1) {
            return null;
        }
        AxesWalker firstWalker = createDefaultWalker(compiler, stepType, lpi, 0);
        firstWalker.init(compiler, stepOpCodePos, stepType);
        return firstWalker;
    }

    static AxesWalker loadWalkers(WalkingIterator lpi, Compiler compiler, int stepOpCodePos, int stepIndex) throws TransformerException {
        AxesWalker firstWalker = null;
        AxesWalker prevWalker = null;
        int analysis = analyze(compiler, stepOpCodePos, stepIndex);
        do {
            int stepType = compiler.getOp(stepOpCodePos);
            if (-1 == stepType) {
                break;
            }
            AxesWalker walker = createDefaultWalker(compiler, stepOpCodePos, lpi, analysis);
            walker.init(compiler, stepOpCodePos, stepType);
            walker.exprSetParent(lpi);
            if (firstWalker == null) {
                firstWalker = walker;
            } else {
                prevWalker.setNextWalker(walker);
                walker.setPrevWalker(prevWalker);
            }
            prevWalker = walker;
            stepOpCodePos = compiler.getNextStepPos(stepOpCodePos);
        } while (stepOpCodePos >= 0);
        return firstWalker;
    }

    public static boolean isSet(int analysis, int bits) {
        return (analysis & bits) != 0;
    }

    public static void diagnoseIterator(String name, int analysis, Compiler compiler) {
        System.out.println(compiler.toString() + ", " + name + ", " + Integer.toBinaryString(analysis) + ", " + getAnalysisString(analysis));
    }

    public static DTMIterator newDTMIterator(Compiler compiler, int opPos, boolean isTopLevel) throws TransformerException {
        DTMIterator iter;
        int firstStepPos = OpMap.getFirstChildPos(opPos);
        int analysis = analyze(compiler, firstStepPos, 0);
        boolean isOneStep = isOneStep(analysis);
        if (isOneStep && walksSelfOnly(analysis) && isWild(analysis) && !hasPredicate(analysis)) {
            iter = new SelfIteratorNoPredicate(compiler, opPos, analysis);
        } else if (walksChildrenOnly(analysis) && isOneStep) {
            if (!isWild(analysis) || hasPredicate(analysis)) {
                iter = new ChildTestIterator(compiler, opPos, analysis);
            } else {
                iter = new ChildIterator(compiler, opPos, analysis);
            }
        } else if (isOneStep && walksAttributes(analysis)) {
            iter = new AttributeIterator(compiler, opPos, analysis);
        } else if (!isOneStep || walksFilteredList(analysis)) {
            if (isOptimizableForDescendantIterator(compiler, firstStepPos, 0)) {
                iter = new DescendantIterator(compiler, opPos, analysis);
            } else if (isNaturalDocOrder(compiler, firstStepPos, 0, analysis)) {
                iter = new WalkingIterator(compiler, opPos, analysis, true);
            } else {
                iter = new WalkingIteratorSorted(compiler, opPos, analysis, true);
            }
        } else if (walksNamespaces(analysis) || !(walksInDocOrder(analysis) || isSet(analysis, BIT_PARENT))) {
            iter = new OneStepIterator(compiler, opPos, analysis);
        } else {
            iter = new OneStepIteratorForward(compiler, opPos, analysis);
        }
        if (iter instanceof LocPathIterator) {
            ((LocPathIterator) iter).setIsTopLevel(isTopLevel);
        }
        return iter;
    }

    public static int getAxisFromStep(Compiler compiler, int stepOpCodePos) throws TransformerException {
        switch (compiler.getOp(stepOpCodePos)) {
            case OpCodes.OP_VARIABLE /*22*/:
            case OpCodes.OP_GROUP /*23*/:
            case OpCodes.OP_EXTFUNCTION /*24*/:
            case OpCodes.OP_FUNCTION /*25*/:
                return 20;
            case OpCodes.FROM_ANCESTORS /*37*/:
                return 0;
            case OpCodes.FROM_ANCESTORS_OR_SELF /*38*/:
                return 1;
            case OpCodes.FROM_ATTRIBUTES /*39*/:
                return 2;
            case OpCodes.FROM_CHILDREN /*40*/:
                return 3;
            case OpCodes.FROM_DESCENDANTS /*41*/:
                return 4;
            case OpCodes.FROM_DESCENDANTS_OR_SELF /*42*/:
                return 5;
            case OpCodes.FROM_FOLLOWING /*43*/:
                return 6;
            case OpCodes.FROM_FOLLOWING_SIBLINGS /*44*/:
                return 7;
            case OpCodes.FROM_PARENT /*45*/:
                return 10;
            case OpCodes.FROM_PRECEDING /*46*/:
                return 11;
            case OpCodes.FROM_PRECEDING_SIBLINGS /*47*/:
                return 12;
            case OpCodes.FROM_SELF /*48*/:
                return 13;
            case OpCodes.FROM_NAMESPACE /*49*/:
                return 9;
            case OpCodes.FROM_ROOT /*50*/:
                return 19;
            default:
                throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER, new Object[]{Integer.toString(stepType)}));
        }
    }

    public static int getAnalysisBitFromAxes(int axis) {
        switch (axis) {
            case FunctionTable.FUNC_CURRENT /*0*/:
                return BIT_ANCESTOR;
            case OpCodes.OP_XPATH /*1*/:
                return BIT_ANCESTOR_OR_SELF;
            case OpCodes.OP_OR /*2*/:
                return BIT_ATTRIBUTE;
            case OpCodes.OP_AND /*3*/:
                return BIT_CHILD;
            case OpCodes.OP_NOTEQUALS /*4*/:
                return BIT_DESCENDANT;
            case OpCodes.OP_EQUALS /*5*/:
                return BIT_DESCENDANT_OR_SELF;
            case OpCodes.OP_LTE /*6*/:
                return BIT_FOLLOWING;
            case OpCodes.OP_LT /*7*/:
                return BIT_FOLLOWING_SIBLING;
            case OpCodes.OP_GTE /*8*/:
            case OpCodes.OP_GT /*9*/:
                return BIT_NAMESPACE;
            case OpCodes.OP_PLUS /*10*/:
                return BIT_PARENT;
            case OpCodes.OP_MINUS /*11*/:
                return BIT_PRECEDING;
            case OpCodes.OP_MULT /*12*/:
                return BIT_PRECEDING_SIBLING;
            case OpCodes.OP_DIV /*13*/:
                return BIT_SELF;
            case OpCodes.OP_MOD /*14*/:
                return BIT_DESCENDANT_OR_SELF;
            case OpCodes.OP_NEG /*16*/:
            case OpCodes.OP_STRING /*17*/:
            case OpCodes.OP_BOOL /*18*/:
                return BIT_ANY_DESCENDANT_FROM_ROOT;
            case OpCodes.OP_NUMBER /*19*/:
                return BIT_ROOT;
            case OpCodes.OP_UNION /*20*/:
                return BIT_FILTER;
            default:
                return BIT_FILTER;
        }
    }

    static boolean functionProximateOrContainsProximate(Compiler compiler, int opPos) {
        int endFunc = (compiler.getOp(opPos + 1) + opPos) - 1;
        opPos = OpMap.getFirstChildPos(opPos);
        switch (compiler.getOp(opPos)) {
            case OpCodes.OP_XPATH /*1*/:
            case OpCodes.OP_OR /*2*/:
                return true;
            default:
                int i = 0;
                int p = opPos + 1;
                while (p < endFunc) {
                    int innerExprOpPos = p + 2;
                    int argOp = compiler.getOp(innerExprOpPos);
                    if (isProximateInnerExpr(compiler, innerExprOpPos)) {
                        return true;
                    }
                    p = compiler.getNextOpPos(p);
                    i++;
                }
                return false;
        }
    }

    static boolean isProximateInnerExpr(Compiler compiler, int opPos) {
        int op = compiler.getOp(opPos);
        int innerExprOpPos = opPos + 2;
        switch (op) {
            case OpCodes.OP_EQUALS /*5*/:
            case OpCodes.OP_LTE /*6*/:
            case OpCodes.OP_LT /*7*/:
            case OpCodes.OP_GTE /*8*/:
            case OpCodes.OP_GT /*9*/:
                int leftPos = OpMap.getFirstChildPos(op);
                int rightPos = compiler.getNextOpPos(leftPos);
                if (isProximateInnerExpr(compiler, leftPos) || isProximateInnerExpr(compiler, rightPos)) {
                    return true;
                }
            case OpCodes.OP_LITERAL /*21*/:
            case OpCodes.OP_VARIABLE /*22*/:
            case OpCodes.OP_NUMBERLIT /*27*/:
            case OpCodes.OP_LOCATIONPATH /*28*/:
                break;
            case OpCodes.OP_FUNCTION /*25*/:
                if (functionProximateOrContainsProximate(compiler, opPos)) {
                    return true;
                }
                break;
            case OpCodes.OP_ARGUMENT /*26*/:
                if (isProximateInnerExpr(compiler, innerExprOpPos)) {
                    return true;
                }
                break;
            default:
                return true;
        }
        return false;
    }

    public static boolean mightBeProximate(Compiler compiler, int opPos, int stepType) throws TransformerException {
        int argLen;
        switch (stepType) {
            case OpCodes.OP_VARIABLE /*22*/:
            case OpCodes.OP_GROUP /*23*/:
            case OpCodes.OP_EXTFUNCTION /*24*/:
            case OpCodes.OP_FUNCTION /*25*/:
                argLen = compiler.getArgLength(opPos);
                break;
            default:
                argLen = compiler.getArgLengthOfStep(opPos);
                break;
        }
        int predPos = compiler.getFirstPredicateOpPos(opPos);
        int count = 0;
        while (29 == compiler.getOp(predPos)) {
            count++;
            int innerExprOpPos = predPos + 2;
            switch (compiler.getOp(innerExprOpPos)) {
                case OpCodes.OP_EQUALS /*5*/:
                case OpCodes.OP_LTE /*6*/:
                case OpCodes.OP_LT /*7*/:
                case OpCodes.OP_GTE /*8*/:
                case OpCodes.OP_GT /*9*/:
                    int leftPos = OpMap.getFirstChildPos(innerExprOpPos);
                    int rightPos = compiler.getNextOpPos(leftPos);
                    if (!isProximateInnerExpr(compiler, leftPos) && !isProximateInnerExpr(compiler, rightPos)) {
                        break;
                    }
                    return true;
                case OpCodes.OP_NUMBER /*19*/:
                case OpCodes.OP_NUMBERLIT /*27*/:
                    return true;
                case OpCodes.OP_VARIABLE /*22*/:
                    return true;
                case OpCodes.OP_FUNCTION /*25*/:
                    if (!functionProximateOrContainsProximate(compiler, innerExprOpPos)) {
                        break;
                    }
                    return true;
                case OpCodes.OP_LOCATIONPATH /*28*/:
                    break;
                default:
                    return true;
            }
            predPos = compiler.getNextOpPos(predPos);
        }
        return false;
    }

    private static boolean isOptimizableForDescendantIterator(Compiler compiler, int stepOpCodePos, int stepIndex) throws TransformerException {
        int stepCount = 0;
        boolean foundDorDS = false;
        boolean foundSelf = false;
        boolean foundDS = false;
        int nodeTestType = OpCodes.NODETYPE_NODE;
        while (true) {
            int stepType = compiler.getOp(stepOpCodePos);
            if (-1 != stepType) {
                if (nodeTestType != OpCodes.NODETYPE_NODE && nodeTestType != 35) {
                    return false;
                }
                stepCount++;
                if (stepCount > 3 || mightBeProximate(compiler, stepOpCodePos, stepType)) {
                    return false;
                }
                switch (stepType) {
                    case OpCodes.OP_VARIABLE /*22*/:
                    case OpCodes.OP_GROUP /*23*/:
                    case OpCodes.OP_EXTFUNCTION /*24*/:
                    case OpCodes.OP_FUNCTION /*25*/:
                    case OpCodes.FROM_ANCESTORS /*37*/:
                    case OpCodes.FROM_ANCESTORS_OR_SELF /*38*/:
                    case OpCodes.FROM_ATTRIBUTES /*39*/:
                    case OpCodes.FROM_FOLLOWING /*43*/:
                    case OpCodes.FROM_FOLLOWING_SIBLINGS /*44*/:
                    case OpCodes.FROM_PARENT /*45*/:
                    case OpCodes.FROM_PRECEDING /*46*/:
                    case OpCodes.FROM_PRECEDING_SIBLINGS /*47*/:
                    case OpCodes.FROM_NAMESPACE /*49*/:
                    case OpCodes.MATCH_ATTRIBUTE /*51*/:
                    case OpCodes.MATCH_ANY_ANCESTOR /*52*/:
                    case OpCodes.MATCH_IMMEDIATE_ANCESTOR /*53*/:
                        return false;
                    case OpCodes.FROM_CHILDREN /*40*/:
                        if (!(foundDS || (foundDorDS && foundSelf))) {
                            return false;
                        }
                    case OpCodes.FROM_DESCENDANTS /*41*/:
                        break;
                    case OpCodes.FROM_DESCENDANTS_OR_SELF /*42*/:
                        foundDS = true;
                        break;
                    case OpCodes.FROM_SELF /*48*/:
                        if (1 == stepCount) {
                            foundSelf = true;
                            break;
                        }
                        return false;
                    case OpCodes.FROM_ROOT /*50*/:
                        if (1 != stepCount) {
                            return false;
                        }
                        break;
                    default:
                        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER, new Object[]{Integer.toString(stepType)}));
                }
                if (3 == stepCount) {
                    return false;
                }
                foundDorDS = true;
                nodeTestType = compiler.getStepTestType(stepOpCodePos);
                int nextStepOpCodePos = compiler.getNextStepPos(stepOpCodePos);
                if (nextStepOpCodePos >= 0) {
                    if (-1 != compiler.getOp(nextStepOpCodePos) && compiler.countPredicates(stepOpCodePos) > 0) {
                        return false;
                    }
                    stepOpCodePos = nextStepOpCodePos;
                }
            }
            return true;
        }
    }

    private static int analyze(Compiler compiler, int stepOpCodePos, int stepIndex) throws TransformerException {
        int stepCount = 0;
        int analysisResult = 0;
        do {
            int stepType = compiler.getOp(stepOpCodePos);
            if (-1 != stepType) {
                stepCount++;
                if (analyzePredicate(compiler, stepOpCodePos, stepType)) {
                    analysisResult |= BIT_PREDICATE;
                }
                switch (stepType) {
                    case OpCodes.OP_VARIABLE /*22*/:
                    case OpCodes.OP_GROUP /*23*/:
                    case OpCodes.OP_EXTFUNCTION /*24*/:
                    case OpCodes.OP_FUNCTION /*25*/:
                        analysisResult |= BIT_FILTER;
                        break;
                    case OpCodes.FROM_ANCESTORS /*37*/:
                        analysisResult |= BIT_ANCESTOR;
                        break;
                    case OpCodes.FROM_ANCESTORS_OR_SELF /*38*/:
                        analysisResult |= BIT_ANCESTOR_OR_SELF;
                        break;
                    case OpCodes.FROM_ATTRIBUTES /*39*/:
                        analysisResult |= BIT_ATTRIBUTE;
                        break;
                    case OpCodes.FROM_CHILDREN /*40*/:
                        analysisResult |= BIT_CHILD;
                        break;
                    case OpCodes.FROM_DESCENDANTS /*41*/:
                        analysisResult |= BIT_DESCENDANT;
                        break;
                    case OpCodes.FROM_DESCENDANTS_OR_SELF /*42*/:
                        if (2 == stepCount && BIT_ROOT == analysisResult) {
                            analysisResult |= BIT_ANY_DESCENDANT_FROM_ROOT;
                        }
                        analysisResult |= BIT_DESCENDANT_OR_SELF;
                        break;
                    case OpCodes.FROM_FOLLOWING /*43*/:
                        analysisResult |= BIT_FOLLOWING;
                        break;
                    case OpCodes.FROM_FOLLOWING_SIBLINGS /*44*/:
                        analysisResult |= BIT_FOLLOWING_SIBLING;
                        break;
                    case OpCodes.FROM_PARENT /*45*/:
                        analysisResult |= BIT_PARENT;
                        break;
                    case OpCodes.FROM_PRECEDING /*46*/:
                        analysisResult |= BIT_PRECEDING;
                        break;
                    case OpCodes.FROM_PRECEDING_SIBLINGS /*47*/:
                        analysisResult |= BIT_PRECEDING_SIBLING;
                        break;
                    case OpCodes.FROM_SELF /*48*/:
                        analysisResult |= BIT_SELF;
                        break;
                    case OpCodes.FROM_NAMESPACE /*49*/:
                        analysisResult |= BIT_NAMESPACE;
                        break;
                    case OpCodes.FROM_ROOT /*50*/:
                        analysisResult |= BIT_ROOT;
                        break;
                    case OpCodes.MATCH_ATTRIBUTE /*51*/:
                        analysisResult |= -2147450880;
                        break;
                    case OpCodes.MATCH_ANY_ANCESTOR /*52*/:
                        analysisResult |= -2147475456;
                        break;
                    case OpCodes.MATCH_IMMEDIATE_ANCESTOR /*53*/:
                        analysisResult |= -2143289344;
                        break;
                    default:
                        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER, new Object[]{Integer.toString(stepType)}));
                }
                if (OpCodes.NODETYPE_NODE == compiler.getOp(stepOpCodePos + 3)) {
                    analysisResult |= BIT_NODETEST_ANY;
                }
                stepOpCodePos = compiler.getNextStepPos(stepOpCodePos);
            }
            return analysisResult | (stepCount & BITS_COUNT);
        } while (stepOpCodePos >= 0);
        return analysisResult | (stepCount & BITS_COUNT);
    }

    public static boolean isDownwardAxisOfMany(int axis) {
        if (5 == axis || 4 == axis || 6 == axis || 11 == axis) {
            return true;
        }
        return false;
    }

    static StepPattern loadSteps(MatchPatternIterator mpi, Compiler compiler, int stepOpCodePos, int stepIndex) throws TransformerException {
        StepPattern step = null;
        StepPattern firstStep = null;
        StepPattern prevStep = null;
        int analysis = analyze(compiler, stepOpCodePos, stepIndex);
        while (-1 != compiler.getOp(stepOpCodePos)) {
            step = createDefaultStepPattern(compiler, stepOpCodePos, mpi, analysis, firstStep, prevStep);
            if (firstStep == null) {
                firstStep = step;
            } else {
                step.setRelativePathPattern(prevStep);
            }
            prevStep = step;
            stepOpCodePos = compiler.getNextStepPos(stepOpCodePos);
            if (stepOpCodePos < 0) {
                break;
            }
        }
        int axis = 13;
        StepPattern tail = step;
        StepPattern pat = step;
        while (pat != null) {
            int nextAxis = pat.getAxis();
            pat.setAxis(axis);
            int whatToShow = pat.getWhatToShow();
            if (whatToShow == 2 || whatToShow == BIT_PREDICATE) {
                int newAxis = whatToShow == 2 ? 2 : 9;
                if (isDownwardAxisOfMany(axis)) {
                    StepPattern attrPat = new StepPattern(whatToShow, pat.getNamespace(), pat.getLocalName(), newAxis, 0);
                    XNumber score = pat.getStaticScore();
                    pat.setNamespace(null);
                    pat.setLocalName(PsuedoNames.PSEUDONAME_OTHER);
                    attrPat.setPredicates(pat.getPredicates());
                    pat.setPredicates(null);
                    pat.setWhatToShow(1);
                    StepPattern rel = pat.getRelativePathPattern();
                    pat.setRelativePathPattern(attrPat);
                    attrPat.setRelativePathPattern(rel);
                    attrPat.setStaticScore(score);
                    if (11 == pat.getAxis()) {
                        pat.setAxis(15);
                    } else if (4 == pat.getAxis()) {
                        pat.setAxis(5);
                    }
                    pat = attrPat;
                } else if (3 == pat.getAxis()) {
                    pat.setAxis(2);
                }
            }
            axis = nextAxis;
            tail = pat;
            pat = pat.getRelativePathPattern();
        }
        if (axis < 16) {
            StepPattern contextMatchStepPattern = new ContextMatchStepPattern(axis, 13);
            score = tail.getStaticScore();
            tail.setRelativePathPattern(contextMatchStepPattern);
            tail.setStaticScore(score);
            contextMatchStepPattern.setStaticScore(score);
        }
        return step;
    }

    private static StepPattern createDefaultStepPattern(Compiler compiler, int opPos, MatchPatternIterator mpi, int analysis, StepPattern tail, StepPattern head) throws TransformerException {
        int axis;
        int predicateAxis;
        int stepType = compiler.getOp(opPos);
        int whatToShow = compiler.getWhatToShow(opPos);
        StepPattern ai = null;
        switch (stepType) {
            case OpCodes.OP_VARIABLE /*22*/:
            case OpCodes.OP_GROUP /*23*/:
            case OpCodes.OP_EXTFUNCTION /*24*/:
            case OpCodes.OP_FUNCTION /*25*/:
                Expression expr;
                switch (stepType) {
                    case OpCodes.OP_VARIABLE /*22*/:
                    case OpCodes.OP_GROUP /*23*/:
                    case OpCodes.OP_EXTFUNCTION /*24*/:
                    case OpCodes.OP_FUNCTION /*25*/:
                        expr = compiler.compile(opPos);
                        break;
                    default:
                        expr = compiler.compile(opPos + 2);
                        break;
                }
                axis = 20;
                predicateAxis = 20;
                ai = new FunctionPattern(expr, 20, 20);
                break;
            case OpCodes.FROM_ANCESTORS /*37*/:
                axis = 4;
                predicateAxis = 0;
                break;
            case OpCodes.FROM_ANCESTORS_OR_SELF /*38*/:
                axis = 5;
                predicateAxis = 1;
                break;
            case OpCodes.FROM_ATTRIBUTES /*39*/:
                axis = 10;
                predicateAxis = 2;
                break;
            case OpCodes.FROM_CHILDREN /*40*/:
                axis = 10;
                predicateAxis = 3;
                break;
            case OpCodes.FROM_DESCENDANTS /*41*/:
                axis = 0;
                predicateAxis = 4;
                break;
            case OpCodes.FROM_DESCENDANTS_OR_SELF /*42*/:
                axis = 1;
                predicateAxis = 5;
                break;
            case OpCodes.FROM_FOLLOWING /*43*/:
                axis = 11;
                predicateAxis = 6;
                break;
            case OpCodes.FROM_FOLLOWING_SIBLINGS /*44*/:
                axis = 12;
                predicateAxis = 7;
                break;
            case OpCodes.FROM_PARENT /*45*/:
                axis = 3;
                predicateAxis = 10;
                break;
            case OpCodes.FROM_PRECEDING /*46*/:
                axis = 6;
                predicateAxis = 11;
                break;
            case OpCodes.FROM_PRECEDING_SIBLINGS /*47*/:
                axis = 7;
                predicateAxis = 12;
                break;
            case OpCodes.FROM_SELF /*48*/:
                axis = 13;
                predicateAxis = 13;
                break;
            case OpCodes.FROM_NAMESPACE /*49*/:
                axis = 10;
                predicateAxis = 9;
                break;
            case OpCodes.FROM_ROOT /*50*/:
                axis = 19;
                predicateAxis = 19;
                ai = new StepPattern(1280, 19, 19);
                break;
            default:
                throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER, new Object[]{Integer.toString(stepType)}));
        }
        if (ai == null) {
            ai = new StepPattern(compiler.getWhatToShow(opPos), compiler.getStepNS(opPos), compiler.getStepLocalName(opPos), axis, predicateAxis);
        }
        ai.setPredicates(compiler.getCompiledPredicates(compiler.getFirstPredicateOpPos(opPos)));
        return ai;
    }

    static boolean analyzePredicate(Compiler compiler, int opPos, int stepType) throws TransformerException {
        int argLen;
        switch (stepType) {
            case OpCodes.OP_VARIABLE /*22*/:
            case OpCodes.OP_GROUP /*23*/:
            case OpCodes.OP_EXTFUNCTION /*24*/:
            case OpCodes.OP_FUNCTION /*25*/:
                argLen = compiler.getArgLength(opPos);
                break;
            default:
                argLen = compiler.getArgLengthOfStep(opPos);
                break;
        }
        if (compiler.countPredicates(compiler.getFirstPredicateOpPos(opPos)) > 0) {
            return true;
        }
        return false;
    }

    private static AxesWalker createDefaultWalker(Compiler compiler, int opPos, WalkingIterator lpi, int analysis) {
        AxesWalker ai;
        boolean simpleInit = false;
        int totalNumberWalkers = analysis & BITS_COUNT;
        switch (compiler.getOp(opPos)) {
            case OpCodes.OP_VARIABLE /*22*/:
            case OpCodes.OP_GROUP /*23*/:
            case OpCodes.OP_EXTFUNCTION /*24*/:
            case OpCodes.OP_FUNCTION /*25*/:
                ai = new FilterExprWalker(lpi);
                simpleInit = true;
                break;
            case OpCodes.FROM_ANCESTORS /*37*/:
                ai = new ReverseAxesWalker(lpi, 0);
                break;
            case OpCodes.FROM_ANCESTORS_OR_SELF /*38*/:
                ai = new ReverseAxesWalker(lpi, 1);
                break;
            case OpCodes.FROM_ATTRIBUTES /*39*/:
                ai = new AxesWalker(lpi, 2);
                break;
            case OpCodes.FROM_CHILDREN /*40*/:
                ai = new AxesWalker(lpi, 3);
                break;
            case OpCodes.FROM_DESCENDANTS /*41*/:
                ai = new AxesWalker(lpi, 4);
                break;
            case OpCodes.FROM_DESCENDANTS_OR_SELF /*42*/:
                ai = new AxesWalker(lpi, 5);
                break;
            case OpCodes.FROM_FOLLOWING /*43*/:
                ai = new AxesWalker(lpi, 6);
                break;
            case OpCodes.FROM_FOLLOWING_SIBLINGS /*44*/:
                ai = new AxesWalker(lpi, 7);
                break;
            case OpCodes.FROM_PARENT /*45*/:
                ai = new ReverseAxesWalker(lpi, 10);
                break;
            case OpCodes.FROM_PRECEDING /*46*/:
                ai = new ReverseAxesWalker(lpi, 11);
                break;
            case OpCodes.FROM_PRECEDING_SIBLINGS /*47*/:
                ai = new ReverseAxesWalker(lpi, 12);
                break;
            case OpCodes.FROM_SELF /*48*/:
                ai = new AxesWalker(lpi, 13);
                break;
            case OpCodes.FROM_NAMESPACE /*49*/:
                ai = new AxesWalker(lpi, 9);
                break;
            case OpCodes.FROM_ROOT /*50*/:
                ai = new AxesWalker(lpi, 19);
                break;
            default:
                throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER, new Object[]{Integer.toString(stepType)}));
        }
        if (simpleInit) {
            ai.initNodeTest(-1);
        } else {
            int whatToShow = compiler.getWhatToShow(opPos);
            if ((whatToShow & 4163) == 0 || whatToShow == -1) {
                ai.initNodeTest(whatToShow);
            } else {
                ai.initNodeTest(whatToShow, compiler.getStepNS(opPos), compiler.getStepLocalName(opPos));
            }
        }
        return ai;
    }

    public static String getAnalysisString(int analysis) {
        StringBuffer buf = new StringBuffer();
        buf.append("count: " + getStepCount(analysis) + " ");
        if ((BIT_NODETEST_ANY & analysis) != 0) {
            buf.append("NTANY|");
        }
        if ((analysis & BIT_PREDICATE) != 0) {
            buf.append("PRED|");
        }
        if ((analysis & BIT_ANCESTOR) != 0) {
            buf.append("ANC|");
        }
        if ((analysis & BIT_ANCESTOR_OR_SELF) != 0) {
            buf.append("ANCOS|");
        }
        if ((BIT_ATTRIBUTE & analysis) != 0) {
            buf.append("ATTR|");
        }
        if ((BIT_CHILD & analysis) != 0) {
            buf.append("CH|");
        }
        if ((BIT_DESCENDANT & analysis) != 0) {
            buf.append("DESC|");
        }
        if ((BIT_DESCENDANT_OR_SELF & analysis) != 0) {
            buf.append("DESCOS|");
        }
        if ((BIT_FOLLOWING & analysis) != 0) {
            buf.append("FOL|");
        }
        if ((BIT_FOLLOWING_SIBLING & analysis) != 0) {
            buf.append("FOLS|");
        }
        if ((BIT_NAMESPACE & analysis) != 0) {
            buf.append("NS|");
        }
        if ((BIT_PARENT & analysis) != 0) {
            buf.append("P|");
        }
        if ((BIT_PRECEDING & analysis) != 0) {
            buf.append("PREC|");
        }
        if ((BIT_PRECEDING_SIBLING & analysis) != 0) {
            buf.append("PRECS|");
        }
        if ((BIT_SELF & analysis) != 0) {
            buf.append(".|");
        }
        if ((BIT_FILTER & analysis) != 0) {
            buf.append("FLT|");
        }
        if ((BIT_ROOT & analysis) != 0) {
            buf.append("R|");
        }
        return buf.toString();
    }

    public static boolean hasPredicate(int analysis) {
        return (analysis & BIT_PREDICATE) != 0;
    }

    public static boolean isWild(int analysis) {
        return (BIT_NODETEST_ANY & analysis) != 0;
    }

    public static boolean walksAncestors(int analysis) {
        return isSet(analysis, 24576);
    }

    public static boolean walksAttributes(int analysis) {
        return (BIT_ATTRIBUTE & analysis) != 0;
    }

    public static boolean walksNamespaces(int analysis) {
        return (BIT_NAMESPACE & analysis) != 0;
    }

    public static boolean walksChildren(int analysis) {
        return (BIT_CHILD & analysis) != 0;
    }

    public static boolean walksDescendants(int analysis) {
        return isSet(analysis, 393216);
    }

    public static boolean walksSubtree(int analysis) {
        return isSet(analysis, 458752);
    }

    public static boolean walksSubtreeOnlyMaybeAbsolute(int analysis) {
        if (!walksSubtree(analysis) || walksExtraNodes(analysis) || walksUp(analysis) || walksSideways(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean walksSubtreeOnly(int analysis) {
        if (!walksSubtreeOnlyMaybeAbsolute(analysis) || isAbsolute(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean walksFilteredList(int analysis) {
        return isSet(analysis, BIT_FILTER);
    }

    public static boolean walksSubtreeOnlyFromRootOrContext(int analysis) {
        if (!walksSubtree(analysis) || walksExtraNodes(analysis) || walksUp(analysis) || walksSideways(analysis) || isSet(analysis, BIT_FILTER)) {
            return false;
        }
        return true;
    }

    public static boolean walksInDocOrder(int analysis) {
        if ((walksSubtreeOnlyMaybeAbsolute(analysis) || walksExtraNodesOnly(analysis) || walksFollowingOnlyMaybeAbsolute(analysis)) && !isSet(analysis, BIT_FILTER)) {
            return true;
        }
        return false;
    }

    public static boolean walksFollowingOnlyMaybeAbsolute(int analysis) {
        if (!isSet(analysis, 35127296) || walksSubtree(analysis) || walksUp(analysis) || walksSideways(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean walksUp(int analysis) {
        return isSet(analysis, 4218880);
    }

    public static boolean walksSideways(int analysis) {
        return isSet(analysis, 26738688);
    }

    public static boolean walksExtraNodes(int analysis) {
        return isSet(analysis, 2129920);
    }

    public static boolean walksExtraNodesOnly(int analysis) {
        if (!walksExtraNodes(analysis) || isSet(analysis, BIT_SELF) || walksSubtree(analysis) || walksUp(analysis) || walksSideways(analysis) || isAbsolute(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean isAbsolute(int analysis) {
        return isSet(analysis, 201326592);
    }

    public static boolean walksChildrenOnly(int analysis) {
        if (!walksChildren(analysis) || isSet(analysis, BIT_SELF) || walksExtraNodes(analysis) || walksDescendants(analysis) || walksUp(analysis) || walksSideways(analysis)) {
            return false;
        }
        return isAbsolute(analysis) ? isSet(analysis, BIT_ROOT) : true;
    }

    public static boolean walksChildrenAndExtraAndSelfOnly(int analysis) {
        if (!walksChildren(analysis) || walksDescendants(analysis) || walksUp(analysis) || walksSideways(analysis)) {
            return false;
        }
        return isAbsolute(analysis) ? isSet(analysis, BIT_ROOT) : true;
    }

    public static boolean walksDescendantsAndExtraAndSelfOnly(int analysis) {
        if (walksChildren(analysis) || !walksDescendants(analysis) || walksUp(analysis) || walksSideways(analysis)) {
            return false;
        }
        return isAbsolute(analysis) ? isSet(analysis, BIT_ROOT) : true;
    }

    public static boolean walksSelfOnly(int analysis) {
        if (!isSet(analysis, BIT_SELF) || walksSubtree(analysis) || walksUp(analysis) || walksSideways(analysis) || isAbsolute(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean walksUpOnly(int analysis) {
        if (walksSubtree(analysis) || !walksUp(analysis) || walksSideways(analysis) || isAbsolute(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean walksDownOnly(int analysis) {
        if (!walksSubtree(analysis) || walksUp(analysis) || walksSideways(analysis) || isAbsolute(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean walksDownExtraOnly(int analysis) {
        if (!walksSubtree(analysis) || !walksExtraNodes(analysis) || walksUp(analysis) || walksSideways(analysis) || isAbsolute(analysis)) {
            return false;
        }
        return true;
    }

    public static boolean canSkipSubtrees(int analysis) {
        return isSet(analysis, BIT_CHILD) | walksSideways(analysis);
    }

    public static boolean canCrissCross(int analysis) {
        if (walksSelfOnly(analysis)) {
            return false;
        }
        if ((walksDownOnly(analysis) && !canSkipSubtrees(analysis)) || walksChildrenAndExtraAndSelfOnly(analysis) || walksDescendantsAndExtraAndSelfOnly(analysis) || walksUpOnly(analysis) || walksExtraNodesOnly(analysis) || !walksSubtree(analysis) || (!walksSideways(analysis) && !walksUp(analysis) && !canSkipSubtrees(analysis))) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isNaturalDocOrder(int analysis) {
        if (canCrissCross(analysis) || isSet(analysis, BIT_NAMESPACE) || walksFilteredList(analysis) || !walksInDocOrder(analysis)) {
            return false;
        }
        return true;
    }

    private static boolean isNaturalDocOrder(Compiler compiler, int stepOpCodePos, int stepIndex, int analysis) throws TransformerException {
        if (canCrissCross(analysis) || isSet(analysis, BIT_NAMESPACE)) {
            return false;
        }
        if (isSet(analysis, 1572864) && isSet(analysis, 25165824)) {
            return false;
        }
        int stepCount = 0;
        boolean foundWildAttribute = false;
        int potentialDuplicateMakingStepCount = 0;
        while (true) {
            int stepType = compiler.getOp(stepOpCodePos);
            if (-1 != stepType) {
                stepCount++;
                switch (stepType) {
                    case OpCodes.OP_VARIABLE /*22*/:
                    case OpCodes.OP_GROUP /*23*/:
                    case OpCodes.OP_EXTFUNCTION /*24*/:
                    case OpCodes.OP_FUNCTION /*25*/:
                    case OpCodes.FROM_ANCESTORS /*37*/:
                    case OpCodes.FROM_ANCESTORS_OR_SELF /*38*/:
                    case OpCodes.FROM_DESCENDANTS /*41*/:
                    case OpCodes.FROM_DESCENDANTS_OR_SELF /*42*/:
                    case OpCodes.FROM_FOLLOWING /*43*/:
                    case OpCodes.FROM_FOLLOWING_SIBLINGS /*44*/:
                    case OpCodes.FROM_PARENT /*45*/:
                    case OpCodes.FROM_PRECEDING /*46*/:
                    case OpCodes.FROM_PRECEDING_SIBLINGS /*47*/:
                    case OpCodes.FROM_NAMESPACE /*49*/:
                    case OpCodes.MATCH_ANY_ANCESTOR /*52*/:
                    case OpCodes.MATCH_IMMEDIATE_ANCESTOR /*53*/:
                        if (potentialDuplicateMakingStepCount <= 0) {
                            potentialDuplicateMakingStepCount++;
                            break;
                        }
                        return false;
                    case OpCodes.FROM_ATTRIBUTES /*39*/:
                    case OpCodes.MATCH_ATTRIBUTE /*51*/:
                        if (!foundWildAttribute) {
                            if (compiler.getStepLocalName(stepOpCodePos).equals(PsuedoNames.PSEUDONAME_OTHER)) {
                                foundWildAttribute = true;
                                break;
                            }
                        }
                        return false;
                        break;
                    case OpCodes.FROM_CHILDREN /*40*/:
                    case OpCodes.FROM_SELF /*48*/:
                    case OpCodes.FROM_ROOT /*50*/:
                        break;
                    default:
                        throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_NULL_ERROR_HANDLER, new Object[]{Integer.toString(stepType)}));
                }
                if (foundWildAttribute) {
                    return false;
                }
                int nextStepOpCodePos = compiler.getNextStepPos(stepOpCodePos);
                if (nextStepOpCodePos >= 0) {
                    stepOpCodePos = nextStepOpCodePos;
                }
            }
            return true;
        }
    }

    public static boolean isOneStep(int analysis) {
        return (analysis & BITS_COUNT) == 1;
    }

    public static int getStepCount(int analysis) {
        return analysis & BITS_COUNT;
    }
}

package ohos.com.sun.org.apache.xpath.internal.axes;

import java.io.PrintStream;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMIterator;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.compiler.Compiler;
import ohos.com.sun.org.apache.xpath.internal.compiler.OpMap;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.patterns.ContextMatchStepPattern;
import ohos.com.sun.org.apache.xpath.internal.patterns.FunctionPattern;
import ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern;
import ohos.javax.xml.transform.TransformerException;

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

    public static int getAnalysisBitFromAxes(int i) {
        switch (i) {
            case 0:
                return 8192;
            case 1:
                return 16384;
            case 2:
                return 32768;
            case 3:
                return 65536;
            case 4:
                return 131072;
            case 5:
                return 262144;
            case 6:
                return 524288;
            case 7:
                return 1048576;
            case 8:
            case 9:
                return 2097152;
            case 10:
                return 4194304;
            case 11:
                return 8388608;
            case 12:
                return 16777216;
            case 13:
                return 33554432;
            case 14:
                return 262144;
            case 15:
            case 20:
            default:
                return 67108864;
            case 16:
            case 17:
            case 18:
                return 536870912;
            case 19:
                return 134217728;
        }
    }

    public static int getStepCount(int i) {
        return i & 255;
    }

    public static boolean hasPredicate(int i) {
        return (i & 4096) != 0;
    }

    public static boolean isDownwardAxisOfMany(int i) {
        return 5 == i || 4 == i || 6 == i || 11 == i;
    }

    public static boolean isOneStep(int i) {
        return (i & 255) == 1;
    }

    public static boolean isSet(int i, int i2) {
        return (i & i2) != 0;
    }

    public static boolean isWild(int i) {
        return (i & 1073741824) != 0;
    }

    public static boolean walksAttributes(int i) {
        return (i & 32768) != 0;
    }

    public static boolean walksChildren(int i) {
        return (i & 65536) != 0;
    }

    public static boolean walksNamespaces(int i) {
        return (i & 2097152) != 0;
    }

    static AxesWalker loadOneWalker(WalkingIterator walkingIterator, Compiler compiler, int i) throws TransformerException {
        int op = compiler.getOp(i);
        if (op == -1) {
            return null;
        }
        AxesWalker createDefaultWalker = createDefaultWalker(compiler, op, walkingIterator, 0);
        createDefaultWalker.init(compiler, i, op);
        return createDefaultWalker;
    }

    static AxesWalker loadWalkers(WalkingIterator walkingIterator, Compiler compiler, int i, int i2) throws TransformerException {
        int analyze = analyze(compiler, i, i2);
        AxesWalker axesWalker = null;
        AxesWalker axesWalker2 = null;
        while (true) {
            int op = compiler.getOp(i);
            if (-1 == op) {
                break;
            }
            AxesWalker createDefaultWalker = createDefaultWalker(compiler, i, walkingIterator, analyze);
            createDefaultWalker.init(compiler, i, op);
            createDefaultWalker.exprSetParent(walkingIterator);
            if (axesWalker == null) {
                axesWalker = createDefaultWalker;
            } else {
                axesWalker2.setNextWalker(createDefaultWalker);
                createDefaultWalker.setPrevWalker(axesWalker2);
            }
            i = compiler.getNextStepPos(i);
            if (i < 0) {
                break;
            }
            axesWalker2 = createDefaultWalker;
        }
        return axesWalker;
    }

    public static void diagnoseIterator(String str, int i, Compiler compiler) {
        PrintStream printStream = System.out;
        printStream.println(compiler.toString() + ", " + str + ", " + Integer.toBinaryString(i) + ", " + getAnalysisString(i));
    }

    public static DTMIterator newDTMIterator(Compiler compiler, int i, boolean z) throws TransformerException {
        LocPathIterator locPathIterator;
        int firstChildPos = OpMap.getFirstChildPos(i);
        int analyze = analyze(compiler, firstChildPos, 0);
        boolean isOneStep = isOneStep(analyze);
        if (isOneStep && walksSelfOnly(analyze) && isWild(analyze) && !hasPredicate(analyze)) {
            locPathIterator = new SelfIteratorNoPredicate(compiler, i, analyze);
        } else if (!walksChildrenOnly(analyze) || !isOneStep) {
            if (isOneStep && walksAttributes(analyze)) {
                locPathIterator = new AttributeIterator(compiler, i, analyze);
            } else if (!isOneStep || walksFilteredList(analyze)) {
                if (isOptimizableForDescendantIterator(compiler, firstChildPos, 0)) {
                    locPathIterator = new DescendantIterator(compiler, i, analyze);
                } else {
                    locPathIterator = isNaturalDocOrder(compiler, firstChildPos, 0, analyze) ? new WalkingIterator(compiler, i, analyze, true) : new WalkingIteratorSorted(compiler, i, analyze, true);
                }
            } else if (walksNamespaces(analyze) || (!walksInDocOrder(analyze) && !isSet(analyze, 4194304))) {
                locPathIterator = new OneStepIterator(compiler, i, analyze);
            } else {
                locPathIterator = new OneStepIteratorForward(compiler, i, analyze);
            }
        } else if (!isWild(analyze) || hasPredicate(analyze)) {
            locPathIterator = new ChildTestIterator(compiler, i, analyze);
        } else {
            locPathIterator = new ChildIterator(compiler, i, analyze);
        }
        if (locPathIterator instanceof LocPathIterator) {
            locPathIterator.setIsTopLevel(z);
        }
        return locPathIterator;
    }

    public static int getAxisFromStep(Compiler compiler, int i) throws TransformerException {
        int op = compiler.getOp(i);
        switch (op) {
            case 22:
            case 23:
            case 24:
            case 25:
                return 20;
            default:
                switch (op) {
                    case 37:
                        return 0;
                    case 38:
                        return 1;
                    case 39:
                        return 2;
                    case 40:
                        return 3;
                    case 41:
                        return 4;
                    case 42:
                        return 5;
                    case 43:
                        return 6;
                    case 44:
                        return 7;
                    case 45:
                        return 10;
                    case 46:
                        return 11;
                    case 47:
                        return 12;
                    case 48:
                        return 13;
                    case 49:
                        return 9;
                    case 50:
                        return 19;
                    default:
                        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NULL_ERROR_HANDLER", new Object[]{Integer.toString(op)}));
                }
        }
    }

    static boolean functionProximateOrContainsProximate(Compiler compiler, int i) {
        int op = (compiler.getOp(i + 1) + i) - 1;
        int firstChildPos = OpMap.getFirstChildPos(i);
        int op2 = compiler.getOp(firstChildPos);
        if (op2 == 1 || op2 == 2) {
            return true;
        }
        int i2 = firstChildPos + 1;
        while (i2 < op) {
            int i3 = i2 + 2;
            compiler.getOp(i3);
            if (isProximateInnerExpr(compiler, i3)) {
                return true;
            }
            i2 = compiler.getNextOpPos(i2);
        }
        return false;
    }

    static boolean isProximateInnerExpr(Compiler compiler, int i) {
        int op = compiler.getOp(i);
        int i2 = i + 2;
        if (op == 21 || op == 22) {
            return false;
        }
        switch (op) {
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                int firstChildPos = OpMap.getFirstChildPos(op);
                int nextOpPos = compiler.getNextOpPos(firstChildPos);
                if (!isProximateInnerExpr(compiler, firstChildPos) && !isProximateInnerExpr(compiler, nextOpPos)) {
                    return false;
                }
                return true;
            default:
                switch (op) {
                    case 25:
                        if (functionProximateOrContainsProximate(compiler, i)) {
                            return true;
                        }
                        return false;
                    case 26:
                        if (isProximateInnerExpr(compiler, i2)) {
                            return true;
                        }
                        return false;
                    case 27:
                    case 28:
                        return false;
                    default:
                        return true;
                }
        }
    }

    public static boolean mightBeProximate(Compiler compiler, int i, int i2) throws TransformerException {
        switch (i2) {
            case 22:
            case 23:
            case 24:
            case 25:
                compiler.getArgLength(i);
                break;
            default:
                compiler.getArgLengthOfStep(i);
                break;
        }
        int firstPredicateOpPos = compiler.getFirstPredicateOpPos(i);
        while (29 == compiler.getOp(firstPredicateOpPos)) {
            int i3 = firstPredicateOpPos + 2;
            int op = compiler.getOp(i3);
            if (!(op == 19 || op == 22)) {
                if (op != 25) {
                    if (op != 27) {
                        if (op != 28) {
                            switch (op) {
                                default:
                                    return true;
                                case 5:
                                case 6:
                                case 7:
                                case 8:
                                case 9:
                                    int firstChildPos = OpMap.getFirstChildPos(i3);
                                    int nextOpPos = compiler.getNextOpPos(firstChildPos);
                                    if (isProximateInnerExpr(compiler, firstChildPos) || isProximateInnerExpr(compiler, nextOpPos)) {
                                        return true;
                                    }
                                    continue;
                            }
                        } else {
                            continue;
                        }
                    }
                } else if (functionProximateOrContainsProximate(compiler, i3)) {
                    return true;
                }
                firstPredicateOpPos = compiler.getNextOpPos(firstPredicateOpPos);
            }
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0051, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006d, code lost:
        return true;
     */
    private static boolean isOptimizableForDescendantIterator(Compiler compiler, int i, int i2) throws TransformerException {
        int i3 = 1033;
        int i4 = 0;
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        while (true) {
            int op = compiler.getOp(i);
            if (-1 != op) {
                if ((i3 == 1033 || i3 == 35) && (i4 = i4 + 1) <= 3 && !mightBeProximate(compiler, i, op)) {
                    switch (op) {
                        case 22:
                        case 23:
                        case 24:
                        case 25:
                            break;
                        default:
                            switch (op) {
                                case 37:
                                case 38:
                                case 39:
                                case 43:
                                case 44:
                                case 45:
                                case 46:
                                case 47:
                                case 49:
                                case 51:
                                case 52:
                                case 53:
                                    break;
                                case 40:
                                    if (!z && (!z2 || !z3)) {
                                    }
                                case 42:
                                    z = true;
                                case 41:
                                    if (3 != i4) {
                                        z2 = true;
                                        break;
                                    } else {
                                        return false;
                                    }
                                case 48:
                                    if (1 == i4) {
                                        z3 = true;
                                        break;
                                    } else {
                                        return false;
                                    }
                                case 50:
                                    if (1 != i4) {
                                        return false;
                                    }
                                    break;
                                default:
                                    throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NULL_ERROR_HANDLER", new Object[]{Integer.toString(op)}));
                            }
                            i3 = compiler.getStepTestType(i);
                            int nextStepPos = compiler.getNextStepPos(i);
                            if (nextStepPos < 0) {
                                break;
                            } else if (-1 != compiler.getOp(nextStepPos) && compiler.countPredicates(i) > 0) {
                                return false;
                            } else {
                                i = nextStepPos;
                            }
                            break;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static int analyze(Compiler compiler, int i, int i2) throws TransformerException {
        int i3;
        int i4 = 0;
        int i5 = 0;
        do {
            int op = compiler.getOp(i);
            if (-1 != op) {
                i4++;
                if (analyzePredicate(compiler, i, op)) {
                    i5 |= 4096;
                }
                switch (op) {
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                        i3 = 67108864;
                        i5 |= i3;
                        break;
                    default:
                        i3 = 134217728;
                        switch (op) {
                            case 37:
                                i5 |= 8192;
                                break;
                            case 38:
                                i5 |= 16384;
                                break;
                            case 39:
                                i3 = 32768;
                                i5 |= i3;
                                break;
                            case 40:
                                i3 = 65536;
                                i5 |= i3;
                                break;
                            case 41:
                                i3 = 131072;
                                i5 |= i3;
                                break;
                            case 42:
                                if (2 == i4 && 134217728 == i5) {
                                    i5 |= 536870912;
                                }
                                i3 = 262144;
                                i5 |= i3;
                                break;
                            case 43:
                                i3 = 524288;
                                i5 |= i3;
                                break;
                            case 44:
                                i3 = 1048576;
                                i5 |= i3;
                                break;
                            case 45:
                                i3 = 4194304;
                                i5 |= i3;
                                break;
                            case 46:
                                i3 = 8388608;
                                i5 |= i3;
                                break;
                            case 47:
                                i3 = 16777216;
                                i5 |= i3;
                                break;
                            case 48:
                                i3 = 33554432;
                                i5 |= i3;
                                break;
                            case 49:
                                i3 = 2097152;
                                i5 |= i3;
                                break;
                            case 50:
                                i5 |= i3;
                                break;
                            case 51:
                                i3 = -2147450880;
                                i5 |= i3;
                                break;
                            case 52:
                                i3 = -2147475456;
                                i5 |= i3;
                                break;
                            case 53:
                                i3 = -2143289344;
                                i5 |= i3;
                                break;
                            default:
                                throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NULL_ERROR_HANDLER", new Object[]{Integer.toString(op)}));
                        }
                }
                if (1033 == compiler.getOp(i + 3)) {
                    i5 |= 1073741824;
                }
                i = compiler.getNextStepPos(i);
            }
            return (i4 & 255) | i5;
        } while (i >= 0);
        return (i4 & 255) | i5;
    }

    static StepPattern loadSteps(MatchPatternIterator matchPatternIterator, Compiler compiler, int i, int i2) throws TransformerException {
        int analyze = analyze(compiler, i, i2);
        int i3 = i;
        StepPattern stepPattern = null;
        StepPattern stepPattern2 = null;
        StepPattern stepPattern3 = null;
        while (-1 != compiler.getOp(i3)) {
            stepPattern = createDefaultStepPattern(compiler, i3, matchPatternIterator, analyze, stepPattern2, stepPattern3);
            if (stepPattern2 == null) {
                stepPattern2 = stepPattern;
            } else {
                stepPattern.setRelativePathPattern(stepPattern3);
            }
            i3 = compiler.getNextStepPos(i3);
            if (i3 < 0) {
                break;
            }
            stepPattern3 = stepPattern;
        }
        StepPattern stepPattern4 = stepPattern;
        StepPattern stepPattern5 = stepPattern4;
        int i4 = 13;
        while (stepPattern4 != null) {
            int axis = stepPattern4.getAxis();
            stepPattern4.setAxis(i4);
            int whatToShow = stepPattern4.getWhatToShow();
            if (whatToShow == 2 || whatToShow == 4096) {
                int i5 = whatToShow == 2 ? 2 : 9;
                if (isDownwardAxisOfMany(i4)) {
                    StepPattern stepPattern6 = new StepPattern(whatToShow, stepPattern4.getNamespace(), stepPattern4.getLocalName(), i5, 0);
                    XNumber staticScore = stepPattern4.getStaticScore();
                    stepPattern4.setNamespace(null);
                    stepPattern4.setLocalName("*");
                    stepPattern6.setPredicates(stepPattern4.getPredicates());
                    stepPattern4.setPredicates(null);
                    stepPattern4.setWhatToShow(1);
                    StepPattern relativePathPattern = stepPattern4.getRelativePathPattern();
                    stepPattern4.setRelativePathPattern(stepPattern6);
                    stepPattern6.setRelativePathPattern(relativePathPattern);
                    stepPattern6.setStaticScore(staticScore);
                    if (11 == stepPattern4.getAxis()) {
                        stepPattern4.setAxis(15);
                    } else if (4 == stepPattern4.getAxis()) {
                        stepPattern4.setAxis(5);
                    }
                    stepPattern4 = stepPattern6;
                } else if (3 == stepPattern4.getAxis()) {
                    stepPattern4.setAxis(2);
                }
            }
            stepPattern5 = stepPattern4;
            stepPattern4 = stepPattern4.getRelativePathPattern();
            i4 = axis;
        }
        if (i4 < 16) {
            ContextMatchStepPattern contextMatchStepPattern = new ContextMatchStepPattern(i4, 13);
            XNumber staticScore2 = stepPattern5.getStaticScore();
            stepPattern5.setRelativePathPattern(contextMatchStepPattern);
            stepPattern5.setStaticScore(staticScore2);
            contextMatchStepPattern.setStaticScore(staticScore2);
        }
        return stepPattern;
    }

    private static StepPattern createDefaultStepPattern(Compiler compiler, int i, MatchPatternIterator matchPatternIterator, int i2, StepPattern stepPattern, StepPattern stepPattern2) throws TransformerException {
        Expression expression;
        int i3;
        int op = compiler.getOp(i);
        compiler.getWhatToShow(i);
        int i4 = 7;
        int i5 = 12;
        StepPattern stepPattern3 = null;
        switch (op) {
            case 22:
            case 23:
            case 24:
            case 25:
                switch (op) {
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                        expression = compiler.compileExpression(i);
                        break;
                    default:
                        expression = compiler.compileExpression(i + 2);
                        break;
                }
                stepPattern3 = new FunctionPattern(expression, 20, 20);
                i4 = 20;
                i5 = i4;
                break;
            default:
                switch (op) {
                    case 37:
                        i4 = 4;
                        i5 = 0;
                        break;
                    case 38:
                        i4 = 5;
                        i5 = 1;
                        break;
                    case 39:
                        i3 = 2;
                        i5 = i3;
                        i4 = 10;
                        break;
                    case 40:
                        i5 = 3;
                        i4 = 10;
                        break;
                    case 41:
                        i5 = 4;
                        i4 = 0;
                        break;
                    case 42:
                        i5 = 5;
                        i4 = 1;
                        break;
                    case 43:
                        i5 = 6;
                        i4 = 11;
                        break;
                    case 44:
                        i5 = 7;
                        i4 = 12;
                        break;
                    case 45:
                        i4 = 3;
                        i5 = 10;
                        break;
                    case 46:
                        i4 = 6;
                        i5 = 11;
                        break;
                    case 47:
                        break;
                    case 48:
                        i4 = 13;
                        i5 = i4;
                        break;
                    case 49:
                        i3 = 9;
                        i5 = i3;
                        i4 = 10;
                        break;
                    case 50:
                        stepPattern3 = new StepPattern(1280, 19, 19);
                        i4 = 19;
                        i5 = i4;
                        break;
                    default:
                        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NULL_ERROR_HANDLER", new Object[]{Integer.toString(op)}));
                }
        }
        if (stepPattern3 == null) {
            stepPattern3 = new StepPattern(compiler.getWhatToShow(i), compiler.getStepNS(i), compiler.getStepLocalName(i), i4, i5);
        }
        stepPattern3.setPredicates(compiler.getCompiledPredicates(compiler.getFirstPredicateOpPos(i)));
        return stepPattern3;
    }

    static boolean analyzePredicate(Compiler compiler, int i, int i2) throws TransformerException {
        switch (i2) {
            case 22:
            case 23:
            case 24:
            case 25:
                compiler.getArgLength(i);
                break;
            default:
                compiler.getArgLengthOfStep(i);
                break;
        }
        return compiler.countPredicates(compiler.getFirstPredicateOpPos(i)) > 0;
    }

    private static AxesWalker createDefaultWalker(Compiler compiler, int i, WalkingIterator walkingIterator, int i2) {
        AxesWalker axesWalker;
        int op = compiler.getOp(i);
        boolean z = false;
        switch (op) {
            case 22:
            case 23:
            case 24:
            case 25:
                axesWalker = new FilterExprWalker(walkingIterator);
                z = true;
                break;
            default:
                switch (op) {
                    case 37:
                        axesWalker = new ReverseAxesWalker(walkingIterator, 0);
                        break;
                    case 38:
                        axesWalker = new ReverseAxesWalker(walkingIterator, 1);
                        break;
                    case 39:
                        axesWalker = new AxesWalker(walkingIterator, 2);
                        break;
                    case 40:
                        axesWalker = new AxesWalker(walkingIterator, 3);
                        break;
                    case 41:
                        axesWalker = new AxesWalker(walkingIterator, 4);
                        break;
                    case 42:
                        axesWalker = new AxesWalker(walkingIterator, 5);
                        break;
                    case 43:
                        axesWalker = new AxesWalker(walkingIterator, 6);
                        break;
                    case 44:
                        axesWalker = new AxesWalker(walkingIterator, 7);
                        break;
                    case 45:
                        axesWalker = new ReverseAxesWalker(walkingIterator, 10);
                        break;
                    case 46:
                        axesWalker = new ReverseAxesWalker(walkingIterator, 11);
                        break;
                    case 47:
                        axesWalker = new ReverseAxesWalker(walkingIterator, 12);
                        break;
                    case 48:
                        axesWalker = new AxesWalker(walkingIterator, 13);
                        break;
                    case 49:
                        axesWalker = new AxesWalker(walkingIterator, 9);
                        break;
                    case 50:
                        axesWalker = new AxesWalker(walkingIterator, 19);
                        break;
                    default:
                        throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NULL_ERROR_HANDLER", new Object[]{Integer.toString(op)}));
                }
        }
        if (z) {
            axesWalker.initNodeTest(-1);
        } else {
            int whatToShow = compiler.getWhatToShow(i);
            if ((whatToShow & 4163) == 0 || whatToShow == -1) {
                axesWalker.initNodeTest(whatToShow);
            } else {
                axesWalker.initNodeTest(whatToShow, compiler.getStepNS(i), compiler.getStepLocalName(i));
            }
        }
        return axesWalker;
    }

    public static String getAnalysisString(int i) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("count: ");
        stringBuffer.append(getStepCount(i));
        stringBuffer.append(' ');
        if ((1073741824 & i) != 0) {
            stringBuffer.append("NTANY|");
        }
        if ((i & 4096) != 0) {
            stringBuffer.append("PRED|");
        }
        if ((i & 8192) != 0) {
            stringBuffer.append("ANC|");
        }
        if ((i & 16384) != 0) {
            stringBuffer.append("ANCOS|");
        }
        if ((32768 & i) != 0) {
            stringBuffer.append("ATTR|");
        }
        if ((65536 & i) != 0) {
            stringBuffer.append("CH|");
        }
        if ((131072 & i) != 0) {
            stringBuffer.append("DESC|");
        }
        if ((262144 & i) != 0) {
            stringBuffer.append("DESCOS|");
        }
        if ((524288 & i) != 0) {
            stringBuffer.append("FOL|");
        }
        if ((1048576 & i) != 0) {
            stringBuffer.append("FOLS|");
        }
        if ((2097152 & i) != 0) {
            stringBuffer.append("NS|");
        }
        if ((4194304 & i) != 0) {
            stringBuffer.append("P|");
        }
        if ((8388608 & i) != 0) {
            stringBuffer.append("PREC|");
        }
        if ((16777216 & i) != 0) {
            stringBuffer.append("PRECS|");
        }
        if ((33554432 & i) != 0) {
            stringBuffer.append(".|");
        }
        if ((67108864 & i) != 0) {
            stringBuffer.append("FLT|");
        }
        if ((i & 134217728) != 0) {
            stringBuffer.append("R|");
        }
        return stringBuffer.toString();
    }

    public static boolean walksAncestors(int i) {
        return isSet(i, 24576);
    }

    public static boolean walksDescendants(int i) {
        return isSet(i, 393216);
    }

    public static boolean walksSubtree(int i) {
        return isSet(i, 458752);
    }

    public static boolean walksSubtreeOnlyMaybeAbsolute(int i) {
        return walksSubtree(i) && !walksExtraNodes(i) && !walksUp(i) && !walksSideways(i);
    }

    public static boolean walksSubtreeOnly(int i) {
        return walksSubtreeOnlyMaybeAbsolute(i) && !isAbsolute(i);
    }

    public static boolean walksFilteredList(int i) {
        return isSet(i, 67108864);
    }

    public static boolean walksSubtreeOnlyFromRootOrContext(int i) {
        return walksSubtree(i) && !walksExtraNodes(i) && !walksUp(i) && !walksSideways(i) && !isSet(i, 67108864);
    }

    public static boolean walksInDocOrder(int i) {
        return (walksSubtreeOnlyMaybeAbsolute(i) || walksExtraNodesOnly(i) || walksFollowingOnlyMaybeAbsolute(i)) && !isSet(i, 67108864);
    }

    public static boolean walksFollowingOnlyMaybeAbsolute(int i) {
        return isSet(i, 35127296) && !walksSubtree(i) && !walksUp(i) && !walksSideways(i);
    }

    public static boolean walksUp(int i) {
        return isSet(i, 4218880);
    }

    public static boolean walksSideways(int i) {
        return isSet(i, 26738688);
    }

    public static boolean walksExtraNodes(int i) {
        return isSet(i, 2129920);
    }

    public static boolean walksExtraNodesOnly(int i) {
        return walksExtraNodes(i) && !isSet(i, 33554432) && !walksSubtree(i) && !walksUp(i) && !walksSideways(i) && !isAbsolute(i);
    }

    public static boolean isAbsolute(int i) {
        return isSet(i, 201326592);
    }

    public static boolean walksChildrenOnly(int i) {
        return walksChildren(i) && !isSet(i, 33554432) && !walksExtraNodes(i) && !walksDescendants(i) && !walksUp(i) && !walksSideways(i) && (!isAbsolute(i) || isSet(i, 134217728));
    }

    public static boolean walksChildrenAndExtraAndSelfOnly(int i) {
        return walksChildren(i) && !walksDescendants(i) && !walksUp(i) && !walksSideways(i) && (!isAbsolute(i) || isSet(i, 134217728));
    }

    public static boolean walksDescendantsAndExtraAndSelfOnly(int i) {
        return !walksChildren(i) && walksDescendants(i) && !walksUp(i) && !walksSideways(i) && (!isAbsolute(i) || isSet(i, 134217728));
    }

    public static boolean walksSelfOnly(int i) {
        return isSet(i, 33554432) && !walksSubtree(i) && !walksUp(i) && !walksSideways(i) && !isAbsolute(i);
    }

    public static boolean walksUpOnly(int i) {
        return !walksSubtree(i) && walksUp(i) && !walksSideways(i) && !isAbsolute(i);
    }

    public static boolean walksDownOnly(int i) {
        return walksSubtree(i) && !walksUp(i) && !walksSideways(i) && !isAbsolute(i);
    }

    public static boolean walksDownExtraOnly(int i) {
        return walksSubtree(i) && walksExtraNodes(i) && !walksUp(i) && !walksSideways(i) && !isAbsolute(i);
    }

    public static boolean canSkipSubtrees(int i) {
        return walksSideways(i) | isSet(i, 65536);
    }

    public static boolean canCrissCross(int i) {
        if (walksSelfOnly(i)) {
            return false;
        }
        if ((!walksDownOnly(i) || canSkipSubtrees(i)) && !walksChildrenAndExtraAndSelfOnly(i) && !walksDescendantsAndExtraAndSelfOnly(i) && !walksUpOnly(i) && !walksExtraNodesOnly(i) && walksSubtree(i) && (walksSideways(i) || walksUp(i) || canSkipSubtrees(i))) {
            return true;
        }
        return false;
    }

    public static boolean isNaturalDocOrder(int i) {
        if (canCrissCross(i) || isSet(i, 2097152) || walksFilteredList(i) || !walksInDocOrder(i)) {
            return false;
        }
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x005e A[SYNTHETIC] */
    private static boolean isNaturalDocOrder(Compiler compiler, int i, int i2, int i3) throws TransformerException {
        if (canCrissCross(i3) || isSet(i3, 2097152)) {
            return false;
        }
        if (isSet(i3, 1572864) && isSet(i3, 25165824)) {
            return false;
        }
        int i4 = 0;
        boolean z = false;
        do {
            int op = compiler.getOp(i);
            if (-1 != op) {
                switch (op) {
                    default:
                        switch (op) {
                            case 37:
                            case 38:
                            case 41:
                            case 42:
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 49:
                            case 52:
                            case 53:
                                break;
                            case 39:
                            case 51:
                                if (!z) {
                                    if (compiler.getStepLocalName(i).equals("*")) {
                                        z = true;
                                        break;
                                    }
                                } else {
                                    return false;
                                }
                                break;
                            case 40:
                            case 48:
                            case 50:
                                if (z) {
                                    return false;
                                }
                                break;
                            default:
                                throw new RuntimeException(XSLMessages.createXPATHMessage("ER_NULL_ERROR_HANDLER", new Object[]{Integer.toString(op)}));
                        }
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                        if (i4 > 0) {
                            return false;
                        }
                        i4++;
                        if (z) {
                        }
                        break;
                }
                i = compiler.getNextStepPos(i);
            }
            return true;
        } while (i >= 0);
        return true;
    }
}

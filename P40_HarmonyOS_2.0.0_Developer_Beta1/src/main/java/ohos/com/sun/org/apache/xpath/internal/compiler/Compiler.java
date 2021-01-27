package ohos.com.sun.org.apache.xpath.internal.compiler;

import java.io.PrintStream;
import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xml.internal.utils.QName;
import ohos.com.sun.org.apache.xpath.internal.Expression;
import ohos.com.sun.org.apache.xpath.internal.axes.UnionPathIterator;
import ohos.com.sun.org.apache.xpath.internal.axes.WalkerFactory;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncExtFunction;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable;
import ohos.com.sun.org.apache.xpath.internal.functions.Function;
import ohos.com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.com.sun.org.apache.xpath.internal.operations.And;
import ohos.com.sun.org.apache.xpath.internal.operations.Bool;
import ohos.com.sun.org.apache.xpath.internal.operations.Div;
import ohos.com.sun.org.apache.xpath.internal.operations.Equals;
import ohos.com.sun.org.apache.xpath.internal.operations.Gt;
import ohos.com.sun.org.apache.xpath.internal.operations.Gte;
import ohos.com.sun.org.apache.xpath.internal.operations.Lt;
import ohos.com.sun.org.apache.xpath.internal.operations.Lte;
import ohos.com.sun.org.apache.xpath.internal.operations.Minus;
import ohos.com.sun.org.apache.xpath.internal.operations.Mod;
import ohos.com.sun.org.apache.xpath.internal.operations.Mult;
import ohos.com.sun.org.apache.xpath.internal.operations.Neg;
import ohos.com.sun.org.apache.xpath.internal.operations.NotEquals;
import ohos.com.sun.org.apache.xpath.internal.operations.Number;
import ohos.com.sun.org.apache.xpath.internal.operations.Operation;
import ohos.com.sun.org.apache.xpath.internal.operations.Or;
import ohos.com.sun.org.apache.xpath.internal.operations.Plus;
import ohos.com.sun.org.apache.xpath.internal.operations.String;
import ohos.com.sun.org.apache.xpath.internal.operations.UnaryOperation;
import ohos.com.sun.org.apache.xpath.internal.operations.Variable;
import ohos.com.sun.org.apache.xpath.internal.patterns.FunctionPattern;
import ohos.com.sun.org.apache.xpath.internal.patterns.StepPattern;
import ohos.com.sun.org.apache.xpath.internal.patterns.UnionPattern;
import ohos.com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.TransformerException;

public class Compiler extends OpMap {
    private static final boolean DEBUG = false;
    private static long s_nextMethodId;
    int countOp;
    private int locPathDepth;
    private PrefixResolver m_currentPrefixResolver;
    ErrorListener m_errorHandler;
    private FunctionTable m_functionTable;
    SourceLocator m_locator;

    public Compiler(ErrorListener errorListener, SourceLocator sourceLocator, FunctionTable functionTable) {
        this.locPathDepth = -1;
        this.m_currentPrefixResolver = null;
        this.m_errorHandler = errorListener;
        this.m_locator = sourceLocator;
        this.m_functionTable = functionTable;
    }

    public Compiler() {
        this.locPathDepth = -1;
        this.m_currentPrefixResolver = null;
        this.m_errorHandler = null;
        this.m_locator = null;
    }

    public Expression compileExpression(int i) throws TransformerException {
        try {
            this.countOp = 0;
            return compile(i);
        } catch (StackOverflowError unused) {
            this.error(XPATHErrorResources.ER_COMPILATION_TOO_MANY_OPERATION, new Object[]{Integer.valueOf(this.countOp)});
            return null;
        }
    }

    private Expression compile(int i) throws TransformerException {
        switch (getOp(i)) {
            case 1:
                return compile(i + 2);
            case 2:
                return or(i);
            case 3:
                return and(i);
            case 4:
                return notequals(i);
            case 5:
                return equals(i);
            case 6:
                return lte(i);
            case 7:
                return lt(i);
            case 8:
                return gte(i);
            case 9:
                return gt(i);
            case 10:
                return plus(i);
            case 11:
                return minus(i);
            case 12:
                return mult(i);
            case 13:
                return div(i);
            case 14:
                return mod(i);
            case 15:
                error("ER_UNKNOWN_OPCODE", new Object[]{"quo"});
                return null;
            case 16:
                return neg(i);
            case 17:
                return string(i);
            case 18:
                return bool(i);
            case 19:
                return number(i);
            case 20:
                return union(i);
            case 21:
                return literal(i);
            case 22:
                return variable(i);
            case 23:
                return group(i);
            case 24:
                return compileExtension(i);
            case 25:
                return compileFunction(i);
            case 26:
                return arg(i);
            case 27:
                return numberlit(i);
            case 28:
                return locationPath(i);
            case 29:
                return null;
            case 30:
                return matchPattern(i + 2);
            case 31:
                return locationPathPattern(i);
            default:
                error("ER_UNKNOWN_OPCODE", new Object[]{Integer.toString(getOp(i))});
                return null;
        }
    }

    private Expression compileOperation(Operation operation, int i) throws TransformerException {
        this.countOp++;
        int firstChildPos = getFirstChildPos(i);
        operation.setLeftRight(compile(firstChildPos), compile(getNextOpPos(firstChildPos)));
        return operation;
    }

    private Expression compileUnary(UnaryOperation unaryOperation, int i) throws TransformerException {
        unaryOperation.setRight(compile(getFirstChildPos(i)));
        return unaryOperation;
    }

    /* access modifiers changed from: protected */
    public Expression or(int i) throws TransformerException {
        return compileOperation(new Or(), i);
    }

    /* access modifiers changed from: protected */
    public Expression and(int i) throws TransformerException {
        return compileOperation(new And(), i);
    }

    /* access modifiers changed from: protected */
    public Expression notequals(int i) throws TransformerException {
        return compileOperation(new NotEquals(), i);
    }

    /* access modifiers changed from: protected */
    public Expression equals(int i) throws TransformerException {
        return compileOperation(new Equals(), i);
    }

    /* access modifiers changed from: protected */
    public Expression lte(int i) throws TransformerException {
        return compileOperation(new Lte(), i);
    }

    /* access modifiers changed from: protected */
    public Expression lt(int i) throws TransformerException {
        return compileOperation(new Lt(), i);
    }

    /* access modifiers changed from: protected */
    public Expression gte(int i) throws TransformerException {
        return compileOperation(new Gte(), i);
    }

    /* access modifiers changed from: protected */
    public Expression gt(int i) throws TransformerException {
        return compileOperation(new Gt(), i);
    }

    /* access modifiers changed from: protected */
    public Expression plus(int i) throws TransformerException {
        return compileOperation(new Plus(), i);
    }

    /* access modifiers changed from: protected */
    public Expression minus(int i) throws TransformerException {
        return compileOperation(new Minus(), i);
    }

    /* access modifiers changed from: protected */
    public Expression mult(int i) throws TransformerException {
        return compileOperation(new Mult(), i);
    }

    /* access modifiers changed from: protected */
    public Expression div(int i) throws TransformerException {
        return compileOperation(new Div(), i);
    }

    /* access modifiers changed from: protected */
    public Expression mod(int i) throws TransformerException {
        return compileOperation(new Mod(), i);
    }

    /* access modifiers changed from: protected */
    public Expression neg(int i) throws TransformerException {
        return compileUnary(new Neg(), i);
    }

    /* access modifiers changed from: protected */
    public Expression string(int i) throws TransformerException {
        return compileUnary(new String(), i);
    }

    /* access modifiers changed from: protected */
    public Expression bool(int i) throws TransformerException {
        return compileUnary(new Bool(), i);
    }

    /* access modifiers changed from: protected */
    public Expression number(int i) throws TransformerException {
        return compileUnary(new Number(), i);
    }

    /* access modifiers changed from: protected */
    public Expression literal(int i) {
        return (XString) getTokenQueue().elementAt(getOp(getFirstChildPos(i)));
    }

    /* access modifiers changed from: protected */
    public Expression numberlit(int i) {
        return (XNumber) getTokenQueue().elementAt(getOp(getFirstChildPos(i)));
    }

    /* access modifiers changed from: protected */
    public Expression variable(int i) throws TransformerException {
        String str;
        Variable variable = new Variable();
        int firstChildPos = getFirstChildPos(i);
        int op = getOp(firstChildPos);
        if (-2 == op) {
            str = null;
        } else {
            str = (String) getTokenQueue().elementAt(op);
        }
        variable.setQName(new QName(str, (String) getTokenQueue().elementAt(getOp(firstChildPos + 1))));
        return variable;
    }

    /* access modifiers changed from: protected */
    public Expression group(int i) throws TransformerException {
        return compile(i + 2);
    }

    /* access modifiers changed from: protected */
    public Expression arg(int i) throws TransformerException {
        return compile(i + 2);
    }

    /* access modifiers changed from: protected */
    public Expression union(int i) throws TransformerException {
        this.locPathDepth++;
        try {
            return UnionPathIterator.createUnionIterator(this, i);
        } finally {
            this.locPathDepth--;
        }
    }

    public int getLocationPathDepth() {
        return this.locPathDepth;
    }

    /* access modifiers changed from: package-private */
    public FunctionTable getFunctionTable() {
        return this.m_functionTable;
    }

    public Expression locationPath(int i) throws TransformerException {
        this.locPathDepth++;
        try {
            return (Expression) WalkerFactory.newDTMIterator(this, i, this.locPathDepth == 0);
        } finally {
            this.locPathDepth--;
        }
    }

    public Expression predicate(int i) throws TransformerException {
        return compile(i + 2);
    }

    /* access modifiers changed from: protected */
    public Expression matchPattern(int i) throws TransformerException {
        this.locPathDepth++;
        int i2 = 0;
        int i3 = i;
        int i4 = 0;
        while (getOp(i3) == 31) {
            try {
                i3 = getNextOpPos(i3);
                i4++;
            } finally {
                this.locPathDepth--;
            }
        }
        if (i4 == 1) {
            return compile(i);
        }
        UnionPattern unionPattern = new UnionPattern();
        StepPattern[] stepPatternArr = new StepPattern[i4];
        while (getOp(i) == 31) {
            int nextOpPos = getNextOpPos(i);
            stepPatternArr[i2] = (StepPattern) compile(i);
            i2++;
            i = nextOpPos;
        }
        unionPattern.setPatterns(stepPatternArr);
        this.locPathDepth--;
        return unionPattern;
    }

    public Expression locationPathPattern(int i) throws TransformerException {
        return stepPattern(getFirstChildPos(i), 0, null);
    }

    public int getWhatToShow(int i) {
        int op = getOp(i);
        int op2 = getOp(i + 3);
        if (op2 == 34) {
            if (op != 39) {
                if (op == 49) {
                    return 4096;
                }
                switch (op) {
                    case 51:
                        break;
                    case 52:
                    case 53:
                    default:
                        return 1;
                }
            }
            return 2;
        } else if (op2 == 35) {
            return 1280;
        } else {
            switch (op2) {
                case OpCodes.NODETYPE_COMMENT /* 1030 */:
                    return 128;
                case OpCodes.NODETYPE_TEXT /* 1031 */:
                    return 12;
                case 1032:
                    return 64;
                case OpCodes.NODETYPE_NODE /* 1033 */:
                    if (op != 38) {
                        if (op != 39) {
                            if (op != 42) {
                                if (op != 51) {
                                    if (op != 48) {
                                        if (op != 49) {
                                            return getOp(0) == 30 ? -1283 : -3;
                                        }
                                        return 4096;
                                    }
                                }
                            }
                        }
                        return 2;
                    }
                    return -1;
                case OpCodes.NODETYPE_FUNCTEST /* 1034 */:
                    return 65536;
                default:
                    return -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:22:? A[RETURN, SYNTHETIC] */
    public StepPattern stepPattern(int i, int i2, StepPattern stepPattern) throws TransformerException {
        StepPattern stepPattern2;
        int i3;
        int i4;
        int op = getOp(i);
        if (-1 == op) {
            return null;
        }
        int nextOpPos = getNextOpPos(i);
        if (op != 25) {
            switch (op) {
                case 50:
                    i4 = getArgLengthOfStep(i);
                    i = getFirstChildPosOfStep(i);
                    stepPattern2 = new StepPattern(1280, 10, 3);
                    break;
                case 51:
                    i4 = getArgLengthOfStep(i);
                    i3 = getFirstChildPosOfStep(i);
                    stepPattern2 = new StepPattern(2, getStepNS(i), getStepLocalName(i), 10, 2);
                    break;
                case 52:
                    i4 = getArgLengthOfStep(i);
                    i3 = getFirstChildPosOfStep(i);
                    getWhatToShow(i);
                    stepPattern2 = new StepPattern(getWhatToShow(i), getStepNS(i), getStepLocalName(i), 0, 3);
                    break;
                case 53:
                    i4 = getArgLengthOfStep(i);
                    i3 = getFirstChildPosOfStep(i);
                    stepPattern2 = new StepPattern(getWhatToShow(i), getStepNS(i), getStepLocalName(i), 10, 3);
                    break;
                default:
                    error("ER_UNKNOWN_MATCH_OPERATION", null);
                    return null;
            }
            stepPattern2.setPredicates(getCompiledPredicates(i3 + i4));
            if (stepPattern != null) {
                stepPattern2.setRelativePathPattern(stepPattern);
            }
            StepPattern stepPattern3 = stepPattern(nextOpPos, i2 + 1, stepPattern2);
            return stepPattern3 == null ? stepPattern3 : stepPattern2;
        }
        i4 = getOp(i + 1);
        stepPattern2 = new FunctionPattern(compileFunction(i), 10, 3);
        i3 = i;
        stepPattern2.setPredicates(getCompiledPredicates(i3 + i4));
        if (stepPattern != null) {
        }
        StepPattern stepPattern32 = stepPattern(nextOpPos, i2 + 1, stepPattern2);
        if (stepPattern32 == null) {
        }
    }

    public Expression[] getCompiledPredicates(int i) throws TransformerException {
        int countPredicates = countPredicates(i);
        if (countPredicates <= 0) {
            return null;
        }
        Expression[] expressionArr = new Expression[countPredicates];
        compilePredicates(i, expressionArr);
        return expressionArr;
    }

    public int countPredicates(int i) throws TransformerException {
        int i2 = 0;
        while (29 == getOp(i)) {
            i2++;
            i = getNextOpPos(i);
        }
        return i2;
    }

    private void compilePredicates(int i, Expression[] expressionArr) throws TransformerException {
        int i2 = 0;
        while (29 == getOp(i)) {
            expressionArr[i2] = predicate(i);
            i = getNextOpPos(i);
            i2++;
        }
    }

    /* access modifiers changed from: package-private */
    public Expression compileFunction(int i) throws TransformerException {
        int op = (getOp(i + 1) + i) - 1;
        int firstChildPos = getFirstChildPos(i);
        int op2 = getOp(firstChildPos);
        int i2 = firstChildPos + 1;
        if (-1 != op2) {
            Function function = this.m_functionTable.getFunction(op2);
            if (function instanceof FuncExtFunctionAvailable) {
                ((FuncExtFunctionAvailable) function).setFunctionTable(this.m_functionTable);
            }
            function.postCompileStep(this);
            int i3 = 0;
            while (i2 < op) {
                try {
                    function.setArg(compile(i2), i3);
                    i2 = getNextOpPos(i2);
                    i3++;
                } catch (WrongNumberArgsException e) {
                    this.m_errorHandler.fatalError(new TransformerException(XSLMessages.createXPATHMessage("ER_ONLY_ALLOWS", new Object[]{this.m_functionTable.getFunctionName(op2), e.getMessage()}), this.m_locator));
                }
            }
            function.checkNumberArgs(i3);
            return function;
        }
        error("ER_FUNCTION_TOKEN_NOT_FOUND", null);
        return null;
    }

    private synchronized long getNextMethodId() {
        long j;
        if (s_nextMethodId == Long.MAX_VALUE) {
            s_nextMethodId = 0;
        }
        j = s_nextMethodId;
        s_nextMethodId = 1 + j;
        return j;
    }

    private Expression compileExtension(int i) throws TransformerException {
        int op = (getOp(i + 1) + i) - 1;
        int firstChildPos = getFirstChildPos(i);
        int i2 = firstChildPos + 1;
        int i3 = i2 + 1;
        FuncExtFunction funcExtFunction = new FuncExtFunction((String) getTokenQueue().elementAt(getOp(firstChildPos)), (String) getTokenQueue().elementAt(getOp(i2)), String.valueOf(getNextMethodId()));
        int i4 = 0;
        while (i3 < op) {
            try {
                int nextOpPos = getNextOpPos(i3);
                funcExtFunction.setArg(compile(i3), i4);
                i4++;
                i3 = nextOpPos;
            } catch (WrongNumberArgsException unused) {
            }
        }
        return funcExtFunction;
    }

    public void warn(String str, Object[] objArr) throws TransformerException {
        String createXPATHWarning = XSLMessages.createXPATHWarning(str, objArr);
        ErrorListener errorListener = this.m_errorHandler;
        if (errorListener != null) {
            errorListener.warning(new TransformerException(createXPATHWarning, this.m_locator));
            return;
        }
        PrintStream printStream = System.out;
        printStream.println(createXPATHWarning + "; file " + this.m_locator.getSystemId() + "; line " + this.m_locator.getLineNumber() + "; column " + this.m_locator.getColumnNumber());
    }

    public void assertion(boolean z, String str) {
        if (!z) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_INCORRECT_PROGRAMMER_ASSERTION", new Object[]{str}));
        }
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.compiler.OpMap
    public void error(String str, Object[] objArr) throws TransformerException {
        String createXPATHMessage = XSLMessages.createXPATHMessage(str, objArr);
        ErrorListener errorListener = this.m_errorHandler;
        if (errorListener != null) {
            errorListener.fatalError(new TransformerException(createXPATHMessage, this.m_locator));
            return;
        }
        throw new TransformerException(createXPATHMessage, this.m_locator);
    }

    public PrefixResolver getNamespaceContext() {
        return this.m_currentPrefixResolver;
    }

    public void setNamespaceContext(PrefixResolver prefixResolver) {
        this.m_currentPrefixResolver = prefixResolver;
    }
}

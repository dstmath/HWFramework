package org.apache.xpath.compiler;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xpath.Expression;
import org.apache.xpath.axes.UnionPathIterator;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.functions.FuncExtFunctionAvailable;
import org.apache.xpath.functions.Function;
import org.apache.xpath.functions.WrongNumberArgsException;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XString;
import org.apache.xpath.operations.And;
import org.apache.xpath.operations.Bool;
import org.apache.xpath.operations.Div;
import org.apache.xpath.operations.Equals;
import org.apache.xpath.operations.Gt;
import org.apache.xpath.operations.Gte;
import org.apache.xpath.operations.Lt;
import org.apache.xpath.operations.Lte;
import org.apache.xpath.operations.Minus;
import org.apache.xpath.operations.Mod;
import org.apache.xpath.operations.Mult;
import org.apache.xpath.operations.Neg;
import org.apache.xpath.operations.NotEquals;
import org.apache.xpath.operations.Number;
import org.apache.xpath.operations.Operation;
import org.apache.xpath.operations.Or;
import org.apache.xpath.operations.Plus;
import org.apache.xpath.operations.String;
import org.apache.xpath.operations.UnaryOperation;
import org.apache.xpath.operations.Variable;
import org.apache.xpath.patterns.FunctionPattern;
import org.apache.xpath.patterns.StepPattern;
import org.apache.xpath.patterns.UnionPattern;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class Compiler extends OpMap {
    private static final boolean DEBUG = false;
    private static long s_nextMethodId = 0;
    private int locPathDepth;
    private PrefixResolver m_currentPrefixResolver;
    ErrorListener m_errorHandler;
    private FunctionTable m_functionTable;
    SourceLocator m_locator;

    public Compiler(ErrorListener errorHandler, SourceLocator locator, FunctionTable fTable) {
        this.locPathDepth = -1;
        this.m_currentPrefixResolver = null;
        this.m_errorHandler = errorHandler;
        this.m_locator = locator;
        this.m_functionTable = fTable;
    }

    public Compiler() {
        this.locPathDepth = -1;
        this.m_currentPrefixResolver = null;
        this.m_errorHandler = null;
        this.m_locator = null;
    }

    public Expression compile(int opPos) throws TransformerException {
        switch (getOp(opPos)) {
            case 1:
                return compile(opPos + 2);
            case 2:
                return or(opPos);
            case 3:
                return and(opPos);
            case 4:
                return notequals(opPos);
            case 5:
                return equals(opPos);
            case 6:
                return lte(opPos);
            case 7:
                return lt(opPos);
            case 8:
                return gte(opPos);
            case 9:
                return gt(opPos);
            case 10:
                return plus(opPos);
            case 11:
                return minus(opPos);
            case 12:
                return mult(opPos);
            case 13:
                return div(opPos);
            case 14:
                return mod(opPos);
            case 15:
                error(XPATHErrorResources.ER_UNKNOWN_OPCODE, new Object[]{"quo"});
                return null;
            case 16:
                return neg(opPos);
            case 17:
                return string(opPos);
            case 18:
                return bool(opPos);
            case 19:
                return number(opPos);
            case 20:
                return union(opPos);
            case 21:
                return literal(opPos);
            case 22:
                return variable(opPos);
            case 23:
                return group(opPos);
            case 24:
                return compileExtension(opPos);
            case 25:
                return compileFunction(opPos);
            case 26:
                return arg(opPos);
            case 27:
                return numberlit(opPos);
            case 28:
                return locationPath(opPos);
            case 29:
                return null;
            case 30:
                return matchPattern(opPos + 2);
            case 31:
                return locationPathPattern(opPos);
            default:
                error(XPATHErrorResources.ER_UNKNOWN_OPCODE, new Object[]{Integer.toString(getOp(opPos))});
                return null;
        }
    }

    private Expression compileOperation(Operation operation, int opPos) throws TransformerException {
        int leftPos = OpMap.getFirstChildPos(opPos);
        operation.setLeftRight(compile(leftPos), compile(getNextOpPos(leftPos)));
        return operation;
    }

    private Expression compileUnary(UnaryOperation unary, int opPos) throws TransformerException {
        unary.setRight(compile(OpMap.getFirstChildPos(opPos)));
        return unary;
    }

    protected Expression or(int opPos) throws TransformerException {
        return compileOperation(new Or(), opPos);
    }

    protected Expression and(int opPos) throws TransformerException {
        return compileOperation(new And(), opPos);
    }

    protected Expression notequals(int opPos) throws TransformerException {
        return compileOperation(new NotEquals(), opPos);
    }

    protected Expression equals(int opPos) throws TransformerException {
        return compileOperation(new Equals(), opPos);
    }

    protected Expression lte(int opPos) throws TransformerException {
        return compileOperation(new Lte(), opPos);
    }

    protected Expression lt(int opPos) throws TransformerException {
        return compileOperation(new Lt(), opPos);
    }

    protected Expression gte(int opPos) throws TransformerException {
        return compileOperation(new Gte(), opPos);
    }

    protected Expression gt(int opPos) throws TransformerException {
        return compileOperation(new Gt(), opPos);
    }

    protected Expression plus(int opPos) throws TransformerException {
        return compileOperation(new Plus(), opPos);
    }

    protected Expression minus(int opPos) throws TransformerException {
        return compileOperation(new Minus(), opPos);
    }

    protected Expression mult(int opPos) throws TransformerException {
        return compileOperation(new Mult(), opPos);
    }

    protected Expression div(int opPos) throws TransformerException {
        return compileOperation(new Div(), opPos);
    }

    protected Expression mod(int opPos) throws TransformerException {
        return compileOperation(new Mod(), opPos);
    }

    protected Expression neg(int opPos) throws TransformerException {
        return compileUnary(new Neg(), opPos);
    }

    protected Expression string(int opPos) throws TransformerException {
        return compileUnary(new String(), opPos);
    }

    protected Expression bool(int opPos) throws TransformerException {
        return compileUnary(new Bool(), opPos);
    }

    protected Expression number(int opPos) throws TransformerException {
        return compileUnary(new Number(), opPos);
    }

    protected Expression literal(int opPos) {
        return (XString) getTokenQueue().elementAt(getOp(OpMap.getFirstChildPos(opPos)));
    }

    protected Expression numberlit(int opPos) {
        return (XNumber) getTokenQueue().elementAt(getOp(OpMap.getFirstChildPos(opPos)));
    }

    protected Expression variable(int opPos) throws TransformerException {
        String namespace;
        Variable var = new Variable();
        opPos = OpMap.getFirstChildPos(opPos);
        int nsPos = getOp(opPos);
        if (-2 == nsPos) {
            namespace = null;
        } else {
            namespace = (String) getTokenQueue().elementAt(nsPos);
        }
        var.setQName(new QName(namespace, (String) getTokenQueue().elementAt(getOp(opPos + 1))));
        return var;
    }

    protected Expression group(int opPos) throws TransformerException {
        return compile(opPos + 2);
    }

    protected Expression arg(int opPos) throws TransformerException {
        return compile(opPos + 2);
    }

    protected Expression union(int opPos) throws TransformerException {
        this.locPathDepth++;
        try {
            Expression createUnionIterator = UnionPathIterator.createUnionIterator(this, opPos);
            return createUnionIterator;
        } finally {
            this.locPathDepth--;
        }
    }

    public int getLocationPathDepth() {
        return this.locPathDepth;
    }

    FunctionTable getFunctionTable() {
        return this.m_functionTable;
    }

    public Expression locationPath(int opPos) throws TransformerException {
        boolean z = false;
        this.locPathDepth++;
        try {
            if (this.locPathDepth == 0) {
                z = true;
            }
            Expression expression = (Expression) WalkerFactory.newDTMIterator(this, opPos, z);
            return expression;
        } finally {
            this.locPathDepth--;
        }
    }

    public Expression predicate(int opPos) throws TransformerException {
        return compile(opPos + 2);
    }

    protected Expression matchPattern(int opPos) throws TransformerException {
        this.locPathDepth++;
        int nextOpPos = opPos;
        int i = 0;
        while (getOp(nextOpPos) == 31) {
            try {
                nextOpPos = getNextOpPos(nextOpPos);
                i++;
            } finally {
                this.locPathDepth--;
            }
        }
        if (i == 1) {
            Expression compile = compile(opPos);
            return compile;
        }
        UnionPattern up = new UnionPattern();
        StepPattern[] patterns = new StepPattern[i];
        i = 0;
        while (getOp(opPos) == 31) {
            nextOpPos = getNextOpPos(opPos);
            patterns[i] = (StepPattern) compile(opPos);
            opPos = nextOpPos;
            i++;
        }
        up.setPatterns(patterns);
        this.locPathDepth--;
        return up;
    }

    public Expression locationPathPattern(int opPos) throws TransformerException {
        return stepPattern(OpMap.getFirstChildPos(opPos), 0, null);
    }

    public int getWhatToShow(int opPos) {
        int axesType = getOp(opPos);
        switch (getOp(opPos + 3)) {
            case 34:
                switch (axesType) {
                    case 39:
                    case 51:
                        return 2;
                    case 49:
                        return 4096;
                    case 52:
                    case 53:
                        return 1;
                    default:
                        return 1;
                }
            case 35:
                return 1280;
            case OpCodes.NODETYPE_COMMENT /*1030*/:
                return 128;
            case OpCodes.NODETYPE_TEXT /*1031*/:
                return 12;
            case OpCodes.NODETYPE_PI /*1032*/:
                return 64;
            case OpCodes.NODETYPE_NODE /*1033*/:
                switch (axesType) {
                    case 38:
                    case 42:
                    case 48:
                        return -1;
                    case 39:
                    case 51:
                        return 2;
                    case 49:
                        return 4096;
                    default:
                        if (getOp(0) == 30) {
                            return -1283;
                        }
                        return -3;
                }
            case OpCodes.NODETYPE_FUNCTEST /*1034*/:
                return 65536;
            default:
                return -1;
        }
    }

    protected StepPattern stepPattern(int opPos, int stepCount, StepPattern ancestorPattern) throws TransformerException {
        int startOpPos = opPos;
        int stepType = getOp(opPos);
        if (-1 == stepType) {
            return null;
        }
        int argLen;
        StepPattern pattern;
        int endStep = getNextOpPos(opPos);
        switch (stepType) {
            case 25:
                argLen = getOp(opPos + 1);
                pattern = new FunctionPattern(compileFunction(opPos), 10, 3);
                break;
            case 50:
                argLen = getArgLengthOfStep(opPos);
                opPos = OpMap.getFirstChildPosOfStep(opPos);
                pattern = new StepPattern(1280, 10, 3);
                break;
            case 51:
                argLen = getArgLengthOfStep(opPos);
                opPos = OpMap.getFirstChildPosOfStep(opPos);
                pattern = new StepPattern(2, getStepNS(startOpPos), getStepLocalName(startOpPos), 10, 2);
                break;
            case 52:
                argLen = getArgLengthOfStep(opPos);
                opPos = OpMap.getFirstChildPosOfStep(opPos);
                if (1280 == getWhatToShow(startOpPos)) {
                }
                pattern = new StepPattern(getWhatToShow(startOpPos), getStepNS(startOpPos), getStepLocalName(startOpPos), 0, 3);
                break;
            case 53:
                argLen = getArgLengthOfStep(opPos);
                opPos = OpMap.getFirstChildPosOfStep(opPos);
                pattern = new StepPattern(getWhatToShow(startOpPos), getStepNS(startOpPos), getStepLocalName(startOpPos), 10, 3);
                break;
            default:
                error(XPATHErrorResources.ER_UNKNOWN_MATCH_OPERATION, null);
                return null;
        }
        pattern.setPredicates(getCompiledPredicates(opPos + argLen));
        if (ancestorPattern != null) {
            pattern.setRelativePathPattern(ancestorPattern);
        }
        StepPattern relativePathPattern = stepPattern(endStep, stepCount + 1, pattern);
        if (relativePathPattern == null) {
            relativePathPattern = pattern;
        }
        return relativePathPattern;
    }

    public Expression[] getCompiledPredicates(int opPos) throws TransformerException {
        int count = countPredicates(opPos);
        if (count <= 0) {
            return null;
        }
        Expression[] predicates = new Expression[count];
        compilePredicates(opPos, predicates);
        return predicates;
    }

    public int countPredicates(int opPos) throws TransformerException {
        int count = 0;
        while (29 == getOp(opPos)) {
            count++;
            opPos = getNextOpPos(opPos);
        }
        return count;
    }

    private void compilePredicates(int opPos, Expression[] predicates) throws TransformerException {
        int i = 0;
        while (29 == getOp(opPos)) {
            predicates[i] = predicate(opPos);
            opPos = getNextOpPos(opPos);
            i++;
        }
    }

    Expression compileFunction(int opPos) throws TransformerException {
        int endFunc = (getOp(opPos + 1) + opPos) - 1;
        opPos = OpMap.getFirstChildPos(opPos);
        int funcID = getOp(opPos);
        opPos++;
        if (-1 != funcID) {
            Function func = this.m_functionTable.getFunction(funcID);
            if (func instanceof FuncExtFunctionAvailable) {
                ((FuncExtFunctionAvailable) func).setFunctionTable(this.m_functionTable);
            }
            func.postCompileStep(this);
            int i = 0;
            int p = opPos;
            while (p < endFunc) {
                try {
                    func.setArg(compile(p), i);
                    p = getNextOpPos(p);
                    i++;
                } catch (WrongNumberArgsException wnae) {
                    String name = this.m_functionTable.getFunctionName(funcID);
                    this.m_errorHandler.fatalError(new TransformerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ONLY_ALLOWS, new Object[]{name, wnae.getMessage()}), this.m_locator));
                }
            }
            func.checkNumberArgs(i);
            return func;
        }
        error(XPATHErrorResources.ER_FUNCTION_TOKEN_NOT_FOUND, null);
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

    private Expression compileExtension(int opPos) throws TransformerException {
        int endExtFunc = (getOp(opPos + 1) + opPos) - 1;
        opPos = OpMap.getFirstChildPos(opPos);
        opPos++;
        opPos++;
        Function extension = new FuncExtFunction((String) getTokenQueue().elementAt(getOp(opPos)), (String) getTokenQueue().elementAt(getOp(opPos)), String.valueOf(getNextMethodId()));
        int i = 0;
        while (opPos < endExtFunc) {
            try {
                int nextOpPos = getNextOpPos(opPos);
                extension.setArg(compile(opPos), i);
                opPos = nextOpPos;
                i++;
            } catch (WrongNumberArgsException e) {
            }
        }
        return extension;
    }

    public void warn(String msg, Object[] args) throws TransformerException {
        String fmsg = XPATHMessages.createXPATHWarning(msg, args);
        if (this.m_errorHandler != null) {
            this.m_errorHandler.warning(new TransformerException(fmsg, this.m_locator));
        } else {
            System.out.println(fmsg + "; file " + this.m_locator.getSystemId() + "; line " + this.m_locator.getLineNumber() + "; column " + this.m_locator.getColumnNumber());
        }
    }

    public void assertion(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION, new Object[]{msg}));
        }
    }

    public void error(String msg, Object[] args) throws TransformerException {
        String fmsg = XPATHMessages.createXPATHMessage(msg, args);
        if (this.m_errorHandler != null) {
            this.m_errorHandler.fatalError(new TransformerException(fmsg, this.m_locator));
            return;
        }
        throw new TransformerException(fmsg, (SAXSourceLocator) this.m_locator);
    }

    public PrefixResolver getNamespaceContext() {
        return this.m_currentPrefixResolver;
    }

    public void setNamespaceContext(PrefixResolver pr) {
        this.m_currentPrefixResolver = pr;
    }
}

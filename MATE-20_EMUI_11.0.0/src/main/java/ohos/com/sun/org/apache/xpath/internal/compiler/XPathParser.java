package ohos.com.sun.org.apache.xpath.internal.compiler;

import ohos.com.sun.org.apache.xalan.internal.res.XSLMessages;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.com.sun.org.apache.xml.internal.utils.ObjectVector;
import ohos.com.sun.org.apache.xml.internal.utils.PrefixResolver;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathProcessorException;
import ohos.com.sun.org.apache.xpath.internal.domapi.XPathStylesheetDOM3Exception;
import ohos.com.sun.org.apache.xpath.internal.objects.XNumber;
import ohos.com.sun.org.apache.xpath.internal.objects.XString;
import ohos.com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.TransformerException;

public class XPathParser {
    public static final String CONTINUE_AFTER_FATAL_ERROR = "CONTINUE_AFTER_FATAL_ERROR";
    protected static final int FILTER_MATCH_FAILED = 0;
    protected static final int FILTER_MATCH_PREDICATES = 2;
    protected static final int FILTER_MATCH_PRIMARY = 1;
    private int countPredicate;
    private ErrorListener m_errorListener;
    private FunctionTable m_functionTable;
    PrefixResolver m_namespaceContext;
    private OpMap m_ops;
    int m_queueMark = 0;
    SourceLocator m_sourceLocator;
    transient String m_token;
    transient char m_tokenChar = 0;

    public XPathParser(ErrorListener errorListener, SourceLocator sourceLocator) {
        this.m_errorListener = errorListener;
        this.m_sourceLocator = sourceLocator;
    }

    /* JADX WARN: Type inference failed for: r7v3, types: [java.lang.Throwable, ohos.com.sun.org.apache.xpath.internal.XPathProcessorException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void initXPath(Compiler compiler, String str, PrefixResolver prefixResolver) throws TransformerException {
        this.m_ops = compiler;
        this.m_namespaceContext = prefixResolver;
        this.m_functionTable = compiler.getFunctionTable();
        new Lexer(compiler, prefixResolver, this).tokenize(str);
        this.m_ops.setOp(0, 1);
        this.m_ops.setOp(1, 2);
        try {
            nextToken();
            Expr();
            if (this.m_token != null) {
                String str2 = "";
                while (this.m_token != null) {
                    str2 = str2 + "'" + this.m_token + "'";
                    nextToken();
                    if (this.m_token != null) {
                        str2 = str2 + ", ";
                    }
                }
                error("ER_EXTRA_ILLEGAL_TOKENS", new Object[]{str2});
            }
        } catch (XPathProcessorException e) {
            if (CONTINUE_AFTER_FATAL_ERROR.equals(e.getMessage())) {
                initXPath(compiler, "/..", prefixResolver);
            } else {
                throw e;
            }
        } catch (StackOverflowError unused) {
            error(XPATHErrorResources.ER_PREDICATE_TOO_MANY_OPEN, new Object[]{this.m_token, Integer.valueOf(this.m_queueMark), Integer.valueOf(this.countPredicate)});
        }
        compiler.shrink();
    }

    public void initMatchPattern(Compiler compiler, String str, PrefixResolver prefixResolver) throws TransformerException {
        this.m_ops = compiler;
        this.m_namespaceContext = prefixResolver;
        this.m_functionTable = compiler.getFunctionTable();
        new Lexer(compiler, prefixResolver, this).tokenize(str);
        this.m_ops.setOp(0, 30);
        this.m_ops.setOp(1, 2);
        nextToken();
        try {
            Pattern();
        } catch (StackOverflowError unused) {
            error(XPATHErrorResources.ER_PREDICATE_TOO_MANY_OPEN, new Object[]{this.m_token, Integer.valueOf(this.m_queueMark), Integer.valueOf(this.countPredicate)});
        }
        if (this.m_token != null) {
            String str2 = "";
            while (this.m_token != null) {
                str2 = str2 + "'" + this.m_token + "'";
                nextToken();
                if (this.m_token != null) {
                    str2 = str2 + ", ";
                }
            }
            error("ER_EXTRA_ILLEGAL_TOKENS", new Object[]{str2});
        }
        OpMap opMap = this.m_ops;
        opMap.setOp(opMap.getOp(1), -1);
        OpMap opMap2 = this.m_ops;
        opMap2.setOp(1, opMap2.getOp(1) + 1);
        this.m_ops.shrink();
    }

    public void setErrorHandler(ErrorListener errorListener) {
        this.m_errorListener = errorListener;
    }

    public ErrorListener getErrorListener() {
        return this.m_errorListener;
    }

    /* access modifiers changed from: package-private */
    public final boolean tokenIs(String str) {
        String str2 = this.m_token;
        if (str2 != null) {
            return str2.equals(str);
        }
        return str == null;
    }

    /* access modifiers changed from: package-private */
    public final boolean tokenIs(char c) {
        return this.m_token != null && this.m_tokenChar == c;
    }

    /* access modifiers changed from: package-private */
    public final boolean lookahead(char c, int i) {
        int i2 = this.m_queueMark + i;
        if (i2 > this.m_ops.getTokenQueueSize() || i2 <= 0 || this.m_ops.getTokenQueueSize() == 0) {
            return false;
        }
        String str = (String) this.m_ops.m_tokenQueue.elementAt(i2 - 1);
        if (str.length() == 1 && str.charAt(0) == c) {
            return true;
        }
        return false;
    }

    private final boolean lookbehind(char c, int i) {
        char charAt;
        int i2 = this.m_queueMark - (i + 1);
        if (i2 < 0) {
            return false;
        }
        String str = (String) this.m_ops.m_tokenQueue.elementAt(i2);
        return str.length() == 1 && (charAt = str.charAt(0)) != '|' && charAt == c;
    }

    private final boolean lookbehindHasToken(int i) {
        char c;
        if (this.m_queueMark - i <= 0) {
            return false;
        }
        String str = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark - (i - 1));
        if (str == null) {
            c = '|';
        } else {
            c = str.charAt(0);
        }
        if (c == '|') {
            return false;
        }
        return true;
    }

    private final boolean lookahead(String str, int i) {
        if (this.m_queueMark + i <= this.m_ops.getTokenQueueSize()) {
            String str2 = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark + (i - 1));
            if (str2 != null) {
                return str2.equals(str);
            }
            if (str != null) {
                return false;
            }
        } else if (str != null) {
            return false;
        }
        return true;
    }

    private final void nextToken() {
        if (this.m_queueMark < this.m_ops.getTokenQueueSize()) {
            ObjectVector objectVector = this.m_ops.m_tokenQueue;
            int i = this.m_queueMark;
            this.m_queueMark = i + 1;
            this.m_token = (String) objectVector.elementAt(i);
            this.m_tokenChar = this.m_token.charAt(0);
            return;
        }
        this.m_token = null;
        this.m_tokenChar = 0;
    }

    private final String getTokenRelative(int i) {
        int i2 = this.m_queueMark + i;
        if (i2 <= 0 || i2 >= this.m_ops.getTokenQueueSize()) {
            return null;
        }
        return (String) this.m_ops.m_tokenQueue.elementAt(i2);
    }

    private final void prevToken() {
        int i = this.m_queueMark;
        if (i > 0) {
            this.m_queueMark = i - 1;
            this.m_token = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark);
            this.m_tokenChar = this.m_token.charAt(0);
            return;
        }
        this.m_token = null;
        this.m_tokenChar = 0;
    }

    /* JADX WARN: Type inference failed for: r2v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xpath.internal.XPathProcessorException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private final void consumeExpected(String str) throws TransformerException {
        if (tokenIs(str)) {
            nextToken();
        } else {
            error("ER_EXPECTED_BUT_FOUND", new Object[]{str, this.m_token});
            throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
        }
    }

    /* JADX WARN: Type inference failed for: r2v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xpath.internal.XPathProcessorException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private final void consumeExpected(char c) throws TransformerException {
        if (tokenIs(c)) {
            nextToken();
        } else {
            error("ER_EXPECTED_BUT_FOUND", new Object[]{String.valueOf(c), this.m_token});
            throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
        }
    }

    /* access modifiers changed from: package-private */
    public void warn(String str, Object[] objArr) throws TransformerException {
        String createXPATHWarning = XSLMessages.createXPATHWarning(str, objArr);
        ErrorListener errorListener = getErrorListener();
        if (errorListener != null) {
            errorListener.warning(new TransformerException(createXPATHWarning, this.m_sourceLocator));
        } else {
            System.err.println(createXPATHWarning);
        }
    }

    private void assertion(boolean z, String str) {
        if (!z) {
            throw new RuntimeException(XSLMessages.createXPATHMessage("ER_INCORRECT_PROGRAMMER_ASSERTION", new Object[]{str}));
        }
    }

    /* access modifiers changed from: package-private */
    public void error(String str, Object[] objArr) throws TransformerException {
        String createXPATHMessage = XSLMessages.createXPATHMessage(str, objArr);
        ErrorListener errorListener = getErrorListener();
        TransformerException transformerException = new TransformerException(createXPATHMessage, this.m_sourceLocator);
        if (errorListener != null) {
            errorListener.fatalError(transformerException);
            return;
        }
        throw transformerException;
    }

    /* access modifiers changed from: package-private */
    public void errorForDOM3(String str, Object[] objArr) throws TransformerException {
        String createXPATHMessage = XSLMessages.createXPATHMessage(str, objArr);
        ErrorListener errorListener = getErrorListener();
        TransformerException xPathStylesheetDOM3Exception = new XPathStylesheetDOM3Exception(createXPATHMessage, this.m_sourceLocator);
        if (errorListener != null) {
            errorListener.fatalError(xPathStylesheetDOM3Exception);
            return;
        }
        throw xPathStylesheetDOM3Exception;
    }

    /* access modifiers changed from: protected */
    public String dumpRemainingTokenQueue() {
        int i = this.m_queueMark;
        if (i >= this.m_ops.getTokenQueueSize()) {
            return "";
        }
        String str = "\n Remaining tokens: (";
        while (i < this.m_ops.getTokenQueueSize()) {
            str = str + " '" + ((String) this.m_ops.m_tokenQueue.elementAt(i)) + "'";
            i++;
        }
        return str + ")";
    }

    /* access modifiers changed from: package-private */
    public final int getFunctionToken(String str) {
        try {
            Object lookupNodeTest = Keywords.lookupNodeTest(str);
            if (lookupNodeTest == null) {
                lookupNodeTest = this.m_functionTable.getFunctionID(str);
            }
            return ((Integer) lookupNodeTest).intValue();
        } catch (ClassCastException | NullPointerException unused) {
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public void insertOp(int i, int i2, int i3) {
        int op = this.m_ops.getOp(1);
        for (int i4 = op - 1; i4 >= i; i4--) {
            OpMap opMap = this.m_ops;
            opMap.setOp(i4 + i2, opMap.getOp(i4));
        }
        this.m_ops.setOp(i, i3);
        this.m_ops.setOp(1, op + i2);
    }

    /* access modifiers changed from: package-private */
    public void appendOp(int i, int i2) {
        int op = this.m_ops.getOp(1);
        this.m_ops.setOp(op, i2);
        this.m_ops.setOp(op + 1, i);
        this.m_ops.setOp(1, op + i);
    }

    /* access modifiers changed from: protected */
    public void Expr() throws TransformerException {
        OrExpr();
    }

    /* access modifiers changed from: protected */
    public void OrExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        AndExpr();
        if (this.m_token != null && tokenIs("or")) {
            nextToken();
            insertOp(op, 2, 2);
            OrExpr();
            OpMap opMap = this.m_ops;
            opMap.setOp(op + 1, opMap.getOp(1) - op);
        }
    }

    /* access modifiers changed from: protected */
    public void AndExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        EqualityExpr(-1);
        if (this.m_token != null && tokenIs("and")) {
            nextToken();
            insertOp(op, 2, 3);
            AndExpr();
            OpMap opMap = this.m_ops;
            opMap.setOp(op + 1, opMap.getOp(1) - op);
        }
    }

    /* access modifiers changed from: protected */
    public int EqualityExpr(int i) throws TransformerException {
        int i2;
        int op = this.m_ops.getOp(1);
        if (-1 == i) {
            i = op;
        }
        RelationalExpr(-1);
        if (this.m_token == null) {
            return i;
        }
        if (tokenIs('!') && lookahead('=', 1)) {
            nextToken();
            nextToken();
            insertOp(i, 2, 4);
            int op2 = this.m_ops.getOp(1) - i;
            i2 = EqualityExpr(i);
            OpMap opMap = this.m_ops;
            opMap.setOp(i2 + 1, opMap.getOp(i2 + op2 + 1) + op2);
        } else if (!tokenIs('=')) {
            return i;
        } else {
            nextToken();
            insertOp(i, 2, 5);
            int op3 = this.m_ops.getOp(1) - i;
            i2 = EqualityExpr(i);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(i2 + 1, opMap2.getOp(i2 + op3 + 1) + op3);
        }
        return i2 + 2;
    }

    /* access modifiers changed from: protected */
    public int RelationalExpr(int i) throws TransformerException {
        int RelationalExpr;
        int op = this.m_ops.getOp(1);
        if (-1 == i) {
            i = op;
        }
        AdditiveExpr(-1);
        if (this.m_token == null) {
            return i;
        }
        if (tokenIs('<')) {
            nextToken();
            if (tokenIs('=')) {
                nextToken();
                insertOp(i, 2, 6);
            } else {
                insertOp(i, 2, 7);
            }
            int op2 = this.m_ops.getOp(1) - i;
            RelationalExpr = RelationalExpr(i);
            OpMap opMap = this.m_ops;
            opMap.setOp(RelationalExpr + 1, opMap.getOp(RelationalExpr + op2 + 1) + op2);
        } else if (!tokenIs('>')) {
            return i;
        } else {
            nextToken();
            if (tokenIs('=')) {
                nextToken();
                insertOp(i, 2, 8);
            } else {
                insertOp(i, 2, 9);
            }
            int op3 = this.m_ops.getOp(1) - i;
            RelationalExpr = RelationalExpr(i);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(RelationalExpr + 1, opMap2.getOp(RelationalExpr + op3 + 1) + op3);
        }
        return RelationalExpr + 2;
    }

    /* access modifiers changed from: protected */
    public int AdditiveExpr(int i) throws TransformerException {
        int AdditiveExpr;
        int op = this.m_ops.getOp(1);
        if (-1 == i) {
            i = op;
        }
        MultiplicativeExpr(-1);
        if (this.m_token == null) {
            return i;
        }
        if (tokenIs('+')) {
            nextToken();
            insertOp(i, 2, 10);
            int op2 = this.m_ops.getOp(1) - i;
            AdditiveExpr = AdditiveExpr(i);
            OpMap opMap = this.m_ops;
            opMap.setOp(AdditiveExpr + 1, opMap.getOp(AdditiveExpr + op2 + 1) + op2);
        } else if (!tokenIs(LocaleUtility.IETF_SEPARATOR)) {
            return i;
        } else {
            nextToken();
            insertOp(i, 2, 11);
            int op3 = this.m_ops.getOp(1) - i;
            AdditiveExpr = AdditiveExpr(i);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(AdditiveExpr + 1, opMap2.getOp(AdditiveExpr + op3 + 1) + op3);
        }
        return AdditiveExpr + 2;
    }

    /* access modifiers changed from: protected */
    public int MultiplicativeExpr(int i) throws TransformerException {
        int MultiplicativeExpr;
        int op = this.m_ops.getOp(1);
        if (-1 == i) {
            i = op;
        }
        UnaryExpr();
        if (this.m_token == null) {
            return i;
        }
        if (tokenIs('*')) {
            nextToken();
            insertOp(i, 2, 12);
            int op2 = this.m_ops.getOp(1) - i;
            MultiplicativeExpr = MultiplicativeExpr(i);
            OpMap opMap = this.m_ops;
            opMap.setOp(MultiplicativeExpr + 1, opMap.getOp(MultiplicativeExpr + op2 + 1) + op2);
        } else if (tokenIs("div")) {
            nextToken();
            insertOp(i, 2, 13);
            int op3 = this.m_ops.getOp(1) - i;
            MultiplicativeExpr = MultiplicativeExpr(i);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(MultiplicativeExpr + 1, opMap2.getOp(MultiplicativeExpr + op3 + 1) + op3);
        } else if (tokenIs("mod")) {
            nextToken();
            insertOp(i, 2, 14);
            int op4 = this.m_ops.getOp(1) - i;
            MultiplicativeExpr = MultiplicativeExpr(i);
            OpMap opMap3 = this.m_ops;
            opMap3.setOp(MultiplicativeExpr + 1, opMap3.getOp(MultiplicativeExpr + op4 + 1) + op4);
        } else if (!tokenIs("quo")) {
            return i;
        } else {
            nextToken();
            insertOp(i, 2, 15);
            int op5 = this.m_ops.getOp(1) - i;
            MultiplicativeExpr = MultiplicativeExpr(i);
            OpMap opMap4 = this.m_ops;
            opMap4.setOp(MultiplicativeExpr + 1, opMap4.getOp(MultiplicativeExpr + op5 + 1) + op5);
        }
        return MultiplicativeExpr + 2;
    }

    /* access modifiers changed from: protected */
    public void UnaryExpr() throws TransformerException {
        boolean z;
        int op = this.m_ops.getOp(1);
        if (this.m_tokenChar == '-') {
            nextToken();
            appendOp(2, 16);
            z = true;
        } else {
            z = false;
        }
        UnionExpr();
        if (z) {
            OpMap opMap = this.m_ops;
            opMap.setOp(op + 1, opMap.getOp(1) - op);
        }
    }

    /* access modifiers changed from: protected */
    public void StringExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        appendOp(2, 17);
        Expr();
        OpMap opMap = this.m_ops;
        opMap.setOp(op + 1, opMap.getOp(1) - op);
    }

    /* access modifiers changed from: protected */
    public void BooleanExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        appendOp(2, 18);
        Expr();
        int op2 = this.m_ops.getOp(1) - op;
        if (op2 == 2) {
            error("ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL", null);
        }
        this.m_ops.setOp(op + 1, op2);
    }

    /* access modifiers changed from: protected */
    public void NumberExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        appendOp(2, 19);
        Expr();
        OpMap opMap = this.m_ops;
        opMap.setOp(op + 1, opMap.getOp(1) - op);
    }

    /* access modifiers changed from: protected */
    public void UnionExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        boolean z = false;
        while (true) {
            PathExpr();
            if (tokenIs('|')) {
                if (!z) {
                    insertOp(op, 2, 20);
                    z = true;
                }
                nextToken();
            } else {
                OpMap opMap = this.m_ops;
                opMap.setOp(op + 1, opMap.getOp(1) - op);
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void PathExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        int FilterExpr = FilterExpr();
        if (FilterExpr != 0) {
            boolean z = FilterExpr == 2;
            if (tokenIs('/')) {
                nextToken();
                if (!z) {
                    insertOp(op, 2, 28);
                    z = true;
                }
                if (!RelativeLocationPath()) {
                    error("ER_EXPECTED_REL_LOC_PATH", null);
                }
            }
            if (z) {
                OpMap opMap = this.m_ops;
                opMap.setOp(opMap.getOp(1), -1);
                OpMap opMap2 = this.m_ops;
                opMap2.setOp(1, opMap2.getOp(1) + 1);
                OpMap opMap3 = this.m_ops;
                opMap3.setOp(op + 1, opMap3.getOp(1) - op);
                return;
            }
            return;
        }
        LocationPath();
    }

    /* access modifiers changed from: protected */
    public int FilterExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        if (!PrimaryExpr()) {
            return 0;
        }
        if (!tokenIs('[')) {
            return 1;
        }
        insertOp(op, 2, 28);
        while (tokenIs('[')) {
            Predicate();
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public boolean PrimaryExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        char c = this.m_tokenChar;
        if (c == '\'' || c == '\"') {
            appendOp(2, 21);
            Literal();
            OpMap opMap = this.m_ops;
            opMap.setOp(op + 1, opMap.getOp(1) - op);
            return true;
        } else if (c == '$') {
            nextToken();
            appendOp(2, 22);
            QName();
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(op + 1, opMap2.getOp(1) - op);
            return true;
        } else if (c == '(') {
            nextToken();
            appendOp(2, 23);
            Expr();
            consumeExpected(')');
            OpMap opMap3 = this.m_ops;
            opMap3.setOp(op + 1, opMap3.getOp(1) - op);
            return true;
        } else {
            String str = this.m_token;
            if (str != null && (('.' == c && str.length() > 1 && Character.isDigit(this.m_token.charAt(1))) || Character.isDigit(this.m_tokenChar))) {
                appendOp(2, 27);
                Number();
                OpMap opMap4 = this.m_ops;
                opMap4.setOp(op + 1, opMap4.getOp(1) - op);
                return true;
            } else if (lookahead('(', 1) || (lookahead(':', 1) && lookahead('(', 3))) {
                return FunctionCall();
            } else {
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void Argument() throws TransformerException {
        int op = this.m_ops.getOp(1);
        appendOp(2, 26);
        Expr();
        OpMap opMap = this.m_ops;
        opMap.setOp(op + 1, opMap.getOp(1) - op);
    }

    /* access modifiers changed from: protected */
    public boolean FunctionCall() throws TransformerException {
        int op = this.m_ops.getOp(1);
        if (!lookahead(':', 1)) {
            int functionToken = getFunctionToken(this.m_token);
            if (-1 == functionToken) {
                error("ER_COULDNOT_FIND_FUNCTION", new Object[]{this.m_token});
            }
            switch (functionToken) {
                case OpCodes.NODETYPE_COMMENT /* 1030 */:
                case OpCodes.NODETYPE_TEXT /* 1031 */:
                case 1032:
                case OpCodes.NODETYPE_NODE /* 1033 */:
                    return false;
                default:
                    appendOp(3, 25);
                    this.m_ops.setOp(op + 1 + 1, functionToken);
                    nextToken();
                    break;
            }
        } else {
            appendOp(4, 24);
            int i = op + 1;
            this.m_ops.setOp(i + 1, this.m_queueMark - 1);
            nextToken();
            consumeExpected(':');
            this.m_ops.setOp(i + 2, this.m_queueMark - 1);
            nextToken();
        }
        consumeExpected('(');
        while (!tokenIs(')') && this.m_token != null) {
            if (tokenIs(',')) {
                error("ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG", null);
            }
            Argument();
            if (!tokenIs(')')) {
                consumeExpected(',');
                if (tokenIs(')')) {
                    error("ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG", null);
                }
            }
        }
        consumeExpected(')');
        OpMap opMap = this.m_ops;
        opMap.setOp(opMap.getOp(1), -1);
        OpMap opMap2 = this.m_ops;
        opMap2.setOp(1, opMap2.getOp(1) + 1);
        OpMap opMap3 = this.m_ops;
        opMap3.setOp(op + 1, opMap3.getOp(1) - op);
        return true;
    }

    /* access modifiers changed from: protected */
    public void LocationPath() throws TransformerException {
        int op = this.m_ops.getOp(1);
        appendOp(2, 28);
        boolean z = tokenIs('/');
        if (z) {
            appendOp(4, 50);
            OpMap opMap = this.m_ops;
            opMap.setOp(opMap.getOp(1) - 2, 4);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(opMap2.getOp(1) - 1, 35);
            nextToken();
        } else if (this.m_token == null) {
            error("ER_EXPECTED_LOC_PATH_AT_END_EXPR", null);
        }
        if (this.m_token != null && !RelativeLocationPath() && !z) {
            error("ER_EXPECTED_LOC_PATH", new Object[]{this.m_token});
        }
        OpMap opMap3 = this.m_ops;
        opMap3.setOp(opMap3.getOp(1), -1);
        OpMap opMap4 = this.m_ops;
        opMap4.setOp(1, opMap4.getOp(1) + 1);
        OpMap opMap5 = this.m_ops;
        opMap5.setOp(op + 1, opMap5.getOp(1) - op);
    }

    /* access modifiers changed from: protected */
    public boolean RelativeLocationPath() throws TransformerException {
        if (!Step()) {
            return false;
        }
        while (tokenIs('/')) {
            nextToken();
            if (!Step()) {
                error("ER_EXPECTED_LOC_STEP", null);
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean Step() throws TransformerException {
        String str;
        int op = this.m_ops.getOp(1);
        boolean z = tokenIs('/');
        if (z) {
            nextToken();
            appendOp(2, 42);
            OpMap opMap = this.m_ops;
            opMap.setOp(1, opMap.getOp(1) + 1);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(opMap2.getOp(1), OpCodes.NODETYPE_NODE);
            OpMap opMap3 = this.m_ops;
            opMap3.setOp(1, opMap3.getOp(1) + 1);
            OpMap opMap4 = this.m_ops;
            int i = op + 1;
            opMap4.setOp(i + 1, opMap4.getOp(1) - op);
            OpMap opMap5 = this.m_ops;
            opMap5.setOp(i, opMap5.getOp(1) - op);
            op = this.m_ops.getOp(1);
        }
        if (tokenIs(".")) {
            nextToken();
            if (tokenIs('[')) {
                error("ER_PREDICATE_ILLEGAL_SYNTAX", null);
            }
            appendOp(4, 48);
            OpMap opMap6 = this.m_ops;
            opMap6.setOp(opMap6.getOp(1) - 2, 4);
            OpMap opMap7 = this.m_ops;
            opMap7.setOp(opMap7.getOp(1) - 1, OpCodes.NODETYPE_NODE);
        } else if (tokenIs(Constants.ATTRVAL_PARENT)) {
            nextToken();
            appendOp(4, 45);
            OpMap opMap8 = this.m_ops;
            opMap8.setOp(opMap8.getOp(1) - 2, 4);
            OpMap opMap9 = this.m_ops;
            opMap9.setOp(opMap9.getOp(1) - 1, OpCodes.NODETYPE_NODE);
        } else if (tokenIs('*') || tokenIs('@') || tokenIs('_') || ((str = this.m_token) != null && Character.isLetter(str.charAt(0)))) {
            Basis();
            while (tokenIs('[')) {
                Predicate();
            }
            OpMap opMap10 = this.m_ops;
            opMap10.setOp(op + 1, opMap10.getOp(1) - op);
        } else {
            if (z) {
                error("ER_EXPECTED_LOC_STEP", null);
            }
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void Basis() throws TransformerException {
        int i;
        int op = this.m_ops.getOp(1);
        if (lookahead("::", 1)) {
            i = AxisName();
            nextToken();
            nextToken();
        } else if (tokenIs('@')) {
            i = 39;
            appendOp(2, 39);
            nextToken();
        } else {
            i = 40;
            appendOp(2, 40);
        }
        OpMap opMap = this.m_ops;
        opMap.setOp(1, opMap.getOp(1) + 1);
        NodeTest(i);
        OpMap opMap2 = this.m_ops;
        opMap2.setOp(op + 1 + 1, opMap2.getOp(1) - op);
    }

    /* access modifiers changed from: protected */
    public int AxisName() throws TransformerException {
        Integer axisName = Keywords.getAxisName(this.m_token);
        if (axisName == null) {
            error("ER_ILLEGAL_AXIS_NAME", new Object[]{this.m_token});
        }
        int intValue = axisName.intValue();
        appendOp(2, intValue);
        return intValue;
    }

    /* access modifiers changed from: protected */
    public void NodeTest(int i) throws TransformerException {
        if (lookahead('(', 1)) {
            Integer nodeType = Keywords.getNodeType(this.m_token);
            if (nodeType == null) {
                error("ER_UNKNOWN_NODETYPE", new Object[]{this.m_token});
                return;
            }
            nextToken();
            int intValue = nodeType.intValue();
            OpMap opMap = this.m_ops;
            opMap.setOp(opMap.getOp(1), intValue);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(1, opMap2.getOp(1) + 1);
            consumeExpected('(');
            if (1032 == intValue && !tokenIs(')')) {
                Literal();
            }
            consumeExpected(')');
            return;
        }
        OpMap opMap3 = this.m_ops;
        opMap3.setOp(opMap3.getOp(1), 34);
        OpMap opMap4 = this.m_ops;
        opMap4.setOp(1, opMap4.getOp(1) + 1);
        if (lookahead(':', 1)) {
            if (tokenIs('*')) {
                OpMap opMap5 = this.m_ops;
                opMap5.setOp(opMap5.getOp(1), -3);
            } else {
                OpMap opMap6 = this.m_ops;
                opMap6.setOp(opMap6.getOp(1), this.m_queueMark - 1);
                if (!Character.isLetter(this.m_tokenChar) && !tokenIs('_')) {
                    error("ER_EXPECTED_NODE_TEST", null);
                }
            }
            nextToken();
            consumeExpected(':');
        } else {
            OpMap opMap7 = this.m_ops;
            opMap7.setOp(opMap7.getOp(1), -2);
        }
        OpMap opMap8 = this.m_ops;
        opMap8.setOp(1, opMap8.getOp(1) + 1);
        if (tokenIs('*')) {
            OpMap opMap9 = this.m_ops;
            opMap9.setOp(opMap9.getOp(1), -3);
        } else {
            OpMap opMap10 = this.m_ops;
            opMap10.setOp(opMap10.getOp(1), this.m_queueMark - 1);
            if (!Character.isLetter(this.m_tokenChar) && !tokenIs('_')) {
                error("ER_EXPECTED_NODE_TEST", null);
            }
        }
        OpMap opMap11 = this.m_ops;
        opMap11.setOp(1, opMap11.getOp(1) + 1);
        nextToken();
    }

    /* access modifiers changed from: protected */
    public void Predicate() throws TransformerException {
        if (tokenIs('[')) {
            this.countPredicate++;
            nextToken();
            PredicateExpr();
            this.countPredicate--;
            consumeExpected(']');
        }
    }

    /* access modifiers changed from: protected */
    public void PredicateExpr() throws TransformerException {
        int op = this.m_ops.getOp(1);
        appendOp(2, 29);
        Expr();
        OpMap opMap = this.m_ops;
        opMap.setOp(opMap.getOp(1), -1);
        OpMap opMap2 = this.m_ops;
        opMap2.setOp(1, opMap2.getOp(1) + 1);
        OpMap opMap3 = this.m_ops;
        opMap3.setOp(op + 1, opMap3.getOp(1) - op);
    }

    /* access modifiers changed from: protected */
    public void QName() throws TransformerException {
        if (lookahead(':', 1)) {
            OpMap opMap = this.m_ops;
            opMap.setOp(opMap.getOp(1), this.m_queueMark - 1);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(1, opMap2.getOp(1) + 1);
            nextToken();
            consumeExpected(':');
        } else {
            OpMap opMap3 = this.m_ops;
            opMap3.setOp(opMap3.getOp(1), -2);
            OpMap opMap4 = this.m_ops;
            opMap4.setOp(1, opMap4.getOp(1) + 1);
        }
        OpMap opMap5 = this.m_ops;
        opMap5.setOp(opMap5.getOp(1), this.m_queueMark - 1);
        OpMap opMap6 = this.m_ops;
        opMap6.setOp(1, opMap6.getOp(1) + 1);
        nextToken();
    }

    /* access modifiers changed from: protected */
    public void NCName() {
        OpMap opMap = this.m_ops;
        opMap.setOp(opMap.getOp(1), this.m_queueMark - 1);
        OpMap opMap2 = this.m_ops;
        opMap2.setOp(1, opMap2.getOp(1) + 1);
        nextToken();
    }

    /* access modifiers changed from: protected */
    public void Literal() throws TransformerException {
        int length = this.m_token.length() - 1;
        char c = this.m_tokenChar;
        char charAt = this.m_token.charAt(length);
        if ((c == '\"' && charAt == '\"') || (c == '\'' && charAt == '\'')) {
            int i = this.m_queueMark - 1;
            this.m_ops.m_tokenQueue.setElementAt(null, i);
            this.m_ops.m_tokenQueue.setElementAt(new XString(this.m_token.substring(1, length)), i);
            OpMap opMap = this.m_ops;
            opMap.setOp(opMap.getOp(1), i);
            OpMap opMap2 = this.m_ops;
            opMap2.setOp(1, opMap2.getOp(1) + 1);
            nextToken();
            return;
        }
        error("ER_PATTERN_LITERAL_NEEDS_BE_QUOTED", new Object[]{this.m_token});
    }

    /* access modifiers changed from: protected */
    public void Number() throws TransformerException {
        double d;
        String str = this.m_token;
        if (str != null) {
            try {
                if (str.indexOf(101) > -1 || this.m_token.indexOf(69) > -1) {
                    throw new NumberFormatException();
                }
                d = Double.valueOf(this.m_token).doubleValue();
                this.m_ops.m_tokenQueue.setElementAt(new XNumber(d), this.m_queueMark - 1);
                OpMap opMap = this.m_ops;
                opMap.setOp(opMap.getOp(1), this.m_queueMark - 1);
                OpMap opMap2 = this.m_ops;
                opMap2.setOp(1, opMap2.getOp(1) + 1);
                nextToken();
            } catch (NumberFormatException unused) {
                d = XPath.MATCH_SCORE_QNAME;
                error("ER_COULDNOT_BE_FORMATTED_TO_NUMBER", new Object[]{this.m_token});
            }
        }
    }

    /* access modifiers changed from: protected */
    public void Pattern() throws TransformerException {
        while (true) {
            LocationPathPattern();
            if (tokenIs('|')) {
                nextToken();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0098  */
    public void LocationPathPattern() throws TransformerException {
        boolean z;
        int op = this.m_ops.getOp(1);
        appendOp(2, 31);
        if (lookahead('(', 1) && (tokenIs("id") || tokenIs("key"))) {
            IdKeyPattern();
            if (tokenIs('/')) {
                nextToken();
                if (tokenIs('/')) {
                    appendOp(4, 52);
                    nextToken();
                } else {
                    appendOp(4, 53);
                }
                OpMap opMap = this.m_ops;
                opMap.setOp(opMap.getOp(1) - 2, 4);
                OpMap opMap2 = this.m_ops;
                opMap2.setOp(opMap2.getOp(1) - 1, OpCodes.NODETYPE_FUNCTEST);
            } else {
                z = false;
                if (z) {
                }
                OpMap opMap3 = this.m_ops;
                opMap3.setOp(opMap3.getOp(1), -1);
                OpMap opMap4 = this.m_ops;
                opMap4.setOp(1, opMap4.getOp(1) + 1);
                OpMap opMap5 = this.m_ops;
                opMap5.setOp(op + 1, opMap5.getOp(1) - op);
            }
        } else if (tokenIs('/')) {
            if (lookahead('/', 1)) {
                appendOp(4, 52);
                nextToken();
                z = true;
            } else {
                appendOp(4, 50);
                z = true;
            }
            OpMap opMap6 = this.m_ops;
            opMap6.setOp(opMap6.getOp(1) - 2, 4);
            OpMap opMap7 = this.m_ops;
            opMap7.setOp(opMap7.getOp(1) - 1, 35);
            nextToken();
            if (z) {
                if (!tokenIs('|') && this.m_token != null) {
                    RelativePathPattern();
                } else if (z) {
                    error("ER_EXPECTED_REL_PATH_PATTERN", null);
                }
            }
            OpMap opMap32 = this.m_ops;
            opMap32.setOp(opMap32.getOp(1), -1);
            OpMap opMap42 = this.m_ops;
            opMap42.setOp(1, opMap42.getOp(1) + 1);
            OpMap opMap52 = this.m_ops;
            opMap52.setOp(op + 1, opMap52.getOp(1) - op);
        }
        z = true;
        if (z) {
        }
        OpMap opMap322 = this.m_ops;
        opMap322.setOp(opMap322.getOp(1), -1);
        OpMap opMap422 = this.m_ops;
        opMap422.setOp(1, opMap422.getOp(1) + 1);
        OpMap opMap522 = this.m_ops;
        opMap522.setOp(op + 1, opMap522.getOp(1) - op);
    }

    /* access modifiers changed from: protected */
    public void IdKeyPattern() throws TransformerException {
        FunctionCall();
    }

    /* access modifiers changed from: protected */
    public void RelativePathPattern() throws TransformerException {
        boolean StepPattern = StepPattern(false);
        while (tokenIs('/')) {
            nextToken();
            StepPattern = StepPattern(!StepPattern);
        }
    }

    /* access modifiers changed from: protected */
    public boolean StepPattern(boolean z) throws TransformerException {
        return AbbreviatedNodeTestStep(z);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00a1 A[LOOP:0: B:21:0x0099->B:23:0x00a1, LOOP_END] */
    public boolean AbbreviatedNodeTestStep(boolean z) throws TransformerException {
        int i;
        int op = this.m_ops.getOp(1);
        boolean z2 = false;
        int i2 = 53;
        if (tokenIs('@')) {
            appendOp(2, 51);
            nextToken();
            i2 = 51;
        } else {
            if (lookahead("::", 1)) {
                if (tokenIs("attribute")) {
                    appendOp(2, 51);
                    i2 = 51;
                    i = -1;
                } else if (tokenIs("child")) {
                    i = this.m_ops.getOp(1);
                    appendOp(2, 53);
                } else {
                    error("ER_AXES_NOT_ALLOWED", new Object[]{this.m_token});
                    i = -1;
                    i2 = -1;
                }
                nextToken();
                nextToken();
            } else if (tokenIs('/')) {
                if (!z) {
                    error("ER_EXPECTED_STEP_PATTERN", null);
                }
                appendOp(2, 52);
                nextToken();
                i2 = 52;
            } else {
                i = this.m_ops.getOp(1);
                appendOp(2, 53);
            }
            OpMap opMap = this.m_ops;
            opMap.setOp(1, opMap.getOp(1) + 1);
            NodeTest(i2);
            OpMap opMap2 = this.m_ops;
            int i3 = op + 1;
            opMap2.setOp(i3 + 1, opMap2.getOp(1) - op);
            while (tokenIs('[')) {
                Predicate();
            }
            if (i > -1 && tokenIs('/') && lookahead('/', 1)) {
                this.m_ops.setOp(i, 52);
                nextToken();
                z2 = true;
            }
            OpMap opMap3 = this.m_ops;
            opMap3.setOp(i3, opMap3.getOp(1) - op);
            return z2;
        }
        i = -1;
        OpMap opMap4 = this.m_ops;
        opMap4.setOp(1, opMap4.getOp(1) + 1);
        NodeTest(i2);
        OpMap opMap22 = this.m_ops;
        int i32 = op + 1;
        opMap22.setOp(i32 + 1, opMap22.getOp(1) - op);
        while (tokenIs('[')) {
        }
        this.m_ops.setOp(i, 52);
        nextToken();
        z2 = true;
        OpMap opMap32 = this.m_ops;
        opMap32.setOp(i32, opMap32.getOp(1) - op);
        return z2;
    }
}

package org.apache.xpath.compiler;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Constants;
import org.apache.xml.utils.ObjectVector;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPathProcessorException;
import org.apache.xpath.domapi.XPathStylesheetDOM3Exception;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class XPathParser {
    public static final String CONTINUE_AFTER_FATAL_ERROR = "CONTINUE_AFTER_FATAL_ERROR";
    protected static final int FILTER_MATCH_FAILED = 0;
    protected static final int FILTER_MATCH_PREDICATES = 2;
    protected static final int FILTER_MATCH_PRIMARY = 1;
    private ErrorListener m_errorListener;
    private FunctionTable m_functionTable;
    PrefixResolver m_namespaceContext;
    private OpMap m_ops;
    int m_queueMark;
    SourceLocator m_sourceLocator;
    transient String m_token;
    transient char m_tokenChar;

    public XPathParser(ErrorListener errorListener, SourceLocator sourceLocator) {
        this.m_tokenChar = '\u0000';
        this.m_queueMark = FILTER_MATCH_FAILED;
        this.m_errorListener = errorListener;
        this.m_sourceLocator = sourceLocator;
    }

    public void initXPath(Compiler compiler, String expression, PrefixResolver namespaceContext) throws TransformerException {
        this.m_ops = compiler;
        this.m_namespaceContext = namespaceContext;
        this.m_functionTable = compiler.getFunctionTable();
        new Lexer(compiler, namespaceContext, this).tokenize(expression);
        this.m_ops.setOp(FILTER_MATCH_FAILED, FILTER_MATCH_PRIMARY);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, FILTER_MATCH_PREDICATES);
        try {
            nextToken();
            Expr();
            if (this.m_token != null) {
                String extraTokens = SerializerConstants.EMPTYSTRING;
                while (this.m_token != null) {
                    extraTokens = extraTokens + "'" + this.m_token + "'";
                    nextToken();
                    if (this.m_token != null) {
                        extraTokens = extraTokens + ", ";
                    }
                }
                String str = XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS;
                Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
                objArr[FILTER_MATCH_FAILED] = extraTokens;
                error(str, objArr);
            }
        } catch (XPathProcessorException e) {
            if (CONTINUE_AFTER_FATAL_ERROR.equals(e.getMessage())) {
                initXPath(compiler, "/..", namespaceContext);
            } else {
                throw e;
            }
        }
        compiler.shrink();
    }

    public void initMatchPattern(Compiler compiler, String expression, PrefixResolver namespaceContext) throws TransformerException {
        this.m_ops = compiler;
        this.m_namespaceContext = namespaceContext;
        this.m_functionTable = compiler.getFunctionTable();
        new Lexer(compiler, namespaceContext, this).tokenize(expression);
        this.m_ops.setOp(FILTER_MATCH_FAILED, 30);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, FILTER_MATCH_PREDICATES);
        nextToken();
        Pattern();
        if (this.m_token != null) {
            String extraTokens = SerializerConstants.EMPTYSTRING;
            while (this.m_token != null) {
                extraTokens = extraTokens + "'" + this.m_token + "'";
                nextToken();
                if (this.m_token != null) {
                    extraTokens = extraTokens + ", ";
                }
            }
            String str = XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS;
            Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
            objArr[FILTER_MATCH_FAILED] = extraTokens;
            error(str, objArr);
        }
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -1);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        this.m_ops.shrink();
    }

    public void setErrorHandler(ErrorListener handler) {
        this.m_errorListener = handler;
    }

    public ErrorListener getErrorListener() {
        return this.m_errorListener;
    }

    final boolean tokenIs(String s) {
        if (this.m_token != null) {
            return this.m_token.equals(s);
        }
        return s == null;
    }

    final boolean tokenIs(char c) {
        return this.m_token != null && this.m_tokenChar == c;
    }

    final boolean lookahead(char c, int n) {
        int pos = this.m_queueMark + n;
        if (pos > this.m_ops.getTokenQueueSize() || pos <= 0 || this.m_ops.getTokenQueueSize() == 0) {
            return false;
        }
        String tok = (String) this.m_ops.m_tokenQueue.elementAt(pos - 1);
        return tok.length() == FILTER_MATCH_PRIMARY && tok.charAt(FILTER_MATCH_FAILED) == c;
    }

    private final boolean lookbehind(char c, int n) {
        int lookBehindPos = this.m_queueMark - (n + FILTER_MATCH_PRIMARY);
        if (lookBehindPos < 0) {
            return false;
        }
        String lookbehind = (String) this.m_ops.m_tokenQueue.elementAt(lookBehindPos);
        if (lookbehind.length() != FILTER_MATCH_PRIMARY) {
            return false;
        }
        char c0 = lookbehind == null ? '|' : lookbehind.charAt(FILTER_MATCH_FAILED);
        if (c0 != '|' && c0 == c) {
            return true;
        }
        return false;
    }

    private final boolean lookbehindHasToken(int n) {
        if (this.m_queueMark - n <= 0) {
            return false;
        }
        String lookbehind = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark - (n - 1));
        return (lookbehind == null ? '|' : lookbehind.charAt(FILTER_MATCH_FAILED)) != '|';
    }

    private final boolean lookahead(String s, int n) {
        if (this.m_queueMark + n > this.m_ops.getTokenQueueSize()) {
            return s == null;
        } else {
            String lookahead = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark + (n - 1));
            if (lookahead != null) {
                return lookahead.equals(s);
            }
            return s == null;
        }
    }

    private final void nextToken() {
        if (this.m_queueMark < this.m_ops.getTokenQueueSize()) {
            ObjectVector objectVector = this.m_ops.m_tokenQueue;
            int i = this.m_queueMark;
            this.m_queueMark = i + FILTER_MATCH_PRIMARY;
            this.m_token = (String) objectVector.elementAt(i);
            this.m_tokenChar = this.m_token.charAt(FILTER_MATCH_FAILED);
            return;
        }
        this.m_token = null;
        this.m_tokenChar = '\u0000';
    }

    private final String getTokenRelative(int i) {
        int relative = this.m_queueMark + i;
        if (relative <= 0 || relative >= this.m_ops.getTokenQueueSize()) {
            return null;
        }
        return (String) this.m_ops.m_tokenQueue.elementAt(relative);
    }

    private final void prevToken() {
        if (this.m_queueMark > 0) {
            this.m_queueMark--;
            this.m_token = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark);
            this.m_tokenChar = this.m_token.charAt(FILTER_MATCH_FAILED);
            return;
        }
        this.m_token = null;
        this.m_tokenChar = '\u0000';
    }

    private final void consumeExpected(String expected) throws TransformerException {
        if (tokenIs(expected)) {
            nextToken();
            return;
        }
        String str = XPATHErrorResources.ER_EXPECTED_BUT_FOUND;
        Object[] objArr = new Object[FILTER_MATCH_PREDICATES];
        objArr[FILTER_MATCH_FAILED] = expected;
        objArr[FILTER_MATCH_PRIMARY] = this.m_token;
        error(str, objArr);
        throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
    }

    private final void consumeExpected(char expected) throws TransformerException {
        if (tokenIs(expected)) {
            nextToken();
            return;
        }
        String str = XPATHErrorResources.ER_EXPECTED_BUT_FOUND;
        Object[] objArr = new Object[FILTER_MATCH_PREDICATES];
        objArr[FILTER_MATCH_FAILED] = String.valueOf(expected);
        objArr[FILTER_MATCH_PRIMARY] = this.m_token;
        error(str, objArr);
        throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
    }

    void warn(String msg, Object[] args) throws TransformerException {
        String fmsg = XPATHMessages.createXPATHWarning(msg, args);
        ErrorListener ehandler = getErrorListener();
        if (ehandler != null) {
            ehandler.warning(new TransformerException(fmsg, this.m_sourceLocator));
        } else {
            System.err.println(fmsg);
        }
    }

    private void assertion(boolean b, String msg) {
        if (!b) {
            String str = XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION;
            Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
            objArr[FILTER_MATCH_FAILED] = msg;
            throw new RuntimeException(XPATHMessages.createXPATHMessage(str, objArr));
        }
    }

    void error(String msg, Object[] args) throws TransformerException {
        String fmsg = XPATHMessages.createXPATHMessage(msg, args);
        ErrorListener ehandler = getErrorListener();
        TransformerException te = new TransformerException(fmsg, this.m_sourceLocator);
        if (ehandler != null) {
            ehandler.fatalError(te);
            return;
        }
        throw te;
    }

    void errorForDOM3(String msg, Object[] args) throws TransformerException {
        String fmsg = XPATHMessages.createXPATHMessage(msg, args);
        ErrorListener ehandler = getErrorListener();
        TransformerException te = new XPathStylesheetDOM3Exception(fmsg, this.m_sourceLocator);
        if (ehandler != null) {
            ehandler.fatalError(te);
            return;
        }
        throw te;
    }

    protected String dumpRemainingTokenQueue() {
        int q = this.m_queueMark;
        if (q >= this.m_ops.getTokenQueueSize()) {
            return SerializerConstants.EMPTYSTRING;
        }
        String msg = "\n Remaining tokens: (";
        while (q < this.m_ops.getTokenQueueSize()) {
            int q2 = q + FILTER_MATCH_PRIMARY;
            msg = msg + " '" + ((String) this.m_ops.m_tokenQueue.elementAt(q)) + "'";
            q = q2;
        }
        return msg + ")";
    }

    final int getFunctionToken(String key) {
        try {
            Object id = Keywords.lookupNodeTest(key);
            if (id == null) {
                id = this.m_functionTable.getFunctionID(key);
            }
            return ((Integer) id).intValue();
        } catch (NullPointerException e) {
            return -1;
        } catch (ClassCastException e2) {
            return -1;
        }
    }

    void insertOp(int pos, int length, int op) {
        int totalLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        for (int i = totalLen - 1; i >= pos; i--) {
            this.m_ops.setOp(i + length, this.m_ops.getOp(i));
        }
        this.m_ops.setOp(pos, op);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, totalLen + length);
    }

    void appendOp(int length, int op) {
        int totalLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        this.m_ops.setOp(totalLen, op);
        this.m_ops.setOp(totalLen + FILTER_MATCH_PRIMARY, length);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, totalLen + length);
    }

    protected void Expr() throws TransformerException {
        OrExpr();
    }

    protected void OrExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        AndExpr();
        if (this.m_token != null && tokenIs("or")) {
            nextToken();
            insertOp(opPos, FILTER_MATCH_PREDICATES, FILTER_MATCH_PREDICATES);
            OrExpr();
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
        }
    }

    protected void AndExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        EqualityExpr(-1);
        if (this.m_token != null && tokenIs("and")) {
            nextToken();
            insertOp(opPos, FILTER_MATCH_PREDICATES, 3);
            AndExpr();
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
        }
    }

    protected int EqualityExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (-1 == addPos) {
            addPos = opPos;
        }
        RelationalExpr(-1);
        if (this.m_token == null) {
            return addPos;
        }
        int opPlusLeftHandLen;
        if (tokenIs('!') && lookahead('=', (int) FILTER_MATCH_PRIMARY)) {
            nextToken();
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 4);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = EqualityExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        } else if (!tokenIs('=')) {
            return addPos;
        } else {
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 5);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = EqualityExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        }
    }

    protected int RelationalExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (-1 == addPos) {
            addPos = opPos;
        }
        AdditiveExpr(-1);
        if (this.m_token == null) {
            return addPos;
        }
        int opPlusLeftHandLen;
        if (tokenIs('<')) {
            nextToken();
            if (tokenIs('=')) {
                nextToken();
                insertOp(addPos, FILTER_MATCH_PREDICATES, 6);
            } else {
                insertOp(addPos, FILTER_MATCH_PREDICATES, 7);
            }
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = RelationalExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        } else if (!tokenIs('>')) {
            return addPos;
        } else {
            nextToken();
            if (tokenIs('=')) {
                nextToken();
                insertOp(addPos, FILTER_MATCH_PREDICATES, 8);
            } else {
                insertOp(addPos, FILTER_MATCH_PREDICATES, 9);
            }
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = RelationalExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        }
    }

    protected int AdditiveExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (-1 == addPos) {
            addPos = opPos;
        }
        MultiplicativeExpr(-1);
        if (this.m_token == null) {
            return addPos;
        }
        int opPlusLeftHandLen;
        if (tokenIs('+')) {
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 10);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = AdditiveExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        } else if (!tokenIs('-')) {
            return addPos;
        } else {
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 11);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = AdditiveExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        }
    }

    protected int MultiplicativeExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (-1 == addPos) {
            addPos = opPos;
        }
        UnaryExpr();
        if (this.m_token == null) {
            return addPos;
        }
        int opPlusLeftHandLen;
        if (tokenIs('*')) {
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 12);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        } else if (tokenIs("div")) {
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 13);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        } else if (tokenIs("mod")) {
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 14);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        } else if (!tokenIs("quo")) {
            return addPos;
        } else {
            nextToken();
            insertOp(addPos, FILTER_MATCH_PREDICATES, 15);
            opPlusLeftHandLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - addPos;
            addPos = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp((addPos + opPlusLeftHandLen) + FILTER_MATCH_PRIMARY) + opPlusLeftHandLen);
            return addPos + FILTER_MATCH_PREDICATES;
        }
    }

    protected void UnaryExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        boolean isNeg = false;
        if (this.m_tokenChar == '-') {
            nextToken();
            appendOp(FILTER_MATCH_PREDICATES, 16);
            isNeg = true;
        }
        UnionExpr();
        if (isNeg) {
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
        }
    }

    protected void StringExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        appendOp(FILTER_MATCH_PREDICATES, 17);
        Expr();
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected void BooleanExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        appendOp(FILTER_MATCH_PREDICATES, 18);
        Expr();
        int opLen = this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos;
        if (opLen == FILTER_MATCH_PREDICATES) {
            error(XPATHErrorResources.ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL, null);
        }
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, opLen);
    }

    protected void NumberExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        appendOp(FILTER_MATCH_PREDICATES, 19);
        Expr();
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected void UnionExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        boolean foundUnion = false;
        do {
            PathExpr();
            if (!tokenIs('|')) {
                break;
            }
            if (!foundUnion) {
                foundUnion = true;
                insertOp(opPos, FILTER_MATCH_PREDICATES, 20);
            }
            nextToken();
        } while (true);
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected void PathExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        int filterExprMatch = FilterExpr();
        if (filterExprMatch != 0) {
            boolean locationPathStarted = filterExprMatch == FILTER_MATCH_PREDICATES;
            if (tokenIs('/')) {
                nextToken();
                if (!locationPathStarted) {
                    insertOp(opPos, FILTER_MATCH_PREDICATES, 28);
                    locationPathStarted = true;
                }
                if (!RelativeLocationPath()) {
                    error(XPATHErrorResources.ER_EXPECTED_REL_LOC_PATH, null);
                }
            }
            if (locationPathStarted) {
                this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -1);
                this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
                this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
                return;
            }
            return;
        }
        LocationPath();
    }

    protected int FilterExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (!PrimaryExpr()) {
            return FILTER_MATCH_FAILED;
        }
        if (!tokenIs('[')) {
            return FILTER_MATCH_PRIMARY;
        }
        insertOp(opPos, FILTER_MATCH_PREDICATES, 28);
        while (tokenIs('[')) {
            Predicate();
        }
        return FILTER_MATCH_PREDICATES;
    }

    protected boolean PrimaryExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (this.m_tokenChar == '\'' || this.m_tokenChar == '\"') {
            appendOp(FILTER_MATCH_PREDICATES, 21);
            Literal();
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
            return true;
        } else if (this.m_tokenChar == '$') {
            nextToken();
            appendOp(FILTER_MATCH_PREDICATES, 22);
            QName();
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
            return true;
        } else if (this.m_tokenChar == '(') {
            nextToken();
            appendOp(FILTER_MATCH_PREDICATES, 23);
            Expr();
            consumeExpected(')');
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
            return true;
        } else if (this.m_token != null && (('.' == this.m_tokenChar && this.m_token.length() > FILTER_MATCH_PRIMARY && Character.isDigit(this.m_token.charAt(FILTER_MATCH_PRIMARY))) || Character.isDigit(this.m_tokenChar))) {
            appendOp(FILTER_MATCH_PREDICATES, 27);
            Number();
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
            return true;
        } else if (lookahead('(', (int) FILTER_MATCH_PRIMARY) || (lookahead(':', (int) FILTER_MATCH_PRIMARY) && lookahead('(', 3))) {
            return FunctionCall();
        } else {
            return false;
        }
    }

    protected void Argument() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        appendOp(FILTER_MATCH_PREDICATES, 26);
        Expr();
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected boolean FunctionCall() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (!lookahead(':', (int) FILTER_MATCH_PRIMARY)) {
            int funcTok = getFunctionToken(this.m_token);
            if (-1 == funcTok) {
                String str = XPATHErrorResources.ER_COULDNOT_FIND_FUNCTION;
                Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
                objArr[FILTER_MATCH_FAILED] = this.m_token;
                error(str, objArr);
            }
            switch (funcTok) {
                case OpCodes.NODETYPE_COMMENT /*1030*/:
                case OpCodes.NODETYPE_TEXT /*1031*/:
                case OpCodes.NODETYPE_PI /*1032*/:
                case OpCodes.NODETYPE_NODE /*1033*/:
                    return false;
                default:
                    appendOp(3, 25);
                    this.m_ops.setOp((opPos + FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY, funcTok);
                    nextToken();
                    break;
            }
        }
        appendOp(4, 24);
        this.m_ops.setOp((opPos + FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY, this.m_queueMark - 1);
        nextToken();
        consumeExpected(':');
        this.m_ops.setOp((opPos + FILTER_MATCH_PRIMARY) + FILTER_MATCH_PREDICATES, this.m_queueMark - 1);
        nextToken();
        consumeExpected('(');
        while (!tokenIs(')') && this.m_token != null) {
            if (tokenIs(',')) {
                error(XPATHErrorResources.ER_FOUND_COMMA_BUT_NO_PRECEDING_ARG, null);
            }
            Argument();
            if (!tokenIs(')')) {
                consumeExpected(',');
                if (tokenIs(')')) {
                    error(XPATHErrorResources.ER_FOUND_COMMA_BUT_NO_FOLLOWING_ARG, null);
                }
            }
        }
        consumeExpected(')');
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -1);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
        return true;
    }

    protected void LocationPath() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        appendOp(FILTER_MATCH_PREDICATES, 28);
        boolean seenSlash = tokenIs('/');
        if (seenSlash) {
            appendOp(4, 50);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 1, 35);
            nextToken();
        } else if (this.m_token == null) {
            error(XPATHErrorResources.ER_EXPECTED_LOC_PATH_AT_END_EXPR, null);
        }
        if (!(this.m_token == null || RelativeLocationPath() || seenSlash)) {
            String str = XPATHErrorResources.ER_EXPECTED_LOC_PATH;
            Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
            objArr[FILTER_MATCH_FAILED] = this.m_token;
            error(str, objArr);
        }
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -1);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected boolean RelativeLocationPath() throws TransformerException {
        if (!Step()) {
            return false;
        }
        while (tokenIs('/')) {
            nextToken();
            if (!Step()) {
                error(XPATHErrorResources.ER_EXPECTED_LOC_STEP, null);
            }
        }
        return true;
    }

    protected boolean Step() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        boolean doubleSlash = tokenIs('/');
        if (doubleSlash) {
            nextToken();
            appendOp(FILTER_MATCH_PREDICATES, 42);
            this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), OpCodes.NODETYPE_NODE);
            this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
            this.m_ops.setOp((opPos + FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
            opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        }
        if (tokenIs(Constants.ATTRVAL_THIS)) {
            nextToken();
            if (tokenIs('[')) {
                error(XPATHErrorResources.ER_PREDICATE_ILLEGAL_SYNTAX, null);
            }
            appendOp(4, 48);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 1, OpCodes.NODETYPE_NODE);
        } else if (tokenIs(Constants.ATTRVAL_PARENT)) {
            nextToken();
            appendOp(4, 45);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 1, OpCodes.NODETYPE_NODE);
        } else if (tokenIs('*') || tokenIs('@') || tokenIs('_') || (this.m_token != null && Character.isLetter(this.m_token.charAt(FILTER_MATCH_FAILED)))) {
            Basis();
            while (tokenIs('[')) {
                Predicate();
            }
            this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
        } else {
            if (doubleSlash) {
                error(XPATHErrorResources.ER_EXPECTED_LOC_STEP, null);
            }
            return false;
        }
        return true;
    }

    protected void Basis() throws TransformerException {
        int axesType;
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        if (lookahead("::", (int) FILTER_MATCH_PRIMARY)) {
            axesType = AxisName();
            nextToken();
            nextToken();
        } else if (tokenIs('@')) {
            axesType = 39;
            appendOp(FILTER_MATCH_PREDICATES, 39);
            nextToken();
        } else {
            axesType = 40;
            appendOp(FILTER_MATCH_PREDICATES, 40);
        }
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        NodeTest(axesType);
        this.m_ops.setOp((opPos + FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected int AxisName() throws TransformerException {
        Object val = Keywords.getAxisName(this.m_token);
        if (val == null) {
            String str = XPATHErrorResources.ER_ILLEGAL_AXIS_NAME;
            Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
            objArr[FILTER_MATCH_FAILED] = this.m_token;
            error(str, objArr);
        }
        int axesType = ((Integer) val).intValue();
        appendOp(FILTER_MATCH_PREDICATES, axesType);
        return axesType;
    }

    protected void NodeTest(int axesType) throws TransformerException {
        if (lookahead('(', (int) FILTER_MATCH_PRIMARY)) {
            Object nodeTestOp = Keywords.getNodeType(this.m_token);
            if (nodeTestOp == null) {
                String str = XPATHErrorResources.ER_UNKNOWN_NODETYPE;
                Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
                objArr[FILTER_MATCH_FAILED] = this.m_token;
                error(str, objArr);
                return;
            }
            nextToken();
            int nt = ((Integer) nodeTestOp).intValue();
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), nt);
            this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
            consumeExpected('(');
            if (OpCodes.NODETYPE_PI == nt && !tokenIs(')')) {
                Literal();
            }
            consumeExpected(')');
            return;
        }
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), 34);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        if (lookahead(':', (int) FILTER_MATCH_PRIMARY)) {
            if (tokenIs('*')) {
                this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -3);
            } else {
                this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), this.m_queueMark - 1);
                if (!(Character.isLetter(this.m_tokenChar) || tokenIs('_'))) {
                    error(XPATHErrorResources.ER_EXPECTED_NODE_TEST, null);
                }
            }
            nextToken();
            consumeExpected(':');
        } else {
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -2);
        }
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        if (tokenIs('*')) {
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -3);
        } else {
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), this.m_queueMark - 1);
            if (!(Character.isLetter(this.m_tokenChar) || tokenIs('_'))) {
                error(XPATHErrorResources.ER_EXPECTED_NODE_TEST, null);
            }
        }
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        nextToken();
    }

    protected void Predicate() throws TransformerException {
        if (tokenIs('[')) {
            nextToken();
            PredicateExpr();
            consumeExpected(']');
        }
    }

    protected void PredicateExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        appendOp(FILTER_MATCH_PREDICATES, 29);
        Expr();
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -1);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected void QName() throws TransformerException {
        if (lookahead(':', (int) FILTER_MATCH_PRIMARY)) {
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), this.m_queueMark - 1);
            this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
            nextToken();
            consumeExpected(':');
        } else {
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -2);
            this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        }
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), this.m_queueMark - 1);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        nextToken();
    }

    protected void NCName() {
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), this.m_queueMark - 1);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        nextToken();
    }

    protected void Literal() throws TransformerException {
        int last = this.m_token.length() - 1;
        char c0 = this.m_tokenChar;
        char cX = this.m_token.charAt(last);
        if ((c0 == '\"' && cX == '\"') || (c0 == '\'' && cX == '\'')) {
            int tokenQueuePos = this.m_queueMark - 1;
            this.m_ops.m_tokenQueue.setElementAt(null, tokenQueuePos);
            this.m_ops.m_tokenQueue.setElementAt(new XString(this.m_token.substring(FILTER_MATCH_PRIMARY, last)), tokenQueuePos);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), tokenQueuePos);
            this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
            nextToken();
            return;
        }
        String str = XPATHErrorResources.ER_PATTERN_LITERAL_NEEDS_BE_QUOTED;
        Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
        objArr[FILTER_MATCH_FAILED] = this.m_token;
        error(str, objArr);
    }

    protected void Number() throws TransformerException {
        if (this.m_token != null) {
            double num;
            try {
                if (this.m_token.indexOf(101) > -1 || this.m_token.indexOf(69) > -1) {
                    throw new NumberFormatException();
                }
                num = Double.valueOf(this.m_token).doubleValue();
                this.m_ops.m_tokenQueue.setElementAt(new XNumber(num), this.m_queueMark - 1);
                this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), this.m_queueMark - 1);
                this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
                nextToken();
            } catch (NumberFormatException e) {
                num = 0.0d;
                String str = XPATHErrorResources.ER_COULDNOT_BE_FORMATTED_TO_NUMBER;
                Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
                objArr[FILTER_MATCH_FAILED] = this.m_token;
                error(str, objArr);
            }
        }
    }

    protected void Pattern() throws TransformerException {
        while (true) {
            LocationPathPattern();
            if (tokenIs('|')) {
                nextToken();
            } else {
                return;
            }
        }
    }

    protected void LocationPathPattern() throws TransformerException {
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        int relativePathStatus = FILTER_MATCH_FAILED;
        appendOp(FILTER_MATCH_PREDICATES, 31);
        if (lookahead('(', (int) FILTER_MATCH_PRIMARY) && (tokenIs(Constants.ATTRVAL_ID) || tokenIs(Keywords.FUNC_KEY_STRING))) {
            IdKeyPattern();
            if (tokenIs('/')) {
                nextToken();
                if (tokenIs('/')) {
                    appendOp(4, 52);
                    nextToken();
                } else {
                    appendOp(4, 53);
                }
                this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 2, 4);
                this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 1, OpCodes.NODETYPE_FUNCTEST);
                relativePathStatus = FILTER_MATCH_PREDICATES;
            }
        } else if (tokenIs('/')) {
            if (lookahead('/', (int) FILTER_MATCH_PRIMARY)) {
                appendOp(4, 52);
                nextToken();
                relativePathStatus = FILTER_MATCH_PREDICATES;
            } else {
                appendOp(4, 50);
                relativePathStatus = FILTER_MATCH_PRIMARY;
            }
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY) - 1, 35);
            nextToken();
        } else {
            relativePathStatus = FILTER_MATCH_PREDICATES;
        }
        if (relativePathStatus != 0) {
            if (!tokenIs('|') && this.m_token != null) {
                RelativePathPattern();
            } else if (relativePathStatus == FILTER_MATCH_PREDICATES) {
                error(XPATHErrorResources.ER_EXPECTED_REL_PATH_PATTERN, null);
            }
        }
        this.m_ops.setOp(this.m_ops.getOp(FILTER_MATCH_PRIMARY), -1);
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
    }

    protected void IdKeyPattern() throws TransformerException {
        FunctionCall();
    }

    protected void RelativePathPattern() throws TransformerException {
        boolean trailingSlashConsumed = StepPattern(false);
        while (tokenIs('/')) {
            nextToken();
            trailingSlashConsumed = StepPattern(!trailingSlashConsumed);
        }
    }

    protected boolean StepPattern(boolean isLeadingSlashPermitted) throws TransformerException {
        return AbbreviatedNodeTestStep(isLeadingSlashPermitted);
    }

    protected boolean AbbreviatedNodeTestStep(boolean isLeadingSlashPermitted) throws TransformerException {
        int axesType;
        boolean trailingSlashConsumed;
        int opPos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
        int matchTypePos = -1;
        if (tokenIs('@')) {
            axesType = 51;
            appendOp(FILTER_MATCH_PREDICATES, 51);
            nextToken();
        } else if (lookahead("::", (int) FILTER_MATCH_PRIMARY)) {
            if (tokenIs(Constants.ELEMNAME_ATTRIBUTE_STRING)) {
                axesType = 51;
                appendOp(FILTER_MATCH_PREDICATES, 51);
            } else if (tokenIs("child")) {
                matchTypePos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
                axesType = 53;
                appendOp(FILTER_MATCH_PREDICATES, 53);
            } else {
                axesType = -1;
                String str = XPATHErrorResources.ER_AXES_NOT_ALLOWED;
                Object[] objArr = new Object[FILTER_MATCH_PRIMARY];
                objArr[FILTER_MATCH_FAILED] = this.m_token;
                error(str, objArr);
            }
            nextToken();
            nextToken();
        } else if (tokenIs('/')) {
            if (!isLeadingSlashPermitted) {
                error(XPATHErrorResources.ER_EXPECTED_STEP_PATTERN, null);
            }
            axesType = 52;
            appendOp(FILTER_MATCH_PREDICATES, 52);
            nextToken();
        } else {
            matchTypePos = this.m_ops.getOp(FILTER_MATCH_PRIMARY);
            axesType = 53;
            appendOp(FILTER_MATCH_PREDICATES, 53);
        }
        this.m_ops.setOp(FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY);
        NodeTest(axesType);
        this.m_ops.setOp((opPos + FILTER_MATCH_PRIMARY) + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
        while (tokenIs('[')) {
            Predicate();
        }
        if (matchTypePos > -1 && tokenIs('/') && lookahead('/', (int) FILTER_MATCH_PRIMARY)) {
            this.m_ops.setOp(matchTypePos, 52);
            nextToken();
            trailingSlashConsumed = true;
        } else {
            trailingSlashConsumed = false;
        }
        this.m_ops.setOp(opPos + FILTER_MATCH_PRIMARY, this.m_ops.getOp(FILTER_MATCH_PRIMARY) - opPos);
        return trailingSlashConsumed;
    }
}

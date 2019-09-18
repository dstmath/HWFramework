package org.apache.xpath.compiler;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.templates.Constants;
import org.apache.xml.utils.ObjectVector;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPathProcessorException;
import org.apache.xpath.domapi.XPathStylesheetDOM3Exception;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHErrorResources;

public class XPathParser {
    public static final String CONTINUE_AFTER_FATAL_ERROR = "CONTINUE_AFTER_FATAL_ERROR";
    protected static final int FILTER_MATCH_FAILED = 0;
    protected static final int FILTER_MATCH_PREDICATES = 2;
    protected static final int FILTER_MATCH_PRIMARY = 1;
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

    public void initXPath(Compiler compiler, String expression, PrefixResolver namespaceContext) throws TransformerException {
        this.m_ops = compiler;
        this.m_namespaceContext = namespaceContext;
        this.m_functionTable = compiler.getFunctionTable();
        new Lexer(compiler, namespaceContext, this).tokenize(expression);
        this.m_ops.setOp(0, 1);
        this.m_ops.setOp(1, 2);
        try {
            nextToken();
            Expr();
            if (this.m_token != null) {
                String extraTokens = "";
                while (this.m_token != null) {
                    extraTokens = extraTokens + "'" + this.m_token + "'";
                    nextToken();
                    if (this.m_token != null) {
                        extraTokens = extraTokens + ", ";
                    }
                }
                error(XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS, new Object[]{extraTokens});
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
        this.m_ops.setOp(0, 30);
        this.m_ops.setOp(1, 2);
        nextToken();
        Pattern();
        if (this.m_token != null) {
            String extraTokens = "";
            while (this.m_token != null) {
                extraTokens = extraTokens + "'" + this.m_token + "'";
                nextToken();
                if (this.m_token != null) {
                    extraTokens = extraTokens + ", ";
                }
            }
            error(XPATHErrorResources.ER_EXTRA_ILLEGAL_TOKENS, new Object[]{extraTokens});
        }
        this.m_ops.setOp(this.m_ops.getOp(1), -1);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        this.m_ops.shrink();
    }

    public void setErrorHandler(ErrorListener handler) {
        this.m_errorListener = handler;
    }

    public ErrorListener getErrorListener() {
        return this.m_errorListener;
    }

    /* access modifiers changed from: package-private */
    public final boolean tokenIs(String s) {
        if (this.m_token != null) {
            return this.m_token.equals(s);
        }
        return s == null;
    }

    /* access modifiers changed from: package-private */
    public final boolean tokenIs(char c) {
        return this.m_token != null && this.m_tokenChar == c;
    }

    /* access modifiers changed from: package-private */
    public final boolean lookahead(char c, int n) {
        int pos = this.m_queueMark + n;
        boolean b = false;
        if (pos <= this.m_ops.getTokenQueueSize() && pos > 0 && this.m_ops.getTokenQueueSize() != 0) {
            String tok = (String) this.m_ops.m_tokenQueue.elementAt(pos - 1);
            if (tok.length() == 1 && tok.charAt(0) == c) {
                b = true;
            }
        }
        return b;
    }

    private final boolean lookbehind(char c, int n) {
        int lookBehindPos = this.m_queueMark - (n + 1);
        if (lookBehindPos < 0) {
            return false;
        }
        String lookbehind = (String) this.m_ops.m_tokenQueue.elementAt(lookBehindPos);
        if (lookbehind.length() != 1) {
            return false;
        }
        char c0 = lookbehind == null ? '|' : lookbehind.charAt(0);
        if (c0 != '|' && c0 == c) {
            return true;
        }
        return false;
    }

    private final boolean lookbehindHasToken(int n) {
        boolean hasToken = false;
        if (this.m_queueMark - n > 0) {
            String lookbehind = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark - (n - 1));
            if ((lookbehind == null ? '|' : lookbehind.charAt(0)) != '|') {
                hasToken = true;
            }
        }
        return hasToken;
    }

    private final boolean lookahead(String s, int n) {
        boolean z = false;
        if (this.m_queueMark + n > this.m_ops.getTokenQueueSize()) {
            return s == null;
        }
        String lookahead = (String) this.m_ops.m_tokenQueue.elementAt(this.m_queueMark + (n - 1));
        if (lookahead != null) {
            z = lookahead.equals(s);
        } else if (s == null) {
            z = true;
        }
        return z;
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
            this.m_tokenChar = this.m_token.charAt(0);
            return;
        }
        this.m_token = null;
        this.m_tokenChar = 0;
    }

    private final void consumeExpected(String expected) throws TransformerException {
        if (tokenIs(expected)) {
            nextToken();
            return;
        }
        error(XPATHErrorResources.ER_EXPECTED_BUT_FOUND, new Object[]{expected, this.m_token});
        throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
    }

    private final void consumeExpected(char expected) throws TransformerException {
        if (tokenIs(expected)) {
            nextToken();
            return;
        }
        error(XPATHErrorResources.ER_EXPECTED_BUT_FOUND, new Object[]{String.valueOf(expected), this.m_token});
        throw new XPathProcessorException(CONTINUE_AFTER_FATAL_ERROR);
    }

    /* access modifiers changed from: package-private */
    public void warn(String msg, Object[] args) throws TransformerException {
        String fmsg = XSLMessages.createXPATHWarning(msg, args);
        ErrorListener ehandler = getErrorListener();
        if (ehandler != null) {
            ehandler.warning(new TransformerException(fmsg, this.m_sourceLocator));
        } else {
            System.err.println(fmsg);
        }
    }

    private void assertion(boolean b, String msg) {
        if (!b) {
            throw new RuntimeException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION, new Object[]{msg}));
        }
    }

    /* access modifiers changed from: package-private */
    public void error(String msg, Object[] args) throws TransformerException {
        String fmsg = XSLMessages.createXPATHMessage(msg, args);
        ErrorListener ehandler = getErrorListener();
        TransformerException te = new TransformerException(fmsg, this.m_sourceLocator);
        if (ehandler != null) {
            ehandler.fatalError(te);
            return;
        }
        throw te;
    }

    /* access modifiers changed from: package-private */
    public void errorForDOM3(String msg, Object[] args) throws TransformerException {
        String fmsg = XSLMessages.createXPATHMessage(msg, args);
        ErrorListener ehandler = getErrorListener();
        TransformerException te = new XPathStylesheetDOM3Exception(fmsg, this.m_sourceLocator);
        if (ehandler != null) {
            ehandler.fatalError(te);
            return;
        }
        throw te;
    }

    /* access modifiers changed from: protected */
    public String dumpRemainingTokenQueue() {
        int q = this.m_queueMark;
        if (q >= this.m_ops.getTokenQueueSize()) {
            return "";
        }
        String msg = "\n Remaining tokens: (";
        while (q < this.m_ops.getTokenQueueSize()) {
            msg = msg + " '" + ((String) this.m_ops.m_tokenQueue.elementAt(q)) + "'";
            q++;
        }
        return msg + ")";
    }

    /* access modifiers changed from: package-private */
    public final int getFunctionToken(String key) {
        int tok;
        try {
            Object id = Keywords.lookupNodeTest(key);
            if (id == null) {
                id = this.m_functionTable.getFunctionID(key);
            }
            tok = ((Integer) id).intValue();
        } catch (NullPointerException e) {
            tok = -1;
        } catch (ClassCastException e2) {
            tok = -1;
        }
        return tok;
    }

    /* access modifiers changed from: package-private */
    public void insertOp(int pos, int length, int op) {
        int totalLen = this.m_ops.getOp(1);
        for (int i = totalLen - 1; i >= pos; i--) {
            this.m_ops.setOp(i + length, this.m_ops.getOp(i));
        }
        this.m_ops.setOp(pos, op);
        this.m_ops.setOp(1, totalLen + length);
    }

    /* access modifiers changed from: package-private */
    public void appendOp(int length, int op) {
        int totalLen = this.m_ops.getOp(1);
        this.m_ops.setOp(totalLen, op);
        this.m_ops.setOp(totalLen + 1, length);
        this.m_ops.setOp(1, totalLen + length);
    }

    /* access modifiers changed from: protected */
    public void Expr() throws TransformerException {
        OrExpr();
    }

    /* access modifiers changed from: protected */
    public void OrExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        AndExpr();
        if (this.m_token != null && tokenIs("or")) {
            nextToken();
            insertOp(opPos, 2, 2);
            OrExpr();
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
        }
    }

    /* access modifiers changed from: protected */
    public void AndExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        EqualityExpr(-1);
        if (this.m_token != null && tokenIs("and")) {
            nextToken();
            insertOp(opPos, 2, 3);
            AndExpr();
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
        }
    }

    /* access modifiers changed from: protected */
    public int EqualityExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        if (-1 == addPos) {
            addPos = opPos;
        }
        RelationalExpr(-1);
        if (this.m_token == null) {
            return addPos;
        }
        if (tokenIs('!') && lookahead('=', 1)) {
            nextToken();
            nextToken();
            insertOp(addPos, 2, 4);
            int opPlusLeftHandLen = this.m_ops.getOp(1) - addPos;
            int addPos2 = EqualityExpr(addPos);
            this.m_ops.setOp(addPos2 + 1, this.m_ops.getOp(addPos2 + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
            return addPos2 + 2;
        } else if (!tokenIs('=')) {
            return addPos;
        } else {
            nextToken();
            insertOp(addPos, 2, 5);
            int opPlusLeftHandLen2 = this.m_ops.getOp(1) - addPos;
            int addPos3 = EqualityExpr(addPos);
            this.m_ops.setOp(addPos3 + 1, this.m_ops.getOp(addPos3 + opPlusLeftHandLen2 + 1) + opPlusLeftHandLen2);
            return addPos3 + 2;
        }
    }

    /* access modifiers changed from: protected */
    public int RelationalExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        if (-1 == addPos) {
            addPos = opPos;
        }
        AdditiveExpr(-1);
        if (this.m_token == null) {
            return addPos;
        }
        if (tokenIs('<')) {
            nextToken();
            if (tokenIs('=')) {
                nextToken();
                insertOp(addPos, 2, 6);
            } else {
                insertOp(addPos, 2, 7);
            }
            int opPlusLeftHandLen = this.m_ops.getOp(1) - addPos;
            int addPos2 = RelationalExpr(addPos);
            this.m_ops.setOp(addPos2 + 1, this.m_ops.getOp(addPos2 + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
            return addPos2 + 2;
        } else if (!tokenIs('>')) {
            return addPos;
        } else {
            nextToken();
            if (tokenIs('=')) {
                nextToken();
                insertOp(addPos, 2, 8);
            } else {
                insertOp(addPos, 2, 9);
            }
            int opPlusLeftHandLen2 = this.m_ops.getOp(1) - addPos;
            int addPos3 = RelationalExpr(addPos);
            this.m_ops.setOp(addPos3 + 1, this.m_ops.getOp(addPos3 + opPlusLeftHandLen2 + 1) + opPlusLeftHandLen2);
            return addPos3 + 2;
        }
    }

    /* access modifiers changed from: protected */
    public int AdditiveExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        if (-1 == addPos) {
            addPos = opPos;
        }
        MultiplicativeExpr(-1);
        if (this.m_token == null) {
            return addPos;
        }
        if (tokenIs('+')) {
            nextToken();
            insertOp(addPos, 2, 10);
            int opPlusLeftHandLen = this.m_ops.getOp(1) - addPos;
            int addPos2 = AdditiveExpr(addPos);
            this.m_ops.setOp(addPos2 + 1, this.m_ops.getOp(addPos2 + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
            return addPos2 + 2;
        } else if (!tokenIs('-')) {
            return addPos;
        } else {
            nextToken();
            insertOp(addPos, 2, 11);
            int opPlusLeftHandLen2 = this.m_ops.getOp(1) - addPos;
            int addPos3 = AdditiveExpr(addPos);
            this.m_ops.setOp(addPos3 + 1, this.m_ops.getOp(addPos3 + opPlusLeftHandLen2 + 1) + opPlusLeftHandLen2);
            return addPos3 + 2;
        }
    }

    /* access modifiers changed from: protected */
    public int MultiplicativeExpr(int addPos) throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        if (-1 == addPos) {
            addPos = opPos;
        }
        UnaryExpr();
        if (this.m_token == null) {
            return addPos;
        }
        if (tokenIs('*')) {
            nextToken();
            insertOp(addPos, 2, 12);
            int opPlusLeftHandLen = this.m_ops.getOp(1) - addPos;
            int addPos2 = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos2 + 1, this.m_ops.getOp(addPos2 + opPlusLeftHandLen + 1) + opPlusLeftHandLen);
            return addPos2 + 2;
        } else if (tokenIs("div")) {
            nextToken();
            insertOp(addPos, 2, 13);
            int opPlusLeftHandLen2 = this.m_ops.getOp(1) - addPos;
            int addPos3 = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos3 + 1, this.m_ops.getOp(addPos3 + opPlusLeftHandLen2 + 1) + opPlusLeftHandLen2);
            return addPos3 + 2;
        } else if (tokenIs("mod")) {
            nextToken();
            insertOp(addPos, 2, 14);
            int opPlusLeftHandLen3 = this.m_ops.getOp(1) - addPos;
            int addPos4 = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos4 + 1, this.m_ops.getOp(addPos4 + opPlusLeftHandLen3 + 1) + opPlusLeftHandLen3);
            return addPos4 + 2;
        } else if (!tokenIs("quo")) {
            return addPos;
        } else {
            nextToken();
            insertOp(addPos, 2, 15);
            int opPlusLeftHandLen4 = this.m_ops.getOp(1) - addPos;
            int addPos5 = MultiplicativeExpr(addPos);
            this.m_ops.setOp(addPos5 + 1, this.m_ops.getOp(addPos5 + opPlusLeftHandLen4 + 1) + opPlusLeftHandLen4);
            return addPos5 + 2;
        }
    }

    /* access modifiers changed from: protected */
    public void UnaryExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        boolean isNeg = false;
        if (this.m_tokenChar == '-') {
            nextToken();
            appendOp(2, 16);
            isNeg = true;
        }
        UnionExpr();
        if (isNeg) {
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
        }
    }

    /* access modifiers changed from: protected */
    public void StringExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        appendOp(2, 17);
        Expr();
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public void BooleanExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        appendOp(2, 18);
        Expr();
        int opLen = this.m_ops.getOp(1) - opPos;
        if (opLen == 2) {
            error(XPATHErrorResources.ER_BOOLEAN_ARG_NO_LONGER_OPTIONAL, null);
        }
        this.m_ops.setOp(opPos + 1, opLen);
    }

    /* access modifiers changed from: protected */
    public void NumberExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        appendOp(2, 19);
        Expr();
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public void UnionExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        boolean foundUnion = false;
        do {
            PathExpr();
            if (!tokenIs('|')) {
                break;
            }
            if (!foundUnion) {
                foundUnion = true;
                insertOp(opPos, 2, 20);
            }
            nextToken();
        } while (1 != 0);
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public void PathExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        int filterExprMatch = FilterExpr();
        if (filterExprMatch != 0) {
            boolean locationPathStarted = filterExprMatch == 2;
            if (tokenIs('/')) {
                nextToken();
                if (!locationPathStarted) {
                    insertOp(opPos, 2, 28);
                    locationPathStarted = true;
                }
                if (!RelativeLocationPath()) {
                    error(XPATHErrorResources.ER_EXPECTED_REL_LOC_PATH, null);
                }
            }
            if (locationPathStarted) {
                this.m_ops.setOp(this.m_ops.getOp(1), -1);
                this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
                this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
                return;
            }
            return;
        }
        LocationPath();
    }

    /* access modifiers changed from: protected */
    public int FilterExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        if (!PrimaryExpr()) {
            return 0;
        }
        if (!tokenIs('[')) {
            return 1;
        }
        insertOp(opPos, 2, 28);
        while (tokenIs('[')) {
            Predicate();
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public boolean PrimaryExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        if (this.m_tokenChar == '\'' || this.m_tokenChar == '\"') {
            appendOp(2, 21);
            Literal();
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
            return true;
        } else if (this.m_tokenChar == '$') {
            nextToken();
            appendOp(2, 22);
            QName();
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
            return true;
        } else if (this.m_tokenChar == '(') {
            nextToken();
            appendOp(2, 23);
            Expr();
            consumeExpected(')');
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
            return true;
        } else if (this.m_token != null && (('.' == this.m_tokenChar && this.m_token.length() > 1 && Character.isDigit(this.m_token.charAt(1))) || Character.isDigit(this.m_tokenChar))) {
            appendOp(2, 27);
            Number();
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
            return true;
        } else if (lookahead('(', 1) || (lookahead(':', 1) && lookahead('(', 3))) {
            return FunctionCall();
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void Argument() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        appendOp(2, 26);
        Expr();
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public boolean FunctionCall() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        if (!lookahead(':', 1)) {
            int funcTok = getFunctionToken(this.m_token);
            if (-1 == funcTok) {
                error(XPATHErrorResources.ER_COULDNOT_FIND_FUNCTION, new Object[]{this.m_token});
            }
            switch (funcTok) {
                case OpCodes.NODETYPE_COMMENT:
                case OpCodes.NODETYPE_TEXT:
                case OpCodes.NODETYPE_PI:
                case OpCodes.NODETYPE_NODE:
                    return false;
                default:
                    appendOp(3, 25);
                    this.m_ops.setOp(opPos + 1 + 1, funcTok);
                    nextToken();
                    break;
            }
        } else {
            appendOp(4, 24);
            this.m_ops.setOp(opPos + 1 + 1, this.m_queueMark - 1);
            nextToken();
            consumeExpected(':');
            this.m_ops.setOp(opPos + 1 + 2, this.m_queueMark - 1);
            nextToken();
        }
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
        this.m_ops.setOp(this.m_ops.getOp(1), -1);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
        return true;
    }

    /* access modifiers changed from: protected */
    public void LocationPath() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        appendOp(2, 28);
        boolean seenSlash = tokenIs('/');
        if (seenSlash) {
            appendOp(4, 50);
            this.m_ops.setOp(this.m_ops.getOp(1) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(1) - 1, 35);
            nextToken();
        } else if (this.m_token == null) {
            error(XPATHErrorResources.ER_EXPECTED_LOC_PATH_AT_END_EXPR, null);
        }
        if (this.m_token != null && !RelativeLocationPath() && !seenSlash) {
            error(XPATHErrorResources.ER_EXPECTED_LOC_PATH, new Object[]{this.m_token});
        }
        this.m_ops.setOp(this.m_ops.getOp(1), -1);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public boolean RelativeLocationPath() throws TransformerException {
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

    /* access modifiers changed from: protected */
    public boolean Step() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        boolean doubleSlash = tokenIs('/');
        if (doubleSlash) {
            nextToken();
            appendOp(2, 42);
            this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
            this.m_ops.setOp(this.m_ops.getOp(1), OpCodes.NODETYPE_NODE);
            this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
            this.m_ops.setOp(opPos + 1 + 1, this.m_ops.getOp(1) - opPos);
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
            opPos = this.m_ops.getOp(1);
        }
        if (tokenIs(Constants.ATTRVAL_THIS)) {
            nextToken();
            if (tokenIs('[')) {
                error(XPATHErrorResources.ER_PREDICATE_ILLEGAL_SYNTAX, null);
            }
            appendOp(4, 48);
            this.m_ops.setOp(this.m_ops.getOp(1) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(1) - 1, OpCodes.NODETYPE_NODE);
        } else if (tokenIs(Constants.ATTRVAL_PARENT)) {
            nextToken();
            appendOp(4, 45);
            this.m_ops.setOp(this.m_ops.getOp(1) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(1) - 1, OpCodes.NODETYPE_NODE);
        } else if (tokenIs('*') || tokenIs('@') || tokenIs('_') || (this.m_token != null && Character.isLetter(this.m_token.charAt(0)))) {
            Basis();
            while (tokenIs('[')) {
                Predicate();
            }
            this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
        } else {
            if (doubleSlash) {
                error(XPATHErrorResources.ER_EXPECTED_LOC_STEP, null);
            }
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void Basis() throws TransformerException {
        int axesType;
        int opPos = this.m_ops.getOp(1);
        if (lookahead("::", 1)) {
            axesType = AxisName();
            nextToken();
            nextToken();
        } else if (tokenIs('@')) {
            axesType = 39;
            appendOp(2, 39);
            nextToken();
        } else {
            axesType = 40;
            appendOp(2, 40);
        }
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        NodeTest(axesType);
        this.m_ops.setOp(opPos + 1 + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public int AxisName() throws TransformerException {
        Object val = Keywords.getAxisName(this.m_token);
        if (val == null) {
            error(XPATHErrorResources.ER_ILLEGAL_AXIS_NAME, new Object[]{this.m_token});
        }
        int axesType = ((Integer) val).intValue();
        appendOp(2, axesType);
        return axesType;
    }

    /* access modifiers changed from: protected */
    public void NodeTest(int axesType) throws TransformerException {
        if (lookahead('(', 1)) {
            Object nodeTestOp = Keywords.getNodeType(this.m_token);
            if (nodeTestOp == null) {
                error(XPATHErrorResources.ER_UNKNOWN_NODETYPE, new Object[]{this.m_token});
                return;
            }
            nextToken();
            int nt = ((Integer) nodeTestOp).intValue();
            this.m_ops.setOp(this.m_ops.getOp(1), nt);
            this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
            consumeExpected('(');
            if (1032 == nt && !tokenIs(')')) {
                Literal();
            }
            consumeExpected(')');
            return;
        }
        this.m_ops.setOp(this.m_ops.getOp(1), 34);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        if (lookahead(':', 1)) {
            if (tokenIs('*')) {
                this.m_ops.setOp(this.m_ops.getOp(1), -3);
            } else {
                this.m_ops.setOp(this.m_ops.getOp(1), this.m_queueMark - 1);
                if (!Character.isLetter(this.m_tokenChar) && !tokenIs('_')) {
                    error(XPATHErrorResources.ER_EXPECTED_NODE_TEST, null);
                }
            }
            nextToken();
            consumeExpected(':');
        } else {
            this.m_ops.setOp(this.m_ops.getOp(1), -2);
        }
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        if (tokenIs('*')) {
            this.m_ops.setOp(this.m_ops.getOp(1), -3);
        } else {
            this.m_ops.setOp(this.m_ops.getOp(1), this.m_queueMark - 1);
            if (!Character.isLetter(this.m_tokenChar) && !tokenIs('_')) {
                error(XPATHErrorResources.ER_EXPECTED_NODE_TEST, null);
            }
        }
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        nextToken();
    }

    /* access modifiers changed from: protected */
    public void Predicate() throws TransformerException {
        if (tokenIs('[')) {
            nextToken();
            PredicateExpr();
            consumeExpected(']');
        }
    }

    /* access modifiers changed from: protected */
    public void PredicateExpr() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        appendOp(2, 29);
        Expr();
        this.m_ops.setOp(this.m_ops.getOp(1), -1);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public void QName() throws TransformerException {
        if (lookahead(':', 1)) {
            this.m_ops.setOp(this.m_ops.getOp(1), this.m_queueMark - 1);
            this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
            nextToken();
            consumeExpected(':');
        } else {
            this.m_ops.setOp(this.m_ops.getOp(1), -2);
            this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        }
        this.m_ops.setOp(this.m_ops.getOp(1), this.m_queueMark - 1);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        nextToken();
    }

    /* access modifiers changed from: protected */
    public void NCName() {
        this.m_ops.setOp(this.m_ops.getOp(1), this.m_queueMark - 1);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        nextToken();
    }

    /* access modifiers changed from: protected */
    public void Literal() throws TransformerException {
        int last = this.m_token.length() - 1;
        char c0 = this.m_tokenChar;
        char cX = this.m_token.charAt(last);
        if ((c0 == '\"' && cX == '\"') || (c0 == '\'' && cX == '\'')) {
            int tokenQueuePos = this.m_queueMark - 1;
            this.m_ops.m_tokenQueue.setElementAt(null, tokenQueuePos);
            this.m_ops.m_tokenQueue.setElementAt(new XString(this.m_token.substring(1, last)), tokenQueuePos);
            this.m_ops.setOp(this.m_ops.getOp(1), tokenQueuePos);
            this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
            nextToken();
            return;
        }
        error(XPATHErrorResources.ER_PATTERN_LITERAL_NEEDS_BE_QUOTED, new Object[]{this.m_token});
    }

    /* access modifiers changed from: protected */
    public void Number() throws TransformerException {
        double num;
        if (this.m_token != null) {
            try {
                if (this.m_token.indexOf(101) > -1 || this.m_token.indexOf(69) > -1) {
                    throw new NumberFormatException();
                }
                num = Double.valueOf(this.m_token).doubleValue();
                this.m_ops.m_tokenQueue.setElementAt(new XNumber(num), this.m_queueMark - 1);
                this.m_ops.setOp(this.m_ops.getOp(1), this.m_queueMark - 1);
                this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
                nextToken();
            } catch (NumberFormatException e) {
                error(XPATHErrorResources.ER_COULDNOT_BE_FORMATTED_TO_NUMBER, new Object[]{this.m_token});
                num = 0.0d;
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
    public void LocationPathPattern() throws TransformerException {
        int opPos = this.m_ops.getOp(1);
        int relativePathStatus = 0;
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
                this.m_ops.setOp(this.m_ops.getOp(1) - 2, 4);
                this.m_ops.setOp(this.m_ops.getOp(1) - 1, OpCodes.NODETYPE_FUNCTEST);
                relativePathStatus = 2;
            }
        } else if (tokenIs('/')) {
            if (lookahead('/', 1)) {
                appendOp(4, 52);
                nextToken();
                relativePathStatus = 2;
            } else {
                appendOp(4, 50);
                relativePathStatus = 1;
            }
            this.m_ops.setOp(this.m_ops.getOp(1) - 2, 4);
            this.m_ops.setOp(this.m_ops.getOp(1) - 1, 35);
            nextToken();
        } else {
            relativePathStatus = 2;
        }
        if (relativePathStatus != 0) {
            if (!tokenIs('|') && this.m_token != null) {
                RelativePathPattern();
            } else if (relativePathStatus == 2) {
                error(XPATHErrorResources.ER_EXPECTED_REL_PATH_PATTERN, null);
            }
        }
        this.m_ops.setOp(this.m_ops.getOp(1), -1);
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
    }

    /* access modifiers changed from: protected */
    public void IdKeyPattern() throws TransformerException {
        FunctionCall();
    }

    /* access modifiers changed from: protected */
    public void RelativePathPattern() throws TransformerException {
        boolean trailingSlashConsumed = StepPattern(false);
        while (tokenIs('/')) {
            nextToken();
            trailingSlashConsumed = StepPattern(!trailingSlashConsumed);
        }
    }

    /* access modifiers changed from: protected */
    public boolean StepPattern(boolean isLeadingSlashPermitted) throws TransformerException {
        return AbbreviatedNodeTestStep(isLeadingSlashPermitted);
    }

    /* access modifiers changed from: protected */
    public boolean AbbreviatedNodeTestStep(boolean isLeadingSlashPermitted) throws TransformerException {
        int axesType;
        int opPos = this.m_ops.getOp(1);
        int matchTypePos = -1;
        boolean trailingSlashConsumed = false;
        if (tokenIs('@')) {
            axesType = 51;
            appendOp(2, 51);
            nextToken();
        } else if (lookahead("::", 1)) {
            if (tokenIs("attribute")) {
                axesType = 51;
                appendOp(2, 51);
            } else if (tokenIs("child")) {
                matchTypePos = this.m_ops.getOp(1);
                axesType = 53;
                appendOp(2, 53);
            } else {
                axesType = -1;
                error(XPATHErrorResources.ER_AXES_NOT_ALLOWED, new Object[]{this.m_token});
            }
            nextToken();
            nextToken();
        } else if (tokenIs('/') != 0) {
            if (!isLeadingSlashPermitted) {
                error(XPATHErrorResources.ER_EXPECTED_STEP_PATTERN, null);
            }
            axesType = 52;
            appendOp(2, 52);
            nextToken();
        } else {
            matchTypePos = this.m_ops.getOp(1);
            axesType = 53;
            appendOp(2, 53);
        }
        this.m_ops.setOp(1, this.m_ops.getOp(1) + 1);
        NodeTest(axesType);
        this.m_ops.setOp(opPos + 1 + 1, this.m_ops.getOp(1) - opPos);
        while (tokenIs('[')) {
            Predicate();
        }
        if (matchTypePos > -1 && tokenIs('/') && lookahead('/', 1)) {
            this.m_ops.setOp(matchTypePos, 52);
            nextToken();
            trailingSlashConsumed = true;
        }
        this.m_ops.setOp(opPos + 1, this.m_ops.getOp(1) - opPos);
        return trailingSlashConsumed;
    }
}

package gov.nist.core;

import java.text.ParseException;

public abstract class ParserCore {
    public static final boolean debug = Debug.parserDebug;
    static int nesting_level;
    protected LexerCore lexer;

    /* access modifiers changed from: protected */
    public NameValue nameValue(char separator) throws ParseException {
        String str;
        if (debug) {
            dbg_enter("nameValue");
        }
        try {
            this.lexer.match(4095);
            Token name = this.lexer.getNextToken();
            this.lexer.SPorHT();
            boolean quoted = false;
            try {
                if (this.lexer.lookAhead(0) == separator) {
                    this.lexer.consume(1);
                    this.lexer.SPorHT();
                    boolean isFlag = false;
                    if (this.lexer.lookAhead(0) == '\"') {
                        str = this.lexer.quotedString();
                        quoted = true;
                    } else {
                        this.lexer.match(4095);
                        String str2 = this.lexer.getNextToken().tokenValue;
                        if (str2 == null) {
                            isFlag = true;
                            str = "";
                        } else {
                            str = str2;
                        }
                    }
                    NameValue nv = new NameValue(name.tokenValue, str, isFlag);
                    if (quoted) {
                        nv.setQuotedValue();
                    }
                    return nv;
                }
                NameValue nameValue = new NameValue(name.tokenValue, "", true);
                if (debug) {
                    dbg_leave("nameValue");
                }
                return nameValue;
            } catch (ParseException e) {
                NameValue nameValue2 = new NameValue(name.tokenValue, null, false);
                if (debug) {
                    dbg_leave("nameValue");
                }
                return nameValue2;
            }
        } finally {
            if (debug) {
                dbg_leave("nameValue");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dbg_enter(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nesting_level; i++) {
            stringBuffer.append(Separators.GREATER_THAN);
        }
        if (debug) {
            System.out.println(((Object) stringBuffer) + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level++;
    }

    /* access modifiers changed from: protected */
    public void dbg_leave(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nesting_level; i++) {
            stringBuffer.append(Separators.LESS_THAN);
        }
        if (debug) {
            System.out.println(((Object) stringBuffer) + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level--;
    }

    /* access modifiers changed from: protected */
    public NameValue nameValue() throws ParseException {
        return nameValue('=');
    }

    /* access modifiers changed from: protected */
    public void peekLine(String rule) {
        if (debug) {
            Debug.println(rule + Separators.SP + this.lexer.peekLine());
        }
    }
}

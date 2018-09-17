package gov.nist.core;

import java.text.ParseException;

public abstract class ParserCore {
    public static final boolean debug = Debug.parserDebug;
    static int nesting_level;
    protected LexerCore lexer;

    protected NameValue nameValue(char separator) throws ParseException {
        if (debug) {
            dbg_enter("nameValue");
        }
        Token name;
        NameValue nameValue;
        try {
            this.lexer.match(4095);
            name = this.lexer.getNextToken();
            this.lexer.SPorHT();
            boolean quoted = false;
            if (this.lexer.lookAhead(0) == separator) {
                String str;
                this.lexer.consume(1);
                this.lexer.SPorHT();
                boolean isFlag = false;
                if (this.lexer.lookAhead(0) == '\"') {
                    str = this.lexer.quotedString();
                    quoted = true;
                } else {
                    this.lexer.match(4095);
                    str = this.lexer.getNextToken().tokenValue;
                    if (str == null) {
                        str = "";
                        isFlag = true;
                    }
                }
                NameValue nv = new NameValue(name.tokenValue, str, isFlag);
                if (quoted) {
                    nv.setQuotedValue();
                }
                if (debug) {
                    dbg_leave("nameValue");
                }
                return nv;
            }
            nameValue = new NameValue(name.tokenValue, "", true);
            if (debug) {
                dbg_leave("nameValue");
            }
            return nameValue;
        } catch (ParseException e) {
            nameValue = new NameValue(name.tokenValue, null, false);
            if (debug) {
                dbg_leave("nameValue");
            }
            return nameValue;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("nameValue");
            }
            throw th;
        }
    }

    protected void dbg_enter(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nesting_level; i++) {
            stringBuffer.append(Separators.GREATER_THAN);
        }
        if (debug) {
            System.out.println(stringBuffer + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level++;
    }

    protected void dbg_leave(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nesting_level; i++) {
            stringBuffer.append(Separators.LESS_THAN);
        }
        if (debug) {
            System.out.println(stringBuffer + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level--;
    }

    protected NameValue nameValue() throws ParseException {
        return nameValue('=');
    }

    protected void peekLine(String rule) {
        if (debug) {
            Debug.println(rule + Separators.SP + this.lexer.peekLine());
        }
    }
}

package ohos.com.sun.java_cup.internal.runtime;

import java.io.PrintStream;
import java.util.Stack;

public abstract class lr_parser {
    protected static final int _error_sync_size = 3;
    protected boolean _done_parsing;
    private Scanner _scanner;
    protected short[][] action_tab;
    protected Symbol cur_token;
    protected Symbol[] lookahead;
    protected int lookahead_pos;
    protected short[][] production_tab;
    protected short[][] reduce_tab;
    protected Stack stack;
    protected int tos;

    public abstract int EOF_sym();

    public abstract short[][] action_table();

    public abstract Symbol do_action(int i, lr_parser lr_parser, Stack stack2, int i2) throws Exception;

    public abstract int error_sym();

    /* access modifiers changed from: protected */
    public int error_sync_size() {
        return 3;
    }

    /* access modifiers changed from: protected */
    public abstract void init_actions() throws Exception;

    public abstract short[][] production_table();

    public abstract short[][] reduce_table();

    public abstract int start_production();

    public abstract int start_state();

    public void user_init() throws Exception {
    }

    public lr_parser() {
        this._done_parsing = false;
        this.stack = new Stack();
    }

    public lr_parser(Scanner scanner) {
        this();
        setScanner(scanner);
    }

    public void done_parsing() {
        this._done_parsing = true;
    }

    public void setScanner(Scanner scanner) {
        this._scanner = scanner;
    }

    public Scanner getScanner() {
        return this._scanner;
    }

    public Symbol scan() throws Exception {
        return getScanner().next_token();
    }

    public void report_fatal_error(String str, Object obj) throws Exception {
        done_parsing();
        report_error(str, obj);
        throw new Exception("Can't recover from previous error(s)");
    }

    public void report_error(String str, Object obj) {
        System.err.print(str);
        if (obj instanceof Symbol) {
            Symbol symbol = (Symbol) obj;
            if (symbol.left != -1) {
                PrintStream printStream = System.err;
                printStream.println(" at character " + symbol.left + " of input");
                return;
            }
            System.err.println("");
            return;
        }
        System.err.println("");
    }

    public void syntax_error(Symbol symbol) {
        report_error("Syntax error", symbol);
    }

    public void unrecovered_syntax_error(Symbol symbol) throws Exception {
        report_fatal_error("Couldn't repair and continue parse", symbol);
    }

    /* access modifiers changed from: protected */
    public final short get_action(int i, int i2) {
        short[] sArr = this.action_tab[i];
        int i3 = 0;
        if (sArr.length < 20) {
            int i4 = 0;
            while (i4 < sArr.length) {
                int i5 = i4 + 1;
                short s = sArr[i4];
                if (s == i2 || s == -1) {
                    return sArr[i5];
                }
                i4 = i5 + 1;
            }
            return 0;
        }
        int length = ((sArr.length - 1) / 2) - 1;
        while (i3 <= length) {
            int i6 = (i3 + length) / 2;
            int i7 = i6 * 2;
            if (i2 == sArr[i7]) {
                return sArr[i7 + 1];
            }
            if (i2 > sArr[i7]) {
                i3 = i6 + 1;
            } else {
                length = i6 - 1;
            }
        }
        return sArr[sArr.length - 1];
    }

    /* access modifiers changed from: protected */
    public final short get_reduce(int i, int i2) {
        short[] sArr = this.reduce_tab[i];
        if (sArr == null) {
            return -1;
        }
        int i3 = 0;
        while (i3 < sArr.length) {
            int i4 = i3 + 1;
            short s = sArr[i3];
            if (s == i2 || s == -1) {
                return sArr[i4];
            }
            i3 = i4 + 1;
        }
        return -1;
    }

    public Symbol parse() throws Exception {
        this.production_tab = production_table();
        this.action_tab = action_table();
        this.reduce_tab = reduce_table();
        init_actions();
        user_init();
        this.cur_token = scan();
        this.stack.removeAllElements();
        this.stack.push(new Symbol(0, start_state()));
        this.tos = 0;
        this._done_parsing = false;
        Symbol symbol = null;
        while (!this._done_parsing) {
            if (!this.cur_token.used_by_parser) {
                short s = get_action(((Symbol) this.stack.peek()).parse_state, this.cur_token.sym);
                if (s > 0) {
                    Symbol symbol2 = this.cur_token;
                    symbol2.parse_state = s - 1;
                    symbol2.used_by_parser = true;
                    this.stack.push(symbol2);
                    this.tos++;
                    this.cur_token = scan();
                } else if (s < 0) {
                    int i = (-s) - 1;
                    Symbol do_action = do_action(i, this, this.stack, this.tos);
                    short[][] sArr = this.production_tab;
                    short s2 = sArr[i][0];
                    short s3 = sArr[i][1];
                    for (int i2 = 0; i2 < s3; i2++) {
                        this.stack.pop();
                        this.tos--;
                    }
                    do_action.parse_state = get_reduce(((Symbol) this.stack.peek()).parse_state, s2);
                    do_action.used_by_parser = true;
                    this.stack.push(do_action);
                    this.tos++;
                    symbol = do_action;
                } else if (s == 0) {
                    syntax_error(this.cur_token);
                    if (!error_recovery(false)) {
                        unrecovered_syntax_error(this.cur_token);
                        done_parsing();
                    } else {
                        symbol = (Symbol) this.stack.peek();
                    }
                }
            } else {
                throw new Error("Symbol recycling detected (fix your scanner).");
            }
        }
        return symbol;
    }

    public void debug_message(String str) {
        System.err.println(str);
    }

    public void dump_stack() {
        if (this.stack == null) {
            debug_message("# Stack dump requested, but stack is null");
            return;
        }
        debug_message("============ Parse Stack Dump ============");
        for (int i = 0; i < this.stack.size(); i++) {
            debug_message("Symbol: " + ((Symbol) this.stack.elementAt(i)).sym + " State: " + ((Symbol) this.stack.elementAt(i)).parse_state);
        }
        debug_message("==========================================");
    }

    public void debug_reduce(int i, int i2, int i3) {
        debug_message("# Reduce with prod #" + i + " [NT=" + i2 + ", SZ=" + i3 + "]");
    }

    public void debug_shift(Symbol symbol) {
        debug_message("# Shift under term #" + symbol.sym + " to state #" + symbol.parse_state);
    }

    public void debug_stack() {
        StringBuffer stringBuffer = new StringBuffer("## STACK:");
        for (int i = 0; i < this.stack.size(); i++) {
            Symbol symbol = (Symbol) this.stack.elementAt(i);
            stringBuffer.append(" <state " + symbol.parse_state + ", sym " + symbol.sym + ">");
            if (i % 3 == 2 || i == this.stack.size() - 1) {
                debug_message(stringBuffer.toString());
                stringBuffer = new StringBuffer("         ");
            }
        }
    }

    public Symbol debug_parse() throws Exception {
        this.production_tab = production_table();
        this.action_tab = action_table();
        this.reduce_tab = reduce_table();
        debug_message("# Initializing parser");
        init_actions();
        user_init();
        this.cur_token = scan();
        debug_message("# Current Symbol is #" + this.cur_token.sym);
        this.stack.removeAllElements();
        this.stack.push(new Symbol(0, start_state()));
        this.tos = 0;
        this._done_parsing = false;
        Symbol symbol = null;
        while (!this._done_parsing) {
            if (!this.cur_token.used_by_parser) {
                short s = get_action(((Symbol) this.stack.peek()).parse_state, this.cur_token.sym);
                if (s > 0) {
                    Symbol symbol2 = this.cur_token;
                    symbol2.parse_state = s - 1;
                    symbol2.used_by_parser = true;
                    debug_shift(symbol2);
                    this.stack.push(this.cur_token);
                    this.tos++;
                    this.cur_token = scan();
                    debug_message("# Current token is " + this.cur_token);
                } else if (s < 0) {
                    int i = (-s) - 1;
                    Symbol do_action = do_action(i, this, this.stack, this.tos);
                    short[][] sArr = this.production_tab;
                    short s2 = sArr[i][0];
                    short s3 = sArr[i][1];
                    debug_reduce(i, s2, s3);
                    for (int i2 = 0; i2 < s3; i2++) {
                        this.stack.pop();
                        this.tos--;
                    }
                    short s4 = get_reduce(((Symbol) this.stack.peek()).parse_state, s2);
                    debug_message("# Reduce rule: top state " + ((Symbol) this.stack.peek()).parse_state + ", lhs sym " + ((int) s2) + " -> state " + ((int) s4));
                    do_action.parse_state = s4;
                    do_action.used_by_parser = true;
                    this.stack.push(do_action);
                    this.tos = this.tos + 1;
                    debug_message("# Goto state #" + ((int) s4));
                    symbol = do_action;
                } else if (s == 0) {
                    syntax_error(this.cur_token);
                    if (!error_recovery(true)) {
                        unrecovered_syntax_error(this.cur_token);
                        done_parsing();
                    } else {
                        symbol = (Symbol) this.stack.peek();
                    }
                }
            } else {
                throw new Error("Symbol recycling detected (fix your scanner).");
            }
        }
        return symbol;
    }

    /* access modifiers changed from: protected */
    public boolean error_recovery(boolean z) throws Exception {
        if (z) {
            debug_message("# Attempting error recovery");
        }
        if (!find_recovery_config(z)) {
            if (z) {
                debug_message("# Error recovery fails");
            }
            return false;
        }
        read_lookahead();
        while (true) {
            if (z) {
                debug_message("# Trying to parse ahead");
            }
            if (try_parse_ahead(z)) {
                if (z) {
                    debug_message("# Parse-ahead ok, going back to normal parse");
                }
                parse_lookahead(z);
                return true;
            } else if (this.lookahead[0].sym == EOF_sym()) {
                if (z) {
                    debug_message("# Error recovery fails at EOF");
                }
                return false;
            } else {
                if (z) {
                    debug_message("# Consuming Symbol #" + cur_err_token().sym);
                }
                restart_lookahead();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean shift_under_error() {
        return get_action(((Symbol) this.stack.peek()).parse_state, error_sym()) > 0;
    }

    /* access modifiers changed from: protected */
    public boolean find_recovery_config(boolean z) {
        if (z) {
            debug_message("# Finding recovery state on stack");
        }
        int i = ((Symbol) this.stack.peek()).right;
        int i2 = ((Symbol) this.stack.peek()).left;
        while (!shift_under_error()) {
            if (z) {
                debug_message("# Pop stack by one, state was # " + ((Symbol) this.stack.peek()).parse_state);
            }
            i2 = ((Symbol) this.stack.pop()).left;
            this.tos--;
            if (this.stack.empty()) {
                if (!z) {
                    return false;
                }
                debug_message("# No recovery state found on stack");
                return false;
            }
        }
        short s = get_action(((Symbol) this.stack.peek()).parse_state, error_sym());
        if (z) {
            debug_message("# Recover state found (#" + ((Symbol) this.stack.peek()).parse_state + ")");
            StringBuilder sb = new StringBuilder();
            sb.append("# Shifting on error to state #");
            sb.append(s + -1);
            debug_message(sb.toString());
        }
        Symbol symbol = new Symbol(error_sym(), i2, i);
        symbol.parse_state = s - 1;
        symbol.used_by_parser = true;
        this.stack.push(symbol);
        this.tos++;
        return true;
    }

    /* access modifiers changed from: protected */
    public void read_lookahead() throws Exception {
        this.lookahead = new Symbol[error_sync_size()];
        for (int i = 0; i < error_sync_size(); i++) {
            this.lookahead[i] = this.cur_token;
            this.cur_token = scan();
        }
        this.lookahead_pos = 0;
    }

    /* access modifiers changed from: protected */
    public Symbol cur_err_token() {
        return this.lookahead[this.lookahead_pos];
    }

    /* access modifiers changed from: protected */
    public boolean advance_lookahead() {
        this.lookahead_pos++;
        if (this.lookahead_pos < error_sync_size()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void restart_lookahead() throws Exception {
        for (int i = 1; i < error_sync_size(); i++) {
            Symbol[] symbolArr = this.lookahead;
            symbolArr[i - 1] = symbolArr[i];
        }
        this.cur_token = scan();
        this.lookahead[error_sync_size() - 1] = this.cur_token;
        this.lookahead_pos = 0;
    }

    /* access modifiers changed from: protected */
    public boolean try_parse_ahead(boolean z) throws Exception {
        virtual_parse_stack virtual_parse_stack = new virtual_parse_stack(this.stack);
        while (true) {
            short s = get_action(virtual_parse_stack.top(), cur_err_token().sym);
            if (s == 0) {
                return false;
            }
            if (s > 0) {
                int i = s - 1;
                virtual_parse_stack.push(i);
                if (z) {
                    debug_message("# Parse-ahead shifts Symbol #" + cur_err_token().sym + " into state #" + i);
                }
                if (!advance_lookahead()) {
                    return true;
                }
            } else {
                int i2 = (-s) - 1;
                if (i2 == start_production()) {
                    if (z) {
                        debug_message("# Parse-ahead accepts");
                    }
                    return true;
                }
                short[][] sArr = this.production_tab;
                short s2 = sArr[i2][0];
                short s3 = sArr[i2][1];
                for (int i3 = 0; i3 < s3; i3++) {
                    virtual_parse_stack.pop();
                }
                if (z) {
                    debug_message("# Parse-ahead reduces: handle size = " + ((int) s3) + " lhs = #" + ((int) s2) + " from state #" + virtual_parse_stack.top());
                }
                virtual_parse_stack.push(get_reduce(virtual_parse_stack.top(), s2));
                if (z) {
                    debug_message("# Goto state #" + virtual_parse_stack.top());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void parse_lookahead(boolean z) throws Exception {
        this.lookahead_pos = 0;
        if (z) {
            debug_message("# Reparsing saved input with actions");
            debug_message("# Current Symbol is #" + cur_err_token().sym);
            debug_message("# Current state is #" + ((Symbol) this.stack.peek()).parse_state);
        }
        Object obj = null;
        while (!this._done_parsing) {
            short s = get_action(((Symbol) this.stack.peek()).parse_state, cur_err_token().sym);
            if (s > 0) {
                cur_err_token().parse_state = s - 1;
                cur_err_token().used_by_parser = true;
                if (z) {
                    debug_shift(cur_err_token());
                }
                this.stack.push(cur_err_token());
                this.tos++;
                if (!advance_lookahead()) {
                    if (z) {
                        debug_message("# Completed reparse");
                        return;
                    }
                    return;
                } else if (z) {
                    debug_message("# Current Symbol is #" + cur_err_token().sym);
                }
            } else if (s < 0) {
                int i = (-s) - 1;
                Symbol do_action = do_action(i, this, this.stack, this.tos);
                short[][] sArr = this.production_tab;
                short s2 = sArr[i][0];
                short s3 = sArr[i][1];
                if (z) {
                    debug_reduce(i, s2, s3);
                }
                for (int i2 = 0; i2 < s3; i2++) {
                    this.stack.pop();
                    this.tos--;
                }
                short s4 = get_reduce(((Symbol) this.stack.peek()).parse_state, s2);
                do_action.parse_state = s4;
                do_action.used_by_parser = true;
                this.stack.push(do_action);
                this.tos++;
                if (z) {
                    debug_message("# Goto state #" + ((int) s4));
                }
                obj = do_action;
            } else if (s == 0) {
                report_fatal_error("Syntax error", obj);
                return;
            }
        }
    }

    protected static short[][] unpackFromStrings(String[] strArr) {
        StringBuffer stringBuffer = new StringBuffer(strArr[0]);
        for (int i = 1; i < strArr.length; i++) {
            stringBuffer.append(strArr[i]);
        }
        int charAt = (stringBuffer.charAt(0) << 16) | stringBuffer.charAt(1);
        short[][] sArr = new short[charAt][];
        int i2 = 0;
        int i3 = 2;
        while (i2 < charAt) {
            int charAt2 = (stringBuffer.charAt(i3) << 16) | stringBuffer.charAt(i3 + 1);
            sArr[i2] = new short[charAt2];
            int i4 = i3 + 2;
            int i5 = 0;
            while (i5 < charAt2) {
                sArr[i2][i5] = (short) (stringBuffer.charAt(i4) - 2);
                i5++;
                i4++;
            }
            i2++;
            i3 = i4;
        }
        return sArr;
    }
}

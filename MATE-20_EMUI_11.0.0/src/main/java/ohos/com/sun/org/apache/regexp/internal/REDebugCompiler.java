package ohos.com.sun.org.apache.regexp.internal;

import java.io.PrintWriter;
import java.util.Hashtable;
import ohos.global.icu.impl.locale.LanguageTag;

public class REDebugCompiler extends RECompiler {
    static Hashtable hashOpcode = new Hashtable();

    static {
        hashOpcode.put(new Integer(56), "OP_RELUCTANTSTAR");
        hashOpcode.put(new Integer(61), "OP_RELUCTANTPLUS");
        hashOpcode.put(new Integer(47), "OP_RELUCTANTMAYBE");
        hashOpcode.put(new Integer(69), "OP_END");
        hashOpcode.put(new Integer(94), "OP_BOL");
        hashOpcode.put(new Integer(36), "OP_EOL");
        hashOpcode.put(new Integer(46), "OP_ANY");
        hashOpcode.put(new Integer(91), "OP_ANYOF");
        hashOpcode.put(new Integer(124), "OP_BRANCH");
        hashOpcode.put(new Integer(65), "OP_ATOM");
        hashOpcode.put(new Integer(42), "OP_STAR");
        hashOpcode.put(new Integer(43), "OP_PLUS");
        hashOpcode.put(new Integer(63), "OP_MAYBE");
        hashOpcode.put(new Integer(78), "OP_NOTHING");
        hashOpcode.put(new Integer(71), "OP_GOTO");
        hashOpcode.put(new Integer(92), "OP_ESCAPE");
        hashOpcode.put(new Integer(40), "OP_OPEN");
        hashOpcode.put(new Integer(41), "OP_CLOSE");
        hashOpcode.put(new Integer(35), "OP_BACKREF");
        hashOpcode.put(new Integer(80), "OP_POSIXCLASS");
        hashOpcode.put(new Integer(60), "OP_OPEN_CLUSTER");
        hashOpcode.put(new Integer(62), "OP_CLOSE_CLUSTER");
    }

    /* access modifiers changed from: package-private */
    public String opcodeToString(char c) {
        String str = (String) hashOpcode.get(new Integer(c));
        return str == null ? "OP_????" : str;
    }

    /* access modifiers changed from: package-private */
    public String charToString(char c) {
        if (c >= ' ' && c <= 127) {
            return String.valueOf(c);
        }
        return "\\" + ((int) c);
    }

    /* access modifiers changed from: package-private */
    public String nodeToString(int i) {
        char c = this.instruction[i + 0];
        char c2 = this.instruction[i + 1];
        return opcodeToString(c) + ", opdata = " + ((int) c2);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v5, types: [int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void dumpProgram(PrintWriter printWriter) {
        int i;
        for (short s = 0; s < this.lenInstruction; s = i) {
            char c = this.instruction[s + 0];
            char c2 = this.instruction[s + 1];
            short s2 = (short) this.instruction[s + 2];
            printWriter.print(((int) s) + ". " + nodeToString(s) + ", next = ");
            if (s2 == 0) {
                printWriter.print("none");
            } else {
                printWriter.print(s2 + s);
            }
            i = s + 3;
            if (c == '[') {
                printWriter.print(", [");
                int i2 = i;
                int i3 = 0;
                while (i3 < c2) {
                    int i4 = i2 + 1;
                    char c3 = this.instruction[i2];
                    int i5 = i4 + 1;
                    char c4 = this.instruction[i4];
                    if (c3 == c4) {
                        printWriter.print(charToString(c3));
                    } else {
                        printWriter.print(charToString(c3) + LanguageTag.SEP + charToString(c4));
                    }
                    i3++;
                    i2 = i5;
                }
                printWriter.print("]");
                i = i2;
            }
            if (c == 'A') {
                printWriter.print(", \"");
                while (true) {
                    ?? r2 = c2 - 1;
                    if (c2 == 0) {
                        break;
                    }
                    printWriter.print(charToString(this.instruction[i]));
                    c2 = r2;
                    i++;
                }
                printWriter.print("\"");
            }
            printWriter.println("");
        }
    }
}

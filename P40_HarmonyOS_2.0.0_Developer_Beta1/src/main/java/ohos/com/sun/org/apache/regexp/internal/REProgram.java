package ohos.com.sun.org.apache.regexp.internal;

import java.io.Serializable;

public class REProgram implements Serializable {
    static final int OPT_HASBACKREFS = 1;
    int flags;
    char[] instruction;
    int lenInstruction;
    int maxParens;
    char[] prefix;

    public REProgram(char[] cArr) {
        this(cArr, cArr.length);
    }

    public REProgram(int i, char[] cArr) {
        this(cArr, cArr.length);
        this.maxParens = i;
    }

    public REProgram(char[] cArr, int i) {
        this.maxParens = -1;
        setInstructions(cArr, i);
    }

    public char[] getInstructions() {
        int i = this.lenInstruction;
        if (i == 0) {
            return null;
        }
        char[] cArr = new char[i];
        System.arraycopy(this.instruction, 0, cArr, 0, i);
        return cArr;
    }

    public void setInstructions(char[] cArr, int i) {
        int i2;
        this.instruction = cArr;
        this.lenInstruction = i;
        int i3 = 0;
        this.flags = 0;
        this.prefix = null;
        if (cArr != null && i != 0) {
            if (i >= 3 && cArr[0] == '|' && cArr[cArr[2] + 0] == 'E' && i >= 6 && cArr[3] == 'A') {
                char c = cArr[4];
                this.prefix = new char[c];
                System.arraycopy(cArr, 6, this.prefix, 0, c);
            }
            while (i3 < i) {
                char c2 = cArr[i3 + 0];
                if (c2 != '#') {
                    if (c2 == 'A') {
                        i2 = cArr[i3 + 1];
                    } else if (c2 != '[') {
                        i3 += 3;
                    } else {
                        i2 = cArr[i3 + 1] * 2;
                    }
                    i3 += i2 == 1 ? 1 : 0;
                    i3 += 3;
                } else {
                    this.flags |= 1;
                    return;
                }
            }
        }
    }
}

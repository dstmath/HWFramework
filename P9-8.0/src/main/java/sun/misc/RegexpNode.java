package sun.misc;

import java.io.PrintStream;

/* compiled from: RegexpPool */
class RegexpNode {
    char c;
    int depth;
    boolean exact;
    RegexpNode firstchild;
    RegexpNode nextsibling;
    String re;
    Object result;

    RegexpNode() {
        this.re = null;
        this.c = '#';
        this.depth = 0;
    }

    RegexpNode(char C, int depth) {
        this.re = null;
        this.c = C;
        this.depth = depth;
    }

    RegexpNode add(char C) {
        RegexpNode p = this.firstchild;
        if (p == null) {
            p = new RegexpNode(C, this.depth + 1);
        } else {
            while (p != null) {
                if (p.c == C) {
                    return p;
                }
                p = p.nextsibling;
            }
            p = new RegexpNode(C, this.depth + 1);
            p.nextsibling = this.firstchild;
        }
        this.firstchild = p;
        return p;
    }

    RegexpNode find(char C) {
        for (RegexpNode p = this.firstchild; p != null; p = p.nextsibling) {
            if (p.c == C) {
                return p;
            }
        }
        return null;
    }

    void print(PrintStream out) {
        if (this.nextsibling != null) {
            RegexpNode p = this;
            out.print("(");
            while (p != null) {
                out.write(p.c);
                if (p.firstchild != null) {
                    p.firstchild.print(out);
                }
                p = p.nextsibling;
                out.write(p != null ? 124 : 41);
            }
            return;
        }
        out.write(this.c);
        if (this.firstchild != null) {
            this.firstchild.print(out);
        }
    }
}

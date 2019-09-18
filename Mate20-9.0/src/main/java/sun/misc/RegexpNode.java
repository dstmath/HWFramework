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

    RegexpNode(char C, int depth2) {
        this.re = null;
        this.c = C;
        this.depth = depth2;
    }

    /* access modifiers changed from: package-private */
    public RegexpNode add(char C) {
        RegexpNode p;
        RegexpNode p2 = this.firstchild;
        if (p2 == null) {
            p = new RegexpNode(C, this.depth + 1);
        } else {
            while (p2 != null) {
                if (p2.c == C) {
                    return p2;
                }
                p2 = p2.nextsibling;
            }
            p = new RegexpNode(C, this.depth + 1);
            p.nextsibling = this.firstchild;
        }
        this.firstchild = p;
        return p;
    }

    /* access modifiers changed from: package-private */
    public RegexpNode find(char C) {
        for (RegexpNode p = this.firstchild; p != null; p = p.nextsibling) {
            if (p.c == C) {
                return p;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void print(PrintStream out) {
        if (this.nextsibling != null) {
            RegexpNode p = this;
            out.print("(");
            while (p != null) {
                out.write((int) p.c);
                if (p.firstchild != null) {
                    p.firstchild.print(out);
                }
                p = p.nextsibling;
                out.write(p != null ? 124 : 41);
            }
            return;
        }
        out.write((int) this.c);
        if (this.firstchild != null) {
            this.firstchild.print(out);
        }
    }
}

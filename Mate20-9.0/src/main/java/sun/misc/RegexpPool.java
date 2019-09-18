package sun.misc;

import java.io.PrintStream;

public class RegexpPool {
    private static final int BIG = Integer.MAX_VALUE;
    private int lastDepth = Integer.MAX_VALUE;
    private RegexpNode prefixMachine = new RegexpNode();
    private RegexpNode suffixMachine = new RegexpNode();

    public void add(String re, Object ret) throws REException {
        add(re, ret, false);
    }

    public void replace(String re, Object ret) {
        try {
            add(re, ret, true);
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x006f  */
    public Object delete(String re) {
        Object o = null;
        RegexpNode p = this.prefixMachine;
        RegexpNode best = p;
        int len = re.length() - 1;
        boolean prefix = true;
        if (!re.startsWith("*") || !re.endsWith("*")) {
            len++;
        }
        if (len <= 0) {
            return null;
        }
        int i = 0;
        while (p != null) {
            if (p.result != null && p.depth < Integer.MAX_VALUE && (!p.exact || i == len)) {
                best = p;
            }
            if (i >= len) {
                break;
            }
            p = p.find(re.charAt(i));
            i++;
        }
        RegexpNode p2 = this.suffixMachine;
        int i2 = len;
        while (true) {
            i2--;
            if (i2 >= 0 && p2 != null) {
                if (p2.result != null && p2.depth < Integer.MAX_VALUE) {
                    prefix = false;
                    best = p2;
                }
                p2 = p2.find(re.charAt(i2));
            } else if (!prefix) {
                if (re.equals(best.re)) {
                    o = best.result;
                    best.result = null;
                }
            } else if (re.equals(best.re)) {
                o = best.result;
                best.result = null;
            }
        }
        if (!prefix) {
        }
        return o;
    }

    public Object match(String s) {
        return matchAfter(s, Integer.MAX_VALUE);
    }

    public Object matchNext(String s) {
        return matchAfter(s, this.lastDepth);
    }

    private void add(String re, Object ret, boolean replace) throws REException {
        RegexpNode p;
        int len = re.length();
        if (re.charAt(0) == '*') {
            p = this.suffixMachine;
            while (len > 1) {
                len--;
                p = p.add(re.charAt(len));
            }
        } else {
            boolean exact = false;
            if (re.charAt(len - 1) == '*') {
                len--;
            } else {
                exact = true;
            }
            RegexpNode p2 = this.prefixMachine;
            for (int i = 0; i < len; i++) {
                p2 = p2.add(re.charAt(i));
            }
            p2.exact = exact;
            p = p2;
        }
        if (p.result == null || replace) {
            p.re = re;
            p.result = ret;
            return;
        }
        throw new REException(re + " is a duplicate");
    }

    private Object matchAfter(String s, int lastMatchDepth) {
        RegexpNode p = this.prefixMachine;
        RegexpNode best = p;
        int bst = 0;
        int bend = 0;
        int len = s.length();
        if (len <= 0) {
            return null;
        }
        int i = 0;
        while (p != null) {
            if (p.result != null && p.depth < lastMatchDepth && (!p.exact || i == len)) {
                this.lastDepth = p.depth;
                best = p;
                bst = i;
                bend = len;
            }
            if (i >= len) {
                break;
            }
            p = p.find(s.charAt(i));
            i++;
        }
        RegexpNode p2 = this.suffixMachine;
        int i2 = len;
        while (true) {
            i2--;
            if (i2 < 0 || p2 == null) {
                Object o = best.result;
            } else {
                if (p2.result != null && p2.depth < lastMatchDepth) {
                    this.lastDepth = p2.depth;
                    best = p2;
                    bst = 0;
                    bend = i2 + 1;
                }
                p2 = p2.find(s.charAt(i2));
            }
        }
        Object o2 = best.result;
        if (o2 != null && (o2 instanceof RegexpTarget)) {
            o2 = ((RegexpTarget) o2).found(s.substring(bst, bend));
        }
        return o2;
    }

    public void reset() {
        this.lastDepth = Integer.MAX_VALUE;
    }

    public void print(PrintStream out) {
        out.print("Regexp pool:\n");
        if (this.suffixMachine.firstchild != null) {
            out.print(" Suffix machine: ");
            this.suffixMachine.firstchild.print(out);
            out.print("\n");
        }
        if (this.prefixMachine.firstchild != null) {
            out.print(" Prefix machine: ");
            this.prefixMachine.firstchild.print(out);
            out.print("\n");
        }
    }
}

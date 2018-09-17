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

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0065  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Object delete(String re) {
        Object o = null;
        RegexpNode p = this.prefixMachine;
        RegexpNode best = p;
        int len = re.length() - 1;
        boolean prefix = true;
        if (!(re.startsWith("*") && (re.endsWith("*") ^ 1) == 0)) {
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
        p = this.suffixMachine;
        i = len;
        while (true) {
            i--;
            if (i >= 0 && p != null) {
                if (p.result != null && p.depth < Integer.MAX_VALUE) {
                    prefix = false;
                    best = p;
                }
                p = p.find(re.charAt(i));
            } else if (prefix) {
                if (re.equals(best.re)) {
                    o = best.result;
                    best.result = null;
                }
            } else if (re.equals(best.re)) {
                o = best.result;
                best.result = null;
            }
        }
        if (prefix) {
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
            p = this.prefixMachine;
            for (int i = 0; i < len; i++) {
                p = p.add(re.charAt(i));
            }
            p.exact = exact;
        }
        if (p.result == null || (replace ^ 1) == 0) {
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
        Object o;
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
        p = this.suffixMachine;
        i = len;
        while (true) {
            i--;
            if (i < 0 || p == null) {
                o = best.result;
            } else {
                if (p.result != null && p.depth < lastMatchDepth) {
                    this.lastDepth = p.depth;
                    best = p;
                    bst = 0;
                    bend = i + 1;
                }
                p = p.find(s.charAt(i));
            }
        }
        o = best.result;
        if (o != null && (o instanceof RegexpTarget)) {
            o = ((RegexpTarget) o).found(s.substring(bst, bend));
        }
        return o;
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

package sun.net.www;

import java.lang.reflect.Array;
import java.util.Iterator;

public class HeaderParser {
    int asize;
    int nkeys;
    String raw;
    String[][] tab;

    class ParserIterator implements Iterator {
        int index;
        boolean returnsValue;

        ParserIterator(boolean returnValue) {
            this.returnsValue = returnValue;
        }

        public boolean hasNext() {
            return this.index < HeaderParser.this.nkeys;
        }

        public Object next() {
            String[][] strArr = HeaderParser.this.tab;
            int i = this.index;
            this.index = i + 1;
            return strArr[i][this.returnsValue ? 1 : 0];
        }

        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }

    public HeaderParser(String raw) {
        this.asize = 10;
        this.raw = raw;
        this.tab = (String[][]) Array.newInstance(String.class, this.asize, 2);
        parse();
    }

    private HeaderParser() {
        this.asize = 10;
    }

    public HeaderParser subsequence(int start, int end) {
        if (start == 0 && end == this.nkeys) {
            return this;
        }
        if (start < 0 || start >= end || end > this.nkeys) {
            throw new IllegalArgumentException("invalid start or end");
        }
        HeaderParser n = new HeaderParser();
        n.tab = (String[][]) Array.newInstance(String.class, this.asize, 2);
        n.asize = this.asize;
        System.arraycopy(this.tab, start, n.tab, 0, end - start);
        n.nkeys = end - start;
        return n;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parse() {
        if (this.raw != null) {
            int i;
            this.raw = this.raw.trim();
            char[] ca = this.raw.toCharArray();
            int beg = 0;
            int end = 0;
            boolean inKey = true;
            boolean inQuote = false;
            int len = ca.length;
            int i2 = 0;
            while (end < len) {
                char c = ca[end];
                if (c == '=' && !inQuote) {
                    this.tab[i2][0] = new String(ca, beg, end - beg).toLowerCase();
                    inKey = false;
                    end++;
                    beg = end;
                    i = i2;
                } else if (c == '\"') {
                    if (inQuote) {
                        i = i2 + 1;
                        this.tab[i2][1] = new String(ca, beg, end - beg);
                        inQuote = false;
                        while (true) {
                            end++;
                            if (end >= len || !(ca[end] == ' ' || ca[end] == ',')) {
                                inKey = true;
                                beg = end;
                            }
                        }
                        inKey = true;
                        beg = end;
                    } else {
                        inQuote = true;
                        end++;
                        beg = end;
                        i = i2;
                    }
                } else if (c != ' ' && c != ',') {
                    end++;
                    i = i2;
                } else if (inQuote) {
                    end++;
                } else {
                    if (inKey) {
                        i = i2 + 1;
                        this.tab[i2][0] = new String(ca, beg, end - beg).toLowerCase();
                    } else {
                        i = i2 + 1;
                        this.tab[i2][1] = new String(ca, beg, end - beg);
                    }
                    while (end < len && (ca[end] == ' ' || ca[end] == ',')) {
                        end++;
                    }
                    inKey = true;
                    beg = end;
                }
                if (i == this.asize) {
                    this.asize *= 2;
                    Object ntab = (String[][]) Array.newInstance(String.class, this.asize, 2);
                    System.arraycopy(this.tab, 0, ntab, 0, this.tab.length);
                    this.tab = ntab;
                }
                i2 = i;
            }
            end--;
            if (end > beg) {
                if (inKey) {
                    i = i2 + 1;
                    this.tab[i2][0] = new String(ca, beg, (end - beg) + 1).toLowerCase();
                } else if (ca[end] == '\"') {
                    i = i2 + 1;
                    this.tab[i2][1] = new String(ca, beg, end - beg);
                } else {
                    i = i2 + 1;
                    this.tab[i2][1] = new String(ca, beg, (end - beg) + 1);
                }
            } else if (end != beg) {
                i = i2;
            } else if (inKey) {
                i = i2 + 1;
                this.tab[i2][0] = String.valueOf(ca[end]).toLowerCase();
            } else if (ca[end] == '\"') {
                i = i2 + 1;
                this.tab[i2][1] = String.valueOf(ca[end - 1]);
            } else {
                i = i2 + 1;
                this.tab[i2][1] = String.valueOf(ca[end]);
            }
            this.nkeys = i;
        }
    }

    public String findKey(int i) {
        if (i < 0 || i > this.asize) {
            return null;
        }
        return this.tab[i][0];
    }

    public String findValue(int i) {
        if (i < 0 || i > this.asize) {
            return null;
        }
        return this.tab[i][1];
    }

    public String findValue(String key) {
        return findValue(key, null);
    }

    public String findValue(String k, String Default) {
        if (k == null) {
            return Default;
        }
        k = k.toLowerCase();
        int i = 0;
        while (i < this.asize && this.tab[i][0] != null) {
            if (k.equals(this.tab[i][0])) {
                return this.tab[i][1];
            }
            i++;
        }
        return Default;
    }

    public Iterator keys() {
        return new ParserIterator(false);
    }

    public Iterator values() {
        return new ParserIterator(true);
    }

    public String toString() {
        Iterator k = keys();
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("{size=" + this.asize + " nkeys=" + this.nkeys + " ");
        int i = 0;
        while (k.hasNext()) {
            String key = (String) k.next();
            String val = findValue(i);
            if (val != null && "".equals(val)) {
                val = null;
            }
            sbuf.append(" {" + key + (val == null ? "" : "," + val) + "}");
            if (k.hasNext()) {
                sbuf.append(",");
            }
            i++;
        }
        sbuf.append(" }");
        return new String(sbuf);
    }

    public int findInt(String k, int Default) {
        try {
            return Integer.parseInt(findValue(k, String.valueOf(Default)));
        } catch (Throwable th) {
            return Default;
        }
    }
}

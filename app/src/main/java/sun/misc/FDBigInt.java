package sun.misc;

public class FDBigInt {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    int[] data;
    int nWords;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.FDBigInt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.FDBigInt.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.FDBigInt.<clinit>():void");
    }

    public FDBigInt(int v) {
        this.nWords = 1;
        this.data = new int[1];
        this.data[0] = v;
    }

    public FDBigInt(long v) {
        int i = 1;
        this.data = new int[2];
        this.data[0] = (int) v;
        this.data[1] = (int) (v >>> 32);
        if (this.data[1] != 0) {
            i = 2;
        }
        this.nWords = i;
    }

    public FDBigInt(FDBigInt other) {
        int i = other.nWords;
        this.nWords = i;
        this.data = new int[i];
        System.arraycopy(other.data, 0, this.data, 0, this.nWords);
    }

    private FDBigInt(int[] d, int n) {
        this.data = d;
        this.nWords = n;
    }

    public FDBigInt(long seed, char[] digit, int nd0, int nd) {
        int v;
        int n = (nd + 8) / 9;
        if (n < 2) {
            n = 2;
        }
        this.data = new int[n];
        this.data[0] = (int) seed;
        this.data[1] = (int) (seed >>> 32);
        this.nWords = this.data[1] == 0 ? 1 : 2;
        int limit = nd - 5;
        int i = nd0;
        while (i < limit) {
            int ilim = i + 5;
            v = digit[i] - 48;
            i++;
            while (i < ilim) {
                v = ((v * 10) + digit[i]) - 48;
                i++;
            }
            multaddMe(100000, v);
        }
        int factor = 1;
        v = 0;
        while (i < nd) {
            v = ((v * 10) + digit[i]) - 48;
            factor *= 10;
            i++;
        }
        if (factor != 1) {
            multaddMe(factor, v);
        }
    }

    public void lshiftMe(int c) throws IllegalArgumentException {
        if (c > 0) {
            int target;
            int wordcount = c >> 5;
            int bitcount = c & 31;
            int anticount = 32 - bitcount;
            int[] t = this.data;
            int[] s = this.data;
            if ((this.nWords + wordcount) + 1 > t.length) {
                t = new int[((this.nWords + wordcount) + 1)];
            }
            int i = this.nWords + wordcount;
            int src = this.nWords - 1;
            if (bitcount == 0) {
                System.arraycopy(s, 0, t, wordcount, this.nWords);
                target = wordcount - 1;
            } else {
                target = i - 1;
                t[i] = s[src] >>> anticount;
                while (src >= 1) {
                    i = target - 1;
                    src--;
                    t[target] = (s[src] << bitcount) | (s[src] >>> anticount);
                    target = i;
                }
                i = target - 1;
                t[target] = s[src] << bitcount;
                target = i;
            }
            while (target >= 0) {
                i = target - 1;
                t[target] = 0;
                target = i;
            }
            this.data = t;
            this.nWords += wordcount + 1;
            while (this.nWords > 1 && this.data[this.nWords - 1] == 0) {
                this.nWords--;
            }
        } else if (c != 0) {
            throw new IllegalArgumentException("negative shift count");
        }
    }

    public int normalizeMe() throws IllegalArgumentException {
        int wordcount = 0;
        int bitcount = 0;
        int v = 0;
        int src = this.nWords - 1;
        while (src >= 0) {
            v = this.data[src];
            if (v != 0) {
                break;
            }
            wordcount++;
            src--;
        }
        if (src < 0) {
            throw new IllegalArgumentException("zero value");
        }
        this.nWords -= wordcount;
        if ((v & -268435456) != 0) {
            bitcount = 32;
            while ((v & -268435456) != 0) {
                v >>>= 1;
                bitcount--;
            }
        } else {
            while (v <= 1048575) {
                v <<= 8;
                bitcount += 8;
            }
            while (v <= 134217727) {
                v <<= 1;
                bitcount++;
            }
        }
        if (bitcount != 0) {
            lshiftMe(bitcount);
        }
        return bitcount;
    }

    public FDBigInt mult(int iv) {
        long v = (long) iv;
        int[] r = new int[((((long) this.data[this.nWords + -1]) & 4294967295L) * v > 268435455 ? this.nWords + 1 : this.nWords)];
        long p = 0;
        for (int i = 0; i < this.nWords; i++) {
            p += (((long) this.data[i]) & 4294967295L) * v;
            r[i] = (int) p;
            p >>>= 32;
        }
        if (p == 0) {
            return new FDBigInt(r, this.nWords);
        }
        r[this.nWords] = (int) p;
        return new FDBigInt(r, this.nWords + 1);
    }

    public void multaddMe(int iv, int addend) {
        long v = (long) iv;
        long p = ((((long) this.data[0]) & 4294967295L) * v) + (((long) addend) & 4294967295L);
        this.data[0] = (int) p;
        p >>>= 32;
        for (int i = 1; i < this.nWords; i++) {
            p += (((long) this.data[i]) & 4294967295L) * v;
            this.data[i] = (int) p;
            p >>>= 32;
        }
        if (p != 0) {
            this.data[this.nWords] = (int) p;
            this.nWords++;
        }
    }

    public FDBigInt mult(FDBigInt other) {
        int i;
        int[] r = new int[(this.nWords + other.nWords)];
        for (i = 0; i < this.nWords; i++) {
            long v = ((long) this.data[i]) & 4294967295L;
            long p = 0;
            int j = 0;
            while (j < other.nWords) {
                p += (((long) r[i + j]) & 4294967295L) + ((((long) other.data[j]) & 4294967295L) * v);
                r[i + j] = (int) p;
                p >>>= 32;
                j++;
            }
            r[i + j] = (int) p;
        }
        i = r.length - 1;
        while (i > 0 && r[i] == 0) {
            i--;
        }
        return new FDBigInt(r, i + 1);
    }

    public FDBigInt add(FDBigInt other) {
        int[] a;
        int n;
        int[] b;
        int m;
        long c = 0;
        if (this.nWords >= other.nWords) {
            a = this.data;
            n = this.nWords;
            b = other.data;
            m = other.nWords;
        } else {
            a = other.data;
            n = other.nWords;
            b = this.data;
            m = this.nWords;
        }
        int[] r = new int[n];
        int i = 0;
        while (i < n) {
            c += ((long) a[i]) & 4294967295L;
            if (i < m) {
                c += ((long) b[i]) & 4294967295L;
            }
            r[i] = (int) c;
            c >>= 32;
            i++;
        }
        if (c == 0) {
            return new FDBigInt(r, i);
        }
        int[] s = new int[(r.length + 1)];
        System.arraycopy(r, 0, s, 0, r.length);
        int i2 = i + 1;
        s[i] = (int) c;
        return new FDBigInt(s, i2);
    }

    public FDBigInt sub(FDBigInt other) {
        Object obj = null;
        int[] r = new int[this.nWords];
        int n = this.nWords;
        int m = other.nWords;
        int nzeros = 0;
        long c = 0;
        int i = 0;
        while (i < n) {
            c += ((long) this.data[i]) & 4294967295L;
            if (i < m) {
                c -= ((long) other.data[i]) & 4294967295L;
            }
            int i2 = (int) c;
            r[i] = i2;
            if (i2 == 0) {
                nzeros++;
            } else {
                nzeros = 0;
            }
            c >>= 32;
            i++;
        }
        if (!-assertionsDisabled) {
            if (c == 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError(Long.valueOf(c));
            }
        }
        if (-assertionsDisabled || dataInRangeIsZero(i, m, other)) {
            return new FDBigInt(r, n - nzeros);
        }
        throw new AssertionError();
    }

    private static boolean dataInRangeIsZero(int i, int m, FDBigInt other) {
        int i2 = i;
        while (i2 < m) {
            i = i2 + 1;
            if (other.data[i2] != 0) {
                return false;
            }
            i2 = i;
        }
        return true;
    }

    public int cmp(FDBigInt other) {
        int i;
        int j;
        if (this.nWords > other.nWords) {
            j = other.nWords - 1;
            i = this.nWords - 1;
            while (i > j) {
                if (this.data[i] != 0) {
                    return 1;
                }
                i--;
            }
        } else if (this.nWords < other.nWords) {
            j = this.nWords - 1;
            i = other.nWords - 1;
            while (i > j) {
                if (other.data[i] != 0) {
                    return -1;
                }
                i--;
            }
        } else {
            i = this.nWords - 1;
        }
        while (i > 0 && this.data[i] == other.data[i]) {
            i--;
        }
        int a = this.data[i];
        int b = other.data[i];
        if (a < 0) {
            if (b < 0) {
                return a - b;
            }
            return 1;
        } else if (b < 0) {
            return -1;
        } else {
            return a - b;
        }
    }

    public int quoRemIteration(FDBigInt S) throws IllegalArgumentException {
        if (this.nWords != S.nWords) {
            throw new IllegalArgumentException("disparate values");
        }
        int i;
        int n = this.nWords - 1;
        long q = (((long) this.data[n]) & 4294967295L) / ((long) S.data[n]);
        long diff = 0;
        for (i = 0; i <= n; i++) {
            diff += (((long) this.data[i]) & 4294967295L) - ((((long) S.data[i]) & 4294967295L) * q);
            this.data[i] = (int) diff;
            diff >>= 32;
        }
        if (diff != 0) {
            long sum = 0;
            while (sum == 0) {
                sum = 0;
                for (i = 0; i <= n; i++) {
                    sum += (((long) this.data[i]) & 4294967295L) + (((long) S.data[i]) & 4294967295L);
                    this.data[i] = (int) sum;
                    sum >>= 32;
                }
                if (!-assertionsDisabled) {
                    Object obj = (sum == 0 || sum == 1) ? 1 : null;
                    if (obj == null) {
                        throw new AssertionError(Long.valueOf(sum));
                    }
                }
                q--;
            }
        }
        long p = 0;
        for (i = 0; i <= n; i++) {
            p += (((long) this.data[i]) & 4294967295L) * 10;
            this.data[i] = (int) p;
            p >>= 32;
        }
        if (!-assertionsDisabled) {
            if ((p == 0 ? 1 : null) == null) {
                throw new AssertionError(Long.valueOf(p));
            }
        }
        return (int) q;
    }

    public long longValue() {
        if (!-assertionsDisabled) {
            if ((this.nWords > 0 ? 1 : 0) == 0) {
                throw new AssertionError(Integer.valueOf(this.nWords));
            }
        }
        if (this.nWords == 1) {
            return ((long) this.data[0]) & 4294967295L;
        }
        if (-assertionsDisabled || dataInRangeIsZero(2, this.nWords, this)) {
            if (!-assertionsDisabled) {
                if ((this.data[1] >= 0 ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            return (((long) this.data[1]) << 32) | (((long) this.data[0]) & 4294967295L);
        }
        throw new AssertionError();
    }

    public String toString() {
        StringBuffer r = new StringBuffer(30);
        r.append('[');
        int i = Math.min(this.nWords - 1, this.data.length - 1);
        if (this.nWords > this.data.length) {
            r.append("(" + this.data.length + "<" + this.nWords + "!)");
        }
        while (i > 0) {
            r.append(Integer.toHexString(this.data[i]));
            r.append(' ');
            i--;
        }
        r.append(Integer.toHexString(this.data[0]));
        r.append(']');
        return new String(r);
    }
}

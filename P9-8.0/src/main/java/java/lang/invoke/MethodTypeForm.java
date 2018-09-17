package java.lang.invoke;

import sun.invoke.util.Wrapper;

final class MethodTypeForm {
    static final /* synthetic */ boolean -assertionsDisabled = (MethodTypeForm.class.desiredAssertionStatus() ^ 1);
    public static final int ERASE = 1;
    public static final int INTS = 4;
    public static final int LONGS = 5;
    public static final int NO_CHANGE = 0;
    public static final int RAW_RETURN = 6;
    public static final int UNWRAP = 3;
    public static final int WRAP = 2;
    final long argCounts;
    final int[] argToSlotTable;
    final MethodType basicType;
    final MethodType erasedType;
    final long primCounts;
    final int[] slotToArgTable;

    public MethodType erasedType() {
        return this.erasedType;
    }

    public MethodType basicType() {
        return this.basicType;
    }

    private boolean assertIsBasicType() {
        if (-assertionsDisabled || this.erasedType == this.basicType) {
            return true;
        }
        throw new AssertionError("erasedType: " + this.erasedType + " != basicType: " + this.basicType);
    }

    protected MethodTypeForm(MethodType erasedType) {
        int i;
        Wrapper w;
        this.erasedType = erasedType;
        Class<?>[] ptypes = erasedType.ptypes();
        int ptypeCount = ptypes.length;
        int pslotCount = ptypeCount;
        int rtypeCount = 1;
        int rslotCount = 1;
        int pac = 0;
        int lac = 0;
        int prc = 0;
        int lrc = 0;
        Class<?>[] epts = ptypes;
        Class<?>[] bpts = ptypes;
        for (i = 0; i < ptypes.length; i++) {
            Class<?> pt = ptypes[i];
            if (pt != Object.class) {
                pac++;
                w = Wrapper.forPrimitiveType(pt);
                if (w.isDoubleWord()) {
                    lac++;
                }
                if (w.isSubwordOrInt() && pt != Integer.TYPE) {
                    if (bpts == ptypes) {
                        bpts = (Class[]) bpts.clone();
                    }
                    bpts[i] = Integer.TYPE;
                }
            }
        }
        pslotCount = ptypeCount + lac;
        Class<?> rt = erasedType.returnType();
        Class<?> bt = rt;
        if (rt != Object.class) {
            prc = 1;
            w = Wrapper.forPrimitiveType(rt);
            if (w.isDoubleWord()) {
                lrc = 1;
            }
            if (w.isSubwordOrInt() && rt != Integer.TYPE) {
                bt = Integer.TYPE;
            }
            if (rt == Void.TYPE) {
                rslotCount = 0;
                rtypeCount = 0;
            } else {
                rslotCount = lrc + 1;
            }
        }
        MethodTypeForm that;
        if (ptypes == bpts && bt == rt) {
            int[] slotToArgTab;
            int[] argToSlotTab;
            this.basicType = erasedType;
            int slot;
            if (lac != 0) {
                slot = ptypeCount + lac;
                slotToArgTab = new int[(slot + 1)];
                argToSlotTab = new int[(ptypeCount + 1)];
                argToSlotTab[0] = slot;
                for (i = 0; i < ptypes.length; i++) {
                    if (Wrapper.forBasicType(ptypes[i]).isDoubleWord()) {
                        slot--;
                    }
                    slot--;
                    slotToArgTab[slot] = i + 1;
                    argToSlotTab[i + 1] = slot;
                }
                if (!(-assertionsDisabled || slot == 0)) {
                    throw new AssertionError();
                }
            } else if (pac == 0) {
                slot = ptypeCount;
                slotToArgTab = new int[(ptypeCount + 1)];
                argToSlotTab = new int[(ptypeCount + 1)];
                argToSlotTab[0] = ptypeCount;
                for (i = 0; i < ptypeCount; i++) {
                    slot--;
                    slotToArgTab[slot] = i + 1;
                    argToSlotTab[i + 1] = slot;
                }
            } else if (-assertionsDisabled || ptypeCount == pslotCount) {
                that = MethodType.genericMethodType(ptypeCount).form();
                if (-assertionsDisabled || this != that) {
                    slotToArgTab = that.slotToArgTable;
                    argToSlotTab = that.argToSlotTable;
                } else {
                    throw new AssertionError();
                }
            } else {
                throw new AssertionError();
            }
            this.primCounts = pack(lrc, prc, lac, pac);
            this.argCounts = pack(rslotCount, rtypeCount, pslotCount, ptypeCount);
            this.argToSlotTable = argToSlotTab;
            this.slotToArgTable = slotToArgTab;
            if (pslotCount >= 256) {
                throw MethodHandleStatics.newIllegalArgumentException("too many arguments");
            } else if (!-assertionsDisabled && this.basicType != erasedType) {
                throw new AssertionError();
            } else {
                return;
            }
        }
        this.basicType = MethodType.makeImpl(bt, bpts, true);
        that = this.basicType.form();
        if (-assertionsDisabled || this != that) {
            this.primCounts = that.primCounts;
            this.argCounts = that.argCounts;
            this.argToSlotTable = that.argToSlotTable;
            this.slotToArgTable = that.slotToArgTable;
            return;
        }
        throw new AssertionError();
    }

    private static long pack(int a, int b, int c, int d) {
        if (-assertionsDisabled || ((((a | b) | c) | d) & -65536) == 0) {
            return (((long) ((a << 16) | b)) << 32) | ((long) ((c << 16) | d));
        }
        throw new AssertionError();
    }

    private static char unpack(long packed, int word) {
        if (-assertionsDisabled || word <= 3) {
            return (char) ((int) (packed >> ((3 - word) * 16)));
        }
        throw new AssertionError();
    }

    public int parameterCount() {
        return unpack(this.argCounts, 3);
    }

    public int parameterSlotCount() {
        return unpack(this.argCounts, 2);
    }

    public int returnCount() {
        return unpack(this.argCounts, 1);
    }

    public int returnSlotCount() {
        return unpack(this.argCounts, 0);
    }

    public int primitiveParameterCount() {
        return unpack(this.primCounts, 3);
    }

    public int longPrimitiveParameterCount() {
        return unpack(this.primCounts, 2);
    }

    public int primitiveReturnCount() {
        return unpack(this.primCounts, 1);
    }

    public int longPrimitiveReturnCount() {
        return unpack(this.primCounts, 0);
    }

    public boolean hasPrimitives() {
        return this.primCounts != 0 ? true : -assertionsDisabled;
    }

    public boolean hasNonVoidPrimitives() {
        boolean z = true;
        if (this.primCounts == 0) {
            return -assertionsDisabled;
        }
        if (primitiveParameterCount() != 0) {
            return true;
        }
        if (primitiveReturnCount() == 0 || returnCount() == 0) {
            z = -assertionsDisabled;
        }
        return z;
    }

    public boolean hasLongPrimitives() {
        return (longPrimitiveParameterCount() | longPrimitiveReturnCount()) != 0 ? true : -assertionsDisabled;
    }

    public int parameterToArgSlot(int i) {
        return this.argToSlotTable[i + 1];
    }

    public int argSlotToParameter(int argSlot) {
        return this.slotToArgTable[argSlot] - 1;
    }

    static MethodTypeForm findForm(MethodType mt) {
        MethodType erased = canonicalize(mt, 1, 1);
        if (erased == null) {
            return new MethodTypeForm(mt);
        }
        return erased.form();
    }

    public static MethodType canonicalize(MethodType mt, int howRet, int howArgs) {
        Class<?>[] ptypes = mt.ptypes();
        Class<?>[] ptc = canonicalizeAll(ptypes, howArgs);
        Class<?> rtype = mt.returnType();
        Class<?> rtc = canonicalize(rtype, howRet);
        if (ptc == null && rtc == null) {
            return null;
        }
        if (rtc == null) {
            rtc = rtype;
        }
        if (ptc == null) {
            ptc = ptypes;
        }
        return MethodType.makeImpl(rtc, ptc, true);
    }

    static Class<?> canonicalize(Class<?> t, int how) {
        if (t != Object.class) {
            if (!t.isPrimitive()) {
                switch (how) {
                    case 1:
                    case 6:
                        return Object.class;
                    case 3:
                        Class<?> ct = Wrapper.asPrimitiveType(t);
                        if (ct != t) {
                            return ct;
                        }
                        break;
                }
            } else if (t == Void.TYPE) {
                switch (how) {
                    case 2:
                        return Void.class;
                    case 6:
                        return Integer.TYPE;
                }
            } else {
                switch (how) {
                    case 2:
                        return Wrapper.asWrapperType(t);
                    case 4:
                        if (t == Integer.TYPE || t == Long.TYPE) {
                            return null;
                        }
                        if (t == Double.TYPE) {
                            return Long.TYPE;
                        }
                        return Integer.TYPE;
                    case 5:
                        if (t == Long.TYPE) {
                            return null;
                        }
                        return Long.TYPE;
                    case 6:
                        if (t == Integer.TYPE || t == Long.TYPE || t == Float.TYPE || t == Double.TYPE) {
                            return null;
                        }
                        return Integer.TYPE;
                }
            }
        }
        return null;
    }

    static Class<?>[] canonicalizeAll(Class<?>[] ts, int how) {
        Class<?>[] cs = null;
        int imax = ts.length;
        for (int i = 0; i < imax; i++) {
            Class<?> c = canonicalize(ts[i], how);
            if (c == Void.TYPE) {
                c = null;
            }
            if (c != null) {
                if (cs == null) {
                    cs = (Class[]) ts.clone();
                }
                cs[i] = c;
            }
        }
        return cs;
    }

    public String toString() {
        return "Form" + this.erasedType;
    }
}

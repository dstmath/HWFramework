package java.lang.invoke;

import sun.invoke.util.Wrapper;

final class MethodTypeForm {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v16, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v4, resolved type: java.lang.Class<java.lang.Integer>[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0094  */
    protected MethodTypeForm(MethodType erasedType2) {
        int rslotCount;
        int rtypeCount;
        int[] slotToArgTab;
        int[] argToSlotTab;
        Class cls;
        int rslotCount2;
        Class<?> bt;
        MethodType methodType = erasedType2;
        this.erasedType = methodType;
        Class<?>[] ptypes = erasedType2.ptypes();
        int ptypeCount = ptypes.length;
        int pslotCount = ptypeCount;
        int rtypeCount2 = 1;
        int lac = 0;
        int prc = 0;
        int lrc = 0;
        Class<?>[] epts = ptypes;
        Class<?>[] bpts = epts;
        int pac = 0;
        int i = 0;
        while (true) {
            Class<?>[] ptypes2 = ptypes;
            if (i >= epts.length) {
                break;
            }
            Class<?> pt = epts[i];
            int rtypeCount3 = rtypeCount2;
            if (pt != Object.class) {
                pac++;
                Wrapper w = Wrapper.forPrimitiveType(pt);
                lac = w.isDoubleWord() ? lac + 1 : lac;
                if (w.isSubwordOrInt()) {
                    Wrapper wrapper = w;
                    if (pt != Integer.TYPE) {
                        bpts = bpts == epts ? (Class[]) bpts.clone() : bpts;
                        bpts[i] = Integer.TYPE;
                    }
                }
            }
            i++;
            ptypes = ptypes2;
            rtypeCount2 = rtypeCount3;
        }
        int rtypeCount4 = rtypeCount2;
        int pslotCount2 = pslotCount + lac;
        Class<?> rt = erasedType2.returnType();
        Class<?> bt2 = rt;
        if (rt != Object.class) {
            prc = 0 + 1;
            Wrapper w2 = Wrapper.forPrimitiveType(rt);
            lrc = w2.isDoubleWord() ? 0 + 1 : lrc;
            if (w2.isSubwordOrInt()) {
                bt = bt2;
                if (rt != Integer.TYPE) {
                    cls = Integer.TYPE;
                    Class cls2 = cls;
                    if (rt != Void.TYPE) {
                        rslotCount2 = 0;
                        rtypeCount4 = 0;
                    } else {
                        rslotCount2 = 1 + lrc;
                    }
                    rslotCount = rslotCount2;
                    rtypeCount = rtypeCount4;
                    bt2 = cls2;
                }
            } else {
                bt = bt2;
            }
            cls = bt;
            Class cls22 = cls;
            if (rt != Void.TYPE) {
            }
            rslotCount = rslotCount2;
            rtypeCount = rtypeCount4;
            bt2 = cls22;
        } else {
            Class<?> cls3 = bt2;
            rslotCount = 1;
            rtypeCount = rtypeCount4;
        }
        if (epts == bpts && bt2 == rt) {
            this.basicType = methodType;
            if (lac != 0) {
                int slot = ptypeCount + lac;
                slotToArgTab = new int[(slot + 1)];
                argToSlotTab = new int[(1 + ptypeCount)];
                argToSlotTab[0] = slot;
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    Class<?> rt2 = rt;
                    if (i3 >= epts.length) {
                        break;
                    }
                    Class<?>[] epts2 = epts;
                    if (Wrapper.forBasicType(epts[i3]).isDoubleWord()) {
                        slot--;
                    }
                    slot--;
                    slotToArgTab[slot] = i3 + 1;
                    argToSlotTab[1 + i3] = slot;
                    i2 = i3 + 1;
                    rt = rt2;
                    epts = epts2;
                }
            } else {
                Class<?>[] clsArr = epts;
                if (pac != 0) {
                    MethodTypeForm that = MethodType.genericMethodType(ptypeCount).form();
                    slotToArgTab = that.slotToArgTable;
                    argToSlotTab = that.argToSlotTable;
                } else {
                    int slot2 = ptypeCount;
                    slotToArgTab = new int[(slot2 + 1)];
                    int[] argToSlotTab2 = new int[(1 + ptypeCount)];
                    argToSlotTab2[0] = slot2;
                    for (int i4 = 0; i4 < ptypeCount; i4++) {
                        slot2--;
                        slotToArgTab[slot2] = i4 + 1;
                        argToSlotTab2[1 + i4] = slot2;
                    }
                    argToSlotTab = argToSlotTab2;
                }
            }
            this.primCounts = pack(lrc, prc, lac, pac);
            this.argCounts = pack(rslotCount, rtypeCount, pslotCount2, ptypeCount);
            this.argToSlotTable = argToSlotTab;
            this.slotToArgTable = slotToArgTab;
            if (pslotCount2 >= 256) {
                throw MethodHandleStatics.newIllegalArgumentException("too many arguments");
            }
            return;
        }
        Class<?>[] clsArr2 = epts;
        this.basicType = MethodType.makeImpl(bt2, bpts, true);
        MethodTypeForm that2 = this.basicType.form();
        int i5 = ptypeCount;
        this.primCounts = that2.primCounts;
        this.argCounts = that2.argCounts;
        this.argToSlotTable = that2.argToSlotTable;
        this.slotToArgTable = that2.slotToArgTable;
    }

    private static long pack(int a, int b, int c, int d) {
        return (((long) ((a << 16) | b)) << 32) | ((long) ((c << 16) | d));
    }

    private static char unpack(long packed, int word) {
        return (char) ((int) (packed >> ((3 - word) * 16)));
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
        if (this.primCounts != 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    public boolean hasNonVoidPrimitives() {
        int i = (this.primCounts > 0 ? 1 : (this.primCounts == 0 ? 0 : -1));
        boolean z = $assertionsDisabled;
        if (i == 0) {
            return $assertionsDisabled;
        }
        if (primitiveParameterCount() != 0) {
            return true;
        }
        if (!(primitiveReturnCount() == 0 || returnCount() == 0)) {
            z = true;
        }
        return z;
    }

    public boolean hasLongPrimitives() {
        if ((longPrimitiveParameterCount() | longPrimitiveReturnCount()) != 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    public int parameterToArgSlot(int i) {
        return this.argToSlotTable[1 + i];
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

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0013, code lost:
        if (r5 != 6) goto L_0x0035;
     */
    static Class<?> canonicalize(Class<?> t, int how) {
        if (t != Object.class) {
            if (!t.isPrimitive()) {
                if (how != 1) {
                    if (how == 3) {
                        Class<?> ct = Wrapper.asPrimitiveType(t);
                        if (ct != t) {
                            return ct;
                        }
                    }
                }
                return Object.class;
            } else if (t == Void.TYPE) {
                if (how == 2) {
                    return Void.class;
                }
                if (how == 6) {
                    return Integer.TYPE;
                }
            } else if (how == 2) {
                return Wrapper.asWrapperType(t);
            } else {
                switch (how) {
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: java.lang.Class<?>[]} */
    /* JADX WARNING: Multi-variable type inference failed */
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

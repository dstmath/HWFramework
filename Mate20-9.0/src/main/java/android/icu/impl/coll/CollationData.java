package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2_32;
import android.icu.text.UnicodeSet;

public final class CollationData {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    static final int JAMO_CE32S_LENGTH = 67;
    static final int MAX_NUM_SPECIAL_REORDER_CODES = 8;
    static final int REORDER_RESERVED_AFTER_LATIN = 4111;
    static final int REORDER_RESERVED_BEFORE_LATIN = 4110;
    public CollationData base;
    int[] ce32s;
    long[] ces;
    public boolean[] compressibleBytes;
    String contexts;
    public char[] fastLatinTable;
    char[] fastLatinTableHeader;
    int[] jamoCE32s = new int[67];
    public Normalizer2Impl nfcImpl;
    int numScripts;
    long numericPrimary = 301989888;
    public long[] rootElements;
    char[] scriptStarts;
    char[] scriptsIndex;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;

    CollationData(Normalizer2Impl nfc) {
        this.nfcImpl = nfc;
    }

    public int getCE32(int c) {
        return this.trie.get(c);
    }

    /* access modifiers changed from: package-private */
    public int getCE32FromSupplementary(int c) {
        return this.trie.get(c);
    }

    /* access modifiers changed from: package-private */
    public boolean isDigit(int c) {
        if (c < 1632) {
            return c <= 57 && 48 <= c;
        }
        return Collation.hasCE32Tag(getCE32(c), 10);
    }

    public boolean isUnsafeBackward(int c, boolean numeric) {
        return this.unsafeBackwardSet.contains(c) || (numeric && isDigit(c));
    }

    public boolean isCompressibleLeadByte(int b) {
        return this.compressibleBytes[b];
    }

    public boolean isCompressiblePrimary(long p) {
        return isCompressibleLeadByte(((int) p) >>> 24);
    }

    /* access modifiers changed from: package-private */
    public int getCE32FromContexts(int index) {
        return (this.contexts.charAt(index) << 16) | this.contexts.charAt(index + 1);
    }

    /* access modifiers changed from: package-private */
    public int getIndirectCE32(int ce32) {
        int tag = Collation.tagFromCE32(ce32);
        if (tag == 10) {
            return this.ce32s[Collation.indexFromCE32(ce32)];
        }
        if (tag == 13) {
            return -1;
        }
        if (tag == 11) {
            return this.ce32s[0];
        }
        return ce32;
    }

    /* access modifiers changed from: package-private */
    public int getFinalCE32(int ce32) {
        if (Collation.isSpecialCE32(ce32)) {
            return getIndirectCE32(ce32);
        }
        return ce32;
    }

    /* access modifiers changed from: package-private */
    public long getCEFromOffsetCE32(int c, int ce32) {
        return Collation.makeCE(Collation.getThreeBytePrimaryForOffsetData(c, this.ces[Collation.indexFromCE32(ce32)]));
    }

    /* access modifiers changed from: package-private */
    public long getSingleCE(int c) {
        CollationData d;
        int ce32 = getCE32(c);
        if (ce32 == 192) {
            d = this.base;
            ce32 = this.base.getCE32(c);
        } else {
            d = this;
        }
        while (Collation.isSpecialCE32(ce32)) {
            switch (Collation.tagFromCE32(ce32)) {
                case 0:
                case 3:
                    throw new AssertionError(String.format("unexpected CE32 tag for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case 1:
                    return Collation.ceFromLongPrimaryCE32(ce32);
                case 2:
                    return Collation.ceFromLongSecondaryCE32(ce32);
                case 4:
                case 7:
                case 8:
                case 9:
                case 12:
                case 13:
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case 5:
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                        break;
                    } else {
                        throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                    }
                case 6:
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        return d.ces[Collation.indexFromCE32(ce32)];
                    }
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case 10:
                    ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                    break;
                case 11:
                    ce32 = d.ce32s[0];
                    break;
                case 14:
                    return d.getCEFromOffsetCE32(c, ce32);
                case 15:
                    return Collation.unassignedCEFromCodePoint(c);
            }
        }
        return Collation.ceFromSimpleCE32(ce32);
    }

    /* access modifiers changed from: package-private */
    public int getFCD16(int c) {
        return this.nfcImpl.getFCD16(c);
    }

    /* access modifiers changed from: package-private */
    public long getFirstPrimaryForGroup(int script) {
        int index = getScriptIndex(script);
        if (index == 0) {
            return 0;
        }
        return ((long) this.scriptStarts[index]) << 16;
    }

    public long getLastPrimaryForGroup(int script) {
        int index = getScriptIndex(script);
        if (index == 0) {
            return 0;
        }
        return (((long) this.scriptStarts[index + 1]) << 16) - 1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: char} */
    /* JADX WARNING: Multi-variable type inference failed */
    public int getGroupForPrimary(long p) {
        int index;
        long p2 = p >> 16;
        int index2 = 1;
        if (p2 < ((long) this.scriptStarts[1]) || ((long) this.scriptStarts[this.scriptStarts.length - 1]) <= p2) {
            return -1;
        }
        while (true) {
            index = index2;
            if (p2 < ((long) this.scriptStarts[index + 1])) {
                break;
            }
            index2 = index + 1;
        }
        for (int i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                return i;
            }
        }
        for (int i2 = 0; i2 < 8; i2++) {
            if (this.scriptsIndex[this.numScripts + i2] == index) {
                return 4096 + i2;
            }
        }
        return -1;
    }

    private int getScriptIndex(int script) {
        if (script < 0) {
            return 0;
        }
        if (script < this.numScripts) {
            return this.scriptsIndex[script];
        }
        if (script < 4096) {
            return 0;
        }
        int script2 = script - 4096;
        if (script2 < 8) {
            return this.scriptsIndex[this.numScripts + script2];
        }
        return 0;
    }

    public int[] getEquivalentScripts(int script) {
        int index = getScriptIndex(script);
        if (index == 0) {
            return EMPTY_INT_ARRAY;
        }
        if (script >= 4096) {
            return new int[]{script};
        }
        int length = 0;
        for (int i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                length++;
            }
        }
        int[] dest = new int[length];
        if (length == 1) {
            dest[0] = script;
            return dest;
        }
        int length2 = 0;
        for (int i2 = 0; i2 < this.numScripts; i2++) {
            if (this.scriptsIndex[i2] == index) {
                dest[length2] = i2;
                length2++;
            }
        }
        return dest;
    }

    /* access modifiers changed from: package-private */
    public void makeReorderRanges(int[] reorder, UVector32 ranges) {
        makeReorderRanges(reorder, false, ranges);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v12, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v3, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v13, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v14, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v15, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v16, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v17, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v4, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v6, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v19, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v21, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v7, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v8, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v27, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v28, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v30, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v32, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v33, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v34, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v35, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v9, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v10, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v11, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v12, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v36, resolved type: char} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v39, resolved type: char} */
    /*  JADX ERROR: IF instruction can be used only in fallback mode
        jadx.core.utils.exceptions.CodegenException: IF instruction can be used only in fallback mode
        	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:568)
        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:474)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x0079, code lost:
        r11 = r11;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    private void makeReorderRanges(int[] r21, boolean r22, android.icu.impl.coll.UVector32 r23) {
        /*
            r20 = this;
            r0 = r20
            r1 = r21
            r2 = r23
            r23.removeAllElements()
            int r3 = r1.length
            if (r3 == 0) goto L_0x01b2
            r4 = 103(0x67, float:1.44E-43)
            r5 = 0
            r6 = 1
            if (r3 != r6) goto L_0x001a
            r7 = r1[r5]
            if (r7 != r4) goto L_0x001a
            r18 = r3
            goto L_0x01b4
        L_0x001a:
            char[] r7 = r0.scriptStarts
            int r7 = r7.length
            int r7 = r7 - r6
            short[] r7 = new short[r7]
            char[] r8 = r0.scriptsIndex
            int r9 = r0.numScripts
            int r9 = r9 + 4110
            int r9 = r9 + -4096
            char r8 = r8[r9]
            r9 = 255(0xff, float:3.57E-43)
            if (r8 == 0) goto L_0x0030
            r7[r8] = r9
        L_0x0030:
            char[] r10 = r0.scriptsIndex
            int r11 = r0.numScripts
            int r11 = r11 + 4111
            int r11 = r11 + -4096
            char r8 = r10[r11]
            if (r8 == 0) goto L_0x003e
            r7[r8] = r9
        L_0x003e:
            char[] r8 = r0.scriptStarts
            char r8 = r8[r6]
            char[] r10 = r0.scriptStarts
            char[] r11 = r0.scriptStarts
            int r11 = r11.length
            int r11 = r11 - r6
            char r10 = r10[r11]
            r11 = 0
            r12 = r11
            r11 = r5
        L_0x0051:
            r13 = 8
            if (r11 >= r3) goto L_0x0063
            r14 = r1[r11]
            int r14 = r14 + -4096
            if (r14 < 0) goto L_0x0060
            if (r14 >= r13) goto L_0x0060
            int r13 = r6 << r14
            r12 = r12 | r13
        L_0x0060:
            int r11 = r11 + 1
            goto L_0x0051
        L_0x0063:
            r11 = r8
            r8 = r5
        L_0x0065:
            if (r8 >= r13) goto L_0x007c
            char[] r14 = r0.scriptsIndex
            int r15 = r0.numScripts
            int r15 = r15 + r8
            char r14 = r14[r15]
            if (r14 == 0) goto L_0x0079
            int r15 = r6 << r8
            r15 = r15 & r12
            if (r15 != 0) goto L_0x0079
            int r11 = r0.addLowScriptRange(r7, r14, r11)
        L_0x0079:
            int r8 = r8 + 1
            goto L_0x0065
        L_0x007c:
            r8 = 0
            if (r12 != 0) goto L_0x0094
            r14 = r1[r5]
            r15 = 25
            if (r14 != r15) goto L_0x0094
            if (r22 != 0) goto L_0x0094
            char[] r5 = r0.scriptsIndex
            char r5 = r5[r15]
            char[] r15 = r0.scriptStarts
            char r15 = r15[r5]
            int r8 = r15 - r11
            r11 = r15
        L_0x0094:
            r5 = 0
            r16 = 0
        L_0x0097:
            r15 = r16
            if (r15 >= r3) goto L_0x0131
            int r13 = r15 + 1
            r15 = r1[r15]
            r9 = -1
            if (r15 != r4) goto L_0x00f0
            r5 = 1
        L_0x00a3:
            if (r13 >= r3) goto L_0x0133
            int r3 = r3 + -1
            r15 = r1[r3]
            if (r15 == r4) goto L_0x00e6
            if (r15 == r9) goto L_0x00dc
            int r4 = r0.getScriptIndex(r15)
            if (r4 != 0) goto L_0x00b6
        L_0x00b3:
            r4 = 103(0x67, float:1.44E-43)
            goto L_0x00a3
        L_0x00b6:
            short r16 = r7[r4]
            if (r16 != 0) goto L_0x00bf
            int r10 = r0.addHighScriptRange(r7, r4, r10)
            goto L_0x00b3
        L_0x00bf:
            java.lang.IllegalArgumentException r6 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r17 = r3
            java.lang.String r3 = "setReorderCodes(): duplicate or equivalent script "
            r9.append(r3)
            java.lang.String r3 = scriptCodeString(r15)
            r9.append(r3)
            java.lang.String r3 = r9.toString()
            r6.<init>(r3)
            throw r6
        L_0x00dc:
            r17 = r3
            java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
            java.lang.String r4 = "setReorderCodes(): UScript.DEFAULT together with other scripts"
            r3.<init>(r4)
            throw r3
        L_0x00e6:
            r17 = r3
            java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
            java.lang.String r4 = "setReorderCodes(): duplicate UScript.UNKNOWN"
            r3.<init>(r4)
            throw r3
        L_0x00f0:
            if (r15 == r9) goto L_0x0127
            int r4 = r0.getScriptIndex(r15)
            if (r4 != 0) goto L_0x0101
        L_0x00f8:
            r16 = r13
            r4 = 103(0x67, float:1.44E-43)
            r9 = 255(0xff, float:3.57E-43)
            r13 = 8
            goto L_0x0097
        L_0x0101:
            short r9 = r7[r4]
            if (r9 != 0) goto L_0x010a
            int r11 = r0.addLowScriptRange(r7, r4, r11)
            goto L_0x00f8
        L_0x010a:
            java.lang.IllegalArgumentException r6 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r18 = r3
            java.lang.String r3 = "setReorderCodes(): duplicate or equivalent script "
            r9.append(r3)
            java.lang.String r3 = scriptCodeString(r15)
            r9.append(r3)
            java.lang.String r3 = r9.toString()
            r6.<init>(r3)
            throw r6
        L_0x0127:
            r18 = r3
            java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
            java.lang.String r4 = "setReorderCodes(): UScript.DEFAULT together with other scripts"
            r3.<init>(r4)
            throw r3
        L_0x0131:
            r18 = r3
        L_0x0133:
            r4 = r6
        L_0x0134:
            char[] r9 = r0.scriptStarts
            int r9 = r9.length
            int r9 = r9 - r6
            if (r4 >= r9) goto L_0x0150
            short r9 = r7[r4]
            if (r9 == 0) goto L_0x013f
            goto L_0x014d
        L_0x013f:
            char[] r13 = r0.scriptStarts
            char r13 = r13[r4]
            if (r5 != 0) goto L_0x0148
            if (r13 <= r11) goto L_0x0148
            r11 = r13
        L_0x0148:
            int r9 = r0.addLowScriptRange(r7, r4, r11)
            r11 = r9
        L_0x014d:
            int r4 = r4 + 1
            goto L_0x0134
        L_0x0150:
            if (r11 <= r10) goto L_0x0166
            r4 = 65280(0xff00, float:9.1477E-41)
            r4 = r4 & r8
            int r4 = r11 - r4
            if (r4 > r10) goto L_0x015e
            r0.makeReorderRanges(r1, r6, r2)
            return
        L_0x015e:
            android.icu.util.ICUException r4 = new android.icu.util.ICUException
            java.lang.String r6 = "setReorderCodes(): reordering too many partial-primary-lead-byte scripts"
            r4.<init>((java.lang.String) r6)
            throw r4
        L_0x0166:
            r4 = 0
            r9 = r4
            r4 = r6
        L_0x0169:
            r13 = r4
            r4 = r9
        L_0x016b:
            char[] r15 = r0.scriptStarts
            int r15 = r15.length
            int r15 = r15 - r6
            if (r13 >= r15) goto L_0x018c
            short r15 = r7[r13]
            r6 = 255(0xff, float:3.57E-43)
            if (r15 != r6) goto L_0x017a
            r16 = 8
            goto L_0x0187
        L_0x017a:
            char[] r6 = r0.scriptStarts
            char r6 = r6[r13]
            r16 = 8
            int r6 = r6 >> 8
            int r4 = r15 - r6
            if (r4 == r9) goto L_0x0187
            goto L_0x018e
        L_0x0187:
            int r13 = r13 + 1
            r6 = 1
            goto L_0x016b
        L_0x018c:
            r16 = 8
        L_0x018e:
            if (r9 != 0) goto L_0x0197
            char[] r6 = r0.scriptStarts
            int r6 = r6.length
            r15 = 1
            int r6 = r6 - r15
            if (r13 >= r6) goto L_0x01a5
        L_0x0197:
            char[] r6 = r0.scriptStarts
            char r6 = r6[r13]
            int r6 = r6 << 16
            r15 = 65535(0xffff, float:9.1834E-41)
            r15 = r15 & r9
            r6 = r6 | r15
            r2.addElement(r6)
        L_0x01a5:
            char[] r6 = r0.scriptStarts
            int r6 = r6.length
            r15 = 1
            int r6 = r6 - r15
            if (r13 != r6) goto L_0x01ad
            return
        L_0x01ad:
            r9 = r4
            int r4 = r13 + 1
            r6 = r15
            goto L_0x0169
        L_0x01b2:
            r18 = r3
        L_0x01b4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationData.makeReorderRanges(int[], boolean, android.icu.impl.coll.UVector32):void");
    }

    private int addLowScriptRange(short[] table, int index, int lowStart) {
        char start = this.scriptStarts[index];
        if ((start & 255) < (lowStart & 255)) {
            lowStart += 256;
        }
        table[index] = (short) (lowStart >> 8);
        char limit = this.scriptStarts[index + 1];
        return ((lowStart & 65280) + ((limit & 65280) - (65280 & start))) | (limit & 255);
    }

    private int addHighScriptRange(short[] table, int index, int highLimit) {
        char limit = this.scriptStarts[index + 1];
        if ((limit & 255) > (highLimit & 255)) {
            highLimit -= 256;
        }
        char start = this.scriptStarts[index];
        int highLimit2 = ((highLimit & 65280) - ((limit & 65280) - (65280 & start))) | (start & 255);
        table[index] = (short) (highLimit2 >> 8);
        return highLimit2;
    }

    private static String scriptCodeString(int script) {
        if (script < 4096) {
            return Integer.toString(script);
        }
        return "0x" + Integer.toHexString(script);
    }
}

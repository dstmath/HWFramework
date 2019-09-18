package android.text;

import android.icu.lang.UCharacter;
import android.icu.text.Bidi;
import android.icu.text.BidiClassifier;
import android.text.Layout;
import com.android.internal.annotations.VisibleForTesting;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class AndroidBidi {
    private static final EmojiBidiOverride sEmojiBidiOverride = new EmojiBidiOverride();

    public static class EmojiBidiOverride extends BidiClassifier {
        private static final int NO_OVERRIDE = (UCharacter.getIntPropertyMaxValue(4096) + 1);

        public EmojiBidiOverride() {
            super(null);
        }

        public int classify(int c) {
            if (Emoji.isNewEmoji(c)) {
                return 10;
            }
            return NO_OVERRIDE;
        }
    }

    public static int bidi(int dir, char[] chs, byte[] chInfo) {
        byte paraLevel;
        if (chs == null || chInfo == null) {
            throw new NullPointerException();
        }
        int length = chs.length;
        if (chInfo.length >= length) {
            switch (dir) {
                case -2:
                    paraLevel = Byte.MAX_VALUE;
                    break;
                case -1:
                    paraLevel = 1;
                    break;
                case 1:
                    paraLevel = 0;
                    break;
                case 2:
                    paraLevel = 126;
                    break;
                default:
                    paraLevel = 0;
                    break;
            }
            Bidi icuBidi = new Bidi(length, 0);
            icuBidi.setCustomClassifier(sEmojiBidiOverride);
            icuBidi.setPara(chs, paraLevel, null);
            for (int i = 0; i < length; i++) {
                chInfo[i] = icuBidi.getLevelAt(i);
            }
            return (icuBidi.getParaLevel() & 1) == 0 ? 1 : -1;
        }
        throw new IndexOutOfBoundsException();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v23, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v24, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v30, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v32, resolved type: byte} */
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
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:205)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:66)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
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
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0079, code lost:
        if (r4 >= r3) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x007b, code lost:
        r3 = r4;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r7v0, types: [byte] */
    /* JADX WARNING: Multi-variable type inference failed */
    public static android.text.Layout.Directions directions(int r26, byte[] r27, int r28, char[] r29, int r30, int r31) {
        /*
            r2 = r31
            if (r2 != 0) goto L_0x0007
            android.text.Layout$Directions r3 = android.text.Layout.DIRS_ALL_LEFT_TO_RIGHT
            return r3
        L_0x0007:
            r4 = 1
            r5 = r26
            if (r5 != r4) goto L_0x000e
            r6 = 0
            goto L_0x000f
        L_0x000e:
            r6 = r4
        L_0x000f:
            byte r7 = r27[r28]
            r8 = r7
            r9 = 1
            int r10 = r28 + 1
            int r11 = r28 + r2
        L_0x0017:
            if (r10 >= r11) goto L_0x0023
            byte r12 = r27[r10]
            if (r12 == r7) goto L_0x0020
            r7 = r12
            int r9 = r9 + 1
        L_0x0020:
            int r10 = r10 + 1
            goto L_0x0017
        L_0x0023:
            r10 = r2
            r11 = r7 & 1
            r12 = r6 & 1
            if (r11 == r12) goto L_0x0048
        L_0x002a:
            int r10 = r10 + -1
            if (r10 < 0) goto L_0x0043
            int r12 = r30 + r10
            char r12 = r29[r12]
            r13 = 10
            if (r12 != r13) goto L_0x0039
            int r10 = r10 + -1
            goto L_0x0043
        L_0x0039:
            r13 = 32
            if (r12 == r13) goto L_0x0042
            r13 = 9
            if (r12 == r13) goto L_0x0042
            goto L_0x0043
        L_0x0042:
            goto L_0x002a
        L_0x0043:
            int r10 = r10 + r4
            if (r10 == r2) goto L_0x0048
            int r9 = r9 + 1
        L_0x0048:
            if (r9 != r4) goto L_0x0056
            if (r8 != r6) goto L_0x0056
            r3 = r8 & 1
            if (r3 == 0) goto L_0x0053
            android.text.Layout$Directions r3 = android.text.Layout.DIRS_ALL_RIGHT_TO_LEFT
            return r3
        L_0x0053:
            android.text.Layout$Directions r3 = android.text.Layout.DIRS_ALL_LEFT_TO_RIGHT
            return r3
        L_0x0056:
            int r12 = r9 * 2
            int[] r12 = new int[r12]
            r13 = r8
            int r14 = r8 << 26
            r15 = 1
            r16 = r28
            r7 = r8
            r17 = r28
            int r18 = r28 + r10
            r3 = r8
            r7 = r17
        L_0x0068:
            r19 = r18
            r4 = r19
            if (r7 >= r4) goto L_0x0095
            r21 = r4
            byte r4 = r27[r7]
            if (r4 == r8) goto L_0x008f
            r8 = r4
            if (r4 <= r13) goto L_0x0079
            r13 = r4
            goto L_0x007c
        L_0x0079:
            if (r4 >= r3) goto L_0x007c
            r3 = r4
        L_0x007c:
            int r17 = r15 + 1
            int r18 = r7 - r16
            r18 = r18 | r14
            r12[r15] = r18
            int r15 = r17 + 1
            int r18 = r7 - r28
            r12[r17] = r18
            int r14 = r8 << 26
            r4 = r7
            r16 = r4
        L_0x008f:
            int r7 = r7 + 1
            r18 = r21
            r4 = 1
            goto L_0x0068
        L_0x0095:
            int r4 = r28 + r10
            int r4 = r4 - r16
            r4 = r4 | r14
            r12[r15] = r4
            if (r10 >= r2) goto L_0x00ad
            int r15 = r15 + 1
            r12[r15] = r10
            r4 = 1
            int r15 = r15 + r4
            int r7 = r2 - r10
            int r17 = r6 << 26
            r7 = r7 | r17
            r12[r15] = r7
            goto L_0x00ae
        L_0x00ad:
            r4 = 1
        L_0x00ae:
            int r7 = r15 + 1
            int r4 = r12.length
            if (r7 >= r4) goto L_0x00b7
            int[] r12 = java.util.Arrays.copyOf(r12, r7)
        L_0x00b7:
            r4 = r3 & 1
            if (r4 != r6) goto L_0x00c7
            int r3 = r3 + 1
            if (r13 <= r3) goto L_0x00c2
            r20 = 1
            goto L_0x00c4
        L_0x00c2:
            r20 = 0
        L_0x00c4:
            r4 = r20
            goto L_0x00cc
        L_0x00c7:
            r4 = 1
            if (r9 <= r4) goto L_0x00cb
            goto L_0x00cc
        L_0x00cb:
            r4 = 0
        L_0x00cc:
            if (r4 == 0) goto L_0x012f
            int r7 = r13 + -1
        L_0x00d0:
            if (r7 < r3) goto L_0x012f
            r15 = 0
        L_0x00d3:
            r22 = r15
            int r1 = r12.length
            r2 = r22
            if (r2 >= r1) goto L_0x0128
            r1 = r12[r2]
            byte r1 = r27[r1]
            if (r1 < r7) goto L_0x011d
            int r22 = r2 + 2
        L_0x00e2:
            r1 = r22
            r23 = r3
            int r3 = r12.length
            if (r1 >= r3) goto L_0x00f4
            r3 = r12[r1]
            byte r3 = r27[r3]
            if (r3 < r7) goto L_0x00f4
            int r22 = r1 + 2
            r3 = r23
            goto L_0x00e2
        L_0x00f4:
            r3 = r2
            int r15 = r1 + -2
        L_0x00f7:
            r24 = r15
            r0 = r24
            if (r3 >= r0) goto L_0x011a
            r15 = r12[r3]
            r16 = r12[r0]
            r12[r3] = r16
            r12[r0] = r15
            int r16 = r3 + 1
            r15 = r12[r16]
            int r16 = r3 + 1
            int r24 = r0 + 1
            r17 = r12[r24]
            r12[r16] = r17
            int r24 = r0 + 1
            r12[r24] = r15
            int r3 = r3 + 2
            int r15 = r0 + -2
            goto L_0x00f7
        L_0x011a:
            int r22 = r1 + 2
            goto L_0x0121
        L_0x011d:
            r23 = r3
            r22 = r2
        L_0x0121:
            int r15 = r22 + 2
            r3 = r23
            r2 = r31
            goto L_0x00d3
        L_0x0128:
            r23 = r3
            int r7 = r7 + -1
            r2 = r31
            goto L_0x00d0
        L_0x012f:
            r23 = r3
            android.text.Layout$Directions r0 = new android.text.Layout$Directions
            r0.<init>(r12)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.AndroidBidi.directions(int, byte[], int, char[], int, int):android.text.Layout$Directions");
    }
}

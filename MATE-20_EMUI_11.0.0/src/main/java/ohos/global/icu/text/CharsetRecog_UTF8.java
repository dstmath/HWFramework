package ohos.global.icu.text;

class CharsetRecog_UTF8 extends CharsetRecognizer {
    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.CharsetRecognizer
    public String getName() {
        return "UTF-8";
    }

    CharsetRecog_UTF8() {
    }

    /* access modifiers changed from: package-private */
    /*  JADX ERROR: IF instruction can be used only in fallback mode
        jadx.core.utils.exceptions.CodegenException: IF instruction can be used only in fallback mode
        	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:605)
        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:487)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:218)
        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:110)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:56)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:99)
        	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:194)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:67)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:99)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:157)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:99)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:143)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:99)
        	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:239)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:67)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:93)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:59)
        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:245)
        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:238)
        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:341)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:294)
        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:263)
        	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:184)
        	at java.util.ArrayList.forEach(ArrayList.java:1257)
        	at java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:390)
        	at java.util.stream.Sink$ChainedReference.end(Sink.java:258)
        */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x008e A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0061 A[EDGE_INSN: B:58:0x0061->B:32:0x0061 ?: BREAK  , SYNTHETIC] */
    @Override // ohos.global.icu.text.CharsetRecognizer
    public ohos.global.icu.text.CharsetMatch match(ohos.global.icu.text.CharsetDetector r14) {
        /*
            r13 = this;
            byte[] r0 = r14.fRawInput
            int r1 = r14.fRawLength
            r2 = 2
            r3 = 3
            r4 = 0
            r5 = 1
            if (r1 < r3) goto L_0x0024
            byte r1 = r0[r4]
            r1 = r1 & 255(0xff, float:3.57E-43)
            r6 = 239(0xef, float:3.35E-43)
            if (r1 != r6) goto L_0x0024
            byte r1 = r0[r5]
            r1 = r1 & 255(0xff, float:3.57E-43)
            r6 = 187(0xbb, float:2.62E-43)
            if (r1 != r6) goto L_0x0024
            byte r1 = r0[r2]
            r1 = r1 & 255(0xff, float:3.57E-43)
            r6 = 191(0xbf, float:2.68E-43)
            if (r1 != r6) goto L_0x0024
            r1 = r5
            goto L_0x0025
        L_0x0024:
            r1 = r4
        L_0x0025:
            r6 = r4
            r7 = r6
            r8 = r7
        L_0x0028:
            int r9 = r14.fRawLength
            if (r6 >= r9) goto L_0x0063
            byte r9 = r0[r6]
            r10 = r9 & 128(0x80, float:1.794E-43)
            if (r10 != 0) goto L_0x0033
            goto L_0x0061
        L_0x0033:
            r10 = r9 & 224(0xe0, float:3.14E-43)
            r11 = 192(0xc0, float:2.69E-43)
            if (r10 != r11) goto L_0x003b
            r9 = r5
            goto L_0x004a
        L_0x003b:
            r10 = r9 & 240(0xf0, float:3.36E-43)
            r12 = 224(0xe0, float:3.14E-43)
            if (r10 != r12) goto L_0x0043
            r9 = r2
            goto L_0x004a
        L_0x0043:
            r9 = r9 & 248(0xf8, float:3.48E-43)
            r10 = 240(0xf0, float:3.36E-43)
            if (r9 != r10) goto L_0x005f
            r9 = r3
        L_0x004a:
            int r6 = r6 + r5
            int r10 = r14.fRawLength
            if (r6 < r10) goto L_0x0050
            goto L_0x0061
        L_0x0050:
            byte r10 = r0[r6]
            r10 = r10 & r11
            r12 = 128(0x80, float:1.794E-43)
            if (r10 == r12) goto L_0x0058
            goto L_0x005f
        L_0x0058:
            int r9 = r9 + -1
            if (r9 != 0) goto L_0x004a
            int r8 = r8 + 1
            goto L_0x0061
        L_0x005f:
            int r7 = r7 + 1
        L_0x0061:
            int r6 = r6 + r5
            goto L_0x0028
        L_0x0063:
            r0 = 80
            r2 = 100
            if (r1 == 0) goto L_0x006d
            if (r7 != 0) goto L_0x006d
        L_0x006b:
            r4 = r2
            goto L_0x008c
        L_0x006d:
            if (r1 == 0) goto L_0x0075
            int r1 = r7 * 10
            if (r8 <= r1) goto L_0x0075
        L_0x0073:
            r4 = r0
            goto L_0x008c
        L_0x0075:
            if (r8 <= r3) goto L_0x007a
            if (r7 != 0) goto L_0x007a
            goto L_0x006b
        L_0x007a:
            if (r8 <= 0) goto L_0x007f
            if (r7 != 0) goto L_0x007f
            goto L_0x0073
        L_0x007f:
            if (r8 != 0) goto L_0x0086
            if (r7 != 0) goto L_0x0086
            r4 = 15
            goto L_0x008c
        L_0x0086:
            int r7 = r7 * 10
            if (r8 <= r7) goto L_0x008c
            r4 = 25
        L_0x008c:
            if (r4 != 0) goto L_0x0090
            r13 = 0
            goto L_0x0096
        L_0x0090:
            ohos.global.icu.text.CharsetMatch r0 = new ohos.global.icu.text.CharsetMatch
            r0.<init>(r14, r13, r4)
            r13 = r0
        L_0x0096:
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.global.icu.text.CharsetRecog_UTF8.match(ohos.global.icu.text.CharsetDetector):ohos.global.icu.text.CharsetMatch");
    }
}

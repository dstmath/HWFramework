package com.huawei.g11n.tmr.address;

import com.huawei.g11n.tmr.address.jni.DicSearch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SerEn {
    private static final int TYPE_BUILDING = 1;
    private static final int TYPE_BUILDING2 = 2;
    private static final int TYPE_CITY = 0;
    private String location = this.reguEx.location;
    ArrayList<Integer> match_index_2 = new ArrayList<>();
    private String not = "(?i)(?:my|your|his|her|its|their|our|this|that|the|a|an|what|which|whose)";
    private Pattern p1346 = this.reguEx.p1346;
    private Pattern p28 = this.reguEx.p28;
    private Pattern p2s = this.reguEx.p2s;
    private Pattern p52 = this.reguEx.p52;
    private Pattern p52_sub = this.reguEx.p52_sub;
    private Pattern p52s = this.reguEx.p52s;
    private Pattern pCode_a = Pattern.compile("(?<!\\d)(?:\\d{5}(?:\\s*-\\s*\\d{4})?)(?!\\d)");
    Pattern pComma = Pattern.compile("(?:(?:[\\s\\S]*)(?:,|\\.)([\\s\\S]*))");
    Pattern pCut = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
    private Pattern pDir = Pattern.compile("\\s*(south|north|west|east)\\s*");
    Pattern pLocation = Pattern.compile("(?:([\\s\\S]*?)(?<![a-zA-Z])((?:" + this.location + ")((?:\\s+|\\s*&\\s*)(?:" + this.location + "))?" + ")(?![a-zA-Z]))");
    Pattern pNo = Pattern.compile("(?:[\\s\\S]*(?<![a-zA-Z])(?i)(the|in|on|at|from|to|of|for)(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+))");
    Pattern pNot_1 = Pattern.compile("([\\s\\S]*?)(?<![a-zA-Z])" + this.location + "(?![a-zA-Z])");
    Pattern pNot_2 = Pattern.compile("[\\s\\S]*(?<![a-zA-Z])" + this.not + "\\s+");
    private Pattern pNum = Pattern.compile("(?:(?:\\s*[:,\\.\"-]\\s*|\\s*)\\d+(?:\\s*[,\\.\":-]\\s*|\\s+))+");
    Pattern pPre_city = Pattern.compile("(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])");
    Pattern pPre_uni = Pattern.compile("(?:\\b(?i)(in|at|from|near|to|of|for)\\b([\\s\\S]*))");
    private Pattern pRoad = Pattern.compile("(?i)(?:\\s*(?:(in|on|at)\\s+)?(?:the\\s+)?(boulevard|avenue|street|freeway|road|circle|lane|drive|court|ally|parkway|Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Way|Fwy|Crescent|Highway))");
    Pattern pSingle = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
    private Pattern p_box = Pattern.compile(this.reguEx.post_box);
    private Pattern p_resultclean = Pattern.compile("(?:(?:[^0-9a-zA-Z]*)(?i)(?:(?:in|at|on|from|to|of|and)\\s+)?(?:(?:the)\\s+)?)(?:([\\s\\S]*)?,|([\\s\\S]*))");
    private ReguEx reguEx = new ReguEx();
    private String road_suf = "(?:boulevard|avenue|street|freeway|road|circle|way|lane|drive|court|ally|parkway|Crescent|Highway|(?:Ave|AV|Blvd|Cir|Ct|Dr|Ln|Pkwy|Rd|Sq|St|Fwy)(?:\\.|\\b))";

    SerEn() {
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    public java.util.ArrayList<com.huawei.g11n.tmr.address.Match> search(java.lang.String r76) {
        /*
            r75 = this;
            r1 = r75
            r2 = r76
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r3 = r0
            r4 = 0
            java.lang.String r0 = "[A-Z0-9]"
            java.util.regex.Pattern r5 = java.util.regex.Pattern.compile(r0)
            java.lang.String r0 = ""
            java.lang.String r6 = ""
            java.lang.String r7 = "(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(?:uptown|downtown)\\s+)?)?[\\s\\S]*"
            java.util.regex.Pattern r7 = java.util.regex.Pattern.compile(r7)
            java.lang.String r8 = "(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*"
            java.util.regex.Pattern r8 = java.util.regex.Pattern.compile(r8)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            java.lang.String r10 = "(?i)(?<![a-z])(?:(?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+)((?:[\\s\\S]+?)(?:(?<![a-z])((?:in|on|at|to)\\s+(?:the\\s+)?|the\\s+))?"
            r9.<init>(r10)
            java.lang.String r10 = r1.road_suf
            r9.append(r10)
            java.lang.String r10 = "(?![a-zA-Z])[\\s\\S]*)"
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            java.util.regex.Pattern r9 = java.util.regex.Pattern.compile(r9)
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r11 = "(?i)((?<![a-zA-Z])(?:a|what|which|whose|i|you|this|that|my|his|her|out|their|its)\\s+)([\\s\\S]+)?"
            r10.<init>(r11)
            java.lang.String r11 = r1.road_suf
            r10.append(r11)
            java.lang.String r11 = "(?![a-zA-Z])"
            r10.append(r11)
            java.lang.String r10 = r10.toString()
            java.util.regex.Pattern r10 = java.util.regex.Pattern.compile(r10)
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            java.lang.String r12 = "(?:[^0-9a-zA-Z]*|\\s*(?:(?i)the|this|a|that)\\s*)(?:"
            r11.<init>(r12)
            java.lang.String r12 = r1.location
            r11.append(r12)
            java.lang.String r12 = ")[^0-9a-zA-Z]*"
            r11.append(r12)
            java.lang.String r11 = r11.toString()
            java.util.regex.Pattern r11 = java.util.regex.Pattern.compile(r11)
            r12 = 0
            r13 = 0
            r14 = 0
            java.lang.String r15 = ""
            r16 = r4
            r4 = 0
            r17 = r0
            java.lang.Integer r0 = java.lang.Integer.valueOf(r4)
            r3.add(r0)
            java.util.regex.Pattern r0 = r1.p52
            java.util.regex.Matcher r4 = r0.matcher(r2)
            java.util.regex.Pattern r0 = r1.p28
            r19 = r6
            java.util.regex.Matcher r6 = r0.matcher(r2)
            java.util.regex.Pattern r0 = r1.p1346
            r20 = r12
            java.util.regex.Matcher r12 = r0.matcher(r2)
            java.util.regex.Pattern r0 = r1.p52_sub
            r21 = r13
            java.util.regex.Matcher r13 = r0.matcher(r2)
            r0 = 1
            r22 = r0
            java.util.ArrayList<java.lang.Integer> r0 = r1.match_index_2
            r0.clear()
            r0 = r17
        L_0x00aa:
            boolean r17 = r6.find()
            r23 = r14
            if (r17 != 0) goto L_0x0d92
            if (r22 == 0) goto L_0x00ef
            java.util.regex.Pattern r14 = r1.p_box
            java.util.regex.Matcher r14 = r14.matcher(r2)
        L_0x00ba:
            boolean r17 = r14.find()
            if (r17 != 0) goto L_0x00c3
            r14 = r23
            goto L_0x00f5
        L_0x00c3:
            r24 = r0
            int r0 = r14.start()
            r25 = r15
            java.lang.String r15 = r14.group()
            int r15 = r15.length()
            int r15 = r15 + r0
            r26 = r14
            java.lang.Integer r14 = java.lang.Integer.valueOf(r0)
            r3.add(r14)
            java.lang.Integer r14 = java.lang.Integer.valueOf(r15)
            r3.add(r14)
            r21 = r0
            r23 = r15
            r0 = r24
            r15 = r25
            r14 = r26
            goto L_0x00ba
        L_0x00ef:
            r24 = r0
            r25 = r15
            r14 = r23
        L_0x00f5:
            boolean r17 = r4.find()
            r27 = r0
            if (r17 != 0) goto L_0x0991
        L_0x00fd:
            boolean r17 = r13.find()
            if (r17 != 0) goto L_0x0592
        L_0x0103:
            boolean r0 = r12.find()
            if (r0 != 0) goto L_0x026f
            int r0 = r3.size()
            r28 = r14
            int[] r14 = new int[r0]
            r16 = 0
            r29 = r15
            r15 = r16
        L_0x0117:
            if (r15 < r0) goto L_0x0250
            r15 = 4
            if (r0 <= r15) goto L_0x023a
            int[] r15 = new int[r0]
            r16 = 0
            r17 = 1
            r30 = r6
            r6 = r17
        L_0x0126:
            int r17 = r0 + -1
            r31 = r4
            r23 = 2
            int r4 = r17 / 2
            if (r6 < r4) goto L_0x01b9
            r4 = 1
        L_0x0131:
            int r6 = r0 + 1
            int r6 = r6 / 2
            if (r4 < r6) goto L_0x0141
            r4 = 0
            r14[r4] = r16
            r15[r4] = r16
            java.util.ArrayList r4 = r1.createAddressResultData(r15, r2)
            return r4
        L_0x0141:
            int r16 = r16 + 1
            int r6 = r16 * 2
            r17 = 1
            int r6 = r6 + -1
            int r23 = r4 * 2
            int r23 = r23 + -1
            r17 = r14[r23]
            r15[r6] = r17
            int r6 = r16 * 2
            int r17 = r4 * 2
            r17 = r14[r17]
            r15[r6] = r17
            int r6 = r4 + 1
        L_0x015b:
            int r17 = r0 + 1
            r32 = r10
            r23 = 2
            int r10 = r17 / 2
            if (r6 < r10) goto L_0x016a
            r34 = r8
            r33 = r9
            goto L_0x01ad
        L_0x016a:
            int r10 = r4 * 2
            r10 = r14[r10]
            int r17 = r6 * 2
            r23 = 1
            int r17 = r17 + -1
            r33 = r9
            r9 = r14[r17]
            if (r10 < r9) goto L_0x01a7
            int r9 = r4 * 2
            int r10 = r4 * 2
            r10 = r14[r10]
            int r17 = r6 * 2
            r34 = r8
            r8 = r14[r17]
            int r8 = r1.max(r10, r8)
            r14[r9] = r8
            int r8 = r16 * 2
            int r9 = r4 * 2
            r9 = r14[r9]
            r15[r8] = r9
            int r8 = r0 + 1
            r9 = 2
            int r8 = r8 / r9
            r9 = 1
            int r8 = r8 - r9
            if (r6 != r8) goto L_0x019e
            r4 = r6
        L_0x019e:
            int r6 = r6 + 1
            r10 = r32
            r9 = r33
            r8 = r34
            goto L_0x015b
        L_0x01a7:
            r34 = r8
            r4 = r6
            r8 = -1
            int r4 = r4 + r8
        L_0x01ad:
            r6 = 1
            int r4 = r4 + r6
            r10 = r32
            r9 = r33
            r8 = r34
            r23 = 2
            goto L_0x0131
        L_0x01b9:
            r34 = r8
            r33 = r9
            r32 = r10
            int r4 = r6 + 1
        L_0x01c1:
            int r8 = r0 + 1
            r9 = 2
            int r8 = r8 / r9
            if (r4 < r8) goto L_0x01d3
            int r6 = r6 + 1
            r4 = r31
            r10 = r32
            r9 = r33
            r8 = r34
            goto L_0x0126
        L_0x01d3:
            int r8 = r6 * 2
            r9 = 1
            int r8 = r8 - r9
            r8 = r14[r8]
            int r10 = r4 * 2
            int r10 = r10 - r9
            r10 = r14[r10]
            if (r8 <= r10) goto L_0x0237
            int r8 = r6 * 2
            int r8 = r8 - r9
            r10 = r14[r8]
            int r17 = r4 * 2
            int r17 = r17 + -1
            r17 = r14[r17]
            int r10 = r10 + r17
            r14[r8] = r10
            int r8 = r4 * 2
            int r8 = r8 - r9
            int r10 = r6 * 2
            int r10 = r10 - r9
            r10 = r14[r10]
            int r17 = r4 * 2
            int r17 = r17 + -1
            r17 = r14[r17]
            int r10 = r10 - r17
            r14[r8] = r10
            int r8 = r6 * 2
            int r8 = r8 - r9
            int r10 = r6 * 2
            int r10 = r10 - r9
            r10 = r14[r10]
            int r17 = r4 * 2
            int r17 = r17 + -1
            r9 = r14[r17]
            int r10 = r10 - r9
            r14[r8] = r10
            int r8 = r6 * 2
            r9 = r14[r8]
            int r10 = r4 * 2
            r10 = r14[r10]
            int r9 = r9 + r10
            r14[r8] = r9
            int r8 = r4 * 2
            int r9 = r6 * 2
            r9 = r14[r9]
            int r10 = r4 * 2
            r10 = r14[r10]
            int r9 = r9 - r10
            r14[r8] = r9
            int r8 = r6 * 2
            int r9 = r6 * 2
            r9 = r14[r9]
            int r10 = r4 * 2
            r10 = r14[r10]
            int r9 = r9 - r10
            r14[r8] = r9
        L_0x0237:
            int r4 = r4 + 1
            goto L_0x01c1
        L_0x023a:
            r31 = r4
            r30 = r6
            r34 = r8
            r33 = r9
            r32 = r10
            int r4 = r0 + -1
            r6 = 2
            int r4 = r4 / r6
            r6 = 0
            r14[r6] = r4
            java.util.ArrayList r4 = r1.createAddressResultData(r14, r2)
            return r4
        L_0x0250:
            r31 = r4
            r30 = r6
            r34 = r8
            r33 = r9
            r32 = r10
            r6 = 0
            java.lang.Object r4 = r3.get(r15)
            java.lang.Integer r4 = (java.lang.Integer) r4
            int r4 = r4.intValue()
            r14[r15] = r4
            int r15 = r15 + 1
            r6 = r30
            r4 = r31
            goto L_0x0117
        L_0x026f:
            r31 = r4
            r30 = r6
            r34 = r8
            r33 = r9
            r32 = r10
            r28 = r14
            r29 = r15
            r6 = 0
            java.lang.String r0 = r12.group()
            java.util.regex.Matcher r4 = r5.matcher(r0)
            boolean r0 = r4.find()
            if (r0 == 0) goto L_0x0582
            int r8 = r12.start()
            r0 = 8
            java.lang.String[] r0 = new java.lang.String[r0]
            java.util.ArrayList<java.lang.Integer> r9 = r1.match_index_2
            r9.clear()
            java.lang.String r9 = r12.group()
            java.lang.String[] r9 = r1.searBuilding(r9, r8)
            if (r9 == 0) goto L_0x040b
            int r10 = r9.length
            java.util.ArrayList<java.lang.Integer> r0 = r1.match_index_2
            java.util.Iterator r14 = r0.iterator()
            r0 = 0
            r15 = r0
        L_0x02ac:
            if (r15 >= r10) goto L_0x0400
            r0 = r9[r15]
            if (r0 != 0) goto L_0x02ba
            r35 = r4
            r36 = r5
            r38 = r9
            goto L_0x0406
        L_0x02ba:
            java.util.regex.Pattern r0 = r1.p_resultclean
            r6 = r9[r15]
            java.util.regex.Matcher r6 = r0.matcher(r6)
            boolean r0 = r6.matches()
            if (r0 == 0) goto L_0x03e9
            r35 = r4
            r4 = 1
            java.lang.String r0 = r6.group(r4)
            if (r0 == 0) goto L_0x02e4
            java.lang.String r0 = r6.group(r4)
            int r17 = r0.length()
            int r17 = r17 + 1
            r4 = r9[r15]
            int r4 = r4.length()
            int r4 = r4 - r17
            goto L_0x02f5
        L_0x02e4:
            r4 = 2
            java.lang.String r0 = r6.group(r4)
            int r17 = r0.length()
            r4 = r9[r15]
            int r4 = r4.length()
            int r4 = r4 - r17
        L_0x02f5:
            r36 = r5
            java.util.regex.Pattern r5 = r1.pNum
            java.util.regex.Matcher r5 = r5.matcher(r0)
            boolean r18 = r5.lookingAt()
            if (r18 == 0) goto L_0x0322
            r37 = r6
            java.lang.String r6 = r5.group()
            int r6 = r6.length()
            r38 = r9
            int r9 = r0.length()
            java.lang.String r0 = r0.substring(r6, r9)
            java.lang.String r6 = r5.group()
            int r6 = r6.length()
            int r4 = r4 + r6
            r6 = r0
            goto L_0x0327
        L_0x0322:
            r37 = r6
            r38 = r9
            r6 = r0
        L_0x0327:
            boolean r0 = r14.hasNext()
            if (r0 == 0) goto L_0x03e0
            java.lang.Object r0 = r14.next()
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r9 = r0.intValue()
            int r4 = r4 + r9
            int r0 = r6.length()
            r39 = r5
            int r5 = r4 + r0
            java.lang.String r0 = r2.substring(r4, r5)     // Catch:{ Exception -> 0x03b6 }
            r40 = r9
            java.util.regex.Pattern r9 = r1.pDir     // Catch:{ Exception -> 0x03b2 }
            java.util.regex.Matcher r9 = r9.matcher(r0)     // Catch:{ Exception -> 0x03b2 }
            boolean r18 = r9.lookingAt()     // Catch:{ Exception -> 0x03b2 }
            if (r18 == 0) goto L_0x035a
            r6 = r0
            r43 = r0
            r41 = r9
            r45 = r10
            goto L_0x0397
        L_0x035a:
            r41 = r9
            java.lang.String r9 = "((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)"
            java.util.regex.Pattern r9 = java.util.regex.Pattern.compile(r9)     // Catch:{ Exception -> 0x03b2 }
            java.util.regex.Matcher r18 = r9.matcher(r0)     // Catch:{ Exception -> 0x03b2 }
            r42 = r18
            r43 = r0
            r0 = r42
            boolean r18 = r0.matches()     // Catch:{ Exception -> 0x03b2 }
            if (r18 == 0) goto L_0x0395
            r44 = r9
            r45 = r10
            r9 = 1
            java.lang.String r10 = r0.group(r9)     // Catch:{ Exception -> 0x03b0 }
            int r10 = r10.length()     // Catch:{ Exception -> 0x03b0 }
            int r4 = r4 + r10
            java.lang.String r10 = r0.group(r9)     // Catch:{ Exception -> 0x03b0 }
            int r9 = r10.length()     // Catch:{ Exception -> 0x03b0 }
            int r10 = r6.length()     // Catch:{ Exception -> 0x03b0 }
            java.lang.String r9 = r6.substring(r9, r10)     // Catch:{ Exception -> 0x03b0 }
            r0 = r9
            r6 = r0
            goto L_0x0397
        L_0x0395:
            r45 = r10
        L_0x0397:
            java.util.regex.Matcher r0 = r11.matcher(r6)     // Catch:{ Exception -> 0x03b0 }
            boolean r9 = r0.matches()     // Catch:{ Exception -> 0x03b0 }
            if (r9 != 0) goto L_0x03d7
            java.lang.Integer r9 = java.lang.Integer.valueOf(r4)     // Catch:{ Exception -> 0x03b0 }
            r3.add(r9)     // Catch:{ Exception -> 0x03b0 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r5)     // Catch:{ Exception -> 0x03b0 }
            r3.add(r9)     // Catch:{ Exception -> 0x03b0 }
            goto L_0x03d7
        L_0x03b0:
            r0 = move-exception
            goto L_0x03bb
        L_0x03b2:
            r0 = move-exception
            r45 = r10
            goto L_0x03bb
        L_0x03b6:
            r0 = move-exception
            r40 = r9
            r45 = r10
        L_0x03bb:
            java.io.PrintStream r9 = java.lang.System.out
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r46 = r0
            java.lang.String r0 = java.lang.String.valueOf(r4)
            r10.<init>(r0)
            java.lang.String r0 = "**"
            r10.append(r0)
            r10.append(r5)
            java.lang.String r0 = r10.toString()
            r9.println(r0)
        L_0x03d7:
            r21 = r4
            r28 = r5
            r29 = r6
            r20 = r17
            goto L_0x03f3
        L_0x03e0:
            r45 = r10
            r21 = r4
            r29 = r6
            r20 = r17
            goto L_0x03f3
        L_0x03e9:
            r35 = r4
            r36 = r5
            r37 = r6
            r38 = r9
            r45 = r10
        L_0x03f3:
            int r15 = r15 + 1
            r4 = r35
            r5 = r36
            r9 = r38
            r10 = r45
            r6 = 0
            goto L_0x02ac
        L_0x0400:
            r35 = r4
            r36 = r5
            r38 = r9
        L_0x0406:
            r14 = r28
            r15 = r29
            goto L_0x0415
        L_0x040b:
            r35 = r4
            r36 = r5
            r38 = r9
            r14 = r28
            r15 = r29
        L_0x0415:
            r0 = 0
            java.util.ArrayList<java.lang.Integer> r4 = r1.match_index_2
            r4.clear()
            java.lang.String r4 = r12.group()
            java.lang.String[] r4 = r1.searSpot(r4, r8)
            if (r4 == 0) goto L_0x0574
            int r5 = r4.length
            java.util.ArrayList<java.lang.Integer> r0 = r1.match_index_2
            java.util.Iterator r6 = r0.iterator()
            r0 = 0
            r9 = r0
        L_0x042e:
            if (r9 >= r5) goto L_0x0564
            r0 = r4[r9]
            if (r0 != 0) goto L_0x0436
            goto L_0x0564
        L_0x0436:
            java.util.regex.Pattern r0 = r1.p_resultclean
            r10 = r4[r9]
            java.util.regex.Matcher r10 = r0.matcher(r10)
            boolean r0 = r10.matches()
            if (r0 == 0) goto L_0x0552
            r47 = r5
            r5 = 1
            java.lang.String r0 = r10.group(r5)
            if (r0 == 0) goto L_0x0462
            java.lang.String r0 = r10.group(r5)
            int r15 = r0.length()
            int r15 = r15 + r5
            r5 = r4[r9]
            int r5 = r5.length()
            int r5 = r5 - r15
            r20 = r15
            r15 = r5
            goto L_0x0474
        L_0x0462:
            r5 = 2
            java.lang.String r0 = r10.group(r5)
            int r5 = r0.length()
            r15 = r4[r9]
            int r15 = r15.length()
            int r15 = r15 - r5
            r20 = r5
        L_0x0474:
            java.util.regex.Pattern r5 = r1.pNum
            java.util.regex.Matcher r5 = r5.matcher(r0)
            boolean r17 = r5.lookingAt()
            if (r17 == 0) goto L_0x04a1
            r48 = r4
            java.lang.String r4 = r5.group()
            int r4 = r4.length()
            r49 = r8
            int r8 = r0.length()
            java.lang.String r0 = r0.substring(r4, r8)
            java.lang.String r4 = r5.group()
            int r4 = r4.length()
            int r15 = r15 + r4
            r21 = r15
            r15 = r0
            goto L_0x04a8
        L_0x04a1:
            r48 = r4
            r49 = r8
            r21 = r15
            r15 = r0
        L_0x04a8:
            boolean r0 = r6.hasNext()
            if (r0 == 0) goto L_0x0558
            java.lang.Object r0 = r6.next()
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r4 = r0.intValue()
            int r8 = r4 + r21
            int r0 = r15.length()
            int r14 = r8 + r0
            java.lang.String r0 = r2.substring(r8, r14)     // Catch:{ Exception -> 0x0530 }
            java.util.regex.Pattern r2 = r1.pDir     // Catch:{ Exception -> 0x0530 }
            java.util.regex.Matcher r2 = r2.matcher(r0)     // Catch:{ Exception -> 0x0530 }
            boolean r17 = r2.lookingAt()     // Catch:{ Exception -> 0x0530 }
            if (r17 == 0) goto L_0x04d8
            r15 = r0
            r52 = r0
            r50 = r2
            r54 = r4
            goto L_0x0515
        L_0x04d8:
            r50 = r2
            java.lang.String r2 = "((?:(?:[a-z][A-Za-z0-9]*)(?:\\s+|\\s*[,.]\\s*))+)([\\s\\S]+)"
            java.util.regex.Pattern r2 = java.util.regex.Pattern.compile(r2)     // Catch:{ Exception -> 0x0530 }
            java.util.regex.Matcher r17 = r2.matcher(r0)     // Catch:{ Exception -> 0x0530 }
            r51 = r17
            r52 = r0
            r0 = r51
            boolean r17 = r0.matches()     // Catch:{ Exception -> 0x0530 }
            if (r17 == 0) goto L_0x0513
            r53 = r2
            r54 = r4
            r2 = 1
            java.lang.String r4 = r0.group(r2)     // Catch:{ Exception -> 0x052e }
            int r4 = r4.length()     // Catch:{ Exception -> 0x052e }
            int r8 = r8 + r4
            java.lang.String r4 = r0.group(r2)     // Catch:{ Exception -> 0x052e }
            int r2 = r4.length()     // Catch:{ Exception -> 0x052e }
            int r4 = r15.length()     // Catch:{ Exception -> 0x052e }
            java.lang.String r2 = r15.substring(r2, r4)     // Catch:{ Exception -> 0x052e }
            r0 = r2
            r15 = r0
            goto L_0x0515
        L_0x0513:
            r54 = r4
        L_0x0515:
            java.util.regex.Matcher r0 = r11.matcher(r15)     // Catch:{ Exception -> 0x052e }
            boolean r2 = r0.matches()     // Catch:{ Exception -> 0x052e }
            if (r2 != 0) goto L_0x054f
            java.lang.Integer r2 = java.lang.Integer.valueOf(r8)     // Catch:{ Exception -> 0x052e }
            r3.add(r2)     // Catch:{ Exception -> 0x052e }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r14)     // Catch:{ Exception -> 0x052e }
            r3.add(r2)     // Catch:{ Exception -> 0x052e }
            goto L_0x054f
        L_0x052e:
            r0 = move-exception
            goto L_0x0533
        L_0x0530:
            r0 = move-exception
            r54 = r4
        L_0x0533:
            java.io.PrintStream r2 = java.lang.System.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r55 = r0
            java.lang.String r0 = java.lang.String.valueOf(r8)
            r4.<init>(r0)
            java.lang.String r0 = "**"
            r4.append(r0)
            r4.append(r14)
            java.lang.String r0 = r4.toString()
            r2.println(r0)
        L_0x054f:
            r21 = r8
            goto L_0x0558
        L_0x0552:
            r48 = r4
            r47 = r5
            r49 = r8
        L_0x0558:
            int r9 = r9 + 1
            r5 = r47
            r4 = r48
            r8 = r49
            r2 = r76
            goto L_0x042e
        L_0x0564:
            r6 = r30
            r4 = r31
            r10 = r32
            r9 = r33
            r8 = r34
            r5 = r36
            r2 = r76
            goto L_0x0103
        L_0x0574:
            r6 = r30
            r4 = r31
            r10 = r32
            r9 = r33
            r8 = r34
            r5 = r36
            goto L_0x0103
        L_0x0582:
            r14 = r28
            r15 = r29
            r6 = r30
            r4 = r31
            r10 = r32
            r9 = r33
            r8 = r34
            goto L_0x0103
        L_0x0592:
            r31 = r4
            r36 = r5
            r30 = r6
            r34 = r8
            r33 = r9
            r32 = r10
            java.lang.String r15 = ""
            java.util.regex.Pattern r2 = r1.pRoad
            java.lang.String r4 = r13.group()
            java.util.regex.Matcher r2 = r2.matcher(r4)
            boolean r4 = r2.matches()
            if (r4 != 0) goto L_0x0981
            r4 = 5
            java.lang.String r5 = r13.group(r4)
            if (r5 != 0) goto L_0x060a
            java.util.regex.Pattern r4 = r1.p_resultclean
            r5 = 1
            java.lang.String r6 = r13.group(r5)
            java.util.regex.Matcher r4 = r4.matcher(r6)
            boolean r6 = r4.matches()
            if (r6 == 0) goto L_0x0604
            java.lang.String r6 = r4.group(r5)
            if (r6 == 0) goto L_0x05de
            java.lang.String r6 = r4.group(r5)
            int r8 = r6.length()
            int r8 = r8 + r5
            r74 = r8
            r8 = r6
            r6 = r74
            goto L_0x05e7
        L_0x05de:
            r6 = 2
            java.lang.String r8 = r4.group(r6)
            int r6 = r8.length()
        L_0x05e7:
            int r9 = r13.start(r5)
            java.lang.String r10 = r13.group(r5)
            int r5 = r10.length()
            int r5 = r5 - r6
            int r9 = r9 + r5
            int r5 = r8.length()
            int r5 = r5 + r9
            r57 = r2
            r14 = r5
            r20 = r6
            r15 = r8
            r21 = r9
            goto L_0x0606
        L_0x0604:
            r57 = r2
        L_0x0606:
            r2 = r34
            goto L_0x08df
        L_0x060a:
            r4 = 6
            java.lang.String r5 = r13.group(r4)
            if (r5 == 0) goto L_0x0658
            java.util.regex.Pattern r4 = r1.p_resultclean
            java.lang.String r5 = r13.group()
            java.util.regex.Matcher r4 = r4.matcher(r5)
            boolean r5 = r4.matches()
            if (r5 == 0) goto L_0x0604
            r5 = 1
            java.lang.String r6 = r4.group(r5)
            if (r6 == 0) goto L_0x0632
            java.lang.String r6 = r4.group(r5)
            int r8 = r6.length()
            int r8 = r8 + r5
            goto L_0x063b
        L_0x0632:
            r5 = 2
            java.lang.String r6 = r4.group(r5)
            int r8 = r6.length()
        L_0x063b:
            int r5 = r13.start()
            java.lang.String r9 = r13.group()
            int r9 = r9.length()
            int r9 = r9 - r8
            int r5 = r5 + r9
            int r9 = r6.length()
            int r9 = r9 + r5
            r57 = r2
            r21 = r5
            r15 = r6
            r20 = r8
            r14 = r9
            goto L_0x0606
        L_0x0658:
            r4 = 5
            java.lang.String r5 = r13.group(r4)
            java.util.regex.Matcher r4 = r7.matcher(r5)
            boolean r5 = r4.matches()
            if (r5 == 0) goto L_0x0676
            r5 = 1
            java.lang.String r6 = r4.group(r5)
            if (r6 == 0) goto L_0x0673
            java.lang.String r6 = r4.group(r5)
            goto L_0x0678
        L_0x0673:
            java.lang.String r6 = ""
            goto L_0x0678
        L_0x0676:
            java.lang.String r6 = ""
        L_0x0678:
            r5 = 5
            java.lang.String r8 = r13.group(r5)
            int r9 = r6.length()
            java.lang.String r10 = r13.group(r5)
            int r5 = r10.length()
            java.lang.String r5 = r8.substring(r9, r5)
            r8 = 2
            java.lang.String r5 = r1.searCity(r5, r8)
            if (r5 != 0) goto L_0x07da
            java.lang.String r8 = "(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])"
            java.util.regex.Pattern r8 = java.util.regex.Pattern.compile(r8)
            r9 = 3
            java.lang.String r10 = r13.group(r9)
            java.util.regex.Matcher r9 = r8.matcher(r10)
            boolean r10 = r9.lookingAt()
            if (r10 == 0) goto L_0x070d
            java.util.regex.Pattern r10 = r1.p_resultclean
            java.lang.String r0 = r13.group()
            java.util.regex.Matcher r0 = r10.matcher(r0)
            boolean r10 = r0.matches()
            if (r10 == 0) goto L_0x06ff
            r10 = 1
            java.lang.String r17 = r0.group(r10)
            if (r17 == 0) goto L_0x06ce
            java.lang.String r15 = r0.group(r10)
            int r17 = r15.length()
            int r17 = r17 + 1
            goto L_0x06d7
        L_0x06ce:
            r10 = 2
            java.lang.String r15 = r0.group(r10)
            int r17 = r15.length()
        L_0x06d7:
            int r10 = r13.start()
            r56 = r0
            java.lang.String r0 = r13.group()
            int r0 = r0.length()
            int r0 = r0 - r17
            int r10 = r10 + r0
            int r0 = r15.length()
            int r0 = r0 + r10
            r14 = r0
            r57 = r2
            r27 = r5
            r19 = r6
            r21 = r10
            r20 = r17
            r2 = r34
            r4 = r56
            goto L_0x08df
        L_0x06ff:
            r56 = r0
            r57 = r2
            r27 = r5
            r19 = r6
            r2 = r34
            r4 = r56
            goto L_0x08df
        L_0x070d:
            r0 = 5
            java.lang.String r10 = r13.group(r0)
            r57 = r2
            r2 = r34
            java.util.regex.Matcher r0 = r2.matcher(r10)
            boolean r10 = r0.matches()
            if (r10 == 0) goto L_0x077c
            java.util.regex.Pattern r10 = r1.p_resultclean
            r58 = r0
            java.lang.String r0 = r13.group()
            java.util.regex.Matcher r0 = r10.matcher(r0)
            boolean r10 = r0.matches()
            if (r10 == 0) goto L_0x0772
            r10 = 1
            java.lang.String r17 = r0.group(r10)
            if (r17 == 0) goto L_0x0745
            java.lang.String r15 = r0.group(r10)
            int r17 = r15.length()
            int r17 = r17 + 1
            goto L_0x074e
        L_0x0745:
            r10 = 2
            java.lang.String r15 = r0.group(r10)
            int r17 = r15.length()
        L_0x074e:
            int r10 = r13.start()
            r59 = r0
            java.lang.String r0 = r13.group()
            int r0 = r0.length()
            int r0 = r0 - r17
            int r10 = r10 + r0
            int r0 = r15.length()
            int r0 = r0 + r10
            r14 = r0
            r27 = r5
            r19 = r6
            r21 = r10
            r20 = r17
            r4 = r59
            goto L_0x08df
        L_0x0772:
            r59 = r0
            r27 = r5
            r19 = r6
            r4 = r59
            goto L_0x08df
        L_0x077c:
            r58 = r0
            java.util.regex.Pattern r0 = r1.p_resultclean
            r60 = r4
            r10 = 1
            java.lang.String r4 = r13.group(r10)
            java.util.regex.Matcher r0 = r0.matcher(r4)
            r4 = r0
            boolean r0 = r4.matches()
            if (r0 == 0) goto L_0x07d2
            java.lang.String r0 = r4.group(r10)
            if (r0 == 0) goto L_0x07a2
            java.lang.String r0 = r4.group(r10)
            int r15 = r0.length()
            int r15 = r15 + r10
            goto L_0x07ab
        L_0x07a2:
            r10 = 2
            java.lang.String r0 = r4.group(r10)
            int r15 = r0.length()
        L_0x07ab:
            r10 = 1
            int r17 = r13.start(r10)
            r61 = r4
            java.lang.String r4 = r13.group(r10)
            int r4 = r4.length()
            int r4 = r4 - r15
            int r17 = r17 + r4
            int r4 = r0.length()
            int r4 = r17 + r4
            r14 = r4
            r27 = r5
            r19 = r6
            r20 = r15
            r21 = r17
            r4 = r61
            r15 = r0
            goto L_0x08df
        L_0x07d2:
            r61 = r4
            r27 = r5
            r19 = r6
            goto L_0x08df
        L_0x07da:
            r57 = r2
            r60 = r4
            r2 = r34
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r4 = java.lang.String.valueOf(r6)
            r0.<init>(r4)
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            r4 = 7
            java.lang.String r5 = r13.group(r4)
            if (r5 != 0) goto L_0x0813
            r4 = 4
            java.lang.String r5 = r13.group(r4)
            if (r5 == 0) goto L_0x0856
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r8 = r13.group(r4)
            java.lang.String r8 = java.lang.String.valueOf(r8)
            r5.<init>(r8)
            r5.append(r0)
            java.lang.String r0 = r5.toString()
            goto L_0x0856
        L_0x0813:
            r4 = 4
            java.lang.String r5 = r13.group(r4)
            if (r5 == 0) goto L_0x083c
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r8 = r13.group(r4)
            java.lang.String r4 = java.lang.String.valueOf(r8)
            r5.<init>(r4)
            r4 = 5
            java.lang.String r8 = r13.group(r4)
            r5.append(r8)
            r8 = 7
            java.lang.String r9 = r13.group(r8)
            r5.append(r9)
            java.lang.String r0 = r5.toString()
            goto L_0x0856
        L_0x083c:
            r4 = 5
            r8 = 7
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r9 = r13.group(r4)
            java.lang.String r4 = java.lang.String.valueOf(r9)
            r5.<init>(r4)
            java.lang.String r4 = r13.group(r8)
            r5.append(r4)
            java.lang.String r0 = r5.toString()
        L_0x0856:
            java.util.regex.Pattern r4 = r1.p_resultclean
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r8 = 1
            java.lang.String r9 = r13.group(r8)
            java.lang.String r8 = java.lang.String.valueOf(r9)
            r5.<init>(r8)
            r8 = 3
            java.lang.String r9 = r13.group(r8)
            r5.append(r9)
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            java.util.regex.Matcher r4 = r4.matcher(r5)
            boolean r5 = r4.matches()
            if (r5 == 0) goto L_0x08d9
            r5 = 1
            java.lang.String r8 = r4.group(r5)
            if (r8 == 0) goto L_0x0896
            java.lang.String r8 = r4.group(r5)
            int r9 = r8.length()
            int r9 = r9 + r5
            r74 = r9
            r9 = r8
            r8 = r74
            goto L_0x089f
        L_0x0896:
            r8 = 2
            java.lang.String r9 = r4.group(r8)
            int r8 = r9.length()
        L_0x089f:
            int r10 = r13.start(r5)
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r62 = r4
            java.lang.String r4 = r13.group(r5)
            java.lang.String r4 = java.lang.String.valueOf(r4)
            r15.<init>(r4)
            r4 = 3
            java.lang.String r5 = r13.group(r4)
            r15.append(r5)
            r15.append(r0)
            java.lang.String r4 = r15.toString()
            int r4 = r4.length()
            int r4 = r4 - r8
            int r10 = r10 + r4
            int r4 = r9.length()
            int r4 = r4 + r10
            r27 = r0
            r14 = r4
            r19 = r6
            r20 = r8
            r15 = r9
            r21 = r10
            r4 = r62
            goto L_0x08df
        L_0x08d9:
            r62 = r4
            r27 = r0
            r19 = r6
        L_0x08df:
            int r0 = r15.length()
            if (r0 <= 0) goto L_0x0972
            r5 = r33
            java.util.regex.Matcher r0 = r5.matcher(r15)
            boolean r6 = r0.find()
            if (r6 == 0) goto L_0x0910
            r6 = 2
            java.lang.String r8 = r0.group(r6)
            if (r8 != 0) goto L_0x090e
            int r6 = r15.length()
            r8 = 1
            java.lang.String r9 = r0.group(r8)
            int r9 = r9.length()
            int r6 = r6 - r9
            int r21 = r21 + r6
            java.lang.String r15 = r0.group(r8)
            goto L_0x0910
        L_0x090e:
            java.lang.String r15 = ""
        L_0x0910:
            r6 = r32
            java.util.regex.Matcher r8 = r6.matcher(r15)
            boolean r9 = r8.find()
            if (r9 == 0) goto L_0x094d
            r9 = 2
            java.lang.String r10 = r8.group(r9)
            if (r10 == 0) goto L_0x094a
            java.lang.String r10 = r8.group(r9)
            int r9 = r10.length()
            if (r9 <= 0) goto L_0x094a
            r9 = 1
            java.lang.String r10 = r8.group(r9)
            int r10 = r10.length()
            int r9 = r15.length()
            java.lang.String r9 = r15.substring(r10, r9)
            r10 = 1
            java.lang.String r15 = r8.group(r10)
            int r10 = r15.length()
            int r21 = r21 + r10
            goto L_0x094c
        L_0x094a:
            java.lang.String r9 = ""
        L_0x094c:
            r15 = r9
        L_0x094d:
            r9 = r21
            int r10 = r15.length()
            if (r10 <= 0) goto L_0x0963
            java.lang.Integer r10 = java.lang.Integer.valueOf(r9)
            r3.add(r10)
            java.lang.Integer r10 = java.lang.Integer.valueOf(r14)
            r3.add(r10)
        L_0x0963:
            r8 = r2
            r10 = r6
            r21 = r9
            r6 = r30
            r4 = r31
            r2 = r76
            r9 = r5
            r5 = r36
            goto L_0x00fd
        L_0x0972:
            r8 = r2
            r6 = r30
            r4 = r31
            r10 = r32
            r9 = r33
            r5 = r36
            r2 = r76
            goto L_0x00fd
        L_0x0981:
            r6 = r30
            r4 = r31
            r10 = r32
            r9 = r33
            r8 = r34
            r5 = r36
            r2 = r76
            goto L_0x00fd
        L_0x0991:
            r31 = r4
            r36 = r5
            r30 = r6
            r2 = r8
            r5 = r9
            r6 = r10
            java.lang.String r15 = ""
            java.util.regex.Pattern r0 = r1.pRoad
            java.lang.String r8 = r4.group()
            java.util.regex.Matcher r0 = r0.matcher(r8)
            boolean r8 = r0.matches()
            if (r8 != 0) goto L_0x0d85
            r8 = 5
            java.lang.String r9 = r4.group(r8)
            if (r9 != 0) goto L_0x0a12
            java.util.regex.Pattern r8 = r1.p_resultclean
            r9 = 1
            java.lang.String r10 = r4.group(r9)
            java.util.regex.Matcher r8 = r8.matcher(r10)
            boolean r10 = r8.matches()
            if (r10 == 0) goto L_0x0a08
            java.lang.String r10 = r8.group(r9)
            if (r10 == 0) goto L_0x09d4
            java.lang.String r10 = r8.group(r9)
            int r15 = r10.length()
            int r15 = r15 + r9
            goto L_0x09e2
        L_0x09d4:
            r10 = 2
            java.lang.String r15 = r8.group(r10)
            int r10 = r15.length()
            r74 = r15
            r15 = r10
            r10 = r74
        L_0x09e2:
            int r17 = r4.start(r9)
            r63 = r0
            java.lang.String r0 = r4.group(r9)
            int r0 = r0.length()
            int r0 = r0 - r15
            int r17 = r17 + r0
            int r0 = r10.length()
            int r0 = r17 + r0
            r14 = r0
            r68 = r2
            r65 = r7
            r20 = r15
            r21 = r17
            r0 = r27
        L_0x0a05:
            r15 = r10
            goto L_0x0ce7
        L_0x0a08:
            r63 = r0
            r68 = r2
            r65 = r7
            r0 = r27
            goto L_0x0ce7
        L_0x0a12:
            r63 = r0
            r0 = 6
            java.lang.String r8 = r4.group(r0)
            if (r8 == 0) goto L_0x0a6c
            java.util.regex.Pattern r0 = r1.p_resultclean
            java.lang.String r8 = r4.group()
            java.util.regex.Matcher r8 = r0.matcher(r8)
            boolean r0 = r8.matches()
            if (r0 == 0) goto L_0x0a64
            r9 = 1
            java.lang.String r0 = r8.group(r9)
            if (r0 == 0) goto L_0x0a3c
            java.lang.String r0 = r8.group(r9)
            int r10 = r0.length()
            int r10 = r10 + r9
            goto L_0x0a45
        L_0x0a3c:
            r9 = 2
            java.lang.String r0 = r8.group(r9)
            int r10 = r0.length()
        L_0x0a45:
            int r9 = r4.start()
            java.lang.String r15 = r4.group()
            int r15 = r15.length()
            int r15 = r15 - r10
            int r9 = r9 + r15
            int r15 = r0.length()
            int r15 = r15 + r9
            r68 = r2
            r65 = r7
            r21 = r9
            r20 = r10
            r14 = r15
            r15 = r0
            goto L_0x0a68
        L_0x0a64:
            r68 = r2
            r65 = r7
        L_0x0a68:
            r0 = r27
            goto L_0x0ce7
        L_0x0a6c:
            r0 = 5
            java.lang.String r8 = r4.group(r0)
            java.util.regex.Matcher r0 = r7.matcher(r8)
            boolean r8 = r0.matches()
            if (r8 == 0) goto L_0x0a8a
            r8 = 1
            java.lang.String r9 = r0.group(r8)
            if (r9 == 0) goto L_0x0a87
            java.lang.String r9 = r0.group(r8)
            goto L_0x0a8c
        L_0x0a87:
            java.lang.String r9 = ""
            goto L_0x0a8c
        L_0x0a8a:
            java.lang.String r9 = ""
        L_0x0a8c:
            r8 = 5
            java.lang.String r10 = r4.group(r8)
            r64 = r0
            int r0 = r9.length()
            r65 = r7
            java.lang.String r7 = r4.group(r8)
            int r7 = r7.length()
            java.lang.String r0 = r10.substring(r0, r7)
            r7 = 2
            java.lang.String r0 = r1.searCity(r0, r7)
            if (r0 != 0) goto L_0x0be6
            java.lang.String r7 = "(?<![a-zA-Z])(?:\\s*[,.]*\\s*)*(?:(?i)in)(?![a-zA-Z])"
            java.util.regex.Pattern r7 = java.util.regex.Pattern.compile(r7)
            r8 = 3
            java.lang.String r10 = r4.group(r8)
            java.util.regex.Matcher r8 = r7.matcher(r10)
            boolean r10 = r8.lookingAt()
            if (r10 == 0) goto L_0x0b1f
            java.util.regex.Pattern r10 = r1.p_resultclean
            r66 = r7
            java.lang.String r7 = r4.group()
            java.util.regex.Matcher r7 = r10.matcher(r7)
            boolean r10 = r7.matches()
            if (r10 == 0) goto L_0x0b15
            r10 = 1
            java.lang.String r17 = r7.group(r10)
            if (r17 == 0) goto L_0x0ae8
            java.lang.String r15 = r7.group(r10)
            int r17 = r15.length()
            int r17 = r17 + 1
            goto L_0x0af1
        L_0x0ae8:
            r10 = 2
            java.lang.String r15 = r7.group(r10)
            int r17 = r15.length()
        L_0x0af1:
            int r10 = r4.start()
            r67 = r7
            java.lang.String r7 = r4.group()
            int r7 = r7.length()
            int r7 = r7 - r17
            int r10 = r10 + r7
            int r7 = r15.length()
            int r7 = r7 + r10
            r68 = r2
            r14 = r7
            r19 = r9
            r21 = r10
            r20 = r17
            r8 = r67
            goto L_0x0ce7
        L_0x0b15:
            r67 = r7
            r68 = r2
            r19 = r9
            r8 = r67
            goto L_0x0ce7
        L_0x0b1f:
            r66 = r7
            r7 = 5
            java.lang.String r10 = r4.group(r7)
            java.util.regex.Matcher r7 = r2.matcher(r10)
            boolean r10 = r7.matches()
            if (r10 == 0) goto L_0x0b87
            java.util.regex.Pattern r10 = r1.p_resultclean
            r68 = r2
            java.lang.String r2 = r4.group()
            java.util.regex.Matcher r2 = r10.matcher(r2)
            boolean r10 = r2.matches()
            if (r10 == 0) goto L_0x0b7f
            r10 = 1
            java.lang.String r17 = r2.group(r10)
            if (r17 == 0) goto L_0x0b54
            java.lang.String r15 = r2.group(r10)
            int r17 = r15.length()
            int r17 = r17 + 1
            goto L_0x0b5d
        L_0x0b54:
            r10 = 2
            java.lang.String r15 = r2.group(r10)
            int r17 = r15.length()
        L_0x0b5d:
            int r10 = r4.start()
            r69 = r2
            java.lang.String r2 = r4.group()
            int r2 = r2.length()
            int r2 = r2 - r17
            int r10 = r10 + r2
            int r2 = r15.length()
            int r2 = r2 + r10
            r14 = r2
            r19 = r9
            r21 = r10
            r20 = r17
            r8 = r69
            goto L_0x0ce7
        L_0x0b7f:
            r69 = r2
            r19 = r9
            r8 = r69
            goto L_0x0ce7
        L_0x0b87:
            r68 = r2
            java.util.regex.Pattern r2 = r1.p_resultclean
            r70 = r7
            r10 = 1
            java.lang.String r7 = r4.group(r10)
            java.util.regex.Matcher r2 = r2.matcher(r7)
            boolean r7 = r2.matches()
            if (r7 == 0) goto L_0x0bde
            java.lang.String r7 = r2.group(r10)
            if (r7 == 0) goto L_0x0bac
            java.lang.String r7 = r2.group(r10)
            int r15 = r7.length()
            int r15 = r15 + r10
            goto L_0x0bba
        L_0x0bac:
            r7 = 2
            java.lang.String r15 = r2.group(r7)
            int r7 = r15.length()
            r74 = r15
            r15 = r7
            r7 = r74
        L_0x0bba:
            int r17 = r4.start(r10)
            r71 = r2
            java.lang.String r2 = r4.group(r10)
            int r2 = r2.length()
            int r2 = r2 - r15
            int r17 = r17 + r2
            int r2 = r7.length()
            int r2 = r17 + r2
            r14 = r2
            r19 = r9
            r20 = r15
            r21 = r17
            r8 = r71
            r15 = r7
            goto L_0x0ce7
        L_0x0bde:
            r71 = r2
            r19 = r9
            r8 = r71
            goto L_0x0ce7
        L_0x0be6:
            r68 = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r7 = java.lang.String.valueOf(r9)
            r2.<init>(r7)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            r2 = 7
            java.lang.String r7 = r4.group(r2)
            if (r7 != 0) goto L_0x0c1b
            r2 = 4
            java.lang.String r7 = r4.group(r2)
            if (r7 == 0) goto L_0x0c5e
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = r4.group(r2)
            java.lang.String r8 = java.lang.String.valueOf(r8)
            r7.<init>(r8)
            r7.append(r0)
            java.lang.String r0 = r7.toString()
            goto L_0x0c5e
        L_0x0c1b:
            r2 = 4
            java.lang.String r7 = r4.group(r2)
            if (r7 == 0) goto L_0x0c44
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = r4.group(r2)
            java.lang.String r2 = java.lang.String.valueOf(r8)
            r7.<init>(r2)
            r2 = 5
            java.lang.String r8 = r4.group(r2)
            r7.append(r8)
            r8 = 7
            java.lang.String r8 = r4.group(r8)
            r7.append(r8)
            java.lang.String r0 = r7.toString()
            goto L_0x0c5e
        L_0x0c44:
            r2 = 5
            r8 = 7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r10 = r4.group(r2)
            java.lang.String r2 = java.lang.String.valueOf(r10)
            r7.<init>(r2)
            java.lang.String r2 = r4.group(r8)
            r7.append(r2)
            java.lang.String r0 = r7.toString()
        L_0x0c5e:
            java.util.regex.Pattern r2 = r1.p_resultclean
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r8 = 1
            java.lang.String r10 = r4.group(r8)
            java.lang.String r8 = java.lang.String.valueOf(r10)
            r7.<init>(r8)
            r8 = 3
            java.lang.String r10 = r4.group(r8)
            r7.append(r10)
            r7.append(r0)
            java.lang.String r7 = r7.toString()
            java.util.regex.Matcher r8 = r2.matcher(r7)
            boolean r2 = r8.matches()
            if (r2 == 0) goto L_0x0ce1
            r2 = 1
            java.lang.String r7 = r8.group(r2)
            if (r7 == 0) goto L_0x0c9e
            java.lang.String r7 = r8.group(r2)
            int r10 = r7.length()
            int r10 = r10 + r2
            r74 = r10
            r10 = r7
            r7 = r74
            goto L_0x0ca7
        L_0x0c9e:
            r7 = 2
            java.lang.String r10 = r8.group(r7)
            int r7 = r10.length()
        L_0x0ca7:
            int r15 = r4.start(r2)
            r72 = r8
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r73 = r9
            java.lang.String r9 = r4.group(r2)
            java.lang.String r2 = java.lang.String.valueOf(r9)
            r8.<init>(r2)
            r2 = 3
            java.lang.String r9 = r4.group(r2)
            r8.append(r9)
            r8.append(r0)
            java.lang.String r2 = r8.toString()
            int r2 = r2.length()
            int r2 = r2 - r7
            int r15 = r15 + r2
            int r2 = r10.length()
            int r2 = r2 + r15
            r14 = r2
            r20 = r7
            r21 = r15
            r8 = r72
            r19 = r73
            goto L_0x0a05
        L_0x0ce1:
            r72 = r8
            r73 = r9
            r19 = r73
        L_0x0ce7:
            int r2 = r15.length()
            if (r2 <= 0) goto L_0x0d77
            java.util.regex.Matcher r2 = r5.matcher(r15)
            boolean r7 = r2.find()
            if (r7 == 0) goto L_0x0d16
            r7 = 2
            java.lang.String r9 = r2.group(r7)
            if (r9 != 0) goto L_0x0d14
            int r7 = r15.length()
            r9 = 1
            java.lang.String r10 = r2.group(r9)
            int r10 = r10.length()
            int r7 = r7 - r10
            int r21 = r21 + r7
            java.lang.String r15 = r2.group(r9)
            goto L_0x0d16
        L_0x0d14:
            java.lang.String r15 = ""
        L_0x0d16:
            java.util.regex.Matcher r7 = r6.matcher(r15)
            boolean r9 = r7.find()
            if (r9 == 0) goto L_0x0d51
            r9 = 2
            java.lang.String r10 = r7.group(r9)
            if (r10 == 0) goto L_0x0d4e
            java.lang.String r10 = r7.group(r9)
            int r9 = r10.length()
            if (r9 <= 0) goto L_0x0d4e
            r9 = 1
            java.lang.String r10 = r7.group(r9)
            int r10 = r10.length()
            int r9 = r15.length()
            java.lang.String r9 = r15.substring(r10, r9)
            r10 = 1
            java.lang.String r15 = r7.group(r10)
            int r10 = r15.length()
            int r21 = r21 + r10
            goto L_0x0d50
        L_0x0d4e:
            java.lang.String r9 = ""
        L_0x0d50:
            r15 = r9
        L_0x0d51:
            r9 = r21
            int r10 = r15.length()
            if (r10 <= 0) goto L_0x0d67
            java.lang.Integer r10 = java.lang.Integer.valueOf(r9)
            r3.add(r10)
            java.lang.Integer r10 = java.lang.Integer.valueOf(r14)
            r3.add(r10)
        L_0x0d67:
            r10 = r6
            r21 = r9
            r6 = r30
            r7 = r65
            r8 = r68
            r2 = r76
            r9 = r5
            r5 = r36
            goto L_0x00f5
        L_0x0d77:
            r9 = r5
            r10 = r6
            r6 = r30
            r5 = r36
            r7 = r65
            r8 = r68
            r2 = r76
            goto L_0x00f5
        L_0x0d85:
            r8 = r2
            r9 = r5
            r10 = r6
            r0 = r27
            r6 = r30
            r5 = r36
            r2 = r76
            goto L_0x00f5
        L_0x0d92:
            r24 = r0
            r36 = r5
            r30 = r6
            r65 = r7
            r68 = r8
            r5 = r9
            r6 = r10
            r25 = r15
            r2 = r30
            r7 = 1
            java.lang.String r0 = r2.group(r7)
            if (r0 != 0) goto L_0x0e48
            java.util.regex.Pattern r0 = r1.pCode_a
            java.lang.String r7 = r2.group()
            java.util.regex.Matcher r0 = r0.matcher(r7)
            boolean r7 = r0.find()
            if (r7 == 0) goto L_0x0e18
            r7 = 6
            java.lang.String r8 = r2.group(r7)
            r9 = 45
            int r8 = r8.indexOf(r9)
            r9 = -1
            if (r8 == r9) goto L_0x0de9
            int r8 = r2.start(r7)
            java.lang.String r7 = r2.group(r7)
            int r7 = r7.length()
            int r14 = r8 + r7
            java.lang.Integer r7 = java.lang.Integer.valueOf(r8)
            r3.add(r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r14)
            r3.add(r7)
        L_0x0de4:
            r9 = r5
            r10 = r6
            r21 = r8
            goto L_0x0e39
        L_0x0de9:
            r7 = 5
            java.lang.String r8 = r2.group(r7)
            if (r8 == 0) goto L_0x0eb4
            java.lang.String r7 = r2.group(r7)
            int r7 = r7.length()
            if (r7 <= 0) goto L_0x0eb4
            r7 = 6
            int r8 = r2.start(r7)
            java.lang.String r7 = r2.group(r7)
            int r7 = r7.length()
            int r14 = r8 + r7
            java.lang.Integer r7 = java.lang.Integer.valueOf(r8)
            r3.add(r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r14)
            r3.add(r7)
            goto L_0x0de4
        L_0x0e18:
            int r7 = r2.start()
            java.lang.String r8 = r2.group()
            int r8 = r8.length()
            int r14 = r7 + r8
            java.lang.Integer r8 = java.lang.Integer.valueOf(r7)
            r3.add(r8)
            java.lang.Integer r8 = java.lang.Integer.valueOf(r14)
            r3.add(r8)
            r9 = r5
            r10 = r6
            r21 = r7
        L_0x0e39:
            r0 = r24
            r15 = r25
        L_0x0e3d:
            r5 = r36
            r7 = r65
            r8 = r68
            r6 = r2
            r2 = r76
            goto L_0x00aa
        L_0x0e48:
            r7 = 4
            java.lang.String r0 = r2.group(r7)
            if (r0 == 0) goto L_0x0eb9
            java.util.regex.Pattern r0 = r1.p_resultclean
            java.lang.String r7 = r2.group()
            java.util.regex.Matcher r0 = r0.matcher(r7)
            boolean r7 = r0.matches()
            if (r7 == 0) goto L_0x0eb4
            r7 = 1
            java.lang.String r8 = r0.group(r7)
            if (r8 == 0) goto L_0x0e74
            java.lang.String r8 = r0.group(r7)
            int r9 = r8.length()
            int r9 = r9 + r7
            r15 = r8
            r20 = r9
            goto L_0x0e80
        L_0x0e74:
            r7 = 2
            java.lang.String r8 = r0.group(r7)
            int r7 = r8.length()
            r20 = r7
            r15 = r8
        L_0x0e80:
            int r7 = r2.start()
            java.lang.String r8 = r2.group()
            int r8 = r8.length()
            int r8 = r8 - r20
            int r7 = r7 + r8
            int r8 = r15.length()
            int r14 = r7 + r8
            java.lang.Integer r8 = java.lang.Integer.valueOf(r7)
            r3.add(r8)
            java.lang.Integer r8 = java.lang.Integer.valueOf(r14)
            r3.add(r8)
            r8 = 2
            java.lang.String r8 = r2.group(r8)
            if (r8 == 0) goto L_0x0ead
            r22 = 0
        L_0x0ead:
            r9 = r5
            r10 = r6
            r21 = r7
            r0 = r24
            goto L_0x0e3d
        L_0x0eb4:
            r9 = r5
            r10 = r6
            r14 = r23
            goto L_0x0e39
        L_0x0eb9:
            r8 = 2
            java.lang.String r0 = r2.group(r8)
            if (r0 == 0) goto L_0x0ecb
            java.util.regex.Pattern r0 = r1.p_resultclean
            java.lang.String r7 = r2.group()
            java.util.regex.Matcher r0 = r0.matcher(r7)
            goto L_0x0eff
        L_0x0ecb:
            r0 = 3
            java.lang.String r0 = r2.group(r0)
            r7 = 1
            java.lang.String r0 = r1.searCity(r0, r7)
            if (r0 != 0) goto L_0x0ed9
            java.lang.String r0 = ""
        L_0x0ed9:
            java.util.regex.Pattern r7 = r1.p_resultclean
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            java.lang.String r9 = java.lang.String.valueOf(r0)
            r8.<init>(r9)
            r9 = 5
            java.lang.String r10 = r2.group(r9)
            r8.append(r10)
            r9 = 6
            java.lang.String r10 = r2.group(r9)
            r8.append(r10)
            java.lang.String r8 = r8.toString()
            java.util.regex.Matcher r7 = r7.matcher(r8)
            r24 = r0
            r0 = r7
        L_0x0eff:
            boolean r7 = r0.matches()
            if (r7 == 0) goto L_0x0eb4
            r7 = 1
            java.lang.String r8 = r0.group(r7)
            if (r8 == 0) goto L_0x0f1a
            java.lang.String r8 = r0.group(r7)
            int r9 = r8.length()
            int r9 = r9 + r7
            r15 = r8
            r20 = r9
            goto L_0x0f26
        L_0x0f1a:
            r7 = 2
            java.lang.String r8 = r0.group(r7)
            int r7 = r8.length()
            r20 = r7
            r15 = r8
        L_0x0f26:
            r7 = 5
            int r8 = r2.start(r7)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            java.lang.String r7 = r2.group(r7)
            java.lang.String r7 = java.lang.String.valueOf(r7)
            r9.<init>(r7)
            r7 = 6
            java.lang.String r7 = r2.group(r7)
            r9.append(r7)
            java.lang.String r7 = r9.toString()
            int r7 = r7.length()
            int r7 = r7 - r20
            int r7 = r7 + r8
            int r8 = r15.length()
            int r14 = r7 + r8
            java.lang.Integer r8 = java.lang.Integer.valueOf(r7)
            r3.add(r8)
            java.lang.Integer r8 = java.lang.Integer.valueOf(r14)
            r3.add(r8)
            r8 = 2
            java.lang.String r8 = r2.group(r8)
            if (r8 == 0) goto L_0x0ead
            r22 = 0
            goto L_0x0ead
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.address.SerEn.search(java.lang.String):java.util.ArrayList");
    }

    private ArrayList<Match> createAddressResultData(int[] addrArray, String source) {
        if (addrArray.length == 0) {
            return null;
        }
        ArrayList<Match> matchedList = new ArrayList<>();
        int count = addrArray[0];
        for (int i = 1; i < (count * 2) + 1; i += 2) {
            Match mu = new Match();
            mu.setMatchedAddr(source.substring(addrArray[i], addrArray[i + 1]));
            mu.setStartPos(Integer.valueOf(addrArray[i]));
            mu.setEndPos(Integer.valueOf(addrArray[i + 1]));
            matchedList.add(mu);
        }
        return sortAndMergePosList(matchedList, source);
    }

    private String[] searSpot(String string, int head) {
        int position;
        String str;
        int length;
        int count;
        int count2;
        String s_right;
        int length_bracket;
        String cut;
        String cut2;
        String city;
        int count3;
        int count4;
        String cut3;
        int length2 = string.length();
        int head_0 = head;
        int i = 8;
        String[] results = new String[8];
        String str2 = string;
        Pattern pCut2 = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle2 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_city2 = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        int full = str2.length();
        int length_bracket2 = 0;
        int index = 0;
        Object obj = "";
        String cut4 = "";
        String building = "";
        String s_right2 = "";
        int i2 = head;
        while (index < length2) {
            String str3 = str2.substring(index, length2);
            int head2 = head_0 + (full - str3.length());
            int length3 = length2 - index;
            int index2 = 0;
            int head_02 = head_0;
            int position2 = DicSearch.dicsearch(2, str3.toLowerCase(Locale.getDefault()));
            if (position2 == 0) {
                while (index2 < length3 && ((str3.charAt(index2) >= 'a' && str3.charAt(index2) <= 'z') || ((str3.charAt(index2) >= 'A' && str3.charAt(index2) <= 'Z') || (str3.charAt(index2) >= '0' && str3.charAt(index2) <= '9')))) {
                    index2++;
                }
                length = length3;
                str = str3;
                position = position2;
                count = length_bracket2;
                count2 = 1;
            } else {
                String building2 = str3.substring(0, position2);
                String s_right3 = str3.substring(position2, str3.length());
                int length_bracket3 = searchBracket(s_right3);
                if (length_bracket3 > 0) {
                    length = length3;
                    building2 = String.valueOf(building2) + s_right3.substring(0, length_bracket3);
                    s_right3 = s_right3.substring(length_bracket3, s_right3.length());
                } else {
                    length = length3;
                }
                Matcher m52s = this.p52s.matcher(s_right3);
                String city2 = "";
                if (!m52s.lookingAt()) {
                    cut = "";
                    length_bracket = length_bracket3;
                    str = str3;
                    position = position2;
                    Matcher m2s = this.p2s.matcher(s_right3);
                    if (m2s.lookingAt()) {
                        if (m2s.group(3) == null) {
                            count4 = length_bracket2 + 1;
                            results[length_bracket2] = building2;
                            this.match_index_2.add(Integer.valueOf(head2));
                        } else if (m2s.group(4) != null) {
                            count4 = length_bracket2 + 1;
                            results[length_bracket2] = String.valueOf(building2) + m2s.group();
                            this.match_index_2.add(Integer.valueOf(head2));
                        } else {
                            Matcher mCut = pCut2.matcher(m2s.group(3));
                            if (!mCut.matches()) {
                                cut2 = "";
                            } else if (mCut.group(1) != null) {
                                cut2 = mCut.group(1);
                            } else {
                                cut2 = "";
                            }
                            s_right = s_right3;
                            Matcher matcher = mCut;
                            String city3 = searCity(m2s.group(3).substring(cut2.length(), m2s.group(3).length()), 2);
                            if (city3 != null) {
                                String city4 = String.valueOf(cut2) + city3;
                                if (m2s.group(6) == null) {
                                    if (m2s.group(2) != null) {
                                        city4 = String.valueOf(m2s.group(2)) + city4;
                                    }
                                } else if (m2s.group(2) == null) {
                                    city4 = String.valueOf(m2s.group(3)) + m2s.group(5) + m2s.group(6);
                                } else {
                                    city4 = String.valueOf(m2s.group(2)) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                                }
                                results[length_bracket2] = String.valueOf(building2) + city;
                                this.match_index_2.add(Integer.valueOf(head2));
                                city2 = String.valueOf(m2s.group(1)) + city4;
                                count = length_bracket2 + 1;
                                cut = cut2;
                            } else {
                                if (pPre_city2.matcher(m2s.group(1)).matches()) {
                                    count3 = length_bracket2 + 1;
                                    city = city3;
                                    results[length_bracket2] = String.valueOf(building2) + m2s.group();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                } else {
                                    city = city3;
                                    Matcher mSingle = pSingle2.matcher(m2s.group(3));
                                    if (mSingle.matches()) {
                                        count3 = length_bracket2 + 1;
                                        Matcher matcher2 = mSingle;
                                        results[length_bracket2] = String.valueOf(building2) + m2s.group();
                                        this.match_index_2.add(Integer.valueOf(head2));
                                    } else {
                                        results[length_bracket2] = building2;
                                        this.match_index_2.add(Integer.valueOf(head2));
                                        count3 = length_bracket2 + 1;
                                    }
                                }
                                cut = cut2;
                                city2 = city;
                            }
                        }
                        s_right = s_right3;
                        count = count4;
                    } else {
                        s_right = s_right3;
                        results[length_bracket2] = building2;
                        this.match_index_2.add(Integer.valueOf(head2));
                        count = length_bracket2 + 1;
                    }
                } else if (m52s.group(6) == null) {
                    cut = "";
                    results[length_bracket2] = String.valueOf(building2) + m52s.group();
                    this.match_index_2.add(Integer.valueOf(head2));
                    s_right = s_right3;
                    length_bracket = length_bracket3;
                    str = str3;
                    position = position2;
                    count = length_bracket2 + 1;
                } else {
                    cut = "";
                    if (m52s.group(7) != null) {
                        results[length_bracket2] = String.valueOf(building2) + m52s.group();
                        this.match_index_2.add(Integer.valueOf(head2));
                        s_right = s_right3;
                        length_bracket = length_bracket3;
                        str = str3;
                        position = position2;
                        count = length_bracket2 + 1;
                    } else {
                        Matcher mCut2 = pCut2.matcher(m52s.group(6));
                        if (!mCut2.matches()) {
                            cut3 = "";
                        } else if (mCut2.group(1) != null) {
                            cut3 = mCut2.group(1);
                        } else {
                            cut3 = "";
                        }
                        Matcher matcher3 = mCut2;
                        length_bracket = length_bracket3;
                        str = str3;
                        position = position2;
                        String city5 = searCity(m52s.group(6).substring(cut3.length(), m52s.group(6).length()), 2);
                        if (city5 == null) {
                            Matcher mPre_city = pPre_city2.matcher(m52s.group(4));
                            if (mPre_city.matches()) {
                                count = length_bracket2 + 1;
                                Matcher matcher4 = mPre_city;
                                results[length_bracket2] = String.valueOf(building2) + m52s.group();
                                this.match_index_2.add(Integer.valueOf(head2));
                            } else {
                                Matcher matcher5 = mPre_city;
                                Matcher mSingle2 = pSingle2.matcher(m52s.group(3));
                                if (mSingle2.matches()) {
                                    count = length_bracket2 + 1;
                                    Matcher matcher6 = mSingle2;
                                    results[length_bracket2] = String.valueOf(building2) + m52s.group();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                } else {
                                    results[length_bracket2] = String.valueOf(building2) + m52s.group(1) + m52s.group(2);
                                    this.match_index_2.add(Integer.valueOf(head2));
                                    cut = cut3;
                                    s_right = s_right3;
                                    city2 = city5;
                                    count = length_bracket2 + 1;
                                }
                            }
                            cut = cut3;
                            s_right = s_right3;
                            city2 = city5;
                        } else {
                            String city6 = String.valueOf(cut3) + city5;
                            if (m52s.group(8) == null) {
                                if (m52s.group(5) != null) {
                                    city6 = String.valueOf(m52s.group(5)) + city6;
                                }
                            } else if (m52s.group(5) == null) {
                                city6 = String.valueOf(m52s.group(6)) + m52s.group(8);
                            } else {
                                city6 = String.valueOf(m52s.group(5)) + m52s.group(6) + m52s.group(8);
                                results[length_bracket2] = String.valueOf(building2) + m52s.group(1) + m52s.group(2) + m52s.group(4) + city6;
                                this.match_index_2.add(Integer.valueOf(head2));
                                s_right = s_right3;
                                city2 = city6;
                                count = length_bracket2 + 1;
                                cut = cut3;
                            }
                            results[length_bracket2] = String.valueOf(building2) + m52s.group(1) + m52s.group(2) + m52s.group(4) + city6;
                            this.match_index_2.add(Integer.valueOf(head2));
                            s_right = s_right3;
                            city2 = city6;
                            count = length_bracket2 + 1;
                            cut = cut3;
                        }
                    }
                }
                count2 = 1;
                index2 = (results[count - 1].length() + 0) - 1;
                String str4 = city2;
                String str5 = cut;
                int i3 = length_bracket;
                String str6 = s_right;
            }
            index = index2 + count2;
            length_bracket2 = count;
            head_0 = head_02;
            length2 = length;
            str2 = str;
            int i4 = position;
            i = 8;
        }
        if (length_bracket2 >= i) {
            return results;
        }
        String[] re = new String[length_bracket2];
        for (int index3 = 0; index3 < length_bracket2; index3++) {
            re[index3] = results[index3];
        }
        return re;
    }

    private int max(int i, int j) {
        if (i > j) {
            return i;
        }
        return j;
    }

    public String[] searBuilding(String string, int head) {
        boolean flag = true;
        if (stanWri(string)) {
            flag = false;
        }
        return searBuilding_suf(string, "", 0, flag, head);
    }

    /* JADX WARNING: Removed duplicated region for block: B:120:0x049f  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x05b8  */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x07b8  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x07ed  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x08b2  */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x08dd  */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x0a54  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x0a8b  */
    /* JADX WARNING: Removed duplicated region for block: B:231:0x0a9d  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x0ab0  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x0abd A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x005d  */
    private String[] searBuilding_suf(String str, String sub_left, int left_state, boolean flag, int head) {
        String str2;
        int head2;
        Matcher mLocation;
        String[] results_2;
        String[] results_22;
        String[] results_3;
        String[] results_23;
        int head3;
        Matcher mLocation2;
        String cut;
        Matcher mComma;
        String sub2;
        int i;
        String[] results_32;
        int left_state2;
        String sub_left2;
        int count;
        String[] results_24;
        int count2;
        String[] results_25;
        String cut2;
        String city;
        String sub3;
        String cut3;
        String cut4;
        String city2;
        Matcher mPre_city;
        int count3;
        String sub32;
        String cut5;
        String[] results_26;
        String sub22;
        String cut6;
        String sub23;
        String sub1_temp;
        boolean sub1_undone;
        String str3 = str;
        int i2 = left_state;
        String[] results = new String[8];
        String[] results_27 = new String[0];
        String[] results_33 = new String[0];
        int count4 = 0;
        String sub24 = "";
        String sub_right = "";
        String building = "";
        Matcher mLocation3 = this.pNot_1.matcher(str3);
        if (mLocation3.lookingAt()) {
            Matcher mLocation4 = this.pNot_2.matcher(mLocation3.group(1));
            if (mLocation4.lookingAt()) {
                int n = mLocation4.group().length();
                str2 = str3.substring(n, str.length());
                head2 = head + n;
                mLocation = this.pLocation.matcher(str2);
                if (!mLocation.find()) {
                    String sub1 = mLocation.group(1);
                    Matcher mNo = this.pNo.matcher(sub1);
                    if (sub1.length() <= 0 || !noBlank(sub1)) {
                        Object obj = "";
                        String[] results_28 = results_27;
                        String[] results_34 = results_33;
                        String str4 = sub_right;
                        String sub12 = sub1;
                        int head4 = head2;
                        String sub_right2 = mLocation.group();
                        String sub_right3 = str2.substring(sub_right2.length(), str2.length());
                        if (noBlank(sub_right3)) {
                            results_2 = searBuilding_suf(sub_right3, sub_right2, 1, flag, head4 + (str2.length() - sub_right3.length()));
                            String str5 = sub12;
                            String str6 = sub24;
                            results_22 = results_34;
                            String str7 = sub_right3;
                            String str8 = sub_right2;
                        } else {
                            String str9 = sub12;
                            String str10 = sub24;
                            results_2 = results_28;
                            results_22 = results_34;
                            String str11 = sub_right3;
                            String str12 = sub_right2;
                        }
                    } else {
                        Matcher matcher = mNo;
                        if (!mNo.matches()) {
                            String cut7 = "";
                            results_23 = results_27;
                            results_3 = results_33;
                            cut = sub1;
                            mLocation2 = mLocation;
                            head3 = head2;
                        } else if (mLocation.group(3) != null) {
                            Object obj2 = "";
                            results_23 = results_27;
                            results_3 = results_33;
                            cut = sub1;
                            mLocation2 = mLocation;
                            head3 = head2;
                        } else {
                            String sub_left3 = mLocation.group();
                            String sub_right4 = str2.substring(sub_left3.length(), str2.length());
                            if (noBlank(sub_right4)) {
                                Object obj3 = "";
                                String[] strArr = results_27;
                                Matcher matcher2 = mLocation;
                                String[] results_35 = results_33;
                                int i3 = head2;
                                results_2 = searBuilding_suf(sub_right4, sub_left3, 1, flag, head2 + (str2.length() - sub_right4.length()));
                                String str13 = sub1;
                                String str14 = sub24;
                                String str15 = sub_right4;
                                results_22 = results_35;
                                String str16 = sub_left3;
                            } else {
                                Object obj4 = "";
                                String[] strArr2 = results_27;
                                String[] strArr3 = results_33;
                                Matcher matcher3 = mLocation;
                                int i4 = head2;
                                String str17 = sub1;
                                String str18 = sub24;
                                String str19 = sub_right4;
                                results_2 = strArr2;
                                results_22 = strArr3;
                                String str20 = sub_left3;
                            }
                        }
                        Matcher mComma2 = this.pComma.matcher(cut);
                        if (mComma2.find()) {
                            sub2 = mComma2.group(1);
                            if (sub2 != null && noBlank(sub2) && divStr(sub2).length <= 4) {
                                building = String.valueOf(sub2) + mLocation2.group(2);
                                this.match_index_2.add(Integer.valueOf(head3 + mComma2.start(1)));
                            }
                            if (building.length() != 0 || !flag) {
                                mComma = mComma2;
                                String str21 = sub_right;
                            } else {
                                String sub1_temp2 = cut;
                                boolean sub1_undone2 = true;
                                while (sub1_undone2) {
                                    boolean sub1_undone3 = sub1_undone2;
                                    Matcher mComma3 = mComma2;
                                    String sub25 = sub2;
                                    Matcher mPre_uni = this.pPre_uni.matcher(sub1_temp2);
                                    if (mPre_uni.find()) {
                                        sub2 = mPre_uni.group(2);
                                        if (sub2 == null || !noBlank(sub2)) {
                                            sub1_temp = sub1_temp2;
                                            sub1_undone = false;
                                        } else if (divStr(sub2).length <= 4) {
                                            StringBuilder sb = new StringBuilder(String.valueOf(sub2));
                                            sub1_temp = sub1_temp2;
                                            sb.append(mLocation2.group(2));
                                            building = sb.toString();
                                            this.match_index_2.add(Integer.valueOf(head3 + (cut.length() - sub2.length())));
                                            sub1_undone = false;
                                        } else {
                                            sub1_temp2 = sub2;
                                            sub1_undone2 = sub1_undone3;
                                            mComma2 = mComma3;
                                        }
                                        sub1_undone2 = sub1_undone;
                                        mComma2 = mComma3;
                                        sub1_temp2 = sub1_temp;
                                    } else {
                                        String str22 = sub1_temp2;
                                        sub1_undone2 = false;
                                        mComma2 = mComma3;
                                        sub2 = sub25;
                                    }
                                }
                                if (building.length() == 0) {
                                    String[] temp = divStr(cut);
                                    int length = temp.length;
                                    boolean z = sub1_undone2;
                                    if (length > 4) {
                                        mComma = mComma2;
                                        StringBuilder sb2 = new StringBuilder(String.valueOf(temp[length - 4]));
                                        sb2.append(temp[length - 3]);
                                        sb2.append(temp[length - 2]);
                                        sb2.append(temp[length - 1]);
                                        String[] strArr4 = temp;
                                        sb2.append(mLocation2.group(2));
                                        building = sb2.toString();
                                        sub23 = sub2;
                                        this.match_index_2.add(Integer.valueOf(head3 + (cut.length() - (building.length() - mLocation2.group(2).length()))));
                                    } else {
                                        String[] strArr5 = temp;
                                        mComma = mComma2;
                                        sub23 = sub2;
                                        if (length > 0) {
                                            building = String.valueOf(cut) + mLocation2.group(2);
                                        }
                                        this.match_index_2.add(Integer.valueOf(head3));
                                    }
                                    sub2 = sub23;
                                } else {
                                    mComma = mComma2;
                                    String str23 = sub2;
                                    String str24 = sub_right;
                                }
                            }
                        } else {
                            Matcher mComma4 = mComma2;
                            String sub1_temp3 = cut;
                            boolean sub1_undone4 = true;
                            while (sub1_undone4) {
                                boolean sub1_undone5 = sub1_undone4;
                                String sub13 = cut;
                                String sub26 = sub2;
                                String sub_right5 = sub_right;
                                Matcher mComma5 = mComma;
                                Matcher mPre_uni2 = this.pPre_uni.matcher(sub1_temp3);
                                if (mPre_uni2.find()) {
                                    sub24 = mPre_uni2.group(2);
                                    if (sub24 == null || !noBlank(sub24)) {
                                        cut6 = sub13;
                                        sub1_undone4 = false;
                                    } else if (divStr(sub24).length <= 4) {
                                        building = String.valueOf(sub24) + mLocation2.group(2);
                                        cut6 = sub13;
                                        this.match_index_2.add(Integer.valueOf(head3 + (cut6.length() - sub24.length())));
                                        sub1_undone4 = false;
                                    } else {
                                        cut6 = sub13;
                                        sub1_temp3 = sub24;
                                        mComma4 = mComma5;
                                        sub1_undone4 = sub1_undone5;
                                    }
                                    mComma4 = mComma5;
                                } else {
                                    cut6 = sub13;
                                    sub1_undone4 = false;
                                    mComma4 = mComma5;
                                    sub24 = sub26;
                                }
                                sub_right = sub_right5;
                            }
                            if (building.length() == 0) {
                                String[] temp2 = divStr(cut);
                                int length2 = temp2.length;
                                if (length2 > 4) {
                                    boolean z2 = sub1_undone4;
                                    StringBuilder sb3 = new StringBuilder(String.valueOf(temp2[length2 - 4]));
                                    sb3.append(temp2[length2 - 3]);
                                    sb3.append(temp2[length2 - 2]);
                                    sb3.append(temp2[length2 - 1]);
                                    String[] strArr6 = temp2;
                                    sb3.append(mLocation2.group(2));
                                    building = sb3.toString();
                                    sub22 = sub2;
                                    String str25 = sub_right;
                                    this.match_index_2.add(Integer.valueOf(head3 + (cut.length() - (building.length() - mLocation2.group(2).length()))));
                                } else {
                                    boolean z3 = sub1_undone4;
                                    String[] strArr7 = temp2;
                                    sub22 = sub2;
                                    String str26 = sub_right;
                                    if (length2 > 0) {
                                        building = String.valueOf(cut) + mLocation2.group(2);
                                    }
                                    this.match_index_2.add(Integer.valueOf(head3));
                                }
                                sub2 = sub22;
                            } else {
                                String str27 = sub_right;
                            }
                        }
                        if (building.length() == 0 && mLocation2.group(3) != null) {
                            String building2 = mLocation2.group(2);
                            this.match_index_2.add(Integer.valueOf(head3 + mLocation2.group(1).length()));
                            building = building2;
                        }
                        if (building.length() > 0) {
                            String sub27 = mLocation2.group();
                            if (sub27.length() > building.length()) {
                                i = sub27.length() - building.length();
                            } else {
                                i = 0;
                            }
                            int position = i;
                            String sub33 = sub27.substring(0, position);
                            if (i2 == 1) {
                                sub33 = String.valueOf(sub_left) + sub33;
                            }
                            if (noBlank(sub33)) {
                                left_state2 = 2;
                                results_32 = searBuilding_dic(sub33, head3 - sub_left.length());
                                sub_left2 = "";
                            } else {
                                sub_left2 = sub_left;
                                results_32 = results_3;
                                left_state2 = i2;
                            }
                            String sub34 = str2.substring(sub27.length(), str2.length());
                            if (noBlank(sub34)) {
                                Matcher m52s = this.p52s.matcher(sub34);
                                if (m52s.lookingAt()) {
                                    if (m52s.group(6) == null) {
                                        count2 = 0 + 1;
                                        results[0] = String.valueOf(building) + m52s.group();
                                        String sub35 = sub34.substring(m52s.group().length(), sub34.length());
                                        if (noBlank(sub35)) {
                                            int i5 = position;
                                            Matcher matcher4 = m52s;
                                            Matcher matcher5 = mComma;
                                            results_26 = searBuilding_suf(sub35, sub_left2, left_state2, flag, head3 + sub27.length() + m52s.group().length());
                                        }
                                        String sub14 = cut;
                                        String str28 = sub27;
                                    } else {
                                        int i6 = position;
                                        Matcher matcher6 = mComma;
                                        Matcher m52s2 = m52s;
                                        if (m52s2.group(7) != null) {
                                            count2 = 0 + 1;
                                            results[0] = String.valueOf(building) + m52s2.group();
                                            String sub36 = sub34.substring(m52s2.group().length(), sub34.length());
                                            if (noBlank(sub36)) {
                                                results_26 = searBuilding_suf(sub36, sub_left2, left_state2, flag, head3 + sub27.length() + m52s2.group().length());
                                            }
                                            String sub142 = cut;
                                            String str282 = sub27;
                                        } else {
                                            Matcher mCut = this.pCut.matcher(m52s2.group(6));
                                            if (!mCut.matches()) {
                                                cut5 = "";
                                            } else if (mCut.group(1) != null) {
                                                cut4 = mCut.group(1);
                                                String cut8 = cut4;
                                                city2 = searCity(m52s2.group(6).substring(cut4.length(), m52s2.group(6).length()), 2);
                                                if (city2 != null) {
                                                    Matcher mPre_city2 = this.pPre_city.matcher(m52s2.group(4));
                                                    if (mPre_city2.lookingAt()) {
                                                        count3 = 0 + 1;
                                                        results[0] = String.valueOf(building) + m52s2.group();
                                                        sub32 = sub34.substring(m52s2.group().length(), sub34.length());
                                                        mPre_city = mPre_city2;
                                                    } else {
                                                        Matcher mSingle = this.pSingle.matcher(m52s2.group(3));
                                                        if (mSingle.matches()) {
                                                            Matcher matcher7 = mSingle;
                                                            results[0] = String.valueOf(building) + m52s2.group();
                                                            sub32 = sub34.substring(m52s2.group().length(), sub34.length());
                                                            mPre_city = mPre_city2;
                                                            count3 = 0 + 1;
                                                        } else {
                                                            StringBuilder sb4 = new StringBuilder(String.valueOf(building));
                                                            mPre_city = mPre_city2;
                                                            sb4.append(m52s2.group(1));
                                                            sb4.append(m52s2.group(2));
                                                            results[0] = sb4.toString();
                                                            sub32 = sub34.substring(m52s2.group(1).length() + m52s2.group(2).length(), sub34.length());
                                                            count3 = 0 + 1;
                                                        }
                                                    }
                                                    if (noBlank(sub32)) {
                                                        Matcher matcher8 = mPre_city;
                                                        String str29 = cut;
                                                        String str30 = sub27;
                                                        Matcher matcher9 = mCut;
                                                        results_2 = searBuilding_suf(sub32, sub_left2, left_state2, flag, head3 + (str2.length() - sub32.length()));
                                                        String str31 = city2;
                                                        String str32 = cut8;
                                                        results_22 = results_32;
                                                        count4 = count3;
                                                    } else {
                                                        String sub15 = cut;
                                                        String str33 = sub27;
                                                        String str34 = city2;
                                                        String str35 = cut8;
                                                        results_2 = results_23;
                                                        results_22 = results_32;
                                                        count4 = count3;
                                                    }
                                                } else {
                                                    Matcher matcher10 = mCut;
                                                    String str36 = cut;
                                                    String str37 = sub27;
                                                    String cut9 = cut8;
                                                    String city3 = String.valueOf(cut9) + city2;
                                                    if (m52s2.group(8) == null) {
                                                        if (m52s2.group(5) != null) {
                                                            city3 = String.valueOf(m52s2.group(5)) + city3;
                                                        }
                                                    } else if (m52s2.group(5) == null) {
                                                        city3 = String.valueOf(m52s2.group(6)) + m52s2.group(8);
                                                    } else {
                                                        city3 = String.valueOf(m52s2.group(5)) + m52s2.group(6) + m52s2.group(8);
                                                    }
                                                    String city4 = city3;
                                                    int count5 = 0 + 1;
                                                    results[0] = String.valueOf(building) + m52s2.group(1) + m52s2.group(2) + m52s2.group(4) + city4;
                                                    String sub37 = sub34.substring(m52s2.group(1).length() + m52s2.group(2).length() + m52s2.group(4).length() + city4.length(), sub34.length());
                                                    if (noBlank(sub37)) {
                                                        results_2 = searBuilding_suf(sub37, sub_left2, left_state2, flag, head3 + (str2.length() - sub37.length()));
                                                        String str38 = cut9;
                                                        count4 = count5;
                                                    } else {
                                                        String str39 = cut9;
                                                        count4 = count5;
                                                        results_2 = results_23;
                                                    }
                                                    results_22 = results_32;
                                                }
                                            } else {
                                                cut5 = "";
                                            }
                                            cut4 = cut5;
                                            String cut82 = cut4;
                                            city2 = searCity(m52s2.group(6).substring(cut4.length(), m52s2.group(6).length()), 2);
                                            if (city2 != null) {
                                            }
                                        }
                                    }
                                    results_24 = results_26;
                                    String sub16 = cut;
                                    count = count2;
                                    results_22 = results_32;
                                } else {
                                    String sub17 = cut;
                                    String str40 = sub27;
                                    int i7 = position;
                                    Matcher matcher11 = mComma;
                                    Matcher matcher12 = m52s;
                                    Matcher m2s = this.p2s.matcher(sub34);
                                    if (!m2s.lookingAt()) {
                                        String sub38 = sub34;
                                        count2 = 0 + 1;
                                        results[0] = building;
                                        results_25 = searBuilding_suf(sub38, sub_left2, left_state2, flag, head3 + (str2.length() - sub38.length()));
                                    } else if (m2s.group(3) == null) {
                                        int count6 = 0 + 1;
                                        results[0] = building;
                                        if (noBlank(sub34)) {
                                            int count7 = count6;
                                            String str41 = sub34;
                                            results_2 = searBuilding_suf(sub34, sub_left2, left_state2, flag, head3 + (str2.length() - sub34.length()));
                                            results_22 = results_32;
                                            count4 = count7;
                                        } else {
                                            results_2 = results_23;
                                            results_22 = results_32;
                                            count4 = count6;
                                        }
                                    } else {
                                        String sub39 = sub34;
                                        if (m2s.group(4) != null) {
                                            count2 = 0 + 1;
                                            results[0] = String.valueOf(building) + m2s.group();
                                            String sub310 = sub39.substring(m2s.group().length(), sub39.length());
                                            if (noBlank(sub310)) {
                                                results_25 = searBuilding_suf(sub310, sub_left2, left_state2, flag, head3 + (str2.length() - sub310.length()));
                                            }
                                        } else {
                                            Matcher mCut2 = this.pCut.matcher(m2s.group(3));
                                            if (!mCut2.matches()) {
                                                cut3 = "";
                                            } else if (mCut2.group(1) != null) {
                                                cut2 = mCut2.group(1);
                                                if (searCity(m2s.group(3).substring(cut2.length(), m2s.group(3).length()), 2) == null) {
                                                    String city5 = String.valueOf(cut2) + city;
                                                    if (m2s.group(6) == null) {
                                                        if (m2s.group(2) != null) {
                                                            city5 = String.valueOf(m2s.group(2)) + city5;
                                                        }
                                                    } else if (m2s.group(2) == null) {
                                                        city5 = String.valueOf(m2s.group(3)) + m2s.group(5) + m2s.group(6);
                                                    } else {
                                                        city5 = String.valueOf(m2s.group(2)) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                                                    }
                                                    city = String.valueOf(m2s.group(1)) + city5;
                                                } else if (this.pPre_city.matcher(m2s.group(1)).lookingAt()) {
                                                    city = m2s.group();
                                                } else if (this.pSingle.matcher(m2s.group(3)).matches()) {
                                                    city = m2s.group();
                                                } else {
                                                    city = "";
                                                }
                                                String city6 = city;
                                                int count8 = 0 + 1;
                                                results[0] = String.valueOf(building) + city6;
                                                sub3 = sub39.substring(city6.length(), sub39.length());
                                                if (!noBlank(sub3)) {
                                                    String str42 = cut2;
                                                    Matcher matcher13 = mCut2;
                                                    results_24 = searBuilding_suf(sub3, sub_left2, left_state2, flag, head3 + (str2.length() - sub3.length()));
                                                    count = count8;
                                                    String str43 = city6;
                                                    results_22 = results_32;
                                                } else {
                                                    String str44 = cut2;
                                                    count4 = count8;
                                                    String str45 = city6;
                                                    results_2 = results_23;
                                                    results_22 = results_32;
                                                }
                                            } else {
                                                cut3 = "";
                                            }
                                            cut2 = cut3;
                                            if (searCity(m2s.group(3).substring(cut2.length(), m2s.group(3).length()), 2) == null) {
                                            }
                                            String city62 = city;
                                            int count82 = 0 + 1;
                                            results[0] = String.valueOf(building) + city62;
                                            sub3 = sub39.substring(city62.length(), sub39.length());
                                            if (!noBlank(sub3)) {
                                            }
                                        }
                                    }
                                    results_24 = results_25;
                                    count = count2;
                                    results_22 = results_32;
                                }
                                count = count2;
                            } else {
                                String sub18 = cut;
                                String str46 = sub27;
                                int i8 = position;
                                Matcher matcher14 = mComma;
                                String sub28 = sub34;
                                results[0] = building;
                                count = 0 + 1;
                            }
                            results_24 = results_23;
                            results_22 = results_32;
                        } else {
                            String sub19 = cut;
                            Matcher matcher15 = mComma;
                            String sub_left4 = mLocation2.group();
                            String sub_right6 = str2.substring(sub_left4.length(), str2.length());
                            if (noBlank(sub_right6)) {
                                results_2 = searBuilding_suf(sub_right6, sub_left4, 1, flag, head3 + (str2.length() - sub_right6.length()));
                                String str47 = sub2;
                                String str48 = sub_left4;
                                String str49 = sub_right6;
                                results_22 = results_3;
                            } else {
                                String str50 = sub2;
                                String str51 = sub_left4;
                                String str52 = sub_right6;
                                results_2 = results_23;
                                results_22 = results_3;
                            }
                        }
                    }
                } else {
                    String cut10 = "";
                    String[] strArr8 = results_27;
                    String[] results_36 = results_33;
                    String str53 = sub_right;
                    Matcher matcher16 = mLocation;
                    int head5 = head2;
                    if (i2 == 1) {
                        str2 = String.valueOf(sub_left) + str2;
                    }
                    String str54 = sub_left;
                    Object obj5 = "";
                    String str55 = sub24;
                    int i9 = i2;
                    results_2 = searBuilding_dic(str2, head5 - sub_left.length());
                    results_22 = results_36;
                    int i10 = i9;
                }
                if (results_22.length > 0) {
                    int index = 0;
                    while (index < results_22.length) {
                        results[count4] = results_22[index];
                        index++;
                        count4++;
                    }
                }
                if (results_2.length > 0) {
                    int index2 = 0;
                    while (index2 < results_2.length) {
                        results[count4] = results_2[index2];
                        index2++;
                        count4++;
                    }
                }
                if (count4 < 8) {
                    return results;
                }
                String[] re = new String[count4];
                for (int index3 = 0; index3 < count4; index3++) {
                    re[index3] = results[index3];
                }
                return re;
            }
        }
        head2 = head;
        str2 = str3;
        mLocation = this.pLocation.matcher(str2);
        if (!mLocation.find()) {
        }
        if (results_22.length > 0) {
        }
        if (results_2.length > 0) {
        }
        if (count4 < 8) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:116:0x058b  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x05b8  */
    private String[] searBuilding_dic(String string, int head) {
        Pattern pCut2;
        int position;
        int length;
        int count;
        String str;
        int length2;
        String s_right;
        int index;
        String str2;
        String cut;
        int count2;
        Pattern pCut3;
        String cut2;
        int count3;
        String cut3;
        int count4;
        int length3 = string.length();
        int head_0 = head;
        String[] results = new String[8];
        String str3 = string;
        Pattern pPre_building = Pattern.compile("[\\s\\S]*(?<![a-zA-Z])((?i)(in|at|from|near|to|reach))\\b(\\s+(?i)the\\b)?(?:(?:(?:\\s*[,.-:'\"()]\\s*)+)|\\s+)?");
        Pattern pCut4 = Pattern.compile("(\\s*[,.]?\\s*(?:(?i)(?:in|on|at|from|of)\\s+)?(?:(?i)(uptown|downtown)\\s+)?)?[\\s\\S]*");
        Pattern pSingle2 = Pattern.compile("(?:\\.)?\\s*,\\s*[A-Z][a-z]+(?:\\s*(?:[,.)\"'])\\s*)*");
        Pattern pPre_city2 = Pattern.compile("(?:\\s*(?:,|\\.){0,2}\\s*\\b(?i)(?:in)\\b(.*))");
        int full = str3.length();
        boolean flag = true;
        int count5 = 0;
        Object obj = "";
        int index2 = 0;
        String city = "";
        Object obj2 = "";
        int head2 = head;
        String cut4 = "";
        String s_left = "";
        String string2 = string;
        while (index2 < length3) {
            String str4 = str3.substring(index2, length3);
            head2 = (full - str4.length()) + head_0;
            int length4 = length3 - index2;
            int index3 = 0;
            int head_02 = head_0;
            String str5 = s_left;
            String s_left2 = string2.substring(0, string2.length() - length4);
            String string3 = string2;
            int position2 = DicSearch.dicsearch(1, str4.toLowerCase(Locale.getDefault()));
            if (position2 == 0) {
                while (index3 < length4 && ((str4.charAt(index3) >= 'a' && str4.charAt(index3) <= 'z') || ((str4.charAt(index3) >= 'A' && str4.charAt(index3) <= 'Z') || (str4.charAt(index3) >= '0' && str4.charAt(index3) <= '9')))) {
                    index3++;
                }
                length = length4;
                count = count5;
                str = str4;
                position = position2;
                pCut2 = pCut4;
                string2 = string3;
            } else {
                String building = str4.substring(0, position2);
                String s_right2 = str4.substring(position2, str4.length());
                length = length4;
                int length5 = searchBracket(s_right2);
                if (length5 > 0) {
                    String str6 = city;
                    position = position2;
                    building = String.valueOf(building) + s_right2.substring(0, length5);
                    s_right2 = s_right2.substring(length5, s_right2.length());
                } else {
                    position = position2;
                }
                int length_bracket = length5;
                Matcher m52s = this.p52s.matcher(s_right2);
                String city2 = "";
                if (m52s.lookingAt()) {
                    if (m52s.group(6) == null) {
                        cut = "";
                        results[count5] = String.valueOf(building) + m52s.group();
                        this.match_index_2.add(Integer.valueOf(head2));
                        index = 0;
                        Matcher matcher = m52s;
                        s_right = s_right2;
                        str2 = str4;
                        pCut2 = pCut4;
                        count2 = count5 + 1;
                    } else {
                        cut = "";
                        if (m52s.group(7) != null) {
                            results[count5] = String.valueOf(building) + m52s.group();
                            this.match_index_2.add(Integer.valueOf(head2));
                            index = 0;
                            Matcher matcher2 = m52s;
                            s_right = s_right2;
                            str2 = str4;
                            pCut2 = pCut4;
                            count2 = count5 + 1;
                        } else {
                            Matcher mCut = pCut4.matcher(m52s.group(6));
                            if (!mCut.matches()) {
                                cut3 = "";
                            } else if (mCut.group(1) != null) {
                                cut3 = mCut.group(1);
                            } else {
                                cut3 = "";
                            }
                            str2 = str4;
                            Matcher matcher3 = mCut;
                            index = 0;
                            pCut3 = pCut4;
                            String city3 = searCity(m52s.group(6).substring(cut3.length(), m52s.group(6).length()), 2);
                            if (city3 == null) {
                                Matcher mPre_city = pPre_city2.matcher(m52s.group(4));
                                if (mPre_city.matches()) {
                                    count4 = count5 + 1;
                                    Matcher matcher4 = mPre_city;
                                    results[count5] = String.valueOf(building) + m52s.group();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                } else {
                                    Matcher matcher5 = mPre_city;
                                    Matcher mSingle = pSingle2.matcher(m52s.group(3));
                                    if (mSingle.matches()) {
                                        count4 = count5 + 1;
                                        Matcher matcher6 = mSingle;
                                        results[count5] = String.valueOf(building) + m52s.group();
                                        this.match_index_2.add(Integer.valueOf(head2));
                                    } else {
                                        results[count5] = String.valueOf(building) + m52s.group(1) + m52s.group(2);
                                        this.match_index_2.add(Integer.valueOf(head2));
                                        city2 = city3;
                                        Matcher matcher7 = m52s;
                                        s_right = s_right2;
                                        cut = cut3;
                                        pCut2 = pCut3;
                                        count2 = count5 + 1;
                                    }
                                }
                                city2 = city3;
                                s_right = s_right2;
                                cut = cut3;
                                count2 = count4;
                            } else {
                                String city4 = String.valueOf(cut3) + city3;
                                if (m52s.group(8) == null) {
                                    if (m52s.group(5) != null) {
                                        city4 = String.valueOf(m52s.group(5)) + city4;
                                    }
                                } else if (m52s.group(5) == null) {
                                    city4 = String.valueOf(m52s.group(6)) + m52s.group(8);
                                } else {
                                    city4 = String.valueOf(m52s.group(5)) + m52s.group(6) + m52s.group(8);
                                    results[count5] = String.valueOf(building) + m52s.group(1) + m52s.group(2) + m52s.group(4) + city4;
                                    this.match_index_2.add(Integer.valueOf(head2));
                                    city2 = city4;
                                    Matcher matcher8 = m52s;
                                    s_right = s_right2;
                                    count2 = count5 + 1;
                                    pCut2 = pCut3;
                                    cut = cut3;
                                }
                                results[count5] = String.valueOf(building) + m52s.group(1) + m52s.group(2) + m52s.group(4) + city4;
                                this.match_index_2.add(Integer.valueOf(head2));
                                city2 = city4;
                                Matcher matcher82 = m52s;
                                s_right = s_right2;
                                count2 = count5 + 1;
                                pCut2 = pCut3;
                                cut = cut3;
                            }
                        }
                    }
                    if (flag) {
                        index3 = (index + results[count2 - 1].length()) - 1;
                        str = str2;
                        String str7 = building;
                        int i = length_bracket;
                        city = city2;
                        String str8 = cut;
                        String str9 = s_right;
                        count = count2;
                        string2 = str.substring(results[count2 - 1].length(), str.length());
                    } else {
                        str = str2;
                        length2 = 1;
                        index3 = (index + building.length()) - 1;
                        String str10 = building;
                        flag = true;
                        int i2 = length_bracket;
                        String str11 = cut;
                        String str12 = s_right;
                        count = count2;
                        string2 = str.substring(building.length(), str.length());
                        city = city2;
                        index2 = index3 + length2;
                        str3 = str;
                        count5 = count;
                        length3 = length;
                        int i3 = position;
                        pCut4 = pCut2;
                        s_left = s_left2;
                        head_0 = head_02;
                    }
                } else {
                    index = 0;
                    str2 = str4;
                    cut = "";
                    pCut3 = pCut4;
                    Matcher m2s = this.p2s.matcher(s_right2);
                    if (!m2s.lookingAt()) {
                        s_right = s_right2;
                        pCut2 = pCut3;
                        if (pPre_building.matcher(s_left2).matches()) {
                            count2 = count5 + 1;
                            results[count5] = building;
                            this.match_index_2.add(Integer.valueOf(head2));
                            if (flag) {
                            }
                        } else {
                            flag = false;
                        }
                    } else if (m2s.group(3) == null) {
                        if (pPre_building.matcher(s_left2).matches()) {
                            results[count5] = building;
                            this.match_index_2.add(Integer.valueOf(head2));
                            Matcher matcher9 = m52s;
                            s_right = s_right2;
                            count2 = count5 + 1;
                        } else {
                            flag = false;
                            Matcher matcher10 = m52s;
                            s_right = s_right2;
                            pCut2 = pCut3;
                        }
                    } else if (m2s.group(4) != null) {
                        results[count5] = String.valueOf(building) + m2s.group();
                        this.match_index_2.add(Integer.valueOf(head2));
                        Matcher matcher11 = m52s;
                        s_right = s_right2;
                        count2 = count5 + 1;
                    } else {
                        Pattern pCut5 = pCut3;
                        Matcher mCut2 = pCut5.matcher(m2s.group(3));
                        if (!mCut2.matches()) {
                            cut2 = "";
                        } else if (mCut2.group(1) != null) {
                            cut2 = mCut2.group(1);
                        } else {
                            cut2 = "";
                        }
                        Matcher matcher12 = m52s;
                        s_right = s_right2;
                        pCut2 = pCut5;
                        String city5 = searCity(m2s.group(3).substring(cut2.length(), m2s.group(3).length()), 2);
                        if (city5 != null) {
                            String city6 = String.valueOf(cut2) + city5;
                            if (m2s.group(6) == null) {
                                if (m2s.group(2) != null) {
                                    city6 = String.valueOf(m2s.group(2)) + city6;
                                }
                            } else if (m2s.group(2) == null) {
                                city6 = String.valueOf(m2s.group(3)) + m2s.group(5) + m2s.group(6);
                            } else {
                                city6 = String.valueOf(m2s.group(2)) + m2s.group(3) + m2s.group(5) + m2s.group(6);
                            }
                            city5 = String.valueOf(m2s.group(1)) + city6;
                            results[count5] = String.valueOf(building) + city5;
                            this.match_index_2.add(Integer.valueOf(head2));
                            count3 = count5 + 1;
                        } else {
                            Matcher mPre_city2 = pPre_city2.matcher(m2s.group(1));
                            if (mPre_city2.matches()) {
                                count3 = count5 + 1;
                                Matcher matcher13 = mPre_city2;
                                results[count5] = String.valueOf(building) + m2s.group();
                                this.match_index_2.add(Integer.valueOf(head2));
                            } else {
                                Matcher matcher14 = mPre_city2;
                                Matcher mSingle2 = pSingle2.matcher(m2s.group(3));
                                if (mSingle2.matches()) {
                                    count3 = count5 + 1;
                                    Matcher matcher15 = mSingle2;
                                    results[count5] = String.valueOf(building) + m2s.group();
                                    this.match_index_2.add(Integer.valueOf(head2));
                                } else {
                                    if (pPre_building.matcher(s_left2).matches()) {
                                        count3 = count5 + 1;
                                        results[count5] = building;
                                        this.match_index_2.add(Integer.valueOf(head2));
                                    } else {
                                        flag = false;
                                        count3 = count5;
                                    }
                                }
                            }
                        }
                        city2 = city5;
                        cut = cut2;
                        if (flag) {
                        }
                    }
                    count2 = count5;
                    if (flag) {
                    }
                }
                pCut2 = pCut3;
                if (flag) {
                }
            }
            length2 = 1;
            index2 = index3 + length2;
            str3 = str;
            count5 = count;
            length3 = length;
            int i32 = position;
            pCut4 = pCut2;
            s_left = s_left2;
            head_0 = head_02;
        }
        if (count5 < 8) {
            String[] re = new String[count5];
            int i4 = head2;
            for (int index4 = 0; index4 < count5; index4++) {
                re[index4] = results[index4];
            }
            return re;
        }
        return results;
    }

    private boolean noBlank(String str) {
        int n = str.length();
        String str2 = str.toLowerCase(Locale.getDefault());
        boolean flag = true;
        int index = 0;
        while (flag && index < n) {
            if ((str2.charAt(index) <= 'z' && str2.charAt(index) >= 'a') || (str2.charAt(index) <= '9' && str2.charAt(index) >= '0')) {
                flag = false;
            }
            index++;
        }
        return !flag;
    }

    private String[] divStr(String str) {
        int i;
        String[] strs = new String[150];
        int length = str.length();
        int pr = 0;
        strs[0] = "";
        for (int index = 0; index < length; index++) {
            char letter = str.charAt(index);
            if ((letter <= 'z' && letter >= 'a') || ((letter <= 'Z' && letter >= 'A') || (letter <= '9' && letter >= '0'))) {
                strs[pr] = String.valueOf(strs[pr]) + letter;
            } else if (strs[pr].length() > 0) {
                strs[pr] = String.valueOf(strs[pr]) + letter;
                pr++;
                strs[pr] = "";
            } else if (pr > 0) {
                strs[i] = String.valueOf(strs[pr - 1]) + letter;
            }
        }
        if (strs[pr].length() > 0) {
            pr++;
        }
        if (pr >= 150) {
            return strs;
        }
        String[] re = new String[pr];
        for (int index2 = 0; index2 < pr; index2++) {
            re[index2] = strs[index2];
        }
        return re;
    }

    private boolean stanWri(String str) {
        String[] strs = divStr(str);
        int length = strs.length;
        boolean flag = true;
        int index = 0;
        while (flag && index < length) {
            int length_2 = strs[index].length();
            int index_2 = 1;
            while (flag && index_2 < length_2) {
                char letter = strs[index].charAt(index_2);
                if (letter <= 'Z' && letter >= 'A') {
                    flag = false;
                }
                index_2++;
            }
            if (length > 3) {
                if (index == 0) {
                    index = (length / 2) - 1;
                } else if (index == (length / 2) - 1) {
                    index = length - 2;
                }
            }
            index++;
        }
        return flag;
    }

    public String searCity(String string, int mode) {
        int length = string.length();
        String str = string;
        Matcher mCity = Pattern.compile("([\\s\\S]*(?i)(town|city|county)\\b)(?:.*)").matcher(str);
        if (mode == 1) {
            if (mCity.find() && noBlank(mCity.group(1).substring(0, mCity.group(2).length()))) {
                return str;
            }
            int index = 0;
            while (index < length) {
                str = str.substring(index, length);
                length -= index;
                int index2 = 0;
                if (DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault())) != 0) {
                    return str;
                }
                while (index2 < length && ((str.charAt(index2) >= 'a' && str.charAt(index2) <= 'z') || ((str.charAt(index2) >= 'A' && str.charAt(index2) <= 'Z') || (str.charAt(index2) >= '0' && str.charAt(index2) <= '9')))) {
                    index2++;
                }
                index = index2 + 1;
            }
        } else if (mCity.find() && noBlank(mCity.group(1).substring(0, mCity.group(2).length()))) {
            return mCity.group(1);
        } else {
            int position = DicSearch.dicsearch(0, str.toLowerCase(Locale.getDefault()));
            if (position > 0) {
                Matcher mCity2 = Pattern.compile("(\\s+(?i)(town|city|county))\\b.*").matcher(str.substring(position, length));
                if (!mCity2.matches()) {
                    return str.substring(0, position);
                }
                return String.valueOf(str.substring(0, position)) + mCity2.group(1);
            }
        }
        return null;
    }

    public int searchBracket(String str) {
        Matcher mBracket = Pattern.compile("(\\s*.?\\s*)\\)").matcher(str);
        if (mBracket.lookingAt()) {
            return mBracket.group().length();
        }
        return 0;
    }

    public String noShut(String str) {
        Matcher mShut = Pattern.compile("\\s*#").matcher(str);
        if (mShut.lookingAt()) {
            return str.substring(mShut.group().length(), str.length());
        }
        return str;
    }

    private ArrayList<Match> sortAndMergePosList(ArrayList<Match> posList, String sourceTxt) {
        if (posList.isEmpty()) {
            return null;
        }
        Collections.sort(posList, new Comparator<Match>() {
            public int compare(Match p1, Match p2) {
                if (p1.getStartPos().compareTo(p2.getStartPos()) == 0) {
                    return p1.getEndPos().compareTo(p2.getEndPos());
                }
                return p1.getStartPos().compareTo(p2.getStartPos());
            }
        });
        for (int i = posList.size() - 1; i > 0; i--) {
            if (posList.get(i - 1).getStartPos().intValue() <= posList.get(i).getStartPos().intValue() && posList.get(i).getStartPos().intValue() <= posList.get(i - 1).getEndPos().intValue()) {
                if (posList.get(i - 1).getEndPos().intValue() < posList.get(i).getEndPos().intValue()) {
                    posList.get(i - 1).setEndPos(posList.get(i).getEndPos());
                    posList.get(i - 1).setMatchedAddr(sourceTxt.substring(posList.get(i - 1).getStartPos().intValue(), posList.get(i - 1).getEndPos().intValue()));
                    posList.remove(i);
                } else if (posList.get(i - 1).getEndPos().intValue() >= posList.get(i).getEndPos().intValue()) {
                    posList.remove(i);
                }
            }
        }
        return posList;
    }
}

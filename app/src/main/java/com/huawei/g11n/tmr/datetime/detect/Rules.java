package com.huawei.g11n.tmr.datetime.detect;

import com.huawei.connectivitylog.ConnectivityLogManager;
import huawei.android.provider.HwSettings.System;
import java.util.HashMap;

public class Rules {
    private HashMap<Integer, String> filterRegex;
    private HashMap<Integer, String> pastRegex;
    private HashMap<Integer, String> rules;
    private HashMap<String, String> subRules;

    /* renamed from: com.huawei.g11n.tmr.datetime.detect.Rules.2 */
    class AnonymousClass2 extends HashMap<Integer, String> {
        final /* synthetic */ Rules this$0;

        AnonymousClass2(com.huawei.g11n.tmr.datetime.detect.Rules r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.g11n.tmr.datetime.detect.Rules.2.<init>(com.huawei.g11n.tmr.datetime.detect.Rules):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.g11n.tmr.datetime.detect.Rules.2.<init>(com.huawei.g11n.tmr.datetime.detect.Rules):void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.datetime.detect.Rules.2.<init>(com.huawei.g11n.tmr.datetime.detect.Rules):void");
        }
    }

    /* renamed from: com.huawei.g11n.tmr.datetime.detect.Rules.3 */
    class AnonymousClass3 extends HashMap<Integer, String> {
        final /* synthetic */ Rules this$0;

        AnonymousClass3(Rules rules) {
            this.this$0 = rules;
            put(Integer.valueOf(1), "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
            put(Integer.valueOf(5), "[0-9][-0-9.,\\s]+[%\u2030\uff05\u2103\u5ea6]");
            put(Integer.valueOf(6), "[0-9]{1,2}\\s*(-|\\.|/)\\s*[0-9]{1,2}");
            put(Integer.valueOf(7), "(1[0-9]{3}|[a-z][0-9]{1,4})\\s*(-|\\.|/)\\s*[0-9]{1,2}\\s*\\2\\s*[0-9]{1,2}|[0-9]{1,2}\\s*(-|\\.|/)\\s*[0-9]{1,2}\\s*\\1\\s*(1[0-9]{3})");
            put(Integer.valueOf(8), "[0-9]{1,2}(-[0-9]{1,2}){4}");
            put(Integer.valueOf(9), "[0-9]+\\s*(\uc77c|\u6642|\u65f6|\u70b9|\u9ede|\\s+d\\.|a)");
            put(Integer.valueOf(10), "([0-9]+[param_digit])\\s*(\u53f7|\u65e5|\uc77c|\u6642|\u65f6|\u70b9|\u9ede|\\s+d\\.)");
            put(Integer.valueOf(11), "[param_filtertext]");
        }
    }

    /* renamed from: com.huawei.g11n.tmr.datetime.detect.Rules.4 */
    class AnonymousClass4 extends HashMap<Integer, String> {
        final /* synthetic */ Rules this$0;

        AnonymousClass4(Rules rules) {
            this.this$0 = rules;
            put(Integer.valueOf(100), "[param_pastForward]");
            put(Integer.valueOf(ConnectivityLogManager.WIFI_USER_CONNECT), "(?<![0-9])1[0-9]{3}\\s*[./-]");
        }
    }

    public Rules() {
        this.subRules = new HashMap<String, String>() {
            {
                put("hms", "(?<!\\d)(2[0-3]|[0-1]?[0-9])(([param_tmark])([0-5][0-9])){1,2}(?!\\d)");
                put("ampm", "[param_am]|[param_pm]|\\bAM\\b|\\bPM\\b|\\bnoon\\b|\\ba\\.m\\.|\\bp\\.m\\.");
                put("hms2", "(?<!\\d)(1[0-2]|0?[0-9])([:.]([0-5][0-9])){1,2}(?!\\d)");
                put("d", "(?<!\\d)(30|31|0?[1-9]|[1-2][0-9])(?!\\d)");
                put("y", "(?<!\\d)((20){0,1}[0-9]{2})(?!\\d)");
                put(System.FINGERSENSE_KNUCKLE_GESTURE_M_SUFFIX, "(?<!\\d)(1[0-2]|0{0,1}[1-9])(?!\\d)");
                put("zzzz", "((GMT[+-])|\\+)([0-1]?[0-9]|2[0-3])(:?[0-5][0-9])?");
            }
        };
        this.rules = new AnonymousClass2(this);
        this.filterRegex = new AnonymousClass3(this);
        this.pastRegex = new AnonymousClass4(this);
    }

    public HashMap<String, String> getSubRules() {
        return this.subRules;
    }

    public HashMap<Integer, String> getRules() {
        return this.rules;
    }

    public HashMap<Integer, String> getFilterRegex() {
        return this.filterRegex;
    }

    public HashMap<Integer, String> getPastRegex() {
        return this.pastRegex;
    }
}

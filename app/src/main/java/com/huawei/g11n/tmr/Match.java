package com.huawei.g11n.tmr;

import android.util.HwSecureWaterMark;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;

public class Match implements Comparable<Object> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    int begin;
    DatePeriod dp;
    int end;
    boolean isTimePeriod;
    String regex;
    int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.g11n.tmr.Match.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.g11n.tmr.Match.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.Match.<clinit>():void");
    }

    public void setIsTimePeriod(boolean z) {
        this.isTimePeriod = z;
    }

    public boolean isTimePeriod() {
        if (this.isTimePeriod) {
            return this.isTimePeriod;
        }
        if (this.regex == null || this.regex.trim().isEmpty()) {
            return false;
        }
        int parseInt = Integer.parseInt(this.regex);
        if (parseInt > 49999 && parseInt < HwSecureWaterMark.MAX_NUMER) {
            return true;
        }
        return false;
    }

    public Match(int i, int i2, String str) {
        this.isTimePeriod = false;
        this.begin = i;
        this.end = i2;
        this.regex = str;
    }

    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String str) {
        this.regex = str;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int i) {
        this.type = i;
    }

    public DatePeriod getDp() {
        return this.dp;
    }

    public void setDp(DatePeriod datePeriod) {
        this.dp = datePeriod;
    }

    public int getBegin() {
        return this.begin;
    }

    public void setBegin(int i) {
        this.begin = i;
    }

    public int getEnd() {
        return this.end;
    }

    public void setEnd(int i) {
        this.end = i;
    }

    public String toString() {
        return "[" + this.regex + "][" + this.begin + "-" + this.end + "]";
    }

    public int compareTo(Object obj) {
        int i = 0;
        if (!(obj instanceof Match)) {
            return 0;
        }
        Match match = (Match) obj;
        if (this.begin < match.begin) {
            i = -1;
        }
        if (this.begin > match.begin) {
            return 1;
        }
        return i;
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        if ($assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }
}

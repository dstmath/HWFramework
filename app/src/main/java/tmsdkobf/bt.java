package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class bt extends fs {
    static ArrayList<bu> dZ;
    public float fScore;
    public String sRiskClassify;
    public String sRiskName;
    public String sRiskReach;
    public String sRiskUrl;
    public String sRule;
    public ArrayList<bu> stRuleTypeID;
    public int uiActionReason;
    public int uiContentType;
    public int uiFinalAction;
    public int uiMatchCnt;
    public int uiShowRiskName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.bt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.bt.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.bt.<clinit>():void");
    }

    public bt() {
        this.uiFinalAction = 0;
        this.uiContentType = 0;
        this.uiMatchCnt = 0;
        this.fScore = 0.0f;
        this.uiActionReason = 0;
        this.stRuleTypeID = null;
        this.sRule = "";
        this.uiShowRiskName = 0;
        this.sRiskClassify = "";
        this.sRiskUrl = "";
        this.sRiskName = "";
        this.sRiskReach = "";
    }

    public fs newInit() {
        return new bt();
    }

    public void readFrom(fq fqVar) {
        this.uiFinalAction = fqVar.a(this.uiFinalAction, 0, true);
        this.uiContentType = fqVar.a(this.uiContentType, 1, true);
        this.uiMatchCnt = fqVar.a(this.uiMatchCnt, 2, true);
        this.fScore = fqVar.a(this.fScore, 3, true);
        this.uiActionReason = fqVar.a(this.uiActionReason, 4, true);
        this.stRuleTypeID = (ArrayList) fqVar.b(dZ, 5, false);
        this.sRule = fqVar.a(6, false);
        this.uiShowRiskName = fqVar.a(this.uiShowRiskName, 7, false);
        this.sRiskClassify = fqVar.a(8, false);
        this.sRiskUrl = fqVar.a(9, false);
        this.sRiskName = fqVar.a(10, false);
        this.sRiskReach = fqVar.a(11, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.uiFinalAction, 0);
        frVar.write(this.uiContentType, 1);
        frVar.write(this.uiMatchCnt, 2);
        frVar.a(this.fScore, 3);
        frVar.write(this.uiActionReason, 4);
        if (this.stRuleTypeID != null) {
            frVar.a(this.stRuleTypeID, 5);
        }
        if (this.sRule != null) {
            frVar.a(this.sRule, 6);
        }
        if (this.uiShowRiskName != 0) {
            frVar.write(this.uiShowRiskName, 7);
        }
        if (this.sRiskClassify != null) {
            frVar.a(this.sRiskClassify, 8);
        }
        if (this.sRiskUrl != null) {
            frVar.a(this.sRiskUrl, 9);
        }
        if (this.sRiskName != null) {
            frVar.a(this.sRiskName, 10);
        }
        if (this.sRiskReach != null) {
            frVar.a(this.sRiskReach, 11);
        }
    }
}

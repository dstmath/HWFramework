package tmsdk.common.module.intelli_sms;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.module.intelli_sms.SmsCheckResult.SmsRuleTypeID;
import tmsdkobf.no;

/* compiled from: Unknown */
public class MMatchSysResult implements Parcelable {
    public static final Creator<MMatchSysResult> CREATOR = null;
    public static final int EM_FINAL_ACTION_DOUBT = 3;
    public static final int EM_FINAL_ACTION_INTERCEPT = 2;
    public static final int EM_FINAL_ACTION_NEXT_STEP = 4;
    public static final int EM_FINAL_ACTION_PASS = 1;
    public int actionReason;
    public int contentType;
    public int finalAction;
    public int matchCnt;
    public int minusMark;
    public no[] ruleTypeID;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.intelli_sms.MMatchSysResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.intelli_sms.MMatchSysResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.intelli_sms.MMatchSysResult.<clinit>():void");
    }

    private MMatchSysResult() {
    }

    public MMatchSysResult(int i, int i2, int i3, int i4, int i5, no[] noVarArr) {
        this.finalAction = i;
        this.contentType = i2;
        this.matchCnt = i3;
        this.minusMark = i4;
        this.actionReason = i5;
        this.ruleTypeID = noVarArr;
    }

    public MMatchSysResult(SmsCheckResult smsCheckResult) {
        this.finalAction = smsCheckResult.uiFinalAction;
        this.contentType = smsCheckResult.uiContentType;
        this.matchCnt = smsCheckResult.uiMatchCnt;
        this.minusMark = (int) smsCheckResult.fScore;
        this.actionReason = smsCheckResult.uiActionReason;
        if (smsCheckResult.stRuleTypeID == null) {
            this.ruleTypeID = null;
            return;
        }
        this.ruleTypeID = new no[smsCheckResult.stRuleTypeID.size()];
        for (int i = 0; i < this.ruleTypeID.length; i += EM_FINAL_ACTION_PASS) {
            this.ruleTypeID[i] = new no((SmsRuleTypeID) smsCheckResult.stRuleTypeID.get(i));
        }
    }

    public static int getSuggestion(MMatchSysResult mMatchSysResult) {
        int i = mMatchSysResult.finalAction;
        return (i > 0 && i <= EM_FINAL_ACTION_NEXT_STEP) ? i == EM_FINAL_ACTION_PASS ? (mMatchSysResult.actionReason == EM_FINAL_ACTION_PASS || mMatchSysResult.actionReason == 5) ? mMatchSysResult.minusMark > 10 ? EM_FINAL_ACTION_NEXT_STEP : EM_FINAL_ACTION_PASS : i : i : -1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.finalAction);
        parcel.writeInt(this.contentType);
        parcel.writeInt(this.matchCnt);
        parcel.writeInt(this.minusMark);
        parcel.writeInt(this.actionReason);
        parcel.writeArray(this.ruleTypeID);
    }
}

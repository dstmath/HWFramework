package tmsdk.common.module.intelli_sms;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.module.intelli_sms.SmsCheckResult.SmsRuleTypeID;
import tmsdkobf.mp;

public class MMatchSysResult implements Parcelable {
    public static final Creator<MMatchSysResult> CREATOR = new Creator<MMatchSysResult>() {
        /* renamed from: aT */
        public MMatchSysResult[] newArray(int i) {
            return new MMatchSysResult[i];
        }

        /* renamed from: h */
        public MMatchSysResult createFromParcel(Parcel parcel) {
            MMatchSysResult mMatchSysResult = new MMatchSysResult();
            mMatchSysResult.finalAction = parcel.readInt();
            mMatchSysResult.contentType = parcel.readInt();
            mMatchSysResult.matchCnt = parcel.readInt();
            mMatchSysResult.minusMark = parcel.readInt();
            mMatchSysResult.actionReason = parcel.readInt();
            Object[] readArray = parcel.readArray(mp.class.getClassLoader());
            if (readArray != null && readArray.length > 0) {
                int length = readArray.length;
                mp[] mpVarArr = new mp[length];
                for (int i = 0; i < length; i++) {
                    mpVarArr[i] = (mp) readArray[i];
                }
                mMatchSysResult.ruleTypeID = mpVarArr;
            }
            return mMatchSysResult;
        }
    };
    public static final int EM_FINAL_ACTION_DOUBT = 3;
    public static final int EM_FINAL_ACTION_INTERCEPT = 2;
    public static final int EM_FINAL_ACTION_NEXT_STEP = 4;
    public static final int EM_FINAL_ACTION_PASS = 1;
    public int actionReason;
    public int contentType;
    public int finalAction;
    public int matchCnt;
    public int minusMark;
    public mp[] ruleTypeID;

    private MMatchSysResult() {
    }

    public MMatchSysResult(int i, int i2, int i3, int i4, int i5, mp[] mpVarArr) {
        this.finalAction = i;
        this.contentType = i2;
        this.matchCnt = i3;
        this.minusMark = i4;
        this.actionReason = i5;
        this.ruleTypeID = mpVarArr;
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
        this.ruleTypeID = new mp[smsCheckResult.stRuleTypeID.size()];
        for (int i = 0; i < this.ruleTypeID.length; i++) {
            this.ruleTypeID[i] = new mp((SmsRuleTypeID) smsCheckResult.stRuleTypeID.get(i));
        }
    }

    public static int getSuggestion(MMatchSysResult mMatchSysResult) {
        int i = mMatchSysResult.finalAction;
        if (i <= 0 || i > 4) {
            return -1;
        }
        if (i != 1) {
            return i;
        }
        if (mMatchSysResult.actionReason == 1 || mMatchSysResult.actionReason == 5) {
            return mMatchSysResult.minusMark > 10 ? 4 : 1;
        } else {
            return i;
        }
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

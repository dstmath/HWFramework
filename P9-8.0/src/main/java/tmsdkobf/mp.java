package tmsdkobf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.module.intelli_sms.SmsCheckResult.SmsRuleTypeID;

public class mp implements Parcelable {
    public static final Creator<mp> CREATOR = new Creator<mp>() {
        /* renamed from: aV */
        public mp[] newArray(int i) {
            return new mp[i];
        }

        /* renamed from: i */
        public mp createFromParcel(Parcel parcel) {
            mp mpVar = new mp();
            mpVar.fg = parcel.readInt();
            mpVar.fh = parcel.readInt();
            return mpVar;
        }
    };
    public int fg;
    public int fh;

    private mp() {
    }

    public mp(SmsRuleTypeID smsRuleTypeID) {
        this.fg = smsRuleTypeID.uiRuleType;
        this.fh = smsRuleTypeID.uiRuleTypeId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.fg);
        parcel.writeInt(this.fh);
    }
}

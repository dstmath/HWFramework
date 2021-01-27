package huawei.android.aod;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class Action implements Parcelable {
    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        /* class huawei.android.aod.Action.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        @Override // android.os.Parcelable.Creator
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };
    private static final String TAG_SIZE = "size";
    protected int mActionType;
    protected Bundle mOpts;

    public Action() {
    }

    public Action(Parcel in) {
        this.mActionType = in.readInt();
        this.mOpts = in.readBundle();
    }

    public void setActionType(int type) {
        this.mActionType = type;
    }

    public int getActionSize() {
        Bundle bundle = this.mOpts;
        if (bundle == null) {
            return 0;
        }
        return bundle.getInt(TAG_SIZE);
    }

    public int getActionType() {
        return this.mActionType;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "{action : %d}", Integer.valueOf(this.mActionType));
    }

    public Bundle dumpOpts() {
        Bundle opts = new Bundle();
        opts.putInt(TAG_SIZE, getActionSize());
        return opts;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mActionType);
        dest.writeBundle(dumpOpts());
    }

    public int getIntOpt(String key) {
        if (this.mOpts == null) {
            this.mOpts = dumpOpts();
        }
        return this.mOpts.getInt(key);
    }
}

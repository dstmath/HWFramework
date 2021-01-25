package huawei.android.aod;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Locale;

public class Trigger implements Parcelable {
    public static final Parcelable.Creator<Trigger> CREATOR = new Parcelable.Creator<Trigger>() {
        /* class huawei.android.aod.Trigger.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Trigger createFromParcel(Parcel in) {
            return new Trigger(in);
        }

        @Override // android.os.Parcelable.Creator
        public Trigger[] newArray(int size) {
            return new Trigger[size];
        }
    };
    private int mActionBufferSize;
    private int mLayerCount;
    private ArrayList<Layer> mLayerList;
    private int mTriggerTrivals;
    private int mTriggerType;

    public Trigger(Parcel in) {
        readFromParcel(in);
    }

    public Trigger(int type, int length) {
        this.mTriggerType = type;
        this.mActionBufferSize = length;
    }

    public void setmTriggerTrivals(int triggerTrivals) {
        this.mTriggerTrivals = triggerTrivals;
    }

    public void setLayerList(ArrayList<Layer> layerList) {
        this.mLayerCount = layerList.size();
        this.mLayerList = layerList;
    }

    public int getTriggerType() {
        return this.mTriggerType;
    }

    public int getLayerCount() {
        return this.mLayerCount;
    }

    public ArrayList<Layer> getLayerList() {
        ArrayList<Layer> arrayList = this.mLayerList;
        return arrayList != null ? arrayList : new ArrayList<>(0);
    }

    public int getActionBufferSize() {
        return this.mActionBufferSize;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "{trigger : %d, layers : %s}", Integer.valueOf(this.mTriggerType), this.mLayerList);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flag) {
        out.writeInt(this.mTriggerType);
        out.writeInt(this.mActionBufferSize);
        out.writeInt(this.mTriggerTrivals);
        out.writeInt(this.mLayerCount);
        out.writeList(this.mLayerList);
    }

    private void readFromParcel(Parcel in) {
        this.mTriggerType = in.readInt();
        this.mActionBufferSize = in.readInt();
        this.mTriggerTrivals = in.readInt();
        this.mLayerCount = in.readInt();
        this.mLayerList = in.readArrayList(null);
    }
}
